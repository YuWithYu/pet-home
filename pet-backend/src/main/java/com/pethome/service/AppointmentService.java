package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 */
public interface AppointmentService {
    
    /**
     * 创建预约（带冲突检查）
     */
    Appointment createAppointment(Appointment appointment);
    
    /**
     * 更新预约信息
     */
    Appointment updateAppointment(Appointment appointment);
    
    /**
     * 更新预约状态
     */
    Appointment updateAppointmentStatus(Long id, String status);
    
    /**
     * 根据ID查询预约
     */
    Appointment getAppointmentById(Long id);
    
    /**
     * 获取预约列表（分页）
     */
    IPage<Appointment> getAppointmentList(Page<Appointment> page);
    
    /**
     * 获取用户预约列表
     */
    List<Appointment> getUserAppointments(Long userId);
    
    /**
     * 根据服务人员ID查询预约列表
     */
    List<Appointment> getAppointmentsByMemberId(Long memberId);
    
    /**
     * 根据服务类型和日期查询预约列表
     */
    List<Appointment> getAppointmentsByServiceTypeAndDate(String serviceType, LocalDate date);
    
    /**
     * 检查时间段是否可用（防止冲突）
     */
    boolean checkTimeSlotAvailable(String serviceType, LocalDate date, String timeSlot, Long memberId);
    
    /**
     * 分配工单给服务人员
     */
    Appointment assignAppointment(Long appointmentId, Long memberId);
    
    /**
     * 根据日期范围查询预约
     */
    List<Appointment> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 根据核销码查询预约（不核销）
     */
    Appointment getAppointmentByVerifyCode(String verifyCode);

    /**
     * 核销预约（根据核销码）
     */
    Appointment verifyAppointment(String verifyCode);
}
