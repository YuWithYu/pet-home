<template>
  <view class="forgot-page">
    <view class="page-wrapper">
      <view class="header-container">
        <view class="header-bg">
          <view class="header-text-box">
            <text class="header-line">Hi Welcome</text>
            <text class="header-line">Back</text>
            <text class="header-desc">验证手机号后重置密码</text>
          </view>
        </view>
      </view>

      <image class="dog-image" src="/static/images/login-dog.png" mode="aspectFit"></image>

      <view class="content-card">
        <view class="field-group">
          <text class="field-label">手机号</text>
          <view class="field-line-wrapper">
            <input
              class="field-input"
              type="number"
              v-model="form.phone"
              @input="onPhoneInput"
              @blur="onPhoneBlur"
              maxlength="11"
            />
          </view>
        </view>

        <view class="field-group">
          <text class="field-label">验证码</text>
          <view class="field-line-wrapper field-line-sms">
            <input
              class="field-input"
              v-model="form.smsCode"
              maxlength="6"
            />
            <view
              class="sms-btn"
              :class="{ 'is-disabled': !canSendSms || smsSending }"
              @tap="sendSms"
            >
              <text>{{ smsSending ? smsCountdown + '秒' : '获取验证码' }}</text>
            </view>
          </view>
        </view>

        <view class="field-group">
          <text class="field-label">新密码</text>
          <view class="field-line-wrapper">
            <input
              class="field-input"
              :type="showPassword ? 'text' : 'password'"
              v-model="form.password"
              @input="onPasswordInput"
              @blur="onPasswordBlur"
            />
          </view>
        </view>

        <view class="field-group password-group">
          <text class="field-label">再次输入新密码</text>
          <view class="field-line-wrapper">
            <input
              class="field-input"
              :type="showConfirmPassword ? 'text' : 'password'"
              v-model="form.confirmPassword"
              @input="onConfirmPasswordInput"
              @blur="onConfirmPasswordBlur"
            />
          </view>
        </view>

        <view class="button-group">
          <button
            class="btn btn-submit"
            :class="{ 'is-disabled': !canSubmit }"
            :disabled="!canSubmit || submitting"
            @click="submitForm"
          >
            {{ submitting ? '提交中...' : '确认重置' }}
          </button>
          <button class="btn btn-back" @click="goLogin">返回登录</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'ForgotPassword',

  data() {
    return {
      smsSending: false,
      smsCountdown: 60,
      submitting: false,
      showPassword: false,
      showConfirmPassword: false,
      form: {
        phone: '',
        smsCode: '',
        password: '',
        confirmPassword: ''
      }
    }
  },

  computed: {
    canSendSms() {
      return this.form.phone && util.isValidPhone(this.form.phone)
    },

    canSubmit() {
      return this.form.phone &&
             this.form.smsCode &&
             this.form.password &&
             this.form.confirmPassword &&
             this.form.password === this.form.confirmPassword &&
             this.form.password.length >= 6
    }
  },

  onLoad(options = {}) {
    const phone = (options.phone || '').trim()
    if (phone && /^1\d{10}$/.test(phone)) {
      this.form.phone = phone
    }
  },

  methods: {
    onPhoneInput() {},
    onPhoneBlur() {
      if (this.form.phone && !util.isValidPhone(this.form.phone)) {
        util.showToast('请输入正确的手机号', 'none')
      }
    },
    onPasswordInput() {},
    onPasswordBlur() {
      if (this.form.password && this.form.password.length < 6) {
        util.showToast('密码至少6位', 'none')
      }
    },
    onConfirmPasswordInput() {},
    onConfirmPasswordBlur() {
      if (this.form.confirmPassword && this.form.password !== this.form.confirmPassword) {
        util.showToast('两次密码输入不一致', 'none')
      }
    },

    sendSms() {
      if (!this.canSendSms || this.smsSending) return

      this.smsSending = true
      this.smsCountdown = 60

      api.request({
        url: '/api/sms/send',
        method: 'POST',
        data: { phone: String(this.form.phone) },
        showLoading: true
      }).then(res => {
        if (res.code === 200 || res.code === 0) {
          util.showToast('验证码已发送', 'success')
        } else {
          util.showToast(res.msg || '发送失败', 'none')
          this.smsSending = false
          this.smsCountdown = 0
        }
      }).catch(err => {
        console.error('发送验证码失败:', err)
        const msg = (err && (err.message || err.msg)) || '发送失败，请检查网络'
        util.showToast(msg, 'none')
        this.smsSending = false
        this.smsCountdown = 0
      })

      const timer = setInterval(() => {
        this.smsCountdown--
        if (this.smsCountdown <= 0) {
          clearInterval(timer)
          this.smsSending = false
        }
      }, 1000)
    },

    submitForm() {
      if (!this.canSubmit || this.submitting) return

      this.submitting = true

      api.resetPassword(
        String(this.form.phone),
        String(this.form.smsCode),
        String(this.form.password)
      ).then(res => {
        if (res.code === 200 || res.code === 0) {
          util.showToast(res.data || '密码重置成功，请使用新密码登录', 'success')
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          util.showToast(res.msg || '重置失败', 'none')
        }
      }).catch(err => {
        console.error('重置密码失败:', err)
        const msg = (err && (err.msg || err.message)) || '重置失败，请检查网络'
        util.showToast(msg, 'none')
      }).finally(() => {
        this.submitting = false
      })
    },

    goLogin() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
/* 与登录首页保持一致：绿色背景、小狗图排版、输入框与按钮样式 */
.forgot-page {
  width: 750rpx;
  min-height: 1624rpx;
  margin: 0 auto;
  padding: 0;
  box-sizing: border-box;
  background-color: #ffffff;
  display: flex;
  justify-content: center;
}

.page-wrapper {
  width: 750rpx;
  min-height: 1624rpx;
  overflow: hidden;
  background-color: rgba(141, 159, 94, 1);
  box-sizing: border-box;
  position: relative;
}

.header-container {
  width: 100%;
  background-color: rgba(141, 159, 94, 1);
}

.header-bg {
  width: 100%;
  background-color: rgba(141, 159, 94, 1);
  padding-top: 140rpx;
  padding-left: 60rpx;
  padding-right: 60rpx;
  padding-bottom: 120rpx;
  position: relative;
  box-sizing: border-box;
  z-index: 1;
}

.header-text-box {
  margin-top: 40rpx;
  z-index: 1;
  position: relative;
}

.header-line {
  display: block;
  color: #ffffff;
  font-size: 56rpx;
  line-height: 80rpx;
  font-weight: 600;
}

.header-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.4;
}

.dog-image {
  position: absolute;
  right: 30rpx;
  top: 100rpx;
  width: 450rpx;
  height: 450rpx;
  z-index: 5;
  pointer-events: none;
}

.content-card {
  margin-top: -80rpx;
  padding: 60rpx 50rpx 90rpx;
  box-sizing: border-box;
  background: #ffffff;
  border-radius: 40rpx;
  box-shadow: 0 -8rpx 30rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 15;
}

.field-group {
  margin-bottom: 40rpx;
}

.password-group {
  margin-bottom: 28rpx;
}

.field-label {
  display: block;
  font-size: 26rpx;
  color: rgba(141, 159, 94, 1);
  margin-bottom: 16rpx;
  font-weight: 500;
}

.field-line-wrapper {
  border-bottom: 2rpx solid #E0E0E0;
  padding-bottom: 12rpx;
  position: relative;
  display: flex;
  align-items: center;
  min-height: 62rpx;
}

.field-line-sms {
  padding-right: 0;
}

.field-input {
  font-size: 26rpx;
  color: #333333;
  width: 100%;
  padding: 0;
  margin: 0;
  height: 38rpx;
  line-height: 38rpx;
  border: none;
  background-color: transparent;
  flex: 1;
}

.sms-btn {
  position: absolute;
  right: 0;
  bottom: 12rpx;
  padding: 8rpx 0 8rpx 16rpx;
  min-height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  z-index: 2;
}
.sms-btn text {
  font-size: 24rpx;
  color: rgba(141, 159, 94, 1);
  white-space: nowrap;
}
.sms-btn.is-disabled text {
  color: #999999;
}
.sms-btn-hover {
  opacity: 0.8;
}

.button-group {
  margin-top: 40rpx;
}

.btn {
  width: 100%;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  line-height: 80rpx;
  text-align: center;
  margin-bottom: 20rpx;
  border: none;
  padding: 0;
  box-sizing: border-box;
}

.btn-submit {
  color: #ffffff;
  background-color: #8BC34A;
  box-shadow: 0 8rpx 20rpx rgba(139, 195, 74, 0.35);
}
.btn-submit.is-disabled {
  opacity: 0.5;
  background-color: #E0E0E0;
  color: #999999;
  box-shadow: none;
}
.btn-submit:not(.is-disabled):active {
  opacity: 0.9;
  transform: translateY(2rpx);
}

.btn-back {
  color: #666666;
  background-color: #E0E0E0;
  box-shadow: none;
}
.btn-back:active {
  opacity: 0.8;
  transform: translateY(2rpx);
}
</style>
