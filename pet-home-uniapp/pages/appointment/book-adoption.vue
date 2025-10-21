<template>
  <view class="booking-container">

    <!-- 预约表单 -->
    <view class="booking-form">
      <!-- 待领养宠物信息 -->
      <view class="form-section">
        <view class="section-title">待领养宠物</view>
        <view class="adoption-pet-info" v-if="adoptionPet">
          <image :src="adoptionPet.imageUrl" class="pet-image" mode="aspectFill"></image>
          <view class="pet-details">
            <view class="pet-name">{{ adoptionPet.petName }}</view>
            <view class="pet-breed">{{ adoptionPet.breed }} · {{ adoptionPet.age }}岁 · {{ adoptionPet.gender }}</view>
            <view class="adoption-fee">领养费用：¥{{ adoptionPet.adoptionFee }}</view>
          </view>
        </view>
        <view class="no-pet-selected" v-else>
          <text>请先选择要领养的宠物</text>
        </view>
      </view>

      <!-- 预约到店日期 -->
      <view class="form-section">
        <view class="section-title">预约到店日期</view>
        <picker mode="date" :value="formData.appointmentDate" :start="todayDate" :end="maxDate" @change="onDateChange">
          <view class="date-picker">
            <view class="picker-value">{{ formData.appointmentDate || '请选择到店日期' }}</view>
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

      <!-- 联系人信息 -->
      <view class="form-section">
        <view class="section-title">联系人信息</view>
        <view class="contact-inputs">
          <view class="input-row">
            <view class="input-label">姓名</view>
            <input
              class="input-field"
              v-model="formData.contactName"
              placeholder="请输入您的姓名"
              maxlength="20"
            />
          </view>
          <view class="input-row">
            <view class="input-label">手机号</view>
            <input
              class="input-field"
              v-model="formData.contactPhone"
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
            />
          </view>
          <view class="input-row">
            <view class="input-label">身份证号</view>
            <input
              class="input-field"
              v-model="formData.idCard"
              placeholder="请输入身份证号（用于领养审核）"
              maxlength="18"
            />
          </view>
          <view class="input-row">
            <view class="input-label">居住地址</view>
            <input
              class="input-field"
              v-model="formData.address"
              placeholder="请输入详细居住地址"
              maxlength="100"
            />
          </view>
        </view>
      </view>

      <!-- 领养原因 -->
      <view class="form-section">
        <view class="section-title">领养原因</view>
        <textarea
          class="reason-textarea"
          v-model="formData.reason"
          placeholder="请简述您的领养原因和养宠经验（有助于审核通过）"
          maxlength="200"
        ></textarea>
      </view>

      <!-- 备注信息 -->
      <view class="form-section">
        <view class="section-title">备注信息</view>
        <textarea
          class="remark-textarea"
          v-model="formData.remark"
          placeholder="其他需要说明的信息（选填）"
          maxlength="100"
        ></textarea>
      </view>
    </view>

    <!-- 提交按钮 -->
    <view class="submit-section">
      <button class="submit-button" @click="submitBooking" :disabled="!canSubmit">
        提交领养申请
      </button>
    </view>

  </view>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'BookAdoption',

  data() {
    return {
      formData: {
        adoptionPetId: null, // 要领养的宠物ID
        appointmentDate: '',
        timeSlot: '',
        contactName: '',
        contactPhone: '',
        idCard: '',
        address: '',
        reason: '',
        remark: '',
        serviceType: 'adoption',
        adoptionFee: 0
      },
      adoptionPet: null, // 要领养的宠物信息
      timeSlots: [
        { label: '08:00-10:00', value: '08:00-10:00' },
        { label: '10:00-12:00', value: '10:00-12:00' },
        { label: '12:00-14:00', value: '12:00-14:00' },
        { label: '14:00-16:00', value: '14:00-16:00' },
        { label: '16:00-18:00', value: '16:00-18:00' },
        { label: '18:00-20:00', value: '18:00-20:00' }
      ],
      todayDate: '',
      maxDate: ''
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn']),
    
    canSubmit() {
      return this.formData.adoptionPetId &&
             this.formData.appointmentDate &&
             this.formData.timeSlot &&
             this.formData.contactName &&
             this.formData.contactPhone &&
             this.formData.idCard &&
             this.formData.address &&
             this.formData.reason
    }
  },

  onLoad(options) {
    // 设置页面标题
    if (options.serviceName) {
      uni.setNavigationBarTitle({
        title: decodeURIComponent(options.serviceName)
      })
    } else {
      uni.setNavigationBarTitle({
        title: '宠物领养预约'
      })
    }

    // 检查登录状态
    if (!this.isLoggedIn) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
      return
    }

    // 初始化日期
    this.initData()
    
    // 如果有传递的宠物ID，加载宠物信息
    if (options.petId) {
      this.formData.adoptionPetId = parseInt(options.petId)
      this.loadAdoptionPetInfo()
    }
  },

  methods: {
    initData() {
      const today = new Date()
      const maxDate = new Date()
      maxDate.setMonth(maxDate.getMonth() + 1)
      
      this.todayDate = this.formatDate(today)
      this.maxDate = this.formatDate(maxDate)
    },

    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    onDateChange(e) {
      this.formData.appointmentDate = e.detail.value
    },

    selectTimeSlot(slot) {
      this.formData.timeSlot = slot
    },

    async loadAdoptionPetInfo() {
      if (!this.formData.adoptionPetId) return
      
      try {
        // 这里应该调用获取待领养宠物详情的API
        // const res = await this.$api.getAdoptionPetDetail(this.formData.adoptionPetId)
        // this.adoptionPet = res.data
        
        // 临时模拟数据
        this.adoptionPet = {
          id: this.formData.adoptionPetId,
          petName: '小白',
          breed: '金毛',
          age: 2,
          gender: '公',
          imageUrl: '/static/images/default-pet.png',
          adoptionFee: 200
        }
        this.formData.adoptionFee = this.adoptionPet.adoptionFee
      } catch (error) {
        console.error('加载宠物信息失败:', error)
        uni.showToast({
          title: '加载宠物信息失败',
          icon: 'none'
        })
      }
    },

    async submitBooking() {
      if (!this.canSubmit) {
        uni.showToast({
          title: '请完善必填信息',
          icon: 'none'
        })
        return
      }

      // 验证手机号
      if (!/^1[3-9]\d{9}$/.test(this.formData.contactPhone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        })
        return
      }

      // 验证身份证号
      if (!/^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/.test(this.formData.idCard)) {
        uni.showToast({
          title: '请输入正确的身份证号',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({
          title: '提交中...'
        })

        const appointmentData = {
          userId: this.userInfo.uid,
          adoptionPetId: this.formData.adoptionPetId,
          serviceType: this.formData.serviceType,
          appointmentDate: this.formData.appointmentDate,
          timeSlot: this.formData.timeSlot,
          contactName: this.formData.contactName,
          contactPhone: this.formData.contactPhone,
          idCard: this.formData.idCard,
          address: this.formData.address,
          reason: this.formData.reason,
          remark: this.formData.remark,
          adoptionFee: this.formData.adoptionFee,
          status: 'pending'
        }
        
        console.log('用户信息:', this.userInfo)
        console.log('提交预约数据:', appointmentData)

        const res = await this.$api.createAdoptionAppointment(appointmentData)

        if (res.code === 0) {
          uni.hideLoading()
          
          // 跳转到预约成功页面
          uni.showToast({
            title: '申请提交成功',
            icon: 'success'
          })
          
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.hideLoading()
          uni.showToast({
            title: res.message || '提交失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('提交预约失败:', error)
        uni.showToast({
          title: '提交失败，请重试',
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
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.booking-form {
  padding: 20rpx;
}

.form-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

/* 待领养宠物信息 */
.adoption-pet-info {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.pet-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
}

.pet-details {
  flex: 1;
}

.pet-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.pet-breed {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 8rpx;
}

.adoption-fee {
  font-size: 28rpx;
  color: #ff6b35;
  font-weight: bold;
}

.no-pet-selected {
  text-align: center;
  color: #999;
  padding: 40rpx;
}

/* 日期选择器 */
.date-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.picker-value {
  font-size: 30rpx;
  color: #333;
}

.picker-arrow {
  font-size: 32rpx;
  color: #999;
}

/* 时间段选择 */
.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.time-slot {
  flex: 1;
  min-width: 200rpx;
  padding: 20rpx;
  text-align: center;
  background: #f8f9fa;
  border: 2rpx solid #e9ecef;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #666;
  transition: all 0.3s;
}

.time-slot.selected {
  background: #ff6b35;
  border-color: #ff6b35;
  color: white;
}

/* 联系人信息 */
.contact-inputs {
  background: white;
}

.input-row {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.input-row:last-child {
  border-bottom: none;
}

.input-label {
  width: 160rpx;
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.input-field {
  flex: 1;
  font-size: 30rpx;
  color: #333;
  padding: 10rpx 0;
}

/* 文本域 */
.reason-textarea,
.remark-textarea {
  width: 100%;
  min-height: 120rpx;
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  font-size: 30rpx;
  color: #333;
  border: none;
  resize: none;
}

/* 提交按钮 */
.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 20rpx 30rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: 9999;
}

.submit-button {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  border-radius: 44rpx;
  font-size: 32rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.submit-button:disabled {
  background: #ccc;
  color: #999;
}
</style>