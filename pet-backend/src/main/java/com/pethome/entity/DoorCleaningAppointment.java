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
@TableName("door_cleaning_appointment")
@ApiModel(value = "DoorCleaningAppointment对象", description = "上门铲屎服务预约")
public class DoorCleaningAppointment {

    @ApiModelProperty(value = "预约ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "宠物ID")
    private Long petId;

    @ApiModelProperty(value = "服务人员ID")
    private Long memberId;

    @ApiModelProperty(value = "服务类型")
    private String serviceType;

    @ApiModelProperty(value = "预约门店/所属门店ID")
    private Long storeId;

    @ApiModelProperty(value = "预约的具体服务项目ID（litter_services.id）")
    @TableField("service_id")
    private Long serviceId;

    @ApiModelProperty(value = "预约日期时间")
    private LocalDateTime appointmentDate;

    @ApiModelProperty(value = "预约时间段")
    private String timeSlot;

    @ApiModelProperty(value = "预约日期")
    private LocalDate date;

    @ApiModelProperty(value = "预约状态：pending-待确认，confirmed-已确认，cancelled-已取消，completed-已完成")
    private String status;

    @ApiModelProperty(value = "工作人员拒绝原因（状态为已取消时由工作人员填写）")
    @TableField("reject_reason")
    private String rejectReason;

    @ApiModelProperty(value = "备注信息")
    private String remark;

    @ApiModelProperty(value = "联系人姓名")
    private String contactName;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "服务价格")
    private BigDecimal price;

    @ApiModelProperty(value = "服务地点")
    private String location;

    @ApiModelProperty(value = "钥匙给予方式：密码锁、丰巢存件、面交、闪送/跑腿、家中有人、藏于指定位置、其他")
    private String keyHandoverMethod;

    @ApiModelProperty(value = "钥匙归还方式：同上选项")
    private String keyReturnMethod;

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
    private String petName;

    @TableField(exist = false)
    private String memberName;

    @TableField(exist = false)
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
    @ApiModelProperty(value = "预约的具体服务项目名称（由 serviceId 关联 litter_services）")
    private String serviceName;

    // 手动添加 getter/setter 方法以确保 IDE 兼容性
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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

    public String getKeyHandoverMethod() {
        return keyHandoverMethod;
    }

    public void setKeyHandoverMethod(String keyHandoverMethod) {
        this.keyHandoverMethod = keyHandoverMethod;
    }

    public String getKeyReturnMethod() {
        return keyReturnMethod;
    }

    public void setKeyReturnMethod(String keyReturnMethod) {
        this.keyReturnMethod = keyReturnMethod;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

