<template>
  <view class="payment-page">
    <view class="navbar">
      <view class="nav-left" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">订单支付</view>
      <view class="nav-right"></view>
    </view>

    <view class="order-info">
      <view class="order-header">
        <text class="order-title">订单付款</text>
        <text class="order-id">订单号：{{ orderId }}</text>
      </view>
      <view class="amount-info">
        <text class="amount-label">支付金额</text>
        <text class="amount-value">¥{{ paymentAmount }}</text>
      </view>
    </view>

    <view class="payment-methods">
      <view class="section-title">支付方式</view>
      <view class="method-list">
        <view
          class="method-item"
          v-for="method in paymentMethods"
          :key="method.id"
          @click="selectPaymentMethod(method)"
        >
          <view class="method-info">
            <image
              v-if="method.icon && (method.icon.indexOf('/') >= 0 || method.icon.indexOf('.png') >= 0)"
              class="method-icon-img"
              :src="method.icon"
              mode="aspectFit"
            />
            <text v-else class="method-icon">{{ method.icon }}</text>
            <view class="method-details">
              <text class="method-name">{{ method.name }}</text>
              <text class="method-desc">{{ method.desc }}</text>
            </view>
          </view>
          <view class="method-radio" :class="{ active: selectedMethod.id === method.id }">
            <text class="radio-icon">✓</text>
          </view>
        </view>
      </view>
    </view>

    <view class="payment-actions">
      <view class="pay-btn" @click="processPayment" :class="{ disabled: !selectedMethod.id }">
        <text class="btn-text">立即支付 ¥{{ paymentAmount }}</text>
      </view>
    </view>

    <view class="result-modal" v-if="showResult" @click="closeResult">
      <view class="result-content" @click.stop>
        <view class="result-icon" :class="{ success: paymentSuccess, error: !paymentSuccess }">
          <text class="icon-text">{{ paymentSuccess ? '✓' : '✗' }}</text>
        </view>
        <text class="result-title">{{ paymentSuccess ? '支付成功' : '支付失败' }}</text>
        <text class="result-desc">{{ paymentSuccess ? '支付完成，即将跳转...' : '支付失败，请重试' }}</text>
        <view class="result-actions">
          <view class="action-btn" @click="closeResult">
            <text class="btn-text">{{ paymentSuccess ? '完成' : '关闭' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  data() {
    return {
      orderId: '',
      paymentAmount: '0.00',
      selectedMethod: {},
      showResult: false,
      paymentSuccess: false,
      paymentMethods: [
        {
          id: 1,
          name: '微信支付',
          icon: '💚',
          desc: '推荐使用',
          type: 'wechat'
        }
      ]
    }
  },

  onLoad(options) {
    this.orderId = options.orderId || ''
    this.paymentAmount = options.amount || '0.00'
  },

  methods: {
    goBack() {
      uni.navigateBack()
    },

    selectPaymentMethod(method) {
      this.selectedMethod = method
    },

    async processPayment() {
      if (!this.selectedMethod.id) {
        uni.showToast({
          title: '请选择支付方式',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({
          title: '支付处理中...'
        })

        await this.simulatePayment()

        uni.hideLoading()
        this.showPaymentResult(true)
      } catch (error) {
        uni.hideLoading()
        console.error('payment error:', error)
        this.showPaymentResult(false)
      }
    },

    async simulatePayment() {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve()
        }, 1500)
      })
    },

    showPaymentResult(success) {
      this.paymentSuccess = success
      this.showResult = true
    },

    closeResult() {
      this.showResult = false
      if (this.paymentSuccess) {
        this.updateOrderStatus()
        uni.redirectTo({
          url: '/order/list'
        })
      }
    },

    async updateOrderStatus() {
      try {
        try {
          await api.updateOrderStatus(this.orderId, 'paid')
        } catch (error) {
          /* ignore API failure */
        }

        const allOrders = uni.getStorageSync('orderList') || []
        const orderIndex = allOrders.findIndex(order => order.orderId === this.orderId)
        if (orderIndex !== -1) {
          allOrders[orderIndex].status = 'paid'
          allOrders[orderIndex].payTime = new Date().getTime()
          uni.setStorageSync('orderList', allOrders)
        }
      } catch (error) {
        console.error('updateOrderStatus', error)
      }
    }
  }
}
</script>

<style scoped>
.payment-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  background-color: #fff;
  padding: 0 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.nav-left {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 40rpx;
  color: #333;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.nav-right {
  width: 60rpx;
}

.order-info {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 10rpx;
  padding: 30rpx;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.order-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.order-id {
  font-size: 24rpx;
  color: #999;
}

.amount-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.amount-label {
  font-size: 26rpx;
  color: #666;
}

.amount-value {
  font-size: 32rpx;
  font-weight: 600;
  color: #ff4444;
}

.payment-methods {
  background-color: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: 10rpx;
  padding: 30rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.method-list {
  display: flex;
  flex-direction: column;
}

.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.method-item:last-child {
  border-bottom: none;
}

.method-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.method-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.method-icon-img {
  width: 40rpx;
  height: 40rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.method-details {
  display: flex;
  flex-direction: column;
}

.method-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 5rpx;
}

.method-desc {
  font-size: 24rpx;
  color: #999;
}

.method-radio {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.method-radio.active {
  border-color: #ff4444;
}

.radio-icon {
  font-size: 24rpx;
  color: #ff4444;
}

.payment-actions {
  padding: 0 20rpx;
}

.pay-btn {
  width: 100%;
  height: 80rpx;
  background-color: #ff4444;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}

.pay-btn.disabled {
  background-color: #ccc;
}

.btn-text {
  font-size: 30rpx;
  color: #fff;
  font-weight: 600;
}

.result-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.result-content {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 60rpx 40rpx 40rpx;
  text-align: center;
  margin: 0 60rpx;
}

.result-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 30rpx;
}

.result-icon.success {
  background-color: #4caf50;
}

.result-icon.error {
  background-color: #f44336;
}

.icon-text {
  font-size: 60rpx;
  color: #fff;
}

.result-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.result-desc {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 40rpx;
  line-height: 1.5;
}

.result-actions {
  display: flex;
  justify-content: center;
}

.action-btn {
  padding: 20rpx 40rpx;
  background-color: #ff4444;
  border-radius: 30rpx;
}

.action-btn .btn-text {
  font-size: 28rpx;
  color: #fff;
}
</style>
