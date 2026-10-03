<template>
  <view class="product-detail-page">
    <!-- 商品图片 -->
    <view class="product-image-section">
      <image 
        class="product-main-image"
        :src="getProductImageUrl(product.image)"
        mode="aspectFill"
        @error="handleImageError"
      />
    </view>

    <!-- 价格信息 -->
    <view class="price-section">
      <view class="price-info">
        <image src="/static/images/my-cans.png" mode="aspectFit" class="can-icon-small" />
        <text class="price-amount">{{ product.price }}kg</text>
      </view>
    </view>

    <!-- 商品标题和已兑换 -->
    <view class="product-info-section">
      <view class="product-title">{{ product.title }}</view>
    </view>

    <!-- 权益提供方 -->
    <view class="provider-section">
      <view class="section-title">权益提供方</view>
      <view class="provider-info">
        <text class="provider-label">联系方式：</text>
        <text class="provider-name">宠物之家官方旗舰店</text>
      </view>
      <view class="provider-contact">请联系在线客服</view>
    </view>

    <!-- 看了又看 -->
    <view class="recommend-section">
      <view class="recommend-title-wrapper">
        <view class="title-line"></view>
        <text class="section-title">看了又看</text>
        <view class="title-line"></view>
      </view>
      <view class="recommend-products" v-if="recommendProducts && recommendProducts.length > 0">
        <view 
          class="recommend-item"
          v-for="item in recommendProducts"
          :key="item.id"
        >
          <image 
            class="recommend-image"
            :src="getProductImageUrl(item.image)"
            mode="aspectFill"
            @error="handleImageError"
            @click="goToProductDetail(item.id)"
          />
          <view class="recommend-content">
            <view class="recommend-title" @click="goToProductDetail(item.id)">{{ item.title }}</view>
            <view class="recommend-footer">
              <view class="recommend-price">
                <image src="/static/images/my-cans.png" mode="aspectFit" class="recommend-can-icon" />
                <text class="recommend-price-text">× {{ item.price }}kg</text>
              </view>
              <view 
                class="recommend-btn"
                :class="{ 'btn-disabled': item.status === 'ended' }"
                @click.stop="handleRecommendExchange(item)"
              >
                {{ item.status === 'ended' ? '已结束' : '兑换' }}
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 罐头不足提示 -->
    <view class="insufficient-warning" v-if="pointsBalance < requiredPoints">
      <text class="warning-text">你的罐头不足</text>
      <text class="earn-link" @click="goToEarnCans">去赚取罐头 ></text>
    </view>

    <!-- 底部兑换按钮 -->
    <view class="bottom-action">
      <view 
        class="exchange-btn"
        :class="{ 'btn-disabled': pointsBalance < requiredPoints || product.status === 'ended' }"
        @click="handleExchange"
      >
        {{ getButtonText() }}
      </view>
    </view>

    <!-- 兑换规格弹窗（与商城下单交互对齐） -->
    <view class="spec-popup-mask" v-if="showExchangePopup" @click="closeExchangePopup">
      <view class="spec-popup" @click.stop>
        <view class="spec-popup-header">
          <text class="spec-popup-title">选择规格</text>
          <text class="spec-popup-close" @click="closeExchangePopup">×</text>
        </view>
        <view class="spec-popup-product">
          <image class="spec-popup-image" :src="getProductImageUrl(product.image)" mode="aspectFill" />
          <view class="spec-popup-info">
            <view class="spec-popup-name">{{ product.title }}</view>
            <view class="spec-popup-price">{{ requiredPoints }}g / 件</view>
          </view>
        </view>
        <view class="spec-block">
          <view class="spec-label">规格</view>
          <view class="spec-option selected">默认规格</view>
        </view>
        <view class="quantity-row">
          <view class="spec-label">兑换数量</view>
          <view class="qty-box">
            <view class="qty-btn" @click="changeExchangeQuantity(-1)">-</view>
            <view class="qty-num">{{ exchangeQuantity }}</view>
            <view class="qty-btn" @click="changeExchangeQuantity(1)">+</view>
          </view>
        </view>
        <view class="spec-popup-footer">
          <view class="spec-total">合计：{{ requiredPoints * exchangeQuantity }}g</view>
          <view class="spec-confirm" @click="confirmExchangeSelection">确定</view>
        </view>
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
      productId: null,
      product: {},
      pointsBalance: 0,
      requiredPoints: 0,
      recommendProducts: [],
      scrollTop: 0,
      navbarOpacity: 0,
      statusBarHeight: 0,
      showExchangePopup: false,
      exchangeQuantity: 1
    }
  },

  computed: {
    ...mapGetters(['isLoggedIn', 'userInfo'])
  },

  onLoad(options) {
    // 获取系统信息
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
      }
    })
    
    if (options.id) {
      this.productId = parseInt(options.id)
      this.loadProductDetail()
      this.loadUserPoints()
      this.loadRecommendProducts()
    } else {
      uni.showToast({
        title: '商品ID无效',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },
  
  onPageScroll(e) {
    this.scrollTop = e.scrollTop
    // 计算导航栏透明度：滚动超过100rpx时开始显示，超过300rpx时完全不透明
    const threshold1 = 100 // 开始显示（rpx）
    const threshold2 = 300 // 完全不透明（rpx）
    if (e.scrollTop < threshold1) {
      this.navbarOpacity = 0
    } else if (e.scrollTop >= threshold2) {
      this.navbarOpacity = 1
    } else {
      this.navbarOpacity = (e.scrollTop - threshold1) / (threshold2 - threshold1)
    }
  },

  methods: {
    // 加载商品详情
    async loadProductDetail() {
      try {
        uni.showLoading({ title: '加载中...' })
        
        // 从积分商城商品列表获取商品详情
        const res = await api.getPointsMallProducts()
        uni.hideLoading()
        
        if (res && res.data && res.data.products) {
          const product = res.data.products.find(p => p.id === this.productId)
          if (product) {
            this.product = product
            // 价格从kg转换为g
            this.requiredPoints = product.price * 1000
          } else {
            uni.showToast({
              title: '商品不存在',
              icon: 'none'
            })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          }
        } else {
          uni.showToast({
            title: '加载商品失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('加载商品详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },

    // 加载用户积分
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
        console.error('加载用户积分失败:', error)
      }
    },

    // 获取商品图片URL
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

    // 处理图片加载错误
    handleImageError(e) {
      console.error('商品图片加载失败:', e)
      if (e.target) {
        e.target.src = '/static/images/login-dog.png'
      }
    },

    // 获取按钮文字
    getButtonText() {
      // 如果商品还没加载完成，显示默认文字
      if (!this.product || !this.product.id) {
        return '加载中...'
      }
      
      if (this.product.status === 'ended') {
        return '已结束'
      }
      
      // 确保pointsBalance和requiredPoints都是数字
      const balance = Number(this.pointsBalance) || 0
      const required = Number(this.requiredPoints) || 0
      
      if (balance < required) {
        return '罐头不足'
      }
      
      return '立即兑换'
    },

    // 处理兑换
    async handleExchange() {
      if (this.product.status === 'ended') {
        return
      }

      if (this.pointsBalance < this.requiredPoints) {
        this.goToEarnCans()
        return
      }

      this.showExchangePopup = true
    },
    closeExchangePopup() {
      this.showExchangePopup = false
    },
    changeExchangeQuantity(delta) {
      const next = (this.exchangeQuantity || 1) + delta
      if (next < 1) return
      const stock = Number(this.product.stock || 0)
      if (stock > 0 && next > stock) {
        uni.showToast({ title: '超过库存数量', icon: 'none' })
        return
      }
      this.exchangeQuantity = next
    },
    confirmExchangeSelection() {
      if (!this.exchangeQuantity || this.exchangeQuantity < 1) {
        uni.showToast({ title: '请选择兑换数量', icon: 'none' })
        return
      }
      const totalNeedPoints = this.requiredPoints * this.exchangeQuantity
      if (this.pointsBalance < totalNeedPoints) {
        uni.showToast({ title: '罐头不足', icon: 'none' })
        return
      }
      // 构建订单数据，跳转到确认订单页面
      const orderData = {
        goods: {
          id: this.product.id,
          name: this.product.title,
          image: this.product.image,
          price: 0, // 积分商品价格为0，使用积分支付
          points: this.product.price * 1000 // 需要的积分（g）
        },
        selectedSpec: { name: '默认规格' },
        quantity: this.exchangeQuantity,
        totalAmount: 0, // 商品金额为0
        shippingFee: 0,
        finalAmount: 0, // 最终金额为0，使用积分支付
        orderType: 'points', // 标识为积分订单
        points: totalNeedPoints // 需要的积分
      }
      
      // 保存订单数据到本地存储
      uni.setStorageSync('orderData', orderData)
      
      // 跳转到订单确认页面
      uni.navigateTo({
        url: '/order/confirm',
        success: () => {
        },
        fail: (err) => {
          console.error('跳转到订单确认页面失败:', err)
          uni.showToast({
            title: '跳转失败，请重试',
            icon: 'none'
          })
        }
      })
    },

    // 兑换商品
    async exchangeProduct() {
      try {
        // 第一步：填写收货信息
        const addressInfo = await this.showAddressInput()
        if (!addressInfo) {
          return // 用户取消了
        }
        
        // 第二步：确认库存状态
        const stockConfirmed = await this.confirmStockStatus()
        if (!stockConfirmed) {
          return // 用户取消了
        }
        
        // 第三步：执行兑换（支付）
        uni.showLoading({ title: '兑换中...' })
        const exchangeData = {
          productId: this.productId
        }
        if (addressInfo) {
          exchangeData.address = {
            name: addressInfo.contactName || addressInfo.name || '',
            phone: addressInfo.contactPhone || addressInfo.phone || '',
            province: addressInfo.province || '',
            city: addressInfo.city || '',
            district: addressInfo.district || '',
            detail: addressInfo.detail || ''
          }
          if (addressInfo.id) {
            exchangeData.addressId = addressInfo.id
          }
        }
        const res = await api.exchangeProduct(exchangeData)
        uni.hideLoading()
        
        if (res && res.code === 200) {
          uni.showToast({
            title: '兑换成功',
            icon: 'success'
          })
          
          // 更新积分余额
          if (res.data && res.data.newBalance !== undefined) {
            this.pointsBalance = res.data.newBalance
          } else {
            await this.loadUserPoints()
          }
          
          // 更新已兑数量
          if (res.data && res.data.exchangedCount !== undefined) {
            this.product.exchangedCount = res.data.exchangedCount
          } else {
            // 如果后端没有返回，重新加载商品详情
            await this.loadProductDetail()
          }
          
          // 返回上一页
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res?.msg || '兑换失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('兑换失败:', error)
        uni.showToast({
          title: error.message || '兑换失败',
          icon: 'none'
        })
      }
    },
    
    // 显示收货信息输入
    showAddressInput() {
      return new Promise((resolve) => {
        // 先检查是否登录
        const token = uni.getStorageSync('token')
        if (!token) {
          uni.showModal({
            title: '提示',
            content: '请先登录后再选择收货地址',
            showCancel: false,
            success: () => {
              uni.reLaunch({
                url: '/pages/login/login'
              })
            }
          })
          resolve(null)
          return
        }
        
        // 跳转到地址选择页面
        uni.navigateTo({
          url: '/address/list?from=exchange&selectMode=true',
          success: () => {
            // 监听地址选择事件
            const handler = (address) => {
              uni.$off('addressSelected', handler)
              resolve(address || null)
            }
            uni.$on('addressSelected', handler)
            
            // 设置超时，如果用户没有选择地址，则取消
            setTimeout(() => {
              uni.$off('addressSelected', handler)
              // 检查是否已经resolve
              if (!this._addressResolved) {
                resolve(null)
              }
            }, 300000) // 5分钟超时
          },
          fail: (err) => {
            console.error('[积分商城详情] showAddressInput - 跳转失败:', err)
            uni.showToast({
              title: '跳转失败，请重试',
              icon: 'none',
              duration: 2000
            })
            resolve(null)
          }
        })
      })
    },
    
    // 确认库存状态
    confirmStockStatus() {
      return new Promise((resolve) => {
        const stock = this.product.stock || 0
        uni.showModal({
          title: '确认库存状态',
          content: `当前库存：${stock}件\n确认继续兑换吗？`,
          success: (res) => {
            if (res.confirm) {
              resolve(true)
            } else {
              resolve(false)
            }
          },
          fail: () => {
            resolve(false)
          }
        })
      })
    },

    // 去赚取罐头
    goToEarnCans() {
      uni.navigateTo({
        url: '/pages-community/signin'
      })
    },

    // 加载推荐商品
    async loadRecommendProducts() {
      try {
        const res = await api.getPointsMallProducts()
        if (res && res.data && res.data.products) {
          // 排除当前商品，取前4个作为推荐（2x2网格）
          this.recommendProducts = res.data.products
            .filter(p => p.id !== this.productId)
            .slice(0, 4)
        }
      } catch (error) {
        console.error('加载推荐商品失败:', error)
      }
    },

    // 处理推荐商品的兑换
    handleRecommendExchange(item) {
      if (item.status === 'ended') {
        uni.showToast({
          title: '商品已结束',
          icon: 'none'
        })
        return
      }
      // 跳转到商品详情页进行兑换
      this.goToProductDetail(item.id)
    },

    // 跳转到商品详情
    goToProductDetail(productId) {
      uni.redirectTo({
        url: `/points-mall/detail?id=${productId}`
      })
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 160rpx;
}

/* 自定义导航栏 */
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  transition: background-color 0.3s;
}

.navbar-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
}

.navbar-left {
  width: 60rpx;
  display: flex;
  align-items: center;
}

.back-icon {
  font-size: 48rpx;
  font-weight: 300;
  line-height: 1;
}

.navbar-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 20rpx;
}

.navbar-right {
  width: 120rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20rpx;
}

.navbar-icon {
  font-size: 36rpx;
  line-height: 1;
}

/* 商品图片区域 */
.product-image-section {
  position: relative;
  width: 100%;
  height: 600rpx;
  background-color: #fff;
  transition: margin-top 0.3s, padding-top 0.3s;
}

.product-main-image {
  width: 100%;
  height: 100%;
}

/* 价格信息 */
.price-section {
  background-color: #fff;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.can-icon-small {
  width: 40rpx;
  height: 40rpx;
}

.price-amount {
  font-size: 32rpx;
  font-weight: 600;
  color: #ff8c00;
}

/* 商品信息 */
.product-info-section {
  background-color: #fff;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.product-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.5;
  margin-bottom: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 30rpx;
}

/* 权益提供方 */
.provider-section {
  background-color: #fff;
  padding: 30rpx;
  margin-top: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.provider-info {
  margin-bottom: 12rpx;
}

.provider-label {
  font-size: 28rpx;
  color: #666;
}

.provider-name {
  font-size: 28rpx;
  color: #333;
}

.provider-contact {
  font-size: 26rpx;
  color: #999;
}

/* 看了又看 */
.recommend-section {
  background-color: #fff;
  padding: 30rpx;
  margin-top: 20rpx;
  margin-bottom: 40rpx;
  padding-bottom: 40rpx;
}

.recommend-title-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
}

.recommend-title-wrapper .section-title {
  margin: 0 20rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.title-line {
  flex: 1;
  height: 1rpx;
  background-color: #e0e0e0;
}

.recommend-products {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-top: 20rpx;
}

.recommend-item {
  width: calc(50% - 10rpx);
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  border: 1rpx solid #f0f0f0;
  display: flex;
  flex-direction: column;
}

.recommend-image {
  width: 100%;
  height: 320rpx;
  background-color: #f5f5f5;
}

.recommend-content {
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.recommend-item .recommend-title {
  font-size: 26rpx;
  color: #333;
  font-weight: 400;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  line-height: 1.5;
  margin-bottom: 16rpx;
  min-height: 78rpx;
}

.recommend-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.recommend-price {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.recommend-can-icon {
  width: 20rpx;
  height: 20rpx;
}

.recommend-price-text {
  font-size: 24rpx;
  color: #ff8c00;
  font-weight: normal;
}

.recommend-btn {
  padding: 8rpx 24rpx;
  background-color: #ffd700;
  color: #333;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
  white-space: nowrap;
}

.recommend-btn.btn-disabled {
  background-color: #e0e0e0;
  color: #999;
}

/* 罐头不足提示 */
.insufficient-warning {
  background-color: #fff3cd;
  padding: 20rpx 30rpx;
  margin-top: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.warning-text {
  font-size: 28rpx;
  color: #856404;
}

.earn-link {
  font-size: 28rpx;
  color: #007bff;
}

/* 底部操作按钮 */
.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 16rpx 30rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.exchange-btn {
  width: 100%;
  height: 70rpx;
  background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
  border-radius: 35rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 500;
  color: #fff;
}

.exchange-btn.btn-disabled {
  background-color: #e0e0e0;
  color: #999;
}

.spec-popup-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.spec-popup {
  width: 100%;
  background: #fff;
  border-top-left-radius: 24rpx;
  border-top-right-radius: 24rpx;
  padding: 24rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}

.spec-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.spec-popup-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.spec-popup-close {
  font-size: 44rpx;
  color: #999;
}

.spec-popup-product {
  display: flex;
  margin-bottom: 24rpx;
}

.spec-popup-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  margin-right: 16rpx;
}

.spec-popup-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.spec-popup-name {
  font-size: 28rpx;
  color: #333;
}

.spec-popup-price {
  font-size: 28rpx;
  color: #ff6b35;
  font-weight: 600;
}

.spec-block, .quantity-row {
  margin-bottom: 22rpx;
}

.spec-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.spec-option {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 140rpx;
  height: 56rpx;
  border-radius: 28rpx;
  padding: 0 24rpx;
  font-size: 24rpx;
}

.spec-option.selected {
  background: #fff4df;
  color: #ff8c00;
  border: 1rpx solid #ffd28a;
}

.qty-box {
  display: inline-flex;
  align-items: center;
  border: 1rpx solid #eee;
  border-radius: 10rpx;
  overflow: hidden;
}

.qty-btn {
  width: 64rpx;
  height: 56rpx;
  text-align: center;
  line-height: 56rpx;
  font-size: 34rpx;
  color: #333;
  background: #f8f8f8;
}

.qty-num {
  min-width: 80rpx;
  text-align: center;
  font-size: 28rpx;
  color: #333;
}

.spec-popup-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10rpx;
}

.spec-total {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

.spec-confirm {
  width: 220rpx;
  height: 68rpx;
  border-radius: 34rpx;
  background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
  color: #fff;
  text-align: center;
  line-height: 68rpx;
  font-size: 28rpx;
  font-weight: 600;
}
</style>

