/**
 * 页面路径与「权限管理」里保存的 permissionCode 是否匹配。
 * 兼容：预约订单路由聚合、订单核销/门店客服从 service-workbench 迁出、末尾斜杠等。
 */
export function pathMatchesUserPermission(routePath, perm) {
  if (!routePath || !perm || !perm.startsWith("/")) return false
  const norm = (p) => {
    if (!p || typeof p !== "string") return ""
    const s = p.trim()
    if (s.length > 1 && s.endsWith("/")) return s.slice(0, -1)
    return s
  }
  const r = norm(routePath)
  const p = norm(perm)
  if (!r || !p) return false

  if (p === r) return true
  if (r.startsWith(p + "/")) return true
  if (p.startsWith(r + "/")) return true

  const orderPaths = new Set([
    "/service-orders",
    "/service-orders/appointment-orders",
    "/service-orders/litter-service-orders",
    "/service-orders/grooming-service-orders",
    "/service-orders/hospital-service-orders",
  ])
  if (orderPaths.has(r) && orderPaths.has(p)) return true

  const verifyPaths = new Set(["/verify", "/service-workbench/verify"])
  if (verifyPaths.has(r) && verifyPaths.has(p)) return true

  const outletPaths = new Set([
    "/outlet-customer-chat",
    "/service-workbench/outlet-customer-chat",
  ])
  if (outletPaths.has(r) && outletPaths.has(p)) return true

  return false
}

/**
 * 当前用户权限数组中是否包含对 routePath 的访问权。
 */
export function hasAnyPathPermission(routePath, userPermissions) {
  if (!routePath || !Array.isArray(userPermissions)) return false
  return userPermissions.some((perm) =>
    pathMatchesUserPermission(routePath, perm)
  )
}

/**
 * 统一访问控制入口（路由守卫与菜单过滤共用），默认拒绝：
 * 1. admin 角色或持有 admin 通配权限 → 全放行
 * 2. 首页对所有登录用户放行
 * 3. 持有路径级权限（以 / 开头）→ 按路径精确匹配
 * 4. 旧数据兼容：permissions 中存的是角色名时，与路由 meta.permissions 匹配
 * 5. 其余一律拒绝
 */
export function canAccessRoute(routePath, routeMeta, userRole, permissions) {
  const perms = Array.isArray(permissions) ? permissions : []

  if (userRole === "admin" || perms.includes("admin")) return true
  if (routePath === "/" || routePath === "/index") return true

  const hasSpecificPermissions = perms.some(
    (p) => p && p !== "admin" && p.startsWith("/")
  )
  if (hasSpecificPermissions) {
    return hasAnyPathPermission(routePath, perms)
  }

  if (routeMeta && Array.isArray(routeMeta.permissions)) {
    if (perms.some((p) => routeMeta.permissions.includes(p))) return true
  }

  return false
}
