package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务门店实体类
 */
@Data
@TableName("service_stores")
public class ServiceStore {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 门店名称
     */
    private String storeName;
    
    public String getStoreName() {
        return storeName;
    }
    
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 营业时间
     */
    private String businessHours;
    
    /**
     * 提供的服务类型（逗号分隔）
     * 例如：grooming,hospital,litter
     */
    private String services;
    
    /**
     * 门店描述
     */
    private String description;
    
    /**
     * 最大容量（可容纳数量）
     */
    private Integer maxCapacity;
    
    /**
     * 当前预约数量
     */
    private Integer currentBookings;
    
    /**
     * 门店图片URL
     */
    private String imageUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 状态：active-营业中, inactive-暂停营业, closed-已关闭
     */
    private String status;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 是否默认门店
     */
    private Boolean isDefault;
    
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
    
    /**
     * 是否删除
     */
    @TableField("is_deleted")
    private Boolean isDeleted;
}

