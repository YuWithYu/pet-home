<template>
  <view class="grooming-container">
    <view class="service-header">
      <view class="service-icon">✂️</view>
      <view class="service-title">宠物洗护中心</view>
      <view class="service-desc">专业宠物洗护服务，让您的宠物保持清洁健康</view>
    </view>

    <view class="services-container">
      <view class="section-title">洗护服务</view>

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
  name: 'Grooming',

  data() {
    return {
      services: [
        {
          title: '狗狗洗澡',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '猫咪洗澡',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '宠物SPA护理',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '宠物美容造型',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '宠物清洁护理',
          image: '/static/images/placeholder-service.png' // 管理员后台替换
        },
        {
          title: '宠物毛发护理',
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
.grooming-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.service-header {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  padding: 60rpx 40rpx;
  text-align: center;
  color: white;

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
