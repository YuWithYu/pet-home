package com.pethome.util;

import com.pethome.entity.User;
import com.pethome.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 从 Authorization 头解析当前登录用户 ID（不信任前端传入的 userId）
 *
 * 解析链（与历史行为保持一致）：
 * 1. 微信登录的 token_<userId>_<timestamp> 格式直取
 * 2. Redis token 反查（手机号/微信登录会话）
 * 3. JWT subject 反查：username → phone → openid（长串）→ 数字 ID
 * 注意：token subject 可能是 username、手机号（纯数字）或 openid，
 * 不能直接 Long.valueOf(subject)——手机号是纯数字会被误解析成错误的 userId。
 */
@Component
public class UserTokenResolver {

    private static final Logger log = LoggerFactory.getLogger(UserTokenResolver.class);

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    /**
     * @param authHeader Authorization 头（容忍缺少 Bearer 前缀）；无效返回 null
     */
    public Long resolveUserId(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return null;
        }
        String token = authHeader.trim();
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }
        return getUserIdFromBearer(token);
    }

    /** authHeader 必须带 Bearer 前缀（与 OrderController 历史实现一致） */
    public Long getUserIdFromBearer(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        // 微信登录等：UserServiceImpl 生成 token_<userId>_<timestamp>，本身不是 JWT；Redis 未命中时仍可从格式解析用户
        if (token.startsWith("token_")) {
            String[] parts = token.split("_", 3);
            if (parts.length >= 2) {
                try {
                    Long uid = Long.parseLong(parts[1]);
                    if (userService != null) {
                        User u = userService.getUserById(uid);
                        if (u != null && (u.getStatus() == null || u.getStatus() == 1)) {
                            return uid;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        try {
            User cachedUser = userService != null ? userService.getUserByToken(token) : null;
            if (cachedUser != null) {
                return cachedUser.getId();
            }
        } catch (Exception e) {
            // ignore
        }
        if (jwtUtil != null) {
            try {
                String subject = jwtUtil.getUsernameFromToken(token);
                if (subject != null && userService != null) {
                    User user = userService.getUserByUsername(subject);
                    if (user == null) {
                        user = userService.getUserByPhone(subject);
                    }
                    if (user == null && subject.length() >= 20) {
                        user = userService.getUserByOpenid(subject);
                    }
                    if (user == null && subject.matches("\\d+")) {
                        user = userService.getUserById(Long.parseLong(subject));
                    }
                    if (user != null) {
                        return user.getId();
                    }
                }
            } catch (Exception e) {
                log.debug("JWT 解析用户失败: {}", e.getMessage());
            }
        }
        return null;
    }
}
