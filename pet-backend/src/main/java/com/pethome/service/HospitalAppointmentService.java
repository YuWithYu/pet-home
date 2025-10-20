package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.HospitalAppointment;

import java.util.List;

public interface HospitalAppointmentService extends IService<HospitalAppointment> {
    
    /**
     * 创建宠物医院预约
     */
    boolean createHospitalAppointment(HospitalAppointment appointment);
    
    /**
     * 根据用户ID获取预约列表
     */
    List<HospitalAppointment> getAppointmentsByUserId(Long userId);
    
    /**
     * 根据ID获取预约详情
     */
    HospitalAppointment getAppointmentById(Long id);
    
    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long id, String status);
}
