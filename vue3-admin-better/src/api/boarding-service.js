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

// 宠物寄养服务管理API
export default {
  // 分页查询寄养服务
  getBoardingServicePage(params) {
    return service.get('/api/boarding-services/page', { params })
  },

  // 根据ID获取寄养服务详情
  getBoardingServiceById(id) {
    return service.get(`/api/boarding-services/${id}`)
  },

  // 创建寄养服务
  createBoardingService(data) {
    return service.post('/api/boarding-services/create', data)
  },

  // 更新寄养服务
  updateBoardingService(data) {
    return service.put('/api/boarding-services/update', data)
  },

  // 删除寄养服务
  deleteBoardingService(id) {
    return service.delete(`/api/boarding-services/${id}`)
  },

  // 更新寄养服务状态
  updateBoardingServiceStatus(id, status) {
    return service.put(`/api/boarding-services/${id}/status`, null, {
      params: { status }
    })
  },

  // 上传寄养服务图片
  uploadBoardingServiceImage(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post(`/api/boarding-services/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取服务展示图
  getServiceBanner() {
    return service.get('/api/boarding-banners/position/boarding-page-top')
  },

  // 上传服务展示图
  uploadServiceBanner(file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post('/api/boarding-banners/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除服务展示图
  deleteServiceBanner(id) {
    return service.delete(`/api/boarding-banners/${id}`)
  }
}
