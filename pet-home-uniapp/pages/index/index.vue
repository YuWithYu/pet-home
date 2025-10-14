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
          <image class="banner-image" :src="banner.picUrl" mode="aspectFill" />
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
        <view class="service-item" @click="onServiceTap('door-cleaning')">
          <view class="service-icon">
            <image src="/static/images/door-cleaning.svg" mode="aspectFit" class="service-icon-image" />
          </view>
          <view class="service-name">上门铲屎</view>
        </view>
        <view class="service-item" @click="onServiceTap('boarding')">
          <view class="service-icon">
            <image src="/static/images/pet-boarding.svg" mode="aspectFit" class="service-icon-image" />
          </view>
          <view class="service-name">宠物寄养</view>
        </view>
        <view class="service-item" @click="onServiceTap('hospital')">
          <view class="service-icon">
            <image src="/static/images/pet-hospital.svg" mode="aspectFit" class="service-icon-image" />
          </view>
          <view class="service-name">宠物医院</view>
        </view>
        <view class="service-item" @click="onServiceTap('grooming')">
          <view class="service-icon">
            <image src="/static/images/pet-grooming.svg" mode="aspectFit" class="service-icon-image" />
          </view>
          <view class="service-name">宠物洗护</view>
        </view>
        <view class="service-item" @click="onServiceTap('adoption')">
          <view class="service-icon">
            <image src="/static/images/pet-adoption.svg" mode="aspectFit" class="service-icon-image" />
          </view>
          <view class="service-name">宠物领养</view>
        </view>
        <view class="service-item" @click="onServiceTap('consultation')">
          <view class="service-icon">
            <image src="/static/images/online-consultation.svg" mode="aspectFit" class="service-icon-image" />
          </view>
          <view class="service-name">在线咨询</view>
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
        this.loadNotice()
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
        if (res.code === 0 && res.data) {
          this.banners = res.data
        }
      }).catch(err => {
        console.error('加载轮播图失败:', err)
        // 使用默认轮播图数据
        this.banners = [
          {
            id: 1,
            picUrl: '/static/images/banner1.jpg',
            title: '宠物健康生活',
            url: '/pages/goods/list?type=health'
          },
          {
            id: 2,
            picUrl: '/static/images/banner2.jpg',
            title: '优质宠物食品',
            url: '/pages/goods/list?type=food'
          },
          {
            id: 3,
            picUrl: '/static/images/banner3.jpg',
            title: '宠物医疗服务',
            url: '/pages/appointment/medical'
          }
        ]
      })
    },


    // 加载热门商品
    loadHotProducts() {
      return this.$api.getHotProducts(4).then(res => {
        if (res.code === 0 && res.data) {
          this.hotProducts = res.data.map(item => {
            let picUrl = item.image || item.pic
            // 如果图片路径以 /upload/ 开头，拼接完整的后端URL
            if (picUrl && picUrl.startsWith('/upload/')) {
              picUrl = 'https://localhost:8080' + picUrl
            }
            return {
              ...item,
              pic: picUrl,
              price: util.formatPrice(item.price)
            }
          })
        }
      }).catch(err => {
        console.error('加载热门商品失败:', err)
        // 使用模拟数据
        this.hotProducts = [
          {
            id: 1,
            name: '皇家猫粮',
            pic: '/static/images/product1.jpg',
            price: util.formatPrice(128),
            originalPrice: 158
          },
          {
            id: 2,
            name: '狗狗玩具球',
            pic: '/static/images/product2.jpg',
            price: util.formatPrice(25)
          },
          {
            id: 3,
            name: '猫砂盆',
            pic: '/static/images/product3.jpg',
            price: util.formatPrice(89)
          },
          {
            id: 4,
            name: '宠物沐浴露',
            pic: '/static/images/product4.jpg',
            price: util.formatPrice(45)
          }
        ]
      })
    },

    // 加载推荐商品
    loadRecommendProducts() {
      return this.$api.getRecommendProducts(3).then(res => {
        if (res.code === 0 && res.data) {
          this.recommendProducts = res.data.map(item => {
            let picUrl = item.image || item.pic
            // 如果图片路径以 /upload/ 开头，拼接完整的后端URL
            if (picUrl && picUrl.startsWith('/upload/')) {
              picUrl = 'https://localhost:8080' + picUrl
            }
            return {
              ...item,
              pic: picUrl,
              price: util.formatPrice(item.price)
            }
          })
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
          util.showToast('上门铲屎服务开发中')
          break
        case 'boarding':
          uni.navigateTo({
            url: '/pages/appointment/boarding'
          })
          break
        case 'adoption':
          util.showToast('宠物领养功能开发中')
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
          util.showToast('在线咨询功能开发中')
          break
        default:
          util.showToast('服务开发中')
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
