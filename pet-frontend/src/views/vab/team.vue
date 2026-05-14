<template>
  <div class="team-container">
    <el-card shadow="never">
      <div class="tabs-header-wrap">
        <div class="tabs-right-actions">
          <div class="header-actions">
            <el-select
              v-if="!isStoreScopedUser"
              v-model="selectedStoreId"
              placeholder="选择门店"
              clearable
              style="width: 200px"
              @change="handleStoreChange"
            >
              <el-option label="全部门店（平台）" :value="null" />
              <el-option
                v-for="s in storeList"
                :key="'store-' + s.id"
                :label="s.storeName || s.name || ('门店' + s.id)"
                :value="s.id"
              />
            </el-select>
            <span v-else-if="isStoreScopedUser && currentStoreName" class="store-scoped-label">当前门店：{{ currentStoreName }}</span>
            <el-select
              v-model="selectedServiceType"
              placeholder="选择服务类型"
              clearable
              style="width: 180px"
              @change="handleServiceTypeChange"
            >
              <el-option 
                v-for="serviceType in availableServiceTypes" 
                :key="serviceType.value"
                :label="serviceType.label" 
                :value="serviceType.value" 
              />
            </el-select>
            <el-input
              v-model="searchText"
              placeholder="搜索团队成员..."
              clearable
              style="width: 180px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="team-tabs">
        <!-- 超级管理员看全部；门店管理员只看本门店成员 -->
        <el-tab-pane label="所有服务人员" name="all">
          <div>
          <!-- 空状态提示 -->
          <el-empty v-if="!loadingMembers && filteredAllMembers.length === 0" description="暂无成员数据" />
          <el-table
            v-else
            :data="filteredAllMembers"
            v-loading="loadingMembers"
            stripe
            border
            table-layout="fixed"
            :fit="false"
            style="width: 100%"
          >
            <el-table-column label="头像" width="72" align="center">
              <template #default="{ row }">
                <el-avatar
                  :size="36"
                  :src="getAvatarUrl(row.avatar)"
                  :key="'m-' + row.id + '-' + (row.avatar || '')"
                />
              </template>
            </el-table-column>
            <el-table-column label="姓名" width="140" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="cell-ellipsis-text">{{ row.memberName || row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="所属门店" width="190" show-overflow-tooltip>
              <template #default="{ row }">
                <span class="cell-ellipsis-text">{{ getStoreNameForMember(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="服务类型" width="140" show-overflow-tooltip>
              <template #default="{ row }">
                <el-tag
                  v-if="isStoreAdminMember(row)"
                  type="primary"
                  effect="light"
                >
                  分店管理员
                </el-tag>
                <span v-else class="cell-ellipsis-text">{{ getMemberServiceTypeName(row) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="340" align="center">
              <template #default="{ row }">
                <div class="table-action-row">
                  <el-button type="link" @click="editMember(row)">编辑</el-button>
                  <el-button type="link" style="color: #f56c6c" @click="deleteMember(row)">删除</el-button>
                  <el-button v-if="canViewReviews(row)" type="link" @click="openMemberReviewsDialog(row)">查看评价</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          </div>
        </el-tab-pane>
        
        <el-tab-pane v-if="false" label="部门门店管理" name="departments">
          <!-- 一级：展示所有门店，点击进入该门店的部门 -->
          <div v-if="viewingStoreIdForDept === null" class="store-list-view">
            <el-empty v-if="!storeList.length" description="暂无门店数据" />
            <el-row v-else :gutter="20">
              <el-col
                v-for="store in storeList"
                :key="'store-' + store.id"
                :span="8"
                style="margin-bottom: 20px"
              >
                <el-card class="department-card store-card" shadow="hover" @click="enterStoreDepartments(store)">
                  <div class="department-header">
                    <h3>{{ store.storeName || store.name || ('门店' + store.id) }}</h3>
                    <el-tag type="info">点击查看部门</el-tag>
                  </div>
                  <div class="department-description">点击进入该门店，管理其下部门与成员</div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <!-- 二级：当前门店下的部门列表 -->
          <template v-else>
            <div class="department-header-actions" style="margin-bottom: 20px;">
              <el-button type="default" @click="backToStoreList" style="margin-right: 12px;">← 返回门店列表</el-button>
              <span class="current-store-label">当前门店：<strong>{{ viewingStoreName || '未命名门店' }}</strong></span>
              <el-button type="primary" @click="showAddDepartmentDialog" style="margin-left: 16px;">添加部门</el-button>
              <el-button type="success" @click="refreshDepartments">刷新部门列表</el-button>
            </div>
            <el-row :gutter="20">
              <el-col 
                v-for="department in departments" 
                :key="department.id" 
                :span="8"
                style="margin-bottom: 20px"
              >
                <el-card class="department-card" shadow="hover">
                  <div class="department-header">
                    <h3>{{ department.name }}</h3>
                    <el-tag>{{ department.memberCount }} 人</el-tag>
                  </div>
                  <div class="department-description">
                    {{ department.description }}
                  </div>
                  <div class="department-actions">
                    <el-button type="link" @click="viewDepartment(department)">查看详情</el-button>
                    <el-button type="link" @click="editDepartment(department)">编辑</el-button>
                    <el-button type="link" style="color: #f56c6c;" @click="deleteDepartment(department)">删除</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </template>
        </el-tab-pane>
        <el-tab-pane label="排班管理" name="schedule-tab">
          <div class="embedded-schedule-panel">
            <schedule-management
              key="staff-embed-schedule"
              :force-staff-management-mode="true"
              :is-my-schedule="false"
              embedded-staff-tab="schedule"
            />
          </div>
        </el-tab-pane>
        <el-tab-pane v-if="showStaffTimeSlotsTab" label="时间段管理" name="time-slots-tab">
          <div class="embedded-schedule-panel">
            <schedule-management
              key="staff-embed-time-slots"
              :force-staff-management-mode="true"
              :is-my-schedule="false"
              embedded-staff-tab="timeSlots"
            />
          </div>
        </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
    
    <!-- 添加/编辑成员对话框 -->
    <el-dialog 
      v-model="memberDialogVisible" 
      :title="editingMember ? '编辑成员' : '成员'"
      width="600px"
    >
        <el-form
        ref="memberFormRef"
        :model="memberForm"
        :rules="memberRules"
          label-width="92px"
      >
        <el-row :gutter="20">
          <el-col :span="11">
            <el-form-item label="姓名" prop="memberName">
              <el-input v-model="memberForm.memberName" />
            </el-form-item>
            <el-form-item v-if="editingMember" label="所属门店">
              <span class="form-store-name">{{ editingMemberStoreName || '—' }}</span>
            </el-form-item>
            <el-form-item label="服务类型" prop="serviceType">
              <el-select
                v-if="!isEditingStoreAdmin"
                :key="`service-type-select-${editingMemberDepartmentOptions.length}-${memberForm.serviceType}`"
                v-model="memberForm.serviceType" 
                placeholder="请选择服务类型" 
                style="width: 100%"
                :loading="editingMember && editingMemberDepartmentOptionsLoading"
                filterable
                clearable
              >
                <el-option 
                  v-for="opt in editFormServiceTypeOptions" 
                  :key="opt.value"
                  :label="opt.label" 
                  :value="opt.value"
                />
              </el-select>
              <el-input v-else model-value="分店管理员" disabled />
              <div v-if="!isEditingStoreAdmin && editingMember && !editingMemberDepartmentOptionsLoading && editFormServiceTypeOptions.length === 0" 
                   style="font-size: 12px; color: #f56c6c; margin-top: 5px;">
                该门店下暂无部门，请先在「门店」tab 中进入该门店后添加部门
              </div>
              <div v-else-if="!isEditingStoreAdmin && memberForm.serviceType && !editFormServiceTypeOptions.find(s => s.value === memberForm.serviceType)" 
                   style="font-size: 12px; color: #999; margin-top: 5px;">
                提示：当前选择的服务类型对应的部门可能已被删除，请重新选择
              </div>
            </el-form-item>
            
            <el-form-item label="每日最大任务数" prop="maxTasksPerDay">
              <el-input-number v-model="memberForm.maxTasksPerDay" :min="1" :max="20" style="width: 100%" />
            </el-form-item>
          </el-col>
          
          <el-col :span="13">
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="customUpload"
              >
                <img 
                  v-if="memberForm.avatar" 
                  :src="getAvatarUrl(memberForm.avatar)" 
                  class="avatar" 
                  alt="Avatar"
                />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="memberForm.phone" />
            </el-form-item>
            
            <el-form-item label="用户名" prop="username" v-if="!editingMember">
              <el-input 
                v-model="memberForm.username" 
                placeholder="留空则自动生成（使用手机号）"
              />
              <div style="font-size: 12px; color: #999; margin-top: 5px;">
                提示：留空将自动使用手机号作为用户名
              </div>
            </el-form-item>
            
            <el-form-item label="初始密码" prop="password" v-if="!editingMember">
              <el-input 
                v-model="memberForm.password" 
                type="password"
                placeholder="留空则自动生成（使用手机号后6位）"
                show-password
              />
              <div style="font-size: 12px; color: #999; margin-top: 5px;">
                提示：留空将自动使用手机号后6位作为初始密码
              </div>
            </el-form-item>
            
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="memberDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveMember"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 成员排班对话框 -->
    <el-dialog v-model="memberScheduleDialogVisible" :title="`成员排班 - ${currentMemberName}`" width="640px">
      <div class="member-dialog-toolbar">
        <el-date-picker
          v-model="memberScheduleDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          @change="handleMemberScheduleDateChange"
        />
      </div>
      <div v-loading="memberScheduleLoading" class="schedule-slots">
        <el-empty v-if="!memberScheduleLoading && memberScheduleSlots.length === 0" description="该日期暂无排班" />
        <div v-else class="slot-list">
          <div
            v-for="slot in memberScheduleSlots"
            :key="slot.id"
            class="slot-item"
          >
            <span class="slot-time">{{ slot.timeSlot }}</span>
            <span v-if="isSlotBooked(slot)" class="slot-status">已预约</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 成员评价对话框 -->
    <el-dialog v-model="memberReviewsDialogVisible" :title="`成员评价 - ${currentMemberName}`" width="760px">
      <div class="member-review-summary">
        <span>综合评分：</span>
        <el-rate :model-value="memberAverageRating" disabled allow-half show-score text-color="#ff9900" score-template="{value}分" />
        <span class="review-count">共 {{ memberReviews.length }} 条评价</span>
      </div>
      <el-table :data="memberReviews" style="width: 100%" v-loading="memberReviewsLoading">
        <el-table-column prop="userName" label="评价用户" width="120" />
        <el-table-column prop="rating" label="评分" width="160">
          <template #default="{ row }">
            <div class="rate-cell-nowrap">
              <el-rate :model-value="Number(row.rating) || 0" :max="5" disabled show-score text-color="#ff9900" score-template="{value}分" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="comment" label="评价内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="appointmentTypeName" label="服务类型" width="120" />
        <el-table-column prop="createTime" label="评价时间" width="170">
          <template #default="{ row }">
            {{ formatReviewTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!memberReviewsLoading && memberReviews.length === 0" description="暂无评价" />
    </el-dialog>
    
    <!-- 部门详情/编辑对话框 -->
    <el-dialog 
      v-model="departmentDialogVisible" 
      :title="editingDepartment ? '编辑部门' : '添加部门'"
      width="600px"
    >
      <el-form
        ref="departmentFormRef"
        :model="departmentForm"
        :rules="departmentRules"
        label-width="100px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="departmentForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        
        <el-form-item label="服务类型标识" prop="serviceType">
          <el-input 
            v-if="!editingDepartment"
            v-model="departmentForm.serviceType" 
            placeholder="请输入服务类型标识"
            style="width: 100%"
          >
            <template #prepend>
              <el-select 
                v-model="departmentForm.serviceType" 
                placeholder="快速选择"
                style="width: 140px"
                @change="onPresetServiceTypeChange"
              >
                <el-option 
                  v-for="serviceType in availableServiceTypes" 
                  :key="serviceType.value"
                  :label="serviceType.label" 
                  :value="serviceType.value" 
                />
                <el-option label="自定义..." value="" />
              </el-select>
            </template>
          </el-input>
          <el-input 
            v-else
            v-model="departmentForm.serviceType" 
            disabled
            style="width: 100%"
          >
            <template #prepend>
              <span style="color: #999;">不可修改</span>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="部门描述" prop="description">
          <el-input 
            v-model="departmentForm.description" 
            type="textarea"
            :rows="4"
            placeholder="请输入部门描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="departmentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveDepartment">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 部门详情对话框 -->
    <el-dialog 
      v-model="departmentDetailVisible" 
      title="部门详情"
      width="700px"
    >
      <div v-if="detailDepartment">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="部门名称" :span="2">
            {{ detailDepartment.name }}
          </el-descriptions-item>
          <el-descriptions-item label="服务类型">
            {{ getServiceTypeName(detailDepartment.serviceType) }}
          </el-descriptions-item>
          <el-descriptions-item label="成员数量">
            {{ detailDepartment.memberCount }} 人
          </el-descriptions-item>
          <el-descriptions-item label="部门描述" :span="2">
            {{ detailDepartment.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        <h4>部门成员列表</h4>
        <el-table 
          :data="detailDepartmentMembers" 
          style="width: 100%"
          max-height="300"
        >
          <el-table-column prop="memberName" label="姓名" width="120" />
          <el-table-column prop="phone" label="手机号" width="150" />
          <el-table-column prop="rating" label="评分" width="100" v-if="detailDepartmentMembers.some(m => !isPhysician(m))">
            <template #default="{ row }">
              <span v-if="isPhysician(row)">—</span>
              <el-rate v-else v-model="row.rating" disabled show-score />
            </template>
          </el-table-column>
          <el-table-column prop="totalTasks" label="总任务数" width="100" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getMemberStatusType(row.status)">
                {{ getMemberStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="departmentDetailVisible = false">关闭</el-button>
          <el-button type="primary" @click="editDepartment(detailDepartment)">编辑</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Search, Plus } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import * as adminApi from "@/api/admin";
import * as departmentApi from "@/api/department";
import * as serviceScheduleApi from "@/api/service-schedule";
import ScheduleManagement from "@/views/vab/schedule-management.vue";
import request from "@/utils/request";
import axios from "axios";
import { baseURL } from '@/config';

const apiBase = (baseURL || '').trim().replace(/\/api\/?$/, '') || 'http://localhost:8080';

export default {
  name: "Team",
  components: {
    Search,
    Plus,
    ScheduleManagement
  },
  data() {
    return {
      activeTab: "all",
      searchText: "",
      selectedStoreId: null,
      storeList: [], // 服务门店（/stores/all）
      /** 门店 tab 下当前查看的门店 id，null 表示显示门店列表，有值表示显示该门店下的部门 */
      viewingStoreIdForDept: null,
      memberDialogVisible: false,
      memberScheduleDialogVisible: false,
      memberReviewsDialogVisible: false,
      editingMember: null,
      currentMember: null,
      loadingMembers: false,
      selectedServiceType: "", // 默认服务类型（从部门列表加载后设置）
      members: [
        {
          id: 1,
          name: "张三",
          department: "技术部",
          role: "前端工程师",
          phone: "13800138001",
          status: "online",
          avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
        },
        {
          id: 2,
          name: "李四",
          department: "技术部",
          role: "后端工程师",
          phone: "13800138002",
          status: "busy",
          avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
        },
        {
          id: 3,
          name: "王五",
          department: "设计部",
          role: "UI设计师",
          phone: "13800138003",
          status: "away",
          avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
        },
        {
          id: 4,
          name: "赵六",
          department: "产品部",
          role: "产品经理",
          phone: "13800138004",
          status: "online",
          avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
        },
        {
          id: 5,
          name: "钱七",
          department: "市场部",
          role: "市场专员",
          phone: "13800138005",
          status: "offline",
          avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
        },
        {
          id: 6,
          name: "孙八",
          department: "技术部",
          role: "测试工程师",
          phone: "13800138006",
          status: "online",
          avatar: "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
        }
      ],
      departments: [],
      memberForm: {
        memberName: "",
        userId: null,
        serviceType: "door-cleaning",
        phone: "",
        username: "", // 用户名（可选，留空自动生成）
        password: "", // 初始密码（可选，留空自动生成）
        status: 1,
        maxTasksPerDay: 10,
        rating: 5.0,
        totalTasks: 0,
        avatar: "" // 头像URL
      },
      memberRules: {
        memberName: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          { min: 1, max: 50, message: "姓名长度在 1 到 50 个字符", trigger: "blur" }
        ],
        // 服务类型可选：商家/商品店主等可能没有具体部门
        serviceType: [],
        phone: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
        ]
      },
      memberScheduleSlots: [],
      memberScheduleLoading: false,
      memberScheduleDate: new Date().toISOString().slice(0, 10),
      memberReviews: [],
      memberReviewsLoading: false,
      
      // 部门相关数据
      departmentDialogVisible: false,
      departmentDetailVisible: false,
      editingDepartment: null,
      detailDepartment: null,
      departmentForm: {
        name: "",
        serviceType: "",
        description: ""
      },
      departmentRules: {
        name: [
          { required: true, message: "请输入部门名称", trigger: "blur" }
        ],
        serviceType: [
          { required: true, message: "请输入或选择服务类型标识", trigger: "blur" }
        ]
      },
      
      // 所有成员数据（用于部门详情显示）
      allMembers: [],
      /** 部门详情弹窗中的成员列表（仅当前门店+该部门，与成员数量一致） */
      detailDepartmentMembers: [],
      // 编辑成员时，该成员所属门店下的部门选项（仅该门店，不是总部门列表）
      editingMemberDepartmentOptions: [],
      editingMemberDepartmentOptionsLoading: false
    };
  },
  created() {
    const role = (this.$store.getters['user/role'] || '').toLowerCase();
    const serviceStoreId = this.$store.getters['user/serviceStoreId'];
    // 只有非管理员且绑定了门店时，才默认选中该门店；管理员默认显示全部门店
    if (role !== 'admin' && serviceStoreId) {
      this.selectedStoreId = serviceStoreId;
      this.activeTab = 'all';
    } else {
      this.selectedStoreId = null; // 管理员默认选「全部门店」
      this.activeTab = 'all';
    }
    this.loadStoreList();
    this.loadDepartments();
    this.loadAllServiceMembers();
  },
  watch: {
    memberDialogVisible(val) {
      if (!val) {
        this.editingMemberDepartmentOptions = [];
      }
    },
    // 监听可用服务类型变化，确保 el-select 能正确显示 label
    availableServiceTypes: {
      handler(newVal) {
        // 如果当前有选中的服务类型，确保它在新的选项列表中，否则清空
        if (this.memberForm && this.memberForm.serviceType && newVal.length > 0) {
          const exists = newVal.find(s => s.value === this.memberForm.serviceType);
          if (!exists && this.memberDialogVisible) {
            // 如果当前值不在新列表中，强制更新 el-select
            this.$nextTick(() => {
              const currentValue = this.memberForm.serviceType;
              this.memberForm.serviceType = '';
              this.$nextTick(() => {
                this.memberForm.serviceType = currentValue;
              });
            });
          }
        }
      },
      immediate: false
    }
  },
  computed: {
    // 头像上传URL
    uploadAvatarUrl() {
      return apiBase + '/api/upload/image';
    },
    // 上传请求头
    uploadHeaders() {
      // 如果需要token，可以从store获取
      const token = this.$store?.state?.user?.accessToken || '';
      if (token) {
        return {
          'Authorization': `Bearer ${token}`
        };
      }
      return {};
    },
    filteredMembers() {
      if (!this.searchText) {
        return this.members;
      }
      return this.members.filter(member => 
        member.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
        member.role.toLowerCase().includes(this.searchText.toLowerCase()) ||
        member.department.toLowerCase().includes(this.searchText.toLowerCase())
      );
    },
    // "所有服务人员"标签页的过滤结果
    filteredAllMembers() {
      // 确保 allMembers 是数组
      if (!Array.isArray(this.allMembers)) {
        console.warn('allMembers 不是数组:', this.allMembers);
        return [];
      }
      
      let members = [...this.allMembers]; // 使用展开运算符创建新数组，确保响应式

      const currentRole = (this.$store.getters['user/role'] || '').toLowerCase();
      const currentUserId = this.$store.getters['user/userId'] ?? this.$store.getters['user/id'] ?? null;
      const currentUsername = (this.$store.getters['user/username'] || '').toLowerCase();
      const currentServiceStoreId = this.$store.getters['user/serviceStoreId'] ?? null;

      if (currentRole === 'admin') {
        // 平台管理员：可看所有人（包括分店管理员）
      } else if (currentRole === 'store_admin' || currentRole === 'store-admin') {
        // 分店管理员：仅看本门店服务人员；且不显示自己
        members = members.filter((member) => {
          const memberStoreId = member.serviceStoreId ?? member.service_store_id ?? member.storeId ?? member.store_id ?? null;
          const sameStore = currentServiceStoreId == null || memberStoreId == null || String(memberStoreId) === String(currentServiceStoreId);
          const isStoreAdmin = this.isStoreAdminMember(member);
          const sameUserId = currentUserId != null && member.id != null && String(member.id) === String(currentUserId);
          const sameUsername = currentUsername && (member.username || '').toLowerCase() === currentUsername;
          return sameStore && !isStoreAdmin && !sameUserId && !sameUsername;
        });
      } else {
        // 其他角色默认只看服务人员
        members = members.filter(member => !this.isStoreAdminMember(member));
      }
      
      // 如果选择了服务类型/部门，进行过滤
      if (this.selectedServiceType && this.selectedServiceType.trim()) {
        // 需要同时匹配英文和中文的服务类型
        const serviceTypeMap = {
          'door-cleaning': '上门铲屎',
          'grooming': '宠物洗护',
          'hospital': '宠物医院'
        };
        
        // 创建反向映射（中文到英文）
        const reverseMap = {};
        Object.keys(serviceTypeMap).forEach(key => {
          reverseMap[serviceTypeMap[key]] = key;
        });
        
        // 确定要匹配的值（支持英文标识和中文名称）
        let matchValues = [this.selectedServiceType.trim()];
        
        // 如果选中的是英文，添加对应的中文
        if (serviceTypeMap[this.selectedServiceType.trim()]) {
          matchValues.push(serviceTypeMap[this.selectedServiceType.trim()]);
        }
        // 如果选中的是中文，添加对应的英文
        if (reverseMap[this.selectedServiceType.trim()]) {
          matchValues.push(reverseMap[this.selectedServiceType.trim()]);
        }
        
        // 匹配英文标识或中文名称
        const beforeFilter = members.length;
        members = members.filter(m => {
          const memberType = (m.serviceType || '').trim();
          const match = matchValues.includes(memberType);
          return match;
        });
        console.log('服务类型过滤:', beforeFilter, '->', members.length, 'selectedServiceType:', this.selectedServiceType);
      }
      
      // 如果输入了搜索文本，进行过滤（搜索功能仍然有效）
      if (this.searchText && this.searchText.trim()) {
        const beforeSearch = members.length;
        members = members.filter(member => {
          const name = (member.memberName || member.name || '').toLowerCase();
          const serviceType = (member.serviceType || member.role || '').toLowerCase();
          const phone = (member.phone || '').toLowerCase();
          const search = this.searchText.trim().toLowerCase();
          const match = name.includes(search) || serviceType.includes(search) || phone.includes(search);
          return match;
        });
        console.log('搜索过滤:', beforeSearch, '->', members.length, 'searchText:', this.searchText);
      }
      
      console.log('filteredAllMembers 最终结果:', members.length, 'members');
      return members;
    },
    // 所属门店账号仅能看到「部门」tab；管理员始终可选门店/全部
    isStoreScopedUser() {
      const role = (this.$store.getters['user/role'] || '').toLowerCase();
      if (role === 'admin') return false; // 管理员可切换门店或查看全部
      return !!this.$store.getters['user/serviceStoreId'];
    },
    /** 仅分店管理员需要「时间段管理」；平台管理员给分店管理员排班不进入该页 */
    showStaffTimeSlotsTab() {
      const role = (this.$store.getters['user/role'] || '').toLowerCase();
      return role === 'store_admin' || role === 'store-admin';
    },
    currentStoreName() {
      if (!this.selectedStoreId || !this.storeList.length) return '';
      const s = this.storeList.find(x => x.id === this.selectedStoreId);
      return s ? (s.storeName || s.name || '') : '';
    },
    /** 门店 tab 下当前查看的门店名称 */
    viewingStoreName() {
      if (!this.viewingStoreIdForDept || !this.storeList.length) return '';
      const s = this.storeList.find(x => x.id === this.viewingStoreIdForDept);
      return s ? (s.storeName || s.name || '') : '';
    },
    /** 当前部门操作所属门店 id：门店 tab 下为正在查看的门店，否则为顶部选择框选中的门店 */
    currentDepartmentStoreId() {
      if (this.activeTab === 'departments' && this.viewingStoreIdForDept != null) return this.viewingStoreIdForDept;
      return this.selectedStoreId;
    },
    // 可用的服务类型列表（从部门列表中获取，用于顶部筛选）
    availableServiceTypes() {
      const departmentOptions = this.departments
        .filter(dept => dept.serviceType && this.isVisibleServiceDepartment(dept))
        .map(dept => ({
          value: dept.serviceType,
          label: this.getDepartmentDisplayName(dept)
        }))
        .sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'));
      return [
        { label: '全部', value: '' },
        ...departmentOptions
      ];
    },
    // 编辑成员时，表单中的服务类型下拉选项：仅该成员所属门店下的部门
    editFormServiceTypeOptions() {
      if (this.editingMember && this.editingMemberDepartmentOptions.length > 0) {
        return this.editingMemberDepartmentOptions;
      }
      if (!this.editingMember && this.departments.length > 0) {
        return this.departments
          .filter(dept => dept.serviceType && this.isVisibleServiceDepartment(dept))
          .map(dept => ({ value: dept.serviceType, label: this.getDepartmentDisplayName(dept) }))
          .sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'));
      }
      return this.availableServiceTypes.filter(s => s.value !== '');
    },
    // 编辑中的成员所属门店名称（仅服务门店）
    editingMemberStoreName() {
      if (!this.editingMember) return '';
      const sid = this.editingMember.serviceStoreId ?? this.editingMember.service_store_id;
      if (sid != null) {
        const s = this.storeList.find(x => x.id === sid);
        return s ? (s.storeName || s.name || `门店${sid}`) : `门店${sid}`;
      }
      return '';
    },
    isEditingStoreAdmin() {
      const role = (this.editingMember?.role || '').toLowerCase();
      return role === 'store_admin' || role === 'store-admin';
    },
    currentMemberName() {
      return this.currentMember?.memberName || this.currentMember?.name || '成员';
    },
    memberAverageRating() {
      if (!Array.isArray(this.memberReviews) || this.memberReviews.length === 0) return 0;
      const total = this.memberReviews.reduce((sum, item) => sum + (Number(item.rating) || 0), 0);
      return Number((total / this.memberReviews.length).toFixed(1));
    }
  },
  methods: {
    isVisibleServiceDepartment(dept) {
      const fullName = `${dept?.name || ''} ${dept?.serviceType || ''}`.toLowerCase();
      return !(
        fullName.includes('商城') ||
        fullName.includes('mall') ||
        fullName.includes('shop') ||
        fullName.includes('积分') ||
        fullName.includes('咨询') ||
        fullName.includes('医师')
      );
    },
    getDepartmentDisplayName(dept) {
      const rawLabel = (dept?.name || dept?.serviceType || '').trim();
      if (!rawLabel) return '';
      return rawLabel.replace(/部门/g, '');
    },
    async loadStoreList() {
      try {
        const res = await request.get('/stores/all');
        if (res && (res.code === 0 || res.code === 200) && Array.isArray(res.data)) {
          this.storeList = res.data.map(s => ({
            id: s.id,
            name: s.storeName || s.name,
            storeName: s.storeName || s.name || `门店${s.id}`
          }));
        } else {
          this.storeList = [];
        }
      } catch (e) {
        this.storeList = [];
      }
    },
    handleStoreChange() {
      if (this.activeTab !== 'departments') this.loadDepartments();
      this.loadAllServiceMembers();
    },
    handleTabChange(tab) {
      const tabName = typeof tab === 'string' ? tab : (tab?.props?.name || tab?.paneName || '');
      if (tabName === 'departments') {
        this.viewingStoreIdForDept = null;
        this.departments = [];
        this.loadStoreList();
      } else if (tabName === 'all') {
        this.loadAllServiceMembers();
      }
    },
    /** 点击门店卡片：进入该门店的部门列表 */
    enterStoreDepartments(store) {
      this.viewingStoreIdForDept = store.id;
      this.loadDepartments(store.id);
    },
    /** 从部门列表返回门店列表 */
    backToStoreList() {
      this.viewingStoreIdForDept = null;
      this.departments = [];
    },
    // 处理服务类型/部门选择变化
    handleServiceTypeChange(value) {
      console.log('部门选择变化:', value);
      // 清空选择时显示所有成员
      if (!value) {
        this.selectedServiceType = '';
      } else {
        this.selectedServiceType = value;
      }
      // 对于"所有成员"标签页，只需要触发计算属性更新（会自动过滤）
      if (this.activeTab !== 'all') {
        this.loadServiceMembers();
      }
      // 强制触发视图更新
      this.$nextTick(() => {
        console.log('部门过滤后，filteredAllMembers 数量:', this.filteredAllMembers.length);
      });
    },
    getStoreNameForMember(member) {
      if (!member) return '—';
      const sid = member.serviceStoreId ?? member.service_store_id;
      if (sid != null) {
        const s = this.storeList.find(x => x.id === sid);
        return s ? (s.storeName || s.name || `门店${sid}`) : `门店${sid}`;
      }
      return '—';
    },
    getStatusText(status) {
      const statusMap = {
        "online": "在线",
        "busy": "忙碌",
        "away": "离开",
        "offline": "离线"
      };
      return statusMap[status] || status;
    },
    getStatusType(status) {
      const typeMap = {
        "online": "success",
        "busy": "warning",
        "away": "info",
        "offline": "danger"
      };
      return typeMap[status] || "info";
    },
    getMemberStatusText(status) {
      if (status === 1) return "启用";
      if (status === 0) return "禁用";
      return status === "online" ? "在线" : (status === "offline" ? "离线" : "未知");
    },
    getMemberStatusType(status) {
      if (status === 1) return "success";
      if (status === 0) return "danger";
      return status === "online" ? "success" : "info";
    },
    
    // 加载服务人员列表（使用 admin 表）
    async loadServiceMembers() {
      try {
        this.loadingMembers = true;
        // 将服务类型转换为部门名称（中英文映射）
        const departmentMap = {
          'door-cleaning': '上门铲屎',
          'grooming': '宠物洗护',
          'hospital': '宠物医院',
          'pet-adoption': '宠物领养'
        };
        const department = departmentMap[this.selectedServiceType] || this.selectedServiceType;
        
        const response = await adminApi.getAdminStaffList(department, this.selectedStoreId);
        
        if (response.code === 0 || response.code === 200) {
          const staffList = response.data || [];
          // 转换为前端期望的格式（兼容 service_member 字段名）
          const members = staffList.map(staff => ({
            id: staff.id,
            memberName: staff.name,
            name: staff.name,
            serviceType: staff.department,
            avatar: staff.avatar ?? staff.avatar_url ?? '',
            phone: staff.phone,
            status: staff.status,
            username: staff.username,
            role: staff.role,
            todayWorkload: 0,
            storeId: staff.storeId ?? staff.store_id ?? null,
            serviceStoreId: staff.serviceStoreId ?? staff.service_store_id ?? null
          }))
          .filter(m => {
            const role = (m.role || '').toLowerCase();
            const st = (m.serviceType || '').toString();
            return role !== 'doctor' &&
              st !== 'consultation' &&
              !st.includes('咨询') &&
              !st.includes('医师');
          });
          
          this.members = members;
          
          // 刷新部门列表（更新成员数量）
          if (this.activeTab === 'departments') {
            await this.loadDepartments();
          }
        } else {
          this.$message.error(response.msg || '加载服务人员失败');
          this.members = [];
        }
      } catch (error) {
        console.error('加载服务人员失败:', error);
        this.$message.error('加载服务人员失败');
        this.members = [];
      } finally {
        this.loadingMembers = false;
      }
    },
    getProgressStatus(progress) {
      if (progress === 100) {
        return "success";
      } else if (progress < 30) {
        return "exception";
      }
      return "";
    },
    getTaskStatusText(status) {
      const statusMap = {
        "pending": "待确认",
        "confirmed": "已确认",
        "in-progress": "进行中",
        "completed": "已完成",
        "cancelled": "已取消"
      };
      return statusMap[status] || status;
    },
    getTaskStatusType(status) {
      const typeMap = {
        "pending": "info",
        "confirmed": "warning",
        "in-progress": "warning",
        "completed": "success",
        "cancelled": "danger"
      };
      return typeMap[status] || "info";
    },
    // 新账号由超级管理员在权限管理创建并分配门店，门店管理员在此编辑并分配部门
    async editMember(member) {
      this.editingMember = member;
      this.editingMemberDepartmentOptions = [];
      const storeId = member.serviceStoreId ?? member.service_store_id ?? this.selectedStoreId;
      if (storeId != null) {
        this.editingMemberDepartmentOptionsLoading = true;
        try {
          const response = await departmentApi.getAllDepartments(storeId);
          if ((response.code === 0 || response.code === 200) && Array.isArray(response.data)) {
            this.editingMemberDepartmentOptions = (response.data || [])
              .filter(dept => dept.serviceType && this.isVisibleServiceDepartment(dept))
              .map(dept => ({ value: dept.serviceType, label: this.getDepartmentDisplayName(dept) }))
              .sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'));
          }
        } catch (e) {
          console.warn('加载该门店部门列表失败:', e);
        } finally {
          this.editingMemberDepartmentOptionsLoading = false;
        }
      }
      this.openEditMemberDialog(member);
    },
    resolveMemberScheduleId(member) {
      if (!member) return null;
      return member.serviceMemberId ?? member.serviceMember_id ?? null;
    },
    async openMemberScheduleDialog(member) {
      this.currentMember = { ...member };
      this.memberScheduleDialogVisible = true;
      this.memberScheduleSlots = [];
      await this.loadMemberScheduleByDate(member, this.memberScheduleDate);
    },
    async handleMemberScheduleDateChange() {
      if (!this.currentMember) return;
      await this.loadMemberScheduleByDate(this.currentMember, this.memberScheduleDate);
    },
    async loadMemberScheduleByDate(member, date) {
      const memberIdForSchedule = this.resolveMemberScheduleId(member);
      if (!memberIdForSchedule) {
        this.memberScheduleSlots = [];
        return;
      }
      this.memberScheduleLoading = true;
      try {
        const scheduleRes = await serviceScheduleApi.getMemberSchedule(memberIdForSchedule, date);
        if ((scheduleRes.code === 0 || scheduleRes.code === 200) && Array.isArray(scheduleRes.data)) {
          this.memberScheduleSlots = scheduleRes.data;
        } else {
          this.memberScheduleSlots = [];
        }
      } catch (e) {
        console.warn('加载成员排班失败:', e);
        this.memberScheduleSlots = [];
      } finally {
        this.memberScheduleLoading = false;
      }
    },
    async openMemberReviewsDialog(member) {
      this.currentMember = { ...member };
      this.memberReviewsDialogVisible = true;
      this.memberReviews = [];
      const memberIdForSchedule = this.resolveMemberScheduleId(member);
      if (!memberIdForSchedule) {
        this.memberReviewsLoading = false;
        return;
      }
      this.memberReviewsLoading = true;
      try {
        const ratingsRes = await adminApi.getMemberRatings(memberIdForSchedule);
        if ((ratingsRes.code === 0 || ratingsRes.code === 200) && Array.isArray(ratingsRes.data)) {
          this.memberReviews = ratingsRes.data;
        } else {
          this.memberReviews = [];
        }
      } catch (e) {
        console.warn('加载成员评价失败:', e);
        this.memberReviews = [];
      } finally {
        this.memberReviewsLoading = false;
      }
    },
    // Admin.department（宠物医院/宠物医院部门）与 department.service_type（宠物医疗）不一致，需统一映射
    normalizeDepartmentToServiceType(deptVal) {
      if (!deptVal || typeof deptVal !== 'string') return deptVal;
      const v = deptVal.trim();
      if (['宠物医院', '宠物医院部门', '宠物医疗'].includes(v)) return '宠物医疗';
      if (['宠物洗护', '宠物洗护部门'].includes(v)) return '宠物洗护';
      if (['上门铲屎', '上门铲屎部门'].includes(v)) return '上门铲屎';
      return v;
    },
    openEditMemberDialog(member) {
      const options = this.editingMemberDepartmentOptions.length > 0
        ? this.editingMemberDepartmentOptions
        : this.departments
            .filter(dept => dept.serviceType && this.isVisibleServiceDepartment(dept))
            .map(dept => ({ value: dept.serviceType, label: this.getDepartmentDisplayName(dept) }));
      const rawDept = member.serviceType || member.department;
      const memberServiceType = this.normalizeDepartmentToServiceType(rawDept) || rawDept;
      const matched = options.find(s => s.value === memberServiceType);
      const finalServiceType = matched ? memberServiceType : (options.find(s => s.value === rawDept) ? rawDept : '');
      
      this.memberForm = { 
        memberName: member.memberName || member.name,
        userId: member.userId,
        serviceType: '',
        phone: member.phone,
        status: member.status,
        maxTasksPerDay: member.maxTasksPerDay || 10,
        rating: member.rating || 5.0,
        totalTasks: member.totalTasks || 0,
        avatar: member.avatar || ''
      };
      this.memberDialogVisible = true;
      
      this.$nextTick(() => {
        this.$nextTick(() => {
          if (finalServiceType && (this.editingMemberDepartmentOptions.length ? this.editingMemberDepartmentOptions : options).find(s => s.value === finalServiceType)) {
            this.memberForm.serviceType = finalServiceType;
          }
        });
      });
    },
    // 从数据库加载部门列表（可按指定门店或当前上下文门店加载）
    async loadDepartments(storeId) {
      const sid = storeId ?? this.currentDepartmentStoreId;
      if (sid == null) {
        this.departments = [];
        return;
      }
      try {
        const response = await departmentApi.getAllDepartments(sid);
        if (response.code === 0 || response.code === 200) {
          const departments = response.data || [];
          // 加载该门店成员以统计每个部门的成员数量（不覆盖 allMembers）
          let membersSource = [];
          try {
            const staffRes = await adminApi.getAdminStaffList(undefined, sid);
            if (staffRes && (staffRes.code === 0 || staffRes.code === 200) && Array.isArray(staffRes.data)) {
              membersSource = staffRes.data.map(staff => ({
                serviceType: staff.department,
                serviceStoreId: staff.serviceStoreId ?? staff.service_store_id ?? null
              }));
            }
          } catch (e) {
            console.warn('加载门店成员用于部门统计失败:', e);
          }
          this.departments = departments.map(dept => {
            const count = membersSource.filter(m => {
              const norm = this.normalizeDepartmentToServiceType(m.serviceType) || m.serviceType;
              return norm === dept.serviceType &&
                (m.serviceStoreId == null || m.serviceStoreId === sid);
            }).length;
            return { ...dept, memberCount: count };
          });
        } else {
          this.$message.error(response.msg || '加载部门列表失败');
          this.departments = [];
        }
      } catch (error) {
        console.error('加载部门列表失败:', error);
        this.$message.error('加载部门列表失败');
        this.departments = [];
      }
    },
    
    // 刷新部门列表（重新从数据库加载并更新成员数量）
    async refreshDepartments() {
      await this.loadDepartments();
    },
    
    // 加载所有服务类型的成员（使用 admin 表，可选按门店筛选）
    async loadAllServiceMembers() {
      try {
        const response = await adminApi.getAdminStaffList(undefined, this.selectedStoreId);
        if (response.code === 0 || response.code === 200) {
          const staffList = response.data || [];
          // 转换为前端期望的格式（兼容 service_member 字段名）
          const members = staffList.map(staff => ({
            id: staff.id,
            memberName: staff.name,
            name: staff.name,
            serviceType: staff.department,
            avatar: staff.avatar ?? staff.avatar_url ?? '',
            phone: staff.phone,
            status: staff.status,
            username: staff.username,
            role: staff.role,
            memberSource: 'admin',
            storeId: staff.storeId ?? staff.store_id ?? null,
            serviceStoreId: staff.serviceStoreId ?? staff.service_store_id ?? null,
            rating: staff.rating != null ? Number(staff.rating) : 5,
            totalTasks: staff.totalTasks ?? 0,
            maxTasksPerDay: staff.maxTasksPerDay ?? 10,
            todayWorkload: staff.todayWorkload ?? 0,
            serviceMemberId: staff.serviceMemberId
          }))
          .filter(m => {
            const role = (m.role || '').toLowerCase();
            const st = (m.serviceType || '').toString();
            return role !== 'doctor' &&
              st !== 'consultation' &&
              !st.includes('咨询') &&
              !st.includes('医师');
          });
          // Vue 3 直接赋值即可，响应式系统会自动追踪
          this.allMembers = members;
        } else {
          console.error('加载所有成员失败:', response.msg);
          this.allMembers = [];
        }
      } catch (error) {
        console.error('加载所有服务人员失败:', error);
        this.allMembers = [];
      }
    },
    
    // 获取部门成员列表（仅「所有成员」tab 等场景用 allMembers 时使用；部门详情改用 detailDepartmentMembers）
    getDepartmentMembers(serviceType) {
      return this.allMembers.filter(m => m.serviceType === serviceType);
    },
    
    formatReviewTime(t) {
      if (!t) return '—'
      if (typeof t === 'string') return t.length > 19 ? t.slice(0, 19).replace('T', ' ') : t
      if (t && t.format) return t.format('YYYY-MM-DD HH:mm:ss')
      return String(t)
    },
    // 判断排班时间段是否已被预约
    isSlotBooked(slot) {
      if (!slot) return false;
      return (slot.reservedCount != null && slot.reservedCount > 0) ||
        (slot.status && String(slot.status).includes('已预约')) ||
        (slot.taskId != null && slot.taskId !== 0);
    },
    // 医师相关功能已移除，统一按普通服务人员处理
    isPhysician(member) {
      return false;
    },
    isStoreAdminMember(member) {
      const role = (member?.role || '').toLowerCase();
      return role === 'store_admin' || role === 'store-admin';
    },
    isAdminMember(member) {
      const role = (member?.role || '').toLowerCase();
      return role === 'admin';
    },
    getMemberServiceTypeName(member) {
      if (!member) return '';
      if (this.isStoreAdminMember(member)) return '分店管理员';
      if (this.isAdminMember(member)) return '平台管理员';
      return this.getServiceTypeName(member.serviceType || member.role);
    },
    canViewReviews(member) {
      if (!member) return false;
      if (this.isAdminMember(member) || this.isStoreAdminMember(member)) return false;
      return true;
    },
    // 获取服务类型名称（统一展示为「宠物医院」，不再出现「宠物医疗」）
    getServiceTypeName(serviceType) {
      const map = {
        'store_admin': '分店管理员',
        'store-admin': '分店管理员',
        'admin': '平台管理员',
        'staff': '服务人员',
        'door-cleaning': '上门铲屎',
        'grooming': '宠物洗护',
        'hospital': '宠物医院',
        '宠物医院部门': '宠物医院',
        '宠物医疗': '宠物医院',
        'pet-adoption': '宠物领养'
      };
      return (map[serviceType] || serviceType || '').toString().replace(/部门/g, '');
    },
    // 查看部门详情（只拉取当前门店下该部门的成员，与成员数量一致）
    async viewDepartment(department) {
      this.detailDepartment = { ...department };
      this.detailDepartmentMembers = [];
      this.departmentDetailVisible = true;
      const storeId = this.currentDepartmentStoreId;
      if (storeId == null || !department.serviceType) return;
      try {
        const departmentName = this.getServiceTypeName(department.serviceType);
        const response = await adminApi.getAdminStaffList(departmentName, storeId);
        if (response && (response.code === 0 || response.code === 200) && Array.isArray(response.data)) {
          this.detailDepartmentMembers = response.data
            .filter(staff => {
              const norm = this.normalizeDepartmentToServiceType(staff.department);
              return norm === department.serviceType || (staff.department || '') === departmentName || (staff.department || '') === department.serviceType;
            })
            .map(staff => ({
              id: staff.id,
              memberName: staff.name,
              name: staff.name,
              serviceType: staff.department,
              avatar: staff.avatar ?? staff.avatar_url ?? '',
              phone: staff.phone,
              status: staff.status,
              username: staff.username,
              role: staff.role,
              rating: staff.rating != null ? Number(staff.rating) : 5,
              totalTasks: staff.totalTasks ?? 0,
              storeId: staff.storeId ?? staff.store_id ?? null,
              serviceStoreId: staff.serviceStoreId ?? staff.service_store_id ?? null
            }));
        }
      } catch (e) {
        console.warn('加载部门成员列表失败', e);
      }
    },
    
    // 预设服务类型选择变化时
    onPresetServiceTypeChange(value) {
      // 如果选择了预设值，直接使用；如果选择"自定义..."，清空让用户输入
      if (value) {
        this.departmentForm.serviceType = value;
      } else {
        this.departmentForm.serviceType = "";
      }
    },
    
    // 显示添加部门对话框（当前查看的门店即为部门归属门店）
    showAddDepartmentDialog() {
      if (this.currentDepartmentStoreId == null) {
        this.$message.warning('请先点击上方某个门店进入后，再添加该门店下的部门');
        return;
      }
      if (this.isStoreScopedUser && !this.currentDepartmentStoreId) {
        this.$message.warning('当前账号未绑定门店，无法添加部门');
        return;
      }
      this.editingDepartment = null;
      this.departmentForm = {
        name: "",
        serviceType: "",
        description: ""
      };
      this.departmentDialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.departmentFormRef) {
          this.$refs.departmentFormRef.resetFields();
        }
      });
    },
    
    // 编辑部门
    editDepartment(department) {
      this.editingDepartment = department;
      this.departmentForm = {
        name: department.name,
        serviceType: department.serviceType || "",
        description: department.description || ""
      };
      this.departmentDialogVisible = true;
      this.departmentDetailVisible = false;
      this.$nextTick(() => {
        if (this.$refs.departmentFormRef) {
          this.$refs.departmentFormRef.clearValidate();
        }
      });
    },
    
    // 保存部门（保存到数据库）
    async saveDepartment() {
      this.$refs.departmentFormRef.validate(async (valid) => {
        if (valid) {
          try {
            let response;
            if (this.editingDepartment) {
              // 编辑部门（不修改 serviceType、storeId）
              response = await departmentApi.updateDepartment({
                id: this.editingDepartment.id,
                name: this.departmentForm.name,
                serviceType: this.editingDepartment.serviceType,
                description: this.departmentForm.description,
                status: this.editingDepartment.status || 1,
                storeId: this.editingDepartment.storeId ?? null
              });
            } else {
              // 添加部门 - 同一门店下同一服务类型不可重复
              const sid = this.currentDepartmentStoreId;
              const exists = this.departments.find(d =>
                d.serviceType === this.departmentForm.serviceType &&
                (d.storeId == null ? !sid : d.storeId === sid)
              );
              if (exists) {
                this.$message.warning("当前门店下该服务类型的部门已存在");
                return;
              }
              response = await departmentApi.createDepartment({
                name: this.departmentForm.name,
                serviceType: this.departmentForm.serviceType,
                description: this.departmentForm.description,
                status: 1,
                storeId: sid || null
              });
            }
            
            if (response.code === 0 || response.code === 200) {
              this.$message.success(this.editingDepartment ? "部门信息更新成功" : "部门添加成功");
              this.departmentDialogVisible = false;
              // 重新加载部门列表
              await this.loadDepartments();
            } else {
              this.$message.error(response.msg || '操作失败');
            }
          } catch (error) {
            console.error('保存部门失败:', error);
            this.$message.error('保存失败：' + (error.message || '网络错误'));
          }
        }
      });
    },
    
    // 删除部门（从数据库删除，自动处理成员）
    async deleteDepartment(department) {
      // 提示信息：如果有成员，告知会被自动移出
      let confirmMessage = `确定要删除部门"${department.name}"吗？`;
      if (department.memberCount > 0) {
        confirmMessage = `确定要删除部门"${department.name}"吗？\n\n该部门下有 ${department.memberCount} 个成员，删除后这些成员的部门将被清空。`;
      }
      confirmMessage += '\n\n删除后无法恢复。';
      
      this.$confirm(confirmMessage, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const storeId = this.currentDepartmentStoreId ?? this.viewingStoreIdForDept ?? null;
          const response = await departmentApi.deleteDepartment(department.id, storeId);
          if (response.code === 0 || response.code === 200) {
            if (department.memberCount > 0) {
              this.$message.success(`部门删除成功，${department.memberCount} 个成员已移出该部门`);
            } else {
            this.$message.success('部门删除成功');
            }
            await this.loadDepartments();
            if (this.activeTab !== 'departments') await this.loadAllServiceMembers();
          } else {
            this.$message.error(response.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除部门失败:', error);
          this.$message.error('删除失败：' + (error.response?.data?.msg || error.message || '网络错误'));
        }
      }).catch(() => {
        // 取消删除
      });
    },
    
    // 删除成员
    async deleteMember(member) {
      this.$confirm(
        `确定要删除成员"${member.memberName || member.name}"吗？删除后无法恢复。`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        try {
          const deletedId = member.id;
          const response = await adminApi.deleteAdminStaff(deletedId);
          if (response.code === 0 || response.code === 200) {
            this.$message.success('成员删除成功');
            // 乐观更新：立即从列表移除，再刷新
            this.allMembers = (this.allMembers || []).filter(m => m.id != deletedId);
            this.loadServiceMembers();
            this.loadAllServiceMembers();
            this.refreshDepartments();
          } else {
            this.$message.error(response.msg || '删除失败');
          }
        } catch (error) {
          const errStr = String(error);
          if (errStr.includes('员工不存在') || errStr.includes('成员不存在')) {
            this.$message.info('该成员已不存在，已从列表移除');
            this.allMembers = (this.allMembers || []).filter(m => m.id != member.id);
          } else {
            console.error('删除成员失败:', error);
            this.$message.error('删除失败：' + (error.message || '网络错误'));
          }
        }
      }).catch(() => {
        // 取消删除
      });
    },
    async saveMember() {
      try {
        if (!this.editingMember) {
          this.$message.warning('新账号请由超级管理员在权限管理中创建');
          return;
        }
        await this.$refs.memberFormRef.validate();
        
        // 确保 memberName 字段存在且不为空
        if (!this.memberForm.memberName || this.memberForm.memberName.trim() === '') {
          this.$message.error('请输入服务人员姓名');
          return;
        }
        
        // 准备提交的数据，转换为 admin 表格式
        // 将服务类型转换为部门名称（中英文映射）
        const departmentMap = {
          'door-cleaning': '上门铲屎',
          'grooming': '宠物洗护',
          'hospital': '宠物医院',
          'pet-adoption': '宠物领养'
        };
        let department = departmentMap[this.memberForm.serviceType] || this.memberForm.serviceType;
        if (this.isEditingStoreAdmin) {
          department = this.editingMember?.serviceType || this.editingMember?.department || '分店管理员';
        }
        
        const submitData = {
          name: this.memberForm.memberName, // admin 表的 name 字段
          department: department, // admin 表的 department 字段
          phone: this.memberForm.phone,
          status: this.memberForm.status || 1,
          avatar: this.memberForm.avatar || '',
          role: this.editingMember?.role || 'staff',
          maxTasksPerDay: this.memberForm.maxTasksPerDay != null ? this.memberForm.maxTasksPerDay : 10
        };
        
        // 编辑时必须保留 storeId/serviceStoreId，否则会清空门店关联导致成员"消失"
        const sid = this.editingMember.serviceStoreId ?? this.editingMember.service_store_id;
        if (sid != null) submitData.serviceStoreId = sid;
        
        submitData.id = this.editingMember.id;
        const response = await adminApi.updateAdminStaff(submitData);
        
        if (response.code === 0 || response.code === 200) {
          this.$message.success("成员信息更新成功");
          this.memberDialogVisible = false;
          
          // 无论是添加还是编辑，都重新拉取列表以确保数据同步（Vue3 最佳实践）
          console.log('保存成员成功，重新拉取列表以确保数据同步');
          await this.loadAllServiceMembers();
          
          // 如果当前选择了特定部门，也刷新部门成员列表
          if (this.selectedServiceType) {
            await this.loadServiceMembers();
          }
          
          // 刷新部门列表（更新成员数量）
          await this.loadDepartments();
        } else {
          this.$message.error(response.msg || '操作失败');
        }
      } catch (error) {
        console.error('保存成员失败:', error);
        if (error !== false) { // validate失败时不显示错误
          this.$message.error('保存失败：' + (error.message || '网络错误'));
        }
      }
    },
    handleAvatarSuccess(response, file) {
      console.log('上传成功响应:', response);
      // 处理后端返回的数据格式
      // Element Plus 的 el-upload 组件会自动包装响应
      // response 可能是 { data: { code: 0, data: 'url' } } 格式
      let avatarUrl = '';
      
      if (response && response.data) {
        // 如果 response.data 是字符串，直接使用
        if (typeof response.data === 'string') {
          avatarUrl = response.data;
        } 
        // 如果 response.data 是对象，尝试获取其中的 URL
        else if (response.data.data) {
          avatarUrl = response.data.data;
        } 
        // 如果 response.data 有 code 且为成功状态
        else if (response.data.code === 0 || response.data.code === 200) {
          avatarUrl = response.data.data || response.data.url || '';
        }
      }
      
      // 如果还没有获取到URL，尝试从 response 的其他字段获取
      if (!avatarUrl && response) {
        avatarUrl = response.data || response.url || response.result || '';
      }
      
      if (avatarUrl) {
        this.memberForm.avatar = avatarUrl;
        console.log('保存的头像URL:', avatarUrl);
        this.$message.success('头像上传成功');
      } else {
        console.error('无法获取头像URL，响应:', response);
        this.$message.error('头像上传成功，但无法获取URL');
      }
    },
    
    // 自定义上传方法，直接处理响应
    async customUpload(options) {
      const formData = new FormData();
      formData.append('file', options.file);
      
      try {
        // 使用 axios 直接请求，不使用封装的 request，避免响应拦截器处理
        const response = await axios({
          url: apiBase + '/api/upload/image',
          method: 'post',
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data',
            ...this.uploadHeaders
          }
        });
        
        console.log('上传原始响应:', response);
        console.log('响应数据:', response.data);
        
        // 后端返回的格式是 Result<String>，即 { code: 200, msg: "success", data: "url" }
        const result = response.data;
        
        if (result && (result.code === 0 || result.code === 200)) {
          // result.data 就是头像URL字符串
          const avatarUrl = result.data;
          if (avatarUrl && typeof avatarUrl === 'string') {
            this.memberForm.avatar = avatarUrl;
            console.log('成功保存头像URL:', avatarUrl);
            this.$message.success('头像上传成功');
            // 强制更新视图
            this.$forceUpdate();
          } else {
            console.error('URL格式不正确:', avatarUrl);
            this.$message.error('无法获取头像URL，URL格式不正确');
          }
        } else {
          console.error('上传失败，响应:', result);
          this.$message.error(result?.msg || '头像上传失败');
        }
      } catch (error) {
        console.error('上传失败:', error);
        console.error('错误详情:', error.response?.data || error.message);
        this.$message.error('头像上传失败：' + (error.response?.data?.msg || error.message || '网络错误'));
      }
    },
    
    // 获取头像完整URL（兼容多种存储格式）
    getAvatarUrl(avatar) {
      if (!avatar || typeof avatar !== 'string') return '';
      const raw = avatar.trim();
      if (!raw) return '';
      const base = apiBase;
      let url = raw;
      if (raw.startsWith('http://') || raw.startsWith('https://')) {
        url = raw;
        if (url.includes('localhost:8443') || url.includes('127.0.0.1:8443')) {
          url = url.replace(/https?:\/\/[^/]+/, apiBase);
        } else if (url.startsWith('https://localhost') || url.startsWith('https://127.0.0.1')) {
          url = url.replace(/^https:\/\//, 'http://');
        }
        return url;
      }
      if (raw.startsWith('/upload/') || raw.startsWith('/static/')) {
        return base + raw;
      }
      // upload/... 或 static/... 无前导斜杠
      if (raw.startsWith('upload/') || raw.startsWith('static/')) {
        return `${base}/${raw}`;
      }
      return raw;
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === "image/jpeg" || file.type === "image/png";
      const isLt10M = file.size / 1024 / 1024 < 10;

      if (!isJPG) {
        this.$message.error("头像图片只能是 JPG 或 PNG 格式!");
      }
      if (!isLt10M) {
        this.$message.error("头像图片大小不能超过 10MB!");
      }
      return isJPG && isLt10M;
    }
  }
};
</script>

<style lang="scss" scoped>
.team-container {
  padding: 12px;
  .cell-ellipsis-text {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: middle;
  }
  .cell-ellipsis-link {
    display: inline-block;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding: 0;
  }
  .table-action-row {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    flex-wrap: wrap;
    gap: 6px;
    white-space: normal;
  }
  .member-dialog-toolbar {
    margin-bottom: 12px;
  }
  .member-review-summary {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    color: #606266;
  }
  .review-count {
    margin-left: 8px;
    color: #909399;
    font-size: 13px;
  }
  :deep(.el-table .cell) {
    padding-left: 8px;
    padding-right: 8px;
  }
  :deep(.el-table th.el-table__cell),
  :deep(.el-table td.el-table__cell) {
    padding-top: 6px;
    padding-bottom: 6px;
  }
  .store-scoped-label {
    margin-right: 0;
    color: #606266;
    font-size: 14px;
  }
  .tabs-header-wrap {
    position: relative;
  }
  .team-tabs {
    :deep(.el-tabs__header) {
      padding-right: 600px;
    }
  }
  .tabs-right-actions {
    position: absolute;
    right: 0;
    top: 0;
    z-index: 2;
  }
  .header-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .member-card {
    position: relative;
    transition: transform 0.2s;
    
    &:hover {
      transform: translateY(-5px);
      
      .member-actions {
        opacity: 1;
      }
    }
    
    .member-avatar {
      text-align: center;
      position: relative;
    }
    
    .member-info {
      cursor: pointer;
      text-align: center;
      margin-top: 15px;
      
      h3 {
        margin: 10px 0 5px;
      }
    }
    
    .member-actions {
      position: absolute;
      top: 10px;
      right: 10px;
      display: flex;
      gap: 5px;
      opacity: 0;
      transition: opacity 0.3s;
      z-index: 10;
    }
    
    .member-info {
      h3 {
        font-size: 16px;
      }
      
      .member-role {
        color: #999;
        margin: 5px 0;
        font-size: 14px;
      }
      
      .member-department {
        color: #666;
        font-size: 13px;
        margin: 5px 0;
      }
    }
  }
  
  .department-placeholder {
    padding: 20px 0;
    .el-alert p {
      margin: 8px 0 0;
      color: #606266;
      line-height: 1.6;
    }
    .placeholder-hint {
      margin-left: 8px;
      color: #909399;
      font-size: 12px;
    }
  }
  .department-header-actions .current-store-label {
    margin-right: 16px;
    color: #606266;
    font-size: 14px;
    strong {
      color: var(--el-color-primary);
    }
  }
  .member-store {
    font-size: 12px;
    color: var(--el-color-primary);
    margin: 4px 0;
  }
  .form-store-name {
    color: var(--el-text-color-regular);
    font-weight: 500;
  }
  .store-list-view {
    min-height: 200px;
  }
  .store-card {
    cursor: pointer;
    transition: box-shadow 0.2s;
  }
  .store-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  }
  .department-card {
    .department-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      h3 {
        margin: 0;
      }
    }
    
    .department-description {
      color: #666;
      margin: 10px 0;
      min-height: 40px;
    }
  }
  
  .detail-avatar {
    text-align: center;
  }
  
  .schedule-slots {
    min-height: 80px;
  }
  .slot-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px 16px;
  }
  .slot-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 10px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    min-width: 100px;
  }
  .slot-time {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }
  .slot-status {
    font-size: 12px;
    color: var(--el-color-primary);
    margin-top: 4px;
  }
  
  .avatar-uploader .avatar {
    width: 120px;
    height: 120px;
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
  width: 120px;
  height: 120px;
  text-align: center;
}

/* 评价表格中评分列：防止「X分」竖排换行 */
.rate-cell-nowrap {
  display: inline-flex;
  align-items: center;
  white-space: nowrap;
}
.rate-cell-nowrap .el-rate {
  display: inline-flex;
  flex-wrap: nowrap;
}
</style>