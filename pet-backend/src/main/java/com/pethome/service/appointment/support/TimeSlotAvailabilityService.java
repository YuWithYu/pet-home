package com.pethome.service.appointment.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.ServiceSchedule;
import com.pethome.entity.TimeSlot;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.mapper.ServiceScheduleMapper;
import com.pethome.service.appointment.ServiceMemberService;
import com.pethome.service.appointment.TimeSlotService;
import com.pethome.service.common.RedisCacheService;
import com.pethome.util.BookingLeadTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 可预约时段计算：从排班/时段配置汇总剩余名额，并做提前预约时长过滤。
 */
@Component
public class TimeSlotAvailabilityService {

    @Autowired
    private ServiceScheduleMapper scheduleMapper;

    @Autowired
    private ServiceMemberService memberService;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;

    public List<Map<String, Object>> getAvailableTimeSlots(String serviceType, LocalDate date, Long storeId) {
        // 从Redis缓存获取（缓存键包含 storeId，区分不同门店）
        String cacheKey = String.format("schedule:available:%s:%s:%s", 
                date.format(DateTimeFormatter.ISO_DATE), serviceType, storeId != null ? storeId : "all");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cached = (List<Map<String, Object>>) redisCacheService.getCache(cacheKey);
        if (cached != null) {
            return filterSlotsByMinimumAdvance(cached, date, serviceType);
        }

        // 从数据库查询
        List<Map<String, Object>> result = new ArrayList<>();

        // 1. 按门店获取成员：storeId 不为 null 时仅查该门店或平台级成员
        var members = memberService.getMembersByServiceTypeAndStore(serviceType, storeId);

        if (!members.isEmpty()) {
            if (isDoorCleaningScheduleServiceType(serviceType)) {
                // 上门铲屎：名额 = 当日该门店该时段排班总容量 − 未取消/未拒绝的预约单数（含待确认 pending，与业务规则一致）
                result = buildDoorCleaningAvailableSlotsFromMemberSchedules(members, serviceType, date, storeId);
            } else {
                // 洗护/医院等：仍按「可预约」排班行的剩余名额累加
                Set<String> allTimeSlots = new HashSet<>();
                Map<String, Integer> timeSlotCount = new HashMap<>();

                // 批量查询所有成员当天的排班（单次 IN 查询替代逐成员查询，消除 N+1）
                List<Long> memberIds = new ArrayList<>();
                for (var member : members) {
                    if (member.getId() != null) {
                        memberIds.add(member.getId());
                    }
                }
                List<ServiceSchedule> allSchedules = memberIds.isEmpty()
                        ? List.of()
                        : scheduleMapper.selectByMemberIdsAndDate(memberIds, date);
                for (ServiceSchedule schedule : allSchedules) {
                    if (!serviceType.equals(schedule.getServiceType())) {
                        continue;
                    }
                    if ("可预约".equals(schedule.getStatus())
                        && (schedule.getIsAvailable() == null || schedule.getIsAvailable())
                        && (schedule.getReservedCount() == null || schedule.getReservedCount() < schedule.getMaxCapacity())) {
                        String timeSlot = schedule.getTimeSlot();
                        allTimeSlots.add(timeSlot);
                        int maxCap = schedule.getMaxCapacity() != null ? schedule.getMaxCapacity() : 1;
                        int reserved = schedule.getReservedCount() != null ? schedule.getReservedCount() : 0;
                        int available = maxCap - reserved;
                        timeSlotCount.put(timeSlot, timeSlotCount.getOrDefault(timeSlot, 0) + available);
                    }
                }

                result = allTimeSlots.stream()
                        .sorted()
                        .map(timeSlot -> {
                            Map<String, Object> item = new HashMap<>();
                            item.put("timeSlot", timeSlot);
                            item.put("availableCount", timeSlotCount.get(timeSlot));
                            item.put("status", "可预约");
                            return item;
                        })
                        .collect(Collectors.toList());
            }
        }

        // 3. 若服务人员排班为空：指定了 storeId 时不降级（该门店无排班则返回空）；未指定时降级到全局配置
        if (result.isEmpty()) {
            result = getAvailableTimeSlotsFromConfig(serviceType, date, storeId);
        }

        // 4. 洗护/医院/上门铲屎：过滤不满足「至少提前 N 小时」的时段（跨日同样按绝对时间判断）
        result = filterSlotsByMinimumAdvance(result, date, serviceType);

        // 5. 缓存1小时（仅非当天或过滤后的结果）
        redisCacheService.setCache(cacheKey, result, 60);

        return result;
    }

    /**
     * 对洗护/医院/上门铲屎：仅保留「时段开始时间 ≥ 当前时间 + 提前小时数」的选项。
     */
    public List<Map<String, Object>> filterSlotsByMinimumAdvance(
            List<Map<String, Object>> slots, LocalDate date, String serviceType) {
        if (slots == null || slots.isEmpty() || !BookingLeadTimeUtil.appliesScheduledLeadTimeRule(serviceType)) {
            return slots;
        }
        LocalDateTime deadline = LocalDateTime.now().plusHours(BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : slots) {
            Object tsObj = item.get("timeSlot");
            if (tsObj == null) {
                filtered.add(item);
                continue;
            }
            String timeSlotStr = tsObj.toString().trim();
            if (timeSlotStr.isEmpty()) {
                filtered.add(item);
                continue;
            }
            LocalDateTime slotStart = BookingLeadTimeUtil.parseTimeSlotStart(date, timeSlotStr);
            if (slotStart == null) {
                filtered.add(item);
                continue;
            }
            if (!slotStart.isBefore(deadline)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    public List<Map<String, Object>> getAvailableTimeSlotsFromConfig(String serviceType, LocalDate date, Long storeId) {
        List<TimeSlot> activeSlots = timeSlotService.lambdaQuery()
                .eq(TimeSlot::getServiceType, serviceType)
                .eq(storeId != null, TimeSlot::getStoreId, storeId)
                .isNull(storeId == null, TimeSlot::getStoreId)
                .eq(TimeSlot::getIsActive, true)
                .orderByAsc(TimeSlot::getTimeSlot)
                .list();

        if (activeSlots == null || activeSlots.isEmpty()) {
            return new ArrayList<>();
        }

        QueryWrapper<DoorCleaningAppointment> appointmentWrapper = new QueryWrapper<>();
        appointmentWrapper.eq("service_type", serviceType)
                .eq("date", date)
                .notIn("status", Arrays.asList("cancelled", "已取消"));

        List<DoorCleaningAppointment> appointments = doorCleaningAppointmentMapper.selectList(appointmentWrapper);

        Map<String, Long> bookedCount = appointments.stream()
                .filter(a -> a.getTimeSlot() != null && !a.getTimeSlot().trim().isEmpty())
                .collect(Collectors.groupingBy(a -> normalizeTimeSlotKey(a.getTimeSlot()), Collectors.counting()));

        List<Map<String, Object>> fallbackResult = new ArrayList<>();
        for (TimeSlot slot : activeSlots) {
            int maxBookings = slot.getMaxBookings() != null ? slot.getMaxBookings() : 0;
            long used = bookedCount.getOrDefault(normalizeTimeSlotKey(slot.getTimeSlot()), 0L);
            int remaining = Math.max(maxBookings - (int) used, 0);

            if (remaining > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("timeSlot", slot.getTimeSlot());
                item.put("availableCount", remaining);
                item.put("status", "可预约");
                fallbackResult.add(item);
            }
        }

        return fallbackResult;
    }

    /**
     * 上门铲屎：按排班汇总该时段总容量，再减去未取消预约单数量；返回的 timeSlot 使用排班表中的原始字符串便于占坑 SQL 匹配。
     */
    public List<Map<String, Object>> buildDoorCleaningAvailableSlotsFromMemberSchedules(
            List<ServiceMember> members, String serviceType, LocalDate date, Long storeId) {
        Map<String, Integer> totalCapByNormSlot = new HashMap<>();
        Map<String, String> normToCanonicalSlot = new HashMap<>();

        // 批量查询所有成员当天的排班（单次 IN 查询替代逐成员查询，消除 N+1）
        List<Long> memberIds = new ArrayList<>();
        for (var member : members) {
            if (member.getId() != null) {
                memberIds.add(member.getId());
            }
        }
        List<ServiceSchedule> allSchedules = memberIds.isEmpty()
                ? List.of()
                : scheduleMapper.selectByMemberIdsAndDate(memberIds, date);
        for (ServiceSchedule schedule : allSchedules) {
            if (!serviceType.equals(schedule.getServiceType())) {
                continue;
            }
            String st = schedule.getStatus();
            if (st != null && !"可预约".equals(st) && !"不可预约".equals(st)) {
                continue;
            }
            String rawSlot = schedule.getTimeSlot();
            if (rawSlot == null || rawSlot.trim().isEmpty()) {
                continue;
            }
            String canonical = rawSlot.trim();
            String norm = normalizeTimeSlotKey(canonical);
            int mc = schedule.getMaxCapacity() != null ? schedule.getMaxCapacity() : 1;
            totalCapByNormSlot.merge(norm, mc, Integer::sum);
            normToCanonicalSlot.putIfAbsent(norm, canonical);
        }

        Map<String, Long> usedByNorm = countActiveDoorCleaningAppointmentsByNormalizedSlot(serviceType, date, storeId);

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : totalCapByNormSlot.entrySet()) {
            String norm = e.getKey();
            int cap = e.getValue();
            long used = usedByNorm.getOrDefault(norm, 0L);
            int remaining = (int) Math.max(0, cap - used);
            if (remaining <= 0) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("timeSlot", normToCanonicalSlot.getOrDefault(norm, norm));
            item.put("availableCount", remaining);
            item.put("status", "可预约");
            out.add(item);
        }
        out.sort(Comparator.comparing(m -> Objects.toString(m.get("timeSlot"), "")));
        return out;
    }

    public Map<String, Long> countActiveDoorCleaningAppointmentsByNormalizedSlot(String serviceType, LocalDate date, Long storeId) {
        QueryWrapper<DoorCleaningAppointment> w = new QueryWrapper<>();
        w.eq("date", date).notIn("status", Arrays.asList("cancelled", "已取消"));
        if (storeId != null) {
            w.eq("store_id", storeId);
        }
        if ("litter".equals(serviceType)) {
            w.and(q -> q.eq("service_type", "door-cleaning").or().eq("service_type", "litter"));
        } else {
            w.eq("service_type", serviceType);
        }
        List<DoorCleaningAppointment> list = doorCleaningAppointmentMapper.selectList(w);
        return list.stream()
                .filter(a -> a.getTimeSlot() != null && !a.getTimeSlot().trim().isEmpty())
                .collect(Collectors.groupingBy(a -> normalizeTimeSlotKey(a.getTimeSlot()), Collectors.counting()));
    }

    /**
     * 统一时段字符串，避免 "9:00-10:00" 与 "09:00-10:00" 导致名额统计与排班不一致。
     */
    public static String normalizeTimeSlotKey(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim().replace("—", "-").replaceAll("\\s*-\\s*", "-");
        int dash = s.indexOf('-');
        if (dash < 0) {
            return padTimeToken(s);
        }
        String a = s.substring(0, dash).trim();
        String b = s.substring(dash + 1).trim();
        return padTimeToken(a) + "-" + padTimeToken(b);
    }

    public static String padTimeToken(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        DateTimeFormatter flex = DateTimeFormatter.ofPattern("H:mm");
        DateTimeFormatter pad = DateTimeFormatter.ofPattern("HH:mm");
        try {
            return LocalTime.parse(token, flex).format(pad);
        } catch (DateTimeParseException e) {
            try {
                return LocalTime.parse(token, pad).format(pad);
            } catch (DateTimeParseException e2) {
                return token;
            }
        }
    }

    public static boolean isDoorCleaningScheduleServiceType(String serviceType) {
        return serviceType != null && ("door-cleaning".equals(serviceType) || "litter".equals(serviceType));
    }
}
