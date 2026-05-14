<template>
  <view class="edit-username-container">
    <view class="edit-content">
      <view class="logo-wrap">
        <image class="logo-img" src="/static/images/宠物之家.png" mode="aspectFit" />
      </view>
      <view class="account-line">
        <text class="account-label">账号：</text>
        <text class="account-value">{{ currentUsername || '未设置' }}</text>
      </view>
    </view>
    <view class="btn-wrap">
      <button class="btn-change" @click="goModify">修改账号</button>
    </view>
  </view>
</template>

<script>
export default {
  name: 'EditUsername',
  data() {
    return {
      currentUsername: ''
    }
  },
  onLoad() {
    const userInfo = uni.getStorageSync('userInfo') || {}
    this.currentUsername = uni.getStorageSync('username') || userInfo.username || ''
  },
  methods: {
    goModify() {
      uni.navigateTo({ url: '/user/edit-username-verify' })
    }
  }
}
</script>

<style lang="scss" scoped>
.edit-username-container {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.edit-content {
  flex: 1;
  padding: 40rpx 32rpx 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.logo-wrap {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 32rpx;
}
.logo-img {
  width: 100%;
  height: 100%;
}
.account-line {
  margin-bottom: 12rpx;
}
.account-label, .account-value {
  font-size: 26rpx;
  color: #333;
}
.btn-wrap {
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}
.btn-change {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  color: #333;
  background-color: #ffd700;
  border-radius: 8rpx;
  border: none;
}
.btn-change::after { border: none; }
</style>
