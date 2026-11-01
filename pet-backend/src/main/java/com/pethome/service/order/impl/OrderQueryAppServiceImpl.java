package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.Product;
import com.pethome.entity.RefundRequest;
import com.pethome.service.order.OrderItemService;
import com.pethome.service.order.OrderQueryAppService;
import com.pethome.service.order.OrderService;
import com.pethome.service.order.ProductService;
import com.pethome.service.order.RefundRequestService;
import com.pethome.service.order.support.LitterAppointmentOrderQueryService;
import com.pethome.service.order.support.OrderViewAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 订单查询应用服务实现。
 * <p>
 * 原 OrderController 中的列表/详情组装逻辑整体下沉至此，Controller 只做身份解析与结果包装。
 * 视图字段映射由 {@link OrderViewAssembler} 负责，上门预约订单投影由
 * {@link LitterAppointmentOrderQueryService} 负责。
 */
@Service
public class OrderQueryAppServiceImpl implements OrderQueryAppService {

    private static final Logger log = LoggerFactory.getLogger(OrderQueryAppServiceImpl.class);

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 1000;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired
    private RefundRequestService refundRequestService;

    @Autowired
    private OrderViewAssembler orderViewAssembler;

    @Autowired
    private LitterAppointmentOrderQueryService litterAppointmentOrderQueryService;

    @Override
    public Map<String, Object> getLitterAppointments(String status, boolean adminSide, Long currentUserId) {
        return litterAppointmentOrderQueryService.getLitterAppointments(status, adminSide, currentUserId);
    }

    @Override
    public Map<String, Object> getOrderList(String status, String userIdParam, String orderCategory,
                                            Integer currentPage, Integer pageSize, boolean platformAdmin,
                                            Long userIdFromToken, Long currentStoreId) {
        Long userIdParsed = orderViewAssembler.parseUserId(userIdParam);

        Long userId;
        if (currentStoreId != null) {
            // 店铺端：仅当显式传 userId 时按买家过滤；不能用 token 里的管理员 id 过滤订单买家
            userId = userIdParsed;
        } else if (platformAdmin) {
            // 平台管理员：可按请求参数过滤，或全表（userId=null）
            userId = userIdParsed;
        } else {
            // 安全修复 #50：C 端普通用户必须以 token 为准，不再 fallback 到前端传入的 userId
            if (userIdFromToken == null) {
                return orderViewAssembler.emptyOrderList();
            }
            if (userIdParsed != null && !userIdParsed.equals(userIdFromToken)) {
                log.warn("getOrderList 越权尝试：token userId={} 但请求 userId={}", userIdFromToken, userIdParsed);
                return orderViewAssembler.emptyOrderList();
            }
            userId = userIdFromToken;
        }

        // 无 userId 且非店铺上下文：仅平台管理员可查全表，否则返回空列表（避免扫全表）
        if (userId == null && currentStoreId == null && !platformAdmin) {
            return orderViewAssembler.emptyOrderList();
        }

        // 分页：管理端传 currentPage/pageSize 时启用真分页（单页上限 1000）；
        // 未传时保持旧行为，兼容小程序 C 端与积分商城等旧调用
        boolean paginated = currentPage != null || pageSize != null;
        int pageNo = 1;
        int listPageSize;
        if (paginated) {
            pageNo = Math.max(1, currentPage == null ? 1 : currentPage);
            int requested = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
            listPageSize = Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
        } else {
            listPageSize = (userId != null && currentStoreId == null) ? 100 : MAX_PAGE_SIZE;
        }
        // 店铺端需按店铺商品内存过滤，SQL 分页会导致页内条数不定、总数失真；
        // 店铺端分页时改为先拉满窗口，过滤后再内存切页
        int sqlPageNo = (paginated && currentStoreId != null) ? 1 : pageNo;
        int sqlPageSize = (paginated && currentStoreId != null) ? MAX_PAGE_SIZE : listPageSize;

        Integer statusCodeFilter = orderViewAssembler.toStatusCodeFilter(status);
        Page<Order> page = new Page<>(sqlPageNo, sqlPageSize);
        IPage<Order> orderPage;
        if (userId != null && currentStoreId == null) {
            orderPage = orderService.getOrderListForUser(page, userId, statusCodeFilter);
        } else {
            String cat = (orderCategory != null && !orderCategory.isEmpty()) ? orderCategory.trim() : null;
            if ("all".equalsIgnoreCase(cat)) {
                cat = null;
            }
            orderPage = orderService.getOrderList(page, cat, statusCodeFilter);
        }
        List<Order> allOrders = orderPage.getRecords();
        List<Map<String, Object>> orders = new ArrayList<>();
        if (allOrders.isEmpty()) {
            return orderViewAssembler.emptyOrderList();
        }

        List<Long> orderIds = new ArrayList<>(allOrders.size());
        for (Order o : allOrders) {
            orderIds.add(o.getId());
        }
        Map<Long, List<OrderItem>> itemsByOrderId = orderItemService.mapOrderItemsByOrderIds(orderIds);

        Map<Long, RefundRequest> refundByOrderId = new LinkedHashMap<>();
        // 订单列表统一返回退款状态，C 端据此展示“退款成功/申请中/失败”
        List<RefundRequest> refundRows = refundRequestService.listByOrderIds(orderIds);
        if (refundRows != null) {
            for (RefundRequest r : refundRows) {
                if (r.getOrderId() != null) {
                    refundByOrderId.putIfAbsent(r.getOrderId(), r);
                }
            }
        }

        // C 端列表不做店铺维度过滤，无需批量查 product（省一次 IO，降低超时概率）
        Map<Long, Product> productById = new HashMap<>();
        if (currentStoreId != null) {
            Set<Long> productIdSet = new HashSet<>();
            for (List<OrderItem> itemList : itemsByOrderId.values()) {
                for (OrderItem item : itemList) {
                    if (item.getProductId() != null) {
                        productIdSet.add(item.getProductId());
                    }
                }
            }
            if (!productIdSet.isEmpty()) {
                for (Product p : productService.listProductsByIds(productIdSet)) {
                    if (p != null && p.getId() != null) {
                        productById.put(p.getId(), p);
                    }
                }
            }
        }

        for (Order order : allOrders) {
            if (userId != null && !userId.equals(order.getUserId())) {
                continue;
            }
            if (order.getStatus() != null && order.getStatus() == -2) {
                continue;
            }

            List<OrderItem> orderItems = new ArrayList<>(
                    itemsByOrderId.getOrDefault(order.getId(), new ArrayList<>()));
            if (currentStoreId != null) {
                List<OrderItem> storeOrderItems = new ArrayList<>();
                boolean hasStoreProduct = false;
                for (OrderItem item : orderItems) {
                    if (item.getProductId() == null) {
                        continue;
                    }
                    Product product = productById.get(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        hasStoreProduct = true;
                        storeOrderItems.add(item);
                    }
                }
                if (!hasStoreProduct || storeOrderItems.isEmpty()) {
                    continue;
                }
                orderItems = storeOrderItems;
            }

            RefundRequest refundRequest = refundByOrderId.get(order.getId());
            boolean hasRefundRequest = refundRequest != null && !"cancelled".equals(refundRequest.getStatus());
            String refundStatus = refundRequest != null ? refundRequest.getStatus() : null;

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("id", order.getId());
            orderData.put("orderNo", order.getOrderNo());
            String receiverName = order.getReceiverName();
            String receiverPhone = order.getReceiverPhone();
            if (receiverName != null && !receiverName.isEmpty()) {
                orderData.put("customer", receiverName);
            } else {
                orderData.put("customer", "用户" + order.getUserId());
            }
            orderData.put("phone", receiverPhone != null ? receiverPhone : "");
            StringBuilder fullAddr = new StringBuilder();
            if (order.getReceiverProvince() != null) fullAddr.append(order.getReceiverProvince());
            if (order.getReceiverCity() != null) fullAddr.append(order.getReceiverCity());
            if (order.getReceiverRegion() != null) fullAddr.append(order.getReceiverRegion());
            if (order.getReceiverDetailAddress() != null) fullAddr.append(order.getReceiverDetailAddress());
            orderData.put("address", fullAddr.toString());

            // 店铺管理员使用当前店铺商品的金额；否则使用订单总金额
            if (currentStoreId != null && !orderItems.isEmpty()) {
                BigDecimal storeTotal = BigDecimal.ZERO;
                for (OrderItem item : orderItems) {
                    BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    BigDecimal itemQuantity = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 1);
                    storeTotal = storeTotal.add(itemPrice.multiply(itemQuantity));
                }
                orderData.put("totalAmount", storeTotal);
            } else {
                orderData.put("totalAmount", order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
            }

            orderData.put("hasRefundRequest", hasRefundRequest);
            orderData.put("refundStatus", refundStatus);

            String orderStatus = orderViewAssembler.toOrderStatus(order.getStatus());
            // 如果指定了状态筛选，只返回匹配状态的订单（SQL 已过滤，此处为防御性兜底）
            if (status != null && !status.isEmpty() && !status.equals("all") && !status.equals(orderStatus)) {
                continue;
            }
            orderData.put("status", orderStatus);
            orderData.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : "");

            List<Map<String, Object>> products = new ArrayList<>();
            if (!orderItems.isEmpty()) {
                for (OrderItem item : orderItems) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", item.getProductId());
                    product.put("name", item.getProductName());
                    product.put("image", item.getProductImage());
                    product.put("price", item.getPrice());
                    product.put("quantity", item.getQuantity());
                    product.put("specName", item.getSpecName());
                    product.put("specPrice", item.getSpecPrice());
                    products.add(product);
                }
            } else {
                Map<String, Object> product = new HashMap<>();
                product.put("id", order.getId());
                product.put("name", "商品订单 #" + order.getOrderNo());
                product.put("image", "");
                product.put("price", order.getTotalAmount());
                product.put("quantity", 1);
                products.add(product);
            }
            orderData.put("products", products);

            orderViewAssembler.enrichOrderContactForDisplay(order, orderData);
            orders.add(orderData);
        }

        Map<String, Object> data = new HashMap<>();
        if (paginated && currentStoreId != null) {
            // 店铺端分页：orders 为店铺商品过滤后的完整窗口，此处内存切页
            data.put("total", orders.size());
            int from = Math.min((pageNo - 1) * listPageSize, orders.size());
            int to = Math.min(from + listPageSize, orders.size());
            orders = new ArrayList<>(orders.subList(from, to));
        } else {
            // 分页模式返回数据库真实总数；旧调用（未传分页参数）保持返回当前条数
            data.put("total", paginated ? orderPage.getTotal() : orders.size());
        }
        data.put("orders", orders);
        data.put("unpaidCount", (int) orders.stream().filter(o -> "pending".equals(o.get("status"))).count());
        data.put("paidCount", (int) orders.stream().filter(o -> "paid".equals(o.get("status"))).count());
        data.put("shippedCount", (int) orders.stream().filter(o -> "shipped".equals(o.get("status"))).count());
        data.put("completedCount", (int) orders.stream().filter(o -> "completed".equals(o.get("status"))).count());

        log.debug("订单列表 - 最终返回, 订单数={}, 总数={}", orders.size(), data.get("total"));
        return data;
    }

    @Override
    public Map<String, Object> getOrderDetail(String orderId, boolean platformAdmin, Long currentStoreId,
                                              Long userIdFromToken) {
        Order order = resolveOrder(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 数据隔离：店铺管理员只能查看包含本店铺商品的订单
        if (currentStoreId != null) {
            if (!orderContainsStoreProduct(order, currentStoreId)) {
                throw new BusinessException(403, "您没有权限查看此订单");
            }
        } else if (!platformAdmin) {
            // 安全修复 #50：非管理员（普通用户）必须校验订单归属
            if (userIdFromToken == null) {
                throw new BusinessException(401, "未登录或 token 无效");
            }
            if (order.getUserId() == null || !userIdFromToken.equals(order.getUserId().longValue())) {
                log.warn("getOrderDetail 越权尝试：token userId={} 但订单归属 userId={}, orderId={}",
                        userIdFromToken, order.getUserId(), order.getId());
                throw new BusinessException(403, "无权查看此订单");
            }
        }

        Map<String, Object> orderData = orderViewAssembler.buildOrderDetailBase(order);

        List<OrderItem> allOrderItems = orderItemService.getOrderItemsByOrderId(order.getId());
        List<OrderItem> orderItems = allOrderItems;
        Long orderStoreId = null; // 订单关联的店铺ID（取第一个商品的店铺），供平台管理员添加发货仓时使用
        if (currentStoreId != null) {
            // 数据隔离：店铺管理员只显示当前店铺的商品并重算金额
            orderItems = new ArrayList<>();
            BigDecimal storeTotalAmount = BigDecimal.ZERO;
            for (OrderItem item : allOrderItems) {
                Product product = productService.getProductById(item.getProductId());
                if (product != null && currentStoreId.equals(product.getStoreId())) {
                    orderItems.add(item);
                    BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    BigDecimal itemQuantity = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 1);
                    storeTotalAmount = storeTotalAmount.add(itemPrice.multiply(itemQuantity));
                }
            }
            orderData.put("totalAmount", storeTotalAmount);
            orderData.put("payAmount", storeTotalAmount);
        }

        List<Map<String, Object>> orderItemList = new ArrayList<>();
        if (!orderItems.isEmpty()) {
            for (OrderItem item : orderItems) {
                Product p = productService.getProductById(item.getProductId());
                if (p != null && orderStoreId == null) {
                    orderStoreId = p.getStoreId();
                }
                orderItemList.add(orderViewAssembler.buildOrderItemView(item, p));
            }
        } else {
            orderItemList.add(orderViewAssembler.buildFallbackOrderItem(order));
        }
        orderData.put("orderItemList", orderItemList);
        orderData.put("products", orderItemList); // 兼容字段
        if (orderStoreId != null) {
            orderData.put("storeId", orderStoreId);
        }

        orderViewAssembler.fillReceiverAddress(order, orderData);

        // 与订单列表一致：管理端「客户/电话/地址」
        String recvName = orderData.get("receiverName") != null
                ? String.valueOf(orderData.get("receiverName")) : "";
        orderData.put("customer", recvName.isEmpty() ? ("用户" + order.getUserId()) : recvName);
        orderViewAssembler.enrichOrderContactForDisplay(order, orderData);

        orderViewAssembler.fillRefundInfo(order, orderData);

        log.debug("获取订单详情 - 最终返回数据中的phone: {}, address: {}",
                orderData.get("phone"), orderData.get("address"));
        return orderData;
    }

    private Order resolveOrder(String orderId) {
        if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
            return orderService.getOrderByOrderNo(orderId);
        }
        try {
            return orderService.getOrderById(Long.parseLong(orderId));
        } catch (NumberFormatException e) {
            // 无法转换为数字时按订单号查询
            return orderService.getOrderByOrderNo(orderId);
        }
    }

    private boolean orderContainsStoreProduct(Order order, Long storeId) {
        for (OrderItem item : orderItemService.getOrderItemsByOrderId(order.getId())) {
            Product product = productService.getProductById(item.getProductId());
            if (product != null && storeId.equals(product.getStoreId())) {
                return true;
            }
        }
        return false;
    }
}
