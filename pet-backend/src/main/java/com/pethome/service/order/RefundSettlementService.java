package com.pethome.service.order;

import com.pethome.entity.Order;
import com.pethome.entity.RefundRequest;

/**
 * 退款结算服务：批准退款后的一次性副作用（状态流转、积分返还、库存恢复、优惠券返还）。
 * <p>
 * 这些副作用必须原子完成，事务边界在此，不允许留在 Controller。
 */
public interface RefundSettlementService {

    /**
     * 批准退款并执行结算。
     *
     * @return 实际返还给用户的积分数（非积分订单返回 0）
     * @throws IllegalArgumentException 业务校验失败（积分订单缺少可返还积分等）
     */
    Integer approveAndSettle(Order order, RefundRequest refundRequest);
}
