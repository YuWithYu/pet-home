package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约变更申请（用户申请变更时间/地址等，工作人员二次确认）
 */
@Data
@TableName("appointment_change_request")
@ApiModel(value = "AppointmentChangeRequest", description = "预约变更申请")
public class AppointmentChangeRequest {

    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("预约类型：unified / grooming / hospital / door_cleaning")
    @TableField("appointment_type")
    private String appointmentType;

    @ApiModelProperty("预约ID（对应各预约表主键）")
    @TableField("appointment_id")
    private Long appointmentId;

    @ApiModelProperty("申请变更的日期")
    @TableField("requested_date")
    private LocalDate requestedDate;

    @ApiModelProperty("申请变更的时间段")
    @TableField("requested_time_slot")
    private String requestedTimeSlot;

    @ApiModelProperty("申请变更的地址/地点")
    @TableField("requested_location")
    private String requestedLocation;

    @ApiModelProperty("申请变更的宠物ID（三类预约均支持）")
    @TableField("requested_pet_id")
    private Long requestedPetId;

    @ApiModelProperty("申请变更的备注")
    @TableField("requested_remark")
    private String requestedRemark;

    @ApiModelProperty("申请变更的联系电话")
    @TableField("requested_contact_phone")
    private String requestedContactPhone;

    @ApiModelProperty("上门铲屎：申请变更的钥匙给予方式")
    @TableField("requested_key_handover_method")
    private String requestedKeyHandoverMethod;

    @ApiModelProperty("上门铲屎：申请变更的钥匙归还方式")
    @TableField("requested_key_return_method")
    private String requestedKeyReturnMethod;

    @ApiModelProperty("状态：pending-待确认，approved-已同意，rejected-已拒绝，cancelled-用户取消变更")
    private String status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // 手动添加 getter/setter 以兼容 Lombok 失效场景
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getRequestedTimeSlot() {
        return requestedTimeSlot;
    }

    public void setRequestedTimeSlot(String requestedTimeSlot) {
        this.requestedTimeSlot = requestedTimeSlot;
    }

    public String getRequestedLocation() {
        return requestedLocation;
    }

    public void setRequestedLocation(String requestedLocation) {
        this.requestedLocation = requestedLocation;
    }

    public Long getRequestedPetId() {
        return requestedPetId;
    }

    public void setRequestedPetId(Long requestedPetId) {
        this.requestedPetId = requestedPetId;
    }

    public String getRequestedRemark() {
        return requestedRemark;
    }

    public void setRequestedRemark(String requestedRemark) {
        this.requestedRemark = requestedRemark;
    }

    public String getRequestedContactPhone() {
        return requestedContactPhone;
    }

    public void setRequestedContactPhone(String requestedContactPhone) {
        this.requestedContactPhone = requestedContactPhone;
    }

    public String getRequestedKeyHandoverMethod() {
        return requestedKeyHandoverMethod;
    }

    public void setRequestedKeyHandoverMethod(String requestedKeyHandoverMethod) {
        this.requestedKeyHandoverMethod = requestedKeyHandoverMethod;
    }

    public String getRequestedKeyReturnMethod() {
        return requestedKeyReturnMethod;
    }

    public void setRequestedKeyReturnMethod(String requestedKeyReturnMethod) {
        this.requestedKeyReturnMethod = requestedKeyReturnMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
