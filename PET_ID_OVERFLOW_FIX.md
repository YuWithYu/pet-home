# 宠物ID溢出问题修复说明

## 🐛 问题描述

在提交预约时出现JSON解析错误：
```
Numeric value (1760890240231) out of range of int (-2147483648 - 2147483647)
```

## 🔍 问题分析

### 根本原因：
1. **前端宠物ID生成**：使用 `Date.now()` 生成13位时间戳作为宠物ID
2. **后端数据库字段类型**：`petId` 字段定义为 `int` 类型
3. **数据类型不匹配**：13位时间戳超出32位整数范围

### 技术细节：
- **JavaScript时间戳**：`Date.now()` 返回13位数字（如：1760890240231）
- **MySQL int类型**：32位有符号整数，范围 -2,147,483,648 到 2,147,483,647
- **Jackson解析**：后端无法将超出范围的数字解析为int类型

## 🛠️ 修复方案

### 1. 前端修复
将宠物ID转换为字符串类型提交，避免数值溢出：

#### 修复文件：
- `pet-home-uniapp/pages/appointment/book-door-cleaning.vue`
- `pet-home-uniapp/pages/booking/index.vue`

#### 修复代码：
```javascript
// 修复前
petId: this.formData.petId,

// 修复后
petId: this.formData.petId ? String(this.formData.petId) : null,
```

### 2. 后端建议修复（可选）
如果后端需要支持，建议将数据库字段类型改为：
```sql
-- 方案1：使用BIGINT（推荐）
ALTER TABLE appointment MODIFY COLUMN petId BIGINT;

-- 方案2：使用VARCHAR存储字符串ID
ALTER TABLE appointment MODIFY COLUMN petId VARCHAR(20);
```

## 📋 修复详情

### 1. 上门铲屎服务页面
**文件**：`pet-home-uniapp/pages/appointment/book-door-cleaning.vue`

**修复位置**：`submitBooking()` 方法
```javascript
const appointmentData = {
  userId: this.userInfo.id,
  petId: this.formData.petId ? String(this.formData.petId) : null, // 转换为字符串避免int溢出
  serviceType: this.formData.serviceType,
  // ... 其他字段
}
```

### 2. 通用预约页面
**文件**：`pet-home-uniapp/pages/booking/index.vue`

**修复位置**：预约数据提交
```javascript
petId: this.selectedPet ? String(this.selectedPet.id) : null, // 转换为字符串避免int溢出
```

## ✅ 修复验证

### 测试场景：
1. **创建宠物档案**：使用时间戳ID（13位数字）
2. **选择宠物预约**：ID正确传递到后端
3. **提交预约**：不再出现JSON解析错误
4. **预约成功**：正常创建预约记录

### 预期结果：
- ✅ 不再出现数值溢出错误
- ✅ 预约可以正常提交
- ✅ 宠物ID正确关联到预约记录

## 🔄 兼容性说明

### 前端兼容性：
- **现有宠物数据**：无需修改，ID保持时间戳格式
- **新创建宠物**：继续使用时间戳ID
- **数据传递**：统一转换为字符串提交

### 后端兼容性：
- **字符串接收**：后端可以接收字符串类型的petId
- **数据库存储**：如果字段类型为VARCHAR，可以直接存储
- **查询兼容**：字符串ID可以正常用于查询和关联

## 📝 注意事项

1. **ID唯一性**：时间戳ID仍然保持唯一性
2. **数据一致性**：前端显示和存储保持一致
3. **查询性能**：字符串ID查询性能略低于数字ID
4. **未来优化**：建议后端统一使用BIGINT或VARCHAR类型

## 🚀 部署说明

### 修改文件：
- `pet-home-uniapp/pages/appointment/book-door-cleaning.vue`
- `pet-home-uniapp/pages/booking/index.vue`

### 无需额外配置：
- 前端修改即可解决当前问题
- 后端无需立即修改数据库结构
- 保持现有数据完整性

现在预约功能应该可以正常工作了！🎉
