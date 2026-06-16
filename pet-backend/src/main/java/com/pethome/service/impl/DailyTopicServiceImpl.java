package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.DailyTopic;
import com.pethome.entity.DailyTopicPost;
import com.pethome.entity.Post;
import com.pethome.mapper.DailyTopicMapper;
import com.pethome.mapper.DailyTopicPostMapper;
import com.pethome.mapper.PostMapper;
import com.pethome.service.DailyTopicService;
import com.pethome.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DailyTopicServiceImpl extends ServiceImpl<DailyTopicMapper, DailyTopic> implements DailyTopicService {

    @Autowired
    private DailyTopicPostMapper dailyTopicPostMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostService postService;

    @Override
    public DailyTopic getTodayTopic() {
        LocalDate today = LocalDate.now();
        return getTopicByDate(today);
    }

    @Override
    public DailyTopic getTopicByDate(LocalDate date) {
        QueryWrapper<DailyTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("publish_date", date);
        queryWrapper.eq("status", 1); // 只获取已发布的
        queryWrapper.orderByDesc("sort_order");
        queryWrapper.last("LIMIT 1");
        
        DailyTopic topic = this.getOne(queryWrapper);
        if (topic != null) {
            // 加载关联的帖子
            topic.setPosts(getTopicPosts(topic.getId()));
        }
        return topic;
    }

    @Override
    public Page<DailyTopic> getTopicList(Page<DailyTopic> page, Integer status) {
        QueryWrapper<DailyTopic> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("publish_date");
        queryWrapper.orderByDesc("sort_order");
        
        return this.page(page, queryWrapper);
    }

    @Override
    @Transactional
    public DailyTopic createWithPosts(DailyTopic topic, List<Long> postIds) {
        // 设置创建时间
        topic.setCreateTime(LocalDateTime.now());
        topic.setUpdateTime(LocalDateTime.now());
        
        // 保存专题
        this.save(topic);
        
        // 关联帖子
        if (postIds != null && !postIds.isEmpty()) {
            associatePosts(topic.getId(), postIds);
        }
        
        return topic;
    }

    @Override
    @Transactional
    public DailyTopic updateWithPosts(DailyTopic topic, List<Long> postIds) {
        // 设置更新时间
        topic.setUpdateTime(LocalDateTime.now());
        
        // 更新专题
        this.updateById(topic);
        
        // 删除旧的关联
        QueryWrapper<DailyTopicPost> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("topic_id", topic.getId());
        dailyTopicPostMapper.delete(deleteWrapper);
        
        // 创建新的关联
        if (postIds != null && !postIds.isEmpty()) {
            associatePosts(topic.getId(), postIds);
        }
        
        return topic;
    }

    /**
     * 关联帖子到专题
     */
    private void associatePosts(Long topicId, List<Long> postIds) {
        for (int i = 0; i < postIds.size(); i++) {
            DailyTopicPost topicPost = new DailyTopicPost();
            topicPost.setTopicId(topicId);
            topicPost.setPostId(postIds.get(i));
            topicPost.setSortOrder(i);
            topicPost.setIsFeatured(i < 3); // 前3个设为精选
            topicPost.setCreateTime(LocalDateTime.now());
            dailyTopicPostMapper.insert(topicPost);
        }
    }

    @Override
    @Transactional
    public boolean publishTopic(Long topicId) {
        DailyTopic topic = this.getById(topicId);
        if (topic == null) {
            return false;
        }
        topic.setStatus(1); // 已发布
        topic.setUpdateTime(LocalDateTime.now());
        return this.updateById(topic);
    }

    @Override
    @Transactional
    public boolean offlineTopic(Long topicId) {
        DailyTopic topic = this.getById(topicId);
        if (topic == null) {
            return false;
        }
        topic.setStatus(2); // 已下线
        topic.setUpdateTime(LocalDateTime.now());
        return this.updateById(topic);
    }

    @Override
    public List<Post> getTopicPosts(Long topicId) {
        QueryWrapper<DailyTopicPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("topic_id", topicId);
        queryWrapper.orderByAsc("sort_order");
        
        List<DailyTopicPost> topicPosts = dailyTopicPostMapper.selectList(queryWrapper);
        if (topicPosts.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取帖子ID列表
        List<Long> postIds = topicPosts.stream()
            .map(DailyTopicPost::getPostId)
            .collect(Collectors.toList());
        
        // 批量查询帖子
        List<Post> posts = postMapper.selectBatchIds(postIds);
        
        // 填充用户信息
        if (posts != null && !posts.isEmpty()) {
            ((com.pethome.service.impl.PostServiceImpl) postService).fillUserInfoAndLikeStatus(posts, null);
        }
        
        return posts != null ? posts : new ArrayList<>();
    }

    @Override
    public boolean existsByDate(LocalDate date) {
        QueryWrapper<DailyTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("publish_date", date);
        return this.count(queryWrapper) > 0;
    }

    @Override
    @Transactional
    public void incrementViewCount(Long topicId) {
        DailyTopic topic = this.getById(topicId);
        if (topic != null) {
            topic.setViewCount((topic.getViewCount() != null ? topic.getViewCount() : 0) + 1);
            this.updateById(topic);
        }
    }

    @Override
    @Transactional
    public void incrementLikeCount(Long topicId) {
        DailyTopic topic = this.getById(topicId);
        if (topic != null) {
            topic.setLikeCount((topic.getLikeCount() != null ? topic.getLikeCount() : 0) + 1);
            this.updateById(topic);
        }
    }

    @Override
    @Transactional
    public void decrementLikeCount(Long topicId) {
        DailyTopic topic = this.getById(topicId);
        if (topic != null) {
            int current = topic.getLikeCount() != null ? topic.getLikeCount() : 0;
            topic.setLikeCount(Math.max(0, current - 1));
            this.updateById(topic);
        }
    }
}

