<template>
  <div class="order-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header header-actions-only">
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜索订单..."
              clearable
              style="width: 200px; margin-right: 10px"
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select 
              v-model="filterStatus" 
              placeholder="状态筛选" 
              style="width: 120px; margin-right: 10px"
            >
              <el-option label="全部" value=""></el-option>
              <el-option label="待付款" value="pending"></el-option>
              <el-option label="待发货" value="paid"></el-option>
              <el-option label="已发货" value="shipped"></el-option>
              <el-option label="已完成" value="completed"></el-option>
              <el-option label="已取消" value="cancelled"></el-option>
            </el-select>
            <el-button type="primary" @click="exportOrders">导出订单</el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="filteredOrders" 
        style="width: 100%"
        row-key="id"
        v-loading="loading"
        @row-click="handleRowClick"
      >
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
            <div 
              v-for="product in row.products" 
              :key="product.id"
              class="order-product"
            >
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
                  <span>¥{{ product.price }} × {{ product.quantity }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
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
            {{ row.refundRecord ? `¥${row.refundRecord.refundAmount || 0}` : "-" }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button type="text" @click.stop="viewOrder(row)">发货</el-button>
            <el-button 
              v-if="row.status === 'pending'" 
              type="text" 
              @click.stop="cancelOrder(row)"
            >
              取消
            </el-button>
            <el-button
              v-if="row.refundRecord"
              type="text"
              @click.stop="viewRefundDetail(row.refundRecord)"
            >
              退款详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalOrders"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="订单详情"
      width="800px"
    >
      <el-row :gutter="20">
        <el-col :span="16">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="订单号">{{ detailOrder.orderNo || detailOrder.id }}</el-descriptions-item>
            <el-descriptions-item label="客户">{{ detailOrder.customer }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detailOrder.phone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ detailOrder.address }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(detailOrder.status)">
                {{ getStatusText(detailOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ detailOrder.createTime }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ detailOrder.payTime }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ detailOrder.shipTime }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
        <el-col :span="8">
          <div class="order-amount">
            <div class="amount-label">订单总额</div>
            <div class="amount-value">¥{{ detailOrder.totalAmount?.toLocaleString() }}</div>
          </div>
        </el-col>
      </el-row>
      
      <el-divider>商品信息</el-divider>
      
      <el-table :data="detailOrder.products" style="width: 100%">
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image 
                :src="row.image" 
                fit="cover" 
                style="width: 60px; height: 60px; border-radius: 4px; margin-right: 10px"
                :preview-src-list="row.image ? [row.image] : []"
                :initial-index="0"
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
                <div class="product-spec">{{ row.spec }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="subtotal" label="小计" width="100">
          <template #default="{ row }">
            ¥{{ row.subtotal }}
          </template>
        </el-table-column>
      </el-table>
      
      <el-divider>物流信息</el-divider>
      
      <el-steps :active="getLogisticsStep(detailOrder.status)" finish-status="success" simple>
        <el-step title="已下单" :description="detailOrder.createTime" />
        <el-step title="已付款" :description="detailOrder.payTime" />
        <el-step title="已发货" :description="detailOrder.shipTime" />
        <el-step title="已完成" />
      </el-steps>
      
      <!-- 物流信息设置表单 -->
      <el-form 
        :model="logisticsForm" 
        label-width="120px" 
        style="margin-top: 20px"
        v-if="detailOrder.status === 'paid' || detailOrder.status === 'shipped' || detailOrder.status === 1 || detailOrder.status === 2"
      >
        <el-form-item label="物流公司">
          <el-select 
            v-model="logisticsForm.shippingCompany" 
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
          <el-input 
            v-model="logisticsForm.shippingNumber" 
            placeholder="请输入物流单号"
            clearable
          />
        </el-form-item>
        <el-form-item label="发货时间">
          <el-date-picker
            v-model="logisticsForm.shippingTime"
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
            v-model="logisticsForm.warehouseAddress" 
            placeholder="请输入发货仓地址，或从下方已保存的地址中点击选择"
            clearable
          />
          <div style="margin-top: 8px;">
            <el-button type="primary" size="small" @click="addWarehouseFromInput">添加发货仓</el-button>
            <span style="margin-left: 8px; font-size: 12px; color: #999;">添加后会显示在下方，下次可直接点击选用</span>
          </div>
          <!-- 已保存的发货仓列表：添加后显示在下面，点击即可填入上方输入框 -->
          <div class="warehouse-list-wrap" v-if="warehouseList.length > 0">
            <div class="warehouse-list-title">已保存的发货仓（点击下方地址可填入，下次发货直接选）</div>
            <div 
              v-for="w in warehouseList" 
              :key="w.id" 
              class="warehouse-item"
              @click="selectWarehouse(w)"
            >
              <span>{{ w.address }}</span>
              <span class="delete-btn" @click.stop="deleteWarehouse(w)">删除</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveLogisticsInfo">保存物流信息</el-button>
          <el-button @click="loadLogisticsInfo">刷新物流信息</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 显示已有物流信息 -->
      <div v-if="detailOrder.shippingCompany || detailOrder.shippingNumber" style="margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 4px;">
        <div style="margin-bottom: 10px;"><strong>当前物流信息：</strong></div>
        <div v-if="detailOrder.shippingCompany" style="margin-bottom: 5px;">物流公司：{{ detailOrder.shippingCompany }}</div>
        <div v-if="detailOrder.shippingNumber" style="margin-bottom: 5px;">物流单号：{{ detailOrder.shippingNumber }}</div>
        <div v-if="detailOrder.shipTime" style="margin-bottom: 5px;">发货时间：{{ detailOrder.shipTime }}</div>
        <div v-if="detailOrder.warehouseAddress" style="margin-bottom: 5px;">发货仓地址：{{ detailOrder.warehouseAddress }}</div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="detailOrder.status === 'paid' || detailOrder.status === 1" 
            type="primary" 
            @click="shipOrder(detailOrder)"
          >
            立即发货
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="refundDetailDialogVisible"
      title="退款详情"
      width="600px"
    >
      <div v-if="selectedRefund" class="refund-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ selectedRefund.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ selectedRefund.refundAmount ? (typeof selectedRefund.refundAmount === 'number' ? selectedRefund.refundAmount.toLocaleString() : selectedRefund.refundAmount) : '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="退款原因" :span="2">{{ selectedRefund.reason }}</el-descriptions-item>
          <el-descriptions-item label="退款状态">
            <el-tag :type="getRefundStatusType(selectedRefund.status)">
              {{ getRefundStatusText(selectedRefund.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ selectedRefund.createTime }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ selectedRefund.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ selectedRefund.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ selectedRefund.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDetailDialogVisible = false">关闭</el-button>
          <el-button
            v-if="selectedRefund && selectedRefund.status === 'pending'"
            type="success"
            @click="approveRefund(selectedRefund)"
          >
            批准退款
          </el-button>
          <el-button
            v-if="selectedRefund && selectedRefund.status === 'pending'"
            type="danger"
            @click="rejectRefund(selectedRefund)"
          >
            拒绝退款
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Search } from "@element-plus/icons-vue";

import request from '@/utils/request'

// 使用统一的request工具，它会自动添加token
const api = request

export default {
  name: "Order",
  components: {
    Search
  },
  data() {
    return {
      searchText: "",
      filterStatus: "",
      currentPage: 1,
      pageSize: 10,
      totalOrders: 0,
      loading: false,
      detailDialogVisible: false,
      orders: [],
      detailOrder: {},
      logisticsForm: {
        shippingCompany: '',
        shippingNumber: '',
        shippingTime: null,
        warehouseAddress: ''
      },
      warehouseList: []
      ,
      refundLoading: false,
      refundList: [],
      refundDetailDialogVisible: false,
      selectedRefund: null
    };
  },
  computed: {
    filteredOrders() {
      let result = this.orders;
      
      // 搜索过滤
      if (this.searchText) {
        const searchLower = this.searchText.toLowerCase();
        result = result.filter(order => {
          const orderId = (order.id || '').toString().toLowerCase();
          const orderNo = (order.orderNo || '').toString().toLowerCase();
          const customer = (order.customer || '').toLowerCase();
          return orderId.includes(searchLower) || 
                 orderNo.includes(searchLower) ||
                 customer.includes(searchLower);
        });
      }
      
      // 状态过滤（如果后端已经过滤了，这里就不需要再过滤）
      // 但为了前端搜索功能，保留状态过滤逻辑
      if (this.filterStatus) {
        result = result.filter(order => order.status === this.filterStatus);
      }
      
      // 分页处理
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return result.slice(start, end);
    }
  },
  watch: {
    filterStatus() {
      // 当状态筛选改变时，重新加载数据
      this.currentPage = 1;
      this.loadOrders();
    }
  },
  methods: {
    async loadOrders() {
      this.loading = true;
      try {
        const response = await api.get('/orders/list', {
          params: {
            status: this.filterStatus || undefined,
            currentPage: this.currentPage,
            pageSize: this.pageSize,
            // 与积分商城兑换单（订单号 EX 开头）分开，本页仅普通商城订单
            orderCategory: 'mall'
          }
        });
        
        console.log('管理员后台 - 订单列表API响应:', response);
        console.log('管理员后台 - 响应数据类型:', typeof response);
        console.log('管理员后台 - 响应数据键:', Object.keys(response || {}));
        console.log('管理员后台 - 响应数据.code:', response?.code);
        console.log('管理员后台 - 响应数据.data:', response?.data);
        console.log('管理员后台 - 响应数据.data类型:', typeof response?.data);
        console.log('管理员后台 - 订单数据:', response?.data?.orders);
        console.log('管理员后台 - 订单数量:', response?.data?.orders?.length || 0);
        
        // 注意：request拦截器已经返回了data，所以response就是Result对象 {code, msg, data}
        // 兼容 code: 0 和 code: 200 两种情况
        if ((response.code === 0 || response.code === 200) && response.data) {
          // 转换后端数据格式为前端需要的格式
          const ordersData = response.data.orders || [];
          console.log('管理员后台 - 处理订单数据，数量:', ordersData.length);
          this.orders = ordersData.map(order => {
            // 转换商品数据格式
            const products = (order.products || order.orderItemList || []).map(product => {
              // 获取商品图片，兼容多个字段名，并处理相对路径
              let imageUrl = product.image || product.productImage || product.productPic || product.imageUrl || '';
              
              // 如果图片URL是相对路径，添加基础URL
              if (imageUrl && !imageUrl.startsWith('http')) {
                const baseUrl = api.defaults.baseURL || 'http://localhost:8080';
                if (!imageUrl.startsWith('/')) {
                  imageUrl = '/' + imageUrl;
                }
                imageUrl = `${baseUrl}${imageUrl}`;
              }
              
              return {
                id: product.id,
                name: product.productName || product.name || '商品',
                spec: product.specName || product.spec || '',
                price: product.price || product.productPrice || product.specPrice || 0,
                quantity: product.quantity || product.productQuantity || 1,
                subtotal: (product.price || product.productPrice || product.specPrice || 0) * (product.quantity || product.productQuantity || 1),
                image: imageUrl
              };
            });
            
            return {
              id: order.id || order.orderNo || '',
              orderNo: order.orderNo || order.id || '',
              customer: order.customer || '未知用户',
              phone: order.phone || '',
              address: order.address || '',
              totalAmount: order.totalAmount || 0,
              status: order.status || 'pending',
              createTime: order.createTime || '',
              payTime: order.payTime || '',
              shipTime: order.shipTime || '',
              completeTime: order.completeTime || '',
              cancelTime: order.cancelTime || '',
              products: products,
              refundRecord: null
            };
          });
          this.attachRefundToOrders();
          this.totalOrders = response.data.total || this.orders.length;
        } else {
          console.error('管理员后台 - 订单列表API返回错误:', response.data);
          this.orders = [];
          this.totalOrders = 0;
          this.$message.error('获取订单列表失败: ' + (response?.msg || response?.message || '未知错误'));
        }
      } catch (error) {
        console.error('获取订单列表异常:', error);
        this.orders = [];
        this.totalOrders = 0;
        this.$message.error('获取订单列表失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    attachRefundToOrders() {
      const map = new Map();
      (this.refundList || []).forEach((r) => {
        if (r && r.orderNo && !map.has(r.orderNo)) {
          map.set(r.orderNo, r);
        }
      });
      this.orders = (this.orders || []).map((o) => ({
        ...o,
        refundRecord: map.get(o.orderNo) || null
      }));
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      // 分页改变时重新加载数据
      this.loadOrders();
    },
    handleCurrentChange(val) {
      this.currentPage = val;
      // 分页改变时重新加载数据
      this.loadOrders();
    },
    getStatusText(status) {
      // 如果status是数字，转换为字符串状态
      if (typeof status === 'number') {
        const numStatusMap = {
          0: "待付款",
          1: "待发货",
          2: "已发货",
          3: "已完成",
          4: "已取消"
        };
        return numStatusMap[status] || `状态${status}`;
      }
      // 如果是字符串状态
      const statusMap = {
        "pending": "待付款",
        "paid": "待发货",
        "shipped": "已发货",
        "completed": "已完成",
        "cancelled": "已取消"
      };
      return statusMap[status] || status;
    },
    getStatusType(status) {
      // 如果status是数字，转换为字符串状态
      if (typeof status === 'number') {
        const numTypeMap = {
          0: "info",
          1: "warning",
          2: "",
          3: "success",
          4: "danger"
        };
        return numTypeMap[status] || "info";
      }
      // 如果是字符串状态
      const typeMap = {
        "pending": "info",
        "paid": "warning",
        "shipped": "",
        "completed": "success",
        "cancelled": "danger"
      };
      return typeMap[status] || "info";
    },
    getLogisticsStep(status) {
      // 如果status是数字，直接使用
      if (typeof status === 'number') {
        return status >= 0 && status <= 3 ? status : 0;
      }
      // 如果是字符串状态
      const stepMap = {
        "pending": 0,
        "paid": 1,
        "shipped": 2,
        "completed": 3,
        "cancelled": 0
      };
      return stepMap[status] || 0;
    },
    handleRowClick(row, column, event) {
      // 如果点击的是操作列中的按钮，不处理行点击事件
      if (event.target.closest('.el-button') || event.target.closest('.el-table-column--selection')) {
        return;
      }
      // 否则打开订单详情
      this.viewOrder(row);
    },
    async viewOrder(order) {
      // 尝试从后端获取完整订单详情
      try {
        // 优先使用订单号，如果没有则使用ID
        const orderId = order.orderNo || order.id;
        const response = await api.get(`/orders/${orderId}`);
        // 兼容 code: 0 和 code: 200 两种情况
        // 注意：request拦截器已经返回了data，所以response就是Result对象 {code, msg, data}
        if ((response.code === 0 || response.code === 200) && response.data) {
          const orderData = response.data;
          
          console.log('管理员后台 - 订单详情API响应:', response);
          console.log('管理员后台 - 订单详情数据:', orderData);
          console.log('管理员后台 - 联系电话字段:', orderData.phone, orderData.receiverPhone);
          console.log('管理员后台 - 收货地址字段:', orderData.address, orderData.receiverProvince, orderData.receiverCity, orderData.receiverDetailAddress);
          console.log('管理员后台 - 支付时间字段:', orderData.payTime, orderData.paymentTime);
          console.log('管理员后台 - 发货时间字段:', orderData.shipTime, orderData.shippingTime, orderData.deliveryTime);
          
          // 转换数据格式
          const products = (orderData.products || orderData.orderItemList || []).map(product => {
            // 获取商品图片，兼容多个字段名，并处理相对路径
            let imageUrl = product.image || product.productImage || product.productPic || product.imageUrl || '';
            
            // 如果图片URL是相对路径，添加基础URL
            if (imageUrl && !imageUrl.startsWith('http')) {
              // 使用已有的 baseURL
              const baseUrl = api.defaults.baseURL || 'http://localhost:8080';
              // 确保路径以 / 开头
              if (!imageUrl.startsWith('/')) {
                imageUrl = '/' + imageUrl;
              }
              imageUrl = `${baseUrl}${imageUrl}`;
            }
            
            console.log('管理员后台 - 商品图片处理:', {
              original: product.image || product.productImage || product.productPic,
              final: imageUrl
            });
            
            return {
              id: product.id,
              name: product.productName || product.name || '商品',
              spec: product.specName || product.spec || '',
              price: product.price || product.productPrice || product.specPrice || 0,
              quantity: product.quantity || product.productQuantity || 1,
              subtotal: (product.price || product.productPrice || product.specPrice || 0) * (product.quantity || product.productQuantity || 1),
              image: imageUrl
            };
          });
          
          // 组合完整地址（如果后端返回的是分开的字段）
          let fullAddress = orderData.address || '';
          if (!fullAddress && (orderData.receiverProvince || orderData.receiverCity || orderData.receiverDetailAddress)) {
            const parts = [];
            if (orderData.receiverProvince) parts.push(orderData.receiverProvince);
            if (orderData.receiverCity) parts.push(orderData.receiverCity);
            if (orderData.receiverRegion) parts.push(orderData.receiverRegion);
            if (orderData.receiverDetailAddress) parts.push(orderData.receiverDetailAddress);
            fullAddress = parts.join('');
          }
          
          // 获取联系电话（兼容多个字段名）
          const phone = orderData.phone || orderData.receiverPhone || '';
          
          // 获取支付时间（兼容多个字段名）
          const payTime = orderData.payTime || orderData.paymentTime || '';
          
          // 获取发货时间（兼容多个字段名）
          const shipTime = orderData.shipTime || orderData.shippingTime || orderData.deliveryTime || '';
          
          this.detailOrder = {
            id: orderData.id || orderData.orderNo || '',
            orderNo: orderData.orderNo || orderData.id || '',
            customer: orderData.customer || '未知用户',
            phone: phone,
            address: fullAddress,
            totalAmount: orderData.totalAmount || 0,
            status: orderData.status || 'pending',
            createTime: orderData.createTime || '',
            payTime: payTime,
            shipTime: shipTime,
            completeTime: orderData.completeTime || '',
            cancelTime: orderData.cancelTime || '',
            products: products,
            shippingCompany: orderData.shippingCompany || '',
            shippingNumber: orderData.shippingNumber || '',
            storeId: orderData.storeId || (products[0] && products[0].storeId) || null
          };
          
          // 填充物流信息表单
          // 处理发货时间格式
          let shippingTimeFormatted = orderData.shippingTime || shipTime || null;
          if (shippingTimeFormatted && shippingTimeFormatted.includes(' ')) {
            // 如果是空格分隔格式，转换为ISO格式（el-date-picker需要）
            shippingTimeFormatted = shippingTimeFormatted.replace(' ', 'T');
          }
          
          this.logisticsForm = {
            shippingCompany: orderData.shippingCompany || '',
            shippingNumber: orderData.shippingNumber || '',
            shippingTime: shippingTimeFormatted,
            warehouseAddress: orderData.warehouseAddress || ''
          };
          
          // 保存warehouseAddress到detailOrder
          this.detailOrder.warehouseAddress = orderData.warehouseAddress || '';
          this.detailOrder.shippingCompany = orderData.shippingCompany || '';
          this.detailOrder.shippingNumber = orderData.shippingNumber || '';
          
          console.log('管理员后台 - 初始化物流表单:', this.logisticsForm);
        } else {
          // 如果获取详情失败，使用当前订单数据
          this.detailOrder = { ...order };
        }
      } catch (error) {
        console.error('获取订单详情失败:', error);
        // 如果获取详情失败，使用当前订单数据
      this.detailOrder = { ...order };
      }
      this.detailDialogVisible = true;
      this.loadWarehouses();
      // 打开对话框后，如果有物流单号，尝试加载物流信息
      if (this.detailOrder.shippingNumber) {
        this.loadLogisticsInfo();
      }
    },
    shipOrder(order) {
      this.$confirm(`确定要为订单${order.orderNo || order.id}发货吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        // 使用订单号或订单ID，优先使用订单号
        const orderId = order.orderNo || order.id;
        console.log('管理员后台 - 发货订单，订单标识:', orderId);
        console.log('管理员后台 - 订单信息:', order);
        
        // 调用后端API更新订单状态
        api.put(`/orders/${orderId}/status`, null, {
          params: { status: 'shipped' }
        }).then(response => {
          console.log('管理员后台 - 发货API响应:', response);
          console.log('管理员后台 - 响应数据:', response);
          
          // 注意：request拦截器已经返回了data，所以response就是Result对象 {code, msg, data}
          // 兼容 code: 0 和 code: 200 两种情况
          if (response.code === 0 || response.code === 200) {
              this.$message.success("订单已发货");
            // 重新加载订单列表
            this.loadOrders();
            // 如果在详情对话框中操作，关闭对话框
            if (this.detailOrder.id === order.id || this.detailOrder.orderNo === order.orderNo) {
              this.detailDialogVisible = false;
            }
          } else {
            this.$message.error(response.data.message || response.data.msg || "发货失败");
          }
        }).catch(error => {
          console.error('管理员后台 - 发货失败:', error);
          console.error('管理员后台 - 错误详情:', error.response);
          const errorMsg = error.response?.data?.message || error.response?.data?.msg || "发货失败，请稍后重试";
          this.$message.error(errorMsg);
        });
      }).catch(() => {
        this.$message.info("已取消操作");
      });
    },
    cancelOrder(order) {
      this.$confirm(`确定要取消订单${order.orderNo || order.id}吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        // 使用订单号或订单ID，优先使用订单号
        const orderId = order.orderNo || order.id;
        console.log('管理员后台 - 取消订单，订单标识:', orderId);
        
        // 调用后端API更新订单状态
        api.put(`/orders/${orderId}/status`, null, {
          params: { status: 'cancelled' }
        }).then(response => {
          console.log('管理员后台 - 取消订单API响应:', response);
          
          // 注意：request拦截器已经返回了data，所以response就是Result对象 {code, msg, data}
          // 兼容 code: 0 和 code: 200 两种情况
          if (response.code === 0 || response.code === 200) {
              this.$message.success("订单已取消");
            // 重新加载订单列表
            this.loadOrders();
            // 如果在详情对话框中操作，关闭对话框
            if (this.detailOrder.id === order.id || this.detailOrder.orderNo === order.orderNo) {
              this.detailDialogVisible = false;
            }
          } else {
            this.$message.error(response.data.message || response.data.msg || "取消失败");
          }
        }).catch(error => {
          console.error('管理员后台 - 取消失败:', error);
          console.error('管理员后台 - 错误详情:', error.response);
          const errorMsg = error.response?.data?.message || error.response?.data?.msg || "取消失败，请稍后重试";
          this.$message.error(errorMsg);
        });
      }).catch(() => {
        this.$message.info("已取消操作");
      });
    },
    exportOrders() {
      this.$message.success("订单导出成功");
    },
    handleSearch() {
      // 搜索时重置到第一页
      this.currentPage = 1;
    },
    async saveLogisticsInfo() {
      if (!this.logisticsForm.shippingCompany || !this.logisticsForm.shippingNumber) {
        this.$message.warning('请填写物流公司和物流单号');
        return;
      }
      
      try {
        const orderId = this.detailOrder.orderNo || this.detailOrder.id;
        
        // 处理发货时间格式：如果使用el-date-picker的value-format="YYYY-MM-DDTHH:mm:ss"，需要转换为ISO格式
        let shippingTimeValue = this.logisticsForm.shippingTime;
        if (shippingTimeValue) {
          // 如果已经是ISO格式（包含T），直接使用
          if (shippingTimeValue.includes('T')) {
            // 确保格式正确：YYYY-MM-DDTHH:mm:ss
            shippingTimeValue = shippingTimeValue.replace(' ', 'T');
          } else {
            // 如果是其他格式，转换为ISO格式
            shippingTimeValue = shippingTimeValue.replace(' ', 'T');
          }
        } else {
          // 如果没有选择时间，使用当前时间，格式为ISO格式
          const now = new Date();
          shippingTimeValue = now.toISOString().slice(0, 19);
        }
        
        const logisticsData = {
          shippingCompany: this.logisticsForm.shippingCompany,
          shippingNumber: this.logisticsForm.shippingNumber,
          shippingTime: shippingTimeValue,
          warehouseAddress: this.logisticsForm.warehouseAddress || ''
        };
        
        console.log('管理员后台 - 保存物流信息:', orderId, logisticsData);
        
        const response = await api.put(`/orders/${orderId}/logistics`, logisticsData);
        
        // 注意：request拦截器已经返回了data，所以response就是Result对象 {code, msg, data}
        if (response.code === 0 || response.code === 200) {
          this.$message.success('物流信息保存成功');
          
          // 重新加载订单详情，确保获取最新数据
          await this.viewOrder(this.detailOrder);
          
          // 重新加载订单列表
          this.loadOrders();
        } else {
          this.$message.error(response.data.message || response.data.msg || '保存失败');
        }
      } catch (error) {
        console.error('管理员后台 - 保存物流信息失败:', error);
        console.error('错误详情:', error.response);
        const errorMsg = error.response?.data?.message || error.response?.data?.msg || error.message || '保存失败，请稍后重试';
        this.$message.error(errorMsg);
      }
    },
    async loadLogisticsInfo() {
      try {
        const orderId = this.detailOrder.orderNo || this.detailOrder.id;
        const response = await api.get(`/orders/${orderId}/logistics`);
        
        // 注意：request拦截器已经返回了data，所以response就是Result对象 {code, msg, data}
        if ((response.code === 0 || response.code === 200) && response.data) {
          const logisticsData = response.data;
          
          // 处理发货时间格式：后端返回的可能是ISO格式，需要转换为el-date-picker需要的格式
          let shippingTimeFormatted = logisticsData.shippingTime || null;
          if (shippingTimeFormatted && shippingTimeFormatted.includes('T')) {
            // 保持ISO格式，el-date-picker的value-format="YYYY-MM-DDTHH:mm:ss"可以接受
            // 不需要转换
          } else if (shippingTimeFormatted && shippingTimeFormatted.includes(' ')) {
            // 如果是空格分隔的格式，转换为ISO格式
            shippingTimeFormatted = shippingTimeFormatted.replace(' ', 'T');
          }
          
          this.logisticsForm = {
            shippingCompany: logisticsData.shippingCompany || '',
            shippingNumber: logisticsData.shippingNumber || '',
            shippingTime: shippingTimeFormatted,
            warehouseAddress: logisticsData.warehouseAddress || ''
          };
          
          // 更新订单详情显示
          this.detailOrder.shippingCompany = logisticsData.shippingCompany || '';
          this.detailOrder.shippingNumber = logisticsData.shippingNumber || '';
          this.detailOrder.shipTime = logisticsData.shippingTime || '';
          this.detailOrder.warehouseAddress = logisticsData.warehouseAddress || '';
          
          console.log('管理员后台 - 加载物流信息成功:', this.logisticsForm);
          this.$message.success('物流信息已刷新');
        }
      } catch (error) {
        console.error('管理员后台 - 加载物流信息失败:', error);
        this.$message.error('加载物流信息失败');
      }
    },
    async loadWarehouses() {
      try {
        const params = {};
        if (this.detailOrder && this.detailOrder.storeId != null) {
          params.storeId = this.detailOrder.storeId;
        }
        const response = await api.get('/orders/warehouses', { params });
        if ((response.code !== 0 && response.code !== 200) || response.data == null) return;
        // 只有明确拿到数组时才覆盖列表，否则保留当前列表，避免“一闪而过”
        let list = null;
        if (Array.isArray(response.data)) list = response.data;
        else if (response.data && Array.isArray(response.data.data)) list = response.data.data;
        if (list !== null) this.warehouseList = list;
      } catch (e) {
        // 失败时不清空列表，避免添加后因接口失败导致“一闪而过”
      }
    },
    async addWarehouseFromInput() {
      const addr = (this.logisticsForm.warehouseAddress || '').trim();
      if (!addr) {
        this.$message.warning('请先输入发货仓地址再添加');
        return;
      }
      const payload = { address: addr };
      if (this.detailOrder && this.detailOrder.storeId != null) {
        payload.storeId = this.detailOrder.storeId;
      }
      try {
        const response = await api.post('/orders/warehouses', payload);
        if (response.code === 0 || response.code === 200) {
          const added = response.data;
          const addr = added && (typeof added === 'string' ? added : added.address);
          if (addr) {
            this.warehouseList = this.warehouseList || [];
            const has = this.warehouseList.some(w => w.address === addr || (added && added.id != null && w.id === added.id));
            if (!has) {
              this.warehouseList = [...this.warehouseList, {
                id: added && added.id,
                address: addr,
                storeId: added && added.storeId,
                createTime: added && added.createTime
              }];
            }
          }
          this.$message.success('已添加发货仓，已显示在下方列表');
          await this.loadWarehouses();
        } else {
          this.$message.error(response.msg || response.message || '添加失败');
        }
      } catch (error) {
        const msg = (error.response && error.response.data && error.response.data.msg) || error.message || '添加失败';
        this.$message.error(msg);
      }
    },
    selectWarehouse(w) {
      if (w && w.address) {
        this.logisticsForm.warehouseAddress = w.address;
        this.$message.success('已填入发货仓地址');
      }
    },
    async deleteWarehouse(w) {
      if (!w || !w.id) return;
      try {
        await this.$confirm(`确定删除发货仓「${w.address}」？`, '提示', { type: 'warning' });
        const res = await api.delete(`/orders/warehouses/${w.id}`);
        if (res.code === 0 || res.code === 200) {
          this.$message.success('已删除');
          await this.loadWarehouses();
        } else {
          this.$message.error(res.msg || '删除失败');
        }
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e.message || '删除失败');
      }
    },
    async loadRefundList() {
      this.refundLoading = true;
      try {
        const response = await api.get('/orders/refunds', {
          params: {
            page: 1,
            size: 1000
          }
        });
        if (response && response.code === 200) {
          this.refundList = response.data?.records || [];
          this.attachRefundToOrders();
        } else {
          this.refundList = [];
          this.attachRefundToOrders();
          this.$message.error(response?.msg || '获取退款申请列表失败');
        }
      } catch (error) {
        console.error('加载退款申请列表失败:', error);
        this.refundList = [];
        this.attachRefundToOrders();
        this.$message.error('加载退款申请列表失败');
      } finally {
        this.refundLoading = false;
      }
    },
    getRefundStatusText(status) {
      const statusMap = {
        pending: '待处理',
        approved: '已批准',
        rejected: '已拒绝'
      };
      return statusMap[status] || '未知';
    },
    getRefundStatusType(status) {
      const typeMap = {
        pending: 'warning',
        approved: 'success',
        rejected: 'danger'
      };
      return typeMap[status] || '';
    },
    async approveRefund(refund) {
      try {
        await this.$confirm(`确定要批准订单 ${refund.orderNo} 的退款申请吗？`, '确认批准', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        const response = await api.put(`/orders/refunds/${refund.orderNo}/approve`);
        if (response && response.code === 200) {
          this.$message.success('退款申请已批准');
          await this.loadRefundList();
          await this.loadOrders();
        } else {
          this.$message.error(response?.msg || '批准退款失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('批准退款失败');
      }
    },
    async rejectRefund(refund) {
      try {
        const { value: rejectReason } = await this.$prompt('请填写拒绝退款原因（用户可见）', '拒绝退款', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputType: 'textarea',
          inputPlaceholder: '例如：商品已签收超过售后时效，且无质量问题凭证',
          inputValidator: (value) => {
            if (!value || !value.trim()) return '请填写拒绝原因';
            if (value.trim().length < 2) return '拒绝原因至少 2 个字';
            return true;
          }
        });
        await this.$confirm(`确定要拒绝订单 ${refund.orderNo} 的退款申请吗？`, '确认拒绝', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        const response = await api.put(`/orders/refunds/${refund.orderNo}/reject`, {
          rejectReason: rejectReason.trim()
        });
        if (response && response.code === 200) {
          this.$message.success('退款申请已拒绝');
          if (this.selectedRefund && this.selectedRefund.orderNo === refund.orderNo) {
            this.selectedRefund = {
              ...this.selectedRefund,
              status: 'rejected',
              rejectReason: rejectReason.trim()
            };
          }
          await this.loadRefundList();
          await this.loadOrders();
        } else {
          this.$message.error(response?.msg || '拒绝退款失败');
        }
      } catch (error) {
        if (error !== 'cancel') this.$message.error('拒绝退款失败');
      }
    },
    viewRefundDetail(refund) {
      this.selectedRefund = refund;
      this.refundDetailDialogVisible = true;
    }
  },
  mounted() {
    this.loadOrders();
    this.loadRefundList();
  }
};
</script>

<style lang="scss" scoped>
.order-container {
  padding: 0;
  width: 100%;
  box-sizing: border-box;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }

  .header-actions-only {
    justify-content: flex-end;
  }

  :deep(.el-card__body) {
    width: 100%;
    box-sizing: border-box;
  }
  
  .order-product {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .product-info {
      .product-meta {
        font-size: 12px;
        color: #999;
        margin-top: 3px;
      }
    }
  }
  
  .order-amount {
    text-align: center;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 4px;
    
    .amount-label {
      font-size: 14px;
      color: #666;
      margin-bottom: 10px;
    }
    
    .amount-value {
      font-size: 24px;
      font-weight: bold;
      color: #fa541c;
    }
  }
  
  .product-cell {
    display: flex;
    align-items: center;
    
    .product-spec {
      font-size: 12px;
      color: #999;
      margin-top: 3px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    text-align: right;
  }
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

.refund-detail {
  padding: 10px 0;
}
</style>