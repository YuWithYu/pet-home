<template>
  <view class="search-page">
    <!-- 顶部搜索栏 -->
    <view class="search-header">
      <view class="search-input-box">
        <input 
          class="search-input" 
          v-model="searchKeyword" 
          :placeholder="searchMode === 'community' ? '搜索帖子内容' : '搜索宠物用品、医疗服务等'"
          @confirm="handleSearch"
          @input="onInputChange"
          focus
        />
      </view>
    </view>

    <!-- 搜索内容区 -->
    <view class="search-content">
      <!-- 热门搜索 -->
      <view class="hot-search" v-if="searchResults.length === 0 && !hasSearched">
        <view class="section-title">热门搜索</view>
        <view class="hot-tags">
          <view class="hot-tag" v-for="tag in hotSearches" :key="tag" @click="searchByTag(tag)">
            {{ tag }}
          </view>
        </view>
      </view>

      <!-- 搜索历史 -->
      <view class="search-history" v-if="searchResults.length === 0 && !hasSearched && searchHistory.length > 0">
        <view class="section-title">
          <text>搜索历史</text>
          <text class="clear-history" @click="clearHistory">清除</text>
        </view>
        <view class="history-list">
          <view class="history-item" v-for="(item, index) in searchHistory" :key="index" @click="searchByTag(item)">
            {{ item }}
          </view>
        </view>
      </view>

      <!-- 搜索结果 -->
      <view class="search-results" v-if="hasSearched">
        <view v-if="isEmptyResult" class="empty-result">
          <text class="empty-text">暂无搜索结果</text>
        </view>
        <view v-else>
          <!-- 商品结果 -->
          <view class="result-section" v-if="productResults.length > 0">
            <view class="section-title">商品</view>
            <view class="product-list">
              <view class="product-item" v-for="product in productResults" :key="product.id" @click="goToProductDetail(product.id)">
                <image class="product-image" :src="getProductImage(product)" mode="aspectFill" />
                <view class="product-info">
                  <text class="product-name">{{ product.name }}</text>
                  <text class="product-price">¥{{ product.price }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 服务结果 -->
          <view class="result-section" v-if="serviceResults.length > 0">
            <view class="section-title">服务</view>
            <view class="service-list">
              <view class="service-item" v-for="service in serviceResults" :key="service.id">
                <view class="service-info">
                  <text class="service-name">{{ service.storeName }}</text>
                  <text class="service-desc">{{ service.description }}</text>
                  <text class="service-address">{{ service.address }}</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 帖子结果 -->
          <view class="result-section" v-if="postResults.length > 0">
            <view class="section-title">帖子</view>
            <view class="post-list">
              <view class="post-item" v-for="post in postResults" :key="post.id" @click="goToPostDetail(post)">
                <image v-if="getPostImage(post)" class="post-image" :src="getPostImage(post)" mode="aspectFill" />
                <view class="post-info">
                  <text class="post-content">{{ post.content || post.title }}</text>
                  <text class="post-author">{{ post.userName || '用户' }} · {{ formatDate(post.createTime) }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  data() {
    return {
      searchKeyword: '',
      hasSearched: false,
      searchResults: [],
      productResults: [],
      serviceResults: [],
      postResults: [],
      searchHistory: [],
      hotSearches: [],
      searchMode: 'home' // home: 首页搜索, community: 社区搜索
    }
  },

  onLoad(options) {
    // 按当前用户加载搜索历史（新号/换号不会看到别人的历史）
    this.loadSearchHistory()
    
    // 确定搜索模式（从哪个页面来的）
    this.searchMode = options.mode || 'home'
    
    // 加载热搜关键词
    this.loadHotKeywords()
    
    // 如果有传入关键词，直接搜索
    if (options.keyword) {
      this.searchKeyword = decodeURIComponent(options.keyword)
      this.handleSearch()
    }
  },

  computed: {
    isEmptyResult() {
      if (this.searchMode === 'community') {
        return this.postResults.length === 0
      } else {
        return this.productResults.length === 0 && this.serviceResults.length === 0
      }
    }
  },

  methods: {
    onInputChange(e) {
      this.searchKeyword = e.detail.value
    },

    async handleSearch() {
      if (!this.searchKeyword.trim()) {
        return
      }

      this.hasSearched = true

      // 添加到搜索历史
      this.addToHistory(this.searchKeyword)

      // 根据搜索模式调用不同的搜索接口
      if (this.searchMode === 'community') {
        // 社区模式 - 只搜索帖子
        await this.searchCommunity()
      } else {
        // 首页模式 - 搜索商品和服务
        await this.searchHome()
      }
    },

    async searchHome() {
      try {
        const response = await api.searchHome(this.searchKeyword)
        
        if (response && (response.code === 200 || response.code === 0)) {
          // 处理商品图片URL
          const products = response.data?.products || []
          this.productResults = products.map(product => {
            // 处理图片URL
            let picUrl = product.image || product.pic || product.imageUrl || ''
            
            if (picUrl) {
              // 如果路径以 /pages/ 开头，说明路径格式不对，需要修正
              if (picUrl.startsWith('/pages/')) {
                // 尝试从路径中提取正确的图片路径
                // 例如: /pages/search/product/product-xxx.jpg -> /upload/product/product-xxx.jpg
                picUrl = picUrl.replace(/^\/pages\/[^\/]+\//, '/upload/')
              }
              // 如果是相对路径（不以 / 或 http 开头），添加 /upload/ 前缀
              else if (!picUrl.startsWith('/') && !picUrl.startsWith('http://') && !picUrl.startsWith('https://')) {
                picUrl = '/upload/' + picUrl
              }
              
              picUrl = util.getImageUrl(picUrl)
            }
            return {
              ...product,
              image: picUrl,
              pic: picUrl
            }
          })
          this.serviceResults = response.data?.services || []
        }
      } catch (error) {
        console.error('首页搜索失败:', error)
        this.productResults = []
        this.serviceResults = []
      }
    },

    async searchCommunity() {
      try {
        const response = await api.searchCommunity(this.searchKeyword)
        
        if (response && (response.code === 200 || response.code === 0)) {
          // 保留 videos/images 原始结构供跳转判断；仅规范封面用于列表展示
          const posts = response.data || []
          this.postResults = posts.map((post) => {
            let picUrl = post.coverImage || ''
            if (!picUrl && post.images) {
              try {
                if (typeof post.images === 'string') {
                  if (post.images.trim().startsWith('[')) {
                    const arr = JSON.parse(post.images)
                    picUrl = Array.isArray(arr) && arr[0] ? arr[0] : ''
                  } else {
                    picUrl = post.images.split(',')[0].trim() || ''
                  }
                } else if (Array.isArray(post.images) && post.images.length) {
                  picUrl = post.images[0]
                }
              } catch (e) {}
            }
            const displayCover = picUrl ? util.getImageUrl(picUrl) : ''
            return {
              ...post,
              coverImage: displayCover || post.coverImage
            }
          })
        }
      } catch (error) {
        console.error('社区搜索失败:', error)
        this.postResults = []
      }
    },

    async loadHotKeywords() {
      try {
        const response = await api.getHotKeywords()
        if (response && (response.code === 200 || response.code === 0) && response.data) {
          this.hotSearches = response.data
        }
        
        // 如果接口返回空，使用默认值
        if (!this.hotSearches || this.hotSearches.length === 0) {
          this.hotSearches = [
            '猫粮',
            '狗粮',
            '宠物医疗',
            '美容服务',
            '宠物玩具',
            '宠物零食'
          ]
        }
      } catch (error) {
        console.error('加载热搜失败:', error)
        // 使用默认热搜
        this.hotSearches = [
          '猫粮',
          '狗粮',
          '宠物医疗',
          '美容服务',
          '宠物玩具',
          '宠物零食'
        ]
      }
    },

    searchByTag(tag) {
      this.searchKeyword = tag
      this.handleSearch()
    },

    getSearchHistoryKey() {
      const userId = uni.getStorageSync('userId') || 'guest'
      return 'searchHistory_' + userId
    },

    loadSearchHistory() {
      const key = this.getSearchHistoryKey()
      const history = uni.getStorageSync(key)
      if (Array.isArray(history)) {
        this.searchHistory = history
      } else {
        this.searchHistory = []
      }
    },

    addToHistory(keyword) {
      this.searchHistory = this.searchHistory.filter(item => item !== keyword)
      this.searchHistory.unshift(keyword)
      if (this.searchHistory.length > 10) {
        this.searchHistory = this.searchHistory.slice(0, 10)
      }
      uni.setStorageSync(this.getSearchHistoryKey(), this.searchHistory)
    },

    clearHistory() {
      this.searchHistory = []
      uni.removeStorageSync(this.getSearchHistoryKey())
    },

    goToProductDetail(productId) {
      uni.navigateTo({
        url: `/pages-goods/detail?id=${productId}`
      })
    },

    /**
     * 与发现页一致：图文走 post-detail-image，视频走 post-detail-video。
     * 旧版跳 post-detail 在小程序上会黑屏。
     */
    postHasVideo(p) {
      if (!p) return false
      const post = p.post || p
      let v = false
      if (post.videos) {
        try {
          let d = post.videos
          if (typeof d === 'string') d = JSON.parse(d)
          if (Array.isArray(d) && d.length > 0) v = true
          else if (d && typeof d === 'object' && d.url) v = true
        } catch (e) {}
      }
      if (!v && post.images) {
        let imgs = []
        if (typeof post.images === 'string') {
          try {
            imgs = JSON.parse(post.images)
          } catch (e) {
            imgs = [post.images]
          }
        } else if (Array.isArray(post.images)) imgs = post.images
        if (imgs.length) {
          const exts = ['.mp4', '.mov', '.avi', '.m4v', '.webm', '.3gp']
          v = imgs.some((img) => typeof img === 'string' && exts.some((ext) => img.toLowerCase().includes(ext)))
        }
      }
      return v
    },
    getFirstVideoUrlAndCover(item) {
      try {
        let videosData = item.videos || (item.post && item.post.videos)
        if (typeof videosData === 'string') {
          try {
            videosData = JSON.parse(videosData)
          } catch (e) {
            if (videosData.includes('.mp4') || videosData.includes('.mov')) {
              return { url: videosData, cover: item.coverImage || '' }
            }
            return null
          }
        }
        if (Array.isArray(videosData) && videosData.length > 0) {
          const first = videosData[0]
          const url = typeof first === 'string' ? first : first && (first.url || first.src)
          const cover = (first && (first.thumb || first.cover)) || item.coverImage || ''
          return url ? { url, cover } : null
        }
        if (videosData && typeof videosData === 'object' && videosData.url) {
          return {
            url: videosData.url,
            cover: videosData.thumb || videosData.cover || item.coverImage || ''
          }
        }
        const images = item.images
        let list = []
        if (typeof images === 'string') {
          try {
            list = JSON.parse(images)
          } catch (e) {
            list = [images]
          }
        } else if (Array.isArray(images)) {
          list = images
        }
        const videoExt = ['.mp4', '.mov', '.avi', '.m4v']
        const videoUrl = list.find((img) => typeof img === 'string' && videoExt.some((ext) => img.toLowerCase().includes(ext)))
        if (videoUrl) {
          return { url: videoUrl, cover: item.coverImage || '' }
        }
      } catch (e) {}
      return null
    },
    goToPostDetail(post) {
      if (!post) return
      const postId = post.id || post.postId
      if (postId == null || postId === '' || String(postId) === 'undefined') {
        uni.showToast({ title: '帖子信息异常', icon: 'none' })
        return
      }
      const finalPostId = String(postId).trim()
      const likeCount = post.likesCount ?? post.likeCount ?? 0
      const isLiked = !!(post.isLiked ?? post.liked)
      const hasVideo = this.postHasVideo(post)
      let targetUrl
      if (hasVideo) {
        const feed = (this.postResults || []).filter((pp) => this.postHasVideo(pp))
        const videoIds = feed.map((pp) => String(pp.id || pp.postId || '').trim()).filter(Boolean)
        const ids = videoIds.length ? videoIds : [finalPostId]
        let idx = ids.indexOf(finalPostId)
        if (idx < 0) idx = 0
        const firstVideo = this.getFirstVideoUrlAndCover(post)
        if (firstVideo && firstVideo.url) {
          const app = getApp()
          if (!app.globalData) app.globalData = {}
          app.globalData.pendingVideoDetail = app.globalData.pendingVideoDetail || {}
          app.globalData.pendingVideoDetail[finalPostId] = {
            url: firstVideo.url,
            cover: firstVideo.cover || ''
          }
        }
        targetUrl = `/pages-community/post-detail-video?id=${finalPostId}&ids=${ids.join(',')}&index=${idx}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      } else {
        targetUrl = `/pages-community/post-detail-image?id=${finalPostId}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      }
      uni.navigateTo({ url: targetUrl })
    },


    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${month}-${day}`
    },

    // 获取商品图片URL
    getProductImage(product) {
      const picUrl = product.image || product.pic || product.imageUrl || ''
      if (picUrl) {
        return util.getImageUrl(picUrl)
      }
      return ''
    },

    // 获取帖子图片URL
    getPostImage(post) {
      if (post.coverImage) {
        return util.getImageUrl(post.coverImage)
      }
      if (!post.images) return ''
      try {
        if (typeof post.images === 'string') {
          if (post.images.trim().startsWith('[')) {
            const arr = JSON.parse(post.images)
            if (Array.isArray(arr) && arr[0]) return util.getImageUrl(arr[0])
          }
          return util.getImageUrl(post.images)
        }
        if (Array.isArray(post.images) && post.images[0]) {
          return util.getImageUrl(post.images[0])
        }
      } catch (e) {}
      return ''
    }
  }
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background-color: #f8f8f8;
}

/* 搜索头部 */
.search-header {
  display: flex;
  align-items: center;
  padding: 12rpx 30rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;
}

.search-input-box {
  flex: 1;
  background-color: #f5f5f5;
  border-radius: 28rpx;
  padding: 12rpx 24rpx;
  margin-right: 20rpx;
}

.search-input {
  width: 100%;
  font-size: 26rpx;
  color: #333;
}

.search-cancel {
  font-size: 28rpx;
  color: #ff6b35;
  font-weight: 500;
}

/* 搜索内容 */
.search-content {
  padding: 30rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.clear-history {
  font-size: 24rpx;
  color: #999;
  font-weight: 400;
}

/* 热门搜索 */
.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.hot-tag {
  padding: 12rpx 24rpx;
  background-color: #f5f5f5;
  border-radius: 40rpx;
  font-size: 24rpx;
  color: #666;
}

/* 搜索历史 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.history-item {
  padding: 16rpx 0;
  font-size: 26rpx;
  color: #333;
  border-bottom: 1rpx solid #f0f0f0;
}

/* 搜索结果 */
.empty-result {
  padding: 100rpx 0;
  text-align: center;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.result-section {
  margin-bottom: 40rpx;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.product-item {
  display: flex;
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.product-image {
  width: 160rpx;
  height: 160rpx;
}

.product-info {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  line-height: 1.4;
}

.product-price {
  font-size: 32rpx;
  color: #ff6b35;
  font-weight: 600;
}

.service-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.service-item {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.service-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.service-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.service-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
}

.service-address {
  font-size: 22rpx;
  color: #999;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.post-item {
  display: flex;
  background-color: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.post-image {
  width: 160rpx;
  height: 160rpx;
  flex-shrink: 0;
}

.post-info {
  flex: 1;
  padding: 20rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.post-content {
  font-size: 26rpx;
  color: #333;
  line-height: 1.5;
  margin-bottom: 10rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.post-author {
  font-size: 22rpx;
  color: #999;
}
</style>

