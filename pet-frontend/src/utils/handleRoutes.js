import { canAccessRoute } from "@/utils/routePermission"

/**
 * all 模式：渲染后端返回的路由
 */
export function convertRouter(asyncRoutes) {
  if (!asyncRoutes || !Array.isArray(asyncRoutes)) {
    console.warn("后端返回的路由格式不正确或为空")
    return []
  }

  return asyncRoutes
    .map((route) => {
      if (!route) return null

      if (route.component) {
        if (route.component === "Layout") {
          route.component = () => import("@/layouts")
        } else if (route.component === "EmptyLayout") {
          route.component = () => import("@/layouts/EmptyLayout")
        } else {
          try {
            const index = route.component.indexOf("views")
            const path =
              index > 0 ? route.component.slice(index) : `views/${route.component}`
            route.component = () =>
              import(`@/${path}`).catch((err) => {
                console.error(`路由组件加载失败: @/${path}`, err)
                return import("@/views/404")
              })
          } catch (err) {
            console.error(`路由组件解析失败: ${route.component}`, err)
            route.component = () => import("@/views/404")
          }
        }
      }

      if (route.children) {
        if (Array.isArray(route.children) && route.children.length) {
          route.children = convertRouter(route.children)
          route.children = route.children.filter((child) => child !== null)
        }
        if (!route.children || route.children.length === 0) delete route.children
      }

      return route
    })
    .filter((route) => route !== null)
}

/**
 * 菜单可见性判断（访问控制统一走 canAccessRoute，默认拒绝）
 */
function hasMenuPermission(permissions, route, userRole, fullPath) {
  const routePath = fullPath || route.path || ""

  // 平台管理员不显示「我的排班」入口
  if (userRole === "admin" && routePath.startsWith("/my-schedule")) {
    return false
  }

  if (route.meta && route.meta.requireSuperAdmin) {
    return userRole === "admin"
  }

  return canAccessRoute(routePath, route.meta, userRole, permissions)
}

/**
 * intelligence 模式：根据 permissions 拦截路由
 */
export function filterAsyncRoutes(
  routes,
  permissions,
  userRole = "",
  userDepartment = "",
  parentPath = ""
) {
  if (!routes || !Array.isArray(routes)) return []
  if (!permissions || !Array.isArray(permissions)) return []

  const finallyRoutes = []
  routes.forEach((route) => {
    if (!route) return

    const item = { ...route }

    // 构建完整路径用于权限检查
    let fullPath = item.path
    if (fullPath && !fullPath.startsWith("/")) {
      fullPath = parentPath
        ? parentPath === "/"
          ? "/" + fullPath
          : parentPath + "/" + fullPath
        : "/" + fullPath
    } else if (!fullPath) {
      fullPath = parentPath || "/"
    }

    // 有子路由的父路由：先过滤子路由，父路由随子路由可见性显示
    if (item.children && Array.isArray(item.children) && item.children.length > 0) {
      const filteredChildren = filterAsyncRoutes(
        item.children,
        permissions,
        userRole,
        userDepartment,
        fullPath
      )

      if (filteredChildren.length > 0) {
        item.children = filteredChildren
        finallyRoutes.push(item)
        return
      }

      // 子路由全部被过滤，父路由本身有权限时保留（作为独立入口）
      if (hasMenuPermission(permissions, item, userRole, fullPath)) {
        item.children = []
        finallyRoutes.push(item)
      }
      return
    }

    if (hasMenuPermission(permissions, item, userRole, fullPath)) {
      finallyRoutes.push(item)
    }
  })
  return finallyRoutes
}
