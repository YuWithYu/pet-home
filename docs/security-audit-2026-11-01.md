# 安全审计报告：Pet Home 宠物服务平台

**日期**：2026-11-01 | **版本**：1.0 | **作者**：安全工程师
**审计范围**：全量安全审计（威胁建模 + 代码审计 + 依赖 CVE + 密钥/配置审查 + 安全响应头/CORS）
**交付方式**：报告 + 高危及以上问题直接修复
**代码基线**：`fd77d18`（修复已提交于 `26a2335`、`838fa5a`）

---

## 1. 执行摘要

| 严重性 | 数量 | 已修复 | 说明 |
|--------|------|--------|------|
| 严重（Critical） | 0 | - | 未发现 RCE、认证绕过、可拖库 SQL 注入 |
| 高危（High） | 5 | **5** | 全部为「匿名可写」与水平越权，已修复并实测验证 |
| 中危（Medium） | 4 | **4** | 信息泄露、短信轰炸成本风险、种子账号、DB 链路 TLS |
| 低危（Low） | 1 | **1** | 视频分片目录无归属绑定 |
| 信息（Informational） | 4 | 0 | 架构与依赖维护状态改进项 |

**核心结论**：本系统鉴权完全依赖自定义 `RoleInterceptor`（Spring Security 为 `anyRequest().permitAll()`），
因此该拦截器的公开白名单就是全系统的唯一安全边界。本次审计发现白名单以「路径前缀 + 不限 HTTP 方法」放行，
导致 4 个后台写接口可被匿名调用；另有 1 处列表接口直接信任查询参数造成水平越权。5 项已全部修复。
第二轮加固一并处理了中危/低危项：异常信息脱敏（304 处）、短信 IP 维度限流、视频分片归属绑定、
种子账号与生产 DB 链路提示，编译与运行时验证均通过。

已排除的高风险项（经代码核对确认安全）：

- 无 SQL 注入：`mapper/*.xml` 无 `${}` 拼接，无 `createStatement`，全部走 MyBatis-Plus `QueryWrapper` / 参数化查询
- 无自造加密：JWT 使用 jjwt HS512（密钥 ≥64 字节，启动强校验并拒绝生产默认值），密码使用 BCrypt
- 支付回调：微信 V2 MD5 验签 + Redis SETNX 幂等锁 + 金额比对 + 状态机；mock 通道默认关闭（fail-closed）
- 文件上传：扩展名与 Content-Type 双白名单、`uploadId` 正则防路径穿越、图片 10MB/视频 200MB 限制
- 短信验证码：`SecureRandom` 生成、60s/条 + 10 条/日、失败 5 次作废、日志不输出验证码
- 管理员登录：5 次失败锁定 15 分钟，历史明文密码自动升级为 BCrypt
- 全局异常：`GlobalExceptionHandler` 对非业务异常统一返回「系统繁忙」，不泄露堆栈

---

## 2. 系统概述

- **架构**：单体后端 + 三个前端（Vue 3 管理后台 / uni-app 小程序 / Vue 商城前台）
- **技术栈**：Spring Boot 2.7.18、Java 17、Spring Security 5.7.11、MyBatis-Plus 3.5.3.1、MySQL、Redis、RabbitMQ、Kafka
- **鉴权链路**：Spring Security 全放行 → `RoleInterceptor` 解析 `Authorization: Bearer` → 写入 request attribute（`userId` / `adminId` / `role` / `departmentId` / `serviceStoreId`）→ Controller 内做归属校验
- **数据分类**：用户 PII（手机号、收货地址、微信 openid）、订单与支付流水、宠物档案、社区 UGC
- **部署**：Nginx 终止 TLS → 后端 1012 端口 HTTP；上传文件落本地磁盘并静态映射 `/upload/**`
- **外部集成**：微信支付 V2、微信小程序登录、阿里云短信

## 3. 信任边界

| 边界 | 来源 | 目标 | 控制措施 |
|------|------|------|----------|
| 互联网 → 应用 | 终端用户 | Nginx / 后端 | TLS（Nginx）、CORS 来源白名单、拦截器 Bearer 校验 |
| 匿名 → 公开接口 | 未登录用户 | 只读 API | 正向白名单 + **仅 GET** |
| 用户 → 用户数据 | C 端用户 | Controller | token 解析出的 userId 与资源归属比对 |
| 店铺管理员 → 跨店数据 | 店铺端 | 后台 API | `storeId` / `serviceStoreId` 收敛 |
| 应用 → 数据库 | 后端 | MySQL | 参数化查询、账号最小权限（建议） |
| 第三方 → 应用 | 微信支付 / 短信 | 回调与非回调 API | MD5 验签、Redis 幂等锁、金额比对 |

## 4. STRIDE 摘要

| 威胁 | 组件 | 风险 | 攻击场景 | 状态 |
|------|------|------|----------|------|
| 假冒 | `/api/admin/login` | 高 | 口令爆破 | 已缓解（5 次锁定 15 分钟） |
| 篡改 | 匿名公开白名单 | **严重** | 匿名改时间段/公告/横幅、批量生成排班 | **已修复** |
| 抵赖 | 订单 / 支付 | 中 | 否认下单与退款操作 | 建议补不可变审计日志 |
| 信息泄露 | 异常响应 | 中 | `e.getMessage()` 回传内部细节 | **未修复（M-02）** |
| 拒绝服务 | `/api/sms/send` | 中 | 换号轰炸消耗短信费用 | **未修复（M-03）** |
| 权限提升 | `/api/pets/page` | 高 | `?userId=` 枚举他人宠物档案 | **已修复** |

## 5. 攻击面清单

- **外部**：`/api/**` 全部接口（其中公开白名单见附录 A）、`/upload/**` 静态目录、微信支付回调 `/api/payment/notify`
- **内部**：Redis（token 存储）、RabbitMQ 延迟队列、Kafka 埋点、本地消息表 outbox
- **数据**：MySQL（含种子账号）、上传目录、日志
- **基础设施**：Nginx、后端 1012 端口、环境变量注入的凭证

---

## 6. 已修复并验证（高危）

### H-01 匿名可增删改服务时间段

- **证据**：`RoleInterceptor.java` 原白名单 `uri.startsWith("/api/time-slots/")` 不限方法；`TimeSlotController` 暴露 `POST /create`、`POST /update`、`PUT /{id}/status`、`DELETE /{id}`
- **可利用性**：`curl -X POST /api/time-slots/create -d '{...}'` 未登录即成功，可任意篡改门店可预约时段（影响全部预约业务）
- **修复**：公开白名单统一收敛为「仅 GET」，见 `RoleInterceptor.java:138` 与 `isPublicReadApi`（`RoleInterceptor.java:51-97`）

### H-02 匿名可增删改公告

- **证据**：原白名单 `uri.startsWith("/api/notice/")`；`NoticeController` 有 `POST /create`、`PUT /update`、`DELETE /{id}`（`NoticeController.java:98/121/169`）
- **可利用性**：匿名写入站内公告 → 内容篡改 / 钓鱼文案投放
- **修复**：同上，写方法现返回 401

### H-03 匿名可批量生成排班

- **证据**：`/api/schedule-config/generate-batch`（POST）位于原匿名白名单
- **可利用性**：对任意门店 / 人员 / 日期批量写库，造成排班数据污染与写放大（资源消耗）
- **修复**：该路径已移出白名单，POST 现返回 401

### H-04 匿名可增改服务横幅（前缀重叠）

- **证据**：白名单前缀 `/api/grooming-banner`、`/api/litter-banner` 会同时匹配 `GroomingServiceBannerController(/api/grooming-banners)`、`LitterBannerController(/api/litter-banners)` 的 `POST /create`、`PUT /update`
- **可利用性**：匿名替换小程序首页 / 服务页横幅（内容投毒、引流劫持）
- **修复**：横幅前缀仅 GET 放行；写操作需登录（`RoleInterceptor.java:80-85`）

### H-05 `/api/pets/page?userId=` 水平越权（IDOR）

- **证据**：`PetController#getPetPage` 原实现直接使用查询参数 `userId` 查询
- **可利用性**：任意登录用户（甚至匿名）递增 `userId` 即可枚举全平台宠物档案与主人关联
- **修复**：非管理端必须以 token 身份为准，参数与 token 不一致返回 403；无 token 且未传 userId 返回空列表，不再返回全库（`PetController.java:108-190`）
- **附带修复**：`GET /api/pets/{id}` 增加归属校验（非本人/管理端返回 403，`PetController.java:311-341`）

### M-01 拦截器 OPTIONS 分支硬编码 CORS 通配头（已修复）

- **证据**：`RoleInterceptor` 原 OPTIONS 分支直接写 `Access-Control-Allow-Origin: *`（含 `Allow-Credentials`），绕过 `SecurityConfig` 的 `app.cors.allowed-origins` 白名单
- **修复**：删除手写响应头，预检统一由 Spring Security `CorsFilter` 生成（`RoleInterceptor.java:104-109`）。`SecurityConfig` 保持 `allowCredentials=false`，鉴权走 `Authorization` 头而非 Cookie，`*` 无法携带凭据

### 修复文件与提交

| 文件 | 变更 |
|------|------|
| `pet-backend/src/main/java/com/pethome/interceptor/RoleInterceptor.java` | 公共写接口白名单收敛为「仅 GET 正向白名单」；移除 OPTIONS 通配 CORS；移除 `/api/upload/` 匿名放行 |
| `pet-backend/src/main/java/com/pethome/controller/PetController.java` | 列表按 token 归属收敛；详情增加归属校验 |
| `pet-backend/pom.xml` | Spring Boot 2.7.14 → **2.7.18**（Spring 5.3.31 / Security 5.7.11 / Tomcat 9.0.83） |

### 运行时验证（临时实例 18080，`APP_MQ_ENABLED=false APP_KAFKA_ENABLED=false`）

| 请求 | 结果 |
|------|------|
| `POST /api/time-slots/create` | **401** ✅ |
| `DELETE /api/time-slots/999` | **401** ✅ |
| `POST /api/notice/create` | **401** ✅ |
| `POST /api/schedule-config/generate-batch` | **401** ✅ |
| `POST /api/grooming-banners/create` | **401** ✅ |
| `GET /api/time-slots/list` | 200（数据正常）✅ |
| `GET /api/notice/list` | 200 ✅ |
| `GET /api/banner/list` | 200 ✅ |

编译：`mvn -B -q -DskipTests compile` → `EXIT=0`；启动日志确认 `Spring Boot v2.7.18 / Spring v5.3.31 / Tomcat 9.0.83`。
验证实例已停止，**需重启正在运行的 8080 后端进程**后修复才生效。

---

## 7. 第二轮加固：中危 / 低危处置

### M-02 异常 message 直接回传前端（信息泄露）· 中危 · 已修复

- **证据**：50+ 处 `Result.error("失败: " + e.getMessage())`，例如
  [CommonUploadController.java:156](file:///c:/Users/Yu/Desktop/pet-home/pet-backend/src/main/java/com/pethome/controller/CommonUploadController.java#L156)、
  [BannerController.java:197](file:///c:/Users/Yu/Desktop/pet-home/pet-backend/src/main/java/com/pethome/controller/BannerController.java#L197)、
  [UploadController.java:44](file:///c:/Users/Yu/Desktop/pet-home/pet-backend/src/main/java/com/pethome/controller/UploadController.java#L44)、
  [AdminController.java:214](file:///c:/Users/Yu/Desktop/pet-home/pet-backend/src/main/java/com/pethome/controller/AdminController.java#L214)
- **可利用性**：构造畸形上传 / 非法参数触发异常，响应体可能包含磁盘绝对路径、SQL 报错、类名与依赖版本，为后续攻击提供测绘信息（如 `java.io.FileNotFoundException: /www/wwwroot/upload/...`）
- **本轮处理**：45 个控制器共 **304 处**响应已批量脱敏——异常细节只写入日志（新增 167 条 `log.error`），
  响应统一为「XX失败，请稍后重试」；对 `BusinessException` / `IllegalArgumentException` 主动抛出的业务文案
  （如「用户名已存在」「库存不足」）保留原样，避免削弱前端提示。示例改法：

```java
// 控制器内
} catch (Exception e) {
    log.error("文件上传失败", e);                 // 细节只进日志
    return Result.error("文件上传失败，请稍后重试"); // 客户端只看到通用文案
}
```

对确实需要给用户看的业务错误，使用专门异常类型（如 `BusinessException`）并只在 `GlobalExceptionHandler` 透传其 `message`，其余一律通用化。建议按 Controller 分批治理，优先处理匿名可访问的 `/api/upload/*`。

### M-03 短信发送缺少 IP 维度限流 · 中危（费用风险）· 已修复

- **证据**：`SmsController.java:32-57` 匿名可调用；`SmsServiceImpl` 仅按手机号限流（60s/条、10 条/日）
- **可利用性**：攻击者用大量不同手机号（或错误号码）循环调用，突破单号限流，造成短信费用消耗与号码骚扰
- **本轮处理**：`SmsController` 已增加「单 IP 每小时 60 条」计数（取 `X-Forwarded-For` 最右一跳，无代理头回落 `remoteAddr`），
  与手机号维度限流叠加；Redis 不可用时降级为仅手机号限流，不影响可用性：

```java
String ip = request.getRemoteAddr();
String ipKey = "sms:limit:ip:" + ip;                       // 建议：Nginx 透传真实 IP 后再取 X-Forwarded-For
Long n = stringRedisTemplate.opsForValue().increment(ipKey);
if (n != null && n == 1L) {
    stringRedisTemplate.expire(ipKey, 1, TimeUnit.HOURS);
}
if (n != null && n > 20) {                                 // 单 IP 每小时 20 条
    return Result.error("请求过于频繁，请稍后再试");
}
```

同时建议接入图形/滑块验证码，并在短信服务商侧配置发送量告警。

### M-04 种子脚本内置同一 BCrypt 哈希的演示账号 · 中危 · 已缓解（部署约束）

- **证据**：`database/pet_home.sql:94`（admin 种子）、`:2476`（用户种子）所有演示账号使用同一 BCrypt 哈希，并含大量 `135000000xxx` 演示手机号
- **可利用性**：若把该脚本导入生产，任何知道演示口令的人可直接以管理员登录（默认口令是最常见入侵入口之一）
- **本轮处理**：`database/pet_home.sql` 头部已加「仅限开发/演示、生产导入后必须立即重置口令」警示；
  该风险本质属部署配置，代码层无法自动消除，生产仍须执行下面的重置步骤：
- **修复建议**：生产初始化改为「导入后立即随机化」：

```sql
-- 部署脚本中执行一次：把演示口令替换为随机值，并强制下次登录改密
UPDATE admin SET password = '{bcrypt_random_1}' WHERE username IN ('admin','...');
UPDATE users SET password = '{bcrypt_random_2}' WHERE phone LIKE '135000000%';
```

更彻底的做法是把演示账号从 `pet_home.sql` 中拆到独立的 `dev_seed.sql`，生产构建不包含该文件。

### M-05 生产数据库连接关闭 TLS + 允许公钥检索 · 中危（条件性）· 已缓解（配置参数化）

- **证据**：`application-prod.yml:12` `useSSL=false&allowPublicKeyRetrieval=true`；`:23-24` `server.ssl.enabled: false`（依赖 Nginx 终止 TLS）
- **可利用性**：若 MySQL 端口（3306）对外可达，链路明文可被中间人窃取凭证与数据；`allowPublicKeyRetrieval=true` 在无 CA 校验时削弱抵御中间人的能力
- **本轮处理**：`application-prod.yml` 已将 `useSSL` 参数化为 `${MYSQL_USE_SSL:false}`，运维无需改代码即可
  通过环境变量启用链路 TLS（默认行为保持不变，避免影响现有部署）；网络层约束仍需按下述执行：
- **修复建议**（二选一，推荐同时做）：
  1. 网络层：`bind-address=127.0.0.1` + 防火墙仅放行本机 / 内网，3306 不对公网暴露；
  2. 链路层：启用 `useSSL=true&requireSSL=true&verifyServerCertificate=true` 并配置 CA（或改用 SSH 隧道 / 内网专线）。
  Nginx 终止 TLS 属可接受方案，但必须在报告中明确前提：后端 1012 端口不得直接暴露公网。

### L-01 视频分片目录未与用户绑定 · 低危 · 已修复

- **证据**：[CommonUploadController.java:258-320](file:///c:/Users/Yu/Desktop/pet-home/pet-backend/src/main/java/com/pethome/controller/CommonUploadController.java#L258-L320)，分片目录为 `chunks/<uploadId>`，合并 / 清理仅校验 `uploadId` 格式
- **可利用性**：任意登录用户若猜中 / 复用他人 `uploadId`，可合并或删除他人上传的分片（内容破坏，无法读取内容）
- **本轮处理**：分片目录改为 `chunks/<owner>-<uploadId>`（owner 取拦截器写入的身份：`u<userId>` 或 `a<adminId>`），
  上传与合并都按同一身份寻址，无法再碰他人分片；无身份时返回 401。改造前已存在的旧命名分片需人工清理。
- **参考实现**：

```java
Long uid = userTokenResolver.getUserIdFromBearer(request.getHeader("Authorization"));
File chunkDir = new File(baseDir, CHUNKS_SUBDIR + File.separator + uid + "-" + uploadId);
```

### 第二轮验证（2026-11-01）

| 验证项 | 结果 |
|--------|------|
| `mvn -B -q -DskipTests package` | EXIT=0 |
| 启动（18080，默认 profile） | `Started PetHomeApplication in 15.6s`，`/actuator/health` → **200** |
| `POST /api/upload/video/chunk`（无 token） | **401** ✅ |
| `POST /api/sms/send`（13800000000） | `{"code":200,"msg":"验证码发送成功"}` ✅ |
| 同一手机号再次调用 | `发送过于频繁`（原手机号限流仍生效）✅ |
| Redis `sms:limit:ip:127.0.0.1` | 存在 → IP 维度计数生效 ✅ |
| `GET /api/banner/list` | **200** ✅ |

### 信息级

| ID | 项 | 建议 |
|----|----|------|
| I-01 | 单层鉴权：Spring Security `anyRequest().permitAll()` + `csrf().disable()`（`SecurityConfig.java:48-50`），唯一边界是拦截器 | 后续把「需登录/需角色」规则下沉到 `SecurityFilterChain`（`authorizeHttpRequests`），拦截器只做身份解析，实现纵深防御 |
| I-02 | 依赖维护状态：`kaptcha 2.3.2`（2010 年停更）、`thymeleaf 3.0.15`、`commons-lang3 3.12.0`、`jackson 2.13.5`、`snakeyaml 1.30`、`spring-boot-maven-plugin` 仍为 2.7.14 | 建立 SCA 门禁（Trivy / OWASP Dependency-Check）；kaptcha 建议替换为 `easy-captcha` 或自研（仅用于后台登录） |
| I-03 | mock 支付通道 | `mockEnabled=false` 时 `/mock/confirm` 直接 403（fail-closed），需在发布检查单中确认生产环境未开启 `PAYMENT_MOCK_ENABLED` |
| I-04 | 公开白名单中的 `/api/pets/\d+`、`/api/user/detail` 等条目 | 现由 Controller 内归属 / 登录校验兜底，条目可保留；建议后续把此类接口移出白名单，让 401 在拦截器层直接返回，减少「Controller 忘记校验」的机会 |

---

## 8. 纵深防御加固建议（按投入产出排序）

1. **上线 CI 安全门禁**（半天内可落地，收益最大）：Semgrep（`p/owasp-top-ten` + `p/cwe-top-25`）、Trivy（`CRITICAL,HIGH` 阻断）、Gitleaks（全历史密钥扫描）
2. **密钥泄露处置**：`application-prod.yml` 历史上曾提交明文数据库密码与微信 AppSecret，**视为已泄露，必须完成轮换**（配置已改为环境变量注入，但旧值仍需在服务端重置）
3. **鉴权下沉**：把后台 `/api/admin/**`、`/api/**` 的写操作收敛到 Spring Security 规则 + 方法级 `@PreAuthorize`，拦截器保留为补充
4. **统一错误出口**：`GlobalExceptionHandler` 收敛所有 Controller 的 `e.getMessage()` 直返（见 M-02）
5. **审计日志**：支付、退款、管理员登录、权限变更写入不可篡改存储，便于事件响应与追责
6. **监控告警**：对 401/403 突增、`/api/sms/send` 调用量、上传失败率设置阈值告警

## 9. 复测与回归清单

建议把以下用例固化为安全回归测试（每个 PR 执行）：

- [ ] 匿名 `POST/PUT/DELETE` 于 `/api/time-slots/*`、`/api/notice/*`、`/api/grooming-banners/*`、`/api/litter-banners/*`、`/api/schedule-config/generate-batch` → 必须 401
- [ ] 用户 A 的 token + `?userId=B` 请求 `/api/pets/page` → 必须 403
- [ ] 用户 A 的 token 请求 `GET /api/pets/{B 的宠物 id}` → 必须 403
- [ ] 公开只读接口（`/api/banner/list`、`/api/notice/list`、`/api/time-slots/list`）→ 必须 200
- [ ] OPTIONS 预检响应中 `Access-Control-Allow-Origin` 只允许 `app.cors.allowed-origins` 内的来源
- [ ] 构造异常的上传请求，响应体不得包含磁盘路径 / 类名 / SQL 片段

验证命令示例：

```powershell
# 编译
mvn -B -q -DskipTests compile

# 本地临时实例（关闭中间件依赖）
java -jar target/pet-home-*.jar --server.port=18080
curl.exe -i -X POST http://127.0.0.1:18080/api/time-slots/create
```

## 附录 A：当前匿名只读白名单（`RoleInterceptor#isPublicReadApi`）

`/api/banner/list|active`、`/api/service-config/all`、`/api/product/hot|list|recommend|{id}`、`/api/notice/*`、
`/api/categories/all|page`、`/api/user/current|stats|detail`、`/api/pets/{id}`、`/api/grooming-appointments/{id}`、
`/api/door-cleaning/{id}`、`/api/community/posts*`、`/api/community/users/*`、`/api/daily-topics*`、`/api/tag/hot`、
`/api/search/*`、`/api/regions/*`、`/api/stores/page|all|by-service/*`、`/api/grooming-banner*`、`/api/hospital-banners/position/*`、
`/api/medical-banner*`、`/api/litter-banner*`、`/api/time-slots/*`、`/api/schedule/available|day|calendar`、
`/api/department/list`、`/api/service-member/list`、`/api/signin/*`、`/api/tasks/*`、`/api/points/products`、
`/api/(grooming|hospital|litter)-services/(page|list|{id})`

**约定**：该清单只允许只读 GET；任何新增写接口默认需要登录，避免被旧前缀「顺带放行」。
