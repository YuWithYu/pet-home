package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.User;
import com.pethome.service.TaskService;
import com.pethome.service.UserService;
import com.pethome.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/tasks")
@Api(tags = "任务管理")
public class TaskController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TaskService taskService;

    @GetMapping("/progress")
    @ApiOperation("获取任务进度")
    public Result<Map<String, Object>> getTaskProgress(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("========== TaskController.getTaskProgress ==========");
            System.out.println("Authorization Header: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null"));
            
            Long userId = getUserIdFromToken(authHeader);
            System.out.println("从Token解析出的用户ID: " + userId);
            
            if (userId == null) {
                System.out.println("用户未登录，返回空数据");
                // 未登录用户返回空数据
                Map<String, Object> data = new java.util.HashMap<>();
                data.put("tasks", new java.util.ArrayList<>());
                data.put("totalProgress", 0);
                data.put("totalReward", 0);
                return Result.success("获取任务进度成功", data);
            }
            
            // 使用真实服务获取任务进度
            System.out.println("调用TaskService.getTaskProgress，用户ID: " + userId);
            Map<String, Object> data = taskService.getTaskProgress(userId);
            System.out.println("任务进度数据: " + data);
            System.out.println("任务列表大小: " + (data.get("tasks") != null ? ((List<?>) data.get("tasks")).size() : 0));
            System.out.println("====================================");
            
            return Result.success("获取任务进度成功", data);
        } catch (Exception e) {
            System.err.println("========== 获取任务进度异常 ==========");
            System.err.println("异常类型: " + e.getClass().getName());
            System.err.println("异常消息: " + e.getMessage());
            e.printStackTrace();
            System.err.println("====================================");
            return Result.error("获取任务进度失败：" + e.getMessage());
        }
    }

    @GetMapping("/daily")
    @ApiOperation("获取每日任务")
    public Result<List<Map<String, Object>>> getDailyTasks(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            
            if (userId == null) {
                return Result.success("获取每日任务成功", new java.util.ArrayList<>());
            }
            
            // 使用真实服务获取每日任务
            List<Map<String, Object>> tasks = taskService.getDailyTasks(userId);
            
            return Result.success("获取每日任务成功", tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取每日任务失败：" + e.getMessage());
        }
    }

    @PostMapping("/claim")
    @ApiOperation("领取任务奖励")
    public Result<Map<String, Object>> claimTaskReward(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String taskId = request.get("taskId");
            if (taskId == null || taskId.isEmpty()) {
                return Result.error("任务ID不能为空");
            }
            
            Long userId = getUserIdFromToken(authHeader);
            
            if (userId == null) {
                return Result.error("未登录或token无效");
            }
            
            // 使用真实服务领取任务奖励
            Map<String, Object> data = taskService.claimTaskReward(userId, taskId);
            
            return Result.success("领取奖励成功", data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("领取奖励失败：" + e.getMessage());
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

