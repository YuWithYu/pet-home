<template>
  <view class="follows-container">
    <!-- 内容区域 -->
    <scroll-view class="content-list" scroll-y @scrolltolower="loadMore" enable-back-to-top>
      <!-- 新增关注列表 -->
      <view v-if="loading && newFollowsList.length === 0" class="loading-state">
        <text>加载中...</text>
      </view>
      <view v-else-if="newFollowsList.length === 0" class="empty-state">
        <view class="empty-icon">📦</view>
        <text class="empty-text">暂无新关注</text>
        <text class="empty-hint">还没有人关注你哦~</text>
      </view>
      <view v-else>
        <view
          class="follow-item"
          v-for="(item, index) in newFollowsList"
          :key="item.id || index"
        >
          <image
            class="user-avatar"
            :src="getAvatarUrl(item.userAvatar)"
            mode="aspectFill"
            @tap.stop="goUserProfileByIndex(index)"
          />
          <view class="follow-content" @tap.stop="goUserProfileByIndex(index)">
            <view class="follow-header">
              <text class="user-name">{{ item.userName || '用户' }}</text>
              <text class="follow-time">{{ formatTime(item.createTime) }}</text>
            </view>
            <view class="follow-action">
              <text class="action-text">开始关注你</text>
            </view>
          </view>
          <view class="follow-action-btn">
            <view
              class="follow-back-btn"
              :class="{ 'followed': item.isFollowed }"
              @tap.stop="onFollowBack(index)"
            >
              <text class="btn-text">{{ item.isFollowed ? '已关注' : '回粉' }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 推荐用户 -->
      <view v-if="recommendedUsers.length > 0" class="recommend-section">
        <view class="recommend-header">
          <text class="recommend-title">你可能感兴趣的人</text>
          <text class="close-btn" @tap="closeRecommend">关闭</text>
        </view>
        <view 
          class="recommend-item" 
          v-for="(user, index) in recommendedUsers" 
          :key="user.id || index"
        >
          <image
            class="user-avatar"
            :src="getAvatarUrl(user.avatar)"
            mode="aspectFill"
            @tap.stop="goRecommendProfile(index)"
          />
          <view class="recommend-content" @tap.stop="goRecommendProfile(index)">
            <view class="recommend-header-info">
              <text class="user-name">{{ user.nickname || user.username || '用户' }}</text>
              <text class="user-gender">{{ user.gender === 1 ? '男' : '女' }}</text>
            </view>
            <view class="recommend-stats">
              <text class="stat-text">粉丝 {{ user.fansCount || 0 }}</text>
              <text class="stat-text" v-if="user.likeCount">被点赞超过{{ user.likeCount }}次</text>
              <text class="stat-text" v-else-if="user.postCount">发布作品{{ user.postCount }}条</text>
              <text class="stat-text" v-else-if="user.fansCount">已有{{ user.fansCount }}人关注</text>
            </view>
          </view>
          <view class="follow-action-btn">
            <view 
              class="follow-btn" 
              :class="{ 'followed': user.isFollowed }"
              @tap.stop="toggleFollowUser(user, index)"
            >
              <text class="btn-text">{{ user.isFollowed ? '已关注' : '关注' }}</text>
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
  name: 'FollowsPage',
  data() {
    return {
      newFollowsList: [],
      recommendedUsers: [],
      loading: false,
      currentUserId: null,
      page: 1,
      pageSize: 20,
      hasMore: true,
      showRecommend: true
    }
  },
  
  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn'])
  },
  
  onLoad() {
    // 获取当前用户ID
    this.currentUserId =
      this.userInfo?.id ||
      this.userInfo?.uid ||
      this.userInfo?.userId ||
      uni.getStorageSync('userId') ||
      null
    
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
    
    // 加载新增关注列表
    this.loadNewFollows()
    // 加载推荐用户
    this.loadRecommendedUsers()
  },
  
  onShow() {
    // 登录态可能在 onLoad 之后才写入 Vuex，这里补一次当前用户 ID，避免后续接口用 undefined
    const uid =
      this.userInfo?.id ||
      this.userInfo?.uid ||
      this.userInfo?.userId ||
      uni.getStorageSync('userId') ||
      null
    if (uid) {
      this.currentUserId = uid
    }
    // 进入本页即视为已读，用于消息页角标返回后清零
    if (this.currentUserId) {
      uni.setStorageSync('message_lastSeenFollowsAt', Date.now())
    }
  },
  
  methods: {
    // 加载新增关注列表
    async loadNewFollows() {
      if (this.loading) return
      
      this.loading = true
      
      try {
        // 调用后端接口获取新增关注列表
        const res = await api.getNewFollows({
          userId: this.currentUserId,
          page: this.page,
          size: this.pageSize
        })
        
        if ((res.code === 0 || res.code === 200) && res.data) {
          const newFollows = res.data.follows || res.data.records || res.data || []
          
          if (this.page === 1) {
            this.newFollowsList = newFollows
          } else {
            this.newFollowsList = [...this.newFollowsList, ...newFollows]
          }
          
          this.hasMore = newFollows.length >= this.pageSize
        }
      } catch (error) {
        console.error('加载新增关注列表失败:', error)
        // 如果接口不存在，显示空状�?
      } finally {
        this.loading = false
      }
    },
    
    // 加载推荐用户
    async loadRecommendedUsers() {
      try {
        const res = await api.getRecommendedUsers({
          userId: this.currentUserId,
          limit: 10
        })
        
        if ((res.code === 0 || res.code === 200) && res.data) {
          this.recommendedUsers = res.data.users || res.data || []
        }
      } catch (error) {
        console.error('加载推荐用户失败:', error)
      }
    },
    
    // 加载更多
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadNewFollows()
    },
    
    /** 从列表项解析「对方用户 id」。注意：不要用 row.id，多为关注记录主键而非用户 id */
    resolveFollowTargetId(row) {
      if (!row || typeof row !== 'object') return null
      const id =
        row.userId ??
        row.followerId ??
        row.fromUserId ??
        row.uid
      if (id === undefined || id === null || id === '') return null
      if (typeof id === 'number' && Number.isFinite(id)) return id
      const s = String(id).trim()
      if (!s) return null
      if (/^\d+$/.test(s)) {
        return s.length > 15 ? s : Number(s)
      }
      return null
    },

    /** 微信小程序编译后 @tap 传 item 可能解析失败导致 item 为 undefined，改用 index 从列表取值 */
    goUserProfileByIndex(index) {
      const item = this.newFollowsList[index]
      if (!item) {
        uni.showToast({ title: '数据异常，请重试', icon: 'none' })
        return
      }
      const targetId = this.resolveFollowTargetId(item)
      if (targetId == null) {
        uni.showToast({ title: '无法打开主页', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/user/profile?userId=${encodeURIComponent(String(targetId))}`
      })
    },

    goRecommendProfile(index) {
      const user = this.recommendedUsers[index]
      if (!user || user.id == null || user.id === '') {
        uni.showToast({ title: '无法打开主页', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/user/profile?userId=${encodeURIComponent(String(user.id))}`
      })
    },

    onFollowBack(index) {
      const item = this.newFollowsList[index]
      if (!item) {
        uni.showToast({ title: '数据异常，请重试', icon: 'none' })
        return
      }
      this.toggleFollow(item, index)
    },

    // 切换关注状态（回粉）
    async toggleFollow(item, index) {
      if (!item) {
        uni.showToast({ title: '数据异常，请重试', icon: 'none' })
        return
      }
      const me =
        this.currentUserId ||
        this.userInfo?.id ||
        this.userInfo?.uid ||
        uni.getStorageSync('userId')
      if (!me) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      this.currentUserId = me

      const targetId = this.resolveFollowTargetId(item)
      if (targetId == null) {
        uni.showToast({
          title: '用户信息错误',
          icon: 'none'
        })
        return
      }

      try {
        let result
        if (item.isFollowed) {
          result = await api.unfollowUser(targetId, me)
        } else {
          result = await api.followUser(targetId, me)
        }

        if (result && (result.code === 200 || result.code === 0)) {
          const list = this.newFollowsList || []
          const i =
            typeof index === 'number'
              ? index
              : list.findIndex(
                  (x) =>
                    x &&
                    String(this.resolveFollowTargetId(x)) === String(targetId)
                )
          if (i >= 0 && list[i]) {
            this.$set(list, i, { ...list[i], isFollowed: !item.isFollowed })
          }
          uni.showToast({
            title: item.isFollowed ? '取消关注成功' : '回粉成功',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: (result && (result.msg || result.message)) || '操作失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('关注/取消关注失败:', error)
        uni.showToast({
          title: (error && (error.message || error.errMsg)) || '操作失败',
          icon: 'none'
        })
      }
    },
    
    // 切换推荐用户关注状�?
    async toggleFollowUser(user, index) {
      if (!this.currentUserId || !user.id) {
        uni.showToast({
          title: '用户信息错误',
          icon: 'none'
        })
        return
      }
      
      try {
        let result
        if (user.isFollowed) {
          result = await api.unfollowUser(user.id, this.currentUserId)
        } else {
          result = await api.followUser(user.id, this.currentUserId)
        }
        
        if (result && (result.code === 200 || result.code === 0)) {
          this.$set(this.recommendedUsers[index], 'isFollowed', !user.isFollowed)
          uni.showToast({
            title: user.isFollowed ? '取消关注成功' : '关注成功',
            icon: 'success'
          })
        }
      } catch (error) {
        console.error('关注/取消关注失败:', error)
        uni.showToast({
          title: '操作失败',
          icon: 'none'
        })
      }
    },
    
    // 关闭推荐
    closeRecommend() {
      this.showRecommend = false
      this.recommendedUsers = []
    },
    
    // 清空消息
    clearAllMessages() {
      uni.showModal({
        title: '提示',
        content: '确定要清空所有消息吗?',
        success: (res) => {
          if (res.confirm) {
            this.newFollowsList = []
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
    
    // 格式化时�?
    formatTime(time) {
      if (!time) return ''
      
      try {
        const date = util.parseDate ? util.parseDate(time) : new Date(time)
        if (!date || isNaN(date.getTime())) {
          return ''
        }
        
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
.follows-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content-list {
  flex: 1;
  padding-top: 20rpx;
  height: 100vh;
}

.follow-item,
.recommend-item {
  display: flex;
  align-items: center;
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

.follow-content,
.recommend-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.follow-header,
.recommend-header-info {
  display: flex;
  align-items: center;
  margin-bottom: 8rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-right: 12rpx;
}

.user-gender {
  font-size: 24rpx;
  color: #666;
}

.follow-time {
  font-size: 22rpx;
  color: #999;
  margin-left: auto;
  flex-shrink: 0;
}

.follow-action {
  margin-bottom: 0;
}

.action-text {
  font-size: 26rpx;
  color: #666;
}

.recommend-stats {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.stat-text {
  font-size: 24rpx;
  color: #999;
}

.follow-action-btn {
  margin-left: 20rpx;
  flex-shrink: 0;
}

.follow-back-btn,
.follow-btn {
  background: #ffd700;
  color: #333;
  font-size: 26rpx;
  padding: 10rpx 24rpx;
  border-radius: 22rpx;
  cursor: pointer;
  transition: all 0.3s;
  
  &.followed {
    background: #e0e0e0;
    color: #666;
  }
}

.btn-text {
  font-size: 26rpx;
}

.recommend-section {
  margin-top: 20rpx;
  background-color: #fff;
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.recommend-title {
  font-size: 28rpx;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.close-btn {
  font-size: 24rpx;
  color: #666;
  cursor: pointer;
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
