package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.LitterService;
import com.pethome.entity.Pet;
import com.pethome.entity.ServiceMember;
import com.pethome.entity.ServiceSchedule;
import com.pethome.mapper.DoorCleaningAppointmentMapper;
import com.pethome.mapper.GroomingAppointmentMapper;
import com.pethome.mapper.HospitalAppointmentMapper;
import com.pethome.service.DoorCleaningAppointmentService;
import com.pethome.service.LitterServiceService;
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
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.security.SecureRandom;

@Service
public class DoorCleaningAppointmentServiceImpl extends ServiceImpl<DoorCleaningAppointmentMapper, DoorCleaningAppointment> implements DoorCleaningAppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(DoorCleaningAppointmentServiceImpl.class);
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
    private HospitalAppointmentMapper hospitalAppointmentMapper;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    private LitterServiceService litterServiceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDoorCleaningAppointment(DoorCleaningAppointment appointment) {
        // 创建时一律为「待确认」，由管理员后台确认后再改为已确认
        appointment.setStatus("pending");
        if (appointment.getCreateTime() == null) {
            appointment.setCreateTime(LocalDateTime.now());
        }
        appointment.setUpdateTime(LocalDateTime.now());
        if (appointment.getVerifyCode() == null || appointment.getVerifyCode().isEmpty()) {
            appointment.setVerifyCode(generateUniqueSixDigitVerifyCode());
        }
        if (appointment.getIsVerified() == null) {
            appointment.setIsVerified(0);
        }
        if (appointment.getServiceType() == null || appointment.getServiceType().isEmpty()) {
            appointment.setServiceType("door-cleaning");
        }

        // 兼容旧数据：确保 date 有值
        LocalDate appointmentDate = null;
        if (appointment.getDate() != null) {
            appointmentDate = appointment.getDate();
        } else if (appointment.getAppointmentDate() != null) {
            appointmentDate = appointment.getAppointmentDate().toLocalDate();
            appointment.setDate(appointmentDate);
        }

        if (appointmentDate == null) {
            throw new IllegalArgumentException("预约日期不能为空");
        }
        if (appointment.getTimeSlot() == null || appointment.getTimeSlot().trim().isEmpty()) {
            throw new IllegalArgumentException("预约时间段不能为空");
        }

        BookingLeadTimeUtil.assertAtLeastHoursAhead(appointmentDate, appointment.getTimeSlot(),
                BookingLeadTimeUtil.SCHEDULED_SERVICE_MIN_ADVANCE_HOURS);

        // 若未显式设置预约日期时间，则默认当日开始时间
        if (appointment.getAppointmentDate() == null) {
            appointment.setAppointmentDate(appointmentDate.atStartOfDay());
        }

        boolean saved = this.save(appointment);
        if (!saved) {
            return false;
        }

        try {
            ServiceSchedule schedule = serviceScheduleService.bookTimeSlot(
                    appointment.getServiceType(),
                    appointmentDate,
                    appointment.getTimeSlot(),
                    appointment.getId(),
                    appointment.getStoreId()
            );

            if (schedule != null) {
                appointment.setMemberId(schedule.getMemberId());
                // 保持 pending，与洗护一致：由管理员在后台确认后再改为已确认，不在此处改为「已分配」
                appointment.setUpdateTime(LocalDateTime.now());
                this.updateById(appointment);
            }
        } catch (Exception e) {
            logger.error("自动分配服务人员失败，将回滚此次预约: {}", e.getMessage(), e);
            // 回滚预约记录
            throw e;
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
        QueryWrapper<DoorCleaningAppointment> doorWrapper = new QueryWrapper<>();
        doorWrapper.eq("verify_code", code);
        if (this.baseMapper.selectCount(doorWrapper) > 0) {
            return true;
        }

        QueryWrapper<com.pethome.entity.GroomingAppointment> groomingWrapper = new QueryWrapper<>();
        groomingWrapper.eq("verify_code", code);
        if (groomingAppointmentMapper.selectCount(groomingWrapper) > 0) {
            return true;
        }

        QueryWrapper<com.pethome.entity.HospitalAppointment> hospitalWrapper = new QueryWrapper<>();
        hospitalWrapper.eq("verify_code", code);
        return hospitalAppointmentMapper.selectCount(hospitalWrapper) > 0;
    }

    @Override
    public List<DoorCleaningAppointment> getAppointmentsByUserId(Long userId) {
        logger.info("查询用户预约，userId: {}", userId);
        if (userId == null) {
            return List.of();
        }
        QueryWrapper<DoorCleaningAppointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("date", "create_time");
        List<DoorCleaningAppointment> result = this.list(queryWrapper);
        if (result == null) {
            return List.of();
        }
        for (DoorCleaningAppointment a : result) {
            try {
                enrichAppointment(a);
            } catch (Exception e) {
                logger.warn("enrichAppointment 失败, appointmentId: {}", a != null ? a.getId() : null, e);
            }
        }
        logger.info("查询结果数量: {}", result.size());
        return result;
    }

    @Override
    public DoorCleaningAppointment getAppointmentById(Long id) {
        DoorCleaningAppointment appointment = this.getById(id);
        enrichAppointment(appointment);
        return appointment;
    }

    @Override
    public List<DoorCleaningAppointment> getAppointmentsByMemberId(Long memberId) {
        if (memberId == null) {
            return List.of();
        }
        QueryWrapper<DoorCleaningAppointment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId)
                .orderByDesc("date", "create_time");
        List<DoorCleaningAppointment> appointments = this.list(queryWrapper);
        if (appointments != null) {
            appointments.forEach(this::enrichAppointment);
        }
        return appointments;
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
        DoorCleaningAppointment appointment = this.getById(id);
        if (appointment == null) {
            return false;
        }
        String previousStatus = appointment.getStatus();
        if ("cancelled".equals(status) && !allowCancelAfterServiceStarted
                && CancellationPenaltyUtil.isServiceStarted(appointment.getDate(), appointment.getTimeSlot())) {
            throw new IllegalArgumentException("服务已开始或已结束，无法取消；如有特殊情况请联系客服或在管理后台操作强制取消。");
        }
        appointment.setStatus(status);
        if (rejectReason != null && !rejectReason.trim().isEmpty()) {
            // 取消申请被拒绝时状态会回到 confirmed，也需要保留拒绝原因给用户端展示
            setFieldStringValue(appointment, "rejectReason", rejectReason.trim());
        }
        if ("cancelled".equals(status) && isCancellationPenaltyEligiblePreviousStatus(previousStatus)
                && CancellationPenaltyUtil.isInPenaltyWindow(appointment.getDate(), appointment.getTimeSlot())) {
            BigDecimal penalty = CancellationPenaltyUtil.calculatePenaltyAmount(getFieldBigDecimalValue(appointment, "price"));
            setFieldBigDecimalValue(appointment, "cancellationPenaltyAmount", penalty);
        }
        boolean updated = this.updateById(appointment);

        if (updated) {
            try {
                serviceScheduleService.invalidateAvailableTimeSlotsCache(
                        appointment.getDate(),
                        appointment.getServiceType() != null ? appointment.getServiceType() : "door-cleaning",
                        appointment.getStoreId());
            } catch (Exception ex) {
                logger.warn("清除可预约时段缓存失败, appointmentId={}", id, ex);
            }
        }

        // 预约状态首次从「非已确认/已分配」->「已确认/已分配」时，立即生成系统通知（便于用户端展示）
        if (updated && notificationService != null
                && !isStaffConfirmedStatus(previousStatus)
                && isStaffConfirmedStatus(status)
                && appointment.getUserId() != null) {
            Notification n = new Notification();
            n.setUserId(appointment.getUserId());
            n.setTitle("预约已确认");
            // 组合“日期 + 时间段/开始时间”，避免出现 2026-03-20T00:00 这种无意义时间
            String when = buildDoorCleaningWhen(appointment);
            n.setContent("您的上门铲屎预约已确认（预约单#" + appointment.getId() + "）"
                    + (when != null && !when.isEmpty() ? "，服务时间：" + when : "，请留意服务时间。")
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

    /** 已确认/取消审核中等状态下取消，才可能涉及违约金或服务已开始限制 */
    private static boolean isCancellationPenaltyEligiblePreviousStatus(String status) {
        if (status == null) {
            return false;
        }
        return isStaffConfirmedStatus(status) || "cancel_pending".equals(status);
    }

    private String buildDoorCleaningWhen(DoorCleaningAppointment appointment) {
        if (appointment == null) return "";
        // 优先使用 date + timeSlot（LocalDate + 时间段）
        if (appointment.getDate() != null && appointment.getTimeSlot() != null && !appointment.getTimeSlot().trim().isEmpty()) {
            return appointment.getDate().toString() + " " + appointment.getTimeSlot();
        }
        // 若 date/timeSlot 缺失，则回退到 appointmentDate，但格式化避免出现 T00:00
        if (appointment.getAppointmentDate() != null) {
            return appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        // 部分字段缺失的兜底
        if (appointment.getDate() != null) return appointment.getDate().toString();
        if (appointment.getTimeSlot() != null) return appointment.getTimeSlot();
        return "";
    }

    private static BigDecimal getFieldBigDecimalValue(DoorCleaningAppointment appointment, String fieldName) {
        if (appointment == null || fieldName == null || fieldName.isEmpty()) return null;
        try {
            Field field = DoorCleaningAppointment.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(appointment);
            if (value instanceof BigDecimal) return (BigDecimal) value;
        } catch (Exception ignore) {
        }
        return null;
    }

    private static void setFieldBigDecimalValue(DoorCleaningAppointment appointment, String fieldName, BigDecimal value) {
        if (appointment == null || fieldName == null || fieldName.isEmpty()) return;
        try {
            Field field = DoorCleaningAppointment.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(appointment, value);
        } catch (Exception ignore) {
        }
    }

    private static void setFieldStringValue(DoorCleaningAppointment appointment, String fieldName, String value) {
        if (appointment == null || fieldName == null || fieldName.isEmpty()) return;
        try {
            Field field = DoorCleaningAppointment.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(appointment, value);
        } catch (Exception ignore) {
        }
    }

    @Override
    public IPage<DoorCleaningAppointment> getAppointmentPage(Page<DoorCleaningAppointment> page,
                                                             String status,
                                                             String keyword,
                                                             String serviceType,
                                                             Long storeId) {
        QueryWrapper<DoorCleaningAppointment> wrapper = new QueryWrapper<>();

        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }

        if (serviceType != null && !serviceType.isEmpty()) {
            // 历史数据可能为 litter，与 door-cleaning 同属上门铲屎业务
            if ("door-cleaning".equalsIgnoreCase(serviceType) || "litter".equalsIgnoreCase(serviceType)) {
                wrapper.and(w -> w.eq("service_type", "door-cleaning").or().eq("service_type", "litter"));
            } else {
                wrapper.eq("service_type", serviceType);
            }
        }

        // storeId=-1 表示员工未分配门店，不按门店筛选，显示全部预约
        // 兼容历史/异常数据：store_id 为空时仍应在「按门店查看」时可见
        if (storeId != null && storeId >= 0) {
            wrapper.and(w -> w.eq("store_id", storeId).or().isNull("store_id"));
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("contact_name", keyword)
                    .or()
                    .like("contact_phone", keyword)
                    .or()
                    .like("location", keyword)
                    .or()
                    .like("service_type", keyword));
        }

        wrapper.orderByDesc("create_time");
        IPage<DoorCleaningAppointment> pageResult = this.page(page, wrapper);
        if (pageResult != null && pageResult.getRecords() != null) {
            pageResult.getRecords().forEach(this::enrichAppointment);
        }
        return pageResult;
    }

    private void enrichAppointment(DoorCleaningAppointment appointment) {
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

        if (appointment.getServiceId() != null && litterServiceService != null) {
            try {
                LitterService ls = litterServiceService.getLitterServiceById(appointment.getServiceId());
                if (ls != null && StringUtils.hasText(ls.getName())) {
                    appointment.setServiceName(ls.getName());
                }
            } catch (Exception e) {
                logger.warn("查询上门铲屎服务项目名称失败, serviceId: {}", appointment.getServiceId(), e);
            }
        }
    }
}

