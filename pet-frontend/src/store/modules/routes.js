/**
 * 路由拦截状态管理（intelligence 模式：前端按 permissions 过滤本地路由）
 */
import { asyncRoutes, constantRoutes } from "@/router";
import { getRouterList } from "@/api/router";
import { convertRouter, filterAsyncRoutes } from "@/utils/handleRoutes";

const state = () => ({
  routes: [],
  partialRoutes: [],
  // 动态路由是否已注册（与用户信息的 permissionsLoaded 分离：
  // 登录页会预拉用户信息置位 permissionsLoaded，但路由注册仍需守卫完成）
  routesLoaded: false,
});
const getters = {
  routes: (state) => state.routes,
  partialRoutes: (state) => state.partialRoutes,
  routesLoaded: (state) => state.routesLoaded,
};
const mutations = {
  setRoutes(state, routes) {
    state.routes = constantRoutes.concat(routes);
  },
  setRoutesLoaded(state, loaded) {
    state.routesLoaded = !!loaded;
  },
};
const actions = {
  /**
   * intelligence 模式：按 permissions 与角色过滤路由
   */
  async setRoutes({ commit, rootGetters }, permissions) {
    const userRole = rootGetters["user/role"] || "";
    const userDepartment = rootGetters["user/department"] || "";

    const accessedRoutes = filterAsyncRoutes(
      asyncRoutes,
      permissions,
      userRole,
      userDepartment
    );

    commit("setRoutes", accessedRoutes);
    return accessedRoutes;
  },
  /**
   * all 模式：完全由后端返回路由
   */
  async setAllRoutes({ commit }) {
    try {
      let { data } = await getRouterList();
      if (!data || !Array.isArray(data)) {
        console.error("后端返回的路由数据格式不正确", data);
        data = [];
      }
      const accessedRoutes = convertRouter(data);
      commit("setRoutes", accessedRoutes);
      return accessedRoutes;
    } catch (error) {
      console.error("获取路由列表失败", error);
      commit("setRoutes", []);
      return [];
    }
  },
};
export default { state, getters, mutations, actions };
