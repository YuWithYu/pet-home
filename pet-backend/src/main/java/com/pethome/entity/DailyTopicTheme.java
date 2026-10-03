package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 宠物专题主题分类（后台可增删改查，小程序 Tab 与此同步）
 */
@TableName("daily_topic_theme")
public class DailyTopicTheme {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 唯一标识，如 recommend、basic、feeding，用于 daily_topic.theme 关联 */
    private String code;
    /** 显示名称，如 推荐、基本知识 */
    private String name;
    @TableField("sort_order")
    private Integer sortOrder = 0;
    /** 1-启用 0-禁用 */
    private Integer status = 1;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
