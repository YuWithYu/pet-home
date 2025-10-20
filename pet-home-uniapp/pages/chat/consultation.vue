<template>
  <view class="chat-container">
    <!-- 聊天头部 -->
    <view class="chat-header">
      <view class="doctor-info">
        <image :src="doctorInfo.avatar" mode="aspectFill" class="doctor-avatar" />
        <view class="doctor-details">
          <text class="doctor-name">{{ doctorInfo.name }}</text>
          <text class="doctor-specialty">{{ doctorInfo.specialty }}</text>
        </view>
      </view>
      <view class="chat-status">
        <text class="status-text">在线</text>
      </view>
    </view>

    <!-- 聊天消息列表 -->
    <scroll-view 
      class="message-list" 
      scroll-y 
      :scroll-top="scrollTop"
      scroll-with-animation
    >
      <view 
        class="message-item" 
        v-for="(message, index) in messages" 
        :key="index"
        :class="{ 'user-message': message.isUser, 'doctor-message': !message.isUser }"
      >
        <view class="message-avatar" v-if="!message.isUser">
          <image :src="doctorInfo.avatar" mode="aspectFill" />
        </view>
        <view class="message-content">
          <view class="message-bubble">
            <text class="message-text">{{ message.content }}</text>
          </view>
          <view class="message-time">{{ message.time }}</view>
        </view>
        <view class="message-avatar" v-if="message.isUser">
          <image :src="userAvatar" mode="aspectFill" />
        </view>
      </view>
    </scroll-view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-container">
        <input 
          class="message-input" 
          v-model="inputMessage" 
          placeholder="请输入您的问题..."
          @confirm="sendMessage"
        />
        <view class="send-btn" @tap="sendMessage">
          <text class="send-text">发送</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'ConsultationChat',
  
  data() {
    return {
      doctorInfo: {
        id: 1,
        name: '禾小春',
        specialty: '大类',
        avatar: '/static/images/doctor-1.png'
      },
      userAvatar: '/static/images/user-avatar.png',
      messages: [
        {
          isUser: false,
          content: '您好！我是禾小春医生，很高兴为您服务。请详细描述一下您宠物的症状。',
          time: '14:30'
        }
      ],
      inputMessage: '',
      scrollTop: 0
    }
  },

  onLoad(options) {
    if (options.doctorId && options.doctorName) {
      this.doctorInfo.id = options.doctorId
      this.doctorInfo.name = options.doctorName
    }
    this.loadChatHistory()
  },

  methods: {
    sendMessage() {
      if (!this.inputMessage.trim()) {
        return
      }

      // 添加用户消息
      const userMessage = {
        isUser: true,
        content: this.inputMessage,
        time: this.getCurrentTime()
      }
      this.messages.push(userMessage)
      
      const userInput = this.inputMessage
      this.inputMessage = ''
      
      // 滚动到底部
      this.scrollToBottom()
      
      // 模拟医生回复
      setTimeout(() => {
        this.simulateDoctorReply(userInput)
      }, 1000)
    },

    simulateDoctorReply(userInput) {
      // 简单的回复逻辑
      let reply = ''
      if (userInput.includes('食欲') || userInput.includes('不吃')) {
        reply = '食欲不振可能有多种原因，建议您观察宠物的精神状态、体温是否正常，以及是否有其他症状。可以尝试更换食物或少量多餐。'
      } else if (userInput.includes('拉稀') || userInput.includes('腹泻')) {
        reply = '腹泻需要引起重视，建议暂时禁食12小时，只给水。如果症状持续或加重，请及时就医。'
      } else if (userInput.includes('呕吐')) {
        reply = '呕吐可能是消化不良或肠胃问题，建议观察呕吐物的颜色和频率，如果频繁呕吐请及时就医。'
      } else {
        reply = '感谢您的描述。为了更好地帮助您，建议您提供更详细的症状信息，包括发病时间、症状持续时间、是否有其他异常等。'
      }

      const doctorMessage = {
        isUser: false,
        content: reply,
        time: this.getCurrentTime()
      }
      this.messages.push(doctorMessage)
      this.scrollToBottom()
    },

    getCurrentTime() {
      const now = new Date()
      return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
    },

    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollTop = 99999
      })
    },

    loadChatHistory() {
      // 这里可以加载历史聊天记录
      console.log('加载聊天历史')
    }
  }
}
</script>

<style lang="scss" scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8f8f8;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #ffffff;
  border-bottom: 1rpx solid #f0f0f0;
  
  .doctor-info {
    display: flex;
    align-items: center;
    
    .doctor-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 40rpx;
      margin-right: 20rpx;
    }
    
    .doctor-details {
      .doctor-name {
        font-size: 32rpx;
        font-weight: bold;
        color: #333333;
        display: block;
        margin-bottom: 4rpx;
      }
      
      .doctor-specialty {
        font-size: 24rpx;
        color: #666666;
        display: block;
      }
    }
  }
  
  .chat-status {
    .status-text {
      font-size: 24rpx;
      color: #52C41A;
    }
  }
}

.message-list {
  flex: 1;
  padding: 20rpx;
  
  .message-item {
    display: flex;
    margin-bottom: 30rpx;
    
    &.user-message {
      flex-direction: row-reverse;
      
      .message-content {
        align-items: flex-end;
        
        .message-bubble {
          background-color: #4FC3F7;
          
          .message-text {
            color: #ffffff;
          }
        }
      }
    }
    
    &.doctor-message {
      .message-content {
        align-items: flex-start;
        
        .message-bubble {
          background-color: #ffffff;
          
          .message-text {
            color: #333333;
          }
        }
      }
    }
    
    .message-avatar {
      width: 60rpx;
      height: 60rpx;
      border-radius: 30rpx;
      margin: 0 20rpx;
      
      image {
        width: 100%;
        height: 100%;
      }
    }
    
    .message-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      max-width: 70%;
      
      .message-bubble {
        padding: 20rpx 24rpx;
        border-radius: 16rpx;
        margin-bottom: 8rpx;
        
        .message-text {
          font-size: 28rpx;
          line-height: 1.4;
        }
      }
      
      .message-time {
        font-size: 22rpx;
        color: #999999;
      }
    }
  }
}

.input-area {
  background-color: #ffffff;
  border-top: 1rpx solid #f0f0f0;
  padding: 20rpx 30rpx;
  
  .input-container {
    display: flex;
    align-items: center;
    background-color: #f8f8f8;
    border-radius: 24rpx;
    padding: 16rpx 24rpx;
    
    .message-input {
      flex: 1;
      font-size: 28rpx;
      color: #333333;
      background-color: transparent;
      border: none;
      outline: none;
    }
    
    .send-btn {
      padding: 12rpx 24rpx;
      background-color: #4FC3F7;
      border-radius: 20rpx;
      margin-left: 20rpx;
      
      .send-text {
        font-size: 24rpx;
        color: #ffffff;
      }
    }
  }
}
</style>
