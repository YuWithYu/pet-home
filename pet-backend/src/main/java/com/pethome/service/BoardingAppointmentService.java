package com.pethome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.BoardingAppointment;

import java.util.List;

public interface BoardingAppointmentService extends IService<BoardingAppointment> {
    
    /**
     * 创建宠物寄养预约
     */
    boolean createBoardingAppointment(BoardingAppointment appointment);
    
    /**
     * 根据用户ID获取预约列表
     */
    List<BoardingAppointment> getAppointmentsByUserId(Long userId);
    
    /**
     * 根据ID获取预约详情
     */
    BoardingAppointment getAppointmentById(Long id);
    
    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long id, String status);
}

