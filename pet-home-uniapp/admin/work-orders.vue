<template>
  <view class="work-orders-container">
    <view class="filter-row">
      <view
        v-for="item in statusTabs"
        :key="item.key"
        class="filter-pill"
        :class="{ active: activeStatus === item.key }"
        @tap="activeStatus = item.key"
      >
        {{ item.label }}
      </view>
    </view>
    <view class="tool-row">
      <input
        v-model="searchKeyword"
        class="search-input"
        placeholder="搜索订单号/联系人/电话/项目"
        placeholder-class="placeholder"
      />
      <picker mode="date" :value="selectedDate" @change="onDateChange">
        <view class="today-switch" :class="{ active: selectedDate !== '' }">
          {{ selectedDate || '选择日期' }}
        </view>
      </picker>
      <view class="batch-btn plain" @tap="clearDate">清空</view>
    </view>

    <view v-if="loading" class="loading-state">
      <view class="loading-spinner"></view>
      <text>加载中...</text>
    </view>

    <view v-else-if="displayOrders.length === 0" class="empty-state">
      <view class="empty-icon">📋</view>
      <text class="empty-text">暂无工单</text>
      <text class="empty-hint">{{ activeStatus === 'all' ? '您当前没有待处理的工单' : '当前筛选下暂无工单' }}</text>
    </view>

    <scroll-view v-else class="order-list" scroll-y :show-scrollbar="false">
      <view
        v-for="(order, index) in displayOrders"
        :key="orderKey(order, index)"
        class="order-card"
      >
        <view class="order-row">
          <text class="label">订单号</text>
          <text class="value">{{ order.id }}</text>
        </view>
        <view class="order-row">
          <text class="label">服务类型</text>
          <text class="value">{{ order.serviceTypeName }}</text>
        </view>
        <view class="order-row">
          <text class="label">服务项目</text>
          <text class="value">{{ getProjectName(order) }}</text>
        </view>
        <view class="order-row" v-if="order.petId">
          <text class="label">宠物</text>
          <text class="value">
            {{ order.petName || '未知' }}
            <text class="pet-link" :data-index="index" @tap.stop="showPetDetailByIndex">查看宠物信息</text>
          </text>
        </view>
        <view class="order-row">
          <text class="label">联系人</text>
          <text class="value">{{ order.contactName }}</text>
        </view>
        <view class="order-row">
          <text class="label">联系电话</text>
          <text class="value" @tap="callPhone(order.contactPhone)">{{ order.contactPhone }}</text>
        </view>
        <view class="order-row">
          <text class="label">预约日期</text>
          <text class="value">{{ order.date }}</text>
        </view>
        <view class="order-row">
          <text class="label">时间段</text>
          <text class="value">{{ order.timeSlot }}</text>
        </view>
        <view class="order-row" v-if="order.address">
          <text class="label">服务地址</text>
          <text class="value address" :data-index="index" @tap="openMapByIndex">{{ order.address }}</text>
        </view>
        <view class="order-row" v-if="order.remark">
          <text class="label">客户备注</text>
          <text class="value remark">{{ order.remark }}</text>
        </view>
        <view class="order-row" v-if="order.rejectReason">
          <text class="label">处理说明</text>
          <text class="value reject-reason">{{ order.rejectReason }}</text>
        </view>
        <view class="order-row">
          <text class="label">状态</text>
          <text class="value status" :class="'status-' + order.status">{{ getStatusText(order.status) }}</text>
        </view>
        <view class="order-row" v-if="order.updateTime">
          <text class="label">最近更新</text>
          <text class="value">{{ formatDateTime(order.updateTime) }}</text>
        </view>
        <view class="order-actions">
          <!-- 待确认/已分配：确认 + 拒绝 -->
          <view
            v-if="order.status === 'pending' || order.status === 'assigned'"
            class="action-btn confirm"
            :data-id="order.id"
            :data-service-type="order.serviceType"
            @tap="confirmOrder"
          >
            确认
          </view>
          <view
            v-if="order.status === 'pending' || order.status === 'assigned'"
            class="action-btn reject"
            :data-id="order.id"
            :data-service-type="order.serviceType"
            @tap="rejectOrder"
          >
            拒绝
          </view>
          <!-- 已确认：完成 + 取消 -->
          <view
            v-if="order.status === 'confirmed'"
            class="action-btn complete"
            :data-id="order.id"
            :data-service-type="order.serviceType"
            @tap="completeOrder"
          >
            完成
          </view>
          <view
            v-if="order.status === 'confirmed'"
            class="action-btn reject"
            :data-id="order.id"
            :data-service-type="order.serviceType"
            @tap="cancelOrder"
          >
            取消
          </view>
          <!-- 待变更审核：【去审核】按钮 -->
          <view
            v-if="order.status === 'change_pending'"
            class="action-btn review"
            :data-id="order.id"
            :data-service-type="order.serviceType"
            @tap="openChangeReviewModal"
          >
            去审核
          </view>
          <!-- 待取消审核：【去审核】按钮 -->
          <view
            v-if="order.status === 'cancel_pending'"
            class="action-btn review"
            :data-id="order.id"
            :data-service-type="order.serviceType"
            @tap="openCancelReviewModal"
          >
            去审核
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="ops-card" v-if="recentActions.length > 0">
      <view class="ops-title">最近操作</view>
      <view v-for="(item, idx) in recentActions" :key="idx" class="ops-item">
        {{ item }}
      </view>
    </view>

    <!-- 宠物详情弹窗 -->
    <view v-if="showPetModal" class="reject-modal-mask" @tap="closePetModal">
      <view class="pet-modal" @tap.stop>
        <view class="pet-modal-title">宠物信息</view>
        <view class="pet-modal-body">
          <view class="pet-info-row" v-if="currentPet.petAvatar">
            <image class="pet-avatar-img" :src="currentPet.petAvatar" mode="aspectFill" />
          </view>
          <view class="pet-info-row">
            <text class="info-label">宠物名称</text>
            <text class="info-value">{{ currentPet.petName || '-' }}</text>
          </view>
          <view class="pet-info-row">
            <text class="info-label">品种</text>
            <text class="info-value">{{ currentPet.petBreed || '-' }}</text>
          </view>
          <view class="pet-info-row">
            <text class="info-label">性别</text>
            <text class="info-value">{{ petGenderText(currentPet.petGender) }}</text>
          </view>
          <view class="pet-info-row">
            <text class="info-label">年龄</text>
            <text class="info-value">{{ formatPetAge(currentPet.petAge) }}</text>
          </view>
          <view class="pet-info-row">
            <text class="info-label">体重</text>
            <text class="info-value">{{ currentPet.petWeight ? currentPet.petWeight + 'kg' : '-' }}</text>
          </view>
        </view>
        <view class="pet-modal-close" @tap="closePetModal">关闭</view>
      </view>
    </view>

    <!-- 原因弹窗（拒绝/取消共用） -->
    <view v-if="showReasonModal" class="reject-modal-mask" @tap="closeReasonModal">
      <view class="reject-modal" @tap.stop>
        <view class="reject-modal-title">填写{{ reasonActionText }}原因</view>
        <view class="reject-modal-tip">请填写{{ reasonActionText }}原因，用户将看到该说明</view>
        <textarea
          v-model="reasonInput"
          class="reject-reason-input"
          :placeholder="`请输入${reasonActionText}原因（必填）`"
          placeholder-class="placeholder"
          maxlength="500"
        />
        <view class="reject-modal-actions">
          <view class="reject-btn cancel" @tap="closeReasonModal">取消</view>
          <view class="reject-btn submit" @tap="submitReasonAction">提交</view>
        </view>
      </view>
    </view>

    <!-- 订单详情弹窗 -->
    <view v-if="showDetailModal" class="reject-modal-mask" @tap="closeDetailModal">
      <view class="detail-modal" @tap.stop>
        <view class="detail-modal-title">工单详情</view>
        <scroll-view scroll-y class="detail-scroll">
          <view class="detail-row"><text class="dl">订单号</text><text class="dv">{{ detailOrder.id || '-' }}</text></view>
          <view class="detail-row"><text class="dl">服务类型</text><text class="dv">{{ detailOrder.serviceTypeName || '-' }}</text></view>
          <view class="detail-row"><text class="dl">服务项目</text><text class="dv">{{ getProjectName(detailOrder) }}</text></view>
          <view class="detail-row" v-if="detailOrder.petId"><text class="dl">宠物</text><text class="dv">{{ detailOrder.petName || '未知' }}</text></view>
          <view class="detail-row"><text class="dl">联系人</text><text class="dv">{{ detailOrder.contactName || '-' }}</text></view>
          <view class="detail-row"><text class="dl">联系电话</text><text class="dv">{{ detailOrder.contactPhone || '-' }}</text></view>
          <view class="detail-row"><text class="dl">预约日期</text><text class="dv">{{ detailOrder.date || '-' }}</text></view>
          <view class="detail-row"><text class="dl">时间段</text><text class="dv">{{ detailOrder.timeSlot || '-' }}</text></view>
          <view class="detail-row" v-if="detailOrder.address"><text class="dl">服务地址</text><text class="dv addr">{{ detailOrder.address }}</text></view>
          <view class="detail-row" v-if="detailOrder.remark"><text class="dl">客户备注</text><text class="dv remark">{{ detailOrder.remark }}</text></view>
          <view class="detail-row" v-if="detailOrder.rejectReason"><text class="dl">处理说明</text><text class="dv reject-reason">{{ detailOrder.rejectReason }}</text></view>
          <view class="detail-row"><text class="dl">状态</text><text class="dv status" :class="'status-' + detailOrder.status">{{ getStatusText(detailOrder.status) }}</text></view>
          <view class="detail-row" v-if="detailOrder.updateTime"><text class="dl">最近更新</text><text class="dv">{{ formatDateTime(detailOrder.updateTime) }}</text></view>
        </scroll-view>
        <view class="detail-close-btn" @tap="closeDetailModal">关闭</view>
      </view>
    </view>

    <!-- 变更审核弹窗 -->
    <view v-if="showChangeReviewModal" class="reject-modal-mask" @tap="closeChangeReviewModal">
      <view class="review-modal" @tap.stop>
        <view class="review-modal-title">变更申请审核</view>
        <view v-if="changeReviewLoading" class="review-loading">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        <template v-else-if="changeReviewData">
          <scroll-view scroll-y class="review-scroll">
            <view class="review-section-title">变更内容</view>
            <view class="review-row" v-if="changeReviewData.requestedDate"><text class="rl">新日期</text><text class="rv">{{ changeReviewData.requestedDate }}</text></view>
            <view class="review-row" v-if="changeReviewData.requestedTimeSlot"><text class="rl">新时段</text><text class="rv">{{ changeReviewData.requestedTimeSlot }}</text></view>
            <view class="review-row" v-if="changeReviewData.requestedLocation"><text class="rl">新地址</text><text class="rv addr">{{ changeReviewData.requestedLocation }}</text></view>
            <view class="review-row" v-if="changeReviewData.requestedRemark"><text class="rl">变更说明</text><text class="rv remark">{{ changeReviewData.requestedRemark }}</text></view>
            <view class="review-row" v-if="changeReviewData.requestedContactPhone"><text class="rl">新电话</text><text class="rv">{{ changeReviewData.requestedContactPhone }}</text></view>
            <view class="review-row"><text class="rl">申请时间</text><text class="rv">{{ formatDateTime(changeReviewData.createTime) }}</text></view>
          </scroll-view>
          <view class="review-actions">
            <view class="review-btn approve" @tap="confirmApproveChange">同意变更</view>
            <view class="review-btn reject" @tap="openRejectChangeReason">拒绝变更</view>
          </view>
        </template>
        <view v-else class="review-empty">未找到待处理的变更申请或已处理</view>
      </view>
    </view>

    <!-- 取消审核弹窗 -->
    <view v-if="showCancelReviewModal" class="reject-modal-mask" @tap="closeCancelReviewModal">
      <view class="review-modal" @tap.stop>
        <view class="review-modal-title">取消申请审核</view>
        <view v-if="cancelReviewLoading" class="review-loading">
          <view class="loading-spinner"></view>
          <text>加载中...</text>
        </view>
        <template v-else-if="cancelReviewData">
          <scroll-view scroll-y class="review-scroll">
            <view class="review-section-title">取消信息</view>
            <view class="review-row" v-if="cancelReviewData.reason"><text class="rl">取消原因</text><text class="rv remark">{{ cancelReviewData.reason }}</text></view>
            <view class="review-row"><text class="rl">申请时间</text><text class="rv">{{ formatDateTime(cancelReviewData.createTime) }}</text></view>
          </scroll-view>
          <view class="review-actions">
            <view class="review-btn reject" @tap="confirmApproveCancel">同意取消</view>
            <view class="review-btn approve" @tap="openRejectCancelReason">拒绝取消</view>
          </view>
        </template>
        <view v-else class="review-empty">未找到待处理的取消申请或已处理</view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'WorkOrders',

  data() {
    return {
      orders: [],
      loading: false,
      activeStatus: 'all',
      searchKeyword: '',
      selectedDate: '',
      recentActions: [],
      statusTabs: [
        { key: 'all', label: '全部' },
        { key: 'pending', label: '待确认' },
        { key: 'confirmed', label: '已确认' },
        { key: 'change_pending', label: '待变更' },
        { key: 'cancel_pending', label: '待取消' },
        { key: 'completed', label: '已完成' },
        { key: 'cancelled', label: '已取消' }
      ],
      showReasonModal: false,
      reasonOrderId: null,
      reasonServiceType: null,
      reasonInput: '',
      reasonAction: 'cancelled',
      showPetModal: false,
      currentPet: {},
      showDetailModal: false,
      detailOrder: {},
      showChangeReviewModal: false,
      changeReviewData: null,
      changeReviewLoading: false,
      changeReviewOrderId: null,
      changeReviewServiceType: null,
      showCancelReviewModal: false,
      cancelReviewData: null,
      cancelReviewLoading: false,
      cancelReviewOrderId: null,
      cancelReviewServiceType: null
    }
  },
  computed: {
    displayOrders() {
      let list = this.orders
      if (this.activeStatus !== 'all') {
        list = list.filter((it) => this.normalizeStatus(it.status) === this.activeStatus)
      }
      if (this.selectedDate) {
        list = list.filter((it) => this.normalizeDateText(it.date) === this.selectedDate)
      }
      const kw = String(this.searchKeyword || '').trim().toLowerCase()
      if (kw) {
        list = list.filter((it) => {
          const project = this.getProjectName(it)
          const text = `${it.id || ''} ${it.contactName || ''} ${it.contactPhone || ''} ${project || ''}`.toLowerCase()
          return text.includes(kw)
        })
      }
      return list
    },
    reasonActionText() {
      const map = {
        cancelled: '取消',
        reject_change: '拒绝变更',
        reject_cancel: '拒绝取消'
      }
      return map[this.reasonAction] || '取消'
    }
  },

  onLoad() {
    const staffToken = uni.getStorageSync('staffToken')
    if (!staffToken) {
      uni.redirectTo({ url: '/pages-auth/login' })
      return
    }
    this.loadOrders()
  },

  onShow() {
    if (uni.getStorageSync('staffToken')) {
      this.loadOrders()
    }
  },

  onPullDownRefresh() {
    this.loadOrders().finally(() => {
      uni.stopPullDownRefresh()
    })
  },

  methods: {
    async loadOrders() {
      this.loading = true
      try {
        const res = await api.getStaffWorkOrders()
        if (res.code === 200 || res.code === 0) {
          const source = Array.isArray(res.data) ? res.data : []
          this.orders = source.map((it) => ({
            ...it,
            status: this.normalizeStatus(it.status)
          }))
        } else {
          this.orders = []
          uni.showToast({ title: res.msg || '加载失败', icon: 'none' })
        }
      } catch (err) {
        console.error('加载工单失败:', err)
        this.orders = []
        uni.showToast({ title: '加载工单失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },

    orderKey(order, index) {
      return String(order.id || '') + '-' + String(order.serviceType || '') + '-' + index
    },
    orderStableKey(order) {
      return `${order.serviceType || ''}-${order.id || ''}`
    },
    getStatusText(status) {
      const map = {
        pending: '待确认',
        assigned: '已分配',
        confirmed: '已确认',
        change_pending: '待变更审核',
        cancel_pending: '待取消审核',
        no_show: '已失约',
        completed: '已完成',
        cancelled: '已取消'
      }
      return map[status] || status || '待确认'
    },
    normalizeStatus(status) {
      const s = String(status || '').toLowerCase().trim()
      if (s === 'assigned') return 'pending'
      if (s === 'done') return 'completed'
      if (s === 'canceled') return 'cancelled'
      if (s === 'change_pending' || s === 'cancel_pending') return s
      return s || 'pending'
    },
    getProjectName(order) {
      return order.serviceProjectName || order.serviceName || order.itemName || order.remark || '未设置服务项目'
    },
    normalizeDateText(val) {
      const text = String(val || '').slice(0, 10).replace(/\//g, '-').replace(/\./g, '-')
      return /^\d{4}-\d{2}-\d{2}$/.test(text) ? text : ''
    },
    todayText() {
      const d = new Date()
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    formatDateTime(v) {
      return String(v || '').replace('T', ' ').slice(0, 16)
    },
    onDateChange(e) {
      this.selectedDate = e?.detail?.value || ''
    },
    clearDate() {
      this.selectedDate = ''
    },
    logAction(text) {
      const stamp = this.formatDateTime(new Date().toISOString())
      this.recentActions = [`${stamp} ${text}`, ...this.recentActions].slice(0, 8)
    },

    callPhone(phone) {
      if (!phone || phone === '-') return
      const num = String(phone).replace(/\D/g, '')
      if (num.length < 7) return
      uni.makePhoneCall({ phoneNumber: num })
    },
    openMap(order) {
      if (!order) return
      const addr = (order.address || '').trim()
      if (!addr) return
      const lat = Number(order.latitude || order.lat || 0)
      const lng = Number(order.longitude || order.lng || 0)
      if (lat && lng) {
        uni.openLocation({
          latitude: lat,
          longitude: lng,
          address: addr,
          name: order.contactName || '服务地址'
        })
      } else {
        uni.setClipboardData({
          data: addr,
          success: () => {
            uni.showToast({ title: '地址已复制', icon: 'none' })
          }
        })
      }
    },
    openMapByIndex(e) {
      const idx = e.currentTarget.dataset.index
      const order = this.displayOrders[idx]
      this.openMap(order)
    },

    confirmOrder(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      uni.showModal({
        title: '确认工单',
        content: '确认接受此工单吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await this.performUpdate(serviceType, id, 'confirmed')
            this.logAction(`工单#${id} 已确认`)
          } catch (err) {
            uni.showToast({ title: err.message || '操作失败', icon: 'none' })
          }
        }
      })
    },

    rejectOrder(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      this.openReasonModal(id, serviceType, 'cancelled')
    },
    cancelOrder(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      this.openReasonModal(id, serviceType, 'cancelled')
    },
    openReasonModal(id, serviceType, action) {
      this.reasonOrderId = id
      this.reasonServiceType = serviceType
      this.reasonAction = action
      this.reasonInput = ''
      this.showReasonModal = true
    },
    closeReasonModal() {
      this.showReasonModal = false
      this.reasonOrderId = null
      this.reasonServiceType = null
      this.reasonInput = ''
      this.reasonAction = 'cancelled'
    },
    async submitReasonAction() {
      const reason = (this.reasonInput || '').trim()
      if (!reason) {
        uni.showToast({ title: `请填写${this.reasonActionText}原因`, icon: 'none' })
        return
      }
      const id = this.reasonOrderId
      const serviceType = this.reasonServiceType
      if (!id || !serviceType) {
        this.closeReasonModal()
        return
      }
      try {
        if (this.reasonAction === 'reject_change') {
          await this.handleChangeRequest(id, serviceType, false, reason)
        } else if (this.reasonAction === 'reject_cancel') {
          await this.handleCancellationRequest(id, serviceType, false, reason)
        } else {
          await this.performUpdate(serviceType, id, this.reasonAction, reason)
        }
        this.closeReasonModal()
        this.logAction(`工单#${id} ${this.reasonAction === 'cancelled' ? '已取消' : this.reasonAction === 'reject_change' ? '已拒绝变更' : '已拒绝取消'}${reason ? '(' + reason + ')' : ''}`)
      } catch (err) {
        uni.showToast({ title: err.message || '操作失败', icon: 'none' })
      }
    },

    completeOrder(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      uni.showModal({
        title: '完成工单',
        content: '确认已完成此工单吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await this.performUpdate(serviceType, id, 'completed')
            this.logAction(`工单#${id} 已完成`)
          } catch (err) {
            uni.showToast({ title: err.message || '操作失败', icon: 'none' })
          }
        }
      })
    },
    async performUpdate(serviceType, id, status, reason) {
      uni.showLoading({ title: '提交中...' })
      try {
        const result = await api.updateAppointmentStatusForStaff(serviceType, id, status, reason)
        if (!(result.code === 200 || result.code === 0)) {
          throw new Error(result.msg || '操作失败')
        }
        uni.showToast({
          title: status === 'confirmed' ? '已确认' : status === 'completed' ? '已完成' : '已取消',
          icon: 'success'
        })
        await this.loadOrders()
      } finally {
        uni.hideLoading()
      }
    },

    // ========== 变更/取消请求处理 ==========
    async approveChangeRequest(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      uni.showModal({
        title: '同意变更',
        content: '确认同意此预约的变更申请吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await this.handleChangeRequest(id, serviceType, true)
          } catch (err) {
            uni.showToast({ title: err.message || '操作失败', icon: 'none' })
          }
        }
      })
    },

    async rejectChangeRequest(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      this.openReasonModal(id, serviceType, 'reject_change')
    },

    async approveCancellationRequest(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      uni.showModal({
        title: '同意取消',
        content: '确认同意取消此预约吗？（将按规则处理违约金）',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await this.handleCancellationRequest(id, serviceType, true)
          } catch (err) {
            uni.showToast({ title: err.message || '操作失败', icon: 'none' })
          }
        }
      })
    },

    async rejectCancellationRequest(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      this.openReasonModal(id, serviceType, 'reject_cancel')
    },

    async handleChangeRequest(orderId, serviceType, isApprove, reason) {
      uni.showLoading({ title: isApprove ? '同意中...' : '拒绝中...' })
      try {
        const pendingRes = await api.getPendingChangeByAppointment(serviceType, orderId)
        if (!(pendingRes.code === 200 || pendingRes.code === 0) || !pendingRes.data) {
          throw new Error('未找到待处理的变更申请')
        }
        const changeRequestId = pendingRes.data.id
        let result
        if (isApprove) {
          result = await api.approveChangeRequest(changeRequestId)
        } else {
          result = await api.rejectChangeRequest(changeRequestId)
        }
        if (!(result.code === 200 || result.code === 0)) {
          throw new Error(result.msg || '操作失败')
        }
        uni.showToast({ title: isApprove ? '已同意变更' : '已拒绝变更', icon: 'success' })
        this.logAction(`工单#${orderId} ${isApprove ? '已同意' : '已拒绝'}变更${reason ? '(' + reason + ')' : ''}`)
        await this.loadOrders()
      } finally {
        uni.hideLoading()
      }
    },

    async handleCancellationRequest(orderId, serviceType, isApprove, reason) {
      uni.showLoading({ title: isApprove ? '同意中...' : '拒绝中...' })
      try {
        const pendingRes = await api.getPendingCancellationByAppointment(serviceType, orderId)
        if (!(pendingRes.code === 200 || pendingRes.code === 0) || !pendingRes.data) {
          throw new Error('未找到待处理的取消申请')
        }
        const cancellationRequestId = pendingRes.data.id
        let result
        if (isApprove) {
          result = await api.approveCancellationRequest(cancellationRequestId)
        } else {
          result = await api.rejectCancellationRequest(cancellationRequestId, reason || '')
        }
        if (!(result.code === 200 || result.code === 0)) {
          throw new Error(result.msg || '操作失败')
        }
        uni.showToast({ title: isApprove ? '已同意取消' : '已拒绝取消', icon: 'success' })
        this.logAction(`工单#${orderId} ${isApprove ? '已同意' : '已拒绝'}取消${reason ? '(' + reason + ')' : ''}`)
        await this.loadOrders()
      } finally {
        uni.hideLoading()
      }
    },
    showPetDetail(order) {
      if (!order) return
      this.currentPet = {
        petName: order.petName,
        petBreed: order.petBreed,
        petGender: order.petGender,
        petAge: order.petAge,
        petWeight: order.petWeight,
        petSpecies: order.petSpecies,
        petAvatar: order.petAvatar
      }
      this.showPetModal = true
    },
    showPetDetailByIndex(e) {
      const idx = e.currentTarget.dataset.index
      const order = this.displayOrders[idx]
      this.showPetDetail(order)
    },
    closePetModal() {
      this.showPetModal = false
      this.currentPet = {}
    },
    petGenderText(gender) {
      if (!gender) return '-'
      const map = { male: '公', female: '母', other: '其他' }
      return map[gender] || gender
    },
    formatPetAge(age) {
      if (age == null) return '-'
      return age + '岁'
    },

    // ========== 详情弹窗 ==========
    viewOrderDetail(e) {
      const idx = e.currentTarget.dataset.index
      const order = this.displayOrders[idx]
      if (!order) return
      this.detailOrder = order
      this.showDetailModal = true
    },
    closeDetailModal() {
      this.showDetailModal = false
      this.detailOrder = {}
    },

    // ========== 变更审核弹窗 ==========
    async openChangeReviewModal(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      this.changeReviewOrderId = id
      this.changeReviewServiceType = serviceType
      this.changeReviewData = null
      this.changeReviewLoading = true
      this.showChangeReviewModal = true
      try {
        const res = await api.getPendingChangeByAppointment(serviceType, id)
        if ((res.code === 200 || res.code === 0) && res.data && res.data.id) {
          this.changeReviewData = res.data
        } else {
          this.changeReviewData = null
          uni.showToast({ title: '未找到待处理的变更申请或已处理', icon: 'none' })
        }
      } catch (err) {
        console.error('加载变更申请失败:', err)
        this.changeReviewData = null
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.changeReviewLoading = false
      }
    },
    closeChangeReviewModal() {
      this.showChangeReviewModal = false
      this.changeReviewData = null
      this.changeReviewOrderId = null
      this.changeReviewServiceType = null
    },
    async confirmApproveChange() {
      if (!this.changeReviewData?.id) return
      try {
        const result = await api.approveChangeRequest(this.changeReviewData.id)
        if (!(result.code === 200 || result.code === 0)) throw new Error(result.msg || '操作失败')
        uni.showToast({ title: '已同意变更', icon: 'success' })
        this.logAction(`工单#${this.changeReviewOrderId} 已同意变更`)
        this.closeChangeReviewModal()
        await this.loadOrders()
      } catch (err) {
        uni.showToast({ title: err.message || '操作失败', icon: 'none' })
      }
    },
    openRejectChangeReason() {
      this.closeChangeReviewModal()
      this.openReasonModal(this.changeReviewOrderId, this.changeReviewServiceType, 'reject_change')
    },

    // ========== 取消审核弹窗 ==========
    async openCancelReviewModal(e) {
      const id = e.currentTarget.dataset.id
      const serviceType = e.currentTarget.dataset.serviceType
      if (!id || !serviceType) return
      this.cancelReviewOrderId = id
      this.cancelReviewServiceType = serviceType
      this.cancelReviewData = null
      this.cancelReviewLoading = true
      this.showCancelReviewModal = true
      try {
        const res = await api.getPendingCancellationByAppointment(serviceType, id)
        if ((res.code === 200 || res.code === 0) && res.data && res.data.id) {
          this.cancelReviewData = res.data
        } else {
          this.cancelReviewData = null
          uni.showToast({ title: '未找到待处理的取消申请或已处理', icon: 'none' })
        }
      } catch (err) {
        console.error('加载取消申请失败:', err)
        this.cancelReviewData = null
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.cancelReviewLoading = false
      }
    },
    closeCancelReviewModal() {
      this.showCancelReviewModal = false
      this.cancelReviewData = null
      this.cancelReviewOrderId = null
      this.cancelReviewServiceType = null
    },
    async confirmApproveCancel() {
      if (!this.cancelReviewData?.id) return
      try {
        const result = await api.approveCancellationRequest(this.cancelReviewData.id)
        if (!(result.code === 200 || result.code === 0)) throw new Error(result.msg || '操作失败')
        uni.showToast({ title: '已同意取消', icon: 'success' })
        this.logAction(`工单#${this.cancelReviewOrderId} 已同意取消`)
        this.closeCancelReviewModal()
        await this.loadOrders()
      } catch (err) {
        uni.showToast({ title: err.message || '操作失败', icon: 'none' })
      }
    },
    openRejectCancelReason() {
      this.closeCancelReviewModal()
      this.openReasonModal(this.cancelReviewOrderId, this.cancelReviewServiceType, 'reject_cancel')
    }
  }
}
</script>

<style lang="scss" scoped>
.work-orders-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 24rpx;
  width: 100%;
  box-sizing: border-box;
}
.filter-row {
  display: flex;
  overflow-x: auto;
  padding: 12rpx 24rpx 6rpx;
  gap: 12rpx;
  box-sizing: border-box;
}
.tool-row {
  display: flex;
  padding: 8rpx 24rpx;
  gap: 12rpx;
  align-items: center;
}
.search-input {
  flex: 1;
  height: 64rpx;
  background: #fff;
  border-radius: 12rpx;
  padding: 0 18rpx;
  font-size: 24rpx;
}
.today-switch {
  padding: 10rpx 16rpx;
  border-radius: 10rpx;
  background: #eceff7;
  color: #6e7795;
  font-size: 22rpx;
}
.today-switch.active {
  color: #3453c7;
  background: #dfe7ff;
}
.batch-btn {
  padding: 8rpx 14rpx;
  border-radius: 10rpx;
  background: #fff3ed;
  color: #ff6b35;
  font-size: 22rpx;
}
.batch-btn.complete {
  background: #e8f5e9;
  color: #2e8b57;
}
.batch-btn.plain {
  background: #eceff7;
  color: #6e7795;
}
.filter-pill {
  flex-shrink: 0;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  background: #eceff7;
  color: #6e7795;
  font-size: 22rpx;
}
.filter-pill.active {
  background: #dfe7ff;
  color: #3453c7;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48rpx 0;

  .loading-spinner {
    width: 40rpx;
    height: 40rpx;
    border: 4rpx solid #eee;
    border-top-color: #ff6b35;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  text {
    margin-top: 12rpx;
    font-size: 24rpx;
    color: #999;
  }

  .empty-icon {
    font-size: 60rpx;
    margin-bottom: 12rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #333;
  }

  .empty-hint {
    font-size: 22rpx;
    color: #999;
    margin-top: 4rpx;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.order-list {
  height: calc(100vh - 220rpx);
  padding: 12rpx 24rpx;
  width: 100%;
  box-sizing: border-box;
}

.order-card {
  background: #fff;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  width: 100%;
  box-sizing: border-box;
}

.order-row {
  display: flex;
  align-items: center;
  margin-bottom: 10rpx;
  font-size: 24rpx;

  .label {
    width: 130rpx;
    color: #999;
    flex-shrink: 0;
  }

  .value {
    flex: 1;
    color: #333;
    word-break: break-all;

    &.address {
      color: #1565c0;
    }

    &.remark {
      color: #555;
      font-style: normal;
    }

    &.status-pending,
    &.status-assigned {
      color: #ff9800;
    }

    &.status-confirmed {
      color: #2196f3;
    }

    &.status-change_pending {
      color: #9c27b0;
    }

    &.status-cancel_pending {
      color: #ff5722;
    }

    &.status-no_show {
      color: #d64545;
    }

    &.status-completed {
      color: #4caf50;
    }

    &.status-cancelled {
      color: #999;
    }
    &.reject-reason {
      color: #d64545;
    }
  }
}

.order-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 14rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #eee;
}

.action-btn {
  flex: 1;
  padding: 14rpx;
  text-align: center;
  border-radius: 8rpx;
  font-size: 24rpx;

  &.confirm {
    background: #fff3ed;
    color: #ff6b35;
  }

  &.reject {
    background: #ffebee;
    color: #e53935;
  }

  &.complete {
    background: #e8f5e9;
    color: #4caf50;
  }
}

.reject-modal-mask {
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
  box-sizing: border-box;
}
.reject-modal {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-sizing: border-box;
}
.reject-modal-title {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 12rpx;
}
.reject-modal-tip {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 24rpx;
}
.reject-reason-input {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.5;
  box-sizing: border-box;
  margin-bottom: 24rpx;
}
.reject-modal-actions {
  display: flex;
  gap: 24rpx;
}
.reject-btn {
  flex: 1;
  padding: 24rpx;
  text-align: center;
  border-radius: 12rpx;
  font-size: 28rpx;
  &.cancel {
    background: #f0f0f0;
    color: #666;
  }
  &.submit {
    background: #e53935;
    color: #fff;
  }
}
.placeholder {
  color: #bbb;
}
.ops-card {
  margin: 8rpx 24rpx 24rpx;
  background: #fff;
  border-radius: 12rpx;
  padding: 16rpx 18rpx;
}
.ops-title {
  font-size: 24rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 8rpx;
}
.ops-item {
  font-size: 22rpx;
  color: #6f7895;
  line-height: 1.6;
  margin-bottom: 4rpx;
}
.pet-link {
  color: #1565c0;
  margin-left: 12rpx;
}
.pet-modal {
  width: 100%;
  max-width: 600rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx;
  box-sizing: border-box;
}
.pet-modal-title {
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 20rpx;
  text-align: center;
}
.pet-modal-body {
  margin-bottom: 24rpx;
}
.pet-info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.pet-info-row:last-child {
  border-bottom: none;
}
.info-label {
  width: 140rpx;
  color: #999;
  font-size: 26rpx;
  flex-shrink: 0;
}
.info-value {
  flex: 1;
  text-align: right;
  color: #333;
  font-size: 26rpx;
  word-break: break-all;
}
.pet-avatar-img {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  margin: 0 auto;
  display: block;
}
.pet-modal-close {
  padding: 24rpx;
  text-align: center;
  background: #3453c7;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
}

// ========== 详情弹窗 ==========
.detail-modal {
  width: 620rpx;
  max-height: 80vh;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}
.detail-modal-title {
  padding: 28rpx 24rpx;
  font-size: 30rpx;
  font-weight: bold;
  text-align: center;
  border-bottom: 1rpx solid #eee;
}
.detail-scroll {
  max-height: 60vh;
  padding: 16rpx 24rpx;
}
.detail-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 14rpx;
  font-size: 26rpx;
  .dl { width: 140rpx; color: #999; flex-shrink: 0; }
  .dv { flex: 1; color: #333; word-break: break-all;
    &.addr { color: #1565c0; }
    &.remark { color: #555; font-style: normal; }
    &.reject-reason { color: #d64545; }
  }
}
.detail-close-btn {
  margin: 16rpx 24rpx 24rpx;
  padding: 22rpx;
  text-align: center;
  background: #3453c7;
  color: #fff;
  border-radius: 12rpx;
  font-size: 28rpx;
}

// ========== 审核弹窗 ==========
.review-modal {
  width: 620rpx;
  max-height: 80vh;
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
}
.review-modal-title {
  padding: 28rpx 24rpx;
  font-size: 30rpx;
  font-weight: bold;
  text-align: center;
  border-bottom: 1rpx solid #eee;
}
.review-loading, .review-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60rpx 24rpx;
  color: #999;
  font-size: 26rpx;
}
.review-scroll {
  max-height: 50vh;
  padding: 12rpx 24rpx;
}
.review-section-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 12rpx;
  padding-bottom: 8rpx;
  border-bottom: 2rpx solid #3453c7;
}
.review-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10rpx;
  font-size: 25rpx;
  .rl { width: 130rpx; color: #999; flex-shrink: 0; }
  .rv { flex: 1; color: #333; word-break: break-all;
    &.addr { color: #1565c0; }
    &.remark { color: #555; }
  }
}
.review-actions {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 24rpx 24rpx;
}
.review-btn {
  flex: 1;
  padding: 20rpx 0;
  text-align: center;
  border-radius: 10rpx;
  font-size: 27rpx;
  font-weight: 500;
  &.approve { background: #e8f5e9; color: #2e8b57; }
  &.reject { background: #ffebee; color: #d64545; }
}

// ========== 按钮颜色补充 ==========
.action-btn {
  &.detail { background: #e3f2fd; color: #1565c0; }
  &.review { background: #f3e5f5; color: #7b1fa2; }
}
</style>
