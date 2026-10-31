package com.pethome.service.community;

/**
 * 帖子曝光与热度维护（批量）。
 */
public interface PostExposureService {

    /**
     * 计算 7 天内新帖的曝光分数增量。
     *
     * @return 受影响条数
     */
    int updateExposureScore();

    /**
     * 按热度公式刷新 is_hot 标记。
     *
     * @return 本次标记变更的帖子数（新增热门 + 取消热门）
     */
    int refreshHotFlag();
}
