package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Notice;
import com.pethome.mapper.NoticeMapper;
import com.pethome.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public IPage<Notice> getNoticePage(Page<Notice> page, String keyword, String type, Integer status) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Notice::getTitle, keyword.trim());
        }
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(Notice::getType, type.trim());
        }
        if (status != null) {
            wrapper.eq(Notice::getStatus, status);
        }
        wrapper.orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getSortOrder)
                .orderByDesc(Notice::getCreateTime);
        return noticeMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Notice> getNoticeList(String type, Integer status, Integer limit) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(Notice::getType, type.trim());
        }
        if (status != null) {
            wrapper.eq(Notice::getStatus, status);
        } else {
            // 默认只返回已发布的公告
            wrapper.eq(Notice::getStatus, 1);
        }

        LocalDateTime now = LocalDateTime.now();
        // 生效时间过滤：effective_time 为空或 <= 当前时间
        wrapper.and(w -> w.isNull(Notice::getEffectiveTime).or()
                .le(Notice::getEffectiveTime, now));
        // 失效时间过滤：expire_time 为空或 > 当前时间
        wrapper.and(w -> w.isNull(Notice::getExpireTime).or()
                .gt(Notice::getExpireTime, now));

        wrapper.orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getSortOrder)
                .orderByDesc(Notice::getCreateTime);

        if (limit != null && limit > 0) {
            Page<Notice> page = new Page<>(1, limit);
            IPage<Notice> result = noticeMapper.selectPage(page, wrapper);
            return result.getRecords();
        } else {
            return noticeMapper.selectList(wrapper);
        }
    }

    @Override
    public Notice getLatestNotice(String type) {
        List<Notice> list = getNoticeList(type, 1, 1);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public Notice createNotice(Notice notice) {
        LocalDateTime now = LocalDateTime.now();
        if (notice.getCreateTime() == null) {
            notice.setCreateTime(now);
        }
        notice.setUpdateTime(now);
        if (notice.getStatus() == null) {
            notice.setStatus(0); // 默认草稿
        }
        if (notice.getIsTop() == null) {
            notice.setIsTop(0);
        }
        if (notice.getSortOrder() == null) {
            notice.setSortOrder(0);
        }
        noticeMapper.insert(notice);
        return notice;
    }

    @Override
    public Notice updateNotice(Notice notice) {
        notice.setUpdateTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return noticeMapper.selectById(notice.getId());
    }

    @Override
    public boolean deleteNotice(Long id) {
        return noticeMapper.deleteById(id) > 0;
    }

    @Override
    public Notice getById(Long id) {
        return noticeMapper.selectById(id);
    }
}

