// 购物车相关API
import { api } from '../api.js'

export default {
  // 添加商品到购物车
  addToCart(data) {
    return api.request({
      url: '/api/cart/add',
      method: 'POST',
      data: data
    })
  },
  
  // 获取购物车列表
  fetchCartList(userId) {
    // 构建URL，如果提供了userId，则作为参数传递
    let url = '/api/cart/list'
    if (userId) {
      url += `?userId=${userId}`
    }
    
    return api.request({
      url: url,
      method: 'GET'
    })
  },
  
  // 删除购物车商品
  deleteCartItem(params) {
    return api.request({
      url: '/api/cart/delete',
      method: 'POST',
      data: params
    })
  },
  
  // 更新购物车商品数量
  updateQuantity(params) {
    return api.request({
      url: '/api/cart/update/quantity',
      method: 'POST',
      data: params
    })
  },
  
  // 清空购物车
  clearCartList() {
    return api.request({
      url: '/api/cart/clear',
      method: 'POST'
    })
  }
}
