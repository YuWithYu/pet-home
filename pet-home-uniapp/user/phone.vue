<template>
  <view class="phone-container">
    <view class="phone-content">
      <view class="phone-line">
        <text class="phone-label">{{ ui.labelBound }}</text>
        <text class="phone-value">{{ displayPhone }}</text>
      </view>
    </view>
    <view class="btn-wrap">
      <view class="btn-change" @tap="goChange">{{ ui.btnChange }}</view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { mapGetters } from 'vuex'

export default {
  name: 'Phone',
  data() {
    return {
      phone: '',
      displayPhone: '',
      ui: {
        labelBound: '\u5df2\u7ed1\u5b9a\u624b\u673a\u53f7\uff1a',
        btnChange: '\u66f4\u6362\u624b\u673a\u53f7',
        unbound: '\u672a\u7ed1\u5b9a\u624b\u673a\u53f7'
      }
    }
  },
  computed: {
    ...mapGetters(['userInfo'])
  },
  onLoad() {
    this.syncDisplay()
    this.loadPhone()
  },
  onShow() {
    this.loadPhone()
  },
  methods: {
    syncDisplay() {
      const p = (this.phone || '').trim()
      this.displayPhone = p || this.ui.unbound
    },
    loadPhone() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId =
        (this.userInfo && (this.userInfo.id != null ? this.userInfo.id : this.userInfo.uid)) ||
        uni.getStorageSync('userId')
      const username = uni.getStorageSync('username')
      let p = (userInfo.phone || userInfo.phoneNumber || '').trim()
      if (p) {
        this.phone = p
        this.syncDisplay()
        return
      }
      this.phone = ''
      this.syncDisplay()
      const params = { showLoading: false }
      if (userId) params.userId = userId
      else if (username) params.username = username
      api.getCurrentUser(params)
        .then(res => {
          if (res && res.data) {
            const d = res.data
            const ph = (d.phone || d.phoneNumber || '').trim()
            this.phone = ph || ''
            this.syncDisplay()
            if (ph && userInfo) {
              try {
                const next = { ...userInfo, phone: ph }
                uni.setStorageSync('userInfo', next)
              } catch (e) {}
            }
          }
        })
        .catch(() => {})
    },
    goChange() {
      uni.navigateTo({ url: '/user/verify-phone' })
    }
  }
}
</script>

<style lang="scss" scoped>
.phone-container {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.phone-content {
  flex: 1;
  padding: 40rpx 32rpx 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
}
.phone-line {
  margin-bottom: 12rpx;
  text-align: center;
  width: 100%;
}
.phone-label,
.phone-value {
  font-size: 30rpx;
  color: #333;
  font-weight: normal;
}
.btn-wrap {
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}
.btn-change {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 28rpx;
  color: #333;
  background-color: #ffd700;
  border-radius: 8rpx;
}
</style>
