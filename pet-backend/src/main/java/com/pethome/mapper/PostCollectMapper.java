package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.PostCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostCollectMapper extends BaseMapper<PostCollect> {
    
    /**
     * 检查用户是否已收藏帖子
     */
    PostCollect checkCollectStatus(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 统计某帖子的收藏数（用于规则型推荐得分）
     */
    @Select("SELECT COUNT(*) FROM post_collect WHERE post_id = #{postId}")
    int countByPostId(@Param("postId") Long postId);
    
    /**
     * 获取用户今日已收藏的帖子数量
     */
    int getTodayCollectCount(@Param("userId") Long userId);
}

