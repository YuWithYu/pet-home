<template>
  <view class="user-container">
    <!-- 自定义渐变导航栏 -->
    <custom-navbar :title="''" :show-back="false"></custom-navbar>
    
    <!-- 不用 custom-pull-refresh：其根节点绑定全页 touchmove，会拦截小程序内 navigator/点击（表现：整页点不进去） -->
    <view class="main-page-inner">
      <!-- 已登录状态（直接显示，不需要登录） -->
      <view class="user-section" :style="{ paddingTop: navBarTotalHeight + 'px' }">
      <!-- 黄色背景区域（半圆形） -->
      <view class="yellow-background-area">
        <!-- 移除所有图标 -->
      </view>
      
      <!-- 白色个人信息卡片 -->
      <view class="user-profile-card">
        <!-- 用户信息主体 -->
        <view class="user-info-main" @tap="goToProfile">
          <view class="user-avatar-section">
            <view class="user-avatar">
              <image :src="(isStaffLoggedIn ? staffAvatar : (currentUserAvatar || userAvatar)) || '/static/images/login-dog.png'" mode="aspectFill" @error="handleAvatarError" />
            </view>
            <view class="user-basic-info">
              <view class="user-name">{{ isStaffLoggedIn ? staffDisplayName : (displayNickname || '宠友' + displayUserId) }}</view>
              <view class="user-level-assets" v-if="!isStaffLoggedIn">
                <view class="can-amount">
                  <view class="can-icon">
                    <image src="/static/images/my-cans.png" mode="aspectFit" class="can-icon-image" />
                  </view>
                  <view class="can-text">{{ canAmount }}g</view>
                </view>
              </view>
            </view>
          </view>
          
          <!-- 操作按钮（工作人员登录时隐藏） -->
          <view class="action-buttons" v-if="!isStaffLoggedIn">
            <view class="sign-btn" @tap.stop="goToSignIn" :class="{ 'disabled': hasSignedToday }">
              {{ hasSignedToday ? '已签到' : '签到' }}
            </view>
            <view class="redeem-btn" @tap.stop="goToExchange">兑换</view>
          </view>
        </view>
        
        <!-- 用户统计信息（工作人员登录时隐藏） -->
        <view class="user-stats" v-if="!isStaffLoggedIn">
          <view class="stat-item" @tap="gotoFollowingTabs('following')">
            <view class="stat-number">{{ stats.follows }}</view>
            <view class="stat-label">关注</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @tap="gotoFollowingTabs('fans')">
            <view class="stat-number">{{ stats.fans }}</view>
            <view class="stat-label">粉丝</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item" @tap="gotoFollowingTabs('posts')">
            <view class="stat-number">{{ stats.dynamics }}</view>
            <view class="stat-label">动态</view>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <view class="stat-number">{{ stats.likes }}</view>
            <view class="stat-label">获赞</view>
          </view>
        </view>
      </view>

      <!-- 服务功能区（工作人员登录时隐藏） -->
      <view class="orders-card" v-if="!isStaffLoggedIn">
        <view class="card-header">
          <view class="card-title">
            <view class="title-decoration"></view>
            我的服务
          </view>
        </view>
        <view class="orders-grid">
          <!-- data-svc：避免模板里写 openServicePage('/path') 在部分小程序编译器下事件绑定异常 -->
          <view class="order-item" data-svc="pets" @tap.stop="onServiceItemTap">
            <view class="order-icon">
              <image src="/static/images/my-pets.png" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的宠物</view>
          </view>
          <view class="order-item" data-svc="cans" @tap.stop="onServiceItemTap">
            <view class="order-icon">
              <image src="/static/images/my-cans.png" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的罐头</view>
          </view>
          <view class="order-item" data-svc="appointments" @tap.stop="onServiceItemTap">
            <view class="order-icon">
              <image src="/static/images/my-appointments.png" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的预约</view>
          </view>
          <view class="order-item" data-svc="collections" @tap.stop="onServiceItemTap">
            <view class="order-icon">
              <image src="/static/images/my-favorites.png" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的收藏</view>
          </view>
          <view class="order-item" data-svc="orders" @tap.stop="onServiceItemTap">
            <view class="order-icon">
              <image src="/static/images/我的订单.png" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的订单</view>
          </view>
          <view class="order-item" data-svc="cart" @tap.stop="onServiceItemTap">
            <view class="order-icon">
              <image src="/static/images/my-orders.png" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">购物车</view>
          </view>
          <view class="order-item" data-svc="address" @tap.stop="onServiceItemTap">
            <view class="order-icon">
              <image src="/static/images/我的地址.png" mode="aspectFit" class="order-icon-image" />
            </view>
            <view class="order-text">我的地址</view>
          </view>
        </view>
      </view>

      <!-- 我的权益 -->
      <view class="rights-card">
        <view class="card-header">
          <view class="card-title">
            <view class="title-decoration"></view>
            我的权益
          </view>
        </view>
        <view class="rights-grid" :class="{ 'rights-grid-staff': isStaffLoggedIn }">
          <view class="right-item" @tap="goCustomerService" v-if="!isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/online-consultation.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">在线咨询</view>
          </view>
          <view class="right-item" @tap="goAnnouncement" v-if="!isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/about.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">系统公告</view>
          </view>
          <view class="right-item" @tap="handleReport" v-if="!isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/report.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">投诉举报</view>
          </view>
          <view class="right-item" @tap="handleAbout" v-if="!isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/about.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">关于宠物家</view>
          </view>
          <view class="right-item" @tap="handleSettings" v-if="!isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/settings.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">设置</view>
          </view>
          <view class="right-item" @tap="goWorkOrders" v-if="isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/我的订单.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">我的工单</view>
          </view>
          <view class="right-item" @tap="goOrderVerify" v-if="isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/my-appointments.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">订单核销</view>
          </view>
          <view class="right-item" @tap="goMySchedule" v-if="isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/my-appointments.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">我的排班</view>
          </view>
          <view class="right-item" @tap="goMyReviews" v-if="isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/settings.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">我的评价</view>
          </view>
          <view class="right-item" @tap="staffLogout" v-if="isStaffLoggedIn">
            <view class="right-icon">
              <image src="/static/images/settings.png" mode="aspectFit" class="right-icon-image" />
            </view>
            <view class="right-text">退出</view>
          </view>
        </view>
      </view>

    </view>
    </view>
  </view>
</template>

<script>
import { mapGetters, mapMutations } from 'vuex'
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'
// CustomNavbar 已在 main.js 全局注册

export default {
  name: 'User',
  components: {},

  data() {
    return {
      canAmount: 0,
      hasSignedToday: false,
      currentUserAvatar: '', // 当前用户的头像URL
      localUserInfo: null, // 本地用户信息（从API获取），用于显示
      nickname: '', // 用户昵称（用于显示）
      loadUserDataTimer: null, // 加载用户数据的定时器，用于防抖
      staffLoggedIn: false, // 工作人员登录状态（需在 onShow 刷新，否则 tab 缓存时不会更新）
      stats: {
        follows: 0,
        fans: 0,
        dynamics: 0,
        likes: 0
      },
      statusBarHeight: 0,
      navBarHeight: 44,
      lastUserDataLoadTime: 0 // 上次静默加载用户数据时间，用于 onShow 节流，避免频繁「加载中」
    }
  },

  computed: {
    ...mapGetters(['userInfo', 'userNickname', 'userAvatar', 'isLoggedIn']),
    isStaffLoggedIn() {
      return this.staffLoggedIn
    },
    
    // 导航栏总高度（状态栏 + 导航栏）
    navBarTotalHeight() {
      return this.statusBarHeight + this.navBarHeight
    },
    
    // 显示用的用户信息：优先使用本地信息，否则使用Vuex中的信息
    displayUserInfo() {
      return this.localUserInfo || this.userInfo || {}
    },
    
    // 显示用的昵称：优先使用本地昵称，否则使用Vuex中的昵称
    displayNickname() {
      return this.nickname || this.userNickname || '用户'
    },
    
    // 显示用的用户ID（用于模板中）
    displayUserId() {
      const info = this.displayUserInfo
      return (info && (info.uid || info.id)) || 'ddf94a72'
    },

    // 工作人员模式下的显示名称
    staffDisplayName() {
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      return staffInfo.name || staffInfo.username || '工作人员'
    },

    // 工作人员头像：从 staffInfo 读取并处理为可访问的 URL，无则返回空以便用默认图
    staffAvatar() {
      if (!this.staffLoggedIn) return ''
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      const raw = staffInfo.avatar
      if (!raw || raw === 'null' || String(raw).trim() === '') return ''
      if (raw.includes('tmp') || raw.includes('__tmp__')) return ''
      if (raw === '/static/images/login-dog.png' || raw.includes('login-dog.png')) return '/static/images/login-dog.png'
      return (util.getImageUrl && util.getImageUrl(raw)) || raw
    }
  },

  onLoad() {
    // 不在 onLoad 里 loadSubpackage；分包在「我的服务」点击时由 navigateToUrl 按需加载
    // 使用 getWindowInfo/getDeviceInfo 替代已废弃的 getSystemInfo
    try {
      if (typeof uni.getWindowInfo === 'function') {
        const win = uni.getWindowInfo()
        this.statusBarHeight = win.statusBarHeight || 0
      }
      if (typeof uni.getDeviceInfo === 'function') {
        const dev = uni.getDeviceInfo()
        this.navBarHeight = dev.platform === 'ios' ? 44 : 48
      } else if (typeof uni.getSystemInfoSync === 'function') {
        const sys = uni.getSystemInfoSync()
        this.statusBarHeight = this.statusBarHeight || sys.statusBarHeight || 0
        this.navBarHeight = sys.platform === 'ios' ? 44 : 48
      } else if (typeof uni.getSystemInfo === 'function') {
        uni.getSystemInfo({
          success: (res) => {
            this.statusBarHeight = res.statusBarHeight || 0
            this.navBarHeight = res.platform === 'ios' ? 44 : 48
          }
        })
      }
    } catch (e) {
      uni.getSystemInfo && uni.getSystemInfo({
        success: (res) => {
          this.statusBarHeight = res.statusBarHeight || 0
          this.navBarHeight = res.platform === 'ios' ? 44 : 48
        }
      })
    }
    
    // 监听用户信息更新事件（静默刷新，不弹全局「加载中」）
    uni.$on('userInfoUpdated', () => {
      this.loadUserData(true)
    })
    
    // 刷新工作人员登录状态（storage 非响应式，需显式更新）
    this.staffLoggedIn = !!uni.getStorageSync('staffToken')
    // 检查登录状态：普通用户需 token+userId，工作人员需 staffToken
    setTimeout(() => {
      const token = uni.getStorageSync('token')
      const userId = uni.getStorageSync('userId')
      const staffToken = uni.getStorageSync('staffToken')
      const isLoggedIn = this.isLoggedIn
      
      const isUserLoggedIn = token && userId && isLoggedIn
      const isStaffLoggedIn = !!staffToken
      
      if (!isUserLoggedIn && !isStaffLoggedIn) {
        uni.navigateTo({ url: '/pages-auth/login' })
        return
      }
      if (isUserLoggedIn) {
        this.loadUserData()
      }
    }, 200)
  },

  onShow() {
    // 每次显示时刷新工作人员状态，解决 tab 切换/登录后缓存页不更新的问题
    this.staffLoggedIn = !!uni.getStorageSync('staffToken')
    // 若刚删过帖子，强制刷新一次动态数，避免「动态」显示不更新
    if (uni.getStorageSync('REFRESH_MY_STATS') === '1') {
      uni.removeStorageSync('REFRESH_MY_STATS')
      this.lastUserDataLoadTime = 0
    }
    // 页面显示时静默刷新：节流（距上次加载不足 15 秒不重复请求）+ 不显示全局「加载中」
    if (this.isLoggedIn) {
      if (this.loadUserDataTimer) {
        clearTimeout(this.loadUserDataTimer)
      }
      const now = Date.now()
      const throttleMs = 15 * 1000 // 15 秒内不重复静默刷新，避免停留页面时反复出现加载中
      if (now - this.lastUserDataLoadTime < throttleMs && this.lastUserDataLoadTime > 0) {
        return
      }
      this.loadUserDataTimer = setTimeout(() => {
        this.loadUserData(true) // true = 静默刷新，不显示全局 loading
      }, 300)
    }
  },
  
  onUnload() {
    // 移除事件监听
    uni.$off('userInfoUpdated')
    // 清除定时器
    if (this.loadUserDataTimer) {
      clearTimeout(this.loadUserDataTimer)
      this.loadUserDataTimer = null
    }
  },

  methods: {
    ...mapMutations(['SET_USER_INFO']),
    
    loadUserData(silent = false) {
      // silent 为 true 时不显示全局「加载中」，用于 onShow 静默刷新，避免影响体验
      if (silent) {
        this.lastUserDataLoadTime = Date.now()
      }
      // 获取当前用户ID
      const vuexUserInfo = this.userInfo || {} // 从Vuex获取
      const localUserInfo = this.localUserInfo || {} // 从本地data获取
      const currentUserId = localUserInfo.id || localUserInfo.uid || vuexUserInfo.id || vuexUserInfo.uid || uni.getStorageSync('userId') || null
      const token = uni.getStorageSync('token')
      
      // 如果没有用户ID和token，说明未登录，不调用接口
      if (!currentUserId && !token) {
        return
      }
      
      // 如果只有token但没有userId，尝试从token中解析或使用默认值
      if (!currentUserId && token) {
        const storedUserInfo = uni.getStorageSync('userInfo')
        if (storedUserInfo && (storedUserInfo.id || storedUserInfo.uid)) {
          const userId = storedUserInfo.id || storedUserInfo.uid
          // 更新本地userInfo
          if (!this.localUserInfo) {
            this.localUserInfo = storedUserInfo
            // 同时更新Vuex store
            this.SET_USER_INFO(storedUserInfo)
          }
        }
      }
      
      // 再次获取currentUserId（可能已更新）
      const localInfo = this.localUserInfo || {}
      const vuexInfo = this.userInfo || {}
      const finalUserId = localInfo.id || localInfo.uid || vuexInfo.id || vuexInfo.uid || currentUserId || uni.getStorageSync('userId')
      
      // 加载用户基本信息（静默刷新时不显示全局 loading）
      api.getCurrentUser({ userId: finalUserId, showLoading: !silent })
        .then(res => {
          if (!res) {
            console.error('API响应为空')
            return
          }
          if ((res.code === 200 || res.code === 0) && res.data) {
            const data = res.data
            // 罐头数量：优先使用后端返回的points，如果没有则为0（不是120）
            this.canAmount = data.points !== undefined && data.points !== null ? data.points : 0
            this.hasSignedToday = data.hasSignedToday || false
            
            // 更新本地用户信息对象（不使用Vuex的computed属性）
            this.localUserInfo = {
              id: data.id || data.uid || finalUserId,
              uid: data.uid || data.id || finalUserId,
              nickname: data.nickname || data.username || '用户',
              username: data.username || data.nickname || '用户',
              avatar: data.avatar || '/static/images/login-dog.png'
            }
            
            // 同时更新Vuex store（使用mutation）
            this.SET_USER_INFO(this.localUserInfo)
            
            // 更新头像URL（使用util处理URL，过滤临时路径）
            if (data.avatar && data.avatar !== 'null' && data.avatar !== 'undefined' && data.avatar.trim() !== '') {
              // 检查是否是临时路径（tmp或__tmp__），如果是则使用默认头像
              if (data.avatar.includes('tmp') || data.avatar.includes('__tmp__')) {
                this.currentUserAvatar = '/static/images/login-dog.png'
                if (this.localUserInfo) {
                  this.localUserInfo.avatar = '/static/images/login-dog.png'
                }
              } else {
                // 如果是静态资源路径，直接使用，不经过util.getImageUrl处理
                if (data.avatar === '/static/images/login-dog.png' || data.avatar.includes('login-dog.png')) {
                  this.currentUserAvatar = '/static/images/login-dog.png'
                } else {
                  // 其他路径使用util.getImageUrl处理
                const processedAvatar = util.getImageUrl(data.avatar)
                this.currentUserAvatar = processedAvatar
                }
                if (this.localUserInfo) {
                  this.localUserInfo.avatar = this.currentUserAvatar
                }
              }
            } else {
              // 没有头像或为空，使用默认头像
              this.currentUserAvatar = '/static/images/login-dog.png'
              if (this.localUserInfo) {
                this.localUserInfo.avatar = '/static/images/login-dog.png'
              }
            }
            
            // 更新昵称显示
            this.nickname = data.nickname || data.username || '用户'
            
            // 从响应中获取 userId，用于后续统计接口调用
            const userId = data.id || data.uid || currentUserId || null
            
            // 加载用户统计信息（调用专门的统计接口）
            if (userId) {
              const statsUserId = typeof userId === 'string' ? parseInt(userId) : userId
              
              // 延迟加载统计信息，避免与getCurrentUser同时请求导致401（静默时也不弹 loading）
              setTimeout(() => {
                api.getUserStats(statsUserId, !silent)
                  .then(statsRes => {
                    if ((statsRes.code === 200 || statsRes.code === 0) && statsRes.data) {
                      this.stats = {
                        follows: statsRes.data.follows || statsRes.data.followCount || 0,
                        fans: statsRes.data.fans || statsRes.data.fansCount || 0,
                        dynamics: statsRes.data.dynamics || statsRes.data.postCount || 0,
                        likes: statsRes.data.likes || statsRes.data.likeCount || 0
                      }
                      this.followCount = this.stats.follows
                      this.fansCount = this.stats.fans
                      this.postCount = this.stats.dynamics
                      this.likeCount = this.stats.likes
                    } else {
                      // 使用默认值
                      this.stats = {
                        follows: 0,
                        fans: 0,
                        dynamics: 0,
                        likes: 0
                      }
                      this.followCount = 0
                      this.fansCount = 0
                      this.postCount = 0
                      this.likeCount = 0
                    }
                  })
                  .catch(statsErr => {
                    console.error('加载用户统计失败:', statsErr)
                    // 如果是401错误且是刚登录，不处理（避免清除存储）
                    if (statsErr.message && statsErr.message.includes('认证失败')) {
                      // 静默处理错误
                      // 3秒后重试一次
                      setTimeout(() => {
                        api.getUserStats(statsUserId, !silent)
                          .then(statsRes => {
                            if ((statsRes.code === 200 || statsRes.code === 0) && statsRes.data) {
                              this.stats = {
                                follows: statsRes.data.follows || statsRes.data.followCount || 0,
                                fans: statsRes.data.fans || statsRes.data.fansCount || 0,
                                dynamics: statsRes.data.dynamics || statsRes.data.postCount || 0,
                                likes: statsRes.data.likes || statsRes.data.likeCount || 0
                              }
                              this.followCount = this.stats.follows
                              this.fansCount = this.stats.fans
                              this.postCount = this.stats.dynamics
                              this.likeCount = this.stats.likes
                            }
                          })
                          .catch(() => {
                            // 重试失败，忽略
                          })
                      }, 3000)
                    } else {
                      // 使用默认值
                      this.stats = {
                        follows: 0,
                        fans: 0,
                        dynamics: 0,
                        likes: 0
                      }
                      this.followCount = 0
                      this.fansCount = 0
                      this.postCount = 0
                      this.likeCount = 0
                    }
                  })
              }, 500) // 延迟500ms加载统计信息
            } else {
              this.stats = {
                follows: 0,
                fans: 0,
                dynamics: 0,
                likes: 0
              }
            }
          }
        })
        .catch(err => {
          console.error('加载用户数据失败:', err)
          
          // 如果用户不存在或已被禁用，清除本地存储并跳转到登录页
          if (err.message && (err.message.includes('用户不存在') || err.message.includes('已被禁用'))) {
            uni.removeStorageSync('token')
            uni.removeStorageSync('userId')
            uni.removeStorageSync('userInfo')
            // 更新Vuex状态
            this.$store.commit('SET_TOKEN', null)
            this.$store.commit('SET_USER_INFO', null)
            
            util.showToast('用户信息异常，请重新登录', 'none')
            setTimeout(() => {
              uni.reLaunch({
                url: '/pages-auth/login'
              })
            }, 1500)
            return
          }
          
          // 如果是401错误，说明token过期，但不应该清除存储（因为可能是刚登录）
          // 只有在明确是认证失败时才清除
          if (err.message && err.message.includes('未授权') && err.message.includes('重新登录')) {
            // 不自动跳转，让用户手动操作
          }
          
          // 即使获取用户信息失败，也尝试加载统计信息（静默时不弹 loading）
          if (currentUserId) {
            const statsUserId = typeof currentUserId === 'string' ? parseInt(currentUserId) : currentUserId
            api.getUserStats(statsUserId, !silent)
              .then(statsRes => {
                if ((statsRes.code === 200 || statsRes.code === 0) && statsRes.data) {
                  this.stats = {
                    follows: statsRes.data.follows || statsRes.data.followCount || 0,
                    fans: statsRes.data.fans || statsRes.data.fansCount || 0,
                    dynamics: statsRes.data.dynamics || statsRes.data.postCount || 0,
                    likes: statsRes.data.likes || statsRes.data.likeCount || 0
                  }
                }
              })
              .catch(statsErr => {
                console.error('加载用户统计失败（备用）:', statsErr)
              })
          }
          
          uni.showToast({
            title: '加载数据失败',
            icon: 'none'
          })
        })
    },

    // 处理签到
    handleSignIn() {
      if (this.hasSignedToday) {
        uni.showToast({
          title: '今日已签到',
          icon: 'none'
        })
        return
      }

      uni.showLoading({
        title: '签到中...'
      })

      // 调用签到API
      api.signIn()
        .then(res => {
          uni.hideLoading()
          if (res.code === 200 || res.code === 0) {
            this.hasSignedToday = true
            this.canAmount += res.data.points || 2 // 签到获得积分
            uni.showToast({
              title: `签到成功，获得${res.data.points || 2}积分`,
              icon: 'success'
            })
          } else {
            uni.showToast({
              title: res.msg || '签到失败',
              icon: 'none'
            })
          }
        })
        .catch(err => {
          uni.hideLoading()
          console.error('签到失败:', err)
          uni.showToast({
            title: '签到失败，请重试',
            icon: 'none'
          })
        })
    },

      // 直接执行签到（在"我的"页面）
      async handleSignInDirect() {
        if (this.hasSignedToday) {
          uni.showToast({
            title: '今日已签到',
            icon: 'none'
          })
          return
        }

        try {
          uni.showLoading({
            title: '签到中...'
          })

          // 调用签到API
          const res = await api.doSignIn()
          uni.hideLoading()

          if (res.code === 200 || res.code === 0) {
            const points = res.data?.points || 0
            const balance = res.data?.balance !== undefined && res.data?.balance !== null ? res.data?.balance : (this.canAmount + points)
            
            this.hasSignedToday = true
            this.canAmount = balance
            
            uni.showToast({
              title: `签到成功，获得${points}g`,
              icon: 'success',
              duration: 2000
            })
            
            // 重新加载用户数据，确保数据同步
            setTimeout(() => {
              this.loadUserData()
            }, 500)
          } else {
            uni.showToast({
              title: res.msg || '签到失败',
              icon: 'none'
            })
          }
        } catch (err) {
          uni.hideLoading()
          console.error('签到失败:', err)
          uni.showToast({
            title: '签到失败，请重试',
            icon: 'none'
          })
        }
      },

      // 跳转到签到页面
      goToSignIn() {
        uni.navigateTo({
          url: '/pages-community/signin',
          success: () => {
          },
          fail: (err) => {
            console.error('跳转签到页面失败:', err)
            uni.showToast({
              title: '跳转失败，请重试',
              icon: 'none'
            })
          }
        })
      },
      
      // 跳转到兑换页面
      goToExchange() {
        uni.navigateTo({
          url: '/points-mall/index'
        })
      },

    goOrders(status) {
      uni.navigateTo({
        url: `/order/list?status=${status}`
      })
    },

    // 隐藏所有加载提示
    hideAllLoading() {
      // #ifdef MP-WEIXIN
      try {
        wx.hideNavigationBarLoading()
        wx.hideLoading()
      } catch (e) {}
      // #endif
      try {
        uni.hideLoading()
        uni.hideNavigationBarLoading && uni.hideNavigationBarLoading()
      } catch (e) {}
    },

    /** 与 data-svc 对应（不在模板里写带斜杠的路径，避免小程序端事件绑定失败） */
    servicePageUrls() {
      return {
        pets: '/user/my-pets',
        cans: '/user/my-cans',
        appointments: '/user/my-appointments',
        collections: '/user/my-collections',
        orders: '/order/list',
        cart: '/pages-cart/index',
        address: '/address/list'
      }
    },

    /** 与 servicePageUrls 中路径对应的分包 name（pages.json subPackages.name） */
    subPackageNameForNavigateUrl(url) {
      if (!url || typeof url !== 'string') return ''
      const path = url.split('?')[0]
      if (path.indexOf('/user/') === 0) return 'user'
      if (path.indexOf('/order/') === 0) return 'order'
      if (path.indexOf('/pages-cart/') === 0) return 'cart'
      if (path.indexOf('/address/') === 0) return 'address'
      return ''
    },

    navigateToUrl(url) {
      if (!url) return
      this.hideAllLoading()
      const onNavFail = (err) => {
        const msg = (err && (err.errMsg || err.message)) ? String(err.errMsg || err.message) : '\u65e0\u6cd5\u6253\u5f00\u9875\u9762'
        console.error('[main/index] navigateTo fail', url, err)
        uni.showToast({
          title: msg.length > 40 ? msg.slice(0, 40) + '...' : msg,
          icon: 'none',
          duration: 3500
        })
      }
      const go = () => {
        uni.navigateTo({
          url,
          fail: (err) => {
            const em = err && err.errMsg ? String(err.errMsg) : ''
            if (em.indexOf('timeout') !== -1) {
              setTimeout(() => {
                uni.navigateTo({ url, fail: onNavFail })
              }, 320)
              return
            }
            onNavFail(err)
          }
        })
      }
      // 小程序：先确保目标分包就绪再跳转，避免与 preload 多分包并发导致 navigateTo timeout
      // #ifdef MP-WEIXIN
      const pkg = this.subPackageNameForNavigateUrl(url)
      if (pkg && typeof wx !== 'undefined' && wx.loadSubPackage) {
        wx.loadSubPackage({
          name: pkg,
          success: () => setTimeout(go, 24),
          fail: (e) => {
            console.warn('[main/index] loadSubPackage', pkg, e)
            setTimeout(go, 24)
          }
        })
        return
      }
      setTimeout(go, 24)
      // #endif
      // #ifndef MP-WEIXIN
      go()
      // #endif
    },

    onServiceItemTap(e) {
      const ds = e.currentTarget && e.currentTarget.dataset ? e.currentTarget.dataset : {}
      const svc = ds.svc
      const map = this.servicePageUrls()
      const url = svc ? map[svc] : ''
      if (!url) {
        console.warn('[main/index] onServiceItemTap: missing svc or url', ds)
        return
      }
      // 跳转逻辑见 navigateToUrl：先 loadSubPackage 再 navigateTo，避免分包未就绪导致 timeout
      this.navigateToUrl(url)
    },

    goCustomerService() {
      uni.navigateTo({
        url: '/chat/customer-service?isPlatform=true'
      })
    },

    // 查看系统公告
    goAnnouncement() {
      uni.navigateTo({
        url: '/user/announcement'
      })
    },


    // 我的权益相关方法

    handleReport() {
      uni.navigateTo({
        url: '/settings/report'
      })
    },

    handleAbout() {
      uni.navigateTo({
        url: '/settings/about'
      })
    },


    handleSettings() {
      uni.navigateTo({
        url: '/settings/index'
      })
    },

    goWorkOrders() {
      if (!this.isStaffLoggedIn) {
        uni.navigateTo({ url: '/pages-auth/login' })
        return
      }
      uni.navigateTo({ url: '/admin/work-orders' })
    },

    goOrderVerify() {
      if (!this.isStaffLoggedIn) {
        uni.navigateTo({ url: '/pages-auth/login' })
        return
      }
      uni.navigateTo({ url: '/admin/verify' })
    },

    goMySchedule() {
      if (!this.isStaffLoggedIn) {
        uni.navigateTo({ url: '/pages-auth/login' })
        return
      }
      uni.navigateTo({ url: '/admin/my-schedule' })
    },

    goMyReviews() {
      if (!this.isStaffLoggedIn) {
        uni.navigateTo({ url: '/pages-auth/login' })
        return
      }
      uni.navigateTo({ url: '/admin/my-reviews' })
    },

    staffLogout() {
      uni.removeStorageSync('staffToken')
      uni.removeStorageSync('staffInfo')
      this.staffLoggedIn = false
      this.staffInfo = null
      uni.showToast({ title: '已退出', icon: 'none' })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages-auth/login' })
      }, 200)
    },

    // 跳转到个人资料页面
    goToProfile() {
      uni.navigateTo({
        url: '/user/profile'
      })
    },
    
    // 处理头像加载错误
    handleAvatarError(e) {
      // 真机常见：静态资源或网络头像加载失败，静默使用默认图即可
      // 如果是默认头像也加载失败，不再尝试，避免无限循环
      if (this.currentUserAvatar && (this.currentUserAvatar.includes('login-dog.png') || this.currentUserAvatar.includes('pet-paw.png'))) {
        return
      }
      // 如果其他头像加载失败，设置为默认图片
      this.currentUserAvatar = '/static/images/login-dog.png'
      if (this.localUserInfo) {
        this.localUserInfo.avatar = '/static/images/login-dog.png'
      }
    },

    // 跳转到关注/粉丝/动态列表
    gotoFollowingTabs(tabType) {
      const localInfo = this.localUserInfo || {}
      const vuexInfo = this.userInfo || {}
      const currentUserId = localInfo.id || localInfo.uid || vuexInfo.id || vuexInfo.uid || uni.getStorageSync('userId') || null
      const currentUsername = localInfo.username || localInfo.nickname || vuexInfo.username || uni.getStorageSync('username') || '用户'
      
      // 获取当前用户的昵称
      const userNickname = this.userNickname || currentUsername || '用户'
      
      uni.navigateTo({
        url: `/user/FollowingTabs?ownerId=${currentUserId}&ownerName=${encodeURIComponent(userNickname)}&defaultTab=${tabType}`
      })
    },

  }
}
</script>

<style lang="scss" scoped>
.user-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

/* 移除页面标题栏样式 */

/* 登录区域 */
.login-section {
  padding: 40rpx;
}

.login-card {
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

.login-gradient-bg {
  background-color: white;
  padding: 60rpx 40rpx;
  text-align: center;
  color: #333;

  .login-avatar {
    margin-bottom: 30rpx;

    .avatar-large {
      width: 120rpx;
      height: 120rpx;
      background-color: #f0f0f0;
      border: 2rpx solid #e0e0e0;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 60rpx;
      color: #999;
      margin: 0 auto;
    }
  }

  .login-text {
    font-size: 28rpx;
    margin-bottom: 30rpx;
    color: #666;
  }

  .login-btn {
    font-size: 32rpx;
    font-weight: bold;
    padding: 20rpx 60rpx;
    background-color: #ff6b35;
    color: white;
    border: none;
    border-radius: 12rpx;
    display: inline-block;
  }
}

/* 主内容区（原 custom-pull-refresh 槽位，勿再包一层会劫持 touchmove 的组件） */
.main-page-inner {
  width: 100%;
  min-height: 100vh;
  box-sizing: border-box;
}

/* 用户区域 */
.user-section {
  background-color: #f5f5f5;
  /* padding-top 通过内联样式动态设置，适配不同设备 */
}

/* 黄色背景区域（半圆形，紧贴自定义导航栏） */
.yellow-background-area {
  background: linear-gradient(to right, #FF8C00 0%, #FFD700 100%);
  height: 280rpx; /* 减少高度，让半圆更合适 */
  border-bottom-left-radius: 50%; /* 真正的半圆形状 */
  border-bottom-right-radius: 50%; /* 真正的半圆形状 */
  position: relative;
  z-index: 1;
  margin-top: -20rpx; /* 负边距，让半圆往上贴到导航栏 */
  margin-left: 0; /* 从屏幕左边开始 */
  margin-right: 0; /* 到屏幕右边结束 */
  width: 100%; /* 全屏宽度 */
  pointer-events: none; /* 装饰层不抢触摸，避免挡住下方卡片点击 */
}

/* 顶部状态栏 */
.top-status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 30rpx 10rpx;
  
  .status-left {
    display: flex;
    gap: 20rpx;
    
    .bell-icon, .scan-icon {
      font-size: 32rpx;
      color: #333;
    }
  }
  
  .status-right {
    display: flex;
    gap: 15rpx;
    
    .status-icon {
      width: 60rpx;
      height: 60rpx;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24rpx;
      color: #333;
      
      &.active {
        background: rgba(255, 255, 255, 0.3);
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          top: 8rpx;
          right: 8rpx;
          width: 12rpx;
          height: 12rpx;
          background: #ff4757;
          border-radius: 50%;
        }
      }
    }
  }
}

/* 白色个人信息卡片 */
.user-profile-card {
  background-color: white;
  margin: -220rpx 40rpx 20rpx; /* 继续增加负上边距，让卡片再往上挪 */
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 10;
}

/* 用户信息主体 */
.user-info-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
}

/* 用户头像和信息区域 */
.user-info-main {
  transition: background-color 0.2s ease;
}

.user-info-main:active {
  background-color: #f8f8f8;
}

.user-avatar-section {
  display: flex;
  align-items: center;
  flex: 1;
}

.user-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #fff;
  border: 3rpx solid rgba(255, 255, 255, 0.8);
  overflow: hidden;
  margin-right: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);

  image {
    width: 100%;
    height: 100%;
  }

  .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 50rpx;
    background-color: #fff;
  }
}

.user-basic-info {
  flex: 1;
  color: #333;

  .user-name {
    font-size: 32rpx;
    font-weight: bold;
    margin-bottom: 12rpx;
    color: #333;
  }

  .user-level-assets {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .can-amount {
      display: flex;
      align-items: center;
      background: rgba(255, 255, 255, 0.2);
      padding: 4rpx 8rpx;
      border-radius: 8rpx;

      .can-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 4rpx;
        
        .can-icon-image {
          width: 20rpx;
          height: 20rpx;
        }
      }

      .can-text {
        font-size: 22rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12rpx;

  .sign-btn {
    background-color: #ff4757;
    color: white;
    font-size: 22rpx;
    padding: 12rpx 20rpx;
    border-radius: 20rpx;
    text-align: center;
    font-weight: bold;
    box-shadow: 0 4rpx 12rpx rgba(255, 71, 87, 0.3);
    transition: all 0.3s ease;
    
    &.disabled {
      background-color: #ccc;
      box-shadow: none;
    }
    
    &:active:not(.disabled) {
      transform: translateY(2rpx);
      box-shadow: 0 2rpx 6rpx rgba(255, 71, 87, 0.3);
    }
  }

  .redeem-btn {
    background-color: white;
    color: #ff4757;
    font-size: 22rpx;
    padding: 10rpx 20rpx;
    border: 2rpx solid #ff4757;
    border-radius: 20rpx;
    text-align: center;
    font-weight: bold;
    box-shadow: 0 4rpx 12rpx rgba(255, 71, 87, 0.2);
    transition: all 0.3s ease;
    
    &:active {
      transform: translateY(2rpx);
      box-shadow: 0 2rpx 6rpx rgba(255, 71, 87, 0.2);
    }
  }
}

/* 用户统计信息 */
.user-stats {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx 30rpx;
  background: white;

  .stat-item {
    flex: 1;
    text-align: center;

    .stat-number {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      margin-bottom: 6rpx;
    }

    .stat-label {
      font-size: 22rpx;
      color: #666;
      font-weight: 500;
    }
  }

  .stat-divider {
    width: 1rpx;
    height: 30rpx;
    background-color: #e0e0e0;
    margin: 0 15rpx;
  }
}

/* 订单功能区 */
.orders-card {
  background-color: white;
  margin: 0 20rpx 20rpx;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 20; /* 高于用户信息卡(10)，避免层叠区域点不中 */
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 40rpx;
  border-bottom: 1rpx solid #f8f8f8;

  .card-title {
    display: flex;
    align-items: center;
    font-size: 28rpx;
    font-weight: normal;
    color: #333;
  }

  .card-more {
    font-size: 26rpx;
    color: #ff6b35;
  }
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15rpx;
  padding: 30rpx 20rpx;
}

.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15rpx;
  position: relative;
  z-index: 1;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  text-decoration: none;
  color: inherit;
  box-sizing: border-box;
  
  &:active {
    opacity: 0.7;
  }

  .order-icon {
    font-size: 48rpx;
    margin-bottom: 12rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .order-icon-image {
      width: 48rpx;
      height: 48rpx;
    }
  }

  .order-text {
    font-size: 24rpx;
    color: #666;
  }
}


/* 下拉刷新提示区域 */
.pull-refresh-tip {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  transition: height 0.1s ease;
}

.pull-refresh-text {
  color: #666;
  font-size: 24rpx;
  font-weight: 400;
  white-space: nowrap;
}

/* 标题装饰条 */
.title-decoration {
  width: 6rpx;
  height: 32rpx;
  background: linear-gradient(to bottom, #FF8C00, #FFD700);
  border-radius: 3rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

/* 我的权益卡片 */
.rights-card {
  background: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: 20rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 20;
}

/* 与「我的服务」一致的 4 列网格，保证上下对齐 */
.rights-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15rpx;
  padding: 30rpx 20rpx;
}

.rights-grid-staff {
  /* 工作人员模式下仍为 4 列，仅项数不同 */
  grid-template-columns: repeat(4, 1fr);
}

.right-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15rpx;
  border-radius: 16rpx;
  transition: background-color 0.2s ease;
  position: relative;
  z-index: 1;
  -webkit-tap-highlight-color: transparent;
}

.right-item:active {
  background-color: #f5f5f5;
}

.right-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.right-icon-image {
  width: 48rpx;
  height: 48rpx;
}

.right-text {
  font-size: 24rpx;
  color: #666;
  text-align: center;
  line-height: 1.2;
}
</style>

