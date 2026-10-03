<template>
  <view class="follow-page">
    <!-- 标签切换导航 -->
    <view class="tab-nav">
      <view 
        v-for="(tab, index) in tabs" 
        :key="index"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        <text class="tab-text">{{ tab.name }}</text>
      </view>
    </view>

    <!-- 滑动容器 -->
    <swiper 
      class="content-swiper" 
      :current="currentTab" 
      @change="onSwiperChange"
      :duration="300"
    >
      <!-- 关注内容 -->
      <swiper-item>
        <scroll-view scroll-y class="tab-content">
          <!-- 空状态提示 -->
          <view class="empty-state" v-if="followedUsers.length === 0 && !loading">
            <view class="empty-message">
              <text class="empty-title">您关注的用户还没有动态呢~</text>
              <text class="empty-desc">别那么高冷啦,快去关注更多小伙伴吧~</text>
            </view>
          </view>

          <!-- 推荐用户列表 -->
          <view class="recommended-users-section" v-if="recommendedUsers.length > 0">
            <view class="section-header">
              <text class="section-title">为你推荐</text>
            </view>
            
            <view class="users-list">
              <view 
                v-for="(user, index) in recommendedUsers" 
                :key="index" 
                class="user-item"
              >
                <view class="user-header">
                  <image class="user-avatar" :src="user.avatar" mode="aspectFill" />
                  <text class="user-name">{{ user.name }}</text>
                  <button class="follow-btn" @click="followUser(user)">
                    <text class="follow-icon">+</text>
                    <text class="follow-text">关注</text>
                  </button>
                </view>
                
                <view class="user-content-grid">
                  <view 
                    v-for="(image, imgIndex) in user.images" 
                    :key="imgIndex" 
                    class="content-image"
                    @click="goToUserContent(user, imgIndex)"
                  >
                    <image :src="image.url" mode="aspectFill" />
                    <text class="image-caption">{{ image.caption }}</text>
                  </view>
                </view>
              </view>
            </view>
          </view>

          <!-- 瀑布流帖子展示 -->
          <view class="posts-section" v-if="posts.length > 0">
            <view class="waterfall-container">
              <view class="waterfall-column" v-for="(column, columnIndex) in waterfallColumns" :key="columnIndex">
                <view 
                  v-for="(post, postIndex) in column" 
                  :key="post.id" 
                  class="post-card"
                  @click="goToPostDetail(post)"
                >
                  <view class="post-image-container">
                    <image 
                      class="post-main-image" 
                      :src="post.thumbnail" 
                      mode="aspectFill"
                      @error="handleImageError"
                    />
                    <view v-if="post.isVideo" class="play-button">
                      <text class="play-icon">▶</text>
                    </view>
                  </view>
                  
                  <view class="post-content">
                    <text class="post-title">{{ post.title }}</text>
                    <view class="post-meta">
                      <text class="channel-name">{{ post.channelName }}</text>
                      <text class="post-date">{{ post.date }}</text>
                      <view class="like-section">
                        <image 
                          class="like-icon" 
                          :src="post.isLiked ? '/static/images/点赞后.png' : '/static/images/点赞前.png'"
                          mode="aspectFit"
                        ></image>
                        <text class="like-count">{{ post.likesCount }}</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </swiper-item>

      <!-- 发现内容 -->
      <swiper-item>
        <scroll-view scroll-y class="tab-content">
          <!-- 瀑布流帖子展示 -->
          <view class="posts-section" v-if="discoverPosts.length > 0">
            <view class="waterfall-container">
              <view class="waterfall-column" v-for="(column, columnIndex) in discoverWaterfallColumns" :key="columnIndex">
                <view 
                  v-for="(post, postIndex) in column" 
                  :key="post.id" 
                  class="post-card"
                  @click="goToPostDetail(post)"
                >
                  <view class="post-image-container">
                    <image 
                      class="post-main-image" 
                      :src="post.thumbnail" 
                      mode="aspectFill"
                      @error="handleImageError"
                    />
                    <view v-if="post.isVideo" class="play-button">
                      <text class="play-icon">▶</text>
                    </view>
                  </view>
                  
                  <view class="post-content">
                    <text class="post-title">{{ post.title }}</text>
                    <view class="post-meta">
                      <text class="channel-name">{{ post.channelName }}</text>
                      <text class="post-date">{{ post.date }}</text>
                      <view class="like-section">
                        <image 
                          class="like-icon" 
                          :src="post.isLiked ? '/static/images/点赞后.png' : '/static/images/点赞前.png'"
                          mode="aspectFit"
                        ></image>
                        <text class="like-count">{{ post.likesCount }}</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
          
          <!-- 空状态 -->
          <view v-if="discoverPosts.length === 0 && !loading" class="empty-discover">
            <text class="empty-text">暂无发现内容</text>
          </view>
        </scroll-view>
      </swiper-item>

      <!-- 每日专题内容 -->
      <swiper-item>
        <scroll-view scroll-y class="tab-content">
          <!-- Banner轮播 -->
          <view class="banner-section" v-if="banners.length > 0">
            <swiper class="banner-swiper" indicator-dots="true" autoplay="true" interval="3000" duration="500">
              <swiper-item v-for="(banner, index) in banners" :key="index">
                <view class="banner-item" @click="goToBannerDetail(banner)">
                  <image class="banner-image" :src="banner.image" mode="aspectFill" />
                  <view class="banner-content">
                    <text class="banner-title">{{ banner.title }}</text>
                    <text class="banner-desc">{{ banner.desc }}</text>
                    <view class="banner-meta">
                      <text class="banner-chapters">{{ banner.chapters }}章</text>
                      <text class="banner-views">{{ banner.views }}次观看</text>
                    </view>
                  </view>
                </view>
              </swiper-item>
            </swiper>
          </view>

          <!-- 文章列表 -->
          <view class="articles-section" v-if="articles.length > 0">
            <view 
              v-for="(article, index) in articles" 
              :key="index" 
              class="article-item"
              @click="goToArticleDetail(article)"
            >
              <view class="article-content">
                <text class="article-title">{{ article.title }}</text>
                <text class="article-summary">{{ article.summary }}</text>
              </view>
              <image class="article-thumbnail" :src="article.thumbnail" mode="aspectFill" />
            </view>
          </view>

          <!-- 问答列表 -->
          <view class="qa-section" v-if="qaList.length > 0">
            <view 
              v-for="(qa, index) in qaList" 
              :key="index" 
              class="qa-item"
              @click="goToQADetail(qa)"
            >
              <view class="qa-content">
                <text class="qa-question">{{ qa.question }}</text>
                <text class="qa-answer">{{ qa.answer }}</text>
                <view class="qa-author-info">
                  <text class="qa-author">{{ qa.author }}</text>
                  <text class="qa-date">{{ qa.date }}</text>
                </view>
              </view>
              <view class="qa-actions">
                <view class="qa-stats">
                  <text class="qa-likes">{{ qa.likes }}</text>
                  <text class="qa-replies">{{ qa.replies }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  data() {
    return {
      activeTab: 'follow', // 当前激活的标签
      currentTab: 0, // 当前swiper的索引
      tabs: [
        { name: '关注', key: 'follow' },
        { name: '发现', key: 'discover' },
        { name: '宠物专题', key: 'daily' }
      ],
      posts: [],
      discoverPosts: [],
      discoverWaterfallColumns: [[], []],
      followedUsers: [],
      recommendedUsers: [],
      waterfallColumns: [[], []],
      banners: [],
      articles: [],
      qaList: [],
      loading: false,
      hasMore: true,
      page: 1,
      currentUserId: 18
    }
  },

  onLoad() {
    this.loadFollowedUsers()
    this.loadRecommendedUsers()
    this.loadPosts()
  },

  onPullDownRefresh() {
    this.page = 1
    this.hasMore = true
    this.loadPosts().finally(() => {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    switchTab(index) {
      this.currentTab = index
      this.activeTab = this.tabs[index].key
      // 根据标签加载不同的数据
      if (index === 0) {
        this.loadFollowedUsers()
        this.loadRecommendedUsers()
        this.loadPosts()
      } else if (index === 1) {
        // 加载发现页面的数据
        this.loadDiscoverPosts()
      } else if (index === 2) {
        // 加载每日专题的数据
        this.loadDailyTopics()
      }
    },

    onSwiperChange(e) {
      const index = e.detail.current
      this.currentTab = index
      this.activeTab = this.tabs[index].key
      // 根据标签加载不同的数据
      if (index === 0) {
        this.loadFollowedUsers()
        this.loadRecommendedUsers()
        this.loadPosts()
      } else if (index === 1) {
        // 加载发现页面的数据
        this.loadDiscoverPosts()
      } else if (index === 2) {
        // 加载每日专题的数据
        this.loadDailyTopics()
      }
    },

    async loadFollowedUsers() {
      try {
        const response = await api.getFollowedUsers(this.currentUserId)
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

    async loadRecommendedUsers() {
      if (!this.currentUserId) return
      try {
        const response = await api.getRecommendedUsers({ userId: this.currentUserId })
        if (response && (response.code === 200 || response.code === 0)) {
          const list = response.data || []
          if (Array.isArray(list) && list.length > 0) {
            this.recommendedUsers = list
          } else {
            this.recommendedUsers = [
              { id: 19, name: 'test_user', avatar: '/static/images/garfield-default-avatar.png' },
              { id: 20, name: 'pet_lover_001', avatar: '/static/images/garfield-default-avatar.png' },
              { id: 21, name: 'cat_guardian', avatar: '/static/images/garfield-default-avatar.png' },
              { id: 22, name: 'cat_guardian', avatar: '/static/images/garfield-default-avatar.png' },
              { id: 23, name: 'dog_manager', avatar: '/static/images/garfield-default-avatar.png' },
              { id: 24, name: 'pet_doctor', avatar: '/static/images/garfield-default-avatar.png' },
              { id: 25, name: 'pet_diary', avatar: '/static/images/garfield-default-avatar.png' }
            ]
          }
        } else {
          this.recommendedUsers = [
            { id: 19, name: 'test_user', avatar: '/static/images/garfield-default-avatar.png' },
            { id: 20, name: 'pet_lover_001', avatar: '/static/images/garfield-default-avatar.png' },
            { id: 21, name: 'cat_guardian', avatar: '/static/images/garfield-default-avatar.png' },
            { id: 22, name: 'cat_guardian', avatar: '/static/images/garfield-default-avatar.png' },
            { id: 23, name: 'dog_manager', avatar: '/static/images/garfield-default-avatar.png' },
            { id: 24, name: 'pet_doctor', avatar: '/static/images/garfield-default-avatar.png' },
            { id: 25, name: 'pet_diary', avatar: '/static/images/garfield-default-avatar.png' }
          ]
        }
      } catch (error) {
        console.error('加载推荐用户失败:', error)
        this.recommendedUsers = [
          { id: 19, name: 'test_user', avatar: '/static/images/garfield-default-avatar.png' },
          { id: 20, name: 'pet_lover_001', avatar: '/static/images/garfield-default-avatar.png' },
          { id: 21, name: 'cat_guardian', avatar: '/static/images/garfield-default-avatar.png' },
          { id: 22, name: 'cat_guardian', avatar: '/static/images/garfield-default-avatar.png' },
          { id: 23, name: 'dog_manager', avatar: '/static/images/garfield-default-avatar.png' },
          { id: 24, name: 'pet_doctor', avatar: '/static/images/garfield-default-avatar.png' },
          { id: 25, name: 'pet_diary', avatar: '/static/images/garfield-default-avatar.png' }
        ]
      }
    },

    async loadPosts() {
      if (this.loading) return
      
      try {
        this.loading = true
        
        const params = {
          page: this.page,
          size: 10
        }
        
        const response = await api.getFollowingPosts(this.currentUserId, params)
        if (response && (response.code === 200 || response.code === 0)) {
          const newPosts = response.data.posts || []
          
          if (this.page === 1) {
            this.posts = newPosts
          } else {
            this.posts.push(...newPosts)
          }
          
          // 重新排列瀑布流
          this.arrangeWaterfall()
          
          // 检查是否还有更多数据
          if (newPosts.length < params.size) {
            this.hasMore = false
          }
          
          this.page++
        } else {
          // 如果API失败，使用示例数据
          this.posts = [
            {
              id: 1,
              title: '二课系统培训 | 如何查看修改我的个人信息',
              thumbnail: '/static/images/training1.jpg',
              channelName: 'erk 中青二课',
              date: '09-03',
              likesCount: 363,
              isVideo: false
            },
            {
              id: 2,
              title: '50秒学会室内消火栓到底咋用',
              thumbnail: '/static/images/fire1.jpg',
              channelName: 'erk 中青二课',
              date: '09-05',
              likesCount: 75,
              isVideo: true
            },
            {
              id: 3,
              title: '"隐形杀手"!这些校内区域最容易着火!',
              thumbnail: '/static/images/fire2.jpg',
              channelName: 'erk 中青二课',
              date: '09-05',
              likesCount: 115,
              isVideo: true
            },
            {
              id: 4,
              title: '校园消防安全指南',
              thumbnail: '/static/images/fire3.jpg',
              channelName: 'erk 中青二课',
              date: '09-05',
              likesCount: 94,
              isVideo: true
            }
          ]
          this.arrangeWaterfall()
        }
        
      } catch (error) {
        console.error('加载帖子失败:', error)
        this.posts = []
      } finally {
        this.loading = false
      }
    },

    async loadDiscoverPosts() {
      try {
        const response = await api.getPostList({
          page: 1,
          size: 20,
          category: '推荐'
        })
        if (response && (response.code === 200 || response.code === 0)) {
          this.discoverPosts = response.data.posts || []
          this.arrangeDiscoverWaterfall()
        } else {
          // 使用示例数据
          this.discoverPosts = [
            {
              id: 1,
              title: '可爱的小猫咪',
              thumbnail: '', // 不再使用硬编码图片，应从数据库加载
              channelName: '宠物分享',
              date: '10-28',
              likesCount: 123,
              isVideo: false
            },
            {
              id: 2,
              title: '狗狗的日常',
              thumbnail: '/static/images/dog1.jpg',
              channelName: '宠物分享',
              date: '10-28',
              likesCount: 89,
              isVideo: true
            }
          ]
          this.arrangeDiscoverWaterfall()
        }
      } catch (error) {
        console.error('加载发现帖子失败:', error)
        this.discoverPosts = []
      }
    },

    async loadDailyTopics() {
      try {
        // 加载Banner
        this.loadBanners()
        // 加载文章
        this.loadArticles()
        // 加载问答
        this.loadQAList()
      } catch (error) {
        console.error('加载每日专题失败:', error)
      }
    },

    async loadBanners() {
      try {
        const response = await api.getDailyTopics()
        if (response && (response.code === 200 || response.code === 0)) {
          this.banners = response.data.banners || []
        } else {
          // 使用示例数据
          this.banners = [
            {
              id: 1,
              title: '宠物营养全解析',
              desc: '了解宠物营养需求，科学喂养',
              image: '/static/images/banner1.jpg',
              chapters: 12,
              views: 15680
            },
            {
              id: 2,
              title: '宠物行为训练',
              desc: '培养宠物良好行为习惯',
              image: '/static/images/banner2.jpg',
              chapters: 8,
              views: 12340
            }
          ]
        }
      } catch (error) {
        console.error('加载Banner失败:', error)
        this.banners = []
      }
    },

    async loadArticles() {
      try {
        const response = await api.getPostList({
          page: 1,
          size: 10,
          category: '推荐'
        })
        if (response && (response.code === 200 || response.code === 0)) {
          this.articles = response.data.posts.slice(0, 5).map(post => ({
            id: post.id,
            title: post.title || '无标题',
            summary: post.content ? (post.content.substring(0, 120) + '...') : '暂无描述',
            thumbnail: this.getPostImage(post)
          }))
        } else {
          // 使用示例数据
          this.articles = [
            {
              id: 1,
              title: '主粮怎么挑?宠粮营养全解读',
              summary: '犬猫对蛋白质的需求很高,尤其是猫咪——————猫的日常热量-半以上来自于蛋白质,这个比例高于狗狗和人类。犬猫都有10种以上的必需氨基酸,要确保毛孩子每顿饭都能吃到这些重要的蛋白质哦!',
              thumbnail: '/static/images/cat-food-article.jpg'
            },
            {
              id: 2,
              title: '踹好手手告诉你圆滚滚小猫咪的秘密',
              summary: '经常有人问电波都一岁多了好像和小时候也没有太大变化,还是那么萌呀!呐!电波可是很有自己的一套的,总结起来就是好好吃肉,多多跑酷,无忧无虑哈哈',
              thumbnail: '/static/images/cute-cat.jpg'
            }
          ]
        }
      } catch (error) {
        console.error('加载文章列表失败:', error)
        this.articles = []
      }
    },

    async loadQAList() {
      try {
        const response = await api.getHotPosts(10)
        if (response && (response.code === 200 || response.code === 0)) {
          this.qaList = response.data.slice(0, 5).map(post => ({
            id: post.id,
            question: post.title || '无标题',
            answer: post.content ? (post.content.substring(0, 80) + '...') : '暂无内容',
            author: post.userName || '用户' + post.userId,
            date: this.formatDate(post.createTime),
            likes: post.likesCount || 0,
            replies: post.commentsCount || 0
          }))
        } else {
          // 使用示例数据
          this.qaList = [
            {
              id: 1,
              question: '猫咪为什么总是掉毛？',
              answer: '猫咪掉毛是正常现象，但过多掉毛可能是营养不均衡或皮肤问题...',
              author: '宠物医生',
              date: '10-28',
              likes: 45,
              replies: 12
            },
            {
              id: 2,
              question: '狗狗需要多久洗一次澡？',
              answer: '一般来说，狗狗每月洗1-2次澡比较合适，过于频繁会破坏皮肤...',
              author: '宠物护理师',
              date: '10-27',
              likes: 38,
              replies: 8
            }
          ]
        }
      } catch (error) {
        console.error('加载问答列表失败:', error)
        this.qaList = []
      }
    },

    arrangeDiscoverWaterfall() {
      this.discoverWaterfallColumns = [[], []]
      this.discoverPosts.forEach((post, index) => {
        const columnIndex = index % 2
        this.discoverWaterfallColumns[columnIndex].push(post)
      })
    },

    async followUser(user) {
      try {
        const followingId = user && typeof user.id !== 'undefined' ? user.id : null
        const followerId = typeof this.currentUserId !== 'undefined' ? this.currentUserId : null
        if (followingId === null) {
          uni.showToast({ title: '无效的用户，无法关注', icon: 'none' })
          return
        }
        if (followerId === null) {
          uni.showToast({ title: '未登录，无法关注', icon: 'none' })
          return
        }
        const result = await api.followUser(followingId, followerId)
        if (result && (result.code === 200 || result.code === 0)) {
          uni.showToast({
            title: '关注成功',
            icon: 'success'
          })
          // 从推荐列表中移除
          this.recommendedUsers = this.recommendedUsers.filter(u => u.id !== followingId)
        }
      } catch (error) {
        console.error('关注用户失败:', error)
        uni.showToast({
          title: '关注失败',
          icon: 'none'
        })
      }
    },

    goToUserContent(user, imageIndex) {
      // 可以跳转到用户详情或内容详情
    },

    goToPostDetail(post) {
      const hasVideo = post.videos || (post.post && post.post.videos)
      if (hasVideo) {
        uni.navigateTo({
          url: `/pages-community/post-detail-video?id=${post.id}`
        })
      } else {
        uni.navigateTo({
          url: `/pages-community/post-detail-image?id=${post.id}`
        })
      }
    },

    handleImageError(e) {
    },

    goToArticleDetail(article) {
      uni.navigateTo({
        url: `/pages-community/topic-detail?id=${article.id}`
      })
    },

    goToQADetail(qa) {
      uni.navigateTo({
        url: `/pages-community/topic-detail?id=${qa.id}`
      })
    },

    goToBannerDetail(banner) {
      uni.navigateTo({
        url: `/pages-community/topic-detail?id=${banner.id}`
      })
    },

    async loadDiscoverPosts() {
      try {
        // 加载发现页面的帖子数据
        const response = await api.getPostList({
          page: 1,
          size: 20,
          category: '推荐'
        })
        if (response && (response.code === 200 || response.code === 0)) {
          this.posts = response.data.posts || []
          this.arrangeWaterfall()
        }
      } catch (error) {
        console.error('加载发现页面数据失败:', error)
      }
    },

    async loadDailyTopics() {
      try {
        // 加载每日专题的数据
        const response = await api.getDailyTopics()
        if (response && (response.code === 200 || response.code === 0)) {
          // 处理每日专题数据
          this.banners = response.data.banners || []
          this.articles = response.data.articles || []
          this.qaList = response.data.qaList || []
        } else {
          // 如果API失败，使用示例数据
          this.banners = [
            {
              id: 1,
              image: '/static/images/banner1.jpg',
              title: '宠物健康指南',
              desc: '专业宠物护理知识分享',
              chapters: 12,
              views: 1250
            }
          ]
          this.articles = [
            {
              id: 1,
              title: '主粮怎么挑?宠粮营养全解读',
              summary: '犬猫对蛋白质的需求很高,尤其是猫咪——————猫的日常热量-半以上来自于蛋白质,这个比例高于狗狗和人类。',
              thumbnail: '/static/images/cat-food-article.jpg'
            },
            {
              id: 2,
              title: '踹好手手告诉你圆滚滚小猫咪的秘密',
              summary: '经常有人问电波都一岁多了好像和小时候也没有太大变化,还是那么萌呀!',
              thumbnail: '/static/images/cute-cat.jpg'
            }
          ]
          this.qaList = [
            {
              id: 1,
              question: '猫咪为什么总是半夜叫？',
              answer: '猫咪是夜行动物，半夜叫可能是饿了或者想要玩耍...',
              author: '宠物医生',
              date: '2024-01-15',
              likes: 25,
              replies: 8
            }
          ]
        }
      } catch (error) {
        console.error('加载每日专题数据失败:', error)
        // 使用示例数据
        this.banners = []
        this.articles = []
        this.qaList = []
      }
    }
  }
}
</script>

<style scoped>
.follow-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 标签导航 */
.tab-nav {
  display: flex;
  background-color: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  padding: 0 30rpx;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30rpx 0;
  position: relative;
}

.tab-item.active {
  color: #ff6b35;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 4rpx;
  background-color: #ff6b35;
  border-radius: 2rpx;
}

.tab-text {
  font-size: 28rpx;
  font-weight: 500;
  color: #666;
}

.tab-item.active .tab-text {
  color: #ff6b35;
  font-weight: 600;
}

/* 标签内容 */
.tab-content {
  flex: 1;
}

/* 滑动容器 */
.content-swiper {
  height: calc(100vh - 200rpx);
}

.tab-content {
  height: 100%;
  padding: 20rpx;
}

/* 推荐用户列表 */
.recommended-users-section {
  margin-top: 20rpx;
  background-color: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.users-list {
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}

.user-item {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.user-name {
  flex: 1;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.follow-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background-color: #ff6b35;
  border-radius: 20rpx;
  border: none;
}

.follow-icon {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
}

.follow-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
}

.user-content-grid {
  display: flex;
  gap: 15rpx;
}

.content-image {
  flex: 1;
  position: relative;
}

.content-image image {
  width: 100%;
  height: 200rpx;
  border-radius: 12rpx;
  object-fit: cover;
}

.image-caption {
  position: absolute;
  bottom: 10rpx;
  left: 10rpx;
  right: 10rpx;
  font-size: 22rpx;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.6);
  padding: 8rpx 12rpx;
  border-radius: 8rpx;
  text-align: center;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 50rpx;
  text-align: center;
  margin-top: 20rpx;
}

.empty-message {
  background-color: #fff;
  padding: 40rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  border: 2rpx solid #ff6b35;
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

/* 瀑布流帖子展示 */
.posts-section {
  padding: 0 15rpx;
}

.waterfall-container {
  display: flex;
  gap: 15rpx;
}

.waterfall-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.post-card {
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.post-image-container {
  position: relative;
  width: 100%;
  height: 300rpx;
}

.post-main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80rpx;
  height: 80rpx;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon {
  font-size: 32rpx;
  color: #fff;
  margin-left: 4rpx;
}

.post-content {
  padding: 20rpx;
}

.post-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 15rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10rpx;
}

.channel-name {
  font-size: 22rpx;
  color: #666;
  flex: 1;
}

.post-date {
  font-size: 22rpx;
  color: #999;
}

.like-section {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.like-icon {
  width: 20rpx;
  height: 20rpx;
  font-size: 0;
}

/* 每日专题样式 */
.banner-section {
  margin-bottom: 30rpx;
}

.banner-swiper {
  height: 400rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.banner-item {
  position: relative;
  width: 100%;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  padding: 40rpx 30rpx 30rpx;
  color: #fff;
}

.banner-title {
  font-size: 32rpx;
  font-weight: 600;
  margin-bottom: 10rpx;
  display: block;
}

.banner-desc {
  font-size: 24rpx;
  opacity: 0.9;
  margin-bottom: 15rpx;
  display: block;
}

.banner-meta {
  display: flex;
  gap: 20rpx;
}

.banner-chapters,
.banner-views {
  font-size: 22rpx;
  opacity: 0.8;
}

/* 文章列表样式 */
.articles-section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.article-item {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.article-item:last-child {
  border-bottom: none;
}

.article-content {
  flex: 1;
  margin-right: 20rpx;
}

.article-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #000;
  line-height: 1.4;
  margin-bottom: 10rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.article-summary {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.article-thumbnail {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  object-fit: cover;
  flex-shrink: 0;
}

/* 问答列表样式 */
.qa-section {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
}

.qa-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.qa-item:last-child {
  border-bottom: none;
}

.qa-content {
  flex: 1;
  margin-right: 20rpx;
}

.qa-question {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 10rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.qa-answer {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 15rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
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

/* 发现页面空状态 */
.empty-discover {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.follow-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
}

.user-content-grid {
  display: flex;
  gap: 15rpx;
}

.content-image {
  flex: 1;
  position: relative;
}

.content-image image {
  width: 100%;
  height: 200rpx;
  border-radius: 12rpx;
  object-fit: cover;
}

.image-caption {
  position: absolute;
  bottom: 10rpx;
  left: 10rpx;
  right: 10rpx;
  font-size: 22rpx;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.6);
  padding: 8rpx 12rpx;
  border-radius: 8rpx;
  text-align: center;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 50rpx;
  text-align: center;
  margin-top: 20rpx;
}

.empty-message {
  background-color: #fff;
  padding: 40rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  border: 2rpx solid #ff6b35;
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

/* 瀑布流帖子展示 */
.posts-section {
  padding: 0 15rpx;
}

.waterfall-container {
  display: flex;
  gap: 15rpx;
}

.waterfall-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.post-card {
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.post-image-container {
  position: relative;
  width: 100%;
  height: 300rpx;
}

.post-main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.play-button {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80rpx;
  height: 80rpx;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.play-icon {
  font-size: 32rpx;
  color: #fff;
  margin-left: 4rpx;
}

.post-content {
  padding: 20rpx;
}

.post-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin-bottom: 15rpx;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10rpx;
}

.channel-name {
  font-size: 22rpx;
  color: #666;
  flex: 1;
}

.post-date {
  font-size: 22rpx;
  color: #999;
}

.like-section {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.like-icon {
  width: 20rpx;
  height: 20rpx;
  font-size: 0;
}

.like-count {
  font-size: 22rpx;
  color: #999;
}

/* 发现和每日专题内容 */
.discover-content,
.daily-content {
  padding: 40rpx;
  text-align: center;
}

.discover-text,
.daily-text {
  font-size: 32rpx;
  color: #666;
}
</style>