package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hospital_appointments")
@ApiModel(value = "HospitalAppointment对象", description = "宠物医院预约")
public class HospitalAppointment {

    @ApiModelProperty(value = "预约ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "宠物ID")
    private Long petId;

    @ApiModelProperty(value = "医院服务ID")
    private Long serviceId;

    @ApiModelProperty(value = "服务人员ID")
    private Long memberId;

    @ApiModelProperty(value = "服务类型")
    private String serviceType;

    @ApiModelProperty(value = "预约门店ID")
    private Long storeId;

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @ApiModelProperty(value = "预约日期")
    private LocalDate date;

    @ApiModelProperty(value = "预约时间段")
    private String timeSlot;

    @ApiModelProperty(value = "服务地址")
    private String location;

    @ApiModelProperty(value = "联系人姓名")
    private String contactName;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "备注信息")
    private String remark;

    @ApiModelProperty(value = "服务价格")
    private BigDecimal price;

    @ApiModelProperty(value = "预约状态：pending-待确认，confirmed-已确认，cancelled-已取消，completed-已完成")
    private String status;

    @ApiModelProperty(value = "工作人员拒绝原因（状态为已取消时由工作人员填写）")
    @TableField("reject_reason")
    private String rejectReason;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "取消时违约金（服务开始前0-2小时内取消为订单金额的40%）")
    private BigDecimal cancellationPenaltyAmount;

    @ApiModelProperty(value = "核销码")
    @TableField("verify_code")
    private String verifyCode;

    @ApiModelProperty(value = "是否已核销：0-未核销，1-已核销")
    private Integer isVerified;

    @ApiModelProperty(value = "核销时间")
    private LocalDateTime verifyTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "宠物名称")
    private String petName;

    @TableField(exist = false)
    @ApiModelProperty(value = "服务人员姓名")
    private String memberName;

    @TableField(exist = false)
    @ApiModelProperty(value = "服务人员电话")
    private String memberPhone;

    @TableField(exist = false)
    @ApiModelProperty(value = "预约门店名称")
    private String storeName;

    @TableField(exist = false)
    @ApiModelProperty(value = "宠物疫苗记录")
    private String petVaccinations;

    @TableField(exist = false)
    @ApiModelProperty(value = "宠物健康状况")
    private String petHealthStatus;

    @TableField(exist = false)
    @ApiModelProperty(value = "预约的具体服务项目名称（由 serviceId 关联 hospital_services）")
    private String serviceName;

    // 手动添加setter方法以确保兼容性
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Integer getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Integer isVerified) {
        this.isVerified = isVerified;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public LocalDateTime getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(LocalDateTime verifyTime) {
        this.verifyTime = verifyTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberPhone() {
        return memberPhone;
    }

    public void setMemberPhone(String memberPhone) {
        this.memberPhone = memberPhone;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public BigDecimal getCancellationPenaltyAmount() {
        return cancellationPenaltyAmount;
    }

    public void setCancellationPenaltyAmount(BigDecimal cancellationPenaltyAmount) {
        this.cancellationPenaltyAmount = cancellationPenaltyAmount;
    }
}
