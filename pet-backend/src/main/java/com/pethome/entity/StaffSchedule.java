package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 员工排班实体类
 */
@Data
@TableName("staff_schedule")
public class StaffSchedule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 员工admin id
     */
    @TableField("admin_id")
    private Long adminId;
    
    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 班次开始时间
     */
    @TableField("start_time")
    private LocalTime startTime;
    
    /**
     * 班次结束时间
     */
    @TableField("end_time")
    private LocalTime endTime;
    
    /**
     * 同一时段可接单数
     */
    private Integer capacity;
    
    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // Getter 和 Setter 方法（Lombok会自动生成，但为了兼容性手动添加）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

