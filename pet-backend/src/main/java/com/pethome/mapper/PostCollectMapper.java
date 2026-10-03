package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.PostCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostCollectMapper extends BaseMapper<PostCollect> {

    /**
     * 检查用户是否已收藏帖子
     */
    PostCollect checkCollectStatus(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 批量统计帖子收藏数（用于推荐/热度列表，单次 GROUP BY 消除 N+1）
     * 返回行结构：postId -> 帖子ID，cnt -> 收藏数
     */
    @Select("<script>"
            + "SELECT post_id AS postId, COUNT(*) AS cnt FROM post_collect "
            + "WHERE post_id IN "
            + "<foreach item='id' collection='postIds' open='(' separator=',' close=')'>#{id}</foreach> "
            + "GROUP BY post_id"
            + "</script>")
    List<Map<String, Object>> countByPostIds(@Param("postIds") List<Long> postIds);

    /**
     * 获取用户今日已收藏的帖子数量
     */
    int getTodayCollectCount(@Param("userId") Long userId);
}

