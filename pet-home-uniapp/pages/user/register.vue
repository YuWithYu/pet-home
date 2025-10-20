<template>
  <view class="register-container">
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
          <view class="brand-subtitle">加入宠物之家，享受更多宠物服务</view>
        </view>
      </view>

      <!-- 注册表单 -->
      <view class="register-section">
        <!-- 手机号输入 -->
        <view class="input-group">
          <view class="input-icon">📱</view>
          <input
            class="register-input"
            type="number"
            v-model="form.phone"
            placeholder="请输入手机号"
            maxlength="11"
            @input="onPhoneInput"
            @blur="onPhoneBlur"
          />
        </view>

        <!-- 验证码输入 -->
        <view class="input-group sms-group">
          <view class="input-icon">🔐</view>
          <input
            class="register-input sms-code-input"
            v-model="form.smsCode"
            placeholder="请输入验证码"
            maxlength="6"
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
          <view class="input-icon">🔒</view>
          <input
            class="register-input"
            :type="showPassword ? 'text' : 'password'"
            v-model="form.password"
            placeholder="请输入密码（至少6位）"
            @input="onPasswordInput"
            @blur="onPasswordBlur"
          />
          <view class="password-toggle" @click="togglePassword">
            <text class="eye-icon">{{ showPassword ? '👁️' : '🙈' }}</text>
          </view>
        </view>

        <!-- 确认密码输入 -->
        <view class="input-group">
          <view class="input-icon">🔒</view>
          <input
            class="register-input"
            :type="showConfirmPassword ? 'text' : 'password'"
            v-model="form.confirmPassword"
            placeholder="请再次输入密码"
            @input="onConfirmPasswordInput"
            @blur="onConfirmPasswordBlur"
          />
          <view class="password-toggle" @click="toggleConfirmPassword">
            <text class="eye-icon">{{ showConfirmPassword ? '👁️' : '🙈' }}</text>
          </view>
        </view>

        <!-- 昵称输入 -->
        <view class="input-group">
          <view class="input-icon">👤</view>
          <input
            class="register-input"
            v-model="form.nickname"
            placeholder="请输入昵称（可选）"
            maxlength="20"
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
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'Register',

  data() {
    return {
      smsSending: false,
      smsCountdown: 60,
      registering: false,
      agreeProtocol: false,
      showPassword: false,
      showConfirmPassword: false,
      form: {
        phone: '',
        smsCode: '',
        password: '',
        confirmPassword: '',
        nickname: ''
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
    // 手机号输入
    onPhoneInput() {
      this.validateForm()
    },

    onPhoneBlur() {
      if (this.form.phone && !util.isValidPhone(this.form.phone)) {
        util.showToast('请输入正确的手机号', 'none')
      }
    },

    // 密码输入
    onPasswordInput() {
      this.validateForm()
    },

    onPasswordBlur() {
      if (this.form.password && this.form.password.length < 6) {
        util.showToast('密码至少6位', 'none')
      }
    },

    // 确认密码输入
    onConfirmPasswordInput() {
      this.validateForm()
    },

    onConfirmPasswordBlur() {
      if (this.form.confirmPassword && this.form.password !== this.form.confirmPassword) {
        util.showToast('两次密码输入不一致', 'none')
      }
    },

    // 显示/隐藏密码
    togglePassword() {
      this.showPassword = !this.showPassword
    },

    toggleConfirmPassword() {
      this.showConfirmPassword = !this.showConfirmPassword
    },

    // 表单验证
    validateForm() {
      // 触发computed重新计算
    },

    sendSms() {
      if (!this.canSendSms || this.smsSending) return

      this.smsSending = true
      this.smsCountdown = 60

      // 调用发送验证码API
      api.request({
        url: '/tz/sms/send',
        method: 'POST',
        data: { phone: String(this.form.phone) }  // 确保phone是字符串类型
      }).then(res => {
        if (res.code === 0) {
          util.showToast('验证码已发送', 'success')
        } else {
          util.showToast(res.msg || '发送失败', 'none')
          this.smsSending = false
          this.smsCountdown = 0
        }
      }).catch(err => {
        console.error('发送验证码失败:', err)
        util.showToast('发送失败，请检查网络', 'none')
        this.smsSending = false
        this.smsCountdown = 0
      })

      // 开始倒计时
      const timer = setInterval(() => {
        this.smsCountdown--
        if (this.smsCountdown <= 0) {
          clearInterval(timer)
          this.smsSending = false
        }
      }, 1000)
    },

    // 提交注册表单
    submitForm() {
      if (!this.canRegister || this.registering) return

      this.registering = true

      // 调用注册API，确保所有参数都是字符串类型
      api.register(
        String(this.form.phone), 
        String(this.form.password), 
        String(this.form.nickname), 
        String(this.form.smsCode)
      ).then(res => {
        if (res.code === 0) {
          // 注册成功，保存用户信息和token
          const { token, uid } = res.data
          
          // 保存到本地存储
          uni.setStorageSync('token', token)
          uni.setStorageSync('userInfo', {
            phone: this.form.phone,
            uid: uid,
            nickname: res.data.nickname || this.form.phone,
            avatar: res.data.avatar || '/static/images/garfield-default-avatar.png'
          })

          util.showToast('注册成功', 'success')

          // 延迟跳转，给用户看到成功提示
          setTimeout(() => {
            uni.switchTab({
              url: '/pages/user/index'
            })
          }, 1500)
        } else {
          util.showToast(res.msg || '注册失败', 'none')
        }
      }).catch(err => {
        console.error('注册失败:', err)
        util.showToast('注册失败，请检查网络连接', 'none')
      }).finally(() => {
        this.registering = false
      })
    },

    // 切换协议同意状态
    toggleAgreeProtocol() {
      this.agreeProtocol = !this.agreeProtocol
    },

    // 显示用户协议
    showProtocol() {
      uni.showToast({
        title: '用户协议功能开发中',
        icon: 'none'
      })
    },

    // 显示隐私政策
    showPrivacy() {
      uni.showToast({
        title: '隐私政策功能开发中',
        icon: 'none'
      })
    },

    // 跳转到登录页面
    goLogin() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.register-container {
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
  padding: 60rpx 50rpx 50rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 2;
  border: 1rpx solid rgba(255, 255, 255, 0.2);
}

.brand-section {
  text-align: center;
  margin-bottom: 60rpx;
}

.brand-icon {
  margin-bottom: 30rpx;

  .logo-container {
    width: 100rpx;
    height: 100rpx;
    background: linear-gradient(135deg, #ff6b35 0%, #f7931e 100%);
    border-radius: 25rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto;
    box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);

    .logo-emoji {
      font-size: 50rpx;
    }
  }
}

.brand-text {
  .brand-title {
    font-size: 44rpx;
    font-weight: 700;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 12rpx;
    letter-spacing: 2rpx;
  }

  .brand-subtitle {
    font-size: 26rpx;
    color: #666;
    font-weight: 400;
  }
}

.register-section {
  .input-group {
    margin-bottom: 28rpx;
    position: relative;
    display: flex;
    align-items: center;
    background: #f8f9fa;
    border-radius: 24rpx;
    border: 2rpx solid transparent;
    transition: all 0.3s ease;

    &:focus-within {
      border-color: #667eea;
      background: white;
      box-shadow: 0 0 0 6rpx rgba(102, 126, 234, 0.1);
    }

    .input-icon {
      padding: 0 20rpx;
      font-size: 28rpx;
      color: #999;
    }

    .register-input {
      flex: 1;
      height: 88rpx;
      padding: 0 16rpx 0 0;
      background: transparent;
      border: none;
      font-size: 30rpx;
      color: #333;

      &::placeholder {
        color: #999;
      }
    }

    .password-toggle {
      padding: 0 20rpx;
      cursor: pointer;

      .eye-icon {
        font-size: 28rpx;
        color: #999;
        transition: color 0.3s ease;

        &:hover {
          color: #667eea;
        }
      }
    }

    &.sms-group {
      .sms-code-input {
        flex: 1;
      }

      .sms-btn {
        height: 60rpx;
        padding: 0 20rpx;
        border-radius: 16rpx;
        font-size: 24rpx;
        font-weight: 500;
        border: none;
        margin-right: 16rpx;
        transition: all 0.3s ease;

        &.active {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        &.inactive {
          background: #f0f0f0;
          color: #999;
        }
      }
    }
  }

  .protocol-section {
    margin: 30rpx 0;

    .checkbox-wrapper {
      display: flex;
      align-items: flex-start;
      gap: 12rpx;

      .checkbox-input {
        display: none;
      }

      .checkbox-icon {
        width: 32rpx;
        height: 32rpx;
        border: 2rpx solid #ddd;
        border-radius: 6rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        margin-top: 2rpx;

        &.checked {
          background: #667eea;
          border-color: #667eea;

          &::after {
            content: '✓';
            color: white;
            font-size: 20rpx;
            font-weight: bold;
          }
        }
      }

      .protocol-text {
        flex: 1;
        font-size: 24rpx;
        color: #666;
        line-height: 1.5;

        .protocol-link {
          color: #667eea;
          text-decoration: underline;
        }
      }
    }
  }

  .register-button {
    width: 100%;
    height: 88rpx;
    border-radius: 24rpx;
    border: none;
    margin-bottom: 30rpx;
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
        font-size: 24rpx;
        animation: spin 1s linear infinite;
      }

      .button-text {
        font-size: 30rpx;
        font-weight: 600;
        color: white;
      }
    }
  }

  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
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
      color: #667eea;
      font-weight: 600;
      text-decoration: underline;
    }
  }
}
</style>