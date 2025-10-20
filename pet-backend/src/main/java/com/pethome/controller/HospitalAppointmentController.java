package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.HospitalAppointment;
import com.pethome.service.HospitalAppointmentService;
import com.pethome.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital-appointments")
@Api(tags = "宠物医院预约管理")
public class HospitalAppointmentController {

    @Autowired
    private HospitalAppointmentService hospitalAppointmentService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物医院预约")
    public Result<IPage<HospitalAppointment>> getHospitalAppointmentPage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        
        Page<HospitalAppointment> page = new Page<>(current, size);
        IPage<HospitalAppointment> result = hospitalAppointmentService.page(page);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @ApiOperation("获取用户宠物医院预约列表")
    public Result<List<HospitalAppointment>> getUserHospitalAppointments(@PathVariable Long userId) {
        List<HospitalAppointment> appointments = hospitalAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物医院预约")
    public Result<String> createHospitalAppointment(@RequestBody HospitalAppointment appointment) {
        try {
            boolean success = hospitalAppointmentService.createHospitalAppointment(appointment);
            if (success) {
                return Result.success("预约创建成功");
            } else {
                return Result.error("预约创建失败");
            }
        } catch (Exception e) {
            return Result.error("预约创建失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("获取宠物医院预约详情")
    public Result<HospitalAppointment> getHospitalAppointmentDetail(@PathVariable Long id) {
        HospitalAppointment appointment = hospitalAppointmentService.getAppointmentById(id);
        if (appointment != null) {
            return Result.success(appointment);
        } else {
            return Result.error("预约不存在");
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新宠物医院预约状态")
    public Result<String> updateHospitalAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            boolean success = hospitalAppointmentService.updateAppointmentStatus(id, status);
            if (success) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }
}
