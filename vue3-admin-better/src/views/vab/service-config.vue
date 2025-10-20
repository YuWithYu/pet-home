<template>
  <div class="service-config-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>服务配置管理</span>
          <el-button type="primary" @click="showAddDialog">添加服务</el-button>
        </div>
      </template>
      
      <el-table 
        :data="serviceList" 
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="serviceType" label="服务类型" width="150" />
        <el-table-column prop="serviceName" label="服务名称" width="150" />
        <el-table-column prop="description" label="服务描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="editService(row)">编辑</el-button>
            <el-button 
              type="text" 
              size="small"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="text" size="small" style="color: #f56c6c" @click="deleteService(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 添加/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editingService ? '编辑服务' : '添加服务'"
      width="800px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="服务类型" prop="serviceType">
              <el-input 
                v-model="form.serviceType" 
                placeholder="如：door-cleaning"
                :disabled="!!editingService"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务名称" prop="serviceName">
              <el-input v-model="form.serviceName" placeholder="如：上门铲屎服务" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="服务描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入服务描述"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="服务价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number
                v-model="form.sortOrder"
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="图标路径">
              <el-input v-model="form.icon" placeholder="/static/images/xxx.svg" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务图片">
              <el-input v-model="form.image" placeholder="/static/images/xxx.jpg" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="服务区域">
          <el-input v-model="form.serviceArea" placeholder="如：北京市朝阳区" />
        </el-form-item>
        
        <el-form-item label="每日最大预约">
          <el-input-number
            v-model="form.maxBookingsPerDay"
            :min="1"
            :max="100"
            style="width: 200px"
          />
        </el-form-item>
        
        <el-form-item label="时间段">
          <el-input 
            v-model="form.timeSlots" 
            type="textarea"
            :rows="3"
            placeholder='输入JSON格式，如：["08:00-10:00", "10:00-12:00"]'
          />
        </el-form-item>
        
        <el-form-item label="服务特点">
          <el-input 
            v-model="form.features" 
            type="textarea"
            :rows="4"
            placeholder='输入JSON格式，如：["特点1|描述1", "特点2|描述2"]'
          />
        </el-form-item>
        
        <el-form-item label="温馨提示">
          <el-input 
            v-model="form.notice" 
            type="textarea"
            :rows="4"
            placeholder='输入JSON格式，如：["提示1", "提示2"]'
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveService">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

export default {
  name: 'ServiceConfig',
  data() {
    return {
      loading: false,
      dialogVisible: false,
      editingService: null,
      serviceList: [],
      form: {
        serviceType: '',
        serviceName: '',
        description: '',
        price: 0,
        icon: '',
        image: '',
        timeSlots: '',
        serviceArea: '',
        maxBookingsPerDay: 10,
        status: 1,
        sortOrder: 0,
        features: '',
        notice: ''
      },
      rules: {
        serviceType: [
          { required: true, message: '请输入服务类型', trigger: 'blur' }
        ],
        serviceName: [
          { required: true, message: '请输入服务名称', trigger: 'blur' }
        ],
        price: [
          { required: true, message: '请输入服务价格', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadServices()
  },
  methods: {
    async loadServices() {
      try {
        this.loading = true
        const response = await request({
          url: '/api/service-config/all',
          method: 'get'
        })
        
        if (response.code === 0) {
          this.serviceList = response.data || []
        }
      } catch (error) {
        console.error('加载服务列表失败:', error)
        ElMessage.error('加载服务列表失败')
      } finally {
        this.loading = false
      }
    },
    
    showAddDialog() {
      this.editingService = null
      this.form = {
        serviceType: '',
        serviceName: '',
        description: '',
        price: 0,
        icon: '',
        image: '',
        timeSlots: '["08:00-10:00", "10:00-12:00", "12:00-14:00", "14:00-16:00", "16:00-18:00", "18:00-20:00"]',
        serviceArea: '',
        maxBookingsPerDay: 10,
        status: 1,
        sortOrder: 0,
        features: '["特点1|描述1", "特点2|描述2"]',
        notice: '["提示1", "提示2"]'
      }
      this.dialogVisible = true
      
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.resetFields()
        }
      })
    },
    
    editService(service) {
      this.editingService = service
      this.form = { ...service }
      this.dialogVisible = true
    },
    
    async saveService() {
      try {
        await this.$refs.formRef.validate()
        
        const url = this.editingService 
          ? '/api/service-config/update'
          : '/api/service-config/create'
        
        const response = await request({
          url,
          method: this.editingService ? 'put' : 'post',
          data: this.form
        })
        
        if (response.code === 0) {
          ElMessage.success(this.editingService ? '更新成功' : '添加成功')
          this.dialogVisible = false
          this.loadServices()
        } else {
          ElMessage.error(response.msg || '操作失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
        if (error !== 'cancel') {
          ElMessage.error('操作失败')
        }
      }
    },
    
    async toggleStatus(service) {
      try {
        const newStatus = service.status === 1 ? 0 : 1
        const response = await request({
          url: `/api/service-config/${service.id}/status`,
          method: 'put',
          params: { status: newStatus }
        })
        
        if (response.code === 0) {
          ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
          this.loadServices()
        } else {
          ElMessage.error(response.msg || '操作失败')
        }
      } catch (error) {
        console.error('更新状态失败:', error)
        ElMessage.error('操作失败')
      }
    },
    
    async deleteService(service) {
      try {
        await ElMessageBox.confirm(
          `确定要删除服务"${service.serviceName}"吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await request({
          url: `/api/service-config/${service.id}`,
          method: 'delete'
        })
        
        if (response.code === 0) {
          ElMessage.success('删除成功')
          this.loadServices()
        } else {
          ElMessage.error(response.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.service-config-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }
}
</style>

