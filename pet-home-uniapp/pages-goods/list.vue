<template>
  <view class="container">
    <view class="store-header">
      <view class="store-info-wrapper">
        <view v-if="storeInfo.avatar || storeInfo.logo" class="store-logo-wrapper">
          <image
            class="store-logo"
            :src="getStoreImageUrl(storeInfo.avatar || storeInfo.logo)"
            mode="aspectFill"
            @error="handleStoreImageError"
          />
        </view>
        <view class="store-info-content">
          <view class="store-name">{{ storeInfo.name || '宠物家商品官方旗舰店' }}</view>
          <view class="store-stats">
            <text class="sale-count">已售{{ formatStoreHeaderSales(storeTotalSales) }}</text>
            <template v-if="reviewCount > 0">
              <text class="divider">|</text>
              <view class="rating-wrapper">
                <view class="rating-stars">
                  <text v-for="idx in 5" :key="idx" class="star">{{ getStarDisplay(idx - 1) }}</text>
                </view>
                <text class="rating-score">{{ storeRating }}</text>
              </view>
            </template>
          </view>
        </view>
        <view class="store-actions">
          <view class="action-btn service-btn" @click="handleService">
            <image class="btn-icon-image" src="/static/images/online-consultation.png" mode="aspectFit" />
            <text class="btn-text">咨询</text>
          </view>
        </view>
      </view>
    </view>

    <view class="nav-tabs">
      <view
        class="nav-tab-item"
        :class="{ active: currentTab === 'products' }"
        @click="switchTab('products')"
      >
        <text class="tab-text">全部商品</text>
      </view>
      <view
        class="nav-tab-item"
        :class="{ active: currentTab === 'reviews' }"
        @click="switchTab('reviews')"
      >
        <text class="tab-text">评价({{ formattedReviewCount }})</text>
      </view>
    </view>

    <view v-if="currentTab === 'products'" class="sort-bar">
      <view class="sort-item" :class="{ active: sortType === 'default' }" @click="changeSort('default')">
        <text>默认</text>
      </view>
      <view class="sort-item" :class="{ active: sortType === 'sales' }" @click="changeSort('sales')">
        <text>销量</text>
      </view>
      <view class="sort-item" :class="{ active: sortType === 'new' }" @click="changeSort('new')">
        <text>上新</text>
      </view>
      <view class="sort-item" :class="{ active: sortType === 'price' }" @click="changeSort('price')">
        <text>价格</text>
        <view v-if="sortType === 'price'" class="price-arrows">
          <text class="arrow-up" :class="{ active: priceOrder === 'asc' }" />
          <text class="arrow-down" :class="{ active: priceOrder === 'desc' }" />
        </view>
      </view>
    </view>

    <view v-if="currentTab === 'products'" class="goods-list-section">
      <block v-if="goodsList && goodsList.length > 0">
        <view
          v-for="(goods, index) in goodsList"
          :key="index"
          class="goods-item"
          @click="goToGoodsDetail(goods.id || goods.goodsId)"
        >
          <view class="goods-image-wrapper">
            <image
              class="goods-image"
              :src="getGoodsImageUrl(goods, true)"
              mode="aspectFill"
              lazy-load
              @error="handleImageError($event, goods)"
            />
            <view v-if="goods.banners && goods.banners.length" class="goods-banners">
              <view
                v-for="(banner, bidx) in goods.banners"
                :key="bidx"
                class="goods-banner"
                :class="bidx % 2 === 0 ? 'banner-purple' : 'banner-red'"
              >
                {{ banner }}
              </view>
            </view>
          </view>
          <view class="goods-info">
            <view class="goods-name">{{ goods.name || goods.goodsName }}</view>
            <view v-if="goods.desc || goods.subTitle" class="goods-desc">{{ goods.desc || goods.subTitle }}</view>
            <view v-if="goods.tags && goods.tags.length" class="goods-tags">
              <text v-for="(tag, tidx) in goods.tags" :key="tidx" class="goods-tag">{{ tag }}</text>
            </view>
            <view class="goods-price-row">
              <view class="price-info">
                <text class="price-symbol">¥</text>
                <text class="price-value">{{ goods.price || goods.salePrice || '0.00' }}</text>
              </view>
              <view class="add-cart-btn" @click.stop="addToCart(goods)">
                <text class="add-icon">+</text>
              </view>
            </view>
            <view class="goods-sales">
              已售{{ formatSales(goods.saleCount || goods.sale || 0) }}{{ goods.unit || '件' }}
            </view>
          </view>
        </view>
      </block>
      <view v-else class="empty-goods">
        <text class="empty-text">暂无商品</text>
      </view>
    </view>

    <view v-if="currentTab === 'reviews'" class="reviews-section">
      <view v-if="reviewsList && reviewsList.length" class="reviews-list">
        <view v-for="(review, index) in reviewsList" :key="index" class="review-item">
          <view class="review-header">
            <image class="review-avatar" :src="getReviewImageUrl(review.userAvatar)" mode="aspectFill" />
            <view class="review-user-info">
              <text class="review-username">{{ review.userName || '匿名用户' }}</text>
              <text v-if="review.productName" class="purchase-text">购买了{{ review.productName }}</text>
            </view>
          </view>
          <view class="review-content">
            <text class="review-text" :class="{ expanded: review.expanded }">
              {{ review.comment || review.content || '暂无评价内容' }}
            </text>
            <text
              v-if="!review.expanded && shouldShowExpandBtn(review)"
              class="expand-btn"
              @click="expandReview(review)"
            >
              展开
            </text>
          </view>
          <view
            v-if="getReviewImages(review).length || getReviewVideos(review).length"
            class="review-images"
          >
            <image
              v-for="(img, imgIndex) in getReviewImages(review).slice(0, 3)"
              :key="imgIndex"
              class="review-image"
              :src="getReviewImageUrl(img)"
              mode="aspectFill"
              @click="previewReviewImages(review, imgIndex)"
            />
            <view
              v-for="(video, videoIndex) in getReviewVideos(review).slice(0, 3)"
              :key="'v' + videoIndex"
              class="review-video"
              @click="previewReviewVideo(review, videoIndex)"
            >
              <image class="review-image" :src="getReviewVideoThumbnail(video)" mode="aspectFill" />
              <view class="video-play-icon">▶</view>
            </view>
            <view
              v-if="getReviewImages(review).length + getReviewVideos(review).length > 3"
              class="image-count"
            >
              {{ getReviewImages(review).length + getReviewVideos(review).length }}+
            </view>
          </view>
          <view v-if="review.followupComment" class="review-followup">
            <text class="followup-label">用户1天内追评</text>
          </view>
          <view class="review-footer">
            <text class="review-time">{{ formatReviewTime(review.createTime) }}</text>
            <view class="review-actions">
              <view
                class="review-action-item"
                :class="{ liked: review.isLiked }"
                @tap.stop="toggleLikeReview(review)"
              >
                <image
                  class="action-icon-image"
                  src="/static/images/点赞.png"
                  mode="aspectFit"
                />
                <text class="action-count">{{ review.likeCount || 0 }}</text>
              </view>
              <view class="review-action-item" @tap.stop="showReplyInput(review)">
                <image class="action-icon-image" src="/static/images/在线咨询.png" mode="aspectFit" />
                <text class="action-count">{{ review.replyCount || 0 }}</text>
              </view>
            </view>
          </view>
          <view v-if="review.replies && review.replies.length" class="review-replies">
            <view v-for="(reply, ridx) in review.replies" :key="ridx" class="reply-item">
              <text class="reply-username">{{ reply.userName || '匿名用户' }}:</text>
              <text class="reply-content">{{ reply.content }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-reviews">
        <text class="empty-text">暂无评价</text>
      </view>
      <view v-if="reviewsList.length && reviewsHasMore" class="load-more">
        <text class="load-more-text" @click="loadMoreReviews">加载更多</text>
      </view>
      <view v-if="replyingToReview" class="reply-input-bar">
        <input
          v-model="replyText"
          class="reply-input"
          placeholder="热情回复,文明用语"
          @confirm="submitReply"
        />
        <text class="reply-send-btn" @click="submitReply">发送</text>
      </view>
    </view>

    <view v-if="currentTab !== 'products' && currentTab !== 'reviews'" class="tab-content">
      <view class="empty-tab">
        <text class="empty-text">{{ getTabName(currentTab) }}内容开发中</text>
      </view>
    </view>
  </view>
</template>

<script>
import { util } from '@/common/js/util.js'
import { api } from '@/common/js/api.js'
import cartApi from '@/common/js/api/cart.js'

export default {
  data() {
    return {
      storeInfo: {
        name: '宠物家商品官方旗舰店',
        logo: '',
        avatar: '',
        saleCount: 0,
        rating: 0,
        id: null
      },
      storeRating: 0,
      currentTab: 'products',
      sortType: 'default',
      priceOrder: 'asc',
      goodsList: [],
      reviewCount: 0,
      page: 1,
      pageSize: 20,
      hasMore: true,
      loading: false,
      reviewsList: [],
      reviewsPage: 1,
      reviewsPageSize: 10,
      reviewsHasMore: true,
      reviewsLoading: false,
      replyingToReview: null,
      replyText: '',
      filterProductId: null,
      failedThumbIds: {}
    }
  },
  computed: {
    storeTotalSales() {
      if (this.goodsList && this.goodsList.length > 0) {
        return this.goodsList.reduce(
          (sum, g) => sum + parseInt(g.saleCount || g.sale || 0, 10),
          0
        )
      }
      return this.storeInfo.saleCount != null ? this.storeInfo.saleCount : 0
    },
    formattedReviewCount() {
      const count = this.reviewCount || 0
      if (count < 100) return String(count)
      if (count < 1000) return '100+'
      if (count < 10000) return '1000+'
      return `${(count / 10000).toFixed(1)}万`
    }
  },
  async onLoad(options) {
    if (options.storeName) {
      this.storeInfo.name = decodeURIComponent(options.storeName)
    }
    if (options.storeId) {
      this.storeInfo.id = options.storeId
    }
    if (options.storeAvatar) {
      this.storeInfo.avatar = decodeURIComponent(options.storeAvatar)
    }
    if (options.storeLogo) {
      this.storeInfo.logo = decodeURIComponent(options.storeLogo)
    }
    if (this.storeInfo.avatar && !this.storeInfo.logo) {
      this.storeInfo.logo = this.storeInfo.avatar
    }
    if (this.storeInfo.logo && !this.storeInfo.avatar) {
      this.storeInfo.avatar = this.storeInfo.logo
    }
    if (options.productId) {
      this.filterProductId = options.productId
    }
    if (options.tab) {
      this.currentTab = options.tab
    }
    if (this.storeInfo.id) {
      await this.loadGoodsList()
    } else {
      this.loadReviewsList().catch(() => {})
    }
  },
  onPullDownRefresh() {
    this.page = 1
    this.goodsList = []
    this.hasMore = true
    this.loadGoodsList().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  onReachBottom() {
    if (this.currentTab === 'products' && this.hasMore && !this.loading) {
      this.loadGoodsList()
    } else if (this.currentTab === 'reviews' && this.reviewsHasMore && !this.reviewsLoading) {
      this.loadMoreReviews()
    }
  },
  methods: {
    handleService() {
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid') || staffInfo.adminId
      if (!userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => uni.navigateTo({ url: '/pages-auth/login' }), 1500)
        return
      }
      uni.navigateTo({
        url: '/chat/customer-service?isPlatform=true'
      })
    },
    async loadStoreInfo() {
      if (!this.storeInfo.id) return
      try {
        const response = await api.request({
          url: '/api/store/info',
          method: 'GET',
          data: { storeId: this.storeInfo.id },
          showLoading: false
        })
        if (response && response.code === 200 && response.data) {
          const storeData = response.data
          this.storeInfo = {
            ...this.storeInfo,
            ...storeData,
            avatar: storeData.avatar || storeData.logo || this.storeInfo.avatar || '',
            logo: storeData.logo || storeData.avatar || this.storeInfo.logo || ''
          }
          this.storeRating = storeData.rating != null ? storeData.rating : this.storeInfo.rating || 0
          if (storeData.saleCount != null) {
            this.storeInfo.saleCount = storeData.saleCount
          }
        }
      } catch (e) {
        // 接口失败时沿用路由参数
      }
    },
    /** 店铺头已售：当前列表商品销量合计（件），满一万再显示「万件」，避免原先「4+万件」被当成四万多件 */
    formatStoreHeaderSales(totalPieces) {
      const n = Math.floor(Number(totalPieces) || 0)
      if (n <= 0) return '0件'
      if (n < 10000) return `${n}件`
      const s = (n / 10000).toFixed(1).replace(/\.0$/, '')
      return `${s}万件`
    },
    getStoreImageUrl(url) {
      if (!url) return ''
      return util.getImageUrl(url)
    },
    handleStoreImageError() {},
    getStarDisplay(index) {
      const rating = this.storeRating
      const fullStars = Math.floor(rating)
      const hasHalf = rating % 1 !== 0
      if (index < fullStars) return '★'
      if (index === fullStars && hasHalf) return '★'
      return '☆'
    },
    switchTab(tab) {
      this.currentTab = tab
      if (tab === 'products' && this.goodsList.length === 0) {
        this.loadGoodsList()
      } else if (tab === 'reviews' && this.reviewsList.length === 0) {
        this.loadReviewsList()
      }
    },
    changeSort(type) {
      if (type === 'price') {
        if (this.sortType === 'price') {
          this.priceOrder = this.priceOrder === 'asc' ? 'desc' : 'asc'
        } else {
          this.sortType = 'price'
          this.priceOrder = 'asc'
        }
      } else {
        this.sortType = type
      }
      this.page = 1
      this.hasMore = true
      this.goodsList = []
      this.loadGoodsList()
    },
    goToGoodsDetail(goodsId) {
      if (!goodsId) {
        uni.showToast({ title: '商品ID不存在', icon: 'none' })
        return
      }
      uni.navigateTo({ url: `/pages-goods/detail?id=${goodsId}` })
    },
    async addToCart(goods) {
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid') || staffInfo.adminId
      const token = uni.getStorageSync('token')
      if (!userId || !token) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => uni.navigateTo({ url: '/pages-auth/login' }), 1500)
        return
      }
      try {
        const res = await cartApi.addToCart({
          userId,
          productId: goods.id || goods.goodsId,
          quantity: 1
        })
        if (res && (res.code === 200 || res.code === 0)) {
          uni.showToast({ title: '已加入购物车', icon: 'success' })
        } else {
          uni.showToast({ title: (res && res.msg) || '加入购物车失败', icon: 'none' })
        }
      } catch (err) {
        uni.showToast({ title: '加入购物车失败，请重试', icon: 'none' })
      }
    },
    getGoodsImageUrl(urlOrGoods, useThumbnail) {
      const url =
        typeof urlOrGoods === 'object' && urlOrGoods != null
          ? urlOrGoods.pic || urlOrGoods.image || urlOrGoods.mainPic
          : urlOrGoods
      if (!url) return '/static/images/shop.png'
      const gid =
        typeof urlOrGoods === 'object' && urlOrGoods != null
          ? urlOrGoods.id || urlOrGoods.goodsId
          : null
      if (
        useThumbnail &&
        typeof url === 'string' &&
        gid != null &&
        !this.failedThumbIds[gid]
      ) {
        const s = url.trim()
        const lastSlash = Math.max(s.lastIndexOf('/'), s.lastIndexOf('\\'))
        if (lastSlash >= 0) {
          const pathBefore = s.substring(0, lastSlash + 1)
          const filename = s.substring(lastSlash + 1)
          if (filename && !filename.startsWith('thumb_')) {
            const thumbUrl = pathBefore + 'thumb_' + filename
            return util.getImageUrl(thumbUrl)
          }
        }
      }
      return util.getImageUrl(url)
    },
    formatSales(count) {
      if (count >= 10000) return (count / 10000).toFixed(1) + '万'
      return String(count)
    },
    sortGoodsList(list) {
      if (!list || !list.length) return list
      const sortedList = [...list]
      switch (this.sortType) {
        case 'sales':
          sortedList.sort((a, b) => {
            const sa = parseInt(a.saleCount || a.sale || 0, 10)
            const sb = parseInt(b.saleCount || b.sale || 0, 10)
            return sb - sa
          })
          break
        case 'new':
          sortedList.sort((a, b) => {
            const ta = a.createTime || a.updateTime || a.onlineTime || ''
            const tb = b.createTime || b.updateTime || b.onlineTime || ''
            if (!ta && !tb) return 0
            if (!ta) return 1
            if (!tb) return -1
            return String(tb).localeCompare(String(ta))
          })
          break
        case 'price':
          sortedList.sort((a, b) => {
            const pa = parseFloat(a.price || a.salePrice || 0)
            const pb = parseFloat(b.price || b.salePrice || 0)
            return this.priceOrder === 'asc' ? pa - pb : pb - pa
          })
          break
        default:
          break
      }
      return sortedList
    },
    getTabName(tab) {
      const names = { products: '全部商品', reviews: '评价' }
      return names[tab] || ''
    },
    async loadGoodsList() {
      if (!this.storeInfo.id) {
        this.goodsList = []
        return
      }
      const isReload = this.page === 1
      if (this.loading || (!this.hasMore && !isReload)) return
      this.loading = true
      try {
        const response = await api.request({
          url: '/api/product/list',
          method: 'GET',
          data: {
            page: this.page,
            pageSize: this.pageSize,
            sortType: this.sortType,
            priceOrder: this.priceOrder,
            storeId: this.storeInfo.id
          },
          showLoading: this.page === 1
        })
        if (response && (response.code === 200 || response.code === 0)) {
          let list = []
          const d = response.data
          if (Array.isArray(d)) list = d
          else if (d && Array.isArray(d.list)) list = d.list
          else if (d && Array.isArray(d.data)) list = d.data
          else if (d && Array.isArray(d.records)) list = d.records
          if (!Array.isArray(list)) list = []
          if (this.storeInfo.id && list.length) {
            const sid = String(this.storeInfo.id)
            const filtered = list.filter(item => {
              const itemStoreId =
                item.storeId ||
                item.store_id ||
                (item.storeInfo && item.storeInfo.id) ||
                (item.store && item.store.id)
              return itemStoreId && String(itemStoreId) === sid
            })
            if (filtered.length) list = filtered
          }
          list = list.filter(item => {
            const cat = item.category || item.categoryName || ''
            const points =
              cat.includes('积分') ||
              cat.includes('积分商城') ||
              item.categoryId === 'points' ||
              item.categoryId === 'jifen'
            return !points
          })
          const processed = list.map(item => {
            const tags = []
            if (item.favoriteCount > 0) {
              tags.push(this.formatFavoriteCount(item.favoriteCount) + '人收藏')
            }
            if (item.serviceTags && item.serviceTags.length) {
              tags.push(...item.serviceTags)
            }
            const banners = item.banners && item.banners.length ? [...item.banners] : []
            return {
              ...item,
              tags: tags.length ? tags : undefined,
              banners: banners.length ? banners : undefined
            }
          })
          const sorted = this.sortGoodsList(processed)
          if (this.page === 1) {
            this.$set(this, 'goodsList', sorted)
          } else {
            this.goodsList = [...this.goodsList, ...sorted]
          }
          this.hasMore = list.length >= this.pageSize
          this.page += 1
        } else if (this.page === 1) {
          this.goodsList = []
        }
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    formatFavoriteCount(count) {
      if (count >= 10000) return (count / 10000).toFixed(1) + '万'
      return String(count)
    },
    handleImageError(e, goods) {
      if (goods && (goods.id != null || goods.goodsId != null)) {
        const id = goods.id != null ? goods.id : goods.goodsId
        this.failedThumbIds = { ...this.failedThumbIds, [id]: true }
      }
    },
    async loadReviewsList() {
      if (!this.storeInfo.id) {
        this.reviewsList = []
        return
      }
      if (this.reviewsLoading || (!this.reviewsHasMore && this.reviewsPage > 1)) return
      this.reviewsLoading = true
      try {
        let productIds = []
        const productsMap = {}
        if (this.filterProductId) {
          const pr = await api.request({
            url: `/api/product/${this.filterProductId}`,
            method: 'GET',
            showLoading: false
          })
          if (pr && (pr.code === 200 || pr.code === 0) && pr.data) {
            const product = pr.data
            const itemStoreId =
              product.storeId ||
              product.store_id ||
              (product.storeInfo && product.storeInfo.id) ||
              (product.store && product.store.id)
            if (
              this.storeInfo.id &&
              itemStoreId &&
              String(itemStoreId) !== String(this.storeInfo.id)
            ) {
              this.reviewsList = []
              this.reviewsHasMore = false
              return
            }
            productsMap[this.filterProductId] = product
            productIds = [this.filterProductId]
          }
        } else {
          const goodsResponse = await api.request({
            url: '/api/product/list',
            method: 'GET',
            data: { storeId: this.storeInfo.id, page: 1, pageSize: 100 },
            showLoading: this.reviewsPage === 1
          })
          if (goodsResponse && (goodsResponse.code === 200 || goodsResponse.code === 0)) {
            let products = []
            const gd = goodsResponse.data
            if (Array.isArray(gd)) products = gd
            else if (gd && gd.records) products = gd.records
            else if (gd && gd.list) products = gd.list
            const sid = String(this.storeInfo.id)
            products = products.filter(p => {
              const ps =
                p.storeId ||
                p.store_id ||
                (p.storeInfo && p.storeInfo.id) ||
                (p.store && p.store.id)
              return ps && String(ps) === sid
            })
            productIds = products.map(p => p.id).filter(Boolean)
            productIds.forEach(id => {
              const p = products.find(x => x.id === id)
              if (p) productsMap[id] = p
            })
          }
        }
        if (!productIds.length) {
          this.reviewsList = []
          this.reviewsHasMore = false
          return
        }
        const allReviews = []
        const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid')
        for (const productId of productIds) {
          try {
            const reviewResponse = await api.request({
              url: `/api/product/${productId}/reviews`,
              method: 'GET',
              data: userId ? { userId } : {},
              showLoading: false
            })
            if (reviewResponse && (reviewResponse.code === 200 || reviewResponse.code === 0)) {
              let reviews = []
              const rd = reviewResponse.data
              if (Array.isArray(rd)) reviews = rd
              else if (rd && rd.list) reviews = rd.list
              else if (rd && rd.records) reviews = rd.records
              const product = productsMap[productId]
              reviews.forEach(review => {
                if (product) {
                  review.productName = product.name
                  review.productImage = product.image
                }
                if (review.expanded === undefined) review.expanded = false
                if (review.likeCount === undefined) review.likeCount = 0
                if (review.replyCount === undefined) review.replyCount = 0
                if (review.isLiked === undefined) review.isLiked = false
                if (!review.replies) review.replies = []
              })
              allReviews.push(...reviews)
            }
          } catch (e) {
            // 单个商品评价失败则跳过
          }
        }
        allReviews.sort((a, b) => {
          const ta = new Date(String(a.createTime || 0).replace(' ', 'T')).getTime()
          const tb = new Date(String(b.createTime || 0).replace(' ', 'T')).getTime()
          return tb - ta
        })
        this.reviewCount = allReviews.length
        const start = (this.reviewsPage - 1) * this.reviewsPageSize
        const end = start + this.reviewsPageSize
        const pageReviews = allReviews.slice(start, end)
        if (this.reviewsPage === 1) {
          this.reviewsList = pageReviews
        } else {
          this.reviewsList = [...this.reviewsList, ...pageReviews]
        }
        this.reviewsHasMore = end < allReviews.length
        this.reviewsPage += 1
      } catch (e) {
        uni.showToast({ title: '加载评价失败', icon: 'none' })
      } finally {
        this.reviewsLoading = false
      }
    },
    loadMoreReviews() {
      if (this.reviewsHasMore && !this.reviewsLoading) {
        this.loadReviewsList()
      }
    },
    expandReview(review) {
      const i = this.reviewsList.findIndex(r => r.id === review.id || r === review)
      if (i !== -1) {
        this.$set(this.reviewsList[i], 'expanded', true)
      } else {
        this.$set(review, 'expanded', true)
      }
    },
    shouldShowExpandBtn(review) {
      if (!review || (!review.comment && !review.content)) return false
      const text = review.comment || review.content || ''
      return text.length > 80
    },
    getReviewImages(review) {
      if (!review) return []
      if (!review.images || review.images === '' || review.images === 'null') return []
      if (Array.isArray(review.images)) {
        return review.images.filter(img => img && String(img).trim() !== '')
      }
      if (typeof review.images === 'string') {
        try {
          const parsed = JSON.parse(review.images)
          if (Array.isArray(parsed)) {
            return parsed.filter(img => img && String(img).trim() !== '')
          }
        } catch (e) {
          /* ignore */
        }
        if (review.images.includes(',')) {
          return review.images.split(',').map(s => s.trim()).filter(Boolean)
        }
        if (review.images.trim()) return [review.images.trim()]
      }
      return []
    },
    getReviewVideos(review) {
      if (!review || !review.videos) return []
      if (Array.isArray(review.videos)) return review.videos
      if (typeof review.videos === 'string') {
        if (review.videos.includes(',')) {
          return review.videos.split(',').map(v => v.trim()).filter(Boolean)
        }
        try {
          const parsed = JSON.parse(review.videos)
          if (Array.isArray(parsed)) return parsed
        } catch (e) {
          return [review.videos]
        }
      }
      return []
    },
    getReviewVideoThumbnail() {
      return '/static/images/video-placeholder.png'
    },
    previewReviewVideo() {
      uni.showToast({ title: '视频播放功能开发中', icon: 'none' })
    },
    getReviewImageUrl(url) {
      if (!url) return '/static/images/garfield-default-avatar.png'
      return util.getImageUrl(url)
    },
    previewReviewImages(review, currentIndex) {
      const images = this.getReviewImages(review).map(img => this.getReviewImageUrl(img))
      uni.previewImage({ urls: images, current: currentIndex })
    },
    formatReviewTime(timeStr) {
      if (!timeStr) return ''
      const formatted = String(timeStr).replace(/-/g, '/')
      const date = new Date(formatted)
      const now = new Date()
      const diff = now.getTime() - date.getTime()
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      if (days === 0) return '今天'
      if (days === 1) return '昨天'
      if (days < 7) return `${days}天前`
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      const h = String(date.getHours()).padStart(2, '0')
      const min = String(date.getMinutes()).padStart(2, '0')
      return `${y}-${m}-${d} ${h}:${min}`
    },
    async toggleLikeReview(review) {
      if (!review || !review.id) {
        uni.showToast({ title: '评价信息错误', icon: 'none' })
        return
      }
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid') || staffInfo.adminId
      if (!userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      try {
        const response = await api.request({
          url: `/api/product/review/${review.id}/like`,
          method: 'POST',
          data: { userId }
        })
        if (response.code === 200 && response.data) {
          const idx = this.reviewsList.findIndex(r => r.id === review.id)
          if (idx !== -1) {
            this.$set(this.reviewsList[idx], 'isLiked', response.data.isLiked)
            this.$set(this.reviewsList[idx], 'likeCount', response.data.likeCount || 0)
          }
        } else {
          throw new Error((response && response.msg) || '操作失败')
        }
      } catch (e) {
        uni.showToast({ title: (e && e.message) || '操作失败', icon: 'none' })
      }
    },
    async showReplyInput(review) {
      if (!review || !review.id) {
        uni.showToast({ title: '评价信息错误', icon: 'none' })
        return
      }
      if (!review.replies || !review.replies.length) {
        await this.loadReviewReplies(review)
      }
      this.replyingToReview = review
      this.replyText = ''
    },
    async loadReviewReplies(review) {
      if (!review || !review.id) return
      try {
        const response = await api.request({
          url: `/api/product/review/${review.id}/replies`,
          method: 'GET'
        })
        if (response.code === 200 && response.data) {
          const replies = Array.isArray(response.data) ? response.data : []
          const idx = this.reviewsList.findIndex(r => r.id === review.id)
          if (idx !== -1) {
            this.$set(this.reviewsList[idx], 'replies', replies)
            this.$set(this.reviewsList[idx], 'replyCount', replies.length)
          }
        }
      } catch (e) {
        console.error(e)
      }
    },
    async submitReply() {
      if (!this.replyingToReview) return
      const content = (this.replyText || '').trim()
      if (!content) {
        uni.showToast({ title: '请输入回复内容', icon: 'none' })
        return
      }
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid') || staffInfo.adminId
      if (!userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      try {
        const response = await api.request({
          url: `/api/product/review/${this.replyingToReview.id}/reply`,
          method: 'POST',
          data: { userId, content }
        })
        if (response.code === 200 && response.data) {
          const idx = this.reviewsList.findIndex(r => r.id === this.replyingToReview.id)
          if (idx !== -1) {
            if (!this.reviewsList[idx].replies) {
              this.$set(this.reviewsList[idx], 'replies', [])
            }
            this.reviewsList[idx].replies.push(response.data)
            const n = (this.reviewsList[idx].replyCount || 0) + 1
            this.$set(this.reviewsList[idx], 'replyCount', n)
          }
          this.replyText = ''
          this.replyingToReview = null
          uni.showToast({ title: '回复成功', icon: 'success' })
        } else {
          throw new Error((response && response.msg) || '回复失败')
        }
      } catch (e) {
        uni.showToast({ title: (e && e.message) || '回复失败', icon: 'none' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background-color: #ffffff;
}
.store-header {
  background-color: #ffffff;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.store-info-wrapper {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.store-logo-wrapper {
  width: 100rpx;
  height: 100rpx;
  border-radius: 10rpx;
  overflow: hidden;
  background: linear-gradient(135deg, #a5d6a7 0%, #81c784 100%);
  flex-shrink: 0;
  .store-logo {
    width: 100%;
    height: 100%;
  }
}
.store-info-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  .store-name {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .store-stats {
    display: flex;
    align-items: center;
    gap: 12rpx;
    font-size: 22rpx;
    .sale-count {
      color: #666;
    }
    .divider {
      color: #ddd;
    }
    .rating-wrapper {
      display: flex;
      align-items: center;
      gap: 8rpx;
      .rating-stars .star {
        font-size: 20rpx;
        color: #ff6b6b;
      }
      .rating-score {
        font-size: 22rpx;
        color: #666;
      }
    }
  }
}
.store-actions {
  flex-shrink: 0;
  .service-btn {
    border: none;
    background: transparent;
    padding: 0;
    .btn-icon-image {
      width: 40rpx;
      height: 40rpx;
    }
    .btn-text {
      display: none;
    }
  }
}
.nav-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  .nav-tab-item {
    flex: 1;
    text-align: center;
    padding: 24rpx 0;
    font-size: 28rpx;
    color: #666;
    position: relative;
    &.active {
      color: #ff6b35;
      font-weight: 500;
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: #ff6b35;
        border-radius: 2rpx;
      }
    }
  }
}
.sort-bar {
  display: flex;
  background: #fff;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  .sort-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 26rpx;
    color: #666;
    &.active {
      color: #ff6b35;
      font-weight: 500;
    }
    .price-arrows {
      display: flex;
      flex-direction: column;
      margin-left: 4rpx;
      .arrow-up,
      .arrow-down {
        font-size: 16rpx;
        color: #ccc;
        &.active {
          color: #ff6b35;
        }
      }
    }
  }
}
.goods-list-section {
  display: flex;
  flex-wrap: wrap;
  padding: 20rpx 15rpx;
  gap: 20rpx;
  background: #f5f5f5;
  min-height: 400rpx;
}
.goods-item {
  width: calc((100% - 20rpx) / 2);
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-sizing: border-box;
}
.goods-image-wrapper {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 100%;
  overflow: hidden;
  .goods-image {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }
  .goods-banners {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    flex-wrap: wrap;
    gap: 8rpx;
    padding: 8rpx;
    .goods-banner {
      padding: 4rpx 12rpx;
      border-radius: 4rpx;
      font-size: 20rpx;
      color: #fff;
      &.banner-purple {
        background: #9c27b0;
      }
      &.banner-red {
        background: #ff6b6b;
      }
    }
  }
}
.goods-info {
  padding: 16rpx;
  .goods-name {
    font-size: 26rpx;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    min-height: 72rpx;
  }
  .goods-desc {
    font-size: 22rpx;
    color: #999;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .goods-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8rpx;
    .goods-tag {
      padding: 4rpx 10rpx;
      background: #f5f5f5;
      border-radius: 4rpx;
      font-size: 20rpx;
      color: #666;
    }
  }
  .goods-price-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 8rpx;
    .price-symbol,
    .price-value {
      color: #ff6b6b;
      font-weight: bold;
    }
    .price-value {
      font-size: 32rpx;
    }
    .add-cart-btn {
      width: 48rpx;
      height: 48rpx;
      min-width: 48rpx;
      min-height: 48rpx;
      flex-shrink: 0;
      border-radius: 50%;
      background: #ff6b6b;
      display: flex;
      align-items: center;
      justify-content: center;
      .add-icon {
        color: #fff;
        font-size: 30rpx;
        font-weight: 500;
        line-height: 1;
      }
    }
  }
  .goods-sales {
    font-size: 22rpx;
    color: #999;
    margin-top: 4rpx;
  }
}
.empty-goods {
  width: 100%;
  padding: 100rpx 0;
  text-align: center;
  .empty-text {
    color: #999;
    font-size: 28rpx;
  }
}
.reviews-section {
  padding: 20rpx;
  background: #fff;
}
.review-item {
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  .review-avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    margin-right: 16rpx;
  }
  .review-user-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }
  .review-username {
    font-size: 28rpx;
    font-weight: 500;
    color: #333;
  }
  .purchase-text {
    font-size: 24rpx;
    color: #999;
  }
}
.review-content {
  margin-bottom: 16rpx;
  .review-text {
    font-size: 28rpx;
    color: #333;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    overflow: hidden;
    &.expanded {
      display: block;
      -webkit-line-clamp: unset;
      line-clamp: unset;
    }
  }
  .expand-btn {
    font-size: 24rpx;
    color: #999;
    margin-left: 8rpx;
  }
}
.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 16rpx;
  position: relative;
  .review-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: 8rpx;
  }
  .review-video {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    .video-play-icon {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      color: #fff;
      font-size: 32rpx;
    }
  }
  .image-count {
    position: absolute;
    bottom: 12rpx;
    right: 12rpx;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
    font-size: 20rpx;
    padding: 4rpx 12rpx;
    border-radius: 4rpx;
  }
}
.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  .review-time {
    font-size: 24rpx;
    color: #999;
  }
  .review-actions {
    display: flex;
    gap: 30rpx;
  }
  .review-action-item {
    display: flex;
    align-items: center;
    gap: 8rpx;
    .action-icon-image {
      width: 32rpx;
      height: 32rpx;
    }
    .action-count {
      font-size: 24rpx;
      color: #999;
    }
  }
}
.review-replies {
  margin-top: 20rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
  .reply-item {
    font-size: 26rpx;
    margin-bottom: 12rpx;
    .reply-username {
      color: #666;
      font-weight: 500;
    }
    .reply-content {
      color: #333;
    }
  }
}
.empty-reviews {
  padding: 100rpx 0;
  text-align: center;
  color: #999;
}
.load-more {
  padding: 40rpx;
  text-align: center;
  .load-more-text {
    font-size: 28rpx;
    color: #666;
    padding: 16rpx 40rpx;
    border: 1rpx solid #e0e0e0;
    border-radius: 8rpx;
  }
}
.reply-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  border-top: 1px solid #e5e5e5;
  z-index: 999;
  .reply-input {
    flex: 1;
    padding: 20rpx 24rpx;
    background: #f5f5f5;
    border-radius: 40rpx;
    font-size: 28rpx;
  }
  .reply-send-btn {
    margin-left: 20rpx;
    padding: 20rpx 30rpx;
    background: #ff6b00;
    color: #fff;
    border-radius: 40rpx;
    font-size: 28rpx;
  }
}
.tab-content {
  min-height: 400rpx;
  padding: 100rpx 0;
  text-align: center;
  background: #f5f5f5;
  .empty-text {
    color: #999;
    font-size: 28rpx;
  }
}
</style>
