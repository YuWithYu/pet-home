package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.DailyTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;

@Mapper
public interface DailyTopicMapper extends BaseMapper<DailyTopic> {
    /**
     * 根据发布日期查询专题
     */
    DailyTopic selectByPublishDate(@Param("publishDate") LocalDate publishDate);
    
    /**
     * 检查指定日期是否已有专题
     */
    boolean existsByPublishDate(@Param("publishDate") LocalDate publishDate);
}



