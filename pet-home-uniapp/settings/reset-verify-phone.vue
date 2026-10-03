<template>
  <view class="page">
    <view class="form-card">
      <view class="field-row">
        <text class="label">手机号</text>
        <text class="value">{{ phone }}</text>
      </view>
      <view class="field-row">
        <text class="label">验证码</text>
        <view class="code-row">
          <input class="input" v-model="code" placeholder="请填写验证码" maxlength="6" type="number" />
          <view class="sms-btn" :class="{ disabled: countdown > 0 }" @tap="onResend">
            <text>{{ countdown > 0 ? countdown + '秒后重试' : '获取验证码' }}</text>
          </view>
        </view>
      </view>
    </view>
    <view class="bottom-wrap">
      <button class="btn-submit" :class="{ disabled: !canSubmit }" :disabled="!canSubmit || submitting" @click="onSubmit">提交</button>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'
export default {
  name: 'ResetVerifyPhone',
  data() {
    return { phone: '', code: '', countdown: 0, submitting: false, timer: null }
  },
  computed: {
    canSubmit() { return this.phone && this.code.trim().length >= 4 }
  },
  onLoad(options) {
    const p = (options.phone || '').trim()
    if (p) this.phone = p
    this.startCountdown()
  },
  onUnload() { if (this.timer) clearInterval(this.timer) },
  methods: {
    startCountdown() {
      this.countdown = 59
      this.timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0 && this.timer) { clearInterval(this.timer); this.timer = null }
      }, 1000)
    },
    onResend() {
      if (this.countdown > 0) return
      if (!this.phone) { util.showToast('手机号为空', 'none'); return }
      util.showLoading('发送中...')
      api.sendSmsCode(this.phone).then(res => {
        util.hideLoading()
        if (res.code === 200 || res.code === 0) { util.showToast('验证码已发送', 'success'); this.startCountdown() }
        else util.showToast(res.msg || '发送失败', 'none')
      }).catch(() => { util.hideLoading(); util.showToast('发送失败', 'none') })
    },
    onSubmit() {
      if (!this.canSubmit || this.submitting) return
      this.submitting = true
      uni.setStorageSync('reset_phone', this.phone)
      uni.setStorageSync('reset_code', this.code.trim())
      uni.navigateTo({ url: '/settings/reset-set-password' })
      this.submitting = false
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
}
.form-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
}
.field-row { margin-bottom: 40rpx; }
.field-row:last-child { margin-bottom: 0; }
.label { display: block; font-size: 28rpx; color: #333; margin-bottom: 16rpx; }
.value { font-size: 30rpx; color: #333; }
.code-row { display: flex; align-items: center; border-bottom: 2rpx solid #e0e0e0; padding-bottom: 12rpx; }
.input { flex: 1; font-size: 28rpx; height: 60rpx; }
.sms-btn { font-size: 26rpx; color: #07c160; padding-left: 24rpx; }
.sms-btn.disabled { color: #999; }

.bottom-wrap {
  flex: 1;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 60rpx;
  padding-top: 48rpx;
}
.btn-submit {
  width: 320rpx;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  font-size: 28rpx;
  color: #fff;
  background: #07c160;
  border-radius: 12rpx;
  border: none;
}
.btn-submit.disabled { background: #ccc; color: #999; }
</style>
