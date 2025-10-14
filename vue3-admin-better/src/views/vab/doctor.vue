<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>医师管理</span>
          <el-button type="primary" @click="handleAdd">添加医师</el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-form :inline="true" :model="searchForm" class="demo-form-inline">
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入医师姓名" clearable />
          </el-form-item>
          <el-form-item label="专业领域">
            <el-select v-model="searchForm.specialization" placeholder="请选择专业领域" clearable>
              <el-option label="综合科" value="General" />
              <el-option label="犬类异宠类" value="Canine Exotic" />
              <el-option label="猫科" value="Feline" />
              <el-option label="犬类猫科" value="Canine Feline" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="在线" value="online" />
              <el-option label="离线" value="offline" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 医师列表 -->
      <el-table :data="doctorList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="头像" width="100">
          <template #default="scope">
            <el-image
              :src="getDoctorAvatar(scope.row.avatar)"
              style="width: 50px; height: 50px"
              fit="cover"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="specialization" label="专业领域" width="150" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'online' ? 'success' : 'info'">
              {{ scope.row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 添加/编辑医师对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="doctorForm" :rules="doctorRules" ref="doctorFormRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="doctorForm.name" placeholder="请输入医师姓名" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <el-input v-model="doctorForm.avatar" placeholder="请输入头像路径" />
        </el-form-item>
        <el-form-item label="专业领域" prop="specialization">
          <el-select v-model="doctorForm.specialization" placeholder="请选择专业领域">
            <el-option label="综合科" value="General" />
            <el-option label="犬类异宠类" value="Canine Exotic" />
            <el-option label="猫科" value="Feline" />
            <el-option label="犬类猫科" value="Canine Feline" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="doctorForm.category" placeholder="请选择类别">
            <el-option label="综合科" value="Comprehensive" />
            <el-option label="专科" value="Specialist" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="doctorForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入医师描述"
          />
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="doctorForm.rating" show-score />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="doctorForm.status" placeholder="请选择状态">
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { doctorApi } from '@/api/doctor.js'

export default {
  name: 'DoctorManagement',
  setup() {
    const loading = ref(false)
    const doctorList = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const doctorFormRef = ref()

    // 搜索表单
    const searchForm = reactive({
      name: '',
      specialization: '',
      status: ''
    })

    // 医师表单
    const doctorForm = reactive({
      id: null,
      name: '',
      avatar: '',
      specialization: '',
      category: '',
      description: '',
      rating: 0,
      status: 'online'
    })

    // 表单验证规则
    const doctorRules = {
      name: [
        { required: true, message: '请输入医师姓名', trigger: 'blur' }
      ],
      specialization: [
        { required: true, message: '请选择专业领域', trigger: 'change' }
      ],
      category: [
        { required: true, message: '请选择类别', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请输入医师描述', trigger: 'blur' }
      ]
    }

    // 获取医师头像URL
    const getDoctorAvatar = (avatarPath) => {
      if (!avatarPath) return 'http://localhost:8080/static/default-doctor.png'
      if (avatarPath.startsWith('http://') || avatarPath.startsWith('https://')) {
        return avatarPath
      }
      if (avatarPath.startsWith('/upload/')) {
        return 'http://localhost:8080' + avatarPath
      }
      return 'http://localhost:8080/upload/' + avatarPath
    }

    // 加载医师列表
    const loadDoctors = async () => {
      try {
        loading.value = true
        const response = await doctorApi.getAllDoctors()
        if (response.code === 200 || response.code === 0) {
          doctorList.value = response.data || []
          total.value = doctorList.value.length
        } else {
          ElMessage.error(response.msg || '获取医师列表失败')
        }
      } catch (error) {
        console.error('加载医师列表失败:', error)
        ElMessage.error('加载医师列表失败')
      } finally {
        loading.value = false
      }
    }

    // 搜索
    const handleSearch = () => {
      currentPage.value = 1
      loadDoctors()
    }

    // 重置搜索
    const handleReset = () => {
      Object.keys(searchForm).forEach(key => {
        searchForm[key] = ''
      })
      handleSearch()
    }

    // 分页大小改变
    const handleSizeChange = (val) => {
      pageSize.value = val
      loadDoctors()
    }

    // 当前页改变
    const handleCurrentChange = (val) => {
      currentPage.value = val
      loadDoctors()
    }

    // 添加医师
    const handleAdd = () => {
      dialogTitle.value = '添加医师'
      dialogVisible.value = true
      resetForm()
    }

    // 编辑医师
    const handleEdit = (row) => {
      dialogTitle.value = '编辑医师'
      dialogVisible.value = true
      Object.keys(doctorForm).forEach(key => {
        doctorForm[key] = row[key]
      })
    }

    // 删除医师
    const handleDelete = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除这个医师吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const response = await doctorApi.deleteDoctor(row.id)
        if (response.code === 200 || response.code === 0) {
          ElMessage.success('删除成功')
          loadDoctors()
        } else {
          ElMessage.error(response.msg || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除医师失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }

    // 提交表单
    const handleSubmit = async () => {
      try {
        await doctorFormRef.value.validate()
        
        let response
        if (doctorForm.id) {
          // 编辑
          response = await doctorApi.updateDoctor(doctorForm.id, doctorForm)
        } else {
          // 添加
          response = await doctorApi.addDoctor(doctorForm)
        }
        
        if (response.code === 200 || response.code === 0) {
          ElMessage.success(doctorForm.id ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadDoctors()
        } else {
          ElMessage.error(response.msg || '操作失败')
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error('提交失败')
      }
    }

    // 关闭对话框
    const handleDialogClose = () => {
      resetForm()
    }

    // 重置表单
    const resetForm = () => {
      Object.keys(doctorForm).forEach(key => {
        doctorForm[key] = key === 'rating' ? 0 : key === 'status' ? 'online' : ''
      })
      doctorForm.id = null
      if (doctorFormRef.value) {
        doctorFormRef.value.resetFields()
      }
    }

    onMounted(() => {
      loadDoctors()
    })

    return {
      loading,
      doctorList,
      total,
      currentPage,
      pageSize,
      dialogVisible,
      dialogTitle,
      doctorFormRef,
      searchForm,
      doctorForm,
      doctorRules,
      getDoctorAvatar,
      loadDoctors,
      handleSearch,
      handleReset,
      handleSizeChange,
      handleCurrentChange,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleDialogClose
    }
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.box-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: bold;
  }
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>

