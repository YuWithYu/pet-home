package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.User;
import com.pethome.service.TaskService;
import com.pethome.service.UserService;
import com.pethome.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务控制器
 */
@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务管理")
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TaskService taskService;

    @GetMapping("/progress")
    @Operation(summary = "获取任务进度")
    public Result<Map<String, Object>> getTaskProgress(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromToken(authHeader);
            log.debug("从Token解析出的用户ID: {}", userId);
            
            if (userId == null) {
                log.debug("用户未登录，返回空数据");
                // 未登录用户返回空数据
                Map<String, Object> data = new java.util.HashMap<>();
                data.put("tasks", new java.util.ArrayList<>());
                data.put("totalProgress", 0);
                data.put("totalReward", 0);
                return Result.success("获取任务进度成功", data);
            }
            
            // 使用真实服务获取任务进度
            log.debug("调用TaskService.getTaskProgress，用户ID: {}", userId);
            Map<String, Object> data = taskService.getTaskProgress(userId);
            log.debug("任务进度数据: {}", data);
            log.debug("任务列表大小: {}", data.get("tasks") != null ? ((List<?>) data.get("tasks")).size() : 0);
            
            return Result.success("获取任务进度成功", data);
        } catch (Exception e) {
            log.error("获取任务进度失败", e);
            return Result.error("获取任务进度失败：" + e.getMessage());
        }
    }

    @GetMapping("/daily")
    @Operation(summary = "获取每日任务")
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
            log.error("获取每日任务失败", e);
            return Result.error("获取每日任务失败：" + e.getMessage());
        }
    }

    @PostMapping("/claim")
    @Operation(summary = "领取任务奖励")
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
            log.error("领取奖励失败", e);
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
            log.debug("Token解析失败", e);
        }
        return null;
    }
}
