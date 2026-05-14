<template>
  <view class="notice-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ ui.loading }}</text>
    </view>

    <view v-else-if="filteredFlatList.length === 0" class="empty-container">
      <text class="empty-text">{{ ui.empty }}</text>
    </view>

    <scroll-view
      v-else
      class="notice-list"
      scroll-y
      :style="{ height: scrollHeight + 'px' }"
      enable-back-to-top
    >
      <!-- 分类标签栏 -->
      <view class="tab-bar">
        <view
          v-for="(tab, idx) in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ 'tab-active': activeTab === tab.key }"
          @tap="activeTab = tab.key"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <text v-if="getUnreadCount(tab.key) > 0" class="tab-badge">{{ getUnreadCount(tab.key) > 99 ? '99+' : getUnreadCount(tab.key) }}</text>
        </view>
      </view>

      <!-- 通知列表 -->
      <view v-for="(item, index) in filteredFlatList" :key="getNoticeKey(item, index)" class="msg-item" @tap="onNoticeTap(item)">
        <view v-if="isUnreadItem(item)" class="msg-left-dot" />
        <view class="msg-type-tag" :class="'tag-' + getTypeCategory(item.type)">{{ getTypeLabel(item.type) }}</view>
        <view class="msg-main">
          <view class="msg-title-row">
            <text class="msg-title">{{ item.title }}</text>
            <text class="time-text">{{ formatRelativeTime(item.createTime || item.effectiveTime || item.updateTime) }}</text>
          </view>
          <text class="msg-content">{{ item.content }}</text>
          <navigator :url="getDetailUrl(item)" class="msg-detail-link" open-type="navigate">
            <text class="detail-link-text">{{ getDetailLinkText(item) }}</text>
          </navigator>
        </view>
      </view>

      <view class="safe-area-bottom"></view>
    </scroll-view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'Notice',
  data() {
    return {
      loading: false,
      list: [],
      scrollHeight: 0,
      activeTab: 'all',
      tabs: [
        { key: 'all', label: '\u5168\u90e8' },
        { key: 'appointment', label: '\u9884\u7ea6' },
        { key: 'order', label: '\u8ba2\u5355' },
        { key: 'system', label: '\u7cfb\u7edf' },
        { key: 'complaint', label: '\u6295\u8bc9' }
      ],
      ui: {
        loading: '\u52a0\u8f7d\u4e2d...',
        empty: '\u6682\u65e0\u901a\u77e5'
      }
    }
  },
  computed: {
    flatList() {
      if (!this.list || this.list.length === 0) return []
      return [...this.list].sort((a, b) => {
        const ta = new Date(a.createTime || a.effectiveTime || a.updateTime || 0).getTime()
        const tb = new Date(b.createTime || b.effectiveTime || b.updateTime || 0).getTime()
        return tb - ta
      })
    },
    filteredFlatList() {
      if (this.activeTab === 'all') return this.flatList
      return this.flatList.filter(item => this.getTypeCategory(item.type) === this.activeTab)
    }
  },
  onLoad(options) {
    this.calcScrollHeight()
    this.bootstrapPage().catch(() => { this.list = [] })
  },
  onShow() {
    this.bootstrapPage().catch(() => { this.list = [] })
  },
  methods: {
    getNoticeKey(item, index) {
      try {
        if (!item) return String(index)
        if (item.id != null) return String(item.id)
        return String(index)
      } catch (e) {
        return String(index)
      }
    },

    getTypeCategory(type) {
      if (!type) return 'system'
      const t = String(type).toLowerCase()
      if (t.includes('appointment') || t.includes('noshow')) return 'appointment'
      if (t.includes('order')) return 'order'
      if (t.includes('complaint')) return 'complaint'
      if (t.includes('announce') || t.includes('system')) return 'system'
      return 'system'
    },

    getTypeLabel(type) {
      const cat = this.getTypeCategory(type)
      const map = { appointment: '\u9884\u7ea6', order: '\u8ba2\u5355', system: '\u7cfb\u7edf', complaint: '\u6295\u8bc9' }
      return map[cat] || '\u7cfb\u7edf'
    },

    getUnreadCount(tabKey) {
      if (tabKey === 'all') {
        return this.flatList.filter(item => this.isUnreadItem(item)).length
      }
      return this.flatList.filter(item => this.isUnreadItem(item) && this.getTypeCategory(item.type) === tabKey).length
    },

    async bootstrapPage() {
      await this.clearUnreadState()
      await this.loadNotices()
    },

    isUnreadItem(item) {
      if (!item) return false
      if (item._src === 'notice') return true
      if (item._src === 'notification') {
        const s = item.status
        return s === 0 || s === '0' || s === 'unread'
      }
      const s = item.status
      if (item.userId != null && item.userId !== undefined) {
        return s === 0 || s === '0' || s === 'unread'
      }
      return true
    },

    calcScrollHeight() {
      uni.getSystemInfo({
        success: (res) => {
          this.scrollHeight = (res.windowHeight || 600) - 100
        }
      })
    },

    async loadNotices() {
      this.loading = true
      try {
        const settled = await Promise.allSettled([
          api.getMyNotifications(1, 50, false),
          api.getNoticeList(null, 1, 50, false)
        ])
        let myNotifRes = settled[0].status === 'fulfilled' ? settled[0].value : null
        const noticeRes = settled[1].status === 'fulfilled' ? settled[1].value : null
        const raw = myNotifRes && myNotifRes.data
        let myList = []
        if (Array.isArray(raw)) {
          myList = raw
        } else if (raw && Array.isArray(raw.list)) {
          myList = raw.list
        } else if (raw && Array.isArray(raw.records)) {
          myList = raw.records
        }
        const filtered = myList.filter(n => {
          const t = n && n.type ? String(n.type) : ''
          return !['community_like', 'community_comment', 'community_follow'].includes(t)
        })
        const notifRows = filtered.map((n) => ({ ...n, _src: 'notification' }))
        const nr = noticeRes && noticeRes.data
        const noticeArr = Array.isArray(nr) ? nr : []
        const noticeRows = noticeArr.map((n) => ({ ...n, _src: 'notice' }))
        this.list = [...notifRows, ...noticeRows]
      } catch (e) {
        console.error('loadNotices failed:', e)
        this.list = []
      } finally {
        this.loading = false
      }
    },

    async clearUnreadState() {
      try {
        await Promise.all([
          api.markAllNotificationsRead(false).catch(() => {}),
          this.markAllNoticesRead()
        ])
      } catch (e) {}
    },

    async markAllNoticesRead() {
      try {
        const res = await api.getUnreadNotices(50, false)
        const list = (res && res.data && Array.isArray(res.data)) ? res.data : []
        if (list.length > 0) {
          await Promise.all(list.map(n => api.markNoticeAsRead(n.id, false).catch(() => null)))
        }
      } catch (e) {}
    },

    formatRelativeTime(timestampOrString) {
      if (!timestampOrString) return ''
      try {
        const date = new Date(timestampOrString)
        if (isNaN(date.getTime())) return ''
        const now = new Date()
        const diffMs = now.getTime() - date.getTime()
        if (diffMs < 0) return ''
        const diffMinutes = Math.floor(diffMs / (1000 * 60))
        if (diffMinutes < 1) return '\u521a\u521a'
        if (diffMinutes < 60) return `${diffMinutes}\u5206\u949f\u524d`
        const diffHours = Math.floor(diffMinutes / 60)
        if (diffHours < 24) return `${diffHours}\u5c0f\u65f6\u524d`
        const diffDays = Math.floor(diffHours / 24)
        if (diffDays < 30) return `${diffDays}\u5929\u524d`
        if (diffDays < 365) {
          const diffMonths = Math.floor(diffDays / 30)
          return `${diffMonths}\u4e2a\u6708\u524d`
        }
        const y = date.getFullYear()
        const m = String(date.getMonth() + 1).padStart(2, '0')
        const d = String(date.getDate()).padStart(2, '0')
        return `${y}-${m}-${d}`
      } catch (e) {
        return ''
      }
    },

    getDetailUrl(item) {
      if (!item) return ''
      if (item._src === 'notice') {
        return item.id != null ? `/user/announcement?id=${item.id}` : '/user/announcement'
      }

      const type = item.type ? String(item.type) : ''
      const title = item.title ? String(item.title) : ''
      const content = item.content ? String(item.content) : ''
      const full = `${title} ${content}`

      let appointmentId = item.relatedId != null ? Number(item.relatedId) : null
      let serviceType = ''
      if (full.includes('\u4e0a\u95e8\u5c34\u5c4e')) serviceType = 'door-cleaning'
      else if (full.includes('\u6d17\u62a4')) serviceType = 'grooming'
      else if (full.includes('\u533b\u9662')) serviceType = 'hospital'

      if (type === 'order_status' || full.includes('\u8ba2\u5355')) {
        const orderId = item.relatedId != null ? Number(item.relatedId) : null
        if (orderId && orderId > 0) {
          return `/order/detail?id=${orderId}`
        }
        return '/order/list'
      }

      if (type.includes('appointment') || full.includes('\u9884\u7ea6')) {
        if (!appointmentId) {
          const idMatch = full.match(/\u9884\u7ea6\u5355#(\d+)/)
          appointmentId = idMatch && idMatch[1] ? Number(idMatch[1]) : null
        }
        if (appointmentId) {
          return `/appointment/detail?id=${appointmentId}&serviceType=${serviceType || 'door-cleaning'}`
        }
        return '/user/my-appointments?status=confirmed'
      }

      if (type.includes('complaint')) {
        return '/user/announcement'
      }

      const isSystemLike =
        type.toLowerCase().includes('system') ||
        type.toLowerCase().includes('announce') ||
        /\u7cfb\u7edf(\u66f4\u65b0|\u901a\u77e5|\u516c\u544a)|^\s*\u516c\u544a/.test(title + content)
      if (isSystemLike) {
        return '/user/announcement'
      }

      if (appointmentId) {
        return `/appointment/detail?id=${appointmentId}&serviceType=${serviceType || 'door-cleaning'}`
      }
      return '/user/announcement'
    },

    getDetailLinkText(item) {
      if (!item) return '\u67e5\u770b\u8be6\u60c5'
      const type = item.type ? String(item.type) : ''
      const full = `${item.title || ''} ${item.content || ''}`
      if (item._src === 'notice') return '\u67e5\u770b\u516c\u544a\u8be6\u60c5'
      if (type === 'order_status' || full.includes('\u8ba2\u5355')) return '\u67e5\u770b\u8ba2\u5355\u8be6\u60c5'
      if (type.includes('appointment') || full.includes('\u9884\u7ea6')) return '\u67e5\u770b\u9884\u7ea6\u8be6\u60c5'
      if (type.includes('complaint')) return '\u67e5\u770b\u8be6\u60c5'
      return '\u67e5\u770b\u8be6\u60c5'
    },

    onNoticeTap(item) {
      const url = this.getDetailUrl(item)
      if (url) uni.navigateTo({ url })
    }
  }
}
</script>

<style lang="scss" scoped>
.notice-page {
  min-height: 100vh;
  background-color: #f5f6fa;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  .loading-text,
  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

/* 分类标签栏 */
.tab-bar {
  display: flex;
  background-color: #fff;
  padding: 16rpx 20rpx;
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  position: relative;
  padding: 12rpx 28rpx;
  margin-right: 16rpx;
  border-radius: 32rpx;
  background-color: #f0f0f0;
  transition: all 0.2s;

  &.tab-active {
    background-color: #1890ff;

    .tab-text {
      color: #fff;
      font-weight: 600;
    }
  }
}

.tab-text {
  font-size: 26rpx;
  color: #666;
  white-space: nowrap;
}

.tab-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  min-width: 32rpx;
  height: 32rpx;
  line-height: 32rpx;
  text-align: center;
  background-color: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  padding: 0 8rpx;
}

/* 通知列表 */
.notice-list {
  padding: 0 0 40rpx;
}

.msg-item {
  padding: 26rpx 24rpx;
  border-bottom: 1rpx solid #f6f6f6;
  display: flex;
  align-items: flex-start;
  background-color: #ffffff;
  position: relative;
}

.msg-left-dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background-color: #ff4d4f;
  flex-shrink: 0;
  margin-top: 15rpx;
  margin-right: 16rpx;
}

.msg-type-tag {
  flex-shrink: 0;
  padding: 4rpx 14rpx;
  border-radius: 6rpx;
  font-size: 20rpx;
  color: #fff;
  margin-right: 14rpx;
  margin-top: 6rpx;

  &.tag-appointment { background-color: #17c3b2; }
  &.tag-order { background-color: #1890ff; }
  &.tag-system { background-color: #faad14; }
  &.tag-complaint { background-color: #ff7a45; }
}

.msg-main {
  flex: 1;
  min-width: 0;
}

.msg-title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.msg-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #212121;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.time-text {
  font-size: 20rpx;
  color: #9499a0;
  flex-shrink: 0;
  max-width: 180rpx;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.msg-content {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 26rpx;
  color: #61666d;
  line-height: 1.6;
  word-break: break-all;
}

.msg-detail-link {
  margin-top: 12rpx;
}

.detail-link-text {
  font-size: 26rpx;
  color: #1890ff;
}

.safe-area-bottom {
  height: 50rpx;
}
</style>
