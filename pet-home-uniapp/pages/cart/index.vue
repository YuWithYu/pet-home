<template>
  <view class="cart-container">
    <!-- 购物车为空 -->
    <view class="empty-cart" v-if="cartList.length === 0">
      <view class="empty-icon">🛒</view>
      <view class="empty-text">购物车还是空的</view>
      <view class="empty-desc">快去添加一些商品吧</view>
      <button class="go-home-btn" @click="goHome">去首页逛逛</button>
    </view>

    <!-- 购物车商品列表 -->
    <view class="cart-list" v-else>
      <!-- 商品列表 -->
      <view
        class="cart-item"
        v-for="(item, index) in cartList"
        :key="item.id"
      >
        <view class="item-checkbox">
          <checkbox :checked="item.selected" @change="onItemSelect(item.id)" />
            <view class="item-info">
              <view class="item-image">
                <image :src="item.pic" mode="aspectFill" />
              </view>
              <view class="item-details">
                <view class="item-name">{{ item.name }}</view>
                <view class="item-spec" v-if="item.spec">{{ item.spec }}</view>
                <view class="item-price">
                  <text class="price-current">{{ item.price }}</text>
                  <text class="price-original" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
                </view>
              </view>
            </view>
          </u-checkbox>
        </view>

        <view class="item-actions">
          <view class="quantity-control">
            <button class="quantity-btn" @click="changeQuantity(item.id, -1)">-</button>
            <input class="quantity-input" type="number" :value="item.quantity" @change="onQuantityChange(item.id, $event)" />
            <button class="quantity-btn" @click="changeQuantity(item.id, 1)">+</button>
          </view>
        </view>
      </view>

      <!-- 购物车底部 -->
      <view class="cart-footer">
        <view class="footer-left">
          <checkbox :checked="allSelected" @change="onSelectAll" />
          <text class="select-all-text">全选</text>
        </view>

        <view class="footer-right">
          <view class="total-info">
            <view class="total-text">
              合计：
              <text class="total-price">{{ formatPrice(totalAmount) }}</text>
            </view>
            <view class="discount-text" v-if="discountAmount > 0">
              优惠：-{{ formatPrice(discountAmount) }}
            </view>
          </view>

          <button
            class="checkout-btn"
            :disabled="!hasSelectedItems"
            @click="onCheckout"
          >
            去结算({{ selectedCount }})
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'
import { util } from '@/common/js/util.js'

export default {
  name: 'Cart',

  data() {
    return {
      cartList: [],
      loading: false,
      pageNo: 1,
      pageSize: 20
    }
  },

  onLoad() {
    this.loadCartData()
  },

  onShow() {
    this.loadCartData()
  },

  onPullDownRefresh() {
    this.loadCartData(() => {
      uni.stopPullDownRefresh()
    })
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn']),
    

    // 全选状态
    allSelected: {
      get() {
        return this.cartList.length > 0 && this.cartList.every(item => item.selected)
      },
      set(value) {
        this.cartList.forEach(item => {
          this.$set(item, 'selected', value)
        })
      }
    },

    // 已选择商品
    selectedItems() {
      return this.cartList.filter(item => item.selected)
    },

    // 已选择商品数量
    selectedCount() {
      return this.selectedItems.length
    },

    // 是否有选择商品
    hasSelectedItems() {
      return this.selectedCount > 0
    },

    // 总金额
    totalAmount() {
      return this.selectedItems.reduce((total, item) => {
        const price = parseFloat(item.price.replace('¥', ''))
        return total + price * item.quantity
      }, 0)
    },

    // 优惠金额
    discountAmount() {
      return 0 // 这里可以根据促销规则计算
    }
  },

  methods: {
    // 加载购物车数据
    async loadCartData(callback) {
      if (!this.isLoggedIn || !this.userInfo || !this.userInfo.uid) {
        // 如果未登录，显示空购物车
        this.cartList = []
        callback && callback()
        return
      }

      try {
        this.loading = true
        const res = await this.$api.getCartPage(this.pageNo, this.pageSize, this.userInfo.uid)
        
        if (res.code === 0 && res.data) {
          const cartItems = res.data.records || []
          
          // 处理购物车数据
          this.cartList = cartItems.map(item => {
            let picUrl = item.productImage || item.pic || ''
            
            // 使用getImageUrl函数处理图片URL，解决小程序HTTP协议限制问题
            picUrl = this.getImageUrl(picUrl)
            
            return {
              id: item.id,
              productId: item.productId,
              name: item.productName,
              spec: item.specification || '',
              pic: picUrl,
              price: util.formatPrice(item.price || 0),
              originalPrice: item.originalPrice ? util.formatPrice(item.originalPrice) : null,
              quantity: item.quantity || 1,
              selected: false // 默认不选中
            }
          })
        } else {
          this.cartList = []
          uni.showToast({
            title: '加载购物车失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载购物车失败:', error)
        this.cartList = []
        uni.showToast({
          title: '加载购物车失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
        callback && callback()
      }
    },

    // 商品选择
    onItemSelect(itemId) {
      const item = this.cartList.find(item => item.id === itemId)
      if (item) {
        this.$set(item, 'selected', !item.selected)
      }
    },

    // 全选
    onSelectAll() {
      // 这里逻辑已在计算属性中处理
    },

    // 数量变化
    onQuantityChange(itemId, e) {
      const quantity = parseInt(e.detail.value) || 1
      const item = this.cartList.find(item => item.id === itemId)
      if (item) {
        this.$set(item, 'quantity', Math.max(1, Math.min(99, quantity)))
      }
    },

    // 改变数量
    async changeQuantity(itemId, delta) {
      const item = this.cartList.find(item => item.id === itemId)
      if (item) {
        const newQuantity = Math.max(1, Math.min(99, item.quantity + delta))
        
        // 更新本地状态
        this.$set(item, 'quantity', newQuantity)
        
        // 同步到后端
        try {
          await this.$api.updateCartItem({
            id: itemId,
            quantity: newQuantity
          })
        } catch (error) {
          console.error('更新购物车数量失败:', error)
          uni.showToast({
            title: '更新失败',
            icon: 'none'
          })
          // 恢复原来的数量
          this.$set(item, 'quantity', item.quantity - delta)
        }
      }
    },

    // 去结算
    onCheckout() {
      if (this.selectedCount === 0) {
        util.showToast('请选择要结算的商品', 'none')
        return
      }

      uni.navigateTo({
        url: '/pages/order/checkout'
      })
    },

    // 去首页
    goHome() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    },

    // 格式化价格
    formatPrice(price) {
      return util.formatPrice(price)
    },

    // 处理图片URL，解决小程序HTTP协议限制问题
    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
    }
  }
}
</script>

<style lang="scss" scoped>
.cart-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

/* 空购物车 */
.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 60rpx;
  color: #999;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 40rpx;
}

.empty-text {
  font-size: 32rpx;
  margin-bottom: 20rpx;
}

.empty-desc {
  font-size: 26rpx;
  margin-bottom: 60rpx;
}

.go-home-btn {
  background-color: #ff6b35;
  color: white;
  border: none;
  padding: 24rpx 60rpx;
  border-radius: 12rpx;
  font-size: 28rpx;

  &:active {
    background-color: #e55a2b;
  }
}

/* 购物车商品列表 */
.cart-list {
  background-color: white;
}

.cart-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.item-checkbox {
  flex: 1;
}

.item-info {
  display: flex;
  align-items: center;
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  overflow: hidden;
  margin-right: 20rpx;
  flex-shrink: 0;

  image {
    width: 100%;
    height: 100%;
  }
}

.item-details {
  flex: 1;
}

.item-name {
  font-size: 30rpx;
  color: #333;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-spec {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.item-price {
  display: flex;
  align-items: center;
}

.price-current {
  font-size: 28rpx;
  color: #ff6b35;
  font-weight: bold;
}

.price-original {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 12rpx;
}

.item-actions {
  margin-left: 30rpx;
}

/* 数量控制器 */
.quantity-control {
  display: flex;
  align-items: center;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
}

.quantity-btn {
  width: 60rpx;
  height: 60rpx;
  background-color: #f8f8f8;
  border: none;
  font-size: 32rpx;
  color: #666;

  &:active {
    background-color: #eee;
  }

  &:first-child {
    border-right: 2rpx solid #ddd;
  }

  &:last-child {
    border-left: 2rpx solid #ddd;
  }
}

.quantity-input {
  width: 80rpx;
  height: 60rpx;
  text-align: center;
  border: none;
  font-size: 28rpx;
  background-color: white;
}

/* 购物车底部 */
.cart-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  background-color: white;
  border-top: 1rpx solid #f0f0f0;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.total-info {
  text-align: right;
}

.total-text {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
}

.total-price {
  font-size: 36rpx;
  color: #ff6b35;
  font-weight: bold;
}

.discount-text {
  font-size: 24rpx;
  color: #52c41a;
}

.select-all-text {
  margin-left: 16rpx;
  font-size: 28rpx;
  color: #333;
}

.checkout-btn {
  background-color: #ff6b35;
  color: white;
  border: none;
  padding: 20rpx 40rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  min-width: 200rpx;

  &:disabled {
    background-color: #f8f8f8;
    color: #999;
  }

  &:active:not(:disabled) {
    background-color: #e55a2b;
  }
}
</style>
