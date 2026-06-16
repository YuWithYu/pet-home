package com.pethome.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.*;
import com.pethome.mapper.*;
import com.pethome.util.CancellationPenaltyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class AppointmentDelayService extends KeyExpirationEventMessageListener {

    private static final Logger log = LoggerFactory.getLogger(AppointmentDelayService.class);

    public static final String PREFIX_CONFIRM = "apt:confirm:";
    public static final String PREFIX_NOSHOW = "apt:noshow:";
    public static final String PREFIX_ORDER_CANCEL = "order:cancel:";
    public static final String PREFIX_REMIND_24H = "remind:24h:";
    public static final String PREFIX_REMIND_2H = "remind:2h:";

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_CONFIRMED = "confirmed";
    private static final String STATUS_ASSIGNED = "assigned";
    private static final String STATUS_NO_SHOW = "no_show";
    private static final String[] ACTIVE_STATUSES = {STATUS_CONFIRMED, STATUS_ASSIGNED, "\u5df2\u786e\u8ba4", "\u5df2\u5206\u914d"};

    private static final String REASON_AUTO_CONFIRM = "\u7cfb\u7edc\u81ea\u52a8\u5904\u7406\uff1a\u5de5\u4f5c\u4eba\u5458\u672a\u53ca\u65f6\u786e\u8ba4\uff0c\u5df2\u81ea\u52a8\u786e\u8ba4";
    private static final String REASON_NO_SHOW = "\u7cfb\u7edc\u81ea\u52a8\u5904\u7406\uff1a\u670d\u52a1\u65f6\u95f4\u5df2\u8fc7\u4e14\u672a\u6838\u9500\uff0c\u5df2\u6807\u8bb0\u723d\u7ea6";

    @Autowired(required = false)
    private DoorCleaningAppointmentMapper doorCleaningMapper;
    @Autowired(required = false)
    private GroomingAppointmentMapper groomingMapper;
    @Autowired(required = false)
    private HospitalAppointmentMapper hospitalMapper;
    @Autowired(required = false)
    private OrderMapper orderMapper;
    @Autowired(required = false)
    private NotificationService notificationService;
    @Autowired(required = false)
    private AppointmentReminderSentMapper reminderSentMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private RedisCacheService redisCacheService;

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private PointsRecordMapper pointsRecordMapper;

    public AppointmentDelayService(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    public void registerDelayKeys(String serviceType, Long id, LocalDateTime appointmentTime, String timeSlot) {
        if (id == null || appointmentTime == null) return;
        try {
            long secondsToConfirm = ChronoUnit.SECONDS.between(LocalDateTime.now(), appointmentTime);
            if (secondsToConfirm > 0) {
                stringRedisTemplate.opsForValue().set(PREFIX_CONFIRM + serviceType + ":" + id, "1",
                        Duration.ofSeconds(secondsToConfirm));
                log.info("\u6ce8\u518c\u81ea\u52a8\u786e\u8ba4\u5ef6\u65f6: {}:{} \u79d2\u540e\u89e6\u53d1", serviceType, id, secondsToConfirm);
            }
            LocalDateTime noshowTime = parseEndTime(appointmentTime.toLocalDate(), timeSlot).plusHours(2);
            long secondsToNoshow = ChronoUnit.SECONDS.between(LocalDateTime.now(), noshowTime);
            if (secondsToNoshow > 0) {
                stringRedisTemplate.opsForValue().set(PREFIX_NOSHOW + serviceType + ":" + id, "1",
                        Duration.ofSeconds(secondsToNoshow));
                log.info("\u6ce8\u518c\u723d\u7ea6\u5ef6\u65f6: {}:{} \u79d2\u540e\u89e6\u53d1", serviceType, id, secondsToNoshow);
            }
        } catch (Exception e) {
            log.warn("\u6ce8\u518c\u5ef6\u65f6key\u5931\u8d25: {}:{}", serviceType, id, e);
        }
    }

    public void registerOrderCancelKey(Long orderId, int timeoutMinutes) {
        if (orderId == null || timeoutMinutes <= 0) return;
        try {
            stringRedisTemplate.opsForValue().set(PREFIX_ORDER_CANCEL + orderId, "1",
                    Duration.ofMinutes(timeoutMinutes));
            log.info("\u6ce8\u518c\u8ba2\u5355\u8d85\u65f6\u53d6\u6d88: {} \u5206\u949f\u540e\u89e6\u53d1", orderId, timeoutMinutes);
        } catch (Exception e) {
            log.warn("\u6ce8\u518c\u8ba2\u5355\u53d6\u6d88\u5ef6\u65f6key\u5931\u8d25: {}", orderId, e);
        }
    }

    public void cancelOrderCancelKey(Long orderId) {
        if (orderId == null) return;
        try {
            stringRedisTemplate.delete(PREFIX_ORDER_CANCEL + orderId);
        } catch (Exception ignored) {}
    }

    public void registerReminderKeys(String appointmentType, Long appointmentId,
                                     Long userId, LocalDateTime appointmentStart, String serviceName) {
        if (appointmentId == null || userId == null || appointmentStart == null) return;
        try {
            LocalDateTime now = LocalDateTime.now();
            if (!appointmentStart.isBefore(now)) {
                long hours24 = ChronoUnit.HOURS.between(now, appointmentStart.minusHours(24));
                long hours2 = ChronoUnit.HOURS.between(now, appointmentStart.minusHours(2));
                if (hours24 >= -4 && hours24 <= 28) {
                    long sec24 = ChronoUnit.SECONDS.between(now, appointmentStart.minusHours(24));
                    if (sec24 > 0) {
                        String key = PREFIX_REMIND_24H + appointmentType + ":" + appointmentId;
                        stringRedisTemplate.opsForValue().set(key,
                                appointmentType + "|" + appointmentId + "|" + userId + "|" + serviceName + "|24h",
                                Duration.ofSeconds(sec24));
                    }
                }
                if (hours2 >= -4 && hours2 <= 6) {
                    long sec2 = ChronoUnit.SECONDS.between(now, appointmentStart.minusHours(2));
                    if (sec2 > 0) {
                        String key = PREFIX_REMIND_2H + appointmentType + ":" + appointmentId;
                        stringRedisTemplate.opsForValue().set(key,
                                appointmentType + "|" + appointmentId + "|" + userId + "|" + serviceName + "|2h",
                                Duration.ofSeconds(sec2));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("\u6ce8\u518c\u63d0\u9192\u5ef6\u65f6key\u5931\u8d25: {}:{}", appointmentType, appointmentId, e);
        }
    }

    public void cancelDelayKeys(String serviceType, Long id) {
        if (id == null) return;
        try {
            stringRedisTemplate.delete(PREFIX_CONFIRM + serviceType + ":" + id);
            stringRedisTemplate.delete(PREFIX_NOSHOW + serviceType + ":" + id);
            stringRedisTemplate.delete(PREFIX_REMIND_24H + serviceType + ":" + id);
            stringRedisTemplate.delete(PREFIX_REMIND_2H + serviceType + ":" + id);
        } catch (Exception ignored) {}
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        try {
            if (expiredKey.startsWith(PREFIX_CONFIRM)) {
                handleAutoConfirm(expiredKey.substring(PREFIX_CONFIRM.length()));
            } else if (expiredKey.startsWith(PREFIX_NOSHOW)) {
                handleNoShow(expiredKey.substring(PREFIX_NOSHOW.length()));
            } else if (expiredKey.startsWith(PREFIX_ORDER_CANCEL)) {
                handleOrderCancel(expiredKey.substring(PREFIX_ORDER_CANCEL.length()));
            } else if (expiredKey.startsWith(PREFIX_REMIND_24H) || expiredKey.startsWith(PREFIX_REMIND_2H)) {
                String prefix = expiredKey.startsWith(PREFIX_REMIND_24H) ? PREFIX_REMIND_24H : PREFIX_REMIND_2H;
                handleReminder(expiredKey.substring(prefix.length()));
            }
        } catch (Exception e) {
            log.error("\u5904\u7406\u8fc7\u671fkey\u5f02\u5e38: {}", expiredKey, e);
        }
    }

    private void handleAutoConfirm(String keySuffix) {
        String[] parts = keySuffix.split(":");
        if (parts.length < 2) return;
        String type = parts[0];
        Long id = Long.parseLong(parts[1]);
        log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u89e6\u53d1\u81ea\u52a8\u786e\u8ba4: type={}, id={}", type, id);
        switch (type) {
            case "door-cleaning": case "door_cleaning": autoConfirmDoor(id); break;
            case "grooming": autoConfirmGrooming(id); break;
            case "hospital": autoConfirmHospital(id); break;
        }
    }

    private void handleNoShow(String keySuffix) {
        String[] parts = keySuffix.split(":");
        if (parts.length < 2) return;
        String type = parts[0];
        Long id = Long.parseLong(parts[1]);
        log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u89e6\u53d1\u723d\u7ea6: type={}, id={}", type, id);
        switch (type) {
            case "door-cleaning": case "door_cleaning": markNoShowDoor(id); break;
            case "grooming": markNoShowGrooming(id); break;
            case "hospital": markNoShowHospital(id); break;
        }
    }

    private void handleOrderCancel(String keySuffix) {
        try {
            Long orderId = Long.parseLong(keySuffix);
            log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u89e6\u53d1\u8ba2\u5355\u8d85\u65f6\u53d6\u6d88: orderId={}", orderId);
            if (orderMapper == null) return;
            QueryWrapper<Order> q = new QueryWrapper<>();
            q.eq("id", orderId).eq("status", 0);
            Order order = orderMapper.selectOne(q);
            if (order == null) return;
            UpdateWrapper<Order> uw = new UpdateWrapper<>();
            uw.eq("id", orderId).set("status", -1).set("update_time", LocalDateTime.now());
            orderMapper.update(null, uw);
            log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u8ba2\u5358\u5df2\u81ea\u52a8\u53d6\u6d88: orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
        } catch (NumberFormatException ignored) {}
    }

    private void handleReminder(String keySuffix) {
        try {
            String[] parts = keySuffix.split(":");
            if (parts.length < 2) return;
            String aptType = parts[0];
            Long aptId = Long.parseLong(parts[1]);
            log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u89e6\u53d1\u9884\u7ea6\u63d0\u9192: type={}, id={}", aptType, aptId);
            if (notificationService == null || reminderSentMapper == null) return;
            String remindType = keySuffix.contains("remind:24h") ? "24h" : "2h";
            QueryWrapper<AppointmentReminderSent> checkQ = new QueryWrapper<>();
            checkQ.eq("appointment_type", aptType).eq("appointment_id", aptId).eq("remind_type", remindType);
            if (reminderSentMapper.selectCount(checkQ) > 0) return;
            Long userId = null;
            LocalDateTime start = null;
            String svcName = "\u670d\u52a1";
            switch (aptType) {
                case "grooming":
                    if (groomingMapper != null) { GroomingAppointment ga = groomingMapper.selectOne(new QueryWrapper<GroomingAppointment>().eq("id", aptId)); if (ga != null) { userId = ga.getUserId(); start = parseGroomingStart(ga); svcName = "\u6d17\u62a4\u9884\u7ea6"; }}
                    break;
                case "hospital":
                    if (hospitalMapper != null) { HospitalAppointment ha = hospitalMapper.selectOne(new QueryWrapper<HospitalAppointment>().eq("id", aptId)); if (ha != null) { userId = ha.getUserId(); start = parseHospitalStart(ha); svcName = "\u533b\u9662\u9884\u7ea6"; }}
                    break;
                case "door-cleaning": case "door_cleaning":
                    if (doorCleaningMapper != null) { DoorCleaningAppointment da = doorCleaningMapper.selectOne(new QueryWrapper<DoorCleaningAppointment>().eq("id", aptId)); if (da != null) { userId = da.getUserId(); start = da.getAppointmentDate() != null ? da.getAppointmentDate() : null; svcName = "\u4e0a\u95e8\u94f2\u5c4e\u9884\u7ea6"; }}
                    break;
            }
            if (userId == null || start == null) return;
            String titleSuffix = "24h".equals(remindType) ? "24 \u5c0f\u65f6" : "2 \u5c0f\u65f6";
            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle("\u9884\u7ea6\u63d0\u9192");
            n.setContent("\u60a8\u7684" + svcName + "(\u9884\u7ea6\u5355#" + aptId + ")\u5c06\u4e8e " + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " \u5f00\u59cb(\u7ea6" + titleSuffix + "\u540e)\uff0c\u8bf7\u63d0\u524d\u5b89\u6392\u65f6\u95f4\u3002");
            n.setType("appointment_remind");
            n.setStatus(0);
            n.setRelatedId(aptId);
            n.setRelatedType("appointment");
            n.setCreateTime(LocalDateTime.now());
            n.setUpdateTime(LocalDateTime.now());
            notificationService.createNotification(n);
            AppointmentReminderSent sent = new AppointmentReminderSent();
            sent.setAppointmentType(aptType);
            sent.setAppointmentId(aptId);
            sent.setRemindType(remindType);
            sent.setCreatedAt(LocalDateTime.now());
            reminderSentMapper.insert(sent);
            log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u9884\u7ea6\u63d0\u9192\u5df2\u53d1\u9001: type={}, id={}, userId={}, {}", aptType, aptId, userId, remindType);
        } catch (Exception e) {
            log.error("\u9884\u7ea6\u63d0\u9192\u5904\u7406\u5f02\u5e38: {}", keySuffix, e);
        }
    }

    private LocalDateTime parseGroomingStart(GroomingAppointment a) {
        if (a.getDate() == null || a.getTimeSlot() == null) return null;
        String s = a.getTimeSlot().split("-")[0].trim();
        try { LocalTime t = LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm")); return a.getDate().atTime(t); }
        catch (Exception e) { try { LocalTime t = LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm")); return a.getDate().atTime(t); } catch (Exception ignored) {} }
        return a.getDate().atStartOfDay();
    }

    private LocalDateTime parseHospitalStart(HospitalAppointment a) {
        if (a.getDate() == null || a.getTimeSlot() == null) return null;
        String s = a.getTimeSlot().split("-")[0].trim();
        try { LocalTime t = LocalTime.parse(s, DateTimeFormatter.ofPattern("HH:mm")); return a.getDate().atTime(t); }
        catch (Exception e) { try { LocalTime t = LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm")); return a.getDate().atTime(t); } catch (Exception ignored) {} }
        return a.getDate().atStartOfDay();
    }

    private void autoConfirmDoor(Long id) {
        if (doorCleaningMapper == null) return;
        QueryWrapper<DoorCleaningAppointment> q = new QueryWrapper<>();
        q.eq("id", id).eq("status", STATUS_PENDING);
        DoorCleaningAppointment a = doorCleaningMapper.selectOne(q);
        if (a == null) return;
        UpdateWrapper<DoorCleaningAppointment> uw = new UpdateWrapper<>();
        uw.eq("id", id).set("status", STATUS_CONFIRMED).set("reject_reason", REASON_AUTO_CONFIRM).set("update_time", LocalDateTime.now());
        doorCleaningMapper.update(null, uw);
        log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u4e0a\u95e8\u94f2\u5c4e\u81ea\u52a8\u786e\u8ba4: id={}, userId={}", id, a.getUserId());
    }

    private void autoConfirmGrooming(Long id) {
        if (groomingMapper == null) return;
        QueryWrapper<GroomingAppointment> q = new QueryWrapper<>();
        q.eq("id", id).eq("status", STATUS_PENDING);
        GroomingAppointment a = groomingMapper.selectOne(q);
        if (a == null) return;
        UpdateWrapper<GroomingAppointment> uw = new UpdateWrapper<>();
        uw.eq("id", id).set("status", STATUS_CONFIRMED).set("reject_reason", REASON_AUTO_CONFIRM).set("update_time", LocalDateTime.now());
        groomingMapper.update(null, uw);
        log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u6d17\u62a4\u81ea\u52a8\u786e\u8ba4: id={}, userId={}", id, a.getUserId());
    }

    private void autoConfirmHospital(Long id) {
        if (hospitalMapper == null) return;
        QueryWrapper<HospitalAppointment> q = new QueryWrapper<>();
        q.eq("id", id).eq("status", STATUS_PENDING);
        HospitalAppointment a = hospitalMapper.selectOne(q);
        if (a == null) return;
        UpdateWrapper<HospitalAppointment> uw = new UpdateWrapper<>();
        uw.eq("id", id).set("status", STATUS_CONFIRMED).set("reject_reason", REASON_AUTO_CONFIRM).set("update_time", LocalDateTime.now());
        hospitalMapper.update(null, uw);
        log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u533b\u9662\u81ea\u52a8\u786e\u8ba4: id={}, userId={}", id, a.getUserId());
    }

    private void markNoShowDoor(Long id) {
        if (doorCleaningMapper == null) return;
        QueryWrapper<DoorCleaningAppointment> q = new QueryWrapper<>();
        q.eq("id", id).in("status", (Object[]) ACTIVE_STATUSES).and(w -> w.isNull("is_verified").or().eq("is_verified", 0));
        DoorCleaningAppointment a = doorCleaningMapper.selectOne(q);
        if (a == null) return;
        BigDecimal penalty = CancellationPenaltyUtil.calculatePenaltyAmount(a.getPrice());
        UpdateWrapper<DoorCleaningAppointment> uw = new UpdateWrapper<>();
        uw.eq("id", id).set("status", STATUS_NO_SHOW).set("reject_reason", REASON_NO_SHOW + "\uff08\u8fdd\u7ea6\u91d1" + penalty.toPlainString() + "\u5143\uff09").set("cancellation_penalty_amount", penalty).set("update_time", LocalDateTime.now());
        doorCleaningMapper.update(null, uw);
        // 释放时间段锁，允许其他用户预约
        if (redisCacheService != null && a.getAppointmentDate() != null && a.getTimeSlot() != null && a.getMemberId() != null) {
            String cacheKey = "timeslot:door-cleaning:" + a.getAppointmentDate() + ":" + a.getTimeSlot() + ":" + a.getMemberId();
            redisCacheService.deleteCache(cacheKey);
            log.info("\u91ca\u653e\u4e0a\u95e8\u94f2\u5c4e\u65f6\u95f4\u6bb5\u9501: {}", cacheKey);
        }
        log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u4e0a\u95e8\u94f2\u5c4e\u723d\u7ea6: id={}, userId={}, \u8fdd\u7ea6\u91d1{}\u5143", id, a.getUserId(), penalty);
        sendNoShowNotification(a.getUserId(), id, "\u4e0a\u95e8\u94f2\u5c4e", penalty);
    }

    private void markNoShowGrooming(Long id) {
        if (groomingMapper == null) return;
        QueryWrapper<GroomingAppointment> q = new QueryWrapper<>();
        q.eq("id", id).in("status", (Object[]) ACTIVE_STATUSES).and(w -> w.isNull("is_verified").or().eq("is_verified", 0));
        GroomingAppointment a = groomingMapper.selectOne(q);
        if (a == null) return;
        BigDecimal penalty = CancellationPenaltyUtil.calculatePenaltyAmount(a.getPrice());
        UpdateWrapper<GroomingAppointment> uw = new UpdateWrapper<>();
        uw.eq("id", id).set("status", STATUS_NO_SHOW).set("reject_reason", REASON_NO_SHOW + "\uff08\u8fdd\u7ea6\u91d1" + penalty.toPlainString() + "\u5143\uff09").set("cancellation_penalty_amount", penalty).set("update_time", LocalDateTime.now());
        groomingMapper.update(null, uw);
        // 释放时间段锁，允许其他用户预约
        if (redisCacheService != null && a.getDate() != null && a.getTimeSlot() != null && a.getMemberId() != null) {
            String cacheKey = "timeslot:grooming:" + a.getDate() + ":" + a.getTimeSlot() + ":" + a.getMemberId();
            redisCacheService.deleteCache(cacheKey);
            log.info("\u91ca\u653e\u6d17\u62a4\u65f6\u95f4\u6bb5\u9501: {}", cacheKey);
        }
        log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u6d17\u62a4\u723d\u7ea6: id={}, userId={}, \u8fdd\u7ea6\u91d1{}\u5143", id, a.getUserId(), penalty);
        sendNoShowNotification(a.getUserId(), id, "\u6d17\u62a4", penalty);
    }

    private void markNoShowHospital(Long id) {
        if (hospitalMapper == null) return;
        QueryWrapper<HospitalAppointment> q = new QueryWrapper<>();
        q.eq("id", id).in("status", (Object[]) ACTIVE_STATUSES).and(w -> w.isNull("is_verified").or().eq("is_verified", 0));
        HospitalAppointment a = hospitalMapper.selectOne(q);
        if (a == null) return;
        BigDecimal penalty = CancellationPenaltyUtil.calculatePenaltyAmount(a.getPrice());
        UpdateWrapper<HospitalAppointment> uw = new UpdateWrapper<>();
        uw.eq("id", id).set("status", STATUS_NO_SHOW).set("reject_reason", REASON_NO_SHOW + "\uff08\u8fdd\u7ea6\u91d1" + penalty.toPlainString() + "\u5143\uff09").set("cancellation_penalty_amount", penalty).set("update_time", LocalDateTime.now());
        hospitalMapper.update(null, uw);
        // 释放时间段锁，允许其他用户预约
        if (redisCacheService != null && a.getDate() != null && a.getTimeSlot() != null && a.getMemberId() != null) {
            String cacheKey = "timeslot:hospital:" + a.getDate() + ":" + a.getTimeSlot() + ":" + a.getMemberId();
            redisCacheService.deleteCache(cacheKey);
            log.info("\u91ca\u653e\u533b\u9662\u65f6\u95f4\u6bb5\u9501: {}", cacheKey);
        }
        log.info("\u3010\u65f6\u95f4\u9a71\u52a8\u3011\u533b\u9662\u723d\u7ea6: id={}, userId={}, \u8fdd\u7ea6\u91d1{}\u5143", id, a.getUserId(), penalty);
        sendNoShowNotification(a.getUserId(), id, "\u533b\u9662", penalty);
    }

    private void sendNoShowNotification(Long userId, Long appointmentId, String serviceName, BigDecimal penalty) {
        if (notificationService == null || userId == null) return;
        try {
            // 扣除用户积分（违约金转换为积分，1元=100积分）
            if (userService != null && pointsRecordMapper != null && penalty != null && penalty.compareTo(BigDecimal.ZERO) > 0) {
                try {
                    User user = userService.getUserById(userId);
                    if (user != null) {
                        int penaltyPoints = penalty.multiply(new BigDecimal("100")).intValue();
                        int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
                        user.setPoints(Math.max(currentPoints - penaltyPoints, 0));
                        userService.updateUser(user);

                        PointsRecord record = new PointsRecord();
                        record.setUserId(userId);
                        record.setType("spend");
                        record.setPoints(-penaltyPoints);
                        record.setDescription("预约失约违约金扣除（" + serviceName + "预约单#" + appointmentId + "）");
                        record.setCreateTime(LocalDateTime.now());
                        pointsRecordMapper.insert(record);
                        log.info("已扣除违约积分: userId={}, points={}", userId, penaltyPoints);
                    }
                } catch (Exception e) {
                    log.warn("扣除违约积分失败: userId={}", userId, e);
                }
            }

            Notification n = new Notification();
            n.setUserId(userId);
            n.setTitle("预约失约通知");
            n.setContent("您的" + serviceName + "预约(预约单#" + appointmentId + ")已被标记为失约，违约金 " + penalty.toPlainString() + " 元已从账户积分中扣除。如有疑问请联系客服。");
            n.setType("appointment_noshow");
            n.setStatus(0);
            n.setRelatedId(appointmentId);
            n.setRelatedType("appointment");
            n.setCreateTime(LocalDateTime.now());
            n.setUpdateTime(LocalDateTime.now());
            notificationService.createNotification(n);
            log.info("失约通知已发送: userId={}, aptId={}, svc={}, 违约金{}", userId, appointmentId, serviceName, penalty);
        } catch (Exception e) {
            log.warn("发送失约通知失败: {}", appointmentId, e);
        }
    }

    private static LocalDateTime parseEndTime(LocalDate date, String timeSlot) {
        if (date == null || timeSlot == null || timeSlot.isBlank()) return date.atTime(23, 59);
        String[] parts = timeSlot.split("-");
        String endPart = parts.length > 1 ? parts[1].trim() : parts[0].trim();
        try { LocalTime t = LocalTime.parse(endPart, DateTimeFormatter.ofPattern("HH:mm")); return date.atTime(t); }
        catch (Exception e) { try { LocalTime t = LocalTime.parse(endPart, DateTimeFormatter.ofPattern("H:mm")); return date.atTime(t); } catch (Exception ignored) {} }
        return date.atTime(23, 59);
    }
}
