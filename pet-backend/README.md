# pet-backend — 后端 API

宠物之家后端服务：Spring Boot 2.7.14 · JDK 17 · MyBatis-Plus · MySQL 8 · Redis，端口 8080。

## 启动

```powershell
mvn clean compile
mvn spring-boot:run
```

- 本地零环境变量即可启动（MySQL/Redis/JWT 均有本地默认值）。
- profile 覆盖：`mvn spring-boot:run -Dspring-boot.run.profiles=local`（对应 `application-local.yml`）。
- 部署所需环境变量见 [../docs/configuration.md](../docs/configuration.md)。

## 目录结构

```
src/main/java/com/pethome/
├── common/        # Result 统一返回、BusinessException、全局异常处理
├── config/        # SecurityConfig、WebConfig（拦截器白名单）、CORS、启动期建表/迁移初始化器
├── controller/    # 60+ REST 控制器（/api/**）
├── entity/        # MyBatis-Plus 实体（@TableName 映射 64+ 张表）
├── mapper/        # Mapper 接口
├── service/       # 业务接口与 impl 实现
├── task/          # 7 个定时任务（订单/预约/缓存/帖子）
└── util/          # JwtUtil、FileUploadUtil 等工具类
```

## 关键机制

| 机制 | 位置/入口 | 说明 |
|---|---|---|
| 统一返回 | `common/Result` | 业务码：200 成功、401 未登录、403 无权限、400 参数、500 系统 |
| 认证 | token 解析于 Controller 层 | 用户归属校验一律从 `Authorization` 头解析，禁止接受 query/body 传入的 userId |
| 鉴权 | `config/WebConfig` + `RoleInterceptor` | 正向白名单放行匿名端点（登录/注册等）；新端点默认要求认证，管理端点（`/api/admin/**`）仅 admin 角色 |
| CORS | `application.yml` `app.cors.allowed-origins` | 白名单机制，禁止 `*` |
| 启动初始化 | `config/CouponSchemaInitializer` 等 | 自动建 `coupon`/`user_coupon`，`time_slots` 自动补列 |
| 定时任务 | `task/` | 见 [../docs/scheduled-tasks.md](../docs/scheduled-tasks.md) |
| 支付 | `PaymentController` | 真实微信支付 + 可选模拟通道，见 [../docs/mock-payment.md](../docs/mock-payment.md) |

## API 文档

- Swagger UI：<http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON：`/v3/api-docs`（springdoc-openapi）
- 生产环境用 `SWAGGER_ENABLED=false` 关闭

## 工程约定

- 日志：SLF4J；禁止 `System.out.println` / `e.printStackTrace`。
- 业务异常：抛 `BusinessException(code, msg)`，由全局异常处理器统一转换为 `Result`；Controller 不写 try-catch 样板。
- 事务：`@Transactional(rollbackFor = Exception.class)` 显式声明。
- SQL：MyBatis-Plus Wrapper / 参数化语句，禁止字符串拼接。
- 批量写：用 `UpdateWrapper` 批量更新替代循环逐条 UPDATE（防 N+1 写）。

## 常见问题

| 症状 | 解决 |
|---|---|
| `Access denied for user 'root'` | 设置 `MYSQL_PASSWORD` 后重启 |
| Redis 连接失败 | 先启动 Redis（默认 `localhost:6379`） |
| 登录 401 | 检查请求头 `Authorization: Bearer <token>`；token 过期需重新登录 |
| 数据库表不存在 | 按 [../docs/database.md](../docs/database.md) 初始化流程导入转储（已含全部 74 张表） |
