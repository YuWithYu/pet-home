package com.pethome.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.mapper.GroomingAppointmentMapper;
import com.pethome.mapper.HospitalAppointmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 预约自动处理任务（兜底机制）：
 * 1. 自动确认：对于临近服务时间（默认提前2小时）仍未处理的预约，系统自动将其状态变更为“已确认”，防止因人工操作滞后导致的服务延误。
 * 2. 爽约判定：对于服务时间结束（默认延后2小时）且未完成核销的订单，自动标记为“no_show”（爽约），并依据规则计算违约金。
 * 此定时任务作为业务闭环的最终保障，确保异常或遗漏的预约能得到标准化处理。
 */
@Component
public class AppointmentAutoExpireTask {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentAutoExpireTask.class);

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_CONFIRMED = "confirmed";
    private static final String STATUS_ASSIGNED = "assigned";
    private static final String STATUS_CONFIRMED_CN = "\u5df2\u786e\u8ba4";
    private static final String STATUS_ASSIGNED_CN = "\u5df2\u5206\u914d";
    private static final String STATUS_NO_SHOW = "no_show";
    private static final String REASON_AUTO_CONFIRM = "\u7cfb\u7edf\u81ea\u52a8\u5904\u7406\uff1a\u5de5\u4f5c\u4eba\u5458\u672a\u53ca\u65f6\u786e\u8ba4\uff0c\u5df2\u81ea\u52a8\u786e\u8ba4";
    private static final String REASON_NO_SHOW = "\u7cfb\u7edc\u81ea\u52a8\u5904\u7406\uff1a\u670d\u52a1\u65f6\u95f4\u5df2\u8fc7\u4e14\u672a\u6838\u9500\uff0c\u5df2\u6807\u8bb0\u723d\u7ea6";

    private static final int PENDING_AUTO_CONFIRM_HOURS_BEFORE = 2;
    private static final int CONFIRMED_NO_SHOW_HOURS_AFTER = 2;

    @Autowired(required = false)
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;

    @Autowired(required = false)
    private GroomingAppointmentMapper groomingAppointmentMapper;

    @Autowired(required = false)
    private HospitalAppointmentMapper hospitalAppointmentMapper;

    @Scheduled(cron = "0 0 * * * ?")
    public void autoProcessAppointments() {
        try {
            int totalProcessed = 0;
            totalProcessed += autoConfirmDoorCleaning();
            totalProcessed += autoConfirmGrooming();
            totalProcessed += autoConfirmHospital();
            totalProcessed += noShowDoorCleaning();
            totalProcessed += noShowGrooming();
            totalProcessed += noShowHospital();
            if (totalProcessed > 0) {
                logger.info("\u9884\u7ea6\u81ea\u52a8\u5904\u7406\u4efb\u52a1\u5b8c\u6210\uff1a\u5171\u5904\u7406 {} \u6761", totalProcessed);
            }
        } catch (Exception e) {
            logger.error("\u9884\u7ea6\u81ea\u52a8\u5904\u7406\u4efb\u52a1\u5f02\u5e38", e);
        }
    }

    private int autoConfirmDoorCleaning() {
        if (doorCleaningAppointmentMapper == null) return 0;
        LocalDateTime deadline = LocalDateTime.now().plusHours(PENDING_AUTO_CONFIRM_HOURS_BEFORE);
        // 性能修复 #58：原 selectList + 循环单条 update 改为单条批量 UPDATE
        // （door_cleaning 用 appointment_date 字段可直接在 SQL 比较，无需 Java 解析 time_slot）
        UpdateWrapper<DoorCleaningAppointment> uw = new UpdateWrapper<>();
        uw.eq("status", STATUS_PENDING)
          .le("appointment_date", deadline)
          .set("status", STATUS_CONFIRMED)
          .set("reject_reason", REASON_AUTO_CONFIRM)
          .set("update_time", LocalDateTime.now());
        int count = doorCleaningAppointmentMapper.update(null, uw);
        if (count > 0) {
            logger.info("上门铲屎预约自动确认: 共 {} 条（截止时间 {}）", count, deadline);
        }
        return count;
    }

    private int autoConfirmGrooming() {
        if (groomingAppointmentMapper == null) return 0;
        LocalDateTime deadline = LocalDateTime.now().plusHours(PENDING_AUTO_CONFIRM_HOURS_BEFORE);
        QueryWrapper<GroomingAppointment> q = new QueryWrapper<>();
        // 服务开始必然晚于预约日期零点，先用日期粗筛缩小候选集；仅取解析所需列
        q.select("id", "date", "time_slot")
         .eq("status", STATUS_PENDING).isNotNull("date").isNotNull("time_slot")
         .le("date", deadline.toLocalDate());
        List<GroomingAppointment> list = groomingAppointmentMapper.selectList(q);
        if (list == null || list.isEmpty()) return 0;
        List<Long> ids = new ArrayList<>();
        for (GroomingAppointment a : list) {
            LocalDateTime start = parseStartDateTime(a.getDate(), a.getTimeSlot());
            if (start == null || start.isAfter(deadline)) continue;
            ids.add(a.getId());
        }
        if (ids.isEmpty()) return 0;
        // 循环逐条 update 改为单条批量 UPDATE（time_slot 需 Java 解析，无法纯 SQL 判定）
        UpdateWrapper<GroomingAppointment> uw = new UpdateWrapper<>();
        uw.in("id", ids)
          .set("status", STATUS_CONFIRMED)
          .set("reject_reason", REASON_AUTO_CONFIRM)
          .set("update_time", LocalDateTime.now());
        int count = groomingAppointmentMapper.update(null, uw);
        if (count > 0) {
            logger.info("洗护预约自动确认: 共 {} 条（截止时间 {}）", count, deadline);
        }
        return count;
    }

    private int autoConfirmHospital() {
        if (hospitalAppointmentMapper == null) return 0;
        LocalDateTime deadline = LocalDateTime.now().plusHours(PENDING_AUTO_CONFIRM_HOURS_BEFORE);
        QueryWrapper<HospitalAppointment> q = new QueryWrapper<>();
        // 服务开始必然晚于预约日期零点，先用日期粗筛缩小候选集；仅取解析所需列
        q.select("id", "date", "time_slot")
         .eq("status", STATUS_PENDING).isNotNull("date").isNotNull("time_slot")
         .le("date", deadline.toLocalDate());
        List<HospitalAppointment> list = hospitalAppointmentMapper.selectList(q);
        if (list == null || list.isEmpty()) return 0;
        List<Long> ids = new ArrayList<>();
        for (HospitalAppointment a : list) {
            LocalDateTime start = parseStartDateTime(a.getDate(), a.getTimeSlot());
            if (start == null || start.isAfter(deadline)) continue;
            ids.add(a.getId());
        }
        if (ids.isEmpty()) return 0;
        // 循环逐条 update 改为单条批量 UPDATE（time_slot 需 Java 解析，无法纯 SQL 判定）
        UpdateWrapper<HospitalAppointment> uw = new UpdateWrapper<>();
        uw.in("id", ids)
          .set("status", STATUS_CONFIRMED)
          .set("reject_reason", REASON_AUTO_CONFIRM)
          .set("update_time", LocalDateTime.now());
        int count = hospitalAppointmentMapper.update(null, uw);
        if (count > 0) {
            logger.info("医院预约自动确认: 共 {} 条（截止时间 {}）", count, deadline);
        }
        return count;
    }

    private int noShowDoorCleaning() {
        if (doorCleaningAppointmentMapper == null) return 0;
        LocalDateTime deadline = LocalDateTime.now().minusHours(CONFIRMED_NO_SHOW_HOURS_AFTER);
        List<String> activeStatuses = Arrays.asList(STATUS_CONFIRMED, STATUS_ASSIGNED, STATUS_CONFIRMED_CN, STATUS_ASSIGNED_CN);
        // 性能修复 #58：原 selectList + 循环单条 update 改为单条批量 UPDATE
        // 罚款金额 = price * 40% 与 CancellationPenaltyUtil.PENALTY_RATE 一致，直接在 SQL 中计算
        UpdateWrapper<DoorCleaningAppointment> uw = new UpdateWrapper<>();
        uw.in("status", activeStatuses)
          .le("appointment_date", deadline)
          .and(w -> w.isNull("is_verified").or().eq("is_verified", 0))
          .set("status", STATUS_NO_SHOW)
          .set("reject_reason", REASON_NO_SHOW)
          .setSql("cancellation_penalty_amount = ROUND(IFNULL(price, 0) * 0.40, 2)")
          .set("update_time", LocalDateTime.now());
        int count = doorCleaningAppointmentMapper.update(null, uw);
        if (count > 0) {
            logger.info("上门铲屎预约爽约: 共 {} 条（截止时间 {}）", count, deadline);
        }
        return count;
    }

    private int noShowGrooming() {
        if (groomingAppointmentMapper == null) return 0;
        LocalDateTime deadline = LocalDateTime.now().minusHours(CONFIRMED_NO_SHOW_HOURS_AFTER);
        List<String> activeStatuses = Arrays.asList(STATUS_CONFIRMED, STATUS_ASSIGNED, STATUS_CONFIRMED_CN, STATUS_ASSIGNED_CN);
        QueryWrapper<GroomingAppointment> q = new QueryWrapper<>();
        // 服务结束必然早于预约日期 23:59:59，先用日期粗筛缩小候选集；仅取解析所需列
        q.select("id", "date", "time_slot")
         .in("status", activeStatuses).isNotNull("date").isNotNull("time_slot")
         .le("date", deadline.toLocalDate())
         .and(w -> w.isNull("is_verified").or().eq("is_verified", 0));
        List<GroomingAppointment> list = groomingAppointmentMapper.selectList(q);
        if (list == null || list.isEmpty()) return 0;
        List<Long> ids = new ArrayList<>();
        for (GroomingAppointment a : list) {
            LocalDateTime end = parseEndDateTime(a.getDate(), a.getTimeSlot());
            if (end == null || end.isAfter(deadline)) continue;
            ids.add(a.getId());
        }
        if (ids.isEmpty()) return 0;
        // 循环逐条 update 改为单条批量 UPDATE；
        // 罚款金额 = price * 40%（与 CancellationPenaltyUtil.PENALTY_RATE 一致），SQL 内直接计算
        UpdateWrapper<GroomingAppointment> uw = new UpdateWrapper<>();
        uw.in("id", ids)
          .set("status", STATUS_NO_SHOW)
          .set("reject_reason", REASON_NO_SHOW)
          .setSql("cancellation_penalty_amount = ROUND(IFNULL(price, 0) * 0.40, 2)")
          .set("update_time", LocalDateTime.now());
        int count = groomingAppointmentMapper.update(null, uw);
        if (count > 0) {
            logger.info("洗护预约爽约: 共 {} 条（截止时间 {}）", count, deadline);
        }
        return count;
    }

    private int noShowHospital() {
        if (hospitalAppointmentMapper == null) return 0;
        LocalDateTime deadline = LocalDateTime.now().minusHours(CONFIRMED_NO_SHOW_HOURS_AFTER);
        List<String> activeStatuses = Arrays.asList(STATUS_CONFIRMED, STATUS_ASSIGNED, STATUS_CONFIRMED_CN, STATUS_ASSIGNED_CN);
        QueryWrapper<HospitalAppointment> q = new QueryWrapper<>();
        // 服务结束必然早于预约日期 23:59:59，先用日期粗筛缩小候选集；仅取解析所需列
        q.select("id", "date", "time_slot")
         .in("status", activeStatuses).isNotNull("date").isNotNull("time_slot")
         .le("date", deadline.toLocalDate())
         .and(w -> w.isNull("is_verified").or().eq("is_verified", 0));
        List<HospitalAppointment> list = hospitalAppointmentMapper.selectList(q);
        if (list == null || list.isEmpty()) return 0;
        List<Long> ids = new ArrayList<>();
        for (HospitalAppointment a : list) {
            LocalDateTime end = parseEndDateTime(a.getDate(), a.getTimeSlot());
            if (end == null || end.isAfter(deadline)) continue;
            ids.add(a.getId());
        }
        if (ids.isEmpty()) return 0;
        // 循环逐条 update 改为单条批量 UPDATE；
        // 罚款金额 = price * 40%（与 CancellationPenaltyUtil.PENALTY_RATE 一致），SQL 内直接计算
        UpdateWrapper<HospitalAppointment> uw = new UpdateWrapper<>();
        uw.in("id", ids)
          .set("status", STATUS_NO_SHOW)
          .set("reject_reason", REASON_NO_SHOW)
          .setSql("cancellation_penalty_amount = ROUND(IFNULL(price, 0) * 0.40, 2)")
          .set("update_time", LocalDateTime.now());
        int count = hospitalAppointmentMapper.update(null, uw);
        if (count > 0) {
            logger.info("医院预约爽约: 共 {} 条（截止时间 {}）", count, deadline);
        }
        return count;
    }

    private LocalDateTime parseStartDateTime(LocalDate date, String timeSlot) {
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

    private LocalDateTime parseEndDateTime(LocalDate date, String timeSlot) {
        if (date == null || timeSlot == null || timeSlot.isBlank()) return null;
        String[] parts = timeSlot.split("-");
        String endPart = parts.length > 1 ? parts[1].trim() : parts[0].trim();
        if (endPart.isEmpty()) return date.atTime(23, 59);
        try {
            LocalTime t = LocalTime.parse(endPart, DateTimeFormatter.ofPattern("HH:mm"));
            return date.atTime(t);
        } catch (Exception e) {
            try {
                LocalTime t = LocalTime.parse(endPart, DateTimeFormatter.ofPattern("H:mm"));
                return date.atTime(t);
            } catch (Exception e2) {
                return date.atTime(23, 59);
            }
        }
    }
}
