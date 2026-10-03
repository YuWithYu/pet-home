package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.TopicCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TopicCollectMapper extends BaseMapper<TopicCollect> {

    TopicCollect checkCollectStatus(@Param("topicId") Long topicId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM topic_collect WHERE topic_id = #{topicId}")
    int countByTopicId(@Param("topicId") Long topicId);
}
