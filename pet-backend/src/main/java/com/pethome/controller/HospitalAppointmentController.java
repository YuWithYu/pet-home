package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.HospitalAppointment;
import com.pethome.service.HospitalAppointmentService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/hospital-appointments")
@Api(tags = "宠物医院预约管理")
public class HospitalAppointmentController {

    @Autowired
    private HospitalAppointmentService hospitalAppointmentService;

    @Autowired
    private com.pethome.service.AdminService adminService;

    @Autowired
    private com.pethome.service.ServiceMemberService serviceMemberService;

    @Autowired
    private com.pethome.service.AppointmentDelayService appointmentDelayService;

    @GetMapping("/page")
    @ApiOperation("分页查询宠物医院预约")
    public Result<IPage<HospitalAppointment>> getHospitalAppointmentPage(
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

        Page<HospitalAppointment> page = new Page<>(current, size);
        IPage<HospitalAppointment> result = hospitalAppointmentService.getAppointmentPage(page, status, keyword, serviceType, storeId);
        return Result.success(result);
    }

    @GetMapping("/user/list/{userId}")
    @ApiOperation("获取用户宠物医院预约列表")
    public Result<List<HospitalAppointment>> getUserHospitalAppointments(@PathVariable Long userId) {
        List<HospitalAppointment> appointments = hospitalAppointmentService.getAppointmentsByUserId(userId);
        return Result.success(appointments);
    }

    @GetMapping("/member/list")
    @ApiOperation("获取服务人员的宠物医院预约列表")
    public Result<List<HospitalAppointment>> getMemberHospitalAppointments(@RequestParam Long memberId) {
        List<HospitalAppointment> appointments = hospitalAppointmentService.getAppointmentsByMemberId(memberId);
        return Result.success(appointments);
    }

    @PostMapping("/create")
    @ApiOperation("创建宠物医院预约")
    public Result<HospitalAppointment> createHospitalAppointment(@RequestBody HospitalAppointment appointment) {
        try {
            if (appointment.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (appointment.getDate() == null) {
                return Result.error("预约日期不能为空");
            }
            if (appointment.getTimeSlot() == null || appointment.getTimeSlot().trim().isEmpty()) {
                return Result.error("预约时间段不能为空");
            }

            if (appointment.getServiceType() == null || appointment.getServiceType().isEmpty()) {
                appointment.setServiceType("hospital");
            }
            boolean success = hospitalAppointmentService.createHospitalAppointment(appointment);
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
    @ApiOperation("获取宠物医院预约详情")
    public Result<HospitalAppointment> getHospitalAppointmentDetail(@PathVariable Long id) {
        HospitalAppointment appointment = hospitalAppointmentService.getAppointmentById(id);
        if (appointment != null) {
            return Result.success(appointment);
        } else {
            return Result.error("预约不存在");
        }
    }

    @PutMapping("/{id}/assign")
    @ApiOperation("分配服务人员到预约")
    public Result<String> assignMember(
            @PathVariable Long id,
            @RequestParam Long memberId) {
        try {
            boolean success = hospitalAppointmentService.assignMember(id, memberId);
            if (success) {
                HospitalAppointment apt = hospitalAppointmentService.getById(id);
                if (apt != null) {
                    java.time.LocalDateTime aptTime = apt.getDate() != null ? apt.getDate().atStartOfDay() : null;
                    appointmentDelayService.registerReminderKeys("hospital", id, apt.getUserId(), aptTime, "医院预约");
                }
                return Result.success("分配成功");
            } else {
                return Result.error("分配失败，请确认服务人员存在且类型为医院");
            }
        } catch (Exception e) {
            return Result.error("分配失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新宠物医院预约状态（拒绝时可传 rejectReason，用户端会展示）")
    public Result<String> updateHospitalAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String rejectReason,
            @RequestParam(required = false, defaultValue = "false") boolean forceCancelAfterServiceStart) {
        try {
            String reason = (rejectReason != null && !rejectReason.trim().isEmpty()) ? rejectReason.trim() : null;
            boolean success = hospitalAppointmentService.updateAppointmentStatus(id, status, reason, forceCancelAfterServiceStart);
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
