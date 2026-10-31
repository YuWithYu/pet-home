package com.pethome.service.community;

import java.util.Map;

/**
 * 消息通知应用服务：已读标记、点赞/收藏、新增关注、评论与 @ 通知的聚合与未读统计。
 */
public interface MessageNotificationAppService {

    /** 标记会话内消息为已读，返回提示文案 */
    String markMessagesAsRead(Map<String, Object> request, boolean staffOrAdmin, Long loginUserId);

    /** 点赞与收藏通知列表 */
    Map<String, Object> getLikesAndCollections(Long userId, Integer page, Integer size, Long lastSeenAt,
                                               boolean staffOrAdmin, Long loginUserId);

    /** 新增关注通知列表 */
    Map<String, Object> getNewFollows(Long userId, Integer page, Integer size, Long lastSeenAt,
                                      boolean staffOrAdmin, Long loginUserId);

    /** 评论与 @ 通知列表 */
    Map<String, Object> getCommentsAndMentions(Long userId, Integer page, Integer size, Long lastSeenAt,
                                               boolean staffOrAdmin, Long loginUserId);
}
