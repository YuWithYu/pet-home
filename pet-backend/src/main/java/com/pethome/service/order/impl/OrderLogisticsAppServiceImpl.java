package com.pethome.service.order.impl;

import com.pethome.common.BusinessException;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.Product;
import com.pethome.service.order.LogisticsService;
import com.pethome.service.order.OrderItemService;
import com.pethome.service.order.OrderLogisticsAppService;
import com.pethome.service.order.OrderService;
import com.pethome.service.order.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单物流应用服务实现。
 * <p>
 * 原 OrderController 的物流查询/发货维护逻辑下沉至此，权限与数据隔离统一在此校验。
 */
@Service
public class OrderLogisticsAppServiceImpl implements OrderLogisticsAppService {

    private static final Logger log = LoggerFactory.getLogger(OrderLogisticsAppServiceImpl.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private LogisticsService logisticsService;

    @Override
    public Map<String, Object> getOrderLogistics(String orderId, boolean adminSide,
                                                 Long currentUserId, Long currentStoreId) {
        Order order = resolveOrder(orderId);

        // 安全修复：物流详情含收货人姓名/电话/详细地址，非管理端仅限订单本人
        if (!adminSide && (currentUserId == null || !currentUserId.equals(order.getUserId()))) {
            throw new BusinessException(403, "无权查看此订单");
        }

        // 数据隔离：店铺管理员只能查看包含本店铺商品的订单
        if (currentStoreId != null && !orderContainsStoreProduct(order.getId(), currentStoreId)) {
            throw new BusinessException(403, "您没有权限查看此订单");
        }

        Map<String, Object> logisticsData = new HashMap<>();
        logisticsData.put("orderId", order.getId());
        logisticsData.put("orderNo", order.getOrderNo());
        logisticsData.put("shippingCompany", order.getShippingCompany() != null ? order.getShippingCompany() : "");
        logisticsData.put("shippingNumber", order.getShippingNumber() != null ? order.getShippingNumber() : "");
        logisticsData.put("shippingTime", order.getShippingTime() != null ? order.getShippingTime().toString() : "");

        Integer orderStatus = order.getStatus() != null ? order.getStatus() : 0;
        logisticsData.put("status", toOrderStatus(orderStatus));
        logisticsData.put("statusNumber", orderStatus);

        logisticsData.put("receiverName", order.getReceiverName() != null ? order.getReceiverName() : "");
        logisticsData.put("receiverPhone", order.getReceiverPhone() != null ? order.getReceiverPhone() : "");
        String fullAddress = buildReceiverAddress(order);
        logisticsData.put("receiverAddress", fullAddress);
        logisticsData.put("warehouseAddress", order.getWarehouseAddress() != null ? order.getWarehouseAddress() : "");

        // 如果有物流单号，查询物流轨迹
        List<Map<String, Object>> tracks = new ArrayList<>();
        if (order.getShippingNumber() != null && !order.getShippingNumber().trim().isEmpty()) {
            LocalDateTime shippingTime = order.getShippingTime() != null
                    ? order.getShippingTime() : LocalDateTime.now();
            try {
                Map<String, Object> logisticsResult = logisticsService.queryLogistics(
                        order.getShippingCompany(),
                        order.getShippingNumber(),
                        fullAddress,
                        order.getWarehouseAddress() != null ? order.getWarehouseAddress() : "",
                        shippingTime);
                if (logisticsResult != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> queryTracks = (List<Map<String, Object>>) logisticsResult.get("tracks");
                    if (queryTracks != null && !queryTracks.isEmpty()) {
                        tracks = queryTracks;
                    }
                    if (logisticsResult.containsKey("estimatedDays")) {
                        logisticsData.put("estimatedDays", logisticsResult.get("estimatedDays"));
                    }
                    if (logisticsResult.containsKey("estimatedDeliveryTip")) {
                        logisticsData.put("estimatedDeliveryTip", logisticsResult.get("estimatedDeliveryTip"));
                    }
                }
            } catch (Exception e) {
                // 物流轨迹查询失败不影响主流程，返回空轨迹
                log.warn("查询物流轨迹失败: {}", e.getMessage());
            }
        }
        logisticsData.put("tracks", tracks);
        return logisticsData;
    }

    @Override
    public boolean updateOrderLogistics(String orderId, Map<String, Object> logisticsData,
                                        boolean adminSide, Long currentStoreId) {
        if (!adminSide) {
            throw new BusinessException(403, "无权操作此订单物流");
        }
        Order order = resolveOrder(orderId);

        if (currentStoreId != null && !orderContainsStoreProduct(order.getId(), currentStoreId)) {
            throw new BusinessException(403, "您没有权限操作此订单");
        }

        if (logisticsData.containsKey("shippingCompany")) {
            order.setShippingCompany(logisticsData.get("shippingCompany").toString());
        }
        if (logisticsData.containsKey("shippingNumber")) {
            order.setShippingNumber(logisticsData.get("shippingNumber").toString());
        }
        if (logisticsData.containsKey("shippingTime")) {
            order.setShippingTime(parseShippingTime(logisticsData.get("shippingTime")));
        } else {
            order.setShippingTime(LocalDateTime.now());
        }

        applyIfPresent(logisticsData, "receiverName", order::setReceiverName);
        applyIfPresent(logisticsData, "receiverPhone", order::setReceiverPhone);
        applyIfPresent(logisticsData, "receiverProvince", order::setReceiverProvince);
        applyIfPresent(logisticsData, "receiverCity", order::setReceiverCity);
        applyIfPresent(logisticsData, "receiverRegion", order::setReceiverRegion);
        applyIfPresent(logisticsData, "receiverDetailAddress", order::setReceiverDetailAddress);
        applyIfPresent(logisticsData, "warehouseAddress", order::setWarehouseAddress);

        // 保存物流字段（此时内存中 status 未变，全字段覆盖无副作用）
        orderService.updateOrder(order);

        // 仅「已付款(1)」订单保存物流后自动转「已发货(2)」，并走正式状态机
        if (order.getStatus() != null && order.getStatus() == 1) {
            orderService.updateOrderStatus(order.getId(), 2);
        }

        log.info("更新订单物流信息成功 - 订单ID: {}, 物流公司: {}, 物流单号: {}, 发货仓: {}, 发货时间: {}",
                order.getId(), order.getShippingCompany(), order.getShippingNumber(),
                order.getWarehouseAddress(), order.getShippingTime());
        return true;
    }

    private LocalDateTime parseShippingTime(Object raw) {
        String timeStr = raw.toString();
        try {
            if (timeStr.contains("T")) {
                // ISO 格式：2026-01-21T02:12:35 或 2026-01-21T02:12:35.000
                if (timeStr.length() > 19) {
                    timeStr = timeStr.substring(0, 19);
                }
                return LocalDateTime.parse(timeStr);
            }
            if (timeStr.contains(" ")) {
                return LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            return LocalDateTime.parse(timeStr);
        } catch (Exception e) {
            // 解析失败时使用当前时间，避免发货流程被脏数据阻断
            log.warn("解析发货时间失败: {}, 错误: {}", raw, e.getMessage());
            return LocalDateTime.now();
        }
    }

    private void applyIfPresent(Map<String, Object> source, String key,
                                java.util.function.Consumer<String> setter) {
        if (source.containsKey(key)) {
            setter.accept(source.get(key).toString());
        }
    }

    private String buildReceiverAddress(Order order) {
        StringBuilder fullAddress = new StringBuilder();
        if (order.getReceiverProvince() != null) fullAddress.append(order.getReceiverProvince());
        if (order.getReceiverCity() != null) fullAddress.append(order.getReceiverCity());
        if (order.getReceiverRegion() != null) fullAddress.append(order.getReceiverRegion());
        if (order.getReceiverDetailAddress() != null) fullAddress.append(order.getReceiverDetailAddress());
        return fullAddress.toString();
    }

    private String toOrderStatus(Integer status) {
        if (status == null) {
            return "pending";
        }
        switch (status) {
            case 1: return "paid";
            case 2: return "shipped";
            case 3: return "completed";
            case -1:
            case 4: // 4 为历史遗留的取消值，兼容
                return "cancelled";
            default: return "pending";
        }
    }

    private Order resolveOrder(String orderId) {
        if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
            Order order = orderService.getOrderByOrderNo(orderId);
            if (order == null) {
                throw new BusinessException(404, "订单不存在");
            }
            return order;
        }
        try {
            Order order = orderService.getOrderById(Long.parseLong(orderId));
            if (order == null) {
                throw new BusinessException(404, "订单不存在");
            }
            return order;
        } catch (NumberFormatException e) {
            Order order = orderService.getOrderByOrderNo(orderId);
            if (order == null) {
                throw new BusinessException(404, "订单不存在");
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
}
