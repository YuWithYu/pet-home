<template>
  <view class="detail-container">
    <!--
      使用三个独立 v-if（detailPagePhase），避免微信小程序将 v-if/v-else-if/v-else
      编译成嵌套 wx:else 时偶发整页空白。
    -->
    <view v-if="detailPagePhase === 'loading'" class="detail-loading">
      <text class="detail-loading-text">加载预约详情中...</text>
    </view>

    <view v-if="detailPagePhase === 'error'" class="detail-loading detail-error">
      <text class="detail-loading-text">加载失败</text>
      <text class="detail-error-desc">请检查网络或稍后重试</text>
      <button class="retry-btn" @tap="loadAppointmentDetail">重试</button>
    </view>

    <view v-if="detailPagePhase === 'content'" class="detail-body">
    <!-- 预约信息卡片（状态提示放在白框内顶部） -->
    <view class="info-card">
      <view class="status-desc-inline" v-if="statusDesc">{{ statusDesc }}</view>
      <view class="reject-reason-inline" v-if="appointmentDetail.status === 'cancelled' && (appointmentDetail.rejectReason || appointmentDetail.reject_reason)">
        <text class="reject-reason-label">拒绝原因：</text>
        <text class="reject-reason-value">{{ appointmentDetail.rejectReason || appointmentDetail.reject_reason }}</text>
      </view>
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
        <view class="info-item" v-if="serviceItemDisplayName">
          <text class="label">服务项目：</text>
          <text class="value">{{ serviceItemDisplayName }}</text>
        </view>
        <view class="info-item">
          <text class="label">预约日期：</text>
          <text class="value">{{ formatDate(appointmentDetail.date) }}</text>
        </view>
        <view class="info-item">
          <text class="label">预约时间：</text>
          <text class="value">{{ appointmentDetail.timeSlot }}</text>
          <text v-if="isAppointmentTimePast" class="time-past-hint">（该时段已过）</text>
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
    <view class="info-card" v-if="showQrCode">
      <view class="card-title">核销信息</view>
      <view class="verify-info">
        <!-- 二维码区�?-->
        <view class="qr-code-section">
          <view class="qr-code-container">
        <view class="qr-code-square">
          <!-- 不使用 canvas：部分基础库下页面内 canvas 会导致整页白屏；二维码以在线生成图 + 占位为准 -->
          <image 
            :src="qrCodeUrl" 
            mode="aspectFill" 
            class="qr-image"
            @error="onQrImageError"
          />
            </view>
            <view class="qr-code-text">核销码</view>
            <view class="qr-code-number">{{ displayVerifyCode }}</view>
          </view>
        </view>
        
        <view class="verify-status" v-if="appointmentDetail.isVerified === 1">
          <text class="verified-text">✓ 已核销</text>
          <text class="verify-time">{{ formatDateTime(appointmentDetail.verifyTime) }}</text>
        </view>
        <view class="verify-status" v-else>
          <text class="unverified-text">✓ 待核销</text>
        </view>
      </view>
    </view>

    <!-- 服务评价（仅已完成且有服务人员时显示�?-->
    <view id="rating-section" class="info-card" v-if="showRatingSection">
      <view class="card-title">服务评价</view>
      <view v-if="alreadyRated" class="rating-done">
        <text class="rating-done-text">您已对该服务完成评价</text>
        <view class="rating-stars-display">
          <text v-for="i in ratingStarIndexes" :key="i" class="star" :class="{ active: i <= ratingDisplay }">★</text>
        </view>
      </view>
      <view v-else class="rating-form">
        <view class="rating-label">为服务人员 {{ appointmentDetail.memberName || '服务人员' }} 评分：</view>
        <view class="rating-stars">
          <text
            v-for="i in ratingStarIndexes"
            :key="i"
            class="star clickable"
            :class="{ active: i <= ratingValue }"
            @tap="setRating(i)"
          >★</text>
        </view>
        <textarea
          v-model="ratingComment"
          class="rating-input"
          placeholder="选填：说说您的服务体验"
          maxlength="200"
        />
        <button class="submit-rating-btn" @tap="submitRating" :disabled="ratingSubmitting">
          {{ ratingSubmitting ? '提交中...' : '提交评价' }}
        </button>
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
        v-if="appointmentDetail.status === 'pending' || appointmentDetail.status === 'confirmed'" 
        class="action-button cancel-button"
        @tap="cancelAppointment"
      >
        <text class="button-text">取消预约</text>
      </view>
      <!-- 已确认订单取消需填原因，弹窗填写后提交取消申请 -->
      <view v-if="showCancelReasonPopup" class="cancel-reason-mask" @tap.self="showCancelReasonPopup = false">
        <view class="cancel-reason-popup">
          <view class="cancel-reason-title">填写取消原因</view>
          <textarea 
            v-model="cancelReason" 
            class="cancel-reason-input" 
            placeholder="请填写取消原因（必填）" 
            maxlength="500"
          />
          <view v-if="cancelPenaltyHint" class="cancel-penalty-hint">{{ cancelPenaltyHint }}</view>
          <view class="cancel-reason-actions">
            <view class="cancel-reason-btn cancel-reason-btn-cancel" @tap="showCancelReasonPopup = false; cancelReason = ''">取消</view>
            <view class="cancel-reason-btn cancel-reason-btn-submit" @tap="submitCancelRequest">提交申请</view>
          </view>
        </view>
      </view>
      <view 
        v-if="appointmentDetail.status === 'confirmed'" 
        class="action-button change-button"
        @tap="goToChangeRequest"
      >
        <text class="button-text">申请变更</text>
      </view>
      <view 
        v-if="appointmentDetail.status === 'confirmed'" 
        class="action-button contact-button"
        @tap="contactService"
      >
        <text class="button-text">联系客服</text>
      </view>
      <view 
        v-if="appointmentDetail.status === 'change_pending'" 
        class="action-button cancel-change-button"
        @tap="cancelChangeRequest"
      >
        <text class="button-text">取消变更</text>
      </view>
      <view v-if="appointmentDetail.status !== 'pending' && appointmentDetail.status !== 'confirmed'" class="action-button primary-button" @tap="goBack">
        <text class="button-text">返回</text>
      </view>
    </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'AppointmentDetail',
  
  data() {
    return {
      appointmentId: null,
      appointmentDetail: {},
      petInfo: null,
      loading: true,
      loadError: false,
      cancelReason: '',
      showCancelReasonPopup: false,
      qrCodeData: '',
      qrCodeUrl: '',
      serviceType: 'door-cleaning',
      showQrCode: false,
      displayVerifyCode: '',
      alreadyRated: false,
      ratingDisplay: 0,
      ratingValue: 0,
      ratingComment: '',
      ratingSubmitting: false,
      scrollToRating: false, // 从「评价」进入时为 true，详情加载后自动滚动到服务评价
      ratingStarIndexes: [1, 2, 3, 4, 5]
    }
  },

  computed: {
    /** loading | error | content — 供模板拆成独立 v-if，避免小程序嵌套 wx:else 白屏 */
    detailPagePhase() {
      if (this.loading) return 'loading'
      if (this.loadError) return 'error'
      return 'content'
    },
    /** 具体套餐/项目名（后端根据 serviceId 关联查询） */
    serviceItemDisplayName() {
      const a = this.appointmentDetail
      if (!a) return ''
      const n = a.serviceName != null ? a.serviceName : (a.service_name != null ? a.service_name : '')
      return String(n).trim()
    },
    showRatingSection() {
      const apt = this.appointmentDetail
      if (!apt || apt.status !== 'completed') return false
      const t = this.serviceType
      if (t !== 'door-cleaning' && t !== 'grooming' && t !== 'hospital') return false
      return !!apt.memberId
    },
    statusText() {
      const statusMap = {
        'pending': '待确认?',
        'confirmed': '已确认?',
        'change_pending': '变更待确认?',
        'cancel_pending': '取消待确认?',
        'cancelled': '已取消?',
        'completed': '已完成?',
      }
      return statusMap[this.appointmentDetail.status] || '未知状态?'
    },

    statusDesc() {
      const descMap = {
        'pending': '您的预约正在等待确认，请耐心等待',
        'confirmed': '预约已确认，请按时到达',
        'change_pending': '您已提交变更申请，请等待工作人员确认',
        'cancel_pending': '您已提交取消申请，请等待工作人员确认',
        'cancelled': '预约已取消',
        'completed': '服务已完成',
      }
      return descMap[this.appointmentDetail.status] || ''
    },

    statusClass() {
      return `status-${this.appointmentDetail.status}`
    },

    // 取消申请弹窗内显示的违约金提示（已确认且 0-2h 内）
    cancelPenaltyHint() {
      if (!this.showCancelReasonPopup || !this.isStaffConfirmed()) return ''
      const { inPenaltyWindow, penaltyAmount } = this.getCancelPenaltyInfo()
      if (!inPenaltyWindow) return ''
      const p = Number(penaltyAmount)
      const safe = Number.isFinite(p) ? p : 0
      return `服务开始前 0-2 小时内取消将扣除订单金额的 40% 作为违约金，本次 ¥${safe.toFixed(2)}。`
    },

    // 预约日期+时间段是否已过（用于显示“该时段已过”提示）
    isAppointmentTimePast() {
      const d = this.appointmentDetail
      if (!d || !d.date || !d.timeSlot) return false
      const dateStr = this.formatDate(d.date)
      if (!dateStr) return false
      const today = new Date()
      const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
      if (dateStr < todayStr) return true
      if (dateStr > todayStr) return false
      const ts = String(d.timeSlot).trim()
      const dash = ts.indexOf('-')
      const endStr = dash >= 0 ? ts.substring(dash + 1).trim() : ts
      const m = endStr.match(/^(\d{1,2}):(\d{2})$/)
      if (!m) return false
      const endMin = parseInt(m[1], 10) * 60 + parseInt(m[2], 10)
      const nowMin = today.getHours() * 60 + today.getMinutes()
      return endMin <= nowMin
    }
  },

  onLoad(options) {
    const rawId = options.id != null ? String(options.id) : (options.appointmentId != null ? String(options.appointmentId) : '')
    let id = rawId.trim()
    try {
      if (id) id = decodeURIComponent(id)
    } catch (e) {
      /* ignore */
    }
    if (id) {
      this.appointmentId = id
      this.serviceType = (options.serviceType || 'door-cleaning').toString().toLowerCase()
      if (this.serviceType === 'litter' || this.serviceType === 'medical') {
        this.serviceType = this.serviceType === 'litter' ? 'door-cleaning' : 'hospital'
      }
      this.scrollToRating = options.focus === 'rating'
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
        this.loadError = false
        uni.showLoading({
          title: '加载中...'
        })

        // 根据服务类型调用对应的详情接口?
        let res
        const type = this.serviceType
        if (type === 'hospital') {
          res = await api.getHospitalAppointmentDetail(this.appointmentId)
        } else if (type === 'grooming') {
          res = await api.getGroomingAppointmentDetail(this.appointmentId)
        } else if (type === 'adoption') {
          res = await api.getAdoptionAppointmentDetail(this.appointmentId)
        } else {
          // 默认上门清洁/通用
          res = await api.getAppointmentDetail(this.appointmentId)
        }
        
        const codeVal = res && res.code
        const ok = res && (Number(codeVal) === 200 || Number(codeVal) === 0 || codeVal === '200' || codeVal === '0' || codeVal === 200 || codeVal === 0)
        let data = res && (res.data != null ? res.data : res.result)
        if (ok && data && typeof data === 'object' && !Array.isArray(data) && data.data != null && data.id == null) {
          data = data.data
        }
        if (ok && data) {
          const pick = (k, ...altKeys) => {
            if (data[k] != null && data[k] !== '') return data[k]
            for (const key of altKeys) if (data[key] != null && data[key] !== '') return data[key]
            return null
          }
          const normalizedDate = Array.isArray(data.date)
            ? data.date
            : (this._parseDateToArray(pick('appointmentDate', 'appointment_date') || pick('date')) || this._parseDateToArray(data.startDate) || null)

          this.appointmentDetail = {
            ...data,
            id: data.id,
            serviceType: pick('serviceType', 'service_type') || type,
            date: normalizedDate || pick('date') || pick('appointment_date'),
            timeSlot: pick('timeSlot', 'time_slot'),
            location: pick('location'),
            remark: pick('remark'),
            createTime: pick('createTime', 'create_time'),
            updateTime: pick('updateTime', 'update_time'),
            status: pick('status'),
            rejectReason: pick('rejectReason', 'reject_reason'),
            reject_reason: pick('rejectReason', 'reject_reason')
          }

          // 生成显示用核销码：优先后端verifyCode，否则使用预约ID生成本地核销?
          this.displayVerifyCode = (this.appointmentDetail.verifyCode && String(this.appointmentDetail.verifyCode)) 
            || `APPT-${(type || 'svc').toUpperCase()}-${this.appointmentDetail.id}`
          this.showQrCode = true

          // 生成二维?
          this.generateQRCode()

          // 加载宠物信息
          if (this.appointmentDetail.petId) {
            await this.loadPetInfo(this.appointmentDetail.petId)
          }
          // 检查是否已评价（仅支持 door-cleaning, grooming, hospital?
          const type = this.appointmentDetail.serviceType || this.serviceType
          if (this.appointmentDetail.status === 'completed' && this.appointmentDetail.memberId &&
              (type === 'door-cleaning' || type === 'grooming' || type === 'hospital')) {
            this.checkIfRated()
          }
          // 从「评价」进入时，滚动到服务评价区域
          if (this.scrollToRating && this.showRatingSection) {
            this.$nextTick(() => {
              setTimeout(() => this.doScrollToRatingSection(), 200)
            })
          }
        } else {
          this.loadError = true
          uni.showToast({
            title: '加载预约详情失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载预约详情失败:', error)
        this.loadError = true
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
        const res = await api.getPetById(petId)
        if ((res.code === 0 || res.code === 200) && res.data) {
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
        'hospital': '宠物医院服务',
        'medical': '医疗服务',
        'grooming': '美容服务',
        'adoption': '领养服务',
        'door-cleaning': '上门清洁服务'
      }
      return typeMap[serviceType] || serviceType
    },


    _parseDateToArray(val) {
      if (val == null) return null
      const s = String(val).trim()
      if (!s) return null
      const dateOnly = s.indexOf('T') >= 0 ? s.substring(0, s.indexOf('T')) : s.substring(0, 10)
      const parts = dateOnly.split('-').map(n => parseInt(n, 10))
      if (parts.length >= 3 && !parts.some(isNaN)) {
        return [parts[0], parts[1], parts[2]]
      }
      return null
    },

    // 格式化日?
    formatDate(dateArrayOrString) {
      if (!dateArrayOrString) return ''
      // 兼容字符?'YYYY-MM-DD' ?'YYYY-MM-DDTHH:mm:ss'
      if (typeof dateArrayOrString === 'string') {
        const arr = this._parseDateToArray(dateArrayOrString)
        return arr ? `${arr[0]}-${String(arr[1]).padStart(2, '0')}-${String(arr[2]).padStart(2, '0')}` : dateArrayOrString.substring(0, 10)
      }
      // 兼容数组 [YYYY,MM,DD]
      if (Array.isArray(dateArrayOrString) && dateArrayOrString.length >= 3) {
        const year = dateArrayOrString[0]
        const month = dateArrayOrString[1]
        const day = dateArrayOrString[2]
        const d = parseInt(day, 10)
        const safeDay = (d >= 1 && d <= 31) ? d : 1
        return `${year}-${String(month).padStart(2, '0')}-${String(safeDay).padStart(2, '0')}`
      }
      return ''
    },

    // 格式化日期时间（iOS兼容?
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const str = typeof dateTime === 'string' ? dateTime.replace(/ /g, 'T') : dateTime
      const date = new Date(str)
      if (isNaN(date)) return ''
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      const hh = String(date.getHours()).padStart(2, '0')
      const mm = String(date.getMinutes()).padStart(2, '0')
      return `${y}-${m}-${d} ${hh}:${mm}`
    },

    calculateUtf8Length(str) {
      let len = 0
      for (let i = 0; i < str.length; i++) {
        const code = str.charCodeAt(i)
        if (code <= 0x7f) {
          len += 1
        } else if (code <= 0x7ff) {
          len += 2
        } else if (code >= 0xd800 && code <= 0xdbff) {
          len += 4
          i++
        } else if (code <= 0xffff) {
          len += 3
        } else {
          len += 4
        }
      }
      return len
    },

    buildQrPayload() {
      const QR_BYTE_LIMIT = 260
      const QR_CHAR_LIMIT = 260
      const basePayload = {
        t: 'verify',
        c: this.displayVerifyCode,
        o: this.appointmentDetail.id,
        s: (this.appointmentDetail.serviceType || this.serviceType || '').toString()
      }

      const optionalFields = [
        ['d', this.formatDate(this.appointmentDetail.date)],
        ['tm', this.appointmentDetail.timeSlot],
        ['l', this.appointmentDetail.location]
      ]

      const payload = { ...basePayload }

      optionalFields.forEach(([key, rawValue]) => {
        if (rawValue === undefined || rawValue === null || rawValue === '') return
        let value = typeof rawValue === 'string' ? rawValue : String(rawValue)

        if (key === 'l') {
          value = value.trim()
          if (value.length > 60) {
            value = value.slice(0, 60)
            // 静默处理警告
          }
        }

        const candidate = { ...payload, [key]: value }
        const candidateString = JSON.stringify(candidate)
        if (
          this.calculateUtf8Length(candidateString) <= QR_BYTE_LIMIT &&
          candidateString.length <= QR_CHAR_LIMIT
        ) {
          payload[key] = value
        } else {
          // 静默处理警告
        }
      })

      const finalString = JSON.stringify(payload)
      if (
        this.calculateUtf8Length(finalString) > QR_BYTE_LIMIT ||
        finalString.length > QR_CHAR_LIMIT
      ) {
        // 静默处理警告
        return this.getMinimalQrPayload()
      }

      return payload
    },

    getMinimalQrPayload() {
      return {
        t: 'verify',
        c: this.displayVerifyCode,
        o: this.appointmentDetail.id
      }
    },

    enforceQrLimit() {
      const QR_BYTE_LIMIT = 260
      const QR_CHAR_LIMIT = 260
      const minimal = JSON.stringify(this.getMinimalQrPayload())

      if (!this.qrCodeData) {
        this.qrCodeData = minimal
        return
      }

      const stringLength = this.qrCodeData.length
      const byteLength = this.calculateUtf8Length(this.qrCodeData)
      if (stringLength > QR_CHAR_LIMIT || byteLength > QR_BYTE_LIMIT) {
        // 静默处理警告
        this.qrCodeData = minimal
      }
    },

    // 取消预约：服务开始前 0-2 小时内取消将?40% 违约?
    getCancelPenaltyInfo() {
      const d = this.appointmentDetail
      if (!d) return { inPenaltyWindow: false, penaltyAmount: 0 }
      const date = d.date
      const timeSlot = d.timeSlot || ''
      const price = Number(d.price) || 0
      if (!date || !timeSlot || timeSlot.indexOf('-') === -1) {
        return { inPenaltyWindow: false, penaltyAmount: 0 }
      }
      const dateStr = Array.isArray(date) ? `${date[0]}-${String(date[1]).padStart(2, '0')}-${String(date[2]).padStart(2, '0')}` : String(date).split('T')[0]
      const startPart = timeSlot.split('-')[0].trim()
      const serviceStart = new Date(dateStr + 'T' + startPart + ':00').getTime()
      const now = Date.now()
      const twoHours = 2 * 60 * 60 * 1000
      const inPenaltyWindow = now >= serviceStart - twoHours && now < serviceStart
      const penaltyAmount = inPenaltyWindow ? Math.round(price * 0.4 * 100) / 100 : 0
      return { inPenaltyWindow, penaltyAmount }
    },
    // 是否已被服务人员确认（仅此类订单?0-2h 内取消才扣违约金?
    isStaffConfirmed() {
      const s = (this.appointmentDetail && this.appointmentDetail.status) || ''
      return ['confirmed', 'assigned', '已确认?', '已分配?'].indexOf(s) >= 0
    },
    cancelAppointment() {
      // 统一改为取消申请流程：用户填写原因，工作人员审核后生效
      const status = (this.appointmentDetail && this.appointmentDetail.status) || ''
      if (['pending', 'confirmed', 'change_pending', 'assigned'].indexOf(status) < 0) {
        uni.showToast({ title: '当前状态不可取消', icon: 'none' })
        return
      }
      const { inPenaltyWindow, penaltyAmount } = this.getCancelPenaltyInfo()
      const applyPenalty = inPenaltyWindow && this.isStaffConfirmed()
      if (applyPenalty) {
        uni.showToast({
          title: `若审核通过将收取¥${penaltyAmount.toFixed(2)}违约金`,
          icon: 'none',
          duration: 2200
        })
      }
      this.cancelReason = ''
      this.showCancelReasonPopup = true
    },

    async submitCancelRequest() {
      const reason = (this.cancelReason || '').trim()
      if (!reason) {
        uni.showToast({ title: '请填写取消原因', icon: 'none' })
        return
      }
      const appointmentType = this.serviceType === 'door-cleaning' ? 'door_cleaning' : this.serviceType
      const uid = uni.getStorageSync('userId') || (this.$store && (this.$store.getters?.userInfo?.id ?? this.$store.getters?.userInfo?.uid))
      if (!uid) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      try {
        uni.showLoading({ title: '提交中...' })
        const result = await api.submitCancellationRequest({
          userId: Number(uid),
          appointmentType,
          appointmentId: Number(this.appointmentId),
          reason
        })
        uni.hideLoading()
        if (result.code === 200 || result.code === 0) {
          this.showCancelReasonPopup = false
          this.cancelReason = ''
          uni.showToast({ title: '已提交取消申请，请等待工作人员确认', icon: 'success' })
          this.loadAppointmentDetail()
        } else {
          uni.showToast({ title: result.msg || result.message || '提交失败', icon: 'none' })
        }
      } catch (error) {
        console.error('提交取消申请失败:', error)
        uni.hideLoading()
        uni.showToast({ title: error.message || '提交失败，请稍后重试', icon: 'none' })
      }
    },

    checkIfRated() {
      api.checkAppointmentRated(this.serviceType, this.appointmentId).then(res => {
        if ((res.code === 0 || res.code === 200) && res.data && res.data.rated) {
          this.alreadyRated = true
          this.ratingDisplay = res.data.rating || 0
        }
      }).catch(() => {})
    },
    setRating(n) {
      this.ratingValue = n
    },
    async submitRating() {
      if (this.ratingValue < 1 || this.ratingValue > 5) {
        uni.showToast({ title: '请选择评分', icon: 'none' })
        return
      }
      this.ratingSubmitting = true
      try {
        const res = await api.submitAppointmentRating({
          appointmentType: this.serviceType,
          appointmentId: this.appointmentId,
          rating: this.ratingValue,
          comment: this.ratingComment || undefined
        })
        if (res.code === 0 || res.code === 200) {
          uni.showToast({ title: '评价成功', icon: 'success' })
          this.alreadyRated = true
          this.ratingDisplay = this.ratingValue
        } else {
          uni.showToast({ title: res.msg || '评价失败', icon: 'none' })
        }
      } catch (e) {
        uni.showToast({ title: e.message || '评价失败，请重试', icon: 'none' })
      } finally {
        this.ratingSubmitting = false
      }
    },

    // 联系客服（跳转门店客服聊天）
    contactService() {
      const storeId = this.appointmentDetail?.storeId ?? this.appointmentDetail?.store_id
      if (!storeId) {
        uni.showModal({
          title: '联系客服',
          content: '客服电话�?00-123-4567\n工作时间�?:00-18:00',
          showCancel: false,
          confirmText: '知道?'
        })
        return
      }
      const uid = uni.getStorageSync('userId') || (this.$store?.getters?.userInfo?.id ?? this.$store?.getters?.userInfo?.uid)
      if (!uid) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/user/chat?isService=true&isOutlet=true&storeId=${storeId}`
      })
    },

    // 申请变更（跳转变更申请页?
    goToChangeRequest() {
      const type = this.appointmentDetail.serviceType || this.serviceType || 'door-cleaning'
      uni.navigateTo({
        url: `/appointment/change-request?id=${this.appointmentId}&serviceType=${type}`
      })
    },

    // 取消变更：仅工作人员未确认时可取?
    async cancelChangeRequest() {
      const type = this.appointmentDetail.serviceType || this.serviceType || 'door-cleaning'
      const appointmentType = type === 'door-cleaning' ? 'door_cleaning' : type
      const uid = this.$store.getters.userInfo?.uid ?? this.$store.getters.userInfo?.id ?? uni.getStorageSync('userId')
      if (!uid) {
        uni.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      uni.showModal({
        title: '确认取消变更',
        content: '确定要取消本次变更申请吗？取消后预约将恢复为已确认状态?',
        success: async (res) => {
          if (!res.confirm) return
          try {
            uni.showLoading({ title: '处理中...' })
            const result = await api.cancelAppointmentChangeRequest({
              userId: Number(uid),
              appointmentType,
              appointmentId: this.appointmentId
            })
            uni.hideLoading()
            if (result.code === 200 || result.code === 0) {
              uni.showToast({ title: '已取消变更', icon: 'success' })
              this.loadAppointmentDetail()
            } else {
              uni.showToast({ title: result.msg || '取消失败', icon: 'none' })
            }
          } catch (e) {
            uni.hideLoading()
            uni.showToast({ title: e.message || '取消失败，请重试', icon: 'none' })
          }
        }
      })
    },

    // 返回
    goBack() {
      uni.navigateBack()
    },

    // 滚动到服务评价区域（从「评价」进入时调用?
    doScrollToRatingSection() {
      const query = uni.createSelectorQuery().in(this)
      query.select('#rating-section').boundingClientRect()
      query.selectViewport().scrollOffset()
      query.exec((res) => {
        if (!res || !res[0] || !res[1]) return
        const rect = res[0]
        const scroll = res[1]
        const scrollTop = scroll.scrollTop + rect.top - 80
        uni.pageScrollTo({
          scrollTop: Math.max(0, scrollTop),
          duration: 300
        })
      })
    },

    // 生成二维?
    generateQRCode() {
      try {
        const qrData = this.buildQrPayload()
        this.qrCodeData = JSON.stringify(qrData)
        this.enforceQrLimit()
        const stringLength = this.qrCodeData.length
        const byteLength = this.calculateUtf8Length(this.qrCodeData)
        this.generateQRCodeUrl()
      } catch (error) {
        console.error('生成二维码失败:', error)
      }
    },

    // 生成二维码 URL（在线图片 + 占位；不再使用页面内 canvas，避免部分机型白屏）
    async generateQRCodeUrl() {
      try {
        const encodedData = encodeURIComponent(this.qrCodeData)
        this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodedData}&ecc=M`
      } catch (error) {
        console.error('生成二维码地址失败', error)
        this.setPlaceholderQRCode()
      }
    },

    setPlaceholderQRCode() {
      const svg = `<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'><rect width='200' height='200' fill='%23f5f5f5'/><rect x='10' y='10' width='180' height='180' fill='none' stroke='%23999999' stroke-width='4'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='16' fill='%23666666'>核销?${this.displayVerifyCode || ''}</text></svg>`
      this.qrCodeUrl = `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`
    },

    // 二维码图片加载失败处?
    async onQrImageError(e) {
      console.error('二维码图片加载失败', e)
      try {
        const encodedData = encodeURIComponent(this.qrCodeData || JSON.stringify(this.getMinimalQrPayload()))
        this.qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=280x280&data=${encodedData}&ecc=L`
      } catch (err) {
        this.setPlaceholderQRCode()
      }
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

.detail-loading {
  padding: 120rpx 30rpx;
  text-align: center;
}
.detail-loading-text {
  color: #999;
  font-size: 28rpx;
}
.detail-error .detail-loading-text {
  color: #333;
}
.detail-error-desc {
  display: block;
  color: #999;
  font-size: 26rpx;
  margin-top: 16rpx;
}
.detail-error .retry-btn {
  margin-top: 32rpx;
  padding: 20rpx 48rpx;
  font-size: 28rpx;
  color: #fff;
  background-color: #07c160;
  border-radius: 12rpx;
  border: none;
}

// 状态提示（放在预约信息白框内顶部，简洁样式）
.status-desc-inline {
  font-size: 26rpx;
  color: #e53935;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.reject-reason-inline {
  font-size: 26rpx;
  margin-bottom: 16rpx;
  padding: 16rpx;
  background: #fff8f0;
  border-radius: 8rpx;
  border-left: 4rpx solid #ff9800;
  .reject-reason-label { color: #666; }
  .reject-reason-value { color: #333; }
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
  font-weight: normal;
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
}

.info-item .time-past-hint {
  font-size: 24rpx;
  color: #999999;
  margin-left: 8rpx;
}

// 核销信息
.verify-info {
  display: flex;
  flex-direction: column;
}

// 二维码区�?
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
  font-weight: normal;
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
  background-color: #ffd700;
  color: #333;
}

.cancel-button {
  background-color: #ffd700;
  color: #333333;
}

.contact-button {
  background-color: #1890ff;
  color: #ffffff;
}

.change-button {
  background-color: #8D9F5E;
  color: #ffffff;
}

.cancel-change-button {
  background: #e8e8e8;
  color: #666;
}
.change-pending-hint {
  background-color: #f0f0f0;
  color: #999;
}

.button-text {
  font-size: 28rpx;
  font-weight: bold;
}

// 取消原因弹窗
.cancel-reason-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}
.cancel-reason-popup {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 36rpx;
}
.cancel-reason-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 24rpx;
  color: #333;
}
.cancel-reason-input {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  border: 1rpx solid #e8e8e8;
  border-radius: 12rpx;
  font-size: 28rpx;
  box-sizing: border-box;
  margin-bottom: 16rpx;
}
.cancel-penalty-hint {
  font-size: 24rpx;
  color: #faad14;
  margin-bottom: 24rpx;
  line-height: 1.5;
}
.cancel-reason-actions {
  display: flex;
  gap: 24rpx;
}
.cancel-reason-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: bold;
}
.cancel-reason-btn-cancel {
  background: #f0f0f0;
  color: #666;
}
.cancel-reason-btn-submit {
  background: #8D9F5E;
  color: #fff;
}

// 状态样�?
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

// 服务评价
.rating-done {
  .rating-done-text {
    font-size: 26rpx;
    color: #52c41a;
    margin-bottom: 12rpx;
  }
}
.rating-stars-display,
.rating-stars {
  display: flex;
  gap: 8rpx;
  margin: 16rpx 0;
  .star {
    font-size: 44rpx;
    color: #e0e0e0;
    &.active {
      color: #ffc107;
    }
  }
}
.rating-stars .star.clickable {
  cursor: pointer;
}
.rating-form .rating-label {
  font-size: 26rpx;
  color: #666;
  margin-bottom: 8rpx;
}
.rating-input {
  width: 100%;
  min-height: 120rpx;
  margin-top: 16rpx;
  padding: 16rpx;
  border: 1rpx solid #e8e8e8;
  border-radius: 12rpx;
  font-size: 26rpx;
  box-sizing: border-box;
}
.submit-rating-btn {
  margin-top: 24rpx;
  background: #ffd700;
  color: #333;
  border: none;
  border-radius: 40rpx;
  height: 80rpx;
  line-height: 80rpx;
  font-size: 28rpx;
  font-weight: bold;
}
.submit-rating-btn[disabled] {
  opacity: 0.6;
}
</style>
