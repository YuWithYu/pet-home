package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.Admin;
import com.pethome.entity.Appointment;
import com.pethome.service.AdminService;
import com.pethome.service.AppointmentService;
import com.pethome.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一预约管理控制器
 * 支持管理员和服务人员操作
 */
@RestController
@RequestMapping("/api/appointment")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "预约管理")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.pethome.service.AppointmentDelayService appointmentDelayService;
    
    /**
     * 添加预约（小程序端用户）
     */
    @PostMapping("/add")
    @ApiOperation("添加预约")
    public Result<Appointment> addAppointment(@RequestBody Appointment appointment,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 验证必填字段
            if (appointment.getServiceType() == null || appointment.getServiceType().trim().isEmpty()) {
                return Result.error("服务类型不能为空");
            }
            if (appointment.getDate() == null) {
                return Result.error("预约日期不能为空");
            }
            if (appointment.getTimeSlot() == null || appointment.getTimeSlot().trim().isEmpty()) {
                return Result.error("时间段不能为空");
            }
            // 验证 userId
            if (appointment.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 检查时间段是否可用
            boolean available = appointmentService.checkTimeSlotAvailable(
                appointment.getServiceType(),
                appointment.getDate(),
                appointment.getTimeSlot(),
                appointment.getMemberId()
            );
            
            if (!available) {
                return Result.error("该时间段已被占用，请选择其他时间");
            }
            
            // 创建预约
            Appointment created = appointmentService.createAppointment(appointment);
            if (created != null && created.getId() != null) {
                String st = appointment.getServiceType();
                java.time.LocalDateTime aptTime = created.getDate() != null ? created.getDate().atStartOfDay() : null;
                appointmentDelayService.registerDelayKeys(st, created.getId(), aptTime, created.getTimeSlot());
                String svcName = "grooming".equals(st) ? "洗护预约" : ("hospital".equals(st) ? "医院预约" : "服务预约");
                appointmentDelayService.registerReminderKeys(st, created.getId(), created.getUserId(), aptTime, svcName);
            }
            return Result.success(created);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加预约失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查时间段是否可用
     */
    @GetMapping("/checkAvailable")
    @ApiOperation("检查时间段是否可用")
    public Result<Map<String, Object>> checkAvailable(@RequestParam String serviceType,
                                                       @RequestParam String date,
                                                       @RequestParam String timeSlot,
                                                       @RequestParam(required = false) Long memberId) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            boolean available = appointmentService.checkTimeSlotAvailable(serviceType, localDate, timeSlot, memberId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("available", available);
            result.put("serviceType", serviceType);
            result.put("date", date);
            result.put("timeSlot", timeSlot);
            result.put("message", available ? "时间段可用" : "时间段已被占用");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取预约列表（管理员可查看全部，服务人员只能查看自己的）
     */
    @GetMapping("/list")
    @ApiOperation("获取预约列表")
    public Result<List<Appointment>> listAppointments(@RequestParam(required = false) String serviceType,
                                                         @RequestParam(required = false) String date,
                                                         @RequestParam(required = false) String status,
                                                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            List<Appointment> appointments;
            
            // 从 token 获取当前登录用户信息
            Admin currentAdmin = getCurrentAdminFromToken(token);
            
            if (currentAdmin != null) {
                if ("staff".equals(currentAdmin.getRole())) {
                    // 服务人员：只能查看自己的预约
                    // 需要先找到对应的 member_id（根据 admin 的 department 和 name 匹配 service_member）
                    // 这里简化处理，假设 admin.id 对应 member_id（需要根据实际情况调整）
                    appointments = appointmentService.getAppointmentsByMemberId(currentAdmin.getId());
                } else {
                    // 超级管理员：可以查看所有预约
                    if (serviceType != null && date != null) {
                        LocalDate localDate = LocalDate.parse(date);
                        appointments = appointmentService.getAppointmentsByServiceTypeAndDate(serviceType, localDate);
                    } else {
                        // 查询所有预约（需要分页，这里简化处理）
                        appointments = appointmentService.getAppointmentsByDateRange(
                            LocalDate.now().minusDays(30),
                            LocalDate.now().plusDays(30)
                        );
                    }
                }
            } else {
                // 未登录或 token 无效，返回空列表
                return Result.error("未登录或登录已过期");
            }
            
            // 根据状态过滤
            if (status != null && !status.trim().isEmpty()) {
                final String statusFilter = status.trim();
                appointments = appointments.stream()
                    .filter(a -> statusFilter.equals(a.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return Result.success(appointments);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取预约列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 服务人员查看自己的预约列表
     */
    @GetMapping("/listByStaff")
    @ApiOperation("服务人员查看自己的预约列表")
    public Result<List<Appointment>> listByStaff(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            if (!"staff".equals(currentAdmin.getRole())) {
                return Result.error("该接口仅限服务人员访问");
            }
            
            // 获取服务人员的预约列表
            // 需要根据 admin 找到对应的 service_member 的 id
            // 这里简化处理，假设可以通过某种方式关联
            List<Appointment> appointments = appointmentService.getAppointmentsByMemberId(currentAdmin.getId());
            
            return Result.success(appointments);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取预约列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 分配工单给服务人员（管理员操作）
     */
    @PostMapping("/assign")
    @ApiOperation("分配工单")
    public Result<Appointment> assignAppointment(@RequestParam Long appointmentId,
                                                   @RequestParam Long memberId,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            if (!"admin".equalsIgnoreCase(currentAdmin.getRole())) {
                return Result.error("只有管理员可以分配工单");
            }
            
            Appointment appointment = appointmentService.assignAppointment(appointmentId, memberId);
            if (appointment != null && appointment.getId() != null) {
                String st = appointment.getServiceType();
                java.time.LocalDateTime aptTime = appointment.getDate() != null ? appointment.getDate().atStartOfDay() : null;
                String svcName = "grooming".equals(st) ? "洗护预约" : ("hospital".equals(st) ? "医院预约" : "服务预约");
                appointmentDelayService.registerReminderKeys(st, appointment.getId(), appointment.getUserId(), aptTime, svcName);
            }
            return Result.success(appointment);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("分配工单失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新预约信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新预约信息")
    public Result<Appointment> updateAppointment(@PathVariable Long id,
                                                   @RequestBody Appointment appointment,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            Appointment existing = appointmentService.getAppointmentById(id);
            if (existing == null) {
                return Result.error("预约不存在");
            }
            
            // 服务人员只能更新自己工单的信息
            if ("staff".equals(currentAdmin.getRole())) {
                if (existing.getMemberId() == null || !existing.getMemberId().equals(currentAdmin.getId())) {
                    return Result.error("无权操作此预约");
                }
            }
            
            // 设置ID并更新
            appointment.setId(id);
            Appointment updated = appointmentService.updateAppointment(appointment);
            return Result.success(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新预约失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新预约状态
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新预约状态")
    public Result<Appointment> updateStatus(@PathVariable Long id,
                                             @RequestParam String status,
                                             @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            Appointment appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                return Result.error("预约不存在");
            }
            
            // 服务人员只能更新自己工单的状态
            if ("staff".equals(currentAdmin.getRole())) {
                if (appointment.getMemberId() == null || !appointment.getMemberId().equals(currentAdmin.getId())) {
                    return Result.error("无权操作此预约");
                }
            }
            
            Appointment updated = appointmentService.updateAppointmentStatus(id, status);
            return Result.success(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取预约详情")
    public Result<Appointment> getAppointmentDetail(@PathVariable Long id,
                                                      @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Admin currentAdmin = getCurrentAdminFromToken(token);
            if (currentAdmin == null) {
                return Result.error("未登录或登录已过期");
            }
            
            Appointment appointment = appointmentService.getAppointmentById(id);
            if (appointment == null) {
                return Result.error("预约不存在");
            }
            
            // 服务人员只能查看自己的预约
            if ("staff".equals(currentAdmin.getRole())) {
                if (appointment.getMemberId() == null || !appointment.getMemberId().equals(currentAdmin.getId())) {
                    return Result.error("无权查看此预约");
                }
            }
            
            return Result.success(appointment);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取预约详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 从 token 获取当前管理员信息
     */
    private Admin getCurrentAdminFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        try {
            token = token.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null) {
                return null;
            }
            return adminService.getByUsername(username);
        } catch (Exception e) {
            System.err.println("解析 token 失败: " + e.getMessage());
            return null;
        }
    }
}


