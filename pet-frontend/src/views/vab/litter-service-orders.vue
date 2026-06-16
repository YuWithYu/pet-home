<template>
  <div class="door-cleaning-service-container">
    <el-card shadow="never" class="main-card">
      <template #header v-if="false">
        <div class="card-header">
          <span>预约订单管理</span>
          <div class="header-actions">
            <el-input
              v-if="!showOrderTabs"
              v-model="searchText"
              placeholder="搜索铲屎服务..."
              clearable
              class="header-input"
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
            <el-select
              v-if="!showOrderTabs"
              v-model="selectedStatus"
              placeholder="选择状态"
              clearable
              class="header-select"
              @change="handleStatusFilter"
            >
              <el-option label="全部状态" value="" />
              <el-option label="启用" value="active" />
              <el-option label="禁用" value="inactive" />
            </el-select>
            <el-button v-if="!showOrderTabs" type="primary" :icon="Plus" @click="openAddServiceDialog">
              添加上门铲屎服务
            </el-button>
            <el-button v-if="!showOrderTabs" type="success" :icon="Histogram" @click="showStatisticsDialog">
              统计信息
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="inner-tabs" @tab-change="handleTabChange">
        <el-tab-pane v-if="!isStaff && !showOrderTabs" label="所有铲屎服务" name="services">
          <el-table
            :data="services"
            v-loading="loadingServices"
            border
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="服务名称" min-width="160" />
            <el-table-column label="服务简介" min-width="200">
              <template #default="{ row }">
                <div class="description-cell">{{ row.description || '-' }}</div>
              </template>
            </el-table-column>
            <el-table-column label="商品介绍" min-width="220">
              <template #default="{ row }">
                <el-tooltip v-if="row.introduction && row.introduction.length" placement="top">
                  <template #content>
                    <div v-for="(item, index) in row.introduction" :key="index">{{ index + 1 }}. {{ item }}</div>
                  </template>
                  <span class="ellipsis">{{ row.introduction.join('，') }}</span>
                </el-tooltip>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="使用须知" min-width="220">
              <template #default="{ row }">
                <el-tooltip v-if="row.instructions && row.instructions.length" placement="top">
                  <template #content>
                    <div v-for="(item, index) in row.instructions" :key="index">{{ index + 1 }}. {{ item }}</div>
                  </template>
                  <span class="ellipsis">{{ row.instructions.join('，') }}</span>
                </el-tooltip>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="价格" width="100">
              <template #default="{ row }">
                <span class="price-tag">¥{{ formatAmount(row.price) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(分钟)" width="110" />
            <el-table-column label="封面图" width="120">
              <template #default="{ row }">
                <el-image
                  v-if="getServiceImageUrl(row)"
                  :key="'img-' + row.id + '-' + (getServiceImageUrl(row) || '')"
                  :src="getImageUrl(getServiceImageUrl(row))"
                  :preview-src-list="[getImageUrl(getServiceImageUrl(row))]"
                  style="width: 60px; height: 60px"
                  fit="cover"
                />
                <span v-else>无图片</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'">
                  {{ row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button type="link" size="small" @click="openEditServiceDialog(row)">编辑</el-button>
                <el-button type="link" size="small" @click="openImageDialog(row)">更换图片</el-button>
                <el-button
                  type="link"
                  size="small"
                  :style="{ color: row.status === 'active' ? '#e6a23c' : '#67c23a' }"
                  @click="toggleServiceStatus(row)"
                >
                  {{ row.status === 'active' ? '禁用' : '启用' }}
                </el-button>
                <el-popconfirm
                  title="确定删除该服务吗？"
                  @confirm="deleteService(row)"
                >
                  <template #reference>
                    <el-button type="link" size="small" style="color: #f56c6c">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="servicePagination.current"
              v-model:page-size="servicePagination.size"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="servicePagination.total"
              @size-change="handleServiceSizeChange"
              @current-change="handleServicePageChange"
            />
          </div>

        </el-tab-pane>

        <el-tab-pane v-if="!isStaff && showStaffManagementTabs" label="排班管理" name="schedule">
          <div class="schedule-panel">
            <div class="schedule-toolbar">
              <el-date-picker
                v-model="selectedDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                @change="viewDaySchedule"
              />
              <el-select
                v-if="isSuperAdmin"
                v-model="scheduleStoreId"
                placeholder="选择门店"
                clearable
                class="schedule-store-select"
                style="width: 220px"
                @change="onScheduleStoreChange"
              >
                <el-option label="全部门店" :value="null" />
                <el-option
                  v-for="s in scheduleStoreOptions"
                  :key="s.id"
                  :label="s.storeName || s.name"
                  :value="s.id"
                />
              </el-select>
              <div class="schedule-toolbar-actions">
                <el-button type="primary" @click="openBatchScheduleDialog">批量生成排班</el-button>
                <el-button @click="loadScheduleCalendar">刷新日历</el-button>
              </div>
            </div>

            <el-calendar v-model="calendarDisplayDate">
              <template #date-cell="{ data }">
                <div
                  class="calendar-cell"
                  :class="{ active: data.day === selectedDate }"
                  @click="selectCalendarDate(data.day)"
                >
                  <span>{{ Number(data.day.split('-')[2]) }}</span>
                  <el-badge
                    v-if="scheduleCalendar[data.day]"
                    :value="scheduleCalendar[data.day]"
                    type="primary"
                  />
                </div>
              </template>
            </el-calendar>

            <div class="schedule-table-wrapper">
              <div class="schedule-header-text">
                <h4>日期：{{ selectedDate || '未选择' }}</h4>
                <span class="extra">共 {{ daySchedule.length }} 条排班记录</span>
              </div>

              <el-table
                :data="daySchedule"
                border
                stripe
                v-loading="scheduleLoading"
                style="width: 100%"
              >
                <el-table-column prop="memberName" label="服务人员" min-width="120" />
                <el-table-column prop="timeSlot" label="时间段" min-width="120" />
                <el-table-column label="容量" min-width="100">
                  <template #default="{ row }">
                    {{ row.reservedCount || 0 }} / {{ row.maxCapacity || 1 }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" min-width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === '可预约' ? 'success' : 'info'">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" min-width="140">
                  <template #default="{ row }">
                    <el-button type="link" size="small" @click="openScheduleEditDialog(row)">编辑</el-button>
                    <el-popconfirm
                      title="确定删除该排班吗？"
                      @confirm="deleteSchedule(row)"
                    >
                      <template #reference>
                        <el-button type="link" size="small" style="color: #f56c6c">删除</el-button>
                      </template>
                    </el-popconfirm>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="!isStaff && showStaffManagementTabs" label="时间段管理" name="timeSlots">
          <div class="time-slot-panel">
            <div class="time-slot-toolbar">
              <el-button type="primary" @click="showAddTimeSlotDialog">添加时间段</el-button>
              <el-button @click="loadTimeSlots">刷新</el-button>
            </div>

            <el-table
              :data="timeSlots"
              border
              stripe
              v-loading="loadingTimeSlots"
              style="width: 100%"
            >
              <el-table-column prop="timeSlot" label="时间段" width="160" />
              <el-table-column prop="maxBookings" label="最大预约数" width="140" />
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.isActive ? 'success' : 'info'">
                    {{ row.isActive ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button type="link" size="small" @click="editTimeSlot(row)">编辑</el-button>
                  <el-button
                    type="link"
                    size="small"
                    @click="toggleTimeSlotStatus(row)"
                  >
                    {{ row.isActive ? '禁用' : '启用' }}
                  </el-button>
                  <el-popconfirm
                    title="确定删除该时间段吗？"
                    @confirm="deleteTimeSlot(row)"
                  >
                    <template #reference>
                      <el-button type="link" size="small" style="color: #f56c6c">删除</el-button>
                    </template>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="!isStaff && !showOrderTabs" label="服务展示图管理" name="banner">
          <div class="banner-section">
            <h4>上门铲屎服务展示图</h4>
            <p class="banner-desc">展示图会显示在小程序上门铲屎服务页顶部，推荐尺寸 750×400</p>
            <div v-if="serviceBannerImage" class="banner-preview">
              <el-image
                :src="getImageUrl(serviceBannerImage)"
                style="width: 320px; height: 200px"
                fit="cover"
              />
              <div class="banner-actions">
                <el-button type="primary" @click="bannerDialogVisible = true">更换图片</el-button>
                <el-button type="danger" @click="removeServiceBanner">删除图片</el-button>
              </div>
            </div>
            <el-empty v-else description="暂无展示图">
              <el-button type="primary" @click="bannerDialogVisible = true">上传展示图</el-button>
            </el-empty>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="!isStaff && showOrderTabs" label="预约订单" name="orders">
          <div class="orders-panel">
            <div class="orders-toolbar">
              <el-input
                v-model="orderSearchText"
                placeholder="搜索服务人员"
                clearable
                class="orders-input"
                @keyup.enter="loadServiceOrders"
                @clear="loadServiceOrders"
              />
              <el-select
                v-model="orderFilterStatus"
                placeholder="状态筛选"
                clearable
                class="orders-select"
                @change="handleOrderStatusFilter"
              >
                <el-option label="全部" value="" />
                <el-option label="待确认" value="pending" />
                <el-option label="已确认" value="confirmed" />
                <el-option label="取消待确认" value="cancel_pending" />
                <el-option label="变更待确认" value="change_pending" />
                <el-option label="已完成" value="completed" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
              <el-select
                v-model="orderFilterServiceName"
                placeholder="服务筛选"
                clearable
                class="orders-select"
                @change="handleOrderServiceFilter"
              >
                <el-option
                  v-for="item in serviceFilterOptions"
                  :key="'service-filter-' + item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <el-select
                v-if="isSuperAdmin"
                v-model="orderFilterStoreId"
                placeholder="选择门店"
                clearable
                class="orders-select"
                @change="handleOrderStoreFilter"
              >
                <el-option label="全部门店" :value="null" />
                <el-option
                  v-for="s in orderServiceStoreList"
                  :key="'store-' + s.id"
                  :label="s.storeName || s.name || ('门店' + s.id)"
                  :value="s.id"
                />
              </el-select>
              <el-button :icon="Refresh" @click="loadServiceOrders">刷新</el-button>
            </div>

            <el-table
              :data="filteredServiceOrders"
              border
              stripe
              v-loading="ordersLoading"
              style="width: 100%"
            >
              <el-table-column prop="id" label="订单号" width="140" />
              <el-table-column label="门店" width="160">
                <template #default="{ row }">
                  {{ row.storeName || row.location || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="customer" label="联系人" width="140" />
              <el-table-column prop="phone" label="联系电话" width="140" />
              <el-table-column prop="petName" label="宠物" width="120" />
              <el-table-column prop="serviceName" label="服务项目" min-width="140" show-overflow-tooltip />
              <el-table-column label="预约日期" width="160">
                <template #default="{ row }">
                  {{ formatDateOnly(row.date) }}
                </template>
              </el-table-column>
              <el-table-column prop="timeSlot" label="时间段" width="140" />
              <el-table-column prop="memberName" label="服务人员" width="140" />
              <el-table-column label="金额" width="120">
                <template #default="{ row }">
                  <span class="price-tag">¥{{ formatAmount(row.totalAmount) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusType(row.status)">
                    {{ getOrderStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <span class="order-actions-inline">
                    <el-button type="link" size="small" @click="viewOrderDetail(row)">详情</el-button>
                    <el-button
                      v-if="row.status === 'cancel_pending'"
                      type="link"
                      size="small"
                      class="text-primary"
                      @click="openCancelReviewModal(row)"
                    >
                      去审核
                    </el-button>
                    <el-button
                      v-if="row.status === 'change_pending'"
                      type="link"
                      size="small"
                      class="text-primary"
                      @click="openChangeReviewModal(row)"
                    >
                      去审核
                    </el-button>
                    <el-button
                      v-if="row.status === 'pending'"
                      type="link"
                      size="small"
                      @click="confirmOrder(row)"
                    >
                      确认
                    </el-button>
                    <el-button
                      v-if="row.status === 'pending'"
                      type="link"
                      size="small"
                      class="text-danger"
                      @click="rejectOrder(row)"
                    >
                      拒绝
                    </el-button>
                  </span>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container">
              <el-pagination
                v-model:current-page="orderPagination.current"
                v-model:page-size="orderPagination.size"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="orderPagination.total"
                @size-change="handleOrderSizeChange"
                @current-change="handleOrderCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane v-if="isStaff" label="我的工单" name="myOrders">
          <div class="orders-panel">
            <div class="orders-toolbar">
              <span>服务人员：{{ myMemberName || '未匹配到账号信息' }}</span>
              <el-button :icon="Refresh" @click="loadMyOrders" :disabled="!myMemberId">
                刷新
              </el-button>
            </div>

            <el-table
              :data="myOrders"
              border
              stripe
              v-loading="myOrdersLoading"
              style="width: 100%"
            >
              <el-table-column prop="id" label="订单号" width="140" />
              <el-table-column prop="customer" label="联系人" width="140" />
              <el-table-column prop="phone" label="联系电话" width="140" />
              <el-table-column prop="date" label="预约日期" width="160">
                <template #default="{ row }">
                  {{ formatDateOnly(row.date) }}
                </template>
              </el-table-column>
              <el-table-column prop="timeSlot" label="时间段" width="140" />
              <el-table-column prop="status" label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusType(row.status)">
                    {{ getOrderStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <span class="order-actions-inline">
                    <el-button type="link" size="small" @click="viewOrderDetail(row)">详情</el-button>
                    <el-button
                      v-if="row.status === 'cancel_pending'"
                      type="link"
                      size="small"
                      class="text-primary"
                      @click="openCancelReviewModal(row)"
                    >
                      去审核
                    </el-button>
                    <el-button
                      v-if="row.status === 'change_pending'"
                      type="link"
                      size="small"
                      class="text-primary"
                      @click="openChangeReviewModal(row)"
                    >
                      去审核
                    </el-button>
                    <el-button
                      v-if="row.status === 'pending'"
                      type="link"
                      size="small"
                      @click="confirmOrder(row)"
                    >
                      确认
                    </el-button>
                    <el-button
                      v-if="row.status === 'pending'"
                      type="link"
                      size="small"
                      class="text-danger"
                      @click="rejectOrder(row)"
                    >
                      拒绝
                    </el-button>
                    <el-button
                      v-if="row.status === 'confirmed'"
                      type="link"
                      size="small"
                      @click="completeOrder(row)"
                    >
                      完成
                    </el-button>
                  </span>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!myOrdersLoading && myOrders.length === 0" description="暂无工单" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 服务编辑 -->
    <el-dialog
      v-model="serviceDialogVisible"
      :title="isEditService ? '编辑上门铲屎服务' : '添加上门铲屎服务'"
      width="640px"
      @closed="resetServiceForm"
    >
      <el-form
        ref="serviceFormRef"
        :model="serviceForm"
        :rules="serviceFormRules"
        label-width="110px"
      >
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="serviceForm.name" placeholder="请输入服务名称" />
        </el-form-item>

        <el-form-item label="服务价格" prop="price">
          <el-input-number
            v-model="serviceForm.price"
            :min="0"
            :step="1"
            :precision="2"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="服务时长(分钟)" prop="duration">
          <el-input-number
            v-model="serviceForm.duration"
            :min="10"
            :max="480"
            :step="5"
            controls-position="right"
            style="width: 200px"
          />
        </el-form-item>


        <el-form-item label="服务简介">
          <el-input
            v-model="serviceForm.description"
            placeholder="简要介绍服务亮点"
            type="textarea"
            :rows="2"
          />
        </el-form-item>

        <el-form-item label="商品介绍" prop="introductionText">
          <el-input
            v-model="serviceForm.introductionText"
            type="textarea"
            :rows="4"
            placeholder="每行一条，例如：• 上门服务"
          />
        </el-form-item>

        <el-form-item label="使用须知">
          <el-input
            v-model="serviceForm.instructionsText"
            type="textarea"
            :rows="4"
            placeholder="每行一条使用须知"
          />
        </el-form-item>

        <el-form-item label="背景颜色">
          <el-color-picker v-model="serviceForm.bgColor" />
        </el-form-item>

        <el-form-item label="是否推荐">
          <el-switch v-model="serviceForm.isRecommended" />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="serviceForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="serviceDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="savingService" @click="saveService">
            保 存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 服务图片 -->
    <el-dialog
      v-model="imageDialogVisible"
      title="上传服务图片"
      width="420px"
    >
      <el-upload
        ref="serviceUploadRef"
        class="upload-block"
        drag
        :action="uploadUrl"
        :headers="uploadHeaders"
        :data="imageUploadPayload"
        :auto-upload="true"
        :show-file-list="false"
        :before-upload="beforeImageUpload"
        :on-success="handleServiceImageSuccess"
        :on-error="handleUploadError"
      >
        <UploadFilled class="upload-icon" />
        <div class="upload-text">
          <p>将图片拖到此处，或点击上传</p>
          <small>支持 jpg/png，大小不超过 10MB</small>
        </div>
      </el-upload>
    </el-dialog>

    <!-- 展示图上传 -->
    <el-dialog
      v-model="bannerDialogVisible"
      title="上传上门铲屎服务展示图"
      width="420px"
    >
      <el-upload
        class="upload-block"
        drag
        :action="bannerUploadUrl"
        :headers="uploadHeaders"
        :auto-upload="true"
        :show-file-list="false"
        :before-upload="beforeBannerUpload"
        :on-success="handleBannerUploadSuccess"
        :on-error="handleUploadError"
      >
        <UploadFilled class="upload-icon" />
        <div class="upload-text">
          <p>将图片拖到此处，或点击上传</p>
          <small>建议尺寸 750×400，大小不超过 10MB</small>
        </div>
      </el-upload>
    </el-dialog>

    <!-- 时间段 -->
    <el-dialog
      v-model="timeSlotDialogVisible"
      :title="isEditSlot ? '编辑时间段' : '添加时间段'"
      width="420px"
      @closed="resetTimeSlotForm"
    >
      <el-form
        ref="timeSlotFormRef"
        :model="timeSlotForm"
        :rules="timeSlotFormRules"
        label-width="110px"
      >
        <el-form-item label="时间段" prop="timeSlot">
          <el-input v-model="timeSlotForm.timeSlot" placeholder="例如：09:00-10:00" />
        </el-form-item>
        <el-form-item label="最大预约数" prop="maxBookings">
          <el-input-number v-model="timeSlotForm.maxBookings" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="timeSlotForm.isActive" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="timeSlotDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="savingTimeSlot" @click="saveTimeSlot">
            确 定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量排班 -->
    <el-dialog
      v-model="batchScheduleDialogVisible"
      title="批量生成排班"
      width="520px"
      @closed="resetBatchScheduleForm"
    >
      <el-form
        ref="batchScheduleFormRef"
        :model="batchScheduleForm"
        :rules="batchScheduleFormRules"
        label-width="120px"
      >
        <el-form-item label="排班日期" prop="dateRange">
          <el-date-picker
            v-model="batchScheduleForm.dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="服务人员" prop="memberIds">
          <el-select
            v-model="batchScheduleForm.memberIds"
            multiple
            placeholder="选择服务人员"
          >
            <el-option
              v-for="member in serviceMembers"
              :key="member.id"
              :label="member.memberName || member.username"
              :value="member.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlots">
          <el-select
            v-model="batchScheduleForm.timeSlots"
            multiple
            placeholder="选择排班时间段"
          >
            <el-option
              v-for="slot in activeTimeSlots"
              :key="slot.id || slot.timeSlot"
              :label="slot.timeSlot"
              :value="slot.timeSlot"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="单次接待数" prop="maxCapacity">
          <el-input-number v-model="batchScheduleForm.maxCapacity" :min="1" :max="5" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchScheduleDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="batchScheduling" @click="generateBatchSchedule">
            生成排班
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 排班编辑 -->
    <el-dialog
      v-model="scheduleEditDialogVisible"
      title="编辑排班"
      width="420px"
      @closed="resetScheduleEditForm"
    >
      <el-form
        ref="scheduleEditFormRef"
        :model="scheduleEditForm"
        :rules="scheduleEditFormRules"
        label-width="110px"
      >
        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="scheduleEditForm.date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
          />
        </el-form-item>
        <el-form-item label="服务人员" prop="memberId">
          <el-select v-model="scheduleEditForm.memberId" placeholder="选择服务人员">
            <el-option
              v-for="member in serviceMembers"
              :key="member.id"
              :label="member.memberName || member.username"
              :value="member.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间段" prop="timeSlot">
          <el-select v-model="scheduleEditForm.timeSlot" placeholder="选择时间段">
            <el-option
              v-for="slot in activeTimeSlots"
              :key="slot.id || slot.timeSlot"
              :label="slot.timeSlot"
              :value="slot.timeSlot"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="最大容量" prop="maxCapacity">
          <el-input-number v-model="scheduleEditForm.maxCapacity" :min="1" :max="5" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="scheduleEditForm.status">
            <el-option label="可预约" value="可预约" />
            <el-option label="已占用" value="已占用" />
            <el-option label="不可用" value="不可用" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="scheduleEditDialogVisible = false">取 消</el-button>
          <el-button type="primary" :loading="savingSchedule" @click="saveSchedule">
            保 存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 订单详情 -->
    <el-dialog
      v-model="orderDetailDialogVisible"
      title="预约详情"
      width="520px"
      @closed="onOrderDetailClosed"
    >
      <el-descriptions
        v-if="currentOrderDetail"
        :column="2"
        border
        label-width="110px"
      >
        <el-descriptions-item label="订单号">{{ currentOrderDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getOrderStatusType(currentOrderDetail.status)">
            {{ getOrderStatusText(currentOrderDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentOrderDetail.customer }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentOrderDetail.phone }}</el-descriptions-item>
        <el-descriptions-item label="宠物">
          <span>{{ currentOrderDetail.petName || '-' }}</span>
          <el-button
            class="pet-detail-link"
            link
            type="primary"
            @click="openPetDetailDialog(currentOrderDetail)"
          >
            查看宠物信息
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="服务项目" :span="2">
          {{ currentOrderDetail.serviceName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ formatDateOnly(currentOrderDetail.date) }}</el-descriptions-item>
        <el-descriptions-item label="时间段">{{ currentOrderDetail.timeSlot }}</el-descriptions-item>
        <el-descriptions-item label="服务人员">{{ currentOrderDetail.memberName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务地址" :span="2">
          {{ currentOrderDetail.location || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentOrderDetail.remark || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="核销码">
          {{ currentOrderDetail.verifyCode || '未生成' }}
        </el-descriptions-item>
        <el-descriptions-item label="金额">
          <span class="price-tag">¥{{ formatAmount(currentOrderDetail.totalAmount) }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-skeleton v-else :rows="6" animated />
    </el-dialog>

    <!-- 宠物信息 -->
    <el-dialog
      v-model="petDetailDialogVisible"
      title="宠物信息"
      width="520px"
      @closed="currentPetDetail = null"
    >
      <el-skeleton v-if="petDetailLoading" :rows="5" animated />
      <el-descriptions
        v-else-if="currentPetDetail"
        :column="2"
        border
        label-width="110px"
      >
        <el-descriptions-item label="宠物ID">{{ currentPetDetail.id || '-' }}</el-descriptions-item>
        <el-descriptions-item label="宠物名称">{{ currentPetDetail.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ currentPetDetail.breed || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ formatPetGenderForDisplay(currentPetDetail.gender) }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentPetDetail.age || '-' }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ currentPetDetail.weight || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="暂无宠物信息" />
    </el-dialog>

    <!-- 取消预约审核弹窗（本页弹框，不跳转） -->
    <el-dialog
      v-model="cancelReviewModalVisible"
      title="取消预约审核"
      width="480px"
      :close-on-click-modal="false"
      @closed="cancelReviewRequest = null"
    >
      <div v-if="cancelReviewRequest" class="review-modal-body">
        <p><strong>预约ID：</strong>{{ cancelReviewRequest.appointmentId }}</p>
        <p><strong>取消原因：</strong>{{ cancelReviewRequest.reason }}</p>
        <p class="tip-inline">同意后将执行取消；服务开始前 0-2 小时内将扣 40% 违约金。</p>
      </div>
      <el-skeleton v-else :rows="2" animated />
      <template #footer>
        <el-button @click="cancelReviewModalVisible = false">关闭</el-button>
        <el-button type="danger" :loading="cancelReviewLoading" @click="confirmCancelReject">拒绝</el-button>
        <el-button type="success" :loading="cancelReviewLoading" @click="confirmCancelApprove">同意取消</el-button>
      </template>
    </el-dialog>

    <!-- 预约变更审核弹窗（本页弹框，不跳转） -->
    <el-dialog
      v-model="changeReviewModalVisible"
      title="预约变更审核"
      width="500px"
      :close-on-click-modal="false"
      @closed="changeReviewRequest = null"
    >
      <div v-if="changeReviewRequest" class="review-modal-body change-fields">
        <p><strong>预约ID：</strong>{{ changeReviewRequest.appointmentId }}</p>
        <template v-if="changeReviewRequest.requestedDate"><p><strong>申请日期：</strong>{{ changeReviewRequest.requestedDate }}</p></template>
        <template v-if="changeReviewRequest.requestedTimeSlot"><p><strong>申请时段：</strong>{{ changeReviewRequest.requestedTimeSlot }}</p></template>
        <template v-if="changeReviewRequest.requestedLocation"><p><strong>申请地址：</strong>{{ changeReviewRequest.requestedLocation }}</p></template>
        <template v-if="changeReviewRequest.requestedContactPhone"><p><strong>联系电话：</strong>{{ changeReviewRequest.requestedContactPhone }}</p></template>
        <template v-if="changeReviewRequest.requestedRemark"><p><strong>备注：</strong>{{ changeReviewRequest.requestedRemark }}</p></template>
      </div>
      <el-skeleton v-else :rows="2" animated />
      <template #footer>
        <el-button @click="changeReviewModalVisible = false">关闭</el-button>
        <el-button type="danger" :loading="changeReviewLoading" @click="confirmChangeReject">拒绝</el-button>
        <el-button type="success" :loading="changeReviewLoading" @click="confirmChangeApprove">同意</el-button>
      </template>
    </el-dialog>

    <!-- 统计信息 -->
    <el-dialog
      v-model="statisticsDialogVisible"
      title="上门铲屎服务统计"
      width="420px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="铲屎服务总数">
          {{ statistics.totalServices }}
        </el-descriptions-item>
        <el-descriptions-item label="启用中的服务">
          {{ statistics.activeServices }}
        </el-descriptions-item>
        <el-descriptions-item label="今日排班记录">
          {{ statistics.todaySchedule }}
        </el-descriptions-item>
        <el-descriptions-item label="本月排班总数">
          {{ statistics.monthSchedule }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled, Refresh, Histogram } from '@element-plus/icons-vue'
import axios from 'axios'
import dayjs from '@/utils/dayjs'
import { formatPetGenderForDisplay } from '@/utils/petDisplay'

import litterServiceApi from '@/api/litter-service'
import { baseURL } from '@/config'
import { getPendingByAppointment as getPendingChangeByAppointment, approve as approveChange, reject as rejectChange } from '@/api/appointment-change-request'
import { getPendingByAppointment as getPendingCancellationByAppointment, approve as approveCancellation, reject as rejectCancellation } from '@/api/appointment-cancellation-request'

const apiBase = (baseURL || '').trim().replace(/\/api\/?$/, '') || 'http://localhost:8080'

const SERVICE_TYPE = 'door-cleaning'
const BANNER_POSITION = 'litter-page-top'

const store = useStore()
const route = useRoute()
const router = useRouter()
const userRole = computed(() => store.getters['user/role'] || '')
const currentUserId = computed(() => store.getters['user/userInfo']?.id || store.getters['user/id'] || null)
const currentUsername = computed(() => store.getters['user/userInfo']?.username || store.getters['user/username'] || '')
const isStaff = computed(() => {
  const role = (userRole.value || '').toLowerCase()
  return role.includes('staff') || role.includes('nurse')
})
const serviceTypeNav = ref('door-cleaning')
const inStaffManagement = computed(() => route.path.startsWith('/service-staff'))
const inOrderManagement = computed(() => route.path.startsWith('/service-orders'))
const showStaffManagementTabs = computed(() => inStaffManagement.value)
const showOrderTabs = computed(() => inOrderManagement.value)

const syncServiceTypeNav = () => {
  if (route.path.includes('/grooming-services')) {
    serviceTypeNav.value = 'grooming'
  } else if (route.path.includes('/hospital-service')) {
    serviceTypeNav.value = 'hospital'
  } else {
    serviceTypeNav.value = 'door-cleaning'
  }
}

const handleServiceTypeChange = (val) => {
  const prefix = inOrderManagement.value ? '/service-orders' : '/service-platform'
  const map = {
    'door-cleaning': `${prefix}/${inOrderManagement.value ? 'appointment-orders' : 'litter-service'}`,
    grooming: `${prefix}/${inOrderManagement.value ? 'grooming-service-orders' : 'grooming-services'}`,
    hospital: `${prefix}/${inOrderManagement.value ? 'hospital-service-orders' : 'hospital-service'}`,
  }
  const target = map[val]
  if (target && route.path !== target) router.push(target)
}

const api = axios.create({
  baseURL: apiBase,
  timeout: 12000,
})

api.interceptors.request.use(
  (config) => {
    const token =
      localStorage.getItem('vue-admin-better-2024') ||
      sessionStorage.getItem('vue-admin-better-2024') ||
      localStorage.getItem('token') ||
      ''
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // 处理 401 错误（Token无效或已过期）
    if (error.response && error.response.status === 401) {
      const errorMsg = error.response.data?.msg || error.response.data?.message || 'Token无效或已过期'
      console.error('API返回401错误:', errorMsg)
      
      // 检查是否已经显示过401提示，避免重复提示
      const hasShown401 = sessionStorage.getItem('hasShown401')
      if (!hasShown401) {
        sessionStorage.setItem('hasShown401', 'true')
        
        // 清除本地存储的token
        localStorage.removeItem('vue-admin-better-2024')
        sessionStorage.removeItem('vue-admin-better-2024')
        localStorage.removeItem('token')
        
        // 显示错误提示，并提供重新登录选项
        ElMessageBox.confirm(
          errorMsg + '，需要重新登录才能继续操作',
          '登录已过期',
          {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning',
            showClose: false,
            closeOnClickModal: false,
            closeOnPressEscape: false,
          }
        )
          .then(() => {
            // 用户点击确认，跳转到登录页
            sessionStorage.removeItem('hasShown401')
            window.location.href = '/login'
          })
          .catch(() => {
            // 用户点击取消，清除标记，允许后续操作
            sessionStorage.removeItem('hasShown401')
          })
      }
    }
    return Promise.reject(error)
  },
)

const isSuccess = (res) => res && (res.code === 0 || res.code === 200 || res.success === true)
const resolveMessage = (res, fallback = '操作失败') =>
  res?.message || res?.msg || res?.data?.message || fallback

const activeTab = ref('services')

/**
 * 服务列表
 */
const services = ref([])
const loadingServices = ref(false)
const searchText = ref('')
const selectedStatus = ref('')
const servicePagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})

const loadServices = async () => {
  loadingServices.value = true
  try {
    const params = {
      pageNo: servicePagination.current,
      pageSize: servicePagination.size,
      name: searchText.value || undefined,
      status: selectedStatus.value || undefined,
    }
    const res = await litterServiceApi.getLitterServicePage(params)
    if (isSuccess(res) && res.data) {
      services.value = Array.isArray(res.data.records) ? res.data.records : []
      servicePagination.total = res.data.total || services.value.length
    } else {
      services.value = []
      servicePagination.total = 0
      ElMessage.error(resolveMessage(res, '加载铲屎服务失败'))
    }
  } catch (error) {
    console.error('加载铲屎服务失败:', error)
    services.value = []
    servicePagination.total = 0
    ElMessage.error('加载铲屎服务失败，请稍后重试')
  } finally {
    loadingServices.value = false
  }
}

const handleSearch = () => {
  servicePagination.current = 1
  loadServices()
}

const handleStatusFilter = () => {
  servicePagination.current = 1
  loadServices()
}

const handleServiceSizeChange = (val) => {
  servicePagination.size = val
  servicePagination.current = 1
  loadServices()
}

const handleServicePageChange = (val) => {
  servicePagination.current = val
  loadServices()
}

/**
 * 服务表单
 */
const serviceDialogVisible = ref(false)
const isEditService = ref(false)
const savingService = ref(false)
const serviceFormRef = ref(null)
const serviceForm = reactive({
  id: null,
  name: '',
  price: 0,
  category: '',
  duration: 45,
  description: '',
  introductionText: '',
  instructionsText: '',
  bgColor: '#f6f7fb',
  isRecommended: false,
  sortOrder: 0,
  tags: [],
})

const serviceFormRules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入服务价格', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入服务时长', trigger: 'blur' }],
  introductionText: [{ required: true, message: '请填写商品介绍', trigger: 'blur' }],
}

const resetServiceForm = () => {
  serviceForm.id = null
  serviceForm.name = ''
  serviceForm.price = 0
  serviceForm.category = ''
  serviceForm.duration = 45
  serviceForm.description = ''
  serviceForm.introductionText = ''
  serviceForm.instructionsText = ''
  serviceForm.bgColor = '#f6f7fb'
  serviceForm.isRecommended = false
  serviceForm.sortOrder = 0
  serviceForm.tags = []
  serviceFormRef.value?.resetFields()
}

const openAddServiceDialog = () => {
  isEditService.value = false
  resetServiceForm()
  serviceDialogVisible.value = true
}

const openEditServiceDialog = async (row) => {
  try {
    const res = await litterServiceApi.getLitterServiceById(row.id)
    const data = isSuccess(res) && res.data ? res.data : row
    serviceForm.id = data.id
    serviceForm.name = data.name
    serviceForm.price = Number(data.price || 0)
    serviceForm.category = data.category || ''
    serviceForm.duration = data.duration || 45
    serviceForm.description = data.description || ''
    serviceForm.introductionText = normalizeArray(data.introduction).join('\n')
    serviceForm.instructionsText = normalizeArray(data.instructions).join('\n')
    serviceForm.bgColor = data.bgColor || '#f6f7fb'
    serviceForm.isRecommended = !!data.isRecommended
    serviceForm.sortOrder = data.sortOrder || 0
    serviceForm.tags = Array.isArray(data.tags) ? data.tags : []
    isEditService.value = true
    serviceDialogVisible.value = true
  } catch (error) {
    console.error('加载铲屎服务详情失败:', error)
    ElMessage.error('加载详情失败，请稍后重试')
  }
}

const saveService = async () => {
  try {
    await serviceFormRef.value.validate()
  } catch (error) {
    return
  }
  savingService.value = true
  try {
    const payload = {
      id: serviceForm.id,
      name: serviceForm.name,
      price: Number(serviceForm.price || 0),
      category: serviceForm.category,
      duration: serviceForm.duration,
      description: serviceForm.description,
      introduction: normalizeArray(serviceForm.introductionText),
      instructions: normalizeArray(serviceForm.instructionsText),
      bgColor: serviceForm.bgColor,
      isRecommended: serviceForm.isRecommended,
      sortOrder: serviceForm.sortOrder,
      tags: serviceForm.tags,
      status: 'active',
    }
    let res
    if (isEditService.value) {
      res = await litterServiceApi.updateLitterService(payload)
    } else {
      res = await litterServiceApi.createLitterService(payload)
    }
    if (isSuccess(res)) {
      ElMessage.success(isEditService.value ? '更新成功' : '创建成功')
      serviceDialogVisible.value = false
      loadServices()
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('保存铲屎服务失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    savingService.value = false
  }
}

const toggleServiceStatus = async (row) => {
  const next = row.status === 'active' ? 'inactive' : 'active'
  try {
    const res = await litterServiceApi.updateLitterServiceStatus(row.id, next)
    if (isSuccess(res)) {
      ElMessage.success('状态更新成功')
      loadServices()
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

const deleteService = async (row) => {
  try {
    const res = await litterServiceApi.deleteLitterService(row.id)
    if (isSuccess(res)) {
      ElMessage.success('删除成功')
      loadServices()
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('删除铲屎服务失败:', error)
    ElMessage.error('删除失败，请稍后重试')
  }
}

/**
 * 服务图片上传
 */
const imageDialogVisible = ref(false)
const serviceUploadRef = ref(null)
const imageUploadPayload = reactive({
  id: null,
})

const resolveAuthToken = () => {
  return (
    localStorage.getItem('vue-admin-better-2024') ||
    sessionStorage.getItem('vue-admin-better-2024') ||
    localStorage.getItem('token') ||
    ''
  )
}

const buildAuthorizationHeader = () => {
  const token = resolveAuthToken()
  return token ? `Bearer ${token}` : ''
}

const uploadHeaders = reactive({
  Authorization: buildAuthorizationHeader(),
})

const refreshUploadHeaders = () => {
  uploadHeaders.Authorization = buildAuthorizationHeader()
}

const uploadUrl = ref(apiBase + '/api/litter-services/upload')
const bannerUploadUrl = ref(apiBase + '/api/litter-banners/upload')

const openImageDialog = (row) => {
  refreshUploadHeaders()
  imageUploadPayload.id = row.id
  imageDialogVisible.value = true
}

const beforeImageUpload = (file) => {
  refreshUploadHeaders()
  const isImage = file.type?.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) ElMessage.error('仅支持图片格式的文件')
  if (!isLt10M) ElMessage.error('图片大小不能超过 10MB')
  return isImage && isLt10M
}

const handleServiceImageSuccess = (response) => {
  if (isSuccess(response)) {
    ElMessage.success('图片上传成功')
    imageDialogVisible.value = false
    loadServices()
  } else {
    ElMessage.error(resolveMessage(response, '图片上传失败'))
  }
}

const beforeBannerUpload = (file) => {
  refreshUploadHeaders()
  const isImage = file.type?.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) ElMessage.error('仅支持图片格式的文件')
  if (!isLt10M) ElMessage.error('图片大小不能超过 10MB')
  return isImage && isLt10M
}

const handleBannerUploadSuccess = (response) => {
  if (isSuccess(response)) {
    ElMessage.success('展示图上传成功')
    bannerDialogVisible.value = false
    loadServiceBanner()
  } else {
    ElMessage.error(resolveMessage(response, '展示图上传失败'))
  }
}

const handleUploadError = (error) => {
  console.error('文件上传失败:', error)
  ElMessage.error('上传失败，请稍后再试')
}

/**
 * 服务展示图
 */
const serviceBannerImage = ref('')
const serviceBannerRecord = ref(null)
const bannerDialogVisible = ref(false)

const loadServiceBanner = async () => {
  try {
    const res = await litterServiceApi.getServiceBanner()
    if (isSuccess(res) && res.data) {
      serviceBannerRecord.value = res.data
      serviceBannerImage.value = res.data.imageUrl || ''
    } else {
      serviceBannerRecord.value = null
      serviceBannerImage.value = ''
    }
  } catch (error) {
    console.error('加载展示图失败:', error)
    serviceBannerRecord.value = null
    serviceBannerImage.value = ''
  }
}

const removeServiceBanner = () => {
  if (!serviceBannerRecord.value) {
    return
  }
  const bannerId = serviceBannerRecord.value?.id
  ElMessageBox.confirm('确定要删除当前展示图吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        let res
        if (bannerId) {
          res = await litterServiceApi.deleteServiceBanner(bannerId)
        } else {
          serviceBannerRecord.value = null
          serviceBannerImage.value = ''
          ElMessage.success('展示图已删除')
          return
        }
        if (isSuccess(res)) {
          ElMessage.success('展示图已删除')
          serviceBannerRecord.value = null
          serviceBannerImage.value = ''
          loadServiceBanner()
        } else {
          ElMessage.error(resolveMessage(res, '删除失败'))
        }
      } catch (error) {
        console.error('删除展示图失败:', error)
        ElMessage.error('删除失败，请稍后再试')
      }
    })
    .catch(() => {})
}

/**
 * 时间段管理
 */
const timeSlots = ref([])
const loadingTimeSlots = ref(false)
const timeSlotDialogVisible = ref(false)
const isEditSlot = ref(false)
const savingTimeSlot = ref(false)
const timeSlotFormRef = ref(null)
const timeSlotForm = reactive({
  id: null,
  timeSlot: '',
  maxBookings: 6,
  isActive: true,
})

const timeSlotFormRules = {
  timeSlot: [
    { required: true, message: '请输入时间段', trigger: 'blur' },
    {
      pattern: /^([01]\d|2[0-3]):[0-5]\d-([01]\d|2[0-3]):[0-5]\d$/,
      message: '格式错误，例如 09:00-10:00',
      trigger: 'blur',
    },
  ],
  maxBookings: [{ required: true, message: '请输入最大预约数', trigger: 'blur' }],
}

const loadTimeSlots = async () => {
  loadingTimeSlots.value = true
  try {
    const res = await api.get('/api/time-slots/list', {
      params: { serviceType: SERVICE_TYPE },
    })
    if (isSuccess(res)) {
      timeSlots.value = res.data || []
    }
  } catch (error) {
    console.error('加载时间段失败:', error)
    ElMessage.error('加载时间段失败')
  } finally {
    loadingTimeSlots.value = false
  }
}

const showAddTimeSlotDialog = () => {
  isEditSlot.value = false
  resetTimeSlotForm()
  timeSlotDialogVisible.value = true
}

const editTimeSlot = (slot) => {
  isEditSlot.value = true
  timeSlotForm.id = slot.id
  timeSlotForm.timeSlot = slot.timeSlot
  timeSlotForm.maxBookings = slot.maxBookings
  timeSlotForm.isActive = slot.isActive
  timeSlotDialogVisible.value = true
}

const resetTimeSlotForm = () => {
  timeSlotForm.id = null
  timeSlotForm.timeSlot = ''
  timeSlotForm.maxBookings = 6
  timeSlotForm.isActive = true
  timeSlotFormRef.value?.resetFields()
}

const saveTimeSlot = async () => {
  try {
    await timeSlotFormRef.value.validate()
  } catch (error) {
    return
  }
  savingTimeSlot.value = true
    try {
      const storeId = effectiveScheduleStoreId.value ?? currentStoreId.value
      const payload = {
        id: timeSlotForm.id,
        timeSlot: timeSlotForm.timeSlot,
        maxBookings: timeSlotForm.maxBookings,
        isActive: timeSlotForm.isActive,
        serviceType: SERVICE_TYPE,
      }
      if (storeId != null) payload.storeId = storeId
      const url = isEditSlot.value ? '/api/time-slots/update' : '/api/time-slots/create'
      const res = await api.post(url, payload)
    if (isSuccess(res)) {
      ElMessage.success(isEditSlot.value ? '时间段更新成功' : '时间段添加成功')
      timeSlotDialogVisible.value = false
      loadTimeSlots()
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('保存时间段失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    savingTimeSlot.value = false
  }
}

const toggleTimeSlotStatus = async (slot) => {
  try {
    const res = await api.put(`/api/time-slots/${slot.id}/status`, null, {
      params: { isActive: !slot.isActive },
    })
    if (isSuccess(res)) {
      ElMessage.success('状态更新成功')
      loadTimeSlots()
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新失败，请稍后再试')
  }
}

const deleteTimeSlot = (slot) => {
  ElMessageBox.confirm('确定要删除这个时间段吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        const res = await api.delete(`/api/time-slots/${slot.id}`)
        if (isSuccess(res)) {
          ElMessage.success('删除成功')
          loadTimeSlots()
        } else {
          ElMessage.error(resolveMessage(res, '删除失败'))
        }
      } catch (error) {
        console.error('删除时间段失败:', error)
        ElMessage.error('删除失败，请稍后再试')
      }
    })
    .catch(() => {})
}

const activeTimeSlots = computed(() => timeSlots.value.filter((slot) => slot.isActive))

/**
 * 排班管理
 */
const serviceMembers = ref([])
const selectedDate = ref(dayjs().format('YYYY-MM-DD'))
const calendarDisplayDate = ref(dayjs().format('YYYY-MM-DD'))
const scheduleCalendar = reactive({})
const lastLoadedCalendarMonth = ref('')
const daySchedule = ref([])
const scheduleLoading = ref(false)
const batchScheduling = ref(false)
const savingSchedule = ref(false)

const batchScheduleDialogVisible = ref(false)
const scheduleEditDialogVisible = ref(false)
const batchScheduleFormRef = ref(null)
const scheduleEditFormRef = ref(null)

const batchScheduleForm = reactive({
  dateRange: [],
  memberIds: [],
  timeSlots: [],
  maxCapacity: 1,
})

const batchScheduleFormRules = {
  dateRange: [{ required: true, message: '请选择排班日期范围', trigger: 'change' }],
  memberIds: [{ required: true, message: '请选择服务人员', trigger: 'change' }],
  timeSlots: [{ required: true, message: '请选择时间段', trigger: 'change' }],
}

const scheduleEditForm = reactive({
  id: null,
  date: '',
  memberId: null,
  timeSlot: '',
  maxCapacity: 1,
  status: '可预约',
})

const scheduleEditFormRules = {
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  memberId: [{ required: true, message: '请选择服务人员', trigger: 'change' }],
  timeSlot: [{ required: true, message: '请选择时间段', trigger: 'change' }],
  maxCapacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
}

// 服务门店ID，分店管理员只看到本店排班与成员
const currentStoreId = computed(() => store.getters['user/serviceStoreId'] ?? null)
const isSuperAdmin = computed(() => store.getters['user/role'] === 'admin')

// 排班门店选择（超级管理员可选门店查看，分店管理员固定本店）
const scheduleStoreOptions = ref([])
const scheduleStoreId = ref(null)
const effectiveScheduleStoreId = computed(() => {
  if (isSuperAdmin.value) return scheduleStoreId.value ?? null
  return currentStoreId.value
})

const loadScheduleStoreOptions = async () => {
  try {
    const res = await api.get('/api/stores/all')
    if (isSuccess(res) && res.data && Array.isArray(res.data)) {
      scheduleStoreOptions.value = res.data.map((s) => ({
        id: s.id,
        name: s.storeName || s.name,
        storeName: s.storeName || s.name || `门店${s.id}`,
      }))
    } else {
      scheduleStoreOptions.value = []
    }
  } catch (e) {
    scheduleStoreOptions.value = []
  }
}

const onScheduleStoreChange = () => {
  loadScheduleCalendar()
  viewDaySchedule(selectedDate.value)
  loadServiceMembers()
  resetBatchScheduleForm()
}

// 服务人员按「当前选中的排班门店」过滤；未选门店时不给任何人（排班必须指定门店）
const loadServiceMembers = async () => {
  const storeId = effectiveScheduleStoreId.value ?? currentStoreId.value
  if (isSuperAdmin.value && storeId == null) {
    serviceMembers.value = []
    return
  }
  try {
    const params = { serviceType: SERVICE_TYPE }
    if (storeId != null) params.storeId = storeId
    const res = await api.get('/api/service-member/list', { params })
    serviceMembers.value = isSuccess(res) && Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('加载上门铲屎服务人员失败:', error)
    serviceMembers.value = []
  }
}

// 打开批量排班弹窗前校验：超级管理员必须先选择门店
const openBatchScheduleDialog = () => {
  if (isSuperAdmin.value && scheduleStoreId.value == null) {
    ElMessage.warning('请先选择门店再生成排班')
    return
  }
  batchScheduleDialogVisible.value = true
}

const loadScheduleCalendar = async () => {
  try {
    const base = calendarDisplayDate.value ? dayjs(calendarDisplayDate.value) : dayjs()
    const startDate = base.startOf('month').format('YYYY-MM-DD')
    const endDate = base.endOf('month').format('YYYY-MM-DD')
    const params = {
      serviceType: SERVICE_TYPE,
      startDate,
      endDate,
    }
    if (effectiveScheduleStoreId.value != null) params.storeId = effectiveScheduleStoreId.value
    const res = await api.get('/api/schedule/calendar', { params })
    if (isSuccess(res) && res.data) {
      Object.keys(scheduleCalendar).forEach((key) => {
        delete scheduleCalendar[key]
      })
      Object.entries(res.data).forEach(([key, value]) => {
        scheduleCalendar[key] = value
      })
      lastLoadedCalendarMonth.value = base.format('YYYY-MM')
    }
  } catch (error) {
    console.error('加载排班日历失败:', error)
  }
}

const viewDaySchedule = async (date = selectedDate.value) => {
  if (!date) return
  selectedDate.value = date
  calendarDisplayDate.value = date
  scheduleLoading.value = true
  try {
    const params = { serviceType: SERVICE_TYPE, date }
    if (effectiveScheduleStoreId.value != null) params.storeId = effectiveScheduleStoreId.value
    const res = await api.get('/api/schedule/day', { params })
    daySchedule.value = isSuccess(res) && Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('加载排班详情失败:', error)
    daySchedule.value = []
  } finally {
    scheduleLoading.value = false
  }
}

const selectCalendarDate = (date) => {
  calendarDisplayDate.value = date
  viewDaySchedule(date)
}

const openScheduleEditDialog = (record) => {
  scheduleEditForm.id = record.id
  scheduleEditForm.date = record.date
  scheduleEditForm.memberId = record.memberId
  scheduleEditForm.timeSlot = record.timeSlot
  scheduleEditForm.maxCapacity = record.maxCapacity || 1
  scheduleEditForm.status = record.status || '可预约'
  scheduleEditDialogVisible.value = true
}

const resetScheduleEditForm = () => {
  scheduleEditForm.id = null
  scheduleEditForm.date = selectedDate.value
  scheduleEditForm.memberId = null
  scheduleEditForm.timeSlot = ''
  scheduleEditForm.maxCapacity = 1
  scheduleEditForm.status = '可预约'
  scheduleEditFormRef.value?.resetFields()
}

const saveSchedule = async () => {
  try {
    await scheduleEditFormRef.value.validate()
  } catch (error) {
    return
  }
  savingSchedule.value = true
  try {
    const payload = {
      id: scheduleEditForm.id,
      date: scheduleEditForm.date,
      memberId: scheduleEditForm.memberId,
      timeSlot: scheduleEditForm.timeSlot,
      maxCapacity: scheduleEditForm.maxCapacity,
      status: scheduleEditForm.status,
      serviceType: SERVICE_TYPE,
    }
    const res = await api.put(`/api/schedule/${scheduleEditForm.id}`, payload)
    if (isSuccess(res)) {
      ElMessage.success('排班更新成功')
      scheduleEditDialogVisible.value = false
      viewDaySchedule()
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('更新排班失败:', error)
    ElMessage.error('更新失败，请稍后重试')
  } finally {
    savingSchedule.value = false
  }
}

const deleteSchedule = async (record) => {
  try {
    const res = await api.delete(`/api/schedule/${record.id}`)
    if (isSuccess(res)) {
      ElMessage.success('排班已删除')
      viewDaySchedule()
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('删除排班失败:', error)
    ElMessage.error('删除失败')
  }
}

const resetBatchScheduleForm = () => {
  batchScheduleForm.dateRange = []
  batchScheduleForm.memberIds = []
  batchScheduleForm.timeSlots = []
  batchScheduleForm.maxCapacity = 1
  batchScheduleFormRef.value?.resetFields()
}

const generateBatchSchedule = async () => {
  try {
    await batchScheduleFormRef.value.validate()
  } catch (error) {
    return
  }
  batchScheduling.value = true
  try {
    const [start, end] = batchScheduleForm.dateRange
    if (!start || !end) {
      ElMessage.warning('请选择排班日期范围')
      return
    }
    if (!batchScheduleForm.memberIds?.length) {
      ElMessage.warning('请选择服务人员')
      return
    }
    if (!batchScheduleForm.timeSlots?.length) {
      ElMessage.warning('请选择时间段')
      return
    }

    const params = new URLSearchParams()
    params.append('serviceType', SERVICE_TYPE)
    params.append('startDate', start)
    params.append('endDate', end)
    params.append('maxCapacity', String(batchScheduleForm.maxCapacity || 1))
    batchScheduleForm.memberIds.forEach((id) => params.append('memberIds', String(id)))
    batchScheduleForm.timeSlots.forEach((slot) => params.append('timeSlots', slot))
    const storeId = effectiveScheduleStoreId.value ?? currentStoreId.value
    if (storeId != null) params.append('storeId', String(storeId))

    const res = await api.post(`/api/schedule-config/generate-batch?${params.toString()}`)
    if (isSuccess(res)) {
      ElMessage.success(`排班生成成功，共 ${res.data || 0} 条`)
      batchScheduleDialogVisible.value = false
      loadScheduleCalendar()
      viewDaySchedule(selectedDate.value)
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('批量生成排班失败:', error)
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    batchScheduling.value = false
  }
}

/**
 * 订单管理
 */
const serviceOrders = ref([])
const ordersLoading = ref(false)
const orderSearchText = ref('')
const orderFilterStatus = ref('')
const orderFilterServiceName = ref('')
const orderFilterStoreId = ref(null)
const serviceFilterOptions = [
  { label: '全部服务', value: '' },
  { label: '上门铲屎', value: 'door-cleaning' },
  { label: '宠物洗护', value: 'grooming' },
  { label: '宠物医院', value: 'hospital' },
]
const filteredServiceOrders = computed(() => {
  return serviceOrders.value || []
})
const orderServiceStoreList = ref([])
const orderPagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})

const currentOrderDetail = ref(null)
const orderDetailDialogVisible = ref(false)
const petDetailDialogVisible = ref(false)
const petDetailLoading = ref(false)
const currentPetDetail = ref(null)

const cancelReviewModalVisible = ref(false)
const cancelReviewRequest = ref(null)
const cancelReviewLoading = ref(false)
const changeReviewModalVisible = ref(false)
const changeReviewRequest = ref(null)
const changeReviewLoading = ref(false)

const APPOINTMENT_TYPE = 'door_cleaning'
const resolveAppointmentTypeForAudit = (order) => {
  const serviceType = resolveOrderServiceType(order)
  if (serviceType === 'grooming') return 'grooming'
  if (serviceType === 'hospital') return 'hospital'
  return APPOINTMENT_TYPE
}

const serviceApiMap = {
  'door-cleaning': {
    url: '/api/door-cleaning/page',
    serviceType: 'door-cleaning',
  },
  grooming: {
    url: '/api/grooming-appointments/page',
    serviceType: 'grooming',
  },
  hospital: {
    url: '/api/hospital-appointments/page',
    serviceType: 'hospital',
  },
}

const applyMemberKeywordFilter = (list, keyword) => {
  const q = String(keyword || '').trim().toLowerCase()
  if (!q) return Array.isArray(list) ? list : []
  const source = Array.isArray(list) ? list : []
  return source.filter((item) => {
    const memberName = String(item?.memberName || '').toLowerCase()
    const memberUsername = String(item?.memberUsername || item?.username || '').toLowerCase()
    const memberId = String(item?.memberId ?? '')
    return memberName.includes(q) || memberUsername.includes(q) || memberId.includes(q)
  })
}

const loadServiceOrders = async () => {
  // 服务人员只看「我的工单」，不请求管理端分页接口，避免 403 误报
  if (isStaff.value) {
    serviceOrders.value = []
    orderPagination.total = 0
    ordersLoading.value = false
    return
  }
  if (orderServiceStoreList.value.length === 0) {
    loadOrderServiceStores()
  }
  // 超管：仅按订单 Tab 的门店下拉筛选；不要用顶部 serviceStoreId，否则易把「全部门店」变成单店且与 store_id 为空的订单对不上
  const effectiveStoreId = isSuperAdmin.value
    ? orderFilterStoreId.value
    : (currentStoreId.value ?? orderFilterStoreId.value)
  ordersLoading.value = true
  try {
    const selectedType = orderFilterServiceName.value
    const keyword = String(orderSearchText.value || '').trim()
    const commonParams = {
      status: orderFilterStatus.value || undefined,
      // 服务人员搜索改为前端统一过滤，避免后端 keyword 字段与 memberName 不一致导致误筛空
      keyword: undefined,
      storeId: effectiveStoreId ?? undefined,
    }

    if (selectedType) {
      const config = serviceApiMap[selectedType]
      const res = await api.get(config.url, {
        params: {
          pageNo: keyword ? 1 : orderPagination.current,
          pageSize: keyword ? 500 : orderPagination.size,
          serviceType: config.serviceType,
          ...commonParams,
        },
      })
      if (isSuccess(res) && res.data) {
        const records = res.data.records || []
        const mapped = records.map((x) => transformAppointmentRecord(x, selectedType))
        const filtered = applyMemberKeywordFilter(mapped, keyword)
        if (keyword) {
          orderPagination.total = filtered.length
          const start = (orderPagination.current - 1) * orderPagination.size
          serviceOrders.value = filtered.slice(start, start + orderPagination.size)
        } else {
          serviceOrders.value = filtered
          orderPagination.total = res.data.total || filtered.length
        }
      } else {
        serviceOrders.value = []
        orderPagination.total = 0
        ElMessage.error(resolveMessage(res, '加载预约订单失败'))
      }
    } else {
      const fetchAll = Object.entries(serviceApiMap).map(([type, config]) =>
        api.get(config.url, {
          params: {
            pageNo: 1,
            pageSize: 500,
            serviceType: config.serviceType,
            ...commonParams,
          },
        }).then((res) => ({ type, res }))
      )
      const allResults = await Promise.all(fetchAll)
      const merged = []
      for (const { type, res } of allResults) {
        if (isSuccess(res) && res.data) {
          const records = res.data.records || []
          merged.push(...records.map((x) => transformAppointmentRecord(x, type)))
        }
      }
      merged.sort((a, b) => String(b.createTime || '').localeCompare(String(a.createTime || '')))
      const filtered = applyMemberKeywordFilter(merged, keyword)
      orderPagination.total = filtered.length
      const start = (orderPagination.current - 1) * orderPagination.size
      serviceOrders.value = filtered.slice(start, start + orderPagination.size)
    }
  } catch (error) {
    console.error('加载上门铲屎预约订单失败:', error)
    serviceOrders.value = []
    orderPagination.total = 0
    const errorMsg = error.response?.data?.msg || error.message || '网络请求失败，请检查网络连接或登录状态'
    ElMessage.error(errorMsg)
  } finally {
    ordersLoading.value = false
  }
}

const handleOrderStatusFilter = () => {
  orderPagination.current = 1
  loadServiceOrders()
}

const handleOrderStoreFilter = () => {
  orderPagination.current = 1
  loadServiceOrders()
}

const handleOrderServiceFilter = () => {
  orderPagination.current = 1
  loadServiceOrders()
}

const loadOrderServiceStores = async () => {
  try {
    const res = await api.get('/api/stores/all')
    if (isSuccess(res) && res.data && Array.isArray(res.data)) {
      orderServiceStoreList.value = res.data.map((s) => ({
        id: s.id,
        name: s.storeName || s.name,
        storeName: s.storeName || s.name || `门店${s.id}`,
      }))
    } else {
      orderServiceStoreList.value = []
    }
  } catch (e) {
    orderServiceStoreList.value = []
  }
}

const handleOrderSizeChange = (val) => {
  orderPagination.size = val
  orderPagination.current = 1
  loadServiceOrders()
}

const handleOrderCurrentChange = (val) => {
  orderPagination.current = val
  loadServiceOrders()
}

const openCancelReviewModal = async (row) => {
  if (!row || !row.id) return
  cancelReviewModalVisible.value = true
  cancelReviewRequest.value = null
  try {
    const appointmentType = resolveAppointmentTypeForAudit(row)
    const res = await getPendingCancellationByAppointment(appointmentType, row.id)
    const data = (res && res.data !== undefined) ? res.data : res
    cancelReviewRequest.value = (data && data.id) ? data : (Array.isArray(data) ? data[0] : null)
    if (!cancelReviewRequest.value) {
      ElMessage.warning('未查到该预约的取消申请或已处理')
    }
  } catch (e) {
    ElMessage.error(e?.message || e?.msg || '加载失败')
    cancelReviewModalVisible.value = false
  }
}
const openChangeReviewModal = async (row) => {
  if (!row || !row.id) return
  changeReviewModalVisible.value = true
  changeReviewRequest.value = null
  try {
    const appointmentType = resolveAppointmentTypeForAudit(row)
    const res = await getPendingChangeByAppointment(appointmentType, row.id)
    const data = (res && res.data !== undefined) ? res.data : res
    changeReviewRequest.value = (data && data.id) ? data : (Array.isArray(data) ? data[0] : null)
    if (!changeReviewRequest.value) {
      ElMessage.warning('未查到该预约的变更申请或已处理')
    }
  } catch (e) {
    ElMessage.error(e?.message || e?.msg || '加载失败')
    changeReviewModalVisible.value = false
  }
}
const confirmCancelApprove = async () => {
  if (!cancelReviewRequest.value?.id) return
  cancelReviewLoading.value = true
  try {
    await approveCancellation(cancelReviewRequest.value.id)
    ElMessage.success('已同意取消')
    cancelReviewModalVisible.value = false
    loadServiceOrders()
    if (isStaff.value) loadMyOrders()
  } catch (e) {
    ElMessage.error(e?.message || e?.msg || '操作失败')
  } finally {
    cancelReviewLoading.value = false
  }
}
const confirmCancelReject = async () => {
  if (!cancelReviewRequest.value?.id) return
  try {
    const { value } = await ElMessageBox.prompt('请填写拒绝取消原因，用户将看到该说明', '拒绝取消申请', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因（必填）',
      inputValidator: (val) => (val != null && String(val).trim() !== '' ? true : '请填写拒绝原因'),
      inputErrorMessage: '请填写拒绝原因',
    })
    const reason = value != null ? String(value).trim() : ''
    cancelReviewLoading.value = true
    await rejectCancellation(cancelReviewRequest.value.id, reason)
    ElMessage.success('已拒绝取消')
    cancelReviewModalVisible.value = false
    loadServiceOrders()
    if (isStaff.value) loadMyOrders()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.message || e?.msg || '操作失败')
    }
  } finally {
    cancelReviewLoading.value = false
  }
}
const confirmChangeApprove = async () => {
  if (!changeReviewRequest.value?.id) return
  changeReviewLoading.value = true
  try {
    await approveChange(changeReviewRequest.value.id)
    ElMessage.success('已同意变更')
    changeReviewModalVisible.value = false
    loadServiceOrders()
    if (isStaff.value) loadMyOrders()
  } catch (e) {
    ElMessage.error(e?.message || e?.msg || '操作失败')
  } finally {
    changeReviewLoading.value = false
  }
}
const confirmChangeReject = async () => {
  if (!changeReviewRequest.value?.id) return
  changeReviewLoading.value = true
  try {
    await rejectChange(changeReviewRequest.value.id)
    ElMessage.success('已拒绝变更')
    changeReviewModalVisible.value = false
    loadServiceOrders()
    if (isStaff.value) loadMyOrders()
  } catch (e) {
    ElMessage.error(e?.message || e?.msg || '操作失败')
  } finally {
    changeReviewLoading.value = false
  }
}

const resolvePetIdFromOrder = (order) => {
  if (!order) return null
  const raw = order.petId ?? order.pet_id ?? order.pet?.id ?? null
  if (raw == null) return null
  const str = String(raw).trim()
  if (!str) return null
  return /^\d+$/.test(str) ? Number(str) : null
}

const buildPetDetailFallback = (order) => ({
  id: order?.petId ?? order?.pet_id ?? '-',
  name: order?.petName || order?.pet?.name || '-',
  breed: order?.petBreed || order?.pet?.breed || '-',
  gender: order?.petGender || order?.pet?.gender || '-',
  age: order?.petAge || order?.pet?.age || '-',
  weight: order?.petWeight || order?.pet?.weight || '-',
  color: order?.petColor || order?.pet?.color || '-',
  status: order?.petStatus || order?.pet?.status || '-',
  remark: order?.petRemark || order?.pet?.remark || '',
})

const openPetDetailDialog = async (order) => {
  petDetailDialogVisible.value = true
  petDetailLoading.value = true
  currentPetDetail.value = null

  const petId = resolvePetIdFromOrder(order)
  if (!petId) {
    currentPetDetail.value = buildPetDetailFallback(order)
    petDetailLoading.value = false
    return
  }

  try {
    const res = await api.get(`/api/pets/${petId}`)
    if (isSuccess(res) && res.data) {
      const p = res.data
      currentPetDetail.value = {
        id: p.id ?? petId,
        name: p.name || p.petName || '-',
        breed: p.breed || '-',
        gender: p.gender || '-',
        age: p.age || '-',
        weight: p.weight || '-',
        color: p.color || '-',
        status: p.status || '-',
        remark: p.remark || '',
      }
    } else {
      currentPetDetail.value = buildPetDetailFallback(order)
      ElMessage.warning('未获取到完整宠物信息，已显示订单中的宠物数据')
    }
  } catch (error) {
    console.error('获取宠物详情失败:', error)
    currentPetDetail.value = buildPetDetailFallback(order)
    ElMessage.warning('获取宠物详情失败，已显示订单中的宠物数据')
  } finally {
    petDetailLoading.value = false
  }
}

const onOrderDetailClosed = () => {
  currentOrderDetail.value = null
  petDetailDialogVisible.value = false
  currentPetDetail.value = null
}

const viewOrderDetail = async (order) => {
  if (!order) return
  orderDetailDialogVisible.value = true
  currentOrderDetail.value = null
  try {
    const serviceType = resolveOrderServiceType(order)
    const basePath = getAppointmentApiBase(serviceType)
    const res = await api.get(`${basePath}/${order.id}`)
    if (isSuccess(res) && res.data) {
      currentOrderDetail.value = transformAppointmentRecord(res.data)
    } else {
      currentOrderDetail.value = transformAppointmentRecord(order)
      ElMessage.error(resolveMessage(res, '获取预约详情失败'))
    }
  } catch (error) {
    console.error('获取预约详情失败:', error)
    currentOrderDetail.value = transformAppointmentRecord(order)
    ElMessage.error('获取预约详情失败')
  }
}

const updateAppointmentStatus = async (order, status, successMessage, rejectReason = null) => {
  try {
    const payload = {
      id: order.id,
      serviceType: resolveOrderServiceType(order),
      status,
    }
    if (rejectReason != null && String(rejectReason).trim() !== '') {
      payload.rejectReason = String(rejectReason).trim()
    }
    const statusApi = isStaff.value ? '/api/admin/self/work-orders/status' : '/api/admin/work-orders/status'
    const res = await api.post(statusApi, payload)
    if (isSuccess(res)) {
      ElMessage.success(successMessage)
      if (!isStaff.value) {
        loadServiceOrders()
      }
      if (isStaff.value) {
        loadMyOrders()
      }
    } else {
      ElMessage.error(resolveMessage(res))
    }
  } catch (error) {
    console.error('更新预约状态失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const confirmOrder = (order) => {
  updateAppointmentStatus(order, 'confirmed', `订单 ${order.id} 已确认`)
}

const rejectOrder = async (order) => {
  try {
    const { value } = await ElMessageBox.prompt('请填写拒绝原因，用户将看到该说明', '填写拒绝原因', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入拒绝原因（必填）',
      inputValidator: (val) => (val != null && String(val).trim() !== '' ? true : '请填写拒绝原因'),
      inputErrorMessage: '请填写拒绝原因',
    })
    const reason = value != null ? String(value).trim() : ''
    await updateAppointmentStatus(order, 'cancelled', `订单 ${order.id} 已拒绝`, reason)
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e?.message || '操作失败')
    }
  }
}

const completeOrder = (order) => {
  updateAppointmentStatus(order, 'completed', `订单 ${order.id} 已完成`)
}

const resolveOrderServiceType = (order) => {
  const raw = String(
    order?.serviceType || order?.service_type || SERVICE_TYPE
  ).toLowerCase()
  if (raw.includes('groom') || raw.includes('洗护')) return 'grooming'
  if (raw.includes('hospital') || raw.includes('医院') || raw.includes('medical')) return 'hospital'
  return 'door-cleaning'
}

const getAppointmentApiBase = (serviceType) => {
  if (serviceType === 'grooming') return '/api/grooming-appointments'
  if (serviceType === 'hospital') return '/api/hospital-appointments'
  return '/api/door-cleaning'
}

/**
 * 我的工单
 */
const myMemberId = ref(null)
const myMemberName = ref('')
/** 当前匹配到的服务人员业务类型，用于拉取「我的订单」对应接口 */
const myMemberServiceType = ref('door-cleaning')
const myOrders = ref([])
const myOrdersLoading = ref(false)
const selectedMemberId = ref(null)

const normalizeMemberServiceType = (t) => {
  if (!t) return 'door-cleaning'
  const s = String(t).toLowerCase()
  if (s === 'litter' || s === 'door-cleaning') return 'door-cleaning'
  if (s === 'grooming') return 'grooming'
  if (s === 'hospital') return 'hospital'
  return s
}

const getMyOrdersListUrl = (serviceType) => {
  const st = normalizeMemberServiceType(serviceType)
  if (st === 'grooming') return '/api/grooming-appointments/member/list'
  if (st === 'hospital') return '/api/hospital-appointments/member/list'
  return '/api/door-cleaning/member/list'
}

// 手动选择服务人员
const handleMemberSelect = (memberId) => {
  if (memberId) {
    const member = serviceMembers.value.find(m => m.id === memberId)
    if (member) {
      myMemberId.value = member.id
      myMemberName.value = member.memberName || member.username || ''
      myMemberServiceType.value = normalizeMemberServiceType(member.serviceType)
      console.log('手动选择服务人员:', member)
      loadMyOrders()
    }
  }
}

const loadMyServiceMember = async () => {
  const userId = currentUserId.value
  const username = currentUsername.value
  const userDepartment = store.getters['user/department'] || ''
  
  console.log('加载服务人员信息，userId:', userId, 'username:', username, 'department:', userDepartment)
  
  if (!username) {
    console.warn('无法获取用户名')
    return
  }
  
  try {
    let member = null
    
    // 方法1: 通过 userId 查找（如果存在）
    if (userId) {
      try {
        const res = await api.get(`/api/service-member/by-user/${userId}`)
        if (isSuccess(res) && res.data) {
          member = res.data
          console.log('通过userId找到服务人员:', member)
        }
      } catch (e) {
        console.warn('通过userId查找失败:', e)
      }
    }
    
    // 方法2: 通过 username 在列表中查找（按门店缩小范围；多服务类型依次尝试）
    if (!member) {
      try {
        const serviceStoreId = store.getters['user/serviceStoreId'] || null
        const tryTypes = ['door-cleaning', 'grooming', 'hospital']
        for (const st of tryTypes) {
          const params = { serviceType: st }
          if (serviceStoreId != null) params.storeId = serviceStoreId
          const listRes = await api.get('/api/service-member/list', { params })
          if (!isSuccess(listRes) || !Array.isArray(listRes.data)) continue
          const list = listRes.data
          // 优先匹配 username
          member = list.find((item) => {
            if (item.username && item.username.toLowerCase() === username.toLowerCase()) {
              return true
            }
            return false
          }) || null
          
          // 如果 username 不匹配，尝试匹配 member_name
          if (!member) {
            member = list.find((item) => {
              if (item.memberName && item.memberName.toLowerCase() === username.toLowerCase()) {
                return true
              }
              return false
            }) || null
          }
          
          // 如果还是没匹配到，且部门匹配，且只有一个服务人员，自动关联
          if (!member && userDepartment && (userDepartment.includes('铲屎') || userDepartment.includes('上门'))) {
            if (list.length === 1) {
              console.log('管理员账号，部门匹配，自动关联唯一服务人员')
              member = list[0]
            } else if (list.length > 0) {
              // 如果有多个，尝试通过 name 匹配（admin 表的 name 字段）
              const adminName = store.getters['user/userInfo']?.name || store.getters['user/name'] || ''
              if (adminName) {
                member = list.find((item) => {
                  return item.memberName && item.memberName === adminName
                }) || null
                if (member) {
                  console.log('通过admin表的name字段匹配到服务人员:', member)
                }
              }
            }
          }
          
          if (member) {
            console.log('通过列表匹配到服务人员:', member, 'serviceType:', st)
            break
          }
        }
        if (!member) {
          console.log('未找到匹配的服务人员（已尝试 door-cleaning / grooming / hospital）', { username, userId, serviceStoreId })
        }
      } catch (e) {
        console.warn('通过列表查找失败:', e)
      }
    }
    
    if (member) {
      myMemberId.value = member.id
      myMemberName.value = member.memberName || member.username || ''
      myMemberServiceType.value = normalizeMemberServiceType(member.serviceType)
      console.log('成功匹配服务人员，ID:', myMemberId.value, '名称:', myMemberName.value, '业务类型:', myMemberServiceType.value)
    } else {
      console.warn('未找到匹配的服务人员记录，userId:', userId, 'username:', username, 'department:', userDepartment)
      myMemberId.value = null
      myMemberName.value = ''
      myMemberServiceType.value = 'door-cleaning'
    }
  } catch (error) {
    console.error('获取上门铲屎服务人员信息失败:', error)
    myMemberId.value = null
    myMemberName.value = ''
    myMemberServiceType.value = 'door-cleaning'
  }
}

const loadMyOrders = async () => {
  if (!myMemberId.value) {
    myOrders.value = []
    return
  }
  myOrdersLoading.value = true
  try {
    const listUrl = getMyOrdersListUrl(myMemberServiceType.value)
    const res = await api.get(listUrl, {
      params: { memberId: myMemberId.value },
    })
    if (isSuccess(res) && Array.isArray(res.data)) {
      myOrders.value = res.data.map(transformAppointmentRecord)
    } else {
      myOrders.value = []
    }
  } catch (error) {
    console.error('加载我的工单失败:', error)
    myOrders.value = []
  } finally {
    myOrdersLoading.value = false
  }
}

/**
 * 统计信息
 */
const statisticsDialogVisible = ref(false)
const statistics = reactive({
  totalServices: 0,
  activeServices: 0,
  todaySchedule: 0,
  monthSchedule: 0,
})

const showStatisticsDialog = async () => {
  statistics.totalServices = services.value.length
  statistics.activeServices = services.value.filter((s) => s.status === 'active').length
  statistics.todaySchedule = scheduleCalendar[selectedDate.value] || 0
  statistics.monthSchedule = Object.values(scheduleCalendar).reduce((sum, val) => sum + Number(val || 0), 0)
  statisticsDialogVisible.value = true
}

/**
 * 工具函数
 */
const normalizeArray = (value) => {
  if (Array.isArray(value)) {
    return value.filter((item) => item && String(item).trim()).map((item) => String(item).trim())
  }
  if (!value) return []
  if (typeof value === 'string') {
    return value
      .split(/\r?\n/)
      .map((item) => item.trim())
      .filter(Boolean)
  }
  return []
}

const transformAppointmentRecord = (record, fallbackServiceType = SERVICE_TYPE) => {
  if (!record) return null
  const date = record.date || record.startDate || record.appointmentDate || ''
  return {
    id: record.id,
    storeId: record.storeId,
    storeName: record.storeName || null,
    customer: record.contactName || record.customer || '-',
    phone: record.contactPhone || record.phone || '-',
    petId: record.petId ?? null,
    petName: record.petName || '-',
    serviceId: record.serviceId ?? null,
    serviceName: record.serviceName || '',
    serviceType: record.serviceType || fallbackServiceType || SERVICE_TYPE,
    date,
    timeSlot: record.timeSlot || '-',
    status: record.status || 'pending',
    location: record.location || '-',
    totalAmount: record.price || record.orderAmount || 0,
    orderAmount: record.price || record.orderAmount || 0,
    memberName: record.memberName || '',
    memberPhone: record.memberPhone || '',
    verifyCode: record.verifyCode || '',
    createTime: record.createTime,
    updateTime: record.updateTime,
    remark: record.remark || '',
  }
}

const getOrderStatusText = (status) => {
  const map = {
    pending: '待确认',
    confirmed: '已确认',
    cancel_pending: '取消待确认',
    change_pending: '变更待确认',
    completed: '已完成',
    cancelled: '已取消',
    no_show: '已失约',
    已分配: '已确认',
  }
  return map[status] || status || '-'
}

const getOrderStatusType = (status) => {
  const map = {
    pending: 'warning',
    confirmed: 'primary',
    cancel_pending: 'warning',
    change_pending: 'warning',
    completed: 'success',
    cancelled: 'info',
    no_show: 'danger',
    已分配: 'primary',
  }
  return map[status] || 'info'
}

const formatAmount = (value) => {
  const num = Number(value || 0)
  return Number.isNaN(num) ? '0.00' : num.toFixed(2)
}

const formatDateOnly = (value) => {
  if (value == null || value === '') return '-'
  // 后端可能返回 [year, month, day]（month 1-12），dayjs 数组构造月份是 0 起算，会错一个月
  if (Array.isArray(value) && value.length >= 3) {
    const [y, m, d] = value
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }
  // 字符串：取前 10 位避免带时区的 ISO 串在本地解析时日期漂移
  if (typeof value === 'string' && value.length >= 10) {
    return value.slice(0, 10)
  }
  return dayjs(value).format('YYYY-MM-DD')
}

const getImageBase = () => {
  const u = (baseURL || '').trim().replace(/\/api\/?$/, '')
  if (u && (u.startsWith('http://') || u.startsWith('https://'))) return u
  return 'http://localhost:8080'
}
/** 获取服务自己的封面图（后端上传的 imageUrl/image_url），不用默认图 */
const getServiceImageUrl = (row) => {
  if (!row) return ''
  const u = row.imageUrl || row.image_url || ''
  return typeof u === 'string' ? u.trim() : ''
}
const getImageUrl = (url) => {
  if (!url || typeof url !== 'string') return ''
  const raw = url.trim()
  if (!raw) return ''
  const base = getImageBase()
  if (raw.startsWith('http://') || raw.startsWith('https://')) {
    try {
      const pathStart = raw.indexOf('/', raw.indexOf('://') + 3)
      if (pathStart > 0) return base + raw.substring(pathStart)
    } catch (e) {}
    return raw
  }
  if (raw.startsWith('/')) return base + raw
  return base + (raw.startsWith('upload') ? '/' + raw : '/upload/' + raw.replace(/^\/+/, ''))
}

/**
 * 生命周期
 */
onMounted(async () => {
  syncServiceTypeNav()
  await loadServices()
  await loadServiceBanner()
  await loadTimeSlots()
  await loadServiceMembers()
  if (isSuperAdmin.value) await loadScheduleStoreOptions()
  await loadScheduleCalendar()
  await viewDaySchedule(selectedDate.value)
  if (!isStaff.value) {
    await loadServiceOrders()
  }
  if (isStaff.value) {
    activeTab.value = 'myOrders'
    await loadMyServiceMember()
    await loadMyOrders()
  }
})

watch(
  () => route.path,
  () => {
    syncServiceTypeNav()
    if (showStaffManagementTabs.value && route.path.includes('/service-staff/schedule')) {
      activeTab.value = 'schedule'
    } else if (showStaffManagementTabs.value && route.path.includes('/service-staff/time-slots')) {
      activeTab.value = 'timeSlots'
    } else if (showOrderTabs.value && isStaff.value) {
      activeTab.value = 'myOrders'
    } else if (showOrderTabs.value) {
      activeTab.value = 'orders'
    } else if (!showStaffManagementTabs.value && (activeTab.value === 'schedule' || activeTab.value === 'timeSlots')) {
      activeTab.value = 'services'
    } else if (!showOrderTabs.value && activeTab.value === 'orders') {
      activeTab.value = 'services'
    }
  },
  { immediate: true },
)

watch(
  () => isStaff.value,
  (val) => {
    if (val) {
      loadMyServiceMember()
      loadMyOrders()
    } else if (showOrderTabs.value) {
      loadServiceOrders()
    }
  },
)

watch(batchScheduleDialogVisible, (visible) => {
  if (visible) loadServiceMembers()
})

// 切换日历月份时重新加载该月的排班数据
watch(calendarDisplayDate, (newVal) => {
  const month = newVal ? dayjs(newVal).format('YYYY-MM') : ''
  if (month && month !== lastLoadedCalendarMonth.value) {
    lastLoadedCalendarMonth.value = month
    loadScheduleCalendar()
  }
}, { flush: 'post' })

// 监听标签页切换，当切换到订单/排班标签页时重新加载数据
const handleTabChange = (tabName) => {
  if (tabName === 'orders') {
    console.log('切换到上门铲屎预约订单标签页，重新加载数据')
    loadServiceOrders()
  } else if (tabName === 'myOrders' && isStaff.value) {
    console.log('切换到我的工单标签页，重新加载数据')
    loadMyOrders()
  } else if (tabName === 'schedule' && isSuperAdmin.value) {
    loadScheduleStoreOptions().then(() => {
      loadScheduleCalendar()
      viewDaySchedule(selectedDate.value)
    })
  }
}
</script>

<style scoped>
.review-modal-body p { margin: 8px 0; }
.review-modal-body .tip-inline { font-size: 12px; color: #909399; margin-top: 12px; }
.order-actions-inline {
  display: inline-flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.door-cleaning-service-container {
  padding: 16px;
}

.main-card {
  min-height: 720px;
}

.service-type-nav {
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.header-input {
  width: 220px;
}

.header-select {
  width: 150px;
}

.inner-tabs {
  margin-top: 12px;
}

.inner-tabs :deep(.el-tabs__header) {
  display: none;
}

.inner-tabs :deep(.el-tabs__content) {
  margin-top: 0;
}

.description-cell {
  max-height: 52px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ellipsis {
  display: inline-block;
  max-width: 260px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.pet-detail-link {
  margin-left: 8px;
  padding: 0;
}

.price-tag {
  color: #ff6b35;
  font-weight: 600;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.banner-section {
  margin-top: 32px;
}

.banner-section h4 {
  margin-bottom: 6px;
}

.banner-desc {
  margin: 0 0 12px;
  color: #909399;
  font-size: 13px;
}

.banner-preview {
  display: flex;
  align-items: center;
  gap: 16px;
}

.banner-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-panel {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
}

.schedule-toolbar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  grid-row: 1 / -1;
}

.schedule-toolbar-actions {
  display: flex;
  gap: 12px;
}

.calendar-cell {
  position: relative;
  width: 100%;
  height: 100%;
  padding: 6px;
  cursor: pointer;
}

.calendar-cell.active {
  background-color: rgba(64, 158, 255, 0.15);
  border-radius: 6px;
}

.schedule-table-wrapper {
  margin-top: 16px;
  grid-column: 2;
  grid-row: 2;
  min-width: 0;
  width: 100%;
}

.schedule-table-wrapper :deep(.el-table) {
  width: 100% !important;
}

.schedule-header-text {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-left: 4px;
  width: 100%;
  box-sizing: border-box;
}

.schedule-header-text h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.schedule-header-text .extra {
  color: #909399;
  font-size: 13px;
}

.time-slot-panel,
.orders-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.time-slot-toolbar,
.orders-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.orders-input {
  width: 240px;
}

.orders-select {
  width: 140px;
}

.upload-block {
  width: 100%;
}

.upload-icon {
  font-size: 40px;
  color: #409eff;
}

.upload-text {
  margin-top: 12px;
  color: #606266;
}

.dialog-footer {
  text-align: right;
}
</style>

