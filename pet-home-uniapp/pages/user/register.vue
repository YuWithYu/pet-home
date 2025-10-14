<template>
  <view class="register-container">
    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 品牌区域 -->
      <view class="brand-section">
        <view class="brand-icon">
          <image src="/static/images/brand-logo.svg" mode="aspectFit" class="logo-image" />
        </view>
        <view class="brand-text">
          <view class="brand-title">宠物之家</view>
          <view class="brand-subtitle">加入宠物之家，享受更多宠物服务</view>
        </view>
      </view>

      <!-- 注册表单 -->
      <view class="register-section">
        <!-- 手机号输入 -->
        <view class="input-group">
          <input
            class="register-input"
            type="number"
            v-model="form.phone"
            placeholder="请输入手机号"
          />
        </view>

        <!-- 验证码输入 -->
        <view class="input-group sms-group">
          <input
            class="register-input sms-code-input"
            v-model="form.smsCode"
            placeholder="请输入验证码"
          />
          <button
            :class="['sms-btn', { 'active': canSendSms, 'inactive': !canSendSms || smsSending }]"
            :disabled="!canSendSms || smsSending"
            @click="sendSms"
          >
            {{ smsSending ? `${smsCountdown}s` : '获取验证码' }}
          </button>
        </view>

        <!-- 密码输入 -->
        <view class="input-group">
          <input
            class="register-input"
            type="password"
            v-model="form.password"
            placeholder="请输入密码（至少6位）"
          />
        </view>

        <!-- 确认密码输入 -->
        <view class="input-group">
          <input
            class="register-input"
            type="password"
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
          />
        </view>

        <!-- 邀请码输入（可选） -->
        <view class="input-group">
          <input
            class="register-input"
            v-model="form.inviteCode"
            placeholder="请输入邀请码（可选）"
          />
        </view>

        <!-- 用户协议 -->
        <view class="protocol-section">
          <view class="checkbox-wrapper" @click="toggleAgreeProtocol">
            <input type="checkbox" v-model="agreeProtocol" class="checkbox-input" />
            <view :class="['checkbox-icon', { 'checked': agreeProtocol }]"></view>
            <text class="protocol-text">
              我已阅读并同意
              <text class="protocol-link" @click.stop="showProtocol">《用户协议》</text>
              和
              <text class="protocol-link" @click.stop="showPrivacy">《隐私政策》</text>
            </text>
          </view>
        </view>

        <!-- 注册按钮 -->
        <button
          :class="['register-button', { 'active': canRegister, 'inactive': !canRegister }]"
          :disabled="!canRegister"
          @click="submitForm"
        >
          {{ registering ? '注册中...' : '立即注册' }}
        </button>

        <!-- 登录链接 -->
        <view class="login-prompt">
          <text class="login-text">已有账号？</text>
          <text class="login-link" @click="goLogin">立即登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { util } from '@/common/js/util.js'

export default {
  name: 'Register',

  data() {
    return {
      smsSending: false,
      smsCountdown: 60,
      registering: false,
      agreeProtocol: false,
      form: {
        phone: '',
        smsCode: '',
        password: '',
        confirmPassword: '',
        inviteCode: ''
      }
    }
  },

  computed: {
    canSendSms() {
      return this.form.phone && util.isValidPhone(this.form.phone)
    },

    canRegister() {
      return this.form.phone &&
             this.form.smsCode &&
             this.form.password &&
             this.form.confirmPassword &&
             this.form.password === this.form.confirmPassword &&
             this.form.password.length >= 6 &&
             this.agreeProtocol
    }
  },

  methods: {
    sendSms() {
      if (!this.canSendSms || this.smsSending) return

      this.smsSending = true
      this.smsCountdown = 60
      this.$util.showLoading('发送验证码中...')

      // 发送验证码API
      this.$api.sendSmsCode(this.form.phone).then(() => {
        uni.showToast({
          title: '验证码已发送',
          icon: 'success'
        })

        // 开始倒计时
        const timer = setInterval(() => {
          this.smsCountdown--
          if (this.smsCountdown <= 0) {
            clearInterval(timer)
            this.smsSending = false
          }
        }, 1000)
      }).catch(() => {
        this.smsSending = false
        uni.showToast({
          title: '发送失败',
          icon: 'none'
        })
      }).finally(() => {
        this.$util.hideLoading()
      })
    },

    submitForm() {
      if (!this.canRegister || this.registering) return

      // 手动验证表单
      if (!this.validateForm()) {
        return
      }

      this.registering = true
      this.$util.showLoading('注册中...')

      // 调用注册API
      this.$api.register({
        phone: this.form.phone,
        password: this.form.password,
        smsCode: this.form.smsCode,
        inviteCode: this.form.inviteCode
      }).then(res => {
        if (res.code === 0) {
          uni.showToast({
            title: '注册成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.msg || '注册失败',
            icon: 'none'
          })
        }
      }).catch(() => {
        uni.showToast({
          title: '注册失败',
          icon: 'none'
        })
      }).finally(() => {
        this.registering = false
        this.$util.hideLoading()
      })
    },

    // 表单验证
    validateForm() {
      if (!this.form.phone) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return false
      }

      if (!this.form.smsCode) {
        uni.showToast({
          title: '请输入验证码',
          icon: 'none'
        })
        return false
      }

      if (!this.form.password) {
        uni.showToast({
          title: '请输入密码',
          icon: 'none'
        })
        return false
      }

      if (this.form.password.length < 6) {
        uni.showToast({
          title: '密码至少6位',
          icon: 'none'
        })
        return false
      }

      if (this.form.password !== this.form.confirmPassword) {
        uni.showToast({
          title: '两次密码不一致',
          icon: 'none'
        })
        return false
      }

      if (!this.agreeProtocol) {
        uni.showToast({
          title: '请同意用户协议',
          icon: 'none'
        })
        return false
      }

      return true
    },

    showProtocol() {
      uni.showToast({
        title: '用户协议页面开发中',
        icon: 'none',
        duration: 2000
      })
    },

    showPrivacy() {
      uni.showToast({
        title: '隐私政策页面开发中',
        icon: 'none',
        duration: 2000
      })
    },

    // 切换协议同意状态
    toggleAgreeProtocol() {
      this.agreeProtocol = !this.agreeProtocol
    },

    goLogin() {
      uni.navigateTo({
        url: '/pages/user/login'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
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
  margin-bottom: 60rpx;
}

.brand-icon {
  margin-bottom: 30rpx;

  .logo-image {
    width: 100rpx;
    height: 100rpx;
    border-radius: 20rpx;
  }
}

.brand-text {
  .brand-title {
    font-size: 40rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 8rpx;
  }

  .brand-subtitle {
    font-size: 26rpx;
    color: #666;
  }
}

.register-section {
  .input-group {
    margin-bottom: 32rpx;

    &:last-child {
      margin-bottom: 40rpx;
    }

    &.sms-group {
      display: flex;
      gap: 20rpx;
      align-items: center;

      .sms-code-input {
        flex: 1;
      }

      .sms-btn {
        width: 200rpx;
        height: 72rpx;
        border-radius: 18rpx;
        font-size: 24rpx;
        border: none;
        transition: all 0.3s ease;

        &.active {
          background-color: #ff6b35;
          color: white;
        }

        &.inactive {
          background-color: #f0f0f0;
          color: #999;
        }
      }
    }
  }

  .register-input {
    width: 100%;
    height: 88rpx;
    padding: 0 30rpx;
    background-color: #f8f9fa;
    border: 2rpx solid #e9ecef;
    border-radius: 22rpx;
    font-size: 30rpx;
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

  .protocol-section {
    margin-bottom: 40rpx;

    .checkbox-wrapper {
      display: flex;
      align-items: flex-start;
      gap: 16rpx;
      padding: 20rpx;
      border-radius: 12rpx;
      cursor: pointer;
      transition: background-color 0.3s ease;
      min-height: 60rpx;
      position: relative;

      &:active {
        background-color: #f8f9fa;
      }

      .checkbox-input {
        position: absolute;
        opacity: 0;
        width: 0;
        height: 0;
      }

      .checkbox-icon {
        width: 32rpx;
        height: 32rpx;
        border: 2rpx solid #ddd;
        border-radius: 6rpx;
        margin-top: 4rpx;
        position: relative;
        background-color: white;
        transition: all 0.3s ease;
        flex-shrink: 0;
        z-index: 2;

        &::after {
          content: '';
          position: absolute;
          left: 50%;
          top: 50%;
          transform: translate(-50%, -50%);
          width: 16rpx;
          height: 16rpx;
          background-color: #ff6b35;
          border-radius: 2rpx;
          opacity: 0;
          transition: opacity 0.3s ease;
        }
      }

      .checkbox-icon.checked {
        border-color: #ff6b35;
        background-color: #fff7e6;

        &::after {
          opacity: 1;
        }
      }

      .protocol-text {
        font-size: 26rpx;
        color: #666;
        line-height: 1.4;
        flex: 1;
        user-select: none;
        z-index: 2;
        position: relative;
      }

      .protocol-link {
        color: #ff6b35;
        text-decoration: underline;
      }
    }
  }

  .register-button {
    width: 100%;
    height: 88rpx;
    border-radius: 22rpx;
    font-size: 32rpx;
    font-weight: bold;
    border: none;
    margin-bottom: 32rpx;
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

  .login-prompt {
    text-align: center;

    .login-text {
      font-size: 26rpx;
      color: #666;
      margin-right: 8rpx;
    }

    .login-link {
      font-size: 26rpx;
      color: #ff6b35;
      font-weight: 600;
    }
  }
}
</style>
