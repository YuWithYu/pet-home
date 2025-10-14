package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.User;
import com.pethome.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证管理")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String phone = loginData.get("phone");
        String password = loginData.get("password");

        Map<String, Object> result = new HashMap<>();
        if (username != null) {
            String token = userService.login(username, password);
            result.put("token", token);
            result.put("userId", 1); // 这里应该从数据库获取真实的userId
            return Result.success(result);
        } else if (phone != null) {
            String token = userService.loginByPhone(phone, password);
            result.put("token", token);
            result.put("userId", 1); // 这里应该从数据库获取真实的userId
            return Result.success(result);
        } else {
            return Result.error("用户名或手机号不能为空");
        }
    }

    @GetMapping("/current")
    @ApiOperation("获取当前用户信息")
    public Result<User> getCurrentUser() {
        try {
            User user = new User();
            user.setId(1L);
            user.setUsername("Yuu");
            user.setNickname("宠物达人");
            user.setPoints(100);
            user.setMemberLevel(1);
            user.setEmail("user@example.com");
            user.setPhone("13800138001");
            user.setAvatar("/upload/default-avatar.png");
            user.setRole("user");
            user.setStatus(1);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
