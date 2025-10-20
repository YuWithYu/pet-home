import request from "@/utils/request";

/**
 * 预约管理API
 */
export default {
  /**
   * 统一后端基础路径：对齐 AppointmentController 映射 /api/door-cleaning
   */
  base: "/door-cleaning",
  /**
   * 创建预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createAppointment(data) {
    return request({
      url: `${this.base}/create`,
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
      url: `${this.base}/${id}`,
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
    // 后端未提供物理删除接口，这里用更新状态为 cancelled 代替
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "cancelled" },
    });
  },

  /**
   * 获取预约详情
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getAppointmentById(id) {
    return request({
      url: `${this.base}/${id}`,
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
      url: `${this.base}/list`,
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
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "confirmed", operatorId },
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
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "cancelled", operatorId, reason },
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
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "completed", operatorId },
    });
  },

  /**
   * 获取可用时间段
   * @param {String} date 日期
   * @returns {Promise}
   */
  getAvailableTimeSlots(date) {
    return request({
      // 对齐后端时间段控制器 /api/time-slots
      url: `/time-slots/available`,
      method: "get",
      params: { date },
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
      url: `/time-slots/available`,
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
      // 暂无对应后端，返回空成功，前端自行统计
      url: `/orders/list`,
      method: "get",
      params: { date },
    });
  },

  /**
   * 获取即将到来的预约
   * @param {Number} days 天数
   * @returns {Promise}
   */
  getUpcomingAppointments(days = 3) {
    return request({
      url: `/orders/list`,
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
      url: `/orders/list`,
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
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 获取服务类型统计
   * @returns {Promise}
   */
  getServiceTypeStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: [] });
  },

  /**
   * 获取预约状态统计
   * @returns {Promise}
   */
  getAppointmentStatusStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: {} });
  },
};
