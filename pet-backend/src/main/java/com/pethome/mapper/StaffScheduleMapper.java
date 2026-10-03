package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.StaffSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 员工排班Mapper
 */
@Mapper
public interface StaffScheduleMapper extends BaseMapper<StaffSchedule> {
    
    /**
     * 根据部门、日期、时间段查找可用的员工
     * @param departmentId 部门ID
     * @param date 日期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 可用的员工ID列表
     */
    List<Long> findAvailableStaffIds(
        @Param("departmentId") Long departmentId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime
    );
    
    /**
     * 根据员工ID和日期查询排班
     */
    List<StaffSchedule> findByAdminIdAndDate(
        @Param("adminId") Long adminId,
        @Param("date") LocalDate date
    );
}

