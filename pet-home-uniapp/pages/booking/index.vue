<template>
  <view class="booking-container">
    <!-- 顶部服务信息卡片 -->
    <view class="service-card">
      <image class="service-image" :src="serviceInfo.imageUrl" mode="aspectFill"></image>
      <view class="service-info">
        <view class="service-name">{{ serviceInfo.name }}</view>
        <view class="service-price">¥{{ serviceInfo.price }}</view>
      </view>
    </view>

    <!-- 预约信息表单 -->
    <view class="form-container">
      <!-- 选择日期 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">📅</text>
          <text class="label-text">预约日期</text>
        </view>
        <picker 
          mode="date" 
          :value="formData.date" 
          :start="minDate"
          :end="maxDate"
          @change="onDateChange"
        >
          <view class="picker-value">
            {{ formData.date || '请选择日期' }}
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 选择时间段 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">🕐</text>
          <text class="label-text">预约时间</text>
        </view>
        <view class="time-slots" v-if="timeSlots.length > 0">
          <view 
            v-for="(slot, index) in timeSlots" 
            :key="index"
            class="time-slot"
            :class="{ 
              'active': formData.timeSlot === slot.time,
              'disabled': !slot.available
            }"
            @tap="selectTimeSlot(slot)"
          >
            <view class="time-text">{{ slot.time }}</view>
            <view class="status-text" v-if="!slot.available">已满</view>
          </view>
        </view>
        <view class="empty-tips" v-else>
          {{ formData.date ? '该日期暂无可预约时间' : '请先选择日期' }}
        </view>
      </view>

      <!-- 联系人信息 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">👤</text>
          <text class="label-text">联系人</text>
        </view>
        <input 
          class="input" 
          v-model="formData.contactName" 
          placeholder="请输入联系人姓名"
          placeholder-class="placeholder"
        />
      </view>

      <!-- 联系电话 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">📱</text>
          <text class="label-text">联系电话</text>
        </view>
        <input 
          class="input" 
          v-model="formData.contactPhone" 
          type="number"
          placeholder="请输入联系电话"
          placeholder-class="placeholder"
        />
      </view>

      <!-- 服务地址（仅限上门服务） -->
      <view class="form-item" v-if="needAddress">
        <view class="label">
          <text class="label-icon">📍</text>
          <text class="label-text">服务地址</text>
        </view>
        <textarea 
          class="textarea" 
          v-model="formData.address" 
          placeholder="请输入详细服务地址"
          placeholder-class="placeholder"
          maxlength="200"
        />
      </view>

      <!-- 选择宠物 -->
      <view class="form-item" v-if="needPetInfo">
        <view class="label">
          <text class="label-icon">🐾</text>
          <text class="label-text">选择宠物</text>
        </view>
        <view class="pet-selector" @tap="selectPet">
          <view v-if="selectedPet" class="selected-pet">
            <image :src="selectedPet.avatar" class="pet-avatar" mode="aspectFill" />
            <view class="pet-info">
              <text class="pet-name">{{ selectedPet.name }}</text>
              <text class="pet-details">{{ selectedPet.breed }} · {{ selectedPet.gender === 'male' ? '男孩子' : '女孩子' }} · {{ selectedPet.weight }}kg</text>
            </view>
          </view>
          <view v-else class="no-pet-selected">
            <text class="placeholder-text">请选择宠物</text>
            <text class="arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 宠物信息（备用输入） -->
      <view class="form-item" v-if="needPetInfo && !hasPets">
        <view class="label">
          <text class="label-icon">📝</text>
          <text class="label-text">宠物信息</text>
        </view>
        <textarea 
          class="textarea" 
          v-model="formData.petInfo" 
          placeholder="请输入宠物种类、年龄、体重等信息"
          placeholder-class="placeholder"
          maxlength="200"
        />
      </view>

      <!-- 宠物数量 -->
      <view class="form-item" v-if="needPetInfo">
        <view class="label">
          <text class="label-icon">🔢</text>
          <text class="label-text">宠物数量</text>
        </view>
        <view class="counter-wrapper">
          <view class="counter-btn" @tap="decrementPetCount">-</view>
          <input 
            class="counter-input" 
            v-model.number="formData.petCount" 
            type="number"
            disabled
          />
          <view class="counter-btn" @tap="incrementPetCount">+</view>
        </view>
      </view>

      <!-- 宠物照片 -->
      <view class="form-item" v-if="needPetInfo">
        <view class="label">
          <text class="label-icon">📷</text>
          <text class="label-text">宠物照片</text>
          <text class="label-tips">（选填，最多3张）</text>
        </view>
        <view class="photo-upload">
          <view 
            v-for="(photo, index) in formData.petPhotos" 
            :key="index" 
            class="photo-item"
          >
            <image :src="photo" mode="aspectFill" class="photo-img"></image>
            <view class="photo-delete" @tap="deletePhoto(index)">×</view>
          </view>
          <view 
            v-if="formData.petPhotos.length < 3" 
            class="photo-add" 
            @tap="choosePhoto"
          >
            <text class="photo-add-icon">+</text>
            <text class="photo-add-text">添加照片</text>
          </view>
        </view>
      </view>

      <!-- 服务时长 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">⏰</text>
          <text class="label-text">服务时长</text>
        </view>
        <view class="duration-options">
          <view 
            v-for="(duration, index) in durationOptions" 
            :key="index"
            class="duration-item"
            :class="{ 'active': formData.duration === duration.value }"
            @tap="selectDuration(duration)"
          >
            <view class="duration-name">{{ duration.label }}</view>
            <view class="duration-price">+¥{{ duration.extraPrice }}</view>
          </view>
        </view>
      </view>

      <!-- 附加服务 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">➕</text>
          <text class="label-text">附加服务</text>
          <text class="label-tips">（可多选）</text>
        </view>
        <view class="extra-services">
          <view 
            v-for="(service, index) in extraServices" 
            :key="index"
            class="extra-service-item"
            :class="{ 'active': formData.selectedExtras.includes(service.id) }"
            @tap="toggleExtraService(service)"
          >
            <view class="extra-service-name">{{ service.name }}</view>
            <view class="extra-service-price">+¥{{ service.price }}</view>
          </view>
        </view>
      </view>

      <!-- 紧急联系人 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">🆘</text>
          <text class="label-text">紧急联系人</text>
          <text class="label-tips">（选填）</text>
        </view>
        <input 
          class="input" 
          v-model="formData.emergencyContact" 
          placeholder="紧急联系人姓名"
          placeholder-class="placeholder"
        />
        <input 
          class="input" 
          style="margin-top: 20rpx;"
          v-model="formData.emergencyPhone" 
          type="number"
          placeholder="紧急联系人电话"
          placeholder-class="placeholder"
        />
      </view>

      <!-- 特殊需求 -->
      <view class="form-item" v-if="needPetInfo">
        <view class="label">
          <text class="label-icon">⚠️</text>
          <text class="label-text">特殊需求/过敏史</text>
          <text class="label-tips">（选填）</text>
        </view>
        <textarea 
          class="textarea" 
          v-model="formData.specialNeeds" 
          placeholder="如：对某些食物过敏、怕打雷、攻击性等"
          placeholder-class="placeholder"
          maxlength="200"
        />
      </view>

      <!-- 优惠券 -->
      <view class="form-item coupon-item" @tap="selectCoupon">
        <view class="label">
          <text class="label-icon">🎫</text>
          <text class="label-text">优惠券</text>
        </view>
        <view class="coupon-value">
          <text v-if="formData.couponId">-¥{{ couponDiscount }}</text>
          <text v-else class="placeholder-text">选择优惠券</text>
          <text class="arrow">›</text>
        </view>
      </view>

      <!-- 支付方式 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">💳</text>
          <text class="label-text">支付方式</text>
        </view>
        <view class="payment-methods">
          <view 
            v-for="(method, index) in paymentMethods" 
            :key="index"
            class="payment-item"
            :class="{ 'active': formData.paymentMethod === method.value }"
            @tap="selectPaymentMethod(method)"
          >
            <text class="payment-icon">{{ method.icon }}</text>
            <text class="payment-name">{{ method.label }}</text>
          </view>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-item">
        <view class="label">
          <text class="label-icon">📝</text>
          <text class="label-text">备注</text>
        </view>
        <textarea 
          class="textarea" 
          v-model="formData.remark" 
          placeholder="其他需要说明的信息（选填）"
          placeholder-class="placeholder"
          maxlength="200"
        />
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="price-info">
        <text class="label">总计：</text>
        <text class="price">¥{{ calculateTotalPrice() }}</text>
      </view>
      <button class="submit-btn" @tap="submitBooking">确认预约</button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      serviceType: '', // litter, boarding, medical, grooming, adoption
      serviceId: '',
      serviceInfo: {
        name: '',
        price: 0,
        imageUrl: ''
      },
      formData: {
        date: '',
        timeSlot: '',
        contactName: '',
        contactPhone: '',
        address: '',
        petInfo: '',
        petCount: 1,
        petPhotos: [],
        duration: 'standard',
        selectedExtras: [],
        emergencyContact: '',
        emergencyPhone: '',
        specialNeeds: '',
        couponId: null,
        paymentMethod: 'online',
        remark: ''
      },
      petList: [], // 用户宠物列表
      selectedPet: null, // 选中的宠物
      timeSlots: [],
      minDate: '',
      maxDate: '',
      durationOptions: [
        { label: '标准时长', value: 'standard', extraPrice: 0 },
        { label: '加长服务', value: 'extended', extraPrice: 50 },
        { label: '深度服务', value: 'deep', extraPrice: 100 }
      ],
      extraServices: [
        { id: 1, name: '拍照记录', price: 20 },
        { id: 2, name: '视频记录', price: 30 },
        { id: 3, name: '专车接送', price: 50 },
        { id: 4, name: '加急服务', price: 80 }
      ],
      paymentMethods: [
        { label: '在线支付', value: 'online', icon: '💳' },
        { label: '到店支付', value: 'offline', icon: '💰' }
      ],
      couponDiscount: 0
    }
  },
  computed: {
    needAddress() {
      // 上门铲屎需要地址
      return this.serviceType === 'litter'
    },
    needPetInfo() {
      // 寄养、医院、洗护、领养需要宠物信息
      return ['boarding', 'medical', 'grooming', 'adoption'].includes(this.serviceType)
    },
    
    hasPets() {
      // 检查用户是否有宠物档案
      return this.petList.length > 0
    }
  },
  onLoad(options) {
    this.serviceType = options.serviceType || 'litter'
    this.serviceId = options.serviceId || ''
    
    // 设置日期范围（今天到30天后）
    const today = new Date()
    this.minDate = this.formatDate(today)
    const maxDay = new Date()
    maxDay.setDate(maxDay.getDate() + 30)
    this.maxDate = this.formatDate(maxDay)
    
    // 加载服务信息
    this.loadServiceInfo()
    
    // 从本地存储加载用户信息
    this.loadUserInfo()
    
    // 加载宠物列表
    this.loadPetList()
  },
  methods: {
    // 加载服务信息
    async loadServiceInfo() {
      try {
        let apiMethod = ''
        switch(this.serviceType) {
          case 'litter':
            apiMethod = 'getLitterServicePage'
            break
          case 'boarding':
            apiMethod = 'getBoardingServicePage'
            break
          case 'medical':
            apiMethod = 'getMedicalServicePage'
            break
          case 'grooming':
            apiMethod = 'getGroomingServicePage'
            break
          case 'adoption':
            apiMethod = 'getAdoptionServicePage'
            break
        }
        
        if (apiMethod) {
          const response = await this.$api[apiMethod]({
            current: 1,
            size: 10,
            status: 'active'
          })
          
          if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
            const service = response.data.records[0]
            this.serviceInfo = {
              name: service.name,
              price: service.price,
              imageUrl: service.imageUrl || '/static/images/banner1.jpg'
            }
          }
        }
      } catch (error) {
        console.error('加载服务信息失败:', error)
      }
    },
    
    // 加载用户信息
    loadUserInfo() {
      try {
        const userInfo = uni.getStorageSync('userInfo')
        if (userInfo) {
          this.formData.contactName = userInfo.name || ''
          this.formData.contactPhone = userInfo.phone || ''
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
      }
    },
    
    // 日期选择
    onDateChange(e) {
      this.formData.date = e.detail.value
      this.formData.timeSlot = '' // 重置时间段
      this.loadTimeSlots()
    },
    
    // 加载时间段
    async loadTimeSlots() {
      if (!this.formData.date) {
        this.timeSlots = []
        return
      }
      
      try {
        // 调用API获取可用时间段
        const response = await this.$api.getAvailableTimeSlots({
          serviceType: this.serviceType,
          date: this.formData.date
        })
        
        if (response.code === 0 && response.data) {
          this.timeSlots = response.data
        } else {
          // 如果API未实现，使用默认时间段
          this.loadDefaultTimeSlots()
        }
      } catch (error) {
        console.error('加载时间段失败:', error)
        // 使用默认时间段
        this.loadDefaultTimeSlots()
      }
    },
    
    // 加载默认时间段
    loadDefaultTimeSlots() {
      const slots = [
        { time: '09:00-10:00', available: true },
        { time: '10:00-11:00', available: true },
        { time: '11:00-12:00', available: true },
        { time: '14:00-15:00', available: true },
        { time: '15:00-16:00', available: true },
        { time: '16:00-17:00', available: true },
        { time: '17:00-18:00', available: true }
      ]
      this.timeSlots = slots
    },
    
    // 选择时间段
    selectTimeSlot(slot) {
      if (!slot.available) {
        uni.showToast({
          title: '该时间段已满',
          icon: 'none'
        })
        return
      }
      this.formData.timeSlot = slot.time
    },
    
    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    
    // 验证表单
    validateForm() {
      if (!this.formData.date) {
        uni.showToast({ title: '请选择预约日期', icon: 'none' })
        return false
      }
      
      if (!this.formData.timeSlot) {
        uni.showToast({ title: '请选择预约时间', icon: 'none' })
        return false
      }
      
      if (!this.formData.contactName) {
        uni.showToast({ title: '请输入联系人姓名', icon: 'none' })
        return false
      }
      
      if (!this.formData.contactPhone) {
        uni.showToast({ title: '请输入联系电话', icon: 'none' })
        return false
      }
      
      const phoneReg = /^1[3-9]\d{9}$/
      if (!phoneReg.test(this.formData.contactPhone)) {
        uni.showToast({ title: '请输入正确的手机号码', icon: 'none' })
        return false
      }
      
      if (this.needAddress && !this.formData.address) {
        uni.showToast({ title: '请输入服务地址', icon: 'none' })
        return false
      }
      
      if (this.needPetInfo) {
        if (!this.selectedPet && !this.formData.petInfo) {
          uni.showToast({ title: '请选择宠物或输入宠物信息', icon: 'none' })
          return false
        }
      }
      
      return true
    },
    
    // 宠物数量增减
    incrementPetCount() {
      if (this.formData.petCount < 10) {
        this.formData.petCount++
      }
    },
    
    decrementPetCount() {
      if (this.formData.petCount > 1) {
        this.formData.petCount--
      }
    },
    
    // 加载宠物列表
    loadPetList() {
      try {
        const petList = uni.getStorageSync('petList') || []
        this.petList = petList
        console.log('加载宠物列表:', petList)
      } catch (error) {
        console.error('加载宠物列表失败:', error)
        this.petList = []
      }
    },
    
    // 选择宠物
    selectPet() {
      if (this.petList.length === 0) {
        uni.showModal({
          title: '提示',
          content: '您还没有添加宠物档案，是否前往添加？',
          success: (res) => {
            if (res.confirm) {
              uni.navigateTo({
                url: '/pages/user/my-pets'
              })
            }
          }
        })
        return
      }
      
      // 显示宠物选择器
      const petNames = this.petList.map(pet => pet.name)
      uni.showActionSheet({
        itemList: petNames,
        success: (res) => {
          const selectedIndex = res.tapIndex
          this.selectedPet = this.petList[selectedIndex]
          // 自动填充宠物信息
          this.formData.petInfo = `${this.selectedPet.name} · ${this.selectedPet.breed} · ${this.selectedPet.gender === 'male' ? '男孩子' : '女孩子'} · ${this.selectedPet.weight}kg`
        }
      })
    },
    
    // 选择宠物照片
    choosePhoto() {
      uni.chooseImage({
        count: 3 - this.formData.petPhotos.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.formData.petPhotos = [...this.formData.petPhotos, ...res.tempFilePaths]
        }
      })
    },
    
    // 删除照片
    deletePhoto(index) {
      this.formData.petPhotos.splice(index, 1)
    },
    
    // 选择服务时长
    selectDuration(duration) {
      this.formData.duration = duration.value
    },
    
    // 切换附加服务
    toggleExtraService(service) {
      const index = this.formData.selectedExtras.indexOf(service.id)
      if (index > -1) {
        this.formData.selectedExtras.splice(index, 1)
      } else {
        this.formData.selectedExtras.push(service.id)
      }
    },
    
    // 选择优惠券
    selectCoupon() {
      uni.showToast({
        title: '优惠券功能开发中',
        icon: 'none'
      })
    },
    
    // 选择支付方式
    selectPaymentMethod(method) {
      this.formData.paymentMethod = method.value
    },
    
    // 计算总价
    calculateTotalPrice() {
      let total = parseFloat(this.serviceInfo.price) || 0
      
      // 宠物数量
      total = total * this.formData.petCount
      
      // 服务时长
      const selectedDuration = this.durationOptions.find(d => d.value === this.formData.duration)
      if (selectedDuration) {
        total += selectedDuration.extraPrice
      }
      
      // 附加服务
      this.formData.selectedExtras.forEach(extraId => {
        const extra = this.extraServices.find(e => e.id === extraId)
        if (extra) {
          total += extra.price
        }
      })
      
      // 优惠券
      total -= this.couponDiscount
      
      return total > 0 ? total.toFixed(2) : '0.00'
    },
    
    // 提交预约
    async submitBooking() {
      if (!this.validateForm()) {
        return
      }
      
      uni.showLoading({ title: '提交中...' })
      
      try {
        const bookingData = {
          serviceType: this.serviceType,
          serviceId: this.serviceId,
          serviceName: this.serviceInfo.name,
          bookingDate: this.formData.date,
          timeSlot: this.formData.timeSlot,
          contactName: this.formData.contactName,
          contactPhone: this.formData.contactPhone,
          address: this.formData.address,
          petInfo: this.selectedPet ? 
            `${this.selectedPet.name} · ${this.selectedPet.breed} · ${this.selectedPet.gender === 'male' ? '男孩子' : '女孩子'} · ${this.selectedPet.weight}kg` : 
            this.formData.petInfo,
          petId: this.selectedPet ? this.getCompatiblePetId(this.selectedPet.id) : null, // 确保ID在Integer范围内
          petCount: this.formData.petCount,
          petPhotos: this.formData.petPhotos.join(','),
          duration: this.formData.duration,
          selectedExtras: this.formData.selectedExtras.join(','),
          emergencyContact: this.formData.emergencyContact,
          emergencyPhone: this.formData.emergencyPhone,
          specialNeeds: this.formData.specialNeeds,
          couponId: this.formData.couponId,
          paymentMethod: this.formData.paymentMethod,
          remark: this.formData.remark,
          totalPrice: this.calculateTotalPrice()
        }
        
        const response = await this.$api.createBooking(bookingData)
        
        uni.hideLoading()
        
        if (response.code === 0) {
          // 保存用户信息到本地
          uni.setStorageSync('userInfo', {
            name: this.formData.contactName,
            phone: this.formData.contactPhone
          })
          
          uni.showToast({
            title: '预约成功',
            icon: 'success',
            duration: 2000
          })
          
          // 延迟跳转到预约列表或首页
          setTimeout(() => {
            uni.navigateBack({
              delta: 2
            })
          }, 2000)
        } else {
          uni.showToast({
            title: response.msg || '预约失败，请重试',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('提交预约失败:', error)
        uni.showToast({
          title: '预约失败，请重试',
          icon: 'none'
        })
      }
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
    }
  }
}
</script>

<style scoped>
.booking-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 120rpx;
}

/* 服务信息卡片 */
.service-card {
  background: #fff;
  padding: 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.service-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  margin-right: 24rpx;
}

.service-info {
  flex: 1;
}

.service-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
}

.service-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff6b35;
}

/* 表单容器 */
.form-container {
  background: #fff;
  padding: 0 30rpx;
}

.form-item {
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-item:last-child {
  border-bottom: none;
}

.label {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.label-icon {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.label-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

/* 日期选择器 */
.picker-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
}

.arrow {
  font-size: 36rpx;
  color: #999;
}

/* 时间段选择 */
.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.time-slot {
  flex: 0 0 calc(33.33% - 14rpx);
  padding: 24rpx 12rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  text-align: center;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.time-slot.active {
  background: #fff5f2;
  border-color: #ff6b35;
  color: #ff6b35;
}

.time-slot.disabled {
  background: #f0f0f0;
  color: #ccc;
}

.time-text {
  font-size: 26rpx;
  font-weight: bold;
}

.status-text {
  font-size: 20rpx;
  margin-top: 8rpx;
}

.empty-tips {
  padding: 40rpx 0;
  text-align: center;
  color: #999;
  font-size: 26rpx;
}

/* 输入框 */
.input {
  padding: 20rpx 24rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
}

.textarea {
  width: 100%;
  padding: 20rpx 24rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  min-height: 120rpx;
  box-sizing: border-box;
}

.placeholder {
  color: #999;
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 100;
}

.price-info {
  display: flex;
  align-items: baseline;
}

.price-info .label {
  font-size: 26rpx;
  color: #666;
  margin: 0;
}

.price-info .price {
  font-size: 40rpx;
  font-weight: bold;
  color: #ff6b35;
}

.submit-btn {
  flex: 0 0 240rpx;
  height: 80rpx;
  line-height: 80rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c5a 100%);
  color: #fff;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
  border: none;
  box-shadow: 0 8rpx 20rpx rgba(255, 107, 53, 0.3);
}

.submit-btn:active {
  opacity: 0.8;
}

/* 宠物选择器 */
.pet-selector {
  padding: 20rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  border: 1rpx solid #e0e0e0;
}

.selected-pet {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.pet-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 30rpx;
  background: #f0f0f0;
}

.pet-info {
  flex: 1;
}

.pet-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  display: block;
  margin-bottom: 8rpx;
}

.pet-details {
  font-size: 24rpx;
  color: #666;
  display: block;
}

.no-pet-selected {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.placeholder-text {
  font-size: 28rpx;
  color: #999;
}

/* 宠物数量计数器 */
.counter-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 0;
}

.counter-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #ff6b35;
  color: #fff;
  font-size: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.counter-input {
  width: 120rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin: 0 40rpx;
}

/* 照片上传 */
.photo-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.photo-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
}

.photo-img {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}

.photo-delete {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #ff4d4f;
  color: #fff;
  font-size: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.photo-add {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.photo-add-icon {
  font-size: 60rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.photo-add-text {
  font-size: 24rpx;
  color: #999;
}

/* 服务时长选项 */
.duration-options {
  display: flex;
  gap: 20rpx;
}

.duration-item {
  flex: 1;
  padding: 24rpx 12rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  text-align: center;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.duration-item.active {
  background: #fff5f2;
  border-color: #ff6b35;
}

.duration-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.duration-item.active .duration-name {
  color: #ff6b35;
}

.duration-price {
  font-size: 22rpx;
  color: #ff6b35;
}

/* 附加服务 */
.extra-services {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.extra-service-item {
  flex: 0 0 calc(50% - 10rpx);
  padding: 24rpx 12rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  text-align: center;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.extra-service-item.active {
  background: #fff5f2;
  border-color: #ff6b35;
}

.extra-service-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.extra-service-item.active .extra-service-name {
  color: #ff6b35;
}

.extra-service-price {
  font-size: 22rpx;
  color: #ff6b35;
}

/* 优惠券 */
.coupon-item {
  cursor: pointer;
}

.coupon-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #ff6b35;
  font-weight: bold;
}

.placeholder-text {
  color: #999;
  font-weight: normal;
}

/* 支付方式 */
.payment-methods {
  display: flex;
  gap: 20rpx;
}

.payment-item {
  flex: 1;
  padding: 24rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
  text-align: center;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.payment-item.active {
  background: #fff5f2;
  border-color: #ff6b35;
}

.payment-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.payment-name {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
}

.payment-item.active .payment-name {
  color: #ff6b35;
}

/* 标签提示 */
.label-tips {
  font-size: 22rpx;
  color: #999;
  margin-left: 8rpx;
  font-weight: normal;
}
</style>

