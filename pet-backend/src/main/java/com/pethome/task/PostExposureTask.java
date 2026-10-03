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

/**
 * 帖子曝光分数定时任务
 * 定期更新新帖的曝光分数，使其逐步进入热门帖区块
 */
@Component
public class PostExposureTask {

    private static final Logger logger = LoggerFactory.getLogger(PostExposureTask.class);

    /** 曝光分数衰减窗口（小时），与 7 天对齐 */
    private static final long EXPOSURE_WINDOW_HOURS = 7 * 24L;

    @Autowired
    private PostMapper postMapper;

    /**
     * 每小时执行一次，更新曝光分数。
     *
     * 性能修复 #53：原实现先 selectList 全量加载到内存，再循环逐条 UPDATE（N+1 写入）。
     * 现改为单条 UPDATE 直接在 SQL 中计算增量，1 次写入替代 N 次，且无需加载到内存。
     *
     * 增量公式（与原 Java 实现保持一致）：
     *   base = 0.5 * max(0, 1 - hours_since_create / 168)
     *   interaction = likes*0.1 + comments*0.15 + views*0.05
     *   exposure_score += base + interaction
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行
    public void updateExposureScore() {
        logger.info("开始执行帖子曝光分数更新任务...");

        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            // 单条 UPDATE 完成：曝光分数增量在 SQL 内计算，避免 N+1
            // MySQL TIMESTAMPDIFF(HOUR, create_time, NOW()) 等价于 Java 的 Duration.between(...).toHours()
            UpdateWrapper<Post> uw = new UpdateWrapper<>();
            uw.eq("status", 1)
              .eq("is_top", false)
              .ge("create_time", sevenDaysAgo)
              .setSql("exposure_score = COALESCE(exposure_score, 0) + "
                    + "0.5 * GREATEST(0, 1.0 - TIMESTAMPDIFF(HOUR, create_time, NOW()) / " + EXPOSURE_WINDOW_HOURS + ".0) + "
                    + "IFNULL(likes_count, 0) * 0.1 + "
                    + "IFNULL(comments_count, 0) * 0.15 + "
                    + "IFNULL(views_count, 0) * 0.05");
            int affected = postMapper.update(null, uw);

            logger.info("曝光分数更新任务完成，共更新 {} 条帖子", affected);
        } catch (Exception e) {
            logger.error("更新曝光分数失败", e);
        }
    }

    /**
     * 每天凌晨2点执行，根据热度公式更新 is_hot 标记
     *
     * 性能修复 #44：原 selectList 全表扫描 + 循环逐条 UPDATE 改为 2 条批量 UPDATE
     * 热度 = likes*0.5 + comments*0.3 + views*0.2 + exposure_score
     * 阈值 50.0：通过 apply 直接在 SQL 中过滤，避免全量加载到内存
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void updateHotPosts() {
        logger.info("开始执行热门帖标记更新任务...");

        try {
            // 1. 把热度 >= 50 的帖子批量设为 is_hot = true
            UpdateWrapper<Post> setHotWrapper = new UpdateWrapper<>();
            setHotWrapper.eq("status", 1)
                         .eq("is_top", false)
                         .eq("is_hot", false)
                         .ge("exposure_score", 0) // 暴露分数已存在的才考虑（保证热度可计算）
                         .apply("(IFNULL(likes_count, 0) * 0.5 + IFNULL(comments_count, 0) * 0.3 + "
                                + "IFNULL(views_count, 0) * 0.2 + IFNULL(exposure_score, 0)) >= 50.0")
                         .set("is_hot", true);
            int setHotCount = postMapper.update(null, setHotWrapper);

            // 2. 把热度 < 50 的帖子批量设为 is_hot = false
            UpdateWrapper<Post> unsetHotWrapper = new UpdateWrapper<>();
            unsetHotWrapper.eq("status", 1)
                           .eq("is_top", false)
                           .eq("is_hot", true)
                           .apply("(IFNULL(likes_count, 0) * 0.5 + IFNULL(comments_count, 0) * 0.3 + "
                                  + "IFNULL(views_count, 0) * 0.2 + IFNULL(exposure_score, 0)) < 50.0")
                           .set("is_hot", false);
            int unsetHotCount = postMapper.update(null, unsetHotWrapper);

            logger.info("热门帖标记更新任务完成: 新增热门 {} 条, 取消热门 {} 条", setHotCount, unsetHotCount);
        } catch (Exception e) {
            logger.error("更新热门帖标记失败", e);
        }
    }
}
