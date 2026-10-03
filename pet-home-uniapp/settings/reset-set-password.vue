<template>
  <view class="page">
    <view class="tip">请设置新密码。你可以用绑定的手机号+新密码登录，更快捷。</view>
    <view class="form-card">
      <view class="field-row">
        <text class="label">手机号</text>
        <text class="value">{{ maskedPhone }}</text>
      </view>
      <view class="field-row">
        <text class="label">新密码</text>
        <view class="input-wrap">
          <input
            class="input"
            :type="showNew ? 'text' : 'password'"
            v-model="form.newPassword"
            placeholder="填写新密码"
          />
        </view>
      </view>
      <view class="field-row">
        <text class="label">确认密码</text>
        <view class="input-wrap">
          <input
            class="input"
            :type="showConfirm ? 'text' : 'password'"
            v-model="form.confirmPassword"
            placeholder="再次填写确认"
          />
        </view>
      </view>
      <view class="hint">密码必须是8-16位的英文字母、数字、字符组合(不能是纯数字)</view>
      <button
        class="btn-done"
        :class="{ disabled: !canSubmit || submitting }"
        :disabled="!canSubmit || submitting"
        @click="onDone"
      >
        完成
      </button>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'ResetSetPassword',
  data() {
    return {
      phone: '',
      showNew: false,
      showConfirm: false,
      submitting: false,
      form: {
        newPassword: '',
        confirmPassword: ''
      }
    }
  },
  computed: {
    maskedPhone() {
      const p = this.phone || ''
      if (p.length === 11) return p.slice(0, 3) + '****' + p.slice(7)
      return p || '***'
    },
    canSubmit() {
      const a = this.form.newPassword.trim()
      const b = this.form.confirmPassword.trim()
      return a.length >= 8 && a.length <= 16 && a === b && /^(?!\d+$)[\w!@#$%^&*()-+=]+$/.test(a)
    }
  },
  onLoad() {
    this.phone = uni.getStorageSync('reset_phone') || ''
    if (!this.phone) {
      util.showToast('请先验证手机号', 'none')
      setTimeout(() => uni.navigateBack(), 1500)
    }
  },
  methods: {
    onDone() {
      if (!this.canSubmit || this.submitting) return
      const code = uni.getStorageSync('reset_code') || ''
      if (!code) {
        util.showToast('验证码已失效，请重新获取', 'none')
        return
      }
      this.submitting = true
      api.resetPassword(this.phone, code, this.form.newPassword.trim())
        .then(res => {
          if (res.code === 200 || res.code === 0) {
            util.showToast('密码设置成功，请使用新密码登录', 'success')
            uni.removeStorageSync('reset_phone')
            uni.removeStorageSync('reset_code')
            setTimeout(() => {
              uni.navigateBack({ delta: 3 })
            }, 1500)
          } else {
            util.showToast(res.msg || '设置失败', 'none')
          }
        })
        .catch(err => {
          util.showToast((err && (err.message || err.msg)) || '设置失败', 'none')
        })
        .finally(() => {
          this.submitting = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 40rpx 30rpx;
}
.tip {
  font-size: 26rpx;
  color: #999;
  line-height: 1.5;
  margin-bottom: 30rpx;
}
.form-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
}
.field-row {
  margin-bottom: 36rpx;
  .label {
    display: block;
    font-size: 28rpx;
    color: #333;
    margin-bottom: 12rpx;
  }
  .value {
    font-size: 30rpx;
    color: #666;
  }
  .input-wrap {
    border-bottom: 2rpx solid #e0e0e0;
    padding-bottom: 12rpx;
  }
  .input {
    font-size: 28rpx;
    width: 100%;
    height: 60rpx;
  }
}
.hint {
  font-size: 24rpx;
  color: #999;
  margin-top: -8rpx;
  margin-bottom: 40rpx;
}
.btn-done {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  font-size: 32rpx;
  color: #fff;
  background: #07c160;
  border-radius: 12rpx;
  border: none;
  &.disabled {
    background: #ccc;
    color: #999;
  }
}
</style>
