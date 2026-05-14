package com.pethome.service;

import com.pethome.entity.ServiceMember;
import com.pethome.entity.Admin;

import java.time.LocalDate;
import java.util.List;

/**
 * 服务人员Service接口
 */
public interface ServiceMemberService {
    
    /**
     * 根据服务类型查询启用的服务人员
     */
    List<ServiceMember> getMembersByServiceType(String serviceType);
    
    /**
     * 根据服务类型和门店查询启用的服务人员（storeId 为 null 时等同 getMembersByServiceType）
     */
    List<ServiceMember> getMembersByServiceTypeAndStore(String serviceType, Long storeId);
    
    /**
     * 自动分配服务人员（根据可用时间、工作量等）。storeId 不为 null 时仅从该门店或平台级成员中分配
     */
    ServiceMember autoAssignMember(String serviceType, LocalDate date, String timeSlot, Long storeId);
    
    /**
     * 自动分配服务人员（不按门店过滤，兼容旧逻辑）
     */
    default ServiceMember autoAssignMember(String serviceType, LocalDate date, String timeSlot) {
        return autoAssignMember(serviceType, date, timeSlot, null);
    }
    
    /**
     * 获取服务人员在指定日期的工作量（今日待完成任务数：pending+confirmed）
     */
    int getMemberWorkload(Long memberId, LocalDate date);

    /**
     * 获取服务人员总任务数（所有非取消的预约，动态统计）
     */
    int getMemberTotalTasks(Long memberId);
    
    /**
     * 获取所有服务人员（不区分服务类型）
     */
    List<ServiceMember> getAllMembers();
    
    /**
     * 创建服务人员
     */
    ServiceMember createMember(ServiceMember member);

    /**
     * 根据管理员账号同步服务人员信息
     */
    ServiceMember syncMemberWithAdmin(Admin admin);
    
    /**
     * 更新服务人员信息
     */
    boolean updateMember(ServiceMember member);
    
    /**
     * 根据ID查询服务人员
     */
    ServiceMember getMemberById(Long id);
    
    /**
     * 根据用户ID查询服务人员
     */
    ServiceMember getMemberByUserId(Long userId);
    
    /**
     * 删除服务人员
     */
    boolean deleteMember(Long id);
}

