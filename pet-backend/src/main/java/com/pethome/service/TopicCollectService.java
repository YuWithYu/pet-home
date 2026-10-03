package com.pethome.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.DailyTopic;
import com.pethome.entity.TopicCollect;

public interface TopicCollectService extends IService<TopicCollect> {

    boolean collectTopic(Long topicId, Long userId);
    boolean uncollectTopic(Long topicId, Long userId);
    boolean isCollected(Long topicId, Long userId);
    int getCollectCount(Long topicId);
    Page<DailyTopic> getCollectedTopics(Long userId, Page<DailyTopic> page);
}
