<template>
  <view class="verify-container">
    <view class="verify-content">
      <text class="desc">填写当前账号登录密码，验证本人身份。</text>
      <text class="desc-hint" v-if="showNoPwdHint">您使用微信登录，尚未设置密码，请先设置密码后再修改用户名。</text>
      <view class="input-wrap">
        <input
          class="pwd-input"
          type="password"
          v-model="password"
          placeholder="填写密码"
          @input="onInput"
        />
      </view>
      <view class="link-row">
        <text class="forgot-text" @click="goSetPassword" v-if="showNoPwdHint">去设置密码</text>
        <text class="forgot-text" @click="goForgot" v-else>忘记密码</text>
      </view>
    </view>
    <view class="btn-wrap">
      <button
        class="btn-verify"
        :class="{ active: password.trim().length > 0 }"
        :disabled="!password.trim()"
        @click="verify"
      >
        验证
      </button>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'EditUsernameVerify',
  data() {
    return {
      password: '',
      showNoPwdHint: false // 显示「未设置密码」提示
    }
  },
  methods: {
    onInput(e) {
      this.password = e.detail.value
    },
    goForgot() {
      uni.navigateTo({ url: '/pages/main/forgot-password' })
    },
    goSetPassword() {
      uni.navigateTo({ url: '/settings/change-password' })
    },
    verify() {
      const pwd = this.password.trim()
      if (!pwd) {
        uni.showToast({ title: '请填写密码', icon: 'none' })
        return
      }
      uni.showLoading({ title: '验证中...', mask: true })
      api.verifyPassword(pwd)
        .then(res => {
          uni.hideLoading()
          if (res.code === 200 || res.code === 0) {
            uni.navigateTo({ url: '/user/edit-username-new' })
          } else {
            const msg = res.msg || '密码错误'
            if (msg.indexOf('未设置密码') >= 0) {
              this.showNoPwdHint = true
              uni.showToast({ title: '请先设置密码', icon: 'none' })
            } else {
              uni.showToast({ title: msg, icon: 'none' })
            }
          }
        })
        .catch(err => {
          uni.hideLoading()
          const msg = (err && (err.msg || err.message || (err.response && err.response.data && err.response.data.msg))) || '验证失败'
          const msgStr = typeof msg === 'string' ? msg : String(msg)
          if (msgStr.indexOf('未设置密码') >= 0) {
            this.showNoPwdHint = true
            uni.showToast({ title: '请先设置密码', icon: 'none' })
          } else {
            uni.showToast({ title: msg, icon: 'none' })
          }
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.verify-container {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.verify-content {
  flex: 1;
  padding: 32rpx 24rpx 24rpx;
}
.desc {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 24rpx;
  line-height: 1.4;
}
.input-wrap {
  border-bottom: 1rpx solid #e5e5e5;
  margin-bottom: 16rpx;
}
.pwd-input {
  height: 72rpx;
  font-size: 26rpx;
  color: #333;
}
.link-row {
  margin-bottom: 0;
}
.forgot-text {
  font-size: 24rpx;
  color: #576b95;
}
.desc-hint {
  display: block;
  font-size: 24rpx;
  color: #f56c6c;
  margin-bottom: 16rpx;
  line-height: 1.4;
}
.btn-wrap {
  padding: 24rpx 24rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}
.btn-verify {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  color: #888;
  background: #e5e5e5;
  border-radius: 8rpx;
  border: none;
}
.btn-verify.active {
  background-color: #ffd700;
  color: #333;
}
.btn-verify::after { border: none; }
</style>
