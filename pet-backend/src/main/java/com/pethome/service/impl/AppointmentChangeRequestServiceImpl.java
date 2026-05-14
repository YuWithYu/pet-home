package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.*;
import com.pethome.mapper.AppointmentChangeRequestMapper;
import com.pethome.service.*;
import com.pethome.util.BookingLeadTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 预约变更申请：用户提交后状态改为 change_pending，工作人员同意后写入预约并改回 confirmed，拒绝则仅改回 confirmed
 */
@Service
public class AppointmentChangeRequestServiceImpl implements AppointmentChangeRequestService {

    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_APPROVED = "approved";
    private static final String STATUS_REJECTED = "rejected";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String APPOINTMENT_STATUS_CHANGE_PENDING = "change_pending";
    private static final String APPOINTMENT_STATUS_CONFIRMED = "confirmed";

    @Autowired
    private AppointmentChangeRequestMapper changeRequestMapper;

    @Autowired
    private AppointmentService appointmentService;

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
    public AppointmentChangeRequest submit(Long userId, String appointmentType, Long appointmentId,
                                           java.time.LocalDate requestedDate, String requestedTimeSlot,
                                           String requestedLocation, String requestedRemark, String requestedContactPhone,
                                           String requestedKeyHandoverMethod, String requestedKeyReturnMethod,
                                           Long requestedPetId) {
        // 每个订单只能变更一次（已有任意一条变更记录即不可再申请）
        if (hasAnyChangeRequest(appointmentType, appointmentId)) {
            throw new IllegalArgumentException("每个订单只能变更一次");
        }
        // 同一条预约只能有一条待处理的变更
        AppointmentChangeRequest existing = getPendingByAppointment(appointmentType, appointmentId);
        if (existing != null) {
            throw new IllegalArgumentException("该预约已有变更申请待处理，请等待工作人员确认");
        }
        // 校验预约存在且属于当前用户且为已确认
        checkAppointmentBelongsToUser(appointmentType, appointmentId, userId);

        if (BookingLeadTimeUtil.appliesScheduledLeadTimeRule(appointmentType)
                && requestedDate != null
                && requestedTimeSlot != null
                && !requestedTimeSlot.trim().isEmpty()) {
            BookingLeadTimeUtil.assertAtLeastHoursAhead(requestedDate, requestedTimeSlot.trim(),
                    BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);
        }

        AppointmentChangeRequest req = new AppointmentChangeRequest();
        req.setAppointmentType(appointmentType);
        req.setAppointmentId(appointmentId);
        req.setRequestedDate(requestedDate);
        req.setRequestedTimeSlot(requestedTimeSlot);
        req.setRequestedLocation(requestedLocation);
        req.setRequestedRemark(requestedRemark);
        req.setRequestedContactPhone(requestedContactPhone);
        if (requestedKeyHandoverMethod != null) req.setRequestedKeyHandoverMethod(requestedKeyHandoverMethod);
        if (requestedKeyReturnMethod != null) req.setRequestedKeyReturnMethod(requestedKeyReturnMethod);
        if (requestedPetId != null) req.setRequestedPetId(requestedPetId);
        req.setStatus(STATUS_PENDING);
        req.setCreateTime(LocalDateTime.now());
        req.setUpdateTime(LocalDateTime.now());
        changeRequestMapper.insert(req);

        setAppointmentStatus(appointmentType, appointmentId, APPOINTMENT_STATUS_CHANGE_PENDING);
        return req;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long changeRequestId) {
        AppointmentChangeRequest req = changeRequestMapper.selectById(changeRequestId);
        if (req == null || !STATUS_PENDING.equals(req.getStatus())) {
            return false;
        }
        String type = req.getAppointmentType();
        Long aptId = req.getAppointmentId();
        applyRequestedToAppointment(type, aptId, req);
        // 变更通过时“静默”改回 confirmed，避免触发预约服务层里的「预约已确认」通知
        setAppointmentStatusSilently(type, aptId, APPOINTMENT_STATUS_CONFIRMED);
        req.setStatus(STATUS_APPROVED);
        req.setUpdateTime(LocalDateTime.now());
        changeRequestMapper.updateById(req);
        // 单独发送「预约已变更」通知，明确是变更成功而非首次确认
        sendChangeApprovedNotification(type, aptId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(Long changeRequestId) {
        AppointmentChangeRequest req = changeRequestMapper.selectById(changeRequestId);
        if (req == null || !STATUS_PENDING.equals(req.getStatus())) {
            return false;
        }
        // 变更拒绝时也静默恢复状态，避免给用户发“预约已确认”的误导通知
        setAppointmentStatusSilently(req.getAppointmentType(), req.getAppointmentId(), APPOINTMENT_STATUS_CONFIRMED);
        req.setStatus(STATUS_REJECTED);
        req.setUpdateTime(LocalDateTime.now());
        changeRequestMapper.updateById(req);
        sendChangeRejectedNotification(req.getAppointmentType(), req.getAppointmentId());
        return true;
    }

    @Override
    public AppointmentChangeRequest getPendingByAppointment(String appointmentType, Long appointmentId) {
        QueryWrapper<AppointmentChangeRequest> q = new QueryWrapper<>();
        q.eq("appointment_type", appointmentType).eq("appointment_id", appointmentId).eq("status", STATUS_PENDING);
        return changeRequestMapper.selectOne(q);
    }

    @Override
    public List<AppointmentChangeRequest> listPending() {
        QueryWrapper<AppointmentChangeRequest> q = new QueryWrapper<>();
        q.eq("status", STATUS_PENDING).orderByDesc("create_time");
        return changeRequestMapper.selectList(q);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelByUser(String appointmentType, Long appointmentId, Long userId) {
        AppointmentChangeRequest req = getPendingByAppointment(appointmentType, appointmentId);
        if (req == null) {
            return false;
        }
        checkAppointmentBelongsToUser(appointmentType, appointmentId, userId);
        req.setStatus(STATUS_CANCELLED);
        req.setUpdateTime(LocalDateTime.now());
        changeRequestMapper.updateById(req);
        // 用户取消变更后静默恢复状态，避免再次触发“预约已确认”通知
        setAppointmentStatusSilently(appointmentType, appointmentId, APPOINTMENT_STATUS_CONFIRMED);
        return true;
    }

    @Override
    public boolean hasAnyChangeRequest(String appointmentType, Long appointmentId) {
        QueryWrapper<AppointmentChangeRequest> q = new QueryWrapper<>();
        q.eq("appointment_type", appointmentType).eq("appointment_id", appointmentId);
        return changeRequestMapper.selectCount(q) > 0;
    }

    private void checkAppointmentBelongsToUser(String appointmentType, Long appointmentId, Long userId) {
        switch (appointmentType == null ? "" : appointmentType) {
            case "grooming":
                GroomingAppointment g = groomingAppointmentService.getById(appointmentId);
                if (g == null || !userId.equals(g.getUserId())) throw new IllegalArgumentException("预约不存在或无权操作");
                if (!APPOINTMENT_STATUS_CONFIRMED.equals(g.getStatus()) && !APPOINTMENT_STATUS_CHANGE_PENDING.equals(g.getStatus()))
                    throw new IllegalArgumentException("仅已确认的预约可申请变更");
                break;
            case "hospital":
                HospitalAppointment h = hospitalAppointmentService.getById(appointmentId);
                if (h == null || !userId.equals(h.getUserId())) throw new IllegalArgumentException("预约不存在或无权操作");
                if (!APPOINTMENT_STATUS_CONFIRMED.equals(h.getStatus()) && !APPOINTMENT_STATUS_CHANGE_PENDING.equals(h.getStatus()))
                    throw new IllegalArgumentException("仅已确认的预约可申请变更");
                break;
            case "door_cleaning":
            case "door-cleaning":
                DoorCleaningAppointment d = doorCleaningAppointmentService.getById(appointmentId);
                if (d == null || !userId.equals(d.getUserId())) throw new IllegalArgumentException("预约不存在或无权操作");
                if (!APPOINTMENT_STATUS_CONFIRMED.equals(d.getStatus()) && !APPOINTMENT_STATUS_CHANGE_PENDING.equals(d.getStatus()))
                    throw new IllegalArgumentException("仅已确认的预约可申请变更");
                break;
            default:
                Appointment a = appointmentService.getAppointmentById(appointmentId);
                if (a == null || !userId.equals(a.getUserId())) throw new IllegalArgumentException("预约不存在或无权操作");
                if (!APPOINTMENT_STATUS_CONFIRMED.equals(a.getStatus()) && !APPOINTMENT_STATUS_CHANGE_PENDING.equals(a.getStatus()))
                    throw new IllegalArgumentException("仅已确认的预约可申请变更");
                break;
        }
    }

    private void setAppointmentStatus(String appointmentType, Long appointmentId, String status) {
        String type = appointmentType == null ? "" : appointmentType;
        switch (type) {
            case "grooming":
                groomingAppointmentService.updateAppointmentStatus(appointmentId, status);
                break;
            case "hospital":
                hospitalAppointmentService.updateAppointmentStatus(appointmentId, status);
                break;
            case "door_cleaning":
            case "door-cleaning":
                doorCleaningAppointmentService.updateAppointmentStatus(appointmentId, status);
                break;
            default:
                appointmentService.updateAppointmentStatus(appointmentId, status);
                break;
        }
    }

    private void setAppointmentStatusSilently(String appointmentType, Long appointmentId, String status) {
        String type = appointmentType == null ? "" : appointmentType;
        switch (type) {
            case "grooming":
                GroomingAppointment g = groomingAppointmentService.getById(appointmentId);
                if (g != null) {
                    g.setStatus(status);
                    g.setUpdateTime(LocalDateTime.now());
                    groomingAppointmentService.updateById(g);
                }
                break;
            case "hospital":
                HospitalAppointment h = hospitalAppointmentService.getById(appointmentId);
                if (h != null) {
                    h.setStatus(status);
                    h.setUpdateTime(LocalDateTime.now());
                    hospitalAppointmentService.updateById(h);
                }
                break;
            case "door_cleaning":
            case "door-cleaning":
                DoorCleaningAppointment d = doorCleaningAppointmentService.getById(appointmentId);
                if (d != null) {
                    d.setStatus(status);
                    d.setUpdateTime(LocalDateTime.now());
                    doorCleaningAppointmentService.updateById(d);
                }
                break;
            default:
                Appointment a = appointmentService.getAppointmentById(appointmentId);
                if (a != null) {
                    a.setStatus(status);
                    appointmentService.updateAppointment(a);
                }
                break;
        }
    }

    private void sendChangeApprovedNotification(String appointmentType, Long appointmentId) {
        if (notificationService == null || appointmentId == null) return;
        String type = appointmentType == null ? "" : appointmentType;
        Long userId = null;
        String serviceLabel = "预约";
        String when = "";
        String location = "";
        String storeName = "";

        switch (type) {
            case "grooming": {
                GroomingAppointment g = groomingAppointmentService.getById(appointmentId);
                if (g != null) {
                    userId = g.getUserId();
                    serviceLabel = "洗护预约";
                    when = (g.getDate() != null ? g.getDate().toString() : "") + (g.getTimeSlot() != null ? " " + g.getTimeSlot() : "");
                    location = g.getLocation() == null ? "" : g.getLocation();
                }
                break;
            }
            case "hospital": {
                HospitalAppointment h = hospitalAppointmentService.getById(appointmentId);
                if (h != null) {
                    userId = h.getUserId();
                    serviceLabel = "医院预约";
                    when = (h.getDate() != null ? h.getDate().toString() : "") + (h.getTimeSlot() != null ? " " + h.getTimeSlot() : "");
                    location = h.getLocation() == null ? "" : h.getLocation();
                }
                break;
            }
            case "door_cleaning":
            case "door-cleaning": {
                DoorCleaningAppointment d = doorCleaningAppointmentService.getAppointmentById(appointmentId);
                if (d != null) {
                    userId = d.getUserId();
                    serviceLabel = "上门铲屎预约";
                    when = buildDoorCleaningWhen(d);
                    location = d.getLocation() == null ? "" : d.getLocation();
                    storeName = d.getStoreName() == null ? "" : d.getStoreName();
                }
                break;
            }
            default: {
                Appointment a = appointmentService.getAppointmentById(appointmentId);
                if (a != null) {
                    userId = a.getUserId();
                    serviceLabel = "预约";
                    when = (a.getDate() != null ? a.getDate().toString() : "") + (a.getTimeSlot() != null ? " " + a.getTimeSlot() : "");
                    location = a.getLocation() == null ? "" : a.getLocation();
                }
                break;
            }
        }

        if (userId == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType("appointment_remind");
        n.setStatus(0);
        n.setTitle("预约已变更");
        n.setContent("您的" + serviceLabel + "已变更成功（预约单#" + appointmentId + "）"
                + (when == null || when.trim().isEmpty() ? "" : "，新时间：" + when)
                + (storeName == null || storeName.trim().isEmpty() ? "" : "，服务门店：" + storeName)
                + (location == null || location.trim().isEmpty() ? "" : "，服务地址：" + location));
        n.setCreateTime(LocalDateTime.now());
        n.setUpdateTime(LocalDateTime.now());
        notificationService.createNotification(n);
    }

    private void sendChangeRejectedNotification(String appointmentType, Long appointmentId) {
        if (notificationService == null || appointmentId == null) return;
        String type = appointmentType == null ? "" : appointmentType;
        Long userId = null;
        String serviceLabel = "预约";
        String when = "";
        String location = "";
        String storeName = "";

        switch (type) {
            case "grooming": {
                GroomingAppointment g = groomingAppointmentService.getById(appointmentId);
                if (g != null) {
                    userId = g.getUserId();
                    serviceLabel = "洗护预约";
                    when = (g.getDate() != null ? g.getDate().toString() : "") + (g.getTimeSlot() != null ? " " + g.getTimeSlot() : "");
                    location = g.getLocation() == null ? "" : g.getLocation();
                }
                break;
            }
            case "hospital": {
                HospitalAppointment h = hospitalAppointmentService.getById(appointmentId);
                if (h != null) {
                    userId = h.getUserId();
                    serviceLabel = "医院预约";
                    when = (h.getDate() != null ? h.getDate().toString() : "") + (h.getTimeSlot() != null ? " " + h.getTimeSlot() : "");
                    location = h.getLocation() == null ? "" : h.getLocation();
                }
                break;
            }
            case "door_cleaning":
            case "door-cleaning": {
                DoorCleaningAppointment d = doorCleaningAppointmentService.getAppointmentById(appointmentId);
                if (d != null) {
                    userId = d.getUserId();
                    serviceLabel = "上门铲屎预约";
                    when = buildDoorCleaningWhen(d);
                    location = d.getLocation() == null ? "" : d.getLocation();
                    storeName = d.getStoreName() == null ? "" : d.getStoreName();
                }
                break;
            }
            default: {
                Appointment a = appointmentService.getAppointmentById(appointmentId);
                if (a != null) {
                    userId = a.getUserId();
                    serviceLabel = "预约";
                    when = (a.getDate() != null ? a.getDate().toString() : "") + (a.getTimeSlot() != null ? " " + a.getTimeSlot() : "");
                    location = a.getLocation() == null ? "" : a.getLocation();
                }
                break;
            }
        }

        if (userId == null) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType("appointment_remind");
        n.setStatus(0);
        n.setTitle("变更申请被拒绝");
        n.setContent("您的" + serviceLabel + "变更申请未通过（预约单#" + appointmentId + "）"
                + (when == null || when.trim().isEmpty() ? "" : "，当前时间：" + when)
                + (storeName == null || storeName.trim().isEmpty() ? "" : "，服务门店：" + storeName)
                + (location == null || location.trim().isEmpty() ? "" : "，服务地址：" + location));
        n.setCreateTime(LocalDateTime.now());
        n.setUpdateTime(LocalDateTime.now());
        notificationService.createNotification(n);
    }

    private void applyRequestedToAppointment(String appointmentType, Long appointmentId, AppointmentChangeRequest req) {
        String type = appointmentType == null ? "" : appointmentType;
        switch (type) {
            case "grooming":
                GroomingAppointment g = groomingAppointmentService.getById(appointmentId);
                if (g != null) {
                    LocalDate newDateG = req.getRequestedDate() != null ? req.getRequestedDate() : g.getDate();
                    String newSlotG = req.getRequestedTimeSlot() != null ? req.getRequestedTimeSlot() : g.getTimeSlot();
                    if (newDateG != null && newSlotG != null && !newSlotG.trim().isEmpty()) {
                        BookingLeadTimeUtil.assertAtLeastHoursAhead(newDateG, newSlotG.trim(),
                                BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);
                    }
                    if (req.getRequestedDate() != null) g.setDate(req.getRequestedDate());
                    if (req.getRequestedTimeSlot() != null) g.setTimeSlot(req.getRequestedTimeSlot());
                    if (req.getRequestedLocation() != null) g.setLocation(req.getRequestedLocation());
                    if (req.getRequestedRemark() != null) g.setRemark(req.getRequestedRemark());
                    if (req.getRequestedContactPhone() != null) g.setContactPhone(req.getRequestedContactPhone());
                    if (req.getRequestedPetId() != null) g.setPetId(req.getRequestedPetId());
                    g.setUpdateTime(LocalDateTime.now());
                    groomingAppointmentService.updateById(g);
                }
                break;
            case "hospital":
                HospitalAppointment h = hospitalAppointmentService.getById(appointmentId);
                if (h != null) {
                    LocalDate newDateH = req.getRequestedDate() != null ? req.getRequestedDate() : h.getDate();
                    String newSlotH = req.getRequestedTimeSlot() != null ? req.getRequestedTimeSlot() : h.getTimeSlot();
                    if (newDateH != null && newSlotH != null && !newSlotH.trim().isEmpty()) {
                        BookingLeadTimeUtil.assertAtLeastHoursAhead(newDateH, newSlotH.trim(),
                                BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);
                    }
                    if (req.getRequestedDate() != null) h.setDate(req.getRequestedDate());
                    if (req.getRequestedTimeSlot() != null) h.setTimeSlot(req.getRequestedTimeSlot());
                    if (req.getRequestedLocation() != null) h.setLocation(req.getRequestedLocation());
                    if (req.getRequestedRemark() != null) h.setRemark(req.getRequestedRemark());
                    if (req.getRequestedContactPhone() != null) h.setContactPhone(req.getRequestedContactPhone());
                    if (req.getRequestedPetId() != null) h.setPetId(req.getRequestedPetId());
                    h.setUpdateTime(LocalDateTime.now());
                    hospitalAppointmentService.updateById(h);
                }
                break;
            case "door_cleaning":
            case "door-cleaning":
                DoorCleaningAppointment d = doorCleaningAppointmentService.getById(appointmentId);
                if (d != null) {
                    LocalDate newDateD = req.getRequestedDate() != null ? req.getRequestedDate() : d.getDate();
                    String newSlotD = req.getRequestedTimeSlot() != null ? req.getRequestedTimeSlot() : d.getTimeSlot();
                    if (newDateD != null && newSlotD != null && !newSlotD.trim().isEmpty()) {
                        BookingLeadTimeUtil.assertAtLeastHoursAhead(newDateD, newSlotD.trim(),
                                BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);
                    }
                    if (req.getRequestedDate() != null) d.setDate(req.getRequestedDate());
                    if (req.getRequestedTimeSlot() != null) d.setTimeSlot(req.getRequestedTimeSlot());
                    if (req.getRequestedLocation() != null) d.setLocation(req.getRequestedLocation());
                    if (req.getRequestedRemark() != null) d.setRemark(req.getRequestedRemark());
                    if (req.getRequestedContactPhone() != null) d.setContactPhone(req.getRequestedContactPhone());
                    if (req.getRequestedKeyHandoverMethod() != null) d.setKeyHandoverMethod(req.getRequestedKeyHandoverMethod());
                    if (req.getRequestedKeyReturnMethod() != null) d.setKeyReturnMethod(req.getRequestedKeyReturnMethod());
                    if (req.getRequestedPetId() != null) d.setPetId(req.getRequestedPetId());
                    d.setUpdateTime(LocalDateTime.now());
                    doorCleaningAppointmentService.updateById(d);
                }
                break;
            default:
                Appointment a = appointmentService.getAppointmentById(appointmentId);
                if (a != null) {
                    if (req.getRequestedDate() != null) a.setDate(req.getRequestedDate());
                    if (req.getRequestedTimeSlot() != null) a.setTimeSlot(req.getRequestedTimeSlot());
                    if (req.getRequestedLocation() != null) a.setLocation(req.getRequestedLocation());
                    if (req.getRequestedRemark() != null) a.setRemark(req.getRequestedRemark());
                    if (req.getRequestedContactPhone() != null) a.setContactPhone(req.getRequestedContactPhone());
                    if (req.getRequestedPetId() != null) a.setPetId(req.getRequestedPetId());
                    appointmentService.updateAppointment(a);
                }
                break;
        }
    }

    private String buildDoorCleaningWhen(DoorCleaningAppointment appointment) {
        if (appointment == null) return "";
        // 优先使用 date + timeSlot（LocalDate + 时间段），避免出现 2026-03-20T00:00
        if (appointment.getDate() != null && appointment.getTimeSlot() != null && !appointment.getTimeSlot().trim().isEmpty()) {
            return appointment.getDate().toString() + " " + appointment.getTimeSlot();
        }
        // 若缺失 date/timeSlot，则回退到 appointmentDate，但要格式化为 yyyy-MM-dd HH:mm
        if (appointment.getAppointmentDate() != null) {
            return appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        if (appointment.getDate() != null) return appointment.getDate().toString();
        if (appointment.getTimeSlot() != null) return appointment.getTimeSlot();
        return "";
    }
}
