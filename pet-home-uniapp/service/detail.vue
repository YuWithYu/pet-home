<template>
  <view class="service-detail-container">
    <view class="service-banner">
      <image 
        :src="getImageUrl(serviceData.bannerImage)" 
        mode="aspectFill" 
        class="banner-image"
        @error="onBannerError"
      />
    </view>

    <view class="service-info">
      <view class="price-title">
        <text class="price">{{ priceDisplay }}</text>
        <text class="title">{{ serviceData.name }}</text>
      </view>

      <view class="section">
        <view class="section-title">{{ ui.introTitle }}</view>
        <view class="content-list">
          <view 
            class="content-item" 
            v-for="(item, index) in serviceData.introduction" 
            :key="index"
          >
            <text class="item-number">({{ index + 1 }})</text>
            <text class="item-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-title">{{ ui.noticeTitle }}</view>
        <view class="content-list" v-if="serviceData.instructions && serviceData.instructions.length > 0">
          <view 
            class="content-item" 
            v-for="(item, index) in serviceData.instructions" 
            :key="index"
          >
            <text class="item-number">({{ index + 1 }})</text>
            <text class="item-text">{{ item }}</text>
          </view>
        </view>
        <view v-else class="empty-tip">
          <text class="empty-text">{{ ui.emptyNotice }}</text>
        </view>
      </view>

    </view>

    <view class="book-button-fixed" @tap="onBookNow">
      <text class="button-text">{{ ui.bookNow }}</text>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'
import { util } from '@/common/js/util.js'

export default {
  name: 'ServiceDetail',
  
  data() {
    return {
      serviceData: {
        id: null,
        name: '',
        price: 0,
        bannerImage: '',
        introduction: [],
        instructions: []
      },
      serviceType: '',
      serviceId: null,
      // Unicode escapes: avoids source file encoding corrupting Chinese / currency symbol
      ui: {
        introTitle: '\u670d\u52a1\u4ecb\u7ecd',
        noticeTitle: '\u8d2d\u4e70\u987b\u77e5',
        emptyNotice: '\u6682\u65e0\u8d2d\u4e70\u987b\u77e5\u8bf4\u660e',
        bookNow: '\u7acb\u5373\u9884\u7ea6'
      },
      toastMissingType: '\u7f3a\u5c11\u670d\u52a1\u7c7b\u578b\u53c2\u6570',
      toastUnsupported: '\u6682\u4e0d\u652f\u6301\u8be5\u670d\u52a1\u7c7b\u578b',
      toastLoadFail: '\u52a0\u8f7d\u5931\u8d25'
    }
  },

  computed: {
    priceDisplay() {
      const p = this.serviceData.price
      return '\u00a5' + (p === undefined || p === null ? '' : p)
    }
  },

  onLoad(options) {
    if (!options || !options.serviceType) {
      uni.showToast({
        title: this.toastMissingType,
        icon: 'none'
      })
      return
    }

    this.serviceType = options.serviceType

    const idParam = options.serviceId || options.id
    if (idParam) {
      const parsedId = parseInt(idParam, 10)
      if (!isNaN(parsedId)) {
        this.serviceId = parsedId
      }
    }

    this.loadServiceData()
  },

  methods: {
    async loadServiceData() {
      try {
        switch (this.serviceType) {
          case 'hospital':
            await this.loadHospitalServiceData()
            break
          case 'litter':
            await this.loadLitterServiceData()
            break
          case 'medical':
            await this.loadMedicalServiceData()
            break
          case 'grooming':
            await this.loadGroomingServiceData()
            break
          default:
            uni.showToast({
              title: this.toastUnsupported,
              icon: 'none'
            })
        }
      } catch (error) {
        console.error('loadServiceData failed:', error)
        uni.showToast({
          title: this.toastLoadFail,
          icon: 'none'
        })
      }
    },

    async loadServiceTypeBanner() {
      let url = ''
      if (this.serviceType === 'litter') {
        url = '/api/litter-banner'
      } else if (this.serviceType === 'grooming') {
        url = '/api/grooming-banner'
      } else if (this.serviceType === 'hospital') {
        url = '/api/hospital-banners/position/hospital-page-top'
      } else if (this.serviceType === 'medical') {
        url = '/api/medical-banner'
      }
      if (!url) return
      try {
        const res = await api.request({
          url,
          method: 'GET',
          showLoading: false
        })
        const img = (res.data && (res.data.imageUrl || res.data.image_url || res.data.fileUrl)) || ''
        if ((res.code === 0 || res.code === 200) && img) {
          this.serviceData.bannerImage = img
        }
      } catch (e) {
      }
    },

    async loadHospitalServiceData() {
      try {
        let service = null

        if (this.serviceId) {
          const detailResp = await api.getHospitalServiceById(this.serviceId)
          if ((detailResp.code === 0 || detailResp.code === 200) && detailResp.data) {
            service = detailResp.data
          }
        }

        if (!service) {
          const pageResp = await api.getHospitalServicePage({
            pageNo: 1,
            pageSize: 10,
            status: 'active'
          })
          if ((pageResp.code === 0 || pageResp.code === 200) && pageResp.data) {
            if (Array.isArray(pageResp.data.records) && pageResp.data.records.length > 0) {
              service = pageResp.data.records.find(item => !this.serviceId || item.id === Number(this.serviceId)) || pageResp.data.records[0]
            } else if (Array.isArray(pageResp.data) && pageResp.data.length > 0) {
              service = pageResp.data[0]
            }
          }
        }

        if (service) {
          const introduction = this.normalizeArrayField(service.introduction, this.parseDescriptionToList(service.description))
          const instructions = this.normalizeArrayField(service.instructions)

          this.serviceData = {
            id: service.id,
            name: service.name || '上门服务',
            price: Number(service.price || 0),
            bannerImage: this.getServiceImageUrl(service) || service.bannerImage || this.getDefaultBannerForServiceType(),
            introduction,
            instructions
          }
          const noValidImage = !this.getServiceImageUrl(service) && !service.bannerImage
          const isDefaultImage = (this.getServiceImageUrl(service) || service.bannerImage || '').includes('login-dog')
          if (noValidImage || isDefaultImage) {
            await this.loadServiceTypeBanner()
          }
        } else {
          this.serviceData = {
            id: null,
            name: '服务暂不可用',
            price: 0,
            bannerImage: this.getDefaultBannerForServiceType(),
            introduction: [],
            instructions: []
          }
        }
      } catch (error) {
        console.error('加载服务数据失败:', error)
        this.serviceData = {
          id: null,
          name: '加载失败',
          price: 0,
          bannerImage: this.getDefaultBannerForServiceType(),
          introduction: [],
          instructions: []
        }
      }
    },

    async loadLitterServiceData() {
      try {
        let service = null
        
        if (this.serviceId) {
          const response = await api.getLitterServiceById(this.serviceId)
          if (response.code === 0 || response.code === 200) {
            service = response.data
          }
        } else {
          const response = await api.getLitterServicePage({
            pageNo: 1,
            pageSize: 1,
            status: 'active'
          })
          
          if ((response.code === 0 || response.code === 200) && response.data) {
            if (response.data.records && response.data.records.length > 0) {
              service = response.data.records[0]
            } else if (Array.isArray(response.data) && response.data.length > 0) {
              service = response.data[0]
            } else if (response.data && !response.data.records) {
              service = response.data
            }
          }
        }
        
        
        if (service) {
          const introduction = service.introduction 
            ? (Array.isArray(service.introduction) ? service.introduction : [])
            : (service.description ? this.parseDescriptionToList(service.description) : [])
          
          let instructions = []
          if (service.instructions) {
            if (Array.isArray(service.instructions)) {
              instructions = service.instructions
            } else if (typeof service.instructions === 'string') {
              try {
                const parsed = JSON.parse(service.instructions)
                instructions = Array.isArray(parsed) ? parsed : []
              } catch (e) {
                instructions = service.instructions.split('\n').filter(line => line.trim())
              }
            }
          }
          
          
          this.serviceData = {
            id: service.id,
            name: service.name,
            price: service.price,
            bannerImage: this.getServiceImageUrl(service) || this.getDefaultBannerForServiceType(),
            introduction: introduction,
            instructions: instructions
          }
          const noValidImage = !this.getServiceImageUrl(service)
          const isDefaultImage = (this.getServiceImageUrl(service) || '').includes('login-dog')
          if (noValidImage || isDefaultImage) {
            await this.loadServiceTypeBanner()
          }
        } else {
          this.serviceData = {
            id: null,
            name: '服务暂不可用',
            price: 0,
            bannerImage: this.getDefaultBannerForServiceType(),
            introduction: [],
            instructions: []
          }
        }
      } catch (error) {
        console.error('加载服务详情失败:', error)
        uni.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        })
        this.serviceData = {
          id: null,
          name: '服务不可用',
          price: 0,
          bannerImage: this.getDefaultBannerForServiceType(),
          introduction: [],
          instructions: []
        }
      }
    },

    async loadMedicalServiceData() {
      try {
        const response = await api.getMedicalServicePage({
          current: 1,
          size: 10,
          status: 'active'
        })
        
        if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
          const service = response.data.records.find(item => !this.serviceId || item.id === Number(this.serviceId)) || response.data.records[0]
          this.serviceData = {
            id: service.id,
            name: service.name,
            price: service.price,
            bannerImage: this.getServiceImageUrl(service) || this.getDefaultBannerForServiceType(),
            introduction: this.parseDescriptionToList(service.description),
            instructions: [
              '专业宠物医疗团队',
              '预约1小时',
              '全天候服务'
            ]
          }
          const noValidImage = !this.getServiceImageUrl(service)
          const isDefaultImage = (this.getServiceImageUrl(service) || '').includes('login-dog')
          if (noValidImage || isDefaultImage) {
            await this.loadServiceTypeBanner()
          }
        } else {
          this.loadDefaultMedicalServiceData()
        }
      } catch (error) {
        console.error('加载医疗服务失败:', error)
        this.loadDefaultMedicalServiceData()
      }
    },

    loadDefaultMedicalServiceData() {
      this.serviceData = {
        id: 3,
        name: '宠物医疗服务',
        price: 200,
        bannerImage: this.getDefaultBannerForServiceType(),
        introduction: [
          '提供全面的宠物医疗服务',
          '包括疫苗接种、体检等',
          '专业兽医团队为您服务',
          '环境整洁舒适',
          '配备先进医疗设备',
          '营业时间 8:00-20:00',
          '提供24小时急诊',
          '支持医保支付',
          '预约优先服务'
        ],
        instructions: [
          '请携带宠物疫苗本',
          '提前1小时到达',
          '遵守医院规定'
        ]
      }
    },

    async loadGroomingServiceData() {
      try {
        let service = null

        if (this.serviceId) {
          const detailResp = await api.getGroomingServiceById(this.serviceId)
          if ((detailResp.code === 0 || detailResp.code === 200) && detailResp.data) {
            service = detailResp.data
          }
        }

        if (!service) {
          const listResp = await api.getGroomingServiceList()
          if ((listResp.code === 0 || listResp.code === 200) && listResp.data) {
            const list = Array.isArray(listResp.data)
              ? listResp.data
              : (listResp.data.records || listResp.data.list || [])
            if (list.length > 0) {
              service =
                list.find(item => !this.serviceId || Number(item.id) === Number(this.serviceId)) ||
                list[0]
            }
          }
        }

        if (service) {
          const introduction = this.normalizeArrayField(service.introduction, this.parseDescriptionToList(service.description))
          const instructions = this.normalizeArrayField(service.instructions)

          this.serviceData = {
            id: service.id,
            name: service.name || '宠物美容服务',
            price: Number(service.price || 0),
            bannerImage: this.getServiceImageUrl(service) || service.bannerImage || this.getDefaultBannerForServiceType(),
            introduction,
            instructions
          }
          const noValidImage = !this.getServiceImageUrl(service) && !service.bannerImage
          const isDefaultImage = (this.getServiceImageUrl(service) || service.bannerImage || '').includes('login-dog')
          if (noValidImage || isDefaultImage) {
            await this.loadServiceTypeBanner()
          }
        } else {
          this.loadDefaultGroomingServiceData()
        }
      } catch (error) {
        console.error('加载美容服务失败:', error)
        this.loadDefaultGroomingServiceData()
      }
    },

    loadDefaultGroomingServiceData() {
      this.serviceData = {
        id: 4,
        name: '宠物美容服务',
        price: 150,
        bannerImage: this.getDefaultBannerForServiceType(),
        introduction: [
          '专业宠物美容护理',
          '洗澡、修剪、造型',
          '使用宠物专用产品',
          '环境干净舒适',
          '服务时长 60-90 分钟',
          '提前预约优惠',
          '提供接送服务',
          '支持多种支付方式',
          '会员享受折扣'
        ],
        instructions: [
          '请携带宠物免疫证明',
          '提前1小时到达',
          '遵守美容店规定'
        ]
      }
    },

    onBookNow() {
      const price = Number(this.serviceData.price) || 0
      const serviceId = this.serviceData.id || this.serviceId || ''
      const serviceName = this.serviceData.name || ''
      uni.setStorageSync('serviceBookingBannerImage', this.serviceData.bannerImage || '')

      let url = ''
      switch (this.serviceType) {
        case 'litter':
          url = `/appointment/book-door-cleaning?serviceId=${serviceId}&serviceName=${encodeURIComponent(serviceName || '上门服务')}&price=${price}`
          break
        case 'hospital':
          url = `/appointment/book-hospital?serviceId=${serviceId}&serviceName=${encodeURIComponent(serviceName || '医院服务')}&price=${price}`
          break
        case 'medical':
          url = `/appointment/book-hospital?serviceType=medical&serviceId=${serviceId}&serviceName=${encodeURIComponent(serviceName || '医疗服务')}&price=${price}`
          break
        case 'grooming':
          url = `/appointment/book-grooming?serviceId=${serviceId}&serviceName=${encodeURIComponent(serviceName || '宠物美容服务')}&price=${price}`
          break
      }
      if (url) {
        uni.navigateTo({ url })
      } else {
        uni.showToast({
          title: '该服务暂不支持预约',
          icon: 'none'
        })
      }
    },

    onBannerError(e) {
      const current = this.serviceData.bannerImage || ''
      if (current && !current.includes('login-dog') && (current.includes('upload') || current.includes('http'))) {
        return
      }
      this.serviceData.bannerImage = this.getDefaultBannerForServiceType()
    },

    getDefaultBannerForServiceType() {
      const map = {
        litter: '/static/images/door-cleaning.svg',
        'door-cleaning': '/static/images/door-cleaning.svg',
        grooming: '/static/images/pet-grooming.svg',
        hospital: '/static/images/hospital.png',
        medical: '/static/images/hospital.png'
      }
      return map[this.serviceType] || '/static/images/login-dog.png'
    },

    getServiceImageUrl(service) {
      if (!service) return ''
      const u = service.imageUrl || service.image_url || ''
      return typeof u === 'string' ? u.trim() : ''
    },

    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
    },

    normalizeArrayField(value, fallback = []) {
      if (Array.isArray(value)) {
        return value.map(item => (typeof item === 'string' ? item.trim() : item)).filter(item => !!item)
      }

      if (typeof value === 'string' && value.trim()) {
        const raw = value.trim()
        try {
          const parsed = JSON.parse(raw)
          if (Array.isArray(parsed)) {
            return parsed.map(item => (typeof item === 'string' ? item.trim() : item)).filter(item => !!item)
          }
        } catch (e) {
          // ignore json parse error
        }
        const byLine = raw.split(/\r?\n|[,;???]/).map(item => item.trim()).filter(item => !!item)
        if (byLine.length) {
          return byLine
        }
      }

      return Array.isArray(fallback) ? fallback : []
    },

    parseDescriptionToList(description) {
      if (!description) {
        return []
      }
      
      if (description.includes('\n')) {
        return description.split('\n').filter(item => item.trim())
      }
      
      if (description.includes(';')) {
        return description.split(';').filter(item => item.trim())
      }
      
      if (description.includes('、')) {
        return description.split('、').filter(item => item.trim())
      }
      
      return [description]
    }
  }
}
</script>

<style lang="scss" scoped>
.service-detail-container {
  min-height: 100vh;
  background-color: #f8f8f8;
  padding-bottom: 140rpx; /* 为底部预约按钮留出空间 */
}

.service-banner {
  width: 100%;
  height: 460rpx;
  
  .banner-image {
    width: 100%;
    height: 100%;
  }
}

.service-info {
  background-color: #ffffff;
  padding: 32rpx 24rpx;
  margin-top: -20rpx;
  border-radius: 20rpx 20rpx 0 0;
  
  .price-title {
    display: flex;
    flex-direction: column;
    margin-bottom: 32rpx;
    
    .price {
      font-size: 38rpx;
      font-weight: normal;
      color: #ff6b35;
      margin-bottom: 8rpx;
    }
    
    .title {
      font-size: 30rpx;
      font-weight: normal;
      color: #333333;
    }
  }
  
  .section {
    margin-bottom: 32rpx;
    
    .section-title {
      font-size: 28rpx;
      font-weight: normal;
      color: #333333;
      margin-bottom: 16rpx;
    }
    
    .content-list {
      .content-item {
        display: flex;
        margin-bottom: 12rpx;
        line-height: 1.55;
        
        .item-number {
          font-size: 26rpx;
          color: #666666;
          margin-right: 6rpx;
          flex-shrink: 0;
        }
        
        .item-text {
          font-size: 26rpx;
          color: #333333;
          flex: 1;
        }
      }
    }
  }

  .empty-tip {
    padding: 16rpx 0;
    text-align: center;
    
    .empty-text {
      font-size: 24rpx;
      color: #999999;
    }
  }
}

.book-button-fixed {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16rpx 24rpx;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  background-color: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .button-text {
    width: 100%;
    height: 80rpx;
    line-height: 80rpx;
    text-align: center;
    font-size: 28rpx;
    color: #333;
    font-weight: bold;
    background-color: #ffd700;
    border-radius: 40rpx;
  }
}
</style>
