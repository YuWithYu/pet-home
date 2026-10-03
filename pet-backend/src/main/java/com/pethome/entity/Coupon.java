package com.pethome.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板：管理端定义的券规则（类型/门槛/面额/库存/领取窗口/适用范围）
 *
 * <p>类型说明：
 * <ul>
 *   <li>full_reduction 满减券：满 threshold_amount 减 discount_amount</li>
 *   <li>discount 折扣券：满 threshold_amount 打 discount_rate 折，最高优惠 max_discount_amount</li>
 * </ul>
 */
@Data
@TableName("coupon")
@Schema(name = "Coupon", description = "优惠券模板")
public class Coupon {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "券名称")
    private String name;

    @Schema(description = "类型：full_reduction满减/discount折扣")
    @TableField("type")
    private String type;

    @Schema(description = "使用门槛金额（满X元可用，0=无门槛）")
    @TableField("threshold_amount")
    private BigDecimal thresholdAmount;

    @Schema(description = "满减金额（满减券必填）")
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    @Schema(description = "折扣率（折扣券，0.85=85折）")
    @TableField("discount_rate")
    private BigDecimal discountRate;

    @Schema(description = "折扣券最高优惠金额（空=不封顶）")
    @TableField("max_discount_amount")
    private BigDecimal maxDiscountAmount;

    @Schema(description = "发行总量（0=不限量）")
    @TableField("total_count")
    private Integer totalCount;

    @Schema(description = "已领取数量")
    @TableField("received_count")
    private Integer receivedCount;

    @Schema(description = "每人限领")
    @TableField("per_user_limit")
    private Integer perUserLimit;

    @Schema(description = "适用范围：all全部/category分类/product商品")
    private String scope;

    @Schema(description = "范围值：scope=category 时为分类名称（与 Product.category 对齐）；scope=product 时为商品ID字符串")
    @TableField("scope_value")
    private String scopeValue;

    @Schema(description = "可领取开始时间（空=立即）")
    @TableField("receive_start_time")
    private LocalDateTime receiveStartTime;

    @Schema(description = "可领取结束时间")
    @TableField("receive_end_time")
    private LocalDateTime receiveEndTime;

    @Schema(description = "领取后有效天数")
    @TableField("valid_days")
    private Integer validDays;

    @Schema(description = "状态：draft草稿/active已上架/offline已下架")
    private String status;

    @Schema(description = "使用说明")
    private String description;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
