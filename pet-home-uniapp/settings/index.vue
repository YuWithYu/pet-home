<template>
  <view class="settings-container">
    <!-- 设置内容 -->
    <view class="settings-content">
      
      <!-- 第一组：个人相关 -->
      <view class="settings-group">
        <view class="settings-item" @click="goPersonalInfo">
          <text class="item-text">个人资料</text>
          <text class="item-arrow">></text>
        </view>
        <view class="divider"></view>
        <view class="settings-item" @click="goChangePassword">
          <text class="item-text">修改密码</text>
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
          <text class="item-text">关于宠物家</text>
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
import { api } from '@/common/js/api.js'

export default {
  name: 'Settings',
  methods: {

    // 个人资料：直接进入编辑个人信息页
    goPersonalInfo() {
      uni.navigateTo({
        url: '/user/edit-profile'
      })
    },

    // 修改密码
    goChangePassword() {
      uni.navigateTo({
        url: '/settings/change-password'
      })
    },

    // 投诉举报
    goReport() {
      uni.navigateTo({
        url: '/settings/report'
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
            this.handleDeleteAccount()
          }
        }
      })
    },

    // 处理注销账号
    async handleDeleteAccount() {
      try {
        // 显示加载提示
        uni.showLoading({
          title: '注销中...',
          mask: true
        })

        // 调用注销账号API
        const res = await api.deleteAccount()

        uni.hideLoading()

        if (res.code === 200 || res.code === 0) {
          // 注销成功，清除所有用户数据
          this.$store.dispatch('logout')
          
          // 清除本地存储
          uni.removeStorageSync('token')
          uni.removeStorageSync('userId')
          uni.removeStorageSync('userInfo')
          uni.removeStorageSync('username')

          uni.showToast({
            title: '账号已注销',
            icon: 'success',
            duration: 2000
          })

          // 跳转到登录页
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages-auth/login',
              success: () => {
              },
              fail: (err) => {
                console.error('跳转登录页失败:', err)
                uni.showToast({
                  title: '跳转失败，请重试',
                  icon: 'none'
                })
              }
            })
          }, 2000)
        } else {
          uni.showToast({
            title: res.msg || '注销失败',
            icon: 'none'
          })
        }
      } catch (err) {
        uni.hideLoading()
        console.error('注销账号失败:', err)
        uni.showToast({
          title: '注销失败，请检查网络连接',
          icon: 'none'
        })
      }
    },

    // 帮助与客服
    goHelp() {
      uni.navigateTo({
        url: '/settings/help'
      })
    },

    // 关于宠物家
    goAbout() {
      uni.navigateTo({
        url: '/settings/about'
      })
    },

    // 用户协议
    goUserAgreement() {
      uni.navigateTo({
        url: '/settings/agreement'
      })
    },

    // 隐私条款
    goPrivacyPolicy() {
      uni.navigateTo({
        url: '/settings/privacy'
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
              // 兼容工作人员登录：统一从设置页退出
              uni.removeStorageSync('staffToken')
              uni.removeStorageSync('staffInfo')
              
              // 延迟一下确保数据清除完成
              setTimeout(() => {
                uni.hideLoading()
                
                // 跳转到登录页
                uni.reLaunch({
                  url: '/pages-auth/login',
                  success: () => {
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

.settings-content {
  padding: 16rpx;
}

.settings-group {
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 20rpx;
  background: #fff;
  transition: background-color 0.2s ease;
}

.settings-item:active {
  background-color: #f8f8f8;
}

.item-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 400;
}

.item-arrow {
  font-size: 28rpx;
  color: #999;
  font-weight: 300;
}

.divider {
  height: 1rpx;
  background-color: #f0f0f0;
  margin: 0 20rpx;
}

.logout-section {
  margin-top: 24rpx;
  padding: 0 16rpx;
}

.logout-btn {
  width: 100%;
  height: 72rpx;
  background: #f5f5f5;
  border: none;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.logout-btn:active {
  background-color: #e8e8e8;
}

.logout-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 400;
}
</style>
