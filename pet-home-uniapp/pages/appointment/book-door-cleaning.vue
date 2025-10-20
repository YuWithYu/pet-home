<template>
  <view class="booking-container">

    <!-- 预约表单 -->
    <view class="booking-form">
      <!-- 选择宠物 -->
      <view class="form-section">
        <view class="section-title">选择宠物</view>
        <view class="pet-selector" @click="showPetPicker = true">
          <view class="selector-label">{{ selectedPet ? selectedPet.name : '请选择宠物' }}</view>
          <view class="selector-arrow">›</view>
        </view>
      </view>

      <!-- 预约日期 -->
      <view class="form-section">
        <view class="section-title">预约日期</view>
        <picker mode="date" :value="formData.date" :start="todayDate" :end="maxDate" @change="onDateChange">
          <view class="date-picker">
            <view class="picker-value">{{ formData.date || '请选择日期' }}</view>
            <view class="picker-arrow">›</view>
          </view>
        </picker>
      </view>

      <!-- 预约时间段 -->
      <view class="form-section">
        <view class="section-title">预约时间</view>
        <view class="time-slots">
          <view
            v-for="slot in timeSlots"
            :key="slot.value"
            :class="['time-slot', { 'selected': formData.timeSlot === slot.value }]"
            @click="selectTimeSlot(slot.value)"
          >
            {{ slot.label }}
          </view>
        </view>
      </view>

      <!-- 服务地址 -->
      <view class="form-section">
        <view class="section-title">服务地址</view>
        <view class="address-input">
          <textarea
            v-model="formData.location"
            placeholder="请输入详细地址（包括门牌号）"
            class="address-textarea"
            maxlength="200"
          />
        </view>
      </view>

      <!-- 联系人信息 -->
      <view class="form-section">
        <view class="section-title">联系人信息</view>
        <view class="contact-inputs">
          <view class="input-row">
            <view class="input-label">姓名</view>
            <input
              v-model="formData.contactName"
              placeholder="请输入联系人姓名"
              class="input-field"
            />
          </view>
          <view class="input-row">
            <view class="input-label">电话</view>
            <input
              v-model="formData.contactPhone"
              type="number"
              placeholder="请输入联系电话"
              class="input-field"
            />
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-section">
        <view class="section-title">备注信息（选填）</view>
        <view class="remark-input">
          <textarea
            v-model="formData.remark"
            placeholder="如有特殊要求请在此说明"
            class="remark-textarea"
            maxlength="200"
          />
        </view>
      </view>
    </view>

    <!-- 价格汇总 -->
    <view class="price-summary">
      <view class="summary-row">
        <view class="summary-label">服务费用</view>
        <view class="summary-value">¥99</view>
      </view>
      <view class="summary-total">
        <view class="total-label">合计</view>
        <view class="total-value">¥99</view>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button class="submit-button" @click="submitBooking">确认预约</button>
    </view>

    <!-- 宠物选择弹窗 -->
    <view v-if="showPetPicker" class="picker-modal" @click="showPetPicker = false">
      <view class="picker-content" @click.stop>
        <view class="picker-header">
          <view class="picker-title">选择宠物</view>
          <view class="picker-close" @click="showPetPicker = false">✕</view>
        </view>
        <view class="picker-list">
          <view
            v-for="pet in pets"
            :key="pet.id"
            :class="['pet-item', { 'selected': selectedPet && selectedPet.id === pet.id }]"
            @click="selectPet(pet)"
          >
            <view class="pet-info">
              <view class="pet-name">{{ pet.name }}</view>
              <view class="pet-breed">{{ pet.breed }}</view>
            </view>
            <view v-if="selectedPet && selectedPet.id === pet.id" class="pet-check">✓</view>
          </view>
        </view>
        <view class="add-pet-btn" @click="goToAddPet">
          + 添加新宠物
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'BookDoorCleaning',

  data() {
    return {
      formData: {
        petId: null,
        date: '',
        timeSlot: '',
        location: '',
        contactName: '',
        contactPhone: '',
        remark: '',
        serviceType: 'door-cleaning',
        price: 99
      },
      timeSlots: [
        { label: '08:00-10:00', value: '08:00-10:00' },
        { label: '10:00-12:00', value: '10:00-12:00' },
        { label: '12:00-14:00', value: '12:00-14:00' },
        { label: '14:00-16:00', value: '14:00-16:00' },
        { label: '16:00-18:00', value: '16:00-18:00' },
        { label: '18:00-20:00', value: '18:00-20:00' }
      ],
      pets: [],
      selectedPet: null,
      showPetPicker: false,
      todayDate: '',
      maxDate: ''
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn'])
  },

  onLoad(options) {
    // 设置页面标题为服务名称
    if (options.serviceName) {
      uni.setNavigationBarTitle({
        title: decodeURIComponent(options.serviceName)
      })
    } else {
      uni.setNavigationBarTitle({
        title: '上门铲屎服务'
      })
    }

    // 检查登录状态
    if (!this.isLoggedIn) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/user/login'
        })
      }, 1500)
      return
    }

    // 从本地存储加载用户信息（备用方案）
    const localUserInfo = uni.getStorageSync('userInfo')
    if (localUserInfo && !this.userInfo) {
      this.userInfo = localUserInfo
    }

    this.initData()
    this.loadPets()
  },
  onShow() {
    // 返回本页时刷新一次宠物列表，确保新添加的宠物能显示
    this.loadPets()
  },

  methods: {
    // 初始化数据
    initData() {
      // 设置今天的日期为最小日期
      const today = new Date()
      this.todayDate = this.formatDate(today)
      
      // 设置最大日期为一个月后
      const maxDate = new Date()
      maxDate.setMonth(maxDate.getMonth() + 1)
      this.maxDate = this.formatDate(maxDate)
      
      // 设置联系人信息为用户信息
      if (this.userInfo) {
        this.formData.contactName = this.userInfo.nickname || this.userInfo.username || ''
        this.formData.contactPhone = this.userInfo.phone || ''
      }
    },

    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    // 加载宠物列表（优先本地存储，兼容后端接口）
    async loadPets() {
      try {
        // 1) 先从本地存储读取宠物档案
        const localList = uni.getStorageSync('petList') || []
        if (Array.isArray(localList) && localList.length > 0) {
          this.pets = localList.map(p => ({
            id: p.id,
            name: p.name,
            breed: p.breed,
            avatar: p.avatar,
            gender: p.gender,
            weight: p.weight
          }))
        } else {
          // 2) 兼容：若本地没有，再尝试后端接口（若可用）
          if (this.$api && this.userInfo && this.userInfo.uid) {
            try {
              const res = await this.$api.getPetPage(1, 100, this.userInfo.uid)
              if (res && res.code === 0 && res.data && res.data.records) {
                this.pets = res.data.records
              }
            } catch (err) {
              // 后端不可用时忽略，保持空列表
              console.warn('后端宠物接口不可用，使用空列表作为回退。')
            }
          }
        }

        // 自动选中：若只有一只宠物
        if (this.pets.length === 1) {
          this.selectedPet = this.pets[0]
          this.formData.petId = this.pets[0].id
        }
      } catch (error) {
        console.error('加载宠物列表失败:', error)
        this.pets = []
      }
    },

    // 日期变更
    onDateChange(e) {
      this.formData.date = e.detail.value
    },

    // 选择时间段
    selectTimeSlot(slot) {
      this.formData.timeSlot = slot
    },

    // 选择宠物
    selectPet(pet) {
      this.selectedPet = pet
      this.formData.petId = pet.id
      this.showPetPicker = false
    },

    // 前往添加宠物页面
    goToAddPet() {
      // 直接进入添加宠物档案第一步
      uni.navigateTo({
        url: '/pages/user/add-pet-basic'
      })
    },
    
    // 获取兼容的宠物ID（确保在Integer范围内）
    getCompatiblePetId(originalId) {
      const maxInt = 2147483647
      const minInt = 1
      
      // 如果ID已经在Integer范围内，直接返回
      if (originalId >= minInt && originalId <= maxInt) {
        return originalId
      }
      
      // 如果ID超出范围，生成一个基于原ID的兼容ID
      // 使用原ID的哈希值来确保同一宠物总是得到相同的兼容ID
      let hash = 0
      const str = String(originalId)
      for (let i = 0; i < str.length; i++) {
        const char = str.charCodeAt(i)
        hash = ((hash << 5) - hash) + char
        hash = hash & hash // 转换为32位整数
      }
      
      // 将哈希值映射到Integer范围内
      const compatibleId = Math.abs(hash) % (maxInt - minInt + 1) + minInt
      console.log(`宠物ID转换: ${originalId} -> ${compatibleId}`)
      return compatibleId
    },

    // 表单验证
    validateForm() {
      if (!this.formData.petId) {
        uni.showToast({
          title: '请选择宠物',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.date) {
        uni.showToast({
          title: '请选择预约日期',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.timeSlot) {
        uni.showToast({
          title: '请选择预约时间',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.location || this.formData.location.trim() === '') {
        uni.showToast({
          title: '请输入服务地址',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.contactName || this.formData.contactName.trim() === '') {
        uni.showToast({
          title: '请输入联系人姓名',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.contactPhone || this.formData.contactPhone.trim() === '') {
        uni.showToast({
          title: '请输入联系电话',
          icon: 'none'
        })
        return false
      }

      // 验证手机号格式
      const phoneReg = /^1[3-9]\d{9}$/
      if (!phoneReg.test(this.formData.contactPhone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        })
        return false
      }

      return true
    },

    // 提交预约
    async submitBooking() {
      if (!this.validateForm()) {
        return
      }

      try {
        uni.showLoading({
          title: '提交中...'
        })

        // 检查用户信息
        if (!this.userInfo || !this.userInfo.uid) {
          uni.hideLoading()
          uni.showToast({
            title: '用户信息异常，请重新登录',
            icon: 'none'
          })
          return
        }
        
        const appointmentData = {
          userId: this.userInfo.uid,
          petId: this.getCompatiblePetId(this.formData.petId), // 确保ID在Integer范围内
          serviceType: this.formData.serviceType,
          date: this.formData.date,
          timeSlot: this.formData.timeSlot,
          location: this.formData.location,
          contactName: this.formData.contactName,
          contactPhone: this.formData.contactPhone,
          remark: this.formData.remark,
          price: this.formData.price,
          status: 'pending'
        }
        
        console.log('用户信息:', this.userInfo)
        console.log('提交预约数据:', appointmentData)

        const res = await this.$api.createAppointment(appointmentData)

        if (res.code === 0) {
          uni.hideLoading()
          
          // 跳转到预约成功页面
          const orderInfo = {
            orderNumber: res.data?.id || Date.now().toString(),
            verifyCode: res.data?.verifyCode || '',
            orderTime: new Date().toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            }).replace(/\//g, '-'),
            location: this.formData.location || '广州南方学院店',
            serviceType: this.formData.serviceType || 'litter',
            petName: this.selectedPet?.name || 'YU',
            appointmentDate: this.formData.date,
            appointmentTime: this.formData.timeSlot
          }
          
          const queryParams = Object.keys(orderInfo)
            .map(key => `${key}=${encodeURIComponent(orderInfo[key])}`)
            .join('&')
          
          uni.redirectTo({
            url: `/pages/booking/success?${queryParams}`
          })
        } else {
          uni.hideLoading()
          uni.showToast({
            title: res.msg || '预约失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('提交预约失败:', error)
        uni.hideLoading()
        uni.showToast({
          title: '预约失败，请稍后重试',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.booking-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-bottom: 200rpx;
}


/* 表单区域 */
.booking-form {
  .form-section {
    background-color: white;
    padding: 30rpx;
    margin-bottom: 20rpx;
    
    .section-title {
      font-size: 28rpx;
      color: #333;
      font-weight: bold;
      margin-bottom: 20rpx;
    }
  }
}

/* 宠物选择器 */
.pet-selector, .date-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 30rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  
  .selector-label, .picker-value {
    font-size: 28rpx;
    color: #333;
  }
  
  .selector-arrow, .picker-arrow {
    font-size: 40rpx;
    color: #999;
  }
}

/* 时间段选择 */
.time-slots {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  
  .time-slot {
    padding: 24rpx;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    text-align: center;
    font-size: 26rpx;
    color: #666;
    border: 2rpx solid transparent;
    
    &.selected {
      background-color: #fff7e6;
      color: #ff6b35;
      border-color: #ff6b35;
      font-weight: bold;
    }
  }
}

/* 地址输入 */
.address-input, .remark-input {
  .address-textarea, .remark-textarea {
    width: 100%;
    min-height: 150rpx;
    padding: 20rpx;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    font-size: 28rpx;
    line-height: 1.6;
  }
}

/* 联系人输入 */
.contact-inputs {
  background-color: white;
  position: relative;
  z-index: 1;
  
  .input-row {
    display: flex;
    align-items: center;
    padding: 20rpx 0;
    border-bottom: 1rpx solid #f0f0f0;
    background-color: white;
    position: relative;
    z-index: 1;
    
    &:last-child {
      border-bottom: none;
    }
    
    .input-label {
      width: 120rpx;
      font-size: 28rpx;
      color: #333;
      flex-shrink: 0;
      background-color: white;
      z-index: 1;
    }
    
    .input-field {
      flex: 1;
      font-size: 28rpx;
      padding-left: 20rpx;
      background-color: transparent;
      border: none;
      color: #333;
      opacity: 1;
      z-index: 1;
    }
  }
}

/* 价格汇总 */
.price-summary {
  background-color: white;
  padding: 30rpx;
  margin-bottom: 20rpx;
  
  .summary-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20rpx;
    font-size: 28rpx;
    color: #666;
    
    .summary-value {
      color: #333;
    }
  }
  
  .summary-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 20rpx;
    border-top: 1rpx solid #f0f0f0;
    
    .total-label {
      font-size: 30rpx;
      color: #333;
      font-weight: bold;
    }
    
    .total-value {
      font-size: 40rpx;
      color: #ff6b35;
      font-weight: bold;
    }
  }
}

/* 提交按钮 */
.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background-color: white;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  z-index: 9999;
  
  .submit-button {
    width: 100%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 20rpx;
    border-radius: 40rpx;
    font-size: 28rpx;
    font-weight: bold;
    
    &:active {
      opacity: 0.9;
    }
  }
}

/* 宠物选择弹窗 */
.picker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 1000;
  
  .picker-content {
    width: 100%;
    max-height: 80vh;
    background-color: white;
    border-radius: 40rpx 40rpx 0 0;
    overflow: hidden;
    
    .picker-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30rpx;
      border-bottom: 1rpx solid #f0f0f0;
      
      .picker-title {
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
      }
      
      .picker-close {
        font-size: 40rpx;
        color: #999;
        padding: 10rpx;
      }
    }
    
    .picker-list {
      max-height: 500rpx;
      overflow-y: auto;
      
      .pet-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx;
        border-bottom: 1rpx solid #f0f0f0;
        
        &.selected {
          background-color: #fff7e6;
        }
        
        .pet-info {
          flex: 1;
          
          .pet-name {
            font-size: 30rpx;
            color: #333;
            font-weight: bold;
            margin-bottom: 8rpx;
          }
          
          .pet-breed {
            font-size: 24rpx;
            color: #999;
          }
        }
        
        .pet-check {
          color: #ff6b35;
          font-size: 40rpx;
          font-weight: bold;
        }
      }
    }
    
    .add-pet-btn {
      padding: 30rpx;
      text-align: center;
      color: #667eea;
      font-size: 28rpx;
      font-weight: bold;
      border-top: 1rpx solid #f0f0f0;
    }
  }
}
</style>

