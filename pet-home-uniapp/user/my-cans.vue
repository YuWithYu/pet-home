<template>
  <view class="my-cans-container">
    <!-- 页面内容 -->
    <view class="cans-content">
      <!-- 用户信息区域 -->
      <view class="user-header">
        <view class="user-avatar">
          <image :src="userAvatar || '/static/images/login-dog.png'" mode="aspectFill" class="avatar-image" />
        </view>
        <view class="user-name">{{ userName || '用户' }}</view>
      </view>

      <!-- 可用罐头数 -->
      <view class="available-cans-section">
        <text class="section-title">可用罐头数</text>
        <view class="cans-display">
          <image src="/static/images/my-cans.png" mode="aspectFit" class="can-icon-large" />
          <text class="cans-amount">x {{ currentBalance }}g</text>
        </view>
      </view>

      <!-- 汇总栏 -->
      <view class="summary-bar">
        <view class="summary-item">
          <text class="summary-label">累计获取</text>
          <text class="summary-value">{{ totalEarned }}g</text>
        </view>
        <view class="summary-item highlight">
          <text class="summary-label">当前剩余</text>
          <text class="summary-value highlight-value">{{ currentBalance }}g</text>
        </view>
        <view class="summary-item">
          <text class="summary-label">累计消耗</text>
          <text class="summary-value">{{ totalSpent }}g</text>
        </view>
      </view>

      <!-- 标签页 -->
      <view class="tabs-container">
        <view 
          class="tab-item" 
          :class="{ active: currentTab === 'earn' }"
          @click="switchTab('earn')"
        >
          <text class="tab-text">获取明细</text>
          <view class="tab-underline" v-if="currentTab === 'earn'"></view>
        </view>
        <view 
          class="tab-item" 
          :class="{ active: currentTab === 'spend' }"
          @click="switchTab('spend')"
        >
          <text class="tab-text">消耗明细</text>
          <view class="tab-underline" v-if="currentTab === 'spend'"></view>
        </view>
      </view>

      <!-- 明细列表 -->
      <view class="records-list">
        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>
        <view v-else-if="currentRecords.length === 0" class="empty-state">
          <text class="empty-text">暂无{{ currentTab === 'earn' ? '获取' : '消耗' }}记录</text>
        </view>
        <view v-else>
          <view 
            v-for="(record, index) in currentRecords" 
            :key="record.id || index"
            class="record-item"
          >
            <view class="record-left">
              <text class="record-desc">{{ record.description }}</text>
            </view>
            <view class="record-right">
              <text class="record-amount" :class="{ 'earn': currentTab === 'earn', 'spend': currentTab === 'spend' }">
                {{ currentTab === 'earn' ? '+' : '-' }}{{ record.amount }}g
              </text>
              <text class="record-time">{{ formatTime(record.createTime || record.time) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'MyCans',
  data() {
    return {
      currentTab: 'earn', // 'earn' 获取明细, 'spend' 消耗明细
      userName: '',
      userAvatar: '',
      currentBalance: 0, // 当前剩余
      totalEarned: 0, // 累计获取
      totalSpent: 0, // 累计消耗
      earnRecords: [], // 获取记录
      spendRecords: [], // 消耗记录
      loading: false
    }
  },
  
  computed: {
    currentRecords() {
      return this.currentTab === 'earn' ? this.earnRecords : this.spendRecords
    }
  },
  
  onLoad() {
    // 立即隐藏系统级加载提示
    this.hideAllLoading()
    
    // 加载数据
    this.loadUserInfo()
    this.loadCansData()
  },
  
  onReady() {
    // 页面渲染完成后再次隐藏加载提示
    this.$nextTick(() => {
      this.hideAllLoading()
      setTimeout(() => {
        this.hideAllLoading()
      }, 50)
      setTimeout(() => {
        this.hideAllLoading()
      }, 100)
    })
  },
  
  onShow() {
    // 显示时也隐藏加载提示
    this.hideAllLoading()
  },
  
  methods: {
    // 隐藏所有加载提示
    hideAllLoading() {
      // #ifdef MP-WEIXIN
      try {
        wx.hideNavigationBarLoading()
        wx.hideLoading()
        wx.setNavigationBarLoading && wx.setNavigationBarLoading({ loading: false })
      } catch (e) {
        // 静默处理错误
      }
      // #endif
      try {
        uni.hideLoading()
        uni.hideNavigationBarLoading && uni.hideNavigationBarLoading()
      } catch (e) {
        // 静默处理错误
      }
    },
    
    // 加载用户信息
    async loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        this.userName = userInfo.nickname || userInfo.username || '用户'
        this.userAvatar = util.getImageUrl(userInfo.avatar || '/static/images/login-dog.png')
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    },
    
    // 加载罐头数据（当前剩余与任务中心统一用签到接口，保证两处显示一致）
    async loadCansData() {
      this.loading = true
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.id || userInfo.uid || uni.getStorageSync('userId')
        
        if (!userId) {
          this.loading = false
          return
        }
        
        const signInRes = await api.getSignInData()
        if (signInRes && (signInRes.code === 200 || signInRes.code === 0) && signInRes.data != null) {
          const b = signInRes.data.balance
          this.currentBalance = (b !== undefined && b !== null) ? Number(b) : 0
        }
        
        const statsRes = await api.getPointsStatistics(userId)
        if (statsRes && (statsRes.code === 200 || statsRes.code === 0) && statsRes.data) {
          const d = statsRes.data
          this.totalEarned = d.totalEarned != null ? Number(d.totalEarned) : 0
          this.totalSpent = d.totalSpent != null ? Number(d.totalSpent) : 0
        }
        
        await Promise.all([
          this.loadEarnRecords(),
          this.loadSpendRecords()
        ])
        
        if (this.totalEarned === 0 && this.earnRecords.length > 0) {
          this.totalEarned = this.earnRecords.reduce((sum, record) => sum + (record.amount || 0), 0)
        }
        if (this.totalSpent === 0 && this.spendRecords.length > 0) {
          this.totalSpent = this.spendRecords.reduce((sum, record) => sum + (record.amount || 0), 0)
        }
        
      } catch (error) {
        console.error('加载罐头数据失败:', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    
    // 加载获取明细
    async loadEarnRecords() {
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.id || userInfo.uid || uni.getStorageSync('userId')
        
        if (!userId) {
          this.earnRecords = []
          return
        }
        
        const res = await api.getPointsRecords(userId, 'earn')
        
        if (res && (res.code === 200 || res.code === 0) && res.data) {
          this.earnRecords = res.data.records || []
        } else {
          this.earnRecords = []
        }
      } catch (error) {
        console.error('加载获取明细失败:', error)
        this.earnRecords = []
      }
    },
    
    // 加载消耗明细
    async loadSpendRecords() {
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.id || userInfo.uid || uni.getStorageSync('userId')
        
        if (!userId) {
          this.spendRecords = []
          return
        }
        
        const res = await api.getPointsRecords(userId, 'spend')
        
        if (res && (res.code === 200 || res.code === 0) && res.data) {
          this.spendRecords = res.data.records || []
        } else {
          this.spendRecords = []
        }
      } catch (error) {
        console.error('加载消耗明细失败:', error)
        this.spendRecords = []
      }
    },
    
    // 切换标签
    switchTab(tab) {
      this.currentTab = tab
    },
    
    // 格式化时间（兼容iOS日期格式）
    formatTime(time) {
      if (!time) return ''
      // 使用util.parseDate来兼容iOS日期格式
      const date = util.parseDate(time)
      if (!date) return ''
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
  }
}
</script>

<style lang="scss" scoped>
.my-cans-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

/* 页面内容 */
.cans-content {
  padding: 20rpx;
}

/* 用户信息区域 */
.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16rpx;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.user-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 可用罐头数 */
.available-cans-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.section-title {
  font-size: 24rpx;
  color: #666;
  display: block;
  margin-bottom: 16rpx;
  text-align: center;
}

.cans-display {
  display: flex;
  align-items: center;
  gap: 12rpx;
  justify-content: center;
}

.can-icon-large {
  width: 50rpx;
  height: 50rpx;
}

.cans-amount {
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
}

/* 汇总栏 */
.summary-bar {
  background: #ffd700;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.summary-item.highlight {
  position: relative;
}

.summary-item.highlight::before {
  content: '';
  position: absolute;
  top: -8rpx;
  left: -8rpx;
  right: -8rpx;
  bottom: -8rpx;
  background: rgba(255, 0, 0, 0.1);
  border-radius: 16rpx;
  z-index: -1;
}

.summary-label {
  font-size: 22rpx;
  color: #666;
}

.summary-value {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.summary-value.highlight-value {
  color: #ff3b30;
  font-size: 32rpx;
}

/* 标签页 */
.tabs-container {
  background: #fff;
  border-radius: 16rpx;
  padding: 0 20rpx;
  margin-bottom: 16rpx;
  display: flex;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 0;
  position: relative;
}

.tab-text {
  font-size: 24rpx;
  color: #666;
}

.tab-item.active .tab-text {
  color: #ffd700;
  font-weight: 500;
}

.tab-underline {
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 4rpx;
  background: #ffd700;
  border-radius: 2rpx;
}

/* 明细列表 */
.records-list {
  background: #fff;
  border-radius: 16rpx;
  padding: 0;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.loading-state,
.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60rpx 20rpx;
  font-size: 24rpx;
  color: #999;
}

.record-item {
  padding: 16rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.record-item:last-child {
  border-bottom: none;
}

.record-left {
  flex: 1;
  display: flex;
  align-items: center;
}

.record-desc {
  font-size: 24rpx;
  color: #333;
}

.record-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6rpx;
}

.record-amount {
  font-size: 24rpx;
  font-weight: bold;
}

.record-amount.earn {
  color: #ff3b30;
}

.record-amount.spend {
  color: #333;
}

.record-time {
  font-size: 22rpx;
  color: #999;
}
</style>

