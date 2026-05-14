package com.pethome.controller;

import com.pethome.common.Result;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.service.DoorCleaningAppointmentService;
import com.pethome.service.GroomingAppointmentService;
import com.pethome.service.HospitalAppointmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 用户端「我的预约」统一接口：一次请求返回上门铲屎、医院、洗护三类预约列表，避免三个接口都失败导致整页加载失败。
 */
@RestController
@RequestMapping("/api/user-appointments")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
@Api(tags = "用户预约列表（统一）")
public class UserAppointmentController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DoorCleaningAppointmentService doorCleaningAppointmentService;
    @Autowired
    private HospitalAppointmentService hospitalAppointmentService;
    @Autowired
    private GroomingAppointmentService groomingAppointmentService;

    @GetMapping("/list/{userId}")
    @ApiOperation("获取当前用户全部预约（上门铲屎+医院+洗护）")
    public Result<List<Map<String, Object>>> getUserAppointments(@PathVariable Long userId) {
        if (userId == null || userId < 1) {
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
            // 单类失败不影响其他
        }
        try {
            List<HospitalAppointment> hospitalList = hospitalAppointmentService.getAppointmentsByUserId(userId);
            if (hospitalList != null) {
                for (HospitalAppointment a : hospitalList) {
                    all.add(toMap(a, "hospital"));
                }
            }
        } catch (Exception e) {
        }
        try {
            List<GroomingAppointment> groomingList = groomingAppointmentService.getAppointmentsByUserId(userId);
            if (groomingList != null) {
                for (GroomingAppointment a : groomingList) {
                    all.add(toMap(a, "grooming"));
                }
            }
        } catch (Exception e) {
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
