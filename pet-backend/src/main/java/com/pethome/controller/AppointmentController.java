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
@RequestMapping("/api/door-cleaning")
@Api(tags = "上门铲屎服务预约管理")
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

    @PostMapping("/list")
    @ApiOperation("获取预约列表（支持POST请求）")
    public Result<IPage<Appointment>> getAppointmentList(@RequestBody(required = false) java.util.Map<String, Object> params) {
        try {
            // 从请求参数中获取分页信息，如果没有则使用默认值
            Integer pageNo = params != null && params.containsKey("pageNo") ? 
                Integer.valueOf(params.get("pageNo").toString()) : 1;
            Integer pageSize = params != null && params.containsKey("pageSize") ? 
                Integer.valueOf(params.get("pageSize").toString()) : 10;
            
            Page<Appointment> page = new Page<>(pageNo, pageSize);
            IPage<Appointment> result = appointmentService.getAppointmentList(page);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取预约列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/create")
    @ApiOperation("创建预约")
    public Result<Appointment> createAppointment(@RequestBody Appointment appointment) {
        try {
            Appointment result = appointmentService.createAppointment(appointment);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("创建预约失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新预约信息")
    public Result<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        try {
            appointment.setId(id);
            Appointment result = appointmentService.updateAppointment(appointment);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新预约失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新预约状态")
    public Result<Appointment> updateAppointmentStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Appointment result = appointmentService.updateAppointmentStatus(id, status);
            if (result != null) {
                return Result.success(result);
            } else {
                return Result.error("预约不存在");
            }
        } catch (Exception e) {
            return Result.error("更新预约状态失败: " + e.getMessage());
        }
    }

    @GetMapping("/user/list/{userId}")
    @ApiOperation("获取用户预约列表")
    public Result<java.util.List<Appointment>> getUserAppointments(@PathVariable Long userId) {
        try {
            java.util.List<Appointment> appointments = appointmentService.getUserAppointments(userId);
            return Result.success(appointments);
        } catch (Exception e) {
            return Result.error("获取预约列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/list")
    @ApiOperation("根据用户ID和服务类型获取预约列表")
    public Result<java.util.List<Appointment>> getUserAppointmentsByType(
            @RequestParam Long userId,
            @RequestParam(required = false) String serviceType) {
        try {
            java.util.List<Appointment> appointments = appointmentService.getUserAppointments(userId);
            
            // 如果指定了服务类型，进行过滤
            if (serviceType != null && !serviceType.isEmpty()) {
                appointments = appointments.stream()
                        .filter(a -> serviceType.equals(a.getServiceType()))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            return Result.success(appointments);
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
