package com.pethome.controller.appointment;


import com.pethome.common.BusinessException;
import com.pethome.common.SafeCall;
import com.pethome.common.Result;
import com.pethome.entity.ServiceSchedule;
import com.pethome.service.appointment.ServiceScheduleService;
import com.pethome.security.AdminContext;
import com.pethome.security.RequestIdentity;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 服务预约时间管理Controller
 */
@RestController
@RequestMapping("/api/schedule")
@Tag(name = "服务预约时间管理")
public class ServiceScheduleController {

    private static final Logger log = LoggerFactory.getLogger(ServiceScheduleController.class);

    @Autowired
    private ServiceScheduleService scheduleService;

    @Autowired
    private com.pethome.service.appointment.ServiceMemberService serviceMemberService;
    
    @Autowired(required = false)
    private AdminContext adminContext;

    @GetMapping("/available")
    @Operation(summary = "查询指定日期和服务类型的可预约时间段（可选按门店过滤）")
    public Result<List<Map<String, Object>>> getAvailableTimeSlots(
            @RequestParam String serviceType,
            @RequestParam String date,
            @RequestParam(required = false) Long storeId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }
            Long effectiveStoreId = currentServiceStoreId != null ? currentServiceStoreId : storeId;
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            List<Map<String, Object>> timeSlots = scheduleService.getAvailableTimeSlots(serviceType, localDate, effectiveStoreId);
            return Result.success(timeSlots);
        }, "查询可预约时间段失败，请稍后重试", log);
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "查询服务人员在指定日期的日程")
    public Result<List<ServiceSchedule>> getMemberSchedule(
            @PathVariable Long memberId,
            @RequestParam String date,
            HttpServletRequest request) {
        // 安全修复：排班属门店运营数据，且含员工维度信息，仅 admin/staff 且限本门店
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权查看服务人员日程");
        }
        if (!canManageMember(request, memberId)) {
            throw new BusinessException(403, "无权查看其他门店的服务人员日程");
        }
        return SafeCall.of(() -> {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            List<ServiceSchedule> schedules = scheduleService.getMemberSchedule(memberId, localDate);
            return Result.success(schedules);
        }, "查询服务人员日程失败，请稍后重试", log);
    }

    @PostMapping("/batch-create")
    @Operation(summary = "批量创建服务人员的时间段（排班）")
    public Result<Boolean> batchCreateSchedules(
            @RequestParam Long memberId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestBody List<String> timeSlots,
            HttpServletRequest request) {
        // 安全修复：此前任意登录用户即可为任意服务人员批量创建排班
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权管理排班");
        }
        if (!canManageMember(request, memberId)) {
            throw new BusinessException(403, "无权管理其他门店服务人员的排班");
        }
        return SafeCall.of(() -> {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            if (!scheduleService.batchCreateSchedules(memberId, start, end, timeSlots)) {
                throw new BusinessException("批量创建时间段失败");
            }
            return Result.success(true);
        }, "批量创建时间段失败，请稍后重试", log);
    }
    
    @GetMapping("/calendar")
    @Operation(summary = "获取排班日历统计（用于日历视图，可选按门店、服务人员过滤）")
    public Result<Map<String, Integer>> getScheduleCalendar(
            @RequestParam String serviceType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long memberId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }
            Long effectiveStoreId = currentServiceStoreId != null ? currentServiceStoreId : storeId;
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            Map<String, Integer> calendar = scheduleService.getScheduleCalendar(serviceType, start, end, effectiveStoreId, memberId);
            return Result.success(calendar);
        }, "获取排班日历失败，请稍后重试", log);
    }
    
    @GetMapping("/day")
    @Operation(summary = "获取指定日期的排班详情（可选按门店、服务人员过滤）")
    public Result<List<Map<String, Object>>> getDaySchedule(
            @RequestParam String serviceType,
            @RequestParam String date,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long memberId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        return SafeCall.of(() -> {
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }
            Long effectiveStoreId = currentServiceStoreId != null ? currentServiceStoreId : storeId;
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            List<Map<String, Object>> schedules = scheduleService.getDaySchedule(serviceType, localDate, effectiveStoreId, memberId);
            return Result.success(schedules);
        }, "获取日期排班失败，请稍后重试", log);
    }

    @GetMapping("/member-calendar")
    @Operation(summary = "按服务人员查看月历（所有 service_type 排班计数 + 请假标记）")
    public Result<Map<String, Object>> getMemberCalendar(
            @RequestParam Long memberId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpServletRequest request) {
        // 安全修复：员工月历属门店运营数据，仅 admin/staff 且限本门店
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权查看服务人员月历");
        }
        if (!canManageMember(request, memberId)) {
            throw new BusinessException(403, "无权查看其他门店的服务人员月历");
        }
        return SafeCall.of(() -> {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            Map<String, Object> data = scheduleService.getMemberCalendarView(memberId, start, end);
            return Result.success(data);
        }, "获取人员月历失败，请稍后重试", log);
    }

    @PostMapping("/member-day-attendance")
    @Operation(summary = "设置某服务人员某日上班或请假（删除当日排班）")
    public Result<Boolean> setMemberDayAttendance(@RequestBody Map<String, Object> body,
                                                HttpServletRequest request) {
        // 安全修复：此前任意登录用户即可为他人生成/删除排班
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权管理排班");
        }
        if (body == null || body.get("memberId") == null || body.get("date") == null || body.get("mode") == null) {
            throw new BusinessException("memberId、date、mode 不能为空");
        }
        Long memberId = Long.valueOf(body.get("memberId").toString());
        // 安全修复：店长/店员仅能维护本门店服务人员的出勤
        if (!canManageMember(request, memberId)) {
            throw new BusinessException(403, "无权管理其他门店服务人员的出勤");
        }
        return SafeCall.of(() -> {
            LocalDate localDate = LocalDate.parse(body.get("date").toString(), DateTimeFormatter.ISO_DATE);
            String mode = body.get("mode").toString();
            if (!scheduleService.setMemberDayAttendance(memberId, localDate, mode)) {
                throw new BusinessException("设置失败");
            }
            return Result.success(true);
        }, "设置出勤失败，请稍后重试", log);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新排班")
    public Result<Boolean> updateSchedule(@PathVariable Long id, @RequestBody ServiceSchedule schedule, HttpServletRequest request) {
        // 安全修复：此前任意登录用户即可修改任意排班
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权管理排班");
        }
        ServiceSchedule existing = scheduleService.getScheduleById(id);
        if (existing == null) {
            throw new BusinessException("排班不存在");
        }
        // 安全修复：店长/店员仅能修改本门店服务人员的排班
        if (!canManageMember(request, existing.getMemberId())) {
            throw new BusinessException(403, "无权修改其他门店的排班");
        }
        return SafeCall.of(() -> {
            schedule.setId(id);
            // 未指定服务类型时由 Service 按操作人所属部门回填
            if (!scheduleService.updateSchedule(schedule, RequestIdentity.adminId(request))) {
                throw new BusinessException("更新失败");
            }
            return Result.success(true);
        }, "更新排班失败，请稍后重试", log);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除排班")
    public Result<Boolean> deleteSchedule(@PathVariable Long id, HttpServletRequest request) {
        // 安全修复：此前任意登录用户即可删除任意排班
        if (!RequestIdentity.isAdminOrStaff(request)) {
            throw new BusinessException(403, "无权管理排班");
        }
        ServiceSchedule existing = scheduleService.getScheduleById(id);
        if (existing == null) {
            throw new BusinessException("排班不存在");
        }
        // 安全修复：店长/店员仅能删除本门店服务人员的排班
        if (!canManageMember(request, existing.getMemberId())) {
            throw new BusinessException(403, "无权删除其他门店的排班");
        }
        return SafeCall.of(() -> {
            if (!scheduleService.deleteSchedule(id)) {
                throw new BusinessException("删除失败");
            }
            return Result.success(true);
        }, "删除排班失败，请稍后重试", log);
    }

    /**
     * 门店维度：平台管理员返回 null（不限门店）；
     * 店长/店员返回其服务门店（serviceStoreId，缺失时回落到员工档案的门店），无归属返回 -1。
     */
    private Long scopedStoreId(HttpServletRequest request) {
        if (RequestIdentity.isPlatformAdmin(request)) {
            return null;
        }
        Long storeId = RequestIdentity.serviceStoreId(request);
        if (storeId != null) {
            return storeId;
        }
        Long adminId = RequestIdentity.adminId(request);
        if (adminId != null && serviceMemberService != null) {
            com.pethome.entity.ServiceMember member = serviceMemberService.getMemberByUserId(adminId);
            if (member != null && member.getStoreId() != null) {
                return member.getStoreId();
            }
        }
        return -1L;
    }

    /** 服务人员归属校验：平台管理员放行，其他人必须与员工所属门店一致 */
    private boolean canManageMember(HttpServletRequest request, Long memberId) {
        Long scope = scopedStoreId(request);
        if (scope == null) {
            return true;
        }
        if (memberId == null || serviceMemberService == null) {
            return false;
        }
        com.pethome.entity.ServiceMember member = serviceMemberService.getMemberById(memberId);
        return member != null && scope.equals(member.getStoreId());
    }
}
