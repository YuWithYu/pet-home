# 模拟支付通道（Mock Payment）

> 用途：在没有微信支付商户凭证的本地环境，跑通与生产完全一致的「下单 → 支付 → 状态机流转」。
> 定位：**仅本地联调**。生产环境默认关闭（fail-closed），不设环境变量即走真实微信支付。

## 什么时候需要它

真实微信支付（JSAPI）有两个前提：

1. 用户通过微信授权登录（有 openid）——账号密码注册的用户没有 openid；
2. 服务端配置商户号、API 密钥与证书（`WECHAT_PAY_MCH_ID` 等环境变量，见 [configuration.md](configuration.md)）。

本地开发通常两者都没有，此时调用 `POST /api/payment/prepay` 会返回：

```json
{ "code": 400, "msg": "请使用微信登录后支付" }
```

开启 mock 通道后，支付流程跳过微信收银台，由「模拟确认接口」驱动同一套状态机。

## 开启方式

启动后端前设置环境变量（对应 `application.yml` 的 `wechat.pay.mock-enabled`，默认 `false`）：

```powershell
# Windows PowerShell
$env:WECHAT_PAY_MOCK_ENABLED = "true"
mvn spring-boot:run
```

```bash
# Linux / macOS
WECHAT_PAY_MOCK_ENABLED=true mvn spring-boot:run
```

## 工作原理

开关只影响 `PaymentController` 的三个端点，对比如下：

| 端点 | 真实通道 | mock 通道 |
|---|---|---|
| `POST /api/payment/prepay` | 校验 openid → 微信统一下单 → 返回收银台参数 | 跳过微信，返回 `mock` 标记；服务预约仍幂等建 `payment` 待支付单 |
| `POST /api/payment/query` | DB 未支付时查微信侧状态并补偿更新 | 不查微信，DB 状态即最终状态（返回 `NOTPAY`） |
| `POST /api/payment/mock/confirm` | 不存在该路径语义——**未开启时一律 403** | 模拟「用户付款成功」，驱动与微信回调相同的状态机 |

两类业务的确认语义（与真实回调复用同一状态机，这是 mock 通道不改生产行为的关键）：

| 业务 | 请求体 | 确认动作 |
|---|---|---|
| 商品订单 | `{"orderNo":"ORD..."}` 或 `{"orderId":1}` | 订单状态 0→1 条件更新 + 清除超时取消 key（与 `/notify` 回调第 8/9 步同款），幂等：已支付直接返回成功 |
| 服务预约 | `{"bizType":"APPOINTMENT","serviceType":"grooming\|hospital\|door-cleaning","bizId":1}` | `markPaymentPaid` 条件更新（与回调同款） |

## 安全边界（为什么生产是安全的）

- **默认关闭**：`mock-enabled` 默认 `false`；生产部署不设 `WECHAT_PAY_MOCK_ENABLED` 即走真实支付。
- **fail-closed**：`mock/confirm` 在通道未开启时一律返回 `403 模拟支付未开启`，不存在「误开即生效」的路径。
- **越权校验不变**：mock 确认同样校验 token 归属（订单/预约归属 userId ≠ 当前用户 → 403；未登录 → 401）。
- **状态机复用**：mock 确认调用的是回调同款条件更新，不存在绕过状态机的私有写入路径。

## 小程序端行为

无需任何前端开关。前端根据 `prepay` 返回值自动分流（`payment/index.vue`、`order/confirm.vue`）：

1. `prepay` 返回含 `mock` 标记 → 显示「支付处理中」约 1.5 秒（模拟收银台节奏）；
2. 调用 `api.mockConfirmPay(...)`（`POST /api/payment/mock/confirm`，封装见 `common/js/api.js`）；
3. 确认成功后进入与真实支付完全相同的后续页面流。

## 命令行验证

```powershell
# 1. 登录拿 token（用你注册的测试账号）
# 注意：PowerShell 下 JSON 用单引号包裹、内部双引号原样传递。
# 写成 -d "{\"...\"}" 会把反斜杠原样传给 curl，导致后端解析出 500。
curl.exe -s -X POST http://localhost:8080/api/user/login `
  -H "Content-Type: application/json" `
  -d '{"username":"test","password":"your-password"}'

# 2. 模拟支付确认（商品订单）
curl.exe -s -X POST http://localhost:8080/api/payment/mock/confirm `
  -H "Authorization: Bearer <token>" `
  -H "Content-Type: application/json" `
  -d '{"orderNo":"ORD..."}'
```

成功响应：

```json
{ "code": 200, "msg": "success", "data": { "paid": true, "orderNo": "ORD..." } }
```

未开启 mock 时的响应：

```json
{ "code": 403, "msg": "模拟支付未开启" }
```

## 排错

| 症状 | 解决 |
|---|---|
| 403「模拟支付未开启」 | 后端没有带 `WECHAT_PAY_MOCK_ENABLED=true` 启动；在同一个 shell 设置变量后重启后端 |
| 仍然报 400「请使用微信登录后支付」 | `prepay` 走的是真实通道，说明 mock 变量未生效（检查启动日志或确认是同一进程） |
| 400「订单状态不允许支付」 | 订单已支付或已取消；先调 `POST /api/payment/query` 确认当前状态 |
| 小程序支付页无反应 | 确认重新编译了小程序（HBuilderX 修改后需重新运行到微信开发者工具） |
