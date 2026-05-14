<template>
  <view class="community-page">
    <!-- 主导航标签（关注/发现）和发布 -->
    <view class="main-nav-tabs">
      <view class="main-nav-center">
        <view 
          class="main-nav-tab" 
          v-for="(tab, index) in tabs" 
          :key="tab.key"
          :class="{ active: currentTabIndex === index }"
          @click="switchTab(index)"
        >
          <text class="main-tab-text">{{ tab.label }}</text>
          <view class="main-tab-line" v-if="currentTabIndex === index"></view>
        </view>
      </view>
      <view class="publish-button" @click="goToPublish">
        <text class="publish-text">发布</text>
      </view>
    </view>

    <!-- 搜索框 - 只在发现显示 -->
    <view class="search-section" v-if="currentTabIndex === 1">
      <view class="search-box" @tap="goToSearch">
        <text class="search-placeholder">{{ searchPlaceholder }}</text>
      </view>
    </view>

    <!-- 发现页标签栏（动态：后台可配置；样式与「宠物专题」统一） -->
    <view class="daily-category-bar" v-if="currentTabIndex === 1">
      <scroll-view scroll-x class="daily-category-scroll" :show-scrollbar="false" enhanced>
        <view class="daily-category-inner">
          <view
            v-for="cat in categories"
            :key="cat.key"
            class="daily-category-item"
            :class="{ active: currentCategory === cat.key }"
            @click="selectDiscoverCategory(cat.key)"
          >
            <text class="daily-category-label">{{ cat.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 滑动容器 -->
    <swiper 
      class="content-swiper" 
      :current="currentTabIndex" 
      @change="onSwiperChange"
      :duration="300"
    >
      <!-- 关注内容 - 直接复制Vue项目 -->
      <swiper-item>
        <view class="tab-content">
          <view class="att-wrapper">
            <view class="attContainer">
              <!-- 关注 tab 加载中 -->
              <view class="follow-loading" v-if="followTabLoading">
                <text class="follow-loading-text">加载中...</text>
              </view>
              <!-- 关注用户动态Feed流 -->
              <view class="follow-feed" v-else-if="followPosts && followPosts.length > 0">
                <view 
                  v-for="(post, index) in followPosts" 
                  :key="index" 
                  class="follow-post-item"
                >
                  <!-- 用户信息行 -->
                  <view class="follow-user-header">
                    <view class="user-info-left">
                      <image 
                        :src="getImageUrl(getFollowPostAvatar(post))" 
                        mode="aspectFill" 
                        class="user-avatar"
                        @error="handleAvatarError($event, post)"
                      />
                      <view class="user-info-text">
                        <text class="user-name">{{ post.userName || post.username || '用户' }}</text>
                        <text class="user-desc" v-if="post.userDesc">{{ post.userDesc }}</text>
                      </view>
                    </view>
                  </view>
                  
                  <!-- 帖子内容 -->
                  <view class="follow-post-content" @tap="selectedNote(post, index)">
                    <!-- 文字内容 -->
                    <view class="post-text" v-if="post.content || post.title">
                      <text>{{ post.content || post.title }}</text>
                    </view>
                    
                    <!-- 图片/视频内容（视频帖无封面时用占位图） -->
                    <view class="post-media" v-if="getPostDisplayImages(post).length > 0">
                      <view 
                        v-for="(img, imgIndex) in getPostDisplayImages(post).slice(0, 9)" 
                        :key="imgIndex"
                        class="post-image-item"
                        @longpress="previewPostImages(post, imgIndex)"
                      >
                        <image 
                          :src="getImageUrl(img)" 
                          :mode="getFollowPostImageMode(post)"
                          @error="handleFollowPostImageError($event, post)"
                        />
                        <!-- 视频标识（视频帖显示播放图标，封面可能是 thumb 图片地址） -->
                        <view class="video-play-icon" v-if="postHasVideo(post)">
                          <text>▶</text>
                        </view>
                      </view>
                    </view>
                  </view>
                  
                  <!-- 互动按钮 -->
                  <view class="follow-post-actions">
                    <view 
                      class="action-item" 
                      @click.stop="sharePost(post, $event)"
                      :data-post-id="post.id"
                      :data-post-index="index"
                    >
                      <image 
                        class="action-icon share-icon" 
                        :src="'/static/images/分享.png'"
                        mode="aspectFit"
                      ></image>
                    </view>
                    <view 
                      class="action-item" 
                      @click.stop="goToPostDetail(post, $event)"
                      :data-post-id="post.id"
                      :data-post-index="index"
                    >
                      <image 
                        class="action-icon comment-icon" 
                        :src="'/static/images/在线咨询.png'"
                        mode="aspectFit"
                      ></image>
                      <text>{{ post.commentsCount || post.commentCount || 0 }}</text>
                    </view>
                    <view 
                      class="action-item" 
                      @click.stop="toggleLike(post, index, $event)"
                      :data-post-id="post.id"
                      :data-post-index="index"
                    >
                      <image 
                        class="action-icon like-icon" 
                        :class="{ liked: post.isLiked }"
                        :src="post.isLiked ? '/static/images/点赞后.png' : '/static/images/点赞前.png'"
                        mode="aspectFit"
                      ></image>
                      <text>{{ post.likesCount || post.likeCount || 0 }}</text>
                    </view>
                  </view>
                </view>
              </view>
              
              <!-- 未登录时提示 -->
              <view class="empty-follow-message" v-if="!followTabLoading && (!currentUserId || !isValidUserId(currentUserId))">
                <text class="empty-main-text">登录后查看关注动态</text>
                <text class="empty-sub-text">去首页或「我的」登录即可看到关注的人的最新动态</text>
              </view>
              <!-- 空状态消息框 - 已登录且没有关注用户/没有动态时显示 -->
              <view class="empty-follow-message" v-else-if="!followTabLoading && currentUserId && isValidUserId(currentUserId) && (!followedUsers || followedUsers.length === 0) && (!followPosts || followPosts.length === 0)">
                <text class="empty-main-text">您关注的用户还没有动态呢~</text>
                <text class="empty-sub-text">别那么高冷啦,快去关注更多小伙伴吧~</text>
              </view>
              
              <!-- 为你推荐标题 - 只在关注数为0时显示 -->
              <view class="recommend-title" v-if="!followTabLoading && recommendedUsers.length > 0 && (!followedUsers || followedUsers.length === 0) && (!followPosts || followPosts.length === 0)">为你推荐</view>
              
              <!-- 推荐用户列表 - 只在关注数为0时显示 -->
              <view class="attent-list" v-if="!followTabLoading && recommendedUsers.length > 0 && (!followedUsers || followedUsers.length === 0)">
                <view 
                  v-for="(u, index) in recommendedUsers" 
                  :key="index" 
                  class="attent-item"
                >
                  <!-- 用户信息行 -->
                  <view class="user-info-row">
                    <view class="left" @tap.stop="goToUserProfile(u)">
                      <image :src="getImageUrl(u.avatar || u.img || '/static/images/login-dog.png')" mode="aspectFill" class="attent-avatar" @error="onRecommendAvatarError($event, u, index)" />
                    </view>
                    <view class="right">
                      <view class="a-title" @tap.stop="goToUserProfile(u)">
                        <text class="name">{{ getRecommendDisplayName(u) }}</text>
                        <text class="desc">{{ u.desc || u.bio || '' }}</text>
                      </view>
                      <view class="a-btn">
                        <button 
                          :data-index="index"
                          :data-user-id="u.id || u.userId || u.uid"
                          @tap.stop="followUserByIndex(index, u)"
                          class="follow-btn-yellow"
                        >
                          + 关注
                        </button>
                      </view>
                    </view>
                  </view>
                  <!-- 用户帖子缩略图 -->
                  <view class="user-posts-grid" v-if="u.posts && u.posts.length > 0">
                    <view 
                      v-for="(post, postIndex) in u.posts.slice(0, 3)" 
                      :key="postIndex"
                      class="post-thumbnail"
                      @click="goToPostDetail(post)"
                    >
                      <image :src="getImageUrl(post.image || post.thumbnail)" mode="aspectFill" />
                      <text class="post-caption" v-if="post.caption">{{ post.caption }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>
      </swiper-item>

      <!-- 发现内容 -->
      <swiper-item>
        <view class="tab-content">
          <!-- 发现页面瀑布流 - 小红书式双列布局 -->
          <scroll-view 
            class="discovery-scroll" 
            scroll-y 
            @scrolltolower="onReachBottom"
            :lower-threshold="100"
          >
            <view class="dis-list">
              <view class="left-list">
                <view 
                  v-for="(item, index) in leftDisList" 
                  :key="index"
                  :id="'waterfall-left-' + index"
                  class="waterfall-item"
                  v-if="item"
                  @tap="selectedNote(item)"
                >
                  <view class="note_item">
                    <view class="img">
                      <image 
                        :src="getImageUrl(item.img) || getImageUrl('/static/images/default-product.svg')" 
                        mode="widthFix" 
                        @error="handleWaterfallImageError($event, item, 'left', index)"
                        @load="onWaterfallImageLoad($event, item, 'left', index)"
                      />
                      <!-- 视频帖子标识：右上角小播放图标 -->
                      <view v-if="item.isVideo" class="video-play-icon-small">▶</view>
                    </view>
                    <view class="desc">
                      <text>{{ item.desc }}</text>
                    </view>
                    <view class="note">
                      <view class="user">
                        <image :src="getImageUrl(item.avator)" mode="aspectFill" />
                        <text>{{ item.uname }}</text>
                      </view>
                      <view class="like" @click.stop="toggleLikeInWaterfall(item, index, 'left')">
                        <image 
                          class="like-heart" 
                          :class="{ liked: item.isLiked || (item.post && item.post.isLiked) }"
                          :src="(item.isLiked || (item.post && item.post.isLiked)) ? '/static/images/点赞后.png' : '/static/images/点赞前.png'"
                          mode="aspectFit"
                        ></image>
                        <text class="like-num">{{ (item.post && (item.post.likesCount || item.post.likeCount)) || item.like || 0 }}</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
              <view class="right-list">
                <view 
                  v-for="(item, index) in rightDisList" 
                  :key="index"
                  :id="'waterfall-right-' + index"
                  class="waterfall-item"
                  v-if="item"
                  @tap="selectedNote(item)"
                >
                  <view class="note_item">
                    <view class="img">
                      <image 
                        :src="getImageUrl(item.img) || getImageUrl('/static/images/default-product.svg')" 
                        mode="widthFix" 
                        @error="handleWaterfallImageError($event, item, 'right', index)"
                        @load="onWaterfallImageLoad($event, item, 'right', index)"
                      />
                      <!-- 视频帖子标识：右上角小播放图标 -->
                      <view v-if="item.isVideo" class="video-play-icon-small">▶</view>
                    </view>
                    <view class="desc">
                      <text>{{ item.desc }}</text>
                    </view>
                    <view class="note">
                      <view class="user">
                        <image :src="getImageUrl(item.avator)" mode="aspectFill" />
                        <text>{{ item.uname }}</text>
                      </view>
                      <view class="like" @click.stop="toggleLikeInWaterfall(item, index, 'right')">
                        <image 
                          class="like-heart" 
                          :class="{ liked: item.isLiked || (item.post && item.post.isLiked) }"
                          :src="(item.isLiked || (item.post && item.post.isLiked)) ? '/static/images/点赞后.png' : '/static/images/点赞前.png'"
                          mode="aspectFit"
                        ></image>
                        <text class="like-num">{{ (item.post && (item.post.likesCount || item.post.likeCount)) || item.like || 0 }}</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>
      </swiper-item>

      <!-- 每日专题内容 -->
      <swiper-item>
        <scroll-view scroll-y class="tab-content daily-tab-content">
          <view class="daily-topics-content">
            <!-- 分类 Tab 栏（横向滑动） -->
            <view class="daily-category-bar">
              <scroll-view scroll-x class="daily-category-scroll" :show-scrollbar="false" enhanced>
                <view class="daily-category-inner">
                  <view 
                    v-for="cat in dailyTopicCategories" 
                    :key="cat.key"
                    class="daily-category-item"
                    :class="{ active: currentDailyTopicCategory === cat.key }"
                    @click="selectDailyTopicCategory(cat.key)"
                  >
                    <text class="daily-category-label">{{ cat.label }}</text>
                  </view>
                </view>
              </scroll-view>
            </view>

            <!-- 官方精选专题（管理员后台发布，非用户帖子） -->
            <view class="topics-section" v-if="dailyOfficialTopics.length > 0">
              <view 
                v-for="(t, index) in dailyOfficialTopics" 
                :key="t.id" 
                class="topic-item"
                @click="goToTopicDetail(t, index)"
              >
                <view class="topic-content">
                  <text class="topic-title">{{ t.title }}</text>
                </view>
                <image class="topic-thumb" :src="getImageUrl(t.thumb)" mode="aspectFill" @error="handleImageError" />
              </view>
            </view>
            <view class="topics-empty" v-else-if="!dailyLoading && dailyOfficialTopics.length === 0">
              <text class="topics-empty-text">暂无官方专题，敬请期待</text>
            </view>

            <!-- 空状态 -->
            <view class="empty-state" v-if="dailyOfficialTopics.length === 0 && !dailyLoading">
              <text class="empty-icon">📚</text>
              <text class="empty-text">暂无宠物专题内容</text>
              <text class="empty-desc">敬请期待</text>
            </view>
          </view>
        </scroll-view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  data() {
    return {
      currentTab: 'discover',
      currentTabIndex: 1, // 当前swiper的索引，默认显示发现页面
      tabs: [
        { key: 'follow', label: '关注' },
        { key: 'discover', label: '发现' },
        { key: 'daily', label: '宠物专题' }
      ],
      searchPlaceholder: '',
      currentCategory: 'recommend',
      categories: [
        { key: 'recommend', label: '推荐' }
      ],
      features: [],
      posts: [],
      followPosts: [],
      followPostCoverFailed: {}, // 关注流中封面加载失败的 postId -> true，用于回退占位图
      dailyOfficialTopics: [],
      dailyOfficialTopicsAll: [],
      currentDailyTopicCategory: 'recommend',
      dailyTopicCategories: [],
      dailyTodayTheme: '',
      dailyLoading: false,
      leftFollowPosts: [],
      rightFollowPosts: [],
      leftDisList: [],
      rightDisList: [],
      leftColumnHeight: 0, // 左列总高度
      rightColumnHeight: 0, // 右列总高度
      pendingItems: [], // 待分配的卡片（图片加载中）
      imageLoadQueue: [], // 图片加载队列
      attentList: [
        {
          img: '/static/a1.png',
          name: '小红叔',
          desc: '官方账号，小红书男性频道'
        },
        {
          img: '/static/a2.png',
          name: '视频薯',
          desc: '官方账号，发现热门视频'
        },
        {
          img: '/static/a3.png',
          name: '娱乐薯',
          desc: '官方账号，混迹娱乐圈前沿'
        },
        {
          img: '/static/a4.png',
          name: '吃不饱同学',
          desc: '零食种草机，美食奇趣玩法'
        },
        {
          img: '/static/a1.png',
          name: '小红叔',
          desc: '官方账号，小红书男性频道'
        },
        {
          img: '/static/a2.png',
          name: '视频薯',
          desc: '官方账号，发现热门视频'
        },
        {
          img: '/static/a3.png',
          name: '娱乐薯',
          desc: '官方账号，混迹娱乐圈前沿'
        },
        {
          img: '/static/a4.png',
          name: '吃不饱同学',
          desc: '零食种草机，美食奇趣玩法'
        }
      ],
      followedUsers: [],
      recommendedUsers: [],
      loading: false,
      hasMore: true,
      page: 1,
      discoverRecommendPage: 1, // 推荐流当前页（下拉刷新时切到下一页）
      currentUserId: null, // 从登录信息获取
      scrollTop: 0, // 滚动位置
      lastFollowLoadTime: 0,
      followTabLoading: false // 关注 tab 数据加载中，用于显示加载态
    }
  },

  onLoad() {
    // 获取当前登录用户ID
    this.getCurrentUserId()
    this.testApiConnection()
    // 初始化搜索框placeholder
    this.updateSearchPlaceholder(this.currentTabIndex)
    // 先获取当前用户ID，再加载列表（列表接口需要 checkUserId 才能返回正确的点赞状态和数量）
    this.getCurrentUserId()
    // 加载关注页面的数据（关注流、关注用户、推荐用户都预加载，避免切到关注 tab 空白）
    this.followTabLoading = true
    Promise.all([
      this.loadFollowedUsers(),
      this.loadRecommendedUsers(),
      this.loadFollowPosts()
    ]).finally(() => {
      this.followTabLoading = false
    })
    // 加载发现页标签与数据（先尽量拿到 userId，列表接口才能返回 isLiked）
    this.loadDiscoverCategories().finally(() => {
      this.ensureCurrentUserReady().then(() => this.loadPosts())
    })
    // 宠物专题 Tab 分类（与后台主题分类管理同步）
    this.loadDailyTopicThemes()
  },

  onShow() {
    // 工作人员账号只能使用「我的」页面（异步 switchTab，减轻 routeDone/webviewId 报错）
    if (util.redirectStaffToMineIfNeeded()) return
    this.getCurrentUserId()
    // 关注标签：静默刷新，节流 15 秒，不显示全局「加载中」，避免 tab 切换反复弹 loading
    // 需同时拉取关注流帖子；此前只刷新了关注用户列表，未拉 loadFollowPosts，从别处关注作者后回到本页会看不到新帖
    if (this.currentTabIndex === 0) {
      const now = Date.now()
      const throttleMs = 15 * 1000
      if (now - (this.lastFollowLoadTime || 0) >= throttleMs || (this.lastFollowLoadTime || 0) === 0) {
        this.loadFollowedUsers(true)
        this.loadRecommendedUsers(true)
        this.loadFollowPosts()
        this.lastFollowLoadTime = Date.now()
      }
    }
    // 发现页：合并详情页返回的点赞/取消点赞状态，保证与详情页一致
    if (this.currentTabIndex === 1 && this.$store && this.$store.state.postLikeCache) {
      this.mergePostLikeCacheIntoDiscovery()
    }
  },

  onPullDownRefresh() {
    const timeout = 8000
    const stopRefresh = () => {
      try { uni.stopPullDownRefresh() } catch (e) {}
    }
    if (this.currentTabIndex === 0) {
      this.followTabLoading = true
    }
    const refreshPromise = this.currentTabIndex === 0
      ? Promise.all([
          this.loadFollowPosts(),
          this.loadFollowedUsers(true),
          this.loadRecommendedUsers(true)
        ]).finally(() => {
          this.followTabLoading = false
        })
      : this.currentTabIndex === 2
        ? this.loadDailyTopics()
        : this.loadPosts({ fromPullRefresh: true, forceNextBatch: true })
    Promise.race([
      refreshPromise,
      new Promise(r => setTimeout(r, timeout))
    ]).finally(stopRefresh)
  },

  onReachBottom() {
    this.loadMorePosts()
  },

  methods: {
    
    // 校验是否为有效用户ID（排除 null、undefined、字符串 "undefined"、非数字）
    isValidUserId(id) {
      if (id == null || id === '' || id === 'undefined') return false
      const n = Number(id)
      return Number.isFinite(n) && n > 0
    },
    // 获取当前登录用户ID
    getCurrentUserId() {
      try {
        const token = uni.getStorageSync('token')
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo && (userInfo.id || userInfo.uid)) {
          const id = userInfo.id || userInfo.uid
          this.currentUserId = this.isValidUserId(id) ? (typeof id === 'number' ? id : Number(id)) : null
          return
        }
        const userId = uni.getStorageSync('userId')
        if (userId !== undefined && userId !== null && userId !== '') {
          this.currentUserId = this.isValidUserId(userId) ? (typeof userId === 'number' ? userId : Number(userId)) : null
          return
        }
        if (token) {
          this.fetchCurrentUserFromAPI()
        } else {
          this.currentUserId = null
        }
      } catch (error) {
        console.error('获取用户ID失败:', error)
        this.currentUserId = null
      }
    },
    
    /** 仅 token 在本地、userInfo 未同步时，先拉用户信息再请求发现流，否则后端收不到 userId，点赞态全为未赞 */
    async ensureCurrentUserReady() {
      this.getCurrentUserId()
      if (this.isValidUserId(this.currentUserId)) return
      const token = uni.getStorageSync('token')
      if (token) {
        await this.fetchCurrentUserFromAPI()
        this.getCurrentUserId()
      }
    },

    // 从API获取当前用户信息
    async fetchCurrentUserFromAPI() {
      try {
        const response = await api.getCurrentUser()
        if (response && (response.code === 200 || response.code === 0)) {
          const userInfo = response.data
          if (userInfo && (userInfo.id || userInfo.uid)) {
            this.currentUserId = userInfo.id || userInfo.uid
            // 保存到本地存储
            uni.setStorageSync('userInfo', userInfo)
          }
        }
      } catch (error) {
        console.error('从API获取用户信息失败:', error)
        this.currentUserId = null
      }
    },
    
    async testApiConnection() {
      try {
        const base = (util.getApiBaseUrl && util.getApiBaseUrl()) || ''
        const testUrl = (base.replace(/\/+$/, '') || 'https://situationship.icu') + '/api/community/posts?page=1&size=5'
        const response = await uni.request({
          url: testUrl,
          method: 'GET',
          header: {
            'Content-Type': 'application/json'
          }
        })

      } catch (error) {
        console.error('API连接测试失败:', error)
      }
    },

    switchTab(index) {
      this.currentTabIndex = index
      this.currentTab = this.tabs[index].key
      this.updateSearchPlaceholder(index)
      if (index === 0) {
        this.followTabLoading = true
        Promise.all([
          this.loadFollowedUsers(),
          this.loadRecommendedUsers(),
          this.loadFollowPosts()
        ]).finally(() => {
          this.followTabLoading = false
        })
      } else if (index === 1) {
        this.loadPosts()
      } else if (index === 2) {
        this.loadDailyTopics()
      }
    },

    onSwiperChange(e) {
      const index = e.detail.current
      this.currentTabIndex = index
      this.currentTab = this.tabs[index].key
      this.updateSearchPlaceholder(index)
      if (index === 0) {
        this.followTabLoading = true
        Promise.all([
          this.loadFollowedUsers(),
          this.loadRecommendedUsers(),
          this.loadFollowPosts()
        ]).finally(() => {
          this.followTabLoading = false
        })
      } else if (index === 1) {
        this.loadPosts()
      } else if (index === 2) {
        this.loadDailyTopics()
      }
    },

    updateSearchPlaceholder(index) {
      // 只在发现显示搜索框
      if (index === 1) {
        // 发现
        this.searchPlaceholder = '搜索帖子'
      }
    },

    async loadFollowedUsers(silent = false) {
      if (!this.currentUserId) this.getCurrentUserId()
      if (!this.isValidUserId(this.currentUserId)) {
        this.followedUsers = []
        return
      }
      const uid = typeof this.currentUserId === 'number' ? this.currentUserId : Number(this.currentUserId)
      const showLoading = !silent
      try {
        const response = await api.getFollowedUsers(uid, showLoading)
        if (response && (response.code === 200 || response.code === 0)) {
          this.followedUsers = response.data || []
        } else {
          this.followedUsers = []
        }
      } catch (error) {
        console.error('加载关注用户失败:', error)
        this.followedUsers = []
      }
    },

    async loadRecommendedUsers(silent = false) {
      if (!this.currentUserId) {
        this.getCurrentUserId()
      }
      // 若仍未设置（例如 getCurrentUserId 触发了异步 fetch），再从 storage 同步读一次，避免新号/刚登录时漏请求推荐
      if (!this.currentUserId) {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo && (userInfo.id || userInfo.uid)) {
          this.currentUserId = userInfo.id || userInfo.uid
        } else {
          const uid = uni.getStorageSync('userId')
          if (uid) this.currentUserId = uid
        }
      }
      if (!this.isValidUserId(this.currentUserId)) {
        this.recommendedUsers = []
        return
      }
      const uid = typeof this.currentUserId === 'number' ? this.currentUserId : Number(this.currentUserId)
      const showLoading = !silent
      try {
        const response = await api.getRecommendedUsers({ userId: uid, showLoading })

        if (response && (response.code === 200 || response.code === 0)) {
          const raw = response.data
          const list = Array.isArray(raw) ? raw : (raw && raw.users ? raw.users : [])

          if (Array.isArray(list) && list.length > 0) {
            // 确保每个用户对象都有id字段
            const mappedUsers = list.map(user => ({
              ...user,
              id: user.id || user.userId || user.uid || null,
              name: this.getRecommendDisplayName(user),
              avatar: util.getImageUrl(user.avatar || user.img || '/static/images/login-dog.png'),
              desc: user.desc || user.bio || user.signature || ''
            }))
            
            // 直接使用API返回的用户，不再补充兜底用户
            this.recommendedUsers = mappedUsers
            
            // 为每个推荐用户加载帖子数据
            this.loadUserPostsForRecommended()

          } else {
            // 后端暂无推荐时，显示空列表
            this.recommendedUsers = []
          }
        } else {
          // 接口错误时显示空列表
          this.recommendedUsers = []
        }
      } catch (error) {
        console.error('加载推荐用户失败:', error)
        // 失败时显示空列表
        this.recommendedUsers = []
      }
    },
    
    // 为推荐用户加载帖子数据
    async loadUserPostsForRecommended() {
      if (!this.recommendedUsers || this.recommendedUsers.length === 0) {
        return
      }
      
      // 为每个用户加载最新的3个帖子
      for (let i = 0; i < this.recommendedUsers.length; i++) {
        const user = this.recommendedUsers[i]
        if (!user.id) continue
        
        try {
          // 获取该用户的最新帖子
          const response = await api.getPostList({
            page: 1,
            size: 3,
            userId: user.id
          })
          
          if (response && (response.code === 200 || response.code === 0)) {
            const posts = response.data?.posts || []
            // 处理帖子数据，提取图片和标题
            const processedPosts = posts.map(post => {
              const images = this.getPostImages(post)
              return {
                id: post.id,
                image: images.length > 0 ? images[0] : '',
                thumbnail: images.length > 0 ? images[0] : '',
                caption: post.title || post.content?.substring(0, 20) || ''
              }
            })
            
            // 使用Vue.set确保响应式更新
            this.$set(this.recommendedUsers[i], 'posts', processedPosts)
          }
        } catch (error) {
          console.error(`加载用户 ${user.id} 的帖子失败:`, error)
          // 如果加载失败，使用空数组
          this.$set(this.recommendedUsers[i], 'posts', [])
        }
      }
    },

    async loadFollowPosts() {
      if (!this.currentUserId) this.getCurrentUserId()
      if (!this.isValidUserId(this.currentUserId)) {
        this.followPosts = []
        return
      }
      const uid = typeof this.currentUserId === 'number' ? this.currentUserId : Number(this.currentUserId)
      try {
        const response = await api.getFollowingPosts(uid, {
          page: 1,
          size: 20
        })
        if (response && (response.code === 200 || response.code === 0)) {
          this.followPosts = response.data.posts || response.data || []
          this.followPostCoverFailed = {} // 重新加载时清空封面失败标记
        } else {
          this.followPosts = []
        }
      } catch (error) {
        console.error('加载关注帖子失败:', error)
        this.followPosts = []
      }
    },

    // 格式化数字
    formatNumber(num) {
      if (!num) return '0'
      if (num >= 10000) {
        return (num / 10000).toFixed(1) + 'w'
      } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k'
      }
      return num.toString()
    },

    arrangeFollowWaterfall() {
      this.leftFollowPosts = []
      this.rightFollowPosts = []
      
      this.followPosts.forEach((post, index) => {
        // 处理图片路径
        const postImages = this.getPostImages(post)
        // 确保主图片路径经过getImageUrl处理，且不为空
        let mainImage = ''
        if (postImages && postImages.length > 0 && postImages[0]) {
          const img = postImages[0]
          if (!img.includes('/cat') && !img.includes('cat') && img.trim()) {
            mainImage = this.getImageUrl(img)
          }
        }
        
        // 如果没有有效图片，使用默认图片
        if (!mainImage) {
          mainImage = this.getImageUrl('/static/images/default-product.svg')
        }
        
        // 格式化数据，匹配模板需要的格式
        const item = {
          id: post.id,
          img: mainImage,
          desc: post.title || post.content || '精彩内容',
          avator: this.getImageUrl(post.userAvatar || post.avatar || '/static/images/garfield-default-avatar.png'),
          uname: post.userName || post.username || '用户',
          like: post.likesCount || post.likeCount || 0,
          post: post // 保存原始post对象，用于跳转详情
        }
        
        // 交替分配到左右两列
        if (index % 2 === 0) {
          this.leftFollowPosts.push(item)
        } else {
          this.rightFollowPosts.push(item)
        }
      })
    },

    // 通过索引关注用户（更可靠的方法）
    async followUserByIndex(index, userParam) {
      // 优先使用传入的user参数，否则从数组获取
      let user = userParam
      if (!user && index !== undefined && index !== null && index >= 0) {
        user = this.recommendedUsers[index]
      }
      
      // 如果还是没有，尝试从event中获取
      if (!user && arguments[0] && typeof arguments[0] === 'object') {
        const event = arguments[0]
        if (event.currentTarget || event.target) {
          const target = event.currentTarget || event.target
          const dataset = target.dataset || {}
          const eventIndex = dataset.index !== undefined ? parseInt(dataset.index) : null
          const eventUserId = dataset.userId
          
          if (eventIndex !== null && eventIndex >= 0) {
            user = this.recommendedUsers[eventIndex]
          } else if (eventUserId) {
            // 如果只有userId，从数组中找到对应的用户
            user = this.recommendedUsers.find(u => {
              const uid = u.id || u.userId || u.uid
              return uid == eventUserId
            })
          }
        }
      }
      
      // 最后验证
      if (!user || typeof user !== 'object') {
        console.error('followUserByIndex: 无法获取用户对象', {
          user,
          index,
          userParam,
          recommendedUsersLength: this.recommendedUsers.length,
          recommendedUsers: this.recommendedUsers
        })
        uni.showToast({ 
          title: '无法获取用户信息', 
          icon: 'none' 
        })
        return
      }
      
      // 调用原始的followUser方法
      await this.followUser(user)
    },
    
    // 实际的关注逻辑
    async followUser(user) {
      if (!user || typeof user !== 'object') {
        console.error('followUser: 无效的用户对象', user)
        uni.showToast({ 
          title: '无效的用户信息', 
          icon: 'none' 
        })
        return
      }
      
      // 获取被关注用户的ID（支持多种字段名）
      let followingId = user.id || user.userId || user.uid || user.user_id || null
      
      // 获取当前登录用户ID
      if (!this.currentUserId) {
        this.getCurrentUserId()
      }
      const followerId = this.currentUserId
      
      // 验证用户ID
      if (!followingId || followingId === null || followingId === undefined) {
        console.error('followUser: 无效的关注用户ID', {
          user,
          id: user?.id,
          userId: user?.userId,
          uid: user?.uid,
          user_id: user?.user_id,
          fullUser: JSON.stringify(user)
        })
        uni.showToast({ 
          title: '无效的用户，无法关注', 
          icon: 'none' 
        })
        return
      }
      
      if (!followerId || followerId === null || followerId === undefined) {
        console.error('followUser: 当前用户ID缺失，尝试重新获取')
        // 再次尝试获取用户ID（同步方式，因为getCurrentUserId是同步的）
        this.getCurrentUserId()
        const retryFollowerId = this.currentUserId
        
        if (!retryFollowerId || retryFollowerId === null || retryFollowerId === undefined) {
          // 检查token是否存在
          const token = uni.getStorageSync('token')
          if (!token) {
            uni.showToast({ 
              title: '未登录，请先登录', 
              icon: 'none',
              duration: 2000
            })
            setTimeout(() => {
              uni.navigateTo({
                url: '/pages-auth/login'
              })
            }, 1500)
          } else {
            // 有token但没有用户ID，可能是userInfo丢失，尝试从API获取
            await this.fetchCurrentUserFromAPI()
            const apiFollowerId = this.currentUserId
            
            if (!apiFollowerId) {
              uni.showToast({ 
                title: '获取用户信息失败，请重新登录', 
                icon: 'none',
                duration: 2000
              })
              setTimeout(() => {
                uni.reLaunch({
                  url: '/pages-auth/login'
                })
              }, 1500)
              return
            }
            
            // 如果从API获取到了，继续执行关注
            // 继续执行下面的关注逻辑，使用apiFollowerId
            const finalFollowerId = apiFollowerId
            
            try {
              uni.showLoading({ title: '关注中...' })
              const result = await api.followUser(followingId, finalFollowerId)
              uni.hideLoading()
              
              if (result && (result.code === 200 || result.code === 0)) {
                uni.showToast({
                  title: '关注成功',
                  icon: 'success'
                })
                // 从推荐列表中移除已关注用户
                this.recommendedUsers = this.recommendedUsers.filter(u => {
                  const uid = u.id || u.userId || u.uid
                  return uid !== followingId
                })
              } else {
                uni.showToast({
                  title: result?.msg || '关注失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              uni.hideLoading()
              console.error('关注用户失败:', error)
              uni.showToast({
                title: error?.message || '关注失败',
                icon: 'none'
              })
            }
            return
          }
          return
        }
        
        // 如果重试后获取到了，使用新的ID继续执行
        // 更新followerId变量，继续执行下面的关注逻辑
        const finalFollowerId = retryFollowerId
        
        try {
          uni.showLoading({ title: '关注中...' })
          const result = await api.followUser(followingId, finalFollowerId)
          uni.hideLoading()
          
          if (result && (result.code === 200 || result.code === 0)) {
            uni.showToast({
              title: '关注成功',
              icon: 'success'
            })
            // 从推荐列表中移除已关注用户
            this.recommendedUsers = this.recommendedUsers.filter(u => {
              const uid = u.id || u.userId || u.uid
              return uid !== followingId
            })
          } else {
            uni.showToast({
              title: result?.msg || '关注失败',
              icon: 'none'
            })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('关注用户失败:', error)
          uni.showToast({
            title: error?.message || '关注失败',
            icon: 'none'
          })
        }
        return
      }
      
      try {
        uni.showLoading({ title: '关注中...' })
        const result = await api.followUser(followingId, followerId)
        uni.hideLoading()
        
        if (result && (result.code === 200 || result.code === 0)) {
          uni.showToast({
            title: '关注成功',
            icon: 'success'
          })
          // 从推荐列表中移除已关注用户
          this.recommendedUsers = this.recommendedUsers.filter(u => {
            const uid = u.id || u.userId || u.uid
            return uid !== followingId
          })
        } else {
          uni.showToast({
            title: result?.msg || '关注失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('关注用户失败:', error)
        uni.showToast({
          title: error?.message || '关注失败',
          icon: 'none'
        })
      }
    },

    goToUserContent(user, imageIndex) {
    },


    onImageError(e) {
      // 统一使用 handleImageError 处理图片错误
      this.handleImageError(e)
    },

    // 为你推荐列表：头像加载失败时回退默认头像
    onRecommendAvatarError(e, user, index) {
      if (!this.recommendedUsers || index < 0 || index >= this.recommendedUsers.length) return
      const cur = this.recommendedUsers[index].avatar || ''
      if (cur && (cur.includes('login-dog') || cur.includes('default'))) return
      const defaultAvatar = this.getImageUrl('/static/images/login-dog.png')
      this.$set(this.recommendedUsers[index], 'avatar', defaultAvatar)
    },

    // 跳转到用户个人主页（为你推荐列表点头像/昵称）
    goToUserProfile(user) {
      const uid = user && (user.id || user.userId || user.uid)
      if (uid == null || uid === '') return
      uni.navigateTo({ url: `/user/profile?userId=${uid}` })
    },

    // 为你推荐列表展示名：优先昵称，账号型用户名（wx_、微信用户等）显示为「用户+ID」
    getRecommendDisplayName(user) {
      if (!user) return '用户'
      const nickname = (user.nickname || '').trim()
      if (nickname) return nickname
      const name = (user.name || user.username || '').trim()
      if (this.isAccountLikeUsername(name)) {
        const id = user.id || user.userId || user.uid
        return id != null ? '用户' + id : '用户'
      }
      return name || (user.id != null ? '用户' + user.id : '用户')
    },
    isAccountLikeUsername(s) {
      if (!s || typeof s !== 'string') return true
      const t = s.trim()
      return t.startsWith('wx_') || t.startsWith('微信用户') || t.startsWith('wechat_')
    },

    onImageLoad(e) {
    },

    // 格式化视频时长（秒数转 MM:SS）
    formatVideoDuration(seconds) {
      if (!seconds && seconds !== 0) return ''
      const sec = Math.floor(Number(seconds))
      if (isNaN(sec) || sec < 0) return ''
      const minutes = Math.floor(sec / 60)
      const remainingSeconds = sec % 60
      return `${String(minutes).padStart(2, '0')}:${String(remainingSeconds).padStart(2, '0')}`
    },

    // 获取瀑布流卡片的key
    getWaterfallKey(item, index, column) {
      if (!item) return column + '-' + index
      return item.id ? column + '-' + item.id : column + '-' + index
    },

    // 获取瀑布流卡片的id
    getWaterfallId(item, index, column) {
      if (!item) return 'waterfall-' + column + '-' + index
      const id = item.id ? item.id : index
      return 'waterfall-' + column + '-' + id
    },

    // 判断帖子/item 是否为视频（用于构建视频列表、上下滑切换）
    /** 关注流单图时用 aspectFit 易留灰边；视频帖封面需 aspectFill 铺满方形容器 */
    getFollowPostImageMode(post) {
      const n = (this.getPostDisplayImages(post) || []).length
      if (this.postHasVideo(post)) return 'aspectFill'
      return n === 1 ? 'aspectFit' : 'aspectFill'
    },

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
      if (!v) {
        let imgs = []
        if (post.images) {
          if (typeof post.images === 'string') {
            try { imgs = JSON.parse(post.images) } catch (e) { imgs = [post.images] }
          } else if (Array.isArray(post.images)) imgs = post.images
        }
        if (imgs.length) {
          const exts = ['.mp4', '.mov', '.avi', '.m4v', '.webm', '.3gp']
          v = imgs.some(img => typeof img === 'string' && exts.some(ext => img.toLowerCase().includes(ext)))
        }
      }
      return v
    },

    // 直接复制Vue项目的方法
    selectedNote(item, index) {
      // 如果item为空，尝试从索引获取
      if (!item && index !== undefined && index !== null && index >= 0) {
        if (this.followPosts && this.followPosts[index]) {
          item = this.followPosts[index]
        }
      }
      
      // 检查item是否存在
      if (!item) {
        console.error('selectedNote: item为空且无法从索引获取')
        return
      }
      
      // 尝试多种方式获取postId，按优先级顺序
      let postId = null
      
      // 方式1: 直接使用 item.id（最常见）
      if (item.id !== undefined && item.id !== null && item.id !== 'undefined' && String(item.id).trim() !== '') {
        postId = item.id
      }
      // 方式2: 使用 item.postId
      else if (item.postId !== undefined && item.postId !== null && item.postId !== 'undefined' && String(item.postId).trim() !== '') {
        postId = item.postId
      }
      // 方式3: 使用 item.post?.id
      else if (item.post && item.post.id !== undefined && item.post.id !== null && item.post.id !== 'undefined' && String(item.post.id).trim() !== '') {
        postId = item.post.id
      }
      
      // 验证postId是否有效
      if (!postId || postId === 'undefined' || postId === 'null' || String(postId).trim() === '') {
        console.error('selectedNote: 帖子ID不存在或无效', {
          item,
          id: item.id,
          postId: item.postId,
          post: item.post,
          allKeys: Object.keys(item)
        })
        uni.showToast({
          title: '帖子ID不存在',
          icon: 'none'
        })
        return
      }
      
      // 确保postId是数字或有效的字符串
      const finalPostId = String(postId).trim()
      // 判断帖子类型：图片还是视频
      let hasVideo = false
      
      // 方法1: 从item中直接获取videos字段
      if (item.videos) {
        try {
          let videosData = item.videos
          if (typeof videosData === 'string') {
            videosData = JSON.parse(videosData)
          }
          if (Array.isArray(videosData) && videosData.length > 0) {
            hasVideo = true
          } else if (videosData && typeof videosData === 'object' && videosData.url) {
            hasVideo = true
          }
        } catch (e) {
          // 静默处理错误
        }
      }
      
      // 方法2: 从item.post中获取videos字段
      if (!hasVideo && item.post && item.post.videos) {
        try {
          let videosData = item.post.videos
          if (typeof videosData === 'string') {
            videosData = JSON.parse(videosData)
          }
          if (Array.isArray(videosData) && videosData.length > 0) {
            hasVideo = true
          } else if (videosData && typeof videosData === 'object' && videosData.url) {
            hasVideo = true
          }
        } catch (e) {
          // 静默处理错误
        }
      }
      
      // 方法3: 从posts数组中查找对应的post
      if (!hasVideo && this.posts && this.posts.length > 0) {
        const post = this.posts.find(p => p.id == finalPostId || String(p.id) === finalPostId)
        if (post && post.videos) {
          try {
            let videosData = post.videos
            if (typeof videosData === 'string') {
              videosData = JSON.parse(videosData)
            }
            if (Array.isArray(videosData) && videosData.length > 0) {
              hasVideo = true
            } else if (videosData && typeof videosData === 'object' && videosData.url) {
              hasVideo = true
            }
          } catch (e) {
            // 静默处理错误
          }
        }
      }
      
      // 方法4: 检查images字段中是否有视频文件
      if (!hasVideo) {
        let images = []
        if (item.images) {
          if (typeof item.images === 'string') {
            try {
              images = JSON.parse(item.images)
            } catch (e) {
              images = [item.images]
            }
          } else if (Array.isArray(item.images)) {
            images = item.images
          }
        } else if (item.post && item.post.images) {
          if (typeof item.post.images === 'string') {
            try {
              images = JSON.parse(item.post.images)
            } catch (e) {
              images = [item.post.images]
            }
          } else if (Array.isArray(item.post.images)) {
            images = item.post.images
          }
        }
        
        if (images && images.length > 0) {
          const videoExtensions = ['.mp4', '.mov', '.avi', '.m4v', '.webm', '.3gp']
          const hasVideoInImages = images.some(img => {
            if (typeof img === 'string') {
              const url = img.toLowerCase()
              return videoExtensions.some(ext => url.includes(ext))
            }
            return false
          })
          if (hasVideoInImages) {
            hasVideo = true
          }
        }
      }
      
      const likeCount = (item.post && (item.post.likesCount || item.post.likeCount)) || item.like || 0
      const isLiked = !!(item.isLiked || (item.post && item.post.isLiked))
      let targetUrl = ''
      if (hasVideo) {
        const feed = this.currentTabIndex === 0 ? (this.followPosts || []) : (this.posts || [])
        let videoIds = feed.filter(pp => this.postHasVideo(pp)).map(pp => String(pp.id || pp.postId || (pp.post && pp.post.id) || '').trim()).filter(Boolean)
        // 关注流可能仅靠「发现列表」判为视频，本条在 follow 里未带 videos，筛列表会漏掉 finalPostId，导致详情用 ids[0] 错帖、视频 0:00
        if (!videoIds.includes(finalPostId)) {
          videoIds = [finalPostId, ...videoIds]
        }
        const ids = videoIds.length ? videoIds : [finalPostId]
        let idx = ids.indexOf(finalPostId)
        if (idx < 0) idx = 0
        const firstVideo = this.getFirstVideoUrlAndCover(item)
        if (firstVideo && firstVideo.url) {
          const app = getApp()
          if (!app.globalData) app.globalData = {}
          app.globalData.pendingVideoDetail = app.globalData.pendingVideoDetail || {}
          app.globalData.pendingVideoDetail[finalPostId] = { url: firstVideo.url, cover: firstVideo.cover || '' }
        }
        targetUrl = `/pages-community/post-detail-video?id=${finalPostId}&ids=${ids.join(',')}&index=${idx}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      } else {
        targetUrl = `/pages-community/post-detail-image?id=${finalPostId}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      }

      // uni-app 会自动加载分包，直接跳转即可
      uni.navigateTo({
        url: targetUrl,
        success: (res) => {
        },
        fail: (err) => {
          console.error('selectedNote: 跳转失败', err)
          console.error('selectedNote: 错误详情 - errMsg:', err.errMsg)
          uni.showToast({
            title: '跳转失败: ' + (err.errMsg || '未知错误'),
            icon: 'none',
            duration: 3000
          })
        },
        complete: () => {
        }
      })
    },

    // 通过索引跳转到帖子详情（更可靠的方法）
    selectedNoteByIndex(index) {
      if (index < 0 || index >= this.followPosts.length) {
        console.error('索引无效，无法跳转:', index, 'followPosts长度:', this.followPosts.length)
        return
      }
      
      const post = this.followPosts[index]
      if (!post) {
        console.error('帖子不存在，索引:', index)
        return
      }
      
      // 尝试多种可能的ID字段
      const postId = post.id || post.postId || post.post_id
      
      if (!postId || postId === 'undefined' || postId === 'null' || postId === '') {
        console.error('帖子ID无效，无法跳转:', postId, 'post对象:', post)
        uni.showToast({
          title: '帖子ID无效',
          icon: 'none'
        })
        return
      }
      
      const hasVideo = this.postHasVideo(this.selectedNote || this.findPostById(postId))
      if (hasVideo) {
        uni.navigateTo({
          url: `/pages-community/post-detail-video?id=${postId}`
        })
      } else {
        uni.navigateTo({
          url: `/pages-community/post-detail-image?id=${postId}`
        })
      }
    },

    getPostImage(post) {
      try {
        let imageUrl = ''
        // 处理字符串格式的图片数据
        if (post.images && typeof post.images === 'string') {
          const images = JSON.parse(post.images)
          if (Array.isArray(images) && images.length > 0) {
            imageUrl = images[0]
          }
        } else if (post.images && Array.isArray(post.images) && post.images.length > 0) {
          // 处理数组格式的图片数据
          imageUrl = post.images[0]
        } else if (post.coverImage) {
          // 如果有关联图片，使用关联图片
          imageUrl = post.coverImage
        }
        
        // 使用util.getImageUrl处理图片路径
        return imageUrl ? util.getImageUrl(imageUrl) : ''
      } catch (error) {
        console.error('解析图片数据失败:', error)
        return ''
      }
    },

    // 从列表项中取出第一个视频的 url 和 cover，用于详情页预加载（一点就播）
    getFirstVideoUrlAndCover(item) {
      try {
        let videosData = item.videos || (item.post && item.post.videos)
        if (typeof videosData === 'string') {
          try {
            videosData = JSON.parse(videosData)
          } catch (e) {
            if (videosData.includes('.mp4') || videosData.includes('.mov')) {
              return { url: videosData, cover: (item.coverImage || item.post?.coverImage) || '' }
            }
            return null
          }
        }
        if (Array.isArray(videosData) && videosData.length > 0) {
          const first = videosData[0]
          const url = typeof first === 'string' ? first : (first && (first.url || first.src || first.videoUrl || first.path))
          const cover = (first && (first.thumb || first.cover)) || item.coverImage || (item.post && item.post.coverImage) || ''
          return url ? { url, cover } : null
        }
        if (videosData && typeof videosData === 'object') {
          const u = videosData.url || videosData.src || videosData.videoUrl || videosData.path
          if (u) {
            return {
              url: u,
              cover: videosData.thumb || videosData.cover || item.coverImage || (item.post && item.post.coverImage) || ''
            }
          }
        }
        const images = item.images || (item.post && item.post.images)
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
        const videoUrl = list.find(img => typeof img === 'string' && videoExt.some(ext => img.toLowerCase().includes(ext)))
        if (videoUrl) {
          return { url: videoUrl, cover: item.coverImage || (item.post && item.post.coverImage) || '' }
        }
      } catch (e) {}
      return null
    },

    formatDate(dateString) {
      if (!dateString) return ''
      // 修复iOS日期格式兼容性问题
      let date
      if (dateString.includes(' ')) {
        // 将 "yyyy-MM-dd HH:mm:ss" 格式转换为 "yyyy-MM-ddTHH:mm:ss" 格式
        date = new Date(dateString.replace(' ', 'T'))
      } else {
        date = new Date(dateString)
      }
      
      if (isNaN(date.getTime())) {
        return ''
      }
      
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      return `${month}-${day}`
    },

    // 兼容接口字段：isLiked / liked
    normalizePostEngagementFields(posts) {
      if (!posts || !Array.isArray(posts)) return
      posts.forEach((post) => {
        if (!post) return
        post.isLiked = !!(post.isLiked ?? post.liked)
      })
    },

    // 将帖子中所有图片/封面 URL 统一规范为当前可访问的域名（避免 localhost 导致发现流封面黑块）
    normalizePostImageUrls(posts) {
      if (!posts || !Array.isArray(posts)) return
      const PROD_ORIGIN = 'https://situationship.icu'
      const toProdUrl = (url) => {
        if (!url || typeof url !== 'string') return url
        const s = url.trim()
        if (!s) return url
        const m = s.match(/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?(\/.*)$/i)
        if (m) return PROD_ORIGIN + (m[3] || '/')
        return util.getImageUrl(s)
      }
      const getImg = (url) => (url && typeof url === 'string' && url.trim() ? toProdUrl(url) : url)
      posts.forEach(post => {
        if (!post) return
        if (post.coverImage && typeof post.coverImage === 'string') {
          post.coverImage = getImg(post.coverImage)
        }
        if (post.cover_image && typeof post.cover_image === 'string') {
          post.cover_image = getImg(post.cover_image)
        }
        if (post.images) {
          if (Array.isArray(post.images)) {
            post.images = post.images.map(img => (img && typeof img === 'string' ? getImg(img) : img))
          } else if (typeof post.images === 'string' && post.images.trim()) {
            try {
              const arr = JSON.parse(post.images)
              if (Array.isArray(arr)) {
                post.images = JSON.stringify(arr.map(img => (img && typeof img === 'string' ? getImg(img) : img)))
              }
            } catch (e) {}
          }
        }
        if (post.videos) {
          try {
            let list = post.videos
            if (typeof list === 'string') list = JSON.parse(list)
            if (Array.isArray(list)) {
              list.forEach(v => {
                if (v && typeof v === 'object') {
                  if (v.thumb) v.thumb = getImg(v.thumb)
                  if (v.customThumb) v.customThumb = getImg(v.customThumb)
                  if (v.cover) v.cover = getImg(v.cover)
                }
              })
              post.videos = list
            } else if (list && typeof list === 'object') {
              if (list.thumb) list.thumb = getImg(list.thumb)
              if (list.customThumb) list.customThumb = getImg(list.customThumb)
              if (list.cover) list.cover = getImg(list.cover)
            }
          } catch (e) {}
        }
      })
    },

    async loadDiscoverCategories() {
      try {
        const response = await api.getDiscoverTags()
        if (response && (response.code === 200 || response.code === 0) && Array.isArray(response.data)) {
          const normalized = response.data
            .map(t => (t == null ? '' : String(t).trim()))
            .filter(Boolean)
          const unique = []
          const seen = new Set()
          ;['推荐', ...normalized].forEach(t => {
            const key = t === '推荐' ? 'recommend' : t
            if (!seen.has(key)) {
              seen.add(key)
              unique.push({ key, label: t })
            }
          })
          this.categories = unique.length > 0 ? unique : [{ key: 'recommend', label: '推荐' }]
          if (!this.categories.some(c => c.key === this.currentCategory)) {
            this.currentCategory = 'recommend'
          }
          return
        }
      } catch (e) {}
      this.categories = [{ key: 'recommend', label: '推荐' }]
      this.currentCategory = 'recommend'
    },

    selectDiscoverCategory(categoryKey) {
      if (!categoryKey || this.currentCategory === categoryKey) return
      this.currentCategory = categoryKey
      this.posts = []
      this.leftDisList = []
      this.rightDisList = []
      this.page = 1
      this.hasMore = true
      this.loadPosts()
    },

    /** 拉取关注的人近期帖子，用于与发现推荐合并（抖音式：推荐里也能刷到已关注博主） */
    async fetchFollowPostsForDiscoverMerge() {
      if (!this.isValidUserId(this.currentUserId)) return []
      const uid = typeof this.currentUserId === 'number' ? this.currentUserId : Number(this.currentUserId)
      try {
        const response = await api.getFollowingPosts(uid, { page: 1, size: 40 })
        if (!(response && (response.code === 200 || response.code === 0))) return []
        let list = response.data.posts || response.data || []
        if (!Array.isArray(list)) return []
        const cat = this.currentCategory
        if (cat && cat !== 'recommend') {
          list = list.filter(p => this.postMatchesDiscoverCategory(p, cat))
        }
        return list
      } catch (e) {
        return []
      }
    },

    /** 当前发现子栏目为「视频/猫咪…」时，只混入同分类/标签下的关注帖，避免栏目被冲散 */
    postMatchesDiscoverCategory(post, categoryKey) {
      if (!post || !categoryKey || categoryKey === 'recommend') return true
      // 与后端「视频」栏目一致：带视频资源的帖即视为视频帖，不要求 category 手填「视频」
      const ck = String(categoryKey).trim()
      if (ck === '视频' || ck.toLowerCase() === 'video') {
        return this.postHasVideo(post)
      }
      // 与后端语义分栏一致：未点 #猫咪 但正文提到「猫」等，也可混入该栏
      const bag = this.postTextBagForDiscover(post)
      if (ck === '猫咪' || ck.toLowerCase() === 'cat') {
        if (bag.includes('熊猫')) return false
        return /猫咪|猫猫|喵星人|养猫|英短|布偶|橘猫|喵|猫/.test(bag)
      }
      if (ck === '狗狗' || ck.toLowerCase() === 'dog') {
        if (bag.includes('热狗')) return false
        return /狗狗|狗子|养狗|汪星人|柯基|金毛|哈士奇|[犬狗]/.test(bag)
      }
      if (ck === '养宠知识' || ck === '百科') {
        return /科普|攻略|教程|疫苗|驱虫|疾病|领养|注意|建议|干货|养宠|怎么养|如何养|为什么|指南|新手|误区|医院|症状|治疗/.test(bag)
      }
      const cat = (post.category || '').trim()
      const tagStr = post.tags != null ? String(post.tags) : ''
      const label = this.getCategoryLabel(categoryKey)
      const keys = [categoryKey, label].filter(Boolean)
      for (const k of keys) {
        if (!k) continue
        if (cat === k || (cat && cat.includes(k))) return true
        if (tagStr && (tagStr.includes(k) || tagStr.split(/[,，]/).some(t => t.trim() === k))) return true
      }
      return false
    },

    /** 与后端 postTextBag 对齐，用于发现子栏目语义匹配 */
    postTextBagForDiscover(post) {
      if (!post) return ''
      const parts = [post.title, post.content, post.tags, post.category]
      return parts
        .map(p => (p == null ? '' : typeof p === 'string' ? p : String(p)))
        .join(' ')
    },

    /**
     * 将关注博主的近期帖插到发现流前部并去重（与推荐算法结果叠加，不重复 id）
     */
    mergeFollowIntoDiscoverPosts(discoverPosts, followPosts) {
      const discover = Array.isArray(discoverPosts) ? [...discoverPosts] : []
      const idOf = (p) => (p != null && p.id != null && p.id !== undefined) ? String(p.id).trim() : ''
      if (!followPosts || !followPosts.length) return discover

      const discoverIdSet = new Set(discover.map(idOf).filter(Boolean))
      const followSorted = [...followPosts].sort((a, b) => {
        const ta = new Date(a.createTime || a.create_time || 0).getTime()
        const tb = new Date(b.createTime || b.create_time || 0).getTime()
        return tb - ta
      })
      const PREPEND_CAP = 10
      const head = []
      for (const p of followSorted) {
        if (head.length >= PREPEND_CAP) break
        const id = idOf(p)
        if (!id || discoverIdSet.has(id)) continue
        discoverIdSet.add(id)
        head.push(p)
      }
      if (!head.length) return discover
      const headIds = new Set(head.map(idOf).filter(Boolean))
      const rest = discover.filter(p => {
        const id = idOf(p)
        return !id || !headIds.has(id)
      })
      return [...head, ...rest]
    },

    // 发现页加载帖子：
    // 1) “推荐”分类且已登录：优先协同过滤
    // 2) 协同过滤失败/无数据/未登录：回退规则推荐
    // 3) 已登录：并行拉关注流，合并进列表（推荐/各栏目均可混入已关注博主，去重）
    async loadPosts(options = {}) {
      try {
        const fromPullRefresh = !!options.fromPullRefresh
        const forceNextBatch = !!options.forceNextBatch
        const followPromise = this.fetchFollowPostsForDiscoverMerge()
        const isRecommendCategory = !this.currentCategory || this.currentCategory === 'recommend'
        const canUseCollaborative = isRecommendCategory && !!this.currentUserId && !forceNextBatch

        // 推荐分类优先走协同过滤
        if (canUseCollaborative) {
          try {
            const [cfResponse, followPosts] = await Promise.all([
              api.getCollaborativeFiltering(this.currentUserId, 50),
              followPromise
            ])
            if ((cfResponse.code === 200 || cfResponse.code === 0) && cfResponse.data) {
              const cfPosts = cfResponse.data.posts || []
              if (cfPosts.length > 0) {
                let mergedPosts = this.mergeFollowIntoDiscoverPosts(cfPosts, followPosts)
                if (fromPullRefresh && isRecommendCategory) {
                  mergedPosts = this.shufflePosts(mergedPosts)
                }
                this.posts = mergedPosts
                this.normalizePostEngagementFields(this.posts)
                this.normalizePostImageUrls(this.posts)
                // 协同过滤接口当前是 limit 模式，不支持继续分页，首屏即可
                this.hasMore = false
                this.arrangeWaterfall()
                this.mergePostLikeCacheIntoDiscovery()
                return
              }
            }
          } catch (cfError) {
            // 协同过滤失败时静默回退规则推荐，避免页面空白
            console.warn('协同过滤推荐失败，回退规则推荐:', cfError)
          }
        }

        // 规则推荐（原有逻辑，作为默认与兜底）
        // 推荐分类下拉刷新时请求下一页，做到“真换一批”
        let requestPage = 1
        if (isRecommendCategory) {
          requestPage = forceNextBatch ? (this.discoverRecommendPage + 1) : 1
          if (!forceNextBatch) this.discoverRecommendPage = 1
        }

        const params = { page: requestPage, size: 50 }
        if (this.currentUserId) {
          params.userId = this.currentUserId
        }
        if (this.currentCategory && this.currentCategory !== 'recommend') {
          params.tag = this.currentCategory
        }
        const [response, followPosts] = await Promise.all([
          api.getDiscoverRecommend(params),
          followPromise
        ])
        if (response.code === 200 || response.code === 0) {
          let raw = response.data.posts || []
          // 下拉刷新推荐时：如果下一页数量偏少，回补第一页，保证列表始终“够多”
          if (fromPullRefresh && forceNextBatch && isRecommendCategory && raw.length < 20) {
            try {
              const fallbackRes = await api.getDiscoverRecommend({
                ...params,
                page: 1
              })
              if ((fallbackRes.code === 200 || fallbackRes.code === 0) && fallbackRes.data && Array.isArray(fallbackRes.data.posts)) {
                const fallbackPosts = fallbackRes.data.posts
                const seen = new Set()
                const mergedRaw = []
                ;[...raw, ...fallbackPosts].forEach(p => {
                  const id = p && (p.id || p.postId || p.noteId)
                  const key = id != null ? String(id) : `noid_${mergedRaw.length}`
                  if (!seen.has(key)) {
                    seen.add(key)
                    mergedRaw.push(p)
                  }
                })
                raw = mergedRaw
              }
            } catch (e) {
              // 回补失败不影响主流程，继续使用当前批次
            }
          }

          const mergedPosts = this.mergeFollowIntoDiscoverPosts(raw, followPosts)
          this.posts = mergedPosts
          if (isRecommendCategory) this.discoverRecommendPage = requestPage
          this.normalizePostEngagementFields(this.posts)
          this.normalizePostImageUrls(this.posts)
          // 推荐流允许持续下拉刷新；即使当前页不足，也可继续请求下一批或回补
          this.hasMore = isRecommendCategory ? true : ((response.data.posts || []).length >= (params.size || 50))
          this.arrangeWaterfall()
          this.mergePostLikeCacheIntoDiscovery()
        } else {
          this.posts = []
          this.leftDisList = []
          this.rightDisList = []
          this.hasMore = false
        }
      } catch (error) {
        console.error('加载推荐帖子失败:', error)
        this.posts = []
      }
    },

    async loadDailyTopicThemes() {
      try {
        const res = await api.getDailyTopicThemes()
        if (res && (res.code === 200 || res.code === 0) && Array.isArray(res.data) && res.data.length > 0) {
          this.dailyTopicCategories = res.data.map(t => ({ key: t.code, label: t.name }))
          if (!this.dailyTopicCategories.some(c => c.key === this.currentDailyTopicCategory)) {
            this.currentDailyTopicCategory = this.dailyTopicCategories[0].key
          }
        } else {
          this.dailyTopicCategories = [
            { key: 'recommend', label: '推荐' },
            { key: 'basic', label: '基本知识' },
            { key: 'feeding', label: '喂养' },
            { key: 'care', label: '护理' },
            { key: 'medical', label: '医疗' },
            { key: 'health', label: '健康' },
            { key: 'behavior', label: '行为' }
          ]
        }
      } catch (e) {
        this.dailyTopicCategories = [
          { key: 'recommend', label: '推荐' },
          { key: 'basic', label: '基本知识' },
          { key: 'feeding', label: '喂养' },
          { key: 'care', label: '护理' },
          { key: 'medical', label: '医疗' },
          { key: 'health', label: '健康' },
          { key: 'behavior', label: '行为' }
        ]
      }
    },

    async loadDailyTopics() {
      if (this.dailyTopicCategories.length === 0) await this.loadDailyTopicThemes()
      this.dailyLoading = true
      try {
        const response = await api.getDailyTopics()
        if (response && (response.code === 200 || response.code === 0) && response.data) {
          const data = response.data
          this.dailyTodayTheme = data.todayTheme || ''
          const raw = (data.topics || []).map(t => {
            const desc = t.description || ''
            return {
              id: t.id,
              title: t.title || '无标题',
              summary: desc.length > 80 ? desc.substring(0, 80) + '...' : (desc || '暂无描述'),
              thumb: t.coverImage || '/static/images/default-product.svg',
              theme: t.theme || ''
            }
          })
          this.dailyOfficialTopicsAll = raw
          this.dailyOfficialTopics = this.filterDailyTopicsByCategory(raw, this.currentDailyTopicCategory)
        } else {
          this.dailyTodayTheme = ''
          this.dailyOfficialTopicsAll = []
          this.dailyOfficialTopics = []
        }
      } catch (error) {
        console.error('加载每日专题失败:', error)
        this.dailyTodayTheme = ''
        this.dailyOfficialTopicsAll = []
        this.dailyOfficialTopics = []
        uni.showToast({ title: '加载专题失败，请检查网络或稍后重试', icon: 'none' })
      } finally {
        this.dailyLoading = false
      }
    },

    selectDailyTopicCategory(key) {
      if (this.currentDailyTopicCategory === key) return
      this.currentDailyTopicCategory = key
      this.dailyOfficialTopics = this.filterDailyTopicsByCategory(this.dailyOfficialTopicsAll, key)
    },
    /**
     * 宠物专题 Tab：与后台「主题分类」的 code 一一对应。
     * 之前「推荐」返回全部专题、其它栏用标题关键词猜，导致同一篇在「推荐」和「基本知识」等处重复出现。
     * 规则：有 theme 时只出现在对应 Tab；无 theme 的旧数据仅在「推荐」显示。
     */
    filterDailyTopicsByCategory(topics, categoryKey) {
      if (!topics || topics.length === 0) return []
      const tabKey = String(categoryKey || '').trim().toLowerCase()
      return topics.filter((t) => {
        const th = (t.theme || '').trim().toLowerCase()
        if (th) return th === tabKey
        return tabKey === 'recommend'
      })
    },
    goToTopicDetail(t, index) {
      if (!t || t.id == null) return
      uni.navigateTo({ url: `/pages-community/topic-detail?id=${t.id}&index=${index}` })
    },

    getCategoryLabel(categoryKey) {
      const category = this.categories.find(cat => cat.key === categoryKey)
      return category ? category.label : undefined
    },

    // 获取帖子的图片列表
    getPostImages(post) {
      try {
        let images = []
        if (post.images && typeof post.images === 'string') {
          try {
            const parsedImages = JSON.parse(post.images)
            images = Array.isArray(parsedImages) ? parsedImages.filter(img => img && img.trim()) : []
          } catch (parseError) {
            // 如果不是JSON数组，可能是单个图片路径字符串
            if (post.images.trim()) {
              images = [post.images.trim()]
            }
          }
        } else if (post.images && Array.isArray(post.images)) {
          images = post.images.filter(img => img && img.trim())
        }
        // 封面：优先帖子表封面（兼容 camelCase 与 snake_case）
        if (images.length === 0) {
          const cover = (post.coverImage && post.coverImage.trim()) || (post.cover_image && post.cover_image.trim())
          if (cover) images = [cover]
        }
        // 视频帖：无图片/封面时，用视频缩略图作为封面（thumb / customThumb / cover）
        if (images.length === 0 && post.videos) {
          try {
            let videosData = post.videos
            if (typeof videosData === 'string') videosData = JSON.parse(videosData)
            const first = Array.isArray(videosData) && videosData.length > 0 ? videosData[0] : (videosData && typeof videosData === 'object' ? videosData : null)
            if (first) {
              const thumbUrl = (first.thumb && first.thumb.trim()) || (first.customThumb && first.customThumb.trim()) || (first.cover && first.cover.trim())
              if (thumbUrl) images.push(thumbUrl)
            } else if (videosData && typeof videosData === 'object' && videosData.thumb) {
              images.push(videosData.thumb)
            }
          } catch (e) {}
        }
        
        // 过滤掉临时路径和无效路径，所有帖子图片应从数据库加载并使用 /upload/ 路径
        return images
          .filter(img => {
            if (!img || !img.trim()) return false
            // 过滤临时路径
            if (img.includes('/tmp/') || img.includes('/__tmp__/')) return false
            // 过滤示例图片路径（cat1-6.jpg等静态示例图片不应出现在帖子数据中）
            if (img.includes('/cat') && (img.includes('/static/') || img.includes('cat1') || img.includes('cat2') || img.includes('cat3') || img.includes('cat4') || img.includes('cat5') || img.includes('cat6'))) {
              return false
            }
            return true
          })
          .map(img => util.getImageUrl(img))
          .filter(img => img && img.trim()) // 再次过滤，确保处理后的路径也不为空
      } catch (error) {
        console.error('解析图片数据失败:', error, post)
        // 回退：帖子封面（兼容 cover_image）
        const cover = (post.coverImage && post.coverImage.trim()) || (post.cover_image && post.cover_image.trim())
        if (cover) {
          const url = util.getImageUrl(cover)
          if (url && !url.includes('/cat')) return [url]
        }
        // 视频帖回退：thumb / customThumb / cover
        if (post.videos) {
          try {
            let v = post.videos
            if (typeof v === 'string') v = JSON.parse(v)
            const first = Array.isArray(v) && v[0] ? v[0] : (v && typeof v === 'object' ? v : null)
            const thumbUrl = first && ((first.thumb && first.thumb.trim()) || (first.customThumb && first.customThumb.trim()) || (first.cover && first.cover.trim()))
            if (thumbUrl) return [util.getImageUrl(thumbUrl)]
          } catch (e) {}
        }
        return []
      }
    },

    // 关注流帖子头像（空或加载失败时用默认，支持 handleAvatarError 回退）
    getFollowPostAvatar(post) {
      if (!post) return '/static/images/garfield-default-avatar.png'
      const url = (post.userAvatar || post.avatar || '').trim()
      return url || '/static/images/garfield-default-avatar.png'
    },
    // 关注流头像加载失败时回退默认（微信等第三方头像可能不可用）
    handleAvatarError(e, post) {
      if (post && this.$set) {
        this.$set(post, 'userAvatar', '/static/images/garfield-default-avatar.png')
      }
    },
    // 帖子展示图列表：有图用图，无图或加载失败时用占位图，避免空白
    getPostDisplayImages(post) {
      const postId = post && (post.id || post.postId)
      const placeholder = '/static/images/garfield-default-avatar.png' // PNG 兼容性更好，避免 SVG 不显示
      if (postId && this.followPostCoverFailed[postId]) {
        return [placeholder]
      }
      const imgs = this.getPostImages(post)
      if (imgs && imgs.length > 0) return imgs
      // 无封面时也返回占位图，避免整块不渲染导致空白（含视频帖、或接口未返回 cover/thumb 的情况）
      return [placeholder]
    },
    // 关注流封面/视频缩略图加载失败时回退占位图（如 HTTP 被拦截）
    handleFollowPostImageError(e, post) {
      if (!post) return
      const postId = post.id || post.postId
      if (postId != null && !this.followPostCoverFailed[postId]) {
        this.$set(this.followPostCoverFailed, postId, true)
      }
    },
    // 发现流瀑布流封面加载失败时回退占位图，避免黑块
    handleWaterfallImageError(e, item, column, index) {
      if (item && item.img && !String(item.img).includes('default-product')) {
        this.$set(item, 'img', util.getImageUrl('/static/images/default-product.svg'))
      } else {
        this.handleImageError(e)
      }
      // 图片加载失败时 @load 不会触发，需手动推进瀑布流分配，否则后续帖子永远不显示
      if (item && !item.heightCalculated) {
        item.heightCalculated = true
        item.imageLoaded = true
        const fallbackHeight = 350
        if (column === 'left') {
          this.leftColumnHeight += fallbackHeight
        } else {
          this.rightColumnHeight += fallbackHeight
        }
        if (this.pendingItems && this.pendingItems.length > 0) {
          this.distributeWaterfallItems()
          this.$forceUpdate()
        }
      }
    },
    // 处理图片加载错误（真机常见：后端地址不可达或静态路径问题，不刷控制台）
    handleImageError(e) {
      // 在小程序环境中，直接修改 src 可能不起作用，使用 Vue 的响应式更新
      // 检查是否已经尝试过替换默认图片，避免无限循环
      if (e.target) {
        const currentSrc = e.target.src || (e.detail && e.detail.src) || ''
        const defaultImg = util.getImageUrl('/static/images/default-product.svg')
        
        // 如果当前已经是默认图片，不再替换，避免循环
        if (currentSrc && (currentSrc.includes('default-product') || currentSrc.includes('garfield-default-avatar'))) {
          return
        }
        
        // 如果失败的是示例图片（cat1-6），直接使用默认图片
        if (currentSrc && (currentSrc.includes('/cat') || currentSrc.includes('cat'))) {
          // 在小程序中，需要通过数据绑定来更新图片
          // 这里只记录错误，实际替换由数据层处理
        }
      }
    },

    // 切换点赞状态
    async toggleLike(post, index, event) {
      try {
        // 如果post是undefined，尝试多种方式恢复
        if (!post) {
          // 方式1: 从event中获取
          if (event && event.currentTarget) {
            const dataset = event.currentTarget.dataset || {}
            const postId = dataset.postId || dataset.post_id
            const postIndex = dataset.postIndex !== undefined ? parseInt(dataset.postIndex) : (index !== undefined ? index : -1)
            
            if (postIndex >= 0 && this.followPosts && this.followPosts[postIndex]) {
              post = this.followPosts[postIndex]
            } else if (postId && this.followPosts) {
              // 如果只有postId，从followPosts中查找
              post = this.followPosts.find(p => (p.id == postId || p.postId == postId))
            }
          }
          
          // 方式2: 如果index存在，直接从followPosts数组中获取
          if (!post && index !== undefined && index !== null && index >= 0 && this.followPosts && this.followPosts[index]) {
            post = this.followPosts[index]
          }
        }
        
        if (!post) {
          console.error('toggleLike: post对象不存在', { post, index, event, followPosts: this.followPosts })
          uni.showToast({
            title: '帖子信息不存在',
            icon: 'none'
          })
          return
        }
        
        const currentUserId = this.getCurrentUserId()
        if (!currentUserId) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          return
        }
        
        if (post.isLiked) {
          await api.unlikePost(post.id, currentUserId)
          post.isLiked = false
          post.likesCount = Math.max(0, (post.likesCount || post.likeCount || 0) - 1)
          post.likeCount = post.likesCount
        } else {
          await api.likePost(post.id, currentUserId)
          post.isLiked = true
          post.likesCount = (post.likesCount || post.likeCount || 0) + 1
          post.likeCount = post.likesCount
        }
      } catch (error) {
        console.error('点赞操作失败:', error)
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    },

    // 瀑布流中的点赞操作
    async toggleLikeInWaterfall(item, index, column) {
      try {
        // 检查用户是否登录
        const currentUserId = this.currentUserId || uni.getStorageSync('userId')
        if (!currentUserId) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          return
        }

        // 从item中获取post对象
        let post = item.post || this.posts.find(p => p.id === item.id)
        if (!post) {
          console.error('找不到对应的post对象')
          return
        }

        // 获取post在posts数组中的索引，以便同步更新
        const postIndex = this.posts.findIndex(p => p.id === post.id)
        if (postIndex === -1) {
          console.error('找不到post在posts数组中的位置')
          return
        }

        // 执行点赞操作
        if (post.isLiked) {
          await api.unlikePost(post.id, currentUserId)
          post.isLiked = false
          post.likesCount = Math.max(0, (post.likesCount || post.likeCount || 0) - 1)
          post.likeCount = post.likesCount
        } else {
          await api.likePost(post.id, currentUserId)
          post.isLiked = true
          post.likesCount = (post.likesCount || post.likeCount || 0) + 1
          post.likeCount = post.likesCount
        }

        // 同步更新posts数组中的post对象（确保刷新后状态保持）
        this.$set(this.posts, postIndex, { ...post })

        // 更新瀑布流中的item数据
        item.isLiked = post.isLiked
        item.like = post.likesCount || post.likeCount || 0
        item.post = post // 更新item中的post引用

        // 更新对应的列表
        if (column === 'left') {
          this.$set(this.leftDisList, index, item)
        } else {
          this.$set(this.rightDisList, index, item)
        }

        if (this.$store) {
          this.$store.commit('SET_POST_LIKE', {
            postId: post.id,
            isLiked: post.isLiked,
            likeCount: post.likesCount || post.likeCount || 0
          })
        }

        // 强制更新视图
        this.$forceUpdate()
      } catch (error) {
        console.error('瀑布流点赞操作失败:', error)
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    },

    // 从详情页返回时，把详情里点赞/取消点赞的状态合并到发现页列表，保证两边一致
    mergePostLikeCacheIntoDiscovery() {
      const cache = this.$store.state.postLikeCache
      if (!cache || typeof cache !== 'object') return
      const updateItem = (item) => {
        if (!item) return
        const postId = item.id || (item.post && item.post.id)
        if (!postId) return
        const key = String(postId)
        const cached = cache[key]
        if (!cached) return
        if (cached.isLiked !== undefined) {
          item.isLiked = cached.isLiked
          if (item.post) item.post.isLiked = cached.isLiked
        }
        if (cached.likeCount !== undefined) {
          item.like = cached.likeCount
          if (item.post) {
            item.post.likesCount = cached.likeCount
            item.post.likeCount = cached.likeCount
          }
        }
      }
      this.leftDisList.forEach(updateItem)
      this.rightDisList.forEach(updateItem)
      this.$forceUpdate()
    },

    // 重新排列帖子到瀑布流 - 小红书式动态高度分配
    arrangeWaterfall() {
      // 重置列高度和列表
      this.leftDisList = []
      this.rightDisList = []
      this.leftColumnHeight = 0
      this.rightColumnHeight = 0
      this.pendingItems = []
      
      // 将帖子转换为瀑布流卡片数据
      const items = this.posts.map(post => {
        // 处理图片路径
        let mainImage = ''
        let isVideo = false
        let videoDuration = ''
        
        // 优先检查是否有视频封面（视频帖子）
        if (post.videos && typeof post.videos === 'string') {
          try {
            const videos = JSON.parse(post.videos)
            if (Array.isArray(videos) && videos.length > 0) {
              isVideo = true
              if (videos[0].thumb) {
                mainImage = util.getImageUrl(videos[0].thumb)
              }
              // 格式化视频时长
              if (videos[0].duration) {
                videoDuration = this.formatVideoDuration(videos[0].duration)
              }
            } else if (videos && typeof videos === 'object' && videos.url) {
              isVideo = true
              if (videos.thumb) {
                mainImage = util.getImageUrl(videos.thumb)
              }
              if (videos.duration) {
                videoDuration = this.formatVideoDuration(videos.duration)
              }
            }
          } catch (e) {
            // 静默处理错误
          }
        } else if (post.videos && Array.isArray(post.videos) && post.videos.length > 0) {
          isVideo = true
          if (post.videos[0].thumb) {
            mainImage = util.getImageUrl(post.videos[0].thumb)
          }
          if (post.videos[0].duration) {
            videoDuration = this.formatVideoDuration(post.videos[0].duration)
          }
        } else if (post.videos && typeof post.videos === 'object' && post.videos.url) {
          isVideo = true
          if (post.videos.thumb) {
            mainImage = util.getImageUrl(post.videos.thumb)
          }
          if (post.videos.duration) {
            videoDuration = this.formatVideoDuration(post.videos.duration)
          }
        }
        
        // 如果没有视频封面，使用图片
        if (!mainImage) {
          const postImages = this.getPostImages(post)
          if (postImages && postImages.length > 0 && postImages[0]) {
            const img = postImages[0]
            if (!img.includes('/cat') && !img.includes('cat') && img.trim()) {
              mainImage = img
            } else {
              mainImage = util.getImageUrl('/static/images/default-product.svg')
            }
          } else if (post.coverImage) {
            const coverImg = util.getImageUrl(post.coverImage)
            if (!coverImg.includes('/cat') && !coverImg.includes('cat')) {
              mainImage = coverImg
            } else {
              mainImage = util.getImageUrl('/static/images/default-product.svg')
            }
          } else {
            mainImage = util.getImageUrl('/static/images/default-product.svg')
          }
        }
        
        // 确保图片路径不为空
        if (!mainImage || mainImage.trim() === '') {
          mainImage = util.getImageUrl('/static/images/default-product.svg')
        }
        
        // 处理用户头像
        const userAvatar = util.getImageUrl(post.userAvatar || '/static/images/garfield-default-avatar.png')
        
        return {
          img: mainImage,
          desc: post.title || post.content || '精彩内容',
          avator: userAvatar,
          uname: post.userName || '用户',
          like: post.likesCount || post.likeCount || 0,
          id: post.id,
          post: post,
          isLiked: post.isLiked || false,
          imageLoaded: false,
          heightCalculated: false, // 标记高度是否已计算
          isVideo: isVideo, // 是否为视频帖子
          videoDuration: videoDuration // 视频时长（格式：MM:SS）
        }
      })
      
      // 先将所有卡片添加到待分配队列（过滤掉无效项）
      this.pendingItems = items.filter(item => item && item.id !== undefined && item.id !== null)
      
      // 开始逐个分配（先按简单规则分配，图片加载后重新计算）
      this.distributeWaterfallItems()
      // 兜底：若 3 秒后仍有待分配项，强制分配显示（当前为交替分配，通常已全部分配完）
      this._waterfallFallbackTimer && clearTimeout(this._waterfallFallbackTimer)
      this._waterfallFallbackTimer = setTimeout(() => {
        if (this.pendingItems && this.pendingItems.length > 0) {
          this.distributeWaterfallItems()
        }
      }, 3000)
    },
    
    // 分配瀑布流卡片（左右交替分配，避免高度估算不准导致一侧空列）
    distributeWaterfallItems() {
      if (this.pendingItems.length === 0) return
      
      // 统一采用左右交替：保证两列数量均衡，避免下拉后一侧大面积空白
      while (this.pendingItems.length > 0) {
        const item = this.pendingItems.shift()
        if (!item) continue
        const totalCount = this.leftDisList.length + this.rightDisList.length
        if (totalCount % 2 === 0) {
          this.leftDisList.push(item)
        } else {
          this.rightDisList.push(item)
        }
      }
      this.$forceUpdate()
    },
    
    // 瀑布流图片加载完成回调
    onWaterfallImageLoad(e, item, column, index) {
      // 安全检查：如果item不存在，直接返回
      if (!item) {
        return
      }
      
      // 如果已经计算过高度，不再重复计算
      if (item.heightCalculated) return
      
      const applyHeightAndContinue = (cardHeight) => {
        if (!item || item.heightCalculated) return
        if (column === 'left') {
          this.leftColumnHeight += cardHeight
        } else {
          this.rightColumnHeight += cardHeight
        }
        item.heightCalculated = true
        item.imageLoaded = true
        if (this.pendingItems.length > 0) {
          this.distributeWaterfallItems()
          this.$forceUpdate()
        }
      }
      
      // 延迟一下，确保DOM已渲染完成
      this.$nextTick(() => {
        setTimeout(() => {
          const query = uni.createSelectorQuery().in(this)
          const itemId = 'waterfall-' + column + '-' + index
          
          query.select('#' + itemId).boundingClientRect((rect) => {
            if (!item) return
            // 获取到有效高度则使用，否则用默认高度继续分配，避免瀑布流卡住导致帖子不显示
            const cardHeight = (rect && rect.height) ? rect.height : 350
            applyHeightAndContinue(cardHeight)
          }).exec()
        }, 150)
      })
    },

    async loadMorePosts() {
      if (this.loading || !this.hasMore) return
      
      try {
        // 协同过滤推荐当前为 limit 返回，不支持继续翻页，直接结束
        if (this.currentCategory === 'recommend' && this.currentUserId) {
          this.hasMore = false
          return
        }

        this.loading = true
        const isRecommendCategory = !this.currentCategory || this.currentCategory === 'recommend'
        const params = {
          page: isRecommendCategory ? (this.discoverRecommendPage + 1) : (Math.floor(this.posts.length / 50) + 1),
          size: 50
        }
        if (this.currentUserId) {
          params.userId = this.currentUserId
        }
        if (this.currentCategory && this.currentCategory !== 'recommend') {
          params.tag = this.currentCategory
        }
        const response = await api.getDiscoverRecommend(params)
        if ((response.code === 200 || response.code === 0) && response.data.posts) {
          const newPosts = response.data.posts
          if (newPosts.length > 0) {
            if (isRecommendCategory) this.discoverRecommendPage = params.page
            this.normalizePostEngagementFields(newPosts)
            this.normalizePostImageUrls(newPosts)
            this.posts.push(...newPosts)
            // 将新帖子添加到待分配队列
            const newItems = newPosts.map(post => {
              let mainImage = ''
              let isVideo = false
              let videoDuration = ''
              
              // 优先检查是否有视频封面（视频帖子）
              if (post.videos && typeof post.videos === 'string') {
                try {
                  const videos = JSON.parse(post.videos)
                  if (Array.isArray(videos) && videos.length > 0) {
                    isVideo = true
                    if (videos[0].thumb) {
                      mainImage = util.getImageUrl(videos[0].thumb)
                    }
                    if (videos[0].duration) {
                      videoDuration = this.formatVideoDuration(videos[0].duration)
                    }
                  } else if (videos && typeof videos === 'object' && videos.url) {
                    isVideo = true
                    if (videos.thumb) {
                      mainImage = util.getImageUrl(videos.thumb)
                    }
                    if (videos.duration) {
                      videoDuration = this.formatVideoDuration(videos.duration)
                    }
                  }
                } catch (e) {
                  // 静默处理警告
                }
              } else if (post.videos && Array.isArray(post.videos) && post.videos.length > 0) {
                isVideo = true
                if (post.videos[0].thumb) {
                  mainImage = util.getImageUrl(post.videos[0].thumb)
                }
                if (post.videos[0].duration) {
                  videoDuration = this.formatVideoDuration(post.videos[0].duration)
                }
              } else if (post.videos && typeof post.videos === 'object' && post.videos.url) {
                isVideo = true
                if (post.videos.thumb) {
                  mainImage = util.getImageUrl(post.videos.thumb)
                }
                if (post.videos.duration) {
                  videoDuration = this.formatVideoDuration(post.videos.duration)
                }
              }
              
              // 如果没有视频封面，使用图片
              if (!mainImage) {
                const postImages = this.getPostImages(post)
                if (postImages && postImages.length > 0 && postImages[0]) {
                  const img = postImages[0]
                  if (!img.includes('/cat') && !img.includes('cat') && img.trim()) {
                    mainImage = img
                  } else {
                    mainImage = util.getImageUrl('/static/images/default-product.svg')
                  }
                } else if (post.coverImage) {
                  const coverImg = util.getImageUrl(post.coverImage)
                  if (!coverImg.includes('/cat') && !coverImg.includes('cat')) {
                    mainImage = coverImg
                  } else {
                    mainImage = util.getImageUrl('/static/images/default-product.svg')
                  }
                } else {
                  mainImage = util.getImageUrl('/static/images/default-product.svg')
                }
              }
              
              if (!mainImage || mainImage.trim() === '') {
                mainImage = util.getImageUrl('/static/images/default-product.svg')
              }
              
              const userAvatar = util.getImageUrl(post.userAvatar || '/static/images/garfield-default-avatar.png')
              
              return {
                img: mainImage,
                desc: post.title || post.content || '精彩内容',
                avator: userAvatar,
                uname: post.userName || '用户',
                like: post.likesCount || post.likeCount || 0,
                id: post.id,
                post: post,
                isLiked: post.isLiked || false,
                imageLoaded: false,
                heightCalculated: false,
                isVideo: isVideo,
                videoDuration: videoDuration
              }
            })
            
            this.pendingItems.push(...newItems)
            // 开始分配新卡片（基于当前列高度）
            this.distributeWaterfallItems()
            this.$forceUpdate()
          } else {
            this.hasMore = false
          }
        } else {
          this.hasMore = false
        }
      } catch (error) {
        console.error('加载更多帖子失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    goToSearch() {
      uni.navigateTo({
        url: '/pages-goods/search?mode=community',
        fail: (err) => {
          console.error('navigateTo search failed', err)
          uni.showToast({ title: '打开搜索失败', icon: 'none' })
        }
      })
    },

    goToPublish() {
      uni.navigateTo({
        url: '/pages-community/publish'
      })
    },

    handleFeatureClick(featureKey) {
      const routes = {
        'signin': '/pages-community/signin',
        'chat': '/chat/customer-service?isPlatform=true',
        'encyclopedia': '/pages-goods/search?mode=community',
        'mall': '/points-mall/index',
        'benefit': '',
        'feedback': '',
        'notice': '/user/notice',
        'lottery': '',
        'coupon': ''
      }
      
      if (routes[featureKey]) {
        uni.navigateTo({
          url: routes[featureKey]
        })
      } else {
        uni.showToast({
          title: '功能开发中',
          icon: 'none'
        })
      }
    },

    goToPostDetail(postIdOrPost, event) {
      let postId = null
      let post = null
      
      // 如果是对象，尝试获取id
      if (typeof postIdOrPost === 'object' && postIdOrPost !== null) {
        post = postIdOrPost
        postId = postIdOrPost.id || postIdOrPost.postId
      } else {
        postId = postIdOrPost
      }
      
      // 如果postId无效，尝试从event中获取
      if ((!postId || postId === undefined || postId === null || postId === 'undefined' || postId === 'null') && event && event.currentTarget) {
        const dataset = event.currentTarget.dataset || {}
        const eventPostId = dataset.postId || dataset.post_id
        const postIndex = dataset.postIndex !== undefined ? parseInt(dataset.postIndex) : -1
        
        if (eventPostId) {
          postId = eventPostId
        } else if (postIndex >= 0 && this.followPosts && this.followPosts[postIndex]) {
          post = this.followPosts[postIndex]
          postId = post.id || post.postId
        }
      }
      
      // 验证postId是否有效
      if (!postId || postId === undefined || postId === null || postId === 'undefined' || postId === 'null' || String(postId).trim() === '') {
        console.error('goToPostDetail: 帖子ID不存在或无效', {
          postIdOrPost,
          postId,
          type: typeof postIdOrPost,
          event
        })
        uni.showToast({
          title: '帖子ID不存在',
          icon: 'none'
        })
        return
      }
      
      const finalPostId = String(postId).trim()
      // 若没有 post 对象，从 followPosts 中查找（用于 event 传入 postId 的情况）
      if (!post && this.followPosts) {
        post = this.followPosts.find(p => String(p.id || p.postId || '').trim() === finalPostId)
      }
      // 与发现页 selectedNote 一致：根据帖子类型跳转到 post-detail-image 或 post-detail-video，避免 post-detail 黑屏
      const hasVideo = post ? this.postHasVideo(post) : false
      const likeCount = post ? (post.likesCount ?? post.likeCount ?? 0) : 0
      const isLiked = post ? !!(post.isLiked ?? post.liked) : false
      let targetUrl
      if (hasVideo && post) {
        const feed = this.followPosts || []
        let videoIds = feed.filter(pp => this.postHasVideo(pp)).map(pp => String(pp.id || pp.postId || '').trim()).filter(Boolean)
        if (!videoIds.includes(finalPostId)) {
          videoIds = [finalPostId, ...videoIds]
        }
        const ids = videoIds.length ? videoIds : [finalPostId]
        let idx = ids.indexOf(finalPostId)
        if (idx < 0) idx = 0
        const firstVideo = this.getFirstVideoUrlAndCover(post)
        if (firstVideo && firstVideo.url) {
          const app = getApp()
          if (!app.globalData) app.globalData = {}
          app.globalData.pendingVideoDetail = app.globalData.pendingVideoDetail || {}
          app.globalData.pendingVideoDetail[finalPostId] = { url: firstVideo.url, cover: firstVideo.cover || '' }
        }
        targetUrl = `/pages-community/post-detail-video?id=${finalPostId}&ids=${ids.join(',')}&index=${idx}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      } else {
        targetUrl = `/pages-community/post-detail-image?id=${finalPostId}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      }
      uni.navigateTo({ url: targetUrl })
    },
    
    goToFeed(userId) {
      // 跳转到用户主页或喂食页面
      uni.navigateTo({
        url: `/pages/main/index?userId=${userId}`
      })
    },
    
    sharePost(post, event) {
      // 如果post是undefined，尝试多种方式恢复
      if (!post) {
        // 方式1: 从event中获取
        if (event && event.currentTarget) {
          const dataset = event.currentTarget.dataset || {}
          const postId = dataset.postId || dataset.post_id
          const postIndex = dataset.postIndex !== undefined ? parseInt(dataset.postIndex) : -1
          
          if (postIndex >= 0 && this.followPosts && this.followPosts[postIndex]) {
            post = this.followPosts[postIndex]
          } else if (postId && this.followPosts) {
            // 如果只有postId，从followPosts中查找
            post = this.followPosts.find(p => (p.id == postId || p.postId == postId))
          }
        }
      }
      
      if (!post) {
        console.error('sharePost: post对象不存在', { post, event, followPosts: this.followPosts })
        uni.showToast({
          title: '帖子信息不存在',
          icon: 'none'
        })
        return
      }
      
      const postId = post.id || post.postId
      if (!postId) {
        console.error('sharePost: 帖子ID不存在', { post })
        uni.showToast({
          title: '帖子ID不存在',
          icon: 'none'
        })
        return
      }
      
      // 分享帖子
      uni.share({
        title: post.title || post.content || '分享一个有趣的帖子',
        path: `/pages-community/post-detail-image?id=${postId}`,
        success: () => {
          uni.showToast({
            title: '分享成功',
            icon: 'success'
          })
        }
      })
    },
    
    previewPostImages(post, currentIndex) {
      const images = this.getPostImages(post)
      if (images && images.length > 0) {
        const urls = images.map(img => this.getImageUrl(img))
        uni.previewImage({
          current: currentIndex || 0,
          urls: urls
        })
      }
    },

    // 处理图片URL，供模板使用
    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
    }
  }
}
</script>

<style scoped>
.community-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
  overflow: hidden;
}

/* 主导航标签（关注/发现）和发布 */
.main-nav-tabs {
  position: sticky;
  top: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 30rpx;
  height: 88rpx;
  border-bottom: 1rpx solid #f0f0f0;
  z-index: 99;
}

.main-nav-center {
  display: flex;
  align-items: center;
  gap: 60rpx;
  justify-content: center;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.publish-button {
  position: absolute;
  right: 30rpx;
}

.main-nav-tab {
  position: relative;
  padding: 12rpx 0;
}

.main-tab-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #666;
}

.main-nav-tab.active .main-tab-text {
  color: #ff6b35;
  font-weight: 600;
}

.main-tab-line {
  position: absolute;
  bottom: 6rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 30rpx;
  height: 4rpx;
  background: linear-gradient(90deg, #ff6b35, #ff8c42);
  border-radius: 2rpx;
}

.publish-button {
  padding: 6rpx 20rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #f9ca24 100%);
  border-radius: 25rpx;
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.3);
}

.publish-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 500;
}

/* 搜索框 */
.search-section {
  position: sticky;
  top: 88rpx;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 12rpx 30rpx;
  display: flex;
  align-items: center;
  z-index: 98;
  border-bottom: 1rpx solid #f0f0f0;
}

.search-box {
  flex: 1;
  height: 56rpx;
  background-color: #f5f5f5;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
}

.search-placeholder {
  font-size: 26rpx;
  color: #999;
}

/* 分类标签 */
.category-tabs {
  position: fixed;
  top: calc(env(safe-area-inset-top) + 330rpx);
  left: 0;
  right: 0;
  background-color: #fff;
  z-index: 97;
  border-bottom: 1rpx solid #f0f0f0;
}

.category-scroll {
  white-space: nowrap;
}

.category-tab {
  display: inline-block;
  padding: 20rpx 24rpx;
  margin-right: 20rpx;
}

.category-text {
  font-size: 28rpx;
  color: #666;
}

.category-tab.active .category-text {
  color: #ff6b35;
  font-weight: 600;
}

/* 广告横幅 */
.banner-section {
  margin-top: calc(env(safe-area-inset-top) + 390rpx);
  margin: calc(env(safe-area-inset-top) + 390rpx) 30rpx 30rpx;
  background: linear-gradient(135deg, #ffe5d9 0%, #ffd4c2 100%);
  border-radius: 20rpx;
  overflow: hidden;
}

.banner-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
}

.banner-text {
  flex: 1;
}

.banner-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.banner-subtitle {
  font-size: 32rpx;
  color: #999;
  font-weight: 300;
}

.banner-image {
  width: 150rpx;
  height: 150rpx;
}

.banner-action {
  text-align: center;
  padding: 20rpx;
  font-size: 24rpx;
  color: #ff6b35;
  background-color: rgba(255, 255, 255, 0.3);
}

/* 功能快捷入口 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20rpx;
  padding: 30rpx;
  background-color: #fff;
  margin-bottom: 20rpx;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.feature-icon {
  width: 80rpx;
  height: 80rpx;
  font-size: 50rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffe5d9 0%, #ffd4c2 100%);
  border-radius: 20rpx;
  margin-bottom: 10rpx;
}

.feature-label {
  font-size: 22rpx;
  color: #666;
  text-align: center;
  line-height: 1.2;
}

/* 滑动容器 */
.content-swiper {
  flex: 1;
  height: calc(100vh - 88rpx - 60rpx - 80rpx);
  /* 88rpx: 导航栏高度, 60rpx: 搜索框高度(发现显示), 80rpx: 标签栏高度(发现显示) */
}

swiper-item {
  height: 100%;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.tab-content {
  min-height: 100%;
  width: 100%;
  padding: 0 20rpx;
  box-sizing: border-box;
  padding-bottom: 40rpx;
}

.daily-tab-content {
  height: 100%;
  padding-left: 0;
  padding-right: 0;
}

/* 帖子列表 */
/* 小红书式瀑布流布局 */
/* 关注页面 - 直接复制Vue项目样式 */
.att-wrapper {
  width: 100%;
  min-height: calc(100vh - 200rpx);
  position: relative;
  overflow-y: visible;
  background: #f5f8fa;
  padding-bottom: 40rpx;
}

.attContainer {
  width: 100%;
  min-height: 400rpx;
  background: #f5f8fa;
  padding-top: 20rpx;
  padding-bottom: 40rpx;
}

.attContainer .content {
  width: 100%;
  padding: 40rpx 0;
  text-align: center;
}

.attContainer .content image {
  width: 400rpx;
  height: 300rpx;
}

/* 关注 tab 加载中 */
.follow-loading {
  width: 100%;
  min-height: 200rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}
.follow-loading-text {
  font-size: 28rpx;
  color: #999;
}

/* 关注Feed流样式 */
.follow-feed {
  width: 100%;
  padding: 16rpx 0;
}

.follow-post-item {
  background: #fff;
  margin-bottom: 16rpx;
  padding: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.follow-user-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.user-info-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.follow-post-item .user-avatar {
  width: 52rpx !important;
  height: 52rpx !important;
  border-radius: 50%;
  margin-right: 12rpx;
}

.user-info-text {
  display: flex;
  flex-direction: column;
}

.follow-post-item .user-name {
  font-size: 24rpx !important;
  font-weight: normal !important;
  color: #333;
  margin-bottom: 6rpx;
}

.user-desc {
  font-size: 22rpx;
  color: #999;
}


.follow-post-content {
  margin-bottom: 16rpx;
}

.post-text {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 16rpx;
  word-break: break-all;
}

.post-media {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  min-height: 200rpx;
}

.post-image-item {
  position: relative;
  width: calc(33.33% - 6rpx);
  aspect-ratio: 1;
  min-height: 180rpx;
  border-radius: 10rpx;
  overflow: hidden;
  background: #f0f0f0;
}

.post-image-item:only-child {
  width: 350rpx;
  max-width: 100%;
  aspect-ratio: 1;
  margin: 0;
}

.post-image-item image {
  width: 100%;
  height: 100%;
  min-width: 120rpx;
  min-height: 120rpx;
  display: block;
  background: #f0f0f0;
}

.video-play-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 60rpx;
  height: 60rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24rpx;
}

.follow-post-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 30rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
  font-size: 24rpx;
  color: #666;
}

.action-icon {
  font-size: 32rpx;
  line-height: 1;
}

.action-icon.like-icon {
  width: 32rpx;
  height: 32rpx;
  font-size: 0;
}

.action-icon.like-icon.liked {
  filter: brightness(0) saturate(100%) invert(20%) sepia(100%) saturate(5000%) hue-rotate(0deg) brightness(90%) contrast(120%);
  /* 红色滤镜效果，整个图标填满颜色 */
  -webkit-filter: brightness(0) saturate(100%) invert(20%) sepia(100%) saturate(5000%) hue-rotate(0deg) brightness(90%) contrast(120%);
}

.action-icon.comment-icon {
  width: 32rpx;
  height: 32rpx;
  font-size: 0;
}

.action-icon.share-icon {
  width: 32rpx;
  height: 32rpx;
  font-size: 0;
}

/* 空状态消息框 */
.empty-follow-message {
  width: calc(100% - 60rpx);
  margin: 60rpx auto 40rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 80rpx 40rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  display: flex !important;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 200rpx;
  position: relative;
  z-index: 1;
}

.empty-main-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  display: block;
  margin-bottom: 20rpx;
  text-align: center;
  width: 100%;
}

.empty-sub-text {
  font-size: 26rpx;
  color: #999;
  display: block;
  text-align: center;
  width: 100%;
}

/* 为你推荐标题 */
.recommend-title {
  width: 100%;
  height: 64rpx;
  line-height: 64rpx;
  font-size: 28rpx;
  color: #333;
  padding-left: 24rpx;
  background-color: #f5f8fa;
  font-weight: 600;
  margin-top: 0;
  margin-bottom: 0;
}

.attent-list {
  width: 100%;
  background: #f5f8fa;
}

.attent-item {
  padding: 20rpx 24rpx;
  display: flex;
  flex-direction: column;
  background: #fff;
  margin-bottom: 16rpx;
  border-radius: 12rpx;
}

.attent-item .left {
  margin-right: 16rpx;
}

.attent-item .left image,
.attent-item .attent-avatar {
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

/* 用户信息行（头像+名字+关注按钮） */
.attent-item > view:first-child {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 12rpx;
}

.attent-item .right {
  flex: 1;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.attent-item .right .a-title {
  flex: 1;
}

.attent-item .right .a-title .name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  display: block;
  margin-bottom: 4rpx;
}

.attent-item .right .a-title .desc {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.attent-item .right .a-btn {
  width: 140rpx;
  height: 60rpx;
}

.attent-item .right .a-btn button {
  width: 100%;
  height: 100%;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  background: #fff;
  outline: none;
  font-size: 28rpx;
  line-height: 56rpx;
}

/* 黄色关注按钮 */
.follow-btn-yellow {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%) !important;
  color: #333 !important;
  border: none !important;
  font-weight: 600 !important;
  box-shadow: 0 2rpx 8rpx rgba(255, 215, 0, 0.3);
}

/* 用户信息行 */
.user-info-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
}

/* 用户帖子缩略图网格 */
.user-posts-grid {
  display: flex;
  flex-direction: row;
  gap: 10rpx;
  margin-top: 20rpx;
  width: 100%;
}

.post-thumbnail {
  flex: 1;
  position: relative;
  border-radius: 8rpx;
  overflow: hidden;
  aspect-ratio: 1;
}

.post-thumbnail image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
  color: #fff;
  font-size: 22rpx;
  padding: 20rpx 10rpx 10rpx;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 发现页面瀑布流 - 小红书式双列布局 */
.discovery-scroll {
  width: 100%;
  height: 100%;
  background: #f5f5f5;
}

.dis-list {
  width: 100%;
  display: flex;
  flex-direction: row;
  padding: 16rpx 8rpx;
  background: #f5f5f5;
  box-sizing: border-box;
}

.left-list, .right-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 8rpx;
}

.waterfall-item {
  width: 100%;
  margin-bottom: 16rpx;
}

.note_item {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;
}

.note_item:active {
  transform: scale(0.98);
}

.note_item .img {
  width: 100%;
  overflow: hidden;
  background: #f5f5f5;
  position: relative; /* 为视频覆盖层提供定位基准 */
}

.note_item .img image {
  width: 100%;
  display: block;
  vertical-align: top;
}

/* 播放图标 - 右上角小图标 */
.note_item .img .video-play-icon-small {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 36rpx;
  height: 36rpx;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16rpx;
  font-weight: normal;
  pointer-events: none;
}

.note_item .desc {
  width: 100%;
  padding: 12rpx 16rpx 8rpx 16rpx;
  margin: 0;
  box-sizing: border-box;
}

.note_item .desc text {
  margin: 0;
  padding: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-size: 24rpx;
  color: #333;
  line-height: 1.5;
  font-weight: 500;
  word-break: break-word;
}

.note_item .note {
  height: 56rpx;
  padding: 0 16rpx 12rpx 16rpx;
  margin: 0;
  line-height: 56rpx;
  display: flex;
  color: #333;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
}

.note_item .note .user {
  display: flex;
  color: #333;
  font-size: 20rpx;
  flex-direction: row;
  align-items: center;
  flex: 1;
  overflow: hidden;
}

.note_item .note .user image {
  width: 36rpx;
  height: 36rpx;
  margin-right: 8rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.note_item .note .user text {
  font-size: 20rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note_item .note .like {
  display: flex;
  color: #999;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
  flex-shrink: 0;
}

.note_item .note .like .like-heart {
  width: 24rpx;
  height: 24rpx;
  font-size: 0;
  display: inline-block;
}

.note_item .note .like .like-heart.liked {
  filter: brightness(0) saturate(100%) invert(20%) sepia(100%) saturate(5000%) hue-rotate(0deg) brightness(90%) contrast(120%);
  /* 红色滤镜效果，整个图标填满颜色 */
  -webkit-filter: brightness(0) saturate(100%) invert(20%) sepia(100%) saturate(5000%) hue-rotate(0deg) brightness(90%) contrast(120%);
}

.note_item .note .like .like-num {
  font-size: 20rpx;
  color: #999;
  line-height: 1;
}

/* 帖子图片容器 */
.post-image-container {
  position: relative;
  width: 100%;
  height: 400rpx;
  overflow: hidden;
}

.post-main-image {
  width: 100%;
  height: 100%;
  border-radius: 16rpx 16rpx 0 0;
}

.multi-image-badge {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 6rpx 12rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.badge-icon {
  font-size: 20rpx;
}

.badge-count {
  font-size: 20rpx;
  font-weight: 500;
}

/* 帖子内容 */
.post-content {
  padding: 20rpx;
}

.post-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 4;
  overflow: hidden;
  margin-bottom: 16rpx;
}

/* 帖子底部 */
.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-section {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  margin-right: 12rpx;
  border: 2rpx solid #f0f0f0;
}

.user-name {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

.interaction-section {
  display: flex;
  align-items: center;
}

.like-button {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 12rpx;
  border-radius: 20rpx;
  background-color: #f8f8f8;
  transition: background-color 0.2s ease;
}

.like-button:active {
  background-color: #f0f0f0;
}

.like-icon {
  font-size: 28rpx;
  transition: transform 0.2s ease;
}

.like-icon.liked {
  /* 点赞后与点赞前保持相同尺寸，不放大 */
}

.like-count {
  font-size: 24rpx;
  color: #666;
  font-weight: 500;
}

/* 空状态 */
.empty-state {
  padding: 100rpx 20rpx;
  text-align: center;
  margin-top: 0;
}

.empty-message {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 60rpx 40rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.empty-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  display: block;
}

/* 推荐用户列表 */
.recommended-users-section {
  margin-bottom: 30rpx;
  margin-top: 0;
  padding: 20rpx 0 0;
}

.section-header {
  padding: 0 0 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.users-list {
  padding: 0;
}

.user-item {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.follow-btn {
  background-color: #ff6b35;
  color: #fff;
  border: none;
  border-radius: 20rpx;
  padding: 12rpx 24rpx;
  font-size: 24rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.follow-icon {
  font-size: 20rpx;
}

.follow-text {
  font-size: 24rpx;
}

.user-content-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15rpx;
}

.content-image {
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.content-image image {
  width: 100%;
  height: 120rpx;
}

.image-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: #fff;
  font-size: 20rpx;
  padding: 20rpx 10rpx 10rpx;
  text-align: center;
}

/* Banner轮播 */
.banner-section {
  margin-bottom: 30rpx;
  margin-top: 0;
  padding: 20rpx 0 0;
}

.banner-swiper {
  height: 400rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.banner-item {
  position: relative;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  color: #fff;
  padding: 40rpx 30rpx 30rpx;
}

.banner-title {
  font-size: 36rpx;
  font-weight: 700;
  margin-bottom: 10rpx;
  display: block;
}

.banner-desc {
  font-size: 26rpx;
  opacity: 0.9;
  margin-bottom: 15rpx;
  display: block;
}

.banner-meta {
  display: flex;
  gap: 30rpx;
}

.banner-chapters,
.banner-views {
  font-size: 22rpx;
  opacity: 0.8;
}

/* 文章列表 */
.articles-section {
  margin-bottom: 30rpx;
  margin-top: 0;
  padding: 20rpx 0 0;
}

.article-item {
  display: flex;
  align-items: flex-start;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease;
}

.article-item:active {
  transform: scale(0.98);
}

.article-content {
  flex: 1;
  margin-right: 20rpx;
  padding-right: 20rpx;
}

.article-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #000;
  margin-bottom: 15rpx;
  display: block;
  line-height: 1.4;
}

.article-summary {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  display: block;
  margin-bottom: 15rpx;
}

.article-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.article-date {
  font-size: 24rpx;
  color: #999;
}

.article-comments {
  font-size: 24rpx;
  color: #999;
}

.article-thumbnail {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
  background-color: #f5f5f5;
}

/* 问答列表 */
.qa-section {
  margin-bottom: 30rpx;
  margin-top: 0;
  padding: 20rpx 0 0;
}

.qa-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.qa-content {
  flex: 1;
  margin-right: 20rpx;
}

.qa-question {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
  display: block;
}

.qa-answer {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 15rpx;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.qa-author-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.qa-author {
  font-size: 22rpx;
  color: #999;
}

.qa-date {
  font-size: 22rpx;
  color: #999;
}

.qa-actions {
  flex-shrink: 0;
}

.qa-stats {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.qa-likes,
.qa-replies {
  font-size: 22rpx;
  color: #666;
  padding: 8rpx 16rpx;
  background-color: #f5f5f5;
  border-radius: 20rpx;
  min-width: 80rpx;
  text-align: center;
}

/* 每日专题样式 */
.daily-topics-content {
  padding-top: 20rpx;
}

/* 今日主题卡片 */
.theme-card {
  padding: 0 30rpx 30rpx;
}

.theme-badge {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 8rpx 20rpx rgba(102, 126, 234, 0.3);
}

.theme-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.2);
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
}

.theme-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
  flex: 1;
}

/* 每日专题 - 官方精选专题（小卡片） */
.daily-topics-content .topics-section {
  margin: 0 20rpx 24rpx;
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.daily-topics-content .topic-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 20rpx;
  border-bottom: 1rpx solid #f5f5f5;
  transition: background-color 0.2s;
}

.daily-topics-content .topic-item:last-child {
  border-bottom: none;
}

.daily-topics-content .topic-item:active {
  background-color: #f8f8f8;
}

.daily-topics-content .topic-content {
  flex: 1;
  min-width: 0;
  margin-right: 12rpx;
}

.daily-topics-content .topic-item .topic-title {
  font-size: 26rpx;
  font-weight: normal;
  color: #333;
  line-height: 1.35;
  margin-bottom: 6rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.daily-topics-content .topic-item .topic-summary {
  font-size: 22rpx;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

/* 每日专题 - 分类 Tab 栏（缩小尺寸，激活态为绿色下划线） */
.daily-category-bar {
  margin: 0 20rpx 16rpx;
  background: #fff;
  overflow: hidden;
}
.daily-category-scroll {
  white-space: nowrap;
  width: 100%;
}
.daily-category-inner {
  display: inline-flex;
  padding: 10rpx 16rpx 12rpx;
  gap: 4rpx;
  align-items: center;
}
.daily-category-item {
  flex-shrink: 0;
  padding: 6rpx 16rpx;
  position: relative;
}
.daily-category-item.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -8rpx;
  transform: translateX(-50%);
  width: 24rpx;
  height: 4rpx;
  background: #07c160;
  border-radius: 2rpx;
}
.daily-category-label {
  font-size: 24rpx;
  color: #666;
}
.daily-category-item.active .daily-category-label {
  color: #333;
  font-weight: 500;
}

.daily-topics-content .topic-thumb {
  width: 100rpx;
  height: 100rpx;
  flex-shrink: 0;
  border-radius: 10rpx;
  background-color: #f5f5f5;
}

.daily-topics-content .topics-empty {
  margin: 0 20rpx 24rpx;
  padding: 60rpx 24rpx;
  background: #fff;
  border-radius: 16rpx;
  text-align: center;
}
.daily-topics-content .topics-empty-text {
  font-size: 26rpx;
  color: #999;
}

/* 空状态 */
.daily-topics-content .empty-state {
  margin: 0 20rpx;
  text-align: center;
  padding: 120rpx 20rpx;
}

.daily-topics-content .empty-icon {
  font-size: 120rpx;
  display: block;
  margin-bottom: 30rpx;
  opacity: 0.3;
}

.daily-topics-content .empty-text {
  font-size: 32rpx;
  color: #666;
  display: block;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.daily-topics-content .empty-desc {
  font-size: 26rpx;
  color: #999;
  display: block;
}
</style>
