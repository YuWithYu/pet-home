package com.pethome.service.user.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录防爆破：失败次数计数、锁定判断与失败原因归类，从 UserAuthAppServiceImpl 抽取而来。
 *
 * <p>/api/user/login 同时支持 admin 与 user 表登录，必须与 /api/admin/login 同等失败锁定。</p>
 */
@Service
public class UserLoginGuardService {

    private static final Logger log = LoggerFactory.getLogger(UserLoginGuardService.class);

    private static final String LOGIN_FAIL_PREFIX = "user:login:fail:";
    /** 连续失败达到该次数即锁定 */
    public static final int MAX_LOGIN_FAILS = 5;
    private static final long LOGIN_LOCK_MINUTES = 15;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /** 登录失败计数键：优先账号，其次手机号（同一账号的多入口共用同一计数，防止绕过） */
    public String resolveIdentifier(String username, String phone) {
        if (username != null && !username.trim().isEmpty()) {
            return username.trim().toLowerCase();
        }
        return phone != null ? phone.trim() : "";
    }

    /** 仅凭证类错误计入失败次数，避免「账号被禁用」等业务提示也把正常用户锁死 */
    public boolean isCredentialError(String message) {
        return message != null && (message.contains("密码错误")
                || message.contains("用户名或密码错误") || message.contains("账号不存在")
                || message.contains("账号或密码错误"));
    }

    public int readFailCount(String identifier) {
        if (stringRedisTemplate == null) {
            return 0;
        }
        try {
            String value = stringRedisTemplate.opsForValue().get(LOGIN_FAIL_PREFIX + identifier);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            log.warn("读取登录失败次数异常: identifier={}", identifier, e);
            return 0;
        }
    }

    /** 记录一次登录失败（首次失败时设置锁定窗口） */
    public void recordFailure(String identifier) {
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            Long count = stringRedisTemplate.opsForValue().increment(LOGIN_FAIL_PREFIX + identifier);
            if (count != null && count == 1L) {
                stringRedisTemplate.expire(LOGIN_FAIL_PREFIX + identifier, LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.warn("记录登录失败次数异常: identifier={}", identifier, e);
        }
    }

    public void clearFailures(String identifier) {
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.delete(LOGIN_FAIL_PREFIX + identifier);
        } catch (Exception e) {
            log.warn("清除登录失败次数异常: identifier={}", identifier, e);
        }
    }
}
