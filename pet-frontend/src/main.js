import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "@/store";
import plugins from "./plugins";
import { printLayoutsInfo } from "@/utils/printInfo";
// 导入布局组件注册函数
import { registerLayoutComponents } from "@/layouts/export";
// 导入事件总线
import eventBus from "@/utils/eventBus";
// 导入配置
import { title } from "@/config";
// 导入 ResizeObserver 修复工具
import "@/utils/resizeObserverFix";
// 导入 dayjs 配置（含全套插件与中文语言包）
import dayjs from "@/utils/dayjs";
// 导入 Element Plus 已知运行时噪音过滤
import "@/utils/element-plus-fix";
// 导入 Element Plus 中文语言包
import zhCn from "@/locale/element-plus-zh-cn";

// 确保 dayjs 在全局可用（Element Plus 及部分布局组件依赖 window.dayjs）
window.dayjs = dayjs;

// 创建应用实例
const app = createApp(App);

app.use(store);
app.use(router);

// 初始化所有插件
plugins(app);

// 配置 Element Plus 国际化
app.config.globalProperties.$ELEMENT = {
  locale: zhCn,
};

// 注册所有布局组件
registerLayoutComponents(app);

// 添加事件总线到全局属性（布局组件通过 window.$eventBus 通信）
app.config.globalProperties.$eventBus = eventBus;
window.$eventBus = eventBus;

// 添加全局标题
app.config.globalProperties.$baseTitle = title;

// 仅当明确开启 Mock 时才动态加载（mockjs 不会进入生产 bundle）
// 若需使用 Mock 数据，在 rspack.config.js 中将 VUE_APP_MOCK_ENABLE 设为 "true"
if (
  process.env.NODE_ENV !== "production" &&
  process.env.VUE_APP_MOCK_ENABLE === "true"
) {
  import("@/utils/static").then(({ mockXHR }) => {
    mockXHR();
    console.warn("开发环境已启用Mock拦截");
  });
}
if (process.env.NODE_ENV === "development") {
  printLayoutsInfo();
}

// 挂载应用
app.mount("#vue-admin-better");
