package com.pethome.service;

import com.pethome.entity.AppointmentChangeRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约变更申请：用户提交变更，工作人员同意/拒绝
 */
public interface AppointmentChangeRequestService {

    /**
     * 用户提交变更申请（将预约状态置为 change_pending，等待工作人员确认）
     * requestedKeyHandoverMethod/requestedKeyReturnMethod 仅上门铲屎时使用
     */
    AppointmentChangeRequest submit(Long userId, String appointmentType, Long appointmentId,
                                    LocalDate requestedDate, String requestedTimeSlot,
                                    String requestedLocation, String requestedRemark, String requestedContactPhone,
                                    String requestedKeyHandoverMethod, String requestedKeyReturnMethod,
                                    Long requestedPetId);

    /**
     * 工作人员同意变更：将申请中的时间/地址等写入预约，预约状态改回 confirmed
     */
    boolean approve(Long changeRequestId);

    /**
     * 工作人员拒绝变更：预约状态改回 confirmed，变更申请标记为 rejected
     */
    boolean reject(Long changeRequestId);

    /**
     * 查询某预约的待处理变更申请（最多一条）
     */
    AppointmentChangeRequest getPendingByAppointment(String appointmentType, Long appointmentId);

    /**
     * 查询所有待确认的变更申请（后台/工单用）
     */
    List<AppointmentChangeRequest> listPending();

    /**
     * 用户取消变更（仅待确认时可取消，预约状态改回已确认）
     */
    boolean cancelByUser(String appointmentType, Long appointmentId, Long userId);

    /**
     * 该预约是否已有过变更申请（含待确认/已同意/已拒绝/用户取消），用于限制一单只能变更一次
     */
    boolean hasAnyChangeRequest(String appointmentType, Long appointmentId);
}
