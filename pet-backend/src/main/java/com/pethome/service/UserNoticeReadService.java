package com.pethome.service;

import java.util.List;

/**
 * 用户公告已读
 */
public interface UserNoticeReadService {
    /** 标记某条公告为已读 */
    void markAsRead(Long userId, Long noticeId);
    /** 用户已读的公告ID列表 */
    List<Long> getReadNoticeIds(Long userId);
}
