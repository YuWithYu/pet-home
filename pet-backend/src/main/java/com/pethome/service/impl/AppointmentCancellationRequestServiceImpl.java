package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.*;
import com.pethome.mapper.AppointmentCancellationRequestMapper;
import com.pethome.service.*;
import com.pethome.util.CancellationPenaltyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 取消预约申请：用户提交后预约状态改为 cancel_pending，工作人员同意后执行取消（含 0-2h 违约金），拒绝则改回 confirmed
 */
@Service
public class AppointmentCancellationRequestServiceImpl implements AppointmentCancellationRequestService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_REJECTED = "rejected";
    private static final String APPOINTMENT_STATUS_CANCEL_PENDING = "cancel_pending";
    private static final String APPOINTMENT_STATUS_CONFIRMED = "confirmed";
    private static final String APPOINTMENT_STATUS_CANCELLED = "cancelled";

    @Autowired
    private AppointmentCancellationRequestMapper cancellationRequestMapper;

    @Autowired
    private GroomingAppointmentService groomingAppointmentService;

    @Autowired
    private HospitalAppointmentService hospitalAppointmentService;

    @Autowired
    private DoorCleaningAppointmentService doorCleaningAppointmentService;
    @Autowired(required = false)
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentCancellationRequest submit(Long userId, String appointmentType, Long appointmentId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("请填写取消原因");
        }
        AppointmentCancellationRequest existing = getPendingByAppointment(appointmentType, appointmentId);
        if (existing != null) {
            throw new IllegalArgumentException("该预约已有取消申请待处理，请等待工作人员确认");
        }
        checkAppointmentConfirmedAndBelongsToUser(appointmentType, appointmentId, userId);
        assertServiceNotStartedForUserCancellation(appointmentType, appointmentId);

        AppointmentCancellationRequest req = new AppointmentCancellationRequest();
        req.setAppointmentType(normalizeAppointmentType(appointmentType));
        req.setAppointmentId(appointmentId);
        req.setUserId(userId);
        req.setReason(reason.trim());
        req.setStatus(STATUS_PENDING);
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        cancellationRequestMapper.insert(req);

        setAppointmentStatus(appointmentType, appointmentId, APPOINTMENT_STATUS_CANCEL_PENDING, null);
        return req;
    }

    @Override
    public List<AppointmentCancellationRequest> listPending() {
        QueryWrapper<AppointmentCancellationRequest> q = new QueryWrapper<>();
        q.eq("status", STATUS_PENDING).orderByDesc("create_time");
        return cancellationRequestMapper.selectList(q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long id) {
        AppointmentCancellationRequest req = cancellationRequestMapper.selectById(id);
        if (req == null || !STATUS_PENDING.equals(req.getStatus())) {
            return false;
        }
        String type = req.getAppointmentType();
        Long aptId = req.getAppointmentId();
        assertServiceNotStartedForUserCancellation(type, aptId);
        // 同意取消时不写 reject_reason，避免与“拒绝取消原因”混淆
        setAppointmentStatus(type, aptId, APPOINTMENT_STATUS_CANCELLED, null);
        req.setStatus(STATUS_APPROVED);
        req.setUpdateTime(LocalDateTime.now());
        cancellationRequestMapper.updateById(req);
        sendCancellationNotify(type, aptId, req.getUserId(),
                "取消预约已通过",
                "您的预约单#" + aptId + "已取消成功。");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(Long id, String rejectReason) {
        AppointmentCancellationRequest req = cancellationRequestMapper.selectById(id);
        if (req == null || !STATUS_PENDING.equals(req.getStatus())) {
            return false;
        }
        String reason = rejectReason == null ? "" : rejectReason.trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("请填写拒绝取消的原因");
        }
        setAppointmentStatus(req.getAppointmentType(), req.getAppointmentId(), APPOINTMENT_STATUS_CONFIRMED, reason);
        req.setStatus(STATUS_REJECTED);
        req.setUpdateTime(LocalDateTime.now());
        cancellationRequestMapper.updateById(req);
        sendCancellationNotify(req.getAppointmentType(), req.getAppointmentId(), req.getUserId(),
                "取消申请被拒绝",
                "您的预约单#" + req.getAppointmentId() + "取消申请未通过，原因：" + reason + "。预约仍为已确认状态。");
        return true;
    }

    @Override
    public AppointmentCancellationRequest getPendingByAppointment(String appointmentType, Long appointmentId) {
        String type = normalizeAppointmentType(appointmentType);
        QueryWrapper<AppointmentCancellationRequest> q = new QueryWrapper<>();
        q.eq("appointment_type", type).eq("appointment_id", appointmentId).eq("status", STATUS_PENDING);
        return cancellationRequestMapper.selectOne(q);
    }

    private static String normalizeAppointmentType(String type) {
        if (type == null) return "";
        if ("door-cleaning".equalsIgnoreCase(type)) return "door_cleaning";
        return type.trim();
    }

    /**
     * 服务已开始则用户不可再走取消申请（与文案「服务开始后不可取消」一致）。
     */
    private void assertServiceNotStartedForUserCancellation(String appointmentType, Long appointmentId) {
        String type = normalizeAppointmentType(appointmentType);
        LocalDate date = null;
        String timeSlot = null;
        switch (type) {
            case "grooming": {
                GroomingAppointment g = groomingAppointmentService.getById(appointmentId);
                if (g != null) {
                    date = g.getDate();
                    timeSlot = g.getTimeSlot();
                }
                break;
            }
            case "hospital": {
                HospitalAppointment h = hospitalAppointmentService.getById(appointmentId);
                if (h != null) {
                    date = h.getDate();
                    timeSlot = h.getTimeSlot();
                }
                break;
            }
            case "door_cleaning": {
                DoorCleaningAppointment d = doorCleaningAppointmentService.getById(appointmentId);
                if (d != null) {
                    date = d.getDate();
                    timeSlot = d.getTimeSlot();
                }
                break;
            }
            default:
                return;
        }
        CancellationPenaltyUtil.assertUserCancellationNotAfterServiceStart(date, timeSlot);
    }

    private void checkAppointmentConfirmedAndBelongsToUser(String appointmentType, Long appointmentId, Long userId) {
        String type = normalizeAppointmentType(appointmentType);
        switch (type) {
            case "grooming":
                GroomingAppointment g = groomingAppointmentService.getById(appointmentId);
                if (g == null || !userId.equals(g.getUserId())) throw new IllegalArgumentException("预约不存在或无权操作");
                if (!isCancellableStatus(g.getStatus())) {
                    throw new IllegalArgumentException("当前预约状态不支持申请取消");
                }
                break;
            case "hospital":
                HospitalAppointment h = hospitalAppointmentService.getById(appointmentId);
                if (h == null || !userId.equals(h.getUserId())) throw new IllegalArgumentException("预约不存在或无权操作");
                if (!isCancellableStatus(h.getStatus())) {
                    throw new IllegalArgumentException("当前预约状态不支持申请取消");
                }
                break;
            case "door_cleaning":
                DoorCleaningAppointment d = doorCleaningAppointmentService.getById(appointmentId);
                if (d == null || !userId.equals(d.getUserId())) throw new IllegalArgumentException("预约不存在或无权操作");
                if (!isCancellableStatus(d.getStatus())) {
                    throw new IllegalArgumentException("当前预约状态不支持申请取消");
                }
                break;
            default:
                throw new IllegalArgumentException("不支持的预约类型");
        }
    }

    private void setAppointmentStatus(String appointmentType, Long appointmentId, String status, String rejectReason) {
        String type = normalizeAppointmentType(appointmentType);
        switch (type) {
            case "grooming":
                if (rejectReason != null) {
                    groomingAppointmentService.updateAppointmentStatus(appointmentId, status, rejectReason);
                } else {
                    groomingAppointmentService.updateAppointmentStatus(appointmentId, status);
                }
                break;
            case "hospital":
                if (rejectReason != null) {
                    hospitalAppointmentService.updateAppointmentStatus(appointmentId, status, rejectReason);
                } else {
                    hospitalAppointmentService.updateAppointmentStatus(appointmentId, status);
                }
                break;
            case "door_cleaning":
                if (rejectReason != null) {
                    doorCleaningAppointmentService.updateAppointmentStatus(appointmentId, status, rejectReason);
                } else {
                    doorCleaningAppointmentService.updateAppointmentStatus(appointmentId, status);
                }
                break;
            default:
                throw new IllegalArgumentException("不支持的预约类型");
        }
    }

    private boolean isCancellableStatus(String status) {
        if (status == null) return false;
        return APPOINTMENT_STATUS_CONFIRMED.equals(status)
                || APPOINTMENT_STATUS_CANCEL_PENDING.equals(status)
                || "pending".equals(status)
                || "change_pending".equals(status)
                || "assigned".equals(status);
    }

    private void sendCancellationNotify(String appointmentType, Long appointmentId, Long userId, String title, String content) {
        if (notificationService == null || userId == null || appointmentId == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(buildCancellationContent(appointmentType, appointmentId, content));
        n.setType("appointment_remind");
        n.setStatus(0);
        n.setCreateTime(LocalDateTime.now());
        n.setUpdateTime(LocalDateTime.now());
        notificationService.createNotification(n);
    }

    private String buildCancellationContent(String appointmentType, Long appointmentId, String fallback) {
        String type = normalizeAppointmentType(appointmentType);
        String service = "预约";
        String when = "";
        String location = "";
        switch (type) {
            case "grooming": {
                GroomingAppointment g = groomingAppointmentService.getById(appointmentId);
                if (g != null) {
                    service = "洗护预约";
                    when = (g.getDate() != null ? g.getDate().toString() : "") + (g.getTimeSlot() != null ? " " + g.getTimeSlot() : "");
                    location = g.getLocation() == null ? "" : g.getLocation();
                }
                break;
            }
            case "hospital": {
                HospitalAppointment h = hospitalAppointmentService.getById(appointmentId);
                if (h != null) {
                    service = "医院预约";
                    when = (h.getDate() != null ? h.getDate().toString() : "") + (h.getTimeSlot() != null ? " " + h.getTimeSlot() : "");
                    location = h.getLocation() == null ? "" : h.getLocation();
                }
                break;
            }
            case "door_cleaning": {
                DoorCleaningAppointment d = doorCleaningAppointmentService.getById(appointmentId);
                if (d != null) {
                    service = "上门铲屎预约";
                    when = (d.getDate() != null ? d.getDate().toString() : "") + (d.getTimeSlot() != null ? " " + d.getTimeSlot() : "");
                    location = d.getLocation() == null ? "" : d.getLocation();
                }
                break;
            }
            default:
                break;
        }
        return fallback
                + "（" + service + "）"
                + (when == null || when.trim().isEmpty() ? "" : "，时间：" + when)
                + (location == null || location.trim().isEmpty() ? "" : "，地址：" + location);
    }
}
