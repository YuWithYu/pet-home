<template>
  <view class="order-list">
    <!-- 订单状态筛选 -->
    <view class="status-tabs">
      <view 
        class="tab-item" 
        v-for="status in orderStatuses" 
        :key="status.value"
        :class="{ active: currentStatus === status.value }"
        @click="switchStatus(status.value)"
      >
        <text class="tab-text">{{ status.label }}</text>
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="order-content">
      <view v-if="Array.isArray(orderList) && orderList.length > 0" class="order-items">
        <view 
          class="order-item" 
          v-for="(order, orderIdx) in orderList" 
          :key="orderIdx"
          @click="viewOrderDetail(order)"
        >
          <view class="order-header">
            <view class="shop-entry" @click.stop="goStore(order)">
              <image v-if="order.storeAvatar" class="shop-avatar" :src="getImageUrl(order.storeAvatar)"></image>
              <text class="shop-name">{{ order.storeName || '店铺' }}</text>
              <text class="shop-arrow">></text>
            </view>
            <text class="order-status" :class="order.statusClass || 'status-pending'">
              {{ order.statusText || '待支付' }}
            </text>
          </view>
          
          <view class="order-goods">
            <view class="goods-item">
              <image class="goods-image" :src="getImageUrl(order.goods && order.goods.image)" mode="aspectFill"></image>
              <view class="goods-info">
                <text class="goods-name">{{ (order.goods && order.goods.name) || '商品' }}</text>
                <view class="goods-spec-row">
                  <text class="goods-quantity">x{{ order.quantity || 1 }}</text>
                  <text class="goods-spec" v-if="order.goodsSpec">{{ order.goodsSpec }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="order-amount-row">
            <text class="order-amount-text">实付 ¥{{ order.finalAmount || order.totalAmount || '0.00' }}</text>
          </view>
          
          <view class="order-footer">
            <view class="order-info">
              <!-- 该位置不展示下单日期，保持与参考样式一致 -->
            </view>
            <view class="order-actions">
              <view 
                class="action-btn"
                v-for="action in order.orderActions"
                :key="action.type"
                :class="action.btnClass"
                @click.stop="handleOrderAction(order, action.type)"
              >
                <text class="btn-text">{{ action.text }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <view v-else class="empty-state">
        <view class="empty-icon">📦</view>
        <text class="empty-text">暂无订单</text>
        <view class="go-shopping-btn" @click="goShopping">
          <text class="btn-text">去购物</text>
        </view>
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
      currentStatus: 'all',
      orderList: [],
      listLoadError: '',
      orderStatuses: [
        { label: '全部', value: 'all' },
        { label: '待支付', value: 'pending' },
        { label: '待发货', value: 'paid' },
        { label: '待收货', value: 'shipped' },
        { label: '已完成', value: 'completed' }
      ]
    }
  },

  onLoad() {
    this.loadOrderList()
  },

  onShow() {
    this.loadOrderList(true)
  },

  onPullDownRefresh() {
    this.loadOrderList(true).finally(() => uni.stopPullDownRefresh())
  },

  methods: {
    async loadOrderList(silent = false) {
      this.listLoadError = ''

      try {
        const status = this.currentStatus === 'all' ? null : this.currentStatus
        const userId = this.getCurrentUserId()

        const response = await api.getOrderList(status, userId, !silent, { timeout: 30000 })

        let ordersData = []
        const data = response && response.data
        if (data) {
          if (Array.isArray(data.orders)) ordersData = data.orders
          else if (Array.isArray(data.records)) ordersData = data.records
          else if (Array.isArray(data)) ordersData = data
          else if (Array.isArray(data.data)) ordersData = data.data
          else if (Array.isArray(data.list)) ordersData = data.list
        }

        if (ordersData.length === 0) {
          this.orderList = []
          return
        }

        // 为店铺信息做一层兜底：按商品ID补查商品详情，拿到 storeId/storeName/storeAvatar
        const productIds = [...new Set(
          ordersData
            .map(o => (o.products && o.products[0] && (o.products[0].id || o.products[0].productId)) || null)
            .filter(Boolean)
        )]
        const productStoreMap = new Map()
        if (productIds.length > 0) {
          const detailList = await Promise.all(
            productIds.map(pid =>
              api.getProductDetail(pid).catch(() => null)
            )
          )
          detailList.forEach((res, idx) => {
            if (!res || !(res.code === 200 || res.code === 0) || !res.data) return
            const p = res.data
            const storeInfo = p.storeInfo || p.store || {}
            productStoreMap.set(productIds[idx], {
              storeId: storeInfo.id || storeInfo.storeId || p.storeId || p.store_id || null,
              storeName: storeInfo.name || storeInfo.storeName || p.storeName || p.shopName || '',
              storeAvatar: p.storeAvatar || p.storeLogo || storeInfo.avatar || storeInfo.storeAvatar || ''
            })
          })
        }

        // 极简映射
        const mappedList = ordersData.map((order) => {
          const product = (order.products && order.products[0]) || order
          const productId = product.id || product.productId || null
          const storeFromProduct = productId ? productStoreMap.get(productId) : null
          const mappedOrder = {
            id: order.id,
            orderId: order.orderNo || order.id,
            goods: {
              id: product.id || product.productId || order.id,
              name: product.name || product.productName || '商品',
              image: product.image || product.productImage || ''
            },
            goodsSpec: product.specName || product.spec || product.skuName || '',
            storeId: order.storeId || product.storeId || (storeFromProduct && storeFromProduct.storeId) || null,
            storeName: order.storeName || product.storeName || product.shopName || product.storeTitle || (storeFromProduct && storeFromProduct.storeName) || '',
            storeAvatar: order.storeAvatar || product.storeAvatar || product.shopAvatar || (storeFromProduct && storeFromProduct.storeAvatar) || '',
            finalAmount: order.totalAmount || 0,
            quantity: product.quantity || 1,
            status: order.status || 'pending',
            createTime: order.createTime || Date.now(),
            hasRefundRequest: !!order.hasRefundRequest,
            refundStatus: order.refundStatus || null,
            orderType: order.orderType || order.type || '',
            statusText: this.getStatusText(order.status || 'pending', {
              hasRefundRequest: !!order.hasRefundRequest,
              refundStatus: order.refundStatus || null
            }),
            statusClass: this.getStatusClass(order.status || 'pending', {
              hasRefundRequest: !!order.hasRefundRequest,
              refundStatus: order.refundStatus || null
            })
          }
          return {
            ...mappedOrder,
            orderActions: this.getOrderActions(mappedOrder.status, mappedOrder)
          }
        })

        this.orderList = mappedList

      } catch (error) {
        console.error('订单加载异常:', error)
        this.orderList = []
        this.listLoadError = '加载失败，请检查网络'
      }
    },

    getCurrentUserId() {
      return uni.getStorageSync('userId') || 
             uni.getStorageSync('uid') || 
             uni.getStorageSync('user_id') || null
    },

    getStatusText(status, order = null) {
      if (status === 'completed' && order && order.hasRefundRequest) {
        const rs = String(order.refundStatus || '').toLowerCase()
        if (rs === 'approved') return '退款成功'
        if (rs === 'pending') return '退款申请中'
        if (rs === 'rejected') return '退款失败'
      }
      const map = {
        pending: '待支付',
        paid: '待发货',
        shipped: '待收货',
        completed: '已完成',
        cancelled: '已取消'
      }
      return map[status] || status
    },
    getStatusClass(status, order = null) {
      if (status === 'completed' && order && order.hasRefundRequest) {
        const rs = String(order.refundStatus || '').toLowerCase()
        if (rs === 'approved') return 'order-status status-cancelled'
        if (rs === 'pending') return 'order-status status-shipped'
        if (rs === 'rejected') return 'order-status status-pending'
      }
      return 'order-status status-' + status
    },
    getOrderActions(status, order = null) {
      const isPoints = this.isPointsOrder(order)
      const actionMap = {
        pending: [
          { type: 'cancel', text: '取消订单', btnClass: 'btn-cancel' },
          { type: 'pay', text: '立即付款', btnClass: 'btn-primary' }
        ],
        paid: [
          { type: 'contact', text: '联系客服', btnClass: 'btn-secondary' }
        ],
        shipped: [
          { type: 'track', text: '查看物流', btnClass: 'btn-secondary' },
          { type: 'confirm', text: '确认收货', btnClass: 'btn-primary' }
        ],
        completed: (order && order.hasRefundRequest && String(order.refundStatus || '').toLowerCase() === 'approved')
          ? [
              { type: 'delete-order', text: '删除订单', btnClass: 'btn-cancel' },
              { type: 'after-sale-detail', text: '售后详情', btnClass: 'btn-secondary' }
            ]
          : [
              { type: 'more', text: '更多', btnClass: 'btn-secondary' },
              { type: 'refund', text: '申请退款', btnClass: 'btn-secondary' },
              ...(isPoints ? [] : [{ type: 'review', text: this.getReviewActionText(order), btnClass: 'btn-secondary' }]),
              { type: 'buy-again', text: isPoints ? '再次兑换' : '再次购买', btnClass: 'btn-primary' }
            ],
        cancelled: [
          { type: 'buy-again', text: isPoints ? '再次兑换' : '再次购买', btnClass: 'btn-primary' }
        ]
      }
      return actionMap[status] || []
    },
    getReviewStorageKey(order) {
      if (!order) return ''
      const uid = this.getCurrentUserId()
      const orderId = order.id || order.orderId
      const productId = order.goods && (order.goods.id || order.goods.productId)
      if (!uid || !orderId || !productId) return ''
      return `order_reviewed_${uid}_${orderId}_${productId}`
    },
    isOrderReviewed(order) {
      const key = this.getReviewStorageKey(order)
      if (!key) return false
      return uni.getStorageSync(key) === '1'
    },
    getReviewActionText(order) {
      return this.isOrderReviewed(order) ? '追加评价' : '评价'
    },
    isPointsOrder(order) {
      if (!order) return false
      const orderId = String(order.orderId || order.id || '').toUpperCase()
      const explicitType = String(order.orderType || '').toLowerCase()
      const goodsSpec = String(order.goodsSpec || '')
      if (explicitType === 'exchange' || explicitType === 'points') return true
      if (orderId.startsWith('EX')) return true
      if (goodsSpec.includes('积分兑换')) return true
      return false
    },
    handleOrderAction(order, actionType) {
      switch (actionType) {
        case 'track':
          this.trackOrder(order)
          break
        case 'confirm':
          this.confirmOrder(order)
          break
        case 'pay':
          this.payOrder(order)
          break
        case 'cancel':
          this.cancelOrder(order)
          break
        case 'contact':
          this.contactService()
          break
        case 'more':
          this.showMoreActionSheet(order)
          break
        case 'refund':
          this.requestRefund(order)
          break
        case 'review':
          this.reviewOrder(order)
          break
        case 'buy-again':
          this.buyAgain(order)
          break
        case 'delete-order':
          this.deleteOrder(order)
          break
        case 'after-sale-detail':
          this.showAfterSaleDetail(order)
          break
        case 'view':
          this.viewOrderDetail(order)
          break
        default:
          this.viewOrderDetail(order)
      }
    },
    showMoreActionSheet(order) {
      uni.showActionSheet({
        itemList: ['查看物流', '删除订单'],
        success: (res) => {
          if (res.tapIndex === 0) this.trackOrder(order)
          if (res.tapIndex === 1) this.deleteOrder(order)
        }
      })
    },
    async deleteOrder(order) {
      uni.showModal({
        title: '确认删除',
        content: '删除后该订单不再显示，是否继续？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            const orderId = order.orderId || order.id
            await api.deleteOrder(orderId)
            this.orderList = this.orderList.filter(o => String(o.id || o.orderId) !== String(order.id || order.orderId))
            uni.showToast({ title: '删除成功', icon: 'success' })
          } catch (e) {
            uni.showToast({ title: '删除失败', icon: 'none' })
          }
        }
      })
    },
    showAfterSaleDetail(order) {
      const orderId = order.orderId || order.id
      uni.setStorageSync('refundOrderData', {
        orderInfo: order,
        productInfo: {
          id: order.goods && order.goods.id,
          name: order.goods && order.goods.name,
          image: order.goods && order.goods.image
        }
      })
      uni.navigateTo({
        url: `/order/refund-detail?orderId=${orderId}&orderNo=${order.orderId || orderId}`
      })
    },
    reviewOrder(order) {
      const orderId = order.id || order.orderId
      const productId = order.goods && (order.goods.id || order.goods.productId) || ''
      const productImage = order.goods && order.goods.image ? order.goods.image : ''
      const productName = order.goods && order.goods.name ? order.goods.name : ''
      const append = this.isOrderReviewed(order) ? 1 : 0
      uni.navigateTo({
        url: `/order/review?orderId=${orderId}&productId=${productId}&append=${append}&productImage=${encodeURIComponent(productImage)}&productName=${encodeURIComponent(productName)}`
      })
    },
    requestRefund(order) {
      const orderId = order.orderId || order.id
      uni.setStorageSync('refundOrderData', {
        orderInfo: order,
        productInfo: {
          id: order.goods && order.goods.id,
          name: order.goods && order.goods.name,
          image: order.goods && order.goods.image
        }
      })
      uni.navigateTo({
        url: `/order/refund?orderId=${orderId}&orderNo=${order.orderId || orderId}`
      })
    },
    buyAgain(order) {
      const productId = order.goods && (order.goods.id || order.goods.productId)
      if (productId) {
        if (this.isPointsOrder(order)) {
          uni.navigateTo({ url: `/points-mall/detail?id=${productId}` })
        } else {
          uni.navigateTo({ url: `/pages-goods/detail?id=${productId}` })
        }
      } else {
        uni.showToast({ title: '无法获取商品信息', icon: 'none' })
      }
    },
    trackOrder(order) {
      const orderId = order.orderId || order.id
      uni.navigateTo({
        url: `/order/logistics?orderId=${orderId}`
      })
    },
    confirmOrder(order) {
      uni.showModal({
        title: '确认收货',
        content: '确定已收到商品吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            const orderId = order.orderId || order.id
            await api.updateOrderStatus(orderId, 'completed')
            uni.showToast({ title: '确认收货成功', icon: 'success' })
            this.loadOrderList(true)
          } catch (e) {
            uni.showToast({ title: '确认收货失败', icon: 'none' })
          }
        }
      })
    },
    payOrder(order) {
      const orderId = order.orderId || order.id
      uni.navigateTo({
        url: `/order/confirm?orderId=${encodeURIComponent(orderId)}`
      })
    },
    cancelOrder(order) {
      uni.showModal({
        title: '取消订单',
        content: '确定取消这个订单吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            const orderId = order.orderId || order.id
            await api.updateOrderStatus(orderId, 'cancelled')
            uni.showToast({ title: '订单已取消', icon: 'success' })
            this.loadOrderList(true)
          } catch (e) {
            uni.showToast({ title: '取消失败', icon: 'none' })
          }
        }
      })
    },
    contactService() {
      uni.navigateTo({
        url: '/chat/customer-service?isPlatform=true'
      })
    },

    getImageUrl(url) {
      if (!url) return '/static/images/default-product.svg'
      if (url.startsWith('http')) return url
      return util.getImageUrl ? util.getImageUrl(url) : url
    },
    goStore(order) {
      if (order && order.storeId) {
        const storeName = encodeURIComponent(order.storeName || '')
        const storeAvatar = encodeURIComponent(order.storeAvatar || '')
        uni.navigateTo({
          url: `/pages-goods/list?storeId=${order.storeId}&storeName=${storeName}&storeAvatar=${storeAvatar}`
        })
      } else {
        // 无店铺ID时不提示，避免打扰
      }
    },

    viewOrderDetail(order) {
      uni.navigateTo({
        url: `/order/detail?orderId=${order.orderId || order.id}`
      })
    },

    formatTime(ts) {
      if (!ts) return ''
      const d = new Date(ts)
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    },

    goShopping() {
      uni.switchTab({ url: '/pages/index/index' })
    },

    switchStatus(status) {
      this.currentStatus = status
      this.loadOrderList()
    }
  }
}
</script>

<style scoped>
.order-list { min-height: 100vh; background: #f5f5f5; }
.status-tabs { display: flex; background: #fff; border-bottom: 1rpx solid #eee; }
.tab-item { flex: 1; text-align: center; padding: 14rpx 0; position: relative; font-size: 24rpx; color: #666; }
.tab-item.active { color: #ff4444; }
.tab-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 30rpx; height: 3rpx; background: #ff4444; border-radius: 2rpx; }

.order-content { padding: 18rpx; }
.order-item { background: #fff; margin-bottom: 18rpx; border-radius: 12rpx; overflow: hidden; box-shadow: 0 3rpx 12rpx rgba(0,0,0,0.06); }
.order-header { display: flex; justify-content: space-between; align-items: center; padding: 18rpx 22rpx; border-bottom: 1rpx solid #f0f0f0; }
.shop-entry { display: flex; align-items: center; max-width: 72%; }
.shop-avatar { width: 40rpx; height: 40rpx; border-radius: 6rpx; margin-right: 12rpx; background: #f3f3f3; }
.shop-name { font-size: 28rpx; color: #333; max-width: 320rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.shop-arrow { margin-left: 8rpx; color: #999; font-size: 20rpx; }
.order-status { padding: 6rpx 16rpx; border-radius: 28rpx; font-size: 24rpx; }

.status-pending { background: #fff3cd; color: #d39e00; }
.status-paid { background: #d4edda; color: #155724; }
.status-shipped { background: #cce5ff; color: #004085; }
.status-completed { background: #d4edda; color: #155724; }

.order-goods { padding: 0 22rpx 18rpx; }
.goods-item { display: flex; }
.goods-image { width: 108rpx; height: 108rpx; border-radius: 8rpx; margin-right: 16rpx; }
.goods-info { flex: 1; display: flex; flex-direction: column; justify-content: center; }
.goods-name { font-size: 28rpx; font-weight: 400; color: #333; margin-bottom: 8rpx; line-height: 1.35; }
.goods-spec-row { display: flex; align-items: center; gap: 8rpx; }
.goods-spec { font-size: 24rpx; color: #999; }
.goods-quantity { font-size: 24rpx; color: #999; }
.order-amount-row { padding: 0 22rpx 12rpx; display: flex; justify-content: flex-end; }
.order-amount-text { font-size: 24rpx; color: #333; }

.order-footer { padding: 0 22rpx 22rpx; display: flex; justify-content: space-between; align-items: center; font-size: 26rpx; color: #666; }
.order-actions { display: flex; align-items: center; justify-content: flex-end; gap: 14rpx; flex-wrap: nowrap; white-space: nowrap; }

.action-btn { display: inline-flex; align-items: center; justify-content: center; padding: 12rpx 22rpx; border: 1rpx solid #ddd; border-radius: 36rpx; font-size: 24rpx; line-height: 1; white-space: nowrap; }
.btn-primary { background: #ff4444; border-color: #ff4444; color: #fff; }
.btn-secondary { background: #fff; border-color: #ddd; color: #666; }
.btn-cancel { background: #fff; border-color: #999; color: #999; }

.empty-state { padding: 110rpx 0; text-align: center; }
.empty-icon { font-size: 110rpx; margin-bottom: 18rpx; }
.empty-text { font-size: 28rpx; color: #999; margin-bottom: 34rpx; }
.go-shopping-btn { background: #ff4444; color: white; padding: 18rpx 56rpx; border-radius: 48rpx; font-size: 30rpx; }
</style>
