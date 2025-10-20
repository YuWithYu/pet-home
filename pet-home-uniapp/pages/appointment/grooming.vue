<template>
  <view class="grooming-container">
    <view class="service-header">
      <!-- 如果有展示图数据，显示图片 -->
      <view v-if="bannerData && bannerData.imageUrl" class="banner-image">
        <image :src="getImageUrl(bannerData.imageUrl)" mode="aspectFill" @error="onBannerImageError" />
      </view>
      <!-- 否则显示默认内容 -->
      <view v-else class="default-banner">
        <view class="service-icon">✂️</view>
        <view class="service-title">宠物洗护中心</view>
        <view class="service-desc">专业宠物洗护服务，让您的宠物保持清洁健康</view>
      </view>
    </view>

    <view class="services-container">
      <view class="section-title">洗护服务</view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-container">
        <view class="loading-text">正在加载服务...</view>
      </view>

      <!-- 服务列表 -->
      <view v-else class="services-grid">
        <view class="service-card" v-for="(service, index) in services" :key="service.id || index" @tap="onServiceTap(service, index)">
          <view class="service-image">
            <image :src="getImageUrl(service.image)" mode="aspectFill" @error="onImageError" />
          </view>
          <view class="service-title">{{ service.title }}</view>
          <view v-if="service.price" class="service-price">¥{{ service.price }}</view>
          <view v-if="service.duration" class="service-duration">{{ service.duration }}分钟</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { util } from '@/common/js/util.js'

export default {
  name: 'Grooming',

  data() {
    return {
      services: [],
      loading: true,
      bannerData: null
    }
  },

  onLoad() {
    this.loadGroomingServices()
    this.loadGroomingBanner()
  },

  methods: {
    async loadGroomingServices() {
      try {
        this.loading = true
        console.log('开始请求洗护服务API...')
        
        const [error, response] = await uni.request({
          url: 'http://localhost:8080/api/grooming-services',
          method: 'GET',
          header: {
            'Content-Type': 'application/json'
          },
          timeout: 10000
        })

        console.log('API错误:', error)
        console.log('API响应:', response)
        console.log('响应状态码:', response?.statusCode)
        console.log('响应数据:', response?.data)

        if (error) {
          console.error('API请求出错:', error)
          this.loadDefaultServices()
          return
        }

        if (response && response.statusCode === 200 && response.data && response.data.code === 0) {
          console.log('API请求成功，数据:', response.data.data)
          this.services = response.data.data.map(service => ({
            id: service.id,
            title: service.name,
            description: service.description,
            price: service.price,
            duration: service.duration,
            image: service.imageUrl ? `http://localhost:8080${service.imageUrl}` : '/static/images/pet-grooming.svg'
          }))
          console.log('处理后的服务数据:', this.services)
        } else {
          console.error('获取洗护服务失败 - 状态码:', response?.statusCode, '数据:', response?.data)
          this.loadDefaultServices()
        }
      } catch (error) {
        console.error('请求洗护服务API失败:', error)
        console.error('错误详情:', JSON.stringify(error))
        this.loadDefaultServices()
      } finally {
        this.loading = false
      }
    },

    async loadGroomingBanner() {
      try {
        console.log('开始请求洗护服务展示图API...')
        
        const [error, response] = await uni.request({
          url: 'http://localhost:8080/tz/grooming-banner',
          method: 'GET',
          header: {
            'Content-Type': 'application/json'
          },
          timeout: 10000
        })

        console.log('展示图API错误:', error)
        console.log('展示图API响应:', response)
        console.log('展示图响应数据:', response?.data)

        if (error) {
          console.error('展示图API请求出错:', error)
          return
        }

        if (response && response.statusCode === 200 && response.data && response.data.code === 0) {
          console.log('展示图API请求成功，数据:', response.data.data)
          this.bannerData = response.data.data
        } else {
          console.error('获取洗护服务展示图失败 - 状态码:', response?.statusCode, '数据:', response?.data)
        }
      } catch (error) {
        console.error('请求洗护服务展示图API失败:', error)
        console.error('错误详情:', JSON.stringify(error))
      }
    },

    loadDefaultServices() {
      // 如果API请求失败，使用默认数据
      console.log('使用默认服务数据')
      this.services = [
        {
          id: 1,
          title: '狗狗洗澡',
          image: '/static/images/pet-grooming.svg'
        },
        {
          id: 2,
          title: '猫咪洗澡',
          image: '/static/images/pet-grooming.svg'
        },
        {
          id: 3,
          title: '宠物SPA护理',
          image: '/static/images/pet-grooming.svg'
        },
        {
          id: 4,
          title: '宠物美容造型',
          image: '/static/images/pet-grooming.svg'
        },
        {
          id: 5,
          title: '宠物清洁护理',
          image: '/static/images/pet-grooming.svg'
        },
        {
          id: 6,
          title: '宠物毛发护理',
          image: '/static/images/pet-grooming.svg'
        }
      ]
    },

    onServiceTap(service, index) {
      // 如果service为undefined，使用index从services数组中获取
      if (!service && typeof index === 'number') {
        service = this.services[index]
      }
      
      if (!service) {
        uni.showToast({
          title: '服务信息错误',
          icon: 'none'
        })
        return
      }
      
      uni.navigateTo({
        url: `/pages/service/detail?serviceType=grooming&serviceId=${service.id || index}`
      })
    },

    onImageError(e) {
      console.log('图片加载失败:', e)
      // 可以设置默认图片
    },

    onBannerImageError(e) {
      console.log('展示图加载失败:', e)
      // 展示图加载失败时，显示默认内容
      this.bannerData = null
    },

    // 处理图片URL，解决小程序HTTP协议限制问题
    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
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
  position: relative;
  height: 300rpx;
  overflow: hidden;

  .banner-image {
    width: 100%;
    height: 100%;
    
    image {
      width: 100%;
      height: 100%;
    }
  }

  .default-banner {
    background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
    padding: 60rpx 40rpx;
    text-align: center;
    color: white;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

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

  .loading-container {
    text-align: center;
    padding: 60rpx 0;

    .loading-text {
      font-size: 28rpx;
      color: #999;
    }
  }

  .service-card {
    background-color: white;
    border-radius: 16rpx;
    padding: 20rpx;
    box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.1);
    text-align: center;
    transition: transform 0.2s ease;

    &:active {
      transform: scale(0.98);
    }

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
      margin-bottom: 8rpx;
    }

    .service-price {
      font-size: 24rpx;
      color: #ff6b35;
      font-weight: bold;
      margin-bottom: 4rpx;
    }

    .service-duration {
      font-size: 22rpx;
      color: #999;
    }
  }
}
</style>
