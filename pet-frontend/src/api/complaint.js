import request from "@/utils/request";

/**
 * 投诉举报管理 API（管理员后台）
 */
export const complaintApi = {
  getPage(params) {
    return request({
      url: "/admin/complaints",
      method: "get",
      params,
    });
  },
  getById(id) {
    return request({
      url: `/admin/complaints/${id}`,
      method: "get",
    });
  },
  updateStatus(id, status) {
    return request({
      url: `/admin/complaints/${id}/status`,
      method: "put",
      data: { status },
    });
  },
};
