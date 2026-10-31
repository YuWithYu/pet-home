package com.pethome.service.order;

import java.util.Map;

/**
 * 订单命令应用服务：下单与状态流转。
 * <p>
 * 身份与权限上下文由 Controller 解析后传入；业务校验失败统一抛 {@link com.pethome.common.BusinessException}，
 * 由全局异常出口返回给前端，Controller 不再手写错误分支。
 */
public interface OrderCommandAppService {

    /**
     * 创建商品订单（价格、地址归属、库存、优惠券均由服务端校验）。
     *
     * @param currentUserId 当前登录用户 ID
     * @param orderData     前端请求体（items/addressId/userCouponId）
     * @return 新订单的摘要信息
     */
    Map<String, Object> createOrder(Long currentUserId, Map<String, Object> orderData);

    /**
     * 更新订单状态。
     *
     * @param status         前端状态字符串（pending/paid/shipped/completed/cancelled/deleted）
     * @param platformAdmin  是否平台管理员
     * @param currentStoreId 店铺管理员所属店铺 ID
     * @param userIdFromToken 由 token 解析出的用户 ID
     */
    boolean updateOrderStatus(String orderId, String status, boolean platformAdmin,
                              Long currentStoreId, Long userIdFromToken);
}
