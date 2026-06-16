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
  if (r === "/service-orders/appointment-orders" && p === "/service-orders") return true
  if (p === "/service-orders/appointment-orders" && r === "/service-orders") return true

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
  return userPermissions.some(
    (perm) => pathMatchesUserPermission(routePath, perm)
  )
}
