package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.BusinessException;
import com.pethome.common.ServiceTypeCatalog;
import com.pethome.entity.Admin;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.MemberCalendarDayMark;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.ServiceSchedule;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.mapper.MemberCalendarDayMarkMapper;
import com.pethome.mapper.ServiceScheduleMapper;
import com.pethome.service.admin.AdminService;
import com.pethome.service.appointment.ServiceMemberService;
import com.pethome.service.appointment.ServiceScheduleService;
import com.pethome.service.appointment.support.ScheduleCacheService;
import com.pethome.service.appointment.support.ScheduleCalendarService;
import com.pethome.service.appointment.support.TimeSlotAvailabilityService;
import com.pethome.util.BookingLeadTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;

    @Autowired
    private MemberCalendarDayMarkMapper memberCalendarDayMarkMapper;

    @Autowired
    private AdminService adminService;

    @Autowired
    private TimeSlotAvailabilityService timeSlotAvailabilityService;

    @Autowired
    private ScheduleCalendarService scheduleCalendarService;

    @Autowired
    private ScheduleCacheService scheduleCacheService;

    @Override
    public List<Map<String, Object>> getAvailableTimeSlots(String serviceType, LocalDate date, Long storeId) {
        return timeSlotAvailabilityService.getAvailableTimeSlots(serviceType, date, storeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        scheduleCacheService.clearRelatedCacheByStore(date, serviceType, storeId);

        // 返回最新数据
        return scheduleMapper.selectById(schedule.getId());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public ServiceSchedule tryBookTimeSlotInNewTransaction(String serviceType, LocalDate date, String timeSlot, Long taskId, Long storeId) {
        return bookTimeSlot(serviceType, date, timeSlot, taskId, storeId);
    }

    @Override
    public Map<String, Integer> getScheduleCalendar(String serviceType, LocalDate startDate, LocalDate endDate, Long storeId, Long memberId) {
        return scheduleCalendarService.getScheduleCalendar(serviceType, startDate, endDate, storeId, memberId);
    }

    @Override
    public List<Map<String, Object>> getDaySchedule(String serviceType, LocalDate date, Long storeId, Long memberId) {
        return scheduleCalendarService.getDaySchedule(serviceType, date, storeId, memberId);
    }

    @Override
    public Map<String, Object> getMemberCalendarView(Long memberId, LocalDate startDate, LocalDate endDate) {
        return scheduleCalendarService.getMemberCalendarView(memberId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setMemberDayAttendance(Long memberId, LocalDate date, String attendance) {
        if (memberId == null || date == null || attendance == null || attendance.isEmpty()) {
            throw new BusinessException("memberId、date、attendance 不能为空");
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
                scheduleCacheService.clearRelatedCache(date, st, memberId);
            }
            if (types.isEmpty()) {
                ServiceMember m = memberService.getMemberById(memberId);
                scheduleCacheService.clearRelatedCache(date, m != null ? m.getServiceType() : null, memberId);
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
                scheduleCacheService.clearRelatedCache(date, st, memberId);
            }
            if (dayTypes.isEmpty()) {
                ServiceMember m = memberService.getMemberById(memberId);
                scheduleCacheService.clearRelatedCache(date, m != null ? m.getServiceType() : null, memberId);
            }
            return batchCreateSchedules(memberId, date, date, Collections.singletonList(STORE_ADMIN_DAY_SLOT));
        }
        throw new BusinessException("attendance 须为 work（上班）或 leave（请假）");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchedule(Long scheduleId) {
        return scheduleMapper.deleteById(scheduleId) > 0;
    }

    @Override
    public ServiceSchedule getScheduleById(Long scheduleId) {
        return scheduleId == null ? null : scheduleMapper.selectById(scheduleId);
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
    @Transactional(rollbackFor = Exception.class)
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
        scheduleCacheService.clearRelatedCache(schedule.getDate(), schedule.getServiceType(), schedule.getMemberId());

        return true;
    }

    @Override
    public List<ServiceSchedule> getMemberSchedule(Long memberId, LocalDate date) {
        return scheduleMapper.selectByMemberAndDate(memberId, date);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        scheduleCacheService.clearRelatedCache(startDate, serviceType, memberId);
        
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
            scheduleCacheService.clearRelatedCache(startDate, serviceType, memberId);
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateScheduleStatus(Long scheduleId, String status, Long taskId) {
        return scheduleMapper.updateScheduleStatus(scheduleId, status, taskId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSchedule(ServiceSchedule schedule) {
        if (schedule == null || schedule.getId() == null) {
            throw new BusinessException("排班ID不能为空");
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
                throw new BusinessException("该排班已有预约，无法修改服务人员/日期/时间段");
            }
        }

        existing.setUpdateTime(LocalDateTime.now());
        int updated = scheduleMapper.updateById(existing);
        if (updated > 0) {
            scheduleCacheService.clearRelatedCache(existing.getDate(), existing.getServiceType(), existing.getMemberId());
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSchedule(ServiceSchedule schedule, Long adminId) {
        if (schedule != null && schedule.getServiceType() == null && adminId != null) {
            Admin admin = adminService.getById(adminId);
            if (admin != null) {
                String serviceType = ServiceTypeCatalog.serviceTypeByDepartment(admin.getDepartment());
                if (serviceType != null) {
                    schedule.setServiceType(serviceType);
                }
            }
        }
        return updateSchedule(schedule);
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

    @Override
    public void invalidateAvailableTimeSlotsCache(LocalDate date, String serviceType, Long storeId) {
        scheduleCacheService.invalidateAvailableTimeSlotsCache(date, serviceType, storeId);
    }
}
