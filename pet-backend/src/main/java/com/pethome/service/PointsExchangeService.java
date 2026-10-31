package com.pethome.service;

import java.util.Map;

/**
 * 积分商城应用服务：商品列表与积分兑换编排。
 * <p>
 * 兑换涉及「扣积分 → 扣库存 → 写积分流水 → 建订单 → 建订单明细」五步，
 * 事务边界必须落在服务层，而不是 Controller。
 */
public interface PointsExchangeService {

    /** 积分商城商品列表（含兑换次数统计与图片地址补全） */
    Map<String, Object> listMallProducts();

    /**
     * 执行积分兑换。
     *
     * @param userId      当前登录用户
     * @param requestData 前端请求体（productId / quantity / addressId / address / receiver*）
     * @return 兑换结果（newBalance / productId / productName / exchangedCount / orderId / orderNo）
     */
    Map<String, Object> exchange(Long userId, Map<String, Object> requestData);
}
