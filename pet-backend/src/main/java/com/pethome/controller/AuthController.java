package com.pethome.controller;

import java.util.HashMap;
import java.util.Map;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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

    @PostMapping("/logout")
    @ApiOperation("用户退出登录")
    public Result<String> logout() {
        try {
            // 这里可以添加服务端退出登录的逻辑
            // 比如清除服务端的session、token黑名单等
            // 目前简单返回成功，因为前端主要处理本地数据清除
            
            return Result.success("退出登录成功");
        } catch (Exception e) {
            return Result.error("退出登录失败: " + e.getMessage());
        }
    }
}
