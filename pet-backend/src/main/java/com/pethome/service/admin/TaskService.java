package com.pethome.service.admin;

import java.util.List;
import java.util.Map;

/**
 * 任务服务接口
 */
public interface TaskService {
    
    /**
     * 获取任务进度
     * @param userId 用户ID
     * @return 任务进度数据
     */
    Map<String, Object> getTaskProgress(Long userId);
    
    /**
     * 获取每日任务列表
     * @param userId 用户ID
     * @return 每日任务列表
     */
    List<Map<String, Object>> getDailyTasks(Long userId);
    
    /**
     * 领取任务奖励
     * @param userId 用户ID
     * @param taskId 任务ID
     * @return 领取结果
     */
    Map<String, Object> claimTaskReward(Long userId, String taskId);
    
    /**
     * 更新任务进度
     * @param userId 用户ID
     * @param taskType 任务类型
     * @param progress 进度值
     */
    void updateTaskProgress(Long userId, String taskType, int progress);

    /**
     * 管理员审核/下架帖子后同步「发布内容」任务进度。
     * 同步失败不影响主流程（仅记录告警日志）。
     * @param userId 用户ID
     */
    void syncPostContentProgress(Long userId);
}

