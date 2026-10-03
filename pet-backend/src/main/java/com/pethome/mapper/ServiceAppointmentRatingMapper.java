package com.pethome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethome.entity.ServiceAppointmentRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface ServiceAppointmentRatingMapper extends BaseMapper<ServiceAppointmentRating> {

    ServiceAppointmentRating findByAppointmentAndUser(
        @Param("appointmentType") String appointmentType,
        @Param("appointmentId") Long appointmentId,
        @Param("userId") Long userId);

    BigDecimal avgRatingByMemberId(@Param("memberId") Long memberId);
}
