package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.DoorCleaningAppointment;

import java.util.List;

public interface DoorCleaningAppointmentService extends IService<DoorCleaningAppointment> {
    
    /**
     * 创建上门铲屎预约
     */
    boolean createDoorCleaningAppointment(DoorCleaningAppointment appointment);
    
    /**
     * 根据用户ID获取预约列表
     */
    List<DoorCleaningAppointment> getAppointmentsByUserId(Long userId);
    
    /**
     * 根据ID获取预约详情（带服务人员信息）
     */
    DoorCleaningAppointment getAppointmentById(Long id);
    
    /**
     * 根据服务人员ID获取预约列表
     */
    List<DoorCleaningAppointment> getAppointmentsByMemberId(Long memberId);
    
    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long id, String status);

    /**
     * 更新预约状态（拒绝时可传拒绝原因，供用户端展示）
     */
    boolean updateAppointmentStatus(Long id, String status, String rejectReason);

    /**
     * 同 {@link #updateAppointmentStatus(Long, String, String)}；
     * allowCancelAfterServiceStarted=true 时允许在服务已开始后仍将状态置为已取消（管理端/客服处理特殊情况）。
     */
    boolean updateAppointmentStatus(Long id, String status, String rejectReason, boolean allowCancelAfterServiceStarted);

    /**
     * 分页查询预约列表（storeId 为空时不过滤门店）
     */
    IPage<DoorCleaningAppointment> getAppointmentPage(Page<DoorCleaningAppointment> page,
                                                     String status,
                                                     String keyword,
                                                     String serviceType,
                                                     Long storeId);
}

