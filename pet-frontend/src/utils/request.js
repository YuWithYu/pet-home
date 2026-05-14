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
  tokenName,
} from "@/config";
import store from "@/store";
import qs from "qs";
import router from "@/router";
import { isArray } from "@/utils/validate";
import { ElLoading, ElMessage } from "element-plus";
import { pickBy, identity } from "lodash-es";
import { mock } from "mockjs";

// 在生产环境下引入mock数据
if (process.env.NODE_ENV === "production") {
  const mockContext = require.context("../../mock/controller", true, /\.js$/);
  mockContext.keys().forEach((key) => {
    const mockModule = mockContext(key);
    if (mockModule.default) {
      mockModule.default;
    } else {
      mockModule;
    }
  });
}

let loadingInstance;

const isAuthPath = (path = "") =>
  path === "/login" || path === "/register";

const isAuthRequestUrl = (url = "") => {
  if (!url) return false;
  return url.includes("/login") || url.includes("/register");
};

const forceLogoutAndRedirect = (
  message = "登录状态已失效，请重新登录"
) => {
  const currentPath = router.currentRoute?.value?.path || "";

  store.dispatch("user/resetAccessToken");

  if (isAuthPath(currentPath)) {
    return;
  }

  if (message) {
    ElMessage.closeAll();
    ElMessage.error(message);
  }

  if (loginInterception) {
    const redirectQuery =
      currentPath && currentPath !== "/login" ? { redirect: currentPath } : {};
    router
      .push({ path: "/login", query: redirectQuery })
      .catch(() => {});
  }
};

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 处理code异常
 * @param {*} code
 * @param {*} msg
 * @param {*} requestConfig 可选的请求配置，用于判断是否是登录请求
 */
const handleCode = (code, msg, requestConfig = null) => {
  switch (code) {
    case invalidCode: {
      const currentPath = router.currentRoute?.value?.path || "";
      const requestUrl = requestConfig?.url || "";

      if (isAuthRequestUrl(requestUrl)) {
        console.warn("登录相关接口返回 401，保持在当前页面:", requestUrl);
        break;
      }

      if (isAuthPath(currentPath)) {
        console.warn("当前已在登录页，忽略 401 跳转");
        break;
      }

      console.warn("检测到接口返回 401，强制重新登录:", requestUrl);
      forceLogoutAndRedirect();
      break;
    }
    case noPermissionCode:
      router.push({ path: "/401" }).catch(() => {});
      break;
    default:
      // 其他错误：不显示错误提示，避免频繁弹窗
      // ElMessage.error(msg || `后端接口${code}异常`);
      console.warn('接口返回错误:', code, msg);
      break;
  }
};

// 请求重试配置
const retryConfig = {
  retry: 3, // 重试次数
  retryDelay: 1000, // 重试间隔时间
};

const resolveRuntimeBaseURL = () => {
  let fallback = baseURL || ""

  if (typeof window === "undefined") {
    return fallback
  }

  const { protocol, hostname, port } = window.location

  // 默认后端端口，开发时与后端分离
  const DEFAULT_BACKEND_PORT = "8080"

  const isDev = process.env.NODE_ENV !== "production"

  if (isDev) {
    const targetPort = DEFAULT_BACKEND_PORT
    const needsPort = targetPort && targetPort !== "80" && targetPort !== "443"
    return `${protocol}//${hostname}${needsPort ? `:${targetPort}` : ""}/api`
  }

  // 生产环境：默认同域 /api
  const sameOriginPort =
    port && port !== "80" && port !== "443" ? `:${port}` : ""

  return `${protocol}//${hostname}${sameOriginPort}/api`
}

const runtimeBaseURL = resolveRuntimeBaseURL()

// 创建axios实例
const instance = axios.create({
  baseURL: runtimeBaseURL,
  timeout: 30000, // 超时时间30秒，避免复杂查询超时
  headers: {
    "Content-Type": contentType,
  },
});

// 请求重试方法
instance.defaults.retry = retryConfig.retry;
instance.defaults.retryDelay = retryConfig.retryDelay;

// 请求拦截器
instance.interceptors.request.use(
  (config) => {
    // 仅在有 token 时添加认证头，避免未登录时发送无效 token 导致后端返回 401
    if (store.state.user.accessToken) {
      config.headers["Authorization"] = `Bearer ${store.state.user.accessToken}`;
    }

    // 对于文件上传请求，不要处理data
    if (config.data instanceof FormData) {
      console.log("检测到FormData请求，跳过数据处理");
      // 不要设置Content-Type，让浏览器自动设置
      delete config.headers["Content-Type"];
    } else {
      // 对于数组数据，不要使用 pickBy（pickBy 只适用于对象）
      if (config.data && !Array.isArray(config.data)) {
        //这里会过滤所有为空、0、false的key，如果不需要请自行注释
        config.data = pickBy(config.data, identity);
      }
      if (
        config.data &&
        config.headers["Content-Type"] ===
          "application/x-www-form-urlencoded;charset=UTF-8"
      )
        config.data = qs.stringify(config.data);
    }
    
    if (debounce.some((item) => config.url.includes(item)))
      loadingInstance = ElLoading.service();

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    if (loadingInstance) loadingInstance.close();

    const { data, config } = response;

    // 判断data是否为undefined或null
    if (data === undefined || data === null) {
      ElMessage.error("后端接口返回数据为空");
      return Promise.reject("后端接口返回数据为空");
    }

    // 安全地解构code和msg，避免undefined异常
    const code = data.code !== undefined ? data.code : null;
    const msg = data.msg !== undefined ? data.msg : "未知错误";

    // 操作正常Code数组
    const codeVerificationArray = isArray(successCode)
      ? [...successCode]
      : [...[successCode]];

    // 是否操作正常
    if (code !== null && codeVerificationArray.includes(code)) {
      return data;
    } else {
      handleCode(code, msg, config);
      return Promise.reject(
        `vue-admin-better请求异常拦截:${JSON.stringify({
          url: config.url,
          code,
          msg,
        })}` || "Error"
      );
    }
  },
  (error) => {
    if (loadingInstance) loadingInstance.close();

    // 处理undefined或无法解析的错误情况
    if (!error) {
      ElMessage.error("发生未知错误");
      return Promise.reject("发生未知错误");
    }

    // 统一提取错误信息
    const { config, response, message } = error;
    
    // 处理 401 错误（不重试，直接提示并跳转登录）
    if (response && response.status === 401) {
      const currentPath = router.currentRoute?.value?.path || "";
      const requestUrl = config?.url || "";

      if (isAuthRequestUrl(requestUrl) || isAuthPath(currentPath)) {
        console.warn("401 错误发生在登录流程，保持在当前页面:", requestUrl);
        return Promise.reject(error);
      }

      const backendMsg =
        (response.data && response.data.msg) ||
        "登录状态已失效，请重新登录";

      console.warn("检测到 HTTP 401，触发强制重新登录:", requestUrl);
      forceLogoutAndRedirect(backendMsg);
      return Promise.reject(error);
    }
    
    // 其他错误的重试逻辑已被禁用，避免无限循环
    // if (config && config.retry) {
    //   ...
    // }
    if (response && response.data) {
      const { status, data } = response;
      // 401 错误已在上面处理，这里不再调用 handleCode 避免刷新
      if (status !== 401) {
        handleCode(status, data.msg || message || "未知错误");
      }
      return Promise.reject(error);
    } else {
      let errorMsg = "后端接口未知异常";

      if (message) {
        if (message === "Network Error") {
          errorMsg = "后端接口连接异常";
        } else if (message.includes("timeout")) {
          errorMsg = "后端接口请求超时";
        } else if (message.includes("Request failed with status code")) {
          const code = message.substr(message.length - 3);
          errorMsg = `后端接口${code}异常`;
        }
      }

      // 不显示错误提示，避免频繁弹窗导致页面刷新
      console.warn('请求错误:', errorMsg);
      // ElMessage.error(errorMsg);
      return Promise.reject(error);
    }
  }
);

export default instance;
