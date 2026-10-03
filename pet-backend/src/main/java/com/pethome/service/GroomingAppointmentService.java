package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 根据服务人员ID获取预约列表
     */
    List<GroomingAppointment> getAppointmentsByMemberId(Long memberId);

    /**
     * 分页查询预约（storeId 为空时不过滤门店）
     */
    IPage<GroomingAppointment> getAppointmentPage(Page<GroomingAppointment> page, String status, String keyword, String serviceType, Long storeId);
    
    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long id, String status);

    /**
     * 更新预约状态（拒绝时可传拒绝原因，供用户端展示）
     */
    boolean updateAppointmentStatus(Long id, String status, String rejectReason);

    boolean updateAppointmentStatus(Long id, String status, String rejectReason, boolean allowCancelAfterServiceStarted);
}

