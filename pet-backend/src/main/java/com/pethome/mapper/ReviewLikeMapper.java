package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.ReviewLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewLikeMapper extends BaseMapper<ReviewLike> {
    /**
     * 检查用户是否已点赞该评论
     */
    ReviewLike findByReviewIdAndUserId(@Param("reviewId") Long reviewId, @Param("userId") Long userId);
    
    /**
     * 统计评论的点赞数
     */
    Integer countByReviewId(@Param("reviewId") Long reviewId);
}
