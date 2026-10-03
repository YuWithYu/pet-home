<template>
  <view class="select-sterilization-container">
    <!-- 页面内容（使用默认导航栏） -->
    <view class="select-sterilization-content">
      <!-- 主要内容容器（居中） -->
      <view class="main-content-container">
        <!-- 绝育状态选择区域 -->
        <view class="sterilization-selection-container">
          <view class="sterilization-options">
            <!-- 已绝育 -->
            <view 
              class="sterilization-item" 
              :class="{ 'selected': selectedSterilization === 'sterilized' }"
              @click="selectSterilization('sterilized')"
            >
              <view class="sterilization-icon">
                <text class="sterilization-symbol">✓</text>
              </view>
              <view class="sterilization-label">
                <text>已绝育</text>
              </view>
            </view>

            <!-- 未绝育 -->
            <view 
              class="sterilization-item" 
              :class="{ 'selected': selectedSterilization === 'not-sterilized' }"
              @click="selectSterilization('not-sterilized')"
            >
              <view class="sterilization-icon">
                <text class="sterilization-symbol">✗</text>
              </view>
              <view class="sterilization-label">
                <text>未绝育</text>
              </view>
            </view>

            <!-- 不清楚 -->
            <view 
              class="sterilization-item" 
              :class="{ 'selected': selectedSterilization === 'unknown' }"
              @click="selectSterilization('unknown')"
            >
              <view class="sterilization-icon">
                <text class="sterilization-symbol">?</text>
              </view>
              <view class="sterilization-label">
                <text>不清楚</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 下一步按钮 -->
      <view class="next-button-section">
        <view 
          class="next-button" 
          :class="{ 'active': selectedSterilization }"
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
  name: 'SelectSterilization',
  data() {
    return {
      selectedSterilization: ''
    }
  },
  
  methods: {
    // 选择绝育状态
    selectSterilization(status) {
      this.selectedSterilization = status
    },
    
    // 下一步
    goToNext() {
      if (!this.selectedSterilization) {
        uni.showToast({
          title: '请选择绝育状态',
          icon: 'none'
        })
        return
      }
      
      // 保存绝育状态信息到本地存储
      const basicInfo = uni.getStorageSync('petBasicInfo') || {}
      basicInfo.sterilization = this.selectedSterilization
      uni.setStorageSync('petBasicInfo', basicInfo)
      
      // 跳转到选择体重页面（已移除出生日期步骤）
      uni.navigateTo({
        url: '/user/select-weight'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.select-sterilization-container {
  min-height: 100vh;
  background-color: #fff;
}

/* 页面内容 */
.select-sterilization-content {
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

/* 绝育状态选择容器（居中） */
.sterilization-selection-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.sterilization-options {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 60rpx;
}

.sterilization-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.sterilization-item.selected .sterilization-icon {
  transform: scale(1.1);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
  background: #ffd700;
}

.sterilization-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
  transition: all 0.3s ease;
}

.sterilization-symbol {
  font-size: 60rpx;
  color: #fff;
  font-weight: bold;
}

.sterilization-label {
  text-align: center;
}

.sterilization-label text {
  font-size: 26rpx;
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
