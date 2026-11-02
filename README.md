# 宠物之家

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
  
## Redis启动说明


- 运行 Redis 服务
- start "Redis Server" "C:\Redis\redis-server.exe"
（注意："C:\Redis\redis-server.exe"这个是Redis的启动文件，根据实际情况修改路径）


## 后端启动说明

后端配置位于 `pet-backend/src/main/resources/`：

- `application.yml`：通用基础配置

### 后端启动（启动后端必须先启动 Redis 服务）

cd pet-backend
mvn clean compile
mvn spring-boot:run

### 小程序端（uni-app）

目录：`pet-home-uniapp`

- 在 HBuilderX 中打开项目
- 运行到微信开发者工具（或打包）

### 管理后台

-目录：`pet-frontend`
- 进入前端目录：`cd pet-frontend`
- 安装依赖：`npm install`
- 开发启动：`npm run serve:rspack`
- 构建：`npm run build`


- 运行 Redis 服务
- start "Redis Server" "C:\Redis\redis-server.exe"

## 小程序端

### 登陆注册

<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/639a2ed6-ffcd-41e6-ae0e-51fa8cb9b75f" /> 
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/f4750e1b-88e4-4289-a361-5f7ad07fdd80" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/31d3a75d-a099-4e1a-9b02-c49bf95bf885" />

### 服务预约

<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/8b248d99-f894-4c76-a06c-c9f07a9958b4" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/64fd780f-fbbf-4cda-b172-1e13264af331" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/61ffce71-0d25-4289-b200-dffb6dcd988f" /> 

### 商品购买

<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/23ed1f47-9c0e-4dc0-abb4-d6860e53b694" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/8755acd6-dee6-42d6-a259-8ff6921868d0" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/b615bd04-63bc-4d17-ad07-f8bafbcfe458" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/d3f4846e-666e-477b-bf47-3fa4d7391468" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/eaf6101e-2c73-4467-aa26-3eb1655ec033" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/fa50c97a-02d6-4224-bf7c-f9df2ada2de2" />

### 社区

<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/e0580f34-f490-4de4-bc28-154936fabf5f" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/acbac656-3bef-43ae-9c6e-6e7e70dcd699" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/53475852-f4e2-4525-bbb8-8615785ee92d" />

### 消息

<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/941248c5-23eb-4a91-aad5-3002c91c7aac" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/3a5fa56f-5d64-461e-8320-51108aade438" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/497ae2d3-aa3a-463c-9f6e-5f57d04c128a" />

### 我的

<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/a17405ce-526d-4299-8a5d-e1f6a267f697" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/92e4a426-88f2-42d0-a2a8-56f926e944f7" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/56f9ee2f-b14a-4367-a6f0-5bc738840c06" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/67cdb3a9-e75a-4cd1-8486-b77ae3132d9c" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/43ba61ee-aa60-450f-ad03-cba4957006da" />
<img width="250" height="500" alt="image" src="https://github.com/user-attachments/assets/9451cfff-a95d-4f24-a1c9-130cf32523d7" />

## 后台管理端

<img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/2ac4d705-e680-4922-967a-b7f6b2d35576" />
<img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/c2e0d1dc-f4b2-4246-bf31-6d616b973000" />
<img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/804f690f-e91b-4317-8fb6-e49f0fc8cb83" />
<img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/bda60acb-69a6-41e6-a2e6-63d6c530d899" />
<img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/86ba1022-077a-4c76-b983-24a02c602685" />
<img width="1000" height="500" alt="image" src="https://github.com/user-attachments/assets/ba615c35-f78d-4d5c-8ca5-37c285a81d81" />


























