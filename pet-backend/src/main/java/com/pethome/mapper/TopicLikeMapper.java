package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.TopicLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TopicLikeMapper extends BaseMapper<TopicLike> {

    TopicLike checkLikeStatus(@Param("topicId") Long topicId, @Param("userId") Long userId);
}
