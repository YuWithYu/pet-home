<template>
  <view class="coupon-center">
    <!-- 顶部提示 -->
    <view class="header-tip">
      <text class="tip-text">领券后下单自动抵扣，先到先得</text>
    </view>

    <!-- 券列表 -->
    <view v-if="loading" class="loading-box">
      <text class="loading-text">加载中...</text>
    </view>

    <view v-else-if="loadError" class="empty-box">
      <text class="empty-icon">⚠️</text>
      <text class="empty-text">加载失败</text>
      <text class="empty-text error-detail">{{ loadErrorMsg }}</text>
      <button class="empty-btn retry-btn" @click="loadCoupons">重试</button>
    </view>

    <view v-else-if="coupons.length === 0" class="empty-box">
      <text class="empty-icon">🎁</text>
      <text class="empty-text">暂无可领取的优惠券</text>
      <text class="empty-sub">去逛逛商品，好券随时上新</text>
    </view>

    <view v-else class="coupon-list">
      <view v-for="item in coupons" :key="item.id" class="coupon-card">
        <view class="coupon-left" :class="item.typeCls">
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
            <text class="coupon-desc">{{ scopeText(item) }} · 领取后{{ item.validDays }}天内有效</text>
            <text v-if="item.description" class="coupon-note">{{ item.description }}</text>
          </view>
          <button
            class="receive-btn"
            :class="{ disabled: !item.canReceive }"
            :disabled="!item.canReceive || item.receiving"
            @click="receive(item)">
            {{ item.receiving ? '领取中' : (item.canReceive ? '立即领取' : '已领取') }}
          </button>
        </view>
      </view>
    </view>

    <!-- 我的卡包入口 -->
    <view class="footer-entry" @click="goMyCoupons">
      <text class="footer-text">查看我的卡包</text>
      <text class="footer-arrow">›</text>
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
      coupons: []
    }
  },
  computed: {
    ...mapGetters(['isLoggedIn'])
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
        const res = await api.getCouponCenter()
        const list = (res && res.data) || []
        // 小程序 :class 不支持方法调用，typeCls 预计算挂到数据上
        this.coupons = list.map(c => ({
          ...c,
          receiving: false,
          typeCls: c.type === 'discount' ? 'discount' : 'reduction'
        }))
      } catch (e) {
        // 展示真实错误原因（认证失败/网络失败/网关错误等），不再笼统提示
        const msg = (e && e.message) || '网络请求失败'
        this.loadError = true
        this.loadErrorMsg = msg
        console.error('[领券中心] 加载失败:', msg)
        uni.showToast({ title: msg, icon: 'none', duration: 2500 })
      } finally {
        this.loading = false
      }
    },
    async receive(item) {
      if (!item.canReceive || item.receiving) return
      item.receiving = true
      try {
        await api.receiveCoupon(item.id)
        uni.showToast({ title: '领取成功', icon: 'success' })
        // 局部刷新：已领数量+1，达到限领则置灰（限领数以后端下发的 perUserLimit 为准）
        item.received = (item.received || 0) + 1
        const perLimit = item.perUserLimit || 1
        if (item.received >= perLimit) {
          item.canReceive = false
        }
      } catch (e) {
        uni.showToast({ title: (e && e.message) || '领取失败', icon: 'none' })
        this.loadCoupons()
      } finally {
        item.receiving = false
      }
    },
    amountText(v) {
      if (v == null) return '0'
      // 5.00 显示为 5，5.50 显示为 5.5
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
    goMyCoupons() {
      uni.navigateTo({ url: '/user/my-coupons' })
    }
  }
}
</script>

<style scoped>
.coupon-center {
  min-height: 100vh;
  background-color: #f5f6f8;
  padding: 24rpx 24rpx 140rpx;
  box-sizing: border-box;
}

.header-tip {
  background: linear-gradient(135deg, #fff7e6, #ffffff);
  border-radius: 16rpx;
  padding: 20rpx 28rpx;
  margin-bottom: 24rpx;
}
.tip-text {
  font-size: 26rpx;
  color: #b26a00;
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
/* 打孔缺口 */
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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.coupon-desc {
  font-size: 22rpx;
  color: #8c8c8c;
}
.coupon-note {
  font-size: 22rpx;
  color: #bfbfbf;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.receive-btn {
  flex-shrink: 0;
  margin-left: 20rpx;
  width: 152rpx;
  height: 64rpx;
  line-height: 64rpx;
  border-radius: 32rpx;
  background: linear-gradient(135deg, #ff7a59, #ff4d4f);
  color: #ffffff;
  font-size: 26rpx;
  font-weight: 600;
  padding: 0;
  border: none;
}
.receive-btn.disabled {
  background: #f0f0f0;
  color: #bfbfbf;
}

.loading-box,
.empty-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 160rpx 0;
  gap: 12rpx;
}
.loading-text {
  color: #8c8c8c;
  font-size: 26rpx;
}
.empty-icon {
  font-size: 88rpx;
}
.empty-text {
  font-size: 30rpx;
  color: #595959;
  font-weight: 600;
}
.empty-sub {
  font-size: 24rpx;
  color: #bfbfbf;
}
.error-detail {
  font-size: 24rpx;
  color: #bfbfbf;
  word-break: break-all;
  text-align: center;
  padding: 0 40rpx;
}
.retry-btn {
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

.footer-entry {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: 40rpx;
  background: #ffffff;
  border-radius: 48rpx;
  padding: 24rpx 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}
.footer-text {
  font-size: 28rpx;
  color: #262626;
  font-weight: 600;
}
.footer-arrow {
  font-size: 32rpx;
  color: #bfbfbf;
  margin-left: 8rpx;
}
</style>
