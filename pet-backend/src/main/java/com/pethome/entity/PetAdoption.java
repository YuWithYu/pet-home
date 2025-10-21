package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("pet_adoption")
@ApiModel(value = "PetAdoption对象", description = "待领养宠物")
public class PetAdoption {

    @ApiModelProperty(value = "宠物ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "宠物姓名")
    private String petName;

    @ApiModelProperty(value = "品种")
    private String breed;

    @ApiModelProperty(value = "年龄")
    private Integer age;

    @ApiModelProperty(value = "性别")
    private String gender;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "图片URL")
    private String imageUrl;

    @ApiModelProperty(value = "领养费用")
    private BigDecimal adoptionFee;

    @ApiModelProperty(value = "状态：available-可领养，adopted-已领养")
    private String status;

    @ApiModelProperty(value = "位置")
    private String location;

    @ApiModelProperty(value = "联系信息")
    private String contactInfo;

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