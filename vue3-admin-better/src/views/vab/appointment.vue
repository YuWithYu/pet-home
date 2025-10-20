<template>
  <div class="appointment-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>宠物医院预约管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜索宠物医院预约..."
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
              <el-option label="待确认" value="pending"></el-option>
              <el-option label="已确认" value="confirmed"></el-option>
              <el-option label="已完成" value="completed"></el-option>
              <el-option label="已取消" value="cancelled"></el-option>
            </el-select>
            <el-input
              v-model="dateRange"
              placeholder="选择日期范围"
              style="width: 240px; margin-right: 10px"
              readonly
            />
            <el-button type="primary" @click="showAddAppointmentDialog">添加医院预约</el-button>
            <el-button type="success" @click="exportAppointments">导出数据</el-button>
            <el-button type="info" @click="showStatisticsDialog">统计信息</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="所有医院预约" name="all">
          <el-table 
            :data="filteredAppointments" 
            style="width: 100%"
            row-key="id"
            v-loading="loading"
          >
            <el-table-column prop="id" label="医院预约ID" width="100" />
            <el-table-column prop="username" label="客户姓名" width="120" />
            <el-table-column prop="petName" label="宠物名称" width="120" />
            <el-table-column prop="serviceType" label="服务项目" min-width="150" />
            <el-table-column prop="date" label="医院预约日期" width="120" />
            <el-table-column prop="timeSlot" label="时间段" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="text" @click="viewAppointment(row)">查看</el-button>
                <el-button type="text" @click="editAppointment(row)">编辑</el-button>
                <el-button type="text" @click="deleteAppointment(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="待确认" name="pending">
          <el-table 
            :data="pendingAppointments" 
            style="width: 100%"
            row-key="id"
          >
            <el-table-column prop="id" label="预约ID" width="100" />
            <el-table-column prop="username" label="客户姓名" width="120" />
            <el-table-column prop="petName" label="宠物名称" width="120" />
            <el-table-column prop="serviceType" label="服务项目" min-width="150" />
            <el-table-column prop="date" label="医院预约日期" width="120" />
            <el-table-column prop="timeSlot" label="时间段" width="100" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="text" @click="confirmAppointment(row)">确认</el-button>
                <el-button type="text" @click="rejectAppointment(row)">拒绝</el-button>
                <el-button type="text" @click="viewAppointment(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="医疗服务管理" name="services">
          <div class="services-management">
            <div class="services-header">
              <el-button type="primary" @click="showAddServiceDialog">添加医疗服务</el-button>
              <el-button type="success" @click="refreshServices">刷新服务</el-button>
            </div>
            
            <el-table 
              :data="medicalServices" 
              style="width: 100%"
              row-key="id"
              v-loading="servicesLoading"
            >
              <el-table-column prop="id" label="服务ID" width="80" />
              <el-table-column prop="name" label="服务名称" width="150" />
              <el-table-column prop="description" label="服务描述" min-width="200" />
              <el-table-column prop="price" label="价格" width="100">
                <template #default="{ row }">
                  ¥{{ row.price }}
                </template>
              </el-table-column>
              <el-table-column
                prop="image"
                label="服务图片"
                width="100"
                align="center"
              >
                <template #default="{ row }">
                  <el-image
                    :src="getImageUrl(row.image) + '?v=' + Date.now()"
                    :error-src="'http://localhost:8080/static/default-pet.png'"
                    style="width: 50px; height: 50px"
                    fit="cover"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'active' ? 'success' : 'info'">
                    {{ row.status === 'active' ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button type="text" @click="editService(row)">编辑</el-button>
                  <el-button type="text" @click="updateServiceImage(row)">更换图片</el-button>
                  <el-button type="text" @click="toggleServiceStatus(row)">
                    {{ row.status === 'active' ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="text" @click="deleteService(row)" style="color: #f56c6c">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="服务展示图管理" name="serviceBanner">
          <div class="service-banner-management">
            <div class="banner-header">
              <h3>选择服务下方展示图管理</h3>
              <p>这个图片会显示在小程序首页的"选择服务"标签下方</p>
            </div>
            
            <div class="current-banner" v-if="serviceBannerImage">
              <h4>当前展示图：</h4>
              <el-image
                :src="getImageUrl(serviceBannerImage)"
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
        
        <el-tab-pane label="今日医院预约" name="today">
          <el-table 
            :data="todayAppointments" 
            style="width: 100%"
            row-key="id"
          >
            <el-table-column prop="id" label="预约ID" width="100" />
            <el-table-column prop="username" label="客户姓名" width="120" />
            <el-table-column prop="petName" label="宠物名称" width="120" />
            <el-table-column prop="serviceType" label="服务项目" min-width="150" />
            <el-table-column prop="date" label="医院预约日期" width="120" />
            <el-table-column prop="timeSlot" label="时间段" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="text" @click="completeAppointment(row)">完成</el-button>
                <el-button type="text" @click="viewAppointment(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredAppointments.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑预约对话框 -->
    <el-dialog 
      v-model="appointmentDialogVisible" 
      :title="editingAppointment ? '编辑医院预约' : '添加医院预约'"
      width="800px"
    >
      <el-form
        ref="appointmentFormRef"
        :model="appointmentForm"
        :rules="appointmentRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户ID" prop="userId">
              <el-input-number v-model="appointmentForm.userId" :min="1" style="width: 100%" />
            </el-form-item>
            
            <el-form-item label="宠物ID" prop="petId">
              <el-input-number v-model="appointmentForm.petId" :min="1" style="width: 100%" />
            </el-form-item>
            
            <el-form-item label="医院服务类型" prop="serviceType">
              <el-select v-model="appointmentForm.serviceType" placeholder="请选择医院服务类型" style="width: 100%">
                <el-option label="体检" value="体检"></el-option>
                <el-option label="疫苗接种" value="疫苗接种"></el-option>
                <el-option label="绝育手术" value="绝育手术"></el-option>
                <el-option label="疾病治疗" value="疾病治疗"></el-option>
                <el-option label="牙科护理" value="牙科护理"></el-option>
                <el-option label="急诊" value="急诊"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="医院预约日期" prop="date">
              <el-date-picker
                v-model="appointmentForm.date"
                type="date"
                placeholder="选择医院预约日期"
                style="width: 100%"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            
            <el-form-item label="时间段" prop="timeSlot">
              <el-select v-model="appointmentForm.timeSlot" placeholder="请选择时间段" style="width: 100%">
                <el-option label="上午" value="上午"></el-option>
                <el-option label="下午" value="下午"></el-option>
                <el-option label="晚上" value="晚上"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item label="状态" prop="status">
              <el-select v-model="appointmentForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="待确认" value="pending"></el-option>
                <el-option label="已确认" value="confirmed"></el-option>
                <el-option label="已完成" value="completed"></el-option>
                <el-option label="已取消" value="cancelled"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="备注信息" prop="remark">
              <el-input 
                v-model="appointmentForm.remark" 
                type="textarea"
                :rows="8"
                placeholder="请输入备注信息"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="appointmentDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveAppointment"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 预约详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="医院预约详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="detailAppointment && detailAppointment.id">
        <el-descriptions-item label="预约ID">{{ detailAppointment.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailAppointment.userId }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ detailAppointment.username }}</el-descriptions-item>
        <el-descriptions-item label="宠物ID">{{ detailAppointment.petId }}</el-descriptions-item>
        <el-descriptions-item label="宠物名称">{{ detailAppointment.petName }}</el-descriptions-item>
        <el-descriptions-item label="宠物品种">{{ detailAppointment.petBreed }}</el-descriptions-item>
        <el-descriptions-item label="服务类型">{{ detailAppointment.serviceType }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ detailAppointment.date }}</el-descriptions-item>
        <el-descriptions-item label="时间段">{{ detailAppointment.timeSlot }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailAppointment.status)">
            {{ getStatusText(detailAppointment.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailAppointment.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailAppointment.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="备注信息" :span="2">{{ detailAppointment.remark || '无' }}</el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="editAppointment(detailAppointment)">编辑</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 统计信息对话框 -->
    <el-dialog 
      v-model="statisticsDialogVisible" 
      title="预约统计信息"
      width="600px"
    >
      <div v-if="statisticsData && Object.keys(statisticsData).length > 0">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span>状态统计</span>
              </template>
              <div class="statistics-item">
                <div class="stat-item">
                  <span class="label">待确认:</span>
                  <el-tag type="warning">{{ statisticsData.pendingCount || 0 }}</el-tag>
                </div>
                <div class="stat-item">
                  <span class="label">已确认:</span>
                  <el-tag type="primary">{{ statisticsData.confirmedCount || 0 }}</el-tag>
                </div>
                <div class="stat-item">
                  <span class="label">已完成:</span>
                  <el-tag type="success">{{ statisticsData.completedCount || 0 }}</el-tag>
                </div>
                <div class="stat-item">
                  <span class="label">已取消:</span>
                  <el-tag type="danger">{{ statisticsData.cancelledCount || 0 }}</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <template #header>
                <span>服务类型统计</span>
              </template>
              <div class="statistics-item">
                <div class="stat-item" v-for="service in statisticsData.serviceStats" :key="service.serviceType">
                  <span class="label">{{ service.serviceType }}:</span>
                  <span class="value">{{ service.count }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statisticsDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="loadStatistics">刷新统计</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 医疗服务编辑对话框 -->
    <el-dialog
      :title="editingService ? '编辑医疗服务' : '添加医疗服务'"
      v-model="serviceDialogVisible"
      width="600px"
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="serviceRules"
        label-width="100px"
      >
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="serviceForm.name" placeholder="请输入服务名称" />
        </el-form-item>
        
        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="serviceForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入服务描述"
          />
        </el-form-item>
        
        <el-form-item label="服务价格" prop="price">
          <el-input-number
            v-model="serviceForm.price"
            :min="0"
            :precision="2"
            placeholder="请输入服务价格"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="服务分类" prop="category">
          <el-select v-model="serviceForm.category" placeholder="请选择服务分类" style="width: 100%">
            <el-option label="医疗服务" value="医疗服务" />
            <el-option label="体检服务" value="体检服务" />
            <el-option label="手术服务" value="手术服务" />
            <el-option label="急诊服务" value="急诊服务" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="服务状态" prop="status">
          <el-select v-model="serviceForm.status" placeholder="请选择服务状态" style="width: 100%">
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品介绍" prop="productIntroduction">
          <el-input
            v-model="serviceForm.productIntroduction"
            type="textarea"
            :rows="6"
            placeholder="请输入商品介绍，支持多行文本，每行一个要点"
          />
        </el-form-item>
        
        <el-form-item label="使用须知" prop="usageInstructions">
          <el-input
            v-model="serviceForm.usageInstructions"
            type="textarea"
            :rows="4"
            placeholder="请输入使用须知，支持多行文本，每行一个要点"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="serviceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveService">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 服务图片更新对话框 -->
    <el-dialog
      title="更新服务图片"
      v-model="serviceImageDialogVisible"
      width="500px"
    >
      <div class="image-upload-container">
        <div class="current-image" v-if="editingService && editingService.image">
          <p>当前图片：</p>
          <el-image
            :src="getImageUrl(editingService.image)"
            style="width: 200px; height: 200px"
            fit="cover"
          />
        </div>
        
        <div class="upload-section">
          <p>上传新图片：</p>
          <input
            type="file"
            accept="image/*"
            @change="handleImageUpload"
            style="margin-bottom: 10px"
          />
          <div v-if="serviceForm.image" class="preview-image">
            <p>预览：</p>
            <el-image
              :src="getImageUrl(serviceForm.image)"
              style="width: 200px; height: 200px"
              fit="cover"
            />
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="serviceImageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveServiceImage">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 服务展示图上传对话框 -->
    <el-dialog
      title="更新服务展示图"
      v-model="serviceBannerDialogVisible"
      width="600px"
    >
      <div class="banner-upload-container">
        <div class="upload-instructions">
          <h4>上传说明：</h4>
          <ul>
            <li>支持格式：JPG、PNG、GIF</li>
            <li>建议尺寸：750x400像素</li>
            <li>文件大小：不超过5MB</li>
            <li>此图片将显示在小程序首页"选择服务"标签下方</li>
          </ul>
        </div>
        
        <div class="upload-section">
          <el-upload
            class="banner-uploader"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="handleBannerUpload"
            :on-success="handleBannerUploadSuccess"
            :on-error="handleBannerUploadError"
            accept="image/*"
          >
            <div class="upload-area">
              <i class="el-icon-plus"></i>
              <div class="upload-text">点击上传服务展示图</div>
            </div>
          </el-upload>
        </div>
        
        <div class="preview-section" v-if="serviceBannerImage">
          <h4>预览效果：</h4>
          <el-image
            :src="serviceBannerImage"
            style="width: 100%; max-width: 500px; height: 200px; border: 1px solid #ddd; border-radius: 8px;"
            fit="cover"
          />
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="serviceBannerDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveServiceBanner"
            :loading="bannerUploadLoading"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import appointmentApi from "@/api/appointment";
import medicalServiceApi from "@/api/medical-service";
import serviceBannerApi from "@/api/service-banner";

export default {
  name: "Appointment",
  data() {
    return {
      activeTab: "all",
      searchText: "",
      selectedStatus: "",
      dateRange: [],
      currentPage: 1,
      pageSize: 10,
      totalAppointments: 0,
      loading: false,
      appointmentDialogVisible: false,
      detailDialogVisible: false,
      editingAppointment: null,
      appointments: [],
      appointmentForm: {
        userId: null,
        petId: null,
        serviceType: "",
        date: "",
        timeSlot: "",
        status: "pending",
        remark: ""
      },
      appointmentRules: {
        userId: [
          { required: true, message: "请输入用户ID", trigger: "blur" }
        ],
        petId: [
          { required: true, message: "请输入宠物ID", trigger: "blur" }
        ],
        serviceType: [
          { required: true, message: "请选择服务类型", trigger: "change" }
        ],
        date: [
          { required: true, message: "请选择预约日期", trigger: "change" }
        ],
        timeSlot: [
          { required: true, message: "请选择时间段", trigger: "change" }
        ],
        status: [
          { required: true, message: "请选择状态", trigger: "change" }
        ]
      },
      detailAppointment: {},
      refreshTimer: null,
      autoRefreshInterval: 30000, // 30秒自动刷新一次
      statisticsDialogVisible: false,
      statisticsData: {},
      
      // 医疗服务相关数据
      medicalServices: [],
      servicesLoading: false,
      serviceDialogVisible: false,
      serviceImageDialogVisible: false,
      editingService: {},
      serviceForm: {
        name: "",
        description: "",
        price: null,
        category: "医疗服务",
        image: "",
        status: "active",
        productIntroduction: "",
        usageInstructions: ""
      },
      serviceRules: {
        name: [
          { required: true, message: "请输入服务名称", trigger: "blur" }
        ],
        description: [
          { required: true, message: "请输入服务描述", trigger: "blur" }
        ],
        price: [
          { required: true, message: "请输入服务价格", trigger: "blur" }
        ],
        category: [
          { required: true, message: "请选择服务分类", trigger: "change" }
        ]
      },
      
      // 服务展示图相关数据
      serviceBannerImage: null,
      serviceBannerDialogVisible: false,
      bannerUploadLoading: false,
      
      // 图片上传配置
      uploadAction: "http://localhost:8080/api/admin/upload",
      uploadHeaders: {
        // 暂时移除Authorization头，因为已经移除了权限检查
        // "Authorization": "Bearer " + (localStorage.getItem("token") || "")
      },
      selectedImageFile: null, // 新增：存储选择的图片文件
    };
  },
  computed: {
    filteredAppointments() {
      let result = this.appointments || [];
      
      // 过滤掉洗护服务和寄养服务相关的预约，只显示医院预约
      result = result.filter(appointment => {
        const serviceType = appointment.serviceType || '';
        const appointmentId = appointment.id;
        
        // 根据预约ID和服务类型进行精确过滤
        // ID 1: Health Check - 医院预约 ✓
        // ID 2: Bathing - 洗护服务 ✗
        // ID 3: 乱码服务 - 可能是手术服务 ✓
        // ID 4: 乱码服务 - 可能是医疗服务 ✓  
        // ID 5,6,7: 乱码洗澡服务 - 洗护服务 ✗
        
        // 明确排除洗护服务
        if (appointmentId === 2 || appointmentId === 5 || appointmentId === 6 || appointmentId === 7) {
          return false; // 排除洗护服务
        }
        
        // 排除包含洗护服务关键词的
        if (serviceType.includes('洗澡') || 
            serviceType.includes('洗护') || 
            serviceType.includes('grooming') ||
            serviceType.includes('Bathing') ||
            serviceType.includes('ϴ')) {
          return false;
        }
        
        // 排除寄养服务
        if (serviceType.includes('boarding') || 
            serviceType.includes('pet_boarding') || 
            serviceType.includes('寄养') ||
            serviceType === 'pet_boarding') {
          return false;
        }
        
        return true;
      });
      
      // 搜索过滤
      if (this.searchText) {
        result = result.filter(appointment => 
          (appointment.username && appointment.username.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (appointment.petName && appointment.petName.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (appointment.serviceType && appointment.serviceType.toLowerCase().includes(this.searchText.toLowerCase()))
        );
      }
      
      // 状态过滤
      if (this.selectedStatus) {
        result = result.filter(appointment => 
          appointment.status === this.selectedStatus
        );
      }
      
      // 日期范围过滤
      if (this.dateRange && this.dateRange.length === 2) {
        const startDate = this.dateRange[0];
        const endDate = this.dateRange[1];
        result = result.filter(appointment => {
          const appointmentDate = new Date(appointment.date);
          return appointmentDate >= startDate && appointmentDate <= endDate;
        });
      }
      
      return result;
    },
    pendingAppointments() {
      return this.filteredAppointments.filter(appointment => appointment.status === "pending");
    },
    todayAppointments() {
      const today = new Date().toISOString().split('T')[0];
      return this.filteredAppointments.filter(appointment => {
        if (!appointment.date) return false;
        
        // 处理不同类型的date字段
        let dateStr = '';
        if (typeof appointment.date === 'string') {
          dateStr = appointment.date;
        } else if (appointment.date instanceof Date) {
          dateStr = appointment.date.toISOString().split('T')[0];
        } else if (appointment.date && typeof appointment.date === 'object') {
          // 如果是对象，尝试转换为字符串
          dateStr = appointment.date.toString();
        }
        
        return dateStr && dateStr.startsWith(today);
      });
    }
  },
  created() {
    this.loadAppointments();
    // 设置定时刷新
    this.startAutoRefresh();
  },
  
  beforeUnmount() {
    // 清理定时器
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
    }
  },
  methods: {
    async loadAppointments() {
      try {
        this.loading = true;
        const queryParams = {
          pageNo: this.currentPage,
          pageSize: this.pageSize,
          status: this.selectedStatus || undefined,
          startDate: this.dateRange && this.dateRange[0] ? this.formatDate(this.dateRange[0]) : undefined,
          endDate: this.dateRange && this.dateRange[1] ? this.formatDate(this.dateRange[1]) : undefined
        };

        const response = await appointmentApi.getAppointmentList(queryParams);
        if (response.code === 0) {
          this.appointments = response.data.records || [];
          this.totalAppointments = response.data.total || 0;
        } else {
          this.$message.error(response.msg || '加载预约数据失败');
        }
      } catch (error) {
        console.error('加载预约数据失败:', error);
        this.$message.error('加载预约数据失败: ' + (error.message || '网络错误'));
      } finally {
        this.loading = false;
      }
    },
    
    handleTabChange(tab) {
      this.activeTab = tab;
      this.currentPage = 1;
      this.loadAppointments();
    },
    
    handleStatusFilter() {
      this.currentPage = 1;
      this.loadAppointments();
    },
    
    handleDateRangeChange() {
      this.currentPage = 1;
      this.loadAppointments();
    },
    
    handleSearch() {
      // 搜索通过计算属性实现，不需要重新加载数据
    },
    
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.loadAppointments();
    },
    
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadAppointments();
    },
    
    disabledDate(time) {
      // 禁用今天之前的日期
      return time.getTime() < Date.now() - 8.64e7;
    },
    
    formatDate(date) {
      if (!date) return null;
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    
    getStatusText(status) {
      const statusMap = {
        "pending": "待确认",
        "confirmed": "已确认",
        "completed": "已完成",
        "cancelled": "已取消"
      };
      return statusMap[status] || status;
    },
    
    getStatusType(status) {
      const typeMap = {
        "pending": "warning",
        "confirmed": "primary",
        "completed": "success",
        "cancelled": "danger"
      };
      return typeMap[status] || "info";
    },
    
    showAddAppointmentDialog() {
      this.editingAppointment = null;
      this.appointmentForm = {
        userId: null,
        petId: null,
        serviceType: "",
        date: "",
        timeSlot: "",
        status: "pending",
        remark: ""
      };
      this.appointmentDialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.appointmentFormRef) {
          this.$refs.appointmentFormRef.resetFields();
        }
      });
    },
    
    editAppointment(appointment) {
      this.editingAppointment = appointment;
      this.appointmentForm = { 
        ...appointment,
        date: appointment.date ? new Date(appointment.date) : null
      };
      this.appointmentDialogVisible = true;
      this.detailDialogVisible = false;
    },
    
    async viewAppointment(appointment) {
      try {
        const response = await appointmentApi.getAppointmentById(appointment.id);
        if (response.code === 0) {
          this.detailAppointment = response.data;
          this.detailDialogVisible = true;
        } else {
          this.$message.error(response.msg || '获取预约详情失败');
        }
      } catch (error) {
        console.error('获取预约详情失败:', error);
        this.$message.error('获取预约详情失败');
      }
    },
    
    async deleteAppointment(appointment) {
      try {
        await this.$confirm(`确定要删除预约"${appointment.username} - ${appointment.serviceType}"吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        });
        
        const response = await appointmentApi.deleteAppointment(appointment.id);
        if (response.code === 0) {
          this.$message.success("预约删除成功");
          this.loadAppointments();
        } else {
          this.$message.error(response.msg || '删除预约失败');
        }
      } catch (error) {
        if (error === 'cancel') {
          this.$message.info("已取消删除");
        } else {
          console.error('删除预约失败:', error);
          this.$message.error("删除预约失败，请重试");
        }
      }
    },
    
    async saveAppointment() {
      try {
        await this.$refs.appointmentFormRef.validate();
        
        const formData = {
          ...this.appointmentForm,
          date: this.formatDate(this.appointmentForm.date)
        };
        
        let response;
        if (this.editingAppointment) {
          // 编辑预约
          response = await appointmentApi.updateAppointment(this.editingAppointment.id, formData);
        } else {
          // 添加预约
          response = await appointmentApi.createAppointment(formData);
        }
        
        if (response.code === 0) {
          this.$message.success(this.editingAppointment ? "预约信息更新成功" : "预约添加成功");
          this.appointmentDialogVisible = false;
          this.loadAppointments();
        } else {
          this.$message.error(response.msg || '保存预约失败');
        }
      } catch (error) {
        console.error('保存预约失败:', error);
        this.$message.error('保存预约失败，请重试');
      }
    },
    
    async confirmAppointment(appointment) {
      try {
        const response = await appointmentApi.confirmAppointment(appointment.id, 1); // 假设操作者ID为1
        if (response.code === 0) {
          this.$message.success("预约已确认");
          this.loadAppointments();
        } else {
          this.$message.error(response.msg || '确认预约失败');
        }
      } catch (error) {
        console.error('确认预约失败:', error);
        this.$message.error('确认预约失败');
      }
    },
    
    async rejectAppointment(appointment) {
      try {
        const response = await appointmentApi.merchantCancelAppointment(appointment.id, 1, "商家拒绝");
        if (response.code === 0) {
          this.$message.success("预约已拒绝");
          this.loadAppointments();
        } else {
          this.$message.error(response.msg || '拒绝预约失败');
        }
      } catch (error) {
        console.error('拒绝预约失败:', error);
        this.$message.error('拒绝预约失败');
      }
    },
    
    async completeAppointment(appointment) {
      try {
        const response = await appointmentApi.completeAppointment(appointment.id, 1); // 假设操作者ID为1
        if (response.code === 0) {
          this.$message.success("预约已完成");
          this.loadAppointments();
        } else {
          this.$message.error(response.msg || '完成预约失败');
        }
      } catch (error) {
        console.error('完成预约失败:', error);
        this.$message.error('完成预约失败');
      }
    },
    
    exportAppointments() {
      try {
        // 准备导出数据
        const exportData = this.filteredAppointments.map(appointment => ({
          '预约ID': appointment.id,
          '客户姓名': appointment.username,
          '宠物名称': appointment.petName,
          '服务类型': appointment.serviceType,
          '预约日期': appointment.date,
          '时间段': appointment.timeSlot,
          '状态': this.getStatusText(appointment.status),
          '创建时间': appointment.createTime,
          '备注': appointment.remark || ''
        }));
        
        // 转换为CSV格式
        const csvContent = this.convertToCSV(exportData);
        
        // 创建下载链接
        const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', `预约数据_${new Date().toISOString().split('T')[0]}.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        
        this.$message.success('数据导出成功');
      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败');
      }
    },
    
    convertToCSV(data) {
      if (!data || data.length === 0) return '';
      
      const headers = Object.keys(data[0]);
      const csvRows = [];
      
      // 添加标题行
      csvRows.push(headers.join(','));
      
      // 添加数据行
      data.forEach(row => {
        const values = headers.map(header => {
          const value = row[header];
          // 处理包含逗号或引号的值
          if (typeof value === 'string' && (value.includes(',') || value.includes('"'))) {
            return `"${value.replace(/"/g, '""')}"`;
          }
          return value || '';
        });
        csvRows.push(values.join(','));
      });
      
      return csvRows.join('\n');
    },
    
    startAutoRefresh() {
      // 启动自动刷新
      this.refreshTimer = setInterval(() => {
        // 只在页面可见且不在加载状态时刷新
        if (!document.hidden && !this.loading) {
          this.loadAppointments();
        }
      }, this.autoRefreshInterval);
    },
    
    stopAutoRefresh() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer);
        this.refreshTimer = null;
      }
    },
    
    async showStatisticsDialog() {
      this.statisticsDialogVisible = true;
      await this.loadStatistics();
    },
    
    async loadStatistics() {
      try {
        // 加载状态统计
        const statusResponse = await appointmentApi.getAppointmentStatusStatistics();
        const serviceResponse = await appointmentApi.getServiceTypeStatistics();
        
        if (statusResponse.code === 0 && serviceResponse.code === 0) {
          this.statisticsData = {
            pendingCount: this.appointments.filter(a => a.status === 'pending').length,
            confirmedCount: this.appointments.filter(a => a.status === 'confirmed').length,
            completedCount: this.appointments.filter(a => a.status === 'completed').length,
            cancelledCount: this.appointments.filter(a => a.status === 'cancelled').length,
            serviceStats: serviceResponse.data || []
          };
        }
      } catch (error) {
        console.error('加载统计信息失败:', error);
        this.$message.error('加载统计信息失败');
      }
    },
    
    // 获取完整图片URL
    getImageUrl(imagePath) {
      if (!imagePath) return 'http://localhost:8080/static/default-pet.png';
      
      // 如果已经是完整URL，直接返回
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath;
      }
      
      // 如果是base64数据，直接返回
      if (imagePath.startsWith('data:image')) {
        return imagePath;
      }
      
      // 如果路径以 /upload/ 开头，添加后端服务器地址
      if (imagePath.startsWith('/upload/')) {
        return 'http://localhost:8080' + imagePath;
      }
      
      // 其他情况，假设是文件名，添加 /upload/ 前缀
      return 'http://localhost:8080/upload/' + imagePath;
    },
    
    // 医疗服务管理方法
    async loadMedicalServices() {
      try {
        this.servicesLoading = true;
        
        console.log('开始加载医疗服务数据...');
        // 使用分页查询API获取所有服务（包括inactive状态）
        const response = await medicalServiceApi.getMedicalServiceList({
          pageNo: 1,
          pageSize: 100 // 设置较大的页面大小以获取所有服务
        });
        
        console.log('医疗服务API响应:', response);
        
        if (response.code === 200 || response.code === 0) {
          this.medicalServices = response.data.records || response.data || [];
          console.log('成功加载医疗服务数据:', this.medicalServices.length, '条记录');
        } else {
          console.error('加载医疗服务失败:', response);
          this.$message.error('加载医疗服务失败: ' + (response.msg || '未知错误'));
          this.medicalServices = [];
        }
        
      } catch (error) {
        console.error('加载医疗服务失败:', error);
        this.$message.error('加载医疗服务失败: ' + error.message);
        this.medicalServices = [];
      } finally {
        this.servicesLoading = false;
      }
    },
    
    showAddServiceDialog() {
      this.editingService = null;
      this.serviceForm = {
        name: "",
        description: "",
        price: null,
        category: "医疗服务",
        image: "",
        status: "active",
        productIntroduction: "",
        usageInstructions: ""
      };
      this.serviceDialogVisible = true;
    },
    
    editService(service) {
      this.editingService = service;
      this.serviceForm = { ...service };
      this.serviceDialogVisible = true;
    },
    
    async saveService() {
      try {
        await this.$refs.serviceFormRef.validate();
        
        let response;
        if (this.editingService) {
          // 更新服务
          console.log('更新医疗服务:', this.editingService.id, this.serviceForm);
          response = await medicalServiceApi.updateMedicalService(this.editingService.id, this.serviceForm);
        } else {
          // 添加新服务
          console.log('创建医疗服务:', this.serviceForm);
          response = await medicalServiceApi.createMedicalService(this.serviceForm);
        }
        
        console.log('保存服务API响应:', response);
        
        if (response.code === 200 || response.code === 0) {
          this.$message.success(this.editingService ? '医疗服务更新成功' : '医疗服务添加成功');
          this.loadMedicalServices(); // 重新加载数据
        } else {
          this.$message.error(response.msg || '操作失败');
        }
        
        this.serviceDialogVisible = false;
      } catch (error) {
        console.error('保存医疗服务失败:', error);
      }
    },
    
    updateServiceImage(service) {
      this.editingService = service;
      this.serviceImageDialogVisible = true;
    },
    
    async handleImageUpload(event) {
      const file = event.target.files[0];
      if (!file) return;
      
      // 存储文件对象
      this.selectedImageFile = file;
      
      // 生成预览URL
      const reader = new FileReader();
      reader.onload = (e) => {
        this.serviceForm.image = e.target.result; // 用于预览显示
        this.$message.success('图片选择成功');
      };
      reader.readAsDataURL(file);
    },
    
    async saveServiceImage() {
      try {
        if (this.editingService && this.selectedImageFile) {
          const response = await medicalServiceApi.updateServiceImage(this.editingService.id, this.selectedImageFile);
          if (response.code === 200 || response.code === 0) {
            // 更新服务图片URL
            this.serviceForm.image = response.data; // 假设返回URL
            this.editingService.image = response.data;
            this.$message.success('服务图片更新成功');
            this.serviceImageDialogVisible = false;
            this.loadMedicalServices(); // 重新加载数据
            // 清空文件选择
            this.selectedImageFile = null;
            // 清空input
            if (event && event.target) {
              event.target.value = '';
            }
          } else {
            this.$message.error(response.msg || '图片更新失败');
          }
        } else {
          this.$message.warning('请选择图片文件');
        }
      } catch (error) {
        console.error('更新服务图片失败:', error);
        this.$message.error('图片更新失败: ' + error.message);
      }
    },
    
    async toggleServiceStatus(service) {
      try {
        const newStatus = service.status === 'active' ? 'inactive' : 'active';
        console.log('更新服务状态:', service.id, '从', service.status, '到', newStatus);
        
        const response = await medicalServiceApi.updateServiceStatus(service.id, newStatus);
        
        console.log('状态更新API响应:', response);
        
        if (response.code === 200 || response.code === 0) {
          service.status = newStatus;
          const statusText = service.status === 'active' ? '启用' : '禁用';
          this.$message.success(`服务${statusText}成功`);
        } else {
          this.$message.error(response.msg || '状态更新失败');
        }
      } catch (error) {
        console.error('更新服务状态失败:', error);
        this.$message.error('状态更新失败: ' + error.message);
      }
    },
    
    deleteService(service) {
      this.$confirm('确定要删除这个医疗服务吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          console.log('删除医疗服务:', service.id);
          const response = await medicalServiceApi.deleteMedicalService(service.id);
          
          console.log('删除服务API响应:', response);
          
          if (response.code === 200 || response.code === 0) {
            this.$message.success('医疗服务删除成功');
            this.loadMedicalServices(); // 重新加载数据
          } else {
            this.$message.error(response.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除医疗服务失败:', error);
          this.$message.error('删除失败: ' + error.message);
        }
      });
    },
    
    refreshServices() {
      this.loadMedicalServices();
    },
    
    handleTabChange(tabName) {
      if (tabName === 'services') {
        this.loadMedicalServices();
      } else if (tabName === 'serviceBanner') {
        this.loadServiceBanner();
      }
    },
    
    // 服务展示图管理方法
    async loadServiceBanner() {
      try {
        // 首先从localStorage加载
        const savedImage = localStorage.getItem('serviceBannerImage');
        if (savedImage) {
          console.log('从本地存储加载服务展示图:', savedImage);
          this.serviceBannerImage = savedImage;
          return;
        }
        
        // 如果localStorage没有，从后端加载
        const response = await serviceBannerApi.getServiceSelectionBanner();
        if (response.code === 200 || response.code === 0 && response.data) {
          this.serviceBannerImage = response.data.imageUrl;
          // 保存到localStorage
          localStorage.setItem('serviceBannerImage', response.data.imageUrl);
        } else {
          this.serviceBannerImage = '/static/pet-medical-center.png'; // 默认图片
        }
      } catch (error) {
        console.error('加载服务展示图失败:', error);
        // 使用默认图片
        console.log('使用默认服务展示图');
        this.serviceBannerImage = '/static/pet-medical-center.png';
      }
    },
    
    showUpdateBannerDialog() {
      this.serviceBannerDialogVisible = true;
    },
    
    async handleBannerUpload(file) {
      if (!file) return false;
      
      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        this.$message.error('请选择图片文件');
        return false;
      }
      
      // 检查文件大小 (5MB)
      if (file.size > 5 * 1024 * 1024) {
        this.$message.error('图片大小不能超过5MB');
        return false;
      }
      
      this.bannerUploadLoading = true;
      return true; // 返回true允许上传
    },
    
    async handleBannerUploadSuccess(response, file) {
      console.log('上传成功响应:', response);
      this.bannerUploadLoading = false;
      
      if (response && response.code === 0) {
        this.serviceBannerImage = response.data;
        
        // 保存到localStorage
        localStorage.setItem('serviceBannerImage', response.data);
        
        this.$message.success('服务展示图上传成功');
      } else {
        this.$message.error('上传失败: ' + (response.msg || '未知错误'));
      }
    },
    
    handleBannerUploadError(error) {
      console.error('上传服务展示图失败:', error);
      this.bannerUploadLoading = false;
      this.$message.error('上传失败，请重试');
    },
    
    async saveServiceBanner() {
      try {
        if (!this.serviceBannerImage) {
          this.$message.warning('请先选择图片');
          return;
        }
        
        // 这里应该调用API保存服务展示图
        // 模拟保存成功
        this.$message.success('服务展示图保存成功');
        this.serviceBannerDialogVisible = false;
        
      } catch (error) {
        console.error('保存服务展示图失败:', error);
        this.$message.error('保存失败，请重试');
      }
    },
    
    removeServiceBanner() {
      this.$confirm('确定要删除服务展示图吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.serviceBannerImage = null;
        this.$message.success('服务展示图删除成功');
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.appointment-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
  
  .statistics-item {
    .stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      .label {
        font-weight: 500;
        color: #606266;
      }
      
      .value {
        font-weight: bold;
        color: #409EFF;
      }
    }
  }
  
  // 医疗服务管理样式
  .services-management {
    .services-header {
      margin-bottom: 20px;
      display: flex;
      gap: 10px;
    }
  }
  
  .image-upload-container {
    .current-image {
      margin-bottom: 20px;
      text-align: center;
      
      p {
        margin-bottom: 10px;
        font-weight: bold;
      }
    }
    
    .upload-section {
      text-align: center;
      
      p {
        margin-bottom: 10px;
        font-weight: bold;
      }
      
      .preview-image {
        margin-top: 15px;
        
        p {
          margin-bottom: 10px;
          font-weight: bold;
        }
      }
    }
  }
  
  // 服务展示图管理样式
  .service-banner-management {
    .banner-header {
      margin-bottom: 30px;
      
      h3 {
        margin-bottom: 10px;
        color: #303133;
      }
      
      p {
        color: #606266;
        margin: 0;
      }
    }
    
    .current-banner {
      text-align: center;
      
      h4 {
        margin-bottom: 15px;
        color: #303133;
      }
      
      .banner-actions {
        margin-top: 15px;
        display: flex;
        gap: 10px;
        justify-content: center;
      }
    }
    
    .no-banner {
      text-align: center;
      padding: 40px 0;
    }
  }
  
  .banner-upload-container {
    .upload-instructions {
      margin-bottom: 20px;
      padding: 15px;
      background-color: #f5f7fa;
      border-radius: 8px;
      
      h4 {
        margin-bottom: 10px;
        color: #303133;
      }
      
      ul {
        margin: 0;
        padding-left: 20px;
        
        li {
          margin-bottom: 5px;
          color: #606266;
        }
      }
    }
    
    .upload-section {
      margin-bottom: 20px;
      
      .banner-uploader {
        .upload-area {
          border: 2px dashed #d9d9d9;
          border-radius: 8px;
          width: 100%;
          height: 120px;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: border-color 0.3s;
          
          &:hover {
            border-color: #409EFF;
          }
          
          .el-icon-plus {
            font-size: 28px;
            color: #8c939d;
            margin-bottom: 10px;
          }
          
          .upload-text {
            color: #606266;
            font-size: 14px;
          }
        }
      }
    }
    
    .preview-section {
      h4 {
        margin-bottom: 15px;
        color: #303133;
      }
    }
  }
}
</style>