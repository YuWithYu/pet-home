package com.pethome.controller.community;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Notice;
import com.pethome.service.community.NoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/notice")
@Tag(name = "公告管理")
public class NoticeController {

    private static final Logger log = LoggerFactory.getLogger(NoticeController.class);

    @Autowired
    private NoticeService noticeService;

    /**
     * 后台分页查询公告列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询公告")
    public Result<IPage<Notice>> getNoticePage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        try {
            Page<Notice> page = new Page<>(pageNo, pageSize);
            IPage<Notice> result = noticeService.getNoticePage(page, keyword, type, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询公告列表失败", e);
            return Result.error("查询公告列表失败，请稍后重试");
        }
    }

    /**
     * 获取公告列表（常用于首页展示）
     */
    @GetMapping("/list")
    @Operation(summary = "获取公告列表")
    public Result<List<Notice>> getNoticeList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        try {
            List<Notice> list = noticeService.getNoticeList(type, status, limit);
            return Result.success(list);
        } catch (Exception e) {
            log.error("获取公告列表失败", e);
            return Result.error("获取公告列表失败，请稍后重试");
        }
    }

    /**
     * 获取最新一条公告（对外展示用）
     */
    @GetMapping("/last-one")
    @Operation(summary = "获取最新公告")
    public Result<Map<String, Object>> getLastNotice(@RequestParam(required = false) String type) {
        try {
            Notice latest = noticeService.getLatestNotice(type);
            if (latest == null) {
                return Result.error("暂无有效公告");
            }

            Map<String, Object> notice = new HashMap<>();
            notice.put("id", latest.getId());
            notice.put("title", latest.getTitle());
            notice.put("content", latest.getContent());
            notice.put("type", latest.getType());
            notice.put("status", latest.getStatus());
            notice.put("isTop", latest.getIsTop());
            notice.put("effectiveTime", latest.getEffectiveTime());
            notice.put("expireTime", latest.getExpireTime());
            notice.put("createTime", latest.getCreateTime());
            notice.put("updateTime", latest.getUpdateTime());

            return Result.success(notice);
        } catch (Exception e) {
            log.error("获取公告失败", e);
            return Result.error("获取公告失败，请稍后重试");
        }
    }

    /**
     * 创建公告
     */
    @PostMapping("/create")
    @Operation(summary = "创建公告")
    public Result<Notice> createNotice(@RequestBody Notice notice) {
        try {
            if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
                return Result.error("公告标题不能为空");
            }
            if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
                return Result.error("公告内容不能为空");
            }
            if (notice.getType() == null || notice.getType().trim().isEmpty()) {
                notice.setType("system");
            }
            Notice created = noticeService.createNotice(notice);
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建公告失败", e);
            return Result.error("创建公告失败，请稍后重试");
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/update")
    @Operation(summary = "更新公告")
    public Result<Notice> updateNotice(@RequestBody Notice notice) {
        try {
            if (notice.getId() == null) {
                return Result.error("ID不能为空");
            }
            Notice existing = noticeService.getById(notice.getId());
            if (existing == null) {
                return Result.error("公告不存在");
            }

            // 仅更新非空字段，避免把前端未传的字段覆盖为null
            if (notice.getTitle() != null) {
                existing.setTitle(notice.getTitle());
            }
            if (notice.getContent() != null) {
                existing.setContent(notice.getContent());
            }
            if (notice.getType() != null) {
                existing.setType(notice.getType());
            }
            if (notice.getStatus() != null) {
                existing.setStatus(notice.getStatus());
            }
            if (notice.getIsTop() != null) {
                existing.setIsTop(notice.getIsTop());
            }
            if (notice.getEffectiveTime() != null) {
                existing.setEffectiveTime(notice.getEffectiveTime());
            }
            if (notice.getExpireTime() != null) {
                existing.setExpireTime(notice.getExpireTime());
            }
            if (notice.getSortOrder() != null) {
                existing.setSortOrder(notice.getSortOrder());
            }

            Notice updated = noticeService.updateNotice(existing);
            return Result.success(updated);
        } catch (Exception e) {
            log.error("更新公告失败", e);
            return Result.error("更新公告失败，请稍后重试");
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public Result<Boolean> deleteNotice(@PathVariable Long id) {
        try {
            Notice existing = noticeService.getById(id);
            if (existing == null) {
                return Result.error("公告不存在");
            }
            boolean success = noticeService.deleteNotice(id);
            return success ? Result.success(true) : Result.error("删除公告失败");
        } catch (Exception e) {
            log.error("删除公告失败", e);
            return Result.error("删除公告失败，请稍后重试");
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情")
    public Result<Notice> getNoticeDetail(@PathVariable Long id) {
        try {
            Notice notice = noticeService.getById(id);
            if (notice == null) {
                return Result.error("公告不存在");
            }
            return Result.success(notice);
        } catch (Exception e) {
            log.error("获取公告详情失败", e);
            return Result.error("获取公告详情失败，请稍后重试");
        }
    }
}

