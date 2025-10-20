<template>
  <div class="grooming-service-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>洗护服务管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜索洗护服务..."
              clearable
              style="width: 200px; margin-right: 10px"
              @input="handleSearch"
            />
            <el-select
              v-model="selectedCategory"
              placeholder="选择分类"
              clearable
              style="width: 150px; margin-right: 10px"
              @change="handleCategoryFilter"
            >
              <el-option label="全部分类" value=""></el-option>
              <el-option label="洗护服务" value="grooming"></el-option>
              <el-option label="美容服务" value="beauty"></el-option>
            </el-select>
            <el-select
              v-model="selectedStatus"
              placeholder="选择状态"
              clearable
              style="width: 150px; margin-right: 10px"
              @change="handleStatusFilter"
            >
              <el-option label="全部状态" value=""></el-option>
              <el-option label="启用" value="active"></el-option>
              <el-option label="禁用" value="inactive"></el-option>
            </el-select>
            <el-button type="primary" @click="showAddServiceDialog">添加洗护服务</el-button>
            <el-button type="success" @click="exportServices">导出数据</el-button>
            <el-button type="info" @click="showStatisticsDialog">统计信息</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="所有洗护服务" name="all">
          <el-table 
            :data="services" 
            style="width: 100%"
            row-key="id"
            v-loading="loading"
          >
            <el-table-column prop="id" label="服务ID" width="80" />
            <el-table-column prop="name" label="服务名称" width="150" />
            <el-table-column prop="description" label="商品介绍" min-width="200" />
            <el-table-column prop="category" label="分类" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.category === 'grooming' ? 'primary' : 'success'">
                  {{ scope.row.category === 'grooming' ? '洗护服务' : '美容服务' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="scope">
                <span style="color: #ff6b35; font-weight: bold;">¥{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(分钟)" width="100" />
            <el-table-column label="服务图片" width="120">
              <template #default="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="getImageUrl(scope.row.imageUrl)"
                  :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
                  style="width: 60px; height: 60px"
                  fit="cover"
                />
                <span v-else>无图片</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
                  {{ scope.row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column prop="createdAt" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="text" @click="editService(scope.row)">编辑</el-button>
                <el-button type="text" @click="updateServiceImage(scope.row)">更换图片</el-button>
                <el-button 
                  type="text" 
                  :style="{ color: scope.row.status === 'active' ? '#e6a23c' : '#67c23a' }"
                  @click="toggleServiceStatus(scope.row)"
                >
                  {{ scope.row.status === 'active' ? '禁用' : '启用' }}
                </el-button>
                <el-button type="text" @click="deleteService(scope.row)" style="color: #f56c6c">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="洗护服务" name="grooming">
          <el-table 
            :data="groomingServices" 
            style="width: 100%"
            row-key="id"
            v-loading="loading"
          >
            <el-table-column prop="id" label="服务ID" width="80" />
            <el-table-column prop="name" label="服务名称" width="150" />
            <el-table-column prop="description" label="商品介绍" min-width="200" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="scope">
                <span style="color: #ff6b35; font-weight: bold;">¥{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(分钟)" width="100" />
            <el-table-column label="服务图片" width="120">
              <template #default="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="getImageUrl(scope.row.imageUrl)"
                  :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
                  style="width: 60px; height: 60px"
                  fit="cover"
                />
                <span v-else>无图片</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
                  {{ scope.row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="text" @click="editService(scope.row)">编辑</el-button>
                <el-button type="text" @click="updateServiceImage(scope.row)">更换图片</el-button>
                <el-button 
                  type="text" 
                  :style="{ color: scope.row.status === 'active' ? '#e6a23c' : '#67c23a' }"
                  @click="toggleServiceStatus(scope.row)"
                >
                  {{ scope.row.status === 'active' ? '禁用' : '启用' }}
                </el-button>
                <el-button type="text" @click="deleteService(scope.row)" style="color: #f56c6c">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="美容服务" name="beauty">
          <el-table 
            :data="beautyServices" 
            style="width: 100%"
            row-key="id"
            v-loading="loading"
          >
            <el-table-column prop="id" label="服务ID" width="80" />
            <el-table-column prop="name" label="服务名称" width="150" />
            <el-table-column prop="description" label="商品介绍" min-width="200" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="scope">
                <span style="color: #ff6b35; font-weight: bold;">¥{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(分钟)" width="100" />
            <el-table-column label="服务图片" width="120">
              <template #default="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="getImageUrl(scope.row.imageUrl)"
                  :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
                  style="width: 60px; height: 60px"
                  fit="cover"
                />
                <span v-else>无图片</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
                  {{ scope.row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="scope">
                <el-button type="text" @click="editService(scope.row)">编辑</el-button>
                <el-button type="text" @click="updateServiceImage(scope.row)">更换图片</el-button>
                <el-button 
                  type="text" 
                  :style="{ color: scope.row.status === 'active' ? '#e6a23c' : '#67c23a' }"
                  @click="toggleServiceStatus(scope.row)"
                >
                  {{ scope.row.status === 'active' ? '禁用' : '启用' }}
                </el-button>
                <el-button type="text" @click="deleteService(scope.row)" style="color: #f56c6c">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="服务展示图管理" name="serviceBanner">
          <div class="service-banner-management">
            <div class="banner-header">
              <h3>洗护服务展示图管理</h3>
              <p>这个图片会显示在小程序洗护服务页面的顶部横幅区域</p>
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
        
        <el-tab-pane label="洗护服务订单" name="serviceOrders">
          <div class="service-orders-management">
            <div class="orders-header">
              <h3>洗护服务订单管理</h3>
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
                            <el-tag size="small" style="margin-left: 8px;">{{ product.name.includes('洗') || product.name.includes('护') ? '洗护' : '其他' }}</el-tag>
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
      :title="isEdit ? '编辑洗护服务' : '添加洗护服务'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="serviceFormRules"
        label-width="100px"
      >
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="serviceForm.name" placeholder="请输入服务名称" />
        </el-form-item>
        
        <el-form-item label="商品介绍" prop="description">
          <el-input
            v-model="serviceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品介绍"
          />
        </el-form-item>
        
        <el-form-item label="服务分类" prop="category">
          <el-select v-model="serviceForm.category" placeholder="请选择服务分类" style="width: 100%">
            <el-option label="洗护服务" value="grooming" />
            <el-option label="美容服务" value="beauty" />
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
            placeholder="请输入服务时长(分钟)"
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
            v-model="serviceForm.tags"
            multiple
            filterable
            allow-create
            placeholder="请选择或输入服务标签"
            style="width: 100%"
          >
            <el-option label="洗护" value="洗护" />
            <el-option label="美容" value="美容" />
            <el-option label="造型" value="造型" />
            <el-option label="护理" value="护理" />
            <el-option label="清洁" value="清洁" />
            <el-option label="SPA" value="SPA" />
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
      title="更新洗护服务展示图"
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
        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.groomingServices }}</div>
                <div class="stat-label">洗护服务</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="stat-card">
              <div class="stat-item">
                <div class="stat-value">{{ statistics.beautyServices }}</div>
                <div class="stat-label">美容服务</div>
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
  name: 'GroomingServiceManagement',
  components: {
    UploadFilled
  },
  setup() {
    const loading = ref(false)
    const services = ref([])
    const searchText = ref('')
    const selectedCategory = ref('')
    const selectedStatus = ref('')
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const activeTab = ref('all')

    // 订单相关数据
    const serviceOrders = ref([])
    const orderSearchText = ref('')
    const orderFilterStatus = ref('')
    const orderCurrentPage = ref(1)
    const orderPageSize = ref(10)
    const orderTotal = ref(0)
    const ordersLoading = ref(false)

    // 对话框状态
    const serviceDialogVisible = ref(false)
    const imageDialogVisible = ref(false)
    const bannerDialogVisible = ref(false)
    const statisticsDialogVisible = ref(false)
    const isEdit = ref(false)
    const currentService = ref({})
    const serviceBannerImage = ref('')

    // 表单数据
    const serviceForm = reactive({
      name: '',
      description: '',
      category: '',
      price: 0,
      duration: 60,
      bgColor: '#e3f2fd',
      sortOrder: 0,
      isRecommended: false,
      tags: []
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
      activeServices: 0,
      groomingServices: 0,
      beautyServices: 0
    })

    // 上传配置
    const uploadUrl = ref('http://localhost:8080/api/grooming-services/upload')
    const bannerUploadUrl = ref('http://localhost:8080/api/grooming-banners/upload')
    const uploadHeaders = ref({
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    })

    // 计算属性
    const groomingServices = computed(() => {
      return services.value.filter(service => service.category === 'grooming')
    })

    const beautyServices = computed(() => {
      return services.value.filter(service => service.category === 'beauty')
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
          category: selectedCategory.value,
          status: selectedStatus.value
        }
        
        const response = await api.get('/api/grooming-services/page', { params })
        if (response.code === 0) {
          services.value = response.data.records
          total.value = response.data.total
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
        const response = await api.get('/api/grooming-banners/position/grooming-page-top')
        if (response.code === 0 && response.data) {
          // 只有当图片URL存在且不是默认路径时才设置
          const imageUrl = response.data.imageUrl
          if (imageUrl && !imageUrl.includes('default-grooming-banner.jpg')) {
            serviceBannerImage.value = imageUrl
          } else {
            serviceBannerImage.value = '' // 不显示默认图片
          }
        }
      } catch (error) {
        console.error('加载服务展示图失败:', error)
        serviceBannerImage.value = '' // 出错时不显示任何图片
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

    // 分类筛选
    const handleCategoryFilter = () => {
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
      Object.assign(serviceForm, {
        name: service.name,
        description: service.description,
        category: service.category,
        price: service.price,
        duration: service.duration,
        bgColor: service.bgColor || '#e3f2fd',
        sortOrder: service.sortOrder || 0,
        isRecommended: service.isRecommended || false,
        tags: service.tags ? (typeof service.tags === 'string' ? 
          (service.tags.startsWith('[') && service.tags.endsWith(']') ? 
            (() => {
              try {
                return JSON.parse(service.tags);
              } catch (e) {
                // 如果JSON解析失败，尝试提取方括号内的内容
                const match = service.tags.match(/\[(.*)\]/);
                if (match && match[1]) {
                  return match[1].split(',').map(tag => tag.trim().replace(/['"]/g, ''));
                }
                return [service.tags];
              }
            })() : [service.tags]) : 
          service.tags) : []
      })
      currentService.value = service
      serviceDialogVisible.value = true
    }

    // 重置表单
    const resetForm = () => {
      Object.assign(serviceForm, {
        name: '',
        description: '',
        category: '',
        price: 0,
        duration: 60,
        bgColor: '#e3f2fd',
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
          response = await api.put(`/api/grooming-services/${currentService.value.id}`, data)
        } else {
          response = await api.post('/api/grooming-services', data)
        }
        
        if (response.code === 0) {
          ElMessage.success(isEdit.value ? '服务更新成功' : '服务创建成功')
          serviceDialogVisible.value = false
          loadServices()
        } else {
          ElMessage.error(response.message || '操作失败')
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
        
        const response = await api.delete(`/api/grooming-services/${service.id}`)
        if (response.code === 0) {
          ElMessage.success('服务删除成功')
          loadServices()
        } else {
          ElMessage.error(response.message || '删除失败')
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
        const response = await api.put(`/api/grooming-services/${service.id}/status?status=${newStatus}`)
        
        if (response.code === 0) {
          ElMessage.success(`服务已${newStatus === 'active' ? '启用' : '禁用'}`)
          loadServices()
        } else {
          ElMessage.error(response.message || '状态更新失败')
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
        ElMessage.error(response.message || '图片上传失败')
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
        console.log('开始加载洗护服务订单...')
        
        // 调用专门的洗护服务预约API
        const response = await api.get('/api/orders/grooming-appointments', {
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
        }
      } catch (error) {
        console.error('加载洗护服务订单失败:', error)
        ElMessage.error('加载订单失败: ' + error.message)
        
        // 不使用假数据，保持空数组
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
        
        // 这里需要调用删除展示图的API
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
        statistics.groomingServices = services.value.filter(s => s.category === 'grooming').length
        statistics.beautyServices = services.value.filter(s => s.category === 'beauty').length
        
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
      loadServices()
      loadServiceBanner()
    })

    return {
      loading,
      services,
      searchText,
      selectedCategory,
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
      groomingServices,
      beautyServices,
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
      handleCategoryFilter,
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
.grooming-service-container {
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
  color: #409eff;
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
  
  &:last-child {
    margin-bottom: 0;
  }
}
</style>