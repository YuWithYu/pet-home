import request from "@/utils/request";

/**
 * 公告管理 API
 */
export const noticeApi = {
  /**
   * 后台分页查询公告
   */
  getNoticePage(params) {
    return request({
      url: "/notice/page",
      method: "get",
      params,
    });
  },

  /**
   * 获取公告列表（首页/看板用）
   */
  getNoticeList(limit = 5, type) {
    const params = { limit };
    if (type) {
      params.type = type;
    }
    return request({
      url: "/notice/list",
      method: "get",
      params,
    });
  },

  /**
   * 创建公告
   */
  createNotice(data) {
    return request({
      url: "/notice/create",
      method: "post",
      data,
    });
  },

  /**
   * 更新公告
   */
  updateNotice(data) {
    return request({
      url: "/notice/update",
      method: "put",
      data,
    });
  },

  /**
   * 删除公告
   */
  deleteNotice(id) {
    return request({
      url: `/notice/${id}`,
      method: "delete",
    });
  },
};

