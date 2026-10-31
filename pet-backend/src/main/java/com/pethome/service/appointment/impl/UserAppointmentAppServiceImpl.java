package com.pethome.service.appointment.impl;

import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.service.appointment.DoorCleaningAppointmentService;
import com.pethome.service.appointment.GroomingAppointmentService;
import com.pethome.service.appointment.HospitalAppointmentService;
import com.pethome.service.appointment.UserAppointmentAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserAppointmentAppServiceImpl implements UserAppointmentAppService {

    private static final Logger log = LoggerFactory.getLogger(UserAppointmentAppServiceImpl.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private DoorCleaningAppointmentService doorCleaningAppointmentService;
    @Autowired
    private HospitalAppointmentService hospitalAppointmentService;
    @Autowired
    private GroomingAppointmentService groomingAppointmentService;

    @Override
    public List<Map<String, Object>> listUserAppointments(Long userId) {
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
        return all;
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
