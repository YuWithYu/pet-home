<template>
  <view class="announcement-page">
    <view v-if="loading" class="loading-container">
      <text class="loading-text">{{ ui.loading }}</text>
    </view>

    <view v-else-if="list.length === 0" class="empty-container">
      <text class="empty-text">{{ ui.empty }}</text>
    </view>

    <scroll-view
      v-else
      class="announcement-list"
      scroll-y
      :style="{ height: scrollHeight + 'px' }"
      enable-back-to-top
    >
      <view v-for="(item, index) in sortedList" :key="item.id || index" class="announcement-item" :data-index="index" @tap="onItemTap">
        <view class="announcement-title">{{ item.title }}</view>
        <view class="announcement-time">{{ formatTime(item.createTime || item.effectiveTime || item.updateTime) }}</view>
        <text class="announcement-content">{{ item.content }}</text>
      </view>
      <view class="safe-area-bottom"></view>
    </scroll-view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'Announcement',
  data() {
    return {
      loading: false,
      list: [],
      highlightId: null,
      scrollHeight: 0,
      ui: {
        loading: '\u52a0\u8f7d\u4e2d...',
        empty: '\u6682\u65e0\u516c\u544a',
        titleFallback: '\u516c\u544a',
        confirmOk: '\u786e\u5b9a'
      }
    }
  },
  computed: {
    sortedList() {
      if (!this.list || this.list.length === 0) return []
      return [...this.list].sort((a, b) => {
        const ta = new Date(a.createTime || a.effectiveTime || a.updateTime || 0).getTime()
        const tb = new Date(b.createTime || b.effectiveTime || b.updateTime || 0).getTime()
        return tb - ta
      })
    }
  },
  onLoad(options) {
    this.calcScrollHeight()
    if (options && options.id != null && String(options.id).trim() !== '') {
      this.highlightId = String(options.id).trim()
    }
    this.loadList()
  },
  onPullDownRefresh() {
    this.loadList().finally(() => uni.stopPullDownRefresh())
  },
  methods: {
    calcScrollHeight() {
      try {
        const sys = uni.getSystemInfoSync()
        this.scrollHeight = (sys.windowHeight || sys.screenHeight || 600) - 0
      } catch (e) {
        this.scrollHeight = 600
      }
    },
    async loadList() {
      this.loading = true
      try {
        const res = await api.getNoticeList(null, 1, 50, false)
        if (res && (res.code === 200 || res.code === 0) && Array.isArray(res.data)) {
          this.list = res.data
        } else {
          this.list = []
        }
      } catch (e) {
        console.error('loadNoticeList', e)
        this.list = []
      } finally {
        this.loading = false
        this.$nextTick(() => this.openHighlightIfAny())
      }
    },
    openHighlightIfAny() {
      if (!this.highlightId || !this.sortedList || this.sortedList.length === 0) return
      const found = this.sortedList.find((x) => x && String(x.id) === String(this.highlightId))
      if (!found) return
      if (found.id) {
        api.markNoticeAsRead(found.id, false).catch(() => {})
      }
      uni.showModal({
        title: found.title || this.ui.titleFallback,
        content: found.content || '',
        showCancel: false,
        confirmText: this.ui.confirmOk
      })
      this.highlightId = null
    },
    formatTime(t) {
      if (t == null || t === '') return ''
      try {
        if (typeof t === 'object' && t.date !== undefined) {
          t = t.date + (t.time ? 'T' + t.time : '')
        }
        const d = new Date(t)
        if (isNaN(d.getTime())) return ''
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const day = String(d.getDate()).padStart(2, '0')
        const h = String(d.getHours()).padStart(2, '0')
        const min = String(d.getMinutes()).padStart(2, '0')
        return `${y}-${m}-${day} ${h}:${min}`
      } catch (e) {
        return ''
      }
    },
    onItemTap(e) {
      const idx = e && e.currentTarget && e.currentTarget.dataset ? e.currentTarget.dataset.index : undefined
      const i = idx != null ? parseInt(idx, 10) : -1
      const item = i >= 0 && this.sortedList && this.sortedList[i] ? this.sortedList[i] : null
      if (!item || typeof item !== 'object') return
      if (item.id) {
        api.markNoticeAsRead(item.id, false).catch(() => {})
      }
      uni.showModal({
        title: item.title || this.ui.titleFallback,
        content: item.content || '',
        showCancel: false,
        confirmText: this.ui.confirmOk
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.announcement-page {
  min-height: 100vh;
  background-color: #f5f5f5;
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
.announcement-list {
  padding: 20rpx 0 40rpx;
}
.announcement-item {
  margin: 0 24rpx 20rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 12rpx;
  .announcement-title {
    font-size: 30rpx;
    font-weight: 600;
    color: #333;
    margin-bottom: 12rpx;
  }
  .announcement-time {
    font-size: 22rpx;
    color: #999;
    margin-bottom: 16rpx;
  }
  .announcement-content {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
    display: block;
  }
}
.safe-area-bottom {
  height: 40rpx;
}
</style>
