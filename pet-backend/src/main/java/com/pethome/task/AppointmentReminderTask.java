package com.pethome.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.*;
import com.pethome.mapper.*;
import com.pethome.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预约提醒定时任务：在服务前 24 小时、2 小时向用户推送提醒通知
 */
@Component
public class AppointmentReminderTask {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderTask.class);
    private static final String CONFIRMED = "confirmed";
    private static final String ASSIGNED = "assigned";
    // 兼容数据库可能存“已确认/已分配”
    private static final String CONFIRMED_CN = "已确认";
    private static final String ASSIGNED_CN = "已分配";
    private static final String REMIND_24H = "24h";
    private static final String REMIND_2H = "2h";

    @Autowired(required = false)
    private GroomingAppointmentMapper groomingAppointmentMapper;
    @Autowired(required = false)
    private HospitalAppointmentMapper hospitalAppointmentMapper;
    @Autowired(required = false)
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;
    @Autowired(required = false)
    private AppointmentReminderSentMapper reminderSentMapper;
    @Autowired(required = false)
    private NotificationService notificationService;

    @Scheduled(cron = "0 0 * * * ?")
    public void runReminders() {
        if (notificationService == null || reminderSentMapper == null) return;
        try {
            runGroomingReminders();
            runHospitalReminders();
            runDoorCleaningReminders();
        } catch (Exception e) {
            logger.error("预约提醒任务异常", e);
        }
    }

    private void runGroomingReminders() {
        if (groomingAppointmentMapper == null) return;
        QueryWrapper<GroomingAppointment> q = new QueryWrapper<>();
        q.in("status", java.util.Arrays.asList(CONFIRMED, ASSIGNED, CONFIRMED_CN, ASSIGNED_CN))
         .isNotNull("date").isNotNull("time_slot");
        List<GroomingAppointment> list = groomingAppointmentMapper.selectList(q);
        for (GroomingAppointment a : list) {
            LocalDateTime start = parseAppointmentStart(a.getDate(), a.getTimeSlot());
            if (start == null) continue;
            trySendReminder("grooming", a.getId(), a.getUserId(), start, "洗护预约");
        }
    }

    private void runHospitalReminders() {
        if (hospitalAppointmentMapper == null) return;
        QueryWrapper<HospitalAppointment> q = new QueryWrapper<>();
        q.in("status", java.util.Arrays.asList(CONFIRMED, ASSIGNED, CONFIRMED_CN, ASSIGNED_CN))
         .isNotNull("date").isNotNull("time_slot");
        List<HospitalAppointment> list = hospitalAppointmentMapper.selectList(q);
        for (HospitalAppointment a : list) {
            LocalDateTime start = parseAppointmentStart(a.getDate(), a.getTimeSlot());
            if (start == null) continue;
            trySendReminder("hospital", a.getId(), a.getUserId(), start, "医院预约");
        }
    }

    private void runDoorCleaningReminders() {
        if (doorCleaningAppointmentMapper == null) return;
        QueryWrapper<DoorCleaningAppointment> q = new QueryWrapper<>();
        q.in("status", java.util.Arrays.asList(CONFIRMED, ASSIGNED, CONFIRMED_CN, ASSIGNED_CN));
        List<DoorCleaningAppointment> list = doorCleaningAppointmentMapper.selectList(q);
        for (DoorCleaningAppointment a : list) {
            LocalDateTime start = null;
            if (a.getAppointmentDate() != null) {
                start = a.getAppointmentDate();
            } else if (a.getDate() != null && a.getTimeSlot() != null) {
                start = parseAppointmentStart(a.getDate(), a.getTimeSlot());
            }
            if (start == null) continue;
            trySendReminder("door_cleaning", a.getId(), a.getUserId(), start, "上门铲屎预约");
        }
    }

    private LocalDateTime parseAppointmentStart(LocalDate date, String timeSlot) {
        if (date == null || timeSlot == null || timeSlot.isBlank()) return null;
        String startPart = timeSlot.split("-")[0].trim();
        if (startPart.isEmpty()) return date.atStartOfDay();
        try {
            LocalTime t = LocalTime.parse(startPart, DateTimeFormatter.ofPattern("HH:mm"));
            return date.atTime(t);
        } catch (Exception e) {
            try {
                LocalTime t = LocalTime.parse(startPart, DateTimeFormatter.ofPattern("H:mm"));
                return date.atTime(t);
            } catch (Exception e2) {
                return date.atStartOfDay();
            }
        }
    }

    private void trySendReminder(String appointmentType, Long appointmentId, Long userId,
                                 LocalDateTime appointmentStart, String serviceName) {
        if (userId == null) return;
        LocalDateTime now = LocalDateTime.now();
        if (appointmentStart.isBefore(now)) return;
        java.time.Duration d = java.time.Duration.between(now, appointmentStart);
        long hours = d.toHours();
        String remindType = null;
        String titleSuffix = null;
        // 窗口略微放宽：任务每 30 分钟跑一次，避免刚好错过
        if (hours >= 20 && hours <= 28) {
            remindType = REMIND_24H;
            titleSuffix = "24 小时";
        } else if (hours >= 0 && hours <= 4) {
            remindType = REMIND_2H;
            titleSuffix = "2 小时";
        }
        if (remindType == null) return;
        QueryWrapper<AppointmentReminderSent> q = new QueryWrapper<>();
        q.eq("appointment_type", appointmentType).eq("appointment_id", appointmentId).eq("remind_type", remindType);
        if (reminderSentMapper.selectCount(q) > 0) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle("预约提醒");
        String startText = appointmentStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        n.setContent("您的" + serviceName + "（预约单#" + appointmentId + "）将于 " + startText + " 开始（约" + titleSuffix + "后），请提前安排时间。");
        n.setType("appointment_remind");
        n.setStatus(0);
        n.setRelatedId(appointmentId);
        n.setRelatedType("appointment");
        n.setCreateTime(now);
        n.setUpdateTime(now);
        notificationService.createNotification(n);
        AppointmentReminderSent sent = new AppointmentReminderSent();
        sent.setAppointmentType(appointmentType);
        sent.setAppointmentId(appointmentId);
        sent.setRemindType(remindType);
        sent.setCreatedAt(now);
        reminderSentMapper.insert(sent);
        logger.info("预约提醒已发送: type={}, id={}, userId={}, {}", appointmentType, appointmentId, userId, remindType);
    }
}
