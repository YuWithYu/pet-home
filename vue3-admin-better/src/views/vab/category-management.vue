<template>
  <div class="category-management">
    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
          <span>📂 商品分类管理</span>
          <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </template>

      <!-- 添加分类 -->
      <div class="add-section">
        <h3>添加新分类</h3>
        <el-form :inline="true" @submit.native.prevent="addCategory">
          <el-form-item>
            <el-input
              v-model="newCategoryName"
              placeholder="请输入分类名称"
              style="width: 200px"
              clearable
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addCategory" :loading="adding">
              添加分类
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 分类列表 -->
      <div class="category-list">
        <h3>现有分类</h3>
        <el-table 
          :data="categories" 
          v-loading="loading"
          style="width: 100%"
        >
          <el-table-column prop="name" label="分类名称" min-width="200"></el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="scope">
              <el-button 
                size="mini" 
                type="primary" 
                @click="editCategory(scope.row)"
                :disabled="editing"
              >
                编辑
              </el-button>
              <el-button 
                size="mini" 
                type="danger" 
                @click="deleteCategory(scope.row.name)"
                :disabled="deleting"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 编辑分类对话框 -->
    <el-dialog 
      title="编辑分类" 
      v-model="editDialogVisible" 
      width="400px"
    >
      <el-form :model="editForm" :rules="editRules" ref="editForm">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateCategory" :loading="updating">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { 
  getAllCategories, 
  addCategory as addCategoryApi, 
  deleteCategory as deleteCategoryApi, 
  updateCategory as updateCategoryApi 
} from '@/api/pet-home'

export default {
  name: 'CategoryManagement',
  data() {
    return {
      categories: [],
      loading: false,
      adding: false,
      deleting: false,
      updating: false,
      editing: false,
      newCategoryName: '',
      editDialogVisible: false,
      editForm: {
        name: '',
        originalName: ''
      },
      editRules: {
        name: [
          { required: true, message: '请输入分类名称', trigger: 'blur' },
          { min: 1, max: 20, message: '分类名称长度在 1 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadCategories()
  },
  methods: {
    // 加载分类列表
    async loadCategories() {
      try {
        this.loading = true
        const response = await getAllCategories()
        if ((response.code === 200 || response.code === 0) && response.data) {
          this.categories = response.data.map(name => ({ name }))
        }
      } catch (error) {
        this.$message.error('加载分类列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },

    // 添加分类
    async addCategory() {
      if (!this.newCategoryName.trim()) {
        this.$message.warning('请输入分类名称')
        return
      }

      try {
        this.adding = true
        const response = await addCategoryApi({ name: this.newCategoryName.trim() })
        if (response.code === 200 || response.code === 0) {
          this.$message.success('添加分类成功')
          this.newCategoryName = ''
          this.loadCategories()
        } else {
          this.$message.error(response.msg || '添加分类失败')
        }
      } catch (error) {
        this.$message.error('添加分类失败: ' + error.message)
      } finally {
        this.adding = false
      }
    },

    // 编辑分类
    editCategory(category) {
      this.editForm.name = category.name
      this.editForm.originalName = category.name
      this.editDialogVisible = true
    },

    // 更新分类
    async updateCategory() {
      try {
        await this.$refs.editForm.validate()
        
        this.updating = true
        const response = await updateCategoryApi(this.editForm.originalName, { 
          name: this.editForm.name 
        })
        
        if (response.code === 200 || response.code === 0) {
          this.$message.success('更新分类成功')
          this.editDialogVisible = false
          this.loadCategories()
        } else {
          this.$message.error(response.msg || '更新分类失败')
        }
      } catch (error) {
        if (error.message) {
          this.$message.error('更新分类失败: ' + error.message)
        }
      } finally {
        this.updating = false
      }
    },

    // 删除分类
    async deleteCategory(categoryName) {
      try {
        await this.$confirm(`确定要删除分类 "${categoryName}" 吗？`, '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        this.deleting = true
        const response = await deleteCategoryApi(categoryName)
        
        if (response.code === 200 || response.code === 0) {
          this.$message.success('删除分类成功')
          this.loadCategories()
        } else {
          this.$message.error(response.msg || '删除分类失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除分类失败: ' + error.message)
        }
      } finally {
        this.deleting = false
      }
    },

    // 刷新数据
    refreshData() {
      this.loadCategories()
    }
  }
}
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.add-section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.add-section h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.category-list h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.clearfix::after {
  content: "";
  display: table;
  clear: both;
}

.box-card {
  margin-bottom: 20px;
}

.dialog-footer {
  text-align: right;
}
</style>
