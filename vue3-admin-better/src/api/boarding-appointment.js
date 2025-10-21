import request from "@/utils/request";

/**
 * 宠物寄养预约管理API
 */
export default {
  /**
   * 统一后端基础路径：对齐 BoardingAppointmentController 映射 /api/boarding-appointments
   */
  base: "/boarding-appointments",
  
  /**
   * 创建寄养预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createBoardingAppointment(data) {
    return request({
      url: `${this.base}/create`,
      method: "post",
      data,
    });
  },

  /**
   * 更新寄养预约
   * @param {Number} id 预约ID
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  updateBoardingAppointment(id, data) {
    return request({
      url: `${this.base}/${id}`,
      method: "put",
      data,
    });
  },

  /**
   * 删除寄养预约
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  deleteBoardingAppointment(id) {
    // 后端未提供物理删除接口，这里用更新状态为 cancelled 代替
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "cancelled" },
    });
  },

  /**
   * 获取寄养预约详情
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getBoardingAppointmentById(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 分页查询寄养预约列表
   * @param {Object} query 查询条件
   * @returns {Promise}
   */
  getBoardingAppointmentList(query) {
    return request({
      url: `${this.base}/page`,
      method: "get",
      params: query,
    });
  },

  /**
   * 确认寄养预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  confirmBoardingAppointment(id, operatorId) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "confirmed", operatorId },
    });
  },

  /**
   * 商家取消寄养预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @param {String} reason 取消原因
   * @returns {Promise}
   */
  merchantCancelBoardingAppointment(id, operatorId, reason) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "cancelled", operatorId, reason },
    });
  },

  /**
   * 完成寄养预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  completeBoardingAppointment(id, operatorId) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "completed", operatorId },
    });
  },

  /**
   * 获取用户寄养预约列表
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserBoardingAppointments(userId) {
    return request({
      url: `${this.base}/user/list/${userId}`,
      method: "get",
    });
  },

  /**
   * 获取寄养预约历史记录
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getBoardingAppointmentHistory(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 获取寄养服务类型统计
   * @returns {Promise}
   */
  getBoardingServiceTypeStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: [] });
  },

  /**
   * 获取寄养预约状态统计
   * @returns {Promise}
   */
  getBoardingAppointmentStatusStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: {} });
  },
};

