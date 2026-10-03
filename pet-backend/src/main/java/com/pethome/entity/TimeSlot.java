package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("time_slots")
public class TimeSlot {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("service_type")
    private String serviceType;

    @TableField("store_id")
    private Long storeId;

    @TableField("time_slot")
    private String timeSlot;

    @TableField("max_bookings")
    private Integer maxBookings;

    @TableField("is_active")
    private Boolean isActive;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // 手动添加 getter/setter 方法以确保编译通过（Lombok 可能在某些情况下无法正确生成）
    public String getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getTimeSlot() {
        return timeSlot;
    }
    
    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
    
    public Integer getMaxBookings() {
        return maxBookings;
    }
    
    public void setMaxBookings(Integer maxBookings) {
        this.maxBookings = maxBookings;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

