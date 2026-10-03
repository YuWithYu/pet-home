import request from '@/utils/request'

// 热门推荐管理API

/**
 * 获取商品销售记录
 * @param {number} productId 商品ID
 * @returns {Promise}
 */
export function getProductSalesRecords(productId) {
  return request({
    url: `/product/${productId}/sales`,
    method: 'get'
  })
}

/**
 * 获取商品评价
 * @param {number} productId 商品ID
 * @returns {Promise}
 */
export function getProductReviews(productId) {
  return request({
    url: `/product/${productId}/reviews`,
    method: 'get'
  })
}

// 导出productApi对象，包含所有商品相关的API方法
export const productApi = {
  getList,
  getAllProducts,
  create: createProduct,
  update: updateProduct,
  delete: deleteProduct,
  setProductHot,
  getAllCategories,
  addCategory,
  deleteCategory,
  updateCategory,
  getSalesRecords: getProductSalesRecords,
  getReviews: getProductReviews
}

/**
 * 获取所有商品列表
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getAllProducts(params) {
  return request({
    url: '/product/list',
    method: 'get',
    params
  })
}

/**
 * 获取商品列表 (别名方法，用于兼容)
 * @param {Object} params 查询参数
 * @returns {Promise}
 */
export function getList(params) {
  return getAllProducts(params)
}

/**
 * 设置商品热门状态
 * @param {number} productId 商品ID
 * @param {boolean} isHot 是否热门
 * @returns {Promise}
 */
export function setProductHot(productId, isHot) {
  return request({
    url: `/admin/products/${productId}/hot`,
    method: 'put',
    params: { isHot }
  })
}

/**
 * 获取商品详情
 * @param {number} productId 商品ID
 * @returns {Promise}
 */
export function getProductDetail(productId) {
  return request({
    url: `/product/${productId}`,
    method: 'get'
  })
}

/**
 * 创建商品
 * @param {Object} data 商品数据
 * @returns {Promise}
 */
export function createProduct(data) {
  return request({
    url: '/product/create',
    method: 'post',
    data
  })
}

/**
 * 更新商品信息
 * @param {number} productId 商品ID
 * @param {Object} data 商品数据
 * @returns {Promise}
 */
export function updateProduct(productId, data) {
  return request({
    url: `/product/update`,
    method: 'put',
    data: {
      id: productId,
      ...data
    }
  })
}

/**
 * 删除商品
 * @param {number} productId 商品ID
 * @returns {Promise}
 */
export function deleteProduct(productId) {
  return request({
    url: `/product/${productId}`,
    method: 'delete'
  })
}

// 商品分类管理API（baseURL 已含 /api，这里不要再加 /api，否则会变成 /api/api/...）
export function getAllCategories() {
  return request({
    url: 'categories/all',
    method: 'get'
  })
}

export function addCategory(data) {
  const payload = { ...data }
  if (payload.status === 'active') payload.status = 1
  if (payload.status === 'inactive') payload.status = 0

  return request({
    url: 'categories/create',
    method: 'post',
    data: payload
  })
}

export function deleteCategory(categoryId) {
  return request({
    url: `categories/${categoryId}`,
    method: 'delete'
  })
}

export function updateCategory(categoryId, data) {
  const payload = { ...data }
  if (payload.status === 'active') payload.status = 1
  if (payload.status === 'inactive') payload.status = 0

  return request({
    url: 'categories/update',
    method: 'put',
    data: {
      id: categoryId,
      ...payload
    }
  })
}

// 导入pet.js中的API函数
import { 
  getPetList, 
  getPetById, 
  createPet, 
  updatePet, 
  deletePet,
  getUserList,
  getUserById,
  updateUser
} from './pet.js'

// 导出petApi对象
export const petApi = {
  getList: getPetList,
  getById: getPetById,
  create: createPet,
  update: updatePet,
  delete: deletePet
}

// 导出userApi对象
export const userApi = {
  getList: getUserList,
  getById: getUserById,
  update: updateUser
}