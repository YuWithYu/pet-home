import request from '@/utils/request'

/**
 * 宠物医院服务管理 API
 */
const hospitalServiceApi = {
  /**
   * 分页查询医院服务
   */
  getHospitalServicePage(params) {
    return request({
      url: '/hospital-services/page',
      method: 'get',
      params,
    })
  },

  /**
   * 获取医院服务详情
   */
  getHospitalServiceById(id) {
    return request({
      url: `/hospital-services/${id}`,
      method: 'get',
    })
  },

  /**
   * 创建医院服务
   */
  createHospitalService(data) {
    return request({
      url: '/hospital-services/create',
      method: 'post',
      data,
    })
  },

  /**
   * 更新医院服务
   */
  updateHospitalService(data) {
    return request({
      url: '/hospital-services/update',
      method: 'put',
      data,
    })
  },

  /**
   * 删除医院服务
   */
  deleteHospitalService(id) {
    return request({
      url: `/hospital-services/${id}`,
      method: 'delete',
    })
  },

  /**
   * 更新医院服务状态
   */
  updateHospitalServiceStatus(id, status) {
    return request({
      url: `/hospital-services/${id}/status`,
      method: 'put',
      params: { status },
    })
  },

  /**
   * 上传医院服务图片
   */
  uploadHospitalServiceImage(file, id) {
    const formData = new FormData()
    formData.append('file', file)
    if (id) {
      formData.append('id', id)
    }
    return request({
      url: '/hospital-services/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 获取医院服务展示图
   */
  getServiceBanner(position = 'hospital-page-top') {
    return request({
      url: `/hospital-banners/position/${position}`,
      method: 'get',
    })
  },

  /**
   * 上传医院服务展示图
   */
  uploadServiceBanner(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/hospital-banners/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
  },

  /**
   * 删除医院服务展示图
   */
  deleteServiceBanner(id) {
    return request({
      url: `/hospital-banners/${id}`,
      method: 'delete',
    })
  },
}

export default hospitalServiceApi
