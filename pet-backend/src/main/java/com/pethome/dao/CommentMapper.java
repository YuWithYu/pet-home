package com.pethome.dao;

import com.pethome.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
//    int deleteByPrimaryKey(Long id);
//
//    int insert(Comment record);
//
//    int insertSelective(Comment record);
//
//    Comment selectByPrimaryKey(Long id);
//
//    int updateByPrimaryKeySelective(Comment record);
//
//    int updateByPrimaryKeyWithBLOBs(Comment record);
//
//    int updateByPrimaryKey(Comment record);

    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") Long entityId, @Param("offset") int offset, @Param("limit") int limit);

    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") Long entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(Long id);
}