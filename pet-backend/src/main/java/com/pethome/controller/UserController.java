package com.pethome.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pethome.common.Result;
import com.pethome.entity.User;
import com.pethome.service.UserService;

import java.util.Map;
import java.util.HashMap;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/users")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<User> register(@RequestBody User user) {
        return Result.success(userService.register(user));
    }

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
            result.put("userId", 1);
            return Result.success(result);
        } else if (phone != null) {
            String token = userService.loginByPhone(phone, password);
            result.put("token", token);
            result.put("userId", 1);
            return Result.success(result);
        } else {
            return Result.error("用户名或手机号不能为空");
        }
    }

    @GetMapping("/current")
    @ApiOperation("获取当前用户信息")
    public Result<User> getCurrentUser(@RequestParam(required = false) Long userId,
                                       @RequestParam(required = false) String username) {
        try {
            User user = null;
            
            // 优先使用 userId 查询
            if (userId != null) {
                user = userService.getUserById(userId);
            } 
            // 其次使用 username 查询
            else if (username != null && !username.isEmpty()) {
                user = userService.getUserByUsername(username);
            }
            // 默认返回ID为1的用户（用于测试）
            else {
                user = userService.getUserById(1L);
            }
            
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 隐藏密码
            user.setPassword(null);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
