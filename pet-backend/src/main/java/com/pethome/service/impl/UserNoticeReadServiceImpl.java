package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.UserNoticeRead;
import com.pethome.mapper.UserNoticeReadMapper;
import com.pethome.service.UserNoticeReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserNoticeReadServiceImpl implements UserNoticeReadService {

    @Autowired
    private UserNoticeReadMapper userNoticeReadMapper;

    @Override
    @Transactional
    public void markAsRead(Long userId, Long noticeId) {
        if (userId == null || noticeId == null) return;
        try {
            QueryWrapper<UserNoticeRead> q = new QueryWrapper<>();
            q.eq("user_id", userId).eq("notice_id", noticeId);
            if (userNoticeReadMapper.selectCount(q) > 0) return;
            UserNoticeRead r = new UserNoticeRead();
            r.setUserId(userId);
            r.setNoticeId(noticeId);
            r.setReadAt(LocalDateTime.now());
            userNoticeReadMapper.insert(r);
        } catch (Exception ignored) {
            // 表不存在/字段不匹配时不影响主页面展示
        }
    }

    @Override
    public List<Long> getReadNoticeIds(Long userId) {
        if (userId == null) return List.of();
        try {
            QueryWrapper<UserNoticeRead> q = new QueryWrapper<>();
            q.eq("user_id", userId).select("notice_id");
            return userNoticeReadMapper.selectList(q).stream()
                    .map(UserNoticeRead::getNoticeId)
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
            // 表不存在/字段不匹配时，按“全部未读”降级处理
            return List.of();
        }
    }
}
