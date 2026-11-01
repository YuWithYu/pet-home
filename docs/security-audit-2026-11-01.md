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
第三轮加固把管理端鉴权下沉到 Spring Security（`BearerAuthenticationFilter` + `/api/admin/**` 要求 `ROLE_ADMIN`），
不再依赖 `RoleInterceptor` 单点防护；同时接入 Semgrep / Trivy / Gitleaks 的 CI 安全门禁。

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
| I-01 | 单层鉴权：Spring Security 原为 `anyRequest().permitAll()`，唯一边界是拦截器 | **已修复（部分）**：新增 `BearerAuthenticationFilter` 写入 `SecurityContext`，`/api/admin/**` 要求 `ROLE_ADMIN`、`/api/admin/self/**` 允许 `ADMIN/STAFF`，形成第二道闸门；接口级 `@PreAuthorize` 仍可作为后续增强 |
| I-02 | 依赖维护状态：`kaptcha 2.3.2`（2010 年停更）、`thymeleaf 3.0.15`、`commons-lang3 3.12.0`、`jackson 2.13.5`、`snakeyaml 1.30`、`spring-boot-maven-plugin` 仍为 2.7.14 | 建立 SCA 门禁（Trivy / OWASP Dependency-Check）；kaptcha 建议替换为 `easy-captcha` 或自研（仅用于后台登录） |
| I-03 | mock 支付通道 | `mockEnabled=false` 时 `/mock/confirm` 直接 403（fail-closed），需在发布检查单中确认生产环境未开启 `PAYMENT_MOCK_ENABLED` |
| I-04 | 公开白名单中的 `/api/pets/\d+`、`/api/user/detail` 等条目 | 现由 Controller 内归属 / 登录校验兜底，条目可保留；建议后续把此类接口移出白名单，让 401 在拦截器层直接返回，减少「Controller 忘记校验」的机会 |

---

## 8. 纵深防御加固建议（按投入产出排序）

1. ~~**上线 CI 安全门禁**~~（**已落地**）：`.github/workflows/security.yml` 含 Semgrep（`p/owasp-top-ten` + `p/cwe-top-25`）、Trivy（`CRITICAL,HIGH` 阻断）、Gitleaks（全历史密钥扫描）
2. **密钥泄露处置**：`application-prod.yml` 历史上曾提交明文数据库密码与微信 AppSecret，**视为已泄露，必须完成轮换**（配置已改为环境变量注入，但旧值仍需在服务端重置）
3. ~~**鉴权下沉**~~（**已落地粗粒度部分**）：`BearerAuthenticationFilter` + `SecurityFilterChain` 已对 `/api/admin/**` 强制 `ROLE_ADMIN`；接口级 `@PreAuthorize` 可作为后续增强，拦截器保留为补充
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

---

## 10. 第四轮：细粒度越权复审（2026-11-01）

### 10.1 背景与方法

第三轮把 `/api/admin/**` 下沉到 Spring Security 后，仍有大量业务接口挂在 `/api/orders/**`、`/api/product/**`、
`/api/verify/**`、`/api/hospital-appointments/**` 等非 admin 前缀下。这些路径只要求「登录」，不做角色/归属校验，
因此**任意普通 C 端用户 token 即可调用管理端业务**。本轮以「普通用户 token + 越权路径」逐接口实测取证：

> 注意本项目响应约定：被拒绝时 HTTP 仍返回 200，业务码在响应体 `code` 字段。复测一律按 `body.code` 判定。

身份读取统一收敛到新增工具类 [RequestIdentity.java](file:///c:/Users/Yu/Desktop/pet-home/pet-backend/src/main/java/com/pethome/security/RequestIdentity.java)，
只读 `RoleInterceptor` 写入的 request attribute（`role` / `userId` / `adminId` / `serviceStoreId`），不重复解析 token。

### 10.2 发现与修复

| # | 问题 | 位置 | 影响 | 修复 |
|---|------|------|------|------|
| 1 | 退款列表无角色校验 | `OrderController#getRefundList` | 普通用户可读全平台退款单（含订单号、金额、原因） | 非 `admin/store_admin/staff` → 403 |
| 2 | 退款审批无角色校验 | `OrderController#approveRefund/rejectRefund` | 普通用户可尝试审批他人退款（服务层仅拦 staff，未拦 user） | 非管理员 → 403 |
| 3 | 物流详情 IDOR | `OrderController#getLogistics` | 任意登录用户按订单号读取他人收货人、电话、地址 | 非管理端必须 `order.userId == 当前用户` |
| 4 | 物流写入 IDOR | `OrderController#updateLogistics` | 任意登录用户可修改他人订单物流 | 非管理端 → 403 |
| 5 | 铲屎预约列表全量泄露 | `OrderController#getLitterAppointments` | 普通用户拿到全平台预约（含手机号、地址） | 管理端全量；普通用户仅 `getUserAppointments(userId)` |
| 6 | 发货仓管理无角色校验 | `OrderController` `/warehouses` GET/POST/DELETE | 普通用户可读/建/删发货仓 | 读需 `admin/staff`；写仅管理员 |
| 7 | 商品写接口无角色校验 | `ProductController` create/update/delete | 普通用户可上架/改价/下架商品 | 非管理员 → 403 |
| 8 | 核销码查询与今日核销记录 | `VerifyController` `check-code`、`today-records` | 普通用户可凭核销码查询/枚举核销数据 | 非 `admin/staff` → 403 |
| 9 | `/api/user/current` 匿名回退 + 用户名枚举 | `UserController#getCurrentUser` | 匿名可拿到用户资料骨架；可按 username 探测账号存在性 | 匿名 → 401；统一「用户不存在或已被禁用」 |
| 10 | 通知/时间段/排班写接口 | `NotificationController`、`TimeSlotController`、`ServiceScheduleController`、`ScheduleConfigController#generate-batch` | 普通用户可发站内通知、改可预约时段、批量生成排班 | 写接口非 `admin/staff` → 403 |
| 11 | 客服发信与社区身份伪造 | `MessageController` `/messages/service`、`/follows`、`/comments` | 普通用户可向任意门店发客服消息；按参数冒充他人查询关注/评论 | 发信非 `admin/staff` → 403；列表强制登录态 |
| 12 | 每日专题评论冒充 | `DailyTopicController#addTopicComment` | 请求体传 `userId` 即可以他人身份发言 | 评论人取登录态，未登录 → 401 |
| 13 | 积分兑换地址归属 | `PointsExchangeServiceImpl` | 传他人 `addressId` 可把他人地址设默认/作为收货地址 | 仅接受 `address.userId == 当前用户`，否则忽略并记日志 |
| 14 | 预约跨门店可见性 | `HospitalAppointmentController`、`AppointmentController` | staff 可按 `memberId`/预约 id 读取其他门店客户预约 | 统一 `serviceStoreId` 收敛；跨店 → 403 |

另外修复了上一提交遗留的 4 处编译错误（lambda 捕获可变局部变量、`IOException` 未捕获）：
`VerifyController`、`UserController`、`MessageController`、`UserFollowController`、`LitterServiceBannerController`。
根因是 IDE 写入的增量 class 掩盖了真实错误，**必须删除 `target/classes` 后全量编译才能暴露**。

### 10.3 复测结果（18080 临时实例，`APP_MQ_ENABLED=false APP_KAFKA_ENABLED=false`）

| 用例 | 期望 | 实测 `body.code` | 结果 |
|------|------|------------------|------|
| 匿名 `GET /api/admin/staff/list` | 401 | 401（HTTP 401） | PASS |
| admin `GET /api/admin/staff/list` | 200 | 200 | PASS |
| staff `GET /api/admin/staff/list` | 403 | 403（HTTP 403） | PASS |
| 用户 `POST /api/product/create` | 403 | 403「无权创建商品」 | PASS |
| 用户 `GET /api/orders/refunds` | 403 | 403「无权查看退款列表」 | PASS |
| 用户 `PUT /api/orders/refunds/56/approve` | 403 | 403「无权审批退款」 | PASS |
| 用户 `GET /api/orders/56/logistics` | 403 | 403「无权查看此订单」 | PASS |
| 用户 `PUT /api/orders/56/logistics` | 403 | 403「无权操作此订单物流」 | PASS |
| 用户 `GET /api/verify/check-code/123456` | 403 | 403「无权查询核销码」 | PASS |
| 用户 `GET /api/verify/today-records` | 403 | 403「无权查看核销记录」 | PASS |
| 匿名 `GET /api/user/current` | 401 | 401「未登录，请先登录」 | PASS |
| 用户 `POST /api/notifications/create` | 403 | 403「无权创建通知」 | PASS |
| 用户 `POST /api/time-slots/create` | 403 | 403「无权操作时间段配置」 | PASS |
| 用户 `POST /api/schedule-config/generate-batch` | 403 | 403「无权生成排班」 | PASS |
| 用户 `POST /api/messages/service` | 403 | 403「无权发送客服消息」 | PASS |
| staff 查他店 `GET /api/hospital-appointments/member/list?memberId=34` | 403 | 403「无权查询其他门店的服务人员预约」 | PASS |
| staff 查本店同参数 `memberId=36` | 200 | 200 | PASS |
| admin `GET /api/orders/refunds` | 200 | 200（5 条） | PASS |
| 用户 `GET /api/orders/litter-appointments` | 200 | 200（仅本人，0 条） | PASS |
| 用户 `GET /api/orders/warehouses` | 403 | 403「无权查看发货仓」 | PASS |
| admin `GET /api/orders/warehouses` | 200 | 200 | PASS |
| 用户 `POST /api/orders/warehouses` | 403 | 403「无权维护发货仓」 | PASS |
| 用户 `DELETE /api/orders/warehouses/999` | 403 | 403「无权维护发货仓」 | PASS |
| 用户发专题评论并伪造 `userId=1` | 以本人身份落库 | 返回 `userId=83`（登录态） | PASS |

- 编译：删除 `target/classes` 后 `mvn -B -DskipTests package` → **BUILD SUCCESS**（430 源文件全量编译）。
- 例外说明：积分兑换地址归属用例（`POST /api/points/exchange`，`productId=1`）因商品不存在停在业务前置校验（500「商品不存在」），
  未能触达地址分支；该修复以代码复核为准（仅接受本人 `addressId`）。
- 验证实例已停止，**需重启正在运行的 8080 后端进程**后修复才生效。

### 10.4 后续建议

1. 把上述非 `/api/admin/**` 的管理端接口迁移到 `/api/admin/**` 前缀，让 Spring Security 统一兜底，避免逐接口补校验。
2. 将本节的 `body.code` 用例固化为 PR 安全回归脚本（`scripts/` 下），CI 中执行。
3. `GET /api/admin/staff/list` 当前会把 `Admin` 实体整行返回（`password=null` 已置空），建议改为 DTO 只输出必要字段。

## 11. 第五轮：非 admin 前缀接口越权与 PII 复审（2026-11-01）

前提：`RoleInterceptor` 对已登录普通用户（`role=user`）除 `/api/admin/**` 与 staff 白名单外一律放行，
资源归属必须由各 Controller / Service 自行校验；本轮按此前提逐接口复核并修复下列问题。

### 11.1 修复清单

| # | 风险 | 位置 | 修复 |
|---|------|------|------|
| 1 | staff 可写任意用户主数据（积分/等级/魅力/状态/密码） | `UserController#updateUser`、`PUT /{id}`、`PUT /{id}/status` | 统一收紧为 `RequestIdentity.isAdmin`（admin/store_admin）；C 端仅保留本人安全字段 |
| 2 | `/api/user/login` 无失败锁定，可绕过管理员锁定爆破 | `UserAuthAppServiceImpl` | Redis 计数 `user:login:fail:`，5 次/15 分钟锁定 |
| 3 | 洗护/医院/铲屎预约 `/page` 分页 IDOR（C 端可读全平台预约含手机号、地址） | 三个预约 Controller | 仅 staff/admin + 门店 scope |
| 4 | 公开只读接口提前 return 导致管理端读接口按匿名降级 | `RoleInterceptor` | 公开只读继续解析身份后放行 |
| 5 | 关注/推荐/@提及/评价接口把 `username`（历史数据即手机号）回传 | `UserFollowController`、`CommunityFeedAppServiceImpl`、`MessageRecommendationAppServiceImpl`、`ServiceAppointmentRatingController` | 统一回传展示名（昵称或「用户+id」），禁止回退 `username` |
| 6 | staff 可跨门店批量生成排班 | `ScheduleConfigController#generateBatch` | 校验 `memberIds` 属于本门店并强制 `storeId=scope` |
| 7 | 6 位核销码可枚举且跨门店返回完整预约 PII | `VerifyController#checkCode`、`/today-records` | 仅 staff/admin + 门店 scope，跨店 403 |
| 8 | 任意管理端账号可冒充平台官方客服（`fromId=-9999`） | `MessageController#sendPlatformMessage` | 仅 `isPlatformAdmin` |
| 9 | 店长可读全平台客服会话 | `/conversations/all-platform` | 仅 `isPlatformAdmin` |
| 10 | 可传任意 `serviceStoreId` 跨店读门店客服会话 | `/conversations/all-outlet` | 非平台管理员强制本门店，无归属 403 |
| 11 | 任意用户传 `storeId` 读取指定店铺全部客服会话 | `/conversations/all-service` + `MessageAppServiceImpl` | Controller 要求 `isAdmin`；Service 侧非管理端 token 不再回退请求参数，未绑定店铺返回空列表 |
| 12 | 取消/变更申请审批无门店限制（跨店审批他人订单、队列泄露联系方式） | 两个申请 Controller + 新建 `AppointmentStoreGuard` | `listPending` 按 scope 过滤，`approve`/`reject` 增加归属校验 403 |
| 13 | 推荐/兴趣接口可传他人 `userId` 读取其行为画像与兴趣标签；`limit` 无上限 | `RecommendationController` | `userId` 一律取自登录态，`limit` 收敛至 1–100 |

### 11.2 复测结果（18080 临时实例，`APP_MQ_ENABLED=false APP_KAFKA_ENABLED=false`）

新增用例全部 PASS：

| 用例 | 期望 `body.code` | 实测 |
|------|------------------|------|
| 用户 `GET /api/messages/conversations/all-service?storeId=1` | 403 | 403「无权查看客服会话列表」 |
| 用户 `GET /api/messages/conversations/all-outlet?serviceStoreId=1` | 403 | 403「账号未绑定服务门店，无权查看门店客服会话」 |
| 用户 `GET /api/messages/conversations/all-platform` | 403 | 403「仅平台客服可查看平台会话」 |
| 匿名 `GET /api/messages/conversations/all-service` | 401 | 401（HTTP 401） |
| 用户 `GET /api/recommendation/user-interests?userId=1` | 200（仅本人） | 200，返回本人空兴趣 |
| 用户 `GET /api/recommendation/collaborative-filtering?userId=1` | 200（仅本人） | 200 |
| staff 跨门店 `POST /api/schedule-config/generate-batch`（`memberIds=34`） | 403 | 403「无权为其他门店的服务人员生成排班」 |
| 用户 `PUT /api/user/83`（C 端走 POST `/api/user/update`） | 403 | 403「无权更新用户信息」 |
| admin `PUT /api/user/83` | 200 | 200 |
| 用户 `POST /api/user/update`（本人） | 200 | 200 |

C1–C47 全部沿用上表并复测通过；编译 `mvn -B package "-Dmaven.test.skip=true"` → **BUILD SUCCESS**（475 源文件）。

- 验证实例已停止，**需重启正在运行的 8080 后端进程**后修复才生效。

## 12. 第六轮：匿名身份回退与个人社交数据越权（2026-11-01）

延续第 11 轮前提：`role=user` 的已登录用户除管理端前缀外一律放行，归属校验必须在接口内完成。
本轮聚焦「未登录时回退 query 参数」与「个人社交数据按 query userId 读取」两类模式。

### 12.1 修复清单

| # | 风险 | 位置 | 修复 |
|---|------|------|------|
| 1 | 未登录时回退 query `followerId`，可匿名替任意用户关注 | `UserFollowController#followUser` | 身份仅取 `request.userId`，为空直接 401，不回退 |
| 2 | 同上模式，可匿名强制取关任意用户 | `UserFollowController#unfollowUser` | 同上 |
| 3 | 传任意 `userId` 读取他人每日专题收藏列表 | `DailyTopicController#getCollectedTopics` | 一律用登录态；未登录 401 |
| 4 | 传任意 `userId` 探测他人专题收藏状态 | `DailyTopicController#getTopicCollectStatus` | 一律用登录态；未登录 401 |
| 5 | 专题详情点赞/收藏态可按 query `userId` 伪造 | `DailyTopicController#getTopicDetail` | 点赞/收藏态仅用登录态 |
| 6 | 专题评论点赞态可按 query `userId` 伪造 | `DailyTopicController#getTopicComments` | 同上 |
| 7 | 匿名 `GET /api/user/current` 回退「用户 ID=1」，泄露该用户手机号与积分 | `UserProfileAppServiceImpl#doGetCurrentUser` | 删除回退；无 token/无身份直接 401 |
| 8 | 传 `username` 可枚举任意账号资料 | 同上 | 仅允许查询当前登录账号，不一致 403 |
| 9 | 传任意 `userId` 读取他人关注动态（社交关系泄露） | `CommunityController#getFollowingPosts` | 一律用登录态；未登录 401 |
| 10 | 传任意 `userId` 读取他人 Feed 关注区/发现区 | `CommunityController#getPostFeed` | 个性化仅用登录态，匿名返回公共热门帖 |
| 11 | 传任意 `userId` 读取他人规则型推荐（兴趣画像） | `CommunityController#getRecommendPosts` | 同上 |
| 12 | 传任意 `userId` 读取他人 @ 候选（关注关系） | `CommunityController#getMentionUsers` | 一律用登录态；未登录 401 |
| 13 | 传任意 `userId` 读取他人视频推荐（兴趣画像） | `CommunityController#getVideoFeed` | 个性化仅用登录态，匿名走热门视频 |

### 12.2 复测结果（18080 临时实例，`APP_MQ_ENABLED=false APP_KAFKA_ENABLED=false`）

| 用例 | 期望 `body.code` | 实测 |
|------|------------------|------|
| 匿名 `POST/DELETE /api/community/follow`（带 `followerId=83`） | 401 | 401（HTTP 401） |
| 匿名 `GET /api/daily-topics/collected?userId=83` | 401 | 401 |
| 匿名 `GET /api/daily-topics/1/collect/status?userId=83` | 401 | 401 |
| 用户 `GET /api/daily-topics/collected?userId=1` | 200（仅本人） | 200，`topics: []` |
| 匿名 `GET /api/user/current` | 401 | 401「请先登录」 |
| 匿名 `GET /api/user/current?userId=83` | 200（公开字段） | 200，不含 `phone` / `points` |
| 匿名 `GET /api/user/current?username=admin` | 401 | 401「请先登录」 |
| 用户 `GET /api/user/current?username=admin` | 403 | 403「无权查看其他用户信息」 |
| 用户 `GET /api/user/current`（本人 token） | 200 且含 `phone` | 200 |
| 匿名 `GET /api/community/posts/following?userId=83` | 401 | 401 |
| 用户 `GET /api/community/posts/following?userId=1` | 200（仅本人） | 200 |
| 匿名 `GET /api/community/posts/feed?userId=83` | 200，`followPosts: []` | 200，关注区为空 |
| 匿名 `GET /api/community/posts/recommend?userId=83` | 200（公共推荐） | 200 |
| 匿名 `GET /api/community/posts/1/mention-users?userId=83` | 401 | 401 |
| 匿名 `GET /api/community/posts/video-feed?userId=83` | 200，算法为 `hot-video` | 200 |

C1–C57 原有用例全量回归通过；`mvn -B package "-Dmaven.test.skip=true"` → **BUILD SUCCESS**；
`scripts/check-architecture.ps1` 全部 PASS（基线已收紧至 0）。

- 验证实例已停止，**需重启正在运行的 8080 后端进程**后修复才生效。

## 13. 第七轮：残余低风险项收敛（2026-11-01）

第七轮针对前几轮遗留的 4 项低风险问题做定点收敛，其余高风险面（SQL 拼接、支付回调验签、actuator 暴露面、
JWT 密钥强度与生产 fail-fast）复核确认已具备防护，不再重复修改。

### 13.1 修复清单

| # | 风险 | 位置 | 修复 |
|---|------|------|------|
| 1 | 短信接口无条件信任 `X-Forwarded-For`，可伪造来源 IP 绕过按 IP 发送限流 | `SmsController#sendSms` | 仅当直连来源为可信代理（回环/内网）时才读取 XFF，且取最右一跳；否则一律用 `remoteAddr` |
| 2 | 匿名可枚举任意预约是否存在变更记录（预约号可探测） | `AppointmentChangeRequestController#hasAnyByAppointment` | 要求登录态或 staff/admin，否则 401 |
| 3 | staff 可跨用户读写购物车（店员不应参与购物车业务） | `CartController#isAdminContext` | 管理端上下文仅 `admin` / `store_admin`，移除 `staff` 与 `adminId` 兜底 |
| 4 | staff 可跨用户读取积分流水与积分统计（个人资产数据） | `UserController#getPointsRecords` / `getPointsStatistics` | 跨用户查询仅 `admin` 放行（`RequestIdentity.isAdmin`）；其余一律以登录态为准 |

### 13.2 复测结果（18080 临时实例 + 重启后 8080 抽检）

| 用例 | 期望 | 实测 |
|------|------|------|
| 匿名 `GET /api/appointment-change-request/has-any?…` | 401 | 401（HTTP 401） |
| 用户 `GET /api/appointment-change-request/has-any?…` | 200 | 200，`data: false` |
| staff `GET /api/cart/page?userId=83` | 403 | 403（HTTP 403） |
| admin `GET /api/cart/page?userId=83` | 200 | 200 |
| staff `GET /api/user/points/records?userId=83` | 403 | 403（拦截器 + 服务层双重拒绝） |
| 用户 `GET /api/user/points/records?userId=75` | 200（强制本人） | 200，返回本人流水 |
| 匿名 `GET /api/user/current` | 401 | 401「请先登录」 |

C1–C83 全量回归无 FAIL；`mvn -B package "-Dmaven.test.skip=true"` → **BUILD SUCCESS**；
`scripts/check-architecture.ps1` 全部 PASS。

- 8080 已重启（`java -jar target/pet-home-backend-1.0.0.jar`），上述关键项在 8080 复检通过，修复已生效。

