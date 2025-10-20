<template>
  <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
    <!-- 渐变背景 -->
    <view class="navbar-content">
      <!-- 左侧返回按钮（如果有） -->
      <view class="navbar-left" v-if="showBack" @click="handleBack">
        <text class="back-icon">◀</text>
      </view>
      <view class="navbar-left" v-else></view>
      
      <!-- 中间标题 -->
      <view class="navbar-title" v-if="title">
        <text>{{ title }}</text>
      </view>
      
      <!-- 右侧插槽 -->
      <view class="navbar-right">
        <slot name="right"></slot>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'CustomNavbar',
  props: {
    // 导航栏标题
    title: {
      type: String,
      default: '标题'
    },
    // 是否显示返回按钮
    showBack: {
      type: Boolean,
      default: false
    },
    // 返回按钮点击事件
    onBack: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44 // 导航栏高度（不含状态栏）
    }
  },
  mounted() {
    // 获取系统信息
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
        
        // 根据平台设置导航栏高度
        if (res.platform === 'ios') {
          this.navBarHeight = 44
        } else {
          this.navBarHeight = 48
        }
      }
    })
  },
  methods: {
    handleBack() {
      if (this.onBack) {
        this.onBack()
      } else {
        uni.navigateBack({
          delta: 1
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-navbar {
  width: 100%;
  background: linear-gradient(to right, #FF8C00 0%, #FFD700 100%);
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;
  box-shadow: none; /* 移除阴影，避免白边 */
}

.navbar-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  position: relative;
}

.navbar-left {
  width: 100rpx;
  display: flex;
  align-items: center;
  
  .back-icon {
    color: #fff;
    font-size: 32rpx;
    font-weight: bold;
    padding: 10rpx;
  }
}

.navbar-title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  
  text {
    color: #fff;
    font-size: 36rpx;
    font-weight: bold;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
  }
}

.navbar-right {
  width: 100rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style>

