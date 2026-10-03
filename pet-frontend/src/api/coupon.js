import request from "@/utils/request";

/**
 * 优惠券管理 API（后端 /api/admin/coupons/**，仅 admin 可访问）
 */
export const couponApi = {
  /** 分页查询优惠券 */
  getCouponPage(params) {
    return request({
      url: "/admin/coupons/list",
      method: "get",
      params,
    });
  },

  /** 优惠券详情 */
  getCouponDetail(id) {
    return request({
      url: `/admin/coupons/${id}`,
      method: "get",
    });
  },

  /** 创建优惠券（默认草稿态） */
  createCoupon(data) {
    return request({
      url: "/admin/coupons/create",
      method: "post",
      data,
    });
  },

  /** 更新优惠券（已发放的券禁止修改金额规则） */
  updateCoupon(data) {
    return request({
      url: "/admin/coupons/update",
      method: "put",
      data,
    });
  },

  /** 上架/下架 */
  changeCouponStatus(id, status) {
    return request({
      url: `/admin/coupons/${id}/status`,
      method: "put",
      params: { status },
    });
  },

  /** 发放统计 */
  getCouponStats(id) {
    return request({
      url: `/admin/coupons/${id}/stats`,
      method: "get",
    });
  },

  /** 删除优惠券（仅零领取记录可删） */
  deleteCoupon(id) {
    return request({
      url: `/admin/coupons/${id}`,
      method: "delete",
    });
  },
};
