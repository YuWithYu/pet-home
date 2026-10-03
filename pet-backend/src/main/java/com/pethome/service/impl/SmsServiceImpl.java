package com.pethome.service.impl;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.dypnsapi.model.v20170525.SendSmsVerifyCodeRequest;
import com.aliyuncs.dypnsapi.model.v20170525.SendSmsVerifyCodeResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.pethome.service.SmsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现类。
 * 使用阿里云「号码认证服务-短信认证」，签名/模板在控制台「短信认证服务」下（如 速通互联验证码、模板 100001）。
 * 配置 sms.aliyun.enabled=true 并填写密钥、签名、模板后，验证码会真实下发到用户手机。
 *
 * 安全设计：
 * 1. 验证码存 Redis（5 分钟过期），支持多实例部署
 * 2. 发送频率限制：同一手机号 60 秒内 1 条、每日上限 10 条（防短信轰炸/费用消耗）
 * 3. 校验尝试限制：同一验证码失败 5 次即作废（防暴力破解）
 * 4. 验证码使用 SecureRandom 生成，任何日志不输出明文验证码（本地开发未配置阿里云时除外）
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    private static final String CODE_KEY_PREFIX = "sms:code:";
    private static final String ATTEMPT_KEY_PREFIX = "sms:attempt:";
    private static final String INTERVAL_KEY_PREFIX = "sms:limit:interval:";
    private static final String DAILY_KEY_PREFIX = "sms:limit:daily:";

    /** 验证码有效期（分钟） */
    private static final long CODE_TTL_MINUTES = 5;
    /** 同一手机号两次发送最小间隔（秒） */
    private static final long SEND_INTERVAL_SECONDS = 60;
    /** 同一手机号每日发送上限 */
    private static final long DAILY_SEND_LIMIT = 10;
    /** 验证码最大失败尝试次数，超过即作废 */
    private static final long MAX_VERIFY_ATTEMPTS = 5;

    /** 密码学安全随机数发生器 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String sendVerificationCode(String phone) {
        try {
            // 1. 频率限制：60 秒内仅允许发送 1 条（SETNX 原子判断）
            String intervalKey = INTERVAL_KEY_PREFIX + phone;
            Boolean first = stringRedisTemplate.opsForValue()
                    .setIfAbsent(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(first)) {
                logger.warn("短信发送过于频繁（60s 限流命中），手机号: {}", maskPhone(phone));
                return "发送过于频繁，请稍后再试";
            }

            // 2. 每日上限：同一手机号每天最多 10 条
            String dailyKey = DAILY_KEY_PREFIX + phone;
            Long dailyCount = stringRedisTemplate.opsForValue().increment(dailyKey);
            if (dailyCount != null && dailyCount == 1L) {
                stringRedisTemplate.expire(dailyKey, 1, TimeUnit.DAYS);
            }
            if (dailyCount != null && dailyCount > DAILY_SEND_LIMIT) {
                logger.warn("短信发送超过每日上限，手机号: {}, 次数: {}", maskPhone(phone), dailyCount);
                return "今日验证码发送次数已达上限，请明天再试";
            }

            logger.info("正在发送验证码短信到手机号: {}", maskPhone(phone));

            // 3. 生成验证码（SecureRandom）并写入 Redis，同时清除旧的失败计数
            String code = generateVerificationCode();
            stringRedisTemplate.opsForValue()
                    .set(CODE_KEY_PREFIX + phone, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);
            stringRedisTemplate.delete(ATTEMPT_KEY_PREFIX + phone);

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
                    // 仅限本地开发（未配置阿里云）：验证码无处下发，只能从控制台日志查看。
                    // 生产环境严禁开启该路径——验证码不得写入日志。
                    logger.info("=================================================");
                    logger.info("📱 验证码未真实下发（未配置阿里云），请从控制台查看验证码");
                    logger.info("📞 手机号: {}", maskPhone(phone));
                    logger.info("🔐 验证码: {}", code);
                    logger.info("=================================================");
                    return "success";
                }
                // 发送失败：不再把验证码写入日志（敏感信息），并回滚已写入的验证码与限流计数
                logger.info("📞 手机号: {} 发送失败，原因: {}", maskPhone(phone), failReason);
                stringRedisTemplate.delete(CODE_KEY_PREFIX + phone);
                stringRedisTemplate.delete(intervalKey);
                return failReason != null ? failReason : "验证码发送失败，请稍后重试";
            }
            return "success";
        } catch (Exception e) {
            logger.error("发送验证码短信失败，手机号: {}, 错误: {}", maskPhone(phone), e.getMessage(), e);
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
            String codeKey = CODE_KEY_PREFIX + phone;
            // 检查验证码是否存在（Redis TTL 自动过期）
            String storedCode = stringRedisTemplate.opsForValue().get(codeKey);
            if (storedCode == null) {
                logger.warn("验证码不存在或已过期，手机号: {}", maskPhone(phone));
                return false;
            }

            // 尝试次数限制：同一验证码失败 5 次即作废（防暴力破解）
            String attemptKey = ATTEMPT_KEY_PREFIX + phone;
            Long attempts = stringRedisTemplate.opsForValue().increment(attemptKey);
            if (attempts != null && attempts == 1L) {
                stringRedisTemplate.expire(attemptKey, CODE_TTL_MINUTES, TimeUnit.MINUTES);
            }
            if (attempts != null && attempts > MAX_VERIFY_ATTEMPTS) {
                logger.warn("验证码错误次数超过上限，已作废，手机号: {}", maskPhone(phone));
                stringRedisTemplate.delete(codeKey);
                stringRedisTemplate.delete(attemptKey);
                return false;
            }

            // 校验（常量时间比较，防时序侧信道）
            boolean isValid = MessageDigest.isEqual(
                    storedCode.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    String.valueOf(code).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            if (isValid) {
                logger.info("验证码验证成功，手机号: {}", maskPhone(phone));
                // 验证成功后删除验证码（一次性使用）
                stringRedisTemplate.delete(codeKey);
                stringRedisTemplate.delete(attemptKey);
            } else {
                // 注意：日志中不得输出正确验证码（防止日志泄露导致验证被绕过）
                logger.warn("验证码错误，手机号: {}", maskPhone(phone));
            }
            return isValid;
        } catch (Exception e) {
            logger.error("验证验证码失败，手机号: {}, 错误: {}", maskPhone(phone), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成6位随机验证码（SecureRandom，密码学安全）
     */
    private String generateVerificationCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1000000));
    }
}
