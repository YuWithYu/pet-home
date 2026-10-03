package com.pethome.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 预约取消规则（与业务文案一致）：
 * <ul>
 *   <li>服务开始时间前 2 小时以外：可免费取消（不记违约金）</li>
 *   <li>服务开始前 0～2 小时内：取消扣除订单金额的 40% 作为违约金</li>
 *   <li>服务已开始后：用户不可在线取消；特殊情况由客服/后台处理（需 force 参数绕过）</li>
 * </ul>
 */
public final class CancellationPenaltyUtil {

    private static final int FREE_CANCEL_HOURS_BEFORE_START = 2;
    private static final BigDecimal PENALTY_RATE = new BigDecimal("0.40");
    private static final DateTimeFormatter FLEX_TIME = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter PAD_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private CancellationPenaltyUtil() {
    }

    /**
     * 服务是否已开始（当前时间 ≥ 时段开始时间）
     */
    public static boolean isServiceStarted(LocalDate serviceDate, String timeSlot) {
        LocalDateTime start = parseServiceStart(serviceDate, timeSlot);
        if (start == null) {
            return false;
        }
        return !LocalDateTime.now().isBefore(start);
    }

    /**
     * 用户在线申请取消 / 工作人员同意取消申请时：若服务已开始则不允许（应联系客服）。
     */
    public static void assertUserCancellationNotAfterServiceStart(LocalDate serviceDate, String timeSlot) {
        if (serviceDate == null || timeSlot == null || timeSlot.trim().isEmpty()) {
            return;
        }
        if (isServiceStarted(serviceDate, timeSlot)) {
            throw new IllegalArgumentException("服务已开始或已结束，无法在线取消；如有特殊情况请联系客服处理。");
        }
    }

    /**
     * 是否处于「服务开始前 0～2 小时」内（此时取消须扣 40% 违约金）。按当前时刻判断。
     */
    public static boolean isInPenaltyWindow(LocalDate serviceDate, String timeSlot) {
        LocalDateTime serviceStart = parseServiceStart(serviceDate, timeSlot);
        if (serviceStart == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = serviceStart.minusHours(FREE_CANCEL_HOURS_BEFORE_START);
        return !now.isBefore(windowStart) && now.isBefore(serviceStart);
    }

    /**
     * 是否属于「提前满 2 小时」的免费取消区间（当前时刻早于 服务开始−2h）。
     */
    public static boolean isInFreeCancellationWindow(LocalDate serviceDate, String timeSlot) {
        LocalDateTime serviceStart = parseServiceStart(serviceDate, timeSlot);
        if (serviceStart == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime freeUntil = serviceStart.minusHours(FREE_CANCEL_HOURS_BEFORE_START);
        return now.isBefore(freeUntil);
    }

    public static LocalDateTime parseServiceStart(LocalDate serviceDate, String timeSlot) {
        if (serviceDate == null || timeSlot == null) {
            return null;
        }
        String s = timeSlot.trim();
        if (s.isEmpty()) {
            return null;
        }
        int dash = s.indexOf('-');
        String startStr = dash >= 0 ? s.substring(0, dash).trim() : s;
        LocalTime startTime;
        try {
            startTime = LocalTime.parse(startStr, FLEX_TIME);
        } catch (DateTimeParseException e) {
            try {
                startTime = LocalTime.parse(startStr, PAD_TIME);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
        return LocalDateTime.of(serviceDate, startTime);
    }

    public static BigDecimal calculatePenaltyAmount(BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return orderAmount.multiply(PENALTY_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public static int getFreeCancelHoursBeforeStart() {
        return FREE_CANCEL_HOURS_BEFORE_START;
    }
}
