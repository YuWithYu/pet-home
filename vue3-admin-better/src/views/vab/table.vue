<template>
  <div class="table-container">
    <el-card shadow="never">
      <el-table v-loading="listLoading" :data="list" style="width: 100%">
        <!-- 宠物管理表格列 -->
        <template v-if="isPetPage">
          <el-table-column label="ID" prop="id" width="80" />
          <el-table-column label="宠物名称" prop="name" show-overflow-tooltip />
          <el-table-column label="品种" prop="breed" show-overflow-tooltip />
          <el-table-column label="性别" prop="gender" width="80">
            <template #default="{ row }">
              <el-tag :type="row.gender === '1' || row.gender === 'male' ? 'primary' : 'success'">
                {{ row.gender === '1' || row.gender === 'male' ? '公' : '母' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="年龄" prop="age" width="80" />
          <el-table-column label="体重(kg)" prop="weight" width="100" />
          <el-table-column label="颜色" prop="color" show-overflow-tooltip />
          <el-table-column label="创建时间" prop="createTime" width="160" />
        </template>
        
        <!-- 商品管理表格列 -->
        <template v-else-if="isProductPage">
          <el-table-column label="ID" prop="id" width="80" />
          <el-table-column label="商品名称" prop="name" show-overflow-tooltip />
          <el-table-column label="分类" prop="category" show-overflow-tooltip />
          <el-table-column label="价格" prop="price" width="100">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column label="库存" prop="stock" width="80" />
          <el-table-column label="描述" prop="description" show-overflow-tooltip />
          <el-table-column label="创建时间" prop="createTime" width="160" />
        </template>
        
        <!-- 用户管理表格列 -->
        <template v-else-if="isUserPage">
          <el-table-column label="ID" prop="id" width="80" />
          <el-table-column label="用户名" prop="title" show-overflow-tooltip />
          <el-table-column label="状态" prop="status" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '活跃' ? 'success' : 'warning'">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="页面浏览量" prop="pageViews" />
          <el-table-column label="日期" prop="datetime" width="160" />
        </template>
        
        <!-- 默认表格列 -->
        <template v-else>
          <el-table-column label="ID" prop="id" />
          <el-table-column label="标题" prop="title" show-overflow-tooltip />
          <el-table-column label="作者" prop="author" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status | statusFilter">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="页面浏览量" prop="pageViews" />
          <el-table-column label="日期" prop="datetime" width="160" />
        </template>
        
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="{ row }">
            <el-button type="text" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空数据提示 -->
      <div v-if="!listLoading && list.length === 0" class="empty-data">
        <el-empty description="暂无数据">
          <el-button type="primary" @click="getList">刷新数据</el-button>
        </el-empty>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:currentPage="listQuery.pageNo"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-size="listQuery.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="totalCount"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
// 根据当前路由动态选择API
import { petApi, productApi, userApi } from "@/api/pet-home";

export default {
  name: "Table",
  filters: {
    statusFilter(status) {
      const statusMap = {
        published: "success",
        draft: "info",
        deleted: "danger",
      };
      return statusMap[status];
    },
  },
  data() {
    return {
      list: null,
      totalCount: 0,
      listLoading: true,
      listQuery: {
        pageNo: 1,
        pageSize: 20,
        title: undefined,
      },
    };
  },
  computed: {
    isPetPage() {
      return this.$route.path.includes('/pets/');
    },
    isProductPage() {
      return this.$route.path.includes('/mall/') || this.$route.path.includes('/products');
    },
    isUserPage() {
      return this.$route.path.includes('/users/');
    }
  },
  created() {
    this.getList();
  },
  methods: {
    async getList() {
      this.listLoading = true;
      
      try {
        // 根据当前路由选择对应的API
        let apiCall;
        const currentPath = this.$route.path;
        
        if (currentPath.includes('/pets/')) {
          apiCall = petApi.getList(this.listQuery);
        } else if (currentPath.includes('/mall/') || currentPath.includes('/products')) {
          apiCall = productApi.getList(this.listQuery);
        } else if (currentPath.includes('/users/')) {
          // 用户API暂时使用模拟数据
          this.showMockData();
          return;
        } else {
          // 默认使用宠物API
          apiCall = petApi.getList(this.listQuery);
        }
        
        const response = await apiCall;
        if (response.code === 200 || response.code === 0) {
          // 后端返回的数据格式：{ records: [...], total: number }
          this.list = response.data.records || response.data || [];
          this.totalCount = response.data.total || response.data.length || 0;
        } else {
          // 如果API失败，显示模拟数据
          this.showMockData();
        }
      } catch (error) {
        console.error('获取数据失败:', error);
        // 显示模拟数据而不是错误
        this.showMockData();
        this.$message.warning('正在显示演示数据，请确保后端服务正常运行');
      } finally {
        this.listLoading = false;
      }
    },
    handleEdit(row) {
      this.$message.info("编辑操作：" + row.title);
    },
    handleDelete(row) {
      this.$message.info("删除操作：" + row.title);
    },
    handleSizeChange(val) {
      this.listQuery.pageSize = val;
      this.getList();
    },
    handleCurrentChange(val) {
      this.listQuery.pageNo = val;
      this.getList();
    },
    showMockData() {
      // 根据当前路由显示不同的模拟数据
      const currentPath = this.$route.path;
      
      if (currentPath.includes('/pets/')) {
        this.list = [
          {
            id: 1,
            title: "小白",
            status: "健康",
            pageViews: 100,
            datetime: "2024-01-01 10:00:00"
          },
          {
            id: 2,
            title: "小黑",
            status: "健康",
            pageViews: 150,
            datetime: "2024-01-02 11:00:00"
          }
        ];
      } else if (currentPath.includes('/shop/') || currentPath.includes('/products')) {
        this.list = [
          {
            id: 1,
            title: "狗粮",
            status: "在售",
            pageViews: 200,
            datetime: "2024-01-01 10:00:00"
          },
          {
            id: 2,
            title: "猫砂",
            status: "在售",
            pageViews: 180,
            datetime: "2024-01-02 11:00:00"
          }
        ];
      } else if (currentPath.includes('/users/')) {
        this.list = [
          {
            id: 1,
            title: "张三",
            status: "活跃",
            pageViews: 50,
            datetime: "2024-01-01 10:00:00"
          },
          {
            id: 2,
            title: "李四",
            status: "活跃",
            pageViews: 75,
            datetime: "2024-01-02 11:00:00"
          }
        ];
      } else {
        this.list = [
          {
            id: 1,
            title: "示例数据1",
            status: "正常",
            pageViews: 100,
            datetime: "2024-01-01 10:00:00"
          },
          {
            id: 2,
            title: "示例数据2",
            status: "正常",
            pageViews: 150,
            datetime: "2024-01-02 11:00:00"
          }
        ];
      }
      this.totalCount = this.list.length;
    },
  },
};
</script>

<style lang="scss" scoped>
.table-container {
  padding: 20px;

  .pagination-container {
    margin-top: 20px;
    text-align: center;
  }
  
  .empty-data {
    margin: 40px 0;
    text-align: center;
  }
}
</style>
