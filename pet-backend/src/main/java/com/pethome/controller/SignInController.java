package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.User;
import com.pethome.service.SignInService;
import com.pethome.service.UserService;
import com.pethome.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 签到控制器
 */
@RestController
@RequestMapping("/api/signin")
@Tag(name = "签到管理")
public class SignInController {

    private static final Logger log = LoggerFactory.getLogger(SignInController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SignInService signInService;

    @GetMapping("/data")
    @Operation(summary = "获取签到数据")
    public Result<Map<String, Object>> getSignInData(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            
            if (userId == null) {
                // 未登录用户返回默认数据
                Map<String, Object> data = new java.util.HashMap<>();
                data.put("balance", 0);
                data.put("consecutiveDays", 0);
                data.put("canSignIn", false);
                data.put("calendar", new boolean[]{false, false, false, false, false, false, false});
                return Result.success("获取签到数据成功", data);
            }
            
            // 使用真实服务获取签到数据
            Map<String, Object> data = signInService.getSignInData(userId);
            
            return Result.success("获取签到数据成功", data);
        } catch (Exception e) {
            log.error("获取签到数据失败", e);
            return Result.error("获取签到数据失败，请稍后重试");
        }
    }

    @PostMapping("/do")
    @Operation(summary = "执行签到")
    public Result<Map<String, Object>> doSignIn(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            // 使用真实服务执行签到
            Map<String, Object> data = signInService.doSignIn(userId);
            
            return Result.success("签到成功", data);
        } catch (Exception e) {
            log.error("签到失败", e);
            return Result.error("签到失败，请稍后重试");
        }
    }
    
    /**
     * 从token中获取用户ID（与 UserController 一致：先查 Redis 再解析 JWT）
     */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        // 1. 先尝试从 Redis 缓存获取（登录时存的 token -> user）
        try {
            User cachedUser = userService.getUserByToken(token);
            if (cachedUser != null) {
                return cachedUser.getId();
            }
        } catch (Exception e) {
            // 忽略，继续走 JWT 解析
        }
        // 2. 解析 JWT，用 subject 查 username/phone/userId
        try {
            Claims claims = jwtUtil.parseToken(token);
            if (claims != null) {
                String subject = claims.getSubject();
                if (subject != null) {
                    User user = userService.getUserByUsername(subject);
                    if (user == null) {
                        user = userService.getUserByPhone(subject);
                    }
                    if (user == null && subject.matches("\\d+")) {
                        user = userService.getUserById(Long.parseLong(subject));
                    }
                    if (user != null) {
                        return user.getId();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Token解析失败", e);
        }
        return null;
    }
}

