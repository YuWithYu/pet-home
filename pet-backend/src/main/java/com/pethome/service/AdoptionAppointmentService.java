package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.AdoptionAppointment;

import java.util.List;

public interface AdoptionAppointmentService extends IService<AdoptionAppointment> {
    
    /**
     * 创建宠物领养预约
     */
    boolean createAdoptionAppointment(AdoptionAppointment appointment);
    
    /**
     * 根据用户ID获取预约列表
     */
    List<AdoptionAppointment> getAppointmentsByUserId(Long userId);
    
    /**
     * 根据ID获取预约详情
     */
    AdoptionAppointment getAppointmentById(Long id);
    
    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long id, String status);
}

