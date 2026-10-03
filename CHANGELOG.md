# 更新日志

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [未发布]

### 新增

- **开源准备**：新增 MIT [LICENSE](LICENSE)；README 增加许可证徽章、开源说明（模板来源声明、数据脱敏说明）；新增 `database/sanitize-for-release.sql` 脱敏脚本。

### 安全（开源前清理）

- **演示数据全量脱敏并重导出**：真实手机号（用户/管理员/订单收货/预约联系/员工/门店/投诉，含误存入 `complaints.user_id` 的手机号）、邮箱、微信 `openid`/`unionid` 全部替换或置空；私信清空；全部账号密码哈希统一为演示密码 `123456`。文件级验证：真实号段/openid/邮箱残留为 0，74 张表结构不变。本地开发库 `pet_home` 未受影响（脱敏仅作用于发布副本）。

- **模拟支付通道**（2026-08-31）：`POST /api/payment/mock/confirm` 确认端点 + `prepay`/`query` 的 mock 分支；配置项 `wechat.pay.mock-enabled`（环境变量 `WECHAT_PAY_MOCK_ENABLED`，默认关闭，生产 fail-closed 未开启一律 403）。mock 确认复用与微信回调一致的状态机（订单 0→1 条件更新 + 清超时取消 key；预约 `markPaymentPaid`）。小程序端 `api.mockConfirmPay` 与支付页按 `prepay` 返回的 `mock` 标记自动分流，前端零改动。文档见 `docs/mock-payment.md`。
- **文档体系**（2026-08-31）：重写根 README（数据库初始化、环境变量、默认账号、三端启动顺序、排错）；新增 `docs/configuration.md`、`docs/mock-payment.md`、`docs/database.md`、`docs/scheduled-tasks.md`；新增三个子项目 README；开发计划书新增「0.4 与当前代码现状的已知差异」并修正安全误导示例（详见下条）。
- 开发计划书修订：Springfox → springdoc、上传限额 200MB、JWT HS512 ≥64 字节、`Slf4jImpl`、CORS 白名单示例、移除 `MD5Encoder`、支付对接示例端点更正为 `/api/payment/prepay`。

### 修复

- **补建 7 张缺失表并重导出转储**：`pet_medical_record`、`pet_weight_record`、`medical_services`、`staff_schedule`、`member_calendar_day_mark`、`appointment`、`bookings` 此前仅存在于实体/Mapper 中，旧转储（2026-05-03）与本地库均无对应表，宠物健康档案、员工排班、统一预约相关功能在导入旧转储的环境直接 500。已按实体 `@TableField` 推导 DDL 建入本地库，并重新全量导出 `database/pet_home.sql`（74 张表、mysqldump 格式含 `DROP TABLE IF EXISTS`）。实测宠物健康档案「新增 → 列表 → 到期提醒」链路全部 200。
- 验证：管理后台默认账号 `admin / 123456` 登录实测通过（BCrypt 校验、HS512 token 签发正常）。
- 文档：`docs/mock-payment.md` 的 PowerShell curl 示例修正为单引号 JSON 写法（原 `\"` 转义在 PowerShell 下会传给 curl 字面反斜杠导致 500）。
- `.gitignore` 显式放行 `database/*.sql`：原全局 `*.sql` 规则会静默忽略数据库目录新增脚本（现有 `pet_home.sql` 已被 git 跟踪、克隆可获取，但后续新增的 SQL 不会入库）。

## [安全加固批次] - 2026-08

> 依据 `application.yml` 内置注释与仓库现状整理（该批改动未逐条形成独立提交）。

- CORS 由通配来源改为域名白名单（`APP_CORS_ALLOWED_ORIGINS`，禁 `*`）。
- 上传限额收敛：单文件 500MB → 200MB（图片链路另有 10MB 分级限制与自动压缩）。
- 移除微信商户凭证、短信密钥的源码默认值，一律环境变量注入，未配置时 fail-closed。
- MyBatis SQL 日志 `StdOutImpl` → `Slf4jImpl`，避免敏感字段 SQL 进控制台。
- Swagger 生产开关（`SWAGGER_ENABLED=false`）；Actuator 仅暴露 `/actuator/health`。
- 接口文档框架 Springfox → springdoc-openapi 1.7.0。

## [1.0.0] - 2026-05-14

- Initial commit：宠物之家毕业设计项目基线——三端（Spring Boot 后端 / Rspack 管理后台 / uni-app 微信小程序）+ 64 张表 + 演示数据。
