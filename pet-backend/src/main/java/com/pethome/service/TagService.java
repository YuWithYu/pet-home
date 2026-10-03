package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {
    
    /**
     * 根据标签名获取或创建标签
     */
    Tag getOrCreateTag(String tagName);
    
    /**
     * 批量获取或创建标签
     */
    List<Tag> batchGetOrCreateTags(List<String> tagNames);
    
    /**
     * 获取热门标签（从Redis）
     */
    List<String> getHotTags(Integer limit);
    
    /**
     * 增加标签热度（Redis）
     */
    void incrementTagHotness(String tagName);
    
    /**
     * 根据帖子ID获取标签列表
     */
    List<Tag> getTagsByPostId(Long postId);
    
    /**
     * 根据标签名获取帖子ID列表
     */
    List<Long> getPostIdsByTagName(String tagName);

    /**
     * 获取全部标签名（用于发现页标签栏）
     */
    List<String> getAllTagNames();

    /**
     * 管理员创建标签
     */
    Tag createTag(String tagName);

    /**
     * 管理员删除标签（受保护标签不可删除）
     */
    boolean deleteTagByName(String tagName);
}

