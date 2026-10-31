package com.pethome.controller.common;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.service.common.SmsService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 短信验证码控制器
 */
@RestController
@RequestMapping("/api/sms")
@Tag(name = "短信验证码")
public class SmsController {

    @Autowired
    private SmsService smsService;

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

    @PostMapping("/send")
    @Operation(summary = "发送短信验证码")
    public Map<String, Object> sendSms(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        // 获取手机号，处理可能的数字类型
        String phone = params.get("phone") != null ? params.get("phone").toString() : null;
        // 校验、单 IP 限流与异常兜底统一由服务层处理，保持返回结构 {code, msg} 不变
        return smsService.sendSmsCode(phone, resolveClientIp(request));
    }
}
