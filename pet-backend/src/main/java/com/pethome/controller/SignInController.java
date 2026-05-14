package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.User;
import com.pethome.service.SignInService;
import com.pethome.service.UserService;
import com.pethome.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 签到控制器
 */
@RestController
@RequestMapping("/api/signin")
@Api(tags = "签到管理")
public class SignInController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SignInService signInService;

    @GetMapping("/data")
    @ApiOperation("获取签到数据")
    public Result<Map<String, Object>> getSignInData(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("========== 获取签到数据 ==========");
            System.out.println("Authorization Header: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null"));
            
            Long userId = getUserIdFromToken(authHeader);
            System.out.println("从Token解析出的用户ID: " + userId);
            
            if (userId == null) {
                System.out.println("用户未登录，返回默认数据");
                // 未登录用户返回默认数据
                Map<String, Object> data = new java.util.HashMap<>();
                data.put("balance", 0);
                data.put("consecutiveDays", 0);
                data.put("canSignIn", false);
                data.put("calendar", new boolean[]{false, false, false, false, false, false, false});
                return Result.success("获取签到数据成功", data);
            }
            
            // 使用真实服务获取签到数据
            System.out.println("调用SignInService获取签到数据，用户ID: " + userId);
            Map<String, Object> data = signInService.getSignInData(userId);
            System.out.println("签到数据: " + data);
            System.out.println("====================================");
            
            return Result.success("获取签到数据成功", data);
        } catch (Exception e) {
            System.err.println("========== 获取签到数据异常 ==========");
            e.printStackTrace();
            System.err.println("====================================");
            return Result.error("获取签到数据失败：" + e.getMessage());
        }
    }

    @PostMapping("/do")
    @ApiOperation("执行签到")
    public Result<Map<String, Object>> doSignIn(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("========== 执行签到 ==========");
            System.out.println("Authorization Header: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null"));
            
            Long userId = getUserIdFromToken(authHeader);
            System.out.println("从Token解析出的用户ID: " + userId);
            
            if (userId == null) {
                System.err.println("用户未登录或token无效");
                return Result.error("未登录或token无效");
            }
            
            // 使用真实服务执行签到
            System.out.println("调用SignInService执行签到，用户ID: " + userId);
            Map<String, Object> data = signInService.doSignIn(userId);
            System.out.println("签到结果: " + data);
            System.out.println("====================================");
            
            return Result.success("签到成功", data);
        } catch (Exception e) {
            System.err.println("========== 执行签到异常 ==========");
            e.printStackTrace();
            System.err.println("====================================");
            return Result.error("签到失败：" + e.getMessage());
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
            System.err.println("Token解析失败: " + e.getMessage());
        }
        return null;
    }
}

