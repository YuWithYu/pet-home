package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务人员可预约时间表实体类
 */
@Data
@TableName("service_schedule")
public class ServiceSchedule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 服务人员ID
     */
    @TableField("member_id")
    private Long memberId;
    
    /**
     * 所属门店ID（排班所属门店，独立于成员的 store_id，避免团队保存时覆盖导致跨店排班消失）
     */
    @TableField("store_id")
    private Long storeId;
    
    /**
     * 服务类型（door-cleaning等）
     */
    @TableField("service_type")
    private String serviceType;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 时间段，例如 "09:00-10:00"
     */
    @TableField("time_slot")
    private String timeSlot;
    
    /**
     * 该时间段最大可预约数
     */
    @TableField("max_capacity")
    private Integer maxCapacity;
    
    /**
     * 当前已预约数
     */
    @TableField("reserved_count")
    private Integer reservedCount;
    
    /**
     * 是否可用：1-可用，0-不可用
     */
    @TableField("is_available")
    private Boolean isAvailable;
    
    /**
     * 状态：可预约、已预约
     */
    private String status;
    
    /**
     * 若被占用，关联appointment.id
     */
    @TableField("task_id")
    private Long taskId;
    
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
    
    // 手动添加 getter/setter 方法以确保编译通过（Lombok 可能在某些情况下无法正确生成）
    public Long getMemberId() {
        return memberId;
    }
    
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
    
    public Long getStoreId() {
        return storeId;
    }
    
    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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
    
    public Integer getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    
    public Integer getReservedCount() {
        return reservedCount;
    }
    
    public void setReservedCount(Integer reservedCount) {
        this.reservedCount = reservedCount;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

