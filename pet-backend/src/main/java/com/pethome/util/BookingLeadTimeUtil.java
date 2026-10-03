package com.pethome.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 预约提前量：洗护、医院、上门铲屎等按时段预约的服务，需至少提前若干小时下单。
 */
public final class BookingLeadTimeUtil {

    public static final int SCHEDULED_SERVICE_MIN_ADVANCE_HOURS = 2;

    private static final DateTimeFormatter FLEX_TIME = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter PAD_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private BookingLeadTimeUtil() {
    }

    /**
     * 是否对「可预约时段列表 / 占坑」应用提前量规则（与小程序三类预约一致）。
     */
    public static boolean appliesScheduledLeadTimeRule(String serviceType) {
        if (serviceType == null) {
            return false;
        }
        return "door-cleaning".equals(serviceType)
                || "door_cleaning".equals(serviceType)
                || "litter".equals(serviceType)
                || "grooming".equals(serviceType)
                || "hospital".equals(serviceType);
    }

    /**
     * 解析时段开始时刻：支持 "10:00-11:00" 或单段 "10:00"。
     */
    public static LocalDateTime parseTimeSlotStart(LocalDate date, String timeSlot) {
        if (date == null || timeSlot == null) {
            return null;
        }
        String ts = timeSlot.trim();
        if (ts.isEmpty()) {
            return null;
        }
        int dash = ts.indexOf('-');
        String startStr = dash >= 0 ? ts.substring(0, dash).trim() : ts;
        LocalTime start;
        try {
            start = LocalTime.parse(startStr, FLEX_TIME);
        } catch (DateTimeParseException e) {
            try {
                start = LocalTime.parse(startStr, PAD_TIME);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
        return LocalDateTime.of(date, start);
    }

    /**
     * 校验：服务开始时间须不早于「当前时间 + hours 小时」（含整点边界：恰等于允许）。
     */
    public static void assertAtLeastHoursAhead(LocalDate date, String timeSlot, int hours) {
        if (date == null || timeSlot == null || timeSlot.trim().isEmpty()) {
            return;
        }
        LocalDateTime slotStart = parseTimeSlotStart(date, timeSlot.trim());
        if (slotStart == null) {
            throw new IllegalArgumentException("预约时段格式无效，请重新选择");
        }
        LocalDateTime deadline = LocalDateTime.now().plusHours(hours);
        if (slotStart.isBefore(deadline)) {
            throw new IllegalArgumentException("需提前至少" + hours + "小时预约，请重新选择日期或时段");
        }
    }
}
