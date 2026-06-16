package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品商家实体类
 */
@Data
@TableName("stores")
public class Store {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 商家账号ID（关联user表）
     * 注意：执行 database/add_store_owner_simple.sql 后，将 exist = false 改为 exist = true
     */
    @TableField(value = "owner_id", exist = false)
    private Long ownerId;
    
    /**
     * 商家账号用户名（冗余字段，便于查询）
     * 注意：执行 database/add_store_owner_simple.sql 后，将 exist = false 改为 exist = true
     */
    @TableField(value = "owner_username", exist = false)
    private String ownerUsername;
    
    /**
     * 商家名称
     */
    private String name;
    
    /**
     * 商家描述
     */
    private String description;
    
    /**
     * 商家头像
     */
    private String avatar;
    
    /**
     * 商家logo（兼容字段）
     */
    private String logo;
    
    /**
     * 状态：1-启用，0-禁用
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
    
    // 手动添加 getter 和 setter 方法以确保编译通过（Lombok 可能在某些情况下无法正确生成）
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public String getLogo() {
        return logo;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public void setLogo(String logo) {
        this.logo = logo;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
}
