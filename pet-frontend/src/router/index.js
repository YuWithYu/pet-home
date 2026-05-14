/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description router全局配置，如有必要可分文件抽离，其中asyncRoutes只有在intelligence模式下才会用到，vip文档中已提供路由的基础图标与小清新图标的配置方案，请仔细阅读
 */

import { createRouter, createWebHistory } from "vue-router";
import Layout from "@/layouts/index.vue";
import EmptyLayout from "@/layouts/EmptyLayout.vue";
import pointsMallIcon from "@/assets/menu/points-mall.png";
import serviceStaffIcon from "@/assets/menu/service-staff.png";
import serviceOrdersIcon from "@/assets/menu/service-orders.png";

export const constantRoutes = [
  {
    path: "/",
    component: Layout,
    redirect: "/index",
    hidden: true,
    children: [
      {
        path: "index",
        name: "Index",
        component: () => import("@/views/index/index.vue"),
        meta: { title: "宠物家首页", icon: "home", affix: true },
      },
    ],
  },
  {
    path: "/login",
    component: () => import("@/views/login/index.vue"),
    hidden: true,
  },
  {
    path: "/register",
    component: () => import("@/views/register/index.vue"),
    hidden: true,
  },
  {
    path: "/401",
    name: "401",
    component: () => import("@/views/401.vue"),
    hidden: true,
  },
  {
    path: "/404",
    name: "404",
    component: () => import("@/views/404.vue"),
    hidden: true,
  },
];

export const asyncRoutes = [
  // 首页 /、/index 已在 constantRoutes 中定义，避免未登录时 "No match for /index"
  // 侧栏顺序 = 下列各组 children 顺序；分组顺序：内容 → 商城 → 服务 → 用户 → 报表
  {
    path: "/content",
    component: Layout,
    redirect: "noRedirect",
    name: "Content",
    alwaysShow: true,
    meta: { title: "内容管理", icon: "document", defaultOpen: true, permissions: ["admin"] },
    children: [
      {
        path: "banner",
        name: "BannerManagement",
        component: () => import("@/views/vab/upload.vue"),
        meta: {
          title: "轮播图管理",
          permissions: ["admin"],
        },
      },
      {
        path: "notice",
        name: "NoticeManagement",
        component: () => import("@/views/vab/notice.vue"),
        meta: {
          title: "公告管理",
          permissions: ["admin"],
        },
      },
      {
        path: "community",
        name: "CommunityManagement",
        component: () => import("@/views/admin/community.vue"),
        meta: {
          title: "社区管理",
          permissions: ["admin"],
        },
      },
      {
        path: "complaint",
        name: "ComplaintManagement",
        component: () => import("@/views/vab/complaint.vue"),
        meta: {
          title: "投诉举报",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 商城管理（直接进入商品页）
  {
    path: "/mall",
    component: Layout,
    redirect: "/mall",
    name: "Mall",
    meta: { title: "商城管理", icon: "shop", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "",
        name: "MallManagement",
        component: () => import("@/views/vab/product.vue").catch(() => {
          console.error('商品管理组件加载失败');
          return import("@/views/404.vue");
        }),
        meta: {
          title: "商城管理",
          icon: "shop",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 积分商城管理（独立菜单）
  {
    path: "/points-mall",
    component: Layout,
    redirect: "/points-mall",
    name: "PointsMall",
    meta: { title: "积分商城管理", icon: pointsMallIcon, defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "",
        name: "PointsMallManagement",
        component: () => import("@/views/vab/points-mall.vue"),
        meta: {
          title: "积分商城管理",
          icon: pointsMallIcon,
          permissions: ["admin"],
        },
      },
    ],
  },
  // 服务管理（一级菜单；三类服务页面保留但不在侧栏展开）
  {
    path: "/service-platform",
    component: Layout,
    redirect: "/service-platform/litter-service",
    name: "ServicePlatform",
    meta: { title: "服务管理", icon: "tools", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "litter-service",
        name: "LitterServiceManagement",
        component: () => import("@/views/vab/schedule-management.vue"),
        meta: {
          title: "服务管理",
          icon: "tools",
          permissions: ["admin"],
          department: "上门铲屎",
        },
      },
      {
        path: "grooming-services",
        name: "GroomingServiceManagement",
        hidden: true,
        redirect: "/service-platform/litter-service",
        meta: {
          title: "洗护服务管理",
          permissions: ["admin"],
          department: "宠物洗护",
        },
      },
      {
        path: "hospital-service",
        name: "HospitalServiceManagement",
        hidden: true,
        redirect: "/service-platform/litter-service",
        meta: {
          title: "宠物医院服务管理",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 服务人员管理（平台/分店管理员入口）
  {
    path: "/service-staff",
    component: Layout,
    redirect: "/service-staff",
    name: "ServiceStaff",
    meta: { title: "服务人员管理", icon: serviceStaffIcon, defaultOpen: false, permissions: ["admin", "store_admin"] },
    children: [
      {
        path: "",
        name: "ServiceStaffManagement",
        component: () => import("@/views/vab/team.vue"),
        meta: {
          title: "服务人员管理",
          icon: serviceStaffIcon,
          permissions: ["admin", "store_admin"],
        },
      },
    ],
  },
  // 我的排班（仅服务人员、分店管理员）
  {
    path: "/my-schedule",
    component: Layout,
    redirect: "/my-schedule",
    name: "MySchedule",
    meta: { title: "我的排班", icon: "calendar", defaultOpen: false, permissions: ["store_admin", "staff"] },
    children: [
      {
        path: "",
        name: "MyScheduleManagement",
        component: () => import("@/views/vab/schedule-management.vue"),
        props: { isMySchedule: true },
        meta: {
          title: "我的排班",
          icon: "calendar",
          permissions: ["store_admin", "staff"],
          isMySchedule: true,
        },
      },
    ],
  },
  {
    path: "/service-stores",
    component: Layout,
    redirect: "/service-stores",
    name: "ServiceStoreManagementRoot",
    meta: { title: "服务门店管理", icon: "shop", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "",
        name: "ServiceStoreManagement",
        component: () => import("@/views/vab/service-store-list.vue").catch(() => {
          console.error('服务门店管理组件加载失败');
          return import("@/views/404.vue");
        }),
        meta: {
          title: "服务门店管理",
          icon: "shop",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 预约订单管理（聚合三类服务订单）
  {
    path: "/service-orders",
    component: Layout,
    redirect: "/service-orders/appointment-orders",
    name: "ServiceOrders",
    meta: { title: "预约订单管理", icon: serviceOrdersIcon, defaultOpen: false, permissions: ["admin", "store_admin", "staff"] },
    children: [
      {
        path: "appointment-orders",
        name: "AppointmentOrderManagement",
        component: () => import("@/views/vab/litter-service-orders.vue"),
        meta: {
          title: "预约订单管理",
          icon: serviceOrdersIcon,
          permissions: ["admin", "store_admin", "staff"],
        },
      },
      {
        path: "litter-service-orders",
        redirect: "/service-orders/appointment-orders",
        hidden: true,
        meta: {
          permissions: ["admin", "store_admin", "staff"],
        },
      },
      {
        path: "grooming-service-orders",
        name: "GroomingServiceOrderManagement",
        hidden: true,
        redirect: "/service-orders/appointment-orders",
        meta: {
          title: "宠物洗护预约订单",
          permissions: ["admin", "store_admin", "staff"],
        },
      },
      {
        path: "hospital-service-orders",
        name: "HospitalServiceOrderManagement",
        hidden: true,
        redirect: "/service-orders/appointment-orders",
        meta: {
          title: "宠物医院预约订单",
          permissions: ["admin", "store_admin", "staff"],
        },
      },
    ],
  },
  // 订单核销（一级菜单）
  {
    path: "/verify",
    component: Layout,
    redirect: "/verify",
    name: "VerifyRoot",
    meta: { title: "订单核销", icon: "check-circle", defaultOpen: false, permissions: ["admin", "store_admin", "staff"] },
    children: [
      {
        path: "",
        name: "VerifyManagement",
        component: () => import("@/views/vab/verify.vue").catch(() => {
          console.error('订单核销组件加载失败');
          return import("@/views/404.vue");
        }),
        meta: {
          title: "订单核销",
          icon: "check-circle",
          permissions: ["admin", "store_admin", "staff"],
        },
      },
    ],
  },
  // 门店客服（一级菜单）
  {
    path: "/outlet-customer-chat",
    component: Layout,
    redirect: "/outlet-customer-chat",
    name: "OutletCustomerChatRoot",
    meta: { title: "门店客服", icon: "comments", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "",
        name: "OutletCustomerChat",
        component: () => import("@/views/vab/outlet-customer-chat.vue"),
        meta: {
          title: "门店客服",
          icon: "comments",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 用户管理路由组
  {
    path: "/users",
    component: Layout,
    redirect: "noRedirect",
    name: "Users",
    alwaysShow: true,
    meta: { title: "用户管理", icon: "user", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "list",
        name: "UserList",
        component: () => import("@/views/vab/customer.vue"),
        meta: {
          title: "用户列表",
          permissions: ["admin"],
        },
      },
      {
        path: "permissions",
        name: "UserPermissions",
        component: () => import("@/views/vab/permissions.vue"),
        meta: {
          title: "账号管理",
          permissions: ["admin"],
        },
      },
      {
        path: "platform-customer-chat",
        name: "PlatformCustomerChat",
        component: () => import("@/views/vab/platform-customer-chat.vue"),
        meta: {
          title: "平台客服",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 统计报表路由组
  {
    path: "/reports",
    component: Layout,
    redirect: "noRedirect",
    name: "Reports",
    alwaysShow: true,
    meta: { title: "统计报表", icon: "chart", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/vab/statistics.vue"),
        meta: {
          title: "数据大屏",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 通配路由须放在 asyncRoutes 末尾，避免拦截商城/服务等动态路由
  {
    path: "/:pathMatch(.*)*",
    redirect: "/404",
    hidden: true,
  },
];

const router = createRouter({
  history: createWebHistory("/"),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

export function resetRouter() {
  // 注意：所有动态路由路由必须带有name属性，否则可能会不能完全重置干净
  try {
    router.getRoutes().forEach((route) => {
      const { name } = route;
      if (name && name !== "Login") {
        router.hasRoute(name) && router.removeRoute(name);
      }
    });
  } catch (error) {
    // 如果路由重置失败，只记录错误，不刷新页面
    console.error('重置路由失败:', error);
    // 不要强制刷新页面，避免无限循环
    // window.location.reload();
  }
}

/**
 * 根据角色动态设置路由
 * @param {string} role - 用户角色：admin 或 staff
 */
export function setupRoutesByRole(role, addToRouter = true) {
  // 注意：不要在这里调用 resetRouter()，因为它可能触发页面刷新
  // 路由守卫会处理路由的动态添加
  // resetRouter();
  
  if (role === 'admin') {
    // 超级管理员：加载所有路由（由权限系统控制）
    // 路由已在 permission.js 中通过 filterAsyncRoutes 过滤
    console.log('超级管理员登录，加载所有路由');
    return asyncRoutes;
  } else if (role === 'staff') {
    console.log('服务人员登录，加载我的排班与订单核销');
    return asyncRoutes.filter((route) => ["MySchedule", "VerifyRoot"].includes(route.name));
  } else {
    // 其他角色或未登录：不加载路由
    console.log('未知角色，不加载路由:', role);
    return [];
  }
}

export default router;
