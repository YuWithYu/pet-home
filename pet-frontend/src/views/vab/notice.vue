<template>
  <div class="notice-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleAdd">新增公告</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="filter-form" size="small">
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="标题关键词"
            clearable
            @keyup.enter.native="loadData"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部类型">
            <el-option label="系统公告" value="system" />
            <el-option label="活动公告" value="marketing" />
            <el-option label="更新公告" value="update" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部状态">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下线" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="list"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">
              {{ formatType(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              size="small"
              :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'"
            >
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.isTop === 1 ? 'success' : 'info'">
              {{ row.isTop === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="effectiveTime" label="生效时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.effectiveTime) || '立即生效' }}
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="失效时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.expireTime) || '长期有效' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              type="text"
              size="small"
              @click="togglePublish(row)"
            >
              {{ row.status === 1 ? '下线' : '发布' }}
            </el-button>
            <el-button
              type="text"
              size="small"
              style="color: #f56c6c"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total"
          :current-page="pagination.pageNo"
          :page-size="pagination.pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑公告' : '新增公告'"
      width="800px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="系统公告" value="system" />
            <el-option label="活动公告" value="marketing" />
            <el-option label="更新公告" value="update" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已下线" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
            :max="9999"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker
            v-model="form.effectiveTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="不选则立即生效"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="失效时间">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="不选则长期有效"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容（支持纯文本）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import dayjs from "dayjs";
import { ElMessage, ElMessageBox } from "element-plus";
import { noticeApi } from "@/api/notice";

export default {
  name: "NoticeManagement",
  data() {
    return {
      loading: false,
      list: [],
      total: 0,
      query: {
        keyword: "",
        type: "",
        status: null,
      },
      pagination: {
        pageNo: 1,
        pageSize: 10,
      },
      dialogVisible: false,
      editing: false,
      form: {
        id: null,
        title: "",
        content: "",
        type: "system",
        status: 1,
        isTop: 0,
        sortOrder: 0,
        effectiveTime: null,
        expireTime: null,
      },
      rules: {
        title: [
          { required: true, message: "请输入公告标题", trigger: "blur" },
          { min: 2, max: 100, message: "长度在 2 到 100 个字符", trigger: "blur" },
        ],
        type: [{ required: true, message: "请选择公告类型", trigger: "change" }],
        status: [{ required: true, message: "请选择状态", trigger: "change" }],
        content: [{ required: true, message: "请输入公告内容", trigger: "blur" }],
      },
    };
  },
  created() {
    this.loadData();
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {
        const params = {
          pageNo: this.pagination.pageNo,
          pageSize: this.pagination.pageSize,
        };
        if (this.query.keyword) params.keyword = this.query.keyword;
        if (this.query.type) params.type = this.query.type;
        if (this.query.status !== null && this.query.status !== undefined) {
          params.status = this.query.status;
        }
        const res = await noticeApi.getNoticePage(params);
        if (res.code === 0 || res.code === 200) {
          const page = res.data || {};
          this.list = page.records || [];
          this.total = page.total || 0;
        } else {
          ElMessage.error(res.msg || "加载公告失败");
        }
      } catch (e) {
        console.error("加载公告失败:", e);
        ElMessage.error("加载公告失败");
      } finally {
        this.loading = false;
      }
    },
    resetQuery() {
      this.query = {
        keyword: "",
        type: "",
        status: null,
      };
      this.pagination.pageNo = 1;
      this.loadData();
    },
    handlePageChange(page) {
      this.pagination.pageNo = page;
      this.loadData();
    },
    handleAdd() {
      this.editing = false;
      this.form = {
        id: null,
        title: "",
        content: "",
        type: "system",
        status: 1,
        isTop: 0,
        sortOrder: 0,
        effectiveTime: null,
        expireTime: null,
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.resetFields();
        }
      });
    },
    handleEdit(row) {
      this.editing = true;
      this.form = {
        id: row.id,
        title: row.title,
        content: row.content,
        type: row.type || "system",
        status: row.status,
        isTop: row.isTop || 0,
        sortOrder: row.sortOrder || 0,
        effectiveTime: row.effectiveTime
          ? dayjs(row.effectiveTime).format("YYYY-MM-DD HH:mm:ss")
          : null,
        expireTime: row.expireTime
          ? dayjs(row.expireTime).format("YYYY-MM-DD HH:mm:ss")
          : null,
      };
      this.dialogVisible = true;
      this.$nextTick(() => {
        if (this.$refs.formRef) {
          this.$refs.formRef.clearValidate();
        }
      });
    },
    async handleSave() {
      try {
        await this.$refs.formRef.validate();
        const payload = { ...this.form };
        let res;
        if (this.editing) {
          res = await noticeApi.updateNotice(payload);
        } else {
          res = await noticeApi.createNotice(payload);
        }
        if (res.code === 0 || res.code === 200) {
          ElMessage.success(this.editing ? "更新成功" : "创建成功");
          this.dialogVisible = false;
          this.loadData();
        } else {
          ElMessage.error(res.msg || "保存失败");
        }
      } catch (e) {
        if (e !== "cancel") {
          console.error("保存公告失败:", e);
          ElMessage.error("保存失败");
        }
      }
    },
    async handleDelete(row) {
      try {
        await ElMessageBox.confirm(
          `确定要删除公告「${row.title}」吗？`,
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
        );
        const res = await noticeApi.deleteNotice(row.id);
        if (res.code === 0 || res.code === 200) {
          ElMessage.success("删除成功");
          this.loadData();
        } else {
          ElMessage.error(res.msg || "删除失败");
        }
      } catch (e) {
        if (e !== "cancel") {
          console.error("删除公告失败:", e);
          ElMessage.error("删除失败");
        }
      }
    },
    async togglePublish(row) {
      const newStatus = row.status === 1 ? 2 : 1;
      const text = newStatus === 1 ? "发布" : "下线";
      try {
        const payload = {
          id: row.id,
          status: newStatus,
        };
        const res = await noticeApi.updateNotice(payload);
        if (res.code === 0 || res.code === 200) {
          ElMessage.success(`${text}成功`);
          row.status = newStatus;
        } else {
          ElMessage.error(res.msg || `${text}失败`);
        }
      } catch (e) {
        console.error(`${text}公告失败:`, e);
        ElMessage.error(`${text}失败`);
      }
    },
    formatDateTime(value) {
      if (!value) return "";
      return dayjs(value).format("YYYY-MM-DD HH:mm:ss");
    },
    formatStatus(status) {
      if (status === 1) return "已发布";
      if (status === 2) return "已下线";
      return "草稿";
    },
    formatType(type) {
      if (type === "marketing") return "活动公告";
      if (type === "update") return "更新公告";
      return "系统公告";
    },
  },
};
</script>

<style lang="scss" scoped>
.notice-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }

  .filter-form {
    margin-bottom: 10px;
  }

  .pagination {
    margin-top: 16px;
    text-align: right;
  }
}
</style>

