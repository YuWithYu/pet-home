package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.ReviewReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReviewReplyMapper extends BaseMapper<ReviewReply> {
    /**
     * 根据评论ID获取所有回复
     */
    List<ReviewReply> findByReviewId(@Param("reviewId") Long reviewId);
    
    /**
     * 统计评论的回复数
     */
    Integer countByReviewId(@Param("reviewId") Long reviewId);
}
