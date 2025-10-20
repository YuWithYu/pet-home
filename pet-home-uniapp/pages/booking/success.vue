<template>
  <view class="booking-success-container">
    <!-- 顶部成功状态区域 -->
    <view class="success-header">
      <view class="success-icon">
        <text class="icon">✓</text>
      </view>
      <view class="success-title">预约成功</view>
      <view class="success-subtitle">感谢您的预约</view>
    </view>

    <!-- 订单详情卡片 -->
    <view class="order-details-card">
      <view class="detail-item">
        <text class="label">订单编号:</text>
        <text class="value">{{ orderInfo.orderNumber || '1420033203048590' }}</text>
      </view>
      <view class="detail-item">
        <text class="label">下单时间:</text>
        <text class="value">{{ orderInfo.orderTime || '2023-04-13 10:41:01' }}</text>
      </view>
      <view class="detail-item">
        <text class="label">订单状态:</text>
        <text class="value status-success">预约成功</text>
      </view>
      <view class="detail-item">
        <text class="label">预约地点:</text>
        <text class="value">{{ orderInfo.location || '广州南方学院店' }}</text>
      </view>
    </view>

    <!-- 使用说明 -->
    <view class="instructions">
      <text class="instruction-text">请在预定时间内凭核销码前往，若有事无法前往，请提前取消订单。</text>
    </view>

    <!-- 二维码区域 -->
    <view class="qr-code-section">
      <view class="qr-code-container">
        <view class="qr-code-square">
          <image 
            :src="qrCodeUrl" 
            mode="aspectFill" 
            class="qr-image"
            @error="onQrImageError"
          />
        </view>
        <view class="qr-code-text">核销码</view>
        <view class="qr-code-number">{{ orderInfo.verifyCode }}</view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <view class="primary-button" @tap="viewOrder">
        <text class="button-text">查看订单</text>
      </view>
      <view class="secondary-button" @tap="backToHome">
        <text class="button-text">返回首页</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'BookingSuccess',
  
  data() {
    return {
      orderInfo: {
        orderNumber: '',
        verifyCode: '',
        orderTime: '',
        location: '',
        serviceType: '',
        petName: '',
        appointmentDate: '',
        appointmentTime: ''
      },
      qrCodeData: '',
      qrCodeUrl: ''
    }
  },

  onLoad(options) {
    // 从页面参数获取订单信息
    if (options.orderNumber) {
      this.orderInfo.orderNumber = decodeURIComponent(options.orderNumber)
    }
    if (options.verifyCode) {
      this.orderInfo.verifyCode = decodeURIComponent(options.verifyCode)
    }
    if (options.orderTime) {
      this.orderInfo.orderTime = decodeURIComponent(options.orderTime)
    }
    if (options.location) {
      this.orderInfo.location = decodeURIComponent(options.location)
    }
    if (options.serviceType) {
      this.orderInfo.serviceType = decodeURIComponent(options.serviceType)
    }
    if (options.petName) {
      this.orderInfo.petName = decodeURIComponent(options.petName)
    }
    if (options.appointmentDate) {
      this.orderInfo.appointmentDate = decodeURIComponent(options.appointmentDate)
    }
    if (options.appointmentTime) {
      this.orderInfo.appointmentTime = decodeURIComponent(options.appointmentTime)
    }

    // 生成二维码
    this.generateQRCode()
  },

  methods: {
    // 生成二维码
    generateQRCode() {
      try {
        // 构建二维码数据 - 包含核销码的JSON格式
        const qrData = {
          type: 'verify',
          orderId: this.orderInfo.orderNumber,
          verifyCode: this.orderInfo.verifyCode,
          serviceType: this.orderInfo.serviceType,
          appointmentDate: this.orderInfo.appointmentDate,
          appointmentTime: this.orderInfo.appointmentTime,
          location: this.orderInfo.location
        }
        
        // 将数据转换为JSON字符串
        this.qrCodeData = JSON.stringify(qrData)
        
        // 生成二维码URL
        this.generateQRCodeUrl()
        
        console.log('生成核销二维码数据:', qrData)
      } catch (error) {
        console.error('生成二维码失败:', error)
      }
    },

    // 生成二维码URL
    generateQRCodeUrl() {
      try {
        // 使用在线二维码API生成真正的二维码
        const encodedData = encodeURIComponent(this.qrCodeData)
        // 使用qrcode.js.org的API生成二维码
        this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodedData}`
        
        console.log('二维码URL:', this.qrCodeUrl)
      } catch (error) {
        console.error('生成二维码URL失败:', error)
        // 如果API失败，使用简化的本地生成
        this.generateLocalQRCode()
      }
    },

    // 生成本地二维码（备用方案）
    generateLocalQRCode() {
      // 创建一个简单的二维码占位符
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      canvas.width = 200
      canvas.height = 200
      
      // 绘制白色背景
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, 200, 200)
      
      // 绘制黑色边框
      ctx.fillStyle = '#000000'
      ctx.fillRect(0, 0, 200, 200)
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(2, 2, 196, 196)
      
      // 绘制简单的二维码图案
      ctx.fillStyle = '#000000'
      for (let i = 0; i < 25; i++) {
        for (let j = 0; j < 25; j++) {
          if ((i + j) % 3 === 0 || (i * j) % 5 === 0) {
            ctx.fillRect(i * 8, j * 8, 8, 8)
          }
        }
      }
      
      this.qrCodeUrl = canvas.toDataURL()
    },

    // 二维码图片加载失败
    onQrImageError(e) {
      console.error('二维码图片加载失败:', e)
      // 使用备用方案
      this.generateLocalQRCode()
    },






    // 查看订单
    viewOrder() {
      uni.navigateTo({
        url: '/pages/user/my-appointments'
      })
    },

    // 返回首页
    backToHome() {
      uni.reLaunch({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.booking-success-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 40rpx;
}

// 顶部成功状态区域
.success-header {
  background: linear-gradient(135deg, #ff6b35, #ff8c42);
  padding: 80rpx 40rpx 60rpx;
  text-align: center;
  position: relative;
}

.success-icon {
  width: 120rpx;
  height: 120rpx;
  background-color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 30rpx;
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
}

.success-icon .icon {
  font-size: 60rpx;
  color: #ff6b35;
  font-weight: bold;
}

.success-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 10rpx;
}

.success-subtitle {
  font-size: 28rpx;
  color: #ffffff;
  opacity: 0.9;
}

// 订单详情卡片
.order-details-card {
  background-color: #ffffff;
  margin: -30rpx 30rpx 40rpx;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.1);
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item .label {
  font-size: 28rpx;
  color: #666666;
  font-weight: 500;
}

.detail-item .value {
  font-size: 28rpx;
  color: #333333;
  font-weight: 600;
  text-align: right;
  flex: 1;
  margin-left: 20rpx;
}

.detail-item .value.status-success {
  color: #ff6b35;
  font-weight: bold;
}

// 使用说明
.instructions {
  margin: 0 30rpx 40rpx;
  padding: 30rpx;
  background-color: #fff5f0;
  border-radius: 15rpx;
  border-left: 6rpx solid #ff6b35;
}

.instruction-text {
  font-size: 26rpx;
  color: #ff6b35;
  line-height: 1.6;
}

// 二维码区域
.qr-code-section {
  display: flex;
  justify-content: center;
  margin: 40rpx 0;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.qr-code-square {
  width: 300rpx;
  height: 300rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.qr-image {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
}

.qr-code-text {
  font-size: 24rpx;
  color: #666666;
  margin-top: 20rpx;
  font-weight: 500;
  text-align: center;
}

.qr-code-number {
  font-size: 20rpx;
  color: #999999;
  margin-top: 5rpx;
  text-align: center;
}

// 操作按钮
.action-buttons {
  padding: 0 30rpx;
  margin-top: 40rpx;
}

.primary-button {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8c42);
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 20rpx rgba(255, 107, 53, 0.3);
}

.primary-button .button-text {
  font-size: 32rpx;
  color: #ffffff;
  font-weight: bold;
}

.secondary-button {
  width: 100%;
  height: 88rpx;
  background-color: #ffffff;
  border: 2rpx solid #e0e0e0;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.secondary-button .button-text {
  font-size: 32rpx;
  color: #666666;
  font-weight: 500;
}

// 按钮点击效果
.primary-button:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.secondary-button:active {
  transform: scale(0.98);
  background-color: #f8f8f8;
}
</style>
