<template>
  <view class="topic-detail-page">
    <view v-if="loading" class="loading-wrap">
      <text class="loading-text">加载中...</text>
    </view>
    <view v-else-if="error" class="empty-wrap">
      <text class="empty-text">{{ error }}</text>
    </view>
    <view v-else-if="!topic" class="empty-wrap">
      <text class="empty-text">专题不存在或已下架</text>
    </view>
    <scroll-view v-else scroll-y class="xhscroll">
      <!-- 1. 标题（最上方，与参考图一致） -->
      <view class="xh-title" v-if="topic.title">
        <text class="xh-title-txt">{{ topic.title }}</text>
      </view>

      <!-- 2. 正文区：富内容（文字+图片块）。封面仅用于列表缩略图，详情页不展示 -->
      <view class="xh-content">
        <block v-for="(block, idx) in contentBlocks" :key="idx">
          <text v-if="block.t === 'text' && block.v" class="xh-content-txt">{{ block.v }}</text>
          <view v-else-if="block.t === 'img' && block.v" class="xh-content-img-wrap">
            <image class="xh-content-img" :src="getImageUrl(block.v)" mode="widthFix" @error="onImageError" />
          </view>
        </block>
        <!-- 兼容旧数据：无 content 时显示 description -->
        <text v-if="contentBlocks.length === 0 && topic.description" class="xh-content-txt">{{ topic.description }}</text>
      </view>

      <!-- 4. 底部互动区 + 评论区（与图片帖子完全一致） -->
      <view class="xh-bottom-actions">
        <view class="xh-reply-hint-row" v-if="replyingToUserName">
          <text class="xh-reply-hint-text">回复 {{ replyingToUserName }}</text>
          <text class="xh-reply-cancel" @tap="cancelReply">取消</text>
        </view>
        <view class="xh-bottom-row">
          <input class="xh-bottom-input" type="text" v-model="commentText" :focus="isCommentFocused" :placeholder="replyingToUserName ? `回复 ${replyingToUserName}...` : '喜欢就给个评论支持一下~'" @focus="onCommentFocus" @blur="onCommentBlur" @confirm="submitComment" />
          <view class="xh-bottom-icons">
            <view class="xh-bottom-icon" @tap="toggleLike">
              <image class="xh-icon-img" :class="{ 'xh-icon-active': isLiked }" :src="isLiked ? '/static/images/community-like-on.svg' : '/static/images/community-like-off.svg'" mode="aspectFit" />
              <text class="xh-icon-num">{{ formatCount(likeCount) }}</text>
            </view>
            <view class="xh-bottom-icon" @tap="toggleCollect">
              <image class="xh-icon-img xh-collect-icon" :class="{ 'xh-icon-active': isCollected }" :src="isCollected ? '/static/images/community-collect-on.svg' : '/static/images/community-collect-off.svg'" mode="aspectFit" />
              <text class="xh-icon-num">{{ formatCount(collectCount) }}</text>
            </view>
            <view class="xh-bottom-icon" @tap="shareTopic">
              <image class="xh-icon-img" src="/static/images/分享.png" mode="aspectFit" />
            </view>
          </view>
        </view>
      </view>

      <!-- 评论区（与图片帖子完全一致的结构和样式） -->
      <view class="xh-comments-section">
        <view class="xh-comments-header">
          <view class="xh-comments-bar"></view>
          <text class="xh-comments-title">共{{ commentCount }}条评论</text>
        </view>
        <view class="xh-comments-list" v-if="comments.length > 0">
          <view class="xh-comment-item" v-for="c in comments" :key="c.id">
            <view class="xh-comment-main-row">
              <image class="xh-comment-avatar" :src="getImageUrl(c.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill" />
              <view class="xh-comment-content-wrapper">
                <view class="xh-comment-header">
                  <text class="xh-comment-name">{{ c.userName }}</text>
                </view>
                <text class="xh-comment-text">{{ c.content }}</text>
                <view class="xh-comment-meta">
                  <text class="xh-comment-date-loc">{{ formatCommentDate(c.createTime) }} {{ c.location || '' }}</text>
                  <text class="xh-comment-reply-btn" @click.stop="replyComment(c)">回复</text>
                </view>
                <view class="xh-comment-actions-right">
                  <view class="xh-comment-action-item" @click.stop="toggleCommentLike(c)">
                    <image class="xh-action-icon" :src="c.isLiked ? '/static/images/community-like-on.svg' : '/static/images/community-like-off.svg'" mode="aspectFit" />
                    <text class="xh-action-num" :class="{ 'xh-num-liked': c.isLiked }" v-if="(c.likesCount || 0) > 0">{{ c.likesCount }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view class="xh-reply-level1" v-if="hasCommentReplies(c)">
              <template v-if="c.replies && c.replies.length > 0">
                <template v-for="(ent, idx) in getFlatReplies(c)">
                <view v-if="ent.type === 'reply'" :key="ent._key" class="xh-reply-item">
                  <image class="xh-reply-avatar" :src="getImageUrl(ent.item.userAvatar || '/static/images/garfield-default-avatar.png')" mode="aspectFill" />
                  <view class="xh-reply-content-wrapper">
                    <view class="xh-reply-header">
                      <text class="xh-reply-name">{{ ent.item.userName }}</text>
                      <text class="xh-reply-arrow" v-if="getReplyToUserName(ent)">▲</text>
                      <text class="xh-reply-to-name" v-if="getReplyToUserName(ent)">{{ getReplyToUserName(ent) }}</text>
                    </view>
                    <text class="xh-reply-text">{{ ent.item.content }}</text>
                    <view class="xh-reply-meta">
                      <text class="xh-reply-date-loc">{{ formatCommentDate(ent.item.createTime) }} {{ ent.item.location || '' }}</text>
                      <text class="xh-reply-reply-btn" @click.stop="replyComment(ent.item)">回复</text>
                    </view>
                    <view class="xh-reply-actions-right">
                      <view class="xh-reply-action-item" @click.stop="toggleCommentLike(ent.item, ent.parent)">
                        <image class="xh-action-icon" :src="ent.item.isLiked ? '/static/images/community-like-on.svg' : '/static/images/community-like-off.svg'" mode="aspectFit" />
                        <text class="xh-action-num" :class="{ 'xh-num-liked': ent.item.isLiked }" v-if="(ent.item.likesCount || 0) > 0">{{ ent.item.likesCount }}</text>
                      </view>
                    </view>
                  </view>
                </view>
                <view v-else-if="ent.type === 'expand'" :key="`expand-${ent._key}`" class="xh-expand-same" @click.stop="expandReplies(ent.key)">
                  <view class="xh-expand-line"></view>
                  <text class="xh-expand-text">展开{{ ent.count }}条回复</text>
                  <text class="xh-expand-arrow">▼</text>
                </view>
                <view v-else-if="ent.type === 'expand_more_row'" :key="`expand_more_row-${ent._key}`" class="xh-expand-same xh-expand-more-row">
                  <view class="xh-expand-line"></view>
                  <text class="xh-expand-text" @click.stop="expandMoreReplies(ent.commentId)">展开更多</text>
                  <text class="xh-expand-arrow" @click.stop="expandMoreReplies(ent.commentId)">▼</text>
                  <view class="xh-expand-gap"></view>
                  <text class="xh-expand-text xh-expand-collapse" @click.stop="collapseReplies(ent.commentId)">收起</text>
                  <text class="xh-expand-arrow-up" @click.stop="collapseReplies(ent.commentId)">▲</text>
                </view>
                <view v-else-if="ent.type === 'collapse_row'" :key="`collapse_row-${ent._key}`" class="xh-expand-same" @click.stop="collapseReplies(ent.commentId)">
                  <view class="xh-expand-line"></view>
                  <text class="xh-expand-text">收起</text>
                  <text class="xh-expand-arrow-up">▲</text>
                </view>
              </template>
              </template>
              <view v-else class="xh-expand-same" @click.stop="expandReplies(c)">
                <view class="xh-expand-line"></view>
                <text class="xh-expand-text">{{ c.repliesLoading ? '加载中...' : '展开' + (c.replyCount || 0) + '条回复' }}</text>
                <text class="xh-expand-arrow">▼</text>
              </view>
            </view>
          </view>
        </view>
        <view class="xh-comments-empty" v-else>
          <text class="xh-comments-empty-txt">暂无评论，快来抢沙发吧~</text>
        </view>
      </view>
      <view class="xh-bottom-pad"></view>
    </scroll-view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  data() {
    return {
      id: null,
      topic: null,
      loading: true,
      error: '',
      currentUserId: null,
      commentText: '',
      replyingTo: null,
      replyingToUserName: '',
      comments: [],
      commentCount: 0,
      isLiked: false,
      likeCount: 0,
      isCollected: false,
      collectCount: 0,
      currentLocation: '',
      isCommentFocused: false,
      expandedReplies: {},
      expandedVisibleCount: {},
      lastCommentsLoadTime: 0
    }
  },
  computed: {
    contentBlocks() {
      if (!this.topic || !this.topic.content) return []
      try {
        const arr = JSON.parse(this.topic.content)
        return Array.isArray(arr) ? arr : []
      } catch {
        return []
      }
    }
  },
  onLoad(options) {
    const raw =
      options.id != null && options.id !== ''
        ? options.id
        : options.topicId != null && options.topicId !== ''
          ? options.topicId
          : null
    const n = raw != null ? parseInt(String(raw), 10) : NaN
    this.id = !isNaN(n) && n > 0 ? n : null
    if (!this.id) {
      this.loading = false
      this.error = '缺少专题 ID'
      return
    }
    this.loadCurrentUserId()
    this.getCurrentLocation()
    this.loadDetail().then(() => {
      this.loadComments(true)
    })
  },
  onShow() {
    this.loadCurrentUserId()
    if (!this.id) return
    // 静默刷新：节流 15 秒，不显示全局「加载中」，避免从其他页返回时反复弹 loading
    const now = Date.now()
    const throttleMs = 15 * 1000
    if (now - (this.lastCommentsLoadTime || 0) < throttleMs && (this.lastCommentsLoadTime || 0) > 0) return
    this.loadComments(true)
  },
  methods: {
    loadCurrentUserId() {
      let userId = uni.getStorageSync('userId')
      if (!userId) {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) userId = userInfo.id || userInfo.uid || userInfo.userId
      }
      this.currentUserId = userId ? Number(userId) : null
    },
    getCurrentLocation() {
      const cached = uni.getStorageSync('cached_location')
      const cacheTime = uni.getStorageSync('cached_location_time')
      if (cached && cacheTime && Date.now() - cacheTime < 5 * 60 * 1000) {
        this.currentLocation = cached
        return
      }
      this.currentLocation = ''
    },
    getImageUrl(url) {
      if (!url) return ''
      return util.getImageUrl ? util.getImageUrl(url) : url
    },
    onImageError() {},
    formatDate(str) {
      if (!str) return ''
      try {
        const s = String(str).replace(' ', 'T')
        const d = new Date(s)
        if (isNaN(d.getTime())) return ''
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
      } catch (e) {
        return ''
      }
    },
    formatCount(n) {
      if (!n) return '0'
      if (n < 1000) return String(n)
      if (n < 10000) return (n / 1000).toFixed(1) + 'k'
      return (n / 10000).toFixed(1) + 'w'
    },
    formatCommentDate(createTime) {
      const time = this._timeToMs(createTime)
      if (time == null || isNaN(time)) return ''
      const now = Date.now()
      const diff = now - time
      const minute = 60000
      const hour = 60 * minute
      const day = 24 * hour
      if (diff < minute) return '刚刚'
      if (diff < hour) return Math.floor(diff / minute) + '分钟前'
      if (diff < day) return Math.floor(diff / hour) + '小时前'
      if (diff < 7 * day) return Math.floor(diff / day) + '天前'
      const d = new Date(time)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    },
    _timeToMs(v) {
      if (v == null) return null
      if (typeof v === 'number') return v < 1e12 ? v * 1000 : v
      const d = new Date(typeof v === 'string' && v.includes(' ') ? v.replace(' ', 'T') : v)
      return isNaN(d.getTime()) ? null : d.getTime()
    },
    async loadDetail() {
      this.loading = true
      this.error = ''
      this.topic = null
      try {
        const res = await api.getDailyTopicDetail(this.id, this.currentUserId, false)
        if (res && (res.code === 200 || res.code === 0)) {
          if (!res.data) {
            this.error = (res && res.msg) || '专题不存在或已下架'
          } else {
            this.topic = res.data
            this.isLiked = this.topic.isLiked === true || this.topic.isLiked === 1
            this.likeCount = this.topic.likeCount || 0
            this.isCollected = this.topic.isCollected === true || this.topic.isCollected === 1
            this.collectCount = this.topic.collectCount != null ? this.topic.collectCount : 0
            const title = (this.topic.title || '').trim()
            if (title) {
              uni.setNavigationBarTitle({ title })
            }
          }
        } else {
          this.error = (res && res.msg) || '专题不存在或已下架'
        }
      } catch (e) {
        this.error = (e && e.message) || '加载失败，请重试'
      } finally {
        this.loading = false
      }
    },
    async loadComments(silent = false) {
      if (!this.id) return
      const showLoading = !silent
      try {
        const res = await api.getTopicComments(this.id, this.currentUserId, showLoading)
        if (res && (res.code === 200 || res.code === 0) && Array.isArray(res.data)) {
          this.comments = res.data
          for (const c of this.comments) {
            const repliesRes = await api.getCommentReplies(c.id, this.currentUserId, false)
            let rawReplies = []
            if (repliesRes && (repliesRes.code === 200 || repliesRes.code === 0) && Array.isArray(repliesRes.data)) {
              rawReplies = repliesRes.data
            }
            const tree = this.buildReplyTree(rawReplies, c.id, c.userName)
            this.$set(c, 'replies', tree)
            this.$set(c, 'replyCount', this.countAllReplies(tree))
          }
          this.commentCount = this.comments.reduce((s, c) => s + 1 + this.countAllReplies(c.replies || []), 0)
          this.lastCommentsLoadTime = Date.now()
        }
      } catch (e) {
        console.warn('加载评论失败:', e)
      }
    },
    buildReplyTree(flatList, parentId, parentUserName) {
      if (!flatList || !flatList.length) return []
      const pid = Number(parentId)
      const topLevel = flatList.filter(r => Number(r.parentId || 0) === pid || r.parentId === parentId)
      return topLevel.map(r => {
        const rid = Number(r.id)
        const nested = flatList.filter(n => Number(n.parentId || 0) === rid || n.parentId === r.id)
        const replyToUserName = (Number(r.parentId || 0) === pid || r.parentId === parentId) ? parentUserName : (flatList.find(x => Number(x.id) === Number(r.parentId)) || {}).userName || parentUserName
        return {
          ...r,
          replyToUserName,
          replies: nested.length ? this.buildReplyTree(flatList, r.id, r.userName) : []
        }
      })
    },
    countAllReplies(arr) {
      if (!Array.isArray(arr) || !arr.length) return 0
      return arr.reduce((n, r) => n + 1 + this.countAllReplies(r.replies || []), 0)
    },
    hasCommentReplies(c) {
      if (c.replyCount != null && Number(c.replyCount) === 0) return false
      if (c.replies && c.replies.length > 0) return true
      if (c.replyCount != null && Number(c.replyCount) > 0) return true
      return false
    },
    getReplyToUserName(ent) {
      if (!ent || !ent.item) return ''
      return ent.item.replyToUserName || (ent.parent && ent.parent.userName) || ''
    },
    getFlatReplies(c) {
      if (!c.replies || !c.replies.length) return []
      const expanded = this.expandedReplies[c.id]
      const out = []
      const flatList = []
      for (const r of c.replies) {
        flatList.push({ type: 'reply', item: r, parent: c, isNested: false, _key: 'r-' + (r.id || '') })
        for (const n of (r.replies || [])) {
          flatList.push({ type: 'reply', item: n, parent: r, isNested: true, _key: 'r-' + (n.id || '') })
        }
      }
      const totalReplies = flatList.length
      if (expanded) {
        const visibleCount = this.expandedVisibleCount[c.id] != null ? this.expandedVisibleCount[c.id] : 3
        const showCount = Math.min(visibleCount, totalReplies)
        for (let i = 0; i < showCount; i++) out.push(flatList[i])
        if (totalReplies > 3 && showCount < totalReplies) {
          out.push({ type: 'expand_more_row', commentId: c.id, totalReplies, visibleCount, _key: 'em-' + c.id })
        } else {
          out.push({ type: 'collapse_row', commentId: c.id, _key: 'co-' + c.id })
        }
      } else {
        const firstReply = c.replies[0]
        out.push({ type: 'reply', item: firstReply, parent: c, isNested: false, _key: 'r-' + (firstReply.id || '') })
        const hiddenCount = totalReplies - 1
        if (hiddenCount > 0) {
          out.push({ type: 'expand', key: c.id, count: hiddenCount, _key: 'e-' + c.id })
        }
      }
      return out
    },
    expandReplies(comment) {
      const commentId = typeof comment === 'object' ? comment.id : comment
      this.$set(this.expandedReplies, commentId, true)
    },
    expandMoreReplies(commentId) {
      const c = this.comments.find(co => co.id === commentId)
      if (!c) return
      const total = this.countAllReplies(c.replies || [])
      this.$set(this.expandedVisibleCount, commentId, total)
    },
    collapseReplies(commentId) {
      this.$set(this.expandedReplies, commentId, false)
      this.$set(this.expandedVisibleCount, commentId, 3)
    },
    onCommentFocus() {
      this.isCommentFocused = true
    },
    onCommentBlur() {
      this.isCommentFocused = false
    },
    async submitComment() {
      const text = (this.commentText || '').trim()
      if (!text) return
      if (!this.currentUserId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      try {
        const data = { userId: this.currentUserId, content: text, location: this.currentLocation }
        if (this.replyingTo) {
          data.parentId = this.replyingTo.id
        }
        const res = await api.addTopicComment(this.id, data)
        if (res && (res.code === 200 || res.code === 0)) {
          this.commentText = ''
          this.replyingTo = null
          this.replyingToUserName = ''
          uni.showToast({ title: '评论成功', icon: 'success' })
          this.loadComments()
        } else {
          uni.showToast({ title: (res && res.msg) || '评论失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: (e && e.message) || '评论失败', icon: 'none' })
      }
    },
    replyComment(c) {
      this.replyingTo = c
      this.replyingToUserName = c.userName || '用户'
      this.commentText = ''
      this.$nextTick(() => {
        this.isCommentFocused = true
      })
    },
    cancelReply() {
      this.replyingTo = null
      this.replyingToUserName = ''
    },
    async toggleLike() {
      if (!this.currentUserId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const nextLiked = !this.isLiked
      const oldCount = this.likeCount
      this.isLiked = nextLiked
      this.likeCount = nextLiked ? oldCount + 1 : Math.max(0, oldCount - 1)
      try {
        const res = nextLiked ? await api.likeTopic(this.id, this.currentUserId) : await api.unlikeTopic(this.id, this.currentUserId)
        if (res && res.code !== 200 && res.code !== 0) {
          this.isLiked = !nextLiked
          this.likeCount = oldCount
          uni.showToast({ title: (res && res.msg) || '操作失败', icon: 'none' })
        } else {
          uni.showToast({ title: nextLiked ? '点赞成功' : '已取消点赞', icon: 'success' })
        }
      } catch (e) {
        this.isLiked = !nextLiked
        this.likeCount = oldCount
        uni.showToast({ title: (e && e.message) || '操作失败', icon: 'none' })
      }
    },
    async toggleCollect() {
      if (!this.currentUserId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const nextCollected = !this.isCollected
      const oldCount = this.collectCount
      this.isCollected = nextCollected
      this.collectCount = nextCollected ? oldCount + 1 : Math.max(0, oldCount - 1)
      try {
        const res = nextCollected ? await api.collectTopic(this.id, this.currentUserId) : await api.uncollectTopic(this.id, this.currentUserId)
        if (res && res.code !== 200 && res.code !== 0) {
          this.isCollected = !nextCollected
          this.collectCount = oldCount
          uni.showToast({ title: (res && res.msg) || '操作失败', icon: 'none' })
        } else {
          uni.showToast({ title: this.isCollected ? '收藏成功' : '已取消收藏', icon: 'success' })
        }
      } catch (e) {
        this.isCollected = !nextCollected
        this.collectCount = oldCount
        uni.showToast({ title: (e && e.message) || '操作失败', icon: 'none' })
      }
    },
    shareTopic() {
      uni.showActionSheet({
        itemList: ['分享到微信', '分享到朋友圈', '复制链接'],
        success: (res) => {
          if (res.tapIndex === 0) {
            uni.showToast({ title: '请点击右上角···选择「发送给朋友」', icon: 'none', duration: 2500 })
          } else if (res.tapIndex === 1) {
            uni.showToast({ title: '请点击右上角···选择「分享到朋友圈」', icon: 'none', duration: 2500 })
          } else if (res.tapIndex === 2) {
            const link = this.getShareLink()
            uni.setClipboardData({
              data: link,
              success: () => uni.showToast({ title: '链接已复制', icon: 'success' }),
              fail: () => uni.showToast({ title: '复制失败', icon: 'none' })
            })
          }
        }
      })
    },
    getShareLink() {
      const path = `/pages-community/topic-detail?id=${this.id || ''}`
      try {
        const base = typeof util !== 'undefined' && util.getApiBaseUrl ? util.getApiBaseUrl().replace(/\/api.*$/, '') : ''
        if (base) return `${base}${path}`
      } catch (e) {}
      return path
    },
    async toggleCommentLike(c, parentComment) {
      if (!this.currentUserId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const wasLiked = c.isLiked
      const nextLiked = !wasLiked
      const oldCount = c.likesCount || c.likeCount || 0
      const nextCount = nextLiked ? oldCount + 1 : Math.max(0, oldCount - 1)
      this.$set(c, 'isLiked', nextLiked)
      this.$set(c, 'likesCount', nextCount)
      try {
        const res = nextLiked ? await api.likeComment(c.id, this.currentUserId) : await api.unlikeComment(c.id, this.currentUserId)
        if (res && res.code !== 200 && res.code !== 0) {
          this.$set(c, 'isLiked', wasLiked)
          this.$set(c, 'likesCount', oldCount)
        }
      } catch {
        this.$set(c, 'isLiked', wasLiked)
        this.$set(c, 'likesCount', oldCount)
      }
    }
  }
}
</script>

<style scoped>
.topic-detail-page { min-height: 100vh; width: 100%; background-color: #fff; }
.loading-wrap, .empty-wrap { display: flex; align-items: center; justify-content: center; min-height: 60vh; padding: 40rpx; }
.loading-text, .empty-text { font-size: 28rpx; color: #999; text-align: center; }
.xhscroll { width: 100%; height: 100vh; background-color: #fff; }

.xh-title { padding: 24rpx 30rpx 16rpx; background: #fff; }
.xh-title-txt { font-size: 36rpx; color: #333; line-height: 1.5; font-weight: 600; }

.xh-content { padding: 0 30rpx 24rpx; background: #fff; }
.xh-content-txt { font-size: 28rpx; color: #333; line-height: 1.8; word-break: break-word; display: block; margin-bottom: 24rpx; white-space: pre-wrap; }
.xh-content-img-wrap { margin-bottom: 24rpx; }
.xh-content-img { width: 100%; display: block; border-radius: 8rpx; background: #f5f5f5; }

/* 底部互动区（输入框+图标）- 固定在屏幕底部，与 post-detail-image 完全一致 */
.xh-bottom-actions {
  position: fixed !important;
  bottom: 0 !important;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1rpx solid #f0f0f0;
  padding: 14rpx 24rpx 18rpx 24rpx !important;
  margin: 0 !important;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8rpx;
  z-index: 100;
  box-sizing: border-box;
}
.xh-reply-hint-row { display: flex; align-items: center; justify-content: space-between; padding: 0 4rpx; }
.xh-reply-hint-text { font-size: 22rpx; color: #666; }
.xh-reply-cancel { font-size: 22rpx; color: #999; padding: 6rpx 12rpx; }
.xh-bottom-row { display: flex; align-items: center; gap: 14rpx; }
.xh-bottom-input { flex: 1; height: 64rpx; background: #f5f5f5; border-radius: 32rpx; padding: 0 22rpx; font-size: 26rpx; color: #333; }
.xh-bottom-icons { display: flex; align-items: center; gap: 14rpx; flex-shrink: 0; }
.xh-bottom-icon { display: flex; flex-direction: row; align-items: center; gap: 8rpx; }
.xh-icon-img { width: 36rpx; height: 36rpx; }
.xh-icon-img.xh-icon-active { }
.xh-collect-icon.xh-icon-active { filter: none; }
.xh-icon-num { font-size: 22rpx; color: #666; }

.xh-comments-section { padding: 14rpx 0 14rpx 24rpx; background: #fff; width: 100%; box-sizing: border-box; }
.xh-comments-header { display: flex; align-items: center; gap: 8rpx; margin-bottom: 14rpx; }
.xh-comments-bar { width: 4rpx; height: 20rpx; background: #ff2442; border-radius: 2rpx; }
.xh-comments-title { font-size: 26rpx; color: #333; }
.xh-comments-list { display: block; }
.xh-comment-item { margin-bottom: 20rpx; padding: 0 24rpx 0 0; box-sizing: border-box; }
.xh-comment-main-row { display: flex; gap: 12rpx; position: relative; }
.xh-comment-avatar { width: 56rpx; height: 56rpx; border-radius: 50%; flex-shrink: 0; }
.xh-comment-content-wrapper { flex: 1; position: relative; padding-right: 96rpx; }
.xh-comment-header { display: flex; align-items: center; gap: 6rpx; margin-bottom: 6rpx; }
.xh-comment-name { font-size: 24rpx; color: #999; }
.xh-comment-text { font-size: 24rpx; color: #333; line-height: 1.5; word-break: break-word; display: block; margin-bottom: 6rpx; }
.xh-comment-meta { display: flex; align-items: center; gap: 10rpx; margin-bottom: 6rpx; justify-content: flex-start; }
.xh-comment-date-loc { font-size: 22rpx; color: #999; }
.xh-comment-reply-btn { font-size: 22rpx; color: #999; padding: 4rpx 8rpx; user-select: none; }
.xh-comment-actions-right { position: absolute; right: 24rpx; bottom: 0; top: auto; display: flex; flex-direction: row; align-items: center; justify-content: flex-end; min-height: 48rpx; z-index: 10; }
.xh-comment-action-item { display: flex; flex-direction: row; align-items: center; justify-content: center; min-height: 48rpx; gap: 4rpx; }
.xh-action-icon { width: 24rpx; height: 24rpx; transition: transform 0.2s ease; }
.xh-comment-action-item:active .xh-action-icon { transform: scale(0.88); }
.xh-action-num { font-size: 22rpx; color: #999; margin-left: 0; }
.xh-action-num.xh-num-liked { color: #ff6b81; }

.xh-reply-level1 { margin-top: 12rpx; padding-left: 68rpx; }
.xh-reply-item { display: flex; gap: 10rpx; margin-bottom: 14rpx; position: relative; }
.xh-reply-avatar { width: 48rpx; height: 48rpx; border-radius: 50%; flex-shrink: 0; }
.xh-reply-content-wrapper { flex: 1; min-width: 0; }
.xh-reply-header { display: flex; align-items: center; flex-wrap: wrap; gap: 4rpx; margin-bottom: 4rpx; }
.xh-reply-name { font-size: 24rpx; color: #999; }
.xh-reply-arrow { font-size: 20rpx; color: #999; }
.xh-reply-to-name { font-size: 24rpx; color: #999; }
.xh-reply-text { font-size: 24rpx; color: #333; line-height: 1.5; word-break: break-word; }
.xh-reply-meta { display: flex; align-items: center; gap: 10rpx; margin-bottom: 4rpx; }
.xh-reply-date-loc { font-size: 22rpx; color: #999; }
.xh-reply-reply-btn { font-size: 22rpx; color: #999; padding: 4rpx 8rpx; }
.xh-reply-actions-right { position: absolute; right: 0; top: 0; display: flex; flex-direction: row; align-items: center; gap: 4rpx; }
.xh-expand-same { display: flex; align-items: center; gap: 8rpx; padding: 8rpx 0; }
.xh-expand-line { flex: 1; height: 1rpx; background: #eee; }
.xh-expand-text { font-size: 22rpx; color: #999; }
.xh-expand-arrow { font-size: 20rpx; color: #999; }
.xh-expand-arrow-up { font-size: 20rpx; color: #999; }
.xh-expand-more-row { justify-content: flex-start; gap: 12rpx; }
.xh-expand-gap { width: 20rpx; }
.xh-expand-collapse { margin-left: 8rpx; }
.xh-comments-empty { padding: 40rpx 0; text-align: center; }
.xh-comments-empty-txt { font-size: 26rpx; color: #999; }
.xh-bottom-pad { height: 140rpx; }
</style>
