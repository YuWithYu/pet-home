<template>
  <view class="select-breed-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>选择品种</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="select-breed-content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
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
      statusBarHeight: 0,
      navBarHeight: 44,
      selectedBreed: ''
    }
  },
  
  computed: {
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight
    }
  },
  
  onLoad() {
    // 获取系统信息
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
        this.navBarHeight = res.platform === 'ios' ? 44 : 48
      }
    })
  },
  
  
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 跳转到品种列表页面
    showBreedList() {
      // 获取当前选择的宠物类型
      const basicInfo = uni.getStorageSync('petBasicInfo') || {}
      const petType = basicInfo.type || 'cat'
      
      uni.navigateTo({
        url: `/pages/user/select-breed-list?petType=${petType}`
      })
    },
    
    // 处理从品种列表页面选择的品种
    handleSelectedBreed(breed) {
      console.log('接收到选择的品种:', breed)
      this.selectedBreed = breed
      console.log('当前selectedBreed:', this.selectedBreed)
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
        url: '/pages/user/select-pet-gender'
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

/* 自定义白色导航栏 */
.custom-white-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
  border-bottom: 1rpx solid #f0f0f0;
}

.navbar-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
}

.navbar-left {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 40rpx;
  color: #333;
  font-weight: bold;
}

.navbar-title {
  flex: 1;
  text-align: center;
}

.navbar-title text {
  font-size: 32rpx;
  color: #333;
  font-weight: 400;
}

.navbar-right {
  width: 60rpx;
}

/* 页面内容 */
.select-breed-content {
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 88rpx);
}

/* 主要内容容器（居中） */
.main-content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
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
