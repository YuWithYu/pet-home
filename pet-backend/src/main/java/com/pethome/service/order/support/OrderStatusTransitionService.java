package com.pethome.service.order.support;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.common.BusinessException;
import com.pethome.entity.Notification;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.mapper.OrderMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.service.community.NotificationService;
import com.pethome.service.order.OrderItemService;
import com.pethome.service.order.UserCouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单状态流转支撑：状态机校验、条件更新、取消副作用（还库存/返券）与状态通知。
 * 本类不声明事务，全部操作依附调用方（OrderServiceImpl）的事务边界。
 */
@Service
public class OrderStatusTransitionService {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusTransitionService.class);

    /** 订单状态常量 */
    public static final int STATUS_PENDING_PAY = 0;   // 待付款
    public static final int STATUS_PAID = 1;          // 已付款（待发货）
    public static final int STATUS_SHIPPED = 2;       // 已发货（待收货）
    public static final int STATUS_COMPLETED = 3;     // 已完成
    public static final int STATUS_CANCELLED = -1;    // 已取消

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired(required = false)
    private NotificationService notificationService;
    @Autowired(required = false)
    private OrderItemService orderItemService;
    @Autowired(required = false)
    private UserCouponService userCouponService;

    /**
     * 订单状态机校验
     * 合法跳转：
     *   待付款(0)    → 已付款(1)   【支付回调】
     *   待付款(0)    → 已取消(-1)  【用户取消/超时取消】
     *   已付款(1)    → 已发货(2)   【后台发货】
     *   已付款(1)    → 已取消(-1)  【全额退款】
     *   已发货(2)    → 已完成(3)   【用户确认收货/自动完成】
     * 其他跳转一律拒绝
     */
    public void validateStatusTransition(Integer from, Integer to) {
        if (from == null || to == null) {
            throw new BusinessException("订单状态异常");
        }
        boolean allowed = false;
        if (from == STATUS_PENDING_PAY) {
            allowed = (to == STATUS_PAID || to == STATUS_CANCELLED);
        } else if (from == STATUS_PAID) {
            allowed = (to == STATUS_SHIPPED || to == STATUS_CANCELLED);
        } else if (from == STATUS_SHIPPED) {
            allowed = (to == STATUS_COMPLETED);
        }
        // 已完成(3)、已取消(-1) 为终态，不允许任何跳转
        if (!allowed) {
            log.warn("非法状态跳转被拒绝: from={}, to={}", from, to);
            throw new BusinessException(400, "订单状态变更非法: " + from + " -> " + to);
        }
    }

    /**
     * 条件更新（WHERE status=旧状态）：并发竞争下仅一个调用胜出。
     * 返回受影响行数，0 表示已被其他调用变更。
     */
    public int applyConditionalStatusUpdate(Long id, Integer oldStatus, Integer status) {
        UpdateWrapper<Order> conditionUpdate = new UpdateWrapper<>();
        conditionUpdate.eq("id", id).eq("status", oldStatus)
                .set("status", status)
                .set("update_time", LocalDateTime.now());
        if (status == STATUS_SHIPPED) {
            conditionUpdate.set("delivery_status", 1).set("shipping_time", LocalDateTime.now());
        }
        if (status == STATUS_COMPLETED) {
            conditionUpdate.set("delivery_status", 2);
        }
        return orderMapper.update(null, conditionUpdate);
    }

    /**
     * 取消订单的副作用：恢复库存 + 返还已核销优惠券。
     * 仅由状态竞争胜出方调用（条件更新 affected==1），保证恰好执行一次。
     */
    public void restoreStockAndReturnCoupon(Long id, Order order) {
        if (orderItemService != null) {
            List<OrderItem> items = orderItemService.getOrderItemsByOrderId(id);
            for (OrderItem item : items) {
                if (item.getProductId() != null && item.getQuantity() != null) {
                    productMapper.incrementStock(item.getProductId(), item.getQuantity());
                    log.info("订单取消恢复库存: orderId={}, productId={}, quantity={}",
                            id, item.getProductId(), item.getQuantity());
                }
            }
        }
        if (order.getCouponId() != null && userCouponService != null) {
            userCouponService.returnByOrder(id, order.getUserId());
        }
    }

    /**
     * 状态变更通知：发货 / 完成 / 取消
     */
    public void notifyStatusChange(Long id, Order order, Integer oldStatus, Integer status) {
        if (notificationService != null && !status.equals(oldStatus) && order.getUserId() != null) {
            String title = null, content = null;
            String orderNo = order.getOrderNo() != null ? order.getOrderNo() : String.valueOf(id);
            if (status == STATUS_SHIPPED) {
                title = "订单已发货";
                content = "您的订单 " + orderNo + " 已发货，请留意物流信息。";
            } else if (status == STATUS_COMPLETED) {
                title = "订单已完成";
                content = "您的订单 " + orderNo + " 已完成，感谢您的购买。";
            } else if (status == STATUS_CANCELLED) {
                title = "订单已取消";
                content = "您的订单 " + orderNo + " 已取消。";
            }
            if (title != null) {
                Notification n = new Notification();
                n.setUserId(order.getUserId());
                n.setTitle(title);
                n.setContent(content);
                n.setType("order_status");
                n.setStatus(0);
                n.setRelatedId(id);
                n.setRelatedType("order");
                n.setCreateTime(LocalDateTime.now());
                n.setUpdateTime(LocalDateTime.now());
                notificationService.createNotification(n);
            }
        }
    }
}
