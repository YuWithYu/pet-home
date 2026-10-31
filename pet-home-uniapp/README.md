# pet-home-uniapp — 微信小程序

宠物之家 C 端小程序：uni-app（Vue 2 语法）+ HBuilderX 运行到微信开发者工具。

## 运行

1. HBuilderX 打开 `pet-home-uniapp` 目录（需安装 uni-app 编译插件）。
2. 菜单「运行 → 运行到小程序模拟器 → 微信开发者工具」。
3. 微信开发者工具需开启「服务端口」（设置 → 安全 → 服务端口），供 HBuilderX 调起。

前提：后端（8080）已启动。

## 环境切换

| 构建 | 后端地址 | 定义位置 |
|---|---|---|
| 开发（HBuilderX 运行 / `dev:mp-weixin`） | `http://localhost:8080` | `main.js` 的 `API_BASE` |
| 发行（`build:mp-weixin`） | `http://localhost:8080` | 同上，统一连本地后端 |

运行时覆盖（联调连测试机后端时用）：

- 启动参数：编译参数加 `?apiBaseUrl=http%3A%2F%2F192.168.x.x%3A8080`（`util.js` 解析）。
- 本地存储：key `PETHOME_API_BASE_URL`（`common/js/util.js`）。

> 微信开发者工具在 Windows 上可能无法访问内网 IP，默认已将图片域名回退处理为 `localhost`（见 `util.js` 注释）。

## 必查配置（真机/发布前）

| 配置 | 位置 | 说明 |
|---|---|---|
| DCloud appid | `manifest.json` 基础配置 | 当前为占位符 `__UNI__12345678`，发布前替换为你自己的 |
| 微信小程序 appid | `manifest.json` → `mp-weixin` → `appid` | 当前为 `wxcf8868d6bfdc0d6f`，按需替换 |
| request 合法域名 | 微信公众平台 | 仅本地运行时无需配置；若真机调试，需在开发者工具勾选「不校验合法域名」 |

## 支付联调

没有微信商户凭证时开启模拟支付通道（后端环境变量 `WECHAT_PAY_MOCK_ENABLED=true`），小程序无需任何改动即可走完支付流程，见 [../docs/mock-payment.md](../docs/mock-payment.md)。

## 目录速览

```
pet-home-uniapp/
├── pages/            # Tab 页（首页/商城分类/社区/消息/我的）
├── pages-auth/       # 登录、注册、找回密码
├── pages-goods/      # 商品列表/详情/搜索/评价
├── pages-cart/       # 购物车
├── pages-community/  # 发帖/话题/签到
├── pages-message/    # 评论、点赞、关注、通知
├── order/            # 订单确认/列表/详情/物流/退款/评价
├── payment/index.vue # 收银台（含 mock 支付分流）
├── appointment/      # 三类服务预约（美容/医院/上门清理）
├── user/             # 个人中心（宠物档案/优惠券/健康工具）
├── common/js/api.js  # 全部接口封装（含 mockConfirmPay）
├── common/js/util.js # 网络环境/图片域名处理
└── manifest.json     # appid 等平台配置
```
