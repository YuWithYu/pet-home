<template>
  <div class="outlet-customer-chat-container">
    <!-- 服务门店选择器 -->
    <div class="store-selector-bar">
      <span class="selector-label">当前门店：</span>
      <el-select v-model="selectedServiceStoreId" placeholder="请选择门店" size="default" class="store-select" :disabled="isOutletAdmin" @change="onOutletChange">
        <el-option v-for="s in outletList" :key="s.id" :label="s.storeName || ('门店' + s.id)" :value="s.id" />
      </el-select>
      <span v-if="isOutletAdmin" class="admin-hint">（您仅能查看本门店的客服会话）</span>
    </div>
    <el-container>
      <el-aside width="400px" class="chat-list-aside">
        <div class="aside-header">
          <h3>{{ selectedOutletName }} - 客服会话</h3>
          <el-select v-model="filterStatus" placeholder="筛选状态" size="small" style="width: 120px" @change="loadConversations">
            <el-option label="全部" value="" />
            <el-option label="有未读" value="unread" />
            <el-option label="已读" value="read" />
          </el-select>
        </div>
        <el-scrollbar class="conversation-list">
          <div
            v-for="conv in conversationList"
            :key="conv.conversationId"
            class="conversation-item"
            :class="{ active: currentConversation && currentConversation.conversationId === conv.conversationId }"
            @click="selectConversation(conv)"
          >
            <div class="item-main">
              <div class="item-header">
                <div class="user-info">
                  <el-avatar :size="40" class="user-avatar" :src="getImageUrl(conv.userAvatar)">
                    <el-icon><User /></el-icon>
                  </el-avatar>
                  <div class="user-detail">
                    <div class="user-name">{{ conv.userName || conv.userNickname || '用户' }}</div>
                    <div class="conversation-time">{{ formatTime(conv.lastMessageTime) }}</div>
                  </div>
                </div>
                <el-badge v-if="conv.unreadCount > 0" :value="conv.unreadCount" :max="99" class="unread-badge" />
              </div>
              <div class="item-content">
                <div class="last-message">{{ conv.lastMessage || '暂无消息' }}</div>
              </div>
            </div>
            <div class="item-actions" @click.stop>
              <el-button type="danger" text size="small" @click="deleteConversation(conv)">删除</el-button>
            </div>
          </div>
          <el-empty v-if="!selectedServiceStoreId" description="请先选择门店" />
          <el-empty v-else-if="!conversationList.length" description="暂无会话记录" />
        </el-scrollbar>
      </el-aside>
      <el-container class="chat-container">
        <template v-if="currentConversation">
          <el-header height="60px" class="chat-header">
            <div class="chat-header-info">
              <el-avatar :size="36" :src="getImageUrl(currentConversation.userAvatar)">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="header-detail">
                <div class="store-name">{{ currentConversation.userName || currentConversation.userNickname || '用户' }}</div>
                <div class="store-info">用户ID: {{ currentConversation.userId || currentConversation.otherUserId }}</div>
              </div>
            </div>
            <div class="chat-header-actions">
              <el-button size="small" @click="loadConversations">刷新</el-button>
            </div>
          </el-header>
          <el-main class="chat-main">
            <el-scrollbar ref="chatScrollbar" class="message-scrollbar">
              <div class="message-list">
                <template v-for="msg in messageList" :key="msg.id">
                  <div v-if="isServiceMessage(msg)" class="message-item my-message">
                    <el-avatar :size="40" class="message-avatar" :src="getImageUrl(currentConversation.storeAvatar)">
                      <el-icon><User /></el-icon>
                    </el-avatar>
                    <div class="message-content">
                      <div class="message-bubble">
                        <div class="message-text">{{ msg.content }}</div>
                        <div class="message-time">{{ formatDateTime(msg.createTime || msg.time) }}</div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="message-item other-message">
                    <el-avatar :size="40" class="message-avatar" :src="getImageUrl(currentConversation.userAvatar)">
                      <el-icon><User /></el-icon>
                    </el-avatar>
                    <div class="message-content">
                      <div class="message-bubble">
                        <div class="message-text">{{ msg.content }}</div>
                        <div class="message-time">{{ formatDateTime(msg.createTime || msg.time) }}</div>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </el-scrollbar>
          </el-main>
          <el-footer height="auto" class="chat-footer">
            <div class="reply-input-wrapper">
              <el-input v-model="replyText" type="textarea" :rows="4" placeholder="请输入回复内容..." maxlength="1000" show-word-limit class="reply-textarea" @keydown.enter.ctrl="sendMessage" @keydown.enter.exact.prevent="handleEnterKey" />
            </div>
            <div class="reply-actions">
              <el-button @click="clearReply">清空</el-button>
              <el-button type="primary" @click="sendMessage" :loading="sending" :disabled="!replyText.trim()">发送消息</el-button>
            </div>
          </el-footer>
        </template>
        <el-empty v-else description="请选择一个会话" style="height: 100%; display: flex; align-items: center; justify-content: center;" />
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { baseURL } from '@/config'

const store = useStore()

const filterStatus = ref('')
const conversationList = ref([])
const currentConversation = ref(null)
const messageList = ref([])
const replyText = ref('')
const sending = ref(false)
const chatScrollbar = ref(null)

const hiddenConversationIdsKey = 'outlet_hidden_conversations'
const hiddenConversationIds = ref(new Set())

const loadHiddenConversations = () => {
  try {
    const raw = localStorage.getItem(hiddenConversationIdsKey)
    if (raw) hiddenConversationIds.value = new Set(JSON.parse(raw))
  } catch (e) {
    hiddenConversationIds.value = new Set()
  }
}

const saveHiddenConversations = () => {
  try {
    localStorage.setItem(hiddenConversationIdsKey, JSON.stringify(Array.from(hiddenConversationIds.value)))
  } catch (e) {}
}

const outletList = ref([])
const selectedServiceStoreId = ref(null)
const isOutletAdmin = ref(false)
const selectedOutletStoreKey = 'admin_selected_service_store_id'

const selectedOutletName = computed(() => {
  const s = outletList.value.find(x => x.id === selectedServiceStoreId.value)
  return s ? (s.storeName || '门店' + s.id) : '请选择门店'
})

const loadOutlets = async () => {
  if (!store.getters['user/accessToken']) return
  try {
    const res = await request.get('/admin/service-stores')
    if ((res.code === 0 || res.code === 200) && res.data) {
      const list = Array.isArray(res.data) ? res.data : (res.data.list || res.data.records || [])
      outletList.value = list
      if (list.length > 0) {
        isOutletAdmin.value = list.length === 1
        const savedId = localStorage.getItem(selectedOutletStoreKey)
        const validSaved = savedId && list.some(s => s.id === Number(savedId))
        selectedServiceStoreId.value = validSaved ? Number(savedId) : list[0].id
        loadConversations()
      }
    }
  } catch (e) {
    console.error('加载门店列表失败:', e)
    ElMessage.error('加载门店列表失败')
  }
}

const onOutletChange = () => {
  if (selectedServiceStoreId.value) {
    localStorage.setItem(selectedOutletStoreKey, String(selectedServiceStoreId.value))
  }
  currentConversation.value = null
  messageList.value = []
  loadConversations()
}

const loadConversations = async () => {
  if (!store.getters['user/accessToken']) return
  if (!selectedServiceStoreId.value) return
  try {
    const response = await request.get('/messages/conversations/all-outlet', {
      params: { serviceStoreId: selectedServiceStoreId.value }
    })
    if ((response.code === 0 || response.code === 200) && response.data) {
      let conversations = response.data
      if (filterStatus.value === 'unread') conversations = conversations.filter(conv => conv.unreadCount > 0)
      else if (filterStatus.value === 'read') conversations = conversations.filter(conv => conv.unreadCount === 0)
      conversations = conversations.filter(conv => {
        if (!conv.conversationId) return true
        if (!hiddenConversationIds.value.has(conv.conversationId)) return true
        return conv.unreadCount > 0
      })
      conversationList.value = conversations
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  }
}

const selectConversation = async (conv) => {
  try {
    currentConversation.value = conv
    const conversationId = conv.conversationId
    let userId = null
    let serviceStoreId = selectedServiceStoreId.value || conv.serviceStoreId

    if (conversationId && conversationId.startsWith('outlet_')) {
      const parts = conversationId.replace('outlet_', '').split('_')
      if (parts.length >= 2) {
        serviceStoreId = parseInt(parts[0])
        userId = parseInt(parts[1])
      }
    } else {
      userId = conv.userId || conv.otherUserId
    }

    if (!userId) {
      ElMessage.warning('无法获取用户ID')
      return
    }

    const response = await request.get('/messages/private', {
      params: { userId, conversationId, page: 1, size: 100 }
    })

    if ((response.code === 0 || response.code === 200) && response.data) {
      messageList.value = (response.data.messages || []).reverse()
      if (response.data.storeInfo && currentConversation.value) {
        currentConversation.value.storeName = response.data.storeInfo.storeName
        currentConversation.value.storeAvatar = response.data.storeInfo.storeAvatar
        if (response.data.storeInfo.serviceStoreId) serviceStoreId = response.data.storeInfo.serviceStoreId
      }
      if (currentConversation.value) {
        currentConversation.value.serviceStoreId = serviceStoreId
      }
      await markMessagesAsRead(conversationId, serviceStoreId)
      replyText.value = ''
      nextTick(() => scrollToBottom())
    }
  } catch (error) {
    console.error('加载会话消息失败:', error)
    ElMessage.error('加载会话消息失败')
  }
}

const sendMessage = async () => {
  if (!replyText.value.trim() || !currentConversation.value) return
  try {
    sending.value = true
    const conversationId = currentConversation.value.conversationId
    let userId = null
    let serviceStoreId = selectedServiceStoreId.value || currentConversation.value.serviceStoreId

    if (conversationId && conversationId.startsWith('outlet_')) {
      const parts = conversationId.replace('outlet_', '').split('_')
      if (parts.length >= 2) {
        serviceStoreId = parseInt(parts[0])
        userId = parseInt(parts[1])
      }
    }

    if (!userId) {
      ElMessage.error('无法获取用户ID')
      return
    }

    const response = await request.post('/messages/outlet', {
      userId,
      serviceStoreId,
      content: replyText.value
    })

    if ((response.code === 0 || response.code === 200) && response.data) {
      messageList.value.push({
        id: response.data.id,
        senderId: response.data.senderId,
        receiverId: response.data.receiverId,
        content: response.data.content,
        createTime: response.data.createTime,
        time: formatDateTime(response.data.createTime)
      })
      replyText.value = ''
      nextTick(() => scrollToBottom())
      loadConversations()
    } else {
      ElMessage.error(response.msg || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败：' + (error.response?.data?.msg || error.message))
  } finally {
    sending.value = false
  }
}

const markMessagesAsRead = async (conversationId, serviceStoreId) => {
  try {
    const response = await request.post('/messages/read', {
      userId: -serviceStoreId,
      conversationId
    })
    if (response.code === 0 || response.code === 200) loadConversations()
  } catch (e) {}
}

const handleEnterKey = (event) => {
  if (event.key === 'Enter' && !event.ctrlKey && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const isServiceMessage = (msg) => {
  const senderId = msg.senderId !== undefined ? msg.senderId : msg.fromId
  if (senderId == null) return false
  return Number(senderId) < 0
}

const clearReply = () => { replyText.value = '' }

const deleteConversation = async (conv) => {
  try {
    await ElMessageBox.confirm('删除后，该会话会从列表中移除。当该用户再次发送新消息时，会话会重新出现。是否继续？', '删除会话', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    if (conv.conversationId) {
      hiddenConversationIds.value.add(conv.conversationId)
      saveHiddenConversations()
    }
    if (currentConversation.value && currentConversation.value.conversationId === conv.conversationId) {
      currentConversation.value = null
      messageList.value = []
      replyText.value = ''
    }
    loadConversations()
    ElMessage.success('会话已删除')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const scrollToBottom = () => {
  if (chatScrollbar.value) nextTick(() => { if (chatScrollbar.value?.setScrollTop) chatScrollbar.value.setScrollTop(99999) })
}

const formatTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const diff = new Date() - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const getImageBase = () => (baseURL || '').trim().replace(/\/api\/?$/, '') || 'http://localhost:8080'
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = getImageBase()
  if (url.startsWith('/')) return `${base}${url}`
  return `${base}/${url}`
}

onMounted(() => {
  loadHiddenConversations()
  if (store.getters['user/accessToken']) {
    loadOutlets()
    setInterval(() => {
      if (!store.getters['user/accessToken']) return
      if (currentConversation.value) selectConversation(currentConversation.value)
      else loadConversations()
    }, 5000)
  }
})
</script>

<style lang="scss" scoped>
.outlet-customer-chat-container {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}
.store-selector-bar {
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 10px;
}
.store-selector-bar .selector-label { font-size: 14px; color: #606266; }
.store-selector-bar .store-select { min-width: 220px; }
.store-selector-bar .admin-hint { font-size: 12px; color: #909399; }

.chat-list-aside {
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  .aside-header { padding: 15px; border-bottom: 1px solid #e4e7ed; display: flex; justify-content: space-between; align-items: center; background-color: #fff; h3 { margin: 0; font-size: 16px; font-weight: 600; } }
  .conversation-list {
    flex: 1;
    .conversation-item {
      display: flex;
      align-items: stretch;
      padding: 15px;
      border-bottom: 1px solid #e4e7ed;
      cursor: pointer;
      background-color: #fff;
      &:hover { background-color: #f5f7fa; }
      &.active { background-color: #e6f7ff; border-left: 3px solid #1890ff; }
      .item-main { flex: 1; min-width: 0; }
      .item-actions { margin-left: 8px; .el-button { opacity: 0; } &:hover .el-button { opacity: 1; } }
      .item-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; .user-info { display: flex; align-items: center; flex: 1; .user-avatar { margin-right: 10px; } .user-detail { flex: 1; .user-name { font-size: 14px; font-weight: 600; color: #303133; } .conversation-time { font-size: 12px; color: #909399; } } } }
      .item-content .last-message { font-size: 12px; color: #606266; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    }
  }
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  .chat-header { border-bottom: 1px solid #e4e7ed; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; background-color: #fff; .chat-header-info { display: flex; align-items: center; .header-detail { margin-left: 12px; .store-name { font-size: 16px; font-weight: 600; } .store-info { font-size: 12px; color: #909399; } } } }
  .chat-main { flex: 1; padding: 20px; background-color: #f5f7fa; overflow: hidden; min-height: 0; .message-scrollbar { height: 100%; } }
  .message-list .message-item {
    display: flex;
    margin-bottom: 20px;
    align-items: flex-start;
    .message-avatar { flex-shrink: 0; width: 40px; height: 40px; }
    .message-content { flex: 1; margin: 0 12px; .message-bubble { display: inline-block; padding: 10px 15px; border-radius: 8px; max-width: 70%; .message-text { font-size: 14px; line-height: 1.5; } .message-time { font-size: 12px; color: #909399; margin-top: 5px; } } }
    &.my-message { flex-direction: row-reverse; .message-content { text-align: right; .message-bubble { background-color: #95ec69; } } }
    &.other-message { .message-content .message-bubble { background-color: #fff; } }
  }
  .chat-footer { border-top: 1px solid #e4e7ed; padding: 15px 20px; background-color: #fff; .reply-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; } }
}
</style>
