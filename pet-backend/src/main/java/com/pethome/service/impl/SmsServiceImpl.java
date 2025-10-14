package com.pethome.service.impl;

import com.pethome.service.SmsService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 短信服务实现类
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Override
    public String sendVerificationCode(String phone) {
        try {
            logger.info("正在发送验证码短信到手机号: {}", phone);

            // 这里应该集成真实的短信服务商，比如阿里云短信服务、腾讯云短信服务等
            // 为了演示，这里只是模拟发送成功

            // 模拟短信发送逻辑
            // 实际项目中应该：
            // 1. 生成6位随机验证码
            // 2. 将验证码存储到Redis或数据库中，设置过期时间（5分钟）
            // 3. 调用短信服务商的API发送短信

            logger.info("验证码短信发送成功，手机号: {}", phone);

            // 返回成功状态
            return "success";

        } catch (Exception e) {
            logger.error("发送验证码短信失败，手机号: {}, 错误: {}", phone, e.getMessage(), e);
            return "failed";
        }
    }
}
