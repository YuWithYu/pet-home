package com.pethome.controller.admin;


import com.pethome.common.BusinessException;
import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.service.admin.TaskService;
import com.pethome.service.user.UserTokenService;
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
    private UserTokenService userTokenService;

    @Autowired
    private TaskService taskService;

    @GetMapping("/progress")
    @Operation(summary = "获取任务进度")
    public Result<Map<String, Object>> getTaskProgress(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = userTokenService.resolveUserId(authHeader);
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
        }, "获取任务进度失败，请稍后重试", log);
    }

    @GetMapping("/daily")
    @Operation(summary = "获取每日任务")
    public Result<List<Map<String, Object>>> getDailyTasks(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return com.pethome.common.SafeCall.of(() -> {
            Long userId = userTokenService.resolveUserId(authHeader);
            
            if (userId == null) {
                return Result.success("获取每日任务成功", new java.util.ArrayList<>());
            }
            
            // 使用真实服务获取每日任务
            List<Map<String, Object>> tasks = taskService.getDailyTasks(userId);
            
            return Result.success("获取每日任务成功", tasks);
        }, "获取每日任务失败，请稍后重试", log);
    }

    @PostMapping("/claim")
    @Operation(summary = "领取任务奖励")
    public Result<Map<String, Object>> claimTaskReward(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        return com.pethome.common.SafeCall.of(() -> {
            String taskId = request.get("taskId");
            if (taskId == null || taskId.isEmpty()) {
                throw new BusinessException("任务ID不能为空");
            }
            
            Long userId = userTokenService.resolveUserId(authHeader);
            
            if (userId == null) {
                throw new BusinessException("未登录或token无效");
            }
            
            // 使用真实服务领取任务奖励
            Map<String, Object> data = taskService.claimTaskReward(userId, taskId);
            
            return Result.success("领取奖励成功", data);
        }, "领取奖励失败，请稍后重试", log);
    }
}
