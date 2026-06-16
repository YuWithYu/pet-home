<template>
  <view class="reviews-container">
    <view class="summary-card" v-if="!loading">
      <view class="summary-item">
        <view class="summary-value">{{ summary.avgRating }}</view>
        <view class="summary-label">综合评分</view>
      </view>
      <view class="summary-item">
        <view class="summary-value">{{ summary.total }}</view>
        <view class="summary-label">服务单数</view>
      </view>
      <view class="summary-item">
        <view class="summary-value">{{ summary.positiveRate }}</view>
        <view class="summary-label">好评率</view>
      </view>
    </view>

    <view class="distribution-card" v-if="!loading && reviews.length > 0">
      <view class="dist-row" v-for="row in ratingDistribution" :key="row.star">
        <text class="dist-label">{{ row.star }}星</text>
        <view class="dist-bar">
          <view class="dist-fill" :style="{ width: row.percent }"></view>
        </view>
        <text class="dist-count">{{ row.count }}</text>
      </view>
    </view>

    <view class="filter-row" v-if="!loading && reviews.length > 0">
      <view
        v-for="item in filterTabs"
        :key="item.key"
        class="filter-tab"
        :class="{ active: activeFilter === item.key }"
        @tap="activeFilter = item.key"
      >
        {{ item.label }}
      </view>
    </view>

    <view v-if="loading" class="state-card">加载中...</view>
    <view v-else-if="filteredReviews.length === 0" class="state-card">{{ emptyText }}</view>
    <scroll-view v-else class="list-wrap" scroll-y :show-scrollbar="false">
      <view v-for="(item, idx) in filteredReviews" :key="item.id + '-' + idx" class="review-card">
        <view class="review-top">
          <image v-if="item.userAvatar" class="avatar-img" :src="item.userAvatar" mode="aspectFill" />
          <view v-else class="avatar">{{ (item.userName || '匿').slice(0, 1) }}</view>
          <view class="review-main">
            <view class="name-line">
              <text class="user-name">{{ item.userName || '匿名用户' }}</text>
              <text class="time">{{ formatTime(item.createTime).slice(0, 10) }}</text>
            </view>
            <view class="stars">
              <text v-for="n in 5" :key="n" class="star" :class="{ on: n <= Number(item.rating || 0) }">★</text>
            </view>
          </view>
        </view>
        <view class="comment">{{ item.comment || '用户未填写文字评价' }}</view>
        <view class="order-ref">
          {{ formatOrderRef(item) }}
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'MyReviews',
  data() {
    return {
      loading: false,
      reviews: [],
      activeFilter: 'all',
      filterTabs: [
        { key: 'all', label: '全部' },
        { key: 'good', label: '好评(4-5星)' },
        { key: 'mid', label: '中评(3星)' },
        { key: 'bad', label: '待改进(1-2星)' }
      ]
    }
  },
  computed: {
    summary() {
      const total = this.reviews.length
      if (!total) {
        return {
          avgRating: '0.0',
          total: 0,
          positiveRate: '0%'
        }
      }
      const ratings = this.reviews.map((it) => Number(it.rating || 0))
      const scoreTotal = ratings.reduce((sum, cur) => sum + cur, 0)
      const avg = (scoreTotal / total).toFixed(1)
      const positive = ratings.filter((it) => it >= 4).length
      return {
        avgRating: avg,
        total,
        positiveRate: `${Math.round((positive / total) * 100)}%`
      }
    },
    ratingDistribution() {
      const total = this.reviews.length || 1
      return [5, 4, 3, 2, 1].map((star) => {
        const count = this.reviews.filter((it) => Number(it.rating || 0) === star).length
        return {
          star,
          count,
          percent: `${Math.round((count / total) * 100)}%`
        }
      })
    },
    filteredReviews() {
      if (this.activeFilter === 'all') return this.reviews
      if (this.activeFilter === 'good') return this.reviews.filter((it) => Number(it.rating || 0) >= 4)
      if (this.activeFilter === 'mid') return this.reviews.filter((it) => Number(it.rating || 0) === 3)
      return this.reviews.filter((it) => Number(it.rating || 0) <= 2)
    },
    emptyText() {
      if (this.reviews.length === 0) return '暂无评价记录'
      return '当前筛选下暂无评价'
    }
  },
  onLoad() {
    if (!uni.getStorageSync('staffToken')) {
      uni.navigateTo({ url: '/pages-auth/login' })
      return
    }
    this.loadReviews()
  },
  onPullDownRefresh() {
    this.loadReviews().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    async loadReviews() {
      this.loading = true
      try {
        const res = await api.getStaffMyReviews()
        if (res.code === 200 || res.code === 0) {
          const source = Array.isArray(res.data) ? res.data : []
          this.reviews = source.sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))
        } else {
          this.reviews = []
          uni.showToast({ title: res.msg || '加载失败', icon: 'none' })
        }
      } catch (e) {
        this.reviews = []
        uni.showToast({ title: '加载评价失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    formatTime(val) {
      if (!val) return '-'
      const str = String(val)
      return str.replace('T', ' ').slice(0, 19)
    },
    getMaskedName(name) {
      const str = String(name || '匿名用户').trim()
      if (str.length <= 1) return `${str}**`
      if (str.length === 2) return `${str.slice(0, 1)}*`
      return `${str.slice(0, 1)}**`
    },
    formatOrderRef(item) {
      const date = this.formatTime(item.createTime).slice(0, 10)
      const service = item.appointmentTypeName || '服务订单'
      return `${date} ${service}`
    }
  }
}
</script>

<style lang="scss" scoped>
.reviews-container {
  min-height: 100vh;
  background: #f6f7fb;
  padding: 24rpx;
  box-sizing: border-box;
}
.summary-card,
.distribution-card,
.state-card,
.review-card {
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(38, 54, 126, 0.06);
}
.summary-card {
  margin-bottom: 12rpx;
  padding: 18rpx 12rpx;
  display: flex;
}
.distribution-card {
  margin-bottom: 12rpx;
  padding: 18rpx 20rpx;
}
.dist-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
}
.dist-row:last-child {
  margin-bottom: 0;
}
.dist-label {
  width: 54rpx;
  color: #5f6f96;
  font-size: 22rpx;
}
.dist-bar {
  flex: 1;
  height: 12rpx;
  border-radius: 999rpx;
  background: #ebeffa;
  overflow: hidden;
}
.dist-fill {
  height: 12rpx;
  border-radius: 999rpx;
  background: linear-gradient(90deg, #6f88ff, #52c2ff);
}
.dist-count {
  width: 44rpx;
  text-align: right;
  color: #8a96b8;
  font-size: 22rpx;
  margin-left: 10rpx;
}
.filter-row {
  display: flex;
  margin-bottom: 10rpx;
}
.filter-tab {
  margin-right: 12rpx;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: #edf0f9;
  color: #7380a6;
  font-size: 22rpx;
}
.filter-tab.active {
  background: #dfe7ff;
  color: #3a58cb;
}
.summary-item {
  flex: 1;
  text-align: center;
}
.summary-value {
  color: #2e3a59;
  font-size: 36rpx;
  font-weight: 700;
}
.summary-label {
  margin-top: 6rpx;
  color: #97a0ba;
  font-size: 22rpx;
}
.state-card {
  padding: 48rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #8b90a7;
}
.list-wrap {
  height: calc(100vh - 500rpx);
}
.review-card {
  padding: 22rpx;
  margin-bottom: 16rpx;
}
.review-top {
  display: flex;
  align-items: flex-start;
}
.avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #7d8cff, #6fc2ff);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
}
.avatar-img {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
}
.review-main {
  flex: 1;
  margin-left: 16rpx;
}
.name-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.user-name {
  color: #2e3a59;
  font-size: 26rpx;
  font-weight: 600;
}
.time {
  color: #9aa2ba;
  font-size: 22rpx;
}
.stars {
  margin-top: 8rpx;
}
.star {
  color: #d7dbea;
  font-size: 26rpx;
  margin-right: 4rpx;
}
.star.on {
  color: #ffb237;
}
.comment {
  margin-top: 14rpx;
  color: #2d3347;
  font-size: 25rpx;
  line-height: 1.6;
}
.order-ref {
  margin-top: 14rpx;
  padding: 12rpx 14rpx;
  border-radius: 12rpx;
  background: #f3f6ff;
  color: #5f6f96;
  font-size: 24rpx;
}
</style>
