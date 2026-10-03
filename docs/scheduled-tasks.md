# 定时任务

后端共 7 个定时任务类（`pet-backend/.../task/`），由 `@EnableScheduling` 驱动。cron 表达式均为 Spring 6 段格式（秒 分 时 日 月 周）。

## 总览

| 任务类 | 频率 | 职责 |
|---|---|---|
| `OrderAutoCancelTask` | 每小时整点 | 兜底取消超时未付款订单 |
| `OrderAutoCompleteTask` | 每天 02:00 | 发货满 7 天自动完成 |
| `AppointmentAutoExpireTask` | 每小时整点 | 预约自动确认 + 爽约判定 |
| `AppointmentReminderTask` | 每小时整点 | 服务前 24h/2h 推送提醒 |
| `CouponExpireTask` | 每天 03:00 | 优惠券过期处理 |
| `CacheSyncTask` | 15 分钟 ~ 每天 | 5 个缓存同步子任务 |
| `PostExposureTask` | 每小时 + 每天 02:00 | 帖子曝光分落库 + 热门标记 |

## 订单闭环

### OrderAutoCancelTask — 超时取消兜底

- cron：`0 0 * * * ?`（每小时整点）
- 规则：「待付款」且创建时间距今 ≥30 分钟的订单逐笔取消（含库存恢复）。
- **兜底定位**：主力已改为 Redis 时间驱动（延迟 key），本任务防延迟队列漏单。
- 支付成功会清除超时取消 key（`mock/confirm` 与真实回调同款处理）。

### OrderAutoCompleteTask — 自动完成

- cron：`0 0 2 * * ?`（每天凌晨 2 点）
- 规则：「已发货」且发货时间距今 ≥7 天的订单批量置「已完成」；批量 UPDATE，单批 ≤500 条。

## 预约闭环

### AppointmentAutoExpireTask — 自动处理

- cron：`0 0 * * * ?`（每小时整点）
- 覆盖三类服务（美容/医院/上门清理），两条规则：
  1. **自动确认**：临近服务时间（提前 2 小时）仍未处理的预约 → 「已确认」；
  2. **爽约判定**：服务时间结束（延后 2 小时）未核销 → `no_show`，并按规则计算违约金。
- 状态兼容英文（`pending`/`confirmed`/`assigned`）与中文（已确认/已分配）两种存量值。

### AppointmentReminderTask — 预约提醒

- cron：`0 0 * * * ?`（每小时整点）
- 规则：服务前 24 小时、2 小时各推送一次提醒通知；`appointment_reminder_sent` 表防重复发送；候选集最多提前 28 小时。

## 营销与缓存

### CouponExpireTask

- cron：`0 0 3 * * ?`（每天凌晨 3 点，错开 2 点的订单任务高峰）。
- 处理过期用户优惠券状态。

### CacheSyncTask — 5 个子任务

| 子任务 | cron |
|---|---|
| 用户统计数据 → Redis | `0 0 * * * ?`（每小时整点） |
| 关注/粉丝关系 → Redis | `0 */30 * * * ?`（每 30 分钟） |
| 热门帖子列表更新 | `0 */15 * * * ?`（每 15 分钟） |
| 帖子统计数据同步 | `0 30 * * * ?`（每小时 30 分） |
| 热搜关键词热度衰减 | `0 0 2 * * ?`（每天凌晨 2 点，防过时热词长期占榜） |

## 社区热度

### PostExposureTask — 曝光分与热门标记

- **每小时**（`0 0 * * * ?`）：帖子曝光分增量落库，单条 UPDATE 直接在 SQL 内计算（替代全量加载 + 逐条更新的 N+1 写）：
  - `exposure_score += base + interaction`
  - `base = 0.5 * max(0, 1 - 发帖小时数 / 168)`（一周时间衰减）
  - `interaction = 点赞*0.1 + 评论*0.15 + 浏览*0.05`
- **每天 02:00**（`0 0 2 * * ?`）：更新 `is_hot` 标记，热度 = `点赞*0.5 + 评论*0.3 + 浏览*0.2 + 曝光分`，阈值 50.0，两条批量 UPDATE 分别置/撤热门。

## 运维注意

- 任务依赖 Redis / MySQL，任一不可用时本周期跳过并记 ERROR，下个周期自动重试。
- 所有任务均带 try-catch + 日志，单个任务异常不影响其他任务线程。
- 调整频率时修改类内 `@Scheduled` 注解即可，无需改配置文件。
