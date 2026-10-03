<template>
  <view class="edit-pet-profile">
    <!-- 使用系统默认导航栏，无需自定义 -->

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 宠物头像和昵称 -->
      <view class="pet-avatar-section">
        <view class="avatar-container">
          <image
            :src="avatarSrc"
            class="pet-avatar"
            mode="aspectFill"
            @error="handleAvatarError"
          ></image>
          <view class="camera-btn" @tap="chooseAvatar">
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
        </view>
      </view>

      <!-- 宠物信息表单 -->
      <view class="pet-form" id="petForm">
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
            <text class="item-arrow">›</text>
          </view>
        </view>

        <!-- 宠物品种 -->
        <view class="form-item" @tap="selectBreed">
          <text class="item-label">宠物品种</text>
          <view class="item-content">
            <text class="item-value">{{ petData.breed || '请选择品种' }}</text>
            <text class="item-arrow">›</text>
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

        <!-- 宠物体重 -->
        <view class="form-item" @tap="editWeight">
          <text class="item-label">宠物体重</text>
          <view class="item-content">
            <text class="item-value">{{ displayWeight }} kg</text>
            <text class="item-arrow">›</text>
          </view>
        </view>

        <!-- 一句话描述 -->
        <view class="form-item" @tap="editDescription">
          <text class="item-label">一句话描述</text>
          <view class="item-content">
            <text class="item-value">{{ petData.description || defaultDescriptionText }}</text>
            <text class="item-arrow">›</text>
          </view>
        </view>

        <!-- 疫苗记录/健康状况/就医记录（宠物医疗模块）已移除 -->
      </view>

      <!-- 保存按钮 -->
      <view class="save-btn" @tap="saveEdit">
        <text class="save-text">保存编辑</text>
      </view>
    </view>

    <!-- 体重编辑弹窗（小程序 showModal 不支持 editable，用自定义弹窗） -->
    <view class="weight-popup-mask" v-if="showWeightPopup" @tap="closeWeightPopup">
      <view class="weight-popup" @tap.stop>
        <view class="weight-popup-title">编辑体重</view>
        <view class="weight-popup-input-wrap">
          <input
            class="weight-popup-input"
            type="digit"
            :value="tempWeight"
            @input="onWeightInput"
            placeholder="请输入体重(kg)"
            focus
          />
          <text class="weight-popup-unit">kg</text>
        </view>
        <view class="weight-popup-btns">
          <view class="weight-popup-btn cancel" @tap="closeWeightPopup">取消</view>
          <view class="weight-popup-btn confirm" @tap="confirmWeight">确定</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

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
        arrivalDate: '2025-10-19',
        description: '爱吃啥，有啥特点',
        userId: null,
        species: ''
      },
      isUploading: false, // 防止重复上传
      showWeightPopup: false,
      tempWeight: '',
      // 疫苗记录/健康状况/就医记录（宠物医疗模块）已移除
    }
  },
  computed: {
    displayWeight() {
      const w = this.petData.weight
      if (w === '' || w === null || w === undefined) return '0'
      return String(w)
    },
    /** 小程序首屏在模板里调方法偶发异常，用计算属性稳定 image src */
    avatarSrc() {
      try {
        return this.getAvatarUrl(this.petData && this.petData.avatar)
      } catch (e) {
        return '/static/images/login-dog.png'
      }
    },
    /** 默认描述文案放计算属性，避免模板里出现 ~> 等字符破坏编译导致 render 为空、整页白屏 */
    defaultDescriptionText() {
      return '爱吃啥，有啥特点'
    }
  },
  
  onLoad(options) {
    const raw =
      (options.petId != null && options.petId !== undefined ? String(options.petId).trim() : '') ||
      (options.id != null && options.id !== undefined ? String(options.id).trim() : '') ||
      (options.pet_id != null ? String(options.pet_id).trim() : '')
    if (raw && raw !== 'undefined' && raw !== 'null') {
      this.loadPetData(raw)
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
    
    // 获取头像URL（临时路径 __tmp__/tmp 无法访问，返回默认头像）
    getAvatarUrl(avatar) {
      if (!avatar) {
        return '/static/images/login-dog.png'
      }
      const s = String(avatar).trim()
      if (s.includes('garfield-default-avatar') || s.includes('__tmp__') || s.includes('/tmp/') || /^https?:\/\/tmp\//.test(s)) {
        return '/static/images/login-dog.png'
      }
      if (s.startsWith('wxfile://') || s.startsWith('file://')) {
        return s
      }
      try {
        return util.getImageUrl(s)
      } catch (e) {
        return '/static/images/login-dog.png'
      }
    },
    
    // 处理头像加载错误
    handleAvatarError(e) {
      console.error('头像加载失败:', this.petData.avatar, e)
      // 如果头像加载失败，设置为默认图片
      if (this.petData.avatar && !this.petData.avatar.includes('login-dog')) {
        this.petData.avatar = '/static/images/login-dog.png'
      }
    },
    
    // 加载宠物数据
    async loadPetData(petId) {
      try {
        // 优先从 API 加载最新数据
        const res = await api.getPetDetail(petId)
        
        if ((res.code === 0 || res.code === 200) && res.data) {
          const pet = res.data
          this.petData = {
            id: pet.id,
            name: pet.name || '',
            avatar: pet.avatar || '',
            breed: pet.breed || '',
            gender: pet.gender || 'male',
            sterilization: pet.sterilization !== undefined && pet.sterilization !== null ? pet.sterilization : 'no', // 优先使用后端数据
            dating: pet.dating !== undefined && pet.dating !== null ? pet.dating : 'no', // 优先使用后端数据
            weight: pet.weight || 0,
            arrivalDate: pet.arrivalDate || pet.arrival_date || '',
            description: pet.description || '爱吃啥，有啥特点',
            userId: pet.userId,
            species: pet.species || pet.type || ''
          }
          return
        }
      } catch (error) {
        // 静默处理错误
      }
      
      // 如果 API 加载失败，从本地存储获取
      try {
        const petList = uni.getStorageSync('petList') || []
        const pet = petList.find((p) => {
          if (p == null || p.id === undefined || p.id === null) return false
          return p.id == petId || String(p.id) === String(petId)
        })
        if (pet) {
          this.petData = { 
            ...pet,
            dating: pet.dating || 'no',
            description: pet.description || '爱吃啥，有啥特点',
            userId: pet.userId,
            species: pet.species || pet.type || ''
          }
        } else {
          console.error('未找到宠物数据, petId:', petId)
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
    
    // 选择头像（选图后可裁剪，1:1 方形适合头像）
    chooseAvatar() {
      if (this.isUploading) {
        uni.showToast({
          title: '正在上传中，请稍候',
          icon: 'none'
        })
        return
      }
      
      uni.showActionSheet({
        itemList: ['从相册选择', '拍照'],
        success: (res) => {
          const sourceType = res.tapIndex === 0 ? ['album'] : ['camera']
          uni.chooseImage({
            count: 1,
            sizeType: ['original', 'compressed'],
            sourceType: sourceType,
            success: (imgRes) => {
              this._cropThenUploadPetAvatar(imgRes.tempFilePaths[0])
            },
            fail: (err) => {
              console.error('选择图片失败:', err)
              uni.showToast({
                title: '选择图片失败',
                icon: 'none'
              })
            }
          })
        }
      })
    },

    // 微信小程序裁剪后上传宠物头像
    _cropThenUploadPetAvatar(tempPath) {
      // #ifdef MP-WEIXIN
      if (typeof wx !== 'undefined' && wx.cropImage) {
        wx.cropImage({
          src: tempPath,
          cropScale: '1:1',
          success: (cropRes) => {
            this.petData.avatar = cropRes.tempFilePath
            this.uploadAvatar(cropRes.tempFilePath)
          },
          fail: () => {
            this.petData.avatar = tempPath
            this.uploadAvatar(tempPath)
          }
        })
      } else {
        this.petData.avatar = tempPath
        this.uploadAvatar(tempPath)
      }
      // #endif
      // #ifndef MP-WEIXIN
      this.petData.avatar = tempPath
      this.uploadAvatar(tempPath)
      // #endif
    },
    
    // 上传头像到服务器
    uploadAvatar(filePath) {
      if (this.isUploading) {
        return
      }
      
      this.isUploading = true
      uni.showLoading({
        title: '上传中...',
        mask: true
      })
      
      uni.uploadFile({
        url: api.baseURL + '/api/upload/image',
        filePath: filePath,
        name: 'file',
        formData: {
          type: 'pet-avatar'
        },
        success: (res) => {
          uni.hideLoading()
          this.isUploading = false
          try {
            const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
            
            if ((data.code === 0 || data.code === 200) && data.data) {
              // data.data 可能是字符串（图片URL）或对象
              let imageUrl = typeof data.data === 'string' ? data.data : (data.data.url || data.data)
              
              // 修复 URL：确保是完整的服务器路径
              if (imageUrl && !imageUrl.startsWith('http')) {
                // 如果是相对路径，使用util处理
                imageUrl = util.getImageUrl(imageUrl)
              }
              
              // 更新头像 URL
              this.petData.avatar = imageUrl
              
              uni.showToast({
                title: '头像上传成功',
                icon: 'success'
              })
            } else {
              uni.showToast({
                title: data.msg || '上传失败',
                icon: 'none'
              })
            }
          } catch (e) {
            console.error('解析上传响应失败:', e, res)
            uni.showToast({
              title: '上传失败：响应格式错误',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          uni.hideLoading()
          this.isUploading = false
          console.error('上传头像失败:', err)
          uni.showToast({
            title: '上传失败：' + (err.errMsg || '网络错误'),
            icon: 'none'
          })
        },
        complete: () => {
          uni.hideLoading()
          this.isUploading = false
        }
      })
    },
    
// 选择品种
    selectBreed() {
      uni.navigateTo({
        url: '/user/select-breed-list?petType=' + (this.petData.petType || 'cat')
      })
    },
    
    // 处理品种选择结果
    handleSelectedBreed(breedName) {
      this.petData.breed = breedName
    },
    
    // 检查页面更新
    checkForUpdates() {
      try {
        const pages = getCurrentPages()
        if (pages.length > 1) {
          const prevPage = pages[pages.length - 2]
          if (prevPage && prevPage.$vm && prevPage.$vm.selectedBreed) {
            this.petData.breed = prevPage.$vm.selectedBreed
            prevPage.$vm.selectedBreed = null
          }
        }
      } catch (e) {
        console.warn('checkForUpdates', e)
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
    
    // 编辑体重（小程序 showModal 不支持 editable，用自定义弹窗）
    editWeight() {
      this.tempWeight = this.displayWeight
      this.showWeightPopup = true
    },
    onWeightInput(e) {
      this.tempWeight = (e.detail && e.detail.value) ? e.detail.value : ''
    },
    closeWeightPopup() {
      this.showWeightPopup = false
      this.tempWeight = ''
    },
    confirmWeight() {
      const weight = parseFloat(String(this.tempWeight).trim())
      if (!isNaN(weight) && weight >= 0 && weight <= 100) {
        this.petData.weight = weight.toFixed(2)
        this.showWeightPopup = false
        this.tempWeight = ''
      } else {
        uni.showToast({
          title: '请输入0-100之间的数字',
          icon: 'none'
        })
      }
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
    async saveEdit() {
      if (!this.petData.name || !this.petData.name.trim()) {
        uni.showToast({
          title: '请输入宠物名字',
          icon: 'none'
        })
        return
      }
      
      if (!this.petData.id) {
        uni.showToast({
          title: '宠物ID不存在',
          icon: 'none'
        })
        return
      }
      
      try {
        uni.showLoading({
          title: '保存中...',
          mask: true
        })
        
        // 构建要更新的数据（确保字段名与后端实体类匹配）
        const updateData = {
          id: parseInt(this.petData.id) // 确保是数字类型，必需字段
        }
        
        // 只添加有值的字段
        if (this.petData.name && this.petData.name.trim()) {
          updateData.name = this.petData.name.trim()
        }
        
        if (this.petData.avatar) {
          const isLocalPath = this.petData.avatar.startsWith('wxfile://') || this.petData.avatar.startsWith('file://')
          // 仅当用户刚选了新头像且正在上传时拦截；未换头像或本地路径为旧数据时，跳过头像字段继续保存
          if (isLocalPath && this.isUploading) {
            uni.hideLoading()
            uni.showToast({
              title: '请等待头像上传完成',
              icon: 'none'
            })
            return
          }
          if (!isLocalPath) {
            updateData.avatar = this.petData.avatar
          }
        }
        
        if (this.petData.breed) {
          updateData.breed = this.petData.breed
        }
        
        if (this.petData.gender) {
          updateData.gender = this.petData.gender
        }
        
        // 保存绝育状态（重要：必须保存用户选择的值）
        if (this.petData.sterilization !== undefined && this.petData.sterilization !== null) {
          updateData.sterilization = this.petData.sterilization
        }
        
        if (this.petData.weight) {
          updateData.weight = parseFloat(this.petData.weight)
        }
        
        if (this.petData.description) {
          updateData.description = this.petData.description
        }
        
        if (this.petData.species) {
          updateData.species = this.petData.species
        }
        
        if (this.petData.userId) {
          updateData.userId = parseInt(this.petData.userId) // 确保是数字类型
        }
        
        // 调用后端 API 更新
        const res = await api.updatePet(updateData)
        
        uni.hideLoading()
        
        if (res.code === 0 || res.code === 200) {
          const updatedPet = res.data || updateData
          
          // 更新本地存储
          const petList = uni.getStorageSync('petList') || []
          const petId = parseInt(this.petData.id)
          const index = petList.findIndex(p => p.id === petId)
          
          if (index !== -1) {
            const birthdayVal =
              updatedPet.birthday ||
              updatedPet.birth_date ||
              petList[index].birthday ||
              petList[index].birthDate ||
              ''
            // 合并更新后的数据
            petList[index] = {
              ...petList[index],
              ...updatedPet,
              id: petId,
              age: this.calculateAge(birthdayVal),
              companionshipDays: this.calculateCompanionshipDays(this.petData.arrivalDate),
              birthDate: birthdayVal,
              birthday: birthdayVal,
              arrivalDate: updatedPet.arrivalDate || updatedPet.arrival_date || this.petData.arrivalDate || ''
            }
            uni.setStorageSync('petList', petList)
          }
          
          uni.showToast({
            title: '保存成功',
            icon: 'success'
          })
          
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } else {
          uni.showToast({
            title: res.msg || '保存失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('保存失败:', error)
        uni.showToast({
          title: '保存失败: ' + (error.message || '网络错误'),
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

/* 页面内容（使用系统导航栏，无需顶部占位） */
.page-content {
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



.health-options {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}
.health-btn {
  padding: 12rpx 20rpx;
  border-radius: 20rpx;
  background-color: #f0f0f0;
  border: 1px solid #e0e0e0;
}
.health-btn.active {
  background-color: #ffd700;
  border-color: #ffd700;
}
.health-text {
  font-size: 22rpx;
  color: #666;
}
.health-btn.active .health-text {
  color: #333;
}

.medical-records-section {
  margin-top: 16rpx;
  padding: 24rpx 30rpx;
  border-top: 1rpx solid #eee;
}
.medical-records-section .section-title {
  margin-bottom: 12rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}
.records-loading,
.records-empty {
  font-size: 24rpx;
  color: #999;
  padding: 16rpx 0;
}
.records-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}
.record-item {
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  border-left: 4rpx solid #8D9F5E;
}
.record-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}
.record-date {
  font-size: 24rpx;
  color: #666;
}
.record-type-tag {
  font-size: 20rpx;
  color: #8D9F5E;
  background: #e8f0e8;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.record-item-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}
.record-next {
  font-size: 22rpx;
  color: #666;
  margin-top: 6rpx;
}
.record-remark {
  font-size: 22rpx;
  color: #999;
  margin-top: 6rpx;
}

/* 保存按钮 */
.save-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 88rpx;
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

/* 体重编辑弹窗 */
.weight-popup-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.weight-popup {
  width: 560rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.weight-popup-title {
  font-size: 32rpx;
  color: #333;
  text-align: center;
  margin-bottom: 30rpx;
}

.weight-popup-input-wrap {
  display: flex;
  align-items: center;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 30rpx;
}

.weight-popup-input {
  flex: 1;
  font-size: 30rpx;
  height: 60rpx;
}

.weight-popup-unit {
  font-size: 28rpx;
  color: #999;
  margin-left: 16rpx;
}

.weight-popup-btns {
  display: flex;
  gap: 24rpx;
}

.weight-popup-btn {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 30rpx;
  border-radius: 12rpx;
}

.weight-popup-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.weight-popup-btn.confirm {
  background: #ffd700;
  color: #333;
}
</style>
