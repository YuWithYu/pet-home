<template>
  <view class="notifications-container">
    <view class="toolbar">
      <text v-if="unreadCount > 0" class="mark-all" @tap="markAllRead">全部标为已读</text>
    </view>
    <scroll-view class="list" scroll-y @scrolltolower="loadMore" enable-back-to-top>
      <view v-if="loading && mergedList.length === 0" class="loading-state">
        <text>加载中...</text>
      </view>
      <view v-else-if="mergedList.length === 0" class="empty-state">
        <view class="empty-icon">📬</view>
        <text class="empty-text">暂无通知</text>
        <text class="empty-hint">预约提醒、订单状态、系统公告与互动消息会显示在这里</text>
      </view>
      <view v-else>
        <view
          v-for="(item, index) in mergedList"
          :key="item.uniqKey"
          class="item"
          :class="{ unread: item.status === 0 || item.status === 'unread' }"
          @tap="onItemTap(item)"
        >
          <view class="dot">
            <view :class="['type-dot', typeClass(item.type)]"></view>
          </view>
          <view class="body">
            <view class="line1">
              <text class="title">{{ item.title }}</text>
              <text class="time">{{ formatTime(item.createTime || item.create_time) }}</text>
            </view>
            <text class="desc">{{ item.content || item.desc }}</text>
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
  name: 'NotificationsPage',
  data() {
    return {
      list: [],
      noticesUnread: [],
      loading: false,
      currentUserId: null,
      pageNo: 1,
      pageSize: 20,
      hasMore: true,
      unreadCount: 0
    }
  },
  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn']),
    mergedList() {
      const items = []
      this.noticesUnread.forEach((n, i) => {
        items.push({
          uniqKey: 'notice_' + n.id,
          type: 'system_announcement',
          title: n.title || '系统公告',
          content: n.content,
          desc: n.content,
          createTime: n.createTime || n.create_time,
            status: 0,
          isNotice: true,
          noticeId: n.id
        })
      })
      this.list.forEach((n, i) => {
        // 系统通知中心不展示社区互动，避免和消息页三个分类重复
        if (n && (n.type === 'community_like' || n.type === 'community_comment' || n.type === 'community_follow')) return
        items.push({
          ...n,
          uniqKey: 'n_' + n.id,
          isNotice: false
        })
      })
      items.sort((a, b) => {
        const ta = util.parseDate ? util.parseDate(a.createTime) : new Date(a.createTime)
        const tb = util.parseDate ? util.parseDate(b.createTime) : new Date(b.createTime)
        return (tb && tb.getTime ? tb.getTime() : 0) - (ta && ta.getTime ? ta.getTime() : 0)
      })
      return items
    }
  },
  onLoad() {
    this.currentUserId = this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId') || null
    if (!this.currentUserId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      setTimeout(() => uni.navigateTo({ url: '/pages-auth/login' }), 1500)
      return
    }
    this.loadData()
  },
  onPullDownRefresh() {
    this.loadData(true).finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    typeClass(type) {
      const m = {
        appointment_remind: 'dot-remind',
        order_status: 'dot-order',
        system_announcement: 'dot-notice',
        community_like: 'dot-like',
        community_comment: 'dot-comment',
        community_follow: 'dot-follow'
      }
      return m[type] || 'dot-default'
    },
    async loadData(refresh = false) {
      if (this.loading) return
      if (refresh) {
        this.pageNo = 1
        this.list = []
        this.noticesUnread = []
        this.hasMore = true
      }
      this.loading = true
      try {
        if (this.pageNo === 1) {
          const [myRes, noticesRes, countRes] = await Promise.all([
            api.getMyNotifications(this.pageNo, this.pageSize, false),
            api.getUnreadNotices(20, false),
            api.getNotificationUnreadCount(false)
          ])
          if ((myRes.code === 200 || myRes.code === 0) && myRes.data) {
            this.list = myRes.data.list || []
            this.unreadCount = myRes.data.unreadCount != null ? myRes.data.unreadCount : (countRes.data && countRes.data.count) || 0
          }
          if ((noticesRes.code === 200 || noticesRes.code === 0) && noticesRes.data) {
            this.noticesUnread = Array.isArray(noticesRes.data) ? noticesRes.data : []
          }
          // 调试：接口返回成功但仍为空时提示（便于定位数据或接口问题）
          if ((this.list.length === 0) && (this.noticesUnread.length === 0)) {
            uni.showToast({
              title: '调试：通知0条，公告0条',
              icon: 'none',
              duration: 2500
            })
          }
        } else {
          const myRes = await api.getMyNotifications(this.pageNo, this.pageSize, false)
          if ((myRes.code === 200 || myRes.code === 0) && myRes.data && myRes.data.list) {
            this.list = [...this.list, ...(myRes.data.list || [])]
          }
        }
        const total = (this.list.length >= this.pageSize) ? this.pageSize + 1 : this.list.length
        this.hasMore = this.list.length >= this.pageSize
      } catch (e) {
        console.error('加载通知失败', e)
        uni.showToast({
          title: '通知加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },
    loadMore() {
      if (!this.hasMore || this.loading) return
      this.pageNo++
      this.loadData()
    },
    async onItemTap(item) {
      if (item.isNotice && item.noticeId) {
        api.markNoticeAsRead(item.noticeId, false).catch(() => {})
      } else if (item.id && (item.status === 0 || item.status === 'unread')) {
        api.markNotificationRead(item.id, false).then(() => {
          item.status = 1
          if (this.unreadCount > 0) this.unreadCount--
        }).catch(() => {})
      }
      if (item.relatedType === 'order' && item.relatedId) {
        uni.navigateTo({ url: `/order/detail?orderId=${item.relatedId}` })
        return
      }
      if (item.relatedType === 'appointment' && item.relatedId) {
        uni.navigateTo({ url: `/user/my-appointments` })
        return
      }
      if (item.relatedType === 'post' && item.relatedId) {
        uni.navigateTo({ url: `/pages-community/post-detail-image?id=${item.relatedId}` })
        return
      }
      if (item.isNotice) {
        uni.navigateTo({ url: `/user/notice` })
      }
    },
    async markAllRead() {
      try {
        await api.markAllNotificationsRead(true)
        this.unreadCount = 0
        this.list.forEach(n => { n.status = 1 })
        // 同步标记所有未读公告为已读
        if (Array.isArray(this.noticesUnread) && this.noticesUnread.length) {
          await Promise.all(this.noticesUnread.map(n => api.markNoticeAsRead(n.id, false).catch(() => null)))
        }
        this.noticesUnread = []
        uni.showToast({ title: '已全部标为已读', icon: 'success' })
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    },
    formatTime(t) {
      if (!t) return ''
      const d = util.parseDate ? util.parseDate(t) : new Date(t)
      if (!d || isNaN(d.getTime())) return ''
      const now = new Date()
      const diff = now.getTime() - d.getTime()
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
      return (d.getMonth() + 1) + '-' + d.getDate()
    }
  }
}
</script>

<style lang="scss" scoped>
.notifications-container {
  min-height: 100vh;
  background: #f5f5f5;
}
.toolbar {
  padding: 24rpx 32rpx;
  text-align: right;
  background: #fff;
  border-bottom: 1rpx solid #eee;
}
.mark-all {
  font-size: 26rpx;
  color: #8D9F5E;
}
.list {
  height: calc(100vh - 120rpx);
  padding: 0 24rpx;
}
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}
.empty-icon { font-size: 100rpx; margin-bottom: 24rpx; opacity: 0.6; }
.empty-text { font-size: 28rpx; color: #666; margin-bottom: 10rpx; }
.empty-hint { font-size: 22rpx; color: #999; }
.item {
  display: flex;
  align-items: flex-start;
  padding: 22rpx 26rpx;
  margin: 18rpx 0;
  background: #fff;
  border: 2rpx solid #dedee8;
  border-radius: 22rpx;
  overflow: hidden; /* 确保圆角裁剪 */
  box-shadow: none;
  box-sizing: border-box;
  width: 100%;
}
.item.unread {
  background: #f7f9ff;
  border-color: #dfe7ff;
}
.dot {
  width: 40rpx;
  flex-shrink: 0;
  padding-top: 16rpx;
}
.type-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-left: 0;
}
.dot-remind { background: #ff9800; }
.dot-order { background: #2196f3; }
.dot-notice { background: #9c27b0; }
.dot-like { background: #e91e63; }
.dot-comment { background: #00bcd4; }
.dot-follow { background: #4caf50; }
.dot-default { background: #9e9e9e; }
.body {
  flex: 1;
  min-width: 0;
}
.line1 {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10rpx;
}
.title {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 16rpx;
}
.desc {
  font-size: 26rpx;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: normal;
}
.time {
  font-size: 22rpx;
  color: #999;
}
</style>
