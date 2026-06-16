package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 获取帖子的评论列表（包含用户信息）
     */
    List<Comment> getCommentsByPostId(@Param("postId") Long postId);
    
    /**
     * 获取专题的评论列表（包含用户信息）
     */
    List<Comment> getCommentsByTopicId(@Param("topicId") Long topicId);
    
    /**
     * 获取评论的回复列表
     */
    List<Comment> getRepliesByCommentId(@Param("commentId") Long commentId);
}
