<template>
  <view class="user-container">
    <!-- 自定义渐变导航栏 -->
    <custom-navbar :title="null" :show-back="false"></custom-navbar>
    
    <!-- 自定义下拉刷新 -->
    <custom-pull-refresh :on-refresh="handleRefresh" @pull="onPull" @pull-end="onPullEnd">
      <!-- 下拉刷新提示区域 -->
      <view class="pull-refresh-tip" :style="{ height: pullHeight + 'px' }" v-if="pullHeight > 0">
        <text class="pull-refresh-text">{{ refreshText }}</text>
      </view>
      
      <!-- 已登录状态（直接显示，不需要登录） -->
      <view class="user-section" :style="{ paddingTop: (pullHeight > 0 ? 0 : navBarTotalHeight) + 'px' }">
      <!-- 黄色背景区域（半圆形） -->
      <view class="yellow-background-area">
        <!-- 移除所有图标 -->
      </view>
      
      <!-- 白色个人信息卡片 -->
      <view class="user-profile-card">
        <!-- 用户信息主体 -->
        <view class="user-info-main" @click="goToProfile">
          <view class="user-avatar-section">
            <view class="user-avatar">
              <image :src="userAvatar || '/static/images/garfield-default-avatar.png'" mode="aspectFill" />
            </view>
            <view class="user-basic-info">
              <view class="user-name">{{ userNickname || '宠友' + (userInfo.uid || 'ddf94a72') }}</view>
              <view class="user-level-assets">
                <view class="level-badge">Lv.{{ level }}</view>
                <view class="charm-text">魅力: {{ charm }}</view>
                <view class="can-amount">
                  <view class="can-icon">🥫</view>
                  <view class="can-text">{{ canAmount }}g</view>
                </view>
              </view>
            </view>
          </view>
          
          <!-- 操作按钮 -->
          <view class="action-buttons">
            <view class="sign-btn" @click="handleSignIn" :class="{ 'disabled': hasSignedToday }">
              {{ hasSignedToday ? '已签到' : '签到 2+' }}
            </view>
            <view class="redeem-btn" @click="goToExchange">兑换</view>
          </view>
        </view>
        
        <!-- 用户统计信息 -->
        <view class="user-stats">
          <view class="stat-item">
            <view class="stat-number">{{ stats.follows }}</view>
            <view class="stat-label">关注</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-number">{{ stats.fans }}</view>
            <view class="stat-label">粉丝</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-number">{{ stats.dynamics }}</view>
            <view class="stat-label">动态</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-number">{{ stats.likes }}</view>
            <view class="stat-label">获赞与收藏</view>
          </view>
        </view>
      </view>

      <!-- 服务功能区 -->
      <view class="orders-card">
        <view class="card-header">
          <view class="card-title">
            <view class="title-decoration"></view>
            我的服务
          </view>
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

      <!-- 我的权益 -->
      <view class="rights-card">
        <view class="card-header">
          <view class="card-title">
            <view class="title-decoration"></view>
            我的权益
          </view>
        </view>
        <view class="rights-grid">
          <view class="right-item" @click="handleFeedback">
            <view class="right-icon">
              <image src="/static/images/吐个槽.svg" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">吐个槽</view>
          </view>
          <view class="right-item" @click="handleReport">
            <view class="right-icon">
              <image src="/static/images/投诉举报.svg" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">投诉举报</view>
          </view>
          <view class="right-item" @click="handleCustomerService">
            <view class="right-icon">
              <image src="/static/images/客服.svg" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">客服</view>
          </view>
          <view class="right-item" @click="handleAbout">
            <view class="right-icon">
              <image src="/static/images/关于友猫.svg" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">关于友猫</view>
          </view>
          <view class="right-item" @click="handleOfficialCert">
            <view class="right-icon">
              <image src="/static/images/官方认证.svg" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">官方认证</view>
          </view>
          <view class="right-item" @click="handleMemberManage">
            <view class="right-icon">
              <image src="/static/images/成员管理.svg" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">成员管理</view>
          </view>
          <view class="right-item" @click="handleSettings">
            <view class="right-icon">
              <image src="/static/images/设置.svg" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">设置</view>
          </view>
        </view>
      </view>

    </view>
    </custom-pull-refresh>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'
import { api } from '@/common/js/api.js'
import CustomNavbar from '@/components/custom-navbar/custom-navbar.vue'
import CustomPullRefresh from '@/components/custom-pull-refresh/custom-pull-refresh.vue'

export default {
  name: 'User',
  components: {
    CustomNavbar,
    CustomPullRefresh
  },

  data() {
    return {
      level: 1,
      charm: 0,
      canAmount: 120,
      hasSignedToday: false,
      stats: {
        follows: 0,
        fans: 0,
        dynamics: 0,
        likes: 0
      },
      statusBarHeight: 0,
      navBarHeight: 44,
      pullHeight: 0,
      refreshText: '下拉刷新'
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'userNickname', 'userAvatar', 'isLoggedIn']),
    
    // 导航栏总高度（状态栏 + 导航栏）
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight
    }
  },

  onLoad() {
    // 获取系统信息，计算导航栏高度
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
        // 根据平台设置导航栏高度（rpx转px）
        this.navBarHeight = res.platform === 'ios' ? 44 : 48
      }
    })
    
    // 检查登录状态，如果未登录则跳转到登录页面
    if (!this.isLoggedIn) {
      uni.navigateTo({
        url: '/pages/user/login'
      })
      return
    }
    this.loadUserData()
  },

  // 自定义下拉刷新处理
  handleRefresh() {
    return new Promise((resolve, reject) => {
      console.log('开始自定义下拉刷新')
      
      // 重新加载用户数据
      this.loadUserData()
      
      // 模拟刷新延迟
      setTimeout(() => {
        resolve()
      }, 800)
    })
  },


  methods: {
    // 下拉事件
    onPull(height) {
      this.pullHeight = height
      if (height < 30) {
        this.refreshText = '下拉刷新'
      } else {
        this.refreshText = '释放更新'
      }
    },

    // 下拉结束事件
    onPullEnd() {
      this.pullHeight = 0
      this.refreshText = '加载中...'
      setTimeout(() => {
        this.refreshText = '加载成功'
        setTimeout(() => {
          this.pullHeight = 0
        }, 500)
      }, 800)
    },

    loadUserData() {
      api.getCurrentUser()
        .then(res => {
          if (res.code === 0) {
            const data = res.data
            this.level = data.level || 1
            this.charm = data.charm || 0
            this.canAmount = data.canAmount || 120
            this.hasSignedToday = data.hasSignedToday || false
            this.stats = {
              follows: data.stats?.follows || 0,
              fans: data.stats?.fans || 0,
              dynamics: data.stats?.dynamics || 0,
              likes: data.stats?.likes || 0
            }
          }
        })
        .catch(err => {
          console.error('加载用户数据失败:', err)
          uni.showToast({
            title: '加载数据失败',
            icon: 'none'
          })
        })
    },

    // 处理签到
    handleSignIn() {
      if (this.hasSignedToday) {
        uni.showToast({
          title: '今日已签到',
          icon: 'none'
        })
        return
      }

      uni.showLoading({
        title: '签到中...'
      })

      // 调用签到API
      api.signIn()
        .then(res => {
          uni.hideLoading()
          if (res.code === 0) {
            this.hasSignedToday = true
            this.canAmount += res.data.points || 2 // 签到获得积分
            uni.showToast({
              title: `签到成功，获得${res.data.points || 2}积分`,
              icon: 'success'
            })
          } else {
            uni.showToast({
              title: res.msg || '签到失败',
              icon: 'none'
            })
          }
        })
        .catch(err => {
          uni.hideLoading()
          console.error('签到失败:', err)
          uni.showToast({
            title: '签到失败，请重试',
            icon: 'none'
          })
        })
    },

    // 跳转到兑换页面
    goToExchange() {
      uni.navigateTo({
        url: '/pages/exchange/index'
      })
    },

    goOrders(status) {
      uni.navigateTo({
        url: `/pages/user/orders?status=${status}`
      })
    },

    goMyPets() {
      uni.navigateTo({
        url: '/pages/user/my-pets'
      })
    },

    goMyCans() {
      uni.showToast({
        title: '我的罐头功能开发中',
        icon: 'none'
      })
    },

    goMyAppointments() {
      uni.navigateTo({
        url: '/pages/user/my-appointments'
      })
    },

    goMyFavorites() {
      uni.showToast({
        title: '我的收藏功能开发中',
        icon: 'none'
      })
    },


    // 我的权益相关方法
    handleFeedback() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    handleReport() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    handleCustomerService() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    handleAbout() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    handleOfficialCert() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    handleMemberManage() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    handleSettings() {
      uni.navigateTo({
        url: '/pages/settings/index'
      })
    },

    // 跳转到个人资料页面
    goToProfile() {
      uni.navigateTo({
        url: '/pages/user/profile'
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

/* 移除页面标题栏样式 */

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
  /* padding-top 通过内联样式动态设置，适配不同设备 */
}

/* 黄色背景区域（半圆形，紧贴自定义导航栏） */
.yellow-background-area {
  background: linear-gradient(to right, #FF8C00 0%, #FFD700 100%);
  height: 280rpx; /* 减少高度，让半圆更合适 */
  border-bottom-left-radius: 50%; /* 真正的半圆形状 */
  border-bottom-right-radius: 50%; /* 真正的半圆形状 */
  position: relative;
  z-index: 1;
  margin-top: -20rpx; /* 负边距，让半圆往上贴到导航栏 */
  margin-left: 0; /* 从屏幕左边开始 */
  margin-right: 0; /* 到屏幕右边结束 */
  width: 100%; /* 全屏宽度 */
}

/* 顶部状态栏 */
.top-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx 10rpx;
  
  .status-left {
    display: flex;
    gap: 20rpx;
    
    .bell-icon, .scan-icon {
      font-size: 32rpx;
      color: #333;
    }
  }
  
  .status-right {
    display: flex;
    gap: 15rpx;
    
    .status-icon {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      color: #333;
      
      &.active {
        background: rgba(255, 255, 255, 0.3);
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          top: 8rpx;
          right: 8rpx;
          width: 12rpx;
          height: 12rpx;
          background: #ff4757;
          border-radius: 50%;
        }
      }
    }
  }
}

/* 白色个人信息卡片 */
.user-profile-card {
  background-color: white;
  margin: -220rpx 40rpx 20rpx; /* 继续增加负上边距，让卡片再往上挪 */
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 10;
}

/* 用户信息主体 */
.user-info-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
}

/* 用户头像和信息区域 */
.user-info-main {
  transition: background-color 0.2s ease;
}

.user-info-main:active {
  background-color: #f8f8f8;
}

.user-avatar-section {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #fff;
  border: 3rpx solid rgba(255, 255, 255, 0.8);
  overflow: hidden;
  margin-right: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

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
    font-size: 50rpx;
    background-color: #fff;
  }
}

.user-basic-info {
  flex: 1;
  color: #333;

  .user-name {
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 12rpx;
    color: #333;
  }

  .user-level-assets {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .level-badge {
      background-color: #ff6b35;
      color: white;
      font-size: 22rpx;
      padding: 6rpx 12rpx;
      border-radius: 12rpx;
      font-weight: bold;
    }

    .charm-text {
      font-size: 24rpx;
      color: #333;
      font-weight: 500;
    }

    .can-amount {
      display: flex;
      align-items: center;
      background: rgba(255, 255, 255, 0.2);
      padding: 4rpx 8rpx;
      border-radius: 8rpx;

      .can-icon {
        font-size: 20rpx;
        margin-right: 4rpx;
      }

      .can-text {
        font-size: 22rpx;
        color: #333;
        font-weight: 500;
      }
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
    font-size: 22rpx;
    padding: 12rpx 20rpx;
    border-radius: 20rpx;
    text-align: center;
    font-weight: bold;
    box-shadow: 0 4rpx 12rpx rgba(255, 71, 87, 0.3);
    transition: all 0.3s ease;
    
    &.disabled {
      background-color: #ccc;
      box-shadow: none;
    }
    
    &:active:not(.disabled) {
      transform: translateY(2rpx);
      box-shadow: 0 2rpx 6rpx rgba(255, 71, 87, 0.3);
    }
  }

  .redeem-btn {
    background-color: white;
    color: #ff4757;
    font-size: 22rpx;
    padding: 10rpx 20rpx;
    border: 2rpx solid #ff4757;
    border-radius: 20rpx;
    text-align: center;
    font-weight: bold;
    box-shadow: 0 4rpx 12rpx rgba(255, 71, 87, 0.2);
    transition: all 0.3s ease;
    
    &:active {
      transform: translateY(2rpx);
      box-shadow: 0 2rpx 6rpx rgba(255, 71, 87, 0.2);
    }
  }
}

/* 用户统计信息 */
.user-stats {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx 30rpx;
  background: white;

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
      font-weight: 500;
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
    display: flex;
    align-items: center;
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


/* 下拉刷新提示区域 */
.pull-refresh-tip {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  transition: height 0.1s ease;
}

.pull-refresh-text {
  color: #666;
  font-size: 24rpx;
  font-weight: 400;
  white-space: nowrap;
}

/* 标题装饰条 */
.title-decoration {
  width: 6rpx;
  height: 32rpx;
  background: linear-gradient(to bottom, #FF8C00, #FFD700);
  border-radius: 3rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

/* 我的权益卡片 */
.rights-card {
  background: #fff;
  margin: 20rpx;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.rights-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-top: 20rpx;
}

.right-item {
  width: 22%;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30rpx;
  padding: 20rpx 10rpx;
  border-radius: 16rpx;
  transition: background-color 0.2s ease;
}

.right-item:active {
  background-color: #f5f5f5;
}

.right-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.right-icon-image {
  width: 60rpx;
  height: 60rpx;
}

.right-text {
  font-size: 24rpx;
  color: #333;
  text-align: center;
  line-height: 1.2;
}
</style>

