import axios from "axios";
import {
  baseURL,
  contentType,
  debounce,
  invalidCode,
  loginInterception,
  noPermissionCode,
  requestTimeout,
  successCode,
} from "@/config";
import store from "@/store";
import qs from "qs";
import router from "@/router";
import { isArray } from "@/utils/validate";
import { ElLoading, ElMessage } from "element-plus";
import { omitBy } from "lodash-es";

let loadingInstance;

const isAuthPath = (path = "") => path === "/login" || path === "/register";

const isAuthRequestUrl = (url = "") =>
  !!url && (url.includes("/login") || url.includes("/register"));

// 同一错误信息 3 秒内只提示一次，避免并发请求触发弹窗轰炸
const messageShownAt = new Map();
const showErrorOnce = (msg) => {
  if (!msg) return;
  const now = Date.now();
  const last = messageShownAt.get(msg) || 0;
  if (now - last < 3000) return;
  messageShownAt.set(msg, now);
  ElMessage.error(msg);
};

const forceLogoutAndRedirect = (message = "登录状态已失效，请重新登录") => {
  const currentPath = router.currentRoute?.value?.path || "";

  store.dispatch("user/resetAccessToken");

  if (isAuthPath(currentPath)) return;

  ElMessage.closeAll();
  showErrorOnce(message);

  if (loginInterception) {
    const redirectQuery =
      currentPath && currentPath !== "/login" ? { redirect: currentPath } : {};
    router.push({ path: "/login", query: redirectQuery }).catch(() => {});
  }
};

/**
 * 处理业务 code 异常
 * 后端约定：401=未登录/Token 失效，403=无权限，400=参数错误，500=系统错误
 */
const handleCode = (code, msg, requestConfig = null) => {
  switch (code) {
    case invalidCode: {
      // 登录流程中的 401 交由页面自行处理，不触发全局跳转
      if (
        isAuthRequestUrl(requestConfig?.url) ||
        isAuthPath(router.currentRoute?.value?.path)
      ) {
        break;
      }
      forceLogoutAndRedirect(msg);
      break;
    }
    case noPermissionCode:
      showErrorOnce(msg || "您没有权限执行此操作");
      router.push({ path: "/401" }).catch(() => {});
      break;
    default:
      showErrorOnce(msg || `后端接口${code}异常`);
      break;
  }
};

// 开发环境走 devServer 代理（/api → localhost:8080），生产环境优先使用配置的 baseURL
const resolveBaseURL = () => {
  if (process.env.NODE_ENV !== "production") return "/api";
  if (baseURL) return baseURL;
  const { protocol, hostname, port } = window.location;
  const sameOriginPort =
    port && port !== "80" && port !== "443" ? `:${port}` : "";
  return `${protocol}//${hostname}${sameOriginPort}/api`;
};

const instance = axios.create({
  baseURL: resolveBaseURL(),
  timeout: requestTimeout,
  headers: {
    "Content-Type": contentType,
  },
});

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    if (store.state.user.accessToken) {
      config.headers["Authorization"] = `Bearer ${store.state.user.accessToken}`;
    }

    if (config.data instanceof FormData) {
      // 让浏览器自动设置带 boundary 的 Content-Type
      delete config.headers["Content-Type"];
    } else {
      if (config.data && !Array.isArray(config.data)) {
        // 仅剔除 undefined/null，保留 0、false、空字符串等合法值
        config.data = omitBy(config.data, (v) => v === undefined || v === null);
      }
      if (
        config.data &&
        config.headers["Content-Type"] ===
          "application/x-www-form-urlencoded;charset=UTF-8"
      ) {
        config.data = qs.stringify(config.data);
      }
    }

    if (debounce.some((item) => config.url.includes(item))) {
      loadingInstance = ElLoading.service();
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    if (loadingInstance) loadingInstance.close();

    const { data, config } = response;

    if (data === undefined || data === null) {
      showErrorOnce("后端接口返回数据为空");
      return Promise.reject(new Error("后端接口返回数据为空"));
    }

    const code = data.code !== undefined ? data.code : null;
    const msg = data.msg !== undefined ? data.msg : "未知错误";

    const codeVerificationArray = isArray(successCode)
      ? [...successCode]
      : [...[successCode]];

    if (code !== null && codeVerificationArray.includes(code)) {
      return data;
    }

    handleCode(code, msg, config);
    return Promise.reject(
      new Error(
        `请求异常: ${JSON.stringify({ url: config.url, code, msg })}`
      )
    );
  },
  (error) => {
    if (loadingInstance) loadingInstance.close();

    if (!error) {
      showErrorOnce("发生未知错误");
      return Promise.reject(new Error("发生未知错误"));
    }

    const { config, response, message } = error;

    // HTTP 401：登录态失效，强制重新登录
    if (response && response.status === 401) {
      if (isAuthRequestUrl(config?.url) || isAuthPath(router.currentRoute?.value?.path)) {
        return Promise.reject(error);
      }
      forceLogoutAndRedirect(
        (response.data && response.data.msg) || "登录状态已失效，请重新登录"
      );
      return Promise.reject(error);
    }

    if (response && response.data) {
      const { status, data } = response;
      handleCode(status, data.msg || message || "未知错误", config);
      return Promise.reject(error);
    }

    let errorMsg = "后端接口未知异常";
    if (message === "Network Error") {
      errorMsg = "后端接口连接异常";
    } else if (message && message.includes("timeout")) {
      errorMsg = "后端接口请求超时";
    } else if (message && message.includes("Request failed with status code")) {
      errorMsg = `后端接口${message.slice(-3)}异常`;
    }
    showErrorOnce(errorMsg);
    return Promise.reject(error);
  }
);

export default instance;
