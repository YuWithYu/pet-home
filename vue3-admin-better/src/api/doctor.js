import request from '@/utils/request'

// 医师管理API
export const doctorApi = {
  // 获取所有医师
  getAllDoctors() {
    return request({
      url: '/doctors/all',
      method: 'get'
    })
  },

  // 获取在线医师
  getOnlineDoctors() {
    return request({
      url: '/doctors/online',
      method: 'get'
    })
  },

  // 根据专业领域获取医师
  getDoctorsBySpecialization(specialization) {
    return request({
      url: `/doctors/specialization/${specialization}`,
      method: 'get'
    })
  },

  // 根据类别获取医师
  getDoctorsByCategory(category) {
    return request({
      url: `/doctors/category/${category}`,
      method: 'get'
    })
  },

  // 根据ID获取医师详情
  getDoctorById(id) {
    return request({
      url: `/doctors/${id}`,
      method: 'get'
    })
  },

  // 添加医师
  addDoctor(data) {
    return request({
      url: '/doctors',
      method: 'post',
      data
    })
  },

  // 更新医师信息
  updateDoctor(id, data) {
    return request({
      url: `/doctors/${id}`,
      method: 'put',
      data
    })
  },

  // 删除医师
  deleteDoctor(id) {
    return request({
      url: `/doctors/${id}`,
      method: 'delete'
    })
  },

  // 更新医师头像
  updateDoctorAvatar(id, file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: `/doctors/${id}/avatar`,
      method: 'post',
      data: formData
    })
  }
}

