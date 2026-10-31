package com.pethome.service.community.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.DailyTopicTheme;
import com.pethome.mapper.DailyTopicThemeMapper;
import com.pethome.service.community.DailyTopicThemeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DailyTopicThemeServiceImpl extends ServiceImpl<DailyTopicThemeMapper, DailyTopicTheme> implements DailyTopicThemeService {

    private static final Logger log = LoggerFactory.getLogger(DailyTopicThemeServiceImpl.class);

    @Override
    public List<DailyTopicTheme> listEnabled() {
        try {
            QueryWrapper<DailyTopicTheme> q = new QueryWrapper<>();
            q.eq("status", 1).orderByAsc("sort_order");
            return list(q);
        } catch (Exception e) {
            // 表可能尚未创建：为了避免前端页面被 500 卡死，降级为空列表
            log.error("获取每日话题主题列表失败", e);
            return List.of();
        }
    }
}
