package com.pethome.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.dypnsapi.model.v20170525.SendSmsVerifyCodeRequest;
import com.aliyuncs.dypnsapi.model.v20170525.SendSmsVerifyCodeResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.pethome.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信服务实现类。
 * 使用阿里云「号码认证服务-短信认证」，签名/模板在控制台「短信认证服务」下（如 速通互联验证码、模板 100001）。
 * 配置 sms.aliyun.enabled=true 并填写密钥、签名、模板后，验证码会真实下发到用户手机。
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${sms.aliyun.enabled:false}")
    private boolean smsAliyunEnabled;
    @Value("${sms.aliyun.access-key-id:}")
    private String accessKeyId;
    @Value("${sms.aliyun.access-key-secret:}")
    private String accessKeySecret;
    @Value("${sms.aliyun.sign-name:}")
    private String signName;
    @Value("${sms.aliyun.template-code:}")
    private String templateCode;
    @Value("${sms.aliyun.region-id:cn-hangzhou}")
    private String regionId;

    // 临时存储验证码（实际项目中建议使用 Redis）
    private static final Map<String, String> verificationCodes = new ConcurrentHashMap<>();
    private static final Map<String, Long> codeExpiry = new ConcurrentHashMap<>();

    @Override
    public String sendVerificationCode(String phone) {
        try {
            logger.info("正在发送验证码短信到手机号: {}", maskPhone(phone));

            String code = generateVerificationCode();
            verificationCodes.put(phone, code);
            codeExpiry.put(phone, System.currentTimeMillis() + 5 * 60 * 1000);

            boolean sent = false;
            String failReason = null;
            if (smsAliyunEnabled && accessKeyId != null && !accessKeyId.isEmpty()
                    && accessKeySecret != null && !accessKeySecret.isEmpty()
                    && signName != null && !signName.isEmpty()
                    && templateCode != null && !templateCode.isEmpty()) {
                try {
                    DefaultAcsClient client = createDypnsClient();
                    SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest();
                    request.setPhoneNumber(phone);
                    request.setSignName(signName);
                    request.setTemplateCode(templateCode);
                    request.setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}");
                    request.setCountryCode("86");
                    SendSmsVerifyCodeResponse response = client.getAcsResponse(request);
                    if (response != null && "OK".equalsIgnoreCase(response.getCode())) {
                        sent = true;
                        logger.info("阿里云号码认证短信已下发，手机号: {}", maskPhone(phone));
                    } else {
                        failReason = response != null && response.getMessage() != null ? response.getMessage() : "返回异常";
                        logger.warn("阿里云短信发送失败，手机号: {}, Code: {}, Message: {}", maskPhone(phone),
                                response != null ? response.getCode() : null,
                                response != null ? response.getMessage() : null);
                    }
                } catch (Exception e) {
                    if (e.getClass().getSimpleName().equals("ClientException")) {
                        try {
                            failReason = (String) e.getClass().getMethod("getErrMsg").invoke(e);
                        } catch (Throwable t) {
                            failReason = e.getMessage();
                        }
                    } else {
                        failReason = e.getMessage() != null ? e.getMessage() : "调用异常";
                    }
                    if (failReason == null) failReason = e.getMessage() != null ? e.getMessage() : "调用异常";
                    logger.error("阿里云短信调用异常，手机号: {}, 错误: {}", maskPhone(phone), e.getMessage(), e);
                }
            }

            if (!sent) {
                if (!smsAliyunEnabled) {
                    logger.info("=================================================");
                    logger.info("📱 验证码未真实下发（未配置阿里云），请从控制台查看验证码");
                    logger.info("📞 手机号: {}", maskPhone(phone));
                    logger.info("🔐 验证码: {}", code);
                    logger.info("=================================================");
                    return "success";
                }
                logger.info("📞 手机号: {} 发送失败，原因: {}；验证码已存，可从日志查看: {}", maskPhone(phone), failReason, code);
                return failReason != null ? failReason : "验证码发送失败，请稍后重试";
            }
            return "success";
        } catch (Exception e) {
            logger.error("发送验证码短信失败，手机号: {}, 错误: {}", maskPhone(phone), e.getMessage(), e);
            verificationCodes.remove(phone);
            codeExpiry.remove(phone);
            return (e.getMessage() != null ? e.getMessage() : "验证码发送异常，请稍后重试");
        }
    }

    /** 号码认证服务（短信认证）endpoint: dypnsapi.aliyuncs.com */
    private DefaultAcsClient createDypnsClient() throws Exception {
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint(regionId, regionId, "Dypnsapi", "dypnsapi.aliyuncs.com");
        return new DefaultAcsClient(profile);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
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
