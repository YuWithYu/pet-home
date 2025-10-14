<template>
  <div class="product-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
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
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.name"
              />
            </el-select>
            <el-button type="primary" @click="showAddProductDialog">添加商品</el-button>
          </div>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="所有商品" name="all">
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
            <el-table-column prop="brand" label="品牌" width="120" />
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
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="text" @click="viewProduct(row)">查看</el-button>
                <el-button type="text" @click="editProduct(row)">编辑</el-button>
                <el-button type="text" @click="deleteProduct(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="在售产品" name="onsale">
          <el-table 
            :data="onsaleProducts" 
            style="width: 100%"
            row-key="id"
          >
            <el-table-column prop="name" label="产品名称" min-width="200">
              <template #default="{ row }">
                <div class="product-name">
                  <el-image 
                    :src="getImageUrl(row)" 
                    fit="cover" 
                    style="width: 40px; height: 40px; border-radius: 4px"
                    @error="handleImageError"
                  />
                  <span style="margin-left: 10px">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="产品分类" width="120" />
            <el-table-column prop="brand" label="品牌" width="120" />
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
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="text" @click="viewProduct(row)">查看</el-button>
                <el-button type="text" @click="editProduct(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="缺货产品" name="outofstock">
          <el-table 
            :data="outOfStockProducts" 
            style="width: 100%"
            row-key="id"
          >
            <el-table-column prop="name" label="产品名称" min-width="200">
              <template #default="{ row }">
                <div class="product-name">
                  <el-image 
                    :src="getImageUrl(row)" 
                    fit="cover" 
                    style="width: 40px; height: 40px; border-radius: 4px"
                    @error="handleImageError"
                  />
                  <span style="margin-left: 10px">{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="产品分类" width="120" />
            <el-table-column prop="brand" label="品牌" width="120" />
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
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="text" @click="editProduct(row)">补货</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="分类管理" name="categories">
          <div class="category-management">
            <div class="category-header">
              <el-button type="primary" @click="showAddCategoryDialog">添加分类</el-button>
            </div>
            
            <el-table 
              :data="categories" 
              style="width: 100%"
              row-key="id"
              v-loading="categoryLoading"
            >
              <el-table-column prop="name" label="分类名称" width="200" />
              <el-table-column prop="description" label="分类描述" min-width="300" />
              <el-table-column prop="productCount" label="商品数量" width="120">
                <template #default="{ row }">
                  <el-tag type="info">{{ row.productCount || 0 }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                    {{ row.status === 'active' ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button size="small" @click="editCategory(row)">编辑</el-button>
                  <el-button 
                    size="small" 
                    :type="row.status === 'active' ? 'warning' : 'success'"
                    @click="toggleCategoryStatus(row)"
                  >
                    {{ row.status === 'active' ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="small" type="danger" @click="deleteCategory(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
      
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
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.name"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="productForm.brand" />
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
                <el-option label="在售" value="onsale"></el-option>
                <el-option label="下架" value="offsale"></el-option>
                <el-option label="缺货" value="outofstock"></el-option>
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
                :on-error="handleImageError"
                :before-upload="beforeImageUpload"
              >
                <img 
                  v-if="productForm.image" 
                  :src="productForm.image" 
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
            <el-descriptions-item label="品牌">{{ detailProduct.brand }}</el-descriptions-item>
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
        <el-tab-pane label="销售记录" name="sales">
          <el-table :data="productSales" style="width: 100%">
            <el-table-column prop="orderId" label="订单号" width="150" />
            <el-table-column prop="customer" label="客户" width="150" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">
                ¥{{ row.amount.toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column prop="date" label="销售时间" width="180" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="评价" name="reviews">
          <el-table :data="productReviews" style="width: 100%">
            <el-table-column prop="customer" label="客户" width="150" />
            <el-table-column label="评分" width="120">
              <template #default="{ row }">
                <el-rate
                  v-model="row.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}分"
                />
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评价内容" />
            <el-table-column prop="date" label="评价时间" width="180" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="editProduct(detailProduct)">编辑</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加/编辑分类对话框 -->
    <el-dialog 
      v-model="categoryDialogVisible" 
      :title="editingCategory ? '编辑分类' : '添加分类'"
      width="500px"
    >
      <el-form 
        ref="categoryFormRef" 
        :model="categoryForm" 
        :rules="categoryRules" 
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="分类描述" prop="description">
          <el-input 
            v-model="categoryForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入分类描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCategory">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { productApi } from "@/api/pet-home";

export default {
  name: "Product",
  data() {
    return {
      activeTab: "all",
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
      editingCategory: null,
      imageUpdateTimestamp: 0,
      uploadAction: "http://localhost:8080/api/admin/upload",
      uploadHeaders: {
        // 暂时移除Authorization头，因为已经移除了权限检查
        // "Authorization": "Bearer " + (localStorage.getItem("token") || "")
      },
      products: [],
      allProducts: [],
      categories: [],
      categoryLoading: false,
      categoryForm: {
        name: "",
        description: "",
        status: "active"
      },
      categoryRules: {
        name: [
          { required: true, message: "请输入分类名称", trigger: "blur" }
        ],
        description: [
          { required: true, message: "请输入分类描述", trigger: "blur" }
        ],
        status: [
          { required: true, message: "请选择状态", trigger: "change" }
        ]
      },
      productForm: {
        name: "",
        category: "",
        brand: "",
        price: 0,
        stock: 0,
        status: "onsale",
        image: "",
        description: ""
      },
      detailProduct: {},
      productRules: {
        name: [
          { required: true, message: "请输入产品名称", trigger: "blur" }
        ],
        category: [
          { required: true, message: "请选择产品分类", trigger: "change" }
        ],
        brand: [
          { required: true, message: "请输入品牌", trigger: "blur" }
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
      productSales: [
        {
          orderId: "SO202305001",
          customer: "张三",
          quantity: 2,
          amount: 15998,
          date: "2023-05-01 14:30:00"
        },
        {
          orderId: "SO202304015",
          customer: "李四",
          quantity: 1,
          amount: 7999,
          date: "2023-04-15 10:15:00"
        }
      ],
      productReviews: [
        {
          customer: "王五",
          rating: 5,
          content: "产品质量非常好，使用体验很棒！",
          date: "2023-05-10 16:00:00"
        },
        {
          customer: "赵六",
          rating: 4,
          content: "产品不错，性价比高，推荐购买。",
          date: "2023-05-08 09:30:00"
        }
      ],
      baseUrl: 'http://localhost:8080/static/product/',
    };
  },
  computed: {
    filteredProducts() {
      let result = this.allProducts || [];
      
      // 搜索过滤
      if (this.searchText) {
        result = result.filter(product => 
          product.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
          product.category.toLowerCase().includes(this.searchText.toLowerCase()) ||
          product.brand.toLowerCase().includes(this.searchText.toLowerCase())
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
    onsaleProducts() {
      let result = (this.allProducts || []).filter(product => product.status === "onsale");
      
      // 应用搜索过滤
      if (this.searchText) {
        result = result.filter(product => 
          product.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
          product.category.toLowerCase().includes(this.searchText.toLowerCase()) ||
          product.brand.toLowerCase().includes(this.searchText.toLowerCase())
        );
      }
      
      // 应用分类过滤
      if (this.selectedCategory) {
        result = result.filter(product => 
          product.category === this.selectedCategory
        );
      }
      
      return result;
    },
    outOfStockProducts() {
      let result = (this.allProducts || []).filter(product => product.stock === 0);
      
      // 应用搜索过滤
      if (this.searchText) {
        result = result.filter(product => 
          product.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
          product.category.toLowerCase().includes(this.searchText.toLowerCase()) ||
          product.brand.toLowerCase().includes(this.searchText.toLowerCase())
        );
      }
      
      // 应用分类过滤
      if (this.selectedCategory) {
        result = result.filter(product => 
          product.category === this.selectedCategory
        );
      }
      
      return result;
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
    }
  },
  methods: {
    // 获取图片URL的统一方法
      getImageUrl(item) {
        console.log('处理商品图片URL:', item);
        
        let imagePath = item.image || '';
        
        // 如果已经是完整URL，直接返回
        if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
          return imagePath;
        }
        
        // 如果是 /upload/ 开头，添加服务器前缀
        if (imagePath.startsWith('/upload/')) {
          return `http://localhost:8080${imagePath}`;
        }
        
        // 兼容旧数据：/uploads/ 或 /static/ 开头的，也映射到 /upload/
        if (imagePath.startsWith('/uploads/') || imagePath.startsWith('/static/')) {
          // 提取文件名
          const filename = imagePath.substring(imagePath.lastIndexOf('/') + 1);
          return `http://localhost:8080/upload/${filename}`;
        }
        
        // 默认处理：只有文件名时，添加 /upload/ 前缀
        return imagePath ? `http://localhost:8080/upload/${imagePath}` : '@/static/default-product.png';
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
            // 获取所有商品数据
            this.allProducts = response.data.records.map(item => {
              console.log('处理商品项:', item);
              console.log('商品项的image字段:', item.image);
              console.log('商品项的imageUrl字段:', item.imageUrl);
              
              const product = {
                id: item.id,
                name: item.name,
                category: item.category,
                brand: item.brand || "宠物用品",
                price: item.price,
                stock: item.stock,
                status: item.stock > 0 ? "onsale" : "outofstock",
                image: item.image, // 使用原始图片字段
                imageUrl: item.imageUrl, // 保存完整的图片URL
                description: item.description || "暂无描述",
                createTime: item.createTime
              };
              console.log('处理后的商品项:', product);
              return product;
            });
            this.totalProducts = response.data.total || 0;
          } else if (Array.isArray(response.data)) {
            console.log('处理数组数据，记录数:', response.data.length);
            // 直接数组响应格式
            this.allProducts = response.data.map(item => {
              console.log('处理商品项:', item);
              console.log('商品项的image字段:', item.image);
              console.log('商品项的imageUrl字段:', item.imageUrl);
              
              const product = {
                id: item.id,
                name: item.name,
                category: item.category,
                brand: item.brand || "宠物用品",
                price: item.price,
                stock: item.stock,
                status: item.stock > 0 ? "onsale" : "outofstock",
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
        // 从现有商品中提取所有唯一的分类
        const uniqueCategories = [...new Set((this.allProducts || []).map(product => product.category))];
        
        // 根据实际商品分类创建分类列表
        this.categories = uniqueCategories.map((categoryName, index) => ({
          id: index + 1,
          name: categoryName,
          description: `${categoryName}相关产品`,
          productCount: (this.allProducts || []).filter(p => p.category === categoryName).length,
          status: "active",
          createTime: new Date().toLocaleString()
        }));
      } catch (error) {
        console.error('加载分类数据失败:', error);
      }
    },
    handleTabChange(tab) {
      this.activeTab = tab;
      this.currentPage = 1;
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
        "onsale": "在售",
        "offsale": "下架",
        "outofstock": "缺货"
      };
      return statusMap[status] || status;
    },
    getStatusType(status) {
      const typeMap = {
        "onsale": "success",
        "offsale": "info",
        "outofstock": "danger"
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
        brand: "",
        price: 0,
        stock: 0,
        status: "onsale",
        image: "",
        description: ""
      };
      this.productDialogVisible = true;
      this.$nextTick(() => {
        this.$refs.productFormRef.resetFields();
      });
    },
    editProduct(product) {
      this.editingProduct = product;
      this.productForm = { 
        ...product,
        // 确保图片URL正确显示 - 使用统一的图片URL处理方法
        image: this.getImageUrl(product)
      };
      console.log('编辑产品 - 原始图片:', product.image);
      console.log('编辑产品 - 构造后的图片URL:', this.productForm.image);
      this.productDialogVisible = true;
      this.detailDialogVisible = false;
    },
    viewProduct(product) {
      this.detailProduct = { 
        ...product,
        // 确保图片URL正确显示 - 使用统一的图片URL处理方法
        image: this.getImageUrl(product)
      };
      console.log('查看产品 - 原始图片:', product.image);
      console.log('查看产品 - 构造后的图片URL:', this.detailProduct.image);
      this.detailDialogVisible = true;
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
        if (response.code === 0) {
          // 从前端数组中移除商品
          const index = (this.allProducts || []).findIndex(p => p.id === product.id);
          if (index !== -1) {
            this.allProducts.splice(index, 1);
            this.totalProducts = this.allProducts.length;
          }
          this.$message.success("产品删除成功");
        } else {
          this.$message.error("产品删除失败: " + response.message);
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
    async saveProduct() {
      try {
        await this.$refs.productFormRef.validate();
        
        // 准备商品数据
        console.log('保存产品 - 当前productForm.image:', this.productForm.image);
        // 提取图片文件名，确保只保存文件名而不是完整URL
        let imageFilename = null;
        if (this.productForm.image) {
          if (this.productForm.image.startsWith('http')) {
            // 从完整URL中提取文件名
            const urlParts = this.productForm.image.split('/');
            imageFilename = urlParts[urlParts.length - 1];
          } else {
            // 如果已经是文件名，直接使用
            imageFilename = this.productForm.image;
          }
        }
        console.log('保存产品 - 提取的文件名:', imageFilename);
        console.log('保存产品 - 原始URL:', this.productForm.image);
        
        const productData = {
          name: this.productForm.name,
          category: this.productForm.category,
          brand: this.productForm.brand,
          price: this.productForm.price,
          stock: this.productForm.stock,
          status: this.productForm.status,
          description: this.productForm.description,
          image: imageFilename
        };
        
        console.log('保存产品 - 发送的数据:', productData);
        
        if (this.editingProduct) {
          // 编辑产品 - 调用后端API
          const response = await productApi.update(this.editingProduct.id, productData);
          
          if (response.code === 0) {
            this.$message.success("产品信息更新成功");
            console.log('产品更新成功，重新加载产品列表');
            console.log('更新前的产品列表长度:', this.allProducts.length);
            
            // 强制刷新产品列表
            await this.loadProducts();
            console.log('重新加载后的产品列表长度:', this.allProducts.length);
            
            // 查找更新后的产品，检查图片URL
            const updatedProduct = this.allProducts.find(p => p.id === this.editingProduct.id);
            if (updatedProduct) {
              console.log('=== 产品更新后调试信息 ===');
              console.log('更新后的产品信息:', updatedProduct);
              console.log('更新后的产品图片字段:', updatedProduct.image);
              console.log('更新后的产品imageUrl字段:', updatedProduct.imageUrl);
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
          } else {
            this.$message.error("产品更新失败: " + response.message);
          }
        } else {
          // 添加产品 - 调用后端API
          const response = await productApi.create(productData);
          if (response.code === 0) {
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
          } else {
            this.$message.error("产品添加失败: " + response.message);
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
      console.log('响应data长度:', response?.data?.length);
      
      if (response && response.code === 0) {
        // 直接使用后端返回的完整图片URL
        const imageUrl = response.data;
        console.log('原始图片URL:', imageUrl);
        console.log('URL长度:', imageUrl?.length);
        console.log('URL包含/uploads/product/:', imageUrl?.includes('/uploads/product/'));
        
        this.productForm.image = imageUrl;
        console.log('设置productForm.image为:', this.productForm.image);
        console.log('设置后的长度:', this.productForm.image?.length);
        console.log('当前productForm:', JSON.stringify(this.productForm, null, 2));
        this.$message.success('图片上传成功');
        
        // 强制更新视图
        this.$nextTick(() => {
          console.log('强制更新视图后的productForm.image:', this.productForm.image);
          this.$forceUpdate();
        });
        
        // 延迟再次检查
        setTimeout(() => {
          console.log('延迟检查 - productForm.image:', this.productForm.image);
        }, 1000);
      } else {
        console.error('上传失败响应:', response);
        this.$message.error('图片上传失败: ' + (response?.msg || '未知错误'));
      }
    },
    handleImageError(error, file) {
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
    showAddCategoryDialog() {
      this.editingCategory = null;
      this.categoryForm = {
        name: "",
        description: "",
        status: "active"
      };
      this.categoryDialogVisible = true;
    },
    
    editCategory(category) {
      this.editingCategory = category;
      this.categoryForm = {
        name: category.name,
        description: category.description,
        status: category.status
      };
      this.categoryDialogVisible = true;
    },
    
    async saveCategory() {
      try {
        await this.$refs.categoryFormRef.validate();
        
        if (this.editingCategory) {
          // 编辑分类
          const index = this.categories.findIndex(c => c.id === this.editingCategory.id);
          if (index !== -1) {
            this.categories[index] = {
              ...this.categories[index],
              ...this.categoryForm
            };
          }
          this.$message.success("分类更新成功");
        } else {
          // 添加分类
          const newCategory = {
            id: Date.now(), // 临时ID
            ...this.categoryForm,
            productCount: 0,
            createTime: new Date().toLocaleString()
          };
          this.categories.push(newCategory);
          this.$message.success("分类添加成功");
        }
        
        this.categoryDialogVisible = false;
      } catch (error) {
        console.error("保存分类失败:", error);
      }
    },
    
    toggleCategoryStatus(category) {
      const newStatus = category.status === 'active' ? 'inactive' : 'active';
      const index = this.categories.findIndex(c => c.id === category.id);
      if (index !== -1) {
        this.categories[index].status = newStatus;
        this.$message.success(`分类已${newStatus === 'active' ? '启用' : '禁用'}`);
      }
    },
    
    deleteCategory(category) {
      this.$confirm(`确定删除分类"${category.name}"吗？`, '确认删除', {
        type: 'warning'
      }).then(() => {
        const index = this.categories.findIndex(c => c.id === category.id);
        if (index !== -1) {
          this.categories.splice(index, 1);
          this.$message.success("分类删除成功");
        }
      }).catch(() => {
        // 用户取消删除
      });
    },
    
    // 图片加载事件处理
    handleImageError(event) {
      console.log('图片加载失败:', event.target.src);
      // 可以在这里设置默认图片
    },
    
    handleImageLoad(event) {
      console.log('图片加载成功:', event.target.src);
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
    }
  }
};
</script>

<style lang="scss" scoped>
.product-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }
  
  .product-name {
    display: flex;
    align-items: center;
  }
  
  .category-management {
    .category-header {
      margin-bottom: 20px;
      display: flex;
      justify-content: flex-end;
    }
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
</style>