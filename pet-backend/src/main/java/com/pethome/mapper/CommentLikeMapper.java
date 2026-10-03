package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    
    /**
     * 检查用户是否已点赞评论
     */
    CommentLike checkLikeStatus(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    /**
     * 获取用户今日已点赞的评论数量
     */
    int getTodayLikeCount(@Param("userId") Long userId);
}
