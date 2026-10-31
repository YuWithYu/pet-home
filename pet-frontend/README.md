# pet-frontend — 管理后台

宠物之家 B 端管理后台：Vue 3 · Element Plus · Vuex · Rspack。开发端口 8091。

> **来源声明**：本工程基于开源模板 [vue-admin-better](https://github.com/zxwk1998/vue-admin-better)（作者 zxwk1998）二次开发，业务页面与接口均为宠物之家管理后台功能。

## 常用命令

```powershell
npm install        # 安装依赖
npm run serve:rspack   # 开发（http://localhost:8091）
npm run build      # 生产构建（输出 dist/）
npm run lint       # ESLint 检查并自动修复（提交前必须执行）
npm run build:zip  # 构建并打包 zip
```

## 开发联调

- 后端地址：`src/config/net.config.js`。本项目仅本地运行，统一直连 `http://localhost:8080/api`。
- `rspack.config.js` 的 `devServer.proxy`（`/api` → `http://localhost:8080`）为代理兜底，日常无需修改。
- 前提：后端（8080）与 MySQL/Redis 已启动，见 [../pet-backend/README.md](../pet-backend/README.md)。
- 默认登录账号：`admin / 123456`（见仓库根 [../README.md](../README.md)）。

## 关键配置与约定

| 主题 | 位置 | 说明 |
|---|---|---|
| 后端地址 | `src/config/net.config.js` | 唯一出口，勿散落硬编码 |
| 状态码语义 | `src/config/net.config.js` | 200=成功，401=token 失效（强制重登），403=无权限，400=参数错，500=系统错 |
| 路由权限 | `src/utils/routePermission.js` | `canAccessRoute()` 是访问控制**单一入口**（路由守卫与菜单过滤共用）；default-deny，无角色兜底 |
| 认证状态 | Vuex store | 角色只存 store（服务端拉取）；**禁止**从 localStorage/sessionStorage 恢复角色 |
| 请求体清洗 | `request.js` | 只剔除 `undefined`/`null`（保留 `0`/`false`）；错误提示 3 秒去重 |

## 构建与代码质量

- CSS 提取：`CssExtractRspackPlugin`（勿换用 mini-css-extract-plugin，那是 webpack 专用）。
- 压缩：`SwcJsMinimizerRspackPlugin` 的 `pure_funcs` 剔除 `console.log/info/debug/trace`（保留 `warn/error`）。
- Lint：ESLint 9 flat config（`eslint.config.mjs`），console 语句仅允许 `warn`/`error`；**提交前跑 `npm run lint`**。
- mockjs 动态导入（`main.js` 条件引入），不会进入生产主包。

## 目录速览

```
pet-frontend/
├── rspack.config.js   # 构建/开发服务器配置（端口 8091、/api 代理）
├── rspack.js          # dev/build 启动入口
├── eslint.config.mjs  # ESLint 9 flat config
└── src/
    ├── api/           # 接口封装（按模块）
    ├── config/        # net.config.js 等全局配置
    ├── router/        # 路由与守卫
    ├── store/         # Vuex
    ├── utils/         # routePermission.js、request.js 等
    ├── views/         # 业务页面
    └── ...
```
