<template>
  <view class="appointments-container">
    <!-- 标签栏 -->
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.status"
        :class="['tab-item', { 'active': currentTab === tab.status }]"
        @click="switchTab(tab.status)"
      >
        {{ tab.label }}
      </view>
    </view>

    <!-- 预约列表 -->
    <view class="appointments-list">
      <view v-if="loading" class="loading-container">
        <view class="loading-text">加载中...</view>
      </view>

      <view v-else-if="appointments.length === 0" class="empty-state">
        <view class="empty-icon">📋</view>
        <view class="empty-text">暂无预约记录</view>
        <view class="empty-desc">快去预约服务吧</view>
        <button class="go-book-btn" @click="goToServices">去预约</button>
      </view>

      <view v-else class="appointment-cards">
        <view
          v-for="appointment in appointments"
          :key="appointment.id"
          class="appointment-card"
          @click="viewDetail(appointment)"
        >
          <!-- 服务类型标题 -->
          <view class="card-header">
            <view class="service-type">
              <view class="service-icon">{{ getServiceIcon(appointment.serviceType) }}</view>
              <view class="service-name">{{ getServiceName(appointment.serviceType) }}</view>
            </view>
            <view :class="['status-badge', getStatusClass(appointment.status)]">
              {{ getStatusText(appointment.status) }}
            </view>
          </view>

          <!-- 预约信息 -->
          <view class="card-body">
            <view class="info-row">
              <view class="info-label">预约时间</view>
              <view class="info-value">{{ appointment.date }} {{ appointment.timeSlot }}</view>
            </view>
            <view class="info-row">
              <view class="info-label">服务地址</view>
              <view class="info-value">{{ appointment.location || '待确认' }}</view>
            </view>
            <view class="info-row">
              <view class="info-label">联系电话</view>
              <view class="info-value">{{ appointment.contactPhone }}</view>
            </view>
          </view>

          <!-- 价格和操作 -->
          <view class="card-footer">
            <view class="price-section">
              <text class="price-label">服务费用：</text>
              <text class="price-value">¥{{ appointment.price }}</text>
            </view>
            <view class="action-buttons">
              <button
                v-if="appointment.status === 'pending'"
                class="btn btn-cancel"
                @click.stop="cancelAppointment(appointment)"
              >
                取消预约
              </button>
              <button
                v-if="appointment.status === 'confirmed'"
                class="btn btn-contact"
                @click.stop="contactService(appointment)"
              >
                联系客服
              </button>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'MyAppointments',

  data() {
    return {
      tabs: [
        { label: '全部', status: 'all' },
        { label: '待确认', status: 'pending' },
        { label: '已确认', status: 'confirmed' },
        { label: '已完成', status: 'completed' },
        { label: '已取消', status: 'cancelled' }
      ],
      currentTab: 'all',
      appointments: [],
      loading: false
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn'])
  },

  onLoad(options) {
    if (options.status) {
      this.currentTab = options.status
    }
    this.checkLogin()
  },

  onShow() {
    if (this.isLoggedIn) {
      this.loadAppointments()
    }
  },

  methods: {
    // 检查登录状态
    checkLogin() {
      if (!this.isLoggedIn) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages/user/login'
          })
        }, 1500)
        return false
      }
      this.loadAppointments()
      return true
    },

    // 加载预约列表
    async loadAppointments() {
      if (!this.userInfo || !this.userInfo.uid) {
        return
      }

      try {
        this.loading = true
        
        const res = await this.$api.getUserAppointments(this.userInfo.uid)

        if (res.code === 0 && res.data) {
          let appointments = res.data

          // 根据当前标签过滤
          if (this.currentTab !== 'all') {
            appointments = appointments.filter(a => a.status === this.currentTab)
          }

          this.appointments = appointments
        } else {
          this.appointments = []
        }
      } catch (error) {
        console.error('加载预约列表失败:', error)
        this.appointments = []
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    // 切换标签
    switchTab(status) {
      this.currentTab = status
      this.loadAppointments()
    },

    // 获取服务图标
    getServiceIcon(serviceType) {
      const icons = {
        'door-cleaning': '🏠',
        'grooming': '✨',
        'hospital': '🏥',
        'boarding': '🏡'
      }
      return icons[serviceType] || '📋'
    },

    // 获取服务名称
    getServiceName(serviceType) {
      const names = {
        'door-cleaning': '上门铲屎服务',
        'grooming': '宠物洗护服务',
        'hospital': '宠物医院',
        'boarding': '宠物寄养'
      }
      return names[serviceType] || '服务'
    },

    // 获取状态文本
    getStatusText(status) {
      const texts = {
        'pending': '待确认',
        'confirmed': '已确认',
        'in-service': '服务中',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return texts[status] || status
    },

    // 获取状态样式类
    getStatusClass(status) {
      const classes = {
        'pending': 'status-pending',
        'confirmed': 'status-confirmed',
        'in-service': 'status-in-service',
        'completed': 'status-completed',
        'cancelled': 'status-cancelled'
      }
      return classes[status] || ''
    },

    // 查看详情
    viewDetail(appointment) {
      uni.navigateTo({
        url: `/pages/appointment/detail?id=${appointment.id}`
      })
    },

    // 取消预约
    cancelAppointment(appointment) {
      uni.showModal({
        title: '确认取消',
        content: '确定要取消这个预约吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({
                title: '处理中...'
              })

              const result = await this.$api.updateAppointmentStatus(appointment.id, 'cancelled')
              
              console.log('取消预约API响应:', result)

              if (result.code === 0) {
                uni.hideLoading()
                uni.showToast({
                  title: '已取消预约',
                  icon: 'success'
                })
                this.loadAppointments()
              } else {
                uni.hideLoading()
                uni.showToast({
                  title: '取消失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('取消预约失败:', error)
              uni.hideLoading()
              uni.showToast({
                title: error.message || '取消失败，请稍后重试',
                icon: 'none'
              })
            }
          }
        }
      })
    },

    // 联系客服
    contactService(appointment) {
      uni.showToast({
        title: '客服功能开发中',
        icon: 'none'
      })
    },

    // 前往服务页面
    goToServices() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.appointments-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

/* 标签栏 */
.tabs {
  display: flex;
  background-color: white;
  padding: 0 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  
  .tab-item {
    flex: 1;
    text-align: center;
    padding: 30rpx 10rpx;
    font-size: 28rpx;
    color: #666;
    position: relative;
    
    &.active {
      color: #667eea;
      font-weight: bold;
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 6rpx;
        background-color: #667eea;
        border-radius: 3rpx;
      }
    }
  }
}

/* 列表 */
.appointments-list {
  padding: 20rpx;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
  
  .loading-text {
    font-size: 28rpx;
    color: #999;
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 150rpx 40rpx;
  
  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 40rpx;
  }
  
  .empty-text {
    font-size: 32rpx;
    color: #333;
    margin-bottom: 20rpx;
  }
  
  .empty-desc {
    font-size: 26rpx;
    color: #999;
    margin-bottom: 60rpx;
  }
  
  .go-book-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 24rpx 60rpx;
    border-radius: 50rpx;
    font-size: 28rpx;
  }
}

/* 预约卡片 */
.appointment-cards {
  .appointment-card {
    background-color: white;
    border-radius: 16rpx;
    padding: 20rpx;
    margin-bottom: 16rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  }
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .service-type {
    display: flex;
    align-items: center;
    
    .service-icon {
      font-size: 40rpx;
      margin-right: 12rpx;
    }
    
    .service-name {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
    }
  }
  
  .status-badge {
    padding: 8rpx 20rpx;
    border-radius: 20rpx;
    font-size: 24rpx;
    
    &.status-pending {
      background-color: #fff7e6;
      color: #ff9800;
    }
    
    &.status-confirmed {
      background-color: #e8f4fd;
      color: #2196f3;
    }
    
    &.status-in-service {
      background-color: #fff3e0;
      color: #ff6b35;
    }
    
    &.status-completed {
      background-color: #e8f5e9;
      color: #4caf50;
    }
    
    &.status-cancelled {
      background-color: #fafafa;
      color: #999;
    }
  }
}

/* 卡片主体 */
.card-body {
  .info-row {
    display: flex;
    margin-bottom: 16rpx;
    font-size: 26rpx;
    
    .info-label {
      width: 140rpx;
      color: #999;
      flex-shrink: 0;
    }
    
    .info-value {
      flex: 1;
      color: #333;
    }
  }
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
  
  .price-section {
    .price-label {
      font-size: 26rpx;
      color: #999;
    }
    
    .price-value {
      font-size: 32rpx;
      color: #ff6b35;
      font-weight: bold;
      margin-left: 8rpx;
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 16rpx;
    
    .btn {
      padding: 8rpx 20rpx;
      border-radius: 20rpx;
      font-size: 22rpx;
      border: none;
      
      &.btn-cancel {
        background-color: #f5f5f5;
        color: #666;
      }
      
      &.btn-contact {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }
    }
  }
}
</style>

