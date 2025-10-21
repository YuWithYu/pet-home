package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.BoardingAppointment;
import com.pethome.service.BoardingAppointmentService;
import com.pethome.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boarding-appointments")
@Api(tags = "宠物寄养预约管理")
public class BoardingAppointmentController {

    @Autowired
    private BoardingAppointmentService boardingAppointmentService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物寄养预约")
    public Result<IPage<BoardingAppointment>> getBoardingAppointmentPage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态") @RequestParam(required = false) String status) {
        
        Page<BoardingAppointment> page = new Page<>(current, size);
        IPage<BoardingAppointment> result = boardingAppointmentService.page(page);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @ApiOperation("获取用户宠物寄养预约列表")
    public Result<List<BoardingAppointment>> getUserBoardingAppointments(@PathVariable Long userId) {
        List<BoardingAppointment> appointments = boardingAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物寄养预约")
    public Result<String> createBoardingAppointment(@RequestBody BoardingAppointment appointment) {
        try {
            boolean success = boardingAppointmentService.createBoardingAppointment(appointment);
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
    @ApiOperation("获取宠物寄养预约详情")
    public Result<BoardingAppointment> getBoardingAppointmentDetail(@PathVariable Long id) {
        BoardingAppointment appointment = boardingAppointmentService.getAppointmentById(id);
        if (appointment != null) {
            return Result.success(appointment);
        } else {
            return Result.error("预约不存在");
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新宠物寄养预约状态")
    public Result<String> updateBoardingAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            boolean success = boardingAppointmentService.updateAppointmentStatus(id, status);
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
    @ApiOperation("更新宠物寄养预约")
    public Result<BoardingAppointment> updateBoardingAppointment(@PathVariable Long id, @RequestBody BoardingAppointment appointment) {
        try {
            appointment.setId(id);
            boolean success = boardingAppointmentService.updateById(appointment);
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
    @ApiOperation("删除宠物寄养预约")
    public Result<Boolean> deleteBoardingAppointment(@PathVariable Long id) {
        try {
            boolean success = boardingAppointmentService.removeById(id);
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

