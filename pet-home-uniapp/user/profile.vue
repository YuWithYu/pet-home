<template>
  <view class="profile-container">
    <!-- 背景图区�?- 从顶部开始，覆盖状态栏（高度加大以让背景图显示更大�?-->
    <view class="background-section" :style="{ height: (420 + statusBarHeight * 2) + 'rpx', marginTop: '-' + statusBarHeight + 'px', paddingTop: statusBarHeight + 'px' }">
      <view class="background-image-wrapper">
        <!-- 背景图片 -->
        <image 
          v-if="backgroundImage" 
          :src="getBackgroundImageUrl(backgroundImage)" 
          class="background-image"
          mode="aspectFill"
          @error="handleBackgroundImageError"
        />
        <!-- 默认渐变背景 -->
        <view v-else class="background-image-gradient"></view>
        
        <!-- 返回按钮 - 位于左上角（考虑状态栏高度�?-->
        <view class="back-button" :style="{ top: statusBarHeight + 20 + 'px' }" @click="goBack">
          <text class="back-icon-white">‹</text>
        </view>
        <!-- 上传背景图按�?- 位于右下角（仅在自己的主页显示） -->
        <view v-if="!targetUserId || targetUserId === currentUserId" class="background-upload-btn" @click="uploadBackground">
          <image src="/static/images/图片.png" mode="aspectFit" class="upload-icon-image" />
        </view>
      </view>
    </view>

    <!-- 用户信息卡片 -->
    <view class="user-info-card">
      <view class="user-avatar-section">
        <view class="user-avatar" :class="{ 'avatar-clickable': !targetUserId || targetUserId === currentUserId }" @click="(!targetUserId || targetUserId === currentUserId) && chooseAvatar()">
          <image :src="getAvatarUrl(displayUserInfo.avatar)" mode="aspectFill" class="avatar-image" @error="handleAvatarError" />
        </view>
        <!-- 用户信息在头像下�?-->
        <view class="user-info-below-avatar">
          <view class="user-name-row">
            <text class="user-name">{{ displayUserInfo.nickname || '未设置昵称' }}</text>
            <text class="divider">|</text>
            <view class="gender-icon" :class="{ 'gender-female': isFemale(displayUserInfo.gender), 'gender-male': !isFemale(displayUserInfo.gender) }">{{ getUserGenderIcon(displayUserInfo.gender) }}</view>
            <text class="divider">|</text>
            <view class="points-info">
              <image src="/static/images/my-cans.png" mode="aspectFit" class="points-can-icon" />
              <text class="points-text">{{ formatPoints(displayUserInfo.points) }}</text>
            </view>
          </view>
          <view class="user-bio">
            <text class="bio-text">{{ displayUserInfo.signature || displayUserInfo.bio || '这个人很懒，什么都没留下~' }}</text>
          </view>
        </view>
        <!-- 仅查看他人时显示关注、私信按�?-->
        <view v-if="targetUserId && targetUserId !== currentUserId" class="edit-profile-section">
          <view class="action-buttons-wrapper">
            <view 
              class="follow-btn" 
              :class="{ 'followed': isFollowingUser }"
              @click="toggleFollow"
            >
              <text class="follow-text">{{ isFollowingUser ? '已关注' : '关注' }}</text>
            </view>
            <view class="chat-btn" @tap.stop="goToChat">
              <image class="chat-icon-image" src="/static/images/私信.png" mode="aspectFit"></image>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 统计数据 -->
      <view class="user-stats">
        <view class="stat-item" @click="gotoFollowingTabs('following')">
          <text class="stat-number">{{ userStats.follows }}</text>
          <text class="stat-label">关注</text>
        </view>
        <view class="stat-item" @click="gotoFollowingTabs('fans')">
          <text class="stat-number">{{ userStats.fans }}</text>
          <text class="stat-label">粉丝</text>
        </view>
        <view class="stat-item">
          <text class="stat-number">{{ userStats.likes }}</text>
          <text class="stat-label">获赞</text>
        </view>
      </view>
    </view>

    <!-- 导航标签 -->
    <view class="nav-tabs">
      <view class="tab-item active">
        <text class="tab-text">{{ dynamicsTabLabel }}</text>
        <view class="tab-underline"></view>
      </view>
    </view>

    <!-- 动态内�?- 发现页同款双列瀑布流，仅展示当前用户的动�?-->
    <view>
      <view v-if="loadingDynamics" class="loading-state">
        <text>加载中...</text>
      </view>
      <view v-else-if="dynamicsList.length > 0" class="dynamics-waterfall-wrap">
        <scroll-view class="discovery-scroll dynamics-scroll" scroll-y>
          <view class="dis-list">
            <view class="dynamics-col-0">
              <view 
                v-for="(item, index) in leftDynamicsList" 
                :key="10000 + index"
                :id="'waterfall-dynamics-0-' + index"
                class="waterfall-item"
                v-if="item"
                @tap="goToPostDetail(dynamicsPost(item))"
              >
                <view class="note_item">
                  <view class="img">
                    <image 
                      :src="item.img || getImageUrl('/static/images/default-product.svg')" 
                      mode="widthFix" 
                      data-column="0"
                      :data-index="index"
                      @error="handleImageError"
                      @load="onDynamicsWaterfallImageLoad($event)"
                    />
                    <view v-if="item.isVideo" class="video-play-icon-small">▶</view>
                  </view>
                  <view class="desc">
                    <text>{{ item.desc }}</text>
                  </view>
                  <view class="note">
                    <view class="user">
                      <image :src="item.avator" mode="aspectFill" />
                      <text>{{ item.uname }}</text>
                    </view>
                    <view 
                      class="like" 
                      :data-post-id="dynamicsPostId(item)"
                      data-column="0"
                      :data-index="index"
                      @click.stop="handleLikeInDynamicsWaterfall($event)"
                    >
                      <image 
                        class="like-heart" 
                        :class="{ liked: dynamicsItemLiked(item) }"
                        :src="dynamicsItemLiked(item) ? '/static/images/点赞后.png' : '/static/images/点赞前.png'"
                        mode="aspectFit"
                      />
                      <text class="like-num">{{ dynamicsItemLikeCount(item) }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
            <view class="dynamics-col-1">
              <view 
                v-for="(item, index) in rightDynamicsList" 
                :key="20000 + index"
                :id="'waterfall-dynamics-1-' + index"
                class="waterfall-item"
                v-if="item"
                @tap="goToPostDetail(dynamicsPost(item))"
              >
                <view class="note_item">
                  <view class="img">
                    <image 
                      :src="item.img || getImageUrl('/static/images/default-product.svg')" 
                      mode="widthFix" 
                      data-column="1"
                      :data-index="index"
                      @error="handleImageError"
                      @load="onDynamicsWaterfallImageLoad($event)"
                    />
                    <view v-if="item.isVideo" class="video-play-icon-small">▶</view>
                  </view>
                  <view class="desc">
                    <text>{{ item.desc }}</text>
                  </view>
                  <view class="note">
                    <view class="user">
                      <image :src="item.avator" mode="aspectFill" />
                      <text>{{ item.uname }}</text>
                    </view>
                    <view 
                      class="like" 
                      :data-post-id="dynamicsPostId(item)"
                      data-column="1"
                      :data-index="index"
                      @click.stop="handleLikeInDynamicsWaterfall($event)"
                    >
                      <image 
                        class="like-heart" 
                        :class="{ liked: dynamicsItemLiked(item) }"
                        :src="dynamicsItemLiked(item) ? '/static/images/点赞后.png' : '/static/images/点赞前.png'"
                        mode="aspectFit"
                      />
                      <text class="like-num">{{ dynamicsItemLikeCount(item) }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
      <view v-else class="empty-state">
        <image src="/static/images/暂无动态.png" mode="aspectFit" class="empty-icon-image" />
        <text class="empty-text">暂无动态内容</text>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters } from 'vuex'
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'Profile',
  data() {
    return {
      statusBarHeight: 0,
      targetUserId: null,
      petName: '',
      backgroundImage: '',
      currentTab: 'dynamics',
      userStats: {
        follows: 0,
        fans: 0,
        dynamics: 0,
        likes: 0
      },
      petList: [],
      userData: {},
      isFollowingUser: false,
      currentUserId: null, // 当前登录用户ID
      dynamicsList: [],
      articlesList: [],
      loadingDynamics: false,
      loadingArticles: false,
      leftDynamicsList: [],
      rightDynamicsList: [],
      leftDynamicsColumnHeight: 0,
      rightDynamicsColumnHeight: 0,
      pendingDynamicsItems: []
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    dynamicsTabLabel() {
      return (this.targetUserId && this.targetUserId !== this.currentUserId) ? 'TA的动态' : '动态'
    },
    // 合并vuex中的userInfo和API返回的userData
    displayUserInfo() {
      // 【关键修复】如果查看其他用户的资料（targetUserId存在），完全使用API返回的userData，不合并userInfo
      if (this.targetUserId && this.targetUserId > 0) {
        const userData = this.userData || {}
        
        const result = { ...userData }
        if (result.points === null || result.points === undefined) {
          result.points = 0
        }
        return result
      }
      
      // 查看自己的资料时，合并vuex中的userInfo和API返回的userData
      const userInfo = this.userInfo || {}
      const userData = this.userData || {}
      const merged = { ...userInfo }
      
      Object.keys(userData).forEach(key => {
        const value = userData[key]
        // 对于 signature �?bio 字段，即使为空字符串也要更新（允许清空签名）
        if (key === 'signature' || key === 'bio') {
          merged[key] = value !== undefined ? value : merged[key]
        }
        else if (key === 'gender') {
          merged[key] = value !== undefined && value !== null ? value : merged[key]
        }
        else if (value !== null && value !== undefined && value !== '') {
          merged[key] = value
        } else if (merged[key] === undefined || merged[key] === null || merged[key] === '') {
          merged[key] = value
        }
        // 否则保持merged中的原有值（不覆盖）
      })
      
      // 确保 signature �?bio 字段的优先级：signature > bio
      if (merged.signature !== undefined && merged.signature !== null) {
        merged.bio = merged.signature
      } else if (merged.bio !== undefined && merged.bio !== null) {
        merged.signature = merged.bio
      }
      
      if (merged.points === null || merged.points === undefined) {
        merged.points = 0
      }
      
      return merged
    }
  },
  onLoad(options) {
    // 获取系统信息
    uni.getSystemInfo({
      success: (res) => {
        this.statusBarHeight = res.statusBarHeight || 0
      }
    })
    
    // 获取当前登录用户ID
    this.currentUserId = this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId') || null
    
    // 获取传入的用户ID（如果是从其他页面跳转过来的�?    
    if (options && options.userId) {
      // 将字符串转换为数字（URL参数通常是字符串
      let parsedUserId = options.userId
      if (typeof options.userId === 'string') {
        parsedUserId = parseInt(options.userId)
        if (isNaN(parsedUserId)) {
          console.error('无法解析用户ID参数:', options.userId)
          uni.showToast({
            title: '无效的用户ID',
            icon: 'none'
          })
          this.targetUserId = null
        } else {
          this.targetUserId = parsedUserId
        }
      } else {
        this.targetUserId = parsedUserId
      }
      
      if (this.targetUserId && this.targetUserId > 0) {
      } else {
        // 静默处理警告
        this.targetUserId = null
      }
    } else {
      // 如果没有传入userId，则显示当前登录用户的资
      this.targetUserId = null
    }
    
    // 加载用户数据
    this.loadUserData()
  },
  
  onShow() {
    // 页面显示时不需要自动刷新，避免丢失点赞状�?    // 如果需要刷新，用户可以手动下拉刷新
    
    // 监听用户信息更新事件
    uni.$on('userInfoUpdated', () => {
      // 立即更新 userData 中的关键字段（从本地存储获取最新值）
      const latestUserInfo = uni.getStorageSync('userInfo') || {}
      if (latestUserInfo.signature !== undefined) {
        this.userData = { ...this.userData, signature: latestUserInfo.signature }
      }
      if (latestUserInfo.gender !== undefined) {
        this.userData = { ...this.userData, gender: latestUserInfo.gender }
      }
      // 然后重新加载完整数据
      this.loadUserData()
    })
  },
  onUnload() {
    // 移除事件监听
    uni.$off('userInfoUpdated')
  },
  methods: {
    // 获取头像URL（处理本地临时路径和服务器路径）
    getAvatarUrl(avatar) {
      if (!avatar) {
        return '/static/images/login-dog.png'
      }
      
      // 如果是前端静态资源（默认头像），直接返回，不经过服务
      if (avatar === '/static/images/login-dog.png' || avatar.includes('login-dog.png')) {
        return '/static/images/login-dog.png'
      }
      
      // 如果是本地临时路径（wxfile:// �?file://），直接使用
      if (avatar.startsWith('wxfile://') || avatar.startsWith('file://')) {
        return avatar
      }
      
      // 如果是服务器路径，使用util处理
      return util.getImageUrl(avatar)
    },
    
    // 获取背景图URL（处理本地临时路径和服务器路径）
    getBackgroundImageUrl(bgImage) {
      if (!bgImage) {
        return ''
      }
      
      // 如果是本地临时路径（wxfile:// 或 file://），直接使用
      if (bgImage.startsWith('wxfile://') || bgImage.startsWith('file://')) {
        return bgImage
      }
      
      // 如果是服务器路径，使用util处理
      return util.getImageUrl(bgImage)
    },
    
    handleBackgroundImageError(e) {
      console.error('背景图加载失败:', this.backgroundImage, e)
      // 背景图加载失败时，使用默认渐变背
      this.backgroundImage = ''
    },
    
    // 处理头像加载失败
    handleAvatarError(e) {
      const currentAvatar = this.displayUserInfo?.avatar || ''
      if (currentAvatar.includes('login-dog.png')) return
      if (this.userData) {
        this.userData.avatar = '/static/images/login-dog.png'
      }
    },
    
    // 安全获取图片URL（确保URL格式正确�?
    getSafeImageUrl(img) {
      if (!img) {
        return '/static/images/garfield-default-avatar.png'
      }
      
      // 转换为字符串
      let imgStr = String(img).trim()
      
      // 如果包含URL编码的引号（%22），先解
      if (imgStr.includes('%22') || imgStr.includes('%27')) {
        try {
          imgStr = decodeURIComponent(imgStr)
        } catch (e) {
          // 静默处理错误
        }
      }
      
      // 如果�?[ 开头和 ] 结尾，说明可能是数组格式的字符串，尝试提取URL
      if (imgStr.startsWith('[') && imgStr.endsWith(']')) {
        try {
          const parsed = JSON.parse(imgStr)
          if (Array.isArray(parsed) && parsed.length > 0) {
            imgStr = String(parsed[0]).trim()
          } else {
            return '/static/images/garfield-default-avatar.png'
          }
        } catch (e) {
          // 如果JSON解析失败，尝试提取引号内的内
      const match = imgStr.match(/["']([^"']+)["']/)
          if (match && match[1]) {
            imgStr = match[1].trim()
          } else {
            return '/static/images/garfield-default-avatar.png'
          }
        }
      }
      
      // 去掉首尾的引号（如果存在�?      imgStr = imgStr.replace(/^["']|["']$/g, '').trim()
      
      // 如果处理后为空，返回默认图片
      if (!imgStr || imgStr === 'null' || imgStr === 'undefined') {
        return '/static/images/garfield-default-avatar.png'
      }
      
      // 使用util.getImageUrl处理
      return util.getImageUrl(imgStr)
    },
    
    // 处理动态图片加载错误（小程序中 e.target.style 可能不存在，避免报错�?
    handleImageError(e) {
      try {
        if (e && e.target && e.target.style) {
          e.target.style.display = 'none'
        }
      } catch (err) {
        // 小程序环境下忽略
      }
    },
    
    // 返回上一�?
    goBack() {
      uni.navigateBack()
    },

    // 跳转到关�?粉丝/动态列�?
    gotoFollowingTabs(tabType) {
      const currentUserId = this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId') || null
      
      // 确定要查看的用户ID：如果正在查看其他用户的资料，使用targetUserId；否则使用当前登录用户ID
      let ownerId = null
      let ownerName = '用户'
      
      if (this.targetUserId && this.targetUserId !== currentUserId) {
        ownerId = typeof this.targetUserId === 'string' ? parseInt(this.targetUserId) : this.targetUserId
        ownerName = this.userData?.nickname || this.userData?.username || '用户'
      } else {
        ownerId = currentUserId
        const currentUsername = this.userInfo?.username || uni.getStorageSync('username') || '用户'
        ownerName = this.userData?.nickname || currentUsername || '用户'
      }
      
      if (!ownerId) {
        uni.showToast({
          title: '无法获取用户信息',
          icon: 'none'
        })
        return
      }
      
      uni.navigateTo({
        url: `/user/FollowingTabs?ownerId=${ownerId}&ownerName=${encodeURIComponent(ownerName)}&defaultTab=${tabType}`
      })
    },

    // 加载用户数据
    loadUserData() {
      // 确定要加载的用户ID：如果指定了targetUserId，则加载该用户的资料；否则加载当前登录用户的资料
      // 注意：targetUserId 可能是字符串（从URL参数获取），需要转换为数字
      let targetId = null
      if (this.targetUserId) {
        // 如果targetUserId存在，优先使用它（可能是字符串，需要转换）
        targetId = typeof this.targetUserId === 'string' ? parseInt(this.targetUserId) : this.targetUserId
      } else {
        // 如果没有指定targetUserId，使用当前登录用户的ID
        targetId = this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId') || null
      }
      
      const currentUserId = this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId') || null
      // 查询时使用username（因为登录是用username的），显示时用nickname
      const currentUsername = this.userInfo?.username || uni.getStorageSync('username') || null
      
      // 如果没有目标用户ID，说明未登录且没有指定用
      if (!targetId || isNaN(targetId)) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateTo({
            url: '/pages-auth/login'
          })
        }, 1500)
        return Promise.reject(new Error('未登录且未指定用户ID'))
      }
      
      // 构建请求参数
      const params = {}
      if (targetId) {
        params.userId = targetId
      } else if (currentUsername) {
        params.username = currentUsername
      }
      
      // 加载用户基本信息
      return api.getCurrentUser(params)
        .then(res => {
          if ((res.code === 200 || res.code === 0) && res.data) {
            this.userData = res.data

            if (targetId && targetId !== currentUserId && currentUserId) {
              this.checkFollowStatus(targetId, currentUserId)
            }

            if (res.data.backgroundImage) {
              let bgUrl = res.data.backgroundImage
              this.backgroundImage = bgUrl
            } else {
              if (!this.backgroundImage) {
                this.backgroundImage = ''
              }
            }

            const userId = targetId || res.data.id || res.data.uid || currentUserId || null
            const petUserId = userId ? (typeof userId === 'string' ? parseInt(userId) : userId) : null

            api.getPetList(petUserId)
              .then(petRes => {
                if ((petRes.code === 200 || petRes.code === 0) || petRes.success) {
                  let pets = []
                  if (petRes.data && petRes.data.records) {
                    pets = petRes.data.records || []
                  } else if (Array.isArray(petRes.data)) {
                    pets = petRes.data
                  } else if (petRes.data) {
                    pets = []
                  }
                  this.petList = pets.map(pet => this.formatPetDataForProfile(pet)).filter(pet => pet && pet.id)
                  this.loadTabContent(this.currentTab)
                } else {
                  this.petList = []
                }
              })
              .catch(err => {
                this.petList = []
              })
            
            // 加载用户统计信息（优先使用targetId，因为这是我们要查看的用户）
            const statsUserId = targetId || (userId ? (typeof userId === 'string' ? parseInt(userId) : userId) : null)
            if (statsUserId) {
              api.getUserStats(statsUserId)
                .then(res => {
                  if ((res.code === 200 || res.code === 0) && res.data) {
                    this.userStats = {
                      follows: res.data.follows || res.data.followCount || 0,
                      fans: res.data.fans || res.data.fansCount || 0,
                      dynamics: res.data.dynamics || res.data.postCount || 0,
                      likes: res.data.likes || res.data.likeCount || 0
                    }
                  }
                })
                .catch(err => {
                  // 静默处理错误
                })
            }
          }
        })
        .catch(err => {
          console.error('加载用户信息失败:', err)
          // 即使获取用户信息失败，也尝试使用当前登录的userId加载宠物列表
          const fallbackUserId = currentUserId || uni.getStorageSync('userId') || null
          
          if (fallbackUserId) {
            const petUserId = typeof fallbackUserId === 'string' ? parseInt(fallbackUserId) : fallbackUserId

            api.getPetList(petUserId)
              .then(res => {
                if ((res.code === 200 || res.code === 0) || res.success) {
                  let pets = []
                  if (res.data && res.data.records) {
                    pets = res.data.records || []
                  } else if (Array.isArray(res.data)) {
                    pets = res.data
                  } else {
                    pets = []
                  }
                  this.petList = pets.map(pet => this.formatPetDataForProfile(pet)).filter(pet => pet && pet.id)
                  this.loadTabContent(this.currentTab)
                } else {
                  this.petList = []
                }
              })
              .catch(petErr => {
                this.petList = []
              })
            
            // 使用备用userId加载统计信息
            api.getUserStats(petUserId)
              .then(res => {
                if ((res.code === 200 || res.code === 0) && res.data) {
                  this.userStats = {
                    follows: res.data.follows || res.data.followCount || 0,
                    fans: res.data.fans || res.data.fansCount || 0,
                    dynamics: res.data.dynamics || res.data.postCount || 0,
                    likes: res.data.likes || res.data.likeCount || 0
                  }
                }
              })
              .catch(err => {
                // 静默处理错误
              })
          } else {
            this.petList = []
          }
        })
    },

    // 搜索宠物
    searchPet() {
      if (this.petName.trim()) {
        uni.showToast({
          title: `搜索宠物: ${this.petName}`,
          icon: 'none'
        })
      }
    },

    // 显示更多选项
    showMoreOptions() {
      uni.showActionSheet({
        itemList: ['分享', '设置', '帮助'],
        success: (res) => {
          switch (res.tapIndex) {
            case 0:
              this.shareProfile()
              break
            case 1:
              this.goToSettings()
              break
            case 2:
              this.showHelp()
              break
          }
        }
      })
    },

    // 显示目标
    showTarget() {
      uni.showToast({
        title: '目标功能开发中...',
        icon: 'none'
      })
    },


      // 上传背景图：先选图，再裁剪（支持用户选择要展示的部分），再上�?
      uploadBackground() {
        uni.chooseImage({
          count: 1,
          sizeType: ['original', 'compressed'],
          sourceType: ['album', 'camera'],
          success: (res) => {
            const tempPath = res.tempFilePaths[0]
            // #ifdef MP-WEIXIN
            // 微信小程序：调用原生裁剪（基础?2.26.0+），16:9 横屏比例适合背景
      if (typeof wx !== 'undefined' && wx.cropImage) {
              wx.cropImage({
                src: tempPath,
                cropScale: '16:9',
                success: (cropRes) => {
                  this.uploadBackgroundImage(cropRes.tempFilePath)
                },
                fail: (err) => {
                  console.warn('裁剪取消或失败，直接上传原图:', err)
                  this.uploadBackgroundImage(tempPath)
                }
              })
            } else {
              this.uploadBackgroundImage(tempPath)
            }
            // #endif
            // #ifndef MP-WEIXIN
            this.uploadBackgroundImage(tempPath)
            // #endif
          },
          fail: (err) => {
            console.error('选择背景图失败:', err)
            uni.showToast({
              title: '选择背景图失败',
              icon: 'none'
            })
          }
        })
      },

      uploadBackgroundImage(filePath) {
        uni.showLoading({
          title: '上传中...',
          mask: true
        })

        uni.uploadFile({
          url: api.baseURL + '/api/upload/image',
          filePath: filePath,
          name: 'file',
          formData: {
            type: 'background'
          },
          success: (res) => {
            try {
              const data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
              if ((data.code === 0 || data.code === 200) && data.data) {
                let imageUrl = typeof data.data === 'string' ? data.data : (data.data.url || '')

                this.backgroundImage = imageUrl

                const userId = this.userData?.id || this.userData?.uid || uni.getStorageSync('userId')
                if (!userId) {
                  uni.hideLoading()
                  uni.showToast({
                    title: '无法获取用户ID',
                    icon: 'none'
                  })
                  return
                }
                api.updateUserProfile({
                  id: userId,
                  backgroundImage: imageUrl
                })
                  .then(() => {
                    if (this.userData) {
                      this.userData.backgroundImage = imageUrl
                    }
                    this.backgroundImage = imageUrl
                    uni.hideLoading()
                    uni.showToast({
                      title: '背景图上传成功',
                      icon: 'success'
                    })
                    const savedBgImage = imageUrl
                    this.loadUserData().then(() => {
                      if (savedBgImage) {
                        this.backgroundImage = savedBgImage
                      }
                    })
                  })
                  .catch((err) => {
                    uni.hideLoading()
                    console.error('保存背景图失败:', err)
                    uni.showToast({
                      title: '背景图上传成功，但保存失败',
                      icon: 'none'
                    })
                  })
              } else {
                uni.hideLoading()
                console.error('上传背景失败:', data)
                uni.showToast({
                  title: data.msg || '上传失败',
                  icon: 'none',
                  duration: 2000
                })
              }
            } catch (e) {
              uni.hideLoading()
              console.error('解析上传响应失败:', e, res)
              uni.showToast({
                title: '上传失败：响应格式错误',
                icon: 'none',
                duration: 2000
              })
            }
          },
          fail: (err) => {
            uni.hideLoading()
            console.error('上传背景失败:', err)
            uni.showToast({
              title: '上传失败：' + (err.errMsg || '网络错误'),
              icon: 'none',
              duration: 2000
            })
          }
        })
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

    // 从相册选择（选图后可裁剪�?:1 方形适合头像�?
    chooseFromAlbum() {
      uni.chooseImage({
        count: 1,
        sizeType: ['original', 'compressed'],
        sourceType: ['album'],
        success: (res) => {
          this._cropThenUpdateAvatar(res.tempFilePaths[0])
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
          this._cropThenUpdateAvatar(res.tempFilePaths[0])
        }
      })
    },

    // 微信小程序裁剪后上传头像，其它平台直接上�?
    _cropThenUpdateAvatar(tempPath) {
      // #ifdef MP-WEIXIN
      if (typeof wx !== 'undefined' && wx.cropImage) {
        wx.cropImage({
          src: tempPath,
          cropScale: '1:1',
          success: (cropRes) => this.updateAvatar(cropRes.tempFilePath),
          fail: () => this.updateAvatar(tempPath)
        })
      } else {
        this.updateAvatar(tempPath)
      }
      // #endif
      // #ifndef MP-WEIXIN
      this.updateAvatar(tempPath)
      // #endif
    },

      // 更新头像
      async updateAvatar(avatarPath) {
        uni.showLoading({
          title: '上传中...',
          mask: true
        })

        try {
          const uploadRes = await new Promise((resolve, reject) => {
            uni.uploadFile({
              url: api.baseURL + '/api/upload/image',
              filePath: avatarPath,
              name: 'file',
              formData: {
                type: 'avatar'
              },
              success: resolve,
              fail: reject
            })
          })

          let data
          try {
            data = typeof uploadRes.data === 'string' ? JSON.parse(uploadRes.data) : uploadRes.data
          } catch (parseError) {
            console.error('解析上传响应失败:', parseError, uploadRes)
            uni.showToast({
              title: '上传失败：响应格式错误',
              icon: 'none',
              duration: 2000
            })
            return
          }

          if (!(data && (data.code === 0 || data.code === 200) && data.data)) {
            uni.showToast({
              title: (data && (data.msg || data.message)) || '上传失败',
              icon: 'none',
              duration: 2000
            })
            return
          }

          let imageUrl = typeof data.data === 'string' ? data.data : (data.data.url || '')

          if (!imageUrl) {
            uni.showToast({
              title: '上传失败：未返回头像地址',
              icon: 'none'
            })
            return
          }

          if (this.userData) {
            this.userData.avatar = imageUrl
          }

          const userId = this.userData?.id || this.userData?.uid || uni.getStorageSync('userId')
          if (!userId) {
            uni.showToast({
              title: '无法获取用户ID',
              icon: 'none'
            })
            return
          }

          const saveReq = api.updateUserProfile({
            id: userId,
            avatar: imageUrl
          })
          const timeoutReq = new Promise((_, reject) => {
            setTimeout(() => reject(new Error('保存头像超时')), 15000)
          })
          await Promise.race([saveReq, timeoutReq])

          uni.showToast({
            title: '头像更新成功',
            icon: 'success'
          })
          this.loadUserData()
        } catch (err) {
          console.error('上传头像失败:', err)
          uni.showToast({
            title: '上传失败：' + ((err && err.errMsg) || err.message || '网络错误'),
            icon: 'none',
            duration: 2000
          })
        } finally {
          uni.hideLoading()
        }
      },

    // 编辑资料（用 redirectTo 替代 navigateTo，避免「套两层」需返回两次�?
    editProfile() {
      uni.redirectTo({
        url: '/user/edit-profile',
        fail: (err) => {
          console.error('跳转失败:', err)
        }
      })
    },

    // 显示用户选项
    showUserOptions() {
      uni.showActionSheet({
        itemList: ['编辑资料', '隐私设置', '账号管理'],
        success: (res) => {
          switch (res.tapIndex) {
            case 0:
              this.editProfile()
              break
            case 1:
              this.goToPrivacySettings()
              break
            case 2:
              this.goToAccountManagement()
              break
          }
        }
      })
    },
    
    // 检查关注状
    async checkFollowStatus(targetUserId, currentUserId) {
      if (!targetUserId || !currentUserId) {
        this.isFollowingUser = false
        return
      }
      try {
        const response = await api.checkFollowStatus(currentUserId, targetUserId)
        if (response && (response.code === 200 || response.code === 0)) {
          this.isFollowingUser = response.data || false
        } else {
          this.isFollowingUser = false
        }
      } catch (error) {
        console.error('检查关注状态失败:', error)
        this.isFollowingUser = false
      }
    },
    
    // 切换关注状
    async toggleFollow() {
      if (!this.targetUserId || !this.currentUserId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }
      
      if (this.targetUserId === this.currentUserId) {
        uni.showToast({
          title: '不能关注自己',
          icon: 'none'
        })
        return
      }
      
      try {
        let result
        if (this.isFollowingUser) {
          // 取消关注
          result = await api.unfollowUser(this.targetUserId, this.currentUserId)
        } else {
          // 关注
          result = await api.followUser(this.targetUserId, this.currentUserId)
        }
        
        if (result && (result.code === 200 || result.code === 0)) {
          this.isFollowingUser = !this.isFollowingUser
          uni.showToast({
            title: this.isFollowingUser ? '关注成功' : '取消关注成功',
            icon: 'success'
          })
          // 刷新用户统计数据
          this.loadUserData()
        } else {
          uni.showToast({
            title: result.msg || '操作失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('关注/取消关注操作失败:', error)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
      }
    },
    
    // 跳转到聊天页�?
    goToChat() {
      if (!this.targetUserId || !this.currentUserId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }
      
      if (this.targetUserId === this.currentUserId) {
        uni.showToast({
          title: '不能给自己发消息',
          icon: 'none'
        })
        return
      }
      
      // 跳转到私信聊天页
      const targetUserName = this.displayUserInfo?.nickname || this.userData?.nickname || '用户'
      const url = `/user/chat?targetUserId=${this.targetUserId}&targetUserName=${encodeURIComponent(targetUserName)}`
      
      uni.navigateTo({
        url: url,
        success: () => {
        },
        fail: (err) => {
          console.error('跳转失败:', err)
          uni.showToast({
            title: '跳转失败',
            icon: 'none'
          })
        }
      })
    },

    // 处理宠物卡片点击事件
    handlePetCardClick(event, pet, index) {
      // 尝试从多种方式获取pet对象
      let targetPet = pet
      
      // 如果pet无效，尝试从索引获取
      if (!targetPet && index !== undefined && index !== null && this.petList && this.petList[index]) {
        targetPet = this.petList[index]
      }
      
      // 如果还是无效，尝试从事件数据获取
      if (!targetPet && event && event.currentTarget && event.currentTarget.dataset) {
        const dataIndex = event.currentTarget.dataset.index
        if (dataIndex !== undefined && this.petList && this.petList[dataIndex]) {
          targetPet = this.petList[dataIndex]
        }
      }
      
      // 点击宠物卡片：跳转到设置宠物（已移除宠物详情页）
      this.setupPet(targetPet, index)
    },

    // 设置宠物
    setupPet(pet, index) {
      // 如果pet无效，尝试从索引获取
      if (!pet && index !== undefined && index !== null && this.petList && this.petList[index]) {
        pet = this.petList[index]
      }
      
      if (!pet || !pet.id) {
        console.error('设置宠物失败：宠物数据无效', pet)
        uni.showToast({
          title: '宠物信息不存在',
          icon: 'none'
        })
        return
      }
      uni.navigateTo({
        url: `/user/setup-pet?id=${pet.id}`
      })
    },

    // 切换标签
    switchTab(tab) {
      this.currentTab = tab
      // 切换标签时加载对应的内容
      this.loadTabContent(tab)
    },
    
    // 加载标签页内
    async loadTabContent(tab) {
      const userId = this.targetUserId || this.currentUserId
      if (!userId) {
        // 静默处理警告
        return
      }
      
      switch(tab) {
        case 'dynamics':
          if (this.dynamicsList.length === 0 && !this.loadingDynamics) {
            await this.loadDynamics(userId)
          }
          break
      }
    },
    
    // 加载动态内
    async loadDynamics(userId) {
      this.loadingDynamics = true
      try {
        // 获取当前登录用户ID，用于检查点赞状
      const currentUserId = this.currentUserId || this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId')
        
        // 获取帖子列表，传递当前登录用户ID用于检查点赞状
      const params = {
          page: 1,
          size: 20,
          userId: userId // 要查看的用户ID（可能是自己，也可能是别人）
        }
        // 如果当前用户已登录，传递checkUserId参数给后端，后端会根据这个参数检查点赞状
      if (currentUserId) {
          params.checkUserId = currentUserId
        }
        const response = await api.getPostList(params)
        
        if (response && (response.code === 200 || response.code === 0)) {
          const posts = response.data?.posts || response.data?.records || response.data || []
          this.dynamicsList = posts.map(post => {
            const images = this.getPostImages(post)
            return {
              ...post,
              images: images,
              userAvatar: post.userAvatar || post.avatar || '/static/images/garfield-default-avatar.png',
              userName: post.userName || post.username || '用户',
              // 正确映射点赞状态和数量（后端应该返回这些字段）
              isLiked: post.isLiked !== undefined ? post.isLiked : (post.liked !== undefined ? post.liked : false),
              likeCount: post.likeCount !== undefined ? post.likeCount : (post.likesCount !== undefined ? post.likesCount : (post.likes !== undefined ? post.likes : 0)),
              likesCount: post.likesCount !== undefined ? post.likesCount : (post.likeCount !== undefined ? post.likeCount : (post.likes !== undefined ? post.likes : 0))
            }
          })
          this.arrangeDynamicsWaterfall()
        } else {
          this.dynamicsList = []
          this.leftDynamicsList = []
          this.rightDynamicsList = []
        }
      } catch (error) {
        console.error('加载动态失败:', error)
        this.dynamicsList = []
        this.leftDynamicsList = []
        this.rightDynamicsList = []
      } finally {
        this.loadingDynamics = false
      }
    },

    // 将动态列表排列为双列瀑布流（与发现页一致）
    arrangeDynamicsWaterfall() {
      this.leftDynamicsList = []
      this.rightDynamicsList = []
      this.leftDynamicsColumnHeight = 0
      this.rightDynamicsColumnHeight = 0
      this.pendingDynamicsItems = []
      const posts = this.dynamicsList || []
      const items = posts.map(post => this.dynamicsPostToWaterfallItem(post))
      this.pendingDynamicsItems = items.filter(item => item && (item.id !== undefined && item.id !== null))
      this.distributeDynamicsWaterfall()
    },

    dynamicsPostToWaterfallItem(post) {
      let mainImage = ''
      let isVideo = false
      // 优先帖子封面（兼?cover_image
      const cover = (post.coverImage && post.coverImage.trim()) || (post.cover_image && post.cover_image.trim())
      if (cover) mainImage = util.getImageUrl(cover)
      if (post.videos) {
        try {
          let v = post.videos
          if (typeof v === 'string') v = JSON.parse(v)
          if (Array.isArray(v) && v.length > 0) {
            isVideo = true
            if (!mainImage && v[0]) {
              const thumbUrl = (v[0].thumb && v[0].thumb.trim()) || (v[0].customThumb && v[0].customThumb.trim()) || (v[0].cover && v[0].cover.trim())
              if (thumbUrl) mainImage = util.getImageUrl(thumbUrl)
            }
          } else if (v && typeof v === 'object' && v.thumb) {
            isVideo = true
            if (!mainImage) mainImage = util.getImageUrl(v.thumb)
          }
        } catch (e) {}
      }
      if (!mainImage) {
        const imgs = this.getPostImages(post)
        if (imgs && imgs.length > 0 && imgs[0]) {
          const img = imgs[0]
          const placeholder = '/static/images/garfield-default-avatar.png'
          mainImage = (!img.includes('/cat') && !img.includes('cat') && img.trim()) ? this.getSafeImageUrl(img) : util.getImageUrl(placeholder)
        } else {
          mainImage = util.getImageUrl('/static/images/garfield-default-avatar.png')
        }
      }
      if (!mainImage || !mainImage.trim()) mainImage = util.getImageUrl('/static/images/garfield-default-avatar.png')
      const userAvatar = util.getImageUrl(post.userAvatar || post.avatar || '/static/images/garfield-default-avatar.png')
      return {
        img: mainImage,
        desc: post.title || post.content || '精彩内容',
        avator: userAvatar,
        uname: post.userName || post.username || '用户',
        like: post.likesCount !== undefined ? post.likesCount : (post.likeCount !== undefined ? post.likeCount : 0),
        id: post.id,
        post: post,
        isLiked: post.isLiked !== undefined ? post.isLiked : !!post.liked,
        imageLoaded: false,
        heightCalculated: false,
        isVideo: isVideo
      }
    },

    distributeDynamicsWaterfall() {
      if (this.pendingDynamicsItems.length === 0) return
      if (this.leftDynamicsList.length === 0 && this.rightDynamicsList.length === 0) {
        for (let i = 0; i < 6 && this.pendingDynamicsItems.length > 0; i++) {
          const item = this.pendingDynamicsItems.shift()
          if (!item) continue
          if (i % 2 === 0) this.leftDynamicsList.push(item)
          else this.rightDynamicsList.push(item)
        }
        return
      }
      if (this.leftDynamicsList.length === 0 && this.pendingDynamicsItems.length > 0) {
        const item = this.pendingDynamicsItems.shift()
        if (item) this.leftDynamicsList.push(item)
        return
      }
      if (this.rightDynamicsList.length === 0 && this.pendingDynamicsItems.length > 0) {
        const item = this.pendingDynamicsItems.shift()
        if (item) this.rightDynamicsList.push(item)
        return
      }
      if (this.pendingDynamicsItems.length > 0) {
        const item = this.pendingDynamicsItems.shift()
        if (!item) return
        if (this.leftDynamicsColumnHeight <= this.rightDynamicsColumnHeight) {
          this.leftDynamicsList.push(item)
        } else {
          this.rightDynamicsList.push(item)
        }
      }
    },

    onDynamicsWaterfallImageLoad(e) {
      const dataset = e.currentTarget && e.currentTarget.dataset
      if (!dataset) return
      const column = parseInt(dataset.column, 10)
      const index = parseInt(dataset.index, 10)
      const item = (column === 0 ? this.leftDynamicsList : this.rightDynamicsList)[index]
      if (!item || item.heightCalculated) return
      this.$nextTick(() => {
        setTimeout(() => {
          const query = uni.createSelectorQuery().in(this)
          const itemId = 'waterfall-dynamics-' + column + '-' + index
          query.select('#' + itemId).boundingClientRect((rect) => {
            if (!item || !rect || !rect.height || item.heightCalculated) return
            if (column === 0) this.leftDynamicsColumnHeight += rect.height
            else this.rightDynamicsColumnHeight += rect.height
            item.heightCalculated = true
            item.imageLoaded = true
            if (this.pendingDynamicsItems.length > 0) {
              this.distributeDynamicsWaterfall()
              this.$forceUpdate()
            }
          }).exec()
        }, 150)
      })
    },

    dynamicsPost(item) {
      return item && (item.post || item)
    },
    dynamicsPostId(item) {
      if (!item) return ''
      const p = item.post || item
      return (p && (p.id !== undefined && p.id !== null)) ? String(p.id) : ''
    },
    dynamicsItemLiked(item) {
      if (!item) return false
      const p = item.post || item
      return !!(p && (p.isLiked !== undefined ? p.isLiked : (p.liked !== undefined ? p.liked : false)))
    },
    dynamicsItemLikeCount(item) {
      if (!item) return 0
      const p = item.post || item
      if (!p) return 0
      const n = p.likesCount !== undefined ? p.likesCount : (p.likeCount !== undefined ? p.likeCount : (p.likes !== undefined ? p.likes : item.like))
      return Number(n) || 0
    },
    handleLikeInDynamicsWaterfall(e) {
      const postId = e.currentTarget && e.currentTarget.dataset && e.currentTarget.dataset.postId
      if (!postId) return
      const post = this.dynamicsList.find(p => String(p.id || p.postId) === String(postId))
      if (post) this.handleLike(post)
      this.$forceUpdate()
    },
    
    // 加载文章内容
    async loadArticles(userId) {
      this.loadingArticles = true
      try {
        const response = await api.getPostList({
          page: 1,
          size: 20,
          userId: userId,
          type: 'article'
        })

        if (response && (response.code === 200 || response.code === 0)) {
          const posts = response.data?.posts || response.data?.records || response.data || []
          const articles = posts.filter(post => post.type === 'article')
          
          this.articlesList = articles.map(article => ({
            ...article,
            images: this.getPostImages(article),
            userAvatar: article.userAvatar || article.avatar || '/static/images/garfield-default-avatar.png',
            userName: article.userName || article.username || '用户'
          }))
        } else {
          this.articlesList = []
        }
      } catch (error) {
        console.error('加载文章失败:', error)
        this.articlesList = []
      } finally {
        this.loadingArticles = false
      }
    },
    
    // 动态列表展示用图：有图用图，无图用占位（避免空白）
    getDisplayImages(item) {
      if (!item) return []
      const imgs = (item.images && item.images.length > 0) ? item.images : []
      if (imgs.length > 0) return imgs
      return ['/static/images/default-product.svg']
    },

    getPostImages(post) {
      if (!post) return []

      let imagesData = post.images

      if (imagesData && Array.isArray(imagesData) && imagesData.length > 0) {
        return imagesData
          .map(img => {
            let imgStr = ''
            if (typeof img === 'string') {
              imgStr = img.trim()
            } else if (img && typeof img === 'object' && img.url) {
              imgStr = String(img.url).trim()
            } else {
              imgStr = String(img).trim()
            }
            if (imgStr.startsWith('[') && imgStr.endsWith(']')) {
              try {
                const parsed = JSON.parse(imgStr)
                if (Array.isArray(parsed) && parsed.length > 0) {
                  imgStr = String(parsed[0]).trim()
                } else if (typeof parsed === 'string') {
                  imgStr = parsed.trim()
                }
              } catch (e) {
                const match = imgStr.match(/["']([^"']+)["']/)
                if (match && match[1]) imgStr = match[1].trim()
              }
            }
            imgStr = imgStr.replace(/^["']|["']$/g, '').trim()
            return imgStr
          })
          .filter(img => img && img.length > 0 && img !== 'null' && img !== 'undefined')
      }

      if (imagesData && typeof imagesData === 'string' && imagesData.trim()) {
        let imageStr = imagesData.trim()
        if (imageStr.includes('%22') || imageStr.includes('%27')) {
          try {
            imageStr = decodeURIComponent(imageStr)
          } catch (e) {}
        }
        if (imageStr.startsWith('[') && imageStr.endsWith(']')) {
          try {
            const parsed = JSON.parse(imageStr)
            if (Array.isArray(parsed) && parsed.length > 0) {
              return parsed
                .map(img => {
                  if (typeof img === 'string') return img.trim()
                  if (img && typeof img === 'object' && img.url) return String(img.url).trim()
                  return String(img).trim()
                })
                .filter(img => img && img.length > 0)
            }
          } catch (e) {
            const match = imageStr.match(/["']([^"']+)["']/)
            if (match && match[1]) return [match[1].trim()]
          }
        } else {
          try {
            const parsed = JSON.parse(imageStr)
            if (Array.isArray(parsed) && parsed.length > 0) {
              return parsed.map(img => (typeof img === 'string' ? img.trim() : String(img))).filter(Boolean)
            }
            if (typeof parsed === 'string' && parsed.trim()) {
              return [parsed.trim()]
            }
          } catch (e) {
            const images = imageStr.split(',').map(s => s.trim()).filter(Boolean)
            if (images.length > 0) return images
          }
        }
      }

      const cover = (post.coverImage && post.coverImage.trim()) || (post.cover_image && post.cover_image.trim())
      if (cover) return [cover]

      if (post.videos) {
        try {
          let v = post.videos
          if (typeof v === 'string') v = JSON.parse(v)
          const first = Array.isArray(v) && v[0] ? v[0] : (v && typeof v === 'object' ? v : null)
          const thumbUrl = first && ((first.thumb && String(first.thumb).trim()) || (first.customThumb && String(first.customThumb).trim()) || (first.cover && String(first.cover).trim()))
          if (thumbUrl) return [String(thumbUrl).trim()]
        } catch (e) {}
      }

      if (post.image && post.image.trim()) return [post.image]
      if (post.thumbnail && post.thumbnail.trim()) return [post.thumbnail]
      if (post.thumbnailImage && post.thumbnailImage.trim()) return [post.thumbnailImage]

      if (post.content && typeof post.content === 'string') {
        const imgRegex = /(https?:\/\/[^\s]+\.(jpg|jpeg|png|gif|webp|bmp|webp))/gi
        const matches = post.content.match(imgRegex)
        if (matches && matches.length > 0) return matches
      }

      return []
    },
    
    // 判断帖子是否为视频（与发现页、收藏页一致，用于跳转到视频详情或图片详情�?
    postHasVideo(post) {
      if (!post) return false
      if (post.videos) {
        try {
          let d = post.videos
          if (typeof d === 'string') d = JSON.parse(d)
          if (Array.isArray(d) && d.length > 0) return true
          if (d && typeof d === 'object' && d.url) return true
        } catch (e) {}
      }
      const images = this.getPostImages(post)
      if (images.length) {
        const exts = ['.mp4', '.mov', '.avi', '.m4v', '.webm', '.3gp']
        return images.some(img => typeof img === 'string' && exts.some(ext => img.toLowerCase().includes(ext)))
      }
      return false
    },

    // 跳转到帖子详情（与发现页一致：视频�?post-detail-video，图片用 post-detail-image�?
    goToPostDetail(post) {
      if (!post) return
      const postId = post.id || post.postId || post.post_id
      if (!postId) return
      const id = String(postId).trim()
      const likeCount = post.likeCount ?? post.likesCount ?? post.likes ?? 0
      const isLiked = !!(post.isLiked !== undefined ? post.isLiked : (post.liked !== undefined ? post.liked : false))
      const hasVideo = this.postHasVideo(post)
      let targetUrl
      if (hasVideo) {
        // 个人主页：只滑自己的视频，按发布时间升序（下�?更新的，上滑=更早的）
        const videoPosts = (this.dynamicsList || [])
          .filter(p => this.postHasVideo(p))
          .slice()
        const sortTime = (p) => {
          const t = p.createTime || p.create_time
          if (!t) return 0
          return new Date(t).getTime()
        }
        videoPosts.sort((a, b) => sortTime(a) - sortTime(b)) // 升序：越早的越前
        let videoIds = videoPosts
          .map(p => String(p.id || p.postId || p.post_id || '').trim())
          .filter(Boolean)
        if (!videoIds.includes(id)) {
          videoIds = [id, ...videoIds]
        }
        const ids = videoIds.length ? videoIds : [id]
        let idx = ids.indexOf(id)
        if (idx < 0) idx = 0
        const firstVideo = this.getFirstVideoUrlAndCover(post)
        if (firstVideo && firstVideo.url) {
          const app = getApp()
          if (!app.globalData) app.globalData = {}
          app.globalData.pendingVideoDetail = app.globalData.pendingVideoDetail || {}
          app.globalData.pendingVideoDetail[id] = { url: firstVideo.url, cover: firstVideo.cover || '' }
        }
        targetUrl = `/pages-community/post-detail-video?id=${id}&ids=${ids.join(',')}&index=${idx}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}&fromProfile=1`
      } else {
        targetUrl = `/pages-community/post-detail-image?id=${id}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}`
      }
      uni.navigateTo({ url: targetUrl })
    },

    getFirstVideoUrlAndCover(post) {
      try {
        let videosData = post.videos
        if (typeof videosData === 'string') {
          try {
            videosData = JSON.parse(videosData)
          } catch (e) {
            if (videosData.includes('.mp4') || videosData.includes('.mov')) {
              return { url: videosData, cover: post.coverImage || '' }
            }
            return null
          }
        }
        if (Array.isArray(videosData) && videosData.length > 0) {
          const first = videosData[0]
          const url = typeof first === 'string' ? first : (first && (first.url || first.src || first.videoUrl || first.path))
          const cover = (first && (first.thumb || first.cover)) || post.coverImage || ''
          return url ? { url, cover } : null
        }
        if (videosData && typeof videosData === 'object') {
          const u = videosData.url || videosData.src || videosData.videoUrl || videosData.path
          if (u) {
            return {
              url: u,
              cover: videosData.thumb || videosData.cover || post.coverImage || ''
            }
          }
        }
        const images = post.images
        let list = []
        if (typeof images === 'string') {
          try {
            list = JSON.parse(images)
          } catch (e) {
            list = [images]
          }
        } else if (Array.isArray(images)) {
          list = images
        }
        const videoUrl = list.find(img => typeof img === 'string' && ['.mp4', '.mov', '.avi', '.m4v'].some(ext => img.toLowerCase().includes(ext)))
        if (videoUrl) return { url: videoUrl, cover: post.coverImage || '' }
      } catch (e) {}
      return null
    },

    // 通过索引跳转到帖子详情（更可靠的方式�?
    goToPostDetailByIndex(index, listType = 'dynamics') {
      const list = listType === 'articles' ? this.articlesList : this.dynamicsList
      if (!list || index === undefined || index === null || !list[index]) {
        console.error('帖子数据无效，无法跳转', { index, listType, listLength: list?.length })
        return
      }
      const post = list[index]
      this.goToPostDetail(post)
    },
    
    // 通过索引处理分享（兼容uni-app�?
    handleShareByIndex(e) {
      const index = parseInt(e.currentTarget.dataset.index)
      if (index !== undefined && index !== null && this.dynamicsList && this.dynamicsList[index]) {
        const item = this.dynamicsList[index]
        this.handleShare(item)
      } else {
        console.error('分享失败：无法获取帖子数据', { index, dynamicsListLength: this.dynamicsList?.length, dynamicsList: this.dynamicsList })
      }
    },
    
    // 分享动�?
    handleShare(post) {
      if (!post) {
        console.error('分享失败：帖子数据无效', post)
        return
      }
      
      // 检查是否有id字段，如果没有可能是数据格式问题
      const postId = post.id || post.postId || post.post_id
      if (!postId) {
        console.error('分享失败：帖子ID不存在', post)
        uni.showToast({
          title: '帖子信息异常',
          icon: 'none'
        })
        return
      }
      
      // 与帖子详情页一致的分享入口
      uni.showActionSheet({
        itemList: ['分享到微信', '分享到朋友圈', '复制链接'],
        success: () => {
          uni.showToast({
            title: '分享成功',
            icon: 'success'
          })
        }
      })
    },
    
    // 评论动态（跳转到对应视�?图片详情页并聚焦评论�?
    handleComment(post) {
      if (!post) return
      const postId = post.id || post.postId || post.post_id
      if (!postId) return
      const id = String(postId).trim()
      const likeCount = post.likeCount ?? post.likesCount ?? post.likes ?? 0
      const isLiked = !!(post.isLiked !== undefined ? post.isLiked : (post.liked !== undefined ? post.liked : false))
      const hasVideo = this.postHasVideo(post)
      let targetUrl
      if (hasVideo) {
        const videoPosts = (this.dynamicsList || []).filter(p => this.postHasVideo(p)).slice()
        const sortTime = (p) => { const t = p.createTime || p.create_time; return t ? new Date(t).getTime() : 0 }
        videoPosts.sort((a, b) => sortTime(a) - sortTime(b))
        let videoIds = videoPosts.map(p => String(p.id || p.postId || p.post_id || '').trim()).filter(Boolean)
        if (!videoIds.includes(id)) {
          videoIds = [id, ...videoIds]
        }
        const ids = videoIds.length ? videoIds : [id]
        let idx = ids.indexOf(id)
        if (idx < 0) idx = 0
        const firstVideo = this.getFirstVideoUrlAndCover(post)
        if (firstVideo && firstVideo.url) {
          const app = getApp()
          if (!app.globalData) app.globalData = {}
          app.globalData.pendingVideoDetail = app.globalData.pendingVideoDetail || {}
          app.globalData.pendingVideoDetail[id] = { url: firstVideo.url, cover: firstVideo.cover || '' }
        }
        targetUrl = `/pages-community/post-detail-video?id=${id}&ids=${ids.join(',')}&index=${idx}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}&fromProfile=1&focusComment=true`
      } else {
        targetUrl = `/pages-community/post-detail-image?id=${id}&likeCount=${likeCount}&isLiked=${isLiked ? '1' : '0'}&focusComment=true`
      }
      uni.navigateTo({ url: targetUrl })
    },
    
    // 通过索引处理收藏（兼容uni-app�?
    handleFavoriteByIndex(e) {
      const index = parseInt(e.currentTarget.dataset.index)
      if (index !== undefined && index !== null && this.dynamicsList && this.dynamicsList[index]) {
        const item = this.dynamicsList[index]
        this.handleFavorite(item)
      } else {
        console.error('收藏失败：无法获取帖子数据', { index, dynamicsListLength: this.dynamicsList?.length, dynamicsList: this.dynamicsList })
      }
    },
    
    // 收藏动
    async handleFavorite(post) {
      if (!post) {
        console.error('收藏失败：帖子数据无效', post)
        return
      }
      
      const postId = post.id || post.postId || post.post_id
      if (!postId) {
        console.error('收藏失败：帖子ID不存在', post)
        uni.showToast({
          title: '帖子信息异常',
          icon: 'none'
        })
        return
      }

      const currentUserId = this.currentUserId || this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId')
      if (!currentUserId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }

      const wasCollected = post.isCollected
      const newCollectedState = !wasCollected

      const index = this.dynamicsList.findIndex(item => (item.id || item.postId || item.post_id) === postId)
      if (index !== -1) {
        this.$set(this.dynamicsList[index], 'isCollected', newCollectedState)
      }

      try {
        if (newCollectedState) {
          await api.collectPost(postId, currentUserId)
        } else {
          await api.uncollectPost(postId, currentUserId)
        }
        uni.showToast({
          title: newCollectedState ? '收藏成功' : '已取消收藏',
          icon: 'success',
          duration: 1500
        })
      } catch (error) {
        console.error('收藏失败:', error)
        if (index !== -1) {
          this.$set(this.dynamicsList[index], 'isCollected', wasCollected)
        }
        const errorMsg = error.message || error.errMsg || '操作失败'
        uni.showToast({
          title: errorMsg.includes('登录') ? '请先登录' : '操作失败',
          icon: 'none',
          duration: 2000
        })
      }
    },
    
    // 通过索引处理点赞（兼容uni-app�?
    handleLikeByIndex(e) {
      const index = parseInt(e.currentTarget.dataset.index)
      if (index !== undefined && index !== null && this.dynamicsList && this.dynamicsList[index]) {
        const item = this.dynamicsList[index]
        this.handleLike(item)
      } else {
        console.error('点赞失败：无法获取帖子数据', { index, dynamicsListLength: this.dynamicsList?.length, dynamicsList: this.dynamicsList })
      }
    },
    
    // 点赞动
    async handleLike(post) {
      if (!post) {
        console.error('点赞失败：帖子数据无效', post)
        return
      }
      
      const postId = post.id || post.postId || post.post_id
      if (!postId) {
        console.error('点赞失败：帖子ID不存在', post)
        uni.showToast({
          title: '帖子信息异常',
          icon: 'none'
        })
        return
      }
      
      // 获取当前用户ID
      const currentUserId = this.currentUserId || this.userInfo?.id || this.userInfo?.uid || uni.getStorageSync('userId')
      if (!currentUserId) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        return
      }
      
      // 切换点赞状
      const wasLiked = post.isLiked || false
      const newLikedState = !wasLiked
      
      // 立即更新UI状态（乐观更新
      const index = this.dynamicsList.findIndex(item => (item.id || item.postId || item.post_id) === postId)
      if (index !== -1) {
        this.$set(this.dynamicsList[index], 'isLiked', newLikedState)
        // 更新点赞
      const currentLikeCount = this.dynamicsList[index].likeCount || this.dynamicsList[index].likesCount || 0
        if (newLikedState) {
          this.$set(this.dynamicsList[index], 'likeCount', currentLikeCount + 1)
          this.$set(this.dynamicsList[index], 'likesCount', currentLikeCount + 1)
        } else {
          this.$set(this.dynamicsList[index], 'likeCount', Math.max(0, currentLikeCount - 1))
          this.$set(this.dynamicsList[index], 'likesCount', Math.max(0, currentLikeCount - 1))
        }
      }
      
      
      try {
        // 调用后端API
        if (newLikedState) {
          await api.likePost(postId, currentUserId)
        } else {
          await api.unlikePost(postId, currentUserId)
        }
        
        uni.showToast({
          title: newLikedState ? '点赞成功' : '已取消点赞',
          icon: 'success',
          duration: 1500
        })
      } catch (error) {
        console.error('点赞失败:', error)
        if (index !== -1) {
          this.$set(this.dynamicsList[index], 'isLiked', wasLiked)
          const currentLikeCount = this.dynamicsList[index].likeCount || this.dynamicsList[index].likesCount || 0
          if (wasLiked) {
            this.$set(this.dynamicsList[index], 'likeCount', currentLikeCount + 1)
            this.$set(this.dynamicsList[index], 'likesCount', currentLikeCount + 1)
          } else {
            this.$set(this.dynamicsList[index], 'likeCount', Math.max(0, currentLikeCount - 1))
            this.$set(this.dynamicsList[index], 'likesCount', Math.max(0, currentLikeCount - 1))
          }
        }
        const errorMsg = error.message || error.errMsg || '操作失败'
        uni.showToast({
          title: errorMsg.includes('登录') ? '请先登录' : '操作失败',
          icon: 'none',
          duration: 2000
        })
      }
    },
    
    // 格式化时间（兼容iOS日期格式�?
    formatTime(time) {
      if (!time) return ''
      // 使用util.parseDate来兼容iOS日期格式
      const date = util.parseDate(time)
      if (!date) return ''
      const now = new Date()
      const diff = now.getTime() - date.getTime()
      const minutes = Math.floor(diff / (1000 * 60))
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      
      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    
    // 获取图片URL（用于模板）
    getImageUrl(url) {
      if (!url) return '/static/images/garfield-default-avatar.png'
      return util.getImageUrl(url)
    },

    // 分享资料
    shareProfile() {
      uni.showToast({
        title: '分享功能开发中...',
        icon: 'none'
      })
    },

    // 去设�?
    goToSettings() {
      uni.navigateTo({
        url: '/settings/index'
      })
    },

    // 显示帮助
    showHelp() {
      uni.showToast({
        title: '帮助功能开发中...',
        icon: 'none'
      })
    },

    // 去隐私设�?
    goToPrivacySettings() {
      uni.navigateTo({
        url: '/settings/privacy'
      })
    },

    // 去账号管�?
    goToAccountManagement() {
      uni.navigateTo({
        url: '/user/account-management'
      })
    },
    
    // 格式化宠物数据，完全使用�?我的宠物"页面相同的数据格式和逻辑
    formatPetDataForProfile(pet) {
      if (!pet) {
        // 静默处理警告
        return null
      }
      
      // 确保id存在，如果不存在则跳过这条数
      if (!pet.id) {
        // 静默处理警告
        return null
      }
      
      // 完全使用�?my-pets.vue �?checkPetsData 方法相同的数据转换逻辑
      // 处理头像URL：临时路径（tmp）表示无效，使用默认图片；否则使用util处理
      let avatar = pet.avatar
      if (!avatar || avatar === 'null' || avatar === 'undefined') {
        // 空值，使用默认图片
        avatar = '/static/images/login-dog.png'
      } else if (avatar.includes('tmp') || avatar.includes('__tmp__')) {
        // 临时路径，表示无效，使用默认图片
        avatar = '/static/images/login-dog.png'
      } else {
        // 正常的服务器路径，使用util处理
        avatar = util.getImageUrl(avatar)
      }
      // 处理后的宠物对象
      const processedPet = {
        id: pet.id,
        name: pet.name || '未命名宠物',
        type: pet.species || pet.type,
        breed: pet.breed,
        gender: pet.gender,
        sterilization: this.mapGenderToSterilization(pet.gender),
        weight: pet.weight || 0,
        birthDate: pet.birthday || '',
        birthday: pet.birthday || '',
        // 优先使用arrivalDate（到家日期），如果没有则使用createTime（创建时间）
        arrivalDate: pet.arrivalDate || pet.arrival_date || null,
        age: pet.age || 0,
        totalFood: 0,
        avatar: avatar,
        description: pet.description || '',
        createTime: pet.createTime || pet.create_time || pet.createTime || null
      }
      
      
      // 计算陪伴天数（传入整个对象）
      processedPet.companionshipDays = this.calculateCompanionshipDays(processedPet)
      
      
      return processedPet
    },
    
    // 映射性别到绝育状态（和my-pets.vue完全相同�?
    mapGenderToSterilization(gender) {
      // 这里简化处理，实际应该从数据库获取
      return 'unknown'
    },
    
    // 计算陪伴天数（和my-pets.vue完全相同�?
    calculateCompanionshipDays(pet) {
      if (!pet) {
        return 0
      }
      
      // 优先使用后端返回的陪伴天
      if (pet.companionshipDays !== undefined && pet.companionshipDays !== null) {
        const days = parseInt(pet.companionshipDays) || 0
        return days
      }
      
      // 优先使用arrivalDate（到家日期）计算陪伴天数
      let arrivalDate = pet.arrivalDate || pet.arrival_date || null
      if (arrivalDate) {
        try {
          // 如果arrivalDate是数组格式（�?[2026, 1, 9]），转换为日期字符串
          if (Array.isArray(arrivalDate)) {
            if (arrivalDate.length >= 3) {
              const year = arrivalDate[0]
              const month = String(arrivalDate[1]).padStart(2, '0')
              const day = String(arrivalDate[2]).padStart(2, '0')
              arrivalDate = `${year}-${month}-${day}`
            } else {
              arrivalDate = null
            }
          }
          
          if (arrivalDate) {
            // 使用util.parseDate来兼容iOS日期格式
            const arrival = util.parseDate(arrivalDate)
            if (arrival) {
          // 如果arrivalDate是日期格式（不包含时间），设置为当天�?0:00:00
              if (typeof arrivalDate === 'string' && arrivalDate.includes('T') === false && arrivalDate.length === 10) {
            arrival.setHours(0, 0, 0, 0)
          }
          const now = new Date()
          now.setHours(0, 0, 0, 0)
          const diff = now.getTime() - arrival.getTime()
          const days = Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)))
          return days
            }
          }
        } catch (e) {
          console.error('使用arrivalDate计算陪伴天数失败:', e, 'arrivalDate:', arrivalDate)
        }
      }
      
      // 如果没有arrivalDate，使用createTime作为陪伴开始时
      const createTime = pet.createTime || pet.create_time || null
      if (createTime) {
        try {
          // 使用util.parseDate来兼容iOS日期格式
          const startTime = util.parseDate(createTime)
          if (startTime) {
          const now = new Date()
          const diff = now.getTime() - startTime.getTime()
          const days = Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)))
          return days
          }
        } catch (e) {
          console.error('使用createTime计算陪伴天数失败:', e, 'createTime:', createTime)
        }
      }
      
      return 0
    },
    
    // 判断是否为女性（用于设置样式类）
    isFemale(gender) {
      if (gender === undefined || gender === null) return false
      // 处理数字类型?表示女
      if (typeof gender === 'number') {
        return gender === 1
      }
      // 处理字符串类
      if (typeof gender === 'string') {
        return gender === '1' || gender === 'female' || gender === '女' || gender === '♀'
      }
      return false
    },
    
    // 获取性别图标（用于用户资料显示）
    getUserGenderIcon(gender) {
      if (gender === undefined || gender === null) return '♂'
      if (typeof gender === 'number') {
        return gender === 1 ? '♀' : '♂'
      }
      if (typeof gender === 'string') {
        if (gender === '1' || gender === 'female' || gender === '女' || gender === '♀') {
          return '♀'
        } else if (gender === '0' || gender === 'male' || gender === '男' || gender === '♂') {
          return '♂'
        }
      }
      return '♂'
    },
    
    // 获取性别图标（用于宠物显示）
    getGenderIcon(gender) {
      if (!gender) return ''
      // 根据性别返回图标
      if (gender === 'female' || gender === '女' || gender === '♀') {
        return '♀'
      } else if (gender === 'male' || gender === '男' || gender === '♂') {
        return '♂'
      }
      return ''
    },
    
    // 获取性别文本
    getGenderText(gender) {
      if (!gender) return ''
      if (gender === 'male' || gender === '男' || gender === '♂') {
        return '男孩'
      } else if (gender === 'female' || gender === '女' || gender === '♀') {
        return '女孩'
      }
      return gender
    },
    
    // 获取绝育状态（和my-pets.vue相同�?
    getSterilizationStatus(pet) {
      return pet.sterilization || 'unknown'
    },
    
    // 获取绝育状态文本（和my-pets.vue相同�?
    getSterilizationText(sterilization) {
      const statusMap = {
        'sterilized': '已绝育',
        'not-sterilized': '未绝育',
        'yes': '已绝育',
        'no': '未绝育',
        'unknown': '不清楚'
      }
      return statusMap[sterilization] || statusMap['unknown']
    },
    
    // 格式化罐头数（超�?000g时转换为kg�?
    formatPoints(points) {
      if (points === undefined || points === null) {
        return '0g'
      }
      const numPoints = Number(points)
      if (isNaN(numPoints) || numPoints < 0) {
        return '0g'
      }
      // 超过1000g时转换为kg，保?位小
      if (numPoints >= 1000) {
        const kg = (numPoints / 1000).toFixed(2)
        // 去掉末尾�?
        return parseFloat(kg) + 'kg'
      }
      return numPoints + 'g'
    },
    
    // 格式化陪伴时间（显示�?X小时"格式，基于实际小时数计算，兼容iOS日期格式�?
    formatCompanionTime(pet) {
      if (!pet) return '0小时'
      
      // 获取到家日期或创建时
      let startDate = pet.arrivalDate || pet.arrival_date || pet.createTime || pet.create_time
      if (!startDate) return '0小时'
      
      try {
        // 如果startDate是数组格式（�?[2026, 1, 9]），转换为日期字符串
        if (Array.isArray(startDate)) {
          if (startDate.length >= 3) {
            const year = startDate[0]
            const month = String(startDate[1]).padStart(2, '0')
            const day = String(startDate[2]).padStart(2, '0')
            startDate = `${year}-${month}-${day}`
          } else {
            return '0小时'
          }
        }
        
        // 使用util.parseDate来兼容iOS日期格式
        const start = util.parseDate(startDate)
        if (!start) return '0小时'
        
        const now = new Date()
        const diff = now.getTime() - start.getTime()
        
        if (diff < 0) return '0小时'
        
        // 计算总小时数
        const totalHours = Math.floor(diff / (1000 * 60 * 60))
        const days = Math.floor(totalHours / 24)
        const hours = totalHours % 24
        
        // 如果有天数，显示"X天X小时"，否则只显示"X小时"
        if (days > 0) {
          return hours > 0 ? `${days}天${hours}小时` : `${days}天`
        }
        return `${hours}小时`
      } catch (e) {
        console.error('格式化陪伴时间失败:', e, 'startDate:', startDate)
        return '0小时'
      }
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

/* 背景图区�?*/
.background-section {
  position: relative;
  width: 100%;
  margin-bottom: -70rpx; /* 头像高度的一半，让头像一半在背景图上 */
  margin-top: 0;
  padding-top: 0; /* 高度通过内联样式动态设置，包含状态栏高度 */
  /* 背景图从容器顶部开始，向上覆盖状态栏 */
      }

.background-image-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  /* 确保背景图从容器顶部开始，覆盖整个容器包括状态栏区域 */
      }

.background-image {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
  /* 背景图从容器顶部开始，向上延伸到状态栏 */
      }

.background-image-gradient {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  /* 默认渐变背景 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1;
      }

/* 返回按钮 - 位于背景图左上角 */
.back-button {
  position: absolute;
  left: 20rpx;
  width: 70rpx;
  height: 70rpx;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  /* top 位置通过内联样式动态设置，考虑状态栏高度 */
      }

.back-icon-white {
  font-size: 48rpx;
  color: #fff;
  font-weight: 300;
      }

/* 上传背景图按�?- 位于右下�?*/
.background-upload-btn {
  position: absolute;
  right: 30rpx;
  bottom: 30rpx;
  width: 80rpx;
  height: 80rpx;
  background: transparent;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
      }

.upload-icon {
  font-size: 36rpx;
      }

.upload-icon-image {
  width: 40rpx;
  height: 40rpx;
      }

/* 用户信息卡片 */
.user-info-card {
  background: #fff;
  margin: 0 20rpx 20rpx;
  margin-top: 70rpx; /* 调整margin-top，配合头像位�?*/
  border-radius: 20rpx;
  padding: 30rpx;
  padding-top: 0; /* 移除顶部padding，让头像可以向上重叠 */
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
      }

.user-avatar-section {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  margin-bottom: 20rpx;
  position: relative;
      }

.user-avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%; /* 头像容器是圆形的 */
  border: 4rpx solid #ffd700;
  overflow: hidden; /* 关键：确保内容被裁剪成圆�?*/
  flex-shrink: 0;
  position: absolute;
  top: -70rpx; /* 向上移动70rpx（头像高度的一半），使头像一半在背景图上 */
  left: 0;
  z-index: 15;
  background: #fff; /* 确保头像区域有背景色 */
      }

.user-avatar.avatar-clickable {
  cursor: pointer;
      }

.avatar-image {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: 50%; /* 确保图片本身也是圆形�?*/
      }

/* 用户信息在头像下�?*/
.user-info-below-avatar {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 0;
  padding-top: 0;
  margin-top: 70rpx; /* 增加margin-top，使内容与头像下半部分对�?*/
      }

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
      }

.user-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
      }

.user-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
      }

.divider {
  font-size: 24rpx;
  color: #ccc;
  margin: 0 4rpx;
      }

.level-badge {
  background: #ffd700;
  color: #333;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 12rpx;
  font-weight: 600;
      }

.gender-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  
  &.gender-female {
    color: #ff1493; /* 深粉色文�?*/
  }
  
  &.gender-male {
    color: #1e90ff; /* 深蓝色文�?*/
  }
      }

.points-info {
  display: flex;
  align-items: center;
  gap: 6rpx;
      }

.points-text {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
      }

.points-can-icon {
  width: 24rpx;
  height: 24rpx;
      }

/* 编辑按钮区域在右�?*/
.edit-profile-section {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
  margin-left: 20rpx;
  padding-top: 0;
  margin-top: 70rpx; /* 增加margin-top，使内容与头像下半部分对�?*/
  position: relative;
  z-index: 10;
  pointer-events: auto;
      }

.edit-profile-btn,
.follow-btn {
  background: #ffd700;
  color: #333;
  font-size: 26rpx;
  padding: 10rpx 24rpx;
  border-radius: 22rpx;
  cursor: pointer;
  transition: all 0.3s;
      }

.follow-btn.followed {
  background: #e0e0e0;
  color: #666;
  font-weight: 600;
  cursor: pointer;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
  position: relative;
  z-index: 11;
  width: auto;
  min-width: auto;
  padding: 10rpx 16rpx;
  display: inline-flex;
  align-items: center;
  justify-content: center;
      }

.action-buttons-wrapper {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12rpx;
  position: relative;
  z-index: 12;
  pointer-events: auto;
      }

.chat-btn {
  width: auto;
  height: 46rpx;
  min-height: 46rpx;
  background: transparent;
  border: none;
  border-radius: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: none;
  position: relative;
  z-index: 12;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
  padding: 0;
      }

.chat-btn:active {
  opacity: 0.7;
  transform: scale(0.95);
      }

.chat-icon {
  font-size: 36rpx;
  line-height: 1;
      }

.chat-icon-image {
  width: 46rpx;
  height: 46rpx;
      }

.dropdown-icon {
  font-size: 24rpx;
  color: #999;
  cursor: pointer;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
  padding: 5rpx;
  text-align: center;
  position: relative;
  z-index: 11;
  width: 40rpx;
  height: 40rpx;
  line-height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
      }

.user-bio {
  margin-top: 10rpx;
      }

.bio-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
      }

/* 统计数据 */
.user-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
      }

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
      }

.stat-number {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
      }

.stat-label {
  font-size: 24rpx;
  color: #666;
      }

/* 宠物信息区域 */
.pet-info-section {
  background: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: 20rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
      }

.pet-header {
  margin-bottom: 20rpx;
      }

.pet-count {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
      }

/* 横向滚动容器 */
.pet-scroll-container {
  width: 100%;
  white-space: nowrap;
  /* uni-app的scroll-view组件不需要overflow设置，它会自动处�?*/
      }

.pet-cards-wrapper {
  display: flex;
  flex-direction: row;
  padding: 0 0 10rpx 20rpx;
  /* 只保留左边和底部的padding */
  /* 不设置宽度限制，让内容自然展开 */
      }

/* 横向宠物卡片 */
.pet-card-horizontal {
  position: relative;
  width: 340rpx; /* 增加卡片宽度，让内容更舒�?*/
  min-width: 340rpx;
  max-width: 340rpx;
  background: #fff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-right: 24rpx; /* 卡片之间的间�?*/
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.15);
  border: 1rpx solid #e8e8e8;
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  justify-content: space-between;
  min-height: 180rpx;
  flex-shrink: 0;
  flex-grow: 0;
  box-sizing: border-box;
  overflow: hidden; /* 防止内容超出卡片 */
      }

.pet-card-horizontal:last-child {
  margin-right: 20rpx; /* 最后一个卡片也保留右边距，方便滚动 */
      }

/* 左侧信息区域 */
.pet-info-name-section {
  flex: 1; /* 自动伸缩，占据剩余空�?*/
  min-width: 0; /* 允许收缩 */
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding-right: 16rpx;
  overflow: hidden;
  word-break: break-word; /* 文本换行 */
      }

/* 左上角名�?*/
.pet-name-large {
  font-size: 30rpx;
  font-weight: normal;
  color: #333;
  line-height: 1.3;
  margin-bottom: 6rpx;
      }

/* 状态标�?*/
.pet-status-tag-vertical {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 5rpx 12rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  color: #fff;
  width: fit-content;
  /* 默认蓝色（男孩子�?*/
  background: #e3f2fd;
      }

.pet-status-tag-vertical.pink {
  /* 粉色（女孩子�?*/
  background: #ff69b4;
      }

.pet-status-tag-vertical.blue {
  /* 蓝色（男孩子�?*/
  background: #e3f2fd;
      }

.gender-icon-small {
  font-size: 16rpx;
  line-height: 1;
      }

.pet-status-tag-vertical.pink .gender-icon-small,
.pet-status-tag-vertical.pink text {
  color: #fff;
      }

.pet-status-tag-vertical.blue .gender-icon-small,
.pet-status-tag-vertical.blue text {
  color: #1976d2;
      }

/* 时间和陪伴天�?*/
.pet-time-vertical {
  font-size: 22rpx;
  color: #999;
  line-height: 1.4;
      }

/* 分隔�?*/
.pet-divider {
  width: 100%;
  height: 1rpx;
  background: #f0f0f0;
  margin: 8rpx 0;
      }


/* 右侧区域（头像和按钮�?*/
.pet-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  gap: 8rpx;
  flex-shrink: 0;
  width: 90rpx; /* 固定右侧区域宽度 */
  min-width: 90rpx;
  max-width: 90rpx;
      }

/* 右侧头像 */
.pet-avatar-vertical {
  width: 100rpx;
  height: 100rpx;
  min-width: 100rpx;
  min-height: 100rpx;
  max-width: 100rpx;
  max-height: 100rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 3rpx solid #f0f0f0;
  flex-shrink: 0;
  background-color: #f5f5f5;
  box-sizing: border-box;
      }

.pet-avatar-vertical image {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
      }

/* 导航标签 - 小号、左对齐 */
.nav-tabs {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  background: #fff;
  margin: 0 20rpx 20rpx;
  padding-left: 24rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  overflow: hidden;
      }

.tab-item {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 24rpx 0;
  position: relative;
      }

.tab-text {
  font-size: 24rpx;
  color: #666;
  font-weight: 500;
      }

.tab-item.active .tab-text {
  color: #333;
      }

.tab-underline {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40rpx;
  height: 4rpx;
  background: #007aff;
  border-radius: 2rpx;
      }

/* 内容区域 - 移除最外层框，只保留里面的卡片 */
.content-area {
  background: transparent;
  margin: 0;
  border-radius: 0;
  box-shadow: none;
  min-height: 0;
      }

.tab-content {
  padding: 0;
      }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 0;
      }

.empty-icon-image {
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 20rpx;
      }

.empty-text {
  font-size: 28rpx;
  color: #999;
      }

/* 加载状�?*/
.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 80rpx 0;
  font-size: 28rpx;
  color: #999;
      }

/* 内容列表 - 每个动态独立成�?*/
.content-item {
  position: relative;
  background: #fff;
  border-radius: 0;
  padding: 0;
  margin: 0 20rpx 20rpx 20rpx; /* 左右有边距，底部有间距，形成独立�?*/
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  border-radius: 8rpx;
  overflow: hidden;
      }

/* 动态标题（左上角） */
.content-number {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  right: 140rpx; /* 右边留出空间给时�?*/
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
  z-index: 10;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: calc(100% - 200rpx); /* 确保不会和时间重�?*/
  line-height: 1.4;
      }

/* 时间（右上角�?*/
.content-time-top {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  font-size: 24rpx;
  color: #999;
  z-index: 10;
      }

/* 动态主要内容区�?*/
.content-main {
  padding: 60rpx 20rpx 20rpx; /* 顶部留出空间给编号和时间 */
      }

.content-body {
  margin-bottom: 0;
      }

.content-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  display: block;
  margin-bottom: 15rpx;
      }

/* 图片容器（靠左显示，像微信朋友圈�?*/
.content-images-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  justify-content: flex-start; /* 靠左对齐 */
  margin-top: 30rpx; /* 增加标题和图片之间的间距 */
      }

.content-image-large {
  border-radius: 8rpx;
  flex-shrink: 0;
  display: block;
  background-color: #f5f5f5;
      }

/* 单张图片时，缩小尺寸，靠左显示（像微信朋友圈），高度增加 */
.content-image-large.single-image {
  width: 65%; /* 屏幕宽度�?5%，像微信朋友�?*/
  max-width: 500rpx; /* 最大宽度限�?*/
  height: 500rpx; /* 增加高度，从375rpx增加�?00rpx */
  max-height: 700rpx; /* 增加最大高�?*/
  min-height: 300rpx; /* 增加最小高�?*/
      }

/* 多张图片时，显示为网格，靠左 */
.content-image-large.multiple-images {
  width: calc((65% - 20rpx) / 3); /* 基于65%宽度计算，三列网�?*/
  max-width: calc((500rpx - 20rpx) / 3);
  height: calc((65vw - 20rpx) / 3); /* 正方形，高度等于宽度 */
  min-height: 120rpx;
  max-height: 160rpx;
      }

/* 操作栏（点赞、收藏、分享）- 靠右显示，紧凑排�?*/
.content-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-end; /* 靠右对齐 */
  padding: 15rpx 20rpx;
  border-top: 1rpx solid #f0f0f0;
  gap: 20rpx; /* 减少间隙，从40rpx改为20rpx */
      }

.action-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 50rpx;
  height: 50rpx;
  cursor: pointer;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
      }

.action-icon {
  width: 36rpx;
  height: 36rpx;
  flex-shrink: 0;
  pointer-events: none;
  transition: filter 0.3s ease;
      }

/* 已点赞状�?- 红色（与帖子详情一致） */
.action-icon.like-icon.liked {
  filter: brightness(0) saturate(100%) invert(27%) sepia(98%) saturate(5000%) hue-rotate(350deg) brightness(95%) contrast(101%);
      }

/* 已收藏状�?- 黄色 */
.action-icon.favorite-icon.collected {
  filter: brightness(0) saturate(100%) invert(73%) sepia(100%) saturate(2000%) hue-rotate(0deg) brightness(105%) contrast(105%);
  /* 金黄色滤镜效�?*/
      }

.action-text {
  display: none; /* 隐藏文字 */
      }

/* 我的动�?- 发现页同款双列瀑布�?*/
.dynamics-waterfall-wrap {
  flex: 1;
  min-height: 400rpx;
  background: #f5f5f5;
      }

.dynamics-scroll.discovery-scroll {
  width: 100%;
  height: 100%;
  min-height: 60vh;
  background: #f5f5f5;
      }

.dis-list {
  width: 100%;
  display: flex;
  flex-direction: row;
  padding: 16rpx 8rpx;
  background: #f5f5f5;
  box-sizing: border-box;
      }

.profile-container .dynamics-col-0,
.profile-container .dynamics-col-1 {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 8rpx;
      }

.profile-container .waterfall-item {
  width: 100%;
  margin-bottom: 16rpx;
      }

.profile-container .note_item {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: transform 0.2s;
      }

.profile-container .note_item:active {
  transform: scale(0.98);
      }

.profile-container .note_item .img {
  width: 100%;
  min-height: 240rpx;
  overflow: hidden;
  background: #f0f0f0;
  position: relative;
      }

.profile-container .note_item .img image {
  width: 100%;
  min-height: 200rpx;
  display: block;
  vertical-align: top;
  background: #f0f0f0;
      }

.profile-container .note_item .img .video-play-icon-small {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 36rpx;
  height: 36rpx;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16rpx;
  pointer-events: none;
      }

.profile-container .note_item .desc {
  width: 100%;
  padding: 12rpx 16rpx 8rpx 16rpx;
  margin: 0;
  box-sizing: border-box;
      }

.profile-container .note_item .desc text {
  margin: 0;
  padding: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-size: 24rpx;
  color: #333;
  line-height: 1.5;
  font-weight: 500;
  word-break: break-word;
      }

.profile-container .note_item .note {
  height: 56rpx;
  padding: 0 16rpx 12rpx 16rpx;
  margin: 0;
  line-height: 56rpx;
  display: flex;
  color: #333;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
      }

.profile-container .note_item .note .user {
  display: flex;
  color: #333;
  font-size: 20rpx;
  flex-direction: row;
  align-items: center;
  flex: 1;
  overflow: hidden;
      }

.profile-container .note_item .note .user image {
  width: 36rpx;
  height: 36rpx;
  margin-right: 8rpx;
  border-radius: 50%;
  flex-shrink: 0;
      }

.profile-container .note_item .note .user text {
  font-size: 20rpx;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
      }

.profile-container .note_item .note .like {
  display: flex;
  color: #999;
  flex-direction: row;
  align-items: center;
  gap: 6rpx;
  flex-shrink: 0;
      }

.profile-container .note_item .note .like .like-heart {
  width: 24rpx;
  height: 24rpx;
  font-size: 0;
  display: inline-block;
      }

.profile-container .note_item .note .like .like-heart.liked {
  filter: brightness(0) saturate(100%) invert(20%) sepia(100%) saturate(5000%) hue-rotate(0deg) brightness(90%) contrast(120%);
  -webkit-filter: brightness(0) saturate(100%) invert(20%) sepia(100%) saturate(5000%) hue-rotate(0deg) brightness(90%) contrast(120%);
      }

.profile-container .note_item .note .like .like-num {
  font-size: 20rpx;
  color: #999;
  line-height: 1;
      }
</style>