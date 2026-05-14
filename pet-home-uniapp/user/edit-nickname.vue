<template>
  <view class="edit-nickname-container">
    <!-- 使用默认导航栏，内容区域适当内边距 -->
    <view class="edit-content">
      <!-- 提示文字 -->
      <view class="hint-text">4-20个字符,起个响亮的名字~</view>

      <!-- 输入框和保存按钮 -->
      <view class="input-section">
        <input
          class="nickname-input"
          v-model="nickname"
          placeholder="请输入昵称"
          maxlength="20"
          @input="onInput"
        />
        <button
          class="save-btn"
          :class="{ active: canSave, disabled: !canSave }"
          :disabled="!canSave"
          @click="saveNickname"
        >
          保存
        </button>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'EditNickname',
  data() {
    return {
      nickname: '',
      saving: false
    }
  },
  computed: {
    canSave() {
      const value = this.nickname ? this.nickname.trim() : ''
      return value.length >= 4 && value.length <= 20
    }
  },
  onLoad(options) {
    // 优先使用路由参数中的昵称，其次使用本地存储中的昵称
    if (options && options.nickname) {
      try {
        this.nickname = decodeURIComponent(options.nickname)
      } catch (e) {
        this.nickname = options.nickname
      }
    } else {
      const userInfo = uni.getStorageSync('userInfo') || {}
      this.nickname = userInfo.nickname || ''
    }
  },
  methods: {
    // 输入处理
    onInput(e) {
      this.nickname = e.detail.value
    },

    // 保存昵称
    async saveNickname() {
      if (!this.canSave || this.saving) return

      const token = uni.getStorageSync('token')
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      const currentUserId = uni.getStorageSync('userId') || staffInfo.adminId
      if (!token || !currentUserId) {
        uni.showToast({
          title: '用户信息获取失败，请重新登录',
          icon: 'none'
        })
        return
      }

      this.saving = true
      uni.showLoading({
        title: '保存中...',
        mask: true
      })

      const userId = parseInt(currentUserId)
      const nickname = this.nickname.trim()

      try {
        // 调用后端API更新昵称（与编辑资料页保持一致，带上用户ID）
        const saveReq = api.updateUser({
          id: userId,
          userId: userId,
          nickname
        })
        const timeoutReq = new Promise((_, reject) => {
          setTimeout(() => reject(new Error('保存超时，请稍后重试')), 15000)
        })
        await Promise.race([saveReq, timeoutReq])

        // 保存到本地存储，供其他页面使用
        const userInfo = uni.getStorageSync('userInfo') || {}
        userInfo.nickname = nickname
        uni.setStorageSync('userInfo', userInfo)
        // 同时写入一个临时值，方便编辑资料页 onShow 时刷新
        uni.setStorageSync('editedNickname', nickname)

        uni.showToast({
          title: '昵称保存成功',
          icon: 'success'
        })

        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      } catch (err) {
        console.error('保存昵称失败:', err)
        const rawMsg = (err && (err.message || err.errMsg)) ? String(err.message || err.errMsg) : ''
        const duplicate = /已存在|已被使用|重复|duplicate|exists/i.test(rawMsg)
        uni.showToast({
          title: duplicate ? '该昵称已被使用，请换一个' : (rawMsg || '保存失败，请重试'),
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
        this.saving = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.edit-nickname-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 编辑内容，整体稍微缩小一点 */
.edit-content {
  padding: 30rpx 24rpx;
}

.hint-text {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 30rpx;
}

.input-section {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.nickname-input {
  flex: 1;
  height: 72rpx;
  padding: 0 18rpx;
  background: #fff;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 30rpx;
  color: #333;
}

.nickname-input:focus {
  border-color: #ff6b35;
}

.save-btn {
  width: 110rpx;
  height: 72rpx;
  border-radius: 12rpx;
  border: none;
  font-size: 26rpx;
  font-weight: 500;
  transition: all 0.3s ease;
}

.save-btn.active {
  background: #ff6b35;
  color: #fff;
}

.save-btn.disabled {
  background: #f0f0f0;
  color: #999;
}
</style>
