<template>
  <view class="select-gender-container">
    <!-- 页面内容（使用默认导航栏） -->
    <view class="select-gender-content">
      <!-- 主要内容容器（居中） -->
      <view class="main-content-container">
        <!-- 性别选择区域 -->
        <view class="gender-selection-container">
          <view class="gender-options">
            <!-- 男孩子 -->
            <view 
              class="gender-item" 
              :class="{ 'selected': selectedGender === 'male' }"
              @click="selectGender('male')"
            >
              <view class="gender-icon male-icon">
                <text class="gender-symbol">♂</text>
              </view>
              <view class="gender-label">
                <text>男孩子</text>
              </view>
            </view>

            <!-- 女孩子 -->
            <view 
              class="gender-item" 
              :class="{ 'selected': selectedGender === 'female' }"
              @click="selectGender('female')"
            >
              <view class="gender-icon female-icon">
                <text class="gender-symbol">♀</text>
              </view>
              <view class="gender-label">
                <text>女孩子</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 下一步按钮 -->
      <view class="next-button-section">
        <view 
          class="next-button" 
          :class="{ 'active': selectedGender }"
          @click="goToNext"
        >
          <text>下一步</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'SelectPetGender',
  data() {
    return {
      selectedGender: ''
    }
  },
  
  methods: {
    // 选择性别
    selectGender(gender) {
      this.selectedGender = gender
    },
    
    // 下一步
    goToNext() {
      if (!this.selectedGender) {
        uni.showToast({
          title: '请选择性别',
          icon: 'none'
        })
        return
      }
      
      // 保存性别信息到本地存储
      const basicInfo = uni.getStorageSync('petBasicInfo') || {}
      basicInfo.gender = this.selectedGender
      uni.setStorageSync('petBasicInfo', basicInfo)
      
      // 跳转到选择绝育状态页面
      uni.navigateTo({
        url: '/user/select-sterilization'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.select-gender-container {
  min-height: 100vh;
  background-color: #fff;
}

/* 页面内容 */
.select-gender-content {
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 主要内容容器（居中） */
.main-content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-top: -200rpx; /* 向上移动，使选项更居中 */
}

/* 性别选择容器（居中） */
.gender-selection-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.gender-options {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 80rpx;
}

.gender-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.gender-item.selected .gender-icon {
  transform: scale(1.1);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
}

.gender-icon {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
  transition: all 0.3s ease;
}

.male-icon {
  background: #42a5f5;
}

.female-icon {
  background: #ec407a;
}

.gender-symbol {
  font-size: 80rpx;
  color: #fff;
  font-weight: bold;
}

.gender-label {
  text-align: center;
}

.gender-label text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 下一步按钮 */
.next-button-section {
  position: fixed;
  bottom: 40rpx;
  left: 30rpx;
  right: 30rpx;
}

.next-button {
  width: 100%;
  height: 88rpx;
  background: #e0e0e0;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.next-button.active {
  background: #ffd700;
}

.next-button text {
  font-size: 32rpx;
  color: #999;
  font-weight: 500;
}

.next-button.active text {
  color: #333;
  font-weight: bold;
}

.next-button:active {
  transform: scale(0.98);
}
</style>
