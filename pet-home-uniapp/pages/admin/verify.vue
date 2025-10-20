<template>
  <view class="verify-container">
    <!-- 顶部标题 -->
    <view class="header">
      <view class="title">订单核销</view>
      <view class="subtitle">扫描客户二维码进行核销</view>
    </view>

    <!-- 扫描按钮 -->
    <view class="scan-section">
      <view class="scan-button" @tap="scanQRCode">
        <view class="scan-icon">📱</view>
        <view class="scan-text">扫描二维码</view>
      </view>
    </view>

    <!-- 手动输入核销码 -->
    <view class="manual-input-section">
      <view class="section-title">或手动输入核销码</view>
      <view class="input-container">
        <input 
          class="verify-input" 
          placeholder="请输入核销码" 
          v-model="manualCode"
          @confirm="verifyManualCode"
        />
        <view class="verify-button" @tap="verifyManualCode">验证</view>
      </view>
    </view>

    <!-- 核销结果 -->
    <view class="result-section" v-if="verifyResult">
      <view class="result-card" :class="verifyResult.success ? 'success' : 'error'">
        <view class="result-icon">{{ verifyResult.success ? '✓' : '✗' }}</view>
        <view class="result-title">{{ verifyResult.success ? '核销成功' : '核销失败' }}</view>
        <view class="result-message">{{ verifyResult.message }}</view>
        
        <view v-if="verifyResult.success && verifyResult.data" class="order-info">
          <view class="info-item">
            <text class="label">订单号：</text>
            <text class="value">{{ verifyResult.data.id }}</text>
          </view>
          <view class="info-item">
            <text class="label">服务类型：</text>
            <text class="value">{{ getServiceTypeName(verifyResult.data.serviceType) }}</text>
          </view>
          <view class="info-item">
            <text class="label">预约时间：</text>
            <text class="value">{{ formatDateTime(verifyResult.data.appointmentDate) }}</text>
          </view>
          <view class="info-item">
            <text class="label">核销时间：</text>
            <text class="value">{{ formatDateTime(verifyResult.data.verifyTime) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 历史记录 -->
    <view class="history-section" v-if="verifyHistory.length > 0">
      <view class="section-title">今日核销记录</view>
      <view class="history-list">
        <view 
          class="history-item" 
          v-for="(item, index) in verifyHistory" 
          :key="index"
        >
          <view class="history-info">
            <view class="order-id">订单：{{ item.id }}</view>
            <view class="verify-time">{{ formatDateTime(item.verifyTime) }}</view>
          </view>
          <view class="history-status success">已核销</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import api from '@/common/js/api.js'

export default {
  name: 'AdminVerify',
  
  data() {
    return {
      manualCode: '',
      verifyResult: null,
      verifyHistory: []
    }
  },

  onLoad() {
    this.loadVerifyHistory()
  },

  methods: {
    // 扫描二维码
    scanQRCode() {
      uni.scanCode({
        success: (res) => {
          console.log('扫描结果:', res)
          this.handleScanResult(res.result)
        },
        fail: (err) => {
          console.error('扫描失败:', err)
          uni.showToast({
            title: '扫描失败',
            icon: 'none'
          })
        }
      })
    },

    // 处理扫描结果
    handleScanResult(scanResult) {
      try {
        // 尝试解析JSON格式的二维码数据
        const qrData = JSON.parse(scanResult)
        
        if (qrData.type === 'verify' && qrData.verifyCode) {
          this.verifyCode(qrData.verifyCode)
        } else {
          uni.showToast({
            title: '无效的核销码',
            icon: 'none'
          })
        }
      } catch (error) {
        // 如果不是JSON格式，直接作为核销码处理
        this.verifyCode(scanResult)
      }
    },

    // 手动验证核销码
    verifyManualCode() {
      if (!this.manualCode.trim()) {
        uni.showToast({
          title: '请输入核销码',
          icon: 'none'
        })
        return
      }
      
      this.verifyCode(this.manualCode.trim())
    },

    // 验证核销码
    async verifyCode(verifyCode) {
      try {
        uni.showLoading({
          title: '验证中...',
          mask: true
        })

        const res = await api.verifyCode(verifyCode)
        
        this.verifyResult = {
          success: res.code === 0,
          message: res.msg,
          data: res.data
        }

        if (res.code === 0) {
          // 核销成功，添加到历史记录
          this.verifyHistory.unshift(res.data)
          // 清空手动输入的核销码
          this.manualCode = ''
          
          uni.showToast({
            title: '核销成功',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: res.msg || '核销失败',
            icon: 'none'
          })
        }
        
      } catch (error) {
        console.error('核销验证失败:', error)
        this.verifyResult = {
          success: false,
          message: '网络错误，请重试'
        }
        
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 获取服务类型名称
    getServiceTypeName(serviceType) {
      const typeMap = {
        'litter': '上门铲屎服务',
        'boarding': '寄养服务',
        'medical': '医疗服务',
        'grooming': '美容服务',
        'adoption': '领养服务'
      }
      return typeMap[serviceType] || serviceType
    },

    // 格式化时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    // 加载核销历史（这里可以调用后端接口）
    loadVerifyHistory() {
      // 暂时使用本地存储的模拟数据
      this.verifyHistory = uni.getStorageSync('verifyHistory') || []
    }
  }
}
</script>

<style lang="scss" scoped>
.verify-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 40rpx 30rpx;
}

// 顶部标题
.header {
  text-align: center;
  margin-bottom: 60rpx;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 20rpx;
}

.subtitle {
  font-size: 28rpx;
  color: #666666;
}

// 扫描区域
.scan-section {
  display: flex;
  justify-content: center;
  margin-bottom: 60rpx;
}

.scan-button {
  width: 300rpx;
  height: 300rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c42);
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(255, 107, 53, 0.3);
}

.scan-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.scan-text {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: bold;
}

// 手动输入区域
.manual-input-section {
  margin-bottom: 40rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 30rpx;
  text-align: center;
}

.input-container {
  display: flex;
  align-items: center;
  background-color: #ffffff;
  border-radius: 15rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.verify-input {
  flex: 1;
  font-size: 28rpx;
  padding: 10rpx;
  border: none;
  outline: none;
}

.verify-button {
  background-color: #ff6b35;
  color: #ffffff;
  padding: 20rpx 30rpx;
  border-radius: 10rpx;
  font-size: 28rpx;
  font-weight: bold;
}

// 核销结果
.result-section {
  margin-bottom: 40rpx;
}

.result-card {
  background-color: #ffffff;
  border-radius: 20rpx;
  padding: 40rpx;
  text-align: center;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.1);
}

.result-card.success {
  border-left: 8rpx solid #52c41a;
}

.result-card.error {
  border-left: 8rpx solid #ff4d4f;
}

.result-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.result-card.success .result-icon {
  color: #52c41a;
}

.result-card.error .result-icon {
  color: #ff4d4f;
}

.result-title {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.result-card.success .result-title {
  color: #52c41a;
}

.result-card.error .result-title {
  color: #ff4d4f;
}

.result-message {
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 30rpx;
}

// 订单信息
.order-info {
  background-color: #f8f9fa;
  border-radius: 15rpx;
  padding: 30rpx;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx 0;
  border-bottom: 1rpx solid #e8e8e8;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  font-size: 26rpx;
  color: #666666;
}

.info-item .value {
  font-size: 26rpx;
  color: #333333;
  font-weight: 500;
}

// 历史记录
.history-section {
  margin-bottom: 40rpx;
}

.history-list {
  background-color: #ffffff;
  border-radius: 15rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.history-item:last-child {
  border-bottom: none;
}

.history-info {
  flex: 1;
}

.order-id {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
  margin-bottom: 10rpx;
}

.verify-time {
  font-size: 24rpx;
  color: #999999;
}

.history-status {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-weight: bold;
}

.history-status.success {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1rpx solid #b7eb8f;
}
</style>
