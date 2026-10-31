package com.pethome.service.user.impl;

import com.pethome.entity.User;
import com.pethome.security.JwtUtil;
import com.pethome.service.user.UserService;
import com.pethome.service.user.UserTokenService;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTokenServiceImpl implements UserTokenService {

    private static final Logger log = LoggerFactory.getLogger(UserTokenServiceImpl.class);

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    @Override
    public Long resolveUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return resolveUserIdByToken(authHeader.substring(BEARER_PREFIX.length()).trim());
    }

    @Override
    public Long resolveUserIdByToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 1. Redis 登录缓存优先（登录时写入 token -> user）
        try {
            User cached = userService.getUserByToken(token);
            if (cached != null) {
                return cached.getId();
            }
        } catch (Exception e) {
            log.debug("读取登录缓存失败，继续解析 JWT", e);
        }
        // 2. 解析 JWT，用 subject 反查用户
        if (jwtUtil == null) {
            return null;
        }
        try {
            Claims claims = jwtUtil.parseToken(token);
            String subject = claims != null ? claims.getSubject() : null;
            if (subject == null) {
                return null;
            }
            return findUserIdBySubject(subject);
        } catch (Exception e) {
            log.debug("Token 解析失败", e);
            return null;
        }
    }

    /** subject 可能是用户名/手机号/数字ID，逐个尝试；手机号不能被误当成 userId */
    private Long findUserIdBySubject(String subject) {
        User user = userService.getUserByUsername(subject);
        if (user == null) {
            user = userService.getUserByPhone(subject);
        }
        if (user == null && subject.matches("\\d+")) {
            user = userService.getUserById(Long.parseLong(subject));
        }
        return user != null ? user.getId() : null;
    }
}
