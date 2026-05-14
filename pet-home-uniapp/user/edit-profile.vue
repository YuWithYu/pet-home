<template>
  <view class="edit-profile-container">
    <!-- 微信登录后未绑定手机号提示 -->
    <view class="must-bind-banner" v-if="mustBindPhone">
      <text class="must-bind-text">为保障账号安全与后续使用（如找回密码），请先绑定手机号</text>
    </view>
    <!-- 个人信息卡片（使用默认导航栏） -->
    <view class="profile-card">
      <!-- 头像 -->
      <view class="card-item" @click="chooseAvatar">
        <view class="item-label">头像</view>
        <view class="item-content">
          <view class="avatar-display">
            <image :src="getAvatarUrl(form.avatar)" mode="aspectFill" class="avatar-img" @error="handleAvatarError" />
          </view>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 昵称 -->
      <view class="card-item" @click="editNickname">
        <view class="item-label">昵称</view>
        <view class="item-content">
          <text class="item-value">{{ form.nickname || '' }}</text>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 账号（点击进入修改） -->
      <view class="card-item card-item-account" @click="goEditUsername">
        <view class="item-label">账号</view>
        <view class="item-content">
          <text class="item-value">{{ form.username || '未设置' }}</text>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 性别 -->
      <view class="card-item" @click="editGender">
        <view class="item-label">性别</view>
        <view class="item-content">
          <text class="item-value">{{ getGenderText(form.gender) }}</text>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 手机号（仅支持更换，不支持解绑） -->
      <view class="card-item card-item-phone" @click="bindPhone">
        <view class="item-label">手机号</view>
        <view class="item-content">
          <text class="item-value">{{ form.phone || (userInfo && userInfo.phone ? userInfo.phone : '') || '绑定手机号' }}</text>
          <text class="item-arrow">></text>
        </view>
      </view>

      <!-- 个性签名 -->
      <view class="card-item" @click="editSignature">
        <view class="item-label">个性签名</view>
        <view class="item-content">
          <text class="item-value">{{ form.signature || '' }}</text>
          <text class="item-arrow">></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'EditProfile',
  data() {
    return {
      mustBindPhone: false, // 微信登录后必须绑定手机号
      form: {
        avatar: '',
        nickname: '',
        username: '', // 账号（用于登录，与微信「账号」一致）
        gender: '0', // 0-男, 1-女
        phone: '',
        signature: ''
      }
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.userInfo || uni.getStorageSync('userInfo') || {}
    }
  },
  onLoad(options) {
    this.mustBindPhone = options.mustBindPhone === '1' || options.mustBindPhone === 'true'
    this.loadProfile()
    if (this.mustBindPhone) {
      setTimeout(() => {
        uni.showModal({
          title: '请绑定手机号',
          content: '为保障账号安全与后续使用（如找回密码），请先绑定手机号。',
          showCancel: true,
          cancelText: '暂不绑定',
          confirmText: '去绑定',
          success: (res) => {
            if (res.cancel) {
              this.mustBindPhone = false
              uni.switchTab({ url: '/pages/main/index' })
            } else if (res.confirm) {
              this.bindPhone()
            }
          }
        })
      }, 300)
    }
  },
  methods: {
    // 加载个人资料；silent 为 true 时不显示全局「加载中」（用于 onShow 静默刷新）
    loadProfile(silent = false) {
      const currentUserId = uni.getStorageSync('userId')
      const currentUsername = uni.getStorageSync('username')
      
      const params = { showLoading: !silent }
      if (currentUserId) {
        params.userId = currentUserId
      } else if (currentUsername) {
        params.username = currentUsername
      }

      api.getCurrentUser(params)
        .then(res => {
          if ((res.code === 200 || res.code === 0) && res.data) {
            const user = res.data
            // 获取手机号，优先使用API返回的，其次从userInfo获取
            let phone = user.phone || ''
            if (!phone && this.userInfo && this.userInfo.phone) {
              phone = this.userInfo.phone
            }
            if (!phone) {
              const storedUserInfo = uni.getStorageSync('userInfo')
              if (storedUserInfo && storedUserInfo.phone) {
                phone = storedUserInfo.phone
              }
            }
            
            this.form = {
              avatar: user.avatar || null,
              nickname: user.nickname || '',
              username: currentUsername || user.username || (this.userInfo && this.userInfo.username) || '',
              gender: user.gender !== undefined && user.gender !== null ? String(user.gender) : '0',
              phone: phone,
              signature: user.signature || user.bio || ''
            }
            // 微信登录后为可选绑定：不再自动跳转手机号页，由弹窗「去绑定」/「暂不绑定」决定
          }
        })
        .catch(err => {
          console.error('加载用户信息失败:', err)
        })
    },

    // 获取性别文本
    getGenderText(gender) {
      const genderMap = {
        '0': '男',
        '1': '女',
        '2': '其他'
      }
      return genderMap[gender] || '男'
    },

    // 获取头像URL（处理本地临时路径和服务器路径）
    getAvatarUrl(avatar) {
      if (!avatar) {
        return '/static/images/login-dog.png'
      }
      
      // 如果是静态资源路径，直接返回
      if (avatar === '/static/images/login-dog.png' || avatar.includes('login-dog.png')) {
        return '/static/images/login-dog.png'
      }
      
      // 如果是本地临时路径（wxfile:// 或 file://），直接使用
      if (avatar.startsWith('wxfile://') || avatar.startsWith('file://')) {
        return avatar
      }
      
      // 如果是服务器路径，使用 util 处理
      return util.getImageUrl(avatar)
    },

    // 处理头像加载错误
    handleAvatarError(e) {
      const currentAvatar = this.form?.avatar || ''
      console.error('头像加载失败:', currentAvatar, e)
      
      // 如果是默认头像也加载失败，不再尝试，避免无限循环
      if (currentAvatar && (currentAvatar.includes('login-dog') || currentAvatar.includes('pet-paw.png'))) {
        return
      }
      
      // 如果其他头像加载失败，设置为默认图片
      if (this.form) {
        this.form.avatar = '/static/images/login-dog.png'
      }
    },

    // 选择头像
    chooseAvatar() {
      uni.showActionSheet({
        itemList: ['从相册选择', '拍照'],
        success: (res) => {
          if (res.tapIndex === 0) {
            this.chooseFromAlbum()
          } else if (res.tapIndex === 1) {
            this.takePhoto()
          }
        }
      })
    },

    // 从相册选择（选图后可裁剪，1:1 方形适合头像）
    chooseFromAlbum() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album'],
        success: (res) => {
          this._cropThenUploadAvatar(res.tempFilePaths[0])
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
          this._cropThenUploadAvatar(res.tempFilePaths[0])
        }
      })
    },

    // 微信小程序裁剪后上传头像，其它平台直接上传
    _cropThenUploadAvatar(tempPath) {
      // #ifdef MP-WEIXIN
      if (typeof wx !== 'undefined' && wx.cropImage) {
        wx.cropImage({
          src: tempPath,
          cropScale: '1:1',
          success: (cropRes) => this.uploadImage(cropRes.tempFilePath, 'avatar'),
          fail: () => this.uploadImage(tempPath, 'avatar')
        })
      } else {
        this.uploadImage(tempPath, 'avatar')
      }
      // #endif
      // #ifndef MP-WEIXIN
      this.uploadImage(tempPath, 'avatar')
      // #endif
    },

    // 上传图片
    uploadImage(filePath, type) {
      uni.showLoading({
        title: '上传中...'
      })

      const base = (util.getApiBaseUrl && util.getApiBaseUrl()) || api.baseURL || ''
      const uploadUrl = base.replace(/\/+$/, '') + '/api/upload/image'
      uni.uploadFile({
        url: uploadUrl,
        filePath: filePath,
        name: 'file',
        formData: {
          type: type
        },
        success: (res) => {
          try {
            const raw = res.data
            if (typeof raw === 'string' && raw.trim().startsWith('<')) {
              uni.showToast({ title: '上传接口异常，请检查网络或后端', icon: 'none' })
              return
            }
            const data = typeof raw === 'string' ? JSON.parse(raw) : raw
            if (data.code === 200 || data.code === 0) {
              if (type === 'avatar') {
                // 获取后端返回的图片URL（可能是相对路径或绝对路径）
                const imageUrl = data.data.url || data.data
                // 检查是否是临时路径
                if (imageUrl && typeof imageUrl === 'string') {
                  const tmpPattern = /(\/tmp\/|\/__tmp__\/|^http:\/\/(tmp\/|__tmp__\/)|^https:\/\/(tmp\/|__tmp__\/)|^(tmp\/|__tmp__\/))/
                  if (tmpPattern.test(imageUrl)) {
                    console.error('上传返回的是临时路径，请检查上传逻辑:', imageUrl)
                    uni.showToast({
                      title: '头像上传失败，请重试',
                      icon: 'none'
                    })
                    return
                  }
                }
                // 保存实际路径（后端返回的路径，不要用getImageUrl处理，直接保存相对路径）
                // 因为getImageUrl可能会转换路径格式，我们保存时应该保存原始路径或相对路径
                this.form.avatar = imageUrl
              }
              this.saveProfile()
              uni.showToast({
                title: '上传成功',
                icon: 'success'
              })
            } else {
              uni.showToast({
                title: data.msg || '上传失败',
                icon: 'none'
              })
            }
          } catch (e) {
            console.error('解析响应失败:', e)
            uni.showToast({
              title: '上传失败',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          console.error('上传失败:', err)
          uni.showToast({
            title: '上传失败',
            icon: 'none'
          })
        },
        complete: () => {
          uni.hideLoading()
        }
      })
    },

    // 编辑昵称
    editNickname() {
      uni.navigateTo({
        url: `/user/edit-nickname?nickname=${encodeURIComponent(this.form.nickname || '')}`
      })
    },

    // 修改账号（每1年1次，格式：字母或下划线开头，6-20位）
    goEditUsername() {
      uni.navigateTo({
        url: `/user/edit-username?current=${encodeURIComponent(this.form.username || '')}`
      })
    },

    // 编辑性别
    editGender() {
      uni.showActionSheet({
        itemList: ['男', '女', '其他'],
        success: (res) => {
          const genderMap = {
            0: '0', // 男
            1: '1', // 女
            2: '2'  // 其他
          }
          this.form.gender = genderMap[res.tapIndex]
          this.saveProfile()
        }
      })
    },

    // 编辑个性签名
    editSignature() {
      uni.showModal({
        title: '编辑个性签名',
        editable: true,
        placeholderText: '请输入个性签名',
        content: this.form.signature || '',
        success: (res) => {
          if (res.confirm) {
            this.form.signature = res.content || ''
            this.saveProfile()
          }
        }
      })
    },

    // 跳转到手机号页（展示已绑定号码、更换入口）
    bindPhone() {
      uni.navigateTo({ url: '/user/phone' })
    },

    // 绑定手机号后的统一处理：同步 storage，若为「必须绑定」则跳转首页
    afterPhoneBound(phone) {
      const u = uni.getStorageSync('userInfo') || {}
      u.phone = phone
      u.phoneNumber = phone
      uni.setStorageSync('userInfo', u)
      this.$store.commit('SET_USER_INFO', u)
      if (this.mustBindPhone) {
        this.mustBindPhone = false
        setTimeout(() => {
          uni.switchTab({ url: '/pages/main/index' })
        }, 800)
      }
    },

    // 保存资料
    saveProfile() {
      // 获取用户ID
      const currentUserId = uni.getStorageSync('userId')
      if (!currentUserId) {
        uni.showToast({
          title: '用户信息获取失败，请重新登录',
          icon: 'none'
        })
        return
      }

      uni.showLoading({
        title: '保存中...'
      })

      const payload = {
        id: currentUserId,
        userId: currentUserId,
        avatar: this.form.avatar,
        nickname: this.form.nickname,
        gender: parseInt(this.form.gender),
        signature: this.form.signature
      }
      if (this.form.phone && this.form.phone.trim()) {
        payload.phone = this.form.phone.trim()
      }
      api.updateUser(payload)
        .then(res => {
          uni.hideLoading()
          if (res.code === 200 || res.code === 0) {
            uni.showToast({
              title: '保存成功',
              icon: 'success',
              duration: 1500
            })
            // 更新本地存储的用户信息
            const updatedUserInfo = uni.getStorageSync('userInfo') || {}
            if (res.data) {
              updatedUserInfo.avatar = res.data.avatar || updatedUserInfo.avatar
              updatedUserInfo.nickname = res.data.nickname || updatedUserInfo.nickname
              updatedUserInfo.gender = res.data.gender !== undefined ? res.data.gender : updatedUserInfo.gender
              updatedUserInfo.signature = res.data.signature || res.data.bio || updatedUserInfo.signature
              updatedUserInfo.phone = (res.data.phone || res.data.phoneNumber) || this.form.phone || updatedUserInfo.phone
              uni.setStorageSync('userInfo', updatedUserInfo)
              this.$store.commit('SET_USER_INFO', updatedUserInfo)
            }
            // 触发页面刷新
            setTimeout(() => {
              uni.$emit('userInfoUpdated')
            }, 500)
          } else {
            uni.showToast({
              title: res.msg || '保存失败',
              icon: 'none'
            })
          }
        })
        .catch(err => {
          uni.hideLoading()
          console.error('保存失败:', err)
          uni.showToast({
            title: '保存失败，请重试',
            icon: 'none'
          })
        })
    }
  },
  onShow() {
    // 从编辑昵称页面返回时刷新数据
    const editedNickname = uni.getStorageSync('editedNickname')
    if (editedNickname) {
      this.form.nickname = editedNickname
      uni.removeStorageSync('editedNickname')
      this.saveProfile()
    }
    // 每次显示页面时静默刷新用户信息，不显示全局「加载中」
    this.loadProfile(true)
  }
}
</script>

<style lang="scss" scoped>
.edit-profile-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.must-bind-banner {
  background: linear-gradient(135deg, #ff9a56 0%, #ff6b35 100%);
  padding: 24rpx 32rpx;
  margin: 0 24rpx;
  margin-top: 24rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.25);
}
.must-bind-text {
  color: #fff;
  font-size: 28rpx;
  line-height: 1.5;
}

/* 个人信息卡片（整体缩小） */
.profile-card {
  background: #fff;
  margin-left: 0;
  margin-right: 0;
  border-radius: 0;
}

.card-item {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  min-height: 68rpx;
}

.card-item:last-child {
  border-bottom: none;
}

.item-label {
  font-size: 28rpx;
  color: #333;
  min-width: 128rpx;
}

.item-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 14rpx;
}

.item-value {
  font-size: 28rpx;
  color: #333;
  text-align: right;
  flex: 1;
}

/* 头像显示 */
.avatar-display {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 2rpx solid #ffd700;
  background: #f0f0f0;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.item-arrow {
  font-size: 28rpx;
  color: #999;
  font-weight: 300;
}

.card-item-account .item-content {
  flex-direction: row;
  align-items: center;
}
.item-hint {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

</style>
