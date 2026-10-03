package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.PointsRecord;
import com.pethome.mapper.PointsRecordMapper;
import com.pethome.service.PointsRecordService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsRecordServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements PointsRecordService {
    
    @Override
    public void addRecord(PointsRecord record) {
        this.save(record);
    }
    
    @Override
    public List<PointsRecord> getEarnRecords(Long userId) {
        QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("type", "earn")
               .orderByDesc("create_time");
        return this.list(wrapper);
    }
    
    @Override
    public List<PointsRecord> getSpendRecords(Long userId) {
        QueryWrapper<PointsRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("type", "spend")
               .orderByDesc("create_time");
        return this.list(wrapper);
    }
    
    @Override
    public Map<String, Object> getPointsStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取累计获取
        Integer totalEarned = baseMapper.getTotalEarnedByUserId(userId);
        result.put("totalEarned", totalEarned != null ? totalEarned : 0);
        
        // 获取累计消耗
        Integer totalSpent = baseMapper.getTotalSpentByUserId(userId);
        result.put("totalSpent", totalSpent != null ? totalSpent : 0);
        
        return result;
    }
    
    @Override
    public Integer calculateCharm(Long userId) {
        // 魅力值 = 所有历史积分的累计总和（只计算earn类型）
        Integer totalEarned = baseMapper.getTotalEarnedByUserId(userId);
        return totalEarned != null ? totalEarned : 0;
    }
}

