<template>
  <view class="address-list-container">
    <!-- 地址列表 -->
    <view class="address-list" v-if="addressList.length > 0">
      <view 
        class="address-item" 
        v-for="address in addressList" 
        :key="address.id"
      >
        <!-- 删除按钮 -->
        <view class="address-actions">
          <view class="delete-btn" @click="deleteAddress(address.id)">
            <text class="delete-icon">×</text>
          </view>
        </view>

        <!-- 地址信息 -->
        <view class="address-info">
          <view class="recipient-info">
            <text class="recipient-name">{{ address.name }}</text>
            <text class="recipient-phone">{{ address.phone }}</text>
          </view>
          <view class="address-detail">{{ address.fullAddress }}</view>
        </view>

        <!-- 操作按钮 -->
        <view class="address-footer">
          <view class="default-section">
            <view class="radio-btn" @click="setDefaultAddress(address.id)">
              <view 
                :class="['radio', { 'checked': address.isDefault }]"
              ></view>
              <text class="radio-text">{{ address.isDefault ? '已设默认' : '默认' }}</text>
            </view>
          </view>
          <view class="action-buttons">
            <view class="action-btn" @click="copyAddress(address)">
              <text class="btn-text">复制</text>
            </view>
            <view class="action-btn" @click="editAddress(address)">
              <text class="btn-text">修改</text>
            </view>
            <view 
              class="use-btn" 
              @click="useAddress(address)"
            >
              <text class="btn-text">{{ address.isDefault ? '✓' : '使用' }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <image class="empty-icon" src="/static/images/location-pin.svg" mode="aspectFit" />
      <view class="empty-text">暂无收货地址</view>
    </view>

    <!-- 添加地址按钮 -->
    <view class="add-address-btn" @click="addAddress">
      <view class="add-icon">+</view>
      <text class="add-text">添加收货地址</text>
    </view>

    <!-- 添加/编辑地址弹窗 -->
    <add-address-modal 
      v-if="showAddressModal" 
      :address="editingAddress"
      @close="showAddressModal = false"
      @save="handleSaveAddress"
    />
  </view>
</template>

<script>
import addressApi from '@/common/js/api/address.js'
import { api } from '@/common/js/api.js'

export default {
  name: 'AddressList',
  
  data() {
    return {
      addressList: [],
      showAddressModal: false,
      editingAddress: null,
      lastAddressLoadTime: 0
    }
  },

  onLoad(options) {
    // 检查token
    const token = uni.getStorageSync('token')
    const userInfo = uni.getStorageSync('userInfo')
    this.loadAddressList()
  },

  onShow() {
    // 每次页面显示时静默刷新：节流 15 秒，不显示全局「加载中」，避免从其他页返回时反复弹 loading
    const now = Date.now()
    const throttleMs = 15 * 1000
    if (now - (this.lastAddressLoadTime || 0) < throttleMs && (this.lastAddressLoadTime || 0) > 0) {
      return
    }
    this.loadAddressList(true)
  },

  methods: {
    getSelectedAddressStorageKey() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const raw = userInfo.id || userInfo.uid || uni.getStorageSync('userId')
      if (!raw || raw === '' || raw === 'undefined') return null
      const n = Number(raw)
      if (!Number.isFinite(n) || n <= 0) return null
      return `selectedAddress_${n}`
    },
    async loadAddressList(silent = false) {
      try {
        // 检查是否登录
        const token = uni.getStorageSync('token')
        if (!token) {
          this.addressList = []
          // 如果是从兑换页面跳转过来的，提示用户先登录
          const pages = getCurrentPages()
          const currentPage = pages[pages.length - 1]
          if (currentPage && currentPage.options && currentPage.options.from === 'exchange') {
            uni.showModal({
              title: '提示',
              content: '请先登录后再选择收货地址',
              showCancel: false,
              success: () => {
                uni.navigateBack()
              }
            })
          }
          return
        }
        
        // 调用API获取地址列表（不传递userId，后端会从token中获取）
        const res = await addressApi.getAddressList(!silent)

        const ok = res && (res.code === 200 || res.code === 0)
        if (ok) {
          const raw = res.data
          const list = Array.isArray(raw)
            ? raw
            : (raw && Array.isArray(raw.records) ? raw.records : [])
          this.addressList = list || []
          this.lastAddressLoadTime = Date.now()
        } else {
          this.addressList = []
          // 如果是token无效，提示用户重新登录
          if (res && res.code === 500 && res.msg && res.msg.includes('未登录')) {
            uni.showModal({
              title: '提示',
              content: '登录已过期，请重新登录',
              showCancel: false,
              success: () => {
                uni.reLaunch({
                  url: '/pages-auth/login'
                })
              }
            })
          }
        }
      } catch (error) {
        this.addressList = []
        // 如果是未登录错误，不显示toast，避免重复提示
        if (error.message && error.message.includes('未登录')) {
          return
        }
        uni.showToast({
          title: '加载地址列表失败',
          icon: 'none'
        })
      }
    },

    // 已移除自动创建示例地址的功能，避免覆盖用户数据
    // createSampleAddresses() {
    //   // 不再自动创建示例地址
    // },

    addAddress() {
      this.editingAddress = null
      this.showAddressModal = true
    },

    editAddress(address) {
      this.editingAddress = address
      this.showAddressModal = true
    },

    deleteAddress(addressId) {
      // 勿用 await uni.showModal：部分微信小程序基础库对 Promise 封装不完整，点「确定」后 confirm 未触发，导致永远删不了
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这个收货地址吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            uni.showLoading({ title: '删除中...', mask: true })
            const deleteRes = await addressApi.deleteAddress(addressId)
            uni.hideLoading()
            const ok = deleteRes && (deleteRes.code === 200 || deleteRes.code === 0)
            if (ok) {
              const idStr = String(addressId)
              this.addressList = this.addressList.filter(item => String(item.id) !== idStr)
              const key = this.getSelectedAddressStorageKey()
              if (key) {
                try {
                  const sel = uni.getStorageSync(key)
                  if (sel && String(sel.id) === idStr) {
                    uni.removeStorageSync(key)
                  }
                } catch (e) {}
              }
              uni.showToast({ title: '删除成功', icon: 'success' })
            } else {
              uni.showToast({
                title: (deleteRes && deleteRes.msg) || '删除失败',
                icon: 'none'
              })
            }
          } catch (error) {
            uni.hideLoading()
            console.error('删除地址失败:', error)
            uni.showToast({
              title: (error && error.message) ? String(error.message).slice(0, 24) : '删除失败，请重试',
              icon: 'none'
            })
          }
        }
      })
    },

    async setDefaultAddress(addressId) {
      try {
        uni.showLoading({
          title: '设置中...'
        })
        
        // 调用API设置默认地址
        const res = await addressApi.setDefaultAddress(addressId)
        uni.hideLoading()
        
        if (res && res.code === 200) {
          // 设置默认地址
          this.addressList.forEach(item => {
            item.isDefault = item.id === addressId
          })
          
          uni.showToast({
            title: '设置成功',
            icon: 'success'
          })
        } else {
          uni.showToast({
            title: res?.msg || '设置失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('设置默认地址失败:', error)
        uni.showToast({
          title: '设置失败，请重试',
          icon: 'none'
        })
      }
    },

    useAddress(address) {
      // 保存选择的地址到本地存储（按用户隔离，避免新账号显示旧账号地址）
      const key = this.getSelectedAddressStorageKey()
      if (key) uni.setStorageSync(key, address)
      
      // 返回选择的地址
      const pages = getCurrentPages()
      const prevPage = pages[pages.length - 2]
      
      if (prevPage && prevPage.$vm) {
        // 检查是否有onAddressSelected方法
        if (prevPage.$vm.onAddressSelected) {
          prevPage.$vm.onAddressSelected(address)
        } else if (prevPage.$vm.selectedAddress !== undefined) {
          // 兼容旧的方式
          prevPage.$vm.selectedAddress = address
        } else {
          // 通过事件总线传递地址
          uni.$emit('addressSelected', address)
        }
      }
      
      // 显示选择成功提示
      uni.showToast({
        title: '地址选择成功',
        icon: 'success',
        duration: 1500
      })
      
      // 延迟返回，让用户看到提示
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    },

    copyAddress(address) {
      // 复制地址到剪贴板
      uni.setClipboardData({
        data: address.fullAddress,
        success: () => {
          uni.showToast({
            title: '地址已复制',
            icon: 'success'
          })
        }
      })
    },

    async handleSaveAddress(addressData) {
      try {
        const isEdit = !!this.editingAddress
        
        uni.showLoading({
          title: isEdit ? '保存中...' : '添加中...'
        })
        
        let res
        if (isEdit) {
          // 编辑地址
          const updateData = {
            ...addressData,
            id: this.editingAddress.id
          }
          res = await addressApi.updateAddress(updateData)
        } else {
          // 添加新地址
          res = await addressApi.createAddress(addressData)
        }
        
        uni.hideLoading()
        
        if (res && res.code === 200) {
          this.showAddressModal = false
          const wasEdit = isEdit
          const editedId = this.editingAddress ? this.editingAddress.id : null
          this.editingAddress = null
          
          // 重新加载地址列表
          await this.loadAddressList()
          
          // 若本次修改的正是当前选中的地址，同步更新 storage，确认订单页返回时即可显示新内容，无需再点「使用」
          if (wasEdit && editedId) {
            const key = this.getSelectedAddressStorageKey()
            const selected = key ? uni.getStorageSync(key) : null
            if (selected && selected.id === editedId) {
              const updated = {
                id: editedId,
                name: addressData.name || '',
                phone: addressData.phone || '',
                province: addressData.province || '',
                city: addressData.city || '',
                district: addressData.district || '',
                detail: addressData.detail || '',
                isDefault: !!addressData.isDefault,
                fullAddress: addressData.fullAddress || `${(addressData.province || '')} ${(addressData.city || '')} ${(addressData.district || '')} ${(addressData.detail || '')}`.trim()
              }
              if (key) uni.setStorageSync(key, updated)
              uni.$emit('addressSelected', updated)
            }
          }
          
          uni.showToast({
            title: wasEdit ? '修改成功' : '添加成功',
            icon: 'success'
          })
        } else {
          console.error('[handleSaveAddress] 保存失败，响应:', res)
          uni.showToast({
            title: res?.msg || (isEdit ? '修改失败' : '添加失败'),
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('[handleSaveAddress] 保存地址失败:', error)
        uni.showToast({
          title: (this.editingAddress ? '修改失败，请重试' : '添加失败，请重试') + ': ' + (error.message || '未知错误'),
          icon: 'none',
          duration: 3000
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.address-list-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-bottom: 120rpx;
}

.address-list {
  padding: 20rpx 30rpx;
}

.address-item {
  background-color: white;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
  padding: 30rpx;
  position: relative;
  margin-top: 10rpx;
}

.address-item:first-child {
  margin-top: 0;
}

.address-actions {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  display: flex;
  gap: 16rpx;
}

.delete-btn {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-icon {
  font-size: 24rpx;
  color: #666;
}

.address-info {
  margin-bottom: 30rpx;
}

.recipient-info {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.recipient-name {
  font-size: 28rpx;
  color: #333;
  margin-right: 20rpx;
}

.recipient-phone {
  font-size: 28rpx;
  color: #333;
}

.address-detail {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.address-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.default-section {
  display: flex;
  align-items: center;
}

.radio-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.radio {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;

  &.checked {
    border-color: #ffd700;
    background-color: #ffd700;
    
    &::after {
      content: '';
      width: 16rpx;
      height: 16rpx;
      background-color: white;
      border-radius: 50%;
    }
  }
}

.radio-text {
  font-size: 24rpx;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 16rpx;
}

.action-btn, .use-btn {
  padding: 12rpx 24rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  background-color: white;
}

.use-btn {
  border-color: #ffd700;
  background-color: #ffd700;
}

.btn-text {
  font-size: 24rpx;
  color: #666;
}

.use-btn .btn-text {
  color: #333;
}

.default-badge {
  width: 60rpx;
  height: 60rpx;
  background-color: #ffd700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.badge-icon {
  font-size: 24rpx;
  color: #333;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400rpx;
  margin-top: 200rpx;
}

.empty-icon {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.add-address-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background-color: #ffd700;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.add-icon {
  font-size: 32rpx;
  color: #333;
}

.add-text {
  font-size: 32rpx;
  color: #333;
  font-weight: bold;
}
</style>
