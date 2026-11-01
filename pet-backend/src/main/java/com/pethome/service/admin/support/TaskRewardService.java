package com.pethome.service.admin.support;

import com.pethome.entity.PointsRecord;
import com.pethome.entity.User;
import com.pethome.service.order.PointsRecordService;
import com.pethome.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 任务奖励发放支持类：积分入账、积分流水与魅力值/等级联动。
 *
 * 说明：本类不声明事务，始终运行在 TaskServiceImpl.claimTaskReward 的事务内。
 */
@Service
public class TaskRewardService {

    private static final Logger log = LoggerFactory.getLogger(TaskRewardService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PointsRecordService pointsRecordService;

    /**
     * 发放任务奖励积分。
     *
     * @return 发放后的积分余额（用户不存在时为本次奖励积分）
     */
    public int grantTaskReward(Long userId, int rewardPoints, String taskName) {
        User user = userService.getUserById(userId);
        if (user != null) {
            int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(currentPoints + rewardPoints);
            userService.updateUser(user);
        }

        // 创建积分记录（失败不影响领取结果）
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
            log.info("创建任务积分记录成功，任务: {}, 积分: {}", taskName, rewardPoints);

            // 更新用户的魅力值和等级
            if (userService != null) {
                userService.updateCharmAndLevel(userId);
            }
        } catch (Exception e) {
            log.error("创建任务积分记录失败", e);
        }

        return user != null ? user.getPoints() : rewardPoints;
    }
}
