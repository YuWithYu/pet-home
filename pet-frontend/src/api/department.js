import request from '@/utils/request'

// 部门管理API

/**
 * 获取部门列表（可选按门店筛选）
 * @param {number} [storeId] - 所属服务门店ID，传则只返回该门店的部门
 */
export function getAllDepartments(storeId) {
  return request({
    url: '/department/list',
    method: 'get',
    params: storeId != null ? { storeId } : {}
  })
}

/**
 * 根据ID获取部门详情
 */
export function getDepartmentById(id) {
  return request({
    url: `/department/${id}`,
    method: 'get'
  })
}

/**
 * 创建部门（data 可含 storeId 表示所属服务门店）
 */
export function createDepartment(data) {
  return request({
    url: '/department/create',
    method: 'post',
    data
  })
}

/**
 * 更新部门
 */
export function updateDepartment(data) {
  return request({
    url: '/department/update',
    method: 'put',
    data
  })
}

/**
 * 删除部门（自动处理成员）
 * @param {number} id 部门ID
 * @param {number} [storeId] 当前门店ID；传入时后端仅允许删除该门店下的部门，避免删到平台部门或其他门店
 */
export function deleteDepartment(id, storeId) {
  return request({
    url: `/department/${id}`,
    method: 'delete',
    params: storeId != null ? { storeId } : {}
  })
}

