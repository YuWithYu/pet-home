package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.DailyTopic;
import com.pethome.entity.TopicCollect;
import com.pethome.mapper.TopicCollectMapper;
import com.pethome.service.DailyTopicService;
import com.pethome.service.TaskService;
import com.pethome.service.TopicCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TopicCollectServiceImpl extends ServiceImpl<TopicCollectMapper, TopicCollect> implements TopicCollectService {

    @Autowired
    private TopicCollectMapper topicCollectMapper;

    @Autowired
    private DailyTopicService dailyTopicService;

    @Autowired
    @Lazy
    private TaskService taskService;

    @Override
    @Transactional
    public boolean collectTopic(Long topicId, Long userId) {
        if (topicId == null || userId == null) {
            throw new IllegalArgumentException("专题ID和用户ID不能为空");
        }
        TopicCollect existing = topicCollectMapper.checkCollectStatus(topicId, userId);
        if (existing != null) {
            return false;
        }
        TopicCollect collect = new TopicCollect();
        collect.setTopicId(topicId);
        collect.setUserId(userId);
        collect.setCreateTime(LocalDateTime.now());
        topicCollectMapper.insert(collect);
        try {
            Long count = topicCollectMapper.selectCount(
                new QueryWrapper<TopicCollect>().eq("user_id", userId)
                    .apply("DATE(create_time) = CURDATE()"));
            int todayCount = count != null ? count.intValue() : 0;
            taskService.updateTaskProgress(userId, "collect_content", todayCount);
        } catch (Exception e) {
            // 任务更新失败不影响收藏
        }
        return true;
    }

    @Override
    @Transactional
    public boolean uncollectTopic(Long topicId, Long userId) {
        if (topicId == null || userId == null) {
            throw new IllegalArgumentException("专题ID和用户ID不能为空");
        }
        TopicCollect collect = topicCollectMapper.checkCollectStatus(topicId, userId);
        if (collect == null) {
            return false;
        }
        topicCollectMapper.deleteById(collect.getId());
        return true;
    }

    @Override
    public boolean isCollected(Long topicId, Long userId) {
        return topicCollectMapper.checkCollectStatus(topicId, userId) != null;
    }

    @Override
    public int getCollectCount(Long topicId) {
        return topicCollectMapper.countByTopicId(topicId);
    }

    @Override
    public Page<DailyTopic> getCollectedTopics(Long userId, Page<DailyTopic> page) {
        QueryWrapper<TopicCollect> q = new QueryWrapper<>();
        q.eq("user_id", userId).orderByDesc("create_time");
        List<TopicCollect> collects = topicCollectMapper.selectList(q);
        if (collects == null || collects.isEmpty()) {
            page.setRecords(new ArrayList<>());
            page.setTotal(0);
            return page;
        }
        List<Long> topicIds = collects.stream().map(TopicCollect::getTopicId).distinct().collect(Collectors.toList());
        long total = topicIds.size();
        int from = (int) ((page.getCurrent() - 1) * page.getSize());
        int to = (int) Math.min(from + page.getSize(), topicIds.size());
        if (from >= topicIds.size()) {
            page.setRecords(new ArrayList<>());
            page.setTotal(total);
            return page;
        }
        List<Long> pageIds = topicIds.subList(from, to);
        List<DailyTopic> topics = dailyTopicService.listByIds(pageIds);
        // 按收藏顺序排列（collects 顺序）
        List<DailyTopic> ordered = new ArrayList<>();
        for (Long id : pageIds) {
            topics.stream().filter(t -> t.getId().equals(id)).findFirst().ifPresent(ordered::add);
        }
        page.setRecords(ordered);
        page.setTotal(total);
        return page;
    }
}
