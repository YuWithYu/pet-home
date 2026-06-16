<template>
  <view class="chat-container">
    <scroll-view 
      class="message-list" 
      scroll-y 
      :scroll-into-view="scrollToView"
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
      @scrolltolower="loadMoreMessages"
      @scroll="handleScroll"
    >
      <view v-if="messages.length === 0 && !loading" class="empty-tip">
        <text class="empty-text">No messages yet</text>
      </view>
      <view class="message-wrapper" v-for="(msg, index) in messages" :key="msg.id || 'msg-' + index" :id="'msg-' + index" :data-index="index">
        <view v-if="shouldShowMessageTimestamp(msg, index)" class="message-timestamp">
          {{ getWeChatTimestamp(msg) }}
        </view>
        
        <view v-if="(isService && msg.senderId < 0) || (!isService && msg.senderId == targetUserId && msg.senderId != currentUserId)" class="message-item other-msg">
          <view class="avatar-wrap" @tap.stop="onAvatarClick(targetUserId)">
            <image class="avatar" :src="getTargetUserAvatar()" mode="aspectFill" />
          </view>
          <view class="message-content" :class="{ 'content-image-only': isMessageImageOnly(msg) }">
            <view class="message-bubble" :class="{ 'bubble-image-only': isMessageImageOnly(msg) }">
              <view class="message-text-wrapper">
                <block v-for="(part, pidx) in parseChatContent(msg.content)" :key="'m' + index + '-p' + pidx">
                  <text v-if="part.type === 'text'" class="message-text">{{ part.text }}</text>
                  <image v-else-if="part.type === 'image'" class="message-image-inline" :src="getImageUrl(part.url)" mode="widthFix" />
                </block>
              </view>
            </view>
          </view>
        </view>

        <view v-else-if="msg.senderId == currentUserId" class="message-item my-msg">
          <view class="message-content" :class="{ 'content-image-only': isMessageImageOnly(msg) }">
            <view class="message-bubble" :class="{ 'bubble-image-only': isMessageImageOnly(msg) }">
              <view class="message-text-wrapper">
                <block v-for="(part, pidx) in parseChatContent(msg.content)" :key="'m' + index + '-p' + pidx">
                  <text v-if="part.type === 'text'" class="message-text">{{ part.text }}</text>
                  <image v-else-if="part.type === 'image'" class="message-image-inline" :src="getImageUrl(part.url)" mode="widthFix" />
                </block>
              </view>
            </view>
          </view>
          <view class="avatar-wrap" @tap.stop="onAvatarClick(currentUserId)">
            <image class="avatar" :src="getUserAvatar()" mode="aspectFill" />
          </view>
        </view>
      </view>

      <view id="msg-bottom" style="height: 1rpx;"></view>
    </scroll-view>

    <view class="input-bar">
      <view class="input-bar-row">
        <view class="input-wrapper">
          <input 
            class="message-input"
            v-model="inputMessage"
            :placeholder="ui.inputPlaceholder"
            confirm-type="send"
            :adjust-position="true"
            @confirm="sendMessage"
            maxlength="500"
          />
          <image class="input-action-icon" :src="chatAttachmentIcon" mode="aspectFit" @click="chooseChatImage"></image>
        </view>
        <button class="send-button" @tap="sendMessage" :disabled="!inputMessage.trim() && !chatImages.length">
          {{ ui.send }}
        </button>
      </view>
      <view v-if="chatImages.length" class="chat-images-row">
        <view class="chat-image-item" v-for="(img, idx) in chatImages" :key="'img-' + idx">
          <image class="chat-image-thumb" :src="img" mode="aspectFill" />
          <view class="chat-image-del" @click="removeChatImage(idx)">{{ ui.remove }}</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'
export default {
  name: 'UserChat',
  data() {
    return {
      targetUserId: null,
      targetUserName: '',
      targetUserAvatar: '',
      isService: false,
      isPlatform: false,
      isOutlet: false,
      storeId: null,
      messages: [],
      inputMessage: '',
      scrollToView: '',
      scrollTop: 0,
      inputHeight: 72,
      currentUserId: null,
      page: 1,
      pageSize: 20,
      hasMore: true,
      loading: false,
      messagePollingTimer: null,
      lastMessageId: null,
      isPolling: false,
      chatImages: [],
      inputCheckTimer: null,
      inputCursor: 0,
      chatAttachmentIcon: '/static/images/\u56fe\u7247.png',
      ui: {
        inputPlaceholder: '\u8bf7\u8f93\u5165\u6d88\u606f...',
        send: '\u53d1\u9001',
        remove: '\u00d7',
        platformService: '\u5e73\u53f0\u5ba2\u670d',
        outletService: '\u95e8\u5e97\u5ba2\u670d',
        storeService: '\u5e97\u94fa\u5ba2\u670d',
        errOutletInfo: '\u7f3a\u5c11\u95e8\u5e97\u4fe1\u606f',
        errStoreInfo: '\u7f3a\u5c11\u5e97\u94fa\u4fe1\u606f',
        errOpenChat: '\u53c2\u6570\u9519\u8bef\uff0c\u65e0\u6cd5\u6253\u5f00\u804a\u5929',
        pleaseLogin: '\u8bf7\u5148\u767b\u5f55',
        titleChat: '\u804a\u5929',
        defaultStoreName: '\u5ba2\u670d',
        defaultUser: '\u7528\u6237',
        uploading: '\u4e0a\u4f20\u4e2d...',
        uploadFail: '\u4e0a\u4f20\u5931\u8d25',
        sendFail: '\u53d1\u9001\u5931\u8d25',
        maxImages: '\u6700\u591a9\u5f20\u56fe\u7247',
        sheetCamera: '\u62cd\u7167',
        sheetAlbum: '\u4ece\u76f8\u518c\u9009\u62e9',
        voiceDeveloping: '\u8bed\u97f3\u8f93\u5165\u529f\u80fd\u5f00\u53d1\u4e2d',
        moreDeveloping: '\u66f4\u591a\u529f\u80fd\u5f00\u53d1\u4e2d',
        serviceIdError: '\u670d\u52a1ID\u683c\u5f0f\u9519\u8bef'
      }
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn']),
    safeInputValue() {
      let val = this.inputMessage
      
      if (val === null || val === undefined || val === true || val === false) {
        if (this.inputMessage !== '') {
          this.$set(this, 'inputMessage', '')
        }
        return ''
      }
      
      val = String(val)
      
      if (val === 'true' || val === 'false') {
        if (this.inputMessage !== '') {
          this.$set(this, 'inputMessage', '')
        }
        return ''
      }
      
      const trimmed = val.trim()
      if (trimmed === 'true' || trimmed === 'false') {
        if (this.inputMessage !== '') {
          this.$set(this, 'inputMessage', '')
        }
        return ''
      }
      
      return val
    }
  },

  watch: {
    inputMessage: {
      handler(newVal, oldVal) {
        if (typeof newVal !== 'string') {
          this.$set(this, 'inputMessage', '')
          this.$forceUpdate()
          return
        }
        
        if (newVal === 'true' || newVal === 'false') {
          this.$set(this, 'inputMessage', '')
          this.$forceUpdate()
          return
        }
      },
      immediate: true,
      deep: false
    }
  },

  onLoad(options) {
    this.$set(this, 'inputMessage', '')
    
    if (options.targetUserId) {
      const tid = options.targetUserId.toString()
      if (tid.startsWith('platform_')) {
        this.isService = true
        this.isPlatform = true
        this.storeId = 0
        this.targetUserId = tid
        this.targetUserName = this.targetUserName || this.ui.platformService
      } else if (tid.startsWith('outlet_')) {
        this.isService = true
        this.isOutlet = true
        const idStr = tid.replace('outlet_', '')
        const num = parseInt(idStr)
        if (!isNaN(num)) this.storeId = num
        this.targetUserId = tid
      } else if (tid.startsWith('service_')) {
        this.isService = true
        const serviceIdStr = tid.replace('service_', '')
        try {
          this.storeId = parseInt(serviceIdStr)
          this.targetUserId = options.targetUserId
        } catch (e) {
          console.error(this.ui.serviceIdError, e)
        }
      } else {
        this.targetUserId = parseInt(options.targetUserId)
      }
    }
    if (options.targetUserName) {
      this.targetUserName = decodeURIComponent(options.targetUserName)
    }
    if (options.isService === 'true' || options.isService === true) {
      this.isService = true
    }
    if (options.storeId) {
      this.storeId = parseInt(options.storeId)
    }
    if (options.isOutlet === 'true' || options.isOutlet === true) {
      this.isOutlet = true
    }
    if (options.isPlatform === 'true' || options.isPlatform === true) {
      this.isPlatform = true
    }
    
    this.currentUserId = this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId') || null
    
    if (this.isService && !this.targetUserId) {
      if (this.isPlatform) {
        this.targetUserId = 'platform_0'
      } else if (this.storeId) {
        this.targetUserId = this.isOutlet ? `outlet_${this.storeId}` : `service_${this.storeId}`
      }
    }
    
    if (this.isService && !this.isPlatform) {
      if (!this.storeId) {
        uni.showToast({
          title: this.isOutlet ? this.ui.errOutletInfo : this.ui.errStoreInfo,
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
        return
      }
      if (!this.targetUserId) {
        this.targetUserId = `service_${this.storeId}`
      }
    } else {
      if (!this.targetUserId) {
        uni.showToast({
          title: this.ui.errOpenChat,
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
        return
      }
    }
    
    if (this.isService) {
      if (!this.targetUserName) {
        this.targetUserName = this.isPlatform ? this.ui.platformService : (this.isOutlet ? this.ui.outletService : this.ui.storeService)
      }
      if (!this.targetUserAvatar) {
        this.targetUserAvatar = '/static/images/customer-service.svg'
      }
    }
    
    if (!this.currentUserId) {
      uni.showToast({
        title: this.ui.pleaseLogin,
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages-auth/login'
        })
      }, 1500)
      return
    }
    
    uni.setNavigationBarTitle({
      title: this.targetUserName || this.ui.titleChat
    })
    
    this.loadTargetUserInfo()
    this.loadChatHistory()
    this.markAllMessagesAsRead()
  },
  
  onShow() {
    if (this.targetUserId && this.currentUserId) {
      this.markAllMessagesAsRead()
    }

    this.forceClearInvalidInput()

    this.startMessagePolling()
  },
  
  onHide() {
    this.stopMessagePolling()
  },
  
  onUnload() {
  },
  
  mounted() {
    this.forceClearInvalidInput()
    this.inputCheckTimer = setInterval(() => {
      const val = this.inputMessage
      if (val === 'true' || val === 'false' || val === true || val === false || typeof val !== 'string') {
        this.$set(this, 'inputMessage', '')
        this.$forceUpdate()
      }
    }, 50)
  },
  
  beforeDestroy() {
    if (this.inputCheckTimer) {
      clearInterval(this.inputCheckTimer)
      this.inputCheckTimer = null
    }
    this.stopMessagePolling()
  },
  
  updated() {
    this.forceClearInvalidInput()
  },

  methods: {
    async loadTargetUserInfo() {
      if (this.isService) {
        try {
          const conversationId = this.getServiceConversationId()
          const res = await api.getPrivateMessages({
            userId: this.currentUserId,
            conversationId: conversationId,
            page: 1,
            size: 1
          })
          
          if ((res.code === 0 || res.code === 200) && res.data && res.data.storeInfo) {
            const storeInfo = res.data.storeInfo
            this.targetUserName = storeInfo.storeName || this.ui.defaultStoreName
            this.targetUserAvatar = storeInfo.storeAvatar || '/static/images/customer-service.svg'
            uni.setNavigationBarTitle({
              title: this.targetUserName
            })
          }
        } catch (error) {
          console.error('获取服务信息失败:', error)
        }
        return
      }
      
      try {
        const res = await api.getCurrentUser({ userId: this.targetUserId })
        if ((res.code === 0 || res.code === 200) && res.data) {
          this.targetUserName = res.data.nickname || res.data.username || this.ui.defaultUser
          this.targetUserAvatar = res.data.avatar || ''
          uni.setNavigationBarTitle({
            title: this.targetUserName
          })
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
    },

    async loadChatHistory() {
      if (this.loading) return
      this.loading = true
      
      try {
        let res
        if (this.isService) {
          const conversationId = this.getServiceConversationId()
          res = await api.getPrivateMessages({
            userId: this.currentUserId,
            conversationId: conversationId,
            page: this.page,
            size: this.pageSize
          })
        } else {
          res = await api.getPrivateMessages({
            userId: this.currentUserId,
            targetUserId: this.targetUserId,
            page: this.page,
            size: this.pageSize
          })
        }
        
        if ((res.code === 0 || res.code === 200) && res.data) {
          if (this.isService && res.data.storeInfo) {
            const storeInfo = res.data.storeInfo
            this.targetUserName = storeInfo.storeName || this.targetUserName
            this.targetUserAvatar = storeInfo.storeAvatar || this.targetUserAvatar
            uni.setNavigationBarTitle({
              title: this.targetUserName
            })
          }
          
          const newMessages = res.data.messages || []

          const formattedMessages = newMessages.map(msg => {
            const senderId = msg.senderId || msg.fromId
            const createTime = msg.createTime || msg.create_time || null
            let displayTime = msg.time || ''
            if (!displayTime && createTime) {
              displayTime = this.formatTime(createTime)
            }
            return {
              id: msg.id,
              senderId: senderId,
              receiverId: msg.receiverId || msg.toId,
              content: msg.content,
              createTime: createTime,
              time: displayTime
            }
          }).filter(msg => msg && msg.content)

          if (this.page === 1) {
            this.messages = formattedMessages.reverse()
            if (this.messages.length > 0) {
              this.lastMessageId = this.messages[this.messages.length - 1].id
            }
          } else {
            this.messages = [...formattedMessages.reverse(), ...this.messages]
          }

          this.hasMore = formattedMessages.length >= this.pageSize

          if (this.page === 1) {
            this.$nextTick(() => {
              this.scrollToBottom()
            })
          }
        }
      } catch (error) {
        console.error('加载聊天记录失败:', error)
      } finally {
        this.loading = false
      }
    },

    loadMoreMessages() {
      if (!this.hasMore || this.loading) return
      this.page++
      this.loadChatHistory()
    },

    async sendMessage() {
      const text = this.inputMessage ? this.inputMessage.trim() : ''
      const hasImages = this.chatImages.length > 0
      if (!text && !hasImages) return

      let urls = []
      if (hasImages) {
        uni.showLoading({ title: this.ui.uploading, mask: true })
        for (let i = 0; i < this.chatImages.length; i++) {
          const url = await this.uploadOneChatImage(this.chatImages[i])
          if (url) urls.push(url)
        }
        uni.hideLoading()
        if (!urls.length && !text) {
          uni.showToast({ title: this.ui.uploadFail, icon: 'none' })
          return
        }
      }

      const contentsToSend = []
      if (text) contentsToSend.push(text)
      if (urls.length) contentsToSend.push(urls.map(u => '[\u56fe]' + u).join('\n'))

      this.inputMessage = ''
      this.chatImages = []

      const now = new Date()
      const tempIds = []

      for (let i = 0; i < contentsToSend.length; i++) {
        const content = contentsToSend[i]
        const tempMessage = {
          id: 'temp-' + Date.now() + '-' + i,
          senderId: this.currentUserId,
          receiverId: this.targetUserId,
          content: content,
          createTime: now.toISOString(),
          time: this.formatTime(now.toISOString())
        }
        this.messages.push(tempMessage)
        tempIds.push(tempMessage.id)
      }

      this.scrollToBottom()

      const conversationId = this.isService ? this.getServiceConversationId() : null
      const receiverId = this.isService ? (this.isPlatform ? -9999 : -this.storeId) : this.targetUserId
      let lastSentContent = contentsToSend[contentsToSend.length - 1]
      let failed = false

      for (let i = 0; i < contentsToSend.length; i++) {
        const content = contentsToSend[i]
        const tempId = tempIds[i]
        try {
          const res = this.isService
            ? await api.sendPrivateMessage({
                senderId: this.currentUserId,
                receiverId: receiverId,
                content: content,
                conversationId: conversationId
              })
            : await api.sendPrivateMessage({
                senderId: this.currentUserId,
                receiverId: this.targetUserId,
                content: content
              })

          if ((res.code === 0 || res.code === 200) && res.data) {
            const index = this.messages.findIndex(m => m.id === tempId)
            if (index !== -1) {
              if (res.data.id) {
                this.$set(this.messages[index], 'id', res.data.id)
                this.lastMessageId = res.data.id
              }
              if (res.data.createTime) {
                this.$set(this.messages[index], 'createTime', res.data.createTime)
                this.$set(this.messages[index], 'time', this.formatTime(res.data.createTime))
              }
            }
          } else {
            failed = true
            break
          }
        } catch (e) {
          console.error('发送消息失败:', e)
          failed = true
          break
        }
      }

      if (failed) {
        tempIds.forEach(id => {
          const index = this.messages.findIndex(m => m.id === id)
          if (index !== -1) this.messages.splice(index, 1)
        })
        uni.showToast({ title: this.ui.sendFail, icon: 'none' })
        return
      }

      uni.$emit('messageSent', { targetUserId: this.targetUserId, content: lastSentContent })
      setTimeout(() => this.checkNewMessages(), 500)
    },

    handleLineChange(e) {
      const lineCount = e.detail.lineCount || 1
      const maxHeight = 4 * 36 + 20
      const minHeight = 72
      const calculatedHeight = Math.min(Math.max(lineCount * 36 + 20, minHeight), maxHeight)
      this.inputHeight = calculatedHeight
    },
    
    handleInputBlur(e) {
      let value = e.detail.value
      
      if (value === null || value === undefined || value === true || value === false) {
        this.$set(this, 'inputMessage', '')
        this.inputCursor = 0
        this.$forceUpdate()
        return
      }
      
      value = String(value)
      
      if (value === 'true' || value === 'false') {
        this.$set(this, 'inputMessage', '')
        this.inputCursor = 0
        this.$forceUpdate()
        return
      }
      
      this.inputCursor = e.detail.cursor !== undefined && e.detail.cursor !== null ? e.detail.cursor : value.length
    },
    
    forceClearInvalidInput() {
      const val = this.inputMessage
      if (val === 'true' || val === 'false' || val === true || val === false || typeof val !== 'string') {
        this.$set(this, 'inputMessage', '')
        this.$nextTick(() => {
          this.$forceUpdate()
        })
      }
    },
    
    handleInput(e) {
      let value = e.detail.value
      let cursor = e.detail.cursor
      if (cursor === undefined || cursor === null) {
        cursor = value ? value.length : 0
      }

      if (value === null || value === undefined || value === true || value === false) {
        value = ''
        cursor = 0
      } else {
        value = String(value)
      }

      if (value === 'true' || value === 'false') {
        value = ''
        cursor = 0
      }

      const trimmed = value.trim()
      if (trimmed === 'true' || trimmed === 'false') {
        value = ''
        cursor = 0
      }

      const oldValue = this.inputMessage || ''
      if (oldValue && value.length < oldValue.length) {
        const deletedLength = oldValue.length - value.length
        const newCursor = cursor
        const beforeCursor = value.substring(0, newCursor)
        const afterCursor = value.substring(newCursor)
        const fullEmojiMatch = beforeCursor.match(/\[([^\]]+)\]$/)
        if (fullEmojiMatch && deletedLength === 1) {
          const emojiStart = beforeCursor.lastIndexOf('[')
          const beforeEmoji = beforeCursor.substring(0, emojiStart)
          value = beforeEmoji + afterCursor
          this.inputCursor = beforeEmoji.length
        } else {
          const partialStartMatch = beforeCursor.match(/\[([^\]]*)$/)
          const partialEndMatch = afterCursor.match(/^([^\]]*)\]/)
          if (partialStartMatch || partialEndMatch) {
            let emojiStart = -1
            let emojiEnd = -1
            if (partialStartMatch) {
              emojiStart = beforeCursor.lastIndexOf('[')
            } else {
              const beforeText = oldValue.substring(0, newCursor + deletedLength)
              emojiStart = beforeText.lastIndexOf('[')
            }
            if (partialEndMatch) {
              emojiEnd = newCursor + partialEndMatch[0].length
            } else {
              const afterText = oldValue.substring(newCursor + deletedLength)
              const endMatch = afterText.match(/^([^\]]*)\]/)
              if (endMatch) {
                emojiEnd = newCursor + deletedLength + endMatch[0].length
              }
            }
            if (emojiStart >= 0 && emojiEnd > emojiStart) {
              const beforeEmoji = oldValue.substring(0, emojiStart)
              const afterEmoji = oldValue.substring(emojiEnd)
              value = beforeEmoji + afterEmoji
              this.inputCursor = emojiStart
            } else {
              this.inputCursor = cursor
            }
          } else {
            this.inputCursor = cursor
          }
        }
      } else {
        this.inputCursor = cursor
      }

      this.$set(this, 'inputMessage', value)

      this.$nextTick(() => {
        if (this.inputCursor > value.length) {
          this.inputCursor = value.length
        }
        this.$forceUpdate()
      })

      if (value === 'true' || value === 'false') {
        setTimeout(() => {
          this.$set(this, 'inputMessage', '')
          this.inputCursor = 0
          this.$forceUpdate()
        }, 0)
      }
    },

    handleKeyboardHeightChange() {
    },

    getCurrentTime() {
      const now = new Date()
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      return `${hours}:${minutes}`
    },

    scrollToBottom() {
      this.forceScrollToBottom()
    },

    forceScrollToBottom() {
      this.$nextTick(() => {
        const query = uni.createSelectorQuery().in(this)

        query.select('.message-list').fields({
          scrollOffset: true,
          size: true,
          scrollSize: true
        }, (data) => {
          if (data) {
            const scrollHeight = data.scrollHeight || 0
            const clientHeight = data.height || 0
            const targetScrollTop = Math.max(0, scrollHeight - clientHeight + 200)
            this.scrollTop = targetScrollTop
          }
        }).exec()

        this.scrollToView = 'msg-bottom'
        setTimeout(() => {
          this.scrollToView = ''
          this.$nextTick(() => {
            this.scrollToView = 'msg-bottom'
          })
        }, 50)
      })
    },

    handleScroll() {
    },

    formatTime(timestamp) {
      if (!timestamp) {
        return ''
      }

      if (typeof timestamp === 'string' && /^\d{1,2}:\d{2}$/.test(timestamp.trim())) {
        return timestamp.trim()
      }

      try {
        let date = null

        if (util.parseDate) {
          date = util.parseDate(timestamp)
        } else {
          date = new Date(timestamp)
        }

        if (!date || isNaN(date.getTime())) {
          if (typeof timestamp === 'number' || /^\d+$/.test(String(timestamp))) {
            date = new Date(parseInt(timestamp, 10))
          } else if (typeof timestamp === 'string') {
            date = new Date(timestamp.replace(/-/g, '/'))
          }
        }

        if (!date || isNaN(date.getTime())) {
          console.error('无效时间戳:', timestamp, '类型:', typeof timestamp)
          return ''
        }

        const hours = String(date.getHours()).padStart(2, '0')
        const minutes = String(date.getMinutes()).padStart(2, '0')
        return `${hours}:${minutes}`
      } catch (e) {
        console.error('格式化时间失败:', e, 'timestamp:', timestamp)
        return ''
      }
    },

    getWeChatTimestamp(msg) {
      const timestamp = msg.createTime || msg.time
      if (!timestamp) return ''

      if (typeof timestamp === 'string' && /^\d{1,2}:\d{2}$/.test(timestamp.trim())) {
        const now = new Date()
        const [hours, minutes] = timestamp.split(':')
        const date = new Date(now.getFullYear(), now.getMonth(), now.getDate(), parseInt(hours, 10), parseInt(minutes, 10))
        return util.formatWeChatTimestamp ? util.formatWeChatTimestamp(date) : timestamp
      }

      return util.formatWeChatTimestamp ? util.formatWeChatTimestamp(timestamp) : this.formatTime(timestamp)
    },

    shouldShowMessageTimestamp(msg, index) {
      if (!msg || index < 0) return false

      const currentTimestamp = msg.createTime || msg.time
      if (!currentTimestamp) return false

      if (index === 0) return true

      const previousMsg = this.messages[index - 1]
      if (!previousMsg) return true

      const previousTimestamp = previousMsg.createTime || previousMsg.time

      return util.shouldShowTimestamp ? util.shouldShowTimestamp(currentTimestamp, previousTimestamp) : true
    },

    parseChatContent(text) {
      if (!text || typeof text !== 'string') return [{ type: 'text', text: text || '' }]
      const parts = []
      const imgRegex = /\[\u56fe\](https?:\/\/[^\s\[\]]+)/g
      let lastEnd = 0
      let m
      while ((m = imgRegex.exec(text)) !== null) {
        if (m.index > lastEnd) parts.push({ type: 'text', text: text.substring(lastEnd, m.index) })
        parts.push({ type: 'image', url: m[1] })
        lastEnd = m.index + m[0].length
      }
      if (lastEnd < text.length) parts.push({ type: 'text', text: text.substring(lastEnd) })
      return parts.length ? parts : [{ type: 'text', text: text }]
    },
    isMessageImageOnly(msg) {
      if (!msg || !msg.content) return false
      const parts = this.parseChatContent(msg.content)
      if (!parts.length) return false
      const hasText = parts.some(p => p.type === 'text' && (p.text || '').trim())
      const hasImage = parts.some(p => p.type === 'image')
      return hasImage && !hasText
    },

    getImageUrl(url) {
      return util.getImageUrl ? util.getImageUrl(url) : (url || '')
    },

    onAvatarClick(userId) {
      if (userId == null) return
      const id = typeof userId === 'number' ? userId : parseInt(userId, 10)
      if (isNaN(id) || id <= 0) return
      uni.navigateTo({
        url: `/user/profile?userId=${id}`
      })
    },

    chooseChatImage() {
      const remain = 9 - this.chatImages.length
      if (remain <= 0) {
        uni.showToast({ title: this.ui.maxImages, icon: 'none' })
        return
      }
      uni.showActionSheet({
        itemList: [this.ui.sheetCamera, this.ui.sheetAlbum],
        success: (sheetRes) => {
          const sourceType = sheetRes.tapIndex === 0 ? ['camera'] : ['album']
          uni.chooseMedia({
            count: remain,
            mediaType: ['image'],
            sourceType: sourceType,
            success: (res) => {
              (res.tempFiles || []).forEach(f => {
                if (f.tempFilePath && this.chatImages.length < 9) this.chatImages.push(f.tempFilePath)
              })
            }
          })
        }
      })
    },
    removeChatImage(idx) {
      this.chatImages.splice(idx, 1)
    },

    uploadOneChatImage(tempPath) {
      const doUpload = (filePath) => {
        return new Promise((resolve) => {
          uni.uploadFile({
            url: api.getBaseUrl() + '/api/upload/image',
            filePath: filePath,
            name: 'file',
            formData: { type: 'message' },
            success: (res) => {
              try {
                const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
                if ((data.code === 0 || data.code === 200) && data.data) {
                  const url = typeof data.data === 'string' ? data.data : (data.data.url || data.data)
                  resolve(url)
                } else resolve('')
              } catch (e) { resolve('') }
            },
            fail: () => resolve('')
          })
        })
      }
      const compressedPromise = util.compressImageForUpload
        ? util.compressImageForUpload(tempPath)
        : Promise.resolve(tempPath)
      return compressedPromise.then(doUpload)
    },

    getTargetUserAvatar() {
      if (this.targetUserAvatar) {
        return util.getImageUrl ? util.getImageUrl(this.targetUserAvatar) : this.targetUserAvatar
      }
      return '/static/images/login-dog.png'
    },

    getUserAvatar() {
      if (this.userInfo?.avatar) {
        return util.getImageUrl ? util.getImageUrl(this.userInfo.avatar) : this.userInfo.avatar
      }
      return '/static/images/login-dog.png'
    },
    
    async markAllMessagesAsRead() {
      if (!this.targetUserId || !this.currentUserId) {
        return
      }

      try {
        const conversationId = this.generateConversationId(this.currentUserId, this.targetUserId)

        await api.markMessagesAsRead({
          userId: this.currentUserId,
          conversationId: conversationId
        })

        uni.$emit('messagesRead', {
          conversationId: conversationId,
          targetUserId: this.targetUserId
        })
      } catch (error) {
        console.error('生成会话ID失败:', error)
      }
    },
    
    getServiceConversationId() {
      if (!this.isService || !this.currentUserId) return ''
      if (this.isPlatform) return `platform_${this.currentUserId}`
      if (!this.storeId) return ''
      const prefix = this.isOutlet ? 'outlet_' : 'service_'
      return `${prefix}${this.storeId}_${this.currentUserId}`
    },
    generateConversationId(userId1, userId2) {
      if (this.isService && this.storeId) {
        return this.getServiceConversationId()
      }
      if (userId1 < userId2) {
        return userId1 + '_' + userId2
      } else {
        return userId2 + '_' + userId1
      }
    },

    showVoiceInput() {
      uni.showToast({
        title: this.ui.voiceDeveloping,
        icon: 'none'
      })
    },

    
    showMoreOptions() {
      uni.showToast({
        title: this.ui.moreDeveloping,
        icon: 'none'
      })
    },
    
    startMessagePolling() {
      if (this.messagePollingTimer) {
        return
      }
      this.checkNewMessages()
      this.messagePollingTimer = setInterval(() => {
        this.checkNewMessages()
      }, 2000)
    },
    
    stopMessagePolling() {
      if (this.messagePollingTimer) {
        clearInterval(this.messagePollingTimer)
        this.messagePollingTimer = null
      }
      this.isPolling = false
    },
    
    async checkNewMessages() {
      if (this.isPolling || !this.targetUserId || !this.currentUserId) {
        return
      }

      if (!this.lastMessageId) {
        return
      }

      this.isPolling = true

      try {
        let res
        if (this.isService) {
          const conversationId = this.getServiceConversationId()
          res = await api.getPrivateMessages({
            userId: this.currentUserId,
            conversationId: conversationId,
            page: 1,
            size: 50,
            showLoading: false
          })
        } else {
          res = await api.getPrivateMessages({
            userId: this.currentUserId,
            targetUserId: this.targetUserId,
            page: 1,
            size: 50,
            showLoading: false
          })
        }

        if ((res.code === 0 || res.code === 200) && res.data && res.data.messages) {
          const allMessages = res.data.messages || []

          const lastIndex = allMessages.findIndex(msg => {
            const msgId = msg.id || msg.messageId
            return String(msgId) === String(this.lastMessageId)
          })

          if (lastIndex === -1) {
            this.page = 1
            this.lastMessageId = null
            this.loadChatHistory()
            return
          }

          const newMessages = allMessages.slice(0, lastIndex)

          if (newMessages.length > 0) {
            const formattedNewMessages = newMessages.map(msg => {
              const senderId = msg.senderId || msg.fromId
              const createTime = msg.createTime || msg.create_time || null
              let displayTime = msg.time || ''

              if (!displayTime && createTime) {
                displayTime = this.formatTime(createTime)
              }

              return {
                id: msg.id,
                senderId: senderId,
                receiverId: msg.receiverId || msg.toId,
                content: msg.content,
                createTime: createTime,
                time: displayTime
              }
            }).filter(msg => msg && msg.content)

            formattedNewMessages.reverse()

            this.messages = [...this.messages, ...formattedNewMessages]

            if (formattedNewMessages.length > 0) {
              this.lastMessageId = formattedNewMessages[formattedNewMessages.length - 1].id
            }

            this.$nextTick(() => {
              this.scrollToBottom()
            })

            this.markAllMessagesAsRead()
          }
        }
      } catch (error) {
        console.error('检查新消息失败:', error)
      } finally {
        this.isPolling = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.message-list {
  flex: 1;
  padding: 20rpx 0;
  padding-bottom: 104rpx; /* 为底部输入框留出空间 */
  overflow-y: auto;
  transition: padding-bottom 0.15s ease-out;
  box-sizing: border-box;
}

.empty-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
}

.empty-text {
  font-size: 24rpx;
  color: #999;
}

.message-wrapper {
  margin-bottom: 20rpx;
  padding: 0 20rpx;
}

.message-timestamp {
  text-align: center;
  font-size: 22rpx;
  color: #999;
  margin: 20rpx 0;
  padding: 0 20rpx;
}

.message-item {
  display: flex;
  align-items: flex-start;
  width: 100%;

  .avatar-wrap {
    flex-shrink: 0;
    padding: 4rpx;
    margin: -4rpx;
  }
  .avatar-wrap:active {
    opacity: 0.7;
  }
  .avatar {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background-color: #e0e0e0;
    display: block;
  }

  .message-content {
    max-width: 70%;
    display: flex;
    flex-direction: column;
    flex-shrink: 1;
  }

  .message-bubble {
    padding: 10rpx 15rpx;
    border-radius: 8rpx;
    word-wrap: break-word;
    word-break: break-word;
    max-width: 100%;
    box-sizing: border-box;
    min-height: 50rpx;
    display: inline-block;
    
    .message-text-wrapper {
      display: inline-flex;
      flex-wrap: wrap;
      align-items: center;
      line-height: 1.5;
      min-height: 30rpx;
    }
    
    .message-text {
      font-size: 30rpx;
      line-height: 1.5;
      color: #333;
      white-space: pre-wrap;
    }
  }
}

.other-msg {
  justify-content: flex-start;

  .avatar {
    margin-right: 12rpx;
  }

  .message-content {
    align-items: flex-start;
  }

  .message-bubble {
    background-color: white;
    border-top-left-radius: 0;
  }

  .message-time {
    padding-left: 4rpx;
  }
}

.my-msg {
  justify-content: flex-end !important;
  flex-direction: row;
  align-items: flex-start;

  .message-content {
    order: 1;
    align-items: flex-end;
    position: relative;
    padding-right: 6rpx;
  }

  .message-content::after {
    content: '';
    position: absolute;
    right: 0;
    top: 21rpx;
    width: 0;
    height: 0;
    border-top: 7rpx solid transparent;
    border-bottom: 7rpx solid transparent;
    border-left: 10rpx solid #95ec69;
  }

  .avatar-wrap {
    order: 2;
  }

  .avatar {
    margin-left: 8rpx;
    margin-right: 0;
  }

  .message-bubble {
    background-color: #95ec69;
    border-top-right-radius: 0;
    min-height: 56rpx;
    display: inline-flex;
    align-items: center;
    box-sizing: border-box;
  }

  .message-bubble .message-text-wrapper {
    min-height: 36rpx;
  }

  .message-time {
    padding-right: 4rpx;
  }
}

.input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  background-color: #f7f7f7;
  border-top: 1rpx solid #e5e5e5;

  .input-left {
    display: flex;
    align-items: center;
    margin-right: 12rpx;
  }

  .voice-btn {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }

  .voice-icon {
    font-size: 36rpx;
  }

  .input-wrapper {
    flex: 1;
    background-color: #fff;
    border-radius: 8rpx;
    position: relative;
    margin: 0 12rpx;
  }

  .input-content-wrapper {
    position: relative;
    min-height: 72rpx;
    padding: 10rpx 20rpx;
    display: flex;
    align-items: flex-start;
    background-color: #fff;
    border-radius: 8rpx;
  }

  .input-content-display {
    display: inline-flex;
    flex-wrap: wrap;
    align-items: flex-start;
    min-height: 52rpx;
    line-height: 36rpx;
    font-size: 30rpx;
    letter-spacing: 0;
    flex: 1;
    position: relative;
    z-index: 0;
    word-break: break-all;
    padding-top: 2rpx;
  }

  .input-text {
    font-size: 30rpx;
    line-height: 36rpx;
    color: #333;
    letter-spacing: 0;
    display: inline;
    word-break: break-all;
  }

  .input-emoji {
    width: 30rpx;
    height: 30rpx;
    vertical-align: middle;
    display: inline-block;
    flex-shrink: 0;
  }

}

.input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  padding: 16rpx 20rpx;
  background-color: #f7f7f7;
  border-top: 1rpx solid #e5e5e5;
  z-index: 100;
  transition: bottom 0.15s ease-out;
  will-change: bottom;
  min-height: 104rpx;
  box-sizing: border-box;
}

.input-bar {
  flex-direction: column;
  align-items: stretch;
}
.input-bar-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.input-bar .input-wrapper {
  flex: 1;
  background-color: white;
  border-radius: 8rpx;
  padding: 0 20rpx 0 24rpx;
  margin-right: 0;
  height: 72rpx;
  display: flex;
  align-items: center;
}
.input-action-icon {
  width: 40rpx;
  height: 40rpx;
  flex-shrink: 0;
  margin-left: 12rpx;
}
.chat-images-row {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-top: 12rpx;
}
.chat-image-item {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  overflow: hidden;
}
.chat-image-thumb { width: 100%; height: 100%; }
.chat-image-del {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  width: 36rpx;
  height: 36rpx;
  line-height: 36rpx;
  text-align: center;
  font-size: 28rpx;
  color: #fff;
  background: rgba(0,0,0,0.5);
  border-radius: 50%;
}
.message-image-inline {
  max-width: 280rpx;
  max-height: 280rpx;
  border-radius: 8rpx;
  margin-top: 8rpx;
  display: block;
}

.message-bubble.bubble-image-only {
  background: transparent !important;
  padding: 0 !important;
  min-height: 0 !important;
  box-shadow: none !important;
}
.message-content.content-image-only::after {
  display: none !important;
}
.message-bubble.bubble-image-only .message-text-wrapper {
  min-height: 0 !important;
}
.message-bubble.bubble-image-only .message-image-inline:first-child {
  margin-top: 0;
}

.input-bar .message-input {
  flex: 1;
  min-width: 0;
  height: 72rpx;
  font-size: 28rpx;
  line-height: 72rpx;
  border: 0;
  background: transparent;
}

.input-bar .send-button {
  background-color: #07c160;
  color: white;
  border: none;
  padding: 16rpx 32rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  line-height: 1.4;
  flex-shrink: 0;

  &:disabled {
    background-color: #d0d0d0;
    color: #999;
  }

  &:not(:disabled):active {
    opacity: 0.8;
  }
}

</style>
