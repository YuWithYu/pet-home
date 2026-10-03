package com.pethome.config;

import com.pethome.util.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置
 *
 * 架构说明：认证与授权统一由 RoleInterceptor（JWT/Redis token）实现，
 * Spring Security 仅负责 CORS 与 401/403 的 JSON 出口，避免两套权限规则不一致。
 * （安全修复：原 WebSecurityConfigurerAdapter 写法已废弃，且旧路径规则
 * /user/setting、/discuss/** 等与实际 /api/** 路由不匹配，属于死配置，一并移除。）
 */
@Configuration
public class SecurityConfig {

    /**
     * CORS 允许的来源白名单（逗号分隔，支持通配符）。
     * 安全修复：原配置 allowedOriginPatterns("*") 对任意来源放行；
     * 现默认仅放行本地开发地址，生产环境必须通过环境变量 APP_CORS_ALLOWED_ORIGINS
     * 注入真实域名列表（如 https://m.example.com,https://admin.example.com）。
     */
    @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 认证授权由 RoleInterceptor 统一处理，Security 层放行所有请求
        http.authorizeRequests()
                .anyRequest().permitAll()
                .and().cors().configurationSource(corsConfigurationSource())
                .and().csrf().disable();

        // 统一 JSON 格式的 401/403 出口（前后端分离架构，不再重定向到 /login 等页面）
        http.exceptionHandling()
                .authenticationEntryPoint(unauthorizedEntryPoint())
                .accessDeniedHandler(forbiddenHandler());

        // 覆盖 Security 默认的 /logout 处理，避免拦截自定义退出逻辑
        http.logout().logoutUrl("/securitylogout");

        return http.build();
    }

    /**
     * 忽略静态资源（与旧 WebSecurity.ignoring 行为一致）
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().antMatchers("/resources/**");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 来源白名单：从配置读取，默认仅本地开发地址
        List<String> origins = Arrays.asList(allowedOrigins.split("\\s*,\\s*"));
        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name(), HttpMethod.PATCH.name(), HttpMethod.HEAD.name()));
        config.setAllowedHeaders(List.of("*"));
        // 未启用 cookie 凭证，跨域不携带认证信息
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException e) ->
                writeJson(response, 401, "你还没有登录或登录已过期");
    }

    private AccessDeniedHandler forbiddenHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException e) ->
                writeJson(response, 403, "你没有访问此功能的权限!");
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws IOException {
        // 安全修复：原 application/plain 不是合法 MIME，统一 application/json
        response.setStatus(code);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(JSONUtil.getJSONString(code, msg));
    }
}
