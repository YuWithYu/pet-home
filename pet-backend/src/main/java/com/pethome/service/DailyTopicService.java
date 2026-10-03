package com.pethome.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.DailyTopic;
import com.pethome.entity.Post;
import java.time.LocalDate;
import java.util.List;

/**
 * 每日专题服务接口
 */
public interface DailyTopicService extends IService<DailyTopic> {
    
    /**
     * 获取今日专题
     */
    DailyTopic getTodayTopic();
    
    /**
     * 根据日期获取专题
     */
    DailyTopic getTopicByDate(LocalDate date);
    
    /**
     * 获取专题列表（分页）
     */
    Page<DailyTopic> getTopicList(Page<DailyTopic> page, Integer status);
    
    /**
     * 创建专题并关联帖子
     */
    DailyTopic createWithPosts(DailyTopic topic, List<Long> postIds);
    
    /**
     * 更新专题并更新关联帖子
     */
    DailyTopic updateWithPosts(DailyTopic topic, List<Long> postIds);
    
    /**
     * 发布专题
     */
    boolean publishTopic(Long topicId);
    
    /**
     * 下线专题
     */
    boolean offlineTopic(Long topicId);
    
    /**
     * 获取专题关联的帖子
     */
    List<Post> getTopicPosts(Long topicId);
    
    /**
     * 检查指定日期是否已有专题
     */
    boolean existsByDate(LocalDate date);
    
    /**
     * 增加专题浏览量
     */
    void incrementViewCount(Long topicId);

    /**
     * 增加专题点赞数
     */
    void incrementLikeCount(Long topicId);

    /**
     * 减少专题点赞数
     */
    void decrementLikeCount(Long topicId);
}



