import request from "@/utils/request";

/**
 * 轮播图管理API
 */
export const bannerApi = {
  /**
   * 上传轮播图
   * @param {File} file 文件对象
   * @param {string} title 标题
   * @param {string} description 描述
   * @returns {Promise}
   */
  uploadBanner(file, title = '', description = '') {
    // 验证文件对象
    if (!file) {
      console.error('文件对象为空:', file);
      return Promise.reject(new Error('文件对象不能为空'));
    }
    
    console.log('API调用 - 上传文件:', {
      fileName: file.name,
      fileSize: file.size,
      fileType: file.type,
      isFile: file instanceof File,
      constructor: file.constructor.name,
      title: title,
      description: description
    });
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title);
    formData.append('description', description);
    
    // 调试FormData
    console.log('FormData内容:');
    for (let [key, value] of formData.entries()) {
      if (value instanceof File) {
        console.log(`${key}: File(${value.name}, ${value.size} bytes, ${value.type})`);
      } else {
        console.log(`${key}: ${value}`);
      }
    }
    
    // 直接使用fetch而不是axios，避免拦截器问题
    return fetch('http://localhost:8080/api/banner/upload', {
      method: 'POST',
      body: formData
    }).then(response => {
      console.log('上传响应状态:', response.status);
      return response.json();
    }).then(data => {
      console.log('上传响应数据:', data);
      return data;
    }).catch(error => {
      console.error('上传错误:', error);
      throw error;
    });
  },

  /**
   * 获取轮播图列表
   * @returns {Promise}
   */
  getBannerList() {
    return request({
      url: "/banner/list",
      method: "get"
    });
  },

  /**
   * 删除轮播图
   * @param {string} id 轮播图ID
   * @returns {Promise}
   */
  deleteBanner(id) {
    return request({
      url: `/banner/${id}`,
      method: "delete"
    });
  },

  /**
   * 更新轮播图信息
   * @param {string} id 轮播图ID
   * @param {string} title 标题
   * @param {string} description 描述
   * @param {string} status 状态 (active/inactive)
   * @returns {Promise}
   */
  updateBanner(id, title, description, status) {
    return request({
      url: `/banner/${id}`,
      method: "put",
      params: { title, description, status }
    });
  },

  /**
   * 更新轮播图状态
   * @param {string} id 轮播图ID
   * @param {string} status 状态 (active/inactive)
   * @returns {Promise}
   */
  updateBannerStatus(id, status) {
    return request({
      url: `/banner/${id}/status`,
      method: "put",
      params: { status }
    });
  }
};
