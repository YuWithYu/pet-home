package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.GroomingAppointment;

import java.util.List;

public interface GroomingAppointmentService extends IService<GroomingAppointment> {
    
    /**
     * 创建宠物洗护预约
     */
    boolean createGroomingAppointment(GroomingAppointment appointment);
    
    /**
     * 根据用户ID获取预约列表
     */
    List<GroomingAppointment> getAppointmentsByUserId(Long userId);
    
    /**
     * 根据ID获取预约详情
     */
    GroomingAppointment getAppointmentById(Long id);
    
    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long id, String status);
}

