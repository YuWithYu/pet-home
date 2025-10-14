<template>
  <view class="shop-container">
    <!-- 搜索栏 -->
    <view class="search-bar bg-white">
      <view class="search-input flex items-center" @click="onSearchTap">
        <view class="search-icon">
          <image src="/static/images/搜索.svg" mode="aspectFit" class="search-icon-image" />
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
          <view class="goods-grid">
            <view
              class="goods-item"
              v-for="goods in goodsList"
              :key="goods.id"
              @click="onGoodsTap(goods)"
            >
              <view class="goods-image">
                <image :src="goods.pic" mode="aspectFill" />
                <view class="goods-tag" v-if="goods.tag">{{ goods.tag }}</view>
              </view>
              <view class="goods-info">
                <view class="goods-name">{{ goods.name }}</view>
                <view class="goods-desc" v-if="goods.description">{{ goods.description }}</view>
                <view class="goods-price">
                  <text class="price-current">{{ goods.price }}</text>
                  <text class="price-original" v-if="goods.originalPrice">¥{{ goods.originalPrice }}</text>
                </view>
                <view class="goods-sales" v-if="goods.sales">
                  已售 {{ goods.sales }} 件
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
              <image src="/static/images/暂无商品.svg" mode="aspectFit" class="empty-icon-image" />
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

  methods: {
    loadCategories() {
      this.loading = true
      this.$api.getAllCategories().then(res => {
        if (res.code === 0 && res.data) {
          this.categories = res.data
          this.selectedCategory = res.data[0]
          this.onCategoryTap(res.data[0])
        }
      }).catch(() => {
        this.categories = [
          { id: 1, name: '宠物食品' },
          { id: 2, name: '宠物用品' },
          { id: 3, name: '宠物玩具' },
          { id: 4, name: '医疗服务' },
          { id: 5, name: '美容护理' },
          { id: 6, name: '宠物寄养' }
        ]
        this.selectedCategory = this.categories[0]
        this.onCategoryTap(this.categories[0])
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

      this.$api.getGoodsList(params).then(res => {
        if (res.code === 0 && res.data) {
          const { goods, total, pages } = res.data
          const formattedGoods = goods.map(item => {
            let picUrl = item.pic || item.image
            // 如果图片路径以 /upload/ 开头，拼接完整的后端URL
            if (picUrl && picUrl.startsWith('/upload/')) {
              picUrl = 'https://localhost:8080' + picUrl
            }
            return {
              ...item,
              pic: picUrl,
              price: util.formatPrice(item.price),
              sales: item.sales || Math.floor(Math.random() * 1000) + 100
            }
          })

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
        url: `/pages/goods/detail?id=${goods.id}`
      })
    },

    onSearchTap() {
      uni.navigateTo({
        url: '/pages/search/index'
      })
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
  padding: 5rpx 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.search-input {
  background-color: #f5f5f5;
  border-radius: 30rpx;
  padding: 12rpx 20rpx;
  width: 100%;
}

.search-icon {
  margin-right: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .search-icon-image {
    width: 28rpx;
    height: 28rpx;
  }
}

.search-text {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}


.main-content {
  display: flex;
  height: calc(100vh - 120rpx);
}

/* 左侧分类导航 */
.category-sidebar {
  width: 200rpx;
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
  padding: 30rpx 20rpx;
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
      height: 40rpx;
      background-color: #ff6b35;
    }
  }
}

.category-name {
  font-size: 28rpx;
  color: #333;
  text-align: center;
  line-height: 1.4;
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

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 20rpx;
}

.goods-item {
  background-color: white;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.goods-image {
  position: relative;
  width: 100%;
  height: 280rpx;

  image {
    width: 100%;
    height: 100%;
  }
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
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
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

.goods-sales {
  font-size: 24rpx;
  color: #999;
}

.load-more {
  padding: 40rpx 20rpx;
  text-align: center;
}

.load-more-btn {
  display: inline-block;
  padding: 20rpx 60rpx;
  background-color: #f8f8f8;
  color: #666;
  font-size: 26rpx;
  border-radius: 40rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  color: #999;
}

.empty-icon {
  margin-bottom: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .empty-icon-image {
    width: 120rpx;
    height: 120rpx;
  }
}

.empty-text {
  font-size: 28rpx;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 40rpx;
  color: #999;
}

.loading-spinner {
  width: 60rpx;
  height: 60rpx;
  border: 4rpx solid #f3f3f3;
  border-top: 4rpx solid #ff6b35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 40rpx;
}

.loading-text {
  font-size: 28rpx;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
