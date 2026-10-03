<template>
  <view class="verify-container">
    <!-- 使用系统默认导航栏，标题为「订单核销-->
    <view class="page-header">
      <view class="subtitle">扫描客户二维码进行核销</view>
    </view>

    <!-- 扫描按钮 -->
    <view class="scan-section">
      <image class="scan-button" src="/static/images/核销二维码.png" mode="aspectFit" @tap="scanQRCode" />
    </view>

    <!-- 手动输入核销码-->
    <view class="manual-input-section">
      <view class="input-section-title">或手动输入核销码</view>
      <view class="input-container">
        <input 
          class="verify-input" 
          placeholder="请输入6位核销码" 
          v-model="manualCode"
          maxlength="6"
          type="number"
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
            <text class="value">{{ (verifyResult.data.appointment && verifyResult.data.appointment.id) || verifyResult.data.id }}</text>
          </view>
          <view class="info-item">
            <text class="label">服务类型</text>
            <text class="value">{{ getServiceTypeName(verifyResult.data.serviceType) }}</text>
          </view>
          <view class="info-item">
            <text class="label">预约时间</text>
            <text class="value">{{ formatDateTime((verifyResult.data.appointment && (verifyResult.data.appointment.appointmentDate || verifyResult.data.appointment.date)) || verifyResult.data.appointmentDate) }}</text>
          </view>
          <view class="info-item">
            <text class="label">核销时间</text>
            <text class="value">{{ formatDateTime((verifyResult.data.appointment && verifyResult.data.appointment.verifyTime) || verifyResult.data.verifyTime) }}</text>
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
import { api } from '@/common/js/api.js'

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
    const staffToken = uni.getStorageSync('staffToken')
    if (!staffToken) {
      uni.redirectTo({ url: '/pages-auth/login' })
      return
    }
    this.loadVerifyHistory()
  },

  onShow() {
    if (uni.getStorageSync('staffToken')) {
      this.loadVerifyHistory()
    }
  },

  methods: {
    // 扫描二维码
    scanQRCode() {
      uni.scanCode({
        success: (res) => {
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
        
        const verifyType = qrData.type || qrData.t
        const verifyCode = qrData.verifyCode || qrData.c
        
        if (verifyType === 'verify' && verifyCode) {
          this.verifyCode(verifyCode)
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
      const code = String(this.manualCode || '').replace(/\D/g, '').slice(0, 6)
      this.manualCode = code
      if (!code) {
        uni.showToast({
          title: '请输入核销码',
          icon: 'none'
        })
        return
      }
      if (code.length !== 6) {
        uni.showToast({
          title: '请输入6位核销码',
          icon: 'none'
        })
        return
      }
      
      this.verifyCode(code)
    },

    // 验证核销码
    async verifyCode(verifyCode) {
      try {
        uni.showLoading({
          title: '验证中...',
          mask: true
        })

        const res = await api.verifyCodeForStaff(verifyCode)
        
        const isSuccess = res.code === 200 || res.code === 0
        this.verifyResult = {
          success: isSuccess,
          message: res.msg || (isSuccess ? '核销成功' : '核销失败'),
          data: res.data
        }

        if (isSuccess) {
          const apt = res.data?.appointment || res.data
          const historyItem = {
            id: apt?.id || res.data?.id,
            serviceType: res.data?.serviceType,
            appointmentDate: apt?.appointmentDate || apt?.date,
            verifyTime: apt?.verifyTime || res.data?.verifyTime
          }
          this.verifyHistory.unshift(historyItem)
          const staffKey = this.getVerifyHistoryKey()
          const history = uni.getStorageSync(staffKey) || []
          history.unshift(historyItem)
          uni.setStorageSync(staffKey, history.slice(0, 50))
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
        const errMsg = (error && error.message) || '核销失败'
        this.verifyResult = {
          success: false,
          message: errMsg
        }
        
        uni.showToast({
          title: errMsg,
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },

    // 获取服务类型名称
    getServiceTypeName(serviceType) {
      const typeMap = {
        'door-cleaning': '上门铲屎',
        'grooming': '宠物洗护',
        'hospital': '宠物医院'
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

    // 获取当前账号专属的核销历史存储 key（按工作人员账号隔离）
    getVerifyHistoryKey() {
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      const adminId = staffInfo.adminId || staffInfo.id || 'default'
      return 'verifyHistory_' + adminId
    },

    // 加载核销历史（只加载当前账号自己的记录）
    loadVerifyHistory() {
      const staffKey = this.getVerifyHistoryKey()
      this.verifyHistory = uni.getStorageSync(staffKey) || []
    },

    staffLogout() {
      uni.removeStorageSync('staffToken')
      uni.removeStorageSync('staffInfo')
      uni.showToast({ title: '已退出', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({ url: '/pages-auth/login' })
      }, 500)
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
.page-header {
  padding: 20rpx 30rpx 30rpx;
  margin-bottom: 20rpx;
}
.page-header .subtitle {
  font-size: 26rpx;
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
}

// 手动输入区域
.manual-input-section {
  margin-bottom: 40rpx;
}

.input-section-title {
  font-size: 26rpx;
  color: #666666;
  margin-bottom: 20rpx;
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
