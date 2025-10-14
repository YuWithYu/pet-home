<template>
  <view class="orders-container">
    <!-- 订单状态筛选 -->
    <view class="order-tabs bg-white">
      <view
        :class="['tab-item', { 'active': activeTab === item.key }]"
        v-for="tab in orderTabs"
        :key="tab.key"
        @click="changeTab(tab.key)"
      >
        {{ tab.name }}
        <view class="tab-count" v-if="tab.count > 0">{{ tab.count }}</view>
      </view>
    </view>

    <!-- 订单列表 -->
    <view class="orders-list">
      <view class="empty-orders" v-if="orders.length === 0">
        <view class="empty-icon">📦</view>
        <view class="empty-text">暂无订单</view>
        <u-button type="primary" @click="goHome" shape="circle">去购物</u-button>
      </view>

      <view class="order-item bg-white" v-for="order in orders" :key="order.id">
        <view class="order-header">
          <view class="order-info">
            <view class="order-id">订单号：{{ order.orderNo }}</view>
            <view class="order-time">{{ order.createTime }}</view>
          </view>
          <view class="order-status">
            <text :class="['status-text', order.status]">{{ getStatusText(order.status) }}</text>
          </view>
        </view>

        <view class="order-body">
          <view class="goods-list">
            <view class="goods-item" v-for="goods in order.goods" :key="goods.id">
              <view class="goods-image">
                <image :src="goods.image" mode="aspectFill" />
              </view>
              <view class="goods-info">
                <view class="goods-name">{{ goods.name }}</view>
                <view class="goods-spec">{{ goods.spec }}</view>
                <view class="goods-price">
                  <text class="price-current">{{ goods.price }}</text>
                  <text class="goods-quantity">×{{ goods.quantity }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="order-footer">
          <view class="order-total">
            共{{ order.goods.length }}件商品 合计：
            <text class="total-price">{{ order.totalAmount }}</text>
          </view>
          <view class="order-actions">
            <u-button
              v-if="order.status === 'pending'"
              type="primary"
              size="mini"
              @click="payOrder(order.id)"
            >
              付款
            </u-button>
            <u-button
              v-if="order.status === 'paid'"
              type="default"
              size="mini"
              @click="remindShip(order.id)"
            >
              提醒发货
            </u-button>
            <u-button
              v-if="order.status === 'shipped'"
              type="primary"
              size="mini"
              @click="confirmReceive(order.id)"
            >
              确认收货
            </u-button>
            <u-button
              v-if="order.status === 'completed'"
              type="default"
              size="mini"
              @click="viewOrder(order.id)"
            >
              查看详情
            </u-button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'Orders',

  data() {
    return {
      activeTab: 'all',
      orders: [
        {
          id: 1,
          orderNo: '202401150001',
          createTime: '2024-01-15 10:30',
          status: 'completed',
          totalAmount: '¥258.00',
          goods: [
            {
              id: 1,
              name: '皇家猫粮',
              spec: '成猫粮 2kg',
              image: '/static/images/product1.jpg',
              price: '¥128.00',
              quantity: 2
            }
          ]
        }
      ],
      orderTabs: [
        { key: 'all', name: '全部', count: 0 },
        { key: 'pending', name: '待付款', count: 0 },
        { key: 'paid', name: '待发货', count: 0 },
        { key: 'shipped', name: '待收货', count: 0 },
        { key: 'completed', name: '已完成', count: 0 }
      ]
    }
  },

  onLoad(options) {
    if (options.status) {
      this.activeTab = options.status
    }
    this.loadOrders()
  },

  methods: {
    changeTab(tabKey) {
      this.activeTab = tabKey
      this.loadOrders()
    },

    loadOrders() {
      // 根据activeTab过滤订单
      this.$api.getOrderList(this.activeTab).then(res => {
        if (res.code === 0 && res.data) {
          this.orders = res.data.orders || []
          this.updateTabCounts()
        }
      }).catch(() => {
        // 使用模拟数据
        this.orders = []
      })
    },

    updateTabCounts() {
      // 更新各个tab的数量
      this.orderTabs.forEach(tab => {
        if (tab.key === 'all') {
          tab.count = this.orders.length
        } else {
          tab.count = this.orders.filter(order => order.status === tab.key).length
        }
      })
    },

    getStatusText(status) {
      const statusMap = {
        'pending': '待付款',
        'paid': '待发货',
        'shipped': '待收货',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return statusMap[status] || '未知状态'
    },

    payOrder(orderId) {
      uni.showModal({
        title: '确认付款',
        content: '确定要付款吗？',
        success: (res) => {
          if (res.confirm) {
            // 调用付款API
            uni.showToast({
              title: '付款成功',
              icon: 'success'
            })
          }
        }
      })
    },

    remindShip(orderId) {
      uni.showToast({
        title: '已提醒卖家发货',
        icon: 'success'
      })
    },

    confirmReceive(orderId) {
      uni.showModal({
        title: '确认收货',
        content: '确认已收到商品吗？',
        success: (res) => {
          if (res.confirm) {
            // 调用确认收货API
            uni.showToast({
              title: '确认收货成功',
              icon: 'success'
            })
          }
        }
      })
    },

    viewOrder(orderId) {
      uni.navigateTo({
        url: `/pages/order/detail?id=${orderId}`
      })
    },

    goHome() {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.orders-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.order-tabs {
  display: flex;
  background-color: white;
  border-bottom: 1rpx solid #f0f0f0;
  padding: 0 20rpx;
}

.tab-item {
  position: relative;
  padding: 30rpx 20rpx;
  font-size: 28rpx;
  color: #666;
  flex: 1;
  text-align: center;

  &.active {
    color: #ff6b35;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 60rpx;
      height: 4rpx;
      background-color: #ff6b35;
      border-radius: 2rpx;
    }
  }
}

.tab-count {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  background-color: #ff6b35;
  color: white;
  font-size: 20rpx;
  padding: 2rpx 8rpx;
  border-radius: 12rpx;
  min-width: 32rpx;
  text-align: center;
}

.orders-list {
  .empty-orders {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 200rpx 60rpx;
    color: #999;

    .empty-icon {
      font-size: 120rpx;
      margin-bottom: 40rpx;
    }

    .empty-text {
      font-size: 32rpx;
      margin-bottom: 60rpx;
    }
  }

  .order-item {
    margin-bottom: 20rpx;
    border-radius: 16rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.1);
  }

  .order-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx;
    border-bottom: 1rpx solid #f0f0f0;
    background-color: #fafafa;
  }

  .order-info {
    .order-id {
      font-size: 28rpx;
      color: #333;
      margin-bottom: 8rpx;
    }

    .order-time {
      font-size: 24rpx;
      color: #999;
    }
  }

  .order-status {
    .status-text {
      font-size: 26rpx;
      padding: 8rpx 16rpx;
      border-radius: 20rpx;

      &.pending {
        background-color: #fff7e6;
        color: #d48806;
      }

      &.paid {
        background-color: #e6fffb;
        color: #13c2c2;
      }

      &.shipped {
        background-color: #f6ffed;
        color: #52c41a;
      }

      &.completed {
        background-color: #f6f6f6;
        color: #666;
      }
    }
  }

  .order-body {
    padding: 30rpx;

    .goods-list {
      .goods-item {
        display: flex;
        align-items: center;
        margin-bottom: 20rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .goods-image {
          width: 120rpx;
          height: 120rpx;
          border-radius: 12rpx;
          overflow: hidden;
          margin-right: 20rpx;

          image {
            width: 100%;
            height: 100%;
          }
        }

        .goods-info {
          flex: 1;

          .goods-name {
            font-size: 28rpx;
            color: #333;
            margin-bottom: 8rpx;
          }

          .goods-spec {
            font-size: 24rpx;
            color: #999;
            margin-bottom: 8rpx;
          }

          .goods-price {
            display: flex;
            align-items: center;
            justify-content: space-between;

            .price-current {
              font-size: 28rpx;
              color: #ff6b35;
              font-weight: bold;
            }

            .goods-quantity {
              font-size: 24rpx;
              color: #999;
            }
          }
        }
      }
    }
  }

  .order-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20rpx 30rpx;
    border-top: 1rpx solid #f0f0f0;
    background-color: #fafafa;
  }

  .order-total {
    font-size: 26rpx;
    color: #666;

    .total-price {
      font-size: 32rpx;
      color: #ff6b35;
      font-weight: bold;
    }
  }

  .order-actions {
    display: flex;
    gap: 16rpx;
  }
}
</style>
