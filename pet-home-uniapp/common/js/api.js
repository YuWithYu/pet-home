// common/js/api.js
import { util } from './util.js'

// API接口封装
class ApiService {
  constructor() {
    this.baseURL = 'http://localhost:8080'
  }

  // 请求封装
  request(options) {
    const { url, method = 'GET', data = {}, header = {}, showLoading = true } = options

    // 显示加载提示
    if (showLoading) {
      uni.showLoading({
        title: '加载中...',
        mask: true
      })
    }

    // 构建请求头
    const requestHeader = {
      'Content-Type': 'application/json',
      ...header
    }

    // 如果有token，添加到请求头
    const token = uni.getStorageSync('token')
    if (token) {
      requestHeader['Authorization'] = `Bearer ${token}`
    }

    return new Promise((resolve, reject) => {
      uni.request({
        url: this.baseURL + url,
        method: method,
        data: data,
        header: requestHeader,
        success: (res) => {
          if (res.statusCode === 200) {
            // 兼容不同接口返回格式
            let result = res.data
            if (result.code === 0 || result.success) {
              resolve(result)
            } else {
              uni.showToast({
                title: result.msg || '请求失败',
                icon: 'none',
                duration: 2000
              })
              reject(new Error(result.msg || '请求失败'))
            }
          } else {
            uni.showToast({
              title: '网络错误',
              icon: 'none',
              duration: 2000
            })
            reject(new Error('网络错误'))
          }
        },
        fail: (err) => {
          uni.showToast({
            title: '网络请求失败',
            icon: 'none',
            duration: 2000
          })
          reject(err)
        },
        complete: () => {
          // 隐藏加载提示
          if (showLoading) {
            uni.hideLoading()
          }
        }
      })
    })
  }

  // 获取配置信息
  getConfig(keys) {
    return this.request({
      url: '/tz/config/values',
      method: 'GET',
      data: { keys }
    })
  }

  // 获取轮播图列表
  getBannerList(type) {
    return this.request({
      url: '/tz/banner/list',
      method: 'GET',
      data: { type }
    })
  }

  // 获取铲屎服务列表
  getLitterServicePage(params) {
    return this.request({
      url: '/api/litter-services/page',
      method: 'GET',
      data: params
    })
  }

  // 获取宠物医院服务列表
  getHospitalServicePage(params) {
    return this.request({
      url: '/api/hospital-services/page',
      method: 'GET',
      data: params
    })
  }

  // 获取寄养服务列表
  getBoardingServicePage(params) {
    return this.request({
      url: '/api/boarding-services/page',
      method: 'GET',
      data: params
    })
  }

  // 获取医疗服务列表
  getMedicalServicePage(params) {
    return this.request({
      url: '/api/medical-services/page',
      method: 'GET',
      data: params
    })
  }

  // 获取洗护服务列表
  getGroomingServicePage(params) {
    return this.request({
      url: '/api/grooming-services/page',
      method: 'GET',
      data: params
    })
  }

  // 获取领养服务列表
  getAdoptionServicePage(params) {
    return this.request({
      url: '/api/adoption-services/page',
      method: 'GET',
      data: params
    })
  }

  // 获取可用时间段
  getAvailableTimeSlots(params) {
    return this.request({
      url: '/api/time-slots/available',
      method: 'GET',
      data: params
    })
  }

  // 创建预约
  createBooking(data) {
    return this.request({
      url: '/api/bookings/create',
      method: 'POST',
      data: data
    })
  }

  // 获取我的预约列表
  getMyBookings(params) {
    return this.request({
      url: '/api/bookings/my-list',
      method: 'GET',
      data: params
    })
  }

  // 取消预约
  cancelBooking(id) {
    return this.request({
      url: `/api/bookings/${id}/cancel`,
      method: 'PUT'
    })
  }

  // 微信小程序授权
  wxappAuthorize(params) {
    return this.request({
      url: '/tz/user/wxapp/authorize',
      method: 'POST',
      data: params
    })
  }

  // 手机号密码登录
  loginByPhone(phone, password) {
    return this.request({
      url: '/tz/user/login',
      method: 'POST',
      data: { phone, password }
    })
  }

  // 微信登录
  loginByWechat(code) {
    return this.request({
      url: '/tz/user/wxapp/login',
      method: 'POST',
      data: { code }
    })
  }

  // 用户注册
  register(phone, password, nickname, smsCode) {
    return this.request({
      url: '/tz/user/register',
      method: 'POST',
      data: { 
        phone: String(phone), 
        password: String(password), 
        nickname: String(nickname || ''), 
        smsCode: String(smsCode) 
      }
    })
  }

  // 获取用户详情
  getUserDetail(token) {
    return this.request({
      url: '/tz/user/detail',
      method: 'GET',
      data: { token }
    })
  }

  // 获取用户资产
  getUserAmount(token) {
    return this.request({
      url: '/tz/user/amount',
      method: 'GET',
      data: { token }
    })
  }

  // 获取最新公告
  getLastNotice(type) {
    return this.request({
      url: '/tz/notice/last-one',
      method: 'GET',
      data: { type }
    })
  }

  // 获取所有商品分类
  getAllCategories() {
    return this.request({
      url: '/api/categories/all',
      method: 'GET'
    })
  }

  // 获取商品列表V2
  getGoodsList(params) {
    return this.request({
      url: '/api/product/list',
      method: 'GET',
      data: params
    })
  }

  // 获取商品详情
  getGoodsDetail(id) {
    return this.request({
      url: `/api/product/${id}`,
      method: 'GET'
    })
  }

  // 获取商品动态
  getGoodsDynamic(type = 0) {
    return this.request({
      url: '/tz/shop/goods/dynamic',
      method: 'GET',
      data: { type }
    })
  }

  // 批量获取广告位
  getAdPositionBatch(keys) {
    return this.request({
      url: '/tz/site/adPosition/batch',
      method: 'GET',
      data: { keys }
    })
  }

  // 获取商品动态（备用接口）
  getSiteGoodsDynamic(type = 0) {
    return this.request({
      url: '/tz/site/goods/dynamic',
      method: 'GET',
      data: { type }
    })
  }

  // 获取我的卡券
  getMyCards(token) {
    return this.request({
      url: '/tz/card/my',
      method: 'GET',
      data: { token }
    })
  }

  // 获取购物车信息
  getShoppingCartInfo(token, type) {
    return this.request({
      url: '/tz/shopping-cart/info',
      method: 'GET',
      data: { token, type }
    })
  }

  // ==================== 用户相关接口 ====================

  // 发送短信验证码
  sendSmsCode(phone) {
    return this.request({
      url: '/tz/sms/send',
      method: 'POST',
      data: { phone }
    })
  }

  // 用户登录
  login(loginData) {
    return this.request({
      url: '/tz/user/login',
      method: 'POST',
      data: loginData
    })
  }

  // 获取当前用户信息
  getCurrentUser() {
    return this.request({
      url: '/tz/user/current',
      method: 'GET'
    })
  }

  // 更新用户信息
  updateUser(userData) {
    return this.request({
      url: '/tz/user/update',
      method: 'POST',
      data: userData
    })
  }

  // ==================== 商品相关接口 ====================

  // 分页查询商品
  getProductPage(pageNo = 1, pageSize = 10) {
    return this.request({
      url: '/api/product/page',
      method: 'GET',
      data: { pageNo, pageSize }
    })
  } 

  // 获取热门商品
  getHotProducts(limit = 10) {
    return this.request({
      url: '/api/product/hot',
      method: 'GET',
      data: { limit }
    })
  }

  // 获取推荐商品
  getRecommendProducts(limit = 4) {
    return this.request({
      url: '/api/product/recommend',
      method: 'GET',
      data: { limit }
    })
  }

  // 获取商品详情
  getProductDetail(id) {
    return this.request({
      url: `/api/product/${id}`,
      method: 'GET'
    })
  }

  // ==================== 购物车相关接口 ====================

  // 分页查询购物车
  getCartPage(pageNo = 1, pageSize = 10, userId) {
    return this.request({
      url: '/api/cart/page',
      method: 'GET',
      data: { pageNo, pageSize, userId }
    })
  }

  // 添加到购物车
  addToCart(cartData) {
    return this.request({
      url: '/api/cart/add',
      method: 'POST',
      data: cartData
    })
  }

  // 更新购物车商品
  updateCartItem(cartData) {
    return this.request({
      url: '/api/cart/update',
      method: 'PUT',
      data: cartData
    })
  }

  // 从购物车删除商品
  removeFromCart(id) {
    return this.request({
      url: `/api/cart/${id}`,
      method: 'DELETE'
    })
  }

  // 获取购物车商品数量
  getCartCount(userId) {
    return this.request({
      url: '/api/cart/count',
      method: 'GET',
      data: { userId }
    })
  }

  // ==================== 宠物相关接口 ====================

  // 分页查询宠物
  getPetPage(pageNo = 1, pageSize = 10, userId) {
    return this.request({
      url: '/api/pets/page',
      method: 'GET',
      data: { pageNo, pageSize, userId }
    })
  }

  // 创建宠物
  createPet(petData) {
    return this.request({
      url: '/api/pets/create',
      method: 'POST',
      data: petData
    })
  }

  // 更新宠物
  updatePet(petData) {
    return this.request({
      url: '/api/pets/update',
      method: 'PUT',
      data: petData
    })
  }

  // 删除宠物
  deletePet(id) {
    return this.request({
      url: `/api/pets/${id}`,
      method: 'DELETE'
    })
  }

  // 获取宠物详情
  getPetDetail(id) {
    return this.request({
      url: `/api/pets/${id}`,
      method: 'GET'
    })
  }

  // ==================== 预约相关接口 ====================

  // 分页查询预约
  getAppointmentPage(pageNo = 1, pageSize = 10) {
    return this.request({
      url: '/api/door-cleaning/page',
      method: 'GET',
      data: { pageNo, pageSize }
    })
  }

  // 创建预约
  createAppointment(appointmentData) {
    return this.request({
      url: '/api/door-cleaning/create',
      method: 'POST',
      data: appointmentData
    })
  }

  // 更新预约状态
  updateAppointmentStatus(id, status) {
    console.log('调用更新预约状态API:', { id, status })
    return this.request({
      url: `/api/door-cleaning/${id}/status?status=${status}`,
      method: 'PUT'
    })
  }

  // 获取用户预约列表
  getUserAppointments(userId) {
    return this.request({
      url: `/api/door-cleaning/user/list/${userId}`,
      method: 'GET'
    })
  }

  // 获取宠物详情
  getPetById(id) {
    return this.request({
      url: `/api/pets/${id}`,
      method: 'GET'
    })
  }

  // 获取预约详情
  getAppointmentDetail(id) {
    return this.request({
      url: `/api/door-cleaning/${id}`,
      method: 'GET'
    })
  }

  // ==================== 宠物医院预约相关接口 ====================

  // 创建宠物医院预约
  createHospitalAppointment(appointmentData) {
    return this.request({
      url: '/api/hospital-appointments/create',
      method: 'POST',
      data: appointmentData
    })
  }

  // 获取宠物医院预约列表
  getHospitalAppointmentPage(pageNo = 1, pageSize = 10) {
    return this.request({
      url: '/api/hospital-appointments/page',
      method: 'GET',
      data: { pageNo, pageSize }
    })
  }

  // 获取用户宠物医院预约列表
  getUserHospitalAppointments(userId) {
    return this.request({
      url: `/api/hospital-appointments/user/list/${userId}`,
      method: 'GET'
    })
  }

  // 更新宠物医院预约状态
  updateHospitalAppointmentStatus(id, status) {
    return this.request({
      url: `/api/hospital-appointments/${id}/status?status=${status}`,
      method: 'PUT'
    })
  }

  // 获取宠物医院预约详情
  getHospitalAppointmentDetail(id) {
    return this.request({
      url: `/api/hospital-appointments/${id}`,
      method: 'GET'
    })
  }

  // ==================== 宠物洗护预约相关 API ====================

  // 创建宠物洗护预约
  createGroomingAppointment(appointmentData) {
    return this.request({
      url: '/api/grooming-appointments/create',
      method: 'POST',
      data: appointmentData
    })
  }

  // 获取宠物洗护预约列表
  getGroomingAppointmentPage(pageNo = 1, pageSize = 10) {
    return this.request({
      url: '/api/grooming-appointments/page',
      method: 'GET',
      data: { pageNo, pageSize }
    })
  }

  // 获取用户宠物洗护预约列表
  getUserGroomingAppointments(userId) {
    return this.request({
      url: `/api/grooming-appointments/user/list/${userId}`,
      method: 'GET'
    })
  }

  // 更新宠物洗护预约状态
  updateGroomingAppointmentStatus(id, status) {
    return this.request({
      url: `/api/grooming-appointments/${id}/status?status=${status}`,
      method: 'PUT'
    })
  }

  // 获取宠物洗护预约详情
  getGroomingAppointmentDetail(id) {
    return this.request({
      url: `/api/grooming-appointments/${id}`,
      method: 'GET'
    })
  }


  // ==================== 订单相关接口 ====================

  // 获取订单列表
  getOrderList(status) {
    return this.request({
      url: '/api/orders/list',
      method: 'GET',
      data: { status }
    })
  }

  // 获取订单详情
  getOrderDetail(orderId) {
    return this.request({
      url: `/api/orders/${orderId}`,
      method: 'GET'
    })
  }

  // 创建订单
  createOrder(orderData) {
    return this.request({
      url: '/api/orders/create',
      method: 'POST',
      data: orderData
    })
  }

  // 更新订单状态
  updateOrderStatus(orderId, status) {
    return this.request({
      url: `/api/orders/${orderId}/status`,
      method: 'PUT',
      data: { status }
    })
  }

  // ==================== 美容服务预约订单 ====================

  // 获取美容服务预约订单列表
  getGroomingAppointments(status) {
    return this.request({
      url: '/api/orders/grooming-appointments',
      method: 'GET',
      data: { status }
    })
  }

  // ==================== 服务配置相关接口 ====================

  // 获取所有服务配置
  getAllServiceConfigs() {
    return this.request({
      url: '/api/service-config/all',
      method: 'GET'
    })
  }

  // 根据服务类型获取配置
  getServiceConfigByType(serviceType) {
    return this.request({
      url: `/api/service-config/type/${serviceType}`,
      method: 'GET'
    })
  }

  // ==================== 核销相关接口 ====================

  // 核销验证
  verifyCode(verifyCode) {
    return this.request({
      url: '/api/verify/verify-code',
      method: 'POST',
      data: { verifyCode }
    })
  }

  // 检查核销码状态
  checkVerifyCode(verifyCode) {
    return this.request({
      url: `/api/verify/check-code/${verifyCode}`,
      method: 'GET'
    })
  }
}

// 创建API实例
const api = new ApiService()

export { api }
