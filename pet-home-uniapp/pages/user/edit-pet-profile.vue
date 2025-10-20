<template>
  <view class="edit-pet-profile">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text class="title-text">编辑宠物资料</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 宠物头像和昵称 -->
      <view class="pet-avatar-section">
        <view class="avatar-container">
          <image 
            :src="petData.avatar || '/static/images/garfield-default-avatar.png'" 
            class="pet-avatar"
            mode="aspectFill"
          />
          <view class="camera-btn" @click="chooseAvatar">
            <text class="camera-icon">📷</text>
          </view>
        </view>
        <view class="nickname-input">
          <input 
            v-model="petData.name" 
            placeholder="请输入昵称" 
            class="name-input"
            maxlength="10"
          />
          <view class="next-btn" @click="nextStep">
            <text class="next-text">下一步</text>
          </view>
        </view>
      </view>

      <!-- 宠物信息表单 -->
      <view class="pet-form">
        <!-- 宠物名字 -->
        <view class="form-item">
          <text class="item-label">宠物名字</text>
          <view class="item-content">
            <input 
              v-model="petData.name" 
              placeholder="请输入宠物名字" 
              class="item-input"
              maxlength="10"
            />
            <text class="item-arrow">1></text>
          </view>
        </view>

        <!-- 宠物品种 -->
        <view class="form-item" @click="selectBreed">
          <text class="item-label">宠物品种</text>
          <view class="item-content">
            <text class="item-value">{{ petData.breed || '请选择品种' }}</text>
            <text class="item-arrow">></text>
          </view>
        </view>

        <!-- 宠物性别 -->
        <view class="form-item">
          <text class="item-label">宠物性别</text>
          <view class="gender-options">
            <view 
              class="gender-btn" 
              :class="{ active: petData.gender === 'male' }"
              @click="selectGender('male')"
            >
              <text class="gender-text">男孩子</text>
            </view>
            <view 
              class="gender-btn" 
              :class="{ active: petData.gender === 'female' }"
              @click="selectGender('female')"
            >
              <text class="gender-text">女孩子</text>
            </view>
          </view>
        </view>

        <!-- 绝育状态 -->
        <view class="form-item">
          <text class="item-label">绝育状态</text>
          <view class="sterilization-options">
            <view 
              class="sterilization-btn" 
              :class="{ active: petData.sterilization === 'no' }"
              @click="selectSterilization('no')"
            >
              <text class="sterilization-text">未绝育</text>
            </view>
            <view 
              class="sterilization-btn" 
              :class="{ active: petData.sterilization === 'yes' }"
              @click="selectSterilization('yes')"
            >
              <text class="sterilization-text">已绝育</text>
            </view>
            <view 
              class="sterilization-btn" 
              :class="{ active: petData.sterilization === 'unknown' }"
              @click="selectSterilization('unknown')"
            >
              <text class="sterilization-text">不清楚</text>
            </view>
          </view>
        </view>

        <!-- 相亲意愿 -->
        <view class="form-item">
          <text class="item-label">相亲意愿</text>
          <view class="dating-options">
            <view 
              class="dating-btn" 
              :class="{ active: petData.dating === 'no' }"
              @click="selectDating('no')"
            >
              <text class="dating-text">没有</text>
            </view>
            <view 
              class="dating-btn" 
              :class="{ active: petData.dating === 'yes' }"
              @click="selectDating('yes')"
            >
              <text class="dating-text">有</text>
            </view>
          </view>
        </view>

        <!-- 宠物体重 -->
        <view class="form-item" @click="editWeight">
          <text class="item-label">宠物体重</text>
          <view class="item-content">
            <text class="item-value">{{ petData.weight }} kg</text>
            <text class="item-arrow">></text>
          </view>
        </view>

        <!-- 出生日期 -->
        <view class="form-item" @click="selectBirthDate">
          <text class="item-label">出生日期</text>
          <view class="item-content">
            <text class="item-value">{{ petData.birthDate || '请选择日期' }}</text>
            <text class="item-arrow">></text>
          </view>
        </view>

        <!-- 到家日期 -->
        <view class="form-item" @click="selectArrivalDate">
          <text class="item-label">到家日期</text>
          <view class="item-content">
            <text class="item-value">{{ petData.arrivalDate || '请选择日期' }}</text>
            <text class="item-arrow">></text>
          </view>
        </view>

        <!-- 一句话描述 -->
        <view class="form-item" @click="editDescription">
          <text class="item-label">一句话描述</text>
          <view class="item-content">
            <text class="item-value">{{ petData.description || '爱吃啥,有啥特点~>' }}</text>
            <text class="item-arrow">></text>
          </view>
        </view>
      </view>

      <!-- 提示信息 -->
      <view class="tip-text">
        <text class="tip">出生日期/到家日期不记得可以估算一个值,后续可以修改哦~</text>
      </view>

      <!-- 保存按钮 -->
      <view class="save-btn" @click="saveEdit">
        <text class="save-text">保存编辑</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      petData: {
        id: '',
        name: '',
        avatar: '',
        breed: '',
        gender: 'male',
        sterilization: 'no',
        dating: 'no',
        weight: '8.62',
        birthDate: '2025-10-19',
        arrivalDate: '2025-10-19',
        description: '爱吃啥,有啥特点~>'
      }
    }
  },
  
  onLoad(options) {
    // 获取传入的宠物ID
    if (options.petId) {
      this.loadPetData(options.petId)
    }
  },
  
  onShow() {
    // 页面显示时检查是否有从其他页面返回的数据
    this.checkForUpdates()
  },
  
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 加载宠物数据
    loadPetData(petId) {
      try {
        const petList = uni.getStorageSync('petList') || []
        // 将petId转换为数字类型进行匹配
        const numericPetId = parseInt(petId)
        const pet = petList.find(p => p.id === numericPetId)
        if (pet) {
          this.petData = { 
            ...pet,
            // 确保所有字段都有默认值
            dating: pet.dating || 'no',
            description: pet.description || '爱吃啥,有啥特点~>'
          }
          console.log('成功加载宠物数据:', this.petData)
        } else {
          console.error('未找到宠物数据, petId:', petId, 'petList:', petList)
          uni.showToast({
            title: '宠物数据不存在',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载宠物数据失败:', error)
        uni.showToast({
          title: '加载宠物数据失败',
          icon: 'none'
        })
      }
    },
    
    // 选择头像
    chooseAvatar() {
      uni.showActionSheet({
        itemList: ['从相册选择', '拍照'],
        success: (res) => {
          const sourceType = res.tapIndex === 0 ? ['album'] : ['camera']
          uni.chooseImage({
            count: 1,
            sizeType: ['compressed'],
            sourceType: sourceType,
            success: (res) => {
              this.petData.avatar = res.tempFilePaths[0]
            }
          })
        }
      })
    },
    
    // 下一步
    nextStep() {
      if (!this.petData.name.trim()) {
        uni.showToast({
          title: '请输入宠物名字',
          icon: 'none'
        })
        return
      }
      // 这里可以添加下一步的逻辑
    },
    
    // 选择品种
    selectBreed() {
      uni.navigateTo({
        url: '/pages/user/select-breed-list?petType=' + (this.petData.petType || 'cat')
      })
    },
    
    // 处理品种选择结果
    handleSelectedBreed(breedName) {
      this.petData.breed = breedName
    },
    
    // 检查页面更新
    checkForUpdates() {
      // 检查是否有从品种选择页面返回的数据
      const pages = getCurrentPages()
      if (pages.length > 1) {
        const prevPage = pages[pages.length - 2]
        if (prevPage && prevPage.$vm && prevPage.$vm.selectedBreed) {
          this.petData.breed = prevPage.$vm.selectedBreed
          prevPage.$vm.selectedBreed = null // 清除临时数据
        }
      }
    },
    
    // 选择性别
    selectGender(gender) {
      this.petData.gender = gender
    },
    
    // 选择绝育状态
    selectSterilization(status) {
      this.petData.sterilization = status
    },
    
    // 选择相亲意愿
    selectDating(dating) {
      this.petData.dating = dating
    },
    
    // 编辑体重
    editWeight() {
      uni.showModal({
        title: '编辑体重',
        editable: true,
        placeholderText: '请输入体重(kg)',
        content: this.petData.weight,
        success: (res) => {
          if (res.confirm && res.content) {
            const weight = parseFloat(res.content)
            if (!isNaN(weight) && weight >= 0 && weight <= 100) {
              this.petData.weight = weight.toFixed(2)
            } else {
              uni.showToast({
                title: '请输入0-100之间的数字',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    // 选择出生日期
    selectBirthDate() {
      const currentDate = new Date()
      const maxDate = currentDate.toISOString().split('T')[0]
      const minDate = new Date(currentDate.getFullYear() - 20, 0, 1).toISOString().split('T')[0]
      
      uni.showModal({
        title: '选择出生日期',
        content: '请选择宠物的出生日期',
        showCancel: true,
        cancelText: '取消',
        confirmText: '选择',
        success: (res) => {
          if (res.confirm) {
            // 这里可以调用日期选择器
            this.showDatePicker('birthDate')
          }
        }
      })
    },
    
    // 选择到家日期
    selectArrivalDate() {
      const currentDate = new Date()
      const maxDate = currentDate.toISOString().split('T')[0]
      const minDate = new Date(currentDate.getFullYear() - 10, 0, 1).toISOString().split('T')[0]
      
      uni.showModal({
        title: '选择到家日期',
        content: '请选择宠物的到家日期',
        showCancel: true,
        cancelText: '取消',
        confirmText: '选择',
        success: (res) => {
          if (res.confirm) {
            // 这里可以调用日期选择器
            this.showDatePicker('arrivalDate')
          }
        }
      })
    },
    
    // 显示日期选择器
    showDatePicker(type) {
      // 简化版日期选择，实际项目中可以使用更复杂的日期选择器
      const currentDate = new Date()
      const year = currentDate.getFullYear()
      const month = String(currentDate.getMonth() + 1).padStart(2, '0')
      const day = String(currentDate.getDate()).padStart(2, '0')
      const defaultDate = `${year}-${month}-${day}`
      
      uni.showModal({
        title: '输入日期',
        editable: true,
        placeholderText: 'YYYY-MM-DD',
        content: this.petData[type] || defaultDate,
        success: (res) => {
          if (res.confirm && res.content) {
            // 简单的日期格式验证
            const dateRegex = /^\d{4}-\d{2}-\d{2}$/
            if (dateRegex.test(res.content)) {
              this.petData[type] = res.content
            } else {
              uni.showToast({
                title: '请输入正确的日期格式(YYYY-MM-DD)',
                icon: 'none'
              })
            }
          }
        }
      })
    },
    
    // 编辑描述
    editDescription() {
      uni.showModal({
        title: '编辑描述',
        editable: true,
        placeholderText: '请输入一句话描述',
        content: this.petData.description,
        success: (res) => {
          if (res.confirm && res.content) {
            this.petData.description = res.content
          }
        }
      })
    },
    
    // 保存编辑
    saveEdit() {
      if (!this.petData.name.trim()) {
        uni.showToast({
          title: '请输入宠物名字',
          icon: 'none'
        })
        return
      }
      
      try {
        const petList = uni.getStorageSync('petList') || []
        // 确保ID类型匹配
        const petId = parseInt(this.petData.id)
        const index = petList.findIndex(p => p.id === petId)
        
        if (index !== -1) {
          // 更新宠物数据，保持原有的一些计算字段
          const updatedPet = {
            ...petList[index],
            ...this.petData,
            id: petId, // 确保ID是数字类型
            // 重新计算年龄和陪伴天数
            age: this.calculateAge(this.petData.birthDate),
            companionshipDays: this.calculateCompanionshipDays(this.petData.arrivalDate)
          }
          
          petList[index] = updatedPet
          uni.setStorageSync('petList', petList)
          
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          console.error('未找到要更新的宠物, petId:', petId, 'petList:', petList)
          uni.showToast({
            title: '宠物数据不存在',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('保存失败:', error)
        uni.showToast({
          title: '保存失败',
          icon: 'none'
        })
      }
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
    }
  }
}
</script>

<style scoped>
.edit-pet-profile {
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 自定义导航栏 */
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: #fff;
  border-bottom: 1px solid #eee;
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 30rpx;
  padding-top: var(--status-bar-height);
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
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 400;
}

.navbar-right {
  width: 60rpx;
}

/* 页面内容 */
.page-content {
  padding-top: calc(var(--status-bar-height) + 88rpx);
  padding-bottom: 120rpx;
}

/* 宠物头像和昵称 */
.pet-avatar-section {
  background-color: #fff;
  padding: 40rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
}

.avatar-container {
  position: relative;
  margin-right: 30rpx;
}

.pet-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background-color: #f0f0f0;
}

.camera-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  background-color: #ffd700;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
}

.camera-icon {
  font-size: 20rpx;
}

.nickname-input {
  flex: 1;
  display: flex;
  align-items: center;
}

.name-input {
  flex: 1;
  height: 60rpx;
  padding: 0 20rpx;
  background-color: #f8f8f8;
  border-radius: 30rpx;
  font-size: 28rpx;
  color: #333;
  margin-right: 20rpx;
}

.next-btn {
  width: 120rpx;
  height: 60rpx;
  background-color: #ffd700;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.next-text {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
}

/* 宠物信息表单 */
.pet-form {
  background-color: #fff;
  margin-bottom: 20rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1px solid #f0f0f0;
}

.form-item:last-child {
  border-bottom: none;
}

.item-label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.item-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.item-value {
  font-size: 28rpx;
  color: #333;
}

.item-arrow {
  font-size: 24rpx;
  color: #999;
  margin-left: 20rpx;
}

/* 性别选择 */
.gender-options {
  display: flex;
  gap: 20rpx;
}

.gender-btn {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  background-color: #f0f0f0;
  border: 1px solid #e0e0e0;
}

.gender-btn.active {
  background-color: #ffd700;
  border-color: #ffd700;
}

.gender-text {
  font-size: 24rpx;
  color: #666;
}

.gender-btn.active .gender-text {
  color: #333;
}

/* 绝育状态选择 */
.sterilization-options {
  display: flex;
  gap: 15rpx;
}

.sterilization-btn {
  padding: 12rpx 20rpx;
  border-radius: 20rpx;
  background-color: #f0f0f0;
  border: 1px solid #e0e0e0;
}

.sterilization-btn.active {
  background-color: #ffd700;
  border-color: #ffd700;
}

.sterilization-text {
  font-size: 22rpx;
  color: #666;
}

.sterilization-btn.active .sterilization-text {
  color: #333;
}

/* 相亲意愿选择 */
.dating-options {
  display: flex;
  gap: 20rpx;
}

.dating-btn {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  background-color: #f0f0f0;
  border: 1px solid #e0e0e0;
}

.dating-btn.active {
  background-color: #ffd700;
  border-color: #ffd700;
}

.dating-text {
  font-size: 24rpx;
  color: #666;
}

.dating-btn.active .dating-text {
  color: #333;
}

/* 提示信息 */
.tip-text {
  padding: 20rpx 30rpx;
}

.tip {
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

/* 保存按钮 */
.save-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background-color: #ffd700;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.save-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
}
</style>
