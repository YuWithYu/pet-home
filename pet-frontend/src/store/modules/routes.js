/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 路由拦截状态管理，目前两种模式：all模式与intelligence模式
 */
import { asyncRoutes, constantRoutes } from '@/router'
import { getRouterList } from '@/api/router'
import { convertRouter, filterAsyncRoutes } from '@/utils/handleRoutes'

const state = () => ({
  routes: [],
  partialRoutes: [],
})
const getters = {
  routes: (state) => state.routes,
  partialRoutes: (state) => state.partialRoutes,
}
const mutations = {
  setRoutes(state, routes) {
    state.routes = constantRoutes.concat(routes)
  },
  setAllRoutes(state, routes) {
    state.routes = constantRoutes.concat(routes)
  },
}
const actions = {
  /**
   * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
   * @description intelligence模式设置路由
   * @param {*} { commit }
   * @param {*} permissions
   * @returns
   */
  async setRoutes({ commit, rootGetters }, permissions) {
    // 获取用户角色和部门信息
    const userRole = rootGetters['user/role'] || 'admin'
    const userDepartment = rootGetters['user/department'] || ''
    
    console.log('=== 开始过滤路由 ===');
    console.log('权限列表:', permissions);
    console.log('用户角色:', userRole);
    console.log('用户部门:', userDepartment);
    console.log('所有路由数量:', asyncRoutes.length);
    
    //根据permissions和部门权限做路由筛选
    let accessedRoutes = filterAsyncRoutes(asyncRoutes, permissions, userRole, userDepartment)
    
    console.log('过滤后的路由数量:', accessedRoutes.length);
    console.log('过滤后的路由:', accessedRoutes.map(r => ({ path: r.path, name: r.name, children: r.children?.length || 0 })));
    console.log('=== 路由过滤完成 ===');
    
    commit('setRoutes', accessedRoutes)
    return accessedRoutes
  },
  /**
   * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
   * @description all模式设置路由
   * @param {*} { commit }
   * @returns
   */
  async setAllRoutes({ commit }) {
    try {
      let { data } = await getRouterList()
      if (!data || !Array.isArray(data)) {
        console.error('后端返回的路由数据格式不正确', data)
        data = []
      }

      const accessedRoutes = convertRouter(data)
      commit('setAllRoutes', accessedRoutes)
      return accessedRoutes
    } catch (error) {
      console.error('获取路由列表失败', error)
      commit('setAllRoutes', [])
      return []
    }
  },
}
export default { state, getters, mutations, actions }
