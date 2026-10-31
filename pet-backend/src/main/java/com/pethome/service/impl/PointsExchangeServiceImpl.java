package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Address;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.Product;
import com.pethome.entity.User;
import com.pethome.mapper.PointsRecordMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.mapper.UserMapper;
import com.pethome.service.AddressService;
import com.pethome.service.AppointmentDelayService;
import com.pethome.service.OrderItemService;
import com.pethome.service.OrderService;
import com.pethome.service.PointsExchangeService;
import com.pethome.service.PointsRecordService;
import com.pethome.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsExchangeServiceImpl implements PointsExchangeService {

    private static final Logger log = LoggerFactory.getLogger(PointsExchangeServiceImpl.class);

    /** 积分商品所属分类 */
    private static final String POINTS_CATEGORY = "积分商城";
    /** 兑换订单创建后自动取消的分钟数 */
    private static final int ORDER_CANCEL_MINUTES = 30;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Value("${upload.http-url:http://localhost:8080/upload/}")
    private String uploadHttpUrl;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private PointsRecordService pointsRecordService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private AppointmentDelayService appointmentDelayService;

    @Override
    public Map<String, Object> listMallProducts() {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category", POINTS_CATEGORY)
                .eq("status", 1) // 只查询上架的商品
                .orderByDesc("create_time");

        List<Product> products = productMapper.selectList(queryWrapper);
        log.debug("查询积分商城商品: category={}, status=1, count={}", POINTS_CATEGORY,
                products != null ? products.size() : 0);

        List<PointsRecord> allExchangeRecords = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            // 一次取全部兑换流水，在内存中按描述匹配各商品兑换次数（避免逐商品 COUNT）
            QueryWrapper<PointsRecord> exchangeQuery = new QueryWrapper<>();
            exchangeQuery.eq("type", "spend")
                    .like("description", "兑换商品:")
                    .orderByDesc("create_time");
            List<PointsRecord> rows = pointsRecordMapper.selectList(exchangeQuery);
            if (rows != null) {
                allExchangeRecords = rows;
            }
        }

        List<Map<String, Object>> productList = new ArrayList<>();
        if (products != null) {
            for (Product product : products) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", product.getId());
                productMap.put("title", product.getName());
                productMap.put("subtitle", product.getFeatures());

                // 处理图片URL
                String imageUrl = product.getImage();
                if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.startsWith("http")) {
                    String base = publicSiteOrigin();
                    imageUrl = base + (imageUrl.startsWith("/") ? imageUrl : "/" + imageUrl);
                }
                productMap.put("image", imageUrl);

                // 价格转换为kg（前端显示的是kg，后端存储的是g）
                int priceInKg = 0;
                if (product.getPrice() != null) {
                    try {
                        priceInKg = product.getPrice().divide(new BigDecimal("1000")).intValue();
                    } catch (Exception e) {
                        priceInKg = product.getPrice().intValue() / 1000;
                    }
                }
                productMap.put("price", priceInKg);

                productMap.put("status", (product.getStock() != null && product.getStock() > 0) ? "available" : "ended");

                // 统计该商品的兑换次数（通过 description 匹配）
                int exchangedCount = 0;
                String productName = product.getName();
                for (PointsRecord record : allExchangeRecords) {
                    String desc = record.getDescription();
                    if (desc != null && desc.startsWith("兑换商品:") && desc.contains(productName)) {
                        exchangedCount++;
                    }
                }
                productMap.put("exchangedCount", exchangedCount);
                productList.add(productMap);

                log.debug("积分商品: name={}, price={}kg, stock={}, exchanged={}",
                        product.getName(), priceInKg, product.getStock(), exchangedCount);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("products", productList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> exchange(Long userId, Map<String, Object> requestData) {
        Long productId = parseLongParam(requestData.get("productId"));
        if (productId == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        // 兑换数量（默认1，最小1）
        int quantity = 1;
        if (requestData.get("quantity") != null) {
            try {
                quantity = Integer.parseInt(String.valueOf(requestData.get("quantity")));
            } catch (Exception ignored) {
                quantity = 1;
            }
        }
        if (quantity < 1) {
            quantity = 1;
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (!POINTS_CATEGORY.equals(product.getCategory())) {
            throw new IllegalArgumentException("该商品不属于积分商城");
        }
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new IllegalArgumentException("商品已下架");
        }
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new IllegalArgumentException("商品库存不足");
        }

        // 商品价格为单件积分（单位g）
        int unitPoints = product.getPrice() != null ? product.getPrice().intValue() : 0;
        int requiredPoints = unitPoints * quantity;

        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        int userPoints = user.getPoints() != null ? user.getPoints() : 0;
        if (userPoints < requiredPoints) {
            throw new IllegalArgumentException("积分不足，需要 " + requiredPoints + "g，当前有 " + userPoints + "g");
        }

        // 原子 UPDATE 扣积分，避免并发下扣成负数/丢失更新
        int deducted = userMapper.decrementPoints(userId, requiredPoints);
        if (deducted == 0) {
            throw new IllegalArgumentException("积分不足或并发冲突，请重试");
        }
        int newPoints = userPoints - requiredPoints;

        // 乐观锁扣库存，防止超卖
        int updatedRows = productMapper.decrementStock(productId, quantity);
        if (updatedRows == 0) {
            throw new IllegalArgumentException("商品库存不足或并发冲突，请重试");
        }
        log.info("库存扣减成功，productId={}, quantity={}, 剩余库存={}",
                productId, quantity, product.getStock() - quantity);

        // 积分消耗流水
        PointsRecord pointsRecord = new PointsRecord();
        pointsRecord.setUserId(userId);
        pointsRecord.setType("spend");
        pointsRecord.setPoints(requiredPoints);
        pointsRecord.setDescription("兑换：" + product.getName());
        pointsRecord.setSource("exchange");
        pointsRecord.setCreateTime(LocalDateTime.now());
        pointsRecordService.addRecord(pointsRecord);
        if (pointsRecord.getId() == null) {
            log.error("积分记录创建失败：pointsRecord.id 为空");
            throw new IllegalStateException("积分记录创建失败，积分与库存已回滚");
        }

        // 地址处理（可选）
        applyAddressFromRequest(userId, requestData);

        // 创建积分兑换订单（待发货）
        Order createdOrder;
        try {
            Address orderAddress = resolveShippingAddressForExchange(userId, requestData);

            Order order = new Order();
            order.setOrderNo("EX" + pointsRecord.getId());
            order.setUserId(userId);
            // 订单金额存积分值（g），用于后台“订单金额/退款金额”展示
            order.setTotalAmount(BigDecimal.valueOf(requiredPoints));
            order.setStatus(1);          // 已付款/待发货
            order.setPaymentStatus(1);   // 积分支付
            order.setDeliveryStatus(0);  // 未发货
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
                throw new IllegalStateException("订单创建失败，积分与库存已回滚");
            }

            appointmentDelayService.registerOrderCancelKey(createdOrder.getId(), ORDER_CANCEL_MINUTES);
            log.info("积分兑换订单创建成功, orderNo={}, orderId={}", createdOrder.getOrderNo(), createdOrder.getId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(createdOrder.getId());
            orderItem.setProductId(productId);
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getImage());
            // 商品单价存单件积分（g）
            orderItem.setPrice(BigDecimal.valueOf(unitPoints));
            orderItem.setQuantity(quantity);
            orderItem.setSpecName("积分兑换");
            orderItemService.saveOrderItem(orderItem);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建积分兑换订单失败", e);
            throw new IllegalStateException("订单创建失败，积分与库存已回滚", e);
        }

        // 该商品历史兑换次数
        QueryWrapper<PointsRecord> exchangeQuery = new QueryWrapper<>();
        exchangeQuery.eq("type", "spend").like("description", "兑换商品: " + product.getName());
        int exchangedCount = pointsRecordMapper.selectCount(exchangeQuery).intValue();

        Map<String, Object> result = new HashMap<>();
        result.put("newBalance", newPoints);
        result.put("productId", productId);
        result.put("productName", product.getName());
        result.put("exchangedCount", exchangedCount);
        result.put("orderId", createdOrder.getId());
        result.put("orderNo", createdOrder.getOrderNo());
        return result;
    }

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

    /** 请求里带地址信息时，同步为默认地址 / 新建地址（失败不影响兑换主流程） */
    private void applyAddressFromRequest(Long userId, Map<String, Object> requestData) {
        if (requestData.containsKey("addressId") && requestData.get("addressId") != null) {
            try {
                Long addressId = parseLongParam(requestData.get("addressId"));
                if (addressId != null) {
                    addressService.setDefaultAddress(userId, addressId);
                }
            } catch (Exception e) {
                log.warn("设置默认地址失败: {}", e.getMessage());
            }
        } else if (requestData.containsKey("address")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> addressData = (Map<String, Object>) requestData.get("address");
                Address address = buildAddressFromExchangeRequestMap(addressData, userId);
                if (address == null) {
                    return;
                }
                address.setIsDefault(true);
                addressService.createAddress(address);
            } catch (Exception e) {
                log.warn("创建地址失败: {}", e.getMessage());
            }
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

    /** 与小程序确认页一致：先 addressId，再本次提交的 address 对象/扁平 receiver 字段，最后默认地址 */
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
}
