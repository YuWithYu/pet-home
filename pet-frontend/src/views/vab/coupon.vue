<template>
  <div class="coupon-container">
    <!-- 查询与操作栏 -->
    <div class="table-toolbar">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="草稿" value="draft" />
            <el-option label="已上架" value="active" />
            <el-option label="已下架" value="offline" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="券名称" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
      <el-button type="primary" icon="el-icon-plus" @click="openCreateDialog">新建优惠券</el-button>
    </div>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="couponList" border stripe>
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="name" label="券名称" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 'discount' ? 'warning' : 'danger'" size="small">
            {{ row.type === 'discount' ? '折扣券' : '满减券' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="优惠规则" min-width="150">
        <template #default="{ row }">
          <span v-if="row.type === 'discount'">
            {{ (row.discountRate * 10).toFixed(1).replace(/\.0$/, '') }}折
            <span v-if="row.maxDiscountAmount" class="sub-text">（最高减{{ row.maxDiscountAmount }}元）</span>
          </span>
          <span v-else>减{{ row.discountAmount }}元</span>
          <div class="sub-text">
            {{ Number(row.thresholdAmount) > 0 ? `满${row.thresholdAmount}元可用` : '无门槛' }}
            · {{ scopeText(row) }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="领取/总量" width="110" align="center">
        <template #default="{ row }">
          {{ row.receivedCount }} / {{ row.totalCount === 0 ? '不限' : row.totalCount }}
        </template>
      </el-table-column>
      <el-table-column prop="perUserLimit" label="限领" width="70" align="center" />
      <el-table-column prop="validDays" label="有效期" width="90" align="center">
        <template #default="{ row }">{{ row.validDays }}天</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'draft' || row.status === 'offline'"
            type="success"
            size="small"
            @click="handleChangeStatus(row, 'active')">上架</el-button>
          <el-button
            v-if="row.status === 'active'"
            type="warning"
            size="small"
            @click="handleChangeStatus(row, 'offline')">下架</el-button>
          <el-button link size="small" @click="handleViewStats(row)">统计</el-button>
          <el-button link size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button link size="small" class="danger-btn" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        :page-size="queryForm.size"
        :current-page="queryForm.page"
        @current-change="handlePageChange" />
    </div>

    <!-- 新建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑优惠券' : '新建优惠券'"
      width="560px"
      destroy-on-close>
      <el-form ref="couponFormRef" :model="couponForm" :rules="formRules" label-width="110px">
        <el-form-item label="券名称" prop="name">
          <el-input v-model="couponForm.name" maxlength="50" show-word-limit placeholder="如：新客专享满减券" />
        </el-form-item>
        <el-form-item label="券类型" prop="type">
          <el-radio-group v-model="couponForm.type" :disabled="isEdit && hasReceived">
            <el-radio label="full_reduction">满减券（满X减Y）</el-radio>
            <el-radio label="discount">折扣券（打折）</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 已有人领取：锁定核心权益字段，避免存量券与新领券权益不一致引发客诉 -->
        <el-alert
          v-if="isEdit && hasReceived"
          title="该券已有用户领取，使用门槛 / 优惠金额 / 折扣率 / 有效天数不可修改；如需调整请下架后新建"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 18px" />
        <el-form-item label="使用门槛" prop="thresholdAmount">
          <el-input-number v-model="couponForm.thresholdAmount" :min="0" :precision="2" :step="10" :disabled="isEdit && hasReceived" />
          <span class="form-tip">元（0 = 无门槛）</span>
        </el-form-item>
        <template v-if="couponForm.type === 'full_reduction'">
          <el-form-item label="满减金额" prop="discountAmount">
            <el-input-number v-model="couponForm.discountAmount" :min="0.01" :precision="2" :step="5" :disabled="isEdit && hasReceived" />
            <span class="form-tip">元</span>
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="折扣率" prop="discountRate">
            <el-input-number v-model="couponForm.discountRate" :min="0.01" :max="0.99" :precision="2" :step="0.05" :disabled="isEdit && hasReceived" />
            <span class="form-tip">（0.85 = 85折）</span>
          </el-form-item>
          <el-form-item label="最高优惠">
            <el-input-number v-model="couponForm.maxDiscountAmount" :min="0" :precision="2" :step="10" :disabled="isEdit && hasReceived" />
            <span class="form-tip">元（0 或空 = 不封顶）</span>
          </el-form-item>
        </template>
        <el-form-item label="适用范围" prop="scope">
          <el-select v-model="couponForm.scope" style="width: 150px" @change="onScopeChange">
            <el-option label="全场通用" value="all" />
            <el-option label="指定商品" value="product" />
            <el-option label="指定分类" value="category" />
          </el-select>
          <el-input
            v-if="couponForm.scope !== 'all'"
            v-model="couponForm.scopeValue"
            :placeholder="couponForm.scope === 'product' ? '商品ID，多个用英文逗号分隔' : '分类名称，如：猫粮'"
            style="width: 260px; margin-left: 8px" />
        </el-form-item>
        <el-form-item label="发行总量" prop="totalCount">
          <el-input-number v-model="couponForm.totalCount" :min="0" :step="100" />
          <span class="form-tip">张（0 = 不限量）</span>
        </el-form-item>
        <el-form-item label="每人限领" prop="perUserLimit">
          <el-input-number v-model="couponForm.perUserLimit" :min="1" :max="10" />
          <span class="form-tip">张</span>
        </el-form-item>
        <el-form-item label="领取窗口">
          <el-date-picker
            v-model="receiveWindow"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间（可空）"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效天数" prop="validDays">
          <el-input-number v-model="couponForm.validDays" :min="1" :max="365" :disabled="isEdit && hasReceived" />
          <span class="form-tip">天（领取后开始计算）</span>
        </el-form-item>
        <el-form-item label="使用说明">
          <el-input v-model="couponForm.description" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 统计弹窗 -->
    <el-dialog v-model="statsVisible" title="发放统计" width="420px">
      <div v-if="statsData" class="stats-grid">
        <div class="stats-item">
          <div class="stats-value">{{ statsData.totalCount === 0 ? '不限' : statsData.totalCount }}</div>
          <div class="stats-label">发行总量</div>
        </div>
        <div class="stats-item">
          <div class="stats-value">{{ statsData.receivedCount }}</div>
          <div class="stats-label">已领取</div>
        </div>
        <div class="stats-item">
          <div class="stats-value">{{ statsData.usedCount }}</div>
          <div class="stats-label">已核销</div>
        </div>
        <div class="stats-item">
          <div class="stats-value">{{ statsData.unusedCount }}</div>
          <div class="stats-label">未使用</div>
        </div>
        <div class="stats-item">
          <div class="stats-value">{{ statsData.expiredCount }}</div>
          <div class="stats-label">已过期</div>
        </div>
        <div class="stats-item">
          <!-- 后端不限量时 remaining 为 -1，展示"不限"而不是 -1 -->
          <div class="stats-value">{{ Number(statsData.remaining) === -1 || statsData.remaining === '不限' ? '不限' : statsData.remaining }}</div>
          <div class="stats-label">剩余可领</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="statsVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { couponApi } from "@/api/coupon";

export default {
  name: "CouponManagement",
  data() {
    return {
      loading: false,
      saving: false,
      couponList: [],
      total: 0,
      queryForm: {
        page: 1,
        size: 10,
        status: "",
        keyword: "",
      },
      // 弹窗
      dialogVisible: false,
      isEdit: false,
      hasReceived: false,
      couponForm: this.emptyForm(),
      receiveWindow: [],
      // 统计
      statsVisible: false,
      statsData: null,
    };
  },
  computed: {
    formRules() {
      const rules = {
        name: [{ required: true, message: "请输入券名称", trigger: "blur" }],
        type: [{ required: true, message: "请选择券类型", trigger: "change" }],
        thresholdAmount: [{ required: true, message: "请填写使用门槛", trigger: "blur" }],
        perUserLimit: [{ required: true, message: "请填写每人限领数", trigger: "blur" }],
        validDays: [{ required: true, message: "请填写有效天数", trigger: "blur" }],
      };
      if (this.couponForm.type === "full_reduction") {
        rules.discountAmount = [
          { required: true, message: "请填写满减金额", trigger: "blur" },
          {
            // 防倒挂：满10减50 之类的券在后端会被拒绝，前端先拦住
            validator: (rule, value, callback) => {
              const threshold = Number(this.couponForm.thresholdAmount || 0);
              if (threshold > 0 && Number(value) > threshold) {
                callback(new Error("满减金额不能大于使用门槛"));
              } else {
                callback();
              }
            },
            trigger: "blur",
          },
        ];
      } else {
        rules.discountRate = [{ required: true, message: "请填写折扣率", trigger: "blur" }];
      }
      if (this.couponForm.scope !== "all") {
        rules.scopeValue = [{ required: true, message: "请填写范围值", trigger: "blur" }];
      }
      return rules;
    },
  },
  mounted() {
    this.loadData();
  },
  methods: {
    emptyForm() {
      return {
        id: null,
        name: "",
        type: "full_reduction",
        thresholdAmount: 0,
        discountAmount: 5,
        discountRate: 0.85,
        maxDiscountAmount: 0,
        totalCount: 100,
        perUserLimit: 1,
        scope: "all",
        scopeValue: null,
        receiveStartTime: null,
        receiveEndTime: null,
        validDays: 7,
        description: "",
      };
    },
    async loadData() {
      this.loading = true;
      try {
        const params = {
          page: this.queryForm.page,
          size: this.queryForm.size,
        };
        if (this.queryForm.status) params.status = this.queryForm.status;
        if (this.queryForm.keyword) params.keyword = this.queryForm.keyword;
        const res = await couponApi.getCouponPage(params);
        const data = res.data || res;
        this.couponList = data.records || [];
        this.total = Number(data.total || 0);
      } catch (e) {
        this.$message.error(e.message || "加载优惠券列表失败");
      } finally {
        this.loading = false;
      }
    },
    handleSearch() {
      this.queryForm.page = 1;
      this.loadData();
    },
    handlePageChange(page) {
      this.queryForm.page = page;
      this.loadData();
    },
    // 切换适用范围时清空范围值，避免把商品ID当分类名提交（或反之）
    onScopeChange() {
      this.couponForm.scopeValue = null;
      this.$nextTick(() => {
        if (this.$refs.couponFormRef) this.$refs.couponFormRef.clearValidate("scopeValue");
      });
    },
    openCreateDialog() {
      this.isEdit = false;
      this.hasReceived = false;
      this.couponForm = this.emptyForm();
      this.receiveWindow = [];
      this.dialogVisible = true;
    },
    openEditDialog(row) {
      this.isEdit = true;
      this.hasReceived = (row.receivedCount || 0) > 0;
      this.couponForm = {
        id: row.id,
        name: row.name,
        type: row.type,
        thresholdAmount: Number(row.thresholdAmount || 0),
        discountAmount: row.discountAmount ? Number(row.discountAmount) : 5,
        discountRate: row.discountRate ? Number(row.discountRate) : 0.85,
        maxDiscountAmount: row.maxDiscountAmount ? Number(row.maxDiscountAmount) : 0,
        totalCount: row.totalCount || 0,
        perUserLimit: row.perUserLimit || 1,
        scope: row.scope || "all",
        scopeValue: row.scopeValue,
        receiveStartTime: row.receiveStartTime,
        receiveEndTime: row.receiveEndTime,
        validDays: row.validDays,
        description: row.description,
      };
      this.receiveWindow = row.receiveStartTime || row.receiveEndTime
        ? [row.receiveStartTime || "", row.receiveEndTime || ""]
        : [];
      this.dialogVisible = true;
    },
    async handleSave() {
      try {
        await this.$refs.couponFormRef.validate();
      } catch (e) {
        return;
      }
      // 领取窗口校验：开始时间必须早于结束时间（结束时间为空表示长期有效）
      if (
        Array.isArray(this.receiveWindow) &&
        this.receiveWindow.length === 2 &&
        this.receiveWindow[0] &&
        this.receiveWindow[1]
      ) {
        if (new Date(this.receiveWindow[0]).getTime() >= new Date(this.receiveWindow[1]).getTime()) {
          this.$message.error("领取窗口的开始时间必须早于结束时间");
          return;
        }
      }
      this.saving = true;
      try {
        const payload = { ...this.couponForm };
        // 领取窗口
        if (this.receiveWindow && this.receiveWindow.length === 2) {
          payload.receiveStartTime = this.receiveWindow[0] || null;
          payload.receiveEndTime = this.receiveWindow[1] || null;
        } else {
          payload.receiveStartTime = null;
          payload.receiveEndTime = null;
        }
        // 清理与类型无关的字段，避免脏数据
        if (payload.type === "full_reduction") {
          payload.discountRate = null;
          payload.maxDiscountAmount = null;
        } else {
          payload.discountAmount = null;
        }
        if (payload.scope === "all") {
          payload.scopeValue = null;
        }
        // 不封顶时传 null
        if (payload.maxDiscountAmount === 0) {
          payload.maxDiscountAmount = null;
        }
        if (this.isEdit) {
          await couponApi.updateCoupon(payload);
          this.$message.success("更新成功");
        } else {
          await couponApi.createCoupon(payload);
          this.$message.success("创建成功（草稿态，请上架后发放）");
        }
        this.dialogVisible = false;
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "保存失败");
      } finally {
        this.saving = false;
      }
    },
    async handleChangeStatus(row, status) {
      try {
        await this.$confirm(
          status === "active" ? `确认上架「${row.name}」？上架后用户可在领券中心领取。` : `确认下架「${row.name}」？下架后不可再领取，已领的券仍可使用。`,
          "提示",
          { type: "warning" }
        );
      } catch (e) {
        return;
      }
      try {
        await couponApi.changeCouponStatus(row.id, status);
        this.$message.success(status === "active" ? "已上架" : "已下架");
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "操作失败");
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm(`确认删除「${row.name}」？仅未发放过的券可删除。`, "警告", { type: "warning" });
      } catch (e) {
        return;
      }
      try {
        await couponApi.deleteCoupon(row.id);
        this.$message.success("已删除");
        // 末页钳制：删除的是当前页最后一条且不在第一页时，回退一页
        if (this.couponList.length === 1 && this.queryForm.page > 1) {
          this.queryForm.page -= 1;
        }
        this.loadData();
      } catch (e) {
        this.$message.error(e.message || "删除失败");
      }
    },
    async handleViewStats(row) {
      try {
        const res = await couponApi.getCouponStats(row.id);
        this.statsData = res.data || res;
        this.statsVisible = true;
      } catch (e) {
        this.$message.error(e.message || "获取统计失败");
      }
    },
    statusText(status) {
      return { draft: "草稿", active: "已上架", offline: "已下架" }[status] || status;
    },
    statusTagType(status) {
      return { draft: "info", active: "success", offline: "warning" }[status] || "info";
    },
    scopeText(row) {
      if (row.scope === "product") return `指定商品(${row.scopeValue})`;
      if (row.scope === "category") return `指定分类(${row.scopeValue})`;
      return "全场通用";
    },
  },
};
</script>

<style lang="scss" scoped>
.coupon-container {
  padding: 20px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;

  .query-form {
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

.danger-btn {
  color: #f56c6c;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  .stats-item {
    text-align: center;
    padding: 16px 0;
    background: #f5f7fa;
    border-radius: 8px;

    .stats-value {
      font-size: 24px;
      font-weight: 700;
      color: #303133;
    }

    .stats-label {
      margin-top: 4px;
      font-size: 13px;
      color: #909399;
    }
  }
}
</style>
