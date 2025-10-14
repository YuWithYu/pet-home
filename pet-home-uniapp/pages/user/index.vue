<template>
  <view class="user-container">
    <!-- 页面标题栏 -->
    <view class="page-header">
    </view>

    <!-- 已登录状态（直接显示，不需要登录） -->
    <view class="user-section">
      <!-- 用户个人信息卡片 -->
      <view class="user-profile-card">
        <!-- 用户信息头部 -->
        <view class="user-profile-header">
          <view class="user-avatar">
            <image :src="userAvatar" mode="aspectFill" v-if="userAvatar" />
            <view class="avatar-placeholder" v-else>🐱</view>
          </view>
          <view class="user-details">
            <view class="user-name">{{ userNickname || '宠友' + (userInfo.id || 'ddf94a72') }}</view>
            <view class="user-level-charm">
              <view class="level-badge">Lv.1</view>
              <view class="charm-text">魅力: 0</view>
              <view class="can-amount">
                <view class="can-icon">
                  <image src="/static/images/罐头.svg" mode="aspectFit" class="can-icon-image" />
                </view>
                <view class="can-text">罐头: 120g</view>
              </view>
            </view>
          </view>
          <view class="action-buttons">
            <view class="sign-btn">签到 2+</view>
            <view class="redeem-btn">兑换</view>
          </view>
        </view>
        
        <!-- 用户统计信息 -->
        <view class="user-stats">
          <view class="stat-item">
            <view class="stat-number">0</view>
            <view class="stat-label">关注</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-number">0</view>
            <view class="stat-label">粉丝</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-number">0</view>
            <view class="stat-label">动态</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-number">0</view>
            <view class="stat-label">获赞与收藏</view>
          </view>
        </view>
      </view>

      <!-- 服务功能区 -->
      <view class="orders-card">
        <view class="card-header">
          <view class="card-title">我的服务</view>
        </view>
        <view class="orders-grid">
          <view class="order-item" @click="goMyPets">
            <view class="order-icon">
              <image src="/static/images/我的宠物.svg" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的宠物</view>
          </view>
          <view class="order-item" @click="goMyCans">
            <view class="order-icon">
              <image src="/static/images/我的罐头.svg" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的罐头</view>
          </view>
          <view class="order-item" @click="goMyAppointments">
            <view class="order-icon">
              <image src="/static/images/我的预约.svg" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的预约</view>
          </view>
          <view class="order-item" @click="goMyFavorites">
            <view class="order-icon">
              <image src="/static/images/我的收藏.svg" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的收藏</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'User',

  computed: {
    ...mapGetters(['userInfo', 'userNickname', 'userAvatar', 'isLoggedIn'])
  },

  onLoad() {
    // 检查登录状态，如果未登录则跳转到登录页面
    if (!this.isLoggedIn) {
      uni.navigateTo({
        url: '/pages/user/login'
      })
      return
    }
  },

  methods: {

    goOrders(status) {
      uni.navigateTo({
        url: `/pages/user/orders?status=${status}`
      })
    },

    goMyPets() {
      uni.showToast({
        title: '我的宠物功能开发中',
        icon: 'none'
      })
    },

    goMyCans() {
      uni.showToast({
        title: '我的罐头功能开发中',
        icon: 'none'
      })
    },

    goMyAppointments() {
      uni.showToast({
        title: '我的预约功能开发中',
        icon: 'none'
      })
    },

    goMyFavorites() {
      uni.showToast({
        title: '我的收藏功能开发中',
        icon: 'none'
      })
    },


  }
}
</script>

<style lang="scss" scoped>
.user-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

/* 页面标题栏 */
.page-header {
  background-color: white;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .page-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    text-align: center;
  }
}

/* 登录区域 */
.login-section {
  padding: 40rpx;
}

.login-card {
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.login-gradient-bg {
  background-color: white;
  padding: 60rpx 40rpx;
  text-align: center;
  color: #333;

  .login-avatar {
    margin-bottom: 30rpx;

    .avatar-large {
      width: 120rpx;
      height: 120rpx;
      background-color: #f0f0f0;
      border: 2rpx solid #e0e0e0;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 60rpx;
      color: #999;
      margin: 0 auto;
    }
  }

  .login-text {
    font-size: 28rpx;
    margin-bottom: 30rpx;
    color: #666;
  }

  .login-btn {
    font-size: 32rpx;
    font-weight: bold;
    padding: 20rpx 60rpx;
    background-color: #ff6b35;
    color: white;
    border: none;
    border-radius: 12rpx;
    display: inline-block;
  }
}

/* 用户区域 */
.user-section {
  background-color: #f5f5f5;
}

/* 用户个人信息卡片 */
.user-profile-card {
  background-color: white;
  margin: 20rpx;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.08);
}

/* 用户信息头部 */
.user-profile-header {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f8f8f8;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background-color: #fff7e6;
  border: 2rpx solid #ff6b35;
  overflow: hidden;
  margin-right: 20rpx;

  image {
    width: 100%;
    height: 100%;
  }

  .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40rpx;
    background-color: #fff7e6;
  }
}

.user-details {
  flex: 1;
  color: #333;

  .user-name {
    font-size: 28rpx;
    font-weight: bold;
    margin-bottom: 8rpx;
    color: #333;
  }

  .user-level-charm {
    display: flex;
    align-items: center;
    margin-bottom: 6rpx;

    .level-badge {
      background-color: #ff6b35;
      color: white;
      font-size: 20rpx;
      padding: 4rpx 8rpx;
      border-radius: 8rpx;
      margin-right: 12rpx;
    }

    .charm-text {
      font-size: 22rpx;
      color: #666;
      margin-right: 20rpx;
    }
  }

  .can-amount {
    display: flex;
    align-items: center;

    .can-icon {
      margin-right: 6rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .can-icon-image {
        width: 24rpx;
        height: 24rpx;
      }
    }

    .can-text {
      font-size: 22rpx;
      color: #666;
    }
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12rpx;

  .sign-btn {
    background-color: #ff4757;
    color: white;
    font-size: 20rpx;
    padding: 8rpx 16rpx;
    border-radius: 16rpx;
    text-align: center;
    font-weight: bold;
  }

  .redeem-btn {
    background-color: white;
    color: #ff4757;
    font-size: 20rpx;
    padding: 6rpx 16rpx;
    border: 2rpx solid #ff4757;
    border-radius: 16rpx;
    text-align: center;
    font-weight: bold;
  }
}

/* 用户统计信息 */
.user-stats {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;

  .stat-item {
    flex: 1;
    text-align: center;

    .stat-number {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 6rpx;
    }

    .stat-label {
      font-size: 22rpx;
      color: #666;
    }
  }

  .stat-divider {
    width: 1rpx;
    height: 30rpx;
    background-color: #e0e0e0;
    margin: 0 15rpx;
  }
}

/* 订单功能区 */
.orders-card {
  background-color: white;
  margin: 0 20rpx 20rpx;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 40rpx;
  border-bottom: 1rpx solid #f8f8f8;

  .card-title {
    font-size: 28rpx;
    font-weight: normal;
    color: #333;
  }

  .card-more {
    font-size: 26rpx;
    color: #ff6b35;
  }
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15rpx;
  padding: 30rpx 20rpx;
}

.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15rpx;

.order-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .order-icon-image {
    width: 48rpx;
    height: 48rpx;
  }
}

  .order-text {
    font-size: 24rpx;
    color: #666;
  }
}


</style>

