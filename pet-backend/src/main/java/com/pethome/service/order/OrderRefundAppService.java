package com.pethome.service.order;

import java.util.Map;

/**
 * 订单退款应用服务：退款申请、取消、列表、审批（通过/驳回）的编排逻辑。
 * <p>
 * 事务边界统一放在实现类，Controller 只负责取当前登录用户/权限并做结果包装。
 */
public interface OrderRefundAppService {

    /**
     * 用户申请退款。
     *
     * @param currentUserId 当前登录用户 ID（用于订单归属校验）
     * @return 前端展示用的退款申请数据
     */
    Map<String, Object> requestRefund(String orderId, Map<String, Object> refundData, Long currentUserId);

    /**
     * 用户取消退款申请。
     *
     * @param currentUserId 当前登录用户 ID（为空时保持原有宽松校验）
     */
    boolean cancelRefund(String orderId, Long currentUserId);

    /**
     * 后台退款申请列表。
     *
     * @param currentStoreId 非空时按店铺维度过滤
     */
    Map<String, Object> getRefundList(int page, int size, String status, Long currentStoreId);

    /**
     * 管理员批准退款申请。
     *
     * @param currentStoreId 非空时校验订单是否包含该店铺商品
     * @param adminRole      当前管理员角色（staff 无权审批）
     */
    Map<String, Object> approveRefund(String orderId, Long currentStoreId, String adminRole);

    /**
     * 管理员拒绝退款申请。
     *
     * @param body 请求体，读取 rejectReason
     */
    Map<String, Object> rejectRefund(String orderId, Map<String, Object> body, Long currentStoreId, String adminRole);
}
