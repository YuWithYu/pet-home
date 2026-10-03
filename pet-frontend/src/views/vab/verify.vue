<template>
  <div class="verify-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单核销</span>
          <el-button type="primary" :icon="Refresh" @click="refreshPage">刷新</el-button>
        </div>
      </template>

      <!-- 核销方式选择 -->
      <el-tabs v-model="activeTab" class="verify-tabs">
        <!-- 手动输入核销码 -->
        <el-tab-pane label="手动输入核销码" name="manual">
          <div class="manual-verify-section">
            <el-form :model="verifyForm" label-width="120px" style="max-width: 600px">
              <el-form-item label="核销码" required>
                <el-input
                  v-model="verifyForm.verifyCode"
                  placeholder="请输入核销码"
                  clearable
                  size="large"
                  @keyup.enter="handleVerify"
                  style="width: 400px"
                >
                  <template #append>
                    <el-button type="primary" @click="handleVerify" :loading="verifying">
                      核销
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 扫描二维码 -->
        <el-tab-pane label="扫描二维码" name="scan">
          <div class="scan-verify-section">
            <div class="scan-actions">
              <el-button
                type="primary"
                size="large"
                :icon="Camera"
                @click="startScan"
                :disabled="scanning"
              >
                {{ scanning ? '扫描中...' : '启动摄像头扫描' }}
              </el-button>

              <el-upload
                class="upload-demo"
                :auto-upload="true"
                :show-file-list="false"
                :before-upload="handleUploadQRCode"
                accept="image/*"
                style="display: inline-block; margin-left: 20px"
              >
                <el-button type="success" size="large" :icon="Upload">
                  上传二维码图片
                </el-button>
              </el-upload>
            </div>

            <!-- 摄像头预览区域 -->
            <div v-show="activeTab === 'scan'" class="camera-preview-wrapper">
              <!-- 视频元素始终存在，但只在扫描时显示 -->
              <video
                ref="videoRef"
                v-show="scanning"
                autoplay
                playsinline
                muted
                class="video-preview"
                style="width: 100%; height: auto; display: block;"
              ></video>
              <canvas ref="canvasRef" style="display: none"></canvas>
              
              <div v-if="scanning" class="camera-preview">
                <div class="scan-overlay">
                  <div class="scan-frame">
                    <div class="scan-line"></div>
                  </div>
                  <div class="scan-tip">请将核销二维码对准扫描框</div>
                  <el-button
                    type="danger"
                    :icon="Close"
                    @click="stopScan"
                    class="stop-scan-btn"
                  >
                    停止扫描
                  </el-button>
                </div>
              </div>
              <div v-else class="camera-placeholder">
                <el-empty description="点击上方按钮启动摄像头扫描" :image-size="100" />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 核销结果 -->
      <div v-if="verifyResult" class="verify-result">
        <el-card
          :class="verifyResult.success ? 'success-card' : 'error-card'"
          shadow="hover"
        >
          <div class="result-content">
            <div class="result-icon">
              <el-icon :size="48" :color="verifyResult.success ? '#67c23a' : '#f56c6c'">
                <component :is="verifyResult.success ? 'Check' : 'Close'" />
              </el-icon>
            </div>
            <div class="result-text">
              <h3>{{ verifyResult.success ? '核销成功' : '核销失败' }}</h3>
              <p>{{ verifyResult.message }}</p>
            </div>
          </div>

          <!-- 订单信息 -->
          <div v-if="verifyResult.success && verifyResult.data" class="order-info">
            <el-descriptions title="订单信息" :column="2" border>
              <el-descriptions-item label="订单号">
                {{ verifyResult.data.id }}
              </el-descriptions-item>
              <el-descriptions-item label="服务类型">
                {{ getServiceTypeName(verifyResult.data.serviceType) }}
              </el-descriptions-item>
              <el-descriptions-item label="联系人">
                {{ verifyResult.data.contactName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ verifyResult.data.contactPhone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="预约日期">
                {{ formatDate(verifyResult.data.date || verifyResult.data.appointmentDate) }}
              </el-descriptions-item>
              <el-descriptions-item label="时间段">
                {{ verifyResult.data.timeSlot || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="核销时间" :span="2">
                {{ formatDateTime(verifyResult.data.verifyTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>

      <!-- 今日核销记录 -->
      <div class="verify-history">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日核销记录</span>
              <el-button
                link
                :icon="Refresh"
                @click="loadVerifyHistory"
                :loading="loadingHistory"
              >
                刷新
              </el-button>
            </div>
          </template>

          <el-table
            :data="verifyHistory"
            v-loading="loadingHistory"
            border
            stripe
            style="width: 100%"
          >
            <el-table-column prop="id" label="订单号" width="120" />
            <el-table-column prop="serviceType" label="服务类型" width="120">
              <template #default="{ row }">
                {{ getServiceTypeName(row.serviceType) }}
              </template>
            </el-table-column>
            <el-table-column prop="contactName" label="联系人" width="120" />
            <el-table-column prop="contactPhone" label="联系电话" width="140" />
            <el-table-column prop="verifyCode" label="核销码" width="180" />
            <el-table-column label="核销时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.verifyTime) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default>
                <el-tag type="success">已核销</el-tag>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!loadingHistory && verifyHistory.length === 0" description="暂无核销记录" />
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Camera,
  Upload,
  Close,
  Check,
} from '@element-plus/icons-vue'
import axios from 'axios'
import dayjs from '@/utils/dayjs'
import { baseURL } from '@/config'

// 动态加载 jsQR 库
let jsQR = null
let jsQRLoading = false
const loadJsQR = () => {
  return new Promise((resolve, reject) => {
    if (jsQR) {
      resolve(jsQR)
      return
    }
    if (window.jsQR) {
      jsQR = window.jsQR
      resolve(jsQR)
      return
    }
    
    if (jsQRLoading) {
      // 如果正在加载，等待加载完成
      const checkInterval = setInterval(() => {
        if (jsQR || window.jsQR) {
          clearInterval(checkInterval)
          jsQR = jsQR || window.jsQR
          resolve(jsQR)
        }
      }, 100)
      setTimeout(() => {
        clearInterval(checkInterval)
        if (!jsQR && !window.jsQR) {
          reject(new Error('jsQR 加载超时'))
        }
      }, 10000)
      return
    }
    
    jsQRLoading = true
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/jsqr@1.4.0/dist/jsQR.min.js'
    script.onload = () => {
      jsQR = window.jsQR
      jsQRLoading = false
      if (jsQR) {
        resolve(jsQR)
      } else {
        reject(new Error('jsQR 加载后未找到'))
      }
    }
    script.onerror = (error) => {
      console.error('jsQR 库加载失败:', error)
      jsQRLoading = false
      reject(new Error('jsQR 库加载失败'))
    }
    document.head.appendChild(script)
  })
}

// API配置
const apiBase = (baseURL || '').trim().replace(/\/api\/?$/, '') || 'http://localhost:8080'
const api = axios.create({
  baseURL: apiBase,
  timeout: 12000,
})

api.interceptors.request.use(
  (config) => {
    const token =
      localStorage.getItem('vue-admin-better-2024') ||
      sessionStorage.getItem('vue-admin-better-2024') ||
      localStorage.getItem('token') ||
      ''
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response && error.response.status === 401) {
      ElMessage.error('Token无效或已过期，请重新登录')
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    }
    return Promise.reject(error)
  },
)

// 状态
const activeTab = ref('manual')
const verifying = ref(false)
const scanning = ref(false)
const loadingHistory = ref(false)
const verifyForm = ref({
  verifyCode: '',
})
const verifyResult = ref(null)
const verifyHistory = ref([])

// 摄像头相关
const videoRef = ref(null)
const canvasRef = ref(null)
let stream = null
let scanInterval = null

// 服务类型名称映射
const serviceTypeMap = {
  hospital: '宠物医院',
  grooming: '宠物洗护',
  adoption: '宠物领养',
  'door-cleaning': '上门铲屎',
  litter: '上门铲屎',
}

const getServiceTypeName = (type) => {
  return serviceTypeMap[type] || type || '-'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 核销验证
const handleVerify = async () => {
  if (!verifyForm.value.verifyCode || !verifyForm.value.verifyCode.trim()) {
    ElMessage.warning('请输入核销码')
    return
  }

  verifying.value = true
  verifyResult.value = null

  const verifyCode = verifyForm.value.verifyCode.trim()

  try {
    const res = await api.post('/api/verify/verify-code', {
      verifyCode: verifyCode,
    })

    if (res.code === 200 || res.code === 0) {
      // 后端返回的数据结构：{ appointment: {...}, serviceType: "hospital" }
      const appointmentData = res.data?.appointment || res.data
      verifyResult.value = {
        success: true,
        message: res.msg || '核销成功',
        data: appointmentData,
      }
      ElMessage.success('核销成功')
      // 清空输入框
      verifyForm.value.verifyCode = ''
      // 核销结果已由后端持久化（预约表 is_verified/verify_time），
      // 直接刷新服务端今日核销记录，不再双写 localStorage
      loadVerifyHistory()
    } else {
      verifyResult.value = {
        success: false,
        message: res.msg || '核销失败',
      }
      ElMessage.error(res.msg || '核销失败')
    }
  } catch (error) {
    console.error('核销失败:', error)
    const errorMsg =
      error.response?.data?.msg || error.message || '核销失败，请稍后重试'
    verifyResult.value = {
      success: false,
      message: errorMsg,
    }
    ElMessage.error(errorMsg)
  } finally {
    verifying.value = false
  }
}

// 启动摄像头扫描
const startScan = async () => {
  try {
    // 检查浏览器支持
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      ElMessage.error('您的浏览器不支持摄像头功能，请使用 Chrome 或 Edge 浏览器')
      return
    }

    // 先切换到扫描标签页，确保视频元素已渲染
    if (activeTab.value !== 'scan') {
      activeTab.value = 'scan'
      // 等待标签页切换和 DOM 渲染
      await nextTick()
      await nextTick() // 双重 nextTick 确保渲染完成
      await new Promise(resolve => setTimeout(resolve, 200))
    }

    // 使用多种方式检查视频元素是否存在
    let videoElement = videoRef.value
    if (!videoElement) {
      // 尝试通过 DOM 查询
      await nextTick()
      videoElement = document.querySelector('.video-preview')
      if (!videoElement) {
        console.error('视频元素未找到，videoRef:', videoRef.value)
        ElMessage.error('视频元素未找到，请刷新页面重试')
        return
      }
      // 如果通过 DOM 找到了，更新 ref
      if (videoRef.value !== videoElement) {
        console.warn('通过 DOM 找到视频元素，但 ref 未绑定')
      }
    }

    // 加载 jsQR 库
    await loadJsQR()

    // 请求摄像头权限
    const constraints = {
      video: {
        facingMode: 'environment', // 优先使用后置摄像头
        width: { ideal: 1280 },
        height: { ideal: 720 },
      },
    }

    stream = await navigator.mediaDevices.getUserMedia(constraints)

    // 再次确认视频元素
    videoElement = videoRef.value || document.querySelector('.video-preview')
    if (!videoElement) {
      stream.getTracks().forEach((track) => track.stop())
      ElMessage.error('视频元素未找到，请刷新页面重试')
      return
    }

    // 设置视频流
    videoElement.srcObject = stream
    
    // 等待视频加载
    videoElement.onloadedmetadata = () => {
      if (videoElement) {
        videoElement.play().then(() => {
          scanning.value = true
          // 再次等待，确保扫描状态已更新和DOM渲染完成
          nextTick().then(() => {
            // 额外等待，确保视频画面已经显示
            setTimeout(() => {
              startQRCodeScan()
            }, 500)
          })
        }).catch((err) => {
          console.error('视频播放失败:', err)
          ElMessage.error('视频播放失败，请检查浏览器设置')
          stopScan()
        })
      }
    }

    // 添加错误处理
    videoElement.onerror = (err) => {
      console.error('视频加载错误:', err)
      ElMessage.error('视频加载失败')
      stopScan()
    }
  } catch (error) {
    console.error('启动摄像头失败:', error)
    let errorMsg = '无法访问摄像头'
    if (error.name === 'NotAllowedError') {
      errorMsg = '摄像头权限被拒绝，请在浏览器设置中允许访问摄像头'
    } else if (error.name === 'NotFoundError') {
      errorMsg = '未找到摄像头设备'
    } else if (error.name === 'NotReadableError') {
      errorMsg = '摄像头被其他应用占用'
    }
    ElMessage.error(errorMsg)
    scanning.value = false
    if (stream) {
      stream.getTracks().forEach((track) => track.stop())
      stream = null
    }
  }
}

// 停止扫描
const stopScan = () => {
  if (stream) {
    stream.getTracks().forEach((track) => track.stop())
    stream = null
  }
  if (scanInterval) {
    clearInterval(scanInterval)
    scanInterval = null
  }
  scanning.value = false
  if (videoRef.value) {
    videoRef.value.srcObject = null
  }
}

// 开始二维码扫描
const startQRCodeScan = () => {
  if (!jsQR) {
    console.error('jsQR 库未加载')
    ElMessage.error('二维码识别库加载失败，请刷新页面重试')
    return
  }

  // 清除之前的扫描间隔
  if (scanInterval) {
    clearInterval(scanInterval)
  }

  let scanCount = 0
  scanInterval = setInterval(() => {
    scanCount++
    
    // 尝试多种方式获取视频和 canvas 元素
    let video = videoRef.value
    let canvas = canvasRef.value
    
    // 如果 ref 找不到，尝试通过 DOM 查询
    if (!video) {
      video = document.querySelector('.video-preview')
    }
    if (!canvas) {
      canvas = document.querySelector('canvas')
    }

    if (!video || !canvas) {
      if (scanCount % 25 === 0) { // 每5秒输出一次日志
        console.warn('视频或 canvas 元素未找到', {
          video: !!video,
          canvas: !!canvas,
          videoRef: !!videoRef.value,
          canvasRef: !!canvasRef.value,
          scanning: scanning.value
        })
      }
      return
    }

    if (!scanning.value) {
      return
    }

    if (video.readyState === video.HAVE_ENOUGH_DATA && video.videoWidth > 0 && video.videoHeight > 0) {
      try {
        // 设置 canvas 尺寸
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight

        const context = canvas.getContext('2d', {
          willReadFrequently: true // 优化频繁读取性能
        })
        if (!context) {
          console.error('无法获取 canvas context')
          return
        }

        // 绘制视频帧到 canvas
        context.drawImage(video, 0, 0, canvas.width, canvas.height)

        // 获取完整图像数据（扫描整个画面，确保能识别到二维码）
        const imageData = context.getImageData(0, 0, canvas.width, canvas.height)

        // 使用 jsQR 识别二维码
        // 尝试多种识别选项和参数
        let code = null
        const attempts = [
          { inversionAttempts: 'attemptBoth' },
          { inversionAttempts: 'dontInvert' },
          { inversionAttempts: 'onlyInvert' },
          { inversionAttempts: 'invertFirst' },
        ]
        
        // 确保 imageData 有效
        if (!imageData || !imageData.data || !imageData.width || !imageData.height) {
          console.error('图像数据无效:', imageData)
          return
        }
        
        for (const attempt of attempts) {
          try {
            code = jsQR(imageData.data, imageData.width, imageData.height, attempt)
            if (code && code.data && code.data.trim()) {
              break
            }
          } catch (e) {
            // 某个识别选项失败，继续尝试下一个
            continue
          }
        }
        
        // 如果还是识别不到，尝试缩放图像（可能是分辨率问题）
        if (!code || !code.data) {
          try {
            const scale = 2
            const scaledWidth = Math.floor(imageData.width / scale)
            const scaledHeight = Math.floor(imageData.height / scale)
            
            // 确保缩放后的尺寸有效
            if (scaledWidth > 0 && scaledHeight > 0) {
              const scaledCanvas = document.createElement('canvas')
              const scaledCtx = scaledCanvas.getContext('2d')
              scaledCanvas.width = scaledWidth
              scaledCanvas.height = scaledHeight
              scaledCtx.drawImage(canvas, 0, 0, scaledWidth, scaledHeight)
              const scaledImageData = scaledCtx.getImageData(0, 0, scaledWidth, scaledHeight)
              
              if (scaledImageData && scaledImageData.data && scaledImageData.width && scaledImageData.height) {
                for (const attempt of attempts) {
                  try {
                    code = jsQR(scaledImageData.data, scaledWidth, scaledHeight, attempt)
                    if (code && code.data && code.data.trim()) {
                      break
                    }
                  } catch (e) {
                    continue
                  }
                }
              }
            }
          } catch (e) {
            console.warn('图像缩放识别失败:', e)
          }
        }

        if (code) {
          // 检查二维码数据
          let qrData = code.data || ''
          
          // 如果 data 为空，尝试从 chunks 中提取数据
          if (!qrData && code.chunks && code.chunks.length > 0) {
            try {
              // 尝试多种方式提取数据
              for (let i = 0; i < code.chunks.length; i++) {
                const chunk = code.chunks[i]

                // 方法1: 直接从 chunk 的 bytes 属性提取
                if (chunk.bytes && Array.isArray(chunk.bytes) && chunk.bytes.length > 0) {
                  try {
                    const decoded = new TextDecoder('utf-8').decode(new Uint8Array(chunk.bytes))
                    if (decoded && decoded.trim()) {
                      qrData = decoded
                      break
                    }
                  } catch (e) {
                    try {
                      const decoded = String.fromCharCode.apply(null, chunk.bytes)
                      if (decoded && decoded.trim()) {
                        qrData = decoded
                        break
                      }
                    } catch (e2) {
                      console.warn(`Chunk ${i} 文本解码失败:`, e2)
                    }
                  }
                }

                // 方法2: 从 chunk 的 text 属性提取
                if (chunk.text && chunk.text.trim()) {
                  qrData = chunk.text
                  break
                }

                // 方法3: 从 chunk 的 data 属性提取
                if (chunk.data && chunk.data.trim()) {
                  qrData = chunk.data
                  break
                }

                // 方法4: 尝试直接访问 chunk 的所有属性
                for (const key in chunk) {
                  if (Object.prototype.hasOwnProperty.call(chunk, key) && typeof chunk[key] === 'string' && chunk[key].trim()) {
                    qrData = chunk[key]
                    break
                  }
                }

                if (qrData) break
              }
              
              if (!qrData) {
                console.warn('所有 chunks 提取方法都失败，chunks 内容:', code.chunks)
              }
            } catch (e) {
              console.error('从 chunks 提取数据失败:', e)
            }
          }
          
          if (qrData && qrData.trim()) {
            // 停止扫描
            clearInterval(scanInterval)
            scanInterval = null
            handleQRCodeResult(qrData)
          } else {
            // 识别到二维码但内容为空
            // 检查是否是占位符二维码（SVG格式）
            const isPlaceholder = code.location &&
              (code.location.topLeftCorner || code.location.topRightCorner)

            // 减少提示频率，避免刷屏
            if (scanCount === 1) {
              console.warn('识别到二维码但内容为空，可能是占位符图形', { isPlaceholder })
            } else if (scanCount % 100 === 0) {
              console.warn('持续识别到空二维码', scanCount)
            }
          }
        }
      } catch (error) {
        console.error('二维码识别错误:', error)
        // 不中断扫描，继续尝试
      }
    } else {
      if (scanCount % 25 === 0) {
        console.warn('视频未准备好', {
          readyState: video.readyState,
          videoWidth: video.videoWidth,
          videoHeight: video.videoHeight
        })
      }
    }
  }, 200) // 每200ms扫描一次
}

// 处理二维码识别结果
const handleQRCodeResult = (qrData) => {
  if (!qrData || !qrData.trim()) {
    ElMessage.warning('二维码内容为空')
    return
  }

  try {
    // 尝试解析JSON格式的二维码数据
    const data = JSON.parse(qrData)

    if (data.t === 'verify' && data.c) {
      // JSON格式：{"t":"verify","c":"核销码"}
      const verifyCode = String(data.c).trim()
      verifyForm.value.verifyCode = verifyCode
      stopScan()
      ElMessage.success('识别成功，正在核销...')
      // 延迟一下，确保状态更新
      setTimeout(() => {
        handleVerify()
      }, 100)
    } else if (data.verifyCode) {
      // 其他可能的JSON格式
      verifyForm.value.verifyCode = data.verifyCode
      stopScan()
      ElMessage.success('识别成功，正在核销...')
      handleVerify()
    } else {
      ElMessage.warning('二维码格式不正确，请使用有效的核销码')
      console.warn('无效的二维码格式:', data)
    }
  } catch (e) {
    // 如果不是JSON格式，直接作为核销码处理
    verifyForm.value.verifyCode = qrData.trim()
    stopScan()
    ElMessage.success('识别成功，正在核销...')
    handleVerify()
  }
}

// 上传二维码图片
const handleUploadQRCode = async (file) => {
  try {
    // 加载 jsQR 库
    await loadJsQR()

    const reader = new FileReader()
    reader.onload = async (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        const ctx = canvas.getContext('2d')
        canvas.width = img.width
        canvas.height = img.height
        ctx.drawImage(img, 0, 0)
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)

        try {
          const code = jsQR(imageData.data, imageData.width, imageData.height, {
            inversionAttempts: 'dontInvert',
          })
          if (code) {
            handleQRCodeResult(code.data)
          } else {
            ElMessage.error('无法识别二维码，请确保图片清晰且包含有效的核销码')
          }
        } catch (error) {
          console.error('二维码识别失败:', error)
          ElMessage.error('二维码识别失败，请确保图片清晰')
        }
      }
      img.onerror = () => {
        ElMessage.error('图片加载失败')
      }
      img.src = e.target.result
    }
    reader.onerror = () => {
      ElMessage.error('文件读取失败')
    }
    reader.readAsDataURL(file)
  } catch (error) {
    console.error('加载二维码识别库失败:', error)
    ElMessage.error('二维码识别库加载失败，请刷新页面重试')
  }
  return false // 阻止自动上传
}

// 加载核销历史（服务端持久化，唯一数据源；不再回退 localStorage，
// 避免「本机缓存旧记录/他机记录」与真实核销数据混淆）
const loadVerifyHistory = async () => {
  loadingHistory.value = true
  try {
    const res = await api.get('/api/verify/today-records')
    if (res && (res.code === 200 || res.code === 0) && Array.isArray(res.data)) {
      verifyHistory.value = res.data.map((row) => ({
        id: row.id,
        serviceType: row.serviceType,
        contactName: row.contactName,
        contactPhone: row.contactPhone,
        verifyCode: row.verifyCode,
        verifyTime: row.verifyTime,
      }))
    } else {
      verifyHistory.value = []
      ElMessage.error(res?.msg || '加载核销记录失败')
    }
  } catch (error) {
    console.error('加载核销历史失败:', error)
    verifyHistory.value = []
    ElMessage.error('加载核销记录失败，请稍后重试')
  } finally {
    loadingHistory.value = false
  }
}

// 刷新页面
const refreshPage = () => {
  verifyForm.value.verifyCode = ''
  verifyResult.value = null
  loadVerifyHistory()
}

// 组件卸载时清理
onUnmounted(() => {
  stopScan()
})

// 初始化
onMounted(() => {
  loadVerifyHistory()
})
</script>

<style scoped>
.verify-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.verify-tabs {
  margin-bottom: 20px;
}

.manual-verify-section {
  padding: 20px 0;
}

.scan-verify-section {
  padding: 20px 0;
}

.scan-actions {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.camera-preview {
  position: relative;
  width: 100%;
  max-width: 640px;
  margin: 20px auto;
  border: 2px solid #409eff;
  border-radius: 8px;
  overflow: hidden;
  background: #000;
}

.video-preview {
  width: 100%;
  height: auto;
  display: block;
}

.scan-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.scan-frame {
  width: 250px;
  height: 250px;
  border: 3px solid #67c23a;
  border-radius: 8px;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
  position: relative;
  overflow: hidden;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(to right, transparent, #67c23a, transparent);
  animation: scanLine 2s linear infinite;
}

@keyframes scanLine {
  0% {
    top: 0;
  }
  100% {
    top: 100%;
  }
}

.scan-tip {
  margin-top: 20px;
  color: #fff;
  font-size: 14px;
  text-align: center;
  background: rgba(0, 0, 0, 0.6);
  padding: 8px 16px;
  border-radius: 4px;
}

.stop-scan-btn {
  margin-top: 20px;
}

.verify-result {
  margin: 20px 0;
}

.result-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.result-icon {
  flex-shrink: 0;
}

.result-text {
  flex: 1;
}

.result-text h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
}

.result-text p {
  margin: 0;
  color: #666;
}

.success-card {
  border-left: 4px solid #67c23a;
}

.error-card {
  border-left: 4px solid #f56c6c;
}

.order-info {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.verify-history {
  margin-top: 30px;
}
</style>

