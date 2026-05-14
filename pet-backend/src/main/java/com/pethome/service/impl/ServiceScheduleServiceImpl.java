package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.MemberCalendarDayMark;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.ServiceSchedule;
import com.pethome.entity.TimeSlot;
import com.pethome.mapper.MemberCalendarDayMarkMapper;
import com.pethome.mapper.ServiceScheduleMapper;
import com.pethome.service.RedisCacheService;
import com.pethome.service.ServiceMemberService;
import com.pethome.service.ServiceScheduleService;
import com.pethome.service.TimeSlotService;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.util.BookingLeadTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务人员可预约时间表Service实现类
 */
@Service
public class ServiceScheduleServiceImpl implements ServiceScheduleService {

    private static final Logger log = LoggerFactory.getLogger(ServiceScheduleServiceImpl.class);

    /** 分店管理员按「日」排班，不拆时段；库内仍存 time_slot 以满足唯一键，统一用此占位 */
    public static final String STORE_ADMIN_DAY_SLOT = "全天";

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

    @Autowired
    private MemberCalendarDayMarkMapper memberCalendarDayMarkMapper;

    @Override
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

                for (var member : members) {
                    List<ServiceSchedule> schedules = scheduleMapper.selectByMemberAndDate(member.getId(), date);
                    for (ServiceSchedule schedule : schedules) {
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
    private List<Map<String, Object>> filterSlotsByMinimumAdvance(
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

    @Override
    @Transactional
    public ServiceSchedule bookTimeSlot(String serviceType, LocalDate date, String timeSlot, Long taskId, Long storeId) {
        // 使用乐观锁：先查询可用时间段，然后尝试更新（通过状态判断）
        String normalizedTimeSlot = (timeSlot != null ? timeSlot.trim() : "");
        if (BookingLeadTimeUtil.appliesScheduledLeadTimeRule(serviceType)) {
            BookingLeadTimeUtil.assertAtLeastHoursAhead(date, normalizedTimeSlot,
                    BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);
        }
        // 查询该时间段可用的时间段记录（关联服务类型；storeId 不为 null 时仅该门店，各门店独立）
        List<ServiceSchedule> availableSchedules = scheduleMapper.selectAvailableSchedules(
                date, normalizedTimeSlot, serviceType, storeId);

        if (availableSchedules.isEmpty()) {
            log.warn("未查到可预约排班: serviceType={}, date={}, timeSlot=[{}], storeId={}，请核对：1) 管理后台排班是否为此门店/日期/时间段 2) 时间段格式是否一致（如 16:00-17:00）3) 服务人员是否已启用",
                    serviceType, date, timeSlot, storeId);
            throw new RuntimeException("该时间段已被预约或没有可用服务人员，请选择其他时段");
        }

        // 选择第一个可用记录并尝试更新
        ServiceSchedule schedule = availableSchedules.get(0);
        
        // 再次检查状态（双重检查，防止并发）
        ServiceSchedule currentSchedule = scheduleMapper.selectById(schedule.getId());
        if (currentSchedule == null || !"可预约".equals(currentSchedule.getStatus())) {
            throw new RuntimeException("该时间段已被预约，请重新选择");
        }
        
        int updated = scheduleMapper.occupyTimeSlot(schedule.getId(), taskId);

        if (updated == 0) {
            throw new RuntimeException("该时间段已被预约，请重新选择");
        }

        // 清除相关缓存（按门店清除，保证「X人可用」及时更新）
        clearRelatedCacheByStore(date, serviceType, storeId);

        // 返回最新数据
        return scheduleMapper.selectById(schedule.getId());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ServiceSchedule tryBookTimeSlotInNewTransaction(String serviceType, LocalDate date, String timeSlot, Long taskId, Long storeId) {
        return bookTimeSlot(serviceType, date, timeSlot, taskId, storeId);
    }

    private List<Map<String, Object>> getAvailableTimeSlotsFromConfig(String serviceType, LocalDate date, Long storeId) {
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
    
    @Override
    public Map<String, Integer> getScheduleCalendar(String serviceType, LocalDate startDate, LocalDate endDate, Long storeId, Long memberId) {
        Map<String, Integer> calendar = new HashMap<>();
        
        List<ServiceSchedule> schedules;
        if (storeId != null) {
            schedules = scheduleMapper.selectByServiceTypeAndDateRangeAndStore(serviceType, startDate, endDate, storeId);
        } else {
            QueryWrapper<ServiceSchedule> wrapper = new QueryWrapper<>();
            wrapper.eq("service_type", serviceType)
                   .ge("date", startDate)
                   .le("date", endDate);
            schedules = scheduleMapper.selectList(wrapper);
        }
        
        for (ServiceSchedule schedule : schedules) {
            if (memberId != null && (schedule.getMemberId() == null || !memberId.equals(schedule.getMemberId()))) {
                continue;
            }
            String dateStr = schedule.getDate().format(DateTimeFormatter.ISO_DATE);
            calendar.put(dateStr, calendar.getOrDefault(dateStr, 0) + 1);
        }
        
        return calendar;
    }

    /**
     * 是否为分店管理员专用考勤行（日历/请假用），不应出现在「服务排班」按日表格中。
     */
    private boolean isStoreAdminOnlyScheduleRow(ServiceSchedule schedule) {
        if (schedule == null) {
            return false;
        }
        String st = schedule.getServiceType();
        if (st != null && ("store_admin".equalsIgnoreCase(st.trim()) || "store-admin".equalsIgnoreCase(st.trim()))) {
            return true;
        }
        if (schedule.getMemberId() != null) {
            ServiceMember m = memberService.getMemberById(schedule.getMemberId());
            if (m != null && m.getServiceType() != null) {
                String mst = m.getServiceType().trim();
                if ("store_admin".equalsIgnoreCase(mst) || "store-admin".equalsIgnoreCase(mst)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public List<Map<String, Object>> getDaySchedule(String serviceType, LocalDate date, Long storeId, Long memberId) {
        List<ServiceSchedule> schedules = storeId != null
                ? scheduleMapper.selectByServiceTypeAndDateAndStore(serviceType, date, storeId)
                : scheduleMapper.selectByServiceTypeAndDate(serviceType, date);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ServiceSchedule schedule : schedules) {
            if (memberId != null && (schedule.getMemberId() == null || !memberId.equals(schedule.getMemberId()))) {
                continue;
            }
            // 分店管理员「全天」考勤占位（service_type=store_admin）不进入「服务排班」日列表，避免与可接待预约的服务人员混淆；
            // 其出勤在日历/我的排班中仍通过 member 维度展示。
            if (isStoreAdminOnlyScheduleRow(schedule)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", schedule.getId());
            item.put("memberId", schedule.getMemberId());
            item.put("timeSlot", schedule.getTimeSlot());
            item.put("maxCapacity", schedule.getMaxCapacity());
            item.put("reservedCount", schedule.getReservedCount());
            item.put("status", schedule.getStatus());
            item.put("isAvailable", schedule.getIsAvailable());
            item.put("serviceType", schedule.getServiceType());
            item.put("date", schedule.getDate());
            
            // 查询服务人员信息
            if (schedule.getMemberId() != null) {
                var member = memberService.getMemberById(schedule.getMemberId());
                if (member != null) {
                    item.put("memberName", member.getMemberName());
                }
            }
            
            result.add(item);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getMemberCalendarView(Long memberId, LocalDate startDate, LocalDate endDate) {
        if (memberId == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("memberId、startDate、endDate 不能为空");
        }
        Map<String, Object> out = new HashMap<>();
        QueryWrapper<ServiceSchedule> w = new QueryWrapper<>();
        w.eq("member_id", memberId).ge("date", startDate).le("date", endDate);
        List<ServiceSchedule> list = scheduleMapper.selectList(w);
        Map<String, Integer> counts = new HashMap<>();
        for (ServiceSchedule s : list) {
            if (s.getDate() == null) {
                continue;
            }
            String ds = s.getDate().format(DateTimeFormatter.ISO_DATE);
            counts.put(ds, counts.getOrDefault(ds, 0) + 1);
        }
        List<String> leaveDates = new ArrayList<>();
        try {
            List<LocalDate> leaveRaw = memberCalendarDayMarkMapper.selectLeaveDates(memberId, startDate, endDate);
            leaveDates = leaveRaw.stream()
                    .filter(Objects::nonNull)
                    .map(d -> d.format(DateTimeFormatter.ISO_DATE))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("member_calendar_day_mark 表不存在或查询失败，请假标记将不显示。请执行 database/member-calendar-day-mark.sql 建表: {}", e.getMessage());
        }
        out.put("counts", counts);
        out.put("leaveDates", leaveDates);
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setMemberDayAttendance(Long memberId, LocalDate date, String attendance) {
        if (memberId == null || date == null || attendance == null || attendance.isEmpty()) {
            throw new IllegalArgumentException("memberId、date、attendance 不能为空");
        }
        String mode = attendance.trim().toLowerCase();
        if ("leave".equals(mode) || "请假".equals(attendance.trim())) {
            QueryWrapper<ServiceSchedule> listWrapper = new QueryWrapper<>();
            listWrapper.eq("member_id", memberId).eq("date", date);
            List<ServiceSchedule> existing = scheduleMapper.selectList(listWrapper);
            Set<String> types = existing.stream()
                    .map(ServiceSchedule::getServiceType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            scheduleMapper.delete(listWrapper);
            for (String st : types) {
                clearRelatedCache(date, st, memberId);
            }
            if (types.isEmpty()) {
                ServiceMember m = memberService.getMemberById(memberId);
                clearRelatedCache(date, m != null ? m.getServiceType() : null, memberId);
            }
            tryUpsertLeaveDayMark(memberId, date);
            return true;
        }
        if ("work".equals(mode) || "上班".equals(attendance.trim())) {
            tryClearLeaveDayMark(memberId, date);
            // 当日仅保留一条「全天」，先清掉该日其它时段记录再写入
            QueryWrapper<ServiceSchedule> dayClear = new QueryWrapper<>();
            dayClear.eq("member_id", memberId).eq("date", date);
            List<ServiceSchedule> existingDay = scheduleMapper.selectList(dayClear);
            Set<String> dayTypes = existingDay.stream()
                    .map(ServiceSchedule::getServiceType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            scheduleMapper.delete(dayClear);
            for (String st : dayTypes) {
                clearRelatedCache(date, st, memberId);
            }
            if (dayTypes.isEmpty()) {
                ServiceMember m = memberService.getMemberById(memberId);
                clearRelatedCache(date, m != null ? m.getServiceType() : null, memberId);
            }
            return batchCreateSchedules(memberId, date, date, Collections.singletonList(STORE_ADMIN_DAY_SLOT));
        }
        throw new IllegalArgumentException("attendance 须为 work（上班）或 leave（请假）");
    }
    
    @Override
    @Transactional
    public boolean deleteSchedule(Long scheduleId) {
        return scheduleMapper.deleteById(scheduleId) > 0;
    }

    @Override
    public ServiceSchedule getScheduleForSlot(String serviceType, LocalDate date, String timeSlot, Long storeId) {
        if (date == null || timeSlot == null || timeSlot.trim().isEmpty()) {
            return null;
        }
        String normalizedSlot = timeSlot.trim();
        List<ServiceSchedule> schedules = storeId != null
                ? scheduleMapper.selectByServiceTypeAndDateAndStore(serviceType, date, storeId)
                : scheduleMapper.selectByServiceTypeAndDate(serviceType, date);
        return schedules.stream()
                .filter(s -> s.getTimeSlot() != null && s.getTimeSlot().trim().equals(normalizedSlot))
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean releaseTimeSlot(Long scheduleId) {
        ServiceSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            return false;
        }

        schedule.setStatus("可预约");
        schedule.setTaskId(null);
        schedule.setIsAvailable(true);
        schedule.setReservedCount(0);
        scheduleMapper.updateById(schedule);

        // 清除相关缓存
        clearRelatedCache(schedule.getDate(), schedule.getServiceType(), schedule.getMemberId());

        return true;
    }

    @Override
    public List<ServiceSchedule> getMemberSchedule(Long memberId, LocalDate date) {
        return scheduleMapper.selectByMemberAndDate(memberId, date);
    }

    @Override
    @Transactional
    public boolean batchCreateSchedules(Long memberId, LocalDate startDate, LocalDate endDate, List<String> timeSlots) {
        var member = memberService.getMemberById(memberId);
        String serviceType = member != null ? member.getServiceType() : null;
        Integer defaultMaxCapacity = 1;

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            for (String timeSlot : timeSlots) {
                // 检查是否已存在
                QueryWrapper<ServiceSchedule> wrapper = new QueryWrapper<>();
                wrapper.eq("member_id", memberId)
                        .eq("date", current)
                        .eq("time_slot", timeSlot);
                
                ServiceSchedule existing = scheduleMapper.selectOne(wrapper);
                if (existing == null) {
                    ServiceSchedule schedule = new ServiceSchedule();
                    schedule.setMemberId(memberId);
                    schedule.setDate(current);
                    schedule.setTimeSlot(timeSlot);
                    schedule.setStatus("可预约");
                    schedule.setServiceType(serviceType);
                    schedule.setMaxCapacity(defaultMaxCapacity);
                    schedule.setReservedCount(0);
                    schedule.setIsAvailable(true);
                    scheduleMapper.insert(schedule);
                }
            }
            current = current.plusDays(1);
        }
        
        // 清除缓存
        clearRelatedCache(startDate, serviceType, memberId);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGenerateSchedules(String serviceType, LocalDate startDate, LocalDate endDate, int maxCapacity, List<Long> memberIds, List<String> timeSlots, Long storeId) {
        if (serviceType == null || serviceType.isEmpty() || memberIds == null || memberIds.isEmpty() || timeSlots == null || timeSlots.isEmpty()) {
            return 0;
        }
        int inserted = 0;
        for (Long memberId : memberIds) {
            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                for (String timeSlot : timeSlots) {
                    // 与 uk_member_date_slot(member_id, date, time_slot) 一致：不按 store_id 判重，
                    // 否则历史行 store_id 为 NULL 时查不到会重复 INSERT 触发唯一约束冲突
                    QueryWrapper<ServiceSchedule> wrapper = new QueryWrapper<>();
                    wrapper.eq("member_id", memberId).eq("date", current).eq("time_slot", timeSlot);
                    ServiceSchedule existing = scheduleMapper.selectOne(wrapper);
                    if (existing != null) {
                        if (storeId != null && existing.getStoreId() == null) {
                            existing.setStoreId(storeId);
                            scheduleMapper.updateById(existing);
                        }
                        continue;
                    }
                    ServiceSchedule schedule = new ServiceSchedule();
                    schedule.setMemberId(memberId);
                    schedule.setStoreId(storeId);
                    schedule.setDate(current);
                    schedule.setTimeSlot(timeSlot);
                    schedule.setStatus("可预约");
                    schedule.setServiceType(serviceType);
                    schedule.setMaxCapacity(maxCapacity <= 0 ? 1 : maxCapacity);
                    schedule.setReservedCount(0);
                    schedule.setIsAvailable(true);
                    scheduleMapper.insert(schedule);
                    inserted++;
                }
                current = current.plusDays(1);
            }
            clearRelatedCache(startDate, serviceType, memberId);
        }
        return inserted;
    }

    @Override
    @Transactional
    public boolean updateScheduleStatus(Long scheduleId, String status, Long taskId) {
        return scheduleMapper.updateScheduleStatus(scheduleId, status, taskId) > 0;
    }

    @Override
    @Transactional
    public boolean updateSchedule(ServiceSchedule schedule) {
        if (schedule == null || schedule.getId() == null) {
            throw new IllegalArgumentException("排班ID不能为空");
        }
        ServiceSchedule existing = scheduleMapper.selectById(schedule.getId());
        if (existing == null) {
            return false;
        }

        Long originalMemberId = existing.getMemberId();
        LocalDate originalDate = existing.getDate();
        String originalTimeSlot = existing.getTimeSlot();

        if (schedule.getMemberId() != null) {
            existing.setMemberId(schedule.getMemberId());
        }
        if (schedule.getServiceType() != null) {
            existing.setServiceType(schedule.getServiceType());
        }
        if (schedule.getDate() != null) {
            existing.setDate(schedule.getDate());
        }
        if (schedule.getTimeSlot() != null) {
            existing.setTimeSlot(schedule.getTimeSlot());
        }
        if (schedule.getMaxCapacity() != null) {
            existing.setMaxCapacity(schedule.getMaxCapacity());
        }
        if (schedule.getReservedCount() != null) {
            existing.setReservedCount(schedule.getReservedCount());
        }
        if (schedule.getStatus() != null) {
            existing.setStatus(schedule.getStatus());
        }
        if (schedule.getIsAvailable() != null) {
            existing.setIsAvailable(schedule.getIsAvailable());
        } else {
            existing.setIsAvailable("可预约".equals(existing.getStatus()));
        }

        if (existing.getReservedCount() != null && existing.getReservedCount() < 0) {
            existing.setReservedCount(0);
        }
        if (existing.getMaxCapacity() != null && existing.getReservedCount() != null
                && existing.getReservedCount() > existing.getMaxCapacity()) {
            existing.setReservedCount(existing.getMaxCapacity());
        }

        boolean memberChanged = !Objects.equals(originalMemberId, existing.getMemberId());
        boolean dateChanged = !Objects.equals(originalDate, existing.getDate());
        boolean timeSlotChanged = !Objects.equals(originalTimeSlot, existing.getTimeSlot());

        if (memberChanged || dateChanged || timeSlotChanged) {
            QueryWrapper<DoorCleaningAppointment> wrapper = new QueryWrapper<>();
            wrapper.eq("service_type", existing.getServiceType())
                    .eq("time_slot", originalTimeSlot)
                    .eq("date", originalDate);
            if (originalMemberId != null) {
                wrapper.eq("member_id", originalMemberId);
            }
            wrapper.notIn("status", Arrays.asList("cancelled", "已取消", "completed", "已完成"));
            DoorCleaningAppointment appointment = doorCleaningAppointmentMapper.selectOne(wrapper);
            if (appointment != null) {
                throw new IllegalArgumentException("该排班已有预约，无法修改服务人员/日期/时间段");
            }
        }

        existing.setUpdateTime(LocalDateTime.now());
        int updated = scheduleMapper.updateById(existing);
        if (updated > 0) {
            clearRelatedCache(existing.getDate(), existing.getServiceType(), existing.getMemberId());
            return true;
        }
        return false;
    }

    /** 删除当日排班后写入请假标记；表未创建时忽略，不影响排班删除提交 */
    private void tryUpsertLeaveDayMark(Long memberId, LocalDate date) {
        try {
            QueryWrapper<MemberCalendarDayMark> mw = new QueryWrapper<>();
            mw.eq("member_id", memberId).eq("mark_date", date);
            memberCalendarDayMarkMapper.delete(mw);
            MemberCalendarDayMark mark = new MemberCalendarDayMark();
            mark.setMemberId(memberId);
            mark.setMarkDate(date);
            mark.setMarkType("leave");
            memberCalendarDayMarkMapper.insert(mark);
        } catch (Exception e) {
            log.warn("member_calendar_day_mark 表未创建或写入失败，已跳过请假标记。可在库中执行 database/member-calendar-day-mark.sql: {}", e.getMessage());
        }
    }

    private void tryClearLeaveDayMark(Long memberId, LocalDate date) {
        try {
            QueryWrapper<MemberCalendarDayMark> clearLeave = new QueryWrapper<>();
            clearLeave.eq("member_id", memberId).eq("mark_date", date);
            memberCalendarDayMarkMapper.delete(clearLeave);
        } catch (Exception e) {
            log.warn("member_calendar_day_mark 清除标记失败（可忽略）: {}", e.getMessage());
        }
    }

    /**
     * 清除相关缓存（按门店，保证可用人数及时更新）
     */
    private void clearRelatedCacheByStore(LocalDate date, String serviceType, Long storeId) {
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        if (serviceType != null) {
            redisCacheService.deleteCache(String.format("schedule:available:%s:%s:all", dateStr, serviceType));
            if (storeId != null) {
                redisCacheService.deleteCache(String.format("schedule:available:%s:%s:%s", dateStr, serviceType, storeId));
            }
        }
    }

    /**
     * 清除相关缓存（按成员，用于排班更新等）
     */
    private void clearRelatedCache(LocalDate date, String serviceType, Long memberId) {
        String dateStr = date.format(DateTimeFormatter.ISO_DATE);
        if (serviceType != null) {
            redisCacheService.deleteCache(String.format("schedule:available:%s:%s:all", dateStr, serviceType));
        }
        if (memberId != null) {
            String workloadKey = String.format("member:workload:%d:%s", memberId, dateStr);
            redisCacheService.deleteCache(workloadKey);
        }
    }

    @Override
    public void invalidateAvailableTimeSlotsCache(LocalDate date, String serviceType, Long storeId) {
        if (date == null) {
            return;
        }
        String st = serviceType != null && !serviceType.isEmpty() ? serviceType : "door-cleaning";
        clearRelatedCacheByStore(date, st, storeId);
    }

    private static boolean isDoorCleaningScheduleServiceType(String serviceType) {
        return serviceType != null && ("door-cleaning".equals(serviceType) || "litter".equals(serviceType));
    }

    /**
     * 上门铲屎：按排班汇总该时段总容量，再减去未取消预约单数量；返回的 timeSlot 使用排班表中的原始字符串便于占坑 SQL 匹配。
     */
    private List<Map<String, Object>> buildDoorCleaningAvailableSlotsFromMemberSchedules(
            List<ServiceMember> members, String serviceType, LocalDate date, Long storeId) {
        Map<String, Integer> totalCapByNormSlot = new HashMap<>();
        Map<String, String> normToCanonicalSlot = new HashMap<>();

        for (var member : members) {
            List<ServiceSchedule> schedules = scheduleMapper.selectByMemberAndDate(member.getId(), date);
            for (ServiceSchedule schedule : schedules) {
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

    private Map<String, Long> countActiveDoorCleaningAppointmentsByNormalizedSlot(String serviceType, LocalDate date, Long storeId) {
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
    private static String normalizeTimeSlotKey(String raw) {
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

    private static String padTimeToken(String token) {
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
}

