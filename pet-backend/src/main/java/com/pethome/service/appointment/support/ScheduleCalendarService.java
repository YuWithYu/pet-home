package com.pethome.service.appointment.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.BusinessException;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.ServiceSchedule;
import com.pethome.mapper.MemberCalendarDayMarkMapper;
import com.pethome.mapper.ServiceMemberMapper;
import com.pethome.mapper.ServiceScheduleMapper;
import com.pethome.service.appointment.ServiceMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排班日历与按日排班查询。
 */
@Component
public class ScheduleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleCalendarService.class);

    @Autowired
    private ServiceScheduleMapper scheduleMapper;

    @Autowired
    private ServiceMemberService memberService;

    /** 批量查询服务人员（getDaySchedule 防 N+1 专用） */
    @Autowired
    private ServiceMemberMapper serviceMemberMapper;

    @Autowired
    private MemberCalendarDayMarkMapper memberCalendarDayMarkMapper;

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
    
    public List<Map<String, Object>> getDaySchedule(String serviceType, LocalDate date, Long storeId, Long memberId) {
        List<ServiceSchedule> schedules = storeId != null
                ? scheduleMapper.selectByServiceTypeAndDateAndStore(serviceType, date, storeId)
                : scheduleMapper.selectByServiceTypeAndDate(serviceType, date);
        List<Map<String, Object>> result = new ArrayList<>();

        // 性能修复：先批量收集 memberId，一次查出所有服务人员（替代循环内逐条 getMemberById 的 N+1 查询）
        Set<Long> memberIds = new HashSet<>();
        for (ServiceSchedule schedule : schedules) {
            if (schedule.getMemberId() != null) {
                memberIds.add(schedule.getMemberId());
            }
        }
        Map<Long, ServiceMember> memberMap = new HashMap<>();
        if (!memberIds.isEmpty()) {
            for (ServiceMember m : serviceMemberMapper.selectBatchIds(memberIds)) {
                memberMap.put(m.getId(), m);
            }
        }

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

            // 填充服务人员信息（从预查的 Map 中取，无额外查询）
            if (schedule.getMemberId() != null) {
                ServiceMember member = memberMap.get(schedule.getMemberId());
                if (member != null) {
                    item.put("memberName", member.getMemberName());
                }
            }

            result.add(item);
        }

        return result;
    }

    public Map<String, Object> getMemberCalendarView(Long memberId, LocalDate startDate, LocalDate endDate) {
        if (memberId == null || startDate == null || endDate == null) {
            throw new BusinessException("memberId、startDate、endDate 不能为空");
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
}
