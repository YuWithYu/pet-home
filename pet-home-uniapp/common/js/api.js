// common/js/api.js
import { util } from './util.js'

// API接口封装
class ApiService {
  constructor() {
    this.baseURL = util.getApiBaseUrl()
  }

  setBaseUrl(baseUrl, options = {}) {
    if (typeof baseUrl !== 'string') return
    const normalized = baseUrl.trim().replace(/\/+$/, '')
    if (!normalized) return
    this.baseURL = normalized
    if (options.persist !== false) {
      util.setApiBaseUrl(normalized)
      // 移除API base URL设置日志以提升性能
    }
  }

  getBaseUrl() {
    // 始终返回最新存储值，保证外部修改后立即生效
    const current = util.getApiBaseUrl()
    if (current !== this.baseURL) {
      this.baseURL = current
      // 移除API base URL更新日志以提升性能
    }
    return this.baseURL
  }

  // 请求封装（options.authToken 可覆盖默认 token，用于工作人员接口）
  request(options) {
    const { url, method = 'GET', data = {}, header = {}, showLoading = true, authToken, timeout: timeoutOpt, retryOnTimeout = false, retryCount = 0 } = options
    const timeoutMs =
      typeof timeoutOpt === 'number' && timeoutOpt > 0 ? timeoutOpt : 30000
    const storedBase = this.getBaseUrl()
    const overrideBase =
      typeof options.baseURL === 'string' && options.baseURL.trim()
        ? options.baseURL.trim().replace(/\/+$/, '')
        : ''
    const baseURL = overrideBase || storedBase
    const fullUrl =
      url && url.startsWith('/') ? `${baseURL}${url}` : `${baseURL}/${url || ''}`

    // 使用计数器管理 loading，避免多个请求时 showLoading/hideLoading 不匹配
    let loadingShown = false
    if (showLoading) {
      // 使用 util 的 loading 管理函数，支持计数器
      if (typeof util !== 'undefined' && util.showLoading) {
        util.showLoading('加载中...')
      } else {
        uni.showLoading({
          title: '加载中...',
          mask: true
        })
      }
      loadingShown = true
    }

    // 构建请求头
    const requestHeader = {
      'Content-Type': 'application/json',
      ...header
    }

    // 如果有token，添加到请求头（authToken 优先，用于工作人员接口）
    const token = authToken || uni.getStorageSync('token')
    if (token) {
      requestHeader['Authorization'] = `Bearer ${token}`
      // 移除频繁的token日志输出以提升性能
    } else {
      // 移除token不存在的警告日志以提升性能
    }

    // GET 请求不传 undefined/null 或字符串 "undefined"/"null"，避免后端 Long 解析报错
    let requestData = data
    if (method && method.toUpperCase() === 'GET' && data && typeof data === 'object') {
      requestData = {}
      Object.keys(data).forEach(k => {
        const v = data[k]
        if (v === undefined || v === null) return
        if (v === 'undefined' || v === 'null') return
        if (typeof v === 'string' && (v === '' || v === 'undefined' || v === 'null')) return
        requestData[k] = v
      })
    }

    return new Promise((resolve, reject) => {
      uni.request({
        url: fullUrl,
        method: method,
        data: requestData,
        header: requestHeader,
        timeout: timeoutMs,
        success: (res) => {
          console.log('最终请求URL:', fullUrl)
          // 移除频繁的日志输出以提升性能
          if (res.statusCode === 200) {
            // 兼容不同接口返回格式
            let result = res.data
            // 移除频繁的日志输出以提升性能
            
            // 如果result为null或undefined，尝试使用res.data
            if (!result && res.data) {
              result = res.data
            }
            
            // 修复：后端返回 code: 200 表示成功
            if (result && (result.code === 200 || result.code === 0 || result.success)) {
              resolve(result)
            } else {
              // 不在这里显示 toast，让调用方决定是否显示错误提示
              const errorMsg = (result && (result.msg || result.message)) || '系统异常，请稍后重试'
              console.error('[API响应错误]', fullUrl, '错误信息:', errorMsg, '完整响应:', result, '响应code:', result?.code)
              reject(new Error(errorMsg))
            }
          } else if (res.statusCode === 401) {
            // 401未授权错误，检查当前页面，避免在登录/注册页面清除存储
            try {
              const pages = getCurrentPages()
              const currentPage = pages && pages.length > 0 ? pages[pages.length - 1] : null
              const currentRoute = currentPage ? currentPage.route : ''
              
              // 如果当前在登录或注册页面，不清除存储（可能是刚登录）
              if (currentRoute && (currentRoute.includes('login') || currentRoute.includes('register'))) {
                reject(new Error('未授权，请重新登录'))
                return
              }
              
              // 检查是否是刚登录后的请求（5分钟内）
              const loginTime = uni.getStorageSync('loginTime')
              const token = uni.getStorageSync('token')
              const userId = uni.getStorageSync('userId')
              const now = Date.now()
              const isRecentLogin = loginTime && (now - loginTime) < 5 * 60 * 1000 // 5分钟内
              
              if (token && userId && isRecentLogin) {
                // 不清除存储，只返回错误，让调用方处理
                reject(new Error('认证失败，请稍后重试'))
                return
              }
              
              // 检查是否是用户主页且token存在，可能是token还未完全生效
              if (currentRoute && currentRoute.includes('main/index')) {
                if (token && userId) {
                  // 不清除存储，只返回错误，让调用方处理
                  reject(new Error('认证失败，请稍后重试'))
                  return
                }
              }
            } catch (e) {
              // 静默处理错误
            }
            
            // 401未授权错误，清除token并跳转到登录页
            uni.removeStorageSync('token')
            uni.removeStorageSync('userId')
            uni.removeStorageSync('userInfo')
            uni.removeStorageSync('username')
            uni.removeStorageSync('loginTime') // 清除登录时间戳
            uni.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none',
              duration: 2000
            })
            setTimeout(() => {
              uni.reLaunch({
                url: '/pages-auth/login'
              })
            }, 1500)
            reject(new Error('未授权，请重新登录'))
          } else {
            // 502等服务器错误，记录详细错误信息
            console.error(`[API请求失败] ${method} ${fullUrl} - 状态码: ${res.statusCode}`, res)
            const errorMsg = res.statusCode === 502 ? '服务器网关错误，请检查后端服务是否正常运行' : 
                            res.statusCode === 503 ? '服务暂时不可用' :
                            res.statusCode === 504 ? '请求超时' : '网络错误'
            reject(new Error(errorMsg))
          }
        },
        fail: (err) => {
          // 网络请求失败，记录详细错误信息
          console.error(`[API请求失败] ${method} ${fullUrl}`, err)
          const errMsg = (err && err.errMsg) ? String(err.errMsg) : ''
          const isTimeout = errMsg.toLowerCase().includes('timeout')
          if (isTimeout && retryOnTimeout && retryCount < 1) {
            // 超时自动重试一次（隐藏重复 loading，避免闪烁）
            this.request({
              ...options,
              showLoading: false,
              retryOnTimeout: true,
              retryCount: retryCount + 1,
              timeout: Math.max(timeoutMs, 90000)
            }).then(resolve).catch(reject)
            return
          }
          const errorMsg = err.errMsg || '网络请求失败'
          reject(new Error(errorMsg))
        },
        complete: () => {
          // 隐藏加载提示，使用 util 的 hideLoading 支持计数器
          if (loadingShown) {
            if (typeof util !== 'undefined' && util.hideLoading) {
              util.hideLoading()
            } else {
              uni.hideLoading()
            }
          }
        }
      })
    })
  }

  // 获取配置信息
  getConfig(keys) {
    return this.request({
      url: '/api/config/values',
      method: 'GET',
      data: { keys }
    })
  }

  // 获取轮播图列表（仅启用的）
  getBannerList(showLoading = true) {
    return this.request({
      url: '/api/banner/active',
      method: 'GET',
      showLoading: showLoading,
      timeout: 90000,
      retryOnTimeout: true
    })
  }

  // 获取轮播图/专题Banner详情
  getBannerDetail(id) {
    return this.request({
      url: `/api/banner/${id}`,
      method: 'GET'
    })
  }

  // 获取每日专题数据（含 banners、topics 等）
  getDailyTopics() {
    return this.request({
      url: '/api/daily-topics',
      method: 'GET'
    })
  }

  // 获取专题主题分类列表（与后台「主题分类管理」同步，用于宠物专题 Tab）
  getDailyTopicThemes() {
    return this.request({
      url: '/api/daily-topics/themes',
      method: 'GET'
    })
  }

  // 获取专题详情（管理员后台创建的官方专题，可选 userId 用于点赞/收藏状态）
  // showLoading 默认 false：详情页自带 loading 态，避免与 uni.showLoading 叠加重影/难关闭
  getDailyTopicDetail(id, userId = null, showLoading = false) {
    let url = `/api/daily-topics/${id}`
    if (userId) url += `?userId=${userId}`
    return this.request({ url, method: 'GET', showLoading })
  }

  // 点赞专题
  likeTopic(topicId, userId) {
    return this.request({
      url: `/api/daily-topics/${topicId}/like?userId=${userId}`,
      method: 'POST'
    })
  }

  // 取消点赞专题
  unlikeTopic(topicId, userId) {
    return this.request({
      url: `/api/daily-topics/${topicId}/like?userId=${userId}`,
      method: 'DELETE'
    })
  }

  // 收藏专题（收藏到「我的收藏-文章」）
  collectTopic(topicId, userId) {
    return this.request({
      url: `/api/daily-topics/${topicId}/collect?userId=${userId}`,
      method: 'POST'
    })
  }

  // 取消收藏专题
  uncollectTopic(topicId, userId) {
    return this.request({
      url: `/api/daily-topics/${topicId}/collect?userId=${userId}`,
      method: 'DELETE'
    })
  }

  // 检查专题收藏状态
  checkTopicCollectStatus(topicId, userId) {
    return this.request({
      url: `/api/daily-topics/${topicId}/collect/status?userId=${userId}`,
      method: 'GET'
    })
  }

  // 获取用户收藏的专题列表（用于「我的收藏-文章」）
  getCollectedTopics(params = {}) {
    const { showLoading = true, ...rest } = params
    return this.request({
      url: '/api/daily-topics/collected',
      method: 'GET',
      data: {
        userId: rest.userId,
        page: rest.page || 1,
        size: rest.size || 20
      },
      showLoading: showLoading
    })
  }

  // 获取专题评论列表（详情页建议 showLoading=false，由页面自行控制加载态）
  getTopicComments(topicId, userId = null, showLoading = false) {
    let url = `/api/daily-topics/${topicId}/comments`
    if (userId) url += `?userId=${userId}`
    return this.request({ url, method: 'GET', showLoading })
  }

  // 添加专题评论
  addTopicComment(topicId, commentData) {
    return this.request({
      url: `/api/daily-topics/${topicId}/comments`,
      method: 'POST',
      data: commentData
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

  // 根据ID获取铲屎服务详情
  getLitterServiceById(id) {
    return this.request({
      url: `/api/litter-services/${id}`,
      method: 'GET'
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

  // 根据ID获取宠物医院服务详情
  getHospitalServiceById(id) {
    return this.request({
      url: `/api/hospital-services/${id}`,
      method: 'GET'
    })
  }

  // 获取宠物医院服务列表
  getHospitalServiceList(params = {}) {
    return this.request({
      url: '/api/hospital-services/list',
      method: 'GET',
      data: params
    })
  }

  // 获取宠物医院展示图
  getHospitalBanner(position = 'hospital-page-top') {
    return this.request({
      url: `/api/hospital-banners/position/${position}`,
      method: 'GET'
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

  // 获取启用的洗护服务列表
  getGroomingServiceList(params = {}) {
    return this.request({
      url: '/api/grooming-services/list',
      method: 'GET',
      data: params
    })
  }

  // 根据ID获取洗护服务详情
  getGroomingServiceById(id) {
    return this.request({
      url: `/api/grooming-services/${id}`,
      method: 'GET'
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
      url: '/api/user/wxapp/authorize',
      method: 'POST',
      data: params
    })
  }

  // 手机号密码登录
  loginByPhone(phone, password) {
    return this.request({
      url: '/api/user/login',
      method: 'POST',
      data: { phone, password }
    })
  }

  // 微信登录
  loginByWechat(code) {
    return this.request({
      url: '/api/user/wxapp/login',
      method: 'POST',
      data: { code }
    })
  }

  // 用户注册（smsCode 在有短信服务时必填；勿传字符串 "undefined"）
  register(phone, password, nickname, smsCode) {
    const data = {
      phone: String(phone),
      password: String(password),
      nickname: String(nickname || '')
    }
    if (smsCode != null && smsCode !== '' && String(smsCode) !== 'undefined') {
      data.smsCode = String(smsCode).trim()
    }
    return this.request({
      url: '/api/user/register',
      method: 'POST',
      data
    })
  }

  // 忘记密码：通过手机号+验证码重置密码
  resetPassword(phone, smsCode, newPassword) {
    return this.request({
      url: '/api/user/forgot-password',
      method: 'POST',
      data: {
        phone: String(phone),
        smsCode: String(smsCode),
        newPassword: String(newPassword)
      }
    })
  }

  // 修改密码（登录后，需携带 token）
  changePassword(oldPassword, newPassword) {
    return this.request({
      url: '/api/user/change-password',
      method: 'POST',
      data: {
        oldPassword: String(oldPassword),
        newPassword: String(newPassword)
      }
    })
  }

  // 绑定手机号（使用其它号码时：需先发验证码，再传 phone + smsCode）
  bindPhone(phone, smsCode) {
    return this.request({
      url: '/api/user/bind-phone',
      method: 'POST',
      data: {
        phone: String(phone),
        smsCode: String(smsCode)
      }
    })
  }

  // 解绑手机号（需登录）。解绑后该号码可被其他账号绑定，当前账号可再绑定其他号码
  unbindPhone() {
    return this.request({
      url: '/api/user/unbind-phone',
      method: 'POST'
    })
  }

  // 提交投诉举报（可不登录）
  submitComplaint(type, content, contactInfo, images) {
    return this.request({
      url: '/api/complaint/submit',
      method: 'POST',
      data: {
        type: type || '其他',
        content: String(content),
        contactInfo: contactInfo ? String(contactInfo) : '',
        images: Array.isArray(images) && images.length ? images : null
      },
      showLoading: true
    })
  }

  // 注销账号
  deleteAccount() {
    return this.request({
      url: '/api/user/delete',
      method: 'POST'
    })
  }

  // 获取用户详情
  getUserDetail(token) {
    return this.request({
      url: '/api/user/detail',
      method: 'GET',
      data: { token }
    })
  }

  // 获取用户资产
  getUserAmount(token) {
    return this.request({
      url: '/api/user/amount',
      method: 'GET',
      data: { token }
    })
  }

  // 获取最新公告
  getLastNotice(type, showLoading = true) {
    return this.request({
      url: '/api/notice/last-one',
      method: 'GET',
      data: { type },
      showLoading: showLoading
    })
  }
  // 获取公告列表（系统公告页用，status=1 已发布）
  getNoticeList(type, status = 1, limit = 50, showLoading = false) {
    return this.request({
      url: '/api/notice/list',
      method: 'GET',
      data: { type, status, limit },
      showLoading
    })
  }

  // ========== 通知中心（预约提醒、订单状态、系统公告、社区互动） ==========
  getMyNotifications(pageNo = 1, pageSize = 20, showLoading = false) {
    return this.request({
      url: '/api/notifications/my',
      method: 'GET',
      data: { pageNo, pageSize },
      showLoading
    })
  }
  getNotificationUnreadCount(showLoading = false) {
    return this.request({
      url: '/api/notifications/unread-count',
      method: 'GET',
      showLoading
    })
  }
  markNotificationRead(id, showLoading = false) {
    return this.request({
      url: `/api/notifications/${id}/read`,
      method: 'POST',
      showLoading
    })
  }
  markAllNotificationsRead(showLoading = false) {
    return this.request({
      url: '/api/notifications/read-all',
      method: 'POST',
      showLoading
    })
  }
  getUnreadNotices(limit = 20, showLoading = false) {
    return this.request({
      url: '/api/notifications/notices-unread',
      method: 'GET',
      data: { limit },
      showLoading
    })
  }
  markNoticeAsRead(noticeId, showLoading = false) {
    return this.request({
      url: `/api/notifications/notice/${noticeId}/read`,
      method: 'POST',
      showLoading
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
  getGoodsDetail(id, showLoading = true) {
    return this.request({
      url: `/api/product/${id}`,
      method: 'GET',
      showLoading: showLoading
    })
  }

  // 获取商品评价
  getProductReviews(productId) {
    return this.request({
      url: `/api/product/${productId}/reviews`,
      method: 'GET'
    })
  }

  // 提交商品评价
  submitReview(reviewData) {
    return this.request({
      url: '/api/reviews',
      method: 'POST',
      data: reviewData
    })
  }

  // 获取我的卡券
  getMyCards(token) {
    return this.request({
      url: '/api/card/my',
      method: 'GET',
      data: { token }
    })
  }

  // 获取购物车信息
  getShoppingCartInfo(token, type) {
    return this.request({
      url: '/api/shopping-cart/info',
      method: 'GET',
      data: { token, type }
    })
  }

  // ==================== 用户相关接口 ====================

  // 发送短信验证码
  sendSmsCode(phone) {
    return this.request({
      url: '/api/sms/send',
      method: 'POST',
      data: { phone }
    })
  }

  // 用户登录
  login(loginData) {
    return this.request({
      url: '/api/user/login',
      method: 'POST',
      data: loginData
    })
  }

  // 获取当前用户信息（params.showLoading 为 false 时不显示全局加载中，用于「我的」页静默刷新）
  getCurrentUser(params = {}) {
    const { showLoading = true, ...rest } = params
    let url = '/api/user/current'
    const queryParams = []
    if (rest.userId != null && rest.userId !== undefined && rest.userId !== '') {
      const userId = typeof rest.userId === 'string' ? parseInt(rest.userId) : rest.userId
      if (!isNaN(userId) && userId > 0) {
        queryParams.push(`userId=${userId}`)
      }
    }
    if (rest.username) {
      queryParams.push(`username=${encodeURIComponent(rest.username)}`)
    }
    if (queryParams.length > 0) {
      url += '?' + queryParams.join('&')
    }
    return this.request({
      url: url,
      method: 'GET',
      showLoading: showLoading
    })
  }

  // 更新用户信息
  updateUser(userData) {
    return this.request({
      url: '/api/user/update',
      method: 'POST',
      data: userData
    })
  }

  // 更新用户资料（个人资料页面使用）
  updateUserProfile(userData) {
    return this.request({
      url: '/api/user/update',
      method: 'POST',
      data: userData
    })
  }

  // 修改账号（格式：字母或下划线开头，6-20位，仅字母数字下划线减号）
  changeUsername(newUsername) {
    return this.request({
      url: '/api/user/change-username',
      method: 'POST',
      data: { newUsername }
    })
  }

  // 检查当前账号是否已设置密码（微信登录可能无密码）
  hasPassword() {
    return this.request({
      url: '/api/user/has-password',
      method: 'GET'
    })
  }

  // 验证当前账号登录密码（修改账号前）
  verifyPassword(password) {
    return this.request({
      url: '/api/user/verify-password',
      method: 'POST',
      data: { password }
    })
  }

  // 获取微信手机号（通过code换取）
  getPhoneNumber(data) {
    return this.request({
      url: '/api/user/getPhoneNumber',
      method: 'POST',
      data: data
    })
  }

  // 获取用户统计信息（关注、粉丝、动态、获赞）
  getUserStats(userId = null, showLoading = true) {
    let url = '/api/user/stats'
    if (userId) {
      url += `?userId=${userId}`
    }
    return this.request({
      url: url,
      method: 'GET',
      showLoading: showLoading
    })
  }

  // 获取用户宠物列表
  getPetList(userId = null, showLoading = true) {
    const params = { pageNo: 1, pageSize: 100 }
    if (userId) {
      params.userId = userId
    }
    return this.request({
      url: '/api/pets/page',
      method: 'GET',
      data: params,
      showLoading: showLoading
    })
  }

  // ==================== 商品相关接口 ====================

  // 分页查询商品（可选 storeId 筛选店铺商品；options.showLoading 控制 loading）
  getProductPage(pageNo = 1, pageSize = 10, options = {}) {
    const { storeId, showLoading = true, ...extra } = options && typeof options === 'object' ? options : {}
    const data = { pageNo, pageSize, ...extra }
    if (storeId != null && storeId !== '') {
      data.storeId = storeId
    }
    return this.request({
      url: '/api/product/page',
      method: 'GET',
      data,
      showLoading
    })
  }

  /** C 端：店铺基本信息（公开接口） */
  getStoreInfo(storeId, showLoading = true) {
    return this.request({
      url: '/api/store/info',
      method: 'GET',
      data: { storeId },
      showLoading
    })
  }

  // 获取热门商品
  getHotProducts(limit = 10, showLoading = true) {
    return this.request({
      url: '/api/product/hot',
      method: 'GET',
      data: { limit },
      showLoading: showLoading,
      timeout: 90000,
      retryOnTimeout: true
    })
  }

  // 获取推荐商品
  getRecommendProducts(limit = 4, showLoading = true) {
    return this.request({
      url: '/api/product/recommend',
      method: 'GET',
      data: { limit },
      showLoading: showLoading
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
      url: `/api/cart/count?userId=${userId}`,
      method: 'GET'
    })
  }

  // ==================== 宠物相关接口 ====================

  // 分页查询宠物
  getPetPage(pageNo = 1, pageSize = 10, userId, showLoading = true) {
    return this.request({
      url: '/api/pets/page',
      method: 'GET',
      data: { pageNo, pageSize, userId },
      showLoading: showLoading
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

  // 备注：疫苗记录/健康状况/就医记录（宠物医疗模块）已移除

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
    // 移除更新预约状态API日志以提升性能
    return this.request({
      url: `/api/door-cleaning/${id}/status?status=${status}`,
      method: 'PUT'
    })
  }

  // 获取用户预约列表（上门铲屎）
  getUserAppointments(userId, showLoading = true) {
    return this.request({
      url: `/api/door-cleaning/user/list/${userId}`,
      method: 'GET',
      showLoading: showLoading
    })
  }

  // 获取用户全部预约（统一接口：上门铲屎+医院+洗护，一次请求，避免三个接口都失败导致整页加载失败）
  getUserAppointmentsUnified(userId, showLoading = true) {
    return this.request({
      url: `/api/user-appointments/list/${userId}`,
      method: 'GET',
      showLoading: showLoading
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

  // 提交服务评价（对已完成订单的服务人员评分）
  submitAppointmentRating(data) {
    return this.request({
      url: '/api/appointment/rating',
      method: 'POST',
      data
    })
  }

  // 检查是否已评价
  checkAppointmentRated(appointmentType, appointmentId) {
    return this.request({
      url: `/api/appointment/rating/check?appointmentType=${encodeURIComponent(appointmentType)}&appointmentId=${appointmentId}`,
      method: 'GET',
      showLoading: false
    })
  }

  // ==================== 服务预约时间与智能分配相关接口 ====================

  // 获取可预约时间段（storeId 可选，传入时仅返回该门店的排班）
  getAvailableTimeSlots(serviceType, date, storeId, showLoading = true) {
    const params = { serviceType, date }
    if (storeId != null && storeId !== '' && !isNaN(Number(storeId))) {
      params.storeId = Number(storeId)
    }
    return this.request({
      url: '/api/schedule/available',
      method: 'GET',
      data: params,
      showLoading: showLoading
    })
  }

  // 获取服务人员列表
  getServiceMembers(serviceType) {
    return this.request({
      url: '/api/service-member/list',
      method: 'GET',
      data: { serviceType }
    })
  }

  // 自动分配服务人员
  autoAssignMember(serviceType, date, timeSlot) {
    return this.request({
      url: '/api/service-member/auto-assign',
      method: 'POST',
      data: { serviceType, date, timeSlot }
    })
  }

  // 获取服务人员工作量
  getMemberWorkload(memberId, date) {
    return this.request({
      url: `/api/service-member/workload/${memberId}`,
      method: 'GET',
      data: { date }
    })
  }

  // 获取服务人员日程
  getMemberSchedule(memberId, date) {
    return this.request({
      url: `/api/schedule/member/${memberId}`,
      method: 'GET',
      data: { date }
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
  getUserHospitalAppointments(userId, showLoading = true) {
    return this.request({
      url: `/api/hospital-appointments/user/list/${userId}`,
      method: 'GET',
      showLoading: showLoading
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
      data: { current: pageNo, size: pageSize }
    })
  }

  // 获取用户宠物洗护预约列表
  getUserGroomingAppointments(userId, showLoading = true) {
    return this.request({
      url: `/api/grooming-appointments/user/list/${userId}`,
      method: 'GET',
      showLoading: showLoading
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

  // 获取洗护服务展示图
  getGroomingBanner(position = 'grooming-page-top') {
    return this.request({
      url: `/api/grooming-banners/position/${position}`,
      method: 'GET'
    })
  }

  // 预约变更申请：用户提交变更（提交后预约状态为「变更待确认」，工作人员同意后生效）
  submitAppointmentChangeRequest(data) {
    return this.request({
      url: '/api/appointment-change-request/submit',
      method: 'POST',
      data
    })
  }

  // 按预约查询待处理变更（用于展示「变更待确认」）
  getPendingChangeByAppointment(appointmentType, appointmentId) {
    const q = `appointmentType=${encodeURIComponent(appointmentType || '')}&appointmentId=${appointmentId || ''}`
    return this.request({
      url: `/api/appointment-change-request/pending-by-appointment?${q}`,
      method: 'GET',
      showLoading: false
    })
  }

  // 查询某预约是否已有任意变更记录（用于隐藏「申请变更」按钮）
  hasAnyAppointmentChangeRequest(appointmentType, appointmentId) {
    const q = `appointmentType=${encodeURIComponent(appointmentType || '')}&appointmentId=${appointmentId || ''}`
    return this.request({
      url: `/api/appointment-change-request/has-any?${q}`,
      method: 'GET',
      showLoading: false
    })
  }

  // 用户取消变更申请（仅工作人员未确认时可取消）
  cancelAppointmentChangeRequest(data) {
    return this.request({
      url: '/api/appointment-change-request/cancel',
      method: 'POST',
      data
    })
  }

  // 取消预约申请：已确认订单填写原因提交，工作人员同意后取消（含 0-2 小时违约金）
  submitCancellationRequest(data) {
    return this.request({
      url: '/api/appointment-cancellation-request/submit',
      method: 'POST',
      data
    })
  }

  getPendingCancellationByAppointment(appointmentType, appointmentId) {
    const q = `appointmentType=${encodeURIComponent(appointmentType || '')}&appointmentId=${appointmentId || ''}`
    return this.request({
      url: `/api/appointment-cancellation-request/pending-by-appointment?${q}`,
      method: 'GET',
      showLoading: false
    })
  }

  // ==================== 宠物领养预约相关 API ====================

  // 创建宠物领养预约
  createAdoptionAppointment(appointmentData) {
    return this.request({
      url: '/api/adoption-appointments/create',
      method: 'POST',
      data: appointmentData
    })
  }

  // 获取宠物领养预约列表
  getAdoptionAppointmentPage(pageNo = 1, pageSize = 10) {
    return this.request({
      url: '/api/adoption-appointments/page',
      method: 'GET',
      data: { pageNo, pageSize }
    })
  }

  // 获取用户宠物领养预约列表
  getUserAdoptionAppointments(userId) {
    return this.request({
      url: `/api/adoption-appointments/user/list/${userId}`,
      method: 'GET'
    })
  }

  // 更新宠物领养预约状态
  updateAdoptionAppointmentStatus(id, status) {
    return this.request({
      url: `/api/adoption-appointments/${id}/status?status=${status}`,
      method: 'PUT'
    })
  }

  // 获取宠物领养预约详情
  getAdoptionAppointmentDetail(id) {
    return this.request({
      url: `/api/adoption-appointments/${id}`,
      method: 'GET'
    })
  }

  // ==================== 待领养宠物相关 API ====================

  // 获取待领养宠物列表
  getAdoptionPets() {
    return this.request({
      url: '/api/pet-adoption/list',
      method: 'GET'
    })
  }

  // 获取待领养宠物详情
  getAdoptionPetDetail(id) {
    return this.request({
      url: `/api/pet-adoption/${id}`,
      method: 'GET'
    })
  }

  // 获取领养服务背景图
  getAdoptionBackground() {
    return this.request({
      url: '/api/adoption-background/position/adoption-page-background',
      method: 'GET'
    })
  }


  // ==================== 订单相关接口 ====================

  // 获取订单列表（已合并到下面的方法，保留此方法以兼容旧代码）
  // getOrderList(status) {
  //   return this.request({
  //     url: '/api/orders/list',
  //     method: 'GET',
  //     data: { status }
  //   })
  // }

  // 获取订单详情
  getOrderDetail(orderId) {
    return this.request({
      url: `/api/orders/${orderId}`,
      method: 'GET',
      timeout: 90000,
      retryOnTimeout: true
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
      url: `/api/orders/${orderId}/status?status=${status}`,
      method: 'PUT'
    })
  }

  // 删除订单（软删除：服务端设置为 deleted，跨设备生效）
  deleteOrder(orderId) {
    return this.request({
      url: `/api/orders/${orderId}/status?status=deleted`,
      method: 'PUT'
    })
  }

  // 获取订单物流信息
  getOrderLogistics(orderId) {
    return this.request({
      url: `/api/orders/${orderId}/logistics`,
      method: 'GET',
      timeout: 90000,
      retryOnTimeout: true
    })
  }

  // 更新订单物流信息（管理员用）
  updateOrderLogistics(orderId, logisticsData) {
    return this.request({
      url: `/api/orders/${orderId}/logistics`,
      method: 'PUT',
      data: logisticsData
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

  // ==================== 工作人员相关接口 ====================

  // 工作人员登录（使用 Admin 表，username + password）
  staffLogin(username, password) {
    return this.request({
      url: '/api/admin/login',
      method: 'POST',
      data: { username, password },
      showLoading: true
    })
  }

  // 核销验证（使用工作人员 token，用于小程序端工作人员核销）
  verifyCodeForStaff(verifyCode) {
    const staffToken = uni.getStorageSync('staffToken')
    return this.request({
      url: '/api/verify/verify-code',
      method: 'POST',
      data: { verifyCode },
      authToken: staffToken
    })
  }

  // 工作人员：同意预约变更请求
  approveChangeRequest(changeRequestId) {
    const staffToken = uni.getStorageSync('staffToken')
    return this.request({
      url: `/api/appointment-change-request/${changeRequestId}/approve`,
      method: 'POST',
      authToken: staffToken
    })
  }

  // 工作人员：拒绝预约变更请求
  rejectChangeRequest(changeRequestId) {
    const staffToken = uni.getStorageSync('staffToken')
    return this.request({
      url: `/api/appointment-change-request/${changeRequestId}/reject`,
      method: 'POST',
      authToken: staffToken
    })
  }

  // 工作人员：同意取消预约请求
  approveCancellationRequest(cancellationRequestId) {
    const staffToken = uni.getStorageSync('staffToken')
    return this.request({
      url: `/api/appointment-cancellation-request/${cancellationRequestId}/approve`,
      method: 'POST',
      authToken: staffToken
    })
  }

  // 工作人员：拒绝取消预约请求
  rejectCancellationRequest(cancellationRequestId, rejectReason) {
    const staffToken = uni.getStorageSync('staffToken')
    return this.request({
      url: `/api/appointment-cancellation-request/${cancellationRequestId}/reject`,
      method: 'POST',
      data: { rejectReason },
      authToken: staffToken
    })
  }

  // 按预约查询待处理变更请求
  getPendingChangeByAppointment(appointmentType, appointmentId) {
    const q = `appointmentType=${encodeURIComponent(appointmentType || '')}&appointmentId=${appointmentId || ''}`
    return this.request({
      url: `/api/appointment-change-request/pending-by-appointment?${q}`,
      method: 'GET',
      showLoading: false
    })
  }

  // 按预约查询待处理取消请求
  getPendingCancellationByAppointment(appointmentType, appointmentId) {
    const q = `appointmentType=${encodeURIComponent(appointmentType || '')}&appointmentId=${appointmentId || ''}`
    return this.request({
      url: `/api/appointment-cancellation-request/pending-by-appointment?${q}`,
      method: 'GET',
      showLoading: false
    })
  }

  // 工作人员：获取我的工单列表（与管理员后台规则一致，仅自己的工单）
  getStaffWorkOrders() {
    const staffToken = uni.getStorageSync('staffToken')
    return this.request({
      url: '/api/admin/self/work-orders',
      method: 'GET',
      authToken: staffToken,
      showLoading: false
    })
  }

  // 工作人员：获取我的真实评价列表（来自后端评价表）
  getStaffMyReviews() {
    const staffToken = uni.getStorageSync('staffToken')
    return this.request({
      url: '/api/admin/self/reviews',
      method: 'GET',
      authToken: staffToken,
      showLoading: false
    })
  }

  // 工作人员：更新预约状态（确认/完成/拒绝；拒绝时可传 rejectReason，用户端会展示）
  updateAppointmentStatusForStaff(serviceType, id, status, rejectReason) {
    const staffToken = uni.getStorageSync('staffToken')
    const data = { id, serviceType, status }
    if (rejectReason != null && String(rejectReason).trim() !== '') {
      data.rejectReason = String(rejectReason).trim()
    }
    return this.request({
      url: '/api/admin/self/work-orders/status',
      method: 'POST',
      data,
      authToken: staffToken
    })
  }

  // ==================== 核销相关接口 ====================

  // 核销验证（通用，使用当前 token）
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

  // ==================== 服务门店相关接口 ====================

  // 获取所有营业中的门店
  getAllStores() {
    return this.request({
      url: '/api/stores/all',
      method: 'GET'
    })
  }

  // 根据服务类型获取门店列表
  getStoresByService(serviceType) {
    return this.request({
      url: `/api/stores/by-service/${serviceType}`,
      method: 'GET'
    })
  }

  // 获取默认门店
  getDefaultStore() {
    return this.request({
      url: '/api/stores/default',
      method: 'GET'
    })
  }

  // ==================== 地区选择相关接口 ====================
  
  // 获取所有省份
  getProvinces() {
    return this.request({
      url: '/api/regions/provinces',
      method: 'GET'
    })
  }

  // 根据省份代码获取城市
  getCities(parentCode) {
    return this.request({
      url: '/api/regions/cities',
      method: 'GET',
      data: { parentCode }
    })
  }

  // 根据城市代码获取区县
  getDistricts(parentCode) {
    return this.request({
      url: '/api/regions/districts',
      method: 'GET',
      data: { parentCode }
    })
  }

  // 根据父级代码获取子级地区
  getChildren(parentCode) {
    return this.request({
      url: '/api/regions/children',
      method: 'GET',
      data: { parentCode }
    })
  }

  // 根据代码获取地区信息
  getRegionByCode(code) {
    return this.request({
      url: `/api/regions/${code}`,
      method: 'GET'
    })
  }

  // 获取订单列表（extra.timeout 可加长超时，避免弱网/慢接口被误判为「无订单」）
  getOrderList(status = null, userId = null, showLoading = true, extra = {}) {
    let url = '/api/orders/list'
    const params = []
    
    // 如果传入的是字符串且是状态值，则作为status处理
    if (status && typeof status === 'string' && ['all', 'pending', 'paid', 'shipped', 'completed', 'cancelled'].includes(status)) {
      if (status !== 'all') {
        params.push(`status=${status}`)
      }
    } else if (status && typeof status === 'number') {
      // 如果传入的是数字，可能是userId（兼容旧代码）
      params.push(`userId=${status}`)
    }
    
    // 如果明确传入了userId，使用userId
    if (userId) {
      // 移除可能存在的userId参数，使用新的userId
      const filteredParams = params.filter(p => !p.startsWith('userId='))
      filteredParams.push(`userId=${userId}`)
      params.length = 0
      params.push(...filteredParams)
    }
    
    if (params.length > 0) {
      url += '?' + params.join('&')
    }
    
    return this.request({
      url: url,
      method: 'GET',
      showLoading: showLoading,
      timeout: extra.timeout != null ? extra.timeout : 60000,
      header: { 'Cache-Control': 'no-store' }
    })
  }

  // 获取订单详情
  getOrderDetail(orderId) {
    return this.request({
      url: `/api/orders/${orderId}`,
      method: 'GET',
      timeout: 90000,
      retryOnTimeout: true
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
      url: `/api/orders/${orderId}/status?status=${status}`,
      method: 'PUT'
    })
  }

  // 删除订单（软删除：服务端设置为 deleted，跨设备生效）
  deleteOrder(orderId) {
    return this.request({
      url: `/api/orders/${orderId}/status?status=deleted`,
      method: 'PUT'
    })
  }

  // 获取订单物流信息
  getOrderLogistics(orderId) {
    return this.request({
      url: `/api/orders/${orderId}/logistics`,
      method: 'GET',
      timeout: 90000,
      retryOnTimeout: true
    })
  }

  // 更新订单物流信息（管理员用）
  updateOrderLogistics(orderId, logisticsData) {
    return this.request({
      url: `/api/orders/${orderId}/logistics`,
      method: 'PUT',
      data: logisticsData
    })
  }

  // 申请退款
  requestRefund(orderId, refundData) {
    return this.request({
      url: `/api/orders/${orderId}/refund`,
      method: 'POST',
      data: refundData
    })
  }

  // 取消退款申请（用户侧）
  cancelRefund(orderId) {
    return this.request({
      url: `/api/orders/${orderId}/refund/cancel`,
      method: 'PUT'
    })
  }

  // ==================== 签到任务相关接口 ====================
  
  // 获取签到数据（余额、连续签到天数、签到日历）
  getSignInData() {
    return this.request({
      url: '/api/signin/data',
      method: 'GET'
    })
  }

  // 执行签到
  doSignIn() {
    return this.request({
      url: '/api/signin/do',
      method: 'POST'
    })
  }

  // 获取每日任务列表
  getDailyTasks() {
    return this.request({
      url: '/api/tasks/daily',
      method: 'GET'
    })
  }

  // 领取任务奖励
  claimTaskReward(taskId) {
    return this.request({
      url: '/api/tasks/claim',
      method: 'POST',
      data: { taskId }
    })
  }

  // 获取任务进度
  getTaskProgress() {
    return this.request({
      url: '/api/tasks/progress',
      method: 'GET'
    })
  }

  // ==================== 积分商城相关接口 ====================
  
  // 获取积分商城产品列表
  getPointsMallProducts(extra = {}) {
    return this.request({
      url: '/api/points/products',
      method: 'GET',
      showLoading: false, // 由页面自己控制loading
      timeout: extra.timeout
    })
  }

  // 兑换积分商品
  exchangeProduct(data) {
    // 兼容旧版本：如果传入的是数字，转换为对象
    if (typeof data === 'number' || (typeof data === 'string' && /^\d+$/.test(data))) {
      data = { productId: data }
    }
    return this.request({
      url: '/api/points/exchange',
      method: 'POST',
      data: data
    })
  }

  // 获取兑换记录（可选 userId：与订单列表一致；extra.timeout 可选）
  getExchangeHistory(page = 1, size = 20, showLoading = true, userId = null, extra = {}) {
    let url = `/api/points/exchange/history?page=${page}&size=${size}`
    if (userId != null && userId !== '' && userId !== 'undefined' && userId !== 'null') {
      url += `&userId=${userId}`
    }
    return this.request({
      url,
      method: 'GET',
      showLoading: showLoading,
      timeout: extra.timeout
    })
  }

  // 获取积分记录
  getPointsRecords(userId = null, type = 'earn') {
    let url = '/api/user/points/records'
    // GET 请求参数拼接到 URL
    const params = []
    if (type) {
      params.push(`type=${type}`)
    }
    if (userId) {
      params.push(`userId=${userId}`)
    }
    if (params.length > 0) {
      url += '?' + params.join('&')
    }
    return this.request({
      url: url,
      method: 'GET'
    })
  }

  // 获取积分统计
  getPointsStatistics(userId = null) {
    let url = '/api/user/points/statistics'
    // GET 请求参数拼接到 URL
    if (userId) {
      url += `?userId=${userId}`
    }
    return this.request({
      url: url,
      method: 'GET'
    })
  }

  // ==================== 社区相关接口 ====================
  
  // 获取帖子列表
  getPostList(params = {}) {
    return this.request({
      url: '/api/community/posts',
      method: 'GET',
      data: params
    })
  }

  // 获取帖子详情（默认不弹全局 loading，由详情页自行展示，避免与 uni.showLoading 叠加导致难关闭）
  getPostDetail(postId, userId = null, showLoading = false) {
    const params = userId ? { userId } : {}
    return this.request({
      url: `/api/community/posts/${postId}`,
      method: 'GET',
      data: params,
      showLoading: showLoading
    })
  }

  // 发布帖子
  publishPost(postData) {
    return this.request({
      url: '/api/community/posts',
      method: 'POST',
      data: postData,
      // 发布流程可能包含服务端审核/转码等耗时，避免默认 30s 误判超时
      timeout: 120000
    })
  }

  // ==================== 标签相关接口 ====================
  
  // 获取热门标签
  getHotTags(limit = 10) {
    return this.request({
      url: `/api/tag/hot?limit=${limit}`,
      method: 'GET',
      showLoading: false // 这个接口不需要loading，因为不是关键操作
    })
  }

  // 根据标签查询帖子
  getPostsByTag(tag, page = 1, size = 20) {
    return this.request({
      url: `/api/tag/posts?tag=${encodeURIComponent(tag)}&page=${page}&size=${size}`,
      method: 'GET',
      showLoading: false
    })
  }

  // 获取推荐帖子
  getRecommendPosts(limit = 10) {
    return this.request({
      url: `/api/tag/recommend?limit=${limit}`,
      method: 'GET',
      showLoading: false
    })
  }

  // 更新帖子
  updatePost(postId, postData, userId) {
    return this.request({
      url: `/api/community/posts/${postId}`,
      method: 'PUT',
      data: { ...postData, userId }
    })
  }

  // 删除帖子
  deletePost(postId, userId) {
    return this.request({
      url: `/api/community/posts/${postId}?userId=${userId}`,
      method: 'DELETE'
    })
  }

  // 点赞帖子
  likePost(postId, userId) {
    return this.request({
      url: `/api/community/posts/${postId}/like?userId=${userId}`,
      method: 'POST'
    })
  }

  // 取消点赞帖子
  unlikePost(postId, userId) {
    return this.request({
      url: `/api/community/posts/${postId}/like?userId=${userId}`,
      method: 'DELETE'
    })
  }

  // 获取热门帖子
  getHotPosts(limit = 10) {
    return this.request({
      url: '/api/community/posts/hot',
      method: 'GET',
      data: { limit }
    })
  }

  // 视频推荐流（类抖音）：登录用户走个性化，未登录走热度分页
  getVideoFeed(page = 1, size = 10, userId = null) {
    const data = { page, size }
    if (userId !== null && userId !== undefined && userId !== '') {
      data.userId = userId
    }
    return this.request({
      url: '/api/community/posts/video-feed',
      method: 'GET',
      data,
      showLoading: false
    })
  }

  // 获取关注用户的帖子
  getFollowingPosts(userId, params = {}) {
    if (!userId || userId === null || userId === undefined) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    return this.request({
      url: '/api/community/posts/following',
      method: 'GET',
      data: { userId, ...params }
    })
  }

  // 获取帖子评论列表（showLoading 可选，列表页/详情页批量请求时传 false 避免长时间 loading）
  getPostComments(postId, userId, showLoading = true) {
    let url = `/api/community/posts/${postId}/comments`
    if (userId) {
      url += `?userId=${userId}`
    }
    return this.request({
      url: url,
      method: 'GET',
      showLoading: !!showLoading
    })
  }

  // 获取评论的回复列表（showLoading 可选，详情页按评论逐条请求时传 false 避免长时间 loading）
  getCommentReplies(commentId, userId, showLoading = true) {
    let url = `/api/community/comments/${commentId}/replies`
    if (userId) {
      url += `?userId=${userId}`
    }
    return this.request({
      url: url,
      method: 'GET',
      showLoading: !!showLoading
    })
  }

  // 获取@提及用户列表（抖音式：输入@后弹出选择）
  getMentionUsers(postId, userId, showLoading = false) {
    return this.request({
      url: `/api/community/posts/${postId}/mention-users`,
      method: 'GET',
      data: { userId },
      showLoading: !!showLoading
    })
  }

  // 添加评论
  addComment(postId, commentData) {
    return this.request({
      url: `/api/community/posts/${postId}/comments`,
      method: 'POST',
      data: commentData
    })
  }

  // 删除评论
  deleteComment(commentId, userId) {
    return this.request({
      url: `/api/community/comments/${commentId}?userId=${userId}`,
      method: 'DELETE'
    })
  }

  // 点赞评论
  likeComment(commentId, userId) {
    return this.request({
      url: `/api/community/comments/${commentId}/like?userId=${userId}`,
      method: 'POST'
    })
  }

  // 取消点赞评论
  unlikeComment(commentId, userId) {
    return this.request({
      url: `/api/community/comments/${commentId}/like?userId=${userId}`,
      method: 'DELETE'
    })
  }

  // 关注用户
  followUser(followingId, followerId) {
    return this.request({
      url: `/api/community/follow?followingId=${followingId}&followerId=${followerId}`,
      method: 'POST'
    })
  }

  // 取消关注用户
  unfollowUser(followingId, followerId) {
    return this.request({
      url: `/api/community/follow?followingId=${followingId}&followerId=${followerId}`,
      method: 'DELETE'
    })
  }

  // 获取用户关注列表
  getFollowingList(userId) {
    return this.request({
      url: `/api/community/users/${userId}/following`,
      method: 'GET'
    })
  }

  // 获取用户粉丝列表
  getFollowerList(userId) {
    return this.request({
      url: `/api/community/users/${userId}/followers`,
      method: 'GET'
    })
  }

  // 检查关注状态
  checkFollowStatus(followerId, followingId) {
    return this.request({
      url: '/api/community/follow/status',
      method: 'GET',
      data: { followerId, followingId }
    })
  }

  // 获取用户关注的用户列表
  getFollowedUsers(userId, showLoading = true) {
    if (!userId || userId === null || userId === undefined) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    return this.request({
      url: `/api/community/users/${userId}/followed`,
      method: 'GET',
      showLoading: showLoading
    })
  }

  // 获取推荐用户列表（社区关注页）
  getRecommendedUsers(userId, showLoading = true) {
    if (!userId || userId === null || userId === undefined) {
      return Promise.reject(new Error('用户ID不能为空'))
    }
    return this.request({
      url: `/api/community/users/${userId}/recommended`,
      method: 'GET',
      showLoading: showLoading
    })
  }

  // 获取帖子Feed（置顶+热门+最新+关注区+发现区）
  getPostFeed(params = {}) {
    return this.request({
      url: '/api/community/posts/feed',
      method: 'GET',
      data: params
    })
  }

  /**
   * 发现页规则型推荐（小红书风格）
   * 服务端按 score = 兴趣匹配(+5) + 点赞×2 + 收藏×3 + 评论×4 + 浏览×0.2 + 新帖24h(+20) 排序
   */
  getDiscoverRecommend(params = {}) {
    const data = { page: params.page ?? 1, size: params.size ?? 20 }
    if (params.userId != null && params.userId !== '') {
      data.userId = params.userId
    }
    if (params.tag != null && params.tag !== '') {
      data.tag = params.tag
    }
    return this.request({
      url: '/api/community/posts/recommend',
      method: 'GET',
      data
    })
  }

  // 获取发现页标签
  getDiscoverTags() {
    return this.request({
      url: '/api/tag/all',
      method: 'GET',
      showLoading: false
    })
  }

  // 协同过滤推荐（预留接口）
  getCollaborativeFiltering(userId, limit = 20) {
    return this.request({
      url: '/api/recommendation/collaborative-filtering',
      method: 'GET',
      data: { userId, limit }
    })
  }

  // 获取用户兴趣标签
  getUserInterests(userId) {
    return this.request({
      url: '/api/recommendation/user-interests',
      method: 'GET',
      data: { userId }
    })
  }

  // 获取最新帖子（分页）
  getLatestPosts(params = {}) {
    return this.request({
      url: '/api/community/posts',
      method: 'GET',
      data: {
        page: params.page || 1,
        size: params.size || 10,
        category: params.category || null
      }
    })
  }

  // 首页搜索 - 商品和服务
  searchHome(keyword) {
    return this.request({
      url: '/api/search/home',
      method: 'GET',
      data: { keyword }
    })
  }

  // 社区搜索 - 帖子
  searchCommunity(keyword) {
    return this.request({
      url: '/api/search/community',
      method: 'GET',
      data: { keyword }
    })
  }

  // 获取热搜关键词
  getHotKeywords() {
    return this.request({
      url: '/api/search/hot',
      method: 'GET'
    })
  }

  // 收藏帖子
  collectPost(postId, userId) {
    return this.request({
      url: `/api/community/posts/${postId}/collect?userId=${userId}`,
      method: 'POST'
    })
  }

  // 取消收藏帖子
  uncollectPost(postId, userId) {
    return this.request({
      url: `/api/community/posts/${postId}/collect?userId=${userId}`,
      method: 'DELETE'
    })
  }

  // 检查收藏状态
  checkCollectStatus(postId, userId) {
    return this.request({
      url: `/api/community/posts/${postId}/collect/status?userId=${userId}`,
      method: 'GET'
    })
  }

  // 获取收藏的帖子列表
  getCollectedPosts(params = {}) {
    const { showLoading = true, ...rest } = params
    return this.request({
      url: '/api/community/posts/collected',
      method: 'GET',
      data: {
        userId: rest.userId,
        page: rest.page || 1,
        size: rest.size || 10,
        type: rest.type || null
      },
      showLoading: showLoading
    })
  }

  // ========== 商品收藏相关接口 ==========

  // 收藏商品
  collectProduct(productId, userId) {
    return this.request({
      url: `/api/product/${productId}/collect?userId=${userId}`,
      method: 'POST'
    })
  }

  // 取消收藏商品
  uncollectProduct(productId, userId) {
    return this.request({
      url: `/api/product/${productId}/collect?userId=${userId}`,
      method: 'DELETE'
    })
  }

  // 检查商品收藏状态
  checkProductCollectStatus(productId, userId) {
    return this.request({
      url: `/api/product/${productId}/collect/status?userId=${userId}`,
      method: 'GET'
    })
  }

  // 获取收藏的商品列表
  getCollectedProducts(params = {}) {
    const { showLoading = true, ...rest } = params
    return this.request({
      url: '/api/product/collected',
      method: 'GET',
      data: {
        userId: rest.userId,
        page: rest.page || 1,
        size: rest.size || 10
      },
      showLoading: showLoading
    })
  }

  // ========== 私信相关接口 ==========
  
  // 获取私信列表
  getPrivateMessages(params = {}) {
    const queryParams = {
      userId: params.userId,
      page: params.page || 1,
      size: params.size || 20
    }
    
    // 如果提供了conversationId，使用它（用于客服消息）
    if (params.conversationId) {
      queryParams.conversationId = params.conversationId
    } else if (params.targetUserId) {
      queryParams.targetUserId = params.targetUserId
    }
    
    return this.request({
      url: '/api/messages/private',
      method: 'GET',
      data: queryParams,
      showLoading: params.showLoading !== false // 默认显示loading，轮询时可传false
    })
  }

  // 发送私信
  sendPrivateMessage(data) {
    return this.request({
      url: '/api/messages/private',
      method: 'POST',
      data: {
        senderId: data.senderId,
        receiverId: data.receiverId,
        content: data.content,
        conversationId: data.conversationId // 支持传递会话ID（用于客服会话）
      }
    })
  }

  // 发送客服消息
  sendServiceMessage(data) {
    return this.request({
      url: '/api/messages/service',
      method: 'POST',
      data: {
        userId: data.userId,
        storeId: data.storeId,
        content: data.content
      }
    })
  }

  // 获取会话列表
  getConversationList(userId, showLoading = true) {
    return this.request({
      url: '/api/messages/conversations',
      method: 'GET',
      data: {
        userId: userId
      },
      showLoading: showLoading
    })
  }

  /** 服务端持久化隐藏会话（清缓存后不再出现），conversationId 与列表一致，医师会话为 doctor_{id} */
  hideConversation(userId, conversationId, showLoading = false) {
    return this.request({
      url: '/api/messages/conversations/hide',
      method: 'POST',
      data: { userId, conversationId },
      showLoading
    })
  }

  // 标记消息为已读
  markMessagesAsRead(data) {
    return this.request({
      url: '/api/messages/read',
      method: 'POST',
      data: {
        userId: data.userId,
        conversationId: data.conversationId
      }
    })
  }

  // ========== 互动消息相关接口 ==========
  
  // 获取点赞和收藏列表
  getLikesAndCollections(params = {}) {
    return this.request({
      url: '/api/messages/likes',
      method: 'GET',
      data: {
        userId: params.userId,
        page: params.page || 1,
        size: params.size || 20
      }
    })
  }

  // 获取新增关注列表
  getNewFollows(params = {}) {
    return this.request({
      url: '/api/messages/follows',
      method: 'GET',
      data: {
        userId: params.userId,
        page: params.page || 1,
        size: params.size || 20
      }
    })
  }

  // 获取评论和@列表
  getCommentsAndMentions(params = {}) {
    return this.request({
      url: '/api/messages/comments',
      method: 'GET',
      data: {
        userId: params.userId,
        page: params.page || 1,
        size: params.size || 20
      }
    })
  }

  // 获取推荐用户（params.userId 必填，否则后端会报 NumberFormatException）
  getRecommendedUsers(params = {}) {
    const { showLoading = true, ...rest } = params
    const data = { limit: rest.limit || 10 }
    if (rest.userId != null && rest.userId !== '') data.userId = rest.userId
    return this.request({
      url: '/api/messages/recommended-users',
      method: 'GET',
      data,
      showLoading: showLoading
    })
  }
}

// 创建API实例
const api = new ApiService()

export { api }
