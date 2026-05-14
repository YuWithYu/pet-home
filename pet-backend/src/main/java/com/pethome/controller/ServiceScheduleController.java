package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.ServiceSchedule;
import com.pethome.service.AdminService;
import com.pethome.service.ServiceScheduleService;
import com.pethome.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 服务预约时间管理Controller
 */
@RestController
@RequestMapping("/api/schedule")
@Api(tags = "服务预约时间管理")
public class ServiceScheduleController {

    @Autowired
    private ServiceScheduleService scheduleService;

    @Autowired
    private AdminService adminService;
    
    @Autowired(required = false)
    private AdminContext adminContext;

    @GetMapping("/available")
    @ApiOperation("查询指定日期和服务类型的可预约时间段（可选按门店过滤）")
    public Result<List<Map<String, Object>>> getAvailableTimeSlots(
            @RequestParam String serviceType,
            @RequestParam String date,
            @RequestParam(required = false) Long storeId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }
            Long effectiveStoreId = currentServiceStoreId != null ? currentServiceStoreId : storeId;
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            List<Map<String, Object>> timeSlots = scheduleService.getAvailableTimeSlots(serviceType, localDate, effectiveStoreId);
            return Result.success(timeSlots);
        } catch (Exception e) {
            return Result.error("查询可预约时间段失败: " + e.getMessage());
        }
    }

    @GetMapping("/member/{memberId}")
    @ApiOperation("查询服务人员在指定日期的日程")
    public Result<List<ServiceSchedule>> getMemberSchedule(
            @PathVariable Long memberId,
            @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            List<ServiceSchedule> schedules = scheduleService.getMemberSchedule(memberId, localDate);
            return Result.success(schedules);
        } catch (Exception e) {
            return Result.error("查询服务人员日程失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-create")
    @ApiOperation("批量创建服务人员的时间段（排班）")
    public Result<Boolean> batchCreateSchedules(
            @RequestParam Long memberId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestBody List<String> timeSlots) {
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            boolean success = scheduleService.batchCreateSchedules(memberId, start, end, timeSlots);
            return success ? Result.success(true) : Result.error("批量创建时间段失败");
        } catch (Exception e) {
            return Result.error("批量创建时间段失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/calendar")
    @ApiOperation("获取排班日历统计（用于日历视图，可选按门店、服务人员过滤）")
    public Result<Map<String, Integer>> getScheduleCalendar(
            @RequestParam String serviceType,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long memberId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }
            Long effectiveStoreId = currentServiceStoreId != null ? currentServiceStoreId : storeId;
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            Map<String, Integer> calendar = scheduleService.getScheduleCalendar(serviceType, start, end, effectiveStoreId, memberId);
            return Result.success(calendar);
        } catch (Exception e) {
            return Result.error("获取排班日历失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/day")
    @ApiOperation("获取指定日期的排班详情（可选按门店、服务人员过滤）")
    public Result<List<Map<String, Object>>> getDaySchedule(
            @RequestParam String serviceType,
            @RequestParam String date,
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) Long memberId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }
            Long effectiveStoreId = currentServiceStoreId != null ? currentServiceStoreId : storeId;
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            List<Map<String, Object>> schedules = scheduleService.getDaySchedule(serviceType, localDate, effectiveStoreId, memberId);
            return Result.success(schedules);
        } catch (Exception e) {
            return Result.error("获取日期排班失败: " + e.getMessage());
        }
    }

    @GetMapping("/member-calendar")
    @ApiOperation("按服务人员查看月历（所有 service_type 排班计数 + 请假标记）")
    public Result<Map<String, Object>> getMemberCalendar(
            @RequestParam Long memberId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
            Map<String, Object> data = scheduleService.getMemberCalendarView(memberId, start, end);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取人员月历失败: " + e.getMessage());
        }
    }

    @PostMapping("/member-day-attendance")
    @ApiOperation("设置某服务人员某日上班或请假（删除当日排班）")
    public Result<Boolean> setMemberDayAttendance(@RequestBody Map<String, Object> body) {
        try {
            if (body == null || body.get("memberId") == null || body.get("date") == null || body.get("mode") == null) {
                return Result.error("memberId、date、mode 不能为空");
            }
            Long memberId = Long.valueOf(body.get("memberId").toString());
            LocalDate localDate = LocalDate.parse(body.get("date").toString(), DateTimeFormatter.ISO_DATE);
            String mode = body.get("mode").toString();
            boolean ok = scheduleService.setMemberDayAttendance(memberId, localDate, mode);
            return ok ? Result.success(true) : Result.error("设置失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("设置出勤失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @ApiOperation("更新排班")
    public Result<Boolean> updateSchedule(@PathVariable Long id, @RequestBody ServiceSchedule schedule, HttpServletRequest request) {
        try {
            schedule.setId(id);
            Long adminId = (Long) request.getAttribute("adminId");
            if (adminId != null && schedule.getServiceType() == null) {
                Admin admin = adminService.getById(adminId);
                if (admin != null) {
                    String department = admin.getDepartment();
                    String serviceType = convertDepartmentToServiceType(department);
                    if (serviceType != null) {
                        schedule.setServiceType(serviceType);
                    }
                }
            }
            boolean success = scheduleService.updateSchedule(schedule);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新排班失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @ApiOperation("删除排班")
    public Result<Boolean> deleteSchedule(@PathVariable Long id) {
        try {
            boolean success = scheduleService.deleteSchedule(id);
            return success ? Result.success(true) : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error("删除排班失败: " + e.getMessage());
        }
    }

    private String convertDepartmentToServiceType(String department) {
        if (department == null || department.trim().isEmpty()) {
            return null;
        }
        switch (department.trim()) {
            case "上门铲屎":
            case "上门铲屎部门":
                return "door-cleaning";
            case "宠物洗护":
            case "宠物洗护部门":
                return "grooming";
            case "宠物医院":
            case "宠物医院部门":
                return "hospital";
            default:
                return null;
        }
    }
}

