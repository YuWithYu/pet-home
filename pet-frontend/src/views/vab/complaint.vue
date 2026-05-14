<template>
  <div class="complaint-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">投诉举报</span>
          <span class="subtitle">小程序用户提交的投诉与举报，超级管理员可查看并更新处理状态</span>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form" size="default">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已解决" value="resolved" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="内容违规" value="内容违规" />
            <el-option label="用户行为" value="用户行为" />
            <el-option label="系统问题" value="系统问题" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="90">
          <template #default="{ row }">
            {{ row.userId != null ? row.userId : '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type || '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="投诉内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="contactInfo" label="联系方式" width="130" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.contactInfo || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.status === 'resolved' ? 'success' : row.status === 'processing' ? 'warning' : 'info'"
            >
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="op-row">
              <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
              <el-dropdown trigger="click" @command="(cmd) => handleStatusChange(row, cmd)">
                <el-button type="primary" link size="small" class="op-status-btn">
                  状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="pending" :disabled="row.status === 'pending'">待处理</el-dropdown-item>
                    <el-dropdown-item command="processing" :disabled="row.status === 'processing'">处理中</el-dropdown-item>
                    <el-dropdown-item command="resolved" :disabled="row.status === 'resolved'">已解决</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :current-page="pagination.pageNo"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="投诉举报详情" width="560px" destroy-on-close>
      <el-descriptions v-if="currentRow" :column="1" border>
        <el-descriptions-item label="ID">{{ currentRow.id }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentRow.userId != null ? currentRow.userId : '未登录' }}</el-descriptions-item>
        <el-descriptions-item label="投诉类型">{{ currentRow.type || '其他' }}</el-descriptions-item>
        <el-descriptions-item label="投诉内容">
          <div class="content-block">{{ currentRow.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ currentRow.contactInfo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="投诉图片" v-if="getDetailImages(currentRow).length">
          <div class="detail-images">
            <el-image
              v-for="(url, idx) in getDetailImages(currentRow)"
              :key="idx"
              :src="getImageUrl(url)"
              :preview-src-list="getDetailImages(currentRow).map(u => getImageUrl(u))"
              fit="cover"
              class="detail-img"
            >
              <template #error>
                <div class="detail-img-error">
                  <span>图片加载失败</span>
                  <span class="detail-img-url">{{ getImageUrl(url) }}</span>
                </div>
              </template>
            </el-image>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="currentRow.status === 'resolved' ? 'success' : currentRow.status === 'processing' ? 'warning' : 'info'">
            {{ formatStatus(currentRow.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ formatDateTime(currentRow.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDateTime(currentRow.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { ArrowDown } from "@element-plus/icons-vue";
import { complaintApi } from "@/api/complaint";
import { baseURL } from "@/config";

export default {
  name: "ComplaintManagement",
  components: { ArrowDown },
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: {
        status: "",
        type: "",
      },
      pagination: {
        pageNo: 1,
        pageSize: 10,
      },
      detailVisible: false,
      currentRow: null,
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    parseBackendError(err) {
      if (!err) return "";
      if (typeof err === "string" && err.length > 0) {
        const match = err.match(/"msg"\s*:\s*"([^"]*)"/);
        if (match && match[1]) return match[1];
        if (err.includes("Network Error") || err.includes("network")) return "网络异常，请检查后端服务是否启动";
        if (err.includes("timeout")) return "请求超时";
        if (err.includes("404")) return "接口不存在，请确认后端已部署投诉举报接口";
        if (err.includes("500")) return "服务器错误，请检查后端日志（如投诉表是否已创建）";
      }
      return err.message || "";
    },
    formatStatus(status) {
      const map = { pending: "待处理", processing: "处理中", resolved: "已解决" };
      return map[status] || status || "—";
    },
    formatDateTime(val) {
      if (!val) return "—";
      const d = new Date(val);
      if (isNaN(d.getTime())) return "—";
      return d.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
      });
    },
    loadData() {
      this.loading = true;
      const params = {
        pageNo: this.pagination.pageNo,
        pageSize: this.pagination.pageSize,
        ...(this.query.status && { status: this.query.status }),
        ...(this.query.type && { type: this.query.type }),
      };
      complaintApi
        .getPage(params)
        .then((res) => {
          const data = res && res.data;
          const records = data && (data.records || data.list);
          const total = data && (data.total != null ? data.total : (Array.isArray(records) ? records.length : 0));
          if (Array.isArray(records)) {
            this.list = records;
            this.total = Number(total) || 0;
          } else {
            this.list = [];
            this.total = 0;
          }
        })
        .catch((err) => {
          const msg = this.parseBackendError(err);
          this.$message.error(msg || "加载投诉列表失败");
          this.list = [];
          this.total = 0;
        })
        .finally(() => {
          this.loading = false;
        });
    },
    resetQuery() {
      this.query.status = "";
      this.query.type = "";
      this.pagination.pageNo = 1;
      this.loadData();
    },
    handlePageChange(page) {
      this.pagination.pageNo = page;
      this.loadData();
    },
    handleSizeChange(size) {
      this.pagination.pageSize = size;
      this.pagination.pageNo = 1;
      this.loadData();
    },
    getDetailImages(row) {
      if (!row || !row.images) return [];
      try {
        const arr = typeof row.images === 'string' ? JSON.parse(row.images) : row.images;
        return Array.isArray(arr) ? arr : [];
      } catch (e) {
        return [];
      }
    },
    getImageUrl(url) {
      if (!url || typeof url !== 'string') return '';
      const urlTrim = url.trim();
      if (!urlTrim) return '';
      let base = (baseURL || '').replace(/\/api\/?$/, '') || 'http://localhost:8080';
      if (!base || base.startsWith(':')) base = 'http://localhost:8080';
      if (/^:\d+/.test(base)) base = 'http://localhost' + base;
      if (!/^https?:\/\//.test(base)) base = 'http://localhost:8080';
      if (base.startsWith('https://') && (base.includes('localhost') || base.includes('127.0.0.1'))) {
        base = base.replace(/^https:\/\//, 'http://');
      }
      // 相对路径：拼接后端 base
      if (!/^https?:\/\//i.test(urlTrim)) {
        return base + (urlTrim.startsWith('/') ? urlTrim : '/' + urlTrim);
      }
      // 完整 URL：若为 localhost/127.0.0.1，改为当前配置的 base，避免管理员在别处打开时图片加载失败
      try {
        const u = new URL(urlTrim);
        if (u.hostname === 'localhost' || u.hostname === '127.0.0.1') {
          const baseOrigin = new URL(base).origin;
          return urlTrim.replace(u.origin, baseOrigin);
        }
      } catch (e) {
        // URL 解析失败则原样返回
      }
      return urlTrim;
    },
    handleView(row) {
      this.currentRow = { ...row };
      this.detailVisible = true;
    },
    handleStatusChange(row, status) {
      if (row.status === status) return;
      this.$confirm(`确认为「${this.formatStatus(status)}」？`, "更新状态", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      })
        .then(() => {
          return complaintApi.updateStatus(row.id, status);
        })
        .then((res) => {
          if (res && (res.code === 200 || res.code === 0)) {
            this.$message.success("状态已更新");
            this.loadData();
            if (this.currentRow && this.currentRow.id === row.id) {
              this.currentRow.status = status;
            }
          } else {
            this.$message.error(res?.msg || "更新失败");
          }
        })
        .catch((err) => {
          if (err !== "cancel") {
            this.$message.error(err?.message || "更新失败");
          }
        });
    },
  },
};
</script>

<style lang="scss" scoped>
.complaint-container {
  padding: 0;
}
.card-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
  .title {
    font-size: 16px;
    font-weight: 600;
  }
  .subtitle {
    font-size: 12px;
    color: #909399;
  }
}
.filter-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.content-block {
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 200px;
  overflow-y: auto;
}
.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.detail-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  cursor: pointer;
}
.detail-img-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
  padding: 4px;
  box-sizing: border-box;
}
.detail-img-error .detail-img-url {
  margin-top: 4px;
  font-size: 10px;
  word-break: break-all;
  max-height: 2.4em;
  overflow: hidden;
}
.op-row {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 8px;
}
.op-status-btn {
  display: inline-flex;
  align-items: center;
}
</style>
