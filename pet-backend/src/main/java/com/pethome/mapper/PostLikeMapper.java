package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
    
    /**
     * 检查用户是否已点赞帖子
     */
    PostLike checkLikeStatus(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 获取用户今日已点赞的帖子数量
     */
    int getTodayLikeCount(@Param("userId") Long userId);
}
