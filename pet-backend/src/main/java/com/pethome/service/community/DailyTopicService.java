package com.pethome.service.community;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.DailyTopic;
import com.pethome.entity.Post;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    /**
     * 兼容接口的聚合数据：主题/日期/今日专题/帖子/Banner/已发布专题列表。
     * 单项查询失败时降级为空数据，保证 topics 始终可返回。
     */
    Map<String, Object> getDailyTopicsOverview();
}



