package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物档案实体
 *
 * <p>类型统一修正：id / userId 统一为 Long，避免与 User.id（Long）、
 * 预约实体 petId（Long）、服务层 Long 参数之间发生 Integer↔Long 转换造成的
 * 隐式精度丢失、equals 比较恒不等（Long.equals(Integer) 永远为 false）
 * 以及 intValue() 截断导致大用户 ID 映射错误等归属校验 bug。
 */
@Data
@TableName("pet")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    /** 宠物种类（cat/dog 等） */
    @TableField("species")
    private String species;

    /** 品种 */
    @TableField("breed")
    private String breed;

    @TableField("age")
    private Integer age;

    @TableField("gender")
    private String gender;

    /** 绝育状态：yes/no/unknown */
    @TableField("sterilization")
    private String sterilization;

    /** 相亲意愿：yes/no */
    @TableField("dating")
    private String dating;

    @TableField("weight")
    private BigDecimal weight;

    /** 状态（active / inactive 等） */
    @TableField("status")
    private String status;

    @TableField("birthday")
    private LocalDate birthday;

    @TableField(value = "arrival_date", insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private LocalDate arrivalDate;

    @TableField("color")
    private String color;

    @TableField("description")
    private String description;

    @TableField("avatar")
    private String avatar;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
