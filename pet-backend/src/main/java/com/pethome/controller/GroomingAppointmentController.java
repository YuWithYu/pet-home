package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.GroomingAppointment;
import com.pethome.service.GroomingAppointmentService;
import com.pethome.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grooming-appointments")
@Api(tags = "宠物洗护预约管理")
public class GroomingAppointmentController {

    @Autowired
    private GroomingAppointmentService groomingAppointmentService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物洗护预约")
    public Result<IPage<GroomingAppointment>> getGroomingAppointmentPage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        
        Page<GroomingAppointment> page = new Page<>(current, size);
        IPage<GroomingAppointment> result = groomingAppointmentService.page(page);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @ApiOperation("获取用户宠物洗护预约列表")
    public Result<List<GroomingAppointment>> getUserGroomingAppointments(@PathVariable Long userId) {
        List<GroomingAppointment> appointments = groomingAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物洗护预约")
    public Result<String> createGroomingAppointment(@RequestBody GroomingAppointment appointment) {
        try {
            boolean success = groomingAppointmentService.createGroomingAppointment(appointment);
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
    @ApiOperation("获取宠物洗护预约详情")
    public Result<GroomingAppointment> getGroomingAppointmentDetail(@PathVariable Long id) {
        GroomingAppointment appointment = groomingAppointmentService.getAppointmentById(id);
        if (appointment != null) {
            return Result.success(appointment);
        } else {
            return Result.error("预约不存在");
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新宠物洗护预约状态")
    public Result<String> updateGroomingAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            boolean success = groomingAppointmentService.updateAppointmentStatus(id, status);
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
    @ApiOperation("更新宠物洗护预约")
    public Result<GroomingAppointment> updateGroomingAppointment(@PathVariable Long id, @RequestBody GroomingAppointment appointment) {
        try {
            appointment.setId(id);
            boolean success = groomingAppointmentService.updateById(appointment);
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
    @ApiOperation("删除宠物洗护预约")
    public Result<Boolean> deleteGroomingAppointment(@PathVariable Long id) {
        try {
            boolean success = groomingAppointmentService.removeById(id);
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

