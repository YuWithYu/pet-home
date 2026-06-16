<template>
  <view class="schedule-container">
    <scroll-view class="week-strip" scroll-x :show-scrollbar="false">
      <view class="week-list">
        <view
          v-for="item in weekDays"
          :key="item.date"
          class="day-item"
          :class="{ active: item.date === selectedDate }"
          @tap="selectedDate = item.date"
        >
          <text class="day-week">{{ item.week }}</text>
          <text class="day-date">{{ item.day }}</text>
          <view v-if="item.count > 0" class="day-dot">{{ item.count }}</view>
        </view>
      </view>
    </scroll-view>
    <view class="week-tools">
      <view class="week-btn" @tap="shiftWeek(-1)">上一周</view>
      <picker mode="date" :value="selectedDate" @change="onDateChange">
        <view class="week-current">{{ selectedDate || '选择日期' }}</view>
      </picker>
      <view class="week-btn" @tap="shiftWeek(1)">下一周</view>
    </view>

    <view v-if="loading" class="state-card">加载中...</view>
    <view v-else-if="filteredSchedules.length === 0" class="empty-wrap">
      <text class="empty-emoji">🐾</text>
      <text class="empty-title">暂无排班，休息一下吧</text>
      <text class="empty-sub">{{ emptyTip }}</text>
    </view>
    <scroll-view v-else class="timeline-wrap" scroll-y :show-scrollbar="false">
      <view v-for="(item, idx) in filteredSchedules" :key="item.id + '-' + idx" class="timeline-item">
        <view class="line-dot-wrap">
          <view class="dot"></view>
          <view class="line" v-if="idx !== filteredSchedules.length - 1"></view>
        </view>
        <view class="schedule-card">
          <view class="card-top">
            <text class="time">{{ item.timeRange }}</text>
            <text class="status-tag" :class="item.statusClass">{{ item.statusText }}</text>
          </view>
          <view class="service-row">
            <text class="service-name">{{ item.serviceProjectName || '未标注服务项目' }}</text>
            <view v-if="item.isDoorService" class="map-btn" @tap="handleOpenMap(item)">一键导航</view>
          </view>
          <view class="meta">客户：{{ item.contactName || '未填写' }} {{ item.contactPhone || '' }}</view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'MySchedule',
  data() {
    return {
      loading: false,
      schedules: [],
      selectedDate: ''
    }
  },
  computed: {
    weekDays() {
      const base = this.selectedDate || this.today()
      const date = this.parseDate(base)
      const day = date.getDay() || 7
      const monday = new Date(date)
      monday.setDate(date.getDate() - day + 1)
      const weeks = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
      return Array.from({ length: 7 }).map((_, idx) => {
        const d = new Date(monday)
        d.setDate(monday.getDate() + idx)
        return {
          date: this.formatDate(d),
          week: weeks[idx],
          day: String(d.getDate()).padStart(2, '0'),
          count: this.schedules.filter((it) => it.dateKey === this.formatDate(d)).length
        }
      })
    },
    selectedSchedules() {
      return this.schedules
        .filter((it) => it.dateKey === this.selectedDate)
        .sort((a, b) => a.sortTime - b.sortTime)
    },
    filteredSchedules() {
      return this.selectedSchedules
    },
    todayCount() {
      const today = this.today()
      return this.schedules.filter((it) => it.dateKey === today).length
    },
    processingCount() {
      const today = this.today()
      return this.schedules.filter((it) => it.dateKey === today && it.status === 'assigned').length
    },
    emptyTip() {
      return '今天可以好好陪陪毛孩子'
    }
  },
  onLoad() {
    if (!uni.getStorageSync('staffToken')) {
      uni.navigateTo({ url: '/pages-auth/login' })
      return
    }
    this.selectedDate = this.today()
    this.loadSchedules()
  },
  onPullDownRefresh() {
    this.loadSchedules().finally(() => {
      uni.stopPullDownRefresh()
    })
  },
  methods: {
    async loadSchedules() {
      this.loading = true
      try {
        const res = await api.getStaffWorkOrders()
        if (res.code === 200 || res.code === 0) {
          const source = Array.isArray(res.data) ? res.data : []
          this.schedules = source
            .map((it) => {
              const normalizedStatus = this.normalizeStatus(it.status)
              if (!normalizedStatus) return null
              const dateKey = this.normalizeDateText(it.date)
              const statusText = this.getStatusText(normalizedStatus)
              return {
                ...it,
                status: normalizedStatus,
                dateKey,
                timeRange: it.timeSlot || '待定时段',
                sortTime: this.parseTimeToMinutes(it.timeSlot),
                statusText,
                statusClass: this.getStatusClass(normalizedStatus),
                serviceProjectName: this.resolveServiceProjectName(it),
                isDoorService: this.isDoorService(it.serviceTypeName)
              }
            })
            .filter(Boolean)
          this.alignSelectedDate()
        } else {
          this.schedules = []
          uni.showToast({ title: res.msg || '加载失败', icon: 'none' })
        }
      } catch (e) {
        this.schedules = []
        uni.showToast({ title: '加载排班失败', icon: 'none' })
      } finally {
        this.loading = false
      }
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
      return map[status] || status || '-'
    },
    getStatusClass(status) {
      const map = {
        pending: 'tag-pending',
        assigned: 'tag-processing',
        confirmed: 'tag-processing',
        change_pending: 'tag-change-pending',
        cancel_pending: 'tag-cancel-pending',
        no_show: 'tag-no-show',
        completed: 'tag-done',
        cancelled: 'tag-cancelled'
      }
      return map[status] || 'tag-pending'
    },
    normalizeStatus(status) {
      const s = String(status || '').trim().toLowerCase()
      const alias = {
        pending: 'pending',
        assigned: 'assigned',
        confirming: 'assigned',
        confirmed: 'confirmed',
        processing: 'assigned',
        change_pending: 'change_pending',
        cancel_pending: 'cancel_pending',
        no_show: 'no_show',
        completed: 'completed',
        done: 'completed',
        cancelled: 'cancelled',
        canceled: 'cancelled'
      }
      if (alias[s]) return alias[s]
      if (['rejected'].includes(s)) return ''
      return s || ''
    },
    alignSelectedDate() {
      const week = this.weekDays.map((it) => it.date)
      if (!week.includes(this.selectedDate)) {
        this.selectedDate = week[0] || this.today()
      }
      const hasCurrent = this.schedules.some((it) => it.dateKey === this.selectedDate)
      if (hasCurrent) return
      const firstInWeek = this.schedules.find((it) => week.includes(it.dateKey))
      if (firstInWeek && firstInWeek.dateKey) {
        this.selectedDate = firstInWeek.dateKey
      }
    },
    isDoorService(typeName) {
      const text = String(typeName || '')
      return text.includes('铲屎') || text.includes('上门')
    },
    resolveServiceProjectName(item) {
      const candidates = [
        item.serviceProjectName,
        item.projectName,
        item.serviceName,
        item.itemName,
        item.remark
      ]
      const hit = candidates.find((v) => v != null && String(v).trim() !== '')
      if (hit) return String(hit).trim()
      return '未设置服务项目'
    },
    handleOpenMap(item) {
      const lat = Number(item.latitude || item.lat || 0)
      const lng = Number(item.longitude || item.lng || 0)
      if (lat && lng) {
        uni.openLocation({
          latitude: lat,
          longitude: lng,
          name: item.contactName || '服务地址',
          address: item.address || ''
        })
        return
      }
      uni.showToast({
        title: item.address ? `导航到：${item.address}` : '导航功能开发中',
        icon: 'none'
      })
    },
    today() {
      return this.formatDate(new Date())
    },
    normalizeDateText(value) {
      if (!value) return ''
      const str = String(value).slice(0, 10).replace(/\./g, '-').replace(/\//g, '-')
      return /^\d{4}-\d{2}-\d{2}$/.test(str) ? str : ''
    },
    parseDate(str) {
      const [y, m, d] = String(str).split('-').map((v) => Number(v))
      return new Date(y, (m || 1) - 1, d || 1)
    },
    shiftWeek(offset) {
      const base = this.parseDate(this.selectedDate || this.today())
      base.setDate(base.getDate() + offset * 7)
      this.selectedDate = this.formatDate(base)
    },
    onDateChange(e) {
      const val = e && e.detail ? e.detail.value : ''
      if (val) this.selectedDate = val
    },
    formatDate(d) {
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    parseTimeToMinutes(slot) {
      const text = String(slot || '')
      const match = text.match(/(\d{1,2}):(\d{2})/)
      if (!match) return 9999
      return Number(match[1]) * 60 + Number(match[2])
    }
  }
}
</script>

<style lang="scss" scoped>
.schedule-container {
  min-height: 100vh;
  background: #f6f7fb;
  padding: 24rpx;
  box-sizing: border-box;
}
.header-card,
.state-card,
.schedule-card,
.empty-wrap {
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(38, 54, 126, 0.06);
}
.week-strip {
  white-space: nowrap;
  margin-bottom: 10rpx;
}
.week-list {
  display: inline-flex;
}
.week-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}
.week-btn {
  min-width: 120rpx;
  text-align: center;
  background: #eceff7;
  color: #6e7795;
  border-radius: 999rpx;
  font-size: 22rpx;
  padding: 8rpx 14rpx;
}
.week-current {
  min-width: 220rpx;
  text-align: center;
  background: #dfe7ff;
  color: #3453c7;
  border-radius: 999rpx;
  font-size: 22rpx;
  padding: 8rpx 16rpx;
}
.day-item {
  position: relative;
  width: 116rpx;
  height: 110rpx;
  margin-right: 12rpx;
  border-radius: 16rpx;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(38, 54, 126, 0.05);
}
.day-item.active {
  background: linear-gradient(135deg, #6a8dff, #8f6bff);
}
.day-week {
  font-size: 22rpx;
  color: #8b90a7;
}
.day-date {
  margin-top: 8rpx;
  font-size: 32rpx;
  color: #273148;
  font-weight: 700;
}
.day-item.active .day-week,
.day-item.active .day-date {
  color: #fff;
}
.day-dot {
  position: absolute;
  top: 6rpx;
  right: 8rpx;
  min-width: 30rpx;
  height: 30rpx;
  line-height: 30rpx;
  text-align: center;
  border-radius: 999rpx;
  background: #ff6b6b;
  color: #fff;
  font-size: 20rpx;
  padding: 0 6rpx;
}
.state-card {
  padding: 60rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #8b90a7;
}
.empty-wrap {
  margin-top: 16rpx;
  padding: 70rpx 24rpx;
  text-align: center;
}
.empty-emoji {
  font-size: 64rpx;
}
.empty-title {
  display: block;
  margin-top: 12rpx;
  font-size: 30rpx;
  color: #2b334a;
  font-weight: 600;
}
.empty-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #97a0ba;
}
.timeline-wrap {
  height: calc(100vh - 280rpx);
  padding-top: 6rpx;
}
.timeline-item {
  display: flex;
  margin-bottom: 18rpx;
}
.line-dot-wrap {
  width: 36rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #7c88ff;
  margin-top: 24rpx;
}
.line {
  width: 2rpx;
  flex: 1;
  background: #dde2f8;
  margin-top: 8rpx;
}
.schedule-card {
  flex: 1;
  padding: 22rpx;
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time {
  color: #222;
  font-size: 30rpx;
  font-weight: 600;
}
.status-tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
}
.tag-pending {
  color: #b37b00;
  background: #fff4dc;
}
.tag-processing {
  color: #2267d6;
  background: #e8f1ff;
}
.tag-change-pending {
  color: #9c27b0;
  background: #f3e5f5;
}
.tag-cancel-pending {
  color: #ff5722;
  background: #fff3e0;
}
.tag-no-show {
  color: #d64545;
  background: #ffebee;
}
.tag-done {
  color: #008d60;
  background: #e3f8f0;
}
.tag-cancelled {
  color: #999;
  background: #f5f5f5;
}
.service-row {
  margin-top: 14rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.service-name {
  color: #2e3a59;
  font-size: 28rpx;
  font-weight: 500;
}
.map-btn {
  color: #5c72ff;
  border: 1rpx solid #5c72ff;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
}
.meta {
  margin-top: 14rpx;
  color: #7f89a8;
  font-size: 24rpx;
}
</style>
