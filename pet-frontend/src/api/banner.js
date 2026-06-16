import request from "@/utils/request";
import { baseURL } from "@/config";
import store from "@/store";

/**
 * 获取认证 Token（与 request.js 一致，上传接口需自行带 token）
 */
function getAuthToken() {
  return (
    (store?.state?.user?.accessToken) ||
    localStorage.getItem('vue-admin-better-2024') ||
    sessionStorage.getItem('vue-admin-better-2024') ||
    localStorage.getItem('token') ||
    sessionStorage.getItem('token')
  );
}

/**
 * 轮播图管理API
 */
export const bannerApi = {
  /**
   * 上传轮播图
   * @param {File} file 文件对象
   * @returns {Promise}
   */
  uploadBanner(file) {
    // 验证文件对象
    if (!file) {
      console.error('文件对象为空:', file);
      return Promise.reject(new Error('文件对象不能为空'));
    }
    
    const formData = new FormData();
    formData.append('file', file);
    
    const token = getAuthToken();
    const headers = {};
    if (token) {
      headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    }
    
    const url = (baseURL || 'http://localhost:8080/api').replace(/\/?$/, '') + '/banner/upload';
    
    return fetch(url, {
      method: 'POST',
      headers,
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
