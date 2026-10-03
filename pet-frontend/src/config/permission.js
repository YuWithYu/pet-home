/**
 * 路由守卫（标准动态路由模式）
 *
 * 流程：
 * 1. 白名单直接放行
 * 2. 无 token → 登录页（记录来源路由）
 * 3. 已登录访问 /login → 首页
 * 4. 权限未加载（首次进入或刷新后）→ getUserInfo → 注册动态路由 → 重新匹配当前路由
 * 5. 访问控制统一走 canAccessRoute（默认拒绝）
 */
import router from "@/router";
import store from "@/store";
import VabProgress from "nprogress";
import "nprogress/nprogress.css";
import getPageTitle from "@/utils/pageTitle";
import {
  authentication,
  loginInterception,
  progressBar,
  recordRoute,
  routesWhiteList,
} from "@/config";
import { ElMessage } from "element-plus";
import { canAccessRoute } from "@/utils/routePermission";

VabProgress.configure({
  easing: "ease",
  speed: 500,
  trickleSpeed: 200,
  showSpinner: false,
});

const finishProgress = () => {
  if (progressBar) VabProgress.done();
};

router.beforeEach(async (to, from) => {
  if (progressBar) VabProgress.start();

  // 历史路径兼容：旧服务人员排班入口
  if (to.path === "/service-staff/schedule") {
    finishProgress();
    return { path: "/service-staff", replace: true };
  }

  // 白名单直接放行
  if (routesWhiteList.includes(to.path)) {
    finishProgress();
    return true;
  }

  // 未登录拦截（loginInterception 为 false 时为免登录演示模式）
  if (!store.getters["user/accessToken"] && loginInterception) {
    finishProgress();
    return recordRoute
      ? { path: "/login", query: { redirect: to.fullPath }, replace: true }
      : { path: "/login", replace: true };
  }

  // 已登录访问登录页 → 首页
  if (to.path === "/login") {
    finishProgress();
    return { path: "/index", replace: true };
  }

  // 动态路由未注册（首次进入、页面刷新、登录页跳转）：确保用户信息已加载并注册路由
  // 注意：不能以 permissionsLoaded 作为本块入口条件——登录页会预拉用户信息提前置位它，
  // 导致登录后跳转时跳过路由注册，列表页/菜单缺失，需手动刷新才恢复
  if (!store.getters["routes/routesLoaded"]) {
    let permissions = store.getters["user/permissions"];

    if (!store.getters["user/permissionsLoaded"]) {
      if (!loginInterception) {
        // 免登录演示模式：注入虚拟管理员权限
        permissions = ["admin"];
        await store.dispatch("user/setPermissions", permissions);
        store.commit("user/setPermissionsLoaded", true);
      } else {
        permissions = await store.dispatch("user/getUserInfo");
        if (permissions === false) {
          // token 失效或后端不可用：清理登录态，回登录页
          await store.dispatch("user/resetAccessToken");
          finishProgress();
          return { path: "/login", replace: true };
        }
      }
    }

    const accessRoutes =
      authentication === "all"
        ? await store.dispatch("routes/setAllRoutes")
        : await store.dispatch("routes/setRoutes", permissions);

    accessRoutes.forEach((route) => router.addRoute(route));
    store.commit("routes/setRoutesLoaded", true);

    // 重新匹配目标路由（新注册的路由此刻才可见），无任何延时等待
    finishProgress();
    return { ...to, replace: true };
  }

  // 访问控制：统一入口、默认拒绝
  const allow = canAccessRoute(
    to.path,
    to.meta,
    store.getters["user/role"],
    store.getters["user/permissions"]
  );
  if (!allow) {
    finishProgress();
    ElMessage.error("您没有权限访问该页面");
    // 首次导航（直接输入 URL / 刷新）无历史页面可停留，回落到 401 页避免白屏
    return from.matched.length === 0
      ? { path: "/401", replace: true }
      : false;
  }

  finishProgress();
  return true;
});

router.afterEach((to) => {
  document.title = getPageTitle(to.meta.title);
  if (progressBar) VabProgress.done();
});
