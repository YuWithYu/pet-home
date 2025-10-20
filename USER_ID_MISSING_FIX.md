# 用户ID缺失问题修复说明

## 🐛 问题描述

预约提交时出现数据库错误：
```
Field 'user_id' doesn't have a default value
```

SQL插入语句中缺少 `user_id` 字段：
```sql
INSERT INTO appointment ( pet_id, service_type, DATE, time_slot, STATUS, REMARK, contact_name, contact_phone, PRICE, LOCATION, create_time, update_time ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
```

## 🔍 问题分析

### 根本原因：
1. **用户信息缺失**：前端 `this.userInfo.id` 为 `null` 或 `undefined`
2. **Vuex状态问题**：Vuex中的用户信息可能没有正确加载
3. **数据库约束**：`user_id` 字段没有默认值，必须提供

### 技术细节：
- **前端发送**：`userId: this.userInfo.id` 为 `null`
- **后端接收**：`Appointment` 对象的 `userId` 字段为 `null`
- **数据库插入**：MyBatis-Plus 忽略 `null` 字段，导致 `user_id` 字段缺失

## 🛠️ 修复方案

### 1. 用户信息验证

#### 添加用户信息检查：
```javascript
// 检查用户信息
if (!this.userInfo || !this.userInfo.id) {
  uni.hideLoading()
  uni.showToast({
    title: '用户信息异常，请重新登录',
    icon: 'none'
  })
  return
}
```

### 2. 备用用户信息加载

#### 从本地存储加载用户信息：
```javascript
onLoad() {
  // 检查登录状态
  if (!this.isLoggedIn) {
    // 跳转到登录页面
    return
  }

  // 从本地存储加载用户信息（备用方案）
  const localUserInfo = uni.getStorageSync('userInfo')
  if (localUserInfo && !this.userInfo) {
    this.userInfo = localUserInfo
  }

  this.initData()
  this.loadPets()
}
```

### 3. 调试信息增强

#### 添加详细的调试日志：
```javascript
console.log('用户信息:', this.userInfo)
console.log('提交预约数据:', appointmentData)
```

## 📋 修复详情

### 修改文件：
- `pet-home-uniapp/pages/appointment/book-door-cleaning.vue`

### 修改内容：

#### 1. 用户信息验证
```javascript
// 在提交预约前检查用户信息
if (!this.userInfo || !this.userInfo.id) {
  uni.hideLoading()
  uni.showToast({
    title: '用户信息异常，请重新登录',
    icon: 'none'
  })
  return
}
```

#### 2. 备用用户信息加载
```javascript
// 从本地存储加载用户信息（备用方案）
const localUserInfo = uni.getStorageSync('userInfo')
if (localUserInfo && !this.userInfo) {
  this.userInfo = localUserInfo
}
```

#### 3. 调试信息
```javascript
console.log('用户信息:', this.userInfo)
console.log('提交预约数据:', appointmentData)
```

## ✅ 修复优势

### 1. 数据完整性
- **必填字段**：确保 `user_id` 字段始终有值
- **数据关联**：预约记录正确关联到用户
- **数据库约束**：满足数据库字段约束要求

### 2. 用户体验
- **错误提示**：用户信息异常时给出明确提示
- **自动恢复**：从本地存储自动加载用户信息
- **登录引导**：引导用户重新登录

### 3. 系统稳定性
- **双重保障**：Vuex + 本地存储双重用户信息源
- **错误处理**：完善的错误处理和用户提示
- **调试支持**：详细的调试日志便于问题排查

## 🔄 工作流程

### 1. 页面加载
- 检查登录状态
- 从Vuex获取用户信息
- 备用：从本地存储加载用户信息

### 2. 预约提交
- 验证用户信息完整性
- 检查 `userId` 是否存在
- 构建完整的预约数据

### 3. 数据提交
- 发送包含 `userId` 的预约数据
- 后端正确插入 `user_id` 字段
- 预约创建成功

## 📝 注意事项

1. **用户信息同步**：确保Vuex和本地存储的用户信息同步
2. **登录状态检查**：页面加载时检查登录状态
3. **错误处理**：用户信息异常时给出明确提示
4. **调试日志**：保留调试日志便于问题排查

## 🚀 部署说明

### 修改文件：
- `pet-home-uniapp/pages/appointment/book-door-cleaning.vue`

### 无需额外配置：
- 前端修改即可解决用户ID缺失问题
- 后端无需任何修改
- 保持现有功能完整性

## 🎯 预期效果

修复后：
- ✅ 用户信息正确加载和验证
- ✅ 预约数据包含完整的 `userId`
- ✅ 数据库插入成功，不再出现字段缺失错误
- ✅ 预约记录正确关联到用户

现在预约功能应该可以正常工作了！🎉
