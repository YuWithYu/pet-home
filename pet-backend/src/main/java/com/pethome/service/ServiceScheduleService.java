package com.pethome.service;

import com.pethome.entity.ServiceSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 服务人员可预约时间表Service接口
 */
public interface ServiceScheduleService {
    
    /**
     * 查询指定日期和服务类型的可预约时间段
     * @param storeId 门店ID，为 null 时查全部门店；传入时仅返回该门店有排班的时间段，无排班则返回空
     */
    List<Map<String, Object>> getAvailableTimeSlots(String serviceType, LocalDate date, Long storeId);
    
    /**
     * 预约指定时间段（包含自动分配服务人员）
     * @param storeId 门店ID，不为 null 时仅占用该门店的排班（各门店独立）；null 时不限门店
     */
    ServiceSchedule bookTimeSlot(String serviceType, LocalDate date, String timeSlot, Long taskId, Long storeId);

    /**
     * 在新事务中尝试占用时间段，失败时只回滚本事务，不影响调用方事务（用于预约创建：先保存预约再占排班，占排班失败仍保留预约）
     */
    ServiceSchedule tryBookTimeSlotInNewTransaction(String serviceType, LocalDate date, String timeSlot, Long taskId, Long storeId);
    
    /**
     * 释放时间段（取消预约时）
     */
    boolean releaseTimeSlot(Long scheduleId);
    
    /**
     * 查询服务人员在指定日期的日程
     */
    List<ServiceSchedule> getMemberSchedule(Long memberId, LocalDate date);
    
    /**
     * 批量创建服务人员的时间段（用于排班）
     */
    boolean batchCreateSchedules(Long memberId, LocalDate startDate, LocalDate endDate, List<String> timeSlots);

    /**
     * 批量生成排班：按服务类型、日期范围、多个人员、多个时段，生成 service_schedule 记录（不重复插入）
     * @param storeId 门店ID，用于绑定排班归属，避免团队保存时覆盖成员 store_id 导致跨店排班消失
     * @return 本次新增条数
     */
    int batchGenerateSchedules(String serviceType, LocalDate startDate, LocalDate endDate, int maxCapacity, List<Long> memberIds, List<String> timeSlots, Long storeId);
    
    /**
     * 更新时间段状态
     */
    boolean updateScheduleStatus(Long scheduleId, String status, Long taskId);
    
    /**
     * 获取排班日历统计（日期 -> 排班数量）
     * @param storeId 门店ID，为 null 时查全部门店；传入时仅统计该门店的排班
     */
    Map<String, Integer> getScheduleCalendar(String serviceType, LocalDate startDate, LocalDate endDate, Long storeId, Long memberId);
    
    /**
     * 获取指定日期的排班详情（包含服务人员信息）
     * @param storeId 门店ID，为 null 时查全部门店；传入时仅返回该门店的排班
     * @param memberId 服务人员/分店管理员成员ID，非空时仅返回该成员的排班
     */
    List<Map<String, Object>> getDaySchedule(String serviceType, LocalDate date, Long storeId, Long memberId);

    /**
     * 平台管理员为分店管理员设置某日出勤：work=按平台「分店管理员」时段模板生成排班；leave=删除该日全部排班
     */
    boolean setMemberDayAttendance(Long memberId, LocalDate date, String attendance);

    /**
     * 按服务人员汇总月历：counts=日期→排班条数（含 store_admin 等所有类型），leaveDates=标记为请假的日期
     */
    Map<String, Object> getMemberCalendarView(Long memberId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 更新排班
     */
    boolean updateSchedule(ServiceSchedule schedule);

    /**
     * 删除排班
     */
    boolean deleteSchedule(Long scheduleId);

    /**
     * 根据日期、时间段、门店、服务类型查询排班（用于补全预约的服务人员显示，不要求可用）
     * @return 匹配的排班，若无则 null
     */
    ServiceSchedule getScheduleForSlot(String serviceType, LocalDate date, String timeSlot, Long storeId);

    /**
     * 清除「可预约时段」Redis 缓存（预约状态变更、拒绝订单等场景需调用，避免名额显示滞后）
     */
    void invalidateAvailableTimeSlotsCache(LocalDate date, String serviceType, Long storeId);
}

