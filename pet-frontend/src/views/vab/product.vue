<template>
  <div class="product-container">
    <el-tabs v-model="mallTab" class="mall-tabs">
      <el-tab-pane label="商品" name="product">
        <el-card shadow="never">
      <template #header>
        <div class="card-header header-actions-only">
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜索商品..."
              clearable
              style="width: 200px; margin-right: 10px"
            >
              <template #prefix>
                <el-icon>🔍</el-icon>
              </template>
            </el-input>
            <el-select
              v-model="selectedCategory"
              placeholder="选择分类"
              clearable
              style="width: 150px; margin-right: 10px"
              @change="handleCategoryFilter"
            >
              <el-option label="全部分类" value=""></el-option>
              <el-option
                v-for="category in categories.filter(c => c.name !== '积分商城')"
                :key="category.id"
                :label="category.name"
                :value="category.name"
              />
            </el-select>
            <el-button @click="openCategoryManageDialog">添加分类</el-button>
            <el-button type="primary" @click="showAddProductDialog">添加商品</el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="filteredProducts"
        style="width: 100%"
        row-key="id"
        v-loading="loading"
      >
            <el-table-column prop="name" label="产品名称" min-width="200">
              <template #default="{ row }">
                <div class="product-name">
                  <el-image 
                    :key="`product-${row.id}-${imageUpdateTimestamp}`"
                    :src="getImageUrl(row)" 
                    fit="cover" 
                    style="width: 40px; height: 40px; border-radius: 4px"
                    @error="handleImageError"
                    @load="handleImageLoad"
                  />
                  <span style="margin-left: 10px">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="产品分类" width="120" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                ¥{{ row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="100">
              <template #default="{ row }">
                <el-tag :type="getStockType(row.stock)">
                  {{ row.stock }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="推荐状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.isHot ? 'success' : 'info'">
                  {{ row.isHot ? "推荐" : "普通" }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="240">
              <template #default="{ row }">
                <el-button type="text" @click="viewProduct(row)">查看</el-button>
                <el-button type="text" @click="editProduct(row)">编辑</el-button>
                <el-button
                  type="text"
                  :style="{ color: row.isHot ? '#f56c6c' : '#67c23a' }"
                  @click="toggleRecommendStatus(row)"
                >
                  {{ row.isHot ? "取消推荐" : "推荐" }}
                </el-button>
                <el-button type="text" @click="deleteProduct(row)">删除</el-button>
              </template>
            </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalProducts"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="订单" name="order">
        <OrderManagement />
      </el-tab-pane>
    </el-tabs>
    
    <!-- 添加/编辑产品对话框 -->
    <el-dialog 
      v-model="productDialogVisible" 
      :title="editingProduct ? '编辑产品' : '添加产品'"
      width="800px"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称" prop="name">
              <el-input v-model="productForm.name" />
            </el-form-item>
            
            <el-form-item label="产品分类" prop="category">
              <el-select v-model="productForm.category" placeholder="请选择产品分类" style="width: 100%">
                <el-option
                  v-for="category in categories.filter(c => c.name !== '积分商城')"
                  :key="category.id"
                  :label="category.name"
                  :value="category.name"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="价格" prop="price">
              <el-input-number 
                v-model="productForm.price" 
                :min="0"
                :step="0.01"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="库存" prop="stock">
              <el-input-number 
                v-model="productForm.stock" 
                :min="0"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="状态" prop="status">
              <el-select v-model="productForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="在售" :value="1"></el-option>
                <el-option label="下架" :value="0"></el-option>
                <el-option label="缺货" :value="-1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="产品图片">
              <el-upload
                class="avatar-uploader"
                :action="uploadAction"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleImageSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeImageUpload"
              >
                <img 
                  v-if="productForm.image" 
                  :src="getImageUrl({ image: productForm.image })" 
                  class="avatar" 
                  alt="Product Image"
                  style="width: 178px; height: 178px; object-fit: cover; border: 1px solid #ddd; display: block;"
                  @load="console.log('产品图片加载成功:', productForm.image)"
                  @error="console.log('产品图片加载失败:', productForm.image)"
                />
                <el-icon v-else class="avatar-uploader-icon">+</el-icon>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="产品描述" prop="description">
              <el-input 
                v-model="productForm.description" 
                type="textarea"
                :rows="5"
                placeholder="请输入产品描述"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 下半区：购买类型、服务承诺、详情图片 占满整行，左右均衡 -->
        <el-row :gutter="20" style="margin-top: 16px;">
          <el-col :span="24">
            <!-- 购买类型（规格）与添加规格按钮同一行，标签加宽避免溢出 -->
            <el-form-item label="购买类型（规格）" label-width="130px" class="product-spec-label-inline">
              <div class="product-spec-actions">
                <el-button type="primary" size="small" @click="addSpec">添加规格</el-button>
              </div>
              <div class="product-spec-cards">
                <div v-for="(spec, index) in productForm.specs" :key="index" class="product-spec-card">
                  <el-row :gutter="10">
                    <el-col :span="8">
                      <el-input 
                        v-model="spec.name" 
                        placeholder="规格名称（如：口味、组合、尺码）"
                        size="small"
                      />
                    </el-col>
                    <el-col :span="14">
                      <el-input 
                        v-model="spec.values" 
                        placeholder="规格值，多个用逗号分隔（如：500g,1kg,2kg）"
                        size="small"
                        @input="onSpecValuesInput(index)"
                        @blur="syncSpecValueDetails(index)"
                      />
                    </el-col>
                    <el-col :span="2">
                      <el-button type="danger" size="small" @click="removeSpec(index)">删除</el-button>
                    </el-col>
                  </el-row>
                  <div v-if="hasSpecValues(spec)" class="spec-value-details">
                    <div class="spec-value-details-title">为各规格值设置价格/图片（选填）</div>
                    <div v-for="(v, vi) in getSpecValueList(spec)" :key="vi" class="spec-value-row">
                      <el-input v-model="v.name" placeholder="名称" size="small" style="width: 120px;" />
                      <el-input-number v-model="v.price" :min="0" :precision="2" size="small" placeholder="价格" style="width: 110px; margin-left: 8px;" controls-position="right" />
                      <el-upload
                        :action="uploadAction"
                        :headers="uploadHeaders"
                        :show-file-list="false"
                        :on-success="(res, file, list) => handleSpecValueImageSuccess(res, index, vi)"
                        :before-upload="beforeImageUpload"
                        class="spec-value-upload"
                      >
                        <el-button size="small" type="default" style="margin-left: 8px;">{{ v.image ? '更换图片' : '上传图片' }}</el-button>
                      </el-upload>
                      <img v-if="v.image" :src="getImageUrl({ image: v.image })" class="spec-value-preview" />
                    </div>
                  </div>
                </div>
                <div v-if="!productForm.specs || productForm.specs.length === 0" class="product-spec-empty-tip">
                  提示：添加规格后，用户可以在商品详情页选择不同的规格选项；可为每个款式单独设价格、上传图片
                </div>
              </div>
            </el-form-item>

            <el-form-item label="服务承诺">
              <el-checkbox-group v-model="productForm.services">
                <el-checkbox label="七天无理由退货">七天无理由退货</el-checkbox>
                <el-checkbox label="退货包运费">退货包运费</el-checkbox>
                <el-checkbox label="48小时内发货">48小时内发货</el-checkbox>
                <el-checkbox label="免费包邮">免费包邮</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-form-item label="详情图片">
              <el-upload
                class="detail-images-uploader"
                :action="uploadAction"
                :headers="uploadHeaders"
                :file-list="detailImageList"
                :on-success="handleDetailImageSuccess"
                :on-error="handleDetailImageError"
                :on-remove="handleDetailImageRemove"
                :before-upload="beforeDetailImageUpload"
                multiple
                list-type="picture-card"
              >
                <el-icon class="avatar-uploader-icon">+</el-icon>
              </el-upload>
              <div class="upload-tip">支持多张图片，建议尺寸：800x600px</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="productDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveProduct"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 产品详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="产品详情"
      width="800px"
    >
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="product-image-detail">
            <el-image 
              :src="getImageUrl(detailProduct)" 
              fit="cover" 
              style="width: 100%; height: 300px; border-radius: 8px"
              @error="handleImageError"
            />
          </div>
        </el-col>
        <el-col :span="16">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="产品名称">{{ detailProduct.name }}</el-descriptions-item>
            <el-descriptions-item label="产品分类">{{ detailProduct.category }}</el-descriptions-item>
            <el-descriptions-item label="价格">
              <span class="product-price">¥{{ detailProduct.price }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="库存">
              <el-tag :type="getStockType(detailProduct.stock)">
                {{ detailProduct.stock }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(detailProduct.status)">
                {{ getStatusText(detailProduct.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detailProduct.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
      
      <el-divider>产品描述</el-divider>
      
      <div class="product-description">
        {{ detailProduct.description }}
      </div>
      
      <el-tabs v-model="productActiveTab" style="margin-top: 20px">
        <el-tab-pane :label="`销售信息 (${productSales.length})`" name="sales">
          <div class="sales-summary">
            <el-statistic title="销售笔数" :value="productSales.length" />
            <el-statistic title="累计销量" :value="salesStats.totalQuantity" />
            <el-statistic title="累计销售额" :value="'¥' + formatAmount(salesStats.totalAmount)" />
          </div>
          <el-table :data="productSales" style="width: 100%" empty-text="暂无销售数据">
            <el-table-column prop="orderId" label="订单号" min-width="180" show-overflow-tooltip />
            <el-table-column prop="customer" label="客户" min-width="120" show-overflow-tooltip />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="amount" label="金额" width="140">
              <template #default="{ row }">
                ¥{{ formatAmount(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="date" label="销售时间" min-width="180" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane :label="`评价信息 (${productReviews.length})`" name="reviews">
          <div v-if="productReviews.length === 0" class="review-empty-wrap">
            <el-empty description="暂无评价数据" />
          </div>
          <div v-else class="review-list">
            <div v-for="review in productReviews" :key="review.id || review.date + review.customer" class="review-card">
              <div class="review-header">
                <div class="review-user">
                  <el-avatar :src="review.userAvatar || '/static/images/garfield-default-avatar.png'" :size="36" />
                  <div class="review-user-meta">
                    <div class="name">{{ review.customer }}</div>
                    <div class="meta">{{ formatReviewDate(review.date) }}</div>
                  </div>
                </div>
                <div class="review-score">
                  <el-rate v-model="review.rating" disabled text-color="#ff9900" />
                  <span class="score-text">{{ review.rating }} 分</span>
                </div>
              </div>
              <div class="review-content">{{ review.content || "用户未填写评价内容" }}</div>
              <div v-if="review.images && review.images.length > 0" class="review-media-row">
                <el-image
                  v-for="img in review.images"
                  :key="img"
                  :src="img"
                  fit="cover"
                  :preview-src-list="review.images"
                  preview-teleported
                  class="review-image"
                />
              </div>
              <div v-if="review.videos && review.videos.length > 0" class="review-media-row">
                <video v-for="video in review.videos" :key="video" :src="video" controls class="review-video" />
              </div>
              <div class="review-footer">
                <span>点赞 {{ review.likeCount || 0 }}</span>
                <span>回复 {{ review.replyCount || 0 }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="editProduct(detailProduct)">编辑</el-button>
        </span>
      </template>
    </el-dialog>
    
    <el-dialog
      v-model="categoryDialogVisible"
      title="分类管理"
      width="680px"
      destroy-on-close
    >
      <el-form :inline="true" @submit.native.prevent>
        <el-form-item label="新分类">
          <el-input
            v-model="categoryInput"
            placeholder="例如：猫咪零食"
            maxlength="30"
            style="width: 320px;"
            @keyup.enter.native="quickAddCategory"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="quickAddCategory">添加</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="categories.filter(c => c.name !== '积分商城')" v-loading="categoryLoading" size="small" style="width: 100%; margin-top: 10px;">
        <el-table-column prop="name" label="分类" min-width="220" />
        <el-table-column label="商品数量" width="120" align="center">
          <template #default="{ row }">
            {{ row.productCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button type="text" size="small" style="color:#f56c6c" @click="deleteCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { baseURL } from "@/config";
import { productApi } from "@/api/pet-home";
import OrderManagement from "@/views/vab/order.vue";

// 与接口同源，避免 :8443/HTTPS 证书导致图片 400；部署到线上时优先用当前域名，保证图片能加载
function getImageBase() {
  if (typeof window !== "undefined" && window.location && window.location.origin &&
      !window.location.origin.includes("localhost") && !window.location.origin.includes("127.0.0.1")) {
    return window.location.origin;
  }
  const u = (baseURL || "").trim().replace(/\/api\/?$/, "");
  if (u && (u.startsWith("http://") || u.startsWith("https://"))) return u;
  return "http://localhost:8080";
}

export default {
  name: "Product",
  components: {
    OrderManagement
  },
  data() {
    return {
      mallTab: "product",
      productActiveTab: "sales",
      searchText: "",
      selectedCategory: "",
      currentPage: 1,
      pageSize: 10,
      totalProducts: 0,
      loading: false,
      productDialogVisible: false,
      detailDialogVisible: false,
      categoryDialogVisible: false,
      editingProduct: null,
      categoryInput: "",
      imageUpdateTimestamp: 0,
      products: [],
      allProducts: [],
      categories: [],
      categoryLoading: false,
      productForm: {
        name: "",
        category: "",
        price: 0,
        stock: 0,
        status: 1,
        image: "",
        description: "",
        detailImages: [],
        services: [],
        specs: []
      },
      detailProduct: {},
      productRules: {
        name: [
          { required: true, message: "请输入产品名称", trigger: "blur" }
        ],
        category: [
          { required: true, message: "请选择产品分类", trigger: "change" }
        ],
        price: [
          { required: true, message: "请输入价格", trigger: "blur" }
        ],
        stock: [
          { required: true, message: "请输入库存", trigger: "blur" }
        ],
        status: [
          { required: true, message: "请选择状态", trigger: "change" }
        ]
      },
      productSales: [],
      productReviews: [],
      detailImageList: [], // 详情图片列表
    };
  },
  computed: {
    uploadAction() {
      return (getImageBase() || "http://localhost:8080") + "/api/admin/upload?type=product";
    },
    baseUrl() {
      return (getImageBase() || "http://localhost:8080") + "/static/product/";
    },
    uploadHeaders() {
      // 动态获取token，支持多种存储方式
      const token = 
        localStorage.getItem('vue-admin-better-2024') || 
        sessionStorage.getItem('vue-admin-better-2024') || 
        localStorage.getItem('token') || 
        sessionStorage.getItem('token') || 
        '';
      
      return {
        "Authorization": token ? `Bearer ${token}` : ""
      };
    },
    filteredProducts() {
      // 排除积分商城商品
      let result = (this.allProducts || []).filter(product => product.category !== "积分商城");
      
      // 搜索过滤
      if (this.searchText) {
        result = result.filter(product => 
          product.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
          product.category.toLowerCase().includes(this.searchText.toLowerCase())
        );
      }
      
      // 分类过滤
      if (this.selectedCategory) {
        result = result.filter(product => 
          product.category === this.selectedCategory
        );
      }
      
      // 分页处理
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return result.slice(start, end);
    },
    salesStats() {
      return (this.productSales || []).reduce(
        (acc, item) => {
          const q = Number(item.quantity || 0);
          const a = Number(item.amount || 0);
          acc.totalQuantity += Number.isNaN(q) ? 0 : q;
          acc.totalAmount += Number.isNaN(a) ? 0 : a;
          return acc;
        },
        { totalQuantity: 0, totalAmount: 0 }
      );
    }
  },
  async created() {
    await this.loadProducts();
    this.loadCategories();
  },
  watch: {
    // 监听产品数据变化，强制更新图片
    allProducts: {
      handler(newVal, oldVal) {
        if (newVal && oldVal && newVal.length > 0) {
          console.log('产品数据发生变化，强制更新图片');
          this.imageUpdateTimestamp = Date.now();
        }
      },
      deep: true
    },
    // 规格值一有变化就同步 valueDetails，保证下面“价格/图片”行立刻能显示（不依赖 @input 时机）
    'productForm.specs': {
      handler() {
        if (!this.productForm.specs || !Array.isArray(this.productForm.specs)) return;
        this.productForm.specs.forEach((spec) => {
          const raw = (spec.values || '').trim();
          if (!raw) return;
          const names = this.parseSpecValuesRaw(raw);
          if (!names.length) return;
          const details = spec.valueDetails || [];
          if (details.length !== names.length) {
            const newDetails = names.map((name, i) => {
              const exist = details[i] || details.find(d => (d.name || '').trim() === name);
              return {
                name,
                price: exist && (exist.price !== undefined && exist.price !== null && exist.price !== '') ? exist.price : undefined,
                image: exist && (exist.image || exist.pic) ? (exist.image || exist.pic) : ''
              };
            });
            spec.valueDetails = newDetails;
          }
        });
      },
      deep: true
    }
  },
  methods: {
    // 获取图片URL：统一用与接口同源的 base 拼接，不直接用后端完整 URL（避免 :8443/证书 400）
      getImageUrl(item) {
        if (!item) return getImageBase() + '/static/images/garfield-default-avatar.png';
        let imagePath = item.image || item.imageUrl || '';
        if (!imagePath || typeof imagePath !== 'string') return getImageBase() + '/static/images/garfield-default-avatar.png';
        const raw = imagePath.trim();
        if (!raw) return getImageBase() + '/static/images/garfield-default-avatar.png';
        // 若后端仍返回完整 URL，只取路径部分再用 base 拼
        if (raw.startsWith('http://') || raw.startsWith('https://')) {
          try {
            const pathStart = raw.indexOf('/', raw.indexOf('://') + 3);
            if (pathStart > 0) imagePath = raw.substring(pathStart);
            else imagePath = raw;
          } catch (e) {
            imagePath = raw;
          }
        } else {
          imagePath = raw;
        }
        const base = getImageBase();
        if (imagePath.startsWith('/')) return base + imagePath;
        if (imagePath.startsWith('upload/') || imagePath.startsWith('upload')) return base + '/' + imagePath.replace(/^\/+/, '');
        if (imagePath.startsWith('/uploads/') || imagePath.startsWith('/static/')) {
          const filename = imagePath.substring(imagePath.lastIndexOf('/') + 1);
          return base + '/upload/product/' + filename;
        }
        if (imagePath.startsWith('product/')) return base + '/upload/' + imagePath;
        return base + '/upload/product/' + imagePath.replace(/^\/+/, '');
      },
    formatAmount(amount) {
      if (!amount) return '0.00';
      const num = typeof amount === 'string' ? parseFloat(amount) : amount;
      if (isNaN(num)) return '0.00';
      return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      },
    normalizeMediaList(input) {
      if (!input) return [];
      let arr = [];
      if (Array.isArray(input)) {
        arr = input;
      } else if (typeof input === "string") {
        const text = input.trim();
        if (!text) return [];
        if (text.startsWith("[")) {
          try {
            const parsed = JSON.parse(text);
            if (Array.isArray(parsed)) arr = parsed;
          } catch (e) {
            arr = text.split(",");
          }
        } else {
          arr = text.split(",");
        }
      }
      return arr
        .map((s) => (s == null ? "" : String(s).trim()))
        .filter(Boolean)
        .map((url) => {
          if (url.startsWith("http://") || url.startsWith("https://")) return url;
          const fixed = url.startsWith("/") ? url : "/" + url;
          return getImageBase() + fixed;
        });
    },
    formatReviewDate(dateText) {
      if (!dateText) return "-";
      return String(dateText).replace("T", " ");
    },
    addSpec() {
      if (!this.productForm.specs) {
        this.productForm.specs = [];
      }
      this.productForm.specs.push({
        name: '',
        values: '',
        valueDetails: []
      });
    },
    removeSpec(index) {
      this.productForm.specs.splice(index, 1);
    },
    /** 规格值主输入：支持英文逗号、中文逗号顿号、空格等分隔（与占位提示一致） */
    parseSpecValuesRaw(raw) {
      if (!raw || typeof raw !== 'string') return [];
      return raw
        .split(/[,，、\s]+/)
        .map((s) => s.trim())
        .filter(Boolean);
    },
    // 仅做只读推导，不在此方法内写 spec，避免渲染期改数据导致规格区消失
    getSpecValueList(spec) {
      const raw = (spec.values || '').trim();
      if (!raw) return [];
      const names = this.parseSpecValuesRaw(raw);
      if (!names.length) return [];
      const details = spec.valueDetails;
      if (Array.isArray(details) && details.length === names.length) {
        return details;
      }
      return [];
    },
    // 是否有规格值（用于显示“为各规格值设置价格/图片”区域，失焦后才可编辑）
    hasSpecValues(spec) {
      const raw = (spec.values || '').trim();
      if (!raw) return false;
      return this.parseSpecValuesRaw(raw).length > 0;
    },
    // 保存时用：从 spec.values 与 valueDetails 合并得到列表（即使用户未失焦也能保存规格值）
    getSpecValueListForSave(spec) {
      const raw = (spec.values || '').trim();
      if (!raw) return [];
      const names = this.parseSpecValuesRaw(raw);
      const details = spec.valueDetails || [];
      return names.map((name, i) => {
        const exist = details[i] || details.find(d => (d.name || '').trim() === name);
        return {
          name,
          price: exist && (exist.price !== undefined && exist.price !== null && exist.price !== '') ? exist.price : undefined,
          image: exist && (exist.image || exist.pic) ? (exist.image || exist.pic) : ''
        };
      });
    },
    // 规格值输入时（在 nextTick 中）同步 valueDetails，使下方“价格/图片”行立即显示，无需失焦
    onSpecValuesInput(index) {
      this.$nextTick(() => this.syncSpecValueDetails(index));
    },
    // 规格值输入框失焦时，同步 valueDetails 与 values 的对应关系（创建/更新 valueDetails）
    syncSpecValueDetails(index) {
      const spec = this.productForm.specs[index];
      if (!spec) return;
      const raw = (spec.values || '').trim();
      if (!raw) return;
      const names = this.parseSpecValuesRaw(raw);
      const oldDetails = spec.valueDetails || [];
      const details = names.map((name, i) => {
        const exist = oldDetails.find(d => (d.name || '').trim() === name) || oldDetails[i];
        return {
          name,
          price: exist && (exist.price !== undefined && exist.price !== null && exist.price !== '') ? exist.price : undefined,
          image: exist && (exist.image || exist.pic) ? (exist.image || exist.pic) : ''
        };
      });
      spec.valueDetails = details;
    },
    // 某个规格值的图片上传成功
    handleSpecValueImageSuccess(response, specIndex, valueIndex) {
      const spec = this.productForm.specs[specIndex];
      if (!spec) return;
      const url = (response && response.data) ? response.data : (response && response.url) ? response.url : '';
      if (!url) return;
      const list = this.getSpecValueList(spec);
      if (!list[valueIndex]) return;
      list[valueIndex].image = url;
      spec.valueDetails = [...list];
    },
    async loadProducts() {
      try {
        this.loading = true;
        console.log('开始加载产品数据...');
        
        // 获取所有商品数据，不分页
        const response = await productApi.getList({
          page: 1,
          size: 1000  // 获取足够多的数据
        });
        
        console.log('API响应:', response);
        
        if ((response.code === 200 || response.code === 0) && response.data) {
          // 处理分页数据
          if (response.data.records) {
            console.log('处理分页数据，记录数:', response.data.records.length);
            // 获取所有商品数据，排除积分商城商品
            this.allProducts = response.data.records
              .filter(item => item.category !== "积分商城")
              .map(item => {
                console.log('处理商品项:', item);
                console.log('商品项的image字段:', item.image);
                console.log('商品项的imageUrl字段:', item.imageUrl);
                console.log('商品项的specs字段:', item.specs);
                console.log('商品项的services字段:', item.services);
                
                const product = {
                  id: item.id,
                  name: item.name,
                  category: item.category,
                  price: item.price,
                  stock: item.stock,
                  status: item.stock > 0 ? 1 : -1,
                  isHot: item.isHot === true || item.isHot === 1,
                  image: item.image, // 使用原始图片字段
                  imageUrl: item.imageUrl, // 保存完整的图片URL
                  description: item.description || "暂无描述",
                  createTime: item.createTime,
                  // 确保specs和services字段被正确传递
                  specs: item.specs || null,
                  services: item.services || null,
                  detailImages: item.detailImages || null
                };
                console.log('处理后的商品项:', product);
                console.log('处理后的商品项 - specs:', product.specs);
                console.log('处理后的商品项 - services:', product.services);
                return product;
              });
            this.totalProducts = this.allProducts.length;
          } else if (Array.isArray(response.data)) {
            console.log('处理数组数据，记录数:', response.data.length);
            // 直接数组响应格式，排除积分商城商品
            this.allProducts = response.data
              .filter(item => item.category !== "积分商城")
              .map(item => {
                console.log('处理商品项:', item);
                console.log('商品项的image字段:', item.image);
                console.log('商品项的imageUrl字段:', item.imageUrl);
                
                const product = {
                  id: item.id,
                  name: item.name,
                  category: item.category,
                  price: item.price,
                  stock: item.stock,
                  status: item.stock > 0 ? 1 : -1,
                  isHot: item.isHot === true || item.isHot === 1,
                  image: item.image, // 使用原始图片字段
                  imageUrl: item.imageUrl, // 保存完整的图片URL
                  description: item.description || "暂无描述",
                  createTime: item.createTime
                };
                console.log('处理后的商品项:', product);
                return product;
              });
            this.totalProducts = this.allProducts.length;
          }
          
          console.log('加载完成，产品列表:', this.allProducts);
        } else {
          console.error('API响应格式错误:', response);
          this.$message.error('数据格式错误');
        }
      } catch (error) {
        console.error('加载商品数据失败:', error);
        this.$message.error('加载商品数据失败: ' + error.message);
      } finally {
        this.loading = false;
      }
    },
    async loadCategories() {
      try {
        this.categoryLoading = true;
        // 从后端API获取分类列表
        const response = await productApi.getAllCategories();
        if (response && response.data) {
        this.categories = response.data.map(category => ({
          id: category.id,
          name: category.name,
          productCount: (this.allProducts || []).filter(p => p.category === category.name).length,
          status: category.status === 1 ? 'active' : 'inactive',
          createTime: category.createTime || new Date().toLocaleString()
        }));
        }
      } catch (error) {
        console.error('加载分类数据失败:', error);
        // 如果API调用失败，回退到从商品中提取分类
        const uniqueCategories = [...new Set((this.allProducts || []).map(product => product.category))];
        this.categories = uniqueCategories.map((categoryName, index) => ({
          id: index + 1,
          name: categoryName,
          description: `${categoryName}相关产品`,
          productCount: (this.allProducts || []).filter(p => p.category === categoryName).length,
          status: "active",
          createTime: new Date().toLocaleString()
        }));
      } finally {
        this.categoryLoading = false;
      }
    },
    handleCategoryFilter() {
      // 分类筛选时重置到第一页
      this.currentPage = 1;
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
    },
    handleCurrentChange(val) {
      this.currentPage = val;
    },
    getStatusText(status) {
      const statusMap = {
        1: "在售",
        0: "下架",
        [-1]: "缺货"
      };
      return statusMap[status] || status;
    },
    getStatusType(status) {
      const typeMap = {
        1: "success",
        0: "info",
        [-1]: "danger"
      };
      return typeMap[status] || "info";
    },
    getStockType(stock) {
      if (stock === 0) {
        return "danger";
      } else if (stock < 10) {
        return "warning";
      }
      return "success";
    },
    showAddProductDialog() {
      this.editingProduct = null;
      this.productForm = {
        name: "",
        category: "",
        price: 0,
        stock: 0,
        status: 1,
        image: "",
        description: "",
        detailImages: [],
        services: [], // 服务承诺
        specs: [] // 购买类型（规格）
      };
      this.detailImageList = [];
      this.productDialogVisible = true;
      this.$nextTick(() => {
        this.$refs.productFormRef.resetFields();
      });
    },
    editProduct(product) {
      this.editingProduct = product;
      
      // 解析详情图片数据
      let detailImages = [];
      
      try {
        if (product.detailImages) {
          detailImages = typeof product.detailImages === 'string' ? JSON.parse(product.detailImages) : product.detailImages;
        }
      } catch (error) {
        console.error('解析商品详情数据失败:', error);
      }
      
      // 解析服务承诺
      let services = [];
      try {
        if (product.services) {
          services = typeof product.services === 'string' ? JSON.parse(product.services) : product.services;
        }
      } catch (error) {
        // 如果没有services字段，使用默认值
        services = [];
      }
      
      // 解析购买类型（规格）
      let specs = [];
      try {
        console.log('编辑产品 - 原始specs字段:', product.specs);
        console.log('编辑产品 - specs类型:', typeof product.specs);
        if (product.specs) {
          const specsData = typeof product.specs === 'string' ? JSON.parse(product.specs) : product.specs;
          console.log('编辑产品 - 解析后的specs数据:', specsData);
          if (Array.isArray(specsData)) {
            specs = specsData.map(spec => {
              const vals = spec.values;
              const isValueObjects = Array.isArray(vals) && vals.length > 0 && typeof vals[0] === 'object' && vals[0] !== null && 'name' in vals[0];
              const valueDetails = isValueObjects ? vals.map(o => ({ name: o.name || '', price: o.price, image: o.image || o.pic || '' })) : [];
              const valuesStr = Array.isArray(vals)
                ? (isValueObjects ? vals.map(o => o.name || '').join(',') : vals.map(v => typeof v === 'string' ? v : (v && v.name) ? v.name : '').join(','))
                : (spec.values || '');
              return {
                name: spec.name || '',
                values: valuesStr,
                valueDetails: valueDetails.length ? valueDetails : []
              };
            });
            console.log('编辑产品 - 转换后的specs:', specs);
          }
        } else {
          console.log('编辑产品 - specs字段为空或不存在');
        }
      } catch (error) {
        console.error('解析购买类型失败:', error);
        specs = [];
      }
      
      this.productForm = { 
        ...product,
        // 确保图片URL正确显示 - 使用统一的图片URL处理方法
        image: this.getImageUrl(product),
        detailImages: detailImages,
        services: services,
        specs: specs
      };
      
      // 设置详情图片列表（用于显示，需要完整URL）
      this.detailImageList = detailImages.map((img, index) => {
        // 如果是相对路径，转换为完整URL用于显示
        const displayUrl = img.startsWith('http://') || img.startsWith('https://') 
          ? img 
          : this.getImageUrl({ image: img });
        return {
          name: `detail-${index}.jpg`,
          url: displayUrl
        };
      });
      
      console.log('编辑产品 - 原始图片:', product.image);
      console.log('编辑产品 - 构造后的图片URL:', this.productForm.image);
      console.log('编辑产品 - 原始detailImages字段:', product.detailImages);
      console.log('编辑产品 - detailImages类型:', typeof product.detailImages);
      console.log('编辑产品 - 解析后的detailImages:', detailImages);
      console.log('编辑产品 - detailImages数组长度:', detailImages.length);
      
      this.productDialogVisible = true;
      this.detailDialogVisible = false;
    },
    async viewProduct(product) {
      this.detailProduct = { 
        ...product,
        // 确保图片URL正确显示 - 使用统一的图片URL处理方法
        image: this.getImageUrl(product)
      };
      console.log('查看产品 - 原始图片:', product.image);
      console.log('查看产品 - 构造后的图片URL:', this.detailProduct.image);
      this.detailDialogVisible = true;
      
      // 重置tab到销售记录
      this.productActiveTab = "sales";
      
      // 加载真实的销售记录和评价数据
      await this.loadProductSalesAndReviews(product.id);
    },
    async loadProductSalesAndReviews(productId) {
      try {
        // 加载销售记录
        console.log('开始加载商品销售记录 - 商品ID:', productId);
        const salesResponse = await productApi.getSalesRecords(productId);
        console.log('销售记录API响应:', salesResponse);
        if (salesResponse && (salesResponse.code === 200 || salesResponse.code === 0)) {
          const salesData = salesResponse.data;
          console.log('销售记录数据:', salesData);
          if (salesData && Array.isArray(salesData) && salesData.length > 0) {
            this.productSales = salesData.map(record => ({
              orderId: record.orderId || '',
              customer: record.customer || '未知用户',
              quantity: record.quantity || 0,
              amount: record.amount || 0,
              date: this.formatReviewDate(record.date || '')
            }));
            console.log('处理后的销售记录列表:', this.productSales);
          } else {
            console.log('销售记录数据为空或不是数组');
            this.productSales = [];
          }
        } else {
          console.warn('销售记录API响应格式不正确:', salesResponse);
          this.productSales = [];
        }
        
        // 加载评价
        console.log('开始加载商品评价 - 商品ID:', productId);
        const reviewsResponse = await productApi.getReviews(productId);
        console.log('评价API响应:', reviewsResponse);
        if (reviewsResponse && (reviewsResponse.code === 200 || reviewsResponse.code === 0)) {
          const reviewsData = reviewsResponse.data;
          console.log('评价数据:', reviewsData);
          if (reviewsData && Array.isArray(reviewsData) && reviewsData.length > 0) {
            this.productReviews = reviewsData.map(review => ({
              id: review.id,
              customer: review.userName || review.customer || '匿名用户',
              userAvatar: this.getImageUrl({ image: review.userAvatar || "" }),
              rating: Number(review.rating || 5),
              content: review.comment || review.content || '暂无评价内容',
              date: this.formatReviewDate(review.createTime || review.date || ''),
              likeCount: review.likeCount || 0,
              replyCount: review.replyCount || 0,
              images: this.normalizeMediaList(review.images),
              videos: this.normalizeMediaList(review.videos)
            }));
            console.log('处理后的评价列表:', this.productReviews);
          } else {
            console.log('评价数据为空或不是数组');
            this.productReviews = [];
          }
        } else {
          console.warn('评价API响应格式不正确:', reviewsResponse);
          this.productReviews = [];
        }
      } catch (error) {
        console.error('加载商品销售记录和评价失败:', error);
        this.productSales = [];
        this.productReviews = [];
        this.$message.warning('加载商品销售记录和评价失败');
      }
    },
    async deleteProduct(product) {
      try {
        await this.$confirm(`确定要删除产品"${product.name}"吗？`, "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        });
        
        // 调用后端API删除商品
        const response = await productApi.delete(product.id);
        // 后端返回的 code 是 200 表示成功，500 表示失败
        if (response.code === 200) {
          // 从前端数组中移除商品
          const index = (this.allProducts || []).findIndex(p => p.id === product.id);
          if (index !== -1) {
            this.allProducts.splice(index, 1);
            this.totalProducts = this.allProducts.length;
          }
          this.$message.success("产品删除成功");
        } else {
          this.$message.error("产品删除失败: " + (response.msg || response.message || "未知错误"));
        }
      } catch (error) {
        if (error === 'cancel') {
          this.$message.info("已取消删除");
        } else {
          console.error('删除产品失败:', error);
          this.$message.error("删除产品失败，请重试");
        }
      }
    },
    async toggleRecommendStatus(product) {
      try {
        const targetIsHot = !product.isHot;
        const response = await productApi.setProductHot(product.id, targetIsHot);
        if (response.code === 200 || response.code === 0) {
          product.isHot = targetIsHot;
          this.$message.success(targetIsHot ? "已设为推荐" : "已取消推荐");
        } else {
          this.$message.error(response.msg || response.message || "操作失败");
        }
      } catch (error) {
        console.error("更新推荐状态失败:", error);
        this.$message.error("更新推荐状态失败，请重试");
      }
    },
    async saveProduct() {
      try {
        await this.$refs.productFormRef.validate();
        
        // 准备商品数据
        console.log('保存产品 - 当前productForm.image:', this.productForm.image);
        // 提取图片相对路径（保留 product/ 前缀）
        let imagePath = null;
        if (this.productForm.image) {
          if (this.productForm.image.startsWith('http')) {
            // 从完整URL中提取相对路径（如: product/xxx.jpg）
            const url = this.productForm.image;
            // 找到 /upload/ 后面的部分
            const uploadIndex = url.indexOf('/upload/');
            if (uploadIndex !== -1) {
              imagePath = url.substring(uploadIndex + 8); // 8 = '/upload/'.length
            } else {
              // 如果没有 /upload/，只提取文件名
              const urlParts = url.split('/');
              const filename = urlParts[urlParts.length - 1];
              imagePath = 'product/' + filename;
            }
          } else if (this.productForm.image.startsWith('product/')) {
            // 如果已经是 product/ 开头，直接使用
            imagePath = this.productForm.image;
          } else {
            // 否则添加 product/ 前缀
            imagePath = 'product/' + this.productForm.image;
          }
        }
        console.log('保存产品 - 提取的相对路径:', imagePath);
        console.log('保存产品 - 原始URL:', this.productForm.image);
        
        // 处理服务承诺
        let servicesValue = "";
        if (this.productForm.services && this.productForm.services.length > 0) {
          servicesValue = JSON.stringify(this.productForm.services);
        }
        
        // 处理购买类型（规格）
        let specsValue = "";
        if (this.productForm.specs && this.productForm.specs.length > 0) {
          // 将规格数据转换为标准格式；若某规格有“各款式价格/图片”，则 values 存为对象数组
          const specsData = this.productForm.specs.map(spec => {
            let cleanName = (spec.name || '').trim().replace(/^["']|["']$/g, '');
            const list = this.getSpecValueListForSave(spec);
            const hasPriceOrImage = list.some(v => (v.price !== undefined && v.price !== null && v.price !== '') || (v.image && v.image.trim()));
            const values = hasPriceOrImage
              ? list.map(v => ({
                  name: (v.name || '').trim().replace(/^["']|["']$/g, ''),
                  ...(v.price !== undefined && v.price !== null && v.price !== '' ? { price: Number(v.price) } : {}),
                  ...(v.image && v.image.trim() ? { image: v.image.trim() } : {})
                })).filter(v => v.name)
              : list.map(v => (v.name || '').trim().replace(/^["']|["']$/g, '')).filter(Boolean);
            return { name: cleanName, values };
          }).filter(spec => spec.name && (Array.isArray(spec.values) ? spec.values.length > 0 : true));
          if (specsData.length > 0) {
            specsValue = JSON.stringify(specsData);
            console.log('保存产品 - 处理后的specs数据:', specsData);
            console.log('保存产品 - JSON字符串:', specsValue);
          }
        }
        
        const productData = {
          id: this.editingProduct ? this.editingProduct.id : undefined,
          name: this.productForm.name,
          category: this.productForm.category,
          price: this.productForm.price,
          stock: this.productForm.stock,
          status: this.productForm.status,
          description: this.productForm.description,
          image: imagePath,
          detailImages: JSON.stringify(this.productForm.detailImages),
          services: servicesValue || "",
          specs: specsValue || ""
        };
        
        console.log('保存产品 - 发送的数据:', productData);
        console.log('保存产品 - detailImages值 (字符串):', productData.detailImages);
        console.log('保存产品 - detailImages值 (解析后):', productData.detailImages ? JSON.parse(productData.detailImages) : null);
        console.log('保存产品 - specs值 (字符串):', productData.specs);
        console.log('保存产品 - specs值 (解析后):', productData.specs ? JSON.parse(productData.specs) : null);
        console.log('保存产品 - services值 (字符串):', productData.services);
        console.log('保存产品 - services值 (解析后):', productData.services ? JSON.parse(productData.services) : null);
        
        if (this.editingProduct) {
          // 编辑产品 - 调用后端API
          const response = await productApi.update(this.editingProduct.id, productData);
          
          console.log('保存产品 - API响应:', response);
          console.log('保存产品 - 响应数据:', response.data);
          if (response.data) {
            console.log('保存产品 - 响应中的specs:', response.data.specs);
            console.log('保存产品 - 响应中的services:', response.data.services);
          }
          
          if (response.code === 0 || response.code === 200) {
            this.$message.success("产品信息更新成功");
            console.log('产品更新成功，重新加载产品列表');
            console.log('更新前的产品列表长度:', this.allProducts.length);
            
            // 强制刷新产品列表
            await this.loadProducts();
            console.log('重新加载后的产品列表长度:', this.allProducts.length);
            
            // 查找更新后的产品，检查图片URL和数据
            const updatedProduct = this.allProducts.find(p => p.id === this.editingProduct.id);
            if (updatedProduct) {
              console.log('=== 产品更新后调试信息 ===');
              console.log('更新后的产品信息:', updatedProduct);
              console.log('更新后的产品图片字段:', updatedProduct.image);
              console.log('更新后的产品imageUrl字段:', updatedProduct.imageUrl);
              console.log('更新后的产品specs字段:', updatedProduct.specs);
              console.log('更新后的产品services字段:', updatedProduct.services);
              console.log('使用getImageUrl处理后的URL:', this.getImageUrl(updatedProduct));
              console.log('========================');
            } else {
              console.error('未找到更新后的产品，产品ID:', this.editingProduct.id);
              console.log('当前所有产品:', this.allProducts);
            }
            
            // 更新时间戳，强制刷新图片
            this.imageUpdateTimestamp = Date.now();
            
            // 强制更新视图，确保图片立即显示
            this.$nextTick(() => {
              console.log('强制更新视图');
              this.$forceUpdate();
            });
            
            // 关闭对话框
            this.productDialogVisible = false;
          } else {
            const errorMsg = response.message || response.msg || "未知错误";
            this.$message.error("产品更新失败: " + errorMsg);
            console.error('产品更新失败:', response);
          }
        } else {
          // 添加产品 - 调用后端API
          const response = await productApi.create(productData);
          if (response.code === 0 || response.code === 200) {
            this.$message.success("产品添加成功");
            console.log('产品添加成功，重新加载产品列表');
            
            // 强制刷新产品列表
            await this.loadProducts();
            
            // 更新时间戳，强制刷新图片
            this.imageUpdateTimestamp = Date.now();
            
            // 强制更新视图，确保图片立即显示
            this.$nextTick(() => {
              this.$forceUpdate();
            });
            
            // 关闭对话框
            this.productDialogVisible = false;
          } else {
            const errorMsg = response.message || response.msg || "未知错误";
            this.$message.error("产品添加失败: " + errorMsg);
            console.error('产品添加失败:', response);
          }
        }
        
        this.productDialogVisible = false;
      } catch (error) {
        console.error('保存产品失败:', error);
        this.$message.error('保存产品失败，请重试');
      }
    },
    handleImageSuccess(response, file) {
      console.log('=== 图片上传成功回调 ===');
      console.log('完整响应:', response);
      console.log('响应code:', response?.code);
      console.log('响应data:', response?.data);
      console.log('响应data类型:', typeof response?.data);
      
      // 支持code为200或0的情况
      if (response && (response.code === 200 || response.code === 0)) {
        // 后端返回的可能是完整URL或相对路径
        let imageUrl = response.data;
        
        // 确保 imageUrl 是字符串类型
        if (typeof imageUrl !== 'string') {
          console.log('响应data不是字符串，尝试转换');
          // 如果是对象，尝试获取fileName、url或path属性
          if (imageUrl && typeof imageUrl === 'object') {
            imageUrl = imageUrl.fileName || imageUrl.url || imageUrl.path || imageUrl.fileUrl || '';
            console.log('从对象中提取的路径:', imageUrl);
          } else {
            imageUrl = String(imageUrl || '');
          }
        }
        
        console.log('原始图片URL:', imageUrl);
        
        // 统一用与接口同源的 base 拼接，避免 :8443 导致加载失败
        if (imageUrl) {
          const base = getImageBase();
          if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
            try {
              const pathStart = imageUrl.indexOf('/', imageUrl.indexOf('://') + 3);
              this.productForm.image = pathStart > 0 ? base + imageUrl.substring(pathStart) : imageUrl;
            } catch (e) {
              this.productForm.image = imageUrl;
            }
          } else if (imageUrl.startsWith('/')) {
            this.productForm.image = base + imageUrl;
          } else {
            this.productForm.image = base + '/upload/' + imageUrl.replace(/^\/+/, '');
          }
        } else {
          this.$message.error('图片URL为空');
          return;
        }
        
        console.log('设置productForm.image为:', this.productForm.image);
        this.$message.success('图片上传成功');
        
        // 强制更新视图
        this.$nextTick(() => {
          console.log('强制更新视图后的productForm.image:', this.productForm.image);
          this.$forceUpdate();
        });
      } else {
        console.error('上传失败响应:', response);
        this.$message.error('图片上传失败: ' + (response?.msg || '未知错误'));
      }
    },
    handleUploadError(error, file) {
      console.error('图片上传失败:', error);
      this.$message.error('图片上传失败，请重试');
    },
    beforeImageUpload(file) {
      const isJPG = file.type === "image/jpeg" || file.type === "image/png" || file.type === "image/gif" || file.type === "image/webp";
      const isLt5M = file.size / 1024 / 1024 < 5;
      
      if (!isJPG) {
        this.$message.error("产品图片只能是 JPG、PNG、GIF 或 WEBP 格式!");
        return false;
      }
      if (!isLt5M) {
        this.$message.error("产品图片大小不能超过 5MB!");
        return false;
      }
      return true;
    },
    
    // 分类管理方法
    openCategoryManageDialog() {
      this.categoryInput = "";
      this.categoryDialogVisible = true;
      this.loadCategories();
    },

    async quickAddCategory() {
      const name = (this.categoryInput || "").trim();
      if (!name) {
        this.$message.warning("请输入分类名称");
        return;
      }
      if (name === "积分商城") {
        this.$message.warning("该分类为系统保留分类");
        return;
      }
      try {
        await productApi.addCategory({
          name,
          status: 1
        });
        this.$message.success("分类添加成功");
        this.categoryInput = "";
        await this.loadCategories();
      } catch (error) {
        console.error("添加分类失败:", error);
        this.$message.error("添加分类失败: " + (error.message || error));
      }
    },
    
    async deleteCategory(category) {
      this.$confirm(`确定删除分类"${category.name}"吗？`, '确认删除', {
        type: 'warning'
      }).then(async () => {
        try {
          // 调用后端API删除分类
          await productApi.deleteCategory(category.id);
          this.$message.success("分类删除成功");
          // 重新加载分类列表
          await this.loadCategories();
        } catch (error) {
          console.error('删除分类失败:', error);
          this.$message.error("删除分类失败: " + (error.message || error));
        }
      }).catch(() => {
        // 用户取消删除
      });
    },
    
    // 图片加载事件处理
    handleImageError(event) {
      if (event && event.target && event.target.src) {
        console.log('图片加载失败:', event.target.src);
      } else {
        console.log('图片加载失败:', event);
      }
      // 可以在这里设置默认图片
    },
    
    handleImageLoad(event) {
      if (event && event.target && event.target.src) {
        console.log('图片加载成功:', event.target.src);
      }
    },
    
    // 测试方法：检查产品图片URL
    testProductImageUrl(productId) {
      const product = this.allProducts.find(p => p.id === productId);
      if (product) {
        console.log('=== 产品图片URL测试 ===');
        console.log('产品ID:', product.id);
        console.log('产品名称:', product.name);
        console.log('原始image字段:', product.image);
        console.log('处理后的URL:', this.getImageUrl(product));
        console.log('=====================');
      }
    },
    
    // 详情图片上传成功
    handleDetailImageSuccess(response, file, fileList) {
      console.log('详情图片上传成功:', response);
      console.log('上传返回的data:', response.data);
      console.log('上传返回的code:', response.code);
      console.log('上传返回的完整响应:', JSON.stringify(response));
      console.log('当前productForm.detailImages:', this.productForm.detailImages);
      
      // 兼容不同的响应格式：code === 0 或 code === 200 或没有code字段
      if (response && (response.code === 0 || response.code === 200 || !response.hasOwnProperty('code'))) {
        // 保存相对路径到数组（后端会转换为完整URL）
        // response.data 可能是完整URL或相对路径
        // 如果 response.data 不存在，尝试使用 response.url 或其他字段
        let imagePath = response.data || response.url || response.path || '';
        
        if (!imagePath) {
          console.error('无法获取图片路径，响应数据:', response);
          this.$message.error('上传成功但无法获取图片路径');
          return;
        }
        
        // 如果是完整URL，提取相对路径
        if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
          // 提取 /upload/ 之后的部分（包括 /upload/）
          const uploadIndex = imagePath.indexOf('/upload/');
          if (uploadIndex !== -1) {
            // 保留 /upload/ 之后的部分，去掉开头的 /
            imagePath = imagePath.substring(uploadIndex + 1); // 结果如: "upload/product/xxx.jpg"
            // 如果后端期望的是 "product/xxx.jpg"，则进一步处理
            if (imagePath.startsWith('upload/product/')) {
              imagePath = imagePath.substring('upload/product/'.length); // 结果: "xxx.jpg"
              imagePath = 'product/' + imagePath; // 结果: "product/xxx.jpg"
            }
          }
        } else if (imagePath.startsWith('/upload/')) {
          // 去掉开头的 /，结果如: "upload/product/xxx.jpg"
          imagePath = imagePath.substring(1);
          // 如果后端期望的是 "product/xxx.jpg"，则进一步处理
          if (imagePath.startsWith('upload/product/')) {
            imagePath = imagePath.substring('upload/product/'.length);
            imagePath = 'product/' + imagePath;
          }
        } else if (imagePath.startsWith('upload/product/')) {
          // 已经是 "upload/product/xxx.jpg"，转换为 "product/xxx.jpg"
          imagePath = imagePath.substring('upload/product/'.length);
          imagePath = 'product/' + imagePath;
        } else if (!imagePath.startsWith('product/')) {
          // 如果是纯文件名，添加 "product/" 前缀
          imagePath = 'product/' + imagePath;
        }
        
        console.log('保存的详情图片路径:', imagePath);
        console.log('保存前的detailImages数组:', JSON.parse(JSON.stringify(this.productForm.detailImages)));
        
        this.productForm.detailImages.push(imagePath);
        
        console.log('保存后的detailImages数组:', JSON.parse(JSON.stringify(this.productForm.detailImages)));
        
        // 更新显示列表（需要完整URL用于显示）
        const displayUrl = this.getImageUrl({ image: imagePath });
        this.detailImageList.push({
          name: file.name || `detail-${this.detailImageList.length}.jpg`,
          url: displayUrl
        });
        
        console.log('更新后的detailImageList:', this.detailImageList);
        
        this.$message.success('详情图片上传成功');
      }
    },
    
    // 详情图片上传失败
    handleDetailImageError(error, file, fileList) {
      console.error('详情图片上传失败:', error);
      this.$message.error('详情图片上传失败');
    },
    
    // 删除详情图片
    handleDetailImageRemove(file, fileList) {
      console.log('删除详情图片:', file);
      const index = this.productForm.detailImages.findIndex(img => img.includes(file.name));
      if (index !== -1) {
        this.productForm.detailImages.splice(index, 1);
      }
    },
    
    // 详情图片上传前验证
    beforeDetailImageUpload(file) {
      const isJPG = file.type === "image/jpeg" || file.type === "image/png" || file.type === "image/gif" || file.type === "image/webp";
      const isLt5M = file.size / 1024 / 1024 < 5;
      
      if (!isJPG) {
        this.$message.error("详情图片只能是 JPG、PNG、GIF 或 WEBP 格式!");
        return false;
      }
      if (!isLt5M) {
        this.$message.error("详情图片大小不能超过 5MB!");
        return false;
      }
      return true;
    },
    
  }
};
</script>

<style lang="scss" scoped>
.product-container {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;

  .mall-tabs :deep(.el-tabs__content) {
    margin-top: -12px;
  }

  .mall-tabs :deep(.el-card__header) {
    padding-top: 0;
    padding-bottom: 10px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }

  .header-actions-only {
    justify-content: flex-end;
  }

  .product-name {
    display: flex;
    align-items: center;
  }

  :deep(.el-card__body) {
    width: 100%;
    box-sizing: border-box;
  }
  
  .product-price {
    font-size: 18px;
    font-weight: bold;
    color: #fa541c;
  }
  
  .product-description {
    line-height: 1.8;
    color: #666;
  }

  .sales-summary {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 14px;
    padding: 12px;
    background: #fafafa;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
  }

  .review-empty-wrap {
    padding: 20px 0;
  }

  .review-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .review-card {
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    padding: 14px;
    background: #fff;
  }

  .review-header {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
  }

  .review-user {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .review-user-meta .name {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
  }

  .review-user-meta .meta {
    font-size: 12px;
    color: #909399;
  }

  .review-score {
    display: flex;
    align-items: center;
    gap: 6px;
    white-space: nowrap;
  }

  .score-text {
    color: #fa8c16;
    font-size: 13px;
  }

  .review-content {
    line-height: 1.7;
    color: #303133;
    margin-bottom: 10px;
  }

  .review-media-row {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 10px;
  }

  .review-image {
    width: 84px;
    height: 84px;
    border-radius: 6px;
    border: 1px solid #f0f0f0;
    overflow: hidden;
  }

  .review-video {
    width: 180px;
    height: 120px;
    border-radius: 6px;
    background: #000;
  }

  .review-footer {
    font-size: 12px;
    color: #909399;
    display: flex;
    gap: 16px;
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
  
  .avatar-uploader .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
}

  .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
  }

  .avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
  }

  .el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
  }
  
  .detail-images-uploader {
    .upload-tip {
      font-size: 12px;
      color: #999;
      margin-top: 10px;
    }
  }

  /* 购买类型（规格）与添加规格按钮同一行，标签不换行 */
  .product-spec-label-inline :deep(.el-form-item__label) {
    white-space: nowrap;
  }
  .product-spec-actions {
    margin-bottom: 10px;
  }
  .product-spec-cards {
    width: 100%;
  }
  .product-spec-card {
    width: 100%;
    margin-bottom: 15px;
    padding: 10px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    box-sizing: border-box;
  }
  .product-spec-empty-tip {
    color: #999;
    font-size: 12px;
  }
  .spec-value-details {
    margin-top: 12px;
    padding-top: 10px;
    border-top: 1px dashed #ebeef5;
  }
  .spec-value-details-title {
    font-size: 12px;
    color: #606266;
    margin-bottom: 8px;
  }
  .spec-value-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
    flex-wrap: wrap;
    gap: 4px;
  }
  .spec-value-preview {
    width: 48px;
    height: 48px;
    object-fit: cover;
    border-radius: 4px;
    margin-left: 8px;
    border: 1px solid #ebeef5;
  }
</style>