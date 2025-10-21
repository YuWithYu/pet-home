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
@TableName("boarding_appointments")
@ApiModel(value = "BoardingAppointment对象", description = "宠物寄养预约")
public class BoardingAppointment {

    @ApiModelProperty(value = "预约ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "宠物ID")
    private Long petId;

    @ApiModelProperty(value = "寄养服务ID")
    private Long serviceId;

    @ApiModelProperty(value = "服务类型")
    private String serviceType;

    @ApiModelProperty(value = "寄养开始日期")
    private LocalDate startDate;

    @ApiModelProperty(value = "寄养结束日期")
    private LocalDate endDate;

    @ApiModelProperty(value = "寄养天数")
    private Integer days;

    @ApiModelProperty(value = "送达时间段")
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

