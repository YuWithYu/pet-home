package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.common.BusinessException;
import com.pethome.entity.DoorCleaningAppointment;
import com.pethome.entity.GroomingAppointment;
import com.pethome.entity.HospitalAppointment;
import com.pethome.entity.ServiceAppointmentRating;
import com.pethome.entity.ServiceMember;
import com.pethome.mapper.ServiceAppointmentRatingMapper;
import com.pethome.service.appointment.DoorCleaningAppointmentService;
import com.pethome.service.appointment.GroomingAppointmentService;
import com.pethome.service.appointment.HospitalAppointmentService;
import com.pethome.service.appointment.ServiceAppointmentRatingService;
import com.pethome.service.appointment.ServiceMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceAppointmentRatingServiceImpl
        extends ServiceImpl<ServiceAppointmentRatingMapper, ServiceAppointmentRating>
        implements ServiceAppointmentRatingService {

    private static final Logger log = LoggerFactory.getLogger(ServiceAppointmentRatingServiceImpl.class);

    @Autowired
    private ServiceAppointmentRatingMapper ratingMapper;

    @Autowired(required = false)
    private DoorCleaningAppointmentService doorCleaningAppointmentService;

    @Autowired(required = false)
    private GroomingAppointmentService groomingAppointmentService;

    @Autowired(required = false)
    private HospitalAppointmentService hospitalAppointmentService;

    @Autowired
    private ServiceMemberService serviceMemberService;

    @Override
    public ServiceAppointmentRating findByAppointmentAndUser(String appointmentType, Long appointmentId, Long userId) {
        return ratingMapper.findByAppointmentAndUser(appointmentType, appointmentId, userId);
    }

    @Override
    public BigDecimal avgRatingByMemberId(Long memberId) {
        return ratingMapper.avgRatingByMemberId(memberId);
    }

    @Override
    public Map<Long, BigDecimal> avgRatingByMemberIds(Collection<Long> memberIds) {
        Map<Long, BigDecimal> result = new HashMap<>();
        if (memberIds == null || memberIds.isEmpty()) {
            return result;
        }
        QueryWrapper<ServiceAppointmentRating> wrapper = new QueryWrapper<>();
        wrapper.select("member_id", "AVG(rating) AS avg_rating")
                .in("member_id", memberIds)
                .groupBy("member_id");
        List<Map<String, Object>> rows = ratingMapper.selectMaps(wrapper);
        if (rows == null) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            Object id = row.get("member_id");
            Object avg = row.get("avg_rating");
            if (id instanceof Number && avg instanceof Number) {
                result.put(((Number) id).longValue(), new BigDecimal(avg.toString()));
            }
        }
        return result;
    }

    @Override
    public List<ServiceAppointmentRating> listByMemberId(Long memberId) {
        if (memberId == null) {
            return Collections.emptyList();
        }
        QueryWrapper<ServiceAppointmentRating> q = new QueryWrapper<>();
        q.eq("member_id", memberId).orderByDesc("create_time");
        return ratingMapper.selectList(q);
    }

    @Override
    public ServiceAppointmentRating submitRating(Long userId, String appointmentType, Long appointmentId,
                                                 int rating, String comment) {
        Long memberId = null;
        Long appointmentUserId = null;

        switch (appointmentType) {
            case "door-cleaning":
                if (doorCleaningAppointmentService != null) {
                    DoorCleaningAppointment apt = doorCleaningAppointmentService.getAppointmentById(appointmentId);
                    if (apt == null) {
                        throw new BusinessException("预约不存在");
                    }
                    if (!"completed".equals(apt.getStatus())) {
                        throw new BusinessException("只能对已完成的订单评价");
                    }
                    memberId = apt.getMemberId();
                    appointmentUserId = apt.getUserId();
                }
                break;
            case "grooming":
                if (groomingAppointmentService != null) {
                    GroomingAppointment apt = groomingAppointmentService.getAppointmentById(appointmentId);
                    if (apt == null) {
                        throw new BusinessException("预约不存在");
                    }
                    if (!"completed".equals(apt.getStatus())) {
                        throw new BusinessException("只能对已完成的订单评价");
                    }
                    memberId = apt.getMemberId();
                    appointmentUserId = apt.getUserId();
                }
                break;
            case "hospital":
                if (hospitalAppointmentService != null) {
                    HospitalAppointment apt = hospitalAppointmentService.getAppointmentById(appointmentId);
                    if (apt == null) {
                        throw new BusinessException("预约不存在");
                    }
                    if (!"completed".equals(apt.getStatus())) {
                        throw new BusinessException("只能对已完成的订单评价");
                    }
                    memberId = apt.getMemberId();
                    appointmentUserId = apt.getUserId();
                }
                break;
            default:
                throw new BusinessException("不支持的预约类型");
        }

        if (memberId == null) {
            throw new BusinessException("该预约未分配服务人员，无法评价");
        }
        if (!userId.equals(appointmentUserId)) {
            throw new BusinessException("只能评价自己的订单");
        }

        ServiceAppointmentRating existing = findByAppointmentAndUser(appointmentType, appointmentId, userId);
        if (existing != null) {
            throw new BusinessException("您已评价过该订单");
        }

        ServiceAppointmentRating entity = new ServiceAppointmentRating();
        entity.setAppointmentType(appointmentType);
        entity.setAppointmentId(appointmentId);
        entity.setMemberId(memberId);
        entity.setUserId(userId);
        entity.setRating(rating);
        entity.setComment(comment != null && !comment.isEmpty() ? comment : null);
        this.save(entity);

        BigDecimal avgRating = avgRatingByMemberId(memberId);
        ServiceMember member = serviceMemberService.getMemberById(memberId);
        if (member != null && avgRating != null) {
            member.setRating(avgRating);
            serviceMemberService.updateMember(member);
        }
        return entity;
    }

    @Override
    public Map<String, Object> checkRatedQuietly(String appointmentType, Long appointmentId, Long userId) {
        try {
            ServiceAppointmentRating rating = findByAppointmentAndUser(appointmentType, appointmentId, userId);
            if (rating == null) {
                return Map.of("rated", false, "rating", 0);
            }
            return Map.of("rated", true, "rating", rating.getRating() != null ? rating.getRating() : 0);
        } catch (Exception e) {
            log.warn("查询评价状态失败，按未评价降级: appointmentType={}, appointmentId={}",
                    appointmentType, appointmentId, e);
            return Map.of("rated", false);
        }
    }
}
