package com.pethome.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.GroomingAppointment;
import com.pethome.service.GroomingAppointmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/grooming-appointments")
@Api(tags = "宠物洗护预约管理")
public class GroomingAppointmentController {

    @Autowired
    private GroomingAppointmentService groomingAppointmentService;

    @Autowired
    private com.pethome.service.AdminService adminService;

    @Autowired
    private com.pethome.service.ServiceMemberService serviceMemberService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物洗护预约")
    public Result<IPage<GroomingAppointment>> getGroomingAppointmentPage(
            @ApiParam("当前页") @RequestParam(defaultValue = "1") Integer current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam("状态") @RequestParam(required = false) String status,
            @ApiParam("关键字") @RequestParam(required = false) String keyword,
            @ApiParam("服务类型") @RequestParam(required = false) String serviceType,
            @ApiParam("门店ID") @RequestParam(required = false) Long storeId,
            HttpServletRequest request) {

        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId != null && !"admin".equalsIgnoreCase((String) request.getAttribute("role"))) {
            com.pethome.entity.Admin admin = adminService.getById(adminId);
            if (admin != null) {
                Long effectiveStoreId = admin.getServiceStoreId();
                if (effectiveStoreId == null && serviceMemberService != null) {
                    com.pethome.entity.ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
                    if (sm != null && sm.getStoreId() != null) {
                        effectiveStoreId = sm.getStoreId();
                    }
                }
                if (effectiveStoreId != null) {
                    storeId = effectiveStoreId;
                } else {
                    storeId = -1L;
                }
            }
        }

        Page<GroomingAppointment> page = new Page<>(current, size);
        IPage<GroomingAppointment> result = groomingAppointmentService.getAppointmentPage(page, status, keyword, serviceType, storeId);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @ApiOperation("获取用户宠物洗护预约列表")
    public Result<List<GroomingAppointment>> getUserGroomingAppointments(@PathVariable Long userId) {
        List<GroomingAppointment> appointments = groomingAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @GetMapping("/member/list")
    @ApiOperation("获取服务人员的洗护预约列表")
    public Result<List<GroomingAppointment>> getMemberGroomingAppointments(@RequestParam Long memberId) {
        List<GroomingAppointment> appointments = groomingAppointmentService.getAppointmentsByMemberId(memberId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物洗护预约")
    public Result<GroomingAppointment> createGroomingAppointment(@RequestBody GroomingAppointment appointment) {
        try {
            boolean success = groomingAppointmentService.createGroomingAppointment(appointment);
            if (success) {
                return Result.success(appointment);
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
    @ApiOperation("更新宠物洗护预约状态（拒绝时可传 rejectReason，用户端会展示）")
    public Result<String> updateGroomingAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectReason,
            @RequestParam(required = false, defaultValue = "false") boolean forceCancelAfterServiceStart) {
        try {
            String reason = (rejectReason != null && !rejectReason.trim().isEmpty()) ? rejectReason.trim() : null;
            boolean success = groomingAppointmentService.updateAppointmentStatus(id, status, reason, forceCancelAfterServiceStart);
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

