package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.DailyTopicTheme;

import java.util.List;

public interface DailyTopicThemeService extends IService<DailyTopicTheme> {
    /** 获取所有启用的主题（按 sort_order），供小程序 Tab 与后台下拉使用 */
    List<DailyTopicTheme> listEnabled();
}
