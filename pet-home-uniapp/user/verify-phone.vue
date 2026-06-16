<template>
  <view class="verify-phone-container">
    <view class="verify-phone-content">
      <text class="desc">{{ ui.desc }}</text>
      <view class="field-wrap">
        <text class="field-label">{{ ui.labelPhone }}</text>
        <input
          class="field-input"
          v-model="phone"
          type="number"
          maxlength="11"
          :placeholder="ui.phPhone"
          @input="onPhoneInput"
        />
      </view>
      <view class="field-wrap">
        <text class="field-label">{{ ui.labelCode }}</text>
        <view class="code-row">
          <input
            class="field-input code-input"
            v-model="code"
            type="number"
            maxlength="6"
            :placeholder="ui.phCode"
            @input="onCodeInput"
          />
          <view
            class="btn-code"
            :class="{ active: phoneValid && countdown === 0 }"
            @tap="sendCode"
          >
            <text>{{ codeBtnText }}</text>
          </view>
        </view>
      </view>
    </view>
    <view class="btn-wrap">
      <view
        class="btn-confirm"
        :class="{ active: submitValid }"
        @tap="confirm"
      >
        <text>{{ ui.btnConfirm }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

const PHONE_REG = /^1[3-9]\d{9}$/

export default {
  name: 'VerifyPhone',
  data() {
    return {
      phone: '',
      code: '',
      countdown: 0,
      phoneValid: false,
      submitValid: false,
      codeBtnText: '',
      countdownTimer: null,
      ui: {
        desc:
          '\u4e00\u4e2a\u624b\u673a\u53f7\u53ea\u80fd\u7ed1\u5b9a\u4e00\u4e2a\u8d26\u53f7\uff0c\u66f4\u6362\u540e\u53ef\u4f7f\u7528\u65b0\u624b\u673a\u53f7\u767b\u5f55\u6b64\u8d26\u53f7\u3002\u5bf9\u4e8e\u5df2\u7ed1\u5b9a\u5176\u4ed6\u8d26\u53f7\u7684\u624b\u673a\u53f7\uff0c\u672c\u6b21\u64cd\u4f5c\u540e\u5c06\u4e0e\u539f\u8d26\u53f7\u89e3\u7ed1\u3002',
        labelPhone: '\u65b0\u624b\u673a\u53f7',
        labelCode: '\u9a8c\u8bc1\u7801',
        phPhone: '\u8bf7\u8f93\u5165\u65b0\u624b\u673a\u53f7',
        phCode: '\u8bf7\u8f93\u5165\u9a8c\u8bc1\u7801',
        btnConfirm: '\u786e\u8ba4\u66f4\u6362',
        getCode: '\u83b7\u53d6\u9a8c\u8bc1\u7801',
        secResend: 's\u540e\u91cd\u53d1',
        toastPhone: '\u8bf7\u8f93\u5165\u6b63\u786e\u7684\u624b\u673a\u53f7',
        toastFill: '\u8bf7\u586b\u5199\u65b0\u624b\u673a\u53f7\u548c\u9a8c\u8bc1\u7801',
        sending: '\u53d1\u9001\u4e2d...',
        sentOk: '\u9a8c\u8bc1\u7801\u5df2\u53d1\u9001',
        sendFail: '\u53d1\u9001\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5',
        changing: '\u66f4\u6362\u4e2d...',
        changeOk: '\u66f4\u6362\u6210\u529f',
        changeFail: '\u66f4\u6362\u5931\u8d25'
      }
    }
  },
  onLoad() {
    this.codeBtnText = this.ui.getCode
    this.refreshValidity()
  },
  onUnload() {
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
      this.countdownTimer = null
    }
  },
  methods: {
    refreshValidity() {
      const p = (this.phone || '').trim()
      const c = (this.code || '').trim()
      this.phoneValid = PHONE_REG.test(p)
      this.submitValid = this.phoneValid && c.length >= 4
    },
    onPhoneInput(e) {
      this.phone = (e.detail.value || '').replace(/\D/g, '').slice(0, 11)
      this.refreshValidity()
    },
    onCodeInput(e) {
      this.code = (e.detail.value || '').replace(/\D/g, '').slice(0, 6)
      this.refreshValidity()
    },
    updateCodeBtnText() {
      if (this.countdown > 0) {
        this.codeBtnText = String(this.countdown) + this.ui.secResend
      } else {
        this.codeBtnText = this.ui.getCode
      }
    },
    sendCode() {
      if (!this.phoneValid) {
        uni.showToast({ title: this.ui.toastPhone, icon: 'none' })
        return
      }
      if (this.countdown > 0) return
      const p = this.phone.trim()
      util.showLoading(this.ui.sending)
      api.sendSmsCode(p)
        .then(res => {
          util.hideLoading()
          if (res.code === 200 || res.code === 0) {
            uni.showToast({ title: this.ui.sentOk, icon: 'success' })
            this.countdown = 60
            this.updateCodeBtnText()
            if (this.countdownTimer) clearInterval(this.countdownTimer)
            this.countdownTimer = setInterval(() => {
              this.countdown--
              this.updateCodeBtnText()
              if (this.countdown <= 0) {
                clearInterval(this.countdownTimer)
                this.countdownTimer = null
              }
            }, 1000)
          } else {
            const msg = (res.msg || res.message || '') + ''
            uni.showToast({
              title:
                msg === 'failed' || msg === 'fail' || !msg.trim()
                  ? this.ui.sendFail
                  : msg,
              icon: 'none'
            })
          }
        })
        .catch(err => {
          util.hideLoading()
          const raw = (err && (err.msg || err.message)) || ''
          const msg =
            typeof raw === 'string' && (raw === 'failed' || raw === 'fail' || !raw.trim())
              ? this.ui.sendFail
              : raw || this.ui.sendFail
          uni.showToast({ title: msg, icon: 'none' })
        })
    },
    confirm() {
      if (!this.submitValid) {
        uni.showToast({ title: this.ui.toastFill, icon: 'none' })
        return
      }
      const p = this.phone.trim()
      const c = this.code.trim()
      util.showLoading(this.ui.changing)
      api.bindPhone(p, c)
        .then(res => {
          util.hideLoading()
          if (res.code === 200 || res.code === 0) {
            const userInfo = uni.getStorageSync('userInfo') || {}
            userInfo.phone = p
            userInfo.phoneNumber = p
            uni.setStorageSync('userInfo', userInfo)
            uni.showToast({ title: this.ui.changeOk, icon: 'success' })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          } else {
            uni.showToast({ title: res.msg || this.ui.changeFail, icon: 'none' })
          }
        })
        .catch(err => {
          util.hideLoading()
          uni.showToast({ title: (err && (err.msg || err.message)) || this.ui.changeFail, icon: 'none' })
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.verify-phone-container {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
}
.verify-phone-content {
  flex: 1;
  padding: 32rpx 24rpx 24rpx;
}
.desc {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 28rpx;
}
.field-wrap {
  margin-bottom: 24rpx;
}
.field-label {
  display: block;
  font-size: 24rpx;
  color: #333;
  margin-bottom: 12rpx;
}
.field-input {
  width: 100%;
  height: 72rpx;
  font-size: 26rpx;
  color: #333;
  border-bottom: 1rpx solid #e5e5e5;
}
.code-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.code-input {
  flex: 1;
  margin-bottom: 0;
}
.btn-code {
  flex-shrink: 0;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 24rpx;
  font-size: 24rpx;
  color: #888;
  background: #e5e5e5;
  border-radius: 8rpx;
  text-align: center;
}
.btn-code.active {
  background-color: #ffd700;
  color: #333;
}
.btn-wrap {
  padding: 24rpx 24rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
}
.btn-confirm {
  width: 100%;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: 28rpx;
  color: #333;
  background-color: rgba(255, 215, 0, 0.5);
  border-radius: 8rpx;
}
.btn-confirm.active {
  background-color: #ffd700;
  color: #333;
}
</style>
