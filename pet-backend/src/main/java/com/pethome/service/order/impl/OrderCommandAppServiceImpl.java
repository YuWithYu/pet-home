package com.pethome.service.order.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.Product;
import com.pethome.service.appointment.AppointmentDelayService;
import com.pethome.service.order.OrderCommandAppService;
import com.pethome.service.order.OrderItemService;
import com.pethome.service.order.OrderService;
import com.pethome.service.order.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单命令应用服务实现。
 * <p>
 * 下单与状态流转的编排逻辑原位于 OrderController，现下沉至此，表现层只保留身份解析。
 */
@Service
public class OrderCommandAppServiceImpl implements OrderCommandAppService {

    private static final Logger log = LoggerFactory.getLogger(OrderCommandAppServiceImpl.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired(required = false)
    private AppointmentDelayService appointmentDelayService;

    @Override
    public Map<String, Object> createOrder(Long currentUserId, Map<String, Object> orderData) {
        if (currentUserId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        if (orderData == null || !(orderData.get("items") instanceof List)
                || ((List<?>) orderData.get("items")).isEmpty()) {
            throw new BusinessException("订单商品项不能为空");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) orderData.get("items");
        List<OrderItem> items = new ArrayList<>();
        for (Map<String, Object> raw : rawItems) {
            OrderItem item = new OrderItem();
            if (raw.get("productId") != null) {
                item.setProductId(Long.parseLong(raw.get("productId").toString()));
            }
            if (raw.get("quantity") != null) {
                item.setQuantity(Integer.parseInt(raw.get("quantity").toString()));
            }
            if (raw.get("specName") != null) {
                item.setSpecName(raw.get("specName").toString());
            }
            if (raw.get("specPrice") != null) {
                // 注意：specPrice 也会在 Service 层重新校验，这里只是透传
                item.setSpecPrice(new BigDecimal(raw.get("specPrice").toString()));
            }
            items.add(item);
        }

        Long addressId = parseOptionalLong(orderData.get("addressId"), "地址ID格式错误");
        Long userCouponId = null;
        Object rawCoupon = orderData.get("userCouponId");
        if (rawCoupon != null && !rawCoupon.toString().isBlank()) {
            userCouponId = parseOptionalLong(rawCoupon, "优惠券ID格式错误");
        }

        // 安全创建：校验地址归属、重新计算金额、扣库存、核销优惠券、写订单+订单项（同一事务内）
        Order createdOrder = orderService.createOrderWithStockCheck(currentUserId, items, addressId, userCouponId);

        // 注册超时取消（30 分钟未支付自动取消）
        if (appointmentDelayService != null) {
            appointmentDelayService.registerOrderCancelKey(createdOrder.getId(), 30);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", createdOrder.getId());
        result.put("orderNo", createdOrder.getOrderNo());
        result.put("totalAmount", createdOrder.getTotalAmount());
        result.put("status", "pending");
        return result;
    }

    @Override
    public boolean updateOrderStatus(String orderId, String status, boolean platformAdmin,
                                     Long currentStoreId, Long userIdFromToken) {
        Order order = resolveOrder(orderId);
        Long id = order.getId();

        // 数据隔离：店铺管理员只能操作包含本店铺商品的订单
        if (currentStoreId != null) {
            if (!orderContainsStoreProduct(id, currentStoreId)) {
                throw new BusinessException(403, "您没有权限操作此订单");
            }
        } else if (!platformAdmin) {
            // 安全修复 #50：普通用户必须校验订单归属，且只允许取消待付款订单
            if (userIdFromToken == null) {
                throw new BusinessException(401, "未登录或 token 无效");
            }
            if (order.getUserId() == null || !userIdFromToken.equals(order.getUserId().longValue())) {
                log.warn("updateOrderStatus 越权尝试：token userId={} 但订单归属 userId={}, orderId={}",
                        userIdFromToken, order.getUserId(), id);
                throw new BusinessException(403, "无权操作此订单");
            }
            if (!"cancelled".equalsIgnoreCase(status)) {
                log.warn("updateOrderStatus 普通用户越权尝试：userId={}, orderId={}, status={}",
                        userIdFromToken, id, status);
                throw new BusinessException(403, "无权执行此状态变更");
            }
        }

        int statusCode = toStatusCode(status);
        Integer oldStatus = order.getStatus();
        if (oldStatus != null && !isValidStatusTransition(oldStatus, statusCode)) {
            throw new BusinessException("非法的状态转换：不允许从 " + getStatusName(oldStatus) + " 转换为 " + status);
        }

        log.info("订单状态更新：orderId={}, 旧状态={}, 新状态={}", id, oldStatus, statusCode);
        Order updatedOrder = orderService.updateOrderStatus(id, statusCode);
        if (statusCode != 0 && appointmentDelayService != null) {
            appointmentDelayService.cancelOrderCancelKey(id);
        }
        // 库存统一由 OrderServiceImpl.updateOrderStatus 与下单扣减闭环管理
        return updatedOrder != null;
    }

    private Long parseOptionalLong(Object raw, String formatError) {
        if (raw == null) {
            return null;
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException(400, formatError);
        }
    }

    private Order resolveOrder(String orderId) {
        if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
            Order order = orderService.getOrderByOrderNo(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            return order;
        }
        try {
            Order order = orderService.getOrderById(Long.parseLong(orderId));
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            return order;
        } catch (NumberFormatException e) {
            Order order = orderService.getOrderByOrderNo(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            return order;
        }
    }

    private boolean orderContainsStoreProduct(Long orderId, Long storeId) {
        for (OrderItem item : orderItemService.getOrderItemsByOrderId(orderId)) {
            Product product = productService.getProductById(item.getProductId());
            if (product != null && storeId.equals(product.getStoreId())) {
                return true;
            }
        }
        return false;
    }

    private int toStatusCode(String status) {
        if (status == null) {
            throw new BusinessException("无效的订单状态");
        }
        switch (status) {
            case "pending": return 0;
            case "paid": return 1;
            case "shipped": return 2;
            case "completed": return 3;
            case "cancelled": return -1;
            case "deleted": return -2;
            default: throw new BusinessException("无效的订单状态");
        }
    }

    /**
     * 校验订单状态转换是否合法。
     * pending→paid/cancelled/deleted；paid→shipped/cancelled/deleted；shipped→completed/deleted；
     * completed/cancelled→deleted。
     */
    private boolean isValidStatusTransition(Integer fromStatus, Integer toStatus) {
        if (fromStatus == null || toStatus == null) {
            return true;
        }
        switch (fromStatus) {
            case 0: return toStatus == 1 || toStatus == -1 || toStatus == -2;
            case 1: return toStatus == 2 || toStatus == -1 || toStatus == -2;
            case 2: return toStatus == 3 || toStatus == -2;
            case 3: return toStatus == -2;
            case -1: return toStatus == -2;
            default: return false;
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case -1: return "已取消";
            case -2: return "已删除";
            default: return "未知状态";
        }
    }
}
