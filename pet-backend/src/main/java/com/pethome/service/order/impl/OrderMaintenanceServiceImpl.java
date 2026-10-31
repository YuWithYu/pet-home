package com.pethome.service.order.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.pethome.entity.Order;
import com.pethome.mapper.OrderMapper;
import com.pethome.service.order.OrderMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单维护实现。均为单条批量 UPDATE，天然幂等，不声明事务。
 */
@Service
public class OrderMaintenanceServiceImpl implements OrderMaintenanceService {

    private static final int STATUS_PENDING = 0;   // 待付款
    private static final int STATUS_SHIPPED = 2;   // 已发货
    private static final int STATUS_COMPLETED = 3; // 已完成
    private static final int AUTO_COMPLETE_DAYS = 7;
    private static final int BATCH_LIMIT = 500;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public int autoCompleteShippedOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(AUTO_COMPLETE_DAYS);

        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("status", STATUS_SHIPPED)
          .isNotNull("shipping_time")
          .le("shipping_time", deadline)
          .set("status", STATUS_COMPLETED)
          .set("delivery_status", 2)
          .set("update_time", LocalDateTime.now())
          .last("LIMIT " + BATCH_LIMIT);
        return orderMapper.update(null, uw);
    }

    @Override
    public List<Long> findExpiredPendingOrderIds(int autoCancelMinutes, int limit) {
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.select("id")
          .eq("status", STATUS_PENDING)
          .le("create_time", LocalDateTime.now().minusMinutes(autoCancelMinutes))
          .last("LIMIT " + limit);

        List<Long> ids = new ArrayList<>();
        for (Object idObj : orderMapper.selectObjs(qw)) {
            if (idObj instanceof Number) {
                ids.add(((Number) idObj).longValue());
            }
        }
        return ids;
    }
}
