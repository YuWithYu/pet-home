// main.js
import Vue from 'vue'
import App from './App'

// uView库已移除，使用原生uni-app组件

// 导入uni-app的全局对象
import './uni.promisify.adaptor'

// 导入全局样式 (在App.vue中导入)

// 导入状态管理
import store from './store/index.js'

// 导入路由
import router from './router/index.js'

// 导入工具类
import { api } from './common/js/api'
import { util } from './common/js/util'

// 挂载到Vue原型上
Vue.prototype.$api = api
Vue.prototype.$util = util
Vue.prototype.$store = store

// 全局混入
Vue.mixin({
  onLoad() {
    // 设置导航栏标题
    if (this.$options.navigationBarTitleText) {
      uni.setNavigationBarTitle({
        title: this.$options.navigationBarTitleText
      })
    }
  }
})

// Vue配置
Vue.config.productionTip = false

// 创建Vue实例
const app = new Vue({
  store,
  router,
  render: h => h(App)
})

// 应用启动后初始化用户信息
app.$nextTick(() => {
  store.dispatch('initUserInfo')
})

// 挂载应用
app.$mount()

// #ifdef APP-PLUS
// App环境下需要特殊处理
// #endif

// 全局错误处理
Vue.config.errorHandler = (err, instance, info) => {
  console.error('Vue Error:', err, info)
}

// unhandledRejection处理
if (typeof window !== 'undefined') {
  window.addEventListener('unhandledrejection', event => {
    console.error('Unhandled Promise Rejection:', event.reason)
  })
}
