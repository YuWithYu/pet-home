<template>
  <view class="chat-container">
    <!-- 聊天消息区域（与用户聊天页一致） -->
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
        <text class="empty-text">没有更多消息</text>
      </view>
      <view class="message-wrapper" v-for="(msg, index) in messages" :key="msg.id || index" :id="'msg-' + index" :data-index="index">
        <view v-if="shouldShowMessageTimestamp(msg, index)" class="message-timestamp">
          {{ getWeChatTimestamp(msg) }}
        </view>
        
        <!-- 客服消息（左侧，白色气泡仅包文字；图片单独展示无气泡）-->
        <view v-if="msg.senderId < 0" class="message-item other-msg">
          <image class="avatar" :src="getTargetUserAvatar()" mode="aspectFill" />
          <view class="message-content">
            <block v-for="(part, pidx) in parseChatContent(msg.content)" :key="pidx">
              <view v-if="part.type === 'text' && part.text.trim()" class="message-bubble">
                <text class="message-text">{{ part.text }}</text>
              </view>
              <view v-else-if="part.type === 'image'" class="message-image-wrap">
                <image class="message-image-only" :src="getImageUrl(part.url)" mode="widthFix" />
              </view>
            </block>
          </view>
        </view>

        <!-- 用户自己的消息（右侧，绿色气泡仅包文字；图片单独展示无气泡，仿微信） -->
        <view v-else-if="msg.senderId == currentUserId" class="message-item my-msg">
          <view class="message-content" :class="{ 'has-bubble': hasTextPart(msg) }">
            <block v-for="(part, pidx) in parseChatContent(msg.content)" :key="pidx">
              <view v-if="part.type === 'text' && part.text.trim()" class="message-bubble">
                <text class="message-text">{{ part.text }}</text>
              </view>
              <view v-else-if="part.type === 'image'" class="message-image-wrap">
                <image class="message-image-only" :src="getImageUrl(part.url)" mode="widthFix" />
              </view>
            </block>
          </view>
          <image class="avatar" :src="getUserAvatar()" mode="aspectFill" />
        </view>
      </view>

      <view id="msg-bottom" style="height: 1rpx;"></view>
    </scroll-view>

    <!-- 底部输入栏（与视频评论区一致：图片上传图标 图片.png）-->
    <view class="input-bar">
      <view class="input-bar-row">
        <view class="input-wrapper">
          <input 
            class="message-input"
            v-model="inputMessage"
            placeholder="请输入消息..."
            confirm-type="send"
            :adjust-position="true"
            @confirm="sendMessage"
            maxlength="500"
          />
          <image class="input-action-icon" src="/static/images/图片.png" mode="aspectFit" @click="chooseChatImage"></image>
        </view>
        <button class="send-button" @tap="sendMessage" :disabled="!inputMessage.trim() && !chatImages.length">
          发送        </button>
      </view>
      <view v-if="chatImages.length" class="chat-images-row">
        <view class="chat-image-item" v-for="(img, idx) in chatImages" :key="idx">
          <image class="chat-image-thumb" :src="img" mode="aspectFill" />
          <view class="chat-image-del" @click="removeChatImage(idx)">×</view>
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
  name: 'CustomerServiceChat',
  
  data() {
    return {
      storeId: 1,
      isPlatform: false,
      serviceInfo: {
        id: 1,
        name: '客服小助手',
        title: '在线咨询',
        avatar: '/static/images/客服.png',
        online: true
      },
      userAvatar: '/static/images/user-avatar.png',
      messages: [],
      inputMessage: '',
      scrollToView: '',
      scrollTop: 0,
      currentUserId: null,
      conversationId: null,
      messagePollingTimer: null,
      lastMessageId: null,
      isPolling: false,
      loading: false,
      chatImages: []
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn'])
  },

  onLoad(options) {
    this.$set(this, 'inputMessage', '')
    this.isPlatform = options.isPlatform === 'true' || options.isPlatform === true
    if (!this.isPlatform && options.storeId) {
      this.storeId = parseInt(options.storeId)
    }
    
    this.currentUserId = this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId') || null
    
    if (!this.currentUserId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      setTimeout(() => uni.navigateTo({ url: '/pages-auth/login' }), 1500)
      return
    }
    
    this.conversationId = this.isPlatform
      ? `platform_${this.currentUserId}`
      : `service_${this.storeId}_${this.currentUserId}`
    
    if (this.isPlatform) {
      this.serviceInfo.name = '宠物之家在线咨询'
      this.serviceInfo.title = '在线咨询'
      this.serviceInfo.avatar = '/static/images/宠物之家.png'
      uni.setNavigationBarTitle({ title: '在线咨询' })
    }
    
    this.loadChatHistory()
  },
  
  onShow() {
    this.markAsReadAndNotify()
    this.startMessagePolling()
  },
  
  onHide() {
    this.stopMessagePolling()
  },
  
  onUnload() {
    this.stopMessagePolling()
  },

  methods: {
    // 进入客服聊天时标记已读并通知消息列表清除未读角标
    async markAsReadAndNotify() {
      if (!this.currentUserId || !this.conversationId) return
      try {
        await api.markMessagesAsRead({
          userId: this.currentUserId,
          conversationId: this.conversationId
        })
      } catch (e) {
        // 静默忽略
      }
      const targetUserId = this.isPlatform ? 'platform_0' : (this.storeId != null ? 'service_' + this.storeId : '')
      uni.$emit('messagesRead', {
        conversationId: this.conversationId,
        targetUserId: targetUserId
      })
    },
    parseChatContent(text) {
      if (!text || typeof text !== 'string') return [{ type: 'text', text: text || '' }]
      const parts = []
      const imgRegex = /\[图片\](https?:\/\/[^\s\[\]]+)/g
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
    hasTextPart(msg) {
      if (!msg || !msg.content) return false
      return this.parseChatContent(msg.content).some(p => p.type === 'text' && (p.text || '').trim())
    },
    getImageUrl(url) {
      return util.getImageUrl ? util.getImageUrl(url) : (url || '')
    },
    chooseChatImage() {
      const remain = 9 - this.chatImages.length
      if (remain <= 0) {
        uni.showToast({ title: '最多上传9张图片', icon: 'none' })
        return
      }
      uni.showActionSheet({
        itemList: ['拍照', '从相册选择'],
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
      return new Promise((resolve) => {
        uni.uploadFile({
          url: api.getBaseUrl() + '/api/upload/image',
          filePath: tempPath,
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
    },
    // 与消息列表getAvatarUrl 一致；仅平台客服使用宠物之家.png，门店仍用 客服.png
    getTargetUserAvatar() {
      if (this.isPlatform) {
        const url = '/static/images/宠物之家.png'
        return (util.getImageUrl && util.getImageUrl(url)) || url
      }
      let avatar = this.serviceInfo.avatar || '/static/images/客服.png'
      const s = String(avatar).trim()
      if (s.includes('customer-service.svg')) avatar = '/static/images/客服.png'
      return (util.getImageUrl && util.getImageUrl(avatar)) || avatar
    },
    getUserAvatar() {
      const avatar = this.userInfo?.avatar || this.userAvatar
      if (!avatar) return '/static/images/user-avatar.png'
      if (avatar.startsWith('http')) return avatar
      return (util.getImageUrl && util.getImageUrl(avatar)) || avatar
    },
    loadMoreMessages() {},
    handleScroll() {},
    async loadChatHistory() {
      if (this.loading) return
      this.loading = true
      
      try {
        const res = await api.getPrivateMessages({
          userId: this.currentUserId,
          conversationId: this.conversationId,
          page: 1,
          size: 20
        })
        
        if ((res.code === 0 || res.code === 200) && res.data) {
          if (res.data.storeInfo && !this.isPlatform) {
            const storeInfo = res.data.storeInfo
            this.serviceInfo.name = storeInfo.storeName || '客服小助手'
            const rawAvatar = storeInfo.storeAvatar || '/static/images/宠物之家.png'
            this.serviceInfo.avatar = (rawAvatar && rawAvatar.includes('customer-service.svg')) ? '/static/images/宠物之家.png' : rawAvatar
            uni.setNavigationBarTitle({ title: this.serviceInfo.name })
          }
          
          const newMessages = res.data.messages || []
          const formattedMessages = newMessages.map(msg => {
            let senderId = msg.senderId || msg.fromId
            const createTime = msg.createTime || msg.create_time || null
            let displayTime = msg.time || ''
            if (!displayTime && createTime) displayTime = this.formatTime(createTime)
            return {
              id: msg.id,
              senderId: senderId,
              receiverId: msg.receiverId || msg.toId,
              content: msg.content,
              createTime: createTime,
              time: displayTime
            }
          }).filter(msg => msg && msg.content)
          
          this.messages = formattedMessages.reverse()
          
          if (this.messages.length > 0) {
            this.lastMessageId = this.messages[this.messages.length - 1].id
          }
          
          if (this.messages.length === 0) {
            const serviceSenderId = this.isPlatform ? -9999 : -this.storeId
            this.messages.push({
              id: 'welcome',
              senderId: serviceSenderId,
              content: '您好！我是客服小助手，很高兴为您服务。请问有什么可以帮助您的吗？',
              time: this.getCurrentTime(),
              createTime: new Date().toISOString()
            })
          }
          
          this.$nextTick(() => this.scrollToBottom())
        }
      } catch (error) {
        console.error('加载聊天历史失败:', error)
        const serviceSenderId = this.isPlatform ? -9999 : -this.storeId
        this.messages.push({
          id: 'welcome',
          senderId: serviceSenderId,
          content: '您好！我是客服小助手，很高兴为您服务。请问有什么可以帮助您的吗？',
          time: this.getCurrentTime(),
          createTime: new Date().toISOString()
        })
      } finally {
        this.loading = false
      }
    },
    
    async sendMessage() {
      let message = this.inputMessage ? this.inputMessage.trim() : ''
      if (!message && !this.chatImages.length) return

      if (this.chatImages.length > 0) {
        uni.showLoading({ title: '上传中...', mask: true })
        const urls = []
        for (let i = 0; i < this.chatImages.length; i++) {
          const url = await this.uploadOneChatImage(this.chatImages[i])
          if (url) urls.push(url)
        }
        uni.hideLoading()
        if (urls.length) {
          const imgStr = urls.map(u => '[图片]' + u).join('\n')
          message = message ? message + '\n' + imgStr : imgStr
        } else {
          if (!message) {
            uni.showToast({ title: '图片上传失败', icon: 'none' })
            return
          }
        }
      }

      const receiverId = this.isPlatform ? -9999 : -this.storeId
      const tempMessage = {
        id: 'temp-' + Date.now(),
        senderId: this.currentUserId,
        receiverId: receiverId,
        content: message,
        createTime: new Date().toISOString(),
        time: this.getCurrentTime()
      }
      this.messages.push(tempMessage)
      this.inputMessage = ''
      this.chatImages = []
      this.scrollToBottom()
      
      try {
        const res = await api.sendPrivateMessage({
          senderId: this.currentUserId,
          receiverId: receiverId,
          content: message,
          conversationId: this.conversationId
        })
        
        if ((res.code === 0 || res.code === 200) && res.data) {
          const index = this.messages.findIndex(m => m.id === tempMessage.id)
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
          setTimeout(() => this.checkNewMessages(), 500)
        } else {
          const idx = this.messages.findIndex(m => m.id === tempMessage.id)
          if (idx !== -1) this.messages.splice(idx, 1)
          throw new Error(res.msg || '发送失败')
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        const idx = this.messages.findIndex(m => m.id === tempMessage.id)
        if (idx !== -1) this.messages.splice(idx, 1)
        uni.showToast({ title: '发送失败，请重试', icon: 'none' })
      }
    },
    
    startMessagePolling() {
      if (this.messagePollingTimer) return
      if (!this.conversationId || !this.currentUserId) return
      this.checkNewMessages()
      this.messagePollingTimer = setInterval(() => this.checkNewMessages(), 2000)
    },
    
    stopMessagePolling() {
      if (this.messagePollingTimer) {
        clearInterval(this.messagePollingTimer)
        this.messagePollingTimer = null
      }
      this.isPolling = false
    },
    
    async checkNewMessages() {
      if (this.isPolling || !this.conversationId || !this.currentUserId || !this.lastMessageId) return
      this.isPolling = true
      
      try {
        const res = await api.getPrivateMessages({
          userId: this.currentUserId,
          conversationId: this.conversationId,
          page: 1,
          size: 50,
          showLoading: false
        })
        
        if ((res.code === 0 || res.code === 200) && res.data && res.data.messages) {
          const allMessages = res.data.messages || []
          const reversedMessages = [...allMessages].reverse()
          const lastIndex = reversedMessages.findIndex(msg => String(msg.id || msg.messageId) === String(this.lastMessageId))
          
          if (lastIndex === -1) {
            this.lastMessageId = null
            this.loadChatHistory()
            return
          }
          
          const newMessages = reversedMessages.slice(lastIndex + 1)
          
          if (newMessages.length > 0) {
            const formattedNewMessages = newMessages.map(msg => {
              let senderId = msg.senderId || msg.fromId
              const createTime = msg.createTime || msg.create_time || null
              let displayTime = msg.time || ''
              if (!displayTime && createTime) displayTime = this.formatTime(createTime)
              return {
                id: msg.id,
                senderId,
                receiverId: msg.receiverId || msg.toId,
                content: msg.content,
                createTime,
                time: displayTime
              }
            }).filter(msg => msg && msg.content)
            
            this.messages = [...this.messages, ...formattedNewMessages]
            if (formattedNewMessages.length > 0) {
              this.lastMessageId = formattedNewMessages[formattedNewMessages.length - 1].id
            }
            this.$nextTick(() => this.scrollToBottom())
          }
        }
      } catch (error) {
        console.error('检查新消息失败:', error)
      } finally {
        this.isPolling = false
      }
    },

    getCurrentTime() {
      const now = new Date()
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    },
    
    formatTime(timestamp) {
      if (!timestamp) return ''
      try {
        const date = typeof timestamp === 'string' ? new Date(timestamp) : new Date(timestamp)
        if (isNaN(date.getTime())) return ''
        return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
      } catch (e) {
        return ''
      }
    },

    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollTop = 99999
      })
    },
    
    getWeChatTimestamp(msg) {
      const timestamp = msg.createTime || msg.time
      if (!timestamp) return ''
      if (typeof timestamp === 'string' && /^\d{1,2}:\d{2}$/.test(timestamp.trim())) {
        const now = new Date()
        const [hours, minutes] = timestamp.split(':')
        const date = new Date(now.getFullYear(), now.getMonth(), now.getDate(), parseInt(hours), parseInt(minutes))
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
  padding-bottom: 104rpx;
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

/* 头像统一缩小，圆�?*/
.message-item {
  display: flex;
  align-items: flex-start;
  width: 100%;

  .avatar {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background-color: #e0e0e0;
    flex-shrink: 0;
  }

  .message-content {
    max-width: 70%;
    display: flex;
    flex-direction: column;
    flex-shrink: 1;
    gap: 12rpx;
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
    align-self: flex-start;
  }

  .message-text {
    font-size: 30rpx;
    line-height: 1.5;
    color: #333;
    white-space: pre-wrap;
  }

  /* 图片单独展示，无气泡包裹（仿微信�?*/
  .message-image-wrap {
    display: inline-block;
    align-self: flex-start;
    background: none;
    border: none;
    padding: 0;
  }
  .message-image-only {
    max-width: 280rpx;
    max-height: 280rpx;
    border-radius: 8rpx;
    display: block;
    vertical-align: middle;
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
}

/* 发送方：气泡在左、头像在右；顶部对齐；气泡与头像同高（短消息）；尖端对准头像中心 */
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

  .message-content.has-bubble::after {
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

  .avatar {
    order: 2;
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

}

.input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: 16rpx 20rpx;
  background-color: #f7f7f7;
  border-top: 1rpx solid #e5e5e5;
  z-index: 100;
  transition: bottom 0.15s ease-out;
  will-change: bottom;
  min-height: 104rpx;
  box-sizing: border-box;
}
.input-bar-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
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
.input-action-icon {
  width: 40rpx;
  height: 40rpx;
  flex-shrink: 0;
  margin-left: 12rpx;
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

  .message-input {
    width: 100%;
    height: 72rpx;
    font-size: 28rpx;
    line-height: 72rpx;
    border: 0;
    background: transparent;
  }

  .send-button {
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
