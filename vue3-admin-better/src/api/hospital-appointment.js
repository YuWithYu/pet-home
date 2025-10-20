import request from "@/utils/request";

/**
 * 宠物医院预约管理API
 */
export default {
  /**
   * 统一后端基础路径：对齐 HospitalAppointmentController 映射 /api/hospital-appointments
   */
  base: "/hospital-appointments",
  
  /**
   * 创建医院预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createHospitalAppointment(data) {
    return request({
      url: `${this.base}/create`,
      method: "post",
      data,
    });
  },

  /**
   * 更新医院预约
   * @param {Number} id 预约ID
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  updateHospitalAppointment(id, data) {
    return request({
      url: `${this.base}/${id}`,
      method: "put",
      data,
    });
  },

  /**
   * 删除医院预约
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  deleteHospitalAppointment(id) {
    // 后端未提供物理删除接口，这里用更新状态为 cancelled 代替
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "cancelled" },
    });
  },

  /**
   * 获取医院预约详情
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getHospitalAppointmentById(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 分页查询医院预约列表
   * @param {Object} query 查询条件
   * @returns {Promise}
   */
  getHospitalAppointmentList(query) {
    return request({
      url: `${this.base}/page`,
      method: "get",
      params: query,
    });
  },

  /**
   * 确认医院预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  confirmHospitalAppointment(id, operatorId) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "confirmed", operatorId },
    });
  },

  /**
   * 商家取消医院预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @param {String} reason 取消原因
   * @returns {Promise}
   */
  merchantCancelHospitalAppointment(id, operatorId, reason) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "cancelled", operatorId, reason },
    });
  },

  /**
   * 完成医院预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  completeHospitalAppointment(id, operatorId) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "completed", operatorId },
    });
  },

  /**
   * 获取用户医院预约列表
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserHospitalAppointments(userId) {
    return request({
      url: `${this.base}/user/list/${userId}`,
      method: "get",
    });
  },

  /**
   * 获取医院预约历史记录
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getHospitalAppointmentHistory(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 获取医院服务类型统计
   * @returns {Promise}
   */
  getHospitalServiceTypeStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: [] });
  },

  /**
   * 获取医院预约状态统计
   * @returns {Promise}
   */
  getHospitalAppointmentStatusStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: {} });
  },
};
