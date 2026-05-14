package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("order_logistics_track")
public class OrderLogisticsTrack {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private LocalDateTime trackingTime;
    private String trackingInfo;
    private String location;
    private LocalDateTime createTime;

    // 构造函数
    public OrderLogisticsTrack() {
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getTrackingTime() {
        return trackingTime;
    }

    public void setTrackingTime(LocalDateTime trackingTime) {
        this.trackingTime = trackingTime;
    }

    public String getTrackingInfo() {
        return trackingInfo;
    }

    public void setTrackingInfo(String trackingInfo) {
        this.trackingInfo = trackingInfo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
