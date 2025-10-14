<template>
  <div class="hot-products-management">
    <el-card class="box-card">
      <template #header>
        <div class="clearfix">
        <span>🔥 热门推荐管理</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
        </div>
      </template>

      <!-- 当前热门商品 -->
      <div class="section">
        <h3>当前热门推荐商品</h3>
        <el-row :gutter="20" v-loading="hotProductsLoading">
          <el-col :span="6" v-for="product in hotProducts" :key="product.id">
            <el-card class="product-card hot-card">
              <div class="product-info">
                <div class="product-name">{{ product.name }}</div>
                <div class="product-category">{{ product.category }}</div>
                <div class="product-price">¥{{ product.price }}</div>
              </div>
              <div class="product-actions">
                <el-button size="mini" type="danger" @click="removeFromHot(product.id)">
                  取消推荐
                </el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6" v-if="hotProducts.length === 0">
            <el-empty description="暂无热门推荐商品"></el-empty>
          </el-col>
        </el-row>
      </div>

      <!-- 所有商品管理 -->
      <div class="section">
        <h3>所有商品管理</h3>
        
        <!-- 批量操作 -->
        <div class="batch-controls">
          <el-select v-model="batchAction" placeholder="选择批量操作" style="width: 150px; margin-right: 10px;">
            <el-option label="设为推荐" value="setHot"></el-option>
            <el-option label="取消推荐" value="removeHot"></el-option>
          </el-select>
          <el-button type="primary" @click="batchOperation" :disabled="selectedProducts.length === 0">
            批量操作 ({{ selectedProducts.length }})
          </el-button>
          <el-button @click="loadAllProducts">
            <i class="el-icon-refresh"></i> 刷新商品
          </el-button>
        </div>

        <!-- 商品列表 -->
        <el-table 
          :data="allProducts" 
          v-loading="allProductsLoading"
          @selection-change="handleSelectionChange"
          style="width: 100%"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="name" label="商品名称" min-width="200"></el-table-column>
          <el-table-column prop="category" label="分类" width="120"></el-table-column>
          <el-table-column prop="price" label="价格" width="100">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="80"></el-table-column>
          <el-table-column prop="isHot" label="推荐状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.isHot ? 'success' : 'info'">
                {{ scope.row.isHot ? '推荐' : '普通' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <div style="display: flex; flex-direction: column; gap: 5px; align-items: center; justify-content: center; width: 100%;">
                <el-button 
                  size="mini" 
                  type="success" 
                  @click="setHotStatus(scope.row.id, true)"
                  :disabled="scope.row.isHot"
                  style="width: 80px;"
                >
                  设为推荐
                </el-button>
                <el-button 
                  size="mini" 
                  type="danger" 
                  @click="setHotStatus(scope.row.id, false)"
                  :disabled="!scope.row.isHot"
                  style="width: 80px;"
                >
                  取消推荐
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalProducts"
          style="margin-top: 20px; text-align: right;"
        >
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getHotProducts, getAllProducts, setProductHot, batchSetHotProducts } from '@/api/pet-home'

export default {
  name: 'HotProductsManagement',
  data() {
    return {
      hotProducts: [],
      allProducts: [],
      selectedProducts: [],
      batchAction: '',
      hotProductsLoading: false,
      allProductsLoading: false,
      currentPage: 1,
      pageSize: 20,
      totalProducts: 0
    }
  },
  mounted() {
    this.loadHotProducts()
    this.loadAllProducts()
  },
  methods: {
    // 加载热门商品
    async loadHotProducts() {
      this.hotProductsLoading = true
      try {
        const response = await getHotProducts(50)
        if (response.code === 200 || response.code === 0) {
          this.hotProducts = response.data || []
        }
      } catch (error) {
        this.$message.error('加载热门商品失败: ' + error.message)
      } finally {
        this.hotProductsLoading = false
      }
    },

    // 加载所有商品
    async loadAllProducts() {
      this.allProductsLoading = true
      try {
        const response = await getAllProducts({
          page: this.currentPage,
          size: this.pageSize
        })
        if ((response.code === 200 || response.code === 0) && response.data) {
          this.allProducts = response.data.records || []
          this.totalProducts = response.data.total || 0
        }
      } catch (error) {
        this.$message.error('加载商品列表失败: ' + error.message)
      } finally {
        this.allProductsLoading = false
      }
    },

    // 设置热门状态
    async setHotStatus(productId, isHot) {
      try {
        const response = await setProductHot(productId, isHot)
        if (response.code === 200 || response.code === 0) {
          this.$message.success(isHot ? '设为热门成功' : '取消热门成功')
          this.loadHotProducts()
          this.loadAllProducts()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        this.$message.error('操作失败: ' + error.message)
      }
    },

    // 从热门推荐中移除商品
    async removeFromHot(productId) {
      try {
        const response = await setProductHot(productId, false)
        if (response.code === 200 || response.code === 0) {
          this.$message.success('已从热门推荐中移除')
          this.loadHotProducts()
          this.loadAllProducts()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        this.$message.error('操作失败: ' + error.message)
      }
    },

    // 批量操作
    async batchOperation() {
      if (this.selectedProducts.length === 0) {
        this.$message.warning('请选择要操作的商品')
        return
      }
      if (!this.batchAction) {
        this.$message.warning('请选择批量操作类型')
        return
      }

      const isHot = this.batchAction === 'setHot'
      const productIds = this.selectedProducts.map(p => p.id)

      try {
        const response = await batchSetHotProducts({
          productIds,
          isHot
        })
        if (response.code === 200 || response.code === 0) {
          this.$message.success('批量操作成功')
          this.loadHotProducts()
          this.loadAllProducts()
          this.selectedProducts = []
        } else {
          this.$message.error(response.message || '批量操作失败')
        }
      } catch (error) {
        this.$message.error('批量操作失败: ' + error.message)
      }
    },

    // 表格选择变化
    handleSelectionChange(selection) {
      this.selectedProducts = selection
    },

    // 分页大小变化
    handleSizeChange(val) {
      this.pageSize = val
      this.loadAllProducts()
    },

    // 当前页变化
    handleCurrentChange(val) {
      this.currentPage = val
      this.loadAllProducts()
    },

    // 刷新数据
    refreshData() {
      this.loadHotProducts()
      this.loadAllProducts()
    }
  }
}
</script>

<style scoped>
.hot-products-management {
  padding: 20px;
}

.section {
  margin-bottom: 30px;
}

.section h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
}

.product-card {
  position: relative;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.hot-card {
  border: 2px solid #ff6b35;
  background: linear-gradient(135deg, #fff5f0 0%, #ffffff 100%);
}


.product-info {
  padding: 10px 0;
}

.product-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
  font-size: 14px;
}

.product-category {
  color: #909399;
  font-size: 12px;
  margin-bottom: 5px;
}

.product-price {
  color: #ff6b35;
  font-weight: bold;
  font-size: 16px;
}

.product-actions {
  text-align: center;
  padding-top: 10px;
}

.batch-controls {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both;
}
</style>
