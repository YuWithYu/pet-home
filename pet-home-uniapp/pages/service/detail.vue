<template>
  <view class="service-detail-container">
    <!-- 顶部展示图 -->
    <view class="service-banner">
      <image 
        :src="getImageUrl(serviceData.bannerImage)" 
        mode="aspectFill" 
        class="banner-image"
        @error="onBannerError"
      />
    </view>

    <!-- 服务信息 -->
    <view class="service-info">
      <view class="price-title">
        <text class="price">¥{{ serviceData.price }}</text>
        <text class="title">{{ serviceData.name }}</text>
      </view>

      <!-- 商品介绍 -->
      <view class="section">
        <view class="section-title">商品介绍</view>
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

      <!-- 使用须知 -->
      <view class="section">
        <view class="section-title">使用须知</view>
        <view class="content-list">
          <view 
            class="content-item" 
            v-for="(item, index) in serviceData.instructions" 
            :key="index"
          >
            <text class="item-number">({{ index + 1 }})</text>
            <text class="item-text">{{ item }}</text>
          </view>
        </view>
      </view>

      <!-- 协议说明 -->
      <view class="agreement-text">
        点击预约即代表您阅读并同意《相关协议》
      </view>

      <!-- 预约按钮 -->
      <view class="book-button" @tap="onBookNow">
        <text class="button-text">马上预约</text>
      </view>
    </view>
  </view>
</template>

<script>
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
      serviceType: ''
    }
  },

  onLoad(options) {
    if (options.serviceType) {
      this.serviceType = options.serviceType
      this.loadServiceData()
    }
  },

  methods: {
    async loadServiceData() {
      try {
        // 根据服务类型从后端API加载数据
        switch (this.serviceType) {
          case 'litter':
            await this.loadLitterServiceData()
            break
          case 'boarding':
            await this.loadBoardingServiceData()
            break
          case 'medical':
            await this.loadMedicalServiceData()
            break
          case 'grooming':
            await this.loadGroomingServiceData()
            break
          case 'adoption':
            await this.loadAdoptionServiceData()
            break
          default:
            uni.showToast({
              title: '服务类型错误',
              icon: 'none'
            })
        }
      } catch (error) {
        console.error('加载服务数据失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      }
    },

    async loadLitterServiceData() {
      try {
        // 从后端API获取铲屎服务数据
        const response = await this.$api.getLitterServicePage({
          current: 1,
          size: 10,
          status: 'active'
        })
        
        if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
          const service = response.data.records[0] // 取第一个服务作为详情
          this.serviceData = {
            id: service.id,
            name: service.name,
            price: service.price,
            bannerImage: service.imageUrl || '/static/images/banner1.jpg',
            introduction: this.parseDescriptionToList(service.description),
            instructions: [
              '购买后凭电子券码预约使用',
              '需提前2小时电话预约',
              '节假日价格可能浮动，详情请咨询客服'
            ]
          }
        } else {
          // 如果API没有数据，使用默认数据
          this.loadDefaultLitterServiceData()
        }
      } catch (error) {
        console.error('加载铲屎服务数据失败:', error)
        // 出错时使用默认数据
        this.loadDefaultLitterServiceData()
      }
    },

    loadDefaultLitterServiceData() {
      this.serviceData = {
        id: 1,
        name: '上门铲屎服务',
        price: 50,
        bannerImage: '/static/images/banner1.jpg',
        introduction: [
          '适用宠物: 猫咪、狗狗',
          '适用宠物性别: 公母均可',
          '服务范围: 室内宠物生活区域清洁',
          '服务时长: 30-60分钟',
          '包含内容: 清理猫砂盆、清洁宠物用品、除味处理',
          '服务时间: 每日9:00-18:00',
          '需要提前2小时预约',
          '特殊要求请提前说明',
          '服务完成后提供清洁报告'
        ],
        instructions: [
          '购买后凭电子券码预约使用',
          '需提前2小时电话预约',
          '节假日价格可能浮动，详情请咨询客服'
        ]
      }
    },

    async loadBoardingServiceData() {
      try {
        // 从后端API获取寄养服务数据
        const response = await this.$api.getBoardingServicePage({
          current: 1,
          size: 10,
          status: 'active'
        })
        
        if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
          const service = response.data.records[0] // 取第一个服务作为详情
          this.serviceData = {
            id: service.id,
            name: service.name,
            price: service.price,
            bannerImage: service.imageUrl || '/static/images/banner2.jpg',
            introduction: this.parseDescriptionToList(service.description),
            instructions: [
              '购买后凭电子券码预约使用',
              '需提前一天预约',
              '请提供宠物健康证明和疫苗接种记录'
            ]
          }
        } else {
          // 如果API没有数据，使用默认数据
          this.loadDefaultBoardingServiceData()
        }
      } catch (error) {
        console.error('加载寄养服务数据失败:', error)
        // 出错时使用默认数据
        this.loadDefaultBoardingServiceData()
      }
    },

    loadDefaultBoardingServiceData() {
      this.serviceData = {
        id: 2,
        name: '宠物寄养服务',
        price: 100,
        bannerImage: '/static/images/banner2.jpg',
        introduction: [
          '适用宠物: 猫咪、狗狗',
          '适用宠物性别: 公母均可',
          '寄养环境: 温馨舒适的独立空间',
          '寄养时长: 按天计算',
          '包含内容: 日常喂养、遛狗、清洁、陪伴',
          '专业护理: 24小时监控，专业护理人员',
          '健康保障: 定期健康检查',
          '个性化服务: 根据宠物习惯定制服务',
          '安全保障: 宠物保险覆盖'
        ],
        instructions: [
          '购买后凭电子券码预约使用',
          '需提前一天预约',
          '请提供宠物健康证明和疫苗接种记录'
        ]
      }
    },

    async loadMedicalServiceData() {
      try {
        // 从后端API获取医疗服务数据
        const response = await this.$api.getMedicalServicePage({
          current: 1,
          size: 10,
          status: 'active'
        })
        
        if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
          const service = response.data.records[0] // 取第一个服务作为详情
          this.serviceData = {
            id: service.id,
            name: service.name,
            price: service.price,
            bannerImage: service.imageUrl || '/static/images/banner3.jpg',
            introduction: this.parseDescriptionToList(service.description),
            instructions: [
              '购买后凭电子券码预约使用',
              '需提前一天预约',
              '请携带宠物健康档案'
            ]
          }
        } else {
          // 如果API没有数据，使用默认数据
          this.loadDefaultMedicalServiceData()
        }
      } catch (error) {
        console.error('加载医疗服务数据失败:', error)
        // 出错时使用默认数据
        this.loadDefaultMedicalServiceData()
      }
    },

    loadDefaultMedicalServiceData() {
      this.serviceData = {
        id: 3,
        name: '宠物医疗服务',
        price: 200,
        bannerImage: '/static/images/banner3.jpg',
        introduction: [
          '适用宠物: 猫咪、狗狗',
          '适用宠物性别: 公母均可',
          '服务内容: 健康检查、疫苗接种、疾病治疗',
          '专业医师: 持证兽医，经验丰富',
          '医疗设备: 先进医疗设备，精准诊断',
          '服务时间: 每日8:00-20:00',
          '急诊服务: 24小时急诊热线',
          '药品供应: 宠物专用药品齐全',
          '后续跟踪: 治疗后定期回访'
        ],
        instructions: [
          '购买后凭电子券码预约使用',
          '需提前一天预约',
          '请携带宠物健康档案'
        ]
      }
    },

    async loadGroomingServiceData() {
      try {
        // 从后端API获取洗护服务数据
        const response = await this.$api.getGroomingServicePage({
          current: 1,
          size: 10,
          status: 'active'
        })
        
        if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
          const service = response.data.records[0] // 取第一个服务作为详情
          this.serviceData = {
            id: service.id,
            name: service.name,
            price: service.price,
            bannerImage: service.imageUrl || '/static/images/banner1.jpg',
            introduction: this.parseDescriptionToList(service.description),
            instructions: [
              '购买后凭电子券码预约使用',
              '需提前一天预约',
              '请确保宠物身体健康'
            ]
          }
        } else {
          // 如果API没有数据，使用默认数据
          this.loadDefaultGroomingServiceData()
        }
      } catch (error) {
        console.error('加载洗护服务数据失败:', error)
        // 出错时使用默认数据
        this.loadDefaultGroomingServiceData()
      }
    },

    loadDefaultGroomingServiceData() {
      this.serviceData = {
        id: 4,
        name: '宠物洗护服务',
        price: 150,
        bannerImage: '/static/images/banner1.jpg',
        introduction: [
          '适用宠物: 猫咪、狗狗',
          '适用宠物性别: 公母均可',
          '服务内容: 洗澡、吹干、梳毛、剪指甲',
          '专业用品: 宠物专用洗护用品',
          '服务时长: 60-90分钟',
          '美容师: 专业宠物美容师',
          '造型设计: 根据宠物特点设计造型',
          '健康检查: 洗护过程中进行基础检查',
          '后续护理: 提供护理建议'
        ],
        instructions: [
          '购买后凭电子券码预约使用',
          '需提前一天预约',
          '请确保宠物身体健康'
        ]
      }
    },

    async loadAdoptionServiceData() {
      try {
        // 从后端API获取领养服务数据
        const response = await this.$api.getAdoptionServicePage({
          current: 1,
          size: 10,
          status: 'active'
        })
        
        if (response.code === 0 && response.data && response.data.records && response.data.records.length > 0) {
          const service = response.data.records[0] // 取第一个服务作为详情
          this.serviceData = {
            id: service.id,
            name: service.name,
            price: service.price,
            bannerImage: service.imageUrl || '/static/images/banner2.jpg',
            introduction: this.parseDescriptionToList(service.description),
            instructions: [
              '领养完全免费',
              '需填写领养申请表',
              '通过审核后方可领养'
            ]
          }
        } else {
          // 如果API没有数据，使用默认数据
          this.loadDefaultAdoptionServiceData()
        }
      } catch (error) {
        console.error('加载领养服务数据失败:', error)
        // 出错时使用默认数据
        this.loadDefaultAdoptionServiceData()
      }
    },

    loadDefaultAdoptionServiceData() {
      this.serviceData = {
        id: 5,
        name: '宠物领养服务',
        price: 0,
        bannerImage: '/static/images/banner2.jpg',
        introduction: [
          '适用宠物: 流浪猫、流浪狗',
          '领养条件: 有爱心、有责任心、有稳定住所',
          '领养流程: 申请-审核-面谈-领养',
          '健康保障: 已绝育、已疫苗、已体检',
          '后续支持: 提供饲养指导和医疗咨询',
          '领养协议: 签署领养协议，保障宠物权益',
          '回访服务: 定期回访，确保宠物生活状况',
          '终身服务: 提供终身饲养咨询',
          '爱心传递: 让更多流浪宠物找到温暖的家'
        ],
        instructions: [
          '领养完全免费',
          '需填写领养申请表',
          '通过审核后方可领养'
        ]
      }
    },

    onBookNow() {
      // 根据服务类型跳转到不同的预约页面
      let url = ''
      switch (this.serviceType) {
        case 'litter':
          url = '/pages/appointment/book-door-cleaning'
          break
        case 'boarding':
          url = '/pages/appointment/boarding'
          break
        case 'medical':
          url = '/pages/appointment/book-hospital'
          break
        case 'grooming':
          url = `/pages/appointment/book-grooming?serviceName=${encodeURIComponent(this.service.title || '宠物洗护服务')}`
          break
        case 'adoption':
          url = '/pages/appointment/adoption'
          break
      }
      
      if (url) {
        uni.navigateTo({ url })
      } else {
        // 跳转到预约页面
        uni.navigateTo({
          url: `/pages/booking/index?serviceType=${this.serviceType}&serviceId=${this.serviceId}`
        })
      }
    },

    onBannerError(e) {
      console.log('展示图加载失败:', e)
      // 设置默认图片
      this.serviceData.bannerImage = '/static/images/default-banner.jpg'
    },

    // 处理图片URL，解决小程序HTTP协议限制问题
    getImageUrl(imageUrl) {
      return util.getImageUrl(imageUrl)
    },

    // 解析描述文本为列表
    parseDescriptionToList(description) {
      if (!description) {
        return []
      }
      
      // 如果描述包含换行符，按换行分割
      if (description.includes('\n')) {
        return description.split('\n').filter(item => item.trim())
      }
      
      // 如果描述包含分号，按分号分割
      if (description.includes(';')) {
        return description.split(';').filter(item => item.trim())
      }
      
      // 如果描述包含句号，按句号分割
      if (description.includes('。')) {
        return description.split('。').filter(item => item.trim())
      }
      
      // 否则返回单个描述
      return [description]
    }
  }
}
</script>

<style lang="scss" scoped>
.service-detail-container {
  min-height: 100vh;
  background-color: #f8f8f8;
}

.service-banner {
  width: 100%;
  height: 400rpx;
  
  .banner-image {
    width: 100%;
    height: 100%;
  }
}

.service-info {
  background-color: #ffffff;
  padding: 40rpx 30rpx;
  margin-top: -20rpx;
  border-radius: 20rpx 20rpx 0 0;
  
  .price-title {
    display: flex;
    flex-direction: column;
    margin-bottom: 40rpx;
    
    .price {
      font-size: 48rpx;
      font-weight: bold;
      color: #ff6b35;
      margin-bottom: 10rpx;
    }
    
    .title {
      font-size: 36rpx;
      font-weight: bold;
      color: #333333;
    }
  }
  
  .section {
    margin-bottom: 40rpx;
    
    .section-title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333333;
      margin-bottom: 20rpx;
    }
    
    .content-list {
      .content-item {
        display: flex;
        margin-bottom: 16rpx;
        line-height: 1.6;
        
        .item-number {
          font-size: 28rpx;
          color: #666666;
          margin-right: 8rpx;
          flex-shrink: 0;
        }
        
        .item-text {
          font-size: 28rpx;
          color: #333333;
          flex: 1;
        }
      }
    }
  }
  
  .agreement-text {
    font-size: 24rpx;
    color: #999999;
    text-align: center;
    margin: 40rpx 0 20rpx;
  }
  
  .book-button {
    width: 100%;
    height: 88rpx;
    background-color: #ff6b35;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 20rpx;
    
    .button-text {
      font-size: 32rpx;
      color: #ffffff;
      font-weight: bold;
    }
  }
}
</style>
