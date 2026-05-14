package com.pethome.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.Post;
import com.pethome.mapper.PostMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子曝光分数定时任务
 * 定期更新新帖的曝光分数，使其逐步进入热门帖区块
 */
@Component
public class PostExposureTask {
    
    private static final Logger logger = LoggerFactory.getLogger(PostExposureTask.class);
    
    @Autowired
    private PostMapper postMapper;
    
    /**
     * 每小时执行一次，更新曝光分数
     * cron表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行
    public void updateExposureScore() {
        logger.info("开始执行帖子曝光分数更新任务...");
        
        try {
            // 查询最近7天内的新帖（排除置顶帖）
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1)
                       .eq("is_top", false)
                       .ge("create_time", sevenDaysAgo)
                       .orderByAsc("create_time");
            
            List<Post> newPosts = postMapper.selectList(queryWrapper);
            
            logger.info("找到 {} 条新帖需要更新曝光分数", newPosts.size());
            
            int updatedCount = 0;
            for (Post post : newPosts) {
                // 计算新的曝光分数增量
                // 新帖每小时增加基础曝光分数，随时间递减
                double hoursSinceCreation = java.time.Duration.between(post.getCreateTime(), LocalDateTime.now()).toHours();
                
                // 基础曝光分数增量（每小时增加0.5分，但随时间递减）
                double baseIncrement = 0.5 * Math.max(0, 1.0 - hoursSinceCreation / 168.0); // 168小时 = 7天
                
                // 根据互动数据增加曝光分数
                double interactionIncrement = 
                    (post.getLikesCount() != null ? post.getLikesCount() : 0) * 0.1 +
                    (post.getCommentsCount() != null ? post.getCommentsCount() : 0) * 0.15 +
                    (post.getViewsCount() != null ? post.getViewsCount() : 0) * 0.05;
                
                double totalIncrement = baseIncrement + interactionIncrement;
                
                // 更新曝光分数
                UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", post.getId())
                            .setSql("exposure_score = COALESCE(exposure_score, 0) + " + totalIncrement);
                
                postMapper.update(null, updateWrapper);
                updatedCount++;
            }
            
            logger.info("曝光分数更新任务完成，共更新 {} 条帖子", updatedCount);
            
        } catch (Exception e) {
            logger.error("更新曝光分数失败", e);
        }
    }
    
    /**
     * 每天凌晨2点执行，根据热度公式更新is_hot标记
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void updateHotPosts() {
        logger.info("开始执行热门帖标记更新任务...");
        
        try {
            // 计算所有帖子的热度分数，并更新is_hot标记
            // 热度 = like_count*0.5 + comment_count*0.3 + view_count*0.2 + exposure_score
            
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1).eq("is_top", false);
            
            List<Post> posts = postMapper.selectList(queryWrapper);
            
            int hotCount = 0;
            for (Post post : posts) {
                // 计算热度分数
                double hotnessScore = calculateHotnessScore(post);
                
                // 如果热度分数超过阈值（例如50分），标记为热门
                boolean shouldBeHot = hotnessScore >= 50.0;
                
                if (shouldBeHot != post.getIsHot()) {
                    UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("id", post.getId())
                                .set("is_hot", shouldBeHot);
                    
                    postMapper.update(null, updateWrapper);
                    
                    if (shouldBeHot) {
                        hotCount++;
                    }
                }
            }
            
            logger.info("热门帖标记更新任务完成，共标记 {} 条热门帖", hotCount);
            
        } catch (Exception e) {
            logger.error("更新热门帖标记失败", e);
        }
    }
    
    /**
     * 计算帖子热度分数
     * 热度 = like_count*0.5 + comment_count*0.3 + view_count*0.2 + exposure_score
     */
    private double calculateHotnessScore(Post post) {
        double likesScore = (post.getLikesCount() != null ? post.getLikesCount() : 0) * 0.5;
        double commentsScore = (post.getCommentsCount() != null ? post.getCommentsCount() : 0) * 0.3;
        double viewsScore = (post.getViewsCount() != null ? post.getViewsCount() : 0) * 0.2;
        double exposureScore = post.getExposureScore() != null ? post.getExposureScore() : 0.0;
        
        return likesScore + commentsScore + viewsScore + exposureScore;
    }
}

