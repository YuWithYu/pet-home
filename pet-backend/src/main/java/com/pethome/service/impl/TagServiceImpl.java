package com.pethome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.Tag;
import com.pethome.mapper.PostTagMapper;
import com.pethome.mapper.TagMapper;
import com.pethome.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    
    @Autowired
    private TagMapper tagMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private PostTagMapper postTagMapper;
    
    private static final String HOT_TAGS_KEY = "tag:hot";
    
    @Override
    @Transactional
    public Tag getOrCreateTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }
        
        tagName = tagName.trim();
        
        // 先查找是否已存在
        Tag tag = tagMapper.findByName(tagName);
        if (tag != null) {
            return tag;
        }
        
        // 不存在则创建
        tag = new Tag();
        tag.setName(tagName);
        tag.setCreateTime(LocalDateTime.now());
        tagMapper.insert(tag);
        
        return tag;
    }
    
    @Override
    @Transactional
    public List<Tag> batchGetOrCreateTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            if (tagName != null && !tagName.trim().isEmpty()) {
                Tag tag = getOrCreateTag(tagName.trim());
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }
        return tags;
    }
    
    @Override
    public List<String> getHotTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        // 从Redis ZSet中获取前N个热门标签
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_TAGS_KEY, 0, limit - 1);
        
        if (tuples == null || tuples.isEmpty()) {
            // Redis 没有热度数据时，回退到数据库标签
            return getAllTagNames().stream().limit(limit).collect(Collectors.toList());
        }
        
        return tuples.stream()
                .map(tuple -> tuple.getValue())
                .collect(Collectors.toList());
    }
    
    @Override
    public void incrementTagHotness(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return;
        }
        
        // 在Redis ZSet中增加标签热度分数
        stringRedisTemplate.opsForZSet().incrementScore(HOT_TAGS_KEY, tagName.trim(), 1);
    }
    
    @Override
    public List<Tag> getTagsByPostId(Long postId) {
        if (postId == null) {
            return new ArrayList<>();
        }
        return tagMapper.findByPostId(postId);
    }
    
    @Override
    public List<Long> getPostIdsByTagName(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return tagMapper.findPostIdsByTagName(tagName.trim());
    }

    @Override
    public List<String> getAllTagNames() {
        List<Tag> tags = this.list();
        List<String> names = tags == null ? new ArrayList<>() : tags.stream()
                .map(Tag::getName)
                .filter(n -> n != null && !n.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        return names;
    }

    @Override
    @Transactional
    public Tag createTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }
        return getOrCreateTag(tagName.trim());
    }

    @Override
    @Transactional
    public boolean deleteTagByName(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        String normalized = tagName.trim();
        Tag tag = tagMapper.findByName(normalized);
        if (tag == null || tag.getId() == null) {
            return false;
        }
        if (postTagMapper != null) {
            postTagMapper.deleteByTagId(tag.getId());
        }
        int rows = tagMapper.deleteById(tag.getId());
        stringRedisTemplate.opsForZSet().remove(HOT_TAGS_KEY, normalized);
        return rows > 0;
    }
}

