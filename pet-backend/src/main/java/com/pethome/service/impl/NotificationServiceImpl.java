package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Notification;
import com.pethome.mapper.NotificationMapper;
import com.pethome.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

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
        if (notification.getStatus() == null) {
            notification.setStatus(0);
        }
        if (notification.getCreateTime() == null) {
            notification.setCreateTime(LocalDateTime.now());
        }
        if (notification.getUpdateTime() == null) {
            notification.setUpdateTime(LocalDateTime.now());
        }
        notificationMapper.insert(notification);
        return notification;
    }

    @Override
    public Notification updateNotification(Notification notification) {
        notification.setUpdateTime(LocalDateTime.now());
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

    @Override
    public IPage<Notification> listByUserId(Long userId, Integer pageNo, Integer pageSize) {
        Page<Notification> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 20 : pageSize);
        QueryWrapper<Notification> q = new QueryWrapper<>();
        q.eq("user_id", userId).orderByDesc("create_time");
        return notificationMapper.selectPage(page, q);
    }

    @Override
    public int countUnreadByUserId(Long userId) {
        QueryWrapper<Notification> q = new QueryWrapper<>();
        q.eq("user_id", userId)
                .eq("status", 0)
                // 系统通知中心不展示社区互动（点赞/评论/@/关注），避免和“消息页三个分类”重复
                .notIn("type", Arrays.asList("community_like", "community_comment", "community_follow"));
        return Math.toIntExact(notificationMapper.selectCount(q));
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        Notification n = notificationMapper.selectById(id);
        if (n == null || !n.getUserId().equals(userId)) {
            return false;
        }
        n.setStatus(1);
        n.setUpdateTime(LocalDateTime.now());
        return notificationMapper.updateById(n) > 0;
    }

    @Override
    public int markAllAsRead(Long userId) {
        UpdateWrapper<Notification> u = new UpdateWrapper<>();
        u.eq("user_id", userId)
                .eq("status", 0)
                .notIn("type", Arrays.asList("community_like", "community_comment", "community_follow"))
                .set("status", 1)
                .set("update_time", LocalDateTime.now());
        return notificationMapper.update(null, u);
    }
}


