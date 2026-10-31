package com.pethome.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.service.SmsService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 短信验证码控制器
 */
@RestController
@RequestMapping("/api/sms")
@Tag(name = "短信验证码")
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private SmsService smsService;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /** 单 IP 每小时发送上限：与「手机号 60s/条 + 10 条/日」叠加，防止换号轰炸消耗短信费用 */
    private static final long IP_HOURLY_LIMIT = 60;
    private static final String IP_LIMIT_KEY_PREFIX = "sms:limit:ip:";

    /**
     * 取客户端 IP：Nginx 的 X-Forwarded-For 会追加真实客户端，取最右一跳；
     * 无代理头时退回 remoteAddr（直连部署）。
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            String[] parts = forwarded.split(",");
            String last = parts[parts.length - 1].trim();
            if (!last.isEmpty()) {
                return last;
            }
        }
        return request.getRemoteAddr();
    }

    /** 安全修复：原实现只按手机号限流，攻击者换号即可绕过；此处增加 IP 维度计数 */
    private boolean overIpLimit(HttpServletRequest request) {
        if (stringRedisTemplate == null) {
            return false;
        }
        String ip = resolveClientIp(request);
        String key = IP_LIMIT_KEY_PREFIX + ip;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, 1, TimeUnit.HOURS);
        }
        if (count != null && count > IP_HOURLY_LIMIT) {
            logger.warn("短信发送超过单 IP 每小时上限: ip={}, 次数={}", ip, count);
            return true;
        }
        return false;
    }

    @PostMapping("/send")
    @Operation(summary = "发送短信验证码")
    public Map<String, Object> sendSms(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取手机号，处理可能的数字类型
            String phone = params.get("phone") != null ? params.get("phone").toString() : null;

            if (phone == null || phone.trim().isEmpty()) {
                result.put("code", 1);
                result.put("msg", "手机号不能为空");
                return result;
            }

            // 验证手机号格式
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                result.put("code", 1);
                result.put("msg", "请输入正确的手机号");
                return result;
            }

            // 不再在此处拦截「该手机号已被绑定」：忘记密码、忘记原密码需要向当前账号已绑定的手机号发验证码；
            // 是否被其他账号占用在「注册/绑定/更换手机」等提交时再校验即可。

            if (overIpLimit(request)) {
                result.put("code", 1);
                result.put("msg", "请求过于频繁，请稍后再试");
                return result;
            }

            // 调用短信服务发送验证码
            String smsResult = smsService.sendVerificationCode(phone);

            if ("success".equals(smsResult)) {
                result.put("code", 200);
                result.put("msg", "验证码发送成功");
            } else {
                result.put("code", 1);
                String errMsg = smsResult != null ? smsResult.trim() : "";
                if ("failed".equals(errMsg) || "fail".equals(errMsg) || errMsg.isEmpty()) {
                    errMsg = "验证码发送失败，请稍后重试";
                }
                result.put("msg", errMsg);
            }

        } catch (Exception e) {
            logger.error("发送验证码失败", e);
            result.put("code", 500);
            // 安全修复：异常细节只进日志，不回传内部信息
            result.put("msg", "发送验证码失败，请稍后重试");
        }

        return result;
    }
}

