<template>
  <view class="address-modal" @click="closeModal">
    <view class="modal-content" @click.stop>
      <!-- 头部 -->
      <view class="modal-header">
        <text class="modal-title">{{ isEdit ? '编辑地址' : '添加地址' }}</text>
        <view class="close-btn" @click="closeModal">
          <text class="close-icon">×</text>
        </view>
      </view>

      <!-- 表单内容 -->
      <view class="form-content">
        <!-- 收货人信息 -->
        <view class="form-section">
          <view class="form-item">
            <text class="label">收货人</text>
            <input 
              class="input" 
              v-model="formData.name" 
              placeholder="请输入收货人姓名"
              maxlength="20"
            />
          </view>
          <view class="form-item">
            <text class="label">手机号</text>
            <input 
              class="input" 
              v-model="formData.phone" 
              placeholder="请输入手机号"
              type="number"
              maxlength="11"
            />
          </view>
        </view>

        <!-- 地址选择 -->
        <view class="form-section">
          <view class="form-item" @click="selectRegion">
            <text class="label">所在地区</text>
            <view class="region-display">
              <text class="region-text" v-if="formData.province">
                {{ formData.province }} {{ formData.city }} {{ formData.district }}
              </text>
              <text class="region-text placeholder" v-else>请选择省市区</text>
              <text class="arrow">></text>
            </view>
          </view>
          <view class="form-item">
            <text class="label">详细地址</text>
            <textarea 
              class="textarea" 
              v-model="formData.detail" 
              placeholder="街道、门牌号等详细信息"
              maxlength="100"
            />
          </view>
        </view>

        <!-- 设为默认 -->
        <view class="form-section">
          <view class="form-item checkbox-item">
            <view class="checkbox" @click="toggleDefault">
              <view :class="['checkbox-icon', { 'checked': formData.isDefault }]">
                <text class="check-icon" v-if="formData.isDefault">✓</text>
              </view>
              <text class="checkbox-text">设为默认地址</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 底部按钮 -->
      <view class="modal-footer">
        <view class="btn cancel-btn" @click="closeModal">
          <text class="btn-text">取消</text>
        </view>
        <view class="btn confirm-btn" @click="saveAddress">
          <text class="btn-text">保存</text>
        </view>
      </view>
    </view>

    <!-- 地区选择器 -->
    <region-selector 
      v-if="showRegionSelector"
      :selected-region="selectedRegion"
      @close="closeRegionSelector"
      @confirm="handleRegionConfirm"
    />
  </view>
</template>

<script>
export default {
  name: 'AddAddressModal',
  
  props: {
    address: {
      type: Object,
      default: null
    }
  },

  data() {
    return {
      formData: {
        name: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detail: '',
        isDefault: false
      },
      showRegionSelector: false,
      selectedRegion: {
        province: null,
        city: null,
        district: null
      }
    }
  },

  computed: {
    isEdit() {
      return this.address && this.address.id
    }
  },

  watch: {
    address: {
      handler(newAddress) {
        if (newAddress) {
          this.formData = {
            name: newAddress.name || '',
            phone: newAddress.phone || '',
            province: newAddress.province || '',
            city: newAddress.city || '',
            district: newAddress.district || '',
            detail: newAddress.detail || '',
            isDefault: newAddress.isDefault || false
          }
        } else {
          this.resetForm()
        }
      },
      immediate: true
    }
  },

  methods: {
    closeModal() {
      this.$emit('close')
    },

    resetForm() {
      this.formData = {
        name: '',
        phone: '',
        province: '',
        city: '',
        district: '',
        detail: '',
        isDefault: false
      }
    },

    selectRegion() {
      // 显示地区选择器
      this.showRegionSelector = true
    },

    // 处理地区选择确认
    handleRegionConfirm(region) {
      this.selectedRegion = region
      this.formData.province = region.province ? region.province.name : ''
      this.formData.city = region.city ? region.city.name : ''
      this.formData.district = region.district ? region.district.name : ''
      this.showRegionSelector = false
    },

    // 关闭地区选择器
    closeRegionSelector() {
      this.showRegionSelector = false
    },

    toggleDefault() {
      this.formData.isDefault = !this.formData.isDefault
    },

    validateForm() {
      if (!this.formData.name.trim()) {
        uni.showToast({
          title: '请输入收货人姓名',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.phone.trim()) {
        uni.showToast({
          title: '请输入手机号',
          icon: 'none'
        })
        return false
      }

      if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
        uni.showToast({
          title: '请输入正确的手机号',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.province) {
        uni.showToast({
          title: '请选择所在地区',
          icon: 'none'
        })
        return false
      }

      if (!this.formData.detail.trim()) {
        uni.showToast({
          title: '请输入详细地址',
          icon: 'none'
        })
        return false
      }

      return true
    },

    saveAddress() {
      if (!this.validateForm()) {
        return
      }

      const addressData = {
        ...this.formData,
        fullAddress: `${this.formData.province} ${this.formData.city} ${this.formData.district} ${this.formData.detail}`
      }

      this.$emit('save', addressData)
    }
  }
}
</script>

<style lang="scss" scoped>
.address-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: flex-end;
}

.modal-content {
  width: 100%;
  background-color: white;
  border-radius: 16rpx 16rpx 0 0;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  
  .modal-title {
    font-size: 28rpx;
    font-weight: bold;
    color: #333;
  }
  
  .close-btn {
    width: 36rpx;
    height: 36rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .close-icon {
      font-size: 28rpx;
      color: #999;
    }
  }
}

.form-content {
  padding: 22rpx 24rpx;
}

.form-section {
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 12rpx;
  }
}

.form-item {
  margin-bottom: 20rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .label {
    display: block;
    font-size: 26rpx;
    color: #333;
    margin-bottom: 12rpx;
  }
  
  .input {
    width: 100%;
    height: 68rpx;
    padding: 0 16rpx;
    border: 1rpx solid #ddd;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: #333;
    background-color: #f8f8f8;
    
    &:focus {
      border-color: #4FC3F7;
      background-color: white;
    }
  }
  
  .textarea {
    width: 100%;
    min-height: 100rpx;
    padding: 16rpx;
    border: 1rpx solid #ddd;
    border-radius: 8rpx;
    font-size: 26rpx;
    color: #333;
    background-color: #f8f8f8;
    resize: none;
    
    &:focus {
      border-color: #4FC3F7;
      background-color: white;
    }
  }
}

.region-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 68rpx;
  padding: 0 16rpx;
  border: 1rpx solid #ddd;
  border-radius: 8rpx;
  background-color: #f8f8f8;
  
  .region-text {
    font-size: 26rpx;
    color: #333;
    
    &.placeholder {
      color: #999;
    }
  }
  
  .arrow {
    font-size: 22rpx;
    color: #999;
  }
}

.checkbox-item {
  .checkbox {
    display: flex;
    align-items: center;
    gap: 10rpx;
    
    .checkbox-icon {
      width: 30rpx;
      height: 30rpx;
      border: 2rpx solid #ddd;
      border-radius: 6rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &.checked {
        border-color: #ffd700;
        background-color: #ffd700;
        
        .check-icon {
          font-size: 20rpx;
          color: #333;
        }
      }
    }
    
    .checkbox-text {
      font-size: 24rpx;
      color: #333;
    }
  }
}

.modal-footer {
  display: flex;
  gap: 14rpx;
  padding: 16rpx 20rpx 20rpx;
  border-top: 1rpx solid #f0f0f0;
  
  .btn {
    flex: 1;
    height: 56rpx;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .btn-text {
      font-size: 24rpx;
      font-weight: bold;
    }
  }
  
  .cancel-btn {
    background-color: #f8f8f8;
    border: 1rpx solid #ddd;
    
    .btn-text {
      color: #666;
    }
  }
  
  .confirm-btn {
    background-color: #ffd700;
    
    .btn-text {
      color: #333;
    }
  }
}
</style>