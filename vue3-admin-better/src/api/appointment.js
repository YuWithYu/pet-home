import request from "@/utils/request";

/**
 * 预约管理API
 */
export default {
  /**
   * 创建预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createAppointment(data) {
    return request({
      url: "/appointment/create",
      method: "post",
      data,
    });
  },

  /**
   * 更新预约
   * @param {Number} id 预约ID
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  updateAppointment(id, data) {
    return request({
      url: `/appointment/update/${id}`,
      method: "put",
      data,
    });
  },

  /**
   * 删除预约
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  deleteAppointment(id) {
    return request({
      url: `/appointment/delete/${id}`,
      method: "delete",
    });
  },

  /**
   * 获取预约详情
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getAppointmentById(id) {
    return request({
      url: `/appointment/detail/${id}`,
      method: "get",
    });
  },

  /**
   * 分页查询预约列表
   * @param {Object} query 查询条件
   * @returns {Promise}
   */
  getAppointmentList(query) {
    return request({
      url: "/appointment/list",
      method: "post",
      data: query,
    });
  },

  /**
   * 确认预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  confirmAppointment(id, operatorId) {
    return request({
      url: `/appointment/merchant/confirm/${id}`,
      method: "put",
      params: { operatorId },
    });
  },

  /**
   * 商家取消预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @param {String} reason 取消原因
   * @returns {Promise}
   */
  merchantCancelAppointment(id, operatorId, reason) {
    return request({
      url: `/appointment/merchant/cancel/${id}`,
      method: "put",
      params: { operatorId, reason },
    });
  },

  /**
   * 完成预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  completeAppointment(id, operatorId) {
    return request({
      url: `/appointment/merchant/complete/${id}`,
      method: "put",
      params: { operatorId },
    });
  },

  /**
   * 获取可用时间段
   * @param {String} date 日期
   * @returns {Promise}
   */
  getAvailableTimeSlots(date) {
    return request({
      url: `/appointment/schedule/available/${date}`,
      method: "get",
    });
  },

  /**
   * 检查时间段是否可用
   * @param {String} date 日期
   * @param {String} timeSlot 时间段
   * @returns {Promise}
   */
  isTimeSlotAvailable(date, timeSlot) {
    return request({
      url: "/appointment/schedule/check",
      method: "get",
      params: { date, timeSlot },
    });
  },

  /**
   * 获取日期预约统计
   * @param {String} date 日期
   * @returns {Promise}
   */
  getAppointmentCountByDate(date) {
    return request({
      url: `/appointment/schedule/count/${date}`,
      method: "get",
    });
  },

  /**
   * 获取即将到来的预约
   * @param {Number} days 天数
   * @returns {Promise}
   */
  getUpcomingAppointments(days = 3) {
    return request({
      url: "/appointment/reminder/upcoming",
      method: "get",
      params: { days },
    });
  },

  /**
   * 获取过期的预约
   * @returns {Promise}
   */
  getOverdueAppointments() {
    return request({
      url: "/appointment/reminder/overdue",
      method: "get",
    });
  },

  /**
   * 获取预约历史记录
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getAppointmentHistory(id) {
    return request({
      url: `/appointment/history/${id}`,
      method: "get",
    });
  },

  /**
   * 获取服务类型统计
   * @returns {Promise}
   */
  getServiceTypeStatistics() {
    return request({
      url: "/appointment/statistics/service",
      method: "get",
    });
  },

  /**
   * 获取预约状态统计
   * @returns {Promise}
   */
  getAppointmentStatusStatistics() {
    return request({
      url: "/appointment/statistics/status",
      method: "get",
    });
  },
};
