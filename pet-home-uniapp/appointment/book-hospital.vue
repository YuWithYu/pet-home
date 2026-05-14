<template>
  <view class="booking-container">

    <!-- 预约表单 -->
    <view class="booking-form">
      <!-- 选择宠物 -->
      <view class="form-section">
        <view class="section-title">选择宠物</view>
        <view class="pet-selector" @click="openPetPicker">
          <view class="selector-label">{{ selectedPet ? selectedPet.name : '请选择宠物' }}</view>
        </view>
      </view>

      <!-- 预约日期 -->
      <view class="form-section">
        <view class="section-title">预约日期</view>
        <picker mode="date" :value="formData.date" :start="todayDate" :end="maxDate" @change="onDateChange">
          <view class="date-picker">
            <view class="picker-value">{{ formData.date || '请选择日期' }}</view>
          </view>
        </picker>
      </view>

      <!-- 服务门店（先选门店再选时间） -->
      <view class="form-section">
        <view class="section-title">服务门店</view>
        <view class="store-selector" @click="showStorePicker = true">
          <view class="selector-label">{{ storeSelectorLabel }}</view>
        </view>
        <view
          class="store-consult-entry"
          :class="{ disabled: !formData.storeId }"
          @click="contactSelectedStore"
        >
          联系门店客服
        </view>
      </view>

      <!-- 预约时间段（先选日期再显示）-->
      <view class="form-section">
        <view class="section-title">预约时间</view>
        <view v-if="!formData.date" class="empty-hint">请先选择预约日期</view>
        <view v-else-if="loadingTimeSlots" class="loading-hint">加载中...</view>
        <view v-else-if="timeSlots.length === 0" class="empty-hint">
          该日期暂无服务人员排班，请选择其他日期或联系门店
        </view>
        <view v-else class="time-slots">
          <view
            v-for="slot in timeSlots"
            :key="slot.timeSlot || slot.value"
            :class="[
              'time-slot',
              {
                selected: formData.timeSlot === (slot.timeSlot || slot.value),
                disabled: (slot.status && slot.status !== '可预约') || (slot.availableCount !== undefined && slot.availableCount <= 0)
              }
            ]"
            @click="selectTimeSlot(slot.timeSlot || slot.value)"
          >
            <view class="slot-time">{{ slot.label || slot.timeSlot || slot.value }}</view>
            <view v-if="slot.availableCount !== undefined" class="slot-count">
              {{ slot.availableCount }}人可预约
            </view>
            <view v-else-if="slot.status && slot.status !== '可预约'" class="slot-count">
              {{ slot.status }}
            </view>
          </view>
        </view>
      </view>

      <!-- 联系人信息-->
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
              placeholder="请输入联系联系电话"
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

    <!-- 价格汇总-->
    <view class="price-summary">
      <view class="summary-row">
        <view class="summary-label">服务费用</view>
        <view class="summary-value">¥{{ formattedPrice }}</view>
      </view>
      <view class="summary-total">
        <view class="total-label">合计</view>
        <view class="total-value">¥{{ formattedPrice }}</view>
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
          <view class="picker-close" @click="showPetPicker = false">×</view>
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

    <!-- 门店选择弹窗（与洗护一致：大弹窗、列表拉高以显示更多门店�?-->
    <view v-if="showStorePicker" class="picker-modal store-picker-modal" @click="showStorePicker = false">
      <view class="picker-content store-picker-content" @click.stop>
        <view class="picker-header">
          <view class="picker-title">选择医疗门店</view>
          <view class="picker-close" @click="showStorePicker = false">×</view>
        </view>
        <view class="picker-list">
          <view v-if="hospitalStores.length === 0" class="empty-stores-hint">暂无门店，请在管理后台「服务管�?�?服务门店管理」中添加</view>
          <view
            v-for="store in hospitalStores"
            :key="store.id"
            :class="['store-item', { 'selected': formData.location === store.address }]"
            @click="selectStore(store)"
          >
            <view class="store-info">
              <view class="store-name">
                {{ store.name }}
                <text v-if="store.isDefault" class="default-badge">推荐</text>
              </view>
              <view class="store-address">
                <image class="store-icon" src="/static/images/location-pin.svg" mode="aspectFit" />
                <text>{{ store.address }}</text>
              </view>
              <view class="store-details">
                <view class="store-phone">
                  <image class="store-icon" src="/static/images/电话.png" mode="aspectFit" />
                  <text>{{ store.phone }}</text>
                </view>
                <view v-if="store.businessHours" class="store-hours">
                  <image class="store-icon" src="/static/images/时钟.png" mode="aspectFit" />
                  <text>{{ store.businessHours }}</text>
                </view>
              </view>
            <!-- 与洗�?上门铲屎一致：仅已满或仅剩X位时显示，不显示「余位充足�?-->
            <view v-if="store.capacityClass === 'capacity-full' || store.capacityClass === 'capacity-low'" class="store-extra-info">
              <view class="store-capacity" :class="store.capacityClass">
                {{ store.capacityText }}
              </view>
            </view>
            </view>
            <view v-if="formData.location === store.address" class="store-check">✓</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { mapGetters } from 'vuex'

export default {
  name: 'BookHospital',

  data() {
    return {
      formData: {
        petId: null,
        date: '',
        timeSlot: '',
        location: '',
        storeId: null,
        contactName: '',
        contactPhone: '',
        remark: '',
        serviceType: 'hospital',
        serviceId: null,
        price: 0
      },
      timeSlots: [],
      defaultTimeSlots: [
        '08:00-09:00',
        '09:00-10:00',
        '10:00-11:00',
        '11:00-12:00',
        '14:00-15:00',
        '15:00-16:00',
        '16:00-17:00'
      ],
      loadingTimeSlots: false,
      pets: [],
      selectedPet: null,
      showPetPicker: false,
      showStorePicker: false,
      todayDate: '',
      maxDate: '',
      hospitalStores: [],
      serviceName: '',
      lastOnShowLoadTime: 0
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn']),
    formattedPrice() {
      const price = Number(this.formData.price || 0)
      if (!isFinite(price) || isNaN(price)) {
        return '0'
      }
      return Number.isInteger(price) ? price.toString() : price.toFixed(2)
    },
    storeSelectorLabel() {
      if (this.formData.location) return this.formData.location
      if (this.hospitalStores.length === 0) return '暂无门店（请在管理后台添加）'
      return '请选择医疗门店'
    }
  },

  onLoad(options) {
    this.applyServiceOptions(options)

    // 检查登录状态
    if (!this.isLoggedIn) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages-auth/login'
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
    this.loadStores()

    if (this.formData.serviceId && !this.formData.price) {
      this.fetchServiceInfo(this.formData.serviceId)
    }
  },
  onShow() {
    // 返回本页时静默刷新：节流 15 秒，不显示全局「加载中」，避免从添加宠物返回时反复loading
    const now = Date.now()
    const throttleMs = 15 * 1000
    if (now - (this.lastOnShowLoadTime || 0) < throttleMs && (this.lastOnShowLoadTime || 0) > 0) {
      return
    }
    this.loadPets(true)
    if (this.formData.date) {
      this.loadAvailableTimeSlots(true)
    }
    this.lastOnShowLoadTime = Date.now()
  },

  methods: {
    applyServiceOptions(options = {}) {
      if (!options) return

      if (options.serviceName) {
        this.serviceName = decodeURIComponent(options.serviceName)
      }

      const title = this.serviceName || '宠物医院服务'
      uni.setNavigationBarTitle({
        title
      })

      if (options.serviceId) {
        const parsedId = parseInt(options.serviceId, 10)
        if (!isNaN(parsedId)) {
          this.formData.serviceId = parsedId
        }
      }

      if (options.price) {
        const parsedPrice = parseFloat(options.price)
        if (!isNaN(parsedPrice)) {
          this.formData.price = parsedPrice
        }
      }

      if (options.serviceType) {
        this.formData.serviceType = options.serviceType
      }
    },

    async fetchServiceInfo(serviceId) {
      try {
        const res = await api.getHospitalServiceById(serviceId)
        if ((res.code === 0 || res.code === 200) && res.data) {
          if (!this.serviceName) {
            this.serviceName = res.data.name || ''
            uni.setNavigationBarTitle({
              title: this.serviceName || '宠物医院服务'
            })
          }
          if (!this.formData.price && res.data.price) {
            this.formData.price = Number(res.data.price)
          }
        }
      } catch (error) {
        console.error('加载服务信息失败:', error)
      }
    },

    // 初始化数据
    initData() {
      // 设置今天的日期为最小日期
      const today = new Date()
      this.todayDate = this.formatDate(today)
      
      // 设置最大日期为一个月
      const maxDate = new Date()
      maxDate.setMonth(maxDate.getMonth() + 1)
      this.maxDate = this.formatDate(maxDate)
      
      // 设置联系人信息为用户信息
      if (this.userInfo) {
        this.formData.contactName = this.userInfo.nickname || this.userInfo.username || ''
        this.formData.contactPhone = this.userInfo.phone || ''
      }

      this.timeSlots = this.filterSlotsByMinAdvanceHours(this.createDefaultTimeSlots())
    },

    // 格式化日期
    formatDate(date) {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    createDefaultTimeSlots() {
      return this.defaultTimeSlots.map(slot => ({
        timeSlot: slot,
        label: slot,
        value: slot,
        status: '可预约',
        availableCount: undefined
      }))
    },

    filterSlotsByMinAdvanceHours(slots) {
      if (!slots || !slots.length || !this.formData.date) return slots
      const deadline = Date.now() + 2 * 60 * 60 * 1000
      const ymd = this.formData.date.split('-').map(Number)
      if (ymd.length !== 3 || ymd.some(n => Number.isNaN(n))) return slots
      return slots.filter(s => {
        const ts = (s.timeSlot || s.value || '').toString().trim()
        if (!ts) return true
        const dash = ts.indexOf('-')
        const startStr = dash >= 0 ? ts.substring(0, dash).trim() : ts
        const parts = startStr.match(/^(\d{1,2}):(\d{2})$/)
        if (!parts) return true
        const slotStart = new Date(ymd[0], ymd[1] - 1, ymd[2], parseInt(parts[1], 10), parseInt(parts[2], 10), 0, 0).getTime()
        return slotStart >= deadline
      })
    },

    async loadAvailableTimeSlots(silent = false) {
      if (!this.formData.date) {
        this.timeSlots = this.filterSlotsByMinAdvanceHours(this.createDefaultTimeSlots())
        return
      }

      try {
        this.loadingTimeSlots = true
        const showLoading = !silent
        const res = await api.getAvailableTimeSlots(this.formData.serviceType, this.formData.date, this.formData.storeId, showLoading)
        if (res.code === 200 || res.code === 0) {
          const data = Array.isArray(res.data) ? res.data : []
          if (data.length > 0) {
            this.timeSlots = this.filterSlotsByMinAdvanceHours(data.map(slot => ({
              timeSlot: slot.timeSlot || slot.value,
              label: slot.label || slot.timeSlot || slot.value,
              value: slot.timeSlot || slot.value,
              status: slot.status || '可预约',
              availableCount: (() => {
                if (slot.availableCount !== undefined && slot.availableCount !== null) {
                  return Number(slot.availableCount)
                }
                if (slot.remainingCapacity !== undefined && slot.remainingCapacity !== null) {
                  return Number(slot.remainingCapacity)
                }
                return undefined
              })()
            })))
          } else {
            this.timeSlots = []
          }
        } else {
          this.timeSlots = []
          uni.showToast({
            title: res.msg || '加载时间段失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载医院可用时间段失败', error)
        this.timeSlots = []
        uni.showToast({
          title: '加载时间段失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        this.loadingTimeSlots = false
      }
    },

    // 解析当前用户 ID（兼容Vuex 未就绪或仅存 storage 的情况，避免「选择宠物」有时不显示）
    getCurrentUserId() {
      const u = this.userInfo
      if (u && (u.uid || u.id)) return u.uid || u.id
      const stored = uni.getStorageSync('userId')
      if (stored) return stored
      const userInfo = uni.getStorageSync('userInfo')
      return userInfo && (userInfo.uid || userInfo.id) ? (userInfo.uid || userInfo.id) : null
    },

    // 打开宠物选择弹窗：若列表为空则先拉取一次，避免有时不显示
    openPetPicker() {
      if (this.pets.length === 0) {
        this.loadPets(true)
      }
      this.showPetPicker = true
    },

    // 加载宠物列表（优先本地存储，兼容后端接口）；silent true 时不显示全局「加载中」（用于 onShow 静默刷新）
    async loadPets(silent = false) {
      try {
        const showLoading = !silent
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
          // 2) 若本地没有，从后端拉取（userId 多来源，避免 Vuex 未就绪时拉不到）
          const userId = this.getCurrentUserId()
          if (api && userId) {
            try {
              const res = await api.getPetPage(1, 100, userId, showLoading)
              if (res && (res.code === 0 || res.code === 200) && res.data && res.data.records) {
                this.pets = res.data.records
              }
            } catch (err) {
              // 后端不可用时忽略，保持空列表
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
      if (this.formData.date) {
        this.loadAvailableTimeSlots()
      }
    },

    // 选择时间段
    selectTimeSlot(slot) {
      const target = this.timeSlots.find(s => (s.timeSlot || s.value) === slot)
      if (target && (target.status === '可预约' || target.status === undefined) && (target.availableCount === undefined || target.availableCount > 0)) {
        this.formData.timeSlot = slot
      } else {
        uni.showToast({
          title: '该时间段暂不可用',
          icon: 'none'
        })
      }
    },

    // 选择宠物
    selectPet(pet) {
      this.selectedPet = pet
      this.formData.petId = pet.id
      this.showPetPicker = false
    },

    // 加载门店列表
    async loadStores() {
      try {
        const res = await api.getAllStores()
        if ((res.code === 0 || res.code === 200) && res.data) {
          const storeList = Array.isArray(res.data) ? res.data : (res.data.records || res.data.list || [])
          this.hospitalStores = storeList.map(store => {
            const available = (store.maxCapacity || 0) - (store.currentBookings || 0)
            let capacityText = ''
            let capacityClass = ''

            if (store.maxCapacity) {
              if (available <= 0) {
                capacityText = '🈵 已满'
                capacityClass = 'capacity-full'
              } else if (available <= 3) {
                capacityText = `⚠️ 仅剩${available}位`
                capacityClass = 'capacity-low'
              } else {
                capacityText = '余位充足'
                capacityClass = 'capacity-good'
              }
            }

            return {
              id: store.id,
              name: store.storeName,
              address: store.address,
              phone: store.phone,
              businessHours: store.businessHours,
              isDefault: store.isDefault,
              latitude: store.latitude,
              longitude: store.longitude,
              maxCapacity: store.maxCapacity,
              currentBookings: store.currentBookings,
              capacityText,
              capacityClass
            }
          })

          if (this.hospitalStores.length === 1) {
            this.formData.location = this.hospitalStores[0].address
            this.formData.storeId = this.hospitalStores[0].id
          } else if (this.hospitalStores.length > 1) {
            const defaultStore = this.hospitalStores.find(s => s.isDefault)
            if (defaultStore) {
              this.formData.location = defaultStore.address
              this.formData.storeId = defaultStore.id
            }
          }
          if (this.formData.date) {
            this.loadAvailableTimeSlots()
          }
        }
      } catch (error) {
        console.error('加载门店列表失败:', error)
        this.hospitalStores = []
        this.formData.location = ''
        this.formData.storeId = null
        uni.showToast({ title: '加载门店失败，请稍后重试', icon: 'none' })
      }
    },

    // 选择门店
    selectStore(store) {
      if (store.maxCapacity) {
        const available = store.maxCapacity - (store.currentBookings || 0)
        if (available <= 0) {
          uni.showToast({
            title: '该门店已满，请选择其他门店',
            icon: 'none'
          })
          return
        }
      }

      this.formData.location = store.address
      this.formData.storeId = store.id
      this.showStorePicker = false
      if (this.formData.date) {
        this.loadAvailableTimeSlots()
      }
    },
    contactSelectedStore() {
      const userId = uni.getStorageSync('userId') || uni.getStorageSync('uid')
      if (!userId) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        setTimeout(() => uni.navigateTo({ url: '/pages-auth/login' }), 1500)
        return
      }
      if (!this.formData.storeId) {
        uni.showToast({ title: '请先选择门店', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/user/chat?isService=true&isOutlet=true&storeId=${this.formData.storeId}&targetUserName=${encodeURIComponent('门店客服')}`
      })
    },

    // 前往添加宠物页面
    goToAddPet() {
      // 直接进入添加宠物档案第一页
      uni.navigateTo({
        url: '/user/add-pet-basic'
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
        hash = hash & hash
      }
      
      // 将哈希值映射到Integer范围
      const compatibleId = Math.abs(hash) % (maxInt - minInt + 1) + minInt
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
          title: '请选择医疗门店',
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
          title: '请输入联系联系电话',
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

    // 提交预约：先跳确认订单页支付，支付成功后再创建预约并跳成功页
    async submitBooking() {
      if (!this.validateForm()) {
        return
      }

      const uid = this.userInfo && (this.userInfo.uid || this.userInfo.id)
      if (!uid) {
        uni.showToast({
          title: '用户信息异常，请重新登录',
          icon: 'none'
        })
        return
      }

      const price = Number(this.formData.price) || 0
      const serviceId = this.formData.serviceId || ''
      const serviceName = this.serviceName || '宠物医院服务'

      const serviceImage = uni.getStorageSync('serviceBookingBannerImage') || ''
      const orderData = {
        orderType: 'service',
        serviceType: this.formData.serviceType || 'hospital',
        serviceId: serviceId,
        goods: {
          id: serviceId,
          name: serviceName,
          image: serviceImage,
          price: price
        },
        selectedSpec: null,
        quantity: 1,
        totalAmount: price,
        shippingFee: 0,
        finalAmount: price
      }

      const appointmentData = {
        userId: uid,
        petId: this.getCompatiblePetId(this.formData.petId),
        serviceType: this.formData.serviceType,
        serviceId: this.formData.serviceId,
        date: this.formData.date,
        timeSlot: this.formData.timeSlot,
        location: this.formData.location,
        storeId: this.formData.storeId,
        contactName: this.formData.contactName,
        contactPhone: this.formData.contactPhone,
        remark: this.formData.remark,
        price: this.formData.price,
        status: 'pending'
      }

      const serviceBookingFormData = {
        serviceType: 'hospital',
        appointmentData,
        successParams: {
          location: this.formData.location || '广州南方学院',
          serviceType: this.formData.serviceType || 'hospital',
          petName: this.selectedPet?.name || 'YU',
          appointmentDate: this.formData.date,
          appointmentTime: this.formData.timeSlot
        }
      }

      uni.setStorageSync('orderData', orderData)
      uni.setStorageSync('serviceBookingFormData', serviceBookingFormData)
      uni.removeStorageSync('serviceBookingBannerImage')
      uni.navigateTo({
        url: '/order/confirm'
      })
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


/* 表单区域（与洗护预约页一致：字体、排版、颜色） */
.booking-form {
  .form-section {
    background-color: white;
    padding: 24rpx;
    margin-bottom: 16rpx;
    
    .section-title {
      font-size: 26rpx;
      color: #333;
      font-weight: normal;
      margin-bottom: 16rpx;
    }
  }
}

/* 宠物选择*/
.pet-selector, .date-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  
  .selector-label, .picker-value {
    font-size: 26rpx;
    color: #333;
    font-weight: normal;
  }
  
  .selector-arrow, .picker-arrow {
    font-size: 34rpx;
    color: #999;
  }
}

/* 时间段选择 */
.time-slots {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
  
  .time-slot {
    padding: 20rpx 16rpx;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    text-align: center;
    font-size: 24rpx;
    color: #666;
    border: 2rpx solid transparent;
    position: relative;
    
    .slot-time {
      font-size: 26rpx;
      margin-bottom: 4rpx;
      font-weight: normal;
    }

    .slot-count {
      font-size: 20rpx;
      color: #999;
    }
    
    &.selected {
      background-color: #fff7e6;
      color: #ff6b35;
      border-color: #ff6b35;
      font-weight: normal;
      
      .slot-time {
        color: #ff6b35;
      }
    }

    &.disabled {
      background-color: #f5f5f5;
      color: #ccc;
      opacity: 0.6;
      cursor: not-allowed;
      
      .slot-time,
      .slot-count {
        color: #ccc;
      }
    }
  }
}

.loading-hint,
.empty-stores-hint {
  text-align: center;
  padding: 32rpx 24rpx;
  color: #999;
  font-size: 26rpx;
}
.empty-hint {
  text-align: center;
  padding: 32rpx;
  color: #999;
  font-size: 24rpx;
}

/* 地址输入 */
.address-input, .remark-input {
  .address-textarea, .remark-textarea {
    width: 100%;
    min-height: 140rpx;
    padding: 18rpx;
    background-color: #f8f8f8;
    border-radius: 12rpx;
    font-size: 26rpx;
    line-height: 1.6;
  }
}

/* 联系人输�?*/
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
      font-size: 26rpx;
      color: #333;
      font-weight: normal;
      flex-shrink: 0;
      background-color: white;
      z-index: 1;
    }
    
    .input-field {
      flex: 1;
      font-size: 26rpx;
      padding-left: 16rpx;
      background-color: transparent;
      border: none;
      color: #333;
      opacity: 1;
      z-index: 1;
    }
  }
}

/* 价格汇�?*/
.price-summary {
  background-color: white;
  padding: 24rpx 28rpx;
  margin-bottom: 16rpx;
  
  .summary-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 16rpx;
    font-size: 26rpx;
    color: #666;
    
    .summary-value {
      color: #333;
    }
  }
  
  .summary-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16rpx;
    border-top: 1rpx solid #f0f0f0;
    
    .total-label {
      font-size: 26rpx;
      color: #333;
      font-weight: normal;
    }
    
    .total-value {
      font-size: 34rpx;
      color: #ff6b35;
      font-weight: normal;
    }
  }
}

/* 提交按钮 */
.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15rpx 30rpx;
  background-color: white;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  z-index: 999;
  
  .submit-button {
    width: 100%;
    background-color: #ffd700;
    color: #333;
    border: none;
    padding: 14rpx;
    border-radius: 12rpx;
    font-size: 26rpx;
    font-weight: bold;
    line-height: 1.4;
    
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
        font-weight: normal;
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
            font-weight: normal;
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
    
    /* 门店列表�?*/
    .store-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 30rpx;
      border-bottom: 1rpx solid #f0f0f0;
      
      &.selected {
        background-color: #fff7e6;
        border-left: 4rpx solid #ff6b35;
      }
      
      .store-info {
        flex: 1;
        
        .store-name {
          font-size: 30rpx;
          color: #333;
          font-weight: bold;
          margin-bottom: 10rpx;
          
          .default-badge {
            font-size: 20rpx;
            color: #ff6b35;
            background-color: #fff7e6;
            padding: 4rpx 12rpx;
            border-radius: 20rpx;
            margin-left: 10rpx;
          }
        }
        
        .store-address {
          display: flex;
          align-items: center;
          gap: 8rpx;
          font-size: 26rpx;
          color: #666;
          margin-bottom: 8rpx;
          
          .store-icon {
            width: 28rpx;
            height: 28rpx;
            flex-shrink: 0;
          }
        }
        
        .store-details {
          display: flex;
          gap: 20rpx;
          
          .store-phone, .store-hours {
            display: flex;
            align-items: center;
            gap: 6rpx;
            font-size: 24rpx;
            color: #999;
            
            .store-icon {
              width: 26rpx;
              height: 26rpx;
              flex-shrink: 0;
            }
          }
        }

        .store-extra-info {
          display: flex;
          gap: 16rpx;
          margin-top: 12rpx;

          .store-capacity {
            font-size: 22rpx;
            padding: 4rpx 12rpx;
            border-radius: 20rpx;
            background-color: #f3f4f6;
            color: #666;
          }

          .capacity-full {
            background-color: #fdecea;
            color: #d93025;
          }

          .capacity-low {
            background-color: #fff7e6;
            color: #ff8f00;
          }

          .capacity-good {
            background-color: #e8f5e9;
            color: #2e7d32;
          }
        }
      }
      
      .store-check {
        color: #ff6b35;
        font-size: 40rpx;
        font-weight: bold;
        margin-left: 20rpx;
      }
    }
  }

  /* 门店选择弹窗：与洗护一致的大小规格，拉高以显示更多门店 */
  &.store-picker-modal {
    .picker-content.store-picker-content {
      max-height: 88vh;
    }
    .picker-list {
      max-height: 85vh;
      overflow-y: auto;
    }
    .picker-header .picker-title {
      font-size: 26rpx;
      font-weight: normal;
    }
    .picker-header {
      padding: 18rpx 24rpx;
    }
    .picker-close {
      font-size: 32rpx;
    }
    .picker-list .store-item {
      padding: 20rpx 24rpx;
      .store-name {
        font-size: 26rpx;
        font-weight: normal !important;
        margin-bottom: 6rpx;
        .default-badge {
          font-size: 20rpx;
        }
      }
      .store-address {
        font-size: 22rpx;
        margin-bottom: 4rpx;
      }
      .store-details .store-phone,
      .store-details .store-hours {
        font-size: 22rpx;
      }
      .store-extra-info .store-capacity {
        font-size: 20rpx;
      }
      .store-check {
        font-size: 34rpx;
        font-weight: normal;
      }
    }
    .empty-stores-hint {
      font-size: 24rpx;
      padding: 24rpx;
    }
  }
}

/* 门店选择�?*/
/* 门店选择器（与宠物、日期一致：26rpx，不加大�?*/
.store-selector {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  
  .selector-label {
    font-size: 26rpx;
    color: #333;
    font-weight: normal;
  }
  
  .selector-arrow {
    font-size: 34rpx;
    color: #999;
  }
}

.store-consult-entry {
  margin-top: 14rpx;
  font-size: 24rpx;
  color: #2f80ed;
  line-height: 1.6;
}

.store-consult-entry.disabled {
  color: #bdbdbd;
}
</style>

