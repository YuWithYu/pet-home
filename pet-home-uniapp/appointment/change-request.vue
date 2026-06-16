<template>
  <view class="change-request-container">
    <view v-if="loading" class="loading-wrap">加载中...</view>
    <template v-else>
      <view class="tip">提交后预约状态将变为「变更待确认」，工作人员同意后生效；不同意则保持原预约状态</view>
      <view class="booking-form">
        <!-- 选择宠物（与确认预约页一致，三类服务均有）-->
        <view class="form-section">
          <view class="section-title">选择宠物</view>
          <view class="pet-selector" @click="openPetPicker">
            <view class="selector-label">{{ selectedPet ? selectedPet.name : '请选择宠物' }}</view>
          </view>
        </view>
        <!-- 预约日期：与确认预约页一致的选择日期-->
        <view class="form-section">
          <view class="section-title">预约日期</view>
          <picker mode="date" :value="form.requestedDate" :start="todayDate" :end="maxDate" @change="onDateChange">
            <view class="date-picker">
              <view class="picker-value">{{ form.requestedDate || '请选择日期' }}</view>
            </view>
          </picker>
        </view>
        <!-- 服务门店 -->
        <view class="form-section">
          <view class="section-title">服务门店</view>
          <view class="store-selector" @click="showStorePicker = true">
            <view class="selector-label">{{ storeSelectorLabel }}</view>
          </view>
        </view>
        <!-- 上门铲屎：服务地址（家门牌等） -->
        <view v-if="serviceType === 'door-cleaning'" class="form-section">
          <view class="section-title">服务地址</view>
          <view class="input-box">
            <input
              v-model="form.requestedLocation"
              type="text"
              class="input-field"
              placeholder="请输入服务地址（家门牌等）"
              placeholder-class="placeholder"
            />
          </view>
        </view>
        <!-- 上门铲屎：钥匙给予方式-->
        <view v-if="serviceType === 'door-cleaning'" class="form-section">
          <view class="section-title">钥匙给予方式</view>
          <view class="store-selector" @click="openKeyPicker('handover')">
            <view class="selector-label">{{ form.requestedKeyHandoverMethod || '请选择钥匙给予方式' }}</view>
          </view>
        </view>
        <!-- 上门铲屎：钥匙归还方式-->
        <view v-if="serviceType === 'door-cleaning'" class="form-section">
          <view class="section-title">钥匙归还方式</view>
          <view class="store-selector" @click="openKeyPicker('return')">
            <view class="selector-label">{{ form.requestedKeyReturnMethod || '请选择钥匙归还方式' }}</view>
          </view>
        </view>
        <!-- 预约时间 -->
        <view class="form-section">
          <view class="section-title">预约时间</view>
          <view v-if="!form.requestedDate" class="empty-hint">请先选择预约日期</view>
          <view v-else-if="!storeId && stores.length > 0" class="empty-hint">请先选择门店</view>
          <view v-else-if="loadingTimeSlots" class="loading-hint">加载中...</view>
          <view v-else-if="timeSlots.length === 0" class="empty-hint">
            该日期暂无可用时段，请选择其他日期或联系门店
          </view>
          <view v-else class="time-slots">
            <view
              v-for="(slot, slotIdx) in timeSlots"
              :key="slotKey(slot, slotIdx)"
              :class="[
                'time-slot',
                {
                  selected: form.requestedTimeSlot === (slot.timeSlot || slot.value),
                  disabled: (slot.status && slot.status !== '可预约') || (slot.availableCount !== undefined && slot.availableCount <= 0)
                }
              ]"
              @click="selectTimeSlot(slot.timeSlot || slot.value)"
            >
              <view class="slot-time">{{ slot.label || slot.timeSlot || slot.value }}</view>
              <view v-if="slot.availableCount !== undefined" class="slot-count">{{ slot.availableCount }}人可预约</view>
              <view v-else-if="slot.status && slot.status !== '可预约'" class="slot-count">{{ slot.status }}</view>
            </view>
          </view>
        </view>
        <!-- 联系电话：与确认预约页一致的输入框样式-->
        <view class="form-section">
          <view class="section-title">联系电话</view>
          <view class="input-box">
            <input
              v-model="form.requestedContactPhone"
              type="number"
              class="input-field"
              placeholder="请输入联系联系电话"
              placeholder-class="placeholder"
            />
          </view>
        </view>
        <!-- 备注 -->
        <view class="form-section">
          <view class="section-title">备注信息（选填）</view>
          <view class="remark-input">
            <textarea
              v-model="form.requestedRemark"
              class="remark-textarea"
              placeholder="如有特殊要求请在此说明"
              placeholder-class="placeholder"
              maxlength="200"
            />
          </view>
        </view>
      </view>
      <view class="submit-section">
        <button class="submit-button" :disabled="submitting" @click="submit">确认更改</button>
        <button class="return-button" @click="goBack">返回</button>
      </view>
    </template>

    <!-- 宠物选择弹窗（与确认预约页一致） -->
    <view v-if="showPetPicker" class="picker-modal" @click="showPetPicker = false">
      <view class="picker-content" @click.stop>
        <view class="picker-header">
          <view class="picker-title">选择宠物</view>
          <view class="picker-close" @click="showPetPicker = false">×</view>
        </view>
        <view class="picker-list">
          <view v-if="pets.length === 0" class="empty-hint">暂无宠物，请先添加宠物</view>
          <view
            v-for="pet in pets"
            :key="pet.id"
            :class="['pet-item', { selected: form.requestedPetId === pet.id }]"
            @click="selectPet(pet)"
          >
            <view class="pet-info">
              <view class="pet-name">{{ pet.name }}</view>
              <view class="pet-breed">{{ pet.breed || '' }}</view>
            </view>
            <view v-if="form.requestedPetId === pet.id" class="pet-check">✓</view>
          </view>
        </view>
        <view class="add-pet-btn" @click="goToAddPet">+ 添加新宠物</view>
      </view>
    </view>

    <!-- 钥匙方式选择弹窗（上门铲屎，与确认预约页一致） -->
    <view v-if="showKeyPicker" class="picker-modal" @click="showKeyPicker = false">
      <view class="picker-content key-picker-content" @click.stop>
        <view class="picker-header">
          <view class="picker-title">{{ keyPickerMode === 'handover' ? '选择钥匙给予方式' : '选择钥匙归还方式' }}</view>
          <view class="picker-close" @click="showKeyPicker = false">×</view>
        </view>
        <view class="key-method-list" style="padding: 24rpx 0 48rpx;">
          <view
            v-for="(opt, kIdx) in keyMethodOptions"
            :key="opt"
            :class="['key-method-item', { selected: (keyPickerMode === 'handover' ? form.requestedKeyHandoverMethod : form.requestedKeyReturnMethod) === opt }]"
            :style="'text-align: center; padding-top: 20rpx; padding-bottom: 20rpx; margin-bottom: ' + (kIdx < keyMethodOptions.length - 1 ? '28rpx' : '0') + ';'"
            @click="selectKeyMethod(opt)"
          >
            <text style="display: block; text-align: center; width: 100%;">{{ opt }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 门店选择弹窗 -->
    <view v-if="showStorePicker" class="picker-modal" @click="showStorePicker = false">
      <view class="picker-content" @click.stop>
        <view class="picker-header">
          <view class="picker-title">选择服务门店</view>
          <view class="picker-close" @click="showStorePicker = false">×</view>
        </view>
        <view class="picker-list">
          <view v-if="stores.length === 0" class="empty-hint">暂无门店</view>
          <view
            v-for="store in stores"
            :key="store.id"
            :class="['store-item', { selected: isStoreSelected(store) }]"
            @click="selectStore(store)"
          >
            <view class="store-info">
              <view class="store-name">{{ store.name }}</view>
              <view class="store-address">{{ store.address }}</view>
            </view>
            <view v-if="isStoreSelected(store)" class="store-check">✓</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'AppointmentChangeRequest',

  data() {
    return {
      appointmentId: null,
      serviceType: 'door-cleaning',
      loading: true,
      submitting: false,
      appointment: null,
      form: {
        requestedDate: '',
        requestedTimeSlot: '',
        requestedLocation: '',
        requestedRemark: '',
        requestedContactPhone: '',
        requestedKeyHandoverMethod: '',
        requestedKeyReturnMethod: '',
        requestedPetId: null
      },
      pets: [],
      showPetPicker: false,
      stores: [],
      storeId: null,
      timeSlots: [],
      loadingTimeSlots: false,
      showStorePicker: false,
      showKeyPicker: false,
      keyPickerMode: 'handover',
      keyMethodOptions: ['密码', '丰巢存件', '面交', '闪送跑腿', '家中有人', '藏于指定位置', '其他'],
      todayDate: '',
      maxDate: ''
    }
  },

  computed: {
    selectedPet() {
      const id = this.form.requestedPetId
      if (!id || !this.pets.length) return null
      return this.pets.find(p => p.id === id || p.id === Number(id)) || null
    },
    storeSelectorLabel() {
      // door-cleaning：门店选择独立于“服务地址”
      if (this.serviceType === 'door-cleaning') {
        if (!this.storeId || this.stores.length === 0) return '请选择门店'
        const store = this.stores.find(s => s.id === this.storeId || String(s.id) === String(this.storeId))
        return store ? (store.name || store.address) : '请选择门店'
      }

      // 其他到店服务：历史逻辑复用 requestedLocation 作为“门店地址”
      const loc = (this.form.requestedLocation || '').trim()
      if (!loc || this.stores.length === 0) return '请选择门店'
      const store = this.stores.find(s => (s.address || '').trim() === loc)
      if (store) return store.name || store.address || loc
      return '请选择门店'
    }
  },

  onLoad(options) {
    this.appointmentId = options.id ? Number(options.id) : null
    this.serviceType = options.serviceType || 'door-cleaning'
    const today = new Date()
    this.todayDate = this.formatDate(today)
    const max = new Date()
    max.setMonth(max.getMonth() + 1)
    this.maxDate = this.formatDate(max)
    if (!this.appointmentId) {
      uni.showToast({ title: '预约ID无效', icon: 'none' })
      setTimeout(() => uni.navigateBack(), 1500)
      return
    }
    this.loadDetail()
  },

  methods: {
    slotKey(slot, slotIdx) {
      return String(slot && (slot.timeSlot || slot.value) || '') + '-' + slotIdx
    },
    isStoreSelected(store) {
      if (!store) return false
      if (this.serviceType === 'door-cleaning') {
        return this.storeId != null && (store.id === this.storeId || String(store.id) === String(this.storeId))
      }
      return (
        (this.form.requestedLocation || '').trim() &&
        (store.address || '').trim() &&
        String(this.form.requestedLocation).trim() === String(store.address).trim()
      )
    },
    formatDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },

    async loadDetail() {
      try {
        this.loading = true
        let res
        const type = this.serviceType
        if (type === 'hospital') {
          res = await api.getHospitalAppointmentDetail(this.appointmentId)
        } else if (type === 'grooming') {
          res = await api.getGroomingAppointmentDetail(this.appointmentId)
        } else {
          res = await api.getAppointmentDetail(this.appointmentId)
        }
        if ((res.code === 0 || res.code === 200) && res.data) {
          this.appointment = res.data
          const d = this.appointment.date || this.appointment.appointmentDate
          const dateStr = Array.isArray(d)
            ? (d[0] + '-' + String(d[1]).padStart(2, '0') + '-' + String(d[2]).padStart(2, '0'))
            : (d && d.toString().substring(0, 10)) || ''
          this.form.requestedDate = dateStr
          this.form.requestedTimeSlot = this.appointment.timeSlot || this.appointment.time_slot || ''
          this.form.requestedLocation = this.appointment.location || ''
          this.form.requestedRemark = this.appointment.remark || ''
          this.form.requestedContactPhone = this.appointment.contactPhone || ''
          // door-cleaning：门店选择使用 storeId（供计算可用时段）
          if (this.serviceType === 'door-cleaning') {
            const sid = this.appointment.storeId != null ? this.appointment.storeId : (this.appointment.store_id != null ? this.appointment.store_id : null)
            this.storeId = sid != null ? Number(sid) : null
          }
          if (this.serviceType === 'door-cleaning') {
            this.form.requestedKeyHandoverMethod = this.appointment.keyHandoverMethod || this.appointment.key_handover_method || ''
            this.form.requestedKeyReturnMethod = this.appointment.keyReturnMethod || this.appointment.key_return_method || ''
          }
          const petId = this.appointment.petId != null ? this.appointment.petId : (this.appointment.pet_id != null ? this.appointment.pet_id : null)
          if (petId != null) this.form.requestedPetId = Number(petId)
          await this.loadPets()
          await this.loadStores()
          if (this.form.requestedDate && this.storeId) {
            this.loadAvailableTimeSlots(true)
          }
        } else {
          uni.showToast({ title: '加载预约失败', icon: 'none' })
          setTimeout(() => uni.navigateBack(), 1500)
        }
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '加载失败', icon: 'none' })
        setTimeout(() => uni.navigateBack(), 1500)
      } finally {
        this.loading = false
      }
    },

    async loadStores() {
      try {
        const res = await api.getAllStores()
        if ((res.code === 0 || res.code === 200) && res.data) {
          const list = Array.isArray(res.data) ? res.data : (res.data.records || res.data.list || [])
          this.stores = list.map(s => ({
            id: s.id,
            name: s.storeName || s.name,
            address: s.address
          }))
          // door-cleaning：门店选择使用 storeId，requestedLocation 是“服务地址”，不参与门店匹配
          if (this.serviceType === 'door-cleaning') {
            if (this.storeId != null && this.stores.length) {
              const match = this.stores.find(s => s.id === this.storeId || String(s.id) === String(this.storeId))
              if (!match) this.storeId = null
            }
            if (this.stores.length === 1 && !this.storeId) {
              this.storeId = this.stores[0].id
              if (this.form.requestedDate) this.loadAvailableTimeSlots(true)
            }
            return
          }

          // 到店服务：requestedLocation 复用为门店地址
          const loc = (this.form.requestedLocation || '').trim()
          if (this.stores.length && loc) {
            const match = this.stores.find(s => (s.address || '').trim() === loc)
            if (match) {
              this.storeId = match.id
            } else {
              // 预约里的地址不在门店列表中（1123456 等无效值），清空以便用户重新选择，并显示「请选择门店」
              this.form.requestedLocation = ''
              this.storeId = null
            }
          }
          if (this.stores.length === 1 && !this.form.requestedLocation) {
            this.form.requestedLocation = this.stores[0].address
            this.storeId = this.stores[0].id
            if (this.form.requestedDate) this.loadAvailableTimeSlots(true)
          }
        }
      } catch (e) {
        console.error('加载门店失败', e)
        this.stores = []
      }
    },

    onDateChange(e) {
      this.form.requestedDate = e.detail.value || ''
      this.form.requestedTimeSlot = ''
      if (this.form.requestedDate && this.storeId) this.loadAvailableTimeSlots()
    },

    selectStore(store) {
      if (this.serviceType === 'door-cleaning') {
        this.storeId = store.id
      } else {
        this.form.requestedLocation = store.address
        this.storeId = store.id
      }
      this.showStorePicker = false
      this.form.requestedTimeSlot = ''
      if (this.form.requestedDate) this.loadAvailableTimeSlots()
    },

    async loadPets() {
      try {
        const uid = this.$store.getters.userInfo?.uid ?? this.$store.getters.userInfo?.id ?? uni.getStorageSync('userId')
        const res = await api.getPetList(uid, false)
        if (res && (res.code === 200 || res.code === 0) && res.data) {
          const list = res.data.records || res.data.list || (Array.isArray(res.data) ? res.data : [])
          this.pets = list.map(p => ({ id: p.id, name: p.name, breed: p.breed }))
        } else {
          this.pets = []
        }
      } catch (e) {
        this.pets = []
      }
    },
    openPetPicker() {
      this.showPetPicker = true
    },
    selectPet(pet) {
      this.form.requestedPetId = pet.id
      this.showPetPicker = false
    },
    goToAddPet() {
      this.showPetPicker = false
      uni.navigateTo({ url: '/user/pets/add' })
    },

    openKeyPicker(mode) {
      this.keyPickerMode = mode
      this.showKeyPicker = true
    },
    selectKeyMethod(opt) {
      if (this.keyPickerMode === 'handover') {
        this.form.requestedKeyHandoverMethod = opt
      } else {
        this.form.requestedKeyReturnMethod = opt
      }
      this.showKeyPicker = false
    },

    async loadAvailableTimeSlots(silent = false) {
      if (!this.form.requestedDate) {
        this.timeSlots = []
        return
      }
      try {
        this.loadingTimeSlots = true
        const res = await api.getAvailableTimeSlots(
          this.serviceType,
          this.form.requestedDate,
          this.storeId,
          !silent
        )
        if (res.code === 200 || res.code === 0) {
          const data = Array.isArray(res.data) ? res.data : []
          this.timeSlots = data.length
            ? this.filterSlotsByMinAdvanceHours(
                data.map(slot => ({
                  timeSlot: slot.timeSlot || slot.value,
                  label: slot.label || slot.timeSlot || slot.value,
                  value: slot.timeSlot || slot.value,
                  status: slot.status || '可预约',
                  availableCount:
                    slot.availableCount != null
                      ? Number(slot.availableCount)
                      : slot.remainingCapacity != null
                        ? Number(slot.remainingCapacity)
                        : undefined
                }))
              )
            : []
        } else {
          this.timeSlots = []
        }
      } catch (e) {
        this.timeSlots = []
      } finally {
        this.loadingTimeSlots = false
      }
    },

    filterSlotsByMinAdvanceHours(slots) {
      if (!this.form.requestedDate || !slots || !slots.length) return slots || []
      const deadline = Date.now() + 2 * 60 * 60 * 1000
      const ymd = this.form.requestedDate.split('-').map(Number)
      if (ymd.length !== 3 || ymd.some(n => Number.isNaN(n))) return slots
      return slots.filter(s => {
        const ts = (s.timeSlot || s.value || '').toString().trim()
        if (!ts) return true
        const dash = ts.indexOf('-')
        const startStr = dash >= 0 ? ts.substring(0, dash).trim() : ts
        const m = startStr.match(/^(\d{1,2}):(\d{2})$/)
        if (!m) return true
        const slotStart = new Date(ymd[0], ymd[1] - 1, ymd[2], parseInt(m[1], 10), parseInt(m[2], 10), 0, 0).getTime()
        return slotStart >= deadline
      })
    },

    selectTimeSlot(slotVal) {
      const target = this.timeSlots.find(s => (s.timeSlot || s.value) === slotVal)
      if (target && (target.status === '可预约' || !target.status) && (target.availableCount === undefined || target.availableCount > 0)) {
        this.form.requestedTimeSlot = slotVal
      } else if (target) {
        uni.showToast({ title: '该时段暂不可用', icon: 'none' })
      }
    },

    async submit() {
      if (!this.form.requestedDate || !this.form.requestedTimeSlot) {
        uni.showToast({ title: '请选择预约日期和可用时间段', icon: 'none' })
        return
      }
      // 校验字段：door-cleaning 需storeId（门店）+ requestedLocation（服务地址）
      if (this.serviceType === 'door-cleaning') {
        if (!this.storeId) {
          uni.showToast({ title: '请选择服务门店', icon: 'none' })
          return
        }
        if (!this.form.requestedLocation || !String(this.form.requestedLocation).trim()) {
          uni.showToast({ title: '请输入服务地址', icon: 'none' })
          return
        }
        if (!this.form.requestedKeyHandoverMethod || !this.form.requestedKeyReturnMethod) {
          uni.showToast({ title: '请选择钥匙给予/归还方式', icon: 'none' })
          return
        }
      } else {
        // 到店服务：requestedLocation（门店地址）必填
        if (!this.form.requestedLocation && this.stores.length > 0) {
          uni.showToast({ title: '请选择服务门店', icon: 'none' })
          return
        }
      }
      const uid = this.$store.getters.userInfo?.uid ?? this.$store.getters.userInfo?.id ?? uni.getStorageSync('userId')
      if (!uid) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      const appointmentType = this.serviceType === 'door-cleaning' ? 'door_cleaning' : this.serviceType
      try {
        this.submitting = true
        const payload = {
          userId: Number(uid),
          appointmentType,
          appointmentId: this.appointmentId,
          requestedDate: this.form.requestedDate,
          requestedTimeSlot: this.form.requestedTimeSlot,
          requestedLocation: this.form.requestedLocation || undefined,
          requestedRemark: this.form.requestedRemark || undefined,
          requestedContactPhone: this.form.requestedContactPhone || undefined
        }
        if (this.form.requestedPetId != null) payload.requestedPetId = this.form.requestedPetId
        if (this.serviceType === 'door-cleaning') {
          if (this.form.requestedKeyHandoverMethod) payload.requestedKeyHandoverMethod = this.form.requestedKeyHandoverMethod
          if (this.form.requestedKeyReturnMethod) payload.requestedKeyReturnMethod = this.form.requestedKeyReturnMethod
        }
        const res = await api.submitAppointmentChangeRequest(payload)
        if (res.code === 200 || res.code === 0) {
          uni.showToast({ title: '已提交，请等待工作人员确认', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 1500)
        } else {
          uni.showToast({ title: res.msg || '提交失败', icon: 'none' })
        }
      } catch (e) {
        console.error(e)
        uni.showToast({ title: e.message || '提交失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },

    goBack() {
      uni.navigateBack()
    }
  }
}
</script>

<style lang="scss" scoped>
.change-request-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding: 24rpx 24rpx 200rpx;
}
.loading-wrap {
  text-align: center;
  padding: 60rpx 0;
  color: #999;
  font-size: 28rpx;
}
.tip {
  background: #fff8e6;
  padding: 20rpx 24rpx;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
  font-size: 26rpx;
  color: #666;
}

/* 与确认预约页一致的表单区域 */
.booking-form {
  .form-section {
    background-color: white;
    padding: 24rpx;
    margin-bottom: 16rpx;
    border-radius: 12rpx;

    .section-title {
      font-size: 26rpx;
      color: #333;
      font-weight: normal;
      margin-bottom: 16rpx;
    }
  }
}

/* 宠物选择框（与确认预约页一致） */
.pet-selector {
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
    flex: 1;
  }
}

/* 日期选择框（白底圆角） */
.date-picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 24rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;

  .picker-value {
    font-size: 26rpx;
    color: #333;
    font-weight: normal;
  }
}

/* 门店选择*/
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
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* 钥匙方式弹窗（上门铲屎，与确认预约页一致） */
.key-picker-content .key-method-list {
  padding: 24rpx 0 48rpx;
  width: 100%;
  box-sizing: border-box;
}
.key-picker-content .key-method-list .key-method-item {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 48rpx;
  padding-bottom: 48rpx;
  padding-left: 30rpx;
  padding-right: 30rpx;
  margin-bottom: 32rpx;
  font-size: 30rpx;
  color: #333;
  text-align: center;
  background: #fff;
}
.key-picker-content .key-method-list .key-method-item:last-child {
  margin-bottom: 0;
}
.key-picker-content .key-method-list .key-method-item.selected {
  background-color: #fff7e6;
}
.key-picker-content .key-method-list .key-method-item text {
  text-align: center;
  display: inline-block;
}

/* 时间段选择（与确认预约页一致） */
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
    z-index: 1;

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
      .slot-time { color: #ff6b35; }
    }
    &.disabled {
      background-color: #f5f5f5;
      color: #ccc;
      opacity: 0.6;
      .slot-time, .slot-count { color: #ccc; }
    }
  }
}
.empty-hint, .loading-hint {
  text-align: center;
  padding: 32rpx;
  color: #999;
  font-size: 24rpx;
}

/* 联系电话输入框（白底圆角框） */
.input-box {
  padding: 20rpx 24rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  .input-field {
    width: 100%;
    font-size: 26rpx;
    color: #333;
    background: transparent;
    border: none;
  }
}
.placeholder { color: #bbb; }

/* 备注输入�?*/
.remark-input .remark-textarea {
  width: 100%;
  min-height: 140rpx;
  padding: 18rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
  font-size: 26rpx;
  line-height: 1.6;
  box-sizing: border-box;
}

/* 底部按钮区：与确认预约页一致的黄色主按�?*/
.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 15rpx 30rpx;
  background-color: white;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
  z-index: 999;
  padding-bottom: calc(15rpx + env(safe-area-inset-bottom));

  .submit-button,
  .return-button {
    width: 100%;
    height: 88rpx;
    padding: 0 24rpx;
    border-radius: 12rpx;
    font-size: 26rpx;
    line-height: 1.4;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .submit-button {
    background-color: #ffd700;
    color: #333;
    border: none;
    font-weight: bold;
    margin-bottom: 20rpx;
  }
  .submit-button[disabled] {
    opacity: 0.7;
  }
  .return-button {
    background-color: #fff;
    color: #666;
    border: 1rpx solid #ddd;
  }
}

/* 门店选择弹窗 */
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
}
.picker-content {
  width: 100%;
  max-height: 80vh;
  background-color: white;
  border-radius: 40rpx 40rpx 0 0;
  overflow: hidden;
}
.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
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
.picker-list {
  max-height: 500rpx;
  overflow-y: auto;
  padding: 16rpx;
}
.store-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  border-radius: 12rpx;
  margin-bottom: 12rpx;
  background: #f9f9f9;
}
.store-item.selected {
  background-color: #fff7e6;
  border-left: 4rpx solid #ff6b35;
}
.store-info {
  flex: 1;
  min-width: 0;
}
.store-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}
.store-address {
  font-size: 24rpx;
  color: #666;
  margin-top: 8rpx;
}
.store-check {
  color: #ff6b35;
  font-size: 40rpx;
  font-weight: bold;
  margin-left: 20rpx;
}

/* 宠物选择弹窗列表�?*/
.picker-list .pet-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}
.picker-list .pet-item.selected {
  background-color: #fff7e6;
}
.picker-list .pet-item .pet-info {
  flex: 1;
}
.picker-list .pet-item .pet-name {
  font-size: 30rpx;
  color: #333;
  margin-bottom: 8rpx;
}
.picker-list .pet-item .pet-breed {
  font-size: 24rpx;
  color: #999;
}
.picker-list .pet-item .pet-check {
  color: #ff6b35;
  font-size: 40rpx;
  font-weight: bold;
  margin-left: 20rpx;
}
.add-pet-btn {
  padding: 30rpx;
  text-align: center;
  color: #667eea;
  font-size: 28rpx;
  font-weight: bold;
  border-top: 1rpx solid #f0f0f0;
}
</style>
