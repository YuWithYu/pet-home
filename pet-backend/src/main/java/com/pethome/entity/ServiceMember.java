package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务人员实体类
 */
@Data
@TableName("service_member")
public class ServiceMember {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 服务类型（如：door-cleaning, grooming等）
     */
    @TableField("service_type")
    private String serviceType;
    
    /**
     * 所属门店ID（关联 stores 或 service_stores，null 表示平台级可参与任意门店）
     */
    @TableField("store_id")
    private Long storeId;
    
    /**
     * 服务人员姓名
     */
    @TableField("member_name")
    private String memberName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
    
    /**
     * 每日最大任务数
     */
    @TableField("max_tasks_per_day")
    private Integer maxTasksPerDay;
    
    /**
     * 评分（0-5分）
     */
    private BigDecimal rating;
    
    /**
     * 累计完成任务数
     */
    @TableField("total_tasks")
    private Integer totalTasks;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    // 手动添加 getter/setter 方法以确保 IDE 兼容性
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getMaxTasksPerDay() {
        return maxTasksPerDay;
    }

    public void setMaxTasksPerDay(Integer maxTasksPerDay) {
        this.maxTasksPerDay = maxTasksPerDay;
    }

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getRating() {
        return rating;
    }
    
    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }
}

