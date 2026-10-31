package com.pethome.service.order;

import com.pethome.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户持券 Service（领取/卡包/可用券计算/核销/返还/过期）
 */
public interface UserCouponService {

    /** 领取优惠券（乐观扣减防超发 + 每人限领校验） */
    UserCoupon receive(Long userId, Long couponId);

    /**
     * 我的卡包
     * @param status unused/used/expired，空=全部（expired 为懒计算+定时任务双保障）
     */
    List<Map<String, Object>> myCoupons(Long userId, String status);

    /**
     * 计算可用券：给定订单商品集与总额，返回每张券的可用性与优惠金额
     * @param productIds 订单商品ID集合（用于 category/product 范围匹配）
     * @param totalAmount 订单总额（后端计算，不信任前端）
     */
    List<Map<String, Object>> listUsable(Long userId, List<Long> productIds, BigDecimal totalAmount);

    /**
     * 核销校验：校验券可用性并计算优惠金额（不改库，供下单事务内先算后核销）
     * @throws com.pethome.common.BusinessException 券不可用/门槛不满足/范围不符
     */
    BigDecimal validateAndCalcDiscount(Long userCouponId, Long userId,
                                       List<Long> productIds, BigDecimal totalAmount);

    /**
     * 核销：条件更新置 used（防重复核销，须在同一事务内调用）
     * @throws com.pethome.common.BusinessException 核销失败（已被使用/过期/越权）
     */
    void markUsed(Long userCouponId, Long userId, Long orderId);

    /** 订单取消返还：按订单维度恢复 unused */
    void returnByOrder(Long orderId, Long userId);

    /** 定时任务：批量将过期的 unused 券置为 expired */
    int expireOverdue();
}
