package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pethome.entity.Order;
import com.pethome.entity.OrderItem;
import com.pethome.entity.PointsRecord;
import com.pethome.entity.Product;
import com.pethome.entity.RefundRequest;
import com.pethome.entity.User;
import com.pethome.mapper.PointsRecordMapper;
import com.pethome.service.OrderItemService;
import com.pethome.service.ProductService;
import com.pethome.service.RefundRequestService;
import com.pethome.service.RefundSettlementService;
import com.pethome.service.UserCouponService;
import com.pethome.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefundSettlementServiceImpl implements RefundSettlementService {

    private static final Logger log = LoggerFactory.getLogger(RefundSettlementServiceImpl.class);

    @Autowired
    private RefundRequestService refundRequestService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private PointsRecordMapper pointsRecordMapper;

    @Autowired(required = false)
    private UserCouponService userCouponService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer approveAndSettle(Order order, RefundRequest refundRequest) {
        // 1) 状态置为已批准
        refundRequest.setStatus("approved");
        refundRequest.setUpdateTime(LocalDateTime.now());
        refundRequestService.updateById(refundRequest);

        boolean isPointsOrder = order.getOrderNo() != null && order.getOrderNo().startsWith("EX");

        // 2) 积分订单：按原兑换扣除的积分返还
        Integer pointsRefunded = 0;
        if (isPointsOrder) {
            pointsRefunded = refundPoints(order, refundRequest);
        } else {
            restoreStock(order);
            // 返券幂等：仅 used 且 order_id 匹配的券会被恢复
            if (order.getCouponId() != null && userCouponService != null) {
                userCouponService.returnByOrder(order.getId(), order.getUserId());
            }
        }
        return pointsRefunded;
    }

    private Integer refundPoints(Order order, RefundRequest refundRequest) {
        if (order.getUserId() == null) {
            throw new IllegalArgumentException("积分退款失败：订单缺少用户信息");
        }
        if (userService == null || pointsRecordMapper == null) {
            throw new IllegalArgumentException("积分退款失败：积分服务不可用");
        }

        String exchangeRef = order.getOrderNo().substring(2);
        int pointsToReturn = 0;
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
            throw new IllegalArgumentException("积分退款失败：未找到可返还的积分数");
        }

        // 防重：存在同订单号的“退款返还”积分记录时不重复返还
        QueryWrapper<PointsRecord> duplicateQw = new QueryWrapper<>();
        duplicateQw.eq("user_id", order.getUserId())
                .eq("type", "earn")
                .like("description", "订单退款返还积分")
                .like("description", order.getOrderNo());
        Long duplicateCount = pointsRecordMapper.selectCount(duplicateQw);
        if (duplicateCount == null || duplicateCount == 0) {
            User user = userService.getUserById(order.getUserId());
            if (user == null) {
                throw new IllegalArgumentException("积分退款失败：用户不存在");
            }
            int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(currentPoints + pointsToReturn);
            userService.updateUser(user);

            PointsRecord earnRecord = new PointsRecord();
            earnRecord.setUserId(order.getUserId());
            earnRecord.setType("earn");
            earnRecord.setPoints(pointsToReturn);
            earnRecord.setDescription("订单退款返还积分（订单号：" + order.getOrderNo() + "）");
            earnRecord.setCreateTime(LocalDateTime.now());
            pointsRecordMapper.insert(earnRecord);
        }
        return pointsToReturn;
    }

    private void restoreStock(Order order) {
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderId(order.getId());
        for (OrderItem item : orderItems) {
            if (item.getProductId() != null) {
                Product product = productService.getProductById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productService.updateProduct(product);
                    log.info("退款恢复库存: orderId={}, productId={}, quantity={}",
                            order.getId(), item.getProductId(), item.getQuantity());
                }
            }
        }
    }
}
