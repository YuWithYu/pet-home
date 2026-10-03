<template>
  <view class="health-container">
    <!-- 宠物信息头 -->
    <view class="pet-header">
      <image class="pet-avatar" :src="petAvatar" mode="aspectFill" />
      <view class="pet-info">
        <text class="pet-name">{{ petName }}</text>
        <view class="weight-summary">
          <text class="current-weight">{{ latestWeightText }}</text>
          <text class="weight-change" :class="changeClass">{{ changeText }}</text>
        </view>
      </view>
    </view>

    <!-- 疫苗/驱虫到期提醒 -->
    <view class="section" v-if="reminders.length">
      <view class="section-title">
        <text class="title-text">健康提醒</text>
        <text class="title-sub">{{ reminders.length }} 项待办</text>
      </view>
      <view class="reminder-item" v-for="item in remindersView" :key="item.id">
        <view class="reminder-badge" :class="item.cls">
          <text>{{ item.typeText }}</text>
        </view>
        <view class="reminder-body">
          <text class="reminder-name">{{ item.itemName }}</text>
          <text class="reminder-date">{{ item.nextDueDate }} 到期</text>
        </view>
        <text class="reminder-days" :class="item.cls">{{ item.daysText }}</text>
      </view>
    </view>

    <!-- 体重曲线 -->
    <view class="section">
      <view class="section-title">
        <text class="title-text">体重曲线</text>
        <view class="title-action" @click="openWeightModal">
          <text>+ 记体重</text>
        </view>
      </view>
      <view v-if="weightRecords.length" class="chart-wrap">
        <canvas
          canvas-id="weightChart"
          id="weightChart"
          class="chart-canvas"
          :style="{ width: chartWidth + 'px', height: chartHeight + 'px' }"
        />
      </view>
      <view v-else class="empty-tip">
        <text>还没有体重记录，称一称并记录吧</text>
      </view>
      <!-- 最近体重记录 -->
      <view class="recent-weights" v-if="weightRecords.length">
        <view class="recent-item" v-for="r in recentWeights" :key="r.id">
          <text class="recent-date">{{ r.recordDate }}</text>
          <text class="recent-value">{{ r.weight }}kg</text>
          <text class="recent-remark" v-if="r.remark">{{ r.remark }}</text>
          <view class="recent-delete" @click="confirmDeleteWeight(r)">
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 就医记录时间线 -->
    <view class="section">
      <view class="section-title">
        <text class="title-text">就医记录</text>
        <view class="title-action" @click="openMedicalModal">
          <text>+ 添加记录</text>
        </view>
      </view>
      <view v-if="medicalRecords.length" class="timeline">
        <view class="timeline-item" v-for="record in medicalRecords" :key="record.id">
          <view class="timeline-dot" :class="'dot-' + record.recordType"></view>
          <view class="timeline-content">
            <view class="record-head">
              <text class="record-type" :class="'tag-' + record.recordType">{{ typeText(record.recordType) }}</text>
              <text class="record-name">{{ record.itemName }}</text>
            </view>
            <text class="record-date">日期：{{ record.recordDate }}</text>
            <text class="record-next" v-if="record.nextDueDate">下次：{{ record.nextDueDate }}</text>
            <text class="record-remark" v-if="record.remark">{{ record.remark }}</text>
            <text class="record-delete" @click="confirmDeleteMedical(record)">删除</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-tip">
        <text>暂无就医记录，点击"添加记录"开始建立健康档案</text>
      </view>
    </view>

    <!-- 记录体重弹窗 -->
    <view class="modal-mask" v-if="showWeightModal" @click="closeWeightModal">
      <view class="modal-sheet" @click.stop>
        <view class="modal-title">
          <text>记录体重</text>
        </view>
        <view class="form-row">
          <text class="form-label">体重(kg)</text>
          <input
            class="form-input"
            type="digit"
            v-model="weightForm.weight"
            placeholder="如 4.5"
            placeholder-class="input-placeholder"
          />
        </view>
        <view class="form-row">
          <text class="form-label">称重日期</text>
          <picker mode="date" :value="weightForm.recordDate" :end="today" @change="onWeightDateChange">
            <view class="form-picker">
              <text>{{ weightForm.recordDate }}</text>
              <text class="picker-arrow">></text>
            </view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">备注</text>
          <input
            class="form-input"
            v-model="weightForm.remark"
            placeholder="选填，如体检后称重"
            placeholder-class="input-placeholder"
            maxlength="50"
          />
        </view>
        <view class="modal-footer">
          <view class="btn-cancel" @click="closeWeightModal">
            <text>取消</text>
          </view>
          <view class="btn-confirm" :class="{ 'btn-disabled': submitting }" @click="submitWeight">
            <text>{{ submitting ? '保存中...' : '保存' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 添加就医记录弹窗 -->
    <view class="modal-mask" v-if="showMedicalModal" @click="closeMedicalModal">
      <view class="modal-sheet" @click.stop>
        <view class="modal-title">
          <text>添加就医记录</text>
        </view>
        <view class="form-row">
          <text class="form-label">类型</text>
          <picker
            mode="selector"
            :range="recordTypeOptions"
            range-key="label"
            :value="medicalTypeIndex"
            @change="onTypeChange"
          >
            <view class="form-picker">
              <text>{{ recordTypeOptions[medicalTypeIndex].label }}</text>
              <text class="picker-arrow">></text>
            </view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">项目名称</text>
          <input
            class="form-input"
            v-model="medicalForm.itemName"
            placeholder="如 狂犬疫苗、体内驱虫"
            placeholder-class="input-placeholder"
            maxlength="30"
          />
        </view>
        <view class="form-row">
          <text class="form-label">日期</text>
          <picker mode="date" :value="medicalForm.recordDate" :end="today" @change="onRecordDateChange">
            <view class="form-picker">
              <text>{{ medicalForm.recordDate }}</text>
              <text class="picker-arrow">></text>
            </view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">下次日期</text>
          <picker mode="date" :value="medicalForm.nextDueDate || today" :start="today" @change="onNextDueDateChange">
            <view class="form-picker">
              <text :class="{ 'picker-empty': !medicalForm.nextDueDate }">
                {{ medicalForm.nextDueDate || '选填，用于到期提醒' }}
              </text>
              <text class="picker-arrow" v-if="medicalForm.nextDueDate" @click.stop="clearNextDueDate">x</text>
              <text class="picker-arrow" v-else>></text>
            </view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">备注</text>
          <input
            class="form-input"
            v-model="medicalForm.remark"
            placeholder="选填，如医生嘱托"
            placeholder-class="input-placeholder"
            maxlength="100"
          />
        </view>
        <view class="modal-footer">
          <view class="btn-cancel" @click="closeMedicalModal">
            <text>取消</text>
          </view>
          <view class="btn-confirm" :class="{ 'btn-disabled': submitting }" @click="submitMedical">
            <text>{{ submitting ? '保存中...' : '保存' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

const RECORD_TYPES = [
  { value: 'vaccination', label: '疫苗' },
  { value: 'deworming', label: '驱虫' },
  { value: 'checkup', label: '体检' },
  { value: 'treatment', label: '诊疗' },
  { value: 'other', label: '其他' }
]

export default {
  name: 'PetHealth',
  data() {
    const today = util.formatDate(new Date(), 'YYYY-MM-DD')
    return {
      today,
      petId: null,
      petName: '我的宠物',
      petAvatar: '/static/images/login-dog.png',
      // 体重记录（按日期正序，用于绘图）
      weightRecords: [],
      // 就医记录（按日期倒序）
      medicalRecords: [],
      // 30 天内到期/已逾期的疫苗驱虫提醒
      reminders: [],
      // 记体重弹窗
      showWeightModal: false,
      weightForm: { weight: '', recordDate: today, remark: '' },
      // 就医记录弹窗
      showMedicalModal: false,
      medicalForm: { recordType: 'vaccination', itemName: '', recordDate: today, nextDueDate: '', remark: '' },
      recordTypeOptions: RECORD_TYPES,
      submitting: false,
      // 画布尺寸（px，与 css 1:1 映射，避免 rpx 缩放导致坐标错位）
      chartWidth: 320,
      chartHeight: 160
    }
  },

  computed: {
    // 提醒列表视图：预计算样式类与文案（小程序 :class 不支持方法调用）
    remindersView() {
      return this.reminders.map(item => {
        const days = Number(item.daysLeft)
        let cls = 'later'
        let daysText = ''
        if (days < 0) {
          cls = 'overdue'
          daysText = `逾期${Math.abs(days)}天`
        } else if (days === 0) {
          cls = 'soon'
          daysText = '今天'
        } else if (days <= 7) {
          cls = 'soon'
          daysText = `${days}天后`
        } else {
          daysText = `${days}天后`
        }
        return { ...item, cls, daysText }
      })
    },

    latestWeightText() {
      if (!this.weightRecords.length) return '--'
      const latest = this.weightRecords[this.weightRecords.length - 1]
      return `${latest.weight}kg`
    },

    // 最近两次体重差值（kg），不足两次返回 null
    weightChange() {
      const recs = this.weightRecords
      if (recs.length < 2) return null
      const diff = Number(recs[recs.length - 1].weight) - Number(recs[recs.length - 2].weight)
      return Math.round(diff * 100) / 100
    },

    changeText() {
      if (this.weightChange === null) return '记录两次体重可看趋势'
      if (this.weightChange === 0) return '体重平稳'
      return `${this.weightChange > 0 ? '↑' : '↓'}${Math.abs(this.weightChange).toFixed(2)}kg`
    },

    changeClass() {
      if (this.weightChange === null || this.weightChange === 0) return 'flat'
      return this.weightChange > 0 ? 'up' : 'down'
    },

    // 最近 5 条体重记录（倒序展示）
    recentWeights() {
      return [...this.weightRecords].reverse().slice(0, 5)
    },

    medicalTypeIndex() {
      const idx = RECORD_TYPES.findIndex(t => t.value === this.medicalForm.recordType)
      return idx >= 0 ? idx : 0
    }
  },

  onLoad(options) {
    this.petId = options.petId ? Number(options.petId) : null
    this.petName = decodeURIComponent(options.petName || '') || '我的宠物'
    if (options.avatar) {
      this.petAvatar = decodeURIComponent(options.avatar)
    }

    uni.getSystemInfo({
      success: (res) => {
        // 减去容器左右 padding：20rpx + 24rpx 每侧，共 44px
        this.chartWidth = Math.max(240, res.windowWidth - 44)
      }
    })

    if (!this.petId) {
      uni.showToast({ title: '缺少宠物信息，请返回重试', icon: 'none' })
      return
    }
    this.loadAll()
  },

  methods: {
    // ==================== 数据加载 ====================

    loadAll() {
      this.loadWeights().finally(() => this.drawChart())
      this.loadMedical()
      this.loadReminders()
    },

    async loadWeights() {
      try {
        const res = await api.getPetWeightRecords(this.petId)
        if ((res.code === 0 || res.code === 200) && Array.isArray(res.data)) {
          this.weightRecords = res.data
        }
      } catch (e) {
        console.error('加载体重记录失败:', e)
      }
    },

    async loadMedical() {
      try {
        const res = await api.getPetMedicalRecords(this.petId)
        if ((res.code === 0 || res.code === 200) && Array.isArray(res.data)) {
          this.medicalRecords = res.data
        }
      } catch (e) {
        console.error('加载就医记录失败:', e)
      }
    },

    async loadReminders() {
      try {
        const res = await api.getPetUpcomingReminders(this.petId, 30)
        if ((res.code === 0 || res.code === 200) && Array.isArray(res.data)) {
          this.reminders = res.data
        }
      } catch (e) {
        console.error('加载到期提醒失败:', e)
      }
    },

    // ==================== 体重曲线绘制 ====================

    drawChart() {
      const records = this.weightRecords
      if (!records.length) return
      // canvas 由 v-if 控制，等渲染完成后再绘制
      this.$nextTick(() => {
        setTimeout(() => this.renderChart(), 50)
      })
    },

    renderChart() {
      const records = this.weightRecords
      const ctx = uni.createCanvasContext('weightChart', this)
      const w = this.chartWidth
      const h = this.chartHeight
      const padL = 36
      const padR = 12
      const padT = 14
      const padB = 22
      const plotW = w - padL - padR
      const plotH = h - padT - padB

      ctx.clearRect(0, 0, w, h)

      const values = records.map(r => Number(r.weight))
      let min = Math.min(...values)
      let max = Math.max(...values)
      if (min === max) {
        min = Math.max(0, min - 0.5)
        max = max + 0.5
      } else {
        const pad = (max - min) * 0.15
        min = Math.max(0, min - pad)
        max = max + pad
      }

      // x/y 坐标换算（单点时居中）
      const xOf = i => (records.length === 1 ? padL + plotW / 2 : padL + (plotW * i) / (records.length - 1))
      const yOf = v => padT + plotH - ((v - min) / (max - min)) * plotH

      // 网格线与 y 轴刻度
      ctx.setFontSize(9)
      for (let g = 0; g <= 4; g++) {
        const y = padT + (plotH * g) / 4
        ctx.setStrokeStyle('#f0f0f0')
        ctx.setLineWidth(0.5)
        ctx.beginPath()
        ctx.moveTo(padL, y)
        ctx.lineTo(w - padR, y)
        ctx.stroke()
        const val = max - ((max - min) * g) / 4
        ctx.setFillStyle('#999999')
        ctx.fillText(val.toFixed(1), 2, y + 3)
      }

      // x 轴日期标签（最多 4 个，均匀取样）
      const n = records.length
      let indices = [0]
      if (n > 1) {
        const cnt = Math.min(4, n)
        indices = []
        for (let k = 0; k < cnt; k++) {
          indices.push(Math.round(((n - 1) * k) / (cnt - 1)))
        }
        indices = [...new Set(indices)]
      }
      indices.forEach(i => {
        ctx.setFillStyle('#999999')
        const label = String(records[i].recordDate || '').slice(5)
        ctx.fillText(label, Math.min(Math.max(xOf(i) - 12, padL - 10), w - padR - 24), h - 6)
      })

      // 面积填充
      if (records.length > 1) {
        ctx.beginPath()
        ctx.moveTo(xOf(0), yOf(values[0]))
        for (let i = 1; i < records.length; i++) {
          ctx.lineTo(xOf(i), yOf(values[i]))
        }
        ctx.lineTo(xOf(records.length - 1), padT + plotH)
        ctx.lineTo(xOf(0), padT + plotH)
        ctx.closePath()
        ctx.setFillStyle('rgba(255, 107, 53, 0.08)')
        ctx.fill()
      }

      // 折线
      if (records.length > 1) {
        ctx.beginPath()
        ctx.moveTo(xOf(0), yOf(values[0]))
        for (let i = 1; i < records.length; i++) {
          ctx.lineTo(xOf(i), yOf(values[i]))
        }
        ctx.setStrokeStyle('#ff6b35')
        ctx.setLineWidth(2)
        ctx.stroke()
      }

      // 数据点
      values.forEach((v, i) => {
        ctx.beginPath()
        ctx.arc(xOf(i), yOf(v), 3, 0, Math.PI * 2)
        ctx.setFillStyle('#ffffff')
        ctx.fill()
        ctx.setStrokeStyle('#ff6b35')
        ctx.setLineWidth(1.5)
        ctx.stroke()
      })

      ctx.draw()
    },

    // ==================== 记体重 ====================

    openWeightModal() {
      this.weightForm = { weight: '', recordDate: this.today, remark: '' }
      this.showWeightModal = true
    },

    closeWeightModal() {
      if (this.submitting) return
      this.showWeightModal = false
    },

    onWeightDateChange(e) {
      this.weightForm.recordDate = e.detail.value
    },

    async submitWeight() {
      if (this.submitting) return
      const weight = Number(this.weightForm.weight)
      if (!weight || weight <= 0 || weight > 200) {
        uni.showToast({ title: '请输入有效体重（0~200kg）', icon: 'none' })
        return
      }
      if (!this.weightForm.recordDate) {
        uni.showToast({ title: '请选择称重日期', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const res = await api.createPetWeightRecord({
          petId: this.petId,
          weight,
          recordDate: this.weightForm.recordDate,
          remark: this.weightForm.remark || null
        })
        if (res.code === 0 || res.code === 200) {
          uni.showToast({ title: '记录成功', icon: 'success' })
          this.showWeightModal = false
          this.loadWeights().finally(() => this.drawChart())
        } else {
          uni.showToast({ title: res.msg || '记录失败', icon: 'none' })
        }
      } catch (e) {
        console.error('记录体重失败:', e)
        uni.showToast({ title: '网络错误，请重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },

    confirmDeleteWeight(record) {
      uni.showModal({
        title: '删除体重记录',
        content: `确定删除 ${record.recordDate} 的 ${record.weight}kg 记录吗？`,
        confirmText: '删除',
        confirmColor: '#ff3b30',
        success: (res) => {
          if (res.confirm) {
            this.deleteWeight(record)
          }
        }
      })
    },

    async deleteWeight(record) {
      try {
        const res = await api.deletePetWeightRecord(record.id)
        if (res.code === 0 || res.code === 200) {
          uni.showToast({ title: '已删除', icon: 'success' })
          this.loadWeights().finally(() => this.drawChart())
        } else {
          uni.showToast({ title: res.msg || '删除失败', icon: 'none' })
        }
      } catch (e) {
        console.error('删除体重记录失败:', e)
        uni.showToast({ title: '网络错误，请重试', icon: 'none' })
      }
    },

    // ==================== 就医记录 ====================

    typeText(type) {
      const found = RECORD_TYPES.find(t => t.value === type)
      return found ? found.label : '其他'
    },

    openMedicalModal() {
      this.medicalForm = {
        recordType: 'vaccination',
        itemName: '',
        recordDate: this.today,
        nextDueDate: '',
        remark: ''
      }
      this.showMedicalModal = true
    },

    closeMedicalModal() {
      if (this.submitting) return
      this.showMedicalModal = false
    },

    onTypeChange(e) {
      const option = RECORD_TYPES[Number(e.detail.value)]
      if (option) {
        this.medicalForm.recordType = option.value
      }
    },

    onRecordDateChange(e) {
      this.medicalForm.recordDate = e.detail.value
    },

    onNextDueDateChange(e) {
      this.medicalForm.nextDueDate = e.detail.value
    },

    clearNextDueDate() {
      this.medicalForm.nextDueDate = ''
    },

    async submitMedical() {
      if (this.submitting) return
      const form = this.medicalForm
      if (!form.itemName || !String(form.itemName).trim()) {
        uni.showToast({ title: '请填写项目名称', icon: 'none' })
        return
      }
      if (!form.recordDate) {
        uni.showToast({ title: '请选择日期', icon: 'none' })
        return
      }
      if (form.nextDueDate && form.nextDueDate <= form.recordDate) {
        uni.showToast({ title: '下次日期需晚于本次日期', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const res = await api.createPetMedicalRecord({
          petId: this.petId,
          recordType: form.recordType,
          itemName: String(form.itemName).trim(),
          recordDate: form.recordDate,
          nextDueDate: form.nextDueDate || null,
          remark: form.remark || null
        })
        if (res.code === 0 || res.code === 200) {
          uni.showToast({ title: '添加成功', icon: 'success' })
          this.showMedicalModal = false
          this.loadMedical()
          this.loadReminders()
        } else {
          uni.showToast({ title: res.msg || '添加失败', icon: 'none' })
        }
      } catch (e) {
        console.error('添加就医记录失败:', e)
        uni.showToast({ title: '网络错误，请重试', icon: 'none' })
      } finally {
        this.submitting = false
      }
    },

    confirmDeleteMedical(record) {
      uni.showModal({
        title: '删除就医记录',
        content: `确定删除「${record.itemName}」这条记录吗？`,
        confirmText: '删除',
        confirmColor: '#ff3b30',
        success: (res) => {
          if (res.confirm) {
            this.deleteMedical(record)
          }
        }
      })
    },

    async deleteMedical(record) {
      try {
        const res = await api.deletePetMedicalRecord(record.id)
        if (res.code === 0 || res.code === 200) {
          uni.showToast({ title: '已删除', icon: 'success' })
          this.loadMedical()
          this.loadReminders()
        } else {
          uni.showToast({ title: res.msg || '删除失败', icon: 'none' })
        }
      } catch (e) {
        console.error('删除就医记录失败:', e)
        uni.showToast({ title: '网络错误，请重试', icon: 'none' })
      }
    },

  }
}
</script>

<style lang="scss" scoped>
.health-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding: 20rpx;
  padding-bottom: 60rpx;
}

/* 宠物信息头 */
.pet-header {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  margin-bottom: 20rpx;
}

.pet-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 2rpx solid #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  background: #f0f0f0;
  flex-shrink: 0;
}

.pet-info {
  flex: 1;
  min-width: 0;
}

.pet-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.weight-summary {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
  margin-top: 8rpx;
}

.current-weight {
  font-size: 40rpx;
  font-weight: bold;
  color: #ff6b35;
}

.weight-change {
  font-size: 22rpx;
}

.weight-change.up {
  color: #e53935;
}

.weight-change.down {
  color: #43a047;
}

.weight-change.flat {
  color: #999;
}

/* 通用 section */
.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.title-text {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.title-sub {
  font-size: 22rpx;
  color: #999;
}

.title-action {
  background: #fff3e8;
  color: #ff6b35;
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
}

.title-action:active {
  background: #ffe3d0;
}

.empty-tip {
  padding: 40rpx 0;
  text-align: center;
}

.empty-tip text {
  font-size: 24rpx;
  color: #999;
}

/* 健康提醒 */
.reminder-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.reminder-item:last-child {
  border-bottom: none;
}

.reminder-badge {
  padding: 6rpx 14rpx;
  border-radius: 10rpx;
  font-size: 20rpx;
  flex-shrink: 0;
  background: #e8f5e9;
  color: #2e7d32;
}

.reminder-badge.soon {
  background: #fff3e0;
  color: #ef6c00;
}

.reminder-badge.overdue {
  background: #ffebee;
  color: #c62828;
}

.reminder-body {
  flex: 1;
  min-width: 0;
}

.reminder-name {
  display: block;
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reminder-date {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.reminder-days {
  font-size: 24rpx;
  flex-shrink: 0;
  color: #2e7d32;
}

.reminder-days.soon {
  color: #ef6c00;
}

.reminder-days.overdue {
  color: #c62828;
  font-weight: bold;
}

/* 体重曲线 */
.chart-wrap {
  width: 100%;
}

.chart-canvas {
  display: block;
}

/* 最近体重记录 */
.recent-weights {
  margin-top: 16rpx;
  border-top: 1rpx solid #f5f5f5;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 14rpx 0;
  border-bottom: 1rpx solid #f8f8f8;
}

.recent-item:last-child {
  border-bottom: none;
}

.recent-date {
  font-size: 24rpx;
  color: #666;
  width: 170rpx;
  flex-shrink: 0;
}

.recent-value {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.recent-remark {
  flex: 1;
  min-width: 0;
  font-size: 22rpx;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-delete {
  font-size: 22rpx;
  color: #c62828;
  flex-shrink: 0;
  padding: 4rpx 8rpx;
}

/* 就医时间线 */
.timeline {
  position: relative;
}

.timeline-item {
  display: flex;
  gap: 20rpx;
  position: relative;
  padding-bottom: 28rpx;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 9rpx;
  top: 24rpx;
  bottom: 0;
  width: 2rpx;
  background: #f0f0f0;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-dot {
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6rpx;
  background: #bdbdbd;
  position: relative;
  z-index: 1;
}

.dot-vaccination {
  background: #42a5f5;
}

.dot-deworming {
  background: #66bb6a;
}

.dot-checkup {
  background: #ab47bc;
}

.dot-treatment {
  background: #ff7043;
}

.timeline-content {
  flex: 1;
  min-width: 0;
  background: #fafafa;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
}

.record-head {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.record-type {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: #eeeeee;
  color: #666;
}

.tag-vaccination {
  background: #e3f2fd;
  color: #1565c0;
}

.tag-deworming {
  background: #e8f5e9;
  color: #2e7d32;
}

.tag-checkup {
  background: #f3e5f5;
  color: #7b1fa2;
}

.tag-treatment {
  background: #fbe9e7;
  color: #d84315;
}

.record-name {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.record-date,
.record-next {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.record-next {
  color: #ef6c00;
}

.record-remark {
  display: block;
  font-size: 22rpx;
  color: #666;
  margin-top: 8rpx;
  word-break: break-all;
}

.record-delete {
  display: inline-block;
  font-size: 22rpx;
  color: #c62828;
  margin-top: 8rpx;
  padding: 4rpx 0;
}

/* 弹窗 */
.modal-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}

.modal-sheet {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.modal-title {
  text-align: center;
  margin-bottom: 32rpx;
}

.modal-title text {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.form-label {
  width: 150rpx;
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;
}

.form-input {
  flex: 1;
  background: #f8f8f8;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 26rpx;
  color: #333;
}

.input-placeholder {
  color: #bbb;
}

.form-picker {
  flex: 1;
  background: #f8f8f8;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;
  font-size: 26rpx;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.picker-empty {
  color: #bbb;
}

.picker-arrow {
  color: #bbb;
  font-size: 24rpx;
  padding: 0 8rpx;
}

.modal-footer {
  display: flex;
  gap: 24rpx;
  margin-top: 40rpx;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  text-align: center;
  padding: 22rpx 0;
  border-radius: 44rpx;
  font-size: 28rpx;
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.btn-confirm {
  background: #ff6b35;
  color: #fff;
  font-weight: 500;
}

.btn-confirm:active {
  background: #f05a25;
}

.btn-disabled {
  opacity: 0.6;
}
</style>
