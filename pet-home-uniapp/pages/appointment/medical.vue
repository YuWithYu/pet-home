<template>
  <view class="medical-container">
    <view class="service-header">
      <view class="service-icon">🏥</view>
      <view class="service-title">宠物医疗中心</view>
      <view class="service-desc">本中心承诺</view>
      <view class="service-promise">
        专注于宠物的健康，凭着一切让宠物远离病痛、远离苦恼的信念，打造综合医疗技术亮点。
        <text class="website">www.houjiuhospital.com</text>
      </view>
    </view>

    <view class="services-container">
      <view class="section-title">医疗服务</view>

      <view class="services-grid">
        <view class="service-card" v-for="(service, index) in services" :key="index">
          <view class="service-image">
            <image :src="service.image" mode="aspectFill" />
          </view>
          <view class="service-title">{{ service.title }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Medical',

  data() {
    return {
      services: [
        {
          title: '狗狗皮肤清洗服务',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '猫咪皮肤清洗服务',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '狗狗洗澡',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '猫咪洗澡',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '美容SPA',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '非麻洁牙',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        }
      ]
    }
  },

  methods: {
    onServiceTap(service) {
      uni.showToast({
        title: '服务详情功能开发中',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.medical-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.service-header {
  background: linear-gradient(135deg, #4FC3F7 0%, #29B6F6 100%);
  padding: 60rpx 40rpx;
  text-align: center;
  color: white;
  position: relative;

  .service-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }

  .service-title {
    font-size: 48rpx;
    font-weight: bold;
    margin-bottom: 16rpx;
  }

  .service-desc {
    font-size: 28rpx;
    opacity: 0.9;
    margin-bottom: 20rpx;
  }

  .service-promise {
    font-size: 24rpx;
    line-height: 1.4;
    opacity: 0.9;

    .website {
      display: block;
      margin-top: 10rpx;
      font-weight: bold;
    }
  }
}

.services-container {
  padding: 40rpx;

  .section-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
    margin-bottom: 30rpx;
    text-align: left;
  }

  .services-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20rpx;
  }

  .service-card {
    background-color: white;
    border-radius: 16rpx;
    padding: 20rpx;
    box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.1);
    text-align: center;

    .service-image {
      width: 100%;
      height: 200rpx;
      border-radius: 12rpx;
      overflow: hidden;
      margin-bottom: 16rpx;
      background-color: #f8f8f8;

      image {
        width: 100%;
        height: 100%;
      }
    }

    .service-title {
      font-size: 28rpx;
      color: #333;
      font-weight: 500;
      line-height: 1.3;
    }
  }
}
</style>
