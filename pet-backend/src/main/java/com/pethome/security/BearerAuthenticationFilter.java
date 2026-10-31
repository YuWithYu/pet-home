package com.pethome.security;

import com.pethome.entity.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * Bearer Token 认证过滤器
 *
 * 职责：把已经存在的 Bearer Token 解析结果写入 Spring Security 上下文，
 * 让 Security 层的 authorizeRequests 规则（如 /api/admin/** 需要 ROLE_ADMIN）真正生效。
 *
 * 边界：
 * 1. 解析失败不阻断请求（不写 401），认证/授权的最终判定仍由 RoleInterceptor 与
 *    SecurityConfig 的 401/403 JSON 出口处理，避免两套响应口径不一致；
 * 2. 不复制 RoleInterceptor 的细粒度路径白名单，只区分 ADMIN / STAFF / STORE_ADMIN / USER；
 * 3. 管理员优先于普通用户判定，与 RoleInterceptor 的解析顺序保持一致的结论。
 */
public class BearerAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BearerAuthenticationFilter.class);

    private final AdminContext adminContext;
    private final UserTokenResolver userTokenResolver;

    public BearerAuthenticationFilter(AdminContext adminContext, UserTokenResolver userTokenResolver) {
        this.adminContext = adminContext;
        this.userTokenResolver = userTokenResolver;
    }

    /** 与 RoleInterceptor 保持一致：super_admin 归一为 admin，空角色按 staff 处理 */
    private String normalizeAdminRole(String role) {
        if (role == null) {
            return "staff";
        }
        String r = role.toLowerCase().trim();
        return "super_admin".equals(r) ? "admin" : r;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 仅在 SecurityConfig 中存在非 permitAll 规则的路径上解析 token，
        // 避免给所有带 Bearer 的业务请求额外增加一次数据库查询（RoleInterceptor 已会查询）。
        // 注意：若后续为其它路径增加 hasRole 规则，需同步放宽这里的判断。
        if (request.getRequestURI().startsWith("/api/admin/")) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    Admin admin = adminContext.getCurrentAdmin(authHeader);
                    if (admin != null) {
                        String role = normalizeAdminRole(admin.getRole());
                        setAuthentication(admin.getUsername(), "ROLE_" + role.toUpperCase());
                    } else {
                        Long userId = userTokenResolver.getUserIdFromBearer(authHeader);
                        if (userId != null) {
                            setAuthentication(String.valueOf(userId), "ROLE_USER");
                        }
                    }
                } catch (Exception e) {
                    // 交给后续链路判定：此处静默失败即可
                    log.debug("Bearer 认证解析失败: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String principal, String authority) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, Collections.singletonList(new SimpleGrantedAuthority(authority))));
    }
}
