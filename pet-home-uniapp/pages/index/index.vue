<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-bar bg-white">
      <view class="search-input flex items-center" @click="onSearchTap">
        <input
          class="search-text"
          placeholder="搜索宠物用品、医疗服务等"
          disabled="true"
          @click="onSearchTap"
        />
      </view>
    </view>

    <!-- 轮播图 -->
    <swiper 
      class="banner-swiper" 
      indicator-dots="true" 
      autoplay="true" 
      circular="true" 
      interval="4000"
      duration="500"
      indicator-color="rgba(255, 255, 255, 0.5)"
      indicator-active-color="#ff6b35"
      v-if="banners.length > 0"
    >
      <swiper-item v-for="banner in banners" :key="banner.id">
        <view class="banner-item" @click="onBannerTap(banner)">
          <image class="banner-image" :src="getImageUrl(banner.picUrl)" mode="aspectFill" />
          <view class="banner-overlay" v-if="banner.title">
            <view class="banner-title">{{ banner.title }}</view>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <!-- 公告栏 -->
    <view class="notice-bar bg-white" v-if="notice">
      <view class="notice-icon">📢</view>
      <swiper class="notice-swiper" vertical="true" autoplay="true" circular="true" interval="3000" :show-indicator-dots="false">
        <swiper-item>
          <view class="notice-text">{{ notice.title }}</view>
        </swiper-item>
      </swiper>
    </view>

    <!-- 宠物服务 -->
    <view class="service-nav bg-white">
      <view class="service-grid grid grid-cols-3 gap-15">
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
      <view class="section-header flex justify-between items-center">
        <view class="section-title">热门商品</view>
        <view class="section-more" @click="onMoreTap">查看更多 ></view>
      </view>

      <view class="goods-grid grid grid-cols-2 gap-20">
        <view
          class="goods-item"
          v-for="goods in hotProducts"
          :key="goods.id"
          @click="onGoodsTap(goods)"
        >
          <view class="goods-image">
            <image :src="goods.pic" mode="aspectFill" />
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

    <!-- 推荐商品 -->
    <view class="recommend-goods bg-white" v-if="recommendProducts.length > 0">
      <view class="section-header flex justify-between items-center">
        <view class="section-title">为您推荐</view>
        <view class="section-more" @click="onRecommendMoreTap">查看更多 ></view>
      </view>

      <view class="goods-list">
        <view
          class="goods-item-horizontal"
          v-for="goods in recommendProducts"
          :key="goods.id"
          @click="onGoodsTap(goods)"
        >
          <view class="goods-image-horizontal">
            <image :src="goods.pic" mode="aspectFill" />
          </view>
          <view class="goods-info-horizontal">
            <view class="goods-name">{{ goods.name }}</view>
            <view class="goods-desc">{{ goods.description }}</view>
            <view class="goods-price-horizontal">
              <text class="price-current">{{ goods.price }}</text>
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
      recommendProducts: [],
      notice: null,
      services: [],
      loading: true
    }
  },

  onLoad() {
    this.loadHomeData()
  },

  onShow() {
    // 页面显示时刷新数据
    this.loadHomeData()
  },

  onPullDownRefresh() {
    this.loadHomeData(() => {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    // 加载首页数据
    loadHomeData(callback) {
      this.loading = true

      Promise.all([
        this.loadBanners(),
        this.loadHotProducts(),
        this.loadRecommendProducts(),
        this.loadNotice(),
        this.loadServices()
      ]).then(() => {
        this.loading = false
        callback && callback()
      }).catch(err => {
        console.error('加载首页数据失败:', err)
        this.loading = false
        util.showToast('加载数据失败')
        callback && callback()
      })
    },

    // 加载轮播图
    loadBanners() {
      return this.$api.getBannerList(null).then(res => {
        if (res.code === 0 && res.data && Array.isArray(res.data)) {
          this.banners = res.data
        } else {
          this.banners = []
        }
      }).catch(err => {
        console.error('加载轮播图失败:', err)
        // 不使用硬编码数据，保持空数组
        this.banners = []
        uni.showToast({
          title: '加载轮播图失败',
          icon: 'none'
        })
      })
    },


    // 加载热门商品
    loadHotProducts() {
      return this.$api.getHotProducts(4).then(res => {
        if (res.code === 0 && res.data && Array.isArray(res.data)) {
          this.hotProducts = res.data.map(item => {
            // 获取图片URL，优先使用pic，然后是image，最后是imageUrl
            let picUrl = item.pic || item.image || item.imageUrl || ''
            
            // 如果picUrl为空或者不是有效的路径，使用默认图片
            if (!picUrl || picUrl === 'null' || picUrl === 'undefined') {
              picUrl = '/static/images/暂无商品.svg'
            }
            // 如果图片路径以 /upload/ 开头，拼接完整的后端URL
            else if (picUrl.startsWith('/upload/')) {
              picUrl = 'http://localhost:8080' + picUrl
            }
            // 如果不是完整URL且不是本地static路径，也尝试拼接后端URL
            else if (!picUrl.startsWith('http://') && !picUrl.startsWith('https://') && !picUrl.startsWith('/static/')) {
              picUrl = 'http://localhost:8080/upload/' + picUrl
            }
            
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
        uni.showToast({
          title: '加载商品失败',
          icon: 'none'
        })
      })
    },

    // 加载推荐商品
    loadRecommendProducts() {
      return this.$api.getRecommendProducts(3).then(res => {
        if (res.code === 0 && res.data && Array.isArray(res.data)) {
          this.recommendProducts = res.data.map(item => {
            // 获取图片URL，优先使用pic，然后是image，最后是imageUrl
            let picUrl = item.pic || item.image || item.imageUrl || ''
            
            // 如果picUrl为空或者不是有效的路径，使用默认图片
            if (!picUrl || picUrl === 'null' || picUrl === 'undefined') {
              picUrl = '/static/images/暂无商品.svg'
            }
            // 如果图片路径以 /upload/ 开头，拼接完整的后端URL
            else if (picUrl.startsWith('/upload/')) {
              picUrl = 'http://localhost:8080' + picUrl
            }
            // 如果不是完整URL且不是本地static路径，也尝试拼接后端URL
            else if (!picUrl.startsWith('http://') && !picUrl.startsWith('https://') && !picUrl.startsWith('/static/')) {
              picUrl = 'http://localhost:8080/upload/' + picUrl
            }
            
            return {
              ...item,
              pic: picUrl,
              price: util.formatPrice(item.price)
            }
          })
        } else {
          this.recommendProducts = []
        }
      }).catch(err => {
        console.error('加载推荐商品失败:', err)
        this.recommendProducts = []
      })
    },

    // 加载公告
    loadNotice() {
      return this.$api.getLastNotice(null).then(res => {
        if (res.code === 0 && res.data) {
          this.notice = res.data
        }
      }).catch(err => {
        console.error('加载公告失败:', err)
      })
    },

    // 加载服务列表
    loadServices() {
      // 暂时使用默认服务列表，等数据库表创建后再启用API调用
      this.useDefaultServices()
      
      // 注释掉API调用，等数据库表创建后再启用
      /*
      return this.$api.getAllServiceConfigs().then(res => {
        if (res.code === 0 && res.data && Array.isArray(res.data)) {
          // 只显示启用的服务
          this.services = res.data.filter(s => s.status === 1)
        } else {
          // 使用默认服务列表
          this.useDefaultServices()
        }
      }).catch(err => {
        console.error('加载服务列表失败:', err)
        // 如果加载失败，使用默认服务列表
        this.useDefaultServices()
      })
      */
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
          serviceType: 'boarding',
          serviceName: '宠物寄养',
          icon: '/static/images/pet-boarding.svg'
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
        },
        {
          serviceType: 'adoption',
          serviceName: '宠物领养',
          icon: '/static/images/pet-adoption.svg'
        },
        {
          serviceType: 'consultation',
          serviceName: '在线咨询',
          icon: '/static/images/online-consultation.svg'
        }
      ]
    },

    // 点击搜索
    onSearchTap() {
      uni.navigateTo({
        url: '/pages/search/index'
      })
    },

    // 点击消息
    onMessageTap() {
      util.showToast('消息功能开发中')
    },

    // 点击轮播图
    onBannerTap(banner) {
      if (banner.url) {
        // 根据URL类型进行跳转
        if (banner.url.startsWith('/pages/')) {
          uni.navigateTo({
            url: banner.url
          })
        } else if (banner.url.startsWith('http')) {
          // 外部链接
          uni.showToast({
            title: '外部链接功能开发中',
            icon: 'none'
          })
        }
      } else {
        util.showToast('轮播图功能开发中')
      }
    },


    // 点击热门商品
    onGoodsTap(goods) {
      uni.navigateTo({
        url: `/pages/goods/detail?id=${goods.id}`
      })
    },

    // 点击查看更多热门商品
    onMoreTap() {
      uni.switchTab({
        url: '/pages/category/index'
      })
    },

    // 点击查看更多推荐商品
    onRecommendMoreTap() {
      uni.navigateTo({
        url: '/pages/goods/list?type=recommend'
      })
    },

    // 点击服务入口
    onServiceTap(service) {
      switch (service) {
        case 'door-cleaning':
          uni.navigateTo({
            url: '/pages/appointment/door-cleaning'
          })
          break
        case 'boarding':
          uni.navigateTo({
            url: '/pages/appointment/boarding'
          })
          break
        case 'adoption':
          uni.navigateTo({
            url: '/pages/appointment/adoption'
          })
          break
        case 'hospital':
          uni.navigateTo({
            url: '/pages/appointment/medical'
          })
          break
        case 'grooming':
          uni.navigateTo({
            url: '/pages/appointment/grooming'
          })
          break
        case 'consultation':
          uni.navigateTo({
            url: '/pages/appointment/online-consultation'
          })
          break
        default:
          util.showToast('服务开发中')
      }
    },

    // 处理图片URL，解决小程序HTTP协议限制问题
    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
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
.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.search-input {
  flex: 1;
  background-color: #f5f5f5;
  border-radius: 30rpx;
  padding: 16rpx 30rpx;
}


.search-text {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}



/* 轮播图 */
.banner-swiper {
  height: 320rpx;
  margin: 20rpx;
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

/* 公告栏 */
.notice-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  margin-bottom: 20rpx;
  background-color: #fff7e6;
  border: 1rpx solid #ffeaa7;
  border-radius: 8rpx;
  margin: 0 20rpx 20rpx;
}

.notice-icon {
  margin-right: 16rpx;
  font-size: 28rpx;
}

.notice-swiper {
  height: 40rpx;
  flex: 1;
}

.notice-text {
  font-size: 26rpx;
  color: #d48806;
  line-height: 40rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 宠物服务 */
.service-nav {
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.service-grid {
  margin-bottom: 0;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15rpx 5rpx;
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
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  margin-bottom: 30rpx;
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
  height: 280rpx;
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
  padding: 30rpx;
  margin-bottom: 20rpx;
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
  margin-bottom: 20rpx;
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
