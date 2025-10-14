<template>
  <view class="goods-detail-container">
    <!-- 商品图片轮播 -->
    <swiper class="goods-swiper" indicator-dots="true" autoplay="true" circular="true">
      <swiper-item v-for="image in goods.images" :key="image">
        <image :src="image" mode="aspectFill" />
      </swiper-item>
    </swiper>

    <!-- 商品信息 -->
    <view class="goods-info bg-white">
      <view class="goods-name">{{ goods.name }}</view>
      <view class="goods-desc">{{ goods.description }}</view>
      <view class="goods-price">
        <text class="price-current">{{ goods.price }}</text>
        <text class="price-original" v-if="goods.originalPrice">¥{{ goods.originalPrice }}</text>
      </view>
      <view class="goods-sales" v-if="goods.sales">
        已售 {{ goods.sales }} 件
      </view>
    </view>

    <!-- 商品规格 -->
    <view class="goods-spec bg-white" v-if="goods.specs && goods.specs.length > 0">
      <view class="spec-title">规格选择</view>
      <view class="spec-list">
        <view
          :class="['spec-item', { 'active': selectedSpec.id === item.id }]"
          v-for="spec in goods.specs"
          :key="spec.id"
          @click="selectSpec(spec)"
        >
          {{ spec.name }}
        </view>
      </view>
    </view>

    <!-- 商品详情 -->
    <view class="goods-detail bg-white">
      <view class="detail-title">商品详情</view>
      <view class="detail-content">
        <u-parse :content="goods.detail" v-if="goods.detail"></u-parse>
        <view v-else class="no-detail">暂无详情内容</view>
      </view>
    </view>

    <!-- 评论区域 -->
    <view class="goods-comments bg-white" v-if="goods.comments && goods.comments.length > 0">
      <view class="comments-title">用户评价</view>
      <view class="comments-list">
        <view class="comment-item" v-for="comment in goods.comments" :key="comment.id">
          <view class="comment-header">
            <view class="comment-avatar">👤</view>
            <view class="comment-info">
              <view class="comment-name">{{ comment.userName }}</view>
              <view class="comment-time">{{ comment.time }}</view>
            </view>
          </view>
          <view class="comment-content">{{ comment.content }}</view>
          <view class="comment-images" v-if="comment.images && comment.images.length > 0">
            <image
              v-for="image in comment.images"
              :key="image"
              :src="image"
              mode="aspectFill"
              @click="previewImage(comment.images, image)"
            />
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="goods-footer">
      <view class="footer-actions">
        <view class="action-item" @click="onServiceTap">
          <view class="action-icon">💬</view>
          <view class="action-text">客服</view>
        </view>
        <view class="action-item" @click="onCartTap">
          <view class="action-icon">🛒</view>
          <view class="action-text">购物车</view>
        </view>
        <view class="action-item" @click="addToCart">
          <view class="action-text">加入购物车</view>
        </view>
        <view class="buy-now" @click="buyNow">
          <view class="buy-text">立即购买</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'GoodsDetail',

  data() {
    return {
      goods: {
        id: 1,
        name: '皇家猫粮',
        description: '优质猫粮，营养均衡',
        price: '¥128.00',
        originalPrice: '158.00',
        sales: 256,
        images: [
          '/static/images/product1.jpg',
          '/static/images/product2.jpg'
        ],
        specs: [
          { id: 1, name: '1kg' },
          { id: 2, name: '2kg' },
          { id: 3, name: '5kg' }
        ],
        detail: '<p>商品详情内容</p>',
        comments: [
          {
            id: 1,
            userName: '宠物主人',
            time: '2024-01-15',
            content: '猫粮质量很好，猫咪很喜欢吃',
            images: []
          }
        ]
      },
      selectedSpec: {}
    }
  },

  onLoad(options) {
    if (options.id) {
      this.loadGoodsDetail(options.id)
    }
  },

  methods: {
    loadGoodsDetail(id) {
      // 这里应该调用API获取商品详情
      // 暂时使用模拟数据
      console.log('加载商品详情:', id)
    },

    selectSpec(spec) {
      this.selectedSpec = spec
    },

    onServiceTap() {
      uni.showToast({
        title: '客服功能开发中',
        icon: 'none'
      })
    },

    onCartTap() {
      uni.switchTab({
        url: '/pages/cart/index'
      })
    },

    addToCart() {
      if (Object.keys(this.selectedSpec).length === 0 && this.goods.specs && this.goods.specs.length > 0) {
        uni.showToast({
          title: '请选择规格',
          icon: 'none'
        })
        return
      }

      // 这里调用添加购物车API
      uni.showToast({
        title: '已加入购物车',
        icon: 'success'
      })
    },

    buyNow() {
      if (Object.keys(this.selectedSpec).length === 0 && this.goods.specs && this.goods.specs.length > 0) {
        uni.showToast({
          title: '请选择规格',
          icon: 'none'
        })
        return
      }

      // 跳转到订单确认页面
      uni.navigateTo({
        url: '/pages/order/confirm'
      })
    },

    previewImage(images, current) {
      uni.previewImage({
        urls: images,
        current: current
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.goods-detail-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.goods-swiper {
  height: 600rpx;

  swiper-item {
    height: 100%;

    image {
      width: 100%;
      height: 100%;
    }
  }
}

.goods-info {
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.goods-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 16rpx;
}

.goods-desc {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.goods-price {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.price-current {
  font-size: 40rpx;
  color: #ff6b35;
  font-weight: bold;
}

.price-original {
  font-size: 26rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 16rpx;
}

.goods-sales {
  font-size: 24rpx;
  color: #999;
}

.goods-spec {
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.spec-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.spec-list {
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}

.spec-item {
  padding: 16rpx 32rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #666;

  &.active {
    border-color: #ff6b35;
    color: #ff6b35;
    background-color: #fff7e6;
  }
}

.goods-detail {
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.detail-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.detail-content {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

.no-detail {
  text-align: center;
  color: #999;
  padding: 60rpx 0;
}

.goods-comments {
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.comments-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.comments-list {
  .comment-item {
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .comment-header {
    display: flex;
    align-items: center;
    margin-bottom: 16rpx;
  }

  .comment-avatar {
    width: 60rpx;
    height: 60rpx;
    border-radius: 50%;
    background-color: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16rpx;
  }

  .comment-info {
    flex: 1;
  }

  .comment-name {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 4rpx;
  }

  .comment-time {
    font-size: 24rpx;
    color: #999;
  }

  .comment-content {
    font-size: 28rpx;
    color: #666;
    line-height: 1.5;
    margin-bottom: 16rpx;
  }

  .comment-images {
    display: flex;
    gap: 16rpx;

    image {
      width: 120rpx;
      height: 120rpx;
      border-radius: 8rpx;
    }
  }
}

.goods-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1rpx solid #f0f0f0;
  z-index: 100;
}

.footer-actions {
  display: flex;
  align-items: center;
  height: 100rpx;
}

.action-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10rpx;
  border-right: 1rpx solid #f0f0f0;

  &:last-child {
    border-right: none;
  }
}

.action-icon {
  font-size: 32rpx;
  margin-bottom: 4rpx;
}

.action-text {
  font-size: 24rpx;
  color: #666;
}

.buy-now {
  flex: 2;
  background-color: #ff6b35;
  display: flex;
  align-items: center;
  justify-content: center;
}

.buy-text {
  color: white;
  font-size: 32rpx;
  font-weight: bold;
}
</style>
