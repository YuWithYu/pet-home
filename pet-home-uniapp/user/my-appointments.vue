<template>
  <view class="my-appointments-page">
  <view class="appointments-container">
    <view class="tabs">
      <view
        v-for="tab in tabs"
        :key="tab.status"
        :class="['tab-item', { 'active': currentTab === tab.status }]"
        @tap="switchTab(tab.status)"
      >
        {{ tab.label }}
      </view>
    </view>

    <view class="appointments-list">
      <view v-if="loading" class="loading-container">
        <view class="loading-text">{{ ui.loadingText }}</view>
      </view>

      <view v-else-if="appointments.length === 0" class="empty-state">
        <view class="empty-icon">{{ ui.emptyIcon }}</view>
        <view class="empty-text">{{ loadError ? ui.loadFailTitle : ui.emptyTitle }}</view>
        <view class="empty-desc">{{ loadError ? ui.loadFailDesc : ui.emptyDesc }}</view>
        <button v-if="loadError" class="go-book-btn retry-btn" @tap="loadAppointments">{{ ui.btnRetry }}</button>
        <button v-else class="go-book-btn" @tap="goToServices">{{ ui.btnGoBook }}</button>
      </view>

      <view v-else class="appointment-cards">
        <view
          v-for="(appointment, index) in appointments"
          :key="appointmentKey(appointment, index)"
          class="appointment-card"
          :data-idx="index"
          @tap="onAppointmentCardTap"
        >
          <view class="card-header">
            <view class="service-type">
              <image class="service-icon" :src="getServiceIconUrl(appointment.serviceType)" mode="aspectFit" />
              <view class="service-name">{{ getServiceName(appointment.serviceType) }}</view>
            </view>
            <view :class="['status-badge', getStatusClass(appointment.status)]">
              {{ getStatusText(appointment.status) }}
            </view>
          </view>

          <view class="card-body">
            <view class="info-row">
              <view class="info-label">{{ ui.lblTime }}</view>
              <view class="info-value">{{ appointment.date }} {{ appointment.timeSlot }}</view>
            </view>
            <view class="info-row">
              <view class="info-label">{{ ui.lblAddr }}</view>
              <view class="info-value">{{ appointment.location || ui.noAddr }}</view>
            </view>
            <view class="info-row">
              <view class="info-label">{{ ui.lblPhone }}</view>
              <view class="info-value">{{ appointment.contactPhone }}</view>
            </view>
            <view v-if="appointment.status === 'cancelled' && (appointment.rejectReason || appointment.reject_reason)" class="info-row reject-reason-row">
              <view class="info-label">{{ ui.lblReject }}</view>
              <view class="info-value reject-reason-value">{{ appointment.rejectReason || appointment.reject_reason }}</view>
            </view>
            <view v-if="appointment.status === 'no_show' && (appointment.rejectReason || appointment.reject_reason)" class="info-row no-show-row">
              <view class="info-label">失约原因</view>
              <view class="info-value no-show-value">{{ appointment.rejectReason || appointment.reject_reason }}</view>
            </view>
          </view>

          <view class="card-footer">
            <view class="price-section">
              <text class="price-label">{{ ui.lblPrice }}</text>
              <text class="price-value">{{ ui.yuan }}{{ appointment.price || '0.00' }}</text>
              <text v-if="appointment.status === 'no_show' && (appointment.cancellationPenaltyAmount || appointment.cancellation_penalty_amount)" class="penalty-tag">违约金 {{ ui.yuan }}{{ appointment.cancellationPenaltyAmount || appointment.cancellation_penalty_amount }}</text>
            </view>
            <view class="action-buttons">
              <button
                v-if="canRequestCancellation(appointment)"
                class="btn btn-cancel"
                :data-idx="index"
                @tap.stop="onCancelAppointmentTap"
              >
                {{ ui.btnCancelAppt }}
              </button>
              <button
                v-if="appointment.status === 'confirmed' && canApplyChange(appointment)"
                class="btn btn-change"
                :data-idx="index"
                @tap.stop="onGoToChangeRequestTap"
              >
                {{ ui.btnApplyChange }}
              </button>
              <button
                v-if="appointment.status === 'confirmed' || appointment.status === 'completed'"
                class="btn btn-contact"
                :data-idx="index"
                @tap.stop="onContactServiceTap"
              >
                {{ ui.btnContact }}
              </button>
              <button
                v-if="appointment.status === 'change_pending'"
                class="btn btn-cancel-change"
                :data-idx="index"
                @tap.stop="onCancelChangeRequestTap"
              >
                {{ ui.btnCancelChangeReq }}
              </button>
              <button
                v-if="appointment.status === 'completed' && canRate(appointment)"
                class="btn btn-rate"
                :data-idx="index"
                @tap.stop="onGoToRateTap"
              >
                {{ ui.btnRate }}
              </button>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>

  <view v-if="cancelRequestPopupVisible" class="cancel-request-mask" @tap="closeCancelRequestPopup">
    <view class="cancel-request-popup" @tap.stop>
      <view class="cancel-request-title">取消预约申请</view>
      <view v-if="cancelRequestPenaltyTip" class="cancel-request-tip">{{ cancelRequestPenaltyTip }}</view>
      <textarea
        v-model="cancelRequestReason"
        class="cancel-request-input"
        placeholder="请填写取消原因（必填）"
        maxlength="200"
      />
      <view class="cancel-request-actions">
        <button class="btn-secondary" @tap="closeCancelRequestPopup">取消</button>
        <button class="btn-primary" :disabled="cancelRequestSubmitting" @tap="submitCancellationRequest">提交申请</button>
      </view>
    </view>
  </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { mapGetters } from 'vuex'

export default {
  name: 'MyAppointments',

  data() {
    return {
      // Unicode escapes: avoid source encoding corrupting Chinese / currency in mini program build
      ui: {
        loadingText: '\u52a0\u8f7d\u4e2d...',
        emptyIcon: '\uD83D\uDCC5',
        loadFailTitle: '\u52a0\u8f7d\u5931\u8d25',
        emptyTitle: '\u6682\u65e0\u9884\u7ea6',
        loadFailDesc: '\u8bf7\u4e0b\u62c9\u5237\u65b0\u6216\u7a0d\u540e\u91cd\u8bd5',
        emptyDesc: '\u60a8\u8fd8\u6ca1\u6709\u4efb\u4f55\u9884\u7ea6\uff0c\u53ef\u524d\u5f80\u9996\u9875\u9884\u7ea6\u670d\u52a1',
        btnRetry: '\u91cd\u8bd5',
        btnGoBook: '\u53bb\u9884\u7ea6',
        lblTime: '\u9884\u7ea6\u65f6\u95f4',
        lblAddr: '\u670d\u52a1\u5730\u5740',
        lblPhone: '\u8054\u7cfb\u7535\u8bdd',
        lblReject: '\u62d2\u7edd\u539f\u56e0',
        lblPrice: '\u9884\u7ea6\u91d1\u989d',
        yuan: '\u00a5',
        noAddr: '\u672a\u586b\u5199',
        btnCancelAppt: '\u53d6\u6d88\u9884\u7ea6',
        btnApplyChange: '\u7533\u8bf7\u6539\u671f',
        btnContact: '\u8054\u7cfb\u5ba2\u670d',
        btnCancelChangeReq: '\u53d6\u6d88\u6539\u671f',
        btnRate: '\u8bc4\u4ef7',
        loginFirst: '\u8bf7\u5148\u767b\u5f55',
        userInvalid: '\u7528\u6237\u4fe1\u606f\u5f02\u5e38',
        loadFailToast: '\u52a0\u8f7d\u5931\u8d25',
        noStoreInfo: '\u6682\u65e0\u95e8\u5e97\u4fe1\u606f',
        cancelTitle: '\u53d6\u6d88\u9884\u7ea6',
        cancelConfirm: '\u8bf7\u586b\u5199\u53d6\u6d88\u539f\u56e0\u5e76\u63d0\u4ea4\u5ba1\u6838',
        processing: '\u5904\u7406\u4e2d...',
        cancelledOk: '\u5df2\u63d0\u4ea4\u53d6\u6d88\u7533\u8bf7',
        cancelFail: '\u53d6\u6d88\u5931\u8d25',
        cancelReasonRequired: '\u8bf7\u586b\u5199\u53d6\u6d88\u539f\u56e0',
        cancelPendingTip: '\u8bf7\u7b49\u5f85\u5de5\u4f5c\u4eba\u5458\u5ba1\u6838\uff0c\u5ba1\u6838\u901a\u8fc7\u540e\u624d\u4f1a\u6b63\u5f0f\u53d6\u6d88',
        cancelChangeTitle: '\u53d6\u6d88\u6539\u671f',
        cancelChangeContent: '\u786e\u5b9a\u64a4\u9500\u6539\u671f\u7533\u8bf7\u5417\uff1f\u64a4\u9500\u540e\u5c06\u4fdd\u6301\u539f\u9884\u7ea6\u65f6\u95f4\u3002',
        revokedOk: '\u5df2\u64a4\u9500',
        opFail: '\u64cd\u4f5c\u5931\u8d25'
      },
      currentTab: 'all',
      appointments: [],
      loading: false,
      loadError: false,
      lastAppointmentsLoadTime: 0,
      cancelRequestPopupVisible: false,
      cancelRequestSubmitting: false,
      cancelRequestReason: '',
      cancelRequestPenaltyTip: '',
      cancelRequestAppointment: null,
      /** 并发加载时只采纳最后一次请求的结果，避免后到的失败请求把已成功列表清空 */
      appointmentsLoadSeq: 0
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn']),
    tabs() {
      return [
        { label: '\u5168\u90e8', status: 'all' },
        { label: '\u5f85\u786e\u8ba4', status: 'pending' },
        { label: '\u5df2\u786e\u8ba4', status: 'confirmed' },
        { label: '\u5df2\u5b8c\u6210', status: 'completed' },
        { label: '\u5df2\u53d6\u6d88', status: 'cancelled' }
      ]
    }
  },

  onLoad(options) {
    this.hideAllLoading()

    if (options.status) {
      this.currentTab = options.status
    }
    this.$nextTick(() => {
      setTimeout(() => {
        this.checkLogin()
      }, 50)
    })
  },

  onReady() {
    this.$nextTick(() => {
      this.hideAllLoading()
      setTimeout(() => {
        this.hideAllLoading()
      }, 50)
      setTimeout(() => {
        this.hideAllLoading()
      }, 100)
    })
  },

  onShow() {
    this.hideAllLoading()
    if (this.isLoggedIn) {
      const now = Date.now()
      const throttleMs = 15 * 1000
      if (now - this.lastAppointmentsLoadTime >= throttleMs || this.lastAppointmentsLoadTime === 0) {
        this.loadAppointments(true)
      }
    }
  },

  onPullDownRefresh() {
    this.loadAppointments(true).then(() => {}).catch(() => {}).finally(() => {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    hideAllLoading() {
      // #ifdef MP-WEIXIN
      try {
        wx.hideNavigationBarLoading()
        wx.hideLoading()
        wx.setNavigationBarLoading && wx.setNavigationBarLoading({ loading: false })
      } catch (e) {
      }
      // #endif
      try {
        uni.hideLoading()
        uni.hideNavigationBarLoading && uni.hideNavigationBarLoading()
      } catch (e) {
      }
    },

    checkLogin() {
      if (!this.isLoggedIn) {
        uni.showToast({
          title: this.ui.loginFirst,
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages-auth/login'
          })
        }, 1500)
        return false
      }
      this.loadAppointments()
      return true
    },

    /** 从 Vuex / 本地存储解析数字用户 ID（兼容 id、uid、userId、user_id） */
    resolveNumericUserId() {
      const fromStore = this.userInfo && (this.userInfo.uid ?? this.userInfo.id ?? this.userInfo.userId ?? this.userInfo.user_id)
      const fromStorageId = uni.getStorageSync('userId')
      let fromUserObj = null
      try {
        const u = uni.getStorageSync('userInfo')
        if (u && typeof u === 'object') {
          fromUserObj = u.uid ?? u.id ?? u.userId ?? u.user_id
        } else if (u && (typeof u === 'string' || typeof u === 'number')) {
          fromUserObj = u
        }
      } catch (e) {}
      const raw = fromStore ?? fromStorageId ?? fromUserObj
      if (raw == null || raw === '') return NaN
      const n = parseInt(String(raw).trim(), 10)
      return !isNaN(n) && n >= 1 ? n : NaN
    },

    /** 统一接口 data 可能是数组、JSON 字符串、或 { records/list } */
    unwrapAppointmentList(data) {
      if (Array.isArray(data)) return data
      if (data == null) return []
      if (typeof data === 'string') {
        const t = data.trim()
        if (!t) return []
        try {
          const p = JSON.parse(t)
          return Array.isArray(p) ? p : []
        } catch (e) {
          return []
        }
      }
      if (typeof data === 'object') {
        if (Array.isArray(data.records)) return data.records
        if (Array.isArray(data.list)) return data.list
        if (Array.isArray(data.rows)) return data.rows
      }
      return []
    },

    normalizeAppointmentStatus(status) {
      return status == null ? '' : String(status).trim().toLowerCase()
    },

    async loadAppointments(silent = false) {
      const seq = ++this.appointmentsLoadSeq
      const userId = this.resolveNumericUserId()
      if (!userId || isNaN(userId) || userId < 1) {
        if (!silent) {
          uni.showToast({ title: this.ui.userInvalid, icon: 'none' })
        }
        if (seq === this.appointmentsLoadSeq) {
          this.appointments = []
          this.loading = false
        }
        return
      }

      const showLoading = !silent
      try {
        if (seq === this.appointmentsLoadSeq) {
          this.loading = true
          this.loadError = false
        }

        const isOk = (r) => r && (r.code === 200 || r.code === 0 || Number(r.code) === 200 || Number(r.code) === 0)
        let allAppointments = []

        try {
          const unifiedRes = await api.getUserAppointmentsUnified(userId, showLoading)
          const listPayload = this.unwrapAppointmentList(unifiedRes && unifiedRes.data)
          if (isOk(unifiedRes)) {
            allAppointments = listPayload.map((a) => {
              const raw = a && a.serviceType != null ? String(a.serviceType).toLowerCase() : ''
              let st = 'grooming'
              if (raw === 'door-cleaning' || raw === 'door_cleaning' || raw === 'litter') st = 'door-cleaning'
              else if (raw === 'hospital' || raw === 'medical') st = 'hospital'
              else if (raw === 'grooming') st = 'grooming'
              return {
                ...a,
                date: a.date || a.appointmentDate,
                serviceType: st
              }
            })
          }
        } catch (e) {
          console.warn('[appointments] unified', e && (e.message || e.errMsg || String(e)))
        }

        if (allAppointments.length === 0) {
          const [doorCleaningRes, hospitalRes, groomingRes] = await Promise.all([
            api.getUserAppointments(userId, false).catch(err => ({ code: -1, data: null, _err: err })),
            api.getUserHospitalAppointments(userId, false).catch(err => ({ code: -1, data: null, _err: err })),
            api.getUserGroomingAppointments(userId, false).catch(err => ({ code: -1, data: null, _err: err }))
          ])
          const doorOk = isOk(doorCleaningRes)
          const hospitalOk = isOk(hospitalRes)
          const groomingOk = isOk(groomingRes)
          if (!doorOk && !hospitalOk && !groomingOk) {
            if (seq === this.appointmentsLoadSeq) {
              this.loadError = true
            }
            const firstErr = doorCleaningRes._err || hospitalRes._err || groomingRes._err
            if (firstErr) {
              console.error('[appointments] all apis failed', firstErr)
            }
          }
          const doorList = this.unwrapAppointmentList(doorCleaningRes && doorCleaningRes.data)
          if (doorOk) {
            allAppointments = allAppointments.concat(doorList.map(appointment => ({
              ...appointment,
              date: appointment.date || appointment.appointmentDate,
              serviceType: 'door-cleaning'
            })))
          }
          const hospitalList = this.unwrapAppointmentList(hospitalRes && hospitalRes.data)
          if (hospitalOk) {
            allAppointments = allAppointments.concat(hospitalList.map(appointment => ({
              ...appointment,
              date: appointment.date || appointment.appointmentDate,
              serviceType: 'hospital'
            })))
          }
          const groomingList = this.unwrapAppointmentList(groomingRes && groomingRes.data)
          if (groomingOk) {
            allAppointments = allAppointments.concat(groomingList.map(appointment => ({
              ...appointment,
              date: appointment.date || appointment.appointmentDate,
              serviceType: 'grooming'
            })))
          }
        }

        allAppointments.sort((a, b) => {
          const rawA = a.createTime && typeof a.createTime === 'string' ? a.createTime.replace(/ /g, 'T') : ''
          const rawB = b.createTime && typeof b.createTime === 'string' ? b.createTime.replace(/ /g, 'T') : ''
          const dateA = rawA ? new Date(rawA).getTime() : 0
          const dateB = rawB ? new Date(rawB).getTime() : 0
          return dateB - dateA
        })

        if (this.currentTab !== 'all') {
          if (this.currentTab === 'confirmed') {
            allAppointments = allAppointments.filter((a) => {
              const s = this.normalizeAppointmentStatus(a.status)
              return s === 'confirmed' || s === 'change_pending' || s === 'cancel_pending'
            })
          } else {
            const tab = this.currentTab
            allAppointments = allAppointments.filter((a) => this.normalizeAppointmentStatus(a.status) === tab)
          }
        }

        await Promise.all(allAppointments.map(async (a) => {
          try {
            const type = a.serviceType === 'door-cleaning' ? 'door_cleaning' : (a.serviceType || '')
            if (!type || !a.id) {
              a.hasAnyChangeRequest = false
              return
            }
            const r = await api.hasAnyAppointmentChangeRequest(type, a.id)
            a.hasAnyChangeRequest = !!(r && (r.code === 200 || r.code === 0) && r.data === true)
          } catch (e) {
            a.hasAnyChangeRequest = false
          }
        }))

        if (seq === this.appointmentsLoadSeq) {
          this.appointments = allAppointments
          this.lastAppointmentsLoadTime = Date.now()
        }
      } catch (error) {
        console.error('loadAppointments', error)
        if (seq === this.appointmentsLoadSeq) {
          this.appointments = []
          this.loadError = true
          if (!silent) {
            uni.showToast({
              title: this.ui.loadFailToast,
              icon: 'none'
            })
          }
        }
      } finally {
        if (seq === this.appointmentsLoadSeq) {
          this.loading = false
        }
      }
    },

    switchTab(status) {
      this.currentTab = status
      this.loadAppointments()
    },

    appointmentKey(appointment, index) {
      const i = typeof index === 'number' ? index : 0
      const id = appointment && appointment.id
      if (id !== undefined && id !== null && id !== '') return id
      return 100001 + i
    },

    appointmentFromIdx(e) {
      const raw = e && e.currentTarget && e.currentTarget.dataset ? e.currentTarget.dataset.idx : undefined
      const idx = raw === undefined || raw === '' ? NaN : parseInt(raw, 10)
      if (isNaN(idx) || idx < 0 || idx >= this.appointments.length) return null
      return this.appointments[idx]
    },

    onAppointmentCardTap(e) {
      const a = this.appointmentFromIdx(e)
      if (a) this.viewDetail(a)
    },

    onCancelAppointmentTap(e) {
      const a = this.appointmentFromIdx(e)
      if (a) this.openCancelRequestPopup(a)
    },

    onGoToChangeRequestTap(e) {
      const a = this.appointmentFromIdx(e)
      if (a) this.goToChangeRequest(a)
    },

    onContactServiceTap(e) {
      const a = this.appointmentFromIdx(e)
      if (a) this.contactService(a)
    },

    onCancelChangeRequestTap(e) {
      const a = this.appointmentFromIdx(e)
      if (a) this.cancelChangeRequest(a)
    },

    onGoToRateTap(e) {
      const a = this.appointmentFromIdx(e)
      if (a) this.goToRate(a)
    },

    navigateToAppointmentPackage(url) {
      const failToast = () => {
        uni.showToast({
          title: '\u65e0\u6cd5\u6253\u5f00\u9875\u9762',
          icon: 'none'
        })
      }
      const run = () => {
        uni.navigateTo({
          url,
          fail: (err) => {
            const em = err && err.errMsg ? String(err.errMsg) : ''
            if (em.indexOf('timeout') !== -1) {
              setTimeout(() => {
                uni.navigateTo({ url, fail: () => failToast() })
              }, 400)
              return
            }
            console.error('[my-appointments] navigateTo', url, err)
            failToast()
          }
        })
      }
      // #ifdef MP-WEIXIN
      if (typeof wx !== 'undefined' && wx.loadSubPackage) {
        wx.loadSubPackage({
          name: 'appointment',
          success: () => setTimeout(run, 32),
          fail: () => setTimeout(run, 32)
        })
        return
      }
      // #endif
      setTimeout(run, 32)
    },

    getServiceIconUrl(serviceType) {
      const t = (serviceType || '').toString().toLowerCase()
      const map = {
        'door-cleaning': '/static/images/\u94f2\u5c4e.png',
        litter: '/static/images/\u94f2\u5c4e.png',
        hospital: '/static/images/\u533b\u9662.png',
        medical: '/static/images/\u533b\u9662.png',
        grooming: '/static/images/\u6d17\u62a4.png'
      }
      return map[t] || '/static/images/\u94f2\u5c4e.png'
    },

    getServiceName(serviceType) {
      const t = (serviceType || '').toString().toLowerCase()
      const names = {
        'door-cleaning': '\u4e0a\u95e8\u94f2\u5c4e',
        litter: '\u4e0a\u95e8\u94f2\u5c4e',
        grooming: '\u5ba0\u7269\u7f8e\u5bb9',
        hospital: '\u5ba0\u7269\u533b\u9662',
        medical: '\u5ba0\u7269\u533b\u9662'
      }
      return names[t] || '\u670d\u52a1'
    },

    getStatusText(status) {
      const texts = {
        pending: '\u5f85\u786e\u8ba4',
        confirmed: '\u5df2\u786e\u8ba4',
        change_pending: '\u6539\u671f\u5ba1\u6838\u4e2d',
        cancel_pending: '\u53d6\u6d88\u5ba1\u6838\u4e2d',
        assigned: '\u5df2\u5206\u914d',
        'in-service': '\u670d\u52a1\u4e2d',
        completed: '\u5df2\u5b8c\u6210',
        cancelled: '\u5df2\u53d6\u6d88',
        no_show: '已失约'
      }
      return texts[status] || (status || '')
    },

    getStatusClass(status) {
      const classes = {
        pending: 'status-pending',
        confirmed: 'status-confirmed',
        change_pending: 'status-pending',
        cancel_pending: 'status-pending',
        assigned: 'status-confirmed',
        'in-service': 'status-in-service',
        completed: 'status-completed',
        cancelled: 'status-cancelled',
        no_show: 'status-no-show'
      }
      return classes[status] || ''
    },

    viewDetail(appointment) {
      if (!appointment) return
      const rawId = appointment.id != null ? appointment.id : appointment.appointmentId
      const id = rawId != null && rawId !== '' ? Number(rawId) : NaN
      if (!id || isNaN(id) || id < 1) {
        uni.showToast({
          title: '\u65e0\u6548\u7684\u9884\u7ea6',
          icon: 'none'
        })
        return
      }
      let st = (appointment.serviceType || 'door-cleaning').toString().toLowerCase()
      if (st === 'litter') st = 'door-cleaning'
      if (st === 'medical') st = 'hospital'
      const url =
        st === 'hospital'
          ? `/appointment/detail?id=${id}&serviceType=hospital`
          : `/appointment/detail?id=${id}&serviceType=${encodeURIComponent(st || 'door-cleaning')}`
      this.navigateToAppointmentPackage(url)
    },

    getCancelPenaltyInfo(appointment) {
      const date = appointment.date || appointment.appointmentDate
      const timeSlot = appointment.timeSlot || ''
      const price = Number(appointment.price) || 0
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

    isStaffConfirmed(appointment) {
      const s = (appointment && appointment.status) || ''
      return ['confirmed', 'assigned', 'in-service'].indexOf(s) >= 0
    },

    canRequestCancellation(appointment) {
      const s = (appointment && appointment.status) || ''
      return ['pending', 'confirmed', 'assigned'].indexOf(s) >= 0
    },

    openCancelRequestPopup(appointment) {
      if (!appointment || !appointment.id) return
      const { inPenaltyWindow, penaltyAmount } = this.getCancelPenaltyInfo(appointment)
      const applyPenalty = inPenaltyWindow && this.isStaffConfirmed(appointment)
      this.cancelRequestPenaltyTip = applyPenalty
        ? '\u8ddd\u670d\u52a1\u5f00\u59cb\u4e0d\u8db32\u5c0f\u65f6\uff0c\u82e5\u5ba1\u6838\u901a\u8fc7\u53d6\u6d88\uff0c\u5c06\u6536\u53d6\u7ea6' + this.ui.yuan + penaltyAmount.toFixed(2) + '\u7684\u8fdd\u7ea6\u91d1'
        : this.ui.cancelPendingTip
      this.cancelRequestAppointment = appointment
      this.cancelRequestReason = ''
      this.cancelRequestPopupVisible = true
    },

    closeCancelRequestPopup() {
      if (this.cancelRequestSubmitting) return
      this.cancelRequestPopupVisible = false
      this.cancelRequestReason = ''
      this.cancelRequestPenaltyTip = ''
      this.cancelRequestAppointment = null
    },

    async submitCancellationRequest() {
      const u = this.ui
      const target = this.cancelRequestAppointment
      const reason = (this.cancelRequestReason || '').trim()
      if (!target || !target.id) return
      if (!reason) {
        uni.showToast({ title: u.cancelReasonRequired, icon: 'none' })
        return
      }
      const uid = this.$store.getters.userInfo?.uid ?? this.$store.getters.userInfo?.id ?? uni.getStorageSync('userId')
      if (!uid) {
        uni.showToast({ title: u.loginFirst, icon: 'none' })
        return
      }
      const rawType = (target.serviceType || 'door-cleaning').toString().toLowerCase()
      const appointmentType = rawType === 'door-cleaning' ? 'door_cleaning' : rawType
      this.cancelRequestSubmitting = true
      try {
        uni.showLoading({ title: u.processing })
        const result = await api.submitCancellationRequest({
          userId: Number(uid),
          appointmentType,
          appointmentId: target.id,
          reason
        })
        uni.hideLoading()
        if (result.code === 200 || result.code === 0) {
          uni.showToast({ title: u.cancelledOk, icon: 'success' })
          this.closeCancelRequestPopup()
          this.loadAppointments()
        } else {
          uni.showToast({ title: result.msg || u.cancelFail, icon: 'none' })
        }
      } catch (error) {
        console.error('submitCancellationRequest', error)
        uni.hideLoading()
        uni.showToast({ title: error.message || u.opFail, icon: 'none' })
      } finally {
        this.cancelRequestSubmitting = false
      }
    },

    contactService(appointment) {
      const storeId = appointment.storeId ?? appointment.store_id
      if (!storeId) {
        uni.showToast({
          title: this.ui.noStoreInfo,
          icon: 'none'
        })
        return
      }
      const uid = this.userInfo?.id ?? this.userInfo?.uid ?? uni.getStorageSync('userId')
      if (!uid) {
        uni.showToast({ title: this.ui.loginFirst, icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/user/chat?isService=true&isOutlet=true&storeId=${storeId}`
      })
    },

    canRate(appointment) {
      const t = appointment.serviceType || ''
      return t === 'door-cleaning' || t === 'grooming' || t === 'hospital'
    },

    goToRate(appointment) {
      const type = appointment.serviceType || 'door-cleaning'
      this.navigateToAppointmentPackage(
        `/appointment/detail?id=${appointment.id}&serviceType=${type}&focus=rating`
      )
    },

    canApplyChange(appointment) {
      if (appointment && (appointment.hasAnyChangeRequest === true || appointment.has_any_change_request === true || Number(appointment.changeRequestCount || appointment.change_request_count || 0) > 0)) {
        return false
      }
      const date = appointment.date || appointment.appointmentDate
      const timeSlot = appointment.timeSlot || ''
      if (!date || !timeSlot || timeSlot.indexOf('-') === -1) return false
      const dateStr = Array.isArray(date) ? `${date[0]}-${String(date[1]).padStart(2, '0')}-${String(date[2]).padStart(2, '0')}` : String(date).split('T')[0]
      const startPart = timeSlot.split('-')[0].trim()
      const serviceStart = new Date(dateStr + 'T' + startPart + ':00').getTime()
      return Date.now() < serviceStart
    },

    goToChangeRequest(appointment) {
      const type = appointment.serviceType || 'door-cleaning'
      this.navigateToAppointmentPackage(
        `/appointment/change-request?id=${appointment.id}&serviceType=${type}`
      )
    },

    async cancelChangeRequest(appointment) {
      const u = this.ui
      const type = appointment.serviceType || 'door-cleaning'
      const appointmentType = type === 'door-cleaning' ? 'door_cleaning' : type
      const uid = this.$store.getters.userInfo?.uid ?? this.$store.getters.userInfo?.id ?? uni.getStorageSync('userId')
      if (!uid) {
        uni.showToast({ title: u.loginFirst, icon: 'none' })
        return
      }
      uni.showModal({
        title: u.cancelChangeTitle,
        content: u.cancelChangeContent,
        success: async (res) => {
          if (!res.confirm) return
          try {
            uni.showLoading({ title: u.processing })
            const result = await api.cancelAppointmentChangeRequest({
              userId: Number(uid),
              appointmentType,
              appointmentId: appointment.id
            })
            uni.hideLoading()
            if (result.code === 200 || result.code === 0) {
              uni.showToast({ title: u.revokedOk, icon: 'success' })
              this.loadAppointments()
            } else {
              uni.showToast({ title: result.msg || u.opFail, icon: 'none' })
            }
          } catch (e) {
            uni.hideLoading()
            uni.showToast({ title: e.message || u.opFail, icon: 'none' })
          }
        }
      })
    },

    goToServices() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.my-appointments-page {
  min-height: 100vh;
}

.appointments-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.tabs {
  display: flex;
  background-color: white;
  padding: 0 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 20rpx 8rpx;
    font-size: 24rpx;
    color: #666;
    position: relative;

    &.active {
      color: #667eea;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 6rpx;
        background-color: #667eea;
        border-radius: 3rpx;
      }
    }
  }
}

.appointments-list {
  padding: 20rpx;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100rpx 0;

  .loading-text {
    font-size: 24rpx;
    color: #999;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 150rpx 40rpx;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #333;
    margin-bottom: 20rpx;
  }

  .empty-desc {
    font-size: 24rpx;
    color: #999;
    margin-bottom: 60rpx;
  }

  .go-book-btn {
    background-color: #ffd700;
    color: #333;
    border: none;
    padding: 12rpx 32rpx;
    border-radius: 40rpx;
    font-size: 24rpx;
    font-weight: 500;
  }
  .go-book-btn.retry-btn {
    background-color: #f0f0f0;
    color: #666;
  }
}

.appointment-cards {
  .appointment-card {
    background-color: white;
    border-radius: 12rpx;
    padding: 16rpx 20rpx;
    margin-bottom: 12rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
  padding-bottom: 12rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .service-type {
    display: flex;
    align-items: center;

    .service-icon {
      width: 40rpx;
      height: 40rpx;
      margin-right: 10rpx;
      flex-shrink: 0;
      display: block;
    }

    .service-name {
      font-size: 26rpx;
      font-weight: bold;
      color: #333;
    }
  }

  .status-badge {
    padding: 6rpx 14rpx;
    border-radius: 16rpx;
    font-size: 20rpx;

    &.status-pending {
      background-color: #fff7e6;
      color: #ff9800;
    }

    &.status-confirmed {
      background-color: #e8f4fd;
      color: #2196f3;
    }

    &.status-in-service {
      background-color: #fff3e0;
      color: #ff6b35;
    }

    &.status-completed {
      background-color: #e8f5e9;
      color: #4caf50;
    }

    &.status-cancelled {
      background-color: #fafafa;
      color: #999;
    }

    &.status-no-show {
      background-color: #fee;
      color: #e53e3e;
    }
  }
}

.card-body {
  .info-row {
    display: flex;
    margin-bottom: 8rpx;
    font-size: 22rpx;

    .info-label {
      width: 120rpx;
      color: #999;
      flex-shrink: 0;
    }

    .info-value {
      flex: 1;
      color: #333;
    }
    &.reject-reason-row {
      margin-top: 8rpx;
      padding: 10rpx 12rpx;
      background: #fff8f0;
      border-radius: 8rpx;
      border-left: 4rpx solid #ff9800;
      .reject-reason-value { color: #333; }
    }
    &.no-show-row {
      margin-top: 8rpx;
      padding: 10rpx 12rpx;
      background: #fce4ec;
      border-radius: 8rpx;
      border-left: 4rpx solid #e91e63;
      .no-show-value { color: #c2185b; font-size: 24rpx; }
    }
  }
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10rpx;
  padding-top: 10rpx;
  border-top: 1rpx solid #f0f0f0;

  .price-section {
    display: flex;
    align-items: center;
    .price-label {
      font-size: 22rpx;
      color: #999;
    }

    .price-value {
      font-size: 26rpx;
      color: #ff6b35;
      font-weight: bold;
      margin-left: 6rpx;
    }

    .penalty-tag {
      margin-left: 12rpx;
      padding: 2rpx 10rpx;
      font-size: 20rpx;
      color: #e53e3e;
      background: #fee;
      border-radius: 6rpx;
      font-weight: bold;
    }
  }

  .action-buttons {
    display: flex;
    gap: 12rpx;

    .btn {
      padding: 6rpx 16rpx;
      border-radius: 16rpx;
      font-size: 20rpx;
      border: none;

      &.btn-cancel {
        background-color: #f5f5f5;
        color: #666;
      }

      &.btn-change {
        background-color: #ffffff;
        color: #8D9F5E;
        border: 1px solid #8D9F5E;
      }

      &.btn-change-pending {
        background: #e8e8e8;
        color: #999;
      }

      &.btn-cancel-change {
        background: #e8e8e8;
        color: #666;
      }

      &.btn-contact {
        background-color: #ffd700;
        color: #333;
        font-weight: bold;
      }

      &.btn-rate {
        background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
        color: white;
      }
    }
  }
}

.cancel-request-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.cancel-request-popup {
  width: 640rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx;
  box-sizing: border-box;
}

.cancel-request-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 16rpx;
}

.cancel-request-tip {
  font-size: 22rpx;
  color: #666;
  margin-bottom: 16rpx;
  line-height: 1.5;
}

.cancel-request-input {
  width: 100%;
  min-height: 170rpx;
  border: 1rpx solid #e6e6e6;
  border-radius: 12rpx;
  padding: 16rpx;
  box-sizing: border-box;
  font-size: 24rpx;
  margin-bottom: 20rpx;
}

.cancel-request-actions {
  display: flex;
  gap: 16rpx;
}

.cancel-request-actions .btn-secondary,
.cancel-request-actions .btn-primary {
  flex: 1;
  border-radius: 12rpx;
  font-size: 24rpx;
}

.cancel-request-actions .btn-secondary {
  background: #f2f2f2;
  color: #666;
}

.cancel-request-actions .btn-primary {
  background: #ffd700;
  color: #333;
}
</style>
