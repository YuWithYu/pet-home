<template>
  <view class="pets-container">
    <!-- 添加宠物按钮 -->
    <view class="add-pet-btn" @click="goAddPet">
      <view class="add-icon">+</view>
      <view class="add-text">添加宠物</view>
    </view>

    <!-- 宠物列表 -->
    <view class="pets-list">
      <view class="empty-pets" v-if="pets.length === 0">
        <view class="empty-icon">🐕</view>
        <view class="empty-text">还没有宠物，快去添加吧</view>
      </view>

      <view class="pet-item bg-white" v-for="pet in pets" :key="pet.id">
        <view class="pet-avatar">
          <image :src="pet.avatar" mode="aspectFill" v-if="pet.avatar" />
          <view class="pet-avatar-placeholder" v-else>{{ pet.type === 'dog' ? '🐕' : '🐱' }}</view>
        </view>
        <view class="pet-info">
          <view class="pet-name">{{ pet.name }}</view>
          <view class="pet-breed">{{ pet.breed }} · {{ pet.age }}岁</view>
          <view class="pet-desc" v-if="pet.description">{{ pet.description }}</view>
        </view>
        <view class="pet-actions">
          <u-button type="default" size="mini" @click="editPet(pet)">编辑</u-button>
          <u-button type="primary" size="mini" @click="viewPetDetail(pet)">详情</u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Pets',

  data() {
    return {
      pets: [
        {
          id: 1,
          name: '旺财',
          type: 'dog',
          breed: '金毛犬',
          age: 3,
          avatar: '',
          description: '非常活泼的金毛犬'
        }
      ]
    }
  },

  methods: {
    goAddPet() {
      uni.navigateTo({
        url: '/pages/user/pets/add'
      })
    },

    editPet(pet) {
      uni.navigateTo({
        url: `/pages/user/pets/edit?id=${pet.id}`
      })
    },

    viewPetDetail(pet) {
      uni.navigateTo({
        url: `/pages/user/pets/detail?id=${pet.id}`
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.pets-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.add-pet-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: white;
  margin: 20rpx;
  padding: 40rpx;
  border-radius: 16rpx;
  border: 2rpx dashed #ddd;
  color: #666;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.1);

  .add-icon {
    font-size: 48rpx;
    margin-right: 16rpx;
    color: #ff6b35;
  }

  .add-text {
    font-size: 32rpx;
  }
}

.pets-list {
  .empty-pets {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 200rpx 60rpx;
    color: #999;

    .empty-icon {
      font-size: 120rpx;
      margin-bottom: 40rpx;
    }

    .empty-text {
      font-size: 32rpx;
      margin-bottom: 60rpx;
    }
  }

  .pet-item {
    display: flex;
    align-items: center;
    padding: 30rpx;
    margin: 0 20rpx 20rpx;
    border-radius: 16rpx;
    box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.1);
  }

  .pet-avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 30rpx;
    background-color: #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: center;

    image {
      width: 100%;
      height: 100%;
    }

    .pet-avatar-placeholder {
      font-size: 48rpx;
    }
  }

  .pet-info {
    flex: 1;

    .pet-name {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 8rpx;
    }

    .pet-breed {
      font-size: 26rpx;
      color: #666;
      margin-bottom: 8rpx;
    }

    .pet-desc {
      font-size: 24rpx;
      color: #999;
    }
  }

  .pet-actions {
    display: flex;
    gap: 16rpx;
  }
}
</style>
