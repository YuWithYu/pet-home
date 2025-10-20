import request from '@/utils/request'

// 热门推荐管理API

// 导出productApi对象，包含所有商品相关的API方法
export const productApi = {
  getList,
  getAllProducts,
  getHotProducts,
  create: createProduct,
  update: updateProduct,
  delete: deleteProduct,
  setProductHot,
  batchSetHotProducts,
  getAllCategories,
  addCategory,
  deleteCategory,
  updateCategory
}

/**
 * 获取热门推荐商品
 * @param {number} limit 限制数量
 * @returns {Promise}
 */
export function getHotProducts(limit = 10) {
  return request({
    url: '/admin/products/hot',
    method: 'get',
    params: { limit }
  })
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
 * 批量设置热门推荐
 * @param {Object} data 批量操作数据
 * @returns {Promise}
 */
export function batchSetHotProducts(data) {
  return request({
    url: '/admin/products/hot/batch',
    method: 'put',
    data
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

// 商品分类管理API
export function getAllCategories() {
  return request({
    url: '/admin/categories',
    method: 'get'
  })
}

export function addCategory(data) {
  return request({
    url: '/admin/categories',
    method: 'post',
    data
  })
}

export function deleteCategory(categoryId) {
  return request({
    url: `/admin/categories/${categoryId}`,
    method: 'delete'
  })
}

export function updateCategory(categoryId, data) {
  return request({
    url: `/admin/categories/${categoryId}`,
    method: 'put',
    data
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