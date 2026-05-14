package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.Result;
import com.pethome.entity.*;
import com.pethome.service.*;
import com.pethome.util.AdminContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/verify")
@Api(tags = "核销管理")
public class VerifyController {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private HospitalAppointmentService hospitalAppointmentService;
    
    @Autowired
    private GroomingAppointmentService groomingAppointmentService;
    
    @Autowired
    private DoorCleaningAppointmentService doorCleaningAppointmentService;

    @Autowired
    private ServiceMemberService serviceMemberService;
    
    @Autowired(required = false)
    private AdminContext adminContext;

    @PostMapping("/verify-code")
    @ApiOperation("核销验证（支持所有类型的预约）")
    public Result<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            String verifyCode = request.get("verifyCode");
            
            System.out.println("收到核销请求，核销码: " + verifyCode);
            
            if (verifyCode == null || verifyCode.trim().isEmpty()) {
                return Result.error("核销码不能为空");
            }
            
            verifyCode = verifyCode.trim();
            System.out.println("处理后的核销码: [" + verifyCode + "], 长度: " + verifyCode.length());

            // 身份与权限：平台管理员可核销任意订单；服务人员仅可核销分配给自己的订单；
            // 分店管理员可代核销本门店订单（订单仍归属原服务人员 memberId，不修改）
            Long staffMemberId = null;
            Long storeIdForProxyVerify = null;
            Long adminId = (Long) httpRequest.getAttribute("adminId");
            String role = (String) httpRequest.getAttribute("role");
            Long serviceStoreId = (Long) httpRequest.getAttribute("serviceStoreId");
            if (adminId == null) {
                return Result.error("请先登录工作人员账号后再核销");
            }
            String roleNorm = role != null ? role.trim().toLowerCase() : "";
            if ("admin".equals(roleNorm)) {
                staffMemberId = null;
                storeIdForProxyVerify = null;
            } else if ("store_admin".equals(roleNorm) || "store-admin".equals(roleNorm)) {
                if (serviceMemberService == null) {
                    return Result.error("系统未启用服务人员模块，无法核销");
                }
                ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
                if (sm == null || sm.getStoreId() == null) {
                    return Result.error("分店管理员未关联门店，无法核销");
                }
                storeIdForProxyVerify = sm.getStoreId();
                staffMemberId = null;
                System.out.println("分店管理员代核销: adminId=" + adminId + ", storeId=" + storeIdForProxyVerify);
            } else if ("staff".equalsIgnoreCase(role) || "merchant".equalsIgnoreCase(role)) {
                if ("staff".equalsIgnoreCase(role) && serviceStoreId == null) {
                    return Result.error("当前账号未绑定服务门店，无法核销");
                }
                if (serviceMemberService == null) {
                    return Result.error("系统未启用服务人员模块，无法核销");
                }
                ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
                if (sm == null || sm.getId() == null) {
                    return Result.error("您尚未分配服务部门，无法核销");
                }
                staffMemberId = sm.getId();
                System.out.println("服务人员核销: adminId=" + adminId + ", staffMemberId=" + staffMemberId + ", role=" + role);
            } else {
                if (serviceMemberService == null) {
                    return Result.error("当前账号无核销权限");
                }
                ServiceMember sm = serviceMemberService.getMemberByUserId(adminId);
                if (sm != null && sm.getId() != null) {
                    staffMemberId = sm.getId();
                } else {
                    return Result.error("当前账号无核销权限");
                }
            }

            Map<String, Object> result = verifyAppointmentByCode(verifyCode, staffMemberId, storeIdForProxyVerify);

            if (result != null && Boolean.TRUE.equals(result.get("unauthorized"))) {
                String reason = result.get("reason") != null ? result.get("reason").toString() : "";
                if ("wrong_store".equals(reason)) {
                    return Result.error("只能核销本门店服务人员的订单");
                }
                return Result.error("只有该订单的服务人员才能核销");
            }
            if (result != null && result.containsKey("appointment")) {
                System.out.println("核销成功，预约ID: " + result.get("appointment"));
                return Result.success("核销成功", result);
            } else {
                System.out.println("核销失败：未找到有效预约或已核销");
                // 尝试查找预约是否存在但已核销
                Map<String, Object> checkResult = findAppointmentByCode(verifyCode);
                if (checkResult != null && checkResult.containsKey("appointment")) {
                    Boolean isVerified = (Boolean) checkResult.get("isVerified");
                    if (isVerified != null && isVerified) {
                        return Result.error("核销码已使用");
                    }
                }
                return Result.error("核销码无效或已使用");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("核销异常: " + e.getMessage());
            return Result.error("核销失败：" + e.getMessage());
        }
    }

    @GetMapping("/check-code/{verifyCode}")
    @ApiOperation("检查核销码状态")
    public Result<Map<String, Object>> checkCode(@PathVariable String verifyCode) {
        try {
            if (verifyCode == null || verifyCode.trim().isEmpty()) {
                return Result.error("核销码不能为空");
            }
            
            verifyCode = verifyCode.trim();
            
            // 从各种预约表中查找
            Map<String, Object> data = findAppointmentByCode(verifyCode);
            
            if (data != null && data.containsKey("appointment")) {
                return Result.success("核销码有效", data);
            } else {
                data = new HashMap<>();
                data.put("verifyCode", verifyCode);
                data.put("isValid", false);
                data.put("isVerified", false);
                return Result.error("核销码不存在");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("检查失败：" + e.getMessage());
        }
    }

    @GetMapping("/today-records")
    @ApiOperation("获取今日核销记录（所有已核销的预约，按核销时间倒序）")
    public Result<List<Map<String, Object>>> getTodayVerifyRecords(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            Long currentServiceStoreId = null;
            if (adminContext != null && token != null) {
                currentServiceStoreId = adminContext.getCurrentServiceStoreId(token);
            }
            List<Map<String, Object>> list = new ArrayList<>();

            QueryWrapper<HospitalAppointment> hw = new QueryWrapper<>();
            hw.eq("is_verified", 1).ge("verify_time", todayStart);
            if (currentServiceStoreId != null) {
                hw.eq("store_id", currentServiceStoreId);
            }
            hw.orderByDesc("verify_time");
            hospitalAppointmentService.list(hw).forEach(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", a.getId());
                m.put("serviceType", "hospital");
                m.put("contactName", a.getContactName());
                m.put("contactPhone", a.getContactPhone());
                m.put("verifyCode", a.getVerifyCode());
                m.put("verifyTime", a.getVerifyTime());
                list.add(m);
            });

            QueryWrapper<GroomingAppointment> gw = new QueryWrapper<>();
            gw.eq("is_verified", 1).ge("verify_time", todayStart);
            if (currentServiceStoreId != null) {
                gw.eq("store_id", currentServiceStoreId);
            }
            gw.orderByDesc("verify_time");
            groomingAppointmentService.list(gw).forEach(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", a.getId());
                m.put("serviceType", "grooming");
                m.put("contactName", a.getContactName());
                m.put("contactPhone", a.getContactPhone());
                m.put("verifyCode", a.getVerifyCode());
                m.put("verifyTime", a.getVerifyTime());
                list.add(m);
            });

            QueryWrapper<DoorCleaningAppointment> dw = new QueryWrapper<>();
            dw.eq("is_verified", 1).ge("verify_time", todayStart);
            if (currentServiceStoreId != null) {
                dw.eq("store_id", currentServiceStoreId);
            }
            dw.orderByDesc("verify_time");
            doorCleaningAppointmentService.list(dw).forEach(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", a.getId());
                m.put("serviceType", "door-cleaning");
                m.put("contactName", a.getContactName());
                m.put("contactPhone", a.getContactPhone());
                m.put("verifyCode", a.getVerifyCode());
                m.put("verifyTime", a.getVerifyTime());
                list.add(m);
            });

            List<Map<String, Object>> sorted = list.stream().sorted((a, b) -> {
                LocalDateTime ta = (LocalDateTime) a.get("verifyTime");
                LocalDateTime tb = (LocalDateTime) b.get("verifyTime");
                if (ta == null && tb == null) return 0;
                if (ta == null) return 1;
                if (tb == null) return -1;
                return tb.compareTo(ta);
            }).collect(Collectors.toList());

            return Result.success(sorted);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取今日核销记录失败：" + e.getMessage());
        }
    }
    
    private static Map<String, Object> unauthorizedNotAssignedMember() {
        Map<String, Object> m = new HashMap<>();
        m.put("unauthorized", true);
        m.put("reason", "not_assigned_member");
        return m;
    }

    private static Map<String, Object> unauthorizedWrongStore() {
        Map<String, Object> m = new HashMap<>();
        m.put("unauthorized", true);
        m.put("reason", "wrong_store");
        return m;
    }

    /**
     * 订单是否属于指定门店：优先看订单 store_id，否则看接单服务人员所属门店。
     */
    private boolean orderBelongsToServiceStore(Long appointmentStoreId, Long assignedMemberId, Long requiredStoreId) {
        if (requiredStoreId == null) {
            return true;
        }
        if (appointmentStoreId != null) {
            return requiredStoreId.equals(appointmentStoreId);
        }
        if (assignedMemberId != null && serviceMemberService != null) {
            ServiceMember m = serviceMemberService.getMemberById(assignedMemberId);
            if (m != null && m.getStoreId() != null) {
                return requiredStoreId.equals(m.getStoreId());
            }
        }
        return false;
    }

    /**
     * 根据核销码查找并核销预约
     * @param staffMemberId 服务人员本人核销时非空，须与订单 member_id 一致
     * @param storeIdForProxyVerify 分店管理员代核销时非空，仅允许订单门店与之一致；不修改订单上的服务人员
     */
    private Map<String, Object> verifyAppointmentByCode(String verifyCode, Long staffMemberId, Long storeIdForProxyVerify) {
        System.out.println("开始查找核销码: " + verifyCode);
        
        // 1. 尝试医院预约
        QueryWrapper<HospitalAppointment> hospitalWrapper = new QueryWrapper<>();
        hospitalWrapper.eq("verify_code", verifyCode);
        
        System.out.println("查询医院预约，条件: verify_code = [" + verifyCode + "]");
        
        HospitalAppointment hospitalAppointment = hospitalAppointmentService.getOne(hospitalWrapper);
        
        if (hospitalAppointment != null) {
            System.out.println("医院预约查询结果: 找到，ID=" + hospitalAppointment.getId() 
                + ", verifyCode=[" + hospitalAppointment.getVerifyCode() + "]"
                + ", isVerified=" + hospitalAppointment.getIsVerified()
                + ", status=" + hospitalAppointment.getStatus());
        } else {
            System.out.println("医院预约查询结果: 未找到");
            QueryWrapper<HospitalAppointment> allWrapper = new QueryWrapper<>();
            allWrapper.select("id", "verify_code", "is_verified", "status");
            allWrapper.last("LIMIT 10");
            java.util.List<HospitalAppointment> recentAppointments = hospitalAppointmentService.list(allWrapper);
            String recentCodes = recentAppointments.stream()
                .map(a -> {
                    try {
                        return "ID=" + (a.getId() != null ? a.getId() : "null") + ", verifyCode=[" + (a.getVerifyCode() != null ? a.getVerifyCode() : "null") + "]";
                    } catch (Exception e) {
                        return "ID=unknown";
                    }
                })
                .collect(java.util.stream.Collectors.joining(", "));
            System.out.println("最近10条医院预约的核销码: " + recentCodes);
        }
        
        if (hospitalAppointment != null && (hospitalAppointment.getIsVerified() == null || hospitalAppointment.getIsVerified() == 0)) {
            if (storeIdForProxyVerify != null) {
                if (!orderBelongsToServiceStore(hospitalAppointment.getStoreId(), hospitalAppointment.getMemberId(), storeIdForProxyVerify)) {
                    return unauthorizedWrongStore();
                }
            } else if (staffMemberId != null && (hospitalAppointment.getMemberId() == null || !hospitalAppointment.getMemberId().equals(staffMemberId))) {
                return unauthorizedNotAssignedMember();
            }
            hospitalAppointment.setIsVerified(1);
            hospitalAppointment.setVerifyTime(LocalDateTime.now());
            hospitalAppointment.setUpdateTime(LocalDateTime.now());
            if (!"completed".equals(hospitalAppointment.getStatus())) {
                hospitalAppointment.setStatus("completed");
            }
            hospitalAppointmentService.updateById(hospitalAppointment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("appointment", hospitalAppointment);
            result.put("serviceType", "hospital");
            return result;
        }
        
        // 2. 尝试洗护预约
        QueryWrapper<GroomingAppointment> groomingWrapper = new QueryWrapper<>();
        groomingWrapper.eq("verify_code", verifyCode);
        GroomingAppointment groomingAppointment = groomingAppointmentService.getOne(groomingWrapper);
        if (groomingAppointment != null && (groomingAppointment.getIsVerified() == null || groomingAppointment.getIsVerified() == 0)) {
            if (storeIdForProxyVerify != null) {
                if (!orderBelongsToServiceStore(groomingAppointment.getStoreId(), groomingAppointment.getMemberId(), storeIdForProxyVerify)) {
                    return unauthorizedWrongStore();
                }
            } else if (staffMemberId != null && (groomingAppointment.getMemberId() == null || !groomingAppointment.getMemberId().equals(staffMemberId))) {
                return unauthorizedNotAssignedMember();
            }
            groomingAppointment.setIsVerified(1);
            groomingAppointment.setVerifyTime(LocalDateTime.now());
            groomingAppointment.setUpdateTime(LocalDateTime.now());
            if (!"completed".equals(groomingAppointment.getStatus())) {
                groomingAppointment.setStatus("completed");
            }
            groomingAppointmentService.updateById(groomingAppointment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("appointment", groomingAppointment);
            result.put("serviceType", "grooming");
            return result;
        }
        
        // 3. 尝试上门铲屎预约
        QueryWrapper<DoorCleaningAppointment> doorCleaningWrapper = new QueryWrapper<>();
        doorCleaningWrapper.eq("verify_code", verifyCode);
        DoorCleaningAppointment doorCleaningAppointment = doorCleaningAppointmentService.getOne(doorCleaningWrapper);
        if (doorCleaningAppointment != null && (doorCleaningAppointment.getIsVerified() == null || doorCleaningAppointment.getIsVerified() == 0)) {
            if (storeIdForProxyVerify != null) {
                if (!orderBelongsToServiceStore(doorCleaningAppointment.getStoreId(), doorCleaningAppointment.getMemberId(), storeIdForProxyVerify)) {
                    return unauthorizedWrongStore();
                }
            } else if (staffMemberId != null && (doorCleaningAppointment.getMemberId() == null || !doorCleaningAppointment.getMemberId().equals(staffMemberId))) {
                return unauthorizedNotAssignedMember();
            }
            doorCleaningAppointment.setIsVerified(1);
            doorCleaningAppointment.setVerifyTime(LocalDateTime.now());
            doorCleaningAppointment.setUpdateTime(LocalDateTime.now());
            if (!"completed".equals(doorCleaningAppointment.getStatus())) {
                doorCleaningAppointment.setStatus("completed");
            }
            doorCleaningAppointmentService.updateById(doorCleaningAppointment);
            
            Map<String, Object> result = new HashMap<>();
            result.put("appointment", doorCleaningAppointment);
            result.put("serviceType", "door-cleaning");
            return result;
        }
        
        // 4. 尝试统一预约表（兼容旧数据）
        Appointment appointment = appointmentService.getAppointmentByVerifyCode(verifyCode);
        if (appointment != null && (appointment.getIsVerified() == null || appointment.getIsVerified() == 0)) {
            if (storeIdForProxyVerify != null) {
                if (!orderBelongsToServiceStore(null, appointment.getMemberId(), storeIdForProxyVerify)) {
                    return unauthorizedWrongStore();
                }
            } else if (staffMemberId != null && (appointment.getMemberId() == null || !appointment.getMemberId().equals(staffMemberId))) {
                return unauthorizedNotAssignedMember();
            }
            appointment = appointmentService.verifyAppointment(verifyCode);
            if (appointment != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("appointment", appointment);
                result.put("serviceType", appointment.getServiceType());
                return result;
            }
        } else if (appointment != null && appointment.getIsVerified() != null && appointment.getIsVerified() == 1) {
            // 已核销，不重复处理
        }
        
        return null;
    }
    
    /**
     * 根据核销码查找预约（不核销）
     */
    private Map<String, Object> findAppointmentByCode(String verifyCode) {
        QueryWrapper<HospitalAppointment> hospitalWrapper = new QueryWrapper<>();
        hospitalWrapper.eq("verify_code", verifyCode);
        HospitalAppointment hospitalAppointment = hospitalAppointmentService.getOne(hospitalWrapper);
        if (hospitalAppointment != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("verifyCode", verifyCode);
            data.put("isValid", hospitalAppointment.getIsVerified() == null || hospitalAppointment.getIsVerified() == 0);
            data.put("isVerified", hospitalAppointment.getIsVerified() != null && hospitalAppointment.getIsVerified() == 1);
            data.put("appointment", hospitalAppointment);
            data.put("serviceType", "hospital");
            return data;
        }
        
        QueryWrapper<GroomingAppointment> groomingWrapper = new QueryWrapper<>();
        groomingWrapper.eq("verify_code", verifyCode);
        GroomingAppointment groomingAppointment = groomingAppointmentService.getOne(groomingWrapper);
        if (groomingAppointment != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("verifyCode", verifyCode);
            data.put("isValid", groomingAppointment.getIsVerified() == null || groomingAppointment.getIsVerified() == 0);
            data.put("isVerified", groomingAppointment.getIsVerified() != null && groomingAppointment.getIsVerified() == 1);
            data.put("appointment", groomingAppointment);
            data.put("serviceType", "grooming");
            return data;
        }
        
        QueryWrapper<DoorCleaningAppointment> doorCleaningWrapper = new QueryWrapper<>();
        doorCleaningWrapper.eq("verify_code", verifyCode);
        DoorCleaningAppointment doorCleaningAppointment = doorCleaningAppointmentService.getOne(doorCleaningWrapper);
        if (doorCleaningAppointment != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("verifyCode", verifyCode);
            data.put("isValid", doorCleaningAppointment.getIsVerified() == null || doorCleaningAppointment.getIsVerified() == 0);
            data.put("isVerified", doorCleaningAppointment.getIsVerified() != null && doorCleaningAppointment.getIsVerified() == 1);
            data.put("appointment", doorCleaningAppointment);
            data.put("serviceType", "door-cleaning");
            return data;
        }
        
        return null;
    }
}
