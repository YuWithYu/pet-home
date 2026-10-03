// 地址相关API
import { api } from '../api.js'

/** 与收藏/预约页一致：优先独立 userId，再从 userInfo 取 id/uid，避免只登录未写 userId 时列表永远失败 */
function resolveStoredUserId() {
  try {
    const raw = uni.getStorageSync('userId')
    if (raw != null && raw !== '' && String(raw) !== 'undefined') {
      const n = Number(raw)
      if (Number.isFinite(n) && n > 0) return n
    }
    const u = uni.getStorageSync('userInfo')
    if (u && typeof u === 'object') {
      const id = u.id != null ? u.id : u.uid
      if (id != null && id !== '' && String(id) !== 'undefined') {
        const n = Number(id)
        if (Number.isFinite(n) && n > 0) return n
      }
    }
  } catch (e) {}
  return null
}

const addressApi = {
  // 获取当前用户所有地址（showLoading 为 false 时不显示全局加载中，用于 onShow 静默刷新）
  getAddressList(showLoading = true) {
    const token = uni.getStorageSync('token')
    const uid = resolveStoredUserId()
    // 有 token 时务必请求后端：由 JWT 解析用户；query userId 仅作兜底（与 AddressController 一致）
    if (!token && uid == null) {
      return Promise.resolve({ code: 200, data: [], msg: '未登录' })
    }
    const data = uid != null ? { userId: uid } : {}
    return api.request({
      url: '/api/addresses/list',
      method: 'GET',
      data,
      showLoading: showLoading
    })
  },

  // 创建地址
  createAddress(addressData) {
    // 后端优先从 token 解析 userId，解析不到时使用 body 中的 userId
    const validUserId = resolveStoredUserId()

    const requestData = {
      name: addressData.name,
      phone: addressData.phone,
      province: addressData.province,
      city: addressData.city,
      district: addressData.district,
      detail: addressData.detail,
      isDefault: addressData.isDefault || false
    }
    if (validUserId != null) {
      requestData.userId = validUserId
    }

    // 同时传 query 参数 userId 作为兜底（token 解析失败时后端可回退使用）
    const url = validUserId != null
      ? `/api/addresses/create?userId=${validUserId}`
      : '/api/addresses/create'

    return api.request({
      url,
      method: 'POST',
      data: requestData
    })
  },

  // 更新地址
  updateAddress(addressData) {
    const validUserId = resolveStoredUserId()
    const payload = { ...addressData }
    if (validUserId != null) {
      payload.userId = validUserId
    }
    return api.request({
      url: '/api/addresses/update',
      method: 'PUT',
      data: payload
    })
  },

  // 删除地址（页面已自行 showLoading「删除中」，此处关闭全局 loading 避免双 loading / hide 竞态）
  deleteAddress(addressId) {
    const validUserId = resolveStoredUserId()
    const url = validUserId != null
      ? `/api/addresses/${addressId}?userId=${validUserId}`
      : `/api/addresses/${addressId}`
    return api.request({
      url,
      method: 'DELETE',
      showLoading: false
    })
  },

  // 设置默认地址
  setDefaultAddress(addressId) {
    return api.request({
      url: `/api/addresses/set-default/${addressId}`,
      method: 'PUT'
    })
  },

  // 获取默认地址（showLoading 为 false 时不显示全局加载中，用于 onShow 静默刷新）
  getDefaultAddress(userId, showLoading = true) {
    // 该接口后端会：优先从token解析userId；解析不到时才使用参数 userId
    // 但前端很多地方会传 null/undefined，导致 userId 参数为空，从而触发“未登录或用户ID无效”
    let finalUserId = userId
    if (finalUserId === undefined || finalUserId === null || finalUserId === '' || finalUserId === 'undefined') {
      const resolved = resolveStoredUserId()
      finalUserId = resolved != null ? resolved : null
    } else {
      const n = Number(finalUserId)
      finalUserId = Number.isFinite(n) && n > 0 ? n : null
    }

    // 未登录则直接返回空地址；调用方已有兜底逻辑（不会影响下单）
    if (!finalUserId) {
      return Promise.resolve({ code: 200, data: null, msg: '未登录，跳过获取默认地址' })
    }

    return api.request({
      url: '/api/addresses/default',
      method: 'GET',
      data: { userId: finalUserId },
      showLoading: showLoading
    })
  }
}

export default addressApi
