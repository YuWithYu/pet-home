<template>
  <view class="change-password-container">
    <view class="form-card">
      <!-- 已设置过密码时显示原密码；微信登录无密码时隐藏 -->
      <view class="field-group" v-if="hasPassword">
        <text class="field-label">原密码</text>
        <view class="field-line">
          <input
            class="field-input"
            :type="showOld ? 'text' : 'password'"
            v-model="form.oldPassword"
            placeholder="请输入原密码"
          />
        </view>
      </view>
      <view class="field-group" v-else>
        <text class="field-hint">您使用微信登录，尚未设置密码，可直接设置新密码：</text>
      </view>
      <view class="field-group">
        <text class="field-label">新密码</text>
        <view class="field-line">
          <input
            class="field-input"
            :type="showNew ? 'text' : 'password'"
            v-model="form.newPassword"
            placeholder="请输入新密码（至少6位）"
          />
        </view>
      </view>
      <view class="field-group">
        <text class="field-label">确认新密码</text>
        <view class="field-line">
          <input
            class="field-input"
            :type="showConfirm ? 'text' : 'password'"
            v-model="form.confirmPassword"
            placeholder="请再次输入新密码"
          />
        </view>
      </view>
      <view class="password-hint">密码必须是8-16位的英文字母、数字、字符组合(不能是纯数字)</view>
      <view class="forgot-row" v-if="hasPassword" @click="onForgotPassword">
        <text class="forgot-link">忘记原密码?</text>
      </view>
      <view class="btn-wrap">
        <button
          class="btn-update"
          :class="{ 'is-disabled': !canSubmit || submitting }"
          :disabled="!canSubmit || submitting"
          @click="submitForm"
        >
          {{ submitting ? '提交中...' : (hasPassword ? '确认修改密码' : '确认设置密码') }}
        </button>
      </view>
    </view>

    <!-- 忘记原密码弹窗：与微信一致 -->
    <view class="modal-mask" v-if="showForgotModal" @click="closeForgotModal">
      <view class="modal-box" @click.stop>
        <view class="modal-body">
          <text class="modal-desc">你的账号当前已绑定手机号，可以通过短信验证码重置密码，是否发送验证码到 {{ maskedPhone }}？</text>
        </view>
        <view class="modal-actions">
          <view class="modal-btn cancel" @click="closeForgotModal">取消</view>
          <view class="modal-btn primary" @click="confirmSendCode">发送</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'ChangePassword',

  data() {
    return {
      showOld: false,
      showNew: false,
      showConfirm: false,
      submitting: false,
      showForgotModal: false,
      bindPhone: '',
      hasPassword: true, // 默认 true，加载后根据接口更新；微信登录无密码时为 false
      form: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }
  },

  computed: {
    canSubmit() {
      const f = this.form
      const pwdOk = f.newPassword.trim() && f.confirmPassword.trim() &&
        f.newPassword.length >= 6 && f.newPassword === f.confirmPassword
      if (this.hasPassword) {
        return f.oldPassword.trim() && pwdOk
      }
      return pwdOk
    },
    maskedPhone() {
      const p = this.bindPhone || ''
      if (p.length === 11) return p.slice(0, 3) + '****' + p.slice(7)
      return p ? p.slice(0, 3) + '****' + p.slice(-4) : '***'
    }
  },

  mounted() {
    api.hasPassword()
      .then(res => {
        if (res.code === 200 || res.code === 0) {
          this.hasPassword = !!res.data
          if (!this.hasPassword) {
            uni.setNavigationBarTitle({ title: '设置密码' })
          }
        }
      })
      .catch(() => {})
  },
  methods: {
    onForgotPassword() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      let phone = (userInfo.phone || userInfo.phoneNumber || userInfo.mobile || '').trim()
      if (phone) {
        this.bindPhone = phone
        this.showForgotModal = true
        return
      }
      const userId = userInfo.id || userInfo.uid || uni.getStorageSync('userId') || null
      if (!userId) {
        uni.showModal({
          title: '提示',
          content: '请先登录后再使用忘记密码功能。',
          showCancel: false
        })
        return
      }
      util.showLoading('获取手机号...')
      api.getCurrentUser({ userId: userId, showLoading: false })
        .then(res => {
          if ((res.code === 200 || res.code === 0) && res.data) {
            const data = res.data
            phone = (data.phone || data.phoneNumber || data.mobile || '').trim()
          }
          if (phone) {
            this.bindPhone = phone
            this.showForgotModal = true
          } else {
            uni.showModal({
              title: '提示',
              content: '未获取到绑定手机号。请先到个人资料页绑定手机号后再通过验证码重置密码。',
              cancelText: '取消',
              confirmText: '去个人资料',
              success: (res) => {
                if (res.confirm) uni.navigateTo({ url: '/user/edit-profile' })
              }
            })
          }
        })
        .catch(() => {
          uni.showModal({
            title: '提示',
            content: '获取用户信息失败，请检查网络后重试。若已绑定手机号，也可到个人资料页查看。',
            cancelText: '取消',
            confirmText: '去个人资料',
            success: (res) => {
              if (res.confirm) uni.navigateTo({ url: '/user/edit-profile' })
            }
          })
        })
        .finally(() => {
          util.hideLoading()
        })
    },
    closeForgotModal() {
      this.showForgotModal = false
    },
    confirmSendCode() {
      if (!this.bindPhone) {
        this.closeForgotModal()
        return
      }
      util.showLoading('发送中...')
      api.sendSmsCode(this.bindPhone)
        .then(res => {
          if (res.code === 200 || res.code === 0) {
            util.showToast('验证码已发送', 'success')
            this.closeForgotModal()
            uni.navigateTo({
              url: '/settings/reset-verify-phone?phone=' + encodeURIComponent(this.bindPhone)
            })
          } else {
            util.showToast(res.msg || '发送失败', 'none')
          }
        })
        .catch(err => {
          const msg = (err && (err.msg || err.message)) || '发送失败，请检查网络'
          util.showToast(msg, 'none')
        })
        .finally(() => {
          util.hideLoading()
        })
    },
    submitForm() {
      if (!this.canSubmit || this.submitting) return
      if (this.form.newPassword.length < 6) {
        util.showToast('新密码至少6位', 'none')
        return
      }
      if (this.form.newPassword !== this.form.confirmPassword) {
        util.showToast('两次输入的新密码不一致', 'none')
        return
      }

      this.submitting = true
      const oldPwd = this.hasPassword ? this.form.oldPassword.trim() : ''
      api.changePassword(oldPwd, this.form.newPassword.trim())
        .then(res => {
          if (res.code === 200 || res.code === 0) {
            util.showToast(res.data || '密码修改成功，请使用新密码登录', 'success')
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          } else {
            util.showToast(res.msg || '修改失败', 'none')
          }
        })
        .catch(err => {
          console.error('修改密码失败:', err)
          const msg = (err && (err.msg || err.message)) || '修改失败，请检查网络'
          util.showToast(msg, 'none')
        })
        .finally(() => {
          this.submitting = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.change-password-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  box-sizing: border-box;
}

.form-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.field-group {
  margin-bottom: 36rpx;
}

.field-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
}
.field-hint {
  display: block;
  font-size: 26rpx;
  color: #576b95;
  margin-bottom: 8rpx;
  line-height: 1.5;
}

.field-line {
  border-bottom: 2rpx solid #e0e0e0;
  padding-bottom: 12rpx;
}

.field-input {
  font-size: 28rpx;
  color: #333;
  width: 100%;
  height: 60rpx;
  line-height: 60rpx;
}

.password-hint {
  font-size: 24rpx;
  color: #999;
  line-height: 1.4;
  margin-top: -8rpx;
  margin-bottom: 24rpx;
}
.forgot-row {
  margin-bottom: 24rpx;
}
.forgot-link {
  font-size: 28rpx;
  color: #576b95;
}

.btn-wrap {
  margin-top: 50rpx;
  display: flex;
  justify-content: center;
}

.btn-update {
  min-width: 320rpx;
  width: auto;
  padding: 0 48rpx;
  height: 72rpx;
  line-height: 72rpx;
  text-align: center;
  font-size: 28rpx;
  color: #fff;
  background: #8D9F5E;
  border-radius: 36rpx;
  border: none;

  &.is-disabled {
    opacity: 0.5;
    background: #ccc;
  }
}

.modal-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60rpx;
  box-sizing: border-box;
}
.modal-box {
  width: 100%;
  max-width: 560rpx;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}
.modal-body {
  padding: 40rpx 30rpx 24rpx;
}
.modal-desc {
  display: block;
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}
.modal-actions {
  display: flex;
  border-top: 1rpx solid #eee;
}
.modal-btn {
  flex: 1;
  height: 96rpx;
  line-height: 96rpx;
  text-align: center;
  font-size: 32rpx;
}
.modal-btn.cancel {
  color: #666;
  border-right: 1rpx solid #eee;
}
.modal-btn.primary {
  color: #07c160;
  font-weight: 500;
}
</style>
