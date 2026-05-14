<template>
  <view class="message-container">
    <!-- 消息分类 -->
    <view class="message-categories">
      <view class="category-item" @tap="switchCategory('likes')">
        <view class="category-icon likes-icon">
          <image class="category-icon-image" src="/static/images/赞与收藏.png" mode="aspectFit"></image>
        </view>
        <text class="category-text">赞与收藏</text>
        <view v-if="unreadCounts.likes > 0" class="badge">{{ unreadCounts.likes > 99 ? '99+' : unreadCounts.likes }}</view>
      </view>
      <view class="category-item" @tap="switchCategory('follows')">
        <view class="category-icon follows-icon">
          <image class="category-icon-image" src="/static/images/新增关注.png" mode="aspectFit"></image>
        </view>
        <text class="category-text">新增关注</text>
        <view v-if="unreadCounts.follows > 0" class="badge">{{ unreadCounts.follows > 99 ? '99+' : unreadCounts.follows }}</view>
      </view>
      <view class="category-item" @tap="switchCategory('comments')">
        <view class="category-icon comments-icon">
          <image class="category-icon-image" src="/static/images/评论和@.png" mode="aspectFit"></image>
        </view>
        <text class="category-text">评论和@</text>
        <view v-if="unreadCounts.comments > 0" class="badge">{{ unreadCounts.comments > 99 ? '99+' : unreadCounts.comments }}</view>
      </view>
    </view>

    <!-- 会话列表 -->
    <scroll-view class="conversation-list" scroll-y @scrolltolower="loadMoreConversations">
      <view v-if="loading && conversations.length === 0 && systemNotices.length === 0" class="loading-state">
        <text>加载中...</text>
      </view>
      <view v-else-if="conversations.length === 0 && systemNotices.length === 0" class="empty-state">
        <view class="empty-icon">📦</view>
        <text class="empty-text">您还没有消息哦~</text>
        <text class="empty-hint">互动消息和群消息会显示在这里哦~</text>
      </view>
      <view v-else>
        <!-- 系统通知：用聊天列表同样的行结构展示（头像/名字/时间/最新一条预览） -->
        <view
          v-if="currentUserId"
          class="conversation-item"
          @tap="onSystemNoticesRowTap"
        >
          <image
            class="avatar system-notice-avatar"
            src="/static/images/时钟.png"
            mode="aspectFill"
          />
          <view class="conversation-info">
            <view class="conversation-header">
              <view class="conversation-name-row">
                <text class="conversation-name">系统通知</text>
              </view>
              <text class="conversation-time">{{ systemLatestTimeText }}</text>
            </view>
            <view class="conversation-content">
              <text class="conversation-message">{{ systemLatestTitleText }}</text>
              <view v-if="systemUnreadCount > 0" class="unread-badge">
                {{ systemUnreadCount > 99 ? '99+' : systemUnreadCount }}
              </view>
            </view>
          </view>
        </view>

        <view 
          class="conversation-swipe-wrap"
          v-for="(conv, index) in conversations" 
          :key="index"
        >
          <view 
            class="conversation-swipe-inner"
            :style="{ transform: 'translateX(' + (swipeTranslatePx[index] || 0) + 'px)' }"
            @touchstart="onSwipeStart($event, index)"
            @touchmove="onSwipeMove($event, index)"
            @touchend="onSwipeEnd($event, index)"
            @tap="onConversationTap(index)"
          >
            <view 
              class="conversation-item" 
              :class="{ 'conversation-item-pinned': isPinned(conv) }"
              :data-index="index"
            >
              <image class="avatar" :src="getAvatarUrl(conv.isPlatform ? '/static/images/宠物之家.png' : conv.otherUserAvatar)" mode="aspectFill" />
              <view class="conversation-info">
                <view class="conversation-header">
                  <view class="conversation-name-row">
                    <text class="conversation-name">{{ conv.otherUserName || '用户' }}</text>
                    <view v-if="conv.isService" class="role-tag role-tag-service">客服</view>
                  </view>
                  <text class="conversation-time">{{ formatTime(conv.lastMessageTime) }}</text>
                </view>
                <view class="conversation-content">
                  <text class="conversation-message">{{ displayLastMessage(conv) }}</text>
                  <view v-if="getUnreadCount(conv) > 0" class="unread-badge">{{ getUnreadCount(conv) > 99 ? '99+' : getUnreadCount(conv) }}</view>
                </view>
              </view>
            </view>
            <view 
              class="conversation-pin-btn"
              @tap.stop="togglePin(index)"
            >
              <text>{{ isPinned(conv) ? '取消置顶' : '置顶' }}</text>
            </view>
            <view 
              class="conversation-delete-btn"
              @tap.stop="deleteConversation(index)"
            >
              <text>删除</text>
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
  name: 'MessageIndex',
  data() {
    return {
      conversations: [],
      systemNotices: [],
      loading: false,
      currentUserId: null,
      lastConversationsLoadTime: 0,
      unreadCounts: {
        likes: 0,
        follows: 0,
        comments: 0
      },
      page: 1,
      pageSize: 20,
      hasMore: true,
      systemNoticeLimit: 50,
      refreshTimer: null,
      swipeTranslatePx: {},
      touchStartX: 0,
      touchStartIndex: null,
      pinnedConversationIds: [],
      deletedConversationIds: [] // 用户删除的会话 ID，持久化后刷新不再显示
    }
  },
  
  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn']),
    systemUnreadCount() {
      return Array.isArray(this.systemNotices) ? this.systemNotices.length : 0
    },
    systemLatestNotice() {
      if (!Array.isArray(this.systemNotices) || this.systemNotices.length === 0) return null
      const sorted = [...this.systemNotices].sort((a, b) => {
        const ta = new Date(a.createTime || a.create_time || a.effectiveTime || a.updateTime || 0).getTime()
        const tb = new Date(b.createTime || b.create_time || b.effectiveTime || b.updateTime || 0).getTime()
        return tb - ta
      })
      return sorted[0] || null
    },
    systemLatestNoticeTime() {
      const n = this.systemLatestNotice
      if (!n) return null
      return n.createTime || n.create_time || n.effectiveTime || n.updateTime || null
    },
    systemLatestTimeText() {
      return this.systemLatestNoticeTime ? this.formatTime(this.systemLatestNoticeTime) : ''
    },
    systemLatestTitleText() {
      const n = this.systemLatestNotice
      if (!n) return '系统通知'
      return n.title || n.content || '系统通知'
    }
  },
  onLoad() {
    let raw = this.userInfo?.id ?? this.userInfo?.uid ?? uni.getStorageSync('userId') ?? null
    if (raw === '' || raw === 'undefined' || (typeof raw === 'string' && isNaN(Number(raw)))) raw = null
    this.currentUserId = raw
    
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
    
    // 从本地读取置顶会话 ID 列表
    try {
      const uidKey = this.getPinnedStorageKey ? this.getPinnedStorageKey() : 'message_pinned_ids'
      const raw = uni.getStorageSync(uidKey)
      // 兼容历史全局 key
      const legacyRaw = raw ? '' : uni.getStorageSync('message_pinned_ids')
      this.pinnedConversationIds = (raw || legacyRaw) ? JSON.parse(raw || legacyRaw) : []
    } catch (e) {
      this.pinnedConversationIds = []
    }
    // 从本地读取已删除会话 ID 列表（删除后刷新不再显示）
    try {
      const uidKey = this.getDeletedStorageKey ? this.getDeletedStorageKey() : 'message_deleted_ids'
      const deletedRaw = uni.getStorageSync(uidKey)
      // 兼容历史全局 key
      const legacyDeletedRaw = deletedRaw ? '' : uni.getStorageSync('message_deleted_ids')
      this.deletedConversationIds = (deletedRaw || legacyDeletedRaw) ? JSON.parse(deletedRaw || legacyDeletedRaw) : []
    } catch (e) {
      this.deletedConversationIds = []
    }
    
    // 加载会话列表
    this.loadConversations()
    // 加载系统通知（放到消息列表中展示）
    this.loadSystemNotices()
    
    // 启动定时刷新
    this.startAutoRefresh()
  },
  
  onShow() {
    if (util.redirectStaffToMineIfNeeded()) return
    // 仅当 userId 有效（非 undefined 字符串、可转数字）时才请求未读，避免 500
    const uid = this.currentUserId
    if (uid != null && uid !== '' && uid !== 'undefined' && !(typeof uid === 'string' && isNaN(Number(uid)))) {
      this.loadCategoryUnreadCounts()
      this.loadSystemNotices()
    }
    // 页面显示时静默刷新会话列表（节流 2 秒）
    const now = Date.now()
    const throttleMs = 2 * 1000
    if (this.currentUserId && (now - (this.lastConversationsLoadTime || 0) < throttleMs) && (this.lastConversationsLoadTime || 0) > 0) {
      uni.$on('messageSent', this.handleMessageSent)
      uni.$on('messagesRead', this.handleMessagesRead)
      return
    }
    if (this.currentUserId) {
      this.loadConversations(true, true)
    }
    
    // 监听消息发送事件
    uni.$on('messageSent', this.handleMessageSent)
    // 监听消息已读事件
    uni.$on('messagesRead', this.handleMessagesRead)
  },
  
  onHide() {
    // 移除事件监听
    uni.$off('messageSent', this.handleMessageSent)
    uni.$off('messagesRead', this.handleMessagesRead)
  },

  onPullDownRefresh() {
    if (!this.currentUserId) {
      uni.stopPullDownRefresh()
      return
    }
    Promise.all([this.loadConversations(true, true), this.loadSystemNotices()]).finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  
  onUnload() {
    // 清除定时器
    this.stopAutoRefresh()
  },
  
  methods: {
    getPinnedStorageKey() {
      const uid = this.currentUserId == null ? '' : String(this.currentUserId)
      return uid ? `message_pinned_ids_${uid}` : 'message_pinned_ids'
    },
    getDeletedStorageKey() {
      const uid = this.currentUserId == null ? '' : String(this.currentUserId)
      return uid ? `message_deleted_ids_${uid}` : 'message_deleted_ids'
    },
    isPinned(conv) {
      const id = conv.conversationId || conv.otherUserId || ''
      return (this.pinnedConversationIds || []).indexOf(id) !== -1
    },
    displayLastMessage(conv) {
      const raw = conv.lastMessage || ''
      const stripped = String(raw).replace(/^\[DOCTOR_MESSAGE\]/i, '').trim()
      if (!stripped) return '暂无消息'
      // 像微信一样：图片消息只显示「图片」，不显示 URL
      if (/^\[图片\](https?:\/\/[^\s\[\]]*)?/i.test(stripped) || stripped.startsWith('[图片]')) return '「图片」'
      return stripped
    },
    getUnreadCount(conv) {
      const n = conv.unreadCount
      if (n === undefined || n === null) return 0
      const num = Number(n)
      return isNaN(num) || num < 0 ? 0 : num
    },
    sortConversationsWithPinned(list, getMsgTime) {
      if (!list || !list.length) return
      const pinned = this.pinnedConversationIds || []
      list.sort((a, b) => {
        const aId = a.conversationId || a.otherUserId || ''
        const bId = b.conversationId || b.otherUserId || ''
        const aPin = pinned.indexOf(aId) !== -1
        const bPin = pinned.indexOf(bId) !== -1
        if (aPin && !bPin) return -1
        if (!aPin && bPin) return 1
        const ta = getMsgTime ? getMsgTime(a.lastMessageTime) : 0
        const tb = getMsgTime ? getMsgTime(b.lastMessageTime) : 0
        return tb - ta
      })
    },
    isMerchantServiceConversation(conv) {
      if (!conv) return false
      // 仅隐藏商城商家客服会话；保留平台在线咨询和门店客服会话
      if (conv.isPlatform) return false
      if (conv.isOutlet) return false
      if (conv.isService) return true
      const otherUserId = String(conv.otherUserId || '').toLowerCase()
      if (otherUserId.startsWith('platform_')) return false
      if (otherUserId.startsWith('outlet_')) return false
      if (otherUserId.startsWith('service_')) return true
      const name = String(conv.otherUserName || conv.storeName || '')
      if (name.includes('门店客服')) return false
      if (name.includes('店铺客服')) return true
      return false
    },
    togglePin(index) {
      const conv = this.conversations[index]
      if (!conv) return
      const id = conv.conversationId || conv.otherUserId || ''
      if (!id) return
      let pinned = this.pinnedConversationIds || []
      const i = pinned.indexOf(id)
      if (i !== -1) {
        pinned = pinned.slice(0, i).concat(pinned.slice(i + 1))
      } else {
        pinned = pinned.concat([id])
      }
      this.pinnedConversationIds = pinned
      try {
        uni.setStorageSync(this.getPinnedStorageKey(), JSON.stringify(pinned))
      } catch (e) {}
      const getMsgTime = (t) => {
        if (t instanceof Date) return t.getTime()
        if (typeof t === 'number') return t
        const d = util.parseDate(t)
        return d ? d.getTime() : 0
      }
      this.sortConversationsWithPinned(this.conversations, getMsgTime)
      this.$set(this.swipeTranslatePx, index, 0)
    },
    // 将接口 data 统一成数组（兼容 data 直接为数组或 { records/list }）
    normalizeConversationArray(data) {
      if (!data) return []
      if (Array.isArray(data)) return data
      if (Array.isArray(data.records)) return data.records
      if (Array.isArray(data.list)) return data.list
      return []
    },

    // 加载会话列表；silent 为 true 时不显示全局「加载中」（用于 onShow / 定时刷新）
    async loadConversations(refresh = false, silent = false) {
      if (this.loading) return
      
      if (refresh) {
        this.page = 1
        this.hasMore = true
        // 注意：不要在请求前清空 conversations。若请求失败（网络/401/后端错误），
        // 清空会导致「原来有会话，刷新后全没了」且 catch 里无法恢复。
      }
      
      this.loading = true
      const showLoading = !silent
      
      try {
        const msgRes = await api.getConversationList(this.currentUserId, showLoading)
        
        let conversationList = []
        if ((msgRes.code === 0 || msgRes.code === 200) && msgRes.data != null) {
          conversationList = this.normalizeConversationArray(msgRes.data)
        }
        
        const msgConvList = conversationList
              .filter(conv => !this.isMerchantServiceConversation(conv))
              .filter(conv => {
                if (!conv) return false
                const oid = conv.otherUserId
                if (oid !== undefined && oid !== null && String(oid) !== '') return true
                return !!(conv.conversationId && String(conv.conversationId).trim())
              })
              .map(async (conv) => {
                // 如果是客服消息，使用客服信息
                if (conv.isService) {
                  const isPlatform = conv.isPlatform || (conv.otherUserId && String(conv.otherUserId).startsWith('platform_'))
                  const isOutlet = conv.isOutlet || (conv.otherUserId && String(conv.otherUserId).startsWith('outlet_'))
                  conv.isPlatform = isPlatform
                  conv.isOutlet = isOutlet
                  let storeId = conv.storeId
                  if (!storeId && conv.otherUserId) {
                    const str = String(conv.otherUserId)
                    const parts = str.split('_')
                    if (isPlatform && parts[0] === 'platform') {
                      storeId = 0
                    } else if (isOutlet && parts[0] === 'outlet' && parts.length >= 2) {
                      storeId = parseInt(parts[1]) || parts[1]
                    } else if (parts.length >= 2 && parts[0] === 'service') {
                      storeId = parseInt(parts[1]) || parts[1]
                    }
                  }
                  conv.storeId = storeId
                  conv.otherUserId = conv.otherUserId || (isPlatform ? 'platform_0' : (isOutlet ? 'outlet_' + storeId : 'service_' + storeId))
                  
                  // 平台/门店客服由后端已返回；仅商品店铺需额外拉取
                  if (storeId && !conv.isOutlet && !conv.isPlatform) {
                    try {
                      const storeRes = await api.request({
                        url: `/api/store/info`,
                        method: 'GET',
                        data: { storeId: storeId }
                      })
                      if (storeRes && (storeRes.code === 200 || storeRes.code === 0) && storeRes.data) {
                        // 使用店铺的真实名称
                        conv.otherUserName = storeRes.data.name || storeRes.data.storeName || conv.storeName || conv.otherUserName || '店铺客服'
                        // 使用店铺的头像
                        if (storeRes.data.avatar || storeRes.data.storeAvatar) {
                          conv.otherUserAvatar = storeRes.data.avatar || storeRes.data.storeAvatar
                        }
                      }
                    } catch (e) {
                      // 如果获取失败，使用默认值
                    }
                  }
                  
                  // 优先使用后端返回的 otherUserName，其次使用 storeName，最后使用默认值
                  conv.otherUserName = conv.otherUserName || conv.storeName || (conv.isPlatform ? '宠物之家客服' : (conv.isOutlet ? '门店客服' : '店铺客服'))
                  
                  // 平台客服一律使用品牌图 宠物之家.png（覆盖后端可能返回的其它头像）；店铺/门店用后端或默认 客服.png
                  if (conv.isPlatform) {
                    conv.otherUserAvatar = '/static/images/宠物之家.png'
                  } else {
                    const defaultAvatar = '/static/images/客服.png'
                    let avatarUrl = conv.otherUserAvatar || conv.storeAvatar || defaultAvatar
                    if (avatarUrl && avatarUrl.includes('customer-service.svg')) avatarUrl = defaultAvatar
                    if (avatarUrl && !avatarUrl.startsWith('http') && !avatarUrl.startsWith('/')) avatarUrl = '/' + avatarUrl
                    conv.otherUserAvatar = avatarUrl
                  }
                  conv.conversationId = conv.conversationId || (conv.isPlatform ? 'platform_' + (conv.userId || '') : (conv.isOutlet ? 'outlet_' + storeId : 'service_' + storeId))
                  conv.lastMessage = conv.lastMessage || '暂无消息'
                  conv.lastMessageTime = conv.lastMessageTime || new Date()
                  conv.unreadCount = Number(conv.unreadCount) || 0
                  return conv
                }
                
                // 确保基本字段存在（普通私聊必须有对方 id；仅有 conversationId 时无法拉资料，仍展示会话）
                if (conv.otherUserId === undefined || conv.otherUserId === null || String(conv.otherUserId) === '') {
                  if (conv.conversationId) {
                    conv.otherUserName = conv.otherUserName || '会话'
                    conv.otherUserAvatar = conv.otherUserAvatar || ''
                    conv.conversationId = conv.conversationId || ''
                    conv.lastMessage = conv.lastMessage || '暂无消息'
                    conv.lastMessageTime = conv.lastMessageTime || new Date()
                    conv.unreadCount = Number(conv.unreadCount) || 0
                    return conv
                  }
                  return null
                }

                // 获取对方用户信息
                try {
                  const userRes = await api.getCurrentUser({ userId: conv.otherUserId })
                  if (userRes && (userRes.code === 0 || userRes.code === 200) && userRes.data) {
                    conv.otherUserName = userRes.data.nickname || userRes.data.username || '用户'
                    conv.otherUserAvatar = userRes.data.avatar || ''
                  } else {
                    // 如果获取用户信息失败，使用默认值
                    conv.otherUserName = conv.otherUserName || '用户'
                    conv.otherUserAvatar = conv.otherUserAvatar || ''
                  }
                } catch (e) {
                  console.error('获取用户信息失败:', e)
                  // 使用默认值
                  conv.otherUserName = conv.otherUserName || '用户'
                  conv.otherUserAvatar = conv.otherUserAvatar || ''
                }
                
                // 确保所有必要字段都有默认值
                conv.conversationId = conv.conversationId || ''
                conv.lastMessage = conv.lastMessage || '暂无消息'
                conv.lastMessageTime = conv.lastMessageTime || new Date()
                conv.unreadCount = Number(conv.unreadCount) || 0
                
                return conv
              })
        
        const detailedMsgConvs = await Promise.all(msgConvList)
        const filteredMsg = detailedMsgConvs.filter(conv => conv !== null)
        const allConvs = [...filteredMsg]
        // 使用 util.parseDate 兼容 iOS（不支持 "yyyy-MM-dd HH:mm:ss" 需转为 ISO）
        const getMsgTime = (t) => {
          if (t instanceof Date) return t.getTime()
          if (typeof t === 'number') return t
          const d = util.parseDate(t)
          return d ? d.getTime() : 0
        }
        const deletedSet = new Set((this.deletedConversationIds || []).map(String))
        const filtered = allConvs.filter(c => {
          const id = c.conversationId || c.otherUserId || ''
          return id && !deletedSet.has(String(id))
        })
        // 兜底恢复：若后端有会话但被本地「已删除列表」全部过滤，则自动清空本地过滤并恢复显示
        if (allConvs.length > 0 && filtered.length === 0 && deletedSet.size > 0) {
          this.deletedConversationIds = []
          try {
            uni.removeStorageSync(this.getDeletedStorageKey())
            // 兼容历史全局 key，避免旧缓存继续影响
            uni.removeStorageSync('message_deleted_ids')
          } catch (e) {}
          this.sortConversationsWithPinned(allConvs, getMsgTime)
          this.conversations = allConvs
          uni.showToast({
            title: '已恢复消息会话',
            icon: 'none'
          })
        } else {
          this.sortConversationsWithPinned(filtered, getMsgTime)
          this.conversations = filtered
        }
        this.lastConversationsLoadTime = Date.now()
        this.calculateUnreadCount()
        this.updateTabBarBadge()
        const u = this.currentUserId
        if (u != null && u !== '' && u !== 'undefined' && Number.isFinite(typeof u === 'number' ? u : Number(u))) {
          this.loadCategoryUnreadCounts()
        }
      } catch (error) {
        console.error('加载会话列表失败:', error)
        const msg = (error && error.message) ? String(error.message) : ''
        if (!msg.includes('未授权') && !msg.includes('重新登录') && !msg.includes('认证失败')) {
          uni.showToast({
            title: '消息列表加载失败，请下拉重试',
            icon: 'none',
            duration: 2500
          })
        }
      } finally {
        this.loading = false
      }
    },
    
    // 加载更多
    loadMoreConversations() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadConversations()
    },
    
    // 计算未读数
    calculateUnreadCount() {
      let totalUnread = 0
      this.conversations.forEach(conv => {
        totalUnread += this.getUnreadCount(conv)
      })
    },

    // 加载赞/收藏、新增关注、评论和@ 的未读条数（别人对我的；传入上次查看时间则只统计之后的）
    loadCategoryUnreadCounts() {
      const uid = this.currentUserId
      if (uid == null || uid === '' || uid === 'undefined' || (typeof uid === 'string' && isNaN(Number(uid)))) return
      const numUid = typeof uid === 'number' ? uid : Number(uid)
      if (!Number.isFinite(numUid)) return

      // 止血：清缓存后 lastSeen*At 会丢失，导致后端把“从很久以前开始的全部”当未读
      // 默认改为“当前时间”，保证你重新登录/清缓存后不会莫名出现角标
      const getLastSeenOrNow = (key) => {
        const raw = uni.getStorageSync(key)
        const n = Number(raw)
        if (Number.isFinite(n) && n > 0) return n
        return Date.now()
      }
      const lastSeenLikes = getLastSeenOrNow('message_lastSeenLikesAt')
      const lastSeenFollows = getLastSeenOrNow('message_lastSeenFollowsAt')
      const lastSeenComments = getLastSeenOrNow('message_lastSeenCommentsAt')
      Promise.all([
        api.request({ url: '/api/messages/likes', method: 'GET', data: { userId: numUid, page: 1, size: 1, lastSeenAt: lastSeenLikes || undefined }, showLoading: false }),
        api.request({ url: '/api/messages/follows', method: 'GET', data: { userId: numUid, page: 1, size: 1, lastSeenAt: lastSeenFollows || undefined }, showLoading: false }),
        api.request({ url: '/api/messages/comments', method: 'GET', data: { userId: numUid, page: 1, size: 1, lastSeenAt: lastSeenComments || undefined }, showLoading: false })
      ]).then(([likesRes, followsRes, commentsRes]) => {
        this.unreadCounts.likes = (likesRes && likesRes.data && likesRes.data.total != null) ? likesRes.data.total : 0
        this.unreadCounts.follows = (followsRes && followsRes.data && followsRes.data.total != null) ? followsRes.data.total : 0
        this.unreadCounts.comments = (commentsRes && commentsRes.data && commentsRes.data.total != null) ? commentsRes.data.total : 0
      }).catch(() => {})
    },

    // 加载“系统通知”（展示在消息列表最上方）
    async loadSystemNotices() {
      if (!this.currentUserId) return
      try {
        const [noticeRes, notifRes] = await Promise.all([
          api.getUnreadNotices(this.systemNoticeLimit, false),
          api.getMyNotifications(1, this.systemNoticeLimit, false)
        ])

        const unreadNotices = (noticeRes && (noticeRes.code === 0 || noticeRes.code === 200) && Array.isArray(noticeRes.data))
          ? noticeRes.data : []
        const notifList = (notifRes && notifRes.data && Array.isArray(notifRes.data.list)) ? notifRes.data.list : []
        const unreadNotifList = notifList.filter(n => {
          const t = n && n.type ? String(n.type) : ''
          const unread = n && (n.status === 0 || n.status === '0' || n.status === 'unread')
          return unread && !['community_like', 'community_comment', 'community_follow'].includes(t)
        })

        // 外层“系统通知”行的未读数与详情页保持一致：未读公告 + 未读系统通知（_src 用于区分公告/通知，避免误过滤）
        this.systemNotices = [
          ...unreadNotifList.map((n) => ({ ...n, _src: 'notification' })),
          ...unreadNotices.map((n) => ({ ...n, _src: 'notice' }))
        ]
      } catch (e) {
        this.systemNotices = []
      }
    },

    // 点击“系统通知”这一行：标记该条未读公告为已读并进入系统公告列表
    async onSystemNoticesRowTap() {
      // 第一优先级：确保能跳转进入系统通知页
      // “标记已读”逻辑放在 user/notice.vue 的 bootstrapPage() 内统一处理，避免点开时打断跳转
      uni.navigateTo({
        url: '/user/notice',
        fail: (err) => {
          const errMsg =
            (err && (err.errMsg || err.message)) ||
            (typeof err === 'string' ? err : '')
          uni.showToast({
            title: errMsg ? `跳转失败:${errMsg}` : '跳转失败，请检查页面配置',
            icon: 'none',
            duration: 2000
          })
          console.error('navigateTo /user/notice failed:', err)
        }
      })
    },
    
    // 更新 tabBar 未读数
    updateTabBarBadge() {
      let totalUnread = 0
      this.conversations.forEach(conv => {
        totalUnread += this.getUnreadCount(conv)
      })
      // 系统通知也算进消息未读角标（与聊天列表保持一致体验）
      totalUnread += this.systemUnreadCount || 0
      
      if (totalUnread > 0) {
        uni.setTabBarBadge({
          index: 3, // 消息标签页的索引（首页0，商城1，社区2，消息3，我的4）
          text: totalUnread > 99 ? '99+' : totalUnread.toString()
        })
      } else {
        uni.removeTabBarBadge({
          index: 3
        })
      }
      
      // 触发全局事件，通知其他页面更新未读数
      uni.$emit('unreadCountUpdated', totalUnread)
    },
    
    // 左滑相关：删除按钮宽度约 60px
    onSwipeStart(e, index) {
      this.touchStartX = e.touches[0].clientX
      this.touchStartIndex = index
    },
    onSwipeMove(e, index) {
      if (this.touchStartIndex !== index) return
      const currentX = e.touches[0].clientX
      const delta = currentX - this.touchStartX
      const maxSwipePx = -100
      const px = delta < 0 ? Math.max(delta, maxSwipePx) : 0
      this.$set(this.swipeTranslatePx, index, px)
    },
    onSwipeEnd(e, index) {
      if (this.touchStartIndex !== index) return
      const current = this.swipeTranslatePx[index] || 0
      const open = current < -40
      this.$set(this.swipeTranslatePx, index, open ? -100 : 0)
      this.touchStartIndex = null
    },
    onConversationTap(index) {
      if ((this.swipeTranslatePx[index] || 0) < -20) {
        this.$set(this.swipeTranslatePx, index, 0)
        return
      }
      this.goToChatByIndex({ currentTarget: { dataset: { index } } })
    },
    deleteConversation(index) {
      const conv = this.conversations[index]
      if (!conv) return
      uni.showModal({
        title: '提示',
        content: '确定删除该会话吗？删除后将同步到服务端，清缓存后也不会再显示。',
        success: async (res) => {
          if (res.confirm) {
            const id = conv.conversationId || conv.otherUserId || ''
            const uid = this.currentUserId
            const numUid = typeof uid === 'number' ? uid : Number(uid)
            if (id && Number.isFinite(numUid)) {
              try {
                await api.hideConversation(numUid, String(id), false)
              } catch (e) {
                console.warn('hideConversation failed', e)
                uni.showToast({
                  title: '同步失败仍可本地隐藏，请检查网络或执行库表脚本',
                  icon: 'none',
                  duration: 2800
                })
              }
            }
            if (id) {
              let pinned = (this.pinnedConversationIds || []).filter(x => x !== id)
              this.pinnedConversationIds = pinned
              try {
                uni.setStorageSync(this.getPinnedStorageKey(), JSON.stringify(pinned))
              } catch (e) {}
              const deleted = [...(this.deletedConversationIds || []), id]
              this.deletedConversationIds = deleted
              try {
                uni.setStorageSync(this.getDeletedStorageKey(), JSON.stringify(deleted))
              } catch (e) {}
            }
            this.conversations.splice(index, 1)
            this.$delete(this.swipeTranslatePx, index)
            this.updateTabBarBadge()
          }
        }
      })
    },
    // 通过索引跳转到聊天页面（更可靠的方式）
    goToChatByIndex(e) {
      const index = e.currentTarget.dataset.index
      
      if (index === undefined || index === null) {
        console.error('无法获取索引:', e)
        uni.showToast({
          title: '会话信息错误',
          icon: 'none'
        })
        return
      }
      
      // 从数组中获取会话数据
      const conv = this.conversations[index]
      
      // 检查参数是否有效
      if (!conv) {
        console.error('会话数据无效:', conv, 'index:', index, 'conversations length:', this.conversations.length)
        uni.showToast({
          title: '会话信息错误',
          icon: 'none'
        })
        return
      }
      
      // 检查必要的字段
      if (!conv.otherUserId) {
        console.error('缺少用户ID:', conv)
        uni.showToast({
          title: '用户信息错误',
          icon: 'none'
        })
        return
      }
      
      // 注意：不再在这里标记为已读，而是在聊天页面加载时自动标记
      // 这样可以确保用户真正查看了消息才标记为已读
      
      const targetUserId = conv.otherUserId
      const targetUserName = conv.otherUserName || '用户'
      const isService = conv.isService || false
      const isOutlet = conv.isOutlet || false
      const isPlatform = conv.isPlatform || false
      const storeId = conv.storeId || null
      
      // 如果是客服消息，跳转到客服聊天页面
      if (isService) {
        const outletParam = isOutlet ? '&isOutlet=true' : ''
        const platformParam = isPlatform ? '&isPlatform=true' : ''
        // 平台客服走 customer-service 页（与 我的-在线客服 一致）
        if (isPlatform) {
          uni.navigateTo({
            url: '/chat/customer-service?isPlatform=true',
            fail: (err) => {
              console.error('跳转失败:', err)
              uni.showToast({ title: '跳转失败', icon: 'none' })
            }
          })
          return
        }
        uni.navigateTo({
          url: `/user/chat?targetUserId=${targetUserId}&targetUserName=${encodeURIComponent(targetUserName)}&isService=true&storeId=${storeId || ''}${outletParam}`,
          fail: (err) => {
            console.error('跳转失败:', err)
            uni.showToast({
              title: '跳转失败',
              icon: 'none'
            })
          }
        })
        return
      }
      
      uni.navigateTo({
        url: `/user/chat?targetUserId=${targetUserId}&targetUserName=${encodeURIComponent(targetUserName)}`,
        fail: (err) => {
          console.error('跳转失败:', err)
          uni.showToast({
            title: '跳转失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 标记为已读
    async markAsRead(conversationId) {
      try {
        // 这里可以调用后端接口标记消息为已读
        // await api.markMessagesAsRead({ conversationId, userId: this.currentUserId })
        
        // 更新本地状态
        const conv = this.conversations.find(c => c.conversationId === conversationId)
        if (conv) {
          conv.unreadCount = 0
          this.updateTabBarBadge()
        }
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    },
    
    // 切换分类
    switchCategory(type) {
      let url = ''
      switch(type) {
        case 'likes':
          url = '/pages-message/likes'
          break
        case 'follows':
          url = '/pages-message/follows'
          break
        case 'comments':
          url = '/pages-message/comments'
          break
        default:
          uni.showToast({
            title: '功能开发中',
            icon: 'none'
          })
          return
      }
      
      if (url) {
        uni.navigateTo({
          url: url
        })
      }
    },
    
    // 显示更多选项（通过导航栏右侧按钮触发）
    showMoreOptions() {
      uni.showActionSheet({
        itemList: ['清空消息', '设置'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.clearAllMessages()
          } else if (res.tapIndex === 1) {
            uni.navigateTo({
              url: '/pages/settings/index'
            })
          }
        }
      })
    },
    
    // 清空消息（当前列表全部加入「已删除」，刷新后也不再显示）
    clearAllMessages() {
      uni.showModal({
        title: '提示',
        content: '确定要清空所有消息吗？将同步隐藏所有会话，清缓存后也不再显示。',
        success: async (res) => {
          if (res.confirm) {
            const ids = this.conversations.map(c => c.conversationId || c.otherUserId).filter(Boolean)
            const uid = this.currentUserId
            const numUid = typeof uid === 'number' ? uid : Number(uid)
            if (ids.length && Number.isFinite(numUid)) {
              await Promise.all(
                ids.map((cid) =>
                  api.hideConversation(numUid, String(cid), false).catch(() => {})
                )
              )
            }
            if (ids.length) {
              const deleted = [...new Set([...(this.deletedConversationIds || []), ...ids])]
              this.deletedConversationIds = deleted
              try {
                uni.setStorageSync(this.getDeletedStorageKey(), JSON.stringify(deleted))
              } catch (e) {}
            }
            this.conversations = []
            this.loadSystemNotices().catch(() => {})
            uni.removeTabBarBadge({
              index: 3
            })
            uni.showToast({
              title: '已清空',
              icon: 'success'
            })
          }
        }
      })
    },
    
    // 启动自动刷新
    startAutoRefresh() {
      // 每30秒静默刷新一次，不显示全局「加载中」
      this.refreshTimer = setInterval(() => {
        if (this.currentUserId) {
          this.loadConversations(true, true)
        }
      }, 30000)
    },
    
    // 停止自动刷新
    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
    },
    
    // 获取头像URL（临时路径 __tmp__/tmp 无法访问，返回默认头像）
    getAvatarUrl(avatar) {
      if (!avatar) {
        return '/static/images/login-dog.png'
      }
      const s = String(avatar).trim()
      if (s.includes('__tmp__') || s.includes('/tmp/') || /^https?:\/\/[^/]*\/__tmp__\//.test(s) || /^https?:\/\/tmp\//.test(s)) {
        return '/static/images/login-dog.png'
      }
      // 小程序 image 对 SVG 支持不好，客服默认头像统一用 PNG
      if (s.includes('customer-service.svg')) {
        avatar = '/static/images/客服.png'
      } else {
        avatar = s
      }
      // 确保路径以 / 开头
      if (avatar && !avatar.startsWith('http') && !avatar.startsWith('/')) {
        avatar = '/' + avatar
      }
      return util.getImageUrl ? util.getImageUrl(avatar) : avatar
    },
    
    // 处理消息发送事件
    handleMessageSent(data) {
      // 刷新会话列表
      this.loadConversations(true)
    },
    
    // 处理消息已读事件
    handleMessagesRead(data) {
      // 更新对应会话的未读数
      const conv = this.conversations.find(c => 
        c.conversationId === data.conversationId || 
        c.otherUserId === data.targetUserId
      )
      
      if (conv) {
        // 将未读数设为0
        conv.unreadCount = 0
        // 更新 tabBar 未读数
        this.updateTabBarBadge()
      } else {
        // 如果找不到会话，刷新整个列表
        this.loadConversations(true)
      }
    },
    
    // 格式化时间
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
.message-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.message-categories {
  display: flex;
  justify-content: space-around;
  background-color: #fff;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  cursor: pointer;
}

.category-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  margin-bottom: 10rpx;
  
  &.likes-icon {
    background-color: #fff0f0;
  }
  
  &.follows-icon {
    background-color: #f0f5ff;
  }
  
  &.comments-icon {
    background-color: #f0fff0;
  }
}

.category-icon-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.category-text {
  font-size: 22rpx;
  color: #666;
}

.badge {
  position: absolute;
  top: 0;
  right: -8rpx;
  background-color: #ff4444;
  color: #fff;
  font-size: 18rpx;
  padding: 2rpx 6rpx;
  border-radius: 16rpx;
  min-width: 28rpx;
  text-align: center;
  line-height: 1.4;
}

.conversation-list {
  flex: 1;
  padding-top: 16rpx;
  height: calc(100vh - 260rpx);
}

// 系统通知列表（展示在消息列表最上方）
.system-notices {
  margin: 0 24rpx 8rpx;
  background-color: #ffffff;
  border-radius: 0;
  overflow: hidden;
}

.system-notice-header {
  padding: 18rpx 18rpx 10rpx;
  font-size: 26rpx;
  color: #111827;
  font-weight: 600;
}

.system-notice-item {
  display: flex;
  align-items: flex-start;
  padding: 18rpx 18rpx;
  background-color: transparent;
  border-bottom: 1rpx solid #f0f0f5;
}

.system-notice-icon {
  width: 26rpx;
  height: 26rpx;
  border-radius: 50%;
  background-color: #3b82f6;
  flex-shrink: 0;
  margin-top: 10rpx;
  margin-right: 18rpx;
}

.system-notice-body {
  flex: 1;
  min-width: 0;
}

.system-notice-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.system-notice-title {
  font-size: 28rpx;
  color: #111827;
  font-weight: 500;
}

.system-notice-time {
  font-size: 22rpx;
  color: #9ca3af;
  flex-shrink: 0;
}

.system-notice-snippet {
  font-size: 24rpx;
  color: #6b7280;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.conversation-swipe-wrap {
  overflow: hidden;
  width: 100%;
}

.conversation-swipe-inner {
  display: flex;
  width: calc(100% + 200rpx);
  transition: transform 0.15s ease;
}

.conversation-item {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:active {
    background-color: #f5f5f5;
  }
}

.conversation-item-pinned {
  background-color: #f0f0f0;
  &:active {
    background-color: #e8e8e8;
  }
}

.conversation-name-row {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  margin-right: 12rpx;
}

.role-tag {
  flex-shrink: 0;
  padding: 2rpx 12rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  line-height: 1.4;
  margin-left: 8rpx;
}

.role-tag-doctor {
  background-color: #e3f2fd;
  color: #1976d2;
}

.role-tag-service {
  background-color: #fff3e0;
  color: #e65100;
}

.conversation-pin-btn {
  flex: 0 0 80rpx;
  width: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #888;
  color: #fff;
  font-size: 24rpx;
}

.conversation-delete-btn {
  flex: 0 0 120rpx;
  width: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #dd524d;
  color: #fff;
  font-size: 28rpx;
}

.avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 20rpx;
  background-color: #e0e0e0;
  flex-shrink: 0;
}

.system-notice-avatar {
  background-color: #dbeafe;
}

.conversation-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.conversation-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-time {
  font-size: 22rpx;
  color: #999;
  margin-left: 16rpx;
  flex-shrink: 0;
}

.conversation-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.conversation-message {
  font-size: 24rpx;
  color: #666;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 10rpx;
}

.unread-badge {
  background-color: #ff4444;
  color: #fff;
  font-size: 18rpx;
  padding: 3rpx 10rpx;
  border-radius: 16rpx;
  min-width: 28rpx;
  text-align: center;
  line-height: 1.4;
  flex-shrink: 0;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 10rpx;
}

.empty-hint {
  font-size: 22rpx;
  color: #ccc;
}
</style>
