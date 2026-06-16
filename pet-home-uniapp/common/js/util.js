// common/js/util.js 工具函数

const API_BASE_STORAGE_KEY = 'PETHOME_API_BASE_URL'
const INTERNAL_IP_STORAGE_KEY = 'PETHOME_INTERNAL_IP' // 存储原始内网IP，用于图片URL转换
// 微信开发者工具在 Windows 上可能无法访问内网 IP，使用 localhost
const DEFAULT_HTTP_BASE = 'http://localhost:8080'
const DEFAULT_HTTPS_BASE = 'https://localhost'
const DEFAULT_BACKEND_PORT = '8080'
// 图片 URL 中 localhost 替换兜底（与 main.js 中 DEV_API_BASE 保持一致，避免子包/时序导致未替换）
const FALLBACK_DEV_IMAGE_ORIGIN = 'http://10.43.238.18:8080'
// 线上图片域名兜底：接口/数据库返回 localhost 时，若未替换则用此域名，避免封面黑块（与 main.js API_BASE 一致）
const PRODUCTION_IMAGE_ORIGIN = 'https://situationship.icu'

// 解析URL的辅助函数（替代URL构造函数，兼容小程序环境）
function parseUrl(url) {
  if (typeof url !== 'string' || !url) {
    return null
  }
  
  const urlStr = url.trim()
  if (!urlStr) return null
  
  // 匹配协议、主机名、端口、路径
  // 格式: http://hostname:port/path 或 https://hostname:port/path
  const match = urlStr.match(/^(https?):\/\/([^\/:]+)(:(\d+))?(\/.*)?$/)
  if (!match) {
    // 如果没有协议，尝试匹配 hostname:port/path
    const matchNoProtocol = urlStr.match(/^([^\/:]+)(:(\d+))?(\/.*)?$/)
    if (matchNoProtocol) {
      return {
        protocol: 'http',
        hostname: matchNoProtocol[1] || '',
        port: matchNoProtocol[3] || '',
        pathname: matchNoProtocol[4] || '/',
        origin: `http://${matchNoProtocol[1]}${matchNoProtocol[3] ? `:${matchNoProtocol[3]}` : ''}`
      }
    }
    return null
  }
  
  return {
    protocol: match[1] || 'http',
    hostname: match[2] || '',
    port: match[4] || '',
    pathname: match[5] || '/',
    origin: `${match[1]}://${match[2]}${match[4] ? `:${match[4]}` : ''}`
  }
}

function normalizeBaseUrl(url) {
  if (typeof url !== 'string') return ''
  let normalized = url.trim()
  if (!normalized) return ''
  // 去掉末尾的斜杠，保留协议
  normalized = normalized.replace(/\/+$/, '')
  return normalized
}

function safeGetFromStorage(key) {
  try {
    if (typeof uni !== 'undefined' && uni.getStorageSync) {
      return uni.getStorageSync(key) || ''
    }
  } catch (err) {}
  return ''
}

function safeSetToStorage(key, value) {
  try {
    if (typeof uni !== 'undefined' && uni.setStorageSync) {
      uni.setStorageSync(key, value)
    }
  } catch (err) {}
}

function resolveBaseFromWindow() {
  if (typeof window === 'undefined' || !window.location) return ''
  const { protocol, hostname } = window.location
  if (!hostname) return ''
  const targetPort = (window.__PETHOME_BACKEND_PORT__ || DEFAULT_BACKEND_PORT || '').toString()
  const needPort = targetPort && targetPort !== '80' && targetPort !== '443'
  return `${protocol}//${hostname}${needPort ? `:${targetPort}` : ''}`
}

function resolveBaseFromLaunchOptions() {
  if (typeof uni === 'undefined' || !uni.getLaunchOptionsSync) return ''
  try {
    const options = uni.getLaunchOptionsSync()
    if (!options) return ''
    if (options.query && options.query.apiBaseUrl) {
      return decodeURIComponent(options.query.apiBaseUrl)
    }
    if (
      options.referrerInfo &&
      options.referrerInfo.extraData &&
      options.referrerInfo.extraData.apiBaseUrl
    ) {
      return options.referrerInfo.extraData.apiBaseUrl
    }
  } catch (err) {}
  return ''
}

function sanitizeBaseUrl(url) {
  const normalized = normalizeBaseUrl(url)
  if (!normalized) return ''
  if (/^https?:\/\//i.test(normalized)) {
    return normalized
  }
  return `http://${normalized}`
}

// 检测是否是真机环境（使用新的API避免废弃警告）
function isRealDevice() {
  try {
    if (typeof uni !== 'undefined') {
      // 优先使用新的API
      if (uni.getDeviceInfo) {
        try {
          const deviceInfo = uni.getDeviceInfo()
          return deviceInfo.platform !== 'devtools'
        } catch (e) {
          // 如果新API失败，回退到旧API
        }
      }
      // 回退到旧API（兼容性）
      if (uni.getSystemInfoSync) {
        const systemInfo = uni.getSystemInfoSync()
        // 真机环境：platform 不是 'devtools'
        return systemInfo.platform !== 'devtools'
      }
    }
  } catch (err) {
    // 静默处理错误
  }
  return false
}

function getApiBaseUrl() {
  const cached = normalizeBaseUrl(safeGetFromStorage(API_BASE_STORAGE_KEY))
  const isReal = isRealDevice()
  
  // 开发环境：如果缓存的是错误的虚拟网卡IP（172.27.x.x），忽略它
  // 因为这种IP通常是Clash/TUN等虚拟网卡，手机无法访问
  if (cached && /^https?:\/\/172\.27\./.test(cached)) {
    // 清除错误的缓存
    safeSetToStorage(API_BASE_STORAGE_KEY, '')
    // 继续执行，使用默认值或启动参数
  }
  
  if (cached && !/^https?:\/\/172\.27\./.test(cached)) {
    // 如果缓存的是内网 IP 地址（10.x.x.x, 172.16-31.x.x, 192.168.x.x）
    const isInternalIP = /^https?:\/\/(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(cached)
    if (isInternalIP) {
      // 保存原始内网IP，用于图片URL转换
      try {
        const parsed = parseUrl(cached)
        if (parsed && parsed.hostname) {
          safeSetToStorage(INTERNAL_IP_STORAGE_KEY, parsed.hostname)
          // 移除内网IP保存日志以提升性能
        }
      } catch (err) {
        // 静默处理错误
      }
      
      // 真机环境：使用内网 IP（手机可以访问）
      // 开发者工具：使用 localhost（工具可以访问）
      if (isReal) {
        // 移除真机环境日志以提升性能
        return cached
      } else {
        // 移除开发者工具环境切换日志以提升性能
        safeSetToStorage(API_BASE_STORAGE_KEY, DEFAULT_HTTP_BASE)
        return DEFAULT_HTTP_BASE
      }
    }
    if (cached.includes('localhost') || cached.includes('127.0.0.1')) {
      return cached
    }
    return cached
  }

  const launchBase = normalizeBaseUrl(resolveBaseFromLaunchOptions())
  if (launchBase) {
    // 检查启动参数中的地址是否是内网 IP
    const isInternalIP = /^https?:\/\/(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(launchBase)
    if (isInternalIP) {
      // 保存原始内网IP，用于图片URL转换
      try {
        const parsed = parseUrl(launchBase)
        if (parsed && parsed.hostname) {
          safeSetToStorage(INTERNAL_IP_STORAGE_KEY, parsed.hostname)
          // 移除内网IP保存日志以提升性能
        }
      } catch (err) {
        // 静默处理错误
      }
      
      // 真机环境：使用内网 IP（手机可以访问）
      // 开发者工具：使用 localhost（工具可以访问）
      if (isReal) {
        // 移除启动参数内网IP日志以提升性能
        safeSetToStorage(API_BASE_STORAGE_KEY, launchBase)
        return launchBase
      } else {
        // 移除开发者工具环境切换日志以提升性能
        safeSetToStorage(API_BASE_STORAGE_KEY, DEFAULT_HTTP_BASE)
        return DEFAULT_HTTP_BASE
      }
    }
    safeSetToStorage(API_BASE_STORAGE_KEY, launchBase)
    return launchBase
  }

  const windowBase = normalizeBaseUrl(resolveBaseFromWindow())
  if (windowBase) {
    safeSetToStorage(API_BASE_STORAGE_KEY, windowBase)
    return windowBase
  }

  return DEFAULT_HTTP_BASE
}

function setApiBaseUrl(url) {
  const sanitized = sanitizeBaseUrl(url)
  if (!sanitized) return
  safeSetToStorage(API_BASE_STORAGE_KEY, sanitized)
}

function getHttpBase() {
  return getApiBaseUrl() || DEFAULT_HTTP_BASE
}

function getHttpsBase() {
  const httpBase = getHttpBase()

  try {
    const parsed = parseUrl(httpBase)
    if (!parsed || !parsed.hostname) {
      return DEFAULT_HTTPS_BASE
    }
    
    if (parsed.protocol === 'https') {
      return parsed.origin
    }
    
    // 针对内网 / 开发环境，保留端口号（如果存在且不是80/443）
    const hostname = parsed.hostname
    let portPart = ''
    
    // 开发环境：如果原端口是8080，转换为8443（HTTPS端口）
    if (parsed.port === '8080') {
      portPart = ':8443'
    } else if (parsed.port && parsed.port !== '80' && parsed.port !== '443') {
      // 其他端口保留
      portPart = `:${parsed.port}`
    }
    
    return `https://${hostname}${portPart}`
  } catch (err) {
    return DEFAULT_HTTPS_BASE
  }
}

/**
 * 处理图片URL，解决小程序HTTP协议限制问题
 * @param {string} imageUrl 原始图片URL
 * @returns {string} 处理后的图片URL
 */
function getImageUrl(imageUrl) {
  const HTTP_BASE = getHttpBase()
  const HTTPS_BASE = getHttpsBase()
  const isReal = isRealDevice() // 判断是否是真机环境
  
  // 调试日志：输出环境信息（减少日志输出，只在关键转换时输出）
  // if (imageUrl && (imageUrl.includes('upload') || imageUrl.includes('images'))) {
  //   console.log(`[图片URL处理] 环境: ${isReal ? '真机' : '开发者工具'}, HTTP_BASE: ${HTTP_BASE}, HTTPS_BASE: ${HTTPS_BASE}, 原始URL: ${imageUrl}`)
  // }
  
  // 判断是否是内网/本地地址（需要保持HTTP）
  const isLocalOrInternal = (url) => {
    if (!url) return false
    // localhost 和 127.0.0.1
    if (url.includes('localhost') || url.includes('127.0.0.1')) {
      return true
    }
    // 内网IP地址：10.x.x.x, 172.16-31.x.x, 192.168.x.x
    const internalIpPattern = /(^|\/)(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/
    if (internalIpPattern.test(url)) {
      return true
    }
    return false
  }
  
  // 判断是否是IP地址（包括公网IP和内网IP）
  const isIpAddress = (url) => {
    if (!url) return false
    // 匹配IP地址格式：xxx.xxx.xxx.xxx
    const ipPattern = /\b(\d{1,3}\.){3}\d{1,3}\b/
    return ipPattern.test(url)
  }
  
  // 判断是否是合法域名（不是IP地址）
  const isLegalDomain = (url) => {
    if (!url) return false
    // 如果包含域名特征（如 .com, .cn, .io, .net 等），且不是IP地址
    const domainPattern = /^https?:\/\/[^\/]+/
    const match = url.match(domainPattern)
    if (match) {
      const host = match[0].replace(/^https?:\/\//, '')
      // 如果是IP地址，返回false
      if (isIpAddress(host)) {
        return false
      }
      // 如果包含域名特征，返回true
      return /\./.test(host) && !host.startsWith('localhost')
    }
    return false
  }
  
  // 默认图片直接使用静态资源路径（/static/目录下的资源不需要转换为HTTP URL）
  const defaultImage = '/static/images/garfield-default-avatar.png'
  
  if (!imageUrl || imageUrl === 'null' || imageUrl === 'undefined') {
    return defaultImage
  }
  
  // 转换为字符串并去除首尾空格
  imageUrl = String(imageUrl).trim()
  
  // 如果是空字符串，返回默认图片
  if (imageUrl === '') {
    return defaultImage
  }
  
  // 兜底：后端或数据里可能误把完整 URL 存成 "/https://..." 或 "/http://..."，导致被当成相对路径拼出错误地址，先去掉前导 /
  if (imageUrl.startsWith('/https://') || imageUrl.startsWith('/http://')) {
    imageUrl = imageUrl.slice(1)
  }

  // 只要图片地址是 localhost/127.0.0.1（任意端口），就统一换成可用的域名，避免封面黑屏、ERR_CONNECTION_REFUSED
  const apiBaseRaw = getApiBaseUrl()
  const imageOrigin = (apiBaseRaw || '').trim().replace(/\/api\/?$/, '').replace(/\/+$/, '')
  const isProductionDomain = imageOrigin && /^https?:\/\//.test(imageOrigin) && !/localhost|127\.0\.0\.1/.test(imageOrigin)
  const isLocalhostImage = (imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1')) && (imageUrl.startsWith('http://') || imageUrl.startsWith('https://'))
  if (isLocalhostImage) {
    const pathMatch = imageUrl.match(/^https?:\/\/[^/]+(\/.*)$/)
    if (pathMatch && pathMatch[1]) {
      const path = pathMatch[1].trim()
      const isApiLocalhost = imageOrigin && (imageOrigin.includes('localhost') || imageOrigin.includes('127.0.0.1'))
      if (isProductionDomain) {
        imageUrl = imageOrigin + path
      } else if (isApiLocalhost) {
        // 本地开发：API 和图片都在 localhost，保持原样，避免替换成线上域名导致新上传图片 404
        // imageUrl 已是 localhost 地址，无需修改
      } else {
        // 配置未设置或仍是 localhost 时，用线上域名兜底，确保社区/发现流封面能加载
        imageUrl = PRODUCTION_IMAGE_ORIGIN + path
      }
    }
  }
  // 若后端返回了带 :8443 的完整 URL，且上面未替换（pathMatch 未命中），再改为使用与接口同源的 base（图片用无 /api 的 origin）
  if ((imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) && imageUrl.includes(':8443')) {
    const pathMatch = imageUrl.match(/^https?:\/\/[^/]+(\/.*)$/)
    if (pathMatch) {
      const path = pathMatch[1]
      const base = (apiBaseRaw || '').replace(/\/api\/?$/, '').replace(/\/+$/, '')
      if (base) imageUrl = base + path
    }
  }
  
  // 如果包含URL编码的引号（%22），说明可能是被错误编码的字符串，尝试解码
  if (imageUrl.includes('%22') || imageUrl.includes('%27')) {
    try {
      imageUrl = decodeURIComponent(imageUrl)
      // 如果解码后包含引号，去掉引号
      imageUrl = imageUrl.replace(/^["']|["']$/g, '').trim()
    } catch (e) {
      // 静默处理错误
    }
  }
  
  // 如果以 [ 开头和 ] 结尾，说明可能是数组格式的字符串，尝试提取URL
  if (imageUrl.startsWith('[') && imageUrl.endsWith(']')) {
    try {
      const parsed = JSON.parse(imageUrl)
      if (Array.isArray(parsed) && parsed.length > 0) {
        imageUrl = String(parsed[0]).trim()
      } else {
        return defaultImage
      }
    } catch (e) {
      // 如果不是有效的JSON，尝试提取引号内的内容
      const match = imageUrl.match(/["']([^"']+)["']/)
      if (match && match[1]) {
        imageUrl = match[1].trim()
      } else {
        return defaultImage
      }
    }
  }
  
  // 优先处理临时路径（tmp或__tmp__），必须在HTTP检查之前
  // 匹配 /tmp/、/__tmp__/、http://tmp/xxx、http://__tmp__/xxx、tmp/xxx、__tmp__/xxx 等格式
  if (imageUrl.startsWith('/tmp/') || 
      imageUrl.startsWith('/__tmp__/') || 
      imageUrl.startsWith('http://tmp/') || 
      imageUrl.startsWith('http://__tmp__/') ||
      imageUrl.startsWith('https://tmp/') ||
      imageUrl.startsWith('https://__tmp__/') ||
      (imageUrl.startsWith('tmp/') && !imageUrl.includes('://')) ||
      (imageUrl.startsWith('__tmp__/') && !imageUrl.includes('://'))) {
    return defaultImage
  }

  // 尽早将 localhost/127.0.0.1 替换为当前配置的后端地址，避免个人页/商品/店铺等图片在真机或模拟器加载失败
  if ((imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1')) && (imageUrl.startsWith('http://') || imageUrl.startsWith('https://'))) {
    // 开发者工具下：若配置已是线上域名，上面已替换；否则用当前配置的 host:port（避免强制 8443 导致 REFUSED）
    if (!isReal) {
      const devBase = getApiBaseUrl()
      const devParsed = parseUrl(devBase)
      const isDevLocalhost = devParsed && devParsed.hostname && /localhost|127\.0\.0\.1/.test(devParsed.hostname)
      let devOrigin
      if (isDevLocalhost) {
        devOrigin = `http://${devParsed.hostname}${devParsed.port ? ':' + devParsed.port : ':8080'}`
      } else if (devParsed && devParsed.hostname && !/localhost|127\.0\.0\.1/.test(devParsed.hostname)) {
        devOrigin = (devParsed.protocol === 'https' ? devParsed.origin : `https://${devParsed.hostname}${devParsed.port ? ':' + devParsed.port : ''}`)
      } else {
        devOrigin = devParsed && devParsed.hostname
          ? `https://${devParsed.hostname}${devParsed.port ? ':' + devParsed.port : ':8443'}`
          : 'https://localhost:8443'
      }
      const pathMatch = imageUrl.match(/^https?:\/\/[^/]+(\/.*)$/)
      imageUrl = pathMatch && pathMatch[1] ? (devOrigin.replace(/\/+$/, '') + pathMatch[1]) : imageUrl.replace(/http:\/\/(localhost|127\.0\.0\.1)(:\d+)?/, 'http://localhost:8080')
    } else {
      let originToUse = ''
      try {
        const base = getApiBaseUrl()
        const parsed = parseUrl(base)
        if (parsed && parsed.hostname && !/localhost|127\.0\.0\.1/.test(parsed.hostname)) {
          originToUse = `http://${parsed.hostname}${parsed.port ? ':' + parsed.port : ':8080'}`
        }
      } catch (e) {}
      if (!originToUse) {
        originToUse = FALLBACK_DEV_IMAGE_ORIGIN
      }
      imageUrl = imageUrl.replace(/https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?/, originToUse)
    }
  }
  
  const replaceToHttps = (url) => {
    if (!url) return url
    // 处理各种 localhost 格式
    if (url.startsWith('http://localhost:8080/')) {
      return url.replace('http://localhost:8080', HTTPS_BASE)
    }
    if (url.startsWith('http://localhost:8443/')) {
      return url.replace('http://localhost:8443', HTTPS_BASE)
    }
    if (url.startsWith('http://localhost/')) {
      return url.replace('http://localhost', HTTPS_BASE)
    }
    if (url.startsWith('http://127.0.0.1:8080/')) {
      return url.replace('http://127.0.0.1:8080', HTTPS_BASE)
    }
    if (url.startsWith('http://127.0.0.1:8443/')) {
      return url.replace('http://127.0.0.1:8443', HTTPS_BASE)
    }
    if (url.startsWith('http://127.0.0.1/')) {
      return url.replace('http://127.0.0.1', HTTPS_BASE)
    }
    // 处理 HTTP_BASE
    if (url.startsWith(`${HTTP_BASE}/`)) {
      return url.replace(HTTP_BASE, HTTPS_BASE)
    }
    // 通用HTTP转HTTPS
    if (url.startsWith('http://')) {
      return url.replace(/^http:/, 'https:')
    }
    return url
  }

  // 如果是本地静态资源（/static/），直接返回，不转换为HTTP URL
  // 在uniapp中，/static/目录下的资源在编译时会被打包到小程序中，不需要从服务器加载
  if (imageUrl.startsWith('/static/')) {
    // 移除静态资源日志以提升性能（频繁调用）
    return imageUrl
  }
  
  // 如果是相对路径 /upload/... 或 /images/...
  // 开发环境（localhost/127.0.0.1）统一用 HTTP 8080，避免 8443 证书导致商品图/轮播图不显示
  if (imageUrl.startsWith('/upload/') || imageUrl.startsWith('/images/')) {
    const apiBaseIsDomain = isLegalDomain(HTTP_BASE) || isLegalDomain(HTTPS_BASE)
    let finalUrl
    if (apiBaseIsDomain) {
      finalUrl = `${HTTPS_BASE}${imageUrl}`
    } else {
      // 本地/内网环境：用 HTTP_BASE（8080）拼图，保证「不校验合法域名」时能加载
      finalUrl = `${HTTP_BASE}${imageUrl}`
    }
    // 若拼接后仍是 172.27（虚拟网卡），在开发者工具中改为 localhost 避免超时
    if (!isReal && /^https?:\/\/172\.27\.\d+\.\d+/.test(finalUrl)) {
      finalUrl = finalUrl.replace(/^https?:\/\/172\.27\.\d+\.\d+(?::\d+)?/, 'http://localhost:8080')
    }
    return finalUrl
  }
  
  // 如果是以 / 开头的其他路径（如 /upload/product/xxx）
  if (imageUrl.startsWith('/')) {
    const apiBaseIsDomain = isLegalDomain(HTTP_BASE) || isLegalDomain(HTTPS_BASE)
    if (apiBaseIsDomain) {
      return `${HTTPS_BASE}${imageUrl}`
    }
    // 本地/内网：用 HTTP 8080 拼图，保证商品图等能加载
    return `${HTTP_BASE}${imageUrl}`
  }

  // 相对路径无前导 /（如 product/product-xxx.jpg、upload/xxx），数据库商品图常见格式，拼成 /upload/... 再走 base
  if (!imageUrl.startsWith('http') && imageUrl.length > 0) {
    const withLeadingSlash = imageUrl.startsWith('upload/') || imageUrl.startsWith('upload\\')
      ? '/' + imageUrl.replace(/\\/g, '/')
      : '/upload/' + imageUrl.replace(/\\/g, '/')
    const apiBaseIsDomain = isLegalDomain(HTTP_BASE) || isLegalDomain(HTTPS_BASE)
    let finalUrl = apiBaseIsDomain ? `${HTTPS_BASE}${withLeadingSlash}` : `${HTTP_BASE}${withLeadingSlash}`
    if (!isReal && /^https?:\/\/172\.27\.\d+\.\d+/.test(finalUrl)) {
      finalUrl = finalUrl.replace(/^https?:\/\/172\.27\.\d+\.\d+(?::\d+)?/, 'http://localhost:8080')
    }
    return finalUrl
  }
  
  // 开发者工具/本机无法访问 172.27 虚拟网卡（如 Clash TUN），导致 ERR_CONNECTION_TIMED_OUT，改为 localhost
  if ((imageUrl.startsWith('https://') || imageUrl.startsWith('http://')) && /^https?:\/\/172\.27\.\d+\.\d+/.test(imageUrl) && !isReal) {
    const useHttps = imageUrl.startsWith('https://')
    const originReplace = useHttps ? 'https://localhost:8443' : 'http://localhost:8080'
    return imageUrl.replace(/^https?:\/\/172\.27\.\d+\.\d+(?::\d+)?/, originReplace)
  }
  
  // 最终兜底：任何仍含 localhost/127.0.0.1 的完整 URL 都改为线上域名，避免社区发现流封面黑块
  if ((imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) && (imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1'))) {
    const m = imageUrl.match(/^https?:\/\/[^/]+(\/.*)$/)
    if (m && m[1]) {
      imageUrl = PRODUCTION_IMAGE_ORIGIN + m[1].trim()
    }
  }

  // 如果是HTTPS协议，直接返回
  if (imageUrl.startsWith('https://')) {
    return imageUrl
  }
  
  // 如果是HTTP协议
  if (imageUrl.startsWith('http://')) {
    // 检查是否是开发环境（通过API base URL判断）
    const currentApiBase = getApiBaseUrl()
    const isDevelopment = currentApiBase.includes('localhost') || 
                         currentApiBase.includes('127.0.0.1') ||
                         /^https?:\/\/(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(currentApiBase)
    
    // 开发环境：将 localhost 替换为内网 IP
    if (isDevelopment && (imageUrl.includes('localhost') || imageUrl.includes('127.0.0.1'))) {
      const portMatch = imageUrl.match(/https?:\/\/(localhost|127\.0\.0\.1):(\d+)/)
      if (portMatch) {
        const port = portMatch[2]
        // 获取内网 IP 地址
        let internalIp = null
        
        // 优先从当前 API base 获取
        if (currentApiBase && !currentApiBase.includes('localhost') && !currentApiBase.includes('127.0.0.1')) {
          try {
            const parsed = parseUrl(currentApiBase)
            if (parsed && parsed.hostname) {
              const ipAddress = parsed.hostname
              if (/^(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(ipAddress)) {
                internalIp = ipAddress
              }
            }
          } catch (err) {}
        }
        
        // 如果没找到，从存储的内网 IP 获取
        if (!internalIp) {
          const storedInternalIp = safeGetFromStorage(INTERNAL_IP_STORAGE_KEY)
          if (storedInternalIp && storedInternalIp.trim() && /^(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(storedInternalIp.trim())) {
            internalIp = storedInternalIp.trim()
          }
        }
        
        // 如果还没找到，从存储的 API base 获取
        if (!internalIp) {
          const storedBase = safeGetFromStorage(API_BASE_STORAGE_KEY)
          if (storedBase && !storedBase.includes('localhost') && !storedBase.includes('127.0.0.1')) {
            try {
              const parsed = parseUrl(storedBase)
              if (parsed && parsed.hostname) {
                const storedIp = parsed.hostname
                if (/^(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(storedIp)) {
                  safeSetToStorage(INTERNAL_IP_STORAGE_KEY, storedIp)
                  internalIp = storedIp
                }
              }
            } catch (err) {}
          }
        }
        
        // 如果找到了内网 IP，替换 localhost
        // 开发环境：统一使用HTTP（8080端口）
        // 真机环境下，<image>标签的HTTP图片会被阻止，需要在图片加载失败时使用downloadFile下载
        if (internalIp) {
          // 开发环境：使用HTTP（统一使用8080端口）
          const replacedUrl = imageUrl.replace(/https?:\/\/(localhost|127\.0\.0\.1):\d+/, `http://${internalIp}:8080`)
          // 移除日志输出以提升性能
          return replacedUrl
        } else {
          // 如果没找到内网 IP
          // 特殊处理：如果端口是8443但协议是HTTP，改为8080端口的HTTP（开发环境）
          if (port === '8443' && imageUrl.startsWith('http://')) {
            const httpUrl = imageUrl.replace(/http:\/\/(localhost|127\.0\.0\.1):8443/, 'http://localhost:8080')
            // 移除日志输出以提升性能
            return httpUrl
          }
          
          // 开发环境：保持HTTP不变（需要配置"不校验合法域名"）
          // 无论是开发者工具还是真机，开发环境都使用HTTP
          if (port === '8080') {
            // console.log('未找到内网 IP，保持原URL (开发环境HTTP):', imageUrl)
            return imageUrl
          } else {
            // 其他端口，改为8080端口的HTTP（开发环境）
            const httpUrl = imageUrl.replace(/http:\/\/(localhost|127\.0\.0\.1):\d+/, 'http://localhost:8080')
            // 移除端口转换日志以提升性能
            return httpUrl
          }
        }
      }
    }
    
    // 如果已经是完整URL（http:// 或 https://）
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      // 检查是否是合法域名
      if (isLegalDomain(imageUrl)) {
        // 合法域名：如果是HTTP，转换为HTTPS
        if (imageUrl.startsWith('http://')) {
          const httpsUrl = imageUrl.replace(/^http:/, 'https:')
          // 移除HTTPS转换日志以提升性能
          return httpsUrl
        }
        // 已经是HTTPS，直接返回
        // 移除HTTPS返回日志以提升性能
        return imageUrl
      }
      
      // 使用非捕获组来正确匹配IP和端口
      const internalIpMatch = imageUrl.match(/https?:\/\/((?:10\.\d+\.\d+\.\d+|172\.(?:1[6-9]|2[0-9]|3[01])\.\d+\.\d+|192\.168\.\d+\.\d+|localhost|127\.0\.0\.1))(?::(\d+))?/)
      if (internalIpMatch) {
        const host = internalIpMatch[1]  // 主机名或IP地址
        const port = internalIpMatch[2] || '8080'  // 端口号，默认8080
        
        // 开发环境（内网IP）：保持HTTP不变，需要配置"不校验合法域名"
        // 生产环境：转换为HTTPS
        const isDevEnv = /^(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(host) || 
                         host === 'localhost' || host === '127.0.0.1'
        
        if (isDevEnv) {
          // 开发者工具中微信基础库不再支持 HTTP 图片，必须用 HTTPS，否则报「不再支持 HTTP 协议」且 ERR_PROXY_CONNECTION_FAILED
          if (!isReal && port === '8080' && imageUrl.startsWith('http://')) {
            const httpsUrl = imageUrl.replace(/^http:\/\/([^\/]+)/, (_, hostPort) => 'https://' + hostPort.replace(':8080', ':8443'))
            return httpsUrl
          }
          if (port === '8443' && imageUrl.startsWith('http://')) {
            const httpUrl = imageUrl.replace(/http:\/\/[^\/]+/, `http://${host}:8080`)
            return httpUrl
          } else if (port === '8080' && imageUrl.startsWith('http://')) {
            return imageUrl
          } else if (imageUrl.startsWith('https://')) {
            const httpUrl = imageUrl.replace(/https:\/\/[^\/]+/, `http://${host}:8080`)
            return httpUrl
          } else {
            const httpUrl = imageUrl.replace(/http:\/\/[^\/]+/, `http://${host}:8080`)
            return httpUrl
          }
        } else {
          // 生产环境：转换为HTTPS
          const httpsPort = port === '8080' ? '8443' : (port === '80' ? '443' : port)
          const httpsUrl = imageUrl.replace(/http:\/\/[^\/]+/, `https://${host}:${httpsPort}`)
          // 移除HTTP转HTTPS日志以提升性能
          return httpsUrl
        }
      }
      
      // 对于其他HTTP地址（非内网）
      // 开发环境：保持HTTP不变（需要配置"不校验合法域名"）
      // 生产环境：转换为HTTPS
      const isDevEnv = imageUrl.includes('localhost') || 
                       imageUrl.includes('127.0.0.1') ||
                       /https?:\/\/(10\.|172\.(1[6-9]|2[0-9]|3[01])\.|192\.168\.)/.test(imageUrl)
      
      if (isDevEnv) {
        // 开发环境：保持HTTP不变（需要配置"不校验合法域名"）
        // 无论是开发者工具还是真机，开发环境都使用HTTP
        // 移除非内网HTTP地址日志以提升性能
        return imageUrl
      } else {
        // 生产环境：转换为HTTPS
        const httpsUrl = replaceToHttps(imageUrl)
        if (httpsUrl !== imageUrl) {
          // 移除HTTP转HTTPS日志以提升性能
          return httpsUrl
        }
        // 如果无法转换，至少尝试将协议改为HTTPS
        const fallbackHttps = imageUrl.replace(/^http:/, 'https:')
        return fallbackHttps
      }
    }
  }
  
  // 检查是否是微信小程序的临时文件标识（通常是短字符串，不包含路径分隔符）
  // 这些标识符通常不包含 /、\、:、. 等路径字符
  if (imageUrl.length < 50 && !imageUrl.includes('/') && !imageUrl.includes('\\') && 
      !imageUrl.includes(':') && !imageUrl.includes('.')) {
    return defaultImage
  }
  
  // 其他情况，使用默认图片
  return defaultImage
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
 * 解析日期字符串为Date对象（iOS兼容）
 * 将 "2025-10-20 22:53:23" 格式转换为 "2025-10-20T22:53:23"
 */
function parseDate(dateString) {
  if (!dateString) return null
  
  if (dateString instanceof Date) {
    return dateString
  }
  
  if (typeof dateString === 'string') {
    // 将空格替换为T，使其符合ISO 8601格式，兼容iOS
    const isoDateString = dateString.replace(/ /g, 'T')
    return new Date(isoDateString)
  }
  
  return null
}

/**
 * 格式化日期
 */
function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''

  if (typeof date === 'string') {
    date = parseDate(date)
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
    date = parseDate(date)
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
 * 微信式时间戳格式化
 * 根据消息发送时间相对于当前时间的跨度，采用不同的显示格式
 * @param {Date|string|number} timestamp - 消息时间戳
 * @returns {string} 格式化后的时间字符串
 */
function formatWeChatTimestamp(timestamp) {
  if (!timestamp) return ''
  
  let date = null
  if (timestamp instanceof Date) {
    date = timestamp
  } else if (typeof timestamp === 'string') {
    date = parseDate(timestamp)
  } else if (typeof timestamp === 'number') {
    date = new Date(timestamp)
  } else {
    return ''
  }
  
  if (!date || isNaN(date.getTime())) {
    return ''
  }
  
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const dayMs = 24 * hour
  const weekMs = 7 * dayMs
  
  // 星期数组
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  // 1. 当天消息（0-24小时内）：显示 "时:分"
  if (diff < dayMs) {
    const hours = formatNumber(date.getHours())
    const minutes = formatNumber(date.getMinutes())
    return `${hours}:${minutes}`
  }
  
  // 2. 超过1天但小于1周（1-7天）：显示 "星期几 时:分"
  if (diff < weekMs) {
    const weekday = weekdays[date.getDay()]
    const hours = formatNumber(date.getHours())
    const minutes = formatNumber(date.getMinutes())
    return `${weekday} ${hours}:${minutes}`
  }
  
  // 3. 超过1周（7天以上）：显示 "年月日 时:分"
  const year = date.getFullYear()
  const month = formatNumber(date.getMonth() + 1)
  const day = formatNumber(date.getDate())
  const hours = formatNumber(date.getHours())
  const minutes = formatNumber(date.getMinutes())
  return `${year}年${month}月${day}日 ${hours}:${minutes}`
}

/**
 * 判断是否应该显示时间戳（基于5分钟规则）
 * @param {Date|string|number} currentTimestamp - 当前消息的时间戳
 * @param {Date|string|number} previousTimestamp - 上一条消息的时间戳（如果有）
 * @returns {boolean} 是否应该显示时间戳
 */
function shouldShowTimestamp(currentTimestamp, previousTimestamp) {
  if (!currentTimestamp) return false
  
  // 如果没有上一条消息，总是显示时间戳
  if (!previousTimestamp) return true
  
  let currentDate = null
  let previousDate = null
  
  // 解析当前消息时间
  if (currentTimestamp instanceof Date) {
    currentDate = currentTimestamp
  } else if (typeof currentTimestamp === 'string') {
    currentDate = parseDate(currentTimestamp)
  } else if (typeof currentTimestamp === 'number') {
    currentDate = new Date(currentTimestamp)
  } else {
    return true // 无法解析，显示时间戳
  }
  
  // 解析上一条消息时间
  if (previousTimestamp instanceof Date) {
    previousDate = previousTimestamp
  } else if (typeof previousTimestamp === 'string') {
    previousDate = parseDate(previousTimestamp)
  } else if (typeof previousTimestamp === 'number') {
    previousDate = new Date(previousTimestamp)
  } else {
    return true // 无法解析上一条，显示时间戳
  }
  
  if (!currentDate || isNaN(currentDate.getTime()) || !previousDate || isNaN(previousDate.getTime())) {
    return true
  }
  
  // 计算时间差（毫秒）
  const diff = currentDate.getTime() - previousDate.getTime()
  
  // 5分钟 = 5 * 60 * 1000 毫秒
  const fiveMinutes = 5 * 60 * 1000
  
  // 如果间隔超过5分钟，显示时间戳
  return diff > fiveMinutes
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
 * 返回上一页（带页面栈检查，减轻「routeDone with a webviewId is not found」等 DevTools/竞态报错）
 */
function navigateBack(delta = 1) {
  try {
    const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
    const n = (pages && pages.length) || 0
    if (n <= delta) {
      if (typeof uni !== 'undefined' && uni.switchTab) {
        uni.switchTab({ url: '/pages/main/index', fail: () => {} })
      }
      return
    }
    uni.navigateBack({
      delta,
      fail: () => {
        try {
          if (typeof uni !== 'undefined' && uni.switchTab) {
            uni.switchTab({ url: '/pages/main/index', fail: () => {} })
          }
        } catch (e) {}
      }
    })
  } catch (e) {
    try {
      if (typeof uni !== 'undefined' && uni.switchTab) {
        uni.switchTab({ url: '/pages/main/index', fail: () => {} })
      }
    } catch (e2) {}
  }
}

/** 工作人员跳转「我的」时避免与其它 tab 的 onShow 重复 switchTab，减轻 routeDone/webviewId 类报错 */
let staffSwitchToMineLock = false

/**
 * 工作人员账号仅允许使用「我的」：在其它 tab 页的 onShow 首行调用。
 * 使用异步 switchTab，避免与当前 webview 卸载竞态。
 * @returns {boolean} true 表示已拦截（调用方应 return），false 表示非工作人员可继续 onShow
 */
function redirectStaffToMineIfNeeded() {
  try {
    if (typeof uni === 'undefined' || !uni.getStorageSync('staffToken')) return false
    if (staffSwitchToMineLock) return true
    staffSwitchToMineLock = true
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/main/index',
        complete: () => {
          staffSwitchToMineLock = false
        },
        fail: () => {
          staffSwitchToMineLock = false
        }
      })
      uni.showToast({ title: '工作人员只能使用「我的」页面', icon: 'none' })
    }, 0)
    return true
  } catch (e) {
    staffSwitchToMineLock = false
    return false
  }
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
 * 选择图片（默认优先压缩图，减轻上传与加载）
 */
function chooseImage(count = 1, sizeType = ['compressed'], sourceType = ['album', 'camera']) {
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

/** 上传前压缩：与主流社交应用相当，质量 78、长边最大 1280，加载更快 */
const UPLOAD_COMPRESS_QUALITY = 78
const UPLOAD_COMPRESS_MAX = 1280

function compressImageForUpload(src) {
  return new Promise((resolve) => {
    if (!src || typeof uni.compressImage !== 'function') {
      resolve(src)
      return
    }
    uni.compressImage({
      src,
      quality: UPLOAD_COMPRESS_QUALITY,
      compressedWidth: UPLOAD_COMPRESS_MAX,
      compressedHeight: UPLOAD_COMPRESS_MAX,
      success: (res) => {
        resolve(res.tempFilePath || src)
      },
      fail: () => {
        resolve(src)
      }
    })
  })
}

/**
 * 上传图片（先压缩再上传，用户/管理员上传的图会自动变小）
 */
function uploadImage(filePath) {
  const token = uni.getStorageSync('token')
  const apiBase = getApiBaseUrl()
  const uploadBase =
    typeof apiBase === 'string' && apiBase.startsWith('https://')
      ? getHttpsBase()
      : getHttpBase()
  const uploadUrl = `${uploadBase}/api/upload/image`

  const doUpload = (path) => {
    return new Promise((resolve, reject) => {
      uni.uploadFile({
        url: uploadUrl,
        filePath: path,
        name: 'file',
        header: {
          'Authorization': `Bearer ${token}`
        },
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
              if (data.code === 0 || data.code === 200) {
                resolve(data.data)
              } else {
                reject(new Error(data.msg || '上传失败'))
              }
            } catch (e) {
              reject(new Error('上传失败'))
            }
          } else {
            reject(new Error('上传失败'))
          }
        },
        fail: (err) => reject(err)
      })
    })
  }

  return compressImageForUpload(filePath).then(doUpload)
}

const util = {
  getImageUrl,
  formatPrice,
  parseDate,
  formatDate,
  formatNumber,
  timeAgo,
  formatWeChatTimestamp,
  shouldShowTimestamp,
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
  redirectStaffToMineIfNeeded,
  previewImage,
  chooseImage,
  getLocation,
  makePhoneCall,
  setClipboardData,
  getClipboardData,
  getStorageInfo,
  clearStorage,
  uploadImage,
  compressImageForUpload,
  getApiBaseUrl,
  setApiBaseUrl
}

export { util }
