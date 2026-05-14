<template>
  <view class="my-pets-container">
    <!-- 使用系统默认导航栏（含系统返回键） -->
    <!-- 页面内容 -->
    <view class="pets-content">
      <!-- 宠物信息卡片（有宠物时显示） -->
      <view v-if="hasPets">
        <view class="pet-info-card" v-for="(pet, index) in petList" :key="pet.id">
        <!-- 宠物名字 - 显示在顶部（移除编号，只显示名字） -->
        <view class="pet-name">
          <text class="name-text">{{ pet.name || '未命名宠物' }}</text>
        </view>
        
        <view class="pet-status-tag" :class="{ 'pink': pet.gender === 'female', 'blue': pet.gender === 'male' }" v-if="pet.gender">
          <text class="status-icon">{{ pet.gender === 'male' ? '♂' : '♀' }}</text>
          <text class="status-text">{{ getGenderText(pet.gender) }}</text>
        </view>
        
        <view class="pet-details">
          <view class="detail-stack">
            <view class="detail-item">
              <image class="detail-icon-img" src="/static/images/体重.png" mode="aspectFit" />
              <text class="detail-text">{{ pet.weight }}kg</text>
            </view>
            <view class="detail-item detail-item--companionship">
              <view class="detail-icon-spacer"></view>
              <text class="detail-text">{{ pet.companionshipDays }}天陪伴</text>
            </view>
          </view>
        </view>
        
        <view class="pet-avatar">
          <image 
            :src="pet.avatar || '/static/images/login-dog.png'" 
            mode="aspectFill"
            @error="handleImageError($event, pet)"
            :lazy-load="true"
          />
        </view>
        
        <view class="action-buttons">
          <view class="edit-button" @click="editPetProfile(pet)">
            <text>编辑资料 ></text>
          </view>
          <view class="delete-button" @click="confirmDeletePet(pet)">
            <text>删除</text>
          </view>
        </view>
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
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'MyPets',
  data() {
    return {
      statusBarHeight: 0,
      navBarHeight: 44,
      hasPets: false, // 控制是否显示宠物信息卡片
      petList: [],
      isLoading: false, // 防止重复加载
      lastPetsLoadTime: 0,
      _skipFirstOnShow: true // 首次 onShow 不拉数，避免与 onLoad 延后请求重复、加重首屏主线程
    }
  },
  
  computed: {
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight
    }
  },
  
  onLoad() {
    // 立即隐藏系统级加载提示
    this.hideAllLoading()
    
    // 获取系统信息
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
        this.navBarHeight = res.platform === 'ios' ? 44 : 48
      }
    })
    
    // 延后拉数据，先让分包页完成路由/首屏，降低 navigateTo:fail timeout
    this.$nextTick(() => {
      setTimeout(() => {
        this.checkPetsData(true)
      }, 80)
    })
  },
  
  onReady() {
    // 页面渲染完成后再次隐藏加载提示
    this.$nextTick(() => {
      this.hideAllLoading()
      // 延迟再次隐藏，确保覆盖系统提示
      setTimeout(() => {
        this.hideAllLoading()
      }, 50)
      setTimeout(() => {
        this.hideAllLoading()
      }, 100)
    })
  },
  
  onShow() {
    if (this._skipFirstOnShow) {
      this._skipFirstOnShow = false
      this.hideAllLoading()
      return
    }
    // 每次显示页面时静默刷新：节流 15 秒，不显示全局「加载中」，避免从编辑页返回时反复弹 loading
    const now = Date.now()
    const throttleMs = 15 * 1000
    if (now - (this.lastPetsLoadTime || 0) < throttleMs && (this.lastPetsLoadTime || 0) > 0) {
      this.hideAllLoading()
      return
    }
    this.checkPetsData(true)
    this.hideAllLoading()
  },
  
  methods: {
    // 隐藏所有加载提示
    hideAllLoading() {
      // #ifdef MP-WEIXIN
      try {
        wx.hideNavigationBarLoading()
        wx.hideLoading()
        wx.setNavigationBarLoading && wx.setNavigationBarLoading({ loading: false })
      } catch (e) {
        // 静默处理错误
      }
      // #endif
      try {
        uni.hideLoading()
        uni.hideNavigationBarLoading && uni.hideNavigationBarLoading()
      } catch (e) {
        // 静默处理错误
      }
    },
    
    // 返回上一页
    goBack() {
      uni.navigateBack()
    },
    
    // 检查宠物数据；silent 为 true 时不显示全局「加载中」（用于 onShow 静默刷新）
    async checkPetsData(silent = false) {
      if (this.isLoading) return
      
      this.isLoading = true
      const showLoading = !silent
      try {
        const userInfo = uni.getStorageSync('userInfo') || {}
        const userId = userInfo.id || userInfo.uid || uni.getStorageSync('userId')
        
        if (userId) {
          const res = await api.getPetList(userId, showLoading)
          
          if ((res.code === 0 || res.code === 200) && res.data) {
            let pets = []
            if (res.data.records) {
              pets = res.data.records
            } else if (Array.isArray(res.data)) {
              pets = res.data
            }
            
            // 转换为前端需要的格式
            this.petList = pets.map(pet => {
              // 处理名字：只处理空值情况，不强制替换用户输入的名字
              let petName = pet.name
              if (!petName || petName === 'null' || petName === 'undefined' || petName === '' || String(petName).trim() === '') {
                petName = '未命名宠物'
              } else {
                // 保留用户输入的名字，不做额外判断
                petName = String(petName).trim()
              }
              
              // 处理头像URL：只有后端返回的http://tmp/开头的才是无效路径
              let avatar = pet.avatar
              
              if (!avatar || avatar === 'null' || avatar === 'undefined' || avatar === '') {
                // 空值，使用默认图片
                avatar = '/static/images/login-dog.png'
              } else {
                // 转换为字符串
                avatar = String(avatar).trim()
                // 处理路径：本地临时路径和服务器路径都需要正确处理
                // 如果是本地选择的临时路径（wxfile://、http://tmp/等），直接使用
                // 如果是服务器路径，使用util处理
                if (avatar.startsWith('wxfile://') || avatar.startsWith('file://')) {
                  // 本地临时路径，直接使用
                } else if (avatar.startsWith('http://tmp/') || avatar.startsWith('https://tmp/')) {
                  // 后端返回的临时路径，可能是未上传成功的，使用默认图片
                  avatar = '/static/images/login-dog.png'
                } else {
                  // 服务器路径，使用util处理
                  avatar = util.getImageUrl(avatar)
                }
              }
              
              const processedPet = {
                id: pet.id,
                name: petName,
                type: pet.species || pet.type,
                breed: pet.breed,
                gender: pet.gender,
                sterilization: pet.sterilization || null, // 不要使用 mapGenderToSterilization，保持原始值或 null
                weight: pet.weight || 0,
                birthDate: pet.birthday || '',
                birthday: pet.birthday || '',
                arrivalDate: pet.arrivalDate || '', // 从后端数据获取到达日期
                age: pet.age || 0,
                companionshipDays: this.calculateCompanionshipDays(pet),
                totalFood: 0,
                avatar: avatar,
                description: pet.description || ''
              }
              
              return processedPet
            })
            
            // 同时保存到本地存储
            uni.setStorageSync('petList', this.petList)
            this.hasPets = this.petList.length > 0
            this.lastPetsLoadTime = Date.now()
            this.isLoading = false
            return
          }
        }
      } catch (error) {
        console.error('从API加载宠物列表失败:', error)
      } finally {
        // 确保无论成功失败都重置加载状态
        this.isLoading = false
      }
      
      // 如果API加载失败，从本地存储获取
      const petList = uni.getStorageSync('petList') || []
      
      // 即使是从本地存储加载，也需要确保数据格式正确
      this.petList = petList.map(pet => {
        // 处理名字：只处理空值情况，不强制替换用户输入的名字
        let petName = pet.name
        if (!petName || petName === 'null' || petName === 'undefined' || petName === '' || String(petName).trim() === '') {
          petName = '未命名宠物'
        } else {
          // 保留用户输入的名字，不做额外判断
          petName = String(petName).trim()
        }
        
        // 处理头像URL：只有后端返回的http://tmp/开头的才是无效路径
        let avatar = pet.avatar
        if (!avatar || avatar === 'null' || avatar === 'undefined' || avatar === '') {
          avatar = '/static/images/login-dog.png'
        } else {
          avatar = String(avatar).trim()
          // 处理路径：本地临时路径和服务器路径都需要正确处理
          if (avatar.startsWith('wxfile://') || avatar.startsWith('file://')) {
            // 本地临时路径，直接使用
          } else if (avatar.startsWith('http://tmp/') || avatar.startsWith('https://tmp/')) {
            // 后端返回的临时路径，使用默认图片
            avatar = '/static/images/login-dog.png'
          } else {
            // 服务器路径，使用util处理
            avatar = util.getImageUrl(avatar)
          }
        }
        
        const processedPet = {
          id: pet.id,
          name: petName,
          type: pet.species || pet.type,
          breed: pet.breed,
          gender: pet.gender,
          sterilization: pet.sterilization || this.mapGenderToSterilization(pet.gender),
          weight: pet.weight || 0,
          birthDate: pet.birthday || pet.birthDate || '',
          birthday: pet.birthday || pet.birthDate || '',
          arrivalDate: pet.arrivalDate || '',
          age: pet.age || 0,
          companionshipDays: this.calculateCompanionshipDays(pet),
          totalFood: pet.totalFood || 0,
          avatar: avatar,
          description: pet.description || ''
        }
        
        return processedPet
      })
      
      this.hasPets = this.petList.length > 0
      this.isLoading = false
    },
    
    // 映射性别到绝育状态（如果后端没有返回，则根据性别简单判断）
    mapGenderToSterilization(gender) {
      // 如果没有后端数据，返回 null，不要默认返回 unknown
      // 实际应该从数据库获取真实的绝育状态
      return null // 返回 null，让前端显示空值或默认值，而不是硬编码"不清楚"
    },
    
    // 计算陪伴天数
    calculateCompanionshipDays(pet) {
      // 优先使用后端返回的陪伴天数
      if (pet.companionshipDays !== undefined && pet.companionshipDays !== null) {
        return parseInt(pet.companionshipDays) || 0
      }
      
      // 如果没有，使用createTime作为陪伴开始时间
      if (pet.createTime) {
        try {
          // 使用util.parseDate来兼容iOS日期格式
          const createTime = util.parseDate(pet.createTime)
          if (!createTime) return 0
          const now = new Date()
          const diff = now.getTime() - createTime.getTime()
          return Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)))
        } catch (e) {
          console.error('计算陪伴天数失败:', e)
          return 0
        }
      }
      
      // 如果有arrivalDate，使用它
      if (pet.arrivalDate) {
        try {
          // 使用util.parseDate来兼容iOS日期格式
          const arrival = util.parseDate(pet.arrivalDate)
          if (!arrival) return 0
          const now = new Date()
          const diff = now.getTime() - arrival.getTime()
          return Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)))
        } catch (e) {
          console.error('计算陪伴天数失败:', e)
          return 0
        }
      }
      
      return 0
    },
    
    // 添加宠物档案
    addPetProfile() {
      uni.navigateTo({
        url: '/user/add-pet-basic'
      })
    },
    
    // 编辑宠物资料
    editPetProfile(pet) {
      const id = pet && pet.id !== undefined && pet.id !== null ? pet.id : ''
      if (id === '' || String(id) === 'undefined') {
        uni.showToast({
          title: '宠物信息异常，请返回重试',
          icon: 'none'
        })
        return
      }
      uni.navigateTo({
        url: `/user/edit-pet-profile?petId=${encodeURIComponent(String(id))}`
      })
    },
    
    // 获取性别文本
    getGenderText(gender) {
      if (!gender) return ''
      if (gender === 'male' || gender === '男' || gender === '♂') {
        return '男孩子'
      } else if (gender === 'female' || gender === '女' || gender === '♀') {
        return '女孩子'
      }
      return gender
    },
    
    // 处理图片加载错误（统一回退到 login-dog，避免 404）
    handleImageError(e, pet) {
      const currentAvatar = pet.avatar || ''
      if (currentAvatar.includes('login-dog.png')) return
      pet.avatar = '/static/images/login-dog.png'
    },
    
    // 确认删除宠物
    confirmDeletePet(pet) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除宠物"${pet.name || '未命名宠物'}"吗？此操作不可恢复。`,
        confirmText: '删除',
        confirmColor: '#ff3b30',
        success: (res) => {
          if (res.confirm) {
            this.deletePet(pet)
          }
        }
      })
    },
    
    // 删除宠物
    async deletePet(pet) {
      try {
        uni.showLoading({
          title: '删除中...',
          mask: true
        })
        
        // 调用后端API删除
        const res = await api.deletePet(pet.id)
        
        uni.hideLoading()
        
        
        // 后端返回 code: 200 表示成功，data 是 true/false
        if ((res.code === 0 || res.code === 200) && res.data === true) {
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
          
          // 从本地列表移除，不需要重新调用接口
          this.petList = this.petList.filter(p => p.id !== pet.id)
          this.hasPets = this.petList.length > 0
          uni.setStorageSync('petList', this.petList)
        } else {
          uni.showToast({
            title: res.msg || '删除失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('删除宠物失败:', error)
        
        uni.showToast({
          title: '删除失败: ' + (error.message || '网络错误'),
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.my-pets-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

/* 页面内容 */
.pets-content {
  padding: 20rpx;
}

/* 宠物信息卡片 */
.pet-info-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  position: relative;
}

.pet-name {
  position: absolute;
  top: 24rpx;
  left: 24rpx;
  right: 100rpx;
}

.name-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pet-status-tag {
  position: absolute;
  top: 60rpx;
  left: 24rpx;
  border-radius: 16rpx;
  padding: 6rpx 12rpx;
  display: flex;
  align-items: center;
  gap: 6rpx;
  /* 默认蓝色（男孩子） */
  background: #e3f2fd;
}

.pet-status-tag.pink {
  /* 粉色（女孩子） */
  background: #ff69b4;
}

.pet-status-tag.blue {
  /* 蓝色（男孩子） */
  background: #e3f2fd;
}

.status-icon {
  font-size: 18rpx;
}

.pet-status-tag.pink .status-icon,
.pet-status-tag.pink .status-text {
  color: #fff;
}

.pet-status-tag.blue .status-icon,
.pet-status-tag.blue .status-text {
  color: #1976d2;
}

.status-text {
  font-size: 20rpx;
}

.pet-details {
  margin-top: 100rpx;
  margin-right: 100rpx;
}

/* 体重 + 陪伴两行紧贴，不再中间空一行 */
.detail-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.detail-stack .detail-item {
  margin-bottom: 0;
}

.detail-stack .detail-item:first-child {
  margin-bottom: 4rpx;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
  gap: 10rpx;
}

.detail-item--companionship {
  margin-bottom: 0;
}

/* 与上一行体重图标列宽对齐 */
.detail-icon-spacer {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}

.detail-icon {
  font-size: 22rpx;
  width: 26rpx;
  text-align: center;
}

.detail-icon-img {
  width: 32rpx;
  height: 32rpx;
  flex-shrink: 0;
}

.detail-text {
  font-size: 24rpx;
  color: #333;
}

.divider {
  height: 1rpx;
  background: #f0f0f0;
  margin: 16rpx 0;
}

.pet-avatar {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 2rpx solid #fff;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
  background: #f0f0f0;
}

.pet-avatar image {
  width: 100%;
  height: 100%;
}

.action-buttons {
  position: absolute;
  bottom: 24rpx;
  right: 24rpx;
  display: flex;
  gap: 12rpx;
  align-items: center;
}

.edit-button {
  background: #fff3cd;
  color: #856404;
  padding: 10rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  border: 1rpx solid #ffeaa7;
}

.delete-button {
  background: #ffebee;
  color: #c62828;
  padding: 10rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  border: 1rpx solid #ffcdd2;
}

.delete-button:active {
  background: #ffcdd2;
}

/* 添加宠物档案按钮容器：与宠物信息卡片同宽，不额外左右留白（pets-content 已有 20rpx） */
.add-pet-container {
  padding-bottom: 20rpx;
  margin-top: 0;
}

/* 添加宠物档案按钮：与宠物信息框左右对齐、同宽 */
.add-pet-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  transition: background-color 0.2s ease;
  width: 100%;
  box-sizing: border-box;
}

.add-pet-card:active {
  background-color: #f8f8f8;
}

.add-icon {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #666;
}

.add-text {
  flex: 1;
}

.add-text text {
  font-size: 24rpx;
  color: #333;
  font-weight: 500;
}
</style>
