<template>
  <view class="goods-list-container">
    <!-- 搜索栏 -->
    <view class="search-bar bg-white">
      <view class="search-input flex items-center" @click="onSearchTap">
        <view class="search-icon">🔍</view>
        <input class="search-text" placeholder="搜索商品" disabled @click="onSearchTap" />
      </view>
      <view class="filter-btn" @click="showFilter = true">
        <view class="filter-icon">🔧</view>
      </view>
    </view>

    <!-- 分类标签 -->
    <view class="category-tabs bg-white" v-if="categories.length > 0">
      <scroll-view class="tabs-scroll" scroll-x="true" show-scrollbar="false">
        <view class="tabs-container">
          <view
            :class="['tab-item', { 'active': selectedCategoryId === item.id }]"
            v-for="category in categories"
            :key="category.id"
            @click="onCategoryTap(category.id)"
          >
            {{ category.name }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 排序栏 -->
    <view class="sort-bar bg-white">
      <view class="sort-options">
        <view
              :class="['sort-option', { 'active': sortBy === item.key }]"
          v-for="sortOption in sortOptions"
          :key="sortOption.key"
          @click="onSortTap(sortOption.key)"
        >
          {{ sortOption.name }}
              <view :class="['sort-arrow', { 'up': sortBy === item.key && sortOrder === 'asc', 'down': sortBy === item.key && sortOrder === 'desc' }]"></view>
        </view>
      </view>
    </view>

    <!-- 商品网格 -->
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

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore">
      <view class="load-more-btn" @click="loadMoreGoods">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="goodsList.length === 0 && !loading">
      <view class="empty-icon">📦</view>
      <view class="empty-text">暂无商品</view>
    </view>

    <!-- 筛选弹窗 -->
    <view class="filter-popup" v-if="showFilter">
      <view class="filter-mask" @click="showFilter = false"></view>
      <view class="filter-content">
        <view class="filter-header">
          <view class="filter-title">筛选</view>
          <view class="filter-close" @click="showFilter = false">✕</view>
        </view>

        <view class="filter-body">
          <!-- 价格范围 -->
          <view class="filter-section">
            <view class="filter-section-title">价格范围</view>
            <view class="price-range">
              <input class="price-input" placeholder="最低价" v-model="filterParams.minPrice" />
              <text class="price-separator">-</text>
              <input class="price-input" placeholder="最高价" v-model="filterParams.maxPrice" />
            </view>
          </view>

          <!-- 品牌 -->
          <view class="filter-section">
            <view class="filter-section-title">品牌</view>
            <view class="brand-list">
              <view
                :class="['brand-item', { 'active': filterParams.brand === item.value }]"
                v-for="brand in brandOptions"
                :key="brand.value"
                @click="filterParams.brand = filterParams.brand === brand.value ? '' : brand.value"
              >
                {{ brand.label }}
              </view>
            </view>
          </view>
        </view>

        <view class="filter-footer">
          <u-button @click="resetFilter" plain>重置</u-button>
          <u-button type="primary" @click="applyFilter" style="margin-left: 20rpx;">确定</u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'GoodsList',

  data() {
    return {
      categories: [],
      selectedCategoryId: '',
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
      loadingMore: false,
      showFilter: false,
      filterParams: {
        minPrice: '',
        maxPrice: '',
        brand: ''
      },
      brandOptions: [
        { value: 'royal', label: '皇家' },
        { value: 'hill', label: '希尔斯' },
        { value: 'purina', label: '普瑞纳' }
      ]
    }
  },

  onLoad(options) {
    if (options.categoryId) {
      this.selectedCategoryId = options.categoryId
    }
    this.loadCategories()
    this.loadGoodsList(true)
  },

  methods: {
    loadCategories() {
      this.$api.getAllCategories().then(res => {
        if (res.code === 0 && res.data) {
          this.categories = res.data
        }
      }).catch(() => {
        this.categories = [
          { id: 1, name: '宠物食品' },
          { id: 2, name: '宠物用品' },
          { id: 3, name: '宠物玩具' }
        ]
      })
    },

    onCategoryTap(categoryId) {
      this.selectedCategoryId = categoryId
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
        categoryId: this.selectedCategoryId,
        sortBy: this.sortBy,
        sortOrder: this.sortOrder,
        pageNo: this.pageNo,
        pageSize: this.pageSize,
        ...this.filterParams
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
    },

    resetFilter() {
      this.filterParams = {
        minPrice: '',
        maxPrice: '',
        brand: ''
      }
    },

    applyFilter() {
      this.showFilter = false
      this.pageNo = 1
      this.goodsList = []
      this.hasMore = true
      this.loadGoodsList(true)
    }
  }
}
</script>

<style lang="scss" scoped>
.goods-list-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

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
  margin-right: 20rpx;
}

.search-icon {
  margin-right: 16rpx;
  color: #999;
  font-size: 28rpx;
}

.search-text {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.filter-btn {
  padding: 16rpx;
  border-radius: 8rpx;
  background-color: #f5f5f5;
}

.filter-icon {
  font-size: 32rpx;
  color: #666;
}

.category-tabs {
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 0;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-container {
  display: inline-flex;
  padding: 0 20rpx;
}

.tab-item {
  display: inline-flex;
  align-items: center;
  padding: 20rpx 30rpx;
  font-size: 28rpx;
  color: #666;
  border-bottom: 4rpx solid transparent;

  &.active {
    color: #ff6b35;
    border-bottom-color: #ff6b35;
  }
}

.sort-bar {
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.sort-options {
  display: flex;
  gap: 40rpx;
}

.sort-option {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #666;

  &.active {
    color: #ff6b35;
  }
}

.sort-arrow {
  margin-left: 8rpx;
  width: 0;
  height: 0;
  border-left: 8rpx solid transparent;
  border-right: 8rpx solid transparent;

  &.up {
    border-bottom: 10rpx solid #ff6b35;
  }

  &.down {
    border-top: 10rpx solid #ff6b35;
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 20rpx;
  background-color: white;
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
}

.goods-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
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
  font-size: 120rpx;
  margin-bottom: 40rpx;
}

.empty-text {
  font-size: 28rpx;
}

.filter-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.filter-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
}

.filter-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  border-radius: 20rpx 20rpx 0 0;
  max-height: 80vh;
  overflow: hidden;
}

.filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.filter-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.filter-close {
  font-size: 32rpx;
  color: #999;
}

.filter-body {
  max-height: 60vh;
  overflow-y: auto;
  padding: 30rpx;
}

.filter-section {
  margin-bottom: 40rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.filter-section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.price-input {
  flex: 1;
  padding: 16rpx;
  border: 2rpx solid #ddd;
  border-radius: 8rpx;
  font-size: 28rpx;
}

.price-separator {
  color: #999;
  font-size: 28rpx;
}

.brand-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.brand-item {
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

.filter-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}
</style>
