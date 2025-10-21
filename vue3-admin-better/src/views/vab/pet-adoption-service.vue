<template>
  <div class="litter-service-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>待领养宠物管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜索宠物名称..."
              clearable
              style="width: 200px; margin-right: 10px"
              @input="handleSearch"
            />
            <el-select
              v-model="selectedStatus"
              placeholder="选择状态"
              clearable
              style="width: 150px; margin-right: 10px"
              @change="handleStatusFilter"
            >
              <el-option label="全部状态" value=""></el-option>
              <el-option label="可领养" value="available"></el-option>
              <el-option label="已领养" value="adopted"></el-option>
            </el-select>
            <el-button type="primary" @click="showAddPetDialog">添加待领养宠物</el-button>
            <el-button type="success" @click="exportPets">导出数据</el-button>
            <el-button type="info" @click="showStatisticsDialog">统计信息</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="所有待领养宠物" name="all">
          <el-table 
            :data="services" 
            style="width: 100%"
            row-key="id"
            v-loading="loading"
          >
            <el-table-column prop="id" label="宠物ID" width="80" />
            <el-table-column prop="petName" label="宠物名称" width="150" />
            <el-table-column prop="breed" label="品种" width="120" />
            <el-table-column prop="age" label="年龄" width="80">
              <template #default="scope">
                {{ scope.row.age }}岁
              </template>
            </el-table-column>
            <el-table-column prop="gender" label="性别" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.gender === 'Male' ? 'primary' : 'success'">
                  {{ scope.row.gender === 'Male' ? '公' : '母' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="adoptionFee" label="领养费用" width="100">
              <template #default="scope">
                <span style="color: #ff6b35; font-weight: bold;">¥{{ scope.row.adoptionFee }}</span>
              </template>
            </el-table-column>
            <el-table-column label="宠物照片" width="120">
              <template #default="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="getImageUrl(scope.row.imageUrl)"
                  :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
                  style="width: 60px; height: 60px"
                  fit="cover"
                />
                <span v-else>无照片</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'available' ? 'success' : 'info'">
                  {{ scope.row.status === 'available' ? '可领养' : '已领养' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="所在位置" width="120" />
            <el-table-column prop="createTime" label="添加时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="text" @click="editPet(scope.row)">编辑</el-button>
                <el-button type="text" @click="updatePetImage(scope.row)">更换照片</el-button>
                <el-button 
                  type="text" 
                  :style="{ color: scope.row.status === 'available' ? '#e6a23c' : '#67c23a' }"
                  @click="togglePetStatus(scope.row)"
                >
                  {{ scope.row.status === 'available' ? '标记已领养' : '标记可领养' }}
                </el-button>
                <el-button type="text" @click="deletePet(scope.row)" style="color: #f56c6c">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="时间段管理" name="timeSlots">
          <div class="time-slots-management">
            <div class="slots-header">
              <h3>可预约时间段配置</h3>
              <el-button type="primary" @click="showAddTimeSlotDialog">添加时间段</el-button>
            </div>
            
            <el-table :data="timeSlots" style="width: 100%" v-loading="loadingSlots">
              <el-table-column prop="timeSlot" label="时间段" width="200" />
              <el-table-column prop="maxBookings" label="最大预约数" width="150" />
              <el-table-column label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.isActive ? 'success' : 'danger'">
                    {{ scope.row.isActive ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button type="text" @click="editTimeSlot(scope.row)">编辑</el-button>
                  <el-button 
                    type="text" 
                    :style="{ color: scope.row.isActive ? '#e6a23c' : '#67c23a' }"
                    @click="toggleTimeSlotStatus(scope.row)"
                  >
                    {{ scope.row.isActive ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="text" @click="deleteTimeSlot(scope.row)" style="color: #f56c6c">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="服务展示图管理" name="serviceBanner">
          <div class="service-banner-management">
            <div class="banner-header">
              <h3>领养服务展示图管理</h3>
              <p>这个图片会显示在小程序领养服务页面的顶部横幅区域</p>
            </div>
            
            <div class="current-banner" v-if="serviceBannerImage">
              <h4>当前展示图：</h4>
              <el-image
                :src="serviceBannerImage"
                style="width: 300px; height: 200px; border: 1px solid #ddd; border-radius: 8px;"
                fit="cover"
              />
              <div class="banner-actions">
                <el-button type="primary" @click="showUpdateBannerDialog">更换图片</el-button>
                <el-button type="danger" @click="removeServiceBanner">删除图片</el-button>
              </div>
            </div>
            
            <div class="no-banner" v-else>
              <el-empty description="暂无展示图">
                <el-button type="primary" @click="showUpdateBannerDialog">添加展示图</el-button>
              </el-empty>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="领养服务订单" name="serviceOrders">
          <div class="service-orders-management">
            <div class="orders-header">
              <h3>领养服务订单管理</h3>
              <div class="orders-filter">
                <el-input
                  v-model="orderSearchText"
                  placeholder="搜索订单..."
                  clearable
                  style="width: 200px; margin-right: 10px"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select 
                  v-model="orderFilterStatus" 
                  placeholder="状态筛选" 
                  style="width: 120px; margin-right: 10px"
                  @change="handleOrderStatusFilter"
                >
                  <el-option label="全部" value=""></el-option>
                  <el-option label="待付款" value="pending"></el-option>
                  <el-option label="已付款" value="paid"></el-option>
                  <el-option label="已完成" value="completed"></el-option>
                  <el-option label="已取消" value="cancelled"></el-option>
                </el-select>
                <el-button type="primary" @click="loadServiceOrders">刷新</el-button>
              </div>
            </div>
            
            <el-table 
              :data="filteredServiceOrders" 
              style="width: 100%"
              row-key="id"
              v-loading="ordersLoading"
              @row-click="viewOrderDetail"
            >
              <el-table-column prop="id" label="订单号" width="180" />
              <el-table-column prop="customer" label="客户" width="120" />
              <el-table-column prop="phone" label="联系电话" width="130" />
              <el-table-column label="服务项目" min-width="200">
                <template #default="{ row }">
                  <div v-for="product in row.products" :key="product.id" class="order-service">
                    <span>{{ product.name }}</span>
                    <el-tag size="small" style="margin-left: 8px;">领养服务</el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="totalAmount" label="订单金额" width="100">
                <template #default="scope">
                  <span style="color: #ff6b35; font-weight: bold;">¥{{ scope.row.totalAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getOrderStatusType(scope.row.status)">
                    {{ getOrderStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="下单时间" width="150" />
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button type="text" @click.stop="viewOrderDetail(scope.row)">查看详情</el-button>
                  <el-button 
                    v-if="scope.row.status === 'pending'"
                    type="text" 
                    style="color: #67c23a"
                    @click.stop="confirmOrder(scope.row)"
                  >
                    确认订单
                  </el-button>
                  <el-button 
                    v-if="scope.row.status === 'paid'"
                    type="text" 
                    style="color: #409eff"
                    @click.stop="completeOrder(scope.row)"
                  >
                    完成服务
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination-container" style="margin-top: 20px;">
              <el-pagination
                v-model:current-page="orderCurrentPage"
                v-model:page-size="orderPageSize"
                :page-sizes="[10, 20, 50]"
                :total="orderTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleOrderSizeChange"
                @current-change="handleOrderCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑服务对话框 -->
    <el-dialog
      v-model="serviceDialogVisible"
      :title="isEdit ? '编辑领养服务' : '添加领养服务'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="serviceFormRules"
        label-width="100px"
      >
        <el-form-item label="宠物名称" prop="petName">
          <el-input v-model="serviceForm.petName" placeholder="请输入宠物名称" />
        </el-form-item>
        
        <el-form-item label="品种" prop="breed">
          <el-input v-model="serviceForm.breed" placeholder="请输入宠物品种" />
        </el-form-item>
        
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="serviceForm.age" :min="0" :max="20" placeholder="请输入年龄" />
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-select v-model="serviceForm.gender" placeholder="请选择性别">
            <el-option label="公" value="Male" />
            <el-option label="母" value="Female" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="宠物描述" prop="description">
          <el-input
            v-model="serviceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入宠物描述"
          />
        </el-form-item>
        
        <el-form-item label="服务分类" prop="category">
          <el-select 
            :model-value="typeof serviceForm.category === 'string' ? serviceForm.category : 'basic'"
            @update:model-value="(value) => serviceForm.category = typeof value === 'string' ? value : 'basic'"
            placeholder="请选择服务分类" 
            style="width: 100%"
            clearable
          >
            <el-option label="基础领养服务" value="basic" />
            <el-option label="爱心领养服务" value="love" />
            <el-option label="专业领养服务" value="professional" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="服务价格" prop="price">
          <el-input-number
            v-model="serviceForm.price"
            :min="0.01"
            :precision="2"
            placeholder="请输入服务价格"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="服务时长" prop="duration">
          <el-input-number
            v-model="serviceForm.duration"
            :min="1"
            placeholder="请输入服务时长(天)"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="背景颜色" prop="bgColor">
          <el-color-picker v-model="serviceForm.bgColor" />
        </el-form-item>
        
        <el-form-item label="排序顺序" prop="sortOrder">
          <el-input-number
            v-model="serviceForm.sortOrder"
            :min="0"
            placeholder="请输入排序顺序"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="是否推荐" prop="isRecommended">
          <el-switch v-model="serviceForm.isRecommended" />
        </el-form-item>
        
        <el-form-item label="服务标签">
          <el-select
            :model-value="Array.isArray(serviceForm.tags) ? serviceForm.tags : []"
            @update:model-value="(value) => serviceForm.tags = Array.isArray(value) ? value : []"
            multiple
            filterable
            allow-create
            placeholder="请选择或输入服务标签"
            style="width: 100%"
          >
            <el-option label="领养服务" value="领养服务" />
            <el-option label="爱心传递" value="爱心传递" />
            <el-option label="专业指导" value="专业指导" />
            <el-option label="长期支持" value="长期支持" />
            <el-option label="免费服务" value="免费服务" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="serviceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveService">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 上传图片对话框 -->
    <el-dialog
      v-model="imageDialogVisible"
      title="上传服务图片"
      width="400px"
    >
      <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :data="{ id: currentService.id }"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :show-file-list="false"
        accept="image/*"
      >
        <el-button type="primary">选择图片</el-button>
      </el-upload>
      
      <div v-if="currentService.imageUrl" class="current-image">
        <p>当前图片：</p>
        <el-image
          :src="getImageUrl(currentService.imageUrl)"
          style="width: 200px; height: 200px"
          fit="cover"
        />
      </div>
    </el-dialog>

    <!-- 更新展示图对话框 -->
    <el-dialog
      v-model="bannerDialogVisible"
      title="更新领养服务展示图"
      width="500px"
    >
      <el-upload
        ref="bannerUploadRef"
        :action="bannerUploadUrl"
        :headers="uploadHeaders"
        :on-success="handleBannerUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :show-file-list="false"
        accept="image/*"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传jpg/png文件，且不超过10MB
          </div>
        </template>
      </el-upload>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog v-model="statisticsDialogVisible" title="统计信息" width="500px">
      <div class="statistics-content">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.totalServices }}</div>
                <div class="stat-label">总服务数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.activeServices }}</div>
                <div class="stat-label">启用服务</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import adoptionServiceApi from '@/api/adoption-service'
import axios from 'axios'

// 配置axios baseURL
const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('响应错误:', error)
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default {
  name: 'LitterServiceManagement',
  components: {
    UploadFilled
  },
  setup() {
    const loading = ref(false)
    const services = ref([])
    const searchText = ref('')
    const selectedStatus = ref('') // 使用空字符串而不是null
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const activeTab = ref('all')

    // 订单相关数据
    const serviceOrders = ref([])
    const orderSearchText = ref('')
    const orderFilterStatus = ref('') // 使用空字符串而不是null
    const orderCurrentPage = ref(1)
    const orderPageSize = ref(10)
    const orderTotal = ref(0)
    const ordersLoading = ref(false)

    // 对话框状态
    const serviceDialogVisible = ref(false)
    const imageDialogVisible = ref(false)
    const bannerDialogVisible = ref(false)
    const statisticsDialogVisible = ref(false)
    const timeSlotDialogVisible = ref(false)
    const isEdit = ref(false)
    const isEditSlot = ref(false)
    const currentService = ref({})
    const serviceBannerImage = ref('')
    
    // 时间段管理
    const timeSlots = ref([])
    const loadingSlots = ref(false)
    const timeSlotForm = reactive({
      id: null,
      timeSlot: '',
      maxBookings: 20,
      isActive: true
    })
    const timeSlotFormRef = ref(null)

    // 表单数据
    const serviceForm = reactive({
      name: '',
      description: '',
      category: 'basic', // 设置默认值而不是空字符串
      price: 0,
      duration: 60,
      bgColor: '#fff3e0',
      sortOrder: 0,
      isRecommended: false,
      tags: [] // 确保tags是数组
    })

    // 表单验证规则
    const serviceFormRules = {
      name: [
        { required: true, message: '请输入服务名称', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入商品介绍', trigger: 'blur' }
      ],
      category: [
        { required: true, message: '请选择服务分类', trigger: 'change' }
      ],
      price: [
        { required: true, message: '请输入服务价格', trigger: 'blur' }
      ],
      duration: [
        { required: true, message: '请输入服务时长', trigger: 'blur' }
      ]
    }

    // 统计信息
    const statistics = reactive({
      totalServices: 0,
      activeServices: 0
    })

    // 上传配置
    const uploadUrl = ref('http://localhost:8080/api/adoption-services/upload')
    const bannerUploadUrl = ref('http://localhost:8080/api/adoption-banners/upload')
    const uploadHeaders = ref({
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    })

    // 订单相关计算属性
    const filteredServiceOrders = computed(() => {
      let result = serviceOrders.value
      
      // 搜索过滤
      if (orderSearchText.value) {
        result = result.filter(order => 
          order.id.toLowerCase().includes(orderSearchText.value.toLowerCase()) ||
          order.customer.toLowerCase().includes(orderSearchText.value.toLowerCase())
        )
      }
      
      // 状态过滤
      if (orderFilterStatus.value) {
        result = result.filter(order => order.status === orderFilterStatus.value)
      }
      
      return result
    })

    // 加载服务列表
    const loadServices = async () => {
      loading.value = true
      try {
        const params = {
          pageNo: currentPage.value,
          pageSize: pageSize.value,
          name: searchText.value,
          status: selectedStatus.value
        }
        
        const response = await adoptionServiceApi.getAdoptionServicePage(params)
        if (response.code === 0) {
          // 清理数据，确保所有字段都是正确的类型
          const records = response.data.records || []
          
          console.log('原始数据:', records)
          
          services.value = records.map(service => {
            // 深度转换所有字段为基本类型
            const cleanedService = {
              id: service.id,
              name: String(service.name || ''),
              description: String(service.description || ''),
              category: String(service.category || 'basic'),
              price: Number(service.price) || 0,
              duration: Number(service.duration) || 60,
              imageUrl: String(service.imageUrl || ''),
              bgColor: String(service.bgColor || '#fff3e0'),
              sortOrder: Number(service.sortOrder) || 0,
              isRecommended: Boolean(service.isRecommended),
              tags: Array.isArray(service.tags) ? service.tags.map(t => String(t)) : 
                    (typeof service.tags === 'string' ? 
                      (service.tags.startsWith('[') && service.tags.endsWith(']') ? 
                        (() => {
                          try {
                            const parsed = JSON.parse(service.tags);
                            return Array.isArray(parsed) ? parsed.map(t => String(t)) : [String(service.tags)];
                          } catch (e) {
                            return [String(service.tags)];
                          }
                        })() : [String(service.tags)]) : []),
              status: String(service.status || 'active'),
              createTime: service.createTime,
              updateTime: service.updateTime
            }
            
            console.log('清理后的服务:', cleanedService)
            return cleanedService
          })
          
          total.value = response.data.total || 0
          console.log('加载领养服务列表成功:', services.value)
        } else {
          console.error('加载服务列表失败:', response)
          ElMessage.error('加载服务列表失败: ' + (response.message || response.msg))
        }
      } catch (error) {
        console.error('加载服务列表失败:', error)
        ElMessage.error('加载服务列表失败')
      } finally {
        loading.value = false
      }
    }

    // 加载服务展示图
    const loadServiceBanner = async () => {
      try {
        const response = await adoptionServiceApi.getServiceBanner()
        if (response.code === 0 && response.data) {
          const imageUrl = response.data.imageUrl
          if (imageUrl && !imageUrl.includes('default-adoption-banner.jpg')) {
            serviceBannerImage.value = imageUrl
          } else {
            serviceBannerImage.value = ''
          }
        }
      } catch (error) {
        console.error('加载服务展示图失败:', error)
        serviceBannerImage.value = ''
      }
    }

    // 获取图片URL
    const getImageUrl = (imageUrl) => {
      if (!imageUrl) return ''
      if (imageUrl.startsWith('http')) return imageUrl
      return `http://localhost:8080${imageUrl}`
    }

    // 搜索处理
    const handleSearch = () => {
      currentPage.value = 1
      loadServices()
    }

    // 状态筛选
    const handleStatusFilter = () => {
      currentPage.value = 1
      loadServices()
    }

    // 标签页切换
    const handleTabChange = (tab) => {
      console.log('切换到标签页:', tab)
      if (tab === 'serviceOrders') {
        loadServiceOrders()
      } else if (tab === 'serviceBanner') {
        loadServiceBanner()
      }
    }

    // 分页处理
    const handleSizeChange = (val) => {
      pageSize.value = val
      currentPage.value = 1
      loadServices()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      loadServices()
    }

    // 显示添加服务对话框
    const showAddServiceDialog = () => {
      isEdit.value = false
      resetForm()
      serviceDialogVisible.value = true
    }

    // 编辑服务
    const editService = (service) => {
      isEdit.value = true
      
      console.log('编辑服务原始数据:', service)
      
      // 深度转换所有字段为基本类型
      const cleanService = {
        name: String(service.name || ''),
        description: String(service.description || ''),
        category: String(service.category || 'basic'),
        price: Number(service.price) || 0,
        duration: Number(service.duration) || 60,
        bgColor: String(service.bgColor || '#fff3e0'),
        sortOrder: Number(service.sortOrder) || 0,
        isRecommended: Boolean(service.isRecommended),
        tags: Array.isArray(service.tags) ? service.tags.map(t => String(t)) : 
              (typeof service.tags === 'string' ? 
                (service.tags.startsWith('[') && service.tags.endsWith(']') ? 
                  (() => {
                    try {
                      const parsed = JSON.parse(service.tags);
                      return Array.isArray(parsed) ? parsed.map(t => String(t)) : [String(service.tags)];
                    } catch (e) {
                      return [String(service.tags)];
                    }
                  })() : [String(service.tags)]) : [])
      }
      
      console.log('编辑服务清理后数据:', cleanService)
      
      Object.assign(serviceForm, cleanService)
      currentService.value = service
      serviceDialogVisible.value = true
    }


    // 重置表单
    const resetForm = () => {
      Object.assign(serviceForm, {
        name: '',
        description: '',
        category: 'basic', // 设置默认值而不是空字符串
        price: 0,
        duration: 60,
        bgColor: '#fff3e0',
        sortOrder: 0,
        isRecommended: false,
        tags: []
      })
    }

    // 保存服务
    const saveService = async () => {
      try {
        const data = {
          ...serviceForm,
          bgColor: serviceForm.bgColor
        }
        
        let response
        if (isEdit.value) {
          data.id = currentService.value.id
          response = await adoptionServiceApi.updateAdoptionService(data)
        } else {
          response = await adoptionServiceApi.createAdoptionService(data)
        }
        
        if (response.code === 0) {
          ElMessage.success(isEdit.value ? '服务更新成功' : '服务创建成功')
          serviceDialogVisible.value = false
          loadServices()
        } else {
          ElMessage.error(response.message || response.msg || '操作失败')
        }
      } catch (error) {
        console.error('保存服务失败:', error)
        ElMessage.error('保存服务失败')
      }
    }

    // 删除服务
    const deleteService = async (service) => {
      try {
        await ElMessageBox.confirm(
          `确定要删除服务"${service.name}"吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        const response = await adoptionServiceApi.deleteAdoptionService(service.id)
        if (response.code === 0) {
          ElMessage.success('服务删除成功')
          loadServices()
        } else {
          ElMessage.error(response.message || response.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除服务失败:', error)
          ElMessage.error('删除服务失败')
        }
      }
    }

    // 切换服务状态
    const toggleServiceStatus = async (service) => {
      try {
        const newStatus = service.status === 'active' ? 'inactive' : 'active'
        const response = await adoptionServiceApi.updateAdoptionServiceStatus(service.id, newStatus)
        
        if (response.code === 0) {
          ElMessage.success(`服务已${newStatus === 'active' ? '启用' : '禁用'}`)
          loadServices()
        } else {
          ElMessage.error(response.message || response.msg || '状态更新失败')
        }
      } catch (error) {
        console.error('状态更新失败:', error)
        ElMessage.error('状态更新失败')
      }
    }

    // 更新服务图片
    const updateServiceImage = (service) => {
      currentService.value = service
      imageDialogVisible.value = true
    }

    // 上传前检查
    const beforeUpload = (file) => {
      const isImage = file.type.startsWith('image/')
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isImage) {
        ElMessage.error('只能上传图片文件!')
        return false
      }
      if (!isLt10M) {
        ElMessage.error('图片大小不能超过 10MB!')
        return false
      }
      return true
    }

    // 上传成功
    const handleUploadSuccess = (response) => {
      if (response.code === 0) {
        ElMessage.success('图片上传成功')
        imageDialogVisible.value = false
        loadServices()
      } else {
        ElMessage.error(response.message || response.msg || '图片上传失败')
      }
    }

    // 展示图上传成功
    const handleBannerUploadSuccess = (response) => {
      if (response.code === 0) {
        ElMessage.success('展示图上传成功')
        bannerDialogVisible.value = false
        loadServiceBanner()
      } else {
        ElMessage.error(response.msg || response.message || '展示图上传失败')
      }
    }

    // 上传失败
    const handleUploadError = (error) => {
      console.error('图片上传失败:', error)
      ElMessage.error('图片上传失败')
    }

    // 显示更新展示图对话框
    const showUpdateBannerDialog = () => {
      bannerDialogVisible.value = true
    }

    // 订单相关方法
    const loadServiceOrders = async () => {
      ordersLoading.value = true
      try {
        console.log('开始加载铲屎服务订单...')
        
        const response = await api.get('/api/orders/litter-appointments', {
          params: {
            status: orderFilterStatus.value
          }
        })
        
        console.log('API响应:', response)
        
        if (response.code === 0) {
          serviceOrders.value = response.data.orders || []
          orderTotal.value = serviceOrders.value.length
          console.log('加载的订单数据:', serviceOrders.value)
        } else {
          console.error('API返回错误:', response)
          ElMessage.error('获取订单数据失败: ' + (response.message || response.msg))
          serviceOrders.value = []
          orderTotal.value = 0
        }
      } catch (error) {
        console.error('加载铲屎服务订单失败:', error)
        // 不显示错误消息，避免用户看到技术错误
        serviceOrders.value = []
        orderTotal.value = 0
      } finally {
        ordersLoading.value = false
      }
    }

    const getOrderStatusText = (status) => {
      const statusMap = {
        'pending': '待付款',
        'paid': '已付款',
        'shipped': '已发货',
        'completed': '已完成',
        'cancelled': '已取消'
      }
      return statusMap[status] || status
    }

    const getOrderStatusType = (status) => {
      const typeMap = {
        'pending': 'warning',
        'paid': 'primary',
        'shipped': 'info',
        'completed': 'success',
        'cancelled': 'danger'
      }
      return typeMap[status] || 'info'
    }

    const viewOrderDetail = (order) => {
      ElMessage.info(`查看订单详情: ${order.id}`)
    }

    const confirmOrder = (order) => {
      ElMessage.success(`订单 ${order.id} 已确认`)
      loadServiceOrders()
    }

    const completeOrder = (order) => {
      ElMessage.success(`订单 ${order.id} 服务已完成`)
      loadServiceOrders()
    }

    const handleOrderStatusFilter = () => {
      orderCurrentPage.value = 1
      loadServiceOrders()
    }

    const handleOrderSizeChange = (val) => {
      orderPageSize.value = val
      orderCurrentPage.value = 1
    }

    const handleOrderCurrentChange = (val) => {
      orderCurrentPage.value = val
    }

    // 删除服务展示图
    const removeServiceBanner = async () => {
      try {
        await ElMessageBox.confirm(
          '确定要删除当前展示图吗？',
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
          }
        )
        
        serviceBannerImage.value = ''
        ElMessage.success('展示图删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除展示图失败:', error)
          ElMessage.error('删除展示图失败')
        }
      }
    }

    // 导出数据
    const exportServices = () => {
      ElMessage.info('导出功能开发中...')
    }

    // 显示统计信息
    const showStatisticsDialog = async () => {
      try {
        statistics.totalServices = services.value.length
        statistics.activeServices = services.value.filter(s => s.status === 'active').length
        
        statisticsDialogVisible.value = true
      } catch (error) {
        console.error('获取统计信息失败:', error)
        ElMessage.error('获取统计信息失败')
      }
    }

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }

    // 组件挂载时加载数据
    onMounted(() => {
      // 确保初始数据是正确的类型
      serviceForm.category = typeof serviceForm.category === 'string' ? serviceForm.category : 'basic'
      serviceForm.tags = Array.isArray(serviceForm.tags) ? serviceForm.tags : []
      
      loadServices()
      loadServiceBanner()
    })

    return {
      loading,
      services,
      searchText,
      selectedStatus,
      currentPage,
      pageSize,
      total,
      activeTab,
      serviceDialogVisible,
      imageDialogVisible,
      bannerDialogVisible,
      statisticsDialogVisible,
      isEdit,
      currentService,
      serviceBannerImage,
      serviceForm,
      serviceFormRules,
      statistics,
      uploadUrl,
      bannerUploadUrl,
      uploadHeaders,
      serviceOrders,
      orderSearchText,
      orderFilterStatus,
      orderCurrentPage,
      orderPageSize,
      orderTotal,
      ordersLoading,
      filteredServiceOrders,
      loadServices,
      loadServiceBanner,
      loadServiceOrders,
      getOrderStatusText,
      getOrderStatusType,
      viewOrderDetail,
      confirmOrder,
      completeOrder,
      handleOrderStatusFilter,
      handleOrderSizeChange,
      handleOrderCurrentChange,
      getImageUrl,
      handleSearch,
      handleStatusFilter,
      handleTabChange,
      handleSizeChange,
      handleCurrentChange,
      showAddServiceDialog,
      editService,
      resetForm,
      saveService,
      deleteService,
      toggleServiceStatus,
      updateServiceImage,
      beforeUpload,
      handleUploadSuccess,
      handleBannerUploadSuccess,
      handleUploadError,
      showUpdateBannerDialog,
      removeServiceBanner,
      exportServices,
      showStatisticsDialog,
      formatDate
    }
  }
}
</script>

<style scoped>
.litter-service-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.current-image {
  margin-top: 20px;
  text-align: center;
}

.current-image p {
  margin-bottom: 10px;
  font-weight: bold;
}

.statistics-content {
  padding: 20px 0;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #ff8c00;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.dialog-footer {
  text-align: right;
}

.service-banner-management {
  padding: 20px 0;
}

.banner-header {
  margin-bottom: 30px;
}

.banner-header h3 {
  margin-bottom: 10px;
  color: #333;
}

.banner-header p {
  color: #666;
  margin: 0;
}

.current-banner {
  text-align: center;
}

.current-banner h4 {
  margin-bottom: 20px;
  color: #333;
}

.banner-actions {
  margin-top: 20px;
}

.banner-actions .el-button {
  margin: 0 10px;
}

.no-banner {
  text-align: center;
  padding: 50px 0;
}

.service-orders-management {
  padding: 20px 0;
}

.orders-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.orders-header h3 {
  margin: 0;
  color: #303133;
}

.orders-filter {
  display: flex;
  align-items: center;
}

.order-service {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.order-service:last-child {
  margin-bottom: 0;
}
</style>
