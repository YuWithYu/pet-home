package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 根据服务人员ID获取预约列表
     */
    List<HospitalAppointment> getAppointmentsByMemberId(Long memberId);
    
    /**
     * 分页查询预约（storeId 为空时不过滤门店）
     */
    IPage<HospitalAppointment> getAppointmentPage(Page<HospitalAppointment> page,
                                                  String status,
                                                  String keyword,
                                                  String serviceType,
                                                  Long storeId);
    
    /**
     * 根据ID获取预约详情
     */
    HospitalAppointment getAppointmentById(Long id);
    
    /**
     * 更新预约状态
     */
    boolean updateAppointmentStatus(Long id, String status);

    /**
     * 更新预约状态（拒绝时可传拒绝原因，供用户端展示）
     */
    boolean updateAppointmentStatus(Long id, String status, String rejectReason);

    boolean updateAppointmentStatus(Long id, String status, String rejectReason, boolean allowCancelAfterServiceStarted);

    /**
     * 分配服务人员到预约
     */
    boolean assignMember(Long appointmentId, Long memberId);
}
