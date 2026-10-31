package com.pethome.task;

import com.pethome.service.community.PostExposureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 帖子曝光分数调度入口：定期更新新帖曝光分数，使其逐步进入热门帖区块。
 */
@Component
public class PostExposureTask {

    private static final Logger logger = LoggerFactory.getLogger(PostExposureTask.class);

    private final PostExposureService postExposureService;

    public PostExposureTask(PostExposureService postExposureService) {
        this.postExposureService = postExposureService;
    }

    @Scheduled(cron = "0 0 * * * ?") // 每小时执行
    public void updateExposureScore() {
        try {
            int affected = postExposureService.updateExposureScore();
            logger.info("曝光分数更新任务完成，共更新 {} 条帖子", affected);
        } catch (Exception e) {
            logger.error("更新曝光分数失败", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void updateHotPosts() {
        try {
            int changed = postExposureService.refreshHotFlag();
            logger.info("热门帖标记更新任务完成，共变更 {} 条", changed);
        } catch (Exception e) {
            logger.error("更新热门帖标记失败", e);
        }
    }
}
