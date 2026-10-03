<template>
  <view class="my-coupons">
    <!-- 状态切换 -->
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-item"
        :class="{ active: currentStatus === tab.value }"
        @click="switchTab(tab.value)">
        <text class="tab-text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 券列表 -->
    <view v-if="loading" class="loading-box">
      <text class="loading-text">加载中...</text>
    </view>

    <view v-else-if="loadError" class="empty-box">
      <text class="empty-icon">⚠️</text>
      <text class="empty-text">加载失败</text>
      <text class="empty-text error-detail">{{ loadErrorMsg }}</text>
      <button class="empty-btn" @click="loadCoupons">重试</button>
    </view>

    <view v-else-if="coupons.length === 0" class="empty-box">
      <text class="empty-icon">🎟️</text>
      <text class="empty-text">{{ emptyText }}</text>
      <button class="empty-btn" @click="goCenter">去领券</button>
    </view>

    <view v-else class="coupon-list">
      <view
        v-for="item in coupons"
        :key="item.id"
        class="coupon-card"
        :class="{ 'card-invalid': item.status !== 'unused' }">
        <view class="coupon-left" :class="[typeClass(item), { 'left-invalid': item.status !== 'unused' }]">
          <template v-if="item.type === 'discount'">
            <text class="coupon-value">{{ discountText(item) }}</text>
            <text class="coupon-value-unit">折</text>
          </template>
          <template v-else>
            <text class="coupon-value-unit">¥</text>
            <text class="coupon-value">{{ amountText(item.discountAmount) }}</text>
          </template>
          <text class="coupon-threshold">{{ thresholdText(item) }}</text>
        </view>
        <view class="coupon-right">
          <view class="coupon-info">
            <text class="coupon-name">{{ item.name }}</text>
            <text class="coupon-desc">{{ scopeText(item) }}</text>
            <text class="coupon-date">
              {{ statusText(item) }}
            </text>
          </view>
          <view v-if="item.status === 'used'" class="status-stamp">已使用</view>
          <view v-else-if="item.status === 'expired'" class="status-stamp expired">已过期</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'
import { api } from '../common/js/api.js'

export default {
  data() {
    return {
      loading: true,
      loadError: false,
      loadErrorMsg: '',
      currentStatus: 'unused',
      coupons: [],
      tabs: [
        { label: '未使用', value: 'unused' },
        { label: '已使用', value: 'used' },
        { label: '已过期', value: 'expired' }
      ]
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn']),
    emptyText() {
      const found = this.tabs.find(t => t.value === this.currentStatus)
      return `暂无${found ? found.label : ''}的优惠券`
    }
  },
  onShow() {
    if (!this.checkLogin()) return
    this.loadCoupons()
  },
  methods: {
    checkLogin() {
      if (!this.isLoggedIn) {
        this.loading = false
        this.coupons = []
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => {
          uni.navigateTo({ url: '/pages-auth/login' })
        }, 1500)
        return false
      }
      return true
    },
    async loadCoupons() {
      this.loading = true
      this.loadError = false
      this.loadErrorMsg = ''
      try {
        const res = await api.getMyCoupons(this.currentStatus)
        this.coupons = (res && res.data) || []
      } catch (e) {
        // 展示真实错误原因（认证失败/网络失败/网关错误等），不再笼统提示
        const msg = (e && e.message) || '网络请求失败'
        this.loadError = true
        this.loadErrorMsg = msg
        console.error('[我的卡包] 加载失败:', msg)
        uni.showToast({ title: msg, icon: 'none', duration: 2500 })
      } finally {
        this.loading = false
      }
    },
    switchTab(status) {
      if (this.currentStatus === status) return
      this.currentStatus = status
      this.loadCoupons()
    },
    typeClass(item) {
      return item.type === 'discount' ? 'discount' : 'reduction'
    },
    amountText(v) {
      if (v == null) return '0'
      return parseFloat(v).toString()
    },
    discountText(item) {
      const rate = parseFloat(item.discountRate || 0)
      return (rate * 10).toFixed(1).replace(/\.0$/, '')
    },
    thresholdText(item) {
      const t = parseFloat(item.thresholdAmount || 0)
      return t > 0 ? `满${t}元可用` : '无门槛'
    },
    scopeText(item) {
      if (item.scope === 'product') return '指定商品可用'
      if (item.scope === 'category') return '指定分类可用'
      return '全场通用'
    },
    statusText(item) {
      const end = this.formatDate(item.validEndTime)
      if (item.status === 'used') {
        return `使用时间：${this.formatDate(item.useTime)}`
      }
      if (item.status === 'expired') {
        return `过期时间：${end}`
      }
      return `有效期至：${end}`
    },
    formatDate(v) {
      if (!v) return '-'
      return String(v).replace('T', ' ').slice(0, 16)
    },
    goCenter() {
      uni.navigateTo({ url: '/user/coupon-center' })
    }
  }
}
</script>

<style scoped>
.my-coupons {
  min-height: 100vh;
  background-color: #f5f6f8;
  padding: 24rpx;
  box-sizing: border-box;
}

.tabs {
  display: flex;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 8rpx;
  margin-bottom: 24rpx;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 12rpx;
}
.tab-item.active {
  background: linear-gradient(135deg, #ff7a59, #ff4d4f);
}
.tab-text {
  font-size: 28rpx;
  color: #595959;
}
.tab-item.active .tab-text {
  color: #ffffff;
  font-weight: 600;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.coupon-card {
  display: flex;
  background: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}
.card-invalid {
  opacity: 0.65;
}

.coupon-left {
  width: 220rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32rpx 12rpx;
  color: #ffffff;
  position: relative;
}
.coupon-left.reduction {
  background: linear-gradient(135deg, #ff7a59, #ff4d4f);
}
.coupon-left.discount {
  background: linear-gradient(135deg, #ffb86c, #ff8c42);
}
.coupon-left.left-invalid {
  background: linear-gradient(135deg, #c0c4cc, #909399);
}
.coupon-left::after {
  content: '';
  position: absolute;
  right: -12rpx;
  top: 50%;
  transform: translateY(-50%);
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #f5f6f8;
}

.coupon-value {
  font-size: 56rpx;
  font-weight: 700;
  line-height: 1.1;
}
.coupon-value-unit {
  font-size: 28rpx;
  font-weight: 600;
}
.coupon-threshold {
  font-size: 22rpx;
  margin-top: 8rpx;
  opacity: 0.9;
}

.coupon-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 28rpx 28rpx 36rpx;
  min-width: 0;
}
.coupon-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
  flex: 1;
}
.coupon-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #262626;
}
.coupon-desc {
  font-size: 22rpx;
  color: #8c8c8c;
}
.coupon-date {
  font-size: 22rpx;
  color: #bfbfbf;
}

.status-stamp {
  flex-shrink: 0;
  margin-left: 20rpx;
  border: 2rpx solid #ff4d4f;
  color: #ff4d4f;
  border-radius: 8rpx;
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  transform: rotate(-12deg);
  font-weight: 600;
}
.status-stamp.expired {
  border-color: #909399;
  color: #909399;
}

.loading-box,
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
  gap: 16rpx;
}
.loading-text {
  color: #8c8c8c;
  font-size: 26rpx;
}
.empty-icon {
  font-size: 88rpx;
}
.empty-text {
  font-size: 28rpx;
  color: #595959;
}
.error-detail {
  font-size: 24rpx;
  color: #bfbfbf;
  word-break: break-all;
  text-align: center;
  padding: 0 40rpx;
}
.empty-btn {
  margin-top: 16rpx;
  width: 220rpx;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 36rpx;
  background: linear-gradient(135deg, #ff7a59, #ff4d4f);
  color: #ffffff;
  font-size: 28rpx;
  padding: 0;
  border: none;
}
</style>
