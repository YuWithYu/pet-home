package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Notification;
import com.pethome.service.NotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Api(tags = "通知管理")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

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


