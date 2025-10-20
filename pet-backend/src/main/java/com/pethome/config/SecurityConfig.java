package com.pethome.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));  // 允许所有域名
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .authorizeHttpRequests()
                // 允许所有OPTIONS请求（CORS预检请求）
                .requestMatchers(req -> "OPTIONS".equals(req.getMethod())).permitAll()
                // 公开访问的接口
                .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/users/register")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/users/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/users/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/products/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/categories/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/banners/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/doctors/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/appointment/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/appointments/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/door-cleaning/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/grooming-services/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/grooming-banners/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/service-banners/page/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/orders/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/community/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/product/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/banner/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/medical-services/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/litter-services/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/litter-banners/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/boarding-services/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/adoption-services/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/boarding-banners/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/adoption-banners/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/time-slots/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/bookings/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/consultations/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/adoptions/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/feedback/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/notifications/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/service-config/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/cart/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/address/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/extension/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/vaccine/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/pet/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/pets/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/notification/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/feedback/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/adoption/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/consultation/**")).permitAll()
                // 管理后台接口
                .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/admin/**")).permitAll()
                // 小程序接口
                .requestMatchers(new AntPathRequestMatcher("/tz/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/tz/**")).permitAll()
                // 静态资源
                .requestMatchers(new AntPathRequestMatcher("/upload/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/static/**")).permitAll()
                // Swagger文档
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-resources/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/webjars/**")).permitAll()
                // H2数据库控制台
                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                // 需要认证的请求
                .anyRequest().authenticated();

        return http.build();
    }
}
