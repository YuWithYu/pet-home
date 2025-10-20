<template>
  <view class="my-pets-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>我的宠物</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="pets-content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <!-- 宠物信息卡片（有宠物时显示） -->
      <view class="pet-info-card" v-for="(pet, index) in petList" :key="pet.id" v-if="hasPets">
        <view class="pet-number">{{ index + 1 }}</view>
        <view class="pet-status-tag">
          <text class="status-icon">{{ pet.gender === 'male' ? '♂' : '♀' }}</text>
          <text class="status-text">{{ getSterilizationText(pet.sterilization) }}</text>
        </view>
        
        <view class="pet-details">
          <view class="detail-item">
            <text class="detail-icon">⚖</text>
            <text class="detail-text">{{ pet.weight }}kg</text>
          </view>
          <view class="detail-item">
            <text class="detail-icon">🎂</text>
            <text class="detail-text">{{ pet.birthDate }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-text">{{ pet.age }}岁|{{ pet.companionshipDays }}天陪伴</text>
          </view>
          <view class="divider"></view>
          <view class="detail-item">
            <text class="detail-text">累计获食: {{ pet.totalFood }}g</text>
          </view>
        </view>
        
        <view class="pet-avatar">
          <image :src="pet.avatar" mode="aspectFill" />
        </view>
        
        <view class="edit-button" @click="editPetProfile(pet)">
          <text>编辑资料 ></text>
        </view>
      </view>

      <!-- 添加宠物档案按钮（始终显示，居中） -->
      <view class="add-pet-container">
        <view class="add-pet-card" @click="addPetProfile">
          <view class="add-icon">
            <text>+</text>
          </view>
          <view class="add-text">
            <text>添加宠物档案</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'MyPets',
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      hasPets: false, // 控制是否显示宠物信息卡片
      petList: []
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
    
    // 检查是否有宠物数据
    this.checkPetsData()
  },
  
  onShow() {
    // 每次显示页面时重新检查宠物数据
    this.checkPetsData()
  },
  
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 检查宠物数据
    checkPetsData() {
      // 从本地存储获取宠物数据
      const petList = uni.getStorageSync('petList') || []
      this.petList = petList
      this.hasPets = petList.length > 0
    },
    
    // 添加宠物档案
    addPetProfile() {
      uni.navigateTo({
        url: '/pages/user/add-pet-basic'
      })
    },
    
    // 编辑宠物资料
    editPetProfile(pet) {
      uni.navigateTo({
        url: `/pages/user/edit-pet-profile?petId=${pet.id}`
      })
    },
    
    // 获取绝育状态文本
    getSterilizationText(sterilization) {
      const statusMap = {
        'sterilized': '已绝育',
        'not-sterilized': '未绝育',
        'unknown': '不清楚'
      }
      return statusMap[sterilization] || '不清楚'
    }
  }
}
</script>

<style lang="scss" scoped>
.my-pets-container {
  min-height: 100vh;
  background-color: #f8f8f8;
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
.pets-content {
  padding: 20rpx;
}

/* 宠物信息卡片 */
.pet-info-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  position: relative;
}

.pet-number {
  position: absolute;
  top: 30rpx;
  left: 30rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.pet-status-tag {
  position: absolute;
  top: 60rpx;
  left: 30rpx;
  background: #e3f2fd;
  border-radius: 20rpx;
  padding: 8rpx 16rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.status-icon {
  font-size: 20rpx;
  color: #1976d2;
}

.status-text {
  font-size: 22rpx;
  color: #1976d2;
}

.pet-details {
  margin-top: 100rpx;
  margin-right: 120rpx;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
  gap: 12rpx;
}

.detail-icon {
  font-size: 24rpx;
  width: 30rpx;
  text-align: center;
}

.detail-text {
  font-size: 26rpx;
  color: #333;
}

.divider {
  height: 1rpx;
  background: #f0f0f0;
  margin: 20rpx 0;
}

.pet-avatar {
  position: absolute;
  top: 30rpx;
  right: 30rpx;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 2rpx solid #f0f0f0;
}

.pet-avatar image {
  width: 100%;
  height: 100%;
}

.edit-button {
  position: absolute;
  bottom: 30rpx;
  right: 30rpx;
  background: #fff3cd;
  color: #856404;
  padding: 12rpx 20rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  border: 1rpx solid #ffeaa7;
}

/* 添加宠物档案按钮容器 */
.add-pet-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 20rpx;
}

/* 添加宠物档案按钮 */
.add-pet-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  transition: background-color 0.2s ease;
  width: 100%;
  max-width: 600rpx;
}

.add-pet-card:active {
  background-color: #f8f8f8;
}

.add-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #666;
}

.add-text {
  flex: 1;
}

.add-text text {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}
</style>
