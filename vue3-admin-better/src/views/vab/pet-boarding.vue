<template>
  <div class="pet-boarding-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>宠物寄养管理</h1>
      <p>管理所有宠物寄养预约和状态</p>
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
        <div class="stat-icon in-progress">🏠</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.inProgress }}</div>
          <div class="stat-label">寄养中</div>
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
        <select v-model="filters.status" @change="loadBoardings">
          <option value="">全部状态</option>
          <option value="pending">待确认</option>
          <option value="confirmed">已确认</option>
          <option value="in_progress">寄养中</option>
          <option value="completed">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>寄养类型：</label>
        <select v-model="filters.boardingType" @change="loadBoardings">
          <option value="">全部类型</option>
          <option value="daily">日间寄养</option>
          <option value="overnight">过夜寄养</option>
          <option value="weekly">周寄养</option>
          <option value="monthly">月寄养</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>搜索：</label>
        <input 
          type="text" 
          v-model="filters.search" 
          placeholder="搜索用户、宠物或电话"
          @input="debounceSearch"
        />
      </div>
      
      <button class="refresh-btn" @click="loadBoardings">刷新</button>
    </div>

    <!-- 寄养服务列表 -->
    <div class="boardings-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户信息</th>
            <th>宠物信息</th>
            <th>寄养类型</th>
            <th>寄养时间</th>
            <th>价格</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="boarding in boardings" :key="boarding.id">
            <td>{{ boarding.id }}</td>
            <td>
              <div class="user-info">
                <div class="user-name">{{ boarding.userName || '未知用户' }}</div>
                <div class="user-phone">{{ boarding.userPhone }}</div>
              </div>
            </td>
            <td>
              <div class="pet-info">
                <div class="pet-name">{{ boarding.petName || '未知宠物' }}</div>
                <div class="pet-type">{{ boarding.petType }} - {{ boarding.petBreed }}</div>
              </div>
            </td>
            <td>
              <span class="boarding-type-badge" :class="boarding.boardingType">
                {{ boarding.boardingTypeText }}
              </span>
            </td>
            <td>
              <div class="time-info">
                <div class="start-time">{{ formatDateTime(boarding.startTime) }}</div>
                <div class="end-time">{{ formatDateTime(boarding.endTime) }}</div>
              </div>
            </td>
            <td class="price-cell">¥{{ boarding.price }}</td>
            <td>
              <span class="status-badge" :class="boarding.status">
                {{ boarding.statusText }}
              </span>
            </td>
            <td>
              <div class="action-buttons">
                <button 
                  v-if="boarding.status === 'pending'" 
                  class="confirm-btn" 
                  @click="confirmBoarding(boarding)"
                >
                  确认
                </button>
                <button 
                  v-if="boarding.status === 'confirmed'" 
                  class="start-btn" 
                  @click="startBoarding(boarding)"
                >
                  开始寄养
                </button>
                <button 
                  v-if="boarding.status === 'in_progress'" 
                  class="complete-btn" 
                  @click="completeBoarding(boarding)"
                >
                  完成寄养
                </button>
                <button 
                  v-if="boarding.status !== 'completed' && boarding.status !== 'cancelled'" 
                  class="cancel-btn" 
                  @click="cancelBoarding(boarding)"
                >
                  取消
                </button>
                <button class="detail-btn" @click="viewBoardingDetail(boarding)">
                  详情
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 寄养详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>寄养详情</h3>
          <button class="close-btn" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body" v-if="selectedBoarding">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-row">
              <span class="label">寄养ID：</span>
              <span class="value">{{ selectedBoarding.id }}</span>
            </div>
            <div class="detail-row">
              <span class="label">用户：</span>
              <span class="value">{{ selectedBoarding.userName }} ({{ selectedBoarding.userPhone }})</span>
            </div>
            <div class="detail-row">
              <span class="label">宠物：</span>
              <span class="value">{{ selectedBoarding.petName }} ({{ selectedBoarding.petType }})</span>
            </div>
            <div class="detail-row">
              <span class="label">寄养类型：</span>
              <span class="value">{{ selectedBoarding.boardingTypeText }}</span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>时间信息</h4>
            <div class="detail-row">
              <span class="label">寄养开始：</span>
              <span class="value">{{ formatDateTime(selectedBoarding.startTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">寄养结束：</span>
              <span class="value">{{ formatDateTime(selectedBoarding.endTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">申请时间：</span>
              <span class="value">{{ formatDateTime(selectedBoarding.createTime) }}</span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>联系信息</h4>
            <div class="detail-row">
              <span class="label">紧急联系人：</span>
              <span class="value">{{ selectedBoarding.emergencyContact }}</span>
            </div>
            <div class="detail-row">
              <span class="label">紧急电话：</span>
              <span class="value">{{ selectedBoarding.emergencyPhone }}</span>
            </div>
          </div>
          
          <div class="detail-section" v-if="selectedBoarding.specialRequirements">
            <h4>特殊要求</h4>
            <div class="detail-text">{{ selectedBoarding.specialRequirements }}</div>
          </div>
          
          <div class="detail-section" v-if="selectedBoarding.petHealthStatus">
            <h4>健康状态</h4>
            <div class="detail-text">{{ selectedBoarding.petHealthStatus }}</div>
          </div>
          
          <div class="detail-section" v-if="selectedBoarding.vaccinationRecords">
            <h4>疫苗记录</h4>
            <div class="detail-text">{{ selectedBoarding.vaccinationRecords }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      boardings: [],
      stats: {
        pending: 0,
        confirmed: 0,
        inProgress: 0,
        completed: 0,
        totalRevenue: 0
      },
      filters: {
        status: '',
        boardingType: '',
        search: ''
      },
      showDetailModal: false,
      selectedBoarding: null,
      searchTimeout: null
    }
  },
  
  mounted() {
    this.loadBoardings();
  },
  
  methods: {
    // 加载寄养服务列表
    async loadBoardings() {
      try {
        console.log('开始加载寄养服务数据...');
        
        // 使用fetch直接调用API
        const response = await fetch('http://10.76.242.18:8080/api/pet-boarding/list', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('寄养服务API响应:', data);
        
        if (data.code === 200 || data.code === 0) {
          let boardings = data.data || [];
          
          // 应用筛选
          if (this.filters.status) {
            boardings = boardings.filter(boarding => boarding.status === this.filters.status);
          }
          
          if (this.filters.boardingType) {
            boardings = boardings.filter(boarding => boarding.boardingType === this.filters.boardingType);
          }
          
          if (this.filters.search) {
            const searchLower = this.filters.search.toLowerCase();
            boardings = boardings.filter(boarding => 
              boarding.userName?.toLowerCase().includes(searchLower) ||
              boarding.petName?.toLowerCase().includes(searchLower) ||
              boarding.userPhone?.includes(searchLower)
            );
          }
          
          this.boardings = boardings;
          this.updateStats(data.data || []);
        } else {
          console.error('获取寄养服务列表失败:', data.msg);
          this.boardings = [];
        }
      } catch (error) {
        console.error('加载寄养服务列表失败:', error);
        this.boardings = [];
      }
    },
    
    // 更新统计数据
    updateStats(allBoardings) {
      this.stats.pending = allBoardings.filter(b => b.status === 'pending').length;
      this.stats.confirmed = allBoardings.filter(b => b.status === 'confirmed').length;
      this.stats.inProgress = allBoardings.filter(b => b.status === 'in_progress').length;
      this.stats.completed = allBoardings.filter(b => b.status === 'completed').length;
      this.stats.totalRevenue = allBoardings
        .filter(b => b.status === 'completed')
        .reduce((sum, b) => sum + parseFloat(b.price), 0);
    },
    
    // 防抖搜索
    debounceSearch() {
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => {
        this.loadBoardings();
      }, 500);
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      const date = new Date(dateTimeStr);
      return date.toLocaleString('zh-CN');
    },
    
    // 确认寄养
    async confirmBoarding(boarding) {
      if (confirm('确认接受这个寄养预约吗？')) {
        try {
          console.log('正在确认寄养:', boarding.id);
          
          const response = await fetch(`http://10.76.242.18:8080/api/pet-boarding/${boarding.id}/status?status=confirmed`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          
          const data = await response.json();
          if (data.code === 200 || data.code === 0) {
            alert('确认成功');
            this.loadBoardings();
          } else {
            alert('确认失败: ' + data.msg);
          }
        } catch (error) {
          console.error('确认寄养失败:', error);
          alert('确认失败，请重试');
        }
      }
    },
    
    // 开始寄养
    async startBoarding(boarding) {
      if (confirm('开始寄养服务吗？')) {
        try {
          console.log('正在开始寄养:', boarding.id);
          
          const response = await fetch(`http://10.76.242.18:8080/api/pet-boarding/${boarding.id}/status?status=in_progress`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          
          const data = await response.json();
          if (data.code === 200 || data.code === 0) {
            alert('开始寄养成功');
            this.loadBoardings();
          } else {
            alert('开始寄养失败: ' + data.msg);
          }
        } catch (error) {
          console.error('开始寄养失败:', error);
          alert('开始寄养失败，请重试');
        }
      }
    },
    
    // 完成寄养
    async completeBoarding(boarding) {
      const rating = prompt('请输入评分 (1-5):');
      if (rating && rating >= 1 && rating <= 5) {
        const review = prompt('请输入评价内容 (可选):');
        try {
          console.log('正在完成寄养:', boarding.id, 'rating:', rating);
          
          const response = await fetch(`http://10.76.242.18:8080/api/pet-boarding/${boarding.id}/complete?rating=${rating}&review=${encodeURIComponent(review || '')}`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          
          const data = await response.json();
          if (data.code === 200 || data.code === 0) {
            alert('完成寄养成功');
            this.loadBoardings();
          } else {
            alert('完成寄养失败: ' + data.msg);
          }
        } catch (error) {
          console.error('完成寄养失败:', error);
          alert('完成寄养失败，请重试');
        }
      }
    },
    
    // 取消寄养
    async cancelBoarding(boarding) {
      if (confirm('确定要取消这个寄养预约吗？')) {
        try {
          console.log('正在取消寄养:', boarding.id);
          
          const response = await fetch(`http://10.76.242.18:8080/api/pet-boarding/${boarding.id}/cancel`, {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json'
            }
          });
          
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          
          const data = await response.json();
          if (data.code === 200 || data.code === 0) {
            alert('取消成功');
            this.loadBoardings();
          } else {
            alert('取消失败: ' + data.msg);
          }
        } catch (error) {
          console.error('取消寄养失败:', error);
          alert('取消失败，请重试');
        }
      }
    },
    
    // 查看寄养详情
    viewBoardingDetail(boarding) {
      this.selectedBoarding = boarding;
      this.showDetailModal = true;
    },
    
    // 关闭详情弹窗
    closeDetailModal() {
      this.showDetailModal = false;
      this.selectedBoarding = null;
    }
  }
}
</script>

<style scoped>
.pet-boarding-management {
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
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.pending { background: #fff3cd; }
.stat-icon.confirmed { background: #d1ecf1; }
.stat-icon.in-progress { background: #d4edda; }
.stat-icon.completed { background: #e2e3e5; }
.stat-icon.total { background: #fff5f2; }

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 筛选区域 */
.filters-section {
  background: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
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
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

/* 表格样式 */
.boardings-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.boardings-table table {
  width: 100%;
  border-collapse: collapse;
}

.boardings-table th,
.boardings-table td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.boardings-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.boardings-table td {
  font-size: 14px;
  color: #333;
}

.user-info, .pet-info, .time-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name, .pet-name {
  font-weight: 500;
}

.user-phone, .pet-type, .start-time, .end-time {
  font-size: 12px;
  color: #666;
}

.boarding-type-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.boarding-type-badge.daily { background: #e3f2fd; color: #1976d2; }
.boarding-type-badge.overnight { background: #f3e5f5; color: #7b1fa2; }
.boarding-type-badge.weekly { background: #e8f5e8; color: #388e3c; }
.boarding-type-badge.monthly { background: #fff3e0; color: #f57c00; }

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending { background: #fff3cd; color: #856404; }
.status-badge.confirmed { background: #d1ecf1; color: #0c5460; }
.status-badge.in_progress { background: #d4edda; color: #155724; }
.status-badge.completed { background: #e2e3e5; color: #383d41; }
.status-badge.cancelled { background: #f8d7da; color: #721c24; }

.price-cell {
  font-weight: 600;
  color: #ff6b35;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-buttons button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  font-weight: 500;
}

.confirm-btn { background: #d4edda; color: #155724; }
.start-btn { background: #d1ecf1; color: #0c5460; }
.complete-btn { background: #e2e3e5; color: #383d41; }
.cancel-btn { background: #f8d7da; color: #721c24; }
.detail-btn { background: #f8f9fa; color: #666; }

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
  border-radius: 12px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  color: #666;
}

.modal-body {
  padding: 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #333;
  font-size: 16px;
}

.detail-row {
  display: flex;
  margin-bottom: 8px;
}

.detail-row .label {
  width: 120px;
  color: #666;
  font-size: 14px;
}

.detail-row .value {
  color: #333;
  font-size: 14px;
}

.detail-text {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  color: #333;
  font-size: 14px;
  line-height: 1.5;
}
</style>
