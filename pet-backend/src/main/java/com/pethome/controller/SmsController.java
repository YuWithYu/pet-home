package com.pethome.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.service.SmsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 短信验证码控制器
 */
@RestController
@RequestMapping("/api/sms")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "短信验证码")
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    @ApiOperation("发送短信验证码")
    public Map<String, Object> sendSms(@RequestBody Map<String, Object> params) {
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
            result.put("code", 500);
            result.put("msg", "发送验证码失败：" + e.getMessage());
            logger.error("发送验证码失败", e);
        }

        return result;
    }
}

