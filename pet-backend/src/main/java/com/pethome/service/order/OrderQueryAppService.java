package com.pethome.service.order;

import java.util.Map;

/**
 * 订单查询应用服务：商品订单列表、订单详情、铲屎服务预约订单的编排与视图组装。
 * <p>
 * 身份判定（平台管理员/店铺管理员/普通用户）由 Controller 解析后以参数传入，
 * 服务层只负责数据隔离、聚合与前端字段映射，避免表现层承载业务规则。
 */
public interface OrderQueryAppService {

    /**
     * 铲屎服务预约订单列表（兼容原有前端路径）。
     *
     * @param adminSide     是否管理端（管理端可见全平台，C 端只能看自己）
     * @param currentUserId 当前登录用户 ID，C 端为空时视为未登录
     */
    Map<String, Object> getLitterAppointments(String status, boolean adminSide, Long currentUserId);

    /**
     * 商品订单列表。
     *
     * @param status          前端状态字符串（pending/paid/shipped/completed/cancelled/all）
     * @param userIdParam     前端传入的用户筛选参数（userId/userIdStr，可能为 all/null）
     * @param orderCategory   订单分类，all 视为不筛选
     * @param currentPage     页码，为空表示旧版不分页调用
     * @param pageSize        每页条数，为空表示旧版不分页调用
     * @param platformAdmin   是否平台管理员
     * @param userIdFromToken 由 token 解析出的用户 ID（未登录为空）
     * @param currentStoreId  店铺管理员所属店铺 ID（非店铺端为空）
     */
    Map<String, Object> getOrderList(String status, String userIdParam, String orderCategory,
                                     Integer currentPage, Integer pageSize, boolean platformAdmin,
                                     Long userIdFromToken, Long currentStoreId);

    /**
     * 商品订单详情。
     *
     * @param orderId         订单号或数字 ID
     * @param platformAdmin   是否平台管理员
     * @param currentStoreId  店铺管理员所属店铺 ID
     * @param userIdFromToken 由 token 解析出的用户 ID
     */
    Map<String, Object> getOrderDetail(String orderId, boolean platformAdmin, Long currentStoreId,
                                       Long userIdFromToken);
}
