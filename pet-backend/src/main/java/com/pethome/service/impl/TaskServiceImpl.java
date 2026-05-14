package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.TaskRecord;
import com.pethome.entity.User;
import com.pethome.entity.PointsRecord;
import com.pethome.mapper.TaskRecordMapper;
import com.pethome.service.TaskService;
import com.pethome.service.SignInService;
import com.pethome.service.UserService;
import com.pethome.service.PointsRecordService;
import com.pethome.mapper.UserFollowMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.mapper.CommentMapper;
import com.pethome.mapper.PostCollectMapper;
import com.pethome.mapper.PostLikeMapper;
import com.pethome.mapper.CommentLikeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    
    @Autowired
    private UserService userService;
    
    @Autowired
    @Lazy
    private SignInService signInService;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private PostCollectMapper postCollectMapper;
    
    @Autowired
    private PostLikeMapper postLikeMapper;
    
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    
    @Autowired
    private PointsRecordService pointsRecordService;
    
    // 任务配置
    private static final Map<String, Map<String, Object>> TASK_CONFIG = new HashMap<>();
    static {
        System.out.println("========== TaskService 静态初始化 ==========");
        
        // 点赞内容任务
        Map<String, Object> likeContent = new HashMap<>();
        likeContent.put("id", "like_content");
        likeContent.put("name", "点赞内容");
        likeContent.put("type", "interaction");
        likeContent.put("target", 2);
        likeContent.put("reward", 20);
        TASK_CONFIG.put("like_content", likeContent);
        System.out.println("已添加任务: like_content");
        
        // 点赞评论任务
        Map<String, Object> likeComment = new HashMap<>();
        likeComment.put("id", "like_comment");
        likeComment.put("name", "点赞评论");
        likeComment.put("type", "interaction");
        likeComment.put("target", 4);
        likeComment.put("reward", 10);
        TASK_CONFIG.put("like_comment", likeComment);
        System.out.println("已添加任务: like_comment");
        
        // 发布内容任务
        Map<String, Object> postContent = new HashMap<>();
        postContent.put("id", "post_content");
        postContent.put("name", "发布内容");
        postContent.put("type", "publish");
        postContent.put("target", 1);
        postContent.put("reward", 60);
        TASK_CONFIG.put("post_content", postContent);
        System.out.println("已添加任务: post_content");
        
        // 评论内容任务
        Map<String, Object> commentContent = new HashMap<>();
        commentContent.put("id", "comment_content");
        commentContent.put("name", "评论内容");
        commentContent.put("type", "interaction");
        commentContent.put("target", 2);
        commentContent.put("reward", 50);
        TASK_CONFIG.put("comment_content", commentContent);
        System.out.println("已添加任务: comment_content");
        
        // 关注用户任务
        Map<String, Object> followUser = new HashMap<>();
        followUser.put("id", "follow_user");
        followUser.put("name", "关注用户");
        followUser.put("type", "social");
        followUser.put("target", 2);
        followUser.put("reward", 30);
        TASK_CONFIG.put("follow_user", followUser);
        System.out.println("已添加任务: follow_user");
        
        // 收藏内容任务
        Map<String, Object> collectContent = new HashMap<>();
        collectContent.put("id", "collect_content");
        collectContent.put("name", "收藏内容");
        collectContent.put("type", "interaction");
        collectContent.put("target", 2);
        collectContent.put("reward", 10);
        TASK_CONFIG.put("collect_content", collectContent);
        System.out.println("已添加任务: collect_content");
        
        System.out.println("TASK_CONFIG初始化完成，任务数量: " + TASK_CONFIG.size());
        System.out.println("====================================");
    }
    
    @Override
    public Map<String, Object> getTaskProgress(Long userId) {
        System.out.println("========== TaskService.getTaskProgress ==========");
        System.out.println("用户ID: " + userId);
        System.out.println("TASK_CONFIG大小: " + TASK_CONFIG.size());
        System.out.println("TASK_CONFIG键: " + TASK_CONFIG.keySet());
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        System.out.println("今日日期: " + today);
        
        // 获取所有任务配置
        for (Map.Entry<String, Map<String, Object>> entry : TASK_CONFIG.entrySet()) {
            System.out.println("处理任务: " + entry.getKey());
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
                    int dbProgress = queryTodayPostContentProgress(userId);
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
                int progress = 0;
                if ("daily_signin".equals(taskId)) {
                    // 检查今日是否已签到
                    progress = signInService.hasSignedIn(userId, today) ? 1 : 0;
                } else if ("follow_user".equals(taskId)) {
                    // 查询用户今日已关注的用户数量
                    try {
                        QueryWrapper<com.pethome.entity.UserFollow> followWrapper = 
                            new QueryWrapper<>();
                        followWrapper.eq("follower_id", userId);
                        followWrapper.apply("DATE(create_time) = CURDATE()");
                        progress = Math.toIntExact(userFollowMapper.selectCount(followWrapper));
                        System.out.println("查询关注用户任务进度，用户ID: " + userId + ", 今日关注数: " + progress);
                    } catch (Exception e) {
                        System.err.println("查询关注用户任务进度失败: " + e.getMessage());
                    }
                } else if ("post_content".equals(taskId)) {
                    progress = queryTodayPostContentProgress(userId);
                } else if ("comment_content".equals(taskId)) {
                    // 查询用户今日已发布的顶级评论数量
                    try {
                        QueryWrapper<com.pethome.entity.Comment> commentWrapper = 
                            new QueryWrapper<>();
                        commentWrapper.eq("user_id", userId);
                        commentWrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", 0));
                        commentWrapper.apply("DATE(create_time) = CURDATE()");
                        progress = Math.toIntExact(commentMapper.selectCount(commentWrapper));
                        System.out.println("查询评论内容任务进度，用户ID: " + userId + ", 今日评论数: " + progress);
                    } catch (Exception e) {
                        System.err.println("查询评论内容任务进度失败: " + e.getMessage());
                    }
                } else if ("collect_content".equals(taskId)) {
                    // 查询用户今日已收藏的帖子数量
                    try {
                        QueryWrapper<com.pethome.entity.PostCollect> collectWrapper = 
                            new QueryWrapper<>();
                        collectWrapper.eq("user_id", userId);
                        collectWrapper.apply("DATE(create_time) = CURDATE()");
                        progress = Math.toIntExact(postCollectMapper.selectCount(collectWrapper));
                        System.out.println("查询收藏内容任务进度，用户ID: " + userId + ", 今日收藏数: " + progress);
                    } catch (Exception e) {
                        System.err.println("查询收藏内容任务进度失败: " + e.getMessage());
                    }
                } else if ("like_content".equals(taskId)) {
                    // 查询用户今日已点赞的帖子数量
                    try {
                        progress = postLikeMapper.getTodayLikeCount(userId);
                        System.out.println("查询点赞内容任务进度，用户ID: " + userId + ", 今日点赞数: " + progress);
                    } catch (Exception e) {
                        System.err.println("查询点赞内容任务进度失败: " + e.getMessage());
                    }
                } else if ("like_comment".equals(taskId)) {
                    // 查询用户今日已点赞的评论数量
                    try {
                        QueryWrapper<com.pethome.entity.CommentLike> likeCommentWrapper = 
                            new QueryWrapper<>();
                        likeCommentWrapper.eq("user_id", userId);
                        likeCommentWrapper.apply("DATE(create_time) = CURDATE()");
                        progress = Math.toIntExact(commentLikeMapper.selectCount(likeCommentWrapper));
                        System.out.println("查询点赞评论任务进度，用户ID: " + userId + ", 今日点赞评论数: " + progress);
                    } catch (Exception e) {
                        System.err.println("查询点赞评论任务进度失败: " + e.getMessage());
                    }
                }
                
                task.put("progress", progress);
                task.put("current", progress);
                task.put("completed", progress >= (Integer) config.get("target"));
                task.put("canClaim", progress >= (Integer) config.get("target"));
            }
            
            tasks.add(task);
            System.out.println("任务已添加: " + task.get("name") + ", 进度: " + task.get("current") + "/" + task.get("target"));
        }
        
        System.out.println("任务列表总数: " + tasks.size());
        
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
        
        System.out.println("返回结果 - 任务数: " + tasks.size() + ", 总进度: " + totalProgress + ", 总奖励: " + totalReward);
        System.out.println("====================================");
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getDailyTasks(Long userId) {
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, Object>> entry : TASK_CONFIG.entrySet()) {
            String taskId = entry.getKey();
            Map<String, Object> config = entry.getValue();
            
            Map<String, Object> task = new HashMap<>();
            task.put("id", taskId);
            task.put("name", config.get("name"));
            task.put("description", getTaskDescription(taskId));
            task.put("reward", config.get("reward"));
            
            // 检查是否已完成
            LocalDate today = LocalDate.now();
            QueryWrapper<TaskRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId);
            wrapper.eq("task_type", taskId);
            wrapper.eq("task_date", today);
            TaskRecord record = this.getOne(wrapper);
            task.put("completed", record != null && record.getCompleted() != null && record.getCompleted());
            
            tasks.add(task);
        }
        
        return tasks;
    }
    
    @Override
    @Transactional
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
            
            if ("daily_signin".equals(taskId)) {
                completed = signInService.hasSignedIn(userId, today);
                actualProgress = completed ? 1 : 0;
            } else if ("follow_user".equals(taskId)) {
                // 查询用户今日已关注的用户数量
                QueryWrapper<com.pethome.entity.UserFollow> followWrapper = new QueryWrapper<>();
                followWrapper.eq("follower_id", userId);
                followWrapper.apply("DATE(create_time) = CURDATE()");
                actualProgress = Math.toIntExact(userFollowMapper.selectCount(followWrapper));
                completed = actualProgress >= target;
            } else if ("post_content".equals(taskId)) {
                // 查询用户今日已发布的帖子数量
                QueryWrapper<com.pethome.entity.Post> postWrapper = new QueryWrapper<>();
                postWrapper.eq("user_id", userId);
                postWrapper.in("status", java.util.Arrays.asList(1, 2));
                postWrapper.apply("DATE(create_time) = CURDATE()");
                actualProgress = Math.toIntExact(postMapper.selectCount(postWrapper));
                completed = actualProgress >= target;
            } else if ("comment_content".equals(taskId)) {
                // 查询用户今日已发布的顶级评论数量
                QueryWrapper<com.pethome.entity.Comment> commentWrapper = new QueryWrapper<>();
                commentWrapper.eq("user_id", userId);
                commentWrapper.and(w -> w.isNull("parent_id").or().eq("parent_id", 0));
                commentWrapper.apply("DATE(create_time) = CURDATE()");
                actualProgress = Math.toIntExact(commentMapper.selectCount(commentWrapper));
                completed = actualProgress >= target;
            } else if ("collect_content".equals(taskId)) {
                // 查询用户今日已收藏的帖子数量
                QueryWrapper<com.pethome.entity.PostCollect> collectWrapper = new QueryWrapper<>();
                collectWrapper.eq("user_id", userId);
                collectWrapper.apply("DATE(create_time) = CURDATE()");
                actualProgress = Math.toIntExact(postCollectMapper.selectCount(collectWrapper));
                completed = actualProgress >= target;
            } else if ("like_content".equals(taskId)) {
                // 查询用户今日已点赞的帖子数量
                actualProgress = postLikeMapper.getTodayLikeCount(userId);
                completed = actualProgress >= target;
            } else if ("like_comment".equals(taskId)) {
                // 查询用户今日已点赞的评论数量
                QueryWrapper<com.pethome.entity.CommentLike> likeCommentWrapper = new QueryWrapper<>();
                likeCommentWrapper.eq("user_id", userId);
                likeCommentWrapper.apply("DATE(create_time) = CURDATE()");
                actualProgress = Math.toIntExact(commentLikeMapper.selectCount(likeCommentWrapper));
                completed = actualProgress >= target;
            }
            
            System.out.println("领取奖励 - 任务ID: " + taskId + ", 用户ID: " + userId + ", 实际进度: " + actualProgress + ", 目标: " + target + ", 是否完成: " + completed);
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
            // 更新进度为实际进度
            record.setProgress(actualProgress);
            record.setCompleted(true);
            record.setClaimed(true);
            record.setUpdateTime(LocalDateTime.now());
            this.updateById(record);
        }
        
        // 发放奖励
        int rewardPoints = (Integer) config.get("reward");
        String taskName = (String) config.get("name");
        User user = userService.getUserById(userId);
        if (user != null) {
            int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(currentPoints + rewardPoints);
            userService.updateUser(user);
        }
        
        // 创建积分记录
        try {
            PointsRecord pointsRecord = new PointsRecord();
            pointsRecord.setUserId(userId);
            pointsRecord.setType("earn");
            pointsRecord.setPoints(rewardPoints);
            // 格式：任务奖励 - 具体任务名（如 点赞评论、发布内容、关注用户 等）
            pointsRecord.setDescription("任务奖励：" + (taskName != null ? taskName : "每日任务"));
            pointsRecord.setSource("task");
            pointsRecord.setCreateTime(LocalDateTime.now());
            pointsRecordService.addRecord(pointsRecord);
            System.out.println("创建任务积分记录成功，任务: " + taskName + ", 积分: " + rewardPoints);
            
            // 更新用户的魅力值和等级
            if (userService != null) {
                userService.updateCharmAndLevel(userId);
            }
        } catch (Exception e) {
            System.err.println("创建任务积分记录失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("points", rewardPoints);
        result.put("balance", user != null ? user.getPoints() : rewardPoints);
        
        return result;
    }
    
    @Override
    @Transactional
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

    /** 今日已发帖数：正常 + 待审核（与 PostServiceImpl.getTodayPostCount 一致） */
    private int queryTodayPostContentProgress(Long userId) {
        if (userId == null || postMapper == null) {
            return 0;
        }
        try {
            QueryWrapper<com.pethome.entity.Post> postWrapper = new QueryWrapper<>();
            postWrapper.eq("user_id", userId);
            postWrapper.in("status", java.util.Arrays.asList(1, 2));
            postWrapper.apply("DATE(create_time) = CURDATE()");
            return Math.toIntExact(postMapper.selectCount(postWrapper));
        } catch (Exception e) {
            System.err.println("queryTodayPostContentProgress 失败: " + e.getMessage());
            return 0;
        }
    }
}

