package com.pethome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;

import java.util.List;

public interface OrderService {
    IPage<Order> getOrderList(Page<Order> page);

    /**
     * 管理端订单列表
     * @param orderCategory null 或 all：全部；mall：普通商城（排除积分兑换单 EX 开头）；points：仅积分兑换单
     */
    IPage<Order> getOrderList(Page<Order> page, String orderCategory);

    /**
     * 管理端订单列表（SQL 级状态过滤）
     * @param statusCode 订单状态数值（0/1/2/3/-1），null 表示不过滤
     */
    IPage<Order> getOrderList(Page<Order> page, String orderCategory, Integer statusCode);

    /** C 端列表：按买家过滤并排除已删除，避免扫全表 */
    IPage<Order> getOrderListForUser(Page<Order> page, Long userId);

    /**
     * C 端列表（SQL 级状态过滤，避免取回整页后再丢弃导致页内条数不足）
     * @param statusCode 订单状态数值（0/1/2/3/-1），null 表示不过滤
     */
    IPage<Order> getOrderListForUser(Page<Order> page, Long userId, Integer statusCode);
    Order createOrder(Order order);

    /**
     * 创建订单（带库存扣减、金额校验、事务保证）。
     *
     * 安全修复要点：
     * 1. userId 强制取自当前登录用户，不信任前端传入；
     * 2. totalAmount 由后端根据商品实际价格重新计算，不信任前端传入；
     * 3. 扣库存走乐观锁（stock >= quantity 才扣减），扣减失败抛异常回滚整个事务；
     * 4. 整个方法在一个事务内：订单主表 + 订单项 + 库存扣减 要么全成功要么全失败。
     *
     * @param userId 当前登录用户 ID（由 Controller 从 token 解析）
     * @param items  订单项（前端只传 productId/quantity/specName，价格由后端查库填充）
     * @param addressId 收货地址 ID
     * @return 创建成功的订单
     */
    Order createOrderWithStockCheck(Long userId, List<OrderItem> items, Long addressId);

    /** 重载：带优惠券下单（userCouponId 可空；券在订单创建事务内核销，取消时返还） */
    Order createOrderWithStockCheck(Long userId, List<OrderItem> items, Long addressId, Long userCouponId);

    Order updateOrderStatus(Long id, Integer status);

    /**
     * 超时/延时取消专用：仅待付款(0)→已取消(-1)，已支付订单返回 false（不触发退款语义）。
     * 内部完成库存恢复、优惠券返还与取消通知（条件更新保证并发下副作用恰好执行一次）。
     */
    boolean cancelPendingOrder(Long id);

    Order updateOrder(Order order);
    Order getOrderById(Long id);
    Order getOrderByOrderNo(String orderNo);
}
