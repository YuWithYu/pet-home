<template>
  <view class="select-pet-type-container">
    <!-- 页面内容（使用默认导航栏） -->
    <view class="select-pet-content">
      <!-- 宠物类型选择区域（居中） -->
      <view class="pet-type-selection-container">
        <view class="pet-type-options">
          <view 
            class="pet-type-item" 
            :class="{ 'selected': selectedType === 'cat' }"
            @click="selectPetType('cat')"
          >
            <view class="pet-image-container">
              <image src="/static/images/cat.png" mode="aspectFit" class="pet-image" />
            </view>
            <view class="pet-name">
              <text>猫咪</text>
            </view>
          </view>

          <view 
            class="pet-type-item" 
            :class="{ 'selected': selectedType === 'dog' }"
            @click="selectPetType('dog')"
          >
            <view class="pet-image-container">
              <image src="/static/images/dog.png" mode="aspectFit" class="pet-image" />
            </view>
            <view class="pet-name">
              <text>狗狗</text>
            </view>
          </view>

          <view 
            class="pet-type-item" 
            :class="{ 'selected': selectedType === 'other' }"
            @click="selectPetType('other')"
          >
            <view class="pet-image-container">
              <image src="/static/images/other.png" mode="aspectFit" class="pet-image" />
            </view>
            <view class="pet-name">
              <text>其他</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 下一步按钮 -->
      <view class="next-button-section">
        <view 
          class="next-button" 
          :class="{ 'active': selectedType }"
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
  name: 'SelectPetType',
  data() {
    return {
      selectedType: 'cat' // 默认选择猫咪
    }
  },
  
  methods: {
    // 选择宠物类型
    selectPetType(type) {
      this.selectedType = type
    },
    
    // 下一步
    goToNext() {
      if (!this.selectedType) {
        uni.showToast({
          title: '请选择宠物类型',
          icon: 'none'
        })
        return
      }
      
      // 保存宠物类型到本地存储
      const basicInfo = uni.getStorageSync('petBasicInfo') || {}
      basicInfo.type = this.selectedType
      uni.setStorageSync('petBasicInfo', basicInfo)
      
      // 跳转到选择品种页面
      uni.navigateTo({
        url: '/user/select-pet-breed'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.select-pet-type-container {
  min-height: 100vh;
  background-color: #fff;
}

/* 页面内容 */
.select-pet-content {
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 宠物类型选择容器（居中） */
.pet-type-selection-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -200rpx; /* 向上移动，使选项更居中 */
}

.pet-type-options {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 40rpx;
  width: 100%;
  max-width: 600rpx;
}

.pet-type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  border-radius: 20rpx;
  transition: all 0.3s ease;
  cursor: pointer;
}

.pet-type-item.selected {
  background: rgba(255, 107, 53, 0.1);
  box-shadow: 0 4rpx 20rpx rgba(255, 107, 53, 0.2);
}

.pet-image-container {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 16rpx;
  border: 3rpx solid transparent;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.pet-type-item.selected .pet-image-container {
  border-color: #ff6b35;
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.3);
  background: rgba(255, 107, 53, 0.1);
}

.pet-image {
  width: 100%;
  height: 100%;
}

.pet-name {
  text-align: center;
}

.pet-name text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.pet-type-item.selected .pet-name text {
  color: #ff6b35;
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
