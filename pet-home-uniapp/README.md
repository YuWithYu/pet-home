# 宠物之家 - UniApp项目

宠物之家是一个基于uniapp开发的宠物服务平台，支持微信小程序、H5、App等多端部署。

## 功能特色

- 🏠 **首页展示**：轮播图、热门商品、分类导航
- 📱 **商品商城**：商品分类、列表、详情展示
- 🛒 **购物车**：商品收藏、数量管理
- 👤 **用户中心**：登录注册、个人信息管理
- 🐕 **宠物管理**：宠物档案、医疗记录
- 🏥 **预约服务**：医疗预约、美容护理、宠物寄养
- 📊 **订单管理**：订单查询、状态跟踪

## 技术栈

- **框架**：uniapp (Vue 2.x)
- **UI库**：uView 2.0
- **状态管理**：Vuex
- **HTTP请求**：uni.request
- **样式**：SCSS
- **图标**：Unicode字符图标

## 项目结构

```
pet-home-uniapp/
├── pages/                 # 页面文件
│   ├── index/            # 首页
│   ├── category/         # 分类页面
│   ├── goods/            # 商品相关
│   ├── cart/             # 购物车
│   ├── user/             # 用户中心
│   └── appointment/      # 预约服务
├── components/           # 公共组件
├── common/               # 公共资源
│   ├── css/             # 样式文件
│   └── js/              # 工具函数
├── store/               # 状态管理
├── static/              # 静态资源
└── uview-ui/            # uView组件库
```

## 快速开始

### 环境要求

- **HBuilderX 3.0+** (推荐)
- **微信开发者工具** (用于小程序调试)
- **Node.js 14.0+** (可选，用于自定义构建)

### 运行项目

1. **打开HBuilderX**
2. **导入项目**：文件 → 导入 → 从文件夹导入 → 选择 `pet-home-uniapp` 文件夹
3. **运行项目**：
   - 点击顶部工具栏的**运行**按钮
   - 选择**运行到小程序模拟器**
   - 或选择**真机运行**进行真机调试

### 项目特色

✅ **零依赖** - 无需安装额外依赖包，开箱即用
✅ **直接预览** - 个人主页直接显示，无需登录即可查看完整设计
✅ **完整功能** - 首页、分类、购物车、简洁白色个人中心、预约服务
✅ **现代化技术** - Vue 2.x + uniapp + 原生组件
✅ **完整后端集成** - 基于你的后端API接口设计

## 后端接口

项目使用以下后端接口：

- 基础配置：`/tz/config/values`
- 轮播图：`/tz/banner/list`
- 用户认证：`/tz/user/wxapp/authorize`、`/tz/user/login`
- 商品信息：`/tz/shop/goods/category/all`、`/tz/shop/goods/list/v2`
- 购物车：`/tz/shopping-cart/info`

后端服务地址：`https://localhost:8080`

## 部署说明

### 微信小程序

1. 修改`manifest.json`中的微信小程序配置
2. 在微信开发者工具中导入项目
3. 填写合法域名：`https://localhost:8080` （注意：HTTPS）
4. 点击"真机调试"进行测试

### H5部署

```bash
# 构建生产版本
npm run build:h5

# 部署到服务器
# 将dist/build/h5目录下的文件部署到Web服务器
```

## 开发规范

### 代码风格

- 使用ES6+语法
- 组件命名采用大驼峰命名
- 方法和变量采用小驼峰命名
- 常量采用大写蛇形命名

### 提交规范

```
<type>(<scope>): <subject>

type: feat, fix, docs, style, refactor, test, chore
scope: 影响的模块范围
subject: 简短描述
```

## 图标文件

项目目前使用默认文本图标。如需添加自定义图标，请参考 `ICONS_README.md` 文件。

## 联系我们

如有问题或建议，请联系开发团队。

## 开源协议

MIT License
