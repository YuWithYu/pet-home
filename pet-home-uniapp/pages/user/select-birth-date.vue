<template>
  <view class="select-birth-date-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>选择出生日期</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="select-birth-date-content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <!-- 主要内容容器（居中） -->
      <view class="main-content-container">
        <!-- 出生日期选择区域 -->
        <view class="birth-date-selection-container">
          <view class="date-input-container" @click="showDatePicker">
            <text class="date-text" :class="{ 'placeholder': !selectedBirthDate }">
              {{ selectedBirthDate || '请选择宠物的出生日期' }}
            </text>
            <text class="arrow-icon">></text>
          </view>
        </view>
      </view>

      <!-- 下一步按钮 -->
      <view class="next-button-section">
        <view 
          class="next-button" 
          :class="{ 'active': selectedBirthDate }"
          @click="goToNext"
        >
          <text>下一步</text>
        </view>
      </view>
    </view>

    <!-- 日期选择器模态框 -->
    <view class="date-picker-modal" v-if="showDatePickerModal" @click="onDateCancel">
      <view class="date-picker-content" @click.stop>
        <view class="date-picker-header">
          <text class="date-picker-title">选择出生日期</text>
          <text class="date-picker-cancel" @click="onDateCancel">取消</text>
        </view>
        <picker-view 
          class="date-picker-view" 
          :value="pickerValue" 
          @change="onPickerChange"
        >
          <picker-view-column>
            <view v-for="(year, index) in years" :key="index" class="picker-item">
              {{ year }}年
            </view>
          </picker-view-column>
          <picker-view-column>
            <view v-for="(month, index) in months" :key="index" class="picker-item">
              {{ month }}月
            </view>
          </picker-view-column>
          <picker-view-column>
            <view v-for="(day, index) in days" :key="index" class="picker-item">
              {{ day }}日
            </view>
          </picker-view-column>
        </picker-view>
        <view class="date-picker-footer">
          <view class="date-picker-confirm" @click="confirmDate">
            <text>确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'SelectBirthDate',
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      selectedBirthDate: '',
      showDatePickerModal: false,
      pickerValue: [0, 0, 0],
      years: [],
      months: [],
      days: []
    }
  },
  
  computed: {
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight
    }
  },
  
  onLoad() {
    // 获取系统信息
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
        this.navBarHeight = res.platform === 'ios' ? 44 : 48
      }
    })
    
    // 初始化日期选择器数据
    this.initDatePicker()
  },
  
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 初始化日期选择器数据
    initDatePicker() {
      const currentYear = new Date().getFullYear()
      
      // 生成年份（从当前年份往前推20年）
      for (let i = currentYear; i >= currentYear - 20; i--) {
        this.years.push(i)
      }
      
      // 生成月份
      for (let i = 1; i <= 12; i++) {
        this.months.push(i)
      }
      
      // 生成日期
      this.updateDays()
    },
    
    // 更新日期数组
    updateDays() {
      this.days = []
      const year = this.years[this.pickerValue[0]]
      const month = this.months[this.pickerValue[1]]
      const daysInMonth = new Date(year, month, 0).getDate()
      
      for (let i = 1; i <= daysInMonth; i++) {
        this.days.push(i)
      }
    },
    
    // 显示日期选择器
    showDatePicker() {
      this.showDatePickerModal = true
    },
    
    // 日期选择器变化
    onPickerChange(e) {
      this.pickerValue = e.detail.value
      this.updateDays()
    },
    
    // 确认日期选择
    confirmDate() {
      const year = this.years[this.pickerValue[0]]
      const month = this.months[this.pickerValue[1]]
      const day = this.days[this.pickerValue[2]]
      
      this.selectedBirthDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
      this.showDatePickerModal = false
    },
    
    // 日期选择器取消
    onDateCancel() {
      this.showDatePickerModal = false
    },
    
    // 下一步
    goToNext() {
      if (!this.selectedBirthDate) {
        uni.showToast({
          title: '请选择出生日期',
          icon: 'none'
        })
        return
      }
      
      // 保存出生日期信息到本地存储
      const basicInfo = uni.getStorageSync('petBasicInfo') || {}
      basicInfo.birthDate = this.selectedBirthDate
      uni.setStorageSync('petBasicInfo', basicInfo)
      
      // 跳转到选择到家日期页面
      uni.navigateTo({
        url: '/pages/user/select-arrival-date'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.select-birth-date-container {
  min-height: 100vh;
  background-color: #fff;
}

/* 自定义白色导航栏 */
.custom-white-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: #fff;
  z-index: 100;
  border-bottom: 1rpx solid #f0f0f0;
}

.navbar-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
}

.navbar-left {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 40rpx;
  color: #333;
  font-weight: bold;
}

.navbar-title {
  flex: 1;
  text-align: center;
}

.navbar-title text {
  font-size: 32rpx;
  color: #333;
  font-weight: 400;
}

.navbar-right {
  width: 60rpx;
}

/* 页面内容 */
.select-birth-date-content {
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 88rpx);
}

/* 主要内容容器（居中） */
.main-content-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
}

/* 出生日期选择容器（居中） */
.birth-date-selection-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  max-width: 600rpx;
}

.date-input-container {
  width: 100%;
  background: #fff;
  border-radius: 12rpx;
  border: 1rpx solid #e0e0e0;
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: background-color 0.2s ease;
}

.date-input-container:active {
  background: #f8f8f8;
}

.date-text {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.date-text.placeholder {
  color: #999;
}

.arrow-icon {
  font-size: 24rpx;
  color: #999;
  margin-left: 20rpx;
}

/* 下一步按钮 */
.next-button-section {
  position: fixed;
  bottom: 40rpx;
  left: 30rpx;
  right: 30rpx;
}

.next-button {
  width: 100%;
  height: 88rpx;
  background: #e0e0e0;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.next-button.active {
  background: #ffd700;
}

.next-button text {
  font-size: 32rpx;
  color: #999;
  font-weight: 500;
}

.next-button.active text {
  color: #333;
  font-weight: bold;
}

.next-button:active {
  transform: scale(0.98);
}

/* 日期选择器模态框 */
.date-picker-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.date-picker-content {
  width: 100%;
  background: #fff;
  border-radius: 20rpx 20rpx 0 0;
  padding: 40rpx 30rpx;
  max-height: 80vh;
}

.date-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.date-picker-title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.date-picker-cancel {
  font-size: 28rpx;
  color: #666;
}

.date-picker-view {
  height: 400rpx;
  margin-bottom: 40rpx;
}

.picker-item {
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 28rpx;
  color: #333;
}

.date-picker-footer {
  display: flex;
  justify-content: center;
}

.date-picker-confirm {
  width: 200rpx;
  height: 80rpx;
  background: #007aff;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.date-picker-confirm text {
  font-size: 28rpx;
  color: #fff;
  font-weight: bold;
}
</style>
