package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Complaint;
import com.pethome.entity.Notification;
import com.pethome.mapper.ComplaintMapper;
import com.pethome.service.ComplaintService;
import com.pethome.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Override
    public Complaint submit(Long userId, String type, String content, String contactInfo, String images) {
        Complaint c = new Complaint();
        c.setUserId(userId);
        c.setType(type != null ? type : "其他");
        c.setContent(content);
        c.setContactInfo(contactInfo);
        c.setImages(images != null && !images.trim().isEmpty() ? images.trim() : null);
        c.setStatus("pending");
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        complaintMapper.insert(c);
        if (userId != null && notificationService != null) {
            try {
                Notification n = new Notification();
                n.setUserId(userId);
                n.setTitle("投诉提交成功");
                n.setContent("您的投诉（投诉单#" + c.getId() + "）已提交成功，我们将尽快处理，请耐心等待。");
                n.setType("complaint_submitted");
                n.setStatus(0);
                n.setRelatedId(c.getId());
                n.setRelatedType("complaint");
                n.setCreateTime(LocalDateTime.now());
                n.setUpdateTime(LocalDateTime.now());
                notificationService.createNotification(n);
            } catch (Exception ignored) {}
        }
        return c;
    }

    @Override
    public IPage<Complaint> page(Page<Complaint> page, String status, String type) {
        LambdaQueryWrapper<Complaint> q = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            q.eq(Complaint::getStatus, status);
        }
        if (StringUtils.hasText(type)) {
            q.eq(Complaint::getType, type);
        }
        q.orderByDesc(Complaint::getCreateTime);
        return complaintMapper.selectPage(page, q);
    }

    @Override
    public Complaint getById(Long id) {
        return complaintMapper.selectById(id);
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        Complaint c = complaintMapper.selectById(id);
        if (c == null) return false;
        c.setStatus(status);
        c.setUpdateTime(LocalDateTime.now());
        boolean ok = complaintMapper.updateById(c) > 0;
        if (ok && "resolved".equals(status) && c.getUserId() != null && notificationService != null) {
            try {
                Notification n = new Notification();
                n.setUserId(c.getUserId());
                n.setTitle("投诉处理结果");
                n.setContent("您提交的投诉（投诉单#" + id + "）已处理完成，感谢您的反馈。");
                n.setType("complaint_resolved");
                n.setStatus(0);
                n.setRelatedId(id);
                n.setRelatedType("complaint");
                n.setCreateTime(LocalDateTime.now());
                n.setUpdateTime(LocalDateTime.now());
                notificationService.createNotification(n);
            } catch (Exception ignored) {}
        }
        return ok;
    }
}
