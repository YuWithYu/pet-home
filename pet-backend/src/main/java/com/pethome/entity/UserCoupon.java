package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户持券实例：一次领取一条，记录核销与返还轨迹
 *
 * <p>生命周期：unused（领取）→ used（下单核销）→ 订单取消时返还回 unused。
 */
@Data
@TableName("user_coupon")
@Schema(name = "UserCoupon", description = "用户持券")
public class UserCoupon {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "持券用户")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "关联优惠券模板")
    @TableField("coupon_id")
    private Long couponId;

    @Schema(description = "状态：unused未使用/used已使用/expired已过期")
    private String status;

    @Schema(description = "领取时间")
    @TableField("receive_time")
    private LocalDateTime receiveTime;

    @Schema(description = "失效时间")
    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    @Schema(description = "核销的订单ID")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "核销时间")
    @TableField("use_time")
    private LocalDateTime useTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
