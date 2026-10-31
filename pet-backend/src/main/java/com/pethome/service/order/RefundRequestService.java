package com.pethome.service.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethome.entity.RefundRequest;

import java.util.Collection;
import java.util.List;

/**
 * 退款申请读写服务。
 * <p>
 * 退款单的查询口径（同订单取最新一条）与后台列表的店铺维度过滤集中在此，
 * 表现层不再直接持有 Mapper。
 */
public interface RefundRequestService extends IService<RefundRequest> {

    /** 订单最新一条退款申请 */
    RefundRequest getByOrderId(Long orderId);

    /** 批量订单的退款申请（按订单号升序、创建时间倒序，供列表展示用） */
    List<RefundRequest> listByOrderIds(Collection<Long> orderIds);

    /**
     * 后台退款列表。
     *
     * @param storeId 非空时仅返回该店铺商品的订单退款（店铺数据隔离）
     */
    Page<RefundRequest> pageRefunds(int page, int size, String status, Long storeId);
}
