package com.pethome.service;

import com.pethome.entity.AppointmentCancellationRequest;

import java.util.List;

/**
 * 取消预约申请：用户提交原因，工作人员同意后执行取消（含 0-2 小时违约金）
 */
public interface AppointmentCancellationRequestService {

    /**
     * 用户提交取消预约申请（仅已确认订单，需填原因；预约状态置为 cancel_pending）
     */
    AppointmentCancellationRequest submit(Long userId, String appointmentType, Long appointmentId, String reason);

    /**
     * 工作人员待处理列表
     */
    List<AppointmentCancellationRequest> listPending();

    /**
     * 工作人员同意：执行取消预约，并应用 0-2 小时违约金规则
     */
    boolean approve(Long id);

    /**
     * 工作人员拒绝：预约状态改回 confirmed，rejectReason 必填
     */
    boolean reject(Long id, String rejectReason);

    /**
     * 按预约查询待处理的取消申请（用于前端展示「取消待确认」）
     */
    AppointmentCancellationRequest getPendingByAppointment(String appointmentType, Long appointmentId);
}
