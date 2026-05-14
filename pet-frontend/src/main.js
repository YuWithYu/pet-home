import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "@/store"; // 导入Vuex store
import plugins from "./plugins";
import { printLayoutsInfo } from "@/utils/printInfo";
// 导入布局组件注册函数
import { registerLayoutComponents } from "@/layouts/export";
// 导入事件总线
import eventBus from "@/utils/eventBus";
// 导入配置
import { title } from "@/config";
// 导入mock
import { mockXHR } from "@/utils/static";
// 导入 ResizeObserver 修复工具
import "@/utils/resizeObserverFix";
// 导入 dayjs 配置
import dayjs from "@/utils/dayjs";
// 导入 dayjs 补丁
import "@/utils/dayjs-patch";
// 导入 Element Plus 修复
import "@/utils/element-plus-fix";
// 导入 Element Plus 中文语言包
import zhCn from "@/locale/element-plus-zh-cn";

// 确保 dayjs 在全局可用
window.dayjs = dayjs;

// ResizeObserver 错误处理已在 resizeObserverFix.js 中统一处理

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 生产环境默认都使用mock，如果正式用于生产环境时，记得去掉
 */

// 每次完整加载应用时清除权限同步标记，确保刷新后重新拉取 /user/current 权限
sessionStorage.removeItem("adminPermissionsResolved");

// 创建应用实例
const app = createApp(App);

// 使用Vuex
app.use(store);

app.use(router);

// 初始化所有插件
plugins(app);

// 配置 Element Plus 国际化
app.config.globalProperties.$ELEMENT = {
  locale: zhCn
};

// 注册所有布局组件
registerLayoutComponents(app);

// 添加事件总线到全局属性
app.config.globalProperties.$eventBus = eventBus;

// 添加全局标题
app.config.globalProperties.$baseTitle = title;

// 使全局属性在window上也可用
window.$eventBus = eventBus;
window.$baseTitle = title;

// 仅当明确开启 Mock 时才启用（开发环境默认走真实后端，便于管理员登录）
// 若需使用 Mock 数据，在 rspack.config.js 中将 VUE_APP_MOCK_ENABLE 设为 "true"
if (process.env.NODE_ENV !== "production" && process.env.VUE_APP_MOCK_ENABLE === "true") {
  mockXHR();
  console.log("开发环境已启用Mock拦截");
}
if (process.env.NODE_ENV === "development") {
  printLayoutsInfo();
}

// 全局错误处理：捕获路由组件加载失败
window.addEventListener('error', (event) => {
  if (event.message && event.message.includes('chunk') && event.message.includes('failed')) {
    console.error('检测到chunk加载失败:', event.message);
    // 可以在这里添加重试逻辑或错误提示
  }
}, true);

// 挂载应用
app.mount("#vue-admin-better");
