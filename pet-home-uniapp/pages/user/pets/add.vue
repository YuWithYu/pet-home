<template>
  <view class="add-pet-container">
    <view class="form-container">
      <u-form :model="form" ref="form">
        <!-- 宠物头像 -->
        <view class="form-item">
          <view class="form-label">宠物头像</view>
          <view class="avatar-section" @click="chooseAvatar">
            <view class="avatar-wrapper">
              <image :src="form.avatar" mode="aspectFill" v-if="form.avatar" />
              <view class="avatar-placeholder" v-else>📷</view>
            </view>
            <view class="avatar-text">点击上传头像</view>
          </view>
        </view>

        <!-- 宠物姓名 -->
        <view class="form-item">
          <view class="form-label">宠物姓名</view>
          <input
            class="form-input"
            v-model="form.name"
            placeholder="请输入宠物姓名"
          />
        </view>

        <!-- 宠物类型 -->
        <view class="form-item">
          <view class="form-label">宠物类型</view>
          <view class="radio-group">
            <view
              :class="['radio-item', { 'active': form.type === 'dog' }]"
              @click="form.type = 'dog'"
            >
              狗狗
            </view>
            <view
              :class="['radio-item', { 'active': form.type === 'cat' }]"
              @click="form.type = 'cat'"
            >
              猫咪
            </view>
            <view
              :class="['radio-item', { 'active': form.type === 'other' }]"
              @click="form.type = 'other'"
            >
              其他
            </view>
          </view>
        </view>

        <!-- 宠物品种 -->
        <view class="form-item">
          <view class="form-label">宠物品种</view>
          <input
            class="form-input"
            v-model="form.breed"
            placeholder="请输入宠物品种"
          />
        </view>

        <!-- 宠物年龄 -->
        <view class="form-item">
          <view class="form-label">宠物年龄</view>
          <input
            class="form-input"
            type="number"
            v-model="form.age"
            placeholder="请输入年龄"
          />
        </view>

        <!-- 宠物体重 -->
        <view class="form-item">
          <view class="form-label">宠物体重</view>
          <input
            class="form-input"
            type="number"
            :step="0.1"
            v-model="form.weight"
            placeholder="请输入体重"
          />
        </view>

        <!-- 宠物性别 -->
        <view class="form-item">
          <view class="form-label">宠物性别</view>
          <view class="radio-group">
            <view
              :class="['radio-item', { 'active': form.gender === 'male' }]"
              @click="form.gender = 'male'"
            >
              公
            </view>
            <view
              :class="['radio-item', { 'active': form.gender === 'female' }]"
              @click="form.gender = 'female'"
            >
              母
            </view>
          </view>
        </view>

        <!-- 宠物描述 -->
        <view class="form-item">
          <view class="form-label">宠物描述</view>
          <textarea
            class="form-textarea"
            v-model="form.description"
            placeholder="请输入宠物描述"
            maxlength="200"
          />
        </view>

        <!-- 疫苗记录 -->
        <view class="form-item">
          <view class="form-label">疫苗记录</view>
          <textarea
            class="form-textarea"
            v-model="form.vaccinations"
            placeholder="请输入疫苗接种记录"
            maxlength="300"
          />
        </view>

        <!-- 健康状况 -->
        <view class="form-item">
          <view class="form-label">健康状况</view>
          <view class="radio-group">
            <view
              :class="['radio-item', { 'active': form.health === 'excellent' }]"
              @click="form.health = 'excellent'"
            >
              优秀
            </view>
            <view
              :class="['radio-item', { 'active': form.health === 'good' }]"
              @click="form.health = 'good'"
            >
              良好
            </view>
            <view
              :class="['radio-item', { 'active': form.health === 'fair' }]"
              @click="form.health = 'fair'"
            >
              一般
            </view>
            <view
              :class="['radio-item', { 'active': form.health === 'poor' }]"
              @click="form.health = 'poor'"
            >
              较差
            </view>
          </view>
        </view>

        <!-- 保存按钮 -->
        <view class="save-section">
          <button
          :class="['save-btn', { 'primary': canSave, 'disabled': !canSave }]"
          :disabled="!canSave"
          @click="savePet"
        >
          {{ saving ? '保存中...' : '保存宠物信息' }}
        </button>
        </view>
      </u-form>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AddPet',

  data() {
    return {
      saving: false,
      form: {
        avatar: '',
        name: '',
        type: 'dog',
        breed: '',
        age: 0,
        weight: 0,
        gender: 'male',
        description: '',
        vaccinations: '',
        health: 'good'
      }
    }
  },

  computed: {
    canSave() {
      return this.form.name.trim() !== '' &&
             this.form.breed.trim() !== '' &&
             this.form.age >= 0
    }
  },

  methods: {
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

    savePet() {
      if (!this.canSave) {
        uni.showToast({
          title: '请完善宠物信息',
          icon: 'none'
        })
        return
      }

      this.saving = true

      // 调用保存宠物API
      this.$api.createPet(this.form).then(() => {
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
.add-pet-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.form-container {
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
