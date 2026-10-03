# 配置指南

覆盖：配置文件体系、环境变量参考、三端联调拓扑、上传与日志。

## 配置文件体系

| 文件 | 作用 |
|---|---|
| `pet-backend/src/main/resources/application.yml` | 通用配置。所有敏感项均为 `${ENV_VAR:默认值}` 形式，本地开发零环境变量可启动 |
| `pet-backend/src/main/resources/application-local.yml` | `local` profile 覆盖项（`mvn spring-boot:run -Dspring-boot.run.profiles=local`） |

优先级：**环境变量 > profile 文件 > application.yml**。生产环境新增配置一律走环境变量，禁止回填源码。

## 环境变量参考

### 数据库与缓存

| 变量 | 默认值 | 说明 |
|---|---|---|
| `MYSQL_URL` | `jdbc:mysql://127.0.0.1:3306/pet_home?...` | 数据源连接串（含 utf8mb4、GMT+8） |
| `MYSQL_USERNAME` | `root` | 数据库用户 |
| `MYSQL_PASSWORD` | `123456` | 数据库密码（生产必改） |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis 地址 |
| `REDIS_PASSWORD` | 空 | Redis 密码 |

### 安全

| 变量 | 默认值 | 说明 |
|---|---|---|
| `JWT_SECRET` | 本地固定开发密钥 | HS512 要求 **≥64 字节**；生产必须注入随机密钥，禁止使用默认值 |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:*,http://127.0.0.1:*` | CORS 来源白名单，逗号分隔，支持通配符；生产注入真实域名，**禁止 `*`** |

### 微信支付

| 变量 | 默认值 | 说明 |
|---|---|---|
| `WECHAT_PAY_APP_ID` | 空 | 商户绑定的小程序 appid |
| `WECHAT_PAY_MCH_ID` | 空 | 商户号 |
| `WECHAT_PAY_API_KEY` | 空 | 商户 API 密钥（回调验签前置条件，为空时回调 fail-closed 拒绝处理） |
| `WECHAT_PAY_NOTIFY_URL` | `http://localhost:8080/api/payment/notify` | 支付回调地址（生产为公网 HTTPS） |
| `WECHAT_PAY_CERT_PATH` | `./certs/apiclient_cert.p12` | 商户证书路径 |
| `WECHAT_PAY_CERT_PASSWORD` | 空 | 证书密码 |
| `WECHAT_PAY_MOCK_ENABLED` | `false` | 模拟支付通道开关，见 [mock-payment.md](mock-payment.md) |

### 小程序与短信

| 变量 | 默认值 | 说明 |
|---|---|---|
| `WECHAT_MINIAPP_APP_ID` / `WECHAT_MINIAPP_SECRET` | 空 | 小程序 code2session（微信授权登录） |
| `ALIBABA_CLOUD_ACCESS_KEY_ID` / `ALIBABA_CLOUD_ACCESS_KEY_SECRET` | 空 | 阿里云短信（`sms.aliyun.enabled` 默认关闭） |

### 运维

| 变量 | 默认值 | 说明 |
|---|---|---|
| `SWAGGER_ENABLED` | `true` | API 文档开关；**生产必须置 `false`**（避免暴露全部接口结构） |

## 三端联调拓扑（本地开发）

```
管理后台 (8091)  ──HTTP──►  后端 (8080)  ──►  MySQL (3306) / Redis (6379)
     │                              ▲
     │ dev 直连：src/config/net.config.js      │ dev 直连：main.js API_BASE
     │ baseURL = http://localhost:8080/api     │ (development → http://localhost:8080)
     └─ rspack.config.js 另配 /api 代理兜底 ───┘
小程序 (微信开发者工具) ──────────────┘
```

- 管理后台开发环境**直连**后端（`net.config.js`），`rspack.config.js` 的 `devServer.proxy`（`/api` → `http://localhost:8080`）为兜底路径。
- 浏览器直连受 CORS 约束，后端默认白名单 `localhost:*` 已覆盖 8091 端口。
- 小程序 `wx.request` 不走浏览器 CORS，无需处理。
- **生产**：管理后台 `baseURL = https://situationship.icu/api`；小程序发行构建（`build:mp-weixin`）自动切换到 `https://situationship.icu`（`main.js` 按 `NODE_ENV` 分流）。

## 上传与大小限制

- 全局 multipart 上限：单文件 200MB、单请求 210MB（`application.yml`）。
- 图片链路在 `FileUploadUtil` 中另有 10MB 分级限制；上传后自动压缩（最大 1024×1024，质量 0.75）。
- 上传目录 `./upload/`，访问前缀 `http://localhost:8080/upload/`；视频转码默认关闭。

## 日志

- MyBatis SQL 日志走 `Slf4jImpl`（勿改回 `StdOutImpl`，会把含敏感字段的 SQL 打进控制台）。
- `logging.level.com.pethome: debug`（生产建议调至 `WARN`/`INFO`）。
- 全局日志框架 SLF4J；禁止 `System.out.println` / `e.printStackTrace`。

## API 文档与健康检查

- Swagger UI：<http://localhost:8080/swagger-ui/index.html>；OpenAPI JSON：`/v3/api-docs`。生产用 `SWAGGER_ENABLED=false` 关闭。
- Actuator 仅暴露 `/actuator/health`（不暴露 env/heapdump 等敏感端点），供 Nginx/K8s 探活。
