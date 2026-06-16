package com.pethome.service;

import com.pethome.entity.StaffSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 员工排班服务接口
 */
public interface StaffScheduleService {
    
    /**
     * 创建排班
     */
    StaffSchedule createSchedule(StaffSchedule schedule);

    /**
     * 更新排班
     */
    StaffSchedule updateSchedule(StaffSchedule schedule);
    
    /**
     * 根据部门、日期、时间段查找可用的员工ID列表
     */
    List<Long> findAvailableStaffIds(Long departmentId, LocalDate date, LocalTime startTime, LocalTime endTime);
    
    /**
     * 根据员工ID和日期查询排班
     */
    List<StaffSchedule> findByAdminIdAndDate(Long adminId, LocalDate date);
    
    /**
     * 查询排班列表
     */
    List<StaffSchedule> listSchedules(Long adminId, Long departmentId, LocalDate date);

    /**
     * 查询可用排班列表
     */
    List<StaffSchedule> listAvailableSchedules(Long departmentId, LocalDate date);

    /**
     * 根据ID获取排班
     */
    StaffSchedule getById(Long id);

    /**
     * 校验是否存在冲突
     */
    boolean hasConflict(StaffSchedule schedule);

    /**
     * 删除排班
     */
    boolean deleteSchedule(Long id);
}

