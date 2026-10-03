package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一支付单（与业务状态解耦）
 *
 * biz_type=ORDER 对应商品订单；biz_type=APPOINTMENT 对应服务预约。
 * 预约状态机（pending → 管理员 confirmed）不受支付影响，
 * 支付完成仅标记本单已支付，业务侧按需关联查询。
 */
@Data
@TableName("payment")
public class Payment {

    public static final String BIZ_ORDER = "ORDER";
    public static final String BIZ_APPOINTMENT = "APPOINTMENT";

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_CLOSED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付单号（微信 out_trade_no，PAY 前缀） */
    private String payNo;

    /** 业务类型：ORDER / APPOINTMENT */
    private String bizType;

    /** 业务ID（订单ID或预约ID） */
    private Long bizId;

    /** 预约服务类型（grooming/hospital/door-cleaning，仅 APPOINTMENT） */
    private String serviceType;

    private Long userId;

    /** 应付金额（元，以业务侧为准） */
    private BigDecimal amount;

    /** 支付状态：0-待支付 1-已支付 2-已关闭 */
    private Integer status;

    /** 微信支付单号（回调回填） */
    private String transactionId;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
