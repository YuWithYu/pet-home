// router/index.js
// 路由配置

const routes = [
  {
    path: '/pages/index/index',
    name: 'Index',
    component: () => import('../pages/index/index.vue')
  },
  {
    path: '/pages/category/index',
    name: 'Category',
    component: () => import('../pages/category/index.vue')
  },
  {
    path: '/pages/goods/list',
    name: 'GoodsList',
    component: () => import('../pages/goods/list.vue')
  },
  {
    path: '/pages/goods/detail',
    name: 'GoodsDetail',
    component: () => import('../pages/goods/detail.vue')
  },
  {
    path: '/pages/cart/index',
    name: 'Cart',
    component: () => import('../pages/cart/index.vue')
  },
  {
    path: '/pages/user/index',
    name: 'User',
    component: () => import('../pages/user/index.vue')
  },
  {
    path: '/pages/user/login',
    name: 'Login',
    component: () => import('../pages/user/login.vue')
  },
  {
    path: '/pages/user/register',
    name: 'Register',
    component: () => import('../pages/user/register.vue')
  },
  {
    path: '/pages/user/profile',
    name: 'Profile',
    component: () => import('../pages/user/profile.vue')
  },
  {
    path: '/pages/user/pets',
    name: 'Pets',
    component: () => import('../pages/user/pets.vue')
  },
  {
    path: '/pages/user/pets/add',
    name: 'AddPet',
    component: () => import('../pages/user/pets/add.vue')
  },
  {
    path: '/pages/user/orders',
    name: 'Orders',
    component: () => import('../pages/user/orders.vue')
  },
  {
    path: '/pages/appointment/medical',
    name: 'Medical',
    component: () => import('../pages/appointment/medical.vue')
  },
  {
    path: '/pages/appointment/grooming',
    name: 'Grooming',
    component: () => import('../pages/appointment/grooming.vue')
  },
  {
    path: '/pages/appointment/boarding',
    name: 'Boarding',
    component: () => import('../pages/appointment/boarding.vue')
  }
]

// 简化版的路由对象，仅用于组件引用
const router = {
  routes,
  currentRoute: null,

  // 获取当前路由
  getCurrentRoute() {
    return this.currentRoute
  },

  // 解析路由参数
  parseQuery(queryString) {
    if (!queryString) return {}
    const params = {}
    queryString.split('&').forEach(item => {
      const [key, value] = item.split('=')
      params[decodeURIComponent(key)] = decodeURIComponent(value)
    })
    return params
  }
}

export default router
