<template>
  <view class="logistics-page">
    <!-- 使用系统默认导航栏和状态栏（含系统返回键） -->
    <view class="content">
      <!-- 地图视图 -->
      <view class="map-card" v-if="logisticsInfo.shippingNumber && showMap">
        <map
          class="map-container"
          :latitude="mapCenter.latitude"
          :longitude="mapCenter.longitude"
          :markers="mapMarkers"
          :polyline="mapPolyline"
          :show-location="false"
          :enable-zoom="true"
          :enable-scroll="true"
          :enable-rotate="false"
          :scale="10"
        ></map>
      </view>

      <!-- 地图下方信息区（对齐示例效果） -->
      <view class="logistics-info-card" v-if="logisticsInfo.shippingNumber">
        <view class="pickup-row">
          <view class="pickup-left">
            <text class="pickup-label">取件码</text>
            <text class="pickup-code">{{ pickupCode }}</text>
          </view>
          <view class="action-btn" @click="copyText(pickupCode)">
            <text class="action-btn-text">复制</text>
          </view>
        </view>

        <view class="pickup-station">
          <text class="pickup-station-text">{{ pickupStationText }}</text>
        </view>

        <view class="info-block-row">
          <view class="icon-cell">📦</view>
          <view class="main-cell">
            <view class="main-cell-title">
              <text class="company-name-green">{{ logisticsInfo.shippingCompany || '物流公司' }}</text>
              <text class="tracking-number-text">{{ logisticsInfo.shippingNumber }}</text>
            </view>
          </view>
          <view class="action-btn" @click="copyText(logisticsInfo.shippingNumber)">
            <text class="action-btn-text">复制</text>
          </view>
        </view>

        <view class="info-block-row">
          <view class="icon-cell">👤</view>
          <view class="main-cell">
            <view class="main-cell-title">
              <text class="field-title">快递员：</text>
              <text class="field-value">{{ courierName }}</text>
            </view>
          </view>
          <view class="action-btn" v-if="courierPhone" @click="makePhoneCall(courierPhone)">
            <text class="action-btn-text">拨打电话</text>
          </view>
        </view>

        <view class="info-block-row">
          <view class="icon-cell">🚚</view>
          <view class="main-cell">
            <view class="main-cell-title">
              <text class="field-title">订单编号：</text>
              <text class="field-value">{{ orderNo || orderId || 'N/A' }}</text>
            </view>
            <view class="address-row">
              <text class="address-label">收货地址：</text>
              <text class="address-text" v-if="logisticsInfo.receiverAddress && logisticsInfo.receiverAddress.trim()">
                {{ logisticsInfo.receiverAddress }}
              </text>
              <text class="address-text address-empty" v-else>暂无地址信息</text>
            </view>
          </view>
          <view class="action-btn" @click="copyText(orderNo || orderId)">
            <text class="action-btn-text">复制</text>
          </view>
        </view>
      </view>

      <!-- 物流轨迹 -->
      <view class="track-card">
        <view class="estimated-tip" v-if="estimatedDeliveryTip">{{ estimatedDeliveryTip }}</view>
        <view class="track-list" v-if="tracks && tracks.length > 0">
          <view class="track-item" v-for="(track, index) in tracks" :key="index">
            <view class="track-dot" :class="{ 'active': index === 0, 'completed': index > 0 }"></view>
            <view class="track-line" v-if="index < tracks.length - 1"></view>
            <view class="track-content">
              <view class="track-main">
                <text class="track-info">{{ track.trackingInfo }}</text>
              </view>
              <view class="track-meta">
                <text class="track-time" v-if="track.trackingTime">{{ formatDateTime(track.trackingTime) }}</text>
                <text class="track-location" v-if="track.location">{{ track.location }}</text>
              </view>
            </view>
          </view>
        </view>
        <view class="empty-tracks" v-else>
          <text class="empty-text">暂无物流轨迹信息</text>
          <text class="empty-tip">物流信息更新中，请稍后再查看</text>
        </view>
        
        <!-- 物流咨询提示 -->
        <view class="logistics-tip" v-if="logisticsInfo.shippingCompany && !tracks.length">
          <text class="tip-text">物流太久没有更新？拨打{{ logisticsInfo.shippingCompany }}电话咨询</text>
        </view>
      </view>

      <!-- 提示信息 -->
      <view class="tip-card" v-if="!logisticsInfo.shippingNumber">
        <view class="tip-icon">📦</view>
        <text class="tip-text" v-if="orderStatus === 'pending' || orderStatus === 0">订单待付款，付款后商家将安排发货</text>
        <text class="tip-text" v-else-if="orderStatus === 'paid' || orderStatus === 1">订单已付款，等待商家发货</text>
        <text class="tip-text" v-else-if="orderStatus === 'shipped' || orderStatus === 2">订单已发货，物流信息更新中，请稍后再查看</text>
        <text class="tip-text" v-else>该订单尚未发货，暂无物流信息</text>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'OrderLogistics',
  data() {
    return {
      statusBarHeight: 0,
      orderId: null,
      orderNo: '',
      orderStatus: null,
      logisticsInfo: {},
      tracks: [],
      productImage: '/static/images/default-product.svg',
      showMap: false,
      mapCenter: {
        latitude: 39.908823,
        longitude: 116.397470
      },
      mapMarkers: [],
      mapPolyline: [],
      currentLocation: '',
      destinationLocation: '',
      estimatedDeliveryTip: '',
      pickupCode: '--',
      pickupStationText: '',
      courierName: '暂无信息',
      courierPhone: ''
    }
  },
  onLoad(options) {
    // 获取状态栏高度
    const systemInfo = uni.getSystemInfoSync()
    this.statusBarHeight = systemInfo.statusBarHeight || 0
    
    // 获取订单ID
    this.orderId = options.orderId
    if (!this.orderId) {
      uni.showToast({
        title: '订单ID不存在',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
      return
    }
    
    this.loadLogisticsInfo()
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    
    async loadLogisticsInfo() {
      try {
        uni.showLoading({
          title: '加载中...'
        })
        
        // 获取物流信息
        const response = await api.getOrderLogistics(this.orderId)
        if (response && (response.code === 200 || response.code === 0) && response.data) {
          this.logisticsInfo = response.data
          this.tracks = Array.isArray(response.data.tracks) ? response.data.tracks : []
          this.orderNo = response.data.orderNo || ''
          this.orderStatus = response.data.status || response.data.statusNumber
          this.estimatedDeliveryTip = response.data.estimatedDeliveryTip || ''
        }
        
        // 无论是否有物流信息，都获取订单详情以显示订单状态
        const orderResponse = await api.getOrderDetail(this.orderId)
        if (orderResponse && (orderResponse.code === 200 || orderResponse.code === 0) && orderResponse.data) {
          const order = orderResponse.data
          this.orderNo = order.orderNo || order.orderSn || this.orderNo
          this.orderStatus = order.status || order.statusNumber
          
          // 如果有物流信息但物流API没返回，从订单详情获取
          if (!this.logisticsInfo.shippingNumber && order.shippingNumber) {
            this.logisticsInfo.shippingCompany = order.shippingCompany || ''
            this.logisticsInfo.shippingNumber = order.shippingNumber || ''
            this.logisticsInfo.shippingTime = order.shippingTime || order.shipTime || ''
          }
          
          // 补充收货地址信息（优先使用物流API返回的，否则从订单详情获取）
          if (!this.logisticsInfo.receiverAddress || this.logisticsInfo.receiverAddress === '暂无地址信息') {
            // 从订单详情获取收货地址
            let address = ''
            if (order.receiverProvince) address += order.receiverProvince
            if (order.receiverCity) address += order.receiverCity
            if (order.receiverRegion) address += order.receiverRegion
            if (order.receiverDetailAddress) address += order.receiverDetailAddress
            
            if (address) {
              this.logisticsInfo.receiverAddress = address
            } else if (order.address) {
              // 兼容旧字段
              this.logisticsInfo.receiverAddress = order.address
            }
          }
          
          // 补充收货人信息
          if (!this.logisticsInfo.receiverName && order.receiverName) {
            this.logisticsInfo.receiverName = order.receiverName
            this.logisticsInfo.receiverPhone = order.receiverPhone || ''
          }
          
          // 获取发货仓地址
          this.logisticsInfo.warehouseAddress = response.data.warehouseAddress || order.warehouseAddress || ''
          
          
          // 获取商品图片（从订单商品列表中获取第一张图片）
          if (order.orderItemList && order.orderItemList.length > 0) {
            const firstItem = order.orderItemList[0]
            let imageUrl = firstItem.productPic || firstItem.productImage || firstItem.image || ''
            if (imageUrl) {
              this.productImage = util.getImageUrl(imageUrl)
            }
          } else if (order.products && order.products.length > 0) {
            let imageUrl = order.products[0].image || ''
            if (imageUrl) {
              this.productImage = util.getImageUrl(imageUrl)
            }
          }
          
          // 初始化地图（如果有物流信息）
          if (this.logisticsInfo.shippingNumber) {
            this.initMap()
          }
        }
        // 仅做展示字段补齐（取件码/快递员），轨迹严格使用后端返回
        this.resolveExpressMeta()
        this.tracks = this.normalizeTracksOnly(this.tracks)
        this.estimatedDeliveryTip = this.buildTopTip()
      } catch (error) {
        console.error('加载物流信息失败:', error)
        uni.showToast({
          title: '加载物流信息失败',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    formatDateTime(time) {
      if (!time) return ''
      try {
        const date = util.parseDate(time)
        if (!date) return String(time)
        return util.formatDate(date, 'YYYY-MM-DD HH:mm:ss')
      } catch (e) {
        return String(time)
      }
    },
    resolveExpressMeta() {
      const latestText = this.tracks && this.tracks.length > 0 ? (this.tracks[0].trackingInfo || '') : ''
      const pickupCode = this.extractPickupCode(latestText)
      this.pickupCode = pickupCode || this.logisticsInfo.pickupCode || this.generateMockPickupCode()
      this.pickupStationText = this.logisticsInfo.pickupStation ||
        this.logisticsInfo.stationName ||
        this.logisticsInfo.warehouseAddress ||
        '物流信息以快递公司为准'

      const courierName = this.extractCourierName(latestText)
      const courierPhone = this.extractPhone(latestText)
      this.courierName = courierName || this.logisticsInfo.courierName || this.generateMockCourierName()
      this.courierPhone = courierPhone || this.logisticsInfo.courierPhone || this.generateMockCourierPhone()
    },
    normalizeTracksOnly(tracks) {
      const list = Array.isArray(tracks) ? tracks : []
      return list.map(t => this.normalizeTrackItem(t)).filter(Boolean)
    },
    buildTopTip() {
      const latest = this.tracks && this.tracks.length > 0 ? String(this.tracks[0].trackingInfo || '') : ''
      const deliveredKeywords = ['已签收', '本人签收', '代收', '已妥投', '已送达', '已取件']
      const deliveringKeywords = ['派送中', '派件中', '正在派送', '已离开', '发往', '运输中']
      const arrivedKeywords = ['已到达', '到达']
      const pickedKeywords = ['已收件', '已揽收', '已发货']

      // 优先级：签收 > 派送 > 到达分拨 > 揽收发货，严格跟随最新轨迹
      if (deliveredKeywords.some(k => latest.includes(k))) {
        return '快递已送达，如有问题请联系快递员'
      }
      if (deliveringKeywords.some(k => latest.includes(k))) {
        return '快递派送中，请留意物流或联系快递'
      }
      if (arrivedKeywords.some(k => latest.includes(k))) {
        return '快件已到达目的地，等待派送'
      }
      if (pickedKeywords.some(k => latest.includes(k))) {
        return '快件已揽收，正在运输途中'
      }
      return this.estimatedDeliveryTip || '物流更新中，请稍后查看'
    },
    normalizeTrackItem(track) {
      if (!track) return null
      return {
        ...track,
        trackingInfo: track.trackingInfo || track.context || track.desc || '物流状态更新',
        trackingTime: track.trackingTime || track.time || track.createTime || '',
        location: track.location || track.area || ''
      }
    },
    generateMockPickupCode() {
      const source = String(this.orderNo || this.orderId || Date.now())
      const tail = source.replace(/\D/g, '').slice(-4) || '1024'
      return `${tail.slice(0, 2)}-${tail.slice(2)}`
    },
    generateMockCourierName() {
      const names = ['李师傅', '王师傅', '张师傅', '陈师傅']
      const idx = Number(String(this.orderId || 0).slice(-1)) % names.length
      return names[idx]
    },
    generateMockCourierPhone() {
      const seed = String(this.orderId || Date.now()).replace(/\D/g, '').slice(-8).padStart(8, '0')
      return `13${seed.slice(0, 9)}`
    },
    extractPhone(text) {
      if (!text) return ''
      const m = String(text).match(/1\d{10}/)
      return m ? m[0] : ''
    },
    extractCourierName(text) {
      if (!text) return ''
      const m = String(text).match(/(?:快递员|揽投员)\s*[：: ]?\s*([\u4e00-\u9fa5]{2,6})/)
      return m ? m[1] : ''
    },
    extractPickupCode(text) {
      if (!text) return ''
      const m = String(text).match(/取件码[：:\s]*([A-Za-z0-9-]{2,12})/)
      return m ? m[1] : ''
    },
    makePhoneCall(phone) {
      if (!phone) return
      uni.makePhoneCall({
        phoneNumber: String(phone)
      })
    },
    
    getStatusText(status) {
      const statusMap = {
        0: '待付款',
        1: '待发货',
        2: '待收货',
        3: '已完成',
        4: '已取消',
        'pending': '待付款',
        'paid': '待发货',
        'shipped': '待收货',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return statusMap[status] || '未知状态'
    },
    
    getStatusClass(status) {
      if (status === 0 || status === 'pending') {
        return 'status-pending'
      } else if (status === 1 || status === 'paid') {
        return 'status-paid'
      } else if (status === 2 || status === 'shipped') {
        return 'status-shipped'
      } else if (status === 3 || status === 'completed') {
        return 'status-completed'
      }
      return ''
    },
    
    copyText(text) {
      if (!text) {
        uni.showToast({
          title: '内容为空',
          icon: 'none'
        })
        return
      }
      uni.setClipboardData({
        data: text,
        success: () => {
          uni.showToast({
            title: '已复制',
            icon: 'success'
          })
        },
        fail: () => {
          uni.showToast({
            title: '复制失败',
            icon: 'none'
          })
        }
      })
    },
    
    handleImageError() {
      this.productImage = '/static/images/default-product.svg'
    },
    
    initMap() {
      // 确保有收货地址才显示地图
      if (!this.logisticsInfo.receiverAddress || this.logisticsInfo.receiverAddress === '暂无地址信息') {
        this.showMap = false
        return
      }
      
      this.showMap = true
      
      // 设置目的地（从收货地址中提取城市）
      if (this.logisticsInfo.receiverAddress) {
        const cityName = this.extractCityName(this.logisticsInfo.receiverAddress)
        if (cityName) this.destinationLocation = cityName
      }
      
      // 设置当前位置（从最新轨迹中提取）
      if (this.tracks && this.tracks.length > 0 && this.tracks[0].location) {
        const latestLocation = this.tracks[0].location
        const cityName = this.extractCityName(latestLocation)
        if (cityName) {
          this.currentLocation = `正在发往${cityName}`
        } else {
          this.currentLocation = latestLocation
        }
      } else if (this.logisticsInfo.warehouseAddress) {
        // 如果没有轨迹，从发货仓地址提取
        const cityName = this.extractCityName(this.logisticsInfo.warehouseAddress)
        if (cityName) {
          this.currentLocation = `从${cityName}发货`
        }
      }
      
      // 构建地图标记和路线
      this.buildMapData()
    },
    extractCityName(address) {
      const text = String(address || '').trim()
      if (!text) return ''
      // 先处理直辖市
      const direct = text.match(/(北京市|上海市|天津市|重庆市)/)
      if (direct) return direct[1]
      // 去掉省/自治区/特别行政区前缀，再抓城市，避免出现“省广州市”
      const withoutProvince = text.replace(/^.*?(省|自治区|特别行政区)/, '')
      const city = withoutProvince.match(/[\u4e00-\u9fa5]{2,10}市/)
      return city ? city[0] : ''
    },
    
    // 城市经纬度映射（扩展更多城市）
    getCityCoordinates(cityName) {
      if (!cityName || !cityName.trim()) {
        return { lat: 39.908823, lng: 116.397470 } // 默认北京
      }
      
      const cityMap = {
        '北京': { lat: 39.908823, lng: 116.397470 },
        '上海': { lat: 31.230416, lng: 121.473701 },
        '广州': { lat: 23.129112, lng: 113.264385 },
        '深圳': { lat: 22.543096, lng: 114.057865 },
        '杭州': { lat: 30.274084, lng: 120.155070 },
        '成都': { lat: 30.662420, lng: 104.063321 },
        '武汉': { lat: 30.592849, lng: 114.305392 },
        '西安': { lat: 34.341568, lng: 108.940175 },
        '天津': { lat: 39.343357, lng: 117.200983 },
        '重庆': { lat: 29.563009, lng: 106.551556 },
        '南京': { lat: 32.060255, lng: 118.796877 },
        '苏州': { lat: 31.298886, lng: 120.585315 },
        '郑州': { lat: 34.746611, lng: 113.625367 },
        '长沙': { lat: 28.227779, lng: 112.938814 },
        '沈阳': { lat: 41.805698, lng: 123.431473 },
        '大连': { lat: 38.914003, lng: 121.614682 },
        '青岛': { lat: 36.067108, lng: 120.382607 },
        '济南': { lat: 36.651216, lng: 117.120095 },
        '石家庄': { lat: 38.042759, lng: 114.514861 },
        '太原': { lat: 37.870590, lng: 112.548879 },
        '昆明': { lat: 25.038889, lng: 102.718333 },
        '南宁': { lat: 22.817002, lng: 108.366543 },
        '福州': { lat: 26.074508, lng: 119.296494 },
        '厦门': { lat: 24.479834, lng: 118.081871 },
        '合肥': { lat: 31.820586, lng: 117.227219 },
        '南昌': { lat: 28.682892, lng: 115.857988 },
        '哈尔滨': { lat: 45.773225, lng: 126.657717 },
        '长春': { lat: 43.817071, lng: 125.323544 },
        '贵阳': { lat: 26.647661, lng: 106.630153 },
        '海口': { lat: 20.044412, lng: 110.199890 },
        '乌鲁木齐': { lat: 43.825592, lng: 87.616848 },
        '拉萨': { lat: 29.662557, lng: 91.140856 },
        '西宁': { lat: 36.617134, lng: 101.778223 },
        '银川': { lat: 38.487194, lng: 106.230909 },
        '兰州': { lat: 36.061380, lng: 103.834303 }
      }
      
      // 查找匹配的城市（优先精确匹配，然后模糊匹配）
      // 先尝试精确匹配
      for (const [city, coords] of Object.entries(cityMap)) {
        if (cityName.includes(city + '市') || cityName.includes(city)) {
          return coords
        }
      }
      
      // 如果没找到，尝试提取城市名（去掉省、市等后缀）
      const cityMatch = cityName.match(/(.{2,3})(?:省|市|自治区)/)
      if (cityMatch) {
        const cityKey = cityMatch[1]
        for (const [city, coords] of Object.entries(cityMap)) {
          if (city.includes(cityKey) || cityKey.includes(city)) {
            return coords
          }
        }
      }
      
      // 默认返回北京坐标（但应该尽量避免）
      // 静默处理警告
      return { lat: 39.908823, lng: 116.397470 }
    },
    
    buildMapData() {
      // 构建地图标记点
      const markers = []
      const polyline = []
      
      // 解析发货仓地址和收货地址
      const warehouseAddress = this.logisticsInfo.warehouseAddress || ''
      const receiverAddress = this.logisticsInfo.receiverAddress || ''
      
      // 获取发货地坐标
      const startCoords = this.getCityCoordinates(warehouseAddress)
      let startLat = startCoords.lat
      let startLng = startCoords.lng
      
      // 获取收货地坐标（必须使用收货地址，不能使用默认值）
      if (!receiverAddress || receiverAddress === '暂无地址信息') {
        // 静默处理警告
        this.showMap = false
        return
      }
      
      const endCoords = this.getCityCoordinates(receiverAddress)
      let endLat = endCoords.lat
      let endLng = endCoords.lng
      
      // 如果收货地址是北京，但实际不是北京，说明坐标获取有问题
      // 需要更精确的城市匹配
      if (endLat === 39.908823 && endLng === 116.397470 && !receiverAddress.includes('北京')) {
        // 重新尝试提取城市
        const cityMatch = receiverAddress.match(/(.{2,3}市)/)
        if (cityMatch) {
          const cityName = cityMatch[1]
          const cityCoords = this.getCityCoordinates(cityName)
          endLat = cityCoords.lat
          endLng = cityCoords.lng
        }
      }
      
      // 计算中间点（车辆当前位置）
      let truckLat = (startLat + endLat) / 2
      let truckLng = (startLng + endLng) / 2
      
      // 如果有物流轨迹，根据最新轨迹调整车辆位置
      if (this.tracks && this.tracks.length > 0) {
        const latestTrack = this.tracks[0]
        if (latestTrack.location) {
          const trackCoords = this.getCityCoordinates(latestTrack.location)
          truckLat = trackCoords.lat
          truckLng = trackCoords.lng
        }
      }
      
      // 起点（发货地）
      markers.push({
        id: 0,
        latitude: startLat,
        longitude: startLng,
        width: 20,
        height: 20,
        label: {
          content: '发',
          color: '#fff',
          bgColor: '#52c41a',
          borderRadius: 10,
          padding: 4,
          fontSize: 12
        }
      })
      
      // 终点（收货地）
      markers.push({
        id: 1,
        latitude: endLat,
        longitude: endLng,
        width: 20,
        height: 20,
        label: {
          content: this.destinationLocation || '收',
          color: '#fff',
          bgColor: '#52c41a',
          borderRadius: 10,
          padding: 4,
          fontSize: 12
        }
      })
      
      // 当前车辆位置（在路线上）- 使用文字标签
      if (this.tracks && this.tracks.length > 0) {
        markers.push({
          id: 2,
          latitude: truckLat,
          longitude: truckLng,
          width: 30,
          height: 30,
          label: {
            content: '🚚',
            color: '#52c41a',
            bgColor: 'transparent',
            fontSize: 30
          }
        })
      }
      
      // 构建路线（绿色线条）
      polyline.push({
        points: [
          { latitude: startLat, longitude: startLng },
          { latitude: truckLat, longitude: truckLng },
          { latitude: endLat, longitude: endLng }
        ],
        color: '#52c41a',
        width: 8,
        arrowLine: true,
        borderColor: '#fff',
        borderWidth: 2
      })
      
      this.mapMarkers = markers
      this.mapPolyline = polyline
      
      // 设置地图中心（取起点和终点的中间位置）
      this.mapCenter = {
        latitude: (startLat + endLat) / 2,
        longitude: (startLng + endLng) / 2
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.logistics-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding: 0;
}

/* 地图卡片 */
.map-card {
  width: 100%;
  height: 500rpx;
  background-color: #f5f5f5;
  margin-bottom: 20rpx;
  position: relative;
  overflow: hidden;
}

.map-container {
  width: 100%;
  height: 100%;
}

.map-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 10;
}

.map-status-bubble {
  position: absolute;
  top: 80rpx;
  left: 50%;
  transform: translateX(-50%);
  background-color: #52c41a;
  color: #fff;
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  white-space: nowrap;
  box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
  pointer-events: none;
}

.status-text {
  color: #fff;
  font-size: 24rpx;
}

.map-destination-bubble {
  position: absolute;
  bottom: 80rpx;
  left: 40rpx;
  background-color: #52c41a;
  color: #fff;
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  white-space: nowrap;
  box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.3);
  pointer-events: none;
}

.dest-text {
  color: #fff;
  font-size: 24rpx;
}

/* 物流信息卡片 */
.logistics-info-card {
  background-color: #fff;
  padding: 0;
  margin-bottom: 14rpx;
  margin-left: 14rpx;
  margin-right: 14rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.pickup-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 16rpx 4rpx;
}

.pickup-left {
  display: flex;
  align-items: baseline;
}

.pickup-label {
  font-size: 26rpx;
  color: #52c41a;
  font-weight: 700;
  margin-right: 8rpx;
}

.pickup-code {
  font-size: 30rpx;
  color: #52c41a;
  font-weight: 700;
}

.pickup-station {
  padding: 0 16rpx 8rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.pickup-station-text {
  font-size: 21rpx;
  color: #666;
}

.info-block-row {
  display: flex;
  align-items: center;
  padding: 10rpx 16rpx;
  border-bottom: 1rpx solid #f3f3f3;
}

.info-block-row:last-child {
  border-bottom: 0;
}

.icon-cell {
  width: 34rpx;
  text-align: center;
  margin-right: 10rpx;
  font-size: 24rpx;
}

.main-cell {
  flex: 1;
  min-width: 0;
}

.main-cell-title {
  display: flex;
  align-items: center;
  min-width: 0;
}

.company-name-green {
  font-size: 24rpx;
  color: #52c41a;
  font-weight: 600;
  margin-right: 6rpx;
}

.tracking-number-text {
  font-size: 26rpx;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.field-title {
  font-size: 24rpx;
  color: #555;
}

.field-value {
  font-size: 24rpx;
  color: #333;
}

.action-btn {
  margin-left: 10rpx;
  padding: 4rpx 12rpx;
  border: 1rpx solid #d8d8d8;
  border-radius: 8rpx;
}

.action-btn-text {
  font-size: 21rpx;
  color: #666;
}

.address-row {
  display: flex;
  align-items: flex-start;
  flex-wrap: wrap;
}

.address-label {
  font-size: 24rpx;
  color: #999;
  margin-right: 6rpx;
  flex-shrink: 0;
}

.address-text {
  font-size: 24rpx;
  color: #333;
  flex: 1;
  line-height: 1.3;
  margin-right: 6rpx;
}

.address-card,
.track-card,
.tip-card {
  background-color: #fff;
  border-radius: 0;
  margin-bottom: 14rpx;
  margin-left: 14rpx;
  margin-right: 14rpx;
  padding: 18rpx;
  box-shadow: none;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.card-body {
  display: flex;
  flex-direction: column;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16rpx;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.label {
  font-size: 26rpx;
  color: #999;
  min-width: 140rpx;
}

.value {
  font-size: 26rpx;
  color: #333;
  flex: 1;
  
  &.address-text {
    line-height: 1.6;
  }
}

.estimated-tip {
  padding: 8rpx 8rpx 12rpx;
  font-size: 24rpx;
  color: #ffac30;
  font-weight: 500;
}

.track-list {
  position: relative;
  padding-left: 38rpx;
}

.track-item {
  position: relative;
  padding-bottom: 28rpx;
  
  &:last-child {
    padding-bottom: 0;
  }
}

.track-dot {
  position: absolute;
  left: -28rpx;
  top: 8rpx;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background-color: #d8d8d8;
  border: 3rpx solid #fff;
  box-shadow: 0 0 0 1rpx #d8d8d8;
  z-index: 2;
  
  &.active {
    background-color: #52c41a;
    box-shadow: 0 0 0 2rpx #52c41a;
  }
  
  &.completed {
    background-color: #52c41a;
    box-shadow: 0 0 0 2rpx #52c41a;
  }
}

.track-line {
  position: absolute;
  left: -22rpx;
  top: 20rpx;
  width: 2rpx;
  height: calc(100% - 2rpx);
  background-color: #ececec;
  z-index: 1;
}

.track-content {
  display: flex;
  flex-direction: column;
}

.track-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.track-info {
  font-size: 27rpx;
  color: #333;
  line-height: 1.45;
  flex: 1;
}

.track-subscribe {
  padding: 4rpx 12rpx;
  background-color: #f0f9ff;
  border: 1rpx solid #91d5ff;
  border-radius: 14rpx;
  margin-left: 10rpx;
}

.subscribe-text {
  font-size: 20rpx;
  color: #1890ff;
}

.track-meta {
  display: flex;
  flex-direction: column;
  gap: 2rpx;
}

.track-time {
  font-size: 23rpx;
  color: #999;
}

.track-location {
  font-size: 21rpx;
  color: #999;
}

.logistics-tip {
  margin-top: 16rpx;
  padding: 12rpx;
  background-color: #fff7e6;
  border-left: 4rpx solid #ffc53d;
  border-radius: 8rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #d48806;
  line-height: 1.5;
}

.empty-tracks {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 36rpx 0;
  color: #999;
}

.empty-text {
  font-size: 24rpx;
  margin-bottom: 10rpx;
}

.empty-tip {
  font-size: 20rpx;
  color: #ccc;
}

.tip-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 30rpx;
}

.tip-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.tip-text {
  font-size: 28rpx;
  color: #999;
  text-align: center;
}

</style>
