import request from '@/utils/request'

/**
 * 宠物洗护服务管理 API
 */
const groomingServiceApi = {
  /**
   * 分页查询洗护服务
   */
  getGroomingServicePage(params) {
    return request({
      url: '/grooming-services/page',
      method: 'get',
      params,
    })
  },

  /**
   * 获取洗护服务详情
   */
  getGroomingServiceById(id) {
    return request({
      url: `/grooming-services/${id}`,
      method: 'get',
    })
  },

  /**
   * 创建洗护服务
   */
  createGroomingService(data) {
    return request({
      url: '/grooming-services/create',
      method: 'post',
      data,
    })
  },

  /**
   * 更新洗护服务
   */
  updateGroomingService(data) {
    return request({
      url: '/grooming-services/update',
      method: 'put',
      data,
    })
  },

  /**
   * 删除洗护服务
   */
  deleteGroomingService(id) {
    return request({
      url: `/grooming-services/${id}`,
      method: 'delete',
    })
  },

  /**
   * 更新洗护服务状态
   */
  updateGroomingServiceStatus(id, status) {
    return request({
      url: `/grooming-services/${id}/status`,
      method: 'put',
      params: { status },
    })
  },

  /**
   * 上传洗护服务图片
   */
  uploadGroomingServiceImage(file, id) {
    const formData = new FormData()
    formData.append('file', file)
    if (id) {
      formData.append('id', id)
    }
    return request({
      url: '/grooming-services/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 获取洗护服务展示图
   */
  getServiceBanner(position = 'grooming-page-top') {
    return request({
      url: `/grooming-banners/position/${position}`,
      method: 'get',
    })
  },

  /**
   * 上传洗护服务展示图
   */
  uploadServiceBanner(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/grooming-banners/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 删除洗护服务展示图
   */
  deleteServiceBanner(id) {
    return request({
      url: `/grooming-banners/${id}`,
      method: 'delete',
    })
  },
}

export default groomingServiceApi
