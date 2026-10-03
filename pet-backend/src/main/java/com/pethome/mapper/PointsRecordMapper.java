package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.PointsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord> {
    /**
     * 根据用户ID和类型获取积分记录
     */
    List<PointsRecord> getRecordsByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
    
    /**
     * 获取用户累计获取的积分
     */
    Integer getTotalEarnedByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户累计消耗的积分
     */
    Integer getTotalSpentByUserId(@Param("userId") Long userId);
}

