<template>
  <view class="select-weight-container">
    <!-- 页面内容（使用默认导航栏） -->
    <view class="select-weight-content">
      <!-- 主要内容容器（居中） -->
      <view class="main-content-container">
        <!-- 体重显示区域 -->
        <view class="weight-display-container">
          <view class="weight-indicator">
            <view class="weight-box">
              <text class="weight-value">{{ selectedWeight.toFixed(2) }} kg</text>
            </view>
            <view class="weight-triangle"></view>
            <view class="weight-line"></view>
          </view>
        </view>

        <!-- 体重选择刻度尺 -->
        <view class="weight-scale-container">
          <view class="weight-scale-wrapper">
            <view class="weight-scale" @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd">
              <!-- 刻度标记 -->
              <view 
                class="scale-mark" 
                v-for="(mark, index) in scaleMarks" 
                :key="index"
                :style="{ left: mark.position + 'rpx' }"
                @click="selectWeight(mark.value)"
              >
                <view class="mark-line" :class="{ 'main-mark': mark.isMain }"></view>
                <text class="mark-text" v-if="mark.showText">{{ mark.value }}</text>
              </view>
              
              <!-- 可拖动的光标 -->
              <view 
                class="weight-cursor" 
                :style="{ left: weightCursorPosition + 'rpx' }"
              ></view>
            </view>
          </view>
        </view>

        <!-- 提示文字 -->
        <view class="tip-text" @click="showInputModal">
          <text>点击数字可直接输入哦~</text>
        </view>
      </view>

      <!-- 完成按钮 -->
      <view class="complete-button-section">
        <view class="complete-button" @click="complete">
          <text>完成</text>
        </view>
      </view>
    </view>

    <!-- 数字输入模态框 -->
    <view class="input-modal" v-if="showInput" @click="hideInputModal">
      <view class="input-content" @click.stop>
        <view class="input-header">
          <text class="input-title">输入体重</text>
          <text class="input-cancel" @click="hideInputModal">取消</text>
        </view>
        <view class="input-body">
          <input 
            class="weight-input" 
            type="number" 
            :value="inputWeight" 
            @input="onInputChange"
            placeholder="请输入体重"
            focus
          />
          <text class="input-unit">kg</text>
        </view>
        <view class="input-footer">
          <view class="input-confirm" @click="confirmInput">
            <text>确定</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'SelectWeight',
  data() {
    return {
      selectedWeight: 0.32,
      scaleMarks: [],
      scaleWidth: 1000, // 刻度尺宽度(rpx)
      startX: 0,
      isDragging: false,
      showInput: false,
      inputWeight: '0.32'
    }
  },
  
  computed: {
    weightCursorPosition() {
      // 根据体重值计算光标位置 (0-100kg 对应 0-600rpx)
      return (this.selectedWeight / 100) * this.scaleWidth
    }
  },
  
  onLoad() {
    // 初始化刻度尺
    this.initScaleMarks()
  },
  
  methods: {
    // 初始化刻度尺标记
    initScaleMarks() {
      this.updateScaleMarks()
    },
    
    // 更新刻度尺标记（只显示当前值附近的刻度）
    updateScaleMarks() {
      this.scaleMarks = []
      const currentValue = this.selectedWeight
      const startValue = Math.max(0, Math.floor(currentValue) - 1)
      const endValue = Math.min(100, Math.ceil(currentValue) + 2)
      
      // 生成当前范围内的刻度
      for (let value = startValue; value <= endValue; value += 0.1) {
        const normalizedValue = Math.round(value * 10) / 10
        const position = ((normalizedValue - startValue) / (endValue - startValue)) * this.scaleWidth
        
        this.scaleMarks.push({
          value: normalizedValue,
          position: position,
          isMain: normalizedValue % 1 === 0, // 整数kg为主标记
          showText: normalizedValue % 1 === 0 // 只显示整数kg的数字
        })
      }
    },
    
    // 返回上一页
    // 选择体重
    selectWeight(weight) {
      this.selectedWeight = weight
      this.updateScaleMarks()
    },
    
    // 触摸开始
    onTouchStart(e) {
      this.startX = e.touches[0].clientX
      this.isDragging = true
    },
    
    // 触摸移动
    onTouchMove(e) {
      if (!this.isDragging) return
      
      const currentX = e.touches[0].clientX
      const deltaX = currentX - this.startX
      
      // 计算当前显示范围
      const currentValue = this.selectedWeight
      const startValue = Math.max(0, Math.floor(currentValue) - 1)
      const endValue = Math.min(100, Math.ceil(currentValue) + 2)
      const range = endValue - startValue
      
      // 计算新的体重值 - 向左拖动增加，向右拖动减少
      const sensitivity = 2.0 // 增加拖动敏感度
      const newWeight = this.selectedWeight - (deltaX / this.scaleWidth) * range * sensitivity
      
      // 限制在0-100kg范围内
      if (newWeight >= 0 && newWeight <= 100) {
        this.selectedWeight = Math.round(newWeight * 10) / 10 // 保留一位小数
        this.updateScaleMarks() // 更新刻度尺
      }
      
      this.startX = currentX
    },
    
    // 触摸结束
    onTouchEnd() {
      this.isDragging = false
    },
    
    // 显示输入模态框
    showInputModal() {
      this.inputWeight = this.selectedWeight.toString()
      this.showInput = true
    },
    
    // 隐藏输入模态框
    hideInputModal() {
      this.showInput = false
    },
    
    // 输入变化
    onInputChange(e) {
      this.inputWeight = e.detail.value
    },
    
    // 确认输入
    confirmInput() {
      const weight = parseFloat(this.inputWeight)
      if (weight >= 0 && weight <= 100) {
        this.selectedWeight = weight
        this.updateScaleMarks() // 更新刻度尺
        this.showInput = false
      } else {
        uni.showToast({
          title: '请输入0-100之间的数字',
          icon: 'none'
        })
      }
    },
    
    // 完成
    async complete() {
      // 保存体重信息到本地存储
      const basicInfo = uni.getStorageSync('petBasicInfo') || {}
      basicInfo.weight = this.selectedWeight
      uni.setStorageSync('petBasicInfo', basicInfo)
      
      // 获取当前用户ID
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.id || userInfo.uid || uni.getStorageSync('userId')
      
      // 显示加载提示
      uni.showLoading({
        title: '保存中...',
        mask: true
      })
      
      try {
        // 先上传头像（若为本地临时路径），再创建宠物
        const avatarUrl = await this.uploadAvatarIfNeeded(basicInfo.avatar)
        
        // 构建要发送给后端的数据（只包含后端支持的字段）
        const petData = {
          userId: userId ? parseInt(userId) : null,
          name: basicInfo.name || '未命名',
          species: basicInfo.type || 'cat',  // 前端用type，后端用species
          breed: basicInfo.breed || '未知品种',
          gender: basicInfo.gender || 'male',  // 性别：male/female
          age: this.calculateAge(basicInfo.birthDate),
          weight: this.selectedWeight ? parseFloat(this.selectedWeight) : null,
          birthday: basicInfo.birthDate || null,  // 生日
          arrivalDate: basicInfo.arrivalDate || null,  // 到家日期
          sterilization: basicInfo.sterilization || 'unknown',  // 绝育状态
          dating: basicInfo.dating || 'no',  // 相亲意愿
          description: '爱吃啥,有啥特点~>',
          avatar: avatarUrl,
          status: 'active'
        }
        
        // 调用后端API保存到数据库
        const res = await api.createPet(petData)
        
        if ((res.code === 0 || res.code === 200) && res.data) {
          // 后端保存成功，使用后端返回的数据（包含数据库生成的ID）
          const petProfile = {
            id: res.data.id,  // 使用后端返回的ID
            avatar: res.data.avatar || basicInfo.avatar || '/static/images/garfield-default-avatar.png',
            name: res.data.name || basicInfo.name || '未命名',
            type: res.data.species || basicInfo.type || 'cat',
            breed: res.data.breed || basicInfo.breed || '未知品种',
            gender: res.data.gender || basicInfo.gender || 'male',
            sterilization: basicInfo.sterilization || 'unknown',
            dating: 'no',
            birthDate: res.data.birthday || basicInfo.birthDate || '',
            arrivalDate: basicInfo.arrivalDate || '',
            weight: res.data.weight || this.selectedWeight,
            description: res.data.description || '爱吃啥,有啥特点~>',
            age: res.data.age || this.calculateAge(basicInfo.birthDate),
            companionshipDays: this.calculateCompanionshipDays(basicInfo.arrivalDate),
            totalFood: 0
          }
          
          // 保存到本地存储（用于前端显示）
          const petList = uni.getStorageSync('petList') || []
          petList.push(petProfile)
          uni.setStorageSync('petList', petList)
          
          // 清除临时数据
          uni.removeStorageSync('petBasicInfo')
          
          uni.hideLoading()
          
          // 显示完成提示
          uni.showToast({
            title: '宠物档案创建完成！',
            icon: 'success',
            duration: 1500
          })
          
          // 延迟跳转回我的宠物页面
          setTimeout(() => {
            // 获取页面栈，找到"我的宠物"页面的位置
            const pages = getCurrentPages()
            const myPetsPageIndex = pages.findIndex(page => {
              const route = page.route || page.__route__ || ''
              return route.includes('my-pets') || route === 'pages/user/my-pets'
            })
            
            if (myPetsPageIndex >= 0 && myPetsPageIndex < pages.length - 1) {
              // 如果"我的宠物"页面在栈中且不是当前页面，返回到该页面
              const delta = pages.length - myPetsPageIndex - 1
              uni.navigateBack({
                delta: delta
              })
            } else {
              // 如果不在栈中，使用 redirectTo 跳转（保留之前的页面栈，只替换当前页面）
              uni.redirectTo({
                url: '/user/my-pets'
              })
            }
          }, 1000)
        } else {
          throw new Error(res.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存宠物失败:', error)
        uni.hideLoading()
        uni.showToast({
          title: '保存失败: ' + (error.message || '未知错误'),
          icon: 'none',
          duration: 3000
        })
      }
    },
    
    // 若头像为本地临时路径则上传到服务器，返回可用的URL
    uploadAvatarIfNeeded(avatarPath) {
      const baseUrl = util.getApiBaseUrl()
      const uploadUrl = baseUrl.replace(/\/+$/, '') + '/api/upload/image'
      // 已是服务器URL则不重复上传
      if (avatarPath && (avatarPath.startsWith('http://') || avatarPath.startsWith('https://'))) {
        return Promise.resolve(avatarPath)
      }
      if (!avatarPath || avatarPath.trim() === '') {
        return Promise.resolve('/static/images/login-dog.png')
      }
      const token = uni.getStorageSync('token')
      return new Promise((resolve, reject) => {
        uni.uploadFile({
          url: uploadUrl,
          filePath: avatarPath,
          name: 'file',
          formData: { type: 'pet' },
          header: { 'Authorization': token ? `Bearer ${token}` : '' },
          success: (res) => {
            try {
              const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
              if ((data.code === 0 || data.code === 200) && data.data) {
                const imageUrl = typeof data.data === 'string' ? data.data : (data.data.url || '')
                resolve(imageUrl)
              } else {
                reject(new Error(data.msg || '头像上传失败'))
              }
            } catch (e) {
              reject(e)
            }
          },
          fail: (err) => reject(err)
        })
      })
    },

    // 计算年龄
    calculateAge(birthDate) {
      if (!birthDate) return 0
      const birth = new Date(birthDate)
      const now = new Date()
      const ageInYears = now.getFullYear() - birth.getFullYear()
      return Math.max(0, ageInYears)
    },
    
    // 计算陪伴天数
    calculateCompanionshipDays(arrivalDate) {
      if (!arrivalDate) return 0
      const arrival = new Date(arrivalDate)
      const now = new Date()
      const diffTime = Math.abs(now - arrival)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      return diffDays
    },
    
    // 生成在Integer范围内的宠物ID
    generatePetId() {
      // 生成一个在Integer范围内的随机ID (1 到 2147483647)
      const maxInt = 2147483647
      const minInt = 1
      return Math.floor(Math.random() * (maxInt - minInt + 1)) + minInt
    }
  }
}
</script>

<style lang="scss" scoped>
.select-weight-container {
  min-height: 100vh;
  background-color: #fff;
}

/* 页面内容 */
.select-weight-content {
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
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

/* 体重显示区域 */
.weight-display-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 60rpx;
}

.weight-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.weight-box {
  background: #d4a574;
  border-radius: 16rpx;
  padding: 20rpx 40rpx;
  margin-bottom: 8rpx;
}

.weight-value {
  font-size: 32rpx;
  color: #fff;
  font-weight: bold;
}

.weight-triangle {
  width: 0;
  height: 0;
  border-left: 12rpx solid transparent;
  border-right: 12rpx solid transparent;
  border-top: 16rpx solid #d4a574;
  margin: 0 auto;
}

.weight-line {
  width: 2rpx;
  height: 40rpx;
  background: #d4a574;
  margin: 0 auto;
}

/* 体重选择刻度尺 */
.weight-scale-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  margin-bottom: 40rpx;
}

.weight-scale-wrapper {
  width: 100%;
  max-width: 1200rpx;
  display: flex;
  justify-content: center;
  padding: 0 20rpx;
}

.weight-scale {
  position: relative;
  width: 1000rpx;
  height: 120rpx;
  background: #fff;
  border: 2rpx solid #e0e0e0;
  border-radius: 60rpx;
  margin: 0 auto;
}

.scale-mark {
  position: absolute;
  top: 0;
  transform: translateX(-50%);
  cursor: pointer;
}

.mark-line {
  width: 2rpx;
  height: 20rpx;
  background: #ccc;
  margin: 0 auto;
}

.mark-line.main-mark {
  height: 40rpx;
  background: #999;
  width: 3rpx;
}

.mark-text {
  display: block;
  font-size: 28rpx;
  color: #333;
  text-align: center;
  margin-top: 12rpx;
  font-weight: 500;
}

.weight-cursor {
  position: absolute;
  top: -8rpx;
  width: 4rpx;
  height: 136rpx;
  background: #d4a574;
  border-radius: 2rpx;
  transform: translateX(-50%);
  transition: left 0.1s ease;
}

/* 提示文字 */
.tip-text {
  text-align: center;
  margin-bottom: 40rpx;
  cursor: pointer;
}

.tip-text text {
  font-size: 24rpx;
  color: #999;
}

/* 完成按钮 */
.complete-button-section {
  position: fixed;
  bottom: 40rpx;
  left: 30rpx;
  right: 30rpx;
}

.complete-button {
  width: 100%;
  height: 88rpx;
  background: #ffd700;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.complete-button text {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.complete-button:active {
  transform: scale(0.98);
  background: #e6c200;
}

/* 输入模态框 */
.input-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-content {
  width: 600rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
}

.input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.input-title {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}

.input-cancel {
  font-size: 28rpx;
  color: #666;
}

.input-body {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 40rpx;
}

.weight-input {
  width: 300rpx;
  height: 80rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 32rpx;
  text-align: center;
  margin-right: 20rpx;
}

.input-unit {
  font-size: 28rpx;
  color: #666;
}

.input-footer {
  display: flex;
  justify-content: center;
}

.input-confirm {
  width: 200rpx;
  height: 80rpx;
  background: #007aff;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-confirm text {
  font-size: 28rpx;
  color: #fff;
  font-weight: bold;
}
</style>
