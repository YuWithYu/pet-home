// Element Plus 中文语言包配置
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

export default {
  ...zhCn,
  pagination: {
    ...zhCn.pagination,
    goto: '前往',
    pageClassifier: '页',
    pageSize: '条/页',
    total: '共 {total} 条',
    page: '页'
  },
  el: {
    ...zhCn.el,
    pagination: {
      ...zhCn.el.pagination,
      goto: '前往',
      pageClassifier: '页',
      pageSize: '条/页',
      total: '共 {total} 条',
      page: '页'
    }
  }
}
