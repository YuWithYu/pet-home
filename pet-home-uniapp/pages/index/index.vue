<template>
  <view class="page">
    <!-- 轮播图：有数据时展示，加载中且无数据时显示占位避免白屏 -->
    <view v-if="loading && banners.length === 0" class="banner-swiper banner-placeholder">
      <view class="banner-item"><view class="banner-image placeholder" /></view>
    </view>
    <swiper 
      v-else-if="banners.length > 0"
      class="banner-swiper" 
      indicator-dots="true" 
      autoplay="true" 
      circular="true" 
      interval="4000"
      duration="500"
      indicator-color="rgba(255, 255, 255, 0.5)"
      indicator-active-color="#ff6b35"
    >
      <swiper-item v-for="banner in banners" :key="banner.id">
        <view class="banner-item">
          <image class="banner-image" :src="getImageUrl(banner.picUrl)" mode="aspectFill" @error="handleBannerError(banner)" />
        </view>
      </swiper-item>
    </swiper>

    <!-- 宠物服务：3 个入口时用 3 列均分，避免 4 列布局右侧空一大块 -->
    <view class="service-nav bg-white">
      <view class="service-grid grid" :class="serviceGridClass">
        <view 
          v-for="service in services" 
          :key="service.serviceType"
          class="service-item" 
          @click="onServiceTap(service.serviceType)"
        >
          <view class="service-icon">
            <image :src="service.icon" mode="aspectFit" class="service-icon-image" />
          </view>
          <view class="service-name">{{ service.serviceName }}</view>
        </view>
      </view>
    </view>

    <!-- 热门商品 -->
    <view class="hot-goods bg-white">
      <view class="goods-grid grid grid-cols-2 gap-20">
        <view
          class="goods-item"
          v-for="goods in hotProducts"
          :key="goods.id"
          @click="onGoodsTap(goods)"
        >
          <view class="goods-image">
            <image
              :src="getImageUrl(goods.pic)"
              mode="aspectFill"
              :data-id="goods.id"
              @error="handleImageError"
            />
            <view class="goods-tag" v-if="goods.tag">{{ goods.tag }}</view>
          </view>
          <view class="goods-info">
            <view class="goods-name">{{ goods.name }}</view>
            <view class="goods-price">
              <text class="price-current">{{ goods.price }}</text>
              <text class="price-original" v-if="goods.originalPrice">¥{{ goods.originalPrice }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>


    <!-- 底部占位 -->
    <view class="bottom-spacer"></view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'Index',

  data() {
    return {
      banners: [],
      hotProducts: [],
      services: [],
      loading: true,
      lastHomeLoadTime: 0
    }
  },

  onLoad() {
    this.loadHomeData()
  },

  onShow() {
    if (util.redirectStaffToMineIfNeeded()) return
    // 页面显示时静默刷新：节流（15 秒内不重复），避免 tab 切换反复出现「加载中」
    const now = Date.now()
    const throttleMs = 15 * 1000
    if (now - this.lastHomeLoadTime < throttleMs && this.lastHomeLoadTime > 0) {
      return
    }
    this.loadHomeData(null, true)
  },

  onPullDownRefresh() {
    this.loadHomeData(() => {
      uni.stopPullDownRefresh()
    }, false)
  },

  computed: {
    /** 服务入口数量少时用 3 列铺满一行；4 个及以上用 4 列 */
    serviceGridClass() {
      const n = (this.services && this.services.length) || 0
      if (n >= 4) return 'grid-cols-4 gap-20'
      return 'grid-cols-3 gap-20'
    }
  },

  methods: {
    // 加载首页数据；silent 为 true 时不显示全局「加载中」（用于 onShow 静默刷新）
    loadHomeData(callback, silent = false) {
      this.loading = true
      if (!silent) {
        util.showLoading('加载中...')
      }

      Promise.all([
        this.loadBanners(),
        this.loadHotProducts(),
        this.loadServices()
      ]).then(() => {
        this.loading = false
        this.lastHomeLoadTime = Date.now()
        if (!silent) util.hideLoading()
        callback && callback()
      }).catch(err => {
        console.error('加载首页数据失败:', err)
        this.loading = false
        if (!silent) util.hideLoading()
        if (!silent) util.showToast('加载数据失败，请检查网络连接')
        callback && callback()
      })
    },

    // 加载轮播图
    loadBanners() {
      return api.getBannerList(false).then(res => {
        if ((res.code === 200 || res.code === 0) && res.data && Array.isArray(res.data)) {
          // 处理轮播图数据，使用统一的图片URL处理
          this.banners = res.data.map(banner => {
            // 处理图片URL，优先使用picUrl，然后是url
            const picUrl = banner.picUrl || banner.url || banner.image || ''
            banner.picUrl = util.getImageUrl(picUrl)
            banner.url = banner.url ? util.getImageUrl(banner.url) : banner.url
            return banner
          })
        } else {
          this.banners = []
        }
      }).catch(err => {
        console.error('加载轮播图失败:', err)
        // 不使用硬编码数据，保持空数组
        this.banners = []
        // 不在这里显示 toast，由 loadHomeData 统一处理
      })
    },


    // 加载热门商品
    loadHotProducts() {
      return api.getHotProducts(10, false).then(res => {
        if ((res.code === 200 || res.code === 0) && res.data && Array.isArray(res.data)) {
          // 过滤掉积分商城商品（前端双重保险）
          const filteredData = res.data.filter(item => 
            item.category !== "积分商城" && item.category !== '积分商城'
          )
          
          this.hotProducts = filteredData.map(item => {
            // 保存后端返回的图片路径（相对路径），展示时由 getImageUrl 拼出可访问 URL
            const picUrl = (item.pic || item.image || item.imageUrl || '').trim()
            return {
              ...item,
              pic: picUrl,
              price: util.formatPrice(item.price)
            }
          })
        } else {
          this.hotProducts = []
        }
      }).catch(err => {
        console.error('加载热门商品失败:', err)
        // 不使用硬编码数据，保持空数组
        this.hotProducts = []
        // 不在这里显示 toast，由 loadHomeData 统一处理
      })
    },



    // 加载服务列表
    loadServices() {
      return api.getAllServiceConfigs().then(res => {
        if (res.code === 0 && res.data && Array.isArray(res.data)) {
          this.services = res.data.filter(s => s.status === 1).map(s => ({
            serviceType: s.serviceType,
            serviceName: s.serviceName || s.service_type,
            icon: s.icon || '/static/images/door-cleaning.svg'
          }))
          if (this.services.length === 0) {
            this.useDefaultServices()
          }
        } else {
          this.useDefaultServices()
        }
      }).catch(err => {
        console.error('加载服务列表失败:', err)
        this.useDefaultServices()
      })
    },

    // 使用默认服务列表
    useDefaultServices() {
      this.services = [
        {
          serviceType: 'door-cleaning',
          serviceName: '上门铲屎',
          icon: '/static/images/door-cleaning.svg'
        },
        {
          serviceType: 'hospital',
          serviceName: '宠物医院',
          icon: '/static/images/pet-hospital.svg'
        },
        {
          serviceType: 'grooming',
          serviceName: '宠物洗护',
          icon: '/static/images/pet-grooming.svg'
        }
      ]
    },

    // 点击消息
    onMessageTap() {
      util.showToast('消息功能开发中')
    },

    // 点击热门商品
    onGoodsTap(goods) {
      uni.navigateTo({
        url: `/pages-goods/detail?id=${goods.id}`
      })
    },

    // 点击查看更多热门商品
    onMoreTap() {
      uni.switchTab({
        url: '/pages/goods-category/index'
      })
    },


    // 点击服务入口
    onServiceTap(service) {
      switch (service) {
        case 'door-cleaning':
          uni.navigateTo({
            url: '/appointment/door-cleaning'
          })
          break
        case 'hospital':
          uni.navigateTo({
            url: '/appointment/medical'
          })
          break
        case 'grooming':
          uni.navigateTo({
            url: '/appointment/grooming'
          })
          break
        default:
          util.showToast('服务开发中')
      }
    },

    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
    },
    handleBannerError(banner) {
      if (banner && banner.picUrl) {
        banner.picUrl = ''
      }
    },
    // 图片加载失败时，将该商品项的 pic 置空
    handleImageError(e) {
      const target = e.target || e.currentTarget
      const id = target && target.dataset && target.dataset.id
      if (id != null && this.hotProducts && this.hotProducts.length) {
        const idx = this.hotProducts.findIndex(p => p.id == id || String(p.id) === String(id))
        if (idx >= 0) {
          this.$set(this.hotProducts, idx, { ...this.hotProducts[idx], pic: '' })
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '@/common/css/common.scss';

.page {
  background-color: #f8f8f8;
  min-height: 100vh;
}

/* 搜索栏 */
/* 轮播图 */
.banner-swiper {
  height: 400rpx;
  margin: 10rpx 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.banner-item {
  position: relative;
  height: 100%;
  border-radius: 16rpx;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  border-radius: 16rpx;
}

.banner-placeholder .placeholder {
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 50%, #eee 75%);
  background-size: 200% 100%;
  animation: banner-shine 1.2s ease-in-out infinite;
}

@keyframes banner-shine {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  border-radius: 0 0 16rpx 16rpx;
  padding: 80rpx 30rpx 30rpx;
}

.banner-title {
  color: white;
  font-size: 32rpx;
  font-weight: bold;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.5);
}

/* 宠物服务 */
.service-nav {
  padding: 24rpx 32rpx 28rpx;
  margin-bottom: 10rpx;
}

.service-grid {
  margin-bottom: 0;
  width: 100%;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  min-width: 0;
  padding: 12rpx 8rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.service-item:active {
  background-color: #f0f0f0;
}

.service-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-bottom: 8rpx;
  
  .service-icon-image {
    width: 80rpx;
    height: 80rpx;
  }
}

.service-name {
  font-size: 24rpx;
  color: #666;
  text-align: center;
}

/* 热门商品 */
.hot-goods {
  padding: 20rpx 30rpx;
  margin-bottom: 10rpx;
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.section-more {
  font-size: 26rpx;
  color: #ff6b35;
}

.goods-grid {
  margin-bottom: 0;
}

.goods-item {
  background-color: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.goods-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.goods-image {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
}

.goods-image image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.goods-image image {
  width: 100%;
  height: 100%;
}

.goods-tag {
  position: absolute;
  top: 10rpx;
  left: 10rpx;
  background-color: #ff6b35;
  color: white;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.goods-info {
  padding: 20rpx;
}

.goods-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  display: flex;
  align-items: center;
}

.price-current {
  font-size: 32rpx;
  color: #ff6b35;
  font-weight: bold;
}

.price-original {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 12rpx;
}

/* 推荐商品 */
.recommend-goods {
  padding: 20rpx 30rpx;
  margin-bottom: 10rpx;
}

.goods-list {
  margin-bottom: 0;
}

.goods-item-horizontal {
  display: flex;
  background-color: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  margin-bottom: 15rpx;
  transition: all 0.3s ease;
}

.goods-item-horizontal:active {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.goods-image-horizontal {
  width: 200rpx;
  height: 160rpx;
  flex-shrink: 0;
}

.goods-image-horizontal image {
  width: 100%;
  height: 100%;
}

.goods-info-horizontal {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.goods-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.goods-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goods-price-horizontal {
  font-size: 32rpx;
  color: #ff6b35;
  font-weight: bold;
}


/* 底部占位 */
.bottom-spacer {
  height: 100rpx;
}
</style>
