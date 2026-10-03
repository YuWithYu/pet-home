# 数据库指南

MySQL 8.x，库名 `pet_home`，字符集 utf8mb4。建库脚本位于 `database/pet_home.sql`（phpMyAdmin 导出，含 64 张表与演示数据）。

## 初始化

转储不含 `CREATE DATABASE`，先建库再导入：

```powershell
mysql -u root -p -e "CREATE DATABASE pet_home DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
cmd /c "mysql -u root -p pet_home < database\pet_home.sql"
```

或在 mysql 客户端内 `SOURCE database/pet_home.sql;`。

## 启动期自动建表/迁移

后端启动时部分表结构由初始化器（`pet-backend/.../config/`）自动处理，无需人工干预：

| 表 | 机制 |
|---|---|
| `coupon` / `user_coupon` | `CouponSchemaInitializer` 自动建表 |
| `grooming_services` / `hospital_services` | `IF NOT EXISTS` 兜底建表（转储已含） |
| `time_slots` | `TimeSlotSchemaInitializer` 自动补 `store_id` 列、索引与门店隔离唯一键 |

## 初始数据

| 数据 | 说明 |
|---|---|
| 管理员 `admin` | 密码 `123456`（BCrypt 存储），super_admin，管理后台登录用 |
| 门店/员工/时段 | 三类服务预约的演示数据 |
| 商品/帖子/用户 | 商城与社区的演示数据（数千条） |

## 表字典（64 张）

说明综合转储中的表 COMMENT 与实体语义整理。

### 用户与权限

| 表 | 说明 |
|---|---|
| `user` | 用户主表（账号、手机、openid、角色、积分等） |
| `user_follow` | 用户关注关系 |
| `user_hidden_conversation` | 用户侧隐藏的会话 |
| `user_notice_read` | 用户公告已读记录 |
| `user_post_interaction` | 用户-帖子互动行为表 |
| `admin` | 管理员表 |
| `admin_permissions` | 管理员权限表 |

### 宠物档案

| 表 | 说明 |
|---|---|
| `pet` | 宠物表 |
| `pet_medical_record` | 宠物就医记录（疫苗/驱虫/体检/诊疗，含下次到期日） |
| `pet_weight_record` | 宠物体重记录（体重曲线数据源） |

### 电商

| 表 | 说明 |
|---|---|
| `categories` | 商品分类 |
| `product` | 商品表 |
| `product_favorite` | 商品收藏 |
| `product_review` | 商品评价 |
| `review_like` | 评论点赞 |
| `review_reply` | 评论回复 |
| `shopping_cart` | 购物车 |
| `pet_order` | 商品订单主表 |
| `order_item` | 订单商品明细 |
| `order_logistics_track` | 订单物流跟踪记录 |
| `refund_request` | 退款申请 |
| `stores` | 商品商家表 |
| `store_warehouse` | 店铺发货仓地址 |

### 支付

| 表 | 说明 |
|---|---|
| `payment` | 统一支付单（服务预约支付，与预约状态机解耦） |

### 服务预约（美容 / 医院 / 上门清理）

| 表 | 说明 |
|---|---|
| `service_stores` | 服务门店表 |
| `service_member` | 服务人员表 |
| `service_schedule` | 服务人员可预约时间表 |
| `time_slots` | 可预约时段（按门店隔离，启动期自动补列） |
| `department` | 部门管理表 |
| `grooming_services` | 美容服务项目 |
| `grooming_service_banners` | 美容服务展示图 |
| `grooming_appointments` | 美容预约单 |
| `hospital_services` | 宠物医院服务 |
| `medical_service_banners` | 宠物医院展示图 |
| `hospital_appointments` | 医院预约单 |
| `litter_services` | 上门铲屎服务 |
| `litter_service_banners` | 上门铲屎展示图 |
| `door_cleaning_appointment` | 上门铲屎预约单 |
| `service_appointment_rating` | 服务预约评价 |
| `appointment_cancellation_request` | 取消预约申请 |
| `appointment_change_request` | 预约变更申请 |
| `appointment_reminder_sent` | 预约提醒发送记录（防重复发送） |
| `staff_schedule` | 员工排班（班次/可接单量，预约分单依据） |
| `member_calendar_day_mark` | 员工日历标记 |
| `medical_services` | 医疗服务项目 |
| `appointment` | 统一预约单（跨服务类型） |
| `bookings` | 服务预约单（早期预约体系） |

### 社区

| 表 | 说明 |
|---|---|
| `post` | 帖子表 |
| `comment` | 帖子评论 |
| `comment_like` | 评论点赞 |
| `post_like` | 帖子点赞 |
| `post_collect` | 帖子收藏 |
| `post_tag` | 帖子-标签关联 |
| `tag` | 标签表 |
| `daily_topic` | 每日专题帖子 |
| `daily_topic_theme` | 宠物专题分类 |
| `daily_topic_post` | 宠物专题关联 |
| `topic_like` / `topic_collect` | 专题点赞 / 收藏 |
| `complaints` | 投诉举报 |
| `message` | 私信表 |

### 积分与营销

| 表 | 说明 |
|---|---|
| `points_record` | 积分明细 |
| `sign_in_records` | 签到记录 |
| `task_records` | 任务记录 |
| `coupon` / `user_coupon` | 优惠券 / 用户持券（启动期自动建表） |
| `recommendation_interaction_weight` | 推荐算法互动行为权重配置 |

### 内容与通知

| 表 | 说明 |
|---|---|
| `banner` | 轮播图 |
| `notice` | 公告表 |
| `notification` | 系统通知表 |

### 基础数据

| 表 | 说明 |
|---|---|
| `china_region` | 中国行政区划表 |

## 已知差异（2026-08-31 已根治）

早期转储（导出于 2026-05-03）缺失 7 张被实体/Mapper 引用的表——`pet_medical_record`、`pet_weight_record`、`medical_services`、`staff_schedule`、`member_calendar_day_mark`、`appointment`、`bookings`——宠物健康档案、员工排班、统一预约相关功能在导入旧转储的环境会直接 500。

已于 2026-08-31 根治：

1. 按实体 `@TableField` 定义推导 DDL，将 7 张表建入本地库；
2. 重新全量导出 `database/pet_home.sql`：**74 张表**、mysqldump 格式（每表带 `DROP TABLE IF EXISTS`，可重复导入）、含 `coupon`/`user_coupon` 与近期业务数据。

导入新转储即获得完整结构，无需手工补表。已实测验证：宠物健康档案的「新增 → 列表 → 到期提醒」读写链路全部返回 200（到期天数计算正确）。

`coupon`/`user_coupon`/`time_slots` 的启动期初始化器继续保留（幂等双保险，见上节）。

### 重新导出命令（表结构变更后复用）

```powershell
mysqldump -u root -p --default-character-set=utf8mb4 --add-drop-table --routines --result-file="database\pet_home.sql" pet_home
```

> `--result-file` 让 mysqldump 直接写文件，规避 PowerShell `>` 重定向产生的 UTF-16 编码问题；不带 `--databases`，保持「手动建库」约定。

## 开源发布流程（重新生成脱敏转储）

本仓库公开发布，`database/pet_home.sql` 必须是**脱敏副本**——本地开发库 `pet_home` 保留真实数据不动，发布前按以下流水线生成：

```powershell
# 1. 建发布副本库并导入当前转储（含真实数据的本地导出）
mysql -u root -p123456 -e "DROP DATABASE IF EXISTS pet_home_public; CREATE DATABASE pet_home_public DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p123456 --default-character-set=utf8mb4 pet_home_public -e "source database/pet_home.sql"

# 2. 对副本执行脱敏脚本（手机号/邮箱/openid/私信/密码统一为演示值）
mysql -u root -p123456 --default-character-set=utf8mb4 pet_home_public -e "source database/sanitize-for-release.sql"

# 3. 从副本导出，覆盖发布转储，并删除副本库
mysqldump -u root -p123456 --default-character-set=utf8mb4 --add-drop-table --routines --result-file="database\pet_home.sql" pet_home_public
mysql -u root -p123456 -e "DROP DATABASE pet_home_public;"
```

⚠️ 脱敏脚本**只能对 `pet_home_public` 副本执行**，直接跑在开发库上会销毁真实数据。脱敏后验证（应全为 0）：在转储文件中检索 `152785`、`or25g3`、`wx_or`、`'13 9x/19x/152x 号段'` 等真实数据特征。
