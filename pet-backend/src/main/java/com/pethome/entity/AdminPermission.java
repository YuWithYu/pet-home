package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员权限实体类
 */
@Data
@TableName("admin_permissions")
public class AdminPermission {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 管理员账号ID（关联admin表）
     */
    @TableField("admin_id")
    private Long adminId;
    
    /**
     * 权限代码（对应路由路径或功能代码）
     * 例如：/content/banner, /mall/products, /services/appointments 等
     */
    @TableField("permission_code")
    private String permissionCode;
    
    /**
     * 权限名称（用于显示）
     * 例如：轮播图管理、商品管理、预约管理等
     */
    @TableField("permission_name")
    private String permissionName;
    
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
    
    // 手动添加 getter 和 setter 方法以确保编译通过
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
