<template>
  <view class="adoption-container">
    <!-- 页面头部 -->
    <view class="page-header">
      <view class="header-content">
        <view class="header-icon">🐾</view>
        <view class="header-title">宠物领养服务</view>
      </view>
    </view>

    <!-- 宠物列表 -->
    <view class="pets-section">
      <view class="section-title">待领养宠物</view>
      
      <view class="pets-list" v-if="pets.length > 0">
        <view 
          class="pet-card" 
          v-for="pet in pets" 
          :key="pet.id"
          @click="selectPet(pet)"
        >
          <image :src="getImageUrl(pet.imageUrl)" class="pet-image" mode="aspectFill"></image>
          <view class="pet-info">
            <view class="pet-name">{{ pet.petName }}</view>
            <view class="pet-details">{{ pet.breed }} · {{ pet.age }}岁 · {{ pet.gender }}</view>
            <view class="pet-description">{{ pet.description }}</view>
            <view class="pet-footer">
              <view class="adoption-fee">领养费：¥{{ pet.adoptionFee }}</view>
              <view class="adopt-button">立即领养</view>
            </view>
          </view>
        </view>
      </view>

      <view class="empty-state" v-else-if="!loading">
        <view class="empty-icon">🐕</view>
        <view class="empty-text">暂无待领养宠物</view>
        <view class="empty-desc">请稍后再来看看</view>
      </view>

      <view class="loading-state" v-if="loading">
        <view class="loading-text">加载中...</view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Adoption',
  
  data() {
    return {
      pets: [],
      loading: false
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'isLoggedIn'])
  },

  onLoad() {
    this.loadAdoptionPets()
  },

  methods: {
    async loadAdoptionPets() {
      try {
        this.loading = true
        
        // 调用获取待领养宠物列表的API
        const res = await this.$api.getAdoptionPets()
        
        if (res.code === 0 && res.data) {
          this.pets = res.data
        } else {
          this.pets = []
          console.error('加载宠物列表失败:', res.message)
        }
      } catch (error) {
        console.error('加载宠物列表失败:', error)
        this.pets = []
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        this.loading = false
      }
    },

    getImageUrl(imagePath) {
      if (!imagePath) {
        return '/static/images/default-pet.png'
      }
      
      // 如果是完整URL，直接返回
      if (imagePath.startsWith('http')) {
        return imagePath
      }
      
      // 如果是本地路径，添加服务器地址
      if (imagePath.startsWith('/static/')) {
        return 'http://localhost:8080' + imagePath
      }
      
      return imagePath
    },

    selectPet(pet) {
      if (!this.isLoggedIn) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }

      // 跳转到领养预约页面，传递宠物ID
      uni.navigateTo({
        url: `/pages/appointment/book-adoption?petId=${pet.id}&petName=${encodeURIComponent(pet.petName)}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.adoption-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  padding-bottom: 40rpx;
}

.page-header {
  padding: 60rpx 30rpx 40rpx;
  text-align: center;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.header-title {
  font-size: 48rpx;
  font-weight: bold;
  color: white;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.pets-section {
  background: white;
  margin: 0 20rpx;
  border-radius: 20rpx 20rpx 0 0;
  padding: 40rpx 30rpx;
  min-height: 60vh;
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
  text-align: center;
}

.pets-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.pet-card {
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.3s ease;
}

.pet-card:active {
  transform: scale(0.98);
}

.pet-image {
  width: 100%;
  height: 300rpx;
  background: #f5f5f5;
}

.pet-info {
  padding: 30rpx;
}

.pet-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.pet-details {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 15rpx;
}

.pet-description {
  font-size: 26rpx;
  color: #999;
  line-height: 1.5;
  margin-bottom: 20rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pet-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.adoption-fee {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff6b35;
}

.adopt-button {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  padding: 16rpx 32rpx;
  border-radius: 24rpx;
  font-size: 28rpx;
  font-weight: bold;
}

.empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 30rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.empty-desc {
  font-size: 28rpx;
  color: #999;
}

.loading-state {
  text-align: center;
  padding: 100rpx 0;
}

.loading-text {
  font-size: 28rpx;
  color: #666;
}
</style>
