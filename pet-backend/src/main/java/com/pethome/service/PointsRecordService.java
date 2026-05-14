package com.pethome.service;

import com.pethome.entity.PointsRecord;
import java.util.List;
import java.util.Map;

public interface PointsRecordService {
    /**
     * 添加积分记录
     */
    void addRecord(PointsRecord record);
    
    /**
     * 获取用户的积分获取记录
     */
    List<PointsRecord> getEarnRecords(Long userId);
    
    /**
     * 获取用户的积分消耗记录
     */
    List<PointsRecord> getSpendRecords(Long userId);
    
    /**
     * 获取用户的积分统计信息
     */
    Map<String, Object> getPointsStatistics(Long userId);
    
    /**
     * 计算用户的总魅力值（所有历史积分累计总和）
     * @param userId 用户ID
     * @return 魅力值
     */
    Integer calculateCharm(Long userId);
}

