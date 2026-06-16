package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Notification;
import com.pethome.entity.Notice;
import com.pethome.entity.User;
import com.pethome.service.NotificationService;
import com.pethome.service.NoticeService;
import com.pethome.service.UserNoticeReadService;
import com.pethome.service.UserService;
import com.pethome.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Api(tags = "通知管理")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired(required = false)
    private NoticeService noticeService;
    @Autowired(required = false)
    private UserNoticeReadService userNoticeReadService;
    @Autowired(required = false)
    private UserService userService;
    @Autowired(required = false)
    private JwtUtil jwtUtil;

    /** C 端：我的通知列表（分页） */
    @GetMapping("/my")
    @ApiOperation("我的通知列表")
    public Result<Map<String, Object>> getMyNotifications(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        IPage<Notification> page = notificationService.listByUserId(userId, pageNo, pageSize);
        int unreadCount = notificationService.countUnreadByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("unreadCount", unreadCount);
        return Result.success(data);
    }

    /** C 端：未读数量 */
    @GetMapping("/unread-count")
    @ApiOperation("未读通知数量")
    public Result<Map<String, Object>> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) {
            return Result.success(Map.of("count", 0));
        }
        int count = notificationService.countUnreadByUserId(userId);
        return Result.success(Map.of("count", count));
    }

    /** C 端：标记一条已读 */
    @PostMapping("/{id}/read")
    @ApiOperation("标记通知已读")
    public Result<Boolean> markAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) return Result.error(401, "请先登录");
        boolean ok = notificationService.markAsRead(id, userId);
        return Result.success(ok);
    }

    /** C 端：全部标为已读 */
    @PostMapping("/read-all")
    @ApiOperation("全部标为已读")
    public Result<Integer> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) return Result.error(401, "请先登录");
        int n = notificationService.markAllAsRead(userId);
        return Result.success(n);
    }

    /** C 端：未读的系统公告列表（用于通知中心合并展示） */
    @GetMapping("/notices-unread")
    @ApiOperation("未读系统公告列表")
    public Result<List<Notice>> getUnreadNotices(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = getUserIdFromToken(authHeader);
        if (noticeService == null) return Result.success(List.of());
        List<Notice> published = noticeService.getNoticeList(null, 1, limit == null ? 20 : limit);
        if (userId == null || userNoticeReadService == null) {
            return Result.success(published);
        }
        List<Long> readIds;
        try {
            readIds = userNoticeReadService.getReadNoticeIds(userId);
        } catch (Exception ignored) {
            // 兼容表缺失等异常：直接把所有公告当作未读
            readIds = List.of();
        }
        if (readIds.isEmpty()) return Result.success(published);
        // 避免在 lambda 中引用“可能在 try/catch 中被重新赋值”的局部变量 readIds
        List<Notice> unread = new java.util.ArrayList<>();
        for (Notice n : published) {
            if (n != null && n.getId() != null && !readIds.contains(n.getId())) {
                unread.add(n);
            }
        }
        return Result.success(unread);
    }

    /** C 端：标记某条公告已读 */
    @PostMapping("/notice/{noticeId}/read")
    @ApiOperation("标记公告已读")
    public Result<Void> markNoticeAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long noticeId) {
        Long userId = getUserIdFromToken(authHeader);
        if (userId == null) return Result.error(401, "请先登录");
        if (userNoticeReadService != null) {
            userNoticeReadService.markAsRead(userId, noticeId);
        }
        return Result.success(null);
    }

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.substring(7);
        if (userService != null) {
            try {
                User u = userService.getUserByToken(token);
                if (u != null) return u.getId();
            } catch (Exception ignored) {}
        }
        if (jwtUtil != null) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                if (claims != null && claims.getSubject() != null) {
                    User u = userService.getUserByUsername(claims.getSubject());
                    if (u == null && userService != null) u = userService.getUserByPhone(claims.getSubject());
                    if (u != null) return u.getId();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    @GetMapping("/page")
    @ApiOperation("分页查询通知")
    public Result<IPage<Notification>> getNotificationPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Notification> page = new Page<>(pageNo, pageSize);
        IPage<Notification> result = notificationService.getNotificationList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建通知")
    public Result<Notification> createNotification(@RequestBody Notification notification) {
        return Result.success(notificationService.createNotification(notification));
    }

    @PutMapping("/update")
    @ApiOperation("更新通知")
    public Result<Notification> updateNotification(@RequestBody Notification notification) {
        return Result.success(notificationService.updateNotification(notification));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除通知")
    public Result<Boolean> deleteNotification(@PathVariable Long id) {
        return Result.success(notificationService.deleteNotification(id));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取通知详情")
    public Result<Notification> getNotificationDetail(@PathVariable Long id) {
        return Result.success(notificationService.getNotificationById(id));
    }
}


