# 服务配置管理系统使用说明

## 功能概述

服务配置管理系统允许管理员动态添加、编辑和管理各种上门服务（如上门铲屎、宠物洗护、宠物寄养等），无需修改代码即可增加新服务。

## 数据库设置

### 1. 创建数据库表

在MySQL数据库中执行以下SQL脚本：

```bash
mysql -u root -p pet_home < database/service_config.sql
```

或者手动执行 `database/service_config.sql` 文件中的SQL语句。

### 2. 表结构说明

`service_config` 表包含以下主要字段：

- `service_type`: 服务类型标识（如：door-cleaning）
- `service_name`: 服务名称（如：上门铲屎服务）
- `description`: 服务描述
- `price`: 服务价格
- `icon`: 服务图标路径
- `time_slots`: 可预约时间段（JSON格式）
- `features`: 服务特点（JSON格式）
- `notice`: 温馨提示（JSON格式）
- `status`: 状态（1-启用，0-禁用）
- `sort_order`: 排序顺序

## 管理后台使用

### 访问服务配置管理页面

1. 登录管理后台
2. 在左侧菜单中找到"服务配置管理"
3. 进入服务配置列表页面

### 添加新服务

1. 点击页面右上角的"添加服务"按钮
2. 填写服务信息：
   - **服务类型**: 英文标识，如 `door-cleaning`（必填，添加后不可修改）
   - **服务名称**: 中文名称，如 `上门铲屎服务`（必填）
   - **服务描述**: 服务的简短描述
   - **服务价格**: 服务费用（必填）
   - **图标路径**: 服务图标，如 `/static/images/door-cleaning.svg`
   - **时间段**: JSON格式，如 `["08:00-10:00", "10:00-12:00"]`
   - **服务特点**: JSON格式，如 `["特点1|描述1", "特点2|描述2"]`
   - **温馨提示**: JSON格式，如 `["提示1", "提示2"]`
   - **排序**: 数字越小越靠前
   - **状态**: 启用/禁用

3. 点击"保存"按钮

### 编辑服务

1. 在服务列表中找到要编辑的服务
2. 点击"编辑"按钮
3. 修改服务信息
4. 点击"保存"按钮

### 启用/禁用服务

- 点击服务列表中的"启用/禁用"按钮即可切换服务状态
- 禁用的服务不会在小程序中显示

### 删除服务

1. 点击服务列表中的"删除"按钮
2. 确认删除操作

## 小程序前端展示

### 自动显示

- 小程序首页会自动从后端获取所有启用的服务
- 服务按照 `sort_order` 字段排序显示
- 如果后端服务不可用，会自动使用默认服务列表

### 服务跳转

点击服务后的跳转规则：

- `door-cleaning`: 跳转到上门铲屎服务详情页
- `grooming`: 跳转到宠物洗护预约页
- `hospital`: 跳转到宠物医院预约页
- `boarding`: 跳转到宠物寄养预约页
- 其他服务: 显示"服务开发中"提示

## JSON格式说明

### 时间段格式 (time_slots)

```json
["08:00-10:00", "10:00-12:00", "12:00-14:00", "14:00-16:00", "16:00-18:00", "18:00-20:00"]
```

### 服务特点格式 (features)

格式：`["标题|描述", "标题|描述"]`

```json
["上门服务|专业人员上门为您的宠物清理猫砂/狗便", "灵活时间|可预约多个时间段，满足不同需求"]
```

### 温馨提示格式 (notice)

```json
["请提前预约，至少提前1天", "服务时间为早8:00-晚20:00", "首次服务需要留下门禁密码或钥匙"]
```

## API接口

### 获取所有服务配置

```
GET /api/service-config/all
```

返回所有服务配置列表

### 根据服务类型获取配置

```
GET /api/service-config/type/{serviceType}
```

返回指定类型的服务配置

### 创建服务配置

```
POST /api/service-config/create
Content-Type: application/json

{
  "serviceType": "door-cleaning",
  "serviceName": "上门铲屎服务",
  "description": "专业贴心服务",
  "price": 99.00,
  "status": 1
}
```

### 更新服务配置

```
PUT /api/service-config/update
Content-Type: application/json

{
  "id": 1,
  "serviceName": "上门铲屎服务（更新）",
  "price": 120.00
}
```

### 删除服务配置

```
DELETE /api/service-config/{id}
```

### 更新服务状态

```
PUT /api/service-config/{id}/status?status=1
```

## 常见问题

### Q1: 添加的服务在小程序中不显示？

A: 检查以下几点：
- 服务状态是否为"启用"（status=1）
- 小程序是否已重新编译
- 后端服务是否正常运行
- 浏览器/开发者工具中检查网络请求是否成功

### Q2: 如何更改服务图标？

A: 
1. 将SVG图标文件放到 `pet-home-uniapp/static/images/` 目录
2. 在服务配置中填写图标路径，如 `/static/images/xxx.svg`

### Q3: JSON格式错误怎么办？

A: 
- 确保使用双引号 `"` 而不是单引号 `'`
- 数组格式：`["item1", "item2"]`
- 可以使用在线JSON验证工具检查格式

### Q4: 如何添加新的服务页面？

A:
1. 在 `pet-home-uniapp/pages/appointment/` 目录创建新页面
2. 在 `pages.json` 中注册新页面路由
3. 在 `pages/index/index.vue` 的 `onServiceTap` 方法中添加跳转逻辑
4. 在数据库中添加服务配置

## 技术支持

如有问题，请联系开发团队。

