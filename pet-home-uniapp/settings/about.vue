<template>
  <view class="about-container">
    <view class="about-content">
      <view class="app-logo">
        <image src="/static/images/宠物之家.png" mode="aspectFit" class="logo-image" />
      </view>
      <view class="app-name">宠物家</view>
      <view class="app-version">版本 1.0.0</view>
      
      <view class="about-section">
        <view class="section-title">关于我们</view>
        <view class="section-text">
          宠物家是一个专注于宠物服务的综合性平台，致力于为宠物主人提供全方位的宠物生活服务。
          我们提供宠物医疗、美容、商品购买、社区交流等一站式服务，让您的宠物生活更加便捷和美好。
        </view>
      </view>
      
      <view class="about-section">
        <view class="section-title">我们的使命</view>
        <view class="section-text">
          让每一只宠物都能得到最好的照顾，让每一位宠物主人都能享受到便捷的服务。
          我们相信，宠物不仅是我们的伙伴，更是家庭的一员。
        </view>
      </view>
      
      <view class="about-section">
        <view class="section-title">联系我们</view>
        <view class="contact-info">
          <view class="info-item">
            <text class="info-label">官方网站：</text>
            <text class="info-value">https://situationship.icu</text>
          </view>
          <view class="info-item">
            <text class="info-label">客服邮箱：</text>
            <text class="info-value">csiyu8596@gmail.com</text>
          </view>
          <view class="info-item">
            <text class="info-label">客服电话：</text>
            <text class="info-value">15278562765</text>
          </view>
        </view>
      </view>
      
      <view class="copyright">
        <text>© 2026 宠物家 版权所有</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.about-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.about-content {
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.app-logo {
  width: 88rpx;
  height: 88rpx;
  margin-bottom: 12rpx;
}

.logo-image {
  width: 100%;
  height: 100%;
}

.app-name {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 6rpx;
}

.app-version {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 36rpx;
}

.about-section {
  width: 100%;
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 12rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 12rpx;
}

.section-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.contact-info {
  margin-top: 6rpx;
}

.info-item {
  display: flex;
  margin-bottom: 10rpx;
  font-size: 26rpx;
}

.info-label {
  color: #666;
  min-width: 140rpx;
}

.info-value {
  color: #333;
  flex: 1;
}

.copyright {
  margin-top: 24rpx;
  font-size: 22rpx;
  color: #999;
  text-align: center;
}
</style>
