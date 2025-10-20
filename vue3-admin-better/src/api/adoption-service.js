import axios from 'axios'

const baseURL = process.env.NODE_ENV === 'development' ? 'http://localhost:8080' : ''

// 创建axios实例
const service = axios.create({
  baseURL: baseURL,
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
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
      // 处理未授权错误
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 宠物领养服务管理API
export default {
  // 分页查询领养服务
  getAdoptionServicePage(params) {
    return service.get('/api/adoption-services/page', { params })
  },

  // 根据ID获取领养服务详情
  getAdoptionServiceById(id) {
    return service.get(`/api/adoption-services/${id}`)
  },

  // 创建领养服务
  createAdoptionService(data) {
    return service.post('/api/adoption-services/create', data)
  },

  // 更新领养服务
  updateAdoptionService(data) {
    return service.put('/api/adoption-services/update', data)
  },

  // 删除领养服务
  deleteAdoptionService(id) {
    return service.delete(`/api/adoption-services/${id}`)
  },

  // 更新领养服务状态
  updateAdoptionServiceStatus(id, status) {
    return service.put(`/api/adoption-services/${id}/status`, null, {
      params: { status }
    })
  },

  // 上传领养服务图片
  uploadAdoptionServiceImage(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post(`/api/adoption-services/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取服务展示图
  getServiceBanner() {
    return service.get('/api/adoption-banners/position/adoption-page-top')
  },

  // 上传服务展示图
  uploadServiceBanner(file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post('/api/adoption-banners/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除服务展示图
  deleteServiceBanner(id) {
    return service.delete(`/api/adoption-banners/${id}`)
  }
}
