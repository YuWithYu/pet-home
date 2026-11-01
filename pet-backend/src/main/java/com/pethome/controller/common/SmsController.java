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
     * 取客户端 IP：
     * 仅当直连来源是可信代理（本机/内网，通常是 Nginx）时才信任 X-Forwarded-For，
     * 并取最右一跳（代理追加的真实客户端）；直连部署一律使用 remoteAddr，
     * 防止客户端自行伪造 XFF 绕过按 IP 的发送限流。
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remote = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty() && isTrustedProxy(remote)) {
            String[] parts = forwarded.split(",");
            String last = parts[parts.length - 1].trim();
            if (!last.isEmpty()) {
                return last;
            }
        }
        return remote;
    }

    /** 可信代理判定：回环地址与内网网段（与常规 Nginx 同机/内网部署一致） */
    private static boolean isTrustedProxy(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return ip.equals("127.0.0.1") || ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("10.") || ip.startsWith("192.168.")
                || ip.matches("172\\.(1[6-9]|2[0-9]|3[0-1])\\.\\d{1,3}\\.\\d{1,3}");
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
