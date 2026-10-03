<template>
  <view class="order-confirm">
    <!-- 收货地址（服务订单不需要） -->
    <view class="address-section" v-if="orderData.orderType !== 'service'">
      <view class="address-info" v-if="selectedAddress.name" @click="selectAddress">
        <view class="address-content">
          <view class="recipient">
            <text class="name">{{ selectedAddress.name }}</text>
            <text class="phone">{{ selectedAddress.phone }}</text>
          </view>
          <view class="address-detail">{{ selectedAddress.fullAddress }}</view>
        </view>
        <view class="change-btn" @click.stop="selectAddress">
          <text class="btn-text">更换</text>
        </view>
      </view>
      <view class="no-address" v-else @click="selectAddress">
        <text class="add-text">+ 添加收货地址</text>
      </view>
    </view>

    <!-- 商品/服务信息 -->
    <view class="product-section">
      <view class="section-header">
        <text class="section-title">{{ orderData.orderType === 'service' ? '服务信息' : '商品信息' }}</text>
      </view>
      <view class="product-item">
        <image v-if="orderData.goods.image" class="product-image" :src="getOrderImageUrl(orderData.goods.image)" mode="aspectFill"></image>
        <view class="product-info" :class="{ 'product-info-no-image': !orderData.goods.image }">
          <text class="product-name">{{ orderData.goods.name }}</text>
          <text class="product-spec" v-if="orderData.selectedSpec && orderData.selectedSpec.name">
            规格：{{ orderData.selectedSpec.name }}
          </text>
          <text class="product-spec" v-else-if="orderData.selectedSpec">
            规格：{{ JSON.stringify(orderData.selectedSpec) }}
          </text>
          <view class="price-quantity">
            <text class="price" v-if="orderData.orderType === 'points'">
              <image src="/static/images/my-cans.png" mode="aspectFit" class="can-icon-inline" />
              {{ orderData.points || (((orderData.goods && orderData.goods.points) || 0) * (orderData.quantity || 1)) }}g
            </text>
            <text class="price" v-else>¥{{ (orderData.selectedSpec && orderData.selectedSpec.price) || (orderData.goods && orderData.goods.price) || 0 }}</text>
            <text class="quantity">x{{ orderData.quantity || 1 }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="order-info" v-if="orderData.orderType !== 'points' && orderData.orderType !== 'service'">
      <view class="info-row">
        <text class="label">商品金额</text>
        <text class="value">¥{{ orderData.totalAmount || '0.00' }}</text>
      </view>
      <view class="info-row">
        <text class="label">运费</text>
        <text class="value">¥{{ orderData.shippingFee || '0' }}</text>
      </view>
      <view class="info-row coupon-row" @click="openCouponPicker">
        <text class="label">优惠券</text>
        <view class="coupon-value-wrap">
          <text v-if="selectedCoupon" class="coupon-selected">-¥{{ selectedCoupon.discountAmount }}</text>
          <text v-else-if="usableCoupons.length > 0" class="coupon-available">{{ usableCoupons.length }}张可用</text>
          <text v-else class="coupon-none">暂无可用</text>
          <text class="coupon-arrow">›</text>
        </view>
      </view>
      <view class="info-row total">
        <text class="label">实付金额</text>
        <text class="value">¥{{ orderData.finalAmount || '0.00' }}</text>
      </view>
    </view>
    <!-- 服务订单金额 -->
    <view class="order-info" v-if="orderData.orderType === 'service'">
      <view class="info-row total">
        <text class="label">实付金额</text>
        <text class="value">¥{{ orderData.finalAmount || '0.00' }}</text>
      </view>
    </view>
    <!-- 积分订单信息 -->
    <view class="order-info" v-if="orderData.orderType === 'points'">
      <view class="info-row total">
        <text class="label">需要积分</text>
        <text class="value">
          <image src="/static/images/my-cans.png" mode="aspectFit" class="can-icon-inline" />
          {{ orderData.points || (((orderData.goods && orderData.goods.points) || 0) * (orderData.quantity || 1)) }}g
        </text>
      </view>
    </view>

    <!-- 支付方式（积分订单不显示）-->
    <view class="payment-section" v-if="orderData.orderType !== 'points'">
      <view class="section-header">
        <text class="section-title">支付方式</text>
      </view>
      <view class="payment-methods">
        <view 
          class="payment-item" 
          v-for="method in paymentMethods" 
          :key="method.id"
          @click="selectPaymentMethod(method)"
        >
          <view class="method-info">
            <image v-if="method.icon && (method.icon.indexOf('/') >= 0 || method.icon.indexOf('.png') >= 0)" class="method-icon-img" :src="method.icon" mode="aspectFit" />
            <text v-else class="method-icon">{{ method.icon }}</text>
            <text class="method-name">{{ method.name }}</text>
          </view>
          <view class="method-radio" :class="{ active: selectedPaymentMethod.id === method.id }">
            <text class="radio-icon">✓</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部支付按钮 -->
    <view class="bottom-bar">
      <view class="total-info" v-if="orderData.orderType === 'points'">
        <text class="total-label">需要积分</text>
        <view class="total-amount-points">
          <image src="/static/images/my-cans.png" mode="aspectFit" class="can-icon-inline" />
          <text>{{ orderData.points || (((orderData.goods && orderData.goods.points) || 0) * (orderData.quantity || 1)) }}g</text>
        </view>
      </view>
      <view class="total-info" v-else>
        <text class="total-label">实付金额</text>
        <text class="total-amount">¥{{ orderData.finalAmount || '0.00' }}</text>
      </view>
      <view class="pay-btn" @click="submitOrder">
        <text class="btn-text" v-if="orderData.orderType === 'points'">立即兑换</text>
        <text class="btn-text" v-else>立即支付</text>
      </view>
    </view>

    <!-- 支付弹窗 -->
    <view class="payment-modal" v-if="showPaymentModal" @click.self="closePaymentModal">
      <view class="payment-modal-content" @click.stop>
        <view class="payment-header">
          <text class="payment-title">订单支付</text>
          <text class="order-number">订单号: {{ currentOrderId }}</text>
        </view>
        
        <view class="payment-amount-section">
          <text class="payment-amount-label">支付金额</text>
          <text class="payment-amount-value">¥{{ orderData.finalAmount || '0.00' }}</text>
        </view>

        <view class="payment-method-section">
          <text class="section-title">选择支付方式</text>
          <view class="payment-method-list">
            <view 
              class="payment-method-item" 
              v-for="method in paymentMethodsWithDesc" 
              :key="method.id"
              @click="selectPaymentMethodInModal(method)"
            >
              <view class="method-left">
                <image v-if="method.icon && (method.icon.indexOf('/') >= 0 || method.icon.indexOf('.png') >= 0)" class="method-icon-large-img" :src="method.icon" mode="aspectFit" />
                <text v-else class="method-icon-large">{{ method.icon }}</text>
                <view class="method-text">
                  <text class="method-name">{{ method.name }}</text>
                  <text class="method-desc">{{ method.desc }}</text>
                </view>
              </view>
              <view class="method-radio-large" :class="{ active: selectedPaymentMethod.id === method.id }">
                <text class="radio-icon-large">✓</text>
              </view>
            </view>
          </view>
        </view>

        <view class="payment-confirm-btn" :class="{ disabled: !selectedPaymentMethod.id }" @click="confirmPayment">
          <text class="confirm-btn-text">确认支付 ¥{{ orderData.finalAmount || '0.00' }}</text>
        </view>
      </view>
    </view>

    <!-- 优惠券选择弹层 -->
    <view class="coupon-modal" v-if="showCouponModal" @click.self="closeCouponPicker">
      <view class="coupon-modal-content" @click.stop>
        <view class="coupon-modal-header">
          <text class="coupon-modal-title">选择优惠券</text>
          <text class="coupon-modal-close" @click="closeCouponPicker">✕</text>
        </view>
        <scroll-view scroll-y class="coupon-modal-body">
          <view v-if="couponLoading" class="coupon-modal-loading">加载中...</view>
          <view v-else-if="usableCoupons.length === 0" class="coupon-modal-empty">暂无可用优惠券</view>
          <view
            v-for="c in usableCoupons"
            :key="c.id"
            class="coupon-option"
            :class="{ 'coupon-option-disabled': !c.usable }"
            @click="chooseCoupon(c)">
            <view class="coupon-option-left" :class="{ 'coupon-option-left-disabled': !c.usable }">
              <text v-if="c.type === 'discount'" class="coupon-option-value">{{ (c.discountRate * 10).toFixed(1).replace(/\.0$/, '') }}折</text>
              <text v-else class="coupon-option-value">¥{{ parseFloat(c.discountAmount) }}</text>
              <text class="coupon-option-threshold">{{ couponThresholdText(c) }}</text>
            </view>
            <view class="coupon-option-info">
              <text class="coupon-option-name">{{ c.name }}</text>
              <text class="coupon-option-desc">{{ couponScopeText(c) }}</text>
              <text v-if="!c.usable" class="coupon-option-reason">{{ c.reason }}</text>
              <text v-else class="coupon-option-date">有效期至 {{ formatDateText(c.validEndTime) }}</text>
            </view>
            <view class="coupon-option-radio" :class="{ active: selectedCoupon && selectedCoupon.id === c.id }">
              <text v-if="selectedCoupon && selectedCoupon.id === c.id" class="radio-icon">✓</text>
            </view>
          </view>
        </scroll-view>
        <view class="coupon-modal-footer">
          <view class="coupon-clear-btn" @click="chooseCoupon(null)">
            <text class="coupon-clear-text">不使用优惠券</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import cartApi from '@/common/js/api/cart.js'
import addressApi from '@/common/js/api/address.js'
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  data() {
    return {
      orderData: {
        goods: {},
        selectedSpec: {},
        quantity: 1,
        totalAmount: 0,
        shippingFee: 0,
        finalAmount: 0
      },
      selectedAddress: {},
      selectedPaymentMethod: {},
      paymentMethods: [
        {
          id: 1,
          name: '\u5fae\u4fe1\u652f\u4ed8',
          icon: '/static/images/\u652f\u4ed8-_\u5fae\u4fe1\u652f\u4ed8-copy.png',
          desc: '\u63a8\u8350\u4f7f\u7528\u5fae\u4fe1\u652f\u4ed8'
        }
      ],
      showPaymentModal: false,
      // 优惠券选券状态
      showCouponModal: false,
      couponLoading: false,
      usableCoupons: [],
      selectedCoupon: null,
      currentOrderId: '',
      currentOrderBackendId: null, // 后端返回的订单ID
      existingOrderId: null, // 从订单列表/详情跳转来的待支付订单号
      currentOrderInfo: null,
      lastAddressLoadTime: 0
    }
  },

  computed: {
    paymentMethodsWithDesc() {
      return this.paymentMethods
    }
  },

  async onLoad(options) {
    // 有orderId 时先清空本地订单缓存，避免显示错误订单
    if (options.orderId) {
      uni.removeStorageSync('orderData')
    }
    // 清理旧版全局 selectedAddress，避免新账号显示其他用户的地址
    try { uni.removeStorageSync('selectedAddress') } catch (e) {
      console.error('清理旧版全局 selectedAddress 失败:', e)
    }
    await this.loadOrderData(options)
    this.loadSelectedAddress(true)
    if (this.paymentMethods.length > 0) {
      this.selectedPaymentMethod = this.paymentMethods[0]
    }
  },

  onShow() {
    // 按用户隔离缓存，避免新账号显示旧账号/测试数据
    const key = this.getSelectedAddressKey()
    const savedAddress = key ? uni.getStorageSync(key) : null
    if (savedAddress && savedAddress.name && savedAddress.id) {
      this.selectedAddress = savedAddress
      return
    }
    // 静默刷新：节流15 秒，不显示全局「加载中」，避免从地址列表返回时反复弹 loading
    const now = Date.now()
    const throttleMs = 15 * 1000
    if (now - (this.lastAddressLoadTime || 0) < throttleMs && (this.lastAddressLoadTime || 0) > 0) {
      return
    }
    this.loadSelectedAddress(true)
  },

  methods: {
    async loadOrderData(options) {
      try {
        // 如果传递了 orderId，从订单列表/详情跳转来的待支付订单
        if (options.orderId) {
          await this.loadExistingOrder(options.orderId)
          return
        }
        // 如果传递了 cartIds，从购物车加载数据
        if (options.cartIds) {
          try {
            const cartIds = JSON.parse(options.cartIds)
            await this.loadCartData(cartIds)
            return
          } catch (e) {
            // 静默处理错误
          }
        }
        
        // 否则从本地存储加载（兼容从商品详情页跳转的情况）
        const orderData = uni.getStorageSync('orderData')
        
        if (orderData && orderData.goods) {
          // 深拷贝数据，避免引用问题
          this.orderData = JSON.parse(JSON.stringify(orderData))
          
          // 确保 selectedSpec 有默认值
          if (!this.orderData.selectedSpec) {
            this.orderData.selectedSpec = null
          }
          
          this.calculateAmount()
        } else {
          // 如果没有订单数据，返回上一页
          uni.showToast({
            title: '订单数据异常',
            icon: 'none'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }
      } catch (error) {
        console.error('加载订单数据失败:', error)
        uni.showToast({
          title: '\u8ba2\u5355\u6570\u636e\u52a0\u8f7d\u5931\u8d25',
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    },

    async loadExistingOrder(orderId) {
      try {
        const res = await api.getOrderDetail(orderId)
        if (!res || !(res.code === 200 || res.code === 0) || !res.data) {
          uni.showToast({ title: '订单加载失败', icon: 'none' })
          setTimeout(() => uni.navigateBack(), 1500)
          return
        }
        const o = res.data
        if ((o.status === 'paid' || o.statusNumber >= 1) && (o.payType === 2 || o.paymentStatus === 1)) {
          uni.showToast({ title: '订单已支付', icon: 'none' })
          setTimeout(() => uni.navigateBack(), 1500)
          return
        }
        const items = o.orderItemList || o.products || []
        const first = items[0] || {}
        this.orderData = {
          goods: {
            id: first.productId,
            name: first.productName || first.product_name,
            image: first.productPic || first.productImage || first.product_pic,
            price: first.productPrice || first.price || first.product_price,
            quantity: first.productQuantity || first.quantity || 1
          },
          selectedSpec: null,
          totalAmount: String(o.totalAmount ?? o.payAmount ?? 0),
          shippingFee: String(o.freightAmount ?? o.freight_amount ?? 0),
          finalAmount: String(o.totalAmount ?? o.payAmount ?? 0),
          orderType: o.orderType || 'product'
        }
        this.calculateAmount()
        this.existingOrderId = o.orderNo || orderId
        this.currentOrderId = o.orderNo || orderId
        this.currentOrderBackendId = o.id != null ? o.id : null
        this.currentOrderInfo = { orderId: this.currentOrderId, orderType: 'product' }
        if (o.receiverName || o.receiver_name) {
          this.selectedAddress = {
            id: o.addressId,
            name: o.receiverName || o.receiver_name,
            phone: o.receiverPhone || o.receiver_phone,
            province: o.receiverProvince || o.receiver_province,
            city: o.receiverCity || o.receiver_city,
            district: o.receiverRegion || o.receiver_region,
            detail: o.receiverDetailAddress || o.receiver_detail_address,
            fullAddress: [o.receiverProvince, o.receiverCity, o.receiverRegion, o.receiverDetailAddress].filter(Boolean).join('') || [o.receiver_province, o.receiver_city, o.receiver_region, o.receiver_detail_address].filter(Boolean).join('')
          }
        }
      } catch (e) {
        console.error('加载订单失败:', e)
        uni.showToast({ title: '订单加载失败', icon: 'none' })
        setTimeout(() => uni.navigateBack(), 1500)
      }
    },
    
    async loadCartData(cartIds) {
      try {
        // 获取用户ID
        const userInfo = uni.getStorageSync('userInfo')
        const userId = userInfo?.id || userInfo?.uid
        
        if (!userId) {
          uni.showToast({
            title: '请先登录',
            icon: 'none'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
          return
        }
        
        // 获取购物车列表
        const res = await cartApi.fetchCartList(userId)
        if (res.code === 200 && res.data) {
          // 筛选出选中的购物车项
          const selectedItems = res.data.filter(item => cartIds.includes(item.id))
          
          if (selectedItems.length === 0) {
            uni.showToast({
              title: '未找到选中的商品',
              icon: 'none'
            })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
            return
          }
          
          // 计算总价
          let totalAmount = 0
          selectedItems.forEach(item => {
            totalAmount += (item.price || 0) * (item.quantity || 1)
          })
          
          // 转换为订单数据格式
          // 如果是多个商品，使用第一个商品作为主要显示（兼容现有UI）
          const firstItem = selectedItems[0]
          this.orderData = {
            goods: {
              id: firstItem.productId,
              name: firstItem.productName,
              image: firstItem.productImage || firstItem.productPic,
              price: firstItem.price,
              quantity: firstItem.quantity
            },
            selectedSpec: null,
            storeName: firstItem.storeName,
            allItems: selectedItems, // 保存所有商品，用于计算总价
            totalAmount: totalAmount,
            quantity: selectedItems.reduce((sum, item) => sum + (item.quantity || 1), 0)
          }
          
          this.calculateAmount()
        } else {
          uni.showToast({
            title: '加载购物车数据失败',
            icon: 'none'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        }
      } catch (error) {
        console.error('加载购物车数据失败:', error)
        uni.showToast({
          title: '加载数据失败',
          icon: 'none'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
      }
    },

    getSelectedAddressKey() {
      const userInfo = uni.getStorageSync('userInfo') || {}
      const raw = userInfo.id || userInfo.uid || uni.getStorageSync('userId')
      if (!raw || raw === '' || raw === 'undefined') return null
      const n = Number(raw)
      if (!Number.isFinite(n) || n <= 0) return null
      return `selectedAddress_${n}`
    },
    async loadSelectedAddress(silent = false) {
      try {
        const showLoading = !silent
        const key = this.getSelectedAddressKey()
        const savedAddress = key ? uni.getStorageSync(key) : null
        if (savedAddress && savedAddress.name) {
          this.selectedAddress = savedAddress
        }
        
        if (!this.selectedAddress || !this.selectedAddress.name) {
          const res = await addressApi.getDefaultAddress(null, showLoading)
          
          if (res && res.code === 200 && res.data && res.data.name) {
            this.selectedAddress = res.data
            const key = this.getSelectedAddressKey()
            if (key) uni.setStorageSync(key, res.data)
            this.lastAddressLoadTime = Date.now()
            return
          }
          
          const listRes = await addressApi.getAddressList(showLoading)
          
          if (listRes && listRes.code === 200 && listRes.data && listRes.data.length > 0) {
            this.selectedAddress = listRes.data[0]
            const key = this.getSelectedAddressKey()
            if (key) uni.setStorageSync(key, listRes.data[0])
            this.lastAddressLoadTime = Date.now()
            return
          }
        }
        
        // 如果还是没有地址数据
        if (!this.selectedAddress || !this.selectedAddress.name) {
          this.selectedAddress = {}
        }
      } catch (error) {
        // 出错时尝试从本地存储加载（按用户隔离）
        const key = this.getSelectedAddressKey()
        const savedAddress = key ? uni.getStorageSync(key) : null
        if (savedAddress && savedAddress.name) {
          this.selectedAddress = savedAddress
        } else {
          this.selectedAddress = {}
        }
      }
    },
    
    // 添加地址选择回调方法
    onAddressSelected(address) {
      this.selectedAddress = address
      const key = this.getSelectedAddressKey()
      if (key) uni.setStorageSync(key, address)
    },

    selectAddress() {
      uni.navigateTo({
        url: '/address/list',
        success: () => {
        }
      })
    },

    selectPaymentMethod(method) {
      this.selectedPaymentMethod = method
    },

    selectPaymentMethodInModal(method) {
      this.selectedPaymentMethod = method
    },

    closePaymentModal() {
      this.showPaymentModal = false
    },

    async confirmPayment() {
      if (!this.selectedPaymentMethod.id) {
        uni.showToast({
          title: '请选择支付方式',
          icon: 'none'
        })
        return
      }

      // 服务订单：创建预约（pending）→ 统一下单 → 真实支付 → 轮询确认 → 跳预约详情
      if (this.currentOrderInfo && this.currentOrderInfo.orderType === 'service') {
        this.showPaymentModal = false
        this.payServiceAppointment()
        return
      }

      const orderNo = this.currentOrderId || (this.currentOrderInfo && this.currentOrderInfo.orderId) || ''
      if (!orderNo) {
        uni.showToast({ title: '订单号缺失，无法支付', icon: 'none' })
        return
      }

      uni.showLoading({ title: '发起支付...', mask: true })
      try {
        // 1. 后端统一下单（归属校验、金额以 DB 为准），返回拉起支付所需参数
        const res = await api.createPayParams(orderNo)
        if (!res || !(res.code === 200 || res.code === 0) || !res.data) {
          uni.hideLoading()
          uni.showToast({
            title: (res && res.msg) ? String(res.msg).slice(0, 20) : '发起支付失败',
            icon: 'none',
            duration: 2500
          })
          return
        }
        const payParams = res.data

        // 2. 拉起支付：mock 通道模拟收银台支付耗时（与真实支付节奏一致），真实通道拉起微信收银台
        uni.hideLoading()
        if (payParams.mock) {
          uni.showLoading({ title: '支付处理中...', mask: true })
          await this.simulatePayment()
          const confirmRes = await api.mockConfirmPay(orderNo)
          uni.hideLoading()
          if (!(confirmRes && (confirmRes.code === 200 || confirmRes.code === 0))) {
            throw new Error((confirmRes && confirmRes.msg) || '支付未完成')
          }
        } else {
          await new Promise((resolve, reject) => {
            uni.requestPayment({
              provider: 'wxpay',
              timeStamp: payParams.timeStamp,
              nonceStr: payParams.nonceStr,
              package: payParams.package,
              signType: payParams.signType,
              paySign: payParams.paySign,
              success: resolve,
              fail: reject
            })
          })
        }

        // 3. 支付完成后轮询查单确认（回调到账存在秒级延迟，查单接口会补偿同步状态）
        uni.showLoading({ title: '确认支付结果...', mask: true })
        const paid = await this.pollPayResult(orderNo)
        uni.hideLoading()
        this.showPaymentModal = false

        if (paid) {
          uni.showToast({ title: '支付成功', icon: 'success' })
          setTimeout(() => {
            uni.redirectTo({ url: '/order/list' })
          }, 1500)
        } else {
          uni.showToast({
            title: '支付结果确认中，请稍后在订单列表查看',
            icon: 'none',
            duration: 2500
          })
          setTimeout(() => {
            uni.redirectTo({ url: '/order/list' })
          }, 2200)
        }
      } catch (error) {
        uni.hideLoading()
        const cancelled = error && error.errMsg && String(error.errMsg).indexOf('cancel') >= 0
        uni.showToast({
          title: cancelled ? '已取消支付' : '支付未完成，请重试',
          icon: 'none'
        })
      }
    },

    /** 模拟收银台支付耗时（mock 通道，与真实支付交互节奏一致） */
    simulatePayment() {
      return new Promise(resolve => {
        setTimeout(resolve, 1500)
      })
    },

    /** 查单轮询：回调延迟/未达时后端会以微信侧结果补偿更新订单状态 */
    async pollPayResult(orderNo, attempts = 3, intervalMs = 1000) {
      for (let i = 0; i < attempts; i++) {
        try {
          const res = await api.queryPayResult(orderNo)
          if (res && (res.code === 200 || res.code === 0) && res.data && res.data.paid) {
            return true
          }
        } catch (e) {
          // 单次查询失败继续重试
        }
        if (i < attempts - 1) {
          await new Promise(resolve => setTimeout(resolve, intervalMs))
        }
      }
      return false
    },

    /**
     * 服务预约支付链路：
     * 1. 非付费服务类型走免费流程，仅创建预约后跳详情；
     * 2. 付费流程（grooming/hospital/door-cleaning）先创建预约（pending），
     *    再以统一支付单发起微信支付（金额以预约表 DB 值为准，归属按 token 校验）。
     */
    async payServiceAppointment() {
      const formData = uni.getStorageSync('serviceBookingFormData')
      if (!formData || !formData.appointmentData || !formData.successParams) {
        uni.showToast({
          title: '预约数据丢失，请重新预约',
          icon: 'none',
          duration: 2500
        })
        setTimeout(() => {
          uni.reLaunch({ url: '/user/my-appointments' })
        }, 600)
        return
      }

      const st = String(formData.serviceType || '').toLowerCase()
      const normalized = st === 'medical' ? 'hospital' : (st === 'litter' ? 'door-cleaning' : st)
      const payable = ['grooming', 'hospital', 'door-cleaning'].indexOf(normalized) >= 0
      if (!payable) {
        // 免费流程：不涉及收款，保持创建预约 + 跳转
        uni.removeStorageSync('serviceBookingFormData')
        this.createServiceAppointmentAndGoSuccess(formData)
        return
      }

      uni.showLoading({ title: '提交预约...', mask: true })
      let bizId = null
      try {
        // 1. 创建预约（支付前落库为 pending，状态机由后台确认流转）
        const created = await this.createServiceAppointment(formData)
        if (!created) {
          uni.hideLoading()
          uni.reLaunch({ url: '/user/my-appointments' })
          return
        }
        bizId = created.bizId
        uni.removeStorageSync('serviceBookingFormData')

        // 2. 统一下单（后端校验预约归属与金额，返回拉起支付参数）
        uni.showLoading({ title: '发起支付...', mask: true })
        const res = await api.createPayParams({
          bizType: 'APPOINTMENT',
          serviceType: normalized,
          bizId
        })
        if (!res || !(res.code === 200 || res.code === 0) || !res.data) {
          uni.hideLoading()
          uni.showToast({
            title: (res && res.msg) ? String(res.msg).slice(0, 20) : '发起支付失败',
            icon: 'none',
            duration: 2500
          })
          // 预约已创建，跳详情页查看（可稍后继续支付）
          setTimeout(() => {
            this.goAppointmentDetail(normalized, bizId)
          }, 1600)
          return
        }
        const payParams = res.data

        // 3. 拉起支付：mock 通道模拟收银台支付耗时（与真实支付节奏一致），真实通道拉起微信收银台
        uni.hideLoading()
        if (payParams.mock) {
          uni.showLoading({ title: '支付处理中...', mask: true })
          await this.simulatePayment()
          const confirmRes = await api.mockConfirmPay({
            bizType: 'APPOINTMENT',
            serviceType: normalized,
            bizId
          })
          uni.hideLoading()
          if (!(confirmRes && (confirmRes.code === 200 || confirmRes.code === 0))) {
            throw new Error((confirmRes && confirmRes.msg) || '支付未完成')
          }
        } else {
          await new Promise((resolve, reject) => {
            uni.requestPayment({
              provider: 'wxpay',
              timeStamp: payParams.timeStamp,
              nonceStr: payParams.nonceStr,
              package: payParams.package,
              signType: payParams.signType,
              paySign: payParams.paySign,
              success: resolve,
              fail: reject
            })
          })
        }

        // 4. 轮询查单确认（回调到账存在秒级延迟，查单接口会补偿同步支付状态）
        uni.showLoading({ title: '确认支付结果...', mask: true })
        const paid = await this.pollPayResult({
          bizType: 'APPOINTMENT',
          serviceType: normalized,
          bizId
        })
        uni.hideLoading()
        uni.showToast({
          title: paid ? '支付成功' : '支付结果确认中',
          icon: paid ? 'success' : 'none',
          duration: 1200
        })
        setTimeout(() => {
          this.goAppointmentDetail(normalized, bizId)
        }, 1200)
      } catch (error) {
        uni.hideLoading()
        const cancelled = error && error.errMsg && String(error.errMsg).indexOf('cancel') >= 0
        uni.showToast({
          title: cancelled ? '已取消支付，预约已保留' : '支付未完成，预约已保留',
          icon: 'none',
          duration: 2000
        })
        if (bizId != null) {
          setTimeout(() => {
            this.goAppointmentDetail(normalized, bizId)
          }, 1600)
        }
      }
    },

    /**
     * 创建服务预约（纯创建，不做支付）
     * @returns {{ serviceType: string, bizId: string } | null} 创建成功返回 normalized serviceType 与预约 ID
     */
    async createServiceAppointment(formData) {
      try {
        const payload = Object.assign({}, formData.appointmentData)
        if (!payload) {
          uni.showToast({ title: '预约数据不完整', icon: 'none', duration: 2500 })
          setTimeout(() => {
            uni.reLaunch({ url: '/user/my-appointments' })
          }, 800)
          return null
        }
        // 归属以当前登录用户为准（后端支付时按 token 归属校验，两者必须一致）
        const uid = this.getCurrentUserId()
        if (uid != null) {
          payload.userId = uid
        }
        const st = String(formData.serviceType || '').toLowerCase()
        let res = null
        if (st === 'grooming') {
          res = await api.createGroomingAppointment(payload)
        } else if (st === 'hospital' || st === 'medical') {
          res = await api.createHospitalAppointment(payload)
        } else {
          res = await api.createAppointment(payload)
        }
        const data = res && (res.data != null ? res.data : res.result)
        const newId = data != null && (data.id != null && data.id !== '' ? data.id : (data.appointmentId != null && data.appointmentId !== '' ? data.appointmentId : null))
        if (res && (res.code === 200 || res.code === 0) && newId != null && newId !== '') {
          let serviceType = st === 'medical' ? 'hospital' : (st || 'door-cleaning')
          if (serviceType === 'litter') serviceType = 'door-cleaning'
          return { serviceType, bizId: String(newId) }
        }
        const errMsg = (res && res.msg) ? String(res.msg).replace(/\n/g, ' ').trim() : '预约创建失败'
        uni.showToast({
          title: errMsg.length > 20 ? errMsg.slice(0, 20) + '...' : errMsg,
          icon: 'none',
          duration: 3000
        })
        return null
      } catch (error) {
        console.error('创建预约失败:', error)
        const msg = (error && (error.message || error.errMsg)) ? String(error.message || error.errMsg).slice(0, 25) : '网络异常，请稍后重试'
        uni.showToast({ title: msg, icon: 'none', duration: 2500 })
        return null
      }
    },

    /** 免费流程：创建预约并跳转详情（失败提示在 createServiceAppointment 内处理） */
    async createServiceAppointmentAndGoSuccess(formData) {
      const created = await this.createServiceAppointment(formData)
      if (created) {
        uni.showToast({ title: '预约成功，正在跳转详情', icon: 'success', duration: 1200 })
        setTimeout(() => {
          this.goAppointmentDetail(created.serviceType, created.bizId)
        }, 800)
      }
    },

    goAppointmentDetail(serviceType, bizId) {
      uni.reLaunch({
        url: `/appointment/detail?id=${encodeURIComponent(String(bizId))}&serviceType=${encodeURIComponent(serviceType)}`
      })
    },

    getCurrentUserId() {
      // 从本地存储获取用户ID，或者从登录状态获取
      // 尝试多种可能的存储键
      const staffInfo = uni.getStorageSync('staffInfo') || {}
      const userId = uni.getStorageSync('userId') || 
                    uni.getStorageSync('uid') || 
                    uni.getStorageSync('user_id') ||
                    staffInfo.adminId
      
      if (userId) {
        return userId
      }

      // 兜底：从 token 里解析用户ID（兼容 token_<userId>_<timestamp>）
      const token = uni.getStorageSync('token')
      if (token && typeof token === 'string') {
        if (token.startsWith('token_')) {
          const parts = token.split('_')
          if (parts.length >= 2 && /^\d+$/.test(parts[1])) {
            return Number(parts[1])
          }
        }
        // 兼容 JWT，尝试解析 sub 为数字用户ID
        const segs = token.split('.')
        if (segs.length >= 2) {
          try {
            const payload = JSON.parse(decodeURIComponent(escape(atob(segs[1].replace(/-/g, '+').replace(/_/g, '/')))))
            if (payload && payload.sub != null && /^\d+$/.test(String(payload.sub))) {
              return Number(payload.sub)
            }
          } catch (e) {}
        }
      }
      
      // 尝试从userInfo对象获取
      const userInfo = uni.getStorageSync('userInfo')
      if (userInfo) {
        if (userInfo.id) return userInfo.id
        if (userInfo.uid) return userInfo.uid
        if (userInfo.userId) return userInfo.userId
      }
      
      // 不再使用默认ID，避免把订单写到错误用户下
      return null
    },

    calculateAmount() {
      // 如果是积分订单，不需要计算金额
      if (this.orderData.orderType === 'points') {
        return
      }
      
      let totalAmount = 0
      
      // 如果有多个商品（从购物车加载），使用 allItems 计算总价
      if (this.orderData.allItems && this.orderData.allItems.length > 0) {
        this.orderData.allItems.forEach(item => {
          const price = parseFloat(item.price || 0)
          const quantity = parseInt(item.quantity || 1)
          totalAmount += price * quantity
        })
      } else {
        // 单个商品的情况
        const price = (this.orderData.selectedSpec && this.orderData.selectedSpec.price) || (this.orderData.goods && this.orderData.goods.price) || 0
        const quantity = this.orderData.quantity || 1
        totalAmount = parseFloat(price) * quantity
      }
      
      this.orderData.totalAmount = totalAmount.toFixed(2)
      this.orderData.shippingFee = 0 // 免费配送
      this.recalcFinalAmount()
      // 金额确定后加载可用优惠券（后端按门槛/范围计算可用性与优惠额）
      this.loadUsableCoupons()
    },

    /** 按（商品金额+运费-优惠券）重算实付金额；优惠以后端计算的 discountAmount 为准 */
    recalcFinalAmount() {
      let amount = parseFloat(this.orderData.totalAmount || 0) + parseFloat(this.orderData.shippingFee || 0)
      if (this.selectedCoupon && this.selectedCoupon.usable) {
        amount -= parseFloat(this.selectedCoupon.discountAmount || 0)
      }
      if (amount < 0) amount = 0
      this.orderData.finalAmount = amount.toFixed(2)
    },

    /** 当前订单商品ID集合（选券范围匹配用） */
    currentProductIds() {
      const ids = []
      if (this.orderData.allItems && this.orderData.allItems.length > 0) {
        this.orderData.allItems.forEach(item => {
          if (item.productId) ids.push(item.productId)
          else if (item.id) ids.push(item.id)
        })
      } else if (this.orderData.goods && this.orderData.goods.id) {
        ids.push(this.orderData.goods.id)
      }
      return ids
    },

    async loadUsableCoupons() {
      // 仅商品类订单支持优惠券（服务/积分订单跳过）
      const t = this.orderData.orderType
      if (t === 'service' || t === 'points') {
        return
      }
      try {
        const res = await api.getUsableCoupons(
          this.currentProductIds(),
          parseFloat(this.orderData.totalAmount || 0)
        )
        const list = (res && res.data) || []
        this.usableCoupons = list
        // 已选券失效保护：金额变化导致已选券不可用时自动取消选择
        if (this.selectedCoupon) {
          const still = list.find(c => c.id === this.selectedCoupon.id && c.usable)
          this.selectedCoupon = still || null
        }
        this.recalcFinalAmount()
      } catch (e) {
        // 可用券查询失败不阻断下单流程
        this.usableCoupons = []
      }
    },

    openCouponPicker() {
      this.showCouponModal = true
      this.couponLoading = this.usableCoupons.length === 0
      this.loadUsableCoupons().finally(() => { this.couponLoading = false })
    },

    closeCouponPicker() {
      this.showCouponModal = false
    },

    chooseCoupon(c) {
      if (c && !c.usable) return
      this.selectedCoupon = c
      this.recalcFinalAmount()
      this.closeCouponPicker()
    },

    couponThresholdText(c) {
      const t = parseFloat(c.thresholdAmount || 0)
      return t > 0 ? `满${t}元可用` : '无门槛'
    },

    couponScopeText(c) {
      if (c.scope === 'product') return '指定商品可用'
      if (c.scope === 'category') return '指定分类可用'
      return '全场通用'
    },

    formatDateText(v) {
      if (!v) return '-'
      return String(v).replace('T', ' ').slice(0, 10)
    },

    submitOrder() {
      // 服务订单不需要收货地址
      if (this.orderData.orderType !== 'service' && !this.selectedAddress.name) {
        uni.showToast({
          title: '请选择收货地址',
          icon: 'none'
        })
        return
      }

      // 如果是积分订单，直接兑换
      if (this.orderData.orderType === 'points') {
        this.exchangePointsProduct()
        return
      }

      if (!this.selectedPaymentMethod.id) {
        uni.showToast({
          title: '请选择支付方式',
          icon: 'none'
        })
        return
      }

      // 已有待支付订单（从订单列表/详情跳转），直接弹出支付弹窗
      if (this.existingOrderId) {
        this.currentOrderId = this.existingOrderId
        this.currentOrderInfo = this.currentOrderInfo || { orderId: this.existingOrderId, orderType: 'product' }
        this.showPaymentModal = true
        return
      }

      // 新建订单
      this.createOrder()
    },

    // 积分商品兑换
    async exchangePointsProduct() {
      try {
        uni.showLoading({
          title: '兑换中...',
          mask: true
        })

        // 准备兑换数据，包含地址信息
        const exchangeData = {
          productId: this.orderData.goods.id,
          quantity: this.orderData.quantity || 1
        }
        
        // 如果选择了地址，传递地址ID
        if (this.selectedAddress && this.selectedAddress.id) {
          exchangeData.addressId = this.selectedAddress.id
        } else if (this.selectedAddress && this.selectedAddress.name) {
          // 如果没有ID，传递地址对象
          exchangeData.address = {
            name: this.selectedAddress.name,
            phone: this.selectedAddress.phone,
            province: this.selectedAddress.province || '',
            city: this.selectedAddress.city || '',
            district: this.selectedAddress.district || '',
            detail: this.selectedAddress.detail || this.selectedAddress.fullAddress || ''
          }
        }
        // 与商品订单创建字段对齐，后端优先写入订单，避免只依赖 addressId/默认地址回查
        if (this.selectedAddress && (this.selectedAddress.name || this.selectedAddress.phone)) {
          exchangeData.receiverName = this.selectedAddress.name || this.selectedAddress.contactName || ''
          exchangeData.receiverPhone = this.selectedAddress.phone || this.selectedAddress.contactPhone || ''
          exchangeData.receiverProvince = this.selectedAddress.province || ''
          exchangeData.receiverCity = this.selectedAddress.city || ''
          exchangeData.receiverRegion = this.selectedAddress.district || ''
          exchangeData.receiverDetailAddress = this.selectedAddress.detail || this.selectedAddress.fullAddress || ''
        }

        const response = await api.exchangeProduct(exchangeData)
        uni.hideLoading()

        if (response && (response.code === 200 || response.code === 0)) {
          uni.showToast({
            title: '兑换成功',
            icon: 'success'
          })

          // 兑换成功，跳转到订单列表
          setTimeout(() => {
            uni.redirectTo({
              url: '/order/list'
            })
          }, 1500)
        } else {
          uni.showToast({
            title: response?.msg || '兑换失败',
            icon: 'none'
          })
        }
      } catch (error) {
        uni.hideLoading()
        console.error('兑换失败:', error)
        uni.showToast({
          title: '兑换失败，请重试',
          icon: 'none'
        })
      }
    },

    getOrderImageUrl(src) {
      if (!src) return ''
      return util.getImageUrl ? util.getImageUrl(src) : (src.startsWith('http') ? src : (src.startsWith('/') ? src : '/' + src))
    },

    async createOrder() {
      try {
        // 服务订单：不调用商品下单 API，直接展示支付弹窗
        if (this.orderData.orderType === 'service') {
          const orderInfo = {
            id: Date.now(),
            orderId: 'SRV' + Date.now(),
            orderType: 'service',
            serviceType: this.orderData.serviceType,
            serviceId: this.orderData.serviceId,
            goods: this.orderData.goods,
            selectedSpec: null,
            quantity: 1,
            address: null,
            paymentMethod: this.selectedPaymentMethod,
            totalAmount: this.orderData.totalAmount,
            shippingFee: 0,
            finalAmount: this.orderData.finalAmount,
            status: 'pending',
            createTime: new Date().getTime()
          }
          await new Promise(resolve => setTimeout(resolve, 300))
          this.currentOrderId = orderInfo.orderId
          this.currentOrderInfo = orderInfo
          this.showPaymentModal = true
          return
        }

        uni.showLoading({
          title: '创建订单中...',
          mask: true
        })

        const orderInfo = {
          id: Date.now(),
          orderId: 'ORD' + Date.now(),
          goods: this.orderData.goods,
          selectedSpec: this.orderData.selectedSpec,
          quantity: this.orderData.quantity,
          address: this.selectedAddress,
          paymentMethod: this.selectedPaymentMethod,
          totalAmount: this.orderData.totalAmount,
          shippingFee: this.orderData.shippingFee,
          finalAmount: this.orderData.finalAmount,
          status: 'pending',
          createTime: new Date().getTime()
        }

        // 调用后端 API 创建订单（此前为模拟数据，未落库，导致「我的订单」列表始终为空）
        try {
          const currentUserId = this.getCurrentUserId()
          if (!currentUserId) {
            uni.hideLoading()
            uni.showToast({ title: '请先登录', icon: 'none' })
            return
          }

          const apiOrderData = {
            userId: currentUserId,
            totalAmount: parseFloat(orderInfo.finalAmount),
            items: [{
              productId: orderInfo.goods.id,
              productName: orderInfo.goods.name,
              productImage: orderInfo.goods.image,
              productPrice: orderInfo.goods.price,
              quantity: orderInfo.quantity,
              specName: orderInfo.selectedSpec ? orderInfo.selectedSpec.name : '',
              specPrice: orderInfo.selectedSpec ? orderInfo.selectedSpec.price : null
            }]
          }

          // 已选优惠券：后端重新校验归属/门槛/金额并核销（不信任前端计算的优惠额）
          if (this.selectedCoupon && this.selectedCoupon.usable) {
            apiOrderData.userCouponId = this.selectedCoupon.id
          }

          if (this.selectedAddress && this.selectedAddress.id) {
            apiOrderData.addressId = this.selectedAddress.id
          } else if (this.selectedAddress && this.selectedAddress.name) {
            apiOrderData.receiverName = this.selectedAddress.name || this.selectedAddress.contactName || ''
            apiOrderData.receiverPhone = this.selectedAddress.phone || this.selectedAddress.contactPhone || ''
            apiOrderData.receiverProvince = this.selectedAddress.province || ''
            apiOrderData.receiverCity = this.selectedAddress.city || ''
            apiOrderData.receiverRegion = this.selectedAddress.district || ''
            apiOrderData.receiverDetailAddress = this.selectedAddress.detail || this.selectedAddress.fullAddress || ''
          }

          const response = await api.createOrder(apiOrderData)
          if (response && (response.code === 200 || response.code === 0) && response.data) {
            orderInfo.id = response.data.id
            orderInfo.orderId = response.data.orderNo || response.data.orderSn || orderInfo.orderId
            this.currentOrderBackendId = response.data.id
          } else {
            throw new Error((response && (response.msg || response.message)) || '创建订单失败')
          }
        } catch (error) {
          uni.hideLoading()
          uni.showToast({
            title: (error && error.message) || '创建订单失败',
            icon: 'none'
          })
          return
        }
        
        // 保存订单到本地存储（作为备份）
        try {
          const existingOrders = uni.getStorageSync('orderList') || []
          existingOrders.unshift(orderInfo)
          uni.setStorageSync('orderList', existingOrders)
        } catch (error) {
          // 静默处理错误
        }
        
        // 模拟API调用延迟
        await new Promise(resolve => setTimeout(resolve, 500))
        
        uni.hideLoading()
        
        // 保存订单信息，显示支付弹窗
        this.currentOrderId = orderInfo.orderId
        this.currentOrderInfo = orderInfo
        this.showPaymentModal = true
        
      } catch (error) {
        uni.hideLoading()
        console.error('创建订单失败:', error)
        uni.showToast({
          title: '创建订单失败',
          icon: 'none'
        })
      }
    }
  }
}
</script>

<style scoped>
.order-confirm {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 120rpx;
}

.address-section, .product-section, .order-info, .payment-section {
  background-color: #fff;
  margin-bottom: 16rpx;
}

.address-section {
  padding: 20rpx 24rpx;
}

.address-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
}

.address-content {
  flex: 1;
  padding-right: 20rpx;
}

.change-btn {
  padding: 8rpx 16rpx;
  background-color: #f5f5f5;
  border-radius: 6rpx;
  flex-shrink: 0;
}

.btn-text {
  font-size: 24rpx;
  color: #666;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 0 16rpx 0;
  margin-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.recipient {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-right: 16rpx;
}

.phone {
  font-size: 26rpx;
  color: #666;
}

.address-detail {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

.no-address {
  padding: 30rpx 24rpx;
  text-align: center;
  color: #999;
}

.add-text {
  font-size: 26rpx;
}

.product-section {
  padding: 20rpx 24rpx;
}

.product-item {
  display: flex;
  padding: 0;
}

.product-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 6rpx;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.product-info-no-image {
  margin-left: 0;
}

.product-name {
  font-size: 28rpx;
  color: #333;
  margin-bottom: 8rpx;
  line-height: 1.4;
}

.product-spec {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 8rpx;
}

.price-quantity {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  font-size: 28rpx;
  font-weight: 600;
  color: #ff4444;
  display: flex;
  align-items: center;
}

.quantity {
  font-size: 26rpx;
  color: #666;
}

.order-info {
  padding: 20rpx 24rpx;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  font-size: 26rpx;
  color: #666;
}

.value {
  font-size: 26rpx;
  color: #333;
}

.total .label {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.total .value {
  font-size: 28rpx;
  font-weight: 600;
  color: #ff4444;
  display: flex;
  align-items: center;
}

.payment-section {
  padding: 20rpx 24rpx;
}

.payment-methods {
  padding: 0;
}

.payment-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.payment-item:last-child {
  border-bottom: none;
}

.method-info {
  display: flex;
  align-items: center;
}

.method-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.method-icon-img {
  width: 32rpx;
  height: 32rpx;
  margin-right: 12rpx;
  flex-shrink: 0;
}

.method-name {
  font-size: 26rpx;
  color: #333;
}

.method-radio {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.method-radio.active {
  border-color: #ff4444;
}

.radio-icon {
  font-size: 24rpx;
  color: #ff4444;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background-color: #fff;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.total-info {
  flex: 1;
  display: flex;
  align-items: center;
}

.total-label {
  font-size: 26rpx;
  color: #666;
  margin-right: 6rpx;
}

.can-icon-inline {
  width: 28rpx;
  height: 28rpx;
  margin-right: 6rpx;
  vertical-align: middle;
}

.total-amount-points {
  display: flex;
  align-items: center;
  font-size: 36rpx;
  font-weight: 600;
  color: #ff6b35;
}

.total-amount {
  font-size: 32rpx;
  font-weight: 600;
  color: #ff4444;
}

.pay-btn {
  width: 180rpx;
  height: 64rpx;
  background-color: #ffd700;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pay-btn .btn-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

/* 支付弹窗样式 */
.payment-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.payment-modal-content {
  width: 84%;
  max-width: 520rpx;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx 22rpx;
  max-height: 75vh;
  overflow-y: auto;
}

.payment-header {
  text-align: center;
  margin-bottom: 22rpx;
}

.payment-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.order-number {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.payment-amount-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  margin-bottom: 22rpx;
}

.payment-amount-label {
  font-size: 28rpx;
  color: #333;
}

.payment-amount-value {
  font-size: 40rpx;
  font-weight: 600;
  color: #ff4444;
}

.payment-method-section {
  margin-bottom: 30rpx;
}

.payment-method-section .section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.payment-method-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.payment-method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background-color: #f8f8f8;
  border-radius: 12rpx;
}

.method-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.method-icon-large {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.method-icon-large-img {
  width: 48rpx;
  height: 48rpx;
  margin-right: 20rpx;
  flex-shrink: 0;
}

.method-text {
  display: flex;
  flex-direction: column;
}

.method-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 6rpx;
}

.method-desc {
  font-size: 24rpx;
  color: #999;
}

.method-radio-large {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
}

.method-radio-large.active {
  border-color: #ff4444;
  background-color: #ff4444;
}

.radio-icon-large {
  font-size: 24rpx;
  color: #fff;
  display: none;
}

.method-radio-large.active .radio-icon-large {
  display: block;
}

.payment-confirm-btn {
  width: 100%;
  height: 74rpx;
  background-color: #ffd700;
  border-radius: 37rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 16rpx;
}

.payment-confirm-btn:active {
  opacity: 0.9;
}

.payment-confirm-btn.disabled {
  background-color: #c0c0c0;
  pointer-events: none;
}

.payment-confirm-btn.disabled:active {
  background-color: #c0c0c0;
}

.confirm-btn-text {
  font-size: 28rpx;
  color: #333;
  font-weight: 600;
}

/* ==================== 优惠券行 ==================== */
.coupon-row {
  cursor: pointer;
}
.coupon-value-wrap {
  display: flex;
  align-items: center;
}
.coupon-selected {
  font-size: 28rpx;
  color: #ff4d4f;
  font-weight: 600;
}
.coupon-available {
  font-size: 26rpx;
  color: #ff7a59;
}
.coupon-none {
  font-size: 26rpx;
  color: #bfbfbf;
}
.coupon-arrow {
  font-size: 30rpx;
  color: #bfbfbf;
  margin-left: 8rpx;
}

/* ==================== 优惠券选择弹层 ==================== */
.coupon-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}
.coupon-modal-content {
  width: 100%;
  background: #ffffff;
  border-radius: 32rpx 32rpx 0 0;
  padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  max-height: 75vh;
  display: flex;
  flex-direction: column;
}
.coupon-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}
.coupon-modal-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #262626;
}
.coupon-modal-close {
  font-size: 32rpx;
  color: #bfbfbf;
  padding: 8rpx;
}
.coupon-modal-body {
  flex: 1;
  max-height: 52vh;
  overflow: hidden;
}
.coupon-modal-loading,
.coupon-modal-empty {
  text-align: center;
  padding: 80rpx 0;
  color: #8c8c8c;
  font-size: 26rpx;
}
.coupon-option {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}
.coupon-option:last-child {
  border-bottom: none;
}
.coupon-option-disabled {
  opacity: 0.55;
}
.coupon-option-left {
  width: 160rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  border-radius: 12rpx;
  padding: 20rpx 8rpx;
  background: linear-gradient(135deg, #ff7a59, #ff4d4f);
}
.coupon-option-left-disabled {
  background: linear-gradient(135deg, #c0c4cc, #909399);
}
.coupon-option-value {
  font-size: 36rpx;
  font-weight: 700;
}
.coupon-option-threshold {
  font-size: 20rpx;
  margin-top: 4rpx;
  opacity: 0.9;
}
.coupon-option-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  margin-left: 24rpx;
  min-width: 0;
}
.coupon-option-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #262626;
}
.coupon-option-desc {
  font-size: 22rpx;
  color: #8c8c8c;
}
.coupon-option-reason {
  font-size: 22rpx;
  color: #ff4d4f;
}
.coupon-option-date {
  font-size: 22rpx;
  color: #bfbfbf;
}
.coupon-option-radio {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #d9d9d9;
  margin-left: 20rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.coupon-option-radio.active {
  background: #ff4d4f;
  border-color: #ff4d4f;
}
.radio-icon {
  color: #ffffff;
  font-size: 24rpx;
}
.coupon-modal-footer {
  margin-top: 24rpx;
}
.coupon-clear-btn {
  text-align: center;
  padding: 20rpx 0;
  border-radius: 44rpx;
  background: #f5f5f5;
}
.coupon-clear-text {
  font-size: 28rpx;
  color: #595959;
}
</style>
