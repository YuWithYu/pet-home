package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.BusinessException;
import com.pethome.entity.Address;
import com.pethome.entity.Appointment;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.Product;
import com.pethome.entity.RefundRequest;
import com.pethome.entity.User;
import com.pethome.service.appointment.AppointmentService;
import com.pethome.service.order.AddressService;
import com.pethome.service.order.OrderItemService;
import com.pethome.service.order.OrderQueryAppService;
import com.pethome.service.order.OrderService;
import com.pethome.service.order.ProductService;
import com.pethome.service.order.RefundRequestService;
import com.pethome.service.user.UserService;
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

    @Autowired(required = false)
    private AppointmentService appointmentService;

    @Autowired(required = false)
    private AddressService addressService;

    @Autowired(required = false)
    private UserService userService;

    @Override
    public Map<String, Object> getLitterAppointments(String status, boolean adminSide, Long currentUserId) {
        if (!adminSide && currentUserId == null) {
            throw new BusinessException(401, "未登录或token无效");
        }
        List<Appointment> allAppointments = adminSide
                ? appointmentService.getAppointmentList(new Page<>(1, MAX_PAGE_SIZE)).getRecords()
                : appointmentService.getUserAppointments(currentUserId);
        List<Map<String, Object>> orders = new ArrayList<>();
        for (Appointment appointment : allAppointments) {
            Map<String, Object> order = new HashMap<>();
            order.put("id", "APPT_" + appointment.getId());
            order.put("customer", "用户" + appointment.getUserId());
            order.put("phone", appointment.getContactPhone() != null ? appointment.getContactPhone() : "");
            order.put("address", appointment.getLocation());
            order.put("totalAmount", appointment.getPrice() != null ? appointment.getPrice() : BigDecimal.ZERO);
            String dbStatus = appointment.getStatus();
            String orderStatus = "pending";
            if ("confirmed".equals(dbStatus)) {
                orderStatus = "paid";
            } else if ("completed".equals(dbStatus) || "1".equals(dbStatus)) {
                orderStatus = "completed";
            } else if ("cancelled".equals(dbStatus)) {
                orderStatus = "cancelled";
            }
            order.put("status", orderStatus);
            order.put("createTime", appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "");
            orders.add(order);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("orders", orders);
        data.put("total", orders.size());
        data.put("unpaidCount", (int) orders.stream().filter(o -> "pending".equals(o.get("status"))).count());
        data.put("paidCount", (int) orders.stream().filter(o -> "paid".equals(o.get("status"))).count());
        data.put("shippedCount", 0);
        data.put("completedCount", (int) orders.stream().filter(o -> "completed".equals(o.get("status"))).count());
        return data;
    }

    @Override
    public Map<String, Object> getOrderList(String status, String userIdParam, String orderCategory,
                                            Integer currentPage, Integer pageSize, boolean platformAdmin,
                                            Long userIdFromToken, Long currentStoreId) {
        Long userIdParsed = parseUserId(userIdParam);

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
                return emptyOrderList();
            }
            if (userIdParsed != null && !userIdParsed.equals(userIdFromToken)) {
                log.warn("getOrderList 越权尝试：token userId={} 但请求 userId={}", userIdFromToken, userIdParsed);
                return emptyOrderList();
            }
            userId = userIdFromToken;
        }

        // 无 userId 且非店铺上下文：仅平台管理员可查全表，否则返回空列表（避免扫全表）
        if (userId == null && currentStoreId == null && !platformAdmin) {
            return emptyOrderList();
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

        Integer statusCodeFilter = toStatusCodeFilter(status);
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
            return emptyOrderList();
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

            String orderStatus = toOrderStatus(order.getStatus());
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

            enrichOrderContactForDisplay(order, orderData);
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

        Map<String, Object> orderData = buildOrderDetailBase(order);

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
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("id", item.getId());
                itemData.put("productId", item.getProductId());
                itemData.put("productName", item.getProductName());
                itemData.put("productPic", item.getProductImage());
                itemData.put("productImage", item.getProductImage());
                itemData.put("productPrice", item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                itemData.put("price", item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                itemData.put("productQuantity", item.getQuantity() != null ? item.getQuantity() : 1);
                itemData.put("quantity", item.getQuantity() != null ? item.getQuantity() : 1);
                itemData.put("specName", item.getSpecName() != null ? item.getSpecName() : "");
                itemData.put("productAttr", ""); // 商品属性，暂时为空
                if (p != null && p.getStoreId() != null) {
                    itemData.put("storeId", p.getStoreId());
                }
                orderItemList.add(itemData);
            }
        } else {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("id", order.getId());
            itemData.put("productId", order.getId());
            itemData.put("productName", "商品订单 #" + order.getOrderNo());
            itemData.put("productPic", "");
            itemData.put("productImage", "");
            itemData.put("productPrice", order.getTotalAmount());
            itemData.put("price", order.getTotalAmount());
            itemData.put("productQuantity", 1);
            itemData.put("quantity", 1);
            itemData.put("specName", "");
            itemData.put("productAttr", "");
            orderItemList.add(itemData);
        }
        orderData.put("orderItemList", orderItemList);
        orderData.put("products", orderItemList); // 兼容字段
        if (orderStoreId != null) {
            orderData.put("storeId", orderStoreId);
        }

        fillReceiverAddress(order, orderData);

        // 与订单列表一致：管理端「客户/电话/地址」
        String recvName = orderData.get("receiverName") != null
                ? String.valueOf(orderData.get("receiverName")) : "";
        orderData.put("customer", recvName.isEmpty() ? ("用户" + order.getUserId()) : recvName);
        enrichOrderContactForDisplay(order, orderData);

        fillRefundInfo(order, orderData);

        log.debug("获取订单详情 - 最终返回数据中的phone: {}, address: {}",
                orderData.get("phone"), orderData.get("address"));
        return orderData;
    }

    /**
     * 订单基础字段（金额、状态、时间、物流、支付方式）组装。
     */
    private Map<String, Object> buildOrderDetailBase(Order order) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("id", order.getId());
        orderData.put("orderNo", order.getOrderNo());
        orderData.put("orderSn", order.getOrderNo()); // 兼容字段
        orderData.put("totalAmount", order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        orderData.put("payAmount", order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        orderData.put("freightAmount", BigDecimal.ZERO); // 运费，暂时为0
        orderData.put("promotionAmount", BigDecimal.ZERO); // 活动优惠，暂时为0
        orderData.put("couponAmount", BigDecimal.ZERO); // 优惠券，暂时为0
        orderData.put("integrationAmount", BigDecimal.ZERO); // 积分抵扣，暂时为0
        orderData.put("note", ""); // 备注

        Integer orderStatus = order.getStatus() != null ? order.getStatus() : 0;
        Integer paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus() : 0;

        orderData.put("status", toOrderStatus(orderStatus));
        orderData.put("statusNumber", orderStatus); // 同时保留数字状态，以备需要
        orderData.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : "");

        // 计算支付时间：如果订单已支付（status>=1 或 paymentStatus==1），使用创建时间
        String paymentTime = null;
        if ((orderStatus >= 1 || paymentStatus == 1) && order.getCreateTime() != null) {
            paymentTime = order.getCreateTime().toString();
        }
        orderData.put("paymentTime", paymentTime);

        // 计算发货时间：优先使用订单中的 shippingTime，否则使用更新时间
        String shippingTime = null;
        if (order.getShippingTime() != null) {
            shippingTime = order.getShippingTime().toString();
        } else if (orderStatus >= 2 && order.getUpdateTime() != null) {
            shippingTime = order.getUpdateTime().toString();
        }
        orderData.put("shippingTime", shippingTime);
        orderData.put("deliveryTime", shippingTime); // 兼容字段

        orderData.put("shippingCompany", order.getShippingCompany() != null ? order.getShippingCompany() : "");
        orderData.put("shippingNumber", order.getShippingNumber() != null ? order.getShippingNumber() : "");
        // 已支付默认显示微信支付
        orderData.put("payType", (orderStatus >= 1 || paymentStatus == 1) ? 2 : 0);

        // 为管理员后台添加兼容的时间字段
        orderData.put("payTime", paymentTime);
        orderData.put("shipTime", shippingTime);
        return orderData;
    }

    /**
     * 收货地址填充：优先使用订单快照，缺失时回落到用户默认地址。
     */
    private void fillReceiverAddress(Order order, Map<String, Object> orderData) {
        String receiverName = order.getReceiverName();
        String receiverPhone = order.getReceiverPhone();
        String receiverProvince = order.getReceiverProvince();
        String receiverCity = order.getReceiverCity();
        String receiverRegion = order.getReceiverRegion();
        String receiverDetailAddress = order.getReceiverDetailAddress();

        if (receiverName == null || receiverName.isEmpty()) {
            Address defaultAddress = addressService != null ? addressService.getDefaultAddress(order.getUserId()) : null;
            if (defaultAddress != null) {
                receiverName = defaultAddress.getContactName() != null ? defaultAddress.getContactName() : "";
                receiverPhone = defaultAddress.getContactPhone() != null ? defaultAddress.getContactPhone() : "";
                receiverProvince = defaultAddress.getProvince() != null ? defaultAddress.getProvince() : "";
                receiverCity = defaultAddress.getCity() != null ? defaultAddress.getCity() : "";
                receiverRegion = defaultAddress.getDistrict() != null ? defaultAddress.getDistrict() : "";
                receiverDetailAddress = defaultAddress.getDetail() != null ? defaultAddress.getDetail() : "";
            }
        }

        if (receiverName == null || receiverName.isEmpty()) {
            receiverName = "";
            receiverPhone = "";
            receiverProvince = "";
            receiverCity = "";
            receiverRegion = "";
            receiverDetailAddress = "";
        }

        StringBuilder fullAddress = new StringBuilder();
        if (receiverProvince != null && !receiverProvince.isEmpty()) fullAddress.append(receiverProvince);
        if (receiverCity != null && !receiverCity.isEmpty()) fullAddress.append(receiverCity);
        if (receiverRegion != null && !receiverRegion.isEmpty()) fullAddress.append(receiverRegion);
        if (receiverDetailAddress != null && !receiverDetailAddress.isEmpty()) fullAddress.append(receiverDetailAddress);

        orderData.put("receiverName", receiverName);
        orderData.put("receiverPhone", receiverPhone);
        orderData.put("receiverProvince", receiverProvince);
        orderData.put("receiverCity", receiverCity);
        orderData.put("receiverRegion", receiverRegion);
        orderData.put("receiverDetailAddress", receiverDetailAddress);
        // 为管理员后台添加兼容字段
        orderData.put("phone", receiverPhone);
        orderData.put("address", fullAddress.toString());
    }

    /**
     * 统一补充退款信息（用于 C 端“售后详情”展示申请原因/状态）。
     */
    private void fillRefundInfo(Order order, Map<String, Object> orderData) {
        RefundRequest refundRequest = refundRequestService.getByOrderId(order.getId());
        if (refundRequest != null && !"cancelled".equals(refundRequest.getStatus())) {
            orderData.put("hasRefundRequest", true);
            orderData.put("refundStatus", refundRequest.getStatus());
            orderData.put("refundReason", refundRequest.getReason());
            orderData.put("refundType", refundRequest.getType());
            orderData.put("refundAmount", refundRequest.getRefundAmount() != null ? refundRequest.getRefundAmount() : BigDecimal.ZERO);
            orderData.put("refundRequestId", refundRequest.getId());
            orderData.put("refundCreateTime", refundRequest.getCreateTime() != null ? refundRequest.getCreateTime().toString() : "");
        } else {
            orderData.put("hasRefundRequest", false);
        }
    }

    /**
     * 收货人/电话/地址在订单行未落库时，用默认地址与 C 端用户信息补齐（与 C 端展示一致）。
     */
    private void enrichOrderContactForDisplay(Order order, Map<String, Object> orderData) {
        if (order == null || orderData == null) {
            return;
        }
        Long uid = order.getUserId();
        if (uid == null) {
            return;
        }
        String userPlaceholder = "用户" + uid;
        String customer = (String) orderData.get("customer");
        String phone = orderData.get("phone") != null ? String.valueOf(orderData.get("phone")) : "";
        String addr = orderData.get("address") != null ? String.valueOf(orderData.get("address")) : "";
        boolean needEnrich = (phone != null && phone.isEmpty()) || (addr != null && addr.isEmpty())
                || userPlaceholder.equals(String.valueOf(customer));
        if (addressService != null && needEnrich) {
            Address def = addressService.getDefaultAddress(uid);
            if (def != null) {
                if (userPlaceholder.equals(String.valueOf(customer)) && def.getContactName() != null
                        && !def.getContactName().trim().isEmpty()) {
                    orderData.put("customer", def.getContactName().trim());
                }
                if (phone != null && phone.isEmpty() && def.getContactPhone() != null) {
                    orderData.put("phone", def.getContactPhone().trim());
                }
                if (addr != null && addr.isEmpty()) {
                    StringBuilder full = new StringBuilder();
                    if (def.getProvince() != null) full.append(def.getProvince());
                    if (def.getCity() != null) full.append(def.getCity());
                    if (def.getDistrict() != null) full.append(def.getDistrict());
                    if (def.getDetail() != null) full.append(def.getDetail());
                    orderData.put("address", full.toString());
                }
            }
        }
        customer = (String) orderData.get("customer");
        if (userService != null && (customer == null || customer.isEmpty() || userPlaceholder.equals(customer))) {
            User u = userService.getUserById(uid);
            if (u != null) {
                if (u.getNickname() != null && !u.getNickname().trim().isEmpty()) {
                    orderData.put("customer", u.getNickname().trim());
                } else if (u.getUsername() != null && !u.getUsername().trim().isEmpty()) {
                    orderData.put("customer", u.getUsername().trim());
                }
            }
        }
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

    private Long parseUserId(String userIdParam) {
        if (userIdParam == null || userIdParam.isEmpty()
                || "all".equals(userIdParam) || "null".equals(userIdParam)) {
            return null;
        }
        try {
            return Long.parseLong(userIdParam);
        } catch (NumberFormatException e) {
            log.warn("订单列表 - 警告: userId参数格式错误: {}, 将忽略该参数", userIdParam);
            return null;
        }
    }

    private Integer toStatusCodeFilter(String status) {
        if (status == null || status.isEmpty() || "all".equals(status)) {
            return null;
        }
        switch (status) {
            case "pending": return 0;
            case "paid": return 1;
            case "shipped": return 2;
            case "completed": return 3;
            case "cancelled": return -1;
            default: return null;
        }
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

    private Map<String, Object> emptyOrderList() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("orders", new ArrayList<>());
        empty.put("total", 0);
        empty.put("unpaidCount", 0);
        empty.put("paidCount", 0);
        empty.put("shippedCount", 0);
        empty.put("completedCount", 0);
        return empty;
    }
}
