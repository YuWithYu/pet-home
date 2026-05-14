import { pathMatchesUserPermission } from "@/utils/routePermission"

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description all模式渲染后端返回路由
 * @param constantRoutes
 * @returns {*}
 */
export function convertRouter(asyncRoutes) {
  // 处理空值情况
  if (!asyncRoutes || !Array.isArray(asyncRoutes)) {
    console.warn('后端返回的路由格式不正确或为空')
    return []
  }

  return asyncRoutes
    .map((route) => {
      if (!route) return null

      if (route.component) {
        if (route.component === 'Layout') {
          route.component = () => import('@/layouts')
        } else if (route.component === 'EmptyLayout') {
          route.component = () => import('@/layouts/EmptyLayout')
        } else {
          try {
            const index = route.component.indexOf('views')
            const path = index > 0 ? route.component.slice(index) : `views/${route.component}`
            route.component = () =>
              import(`@/${path}`).catch((err) => {
                console.error(`路由组件加载失败: @/${path}`, err)
                return import('@/views/404')
              })
          } catch (err) {
            console.error(`路由组件解析失败: ${route.component}`, err)
            route.component = () => import('@/views/404')
          }
        }
      }

      if (route.children) {
        if (Array.isArray(route.children) && route.children.length) {
          route.children = convertRouter(route.children)
          // 过滤掉空路由
          route.children = route.children.filter((child) => child !== null)
        }
        if (!route.children || route.children.length === 0) delete route.children
      }

      return route
    })
    .filter((route) => route !== null) // 过滤掉无效路由
}

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description 判断当前路由是否包含权限
 * @param permissions
 * @param route
 * @param userRole 用户角色（admin 或 staff）
 * @param userDepartment 用户部门
 * @returns {boolean|*}
 */
function hasPermission(permissions, route, userRole, userDepartment, fullPath = null) {
  // 确保permissions是数组
  if (!permissions || !Array.isArray(permissions)) {
    return false
  }

  const routePath = fullPath || route.path || route.fullPath || ''
  // 平台管理员不显示「我的排班」
  if (userRole === 'admin' && routePath.startsWith('/my-schedule')) {
    return false
  }

  // 需要管理员的路由，仅 admin 可见
  if (route.meta && route.meta.requireSuperAdmin) {
    return userRole === 'admin'
  }
  // 管理员始终拥有所有权限
  if (userRole === 'admin') {
    return true
  }
  
  // 检查是否有具体权限设置（权限代码以 / 开头）
  const hasSpecificPermissions = permissions.some(perm => perm !== 'admin' && perm.startsWith('/'))

  // 员工只按权限显示菜单，不按部门自动授权
  if (userRole === 'staff') {
    if (route.path === '/' || route.path === '/index' || route.name === 'Index') {
      return true
    }
    if (!hasSpecificPermissions) {
      // 未在权限管理中设置，只显示首页
      return false
    }
    // 有具体权限设置，继续下面的权限检查
  }

  // 检查是否有 admin 权限（兼容旧系统）
  const hasAdminPermission = permissions.includes('admin')
  
  // 如果权限列表中只有 admin，说明没有设置具体权限，允许访问所有页面
  // 如果权限列表中有具体的权限代码，需要检查路由路径是否在权限列表中
  // hasSpecificPermissions 已在上面声明，这里直接使用
  
  // 如果有具体权限设置，必须通过权限检查才能显示
  if (hasSpecificPermissions) {
    // 有具体权限设置，需要检查路由路径是否在权限列表中
    // 使用传入的完整路径，如果没有则使用 route.path
    let routePath = fullPath || route.path || route.fullPath
    
    // 确保路径以 / 开头
    if (routePath && !routePath.startsWith('/')) {
      routePath = '/' + routePath
    }
    
    // 首页始终允许访问
    if (routePath === '/' || routePath === '/index') {
      return true
    }
    
    if (routePath && routePath !== '/' && routePath !== '/index') {
      const hasRoutePermission = permissions.some(perm => pathMatchesUserPermission(routePath, perm))
      
      // 如果路由路径不在权限列表中，拒绝访问
      if (!hasRoutePermission) {
        console.warn('路由过滤 - 权限检查失败');
        console.warn('路由路径:', routePath);
        console.warn('权限列表:', Array.from(permissions));
        console.warn('匹配检查:', Array.from(permissions).map(p => ({
          perm: p,
          exact: p === routePath,
          routeStartsWith: routePath.startsWith(p + '/'),
          permStartsWith: p.startsWith(routePath + '/'),
          pathMatch: pathMatchesUserPermission(routePath, p)
        })));
        return false
      } else {
        console.log('路由过滤 - 权限检查通过:', routePath);
        // 如果有具体权限设置且路径已通过检查，直接返回 true，跳过后续的 meta.permissions 检查
        return true
      }
    }
    // 如果有具体权限设置，但路径是 / 或 /index，允许访问
    if (routePath === '/' || routePath === '/index') {
      return true
    }
    // 其他情况（路径为空等），返回 false
    return false
  }
  
  // 如果没有具体权限设置（只有admin），或者路由路径在权限列表中，允许访问

  // 检查基础权限（兼容旧系统）
  // 注意：如果有具体权限设置，上面的逻辑已经返回 true 或 false，不会执行到这里
  // 但是为了安全，我们仍然检查 meta.permissions，但如果有具体权限设置，允许通过
  if (route.meta && route.meta.permissions) {
    // 如果有具体权限设置，说明已经通过路径权限检查，允许通过 meta.permissions 检查
    if (hasSpecificPermissions) {
      console.log('路由过滤 - 有具体权限设置，跳过 meta.permissions 检查:', fullPath || route.path);
      // 允许通过，继续后续检查
    } else {
      // 没有具体权限设置，使用 meta.permissions 检查
      const hasBasicPermission = permissions.some((role) => route.meta.permissions.includes(role))
      if (!hasBasicPermission) {
        console.log('路由过滤 - meta.permissions 检查失败:', fullPath || route.path, '需要的权限:', route.meta.permissions, '用户权限:', permissions);
        return false
      }
    }
  }

  // 检查部门权限（对于管理员）
  if (route.meta && route.meta.department) {
    // 管理员（admin权限）也可以访问所有页面，不受部门限制
    if (hasAdminPermission) {
      return true
    }
    // 其他管理员角色，如果有部门限制，需要匹配
    if (userDepartment && userDepartment !== route.meta.department) {
      return false
    }
  }

  // 既无 admin 通配、也无路径级权限时，不再默认放行（避免「零权限」仍能看到大量无 meta 的路由）
  if (!hasAdminPermission && !hasSpecificPermissions) {
    return false
  }

  // 没有具体权限设置，且没有部门限制的路由，允许访问（通常对应持有 admin 通配）
  return true
}

/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description intelligence模式根据permissions数组拦截路由
 * @param routes
 * @param permissions
 * @param userRole 用户角色（admin 或 staff）
 * @param userDepartment 用户部门
 * @returns {[]}
 */
export function filterAsyncRoutes(routes, permissions, userRole = 'admin', userDepartment = '', parentPath = '') {
  // 处理无效参数
  if (!routes || !Array.isArray(routes)) {
    return []
  }

  if (!permissions || !Array.isArray(permissions)) {
    return []
  }

  const finallyRoutes = []
  routes.forEach((route) => {
    if (!route) return

    const item = { ...route }
    
    // 构建完整路径用于权限检查
    let fullPath = item.path
    if (fullPath && !fullPath.startsWith('/')) {
      // 如果是相对路径，需要拼接父路径
      if (parentPath) {
        fullPath = parentPath === '/' ? '/' + fullPath : parentPath + '/' + fullPath
      } else {
        fullPath = '/' + fullPath
      }
    } else if (!fullPath) {
      fullPath = parentPath || '/'
    }
    
    // 临时设置完整路径到路由对象，供 hasPermission 使用
    const originalPath = item.path
    item.fullPath = fullPath
    
    // 对于有子路由的父路由，先过滤子路由
    if (item.children && Array.isArray(item.children) && item.children.length > 0) {
      // 先过滤子路由
      const filteredChildren = filterAsyncRoutes(item.children, permissions, userRole, userDepartment, fullPath)
      
      // 如果子路由被过滤后不为空，说明有子路由有权限，父路由也应该显示
      if (filteredChildren.length > 0) {
        item.children = filteredChildren
        console.log('路由过滤 - 父路由有子路由权限，添加父路由:', fullPath, '子路由数量:', filteredChildren.length);
        // 恢复原始路径
        item.path = originalPath
        delete item.fullPath
        finallyRoutes.push(item)
        return
      }
      
      // 如果子路由都被过滤掉了，检查父路由本身是否有权限
      const hasCurrentPermission = hasPermission(permissions, item, userRole, userDepartment, fullPath)
      if (hasCurrentPermission) {
        // 父路由本身有权限，即使没有子路由，也添加父路由
        item.children = [] // 清空子路由
        console.log('路由过滤 - 父路由本身有权限但子路由被过滤，添加父路由:', fullPath);
        item.path = originalPath
        delete item.fullPath
        finallyRoutes.push(item)
        return
      } else {
        // 父路由和子路由都没有权限，移除
        console.log('路由过滤 - 父路由和子路由都没有权限，移除:', fullPath);
        return
      }
    }
    
    // 如果没有子路由，检查当前路由本身是否有权限
    const hasCurrentPermission = hasPermission(permissions, item, userRole, userDepartment, fullPath)
    if (hasCurrentPermission) {
      console.log('路由过滤 - 路由本身有权限，添加:', fullPath);
      // 恢复原始路径
      item.path = originalPath
      delete item.fullPath
      finallyRoutes.push(item)
    } else {
      console.log('路由过滤 - 路由没有权限，移除:', fullPath);
    }
  })
  return finallyRoutes
}
