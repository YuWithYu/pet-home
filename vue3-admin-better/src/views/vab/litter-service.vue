<template>
  <div class="litter-service-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>上门铲屎服务管理</h1>
      <p>管理所有上门铲屎服务预约和状态</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon pending">📋</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待确认</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon confirmed">✅</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.confirmed }}</div>
          <div class="stat-label">已确认</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon completed">🎉</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon total">💰</div>
        <div class="stat-content">
          <div class="stat-number">¥{{ stats.totalRevenue }}</div>
          <div class="stat-label">总收入</div>
        </div>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filters-section">
      <div class="filter-group">
        <label>状态筛选：</label>
        <select v-model="filters.status" @change="loadServices">
          <option value="">全部状态</option>
          <option value="pending">待确认</option>
          <option value="confirmed">已确认</option>
          <option value="in_progress">进行中</option>
          <option value="completed">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>服务类型：</label>
        <select v-model="filters.serviceType" @change="loadServices">
          <option value="">全部类型</option>
          <option value="daily">日常清理</option>
          <option value="deep">深度清洁</option>
          <option value="emergency">紧急服务</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>搜索：</label>
        <input 
          type="text" 
          v-model="filters.search" 
          placeholder="搜索用户、地址或电话"
          @input="debounceSearch"
        />
      </div>
      
      <button class="refresh-btn" @click="loadServices">刷新</button>
    </div>

    <!-- 服务列表 -->
    <div class="services-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户信息</th>
            <th>服务类型</th>
            <th>服务地址</th>
            <th>服务时间</th>
            <th>价格</th>
            <th>状态</th>
            <th>服务人员</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="service in services" :key="service.id">
            <td>{{ service.id }}</td>
            <td>
              <div class="user-info">
                <div class="user-name">{{ service.userName || '未知用户' }}</div>
                <div class="user-phone">{{ service.userPhone || service.contactPhone }}</div>
              </div>
            </td>
            <td>
              <span class="service-type-badge" :class="service.serviceType">
                {{ service.serviceTypeText }}
              </span>
            </td>
            <td>
              <div class="address-info">
                <div class="address-main">{{ service.serviceAddress }}</div>
                <div class="address-detail" v-if="service.detailAddress">{{ service.detailAddress }}</div>
              </div>
            </td>
            <td>{{ formatDateTime(service.serviceTime) }}</td>
            <td class="price">¥{{ service.price }}</td>
            <td>
              <span class="status-badge" :class="service.status">
                {{ service.statusText }}
              </span>
            </td>
            <td>
              <div v-if="service.staffName" class="staff-info">
                {{ service.staffName }}
                <div class="staff-phone">{{ service.staffPhone }}</div>
              </div>
              <span v-else class="no-staff">未分配</span>
            </td>
            <td>
              <div class="action-buttons">
                <button 
                  v-if="service.status === 'pending'" 
                  class="btn btn-confirm"
                  @click="confirmService(service)"
                >
                  确认
                </button>
                <button 
                  v-if="service.status === 'confirmed'" 
                  class="btn btn-assign"
                  @click="assignStaff(service)"
                >
                  分配人员
                </button>
                <button 
                  v-if="service.status === 'in_progress'" 
                  class="btn btn-complete"
                  @click="completeService(service)"
                >
                  完成
                </button>
                <button 
                  v-if="['pending', 'confirmed'].includes(service.status)"
                  class="btn btn-cancel"
                  @click="cancelService(service)"
                >
                  取消
                </button>
                <button 
                  class="btn btn-detail"
                  @click="viewDetail(service)"
                >
                  详情
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <button 
        :disabled="currentPage === 1" 
        @click="changePage(currentPage - 1)"
        class="page-btn"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} 页，共 {{ totalPages }} 页
      </span>
      <button 
        :disabled="currentPage === totalPages" 
        @click="changePage(currentPage + 1)"
        class="page-btn"
      >
        下一页
      </button>
    </div>

    <!-- 服务详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>服务详情</h3>
          <button class="close-btn" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body" v-if="selectedService">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>服务ID：</label>
                <span>{{ selectedService.id }}</span>
              </div>
              <div class="detail-item">
                <label>服务类型：</label>
                <span>{{ selectedService.serviceTypeText }}</span>
              </div>
              <div class="detail-item">
                <label>服务时间：</label>
                <span>{{ formatDateTime(selectedService.serviceTime) }}</span>
              </div>
              <div class="detail-item">
                <label>服务价格：</label>
                <span>¥{{ selectedService.price }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>用户信息</h4>
            <div class="detail-grid">
              <div class="detail-item">
                <label>用户姓名：</label>
                <span>{{ selectedService.userName || '未知' }}</span>
              </div>
              <div class="detail-item">
                <label>联系电话：</label>
                <span>{{ selectedService.contactPhone }}</span>
              </div>
              <div class="detail-item">
                <label>宠物信息：</label>
                <span>{{ selectedService.petName || '未选择宠物' }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>服务地址</h4>
            <div class="address-detail">
              <div class="address-main">{{ selectedService.serviceAddress }}</div>
              <div class="address-sub" v-if="selectedService.detailAddress">{{ selectedService.detailAddress }}</div>
            </div>
          </div>
          
          <div class="detail-section" v-if="selectedService.description">
            <h4>服务描述</h4>
            <p>{{ selectedService.description }}</p>
          </div>
          
          <div class="detail-section" v-if="selectedService.specialRequirements">
            <h4>特殊要求</h4>
            <p>{{ selectedService.specialRequirements }}</p>
          </div>
          
          <div class="detail-section" v-if="selectedService.review">
            <h4>用户评价</h4>
            <div class="review-content">
              <div class="rating">
                <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= selectedService.rating }">★</span>
              </div>
              <p>{{ selectedService.review }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LitterServiceManagement',
  data() {
    return {
      services: [],
      stats: {
        pending: 0,
        confirmed: 0,
        completed: 0,
        totalRevenue: 0
      },
      filters: {
        status: '',
        serviceType: '',
        search: ''
      },
      currentPage: 1,
      totalPages: 1,
      showDetailModal: false,
      selectedService: null,
      searchTimeout: null
    }
  },
  
  mounted() {
    this.loadServices();
  },
  
  methods: {
    // 获取状态显示文本
    getStatusText(status) {
      const statusMap = {
        'pending': '待确认',
        'confirmed': '已确认',
        'in_progress': '进行中',
        'completed': '已完成',
        'cancelled': '已取消'
      };
      return statusMap[status] || status;
    },
    
    // 获取服务类型显示文本
    getServiceTypeText(type) {
      const typeMap = {
        'daily': '日常清理',
        'deep': '深度清洁',
        'emergency': '紧急服务'
      };
      return typeMap[type] || type || '日常清理';
    },
    // 加载服务列表
    async loadServices() {
      try {
        console.log('开始加载铲屎服务数据...');
        
        // 使用fetch直接调用API
        const response = await fetch('http://10.76.242.18:8080/api/litter-service/list', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        const data = await response.json();
        console.log('API响应:', response);
        console.log('响应数据:', data);
        
        if (data.code === 0 || data.code === 200) {
          let services = data.data || [];
          
          // 为每个服务添加显示文本
          services = services.map(service => ({
            ...service,
            statusText: this.getStatusText(service.status),
            serviceTypeText: this.getServiceTypeText(service.serviceType)
          }));
          
          // 应用筛选
          if (this.filters.status) {
            services = services.filter(service => service.status === this.filters.status);
          }
          
          if (this.filters.serviceType) {
            services = services.filter(service => service.serviceType === this.filters.serviceType);
          }
          
          if (this.filters.search) {
            const searchLower = this.filters.search.toLowerCase();
            services = services.filter(service => 
              service.userName?.toLowerCase().includes(searchLower) ||
              service.serviceAddress?.toLowerCase().includes(searchLower) ||
              service.contactPhone?.includes(searchLower)
            );
          }
          
          this.services = services;
          this.updateStats(data.data || []);
        } else {
          console.error('获取服务列表失败:', data.msg);
          this.services = [];
        }
      } catch (error) {
        console.error('加载服务列表失败:', error);
        this.services = [];
      }
    },
    
    // 更新统计数据
    updateStats(allServices) {
      this.stats.pending = allServices.filter(s => s.status === 'pending').length;
      this.stats.confirmed = allServices.filter(s => s.status === 'confirmed').length;
      this.stats.completed = allServices.filter(s => s.status === 'completed').length;
      this.stats.totalRevenue = allServices
        .filter(s => s.status === 'completed')
        .reduce((sum, s) => sum + parseFloat(s.price), 0);
    },
    
    // 防抖搜索
    debounceSearch() {
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => {
        this.loadServices();
      }, 500);
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      const date = new Date(dateTimeStr);
      return date.toLocaleString('zh-CN');
    },
    
    // 确认服务
    async confirmService(service) {
      if (confirm('确认接受这个服务预约吗？')) {
        try {
          console.log('正在确认服务:', service.id);
          
          // 调用后端API更新状态
          const response = await fetch(`http://10.76.242.18:8080/api/litter-service/status/${service.id}?status=confirmed`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          const data = await response.json();
          console.log('确认服务API响应:', data);
          
          if (data.code === 0) {
            // 更新前端显示
            service.status = 'confirmed';
            service.statusText = '已确认';
            
            // 刷新列表获取最新数据
            await this.loadServices();
            
            alert('服务确认成功！');
          } else {
            alert('服务确认失败：' + data.msg);
          }
        } catch (error) {
          console.error('确认服务失败:', error);
          alert('确认服务失败，请重试');
        }
      }
    },
    
    // 分配服务人员
    async assignStaff(service) {
      const staffName = prompt('请输入服务人员姓名:');
      if (staffName) {
        const staffPhone = prompt('请输入服务人员电话:');
        
        try {
          console.log('正在分配服务人员:', service.id, staffName, staffPhone);
          
          // 调用后端API分配服务人员
          const response = await fetch(`http://10.76.242.18:8080/api/litter-service/assign-staff/${service.id}?staffName=${encodeURIComponent(staffName)}&staffPhone=${encodeURIComponent(staffPhone)}`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          const data = await response.json();
          console.log('分配服务人员API响应:', data);
          
          if (data.code === 0) {
            // 更新前端显示
            service.staffName = staffName;
            service.staffPhone = staffPhone;
            service.status = 'in_progress';
            service.statusText = '进行中';
            
            // 刷新列表获取最新数据
            await this.loadServices();
            
            alert(`服务人员分配成功！\n姓名：${staffName}\n电话：${staffPhone}`);
          } else {
            alert('服务人员分配失败：' + data.msg);
          }
        } catch (error) {
          console.error('分配服务人员失败:', error);
          alert('分配服务人员失败，请重试');
        }
      }
    },
    
    // 完成服务
    async completeService(service) {
      if (confirm('确认完成这个服务吗？')) {
        try {
          console.log('正在完成服务:', service.id);
          
          // 调用后端API更新状态
          const response = await fetch(`http://10.76.242.18:8080/api/litter-service/status/${service.id}?status=completed`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          const data = await response.json();
          console.log('完成服务API响应:', data);
          
          if (data.code === 0) {
            // 更新前端显示
            service.status = 'completed';
            service.statusText = '已完成';
            
            // 刷新列表获取最新数据
            await this.loadServices();
            
            alert('服务完成成功！');
          } else {
            alert('服务完成失败：' + data.msg);
          }
        } catch (error) {
          console.error('完成服务失败:', error);
          alert('完成服务失败，请重试');
        }
      }
    },
    
    // 取消服务
    async cancelService(service) {
      const reason = prompt('请输入取消原因:');
      if (reason) {
        try {
          console.log('正在取消服务:', service.id, reason);
          
          // 调用后端API取消服务
          const response = await fetch(`http://10.76.242.18:8080/api/litter-service/cancel/${service.id}?reason=${encodeURIComponent(reason)}`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          const data = await response.json();
          console.log('取消服务API响应:', data);
          
          if (data.code === 0) {
            // 更新前端显示
            service.status = 'cancelled';
            service.statusText = '已取消';
            
            // 刷新列表获取最新数据
            await this.loadServices();
            
            alert('服务取消成功！');
          } else {
            alert('服务取消失败：' + data.msg);
          }
        } catch (error) {
          console.error('取消服务失败:', error);
          alert('取消服务失败，请重试');
        }
      }
    },
    
    // 查看详情
    viewDetail(service) {
      this.selectedService = service;
      this.showDetailModal = true;
    },
    
    // 关闭详情弹窗
    closeDetailModal() {
      this.showDetailModal = false;
      this.selectedService = null;
    },
    
    // 分页
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
        this.loadServices();
      }
    }
  }
}
</script>

<style scoped>
.litter-service-management {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

/* 页面标题 */
.page-header {
  background: white;
  padding: 30px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.page-header h1 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 28px;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 15px;
}

.stat-icon.pending { background: #fff3e0; }
.stat-icon.confirmed { background: #e3f2fd; }
.stat-icon.completed { background: #e8f5e8; }
.stat-icon.total { background: #f3e5f5; }

.stat-content .stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-content .stat-label {
  font-size: 14px;
  color: #666;
}

/* 筛选区域 */
.filters-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-group label {
  font-weight: 500;
  color: #333;
}

.filter-group select,
.filter-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.refresh-btn {
  background: #ff6b35;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.refresh-btn:hover {
  background: #e55a2b;
}

/* 服务表格 */
.services-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.services-table table {
  width: 100%;
  border-collapse: collapse;
}

.services-table th,
.services-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.services-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.user-info .user-name {
  font-weight: 500;
  color: #333;
}

.user-info .user-phone {
  font-size: 12px;
  color: #666;
}

.service-type-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.service-type-badge.daily { background: #e3f2fd; color: #1976d2; }
.service-type-badge.deep { background: #f3e5f5; color: #7b1fa2; }
.service-type-badge.emergency { background: #ffebee; color: #d32f2f; }

.address-info .address-main {
  font-weight: 500;
  color: #333;
}

.address-info .address-detail {
  font-size: 12px;
  color: #666;
}

.price {
  font-weight: bold;
  color: #ff6b35;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending { background: #fff3e0; color: #f57c00; }
.status-badge.confirmed { background: #e3f2fd; color: #1976d2; }
.status-badge.in_progress { background: #e8f5e8; color: #388e3c; }
.status-badge.completed { background: #e8f5e8; color: #2e7d32; }
.status-badge.cancelled { background: #ffebee; color: #d32f2f; }

.staff-info .staff-name {
  font-weight: 500;
  color: #333;
}

.staff-info .staff-phone {
  font-size: 12px;
  color: #666;
}

.no-staff {
  color: #999;
  font-style: italic;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
}

.btn-confirm { background: #4caf50; color: white; }
.btn-assign { background: #2196f3; color: white; }
.btn-complete { background: #ff9800; color: white; }
.btn-cancel { background: #f44336; color: white; }
.btn-detail { background: #9e9e9e; color: white; }

.btn:hover {
  opacity: 0.8;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: #666;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-item label {
  font-weight: 500;
  color: #666;
  font-size: 12px;
  margin-bottom: 2px;
}

.detail-item span {
  color: #333;
  font-size: 14px;
}

.address-detail .address-main {
  font-weight: 500;
  color: #333;
  margin-bottom: 5px;
}

.address-detail .address-sub {
  color: #666;
  font-size: 14px;
}

.review-content .rating {
  margin-bottom: 10px;
}

.star {
  color: #ddd;
  font-size: 16px;
}

.star.filled {
  color: #ffc107;
}

@media (max-width: 768px) {
  .filters-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group {
    justify-content: space-between;
  }
  
  .services-table {
    overflow-x: auto;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
