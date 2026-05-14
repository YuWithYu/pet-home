<template>
  <div class="points-mall-container">
    <el-tabs v-model="mainTab" class="points-mall-main-tabs" @tab-change="onMainTabChange">
      <el-tab-pane label="积分商品" name="products">
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
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddProductDialog">
              <el-icon><Plus /></el-icon>
              添加商品
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="filteredProducts" 
        style="width: 100%"
        row-key="id"
        v-loading="loading"
      >
        <el-table-column prop="name" label="商品名称" min-width="200">
          <template #default="{ row }">
            <div class="product-name">
              <el-image 
                :src="getImageUrl(row)" 
                fit="cover" 
                style="width: 50px; height: 50px; border-radius: 4px; margin-right: 10px"
                @error="handleImageError"
              />
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="商品描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="积分价格" width="120">
          <template #default="{ row }">
            <span style="color: #66bb6a; font-weight: 600">
              {{ formatPrice(row.price) }}kg
            </span>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="editProduct(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteProduct(row)">删除</el-button>
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

      <el-tab-pane label="订单" name="orders">
        <el-card shadow="never">
          <template #header>
            <div class="card-header header-actions-only">
              <div class="header-actions">
                <el-input
                  v-model="orderSearchText"
                  placeholder="搜索订单..."
                  clearable
                  style="width: 200px; margin-right: 10px"
                  @input="orderCurrentPage = 1"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="orderFilterStatus"
                  placeholder="状态筛选"
                  style="width: 120px; margin-right: 10px"
                >
                  <el-option label="全部" value="" />
                  <el-option label="待付款" value="pending" />
                  <el-option label="待发货" value="paid" />
                  <el-option label="已发货" value="shipped" />
                  <el-option label="已完成" value="completed" />
                  <el-option label="已取消" value="cancelled" />
                </el-select>
                <el-button type="primary" @click="exportPointsOrders">导出订单</el-button>
              </div>
            </div>
          </template>
          <el-table :data="pagedPointsOrders" v-loading="ordersLoading" style="width: 100%" row-key="id">
            <el-table-column label="订单号" width="180">
              <template #default="{ row }">
                {{ row.orderNo || row.id }}
              </template>
            </el-table-column>
            <el-table-column prop="customer" label="客户" width="100" />
            <el-table-column prop="phone" label="联系电话" width="120" show-overflow-tooltip />
            <el-table-column prop="address" label="收货地址" min-width="180" show-overflow-tooltip />
            <el-table-column prop="products" label="商品" min-width="250">
              <template #default="{ row }">
                <div v-for="product in (row.products || [])" :key="product.id || product.name" class="order-product">
                  <el-image
                    :src="product.image"
                    fit="cover"
                    style="width: 40px; height: 40px; border-radius: 4px; margin-right: 10px"
                    loading="lazy"
                  >
                    <template #error>
                      <div style="width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 4px;">
                        <span style="font-size: 10px; color: #999;">无图</span>
                      </div>
                    </template>
                  </el-image>
                  <div class="product-info">
                    <div>{{ product.name }}</div>
                    <div class="product-meta">
                      <span>{{ formatPointsAmount(product.price) }} × {{ product.quantity }}</span>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="totalAmount" label="订单金额" width="120">
              <template #default="{ row }">
                {{ formatPointsAmount(row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="orderStatusTagType(row.status)">{{ orderStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="退款状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.refundRecord" :type="getRefundStatusType(row.refundRecord.status)">
                  {{ getRefundStatusText(row.refundRecord.status) }}
                </el-tag>
                <el-tag v-else type="info">无退款</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="退款金额" width="110">
              <template #default="{ row }">
                {{ row.refundRecord ? formatPointsAmount(row.refundRecord.refundAmount || 0) : "-" }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="180" />
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <el-button type="text" @click.stop="openPointsOrderDetail(row)">发货</el-button>
                <el-button v-if="row.status === 'pending'" type="text" @click.stop="cancelPointsOrder(row)">取消</el-button>
                <el-button v-if="row.refundRecord" type="text" @click.stop="viewPointsRefundDetail(row.refundRecord)">退款详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="orderCurrentPage"
              v-model:page-size="orderPageSize"
              :page-sizes="[10, 20, 50]"
              :total="filteredPointsOrders.length"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="orderCurrentPage = 1"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog 
      v-model="productDialogVisible" 
      :title="editingProduct ? '编辑商品' : '添加商品'"
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
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="productForm.name" placeholder="请输入商品名称" />
            </el-form-item>
            
            <el-form-item label="商品描述" prop="description">
              <el-input 
                v-model="productForm.description" 
                type="textarea"
                :rows="3"
                placeholder="请输入商品描述"
              />
            </el-form-item>
            
            <el-form-item label="积分价格(kg)" prop="price">
              <el-input-number 
                v-model="productForm.price" 
                :min="0"
                :step="1"
                :precision="0"
                controls-position="right"
                style="width: 100%"
                placeholder="请输入积分价格（单位：kg）"
              />
              <div class="form-tip">注意：前端显示为kg，后端存储时会转换为g（1kg = 1000g）</div>
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
                <el-option label="上架" :value="1"></el-option>
                <el-option label="下架" :value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          
          <el-col :span="12">
            <el-form-item label="商品图片" prop="image">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeImageUpload"
                :http-request="customImageUpload"
              >
                <img 
                  v-if="productForm.image" 
                  :src="productForm.image" 
                  class="avatar" 
                  alt="Product Image"
                />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">支持 jpg、png；超过 2MB 或尺寸过大将自动压缩后上传（建议 800×600px）</div>
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
            :loading="saving"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="pointsDetailVisible" title="积分订单详情" width="800px" destroy-on-close>
      <el-row v-if="pointsDetailOrder && pointsDetailOrder.orderNo" :gutter="20">
        <el-col :span="16">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">{{ pointsDetailOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="客户">{{ pointsDetailOrder.customer || "用户" }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ pointsDetailOrder.phone || "—" }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ pointsDetailOrder.address || "—" }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="orderStatusTagType(pointsDetailOrder.status)">
                {{ orderStatusLabel(pointsDetailOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ pointsDetailOrder.createTime || "—" }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ pointsDetailOrder.shipTime || "—" }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
        <el-col :span="8">
          <div class="order-amount">
            <div class="amount-label">订单总额</div>
            <div class="amount-value">{{ formatPointsAmount(pointsDetailOrder.totalAmount || 0) }}</div>
          </div>
        </el-col>
      </el-row>

      <el-divider>商品信息</el-divider>
      <el-table :data="pointsDetailOrder.products || []" style="width: 100%">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                :src="row.image"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px; margin-right: 10px"
                :preview-src-list="row.image ? [row.image] : []"
                loading="lazy"
              >
                <template #error>
                  <div style="width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 4px;">
                    <span style="font-size: 12px; color: #999;">无图片</span>
                  </div>
                </template>
              </el-image>
              <div>
                <div>{{ row.name }}</div>
                <div class="product-spec">{{ formatPointsAmount(row.price || 0) }} × {{ row.quantity || 1 }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">
            {{ formatPointsAmount(row.price || 0) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column label="小计" width="120">
          <template #default="{ row }">
            {{ formatPointsAmount((Number(row.price || 0) * Number(row.quantity || 1))) }}
          </template>
        </el-table-column>
      </el-table>

      <el-divider v-if="pointsDetailOrder && (pointsDetailOrder.status === 'paid' || pointsDetailOrder.status === 'shipped')">物流信息</el-divider>
      <el-steps
        v-if="pointsDetailOrder && (pointsDetailOrder.status === 'paid' || pointsDetailOrder.status === 'shipped' || pointsDetailOrder.status === 'completed')"
        :active="getPointsLogisticsStep(pointsDetailOrder.status)"
        finish-status="success"
        simple
      >
        <el-step title="已下单" :description="pointsDetailOrder.createTime" />
        <el-step title="已付款" />
        <el-step title="已发货" :description="pointsDetailOrder.shipTime" />
        <el-step title="已完成" />
      </el-steps>
      <el-form
        v-if="pointsDetailOrder && (pointsDetailOrder.status === 'paid' || pointsDetailOrder.status === 'shipped')"
        :model="pointsLogisticsForm"
        label-width="120px"
        style="margin-top: 20px"
      >
        <el-form-item label="物流公司">
          <el-select
            v-model="pointsLogisticsForm.shippingCompany"
            placeholder="请选择物流公司"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
          >
            <el-option label="顺丰快递" value="顺丰快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达速递" value="韵达速递" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="百世快递" value="百世快递" />
            <el-option label="EMS" value="EMS" />
            <el-option label="德邦快递" value="德邦快递" />
            <el-option label="京东物流" value="京东物流" />
          </el-select>
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="pointsLogisticsForm.shippingNumber" placeholder="请输入物流单号" clearable />
        </el-form-item>
        <el-form-item label="发货时间">
          <el-date-picker
            v-model="pointsLogisticsForm.shippingTime"
            type="datetime"
            placeholder="选择发货时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
            :default-time="new Date(2000, 1, 1, 0, 0, 0)"
          />
          <div style="font-size: 12px; color: #999; margin-top: 5px;">
            提示：选择发货时间，如果不选择则使用当前时间
          </div>
        </el-form-item>
        <el-form-item label="发货仓地址">
          <el-input
            v-model="pointsLogisticsForm.warehouseAddress"
            placeholder="请输入发货仓地址，或从下方已保存的地址中点击选择"
            clearable
          />
          <div style="margin-top: 8px;">
            <el-button type="primary" size="small" @click="addPointsWarehouseFromInput">添加发货仓</el-button>
            <span style="margin-left: 8px; font-size: 12px; color: #999;">添加后会显示在下方，下次可直接点击选用</span>
          </div>
          <div class="warehouse-list-wrap" v-if="pointsWarehouseList.length > 0">
            <div class="warehouse-list-title">已保存的发货仓（点击下方地址可填入，下次发货直接选）</div>
            <div
              v-for="w in pointsWarehouseList"
              :key="w.id"
              class="warehouse-item"
              @click="selectPointsWarehouse(w)"
            >
              <span>{{ w.address }}</span>
              <span class="delete-btn" @click.stop="deletePointsWarehouse(w)">删除</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="savePointsLogisticsInfo">保存物流信息</el-button>
          <el-button @click="loadPointsLogisticsInfo">刷新物流信息</el-button>
        </el-form-item>
      </el-form>
      <div
        v-if="pointsDetailOrder.shippingCompany || pointsDetailOrder.shippingNumber"
        style="margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 4px;"
      >
        <div style="margin-bottom: 10px;"><strong>当前物流信息：</strong></div>
        <div v-if="pointsDetailOrder.shippingCompany" style="margin-bottom: 5px;">物流公司：{{ pointsDetailOrder.shippingCompany }}</div>
        <div v-if="pointsDetailOrder.shippingNumber" style="margin-bottom: 5px;">物流单号：{{ pointsDetailOrder.shippingNumber }}</div>
        <div v-if="pointsDetailOrder.shipTime" style="margin-bottom: 5px;">发货时间：{{ pointsDetailOrder.shipTime }}</div>
        <div v-if="pointsDetailOrder.warehouseAddress" style="margin-bottom: 5px;">发货仓地址：{{ pointsDetailOrder.warehouseAddress }}</div>
      </div>
      <template #footer>
        <el-button @click="pointsDetailVisible = false">关闭</el-button>
        <el-button v-if="pointsDetailOrder && pointsDetailOrder.status === 'paid'" type="primary" @click="shipPointsOrder(pointsDetailOrder)">立即发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="pointsRefundDetailDialogVisible" title="退款详情" width="600px">
      <div v-if="selectedPointsRefund" class="refund-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ selectedPointsRefund.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">{{ formatPointsAmount(selectedPointsRefund.refundAmount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ selectedPointsRefund.reason || '—' }}</el-descriptions-item>
          <el-descriptions-item label="退款状态">
            <el-tag :type="getRefundStatusType(selectedPointsRefund.status)">
              {{ getRefundStatusText(selectedPointsRefund.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ selectedPointsRefund.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ selectedPointsRefund.receiverName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedPointsRefund.receiverPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ selectedPointsRefund.receiverAddress || '—' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="pointsRefundDetailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="selectedPointsRefund && selectedPointsRefund.status === 'pending'"
          type="success"
          @click="approvePointsRefund(selectedPointsRefund)"
        >
          批准退款
        </el-button>
        <el-button
          v-if="selectedPointsRefund && selectedPointsRefund.status === 'pending'"
          type="danger"
          @click="rejectPointsRefund(selectedPointsRefund)"
        >
          拒绝退款
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { baseURL } from "@/config";
import { productApi } from "@/api/pet-home";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, Plus } from "@element-plus/icons-vue";
import store from "@/store";
import axios from "axios";
import request from "@/utils/request";

function getImageBase() {
  const u = (baseURL || "").trim().replace(/\/api\/?$/, "");
  if (u && (u.startsWith("http://") || u.startsWith("https://"))) return u;
  return "http://localhost:8080";
}

export default {
  name: "PointsMall",
  components: {
    Search,
    Plus
  },
  data() {
    return {
      mainTab: "products",
      pointsOrdersAll: [],
      ordersLoading: false,
      orderSearchText: "",
      orderFilterStatus: "",
      orderCurrentPage: 1,
      orderPageSize: 10,
      pointsDetailVisible: false,
      pointsDetailOrder: {},
      pointsRefundList: [],
      pointsRefundDetailDialogVisible: false,
      selectedPointsRefund: null,
      pointsLogisticsForm: {
        shippingCompany: "",
        shippingNumber: "",
        shippingTime: null,
        warehouseAddress: "",
      },
      pointsWarehouseList: [],
      searchText: "",
      currentPage: 1,
      pageSize: 10,
      totalProducts: 0,
      loading: false,
      saving: false,
      productDialogVisible: false,
      editingProduct: null,
      uploadHeaders: {},
      products: [],
      allProducts: [],
      productForm: {
        name: "",
        description: "",
        price: 0,
        stock: 0,
        status: 1,
        image: "",
        category: "积分商城" // 固定为积分商城
      },
      productRules: {
        name: [
          { required: true, message: "请输入商品名称", trigger: "blur" }
        ],
        description: [
          { required: true, message: "请输入商品描述", trigger: "blur" }
        ],
        price: [
          { required: true, message: "请输入积分价格", trigger: "blur" },
          { type: "number", min: 1, message: "积分价格必须大于0", trigger: "blur" }
        ],
        stock: [
          { required: true, message: "请输入库存", trigger: "blur" },
          { type: "number", min: 0, message: "库存不能小于0", trigger: "blur" }
        ],
        status: [
          { required: true, message: "请选择状态", trigger: "change" }
        ]
      },
    };
  },
  computed: {
    filteredPointsOrders() {
      let result = this.pointsOrdersAll || [];
      if (this.orderSearchText) {
        const q = this.orderSearchText.toLowerCase();
        result = result.filter(
          (o) =>
            String(o.orderNo || "")
              .toLowerCase()
              .includes(q) ||
            String(o.customer || "")
              .toLowerCase()
              .includes(q)
        );
      }
      if (this.orderFilterStatus) {
        result = result.filter((o) => String(o.status || "") === String(this.orderFilterStatus));
      }
      return result;
    },
    pagedPointsOrders() {
      const list = this.filteredPointsOrders;
      const start = (this.orderCurrentPage - 1) * this.orderPageSize;
      return list.slice(start, start + this.orderPageSize);
    },
    uploadAction() {
      return (getImageBase() || "http://localhost:8080") + "/api/admin/upload?type=product";
    },
    filteredProducts() {
      let result = this.allProducts || [];
      
      // 搜索过滤
      if (this.searchText) {
        result = result.filter(product => 
          product.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
          (product.description && product.description.toLowerCase().includes(this.searchText.toLowerCase()))
        );
      }
      
      // 分页处理
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      this.totalProducts = result.length;
      return result.slice(start, end);
    }
  },
  mounted() {
    this.loadProducts();
    // 设置上传请求头
    this.setUploadHeaders();
  },
  watch: {
    orderFilterStatus() {
      this.orderCurrentPage = 1;
      this.loadPointsOrders();
    },
  },
  methods: {
    onMainTabChange(name) {
      if (name === "orders") {
        this.loadPointsOrders();
      }
    },
    orderStatusLabel(status) {
      const m = {
        pending: "待付款",
        paid: "待发货",
        shipped: "已发货",
        completed: "已完成",
        cancelled: "已取消",
      };
      return m[status] || status || "—";
    },
    orderStatusTagType(status) {
      const m = {
        pending: "info",
        paid: "warning",
        shipped: "",
        completed: "success",
        cancelled: "danger",
      };
      return m[status] || "info";
    },
    getPointsLogisticsStep(status) {
      const map = {
        pending: 0,
        paid: 1,
        shipped: 2,
        completed: 3,
        cancelled: 0,
      };
      return map[status] ?? 0;
    },
    getRefundStatusText(status) {
      const map = {
        pending: "待处理",
        approved: "已批准",
        rejected: "已拒绝",
        completed: "已退款",
      };
      return map[status] || status || "未知";
    },
    getRefundStatusType(status) {
      const map = {
        pending: "warning",
        approved: "success",
        rejected: "danger",
        completed: "success",
      };
      return map[status] || "info";
    },
    formatPointsAmount(value) {
      const n = Number(value || 0);
      if (Number.isNaN(n)) return "0 积分";
      const pts = Math.round(n);
      return `${pts} 积分`;
    },
    buildFullAddress(order) {
      let fullAddress = order.address || "";
      if (!fullAddress && (order.receiverProvince || order.receiverCity || order.receiverDetailAddress)) {
        const parts = [];
        if (order.receiverProvince) parts.push(order.receiverProvince);
        if (order.receiverCity) parts.push(order.receiverCity);
        if (order.receiverRegion) parts.push(order.receiverRegion);
        if (order.receiverDetailAddress) parts.push(order.receiverDetailAddress);
        fullAddress = parts.join("");
      }
      return fullAddress;
    },
    normalizePointsOrder(order) {
      const products = (order.products || order.orderItemList || []).map((product) => ({
        id: product.id || product.productId,
        name: product.productName || product.name || "商品",
        image: product.productImage || product.image || "",
        price: Number(product.productPrice || product.price || 0),
        quantity: product.quantity || 1,
        subtotal: Number(
          product.subtotal || (product.productPrice || product.price || 0) * (product.quantity || 1)
        ),
      }));
      const fallbackTotal = products.reduce(
        (sum, p) => sum + Number(p.price || 0) * Number(p.quantity || 1),
        0
      );
      const totalAmount = Number(order.totalAmount || order.amount || order.payAmount || fallbackTotal || 0);
      const fullAddress = this.buildFullAddress(order);
      const phone = order.phone || order.receiverPhone || "";
      const customer = order.customer || order.receiverName || "用户";
      return {
        id: order.id || order.orderNo,
        orderNo: order.orderNo || order.id,
        customer,
        phone,
        address: fullAddress,
        status: order.status || "pending",
        createTime: order.createTime || "",
        shippingCompany: order.shippingCompany || "",
        shippingNumber: order.shippingNumber || "",
        shipTime: order.shipTime || order.shippingTime || order.deliveryTime || "",
        warehouseAddress: order.warehouseAddress || "",
        totalAmount,
        products,
        refundRecord: null,
      };
    },
    async openPointsOrderDetail(row) {
      const orderId = row.orderNo || row.id;
      try {
        const response = await request.get(`/orders/${orderId}`);
        if ((response.code === 0 || response.code === 200) && response.data) {
          this.pointsDetailOrder = {
            ...this.normalizePointsOrder(response.data),
            refundRecord: row.refundRecord || null,
          };
        } else {
          this.pointsDetailOrder = { ...row };
        }
      } catch (e) {
        this.pointsDetailOrder = { ...row };
      }
      this.pointsLogisticsForm = {
        shippingCompany: this.pointsDetailOrder.shippingCompany || "",
        shippingNumber: this.pointsDetailOrder.shippingNumber || "",
        shippingTime: this.pointsDetailOrder.shipTime ? String(this.pointsDetailOrder.shipTime).replace(" ", "T") : null,
        warehouseAddress: this.pointsDetailOrder.warehouseAddress || "",
      };
      this.pointsDetailVisible = true;
      this.loadPointsWarehouses();
      this.loadPointsLogisticsInfo();
    },
    async loadPointsOrders() {
      this.ordersLoading = true;
      try {
        const response = await request.get("/orders/list", {
          params: {
            orderCategory: "points",
          },
        });
        if ((response.code === 0 || response.code === 200) && response.data) {
          const ordersData = response.data.orders || [];
          let mappedOrders = ordersData.map((order) => this.normalizePointsOrder(order));
          const refundMap = await this.loadPointsRefunds();
          mappedOrders = mappedOrders.map((order) => ({
            ...order,
            refundRecord: refundMap.get(order.orderNo) || null,
          }));
          this.pointsOrdersAll = mappedOrders;
        } else {
          this.pointsOrdersAll = [];
          ElMessage.error(response?.msg || "加载积分订单失败");
        }
      } catch (e) {
        console.error(e);
        this.pointsOrdersAll = [];
        ElMessage.error("加载积分订单失败");
      } finally {
        this.ordersLoading = false;
      }
    },
    async loadPointsRefunds() {
      try {
        const response = await request.get("/orders/refunds", {
          params: {
            page: 1,
            size: 1000,
            orderCategory: "points",
          },
        });
        if (!(response.code === 0 || response.code === 200)) {
          this.pointsRefundList = [];
          return new Map();
        }
        const records = response.data?.records || [];
        const pointsRefunds = records.filter((r) => {
          const category = String(r.orderCategory || r.category || "").toLowerCase();
          const orderNo = String(r.orderNo || "").toUpperCase();
          return category === "points" || orderNo.startsWith("EX");
        });
        this.pointsRefundList = pointsRefunds;
        return new Map(pointsRefunds.map((r) => [String(r.orderNo), r]));
      } catch (e) {
        this.pointsRefundList = [];
        return new Map();
      }
    },
    cancelPointsOrder(order) {
      const orderId = order.orderNo || order.id;
      ElMessageBox.confirm(`确认取消订单 ${orderId} 吗？`, "提示", {
        type: "warning",
      })
        .then(async () => {
          const res = await request.put(`/orders/${orderId}/status`, null, {
            params: { status: "cancelled" },
          });
          if (res.code === 0 || res.code === 200) {
            ElMessage.success("订单已取消");
            this.loadPointsOrders();
          } else {
            ElMessage.error(res.msg || "取消失败");
          }
        })
        .catch(() => {});
    },
    shipPointsOrder(order) {
      const orderId = order.orderNo || order.id;
      ElMessageBox.confirm(`确认对订单 ${orderId} 发货？`, "提示", {
        type: "warning",
      })
        .then(() => {
          return request.put(`/orders/${orderId}/status`, null, {
            params: { status: "shipped" },
          });
        })
        .then((res) => {
          if (res.code === 0 || res.code === 200) {
            ElMessage.success("已发货");
            this.pointsDetailVisible = false;
            this.loadPointsOrders();
          } else {
            ElMessage.error(res.msg || "操作失败");
          }
        })
        .catch(() => {});
    },
    viewPointsRefundDetail(refund) {
      this.selectedPointsRefund = refund;
      this.pointsRefundDetailDialogVisible = true;
    },
    approvePointsRefund(refund) {
      if (!refund || !refund.orderNo) return;
      ElMessageBox.confirm(`确定批准订单 ${refund.orderNo} 的退款申请吗？`, "确认批准", {
        type: "warning",
      })
        .then(async () => {
          const response = await request.put(`/orders/refunds/${refund.orderNo}/approve`);
          if (response.code === 0 || response.code === 200) {
            ElMessage.success("退款申请已批准");
            this.pointsRefundDetailDialogVisible = false;
            this.selectedPointsRefund = null;
            this.loadPointsOrders();
          } else {
            ElMessage.error(response?.msg || "批准退款失败");
          }
        })
        .catch(() => {});
    },
    rejectPointsRefund(refund) {
      if (!refund || !refund.orderNo) return;
      ElMessageBox.prompt("请填写拒绝退款原因（用户可见）", "拒绝退款", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputPlaceholder: "请输入拒绝原因",
      })
        .then(async ({ value }) => {
          const rejectReason = String(value || "").trim();
          if (!rejectReason) {
            ElMessage.warning("请输入拒绝原因");
            return;
          }
          const response = await request.put(`/orders/refunds/${refund.orderNo}/reject`, {
            rejectReason,
          });
          if (response.code === 0 || response.code === 200) {
            ElMessage.success("退款申请已拒绝");
            this.pointsRefundDetailDialogVisible = false;
            this.selectedPointsRefund = null;
            this.loadPointsOrders();
          } else {
            ElMessage.error(response?.msg || "拒绝退款失败");
          }
        })
        .catch(() => {});
    },
    exportPointsOrders() {
      const rows = this.filteredPointsOrders || [];
      const header = ["订单号", "客户", "电话", "地址", "商品", "订单金额", "状态", "退款状态", "退款金额", "下单时间"];
      const lines = rows.map((row) => {
        const productText = (row.products || []).map((p) => `${p.name}x${p.quantity}`).join("；");
        return [
          row.orderNo || row.id || "",
          row.customer || "",
          row.phone || "",
          row.address || "",
          productText,
          this.formatPointsAmount(row.totalAmount || 0),
          this.orderStatusLabel(row.status),
          row.refundRecord ? this.getRefundStatusText(row.refundRecord.status) : "无退款",
          row.refundRecord ? this.formatPointsAmount(row.refundRecord.refundAmount || 0) : "-",
          row.createTime || "",
        ];
      });
      const csv = [header, ...lines]
        .map((cols) => cols.map((c) => `"${String(c).replace(/"/g, '""')}"`).join(","))
        .join("\n");
      const blob = new Blob(["\uFEFF" + csv], { type: "text/csv;charset=utf-8;" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `积分订单-${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
    },
    async savePointsLogisticsInfo() {
      if (!this.pointsLogisticsForm.shippingCompany || !this.pointsLogisticsForm.shippingNumber) {
        ElMessage.warning("请填写物流公司和物流单号");
        return;
      }
      try {
        const orderId = this.pointsDetailOrder.orderNo || this.pointsDetailOrder.id;
        let shippingTimeValue = this.pointsLogisticsForm.shippingTime;
        if (shippingTimeValue) {
          shippingTimeValue = String(shippingTimeValue).replace(" ", "T");
        } else {
          shippingTimeValue = new Date().toISOString().slice(0, 19);
        }
        const logisticsData = {
          shippingCompany: this.pointsLogisticsForm.shippingCompany,
          shippingNumber: this.pointsLogisticsForm.shippingNumber,
          shippingTime: shippingTimeValue,
          warehouseAddress: this.pointsLogisticsForm.warehouseAddress || "",
        };
        const response = await request.put(`/orders/${orderId}/logistics`, logisticsData);
        if (response.code === 0 || response.code === 200) {
          ElMessage.success("物流信息保存成功");
          await this.loadPointsLogisticsInfo();
          await this.loadPointsOrders();
        } else {
          ElMessage.error(response.msg || "保存失败");
        }
      } catch (error) {
        ElMessage.error(error.message || "保存物流信息失败");
      }
    },
    async loadPointsLogisticsInfo() {
      if (!this.pointsDetailOrder || !this.pointsDetailOrder.orderNo) return;
      try {
        const orderId = this.pointsDetailOrder.orderNo || this.pointsDetailOrder.id;
        const response = await request.get(`/orders/${orderId}/logistics`);
        if ((response.code === 0 || response.code === 200) && response.data) {
          const d = response.data;
          let shippingTime = d.shippingTime || null;
          if (shippingTime && String(shippingTime).includes(" ")) {
            shippingTime = String(shippingTime).replace(" ", "T");
          }
          this.pointsLogisticsForm = {
            shippingCompany: d.shippingCompany || "",
            shippingNumber: d.shippingNumber || "",
            shippingTime,
            warehouseAddress: d.warehouseAddress || "",
          };
          this.pointsDetailOrder.shippingCompany = d.shippingCompany || "";
          this.pointsDetailOrder.shippingNumber = d.shippingNumber || "";
          this.pointsDetailOrder.shipTime = d.shippingTime || "";
          this.pointsDetailOrder.warehouseAddress = d.warehouseAddress || "";
        }
      } catch (e) {
        // 静默处理，避免影响详情展示
      }
    },
    async loadPointsWarehouses() {
      try {
        const response = await request.get("/orders/warehouses");
        if ((response.code === 0 || response.code === 200) && Array.isArray(response.data)) {
          this.pointsWarehouseList = response.data;
        }
      } catch (e) {
        this.pointsWarehouseList = [];
      }
    },
    async addPointsWarehouseFromInput() {
      const addr = (this.pointsLogisticsForm.warehouseAddress || "").trim();
      if (!addr) {
        ElMessage.warning("请先输入发货仓地址再添加");
        return;
      }
      try {
        const response = await request.post("/orders/warehouses", { address: addr });
        if (response.code === 0 || response.code === 200) {
          ElMessage.success("已添加发货仓");
          await this.loadPointsWarehouses();
        } else {
          ElMessage.error(response.msg || "添加失败");
        }
      } catch (error) {
        ElMessage.error(error.message || "添加失败");
      }
    },
    selectPointsWarehouse(w) {
      if (w && w.address) {
        this.pointsLogisticsForm.warehouseAddress = w.address;
        ElMessage.success("已填入发货仓地址");
      }
    },
    async deletePointsWarehouse(w) {
      if (!w || !w.id) return;
      try {
        await ElMessageBox.confirm(`确定删除发货仓「${w.address}」？`, '提示', { type: 'warning' });
        const res = await request.delete(`/orders/warehouses/${w.id}`);
        if (res.code === 0 || res.code === 200) {
          ElMessage.success('已删除');
          await this.loadPointsWarehouses();
        } else {
          ElMessage.error(res.msg || '删除失败');
        }
      } catch (e) {
        if (e !== 'cancel') ElMessage.error(e.message || '删除失败');
      }
    },
    // 设置上传请求头
    setUploadHeaders() {
      const token = store.state.user?.accessToken || localStorage.getItem("token");
      if (token) {
        this.uploadHeaders = {
          "Authorization": "Bearer " + token
        };
      } else {
        this.uploadHeaders = {};
      }
    },
    // 加载商品列表
    async loadProducts() {
      this.loading = true;
      try {
        const res = await productApi.getAllProducts();
        console.log("商品列表API响应:", res);
        if (res && res.code === 200 && res.data) {
          // 后端返回格式可能是 { goods: [...], records: [...], ... } 或直接是数组
          let products = [];
          if (Array.isArray(res.data)) {
            products = res.data;
          } else if (res.data.records && Array.isArray(res.data.records)) {
            products = res.data.records;
          } else if (res.data.goods && Array.isArray(res.data.goods)) {
            products = res.data.goods;
          } else if (res.data.list && Array.isArray(res.data.list)) {
            products = res.data.list;
          }
          
          // 只显示积分商城的商品
          this.allProducts = products.filter(product => 
            product && product.category === "积分商城"
          );
          console.log("积分商城商品列表:", this.allProducts);
        } else {
          this.allProducts = [];
          ElMessage.warning("获取商品列表失败");
        }
      } catch (error) {
        console.error("加载商品列表失败:", error);
        this.allProducts = [];
        ElMessage.error("加载商品列表失败: " + (error.message || "未知错误"));
      } finally {
        this.loading = false;
      }
    },
    
    // 显示添加商品对话框
    showAddProductDialog() {
      this.editingProduct = null;
      this.productForm = {
        name: "",
        description: "",
        price: 0,
        stock: 0,
        status: 1,
        image: "",
        category: "积分商城"
      };
      this.productDialogVisible = true;
    },
    
    // 编辑商品
    editProduct(row) {
      this.editingProduct = row;
      this.productForm = {
        name: row.name || "",
        description: row.description || "",
        price: this.formatPriceFromG(row.price), // 从g转换为kg
        stock: row.stock || 0,
        status: row.status !== undefined ? row.status : 1,
        image: row.image || "",
        category: "积分商城"
      };
      this.productDialogVisible = true;
    },
    
    // 保存商品
    async saveProduct() {
      this.$refs.productFormRef.validate(async (valid) => {
        if (!valid) {
          return false;
        }
        
        this.saving = true;
        try {
          const formData = {
            ...this.productForm,
            // 价格从kg转换为g（后端存储单位）
            price: this.productForm.price * 1000,
            category: "积分商城" // 确保category是积分商城
          };
          
          let res;
          if (this.editingProduct) {
            // 更新商品
            res = await productApi.update(this.editingProduct.id, formData);
          } else {
            // 创建商品
            res = await productApi.create(formData);
          }
          
          if (res && res.code === 200) {
            ElMessage.success(this.editingProduct ? "更新成功" : "添加成功");
            this.productDialogVisible = false;
            await this.loadProducts();
          } else {
            ElMessage.error(res?.msg || "操作失败");
          }
        } catch (error) {
          console.error("保存商品失败:", error);
          ElMessage.error("保存商品失败: " + (error.message || "未知错误"));
        } finally {
          this.saving = false;
        }
      });
    },
    
    // 删除商品
    async deleteProduct(row) {
      try {
        await ElMessageBox.confirm(
          `确定要删除商品"${row.name}"吗？`,
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
        );
        
        const res = await productApi.delete(row.id);
        if (res && res.code === 200) {
          ElMessage.success("删除成功");
          await this.loadProducts();
        } else {
          ElMessage.error(res?.msg || "删除失败");
        }
      } catch (error) {
        if (error !== "cancel") {
          console.error("删除商品失败:", error);
          ElMessage.error("删除商品失败: " + (error.message || "未知错误"));
        }
      }
    },
    
    // 格式化价格（从g转换为kg显示）
    formatPrice(priceInG) {
      if (!priceInG) return 0;
      // 如果price是BigDecimal或数字，转换为kg
      const price = typeof priceInG === 'number' ? priceInG : parseFloat(priceInG);
      return Math.floor(price / 1000);
    },
    
    // 从g转换为kg（用于编辑时显示）
    formatPriceFromG(priceInG) {
      if (!priceInG) return 0;
      const price = typeof priceInG === 'number' ? priceInG : parseFloat(priceInG);
      return Math.floor(price / 1000);
    },
    
    // 获取图片URL
    getImageUrl(row) {
      const base = getImageBase();
      if (!row || !row.image) {
        return base + "/static/images/login-dog.png";
      }
      let imageUrl = row.image;
      if (!imageUrl.startsWith("http")) {
        if (imageUrl.startsWith("/")) {
          imageUrl = base + imageUrl;
        } else {
          imageUrl = base + "/" + imageUrl;
        }
      }
      return imageUrl;
    },
    
    // 处理图片加载错误
    handleImageError() {
      console.error("图片加载失败");
    },
    
    // 获取库存类型
    getStockType(stock) {
      if (stock > 50) return "success";
      if (stock > 10) return "warning";
      return "danger";
    },
    
    // 获取状态类型
    getStatusType(status) {
      return status === 1 ? "success" : "info";
    },
    
    // 获取状态文本
    getStatusText(status) {
      return status === 1 ? "上架" : "下架";
    },
    
    // 分页大小改变
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
    },
    
    // 当前页改变
    handleCurrentChange(val) {
      this.currentPage = val;
    },
    
    // 图片上传成功
    handleImageSuccess(response, file) {
      console.log("图片上传成功:", response);
      if (response && response.code === 200 && response.data) {
        let imageUrl = response.data;
        if (typeof imageUrl === 'string') {
          const base = getImageBase();
          if (!imageUrl.startsWith("http")) {
            if (imageUrl.startsWith("/")) {
              imageUrl = base + imageUrl;
            } else {
              imageUrl = base + "/" + imageUrl;
            }
          }
          this.productForm.image = imageUrl;
          ElMessage.success("图片上传成功");
        } else {
          ElMessage.error("图片上传失败：返回数据格式错误");
        }
      } else {
        ElMessage.error(response?.msg || "图片上传失败");
      }
    },
    
    // 上传错误
    handleUploadError(err) {
      console.error("图片上传失败:", err);
      ElMessage.error("图片上传失败");
    },
    
    // 上传前验证（仅校验类型，超过 2MB 的会在 customImageUpload 中自动压缩）
    beforeImageUpload(file) {
      const isImage = file.type === "image/jpeg" || file.type === "image/png" || file.type === "image/webp" || file.type.startsWith("image/");
      if (!isImage) {
        ElMessage.error("只能上传 jpg、png 等图片文件！");
        return false;
      }
      return true;
    },
    // 客户端压缩图片：限制尺寸与体积，返回 Blob
    compressImage(file) {
      const maxSizeMB = 2;
      const maxW = 800;
      const maxH = 600;
      return new Promise((resolve, reject) => {
        const img = new Image();
        const url = URL.createObjectURL(file);
        img.onload = () => {
          URL.revokeObjectURL(url);
          let w = img.width;
          let h = img.height;
          if (w > maxW || h > maxH) {
            const r = Math.min(maxW / w, maxH / h);
            w = Math.round(w * r);
            h = Math.round(h * r);
          }
          const canvas = document.createElement("canvas");
          canvas.width = w;
          canvas.height = h;
          const ctx = canvas.getContext("2d");
          ctx.drawImage(img, 0, 0, w, h);
          // 统一用 jpeg 便于通过 quality 控制体积，保证能压到 2MB 以内
          const mime = "image/jpeg";
          let quality = 0.85;
          const tryBlob = () => {
            canvas.toBlob(
              (blob) => {
                if (!blob) {
                  reject(new Error("压缩失败"));
                  return;
                }
                if (blob.size <= maxSizeMB * 1024 * 1024 || quality <= 0.3) {
                  resolve(blob);
                  return;
                }
                quality = Math.max(0.3, quality - 0.15);
                tryBlob();
              },
              mime,
              quality
            );
          };
          tryBlob();
        };
        img.onerror = () => {
          URL.revokeObjectURL(url);
          reject(new Error("图片加载失败"));
        };
        img.src = url;
      });
    },
    // 自定义上传：先压缩（若需），再上传
    async customImageUpload({ file }) {
      const maxSizeMB = 2;
      const needCompress = file.size > maxSizeMB * 1024 * 1024;
      let data = file;
      if (needCompress) {
        try {
          ElMessage.info("图片较大，正在压缩…");
          const blob = await this.compressImage(file);
          const name = file.name.replace(/\.[^.]+$/, "") + ".jpg";
          data = new File([blob], name, { type: blob.type });
        } catch (e) {
          ElMessage.error("图片压缩失败，请换一张图或缩小后重试");
          return;
        }
      }
      const formData = new FormData();
      formData.append("file", data);
      const url = (getImageBase() || "http://localhost:8080") + "/api/admin/upload?type=product";
      try {
        const res = await axios.post(url, formData, {
          headers: {
            "Content-Type": "multipart/form-data",
            ...this.uploadHeaders
          }
        });
        const response = res.data;
        if (response && (response.code === 200 || response.code === 0) && response.data) {
          let imageUrl = response.data;
          if (typeof imageUrl === "string") {
            const base = getImageBase();
            if (!imageUrl.startsWith("http")) {
              imageUrl = imageUrl.startsWith("/") ? base + imageUrl : base + "/" + imageUrl;
            }
            this.productForm.image = imageUrl;
            ElMessage.success(needCompress ? "图片已压缩并上传成功" : "图片上传成功");
          } else {
            ElMessage.error("图片上传失败：返回数据格式错误");
          }
        } else {
          ElMessage.error(response?.msg || "图片上传失败");
        }
      } catch (err) {
        console.error("图片上传失败:", err);
        ElMessage.error("图片上传失败");
      }
    }
  }
};
</script>

<style scoped>
.points-mall-container {
  padding: 20px;
}

.points-mall-main-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.points-mall-main-tabs :deep(.el-tabs__content) {
  margin-top: -12px;
}

.points-mall-main-tabs :deep(.el-card__header) {
  padding-top: 0;
  padding-bottom: 10px;
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

.header-actions-only {
  justify-content: flex-end;
}

.product-name {
  display: flex;
  align-items: center;
}

.order-product {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.product-info {
  min-width: 0;
}

.product-meta {
  color: #909399;
  font-size: 12px;
}

.order-amount {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.amount-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.amount-value {
  font-size: 24px;
  font-weight: bold;
  color: #67c23a;
}

.product-cell {
  display: flex;
  align-items: center;
}

.product-spec {
  font-size: 12px;
  color: #999;
  margin-top: 3px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.avatar-uploader {
  width: 178px;
  height: 178px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.refund-detail {
  margin-top: 8px;
}
.warehouse-list-wrap {
  margin-top: 12px;
  padding: 12px;
  background: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}
.warehouse-list-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
  font-weight: 500;
}
.warehouse-item {
  font-size: 13px;
  color: #333;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.warehouse-item:last-child {
  margin-bottom: 0;
}
.warehouse-item:hover {
  background: #ecf5ff;
  border-color: #409eff;
}
.warehouse-item .delete-btn {
  color: #f56c6c;
  cursor: pointer;
  font-size: 12px;
  flex-shrink: 0;
  margin-left: 10px;
}
.warehouse-item .delete-btn:hover {
  color: #f23c3c;
}
</style>

