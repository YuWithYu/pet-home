<template>
  <div class="timeslot-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>预约时间段管理</span>
          <div class="header-actions">
            <el-select
              v-model="selectedServiceType"
              placeholder="选择服务类型"
              style="width: 200px; margin-right: 10px"
              @change="loadTimeSlots"
            >
              <el-option label="上门铲屎" value="litter"></el-option>
              <el-option label="宠物寄养" value="boarding"></el-option>
              <el-option label="宠物医院" value="medical"></el-option>
              <el-option label="宠物洗护" value="grooming"></el-option>
              <el-option label="宠物领养" value="adoption"></el-option>
            </el-select>
            <el-button type="primary" @click="showAddDialog">添加时间段</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="timeSlots" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="serviceType" label="服务类型" width="150">
          <template #default="scope">
            <el-tag>{{ getServiceTypeName(scope.row.serviceType) }}</el-tag>
          </template>
        </el-table-column>
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
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button type="text" @click="editTimeSlot(scope.row)">编辑</el-button>
            <el-button 
              type="text" 
              :style="{ color: scope.row.isActive ? '#e6a23c' : '#67c23a' }"
              @click="toggleStatus(scope.row)"
            >
              {{ scope.row.isActive ? '禁用' : '启用' }}
            </el-button>
            <el-button type="text" @click="deleteTimeSlot(scope.row)" style="color: #f56c6c">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑时间段' : '添加时间段'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="服务类型" prop="serviceType">
          <el-select v-model="form.serviceType" placeholder="请选择服务类型" :disabled="isEdit">
            <el-option label="上门铲屎" value="litter"></el-option>
            <el-option label="宠物寄养" value="boarding"></el-option>
            <el-option label="宠物医院" value="medical"></el-option>
            <el-option label="宠物洗护" value="grooming"></el-option>
            <el-option label="宠物领养" value="adoption"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间段" prop="timeSlot">
          <el-input v-model="form.timeSlot" placeholder="例如: 09:00-10:00" />
        </el-form-item>
        
        <el-form-item label="最大预约数" prop="maxBookings">
          <el-input-number 
            v-model="form.maxBookings" 
            :min="1" 
            :max="100"
            placeholder="请输入最大预约数"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="isActive">
          <el-switch v-model="form.isActive" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  }
)

api.interceptors.response.use(response => response.data)

export default {
  name: 'TimeSlotManagement',
  setup() {
    const loading = ref(false)
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const selectedServiceType = ref('litter')
    const timeSlots = ref([])
    
    const form = reactive({
      id: null,
      serviceType: 'litter',
      timeSlot: '',
      maxBookings: 10,
      isActive: true
    })
    
    const formRef = ref(null)
    
    const rules = {
      serviceType: [
        { required: true, message: '请选择服务类型', trigger: 'change' }
      ],
      timeSlot: [
        { required: true, message: '请输入时间段', trigger: 'blur' },
        { pattern: /^([01]\d|2[0-3]):[0-5]\d-([01]\d|2[0-3]):[0-5]\d$/, message: '格式错误，例如: 09:00-10:00', trigger: 'blur' }
      ],
      maxBookings: [
        { required: true, message: '请输入最大预约数', trigger: 'blur' }
      ]
    }
    
    const getServiceTypeName = (type) => {
      const names = {
        litter: '上门铲屎',
        boarding: '宠物寄养',
        medical: '宠物医院',
        grooming: '宠物洗护',
        adoption: '宠物领养'
      }
      return names[type] || type
    }
    
    const loadTimeSlots = async () => {
      if (!selectedServiceType.value) return
      
      loading.value = true
      try {
        const response = await api.get('/api/time-slots/list', {
          params: { serviceType: selectedServiceType.value }
        })
        if (response.code === 0) {
          timeSlots.value = response.data || []
        }
      } catch (error) {
        console.error('加载失败:', error)
        ElMessage.error('加载时间段失败')
      } finally {
        loading.value = false
      }
    }
    
    const showAddDialog = () => {
      isEdit.value = false
      form.serviceType = selectedServiceType.value
      resetForm()
      dialogVisible.value = true
    }
    
    const editTimeSlot = (slot) => {
      isEdit.value = true
      form.id = slot.id
      form.serviceType = slot.serviceType
      form.timeSlot = slot.timeSlot
      form.maxBookings = slot.maxBookings
      form.isActive = slot.isActive
      dialogVisible.value = true
    }
    
    const save = async () => {
      try {
        await formRef.value.validate()
        
        const url = isEdit.value ? '/api/time-slots/update' : '/api/time-slots/create'
        const response = await api.post(url, form)
        
        if (response.code === 0) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadTimeSlots()
        } else {
          ElMessage.error(response.msg || '操作失败')
        }
      } catch (error) {
        console.error('保存失败:', error)
      }
    }
    
    const toggleStatus = async (slot) => {
      try {
        const response = await api.put(`/api/time-slots/${slot.id}/status`, null, {
          params: { isActive: !slot.isActive }
        })
        
        if (response.code === 0) {
          ElMessage.success('状态更新成功')
          loadTimeSlots()
        } else {
          ElMessage.error(response.msg || '更新失败')
        }
      } catch (error) {
        console.error('更新失败:', error)
        ElMessage.error('更新失败')
      }
    }
    
    const deleteTimeSlot = (slot) => {
      ElMessageBox.confirm('确定要删除这个时间段吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await api.delete(`/api/time-slots/${slot.id}`)
          if (response.code === 0) {
            ElMessage.success('删除成功')
            loadTimeSlots()
          } else {
            ElMessage.error(response.msg || '删除失败')
          }
        } catch (error) {
          console.error('删除失败:', error)
          ElMessage.error('删除失败')
        }
      }).catch(() => {})
    }
    
    const resetForm = () => {
      form.id = null
      form.timeSlot = ''
      form.maxBookings = 10
      form.isActive = true
      formRef.value?.resetFields()
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    }
    
    onMounted(() => {
      loadTimeSlots()
    })
    
    return {
      loading,
      dialogVisible,
      isEdit,
      selectedServiceType,
      timeSlots,
      form,
      formRef,
      rules,
      getServiceTypeName,
      loadTimeSlots,
      showAddDialog,
      editTimeSlot,
      save,
      toggleStatus,
      deleteTimeSlot,
      resetForm,
      formatDate
    }
  }
}
</script>

<style scoped>
.timeslot-container {
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

.dialog-footer {
  text-align: right;
}

.slots-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>

