package com.pethome.service;

import java.util.List;

/**
 * 订单维护（批量兜底操作）。
 *
 * 与下单/支付等交易链路分离：这里只有基于时间窗口的批量状态推进，
 * 单笔取消仍需走 {@code OrderService#cancelPendingOrder} 以保证库存、优惠券、通知的一致性。
 */
public interface OrderMaintenanceService {

    /**
     * 发货满 7 天仍未确认收货的订单批量置为已完成。
     *
     * @return 受影响条数
     */
    int autoCompleteShippedOrders();

    /**
     * 查询超时未支付订单 ID（仅取 ID，不做状态变更）。
     *
     * @param autoCancelMinutes 超时阈值（分钟）
     * @param limit             单批上限
     */
    List<Long> findExpiredPendingOrderIds(int autoCancelMinutes, int limit);
}
