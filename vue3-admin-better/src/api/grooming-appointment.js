import request from "@/utils/request";

/**
 * 宠物洗护预约管理API
 */
export default {
  /**
   * 统一后端基础路径：对齐 GroomingAppointmentController 映射 /api/grooming-appointments
   */
  base: "/grooming-appointments",
  
  /**
   * 创建洗护预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createGroomingAppointment(data) {
    return request({
      url: `${this.base}/create`,
      method: "post",
      data,
    });
  },

  /**
   * 更新洗护预约
   * @param {Number} id 预约ID
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  updateGroomingAppointment(id, data) {
    return request({
      url: `${this.base}/${id}`,
      method: "put",
      data,
    });
  },

  /**
   * 删除洗护预约
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  deleteGroomingAppointment(id) {
    // 后端未提供物理删除接口，这里用更新状态为 cancelled 代替
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "cancelled" },
    });
  },

  /**
   * 获取洗护预约详情
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getGroomingAppointmentById(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 分页查询洗护预约列表
   * @param {Object} query 查询条件
   * @returns {Promise}
   */
  getGroomingAppointmentList(query) {
    return request({
      url: `${this.base}/page`,
      method: "get",
      params: query,
    });
  },

  /**
   * 确认洗护预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  confirmGroomingAppointment(id, operatorId) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "confirmed", operatorId },
    });
  },

  /**
   * 商家取消洗护预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @param {String} reason 取消原因
   * @returns {Promise}
   */
  merchantCancelGroomingAppointment(id, operatorId, reason) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "cancelled", operatorId, reason },
    });
  },

  /**
   * 完成洗护预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  completeGroomingAppointment(id, operatorId) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "completed", operatorId },
    });
  },

  /**
   * 获取用户洗护预约列表
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserGroomingAppointments(userId) {
    return request({
      url: `${this.base}/user/list/${userId}`,
      method: "get",
    });
  },

  /**
   * 获取洗护预约历史记录
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getGroomingAppointmentHistory(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 获取洗护服务类型统计
   * @returns {Promise}
   */
  getGroomingServiceTypeStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: [] });
  },

  /**
   * 获取洗护预约状态统计
   * @returns {Promise}
   */
  getGroomingAppointmentStatusStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: {} });
  },
};

