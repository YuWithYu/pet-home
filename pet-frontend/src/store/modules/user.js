/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 登录、获取用户信息、退出登录、清除accessToken逻辑，不建议修改
 */

import { getUserInfo, login, logout } from "@/api/user";
import {
  getAccessToken,
  removeAccessToken,
  setAccessToken,
} from "@/utils/accessToken";
import { resetRouter } from "@/router";
import { title, tokenName } from "@/config";
import { ElMessage } from "element-plus";

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
  role: "", // 角色：admin / store_admin / staff
  department: "", // 部门：上门铲屎、宠物洗护、宠物医院等
  userId: localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null,
  storeId: localStorage.getItem('storeId') ? Number(localStorage.getItem('storeId')) : null, // 所属商品店铺
  serviceStoreId: localStorage.getItem('serviceStoreId') ? Number(localStorage.getItem('serviceStoreId')) : null, // 所属服务门店，用于团队/排班/预约按门店过滤
});
const getters = {
  accessToken: (state) => state.accessToken,
  username: (state) => state.username,
  avatar: (state) => state.avatar,
  permissions: (state) => state.permissions,
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
    state.permissions = permissions;
  },
  setRole(state, role) {
    state.role = role;
  },
  setDepartment(state, department) {
    state.department = department;
  },
  setUserId(state, id) {
    state.userId = id;
    if (id != null) {
      localStorage.setItem('userId', id);
    } else {
      localStorage.removeItem('userId');
    }
  },
  setStoreId(state, storeId) {
    state.storeId = storeId;
    if (storeId != null) {
      localStorage.setItem('storeId', storeId);
    } else {
      localStorage.removeItem('storeId');
    }
  },
  setServiceStoreId(state, serviceStoreId) {
    state.serviceStoreId = serviceStoreId;
    if (serviceStoreId != null) {
      localStorage.setItem('serviceStoreId', serviceStoreId);
    } else {
      localStorage.removeItem('serviceStoreId');
    }
  },
};
const actions = {
  setPermissions({ commit }, permissions) {
    commit("setPermissions", permissions);
  },
  async login({ commit }, userInfo) {
    const { data } = await login(userInfo);
    const accessToken = data.token; // 后端返回的token字段
    if (accessToken) {
      commit("setAccessToken", accessToken);
      commit("setUsername", data.username);
      if (data.id != null) {
        commit("setUserId", data.id);
      }
      commit("setAvatar", data.avatar || "");
      // 保存角色和部门信息
      if (data.role) {
        commit("setRole", data.role);
        localStorage.setItem('userRole', data.role);
      }
      if (data.department) {
        commit("setDepartment", data.department);
        localStorage.setItem('department', data.department);
      }
      if (data.storeId != null) {
        commit("setStoreId", data.storeId);
        localStorage.setItem('storeId', data.storeId);
      }
      if (data.serviceStoreId != null) {
        commit("setServiceStoreId", data.serviceStoreId);
        localStorage.setItem('serviceStoreId', data.serviceStoreId);
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
    } else {
      ElMessage.error(`登录接口异常，未正确返回token...`);
    }
  },
  async getUserInfo({ commit, state }) {
    try {
      const { data } = await getUserInfo(state.accessToken);
      if (!data) {
        showAuthErrorOnce("验证失败，请重新登录");
        return false;
      }
      
      // 支持新的 Map 格式和旧的 User 对象格式
      let username = data.username || data.nickname;
      let avatar = data.avatar || "";
      let role = data.role || "";
      let department = data.department || "";
      
      const __dev = process.env.NODE_ENV === "development";
      if (__dev) {
        console.log("getUserInfo:", { username, role, department, permissionsLen: data.permissions?.length });
      }
      
      // 统一 role 为小写，便于前端判断
      if (role) {
        role = role.toLowerCase();
        if (role === 'super_admin') role = 'admin';
        if (!['admin', 'store_admin', 'staff', 'user'].includes(role)) role = 'staff';
      }
      
      if (username) {
        // 如果没有role字段，根据 isAdmin 判断
        if (!role) {
          role = data.isAdmin === true || data.isAdmin === "true" ? "admin" : "user";
        }
        
        // 根据角色和权限列表设置权限
        let permissions = [];
        // 后端显式返回数组时（含 length===0）必须采用，表示「细粒度权限已配置」或「零权限」
        if (data.permissions != null && Array.isArray(data.permissions)) {
          permissions = [...data.permissions];
        } else {
          // 旧接口未返回 permissions 字段时的兼容：非 admin 不再默认 admin，避免误放行
          if (role === 'admin') {
            permissions = ['admin'];
          } else if (role === 'staff' || role === 'store_admin') {
            permissions = [];
          } else {
            permissions = ['user'];
          }
        }
        
        commit("setPermissions", permissions);
        commit("setUsername", username);
        commit("setAvatar", avatar);
        commit("setRole", role);
        if (data.id != null) {
          commit("setUserId", data.id);
        }
        
        // 保存角色信息到 localStorage，以便在 getUserInfo 失败时恢复
        if (role) {
          localStorage.setItem('userRole', role);
        }
        
        // 保存门店信息（商品店铺、服务门店）
        if (data.storeId != null) {
          commit("setStoreId", data.storeId);
          localStorage.setItem('storeId', data.storeId);
        }
        if (data.serviceStoreId != null) {
          commit("setServiceStoreId", data.serviceStoreId);
          localStorage.setItem('serviceStoreId', data.serviceStoreId);
        }
        // 保存部门信息到 store 和 localStorage
        if (department) {
          commit("setDepartment", department);
          localStorage.setItem('department', department);
        } else {
          const savedDepartment = localStorage.getItem('department');
          if (savedDepartment) {
            commit("setDepartment", savedDepartment);
          }
        }
        sessionStorage.setItem("adminPermissionsResolved", "1");
        return permissions;
      } else {
        showAuthErrorOnce("用户信息接口异常");
        return false;
      }
    } catch (error) {
      console.error("获取用户信息失败:", error);
      showAuthErrorOnce("获取用户信息失败，请重新登录");
      return false;
    }
  },
  async logout({ dispatch }) {
    await logout();
    await dispatch("resetAccessToken");
    await resetRouter();
      // 延迟刷新，避免频繁触发
      const lastReload = sessionStorage.getItem('lastReload');
      const now = Date.now();
      if (!lastReload || now - parseInt(lastReload) > 2000) {
        sessionStorage.setItem('lastReload', now.toString());
        setTimeout(() => {
          location.reload();
        }, 500);
      }
  },
  resetAccessToken({ commit }) {
    sessionStorage.removeItem("adminPermissionsResolved");
    commit("setPermissions", []);
    commit("setAccessToken", "");
    commit("setRole", "");
    commit("setDepartment", "");
    commit("setUserId", null);
    commit("setStoreId", null);
    removeAccessToken();
  },
};
export default { state, getters, mutations, actions };
