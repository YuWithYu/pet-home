<template>
  <view class="refund-detail-page">
    <view class="status-card">
      <text class="status-title">{{ statusTitle }}</text>
      <text class="status-desc">{{ statusDesc }}</text>
    </view>

    <view class="section product-card">
      <image class="product-image" :src="getImageUrl(productInfo.image)" mode="aspectFill"></image>
      <view class="product-meta">
        <text class="product-name">{{ productInfo.name || '商品' }}</text>
        <text class="product-sub">x{{ productInfo.quantity || 1 }}</text>
      </view>
      <text class="product-amount">¥{{ orderInfo.finalAmount || orderInfo.totalAmount || '0.00' }}</text>
    </view>

    <view class="section info-card">
      <view class="row">
        <text class="label">订单号</text>
        <text class="value">{{ orderInfo.orderId || orderInfo.orderNo || orderInfo.id || '--' }}</text>
      </view>
      <view class="row">
        <text class="label">申请原因</text>
        <text class="value">{{ refundReason }}</text>
      </view>
      <view class="row">
        <text class="label">退款金额</text>
        <text class="value">¥{{ orderInfo.finalAmount || orderInfo.totalAmount || '0.00' }}</text>
      </view>
      <view class="row">
        <text class="label">退款时间</text>
        <text class="value">{{ refundTimeText }}</text>
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
      orderInfo: {},
      productInfo: {},
      refundReason: ''
    }
  },
  computed: {
    statusTitle() {
      const rs = String(this.orderInfo.refundStatus || '').toLowerCase()
      if (rs === 'approved') return '退款申请通过，订单已取消'
      if (rs === 'pending') return '退款申请处理中'
      if (rs === 'rejected') return '退款申请未通过'
      return '售后处理中'
    },
    statusDesc() {
      const rs = String(this.orderInfo.refundStatus || '').toLowerCase()
      if (rs === 'approved') return '商家已同意退款，款项将按原路返回。'
      if (rs === 'pending') return '平台/商家正在审核您的退款申请。'
      if (rs === 'rejected') return '退款申请未通过，请联系商家协商。'
      return '请关注后续售后进度。'
    },
    refundTimeText() {
      const t = this.orderInfo.refundTime ||
        this.orderInfo.refundSuccessTime ||
        this.orderInfo.refundCreateTime ||
        this.orderInfo.refundUpdateTime ||
        this.orderInfo.updateTime ||
        this.orderInfo.createTime
      if (!t) return '--'
      return String(t).replace('T', ' ')
    }
  },
  onLoad(options) {
    const orderData = uni.getStorageSync('refundOrderData')
    if (orderData) {
      this.orderInfo = orderData.orderInfo || {}
      this.productInfo = orderData.productInfo || {}
      this.refundReason = this.getRefundReason(this.orderInfo)
    }
    const orderId = (options && (options.orderId || options.orderNo)) || this.orderInfo.orderId || this.orderInfo.orderNo || this.orderInfo.id
    if (orderId) this.loadOrderInfo(orderId)
  },
  methods: {
    async loadOrderInfo(orderId) {
      try {
        const res = await api.getOrderDetail(orderId)
        if (res && (res.code === 200 || res.code === 0) && res.data) {
          const d = res.data
          this.orderInfo = {
            ...this.orderInfo,
            ...d,
            orderNo: d.orderNo || d.orderSn || this.orderInfo.orderNo,
            orderId: d.id || this.orderInfo.orderId,
            refundStatus: d.refundStatus || this.orderInfo.refundStatus,
            refundReason: d.refundReason || this.orderInfo.refundReason
          }
          if (Array.isArray(d.products) && d.products.length > 0) {
            this.productInfo = {
              id: d.products[0].id,
              name: d.products[0].name || d.products[0].productName,
              image: d.products[0].image || d.products[0].productImage,
              quantity: d.products[0].quantity || 1
            }
          }
          this.refundReason = this.getRefundReason(this.orderInfo)
        }
      } catch (e) {}
    },
    getRefundReason(order) {
      if (!order) return '--'
      const candidate = [
        order.refundReason,
        order.reason,
        order.refundApplyReason,
        order.afterSaleReason,
        order.refundDesc,
        order.refundRemark,
        order.applyReason,
        order.refund && order.refund.reason,
        order.refund && order.refund.refundReason,
        order.refundRequest && order.refundRequest.reason,
        order.refundRequest && order.refundRequest.refundReason
      ].find(v => v != null && String(v).trim() !== '')
      return candidate ? String(candidate) : '--'
    },
    getImageUrl(url) {
      if (!url) return '/static/images/default-product.svg'
      return util.getImageUrl ? util.getImageUrl(url) : url
    }
  }
}
</script>

<style scoped>
.refund-detail-page { min-height: 100vh; background: #f6f7fb; padding: 20rpx; }
.status-card,.section { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.status-title { font-size: 34rpx; font-weight: 600; color: #222; display: block; margin-bottom: 10rpx; }
.status-desc { font-size: 24rpx; color: #666; }
.product-card { display: flex; align-items: center; }
.product-image { width: 104rpx; height: 104rpx; border-radius: 10rpx; margin-right: 16rpx; }
.product-meta { flex: 1; display: flex; flex-direction: column; }
.product-name { font-size: 28rpx; color: #222; margin-bottom: 8rpx; }
.product-sub { font-size: 24rpx; color: #999; }
.product-amount { font-size: 30rpx; color: #222; }
.row { display: flex; justify-content: space-between; align-items: center; padding: 14rpx 0; border-bottom: 1rpx solid #f3f3f3; }
.row:last-child { border-bottom: 0; }
.label { font-size: 26rpx; color: #888; }
.value { font-size: 26rpx; color: #333; max-width: 65%; text-align: right; word-break: break-all; }
</style>
