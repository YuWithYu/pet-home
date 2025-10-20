<template>
  <view class="edit-nickname-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>编辑昵称</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>
    
    <!-- 编辑昵称内容 -->
    <view class="edit-content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      
      <!-- 提示文字 -->
      <view class="hint-text">4-20个字符,起个响亮的名字~</view>
      
      <!-- 输入框和保存按钮 -->
      <view class="input-section">
        <input 
          class="nickname-input" 
          v-model="nickname" 
          placeholder="请输入昵称"
          maxlength="20"
          @input="onInput"
        />
        <button 
          class="save-btn" 
          :class="{ 'active': canSave, 'disabled': !canSave }"
          :disabled="!canSave"
          @click="saveNickname"
        >
          保存
        </button>
      </view>
      
      <!-- 推荐昵称 -->
      <view class="recommend-section">
        <view class="recommend-header">
          <text class="recommend-title">试试推荐昵称:</text>
          <view class="refresh-btn" @click="refreshRecommendations">
            <text class="refresh-icon">↻</text>
            <text class="refresh-text">换一批</text>
          </view>
        </view>
        
        <view class="recommend-list">
          <view 
            class="recommend-item" 
            v-for="(item, index) in recommendations" 
            :key="index"
            @click="selectRecommendation(item)"
          >
            {{ item }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'EditNickname',
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      nickname: '',
      recommendations: [
        '谷梁秋天',
        '虚心的画笔', 
        '第五煎饼',
        '言福老师'
      ],
      recommendPool: [
        '谷梁秋天', '虚心的画笔', '第五煎饼', '言福老师',
        '聪明的猫咪', '快乐的小狗', '温柔的小熊', '活泼的兔子',
        '阳光少年', '月亮公主', '星星守护者', '彩虹使者',
        '梦想家', '旅行者', '美食家', '音乐家'
      ]
    }
  },
  computed: {
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight
    },
    canSave() {
      return this.nickname.trim().length >= 4 && this.nickname.trim().length <= 20
    }
  },
  onLoad() {
    // 获取系统信息
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight
      }
    })
    
    // 获取当前昵称
    const userInfo = uni.getStorageSync('userInfo') || {}
    this.nickname = userInfo.nickname || ''
  },
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 输入处理
    onInput(e) {
      this.nickname = e.detail.value
    },

    // 保存昵称
    saveNickname() {
      if (!this.canSave) return
      
      const token = uni.getStorageSync('token')
      if (!token) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }
      
      // 显示加载提示
      uni.showLoading({
        title: '保存中...',
        mask: true
      })
      
      // 调用后端API更新昵称
      api.updateUser({
        token: token,
        nickname: this.nickname.trim()
      }).then(res => {
        if (res.code === 0) {
          // 保存到本地存储
          const userInfo = uni.getStorageSync('userInfo') || {}
          userInfo.nickname = this.nickname.trim()
          uni.setStorageSync('userInfo', userInfo)
          
          uni.showToast({
            title: '昵称保存成功',
            icon: 'success'
          })
          
          // 延迟返回
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.msg || '保存失败',
            icon: 'none'
          })
        }
      }).catch(err => {
        console.error('保存昵称失败:', err)
        uni.showToast({
          title: '保存失败，请重试',
          icon: 'none'
        })
      }).finally(() => {
        uni.hideLoading()
      })
    },

    // 选择推荐昵称
    selectRecommendation(nickname) {
      this.nickname = nickname
    },

    // 刷新推荐昵称
    refreshRecommendations() {
      // 随机选择4个不同的推荐昵称
      const shuffled = [...this.recommendPool].sort(() => 0.5 - Math.random())
      this.recommendations = shuffled.slice(0, 4)
    }
  }
}
</script>

<style lang="scss" scoped>
.edit-nickname-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 自定义白色导航栏 */
.custom-white-navbar {
  width: 100%;
  height: 88rpx;
  background: #fff;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 99;
  border-bottom: 1rpx solid #f0f0f0;
}

.navbar-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
}

.navbar-left {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 48rpx;
  color: #333;
  font-weight: 300;
}

.navbar-title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.navbar-title text {
  font-size: 32rpx;
  color: #333;
  font-weight: 400;
}

.navbar-right {
  width: 80rpx;
  height: 60rpx;
}

/* 编辑内容 */
.edit-content {
  padding: 40rpx 30rpx;
}

.hint-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.input-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 60rpx;
}

.nickname-input {
  flex: 1;
  height: 80rpx;
  padding: 0 20rpx;
  background: #fff;
  border: 2rpx solid #e0e0e0;
  border-radius: 12rpx;
  font-size: 32rpx;
  color: #333;
}

.nickname-input:focus {
  border-color: #ff6b35;
}

.save-btn {
  width: 120rpx;
  height: 80rpx;
  border-radius: 12rpx;
  border: none;
  font-size: 28rpx;
  font-weight: 500;
  transition: all 0.3s ease;
}

.save-btn.active {
  background: #ff6b35;
  color: #fff;
}

.save-btn.disabled {
  background: #f0f0f0;
  color: #999;
}

.recommend-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.recommend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
}

.recommend-title {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.refresh-icon {
  font-size: 24rpx;
  color: #999;
}

.refresh-text {
  font-size: 24rpx;
  color: #999;
}

.recommend-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.recommend-item {
  padding: 16rpx 24rpx;
  background: #f8f8f8;
  border-radius: 20rpx;
  font-size: 28rpx;
  color: #666;
  transition: all 0.2s ease;
}

.recommend-item:active {
  background: #e8e8e8;
  transform: scale(0.98);
}
</style>
