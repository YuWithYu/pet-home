<template>
  <view class="select-breed-container">
    <!-- 页面内容（使用默认导航栏） -->
    <view class="select-breed-content">
      <!-- 主要内容容器（居中） -->
      <view class="main-content-container">
        <!-- 标题 -->
        <view class="title-section">
          <text class="main-title">选择品种</text>
        </view>

        <!-- 品种选择输入框 -->
        <view class="breed-input-section">
          <view class="input-container" @click="showBreedList">
            <text class="input-text" :class="{ 'placeholder': !selectedBreed }">
              {{ selectedBreed || '请选择品种' }}
            </text>
            <text class="arrow-icon">></text>
          </view>
        </view>

      </view>

      <!-- 下一步按钮 -->
      <view class="next-button-section">
        <view 
          class="next-button" 
          :class="{ 'active': selectedBreed }"
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
  name: 'SelectPetBreed',
  data() {
    return {
      selectedBreed: ''
    }
  },
  
  methods: {
    // 跳转到品种列表页面
    showBreedList() {
      // 获取当前选择的宠物类型
      const basicInfo = uni.getStorageSync('petBasicInfo') || {}
      const petType = basicInfo.type || 'cat'
      
      uni.navigateTo({
        url: `/user/select-breed-list?petType=${petType}`
      })
    },
    
    // 处理从品种列表页面选择的品种
    handleSelectedBreed(breed) {
      this.selectedBreed = breed
    },
    
    // 下一步
    goToNext() {
      if (!this.selectedBreed) {
        uni.showToast({
          title: '请选择品种',
          icon: 'none'
        })
        return
      }
      
      // 保存品种信息到本地存储
      const basicInfo = uni.getStorageSync('petBasicInfo') || {}
      basicInfo.breed = this.selectedBreed
      uni.setStorageSync('petBasicInfo', basicInfo)
      
      // 跳转到选择性别页面
      uni.navigateTo({
        url: '/user/select-pet-gender'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.select-breed-container {
  min-height: 100vh;
  background-color: #fff;
}

/* 页面内容 */
.select-breed-content {
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

/* 标题区域 */
.title-section {
  margin-bottom: 60rpx;
}

.main-title {
  font-size: 36rpx;
  color: #333;
  font-weight: bold;
}

/* 品种选择输入框 */
.breed-input-section {
  width: 100%;
  max-width: 600rpx;
  margin-bottom: 40rpx;
}

.input-container {
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background-color 0.2s ease;
}

.input-container:active {
  background: #eeeeee;
}

.input-text {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.input-text.placeholder {
  color: #999;
}

.arrow-icon {
  font-size: 24rpx;
  color: #999;
  margin-left: 20rpx;
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
