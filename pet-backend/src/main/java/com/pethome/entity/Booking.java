package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("bookings")
public class Booking {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("service_type")
    private String serviceType;

    @TableField("service_id")
    private Long serviceId;

    @TableField("service_name")
    private String serviceName;

    @TableField("user_id")
    private Long userId;

    @TableField("booking_date")
    private LocalDate bookingDate;

    @TableField("time_slot")
    private String timeSlot;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_phone")
    private String contactPhone;

    private String address;

    @TableField("pet_info")
    private String petInfo;

    @TableField("pet_count")
    private Integer petCount;

    @TableField("pet_photos")
    private String petPhotos;

    private String duration;

    @TableField("selected_extras")
    private String selectedExtras;

    @TableField("emergency_contact")
    private String emergencyContact;

    @TableField("emergency_phone")
    private String emergencyPhone;

    @TableField("special_needs")
    private String specialNeeds;

    @TableField("coupon_id")
    private Long couponId;

    @TableField("payment_method")
    private String paymentMethod;

    private String remark;

    @TableField("total_price")
    private BigDecimal totalPrice;

    private String status; // pending/confirmed/completed/cancelled

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

