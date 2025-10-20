/**
 * @author https://github.com/zxwk1998/vue-admin-better （不想保留author可删除）
 * @description router全局配置，如有必要可分文件抽离，其中asyncRoutes只有在intelligence模式下才会用到，vip文档中已提供路由的基础图标与小清新图标的配置方案，请仔细阅读
 */

import { createRouter, createWebHashHistory } from "vue-router";
import Layout from "@/layouts/index.vue";
import EmptyLayout from "@/layouts/EmptyLayout.vue";
import { publicPath } from "@/config";

export const constantRoutes = [
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
  {
    path: "/",
    component: Layout,
    redirect: "/index",
    children: [
      {
        path: "index",
        name: "Index",
        component: () => import("@/views/index/index.vue"),
        meta: {
          title: "宠物家首页",
          icon: "home",
          affix: true,
        },
      },
    ],
  },


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
        component: () => import("@/views/vab/form.vue"),
        meta: {
          title: "公告管理",
          permissions: ["admin"],
        },
      },
      {
        path: "audit",
        name: "ContentAudit",
        component: () => import("@/views/vab/table.vue"),
        meta: {
          title: "内容审核",
          permissions: ["admin"],
        },
      },
      {
        path: "hot-products",
        name: "HotProductsManagement",
        component: () => import("@/views/vab/hot-products.vue"),
        meta: {
          title: "热门推荐管理",
          permissions: ["admin"],
        },
      },
      {
        path: "statistics",
        name: "Statistics",
        component: () => import("@/views/vab/statistics.vue"),
        meta: {
          title: "数据统计",
          permissions: ["admin"],
        },
      },
      {
        path: "team",
        name: "Team",
        component: () => import("@/views/vab/team.vue"),
        meta: {
          title: "团队管理",
          permissions: ["admin"],
        },
      },
      // {
      //   path: "campaign",
      //   name: "Campaign",
      //   component: () => import("@/views/vab/campaign.vue"),
      //   meta: {
      //     title: "营销活动",
      //     permissions: ["admin"],
      //   },
      // },
    ],
  },

  {
    path: "/:pathMatch(.*)*",
    redirect: "/404",
    hidden: true,
  },
  // 商城管理路由组
  {
    path: "/mall",
    component: Layout,
    redirect: "noRedirect",
    name: "Mall",
    alwaysShow: true,
    meta: { title: "商城管理", icon: "shop", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "products",
        name: "ProductManagement",
        component: () => import("@/views/vab/product.vue"),
        meta: {
          title: "商品管理",
          permissions: ["admin"],
        },
      },
      {
        path: "inventory",
        name: "InventoryManagement",
        component: () => import("@/views/vab/table.vue"),
        meta: {
          title: "库存管理",
          permissions: ["admin"],
        },
      },
      {
        path: "orders",
        name: "OrderManagement",
        component: () => import("@/views/vab/order.vue"),
        meta: {
          title: "订单管理",
          permissions: ["admin"],
        },
      },
      {
        path: "refunds",
        name: "RefundManagement",
        component: () => import("@/views/vab/table.vue"),
        meta: {
          title: "退款售后",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 服务管理路由组
  {
    path: "/services",
    component: Layout,
    redirect: "noRedirect",
    name: "Services",
    alwaysShow: true,
    meta: { title: "服务管理", icon: "tools", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "appointments",
        name: "AppointmentManagement",
        component: () => import("@/views/vab/appointment.vue"),
        meta: {
          title: "宠物医院预约管理",
          permissions: ["admin"],
        },
      },
        {
          path: "doctors",
          name: "DoctorManagement",
          component: () => import("@/views/vab/doctor.vue"),
          meta: {
            title: "专业医师管理",
            permissions: ["admin"],
          },
        },
        {
          path: "litter-service",
          name: "LitterServiceManagement",
          component: () => import("@/views/vab/door-cleaning-service.vue"),
          meta: {
            title: "上门铲屎服务管理",
            permissions: ["admin"],
          },
        },
        {
          path: "pet-boarding",
          name: "PetBoardingManagement",
          component: () => import("@/views/vab/pet-boarding-service.vue"),
          meta: {
            title: "宠物寄养管理",
            permissions: ["admin"],
          },
        },
        {
          path: "pet-adoption",
          name: "PetAdoptionManagement",
          component: () => import("@/views/vab/pet-adoption-service.vue"),
          meta: {
            title: "宠物领养管理",
            permissions: ["admin"],
          },
        },
        {
          path: "time-slots",
          name: "TimeSlotManagement",
          component: () => import("@/views/vab/time-slot-management.vue"),
          meta: {
            title: "预约时间段管理",
            permissions: ["admin"],
          },
        },
      {
        path: "grooming-services",
        name: "GroomingServiceManagement",
        component: () => import("@/views/vab/grooming-service.vue"),
        meta: {
          title: "洗护服务管理",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 财务管理路由组
  {
    path: "/finance",
    component: Layout,
    redirect: "noRedirect",
    name: "Finance",
    alwaysShow: true,
    meta: { title: "财务管理", icon: "money", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "payments",
        name: "PaymentManagement",
        component: () => import("@/views/vab/table.vue"),
        meta: {
          title: "支付流水",
          permissions: ["admin"],
        },
      },
      {
        path: "reconciliation",
        name: "Reconciliation",
        component: () => import("@/views/vab/form.vue"),
        meta: {
          title: "对账管理",
          permissions: ["admin"],
        },
      },
      {
        path: "reports",
        name: "FinancialReports",
        component: () => import("@/views/vab/chart.vue"),
        meta: {
          title: "财务报表",
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
        component: () => import("@/views/vab/form.vue"),
        meta: {
          title: "权限管理",
          permissions: ["admin"],
        },
      },
      {
        path: "statistics",
        name: "UserStatistics",
        component: () => import("@/views/vab/chart.vue"),
        meta: {
          title: "用户统计",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 营销管理路由组
  {
    path: "/marketing",
    component: Layout,
    redirect: "noRedirect",
    name: "Marketing",
    alwaysShow: true,
    meta: { title: "营销管理", icon: "gift", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "coupons",
        name: "CouponManagement",
        component: () => import("@/views/vab/table.vue"),
        meta: {
          title: "优惠券管理",
          permissions: ["admin"],
        },
      },
      {
        path: "activities",
        name: "ActivityManagement",
        component: () => import("@/views/vab/form.vue"),
        meta: {
          title: "活动管理",
          permissions: ["admin"],
        },
      },
      {
        path: "members",
        name: "MemberManagement",
        component: () => import("@/views/vab/table.vue"),
        meta: {
          title: "会员管理",
          permissions: ["admin"],
        },
      },
      {
        path: "points",
        name: "PointsManagement",
        component: () => import("@/views/vab/table.vue"),
        meta: {
          title: "积分管理",
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
      {
        path: "sales",
        name: "SalesReports",
        component: () => import("@/views/vab/chart.vue"),
        meta: {
          title: "销售报表",
          permissions: ["admin"],
        },
      },
      {
        path: "users",
        name: "UserReports",
        component: () => import("@/views/vab/chart.vue"),
        meta: {
          title: "用户报表",
          permissions: ["admin"],
        },
      },
      {
        path: "products",
        name: "ProductReports",
        component: () => import("@/views/vab/chart.vue"),
        meta: {
          title: "商品报表",
          permissions: ["admin"],
        },
      },
    ],
  },
  // 系统管理路由组
  {
    path: "/system",
    component: Layout,
    redirect: "noRedirect",
    name: "System",
    alwaysShow: true,
    meta: { title: "系统管理", icon: "setting", defaultOpen: false, permissions: ["admin"] },
    children: [
      {
        path: "logs",
        name: "SystemLogs",
        component: () => import("@/views/vab/table.vue"),
        meta: {
          title: "系统日志",
          permissions: ["admin"],
        },
      },
      {
        path: "monitor",
        name: "SystemMonitor",
        component: () => import("@/views/vab/chart.vue"),
        meta: {
          title: "系统监控",
          permissions: ["admin"],
        },
      },
      {
        path: "settings",
        name: "SystemSettings",
        component: () => import("@/views/vab/form.vue"),
        meta: {
          title: "系统设置",
          permissions: ["admin"],
        },
      },
      {
        path: "roles",
        name: "RoleManagement",
        component: () => import("@/views/vab/form.vue"),
        meta: {
          title: "角色权限",
          permissions: ["admin"],
        },
      },
    ],
  },
];

const router = createRouter({
  history: createWebHashHistory(publicPath),
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
    // 强制刷新浏览器，不要用这种方式
    window.location.reload();
  }
}

export default router;
