import Vue from 'vue'
import App from './App'

// uni-app Promise 支持
import './uni.promisify.adaptor'

// 状态管理
import store from './store/index.js'

// 工具类
import { api } from './common/js/api'
import { util } from './common/js/util'

// 全局注册 custom-navbar，避免在页面内按需加载时报错
import CustomNavbar from './components/custom-navbar/custom-navbar.vue'
Vue.component('CustomNavbar', CustomNavbar)




// 环境自动切换：开发构建（dev:mp-weixin / HBuilderX 运行）连本地后端，
// 发行构建（build:mp-weixin）统一走线上备案域名 https://situationship.icu
const API_BASE = process.env.NODE_ENV === 'development'
  ? 'http://localhost:8080'
  : 'https://situationship.icu'
util.setApiBaseUrl(API_BASE)

// 挂载 Vue 原型
Vue.prototype.$api = api
Vue.prototype.$util = util
Vue.prototype.$store = store

Vue.config.productionTip = false

// 全局错误捕获
Vue.config.errorHandler = (err, instance, info) => {
  console.error('Vue Error:', err, info)
}

/**
 * 创建 Vue 实例
 */
const app = new Vue({
  store,
  render: h => h(App)
})

/**
 * 初始化用户信息
 */
app.$nextTick(() => {
  if (store.dispatch) {
    store.dispatch('initUserInfo')
  }
})

app.$mount()