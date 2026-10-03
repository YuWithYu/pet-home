<template>
  <view class="refund-container">
    <!-- 商品信息 -->
    <view class="product-section" v-if="productInfo && productInfo.name">
      <image 
        class="product-image" 
        :src="getImageUrl(productInfo.image)" 
        mode="aspectFill"
        @error="handleImageError"
      />
      <view class="product-info">
        <text class="product-name">{{ productInfo.name }}</text>
        <text class="product-spec">{{ productInfo.spec }}</text>
      </view>
    </view>

    <!-- 申请表单 -->
    <view class="form-section">
      <!-- 申请类型 -->
    <view class="form-item" @click="showTypePicker = true">
        <text class="form-label">申请类型</text>
        <view class="form-value">
          <text>{{ selectedType || '请选择申请类型' }}</text>
          <text class="arrow"></text>
        </view>
      </view>

      <!-- 申请原因 -->
    <view class="form-item" @click="showReasonPicker = true">
        <text class="form-label">申请原因</text>
        <view class="form-value">
          <text :class="{ 'placeholder': !selectedReason }">
            {{ selectedReason || '点击选择申请原因' }}
          </text>
          <text class="arrow"></text>
        </view>
      </view>

      <!-- 申请金额 -->
      <view class="form-item">
        <text class="form-label">申请金额</text>
        <view class="form-value">
          <text class="amount-text">¥{{ orderInfo.finalAmount || orderInfo.totalAmount || '0.00' }}</text>
        </view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section" v-if="!viewOnly">
      <button class="submit-btn" @click="submitRefund" :disabled="!canSubmit">
        <text class="submit-text">提交申请</text>
      </button>
    </view>
    <view class="submit-section" v-else>
      <view class="submit-btn submit-btn-disabled">
        <text class="submit-text">该订单已申请售后</text>
      </view>
    </view>

    <!-- 申请类型选择�?-->
    <view v-if="showTypePicker" class="picker-mask" @click="showTypePicker = false">
      <view class="picker-container" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择申请类型</text>
          <text class="picker-close" @click="showTypePicker = false">×</text>
        </view>
        <view class="picker-content">
          <view 
            class="picker-item" 
            v-for="type in refundTypes" 
            :key="type.value"
            @click="selectType(type)"
          >
            <text>{{ type.label }}</text>
            <text v-if="selectedType === type.label" class="check-icon"></text>
          </view>
        </view>
      </view>
    </view>

    <!-- 申请原因选择�?-->
    <view v-if="showReasonPicker" class="picker-mask" @click="showReasonPicker = false">
      <view class="picker-container" @click.stop>
        <view class="picker-header">
          <text class="picker-title">请选择申请原因</text>
          <text class="picker-close" @click="showReasonPicker = false">×</text>
        </view>
        <scroll-view class="picker-content" scroll-y>
          <view 
            class="picker-item" 
            v-for="reason in refundReasons" 
            :key="reason"
            @click="selectReason(reason)"
          >
            <text>{{ reason }}</text>
            <text v-if="selectedReason === reason" class="check-icon"></text>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script>
import { util } from '@/common/js/util.js'
import { api } from '@/common/js/api.js'

export default {
  name: 'RefundRequest',
  data() {
    return {
      orderInfo: {},
      productInfo: {},
      selectedType: '我要退货退款',
      selectedReason: '',
      viewOnly: false,
      showTypePicker: false,
      showReasonPicker: false,
      refundTypes: [
        { label: '我要退货退款', value: 'return_refund' },
        { label: '我要换货', value: 'exchange' }
      ],
      refundReasons: [
        '不想要了',
        '材质与商品描述不符',
        '大小尺寸与商品描述不符',
        '做工粗糙/有瑕疵',
        '颜色、款式、图案与描述不符',
        '货物与描述不符',
        '质量问题',
        '收到商品少件(含少配件)',
        '商品破损或污损',
        '商家发错',
        '假冒品牌',
        '空包',
        '其他原因'
      ]
    }
  },
  computed: {
    canSubmit() {
      return this.selectedType && this.selectedReason
    }
  },
  onLoad(options) {
    // 获取订单信息
    const orderId = options.orderId
    const orderNo = options.orderNo
    
    if (orderId || orderNo) {
      this.loadOrderInfo(orderId || orderNo)
    } else {
      // 从本地存储获取订单信息
      const orderData = uni.getStorageSync('refundOrderData')
      if (orderData) {
        this.orderInfo = orderData.orderInfo || {}
        this.productInfo = orderData.productInfo || {}
        this.viewOnly = !!(this.orderInfo && this.orderInfo.hasRefundRequest)
      }
    }
  },
  methods: {
    getImageUrl(url) {
      if (!url) {
        return '/static/images/default-product.svg'
      }
      return util.getImageUrl(url)
    },
    
    handleImageError(e) {
      console.error('图片加载失败:', e)
    },
    
    async loadOrderInfo(orderId) {
      try {
        uni.showLoading({ title: '加载中...', })
        const response = await api.getOrderDetail(orderId)
        uni.hideLoading()
        
        if (response && response.code === 200 && response.data) {
          this.orderInfo = response.data
          this.viewOnly = !!(this.orderInfo && this.orderInfo.hasRefundRequest)
          
          // 获取商品信息
          if (response.data.products && response.data.products.length > 0) {
            const product = response.data.products[0]
            this.productInfo = {
              id: product.id,
              name: product.name,
              image: product.image,
              spec: product.specName || '',
              quantity: product.quantity || 1
            }
          }
        }
      } catch (error) {
        uni.hideLoading()
        console.error('加载订单信息失败:', error)
        uni.showToast({
          title: '加载订单信息失败',
          icon: 'none'
        })
      }
    },
    
    selectType(type) {
      this.selectedType = type.label
      this.showTypePicker = false
    },
    
    selectReason(reason) {
      this.selectedReason = reason
      this.showReasonPicker = false
    },
    
    async submitRefund() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请选择申请原因',
          icon: 'none'
        })
        return
      }
      if (this.viewOnly) {
        uni.showToast({ title: '该订单已申请售后', icon: 'none' })
        return
      }
      
      try {
        uni.showLoading({ title: '提交中...', })
        
        const orderId = this.orderInfo.orderNo || this.orderInfo.id
        const response = await api.requestRefund(orderId, {
          type: this.selectedType,
          reason: this.selectedReason,
          amount: this.orderInfo.finalAmount || this.orderInfo.totalAmount || 0,
          productId: this.productInfo.id,
          productName: this.productInfo.name
        })
        
        uni.hideLoading()
        
        if (response && (response.code === 200 || response.code === 0)) {
          uni.showToast({
            title: '退款申请已提交',
            icon: 'success'
          })
          
          // 返回订单列表
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: response?.msg || '提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('提交退款申请失败', error)
        const msg = (error && (error.message || error.errMsg)) ? String(error.message || error.errMsg) : ''
        if (msg.includes('请勿重复申请') || msg.includes('已申请退款')) {
          uni.showToast({
            title: '该订单已申请退款，请到订单页查看售后状态',
            icon: 'none',
            duration: 2200
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1200)
          return
        }
        uni.showToast({
          title: '提交失败，请重试',
          icon: 'none'
        })
      }
    }
  },
}
</script>

<style scoped>
.refund-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.product-section {
  background-color: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: flex-start;
}

.product-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  margin-right: 20rpx;
  background-color: #f5f5f5;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
  line-height: 1.4;
}

.product-spec {
  font-size: 24rpx;
  color: #999;
}

.form-section {
  background-color: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.form-item:last-child {
  border-bottom: none;
}

.form-label {
  font-size: 28rpx;
  color: #333;
}

.form-value {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: flex-end;
  margin-left: 20rpx;
}

.form-value text {
  font-size: 28rpx;
  color: #333;
}

.form-value .placeholder {
  color: #999;
}

.arrow {
  margin-left: 10rpx;
  color: #999;
  font-size: 32rpx;
}

.amount-text {
  color: #ff4444;
  font-weight: 600;
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background-color: #fff;
  border-top: 1rpx solid #eee;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  background-color: #ff4444;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.submit-btn:disabled {
  background-color: #ccc;
}
.submit-btn-disabled {
  background-color: #ccc;
}

.submit-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
}

.picker-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.picker-container {
  background-color: #fff;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 70vh;
  width: 100%;
  display: flex;
  flex-direction: column;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.picker-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.picker-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.picker-content {
  flex: 1;
  max-height: 60vh;
  padding: 20rpx 0;
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.picker-item:last-child {
  border-bottom: none;
}

.picker-item text:first-child {
  font-size: 28rpx;
  color: #333;
}

.check-icon {
  color: #ff4444;
  font-size: 32rpx;
  font-weight: 600;
}
</style>
