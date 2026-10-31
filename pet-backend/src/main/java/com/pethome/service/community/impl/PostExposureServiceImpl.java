package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.Post;
import com.pethome.mapper.PostMapper;
import com.pethome.service.community.PostExposureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 帖子曝光与热度维护实现。
 *
 * 全部为单条批量 UPDATE：曝光分数增量与热度阈值判断在 SQL 内完成，
 * 避免历史上 selectList 全量加载 + 循环逐条 UPDATE 的 N+1 写法。
 */
@Service
public class PostExposureServiceImpl implements PostExposureService {

    /** 曝光分数衰减窗口（小时），与 7 天对齐 */
    private static final long EXPOSURE_WINDOW_HOURS = 7 * 24L;
    /** 热度阈值：likes*0.5 + comments*0.3 + views*0.2 + exposure_score */
    private static final double HOT_THRESHOLD = 50.0;
    private static final String HOT_FORMULA =
            "(IFNULL(likes_count, 0) * 0.5 + IFNULL(comments_count, 0) * 0.3 + "
            + "IFNULL(views_count, 0) * 0.2 + IFNULL(exposure_score, 0))";

    @Autowired
    private PostMapper postMapper;

    /**
     * 增量公式：
     *   base = 0.5 * max(0, 1 - hours_since_create / 168)
     *   interaction = likes*0.1 + comments*0.15 + views*0.05
     *   exposure_score += base + interaction
     */
    @Override
    public int updateExposureScore() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        UpdateWrapper<Post> uw = new UpdateWrapper<>();
        uw.eq("status", 1)
          .eq("is_top", false)
          .ge("create_time", sevenDaysAgo)
          .setSql("exposure_score = COALESCE(exposure_score, 0) + "
                + "0.5 * GREATEST(0, 1.0 - TIMESTAMPDIFF(HOUR, create_time, NOW()) / " + EXPOSURE_WINDOW_HOURS + ".0) + "
                + "IFNULL(likes_count, 0) * 0.1 + "
                + "IFNULL(comments_count, 0) * 0.15 + "
                + "IFNULL(views_count, 0) * 0.05");
        return postMapper.update(null, uw);
    }

    @Override
    public int refreshHotFlag() {
        // 1. 把热度 >= 阈值 的帖子批量设为 is_hot = true
        UpdateWrapper<Post> setHotWrapper = new UpdateWrapper<>();
        setHotWrapper.eq("status", 1)
                     .eq("is_top", false)
                     .eq("is_hot", false)
                     .ge("exposure_score", 0) // 暴露分数已存在的才考虑（保证热度可计算）
                     .apply(HOT_FORMULA + " >= " + HOT_THRESHOLD)
                     .set("is_hot", true);
        int setHotCount = postMapper.update(null, setHotWrapper);

        // 2. 把热度 < 阈值 的帖子批量设为 is_hot = false
        UpdateWrapper<Post> unsetHotWrapper = new UpdateWrapper<>();
        unsetHotWrapper.eq("status", 1)
                       .eq("is_top", false)
                       .eq("is_hot", true)
                       .apply(HOT_FORMULA + " < " + HOT_THRESHOLD)
                       .set("is_hot", false);
        int unsetHotCount = postMapper.update(null, unsetHotWrapper);

        return setHotCount + unsetHotCount;
    }
}
