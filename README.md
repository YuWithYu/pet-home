# 宠物之家（Pet Home）毕业设计项目

本项目为宠物服务平台，包含：

- `pet-backend`：Spring Boot 后端
- `pet-home-uniapp`：uni-app 微信小程序端
- `pet-frontend`：管理后台前端端

## 目录结构

- `pet-backend/` 后端源码
- `pet-home-uniapp/` 小程序源码
- `pet-frontend/` 前端源码
- `database/` 数据库相关文件

## 开发环境要求

- JDK 17
- Maven 3.8+
- MySQL 8.x
- Redis 6.x
- Node.js 18+（前端）
- HBuilderX（运行 uni-app 小程序）

## 后端启动说明

后端配置位于 `pet-backend/src/main/resources/`：

- `application.yml`：通用基础配置

### 本地启动
cd pet-backend
mvn clean compile
mvn spring-boot:run

### 小程序端（uni-app）

目录：`pet-home-uniapp`

- 在 HBuilderX 中打开项目
- 运行到微信开发者工具（或打包）

### 管理后台

目录：`pet-frontend`
- 进入前端目录：`cd pet-frontend`
- 安装依赖：`npm install`
- 开发启动：`npm run serve:rspack`
- 构建：`npm run build`


- 运行 Redis 服务
- start "Redis Server" "C:\Redis\redis-server.exe"