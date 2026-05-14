package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预约提醒发送记录（防重复发送 24h/2h 提醒）
 */
@Data
@TableName("appointment_reminder_sent")
public class AppointmentReminderSent {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("appointment_type")
    private String appointmentType;
    @TableField("appointment_id")
    private Long appointmentId;
    @TableField("remind_type")
    private String remindType;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
