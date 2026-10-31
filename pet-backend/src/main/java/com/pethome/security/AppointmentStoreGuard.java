package com.pethome.security;

import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.entity.ServiceMember;
import com.pethome.service.appointment.DoorCleaningAppointmentService;
import com.pethome.service.appointment.GroomingAppointmentService;
import com.pethome.service.appointment.HospitalAppointmentService;
import com.pethome.service.appointment.ServiceMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 门店维度访问控制工具。
 *
 * <p>用于「预约/工单审批」等跨门店数据的归属校验：平台管理员不受门店限制；
 * 店长/店员只能操作本服务门店的数据（serviceStoreId 缺失时回落到员工档案的门店）。</p>
 */
@Component
public class AppointmentStoreGuard {

    @Autowired(required = false)
    private ServiceMemberService serviceMemberService;

    @Autowired(required = false)
    private GroomingAppointmentService groomingAppointmentService;

    @Autowired(required = false)
    private HospitalAppointmentService hospitalAppointmentService;

    @Autowired(required = false)
    private DoorCleaningAppointmentService doorCleaningAppointmentService;

    /** 平台管理员返回 null（不限门店）；无门店归属返回 -1（一律拒绝） */
    public Long scopedStoreId(HttpServletRequest request) {
        if (RequestIdentity.isPlatformAdmin(request)) {
            return null;
        }
        Long scoped = RequestIdentity.serviceStoreId(request);
        if (scoped != null) {
            return scoped;
        }
        Long adminId = RequestIdentity.adminId(request);
        if (adminId != null && serviceMemberService != null) {
            ServiceMember member = serviceMemberService.getMemberByUserId(adminId);
            if (member != null && member.getStoreId() != null) {
                return member.getStoreId();
            }
        }
        return -1L;
    }

    /** 预约是否归属于 scope 门店（scope 为 null 表示平台管理员，直接放行） */
    public boolean canAccess(HttpServletRequest request, String appointmentType, Long appointmentId) {
        return canAccess(scopedStoreId(request), appointmentType, appointmentId);
    }

    /** 同 {@link #canAccess(HttpServletRequest, String, Long)}，复用已解析的门店 scope */
    public boolean canAccess(Long scope, String appointmentType, Long appointmentId) {
        if (scope == null) {
            return true;
        }
        if (scope == -1L || appointmentId == null || appointmentType == null) {
            return false;
        }
        Long storeId = null;
        Long memberId = null;
        switch (appointmentType.trim().toLowerCase()) {
            case "grooming": {
                GroomingAppointment a = groomingAppointmentService != null
                        ? groomingAppointmentService.getAppointmentById(appointmentId) : null;
                if (a == null) {
                    return false;
                }
                storeId = a.getStoreId();
                memberId = a.getMemberId();
                break;
            }
            case "hospital": {
                HospitalAppointment a = hospitalAppointmentService != null
                        ? hospitalAppointmentService.getAppointmentById(appointmentId) : null;
                if (a == null) {
                    return false;
                }
                storeId = a.getStoreId();
                memberId = a.getMemberId();
                break;
            }
            case "door-cleaning":
            case "door_cleaning": {
                DoorCleaningAppointment a = doorCleaningAppointmentService != null
                        ? doorCleaningAppointmentService.getAppointmentById(appointmentId) : null;
                if (a == null) {
                    return false;
                }
                storeId = a.getStoreId();
                memberId = a.getMemberId();
                break;
            }
            default:
                return false;
        }
        if (storeId != null) {
            return scope.equals(storeId);
        }
        if (memberId != null && serviceMemberService != null) {
            ServiceMember member = serviceMemberService.getMemberById(memberId);
            return member != null && member.getStoreId() != null && scope.equals(member.getStoreId());
        }
        return false;
    }
}
