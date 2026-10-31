package com.pethome.service.order;

import java.util.Map;

/**
 * 订单物流应用服务：物流详情查询与发货信息维护。
 */
public interface OrderLogisticsAppService {

    /**
     * 获取订单物流详情。
     *
     * @param adminSide     是否管理端（非管理端仅限订单本人）
     * @param currentUserId 当前登录用户 ID
     * @param currentStoreId 店铺管理员所属店铺 ID
     */
    Map<String, Object> getOrderLogistics(String orderId, boolean adminSide,
                                          Long currentUserId, Long currentStoreId);

    /**
     * 管理员更新订单物流/收货信息，必要时推进到「已发货」。
     */
    boolean updateOrderLogistics(String orderId, Map<String, Object> logisticsData,
                                 boolean adminSide, Long currentStoreId);
}
