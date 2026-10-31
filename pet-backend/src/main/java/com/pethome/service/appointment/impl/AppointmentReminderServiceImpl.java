package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.AppointmentReminderSent;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.entity.Notification;
import com.pethome.mapper.AppointmentReminderSentMapper;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.mapper.GroomingAppointmentMapper;
import com.pethome.mapper.HospitalAppointmentMapper;
import com.pethome.service.appointment.AppointmentReminderService;
import com.pethome.service.community.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预约提醒实现。
 *
 * 幂等策略：以 (appointment_type, appointment_id, remind_type) 为业务键，
 * 批量 IN 查询已发送记录后在内存去重，避免逐条 selectCount 的 N+1。
 */
@Service
public class AppointmentReminderServiceImpl implements AppointmentReminderService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentReminderServiceImpl.class);

    private static final String CONFIRMED = "confirmed";
    private static final String ASSIGNED = "assigned";
    // 兼容数据库可能存"已确认/已分配"
    private static final String CONFIRMED_CN = "已确认";
    private static final String ASSIGNED_CN = "已分配";
    private static final String REMIND_24H = "24h";
    private static final String REMIND_2H = "2h";

    /** 提醒窗口上界（小时）：最多提前 28 小时提醒，SQL 日期过滤据此收窄候选集 */
    private static final int MAX_REMIND_HOURS_AHEAD = 28;

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

    @Override
    public int runReminders() {
        if (notificationService == null || reminderSentMapper == null) return 0;
        int sent = 0;
        sent += runGroomingReminders();
        sent += runHospitalReminders();
        sent += runDoorCleaningReminders();
        return sent;
    }

    private int runGroomingReminders() {
        if (groomingAppointmentMapper == null) return 0;
        // 仅取未来 2 天内的预约（提醒窗口最多 28h），历史预约无需进内存
        QueryWrapper<GroomingAppointment> q = new QueryWrapper<>();
        q.select("id", "user_id", "date", "time_slot")
         .in("status", Arrays.asList(CONFIRMED, ASSIGNED, CONFIRMED_CN, ASSIGNED_CN))
         .isNotNull("date").isNotNull("time_slot")
         .between("date", LocalDate.now(), LocalDate.now().plusDays(2));
        List<GroomingAppointment> list = groomingAppointmentMapper.selectList(q);
        List<ReminderCandidate> candidates = new ArrayList<>();
        for (GroomingAppointment a : list) {
            LocalDateTime start = parseAppointmentStart(a.getDate(), a.getTimeSlot());
            if (start == null) continue;
            collectCandidate(candidates, a.getId(), a.getUserId(), start, "洗护预约");
        }
        return sendReminders("grooming", candidates);
    }

    private int runHospitalReminders() {
        if (hospitalAppointmentMapper == null) return 0;
        QueryWrapper<HospitalAppointment> q = new QueryWrapper<>();
        q.select("id", "user_id", "date", "time_slot")
         .in("status", Arrays.asList(CONFIRMED, ASSIGNED, CONFIRMED_CN, ASSIGNED_CN))
         .isNotNull("date").isNotNull("time_slot")
         .between("date", LocalDate.now(), LocalDate.now().plusDays(2));
        List<HospitalAppointment> list = hospitalAppointmentMapper.selectList(q);
        List<ReminderCandidate> candidates = new ArrayList<>();
        for (HospitalAppointment a : list) {
            LocalDateTime start = parseAppointmentStart(a.getDate(), a.getTimeSlot());
            if (start == null) continue;
            collectCandidate(candidates, a.getId(), a.getUserId(), start, "医院预约");
        }
        return sendReminders("hospital", candidates);
    }

    private int runDoorCleaningReminders() {
        if (doorCleaningAppointmentMapper == null) return 0;
        // 仅取提醒窗口内的预约：appointment_date（datetime）或 date（date）二选一命中即可
        LocalDateTime windowEnd = LocalDateTime.now().plusHours(MAX_REMIND_HOURS_AHEAD);
        QueryWrapper<DoorCleaningAppointment> q = new QueryWrapper<>();
        q.select("id", "user_id", "appointment_date", "date", "time_slot")
         .in("status", Arrays.asList(CONFIRMED, ASSIGNED, CONFIRMED_CN, ASSIGNED_CN))
         .and(w -> w.between("appointment_date", LocalDateTime.now(), windowEnd)
                 .or(k -> k.between("date", LocalDate.now(), LocalDate.now().plusDays(2))));
        List<DoorCleaningAppointment> list = doorCleaningAppointmentMapper.selectList(q);
        List<ReminderCandidate> candidates = new ArrayList<>();
        for (DoorCleaningAppointment a : list) {
            LocalDateTime start = null;
            if (a.getAppointmentDate() != null) {
                start = a.getAppointmentDate();
            } else if (a.getDate() != null && a.getTimeSlot() != null) {
                start = parseAppointmentStart(a.getDate(), a.getTimeSlot());
            }
            if (start == null) continue;
            collectCandidate(candidates, a.getId(), a.getUserId(), start, "上门铲屎预约");
        }
        return sendReminders("door_cleaning", candidates);
    }

    /**
     * 计算提醒窗口（24h / 2h，±4 小时容差），命中则加入候选列表
     */
    private void collectCandidate(List<ReminderCandidate> candidates,
                                  Long appointmentId, Long userId,
                                  LocalDateTime appointmentStart, String serviceName) {
        if (userId == null) return;
        LocalDateTime now = LocalDateTime.now();
        if (appointmentStart.isBefore(now)) return;
        long hours = Duration.between(now, appointmentStart).toHours();
        String remindType = null;
        String titleSuffix = null;
        // 窗口略微放宽：任务每小时整点执行一次，放宽到 ±4 小时窗口避免刚好错过
        if (hours >= 20 && hours <= 28) {
            remindType = REMIND_24H;
            titleSuffix = "24 小时";
        } else if (hours >= 0 && hours <= 4) {
            remindType = REMIND_2H;
            titleSuffix = "2 小时";
        }
        if (remindType == null) return;
        candidates.add(new ReminderCandidate(appointmentId, userId, appointmentStart, remindType, titleSuffix, serviceName));
    }

    /**
     * 批量发送提醒：单次 IN 查询已发送记录去重（替代逐条 selectCount，消除 N+1），
     * 仅对未发送的候选创建通知与发送记录。
     */
    private int sendReminders(String appointmentType, List<ReminderCandidate> candidates) {
        if (candidates.isEmpty()) return 0;
        List<Long> appointmentIds = candidates.stream()
                .map(c -> c.appointmentId)
                .distinct()
                .collect(Collectors.toList());
        Set<String> sentKeys = new HashSet<>();
        QueryWrapper<AppointmentReminderSent> q = new QueryWrapper<>();
        q.eq("appointment_type", appointmentType).in("appointment_id", appointmentIds);
        for (AppointmentReminderSent sent : reminderSentMapper.selectList(q)) {
            sentKeys.add(sent.getAppointmentId() + ":" + sent.getRemindType());
        }

        int sent = 0;
        for (ReminderCandidate c : candidates) {
            if (sentKeys.contains(c.appointmentId + ":" + c.remindType)) continue;
            LocalDateTime now = LocalDateTime.now();
            Notification n = new Notification();
            n.setUserId(c.userId);
            n.setTitle("预约提醒");
            String startText = c.appointmentStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            n.setContent("您的" + c.serviceName + "（预约单#" + c.appointmentId + "）将于 " + startText
                    + " 开始（约" + c.titleSuffix + "后），请提前安排时间。");
            n.setType("appointment_remind");
            n.setStatus(0);
            n.setRelatedId(c.appointmentId);
            n.setRelatedType("appointment");
            n.setCreateTime(now);
            n.setUpdateTime(now);
            notificationService.createNotification(n);

            AppointmentReminderSent record = new AppointmentReminderSent();
            record.setAppointmentType(appointmentType);
            record.setAppointmentId(c.appointmentId);
            record.setRemindType(c.remindType);
            record.setCreatedAt(now);
            reminderSentMapper.insert(record);

            sent++;
            logger.info("预约提醒已发送: type={}, id={}, userId={}, {}", appointmentType, c.appointmentId, c.userId, c.remindType);
        }
        return sent;
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

    /** 提醒候选（同一类型内使用） */
    private static final class ReminderCandidate {
        final Long appointmentId;
        final Long userId;
        final LocalDateTime appointmentStart;
        final String remindType;
        final String titleSuffix;
        final String serviceName;

        ReminderCandidate(Long appointmentId, Long userId, LocalDateTime appointmentStart,
                          String remindType, String titleSuffix, String serviceName) {
            this.appointmentId = appointmentId;
            this.userId = userId;
            this.appointmentStart = appointmentStart;
            this.remindType = remindType;
            this.titleSuffix = titleSuffix;
            this.serviceName = serviceName;
        }
    }
}
