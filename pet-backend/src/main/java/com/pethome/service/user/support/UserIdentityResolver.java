package com.pethome.service.user.support;

import com.pethome.common.BusinessException;
import com.pethome.entity.User;
import com.pethome.security.JwtUtil;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户端身份解析器。
 *
 * <p>把「Authorization / token → 登录用户」的解析链收敛到服务层，统一处理
 * Redis 缓存 token、JWT subject（手机号 / 用户名 / 邮箱）等历史形态，
 * 避免每个接口在 Controller 里重复手写。</p>
 */
@Component
public class UserIdentityResolver {

    private static final Logger log = LoggerFactory.getLogger(UserIdentityResolver.class);

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 解析当前登录用户 ID：缓存优先，其次 JWT subject（先用户名后手机号）。
     * 解析失败返回 null，由调用方决定错误码。
     */
    public Long resolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = authorization.substring(BEARER_PREFIX.length());
        try {
            User cachedUser = userService.getUserByToken(token);
            if (cachedUser != null) {
                return cachedUser.getId();
            }
        } catch (Exception e) {
            log.debug("解析登录用户: 从缓存获取用户失败: {}", e.toString());
        }
        try {
            String subject = jwtUtil.getUsernameFromToken(token);
            if (subject != null) {
                User user = userService.getUserByUsername(subject);
                if (user == null) {
                    user = userService.getUserByPhone(subject);
                }
                if (user != null) {
                    return user.getId();
                }
            }
        } catch (Exception e) {
            log.warn("解析登录用户: Token 解析失败", e);
        }
        return null;
    }

    /** 宽松模式：缓存 → JWT subject（先手机号后用户名），任何一步失败均返回 null */
    public User resolveUser(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return resolveUserByToken(authorization.substring(BEARER_PREFIX.length()).trim());
    }

    /** 宽松模式（裸 token） */
    public User resolveUserByToken(String token) {
        User user = userService.getUserByToken(token);
        if (user != null) {
            return user;
        }
        try {
            String subject = jwtUtil.getUsernameFromToken(token);
            user = findBySubject(subject);
        } catch (Exception e) {
            log.debug("从 token 解析用户失败: {}", e.toString());
        }
        return user;
    }

    /**
     * 严格模式（裸 token）：JWT 解析异常统一抛出「登录已过期，请重新登录」，
     * 与资金/账号类敏感接口原有错误语义保持一致。解析不到用户时返回 null。
     */
    public User resolveUserStrict(String token) {
        User user = userService.getUserByToken(token);
        if (user != null) {
            return user;
        }
        try {
            String subject = jwtUtil.getUsernameFromToken(token);
            user = findBySubject(subject);
        } catch (Exception e) {
            log.warn("从 token 解析用户失败", e);
            throw new BusinessException("登录已过期，请重新登录");
        }
        return user;
    }

    private User findBySubject(String subject) {
        if (subject == null || subject.isEmpty()) {
            return null;
        }
        User user = userService.getUserByPhone(subject);
        if (user == null) {
            user = userService.getUserByUsername(subject);
        }
        return user;
    }
}
