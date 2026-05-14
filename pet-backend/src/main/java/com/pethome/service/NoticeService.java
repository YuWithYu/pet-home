package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Notice;

import java.util.List;

/**
 * 公告服务接口
 */
public interface NoticeService {

    /**
     * 分页查询公告
     */
    IPage<Notice> getNoticePage(Page<Notice> page, String keyword, String type, Integer status);

    /**
     * 获取公告列表（用于首页等场景）
     */
    List<Notice> getNoticeList(String type, Integer status, Integer limit);

    /**
     * 获取最新一条有效公告
     */
    Notice getLatestNotice(String type);

    /**
     * 创建公告
     */
    Notice createNotice(Notice notice);

    /**
     * 更新公告
     */
    Notice updateNotice(Notice notice);

    /**
     * 删除公告
     */
    boolean deleteNotice(Long id);

    /**
     * 根据ID获取公告
     */
    Notice getById(Long id);
}

