<template>
  <view class="new-container">
    <view class="new-content">
      <text class="desc">账号须以字母或下划线开头，6-20位，仅限字母、数字、下划线、减号。建议避免包含姓名、生日等涉及个人隐私的信息。</text>
      <view class="field-wrap">
        <text class="field-label">新的账号</text>
        <input
          class="field-input"
          v-model="newUsername"
          placeholder="请输入新账号"
          maxlength="20"
          @input="onInput"
        />
      </view>
    </view>
    <view class="btn-wrap">
      <button
        class="btn-confirm"
        :class="{ active: canSubmit }"
        :disabled="!canSubmit"
        @click="confirm"
      >
        确认
      </button>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

const USERNAME_REG = /^[a-zA-Z_][a-zA-Z0-9_-]{5,19}$/

export default {
  name: 'EditUsernameNew',
  data() {
    return {
      newUsername: ''
    }
  },
  computed: {
    canSubmit() {
      const v = (this.newUsername || '').trim()
      return USERNAME_REG.test(v)
    }
  },
  methods: {
    onInput(e) {
      this.newUsername = e.detail.value
    },
    confirm() {
      if (!this.canSubmit) {
        uni.showToast({ title: '账号须以字母或下划线开头，6-20位', icon: 'none' })
        return
      }
      const v = this.newUsername.trim()
      uni.showLoading({ title: '提交中...', mask: true })
      api.changeUsername(v)
        .then(res => {
          uni.hideLoading()
          if (res.code === 200 || res.code === 0) {
            const userInfo = uni.getStorageSync('userInfo') || {}
            userInfo.username = v
            uni.setStorageSync('userInfo', userInfo)
            uni.setStorageSync('username', v)
            uni.showToast({
              title: '修改成功',
              icon: 'success',
              duration: 3000
            })
            setTimeout(() => {
              uni.redirectTo({ url: '/user/edit-profile' })
            }, 3000)
          } else {
            uni.showToast({ title: res.msg || '修改失败', icon: 'none' })
          }
        })
        .catch(err => {
          uni.hideLoading()
          uni.showToast({ title: (err && (err.msg || err.message)) || '修改失败', icon: 'none' })
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.new-container {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.new-content {
  flex: 1;
  padding: 32rpx 24rpx 24rpx;
}
.desc {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-bottom: 24rpx;
  line-height: 1.4;
}
.field-wrap {
  margin-bottom: 0;
}
.field-label {
  display: block;
  font-size: 24rpx;
  color: #333;
  margin-bottom: 12rpx;
}
.field-input {
  width: 100%;
  height: 72rpx;
  font-size: 26rpx;
  color: #333;
  border-bottom: 1rpx solid #e5e5e5;
}
.btn-wrap {
  padding: 24rpx 24rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}
.btn-confirm {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  color: #333;
  background-color: rgba(255, 215, 0, 0.5);
  border-radius: 8rpx;
  border: none;
}
.btn-confirm.active {
  background-color: #ffd700;
  color: #333;
}
.btn-confirm::after { border: none; }
</style>
