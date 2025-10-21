package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.AdoptionAppointment;
import com.pethome.service.AdoptionAppointmentService;
import com.pethome.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/adoption-appointments")
@Api(tags = "宠物领养预约管理")
public class AdoptionAppointmentController {

    @Autowired
    private AdoptionAppointmentService adoptionAppointmentService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物领养预约")
    public Result<IPage<AdoptionAppointment>> getAdoptionAppointmentPage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        
        Page<AdoptionAppointment> page = new Page<>(current, size);
        IPage<AdoptionAppointment> result = adoptionAppointmentService.page(page);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @ApiOperation("获取用户宠物领养预约列表")
    public Result<List<AdoptionAppointment>> getUserAdoptionAppointments(@PathVariable Long userId) {
        List<AdoptionAppointment> appointments = adoptionAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物领养预约")
    public Result<String> createAdoptionAppointment(@RequestBody AdoptionAppointment appointment) {
        try {
            boolean success = adoptionAppointmentService.createAdoptionAppointment(appointment);
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
    @ApiOperation("获取宠物领养预约详情")
    public Result<AdoptionAppointment> getAdoptionAppointmentDetail(@PathVariable Long id) {
        AdoptionAppointment appointment = adoptionAppointmentService.getAppointmentById(id);
        if (appointment != null) {
            return Result.success(appointment);
        } else {
            return Result.error("预约不存在");
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新宠物领养预约状态")
    public Result<String> updateAdoptionAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            boolean success = adoptionAppointmentService.updateAppointmentStatus(id, status);
            if (success) {
                return Result.success("状态更新成功");
            } else {
                return Result.error("状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新宠物领养预约")
    public Result<AdoptionAppointment> updateAdoptionAppointment(@PathVariable Long id, @RequestBody AdoptionAppointment appointment) {
        try {
            appointment.setId(id);
            boolean success = adoptionAppointmentService.updateById(appointment);
            if (success) {
                return Result.success("预约更新成功", appointment);
            } else {
                return Result.error("预约更新失败");
            }
        } catch (Exception e) {
            return Result.error("预约更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物领养预约")
    public Result<Boolean> deleteAdoptionAppointment(@PathVariable Long id) {
        try {
            boolean success = adoptionAppointmentService.removeById(id);
            if (success) {
                return Result.success("预约删除成功", true);
            } else {
                return Result.error("预约删除失败");
            }
        } catch (Exception e) {
            return Result.error("预约删除失败: " + e.getMessage());
        }
    }
}

