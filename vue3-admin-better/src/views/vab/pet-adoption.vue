<template>
  <div class="pet-adoption-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>宠物领养管理</h1>
      <p>管理所有宠物领养申请和状态</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon pending">📋</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon approved">✅</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.approved }}</div>
          <div class="stat-label">已通过</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon rejected">❌</div>
        <div class="stat-content">
          <div class="stat-number">{{ stats.rejected }}</div>
          <div class="stat-label">已拒绝</div>
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
        <select v-model="filters.status" @change="loadAdoptions">
          <option value="">全部状态</option>
          <option value="pending">待审核</option>
          <option value="approved">已通过</option>
          <option value="rejected">已拒绝</option>
          <option value="completed">已完成</option>
          <option value="cancelled">已取消</option>
        </select>
      </div>
      
      <div class="filter-group">
        <label>搜索：</label>
        <input 
          type="text" 
          v-model="filters.search" 
          placeholder="搜索申请人、宠物或电话"
          @input="debounceSearch"
        />
      </div>
      
      <button class="refresh-btn" @click="loadAdoptions">刷新</button>
    </div>

    <!-- 领养申请列表 -->
    <div class="adoptions-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>申请人信息</th>
            <th>宠物信息</th>
            <th>申请时间</th>
            <th>领养费用</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="adoption in adoptions" :key="adoption.id">
            <td>{{ adoption.id }}</td>
            <td>
              <div class="applicant-info">
                <div class="applicant-name">{{ adoption.applicantName || '未知用户' }}</div>
                <div class="applicant-phone">{{ adoption.applicantPhone }}</div>
              </div>
            </td>
            <td>
              <div class="pet-info">
                <div class="pet-name">{{ adoption.petName || '未知宠物' }}</div>
                <div class="pet-type">{{ adoption.petType }} - {{ adoption.petBreed }}</div>
              </div>
            </td>
            <td>{{ formatDateTime(adoption.createTime) }}</td>
            <td class="price-cell">¥{{ adoption.adoptionFee }}</td>
            <td>
              <span class="status-badge" :class="adoption.status">
                {{ adoption.statusText }}
              </span>
            </td>
            <td>
              <div class="action-buttons">
                <button 
                  v-if="adoption.status === 'pending'" 
                  class="approve-btn" 
                  @click="reviewAdoption(adoption, 'approved')"
                >
                  通过
                </button>
                <button 
                  v-if="adoption.status === 'pending'" 
                  class="reject-btn" 
                  @click="reviewAdoption(adoption, 'rejected')"
                >
                  拒绝
                </button>
                <button 
                  v-if="adoption.status === 'approved'" 
                  class="complete-btn" 
                  @click="completeAdoption(adoption)"
                >
                  完成领养
                </button>
                <button class="detail-btn" @click="viewAdoptionDetail(adoption)">
                  详情
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 领养详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>领养申请详情</h3>
          <button class="close-btn" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body" v-if="selectedAdoption">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="detail-row">
              <span class="label">申请ID：</span>
              <span class="value">{{ selectedAdoption.id }}</span>
            </div>
            <div class="detail-row">
              <span class="label">申请人：</span>
              <span class="value">{{ selectedAdoption.applicantName }} ({{ selectedAdoption.applicantPhone }})</span>
            </div>
            <div class="detail-row">
              <span class="label">宠物：</span>
              <span class="value">{{ selectedAdoption.petName }} ({{ selectedAdoption.petType }})</span>
            </div>
            <div class="detail-row">
              <span class="label">领养费用：</span>
              <span class="value">¥{{ selectedAdoption.adoptionFee }}</span>
            </div>
            <div class="detail-row">
              <span class="label">申请状态：</span>
              <span class="value">{{ selectedAdoption.statusText }}</span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>申请信息</h4>
            <div class="detail-row">
              <span class="label">申请理由：</span>
              <span class="value">{{ selectedAdoption.applicationReason }}</span>
            </div>
            <div class="detail-row">
              <span class="label">家庭环境：</span>
              <span class="value">{{ selectedAdoption.homeEnvironment }}</span>
            </div>
            <div class="detail-row">
              <span class="label">养宠经验：</span>
              <span class="value">{{ selectedAdoption.petExperience }}</span>
            </div>
            <div class="detail-row">
              <span class="label">经济状况：</span>
              <span class="value">{{ selectedAdoption.financialStatus }}</span>
            </div>
            <div class="detail-row">
              <span class="label">家庭成员：</span>
              <span class="value">{{ selectedAdoption.familyMembers }}</span>
            </div>
            <div class="detail-row">
              <span class="label">住房类型：</span>
              <span class="value">{{ selectedAdoption.housingTypeText }}</span>
            </div>
            <div class="detail-row">
              <span class="label">其他宠物：</span>
              <span class="value">{{ selectedAdoption.hasOtherPets ? '是' : '否' }}</span>
            </div>
            <div class="detail-row" v-if="selectedAdoption.otherPetsInfo">
              <span class="label">其他宠物信息：</span>
              <span class="value">{{ selectedAdoption.otherPetsInfo }}</span>
            </div>
            <div class="detail-row">
              <span class="label">预计照顾时间：</span>
              <span class="value">{{ selectedAdoption.expectedCareTime }}小时</span>
            </div>
            <div class="detail-row">
              <span class="label">联系方式：</span>
              <span class="value">{{ selectedAdoption.contactMethod }}</span>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>联系信息</h4>
            <div class="detail-row">
              <span class="label">紧急联系人：</span>
              <span class="value">{{ selectedAdoption.emergencyContact }}</span>
            </div>
            <div class="detail-row">
              <span class="label">紧急电话：</span>
              <span class="value">{{ selectedAdoption.emergencyPhone }}</span>
            </div>
          </div>
          
          <div class="detail-section" v-if="selectedAdoption.reviewComment">
            <h4>审核信息</h4>
            <div class="detail-row">
              <span class="label">审核人：</span>
              <span class="value">{{ selectedAdoption.reviewerName || '管理员' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">审核时间：</span>
              <span class="value">{{ formatDateTime(selectedAdoption.reviewTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">审核意见：</span>
              <span class="value">{{ selectedAdoption.reviewComment }}</span>
            </div>
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
      adoptions: [],
      stats: {
        pending: 0,
        approved: 0,
        rejected: 0,
        completed: 0,
        totalRevenue: 0
      },
      filters: {
        status: '',
        search: ''
      },
      showDetailModal: false,
      selectedAdoption: null,
      searchTimeout: null
    }
  },
  
  mounted() {
    this.loadAdoptions();
  },
  
  methods: {
    // 加载领养申请列表
    async loadAdoptions() {
      try {
        // 从真实API获取数据
        const response = await this.$http.get('http://172.17.209.218:8080/api/pet-adoption/list');
        if (response.data.code === 200) {
          let adoptions = response.data.data || [];
          
          // 应用筛选
          if (this.filters.status) {
            adoptions = adoptions.filter(adoption => adoption.status === this.filters.status);
          }
          
          if (this.filters.search) {
            const searchLower = this.filters.search.toLowerCase();
            adoptions = adoptions.filter(adoption => 
              adoption.applicantName?.toLowerCase().includes(searchLower) ||
              adoption.petName?.toLowerCase().includes(searchLower) ||
              adoption.applicantPhone?.includes(searchLower)
            );
          }
          
          this.adoptions = adoptions;
          this.updateStats(response.data.data || []);
        } else {
          console.error('获取领养申请列表失败:', response.data.msg);
          this.adoptions = [];
        }
      } catch (error) {
        console.error('加载领养申请列表失败:', error);
        this.adoptions = [];
      }
    },
    
    // 更新统计数据
    updateStats(allAdoptions) {
      this.stats.pending = allAdoptions.filter(a => a.status === 'pending').length;
      this.stats.approved = allAdoptions.filter(a => a.status === 'approved').length;
      this.stats.rejected = allAdoptions.filter(a => a.status === 'rejected').length;
      this.stats.completed = allAdoptions.filter(a => a.status === 'completed').length;
      this.stats.totalRevenue = allAdoptions
        .filter(a => a.status === 'completed')
        .reduce((sum, a) => sum + parseFloat(a.adoptionFee), 0);
    },
    
    // 防抖搜索
    debounceSearch() {
      clearTimeout(this.searchTimeout);
      this.searchTimeout = setTimeout(() => {
        this.loadAdoptions();
      }, 500);
    },
    
    // 格式化日期时间
    formatDateTime(dateTimeStr) {
      if (!dateTimeStr) return '';
      const date = new Date(dateTimeStr);
      return date.toLocaleString('zh-CN');
    },
    
    // 审核领养申请
    async reviewAdoption(adoption, status) {
      const action = status === 'approved' ? '通过' : '拒绝';
      if (confirm(`确定要${action}这个领养申请吗？`)) {
        const reviewComment = prompt(`请输入审核意见 (可选):`);
        try {
          const response = await this.$http.put(`http://172.17.209.218:8080/api/pet-adoption/${adoption.id}/review?status=${status}&reviewerId=1&reviewComment=${encodeURIComponent(reviewComment || '')}`);
          if (response.data.code === 200) {
            this.loadAdoptions();
          } else {
            alert('审核失败: ' + response.data.msg);
          }
        } catch (error) {
          console.error('审核领养申请失败:', error);
          alert('审核失败，请重试');
        }
      }
    },
    
    // 完成领养
    async completeAdoption(adoption) {
      if (confirm('确认完成这个领养申请吗？')) {
        try {
          const response = await this.$http.put(`http://172.17.209.218:8080/api/pet-adoption/${adoption.id}/complete`);
          if (response.data.code === 200) {
            this.loadAdoptions();
          } else {
            alert('完成领养失败: ' + response.data.msg);
          }
        } catch (error) {
          console.error('完成领养失败:', error);
          alert('完成领养失败，请重试');
        }
      }
    },
    
    // 查看领养详情
    viewAdoptionDetail(adoption) {
      this.selectedAdoption = adoption;
      this.showDetailModal = true;
    },
    
    // 关闭详情弹窗
    closeDetailModal() {
      this.showDetailModal = false;
      this.selectedAdoption = null;
    }
  }
}
</script>

<style scoped>
.pet-adoption-management {
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
.stat-icon.approved { background: #d1ecf1; }
.stat-icon.rejected { background: #f8d7da; }
.stat-icon.completed { background: #d4edda; }
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
.adoptions-table {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.adoptions-table table {
  width: 100%;
  border-collapse: collapse;
}

.adoptions-table th,
.adoptions-table td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.adoptions-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.adoptions-table td {
  font-size: 14px;
  color: #333;
}

.applicant-info, .pet-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.applicant-name, .pet-name {
  font-weight: 500;
}

.applicant-phone, .pet-type {
  font-size: 12px;
  color: #666;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending { background: #fff3cd; color: #856404; }
.status-badge.approved { background: #d1ecf1; color: #0c5460; }
.status-badge.rejected { background: #f8d7da; color: #721c24; }
.status-badge.completed { background: #d4edda; color: #155724; }
.status-badge.cancelled { background: #e2e3e5; color: #383d41; }

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

.approve-btn { background: #d4edda; color: #155724; }
.reject-btn { background: #f8d7da; color: #721c24; }
.complete-btn { background: #e2e3e5; color: #383d41; }
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
  max-width: 800px;
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
  flex: 1;
}
</style>
