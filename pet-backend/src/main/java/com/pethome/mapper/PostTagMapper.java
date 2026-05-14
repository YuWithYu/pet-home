package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {
    
    /**
     * 批量插入帖子标签关联
     */
    @Insert("<script>" +
            "INSERT INTO post_tag (post_id, tag_id) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.postId}, #{item.tagId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(java.util.List<PostTag> postTags);
    
    /**
     * 删除帖子的所有标签关联
     */
    @Delete("DELETE FROM post_tag WHERE post_id = #{postId}")
    int deleteByPostId(Long postId);

    /**
     * 删除某标签的所有帖子关联
     */
    @Delete("DELETE FROM post_tag WHERE tag_id = #{tagId}")
    int deleteByTagId(Long tagId);
}

