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

// 铲屎服务管理API
export default {
  // 分页查询铲屎服务
  getLitterServicePage(params) {
    return service.get('/api/litter-services/page', { params })
  },

  // 根据ID获取铲屎服务详情
  getLitterServiceById(id) {
    return service.get(`/api/litter-services/${id}`)
  },

  // 创建铲屎服务
  createLitterService(data) {
    return service.post('/api/litter-services/create', data)
  },

  // 更新铲屎服务
  updateLitterService(data) {
    return service.put('/api/litter-services/update', data)
  },

  // 删除铲屎服务
  deleteLitterService(id) {
    return service.delete(`/api/litter-services/${id}`)
  },

  // 更新铲屎服务状态
  updateLitterServiceStatus(id, status) {
    return service.put(`/api/litter-services/${id}/status`, null, {
      params: { status }
    })
  },

  // 上传铲屎服务图片
  uploadLitterServiceImage(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post(`/api/litter-services/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取服务展示图
  getServiceBanner() {
    return service.get('/api/litter-banners/position/litter-page-top')
  },

  // 上传服务展示图
  uploadServiceBanner(file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post('/api/litter-banners/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 删除服务展示图
  deleteServiceBanner(id) {
    return service.delete(`/api/litter-banners/${id}`)
  }
}

