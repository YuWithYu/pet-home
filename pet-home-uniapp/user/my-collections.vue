<template>
  <view class="collections-page">
    <!-- 标签切换（使用默认导航栏，标签紧贴导航栏下方） -->
    <view class="tabs-container">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === '动态' }"
        @click="switchTab('动态')"
      >
        <text>动态</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === '文章' }"
        @click="switchTab('文章')"
      >
        <text>文章</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === '商品' }"
        @click="switchTab('商品')"
      >
        <text>商品</text>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="content-container">
      <!-- 加载中 -->
      <view v-if="loading" class="loading-container">
        <text>加载中...</text>
      </view>
      
      <!-- 空状态 -->
      <view v-else-if="!loading && currentTab !== '商品' && posts.length === 0" class="empty-container">
        <view class="empty-illustration">
          <text class="empty-icon">📦</text>
        </view>
        <text class="empty-text">这里空空如也</text>
        <text class="empty-desc">愿你每一段坚守,都不负岁月。</text>
      </view>
      
      <!-- 商品收藏空状态 -->
      <view v-else-if="!loading && currentTab === '商品' && products.length === 0" class="empty-container">
        <view class="empty-illustration">
          <text class="empty-icon">📦</text>
        </view>
        <text class="empty-text">这里空空如也</text>
        <text class="empty-desc">愿你每一段坚守,都不负岁月。</text>
      </view>
      
      <!-- 文章/动态列表（文章 tab 含帖子+专题） -->
      <view v-else-if="currentTab !== '商品'" class="posts-list">
        <!-- 专题卡片（文章 tab 下） -->
        <view
          v-for="(item, index) in posts"
          :key="item.itemType === 'topic' ? 't-' + item.id : 'p-' + (item.id || index)"
          class="post-item"
          @click="goToPostDetailByIndex(index)"
        >
          <template v-if="item.itemType === 'topic'">
            <view class="post-header topic-header">
              <image
                v-if="item.coverImage"
                class="topic-cover"
                :src="getImageUrl(item.coverImage)"
                mode="aspectFill"
                @error="handleImageError"
              />
              <view class="topic-info">
                <text class="topic-title">{{ item.title || '宠物专题' }}</text>
                <text class="post-time">{{ formatTime(item.createTime) }}</text>
              </view>
              <view class="collect-btn" @click.stop="uncollectItemByIndex(index)">
                <text class="collect-icon">⭐</text>
              </view>
            </view>
          </template>
          <template v-else>
            <!-- 帖子卡片 -->
            <view class="post-header">
              <image 
                :src="getImageUrl(item.userAvatar || '/static/images/garfield-default-avatar.png')" 
                mode="aspectFill" 
                class="user-avatar"
                @error="handleImageError"
              />
              <view class="user-info">
                <text class="user-name">{{ item.userName || '用户' }}</text>
              </view>
              <view class="collect-btn" @click.stop="uncollectItemByIndex(index)">
                <text class="collect-icon">⭐</text>
              </view>
            </view>
            <view class="post-content">
              <text class="post-text" v-if="item.content || item.title">{{ item.content || item.title }}</text>
              <view class="post-images" v-if="getPostImages(item).length > 0">
                <image 
                  v-for="(img, imgIndex) in getPostImages(item).slice(0, 3)" 
                  :key="imgIndex"
                  :src="getImageUrl(img)" 
                  mode="aspectFill"
                  class="post-image"
                  @error="handleImageError"
                />
              </view>
            </view>
            <view class="post-footer">
              <text class="post-time">{{ formatTime(item.createTime) }}</text>
            </view>
          </template>
        </view>
      </view>
      
      <!-- 商品列表 -->
      <view v-else-if="currentTab === '商品'" class="products-list">
        <view 
          class="product-item" 
          v-for="(product, index) in products" 
          :key="product.id || index"
          @click="goToProductDetail(product.id || product.productId)"
        >
          <!-- 商品图片 -->
          <view class="product-image-wrapper">
            <image 
              :src="getProductImageUrl(product.image || product.coverImage || product.images)" 
              mode="aspectFill"
              class="product-image"
              @error="handleImageError"
            />
            <view class="collect-btn" @click.stop="uncollectProduct(product.id || product.productId)">
              <text class="collect-icon">⭐</text>
            </view>
          </view>
          
          <!-- 商品信息 -->
          <view class="product-info">
            <text class="product-name">{{ product.name || product.title || '商品名称' }}</text>
            <view class="product-price-row">
              <text class="product-price">¥{{ product.price || product.salePrice || '0.00' }}</text>
              <text class="product-original-price" v-if="product.originalPrice && product.originalPrice > product.price">
                ¥{{ product.originalPrice }}
              </text>
            </view>
            <text class="product-sales" v-if="product.saleCount || product.sale">
              已售{{ formatSales(product.saleCount || product.sale || 0) }}件
            </text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'
import { mapGetters } from 'vuex'

export default {
  name: 'MyCollections',
  data() {
    return {
      currentTab: '动态',
      posts: [],
      products: [], // 商品收藏列表
      loading: false,
      page: 1,
      size: 10,
      hasMore: true,
      userId: null,
      lastCollectionsLoadTime: 0
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn']),
    /** 与预约页一致：优先 Vuex，再本地，避免仅 storage 未同步时 userId 为空导致列表永远不请求 */
    resolvedUserId() {
      const u = this.userInfo
      if (u && (u.id != null || u.uid != null)) {
        const id = u.id != null ? u.id : u.uid
        if (id !== '' && id !== undefined) return id
      }
      try {
        const s = uni.getStorageSync('userInfo')
        if (s && typeof s === 'object' && (s.id != null || s.uid != null)) {
          return s.id != null ? s.id : s.uid
        }
      } catch (e) {}
      const raw = uni.getStorageSync('userId')
      return raw != null && raw !== '' ? raw : null
    }
  },
  
  onLoad() {
    // 立即隐藏系统级加载提示
    this.hideAllLoading()
    
    this.userId = this.resolvedUserId
    if (this.userId == null || this.userId === '') {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
      return
    }
    
    this.loadCollections()
  },
  
  onReady() {
    // 页面渲染完成后再次隐藏加载提示
    this.$nextTick(() => {
      this.hideAllLoading()
      setTimeout(() => {
        this.hideAllLoading()
      }, 50)
      setTimeout(() => {
        this.hideAllLoading()
      }, 100)
    })
  },
  
  onShow() {
    // 显示时也隐藏加载提示
    this.hideAllLoading()
    this.userId = this.resolvedUserId
    // 页面显示时静默刷新：节流 15 秒，不显示全局「加载中」，避免从其他页返回时反复弹 loading
    if (this.userId != null && this.userId !== '') {
      const now = Date.now()
      const throttleMs = 15 * 1000
      if (now - (this.lastCollectionsLoadTime || 0) < throttleMs && (this.lastCollectionsLoadTime || 0) > 0) {
        return
      }
      this.page = 1
      this.posts = []
      this.products = []
      this.hasMore = true
      if (this.currentTab === '商品') {
        this.loadProductCollections(true)
      } else {
        this.loadCollections(true)
      }
    }
  },
  
  methods: {
    // 隐藏所有加载提示
    hideAllLoading() {
      // #ifdef MP-WEIXIN
      try {
        wx.hideNavigationBarLoading()
        wx.hideLoading()
        wx.setNavigationBarLoading && wx.setNavigationBarLoading({ loading: false })
      } catch (e) {
        // 静默处理错误
      }
      // #endif
      try {
        uni.hideLoading()
        uni.hideNavigationBarLoading && uni.hideNavigationBarLoading()
      } catch (e) {
        // 静默处理错误
      }
    },
    
    goBack() {
      uni.navigateBack()
    },
    
    switchTab(tab) {
      if (this.currentTab === tab) return
      this.userId = this.resolvedUserId
      this.currentTab = tab
      this.page = 1
      this.posts = []
      this.products = []
      this.hasMore = true
      if (tab === '商品') {
        this.loadProductCollections()
      } else {
        this.loadCollections()
      }
    },
    
    async loadCollections(silent = false) {
      if (this.loading) return
      // 仅分页追加时受 hasMore 限制；首页刷新须允许（避免无数据后 hasMore=false 永远无法再请求）
      if (this.page > 1 && !this.hasMore) return
      
      this.userId = this.resolvedUserId
      if (!this.userId) {
        if (!silent) uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      
      const showLoading = !silent
      this.loading = true
      try {
        if (this.currentTab === '文章') {
          // 文章 tab：合并「收藏的帖子(文章)」和「收藏的专题」，一起展示
          const [postsRes, topicsRes] = await Promise.all([
            api.getCollectedPosts({
              userId: this.userId,
              page: this.page,
              size: this.size,
              type: '文章',
              showLoading
            }),
            api.getCollectedTopics({
              userId: this.userId,
              page: this.page,
              size: this.size,
              showLoading
            })
          ])
          const newPosts = (postsRes.code === 200 || postsRes.code === 0) && postsRes.data && postsRes.data.posts
            ? postsRes.data.posts.map(p => ({ ...p, itemType: 'post' }))
            : []
          const newTopics = (topicsRes.code === 200 || topicsRes.code === 0) && topicsRes.data && topicsRes.data.topics
            ? topicsRes.data.topics.map(t => ({ ...t, itemType: 'topic' }))
            : []
          const merged = [...newTopics, ...newPosts].sort((a, b) => {
            const ta = a.createTime ? new Date(a.createTime).getTime() : 0
            const tb = b.createTime ? new Date(b.createTime).getTime() : 0
            return tb - ta
          })
          if (this.page === 1) {
            this.posts = merged
          } else {
            this.posts = [...this.posts, ...merged]
          }
          this.hasMore = newPosts.length >= this.size || newTopics.length >= this.size
          if (this.hasMore) this.page++
          this.lastCollectionsLoadTime = Date.now()
        } else {
          const res = await api.getCollectedPosts({
            userId: this.userId,
            page: this.page,
            size: this.size,
            type: this.currentTab === '动态' ? '动态' : null,
            showLoading
          })
          if (res.code === 200 || res.code === 0) {
            const newPosts = res.data && res.data.posts ? res.data.posts : (res.data || [])
            if (this.page === 1) {
              this.posts = newPosts
            } else {
              this.posts = [...this.posts, ...newPosts]
            }
            this.hasMore = newPosts.length >= this.size
            if (this.hasMore) this.page++
            this.lastCollectionsLoadTime = Date.now()
          } else {
            console.error('加载收藏列表失败，响应码:', res.code, '消息:', res.msg)
            if (!silent) uni.showToast({ title: res.msg || '加载失败', icon: 'none' })
          }
        }
      } catch (error) {
        console.error('加载收藏列表失败:', error)
        if (!silent) uni.showToast({ title: '加载失败，请重试', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    
    uncollectItemByIndex(index) {
      if (index < 0 || index >= this.posts.length) return
      const item = this.posts[index]
      if (!item) return
      this.uncollectItem(item)
    },
    async uncollectItem(item) {
      if (!this.userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const isTopic = item.itemType === 'topic'
      const id = item.id
      try {
        uni.showLoading({ title: '取消收藏中...' })
        const res = isTopic
          ? await api.uncollectTopic(id, this.userId)
          : await api.uncollectPost(id, this.userId)
        uni.hideLoading()
        if (res.code === 200 || res.code === 0) {
          this.posts = this.posts.filter(p => !(p.itemType === item.itemType && (p.id === id || p.id === item.id)))
          uni.showToast({ title: '已取消收藏', icon: 'success' })
        } else {
          uni.showToast({ title: res.msg || '取消收藏失败', icon: 'none' })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('取消收藏失败:', error)
        uni.showToast({ title: '取消收藏失败', icon: 'none' })
      }
    },
    async uncollectPost(postId) {
      const item = this.posts.find(p => (p.itemType !== 'topic') && (p.id === postId || p.postId === postId))
      if (item) return this.uncollectItem(item)
      if (!this.userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      try {
        uni.showLoading({ title: '取消收藏中...' })
        const res = await api.uncollectPost(postId, this.userId)
        uni.hideLoading()
        if (res.code === 200 || res.code === 0) {
          this.posts = this.posts.filter(p => p.id !== postId)
          uni.showToast({ title: '已取消收藏', icon: 'success' })
        } else {
          uni.showToast({ title: res.msg || '取消收藏失败', icon: 'none' })
        }
      } catch (error) {
        uni.hideLoading()
        uni.showToast({ title: '取消收藏失败', icon: 'none' })
      }
    },
    
    // 判断帖子是否为视频（与发现页一致，用于跳转到视频详情或图片详情）
    postHasVideo(item) {
      if (!item) return false
      const post = item.post || item
      if (post.videos) {
        try {
          let d = post.videos
          if (typeof d === 'string') d = JSON.parse(d)
          if (Array.isArray(d) && d.length > 0) return true
          if (d && typeof d === 'object' && d.url) return true
        } catch (e) {}
      }
      const images = this.getPostImages(post)
      if (images.length) {
        const exts = ['.mp4', '.mov', '.avi', '.m4v', '.webm', '.3gp']
        return images.some(img => typeof img === 'string' && exts.some(ext => img.toLowerCase().includes(ext)))
      }
      return false
    },

    // 通过索引跳转到帖子或专题详情（与发现页一致：视频跳视频详情，图片跳图片详情）
    goToPostDetailByIndex(index) {
      if (index < 0 || index >= this.posts.length) return
      const item = this.posts[index]
      if (!item) return
      const id = String(item.id || item.postId || item.post_id || '').trim()
      if (!id || id === 'undefined' || id === 'null') {
        uni.showToast({ title: 'ID无效', icon: 'none' })
        return
      }
      if (item.itemType === 'topic') {
        uni.navigateTo({ url: `/pages-community/topic-detail?id=${id}` })
        return
      }
      const likeCount = item.likesCount ?? item.likeCount ?? item.like ?? 0
      const isLiked = !!(item.isLiked || (item.post && item.post.isLiked))
      const hasVideo = this.postHasVideo(item)
      let targetUrl
      if (hasVideo) {
        targetUrl = `/pages-community/post-detail-video?id=${id}&ids=${id}&index=0&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      } else {
        targetUrl = `/pages-community/post-detail-image?id=${id}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      }
      uni.navigateTo({ url: targetUrl })
    },
    
    // 保留原方法以兼容其他地方可能的调用（无 item 时默认跳图片详情，避免黑屏）
    goToPostDetail(postId) {
      if (!postId || postId === 'undefined' || postId === 'null') {
        console.error('帖子ID无效，无法跳转:', postId)
        uni.showToast({ title: '帖子ID无效', icon: 'none' })
        return
      }
      const id = String(postId).trim()
      uni.navigateTo({ url: `/pages-community/post-detail-image?id=${id}` })
    },
    
    getImageUrl(url) {
      if (!url) return '/static/images/garfield-default-avatar.png'
      return util.getImageUrl(url)
    },
    
    getPostImages(post) {
      if (!post) return []
      if (post.images) {
        try {
          const images = typeof post.images === 'string' ? JSON.parse(post.images) : post.images
          return Array.isArray(images) ? images : []
        } catch (e) {
          return []
        }
      }
      if (post.coverImage) {
        return [post.coverImage]
      }
      return []
    },
    
    formatTime(time) {
      if (!time) return ''
      // 使用util.parseDate来兼容iOS日期格式
      const date = util.parseDate(time)
      if (!date) return ''
      const now = new Date()
      const diff = now.getTime() - date.getTime()
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)
      
      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      
      const month = date.getMonth() + 1
      const day = date.getDate()
      return `${month}-${day}`
    },
    
    handleImageError(e) {
      console.error('图片加载失败:', e)
    },
    
    // 加载商品收藏列表；silent 为 true 时不显示全局「加载中」（用于 onShow 静默刷新）
    async loadProductCollections(silent = false) {
      if (this.loading) return
      if (this.page > 1 && !this.hasMore) return
      
      this.userId = this.resolvedUserId
      if (!this.userId) {
        if (!silent) uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      
      const showLoading = !silent
      this.loading = true
      try {
        const res = await api.getCollectedProducts({
          userId: this.userId,
          page: this.page,
          size: this.size,
          showLoading
        })
        
        if (res.code === 200 || res.code === 0) {
          // 尝试多种数据格式：res.data.products, res.data.list, res.data, res.data.data
          let newProducts = []
          if (res.data) {
            if (Array.isArray(res.data)) {
              newProducts = res.data
            } else if (res.data.products && Array.isArray(res.data.products)) {
              newProducts = res.data.products
            } else if (res.data.list && Array.isArray(res.data.list)) {
              newProducts = res.data.list
            } else if (res.data.data && Array.isArray(res.data.data)) {
              newProducts = res.data.data
            } else if (res.data.records && Array.isArray(res.data.records)) {
              newProducts = res.data.records
            }
          }
          
          // 确保是数组
          if (!Array.isArray(newProducts)) {
            newProducts = []
          }
          
          if (this.page === 1) {
            this.products = newProducts
          } else {
            this.products = [...this.products, ...newProducts]
          }
          
          this.hasMore = newProducts.length >= this.size
          if (this.hasMore) {
            this.page++
          }
          this.lastCollectionsLoadTime = Date.now()
        } else {
          // 如果是接口不存在（404或500且包含"collected"错误），说明后端未实现该接口
          const isApiNotImplemented = res.code === 404 || 
            (res.code === 500 && res.msg && (res.msg.includes('collected') || res.msg.includes('NumberFormatException')))
          
          if (isApiNotImplemented) {
            this.products = []
          } else if (!silent) {
            uni.showToast({
              title: res.msg || '加载失败',
              icon: 'none'
            })
          }
        }
      } catch (error) {
        // 检查是否是接口不存在的错误
        const errorMsg = error.message || error.toString() || ''
        const isApiNotImplemented = errorMsg.includes('collected') || 
          errorMsg.includes('NumberFormatException') ||
          errorMsg.includes('404') ||
          (errorMsg.includes('500') && errorMsg.includes('collected'))
        
        if (isApiNotImplemented) {
          this.products = []
        } else {
          uni.showToast({
            title: '加载失败，请重试',
            icon: 'none'
          })
        }
      } finally {
        this.loading = false
      }
    },
    
    // 取消收藏商品
    async uncollectProduct(productId) {
      if (!this.userId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }
      
      try {
        uni.showLoading({ title: '取消收藏中...' })
        const res = await api.uncollectProduct(productId, this.userId)
        uni.hideLoading()
        
        if (res.code === 200 || res.code === 0) {
          // 从列表中移除
          this.products = this.products.filter(p => (p.id || p.productId) !== productId)
          uni.showToast({
            title: '已取消收藏',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: res.msg || '取消收藏失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('取消收藏商品失败:', error)
        uni.showToast({
          title: '取消收藏失败',
          icon: 'none'
        })
      }
    },
    
    // 跳转到商品详情
    goToProductDetail(productId) {
      if (!productId || productId === 'undefined' || productId === 'null') {
        console.error('商品ID无效，无法跳转:', productId)
        uni.showToast({
          title: '商品ID无效',
          icon: 'none'
        })
        return
      }
      
      uni.navigateTo({
        url: `/pages-goods/detail?id=${productId}`
      })
    },
    
    // 获取商品图片URL
    getProductImageUrl(image) {
      if (!image) return '/static/images/placeholder.png'
      // 如果是数组，取第一张
      if (Array.isArray(image) && image.length > 0) {
        return util.getImageUrl(image[0])
      }
      // 如果是字符串数组（JSON格式）
      if (typeof image === 'string' && image.startsWith('[')) {
        try {
          const images = JSON.parse(image)
          if (Array.isArray(images) && images.length > 0) {
            return util.getImageUrl(images[0])
          }
        } catch (e) {
          // 解析失败，返回原值
        }
      }
      return util.getImageUrl(image)
    },
    
    // 格式化销量
    formatSales(count) {
      if (count >= 10000) {
        return (count / 10000).toFixed(1) + '万+'
      }
      return count + ''
    }
  }
}
</script>

<style lang="scss" scoped>
.collections-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.tabs-container {
  /* 勿用 fixed+top:0，部分小程序机型会导致下方内容区高度为 0 或整页空白 */
  position: relative;
  z-index: 10;
  width: 100%;
  background-color: #fff;
  display: flex;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 20rpx 0;
  font-size: 24rpx;
  color: #666;
  position: relative;
  
  &.active {
    color: #ff7849;
    font-weight: 600;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 4rpx;
      background-color: #ff7849;
      border-radius: 2rpx;
    }
  }
}

.content-container {
  min-height: 60vh;
  box-sizing: border-box;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;
}

.empty-illustration {
  margin-bottom: 40rpx;
}

.empty-icon {
  font-size: 120rpx;
  opacity: 0.3;
}

.empty-text {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #ccc;
}

.posts-list {
  padding: 20rpx;
}

.post-item {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.topic-header {
  width: 100%;
  align-items: center;
}

.topic-cover {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.topic-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.topic-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

.collect-btn {
  padding: 10rpx;
}

.collect-icon {
  font-size: 36rpx;
  color: #ffd700;
}

.post-content {
  margin-bottom: 20rpx;
}

.post-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 20rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.post-images {
  display: flex;
  gap: 10rpx;
  flex-wrap: wrap;
}

.post-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 10rpx;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.post-time {
  font-size: 24rpx;
  color: #999;
}

/* 商品列表样式 - 三列小图 */
.products-list {
  padding: 20rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.product-item {
  width: calc((100% - 32rpx) / 3); /* 三列，留出两个 gap */
  background-color: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-sizing: border-box;
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 宽高比，因卡片变小整体图也变小 */
  background-color: #f5f5f5;
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.product-info {
  padding: 12rpx;
}

.product-name {
  font-size: 22rpx;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.product-price-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.product-price {
  font-size: 26rpx;
  color: #ff7849;
  font-weight: 600;
}

.product-original-price {
  font-size: 22rpx;
  color: #999;
  text-decoration: line-through;
}

.product-sales {
  font-size: 22rpx;
  color: #999;
}
</style>

