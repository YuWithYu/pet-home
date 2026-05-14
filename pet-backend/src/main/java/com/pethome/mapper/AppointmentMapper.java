package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约 Mapper 接口
 */
@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {
    
    /**
     * 根据服务人员ID查询预约列表
     */
    List<Appointment> selectByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 根据管理员ID查询预约列表
     */
    List<Appointment> selectByAdminId(@Param("adminId") Long adminId);
    
    /**
     * 根据服务类型和日期查询预约列表
     */
    List<Appointment> selectByServiceTypeAndDate(@Param("serviceType") String serviceType, 
                                                   @Param("date") LocalDate date);
    
    /**
     * 检查时间段是否可用（防止冲突）
     */
    int countByServiceTypeAndDateTime(@Param("serviceType") String serviceType,
                                      @Param("date") LocalDate date,
                                      @Param("timeSlot") String timeSlot,
                                      @Param("memberId") Long memberId);
    
    /**
     * 根据日期范围查询预约
     */
    List<Appointment> selectByDateRange(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
    
    /**
     * 根据状态查询预约列表
     */
    List<Appointment> selectByStatus(@Param("status") String status);
}
