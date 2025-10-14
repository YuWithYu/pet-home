<template>
  <view class="boarding-container">
    <view class="service-header">
      <view class="service-icon">🏠</view>
      <view class="service-title">宠物寄养预约</view>
      <view class="service-desc">专业宠物寄养服务，让您的宠物得到最好的照顾</view>
    </view>

    <view class="form-container">
      <u-form :model="form" ref="form">
        <!-- 宠物选择 -->
        <u-form-item label="选择宠物" prop="petId" required>
          <u-select v-model="form.petId" :list="petOptions" placeholder="请选择宠物"></u-select>
        </u-form-item>

        <!-- 寄养时间 -->
        <u-form-item label="寄养时间" prop="startDate" required>
          <u-calendar v-model="form.startDate" :show="showCalendar" @confirm="onDateConfirm" placeholder="选择寄养开始日期"></u-calendar>
        </u-form-item>

        <!-- 寄养天数 -->
        <u-form-item label="寄养天数" prop="days" required>
          <u-number-box v-model="form.days" :min="1" :max="30"></u-number-box>
        </u-form-item>

        <!-- 服务类型 -->
        <u-form-item label="服务类型" prop="serviceType" required>
          <u-radio-group v-model="form.serviceType">
            <u-radio value="basic" label="基础寄养"></u-radio>
            <u-radio value="premium" label="豪华寄养"></u-radio>
            <u-radio value="medical" label="医疗寄养"></u-radio>
          </u-radio-group>
        </u-form-item>

        <!-- 特殊需求 -->
        <u-form-item label="特殊需求" prop="requirements">
          <u-input v-model="form.requirements" type="textarea" placeholder="请输入特殊护理需求" :height="120"></u-input>
        </u-form-item>

        <!-- 联系电话 -->
        <u-form-item label="联系电话" prop="phone" required>
          <u-input v-model="form.phone" placeholder="请输入联系电话"></u-input>
        </u-form-item>

        <!-- 提交按钮 -->
        <view class="submit-section">
          <u-button type="primary" @click="submitForm" :loading="submitting" shape="circle">
            {{ submitting ? '提交中...' : '立即预约' }}
          </u-button>
        </view>
      </u-form>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Boarding',

  data() {
    return {
      showCalendar: false,
      submitting: false,
      form: {
        petId: '',
        startDate: '',
        days: 1,
        serviceType: 'basic',
        requirements: '',
        phone: ''
      },
      petOptions: [
        { value: '1', label: '宠物1' },
        { value: '2', label: '宠物2' }
      ]
    }
  },

  methods: {
    onDateConfirm(date) {
      this.form.startDate = date
      this.showCalendar = false
    },

    submitForm() {
      this.$refs.form.validate().then(() => {
        this.submitting = true
        // 这里调用预约API
        setTimeout(() => {
          this.submitting = false
          uni.showToast({
            title: '预约成功',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }, 2000)
      }).catch(() => {
        uni.showToast({
          title: '请完善信息',
          icon: 'none'
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.boarding-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.service-header {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c42 100%);
  padding: 60rpx 40rpx;
  text-align: center;
  color: white;

  .service-icon {
    font-size: 80rpx;
    margin-bottom: 20rpx;
  }

  .service-title {
    font-size: 48rpx;
    font-weight: bold;
    margin-bottom: 16rpx;
  }

  .service-desc {
    font-size: 28rpx;
    opacity: 0.9;
  }
}

.form-container {
  padding: 40rpx;
  background-color: white;
  margin: 20rpx;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.1);
}

.submit-section {
  margin-top: 60rpx;
  padding: 0 40rpx;
}
</style>
