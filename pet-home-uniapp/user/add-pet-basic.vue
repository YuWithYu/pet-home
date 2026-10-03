<template>
  <view class="add-pet-basic-container">
    <!-- 页面内容（使用默认导航栏） -->
    <view class="add-pet-content">
      <!-- 主要内容容器（居中） -->
      <view class="main-content-container">
        <!-- 头像上传区域 -->
        <view class="avatar-upload-section">
          <view class="avatar-container" @click="chooseAvatar">
            <view class="avatar-placeholder" v-if="!petAvatar">
              <image class="camera-icon-img" src="/static/images/拍照.png" mode="aspectFit" />
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
      petName: '',
      petAvatar: ''
    }
  },
  
  computed: {
    canProceed() {
      return this.petName.trim().length > 0
    }
  },
  
  methods: {
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
    
    // 从相册选择（选图后可裁剪，1:1 方形适合头像）
    selectFromAlbum() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album'],
        success: (res) => {
          this._cropThenSetPetAvatar(res.tempFilePaths[0])
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
        sizeType: ['original', 'compressed'],
        sourceType: ['camera'],
        success: (res) => {
          this._cropThenSetPetAvatar(res.tempFilePaths[0])
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

    // 微信小程序裁剪后设置宠物头像
    _cropThenSetPetAvatar(tempPath) {
      // #ifdef MP-WEIXIN
      if (typeof wx !== 'undefined' && wx.cropImage) {
        wx.cropImage({
          src: tempPath,
          cropScale: '1:1',
          success: (cropRes) => { this.petAvatar = cropRes.tempFilePath },
          fail: () => { this.petAvatar = tempPath }
        })
      } else {
        this.petAvatar = tempPath
      }
      // #endif
      // #ifndef MP-WEIXIN
      this.petAvatar = tempPath
      // #endif
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
        url: '/user/select-pet-type'
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

/* 页面内容 */
.add-pet-content {
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
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.camera-icon-img {
  width: 80rpx;
  height: 80rpx;
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
</style>
