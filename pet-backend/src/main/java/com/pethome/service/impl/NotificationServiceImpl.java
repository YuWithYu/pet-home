package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Notification;
import com.pethome.mapper.NotificationMapper;
import com.pethome.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public IPage<Notification> getNotificationList(Page<Notification> page) {
        return notificationMapper.selectPage(page, null);
    }

    @Override
    public Notification createNotification(Notification notification) {
        notificationMapper.insert(notification);
        return notification;
    }

    @Override
    public Notification updateNotification(Notification notification) {
        notificationMapper.updateById(notification);
        return notification;
    }

    @Override
    public boolean deleteNotification(Long id) {
        return notificationMapper.deleteById(id) > 0;
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationMapper.selectById(id);
    }
}


