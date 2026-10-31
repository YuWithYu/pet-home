package com.pethome.controller.order;

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
import com.pethome.common.BusinessException;
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
import com.pethome.service.order.AddressService;
import com.pethome.service.appointment.AppointmentService;
import com.pethome.service.order.LogisticsService;
import com.pethome.service.order.OrderItemService;
import com.pethome.service.order.OrderRefundAppService;
import com.pethome.service.order.OrderService;
import com.pethome.service.order.OrderWarehouseAppService;
import com.pethome.service.order.ProductService;
import com.pethome.service.order.RefundRequestService;
import com.pethome.service.user.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理")
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
    private AddressService addressService;
    
    @Autowired
    private LogisticsService logisticsService;
    
    @Autowired(required = false)
    private com.pethome.service.community.IMessageService messageService;
    
    @Autowired
    private RefundRequestService refundRequestService;

    @Autowired
    private OrderRefundAppService orderRefundAppService;
    
    @Autowired(required = false)
    private com.pethome.security.AdminContext adminContext;

    @Autowired(required = false)
    private UserService userService;
    
    @Autowired(required = false)
    private com.pethome.security.JwtUtil jwtUtil;

    @Autowired(required = false)
    private com.pethome.security.UserTokenResolver userTokenResolver;

    @Autowired
    private OrderWarehouseAppService orderWarehouseAppService;

    @Autowired
    private com.pethome.service.appointment.AppointmentDelayService appointmentDelayService;

    @Autowired(required = false)
    private com.pethome.service.order.UserCouponService userCouponService;

    /**
     * 从 token 中解析当前用户 ID（小程序/APP 端用，保证订单按用户隔离）
     * 实现已抽取至 UserTokenResolver（与 PaymentController 等共用，避免多处维护同一解析链）
     */
    private Long getUserIdFromToken(String authHeader) {
        if (userTokenResolver != null) {
            return userTokenResolver.getUserIdFromBearer(authHeader);
        }
        return null;
    }

    /**
     * 获取铲屎服务预约订单列表（兼容原有前端路径）
     */
    @GetMapping("/litter-appointments")
    @Operation(summary = "获取铲屎服务预约订单列表")
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
            log.error("获取铲屎服务预约列表失败", e);
            return Result.error("获取铲屎服务预约列表失败，请稍后重试");
        }
    }

    /**
     * 获取商品订单列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取商品订单列表")
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userIdStr,
            @RequestParam(value = "userId", required = false) String userIdFromQuery,
            @RequestParam(required = false) String orderCategory,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
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
            } else if (platformAdmin) {
                // 平台管理员：可按请求参数过滤，或全表（userId=null）
                userId = userIdParsed;
            } else {
                // 安全修复 #50：C 端普通用户必须以 token 为准，
                // 不再 fallback 到前端传入的 userId（否则可越权查看他人订单）。
                // 原注释"必须以 token 为准"与下方 fallback 实现矛盾，此处对齐注释。
                if (userIdFromToken != null) {
                    // 兼容校验：若前端传入的 userId 与 token 不一致，按越权处理
                    if (userIdParsed != null && !userIdParsed.equals(userIdFromToken)) {
                        log.warn("getOrderList 越权尝试：token userId={} 但请求 userId={}", userIdFromToken, userIdParsed);
                        Map<String, Object> empty = new HashMap<>();
                        empty.put("orders", new ArrayList<>());
                        empty.put("total", 0);
                        empty.put("unpaidCount", 0);
                        empty.put("paidCount", 0);
                        empty.put("shippedCount", 0);
                        empty.put("completedCount", 0);
                        return Result.success(empty);
                    }
                    userId = userIdFromToken;
                } else {
                    // 未登录（/api/orders/list 在 RoleInterceptor 白名单中允许匿名访问）：直接返回空列表
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

            // 分页：管理端传 currentPage/pageSize 时启用真分页（防拉全表，单页上限 1000）；
            // 未传时保持旧行为，兼容小程序 C 端与积分商城等旧调用
            boolean paginated = currentPage != null || pageSize != null;
            int pageNo = 1;
            int listPageSize;
            if (paginated) {
                pageNo = Math.max(1, currentPage == null ? 1 : currentPage);
                int requested = pageSize == null ? 10 : pageSize;
                listPageSize = Math.min(Math.max(requested, 1), 1000);
            } else {
                // C 端按用户查：条数收敛，手机列表足够；管理/店铺端仍 1000
                listPageSize = (userId != null && currentStoreId == null) ? 100 : 1000;
            }
            // 店铺端需按店铺商品内存过滤，SQL 分页会导致页内条数不定、总数失真；
            // 店铺端分页时改为先拉满窗口，过滤后再内存切页
            int sqlPageNo = (paginated && currentStoreId != null) ? 1 : pageNo;
            int sqlPageSize = (paginated && currentStoreId != null) ? 1000 : listPageSize;
            // SQL 级状态过滤：避免取回整页后再在内存丢弃（页内条数不足/管理端覆盖不到老订单）
            Integer statusCodeFilter = null;
            if (status != null && !status.isEmpty() && !status.equals("all")) {
                switch (status) {
                    case "pending": statusCodeFilter = 0; break;
                    case "paid": statusCodeFilter = 1; break;
                    case "shipped": statusCodeFilter = 2; break;
                    case "completed": statusCodeFilter = 3; break;
                    case "cancelled": statusCodeFilter = -1; break;
                    default: statusCodeFilter = null;
                }
            }
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
            List<RefundRequest> refundRows = refundRequestService.listByOrderIds(orderIds);
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
                        case 4: // 4 为历史遗留的取消值，兼容
                            orderStatus = "cancelled";
                            break;
                        default:
                            orderStatus = "pending";
                    }
                }
                orderData.put("status", orderStatus);

                // 如果指定了状态筛选，只返回匹配状态的订单（SQL 已过滤，此处为防御性兜底）
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
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取商品订单列表异常", e);
            return Result.error("获取商品订单列表失败，请稍后重试");
        }
    }

    /**
     * 获取商品订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取商品订单详情")
    public Result<Map<String, Object>> getOrderDetail(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：获取当前登录用户的店铺ID
            Long currentStoreId = null;
            boolean platformAdmin = false;
            if (adminContext != null && token != null) {
                String bearerToken = token.startsWith("Bearer ") ? token : ("Bearer " + token.trim());
                currentStoreId = adminContext.getCurrentStoreId(bearerToken);
                platformAdmin = adminContext.isPlatformAdmin(bearerToken);
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

            // 数据隔离：店铺管理员，检查订单是否包含该店铺的商品
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
            } else if (!platformAdmin) {
                // 安全修复 #50：非管理员（普通用户）必须校验订单归属，
                // 否则任意登录用户可读取他人订单的收货地址、商品、金额等敏感信息。
                Long currentUserId = getUserIdFromToken(token != null && token.startsWith("Bearer ") ? token : (token == null || token.trim().isEmpty() ? null : "Bearer " + token.trim()));
                if (currentUserId == null) {
                    return Result.error(401, "未登录或 token 无效");
                }
                if (order.getUserId() == null || !currentUserId.equals(order.getUserId().longValue())) {
                    log.warn("getOrderDetail 越权尝试：token userId={} 但订单归属 userId={}, orderId={}", currentUserId, order.getUserId(), order.getId());
                    return Result.error(403, "无权查看此订单");
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
            // status: 0=待付款, 1=已付款/待发货, 2=已发货, 3=已完成, -1=已取消（4 为历史遗留值，兼容）
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
                    case -1:
                    case 4: // 4 为历史遗留的取消值，兼容
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
            } catch (Exception e) {
                // 退款信息查询失败不影响主流程
                log.warn("获取订单详情 - 查询退款信息失败: {}", e.getMessage());
            }

            log.debug("获取订单详情 - 支付时间: {}, 发货时间: {}", paymentTime, shippingTime);
            log.debug("获取订单详情 - 最终返回数据中的phone: {}, address: {}", orderData.get("phone"), orderData.get("address"));
            
            return Result.success(orderData);
        } catch (Exception e) {
            log.error("获取商品订单详情失败", e);
            return Result.error("获取商品订单详情失败，请稍后重试");
        }
    }

    /**
     * 创建商品订单
     */
    @PostMapping("/create")
    @Operation(summary = "创建商品订单")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderData,
                                                    @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 安全修复 #37：不信任前端传入的 userId，强制从 token 解析当前登录用户
            Long currentUserId = resolveUserIdFromToken(token);
            if (currentUserId == null) {
                return Result.error(401, "用户未登录");
            }

            // 解析订单项（前端只需传 productId/quantity/specName，价格由后端查库填充，#36 防篡改）
            if (!orderData.containsKey("items") || !(orderData.get("items") instanceof List)) {
                return Result.error("订单商品项不能为空");
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawItems = (List<Map<String, Object>>) orderData.get("items");
            if (rawItems.isEmpty()) {
                return Result.error("订单商品项不能为空");
            }

            List<OrderItem> items = new java.util.ArrayList<>();
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

            Long addressId = null;
            if (orderData.get("addressId") != null) {
                try {
                    addressId = Long.parseLong(orderData.get("addressId").toString());
                } catch (NumberFormatException ignored) {
                    return Result.error(400, "地址ID格式错误");
                }
            }

            // 优惠券（可选）：userCouponId 为 user_coupon.id，归属/门槛/金额全部由后端校验
            Long userCouponId = null;
            if (orderData.get("userCouponId") != null && !orderData.get("userCouponId").toString().isBlank()) {
                try {
                    userCouponId = Long.parseLong(orderData.get("userCouponId").toString());
                } catch (NumberFormatException e) {
                    return Result.error(400, "优惠券ID格式错误");
                }
            }

            // 调用安全创建方法：内部会校验地址归属、重新计算金额、扣库存、核销优惠券、写订单+订单项（全部在一个事务内，#35 #38）
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
            return Result.success(result);
        } catch (com.pethome.common.BusinessException e) {
            // 业务异常：返回具体提示（如库存不足、地址越权）
            log.error("创建订单失败", e);
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error(500, "创建订单失败，请稍后重试");
        }
    }

    /**
     * 从 Authorization 头解析当前登录用户 ID（不信任前端传入的 userId）
     * 注意：token subject 可能是 username、手机号（新注册用户 username 为空时）或数字 ID，
     * 不能直接 Long.valueOf(subject)——手机号是纯数字会被误解析成错误的 userId（回归实测踩坑）。
     * 统一委托给 getUserIdFromToken 的健壮反查链（Redis 缓存 → username → phone → openid → 数字 ID）。
     */
    private Long resolveUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) return null;
        if (!token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }
        return getUserIdFromToken(token);
    }

    /**
     * 更新商品订单状态
     */
    @PutMapping("/{orderId}/status")
    @Operation(summary = "更新商品订单状态")
    public Result<Boolean> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 数据隔离：获取当前登录用户的店铺ID
            Long currentStoreId = null;
            boolean platformAdmin = false;
            String bearerToken = null;
            if (token != null && !token.trim().isEmpty()) {
                bearerToken = token.startsWith("Bearer ") ? token.trim() : ("Bearer " + token.trim());
            }
            if (adminContext != null && bearerToken != null) {
                currentStoreId = adminContext.getCurrentStoreId(bearerToken);
                platformAdmin = adminContext.isPlatformAdmin(bearerToken);
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

            // 数据隔离：店铺管理员，检查订单是否包含该店铺的商品
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
            } else if (!platformAdmin) {
                // 安全修复 #50：非管理员（普通用户）调用此接口时：
                // 1) 必须校验订单归属（防越权操作他人订单）；
                // 2) 仅允许 "cancelled"（用户主动取消待付款订单），其他管理态（shipped/completed/paid 等）必须由店铺/平台操作。
                Long currentUserId = getUserIdFromToken(bearerToken);
                if (currentUserId == null) {
                    return Result.error(401, "未登录或 token 无效");
                }
                if (order.getUserId() == null || !currentUserId.equals(order.getUserId().longValue())) {
                    log.warn("updateOrderStatus 越权尝试：token userId={} 但订单归属 userId={}, orderId={}", currentUserId, order.getUserId(), id);
                    return Result.error(403, "无权操作此订单");
                }
                if (!"cancelled".equalsIgnoreCase(status)) {
                    log.warn("updateOrderStatus 普通用户越权尝试：userId={}, orderId={}, status={}", currentUserId, id, status);
                    return Result.error(403, "无权执行此状态变更");
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

            // 库存统一由 OrderServiceImpl.updateOrderStatus 与下单扣减闭环管理：
            // 下单已扣减，支付(0→1)不再扣；取消(0/1→-1)在 Service 内恢复（并发条件更新保证只还一次）。
            // 此处历史扣减/恢复逻辑已删除（管理员手动标 paid 曾导致双扣库存）。

            return Result.success(updatedOrder != null);
        } catch (Exception e) {
            // 事务边界在 OrderServiceImpl.updateOrderStatus（DAO 写操作随其事务回滚），表现层只做错误出口
            log.error("更新订单状态失败: orderId={}, status={}", orderId, status, e);
            return Result.error("更新订单状态失败，请稍后重试");
        }
    }

    /**
     * 获取订单物流信息
     */
    @GetMapping("/{orderId}/logistics")
    @Operation(summary = "获取订单物流信息")
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
                    case -1:
                    case 4: // 4 为历史遗留的取消值，兼容
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
            return Result.error("获取物流信息失败，请稍后重试");
        }
    }

    /**
     * 获取当前店铺的发货仓地址列表（管理员用，用于发货时选择）
     * 店铺管理员：用 token 中的 storeId；平台管理员：可传 storeId 参数（当前订单的店铺ID）以查看/刷新该店铺的发货仓列表
     */
    @GetMapping("/warehouses")
    @Operation(summary = "获取当前店铺的发货仓列表")
    public Result<List<StoreWarehouse>> listWarehouses(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(required = false) Long storeId) {
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        if (currentStoreId == null && storeId != null) {
            currentStoreId = storeId;
        }
        boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
        return Result.success(orderWarehouseAppService.listWarehouses(currentStoreId, isPlatform));
    }

    /**
     * 添加发货仓地址（管理员用）
     * 店铺管理员：使用 token 中的 storeId；平台管理员：可传 body.storeId（如当前订单的店铺），须先打开订单详情以带出 storeId
     */
    @PostMapping("/warehouses")
    @Operation(summary = "添加发货仓地址")
    public Result<StoreWarehouse> addWarehouse(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
        return Result.success(orderWarehouseAppService.addWarehouse(body, currentStoreId, isPlatform));
    }

    @DeleteMapping("/warehouses/{id}")
    @Operation(summary = "删除发货仓地址")
    public Result<Boolean> deleteWarehouse(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        boolean isPlatform = adminContext != null && token != null && adminContext.isPlatformAdmin(token);
        orderWarehouseAppService.deleteWarehouse(id, currentStoreId, isPlatform);
        return Result.success(true);
    }

    /**
     * 更新订单物流信息（管理员用）
     */
    @PutMapping("/{orderId}/logistics")
    @Operation(summary = "更新订单物流信息")
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
            
            // 保存物流字段（此时内存中 status 未变，全字段覆盖无副作用）
            orderService.updateOrder(order);

            // 仅「已付款(1)」订单保存物流后自动转「已发货(2)」，并走正式状态机
            // （校验 1→2 合法性、置 deliveryStatus/shippingTime、发送发货通知）；
            // 待付款(0)订单不允许发货，防止绕过支付直接发货
            if (order.getStatus() != null && order.getStatus() == 1) {
                orderService.updateOrderStatus(order.getId(), 2);
            }
            
            // 记录日志
            log.info("更新订单物流信息成功 - 订单ID: {}, 物流公司: {}, 物流单号: {}, 发货仓: {}, 发货时间: {}",
                order.getId(), order.getShippingCompany(), order.getShippingNumber(), order.getWarehouseAddress(), order.getShippingTime());

            return Result.success(true);
        } catch (Exception e) {
            log.error("更新物流信息失败", e);
            return Result.error("更新物流信息失败，请稍后重试");
        }
    }

    /**
     * 获取支持的物流公司列表
     */
    @GetMapping("/logistics/companies")
    @Operation(summary = "获取支持的物流公司列表")
    public Result<List<Map<String, String>>> getLogisticsCompanies() {
        try {
            List<Map<String, String>> companies = logisticsService.getSupportedCompanies();
            return Result.success(companies);
        } catch (Exception e) {
            log.error("获取物流公司列表失败", e);
            return Result.error("获取物流公司列表失败，请稍后重试");
        }
    }

    /**
     * 申请退款
     */
    @PostMapping("/{orderId}/refund")
    @Operation(summary = "申请退款")
    public Result<Map<String, Object>> requestRefund(
            @PathVariable String orderId,
            @RequestBody Map<String, Object> refundData,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 安全修复：原实现 token 为空时直接跳过校验——匿名请求可替任意订单发起退款
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(401, "未登录或token无效");
        }
        Long tokenUserId = getUserIdFromToken(token.startsWith("Bearer ") ? token : ("Bearer " + token));
        if (tokenUserId == null) {
            throw new BusinessException(401, "未登录或token无效");
        }
        return Result.success(orderRefundAppService.requestRefund(orderId, refundData, tokenUserId));
    }

    /**
     * 用户取消退款申请（仅 pending 可取消）
     */
    @PutMapping("/{orderId}/refund/cancel")
    @Operation(summary = "取消退款申请")
    public Result<Boolean> cancelRefund(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long tokenUserId = getUserIdFromToken(token);
        return Result.success(orderRefundAppService.cancelRefund(orderId, tokenUserId));
    }

    /**
     * 获取退款申请列表
     */
    @GetMapping("/refunds")
    @Operation(summary = "获取退款申请列表")
    public Result<Map<String, Object>> getRefundList(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        log.debug("退款列表 - 当前店铺ID: {}", currentStoreId);
        int p = page != null && page > 0 ? page : 1;
        int sz = size != null && size > 0 ? size : 10;
        return Result.success(orderRefundAppService.getRefundList(p, sz, status, currentStoreId));
    }

    /**
     * 批准退款申请
     */
    @PutMapping("/refunds/{orderId}/approve")
    @Operation(summary = "批准退款申请")
    public Result<Map<String, Object>> approveRefund(
            @PathVariable String orderId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Admin currentAdmin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        String role = currentAdmin != null && currentAdmin.getRole() != null ? currentAdmin.getRole().toLowerCase() : "";
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        return Result.success(orderRefundAppService.approveRefund(orderId, currentStoreId, role));
    }

    /**
     * 拒绝退款申请
     */
    @PutMapping("/refunds/{orderId}/reject")
    @Operation(summary = "拒绝退款申请")
    public Result<Map<String, Object>> rejectRefund(
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Admin currentAdmin = adminContext != null && token != null ? adminContext.getCurrentAdmin(token) : null;
        String role = currentAdmin != null && currentAdmin.getRole() != null ? currentAdmin.getRole().toLowerCase() : "";
        Long currentStoreId = adminContext != null && token != null ? adminContext.getCurrentStoreId(token) : null;
        return Result.success(orderRefundAppService.rejectRefund(orderId, body, currentStoreId, role));
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

