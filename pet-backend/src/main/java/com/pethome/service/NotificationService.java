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
}


