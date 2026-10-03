<template>
  <view
    class="custom-navbar"
    :class="themeClass"
    :style="{ paddingTop: statusBarHeight + 'px' }"
  >
    <!-- 渐变背景 -->
    <view class="navbar-content">
      <!-- 左侧返回按钮（如果有） -->
      <view class="navbar-left" v-if="showBack" @click="handleBack">
        <text class="back-icon">◀</text>
      </view>
      <view class="navbar-left" v-else></view>
      
      <!-- 中间标题（仅非空字符串时显示，避免误传 boolean 显示 "true"/"false"） -->
      <view class="navbar-title" v-if="titleString">
        <text class="navbar-title-text">{{ titleString }}</text>
      </view>
      
      <!-- 右侧插槽（使用默认插槽避免小程序下 named slot 编译问题） -->
      <view class="navbar-right">
        <slot></slot>
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
    },
    // 主题：gradient 或 white
    theme: {
      type: String,
      default: 'gradient'
    }
  },
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44 // 导航栏高度（不含状态栏）
    }
  },
  computed: {
    themeClass() {
      return this.theme === 'white' ? 'theme-white' : 'theme-gradient'
    },
    // 只显示有效标题，布尔、null、空、"true"/"false" 一律不显示
    titleString() {
      if (this.title == null || this.title === undefined) return ''
      if (typeof this.title === 'boolean') return ''
      const s = String(this.title).trim()
      if (s === 'true' || s === 'false' || s === '') return ''
      return s
    }
  },
  mounted() {
    // 使用 getWindowInfo/getDeviceInfo 替代已废弃的 getSystemInfo
    try {
      if (typeof uni.getWindowInfo === 'function') {
        const win = uni.getWindowInfo()
        this.statusBarHeight = win.statusBarHeight || 0
      }
      if (typeof uni.getDeviceInfo === 'function') {
        const dev = uni.getDeviceInfo()
        this.navBarHeight = dev.platform === 'ios' ? 44 : 48
      } else if (typeof uni.getSystemInfoSync === 'function') {
        const sys = uni.getSystemInfoSync()
        this.statusBarHeight = this.statusBarHeight || sys.statusBarHeight || 0
        this.navBarHeight = sys.platform === 'ios' ? 44 : 48
      }
    } catch (e) {
      if (typeof uni.getSystemInfo === 'function') {
        uni.getSystemInfo({
          success: (res) => {
            this.statusBarHeight = res.statusBarHeight || 0
            this.navBarHeight = res.platform === 'ios' ? 44 : 48
          }
        })
      }
    }
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
  position: fixed;
  top: 0;
  left: 0;
  z-index: 9999;
  box-shadow: none;
}

.theme-gradient {
  background: linear-gradient(to right, #FF8C00 0%, #FFD700 100%);
}

.theme-white {
  background: #ffffff;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
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
    font-size: 26rpx;
    font-weight: bold;
    padding: 10rpx;
  }
}

.navbar-title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .navbar-title-text {
    font-size: 28rpx;
    font-weight: 500;
    text-shadow: none;
  }
}

.navbar-right {
  width: 100rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.theme-gradient .back-icon,
.theme-gradient .navbar-title .navbar-title-text,
.theme-gradient .navbar-right {
  color: #fff;
}

.theme-white .back-icon,
.theme-white .navbar-title .navbar-title-text,
.theme-white .navbar-right {
  color: #333;
}
</style>

