<template>
  <view class="medical-container">
    <view class="service-header">
      <!-- 如果有展示图数据，显示图-->
      <view v-if="bannerData && bannerData.imageUrl" class="banner-image">
        <image :src="getImageUrl(bannerData.imageUrl)" mode="aspectFill" @error="onBannerImageError" />
      </view>
      <!-- 否则显示默认内容 -->
      <view v-else class="default-banner">
        <view class="service-icon">🏥</view>
        <view class="service-title">宠物医疗中心</view>
        <view class="service-desc">本中心承接宠物医疗、美容、保健、寄养等服务</view>
        <view class="service-promise">
          专注于宠物的健康，凭着一切让宠物远离病痛、远离苦恼的信念，打造综合医疗技术亮点
          <text class="website">www.houjiuhospital.com</text>
        </view>
      </view>
    </view>

    <view class="services-container">
      <!-- 加载状态-->
      <view v-if="loading" class="loading-container">
        <view class="loading-text">正在加载服务...</view>
      </view>

      <!-- 服务列表 -->
      <view v-else class="services-grid">
        <view class="service-card" v-for="(service, index) in services" :key="'service-' + index" @tap="onServiceTap(index)">
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
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'Medical',

  data() {
    return {
      services: [],
      bannerData: null,
      loading: true
    }
  },

  onLoad() {
    this.loadMedicalServices()
    this.loadMedicalBanner()
  },

  methods: {
    async loadMedicalServices() {
      try {
        this.loading = true
        const res = await api.getHospitalServiceList()
        if ((res.code === 0 || res.code === 200) && res.data) {
          const list = Array.isArray(res.data) ? res.data : (res.data.records || res.data.list || [])
          this.services = list.map(service => ({
            id: service.id,
            title: service.name,
            description: service.description,
            price: service.price,
            duration: service.duration,
            image: service.imageUrl || service.image_url || service.bannerImage || '/static/images/placeholder-service.png'
          }))
        } else {
          console.error('获取医疗服务失败:', res.msg)
          this.loadDefaultServices()
        }
      } catch (error) {
        console.error('请求医疗服务API失败:', error)
        this.loadDefaultServices()
      } finally {
        this.loading = false
      }
    },

    async loadMedicalBanner() {
      try {
        const res = await api.getHospitalBanner('hospital-page-top')
        if ((res.code === 200 || res.code === 0) && res.data) {
          this.bannerData = res.data
        } else {
          console.error('获取宠物医院展示图失败:', res.msg || res.message)
          this.bannerData = null
        }
      } catch (error) {
        console.error('请求宠物医院展示图失败:', error)
        this.bannerData = null
      }
    },

    loadDefaultServices() {
      // 如果API请求失败，使用默认数据
      this.services = [
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
    },

    onServiceTap(index) {
      const service = this.services[index]

      if (!service) {
        uni.showToast({
          title: '服务信息错误',
          icon: 'none'
        })
        return
      }

      const url = `/service/detail?serviceType=hospital&serviceId=${service.id || ''}&price=${service.price || 0}`
      uni.navigateTo({
        url,
        fail: (err) => {
          console.error('跳转失败:', err)
          uni.showToast({
            title: '页面跳转失败',
            icon: 'none'
          })
        }
      })
    },


    onImageError(e) {
      // 可以设置默认图片
    },

    onBannerImageError(e) {
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
.medical-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.service-header {
  position: relative;
  height: 460rpx;
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
    background: linear-gradient(135deg, #4FC3F7 0%, #29B6F6 100%);
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
      color: #4FC3F7;
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
