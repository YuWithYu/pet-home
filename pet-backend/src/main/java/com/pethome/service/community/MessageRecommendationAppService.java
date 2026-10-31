package com.pethome.service.community;

import java.util.Map;

/**
 * 私信页推荐应用服务：推荐可能认识的用户（排除已关注与本人，补全粉丝/点赞/帖子数）。
 */
public interface MessageRecommendationAppService {

    /** 推荐用户列表 */
    Map<String, Object> getRecommendedUsers(Long userId, Integer limit);
}
