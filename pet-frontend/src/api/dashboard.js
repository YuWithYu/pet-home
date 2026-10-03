import request from "@/utils/request";

/**
 * 仪表盘统计数据 API
 */
export const dashboardApi = {
  getStatistics() {
    return request({
      url: "/admin/dashboard/statistics",
      method: "get",
    });
  },
};
