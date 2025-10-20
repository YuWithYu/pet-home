package com.pethome.service;

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
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 验证结果
     */
    boolean verifyCode(String phone, String code);
}
