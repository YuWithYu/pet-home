import request from "@/utils/request";

// 标签管理（与后端 /api/tag 对应）
export const tagApi = {
  // 获取全部标签名（用于发现页标签栏）
  getAllTags() {
    return request({
      url: "/tag/all",
      method: "get",
    });
  },

  // 管理员新增标签
  createTag(data) {
    return request({
      url: "/tag/admin/create",
      method: "post",
      data,
    });
  },

  // 管理员删除标签
  deleteTag(tagName) {
    return request({
      url: `/tag/admin/${encodeURIComponent(tagName)}`,
      method: "delete",
    });
  },
};

