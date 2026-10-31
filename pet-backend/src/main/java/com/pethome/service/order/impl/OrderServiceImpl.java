package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.common.BusinessException;
import com.pethome.entity.Address;
import com.pethome.entity.Notification;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.Product;
import com.pethome.mapper.AddressMapper;
import com.pethome.mapper.OrderMapper;
import com.pethome.mapper.ProductMapper;
import com.pethome.mq.MqOutboxService;
import com.pethome.service.community.NotificationService;
import com.pethome.service.order.OrderService;
import com.pethome.service.order.OrderItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    /** 订单状态常量 */
    private static final int STATUS_PENDING_PAY   = 0;   // 待付款
    private static final int STATUS_PAID         = 1;   // 已付款（待发货）
    private static final int STATUS_SHIPPED      = 2;   // 已发货（待收货）
    private static final int STATUS_COMPLETED    = 3;   // 已完成
    private static final int STATUS_CANCELLED    = -1;  // 已取消
    private static final int STATUS_DELETED      = -2;  // 已删除（不在用户视图展示）

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private AddressMapper addressMapper;
    @Autowired(required = false)
    private NotificationService notificationService;
    @Autowired(required = false)
    private OrderItemService orderItemService;
    @Autowired(required = false)
    private com.pethome.service.order.UserCouponService userCouponService;

    /** MQ 未启用时该 Provider 取不到 Bean，下单链路自动降级为定时任务兜底 */
    @Autowired
    private ObjectProvider<MqOutboxService> mqOutboxProvider;

    @Override
    public IPage<Order> getOrderList(Page<Order> page) {
        return getOrderList(page, null);
    }

    @Override
    public IPage<Order> getOrderList(Page<Order> page, String orderCategory) {
        return getOrderList(page, orderCategory, null);
    }

    @Override
    public IPage<Order> getOrderList(Page<Order> page, String orderCategory, Integer statusCode) {
        // 安全修复：本方法仅供管理端调用（管理端展示所有订单）。
        // C 端必须改用 getOrderListForUser，避免任意用户查看所有订单。
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.ne(Order::getStatus, STATUS_DELETED);
        if (statusCode != null) {
            // -1=已取消（4 为历史遗留值，一并归入已取消）
            if (statusCode == -1) {
                qw.and(w -> w.eq(Order::getStatus, -1).or().eq(Order::getStatus, 4));
            } else {
                qw.eq(Order::getStatus, statusCode);
            }
        }
        if ("mall".equals(orderCategory)) {
            qw.and(w -> w.isNull(Order::getOrderNo).or().notLike(Order::getOrderNo, "EX%"));
        } else if ("points".equals(orderCategory)) {
            qw.likeRight(Order::getOrderNo, "EX");
        }
        qw.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, qw);
    }

    @Override
    public IPage<Order> getOrderListForUser(Page<Order> page, Long userId) {
        return getOrderListForUser(page, userId, null);
    }

    @Override
    public IPage<Order> getOrderListForUser(Page<Order> page, Long userId, Integer statusCode) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<>();
        qw.eq(Order::getUserId, userId);
        qw.ne(Order::getStatus, STATUS_DELETED);
        if (statusCode != null) {
            if (statusCode == -1) {
                qw.and(w -> w.eq(Order::getStatus, -1).or().eq(Order::getStatus, 4));
            } else {
                qw.eq(Order::getStatus, statusCode);
            }
        }
        qw.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, qw);
    }

    @Override
    public Order createOrder(Order order) {
        // 简单插入：仅供内部调用（如积分兑换）。C 端下单请走 createOrderWithStockCheck。
        orderMapper.insert(order);
        return order;
    }

    /**
     * 安全创建订单：身份校验 + 金额重新计算 + 库存扣减 + 事务保证。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrderWithStockCheck(Long userId, List<OrderItem> items, Long addressId) {
        return createOrderWithStockCheck(userId, items, addressId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrderWithStockCheck(Long userId, List<OrderItem> items, Long addressId, Long userCouponId) {
        if (userId == null) {
            throw new BusinessException(401, "用户未登录");
        }
        if (items == null || items.isEmpty()) {
            throw new BusinessException("订单商品项不能为空");
        }

        // 1. 收货地址校验：必须属于当前用户
        Address address = null;
        if (addressId != null) {
            address = addressMapper.selectById(addressId);
            if (address == null) {
                throw new BusinessException("收货地址不存在");
            }
            if (!userId.equals(address.getUserId())) {
                // 越权使用他人地址
                log.warn("用户尝试使用他人地址下单: userId={}, addressId={}, addressOwner={}",
                        userId, addressId, address.getUserId());
                throw new BusinessException(403, "无权使用该收货地址");
            }
        }

        // 2. 批量查询商品真实信息（单次 selectBatchIds 替代逐项 selectById，消除下单路径 N+1）
        List<Long> productIds = new ArrayList<>();
        for (OrderItem item : items) {
            if (item.getProductId() == null) {
                throw new BusinessException("商品ID缺失");
            }
            productIds.add(item.getProductId());
        }
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        // 3. 重新计算订单金额（不信任前端传入的价格），并扣库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            if (quantity <= 0) {
                throw new BusinessException("商品数量必须大于0");
            }

            // 从批量查询结果获取真实价格（防止前端篡改单价）
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在: " + item.getProductId());
            }
            if (product.getStatus() != null && product.getStatus() != 1) {
                throw new BusinessException("商品已下架: " + product.getName());
            }

            // 单价以后端为准：规格商品按 product.specs 解析出的规格价，
            // 规格未配置价格时回落商品价；前端传入的 specPrice 仅作参考比对（防篡改，见 resolveUnitPrice）
            BigDecimal unitPrice = resolveUnitPrice(product, item.getSpecName(), item.getSpecPrice());
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("商品价格异常: " + product.getName());
            }

            // 扣库存（乐观锁：stock >= quantity 才扣减；返回 0 表示库存不足）
            int affected = productMapper.decrementStock(product.getId(), quantity);
            if (affected == 0) {
                throw new BusinessException("库存不足: " + product.getName());
            }

            // 回填订单项字段（防止前端伪造商品名/图片/价格）
            item.setProductName(product.getName());
            item.setProductImage(product.getImage());
            item.setPrice(unitPrice);
            BigDecimal itemTotal = unitPrice.multiply(new BigDecimal(quantity));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 4. 优惠券校验与优惠计算（金额/门槛/范围以后端计算为准，事务内先校验后核销）
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (userCouponId != null) {
            if (userCouponService == null) {
                throw new BusinessException("优惠券服务不可用");
            }
            couponDiscount = userCouponService.validateAndCalcDiscount(
                    userCouponId, userId, productIds, totalAmount);
        }

        // 5. 创建订单主表（totalAmount 存优惠后应付金额，优惠额单独记录以便财务追溯）
        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setUserId(userId);
        BigDecimal payableAmount = totalAmount.subtract(couponDiscount);
        order.setTotalAmount(payableAmount);
        if (userCouponId != null) {
            order.setCouponId(userCouponId);
            order.setCouponDiscount(couponDiscount);
        }
        order.setStatus(STATUS_PENDING_PAY);
        order.setPaymentStatus(0);
        order.setDeliveryStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        if (address != null) {
            order.setReceiverName(address.getContactName());
            order.setReceiverPhone(address.getContactPhone());
            order.setReceiverProvince(address.getProvince());
            order.setReceiverCity(address.getCity());
            order.setReceiverRegion(address.getDistrict());
            order.setReceiverDetailAddress(address.getDetail());
        }
        orderMapper.insert(order);

        // 5.1 本地消息表入队（与订单落库同一事务）：MQ 投递失败由补偿任务重试，
        //     MQ 未启用时降级为 OrderAutoCancelTask / Redis 时间驱动兜底
        MqOutboxService outbox = mqOutboxProvider.getIfAvailable();
        if (outbox != null) {
            outbox.enqueueOrderTimeout(order);
        }

        // 6. 批量写订单项（单条 foreach INSERT 替代循环单条 insert）
        if (orderItemService != null) {
            for (OrderItem item : items) {
                item.setOrderId(order.getId());
            }
            orderItemService.saveOrderItems(items);
        } else {
            log.error("OrderItemService 未注入，订单项无法保存，订单将回滚: orderNo={}", order.getOrderNo());
            throw new BusinessException("订单项保存失败");
        }

        // 7. 核销优惠券（条件更新防重复核销；与订单创建同一事务，任一失败整体回滚）
        if (userCouponId != null) {
            userCouponService.markUsed(userCouponId, userId, order.getId());
        }

        log.info("订单创建成功: orderNo={}, userId={}, total={}, payable={}, couponDiscount={}, itemCount={}",
                order.getOrderNo(), userId, totalAmount, payableAmount, couponDiscount, items.size());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order updateOrderStatus(Long id, Integer status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return null;
        }
        Integer oldStatus = order.getStatus();

        // 状态机校验：仅允许合法的状态跳转，拒绝任意状态变更
        validateStatusTransition(oldStatus, status);

        // 条件更新（WHERE status=旧状态）：并发竞争下仅一个调用胜出，
        // 库存恢复/返券/通知等副作用恰好执行一次（updateById 全字段覆盖无法保证这一点）
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
        int affected = orderMapper.update(null, conditionUpdate);
        if (affected == 0) {
            // 并发下状态已被其他调用变更：已达目标状态视为幂等成功，否则拒绝
            Order fresh = orderMapper.selectById(id);
            if (fresh != null && status.equals(fresh.getStatus())) {
                return fresh;
            }
            throw new BusinessException(409, "订单状态已变更，请刷新后重试");
        }

        // 订单取消：恢复库存（库存统一由本方法与下单扣减闭环管理，取消必还）
        // 注意：已付款(1)→取消为退款场景，下单时同样扣过库存，因此 0/1 取消均恢复
        if (status == STATUS_CANCELLED) {
            performCancelSideEffects(id, order);
        }

        // 状态变更通知
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
        order.setStatus(status);
        return order;
    }

    /**
     * 超时/延时取消专用：仅待付款(0)→已取消(-1)。
     * 与 updateOrderStatus(id, -1) 的区别：订单已支付(1)时本方法直接返回 false（不触发退款语义），
     * 防止「支付成功但超时取消 key 恰好过期」误取消已支付订单。
     * 条件更新保证并发下（恰好同时支付成功）取消不会胜出。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPendingOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getStatus() == null || order.getStatus() != STATUS_PENDING_PAY) {
            return false;
        }
        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", id).eq("status", STATUS_PENDING_PAY)
                .set("status", STATUS_CANCELLED)
                .set("update_time", LocalDateTime.now());
        int affected = orderMapper.update(null, uw);
        if (affected == 0) {
            // 并发下已被其他调用变更（如恰好支付成功），放弃取消
            return false;
        }
        log.info("订单超时自动取消: orderId={}, orderNo={}", id, order.getOrderNo());
        performCancelSideEffects(id, order);
        return true;
    }

    /**
     * 取消订单的副作用：恢复库存 + 返还已核销优惠券。
     * 仅由状态竞争胜出方调用（条件更新 affected==1），保证恰好执行一次。
     */
    private void performCancelSideEffects(Long id, Order order) {
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
     * 订单状态机校验
     * 合法跳转：
     *   待付款(0)    → 已付款(1)   【支付回调】
     *   待付款(0)    → 已取消(-1)  【用户取消/超时取消】
     *   已付款(1)    → 已发货(2)   【后台发货】
     *   已付款(1)    → 已取消(-1)  【全额退款】
     *   已发货(2)    → 已完成(3)   【用户确认收货/自动完成】
     * 其他跳转一律拒绝
     */
    private void validateStatusTransition(Integer from, Integer to) {
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

    @Override
    public Order updateOrder(Order order) {
        if (order != null && order.getId() != null) {
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);
        }
        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order getOrderByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    /**
     * 安全解析订单项单价：规格价一律以 DB 中 product.specs 为权威来源，
     * 杜绝「前端改 specPrice=0.01 低价下单」的金额篡改。
     * 定价规则与 ProductController.buildSkuStockList 保持一致：
     * - 商品无规格（specs 空）但请求带 specName：规格不存在，拒绝下单；
     * - specName 拆分后的每个规格值必须能在 specs 的某一组 values 中匹配；
     * - 匹配到的规格值若配置了 price，取后匹配的（与笛卡尔积 SKU 生成时的覆盖顺序一致）；
     * - 所有规格值均未配置 price 时，回落商品价 product.price。
     *
     * @param product       DB 中的商品（含 specs JSON）
     * @param specName      前端传入的规格组合名（如 "红色,XL"）
     * @param frontendPrice 前端传入的规格价（仅用于篡改告警日志，不参与定价）
     */
    private BigDecimal resolveUnitPrice(Product product, String specName, BigDecimal frontendPrice) {
        BigDecimal defaultPrice = product.getPrice();
        String specsStr = product.getSpecs();

        boolean hasSpecName = specName != null && !specName.trim().isEmpty();
        if (specsStr == null || specsStr.trim().isEmpty()) {
            if (hasSpecName) {
                // 商品根本没有规格配置，却按规格下单：非法请求
                throw new BusinessException("商品规格不存在: " + product.getName());
            }
            return defaultPrice;
        }

        com.fasterxml.jackson.databind.JsonNode groups;
        try {
            groups = com.pethome.util.JSONUtil.parseTree(specsStr);
        } catch (Exception e) {
            log.error("解析商品specs失败，拒绝下单: productId={}", product.getId(), e);
            throw new BusinessException("商品规格数据异常，请稍后重试");
        }

        // 格式一：specs 为 {"skus":[{spData,price,promotionPrice,...}]}——按 spData 值集合匹配 SKU
        if (groups != null && groups.isObject()) {
            com.fasterxml.jackson.databind.JsonNode skusNode = groups.get("skus");
            if (skusNode != null && skusNode.isArray() && skusNode.size() > 0) {
                if (!hasSpecName) {
                    return defaultPrice;
                }
                return resolveSkuObjectPrice(product, skusNode, specName);
            }
            // 非法对象结构（无 skus）：视作无规格
            if (hasSpecName) {
                throw new BusinessException("商品规格不存在: " + product.getName());
            }
            return defaultPrice;
        }

        if (groups == null || !groups.isArray() || groups.size() == 0) {
            if (hasSpecName) {
                throw new BusinessException("商品规格不存在: " + product.getName());
            }
            return defaultPrice;
        }

        if (!hasSpecName) {
            // 有规格商品但请求未选规格：无规格价可用，回落商品价（与旧版行为一致）
            return defaultPrice;
        }

        // 收集所有规格值 -> price（同名值以先出现为准；一组内同名值本就属同一选项）
        java.util.Map<String, BigDecimal> priceByValue = new java.util.HashMap<>();
        for (int g = 0; g < groups.size(); g++) {
            com.fasterxml.jackson.databind.JsonNode group = groups.get(g);
            if (group == null || !group.isObject()) {
                continue;
            }
            com.fasterxml.jackson.databind.JsonNode values = group.get("values");
            if (values == null || !values.isArray()) {
                continue;
            }
            for (int v = 0; v < values.size(); v++) {
                com.fasterxml.jackson.databind.JsonNode vo = values.get(v);
                String name;
                BigDecimal price = null;
                if (vo != null && vo.isObject()) {
                    name = vo.hasNonNull("name") ? vo.get("name").asText() : null;
                    com.fasterxml.jackson.databind.JsonNode p = vo.get("price");
                    if (p != null && !p.isNull()) {
                        try {
                            price = p.isNumber() ? p.decimalValue() : new BigDecimal(p.asText());
                        } catch (NumberFormatException ignore) {
                        }
                    }
                } else {
                    name = vo != null ? vo.asText() : null;
                }
                if (name != null && !name.trim().isEmpty() && price != null) {
                    priceByValue.putIfAbsent(name.trim(), price);
                }
            }
        }

        // 校验 specName 并计算权威价：起始为商品价，遇到「配置了价的规格值」则覆盖
        // （与 buildSkuStockList 一致：无价规格值不改变价格，只有带价值覆盖）
        String[] parts = specName.split("[,，]");
        BigDecimal resolved = defaultPrice;
        boolean anyMatched = false;
        for (String part : parts) {
            String val = part == null ? "" : part.trim();
            if (val.isEmpty()) {
                continue;
            }
            BigDecimal p = priceByValue.get(val);
            if (p != null) {
                anyMatched = true;
                resolved = p;
            }
        }
        if (!anyMatched && !priceByValue.isEmpty()) {
            // 商品规格配置了价格，但请求的规格一个都没匹配上：非法请求
            // （规格无价的商品 priceByValue 为空，不会误伤正常「无价规格组合按商品价」的流程）
            throw new BusinessException("商品规格不存在: " + product.getName());
        }

        if (frontendPrice != null && frontendPrice.compareTo(resolved) != 0) {
            log.warn("前端规格价与后端不一致，以后端为准: productId={}, specName={}, frontend={}, backend={}",
                    product.getId(), specName, frontendPrice, resolved);
        }
        return resolved;
    }

    /**
     * specs 为 {"skus":[...]} 对象格式时的安全定价：
     * 将 specName 拆分后的规格值集合与各 SKU 的 spData 值集合匹配（顺序无关），
     * 匹配到的 SKU 价（promotionPrice 优先）为权威价；匹配不上则拒绝下单。
     */
    private BigDecimal resolveSkuObjectPrice(Product product, com.fasterxml.jackson.databind.JsonNode skusNode, String specName) {
        java.util.Set<String> wanted = new java.util.LinkedHashSet<>();
        for (String part : specName.split("[,，]")) {
            String v = part == null ? "" : part.trim();
            if (!v.isEmpty()) {
                wanted.add(v);
            }
        }
        for (int i = 0; i < skusNode.size(); i++) {
            com.fasterxml.jackson.databind.JsonNode sku = skusNode.get(i);
            if (sku == null || !sku.isObject()) {
                continue;
            }
            com.fasterxml.jackson.databind.JsonNode spData = sku.get("spData");
            java.util.Set<String> values = new java.util.LinkedHashSet<>();
            if (spData != null && spData.isArray()) {
                for (int j = 0; j < spData.size(); j++) {
                    com.fasterxml.jackson.databind.JsonNode kv = spData.get(j);
                    if (kv != null && kv.isObject() && kv.hasNonNull("value")) {
                        String v = kv.get("value").asText();
                        if (v != null && !v.trim().isEmpty()) {
                            values.add(v.trim());
                        }
                    }
                }
            }
            if (!wanted.isEmpty() && values.equals(wanted)) {
                // promotionPrice 优先（与前端取价口径一致）
                com.fasterxml.jackson.databind.JsonNode promo = sku.get("promotionPrice");
                if (promo != null && !promo.isNull()) {
                    try {
                        return promo.isNumber() ? promo.decimalValue() : new BigDecimal(promo.asText());
                    } catch (NumberFormatException ignore) {
                    }
                }
                com.fasterxml.jackson.databind.JsonNode price = sku.get("price");
                if (price != null && !price.isNull()) {
                    try {
                        return price.isNumber() ? price.decimalValue() : new BigDecimal(price.asText());
                    } catch (NumberFormatException ignore) {
                    }
                }
                return product.getPrice();
            }
        }
        throw new BusinessException("商品规格不存在: " + product.getName());
    }
}
