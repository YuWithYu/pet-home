package com.pethome.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.common.Result;
import com.pethome.entity.Product;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.User;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.mapper.PointsRecordMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.mapper.UserMapper;
import com.pethome.service.PointsRecordService;
import com.pethome.service.UserService;
import com.pethome.service.OrderService;
import com.pethome.service.OrderItemService;
import com.pethome.service.AddressService;
import com.pethome.entity.Address;
import com.pethome.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Api(tags = "积分商城")
public class PointsMallController {

    private static final Logger log = LoggerFactory.getLogger(PointsMallController.class);

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Value("${upload.http-url:http://localhost:8080/upload/}")
    private String uploadHttpUrl;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PointsRecordService pointsRecordService;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderItemService orderItemService;
    
    @Autowired
    private AddressService addressService;

    @Autowired
    private com.pethome.service.AppointmentDelayService appointmentDelayService;

    private String publicSiteOrigin() {
        if (publicBaseUrl != null && !publicBaseUrl.trim().isEmpty()) {
            return publicBaseUrl.trim().replaceAll("/+$", "");
        }
        String u = uploadHttpUrl != null ? uploadHttpUrl.trim() : "http://localhost:8080/upload/";
        if (!u.endsWith("/")) {
            u += "/";
        }
        if (u.toLowerCase().endsWith("upload/")) {
            u = u.substring(0, u.length() - "upload/".length());
        }
        return u.replaceAll("/+$", "");
    }

    /**
     * 获取积分商城商品列表
     */
    @GetMapping("/products")
    @ApiOperation("获取积分商城商品列表")
    public Result<Map<String, Object>> getPointsMallProducts(
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 查询积分商品（category为"积分商城"的商品）
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("category", "积分商城")
                       .eq("status", 1) // 只查询上架的商品
                       .orderByDesc("create_time");
            
            List<Product> products = productMapper.selectList(queryWrapper);

            log.debug("查询积分商城商品: category=积分商城, status=1, count={}",
                    products != null ? products.size() : 0);
            
            // 转换为前端需要的格式
            List<Map<String, Object>> productList = new ArrayList<>();
            if (products != null && !products.isEmpty()) {
                // 获取所有兑换记录，用于统计每个商品的兑换次数
                // 通过查询所有消耗记录，然后过滤出兑换记录
                QueryWrapper<PointsRecord> exchangeQuery = new QueryWrapper<>();
                exchangeQuery.eq("type", "spend")
                            .like("description", "兑换商品:")
                            .orderByDesc("create_time");
                List<PointsRecord> allExchangeRecords = pointsRecordMapper.selectList(exchangeQuery);
                if (allExchangeRecords == null) {
                    allExchangeRecords = new ArrayList<>();
                }
                
                for (Product product : products) {
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", product.getId());
                    productMap.put("title", product.getName());
                    productMap.put("subtitle", product.getFeatures()); // 使用features作为副标题
                    productMap.put("desc", product.getDescription());
                    // 处理图片URL
                    String imageUrl = product.getImage();
                    if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                        String base = publicSiteOrigin();
                        imageUrl = base + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
                    }
                    productMap.put("image", imageUrl);
                    // 价格转换为kg（前端显示的是kg，后端存储的是g）
                    // 注意：如果price是BigDecimal，需要正确处理
                    int priceInKg = 0;
                    if (product.getPrice() != null) {
                        try {
                            BigDecimal price = product.getPrice();
                            priceInKg = price.divide(new BigDecimal("1000")).intValue();
                        } catch (Exception e) {
                            // 如果price已经是整数（g），直接除以1000
                            priceInKg = product.getPrice().intValue() / 1000;
                        }
                    }
                    productMap.put("price", priceInKg); // 转换为整数kg
                    // 判断商品状态：库存为0或已下架为"已结束"
                    if (product.getStock() != null && product.getStock() > 0) {
                        productMap.put("status", "available"); // 可兑换
                    } else {
                        productMap.put("status", "ended"); // 已结束
                    }
                    
                    // 统计该商品的兑换次数（通过description匹配）
                    int exchangedCount = 0;
                    String productName = product.getName();
                    for (PointsRecord record : allExchangeRecords) {
                        if (record.getDescription() != null && 
                            record.getDescription().startsWith("兑换商品:") &&
                            record.getDescription().contains(productName)) {
                            exchangedCount++;
                        }
                    }
                    productMap.put("exchangedCount", exchangedCount);
                    
                    productList.add(productMap);
                    log.debug("积分商品: name={}, price={}kg, stock={}, exchanged={}", product.getName(), priceInKg, product.getStock(), exchangedCount);
                }
            } else {
                log.warn("未找到积分商城商品");
            }

            log.info("返回积分商品列表, 数量={}", productList.size());

            Map<String, Object> result = new HashMap<>();
            result.put("products", productList);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取积分商品列表失败", e);
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 兑换积分商品
     */
    @PostMapping("/exchange")
    @ApiOperation("兑换积分商品")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> exchangeProduct(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, Object> requestData) {
        try {
            // 获取用户ID
            Long userId = getUserIdFromToken(token);
            if (userId == null) {
                return Result.error(401, "未登录");
            }

            // 获取商品ID
            Long productId = null;
            if (requestData.get("productId") != null) {
                if (requestData.get("productId") instanceof Number) {
                    productId = ((Number) requestData.get("productId")).longValue();
                } else {
                    productId = Long.parseLong(requestData.get("productId").toString());
                }
            }

            if (productId == null) {
                return Result.error("商品ID不能为空");
            }

            // 兑换数量（默认1，最小1）
            int quantity = 1;
            if (requestData.get("quantity") != null) {
                try {
                    Object qObj = requestData.get("quantity");
                    if (qObj instanceof Number) {
                        quantity = ((Number) qObj).intValue();
                    } else {
                        quantity = Integer.parseInt(String.valueOf(qObj));
                    }
                } catch (Exception ignored) {
                    quantity = 1;
                }
            }
            if (quantity < 1) {
                quantity = 1;
            }

            // 查询商品
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return Result.error("商品不存在");
            }

            // 检查商品是否属于积分商城
            if (!"积分商城".equals(product.getCategory())) {
                return Result.error("该商品不属于积分商城");
            }

            // 检查商品状态
            if (product.getStatus() == null || product.getStatus() != 1) {
                return Result.error("商品已下架");
            }

            // 检查库存
            if (product.getStock() == null || product.getStock() < quantity) {
                return Result.error("商品库存不足");
            }

            // 计算需要的积分（商品价格为单件积分，单位g）
            int unitPoints = product.getPrice() != null ? product.getPrice().intValue() : 0;
            int requiredPoints = unitPoints * quantity;

            // 获取用户信息
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 检查用户积分是否足够
            int userPoints = user.getPoints() != null ? user.getPoints() : 0;
            if (userPoints < requiredPoints) {
                return Result.error("积分不足，需要 " + requiredPoints + "g，当前有 " + userPoints + "g");
            }

            // 扣除积分
            int newPoints = userPoints - requiredPoints;
            user.setPoints(newPoints);
            userMapper.updateById(user);

            // 减少库存（使用乐观锁防止超卖）
            int updatedRows = productMapper.decrementStock(productId, quantity);
            if (updatedRows == 0) {
                throw new RuntimeException("商品库存不足或并发冲突，请重试");
            }
            log.info("库存扣减成功，productId={}, quantity={}, 剩余库存={}", productId, quantity, product.getStock() - quantity);

            // 创建积分消耗记录
            PointsRecord pointsRecord = new PointsRecord();
            pointsRecord.setUserId(userId);
            pointsRecord.setType("spend");
            pointsRecord.setPoints(requiredPoints);
            pointsRecord.setDescription("兑换：" + product.getName());
            pointsRecord.setSource("exchange");
            pointsRecord.setCreateTime(LocalDateTime.now());
            pointsRecordService.addRecord(pointsRecord);
            
            // 确保积分记录ID已生成
            if (pointsRecord.getId() == null) {
                return Result.error("积分记录创建失败");
            }
            
            // 处理地址信息（如果传递了地址）
            if (requestData.containsKey("addressId") && requestData.get("addressId") != null) {
                // 如果传递了地址ID，将其设置为默认地址
                try {
                    Long addressId = null;
                    if (requestData.get("addressId") instanceof Number) {
                        addressId = ((Number) requestData.get("addressId")).longValue();
                    } else {
                        addressId = Long.parseLong(requestData.get("addressId").toString());
                    }
                    if (addressId != null) {
                        addressService.setDefaultAddress(userId, addressId);
                    }
                } catch (Exception e) {
                    log.warn("设置默认地址失败: {}", e.getMessage());
                }
            } else if (requestData.containsKey("address")) {
                // 如果传递了地址对象，创建或更新地址
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> addressData = (Map<String, Object>) requestData.get("address");
                    Address address = new Address();
                    address.setUserId(userId);
                    if (addressData.get("name") != null) {
                        address.setContactName(addressData.get("name").toString());
                    }
                    if (addressData.get("phone") != null) {
                        address.setContactPhone(addressData.get("phone").toString());
                    }
                    if (addressData.get("province") != null) {
                        address.setProvince(addressData.get("province").toString());
                    }
                    if (addressData.get("city") != null) {
                        address.setCity(addressData.get("city").toString());
                    }
                    if (addressData.get("district") != null) {
                        address.setDistrict(addressData.get("district").toString());
                    }
                    if (addressData.get("detail") != null) {
                        address.setDetail(addressData.get("detail").toString());
                    }
                    address.setIsDefault(true); // 设置为默认地址
                    addressService.createAddress(address);
                } catch (Exception e) {
                    log.warn("创建地址失败: {}", e.getMessage());
                }
            }
            
            // 创建订单记录（积分兑换订单，状态为待发货）
            Order createdOrder = null;
            try {
                // 与普通商品订单一致：优先使用本次请求中的收货人/地址（确认页已选），不要仅依赖「创建地址后 getDefault 再查」避免不同步
                Address orderAddress = resolveShippingAddressForExchange(userId, requestData);

                Order order = new Order();
                order.setOrderNo("EX" + pointsRecord.getId()); // 使用积分记录ID作为订单号
                order.setUserId(userId);
                // 订单金额存积分值（g），用于后台“订单金额/退款金额”展示
                order.setTotalAmount(BigDecimal.valueOf(requiredPoints));
                order.setStatus(1); // 1=已付款/待发货（等待管理员发货）
                order.setPaymentStatus(1); // 已支付（积分支付）
                order.setDeliveryStatus(0); // 未发货
                if (orderAddress != null) {
                    order.setReceiverName(orderAddress.getContactName());
                    order.setReceiverPhone(orderAddress.getContactPhone());
                    order.setReceiverProvince(orderAddress.getProvince());
                    order.setReceiverCity(orderAddress.getCity());
                    order.setReceiverRegion(orderAddress.getDistrict());
                    order.setReceiverDetailAddress(orderAddress.getDetail());
                }
                order.setCreateTime(LocalDateTime.now());
                order.setUpdateTime(LocalDateTime.now());
                
                createdOrder = orderService.createOrder(order);
                
                if (createdOrder == null || createdOrder.getId() == null) {
                    log.error("积分兑换订单创建失败：createdOrder为null或ID为null");
                    return Result.error("订单创建失败");
                }
                
                appointmentDelayService.registerOrderCancelKey(createdOrder.getId(), 30);
                
                log.info("积分兑换订单创建成功, orderNo={}, orderId={}", createdOrder.getOrderNo(), createdOrder.getId());
                
                // 创建订单商品详情
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(createdOrder.getId());
                orderItem.setProductId(productId);
                orderItem.setProductName(product.getName());
                orderItem.setProductImage(product.getImage());
                // 商品单价存单件积分（g），便于后台订单表展示“商品单价 × 数量”
                orderItem.setPrice(BigDecimal.valueOf(unitPoints));
                orderItem.setQuantity(quantity);
                orderItem.setSpecName("积分兑换");
                orderItemService.saveOrderItem(orderItem);
                
                log.debug("订单商品详情创建成功");
            } catch (Exception e) {
                log.error("创建积分兑换订单失败", e);
                return Result.error("订单创建失败: " + e.getMessage());
            }

            // 计算该商品的兑换次数（通过description匹配）
            QueryWrapper<PointsRecord> exchangeQuery = new QueryWrapper<>();
            exchangeQuery.eq("type", "spend")
                        .like("description", "兑换商品: " + product.getName());
            int exchangedCount = pointsRecordMapper.selectCount(exchangeQuery).intValue();
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("newBalance", newPoints);
            result.put("productId", productId);
            result.put("productName", product.getName());
            result.put("exchangedCount", exchangedCount);
            if (createdOrder != null) {
                result.put("orderId", createdOrder.getId());
                result.put("orderNo", createdOrder.getOrderNo());
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("积分兑换失败", e);
            return Result.error("兑换失败: " + e.getMessage());
        }
    }

    /**
     * 获取兑换记录
     */
    @GetMapping("/exchange/history")
    @ApiOperation("获取兑换记录")
    public Result<Map<String, Object>> getExchangeHistory(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "userId", required = false) Long userIdFromQuery,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            // 用户ID：优先 Token，其次与订单列表一致的 userId 查询参数（小程序端部分场景未带有效 Bearer）
            Long userId = getUserIdFromToken(token);
            if (userId == null && userIdFromQuery != null) {
                userId = userIdFromQuery;
            }
            if (userId == null) {
                Map<String, Object> empty = new HashMap<>();
                empty.put("records", new ArrayList<>());
                empty.put("total", 0);
                empty.put("page", page);
                empty.put("size", size);
                return Result.success(empty);
            }

            // 查询兑换记录（通过description判断，因为source字段不在数据库中）
            List<PointsRecord> records = pointsRecordService.getSpendRecords(userId);
            
            // 过滤出兑换记录（description以"兑换商品:"开头）
            List<PointsRecord> exchangeRecords = new ArrayList<>();
            for (PointsRecord record : records) {
                String description = record.getDescription();
                // 兼容历史数据「兑换商品:」与当前写入「兑换：」两种描述前缀
                if (description != null
                        && (description.startsWith("兑换商品:") || description.startsWith("兑换："))) {
                    exchangeRecords.add(record);
                }
            }

            // 分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, exchangeRecords.size());
            List<PointsRecord> pagedRecords = exchangeRecords.subList(Math.min(start, exchangeRecords.size()), end);

            // 转换为前端格式
            List<Map<String, Object>> recordList = new ArrayList<>();
            for (PointsRecord record : pagedRecords) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("id", record.getId());
                recordMap.put("description", record.getDescription());
                recordMap.put("points", record.getPoints());
                recordMap.put("createTime", record.getCreateTime());
                recordList.add(recordMap);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("records", recordList);
            result.put("total", exchangeRecords.size());
            result.put("page", page);
            result.put("size", size);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取兑换记录失败", e);
            return Result.error("获取兑换记录失败: " + e.getMessage());
        }
    }

    private static String firstNonEmptyString(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object v : values) {
            if (v == null) {
                continue;
            }
            String s = String.valueOf(v).trim();
            if (s.isEmpty() || "null".equalsIgnoreCase(s)) {
                continue;
            }
            return s;
        }
        return null;
    }

    private static Long parseLongParam(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        try {
            String t = o.toString().trim();
            if (t.isEmpty()) {
                return null;
            }
            return Long.parseLong(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean hasUsableAddressFields(Address a) {
        if (a == null) {
            return false;
        }
        return firstNonEmptyString(a.getContactName()) != null
                || firstNonEmptyString(a.getContactPhone()) != null
                || firstNonEmptyString(a.getProvince(), a.getCity(), a.getDistrict(), a.getDetail()) != null;
    }

    private Address buildAddressFromExchangeRequestMap(Map<String, Object> m, Long userId) {
        if (m == null || m.isEmpty()) {
            return null;
        }
        Address a = new Address();
        a.setUserId(userId);
        a.setContactName(firstNonEmptyString(m.get("name"), m.get("contactName")));
        a.setContactPhone(firstNonEmptyString(m.get("phone"), m.get("contactPhone"), m.get("mobile")));
        a.setProvince(firstNonEmptyString(m.get("province")));
        a.setCity(firstNonEmptyString(m.get("city")));
        a.setDistrict(firstNonEmptyString(m.get("district"), m.get("region")));
        a.setDetail(firstNonEmptyString(m.get("detail"), m.get("fullAddress"), m.get("addressDetail")));
        return a;
    }

    /**
     * 与小程序确认页一致：先 addressId，再本次提交的 address 对象/扁平 receiver 字段，最后默认地址
     */
    private Address resolveShippingAddressForExchange(Long userId, Map<String, Object> request) {
        Long addressId = parseLongParam(request.get("addressId"));
        if (addressId != null) {
            Address fromDb = addressService.getAddressById(addressId);
            if (fromDb != null) {
                return fromDb;
            }
        }
        Object addrObj = request.get("address");
        if (addrObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) addrObj;
            Address fromBody = buildAddressFromExchangeRequestMap(m, userId);
            if (hasUsableAddressFields(fromBody)) {
                return fromBody;
            }
        }
        if (request.get("receiverName") != null || request.get("receiverPhone") != null
                || request.get("receiverDetailAddress") != null) {
            Address a = new Address();
            a.setUserId(userId);
            a.setContactName(firstNonEmptyString(request.get("receiverName")));
            a.setContactPhone(firstNonEmptyString(request.get("receiverPhone")));
            a.setProvince(firstNonEmptyString(request.get("receiverProvince")));
            a.setCity(firstNonEmptyString(request.get("receiverCity")));
            a.setDistrict(firstNonEmptyString(request.get("receiverRegion")));
            a.setDetail(firstNonEmptyString(request.get("receiverDetailAddress")));
            if (hasUsableAddressFields(a)) {
                return a;
            }
        }
        return addressService.getDefaultAddress(userId);
    }

    /**
     * 从token中获取用户ID（与 UserController 一致：先查 Redis 再解析 JWT）
     */
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        if (token.startsWith("token_")) {
            String[] parts = token.split("_", 3);
            if (parts.length >= 2) {
                try {
                    Long uid = Long.parseLong(parts[1]);
                    User u = userService.getUserById(uid);
                    if (u != null && (u.getStatus() == null || u.getStatus() == 1)) {
                        return uid;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        try {
            User cachedUser = userService.getUserByToken(token);
            if (cachedUser != null) {
                return cachedUser.getId();
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            String subject = jwtUtil.getUsernameFromToken(token);
            if (subject != null) {
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
            log.warn("解析收货地址失败", e);
        }
        return null;
    }
}

