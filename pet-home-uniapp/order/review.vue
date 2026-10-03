<template>
  <view class="review-container">
    <!-- 媒体上传区域 -->
    <view class="media-upload-section">
      <view class="upload-item" @click="uploadImage">
        <view class="upload-icon">📷</view>
        <text class="upload-text">添加图片</text>
      </view>
      <view class="upload-item" @click="uploadVideo">
        <view class="upload-icon">🎥</view>
        <text class="upload-text">添加短视频</text>
      </view>
    </view>

    <!-- 已上传的图片预览 -->
    <view class="image-preview-section" v-if="uploadedImages.length > 0">
      <view class="preview-item" v-for="(image, index) in uploadedImages" :key="index">
        <image :src="image" mode="aspectFill" class="preview-image"></image>
        <view class="delete-btn" @click="deleteImage(index)">×</view>
      </view>
    </view>

    <!-- 评分区域 -->
    <view class="rating-section">
      <view class="product-info">
        <image :src="getImageUrl(productImage)" mode="aspectFill" class="product-thumb"></image>
        <view class="rating-content">
          <text class="rating-label">综合评分</text>
          <view class="stars">
            <text 
              class="star" 
              v-for="(star, index) in 5" 
              :key="index"
              :class="{ active: index < rating }"
              @click="setRating(index + 1)"
            >
              ★
            </text>
          </view>
          <text class="rating-text">{{ ratingText }}</text>
        </view>
      </view>
    </view>

    <!-- 评价文本输入 -->
    <view class="review-input-section">
      <textarea 
        v-model="reviewText"
        placeholder="多多描述商品和使用感受,更受欢迎哦"
        class="review-textarea"
        maxlength="500"
      ></textarea>
      <view class="char-count">{{ reviewText.length }}/500</view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <view class="submit-btn" @click="submitReview" :class="{ disabled: !canSubmit }">
        <text class="submit-text">{{ isAppend ? '提交追加评价' : '提交评价' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { util } from '@/common/js/util.js'
import { api } from '@/common/js/api.js'

export default {
  data() {
    return {
      orderId: '',
      productId: '',
      productImage: '',
      productName: '',
      rating: 5, // 默认5星
      reviewText: '',
      uploadedImages: [],
      uploadedVideos: [],
      isAppend: false
    }
  },

  computed: {
    ratingText() {
      const texts = {
        1: '很差',
        2: '较差',
        3: '一般',
        4: '满意',
        5: '非常满意'
      }
      return texts[this.rating] || '一般'
    },
    canSubmit() {
      return this.rating > 0 && this.reviewText.trim().length > 0
    }
  },

  onLoad(options) {
    // 获取订单和商品信息
    if (options.orderId) {
      this.orderId = options.orderId
    }
    if (options.productId) {
      this.productId = options.productId
    }
    if (options.productImage) {
      this.productImage = decodeURIComponent(options.productImage)
    }
    if (options.productName) {
      this.productName = decodeURIComponent(options.productName)
    }
    this.isAppend = options && String(options.append || '') === '1'

    // 如果没有传参数，尝试从页面参数或存储中获取
    if (!this.orderId || !this.productId) {
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      if (prevPage && prevPage.$vm) {
        // 尝试从上一页获取订单信息
        const orderData = prevPage.$vm.order || prevPage.$vm.orderData
        if (orderData) {
          this.orderId = orderData.id || orderData.orderId
          if (orderData.goods) {
            this.productId = orderData.goods.id
            this.productImage = orderData.goods.image || orderData.goods.pic
            this.productName = orderData.goods.name
          }
        }
      }
    }
  },

  methods: {
    setRating(rating) {
      this.rating = rating
    },

    uploadImage() {
      uni.chooseImage({
        count: 9 - this.uploadedImages.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePaths = res.tempFilePaths
          this.uploadedImages = [...this.uploadedImages, ...tempFilePaths]
        },
        fail: (err) => {
          console.error('选择图片失败:', err)
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },

    uploadVideo() {
      uni.chooseVideo({
        sourceType: ['album', 'camera'],
        maxDuration: 60,
        camera: 'back',
        compressed: true, // 启用视频压缩，减小文件大小
        success: (res) => {
          const tempFilePath = res.tempFilePath
          
          // 使用 getFileInfo 获取准确的文件大小
          uni.getFileInfo({
            filePath: tempFilePath,
            success: (fileInfo) => {
              // 检查文件大小（微信小程序上传限制为4MB）
              const maxSize = 4 * 1024 * 1024 // 4MB，单位：字节
              const fileSize = fileInfo.size || res.size || 0
              const fileSizeMB = (fileSize / (1024 * 1024)).toFixed(2)
              
              if (fileSize > maxSize) {
                uni.showModal({
                  title: '文件过大',
                  content: `视频文件大小为 ${fileSizeMB}MB，超过了微信小程序4MB的上传限制。\n\n建议：\n1. 选择较短的视频（建议30秒以内）\n2. 使用视频压缩工具压缩后再上传\n3. 或使用剪映等软件降低视频质量`,
                  showCancel: false,
                  confirmText: '知道了'
                })
                return
              }
              
              this.uploadedVideos = [tempFilePath]
            },
            fail: (err) => {
              console.error('获取文件信息失败:', err)
              // 如果获取文件信息失败，使用 res.size 作为备用
              const maxSize = 4 * 1024 * 1024
              const fileSize = res.size || 0
              
              if (fileSize > 0 && fileSize > maxSize) {
                const fileSizeMB = (fileSize / (1024 * 1024)).toFixed(2)
                uni.showModal({
                  title: '文件过大',
                  content: `视频文件大小为 ${fileSizeMB}MB，超过了微信小程序4MB的上传限制。\n\n建议：\n1. 选择较短的视频（建议30秒以内）\n2. 使用视频压缩工具压缩后再上传\n3. 或使用剪映等软件降低视频质量`,
                  showCancel: false,
                  confirmText: '知道了'
                })
                return
              }
              
              // 如果无法获取大小，仍然保存但在上传时会再次检查
              this.uploadedVideos = [tempFilePath]
            }
          })
        },
        fail: (err) => {
          // 用户取消选择视频时不显示错误提示
          if (err.errMsg && err.errMsg.includes('cancel')) {
            return
          }
          console.error('选择视频失败:', err)
          uni.showToast({
            title: '选择视频失败',
            icon: 'none'
          })
        }
      })
    },

    deleteImage(index) {
      this.uploadedImages.splice(index, 1)
    },

    // 上传图片到服务器
    async uploadImageToServer(filePath) {
      return new Promise((resolve, reject) => {
        const baseURL = api.getBaseUrl()
        // 获取token
        const token = uni.getStorageSync('token')
        const header = {}
        if (token) {
          header['Authorization'] = `Bearer ${token}`
        }
        
        uni.uploadFile({
          url: `${baseURL}/api/upload/image`,
          filePath: filePath,
          name: 'file',
          formData: {
            type: 'review'
          },
          header: header,
          success: (res) => {
            try {
              const data = JSON.parse(res.data)
              if (data.code === 200 && data.data) {
                // 后端返回的data.data是URL字符串，不是对象
                const imageUrl = typeof data.data === 'string' ? data.data : (data.data.url || data.data.fileUrl)
                resolve(imageUrl)
              } else {
                reject(new Error(data.msg || '上传失败'))
              }
            } catch (e) {
              reject(new Error('解析上传响应失败'))
            }
          },
          fail: (err) => {
            reject(err)
          }
        })
      })
    },

    async uploadVideoToServer(filePath) {
      return new Promise((resolve, reject) => {
        const baseURL = api.getBaseUrl()
        const token = uni.getStorageSync('token')
        const header = {}
        if (token) {
          header['Authorization'] = `Bearer ${token}`
        }
        uni.uploadFile({
          url: `${baseURL}/api/upload/video`,
          filePath: filePath,
          name: 'file',
          header: header,
          success: (res) => {
            try {
              const data = JSON.parse(res.data)
              if (data.code === 200 && data.data) {
                const url = typeof data.data === 'string' ? data.data : (data.data.url || data.data.fileUrl)
                resolve(url)
              } else {
                reject(new Error(data.msg || '视频上传失败'))
              }
            } catch (e) {
              reject(new Error('解析上传响应失败'))
            }
          },
          fail: (err) => reject(err)
        })
      })
    },

    async submitReview() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请填写评价内容',
          icon: 'none'
        })
        return
      }

      if (!this.orderId || !this.productId) {
        uni.showToast({
          title: '订单信息不完整',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({
          title: '提交中...'
        })

        // 获取当前用户ID
        const userId = this.getCurrentUserId()

        let imageUrls = []
        if (this.uploadedImages.length > 0) {
          for (let i = 0; i < this.uploadedImages.length; i++) {
            const imagePath = this.uploadedImages[i]
            const isLocalPath = imagePath && (
              imagePath.startsWith('http://tmp/') ||
              imagePath.startsWith('https://tmp/') ||
              imagePath.startsWith('tmp/') ||
              imagePath.startsWith('wxfile://') ||
              imagePath.startsWith('file://') ||
              (!imagePath.startsWith('http://') && !imagePath.startsWith('https://'))
            )
            if (isLocalPath) {
              const uploadedUrl = await this.uploadImageToServer(imagePath)
              if (uploadedUrl) {
                imageUrls.push(uploadedUrl)
              }
            } else {
              imageUrls.push(imagePath)
            }
          }
        }

        let videoUrls = []
        if (this.uploadedVideos.length > 0) {
          for (let j = 0; j < this.uploadedVideos.length; j++) {
            const vp = this.uploadedVideos[j]
            const isLocal = vp && (
              vp.startsWith('wxfile://') ||
              vp.startsWith('file://') ||
              (!vp.startsWith('http://') && !vp.startsWith('https://'))
            )
            if (isLocal) {
              const vUrl = await this.uploadVideoToServer(vp)
              if (vUrl) {
                videoUrls.push(vUrl)
              }
            } else if (vp) {
              videoUrls.push(vp)
            }
          }
        }

        const reviewData = {
          productId: this.productId,
          userId: userId,
          rating: this.rating,
          comment: this.reviewText.trim(),
          images: imageUrls.length > 0 ? imageUrls.join(',') : null,
          videos: videoUrls.length > 0 ? videoUrls.join(',') : null
        }

        // 调用后端API提交评价，使用商品评价接口
        const response = await api.request({
          url: `/api/product/${this.productId}/reviews`,
          method: 'POST',
          data: reviewData
        })

        uni.hideLoading()

        if (response && (response.code === 200 || response.code === 0)) {
          this.markReviewed()
          uni.showToast({
            title: this.isAppend ? '追加评价成功' : '评价提交成功',
            icon: 'success'
          })

          // 延迟返回，让用户看到成功提示
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: response.message || response.msg || '评价提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('提交评价失败:', error)
        console.error('错误详情:', error.message || error)
        const errorMsg = error.message || '评价提交失败，请稍后重试'
        uni.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        })
      }
    },

    getCurrentUserId() {
      // 从本地存储获取用户ID
      const userId = uni.getStorageSync('userId') || 
                    uni.getStorageSync('uid') || 
                    uni.getStorageSync('user_id')
      
      if (userId) {
        return userId
      }
      
      // 尝试从userInfo对象获取
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo) {
        if (userInfo.id) return userInfo.id
        if (userInfo.uid) return userInfo.uid
        if (userInfo.userId) return userInfo.userId
      }
      
      return null
    },
    markReviewed() {
      const uid = this.getCurrentUserId()
      if (!uid || !this.orderId || !this.productId) return
      const key = `order_reviewed_${uid}_${this.orderId}_${this.productId}`
      uni.setStorageSync(key, '1')
    },

    getImageUrl(imageUrl) {
      if (!imageUrl) {
        return '/static/images/default-product.svg'
      }
      return util.getImageUrl(imageUrl)
    }
  }
}
</script>

<style lang="scss" scoped>
.review-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 100rpx;
}

.media-upload-section {
  display: flex;
  gap: 15rpx;
  padding: 20rpx 30rpx;
  background-color: #fff;
  margin-top: 20rpx;
}

.upload-item {
  flex: 1;
  height: 120rpx;
  border: 2rpx dashed #ddd;
  border-radius: 10rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
}

.upload-icon {
  font-size: 36rpx;
  margin-bottom: 8rpx;
}

.upload-text {
  font-size: 22rpx;
  color: #666;
}

.image-preview-section {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
  padding: 0 30rpx 20rpx;
  background-color: #fff;
}

.preview-item {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  border-radius: 10rpx;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  background-color: #ff4444;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: bold;
}

.rating-section {
  background-color: #fff;
  padding: 20rpx 30rpx;
  margin-top: 15rpx;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.product-thumb {
  width: 100rpx;
  height: 100rpx;
  border-radius: 10rpx;
  background-color: #f5f5f5;
}

.rating-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.rating-label {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.stars {
  display: flex;
  gap: 6rpx;
}

.star {
  font-size: 32rpx;
  color: #ddd;
  line-height: 1;
}

.star.active {
  color: #ff4444;
}

.rating-text {
  font-size: 22rpx;
  color: #666;
  margin-left: auto;
}

.review-input-section {
  background-color: #fff;
  padding: 20rpx 30rpx;
  margin-top: 15rpx;
}

.review-textarea {
  width: 100%;
  min-height: 240rpx;
  padding: 15rpx;
  background-color: #fafafa;
  border-radius: 10rpx;
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
}

.char-count {
  text-align: right;
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15rpx 30rpx;
  padding-bottom: calc(15rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  border-top: 1rpx solid #f0f0f0;
}

.submit-btn {
  width: 100%;
  height: 72rpx;
  background-color: #ff4444;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-btn.disabled {
  background-color: #ccc;
}

.submit-text {
  font-size: 28rpx;
  color: #fff;
  font-weight: 600;
}
</style>