<template>
  <div class="permissions-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账号管理" name="user">
          <div style="margin-bottom: 20px; display: flex; gap: 10px;">
            <el-button v-if="isSuperAdmin" type="primary" @click="handleAddUser">添加账号</el-button>
          </div>
          <el-table :data="users" style="width: 100%" row-key="id" v-loading="loading" border>
            <el-table-column prop="username" label="用户名" width="150" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column label="角色" width="120">
              <template #default="{ row }">
                <el-tag
                  v-if="row.role === 'admin'"
                  type="warning"
                  style="margin-right: 10px"
                >
                  平台管理员
                </el-tag>
                <el-tag
                  v-else-if="row.role === 'store_admin'"
                  type="primary"
                  style="margin-right: 10px"
                >
                  分店管理员
                </el-tag>
                <el-tag
                  v-else-if="row.role === 'staff'"
                  type="info"
                  style="margin-right: 10px"
                >
                  员工
                </el-tag>
                <el-tag
                  v-else 
                  type="success"
                  style="margin-right: 10px"
                >
                  {{ getRoleName(row.role) || row.role || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="所属门店" width="180">
              <template #default="{ row }">
                <span v-if="row.serviceStoreName">{{ row.serviceStoreName }}</span>
                <span v-else style="color: #999;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="电话" width="150" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="success">启用</el-tag>
                <el-tag v-else type="danger">禁用</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="editUser(row)">编辑</el-button>
                <el-button link type="primary" @click="setPermissions(row)">权限设置</el-button>
                <el-button link type="danger" @click="deleteUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 用户编辑对话框 -->
    <el-dialog v-model="userDialogVisible" :title="currentUser.id ? '编辑账号' : '添加账号'" width="600px" @closed="resetUserForm">
      <el-form :model="currentUser" label-width="100px" :rules="userRules" ref="userFormRef" autocomplete="off">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="currentUser.username" :disabled="!!currentUser.id" placeholder="请输入用户名（用于登录）" autocomplete="off" />
          <div style="font-size: 12px; color: #999; margin-top: 5px;">用户名用于登录系统，必须唯一</div>
        </el-form-item>
        <el-form-item v-if="currentUser.id" label="密码">
          <el-input v-model="currentUser.password" type="text" placeholder="留空不修改，填新密码即重置" clearable autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="currentUser.name" placeholder="请输入姓名（人员姓名）" autocomplete="off" />
          <div style="font-size: 12px; color: #999; margin-top: 5px;">姓名用于显示，可以是真实姓名或昵称</div>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!currentUser.id">
          <el-input v-model="currentUser.password" type="password" placeholder="请输入密码" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="currentUser.role" placeholder="请选择角色" style="width: 100%" @change="handleRoleChange" filterable>
            <el-option 
              v-for="role in roles.filter(r => r.status === 1 && ['admin', 'store_admin', 'staff'].includes(r.roleCode))"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            />
          </el-select>
        </el-form-item>
        <template v-if="currentUser.role && currentUser.role !== 'admin'">
          <div style="font-size: 12px; color: #909399; margin-bottom: 8px; line-height: 1.5;">
            选填，可只选其一或都不选（都不选为平台级）。商品店铺管商城/商品数据，服务门店管排班与预约。
          </div>
          <el-form-item label="所属服务门店" prop="serviceStoreId">
            <el-select 
              v-model="currentUser.serviceStoreId" 
              placeholder="不选则平台级，可参与任意门店排班（选填）" 
              style="width: 100%" 
              clearable
              filterable
            >
              <el-option 
                v-for="store in serviceStoreList" 
                :key="'s' + store.id" 
                :label="(store.storeName || store.name) + (store.address ? '（' + store.address + '）' : '')" 
                :value="store.id"
              />
            </el-select>
            <div style="font-size: 12px; color: #999; margin-top: 5px;">服务岗位和分店管理员都必须绑定门店；平台管理员可不绑定</div>
          </el-form-item>
        </template>
        <el-form-item label="电话">
          <el-input v-model="currentUser.phone" placeholder="请输入电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限设置对话框 -->
    <el-dialog v-model="permissionDialogVisible" title="权限设置" width="800px">
      <div v-if="currentPermissionUser">
        <div style="margin-bottom: 20px;">
          <el-tag type="info" size="large">账号：{{ currentPermissionUser.username }}（{{ currentPermissionUser.name }}）</el-tag>
        </div>
        <el-checkbox-group v-model="selectedPermissions" style="width: 100%;">
          <el-row :gutter="20">
            <el-col :span="12" v-for="permission in pagePermissions" :key="permission.permissionCode" style="margin-bottom: 10px;">
              <el-checkbox :label="permission.permissionCode" :value="permission.permissionCode">
                {{ permission.permissionName }}
              </el-checkbox>
            </el-col>
          </el-row>
        </el-checkbox-group>
            <div style="margin-top: 20px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
          <div style="font-size: 12px; color: #666; line-height: 1.6;">
            <div>已选 <strong>{{ selectedPermissions.length }}</strong> 项</div>
            <div style="margin-top: 4px;">勾选后仅可访问对应菜单；全部不勾选并保存表示清空细粒度权限。</div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePermissions" :loading="permissionSaving">保存</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onActivated } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const store = useStore()
const isSuperAdmin = computed(() => store.getters['user/role'] === 'admin')

// 使用 request 工具，不需要 baseURL（request 已配置）

const activeTab = ref('user')
const userDialogVisible = ref(false)
const loading = ref(false)
const saving = ref(false)
const serviceStoreList = ref([])  // 服务门店（/stores/all）
const userFormRef = ref(null)

// 权限设置相关
const permissionDialogVisible = ref(false)
const permissionSaving = ref(false)
const currentPermissionUser = ref(null)
const availablePermissions = ref([])
const selectedPermissions = ref([])
const pagePermissionConfig = [
  { code: '/content/banner', name: '轮播图管理', candidates: ['/content/banner'] },
  { code: '/content/notice', name: '公告管理', candidates: ['/content/notice'] },
  { code: '/content/community', name: '社区管理', candidates: ['/content/community'] },
  { code: '/content/complaint', name: '投诉举报', candidates: ['/content/complaint'] },
  { code: '/mall', name: '商城管理', candidates: ['/mall', '/mall/'] },
  { code: '/points-mall', name: '积分商城管理', candidates: ['/points-mall', '/points-mall/'] },
  { code: '/service-platform/litter-service', name: '服务管理', candidates: ['/service-platform/litter-service', '/service-platform'] },
  { code: '/service-staff', name: '服务人员管理', candidates: ['/service-staff', '/service-staff/'] },
  { code: '/service-stores', name: '服务门店管理', candidates: ['/service-stores', '/service-stores/'] },
  { code: '/service-orders/appointment-orders', name: '预约订单管理', candidates: ['/service-orders/appointment-orders', '/service-orders'] },
  { code: '/my-schedule', name: '我的排班', candidates: ['/my-schedule', '/my-schedule/'] },
  { code: '/verify', name: '订单核销', candidates: ['/verify', '/verify/'] },
  { code: '/outlet-customer-chat', name: '门店客服', candidates: ['/outlet-customer-chat', '/outlet-customer-chat/'] },
  { code: '/users/list', name: '用户列表', candidates: ['/users/list'] },
  { code: '/users/permissions', name: '账号管理', candidates: ['/users/permissions'] },
  { code: '/users/platform-customer-chat', name: '平台客服', candidates: ['/users/platform-customer-chat'] },
  { code: '/reports/dashboard', name: '数据大屏', candidates: ['/reports/dashboard', '/reports'] },
]

const currentUser = reactive({
  id: null,
  username: "",
  name: "",
  password: "",
  role: "",
  serviceStoreId: null,
  doctorId: null,
  email: "",
  phone: "",
  status: 1
})

const users = ref([])
const roles = ref([])

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  // 仅保留服务门店选择（选填）
}

// 加载账号列表
const loadUsers = async () => {
  try {
    loading.value = true
    const response = await request.get('/admin/staff/list', {
      params: { accountsOnly: true, _t: Date.now() }
    })
    
    if (response.code === 0 || response.code === 200) {
      users.value = (response.data || []).map(user => {
        let serviceStoreName = null
        // 兼容 camelCase(serviceStoreId) 与 snake_case(service_store_id)，并统一用 ID 去服务门店列表里查名称
        const serviceStoreId = user.serviceStoreId ?? user.service_store_id
        if (serviceStoreId != null && serviceStoreList.value.length > 0) {
          const s = serviceStoreList.value.find(x => x.id == serviceStoreId)
          if (s) serviceStoreName = s.storeName || s.name
        }
        return { ...user, serviceStoreName }
      })
    }
  } catch (error) {
    console.error('加载账号列表失败:', error)
    ElMessage.error('加载账号列表失败')
  } finally {
    loading.value = false
  }
}

// 加载服务门店列表（所属服务门店）
const loadServiceStores = async () => {
  try {
    const response = await request.get('/stores/all')
    if (response.code === 0 || response.code === 200) {
      const list = response.data || []
      serviceStoreList.value = list.map(s => ({
        id: s.id,
        name: s.storeName || s.name || `门店${s.id}`,
        storeName: s.storeName,
        address: s.address
      }))
    } else {
      serviceStoreList.value = []
    }
  } catch (error) {
    console.error('加载服务门店列表失败:', error)
    serviceStoreList.value = []
  }
}

// 获取角色名称
const getRoleName = (roleCode) => {
  const role = roles.value.find(r => r.roleCode === roleCode)
  return role ? role.roleName : null
}

const pagePermissions = computed(() => {
  const all = availablePermissions.value || []
  const byCode = new Map(all.map((p) => [p.permissionCode, p]))
  return pagePermissionConfig.map((item) => {
    const matchedCode = (item.candidates || []).find((code) => byCode.has(code))
    const matched = matchedCode ? byCode.get(matchedCode) : null
    return {
      permissionCode: item.code,
      permissionName: matched?.permissionName || item.name,
      candidates: item.candidates,
      actualCode: matchedCode || item.code,
    }
  })
})

// 处理角色变化
const handleRoleChange = (role) => {
  // 管理员角色不绑定服务门店
  if (role === 'admin') {
    currentUser.serviceStoreId = null
    currentUser.doctorId = null
  }
  currentUser.doctorId = null
}

// 关闭对话框时清空表单，避免下次「添加账号」时带出上次的姓名/密码
const resetUserForm = () => {
  Object.assign(currentUser, {
    id: null,
    username: "",
    name: "",
    password: "",
    role: "",
    serviceStoreId: null,
    doctorId: null,
    email: "",
    phone: "",
    status: 1
  })
}

// 添加账号（姓名、密码不预填，由管理员自己填写）
const handleAddUser = () => {
  resetUserForm()
  userDialogVisible.value = true
}

// 编辑账号（兼容接口返回 serviceStoreId 或 service_store_id；空字符串转为 null）
const editUser = (user) => {
  const toNull = (v) => (v === '' || v === undefined) ? null : v
  Object.assign(currentUser, {
    id: user.id,
    username: user.username,
    name: user.name,
    password: "",
    role: user.role,
    serviceStoreId: toNull(user.serviceStoreId ?? user.service_store_id ?? null),
    doctorId: null,
    email: user.email || "",
    phone: user.phone || "",
    status: user.status !== undefined ? user.status : 1
  })
  userDialogVisible.value = true
}

// 删除账号
// 设置权限
const setPermissions = async (user) => {
  try {
    // 管理员可以设置细粒度权限（用于限制访问范围）
    // 如果设置了权限，则只拥有设置的权限
    
    currentPermissionUser.value = user
    selectedPermissions.value = []
    
    // 加载可用权限列表
    const availableResponse = await request.get('/admin/permissions/available')
    
    if (availableResponse.code === 0 || availableResponse.code === 200) {
      availablePermissions.value = availableResponse.data || []
    } else {
      ElMessage.error('加载可用权限列表失败')
      return
    }
    
    // 加载该账号已有的权限
    const permissionResponse = await request.get(`/admin/permissions/${user.id}`)
    
    if (permissionResponse.code === 0 || permissionResponse.code === 200) {
      const userPermissions = permissionResponse.data || []
      const rawSelected = userPermissions
        .filter(p => p.status === 1)
        .map(p => p.permissionCode)
      const rawSet = new Set(rawSelected)
      selectedPermissions.value = pagePermissions.value
        .filter((item) => (item.candidates || []).some((code) => rawSet.has(code)))
        .map((item) => item.permissionCode)
    } else {
      console.warn('加载账号权限失败，使用空权限列表')
    }
    
    permissionDialogVisible.value = true
  } catch (error) {
    console.error('设置权限失败:', error)
    ElMessage.error('设置权限失败')
  }
}

// 保存权限设置
const savePermissions = async () => {
  if (!currentPermissionUser.value) return
  
  try {
    permissionSaving.value = true
    
    // 构建权限数据
    const selectedSet = new Set(selectedPermissions.value)
    const expandedCodes = pagePermissions.value
      .filter((item) => selectedSet.has(item.permissionCode))
      .map((item) => item.actualCode || item.permissionCode)

    const permissions = expandedCodes.map(code => {
      const permission = availablePermissions.value.find(p => p.permissionCode === code)
      return {
        permissionCode: code,
        permissionName: permission ? permission.permissionName : code,
        status: 1
      }
    })
    
    // 使用 request 工具发送权限数据
    const response = await request.post(`/admin/permissions/${currentPermissionUser.value.id}`, permissions)
    
    if (response.code === 0 || response.code === 200) {
      ElMessage.success('权限设置成功')
      permissionDialogVisible.value = false
    } else {
      ElMessage.error(response.msg || '权限设置失败')
    }
  } catch (error) {
    console.error('保存权限失败:', error)
    ElMessage.error('保存权限失败')
  } finally {
    permissionSaving.value = false
  }
}

const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除账号"${user.username}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await request.delete(`/admin/staff/${user.id}`)
    
    if (response.code === 0 || response.code === 200) {
      ElMessage.success('删除成功')
      users.value = users.value.filter(u => u.id != user.id)
      // 不再调用 loadUsers，避免后端 ServiceMember 回退把该条又塞回来
    } else if (response.msg && response.msg.includes('员工不存在')) {
      // 数据库已删除但前端仍有缓存：刷新列表移除该条
      ElMessage.info('该员工已不存在，已刷新列表')
      loadUsers()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error === 'cancel') return
    // 后端返回 500 "员工不存在" 时，request 拦截器会 reject，error 为字符串
    const errStr = String(error)
    if (errStr.includes('员工不存在')) {
      ElMessage.info('该员工已不存在，已从列表移除')
      users.value = users.value.filter(u => u.id != user.id)
      return
    }
    console.error('删除账号失败:', error)
    ElMessage.error('删除账号失败')
  }
}

// 保存账号
const saveUser = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    
    saving.value = true
    
    // 管理员不能关联门店
    if (currentUser.role === 'admin') {
      if (currentUser.serviceStoreId) {
        ElMessage.warning('管理员不能关联门店，已自动清空门店选择')
        currentUser.serviceStoreId = null
      }
    }
    
    // 如果是医师角色，可以不选择医师（系统会自动创建医师记录）
    // 注释掉强制选择医师的检查
    
    if (currentUser.id) {
      // 更新账号（服务岗位必须绑定 serviceStoreId）
      const toNull = (v) => (v === '' || v === undefined) ? null : v
      const serviceStoreIdVal = currentUser.role === 'admin' ? null : toNull(currentUser.serviceStoreId)
      if ((currentUser.role === 'staff' || currentUser.role === 'store_admin') && !serviceStoreIdVal) {
        ElMessage.warning(currentUser.role === 'store_admin' ? '分店管理员必须绑定服务门店' : '服务岗位账号必须绑定服务门店')
        saving.value = false
        return
      }
      const updateData = {
        id: currentUser.id,
        name: currentUser.name,
        role: currentUser.role,
        serviceStoreId: serviceStoreIdVal,
        doctorId: null,
        email: currentUser.email,
        phone: currentUser.phone,
        status: currentUser.status
      }
      if (currentUser.password != null && String(currentUser.password).trim() !== '') {
        updateData.password = currentUser.password.trim()
      }
      
      const response = await request.put('/admin/staff/update', updateData)
      
      if (response.code === 0 || response.code === 200) {
        ElMessage.success('更新成功')
        userDialogVisible.value = false
        loadUsers()
      } else {
        ElMessage.error(response.msg || '更新失败')
      }
    } else {
      // 创建账号
      // 确保用户名和姓名都有值
      if (!currentUser.username || !currentUser.username.trim()) {
        ElMessage.warning('请输入用户名（用于登录）')
        saving.value = false
        return
      }
      if (!currentUser.name || !currentUser.name.trim()) {
        ElMessage.warning('请输入姓名（人员姓名）')
        saving.value = false
        return
      }
      
      const toNull = (v) => (v === '' || v === undefined) ? null : v
      const createServiceStoreId = currentUser.role === 'admin' ? null : toNull(currentUser.serviceStoreId)
      if ((currentUser.role === 'staff' || currentUser.role === 'store_admin') && !createServiceStoreId) {
        ElMessage.warning(currentUser.role === 'store_admin' ? '分店管理员必须绑定服务门店' : '服务岗位账号必须绑定服务门店')
        saving.value = false
        return
      }
      const createData = {
        username: currentUser.username.trim(), // 用户名：用于登录
        password: currentUser.password,
        name: currentUser.name.trim(), // 姓名：人员姓名
        role: currentUser.role,
        serviceStoreId: createServiceStoreId,
        doctorId: null,
        email: currentUser.email ? currentUser.email.trim() : null,
        phone: currentUser.phone ? currentUser.phone.trim() : null,
        status: currentUser.status
      }
      
      console.log('创建账号数据:', createData)
      
      const response = await request.post('/admin/staff/add', createData)
      
      if (response.code === 0 || response.code === 200) {
        ElMessage.success('创建成功')
        userDialogVisible.value = false
        // 重置表单
        Object.assign(currentUser, {
          id: null,
          username: "",
          name: "",
          password: "",
          role: "",
          serviceStoreId: null,
          doctorId: null,
          email: "",
          phone: "",
          status: 1
        })
        // 延迟一下再刷新，确保后端数据已保存
        setTimeout(() => {
          loadUsers()
        }, 300)
      } else {
        // 显示更详细的错误信息
        const errorMsg = response.msg || '创建失败'
        ElMessage.error(errorMsg)
        console.error('创建账号失败:', response)
      }
    }
  } catch (error) {
    if (error !== false) {
      console.error('保存账号失败:', error)
    }
  } finally {
    saving.value = false
  }
}

// 加载角色列表（仅用于账号管理中的角色选择）
const loadRoles = async () => {
  try {
    // 系统内置角色（包含医师角色）
    const systemRoles = [
      { id: 2, roleCode: 'admin', roleName: '平台管理员', description: '可管理全部门店与全局权限', status: 1, createTime: '-' },
      { id: 4, roleCode: 'store_admin', roleName: '分店管理员', description: '仅管理绑定门店的数据与权限', status: 1, createTime: '-' },
      { id: 3, roleCode: 'staff', roleName: '服务人员', description: '门店服务执行人员', status: 1, createTime: '-' }
    ]
    
    roles.value = systemRoles
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 初始化
onMounted(async () => {
  await loadServiceStores()
  loadUsers()
  loadRoles() // 仅用于账号管理中的角色选择下拉框
})

// keep-alive 下从其他页面切回时重新加载，避免显示缓存的错误角色（医师/管理员被显示成员工）
onActivated(() => {
  loadUsers()
})
</script>

<style lang="scss" scoped>
.permissions-container {
  padding: 20px;

  .el-tag {
    margin-bottom: 5px;
  }
}
</style>
