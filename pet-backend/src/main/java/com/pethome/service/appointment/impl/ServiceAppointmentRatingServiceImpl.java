package com.pethome.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.ServiceAppointmentRating;
import com.pethome.mapper.ServiceAppointmentRatingMapper;
import com.pethome.service.appointment.ServiceAppointmentRatingService;
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

    @Autowired
    private ServiceAppointmentRatingMapper ratingMapper;

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
}
