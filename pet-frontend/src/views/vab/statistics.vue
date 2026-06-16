<template>
  <div class="statistics-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>数据统计</span>
          <div class="header-actions">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 300px; margin-right: 10px"
              @change="handleDateRangeChange"
            />
            <el-button type="primary" :loading="loading" @click="refreshData">刷新数据</el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计概览：对接后端真实数据 -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #409EFF">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalUsers }}</div>
                <div class="stat-label">用户总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #67C23A">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalOrders }}</div>
                <div class="stat-label">订单总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #E6A23C">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalPets }}</div>
                <div class="stat-label">宠物总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background-color: #F56C6C">
                <el-icon><Bell /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalNotices }}</div>
                <div class="stat-label">通知总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 图表区域：使用后端返回的趋势与分布数据 -->
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card shadow="never" style="margin-bottom: 20px">
            <template #header>
              <span>用户增长趋势（近7天新增）</span>
            </template>
            <div ref="userGrowthChart" style="height: 300px"></div>
          </el-card>
          
          <el-card shadow="never">
            <template #header>
              <span>订单时段分布（今日）</span>
            </template>
            <div ref="taskCompletionChart" style="height: 300px"></div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card shadow="never" style="margin-bottom: 20px">
            <template #header>
              <span>订单状态分布</span>
            </template>
            <div ref="taskPriorityChart" style="height: 200px"></div>
          </el-card>
          
          <el-card shadow="never">
            <template #header>
              <span>用户活跃度（近7天新增 vs 总用户）</span>
            </template>
            <div ref="userActivityChart" style="height: 200px"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 详细数据：来自后端统计 -->
      <el-card shadow="never" style="margin-top: 20px">
        <template #header>
          <span>详细数据</span>
        </template>
        <el-table :data="detailData" style="width: 100%">
          <el-table-column prop="name" label="指标名称" />
          <el-table-column prop="value" label="数值" />
          <el-table-column prop="change" label="较昨日变化">
            <template #default="{ row }">
              <span :class="row.change > 0 ? 'increase' : row.change < 0 ? 'decrease' : ''">
                <el-icon v-if="row.change > 0"><Top /></el-icon>
                <el-icon v-else-if="row.change < 0"><Bottom /></el-icon>
                {{ row.change != null ? (row.change > 0 ? '+' : '') + row.change + '%' : '-' }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import * as echarts from "echarts";
import { User, Document, TrendCharts, Bell, Top, Bottom } from "@element-plus/icons-vue";
import { dashboardApi } from "@/api/dashboard.js";

export default {
  name: "Statistics",
  components: { User, Document, TrendCharts, Bell, Top, Bottom },
  data() {
    const end = new Date();
    const start = new Date();
    start.setTime(start.getTime() - 30 * 24 * 60 * 60 * 1000);
    return {
      dateRange: [this.formatDate(start), this.formatDate(end)],
      loading: false,
      rawData: null,
      statistics: {
        totalUsers: 0,
        totalOrders: 0,
        totalPets: 0,
        totalNotices: 0
      },
      detailData: [],
      userGrowthChart: null,
      taskCompletionChart: null,
      taskPriorityChart: null,
      userActivityChart: null
    };
  },
  mounted() {
    this.loadData();
  },
  beforeUnmount() {
    if (this.userGrowthChart) this.userGrowthChart.dispose();
    if (this.taskCompletionChart) this.taskCompletionChart.dispose();
    if (this.taskPriorityChart) this.taskPriorityChart.dispose();
    if (this.userActivityChart) this.userActivityChart.dispose();
  },
  methods: {
    formatDate(date) {
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    },
    handleDateRangeChange() {
      this.refreshData();
    },
    async loadData() {
      this.loading = true;
      try {
        const res = await dashboardApi.getStatistics();
        const data = res?.data;
        if ((res?.code === 0 || res?.code === 200) && data && typeof data === "object") {
          this.rawData = data;
          this.statistics = {
            totalUsers: Number(data.totalUsers) || 0,
            totalOrders: Number(data.totalOrders) || 0,
            totalPets: Number(data.totalPets) || 0,
            totalNotices: Number(data.totalNotices ?? data.noticeCount) || 0
          };
          this.buildDetailData(data);
          this.$nextTick(() => {
            this.initCharts();
          });
        } else {
          this.$message.warning("获取统计数据失败，请检查后端接口");
        }
      } catch (e) {
        console.error("加载仪表盘数据失败", e);
        this.$message.error("加载数据失败，请检查网络或后端服务");
      } finally {
        this.loading = false;
      }
    },
    buildDetailData(data) {
      const todayNewUsers = (data.userActivityTrend && data.userActivityTrend.length)
        ? (data.userActivityTrend[data.userActivityTrend.length - 1]?.count ?? 0)
        : 0;
      const yesterdayNewUsers = (data.userActivityTrend && data.userActivityTrend.length >= 2)
        ? (data.userActivityTrend[data.userActivityTrend.length - 2]?.count ?? 0)
        : 0;
      const changeUser = yesterdayNewUsers > 0
        ? Math.round(((todayNewUsers - yesterdayNewUsers) / yesterdayNewUsers) * 1000) / 10
        : (todayNewUsers > 0 ? 100 : 0);
      const todayNewOrders = Number(data.todayNewOrders) || 0;
      const yesterdayNewOrders = Number(data.yesterdayNewOrders) || 0;
      const changeOrder = yesterdayNewOrders > 0
        ? Math.round(((todayNewOrders - yesterdayNewOrders) / yesterdayNewOrders) * 1000) / 10
        : (todayNewOrders > 0 ? 100 : 0);
      this.detailData = [
        { name: "今日新增用户", value: String(todayNewUsers), change: changeUser },
        { name: "用户总数", value: String(this.statistics.totalUsers), change: null },
        { name: "今日新增订单", value: String(todayNewOrders), change: changeOrder },
        { name: "今日营收（元）", value: String(Number(data.todayRevenue) || 0), change: null },
        { name: "本月营收（元）", value: String(Number(data.monthlyRevenue) || 0), change: null },
        { name: "宠物总数", value: String(this.statistics.totalPets), change: null },
        { name: "通知总数", value: String(this.statistics.totalNotices), change: null }
      ];
    },
    refreshData() {
      this.loadData().then(() => {
        this.$message.success("数据已刷新");
      });
    },
    initCharts() {
      if (!this.$refs.userGrowthChart) return;
      [this.userGrowthChart, this.taskCompletionChart, this.taskPriorityChart, this.userActivityChart].forEach((chart) => {
        if (chart) {
          chart.dispose();
        }
      });
      this.userGrowthChart = echarts.init(this.$refs.userGrowthChart);
      this.userGrowthChart.setOption(this.getUserGrowthChartOption());
      this.taskCompletionChart = echarts.init(this.$refs.taskCompletionChart);
      this.taskCompletionChart.setOption(this.getTaskCompletionChartOption());
      this.taskPriorityChart = echarts.init(this.$refs.taskPriorityChart);
      this.taskPriorityChart.setOption(this.getTaskPriorityChartOption());
      this.userActivityChart = echarts.init(this.$refs.userActivityChart);
      this.userActivityChart.setOption(this.getUserActivityChartOption());
    },
    getUserGrowthChartOption() {
      const trend = this.rawData?.userActivityTrend || [];
      const xData = trend.map((t) => t.day || t.date || "");
      const yData = trend.map((t) => t.count ?? 0);
      return {
        tooltip: { trigger: "axis" },
        xAxis: { type: "category", data: xData },
        yAxis: { type: "value" },
        series: [{ name: "新增用户", type: "line", data: yData, smooth: true }]
      };
    },
    getTaskCompletionChartOption() {
      const hours = this.rawData?.orderHourLabels || ["0时", "4时", "8时", "12时", "16时", "20时", "24时"];
      const counts = this.rawData?.orderByHour || [];
      return {
        tooltip: { trigger: "axis" },
        xAxis: { type: "category", data: hours },
        yAxis: { type: "value" },
        series: [{ name: "订单数", type: "bar", data: counts }]
      };
    },
    getTaskPriorityChartOption() {
      const list = this.rawData?.orderStatusCounts || [];
      const data = list.map((i) => ({ name: i.name, value: i.value }));
      return {
        tooltip: { trigger: "item" },
        legend: { bottom: "0%" },
        series: [
          {
            name: "订单状态",
            type: "pie",
            radius: ["40%", "70%"],
            avoidLabelOverlap: false,
            itemStyle: { borderRadius: 10, borderColor: "#fff", borderWidth: 2 },
            label: { show: false },
            emphasis: { label: { show: true, fontSize: "14", fontWeight: "bold" } },
            labelLine: { show: false },
            data
          }
        ]
      };
    },
    getUserActivityChartOption() {
      const trend = this.rawData?.userActivityTrend || [];
      const sum7 = trend.reduce((s, t) => s + (t.count ?? 0), 0);
      const total = this.statistics.totalUsers || 1;
      const other = Math.max(0, total - sum7);
      const data = [
        { value: sum7, name: "近7天新增" },
        { value: other, name: "其他用户" }
      ].filter((d) => d.value > 0);
      if (data.length === 0) data.push({ value: 1, name: "暂无数据" });
      return {
        tooltip: { trigger: "item" },
        legend: { bottom: "0%" },
        series: [
          {
            name: "用户",
            type: "pie",
            radius: ["40%", "70%"],
            itemStyle: { borderRadius: 10, borderColor: "#fff", borderWidth: 2 },
            label: { show: false },
            emphasis: { label: { show: true, fontSize: "14", fontWeight: "bold" } },
            labelLine: { show: false },
            data
          }
        ]
      };
    }
  }
};
</script>

<style lang="scss" scoped>
.statistics-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
  }
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      
      .stat-icon {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        
        .el-icon {
          font-size: 24px;
          color: white;
        }
      }
      
      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #333;
        }
        
        .stat-label {
          font-size: 14px;
          color: #999;
        }
      }
    }
  }
  
  .increase {
    color: #67C23A;
    font-weight: bold;
  }
  
  .decrease {
    color: #F56C6C;
    font-weight: bold;
  }
}
</style>