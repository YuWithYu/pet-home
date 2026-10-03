<template>
  <view class="tools-container">
    <!-- 工具切换 -->
    <view class="tab-bar">
      <view
        class="tab-item"
        v-for="tab in tabs"
        :key="tab.key"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- 喂食量计算器 -->
    <view class="tool-card" v-if="activeTab === 'food'">
      <view class="card-title">
        <text>每日喂食量计算</text>
      </view>
      <view class="form-row">
        <text class="form-label">宠物类型</text>
        <view class="seg-group">
          <view class="seg-item" :class="{ on: foodForm.type === 'dog' }" @click="foodForm.type = 'dog'">
            <text>狗狗</text>
          </view>
          <view class="seg-item" :class="{ on: foodForm.type === 'cat' }" @click="foodForm.type = 'cat'">
            <text>猫猫</text>
          </view>
        </view>
      </view>
      <view class="form-row">
        <text class="form-label">体重(kg)</text>
        <input class="form-input" type="digit" v-model="foodForm.weight" placeholder="如 4.5" placeholder-class="input-placeholder" />
      </view>
      <view class="form-row">
        <text class="form-label">生命阶段</text>
        <view class="seg-group">
          <view class="seg-item" :class="{ on: foodForm.stage === 'young' }" @click="foodForm.stage = 'young'">
            <text>幼年</text>
          </view>
          <view class="seg-item" :class="{ on: foodForm.stage === 'adult' }" @click="foodForm.stage = 'adult'">
            <text>成年</text>
          </view>
          <view class="seg-item" :class="{ on: foodForm.stage === 'senior' }" @click="foodForm.stage = 'senior'">
            <text>老年</text>
          </view>
        </view>
      </view>
      <view class="form-row" v-if="foodForm.stage === 'adult'">
        <text class="form-label">是否绝育</text>
        <view class="seg-group">
          <view class="seg-item" :class="{ on: foodForm.neutered }" @click="foodForm.neutered = true">
            <text>已绝育</text>
          </view>
          <view class="seg-item" :class="{ on: !foodForm.neutered }" @click="foodForm.neutered = false">
            <text>未绝育</text>
          </view>
        </view>
      </view>
      <view class="calc-btn" @click="calcFood">
        <text>计算</text>
      </view>
      <view class="result-card" v-if="foodResult">
        <view class="result-line">
          <text class="result-label">每日所需能量</text>
          <text class="result-value">{{ foodResult.kcal }} kcal</text>
        </view>
        <view class="result-line">
          <text class="result-label">干粮（约3.5kcal/g）</text>
          <text class="result-value">{{ foodResult.dryGrams }}g/天</text>
        </view>
        <view class="result-line">
          <text class="result-label">湿粮（约1kcal/g）</text>
          <text class="result-value">{{ foodResult.wetGrams }}g/天</text>
        </view>
        <view class="result-line">
          <text class="result-label">建议分餐</text>
          <text class="result-value">{{ foodResult.meals }}</text>
        </view>
        <view class="result-tip">
          <text>{{ foodResult.tip }}</text>
        </view>
      </view>
    </view>

    <!-- 年龄换算 -->
    <view class="tool-card" v-if="activeTab === 'age'">
      <view class="card-title">
        <text>宠物年龄换算</text>
      </view>
      <view class="form-row">
        <text class="form-label">宠物类型</text>
        <view class="seg-group">
          <view class="seg-item" :class="{ on: ageForm.type === 'dog' }" @click="ageForm.type = 'dog'">
            <text>狗狗</text>
          </view>
          <view class="seg-item" :class="{ on: ageForm.type === 'cat' }" @click="ageForm.type = 'cat'">
            <text>猫猫</text>
          </view>
        </view>
      </view>
      <view class="form-row" v-if="ageForm.type === 'dog'">
        <text class="form-label">体型</text>
        <view class="seg-group">
          <view class="seg-item" :class="{ on: ageForm.size === 'small' }" @click="ageForm.size = 'small'">
            <text>小型</text>
          </view>
          <view class="seg-item" :class="{ on: ageForm.size === 'medium' }" @click="ageForm.size = 'medium'">
            <text>中型</text>
          </view>
          <view class="seg-item" :class="{ on: ageForm.size === 'large' }" @click="ageForm.size = 'large'">
            <text>大型</text>
          </view>
        </view>
      </view>
      <view class="form-row">
        <text class="form-label">年龄</text>
        <input class="form-input short" type="number" v-model="ageForm.years" placeholder="岁" placeholder-class="input-placeholder" />
        <input class="form-input short gap" type="number" v-model="ageForm.months" placeholder="月" placeholder-class="input-placeholder" />
      </view>
      <view class="calc-btn" @click="calcAge">
        <text>换算</text>
      </view>
      <view class="result-card" v-if="ageResult">
        <view class="age-big">
          <text class="age-value">{{ ageResult.humanYears }}</text>
          <text class="age-unit">岁</text>
        </view>
        <view class="age-desc">
          <text>{{ ageResult.desc }}</text>
        </view>
      </view>
    </view>

    <!-- 饮水建议 -->
    <view class="tool-card" v-if="activeTab === 'water'">
      <view class="card-title">
        <text>每日饮水建议</text>
      </view>
      <view class="form-row">
        <text class="form-label">宠物类型</text>
        <view class="seg-group">
          <view class="seg-item" :class="{ on: waterForm.type === 'dog' }" @click="waterForm.type = 'dog'">
            <text>狗狗</text>
          </view>
          <view class="seg-item" :class="{ on: waterForm.type === 'cat' }" @click="waterForm.type = 'cat'">
            <text>猫猫</text>
          </view>
        </view>
      </view>
      <view class="form-row">
        <text class="form-label">体重(kg)</text>
        <input class="form-input" type="digit" v-model="waterForm.weight" placeholder="如 4.5" placeholder-class="input-placeholder" />
      </view>
      <view class="calc-btn" @click="calcWater">
        <text>计算</text>
      </view>
      <view class="result-card" v-if="waterResult">
        <view class="result-line">
          <text class="result-label">每日建议饮水</text>
          <text class="result-value">{{ waterResult.min }} ~ {{ waterResult.max }} ml</text>
        </view>
        <view class="result-tip">
          <text>{{ waterResult.tip }}</text>
        </view>
      </view>
    </view>

    <!-- 免责说明 -->
    <view class="disclaimer">
      <text>以上结果为通用估算值，仅供参考。具体喂食量请结合粮食包装建议、宠物体况与兽医意见调整。</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'PetTools',
  data() {
    return {
      tabs: [
        { key: 'food', label: '喂食计算' },
        { key: 'age', label: '年龄换算' },
        { key: 'water', label: '饮水建议' }
      ],
      activeTab: 'food',
      // 喂食量计算
      foodForm: {
        type: 'dog',
        weight: '',
        stage: 'adult',
        neutered: true
      },
      foodResult: null,
      // 年龄换算
      ageForm: {
        type: 'dog',
        size: 'small',
        years: '',
        months: ''
      },
      ageResult: null,
      // 饮水建议
      waterForm: {
        type: 'dog',
        weight: ''
      },
      waterResult: null
    }
  },

  methods: {
    switchTab(key) {
      this.activeTab = key
    },

    // ==================== 喂食量计算 ====================

    calcFood() {
      const weight = Number(this.foodForm.weight)
      if (!weight || weight <= 0 || weight > 200) {
        uni.showToast({ title: '请输入有效体重（0~200kg）', icon: 'none' })
        return
      }
      const { type, stage, neutered } = this.foodForm

      // 静息能量需求 RER：狗 70×w^0.75，猫 70×w^0.67
      const rer = 70 * Math.pow(weight, type === 'cat' ? 0.67 : 0.75)

      // 阶段/绝育系数
      let factor
      let meals
      if (type === 'cat') {
        if (stage === 'young') {
          factor = 2.5
          meals = '每天 3~4 餐'
        } else if (stage === 'senior') {
          factor = 1.1
          meals = '每天 2~3 餐'
        } else {
          factor = neutered ? 1.2 : 1.4
          meals = '每天 2~3 餐'
        }
      } else {
        if (stage === 'young') {
          factor = 2.0
          meals = '每天 3~4 餐'
        } else if (stage === 'senior') {
          factor = 1.4
          meals = '每天 2 餐'
        } else {
          factor = neutered ? 1.6 : 1.8
          meals = '每天 2 餐'
        }
      }

      const kcal = Math.round(rer * factor)
      this.foodResult = {
        kcal,
        dryGrams: Math.round(kcal / 3.5),
        wetGrams: Math.round(kcal / 1),
        meals,
        tip: '换粮请按 7 天过渡；若喂零食，热量不超过每日总量的 10%。'
      }
    },

    // ==================== 年龄换算 ====================

    calcAge() {
      const years = Math.max(0, parseInt(this.ageForm.years) || 0)
      const months = Math.min(11, Math.max(0, parseInt(this.ageForm.months) || 0))
      if (years === 0 && months === 0) {
        uni.showToast({ title: '请填写宠物年龄', icon: 'none' })
        return
      }
      const { type, size } = this.ageForm

      let humanYears
      if (type === 'cat') {
        // 猫：1岁=15，2岁=24，之后每年+4
        if (years === 0) {
          humanYears = months / 12 * 15
        } else if (years === 1) {
          humanYears = 15 + months / 12 * 9
        } else {
          humanYears = 24 + (years - 2) * 4 + months / 12 * 4
        }
      } else {
        // 狗：1岁=15，2岁=24，之后每年按体型 小型+4/中型+5/大型+6
        const perYear = size === 'small' ? 4 : size === 'medium' ? 5 : 6
        if (years === 0) {
          humanYears = months / 12 * 15
        } else if (years === 1) {
          humanYears = 15 + months / 12 * 9
        } else {
          humanYears = 24 + (years - 2) * perYear + months / 12 * perYear
        }
      }

      humanYears = Math.round(humanYears * 10) / 10
      let desc
      if (humanYears < 18) {
        desc = '正处于青少年期，精力旺盛，注意营养与社交训练。'
      } else if (humanYears < 40) {
        desc = '正值壮年，保持规律运动与均衡饮食。'
      } else if (humanYears < 60) {
        desc = '进入中年，建议关注体重变化，每年做一次体检。'
      } else {
        desc = '步入老年，建议半年一次体检，留意关节与牙齿健康。'
      }
      this.ageResult = { humanYears, desc }
    },

    // ==================== 饮水建议 ====================

    calcWater() {
      const weight = Number(this.waterForm.weight)
      if (!weight || weight <= 0 || weight > 200) {
        uni.showToast({ title: '请输入有效体重（0~200kg）', icon: 'none' })
        return
      }
      // 狗 50~60ml/kg，猫 45~60ml/kg
      const minPerKg = this.waterForm.type === 'cat' ? 45 : 50
      const maxPerKg = 60
      const min = Math.round(weight * minPerKg)
      const max = Math.round(weight * maxPerKg)
      this.waterResult = {
        min,
        max,
        tip: this.waterForm.type === 'cat'
          ? '猫天性饮水少，可用流动饮水机或多放水碗引导；喂湿粮也能有效补水。'
          : '运动后和天气炎热时需水量会增加；保证水碗清洁、水新鲜。'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.tools-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding: 20rpx;
  padding-bottom: 60rpx;
}

/* 顶部切换 */
.tab-bar {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 8rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 12rpx;
  font-size: 26rpx;
  color: #666;
}

.tab-item.active {
  background: #ff6b35;
  color: #fff;
  font-weight: 500;
}

/* 工具卡片 */
.tool-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
}

.card-title {
  margin-bottom: 24rpx;
}

.card-title text {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

/* 表单 */
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

.form-input.short {
  flex: 0 0 160rpx;
  text-align: center;
}

.form-input.gap {
  margin-left: 20rpx;
}

.input-placeholder {
  color: #bbb;
}

/* 分段选择 */
.seg-group {
  flex: 1;
  display: flex;
  background: #f8f8f8;
  border-radius: 12rpx;
  padding: 6rpx;
}

.seg-item {
  flex: 1;
  text-align: center;
  padding: 12rpx 0;
  border-radius: 10rpx;
  font-size: 24rpx;
  color: #666;
}

.seg-item.on {
  background: #fff;
  color: #ff6b35;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
}

/* 计算按钮 */
.calc-btn {
  background: #ff6b35;
  color: #fff;
  text-align: center;
  padding: 20rpx 0;
  border-radius: 44rpx;
  font-size: 28rpx;
  font-weight: 500;
  margin-top: 8rpx;
}

.calc-btn:active {
  background: #f05a25;
}

/* 结果 */
.result-card {
  margin-top: 24rpx;
  background: #fff8f4;
  border-radius: 12rpx;
  padding: 24rpx;
}

.result-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.result-line:last-of-type {
  margin-bottom: 0;
}

.result-label {
  font-size: 24rpx;
  color: #666;
}

.result-value {
  font-size: 28rpx;
  color: #ff6b35;
  font-weight: bold;
}

.result-tip {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx dashed #f0d5c5;
}

.result-tip text {
  font-size: 22rpx;
  color: #999;
  line-height: 1.6;
}

/* 年龄换算结果 */
.age-big {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8rpx;
}

.age-value {
  font-size: 80rpx;
  font-weight: bold;
  color: #ff6b35;
}

.age-unit {
  font-size: 28rpx;
  color: #ff6b35;
}

.age-desc {
  margin-top: 16rpx;
  text-align: center;
}

.age-desc text {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
}

/* 免责说明 */
.disclaimer {
  padding: 10rpx 20rpx;
}

.disclaimer text {
  font-size: 20rpx;
  color: #bbb;
  line-height: 1.6;
}
</style>
