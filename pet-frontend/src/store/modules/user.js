/**
 * 登录、获取用户信息、退出登录、清除 accessToken 逻辑
 */

import { getUserInfo, login, logout } from "@/api/user";
import {
  getAccessToken,
  removeAccessToken,
  setAccessToken,
} from "@/utils/accessToken";
import { resetRouter } from "@/router";
import { title } from "@/config";
import { ElMessage } from "element-plus";

// 同一鉴权错误 2.5 秒内只提示一次
function showAuthErrorOnce(msg) {
  const now = Date.now();
  const lastTs = Number(sessionStorage.getItem("authErrorToastTs") || 0);
  if (now - lastTs > 2500) {
    ElMessage.error(msg);
    sessionStorage.setItem("authErrorToastTs", String(now));
  }
}

const state = () => ({
  accessToken: getAccessToken(),
  username: "",
  avatar: "",
  permissions: [],
  permissionsLoaded: false, // 页面刷新后为 false，路由守卫据此重新拉取用户信息
  role: "", // 角色：admin / store_admin / staff / user
  department: "", // 部门：上门铲屎、宠物洗护、宠物医院等
  userId: localStorage.getItem("userId")
    ? Number(localStorage.getItem("userId"))
    : null,
  storeId: localStorage.getItem("storeId")
    ? Number(localStorage.getItem("storeId"))
    : null, // 所属商品店铺
  serviceStoreId: localStorage.getItem("serviceStoreId")
    ? Number(localStorage.getItem("serviceStoreId"))
    : null, // 所属服务门店，用于团队/排班/预约按门店过滤
});

const getters = {
  accessToken: (state) => state.accessToken,
  username: (state) => state.username,
  avatar: (state) => state.avatar,
  permissions: (state) => state.permissions,
  permissionsLoaded: (state) => state.permissionsLoaded,
  role: (state) => state.role,
  department: (state) => state.department,
  userId: (state) => state.userId,
  storeId: (state) => state.storeId,
  serviceStoreId: (state) => state.serviceStoreId,
  userInfo: (state) => ({ ...state, id: state.userId }),
};

const mutations = {
  setAccessToken(state, accessToken) {
    state.accessToken = accessToken;
    setAccessToken(accessToken);
  },
  setUsername(state, username) {
    state.username = username;
  },
  setAvatar(state, avatar) {
    state.avatar = avatar;
  },
  setPermissions(state, permissions) {
    state.permissions = Array.isArray(permissions) ? permissions : [];
  },
  setPermissionsLoaded(state, loaded) {
    state.permissionsLoaded = !!loaded;
  },
  setRole(state, role) {
    state.role = role;
    if (role) {
      localStorage.setItem("userRole", role);
    } else {
      localStorage.removeItem("userRole");
    }
  },
  setDepartment(state, department) {
    state.department = department;
    if (department) {
      localStorage.setItem("department", department);
    } else {
      localStorage.removeItem("department");
    }
  },
  setUserId(state, id) {
    state.userId = id;
    if (id != null) {
      localStorage.setItem("userId", id);
    } else {
      localStorage.removeItem("userId");
    }
  },
  setStoreId(state, storeId) {
    state.storeId = storeId;
    if (storeId != null) {
      localStorage.setItem("storeId", storeId);
    } else {
      localStorage.removeItem("storeId");
    }
  },
  setServiceStoreId(state, serviceStoreId) {
    state.serviceStoreId = serviceStoreId;
    if (serviceStoreId != null) {
      localStorage.setItem("serviceStoreId", serviceStoreId);
    } else {
      localStorage.removeItem("serviceStoreId");
    }
  },
};

const actions = {
  setPermissions({ commit }, permissions) {
    commit("setPermissions", permissions);
  },
  async login({ commit }, userInfo) {
    const { data } = await login(userInfo);
    const accessToken = data.token;
    if (!accessToken) {
      ElMessage.error("登录接口异常，未正确返回token...");
      return;
    }

    commit("setAccessToken", accessToken);
    commit("setUsername", data.username);
    if (data.id != null) commit("setUserId", data.id);
    commit("setAvatar", data.avatar || "");
    if (data.role) commit("setRole", data.role.toLowerCase());
    if (data.department) commit("setDepartment", data.department);
    if (data.storeId != null) commit("setStoreId", data.storeId);
    if (data.serviceStoreId != null) {
      commit("setServiceStoreId", data.serviceStoreId);
    }

    const hour = new Date().getHours();
    const thisTime =
      hour < 8
        ? "早上好"
        : hour <= 11
        ? "上午好"
        : hour <= 13
        ? "中午好"
        : hour < 18
        ? "下午好"
        : "晚上好";
    ElMessage.success(`欢迎登录${title}，${thisTime}！`);
  },
  async getUserInfo({ commit, state }) {
    try {
      const { data } = await getUserInfo(state.accessToken);
      if (!data) {
        showAuthErrorOnce("验证失败，请重新登录");
        return false;
      }

      const username = data.username || data.nickname;
      if (!username) {
        showAuthErrorOnce("用户信息接口异常");
        return false;
      }

      let role = (data.role || "").toLowerCase();
      if (role === "super_admin") role = "admin";
      // 未知角色保留原值：无 admin 通配、无具体路径权限时按默认拒绝处理，不做任何提权
      if (!role) {
        role = data.isAdmin === true || data.isAdmin === "true" ? "admin" : "user";
      }

      // 后端显式返回数组时（含 length===0）必须采用，表示「细粒度权限已配置」或「零权限」
      let permissions;
      if (data.permissions != null && Array.isArray(data.permissions)) {
        permissions = [...data.permissions];
      } else if (role === "admin") {
        permissions = ["admin"];
      } else if (role === "staff" || role === "store_admin") {
        permissions = [];
      } else {
        permissions = ["user"];
      }

      commit("setPermissions", permissions);
      commit("setUsername", username);
      commit("setAvatar", data.avatar || "");
      commit("setRole", role);
      if (data.id != null) commit("setUserId", data.id);
      if (data.storeId != null) commit("setStoreId", data.storeId);
      if (data.serviceStoreId != null) {
        commit("setServiceStoreId", data.serviceStoreId);
      }
      // 部门信息后端未返回时保留本地缓存（用于排班等场景展示）
      const department = data.department;
      if (department) {
        commit("setDepartment", department);
      } else if (!state.department) {
        const saved = localStorage.getItem("department");
        if (saved) commit("setDepartment", saved);
      }

      commit("setPermissionsLoaded", true);
      return permissions;
    } catch (error) {
      console.error("获取用户信息失败:", error);
      showAuthErrorOnce("获取用户信息失败，请重新登录");
      return false;
    }
  },
  async logout({ dispatch }) {
    try {
      await logout();
    } catch (error) {
      // 登出接口失败（如 token 已失效）也必须继续清理本地登录态
      console.error("登出接口调用失败:", error);
    }
    await dispatch("resetAccessToken");
    await resetRouter();
    // 硬跳转一次性清理动态路由、tabs、keepAlive 等全部运行时状态
    window.location.href = "/login";
  },
  resetAccessToken({ commit }) {
    commit("setPermissions", []);
    commit("setPermissionsLoaded", false);
    commit("setAccessToken", "");
    commit("setRole", "");
    commit("setDepartment", "");
    commit("setUserId", null);
    commit("setStoreId", null);
    commit("setServiceStoreId", null);
    // 401 强制登出走 SPA 跳转（无页面刷新），必须同步清除动态路由已注册标记，
    // 否则重新登录后守卫会跳过路由注册，列表页/菜单缺失
    commit("routes/setRoutesLoaded", false, { root: true });
    removeAccessToken();
  },
};

export default { state, getters, mutations, actions };
