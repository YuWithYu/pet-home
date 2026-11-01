package com.pethome.service.user.support;

import com.pethome.common.BusinessException;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.User;
import com.pethome.service.order.PointsRecordService;
import com.pethome.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户积分视图组件。
 * <p>
 * 负责积分流水/统计/魅力值的查询与展示结构转换，从用户资料服务中拆分，便于积分体系独立演进。
 */
@Service
public class UserPointsViewService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserIdentityResolver userIdentityResolver;

    @Autowired
    private PointsRecordService pointsRecordService;

    public Map<String, Object> getPointsRecords(String authorization, Long userId, String type, boolean staffOrAdmin) {
        // 从 token 获取用户ID
        Long currentUserId = userIdentityResolver.resolveUserId(authorization);
        if (currentUserId == null && userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 安全修复保留：积分记录属个人数据，非管理端强制以登录态为准
        Long targetUserId = staffOrAdmin ? (userId != null ? userId : currentUserId) : currentUserId;

        Map<String, Object> result = new HashMap<>();
        List<PointsRecord> records;
        if ("earn".equals(type)) {
            records = pointsRecordService.getEarnRecords(targetUserId);
        } else if ("spend".equals(type)) {
            records = pointsRecordService.getSpendRecords(targetUserId);
        } else {
            throw new BusinessException("类型参数错误，只能是 'earn' 或 'spend'");
        }

        // 转换为前端需要的格式
        List<Map<String, Object>> recordList = new ArrayList<>();
        for (PointsRecord record : records) {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("description", record.getDescription());
            recordMap.put("amount", record.getPoints() != null ? record.getPoints() : 0); // 使用points字段，返回给前端时用amount
            recordMap.put("type", record.getType());
            recordMap.put("source", record.getSource() != null ? record.getSource() : ""); // source可能为null
            recordMap.put("createTime", record.getCreateTime());
            recordMap.put("time", record.getCreateTime()); // 兼容前端
            recordList.add(recordMap);
        }

        result.put("records", recordList);
        result.put("statistics", pointsRecordService.getPointsStatistics(targetUserId));
        return result;
    }

    public Map<String, Object> getPointsStatistics(String authorization, Long userId, boolean staffOrAdmin) {
        // 从 token 获取用户ID
        Long currentUserId = userIdentityResolver.resolveUserId(authorization);
        if (currentUserId == null && userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 安全修复保留：积分余额/统计属个人数据，非管理端强制以登录态为准
        Long targetUserId = staffOrAdmin ? (userId != null ? userId : currentUserId) : currentUserId;

        // 先清除用户缓存，再查库，保证与签到/任务中心显示的余额一致（避免 Redis 缓存旧积分）
        userService.clearUserCache(targetUserId);
        User user = userService.getUserById(targetUserId);
        Integer currentBalance = user != null ? user.getPoints() : 0;

        // 获取统计信息（来自 points_record 的累计）
        Map<String, Object> statistics = pointsRecordService.getPointsStatistics(targetUserId);
        statistics.put("currentBalance", currentBalance);

        // 无流水时保持逻辑一致：当前剩余有值则累计获取至少等于当前剩余
        Object te = statistics.get("totalEarned");
        Object ts = statistics.get("totalSpent");
        int totalEarned = (te instanceof Number) ? ((Number) te).intValue() : 0;
        int totalSpent = (ts instanceof Number) ? ((Number) ts).intValue() : 0;
        if (totalEarned == 0 && currentBalance != null && currentBalance > 0 && totalSpent == 0) {
            statistics.put("totalEarned", currentBalance);
        }
        return statistics;
    }

    public Map<String, Object> updateCharmAndLevel(String authorization) {
        Long userId = userIdentityResolver.resolveUserId(authorization);
        if (userId == null) {
            throw new BusinessException(401, "未登录或登录信息无效");
        }

        // 调用服务更新魅力值和等级
        userService.updateCharmAndLevel(userId);

        // 获取更新后的用户信息
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("charm", user.getCharm() != null ? user.getCharm() : 0);
        result.put("level", user.getMemberLevel() != null ? user.getMemberLevel() : 0);
        result.put("points", user.getPoints() != null ? user.getPoints() : 0);

        // 计算总历史积分
        Map<String, Object> statistics = pointsRecordService.getPointsStatistics(userId);
        result.put("totalEarned", statistics.get("totalEarned"));
        result.put("totalSpent", statistics.get("totalSpent"));
        return result;
    }
}
