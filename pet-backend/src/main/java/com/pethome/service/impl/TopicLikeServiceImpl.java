package com.pethome.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.TopicLike;
import com.pethome.mapper.TopicLikeMapper;
import com.pethome.service.DailyTopicService;
import com.pethome.service.TopicLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TopicLikeServiceImpl extends ServiceImpl<TopicLikeMapper, TopicLike> implements TopicLikeService {

    @Autowired
    private TopicLikeMapper topicLikeMapper;

    @Autowired
    private DailyTopicService dailyTopicService;

    @Override
    @Transactional
    public boolean likeTopic(Long topicId, Long userId) {
        if (topicId == null || userId == null) {
            throw new IllegalArgumentException("专题ID和用户ID不能为空");
        }
        TopicLike existing = topicLikeMapper.checkLikeStatus(topicId, userId);
        if (existing != null) {
            return false;
        }
        TopicLike like = new TopicLike();
        like.setTopicId(topicId);
        like.setUserId(userId);
        like.setCreateTime(LocalDateTime.now());
        topicLikeMapper.insert(like);
        dailyTopicService.incrementLikeCount(topicId);
        return true;
    }

    @Override
    @Transactional
    public boolean unlikeTopic(Long topicId, Long userId) {
        if (topicId == null || userId == null) {
            throw new IllegalArgumentException("专题ID和用户ID不能为空");
        }
        TopicLike like = topicLikeMapper.checkLikeStatus(topicId, userId);
        if (like == null) {
            return false;
        }
        topicLikeMapper.deleteById(like.getId());
        dailyTopicService.decrementLikeCount(topicId);
        return true;
    }

    @Override
    public boolean isLiked(Long topicId, Long userId) {
        return topicLikeMapper.checkLikeStatus(topicId, userId) != null;
    }
}
