<template>
  <div class="service-store-list-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>服务门店管理</span>
          <div class="header-actions">
            <el-select v-model="filterServiceType" placeholder="服务类型" clearable style="width: 140px; margin-right: 10px" @change="loadStores">
              <el-option label="全部" value="" />
              <el-option label="洗护" value="grooming" />
              <el-option label="医院" value="hospital" />
              <el-option label="上门铲屎" value="litter" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 120px; margin-right: 10px" @change="loadStores">
              <el-option label="全部" value="" />
              <el-option label="营业中" value="active" />
              <el-option label="暂停" value="inactive" />
              <el-option label="已关闭" value="closed" />
            </el-select>
            <el-button type="primary" @click="showAddDialog">添加服务门店</el-button>
          </div>
        </div>
      </template>

      <el-table :data="storeList" v-loading="loading" style="width: 100%" row-key="id">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="storeName" label="门店名称" min-width="160" />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column prop="businessHours" label="营业时间" width="120" />
        <el-table-column label="服务类型" width="120">
          <template #default="{ row }">
            {{ formatServices(row.services) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : (row.status === 'closed' ? 'info' : 'warning')">
              {{ row.status === 'active' ? '营业中' : (row.status === 'closed' ? '已关闭' : '暂停') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="默认" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault" type="success" size="small">推荐</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="editStore(row)">编辑</el-button>
            <el-button type="danger" link @click="deleteStore(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadStores"
          @current-change="loadStores"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingStore ? '编辑服务门店' : '添加服务门店'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="门店名称" prop="storeName">
          <el-input v-model="form.storeName" placeholder="如：宠物之家·天河旗舰店" />
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="详细地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="如：020-12345678" />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="form.businessHours" placeholder="如：08:00-22:00" />
        </el-form-item>
        <el-form-item label="服务类型" prop="servicesList">
          <el-select v-model="form.servicesList" multiple placeholder="可多选（至少选一项）" style="width: 100%">
            <el-option label="洗护" value="grooming" />
            <el-option label="医院" value="hospital" />
            <el-option label="上门铲屎" value="litter" />
          </el-select>
          <div class="form-tip">小程序预约洗护/医院时会按服务类型筛选门店</div>
        </el-form-item>
        <el-form-item label="最大容量" prop="maxCapacity">
          <el-input-number v-model="form.maxCapacity" :min="0" :max="999" placeholder="可选，0表示不限制" />
          <span class="form-tip-inline">可选，0 表示不限制余位显示</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="active">营业中</el-radio>
            <el-radio value="inactive">暂停营业</el-radio>
            <el-radio value="closed">已关闭</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否默认" prop="isDefault">
          <el-switch v-model="form.isDefault" :active-value="true" :inactive-value="false" />
          <span class="form-tip-inline">默认门店在列表中显示「推荐」</span>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="门店头像（客服会话显示）" prop="imageUrl">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :http-request="uploadImage"
          >
            <img v-if="form.imageUrl" :src="getStoreImageUrl(form.imageUrl)" class="store-avatar" alt="门店头像" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="form-tip">小程序消息列表中该门店客服将显示此头像</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { baseURL } from '@/config'
import { Plus } from '@element-plus/icons-vue'

export default {
  name: 'ServiceStoreList',
  components: { Plus },
  data() {
    return {
      loading: false,
      storeList: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      filterServiceType: '',
      filterStatus: '',
      dialogVisible: false,
      editingStore: null,
      formRef: null,
      form: {
        storeName: '',
        address: '',
        phone: '',
        businessHours: '09:00-21:00',
        servicesList: [],
        maxCapacity: 0,
        status: 'active',
        isDefault: false,
        sortOrder: 0,
        imageUrl: ''
      },
      rules: {
        storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
        address: [{ required: true, message: '请输入详细地址', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
        servicesList: [{ type: 'array', min: 1, message: '请至少选择一项服务类型', trigger: 'change' }]
      }
    }
  },
  mounted() {
    this.loadStores()
  },
  methods: {
    formatServices(services) {
      if (!services) return '-'
      const map = { grooming: '洗护', hospital: '医院', litter: '上门铲屎' }
      const exclude = ['boarding', '寄养']
      return services.split(',')
        .map(s => s.trim())
        .filter(s => s && !exclude.includes(s.toLowerCase()))
        .map(s => map[s] || s)
        .filter(Boolean)
        .join('、') || '-'
    },
    async loadStores() {
      this.loading = true
      try {
        const params = { pageNo: this.currentPage, pageSize: this.pageSize }
        if (this.filterServiceType) params.serviceType = this.filterServiceType
        if (this.filterStatus) params.status = this.filterStatus
        const data = await request.get('/stores/page', { params })
        if (data.code !== 0 && data.code !== 200) {
          this.storeList = []
          this.total = 0
          return
        }
        const raw = data.data
        if (Array.isArray(raw)) {
          this.storeList = raw
          this.total = raw.length
        } else if (raw && typeof raw === 'object') {
          this.storeList = raw.records || raw.list || []
          this.total = raw.total != null ? Number(raw.total) : this.storeList.length
        } else {
          this.storeList = []
          this.total = 0
        }
      } catch (e) {
        console.error('服务门店列表请求失败:', e)
        this.storeList = []
        this.total = 0
        const msg = e?.response?.data?.msg || (typeof e === 'string' && e.includes('401') ? '未登录或登录已过期，请重新登录' : (e?.message || '加载门店列表失败'))
        this.$message.error(msg)
      } finally {
        this.loading = false
      }
    },
    getStoreImageUrl(url) {
      if (!url || typeof url !== 'string') return ''
      const raw = url.trim()
      if (raw.startsWith('http://') || raw.startsWith('https://')) return raw
      const origin = (baseURL || '').replace(/\/api\/?$/, '') || 'http://localhost:8080'
      if (raw.startsWith('/')) return origin + raw
      return origin + '/' + raw
    },
    beforeImageUpload(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt10M = file.size / 1024 / 1024 < 10
      if (!isImage) this.$message.error('只能上传 JPG/PNG 图片')
      if (!isLt10M) this.$message.error('图片大小不能超过 10MB')
      return isImage && isLt10M
    },
    async uploadImage({ file }) {
      const formData = new FormData()
      formData.append('file', file)
      try {
        const res = await request({
          url: '/upload/image',
          method: 'post',
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        const url = (res && res.data) ? res.data : (res && res.url) ? res.url : ''
        if (url && typeof url === 'string') {
          this.form.imageUrl = url
          this.$message.success('头像上传成功')
        } else {
          this.$message.error('上传失败，未返回图片地址')
        }
      } catch (e) {
        console.error(e)
        this.$message.error(e?.response?.data?.msg || e?.message || '上传失败')
      }
    },
    showAddDialog() {
      this.editingStore = null
      this.form = {
        storeName: '',
        address: '',
        phone: '',
        businessHours: '09:00-21:00',
        servicesList: [],
        maxCapacity: 0,
        status: 'active',
        isDefault: false,
        sortOrder: 0,
        imageUrl: ''
      }
      this.dialogVisible = true
    },
    editStore(row) {
      this.editingStore = row
      const excludeTypes = ['boarding', '寄养']
      const services = (row.services || '')
        .split(',')
        .map(s => s.trim())
        .filter(s => s && !excludeTypes.includes(s.toLowerCase()))
      this.form = {
        id: row.id,
        storeName: row.storeName || '',
        address: row.address || '',
        phone: row.phone || '',
        businessHours: row.businessHours || '09:00-21:00',
        servicesList: services,
        maxCapacity: row.maxCapacity || 0,
        status: row.status || 'active',
        isDefault: !!row.isDefault,
        sortOrder: row.sortOrder != null ? row.sortOrder : 0,
        imageUrl: row.imageUrl || ''
      }
      this.dialogVisible = true
    },
    async submitForm() {
      try {
        await this.$refs.formRef.validate()
        const excludeTypes = ['boarding', '寄养']
        const list = Array.isArray(this.form.servicesList) ? this.form.servicesList : []
        const servicesStr = list.filter(s => s && !excludeTypes.includes(String(s).toLowerCase())).join(',')
        const payload = {
          storeName: this.form.storeName,
          address: this.form.address,
          phone: this.form.phone,
          businessHours: this.form.businessHours,
          services: servicesStr,
          maxCapacity: this.form.maxCapacity || null,
          status: this.form.status,
          isDefault: this.form.isDefault,
          sortOrder: this.form.sortOrder,
          imageUrl: this.form.imageUrl || null
        }
        if (this.editingStore) {
          payload.id = this.editingStore.id
          const res = await request.put('/stores/update', payload)
          if (res && (res.code === 0 || res.code === 200)) {
            this.$message.success('更新成功')
            this.dialogVisible = false
            this.loadStores()
          } else {
            this.$message.error((res && res.msg) || '更新失败')
          }
        } else {
          const res = await request.post('/stores/create', payload)
          if (res && (res.code === 0 || res.code === 200)) {
            this.$message.success('添加成功')
            this.dialogVisible = false
            this.currentPage = 1
            await this.loadStores()
          } else {
            this.$message.error((res && res.msg) || '添加失败')
          }
        }
      } catch (e) {
        if (e === 'cancel' || (e && e.toString && e.toString().includes('cancel'))) return
        const msg = (e && e.message) ? e.message : (typeof e === 'string' ? e : '请检查必填项：门店名称、详细地址、联系电话、服务类型')
        this.$message.error(msg)
      }
    },
    async deleteStore(row) {
      try {
        await this.$confirm('确定删除该服务门店？删除后小程序端将不再展示。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        const res = await request.delete(`/stores/${row.id}`)
        if (res && (res.code === 0 || res.code === 200)) {
          this.$message.success('删除成功')
          this.loadStores()
        } else {
          this.$message.error((res && res.msg) || '删除失败')
        }
      } catch (e) {
        if (e !== 'cancel' && e) this.$message.error(e.message || e.msg || '删除失败')
      }
    }
  }
}
</script>

<style scoped>
.service-store-list-container { padding: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.header-actions { display: flex; align-items: center; flex-wrap: wrap; }
.pagination-container { margin-top: 16px; display: flex; justify-content: flex-end; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
.form-tip-inline { font-size: 12px; color: #909399; margin-left: 8px; }
.avatar-uploader :deep(.el-upload) { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; overflow: hidden; }
.avatar-uploader :deep(.el-upload:hover) { border-color: var(--el-color-primary); }
.store-avatar { width: 120px; height: 120px; display: block; object-fit: cover; }
.avatar-uploader-icon { font-size: 28px; color: #8c939d; width: 120px; height: 120px; display: flex; align-items: center; justify-content: center; border: 1px dashed #d9d9d9; border-radius: 6px; }
</style>
