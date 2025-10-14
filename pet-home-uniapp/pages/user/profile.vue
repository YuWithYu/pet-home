<template>
  <view class="profile-container">
    <view class="profile-form">
      <u-form :model="form" ref="form">
        <!-- 头像 -->
        <view class="form-item">
          <view class="form-label">头像</view>
          <view class="avatar-section" @click="chooseAvatar">
            <view class="avatar-wrapper">
              <image :src="form.avatar" mode="aspectFill" v-if="form.avatar" />
              <view class="avatar-placeholder" v-else>👤</view>
            </view>
            <view class="avatar-text">点击更换头像</view>
          </view>
        </view>

        <!-- 昵称 -->
        <view class="form-item">
          <view class="form-label">昵称</view>
          <input
            class="form-input"
            v-model="form.nickname"
            placeholder="请输入昵称"
          />
        </view>

        <!-- 性别 -->
        <view class="form-item">
          <view class="form-label">性别</view>
          <view class="radio-group">
            <view
              :class="['radio-item', { 'active': form.gender === '0' }]"
              @click="form.gender = '0'"
            >
              男
            </view>
            <view
              :class="['radio-item', { 'active': form.gender === '1' }]"
              @click="form.gender = '1'"
            >
              女
            </view>
            <view
              :class="['radio-item', { 'active': form.gender === '2' }]"
              @click="form.gender = '2'"
            >
              其他
            </view>
          </view>
        </view>

        <!-- 生日 -->
        <view class="form-item">
          <view class="form-label">生日</view>
          <picker mode="date" :value="form.birthday" @change="onBirthdayChange">
            <view class="picker-input">{{ form.birthday || '请选择生日' }}</view>
          </picker>
        </view>

        <!-- 地区 -->
        <view class="form-item">
          <view class="form-label">地区</view>
          <picker mode="region" :value="regionValue" @change="onRegionChange">
            <view class="picker-input">{{ form.region || '请选择地区' }}</view>
          </picker>
        </view>

        <!-- 个性签名 -->
        <view class="form-item">
          <view class="form-label">个性签名</view>
          <textarea
            class="form-textarea"
            v-model="form.signature"
            placeholder="请输入个性签名"
            maxlength="100"
          />
        </view>

        <!-- 保存按钮 -->
        <view class="save-section">
          <button
          :class="['save-btn', { 'primary': canSave, 'disabled': !canSave }]"
          :disabled="!canSave"
          @click="saveProfile"
        >
          {{ saving ? '保存中...' : '保存资料' }}
        </button>
        </view>
      </u-form>
    </view>

  </view>
</template>

<script>
export default {
  name: 'Profile',

  data() {
    return {
      saving: false,
      regionValue: [],
      form: {
        avatar: '',
        nickname: '',
        gender: '0',
        birthday: '',
        region: '',
        signature: ''
      }
    }
  },

  computed: {
    canSave() {
      return this.form.nickname.trim() !== ''
    }
  },

  onLoad() {
    this.loadProfile()
  },

  methods: {
    loadProfile() {
      // 从本地存储或API获取用户信息
      const userInfo = uni.getStorageSync('userInfo') || {}
      this.form = { ...this.form, ...userInfo }
    },

    chooseAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.form.avatar = res.tempFilePaths[0]
        }
      })
    },

    onBirthdayChange(e) {
      this.form.birthday = e.detail.value
    },

    onRegionChange(e) {
      this.regionValue = e.detail.value
      this.form.region = e.detail.value.join(' ')
    },

    saveProfile() {
      if (!this.canSave) {
        uni.showToast({
          title: '请输入昵称',
          icon: 'none'
        })
        return
      }

      this.saving = true

      // 调用保存API
      this.$api.updateProfile(this.form).then(() => {
        uni.showToast({
          title: '保存成功',
          icon: 'success'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }).catch(() => {
        uni.showToast({
          title: '保存失败',
          icon: 'none'
        })
      }).finally(() => {
        this.saving = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.profile-form {
  background-color: white;
  margin: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.1);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 30rpx;
  padding: 20rpx 0;

  .avatar-wrapper {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    overflow: hidden;
    background-color: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;

    image {
      width: 100%;
      height: 100%;
    }

    .avatar-placeholder {
      font-size: 48rpx;
      color: #999;
    }
  }

  .avatar-text {
    font-size: 28rpx;
    color: #666;
  }
}

.save-section {
  margin-top: 60rpx;
  padding: 0 40rpx;
}

.form-label {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.form-input {
  width: 100%;
  padding: 24rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;

  &:focus {
    border-color: #ff6b35;
    outline: none;
  }
}

.radio-group {
  display: flex;
  gap: 30rpx;
  flex-wrap: wrap;
}

.radio-item {
  padding: 16rpx 32rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #666;
  text-align: center;
  min-width: 120rpx;

  &.active {
    border-color: #ff6b35;
    color: #ff6b35;
    background-color: #fff7e6;
  }
}

.picker-input {
  padding: 24rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #666;
  background-color: white;

  &:active {
    border-color: #ff6b35;
  }
}

.form-textarea {
  width: 100%;
  min-height: 150rpx;
  padding: 20rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
  resize: vertical;
}

.save-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;

  &.primary {
    background-color: #ff6b35;
    color: white;
  }

  &.disabled {
    background-color: #f8f8f8;
    color: #999;
  }
}
</style>