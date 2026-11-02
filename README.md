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
- `docs/` 配置、数据库、支付、定时任务等说明文档

## 开发环境要求

| 软件 | 版本 | 用途 | 下载地址 |
| --- | --- | --- | --- |
| JDK | 17 | 后端编译与运行 | https://adoptium.net/temurin/releases/?version=17 |
| Maven | 3.8+ | 后端构建 | https://maven.apache.org/download.cgi |
| MySQL | 8.x | 业务数据库 | https://dev.mysql.com/downloads/installer/ |
| Redis | 6.x+ | 缓存、分布式锁（必装） | https://github.com/tporadowski/redis/releases |
| Node.js | 18+ | 管理后台构建 | https://nodejs.org/zh-cn/download |
| HBuilderX | 最新版 | 运行 uni-app 小程序 | https://www.dcloud.io/hbuilderx.html |
| 微信开发者工具 | 最新版 | 小程序预览与调试 | https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html |

以下为可选组件，不安装也能运行（消息队列会自动降级为定时扫表）：

| 软件 | 用途 | 下载地址 |
| --- | --- | --- |
| RabbitMQ 3.x/4.x（Windows 需先装 Erlang） | 订单超时延迟队列 | https://www.rabbitmq.com/docs/install-windows |
| Kafka 3.x/4.x | 行为埋点管道 | https://kafka.apache.org/downloads |

不安装消息队列时，启动后端前设置环境变量 `APP_MQ_ENABLED=false` 即可。

## 1. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE pet_home DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入结构与演示数据（已脱敏）
mysql -u root -p pet_home < database/pet_home.sql
```

数据库连接配置在 `pet-backend/src/main/resources/application.yml`，默认 `127.0.0.1:3306/pet_home`、账号 `root`、密码 `123456`，也可通过环境变量 `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD` 覆盖。

## 2. 启动 Redis（后端启动前必须先启动 Redis）

Windows 下将 Redis 解压到任意目录（例如 `C:\Redis`），然后执行：

```powershell
start "Redis Server" "C:\Redis\redis-server.exe"
```

> 注意：`C:\Redis\redis-server.exe` 是 Redis 的启动文件路径，请根据实际安装位置修改。

Linux / macOS 直接运行：

```bash
redis-server
```

验证是否启动成功：

```bash
redis-cli ping   # 返回 PONG 即正常
```

默认连接 `localhost:6379`，可在 `application.yml` 或用环境变量 `REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD` 修改。

## 3. 启动后端

后端配置位于 `pet-backend/src/main/resources/`：

- `application.yml`：通用基础配置

```bash
cd pet-backend
mvn clean compile
mvn spring-boot:run
```

启动后默认端口 `8080`。

> JWT 密钥：本地未设置 `JWT_SECRET` 时，每次启动会随机生成临时密钥（重启后旧 token 失效，需要重新登录）。需要稳定登录态时，启动前设置 `JWT_SECRET`（至少 64 字节）。

## 4. 小程序端（uni-app）

目录：`pet-home-uniapp`

- 在 HBuilderX 中打开项目
- 运行到微信开发者工具（或打包）

## 5. 管理后台

目录：`pet-frontend`

```bash
cd pet-frontend
npm install        # 安装依赖
npm run serve:rspack   # 开发启动
npm run build          # 构建
```

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
