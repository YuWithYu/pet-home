/**
 * @description 导出默认通用配置
 */
const setting = {
  // 生产环境构建文件的目录名
  outputDir: "dist",
  // 放置生成的静态资源 (js、css、img、fonts) 的 (相对于 outputDir 的) 目录。
  assetsDir: "static",
  // 静态资源公共路径
  publicPath: "/",
  // 开发环境每次保存时是否输出为eslint编译警告
  lintOnSave: true,
  // 进行编译的依赖
  transpileDependencies: [],
  //标题 （包括初次加载雪花屏的标题 页面的标题 浏览器的标题）
  title: "宠物家管理系统",
  //简写
  abbreviation: "宠物家",
  //开发环境端口号
  devPort: "8091",
  //copyright（页脚若展示品牌信息时使用）
  copyright: "宠物家",
  //是否显示页面底部自定义版权信息
  footerCopyright: true,
  //是否显示顶部进度条
  progressBar: true,
  //缓存路由的最大数量
  keepAliveMaxNum: 99,
  // 路由模式，可选值为 history 或 hash
  routerMode: "history",
  //不经过token校验的路由
  routesWhiteList: ["/", "/login", "/register", "/404", "/401"],
  //加载时显示文字
  loadingText: "正在加载中...",
  //token名称
  tokenName: "accessToken",
  //token在localStorage、sessionStorage存储的key的名称
  tokenTableName: "vue-admin-better-2024",
  //token存储位置localStorage sessionStorage
  storage: "localStorage",
  //token失效回退到登录页时是否记录本次的路由
  recordRoute: true,
  //是否显示logo，不显示时设置false；可为 remixIcon 图标名称，或图片路径（使用英文文件名避免 URL 编码导致裂图）
  logo: "/static/pet-home-logo.png",
  //是否显示在页面高亮错误
  errorLog: ["development"],
  //是否开启登录拦截
  loginInterception: true,
  //intelligence和all两种方式，前者后端权限只控制permissions不控制view文件的import（前后端配合，减轻后端工作量），all方式完全交给后端前端只负责加载
  authentication: "intelligence",
  //vertical布局时是否只保持一个子菜单的展开（侧栏过长时更易扫读）
  uniqueOpened: true,
  //vertical布局时默认展开的菜单 path（与 router 一级 path 一致；原模板 /vab 在本项目中不存在）
  defaultOopeneds: ["/content"],
  //需要加loading层的请求，防止重复提交
  debounce: ["doEdit"],
  //需要自动注入并加载的模块
  providePlugin: {},
  //代码生成机生成在view下的文件夹名称
  templateFolder: "project",
  //是否显示终端 donation 打印（已 noop，保留开关即可）
  donation: false,
};
module.exports = setting;
