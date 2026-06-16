<template>
  <div class="my-reviews-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的评价</span>
          <el-button type="primary" :icon="Refresh" @click="loadReviews" :loading="loading">刷新</el-button>
        </div>
      </template>

      <div v-if="!loading && reviews.length > 0" class="summary-section">
        <div class="summary-item">
          <div class="summary-value">{{ summary.avgRating }}</div>
          <div class="summary-label">综合评分</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">{{ summary.total }}</div>
          <div class="summary-label">服务单数</div>
        </div>
        <div class="summary-item">
          <div class="summary-value">{{ summary.positiveRate }}</div>
          <div class="summary-label">好评率</div>
        </div>
      </div>

      <div v-if="!loading && reviews.length > 0" class="distribution-section">
        <div v-for="row in ratingDistribution" :key="row.star" class="dist-row">
          <span class="dist-label">{{ row.star }}星</span>
          <el-progress :percentage="row.percentNum" :stroke-width="8" :show-text="false" color="#409eff" />
          <span class="dist-count">{{ row.count }}</span>
        </div>
      </div>

      <div v-if="!loading && reviews.length > 0" class="filter-section">
        <el-radio-group v-model="activeFilter" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="good">好评(4-5星)</el-radio-button>
          <el-radio-button label="mid">中评(3星)</el-radio-button>
          <el-radio-button label="bad">待改进(1-2星)</el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="loading" class="empty-state">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="filteredReviews.length === 0" class="empty-state">
        <el-empty :description="emptyText" />
      </div>
      <div v-else class="review-list">
        <el-card v-for="(item, idx) in filteredReviews" :key="item.id + '-' + idx" class="review-card" shadow="hover">
          <div class="review-top">
            <el-avatar :size="40" :src="item.userAvatar || undefined" class="review-avatar">
              {{ (item.userName || '匿').slice(0, 1) }}
            </el-avatar>
            <div class="review-main">
              <div class="name-line">
                <span class="user-name">{{ item.userName || '匿名用户' }}</span>
                <span class="time">{{ formatTime(item.createTime).slice(0, 10) }}</span>
              </div>
              <el-rate :model-value="Number(item.rating)" disabled show-score text-color="#ff9900" score-template="{value} 分" />
            </div>
          </div>
          <div class="comment">{{ item.comment || '用户未填写文字评价' }}</div>
          <div class="order-ref">
            <el-tag size="small" type="info">{{ formatOrderRef(item) }}</el-tag>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const reviews = ref([])
const activeFilter = ref('all')

const summary = computed(() => {
  const total = reviews.value.length
  if (!total) return { avgRating: '0.0', total: 0, positiveRate: '0%' }
  const ratings = reviews.value.map((it) => Number(it.rating || 0))
  const scoreTotal = ratings.reduce((sum, cur) => sum + cur, 0)
  const avg = (scoreTotal / total).toFixed(1)
  const positive = ratings.filter((it) => it >= 4).length
  return { avgRating: avg, total, positiveRate: `${Math.round((positive / total) * 100)}%` }
})

const ratingDistribution = computed(() => {
  const total = reviews.value.length || 1
  return [5, 4, 3, 2, 1].map((star) => {
    const count = reviews.value.filter((it) => Number(it.rating || 0) === star).length
    return { star, count, percentNum: Math.round((count / total) * 100) }
  })
})

const filteredReviews = computed(() => {
  if (activeFilter.value === 'all') return reviews.value
  if (activeFilter.value === 'good') return reviews.value.filter((it) => Number(it.rating || 0) >= 4)
  if (activeFilter.value === 'mid') return reviews.value.filter((it) => Number(it.rating || 0) === 3)
  return reviews.value.filter((it) => Number(it.rating || 0) <= 2)
})

const emptyText = computed(() => {
  if (reviews.value.length === 0) return '暂无评价记录'
  return '当前筛选下暂无评价'
})

const loadReviews = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/self/reviews')
    if (res.code === 200 || res.code === 0) {
      const source = Array.isArray(res.data) ? res.data : []
      reviews.value = source.sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))
    } else {
      reviews.value = []
      ElMessage.error(res.msg || '加载失败')
    }
  } catch (e) {
    reviews.value = []
    ElMessage.error('加载评价失败')
  } finally {
    loading.value = false
  }
}

const formatTime = (val) => {
  if (!val) return '-'
  return String(val).replace('T', ' ').slice(0, 19)
}

const formatOrderRef = (item) => {
  const date = formatTime(item.createTime).slice(0, 10)
  const service = item.appointmentTypeName || '服务订单'
  return `${date} ${service}`
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.my-reviews-container {
  padding: 16px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.summary-section {
  display: flex;
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}
.summary-item {
  flex: 1;
  text-align: center;
}
.summary-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.summary-label {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}
.distribution-section {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #fafbfc;
  border-radius: 8px;
}
.dist-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.dist-row:last-child {
  margin-bottom: 0;
}
.dist-label {
  width: 36px;
  color: #606266;
  font-size: 13px;
}
.dist-row .el-progress {
  flex: 1;
  margin: 0 12px;
}
.dist-count {
  width: 32px;
  text-align: right;
  color: #909399;
  font-size: 13px;
}
.filter-section {
  margin-bottom: 16px;
}
.empty-state {
  padding: 48px 0;
  text-align: center;
}
.review-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.review-card {
  border-radius: 8px;
}
.review-top {
  display: flex;
  align-items: flex-start;
}
.review-avatar {
  background: linear-gradient(135deg, #7d8cff, #6fc2ff);
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}
.review-main {
  flex: 1;
  margin-left: 12px;
}
.name-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.user-name {
  color: #303133;
  font-weight: 600;
  font-size: 14px;
}
.time {
  color: #c0c4cc;
  font-size: 12px;
}
.comment {
  margin-top: 12px;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}
.order-ref {
  margin-top: 12px;
}
</style>
