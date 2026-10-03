import axios from 'axios'
import store from '@/store'
import { getAccessToken } from '@/utils/accessToken'
import { baseURL } from '@/config'

// 创建axios实例
const service = axios.create({
  baseURL: (baseURL || '').trim(),
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 优先从store获取token，如果没有则从storage获取
    const token = store.getters['user/accessToken'] || getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('响应错误:', error)
    if (error.response?.status === 401) {
      // 处理未授权错误：清除token
      store.dispatch('user/resetAccessToken')
      console.warn('401 错误，已清除 token');
    }
    return Promise.reject(error)
  }
)

// 铲屎服务管理API
export default {
  // 分页查询铲屎服务
  getLitterServicePage(params) {
    return service.get('/litter-services/page', { params })
  },

  // 根据ID获取铲屎服务详情
  getLitterServiceById(id) {
    return service.get(`/litter-services/${id}`)
  },

  // 创建铲屎服务
  createLitterService(data) {
    return service.post('/litter-services/create', data)
  },

  // 更新铲屎服务
  updateLitterService(data) {
    return service.put('/litter-services/update', data)
  },

  // 删除铲屎服务
  deleteLitterService(id) {
    return service.delete(`/litter-services/${id}`)
  },

  // 更新铲屎服务状态
  updateLitterServiceStatus(id, status) {
    return service.put(`/litter-services/${id}/status`, null, {
      params: { status }
    })
  },

  // 上传铲屎服务图片
  uploadLitterServiceImage(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post(`/litter-services/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取服务展示图
  getServiceBanner() {
    return service.get('/litter-banners/position/litter-page-top')
  },

  // 上传服务展示图
  uploadServiceBanner(file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post('/litter-banners/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除服务展示图
  deleteServiceBanner(id) {
    return service.delete(`/litter-banners/${id}`)
  }
}

