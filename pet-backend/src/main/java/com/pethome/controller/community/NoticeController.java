package com.pethome.controller.community;


import com.pethome.common.SafeCall;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Notice;
import com.pethome.security.RequestIdentity;
import com.pethome.service.community.NoticeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        // 安全修复：分页接口包含未发布/已下线公告，属管理端功能，不能匿名可读
        if (!RequestIdentity.isAdmin(request)) {
            return Result.error(403, "无权查看公告列表");
        }
        return com.pethome.common.SafeCall.of(() -> {
            Page<Notice> page = new Page<>(pageNo, pageSize);
            IPage<Notice> result = noticeService.getNoticePage(page, keyword, type, status);
            return Result.success(result);
        }, "查询公告列表失败，请稍后重试", log);
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
        return com.pethome.common.SafeCall.of(() -> {
            List<Notice> list = noticeService.getNoticeList(type, status, limit);
            return Result.success(list);
        }, "获取公告列表失败，请稍后重试", log);
    }

    /**
     * 获取最新一条公告（对外展示用）
     */
    @GetMapping("/last-one")
    @Operation(summary = "获取最新公告")
    public Result<Map<String, Object>> getLastNotice(@RequestParam(required = false) String type) {
        return com.pethome.common.SafeCall.of(() -> {
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
        }, "获取公告失败，请稍后重试", log);
    }

    /**
     * 创建公告
     */
    @PostMapping("/create")
    @Operation(summary = "创建公告")
    public Result<Notice> createNotice(@RequestBody Notice notice, HttpServletRequest request) {
        // 安全修复：公告为平台级内容，此前任意登录用户即可发布（钓鱼/数据篡改）
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权创建公告");
        }
        return com.pethome.common.SafeCall.of(() -> {
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
        }, "创建公告失败，请稍后重试", log);
    }

    /**
     * 更新公告
     */
    @PutMapping("/update")
    @Operation(summary = "更新公告")
    public Result<Notice> updateNotice(@RequestBody Notice notice, HttpServletRequest request) {
        // 安全修复：公告为平台级内容，仅平台管理员可修改
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权修改公告");
        }
        return com.pethome.common.SafeCall.of(() -> {
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
        }, "更新公告失败，请稍后重试", log);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除公告")
    public Result<Boolean> deleteNotice(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：公告为平台级内容，仅平台管理员可删除
        if (!RequestIdentity.isPlatformAdmin(request)) {
            return Result.error(403, "无权删除公告");
        }
        return com.pethome.common.SafeCall.of(() -> {
            Notice existing = noticeService.getById(id);
            if (existing == null) {
                return Result.error("公告不存在");
            }
            boolean success = noticeService.deleteNotice(id);
            return success ? Result.success(true) : Result.error("删除公告失败");
        }, "删除公告失败，请稍后重试", log);
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情")
    public Result<Notice> getNoticeDetail(@PathVariable Long id) {
        return com.pethome.common.SafeCall.of(() -> {
            Notice notice = noticeService.getById(id);
            if (notice == null) {
                return Result.error("公告不存在");
            }
            return Result.success(notice);
        }, "获取公告详情失败，请稍后重试", log);
    }
}

