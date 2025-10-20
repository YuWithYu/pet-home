<template>
  <view class="custom-pull-refresh" @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd">
    <!-- 下拉刷新提示区域 -->
    <view class="refresh-tip" :style="{ height: pullHeight + 'px' }">
      <view class="refresh-content" v-if="pullHeight > 0">
        <text class="refresh-text">{{ refreshText }}</text>
      </view>
    </view>
    
    <!-- 页面内容 -->
    <view class="page-content">
      <slot></slot>
    </view>
  </view>
</template>

<script>
export default {
  name: 'CustomPullRefresh',
  props: {
    // 刷新回调函数
    onRefresh: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      pullHeight: 0, // 下拉高度
      startY: 0, // 开始触摸的Y坐标
      isPulling: false, // 是否正在下拉
      isRefreshing: false, // 是否正在刷新
      refreshText: '下拉刷新', // 刷新提示文字
      maxPullHeight: 80 // 最大下拉高度
    }
  },
  methods: {
    onTouchStart(e) {
      if (this.isRefreshing) return
      
      this.startY = e.touches[0].clientY
      this.isPulling = true
      console.log('触摸开始', this.startY)
    },
    
    onTouchMove(e) {
      if (this.isRefreshing || !this.isPulling) return
      
      const currentY = e.touches[0].clientY
      const deltaY = currentY - this.startY
      
      console.log('触摸移动', deltaY, this.pullHeight)
      
      // 只有向下拉时才响应
      if (deltaY > 0) {
        // 计算下拉高度，添加阻尼效果
        const pullHeight = Math.min(deltaY * 0.6, this.maxPullHeight)
        this.pullHeight = pullHeight
        
        // 向父组件发送下拉事件
        this.$emit('pull', pullHeight)
        
        // 根据下拉高度显示不同文字
        if (pullHeight < 30) {
          this.refreshText = '下拉刷新'
        } else {
          this.refreshText = '释放更新'
        }
      }
    },
    
    onTouchEnd(e) {
      if (this.isRefreshing || !this.isPulling) return
      
      this.isPulling = false
      
      // 向父组件发送下拉结束事件
      this.$emit('pull-end')
      
      // 如果下拉高度超过阈值，触发刷新
      if (this.pullHeight >= 30) {
        this.startRefresh()
      } else {
        // 回弹
        this.resetPull()
      }
    },
    
    startRefresh() {
      this.isRefreshing = true
      this.refreshText = '加载中...'
      
      // 调用父组件的刷新方法
      if (this.onRefresh) {
        this.onRefresh().then(() => {
          this.refreshText = '加载成功'
          setTimeout(() => {
            this.resetPull()
          }, 1000)
        }).catch(() => {
          this.refreshText = '加载失败'
          setTimeout(() => {
            this.resetPull()
          }, 300)
        })
      } else {
        // 默认刷新逻辑
        setTimeout(() => {
          this.refreshText = '加载成功'
          setTimeout(() => {
            this.resetPull()
          }, 300)
        }, 800)
      }
    },
    
    resetPull() {
      this.pullHeight = 0
      this.isRefreshing = false
      this.refreshText = '下拉刷新'
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-pull-refresh {
  width: 100%;
  min-height: 100vh;
}

.refresh-tip {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(to right, #FF8C00 0%, #FFD700 100%);
  overflow: visible;
  transition: height 0.1s ease;
  min-height: 0;
  position: relative;
  z-index: 1000;
}

.refresh-content {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.refresh-text {
  color: #fff !important;
  font-size: 32rpx !important;
  font-weight: 600 !important;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
  white-space: nowrap;
  z-index: 1001;
  position: relative;
}

.page-content {
  width: 100%;
  min-height: 100vh;
}
</style>
