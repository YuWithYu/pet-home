package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Notification;

public interface NotificationService {
    IPage<Notification> getNotificationList(Page<Notification> page);
    Notification createNotification(Notification notification);
    Notification updateNotification(Notification notification);
    boolean deleteNotification(Long id);
    Notification getNotificationById(Long id);

    /** 按用户分页查询通知（C 端「我的通知」） */
    IPage<Notification> listByUserId(Long userId, Integer pageNo, Integer pageSize);
    /** 用户未读数量 */
    int countUnreadByUserId(Long userId);
    /** 标记已读 */
    boolean markAsRead(Long id, Long userId);
    /** 全部标为已读 */
    int markAllAsRead(Long userId);
}


