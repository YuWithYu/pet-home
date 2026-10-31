package com.pethome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pethome.entity.RefundRequest;
import com.pethome.mapper.RefundRequestMapper;
import com.pethome.service.RefundRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class RefundRequestServiceImpl extends ServiceImpl<RefundRequestMapper, RefundRequest>
        implements RefundRequestService {

    @Autowired
    private RefundRequestMapper refundRequestMapper;

    @Override
    public RefundRequest getByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return refundRequestMapper.selectByOrderId(orderId);
    }

    @Override
    public List<RefundRequest> listByOrderIds(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        return refundRequestMapper.selectByOrderIds(new ArrayList<>(orderIds));
    }

    @Override
    public Page<RefundRequest> pageRefunds(int page, int size, String status, Long storeId) {
        Page<RefundRequest> mpPage = new Page<>(page, size);
        QueryWrapper<RefundRequest> qw = new QueryWrapper<>();
        if (status != null && !status.trim().isEmpty()) {
            qw.eq("status", status.trim());
        }
        if (storeId != null) {
            qw.inSql("order_id",
                    "SELECT DISTINCT oi.order_id FROM order_item oi INNER JOIN product p ON p.id = oi.product_id WHERE p.store_id = "
                            + storeId);
        }
        qw.orderByDesc("create_time");
        refundRequestMapper.selectPage(mpPage, qw);
        return mpPage;
    }
}
