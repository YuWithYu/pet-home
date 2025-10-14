<template>
  <view class="login-container">
    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 品牌区域 -->
      <view class="brand-section">
        <view class="brand-icon">
          <image src="/static/images/brand-logo.svg" mode="aspectFit" class="logo-image" />
        </view>
        <view class="brand-text">
          <view class="brand-title">宠物之家</view>
          <view class="brand-subtitle">让爱宠生活更美好</view>
        </view>
      </view>

      <!-- 登录表单 -->
      <view class="login-section">
        <!-- 手机号输入 -->
        <view class="input-group">
          <input
            class="login-input"
            type="number"
            v-model="phone"
            placeholder="请输入手机号"
            @input="onPhoneInput"
            @blur="onPhoneBlur"
          />
        </view>

        <!-- 密码输入 -->
        <view class="input-group">
          <input
            class="login-input"
            :type="showPassword ? 'text' : 'password'"
            v-model="password"
            placeholder="请输入密码"
            @input="onPasswordInput"
            @blur="onPasswordBlur"
          />
          <view class="password-toggle" @click="togglePassword">
            <image
              :src="showPassword ? '/static/images/eye-open.svg' : '/static/images/eye-close.svg'"
              mode="aspectFit"
              class="eye-icon"
            />
          </view>
        </view>

        <!-- 登录按钮 -->
        <button
          :class="['login-button', { 'active': canLogin, 'inactive': !canLogin }]"
          :disabled="!canLogin"
          @click="onLoginTap"
        >
          {{ loggingIn ? '登录中...' : '登录' }}
        </button>

        <!-- 分割线 -->
        <view class="divider">
          <view class="divider-line"></view>
          <view class="divider-text">或</view>
          <view class="divider-line"></view>
        </view>

        <!-- 微信一键登录 -->
        <button class="wechat-login-btn" @click="onWechatLogin">
          <view class="wechat-icon">💬</view>
          <view class="wechat-text">微信一键登录</view>
        </button>

        <!-- 快速注册 -->
        <view class="register-prompt">
          <text class="register-text">还没有账号？</text>
          <text class="register-link" @click="onRegisterTap">立即注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'Login',

  data() {
    return {
      phone: '',
      password: '',
      showPassword: false,
      canLogin: false,
      loggingIn: false
    }
  },

  methods: {
    // 手机号输入
    onPhoneInput(value) {
      this.phone = value
      this.validateForm()
    },

    // 手机号失去焦点
    onPhoneBlur() {
      if (this.phone && !util.isValidPhone(this.phone)) {
        util.showToast('请输入正确的手机号', 'none')
      }
    },

    // 密码输入
    onPasswordInput(value) {
      this.password = value
      this.validateForm()
    },

    // 密码失去焦点
    onPasswordBlur() {
      if (this.password && this.password.length < 6) {
        util.showToast('密码至少6位', 'none')
      }
    },

    // 显示/隐藏密码
    togglePassword() {
      this.showPassword = !this.showPassword
    },

    // 表单验证
    validateForm() {
      this.canLogin = this.phone &&
                     this.password &&
                     util.isValidPhone(this.phone) &&
                     this.password.length >= 6
    },

    // 登录
    onLoginTap() {
      if (!this.canLogin || this.loggingIn) return

      this.loggingIn = true

      // 调用登录API
      this.$api.loginByPhone(this.phone, this.password).then(res => {
        if (res.code === 0) {
          // 登录成功，保存用户信息和token
          const { token, uid } = res.data
          this.$store.commit('SET_TOKEN', token)
          this.$store.commit('SET_USER_INFO', {
            phone: this.phone,
            uid: uid,
            nickname: res.data.nickname || this.phone,
            avatar: res.data.avatar || ''
          })

          util.showToast('登录成功', 'success')

          // 延迟跳转，给用户看到成功提示
          setTimeout(() => {
            uni.switchTab({
              url: '/pages/user/index'
            })
          }, 1500)
        } else {
          util.showToast(res.msg || '登录失败', 'none')
        }
      }).catch(err => {
        console.error('登录失败:', err)
        util.showToast('登录失败，请检查网络连接', 'none')
      }).finally(() => {
        this.loggingIn = false
      })
    },

    // 注册
    onRegisterTap() {
      uni.navigateTo({
        url: '/pages/user/register'
      })
    },

    // 忘记密码
    onForgotPassword() {
      uni.showToast({
        title: '忘记密码功能开发中',
        icon: 'none'
      })
    },

    // 微信登录
    onWechatLogin() {
      // 在微信小程序中，使用微信登录
      uni.login({
        provider: 'weixin',
        success: (res) => {
          console.log('微信登录授权成功:', res)
          if (res.code) {
            // 调用后端API，用微信授权码获取用户信息和token
            this.$api.loginByWechat(res.code).then(response => {
              if (response.code === 0) {
                const { token, userInfo } = response.data
                this.$store.commit('SET_TOKEN', token)
                this.$store.commit('SET_USER_INFO', userInfo)

                uni.showToast({
                  title: '登录成功',
                  icon: 'success'
                })

                setTimeout(() => {
                  uni.switchTab({
                    url: '/pages/user/index'
                  })
                }, 1500)
              } else {
                uni.showToast({
                  title: response.msg || '微信登录失败，请稍后重试',
                  icon: 'none'
                })
              }
            }).catch(err => {
              console.error('微信登录失败:', err)
              uni.showToast({
                title: '微信登录失败，后端服务可能未启动',
                icon: 'none'
              })
            })
          } else {
            uni.showToast({
              title: '微信授权失败',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          console.error('微信登录调用失败:', err)
          if (err.errMsg && err.errMsg.includes('Failed to fetch')) {
            uni.showToast({
              title: '网络连接失败，请检查后端服务',
              icon: 'none'
            })
          } else {
            uni.showToast({
              title: '微信登录失败',
              icon: 'none'
            })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.main-content {
  width: 100%;
  max-width: 600rpx;
  background-color: #ffffff;
  border-radius: 32rpx;
  padding: 80rpx 60rpx 60rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.brand-section {
  text-align: center;
  margin-bottom: 80rpx;
}

.brand-icon {
  margin-bottom: 40rpx;

  .logo-image {
    width: 120rpx;
    height: 120rpx;
    border-radius: 24rpx;
  }
}

.brand-text {
  .brand-title {
    font-size: 48rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 12rpx;
  }

  .brand-subtitle {
    font-size: 28rpx;
    color: #666;
  }
}

.login-section {
  .input-group {
    margin-bottom: 40rpx;
    position: relative;

    &:last-child {
      margin-bottom: 60rpx;
    }
  }

  .login-input {
    width: 100%;
    height: 96rpx;
    padding: 0 30rpx;
    background-color: #f8f9fa;
    border: 2rpx solid #e9ecef;
    border-radius: 24rpx;
    font-size: 32rpx;
    color: #333;
    transition: all 0.3s ease;

    &::placeholder {
      color: #999;
    }

    &:focus {
      border-color: #ff6b35;
      background-color: white;
      box-shadow: 0 0 0 6rpx rgba(255, 107, 53, 0.1);
    }
  }

  .password-toggle {
    position: absolute;
    right: 15rpx;
    top: 50%;
    transform: translateY(-50%);
    padding: 20rpx;

    .eye-icon {
      width: 32rpx;
      height: 32rpx;
    }
  }

  .login-button {
    width: 100%;
    height: 96rpx;
    border-radius: 24rpx;
    font-size: 32rpx;
    font-weight: bold;
    border: none;
    margin-bottom: 40rpx;
    transition: all 0.3s ease;

    &.active {
      background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
      color: white;
      box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
    }

    &.inactive {
      background-color: #f0f0f0;
      color: #999;
    }
  }

  .divider {
    display: flex;
    align-items: center;
    margin: 40rpx 0;

    .divider-line {
      flex: 1;
      height: 1rpx;
      background-color: #e0e0e0;
    }

    .divider-text {
      padding: 0 30rpx;
      font-size: 24rpx;
      color: #999;
    }
  }

  .wechat-login-btn {
    width: 100%;
    height: 80rpx;
    border-radius: 20rpx;
    background-color: #07c160;
    color: white;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    font-size: 28rpx;
    font-weight: bold;
    margin-bottom: 40rpx;
    transition: all 0.3s ease;

    &:active {
      background-color: #06a552;
      transform: scale(0.98);
    }

    .wechat-icon {
      font-size: 32rpx;
    }

    .wechat-text {
      font-size: 28rpx;
    }
  }

  .register-prompt {
    text-align: center;

    .register-text {
      font-size: 28rpx;
      color: #666;
      margin-right: 8rpx;
    }

    .register-link {
      font-size: 28rpx;
      color: #ff6b35;
      font-weight: 600;
    }
  }
}
</style>
