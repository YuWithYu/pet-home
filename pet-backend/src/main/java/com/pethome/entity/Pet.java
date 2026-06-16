package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@TableName("pet")  // 修正表名：数据库中是pet（单数），不是pets
@JsonIgnoreProperties(ignoreUnknown = true)  // 允许Jackson忽略未知字段，避免反序列化错误
public class Pet {
    @TableId(type = IdType.AUTO)
    private Integer id;  // 数据库中是int，不是bigint
    
    @TableField("user_id")
    private Integer userId;  // 数据库中是int，不是bigint
    @TableField("name")
    private String name;
    
    @TableField("species")
    private String species;  // 宠物种类（猫/狗等）
    
    @TableField("breed")
    private String breed;  // 品种
    
    @TableField("age")
    private Integer age;
    
    @TableField("gender")
    private String gender;
    
    @TableField("sterilization")
    private String sterilization;  // 绝育状态：yes/no/unknown
    
    @TableField("dating")
    private String dating;  // 相亲意愿：yes/no
    
    @TableField("weight")
    private java.math.BigDecimal weight;  // 体重
    
    @TableField("status")
    private String status;  // 状态（active等）
    
    @TableField("birthday")
    private java.time.LocalDate birthday;  // 生日
    
    @TableField(value = "arrival_date", insertStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.IGNORED, updateStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.IGNORED)
    private java.time.LocalDate arrivalDate;  // 到家日期
    
    @TableField("color")
    private String color;  // 颜色
    
    @TableField("description")
    private String description;
    
    @TableField("avatar")
    private String avatar;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;

    // 构造函数
    public Pet() {
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSterilization() {
        return sterilization;
    }

    public void setSterilization(String sterilization) {
        this.sterilization = sterilization;
    }

    public String getDating() {
        return dating;
    }

    public void setDating(String dating) {
        this.dating = dating;
    }

    public java.math.BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(java.math.BigDecimal weight) {
        this.weight = weight;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.time.LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(java.time.LocalDate birthday) {
        this.birthday = birthday;
    }

    public java.time.LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(java.time.LocalDate arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
