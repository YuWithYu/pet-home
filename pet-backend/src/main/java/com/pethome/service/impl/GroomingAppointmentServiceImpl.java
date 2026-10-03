package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.GroomingService;
import com.pethome.entity.Pet;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.ServiceSchedule;
import com.pethome.mapper.GroomingAppointmentMapper;
import com.pethome.mapper.HospitalAppointmentMapper;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.service.GroomingAppointmentService;
import com.pethome.service.GroomingServiceService;
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
public class GroomingAppointmentServiceImpl extends ServiceImpl<GroomingAppointmentMapper, GroomingAppointment> implements GroomingAppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(GroomingAppointmentServiceImpl.class);
    private static final String SERVICE_TYPE = "grooming";
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
    private HospitalAppointmentMapper hospitalAppointmentMapper;

    @Resource
    private DoorCleaningAppointmentMapper doorCleaningAppointmentMapper;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    private GroomingServiceService groomingServiceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createGroomingAppointment(GroomingAppointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("预约信息不能为空");
        }

        if (appointment.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        if (appointment.getDate() == null) {
            throw new IllegalArgumentException("预约日期不能为空");
        }

        if (!org.springframework.util.StringUtils.hasText(appointment.getTimeSlot())) {
            throw new IllegalArgumentException("预约时间段不能为空");
        }

        BookingLeadTimeUtil.assertAtLeastHoursAhead(appointment.getDate(), appointment.getTimeSlot().trim(),
                BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);

        if (!org.springframework.util.StringUtils.hasText(appointment.getServiceType())) {
            appointment.setServiceType(SERVICE_TYPE);
        }

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
        QueryWrapper<GroomingAppointment> groomingWrapper = new QueryWrapper<>();
        groomingWrapper.eq("verify_code", code);
        if (this.baseMapper.selectCount(groomingWrapper) > 0) {
            return true;
        }

        QueryWrapper<com.pethome.entity.HospitalAppointment> hospitalWrapper = new QueryWrapper<>();
        hospitalWrapper.eq("verify_code", code);
        if (hospitalAppointmentMapper.selectCount(hospitalWrapper) > 0) {
            return true;
        }

        QueryWrapper<com.pethome.entity.DoorCleaningAppointment> doorWrapper = new QueryWrapper<>();
        doorWrapper.eq("verify_code", code);
        return doorCleaningAppointmentMapper.selectCount(doorWrapper) > 0;
    }

    @Override
    public List<GroomingAppointment> getAppointmentsByUserId(Long userId) {
        if (userId == null) {
            return List.of();
        }
        QueryWrapper<GroomingAppointment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("date", "create_time");
        List<GroomingAppointment> appointments = this.list(wrapper);
        if (appointments == null) {
            return List.of();
        }
        for (GroomingAppointment a : appointments) {
            try {
                enrichAppointment(a);
            } catch (Exception e) {
                logger.warn("enrichAppointment 失败, appointmentId: {}", a != null ? a.getId() : null, e);
            }
        }
        return appointments;
    }

    @Override
    public GroomingAppointment getAppointmentById(Long id) {
        GroomingAppointment appointment = this.getById(id);
        enrichAppointment(appointment);
        return appointment;
    }

    @Override
    public List<GroomingAppointment> getAppointmentsByMemberId(Long memberId) {
        if (memberId == null) {
            return List.of();
        }
        QueryWrapper<GroomingAppointment> wrapper = new QueryWrapper<>();
        wrapper.eq("member_id", memberId)
                .orderByDesc("date", "create_time");
        List<GroomingAppointment> appointments = this.list(wrapper);
        appointments.forEach(this::enrichAppointment);
        return appointments;
    }

    @Override
    public IPage<GroomingAppointment> getAppointmentPage(Page<GroomingAppointment> page, String status, String keyword, String serviceType, Long storeId) {
        QueryWrapper<GroomingAppointment> wrapper = new QueryWrapper<>();

        if (org.springframework.util.StringUtils.hasText(status)) {
            wrapper.eq("status", status);
        }

        if (org.springframework.util.StringUtils.hasText(serviceType)) {
            // 与医院侧一致：历史数据可能未写入 service_type，仍应出现在洗护管理列表
            if ("grooming".equalsIgnoreCase(serviceType)) {
                wrapper.and(w -> w.eq("service_type", "grooming").or().isNull("service_type"));
            } else {
                wrapper.eq("service_type", serviceType);
            }
        }

        // storeId=-1 表示员工未分配门店，不按门店筛选，显示全部预约
        // 兼容未写入门店的新单：store_id 为空时仍应在「按门店查看」时可见
        if (storeId != null && storeId >= 0) {
            wrapper.and(w -> w.eq("store_id", storeId).or().isNull("store_id"));
        }

        if (org.springframework.util.StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("contact_name", keyword)
                    .or()
                    .like("contact_phone", keyword)
                    .or()
                    .like("location", keyword));
        }

        wrapper.orderByDesc("date", "create_time");
        IPage<GroomingAppointment> result = this.page(page, wrapper);
        if (result != null && result.getRecords() != null) {
            result.getRecords().forEach(this::enrichAppointment);
        }
        return result;
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
        GroomingAppointment appointment = this.getById(id);
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
            n.setContent("您的洗护预约已确认（预约单#" + appointment.getId() + "）"
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

    private void enrichAppointment(GroomingAppointment appointment) {
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

        if (appointment.getServiceId() != null && groomingServiceService != null) {
            try {
                GroomingService gs = groomingServiceService.getGroomingServiceById(appointment.getServiceId());
                if (gs != null && StringUtils.hasText(gs.getName())) {
                    appointment.setServiceName(gs.getName());
                }
            } catch (Exception e) {
                logger.warn("查询洗护服务项目名称失败, serviceId: {}", appointment.getServiceId(), e);
            }
        }
    }
}
