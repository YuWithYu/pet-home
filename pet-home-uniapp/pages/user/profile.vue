<template>
  <view class="profile-container">
    <!-- 自定义白色导航栏 -->
    <view class="custom-white-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="navbar-title">
          <text>编辑个人信息</text>
        </view>
        <view class="navbar-right"></view>
      </view>
    </view>
    
    <!-- 个人资料内容 -->
    <view class="profile-content" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      
      <!-- 头像 -->
      <view class="profile-item" @click="chooseAvatar">
        <view class="item-label">头像</view>
        <view class="item-content">
          <view class="avatar-wrapper">
            <image :src="form.avatar || '/static/images/garfield-default-avatar.png'" mode="aspectFill" class="avatar-image" />
          </view>
        </view>
        <text class="item-arrow">></text>
      </view>
      <view class="divider"></view>

      <!-- 昵称 -->
      <view class="profile-item" @click="editNickname">
        <view class="item-label">昵称</view>
        <view class="item-content">
          <text class="item-text">{{ form.nickname || '宠友ddf94a72' }}</text>
        </view>
        <text class="item-arrow">></text>
      </view>
      <view class="divider"></view>

      <!-- 性别 -->
      <view class="profile-item" @click="editGender">
        <view class="item-label">性别</view>
        <view class="item-content">
          <text class="item-text">{{ getGenderText(form.gender) }}</text>
        </view>
        <text class="item-arrow">></text>
      </view>
      <view class="divider"></view>

      <!-- 手机号 -->
      <view class="profile-item" @click="bindPhone">
        <view class="item-label">手机号</view>
        <view class="item-content">
          <text class="item-text">{{ form.phone || '绑定手机号' }}</text>
        </view>
        <text class="item-arrow">></text>
      </view>
      <view class="divider"></view>

      <!-- 个性签名 -->
      <view class="profile-item" @click="editSignature">
        <view class="item-label">个性签名</view>
        <view class="item-content">
          <text class="item-text">{{ form.signature || '' }}</text>
        </view>
        <text class="item-arrow">></text>
      </view>
      <view class="divider"></view>

      <!-- 我的二维码 -->
      <view class="profile-item" @click="showQRCode">
        <view class="item-label">我的二维码</view>
        <view class="item-content">
          <text class="item-text"></text>
        </view>
        <text class="item-arrow">></text>
      </view>
    </view>

    <!-- 手机号验证弹窗 -->
    <view class="phone-modal" v-if="showPhoneModal" @click="closePhoneModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <view class="app-info">
            <view class="app-icon">🐱</view>
            <text class="app-name">宠物家</text>
          </view>
          <view class="info-icon">ℹ</view>
        </view>
        
        <view class="modal-body">
          <text class="modal-title">申请获取并验证你的手机号</text>
          <text class="modal-subtitle">完善用户信息绑定手机号</text>
          
          <view class="phone-display">
            <text class="phone-number">152****2765</text>
            <text class="phone-label">微信绑定号码</text>
          </view>
        </view>
        
        <view class="modal-actions">
          <button class="disallow-btn" @click="disallowPhone">不允许</button>
          <view class="other-number" @click="useOtherNumber">使用其它号码</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Profile',
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      showPhoneModal: false,
      form: {
        avatar: '',
        nickname: '',
        gender: '1', // 默认女性
        phone: '',
        signature: ''
      }
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
        this.statusBarHeight = res.statusBarHeight
      }
    })
    
    // 加载用户信息
    this.loadProfile()
  },
  methods: {
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },

    // 加载个人资料
    loadProfile() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      this.form = {
        ...this.form,
        ...userInfo
      }
    },

    // 获取性别文本
    getGenderText(gender) {
      const genderMap = {
        '0': '男',
        '1': '女',
        '2': '其他'
      }
      return genderMap[gender] || '女'
    },

    // 选择头像
    chooseAvatar() {
      uni.showActionSheet({
        itemList: ['用微信头像', '从相册选择', '拍照'],
        success: (res) => {
          switch (res.tapIndex) {
            case 0:
              this.useWechatAvatar()
              break
            case 1:
              this.chooseFromAlbum()
              break
            case 2:
              this.takePhoto()
              break
          }
        }
      })
    },

    // 使用微信头像
    useWechatAvatar() {
      uni.showToast({
        title: '微信头像功能开发中...',
        icon: 'none'
      })
    },

    // 从相册选择
    chooseFromAlbum() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album'],
        success: (res) => {
          this.updateAvatar(res.tempFilePaths[0])
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
          this.updateAvatar(res.tempFilePaths[0])
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

    // 更新头像
    updateAvatar(avatarPath) {
      // 更新头像显示
      this.form.avatar = avatarPath
      
      // 保存到本地存储
      const userInfo = uni.getStorageSync('userInfo') || {}
      userInfo.avatar = avatarPath
      uni.setStorageSync('userInfo', userInfo)
      
      uni.showToast({
        title: '头像更新成功',
        icon: 'success'
      })
    },

    // 编辑昵称
    editNickname() {
      uni.navigateTo({
        url: '/pages/user/edit-nickname'
      })
    },

    // 编辑性别
    editGender() {
      uni.showActionSheet({
        itemList: ['男', '女'],
        success: (res) => {
          const genderMap = {
            0: '0', // 男
            1: '1'  // 女
          }
          this.form.gender = genderMap[res.tapIndex]
          
          // 保存到本地存储
          const userInfo = uni.getStorageSync('userInfo') || {}
          userInfo.gender = this.form.gender
          uni.setStorageSync('userInfo', userInfo)
          
          uni.showToast({
            title: '性别更新成功',
            icon: 'success'
          })
        }
      })
    },

    // 绑定手机号
    bindPhone() {
      this.showPhoneModal = true
    },

    // 关闭手机号弹窗
    closePhoneModal() {
      this.showPhoneModal = false
    },

    // 不允许获取手机号
    disallowPhone() {
      this.showPhoneModal = false
      uni.showToast({
        title: '已取消手机号绑定',
        icon: 'none'
      })
    },

    // 使用其他号码
    useOtherNumber() {
      this.showPhoneModal = false
      uni.showToast({
        title: '其他号码绑定功能开发中...',
        icon: 'none'
      })
    },

    // 编辑个性签名
    editSignature() {
      uni.showToast({
        title: '个性签名编辑功能开发中...',
        icon: 'none'
      })
    },

    // 显示二维码
    showQRCode() {
      uni.showToast({
        title: '二维码功能开发中...',
        icon: 'none'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

/* 自定义白色导航栏 */
.custom-white-navbar {
  width: 100%;
  height: 88rpx;
  background: #fff;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 99;
  border-bottom: 1rpx solid #f0f0f0;
}

.navbar-content {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
}

.navbar-left {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 48rpx;
  color: #333;
  font-weight: 300;
}

.navbar-title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.navbar-title text {
  font-size: 32rpx;
  color: #333;
  font-weight: 400;
}

.navbar-right {
  width: 80rpx;
  height: 60rpx;
}

/* 个人资料内容 */
.profile-content {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.profile-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 24rpx;
  background: #fff;
  transition: background-color 0.2s ease;
}

.profile-item:active {
  background-color: #f8f8f8;
}

.item-label {
  font-size: 32rpx;
  color: #333;
  font-weight: 400;
  min-width: 120rpx;
}

.item-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-right: 16rpx;
}

.item-text {
  font-size: 32rpx;
  color: #666;
  text-align: right;
}

.item-arrow {
  font-size: 32rpx;
  color: #999;
  font-weight: 300;
}

.divider {
  height: 1rpx;
  background-color: #f0f0f0;
  margin: 0 24rpx;
}

/* 头像样式 */
.avatar-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f0f0f0;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

/* 手机号验证弹窗 */
.phone-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  width: 100%;
  background: #fff;
  border-radius: 20rpx 20rpx 0 0;
  padding: 40rpx 30rpx 60rpx;
  max-height: 80vh;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40rpx;
}

.app-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.app-icon {
  width: 60rpx;
  height: 60rpx;
  background: #ffd700;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}

.app-name {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
}

.info-icon {
  width: 40rpx;
  height: 40rpx;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  color: #999;
}

.modal-body {
  text-align: center;
  margin-bottom: 40rpx;
}

.modal-title {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.modal-subtitle {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 40rpx;
}

.phone-display {
  background: #f8f8f8;
  border-radius: 16rpx;
  padding: 30rpx;
  margin: 0 20rpx;
}

.phone-number {
  display: block;
  font-size: 32rpx;
  color: #333;
  font-weight: 600;
  margin-bottom: 8rpx;
}

.phone-label {
  display: block;
  font-size: 22rpx;
  color: #999;
}

.modal-actions {
  text-align: center;
}

.disallow-btn {
  width: 100%;
  height: 80rpx;
  background: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 500;
  margin-bottom: 30rpx;
}

.other-number {
  font-size: 26rpx;
  color: #007aff;
  text-decoration: underline;
}
</style>