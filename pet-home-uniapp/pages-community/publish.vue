<template>
  <view class="publish-page">
    <scroll-view scroll-y class="content-scroll">
      <view class="header-section">
        <view class="title-row">
          <input 
            class="title-input" 
            v-model="title" 
            placeholder="添加标题"
            :maxlength="50"
          />
        </view>
        <!-- 作品描述（添加作品描述） -->
        <view class="avatar-input-row">
          <view class="input-wrapper">
            <textarea 
              class="content-input" 
              v-model="content" 
              placeholder="添加作品描述"
              :maxlength="1000"
              auto-height
            />
          </view>
        </view>
      </view>
      
      <!-- 图片/视频上传区域 -->
      <view class="image-section">
        <view class="image-grid">
          <view 
            :class="['image-item', item.type === 'video' ? 'video-item' : '']" 
            v-for="(item, index) in previewItems" 
            :key="index"
          >
            <!-- 图片预览 -->
            <view v-if="item.type === 'image'" class="image-preview-wrap">
              <image 
                class="image-preview" 
                :src="item.url || item" 
                mode="aspectFill"
              ></image>
              <!-- 图文帖可选封面：多图时显示「设为封面」按钮 -->
              <view 
                v-if="imageItemIndices.length > 1" 
                :class="['set-cover-btn', 'cover-btn-image', { 'is-cover': selectedCoverImageIndex === getImageIndex(index) }]"
                @click.stop="setImageCover(getImageIndex(index))"
              >
                <text class="cover-btn-text">{{ selectedCoverImageIndex === getImageIndex(index) ? '封面' : '设为封面' }}</text>
              </view>
            </view>
            <!-- 视频预览（竖版 9:16，类似抖音） -->
            <view v-else-if="item.type === 'video'" class="video-preview">
              <image 
                class="video-cover"
                :key="'cover-' + index + '-' + (item.customThumb || item.thumb || '')"
                :src="item.customThumb || item.thumb || ''"
                mode="aspectFill"
                @error="onVideoCoverError(index)"
                v-if="(item.customThumb || item.thumb)"
              ></image>
              <view v-else class="video-placeholder">
                <text class="placeholder-text">选封面</text>
              </view>
              <view class="video-badge">
                <text class="video-icon">▶</text>
              </view>
              <!-- 选封面按钮（小红书式：点击进入选封面页） -->
              <view class="set-cover-btn" @click.stop="goSelectCover(index)">
                <text class="cover-btn-text">选封面</text>
              </view>
            </view>
            <view class="image-delete" @click.stop="removePreview(index)">×</view>
          </view>
          <view 
            class="image-add" 
            v-if="previewItems.length < 9" 
            @click="showMediaPicker"
          >
            <text class="add-icon">+</text>
          </view>
        </view>
      </view>

      <!-- 标签选择（仅管理员预设标签，最多5个，不支持自定义） -->
      <view class="tag-section">
        <view class="tag-title">选择标签（最多5个）</view>
        <view class="tag-list">
          <view
            v-for="(tag, index) in availableTags"
            :key="index"
            class="tag-item"
            :class="{ active: selectedTags.includes(tag) }"
            @click="toggleTag(tag)"
          >
            #{{ tag }}
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部按钮 -->
    <view class="bottom-actions">
      <view class="publish-btn" :class="{ disabled: !canPublish }" @click="publishPost">
        发布帖子
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  data() {
    return {
      title: '',
      content: '',
      previewItems: [], // 预览项数组，每个项包含 {type: 'image'|'video', url: '', path: ''}
      selectedCoverImageIndex: 0, // 图文帖封面：选中的图片索引（在多图时生效）
      availableTags: [], // 标签列表（由后台管理员配置）
      currentLocation: '', // 当前位置信息
      selectedTags: [],
      publishBackTimer: null
    }
  },
  
  computed: {
    canPublish() {
      return (this.title.trim() || this.content.trim()) && this.previewItems.length > 0
    },
    // 所有图片项在 previewItems 中的索引列表
    imageItemIndices() {
      return this.previewItems
        .map((item, i) => (item.type === 'image' ? i : -1))
        .filter(i => i >= 0)
    }
  },
  
  onLoad() {
    this.loadAvailableTags()
    this.getCurrentLocation() // 获取当前位置
  },

  onShow() {
    // 不再使用独立选封面页面，这里无需处理返回结果
  },

  onUnload() {
    if (this.publishBackTimer) {
      clearTimeout(this.publishBackTimer)
      this.publishBackTimer = null
    }
  },
  
  methods: {
    /**
     * 小红书式：手选标签 + 根据正文/媒体自动补标签（仅补后台配置的 availableTags，最多 5 个）
     * 与发现分栏语义一致，未点 #猫咪 也可能因正文出现「猫」被补签
     */
    mergeAutoTagsForPublish() {
      const out = [...this.selectedTags]
      const tryAdd = (name) => {
        if (!name || out.length >= 5 || out.includes(name)) return
        if (this.availableTags.length && !this.availableTags.includes(name)) return
        out.push(name)
      }
      const bag = `${(this.title || '').trim()}\n${(this.content || '').trim()}`
      const hasVideo = this.previewItems.some(i => i.type === 'video')
      if (hasVideo) tryAdd('视频')
      if (!bag.includes('熊猫') && /猫咪|猫猫|喵星人|养猫|喵|猫/.test(bag)) tryAdd('猫咪')
      if (!bag.includes('热狗') && (/狗狗|狗子|养狗|汪星人|柯基|金毛|哈士奇/.test(bag) || (bag.includes('狗') && !bag.includes('热狗')) || bag.includes('犬'))) {
        tryAdd('狗狗')
      }
      if (/科普|攻略|教程|疫苗|驱虫|新手|养宠|怎么养|如何养|症状|医院/.test(bag)) tryAdd('养宠知识')
      return out.slice(0, 5)
    },
    // 从后台加载可用标签（不包含“推荐”）
    async loadAvailableTags() {
      try {
        const response = await api.getDiscoverTags()
        if (response && (response.code === 200 || response.code === 0)) {
          const list = Array.isArray(response.data) ? response.data : []
          this.availableTags = list
            .map(t => (t || '').toString().trim())
            .filter(t => t && t !== '推荐')
        } else {
          this.availableTags = []
        }
      } catch (error) {
        console.error('加载标签失败:', error)
        this.availableTags = []
      }
    },
    
    // 切换标签选择
    toggleTag(tag) {
      const index = this.selectedTags.indexOf(tag)
      if (index > -1) {
        this.selectedTags.splice(index, 1)
      } else {
        if (this.selectedTags.length < 5) { // 最多选择5个标签
          this.selectedTags.push(tag)
        } else {
          uni.showToast({
            title: '最多选择5个标签',
            icon: 'none'
          })
        }
      }
    },
    
    // 显示媒体选择器（图片或视频）
    showMediaPicker() {
      uni.showActionSheet({
        itemList: ['选择图片', '选择视频'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.chooseImage()
          } else if (res.tapIndex === 1) {
            this.chooseVideo()
          }
        }
      })
    },
    
    // 选择图片
    chooseImage() {
      uni.chooseImage({
        count: 9 - this.previewItems.length,
        success: (res) => {
          const newItems = res.tempFilePaths.map(path => ({
            type: 'image',
            url: path,
            path: path
          }))
          this.previewItems.push(...newItems)
        }
      })
    },
    
    // 选择视频（使用 chooseMedia，支持返回缩略图）
    chooseVideo() {
      uni.chooseMedia({
        count: 1,
        mediaType: ['video'],
        sourceType: ['album', 'camera'],
        maxDuration: 60,
        camera: 'back',
        success: (res) => {
          const files = res.tempFiles || []
          const file = files[0]
          if (!file) return
          if (this.previewItems.length >= 9) {
            uni.showToast({
              title: '最多只能添加9个文件',
              icon: 'none'
            })
            return
          }
          const tempFilePath = file.tempFilePath
          const thumbPath = (file.thumbTempFilePath || '').trim()
          const duration = file.duration || res.duration || 0
          const size = file.size || 0

          const fs = uni.getFileSystemManager()
          const maxSelectSize = 300 * 1024 * 1024 // 300MB
          fs.getFileInfo({
            filePath: tempFilePath,
            success: (fileInfo) => {
              const realSize = fileInfo.size || size
              const fileSizeMB = (realSize / (1024 * 1024)).toFixed(2)
              if (realSize > maxSelectSize) {
                uni.showModal({
                  title: '文件过大',
                  content: `视频为 ${fileSizeMB}MB，建议单条不超过 300MB。\n\n可先压缩或缩短后再添加。`,
                  showCancel: false,
                  confirmText: '知道了'
                })
                return
              }
              this.previewItems.push({
                type: 'video',
                url: tempFilePath,
                path: tempFilePath,
                thumb: thumbPath,
                customThumb: null,
                duration,
                size: realSize
              })
            },
            fail: () => {
              this.previewItems.push({
                type: 'video',
                url: tempFilePath,
                path: tempFilePath,
                thumb: thumbPath,
                customThumb: null,
                duration,
                size
              })
            }
          })
        },
        fail: (err) => {
          console.error('选择视频失败:', err)
          uni.showToast({
            title: '选择视频失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 获取某张图片在「图片列表」中的索引（0=第一张图，1=第二张图...）
    getImageIndex(previewIndex) {
      const idx = this.imageItemIndices.indexOf(previewIndex)
      return idx >= 0 ? idx : 0
    },
    // 设为图文帖封面
    setImageCover(imageIndex) {
      if (imageIndex >= 0 && imageIndex < this.imageItemIndices.length) {
        this.selectedCoverImageIndex = imageIndex
      }
    },
    // 选封面：直接从相册选择一张图片作为封面（仅视频）
    goSelectCover(videoIndex) {
      const videoItem = this.previewItems[videoIndex]
      if (!videoItem || videoItem.type !== 'video') {
        return
      }
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: (res) => {
          const filePath = (res.tempFilePaths && res.tempFilePaths[0]) || ''
          if (!filePath) return
          const thumbPath = filePath.trim()
          this.$set(this.previewItems[videoIndex], 'customThumb', thumbPath)
        },
        fail: () => {}
      })
    },
    
    onVideoCoverError(index) {
      // 封面图加载失败（黑屏或路径无效）时清空，显示「选封面」占位
      if (this.previewItems[index] && this.previewItems[index].type === 'video') {
        this.$set(this.previewItems[index], 'customThumb', '')
        this.$set(this.previewItems[index], 'thumb', '')
      }
    },
    removePreview(index) {
      const item = this.previewItems[index]
      if (item && item.type === 'image') {
        const removedImageIdx = this.getImageIndex(index)
        if (removedImageIdx < this.selectedCoverImageIndex) {
          this.selectedCoverImageIndex = Math.max(0, this.selectedCoverImageIndex - 1)
        } else if (removedImageIdx === this.selectedCoverImageIndex && this.imageItemIndices.length > 1) {
          this.selectedCoverImageIndex = Math.min(this.selectedCoverImageIndex, this.imageItemIndices.length - 2)
        }
      }
      this.previewItems.splice(index, 1)
    },
    
    addPreview() {
      this.showMediaPicker()
    },
    
    // 获取当前位置信息（使用IP定位获取真实位置）
    getCurrentLocation() {
      // 检查缓存，避免频繁请求
      const cachedLocation = uni.getStorageSync('cached_location')
      const cacheTime = uni.getStorageSync('cached_location_time')
      const now = Date.now()
      
      // 如果缓存存在且未过期（5分钟内有效），直接使用
      if (cachedLocation && cacheTime && (now - cacheTime < 5 * 60 * 1000)) {
        this.currentLocation = cachedLocation
        return
      }
      
      // 每次调用时先清空之前的位置，确保是实时获取
      this.currentLocation = ''
      
      // 使用免费的IP定位API获取真实位置（基于网络IP地址）
      // 使用ip-api.com，免费且稳定
      uni.request({
        url: 'https://ip-api.com/json/?lang=zh-CN&fields=status,message,regionName,country',
        method: 'GET',
        timeout: 5000,
        success: (res) => {
          if (res.data && res.data.status === 'success' && res.data.regionName) {
            let province = res.data.regionName
            // 去掉"省"、"市"、"自治区"等后缀
            province = province.replace(/省$|市$|自治区$|特别行政区$|壮族自治区$|维吾尔自治区$|回族自治区$/, '')
            this.currentLocation = province
            
            // 缓存位置信息（5分钟有效）
            uni.setStorageSync('cached_location', province)
            uni.setStorageSync('cached_location_time', now)
          } else {
            this.currentLocation = ''
          }
        },
        fail: (err) => {
          this.currentLocation = ''
        }
      })
    },
    
    // 逆地理编码：通过经纬度获取地址（只获取省份）
    reverseGeocode(latitude, longitude) {
      // 验证经纬度是否有效
      if (!latitude || !longitude || latitude === 0 || longitude === 0) {
        this.currentLocation = ''
        return
      }
      
      // 使用腾讯地图逆地理编码API
      uni.request({
        url: `https://apis.map.qq.com/ws/geocoder/v1/?location=${latitude},${longitude}&key=OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77&get_poi=0`,
        method: 'GET',
        success: (res) => {
          if (res.data && res.data.status === 0 && res.data.result) {
            const address = res.data.result.address_component
            if (address && address.province) {
              // 只显示省份，去掉后缀
              let province = address.province
              province = province.replace(/省$|市$|自治区$|特别行政区$/, '')
              this.currentLocation = province
            } else {
              // 如果无法获取省份，不显示位置
              this.currentLocation = ''
            }
          } else {
            // 逆地理编码失败，不显示位置
            this.currentLocation = ''
          }
        },
        fail: (err) => {
          // 逆地理编码请求失败，不显示位置
          this.currentLocation = ''
        }
      })
    },
    
    showLocationPicker() {
      uni.showToast({
        title: '地点功能待开发',
        icon: 'none'
      })
    },
    
    saveDraft() {
      uni.showToast({
        title: '草稿功能待开发',
        icon: 'none'
      })
    },
    
    async publishPost() {
      if (!this.canPublish) {
        uni.showToast({
          title: '请添加标题或作品描述和至少一张图片/视频',
          icon: 'none'
        })
        return
      }
      
      let publishTimedOut = false
      const publishTimeoutMs = 120000 // 120 秒未完成则提示超时
      const timeoutId = setTimeout(() => {
        publishTimedOut = true
        uni.hideLoading()
        uni.showToast({
          title: '发布超时，请检查网络后重试',
          icon: 'none',
          duration: 3000
        })
      }, publishTimeoutMs)
      
      try {
        uni.showLoading({
          title: '发布中...',
          mask: true
        })
        
        // 发布前，重新获取一次真实位置（确保是实时的）
        await new Promise((resolve) => {
          // 先清空之前的位置
          this.currentLocation = ''
          // 重新获取位置
          this.getCurrentLocation()
          // 等待定位完成（最多等待3秒）
          let checkCount = 0
          const checkInterval = setInterval(() => {
            checkCount++
            if (this.currentLocation || checkCount >= 30) {
              clearInterval(checkInterval)
              resolve()
            }
          }, 100)
        })
        
        // 先上传所有媒体文件到服务器（图片和视频）
        let uploadedImages = []
        let uploadedVideos = []
        if (this.previewItems.length > 0) {
          for (let i = 0; i < this.previewItems.length; i++) {
            const item = this.previewItems[i]
            const tempPath = item.path || item.url || item
            
            if (item.type === 'video') {
              uni.showLoading({ title: '视频上传中，请稍候...', mask: true })
              const maxVideoSize = 300 * 1024 * 1024 // 300MB，超出再提示；≤300MB 走分块上传
              let fileSize = item.size || 0
              if (fileSize > 0 && fileSize > maxVideoSize) {
                clearTimeout(timeoutId)
                const fileSizeMB = (fileSize / (1024 * 1024)).toFixed(2)
                uni.hideLoading()
                uni.showModal({
                  title: '文件过大',
                  content: `视频为 ${fileSizeMB}MB，建议单条不超过 300MB。`,
                  showCancel: false,
                  confirmText: '知道了'
                })
                return
              }
              try {
                const fileInfo = await new Promise((resolve, reject) => {
                  const fs = uni.getFileSystemManager()
                  fs.getFileInfo({
                    filePath: tempPath,
                    success: (res) => resolve({ size: res.size }),
                    fail: reject
                  })
                })
                fileSize = fileInfo.size || fileSize
                if (fileSize > maxVideoSize) {
                  clearTimeout(timeoutId)
                  const fileSizeMB = (fileSize / (1024 * 1024)).toFixed(2)
                  uni.hideLoading()
                  uni.showModal({
                    title: '文件过大',
                    content: `视频为 ${fileSizeMB}MB，建议单条不超过 300MB。`,
                    showCancel: false,
                    confirmText: '知道了'
                  })
                  return
                }
              } catch (err) {
                // 如果无法获取文件大小，且之前也没有大小信息，给出警告
                if (fileSize === 0 || item.needSizeCheck) {
                  // 继续尝试上传，让微信小程序自己检查并返回错误
                }
              }
              
              // 上传视频
              try {
                const uploadedUrl = await this.uploadVideo(tempPath)
                if (uploadedUrl) {
                  // 优先使用用户自定义封面，否则使用系统生成的封面
                  let thumbUrl = null
                  if (item.customThumb) {
                    // 上传用户自定义的封面
                    thumbUrl = await this.uploadImage(item.customThumb)
                  } else if (item.thumb) {
                    // 使用系统自动生成的封面
                    thumbUrl = await this.uploadImage(item.thumb)
                  }
                  
                  uploadedVideos.push({
                    url: uploadedUrl,
                    thumb: thumbUrl,
                    duration: item.duration || 0
                  })
                }
              } catch (uploadError) {
                // 如果是文件大小超限错误，直接抛出，让外层catch处理
                if (uploadError.message && (uploadError.message.includes('50MB') || uploadError.message.includes('4MB') || uploadError.message.includes('80051'))) {
                  throw uploadError
                }
                // 其他错误也抛出
                throw uploadError
              }
            } else {
              // 上传图片
              const uploadedUrl = await this.uploadImage(tempPath)
              if (uploadedUrl) {
                uploadedImages.push(uploadedUrl)
              }
            }
          }
        }
        
        // 获取当前登录用户ID（必须为有效数字，否则后端会报错）
        let currentUserId = null
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo && (userInfo.id != null || userInfo.uid != null)) {
          currentUserId = userInfo.id != null ? userInfo.id : userInfo.uid
        }
        if (currentUserId == null || currentUserId === '' || currentUserId === 'undefined') {
          currentUserId = uni.getStorageSync('userId')
        }
        const uid = (typeof currentUserId === 'number' && Number.isFinite(currentUserId))
          ? currentUserId
          : (currentUserId != null && currentUserId !== '' && currentUserId !== 'undefined' ? Number(currentUserId) : NaN)
        if (!Number.isFinite(uid) || uid <= 0) {
          clearTimeout(timeoutId)
          uni.hideLoading()
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          return
        }
        
        // 封面：视频帖用视频缩略图，图文帖用用户选中的封面图（支持多图时选择）
        let coverImage = null
        if (uploadedVideos.length > 0 && uploadedVideos[0].thumb) {
          coverImage = uploadedVideos[0].thumb
        } else if (uploadedImages.length > 0) {
          const idx = Math.min(this.selectedCoverImageIndex, uploadedImages.length - 1)
          coverImage = uploadedImages[idx]
        }

        // 调用发布API（userId 必须为数字）
        const postData = {
          userId: uid,
          title: (this.title || '').trim() || (this.content || '').substring(0, 50),
          content: this.content.trim(),
          category: '推荐',
          images: JSON.stringify(uploadedImages),
          imageCount: uploadedImages.length,
          videos: uploadedVideos.length > 0 ? JSON.stringify(uploadedVideos) : null, // 视频信息
          videoCount: uploadedVideos.length,
          coverImage: coverImage,
          tags: this.mergeAutoTagsForPublish(),
          petId: null, // 发帖不再关联宠物
          location: this.currentLocation || null // 添加位置信息
        }
        
        uni.showLoading({ title: '发布中...', mask: true })
        const response = await api.publishPost(postData)
        
        clearTimeout(timeoutId)
        if (publishTimedOut) return
        
        const ok = response && (response.code === 200 || response.code === 0 || response.success === true)
        if (ok) {
          uni.hideLoading()
          uni.showToast({
            title: '已提交，等待审核',
            icon: 'success'
          })
          this.publishBackTimer = setTimeout(() => {
            this.publishBackTimer = null
            util.navigateBack()
          }, 1500)
        } else {
          throw new Error((response && (response.msg || response.message)) || '发布失败')
        }
        
      } catch (error) {
        clearTimeout(timeoutId)
        if (!publishTimedOut) uni.hideLoading()
        console.error('发布失败:', error)
        
        const errorMsg = error.message || error.errMsg || '发布失败'
        if (errorMsg.includes('50MB') || errorMsg.includes('4MB') || errorMsg.includes('80051') || errorMsg.includes('exceed') || errorMsg.includes('max limit')) {
          uni.showModal({
            title: '上传失败',
            content: errorMsg.includes('文件大小') ? errorMsg : '视频上传失败，请检查网络后重试。大视频会自动分块上传。',
            showCancel: false,
            confirmText: '知道了'
          })
        } else {
          uni.showToast({
            title: errorMsg.length > 20 ? errorMsg.slice(0, 20) + '...' : errorMsg,
            icon: 'none',
            duration: 3000
          })
        }
      }
    },
    
    // 上传图片到服务器（真机测试需在应用内设置后端地址为电脑局域网 IP）
    async uploadImage(filePath) {
      let localPath = filePath
      if (typeof filePath === 'string' && (filePath.startsWith('http://') || filePath.startsWith('https://'))) {
        try {
          const res = await new Promise((resolve, reject) => {
            uni.downloadFile({
              url: filePath,
              success: resolve,
              fail: reject
            })
          })
          if (res.tempFilePath) localPath = res.tempFilePath
        } catch (e) {
          console.warn('下载封面失败，尝试直接上传:', e)
        }
      }
      return new Promise((resolve, reject) => {
        const baseUrl = util.getApiBaseUrl()
        uni.uploadFile({
          url: baseUrl + '/api/upload/image',
          filePath: localPath,
          name: 'file',
          timeout: 120000,
          success: (res) => {
            try {
              const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
              if (data.code === 200 || data.code === 0) {
                const imageUrl = data.data?.url || data.data
                resolve(imageUrl)
              } else {
                reject(new Error(data.msg || '上传失败'))
              }
            } catch (e) {
              reject(new Error('解析响应失败'))
            }
          },
          fail: (err) => {
            const errMsg = String(err && (err.errMsg || err.message || err) || '')
            if (errMsg.includes('timeout') || errMsg.includes('超时')) {
              reject(new Error('图片上传超时，请检查网络后重试'))
              return
            }
            reject(err)
          }
        })
      })
    },
    
    // 微信单次上传限制 10MB，分块单块用 8MB 更稳妥
    CHUNK_SIZE() {
      return 8 * 1024 * 1024
    },

    // 上传视频到服务器（≤8MB 整块上传，>8MB 分块上传突破微信 10MB 限制）
    uploadVideo(filePath) {
      const baseUrl = util.getApiBaseUrl()
      const chunkSize = this.CHUNK_SIZE()

      return new Promise((resolve, reject) => {
        const fs = uni.getFileSystemManager()
        if (!fs) {
          reject(new Error('当前环境不支持文件读取'))
          return
        }
        fs.getFileInfo({
          filePath,
          success: (info) => {
            const fileSize = info.size || 0
            if (fileSize <= 0) {
              this._uploadVideoSingle(baseUrl, filePath, resolve, reject)
              return
            }
            if (fileSize <= chunkSize) {
              this._uploadVideoSingle(baseUrl, filePath, resolve, reject)
              return
            }
            this._uploadVideoChunked(baseUrl, filePath, fileSize, chunkSize, resolve, reject)
          },
          fail: () => {
            this._uploadVideoSingle(baseUrl, filePath, resolve, reject)
          }
        })
      })
    },

    _uploadVideoSingle(baseUrl, filePath, resolve, reject) {
      uni.uploadFile({
        url: baseUrl + '/api/upload/video',
        filePath,
        name: 'file',
        timeout: 120000,
        success: (res) => {
          try {
            const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
            if (data.code === 200 || data.code === 0) {
              resolve(data.data?.url || data.data)
            } else {
              reject(new Error(data.msg || '视频上传失败'))
            }
          } catch (e) {
            reject(new Error('解析响应失败'))
          }
        },
        fail: (err) => {
          this._handleVideoUploadFail(err, reject)
        }
      })
    },

    _uploadVideoChunked(baseUrl, filePath, fileSize, chunkSize, resolve, reject) {
      const fs = uni.getFileSystemManager()
      const uploadId = Date.now() + '_' + Math.random().toString(36).slice(2, 12)
      const totalChunks = Math.ceil(fileSize / chunkSize)
      const userDataPath = (typeof uni !== 'undefined' && uni.env && uni.env.USER_DATA_PATH) ? uni.env.USER_DATA_PATH : ''

      if (!userDataPath) {
        reject(new Error('分块上传需要小程序环境'))
        return
      }

      fs.readFile({
        filePath,
        encoding: undefined,
        success: (readRes) => {
          const ab = readRes.data
          if (!ab || !(ab instanceof ArrayBuffer)) {
            reject(new Error('读取视频失败'))
            return
          }
          const uploadChunk = (index) => {
            if (index >= totalChunks) {
              const mergeUrl = baseUrl + '/api/upload/video/merge?uploadId=' + encodeURIComponent(uploadId) + '&totalChunks=' + totalChunks + '&filename=' + encodeURIComponent('video.mp4')
              uni.request({
                url: mergeUrl,
                method: 'POST',
                success: (mergeRes) => {
                  const data = mergeRes.data
                  if (data && (data.code === 200 || data.code === 0)) {
                    resolve(data.data || '')
                  } else {
                    reject(new Error(data && data.msg ? data.msg : '合并失败'))
                  }
                },
                fail: (e) => reject(new Error('合并请求失败: ' + (e.errMsg || e.message)))
              })
              return
            }
            const start = index * chunkSize
            const end = Math.min(start + chunkSize, fileSize)
            const chunkBuf = ab.slice(start, end)
            const chunkPath = `${userDataPath}/video_${uploadId}_${index}.part`
            fs.writeFile({
              filePath: chunkPath,
              data: chunkBuf,
              encoding: undefined,
              success: () => {
                uni.uploadFile({
                  url: baseUrl + '/api/upload/video/chunk',
                  filePath: chunkPath,
                  name: 'file',
                  formData: { uploadId, chunkIndex: String(index), totalChunks: String(totalChunks) },
                  timeout: 60000,
                  success: (upRes) => {
                    try {
                      const data = typeof upRes.data === 'string' ? JSON.parse(upRes.data) : upRes.data
                      if (data && (data.code === 200 || data.code === 0)) {
                        fs.unlink({ filePath: chunkPath, fail: () => {} })
                        uploadChunk(index + 1)
                      } else {
                        reject(new Error(data && data.msg ? data.msg : '分块上传失败'))
                      }
                    } catch (e) {
                      reject(new Error('分块响应解析失败'))
                    }
                  },
                  fail: (err) => {
                    reject(new Error('分块上传失败: ' + (err.errMsg || err.message)))
                  }
                })
              },
              fail: () => reject(new Error('写入分块失败'))
            })
          }
          uploadChunk(0)
        },
        fail: () => reject(new Error('读取视频文件失败，请重试'))
      })
    },

    _handleVideoUploadFail(err, reject) {
      const errMsg = String(err.errMsg || err.message || err || '')
      const errCode = err.errCode || err.code || err.errno || ''
      const isSizeError = errCode === 80051 || String(errCode).includes('80051') || errMsg.includes('80051') || errMsg.includes('exceed') || errMsg.includes('max limit') || errMsg.toLowerCase().includes('文件过大')
      const isTimeout = errMsg.includes('timeout') || errMsg.includes('超时')
      if (isTimeout) {
        reject(new Error('视频上传超时，请检查网络后重试'))
        return
      }
      if (isSizeError) {
        reject(new Error('单次上传超过 10MB 限制，将自动使用分块上传；若仍失败请检查网络'))
        return
      }
      reject(new Error('视频上传失败: ' + errMsg))
    }
  }
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background-color: #fff;
  padding-bottom: calc(env(safe-area-inset-bottom) + 100rpx);
}

/* 内容滚动区 */
.content-scroll {
  height: calc(100vh - env(safe-area-inset-bottom) - 100rpx);
}

/* 头部区域 - 标题和描述输入 */
.header-section {
  padding: 30rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.title-row {
  margin-bottom: 16rpx;
}

.title-input {
  width: 100%;
  font-size: 30rpx;
  color: #333;
  line-height: 1.4;
  background-color: transparent;
  border: none;
  padding: 12rpx 0;
}

.avatar-input-row {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.input-wrapper {
  flex: 1;
  min-height: 80rpx;
}

.add-icon-wrapper {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
}

.add-icon-large {
  font-size: 60rpx;
  color: #ccc;
  line-height: 1;
}

.content-scroll {
  height: calc(100vh - env(safe-area-inset-bottom) - 100rpx);
}

/* 图片上传区域 */
.image-section {
  padding: 20rpx;
  background-color: #fff;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.image-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background-color: #f5f5f5;
}

/* 视频项：竖版 9:16，类似抖音预览 */
.video-item {
  width: 200rpx;
  height: 356rpx;
}

.image-preview-wrap {
  position: relative;
  width: 100%;
  height: 100%;
}

.image-preview {
  width: 100%;
  height: 100%;
}

.cover-btn-image.is-cover {
  background-color: rgba(255, 36, 66, 0.9);
}

.video-preview {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: #f5f5f5;
}

.video-cover {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  background-color: #f5f5f5;
}

.video-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0e0e0;
}

.placeholder-text {
  font-size: 24rpx;
  color: #999;
}

.video-player {
  width: 100%;
  height: 100%;
}

.video-badge {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60rpx;
  height: 60rpx;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  z-index: 2;
}

.video-icon {
  color: #fff;
  font-size: 24rpx;
  margin-left: 4rpx; /* 稍微右移，让播放图标看起来更居中 */
}

.set-cover-btn {
  position: absolute;
  bottom: 8rpx;
  left: 50%;
  transform: translateX(-50%);
  padding: 6rpx 16rpx;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 20rpx;
  z-index: 3;
}

.cover-btn-text {
  font-size: 20rpx;
  color: #fff;
  white-space: nowrap;
}

.image-delete {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 44rpx;
  height: 44rpx;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 36rpx;
  line-height: 1;
}

.image-add {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.add-icon {
  font-size: 80rpx;
  color: #ccc;
  line-height: 1;
}

.content-input {
  width: 100%;
  min-height: 200rpx;
  max-height: 600rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  background-color: transparent;
  border: none;
  padding: 20rpx 0;
}

/* 标签 */
.tag-section {
  margin: 0 30rpx 20rpx;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
}

.tag-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 16rpx;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.tag-item {
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 24rpx;
}

.tag-item.active {
  background: #ffe8ec;
  color: #ff2442;
}

/* 底部按钮 */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  display: flex;
  justify-content: center;
  box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 1000;
}

.publish-btn {
  flex: 1;
  height: 64rpx;
  line-height: 64rpx;
  text-align: center;
  background: linear-gradient(135deg, #ff2442 0%, #ff6b9d 100%);
  border-radius: 32rpx;
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(255, 36, 66, 0.3);
}

.publish-btn.disabled {
  background: #e0e0e0;
  color: #999;
  box-shadow: none;
}
</style>