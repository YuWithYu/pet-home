<template>
  <view class="shop-container">
    <!-- 搜索栏 -->
    <view class="search-bar bg-white">
      <view class="search-input flex items-center" @click="onSearchTap">
        <view class="search-icon">
          <image src="/static/images/search.svg" mode="aspectFit" class="search-icon-image" />
        </view>
        <input class="search-text" placeholder="请输入搜索关键词" disabled @click="onSearchTap" />
      </view>
    </view>

    <!-- 主要内容区域 -->
    <view class="main-content">
      <!-- 左侧分类导航 -->
      <view class="category-sidebar">
        <scroll-view class="category-scroll" scroll-y="true" show-scrollbar="false">
          <view
            :class="['category-item', { 'active': selectedCategory && selectedCategory.id === category.id }]"
            v-for="category in categories"
            :key="category.id"
            @click="onCategoryTap(category)"
          >
            <view class="category-name">{{ category.name }}</view>
          </view>
        </scroll-view>
      </view>

      <!-- 右侧商品列表 -->
      <view class="goods-content">

        <scroll-view class="goods-scroll" scroll-y="true" @scrolltolower="loadMoreGoods">
          <view class="goods-list">
            <view
              class="goods-item"
              v-for="goods in goodsList"
              :key="goods.id"
              @click="onGoodsTap(goods)"
            >
              <view class="goods-image">
                <image :src="getImageUrl(goods.pic || goods.image || goods.imageUrl)" mode="aspectFill" @error="onGoodsImageError(goods)" />
                <view class="goods-tag" v-if="goods.tag">{{ goods.tag }}</view>
              </view>
              <view class="goods-info">
                <view class="goods-name">{{ goods.name }}</view>
                <view class="goods-desc" v-if="goods.description">{{ goods.description }}</view>
                <view class="goods-sales">已售{{ goods.sales }}</view>
                <view class="goods-price">
                  <text class="price-current">{{ goods.price }}</text>
                  <text class="price-original" v-if="goods.originalPrice">¥{{ goods.originalPrice }}</text>
                </view>
              </view>
            </view>
          </view>

          <view class="load-more" v-if="hasMore">
            <view class="load-more-btn" @click="loadMoreGoods">
              {{ loadingMore ? '加载中...' : '加载更多' }}
            </view>
          </view>

          <view class="empty-state" v-if="goodsList.length === 0 && !loading">
            <view class="empty-icon">
              <image src="/static/images/no-products.svg" mode="aspectFit" class="empty-icon-image" />
            </view>
            <view class="empty-text">暂无商品</view>
          </view>
        </scroll-view>
      </view>
    </view>

    <view class="loading-state" v-if="loading">
      <view class="loading-spinner"></view>
      <view class="loading-text">加载中...</view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'Category',

  data() {
    return {
      categories: [],
      selectedCategory: null,
      goodsList: [],
      sortOptions: [
        { key: 'default', name: '综合' },
        { key: 'price', name: '价格' },
        { key: 'sales', name: '销量' },
        { key: 'new', name: '新品' }
      ],
      sortBy: 'default',
      sortOrder: 'desc',
      pageNo: 1,
      pageSize: 10,
      hasMore: true,
      loading: true,
      loadingMore: false
    }
  },

  onLoad() {
    this.loadCategories()
  },

  onShow() {
    if (util.redirectStaffToMineIfNeeded()) return
  },

  methods: {
    loadCategories() {
      this.loading = true
      api.getAllCategories().then(res => {
        if ((res.code === 200 || res.code === 0) && res.data && Array.isArray(res.data) && res.data.length > 0) {
          this.categories = res.data
          this.selectedCategory = res.data[0]
          this.onCategoryTap(res.data[0])
        } else {
          this.categories = []
          uni.showToast({
            title: '暂无分类数据',
            icon: 'none'
          })
        }
      }).catch(err => {
        console.error('加载分类失败:', err)
        this.categories = []
        uni.showToast({
          title: '加载分类失败',
          icon: 'none'
        })
      }).finally(() => {
        this.loading = false
      })
    },

    onCategoryTap(category) {
      this.selectedCategory = category
      this.pageNo = 1
      this.goodsList = []
      this.hasMore = true
      this.loadGoodsList(true)
    },

    onSortTap(sortBy) {
      if (this.sortBy === sortBy) {
        this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.sortBy = sortBy
        this.sortOrder = 'desc'
      }
      this.pageNo = 1
      this.goodsList = []
      this.hasMore = true
      this.loadGoodsList(true)
    },

    loadGoodsList(reset = false) {
      if (reset) {
        this.loading = true
        this.pageNo = 1
        this.goodsList = []
      } else {
        this.loadingMore = true
      }

      const params = {
        categoryId: this.selectedCategory ? this.selectedCategory.id : '',
        sortBy: this.sortBy,
        sortOrder: this.sortOrder,
        pageNo: this.pageNo,
        pageSize: this.pageSize
      }

      api.getGoodsList(params).then(res => {
        if ((res.code === 200 || res.code === 0) && res.data) {
          const { goods, total, pages } = res.data
          const formattedGoods = (goods && Array.isArray(goods)) ? goods.map(item => {
            // 获取图片URL，优先使用pic，然后是image，最后是imageUrl
            let picUrl = item.pic || item.image || item.imageUrl || ''
            
            // 使用getImageUrl函数处理图片URL，解决小程序HTTP协议限制问题
            picUrl = this.getImageUrl(picUrl)
            
            return {
              ...item,
              pic: picUrl,
              price: util.formatPrice(item.price),
              sales: item.sale ?? item.sales ?? item.saleCount ?? 0
            }
          }) : []

          this.goodsList = reset ? formattedGoods : [...this.goodsList, ...formattedGoods]
          this.hasMore = this.pageNo < pages
          this.pageNo++
        }
      }).finally(() => {
        this.loading = false
        this.loadingMore = false
      })
    },

    loadMoreGoods() {
      if (this.hasMore && !this.loadingMore) {
        this.loadGoodsList(false)
      }
    },

    onGoodsTap(goods) {
      uni.navigateTo({
        url: `/pages-goods/detail?id=${goods.id}`
      })
    },

    onSearchTap() {
      uni.navigateTo({
        url: '/pages-goods/search'
      })
    },

    // 处理图片URL，解决小程序HTTP协议限制问题
    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
    },

    // 商品图加载失败时置空 pic，由 getImageUrl 显示默认图
    onGoodsImageError(goods) {
      const idx = this.goodsList.findIndex(g => g.id === goods.id)
      if (idx !== -1) {
        this.$set(this.goodsList[idx], 'pic', '')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.shop-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.search-bar {
  padding: 4rpx 16rpx 8rpx;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.05);
}

.search-input {
  background-color: #f5f5f5;
  border-radius: 24rpx;
  padding: 8rpx 16rpx;
  width: 100%;
}

.search-icon {
  margin-right: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .search-icon-image {
    width: 24rpx;
    height: 24rpx;
  }
}

.search-text {
  flex: 1;
  font-size: 24rpx;
  color: #333;
}


.main-content {
  display: flex;
  height: calc(100vh - 96rpx);
}

/* 左侧分类导航 */
.category-sidebar {
  width: 168rpx;
  background-color: white;
  border-right: 1rpx solid #f0f0f0;
}

.category-scroll {
  height: 100%;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 12rpx;
  border-bottom: 1rpx solid #f0f0f0;
  position: relative;

  &.active {
    background-color: #fff7e6;
    color: #ff6b35;
    
    &::after {
      content: '';
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4rpx;
      height: 32rpx;
      background-color: #ff6b35;
    }
  }
}

.category-name {
  font-size: 24rpx;
  color: #333;
  text-align: center;
  line-height: 1.35;
}

.category-item.active .category-name {
  color: #ff6b35;
  font-weight: bold;
}

/* 右侧商品内容 */
.goods-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}


.goods-scroll {
  flex: 1;
  background-color: white;
}

.goods-list {
  padding: 12rpx;
  background-color: #fff;
}

.goods-item {
  display: flex;
  align-items: stretch;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  background-color: white;

  &:last-child {
    border-bottom: none;
  }
}

.goods-image {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  flex-shrink: 0;
  border-radius: 8rpx;
  overflow: hidden;
  background-color: #f5f5f5;

  image {
    width: 100%;
    height: 100%;
  }
}

.goods-tag {
  position: absolute;
  top: 6rpx;
  left: 6rpx;
  background-color: #ff6b35;
  color: white;
  font-size: 18rpx;
  padding: 2rpx 8rpx;
  border-radius: 16rpx;
}

.goods-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0 16rpx;
}

.goods-name {
  font-size: 26rpx;
  color: #333;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goods-desc {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-sales {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
}

.goods-price {
  display: flex;
  align-items: baseline;
  margin-top: 8rpx;
}

.price-current {
  font-size: 28rpx;
  color: #ff6b35;
  font-weight: bold;
}

.price-original {
  font-size: 22rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 12rpx;
}

.load-more {
  padding: 24rpx 16rpx;
  text-align: center;
}

.load-more-btn {
  display: inline-block;
  padding: 14rpx 48rpx;
  background-color: #f8f8f8;
  color: #666;
  font-size: 22rpx;
  border-radius: 32rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
  color: #999;
}

.empty-icon {
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .empty-icon-image {
    width: 80rpx;
    height: 80rpx;
  }
}

.empty-text {
  font-size: 24rpx;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
  color: #999;
}

.loading-spinner {
  width: 48rpx;
  height: 48rpx;
  border: 3rpx solid #f3f3f3;
  border-top: 3rpx solid #ff6b35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24rpx;
}

.loading-text {
  font-size: 24rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
