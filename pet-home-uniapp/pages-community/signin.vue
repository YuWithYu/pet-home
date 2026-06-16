<template>
  <view class="signin-page">
    <!-- 黄色背景区域（半圆形） -->
    <view class="yellow-background-area">
      <!-- 头部罐头信息 -->
      <view class="header-section">
        <view class="header-content">
          <view class="header-left">
            <view class="header-title">我的罐头</view>
            <view class="header-balance">{{ userBalance }} g</view>
          </view>
          <view class="header-actions">
            <view class="header-btn" @click="goToExchange">罐头兑换</view>
          </view>
        </view>
      </view>
    </view>

    <!-- 签到日历 -->
    <view class="signin-card">
      <view class="signin-streak">已连续签到{{ consecutiveDays }}天</view>
      <view class="signin-reward">连续签到满7天,可领取150g</view>
      <view class="calendar">
        <view 
          class="calendar-day"
          v-for="(day, index) in calendarDays"
          :key="index"
          :class="{ active: day.signed, current: day.isToday }"
        >
          <view class="day-label">{{ day.label }}</view>
          <view class="day-status" v-if="day.signed">
            <text class="check-icon">✓</text>
          </view>
          <view class="day-status" v-else-if="day.isToday && canSignIn">
            <text class="signin-today">签到</text>
          </view>
          <view class="day-status" v-else>未</view>
        </view>
      </view>
      <!-- 签到按钮 -->
      <view class="signin-btn-wrapper" v-if="canSignIn">
        <view class="signin-btn" @click="doSignIn">立即签到</view>
      </view>
    </view>

    <!-- 每日任务 -->
    <view class="tasks-card">
      <view class="tasks-title">每日任务</view>
      <view 
        class="task-item"
        v-for="task in tasks"
        :key="task.key"
      >
        <view class="task-icon">
          <image :src="task.icon" mode="aspectFit" class="task-icon-image" />
        </view>
        <view class="task-info">
          <view class="task-name">{{ task.name }} <text class="progress-count">({{ task.current }}/{{ task.target }})</text></view>
          <view class="task-desc">{{ task.description }}</view>
        </view>
        <view 
          class="task-btn"
          :class="{ completed: task.completed, canClaim: task.canClaim }"
          @click="handleTask(task.key)"
        >
          {{ task.canClaim ? '领取' : (task.completed ? '已完成' : '去完成') }}
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  data() {
    return {
      userBalance: 0,
      consecutiveDays: 0,
      canSignIn: false,
      calendarDays: [
        { label: '周一', signed: false, isToday: false },
        { label: '周二', signed: false, isToday: false },
        { label: '周三', signed: false, isToday: false },
        { label: '周四', signed: false, isToday: false },
        { label: '周五', signed: false, isToday: false },
        { label: '周六', signed: false, isToday: false },
        { label: '周日', signed: false, isToday: false }
      ],
      tasks: []
    }
  },

  onLoad() {
    this.loadSignInData()
  },

  onShow() {
    this.loadSignInData()
  },

  methods: {
    async loadSignInData() {
      try {
        // 调用后端API获取真实数据
        const signInRes = await api.getSignInData()
        
        if (signInRes && signInRes.data) {
          const balance = signInRes.data.balance !== undefined && signInRes.data.balance !== null ? signInRes.data.balance : 0
          const days = signInRes.data.consecutiveDays || 0
          const canSign = signInRes.data.canSignIn !== undefined ? signInRes.data.canSignIn : true
          
          this.userBalance = balance
          this.consecutiveDays = days
          this.canSignIn = canSign
          
          if (signInRes.data.calendar && Array.isArray(signInRes.data.calendar)) {
            this.updateCalendar(signInRes.data.calendar)
          }
        } else {
          uni.showToast({
            title: '数据格式异常，请重试',
            icon: 'none',
            duration: 2000
          })
        }

        await this.loadTaskProgress()
      } catch (error) {
        console.error('加载签到数据失败:', error)
        console.error('错误详情:', error.message, error.stack)
        // 显示详细错误信息
        uni.showToast({
          title: '加载数据失败: ' + (error.message || '未知错误'),
          icon: 'none',
          duration: 3000
        })
      }
    },

    updateCalendar(calendarData) {
      const today = new Date().getDay()
      const adjustedDay = today === 0 ? 6 : today - 1
      
      this.calendarDays = this.calendarDays.map((day, index) => ({
        label: day.label,
        signed: calendarData[index] || false,
        isToday: index === adjustedDay
      }))
    },

    async loadTaskProgress() {
      try {
        const res = await api.getTaskProgress()
        
        if (res && res.data && res.data.tasks && Array.isArray(res.data.tasks) && res.data.tasks.length > 0) {
          // 从后端获取任务列表，动态构建任务数据
          this.tasks = res.data.tasks.map(task => {
            // 根据任务类型设置图标和描述
            const taskConfig = this.getTaskConfig(task.id || task.key)
            return {
              key: task.id || task.key,
              icon: taskConfig.icon,
              name: task.name || taskConfig.name,
              current: task.current || task.progress || 0,
              target: task.target || 1,
              description: taskConfig.description.replace('{reward}', task.reward || taskConfig.reward),
              completed: task.completed || false,
              canClaim: task.canClaim || false,
              reward: task.reward || taskConfig.reward
            }
          })
        } else {
          // 如果后端没有返回任务或任务列表为空，使用默认任务配置
          this.initDefaultTasks()
        }
      } catch (error) {
        console.error('加载任务进度失败:', error)
        console.error('错误详情:', error.message, error.stack)
        // 如果接口失败，使用默认任务配置
        this.initDefaultTasks()
      }
    },

    initDefaultTasks() {
      // 默认任务配置（仅作为后备方案）
      const defaultTasks = [
        { key: 'like_content', icon: '/static/images/点赞内容.png', name: '点赞内容', target: 2, description: '点赞内容 (图文、视频)+20g', reward: 20 },
        { key: 'like_comment', icon: '/static/images/点赞-评论.png', name: '点赞评论', target: 4, description: '点赞评论+10g', reward: 10 },
        { key: 'post_content', icon: '/static/images/任务中心-发布内容.png', name: '发布内容', target: 1, description: '发布内容 (图文、视频)+60g', reward: 60 },
        { key: 'comment_content', icon: '/static/images/评论内容.png', name: '评论内容', target: 2, description: '评论内容 (图文、视频)+50g', reward: 50 },
        { key: 'follow_user', icon: '/static/images/关注用户.png', name: '关注用户', target: 2, description: '关注用户+30g', reward: 30 },
        { key: 'collect_content', icon: '/static/images/icon-收藏内容.png', name: '收藏内容', target: 2, description: '收藏内容+10g', reward: 10 }
      ]
      
      this.tasks = defaultTasks.map(task => ({
        ...task,
        current: 0,
        completed: false,
        canClaim: false
      }))
    },

    getTaskConfig(taskKey) {
      // 任务配置映射
      const configs = {
        'like_content': { icon: '/static/images/点赞内容.png', name: '点赞内容', description: '点赞内容 (图文、视频)+{reward}g', reward: 20 },
        'like_comment': { icon: '/static/images/点赞-评论.png', name: '点赞评论', description: '点赞评论+{reward}g', reward: 10 },
        'post_content': { icon: '/static/images/任务中心-发布内容.png', name: '发布内容', description: '发布内容 (图文、视频)+{reward}g', reward: 60 },
        'comment_content': { icon: '/static/images/评论内容.png', name: '评论内容', description: '评论内容 (图文、视频)+{reward}g', reward: 50 },
        'follow_user': { icon: '/static/images/关注用户.png', name: '关注用户', description: '关注用户+{reward}g', reward: 30 },
        'collect_content': { icon: '/static/images/icon-收藏内容.png', name: '收藏内容', description: '收藏内容+{reward}g', reward: 10 },
        'daily_signin': { icon: '📅', name: '每日签到', description: '每日签到+{reward}g', reward: 10 },
        'share_app': { icon: '📤', name: '分享小程序', description: '分享小程序+{reward}g', reward: 5 }
      }
      return configs[taskKey] || { icon: '📋', name: '任务', description: '完成任务+{reward}g', reward: 10 }
    },

    async handleTask(taskKey) {
      const task = this.tasks.find(t => t.key === taskKey)
      if (!task) {
        return
      }

      // 如果可以领取奖励，直接领取
      if (task.canClaim) {
        await this.claimTaskReward(taskKey)
        return
      }

      // 如果已完成，提示
      if (task.completed) {
        uni.showToast({ title: '任务已完成', icon: 'none' })
        return
      }

      // 根据任务类型跳转到相应页面
      const routes = {
        'like_content': 'pages/community/index',
        'like_comment': 'pages/community/index',
        'post_content': '/pages-community/publish',
        'comment_content': 'pages/community/index',
        'follow_user': 'pages/community/index',
        'collect_content': 'pages/community/index',
        'daily_signin': '', // 签到任务在当前页面处理
        'share_app': '' // 分享任务需要调用分享API
      }

      // 处理签到任务
      if (taskKey === 'daily_signin') {
        if (this.canSignIn) {
          await this.doSignIn()
        } else {
          uni.showToast({ title: '今日已签到', icon: 'none' })
        }
        return
      }

      // 处理分享任务
      if (taskKey === 'share_app') {
        // #ifdef MP-WEIXIN
        uni.share({
          provider: 'weixin',
          scene: 'WXSceneSession',
          type: 0,
          success: () => {
            uni.showToast({ title: '分享成功', icon: 'success' })
            // 更新任务进度
            this.updateTaskProgress(taskKey, 1)
          },
          fail: () => {
            uni.showToast({ title: '分享失败', icon: 'none' })
          }
        })
        // #endif
        // #ifndef MP-WEIXIN
        uni.showToast({ title: '请使用微信分享', icon: 'none' })
        // #endif
        return
      }

      // 跳转到其他页面
      const route = routes[taskKey]
      if (route) {
        // 检查是否是 tabbar 页面（tabbar 页面路径不带 / 前缀）
        const tabbarPages = ['pages/community/index', 'pages/main/index', 'goods/category/index', 'pages/index/index']
        const isTabbarPage = tabbarPages.some(page => route === page || route === '/' + page)
        
        if (isTabbarPage) {
          // tabbar 页面使用 switchTab（路径不带 / 前缀）
          const tabbarPath = route.startsWith('/') ? route.substring(1) : route
          uni.switchTab({ 
            url: '/' + tabbarPath,
            fail: (err) => {
              console.error('跳转失败:', err)
              uni.showToast({ title: '页面不存在', icon: 'none' })
            }
          })
        } else {
          // 普通页面使用 navigateTo
          uni.navigateTo({ 
            url: route,
            fail: (err) => {
              console.error('跳转失败:', err)
              uni.showToast({ title: '页面不存在', icon: 'none' })
            }
          })
        }
      } else {
        uni.showToast({ title: '任务功能开发中', icon: 'none' })
      }
    },

    async doSignIn() {
      try {
        uni.showLoading({ title: '签到中...' })
        const res = await api.doSignIn()
        uni.hideLoading()
        
        if (res.code === 200 || res.code === 0) {
          const points = res.data?.points || 0
          const weekReward = res.data?.weekReward || 0
          const balance = res.data?.balance !== undefined && res.data?.balance !== null ? res.data?.balance : (this.userBalance + points)
          const days = res.data?.consecutiveDays || (this.consecutiveDays + 1)
          
          // 如果有7天奖励，显示特殊提示
          let toastTitle = `签到成功，获得${points}g`
          if (weekReward > 0) {
            toastTitle = `签到成功！连续签到满${days}天，获得${points}g（含7天奖励${weekReward}g）`
          }
          
          uni.showToast({
            title: toastTitle,
            icon: 'success',
            duration: 3000
          })
          
          this.userBalance = balance
          this.consecutiveDays = days
          this.canSignIn = false
          
          // 更新日历
          const today = new Date().getDay()
          const adjustedDay = today === 0 ? 6 : today - 1
          if (this.calendarDays[adjustedDay]) {
            this.calendarDays[adjustedDay].signed = true
          }
          
          // 重新加载数据，确保数据同步
          setTimeout(async () => {
            await this.loadSignInData()
          }, 500)
        } else {
          console.error('签到失败，响应码:', res.code, '错误信息:', res.msg)
          uni.showToast({ 
            title: res.msg || '签到失败', 
            icon: 'none',
            duration: 2000
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('签到失败:', error)
        console.error('错误详情:', error.message, error.stack)
        uni.showToast({ 
          title: error.message || '签到失败，请重试', 
          icon: 'none',
          duration: 2000
        })
      }
    },

    updateTaskProgress(taskKey, progress) {
      const task = this.tasks.find(t => t.key === taskKey)
      if (task) {
        task.current = (task.current || 0) + progress
        if (task.current >= task.target) {
          task.completed = true
          task.canClaim = true
        }
      }
    },

    async claimTaskReward(taskKey) {
      try {
        uni.showLoading({ title: '领取中...' })
        const res = await api.claimTaskReward(taskKey)
        uni.hideLoading()
        
        if (res.code === 200 || res.code === 0) {
          const reward = res.data?.points || res.data?.reward || 0
          const balance = res.data?.balance || this.userBalance
          
          uni.showToast({
            title: `获得${reward}g罐头`,
            icon: 'success'
          })
          
          this.userBalance = balance
          
          // 更新任务状态
          const task = this.tasks.find(t => t.key === taskKey)
          if (task) {
            task.completed = true
            task.canClaim = false
          }
          
          // 重新加载任务进度
          await this.loadTaskProgress()
        } else {
          uni.showToast({ 
            title: res.msg || '领取失败', 
            icon: 'none' 
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('领取奖励失败:', error)
        uni.showToast({ 
          title: error.message || '领取失败', 
          icon: 'none' 
        })
      }
    },

    goToExchange() {
      uni.navigateTo({
        url: '/points-mall/index'
      })
    }
  }
}
</script>

<style scoped>
.signin-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 黄色背景区域（半圆形，紧贴导航栏） */
.yellow-background-area {
  background: linear-gradient(to right, #FF8C00 0%, #FFD700 100%);
  height: 280rpx;
  border-bottom-left-radius: 50%;
  border-bottom-right-radius: 50%;
  position: relative;
  z-index: 1;
  margin-top: 0;
  margin-left: 0;
  margin-right: 0;
  width: 100%;
}

/* 头部罐头信息 */
.header-section {
  position: relative;
  padding: 40rpx 30rpx 0;
  z-index: 2;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 2;
}

.header-left {
  flex: 1;
}

.header-title {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 6rpx;
  font-weight: 500;
}

.header-balance {
  font-size: 42rpx;
  font-weight: bold;
  color: #fff;
  line-height: 1.2;
}

.header-actions {
  display: flex;
  flex-direction: row;
  align-items: flex-end;
}

.header-btn {
  background-color: #fff;
  color: #ff7849;
  padding: 10rpx 20rpx;
  border-radius: 40rpx;
  font-size: 22rpx;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

/* 签到卡片 */
.signin-card {
  background-color: #fff;
  margin: -120rpx 40rpx 20rpx;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 10;
}

.signin-streak {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 10rpx;
}

.signin-reward {
  font-size: 24rpx;
  color: #ff7849;
  margin-bottom: 30rpx;
}

.calendar {
  display: flex;
  justify-content: space-between;
  gap: 10rpx;
}

.calendar-day {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 10rpx;
  border-radius: 12rpx;
  background-color: #f5f5f5;
}

.calendar-day.current {
  background-color: #fff4e6;
}

.calendar-day.active {
  background-color: #ffd700;
}

.day-label {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.day-status {
  font-size: 28rpx;
  color: #999;
  font-weight: 500;
}

.check-icon {
  color: #fff;
}

.calendar-day.current .day-status,
.calendar-day.active .check-icon {
  color: #fff;
}

.signin-today {
  color: #ff7849;
  font-weight: 600;
}

/* 签到按钮 */
.signin-btn-wrapper {
  margin-top: 30rpx;
  display: flex;
  justify-content: center;
}

.signin-btn {
  background: linear-gradient(135deg, #ff7849 0%, #ff9a5a 100%);
  color: #fff;
  padding: 20rpx 80rpx;
  border-radius: 50rpx;
  font-size: 28rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 12rpx rgba(255, 120, 73, 0.3);
  text-align: center;
}

.signin-btn:active {
  opacity: 0.8;
  transform: scale(0.98);
}

/* 任务卡片 */
.tasks-card {
  background-color: #fff;
  margin: 0 30rpx;
  border-radius: 20rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.tasks-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 30rpx;
}

.task-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-icon {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff9e6;
  border-radius: 16rpx;
  margin-right: 20rpx;
}

.task-icon-image {
  width: 50rpx;
  height: 50rpx;
}

.task-info {
  flex: 1;
}

.task-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.progress-count {
  font-size: 28rpx;
  color: #ff7849;
  font-weight: 500;
}

.task-desc {
  font-size: 24rpx;
  color: #999;
}

.task-btn {
  background-color: #ffd700;
  color: #333;
  padding: 16rpx 32rpx;
  border-radius: 50rpx;
  font-size: 26rpx;
  font-weight: 500;
  white-space: nowrap;
}

.task-btn.completed {
  background-color: #e0e0e0;
  color: #999;
}

.task-btn.canClaim {
  background-color: #ff7849;
  color: #fff;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}
</style>
