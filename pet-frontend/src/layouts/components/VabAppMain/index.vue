<template>
  <div v-if="routerView" class="app-main-container">
    <router-view v-slot="{ Component }">
      <transition mode="out-in" name="fade-transform">
        <keep-alive :include="cachedRoutes" :max="keepAliveMaxNum">
          <component
            v-if="$route.matched && $route.matched.length > 0"
            :is="Component"
            :key="key"
            class="app-main-height"
          />
          <div v-else class="route-error-container">
          <el-alert
            title="路由加载失败"
            type="error"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>当前路径: {{ $route.path }}</p>
              <p>请检查：</p>
              <ul>
                <li>1. 用户权限是否正确获取</li>
                <li>2. 路由是否正确注册</li>
                <li>3. 控制台是否有错误信息</li>
              </ul>
              <el-button type="primary" @click="reloadRouterView">重新加载</el-button>
            </template>
          </el-alert>
        </div>
        </keep-alive>
      </transition>
    </router-view>
    <footer v-show="footerCopyright" class="footer-copyright">
      Copyright
      <el-icon><CopyDocument /></el-icon>
      宠物家管理系统 {{ fullYear }}
    </footer>
  </div>
</template>

<script>
import { mapActions, mapGetters } from "vuex";
import { copyright, footerCopyright, keepAliveMaxNum, title } from "@/config";
import { CopyDocument } from "@element-plus/icons-vue";
import eventBus from "@/utils/eventBus";

export default {
  name: "VabAppMain",
  components: {
    CopyDocument,
  },
  mounted() {
    if (process.env.NODE_ENV === "development" && (!this.$route.matched || this.$route.matched.length === 0)) {
      console.warn("[dev] 路由未匹配:", this.$route.path);
    }
  },
  data() {
    return {
      show: false,
      fullYear: new Date().getFullYear(),
      copyright,
      title,
      keepAliveMaxNum,
      routerView: true,
      footerCopyright,
    };
  },
  computed: {
    ...mapGetters({
      visitedRoutes: "tabsBar/visitedRoutes",
      device: "settings/device",
    }),
    cachedRoutes() {
      const cachedRoutesArr = [];
      this.visitedRoutes.forEach((item) => {
        if (!item.meta.noKeepAlive) {
          cachedRoutesArr.push(item.name);
        }
      });
      return cachedRoutesArr;
    },
    key() {
      return this.$route.path;
    },
  },
  watch: {
    $route: {
      handler(route) {
        if ("mobile" === this.device) this.foldSideBar();
      },
      immediate: true,
    },
  },
  created() {
    // 监听事件总线中的reload-router-view事件
    eventBus.on("reload-router-view", this.reloadRouterView);
  },
  beforeUnmount() {
    // 组件销毁前移除事件监听
    eventBus.off("reload-router-view", this.reloadRouterView);
  },
  mounted() {},
  methods: {
    ...mapActions({
      foldSideBar: "settings/foldSideBar",
    }),
    // 重新加载路由视图
    reloadRouterView() {
      this.routerView = false;
      this.$nextTick(() => {
        this.routerView = true;
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.app-main-container {
  position: relative;
  width: 100%;
  overflow: hidden;

  .vab-keel {
    margin: $base-padding;
  }

  .app-main-height {
    min-height: $base-app-main-height;
  }

  .footer-copyright {
    min-height: 55px;
    line-height: 55px;
    color: rgba(0, 0, 0, 0.45);
    text-align: center;
    border-top: 1px dashed $base-border-color;
  }

  .route-error-container {
    padding: 20px;
    min-height: $base-app-main-height;

    ul {
      margin: 10px 0;
      padding-left: 20px;
    }

    li {
      margin: 5px 0;
    }
  }
}
</style>
