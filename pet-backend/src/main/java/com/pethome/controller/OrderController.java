package com.pethome.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethome.common.Result;
import com.pethome.entity.Address;
import com.pethome.entity.Admin;
import com.pethome.entity.Appointment;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.Product;
import com.pethome.entity.RefundRequest;
import com.pethome.entity.StoreWarehouse;
import com.pethome.entity.User;
import com.pethome.mapper.PointsRecordMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.mapper.RefundRequestMapper;
import com.pethome.mapper.StoreWarehouseMapper;
import com.pethome.service.AddressService;
import com.pethome.service.AppointmentService;
import com.pethome.service.LogisticsService;
import com.pethome.service.OrderItemService;
import com.pethome.service.OrderService;
import com.pethome.service.ProductService;
import com.pethome.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@Api(tags = "订单管理")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    /**
     * 管理端订单列表：收货人/电话/地址在订单行未落库时，用默认地址与 C 端用户信息补齐（与普通商城订单展示一致）
     */
    private void enrichOrderListContactForDisplay(Order order, Map<String, Object> orderData) {
        if (order == null || orderData == null) {
            return;
        }
        try {
            Long uid = order.getUserId();
            if (uid == null) {
                return;
            }
            String userPlaceholder = "用户" + uid;
            String customer = (String) orderData.get("customer");
            String phone = orderData.get("phone") != null ? String.valueOf(orderData.get("phone")) : "";
            String addr = orderData.get("address") != null ? String.valueOf(orderData.get("address")) : "";
            if (addressService != null && ((phone != null && phone.isEmpty()) || (addr != null && addr.isEmpty())
                    || userPlaceholder.equals(String.valueOf(customer)))) {
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
                        if (def.getProvince() != null) {
                            full.append(def.getProvince());
                        }
                        if (def.getCity() != null) {
                            full.append(def.getCity());
                        }
                        if (def.getDistrict() != null) {
                            full.append(def.getDistrict());
                        }
                        if (def.getDetail() != null) {
                            full.append(def.getDetail());
                        }
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
        } catch (Exception e) {
            log.debug("enrichOrderListContactForDisplay: {}", e.getMessage());
        }
    }

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderItemService orderItemService;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AddressService addressService;
    
    @Autowired
    private LogisticsService logisticsService;
    
    @Autowired(required = false)
    private com.pethome.service.IMessageService messageService;
    
    @Autowired
    private RefundRequestMapper refundRequestMapper;
    
    @Autowired(required = false)
    private com.pethome.util.AdminContext adminContext;

    @Autowired(required = false)
    private UserService userService;
    
    @Autowired(required = false)
    private PointsRecordMapper pointsRecordMapper;

    @Autowired(required = false)
    private com.pethome.util.JwtUtil jwtUtil;

    @Autowired(required = false)
    private StoreWarehouseMapper storeWarehouseMapper;

    @Autowired
    private com.pethome.service.AppointmentDelayService appointmentDelayService;

    /**
     * 从 token 中解析当前用户 ID（小程序/APP 端用，保证订单按用户隔离）
     */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        // 微信登录等：UserServiceV2Impl 生成 token_<userId>_<timestamp>，本身不是 JWT；Redis 未命中时仍可从格式解析用户
        if (token.startsWith("token_")) {
            String[] parts = token.split("_", 3);
            if (parts.length >= 2) {
                try {
                    Long uid = Long.parseLong(parts[1]);
                    if (userService != null) {
                        User u = userService.getUserById(uid);
                        if (u != null && (u.getStatus() == null || u.getStatus() == 1)) {
                            return uid;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        try {
            User cachedUser = userService != null ? userService.getUserByToken(token) : null;
            if (cachedUser != null) {
                return cachedUser.getId();
            }
        } catch (Exception e) {
            // ignore
        }
        if (jwtUtil != null) {
            try {
                String subject = jwtUtil.getUsernameFromToken(token);
                if (subject != null && userService != null) {
                    User user = userService.getUserByUsername(subject);
                    if (user == null) {
                        user = userService.getUserByPhone(subject);
                    }
                    if (user == null && subject.length() >= 20) {
                        user = userService.getUserByOpenid(subject);
                    }
                    if (user == null && subject.matches("\\d+")) {
                        user = userService.getUserById(Long.parseLong(subject));
                    }
                    if (user != null) {
                        return user.getId();
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * 获取铲屎服务预约订单列表（兼容原有前端路径）
     */
    @GetMapping("/litter-appointments")
    @ApiOperation("获取铲屎服务预约订单列表")
    public Result<Map<String, Object>> getLitterAppointments(@RequestParam(required = false) String status) {
        try {
            Page<Appointment> page = new Page<>(1, 1000);
            IPage<Appointment> appointmentPage = appointmentService.getAppointmentList(page);
            List<Appointment> allAppointments = appointmentPage.getRecords();
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
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取铲屎服务预约列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品订单列表
     */
    @GetMapping("/list")
    @ApiOperation("获取商品订单列表")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userIdStr,
            @RequestParam(value = "userId", required = false) String userIdFromQuery,
            @RequestParam(required = false) String orderCategory,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 统一 Bearer，避免 AdminContext / getUserIdFromToken 因缺少前缀解析失败
            String bearerToken = null;
            if (token != null && !token.trim().isEmpty()) {
                String t = token.trim();
                bearerToken = t.startsWith("Bearer ") ? t : ("Bearer " + t);
            }
            boolean platformAdmin = adminContext != null && bearerToken != null && adminContext.isPlatformAdmin(bearerToken);

            // 解析 userId：店铺端可按请求参数 userId 筛选买家；C 端以 token 为准（避免本地缓存 userId 与登录态不一致）
            // 前端 uni-app 传参名为 userId，旧字段为 userIdStr，二者兼容
            String userIdParam = (userIdStr != null && !userIdStr.isEmpty()) ? userIdStr : userIdFromQuery;
            Long userIdParsed = null;
            if (userIdParam != null && !userIdParam.isEmpty() && !userIdParam.equals("all") && !userIdParam.equals("null")) {
                try {
                    userIdParsed = Long.parseLong(userIdParam);
                } catch (NumberFormatException e) {
                    log.warn("订单列表 - 警告: userId参数格式错误: {}, 将忽略该参数", userIdParam);
                }
            }
            Long userIdFromToken = null;
            if (bearerToken != null) {
                userIdFromToken = getUserIdFromToken(bearerToken);
            }
            // 数据隔离：获取当前登录用户的店铺ID（管理端）
            Long currentStoreId = null;
            if (adminContext != null && bearerToken != null) {
                currentStoreId = adminContext.getCurrentStoreId(bearerToken);
            }
            Long userId;
            if (currentStoreId != null) {
                // 店铺端：仅当显式传 userId 时按买家过滤；不能用 token 里的管理员 id 过滤订单买家
                userId = userIdParsed;
            } else {
                // C 端：必须以 token 为准；本地缓存的 userId 若过期会与登录态不一致，导致列表被滤空
                userId = userIdFromToken != null ? userIdFromToken : userIdParsed;
            }

            // 无 token：空。有 token 但无 userId、非店铺上下文：仅「平台管理员」可查全表；否则直接空列表。
            // 否则小程序异常/过期 token 会扫全表订单，极易超时且存在数据风险。
            if (userId == null && currentStoreId == null) {
                if (bearerToken == null || !platformAdmin) {
                    Map<String, Object> empty = new HashMap<>();
                    empty.put("orders", new ArrayList<>());
                    empty.put("total", 0);
                    empty.put("unpaidCount", 0);
                    empty.put("paidCount", 0);
                    empty.put("shippedCount", 0);
                    empty.put("completedCount", 0);
                    return Result.success(empty);
                }
            }

            // C 端按用户查：条数收敛，手机列表足够；管理/店铺端仍 1000
            int listPageSize = (userId != null && currentStoreId == null) ? 100 : 1000;
            Page<Order> page = new Page<>(1, listPageSize);
            IPage<Order> orderPage;
            if (userId != null && currentStoreId == null) {
                orderPage = orderService.getOrderListForUser(page, userId);
            } else {
                String cat = (orderCategory != null && !orderCategory.isEmpty()) ? orderCategory.trim() : null;
                if ("all".equalsIgnoreCase(cat)) {
                    cat = null;
                }
                orderPage = orderService.getOrderList(page, cat);
            }
            List<Order> allOrders = orderPage.getRecords();
            List<Map<String, Object>> orders = new ArrayList<>();

            if (allOrders.isEmpty()) {
                Map<String, Object> emptyData = new HashMap<>();
                emptyData.put("orders", orders);
                emptyData.put("total", 0);
                emptyData.put("unpaidCount", 0);
                emptyData.put("paidCount", 0);
                emptyData.put("shippedCount", 0);
                emptyData.put("completedCount", 0);
                return Result.success(emptyData);
            }

            List<Long> orderIds = new ArrayList<>(allOrders.size());
            for (Order o : allOrders) {
                orderIds.add(o.getId());
            }
            Map<Long, List<OrderItem>> itemsByOrderId = orderItemService.mapOrderItemsByOrderIds(orderIds);

            Map<Long, RefundRequest> refundByOrderId = new LinkedHashMap<>();
            // 订单列表统一返回退款状态，C 端据此展示“退款成功/申请中/失败”
            List<RefundRequest> refundRows = refundRequestMapper.selectByOrderIds(orderIds);
            if (refundRows != null) {
                for (RefundRequest r : refundRows) {
                    if (r.getOrderId() != null) {
                        refundByOrderId.putIfAbsent(r.getOrderId(), r);
                    }
                }
            }

            // C 端列表不做店铺维度过滤，无需批量查 product（省一次 IO，降低超时概率）
            Set<Long> productIdSet = new HashSet<>();
            Map<Long, Product> productById = new HashMap<>();
            if (currentStoreId != null) {
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
                
                // 如果是店铺管理员，使用当前店铺商品的金额；否则使用订单总金额
                if (currentStoreId != null && orderItems != null && !orderItems.isEmpty()) {
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
                
                // 转换订单状态
                String orderStatus = "pending";
                if (order.getStatus() != null) {
                    switch (order.getStatus()) {
                        case 0:
                            orderStatus = "pending";
                            break;
                        case 1:
                            orderStatus = "paid";
                            break;
                        case 2:
                            orderStatus = "shipped";
                            break;
                        case 3:
                            orderStatus = "completed";
                            break;
                        case -1:
                            orderStatus = "cancelled";
                            break;
                        default:
                            orderStatus = "pending";
                    }
                }
                orderData.put("status", orderStatus);
                
                // 如果指定了状态筛选，只返回匹配状态的订单
                if (status != null && !status.isEmpty() && !status.equals("all")) {
                    if (!status.equals(orderStatus)) {
                        continue;
                    }
                }
                orderData.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : "");
                
                // 获取订单商品详情（只显示当前店铺的商品）
                List<Map<String, Object>> products = new ArrayList<>();
                
                if (orderItems != null && !orderItems.isEmpty()) {
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
                    // 如果没有商品详情，使用默认信息
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", order.getId());
                    product.put("name", "商品订单 #" + order.getOrderNo());
                    product.put("image", "");
                    product.put("price", order.getTotalAmount());
                    product.put("quantity", 1);
                    products.add(product);
                }
                orderData.put("products", products);
                
                enrichOrderListContactForDisplay(order, orderData);
                orders.add(orderData);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("orders", orders);
            data.put("total", orders.size());
            data.put("unpaidCount", (int) orders.stream().filter(o -> "pending".equals(o.get("status"))).count());
            data.put("paidCount", (int) orders.stream().filter(o -> "paid".equals(o.get("status"))).count());
            data.put("shippedCount", (int) orders.stream().filter(o -> "shipped".equals(o.get("status"))).count());
            data.put("completedCount", (int) orders.stream().filter(o -> "completed".equals(o.get("status"))).count());
            
            log.debug("订单列表 - 最终返回, 订单数={}, 总数={}", orders.size(), data.get("total"));
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取商品订单列表异常", e);
            return Result.error("获取商品订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品订单详情
     */
    @GetMapping("/{orderId}")
    @ApiOperation("获取商品订单详情")
    public Result<Map<String, Object>> getOrderDetail(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：获取当前登录用户的店铺ID
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            
            Order order = null;
            // 判断orderId是数字ID还是订单号
            if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
                // 通过订单号查询
                order = orderService.getOrderByOrderNo(orderId);
            } else {
                // 通过数字ID查询
                try {
                    Long id = Long.parseLong(orderId);
                    order = orderService.getOrderById(id);
                } catch (NumberFormatException e) {
                    // 如果无法转换为数字，尝试作为订单号查询
                    order = orderService.getOrderByOrderNo(orderId);
                }
            }
            
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            
            // 数据隔离：如果是店铺管理员，检查订单是否包含该店铺的商品
            if (currentStoreId != null) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order.getId());
                boolean hasStoreProduct = false;
                for (OrderItem item : orderItems) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        hasStoreProduct = true;
                        break;
                    }
                }
                // 如果订单中没有该店铺的商品，拒绝访问
                if (!hasStoreProduct) {
                    return Result.error(403, "您没有权限查看此订单");
                }
            }
            
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
            
            // 根据订单状态和支付状态确定支付方式
            // status: 0=待付款, 1=已付款/待发货, 2=已发货, 3=已完成, 4=已取消
            // paymentStatus: 0=未支付, 1=已支付
            Integer orderStatus = order.getStatus() != null ? order.getStatus() : 0;
            Integer paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus() : 0;
            
            // 转换订单状态为字符串格式（与订单列表API保持一致）
            String statusString = "pending";
            if (orderStatus != null) {
                switch (orderStatus) {
                    case 0:
                        statusString = "pending"; // 待付款
                        break;
                    case 1:
                        statusString = "paid"; // 已付款/待发货
                        break;
                    case 2:
                        statusString = "shipped"; // 已发货
                        break;
                    case 3:
                        statusString = "completed"; // 已完成
                        break;
                    case 4:
                        statusString = "cancelled"; // 已取消
                        break;
                    default:
                        statusString = "pending";
                }
            }
            orderData.put("status", statusString);
            orderData.put("statusNumber", orderStatus); // 同时保留数字状态，以备需要
            orderData.put("createTime", order.getCreateTime() != null ? order.getCreateTime().toString() : "");
            
            // 计算支付时间：如果订单已支付（status>=1 或 paymentStatus==1），使用创建时间或更新时间
            String paymentTime = null;
            if ((orderStatus >= 1 || paymentStatus == 1) && order.getCreateTime() != null) {
                paymentTime = order.getCreateTime().toString();
            }
            orderData.put("paymentTime", paymentTime);
            
            // 计算发货时间：优先使用订单中的shippingTime，否则使用更新时间
            String shippingTime = null;
            if (order.getShippingTime() != null) {
                shippingTime = order.getShippingTime().toString();
            } else if (orderStatus >= 2 && order.getUpdateTime() != null) {
                shippingTime = order.getUpdateTime().toString();
            }
            orderData.put("shippingTime", shippingTime);
            orderData.put("deliveryTime", shippingTime); // 兼容字段
            
            // 添加物流信息
            orderData.put("shippingCompany", order.getShippingCompany() != null ? order.getShippingCompany() : "");
            orderData.put("shippingNumber", order.getShippingNumber() != null ? order.getShippingNumber() : "");
            
            // 如果订单状态>=1（已付款/待发货及以上）或支付状态=1（已支付），显示已支付
            // 否则显示未支付
            if (orderStatus >= 1 || paymentStatus == 1) {
                // 已支付，默认显示微信支付（可以根据实际情况设置）
                orderData.put("payType", 2); // 2=微信支付
            } else {
                // 未支付
                orderData.put("payType", 0); // 0=未支付
            }
            
            // 获取订单商品详情
            List<OrderItem> allOrderItems = orderItemService.getOrderItemsByOrderId(order.getId());
            List<OrderItem> orderItems = allOrderItems;
            
            // 数据隔离：如果是店铺管理员，只显示当前店铺的商品
            BigDecimal storeTotalAmount = BigDecimal.ZERO;
            if (currentStoreId != null) {
                orderItems = new ArrayList<>();
                for (OrderItem item : allOrderItems) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        orderItems.add(item);
                        // 计算当前店铺商品的金额
                        BigDecimal itemPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                        BigDecimal itemQuantity = BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 1);
                        storeTotalAmount = storeTotalAmount.add(itemPrice.multiply(itemQuantity));
                    }
                }
                // 更新订单金额为当前店铺商品的金额
                orderData.put("totalAmount", storeTotalAmount);
                orderData.put("payAmount", storeTotalAmount);
            }
            
            List<Map<String, Object>> orderItemList = new ArrayList<>();
            
            Long orderStoreId = null; // 订单关联的店铺ID（取第一个商品的店铺），供平台管理员添加发货仓时使用
            if (orderItems != null && !orderItems.isEmpty()) {
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
                // 如果没有商品详情，使用默认信息
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
            
            // 获取收货地址信息（优先使用订单中的地址，否则从用户的默认地址获取）
            try {
                String receiverName = order.getReceiverName();
                String receiverPhone = order.getReceiverPhone();
                String receiverProvince = order.getReceiverProvince();
                String receiverCity = order.getReceiverCity();
                String receiverRegion = order.getReceiverRegion();
                String receiverDetailAddress = order.getReceiverDetailAddress();
                
                // 如果订单中没有地址信息，尝试从用户的默认地址获取
                if (receiverName == null || receiverName.isEmpty()) {
                    Address defaultAddress = addressService.getDefaultAddress(order.getUserId());
                    log.debug("获取订单详情 - 用户ID: {}, 默认地址: {}", order.getUserId(), defaultAddress != null ? "存在" : "不存在");
                    if (defaultAddress != null) {
                        receiverName = defaultAddress.getContactName() != null ? defaultAddress.getContactName() : "";
                        receiverPhone = defaultAddress.getContactPhone() != null ? defaultAddress.getContactPhone() : "";
                        receiverProvince = defaultAddress.getProvince() != null ? defaultAddress.getProvince() : "";
                        receiverCity = defaultAddress.getCity() != null ? defaultAddress.getCity() : "";
                        receiverRegion = defaultAddress.getDistrict() != null ? defaultAddress.getDistrict() : "";
                        receiverDetailAddress = defaultAddress.getDetail() != null ? defaultAddress.getDetail() : "";
                    }
                }

                if (receiverName != null && !receiverName.isEmpty()) {

                    log.debug("获取订单详情 - 地址信息: 姓名={}, 电话={}, 省={}, 市={}, 区={}, 详细={}", receiverName, receiverPhone, receiverProvince, receiverCity, receiverRegion, receiverDetailAddress);
                    
                    // 组合完整地址
                    StringBuilder fullAddress = new StringBuilder();
                    if (receiverProvince != null && !receiverProvince.isEmpty()) {
                        fullAddress.append(receiverProvince);
                    }
                    if (receiverCity != null && !receiverCity.isEmpty()) {
                        fullAddress.append(receiverCity);
                    }
                    if (receiverRegion != null && !receiverRegion.isEmpty()) {
                        fullAddress.append(receiverRegion);
                    }
                    if (receiverDetailAddress != null && !receiverDetailAddress.isEmpty()) {
                        fullAddress.append(receiverDetailAddress);
                    }
                    String address = fullAddress.toString();
                    
                    orderData.put("receiverName", receiverName);
                    orderData.put("receiverPhone", receiverPhone);
                    orderData.put("receiverProvince", receiverProvince);
                    orderData.put("receiverCity", receiverCity);
                    orderData.put("receiverRegion", receiverRegion);
                    orderData.put("receiverDetailAddress", receiverDetailAddress);
                    
                    // 为管理员后台添加兼容字段
                    orderData.put("phone", receiverPhone); // 管理员后台期望的字段名
                    orderData.put("address", address); // 管理员后台期望的字段名

                    log.debug("获取订单详情 - 返回地址: {}, 电话: {}", address, receiverPhone);
                } else {
                    // 如果没有地址信息，设置为空
                    log.debug("获取订单详情 - 没有地址信息");
                    orderData.put("receiverName", "");
                    orderData.put("receiverPhone", "");
                    orderData.put("receiverProvince", "");
                    orderData.put("receiverCity", "");
                    orderData.put("receiverRegion", "");
                    orderData.put("receiverDetailAddress", "");
                    orderData.put("phone", ""); // 管理员后台期望的字段名
                    orderData.put("address", ""); // 管理员后台期望的字段名
                }
            } catch (Exception e) {
                // 获取地址失败，设置为空
                log.warn("获取订单详情 - 获取地址失败: {}", e.getMessage());
                orderData.put("receiverName", "");
                orderData.put("receiverPhone", "");
                orderData.put("receiverProvince", "");
                orderData.put("receiverCity", "");
                orderData.put("receiverRegion", "");
                orderData.put("receiverDetailAddress", "");
                orderData.put("phone", ""); // 管理员后台期望的字段名
                orderData.put("address", ""); // 管理员后台期望的字段名
            }
            
            // 与订单列表一致：管理端「客户/电话/地址」
            {
                String recvName = orderData.get("receiverName") != null
                        ? String.valueOf(orderData.get("receiverName")) : "";
                if (recvName == null || recvName.isEmpty()) {
                    orderData.put("customer", "用户" + order.getUserId());
                } else {
                    orderData.put("customer", recvName);
                }
            }
            enrichOrderListContactForDisplay(order, orderData);
            
            // 为管理员后台添加兼容的时间字段
            orderData.put("payTime", paymentTime); // 管理员后台期望的字段名
            orderData.put("shipTime", shippingTime); // 管理员后台期望的字段名

            // 统一补充退款信息（用于C端“售后详情”展示申请原因/状态）
            try {
                RefundRequest refundRequest = refundRequestMapper.selectByOrderId(order.getId());
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
            } catch (Exception e) {
                // 退款信息查询失败不影响主流程
                log.warn("获取订单详情 - 查询退款信息失败: {}", e.getMessage());
            }

            log.debug("获取订单详情 - 支付时间: {}, 发货时间: {}", paymentTime, shippingTime);
            log.debug("获取订单详情 - 最终返回数据中的phone: {}, address: {}", orderData.get("phone"), orderData.get("address"));
            
            return Result.success(orderData);
        } catch (Exception e) {
            log.error("获取商品订单详情失败", e);
            return Result.error("获取商品订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建商品订单
     */
    @PostMapping("/create")
    @ApiOperation("创建商品订单")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData) {
        try {
            // 校验必填参数
            if (orderData.get("userId") == null) {
                return Result.error("用户ID不能为空");
            }
            if (orderData.get("totalAmount") == null) {
                return Result.error("订单金额不能为空");
            }

            Order order = new Order();
            order.setOrderNo("ORD" + System.currentTimeMillis());
            order.setUserId(Long.parseLong(orderData.get("userId").toString()));
            BigDecimal totalAmount = new BigDecimal(orderData.get("totalAmount").toString());

            // 校验金额必须大于0
            if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("订单金额必须大于0");
            }
            order.setTotalAmount(totalAmount);
            order.setStatus(0); // 待付款
            order.setPaymentStatus(0);
            order.setDeliveryStatus(0);

            // 校验商品项不能为空
            if (!orderData.containsKey("items") || !(orderData.get("items") instanceof List)) {
                return Result.error("订单商品项不能为空");
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            if (items.isEmpty()) {
                return Result.error("订单商品项不能为空");
            }
            
            // 处理收货地址信息
            if (orderData.containsKey("addressId")) {
                // 如果提供了地址ID，从地址表获取地址信息
                try {
                    Long addressId = Long.parseLong(orderData.get("addressId").toString());
                    Address address = addressService.getAddressById(addressId);
                    if (address != null) {
                        order.setReceiverName(address.getContactName());
                        order.setReceiverPhone(address.getContactPhone());
                        order.setReceiverProvince(address.getProvince());
                        order.setReceiverCity(address.getCity());
                        order.setReceiverRegion(address.getDistrict());
                        order.setReceiverDetailAddress(address.getDetail());
                        log.debug("创建订单 - 从地址ID获取地址: {}, {}", address.getContactName(), address.getFullAddress());
                    }
                } catch (Exception e) {
                    log.warn("创建订单 - 获取地址失败: {}", e.getMessage());
                }
            } else if (orderData.containsKey("receiverName")) {
                // 如果直接提供了地址信息，直接使用
                order.setReceiverName(orderData.get("receiverName") != null ? orderData.get("receiverName").toString() : "");
                order.setReceiverPhone(orderData.get("receiverPhone") != null ? orderData.get("receiverPhone").toString() : "");
                order.setReceiverProvince(orderData.get("receiverProvince") != null ? orderData.get("receiverProvince").toString() : "");
                order.setReceiverCity(orderData.get("receiverCity") != null ? orderData.get("receiverCity").toString() : "");
                order.setReceiverRegion(orderData.get("receiverRegion") != null ? orderData.get("receiverRegion").toString() : "");
                order.setReceiverDetailAddress(orderData.get("receiverDetailAddress") != null ? orderData.get("receiverDetailAddress").toString() : "");
                log.debug("创建订单 - 直接使用提供的地址信息: {}, {}{}", order.getReceiverName(), order.getReceiverProvince(), order.getReceiverCity());
            } else {
                // 如果没有提供地址信息，尝试从用户的默认地址获取
                try {
                    Address defaultAddress = addressService.getDefaultAddress(order.getUserId());
                    if (defaultAddress != null) {
                        order.setReceiverName(defaultAddress.getContactName());
                        order.setReceiverPhone(defaultAddress.getContactPhone());
                        order.setReceiverProvince(defaultAddress.getProvince());
                        order.setReceiverCity(defaultAddress.getCity());
                        order.setReceiverRegion(defaultAddress.getDistrict());
                        order.setReceiverDetailAddress(defaultAddress.getDetail());
                        log.debug("创建订单 - 使用默认地址: {}, {}", defaultAddress.getContactName(), defaultAddress.getFullAddress());
                    }
                } catch (Exception e) {
                    log.warn("创建订单 - 获取默认地址失败: {}", e.getMessage());
                }
            }
            
            Order createdOrder = orderService.createOrder(order);

            appointmentDelayService.registerOrderCancelKey(createdOrder.getId(), 30);

            // 保存订单商品详情（items已在前面的校验中获取）
            for (Map<String, Object> itemData : items) {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrderId(createdOrder.getId());
                    orderItem.setProductId(itemData.get("productId") != null ? Long.parseLong(itemData.get("productId").toString()) : null);
                    orderItem.setProductName(itemData.get("productName") != null ? itemData.get("productName").toString() : "商品");
                    orderItem.setProductImage(itemData.get("productImage") != null ? itemData.get("productImage").toString() : "");
                    orderItem.setPrice(new BigDecimal(itemData.get("productPrice") != null ? itemData.get("productPrice").toString() : "0"));
                    orderItem.setQuantity(itemData.get("quantity") != null ? Integer.parseInt(itemData.get("quantity").toString()) : 1);
                    orderItem.setSpecName(itemData.get("specName") != null ? itemData.get("specName").toString() : "");
                    orderItem.setSpecPrice(itemData.get("specPrice") != null ? new BigDecimal(itemData.get("specPrice").toString()) : null);
                    
                    orderItemService.saveOrderItem(orderItem);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", createdOrder.getId());
            result.put("orderNo", createdOrder.getOrderNo());
            result.put("status", "pending");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("创建商品订单失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品订单状态
     */
    @PutMapping("/{orderId}/status")
    @ApiOperation("更新商品订单状态")
    public Result<Boolean> updateOrderStatus(
            @PathVariable String orderId, 
            @RequestParam String status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：获取当前登录用户的店铺ID
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            
            Long id;
            Order order = null;
            // 判断orderId是数字ID还是订单号
            if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
                // 如果是订单号（ORD或EX开头），通过订单号查找订单
                order = orderService.getOrderByOrderNo(orderId);
                if (order == null) {
                    return Result.error("订单不存在");
                }
                id = order.getId();
            } else {
                // 如果是数字ID，直接转换
                try {
                    id = Long.parseLong(orderId);
                    order = orderService.getOrderById(id);
                } catch (NumberFormatException e) {
                    // 如果无法转换为数字，尝试作为订单号查询
                    order = orderService.getOrderByOrderNo(orderId);
                    if (order == null) {
                        return Result.error("订单不存在");
                    }
                    id = order.getId();
                }
            }
            
            // 数据隔离：如果是店铺管理员，检查订单是否包含该店铺的商品
            if (currentStoreId != null) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(id);
                boolean hasStoreProduct = false;
                for (OrderItem item : orderItems) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        hasStoreProduct = true;
                        break;
                    }
                }
                // 如果订单中没有该店铺的商品，拒绝操作
                if (!hasStoreProduct) {
                    return Result.error(403, "您没有权限操作此订单");
                }
            }
            
            Integer statusCode = 0;
            
            switch (status) {
                case "pending":
                    statusCode = 0;
                    break;
                case "paid":
                    statusCode = 1;
                    break;
                case "shipped":
                    statusCode = 2;
                    break;
                case "completed":
                    statusCode = 3;
                    break;
                case "cancelled":
                    statusCode = -1;
                    break;
                case "deleted":
                    statusCode = -2;
                    break;
                default:
                    return Result.error("无效的订单状态");
            }
            
            Order oldOrder = orderService.getOrderById(id);
            Integer oldStatus = oldOrder != null ? oldOrder.getStatus() : null;
            
            // 状态机校验：只允许合法的状态转换
            if (oldStatus != null && !isValidStatusTransition(oldStatus, statusCode)) {
                return Result.error("非法的状态转换：不允许从 " + getStatusName(oldStatus) + " 转换为 " + status);
            }
            
            log.info("订单状态更新：orderId={}, 旧状态={}, 新状态={}", id, oldStatus, statusCode);
            
            Order updatedOrder = orderService.updateOrderStatus(id, statusCode);
            
            if (statusCode != 0) {
                appointmentDelayService.cancelOrderCancelKey(id);
            }
            
            // 获取订单商品列表（如果是店铺管理员，只处理当前店铺的商品）
            List<OrderItem> allOrderItems = orderItemService.getOrderItemsByOrderId(id);
            List<OrderItem> orderItems = allOrderItems;
            
            // 数据隔离：如果是店铺管理员，只处理当前店铺的商品
            if (currentStoreId != null) {
                orderItems = new ArrayList<>();
                for (OrderItem item : allOrderItems) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        orderItems.add(item);
                    }
                }
            }
            
            // 如果订单状态变为已支付（paid），使用乐观锁减少库存（防超卖）
            if (statusCode == 1 && (oldStatus == null || oldStatus != 1)) {
                for (OrderItem item : orderItems) {
                    if (item.getProductId() != null) {
                        int updated = productMapper.decrementStock(item.getProductId(), item.getQuantity());
                        if (updated == 0) {
                            throw new RuntimeException("商品库存不足，下单失败（productId=" + item.getProductId() + "）");
                        }
                        log.info("订单支付扣减库存: productId={}, quantity={}", item.getProductId(), item.getQuantity());
                    }
                }
            }

            // 如果订单被取消，恢复库存
            if (statusCode == -1 && oldStatus != null && oldStatus != -1) {
                for (OrderItem item : orderItems) {
                    if (item.getProductId() != null) {
                        Product product = productService.getProductById(item.getProductId());
                        if (product != null) {
                            product.setStock(product.getStock() + item.getQuantity());
                            productService.updateProduct(product);
                            log.info("订单取消恢复库存: productId={}, quantity={}", item.getProductId(), item.getQuantity());
                        }
                    }
                }
            }
            
            return Result.success(updatedOrder != null);
        } catch (Exception e) {
            return Result.error("更新商品订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单物流信息
     */
    @GetMapping("/{orderId}/logistics")
    @ApiOperation("获取订单物流信息")
    public Result<Map<String, Object>> getOrderLogistics(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：获取当前登录用户的店铺ID
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            
            Order order = null;
            // 判断orderId是数字ID还是订单号
            if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
                order = orderService.getOrderByOrderNo(orderId);
            } else {
                try {
                    Long id = Long.parseLong(orderId);
                    order = orderService.getOrderById(id);
                } catch (NumberFormatException e) {
                    order = orderService.getOrderByOrderNo(orderId);
                }
            }
            
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            
            // 数据隔离：如果是店铺管理员，检查订单是否包含该店铺的商品
            if (currentStoreId != null) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order.getId());
                boolean hasStoreProduct = false;
                for (OrderItem item : orderItems) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        hasStoreProduct = true;
                        break;
                    }
                }
                // 如果订单中没有该店铺的商品，拒绝访问
                if (!hasStoreProduct) {
                    return Result.error(403, "您没有权限查看此订单");
                }
            }
            
            Map<String, Object> logisticsData = new HashMap<>();
            logisticsData.put("orderId", order.getId());
            logisticsData.put("orderNo", order.getOrderNo());
            logisticsData.put("shippingCompany", order.getShippingCompany() != null ? order.getShippingCompany() : "");
            logisticsData.put("shippingNumber", order.getShippingNumber() != null ? order.getShippingNumber() : "");
            logisticsData.put("shippingTime", order.getShippingTime() != null ? order.getShippingTime().toString() : "");
            
            // 订单状态信息
            Integer orderStatus = order.getStatus() != null ? order.getStatus() : 0;
            String statusString = "pending";
            if (orderStatus != null) {
                switch (orderStatus) {
                    case 0:
                        statusString = "pending";
                        break;
                    case 1:
                        statusString = "paid";
                        break;
                    case 2:
                        statusString = "shipped";
                        break;
                    case 3:
                        statusString = "completed";
                        break;
                    case 4:
                        statusString = "cancelled";
                        break;
                    default:
                        statusString = "pending";
                }
            }
            logisticsData.put("status", statusString);
            logisticsData.put("statusNumber", orderStatus);
            
            // 收货地址信息
            logisticsData.put("receiverName", order.getReceiverName() != null ? order.getReceiverName() : "");
            logisticsData.put("receiverPhone", order.getReceiverPhone() != null ? order.getReceiverPhone() : "");
            String fullAddress = "";
            if (order.getReceiverProvince() != null) fullAddress += order.getReceiverProvince();
            if (order.getReceiverCity() != null) fullAddress += order.getReceiverCity();
            if (order.getReceiverRegion() != null) fullAddress += order.getReceiverRegion();
            if (order.getReceiverDetailAddress() != null) fullAddress += order.getReceiverDetailAddress();
            logisticsData.put("receiverAddress", fullAddress);
            
            // 发货仓地址
            logisticsData.put("warehouseAddress", order.getWarehouseAddress() != null ? order.getWarehouseAddress() : "");
            
            // 如果有物流单号，查询物流轨迹
            List<Map<String, Object>> tracks = new ArrayList<>();
            if (order.getShippingNumber() != null && !order.getShippingNumber().trim().isEmpty()) {
                try {
                    // 构建完整收货地址和发货仓地址
                    String fullReceiverAddress = fullAddress;
                    String warehouseAddr = order.getWarehouseAddress() != null ? order.getWarehouseAddress() : "";
                    // 获取发货时间，用于计算物流轨迹
                    java.time.LocalDateTime shippingTime = order.getShippingTime();
                    if (shippingTime == null) {
                        // 如果没有发货时间，使用当前时间
                        shippingTime = java.time.LocalDateTime.now();
                    }
                    Map<String, Object> logisticsResult = logisticsService.queryLogistics(
                        order.getShippingCompany(), 
                        order.getShippingNumber(),
                        fullReceiverAddress,  // 传递收货地址
                        warehouseAddr,  // 传递发货仓地址
                        shippingTime  // 传递发货时间
                    );
                    if (logisticsResult != null && logisticsResult.containsKey("tracks")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> queryTracks = (List<Map<String, Object>>) logisticsResult.get("tracks");
                        if (queryTracks != null && !queryTracks.isEmpty()) {
                            tracks = queryTracks;
                        }
                    }
                    if (logisticsResult != null) {
                        if (logisticsResult.containsKey("estimatedDays")) {
                            logisticsData.put("estimatedDays", logisticsResult.get("estimatedDays"));
                        }
                        if (logisticsResult.containsKey("estimatedDeliveryTip")) {
                            logisticsData.put("estimatedDeliveryTip", logisticsResult.get("estimatedDeliveryTip"));
                        }
                    }
                } catch (Exception e) {
                    log.warn("查询物流轨迹失败: {}", e.getMessage());
                    // 查询失败时返回空数组
                }
            }
            logisticsData.put("tracks", tracks);
            
            return Result.success(logisticsData);
        } catch (Exception e) {
            log.error("获取物流信息失败", e);
            return Result.error("获取物流信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前店铺的发货仓地址列表（管理员用，用于发货时选择）
     * 店铺管理员：用 token 中的 storeId；平台管理员：可传 storeId 参数（当前订单的店铺ID）以查看/刷新该店铺的发货仓列表
     */
    @GetMapping("/warehouses")
    @ApiOperation("获取当前店铺的发货仓列表")
    public Result<List<StoreWarehouse>> listWarehouses(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long storeId) {
        try {
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            if (currentStoreId == null && storeId != null) {
                currentStoreId = storeId;
            }
            if (currentStoreId == null) {
                boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
                if (isPlatform) {
                    QueryWrapper<StoreWarehouse> q = new QueryWrapper<>();
                    q.orderByAsc("id");
                    List<StoreWarehouse> list = storeWarehouseMapper.selectList(q);
                    return Result.success(list != null ? list : new ArrayList<>());
                }
                return Result.success(new ArrayList<>());
            }
            QueryWrapper<StoreWarehouse> q = new QueryWrapper<>();
            q.eq("store_id", currentStoreId).orderByAsc("id");
            List<StoreWarehouse> list = storeWarehouseMapper.selectList(q);
            return Result.success(list != null ? list : new ArrayList<>());
        } catch (Exception e) {
            log.error("获取发货仓列表失败", e);
            return Result.error("获取发货仓列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加发货仓地址（管理员用）
     * 店铺管理员：使用 token 中的 storeId；平台管理员：可传 body.storeId（如当前订单的店铺），须先打开订单详情以带出 storeId
     */
    @PostMapping("/warehouses")
    @ApiOperation("添加发货仓地址")
    public Result<StoreWarehouse> addWarehouse(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            // 平台管理员无 storeId 时，允许从请求体传入当前订单的 storeId
            if (currentStoreId == null && body != null && body.containsKey("storeId")) {
                Object sid = body.get("storeId");
                if (sid instanceof Number) {
                    currentStoreId = ((Number) sid).longValue();
                } else if (sid != null && !sid.toString().trim().isEmpty()) {
                    try {
                        currentStoreId = Long.parseLong(sid.toString().trim());
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (currentStoreId == null) {
                boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
                if (!isPlatform) {
                    return Result.error("当前账号无权操作，请使用管理员账号");
                }
            }
            if (storeWarehouseMapper == null) {
                return Result.error("功能暂不可用");
            }
            String address = null;
            if (body != null && body.get("address") != null) {
                address = body.get("address").toString().trim();
            }
            if (address == null || address.isEmpty()) {
                return Result.error("请输入发货仓地址");
            }
            StoreWarehouse wh = new StoreWarehouse();
            wh.setStoreId(currentStoreId != null ? currentStoreId : -1L);
            wh.setAddress(address);
            wh.setCreateTime(java.time.LocalDateTime.now());
            storeWarehouseMapper.insert(wh);
            return Result.success(wh);
        } catch (Exception e) {
            log.error("添加发货仓失败", e);
            return Result.error("添加发货仓失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/warehouses/{id}")
    @ApiOperation("删除发货仓地址")
    public Result<Boolean> deleteWarehouse(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (storeWarehouseMapper == null) {
                return Result.error("功能暂不可用");
            }
            StoreWarehouse existing = storeWarehouseMapper.selectById(id);
            if (existing == null) {
                return Result.error("发货仓不存在");
            }
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
            if (!isPlatform && currentStoreId != null && !currentStoreId.equals(existing.getStoreId())) {
                return Result.error("无权删除此发货仓");
            }
            storeWarehouseMapper.deleteById(id);
            return Result.success(true);
        } catch (Exception e) {
            log.error("删除发货仓失败", e);
            return Result.error("删除发货仓失败: " + e.getMessage());
        }
    }

    /**
     * 更新订单物流信息（管理员用）
     */
    @PutMapping("/{orderId}/logistics")
    @ApiOperation("更新订单物流信息")
    public Result<Boolean> updateOrderLogistics(
            @PathVariable String orderId, 
            @RequestBody Map<String, Object> logisticsData,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：获取当前登录用户的店铺ID
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            
            Order order = null;
            // 判断orderId是数字ID还是订单号
            if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
                order = orderService.getOrderByOrderNo(orderId);
            } else {
                try {
                    Long id = Long.parseLong(orderId);
                    order = orderService.getOrderById(id);
                } catch (NumberFormatException e) {
                    order = orderService.getOrderByOrderNo(orderId);
                }
            }
            
            if (order == null) {
                return Result.error(404, "订单不存在");
            }
            
            // 数据隔离：如果是店铺管理员，检查订单是否包含该店铺的商品
            if (currentStoreId != null) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order.getId());
                boolean hasStoreProduct = false;
                for (OrderItem item : orderItems) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        hasStoreProduct = true;
                        break;
                    }
                }
                // 如果订单中没有该店铺的商品，拒绝操作
                if (!hasStoreProduct) {
                    return Result.error(403, "您没有权限操作此订单");
                }
            }
            
            // 更新物流信息
            if (logisticsData.containsKey("shippingCompany")) {
                order.setShippingCompany(logisticsData.get("shippingCompany").toString());
            }
            if (logisticsData.containsKey("shippingNumber")) {
                order.setShippingNumber(logisticsData.get("shippingNumber").toString());
            }
            if (logisticsData.containsKey("shippingTime")) {
                try {
                    String timeStr = logisticsData.get("shippingTime").toString();
                    // 支持多种时间格式
                    java.time.LocalDateTime shippingTime = null;
                    if (timeStr.contains("T")) {
                        // ISO格式：2026-01-21T02:12:35 或 2026-01-21T02:12:35.000
                        if (timeStr.length() > 19) {
                            timeStr = timeStr.substring(0, 19);
                        }
                        shippingTime = java.time.LocalDateTime.parse(timeStr);
                    } else if (timeStr.contains(" ")) {
                        // 空格分隔格式：2026-01-21 02:12:35
                        shippingTime = java.time.LocalDateTime.parse(timeStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } else {
                        // 尝试直接解析
                        shippingTime = java.time.LocalDateTime.parse(timeStr);
                    }
                    order.setShippingTime(shippingTime);
                } catch (Exception e) {
                    log.warn("解析发货时间失败: {}, 错误: {}", logisticsData.get("shippingTime"), e.getMessage());
                    // 如果解析失败，使用当前时间
                    order.setShippingTime(java.time.LocalDateTime.now());
                }
            } else {
                // 如果没有提供发货时间，使用当前时间
                order.setShippingTime(java.time.LocalDateTime.now());
            }
            
            // 更新收货地址信息（如果提供）
            if (logisticsData.containsKey("receiverName")) {
                order.setReceiverName(logisticsData.get("receiverName").toString());
            }
            if (logisticsData.containsKey("receiverPhone")) {
                order.setReceiverPhone(logisticsData.get("receiverPhone").toString());
            }
            if (logisticsData.containsKey("receiverProvince")) {
                order.setReceiverProvince(logisticsData.get("receiverProvince").toString());
            }
            if (logisticsData.containsKey("receiverCity")) {
                order.setReceiverCity(logisticsData.get("receiverCity").toString());
            }
            if (logisticsData.containsKey("receiverRegion")) {
                order.setReceiverRegion(logisticsData.get("receiverRegion").toString());
            }
            if (logisticsData.containsKey("receiverDetailAddress")) {
                order.setReceiverDetailAddress(logisticsData.get("receiverDetailAddress").toString());
            }
            // 更新发货仓地址
            if (logisticsData.containsKey("warehouseAddress")) {
                order.setWarehouseAddress(logisticsData.get("warehouseAddress").toString());
            }
            
            // 如果订单状态是已付款（1）或待发货，更新为已发货（2）
            if (order.getStatus() != null && (order.getStatus() == 1 || order.getStatus() == 0)) {
                order.setStatus(2);
                order.setDeliveryStatus(1); // 1=已发货
            }
            
            // 保存订单
            orderService.updateOrder(order);
            
            // 记录日志
            log.info("更新订单物流信息成功 - 订单ID: {}, 物流公司: {}, 物流单号: {}, 发货仓: {}, 发货时间: {}",
                order.getId(), order.getShippingCompany(), order.getShippingNumber(), order.getWarehouseAddress(), order.getShippingTime());

            return Result.success(true);
        } catch (Exception e) {
            log.error("更新物流信息失败", e);
            return Result.error("更新物流信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的物流公司列表
     */
    @GetMapping("/logistics/companies")
    @ApiOperation("获取支持的物流公司列表")
    public Result<List<Map<String, String>>> getLogisticsCompanies() {
        try {
            List<Map<String, String>> companies = logisticsService.getSupportedCompanies();
            return Result.success(companies);
        } catch (Exception e) {
            log.error("获取物流公司列表失败", e);
            return Result.error("获取物流公司列表失败: " + e.getMessage());
        }
    }

    /**
     * 申请退款
     */
    @PostMapping("/{orderId}/refund")
    @ApiOperation("申请退款")
    public Result<Map<String, Object>> requestRefund(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> refundData,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 查找订单
            Order order = null;
            if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
                order = orderService.getOrderByOrderNo(orderId);
            } else {
                try {
                    Long id = Long.parseLong(orderId);
                    order = orderService.getOrderById(id);
                } catch (NumberFormatException e) {
                    order = orderService.getOrderByOrderNo(orderId);
                }
            }

            if (order == null) {
                return Result.error("订单不存在");
            }

            // 权限校验：只有订单所有者才能申请退款
            if (token != null && !token.trim().isEmpty()) {
                Long tokenUserId = getUserIdFromToken(token.startsWith("Bearer ") ? token : ("Bearer " + token));
                if (tokenUserId != null && order.getUserId() != null && !tokenUserId.equals(order.getUserId())) {
                    return Result.error(403, "无权操作该订单");
                }
            }

            // 检查订单状态，只有已完成的订单才能申请退款
            if (order.getStatus() == null || order.getStatus() != 3) {
                return Result.error("只有已完成的订单才能申请退款");
            }

            // 检查是否已经申请过退款
            RefundRequest existingRefund = refundRequestMapper.selectByOrderId(order.getId());
            if (existingRefund != null && !"rejected".equals(existingRefund.getStatus()) && !"cancelled".equals(existingRefund.getStatus())) {
                return Result.error("该订单已申请退款，请勿重复申请");
            }

            // 记录退款申请信息
            String type = refundData.get("type") != null ? refundData.get("type").toString() : "我要退货退款";
            String reason = refundData.get("reason") != null ? refundData.get("reason").toString() : "用户申请退款";
            BigDecimal amount = refundData.get("amount") != null ?
                new BigDecimal(refundData.get("amount").toString()) : order.getTotalAmount();

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
            refundRequest.setCreateTime(java.time.LocalDateTime.now());
            refundRequest.setUpdateTime(java.time.LocalDateTime.now());
            
            refundRequestMapper.insert(refundRequest);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", refundRequest.getId());
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("refundAmount", amount);
            result.put("type", type);
            result.put("reason", reason);
            result.put("status", "pending");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("申请退款失败", e);
            return Result.error("申请退款失败: " + e.getMessage());
        }
    }

    /**
     * 用户取消退款申请（仅 pending 可取消）
     */
    @PutMapping("/{orderId}/refund/cancel")
    @ApiOperation("取消退款申请")
    public Result<Boolean> cancelRefund(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Order order = null;
            if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
                order = orderService.getOrderByOrderNo(orderId);
            } else {
                try {
                    Long id = Long.parseLong(orderId);
                    order = orderService.getOrderById(id);
                } catch (NumberFormatException e) {
                    order = orderService.getOrderByOrderNo(orderId);
                }
            }
            if (order == null) {
                return Result.error("订单不存在");
            }

            Long tokenUserId = getUserIdFromToken(token);
            if (tokenUserId != null && order.getUserId() != null && !tokenUserId.equals(order.getUserId())) {
                return Result.error(403, "无权操作该订单退款申请");
            }

            RefundRequest refundRequest = refundRequestMapper.selectByOrderId(order.getId());
            if (refundRequest == null) {
                return Result.error("未找到退款申请");
            }
            if (!"pending".equals(refundRequest.getStatus())) {
                return Result.error("当前退款申请状态不可取消");
            }

            refundRequest.setStatus("cancelled");
            refundRequest.setUpdateTime(java.time.LocalDateTime.now());
            refundRequestMapper.updateById(refundRequest);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error("取消退款申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取退款申请列表
     */
    @GetMapping("/refunds")
    @ApiOperation("获取退款申请列表")
    public Result<Map<String, Object>> getRefundList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            log.debug("退款列表 - 当前店铺ID: {}", currentStoreId);

            int p = page != null && page > 0 ? page : 1;
            int sz = size != null && size > 0 ? size : 10;
            Page<RefundRequest> mpPage = new Page<>(p, sz);
            QueryWrapper<RefundRequest> qw = new QueryWrapper<>();
            if (status != null && !status.trim().isEmpty()) {
                qw.eq("status", status.trim());
            }
            if (currentStoreId != null) {
                qw.inSql("order_id",
                        "SELECT DISTINCT oi.order_id FROM order_item oi INNER JOIN product p ON p.id = oi.product_id WHERE p.store_id = "
                                + currentStoreId);
            }
            qw.orderByDesc("create_time");
            refundRequestMapper.selectPage(mpPage, qw);

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
                BigDecimal storeRefundAmount = refundRequest.getRefundAmount() != null ? refundRequest.getRefundAmount() : BigDecimal.ZERO;
                
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
                refund.put("status", refundRequest.getStatus()); // 从退款申请表获取真实状态
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
                    (order.getReceiverProvince() != null ? order.getReceiverProvince() : "") +
                    (order.getReceiverCity() != null ? order.getReceiverCity() : "") +
                    (order.getReceiverRegion() != null ? order.getReceiverRegion() : "") +
                    (order.getReceiverDetailAddress() != null ? order.getReceiverDetailAddress() : ""));
                
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
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取退款申请列表异常", e);
            return Result.error("获取退款申请列表失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误"));
        }
    }

    /**
     * 批准退款申请
     */
    @PutMapping("/refunds/{orderId}/approve")
    @ApiOperation("批准退款申请")
    public Result<Map<String, Object>> approveRefund(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (adminContext != null && token != null) {
                Admin currentAdmin = adminContext.getCurrentAdmin(token);
                if (currentAdmin != null) {
                    String role = currentAdmin.getRole() == null ? "" : currentAdmin.getRole().toLowerCase();
                    if ("staff".equals(role)) {
                        return Result.error(403, "当前角色无权处理退款审批");
                    }
                }
            }
            // 数据隔离：获取当前登录用户的店铺ID
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }
            
            // 查找订单
            Order order = null;
            if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
                order = orderService.getOrderByOrderNo(orderId);
            } else {
                try {
                    Long id = Long.parseLong(orderId);
                    order = orderService.getOrderById(id);
                } catch (NumberFormatException e) {
                    order = orderService.getOrderByOrderNo(orderId);
                }
            }
            
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 数据隔离：如果是店铺管理员，检查订单是否包含该店铺的商品
            if (currentStoreId != null) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order.getId());
                boolean hasStoreProduct = false;
                for (OrderItem item : orderItems) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        hasStoreProduct = true;
                        break;
                    }
                }
                // 如果订单中没有该店铺的商品，拒绝操作
                if (!hasStoreProduct) {
                    return Result.error(403, "您没有权限操作此订单");
                }
            }
            
            // 查找退款申请记录
            RefundRequest refundRequest = refundRequestMapper.selectByOrderId(order.getId());
            if (refundRequest == null) {
                return Result.error("退款申请记录不存在");
            }
            if ("approved".equals(refundRequest.getStatus())) {
                return Result.error("该退款申请已批准，请勿重复处理");
            }
            if (!"pending".equals(refundRequest.getStatus())) {
                return Result.error("该退款申请已处理");
            }
            
            // 更新退款状态为已批准
            refundRequest.setStatus("approved");
            refundRequest.setUpdateTime(java.time.LocalDateTime.now());
            refundRequestMapper.updateById(refundRequest);

            // 积分订单退款：审批通过后返还积分（按原兑换扣除积分数返还）
            Integer pointsRefunded = 0;
            boolean isPointsOrder = order.getOrderNo() != null && order.getOrderNo().startsWith("EX");
            if (isPointsOrder) {
                if (order.getUserId() == null) {
                    return Result.error("积分退款失败：订单缺少用户信息");
                }
                if (userService == null || pointsRecordMapper == null) {
                    return Result.error("积分退款失败：积分服务不可用");
                }

                String exchangeRef = order.getOrderNo().substring(2);
                Integer pointsToReturn = 0;
                try {
                    if (!exchangeRef.isEmpty()) {
                        Long spendRecordId = Long.parseLong(exchangeRef);
                        PointsRecord spendRecord = pointsRecordMapper.selectById(spendRecordId);
                        if (spendRecord != null && "spend".equals(spendRecord.getType()) && spendRecord.getPoints() != null) {
                            pointsToReturn = Math.max(spendRecord.getPoints(), 0);
                        }
                    }
                } catch (NumberFormatException ignored) {
                    // EX 后缀非数字时走兜底逻辑
                }
                if (pointsToReturn <= 0 && refundRequest.getRefundAmount() != null) {
                    pointsToReturn = Math.max(refundRequest.getRefundAmount().intValue(), 0);
                }
                if (pointsToReturn <= 0) {
                    return Result.error("积分退款失败：未找到可返还的积分数");
                }

                // 防重：存在同订单号的“退款返还”积分记录时，不重复返还
                QueryWrapper<PointsRecord> duplicateQw = new QueryWrapper<>();
                duplicateQw.eq("user_id", order.getUserId())
                        .eq("type", "earn")
                        .like("description", "订单退款返还积分")
                        .like("description", order.getOrderNo());
                Long duplicateCount = pointsRecordMapper.selectCount(duplicateQw);
                if (duplicateCount == null || duplicateCount == 0) {
                    User user = userService.getUserById(order.getUserId());
                    if (user == null) {
                        return Result.error("积分退款失败：用户不存在");
                    }
                    int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
                    user.setPoints(currentPoints + pointsToReturn);
                    userService.updateUser(user);

                    PointsRecord earnRecord = new PointsRecord();
                    earnRecord.setUserId(order.getUserId());
                    earnRecord.setType("earn");
                    earnRecord.setPoints(pointsToReturn);
                    earnRecord.setDescription("订单退款返还积分（订单号：" + order.getOrderNo() + "）");
                    earnRecord.setCreateTime(java.time.LocalDateTime.now());
                    pointsRecordMapper.insert(earnRecord);
                }
                pointsRefunded = pointsToReturn;
            }

            // 退款批准后恢复商品库存（非积分订单）
            if (!isPointsOrder) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order.getId());
                for (OrderItem item : orderItems) {
                    if (item.getProductId() != null) {
                        Product product = productService.getProductById(item.getProductId());
                        if (product != null) {
                            product.setStock(product.getStock() + item.getQuantity());
                            productService.updateProduct(product);
                            log.info("退款恢复库存: orderId={}, productId={}, quantity={}", order.getId(), item.getProductId(), item.getQuantity());
                        }
                    }
                }
            }

            // 按产品要求：退款批准后不再下发"店铺客服"会话消息，避免消息页出现该通知

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", "approved");
            if (isPointsOrder) {
                result.put("pointsRefunded", pointsRefunded);
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("批准退款失败", e);
            return Result.error("批准退款失败: " + e.getMessage());
        }
    }

    /**
     * 拒绝退款申请
     */
    @PutMapping("/refunds/{orderId}/reject")
    @ApiOperation("拒绝退款申请")
    public Result<Map<String, Object>> rejectRefund(
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (adminContext != null && token != null) {
                Admin currentAdmin = adminContext.getCurrentAdmin(token);
                if (currentAdmin != null) {
                    String role = currentAdmin.getRole() == null ? "" : currentAdmin.getRole().toLowerCase();
                    if ("staff".equals(role)) {
                        return Result.error(403, "当前角色无权处理退款审批");
                    }
                }
            }
            Long currentStoreId = null;
            if (adminContext != null && token != null) {
                currentStoreId = adminContext.getCurrentStoreId(token);
            }

            Order order = null;
            if (orderId != null && (orderId.startsWith("ORD") || orderId.startsWith("EX"))) {
                order = orderService.getOrderByOrderNo(orderId);
            } else {
                try {
                    Long id = Long.parseLong(orderId);
                    order = orderService.getOrderById(id);
                } catch (NumberFormatException e) {
                    order = orderService.getOrderByOrderNo(orderId);
                }
            }

            if (order == null) {
                return Result.error("订单不存在");
            }

            if (currentStoreId != null) {
                List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order.getId());
                boolean hasStoreProduct = false;
                for (OrderItem item : orderItems) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null && currentStoreId.equals(product.getStoreId())) {
                        hasStoreProduct = true;
                        break;
                    }
                }
                if (!hasStoreProduct) {
                    return Result.error(403, "您没有权限操作此订单");
                }
            }

            RefundRequest refundRequest = refundRequestMapper.selectByOrderId(order.getId());
            if (refundRequest == null) {
                return Result.error("退款申请记录不存在");
            }

            String st = refundRequest.getStatus();
            if (st != null && !"pending".equals(st)) {
                return Result.error("该退款申请已处理");
            }

            String rejectReason = "";
            if (body != null && body.get("rejectReason") != null) {
                rejectReason = body.get("rejectReason").toString().trim();
            }
            if (rejectReason.isEmpty()) {
                return Result.error("拒绝退款原因不能为空");
            }

            refundRequest.setStatus("rejected");
            // 复用 reason 字段持久化“拒绝原因”，避免用户端看不到处理原因
            String userApplyReason = refundRequest.getReason() == null ? "" : refundRequest.getReason().trim();
            String mergedReason = userApplyReason.isEmpty()
                    ? ("拒绝原因：" + rejectReason)
                    : ("申请原因：" + userApplyReason + "；拒绝原因：" + rejectReason);
            refundRequest.setReason(mergedReason);
            refundRequest.setUpdateTime(java.time.LocalDateTime.now());
            refundRequestMapper.updateById(refundRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("status", "rejected");
            result.put("rejectReason", rejectReason);

            return Result.success(result);
        } catch (Exception e) {
            log.error("拒绝退款失败", e);
            return Result.error("拒绝退款失败: " + e.getMessage());
        }
    }
    
    /**
     * 校验订单状态转换是否合法
     * 合法转换：
     * - pending(0) → paid(1)
     * - paid(1) → shipped(2)
     * - shipped(2) → completed(3)
     * - pending(0) → cancelled(-1)
     * - paid(1) → cancelled(-1)
     * - any → deleted(-2) [软删除]
     */
    private boolean isValidStatusTransition(Integer fromStatus, Integer toStatus) {
        if (fromStatus == null || toStatus == null) {
            return true;
        }
        
        switch (fromStatus) {
            case 0: // pending
                return toStatus == 1 || toStatus == -1 || toStatus == -2; // pending→paid/cancelled/deleted
            case 1: // paid
                return toStatus == 2 || toStatus == -1 || toStatus == -2; // paid→shipped/cancelled/deleted
            case 2: // shipped
                return toStatus == 3 || toStatus == -2; // shipped→completed/deleted
            case 3: // completed
                return toStatus == -2; // completed→deleted
            case -1: // cancelled
                return toStatus == -2; // cancelled→deleted
            default:
                return false;
        }
    }
    
    /**
     * 获取状态名称（中文）
     */
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
