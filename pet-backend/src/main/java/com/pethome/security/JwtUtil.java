package com.pethome.security;

import com.pethome.common.BusinessException;
import com.pethome.security.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /** HS512 安全密钥最小字节长度（512 位 = 64 字节） */
    private static final int MIN_SECRET_BYTES = 64;

    @Autowired
    private JwtConfig jwtConfig;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    /**
     * 启动时校验密钥强度，避免短密钥导致的签名强度不足
     */
    @PostConstruct
    public void validateSecret() {
        String secret = jwtConfig.getSecret();
        boolean isProd = activeProfile != null && activeProfile.contains("prod");
        if (secret == null || secret.trim().isEmpty()) {
            if (isProd) {
                throw new IllegalStateException(
                        "生产环境必须通过环境变量 JWT_SECRET 注入至少 " + MIN_SECRET_BYTES + " 字节的随机密钥");
            }
            // 本地开发未注入密钥：启动时生成一次性随机密钥，避免源码中存在任何可预测的默认密钥
            byte[] random = new byte[MIN_SECRET_BYTES];
            new SecureRandom().nextBytes(random);
            jwtConfig.setSecret(Base64.getEncoder().encodeToString(random));
            log.warn("未配置 JWT_SECRET，已生成临时随机密钥；重启后已签发的 token 将失效，生产环境必须显式注入 JWT_SECRET");
            return;
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret 长度不足，HS512 至少需要 " + MIN_SECRET_BYTES + " 字节，请检查 jwt.secret 配置");
        }
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        String secret = jwtConfig.getSecret();
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Token（仅包含用户名，兼容旧版本）
     */
    public String generateToken(String username) {
        return generateToken(username, null, null);
    }

    /**
     * 生成Token（包含用户名、角色、部门ID）
     */
    public String generateToken(String username, String role, Long departmentId) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtConfig.getExpiration() * 1000);

            SecretKey key = getSigningKey();
            var builder = Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate);

            if (role != null) {
                builder.claim("role", role);
            }
            if (departmentId != null) {
                builder.claim("departmentId", departmentId);
            }

            return builder.signWith(key, SignatureAlgorithm.HS512).compact();
        } catch (Exception e) {
            // 不向调用方暴露底层异常细节，避免泄露密钥/算法信息
            log.error("生成 JWT Token 失败", e);
            throw new BusinessException("Token 生成失败");
        }
    }

    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null ? claims.get("role", String.class) : null;
        } catch (Exception e) {
            log.debug("从Token获取角色失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取部门ID
     */
    public Long getDepartmentIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) return null;
            Object deptId = claims.get("departmentId");
            if (deptId == null) return null;
            if (deptId instanceof Number) {
                return ((Number) deptId).longValue();
            }
            return Long.parseLong(deptId.toString());
        } catch (Exception e) {
            log.debug("从Token获取部门ID失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取管理员ID（通过username查询）
     * 注意：此方法需要调用方根据username查询admin，这里只返回username
     */
    public String getUsernameForAdminId(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * 解析Token获取所有Claims
     */
    public Claims parseToken(String token) {
        try {
            SecretKey key = getSigningKey();
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("解析Token失败", e);
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public boolean validateToken(String token) {
        // 复用 parseToken（失败时其内部已记录 debug 日志并返回 null）
        return parseToken(token) != null;
    }
}

