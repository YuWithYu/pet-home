<template>
  <div class="customer-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜索用户..."
              clearable
              style="width: 200px; margin-right: 10px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddCustomerDialog">添加用户</el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="filteredCustomers" 
        style="width: 100%"
        row-key="id"
        v-loading="loading"
      >
        <el-table-column prop="name" label="用户昵称" min-width="150">
          <template #default="{ row }">
            <div class="customer-name">
              <el-avatar :size="32" :src="avatarSrc(row.originalUser?.avatar)" :style="{ backgroundColor: getAvatarColor(row.name) }">
                {{ row.name.charAt(0).toUpperCase() }}
              </el-avatar>
              <span style="margin-left: 10px">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="账号" width="120">
          <template #default="{ row }">
            {{ row.originalUser?.username || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column label="积分" width="100">
          <template #default="{ row }">
            {{ row.originalUser?.points || 0 }}g
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="text" @click="viewCustomer(row)">查看</el-button>
            <el-button type="text" @click="editCustomer(row)">编辑</el-button>
            <el-button type="text" @click="deleteCustomer(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalCustomers"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog 
      v-model="customerDialogVisible" 
      :title="editingCustomer ? '编辑用户' : '添加用户'"
      width="600px"
    >
      <el-form
        ref="customerFormRef"
        :model="customerForm"
        :rules="customerRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="name">
              <el-input v-model="customerForm.name" />
            </el-form-item>
            
            <el-form-item label="账号" prop="username">
              <el-input v-model="customerForm.username" :disabled="!!editingCustomer" />
            </el-form-item>
            
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="customerForm.phone" />
            </el-form-item>
            <el-form-item v-if="!editingCustomer" label="登录密码" prop="password">
              <el-input v-model="customerForm.password" type="password" placeholder="不填则默认123456" show-password />
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="积分" prop="points">
              <el-input-number 
                v-model="customerForm.points" 
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="状态" prop="status">
              <el-select v-model="customerForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="正常" :value="1"></el-option>
                <el-option label="禁用" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="customerDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveCustomer"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 用户详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="用户详情"
      width="600px"
    >
      <el-row :gutter="20">
        <el-col :span="16">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户昵称">{{ detailCustomer.name }}</el-descriptions-item>
            <el-descriptions-item label="账号">{{ detailCustomer.originalUser?.username || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detailCustomer.phone || '未设置' }}</el-descriptions-item>
            <el-descriptions-item label="积分">{{ detailCustomer.originalUser?.points || 0 }}g</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailCustomer.originalUser?.status === 1 ? 'success' : 'danger'">
                {{ detailCustomer.originalUser?.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detailCustomer.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
        <el-col :span="8" class="detail-avatar-col">
          <el-avatar 
            :size="100" 
            :src="avatarSrc(detailCustomer.originalUser?.avatar)" 
            :style="{ backgroundColor: getAvatarColor(detailCustomer.name) }"
          >
            {{ (detailCustomer.name || '').charAt(0).toUpperCase() }}
          </el-avatar>
        </el-col>
      </el-row>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="editCustomer(detailCustomer)">编辑</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Search } from "@element-plus/icons-vue";
import { getUserList, deleteUser, updateUser, updateUserStatus, getUserById } from "@/api/pet.js";
import { register } from "@/api/user.js";
import { baseURL } from "@/config";

export default {
  name: "Customer",
  components: {
    Search
  },
  data() {
    return {
      searchText: "",
      currentPage: 1,
      pageSize: 10,
      totalCustomers: 0,
      loading: false,
      customerDialogVisible: false,
      detailDialogVisible: false,
      editingCustomer: null,
      customers: [],
      regionOptions: [
        {
          value: "北京市",
          label: "北京市",
          children: [
            {
              value: "北京市",
              label: "北京市",
              children: [
                { value: "东城区", label: "东城区" },
                { value: "西城区", label: "西城区" },
                { value: "朝阳区", label: "朝阳区" },
                { value: "海淀区", label: "海淀区" },
                { value: "大兴区", label: "大兴区" }
              ]
            }
          ]
        },
        {
          value: "广东省",
          label: "广东省",
          children: [
            {
              value: "深圳市",
              label: "深圳市",
              children: [
                { value: "南山区", label: "南山区" },
                { value: "福田区", label: "福田区" },
                { value: "宝安区", label: "宝安区" }
              ]
            },
            {
              value: "广州市",
              label: "广州市",
              children: [
                { value: "天河区", label: "天河区" },
                { value: "越秀区", label: "越秀区" }
              ]
            }
          ]
        }
      ],
      customerForm: {
        name: "",
        username: "",
        phone: "",
        password: "",
        points: 0,
        status: 1
      },
      detailCustomer: {},
      customerRules: {
        name: [
          { required: true, message: "请输入用户昵称", trigger: "blur" }
        ],
        username: [
          { required: false, message: "账号可选（微信用户可为空）", trigger: "blur" }
        ],
        phone: [
          { required: true, message: "请输入联系电话", trigger: "blur" }
        ],
        points: [
          { required: true, message: "请设置积分", trigger: "change" }
        ]
      }
    };
  },
  computed: {
    filteredCustomers() {
      let result = this.customers;
      
      // 搜索过滤
      if (this.searchText) {
        result = result.filter(customer => 
          (customer.name && customer.name.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (customer.contact && customer.contact.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (customer.phone && customer.phone.includes(this.searchText)) ||
          (customer.nickname && customer.nickname.toLowerCase().includes(this.searchText.toLowerCase())) ||
          (customer.username && customer.username.includes(this.searchText))
        );
      }
      
      // 分页处理
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return result.slice(start, end);
    }
  },
  mounted() {
    this.loadUsers();
  },
  methods: {
    async loadUsers() {
      this.loading = true;
      try {
        const res = await getUserList({
          page: this.currentPage,
          pageSize: this.pageSize
        });
        
        if (res.code === 200 || res.code === 0) {
          // 将后端返回的User实体映射到前端需要的格式
          this.customers = (res.data?.users || res.data?.records || []).map(user => ({
            id: user.id,
            name: user.nickname || user.username || '未设置昵称',
            contact: user.nickname || user.username || '未设置',
            phone: user.phone || user.username || '未设置',
            region: [], // 后端User实体暂无地区字段
            address: '', // 后端User实体暂无地址字段
            createTime: user.createTime ? new Date(user.createTime).toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            }).replace(/\//g, '-') : '未知',
            // 保留原始用户数据，用于编辑
            originalUser: user
          }));
          
          this.totalCustomers = res.data?.total || res.data?.totalElements || this.customers.length;
        } else {
          this.$message.error(res.msg || '获取用户列表失败');
        }
      } catch (error) {
        console.error('加载用户列表失败:', error);
        this.$message.error('加载用户列表失败，请重试');
      } finally {
        this.loading = false;
      }
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.loadUsers();
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      this.loadUsers();
    },
    getRegionText(region) {
      return region ? region.join(" - ") : "";
    },
    getAvatarColor(name) {
      const colors = ["#409EFF", "#67C23A", "#E6A23C", "#F56C6C", "#909399"];
      let hash = 0;
      for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
      }
      const index = Math.abs(hash) % colors.length;
      return colors[index];
    },
    showAddCustomerDialog() {
      this.editingCustomer = null;
      this.customerForm = {
        name: "",
        username: "",
        phone: "",
        password: "",
        points: 0,
        status: 1
      };
      this.customerDialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.customerFormRef) {
          this.$refs.customerFormRef.resetFields();
        }
      });
    },
    editCustomer(customer) {
      this.editingCustomer = customer;
      const originalUser = customer.originalUser || customer;
      this.customerForm = {
        id: originalUser.id,
        name: originalUser.nickname || originalUser.name || '',
        username: originalUser.username || '',
        phone: originalUser.phone || '',
        points: originalUser.points || 0,
        status: originalUser.status !== undefined ? Number(originalUser.status) : 1
      };
      this.customerDialogVisible = true;
      this.detailDialogVisible = false;
    },
    viewCustomer(customer) {
      this.detailCustomer = { ...customer };
      this.detailDialogVisible = true;
    },
    deleteCustomer(customer) {
      this.$confirm(`确定要删除用户"${customer.name}"吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(async () => {
        try {
          const res = await deleteUser(customer.id);
          if (res.code === 200 || res.code === 0) {
            this.$message.success("用户删除成功");
            await this.loadUsers();
          } else {
            this.$message.error(res.msg || "删除失败");
          }
        } catch (error) {
          console.error("删除用户失败:", error);
          this.$message.error(error?.message || "删除用户失败，请重试");
        }
      }).catch(() => {
        this.$message.info("已取消删除");
      });
    },
    /** 头像地址：与当前页同源，避免 HTTPS 页请求 localhost 导致 Mixed Content */
    avatarSrc(avatar) {
      if (!avatar || typeof avatar !== "string") return "";
      const raw = avatar.trim();
      const base = (baseURL || "").trim().replace(/\/api\/?$/, "") || (typeof window !== "undefined" ? window.location.origin : "");
      if (!base) return raw;
      if (raw.startsWith("http://") || raw.startsWith("https://")) {
        if (/localhost|127\.0\.0\.1/.test(raw)) return raw.replace(/^https?:\/\/[^/]+/, base);
        return raw;
      }
      return base + (raw.startsWith("/") ? raw : "/" + raw);
    },
    async saveCustomer() {
      this.$refs.customerFormRef.validate(async (valid) => {
        if (!valid) return;
        try {
          if (this.editingCustomer) {
            const orig = this.editingCustomer.originalUser || {};
            const body = {
              id: this.customerForm.id,
              nickname: (this.customerForm.name || "").trim() || orig.nickname,
              username: (this.customerForm.username || "").trim() || null,
              phone: (this.customerForm.phone || "").trim() || orig.phone,
              points: this.customerForm.points,
              status: this.customerForm.status,
              avatar: orig.avatar,
              backgroundImage: orig.backgroundImage,
              signature: orig.signature,
              createTime: orig.createTime
            };
            const res = await updateUser(this.customerForm.id, body);
            if (res.code === 200 || res.code === 0) {
              // 状态单独持久化，确保“禁用”不被其他字段更新覆盖
              await updateUserStatus(this.customerForm.id, Number(this.customerForm.status) === 1 ? 1 : 0);
              this.$message.success("用户信息更新成功");
              this.customerDialogVisible = false;
              await this.loadUsers();
            } else {
              this.$message.error(res.msg || "更新失败");
            }
          } else {
            const phone = (this.customerForm.phone || "").trim();
            const nickname = (this.customerForm.name || "").trim();
            if (!phone) {
              this.$message.warning("请输入手机号");
              return;
            }
            if (!nickname) {
              this.$message.warning("请输入用户昵称");
              return;
            }
            const password = (this.customerForm.password || "").trim() || "123456";
            const res = await register({
              phone,
              password,
              nickname
            });
            if (res.code !== 200 && res.code !== 0) {
              this.$message.error(res.msg || "添加失败");
              return;
            }
            const userId = res.data && (res.data.userId || res.data.uid);
            if (userId && (this.customerForm.points !== 0 || this.customerForm.status !== 1)) {
              try {
                const uRes = await getUserById(userId);
                const userData = (uRes && uRes.data) ? uRes.data : (uRes && uRes.code === 200 ? uRes : null);
                if (userData && userData.id) {
                  const body = { ...userData, points: this.customerForm.points, status: this.customerForm.status };
                  delete body.password;
                  await updateUser(userId, body);
                  await updateUserStatus(userId, Number(this.customerForm.status) === 1 ? 1 : 0);
                }
              } catch (e) {
                console.warn("设置积分/状态失败", e);
              }
            }
            this.$message.success("用户添加成功");
            this.customerDialogVisible = false;
            await this.loadUsers();
          }
        } catch (error) {
          console.error("保存用户失败:", error);
          this.$message.error(error?.response?.data?.msg || error?.message || "保存失败，请重试");
        }
      });
    },
  }
};
</script>

<style lang="scss" scoped>
.customer-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }
  
  .customer-name {
    display: flex;
    align-items: center;
  }
  
  .detail-avatar-col {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
}
</style>