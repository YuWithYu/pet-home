<template>
  <view class="settings-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>设置</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>
    
    <!-- 设置内容 -->
    <view class="settings-content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      
      <!-- 第一组：个人相关 -->
      <view class="settings-group">
        <view class="settings-item" @click="goPersonalInfo">
          <text class="item-text">个人资料</text>
          <text class="item-arrow">></text>
        </view>
        <view class="divider"></view>
        <view class="settings-item" @click="goPersonalization">
          <text class="item-text">个性化选项</text>
          <text class="item-arrow">></text>
        </view>
        <view class="divider"></view>
        <view class="settings-item" @click="goStorage">
          <text class="item-text">存储空间</text>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 第二组：投诉相关 -->
      <view class="settings-group">
        <view class="settings-item" @click="goReport">
          <text class="item-text">投诉举报</text>
          <text class="item-arrow">></text>
        </view>
        <view class="divider"></view>
        <view class="settings-item" @click="goAccountDeletion">
          <text class="item-text">注销账号</text>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 第三组：帮助相关 -->
      <view class="settings-group">
        <view class="settings-item" @click="goHelp">
          <text class="item-text">帮助与客服</text>
          <text class="item-arrow">></text>
        </view>
        <view class="divider"></view>
        <view class="settings-item" @click="goAbout">
          <text class="item-text">关于友猫</text>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 第四组：协议相关 -->
      <view class="settings-group">
        <view class="settings-item" @click="goUserAgreement">
          <text class="item-text">用户协议</text>
          <text class="item-arrow">></text>
        </view>
        <view class="divider"></view>
        <view class="settings-item" @click="goPrivacyPolicy">
          <text class="item-text">隐私条款</text>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 退出登录按钮 -->
      <view class="logout-section">
        <button class="logout-btn" @click="handleLogout">
          <text class="logout-text">退出登录</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Settings',
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44
    }
  },
  computed: {
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight
    }
  },
  onLoad() {
    // 获取系统信息
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight
      }
    })
  },
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 个人资料
    goPersonalInfo() {
      uni.navigateTo({
        url: '/pages/user/profile'
      })
    },

    // 个性化选项
    goPersonalization() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    // 存储空间
    goStorage() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    // 投诉举报
    goReport() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    // 注销账号
    goAccountDeletion() {
      uni.showModal({
        title: '注销账号',
        content: '注销账号后将无法恢复，确定要继续吗？',
        confirmText: '确定注销',
        cancelText: '取消',
        confirmColor: '#ff4444',
        success: (res) => {
          if (res.confirm) {
            uni.showToast({
              title: '功能开发中...',
              icon: 'none'
            })
          }
        }
      })
    },

    // 帮助与客服
    goHelp() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    // 关于友猫
    goAbout() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    // 用户协议
    goUserAgreement() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    // 隐私条款
    goPrivacyPolicy() {
      uni.showToast({
        title: '功能开发中...',
        icon: 'none'
      })
    },

    // 退出登录
    handleLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        confirmText: '确定',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            try {
              // 显示加载提示
              uni.showLoading({
                title: '退出中...',
                mask: true
              })
              
              // 清除用户数据
              this.$store.dispatch('logout')
              
              // 延迟一下确保数据清除完成
              setTimeout(() => {
                uni.hideLoading()
                
                // 跳转到登录页
                uni.reLaunch({
                  url: '/pages/user/login',
                  success: () => {
                    console.log('退出登录成功，已跳转到登录页')
                  },
                  fail: (err) => {
                    console.error('跳转登录页失败:', err)
                    uni.showToast({
                      title: '跳转失败，请重试',
                      icon: 'none'
                    })
                  }
                })
              }, 500)
              
            } catch (error) {
              uni.hideLoading()
              console.error('退出登录失败:', error)
              uni.showToast({
                title: '退出失败，请重试',
                icon: 'none'
              })
            }
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.settings-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 自定义白色导航栏 */
.custom-white-navbar {
  width: 100%;
  height: 88rpx;
  background: #fff;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 99;
  border-bottom: 1rpx solid #f0f0f0;
}

.navbar-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
}

.navbar-left {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 48rpx;
  color: #333;
  font-weight: 300;
}

.navbar-title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.navbar-title text {
  font-size: 36rpx;
  color: #333;
  font-weight: 600;
}

.navbar-right {
  width: 80rpx;
  height: 60rpx;
}

.settings-content {
  padding: 20rpx;
}

.settings-group {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 24rpx;
  background: #fff;
  transition: background-color 0.2s ease;
}

.settings-item:active {
  background-color: #f8f8f8;
}

.item-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 400;
}

.item-arrow {
  font-size: 32rpx;
  color: #999;
  font-weight: 300;
}

.divider {
  height: 1rpx;
  background-color: #f0f0f0;
  margin: 0 24rpx;
}

.logout-section {
  margin-top: 40rpx;
  padding: 0 20rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  background: #f5f5f5;
  border: none;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.logout-btn:active {
  background-color: #e8e8e8;
}

.logout-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 400;
}
</style>
