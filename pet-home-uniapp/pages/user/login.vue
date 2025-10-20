<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="background-decoration">
      <view class="decoration-circle circle-1"></view>
      <view class="decoration-circle circle-2"></view>
      <view class="decoration-circle circle-3"></view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 品牌区域 -->
      <view class="brand-section">
        <view class="brand-icon">
          <view class="logo-container">
            <text class="logo-emoji">🐾</text>
          </view>
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
          <view class="input-icon">📱</view>
          <input
            class="login-input"
            type="number"
            v-model="phone"
            placeholder="请输入手机号"
            @input="onPhoneInput"
            @blur="onPhoneBlur"
            maxlength="11"
          />
        </view>

        <!-- 密码输入 -->
        <view class="input-group">
          <view class="input-icon">🔒</view>
          <input
            class="login-input"
            :type="showPassword ? 'text' : 'password'"
            v-model="password"
            placeholder="请输入密码"
            @input="onPasswordInput"
            @blur="onPasswordBlur"
          />
          <view class="password-toggle" @click="togglePassword">
            <text class="eye-icon">{{ showPassword ? '👁️' : '🙈' }}</text>
          </view>
        </view>

        <!-- 忘记密码 -->
        <view class="forgot-password" @click="onForgotPassword">
          <text class="forgot-text">忘记密码？</text>
        </view>

        <!-- 登录按钮 -->
        <button
          :class="['login-button', { 'active': canLogin, 'inactive': !canLogin }]"
          :disabled="!canLogin"
          @click="onLoginTap"
        >
          <view class="button-content">
            <text v-if="loggingIn" class="loading-icon">⏳</text>
            <text class="button-text">{{ loggingIn ? '登录中...' : '登录' }}</text>
          </view>
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
    onPhoneInput(e) {
      this.phone = e.detail.value
      this.validateForm()
    },

    // 手机号失去焦点
    onPhoneBlur() {
      if (this.phone && !util.isValidPhone(this.phone)) {
        util.showToast('请输入正确的手机号', 'none')
      }
    },

    // 密码输入
    onPasswordInput(e) {
      this.password = e.detail.value
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
      api.loginByPhone(this.phone, this.password).then(res => {
        if (res.code === 0) {
          // 登录成功，保存用户信息和token
          const { token, uid } = res.data
          
          // 保存到本地存储
          uni.setStorageSync('token', token)
          uni.setStorageSync('userInfo', {
            phone: this.phone,
            uid: uid,
            nickname: res.data.nickname || this.phone,
            avatar: res.data.avatar || '/static/images/garfield-default-avatar.png'
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
            api.loginByWechat(res.code).then(response => {
              if (response.code === 0) {
                const { token, userInfo } = response.data
                
                // 保存到本地存储
                uni.setStorageSync('token', token)
                // 确保微信登录用户也有默认头像
                if (!userInfo.avatar) {
                  userInfo.avatar = '/static/images/garfield-default-avatar.png'
                }
                uni.setStorageSync('userInfo', userInfo)

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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  position: relative;
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;

  .decoration-circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    animation: float 6s ease-in-out infinite;

    &.circle-1 {
      width: 200rpx;
      height: 200rpx;
      top: 10%;
      left: -50rpx;
      animation-delay: 0s;
    }

    &.circle-2 {
      width: 150rpx;
      height: 150rpx;
      top: 60%;
      right: -30rpx;
      animation-delay: 2s;
    }

    &.circle-3 {
      width: 100rpx;
      height: 100rpx;
      top: 30%;
      right: 20%;
      animation-delay: 4s;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.main-content {
  width: 100%;
  max-width: 600rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-radius: 40rpx;
  padding: 80rpx 60rpx 60rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 2;
  border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.brand-section {
  text-align: center;
  margin-bottom: 80rpx;
}

.brand-icon {
  margin-bottom: 40rpx;

  .logo-container {
    width: 120rpx;
    height: 120rpx;
    background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
    border-radius: 30rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto;
    box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);

    .logo-emoji {
      font-size: 60rpx;
    }
  }
}

.brand-text {
  .brand-title {
    font-size: 52rpx;
    font-weight: 700;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 16rpx;
    letter-spacing: 2rpx;
  }

  .brand-subtitle {
    font-size: 28rpx;
    color: #666;
    font-weight: 400;
  }
}

.login-section {
  .input-group {
    margin-bottom: 32rpx;
    position: relative;
    display: flex;
    align-items: center;
    background: #f8f9fa;
    border-radius: 28rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s ease;

    &:focus-within {
      border-color: #667eea;
      background: white;
      box-shadow: 0 0 0 6rpx rgba(102, 126, 234, 0.1);
    }

    .input-icon {
      padding: 0 24rpx;
      font-size: 32rpx;
      color: #999;
    }

    .login-input {
      flex: 1;
      height: 96rpx;
      padding: 0 20rpx 0 0;
      background: transparent;
      border: none;
      font-size: 32rpx;
      color: #333;

      &::placeholder {
        color: #999;
      }
    }

    .password-toggle {
      padding: 0 24rpx;
      cursor: pointer;

      .eye-icon {
        font-size: 32rpx;
        color: #999;
        transition: color 0.3s ease;

        &:hover {
          color: #667eea;
        }
      }
    }
  }

  .forgot-password {
    text-align: right;
    margin-bottom: 40rpx;

    .forgot-text {
      font-size: 26rpx;
      color: #667eea;
      font-weight: 500;
    }
  }

  .login-button {
    width: 100%;
    height: 96rpx;
    border-radius: 28rpx;
    border: none;
    margin-bottom: 40rpx;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
      transform: translateY(0);

      &:active {
        transform: translateY(2rpx);
        box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
      }
    }

    &.inactive {
      background: #f0f0f0;
      color: #999;
    }

    .button-content {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      height: 100%;

      .loading-icon {
        font-size: 28rpx;
        animation: spin 1s linear infinite;
      }

      .button-text {
        font-size: 32rpx;
        font-weight: 600;
        color: white;
      }
    }
  }

  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }

  .divider {
    display: flex;
    align-items: center;
    margin: 40rpx 0;

    .divider-line {
      flex: 1;
      height: 1rpx;
      background: linear-gradient(90deg, transparent 0%, #e0e0e0 50%, transparent 100%);
    }

    .divider-text {
      padding: 0 30rpx;
      font-size: 24rpx;
      color: #999;
      background: rgba(255, 255, 255, 0.95);
    }
  }

  .wechat-login-btn {
    width: 100%;
    height: 88rpx;
    border-radius: 28rpx;
    background: linear-gradient(135deg, #07c160 0%, #06a552 100%);
    color: white;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    font-size: 30rpx;
    font-weight: 600;
    margin-bottom: 40rpx;
    transition: all 0.3s ease;
    box-shadow: 0 6rpx 20rpx rgba(7, 193, 96, 0.3);

    &:active {
      transform: translateY(2rpx);
      box-shadow: 0 3rpx 10rpx rgba(7, 193, 96, 0.3);
    }

    .wechat-icon {
      font-size: 36rpx;
    }

    .wechat-text {
      font-size: 30rpx;
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
      color: #667eea;
      font-weight: 600;
      text-decoration: underline;
    }
  }
}
</style>
