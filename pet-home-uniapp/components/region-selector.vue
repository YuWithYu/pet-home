<template>
  <view class="region-selector" @click="closeSelector">
    <view class="selector-content" @click.stop>
      <view class="selector-header">
        <view class="header-left">
          <view class="back-btn" @click="handleBack">
            <text class="back-icon">←</text>
          </view>
          <text class="header-title">选择地区</text>
        </view>
        <view class="close-btn" @click="$emit('close')">
          <text class="close-icon">×</text>
        </view>
      </view>

      <view class="breadcrumb">
        <view 
          :class="['breadcrumb-item', { 'active': currentLevel === 1 }]"
          @click="switchLevel(1)"
        >
          {{ selectedRegion.province ? selectedRegion.province.name : '请选择省' }}
        </view>
        <view 
          v-if="selectedRegion.province"
          :class="['breadcrumb-item', { 'active': currentLevel === 2 }]"
          @click="switchLevel(2)"
        >
          {{ selectedRegion.city ? selectedRegion.city.name : '请选择市' }}
        </view>
        <view 
          v-if="selectedRegion.city"
          :class="['breadcrumb-item', { 'active': currentLevel === 3 }]"
          @click="switchLevel(3)"
        >
          {{ selectedRegion.district ? selectedRegion.district.name : '请选择区' }}
        </view>
        <view class="clear-btn" @click="clearSelection">
          <text class="clear-text">清空</text>
        </view>
      </view>

      <view class="region-list">
        <view 
          :class="['region-item', { 'selected': isSelected(item) }]"
          v-for="item in currentRegionList"
          :key="item.id"
          @click="selectRegion(item)"
        >
          <text class="region-name">{{ item.name }}</text>
          <text class="arrow-icon" v-if="item.level < 3">></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '@/common/js/api.js'

export default {
  name: 'RegionSelector',
  
  props: {
    selectedRegion: {
      type: Object,
      default: () => ({
        province: null,
        city: null,
        district: null
      })
    }
  },

  data() {
    return {
      currentLevel: 1,
      regionData: [],
      provinces: [],
      cities: [],
      districts: []
    }
  },

  computed: {
    currentRegionList() {
      switch (this.currentLevel) {
        case 1:
          return this.provinces
        case 2:
          return this.cities
        case 3:
          return this.districts
        default:
          return []
      }
    }
  },

  mounted() {
    this.loadRegionData()
  },

  methods: {
    handleBack() {
      if (this.currentLevel === 1) {
        this.$emit('close')
      } else if (this.currentLevel === 2) {
        this.currentLevel = 1
        this.cities = []
        this.districts = []
      } else if (this.currentLevel === 3) {
        this.currentLevel = 2
        this.districts = []
      }
    },

    closeSelector() {
      this.$emit('close')
    },

    async loadRegionData() {
      try {
        const res = await api.getProvinces()
        if (res.code === 0 || res.code === 200) {
          this.provinces = res.data || []
        } else {
          console.error('加载省份失败:', res.msg || res.message || '接口错误')
          this.loadMockData()
        }
      } catch (error) {
        console.error('加载省份异常:', error)
        this.loadMockData()
      }
    },

    loadMockData() {
      this.provinces = [
        { id: 1, code: '11', name: '北京市', level: 1, parent_code: null },
        { id: 2, code: '12', name: '天津市', level: 1, parent_code: null },
        { id: 3, code: '13', name: '河北省', level: 1, parent_code: null },
        { id: 4, code: '14', name: '山西省', level: 1, parent_code: null },
        { id: 5, code: '15', name: '内蒙古自治区', level: 1, parent_code: null },
        { id: 6, code: '21', name: '辽宁省', level: 1, parent_code: null },
        { id: 7, code: '22', name: '吉林省', level: 1, parent_code: null },
        { id: 8, code: '23', name: '黑龙江省', level: 1, parent_code: null },
        { id: 9, code: '31', name: '上海市', level: 1, parent_code: null },
        { id: 10, code: '32', name: '江苏省', level: 1, parent_code: null },
        { id: 11, code: '33', name: '浙江省', level: 1, parent_code: null },
        { id: 12, code: '34', name: '安徽省', level: 1, parent_code: null },
        { id: 13, code: '35', name: '福建省', level: 1, parent_code: null },
        { id: 14, code: '36', name: '江西省', level: 1, parent_code: null },
        { id: 15, code: '37', name: '山东省', level: 1, parent_code: null },
        { id: 16, code: '41', name: '河南省', level: 1, parent_code: null },
        { id: 17, code: '42', name: '湖北省', level: 1, parent_code: null },
        { id: 18, code: '43', name: '湖南省', level: 1, parent_code: null },
        { id: 19, code: '44', name: '广东省', level: 1, parent_code: null },
        { id: 20, code: '45', name: '广西壮族自治区', level: 1, parent_code: null }
      ]

      if (this.selectedRegion.province) {
        this.loadCities(this.selectedRegion.province.code)
      }
    },

    async loadCities(provinceCode) {
      try {
        const res = await api.getCities(provinceCode)
        if (res.code === 0 || res.code === 200) {
          this.cities = res.data || []
        } else {
          console.error('加载城市失败:', res.msg || res.message || '接口错误')
          this.cities = this.getMockCities(provinceCode)
        }
      } catch (error) {
        console.error('加载城市异常:', error)
        this.cities = this.getMockCities(provinceCode)
      }
    },

    getMockCities(provinceCode) {
      const cityData = {
        '13': [
          { id: 101, code: '1301', name: '石家庄市', level: 2, parent_code: '13' },
          { id: 102, code: '1302', name: '唐山市', level: 2, parent_code: '13' },
          { id: 103, code: '1303', name: '秦皇岛市', level: 2, parent_code: '13' },
          { id: 104, code: '1304', name: '邯郸市', level: 2, parent_code: '13' },
          { id: 105, code: '1305', name: '邢台市', level: 2, parent_code: '13' },
          { id: 106, code: '1306', name: '保定市', level: 2, parent_code: '13' },
          { id: 107, code: '1307', name: '张家口市', level: 2, parent_code: '13' },
          { id: 108, code: '1308', name: '承德市', level: 2, parent_code: '13' }
        ],
        '45': [
          { id: 201, code: '4501', name: '南宁市', level: 2, parent_code: '45' },
          { id: 202, code: '4502', name: '柳州市', level: 2, parent_code: '45' },
          { id: 203, code: '4503', name: '桂林市', level: 2, parent_code: '45' },
          { id: 204, code: '4504', name: '梧州市', level: 2, parent_code: '45' },
          { id: 205, code: '4505', name: '北海市', level: 2, parent_code: '45' },
          { id: 206, code: '4506', name: '防城港市', level: 2, parent_code: '45' },
          { id: 207, code: '4507', name: '钦州市', level: 2, parent_code: '45' },
          { id: 208, code: '4508', name: '贵港市', level: 2, parent_code: '45' }
        ]
      }
      return cityData[provinceCode] || []
    },

    async loadDistricts(cityCode) {
      try {
        const res = await api.getDistricts(cityCode)
        if (res.code === 0 || res.code === 200) {
          this.districts = res.data || []
        } else {
          console.error('加载区县失败:', res.msg || res.message || '接口错误')
          this.districts = this.getMockDistricts(cityCode)
        }
      } catch (error) {
        console.error('加载区县异常:', error)
        this.districts = this.getMockDistricts(cityCode)
      }
    },

    getMockDistricts(cityCode) {
      const districtData = {
        '1306': [
          { id: 301, code: '130602', name: '竞秀区', level: 3, parent_code: '1306' },
          { id: 302, code: '130606', name: '莲池区', level: 3, parent_code: '1306' },
          { id: 303, code: '130607', name: '满城区', level: 3, parent_code: '1306' },
          { id: 304, code: '130608', name: '清苑区', level: 3, parent_code: '1306' },
          { id: 305, code: '130609', name: '徐水区', level: 3, parent_code: '1306' }
        ],
        '4508': [
          { id: 401, code: '450802', name: '港北区', level: 3, parent_code: '4508' },
          { id: 402, code: '450803', name: '港南区', level: 3, parent_code: '4508' },
          { id: 403, code: '450804', name: '覃塘区', level: 3, parent_code: '4508' },
          { id: 404, code: '450821', name: '平南县', level: 3, parent_code: '4508' },
          { id: 405, code: '450881', name: '桂平市', level: 3, parent_code: '4508' }
        ]
      }
      return districtData[cityCode] || []
    },

    switchLevel(level) {
      this.currentLevel = level
    },

    selectRegion(item) {
      if (item.level === 1) {
        this.selectedRegion.province = item
        this.selectedRegion.city = null
        this.selectedRegion.district = null
        this.currentLevel = 2
        this.loadCities(item.code)
      } else if (item.level === 2) {
        this.selectedRegion.city = item
        this.selectedRegion.district = null
        this.currentLevel = 3
        this.loadDistricts(item.code)
      } else if (item.level === 3) {
        this.selectedRegion.district = item
        this.confirmSelection()
      }
    },

    isSelected(item) {
      switch (item.level) {
        case 1:
          return this.selectedRegion.province && this.selectedRegion.province.id === item.id
        case 2:
          return this.selectedRegion.city && this.selectedRegion.city.id === item.id
        case 3:
          return this.selectedRegion.district && this.selectedRegion.district.id === item.id
        default:
          return false
      }
    },

    clearSelection() {
      this.selectedRegion.province = null
      this.selectedRegion.city = null
      this.selectedRegion.district = null
      this.currentLevel = 1
      this.cities = []
      this.districts = []
    },

    confirmSelection() {
      if (this.selectedRegion.province && this.selectedRegion.city && this.selectedRegion.district) {
        this.$emit('confirm', this.selectedRegion)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.region-selector {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 9999;
}

.selector-content {
  width: 100%;
  height: 80vh;
  background-color: white;
  position: absolute;
  bottom: 0;
  border-radius: 20rpx 20rpx 0 0;
  display: flex;
  flex-direction: column;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.back-btn {
  padding: 8rpx 16rpx;
  margin: -8rpx 0 -8rpx -16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 36rpx;
  font-weight: 600;
  color: #333;
}

.header-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.close-btn {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  font-size: 32rpx;
  color: #999;
}

.breadcrumb {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
  gap: 16rpx;
}

.breadcrumb-item {
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  font-size: 24rpx;
  color: #666;
  background-color: #f8f8f8;

  &.active {
    color: #ff4444;
    background-color: #fff7f7;
  }
}

.clear-btn {
  margin-left: auto;
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-text {
  font-size: 24rpx;
  color: #999;
}

.region-list {
  flex: 1;
  overflow-y: auto;
}

.region-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  &.selected {
    background-color: #fff7f7;
  }
}

.region-name {
  font-size: 28rpx;
  color: #333;
}

.arrow-icon {
  font-size: 24rpx;
  color: #999;
}
</style>
