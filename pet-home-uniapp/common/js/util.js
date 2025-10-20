// common/js/util.js 工具函数

/**
 * 处理图片URL，解决小程序HTTP协议限制问题
 * @param {string} imageUrl 原始图片URL
 * @returns {string} 处理后的图片URL
 */
function getImageUrl(imageUrl) {
  if (!imageUrl || imageUrl === 'null' || imageUrl === 'undefined') {
    return '/static/images/pet-paw.png' // 默认图片
  }
  
  // 如果已经是完整URL
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    // 开发环境：localhost的图片直接返回（微信开发者工具支持）
    if (imageUrl.startsWith('http://localhost:8080')) {
      return imageUrl // ✅ 正确返回完整URL
    }
    return imageUrl
  }
  
  // 如果是相对路径（/upload/...），拼接完整URL
  if (imageUrl.startsWith('/upload/') || imageUrl.startsWith('/static/')) {
    return 'http://localhost:8080' + imageUrl
  }
  
  // 如果只是文件名，拼接完整URL
  if (!imageUrl.startsWith('/')) {
    return 'http://localhost:8080/upload/' + imageUrl
  }
  
  // 其他情况，使用默认图片
  return '/static/images/pet-paw.png'
}

/**
 * 格式化价格，保留两位小数
 */
function formatPrice(price) {
  if (typeof price !== 'number') {
    price = parseFloat(price) || 0
  }
  return '¥' + price.toFixed(2)
}

/**
 * 格式化日期
 */
function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''

  if (typeof date === 'string') {
    date = new Date(date)
  }

  if (!(date instanceof Date) || isNaN(date)) {
    return ''
  }

  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  const formatMap = {
    'YYYY': year,
    'MM': formatNumber(month),
    'DD': formatNumber(day),
    'HH': formatNumber(hour),
    'mm': formatNumber(minute),
    'ss': formatNumber(second)
  }

  return format.replace(/YYYY|MM|DD|HH|mm|ss/g, matched => formatMap[matched])
}

/**
 * 格式化数字，不足两位补零
 */
function formatNumber(n) {
  n = n.toString()
  return n[1] ? n : '0' + n
}

/**
 * 计算时间差
 */
function timeAgo(date) {
  if (typeof date === 'string') {
    date = new Date(date)
  }

  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  const week = 7 * day
  const month = 30 * day

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前'
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前'
  } else if (diff < week) {
    return Math.floor(diff / day) + '天前'
  } else if (diff < month) {
    return Math.floor(diff / week) + '周前'
  } else {
    return formatDate(date, 'YYYY-MM-DD')
  }
}

/**
 * 防抖函数
 */
function debounce(func, wait, immediate) {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      timeout = null
      if (!immediate) func(...args)
    }
    const callNow = immediate && !timeout
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    if (callNow) func(...args)
  }
}

/**
 * 节流函数
 */
function throttle(func, wait) {
  let timeout
  return function executedFunction(...args) {
    if (!timeout) {
      timeout = setTimeout(() => {
        timeout = null
        func(...args)
      }, wait)
    }
  }
}

/**
 * 深拷贝
 */
function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(deepClone)
  if (typeof obj === 'object') {
    const clonedObj = {}
    Object.keys(obj).forEach(key => {
      clonedObj[key] = deepClone(obj[key])
    })
    return clonedObj
  }
}

/**
 * 生成随机ID
 */
function generateId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

/**
 * 校验手机号
 */
function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 校验身份证号
 */
function isValidIdCard(idCard) {
  return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard)
}

// Loading 状态管理
let loadingCount = 0

/**
 * 显示加载提示
 */
function showLoading(title = '加载中...') {
  loadingCount++
  uni.showLoading({
    title,
    mask: true
  })
}

/**
 * 隐藏加载提示
 */
function hideLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    uni.hideLoading()
  }
}

/**
 * 获取当前 loading 状态
 */
function getLoadingCount() {
  return loadingCount
}

/**
 * 重置 loading 状态（用于紧急情况）
 */
function resetLoading() {
  loadingCount = 0
  uni.hideLoading()
}

/**
 * 显示提示消息
 */
function showToast(title, icon = 'none', duration = 2000) {
  uni.showToast({
    title,
    icon,
    duration
  })
}

/**
 * 显示模态框
 */
function showModal(title, content, options = {}) {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title,
      content,
      ...options,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 跳转页面
 */
function navigateTo(url) {
  uni.navigateTo({
    url,
    fail: () => {
      showToast('页面跳转失败')
    }
  })
}

/**
 * 跳转到tabBar页面
 */
function switchTab(url) {
  uni.switchTab({
    url,
    fail: () => {
      showToast('页面跳转失败')
    }
  })
}

/**
 * 返回上一页
 */
function navigateBack(delta = 1) {
  uni.navigateBack({
    delta
  })
}

/**
 * 预览图片
 */
function previewImage(urls, current = '') {
  uni.previewImage({
    urls,
    current,
    fail: () => {
      showToast('预览图片失败')
    }
  })
}

/**
 * 选择图片
 */
function chooseImage(count = 1, sizeType = ['original', 'compressed'], sourceType = ['album', 'camera']) {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count,
      sizeType,
      sourceType,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 获取位置信息
 */
function getLocation(type = 'wgs84') {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type,
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 打电话
 */
function makePhoneCall(phoneNumber) {
  uni.makePhoneCall({
    phoneNumber,
    fail: () => {
      showToast('拨打电话失败')
    }
  })
}

/**
 * 设置剪贴板内容
 */
function setClipboardData(data) {
  uni.setClipboardData({
    data,
    success: () => {
      showToast('已复制到剪贴板', 'success')
    },
    fail: () => {
      showToast('复制失败')
    }
  })
}

/**
 * 获取剪贴板内容
 */
function getClipboardData() {
  return new Promise((resolve, reject) => {
    uni.getClipboardData({
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 获取存储空间信息
 */
function getStorageInfo() {
  return new Promise((resolve, reject) => {
    uni.getStorageInfo({
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 清空存储空间
 */
function clearStorage() {
  return new Promise((resolve, reject) => {
    uni.clearStorage({
      success: resolve,
      fail: reject
    })
  })
}

/**
 * 上传图片
 */
function uploadImage(filePath) {
  const token = uni.getStorageSync('token')

  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: 'https://localhost:8080/api/upload/image',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = JSON.parse(res.data)
          if (data.code === 0) {
            resolve(data.data)
          } else {
            reject(new Error(data.msg || '上传失败'))
          }
        } else {
          reject(new Error('上传失败'))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

const util = {
  getImageUrl,
  formatPrice,
  formatDate,
  formatNumber,
  timeAgo,
  debounce,
  throttle,
  deepClone,
  generateId,
  isValidPhone,
  isValidIdCard,
  showLoading,
  hideLoading,
  getLoadingCount,
  resetLoading,
  showToast,
  showModal,
  navigateTo,
  switchTab,
  navigateBack,
  previewImage,
  chooseImage,
  getLocation,
  makePhoneCall,
  setClipboardData,
  getClipboardData,
  getStorageInfo,
  clearStorage,
  uploadImage
}

export { util }
