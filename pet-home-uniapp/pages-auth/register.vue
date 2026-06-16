<template>
  <view class="register-page">
    <view class="page-wrapper">
      <view class="header-container">
        <view class="header-bg">
          <view class="header-text-box">
            <text class="header-line header-line-large">Hello!</text>
            <text class="header-line header-line-small">Good to see you here</text>
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
          <view class="field-line-wrapper sms-wrapper">
            <input
              class="field-input sms-input"
              v-model="form.smsCode"
              maxlength="6"
            />
            <button
              class="sms-btn"
              :class="{ 'is-disabled': !canSendSms || smsSending }"
              :disabled="!canSendSms || smsSending"
              @click="sendSms"
            >
              {{ smsSending ? `${smsCountdown}秒` : '获取验证码' }}
            </button>
          </view>
        </view>

        <view class="field-group">
          <text class="field-label">输入密码</text>
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

        <view class="field-group">
          <text class="field-label">再次输入密码</text>
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

        <view class="field-group">
          <text class="field-label">昵称</text>
          <view class="field-line-wrapper">
            <input
              class="field-input"
              v-model="form.nickname"
              maxlength="20"
            />
          </view>
        </view>

        <view class="button-group">
          <button
            class="btn btn-register"
            :class="{ 'is-disabled': !canRegister }"
            :disabled="!canRegister"
            @click="submitForm"
          >
            {{ registering ? '注册中...' : '注册' }}
          </button>
          <button class="btn btn-login" @click="goLogin">登录</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'
import { mapMutations } from 'vuex'

export default {
  name: 'Register',

  data() {
    return {
      smsSending: false,
      smsCountdown: 60,
      registering: false,
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
             this.form.password.length >= 6
    }
  },

  mounted() {
    // 确保输入框初始值为空
    this.form.phone = ''
    this.form.smsCode = ''
    this.form.password = ''
    this.form.confirmPassword = ''
    this.form.nickname = ''
  },

  methods: {
    ...mapMutations(['SET_USER_INFO', 'SET_TOKEN']),
    
    // 手机号输入
    onPhoneInput() {
      // 触发computed重新计算
    },

    onPhoneBlur() {
      if (this.form.phone && !util.isValidPhone(this.form.phone)) {
        util.showToast('请输入正确的手机号', 'none')
      }
    },

    // 密码输入
    onPasswordInput() {
      // 触发computed重新计算
    },

    onPasswordBlur() {
      if (this.form.password && this.form.password.length < 6) {
        util.showToast('密码至少6位', 'none')
      }
    },

    // 确认密码输入
    onConfirmPasswordInput() {
      // 触发computed重新计算
    },

    onConfirmPasswordBlur() {
      if (this.form.confirmPassword && this.form.password !== this.form.confirmPassword) {
        util.showToast('两次密码输入不一致', 'none')
      }
    },

    sendSms() {
      if (!this.canSendSms || this.smsSending) return

      this.smsSending = true
      this.smsCountdown = 60

      // 调用发送验证码API
      api.request({
        url: '/api/sms/send',
        method: 'POST',
        data: { phone: String(this.form.phone) }
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

      // 调用注册API
      api.register(
        String(this.form.phone), 
        String(this.form.password), 
        String(this.form.nickname), 
        String(this.form.smsCode)
      ).then(res => {
        if (res.code === 200 || res.code === 0) {
          // 注册成功，保存用户信息和token
          const { token, uid, nickname } = res.data
          
          // 保存到本地存储
          uni.setStorageSync('token', token)
          uni.setStorageSync('userId', uid) // 保存用户ID
          const userInfo = {
            id: uid, // 保存id字段
            uid: uid, // 兼容uid字段
            phone: this.form.phone,
            nickname: nickname || this.form.nickname || this.form.phone, // 优先使用后端返回的nickname
            avatar: res.data.avatar || '/static/images/login-dog.png'
          }
          uni.setStorageSync('userInfo', userInfo)
          
          // 更新Vuex store，确保isLoggedIn立即生效
          this.SET_TOKEN(token)
          this.SET_USER_INFO(userInfo)

          util.showToast('注册成功', 'success')

          // 延迟跳转
          setTimeout(() => {
            uni.switchTab({
              url: '/pages/main/index'
            })
          }, 1500)
        } else {
          // 检查错误信息，显示对应的提示
          const errorMsg = res.msg || '注册失败'
          if (errorMsg && (errorMsg.includes('手机号') || errorMsg.includes('已注册'))) {
            util.showToast('该手机号已注册账号，无法继续注册', 'none')
          } else if (errorMsg && (errorMsg.includes('昵称') || errorMsg.includes('已被使用'))) {
            util.showToast('昵称已被使用，请选择其他昵称', 'none')
          } else {
            util.showToast(errorMsg, 'none')
          }
        }
      }).catch(err => {
        console.error('注册失败:', err)
        // 检查错误信息，显示对应的提示
        const errorMsg = err.message || err.msg || err.toString()
        if (errorMsg && (errorMsg.includes('手机号') || errorMsg.includes('已注册'))) {
          util.showToast('该手机号已注册账号，无法继续注册', 'none')
        } else if (errorMsg && (errorMsg.includes('昵称') || errorMsg.includes('已被使用'))) {
          util.showToast('昵称已被使用，请选择其他昵称', 'none')
        } else {
          util.showToast('注册失败，请检查网络连接', 'none')
        }
      }).finally(() => {
        this.registering = false
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
.register-page {
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
  height: 1624rpx;
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
  padding-top: 80rpx;
  padding-left: 60rpx;
  padding-right: 60rpx;
  padding-bottom: 60rpx;
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
  font-weight: 600;
}

.header-line-large {
  font-size: 56rpx;
  line-height: 80rpx;
}

.header-line-small {
  font-size: 28rpx;
  line-height: 38rpx;
  font-weight: 400;
}

.dog-image {
  position: absolute;
  right: 30rpx;
  top: calc(80rpx + 40rpx + 80rpx + 60rpx + 60rpx - 40rpx - 450rpx + 110rpx);
  width: 450rpx;
  height: 450rpx;
  z-index: 10;
}

.content-card {
  margin-top: -40rpx;
  padding: 60rpx 60rpx 80rpx;
  box-sizing: border-box;
  background: rgba(249, 249, 249, 1);
  border-radius: 40rpx;
  box-shadow: 0 -8rpx 30rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 2;
}

.field-group {
  margin-bottom: 40rpx;
}

.field-label {
  display: block;
  font-size: 30rpx;
  color: rgba(141, 159, 94, 1);
  margin-bottom: 24rpx;
  font-weight: 500;
}

.field-line-wrapper {
  border-bottom: 2rpx solid #E0E0E0;
  padding-bottom: 16rpx;
  position: relative;
  display: flex;
  align-items: center;
}

.field-input {
  font-size: 30rpx;
  color: #333333;
  width: 100%;
  padding: 0;
  margin: 0;
  height: 44rpx;
  line-height: 44rpx;
  border: none;
  background-color: transparent;
  flex: 1;
}

.sms-wrapper {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.sms-input {
  flex: 1;
}

.sms-btn {
  height: 60rpx;
  padding: 0 24rpx;
  border-radius: 24rpx;
  font-size: 26rpx;
  font-weight: 500;
  border: none;
  background-color: rgba(141, 159, 94, 1);
  color: #ffffff;
  white-space: nowrap;
  flex-shrink: 0;
  
  &.is-disabled {
    opacity: 0.5;
    background-color: #E0E0E0;
    color: #999999;
  }
  
  &:not(.is-disabled):active {
    opacity: 0.9;
    transform: translateY(2rpx);
  }
}

.button-group {
  margin-top: 40rpx;
}

.btn {
  width: 100%;
  height: 96rpx;
  border-radius: 48rpx;
  font-size: 34rpx;
  line-height: 96rpx;
  text-align: center;
  margin-bottom: 30rpx;
  border: none;
  padding: 0;
  box-sizing: border-box;
}

.btn-register {
  color: #ffffff;
  background-color: rgba(141, 159, 94, 1);
  box-shadow: 0 8rpx 20rpx rgba(141, 159, 94, 0.35);
  
  &.is-disabled {
    opacity: 0.5;
    background-color: #E0E0E0;
    color: #999999;
    box-shadow: none;
  }
  
  &:not(.is-disabled):active {
    opacity: 0.9;
    transform: translateY(2rpx);
  }
}

.btn-login {
  color: #666666;
  background-color: #E0E0E0;
  box-shadow: none;
  
  &:active {
    opacity: 0.8;
    transform: translateY(2rpx);
  }
}
</style>
