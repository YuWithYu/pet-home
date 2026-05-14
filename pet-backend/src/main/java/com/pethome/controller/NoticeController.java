package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Notice;
import com.pethome.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/notice")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "公告管理")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 后台分页查询公告列表
     */
    @GetMapping("/page")
    @ApiOperation("分页查询公告")
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
            return Result.error("查询公告列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告列表（常用于首页展示）
     */
    @GetMapping("/list")
    @ApiOperation("获取公告列表")
    public Result<List<Notice>> getNoticeList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        try {
            List<Notice> list = noticeService.getNoticeList(type, status, limit);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取公告列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新一条公告（对外展示用）
     */
    @GetMapping("/last-one")
    @ApiOperation("获取最新公告")
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
            return Result.error("获取公告失败: " + e.getMessage());
        }
    }

    /**
     * 创建公告
     */
    @PostMapping("/create")
    @ApiOperation("创建公告")
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
            return Result.error("创建公告失败: " + e.getMessage());
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/update")
    @ApiOperation("更新公告")
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
            return Result.error("更新公告失败: " + e.getMessage());
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除公告")
    public Result<Boolean> deleteNotice(@PathVariable Long id) {
        try {
            Notice existing = noticeService.getById(id);
            if (existing == null) {
                return Result.error("公告不存在");
            }
            boolean success = noticeService.deleteNotice(id);
            return success ? Result.success(true) : Result.error("删除公告失败");
        } catch (Exception e) {
            return Result.error("删除公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取公告详情")
    public Result<Notice> getNoticeDetail(@PathVariable Long id) {
        try {
            Notice notice = noticeService.getById(id);
            if (notice == null) {
                return Result.error("公告不存在");
            }
            return Result.success(notice);
        } catch (Exception e) {
            return Result.error("获取公告详情失败: " + e.getMessage());
        }
    }
}

