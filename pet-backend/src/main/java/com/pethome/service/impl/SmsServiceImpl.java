package com.pethome.service.impl;

import com.pethome.service.SmsService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信服务实现类
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);
    
    // 临时存储验证码（实际项目中应该使用Redis）
    private static final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private static final Map<String, Long> codeExpiry = new ConcurrentHashMap<>();

    @Override
    public String sendVerificationCode(String phone) {
        try {
            logger.info("正在发送验证码短信到手机号: {}", phone);

            // 生成6位随机验证码
            String code = generateVerificationCode();
            
            // 存储验证码和过期时间（5分钟）
            verificationCodes.put(phone, code);
            codeExpiry.put(phone, System.currentTimeMillis() + 5 * 60 * 1000); // 5分钟后过期

            // 这里应该集成真实的短信服务商，比如阿里云短信服务、腾讯云短信服务等
            // 为了演示，这里只是模拟发送成功
            
            // 醒目的验证码日志输出
            logger.info("=================================================");
            logger.info("📱 验证码发送成功");
            logger.info("📞 手机号: {}", phone);
            logger.info("🔐 验证码: {}", code);
            logger.info("⏰ 有效期: 5分钟");
            logger.info("=================================================");
            
            // 在控制台打印更醒目的提示
            System.out.println("\n" +
                "╔════════════════════════════════════════╗\n" +
                "║          📱 短信验证码                  ║\n" +
                "╠════════════════════════════════════════╣\n" +
                "║  手机号: " + phone + "                 ║\n" +
                "║  验证码: " + code + "                        ║\n" +
                "║  有效期: 5分钟                          ║\n" +
                "╚════════════════════════════════════════╝\n");

            // 返回成功状态
            return "success";

        } catch (Exception e) {
            logger.error("发送验证码短信失败，手机号: {}, 错误: {}", phone, e.getMessage(), e);
            return "failed";
        }
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        try {
            logger.info("验证验证码，手机号: {}, 验证码: {}", phone, code);
            
            // 检查验证码是否存在
            String storedCode = verificationCodes.get(phone);
            if (storedCode == null) {
                logger.warn("验证码不存在，手机号: {}", phone);
                return false;
            }
            
            // 检查验证码是否过期
            Long expiryTime = codeExpiry.get(phone);
            if (expiryTime == null || System.currentTimeMillis() > expiryTime) {
                logger.warn("验证码已过期，手机号: {}", phone);
                // 清理过期的验证码
                verificationCodes.remove(phone);
                codeExpiry.remove(phone);
                return false;
            }
            
            // 验证验证码是否正确
            boolean isValid = storedCode.equals(code);
            if (isValid) {
                logger.info("验证码验证成功，手机号: {}", phone);
                // 验证成功后删除验证码（一次性使用）
                verificationCodes.remove(phone);
                codeExpiry.remove(phone);
            } else {
                logger.warn("验证码错误，手机号: {}, 输入: {}, 正确: {}", phone, code, storedCode);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("验证验证码失败，手机号: {}, 错误: {}", phone, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 生成6位随机验证码
     */
    private String generateVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
