package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.service.DoorCleaningAppointmentService;
import com.pethome.service.GroomingAppointmentService;
import com.pethome.service.HospitalAppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 用户端「我的预约」统一接口：一次请求返回上门铲屎、医院、洗护三类预约列表，避免三个接口都失败导致整页加载失败。
 */
@RestController
@RequestMapping("/api/user-appointments")
@Tag(name = "用户预约列表（统一）")
public class UserAppointmentController {

    private static final Logger log = LoggerFactory.getLogger(UserAppointmentController.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DoorCleaningAppointmentService doorCleaningAppointmentService;
    @Autowired
    private HospitalAppointmentService hospitalAppointmentService;
    @Autowired
    private GroomingAppointmentService groomingAppointmentService;

    /**
     * 从登录态解析当前用户 ID（RoleInterceptor 对 Redis/JWT 两种 token 均已写入 userId attribute）
     */
    private Long resolveCurrentUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        return attr instanceof Number ? ((Number) attr).longValue() : null;
    }

    @GetMapping("/list/{userId}")
    @Operation(summary = "获取当前用户全部预约（上门铲屎+医院+洗护）")
    public Result<List<Map<String, Object>>> getUserAppointments(@PathVariable Long userId, HttpServletRequest request) {
        if (userId == null || userId < 1) {
            return Result.success(Collections.emptyList());
        }
        // 安全修复：归属校验——userId 必须以登录态为准，路径参数仅兼容旧前端；
        // 未登录或与登录身份不一致时视为越权，返回空列表（与订单列表策略一致）。
        Long currentUserId = resolveCurrentUserId(request);
        if (currentUserId == null || !currentUserId.equals(userId)) {
            log.warn("预约统一列表归属校验未通过: pathUserId={}, currentUserId={}", userId, currentUserId);
            return Result.success(Collections.emptyList());
        }
        List<Map<String, Object>> all = new ArrayList<>();
        try {
            List<DoorCleaningAppointment> doorList = doorCleaningAppointmentService.getAppointmentsByUserId(userId);
            if (doorList != null) {
                for (DoorCleaningAppointment a : doorList) {
                    all.add(toMap(a, "door-cleaning"));
                }
            }
        } catch (Exception e) {
            // 单类失败不影响其他，但必须留痕便于排查
            log.warn("查询用户上门铲屎预约失败 userId={}", userId, e);
        }
        try {
            List<HospitalAppointment> hospitalList = hospitalAppointmentService.getAppointmentsByUserId(userId);
            if (hospitalList != null) {
                for (HospitalAppointment a : hospitalList) {
                    all.add(toMap(a, "hospital"));
                }
            }
        } catch (Exception e) {
            log.warn("查询用户医院预约失败 userId={}", userId, e);
        }
        try {
            List<GroomingAppointment> groomingList = groomingAppointmentService.getAppointmentsByUserId(userId);
            if (groomingList != null) {
                for (GroomingAppointment a : groomingList) {
                    all.add(toMap(a, "grooming"));
                }
            }
        } catch (Exception e) {
            log.warn("查询用户洗护预约失败 userId={}", userId, e);
        }
        all.sort((a, b) -> {
            String t1 = (String) a.get("createTime");
            String t2 = (String) b.get("createTime");
            if (t1 == null) t1 = "";
            if (t2 == null) t2 = "";
            return t2.compareTo(t1);
        });
        return Result.success(all);
    }

    private static Map<String, Object> toMap(DoorCleaningAppointment a, String serviceType) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("userId", a.getUserId());
        m.put("storeId", a.getStoreId());
        m.put("date", a.getDate() != null ? a.getDate().format(DATE_FMT) : null);
        m.put("appointmentDate", a.getAppointmentDate() != null ? a.getAppointmentDate().format(DATETIME_FMT) : null);
        m.put("timeSlot", a.getTimeSlot());
        m.put("status", a.getStatus());
        m.put("serviceType", serviceType);
        m.put("price", a.getPrice() != null ? a.getPrice() : 0);
        m.put("location", a.getLocation());
        m.put("contactPhone", a.getContactPhone());
        m.put("contactName", a.getContactName());
        m.put("createTime", a.getCreateTime() != null ? a.getCreateTime().format(DATETIME_FMT) : null);
        m.put("cancellationPenaltyAmount", a.getCancellationPenaltyAmount());
        m.put("serviceId", a.getServiceId());
        m.put("serviceName", a.getServiceName());
        return m;
    }

    private static Map<String, Object> toMap(HospitalAppointment a, String serviceType) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("userId", a.getUserId());
        m.put("storeId", a.getStoreId());
        m.put("date", a.getDate() != null ? a.getDate().format(DATE_FMT) : null);
        m.put("appointmentDate", null);
        m.put("timeSlot", a.getTimeSlot());
        m.put("status", a.getStatus());
        m.put("serviceType", serviceType);
        m.put("price", a.getPrice() != null ? a.getPrice() : 0);
        m.put("location", a.getLocation());
        m.put("contactPhone", a.getContactPhone());
        m.put("contactName", a.getContactName());
        m.put("createTime", a.getCreateTime() != null ? a.getCreateTime().format(DATETIME_FMT) : null);
        m.put("cancellationPenaltyAmount", a.getCancellationPenaltyAmount());
        m.put("serviceId", a.getServiceId());
        m.put("serviceName", a.getServiceName());
        return m;
    }

    private static Map<String, Object> toMap(GroomingAppointment a, String serviceType) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("userId", a.getUserId());
        m.put("storeId", a.getStoreId());
        m.put("date", a.getDate() != null ? a.getDate().format(DATE_FMT) : null);
        m.put("appointmentDate", null);
        m.put("timeSlot", a.getTimeSlot());
        m.put("status", a.getStatus());
        m.put("serviceType", serviceType);
        m.put("price", a.getPrice() != null ? a.getPrice() : 0);
        m.put("location", a.getLocation());
        m.put("contactPhone", a.getContactPhone());
        m.put("contactName", a.getContactName());
        m.put("createTime", a.getCreateTime() != null ? a.getCreateTime().format(DATETIME_FMT) : null);
        m.put("cancellationPenaltyAmount", a.getCancellationPenaltyAmount());
        m.put("serviceId", a.getServiceId());
        m.put("serviceName", a.getServiceName());
        return m;
    }
}
