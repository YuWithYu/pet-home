package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    
    /**
     * 根据标签名查找标签
     */
    @Select("SELECT * FROM tag WHERE name = #{name}")
    Tag findByName(String name);
    
    /**
     * 根据帖子ID获取标签列表
     */
    @Select("SELECT t.* FROM tag t INNER JOIN post_tag pt ON t.id = pt.tag_id WHERE pt.post_id = #{postId}")
    List<Tag> findByPostId(Long postId);
    
    /**
     * 根据标签名获取帖子ID列表
     */
    @Select("SELECT pt.post_id FROM post_tag pt INNER JOIN tag t ON pt.tag_id = t.id WHERE t.name = #{tagName}")
    List<Long> findPostIdsByTagName(String tagName);
}

