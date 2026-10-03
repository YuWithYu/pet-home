<template>
  <view class="help-container">
    <view class="help-content">
      <view class="section-title">帮助与客服</view>
      
      <view class="help-section">
        <view class="section-subtitle">常见问题</view>
        <view class="faq-list">
          <view 
            v-for="(item, index) in faqList" 
            :key="index"
            class="faq-item"
            @click="toggleFaq(index)"
          >
            <view class="faq-question">
              <text>{{ item.question }}</text>
              <text class="faq-icon" :class="{ 'expanded': item.expanded }">▼</text>
            </view>
            <view v-if="item.expanded" class="faq-answer">
              <text>{{ item.answer }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <view class="help-section">
        <view class="section-subtitle">联系我们</view>
        <view class="contact-list">
          <view class="contact-item" @click="copyEmail">
            <view class="contact-icon">
              <image src="/static/images/邮箱.png" mode="aspectFit" class="contact-icon-img" />
            </view>
            <view class="contact-info">
              <view class="contact-label">客服邮箱</view>
              <view class="contact-value">csiyu8596@gmail.com</view>
            </view>
          </view>
          <view class="contact-item" @click="callService">
            <view class="contact-icon">
              <image src="/static/images/电话.png" mode="aspectFit" class="contact-icon-img" />
            </view>
            <view class="contact-info">
              <view class="contact-label">客服电话</view>
              <view class="contact-value">15278562765</view>
            </view>
          </view>
          <view class="contact-item" @click="openOnlineService">
            <view class="contact-icon">
              <image src="/static/images/在线咨询.png" mode="aspectFit" class="contact-icon-img" />
            </view>
            <view class="contact-info">
              <view class="contact-label">在线客服</view>
              <view class="contact-value">点击咨询</view>
            </view>
          </view>
        </view>
      </view>
      
      <view class="help-section">
        <view class="section-subtitle">服务时间</view>
        <view class="service-time">
          <text>工作日：9:00 - 18:00</text>
          <text>节假日：10:00 - 17:00</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Help',
  data() {
    return {
      faqList: [
        {
          question: '如何注册账号？',
          answer: '您可以通过手机号注册，也可以使用微信快捷登录。点击登录页面的"注册"按钮，按照提示完成注册即可。',
          expanded: false
        },
        {
          question: '如何修改个人信息？',
          answer: '进入"我的"页面，点击"个人资料"，即可修改头像、昵称、性别等信息。',
          expanded: false
        },
        {
          question: '如何添加宠物？',
          answer: '进入"我的"页面，点击"我的宠物"，然后点击"添加宠物"，按照提示填写宠物信息即可。',
          expanded: false
        },
        {
          question: '如何获得罐头？',
          answer: '您可以通过每日签到、完成任务、发布动态等方式获得罐头。罐头可用于兑换商品。',
          expanded: false
        },
        {
          question: '如何预约服务？',
          answer: '在首页或服务页面选择您需要的服务类型（如医疗、美容、上门服务等），选择合适的时间段进行预约。',
          expanded: false
        },
        {
          question: '如何申请退款？',
          answer: '进入"我的订单"，找到需要退款的订单，点击"申请退款"，填写退款原因并提交申请。我们会在1-3个工作日内处理。',
          expanded: false
        }
      ]
    }
  },
  methods: {
    toggleFaq(index) {
      this.faqList[index].expanded = !this.faqList[index].expanded
    },
    copyEmail() {
      uni.setClipboardData({
        data: 'csiyu8596@gmail.com',
        success: () => {
          uni.showToast({
            title: '邮箱已复制',
            icon: 'success'
          })
        }
      })
    },
    callService() {
      uni.makePhoneCall({
        phoneNumber: '15278562765',
        fail: () => {
          uni.showToast({
            title: '无法拨打电话',
            icon: 'none'
          })
        }
      })
    },
    openOnlineService() {
      uni.navigateTo({
        url: '/chat/customer-service?isPlatform=true'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.help-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.help-content {
  padding: 16rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.help-section {
  background: #fff;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 12rpx;
}

.section-subtitle {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.faq-list {
  margin-top: 12rpx;
}

.faq-item {
  border-bottom: 1rpx solid #f0f0f0;
  padding: 18rpx 0;
  
  &:last-child {
    border-bottom: none;
  }
}

.faq-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.faq-icon {
  font-size: 22rpx;
  color: #999;
  transition: transform 0.3s;
  
  &.expanded {
    transform: rotate(180deg);
  }
}

.faq-answer {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
  padding-left: 16rpx;
}

.contact-list {
  margin-top: 12rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 18rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

.contact-icon {
  width: 40rpx;
  height: 40rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}
.contact-icon-img {
  width: 100%;
  height: 100%;
}

.contact-info {
  flex: 1;
}

.contact-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 6rpx;
}

.contact-value {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.service-time {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}
</style>
