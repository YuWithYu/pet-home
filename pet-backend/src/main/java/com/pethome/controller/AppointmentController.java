package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Appointment;
import com.pethome.service.AppointmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointment")
@Api(tags = "宠物医院预约管理")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/page")
    @ApiOperation("分页查询预约")
    public Result<IPage<Appointment>> getAppointmentPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Appointment> page = new Page<>(pageNo, pageSize);
        IPage<Appointment> result = appointmentService.getAppointmentList(page);
        return Result.success(result);
    }

    @PostMapping("/create")
    @ApiOperation("创建预约")
    public Result<Appointment> createAppointment(@RequestBody Appointment appointment) {
        return Result.success(appointmentService.createAppointment(appointment));
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新预约状态")
    public Result<Appointment> updateAppointmentStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(appointmentService.updateAppointmentStatus(id, status));
    }

    @GetMapping("/user/list/{userId}")
    @ApiOperation("获取用户预约列表")
    public Result<java.util.List<Appointment>> getUserAppointments(@PathVariable Long userId) {
        try {
            // 这里应该从数据库查询用户的预约列表
            return Result.success(new java.util.ArrayList<Appointment>());
        } catch (Exception e) {
            return Result.error("获取预约列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取预约详情")
    public Result<Appointment> getAppointmentDetail(@PathVariable Long id) {
        return Result.success(appointmentService.getAppointmentById(id));
    }
}
