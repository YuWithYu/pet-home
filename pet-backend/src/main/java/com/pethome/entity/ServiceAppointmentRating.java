package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务预约评价
 */
@Data
@TableName("service_appointment_rating")
public class ServiceAppointmentRating {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String appointmentType;
    private Long appointmentId;
    private Long memberId;
    private Long userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createTime;
}
