<template>
  <view class="comments-container">
    <!-- 内容区域 -->
    <scroll-view class="content-list" scroll-y @scrolltolower="loadMore" enable-back-to-top>
      <view v-if="loading && commentsList.length === 0" class="loading-state">
        <text>加载中...</text>
      </view>
      <view v-else-if="commentsList.length === 0" class="empty-state">
        <view class="empty-icon">📦</view>
        <text class="empty-text">暂无消息</text>
        <text class="empty-hint">多发帖子可以收获评论~</text>
      </view>
      <view v-else>
        <view 
          class="comment-item" 
          v-for="(item, index) in commentsList" 
          :key="item.id || index"
          @tap="goToPostDetail(item)"
        >
          <image class="user-avatar" :src="getAvatarUrl(item.userAvatar)" mode="aspectFill" />
          <view class="comment-content">
            <view class="comment-header">
              <text class="user-name">{{ item.userName || '用户' }}</text>
              <text class="comment-time">{{ formatTime(item.createTime) }}</text>
            </view>
            <view class="comment-action">
              <text class="action-text">{{ item.type === 'comment' ? '评论' : '@了你' }}</text>
            </view>
            <view class="comment-text">
              <text>{{ item.content || item.commentContent || '' }}</text>
            </view>
            <view v-if="item.postContent" class="post-preview">
              <text class="post-text">{{ item.postContent }}</text>
            </view>
            <view v-if="item.postImage" class="post-image">
              <image :src="getImageUrl(item.postImage)" mode="aspectFill" />
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'CommentsPage',
  data() {
    return {
      commentsList: [],
      loading: false,
      currentUserId: null,
      page: 1,
      pageSize: 20,
      hasMore: true
    }
  },
  
  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn'])
  },
  
  onLoad() {
    // 获取当前用户ID
    this.currentUserId = this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId') || null
    
    if (!this.currentUserId) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages-auth/login'
        })
      }, 1500)
      return
    }
    
    // 加载评论和@列表
    this.loadComments()
  },
  
  onShow() {
    // 进入本页即视为已读，用于消息页角标返回后清零
    if (this.currentUserId) {
      uni.setStorageSync('message_lastSeenCommentsAt', Date.now())
    }
  },
  
  methods: {
    // 加载评论和@列表
    async loadComments() {
      if (this.loading) return
      
      this.loading = true
      
      try {
        // 调用后端接口获取评论和@列表
        const res = await api.getCommentsAndMentions({
          userId: this.currentUserId,
          page: this.page,
          size: this.pageSize
        })
        
        if ((res.code === 0 || res.code === 200) && res.data) {
          const newComments = res.data.comments || res.data.records || res.data || []
          
          if (this.page === 1) {
            this.commentsList = newComments
          } else {
            this.commentsList = [...this.commentsList, ...newComments]
          }
          
          this.hasMore = newComments.length >= this.pageSize
        }
      } catch (error) {
        console.error('加载评论和@列表失败:', error)
        // 如果接口不存在，显示空状�?
      } finally {
        this.loading = false
      }
    },
    
    // 加载更多
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadComments()
    },
    
    // 跳转到帖子详�?
    goToPostDetail(item) {
      const postId = item.postId || item.post_id || item.id
      if (postId) {
        const hasVideo = item.videos || (item.post && item.post.videos)
        if (hasVideo) {
          uni.navigateTo({
            url: `/pages-community/post-detail-video?id=${postId}`
          })
        } else {
          uni.navigateTo({
            url: `/pages-community/post-detail-image?id=${postId}`
          })
        }
      }
    },
    
    // 清空消息
    clearAllMessages() {
      uni.showModal({
        title: '提示',
        content: '确定要清空所有消息吗?',
        success: (res) => {
          if (res.confirm) {
            this.commentsList = []
            uni.showToast({
              title: '已清空',
              icon: 'success'
            })
          }
        }
      })
    },
    
    // 获取头像URL
    getAvatarUrl(avatar) {
      if (!avatar) {
        return '/static/images/login-dog.png'
      }
      return util.getImageUrl ? util.getImageUrl(avatar) : avatar
    },
    
    // 获取图片URL
    getImageUrl(url) {
      if (!url) return ''
      return util.getImageUrl ? util.getImageUrl(url) : url
    },
    
    // 格式化时�?
    formatTime(time) {
      if (!time) return ''
      
      try {
        const date = util.parseDate ? util.parseDate(time) : new Date(time)
        if (!date || isNaN(date.getTime())) {
          return ''
        }
        
        const now = new Date()
        const diff = now.getTime() - date.getTime()
        const minutes = Math.floor(diff / (1000 * 60))
        const hours = Math.floor(diff / (1000 * 60 * 60))
        const days = Math.floor(diff / (1000 * 60 * 60 * 24))
        
        if (minutes < 1) return '刚刚'
        if (minutes < 60) return `${minutes}分钟前`
        if (hours < 24) return `${hours}小时前`
        if (days < 7) return `${days}天前`
        
        const month = String(date.getMonth() + 1).padStart(2, '0')
        const day = String(date.getDate()).padStart(2, '0')
        return `${month}-${day}`
      } catch (e) {
        console.error('格式化时间失败:', e)
        return ''
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.comments-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content-list {
  flex: 1;
  padding-top: 20rpx;
  height: 100vh;
}

.comment-item {
  display: flex;
  align-items: flex-start;
  padding: 24rpx 30rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:active {
    background-color: #f5f5f5;
  }
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #e0e0e0;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.comment-action {
  margin-bottom: 8rpx;
}

.action-text {
  font-size: 26rpx;
  color: #666;
}

.comment-text {
  margin-bottom: 12rpx;
  
  text {
    font-size: 26rpx;
    color: #333;
    line-height: 1.5;
  }
}

.post-preview {
  margin-bottom: 12rpx;
}

.post-text {
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 8rpx;
  overflow: hidden;
  background-color: #f0f0f0;
  
  image {
    width: 100%;
    height: 100%;
  }
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
  opacity: 0.3;
}

.empty-text {
  font-size: 32rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #ccc;
}
</style>
