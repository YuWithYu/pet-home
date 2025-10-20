import request from "@/utils/request";

/**
 * 上门铲屎服务预约管理API
 */
export default {
  /**
   * 分页查询上门铲屎预约列表
   * @param {Object} query 查询条件
   * @returns {Promise}
   */
  getDoorCleaningAppointmentList(query) {
    return request({
      url: "/door-cleaning/list",
      method: "post",
      data: query,
    });
  },

  /**
   * 获取上门铲屎预约详情
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  getDoorCleaningAppointmentById(id) {
    return request({
      url: `/door-cleaning/${id}`,
      method: "get",
    });
  },

  /**
   * 创建上门铲屎预约
   * @param {Object} data 预约数据
   * @returns {Promise}
   */
  createDoorCleaningAppointment(data) {
    return request({
      url: "/door-cleaning/create",
      method: "post",
      data: data,
    });
  },

  /**
   * 更新上门铲屎预约状态
   * @param {Number} id 预约ID
   * @param {String} status 状态
   * @returns {Promise}
   */
  updateDoorCleaningAppointmentStatus(id, status) {
    return request({
      url: `/door-cleaning/${id}/status?status=${status}`,
      method: "put",
    });
  },

  /**
   * 删除上门铲屎预约
   * @param {Number} id 预约ID
   * @returns {Promise}
   */
  deleteDoorCleaningAppointment(id) {
    return request({
      url: `/door-cleaning/${id}`,
      method: "delete",
    });
  },

  /**
   * 核销上门铲屎预约
   * @param {String} verifyCode 核销码
   * @returns {Promise}
   */
  verifyDoorCleaningAppointment(verifyCode) {
    return request({
      url: "/door-cleaning/verify",
      method: "post",
      data: { verifyCode },
    });
  },
};
