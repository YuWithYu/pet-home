import request from "@/utils/request";

/**
 * 医疗服务管理API
 */
const medicalServiceApi = {
  /**
   * 分页查询医疗服务
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMedicalServiceList(params) {
    return request({
      url: "/medical-services/page",
      method: "get",
      params,
    });
  },

  /**
   * 获取所有启用的医疗服务
   * @returns {Promise}
   */
  getActiveServices() {
    return request({
      url: "/medical-services",
      method: "get",
    });
  },

  /**
   * 根据分类查询医疗服务
   * @param {String} category 分类
   * @returns {Promise}
   */
  getServicesByCategory(category) {
    return request({
      url: `/medical-services/category/${category}`,
      method: "get",
    });
  },

  /**
   * 获取医疗服务详情
   * @param {Number} id 服务ID
   * @returns {Promise}
   */
  getMedicalServiceById(id) {
    return request({
      url: `/medical-services/${id}`,
      method: "get",
    });
  },

  /**
   * 创建医疗服务
   * @param {Object} data 服务数据
   * @returns {Promise}
   */
  createMedicalService(data) {
    return request({
      url: "/medical-services/create",
      method: "post",
      data,
    });
  },

  /**
   * 更新医疗服务
   * @param {Number} id 服务ID
   * @param {Object} data 服务数据
   * @returns {Promise}
   */
  updateMedicalService(id, data) {
    return request({
      url: "/medical-services/update",
      method: "put",
      data: { ...data, id },
    });
  },

  /**
   * 删除医疗服务
   * @param {Number} id 服务ID
   * @returns {Promise}
   */
  deleteMedicalService(id) {
    return request({
      url: `/medical-services/${id}`,
      method: "delete",
    });
  },

  /**
   * 更新医疗服务状态
   * @param {Number} id 服务ID
   * @param {String} status 状态
   * @returns {Promise}
   */
  updateServiceStatus(id, status) {
    return request({
      url: `/medical-services/${id}/status`,
      method: "put",
      params: { status },
    });
  },

  /**
   * 更新医疗服务排序
   * @param {Number} id 服务ID
   * @param {Number} sortOrder 排序
   * @returns {Promise}
   */
  updateServiceSortOrder(id, sortOrder) {
    return request({
      url: `/medical-services/${id}/sort-order`,
      method: "put",
      params: { sortOrder },
    });
  },

  /**
   * 更新医疗服务图片
   * @param {Number} id 服务ID
   * @param {File} file 图片文件
   * @returns {Promise}
   */
  updateServiceImage(id, file) {
    const formData = new FormData();
    formData.append('file', file);
    
    return request({
      url: `/medical-services/${id}/image`,
      method: "post",
      data: formData
    });
  }
};

export default medicalServiceApi;
