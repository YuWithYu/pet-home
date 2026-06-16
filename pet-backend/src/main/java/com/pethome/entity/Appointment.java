package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统一预约实体类
 * 用于管理所有类型的服务预约
 */
@Data
@TableName("appointment")
public class Appointment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID（小程序端用户）
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 宠物ID（可选）
     */
    @TableField("pet_id")
    private Long petId;
    
    /**
     * 服务人员ID（分配后）
     */
    @TableField("member_id")
    private Long memberId;
    
    /**
     * 管理员ID（如果是后台创建）
     */
    @TableField("admin_id")
    private Long adminId;
    
    /**
     * 服务类型：上门铲屎、宠物洗护、宠物医院
     */
    @TableField("service_type")
    private String serviceType;
    
    /**
     * 预约日期时间
     */
    @TableField("appointment_date")
    private LocalDateTime appointmentDate;
    
    /**
     * 预约日期（冗余字段，便于查询）
     */
    private LocalDate date;
    
    /**
     * 时间段：09:00-10:00, 10:00-11:00 等
     */
    @TableField("time_slot")
    private String timeSlot;
    
    /**
     * 状态：pending（待分配）、assigned（已分配）、confirmed（已确认）、completed（已完成）、cancelled（已取消）
     */
    private String status;
    
    /**
     * 备注信息
     */
    private String remark;
    
    /**
     * 联系人姓名
     */
    @TableField("contact_name")
    private String contactName;
    
    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;
    
    /**
     * 服务地点
     */
    private String location;
    
    /**
     * 服务价格
     */
    private BigDecimal price;
    
    /**
     * 验证码
     */
    @TableField("verify_code")
    private String verifyCode;
    
    /**
     * 是否已验证：0-未验证，1-已验证
     */
    @TableField("is_verified")
    private Integer isVerified;
    
    /**
     * 验证时间
     */
    @TableField("verify_time")
    private LocalDateTime verifyTime;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 扩展字段（不存储到数据库）
    @TableField(exist = false)
    private String memberName; // 服务人员姓名
    
    @TableField(exist = false)
    private String userName; // 用户姓名
    
    @TableField(exist = false)
    private String petName; // 宠物名称
    
    // 手动添加 getter/setter 以确保兼容性（Lombok 可能在某些情况下无法正确生成）
    public Integer getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Integer isVerified) {
        this.isVerified = isVerified;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    // 手动添加 getter 方法以确保编译通过（Lombok 可能在某些情况下无法正确生成）
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public String getLocation() {
        return location;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }
    
    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }
}
