package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.TopicLike;

public interface TopicLikeService extends IService<TopicLike> {

    boolean likeTopic(Long topicId, Long userId);
    boolean unlikeTopic(Long topicId, Long userId);
    boolean isLiked(Long topicId, Long userId);
}
