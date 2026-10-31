package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.Product;
import com.pethome.entity.RefundRequest;
import com.pethome.service.order.OrderItemService;
import com.pethome.service.order.OrderRefundAppService;
import com.pethome.service.order.OrderService;
import com.pethome.service.order.ProductService;
import com.pethome.service.order.RefundRequestService;
import com.pethome.service.order.RefundSettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单退款应用服务实现。
 * <p>
 * 事务边界在此，Controller 不再持有任何退款编排逻辑。
 */
@Service
public class OrderRefundAppServiceImpl implements OrderRefundAppService {

    private static final Logger log = LoggerFactory.getLogger(OrderRefundAppServiceImpl.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RefundRequestService refundRequestService;

    @Autowired
    private RefundSettlementService refundSettlementService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> requestRefund(String orderId, Map<String, Object> refundData, Long currentUserId) {
        Order order = resolveOrder(orderId);

        // 权限校验：只有订单所有者才能申请退款
        if (order.getUserId() == null || currentUserId == null || !currentUserId.equals(order.getUserId())) {
            log.warn("退款申请越权尝试: tokenUserId={}, orderUserId={}, orderId={}",
                    currentUserId, order.getUserId(), order.getId());
            throw new BusinessException(403, "无权操作该订单");
        }

        // 检查订单状态，只有已完成的订单才能申请退款
        if (order.getStatus() == null || order.getStatus() != 3) {
            throw new BusinessException("只有已完成的订单才能申请退款");
        }

        // 检查是否已经申请过退款
        RefundRequest existingRefund = refundRequestService.getByOrderId(order.getId());
        if (existingRefund != null && !"rejected".equals(existingRefund.getStatus())
                && !"cancelled".equals(existingRefund.getStatus())) {
            throw new BusinessException("该订单已申请退款，请勿重复申请");
        }

        // 记录退款申请信息
        String type = refundData.get("type") != null ? refundData.get("type").toString() : "我要退货退款";
        String reason = refundData.get("reason") != null ? refundData.get("reason").toString() : "用户申请退款";
        BigDecimal amount = order.getTotalAmount();
        if (refundData.get("amount") != null) {
            try {
                amount = new BigDecimal(refundData.get("amount").toString());
            } catch (NumberFormatException e) {
                throw new BusinessException("退款金额格式错误");
            }
        }

        // 安全校验：退款金额必须为正且不得超过订单实付金额
        BigDecimal orderTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退款金额必须大于 0");
        }
        if (amount.compareTo(orderTotal) > 0) {
            log.warn("退款金额超限: orderNo={}, 退款={}, 订单实付={}", order.getOrderNo(), amount, orderTotal);
            throw new BusinessException("退款金额不能超过订单实付金额");
        }

        log.info("订单退款申请 - 订单号: {}, 类型: {}, 原因: {}, 金额: {}", order.getOrderNo(), type, reason, amount);

        // 保存退款申请记录到数据库
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrderId(order.getId());
        refundRequest.setOrderNo(order.getOrderNo());
        refundRequest.setUserId(order.getUserId());
        refundRequest.setType(type);
        refundRequest.setReason(reason);
        refundRequest.setRefundAmount(amount);
        refundRequest.setStatus("pending");
        refundRequest.setCreateTime(LocalDateTime.now());
        refundRequest.setUpdateTime(LocalDateTime.now());

        refundRequestService.save(refundRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("id", refundRequest.getId());
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("refundAmount", amount);
        result.put("type", type);
        result.put("reason", reason);
        result.put("status", "pending");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRefund(String orderId, Long currentUserId) {
        Order order = resolveOrder(orderId);

        if (currentUserId != null && order.getUserId() != null && !currentUserId.equals(order.getUserId())) {
            throw new BusinessException(403, "无权操作该订单退款申请");
        }

        RefundRequest refundRequest = refundRequestService.getByOrderId(order.getId());
        if (refundRequest == null) {
            throw new BusinessException("未找到退款申请");
        }
        if (!"pending".equals(refundRequest.getStatus())) {
            throw new BusinessException("当前退款申请状态不可取消");
        }

        refundRequest.setStatus("cancelled");
        refundRequest.setUpdateTime(LocalDateTime.now());
        refundRequestService.updateById(refundRequest);
        return true;
    }

    @Override
    public Map<String, Object> getRefundList(int page, int size, String status, Long currentStoreId) {
        int p = page > 0 ? page : 1;
        int sz = size > 0 ? size : 10;
        Page<RefundRequest> mpPage = refundRequestService.pageRefunds(p, sz, status, currentStoreId);

        List<RefundRequest> refundRequests = mpPage.getRecords();
        List<Map<String, Object>> refundList = new ArrayList<>();

        for (RefundRequest refundRequest : refundRequests) {
            try {
                // 获取对应的订单信息
                Order order = orderService.getOrderById(refundRequest.getOrderId());
                if (order == null) {
                    log.debug("退款列表 - 跳过退款申请: {} (订单不存在)", refundRequest.getOrderNo());
                    continue;
                }

                Map<String, Object> refund = new HashMap<>();
                refund.put("id", refundRequest.getId());
                refund.put("orderId", refundRequest.getOrderId());
                refund.put("orderNo", refundRequest.getOrderNo());
                refund.put("userId", refundRequest.getUserId());

                // 获取订单商品信息（如果是店铺管理员，只显示当前店铺的商品）
                List<OrderItem> allOrderItems = orderItemService.getOrderItemsByOrderId(order.getId());
                List<OrderItem> orderItems = allOrderItems;
                BigDecimal storeTotalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                BigDecimal storeRefundAmount = refundRequest.getRefundAmount() != null
                        ? refundRequest.getRefundAmount() : BigDecimal.ZERO;

                if (currentStoreId != null) {
                    orderItems = new ArrayList<>();
                    BigDecimal storeTotal = BigDecimal.ZERO;
                    for (OrderItem item : allOrderItems) {
                        Product product = productService.getProductById(item.getProductId());
                        if (product != null && currentStoreId.equals(product.getStoreId())) {
                            orderItems.add(item);
                            // 计算当前店铺商品的金额
                            BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                            BigDecimal itemQuantity = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 1);
                            storeTotal = storeTotal.add(itemPrice.multiply(itemQuantity));
                        }
                    }
                    // 如果是店铺管理员，订单金额和退款金额应该是当前店铺商品的金额
                    if (!orderItems.isEmpty()) {
                        storeTotalAmount = storeTotal;
                        storeRefundAmount = storeTotal;
                    }
                }

                refund.put("totalAmount", storeTotalAmount != null ? storeTotalAmount : BigDecimal.ZERO);
                refund.put("refundAmount", storeRefundAmount != null ? storeRefundAmount : BigDecimal.ZERO);
                refund.put("type", refundRequest.getType());
                refund.put("reason", refundRequest.getReason());
                refund.put("status", refundRequest.getStatus());
                refund.put("createTime", refundRequest.getCreateTime() != null ? refundRequest.getCreateTime().toString() : "");
                refund.put("updateTime", refundRequest.getUpdateTime() != null ? refundRequest.getUpdateTime().toString() : "");

                // 获取订单商品信息（只显示当前店铺的商品）
                List<Map<String, Object>> products = new ArrayList<>();
                if (orderItems != null) {
                    for (OrderItem item : orderItems) {
                        if (item != null) {
                            Map<String, Object> product = new HashMap<>();
                            product.put("id", item.getProductId());
                            product.put("name", item.getProductName() != null ? item.getProductName() : "");
                            product.put("image", item.getProductImage() != null ? item.getProductImage() : "");
                            product.put("price", item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                            product.put("quantity", item.getQuantity() != null ? item.getQuantity() : 0);
                            products.add(product);
                        }
                    }
                }
                refund.put("products", products);

                // 添加收货人信息
                refund.put("receiverName", order.getReceiverName() != null ? order.getReceiverName() : "");
                refund.put("receiverPhone", order.getReceiverPhone() != null ? order.getReceiverPhone() : "");
                refund.put("receiverAddress",
                        (order.getReceiverProvince() != null ? order.getReceiverProvince() : "")
                                + (order.getReceiverCity() != null ? order.getReceiverCity() : "")
                                + (order.getReceiverRegion() != null ? order.getReceiverRegion() : "")
                                + (order.getReceiverDetailAddress() != null ? order.getReceiverDetailAddress() : ""));

                refundList.add(refund);
            } catch (Exception e) {
                log.warn("退款列表 - 处理退款申请 {} 时出错: {}",
                        refundRequest != null ? refundRequest.getOrderNo() : "未知", e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", refundList);
        result.put("total", mpPage.getTotal());
        result.put("page", p);
        result.put("size", sz);

        log.debug("退款列表 - 本页 {} 条, 总 {}", refundList.size(), mpPage.getTotal());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> approveRefund(String orderId, Long currentStoreId, String adminRole) {
        if ("staff".equals(adminRole)) {
            throw new BusinessException(403, "当前角色无权处理退款审批");
        }

        Order order = resolveOrder(orderId);

        // 数据隔离：店铺管理员只能操作包含本店铺商品的订单
        if (currentStoreId != null && !orderContainsStoreProduct(order, currentStoreId)) {
            throw new BusinessException(403, "您没有权限操作此订单");
        }

        RefundRequest refundRequest = refundRequestService.getByOrderId(order.getId());
        if (refundRequest == null) {
            throw new BusinessException("退款申请记录不存在");
        }
        if ("approved".equals(refundRequest.getStatus())) {
            throw new BusinessException("该退款申请已批准，请勿重复处理");
        }
        if (!"pending".equals(refundRequest.getStatus())) {
            throw new BusinessException("该退款申请已处理");
        }

        // 状态流转 + 积分返还 + 库存恢复 + 返券，全部在服务层同一事务内完成
        boolean isPointsOrder = order.getOrderNo() != null && order.getOrderNo().startsWith("EX");
        Integer pointsRefunded;
        try {
            pointsRefunded = refundSettlementService.approveAndSettle(order, refundRequest);
        } catch (IllegalArgumentException e) {
            // 结算业务校验失败（如积分订单无可返还积分），事务已在服务层回滚
            throw new BusinessException(e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("status", "approved");
        if (isPointsOrder) {
            result.put("pointsRefunded", pointsRefunded);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> rejectRefund(String orderId, Map<String, Object> body,
                                            Long currentStoreId, String adminRole) {
        if ("staff".equals(adminRole)) {
            throw new BusinessException(403, "当前角色无权处理退款审批");
        }

        Order order = resolveOrder(orderId);

        if (currentStoreId != null && !orderContainsStoreProduct(order, currentStoreId)) {
            throw new BusinessException(403, "您没有权限操作此订单");
        }

        RefundRequest refundRequest = refundRequestService.getByOrderId(order.getId());
        if (refundRequest == null) {
            throw new BusinessException("退款申请记录不存在");
        }

        String st = refundRequest.getStatus();
        if (st != null && !"pending".equals(st)) {
            throw new BusinessException("该退款申请已处理");
        }

        String rejectReason = "";
        if (body != null && body.get("rejectReason") != null) {
            rejectReason = body.get("rejectReason").toString().trim();
        }
        if (rejectReason.isEmpty()) {
            throw new BusinessException("拒绝退款原因不能为空");
        }

        refundRequest.setStatus("rejected");
        // 复用 reason 字段持久化“拒绝原因”，避免用户端看不到处理原因
        String userApplyReason = refundRequest.getReason() == null ? "" : refundRequest.getReason().trim();
        String mergedReason = userApplyReason.isEmpty()
                ? ("拒绝原因：" + rejectReason)
                : ("申请原因：" + userApplyReason + "；拒绝原因：" + rejectReason);
        refundRequest.setReason(mergedReason);
        refundRequest.setUpdateTime(LocalDateTime.now());
        refundRequestService.updateById(refundRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("status", "rejected");
        result.put("rejectReason", rejectReason);
        return result;
    }

    /**
     * 兼容订单号/数字 ID 两种入参。
     */
    private Order resolveOrder(String orderId) {
        Order order;
        if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
            order = orderService.getOrderByOrderNo(orderId);
        } else {
            try {
                order = orderService.getOrderById(Long.parseLong(orderId));
            } catch (NumberFormatException e) {
                order = orderService.getOrderByOrderNo(orderId);
            }
        }
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private boolean orderContainsStoreProduct(Order order, Long currentStoreId) {
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order.getId());
        for (OrderItem item : orderItems) {
            Product product = productService.getProductById(item.getProductId());
            if (product != null && currentStoreId.equals(product.getStoreId())) {
                return true;
            }
        }
        return false;
    }
}
