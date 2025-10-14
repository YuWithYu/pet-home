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

// 洗护服务管理API
export default {
  // 分页查询洗护服务
  getGroomingServicePage(params) {
    return service.get('/api/grooming-services/page', { params })
  },

  // 查询所有启用的洗护服务
  getActiveGroomingServices() {
    return service.get('/api/grooming-services/active')
  },

  // 根据ID获取洗护服务详情
  getGroomingServiceById(id) {
    return service.get(`/api/grooming-services/${id}`)
  },

  // 创建洗护服务
  createGroomingService(data) {
    return service.post('/api/grooming-services', data)
  },

  // 更新洗护服务
  updateGroomingService(id, data) {
    return service.put(`/api/grooming-services/${id}`, data)
  },

  // 删除洗护服务
  deleteGroomingService(id) {
    return service.delete(`/api/grooming-services/${id}`)
  },

  // 更新洗护服务状态
  updateGroomingServiceStatus(id, status) {
    return service.put(`/api/grooming-services/${id}/status`, null, {
      params: { status }
    })
  },

  // 更新洗护服务排序
  updateGroomingServiceSortOrder(id, sortOrder) {
    return service.put(`/api/grooming-services/${id}/sort-order`, null, {
      params: { sortOrder }
    })
  },

  // 上传洗护服务图片
  uploadGroomingServiceImage(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post(`/api/grooming-services/${id}/image`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传服务展示图
  uploadServiceBanner(file) {
    const formData = new FormData()
    formData.append('file', file)
    return service.post('/api/grooming-banners/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取服务展示图
  getServiceBanner() {
    return service.get('/api/grooming-banners/position/grooming-page-top')
  },

  // 删除服务展示图
  deleteServiceBanner(id) {
    return service.delete(`/api/grooming-banners/${id}`)
  }
}
