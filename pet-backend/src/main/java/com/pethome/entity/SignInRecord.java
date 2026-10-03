package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 签到记录实体类
 */
@Data
@TableName("sign_in_records")
public class SignInRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 签到日期
     */
    @TableField("sign_date")
    private LocalDate signDate;
    
    /**
     * 获得的积分
     */
    @TableField("points")
    private Integer points;
    
    /**
     * 连续签到天数（签到当天的连续天数）
     */
    @TableField("consecutive_days")
    private Integer consecutiveDays;
    
    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}

