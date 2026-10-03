<template>
  <view class="points-mall-page">
    <view class="points-section">
      <view class="points-card">
        <view class="user-avatar">
          <image 
            :src="getAvatarUrl()" 
            mode="aspectFill" 
            class="avatar-image"
            @error="handleAvatarError"
          />
        </view>
        <view class="user-details">
          <text class="user-name">{{ userName }}</text>
          <view class="points-balance">
            <image src="/static/images/my-cans.png" mode="aspectFit" class="can-icon-image" />
            <text class="points-text">{{ ui.canLabel }} {{ pointsBalance }}g</text>
          </view>
        </view>
      </view>
    </view>

    <view class="products-section">
      <view v-if="products && products.length > 0" class="products-grid">
        <view 
          class="product-item"
          v-for="product in products"
          :key="product.id"
        >
          <image 
            class="product-image"
            :src="getProductImageUrl(product.image)"
            mode="aspectFill"
            @error="handleImageError"
          />
          <view class="product-title">{{ product.title }}</view>
          <view class="product-subtitle" v-if="product.subtitle">{{ product.subtitle }}</view>
          <view class="product-desc">{{ product.desc }}</view>
          <view class="product-footer">
            <view class="product-price">
              <image src="/static/images/my-cans.png" mode="aspectFit" class="can-icon-image" />
              <text class="price-text">{{ ui.canLabel }} {{ product.price }}kg</text>
            </view>
            <view 
              class="product-btn"
              :class="{ 
                'btn-disabled': product.status === 'ended'
              }"
              @click="handleProductAction(product)"
            >
              {{ product.status === 'ended' ? ui.btnEnded : ui.btnRedeem }}
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <text class="empty-text">{{ ui.emptyTitle }}</text>
        <text class="empty-subtext">{{ ui.emptySubtext }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { mapGetters } from 'vuex'

export default {
  data() {
    return {
      userName: 'Yuuu',
      pointsBalance: 0,
      products: [],
      // Unicode escapes: avoid garbled text in mp-weixin when source encoding is corrupted
      ui: {
        canLabel: '\u7f50\u5934',
        btnRedeem: '\u5151\u6362',
        btnEnded: '\u5df2\u7ed3\u675f',
        loading: '\u52a0\u8f7d\u4e2d...',
        pleaseLogin: '\u8bf7\u5148\u767b\u5f55',
        loadFailed: '\u52a0\u8f7d\u5931\u8d25',
        exchangeLoading: '\u5151\u6362\u4e2d...',
        exchangeSuccess: '\u5151\u6362\u6210\u529f',
        exchangeErr: '\u5151\u6362\u5931\u8d25',
        emptyTitle: '\u6682\u65e0\u5546\u54c1',
        emptySubtext: '\u656c\u8bf7\u671f\u5f85\u5176\u5b83\u597d\u793c\u54e6~'
      }
    }
  },

  computed: {
    ...mapGetters(['isLoggedIn', 'userNickname', 'userInfo'])
  },

  onLoad() {
    const token = uni.getStorageSync('token')
    if (!token || !this.isLoggedIn) {
      uni.showToast({
        title: this.ui.pleaseLogin,
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages-auth/login'
        })
      }, 1500)
      return
    }
    
    if (this.userNickname) {
      this.userName = this.userNickname
    } else if (this.userInfo && this.userInfo.username) {
      this.userName = this.userInfo.username
    }
    
    this.loadProducts()
  },

  onShow() {
    const token = uni.getStorageSync('token')
    if (!token || !this.isLoggedIn) {
      return
    }
    
    if (this.userNickname) {
      this.userName = this.userNickname
    } else if (this.userInfo && this.userInfo.username) {
      this.userName = this.userInfo.username
    }
    
    this.loadUserPoints()
  },

  methods: {
    getProductImageUrl(imageUrl) {
      if (!imageUrl) {
        return '/static/images/login-dog.png'
      }
      if (imageUrl.startsWith('/static/')) {
        return imageUrl
      }
      const { util } = require('@/common/js/util.js')
      return util.getImageUrl(imageUrl)
    },

    handleImageError(e) {
      console.error('图片加载失败:', e)
      if (e.target) {
        e.target.src = '/static/images/login-dog.png'
      }
    },
    
    getAvatarUrl() {
      if (this.userInfo && this.userInfo.avatar) {
        const { util } = require('@/common/js/util.js')
        return util.getImageUrl(this.userInfo.avatar)
      }
      return '/static/images/login-dog.png'
    },
    
    handleAvatarError(e) {
      console.error('头像加载失败:', e)
      if (e.target) {
        e.target.src = '/static/images/login-dog.png'
      }
    },

    async loadUserPoints() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          return
        }
        
        const res = await api.getSignInData()
        if (res.data) {
          this.pointsBalance = res.data.balance || 0
        }
      } catch (error) {
        console.error('加载积分失败:', error)
      }
    },

    async loadProducts() {
      try {
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showToast({
            title: this.ui.pleaseLogin,
            icon: 'none'
          })
          setTimeout(() => {
            uni.navigateTo({
              url: '/pages-auth/login'
            })
          }, 1500)
          return
        }
        
        uni.showLoading({
          title: this.ui.loading,
          mask: true
        })
        
        const res = await api.getPointsMallProducts()
        uni.hideLoading()
        
        if (res && res.data && res.data.products) {
          this.products = res.data.products
        } else {
          this.products = []
        }
      } catch (error) {
        uni.hideLoading()
        console.error('加载商品列表失败:', error)
        this.products = []
        if (error.message) {
          uni.showToast({
            title: this.ui.loadFailed,
            icon: 'none'
          })
        }
      }
    },

    async handleProductAction(product) {
      uni.navigateTo({
        url: `/points-mall/detail?id=${product.id}`
      })
    },

    async exchangeProduct(product) {
      try {
        uni.showLoading({ title: this.ui.exchangeLoading })
        const res = await api.exchangeProduct(product.id)
        uni.hideLoading()

        if (res.data) {
          uni.showToast({
            title: this.ui.exchangeSuccess,
            icon: 'success'
          })
          
          this.pointsBalance = res.data.newBalance || this.pointsBalance
          await this.loadProducts()
        }
      } catch (error) {
        uni.hideLoading()
        console.error('兑换失败:', error)
        uni.showToast({
          title: error.message || this.ui.exchangeErr,
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.points-mall-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: env(safe-area-inset-bottom);
}

.points-section {
  margin-top: 0;
  padding: 20rpx;
}

.points-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16rpx;
  background-color: #f5f5f5;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.points-balance {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.can-icon-image {
  width: 24rpx;
  height: 24rpx;
}

.points-text {
  font-size: 26rpx;
  color: #333;
  font-weight: normal;
}

.products-section {
  padding: 0 30rpx 30rpx;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.product-item {
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.product-image {
  width: 100%;
  height: 280rpx;
  background-color: #f5f5f5;
}

.product-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  padding: 16rpx 16rpx 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 22rpx;
  color: #666;
  padding: 8rpx 16rpx 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.product-desc {
  font-size: 22rpx;
  color: #999;
  padding: 8rpx 16rpx 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 6rpx;
  flex: 1;
}

.product-price .can-icon-image {
  width: 20rpx;
  height: 20rpx;
}

.price-text {
  font-size: 24rpx;
  color: #333;
  font-weight: normal;
}

.product-btn {
  padding: 10rpx 28rpx;
  border-radius: 40rpx;
  font-size: 26rpx;
  font-weight: 500;
  white-space: nowrap;
  background-color: #ffd700;
  color: #333;
}

.product-btn.btn-disabled {
  background-color: #e0e0e0;
  color: #999;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 40rpx;
  margin-top: 100rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.empty-subtext {
  font-size: 26rpx;
  color: #ccc;
}
</style>
