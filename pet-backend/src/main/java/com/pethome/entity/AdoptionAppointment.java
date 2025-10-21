package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("adoption_appointments")
@ApiModel(value = "AdoptionAppointment对象", description = "宠物领养预约")
public class AdoptionAppointment {

    @ApiModelProperty(value = "预约ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID（领养人）")
    private Long userId;

    @ApiModelProperty(value = "待领养宠物ID（pet_adoption表）")
    private Long adoptionPetId;

    @ApiModelProperty(value = "服务类型")
    private String serviceType;

    @ApiModelProperty(value = "预约日期（到店参观/领养日期）")
    private LocalDate appointmentDate;

    @ApiModelProperty(value = "预约时间段")
    private String timeSlot;

    @ApiModelProperty(value = "联系人姓名")
    private String contactName;

    @ApiModelProperty(value = "联系电话")
    private String contactPhone;

    @ApiModelProperty(value = "身份证号（领养验证用）")
    private String idCard;

    @ApiModelProperty(value = "领养人地址")
    private String address;

    @ApiModelProperty(value = "领养原因/养宠经验")
    private String reason;

    @ApiModelProperty(value = "备注信息")
    private String remark;

    @ApiModelProperty(value = "领养费用")
    private BigDecimal adoptionFee;

    @ApiModelProperty(value = "预约状态：pending-待审核，approved-已通过，rejected-已拒绝，completed-已完成，cancelled-已取消")
    private String status;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    // 手动添加setter和getter方法以确保IDE兼容性
    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}

