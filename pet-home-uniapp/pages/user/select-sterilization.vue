<template>
  <view class="select-sterilization-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>选择绝育状态</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="select-sterilization-content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
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
      statusBarHeight: 0,
      navBarHeight: 44,
      selectedSterilization: ''
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
      
      // 跳转到选择出生日期页面
      uni.navigateTo({
        url: '/pages/user/select-birth-date'
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
.select-sterilization-content {
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
