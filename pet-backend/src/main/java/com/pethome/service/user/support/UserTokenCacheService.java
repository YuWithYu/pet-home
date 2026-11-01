package com.pethome.service.user.support;

import com.pethome.entity.User;
import com.pethome.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 登录 token 与用户信息缓存（Redis）相关逻辑，从 UserServiceImpl 抽取而来。
 */
@Component
public class UserTokenCacheService {

    private static final Logger logger = LoggerFactory.getLogger(UserTokenCacheService.class);

    // Redis 缓存 key 前缀
    private static final String LOGIN_TOKEN_PREFIX = "login:token:";
    private static final String USER_INFO_PREFIX = "user:info:";
    // 缓存过期时间
    private static final long LOGIN_TOKEN_EXPIRE_HOURS = 1; // 登录 token 1小时过期
    private static final long USER_INFO_EXPIRE_MINUTES = 30; // 用户信息 30分钟过期

    @Autowired
    private com.pethome.mapper.UserMapper mpUserMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    public void cacheLoginToken(String token, User user) {
        if (stringRedisTemplate == null || token == null || user == null) {
            logger.warn("缓存登录 token 失败：参数为空或 StringRedisTemplate 未注入");
            return;
        }

        try {
            String loginKey = LOGIN_TOKEN_PREFIX + token;
            String userInfoKey = USER_INFO_PREFIX + user.getId();

            // 缓存登录 token -> 用户信息
            String userJson = JSONUtil.toJSONString(user);
            stringRedisTemplate.opsForValue().set(loginKey, userJson, LOGIN_TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            logger.info("成功缓存登录 token，key: {}, userId: {}, 过期时间: {} 小时", loginKey, user.getId(), LOGIN_TOKEN_EXPIRE_HOURS);

            // 同时缓存用户信息（用于快速查询）
            stringRedisTemplate.opsForValue().set(userInfoKey, userJson, USER_INFO_EXPIRE_MINUTES, TimeUnit.MINUTES);
            logger.info("成功缓存用户信息，key: {}, userId: {}, 过期时间: {} 分钟", userInfoKey, user.getId(), USER_INFO_EXPIRE_MINUTES);

        } catch (Exception e) {
            logger.error("缓存登录 token 失败，userId: {}, 错误: {}", user.getId(), e.getMessage(), e);
        }
    }

    public User getUserByToken(String token) {
        if (stringRedisTemplate == null) {
            logger.warn("getUserByToken - StringRedisTemplate 未注入，无法从缓存获取用户信息");
            return null;
        }

        if (!StringUtils.hasText(token)) {
            logger.debug("getUserByToken - token 为空");
            return null;
        }

        try {
            String loginKey = LOGIN_TOKEN_PREFIX + token;
            String userJson = stringRedisTemplate.opsForValue().get(loginKey);

            if (StringUtils.hasText(userJson)) {
                User cachedUser = JSONUtil.parseObject(userJson, User.class);
                logger.debug("getUserByToken - 命中缓存, userId={}", cachedUser != null ? cachedUser.getId() : null);

                // 关键修复：每次从 token 获取用户时都实时校验账号状态，避免“后台禁用后旧 token 仍可用”
                if (cachedUser == null || cachedUser.getId() == null) {
                    stringRedisTemplate.delete(loginKey);
                    return null;
                }
                User latestUser = mpUserMapper.selectById(cachedUser.getId());
                if (latestUser == null || latestUser.getStatus() == null || latestUser.getStatus() != 1) {
                    String userInfoKey = USER_INFO_PREFIX + cachedUser.getId();
                    stringRedisTemplate.delete(loginKey);
                    stringRedisTemplate.delete(userInfoKey);
                    logger.warn("检测到账号已禁用或不存在，令牌失效，userId: {}, status: {}",
                            cachedUser.getId(), latestUser != null ? latestUser.getStatus() : null);
                    return null;
                }

                // 续期 token，并返回最新用户信息（避免使用缓存中的旧状态）
                stringRedisTemplate.expire(loginKey, LOGIN_TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
                return latestUser;
            }

            logger.debug("getUserByToken - token 无效或已过期");
            return null;
        } catch (Exception e) {
            // 不打印 token 内容，避免敏感信息泄露
            logger.error("getUserByToken - 从缓存获取用户信息失败", e);
            return null;
        }
    }

    public void clearUserCache(Long userId) {
        if (stringRedisTemplate == null || userId == null) {
            return;
        }

        try {
            String userInfoKey = USER_INFO_PREFIX + userId;
            Boolean deleted = stringRedisTemplate.delete(userInfoKey);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("成功清除用户信息缓存，userId: {}, key: {}", userId, userInfoKey);
            } else {
                logger.info("用户信息缓存不存在，userId: {}, key: {}", userId, userInfoKey);
            }
        } catch (Exception e) {
            logger.error("清除用户信息缓存失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
        }
    }
}
