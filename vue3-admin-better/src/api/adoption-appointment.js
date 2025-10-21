import request from "@/utils/request";

/**
 * 宠物领养预约管理API
 */
export default {
  /**
   * 统一后端基础路径：对齐 AdoptionAppointmentController 映射 /api/adoption-appointments
   */
  base: "/adoption-appointments",
  
  /**
   * 创建领养预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createAdoptionAppointment(data) {
    return request({
      url: `${this.base}/create`,
      method: "post",
      data,
    });
  },

  /**
   * 更新领养预约
   * @param {Number} id 预约ID
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  updateAdoptionAppointment(id, data) {
    return request({
      url: `${this.base}/${id}`,
      method: "put",
      data,
    });
  },

  /**
   * 删除领养预约
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  deleteAdoptionAppointment(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "delete",
    });
  },

  /**
   * 获取领养预约详情
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getAdoptionAppointmentById(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 分页查询领养预约列表
   * @param {Object} query 查询条件
   * @returns {Promise}
   */
  getAdoptionAppointmentList(query) {
    return request({
      url: `${this.base}/page`,
      method: "get",
      params: query,
    });
  },

  /**
   * 审核通过领养预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  approveAdoptionAppointment(id, operatorId) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "approved", operatorId },
    });
  },

  /**
   * 拒绝领养预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @param {String} reason 拒绝原因
   * @returns {Promise}
   */
  rejectAdoptionAppointment(id, operatorId, reason) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "rejected", operatorId, reason },
    });
  },

  /**
   * 完成领养预约
   * @param {Number} id 预约ID
   * @param {Number} operatorId 操作者ID
   * @returns {Promise}
   */
  completeAdoptionAppointment(id, operatorId) {
    return request({
      url: `${this.base}/${id}/status`,
      method: "put",
      params: { status: "completed", operatorId },
    });
  },

  /**
   * 获取用户领养预约列表
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getUserAdoptionAppointments(userId) {
    return request({
      url: `${this.base}/user/list/${userId}`,
      method: "get",
    });
  },

  /**
   * 获取领养预约历史记录
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getAdoptionAppointmentHistory(id) {
    return request({
      url: `${this.base}/${id}`,
      method: "get",
    });
  },

  /**
   * 获取领养预约状态统计
   * @returns {Promise}
   */
  getAdoptionAppointmentStatusStatistics() {
    // 后端暂无统计接口，返回空数据由前端本地计算
    return Promise.resolve({ code: 0, data: {} });
  },
};

