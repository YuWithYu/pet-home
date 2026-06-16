package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.HospitalAppointment;
import com.pethome.entity.HospitalService;
import com.pethome.entity.Pet;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.ServiceSchedule;
import com.pethome.mapper.HospitalAppointmentMapper;
import com.pethome.mapper.GroomingAppointmentMapper;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.service.HospitalAppointmentService;
import com.pethome.service.HospitalServiceService;
import com.pethome.service.PetService;
import com.pethome.service.ServiceMemberService;
import com.pethome.service.ServiceScheduleService;
import com.pethome.service.NotificationService;
import com.pethome.service.ServiceStoreService;
import com.pethome.entity.Notification;
import com.pethome.entity.ServiceStore;
import com.pethome.util.BookingLeadTimeUtil;
import com.pethome.util.CancellationPenaltyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.security.SecureRandom;

@Service
public class HospitalAppointmentServiceImpl extends ServiceImpl<HospitalAppointmentMapper, HospitalAppointment> implements HospitalAppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(HospitalAppointmentServiceImpl.class);
    private static final String SERVICE_TYPE = "hospital";
    private static final SecureRandom VERIFY_CODE_RANDOM = new SecureRandom();

    @Resource
    private ServiceScheduleService serviceScheduleService;

    @Resource
    private PetService petService;

    @Resource
    private ServiceMemberService serviceMemberService;

    @Resource
    private ServiceStoreService serviceStoreService;

    @Resource
    private GroomingAppointmentMapper groomingAppointmentMapper;

    @Resource
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    private HospitalServiceService hospitalServiceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createHospitalAppointment(HospitalAppointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("预约信息不能为空");
        }

        if (appointment.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (appointment.getDate() == null) {
            throw new IllegalArgumentException("预约日期不能为空");
        }

        if (appointment.getTimeSlot() == null || appointment.getTimeSlot().trim().isEmpty()) {
            throw new IllegalArgumentException("预约时间段不能为空");
        }

        BookingLeadTimeUtil.assertAtLeastHoursAhead(appointment.getDate(), appointment.getTimeSlot().trim(),
                BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);

        if (appointment.getVerifyCode() == null || appointment.getVerifyCode().isEmpty()) {
            appointment.setVerifyCode(generateUniqueSixDigitVerifyCode());
        }

        if (appointment.getIsVerified() == null) {
            appointment.setIsVerified(0);
        }

        // 创建时一律为「待确认」，由管理员后台确认后再改为已确认
        appointment.setStatus("pending");

        if (appointment.getCreateTime() == null) {
            appointment.setCreateTime(LocalDateTime.now());
        }
        appointment.setUpdateTime(LocalDateTime.now());

        if (appointment.getServiceType() == null || appointment.getServiceType().isEmpty()) {
            appointment.setServiceType(SERVICE_TYPE);
        }

        boolean saved = this.save(appointment);
        if (!saved) {
            return false;
        }

        // 占用排班放在独立事务中执行，失败时只回滚占排班，不影响已保存的预约（避免 rollback-only 导致整笔预约失败）
        try {
            ServiceSchedule schedule = serviceScheduleService.tryBookTimeSlotInNewTransaction(
                    appointment.getServiceType() != null ? appointment.getServiceType() : SERVICE_TYPE,
                    appointment.getDate(),
                    appointment.getTimeSlot(),
                    appointment.getId(),
                    appointment.getStoreId()
            );
            if (schedule != null && schedule.getMemberId() != null) {
                appointment.setMemberId(schedule.getMemberId());
                appointment.setUpdateTime(LocalDateTime.now());
                this.updateById(appointment);
            }
        } catch (Exception e) {
            logger.warn("占用时间段未成功（该时段暂无排班或已约满），预约仍创建成功，待管理员分配: date={}, timeSlot={}, storeId={}, msg={}",
                    appointment.getDate(), appointment.getTimeSlot(), appointment.getStoreId(), e.getMessage());
            // 不抛异常：预约已保存，member_id 为空，状态为 pending，由后台分配
        }

        return true;
    }

    private String generateUniqueSixDigitVerifyCode() {
        for (int i = 0; i < 200; i++) {
            String code = String.format("%06d", VERIFY_CODE_RANDOM.nextInt(1_000_000));
            if (!existsVerifyCodeAcrossAppointments(code)) {
                return code;
            }
        }
        throw new IllegalStateException("核销码生成失败，请重试");
    }

    private boolean existsVerifyCodeAcrossAppointments(String code) {
        QueryWrapper<HospitalAppointment> hospitalWrapper = new QueryWrapper<>();
        hospitalWrapper.eq("verify_code", code);
        if (this.baseMapper.selectCount(hospitalWrapper) > 0) {
            return true;
        }

        QueryWrapper<com.pethome.entity.GroomingAppointment> groomingWrapper = new QueryWrapper<>();
        groomingWrapper.eq("verify_code", code);
        if (groomingAppointmentMapper.selectCount(groomingWrapper) > 0) {
            return true;
        }

        QueryWrapper<com.pethome.entity.DoorCleaningAppointment> doorWrapper = new QueryWrapper<>();
        doorWrapper.eq("verify_code", code);
        return doorCleaningAppointmentMapper.selectCount(doorWrapper) > 0;
    }

    @Override
    public List<HospitalAppointment> getAppointmentsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        QueryWrapper<HospitalAppointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("date", "create_time");
        List<HospitalAppointment> appointments = this.list(queryWrapper);
        if (appointments == null) {
            return List.of();
        }
        for (HospitalAppointment a : appointments) {
            try {
                enrichAppointment(a);
            } catch (Exception e) {
                logger.warn("enrichAppointment 失败, appointmentId: {}", a != null ? a.getId() : null, e);
            }
        }
        return appointments;
    }

    @Override
    public List<HospitalAppointment> getAppointmentsByMemberId(Long memberId) {
        if (memberId == null) {
            return List.of();
        }
        QueryWrapper<HospitalAppointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId)
                .orderByDesc("date", "create_time");
        List<HospitalAppointment> appointments = this.list(queryWrapper);
        appointments.forEach(this::enrichAppointment);
        return appointments;
    }

    @Override
    public IPage<HospitalAppointment> getAppointmentPage(Page<HospitalAppointment> page,
                                                         String status,
                                                         String keyword,
                                                         String serviceType,
                                                         Long storeId) {
        QueryWrapper<HospitalAppointment> wrapper = new QueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }

        if (serviceType != null && !serviceType.isEmpty()) {
            if ("hospital".equalsIgnoreCase(serviceType) || "medical".equalsIgnoreCase(serviceType)) {
                wrapper.and(w -> w.eq("service_type", "hospital")
                        .or().eq("service_type", "medical")
                        .or().isNull("service_type"));
            } else {
                wrapper.eq("service_type", serviceType);
            }
        }

        // storeId=-1 表示员工未分配门店，不按门店筛选，显示全部预约
        // 兼容历史/异常数据：store_id 为空时仍应在「按门店查看」时可见，避免后台列表整页为空
        if (storeId != null && storeId >= 0) {
            wrapper.and(w -> w.eq("store_id", storeId).or().isNull("store_id"));
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("contact_name", keyword)
                    .or()
                    .like("contact_phone", keyword)
                    .or()
                    .like("location", keyword));
        }

        wrapper.orderByDesc("date", "create_time");
        IPage<HospitalAppointment> result = this.page(page, wrapper);
        if (result != null && result.getRecords() != null) {
            result.getRecords().forEach(this::enrichAppointment);
        }
        return result;
    }

    @Override
    public HospitalAppointment getAppointmentById(Long id) {
        HospitalAppointment appointment = this.getById(id);
        enrichAppointment(appointment);
        return appointment;
    }

    @Override
    public boolean updateAppointmentStatus(Long id, String status) {
        return updateAppointmentStatus(id, status, null, false);
    }

    @Override
    public boolean updateAppointmentStatus(Long id, String status, String rejectReason) {
        return updateAppointmentStatus(id, status, rejectReason, false);
    }

    @Override
    public boolean updateAppointmentStatus(Long id, String status, String rejectReason, boolean allowCancelAfterServiceStarted) {
        HospitalAppointment appointment = this.getById(id);
        if (appointment == null) {
            return false;
        }
        String previousStatus = appointment.getStatus();
        if ("cancelled".equals(status) && !allowCancelAfterServiceStarted
                && CancellationPenaltyUtil.isServiceStarted(appointment.getDate(), appointment.getTimeSlot())) {
            throw new IllegalArgumentException("服务已开始或已结束，无法取消；如有特殊情况请联系客服或在管理后台操作强制取消。");
        }
        appointment.setStatus(status);
        appointment.setUpdateTime(LocalDateTime.now());
        if ("cancelled".equals(status) && rejectReason != null && !rejectReason.trim().isEmpty()) {
            appointment.setRejectReason(rejectReason.trim());
        }
        if ("cancelled".equals(status) && isCancellationPenaltyEligiblePreviousStatus(previousStatus)
                && CancellationPenaltyUtil.isInPenaltyWindow(appointment.getDate(), appointment.getTimeSlot())) {
            BigDecimal penalty = CancellationPenaltyUtil.calculatePenaltyAmount(appointment.getPrice());
            appointment.setCancellationPenaltyAmount(penalty);
        }
        boolean updated = this.updateById(appointment);

        // 预约状态首次从「非已确认/已分配」->「已确认/已分配」时，立即生成系统通知（便于用户端展示）
        if (updated && notificationService != null
                && !isStaffConfirmedStatus(previousStatus)
                && isStaffConfirmedStatus(status)
                && appointment.getUserId() != null) {
            Notification n = new Notification();
            n.setUserId(appointment.getUserId());
            n.setTitle("预约已确认");
            n.setContent("您的医院预约已确认（预约单#" + appointment.getId() + "）"
                    + "，服务时间："
                    + (appointment.getDate() != null ? appointment.getDate() : "")
                    + (appointment.getTimeSlot() != null ? " " + appointment.getTimeSlot() : "")
                    + (appointment.getLocation() != null && !appointment.getLocation().trim().isEmpty() ? "，服务地址：" + appointment.getLocation() : ""));
            n.setType("appointment_remind");
            n.setStatus(0);
            n.setRelatedId(appointment.getId());
            n.setRelatedType("appointment");
            n.setCreateTime(LocalDateTime.now());
            n.setUpdateTime(LocalDateTime.now());
            notificationService.createNotification(n);
        }

        return updated;
    }

    private static boolean isStaffConfirmedStatus(String status) {
        return status != null && (status.equals("confirmed") || status.equals("assigned") || "已确认".equals(status) || "已分配".equals(status));
    }

    private static boolean isCancellationPenaltyEligiblePreviousStatus(String status) {
        if (status == null) {
            return false;
        }
        return isStaffConfirmedStatus(status) || "cancel_pending".equals(status);
    }

    private void enrichAppointment(HospitalAppointment appointment) {
        if (appointment == null) {
            return;
        }

        if (appointment.getPetId() != null && appointment.getPetName() == null) {
            try {
                Pet pet = petService.getPetById(appointment.getPetId());
                if (pet != null) {
                    appointment.setPetName(pet.getName());
                }
            } catch (Exception e) {
                logger.warn("查询宠物信息失败, petId: {}", appointment.getPetId(), e);
            }
        }

        if (appointment.getMemberId() != null && (appointment.getMemberName() == null || appointment.getMemberName().trim().isEmpty())) {
            try {
                ServiceMember member = serviceMemberService.getMemberById(appointment.getMemberId());
                if (member != null) {
                    appointment.setMemberName(member.getMemberName());
                    appointment.setMemberPhone(member.getPhone());
                }
            } catch (Exception e) {
                logger.warn("查询服务人员信息失败, memberId: {}", appointment.getMemberId(), e);
            }
        } else if (appointment.getMemberId() == null && (appointment.getMemberName() == null || appointment.getMemberName().trim().isEmpty())
                && appointment.getDate() != null && appointment.getTimeSlot() != null && !appointment.getTimeSlot().trim().isEmpty()) {
            // 预约未分配服务人员时，从排班中补全显示（排班已分配则展示）
            try {
                ServiceSchedule schedule = serviceScheduleService.getScheduleForSlot(
                        SERVICE_TYPE, appointment.getDate(), appointment.getTimeSlot(), appointment.getStoreId());
                if (schedule != null && schedule.getMemberId() != null) {
                    ServiceMember member = serviceMemberService.getMemberById(schedule.getMemberId());
                    if (member != null) {
                        appointment.setMemberName(member.getMemberName());
                        appointment.setMemberPhone(member.getPhone());
                    }
                }
            } catch (Exception e) {
                logger.warn("从排班补全服务人员失败, appointmentId: {}", appointment.getId(), e);
            }
        }

        if (appointment.getStoreId() != null && appointment.getStoreName() == null && serviceStoreService != null) {
            try {
                ServiceStore store = serviceStoreService.getStoreById(appointment.getStoreId());
                if (store != null) {
                    appointment.setStoreName(store.getStoreName());
                }
            } catch (Exception e) {
                logger.warn("查询门店信息失败, storeId: {}", appointment.getStoreId(), e);
            }
        }

        if (appointment.getServiceId() != null && hospitalServiceService != null) {
            try {
                HospitalService hs = hospitalServiceService.getHospitalServiceById(appointment.getServiceId());
                if (hs != null && StringUtils.hasText(hs.getName())) {
                    appointment.setServiceName(hs.getName());
                }
            } catch (Exception e) {
                logger.warn("查询医院服务项目名称失败, serviceId: {}", appointment.getServiceId(), e);
            }
        }
    }

    @Override
    public boolean assignMember(Long appointmentId, Long memberId) {
        if (appointmentId == null || memberId == null) {
            return false;
        }
        HospitalAppointment appointment = this.getById(appointmentId);
        if (appointment == null) {
            return false;
        }
        ServiceMember member = serviceMemberService.getMemberById(memberId);
        if (member == null) {
            logger.warn("分配失败：服务人员不存在, memberId: {}", memberId);
            return false;
        }
        if (!SERVICE_TYPE.equalsIgnoreCase(member.getServiceType())) {
            logger.warn("分配失败：服务人员类型不匹配, 需要: {}, 实际: {}", SERVICE_TYPE, member.getServiceType());
            return false;
        }
        appointment.setMemberId(memberId);
        appointment.setUpdateTime(LocalDateTime.now());
        return this.updateById(appointment);
    }
}
