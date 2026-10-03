<template>
  <view class="login-page">
    <view class="page-wrapper">
      <view class="header-container">
        <view class="header-bg">
          <view class="header-text-box">
            <text class="header-line">Hi Welcome</text>
            <text class="header-line">Back</text>
          </view>
        </view>
      </view>

      <image class="dog-image" src="/static/images/login-dog.png" mode="aspectFit"></image>

      <view class="content-card">
        <!-- 用户/工作人员切换 -->
        <view class="login-mode-switch">
          <view
            class="mode-tab"
            :class="{ active: !isStaffMode }"
            hover-class="mode-tab-hover"
            @tap="switchMode(false)"
          >
            用户登录
          </view>
          <view
            class="mode-tab"
            :class="{ active: isStaffMode }"
            hover-class="mode-tab-hover"
            @tap="switchMode(true)"
          >
            工作人员登录
          </view>
        </view>

        <view class="field-group">
          <text class="field-label">{{ isStaffMode ? '账号' : '手机号/账号' }}</text>
          <view class="field-line-wrapper">
            <input
              class="field-input"
              type="text"
              v-model="accountInput"
              @input="onAccountInput"
              @blur="onAccountBlur"
              :maxlength="isStaffMode ? 50 : 50"
              :placeholder="isStaffMode ? '请输入账号' : '请输入手机号或账号'"
            />
          </view>
        </view>

        <view class="field-group password-group">
          <text class="field-label">密码</text>
          <view class="field-line-wrapper">
            <input
              class="field-input"
              :type="showPassword ? 'text' : 'password'"
              v-model="password"
              @input="onPasswordInput"
              @blur="onPasswordBlur"
            />
            <view
              v-if="!isStaffMode"
              class="forgot-wrap"
              hover-class="forgot-wrap-hover"
              @tap.stop="onForgotPassword"
            >
              <text class="forgot-text">忘记密码</text>
            </view>
          </view>
        </view>

        <view class="button-group">
          <button
            class="btn btn-login"
            :class="{ 'is-disabled': !canLogin }"
            :disabled="!canLogin"
            @click="onLoginTap"
          >
            {{ loggingIn ? '登录中...' : '登录' }}
          </button>
          <button class="btn btn-register" @click="onRegisterTap" v-if="!isStaffMode">注册</button>
          
          <!-- 微信登录按钮 -->
          <view class="wechat-login-wrapper" v-if="!isStaffMode">
            <view class="divider">
              <view class="divider-line"></view>
              <text class="divider-text">或</text>
              <view class="divider-line"></view>
            </view>
            <button class="btn btn-wechat" @click="onWechatLogin" :disabled="loggingIn">
              <image class="wechat-icon" src="/static/images/微信.png" mode="aspectFit"></image>
              <text class="wechat-text">微信一键登录</text>
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'
import { mapMutations } from 'vuex'

export default {
  name: 'Login',

  data() {
    return {
      accountInput: '',
      password: '',
      showPassword: false,
      canLogin: false,
      loggingIn: false,
      isStaffMode: false
    }
  },

  mounted() {
    this.accountInput = ''
    this.password = ''
  },

  methods: {
    ...mapMutations(['SET_USER_INFO', 'SET_TOKEN']),
    
    // 切换用户/工作人员模式
    switchMode(staff) {
      if (this.isStaffMode === staff) return
      this.isStaffMode = staff
      this.accountInput = ''
      this.password = ''
      this.validateForm()
    },

    // 账号输入（手机号或账号）
    onAccountInput(e) {
      this.accountInput = e.detail.value
      this.validateForm()
    },

    onAccountBlur() {
      if (!this.isStaffMode && this.accountInput && this.accountInput.trim().length < 2) {
        util.showToast('请输入手机号或账号（至少2位）', 'none')
      }
    },

    // 密码输入
    onPasswordInput(e) {
      this.password = e.detail.value
      this.validateForm()
    },

    // 密码失去焦点
    onPasswordBlur() {
      if (this.password && this.password.length < 6) {
        util.showToast('密码至少6位', 'none')
      }
    },

    // 显示/隐藏密码
    togglePassword() {
      this.showPassword = !this.showPassword
    },

    // 表单验证（仅支持手机号或账号登录，昵称不能用于登录）
    validateForm() {
      if (this.isStaffMode) {
        this.canLogin = !!this.accountInput.trim() && !!this.password
      } else {
        this.canLogin = !!this.accountInput.trim() &&
                       this.accountInput.trim().length >= 2 &&
                       !!this.password &&
                       this.password.length >= 6
      }
    },

    // 登录
    onLoginTap() {
      if (!this.canLogin || this.loggingIn) return

      this.loggingIn = true

      if (this.isStaffMode) {
        this.doStaffLogin()
        return
      }

      // 用户登录（仅手机号或账号+密码；昵称不能登录，请用账号）
      api.loginByPhone(this.accountInput.trim(), this.password).then(res => {
        if (res.code === 200 || res.code === 0) {
          // 清除工作人员相关存储，避免角色混淆
          uni.removeStorageSync('staffToken')
          uni.removeStorageSync('staffInfo')
          // 登录成功，保存用户信息和token
          const { token, userId, uid, nickname, username, avatar } = res.data || {}
          
          // 确保有有效的用户ID
          const finalUserId = userId || uid
          if (!finalUserId) {
            util.showToast('登录失败：未获取到用户ID', 'none')
            return
          }
          
          // 保存到本地存储（兼容userId和uid）
          uni.setStorageSync('token', token)
          uni.setStorageSync('userId', finalUserId) // 保存用户ID
          const userInfo = {
            id: finalUserId, // 保存id字段
            uid: finalUserId, // 兼容uid字段
            phone: this.accountInput,
            username: username || this.accountInput, // 保存username但不用于显示
            nickname: nickname || this.accountInput, // 只保存nickname，不用手机号作为fallback
            avatar: avatar || '/static/images/garfield-default-avatar.png'
          }
          uni.setStorageSync('userInfo', userInfo)
          // 单独保存账号名，避免昵称编辑流程误影响账号展示
          if (userInfo.username) {
            uni.setStorageSync('username', userInfo.username)
          }
          // 保存登录时间戳，用于401错误处理时判断是否刚登录
          uni.setStorageSync('loginTime', Date.now())
          
          // 更新Vuex store，确保isLoggedIn立即生效
          this.SET_TOKEN(token)
          this.SET_USER_INFO(userInfo)

          util.showToast('登录成功', 'success')

          // 延迟跳转，确保token和用户信息已完全保存
          // 增加延迟时间，避免跳转后立即调用接口时token还未生效
          setTimeout(() => {
            // 再次验证token和userId是否已保存
            const verifyToken = uni.getStorageSync('token')
            const verifyUserId = uni.getStorageSync('userId')
            if (!verifyToken || !verifyUserId) {
              util.showToast('登录信息保存失败，请重新登录', 'none')
              return
            }
            uni.switchTab({
              url: '/pages/main/index',
              success: () => {
              },
              fail: (err) => {
                console.error('跳转失败:', err)
              }
            })
          }, 1500) // 延迟1.5秒，确保token完全保存
        } else {
          // 检查错误信息，显示对应的提示
          const errorMsg = res.msg || '登录失败'
          if (errorMsg && errorMsg.includes('封禁')) {
            util.showToast('该账号已被封禁', 'none', 3000)
          } else if (errorMsg && errorMsg.includes('账号不存在')) {
            util.showToast('请使用手机号或用户名登录，不支持昵称登录', 'none', 3000)
          } else if (errorMsg && errorMsg.includes('密码错误')) {
            util.showToast('密码错误', 'none')
          } else {
            util.showToast(errorMsg, 'none')
          }
        }
      }).catch(err => {
        console.error('登录失败:', err)
        const errorMsg = err.message || err.msg || (err.data && err.data.msg) || err.toString()
        if (errorMsg && errorMsg.includes('封禁')) {
          util.showToast('该账号已被封禁', 'none', 3000)
        } else if (errorMsg && errorMsg.includes('账号不存在')) {
          util.showToast('请使用手机号或用户名登录，不支持昵称登录', 'none', 3000)
        } else if (errorMsg && errorMsg.includes('密码错误')) {
          util.showToast('密码错误', 'none')
        } else {
          util.showToast('登录失败，请检查网络连接', 'none')
        }
      }).finally(() => {
        this.loggingIn = false
      })
    },

    // 工作人员登录（同一页面内完成，跳转到「我的」）
    async doStaffLogin() {
      try {
        const res = await api.staffLogin(this.accountInput.trim(), this.password)
        if (res.code === 200 || res.code === 0) {
          const data = res.data || {}
          const token = data.token
          if (!token) {
            util.showToast('登录失败：未获取到Token', 'none')
            return
          }
          // 清除用户相关存储，避免角色混淆
          uni.removeStorageSync('token')
          uni.removeStorageSync('userId')
          uni.removeStorageSync('userInfo')
          this.SET_TOKEN('')
          this.SET_USER_INFO(null)
          // 保存工作人员信息
          uni.setStorageSync('staffToken', token)
          uni.setStorageSync('staffInfo', {
            adminId: data.adminId,
            username: data.username,
            name: data.name,
            role: data.role,
            department: data.department,
            avatar: data.avatar || ''
          })
          util.showToast('登录成功', 'success')
          setTimeout(() => {
            uni.switchTab({ url: '/pages/main/index' })
          }, 800)
        } else {
          util.showToast(res.msg || '登录失败', 'none')
        }
      } catch (err) {
        console.error('工作人员登录失败:', err)
        util.showToast(err.message || '登录失败，请重试', 'none')
      } finally {
        this.loggingIn = false
      }
    },

    // 注册
    onRegisterTap() {
      uni.navigateTo({
        url: '/pages-auth/register'
      })
    },

    // 忘记密码
    onForgotPassword() {
      uni.navigateTo({
        url: '/pages-auth/forgot-password'
      })
    },

    // 微信登录：用 code 调后端，成功则写存储并跳转
    doWechatLoginWithCode(code, isRetry) {
      return api.loginByWechat(code).then(response => {
        if (response.code !== 200 && response.code !== 0) {
          const msg = response.msg || '微信登录失败'
          return Promise.reject(new Error(msg))
        }
        const { token, userId, uid, nickname, username, avatar, userInfo: responseUserInfo } = response.data || {}
        const finalUserId = userId || uid
        if (!finalUserId) {
          return Promise.reject(new Error('登录失败：未获取到用户ID'))
        }
        const userInfo = responseUserInfo || {
          id: finalUserId,
          uid: finalUserId,
          nickname: nickname || '微信用户',
          username: username || '',
          avatar: avatar || '/static/images/garfield-default-avatar.png'
        }
        if (!userInfo.id) userInfo.id = finalUserId
        if (!userInfo.uid) userInfo.uid = finalUserId
        if (!userInfo.avatar) userInfo.avatar = '/static/images/garfield-default-avatar.png'
        if (!userInfo.nickname) userInfo.nickname = nickname || '微信用户'

        uni.setStorageSync('token', token)
        uni.setStorageSync('userId', finalUserId)
        uni.setStorageSync('userInfo', userInfo)
        // 单独保存账号名，避免昵称编辑流程误影响账号展示
        if (userInfo.username) {
          uni.setStorageSync('username', userInfo.username)
        }
        uni.setStorageSync('loginTime', Date.now())
        this.SET_TOKEN(token)
        this.SET_USER_INFO(userInfo)
        const phoneVal = (userInfo.phone || userInfo.phoneNumber || '').trim()
        const hasPhone = phoneVal.length >= 11
        if (hasPhone) {
          util.showToast('登录成功', 'success')
          setTimeout(() => {
            const verifyToken = uni.getStorageSync('token')
            const verifyUserId = uni.getStorageSync('userId')
            if (!verifyToken || !verifyUserId) {
              util.showToast('登录信息保存失败，请重新登录', 'none')
              return
            }
            uni.switchTab({ url: '/pages/main/index', fail: (e) => console.error('跳转失败:', e) })
          }, 1500)
        } else {
          util.showToast('登录成功，请选择是否绑定手机号', 'none')
          setTimeout(() => {
            const verifyToken = uni.getStorageSync('token')
            const verifyUserId = uni.getStorageSync('userId')
            if (!verifyToken || !verifyUserId) {
              util.showToast('登录信息保存失败，请重新登录', 'none')
              return
            }
            uni.navigateTo({ url: '/user/edit-profile?mustBindPhone=1', fail: (e) => console.error('跳转失败:', e) })
          }, 800)
        }
        return response
      })
    },

    // 微信登录
    onWechatLogin() {
      if (this.loggingIn) return
      this.loggingIn = true

      let retrying = false
      const tryLogin = (code, isRetry) => {
        this.doWechatLoginWithCode(code, isRetry)
          .catch(err => {
            const errorMsg = err.message || err.msg || (err && err.toString()) || ''
            const isCodeInvalid = /过期|无效|40029/.test(errorMsg)
            if (isCodeInvalid && !isRetry) {
              retrying = true
              uni.login({
                provider: 'weixin',
                success: (res2) => {
                  if (res2.code) {
                    tryLogin(res2.code, true)
                    return
                  }
                  this.loggingIn = false
                  util.showToast('登录码已失效，请重新点击「微信一键登录」', 'none')
                },
                fail: () => {
                  this.loggingIn = false
                  util.showToast('登录码已失效，请重新点击「微信一键登录」', 'none')
                }
              })
              return
            }
            if (isCodeInvalid) {
              util.showToast('登录码已失效，请重新点击「微信一键登录」', 'none')
              // 重试后仍失败时，提示检查服务器配置
              if (isRetry) {
                setTimeout(() => {
                  uni.showModal({
                    title: '微信登录失败',
                    content: '多为服务器未配置本小程序的 AppSecret。请管理员在服务器上修改配置：微信公众平台→开发管理→开发设置，复制本小程序(wxcf8868d6bfdc0d6f)的 AppSecret 填到后端的 wechat.miniapp.secret，并重启后端。',
                    showCancel: true,
                    confirmText: '知道了'
                  })
                }, 500)
              }
            } else if (errorMsg.includes('封禁')) {
              util.showToast('该账号已被封禁', 'none', 3000)
            } else if (errorMsg.includes('账号不存在')) {
              util.showToast('当前账号不存在', 'none')
            } else if (errorMsg.includes('密码错误')) {
              util.showToast('密码错误', 'none')
            } else {
              util.showToast(errorMsg || '微信登录失败，请检查网络', 'none')
            }
          })
          .finally(() => {
            if (isRetry) {
              this.loggingIn = false
            } else if (!retrying) {
              this.loggingIn = false
            }
          })
      }

      const doWxLogin = (isRetryLogin) => {
        uni.login({
          provider: 'weixin',
          success: (res) => {
            if (res.code) {
              tryLogin(res.code, false)
            } else {
              this.loggingIn = false
              util.showToast('微信授权失败', 'none')
            }
          },
          fail: (err) => {
            const msg = (err && err.errMsg) || ''
            console.error('微信登录调用失败:', err)
            // login:fail 需要重新登录：多为开发者工具未登录微信或会话失效，真机请到微信里打开小程序再试
            if (msg.includes('需要重新登录')) {
              if (!isRetryLogin) {
                // 自动重试一次，有时会话刚过期可恢复
                setTimeout(() => {
                  doWxLogin(true)
                }, 800)
                return
              }
              util.showToast('请用手机微信打开小程序再点「微信一键登录」；在电脑开发者工具中请先登录微信账号', 'none')
            } else if (msg.includes('Failed to fetch')) {
              util.showToast('网络连接失败，请检查后端服务', 'none')
            } else {
              util.showToast('微信登录失败：' + (msg || '未知错误'), 'none')
            }
            this.loggingIn = false
          }
        })
      }
      doWxLogin(false)
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  width: 750rpx;
  min-height: 1624rpx;
  margin: 0 auto;
  padding: 0;
  box-sizing: border-box;
  background-color: #ffffff;
  display: flex;
  justify-content: center;
}

.page-wrapper {
  width: 750rpx;
  height: 1624rpx;
  overflow: hidden;
  background-color: rgba(141, 159, 94, 1);
  box-sizing: border-box;
  position: relative;
}

.header-container {
  width: 100%;
  background-color: rgba(141, 159, 94, 1);
}

.header-bg {
  width: 100%;
  background-color: rgba(141, 159, 94, 1);
  padding-top: 140rpx;
  padding-left: 60rpx;
  padding-right: 60rpx;
  padding-bottom: 120rpx;
  position: relative;
  box-sizing: border-box;
  z-index: 1;
}

.header-text-box {
  margin-top: 40rpx;
  z-index: 1;
  position: relative;
}

.header-line {
  display: block;
  color: #ffffff;
  font-size: 56rpx;
  line-height: 80rpx;
  font-weight: 600;
}

.dog-image {
  position: absolute;
  right: 30rpx;
  top: 100rpx;
  width: 450rpx;
  height: 450rpx;
  z-index: 5;
  pointer-events: none;
}

.content-card {
  margin-top: -40rpx;
  padding: 60rpx 50rpx 90rpx;
  box-sizing: border-box;
  background: #ffffff;
  border-radius: 40rpx;
  box-shadow: 0 -8rpx 30rpx rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 15;
}

.field-group {
  margin-bottom: 40rpx;
}

.password-group {
  margin-bottom: 28rpx;
}

.field-label {
  display: block;
  font-size: 26rpx;
  color: rgba(141, 159, 94, 1);
  margin-bottom: 16rpx;
  font-weight: 500;
}

.field-line-wrapper {
  border-bottom: 2rpx solid #E0E0E0;
  padding-bottom: 12rpx;
  position: relative;
  display: flex;
  align-items: center;
  min-height: 62rpx;
}

.field-input {
  font-size: 26rpx;
  color: #333333;
  width: 100%;
  padding: 0;
  margin: 0;
  height: 38rpx;
  line-height: 38rpx;
  border: none;
  background-color: transparent;
  flex: 1;
}

.field-placeholder {
  color: transparent;
}

.forgot-wrap {
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  min-width: 140rpx;
  min-height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  z-index: 10;
}
.forgot-wrap-hover {
  opacity: 0.8;
}
.forgot-text {
  font-size: 24rpx;
  color: rgba(141, 159, 94, 1);
  white-space: nowrap;
}

.button-group {
  margin-top: 40rpx;
}

.btn {
  width: 100%;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  line-height: 80rpx;
  text-align: center;
  margin-bottom: 20rpx;
  border: none;
  padding: 0;
  box-sizing: border-box;
}

.btn-login {
  color: #ffffff;
  background-color: #8BC34A;
  box-shadow: 0 8rpx 20rpx rgba(139, 195, 74, 0.35);
  
  &.is-disabled {
    opacity: 0.5;
    background-color: #E0E0E0;
    color: #999999;
    box-shadow: none;
  }
  
  &:not(.is-disabled):active {
    opacity: 0.9;
    transform: translateY(2rpx);
  }
}

.btn-register {
  color: #666666;
  background-color: #E0E0E0;
  box-shadow: none;
  
  &:active {
    opacity: 0.8;
    transform: translateY(2rpx);
  }
}

.wechat-login-wrapper {
  margin-top: 24rpx;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background-color: #E0E0E0;
}

.divider-text {
  margin: 0 20rpx;
  font-size: 24rpx;
  color: #999999;
}

.btn-wechat {
  color: #ffffff;
  background-color: #07C160;
  box-shadow: 0 6rpx 16rpx rgba(7, 193, 96, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: normal;
  height: 80rpx;
  font-size: 28rpx;
  
  &:active {
    opacity: 0.9;
    transform: translateY(2rpx);
  }
  
  &:disabled {
    opacity: 0.5;
    background-color: #E0E0E0;
    color: #999999;
    box-shadow: none;
  }
}

.wechat-icon {
  width: 32rpx;
  height: 32rpx;
  margin-right: 10rpx;
}

.wechat-text {
  font-size: 28rpx;
  line-height: 1;
}

.login-mode-switch {
  display: flex;
  margin-bottom: 32rpx;
  background: #f0f0f0;
  border-radius: 36rpx;
  padding: 4rpx;
}
.mode-tab {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  font-size: 26rpx;
  color: #666;
  border-radius: 32rpx;
  transition: all 0.2s;
  min-height: 56rpx;
  box-sizing: border-box;
}
.mode-tab-hover {
  opacity: 0.8;
}
.mode-tab.active {
  background: #fff;
  color: #8D9F5E;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06);
}
</style>
