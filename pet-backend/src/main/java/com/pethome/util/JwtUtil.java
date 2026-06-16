package com.pethome.util;

import com.pethome.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        String secret = jwtConfig.getSecret();
        // 确保 secret 足够长（至少 256 位用于 HS512）
        if (secret.length() < 64) {
            // 如果 secret 太短，进行填充
            StringBuilder sb = new StringBuilder(secret);
            while (sb.length() < 64) {
                sb.append(secret);
            }
            secret = sb.substring(0, 64);
        }
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
            
            // 添加角色和部门ID到claims
            if (role != null) {
                builder.claim("role", role);
            }
            if (departmentId != null) {
                builder.claim("departmentId", departmentId);
            }
            
            String token = builder.signWith(key, SignatureAlgorithm.HS512)
                    .compact();
            
            System.out.println("JWT Token 生成成功，长度: " + token.length() + ", role: " + role + ", departmentId: " + departmentId);
            return token;
        } catch (Exception e) {
            System.err.println("生成 JWT Token 时发生异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("生成 Token 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从Token中获取角色
     */
    public String getRoleFromToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        } catch (Exception e) {
            System.err.println("从Token获取角色失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 从Token中获取部门ID
     */
    public Long getDepartmentIdFromToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object deptId = claims.get("departmentId");
            if (deptId == null) return null;
            if (deptId instanceof Number) {
                return ((Number) deptId).longValue();
            }
            return Long.parseLong(deptId.toString());
        } catch (Exception e) {
            System.err.println("从Token获取部门ID失败: " + e.getMessage());
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
            System.err.println("解析Token失败: " + e.getMessage());
            return null;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            System.err.println("解析 JWT Token 时发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println("验证 JWT Token 时发生异常: " + e.getMessage());
            return false;
        }
    }
}
