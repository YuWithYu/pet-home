import request from "@/utils/request";

/**
 * 服务展示图管理API
 */
const serviceBannerApi = {
  /**
   * 分页查询服务展示图
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getServiceBannerList(params) {
    return request({
      url: "/service-banners/page",
      method: "get",
      params,
    });
  },

  /**
   * 根据位置查询启用的展示图
   * @param {String} position 展示位置
   * @returns {Promise}
   */
  getActiveBannersByPosition(position) {
    return request({
      url: `/service-banners/position/${position}`,
      method: "get",
    });
  },

  /**
   * 获取选择服务下方的展示图
   * @returns {Promise}
   */
  getServiceSelectionBanner() {
    return request({
      url: "/service-banners/service-selection",
      method: "get",
    });
  },

  /**
   * 获取服务展示图详情
   * @param {Number} id 展示图ID
   * @returns {Promise}
   */
  getServiceBannerById(id) {
    return request({
      url: `/service-banners/${id}`,
      method: "get",
    });
  },

  /**
   * 创建服务展示图
   * @param {Object} data 展示图数据
   * @returns {Promise}
   */
  createServiceBanner(data) {
    return request({
      url: "/service-banners",
      method: "post",
      data,
    });
  },

  /**
   * 更新服务展示图
   * @param {Number} id 展示图ID
   * @param {Object} data 展示图数据
   * @returns {Promise}
   */
  updateServiceBanner(id, data) {
    return request({
      url: `/service-banners/${id}`,
      method: "put",
      data,
    });
  },

  /**
   * 删除服务展示图
   * @param {Number} id 展示图ID
   * @returns {Promise}
   */
  deleteServiceBanner(id) {
    return request({
      url: `/service-banners/${id}`,
      method: "delete",
    });
  },

  /**
   * 更新服务展示图状态
   * @param {Number} id 展示图ID
   * @param {String} status 状态
   * @returns {Promise}
   */
  updateBannerStatus(id, status) {
    return request({
      url: `/service-banners/${id}/status`,
      method: "put",
      params: { status },
    });
  },

  /**
   * 更新服务展示图排序
   * @param {Number} id 展示图ID
   * @param {Number} sortOrder 排序
   * @returns {Promise}
   */
  updateBannerSortOrder(id, sortOrder) {
    return request({
      url: `/service-banners/${id}/sort-order`,
      method: "put",
      params: { sortOrder },
    });
  },

  /**
   * 更新服务展示图图片
   * @param {Number} id 展示图ID
   * @param {File} file 图片文件
   * @returns {Promise}
   */
  updateBannerImage(id, file) {
    const formData = new FormData();
    formData.append('file', file);
    
    return request({
      url: `/service-banners/${id}/image`,
      method: "post",
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  }
};

export default serviceBannerApi;
