/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 路由守卫，目前两种模式：all模式与intelligence模式
 */
import router from "@/router";
import store from "@/store";
import Layout from "@/layouts/index.vue";
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
import { setupRoutesByRole } from "@/router";
import { nextTick } from "vue";
import { hasAnyPathPermission, pathMatchesUserPermission } from "@/utils/routePermission";

VabProgress.configure({
  easing: "ease",
  speed: 500,
  trickleSpeed: 200,
  showSpinner: false,
});

router.beforeEach(async (to, from, next) => {
  if (to.path === "/service-staff/schedule") {
    next({ path: "/service-staff", replace: true });
    if (progressBar) VabProgress.done();
    return;
  }

  // 防止无限循环：检查是否是重复的路由跳转
  const routeChangeKey = `route_${to.path}_${Date.now()}`;
  const lastRouteChange = sessionStorage.getItem('lastRouteChange');
  const routeChangeCount = parseInt(sessionStorage.getItem('routeChangeCount') || '0');
  
  // 如果同一个路径在短时间内多次触发，可能是循环
  // 但是对于 /login 路径，允许跳转（可能是正常的重定向）
  if (lastRouteChange === to.path && routeChangeCount > 3 && to.path !== '/login') {
    console.error('检测到路由循环，阻止跳转:', to.path);
    sessionStorage.removeItem('lastRouteChange');
    sessionStorage.removeItem('routeChangeCount');
    sessionStorage.removeItem('isLoadingRoutes');
    sessionStorage.removeItem('lastRoutePath');
    // 如果是在登录页面，直接放行
    if (to.path === '/login') {
      next();
      if (progressBar) VabProgress.done();
      return;
    }
    // 其他情况，跳转到首页（员工按权限分配，不再按部门自动跳转）
    next({ path: '/', replace: true });
    if (progressBar) VabProgress.done();
    return;
  }
  
  // 记录路由变化
  if (lastRouteChange === to.path) {
    sessionStorage.setItem('routeChangeCount', (routeChangeCount + 1).toString());
  } else {
    sessionStorage.setItem('lastRouteChange', to.path);
    sessionStorage.setItem('routeChangeCount', '1');
  }
  
  if (progressBar) VabProgress.start();
  
  // 特殊处理：如果是登录页，直接放行，不检查token
  if (routesWhiteList.indexOf(to.path) !== -1) {
    next();
    if (progressBar) VabProgress.done();
    return;
  }
  
  // 从多个地方获取token，确保准确性
  let hasToken = store.getters["user/accessToken"];
  // 同时检查localStorage中的token
  const tokenFromStorage = localStorage.getItem('vue-admin-better-2024') || sessionStorage.getItem('vue-admin-better-2024');
  
  // 如果store中没有token但storage中有，说明store状态可能丢失，重新设置
  if (!hasToken && tokenFromStorage) {
    console.warn('store中无token但storage中有，恢复token');
    store.commit('user/setAccessToken', tokenFromStorage);
    hasToken = tokenFromStorage;
  }
  
  // 调试：打印token和权限信息
  console.log('=== 路由守卫开始 ===');
  console.log('目标路径:', to.path);
  console.log('是否有token (store):', !!hasToken);
  console.log('是否有token (storage):', !!tokenFromStorage);
  console.log('token值:', hasToken || tokenFromStorage ? '存在' : '不存在');
  const debugPermissions = store.getters["user/permissions"];
  const debugRole = store.getters["user/role"];
  console.log('用户权限:', debugPermissions);
  console.log('用户角色:', debugRole);
  
  
  // 如果storage中有token但store中没有权限，说明可能是页面刷新导致状态丢失
  // 但这种情况不应该跳转登录页，而是应该重新获取用户信息
  if ((hasToken || tokenFromStorage) && (!debugPermissions || debugPermissions.length === 0)) {
    console.warn('有token但无权限，可能是页面刷新导致状态丢失，需要重新获取用户信息');
    // 这种情况不应该跳转登录页，而是应该继续加载路由
  }
  
  // 特殊处理根路径 "/"
  if (to.path === "/") {
    if (!hasToken && !tokenFromStorage) {
      // 没有token，跳转到登录页
      console.warn('根路径访问，无token，跳转登录页');
      next({ path: "/login", replace: true });
      if (progressBar) VabProgress.done();
      return;
    } else {
      // 有token，已登录，跳转到首页
      console.log('根路径访问，有token，跳转首页');
      next({ path: "/index", replace: true });
      if (progressBar) VabProgress.done();
      return;
    }
  }

  if (!loginInterception) hasToken = true;
  
  // 最终确认：如果storage中有token，即使store中没有，也认为有token
  // 如果store中没有token但storage中有，恢复token到store
  if (!hasToken && tokenFromStorage) {
    console.warn('store中无token但storage中有，恢复token到store');
    store.commit('user/setAccessToken', tokenFromStorage);
    hasToken = tokenFromStorage;
  }
  
  // 更严格的token检查：确保token不是空字符串或只包含空白字符
  const finalHasToken = (hasToken && hasToken.trim && hasToken.trim().length > 0) || 
                        (tokenFromStorage && tokenFromStorage.trim && tokenFromStorage.trim().length > 0);

  // 如果最终确认有token，进入权限检查流程
  if (finalHasToken) {
    console.log('有token，进入权限检查流程');
    if (to.path === "/login") {
      // 如果已登录，跳转到首页（服务人员也跳转到首页，可以看到完整的后台界面）
      const userRole = store.getters["user/role"];
      const userDepartment = store.getters["user/department"] || localStorage.getItem('department');
      
      // 所有用户（包括服务人员）都跳转到首页
      // 服务人员可以看到完整的菜单，但只能访问自己部门的服务页面
      let redirectPath = "/index";
      
      console.log('已登录用户访问登录页，跳转到首页，角色:', userRole, '部门:', userDepartment);
      
      // 直接跳转，不等待
      next({ path: redirectPath, replace: true });
      if (progressBar) VabProgress.done();
    } else {
      // 首先检查用户角色和路由权限（在路由跳转时进行验证）
      const userRole = store.getters["user/role"];
      const userDepartment = store.getters["user/department"] || localStorage.getItem('department');
      const userPermissions = store.getters["user/permissions"];
      const currentServiceStoreId = store.getters["user/serviceStoreId"] || localStorage.getItem("serviceStoreId");
      
      console.log('路由守卫 - 开始检查权限');
      console.log('路由守卫 - 目标路径:', to.path);
      console.log('路由守卫 - 路由meta:', to.meta);
      console.log('路由守卫 - 用户角色:', userRole, '用户部门:', userDepartment);
      console.log('路由守卫 - 用户权限:', userPermissions);

      // 不再按角色做路径硬限制，统一走权限管理中勾选的页面权限
      
      // 检查是否有具体权限设置（权限代码以 / 开头）
      const hasSpecificPermissions = userPermissions && userPermissions.some(perm => perm !== 'admin' && perm.startsWith('/'));
      
      console.log('路由守卫 - 权限检查:', {
        userRole,
        userPermissions: Array.from(userPermissions || []),
        hasSpecificPermissions,
        targetPath: to.path
      });
      
      // 管理员始终可以访问所有页面，不受权限限制
      if (userRole === 'admin') {
        console.log('管理员访问，允许所有页面');
        // 继续后续逻辑，不进行权限检查
      }
      // 如果有具体权限设置，先检查权限代码（不包括 admin 通配）
      else if (hasSpecificPermissions && to.path !== '/' && to.path !== '/index' && to.path !== '/login') {
        const routePath = to.path;
        
        // 检查权限（含预约订单聚合路径、旧 permissionCode 兼容）
        const hasRoutePermission = hasAnyPathPermission(routePath, userPermissions);
        
        if (hasRoutePermission) {
          console.log('路由守卫 - 权限验证通过（通过权限代码）:', routePath, '匹配的权限:', userPermissions.find(p => 
            pathMatchesUserPermission(routePath, p)
          ));
          // 权限验证通过，继续后续逻辑（跳过部门检查，直接放行）
          // 注意：这里不 return，让后续逻辑继续执行（如路由加载等）
        } else {
          console.warn('路由守卫 - 路由路径不在权限列表中，拒绝访问');
          console.warn('路由路径:', routePath);
          console.warn('权限列表:', Array.from(userPermissions || []));
          console.warn('匹配检查详情:', Array.from(userPermissions || []).map(p => ({
            perm: p,
            exact: p === routePath,
            routeStartsWith: routePath.startsWith(p + '/'),
            permStartsWith: p.startsWith(routePath + '/'),
            matched: pathMatchesUserPermission(routePath, p)
          })));
          ElMessage.error("您没有权限访问该页面");
          next(false);
          if (progressBar) VabProgress.done();
          return;
        }
      } else if (userRole === 'staff') {
        // 员工只按权限校验，不按部门自动授权；需在权限管理中手动分配
        if (to.path === '/' || to.path === '/index' || to.name === 'Index') {
          console.log('员工访问首页，允许');
        } else if (userPermissions && userPermissions.some(perm => perm && perm.startsWith('/'))) {
          // 有具体权限时，检查路由是否在权限列表中
          const routePath = to.path;
          const hasRoutePermission = hasAnyPathPermission(routePath, userPermissions);
          if (!hasRoutePermission) {
            console.warn('员工无该页面权限，拒绝访问:', routePath);
            ElMessage.error("您没有权限访问该页面，请在权限管理中设置");
            next(false);
            if (progressBar) VabProgress.done();
            return;
          }
          console.log('员工权限校验通过:', routePath);
        } else {
          // 无具体权限设置，仅允许首页
          console.warn('员工未设置权限，仅可访问首页');
          ElMessage.error("您没有权限访问该页面，请在权限管理中设置");
          next(false);
          if (progressBar) VabProgress.done();
          return;
        }
      }
      
      // 检查部门权限（在路由跳转时进行二次验证）
      // 注意：管理员（有admin权限）可以访问所有页面，不受部门限制
      // 部门限制只适用于服务人员（staff）
      if (to.meta && (to.meta.department || to.meta.deniedDepartments)) {
        // 获取用户权限
        const userPermissions = store.getters["user/permissions"];
        const hasAdminPermission = userPermissions && userPermissions.includes('admin');
        
        console.log('部门权限检查 - 路由:', to.path, '部门:', to.meta.department, '禁止部门:', to.meta.deniedDepartments);
        console.log('部门权限检查 - 用户角色:', userRole, '用户部门:', userDepartment);
        console.log('部门权限检查 - 用户权限:', userPermissions, '有admin权限:', hasAdminPermission);
        
        // 检查是否有具体权限设置（权限代码以 / 开头）
        const hasSpecificPermissions = userPermissions && userPermissions.some(perm => perm !== 'admin' && perm.startsWith('/'));
        
        // 如果有具体权限设置，需要检查路由路径是否在权限列表中
        if (hasSpecificPermissions) {
          const routePath = to.path;
          const hasRoutePermission = hasAnyPathPermission(routePath, userPermissions);
          
          if (!hasRoutePermission) {
            console.warn('路由路径不在权限列表中，拒绝访问:', routePath, '权限列表:', userPermissions);
            ElMessage.error("您没有权限访问该页面");
            next(false);
            if (progressBar) VabProgress.done();
            return;
          }
        }
        
        // 管理员或有admin权限的账号可以访问所有页面，不受部门限制
        // 但如果有具体权限设置，需要先通过上面的检查
        if (userRole === 'admin' || hasAdminPermission) {
          // 允许访问，跳过部门检查
          console.log('管理员权限，允许访问所有页面（不受部门限制）');
        } else if (userRole === 'staff') {
          // 员工只按权限校验，不按部门
          const staffPerms = store.getters["user/permissions"];
          const hasStaffSpecificPerms = staffPerms && staffPerms.some(perm => perm && perm.startsWith('/'));
          if (!hasStaffSpecificPerms) {
            if (to.path !== '/' && to.path !== '/index') {
              console.warn('员工未设置权限，拒绝访问:', to.path);
              ElMessage.error("您没有权限访问该页面，请在权限管理中设置");
              next(false);
              if (progressBar) VabProgress.done();
              return;
            }
          } else {
            const routePath = to.path;
            const hasRoutePermission = hasAnyPathPermission(routePath, staffPerms);
            if (!hasRoutePermission && to.path !== '/' && to.path !== '/index') {
              console.warn('员工无该页面权限，拒绝访问:', routePath);
              ElMessage.error("您没有权限访问该页面");
              next(false);
              if (progressBar) VabProgress.done();
              return;
            }
          }
        } else {
          // 其他角色，如果设置了部门且不匹配，拒绝访问
          // 但医师角色已经在上面处理了，这里不会执行到
          if (to.meta.department && userDepartment && userDepartment !== to.meta.department) {
            console.warn('部门不匹配，拒绝访问');
            ElMessage.error("您没有权限访问该页面");
            next(false); // 阻止跳转
            if (progressBar) VabProgress.done();
            return;
          } else {
            console.log('部门匹配或未设置部门，允许访问');
          }
        }
      }
      
      // 已解析为「空数组」的账号也需走「已有权限」分支，避免反复拉路由并误当成未登录态
      const _perms = store.getters["user/permissions"];
      const _role = store.getters["user/role"];
      const hasPermissions =
        Array.isArray(_perms) &&
        (_perms.length > 0 || (_role && String(_role).length > 0));
        
      // 检查路由是否已存在
      const routes = store.getters["routes/routes"];
      const hasRoutes = routes && routes.length > 0;
        
      // 如果已经有权限且路由已加载，检查路由是否匹配
      if (hasPermissions && hasRoutes) {
        // 先检查部门权限（如果路由有部门限制）
        if (to.meta && (to.meta.department || to.meta.deniedDepartments)) {
          const userPermissions = store.getters["user/permissions"];
          const hasAdminPermission = userPermissions && userPermissions.includes('admin');
          
          console.log('已有路由 - 部门权限检查 - 路由:', to.path, '部门:', to.meta.department, '禁止部门:', to.meta.deniedDepartments);
          console.log('已有路由 - 用户角色:', userRole, '用户部门:', userDepartment);
          console.log('已有路由 - 用户权限:', userPermissions, '有admin权限:', hasAdminPermission);
          
          // 管理员或有admin权限的账号可以访问所有页面，不受部门限制
          if (userRole === 'admin' || hasAdminPermission) {
            console.log('已有路由 - 管理员权限，允许访问所有页面（不受部门限制）');
          } else if (userRole === 'staff') {
            // 员工只按权限校验
            const staffPerms = store.getters["user/permissions"];
            const hasStaffSpecificPerms = staffPerms && staffPerms.some(perm => perm && perm.startsWith('/'));
            if (!hasStaffSpecificPerms) {
              if (to.path !== '/' && to.path !== '/index') {
                console.warn('已有路由 - 员工未设置权限，拒绝访问:', to.path);
                ElMessage.error("您没有权限访问该页面，请在权限管理中设置");
                next(false);
                if (progressBar) VabProgress.done();
                return;
              }
            } else {
              const routePath = to.path;
              const hasRoutePermission = hasAnyPathPermission(routePath, staffPerms);
              if (!hasRoutePermission && to.path !== '/' && to.path !== '/index') {
                console.warn('已有路由 - 员工无该页面权限，拒绝访问:', routePath);
                ElMessage.error("您没有权限访问该页面");
                next(false);
                if (progressBar) VabProgress.done();
                return;
              }
            }
          } else if (userDepartment && to.meta.department && userDepartment.trim() !== to.meta.department.trim()) {
            // 其他用户（如普通用户）需要部门匹配
            console.warn('已有路由 - 部门不匹配，拒绝访问');
            ElMessage.error("您没有权限访问该页面");
            next(false); // 阻止跳转
            if (progressBar) VabProgress.done();
            return;
          }
        }
        
        // 检查路由是否匹配
        let checkPath = to.path === '/' ? '/index' : to.path;
        const resolvedRoute = router.resolve(checkPath);
        console.log('检查路由匹配 - 路径:', checkPath, '匹配结果:', resolvedRoute.matched?.length || 0);
        
        if (resolvedRoute && resolvedRoute.matched && resolvedRoute.matched.length > 0) {
          // 路由已存在且匹配，再次确认权限（双重检查）
          const finalUserPermissions = store.getters["user/permissions"];
          const finalHasAdminPermission = finalUserPermissions && finalUserPermissions.includes('admin');
          const finalUserRole = store.getters["user/role"];
          
          // 检查是否有具体权限设置
          const hasSpecificPermissions = finalUserPermissions && finalUserPermissions.some(perm => perm !== 'admin' && perm.startsWith('/'));
          
          // 管理员始终可以访问所有页面
          if (finalUserRole === 'admin') {
            console.log('管理员访问，允许所有页面（路由匹配检查）');
            // 继续后续逻辑，不进行权限检查
          }
          // 如果有具体权限设置，需要检查路由路径是否在权限列表中
          // 但是首页（/ 和 /index）应该允许所有用户访问
          else if (hasSpecificPermissions && to.path !== '/' && to.path !== '/index' && to.path !== '/login') {
            const routePath = to.path;
            const hasRoutePermission = hasAnyPathPermission(routePath, finalUserPermissions);
            
            if (!hasRoutePermission) {
              console.warn('路由匹配但路径不在权限列表中，拒绝访问:', routePath);
              console.warn('权限列表:', Array.from(finalUserPermissions || []));
              ElMessage.error("您没有权限访问该页面");
              next(false);
              if (progressBar) VabProgress.done();
              return;
            } else {
              console.log('路由匹配且权限验证通过:', routePath);
            }
          } else if (hasSpecificPermissions && (to.path === '/' || to.path === '/index')) {
            // 首页允许访问，即使有具体权限设置
            console.log('访问首页，允许访问（不受权限限制）');
          }
          
          // 如果路由有部门限制，再次检查权限
          if (to.meta && (to.meta.department || to.meta.deniedDepartments)) {
            // 管理员（有admin权限）可以访问所有页面，不受部门限制
            // 但如果有具体权限设置，需要先通过上面的检查
            if (finalUserRole === 'admin' || finalHasAdminPermission) {
              console.log('路由匹配且管理员权限验证通过，直接放行（不受部门限制）');
            } else if (finalUserRole === 'staff') {
              // 员工只按权限校验
              const finalStaffPerms = store.getters["user/permissions"];
              const hasFinalStaffPerms = finalStaffPerms && finalStaffPerms.some(perm => perm && perm.startsWith('/'));
              if (!hasFinalStaffPerms) {
                if (to.path !== '/' && to.path !== '/index') {
                  console.warn('路由匹配但员工未设置权限，拒绝访问');
                  ElMessage.error("您没有权限访问该页面，请在权限管理中设置");
                  next(false);
                  if (progressBar) VabProgress.done();
                  return;
                }
              } else {
                const routePath = to.path;
                const hasRoutePerm = hasAnyPathPermission(routePath, finalStaffPerms);
                if (!hasRoutePerm && to.path !== '/' && to.path !== '/index') {
                  console.warn('路由匹配但员工无该页面权限，拒绝访问:', routePath);
                  ElMessage.error("您没有权限访问该页面");
                  next(false);
                  if (progressBar) VabProgress.done();
                  return;
                }
              }
            } else {
              // 其他角色需要部门匹配
              const finalUserDepartment = store.getters["user/department"] || localStorage.getItem('department');
              if (to.meta.department && finalUserDepartment && finalUserDepartment.trim() !== to.meta.department.trim()) {
                console.warn('路由匹配但部门不匹配，拒绝访问');
                ElMessage.error("您没有权限访问该页面");
                next(false);
                if (progressBar) VabProgress.done();
                return;
              } else {
                console.log('路由匹配且部门匹配，直接放行');
              }
            }
          }
          
          // 路由已存在且匹配，直接放行
          console.log('已有权限和路由，直接放行，路径:', checkPath);
          if (progressBar) VabProgress.done();
          // 如果路径是根路径，重定向到/index
          if (to.path === '/' || to.path === '') {
            next({ path: '/index', replace: true });
          } else {
            next();
          }
          return; // 重要：直接返回，避免继续执行下面的代码
        } else {
          // 路由不存在，需要重新加载路由
          console.warn('权限已存在但路由不匹配，需要重新加载路由');
          console.log('当前所有路由:', router.getRoutes().map(r => ({ path: r.path, name: r.name })));
        }
      } else if (hasPermissions && !hasRoutes) {
        // 有权限但路由未加载，需要加载路由
        console.warn('权限已存在但路由未加载，需要加载路由');
      }
      
      // 如果没有权限或路由未加载，继续执行下面的代码加载路由
      // 防止无限循环：检查是否正在加载路由
      const isLoadingRoutes = sessionStorage.getItem('isLoadingRoutes');
      const loadStartTime = sessionStorage.getItem('loadStartTime');
      const now = Date.now();
      
      // 如果加载超过5秒，强制放行避免卡死
      if (loadStartTime && now - parseInt(loadStartTime) > 5000) {
        console.warn('加载超时，强制放行');
        sessionStorage.removeItem('isLoadingRoutes');
        sessionStorage.removeItem('lastRoutePath');
        sessionStorage.removeItem('loadStartTime');
        if (!hasPermissions) {
          await store.dispatch("user/setPermissions", []);
        }
        // 如果没有路由，至少添加首页路由
        if (!hasRoutes) {
          router.addRoute({
            path: '/',
            component: Layout,
            redirect: '/index',
            children: [{
              path: 'index',
              name: 'Index',
              component: () => import('@/views/index/index.vue'),
              meta: { title: '宠物家首页', icon: 'home' }
            }]
          });
        }
        next();
        if (progressBar) VabProgress.done();
        return;
      }
      
      if (isLoadingRoutes === 'true') {
        // 如果正在加载路由，直接放行，避免重复加载
        console.warn('路由正在加载中，直接放行避免循环');
        next();
        return;
      }
      
      // 防止在同一路径上重复加载
      const lastRoutePath = sessionStorage.getItem('lastRoutePath');
      if (lastRoutePath === to.path) {
        console.warn('同一路径重复加载，直接放行');
        next();
        return;
      }
      
      // 防止 getUserInfo 被频繁调用（防抖）- 增加到5秒
      const lastGetUserInfoTime = sessionStorage.getItem('lastGetUserInfoTime');
      const currentTime = Date.now();
      if (lastGetUserInfoTime && currentTime - parseInt(lastGetUserInfoTime) < 5000) {
        console.warn('getUserInfo 调用过于频繁，使用已有权限');
        const existingPermissions = store.getters["user/permissions"];
        const safePerms = Array.isArray(existingPermissions) ? existingPermissions : [];
        if (!hasRoutes) {
          const quickRoutes = await store.dispatch("routes/setRoutes", safePerms);
          quickRoutes.forEach(route => router.addRoute(route));
          await nextTick();
        }
        sessionStorage.removeItem('isLoadingRoutes');
        sessionStorage.removeItem('lastRoutePath');
        if (progressBar) VabProgress.done();
        next();
        return;
      }
      
      try {
          // 标记正在加载路由
          sessionStorage.setItem('isLoadingRoutes', 'true');
          sessionStorage.setItem('lastRoutePath', to.path);
          sessionStorage.setItem('lastGetUserInfoTime', currentTime.toString());
          sessionStorage.setItem('loadStartTime', Date.now().toString());
          
          // 保存当前路由信息，用于检查是否仍然有效
          const currentRoutePath = to.path;
          
          let permissions;
          if (!loginInterception) {
            //settings.js loginInterception为false时，创建虚拟权限
            await store.dispatch("user/setPermissions", ["admin"]);
            permissions = ["admin"];
          } else {
            try {
              const existingPermissions = store.getters["user/permissions"];
              const existingRole = store.getters["user/role"];
              const syncedOnce =
                sessionStorage.getItem("adminPermissionsResolved") === "1";
              const permissionsAlreadyResolved =
                Array.isArray(existingPermissions) &&
                (existingPermissions.length > 0 ||
                  (syncedOnce &&
                    existingRole &&
                    String(existingRole).length > 0));

              if (permissionsAlreadyResolved) {
                console.log('已同步权限（含零权限），跳过 getUserInfo:', existingPermissions);
                permissions = existingPermissions;
              } else {
                permissions = await store.dispatch("user/getUserInfo");
                if (permissions === false) {
                  // getUserInfo 明确失败：通常是 token 失效/后端不可用。此时不要继续走权限流程，避免落到 /401 并循环报错。
                  console.warn("getUserInfo 返回 false，重置登录态并跳转登录页");
                  await store.dispatch("user/resetAccessToken");
                  sessionStorage.removeItem("isLoadingRoutes");
                  sessionStorage.removeItem("lastRoutePath");
                  sessionStorage.removeItem("lastGetUserInfoTime");
                  sessionStorage.removeItem("loadStartTime");
                  next({ path: "/login", replace: true });
                  if (progressBar) VabProgress.done();
                  return;
                }
                if (!permissions || !Array.isArray(permissions)) {
                  console.warn('getUserInfo 未返回有效权限数组');
                  permissions = [];
                  await store.dispatch("user/setPermissions", permissions);
                }
              }
            } catch (error) {
              console.error('getUserInfo 调用失败:', error);
              // 检查是否已经有权限，如果有就使用已有权限，不跳转登录页
              const existingPermissions = store.getters["user/permissions"];
              if (existingPermissions && existingPermissions.length > 0) {
                console.warn('getUserInfo 调用失败，但已有权限，使用已有权限:', existingPermissions);
                permissions = existingPermissions;
              } else {
                // 检查是否storage中还有token
                const storageToken = localStorage.getItem('vue-admin-better-2024') || sessionStorage.getItem('vue-admin-better-2024');
                const storeToken = store.getters["user/accessToken"];
                const hasToken = storageToken || storeToken;
                
                if (!hasToken) {
                  // 完全没有token，只有在明确是401错误时才跳转登录页
                  if (error.response && error.response.status === 401) {
                    console.error('getUserInfo 返回 401 且完全无token，Token无效，跳转到登录页');
                  console.error('当前路由路径:', to.path);
                  store.dispatch("user/resetAccessToken");
                  sessionStorage.removeItem('isLoadingRoutes');
                  sessionStorage.removeItem('lastRoutePath');
                  sessionStorage.removeItem('loadStartTime');
                  next({ path: "/login", replace: true });
                  if (progressBar) VabProgress.done();
                  return;
                } else {
                    // 其他错误且无token，也跳转登录页
                    console.error('getUserInfo 调用失败且无token，跳转到登录页');
                    store.dispatch("user/resetAccessToken");
                    sessionStorage.removeItem('isLoadingRoutes');
                    sessionStorage.removeItem('lastRoutePath');
                    sessionStorage.removeItem('loadStartTime');
                    next({ path: "/login", replace: true });
                    if (progressBar) VabProgress.done();
                    return;
                  }
                  } else {
                  console.warn('getUserInfo 调用失败，但有 token，按角色降级处理（非 admin 不提升为 admin）');
                  console.warn('错误详情:', error.response?.status, error.message);
                  if (!storeToken && storageToken) {
                    store.commit('user/setAccessToken', storageToken);
                  }
                  const savedRole = localStorage.getItem('userRole');
                  if (savedRole) {
                    store.commit('user/setRole', savedRole);
                  }
                  if (savedRole === 'admin') {
                    permissions = ['admin'];
                    await store.dispatch("user/setPermissions", permissions);
                  } else {
                    permissions = [];
                    await store.dispatch("user/setPermissions", permissions);
                  }
                  console.log('已设置降级权限，继续加载路由');
                }
              }
            }
          }

          let accessRoutes = [];
          
          if (!permissions || !Array.isArray(permissions)) {
            console.warn('权限无效，置为空数组');
            permissions = [];
            await store.dispatch("user/setPermissions", permissions);
          }
          
          try {
            if (authentication === "intelligence") {
              accessRoutes = await store.dispatch(
                "routes/setRoutes",
                permissions
              );
            } else if (authentication === "all") {
              accessRoutes = await store.dispatch("routes/setAllRoutes");
            }
          } catch (routeError) {
            console.error("路由设置失败:", routeError);
            // 如果路由设置失败，尝试使用默认路由
            accessRoutes = [];
          }

          // 确保accessRoutes是数组
          if (!Array.isArray(accessRoutes)) {
            console.error("路由数据格式错误:", accessRoutes);
            accessRoutes = [];
          }
          
          // 注意：不再为服务人员单独合并路由
          // 因为 filterAsyncRoutes 已经为服务人员返回了所有路由（用于显示完整菜单）
          // 如果再次合并 setupRoutesByRole 返回的路由，会导致路由重复
          const currentUserRole = store.getters["user/role"];
          const currentUserDepartment = store.getters["user/department"] || localStorage.getItem('department');
          if (currentUserRole === 'staff') {
            // 确保部门信息已保存到 localStorage
            if (currentUserDepartment && !localStorage.getItem('department')) {
              localStorage.setItem('department', currentUserDepartment);
            }
            console.log('服务人员路由已通过 filterAsyncRoutes 获取，无需额外合并，部门:', currentUserDepartment);
          }
          
          console.log('路由守卫 - 获取到的路由数量:', accessRoutes.length);
          
          // 如果路由为空，至少添加首页路由
          if (accessRoutes.length === 0) {
            console.warn('路由为空，添加默认首页路由');
            accessRoutes = [{
              path: '/',
              component: Layout,
              redirect: '/index',
              children: [
                {
                  path: 'index',
                  name: 'Index',
                  component: () => import('@/views/index/index.vue'),
                  meta: { title: '宠物家首页', icon: 'home' }
                }
              ]
            }];
          }

          // 确保首页路由存在（如果被过滤掉了，需要手动添加）
          const hasIndexRoute = accessRoutes.some(route => 
            route.path === '/' || route.path === '/index' || 
            (route.children && route.children.some(child => child.path === 'index'))
          );
          
          if (!hasIndexRoute) {
            console.warn('首页路由不存在，添加到路由列表');
            accessRoutes.unshift({
              path: '/',
              component: Layout,
              redirect: '/index',
              children: [
                {
                  path: 'index',
                  name: 'Index',
                  component: () => import('@/views/index/index.vue'),
                  meta: { title: '宠物家首页', icon: 'home' }
                }
              ]
            });
          }
          
          // 添加路由到路由器（去重，避免重复添加）
          // 注意：对于嵌套路由，需要检查父路由和子路由
          accessRoutes.forEach((item) => {
            // 对于有子路由的父路由，检查子路由是否已存在
            if (item.children && item.children.length > 0) {
              // 检查父路由是否已存在
              const parentExists = item.name && router.hasRoute(item.name);
              if (parentExists) {
                console.warn('父路由已存在，跳过添加:', item.name, item.path);
                return;
              }
              // 对于嵌套路由，不检查子路由（因为子路由可能在不同的父路由下）
              // 只检查父路由名称，如果父路由不存在就添加
              // 这样可以避免误判导致路由未添加
            } else {
              // 对于没有子路由的路由，检查路由名称或路径
              if (item.name && router.hasRoute(item.name)) {
                console.warn('路由已存在，跳过添加:', item.name, item.path);
                return;
              }
              // 检查路径是否已存在（排除根路径，因为根路径可能有多个）
              if (item.path !== '/' && item.path !== '/index') {
                const existingRoute = router.getRoutes().find(r => r.path === item.path);
                if (existingRoute) {
                  console.warn('路由路径已存在，跳过添加:', item.path);
                  return;
                }
              }
            }
            router.addRoute(item);
            console.log('已添加路由:', item.path, 'name:', item.name, 'children:', item.children?.length || 0);
          });
          
          // 使用 nextTick 确保路由添加完成后再更新 store，避免 DOM 更新冲突
          await nextTick();
          
          // 更新 store 中的路由，确保侧边栏能显示菜单
          if (accessRoutes.length > 0) {
            store.commit('routes/setRoutes', accessRoutes);
            console.log('路由已更新到 store，路由数量:', accessRoutes.length);
          }
          
          // 等待路由添加完成，确保路由已注册（增加等待时间）
          // 对于服务人员，可能需要更多时间让路由完全注册
          const waitUserRole = store.getters["user/role"];
          const waitTime = waitUserRole === 'staff' ? 300 : 150;
          await new Promise(resolve => setTimeout(resolve, waitTime));
          
          // 验证路由是否真的添加成功
          const allRoutes = router.getRoutes();
          const indexRoute = allRoutes.find(r => r.path === '/' && r.children?.some(c => c.path === 'index'));
          console.log('路由验证 - 首页路由是否存在:', !!indexRoute);
          if (indexRoute) {
            console.log('首页路由详情:', {
              path: indexRoute.path,
              children: indexRoute.children?.map(c => ({ path: c.path, name: c.name }))
            });
          }
          
          // 验证服务人员入口路由是否添加成功
          const myScheduleRoute = allRoutes.find(r => r.path === '/my-schedule' || r.name === 'MySchedule');
          const verifyRoute = allRoutes.find(r => r.path === '/verify' || r.name === 'VerifyRoot');
          console.log('服务人员入口路由检查:', {
            mySchedule: !!myScheduleRoute,
            verify: !!verifyRoute,
          });
          
          // 确保权限已保存
          const finalPermissions = store.getters["user/permissions"];
          if (!finalPermissions || finalPermissions.length === 0) {
            console.warn('权限丢失，重新设置');
            await store.dispatch("user/setPermissions", permissions);
          }
          
          // 清除加载标记
          sessionStorage.removeItem('isLoadingRoutes');
          sessionStorage.removeItem('lastRoutePath');
          sessionStorage.removeItem('lastGetUserInfoTime');
          sessionStorage.removeItem('loadStartTime');
          
          // 检查目标路径，如果是根路径或首页，确保正确重定向
          let targetPath = to.path;
          if (targetPath === '/' || targetPath === '') {
            targetPath = '/index';
          }
          
          // 检查路由是否匹配
          const resolvedRoute = router.resolve(targetPath);
          console.log('路由匹配检查 - 目标路径:', targetPath);
          console.log('路由匹配检查 - 匹配结果:', resolvedRoute.matched?.length || 0);
          
          // 注意：不再为服务人员自动重定向到服务页面
          // 服务人员访问首页时，保持在首页，可以看到完整的后台管理界面
          // 但只能访问自己部门对应的服务页面，其他页面点击时会提示"没有权限"
          if (targetPath === "/index") {
            const userRole = store.getters["user/role"];
            const userDepartment = store.getters["user/department"] || localStorage.getItem('department');
            console.log('访问首页，用户角色:', userRole, '用户部门:', userDepartment);
            // 不再重定向，让服务人员也看到首页
          }
          
          // 如果路由不匹配，强制重定向到首页
          if (!resolvedRoute || !resolvedRoute.matched || resolvedRoute.matched.length === 0) {
            console.warn('路由不匹配，强制重定向到首页');
            console.log('目标路径:', targetPath);
            console.log('当前所有路由:', router.getRoutes().map(r => ({ 
              path: r.path, 
              name: r.name,
              children: r.children?.map(c => ({ path: c.path, name: c.name }))
            })));
            
            // 如果是服务人员访问首页，确保路由已加载
            const checkUserRole = store.getters["user/role"];
            const checkUserDepartment = store.getters["user/department"] || localStorage.getItem('department');
            if (checkUserRole === 'staff' && targetPath === '/index') {
              // 再次等待一下，确保路由完全加载
              console.log('服务人员路由不匹配，等待路由加载...');
              await new Promise(resolve => setTimeout(resolve, 200));
              // 再次尝试解析路由
              const retryResolvedRoute = router.resolve(targetPath);
              if (retryResolvedRoute && retryResolvedRoute.matched && retryResolvedRoute.matched.length > 0) {
                console.log('重试后路由匹配成功');
                if (progressBar) VabProgress.done();
                next({ path: targetPath, replace: true });
                return;
              }
            }
            
            if (progressBar) VabProgress.done();
            next({ path: '/index', replace: true });
            return;
          }
          
          // 如果目标路径和当前路径不同，使用replace方式跳转
          if (targetPath !== to.path) {
            console.log('路径修正，从', to.path, '跳转到', targetPath);
            if (progressBar) VabProgress.done();
            next({ path: targetPath, replace: true });
            return;
          }
          
          // 路由已匹配，确保路由完全准备好后再放行
          console.log('路由已匹配，准备放行，目标路径:', targetPath);
          console.log('路由匹配详情:', resolvedRoute.matched.map(m => ({ path: m.path, name: m.name })));
          
          // 再次等待，确保路由完全注册和组件chunk准备就绪
          await nextTick();
          await new Promise(resolve => setTimeout(resolve, 200));
          
          // 清除加载标记，确保不会影响后续导航
          sessionStorage.removeItem('isLoadingRoutes');
          sessionStorage.removeItem('lastRoutePath');
          
          if (progressBar) VabProgress.done();
          
          // 使用 replace: true 确保路由正确导航，避免重复导航
          // 如果路由匹配失败（比如组件chunk加载失败），会触发路由守卫的catch块
          try {
            next({ path: targetPath, replace: true });
          } catch (navError) {
            console.error('路由导航失败:', navError);
            // 如果导航失败，尝试强制刷新页面
            if (navError.message && navError.message.includes('chunk')) {
              console.warn('检测到chunk加载失败，尝试刷新页面');
              window.location.href = targetPath;
            } else {
              // 其他错误，重定向到首页
              next({ path: '/index', replace: true });
            }
          }
        } catch (error) {
          console.error("路由守卫错误:", error);
          sessionStorage.removeItem('isLoadingRoutes');
          sessionStorage.removeItem('lastRoutePath');
          sessionStorage.removeItem('lastGetUserInfoTime');
          
          // 检查是否已经有权限和token（包括storage中的token）
          const existingToken = store.getters["user/accessToken"];
          const existingTokenFromStorage = localStorage.getItem('vue-admin-better-2024') || sessionStorage.getItem('vue-admin-better-2024');
          const existingPermissions = store.getters["user/permissions"];
          const hasToken = existingToken || existingTokenFromStorage;
          const hasExistingAuth = hasToken && existingPermissions && existingPermissions.length > 0;
          
          // 如果storage中有token但store中没有，恢复token
          if (!existingToken && existingTokenFromStorage) {
            console.warn('catch块 - storage中有token但store中没有，恢复token');
            store.commit('user/setAccessToken', existingTokenFromStorage);
          }
          
          // 如果storage中有token但没有权限：仅 admin 在异常场景下可降级为通配 admin，其余角色不自动提权
          if (hasToken && (!existingPermissions || existingPermissions.length === 0)) {
            const savedRole = localStorage.getItem('userRole');
            if (savedRole === 'admin') {
              console.warn('catch块 - admin 无权限缓存，恢复 admin 通配');
              store.commit('user/setPermissions', ['admin']);
              store.commit('user/setRole', savedRole);
            }
          }
          
          const finalPermissions = store.getters["user/permissions"];
          const finalRole = store.getters["user/role"];
          const finalHasAuth =
            hasToken &&
            ((finalPermissions && finalPermissions.length > 0) ||
              (finalRole && String(finalRole).length > 0));
          
          // 检查是否是网络错误或401错误
          if (error.response && error.response.status === 401) {
            // 只有在完全没有token时才跳转登录页
            if (!hasToken) {
              console.error('认证失败且完全无token，清除token并跳转到登录页');
              await store.dispatch("user/resetAccessToken");
              next({ path: "/login", replace: true });
              if (progressBar) VabProgress.done();
              return;
            } else {
              console.warn('API返回401，但有token存在，尝试继续访问（不自动注入 admin）');
              if (!finalHasAuth) {
                const savedRole = localStorage.getItem('userRole');
                if (savedRole === 'admin') {
                  store.commit('user/setPermissions', ['admin']);
                }
                if (savedRole) {
                  store.commit('user/setRole', savedRole);
                }
              }
              if (progressBar) VabProgress.done();
              next();
              return;
            }
          }
          
          if (hasToken) {
            if (finalHasAuth) {
              console.warn('路由守卫错误，但有token和已解析身份，继续访问');
            } else {
              console.warn('路由守卫错误，有token但无已解析身份，不自动注入 admin');
              const savedRole = localStorage.getItem('userRole');
              if (savedRole === 'admin') {
                store.commit('user/setPermissions', ['admin']);
              }
              if (savedRole) {
                store.commit('user/setRole', savedRole);
              }
            }
            if (progressBar) VabProgress.done();
            next();
          } else {
            // 完全没有token，跳转登录页
            console.warn('路由守卫错误且完全无token，跳转登录页');
            if (progressBar) VabProgress.done();
            next({ path: "/login", replace: true });
          }
        }
      }
  } else {
    // 没有token的情况 - 再次检查storage，确保准确性
    const finalTokenCheck = localStorage.getItem('vue-admin-better-2024') || sessionStorage.getItem('vue-admin-better-2024');
    
    console.error('=== 无token检查 ===');
    console.error('目标路径:', to.path);
    console.error('store token:', !!hasToken);
    console.error('storage token:', !!finalTokenCheck);
    console.error('路由白名单:', routesWhiteList);
    console.error('是否在白名单:', routesWhiteList.indexOf(to.path) !== -1);
    
    // 如果storage中有token，说明store状态丢失，恢复token并继续
    // 更严格的token检查：确保token不是空字符串
    const validToken = finalTokenCheck && finalTokenCheck.trim && finalTokenCheck.trim().length > 0;
    
    if (validToken) {
      console.warn('store中无token但storage中有有效token，恢复token并继续');
      store.commit('user/setAccessToken', finalTokenCheck);
      
      // 尝试恢复角色和权限信息
      const savedRole = localStorage.getItem('userRole');
      if (savedRole) {
        store.commit('user/setRole', savedRole);
        console.log('已恢复角色:', savedRole);
      }
      
      const userRole = store.getters["user/role"];
      const userPermissions = store.getters["user/permissions"];
      
      console.log('恢复token后 - 用户角色:', userRole, '用户权限:', userPermissions);
      
      if (userPermissions && userPermissions.length > 0) {
        console.log('恢复token后已有权限，直接放行');
        if (progressBar) VabProgress.done();
        next();
        return;
      }
      
      if (savedRole === 'admin') {
        store.commit('user/setPermissions', ['admin']);
        if (progressBar) VabProgress.done();
        next();
        return;
      }
      
      next({ path: to.fullPath, replace: true });
      if (progressBar) VabProgress.done();
      return;
    }
    
    // 真正没有token的情况
    if (!validToken) {
      // 检查是否有保存的路由信息（页面刷新场景）
      const savedRoute = sessionStorage.getItem('currentRoute');
      if (savedRoute) {
        try {
          const routeInfo = JSON.parse(savedRoute);
          // 如果目标路由不在白名单中，仍然需要登录
          if (routesWhiteList.indexOf(to.path) !== -1) {
            console.log('无token但路径在白名单，允许访问');
            next();
          } else {
            // 检查当前路径是否需要登录
            console.warn('无token且路径不在白名单，跳转登录页');
            if (recordRoute) {
              next(`/login?redirect=${to.path}`);
            } else {
              next("/login");
            }
          }
        } catch (e) {
          console.error('解析保存的路由信息失败:', e);
          // 继续正常的登录检查流程
          if (routesWhiteList.indexOf(to.path) !== -1) {
            console.log('无token但路径在白名单，允许访问');
            next();
          } else {
            console.warn('无token且路径不在白名单，跳转登录页');
            if (recordRoute) {
              next(`/login?redirect=${to.path}`);
            } else {
              next("/login");
            }
          }
        }
      } else {
        if (routesWhiteList.indexOf(to.path) !== -1) {
          console.log('无token但路径在白名单，允许访问');
          next();
        } else {
          console.warn('无token且路径不在白名单，跳转登录页');
          if (recordRoute) {
            next(`/login?redirect=${to.path}`);
          } else {
            next("/login");
          }
        }
      }
    } else {
      // storage中有token，不应该到这里，但如果到了，说明逻辑有问题
      console.error('逻辑错误：storage中有token但进入了无token分支');
      // 恢复token并继续
      store.commit('user/setAccessToken', finalTokenCheck);
      if (progressBar) VabProgress.done();
      next();
    }

    if (progressBar) VabProgress.done();
  }
  document.title = getPageTitle(to.meta.title);
});

router.afterEach(() => {
  if (progressBar) VabProgress.done();
});
