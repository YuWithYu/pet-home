<template>
  <view class="consultation-container">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">专业医师</text>
    </view>

    <!-- 标签页导航 -->
    <view class="tab-navigation">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'doctors' }"
        @tap="switchTab('doctors')"
      >
        <text class="tab-text">在线医生({{ onlineDoctors.length }})</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'records' }"
        @tap="switchTab('records')"
      >
        <text class="tab-text">问诊记录({{ consultationRecords.length }})</text>
      </view>
    </view>

    <!-- 在线医生列表 -->
    <view v-if="activeTab === 'doctors'" class="doctors-list">
      <view 
        class="doctor-card" 
        v-for="(doctor, index) in onlineDoctors" 
        :key="doctor.id || index"
      >
        <!-- 医生头像 -->
        <view class="doctor-avatar">
          <image :src="doctor.avatar" mode="aspectFill" @error="onAvatarError" />
        </view>

        <!-- 医生信息 -->
        <view class="doctor-info">
          <view class="doctor-name">{{ doctor.name }}</view>
          <view class="doctor-specialty">{{ doctor.specialty }}</view>
          <view class="doctor-expertise">{{ doctor.expertise }}</view>
          <view class="doctor-rating">
            <text class="rating-label">评分:</text>
            <text class="rating-value">{{ doctor.rating }}</text>
          </view>
        </view>

        <!-- 在线咨询按钮 -->
        <view class="consultation-btn" @tap="startConsultation(doctor)">
          <text class="btn-text">在线咨询</text>
        </view>
      </view>
    </view>

    <!-- 问诊记录列表 -->
    <view v-if="activeTab === 'records'" class="records-list">
      <view 
        class="record-card" 
        v-for="(record, index) in consultationRecords" 
        :key="record.id || index"
      >
        <view class="record-header">
          <view class="doctor-info">
            <image :src="record.doctorAvatar" mode="aspectFill" class="doctor-avatar-small" />
            <view class="doctor-details">
              <text class="doctor-name">{{ record.doctorName }}</text>
              <text class="consultation-time">{{ record.consultationTime }}</text>
            </view>
          </view>
          <view class="record-status" :class="record.status">
            <text class="status-text">{{ getStatusText(record.status) }}</text>
          </view>
        </view>
        <view class="record-content">
          <text class="record-description">{{ record.description }}</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-actions">
      <view class="action-btn service-notice" @tap="showServiceNotice">
        <text class="btn-text">服务须知</text>
      </view>
      <view class="action-btn edit-info" @tap="editUserInfo">
        <text class="btn-text">编辑信息</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'OnlineConsultation',
  
  data() {
    return {
      activeTab: 'doctors',
      onlineDoctors: [
        {
          id: 1,
          name: '禾小春',
          specialty: '大类',
          expertise: '擅长猫犬科传染病,寄生虫,肠胃炎,老年性疾病,内分泌疾病',
          rating: 4.7,
          avatar: '/static/images/doctor-1.png'
        },
        {
          id: 2,
          name: '李达奔',
          specialty: '犬类异宠类',
          expertise: '认证爱猫医生擅长猫专科、猫行为学,猫普内科、影像科、猫眼科',
          rating: 4.3,
          avatar: '/static/images/doctor-2.png'
        },
        {
          id: 3,
          name: '陈雨泽',
          specialty: '猫科',
          expertise: '新瑞鹏浙闽一区技术经理 院长擅长犬猫,异宠综合内科疾病的精确诊断及有效治疗',
          rating: 4.5,
          avatar: '/static/images/doctor-3.png'
        },
        {
          id: 4,
          name: '禾雨泽',
          specialty: '犬类猫科',
          expertise: '擅长猫犬科传染病,寄生虫,肠胃炎,老年性疾病,内分泌疾病',
          rating: 4.7,
          avatar: '/static/images/doctor-4.png'
        },
        {
          id: 5,
          name: '陈越泽',
          specialty: '猫科',
          expertise: '专业宠物医师，擅长猫咪疾病诊断和治疗',
          rating: 4.6,
          avatar: '/static/images/doctor-5.png'
        }
      ],
      consultationRecords: [
        {
          id: 1,
          doctorName: '禾小春',
          doctorAvatar: '/static/images/doctor-1.png',
          consultationTime: '2024-01-15 14:30',
          description: '猫咪食欲不振，咨询饮食建议',
          status: 'completed'
        }
      ]
    }
  },

  onLoad() {
    this.loadDoctors()
    this.loadConsultationRecords()
  },

  methods: {
    switchTab(tab) {
      this.activeTab = tab
    },

    async loadDoctors() {
      try {
        // 这里可以调用API获取在线医生列表
        console.log('加载在线医生列表')
      } catch (error) {
        console.error('加载医生列表失败:', error)
      }
    },

    async loadConsultationRecords() {
      try {
        // 这里可以调用API获取问诊记录
        console.log('加载问诊记录')
      } catch (error) {
        console.error('加载问诊记录失败:', error)
      }
    },

    startConsultation(doctor) {
      uni.showModal({
        title: '开始咨询',
        content: `确定要开始与${doctor.name}医生的在线咨询吗？`,
        success: (res) => {
          if (res.confirm) {
            // 跳转到咨询聊天页面
            uni.navigateTo({
              url: `/pages/chat/consultation?doctorId=${doctor.id}&doctorName=${doctor.name}`
            })
          }
        }
      })
    },

    showServiceNotice() {
      uni.showModal({
        title: '服务须知',
        content: '1. 在线咨询时间为每日9:00-21:00\n2. 咨询费用按分钟计费\n3. 请详细描述宠物症状\n4. 紧急情况请及时就医',
        showCancel: false
      })
    },

    editUserInfo() {
      uni.navigateTo({
        url: '/pages/user/profile'
      })
    },

    getStatusText(status) {
      const statusMap = {
        'completed': '已完成',
        'ongoing': '进行中',
        'pending': '待回复'
      }
      return statusMap[status] || '未知'
    },

    onAvatarError(e) {
      console.log('头像加载失败:', e)
      // 可以设置默认头像
    }
  }
}
</script>

<style lang="scss" scoped>
.consultation-container {
  min-height: 100vh;
  background-color: #ffffff;
  padding-bottom: 120rpx;
}

.page-header {
  padding: 40rpx 0 20rpx;
  text-align: center;
  background-color: #ffffff;
  
  .page-title {
    font-size: 36rpx;
    font-weight: bold;
    color: #333333;
  }
}

.tab-navigation {
  display: flex;
  background-color: #ffffff;
  border-bottom: 1rpx solid #f0f0f0;
  
  .tab-item {
    flex: 1;
    padding: 30rpx 0;
    text-align: center;
    position: relative;
    
    &.active {
      .tab-text {
        color: #4FC3F7;
        font-weight: bold;
      }
      
      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background-color: #4FC3F7;
        border-radius: 2rpx;
      }
    }
    
    .tab-text {
      font-size: 28rpx;
      color: #666666;
    }
  }
}

.doctors-list {
  padding: 20rpx;
  
  .doctor-card {
    display: flex;
    align-items: center;
    background-color: #ffffff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
    
    .doctor-avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 60rpx;
      overflow: hidden;
      margin-right: 24rpx;
      background-color: #f8f8f8;
      
      image {
        width: 100%;
        height: 100%;
      }
    }
    
    .doctor-info {
      flex: 1;
      
      .doctor-name {
        font-size: 32rpx;
        font-weight: bold;
        color: #333333;
        margin-bottom: 8rpx;
        display: block;
      }
      
      .doctor-specialty {
        font-size: 24rpx;
        color: #666666;
        margin-bottom: 12rpx;
        display: block;
      }
      
      .doctor-expertise {
        font-size: 24rpx;
        color: #999999;
        line-height: 1.4;
        margin-bottom: 12rpx;
        display: block;
      }
      
      .doctor-rating {
        display: flex;
        align-items: center;
        
        .rating-label {
          font-size: 24rpx;
          color: #666666;
          margin-right: 8rpx;
        }
        
        .rating-value {
          font-size: 24rpx;
          color: #FF6B35;
          font-weight: bold;
        }
      }
    }
    
    .consultation-btn {
      padding: 16rpx 24rpx;
      border: 2rpx solid #FF6B35;
      border-radius: 8rpx;
      background-color: #ffffff;
      
      .btn-text {
        font-size: 24rpx;
        color: #FF6B35;
      }
    }
  }
}

.records-list {
  padding: 20rpx;
  
  .record-card {
    background-color: #ffffff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
    
    .record-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;
      
      .doctor-info {
        display: flex;
        align-items: center;
        
        .doctor-avatar-small {
          width: 60rpx;
          height: 60rpx;
          border-radius: 30rpx;
          margin-right: 16rpx;
        }
        
        .doctor-details {
          .doctor-name {
            font-size: 28rpx;
            font-weight: bold;
            color: #333333;
            display: block;
            margin-bottom: 4rpx;
          }
          
          .consultation-time {
            font-size: 22rpx;
            color: #999999;
            display: block;
          }
        }
      }
      
      .record-status {
        padding: 8rpx 16rpx;
        border-radius: 20rpx;
        
        &.completed {
          background-color: #E8F5E8;
          
          .status-text {
            color: #52C41A;
          }
        }
        
        &.ongoing {
          background-color: #E6F7FF;
          
          .status-text {
            color: #1890FF;
          }
        }
        
        &.pending {
          background-color: #FFF7E6;
          
          .status-text {
            color: #FA8C16;
          }
        }
        
        .status-text {
          font-size: 22rpx;
        }
      }
    }
    
    .record-content {
      .record-description {
        font-size: 26rpx;
        color: #666666;
        line-height: 1.4;
      }
    }
  }
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  gap: 20rpx;
  
  .action-btn {
    flex: 1;
    padding: 24rpx 0;
    border-radius: 8rpx;
    text-align: center;
    
    &.service-notice {
      background-color: #ffffff;
      border: 2rpx solid #FF6B35;
      
      .btn-text {
        color: #FF6B35;
        font-size: 28rpx;
      }
    }
    
    &.edit-info {
      background-color: #FF6B35;
      
      .btn-text {
        color: #ffffff;
        font-size: 28rpx;
      }
    }
  }
}
</style>
