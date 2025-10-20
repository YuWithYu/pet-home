<template>
  <view class="detail-container">
    <!-- 顶部状态栏 -->
    <view class="status-bar">
      <view class="status-desc">{{ statusDesc }}</view>
    </view>

    <!-- 预约信息卡片 -->
    <view class="info-card">
      <view class="card-title">预约信息</view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">预约编号：</text>
          <text class="value">{{ appointmentDetail.id }}</text>
        </view>
        <view class="info-item">
          <text class="label">服务类型：</text>
          <text class="value">{{ getServiceTypeName(appointmentDetail.serviceType) }}</text>
        </view>
        <view class="info-item">
          <text class="label">预约日期：</text>
          <text class="value">{{ formatDate(appointmentDetail.date) }}</text>
        </view>
        <view class="info-item">
          <text class="label">预约时间：</text>
          <text class="value">{{ appointmentDetail.timeSlot }}</text>
        </view>
        <view class="info-item" v-if="appointmentDetail.location">
          <text class="label">服务地点：</text>
          <text class="value">{{ appointmentDetail.location }}</text>
        </view>
        <view class="info-item" v-if="appointmentDetail.remark">
          <text class="label">备注信息：</text>
          <text class="value">{{ appointmentDetail.remark }}</text>
        </view>
      </view>
    </view>

    <!-- 宠物信息卡片 -->
    <view class="info-card" v-if="petInfo">
      <view class="card-title">宠物信息</view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">宠物名称：</text>
          <text class="value">{{ petInfo.name }}</text>
        </view>
        <view class="info-item">
          <text class="label">宠物品种：</text>
          <text class="value">{{ petInfo.breed }}</text>
        </view>
        <view class="info-item">
          <text class="label">宠物年龄：</text>
          <text class="value">{{ petInfo.age }}岁</text>
        </view>
        <view class="info-item">
          <text class="label">宠物性别：</text>
          <text class="value">{{ petInfo.gender === 'male' ? '公' : '母' }}</text>
        </view>
      </view>
    </view>

    <!-- 核销信息卡片 -->
    <view class="info-card" v-if="appointmentDetail.verifyCode">
      <view class="card-title">核销信息</view>
      <view class="verify-info">
        <!-- 二维码区域 -->
        <view class="qr-code-section">
          <view class="qr-code-container">
            <view class="qr-code-square">
              <image 
                :src="qrCodeUrl" 
                mode="aspectFill" 
                class="qr-image"
                @error="onQrImageError"
              />
            </view>
            <view class="qr-code-text">核销码</view>
            <view class="qr-code-number">{{ appointmentDetail.verifyCode }}</view>
          </view>
        </view>
        
        <view class="verify-status" v-if="appointmentDetail.isVerified === 1">
          <text class="verified-text">✓ 已核销</text>
          <text class="verify-time">{{ formatDateTime(appointmentDetail.verifyTime) }}</text>
        </view>
        <view class="verify-status" v-else>
          <text class="unverified-text">○ 待核销</text>
        </view>
      </view>
    </view>

    <!-- 时间信息 -->
    <view class="info-card">
      <view class="card-title">时间信息</view>
      <view class="info-list">
        <view class="info-item">
          <text class="label">创建时间：</text>
          <text class="value">{{ formatDateTime(appointmentDetail.createTime) }}</text>
        </view>
        <view class="info-item">
          <text class="label">更新时间：</text>
          <text class="value">{{ formatDateTime(appointmentDetail.updateTime) }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <view 
        v-if="appointmentDetail.status === 'pending'" 
        class="action-button cancel-button"
        @tap="cancelAppointment"
      >
        <text class="button-text">取消预约</text>
      </view>
      <view 
        v-if="appointmentDetail.status === 'confirmed'" 
        class="action-button contact-button"
        @tap="contactService"
      >
        <text class="button-text">联系客服</text>
      </view>
      <view class="action-button primary-button" @tap="goBack">
        <text class="button-text">返回</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AppointmentDetail',
  
  data() {
    return {
      appointmentId: null,
      appointmentDetail: {},
      petInfo: null,
      loading: true,
      qrCodeData: '',
      qrCodeUrl: ''
    }
  },

  computed: {
    statusText() {
      const statusMap = {
        'pending': '待确认',
        'confirmed': '已确认',
        'cancelled': '已取消',
        'completed': '已完成'
      }
      return statusMap[this.appointmentDetail.status] || '未知状态'
    },

    statusDesc() {
      const descMap = {
        'pending': '您的预约正在等待确认，请耐心等待',
        'confirmed': '预约已确认，请按时到达',
        'cancelled': '预约已取消',
        'completed': '服务已完成'
      }
      return descMap[this.appointmentDetail.status] || ''
    },

    statusClass() {
      return `status-${this.appointmentDetail.status}`
    }
  },

  onLoad(options) {
    if (options.id) {
      this.appointmentId = options.id
      this.loadAppointmentDetail()
    } else {
      uni.showToast({
        title: '预约ID不能为空',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  },

  methods: {
    // 加载预约详情
    async loadAppointmentDetail() {
      try {
        this.loading = true
        uni.showLoading({
          title: '加载中...'
        })

        const res = await this.$api.getAppointmentDetail(this.appointmentId)
        
        if (res.code === 0 && res.data) {
          this.appointmentDetail = res.data
          
          // 生成二维码
          if (res.data.verifyCode) {
            this.generateQRCode()
          }
          
          // 加载宠物信息
          if (res.data.petId) {
            await this.loadPetInfo(res.data.petId)
          }
        } else {
          uni.showToast({
            title: '加载预约详情失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载预约详情失败:', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
        this.loading = false
      }
    },

    // 加载宠物信息
    async loadPetInfo(petId) {
      try {
        const res = await this.$api.getPetById(petId)
        if (res.code === 0 && res.data) {
          this.petInfo = res.data
        }
      } catch (error) {
        console.error('加载宠物信息失败:', error)
      }
    },

    // 获取服务类型名称
    getServiceTypeName(serviceType) {
      const typeMap = {
        'litter': '上门铲屎服务',
        'boarding': '寄养服务',
        'medical': '医疗服务',
        'grooming': '美容服务',
        'adoption': '领养服务',
        'door-cleaning': '上门清洁服务'
      }
      return typeMap[serviceType] || serviceType
    },

    // 格式化日期
    formatDate(dateArray) {
      if (!dateArray || dateArray.length < 3) return ''
      const [year, month, day] = dateArray
      return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`
    },

    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    // 取消预约
    cancelAppointment() {
      uni.showModal({
        title: '确认取消',
        content: '确定要取消这个预约吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({
                title: '处理中...'
              })

              const result = await this.$api.updateAppointmentStatus(this.appointmentId, 'cancelled')
              
              if (result.code === 0) {
                uni.hideLoading()
                uni.showToast({
                  title: '已取消预约',
                  icon: 'success'
                })
                // 重新加载详情
                this.loadAppointmentDetail()
              } else {
                uni.hideLoading()
                uni.showToast({
                  title: '取消失败',
                  icon: 'none'
                })
              }
            } catch (error) {
              console.error('取消预约失败:', error)
              uni.hideLoading()
              uni.showToast({
                title: error.message || '取消失败，请稍后重试',
                icon: 'none'
              })
            }
          }
        }
      })
    },

    // 联系客服
    contactService() {
      uni.showModal({
        title: '联系客服',
        content: '客服电话：400-123-4567\n工作时间：9:00-18:00',
        showCancel: false,
        confirmText: '知道了'
      })
    },

    // 返回
    goBack() {
      uni.navigateBack()
    },

    // 生成二维码
    generateQRCode() {
      try {
        const qrData = {
          type: 'verify',
          orderId: this.appointmentDetail.id,
          verifyCode: this.appointmentDetail.verifyCode,
          serviceType: this.appointmentDetail.serviceType,
          appointmentDate: this.formatDate(this.appointmentDetail.date),
          appointmentTime: this.appointmentDetail.timeSlot,
          location: this.appointmentDetail.location
        }
        this.qrCodeData = JSON.stringify(qrData)
        this.generateQRCodeUrl()
        console.log('生成核销二维码数据:', qrData)
      } catch (error) {
        console.error('生成二维码失败:', error)
      }
    },

    // 生成二维码URL
    generateQRCodeUrl() {
      try {
        const encodedData = encodeURIComponent(this.qrCodeData)
        this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodedData}`
        console.log('二维码URL:', this.qrCodeUrl)
      } catch (error) {
        console.error('生成二维码URL失败:', error)
        this.generateLocalQRCode()
      }
    },

    // 本地生成二维码（备用方案）
    generateLocalQRCode() {
      // 简化版的本地二维码生成
      const canvas = document.createElement('canvas')
      const ctx = canvas.getContext('2d')
      canvas.width = 200
      canvas.height = 200
      
      // 白色背景
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, 200, 200)
      
      // 黑色图案
      ctx.fillStyle = '#000000'
      for (let i = 0; i < 25; i++) {
        for (let j = 0; j < 25; j++) {
          if ((i + j) % 3 === 0 || (i * j) % 5 === 0) {
            ctx.fillRect(i * 8, j * 8, 8, 8)
          }
        }
      }
      
      this.qrCodeUrl = canvas.toDataURL()
    },

    // 二维码图片加载失败处理
    onQrImageError(e) {
      console.error('二维码图片加载失败:', e)
      this.generateLocalQRCode()
    }
  }
}
</script>

<style lang="scss" scoped>
.detail-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

// 状态栏
.status-bar {
  background: linear-gradient(135deg, #ff6b35, #ff8c42);
  padding: 60rpx 30rpx 40rpx;
  color: #ffffff;
  text-align: center;
}

.status-icon {
  margin-bottom: 20rpx;
}

.status-text {
  font-size: 36rpx;
  font-weight: bold;
}

.status-desc {
  font-size: 28rpx;
  opacity: 1;
  font-weight: 500;
}

// 信息卡片
.info-card {
  background-color: #ffffff;
  margin: 20rpx 30rpx;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 30rpx;
  padding-bottom: 20rpx;
  border-bottom: 2rpx solid #f0f0f0;
}

.info-list {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  font-size: 28rpx;
  color: #666666;
  min-width: 140rpx;
}

.info-item .value {
  font-size: 28rpx;
  color: #333333;
  font-weight: 500;
  flex: 1;
  text-align: right;
}

// 核销信息
.verify-info {
  display: flex;
  flex-direction: column;
}

// 二维码区域
.qr-code-section {
  display: flex;
  justify-content: center;
  margin: 30rpx 0;
}

.qr-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-code-square {
  width: 300rpx;
  height: 300rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.qr-image {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
}

.qr-code-text {
  font-size: 24rpx;
  color: #666666;
  margin-top: 20rpx;
  font-weight: 500;
  text-align: center;
}

.qr-code-number {
  font-size: 20rpx;
  color: #999999;
  margin-top: 5rpx;
  text-align: center;
}

.verify-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15rpx 0;
}

.verified-text {
  color: #52c41a;
  font-weight: bold;
}

.unverified-text {
  color: #faad14;
  font-weight: bold;
}

.verify-time {
  font-size: 24rpx;
  color: #999999;
}

// 操作按钮
.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  padding: 20rpx 30rpx;
  border-top: 1rpx solid #e8e8e8;
  display: flex;
  gap: 20rpx;
}

.action-button {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: bold;
}

.primary-button {
  background-color: #ff6b35;
  color: #ffffff;
}

.cancel-button {
  background-color: #ff4d4f;
  color: #ffffff;
}

.contact-button {
  background-color: #1890ff;
  color: #ffffff;
}

.button-text {
  font-size: 28rpx;
  font-weight: bold;
}

// 状态样式
.status-pending {
  .status-text {
    color: #faad14;
  }
}

.status-confirmed {
  .status-text {
    color: #52c41a;
  }
}

.status-cancelled {
  .status-text {
    color: #ff4d4f;
  }
}

.status-completed {
  .status-text {
    color: #1890ff;
  }
}
</style>
