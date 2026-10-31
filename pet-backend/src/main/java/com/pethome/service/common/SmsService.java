package com.pethome.service.common;

import java.util.Map;

/**
 * 短信服务接口
 */
public interface SmsService {

    /**
     * 发送验证码短信
     * @param phone 手机号
     * @return 发送结果
     */
    String sendVerificationCode(String phone);

    /**
     * 发送验证码并返回统一响应结构 {code, msg}（含手机号校验与单 IP 限流）
     * @param phone 手机号
     * @param clientIp 调用方 IP，用于单 IP 每小时发送上限
     * @return 响应结构，code 语义：200 成功 / 1 业务提示 / 500 系统异常
     */
    Map<String, Object> sendSmsCode(String phone, String clientIp);

    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifyCode(String phone, String code);
}
