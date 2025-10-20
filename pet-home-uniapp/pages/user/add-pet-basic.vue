<template>
  <view class="add-pet-basic-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>添加宠物档案</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="add-pet-content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <!-- 主要内容容器（居中） -->
      <view class="main-content-container">
        <!-- 头像上传区域 -->
        <view class="avatar-upload-section">
          <view class="avatar-container" @click="chooseAvatar">
            <view class="avatar-placeholder" v-if="!petAvatar">
              <view class="camera-icon">📷</view>
              <view class="upload-tips">
                <text class="tip-line1">添加优质</text>
                <text class="tip-line2">头像更吸引人</text>
              </view>
            </view>
            <view class="avatar-preview" v-else>
              <image :src="petAvatar" mode="aspectFill" />
            </view>
          </view>
        </view>

        <!-- 宠物名称输入区域 -->
        <view class="name-input-section">
          <view class="input-container">
            <input 
              class="name-input" 
              type="text" 
              placeholder="请输入宠物名字" 
              v-model="petName"
              maxlength="20"
            />
          </view>
          <view class="input-tip">
            <text>名字中如有阿拉伯数字,建议输入汉字中的数字零至九哦~</text>
          </view>
        </view>
      </view>

      <!-- 下一步按钮 -->
      <view class="next-button-section">
        <view 
          class="next-button" 
          :class="{ 'active': canProceed }"
          @click="goToNext"
        >
          <text>下一步</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AddPetBasic',
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      petName: '',
      petAvatar: ''
    }
  },
  
  computed: {
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight
    },
    
    canProceed() {
      return this.petName.trim().length > 0
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
  },
  
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 选择头像
    chooseAvatar() {
      uni.showActionSheet({
        itemList: ['从相册选择', '拍照'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.selectFromAlbum()
          } else if (res.tapIndex === 1) {
            this.takePhoto()
          }
        }
      })
    },
    
    // 从相册选择
    selectFromAlbum() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: (res) => {
          this.petAvatar = res.tempFilePaths[0]
        },
        fail: (err) => {
          console.error('选择图片失败:', err)
          uni.showToast({
            title: '选择图片失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 拍照
    takePhoto() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['camera'],
        success: (res) => {
          this.petAvatar = res.tempFilePaths[0]
        },
        fail: (err) => {
          console.error('拍照失败:', err)
          uni.showToast({
            title: '拍照失败',
            icon: 'none'
          })
        }
      })
    },
    
    // 下一步
    goToNext() {
      if (!this.canProceed) {
        uni.showToast({
          title: '请输入宠物名字',
          icon: 'none'
        })
        return
      }
      
      // 保存基础信息到本地存储
      const basicInfo = {
        name: this.petName.trim(),
        avatar: this.petAvatar
      }
      uni.setStorageSync('petBasicInfo', basicInfo)
      
      // 跳转到选择宠物类型页面
      uni.navigateTo({
        url: '/pages/user/select-pet-type'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.add-pet-basic-container {
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
.add-pet-content {
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

/* 头像上传区域 */
.avatar-upload-section {
  display: flex;
  justify-content: center;
  margin-bottom: 40rpx;
}

.avatar-container {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.avatar-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.camera-icon {
  font-size: 48rpx;
  color: #999;
  margin-bottom: 16rpx;
}

.upload-tips {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.tip-line1 {
  font-size: 24rpx;
  color: #999;
}

.tip-line2 {
  font-size: 24rpx;
  color: #999;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-preview image {
  width: 100%;
  height: 100%;
}

/* 宠物名称输入区域 */
.name-input-section {
  width: 100%;
  max-width: 600rpx;
}

.input-container {
  background: #fff;
  border-radius: 16rpx;
  border: 1rpx solid #e0e0e0;
  margin-bottom: 16rpx;
}

.name-input {
  width: 100%;
  height: 88rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
  color: #333;
  background: transparent;
  border: none;
  outline: none;
}

.name-input::placeholder {
  color: #999;
}

.input-tip {
  padding: 0 10rpx;
}

.input-tip text {
  font-size: 24rpx;
  color: #999;
  line-height: 1.4;
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
  background: #ff6b35;
}

.next-button text {
  font-size: 32rpx;
  color: #999;
  font-weight: 500;
}

.next-button.active text {
  color: #fff;
}

.next-button:active {
  transform: scale(0.98);
}
</style>
