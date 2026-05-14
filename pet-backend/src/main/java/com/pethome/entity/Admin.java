package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员实体类
 * 用于管理员和员工账号管理，从 user 表中分离出来
 */
@Data
@TableName("admin")
public class Admin {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 登录账号
     */
    private String username;
    
    /**
     * 登录密码（加密存储）
     */
    private String password;
    
    /**
     * 员工姓名
     */
    private String name;
    
    /**
     * 角色：admin（平台管理员）/ store_admin（分店管理员）/ staff（普通员工）
     */
    private String role;
    
    /**
     * 所属店铺ID（关联stores表）
     * 用于多商家模式：店铺管理员只能管理自己店铺的数据
     * null 表示平台管理员（可以管理所有店铺）
     * updateStrategy=IGNORED：允许显式更新为 null，以支持取消店铺关联
     */
    @TableField(value = "store_id", updateStrategy = FieldStrategy.IGNORED)
    private Long storeId;
    
    /**
     * 所属服务门店ID（关联service_stores表）
     * 用于排班与预约分配；null 表示平台级
     * updateStrategy=IGNORED：允许显式更新为 null，以支持取消门店关联
     */
    @TableField(value = "service_store_id", updateStrategy = FieldStrategy.IGNORED)
    private Long serviceStoreId;
    
    /**
     * 关联的医师ID（关联doctors表）
     * 用于医师账号管理
     */
    @TableField(value = "doctor_id")
    private Long doctorId;
    
    /**
     * 所属部门/服务类型（上门铲屎、宠物洗护、宠物医院等）
     */
    private String department;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态：1启用，0禁用
     */
    private Integer status;
    
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
    
    /** 评分（来自 service_member，非数据库字段） */
    @TableField(exist = false)
    private BigDecimal rating;
    
    /** 累计任务数（来自 service_member，非数据库字段） */
    @TableField(exist = false)
    private Integer totalTasks;
    
    /** 每日最大任务数（来自 service_member，非数据库字段） */
    @TableField(exist = false)
    private Integer maxTasksPerDay;
    
    /** 今日任务数（动态计算，非数据库字段） */
    @TableField(exist = false)
    private Integer todayWorkload;
    
    /** 关联的 service_member.id（用于前端详情等，非数据库字段） */
    @TableField(exist = false)
    private Long serviceMemberId;
    
    /**
     * 判断是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return "super_admin".equals(this.role) || "admin".equals(this.role);
    }

    /**
     * 判断是否为分店管理员（仅管理绑定门店）
     */
    public boolean isStoreAdmin() {
        return "store_admin".equals(this.role);
    }
    
    /**
     * 判断是否为普通员工
     */
    public boolean isStaff() {
        return "staff".equals(this.role);
    }
    
    /**
     * 判断账号是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }
    
    // Getter 和 Setter 方法（Lombok @Data 会自动生成，但为了确保兼容性，手动添加）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }
    
    public Long getServiceStoreId() { return serviceStoreId; }
    public void setServiceStoreId(Long serviceStoreId) { this.serviceStoreId = serviceStoreId; }
    
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Integer totalTasks) { this.totalTasks = totalTasks; }

    public Integer getMaxTasksPerDay() { return maxTasksPerDay; }
    public void setMaxTasksPerDay(Integer maxTasksPerDay) { this.maxTasksPerDay = maxTasksPerDay; }

    public Integer getTodayWorkload() { return todayWorkload; }
    public void setTodayWorkload(Integer todayWorkload) { this.todayWorkload = todayWorkload; }

    public Long getServiceMemberId() { return serviceMemberId; }
    public void setServiceMemberId(Long serviceMemberId) { this.serviceMemberId = serviceMemberId; }
}

