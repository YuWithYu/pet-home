package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("points_record")
public class PointsRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("type")
    private String type; // 'earn' 获取, 'spend' 消耗
    
    @TableField("points")
    private Integer points; // 积分数量（数据库列名为points）
    
    @TableField("description")
    private String description; // 描述
    
    @TableField(exist = false)
    private String source; // 来源：'signin' 签到, 'task' 任务, 'feed' 投食, 'exchange' 兑换等（暂时不存在于数据库）
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    // 构造函数
    public PointsRecord() {
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getPoints() {
        return points;
    }
    
    public void setPoints(Integer points) {
        this.points = points;
    }
    
    // 兼容方法：getAmount 返回 points
    public Integer getAmount() {
        return points;
    }
    
    // 兼容方法：setAmount 设置 points
    public void setAmount(Integer amount) {
        this.points = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

