<template>
  <view class="report-container">
    <view class="report-content">
      <view class="section-title">投诉举报</view>
      <view class="section-desc">如果您在使用过程中遇到任何问题或发现违规内容，请通过以下方式联系我们：</view>
      
      <view class="contact-methods">
        <view class="method-item">
          <view class="method-label">客服邮箱</view>
          <view class="method-value" @click="copyEmail">csiyu8596@gmail.com</view>
        </view>
        <view class="method-item">
          <view class="method-label">客服电话</view>
          <view class="method-value" @click="callService">190-0310-1209</view>
        </view>
        <view class="method-item">
          <view class="method-label">在线客服</view>
          <view class="method-value" @click="openOnlineService">点击咨询</view>
        </view>
      </view>
      
      <view class="report-form">
        <view class="form-title">提交投诉</view>
        <view class="form-item">
          <view class="form-label">投诉类型</view>
          <picker mode="selector" :range="reportTypes" :value="selectedType" @change="onTypeChange">
            <view class="picker-view">{{ reportTypes[selectedType] }}</view>
          </picker>
        </view>
        <view class="form-item">
          <view class="form-label">投诉内容</view>
          <textarea 
            v-model="reportContent" 
            placeholder="请详细描述您要投诉的内容..."
            class="textarea-input"
            maxlength="500"
          ></textarea>
          <view class="char-count">{{ reportContent.length }}/500</view>
        </view>
        <view class="form-item">
          <view class="form-label">上传图片（选填）</view>
          <view class="image-upload-row">
            <view
              v-for="(url, idx) in imageList"
              :key="idx"
              class="image-preview"
              @tap="previewImage(idx)"
            >
              <image :src="url" mode="aspectFill" class="preview-img" />
              <view class="remove-btn" @tap.stop="removeImage(idx)">×</view>
            </view>
            <view
              v-if="imageList.length < 9"
              class="image-add"
              @tap="chooseImage"
            >
              <text class="add-text">+</text>
              <text class="add-hint">添加图片</text>
            </view>
          </view>
        </view>
        <view class="form-item">
          <view class="form-label">联系方式（选填）</view>
          <input 
            v-model="contactInfo" 
            placeholder="请输入您的联系方式"
            class="input-field"
          />
        </view>
      </view>
    </view>
    <view class="bottom-bar">
      <button class="submit-btn" @click="submitReport">提交投诉</button>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'Report',
  data() {
    return {
      reportTypes: ['内容违规', '用户行为', '系统问题', '其他'],
      selectedType: 0,
      reportContent: '',
      contactInfo: '',
      imageList: []
    }
  },
  methods: {
    chooseImage() {
      const remain = 9 - this.imageList.length
      if (remain <= 0) {
        uni.showToast({ title: '最多上传9张', icon: 'none' })
        return
      }
      util.chooseImage(remain).then(res => {
        const tempPaths = res.tempFilePaths || []
        if (!tempPaths.length) return
        uni.showLoading({ title: '上传中...' })
        const uploads = tempPaths.map(path => util.uploadImage(path))
        Promise.all(uploads)
          .then(urls => {
            const list = Array.isArray(urls) ? urls : [urls]
            this.imageList = this.imageList.concat(list)
            uni.hideLoading()
            uni.showToast({ title: '图片上传成功', icon: 'none' })
          })
          .catch(err => {
            uni.hideLoading()
            const msg = (err && err.message) ? err.message : '上传失败'
            const cnMsg = msg === 'success' || msg === '上传失败' ? '上传失败' : msg
            uni.showToast({ title: cnMsg, icon: 'none' })
          })
      }).catch(() => {})
    },
    removeImage(idx) {
      this.imageList.splice(idx, 1)
    },
    previewImage(idx) {
      uni.previewImage({
        current: this.imageList[idx],
        urls: this.imageList
      })
    },
    onTypeChange(e) {
      this.selectedType = e.detail.value
    },
    copyEmail() {
      uni.setClipboardData({
        data: 'csiyu8596@gmail.com',
        success: () => {
          uni.showToast({
            title: '邮箱已复制',
            icon: 'success'
          })
        }
      })
    },
    callService() {
      uni.makePhoneCall({
        phoneNumber: '15278562765',
        fail: () => {
          uni.showToast({
            title: '无法拨打电话',
            icon: 'none'
          })
        }
      })
    },
    openOnlineService() {
      uni.navigateTo({
        url: '/chat/customer-service?isPlatform=true'
      })
    },
    submitReport() {
      if (!this.reportContent.trim()) {
        uni.showToast({
          title: '请输入投诉内容',
          icon: 'none'
        })
        return
      }
      const type = this.reportTypes[this.selectedType] || '其他'
      api.submitComplaint(type, this.reportContent.trim(), this.contactInfo ? this.contactInfo.trim() : '', this.imageList).then(res => {
        if (res.code === 200 || res.code === 0) {
          uni.showToast({
            title: '提交成功',
            icon: 'success',
            duration: 2000
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 2000)
        } else {
          uni.showToast({
            title: res.msg || '提交失败',
            icon: 'none'
          })
        }
      }).catch(err => {
        console.error('提交投诉失败:', err)
        uni.showToast({
          title: err.msg || err.message || '提交失败，请检查网络',
          icon: 'none'
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.report-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
  box-sizing: border-box;
}

.report-content {
  padding: 16rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 12rpx;
}

.section-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 24rpx;
}

.contact-methods {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.method-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.method-label {
  font-size: 24rpx;
  color: #666;
}

.method-value {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
}

.report-form {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
}

.form-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.form-item {
  margin-bottom: 20rpx;
}

.form-label {
  font-size: 24rpx;
  color: #333;
  margin-bottom: 10rpx;
  display: block;
}

.picker-view {
  padding: 14rpx 16rpx;
  background: #f8f8f8;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #333;
}

.textarea-input {
  width: 100%;
  min-height: 160rpx;
  padding: 14rpx 16rpx;
  background: #f8f8f8;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #333;
  box-sizing: border-box;
}

.char-count {
  text-align: right;
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
}

.image-upload-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}
.image-preview {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  overflow: hidden;
  background: #f8f8f8;
}
.preview-img {
  width: 100%;
  height: 100%;
}
.remove-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  text-align: center;
  background: rgba(0,0,0,0.5);
  color: #fff;
  font-size: 28rpx;
}
.image-add {
  width: 160rpx;
  height: 160rpx;
  border: 2rpx dashed #ddd;
  border-radius: 8rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}
.add-text {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}
.add-hint {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.input-field {
  width: 100%;
  min-height: 80rpx;
  height: 80rpx;
  padding: 0 16rpx;
  line-height: 80rpx;
  background: #f8f8f8;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #333;
  box-sizing: border-box;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx;
  padding-bottom: calc(16rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background: #f5f5f5;
  box-sizing: border-box;
}

.submit-btn {
  width: 100%;
  height: 72rpx;
  background: #F5D547;
  color: #333;
  border: none;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-btn:active {
  background: #e6c63d;
}
</style>
