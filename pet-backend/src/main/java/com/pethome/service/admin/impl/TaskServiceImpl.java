package com.pethome.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.TaskRecord;
import com.pethome.mapper.TaskRecordMapper;
import com.pethome.service.admin.TaskService;
import com.pethome.service.admin.support.TaskProgressCalculator;
import com.pethome.service.admin.support.TaskRewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务服务实现类
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskRecordMapper, TaskRecord> implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Autowired
    private TaskProgressCalculator taskProgressCalculator;
    
    @Autowired
    private TaskRewardService taskRewardService;
    
    // 任务配置
    private static final Map<String, Map<String, Object>> TASK_CONFIG = new HashMap<>();
    static {
        // 点赞内容任务
        Map<String, Object> likeContent = new HashMap<>();
        likeContent.put("id", "like_content");
        likeContent.put("name", "点赞内容");
        likeContent.put("type", "interaction");
        likeContent.put("target", 2);
        likeContent.put("reward", 20);
        TASK_CONFIG.put("like_content", likeContent);

        // 点赞评论任务
        Map<String, Object> likeComment = new HashMap<>();
        likeComment.put("id", "like_comment");
        likeComment.put("name", "点赞评论");
        likeComment.put("type", "interaction");
        likeComment.put("target", 4);
        likeComment.put("reward", 10);
        TASK_CONFIG.put("like_comment", likeComment);

        // 发布内容任务
        Map<String, Object> postContent = new HashMap<>();
        postContent.put("id", "post_content");
        postContent.put("name", "发布内容");
        postContent.put("type", "publish");
        postContent.put("target", 1);
        postContent.put("reward", 60);
        TASK_CONFIG.put("post_content", postContent);

        // 评论内容任务
        Map<String, Object> commentContent = new HashMap<>();
        commentContent.put("id", "comment_content");
        commentContent.put("name", "评论内容");
        commentContent.put("type", "interaction");
        commentContent.put("target", 2);
        commentContent.put("reward", 50);
        TASK_CONFIG.put("comment_content", commentContent);

        // 关注用户任务
        Map<String, Object> followUser = new HashMap<>();
        followUser.put("id", "follow_user");
        followUser.put("name", "关注用户");
        followUser.put("type", "social");
        followUser.put("target", 2);
        followUser.put("reward", 30);
        TASK_CONFIG.put("follow_user", followUser);

        // 收藏内容任务
        Map<String, Object> collectContent = new HashMap<>();
        collectContent.put("id", "collect_content");
        collectContent.put("name", "收藏内容");
        collectContent.put("type", "interaction");
        collectContent.put("target", 2);
        collectContent.put("reward", 10);
        TASK_CONFIG.put("collect_content", collectContent);
    }
    
    @Override
    public Map<String, Object> getTaskProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tasks = new ArrayList<>();

        LocalDate today = LocalDate.now();

        // 获取所有任务配置
        for (Map.Entry<String, Map<String, Object>> entry : TASK_CONFIG.entrySet()) {
            String taskId = entry.getKey();
            Map<String, Object> config = entry.getValue();
            
            // 查询今日任务记录
            QueryWrapper<TaskRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("task_type", taskId);
            wrapper.eq("task_date", today);
            TaskRecord record = this.getOne(wrapper);
            
            Map<String, Object> task = new HashMap<>();
            task.put("id", taskId);
            task.put("key", taskId);
            task.put("name", config.get("name"));
            task.put("type", config.get("type"));
            task.put("target", config.get("target"));
            task.put("reward", config.get("reward"));
            
            if (record != null) {
                int storedProgress = record.getProgress() != null ? record.getProgress() : 0;
                int displayProgress = storedProgress;
                boolean completed = Boolean.TRUE.equals(record.getCompleted());
                boolean claimed = Boolean.TRUE.equals(record.getClaimed());
                // 发布内容：未领取前按库中今日发帖数同步任务行，解决先发待审 progress=0、后台通过后仍显示 0/1
                if ("post_content".equals(taskId) && !claimed) {
                    int dbProgress = taskProgressCalculator.queryTodayPostContentProgress(userId);
                    int targetVal = (Integer) config.get("target");
                    displayProgress = dbProgress;
                    completed = dbProgress >= targetVal;
                    boolean prevDone = Boolean.TRUE.equals(record.getCompleted());
                    if (dbProgress != storedProgress || prevDone != completed) {
                        record.setProgress(dbProgress);
                        record.setCompleted(completed);
                        record.setUpdateTime(LocalDateTime.now());
                        this.updateById(record);
                    }
                }
                task.put("progress", displayProgress);
                task.put("current", displayProgress);
                task.put("completed", completed);
                task.put("canClaim", completed && !claimed);
            } else {
                // 根据任务类型初始化进度（从实际数据查询）
                int progress = taskProgressCalculator.calculateDisplayProgress(userId, taskId, today);

                task.put("progress", progress);
                task.put("current", progress);
                task.put("completed", progress >= (Integer) config.get("target"));
                task.put("canClaim", progress >= (Integer) config.get("target"));
            }

            tasks.add(task);
        }

        // 计算总进度和总奖励
        int totalProgress = 0;
        int totalReward = 0;
        for (Map<String, Object> task : tasks) {
            if ((Boolean) task.get("completed")) {
                totalProgress++;
                totalReward += (Integer) task.get("reward");
            }
        }

        result.put("tasks", tasks);
        result.put("totalProgress", totalProgress);
        result.put("totalReward", totalReward);

        return result;
    }
    
    @Override
    public List<Map<String, Object>> getDailyTasks(Long userId) {
        List<Map<String, Object>> tasks = new ArrayList<>();

        // 性能修复：一次性查出该用户今日全部任务记录（替代循环内逐条 getOne 的 N+1 查询）
        LocalDate today = LocalDate.now();
        QueryWrapper<TaskRecord> todayWrapper = new QueryWrapper<>();
        todayWrapper.eq("user_id", userId)
                .eq("task_date", today)
                .in("task_type", TASK_CONFIG.keySet());
        Map<String, TaskRecord> todayRecords = new HashMap<>();
        for (TaskRecord r : this.list(todayWrapper)) {
            if (r.getTaskType() != null) {
                todayRecords.put(r.getTaskType(), r);
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : TASK_CONFIG.entrySet()) {
            String taskId = entry.getKey();
            Map<String, Object> config = entry.getValue();

            Map<String, Object> task = new HashMap<>();
            task.put("id", taskId);
            task.put("name", config.get("name"));
            task.put("description", getTaskDescription(taskId));
            task.put("reward", config.get("reward"));

            // 检查是否已完成（从预查的 Map 中取，无额外查询）
            TaskRecord record = todayRecords.get(taskId);
            task.put("completed", record != null && record.getCompleted() != null && record.getCompleted());

            tasks.add(task);
        }

        return tasks;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> claimTaskReward(Long userId, String taskId) {
        if (!TASK_CONFIG.containsKey(taskId)) {
            throw new RuntimeException("任务不存在");
        }
        
        Map<String, Object> config = TASK_CONFIG.get(taskId);
        LocalDate today = LocalDate.now();
        
        // 查询任务记录
        QueryWrapper<TaskRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("task_type", taskId);
        wrapper.eq("task_date", today);
        TaskRecord record = this.getOne(wrapper);
        
        // 检查任务是否完成
        boolean completed = false;
        int actualProgress = 0;
        
        if (record != null) {
            completed = record.getCompleted() != null && record.getCompleted();
            actualProgress = record.getProgress() != null ? record.getProgress() : 0;
            if (record.getClaimed() != null && record.getClaimed()) {
                throw new RuntimeException("奖励已领取");
            }
        } else {
            // 如果没有记录，从实际数据查询任务完成状态
            int target = (Integer) config.get("target");
            actualProgress = taskProgressCalculator.calculateClaimProgress(userId, taskId, today);
            completed = actualProgress >= target;
            
            log.info("领取奖励 - 任务ID: {}, 用户ID: {}, 实际进度: {}, 目标: {}, 是否完成: {}",
                taskId, userId, actualProgress, target, completed);
        }
        
        if (!completed) {
            throw new RuntimeException("任务未完成，无法领取奖励（当前进度：" + actualProgress + "/" + config.get("target") + "）");
        }
        
        // 创建或更新任务记录
        if (record == null) {
            record = new TaskRecord();
            record.setUserId(userId);
            record.setTaskType(taskId);
            record.setTaskDate(today);
            record.setProgress(actualProgress);
            record.setTarget((Integer) config.get("target"));
            record.setCompleted(true);
            record.setClaimed(true);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            this.save(record);
        } else {
            // 安全修复：领取标记用条件更新（claimed=0 才置 1），防止并发双请求同时通过
            // "已领取"校验后重复发放积分（read-check-write 竞态）
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<TaskRecord> claimWrapper =
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
            claimWrapper.eq("id", record.getId());
            claimWrapper.eq("claimed", 0);
            claimWrapper.set("progress", actualProgress);
            claimWrapper.set("completed", true);
            claimWrapper.set("claimed", true);
            claimWrapper.set("update_time", LocalDateTime.now());
            boolean claimedNow = this.update(claimWrapper);
            if (!claimedNow) {
                throw new RuntimeException("奖励已领取");
            }
        }
        
        // 发放奖励
        int rewardPoints = (Integer) config.get("reward");
        String taskName = (String) config.get("name");
        int balance = taskRewardService.grantTaskReward(userId, rewardPoints, taskName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("points", rewardPoints);
        result.put("balance", balance);
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTaskProgress(Long userId, String taskType, int progress) {
        LocalDate today = LocalDate.now();
        
        QueryWrapper<TaskRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("task_type", taskType);
        wrapper.eq("task_date", today);
        TaskRecord record = this.getOne(wrapper);
        
        Map<String, Object> config = TASK_CONFIG.get(taskType);
        if (config == null) {
            return;
        }
        
        int target = (Integer) config.get("target");
        boolean completed = progress >= target;
        
        if (record == null) {
            record = new TaskRecord();
            record.setUserId(userId);
            record.setTaskType(taskType);
            record.setTaskDate(today);
            record.setProgress(progress);
            record.setTarget(target);
            record.setCompleted(completed);
            record.setClaimed(false);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            this.save(record);
        } else {
            record.setProgress(progress);
            record.setCompleted(completed);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
        }
    }
    
    @Override
    public void syncPostContentProgress(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            updateTaskProgress(userId, "post_content",
                taskProgressCalculator.queryTodayPostContentProgress(userId));
        } catch (Exception e) {
            // 任务进度同步失败不影响管理员审核结果
            log.warn("同步发布内容任务进度失败，用户ID: {}", userId, e);
        }
    }

    private String getTaskDescription(String taskId) {
        switch (taskId) {
            case "daily_signin":
                return "每天签到可获得积分奖励";
            case "share_app":
                return "分享小程序给好友可获得积分奖励";
            default:
                return "完成任务可获得积分奖励";
        }
    }
}

