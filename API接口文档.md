# 脐橙病虫害监测系统 API 接口文档

> 版本：v2.1  
>
> 基础技术栈：Spring Boot 3.2 + PostgreSQL 16.14 + pgvector 0.8.2 + JWT  
>
> 更新日期：2026-06-08

---

## 一、认证方式

### 1.1 JWT Bearer Token

用户登录成功后获取 `accessToken` 和 `refreshToken`。  

在需要认证的接口请求头中携带：

```
Authorization: Bearer <accessToken>
```

### 1.2 设备 API Key

设备上报接口使用 API Key 认证，请求头：

```
X-Api-Key: <deviceApiKey>
```

### 1.3 接口权限标记

| 标记 | 说明 |
| --- | --- |
| 🔓 公开接口 | 无需认证 |
| 🔒 需登录 | 需要 JWT Token |
| 👑 管理员 | 需要管理员角色 |
| 📱 小程序 | 小程序专用接口 |
| 🔧 设备接口 | 需要 API Key |

---

## 二、统一响应格式

### 2.1 标准响应

```json
{
  "code": 200,
  "message": "success",
  "data": { },
  "timestamp": 1716268800000
}
```

### 2.2 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  },
  "timestamp": 1716268800000
}
```

---

## 三、全局错误码

| 错误码 | HTTP状态码 | 说明 |
| --- | --- | --- |
| 200 | 200 | 成功 |
| 400 | 400 | 请求参数错误 |
| 401 | 401 | 未认证/Token过期 |
| 403 | 403 | 无权限访问 |
| 404 | 404 | 资源不存在 |
| 409 | 409 | 资源冲突（如手机号已注册） |
| 422 | 422 | 业务逻辑错误 |
| 429 | 429 | 请求频率限制 |
| 500 | 500 | 服务器内部错误 |
| 1001 | 400 | 验证码错误或已过期 |
| 1002 | 400 | 验证码发送过于频繁 |
| 1003 | 401 | 密码错误 |
| 1004 | 401 | 账号已被禁用 |
| 1005 | 409 | 手机号已注册 |
| 2001 | 404 | 果园不存在 |
| 2002 | 403 | 无权操作该果园 |
| 3001 | 404 | 设备不存在 |
| 3002 | 409 | 设备编码已存在 |
| 3003 | 401 | API Key无效 |
| 4001 | 404 | 消息不存在 |
| 5001 | 400 | 上传文件格式不支持 |
| 5002 | 400 | 上传文件过大 |
| 5003 | 422 | 文献处理失败 |
| 6001 | 404 | 反馈不存在 |
| 6002 | 422 | 问卷已提交 |

---

## 四、接口列表

---

## M1 用户认证模块

### 1.1 手机号注册

🔓 公开接口

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/auth/register |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phone | string | 是 | 手机号，11位数字 |
| username | string | 是 | 用户名，2-20位中文/英文/数字，用于前端展示 |
| password | string | 是 | 密码，6-20位 |
| smsCode | string | 是 | 短信验证码，6位 |

**请求示例**

```json
{
  "phone": "13800138000",
  "username": "果农张三",
  "password": "password123",
  "smsCode": "123456"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 10001,
    "phone": "13800138000",
    "username": "果农张三",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshTokenId": "rt_abc123def456",//注册后直接进入系统，无需再次输入密码
    "expiresIn": 7200
  },
  "timestamp": 1716268800000
}
```

**说明**

- `username` 为用户显示名称，注册后可在个人中心修改
- `refreshTokenId` 为刷新令牌的唯一标识，用于后续刷新Token和退出登录
- 系统不返回原始 `refreshToken`，仅返回其标识ID，提升安全性

---

### 1.2 密码登录

🔓 公开接口

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/auth/login/password |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phone | string | 是 | 手机号 |
| password | string | 是 | 密码 |

**响应示例**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 10001,
    "phone": "13800138000",
    "username": "果农张三",
    "avatarUrl": "https://cdn.example.com/avatar/10001.jpg",
    "roles": [
      {
        "id": 1,
        "code": "ROLE_FARMER",
        "name": "农户"
      }
    ],
    "lastLoginIp": "192.168.1.100",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshTokenId": "rt_abc123def456",
    "expiresIn": 7200
  },
  "timestamp": 1716268800000
}
```

**说明**

- `refreshTokenId` 为刷新令牌的唯一标识，用于后续刷新Token和退出登录
- 系统不返回原始 `refreshToken`，仅返回其标识ID，提升安全性
- 原始 `refreshToken` 的 SHA256 哈希值存储在数据库 `sys_refresh_token` 表中

---

### 1.3 短信验证码登录

🔓 公开接口

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/auth/login/sms |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phone | string | 是 | 手机号 |
| smsCode | string | 是 | 短信验证码 |

**响应示例**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 10001,
    "phone": "13800138000",
    "username": "果农张三",
    "avatarUrl": "https://cdn.example.com/avatar/10001.jpg",
    "roles": [
      {
        "id": 1,
        "code": "ROLE_FARMER",
        "name": "农户"
      }
    ],
    "lastLoginIp": "192.168.1.100",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshTokenId": "rt_abc123def456",
    "expiresIn": 7200
  },
  "timestamp": 1716268800000
}
```

---

### 1.4 微信小程序登录

🔓 公开接口

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/auth/login/wechat |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| code | string | 是 | 微信小程序登录code |

**响应示例**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 10001,
    "phone": "13800138000",
    "username": "果农张三",
    "avatarUrl": "https://cdn.example.com/avatar/10001.jpg",
    "roles": [
      {
        "id": 1,
        "code": "ROLE_FARMER",
        "name": "农户"
      }
    ],
    "isNewUser": false,
    "lastLoginIp": "192.168.1.100",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshTokenId": "rt_abc123def456",
    "expiresIn": 7200
  },
  "timestamp": 1716268800000
}
```

---

### 1.5 刷新Token

🔓 公开接口

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/auth/refresh |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| refreshTokenId | string | 是 | 刷新令牌标识ID（登录时返回） |

**请求示例**

```json
{
  "refreshTokenId": "rt_abc123def456"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshTokenId": "rt_abc123def456",
    "expiresIn": 7200
  },
  "timestamp": 1716268800000
}
```

**说明**

- 使用登录时返回的 `refreshTokenId` 来刷新访问令牌
- 系统根据 `refreshTokenId` 在数据库中查找对应的 `token_hash` 进行验证
- 若该token已被吊销（`revoked=true`），则返回401错误

---

### 1.6 退出登录

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/auth/logout |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| refreshTokenId | string | 是 | Body | 刷新令牌标识ID（登录时返回） |

**请求示例**

```json
{
  "refreshTokenId": "rt_abc123def456"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "退出成功",
  "data": null,
  "timestamp": 1716268800000
}
```

**说明**

退出登录时系统会执行以下操作：

1. 从 Redis 中删除对应的 accessToken 和 refreshToken
2. 在 `sys_refresh_token` 表中将该 token 标记为 `revoked=true`
3. 更新用户表中的 `lastLoginAt` 字段

---

### 1.7 发送验证码

🔓 公开接口

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/auth/sms/send |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phone | string | 是 | 手机号 |
| scene | string | 是 | 使用场景：register=注册, login=登录, reset_password=重置密码 |

**响应示例**

```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": {
    "expireSeconds": 300
  },
  "timestamp": 1716268800000
}
```

---

## M2 用户管理模块

### 2.1 用户列表

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/users |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| keyword | string | 否 | 搜索关键词（手机号/昵称） |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 10001,
        "phone": "13800138000",
        "username": "果农张三",
        "avatarUrl": "https://cdn.example.com/avatar/10001.jpg",
        "roles": [
          {
            "id": 1,
            "code": "ROLE_FARMER",
            "name": "农户"
          }
        ],
        "status": 1,
        "createdAt": "2026-01-15 10:30:00",
        "lastLoginAt": "2026-06-07 08:00:00",
        "lastLoginIp": "192.168.1.100"
      }
    ],
    "total": 150,
    "page": 1,
    "size": 20,
    "pages": 8
  },
  "timestamp": 1716268800000
}
```

---

### 2.2 用户详情

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/users/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 用户ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10001,
    "phone": "13800138000",
    "username": "果农张三",
    "avatarUrl": "https://cdn.example.com/avatar/10001.jpg",
    "wechatOpenId": "oXxxx_xxxxx",
    "roles": [
      {
        "id": 1,
        "code": "ROLE_FARMER",
        "name": "农户"
      }
    ],
    "status": 1,
    "smsEnabled": true,
    "pushEnabled": true,
    "createdAt": "2026-01-15 10:30:00",
    "updatedAt": "2026-06-01 14:20:00",
    "lastLoginAt": "2026-06-07 08:00:00",
    "lastLoginIp": "192.168.1.100",
    "orchardCount": 3
  },
  "timestamp": 1716268800000
}
```

---

### 2.3 更新用户状态

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/admin/users/{id}/status |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 用户ID |
| status | int | 是 | Body | 状态：0=禁用, 1=启用 |

**响应示例**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 2.4 分配角色

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/admin/users/{id}/roles |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 用户ID |
| roleId | int | 是 | Body | 角色ID（单个） |

**请求示例**

```json
{
  "roleId": 1//表示农户，值为2表示管理员
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "分配成功",
  "data": null,
  "timestamp": 1716268800000
}
```

**说明**

- 系统**不支持多角色**，一个用户只能拥有一个角色
- 农户（ROLE_FARMER）和管理员（ROLE_ADMIN）角色互斥
- 若用户已存在角色，新角色将覆盖原有角色

---

### 2.5 操作日志

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/logs |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| action | string | 否 | 操作类型：login/create/update/delete/export |
| userId | long | 否 | 操作用户ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 50001,
        "userId": 10001,
        "username": "果农张三",
        "module": "auth",
        "action": "login",
        "description": "密码登录",
        "ipAddress": "192.168.1.100",
        "userAgent": "Mozilla/5.0...",
        "createdAt": "2026-06-07 08:00:00"
      }
    ],
    "total": 2000,
    "page": 1,
    "size": 20,
    "pages": 100
  },
  "timestamp": 1716268800000
}
```

---

### 2.6 查看个人信息

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/user/profile |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 10001,
    "phone": "138****8000",
    "username": "果农张三",
    "avatarUrl": "https://cdn.example.com/avatar/10001.jpg",
    "roles": [
      {
        "id": 1,
        "code": "ROLE_FARMER",
        "name": "农户"
      }
    ],
    "smsEnabled": true,
    "pushEnabled": true,
    "createdAt": "2026-01-15 10:30:00",
    "lastLoginIp": "192.168.1.100"
  },
  "timestamp": 1716268800000
}
```

---

### 2.7 修改密码

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/user/password |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码，6-20位，需包含字母和数字 |
| confirmPassword | string | 是 | 确认新密码，需与newPassword一致 |

**请求示例**

```json
{
  "oldPassword": "oldPass123",
  "newPassword": "newPass456",
  "confirmPassword": "newPass456"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": 1716268800000
}
```

**说明**

- 新密码不能与原密码相同
- 密码修改成功后，当前accessToken立即失效，需重新登录
- 若忘记原密码，请使用"忘记密码"功能通过短信验证码重置

---

### 2.8 通知设置

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/user/notification-settings |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| smsEnabled | boolean | 否 | 是否开启短信通知 |
| pushEnabled | boolean | 否 | 是否开启推送通知 |

**响应示例**

```json
{
  "code": 200,
  "message": "设置成功",
  "data": {
    "smsEnabled": true,
    "pushEnabled": false
  },
  "timestamp": 1716268800000
}
```

---

## M3 果园管理模块

### 3.1 我的果园列表

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/orchards |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2001,
      "name": "张家果园",
      "formattedAddress": "江西省赣州市信丰县嘉定镇马路坑村",
      "location": "114.93,25.38",
      "areaMu": 50.5,
      "treeAge": 8,
      "variety": "纽荷尔脐橙",
      "roleType": "owner",
      "deviceCount": 5,
      "createdAt": "2026-01-10 09:00:00"
    }
  ],
  "timestamp": 1716268800000
}
```

---

### 3.2 果园详情

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/orchards/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 果园ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "name": "张家果园",
    "formattedAddress": "江西省赣州市信丰县嘉定镇马路坑村",
    "location": "114.93,25.38",
    "areaMu": 50.5,
    "treeAge": 8,
    "variety": "纽荷尔脐橙",
    "roleType": "owner",
    "members": [
      {
        "userId": 10001,
        "username": "果农张三",
        "roleType": "owner",
        "joinedAt": "2026-01-10 09:00:00"
      },
      {
        "userId": 10002,
        "username": "技术员李四",
        "roleType": "manager",
        "joinedAt": "2026-02-15 14:00:00"
      }
    ],
    "devices": [
      {
        "id": 3001,
        "deviceCode": "DEV-2026-001",
        "name": "1号监测点",
        "type": "camera",
        "status": "online"
      }
    ],
    "createdAt": "2026-01-10 09:00:00",
    "updatedAt": "2026-05-20 16:30:00"
  },
  "timestamp": 1716268800000
}
```

---

### 3.3 创建果园

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/orchards |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| name | string | 是 | 果园名称，最长50字 |
| formattedAddress | string | 是 | 地址（如"江西省赣州市信丰县嘉定镇马路坑村"），系统自动调用高德地图API地理编码获取完整地址信息 |
| areaMu | number | 否 | 面积（亩） |
| treeAge | int | 否 | 树龄（年） |
| variety | string | 否 | 品种 |

**请求示例**

```json
{
  "name": "李家果园",
  "formattedAddress": "江西省赣州市信丰县嘉定镇马路坑村",
  "areaMu": 30.5,
  "treeAge": 5,
  "variety": "纽荷尔脐橙"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2002,
    "name": "李家果园",
    "formattedAddress": "江西省赣州市信丰县嘉定镇马路坑村",
    "location": "114.93,25.38"
  },
  "timestamp": 1716268800000
}
```

**说明**

- 系统接收到简单地址后，自动调用高德地图API进行地理编码
- 地理编码结果将自动填充 `formattedAddress`（完整地址）和 `location`（经纬度）字段并存储到数据库
- 前端无需关心地址解析逻辑，只需提供用户输入的简单地址即可

---

### 3.4 更新果园

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/orchards/{id} |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 果园ID |
| name | string | 否 | Body | 果园名称 |
| formattedAddress | string | 否 | Body | 地址，系统自动调用高德地图API地理编码获取完整地址信息 |
| areaMu | number | 否 | Body | 面积（亩） |
| treeAge | int | 否 | Body | 树龄（年） |
| variety | string | 否 | Body | 品种 |

**说明**

- 若传入 `formattedAddress` 参数，系统将自动调用高德地图API进行地理编码，更新 `location` 字段
- 若不传 `formattedAddress` 参数，则保持原有地址信息不变

**响应示例**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 3.5 删除果园

🔒 需登录（仅果园owner）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | DELETE |
| 请求路径 | /api/orchards/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 果园ID |

**响应示例**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 3.6 添加成员

🔒 需登录（果园owner/manager）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/orchards/{id}/members |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 果园ID |
| userId | long | 是 | Body | 被添加用户ID |
| roleType | string | 是 | Body | 角色类型：owner=所有者, manager=管理员, viewer=查看者 |

**响应示例**

```json
{
  "code": 200,
  "message": "添加成功",
  "data": null,
  "timestamp": 1716268800000
}
```

**说明**

- 成员角色在添加时**一经选择即固定**，后续无法修改
- 如需更改角色，必须先移除成员，再重新添加并指定新角色
- 果园所有者（owner）可以添加/移除其他成员，管理员（manager）可以添加/移除查看者（viewer）

---

### 3.7 移除成员

🔒 需登录（果园owner/manager）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | DELETE |
| 请求路径 | /api/orchards/{id}/members/{userId} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 果园ID |
| userId | long | 是 | 用户ID |

**响应示例**

```json
{
  "code": 200,
  "message": "移除成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 3.8 设备列表

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/orchards/{id}/devices |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 果园ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 3001,
      "deviceCode": "DEV-2026-001",
      "name": "1号监测点",
      "type": "camera",
      "model": "HC-200",
      "locationDesc": "果园入口处",
      "status": 1,
      "lastHeartbeat": "2026-06-07 07:55:00",
      "createdAt": "2026-01-15 10:00:00"
    }
  ],
  "timestamp": 1716268800000
}
```

---

### 3.9 注册设备

🔒 需登录（果园owner/manager）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/devices |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| deviceCode | string | 是 | 设备编码（IoTDA的device_id） |
| orchardId | long | 是 | 绑定果园ID |
| name | string | 否 | 设备名称，留空则自动从IoTDA获取 |
| type | string | 是 | 设备类型：camera=摄像头, sensor=传感器 |
| model | string | 否 | 设备型号 |
| locationDesc | string | 否 | 安装位置描述 |

**请求示例**

```json
{
  "deviceCode": "6a2e6c41c9429d337f50a67c_Equipment01",
  "orchardId": 2001,
  "name": "",
  "type": "sensor",
  "model": "HC-200",
  "locationDesc": "果园入口处"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 3002,
    "deviceCode": "6a2e6c41c9429d337f50a67c_Equipment01",
    "apiKey": "ak_xxxxxxxxxxxxxxxxxxxx",
    "name": "Equipment01"
  },
  "timestamp": 1716268800000
}
```

**说明**

- `deviceCode` 必须是华为云IoTDA平台上的设备ID
- `name` 留空时系统会自动从IoTDA获取设备名称
- `apiKey` 仅在注册时返回一次，请妥善保管

---

### 3.10 更新设备

🔒 需登录（果园owner/manager）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/devices/{id} |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 设备ID |
| name | string | 否 | Body | 设备名称 |
| model | string | 否 | Body | 设备型号 |
| locationDesc | string | 否 | Body | 安装位置描述 |

**响应示例**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 3.11 设备状态

🔒 需登录（果园关联用户）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/devices/{id}/status |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 设备ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deviceCode": "6a2e6c41c9429d337f50a67c_Equipment01",
    "name": "1号监测点",
    "type": "sensor",
    "model": "HC-200",
    "locationDesc": "果园入口处",
    "status": 1,
    "lastHeartbeat": "2026-06-07 07:55:00",
    "iotdaStatus": "ONLINE",
    "createdAt": "2026-01-15 10:00:00"
  },
  "timestamp": 1716268800000
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| deviceCode | string | 设备编码（IoTDA设备ID） |
| name | string | 设备名称 |
| type | string | 设备类型：camera=摄像头, sensor=传感器 |
| model | string | 设备型号 |
| locationDesc | string | 安装位置描述 |
| status | int | 本地状态：0=离线 1=在线 2=故障 |
| lastHeartbeat | string | 最后心跳时间 |
| iotdaStatus | string | IoTDA状态：ONLINE, OFFLINE, INACTIVE |
| createdAt | string | 创建时间 |

---

### 3.12 注销设备

🔒 需登录（果园owner）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | DELETE |
| 请求路径 | /api/devices/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 设备ID |

**响应示例**

```json
{
  "code": 200,
  "message": "注销成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 3.13 获取IoTDA设备列表

🔒 需登录（系统管理员或果园园主/管理员）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/devices/iotda |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "deviceId": "6a2e6c41c9429d337f50a67c_Equipment01",
      "deviceName": "Equipment01",
      "productId": "6a2e6c41c9429d337f50a67c",
      "productName": "智能传感器",
      "status": "ONLINE",
      "description": null,
      "fwVersion": null,
      "swVersion": null,
      "registered": true
    },
    {
      "deviceId": "6a2e6c41c9429d337f50a67c_Camera01",
      "deviceName": "Camera01",
      "productId": "6a2e6c41c9429d337f50a67c",
      "productName": "智能摄像头",
      "status": "OFFLINE",
      "description": null,
      "fwVersion": null,
      "swVersion": null,
      "registered": false
    }
  ],
  "timestamp": 1716268800000
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| deviceId | string | IoTDA设备ID |
| deviceName | string | 设备名称 |
| productId | string | 产品ID |
| productName | string | 产品名称 |
| status | string | 设备状态：ONLINE=在线, OFFLINE=离线, INACTIVE=未激活 |
| description | string | 设备描述 |
| fwVersion | string | 固件版本 |
| swVersion | string | 软件版本 |
| registered | boolean | 是否已注册到本地系统 |

**说明**

- 该接口从华为云IoTDA平台获取所有设备
- `registered` 字段标记设备是否已注册到本地系统
- 可用于选择未注册的设备进行注册

---

### 3.14 同步所有设备状态

🔒 需登录（果园关联用户）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/devices/sync |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
  "code": 200,
  "message": "同步成功",
  "data": null,
  "timestamp": 1716268800000
}
```

**说明**

- 从华为云IoTDA获取所有已注册设备的最新状态
- 状态映射：ONLINE → 1（在线），OFFLINE/INACTIVE → 0（离线）
- 系统每5分钟自动执行一次同步，也可手动触发

---

### 3.15 同步单个设备状态

🔒 需登录（果园关联用户，只能同步自己果园的设备）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/devices/{id}/sync |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 本地设备ID |

**响应示例**

```json
{
  "code": 200,
  "message": "同步成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

## M4 数据采集模块

### 4.1 设备上报识别结果

🔧 设备接口（API Key认证）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/device/detection |
| 请求头 | X-Api-Key: {deviceApiKey} |
| Content-Type | application/json |

**说明**

- 设备身份通过 `X-Api-Key` 请求头自动解析，无需在请求体中传递设备编码
- `diseases` 数组中每个元素创建一条独立的病害识别记录
- 病害编码从 `name` 字段截取前缀（如 `S4_Red_nose_fruit` → `S4`），然后匹配 `disease_catalog` 表
- 图片文件名自动拼接为完整 URL：`https://qcbch.obs.cn-south-1.myhuaweicloud.com/citrus/{img}`
- 时间戳为 Unix 秒级时间戳，自动转换为北京时间

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| device | string | 否 | 设备编码（可选，从API Key自动解析） |
| diseases | array | 是 | 病害识别结果数组 |
| diseases[].id | int | 是 | 病害序号 |
| diseases[].name | string | 是 | 病害名称（如 S4_Red_nose_fruit） |
| diseases[].conf | number | 是 | 置信度（0-1） |
| diseases[].cnt | int | 否 | 检测数量 |
| diseases[].img | string | 是 | 图片文件名 |
| diseases[].ts | long | 是 | 时间戳（秒） |
| gps | object | 否 | GPS 坐标 |
| gps.lat | number | 否 | 纬度 |
| gps.lng | number | 否 | 经度 |
| gps.alt | number | 否 | 海拔（米） |

**请求示例**

```json
{
  "diseases": [
    {
      "id": 4,
      "name": "S4_Red_nose_fruit",
      "conf": 0.94,
      "cnt": 1,
      "img": "citrus_1781954601.jpg",
      "ts": 1781954601
    }
  ],
  "device": "maixcam_01",
  "gps": {
    "lat": 22.543097,
    "lng": 114.057861,
    "alt": 10
  }
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "上报成功",
  "data": {
    "detectionIds": [80001],
    "count": 1
  },
  "timestamp": 1716268800000
}
```

**数据存储说明**

| 请求字段 | 存储位置 | 说明 |
| --- | --- | --- |
| diseases[].name | `disease_detection.top_class_code` | 截取前缀 S4 |
| diseases[].name | `disease_detection.disease_name_raw` | 原始名称 S4_Red_nose_fruit |
| diseases[].name | `disease_detection.disease_catalog_id` | 匹配 disease_catalog 表 |
| diseases[].conf | `disease_detection.top_probability` | 置信度 |
| diseases[].img | `disease_detection.image_url` | 拼接完整 URL |
| diseases[].ts | `disease_detection.device_timestamp` | 转换为北京时间 |
| diseases[].conf | `disease_detection.all_probabilities` | {"S4": 0.94} |
| diseases[].cnt + gps + device | `disease_detection.extra_data` | 扩展数据 |

---

### 4.2 批量上报识别结果

🔧 设备接口（API Key认证）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/device/detection/batch |
| 请求头 | X-Api-Key: {deviceApiKey} |
| Content-Type | application/json |

**请求参数（Body）**

请求体为数组，每个元素结构同 4.1 节的请求参数。

**请求示例**

```json
[
  {
    "diseases": [
      {
        "id": 4,
        "name": "S4_Red_nose_fruit",
        "conf": 0.94,
        "cnt": 1,
        "img": "citrus_1781954601.jpg",
        "ts": 1781954601
      }
    ],
    "device": "maixcam_01",
    "gps": {"lat": 22.543097, "lng": 114.057861, "alt": 10}
  },
  {
    "diseases": [
      {
        "id": 14,
        "name": "S14_Canker",
        "conf": 0.87,
        "cnt": 2,
        "img": "citrus_1781954650.jpg",
        "ts": 1781954650
      }
    ],
    "device": "maixcam_01",
    "gps": {"lat": 22.543097, "lng": 114.057861, "alt": 10}
  }
]
```

**响应示例**

```json
{
  "code": 200,
  "message": "批量上报成功",
  "data": {
    "detectionIds": [80001, 80002],
    "successCount": 2,
    "failCount": 0
  },
  "timestamp": 1716268800000
}
```

---

### 4.3 设备心跳

🔧 设备接口（API Key认证）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/device/heartbeat |
| 请求头 | X-Api-Key: {deviceApiKey} |
| Content-Type | application/json |

**说明**

- 设备身份通过 `X-Api-Key` 请求头自动解析，无需在请求体中传递 `deviceCode`
- 接收硬件设备上报的环境传感器数据（空气温湿度、土壤含水率、土壤温度、电导率、PH值）
- 空气温湿度存入环境数据表核心字段，土壤相关数据存入 `extra_data` JSONB 字段
- 同时更新设备状态为在线，记录心跳时间
- 建议设备每5分钟上报一次

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| airTemp | number | 否 | 空气温度（℃） |
| humidity | number | 否 | 空气湿度（%） |
| moisture | number | 否 | 土壤含水率（%） |
| soilTemp | number | 否 | 土壤温度（℃） |
| conductivity | number | 否 | 电导率（uS/cm） |
| ph | number | 否 | PH值 |

**请求示例**

```json
{
  "airTemp": 23.6,
  "humidity": 67.6,
  "moisture": 60,
  "soilTemp": 25.5,
  "conductivity": 0,
  "ph": 7.6
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "serverTime": 1716268800000,
    "configVersion": 1
  },
  "timestamp": 1716268800000
}
```

**数据存储说明**

| 请求字段 | 存储位置 | 说明 |
| --- | --- | --- |
| airTemp | `environment_data.temp` | 空气温度，核心字段 |
| humidity | `environment_data.humidity` | 空气湿度，核心字段 |
| moisture | `environment_data.extra_data.soil_moisture` | 土壤含水率，JSONB字段 |
| soilTemp | `environment_data.extra_data.soil_temp` | 土壤温度，JSONB字段 |
| conductivity | `environment_data.extra_data.conductivity` | 电导率，JSONB字段 |
| ph | `environment_data.extra_data.ph` | PH值，JSONB字段 |

---

### 4.4 识别记录列表

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/detections |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| orchardId | long | 否 | 果园ID |
| diseaseCode | string | 否 | 病害编码 |
| startDate | string | 否 | 开始日期，格式：yyyy-MM-dd |
| endDate | string | 否 | 结束日期，格式：yyyy-MM-dd |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 80001,
        "deviceCode": "DEV-2026-001",
        "deviceName": "1号监测点",
        "orchardId": 2001,
        "orchardName": "张家果园",
        "imageUrl": "https://storage.example.com/detections/xxx.jpg",
        "topClassCode": "S14",
        "topClassName": "溃疡病",
        "topProbability": 0.92,
        "allProbabilities": {
          "S14": 0.92,
          "S11": 0.05,
          "S1": 0.03
        },
        "deviceTimestamp": "2026-06-07 08:00:00",
        "createdAt": "2026-06-07 08:00:05"
      }
    ],
    "total": 5000,
    "page": 1,
    "size": 20,
    "pages": 250
  },
  "timestamp": 1716268800000
}
```

---

### 4.5 识别统计

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/detections/stats |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orchardId | long | 否 | 果园ID，不传则统计所有果园 |
| startDate | string | 是 | 开始日期，格式：yyyy-MM-dd |
| endDate | string | 是 | 结束日期，格式：yyyy-MM-dd |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 5000,
    "alertCount": 120,
    "diseaseDistribution": [
      {
        "diseaseCode": "S14",
        "diseaseName": "溃疡病",
        "count": 2000,
        "percentage": 40.0
      },
      {
        "diseaseCode": "S11",
        "diseaseName": "脂点黄斑病",
        "count": 1500,
        "percentage": 30.0
      },
      {
        "diseaseCode": "S1",
        "diseaseName": "健康果实",
        "count": 1500,
        "percentage": 30.0
      }
    ],
    "dailyTrend": [
      {
        "date": "2026-06-01",
        "count": 180,
        "alertCount": 5
      },
      {
        "date": "2026-06-02",
        "count": 195,
        "alertCount": 8
      }
    ]
  },
  "timestamp": 1716268800000
}
```

---

### 4.6 环境数据列表

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/environment |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| orchardId | long | 是 | 果园ID |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 60001,
        "deviceCode": "DEV-2026-001",
        "orchardId": 2001,
        "temp": 28.5,
        "humidity": 75.2,
        "precip": 0.0,
        "windSpeed": 2.3,
        "dataSource": "weather_api",
        "recordTime": "2026-06-07 08:00:00"
      }
    ],
    "total": 10000,
    "page": 1,
    "size": 20,
    "pages": 500
  },
  "timestamp": 1716268800000
}
```

---

### 4.7 环境趋势图

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/environment/trend |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orchardId | long | 是 | 果园ID |
| days | int | 否 | 查询天数，默认7，可选：7/15/30 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dates": ["2026-06-01", "2026-06-02", "2026-06-03", "2026-06-04", "2026-06-05", "2026-06-06", "2026-06-07"],
    "temp": [26.5, 27.0, 28.2, 27.8, 26.3, 25.8, 27.5],
    "humidity": [72.0, 70.5, 68.2, 71.3, 75.8, 78.2, 73.5],
    "precip": {
      "daily": [0, 0, 0, 5.2, 12.8, 3.5, 0],
      "total": 21.5
    },
    "windSpeed": [2.3, 3.1, 2.8, 4.2, 1.8, 2.5, 3.0]
  },
  "timestamp": 1716268800000
}
```

---

### 4.8 实时天气

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/weather/now |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| location | string | 是 | 果园经纬度，格式："经度,纬度"（如：114.93,25.38） |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "obsTime": "2026-06-10T10:08+08:00",
    "temp": "23",
    "feelsLike": "23",
    "text": "阴",
    "windDir": "东风",
    "windScale": "1",
    "windSpeed": "5",
    "humidity": "55",
    "precip": "0.0",
    "vis": "28"
  },
  "timestamp": 1716268800000
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| obsTime | string | 数据观测时间 |
| temp | string | 温度（℃） |
| feelsLike | string | 体感温度（℃） |
| text | string | 天气状况文字描述 |
| windDir | string | 风向 |
| windScale | string | 风力等级 |
| windSpeed | string | 风速（km/h） |
| humidity | string | 相对湿度（%） |
| precip | string | 降水量（mm） |
| vis | string | 能见度（km） |

**说明**

- 后端根据传入的经纬度调用和风天气API获取实时天气数据
- 前端可从果园详情接口获取经纬度

---

### 4.9 24小时天气预报

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/weather/24h |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| location | string | 是 | 果园经纬度，格式："经度,纬度"（如：114.93,25.38） |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hourly": [
      {
        "fxTime": "2026-06-10T19:00+08:00",
        "temp": "22",
        "text": "多云",
        "windDir": "西南风",
        "windScale": "1-3",
        "windSpeed": "7",
        "humidity": "66",
        "pop": "0",
        "precip": "0.0"
      },
      {
        "fxTime": "2026-06-10T20:00+08:00",
        "temp": "21",
        "text": "晴",
        "windDir": "西南风",
        "windScale": "1-3",
        "windSpeed": "5",
        "humidity": "70",
        "pop": "0",
        "precip": "0.0"
      }
    ]
  },
  "timestamp": 1716268800000
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| hourly[].fxTime | string | 预报时间（ISO 8601格式） |
| hourly[].temp | string | 温度（℃） |
| hourly[].text | string | 天气状况文字描述 |
| hourly[].windDir | string | 风向 |
| hourly[].windScale | string | 风力等级 |
| hourly[].windSpeed | string | 风速（km/h） |
| hourly[].humidity | string | 相对湿度（%） |
| hourly[].pop | string | 降水概率（%） |
| hourly[].precip | string | 降水量（mm） |

**说明**

- 后端根据传入的经纬度调用和风天气API获取24小时逐小时预报数据
- 前端可从果园详情接口获取经纬度


---

### 4.10 7天天气预报

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/weather/7d |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| location | string | 是 | 果园经纬度，格式："经度,纬度"（如：114.93,25.38） |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "daily": [
      {
        "fxDate": "2026-06-11",
        "sunrise": "04:46",
        "sunset": "19:43",
        "tempMax": "31",
        "tempMin": "19",
        "textDay": "晴",
        "textNight": "多云",
        "windDirDay": "南风",
        "windScaleDay": "1-3",
        "windDirNight": "西南风",
        "windScaleNight": "1-3",
        "humidity": "38",
        "precip": "0.0",
        "uvIndex": "11"
      },
      {
        "fxDate": "2026-06-12",
        "sunrise": "04:46",
        "sunset": "19:44",
        "tempMax": "32",
        "tempMin": "20",
        "textDay": "雷阵雨",
        "textNight": "雷阵雨",
        "windDirDay": "南风",
        "windScaleDay": "1-3",
        "windDirNight": "东北风",
        "windScaleNight": "1-3",
        "humidity": "50",
        "precip": "0.0",
        "uvIndex": "7"
      },
      {
        "fxDate": "2026-06-13",
        "sunrise": "04:46",
        "sunset": "19:44",
        "tempMax": "28",
        "tempMin": "19",
        "textDay": "雷阵雨",
        "textNight": "多云",
        "windDirDay": "东风",
        "windScaleDay": "1-3",
        "windDirNight": "北风",
        "windScaleNight": "1-3",
        "humidity": "77",
        "precip": "13.6",
        "uvIndex": "6"
      }
    ]
  },
  "timestamp": 1716268800000
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| daily[].fxDate | string | 预报日期 |
| daily[].sunrise | string | 日出时间 |
| daily[].sunset | string | 日落时间 |
| daily[].tempMax | string | 最高温度（℃） |
| daily[].tempMin | string | 最低温度（℃） |
| daily[].textDay | string | 白天天气状况文字描述 |
| daily[].textNight | string | 夜间天气状况文字描述 |
| daily[].windDirDay | string | 白天风向 |
| daily[].windScaleDay | string | 白天风力等级 |
| daily[].windDirNight | string | 夜间风向 |
| daily[].windScaleNight | string | 夜间风力等级 |
| daily[].humidity | string | 相对湿度（%） |
| daily[].precip | string | 降水量（mm） |
| daily[].uvIndex | string | 紫外线指数 |

**说明**

- 后端根据传入的经纬度调用和风天气API获取3天每日预报数据
- 前端可从果园详情接口获取经纬度
- days(必选)预报天数，支持最多30天预报，可选值：
  3d 3天预报。
  7d 7天预报。
  10d 10天预报。
  15d 15天预报。
  30d 30天预报。

---

### 5.1 规则列表

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/rules |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "溃疡病高发预警",
      "diseaseCode": "S14",
      "diseaseName": "溃疡病",
      "conditions": {
        "tempMin": 25,
        "tempMax": 35,
        "humidityMin": 70,
        "humidityMax": 100,
        "detectionCount": 5,
        "detectionDays": 3
      },
      "level": 3,
      "cooldownHours": 24,
      "enabled": true,
      "createdAt": "2026-01-01 00:00:00"
    },
    {
      "id": 2,
      "name": "黄龙病早期预警",
      "code": "R002",
      "description": "近7天检测到斑驳花纹(S3)或红鼻果(S4)识别≥2次时触发",
      "conditions": {
        "detectionCount": 2,
        "detectionDays": 7
      },
      "level": 3,
      "cooldownHours": 48,
      "enabled": true,
      "createdAt": "2026-01-01 00:00:00"
    }
  ],
  "timestamp": 1716268800000
}
```

---

### 5.2 更新规则阈值

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/admin/rules/{id} |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 规则ID |
| tempMin | number | 否 | Body | 最低温度 |
| tempMax | number | 否 | Body | 最高温度 |
| humidityMin | number | 否 | Body | 最低湿度 |
| humidityMax | number | 否 | Body | 最高湿度 |
| diseaseCode | string | 否 | Body | 病害编码 |
| detectionCount | int | 否 | Body | 识别次数阈值 |
| detectionDays | int | 否 | Body | 统计天数 |
| cooldownHours | int | 否 | Body | 冷却时间（小时） |

**响应示例**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 5.3 启用/禁用规则

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/admin/rules/{id}/toggle |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 规则ID |

**响应示例**

```json
{
  "code": 200,
  "message": "切换成功",
  "data": {
    "enabled": false
  },
  "timestamp": 1716268800000
}
```

---

### 5.4 查询当前果园风险等级

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/alert/risk |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orchardId | long | 是 | 果园ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orchardId": 2001,
    "orchardName": "张家果园",
    "riskLevel": 2,
    "riskScore": 65,
    "factors": [
      {
        "type": "disease",
        "diseaseName": "溃疡病",
        "riskLevel": 3,
        "description": "近3天识别到溃疡病8次，超过阈值5次"
      },
      {
        "type": "environment",
        "riskLevel": 2,
        "description": "当前温度32℃，湿度82%，利于病害发展"
      },
      {
        "type": "weather",
        "riskLevel": 1,
        "description": "未来3天无连续降雨"
      }
    ],
    "suggestions": [
      "建议加强溃疡病防治，及时喷施铜制剂",
      "注意果园通风，降低湿度"
    ],
    "updatedAt": "2026-06-07 08:00:00"
  },
  "timestamp": 1716268800000
}
```

---

## M6 消息推送模块

### 6.1 我的消息列表

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/messages |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| level | int | 否 | 预警等级：1=L1 / 2=L2 / 3=L3 |
| readStatus | int | 否 | 已读状态：0=未读 / 1=已读 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 70001,
        "messageType": "alert",
        "title": "溃疡病预警",
        "summary": "张家果园检测到溃疡病",
        "level": 3,
        "orchardId": 2001,
        "orchardName": "张家果园",
        "ruleCode": "R001",
        "readStatus": 0,
        "createdAt": "2026-06-07 08:05:00"
      }
    ],
    "total": 200,
    "page": 1,
    "size": 20,
    "pages": 10
  },
  "timestamp": 1716268800000
}
```

---

### 6.2 未读数量

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/messages/unread-count |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "byLevel": {
      "L1": 10,
      "L2": 3,
      "L3": 2
    }
  },
  "timestamp": 1716268800000
}
```

---

### 6.3 标记已读

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/messages/{id}/read |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 消息ID |

**响应示例**

```json
{
  "code": 200,
  "message": "标记成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 6.4 全部已读

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/messages/read-all |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
  "code": 200,
  "message": "全部标记成功",
  "data": {
    "markedCount": 15
  },
  "timestamp": 1716268800000
}
```

---

### 6.5 消息详情

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/messages/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 消息ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 70001,
    "messageType": "alert",
    "title": "溃疡病预警",
    "summary": "张家果园检测到溃疡病",
    "content": "您的张家果园1号监测点检测到溃疡病，概率92%，请及时处理",
    "advice": "建议加强溃疡病防治，及时喷施铜制剂",
    "level": 3,
    "orchardId": 2001,
    "orchardName": "张家果园",
    "ruleId": 1,
    "ruleCode": "R001",
    "relatedType": "detection",
    "relatedId": 80001,
    "extraData": {
      "diseaseName": "溃疡病",
      "probability": 0.92,
      "imageUrl": "https://storage.example.com/detections/xxx.jpg"
    },
    "readStatus": 1,
    "readTime": "2026-06-07 08:10:00",
    "pushChannel": "app",
    "pushStatus": 1,
    "pushTime": "2026-06-07 08:05:30",
    "createdAt": "2026-06-07 08:05:00"
  },
  "timestamp": 1716268800000
}
```

---

## M7 知识库模块

### 7.1 上传文献

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/admin/knowledge/upload |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | multipart/form-data |

**请求参数（FormData）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | 文献文件，支持PDF/DOCX/TXT，最大20MB |
| category | string | 是 | 分类：disease=病害资料, pest=虫害资料, cultivation=栽培技术, pesticide=用药指南 |

**响应示例**

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "documentId": 9001,
    "fileName": "柑橘溃疡病防治指南.pdf",
    "fileSize": 2048576,
    "category": "disease",
    "status": "pending",
    "createdAt": "2026-06-07 08:30:00"
  },
  "timestamp": 1716268800000
}
```

---

### 7.2 文献列表

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/knowledge/documents |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| category | string | 否 | 分类筛选 |
| keyword | string | 否 | 文件名关键词 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 9001,
        "title": "柑橘溃疡病防治指南.pdf",
        "category": "disease",
        "fileSize": 2048576,
        "chunkCount": 45,
        "status": 2,
        "createdAt": "2026-06-07 08:30:00",
        "processedAt": "2026-06-07 08:35:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 20,
    "pages": 3
  },
  "timestamp": 1716268800000
}
```

---

### 7.3 删除文献

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | DELETE |
| 请求路径 | /api/admin/knowledge/documents/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 文献ID |

**响应示例**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 7.4 处理文献

👑 管理员

触发文献的分块和向量化处理。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/admin/knowledge/documents/{id}/process |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 文献ID |

**响应示例**

```json
{
  "code": 200,
  "message": "处理已提交",
  "data": {
    "documentId": 9001,
    "status": "processing"
  },
  "timestamp": 1716268800000
}
```

---

### 7.5 知识检索

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/knowledge/search |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| query | string | 是 | 检索关键词 |
| diseaseName | string | 否 | 限定病害名称 |
| orchardId | long | 否 | 关联果园上下文 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "query": "溃疡病防治方法",
    "results": [
      {
        "id": 1,
        "documentName": "柑橘溃疡病防治指南.pdf",
        "chunkContent": "溃疡病是由黄单胞杆菌引起的细菌性病害，主要危害叶片、枝梢和果实...",
        "relevanceScore": 0.95,
        "page": 12
      },
      {
        "id": 2,
        "documentName": "脐橙病虫害综合防治手册.pdf",
        "chunkContent": "防治溃疡病的关键措施包括：1. 加强检疫 2. 清除病源 3. 药剂防治...",
        "relevanceScore": 0.88,
        "page": 45
      }
    ]
  },
  "timestamp": 1716268800000
}
```

---

### 7.6 RAG问答

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/knowledge/qa |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| question | string | 是 | 用户问题 |
| diseaseName | string | 否 | 限定病害名称 |
| orchardId | long | 否 | 关联果园上下文 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "question": "脐橙溃疡病怎么防治？",
    "answer": "脐橙溃疡病的防治方法主要包括以下几个方面：\n\n1. **农业防治**：加强栽培管理，增施有机肥和磷钾肥，增强树势...\n2. **化学防治**：在新梢萌发后15-20天开始喷药，可选用...\n3. **生物防治**：利用拮抗微生物...\n\n注意事项：...",
    "references": [
      {
        "documentName": "柑橘溃疡病防治指南.pdf",
        "page": 12,
        "relevanceScore": 0.95
      }
    ],
    "confidence": 0.92
  },
  "timestamp": 1716268800000
}
```

---

## M8 小程序接口模块

### 8.1 首页看板

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/miniapp/dashboard |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orchardId | long | 否 | 果园ID，不传则返回默认果园 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orchard": {
      "id": 2001,
      "name": "张家果园",
      "areaMu": 50.5
    },
    "risk": {
      "level": 2,
      "score": 65
    },
    "todayStats": {
      "detectionCount": 128,
      "alertCount": 3,
      "healthyRate": 85.5
    },
    "environment": {
      "temperature": 28.5,
      "humidity": 75.2,
      "weather": "多云"
    },
    "recentAlerts": [
      {
        "id": 70001,
        "title": "溃疡病预警",
        "level": 3,
        "createdAt": "2026-06-07 08:05:00"
      }
    ],
    "diseaseRanking": [
      {
        "diseaseName": "溃疡病",
        "count": 52,
        "percentage": 40.6
      },
      {
        "diseaseName": "疮痂病",
        "count": 38,
        "percentage": 29.7
      }
    ]
  },
  "timestamp": 1716268800000
}
```

---

### 8.2 果园详情

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/miniapp/orchards/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 果园ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "name": "张家果园",
    "province": "江西省",
    "city": "赣州市",
    "district": "信丰县",
    "areaMu": 50.5,
    "treeAge": 8,
    "variety": "纽荷尔脐橙",
    "deviceCount": 5,
    "onlineDeviceCount": 4,
    "memberRole": "owner"
  },
  "timestamp": 1716268800000
}
```

---

### 8.3 病害历史

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/miniapp/orchards/{id}/detections |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 果园ID |
| page | int | 否 | Query | 页码，默认1 |
| size | int | 否 | Query | 每页数量，默认20 |
| diseaseCode | string | 否 | Query | 病害编码筛选 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 80001,
        "imageUrl": "https://storage.example.com/detections/xxx.jpg",
        "thumbnailUrl": "https://storage.example.com/detections/xxx_thumb.jpg",
        "diseaseCode": "S14",
        "diseaseName": "溃疡病",
        "probability": 0.92,
        "deviceName": "1号监测点",
        "detectedAt": "2026-06-07 08:00:00"
      }
    ],
    "total": 500,
    "page": 1,
    "size": 20,
    "pages": 25
  },
  "timestamp": 1716268800000
}
```

---

### 8.4 环境趋势

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/miniapp/orchards/{id}/environment/trend |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 果园ID |
| days | int | 否 | Query | 查询天数，默认7，可选：7/15/30 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dates": ["06-01", "06-02", "06-03", "06-04", "06-05", "06-06", "06-07"],
    "temperature": [26.5, 27.0, 28.2, 27.8, 26.3, 25.8, 27.5],
    "humidity": [72.0, 70.5, 68.2, 71.3, 75.8, 78.2, 73.5]
  },
  "timestamp": 1716268800000
}
```

---

### 8.5 预警列表

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/miniapp/alerts |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| level | int | 否 | 预警等级：1=L1 / 2=L2 / 3=L3 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 70001,
        "title": "溃疡病预警",
        "summary": "张家果园检测到溃疡病",
        "level": "danger",
        "orchardName": "张家果园",
        "isRead": false,
        "createdAt": "2026-06-07 08:05:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 20,
    "pages": 3
  },
  "timestamp": 1716268800000
}
```

---

### 8.6 知识检索

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/miniapp/knowledge/search |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| query | string | 是 | 检索关键词 |
| diseaseName | string | 否 | 限定病害名称 |
| orchardId | long | 否 | 关联果园上下文 |

**响应示例**

同 7.5 知识检索响应格式。

---

### 8.7 RAG问答

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/miniapp/knowledge/qa |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| question | string | 是 | 用户问题 |
| diseaseName | string | 否 | 限定病害名称 |
| orchardId | long | 否 | 关联果园上下文 |

**响应示例**

同 7.6 RAG问答响应格式。

---

## M9 农事日历模块

### 9.1 日历列表

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/calendar |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phenologyName | string | 否 | 物候期名称筛选 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "phenologyName": "春梢萌发期",
      "solarTerm": "雨水",
      "startMonth": 2,
      "startDay": 19,
      "endMonth": 3,
      "endDay": 5,
      "adviceContent": "加强水肥管理，促进春梢生长。注意防治溃疡病、疮痂病。",
      "diseaseRelated": ["S14", "S12"],
      "sortOrder": 1
    },
    {
      "id": 2,
      "phenologyName": "开花期",
      "solarTerm": "清明",
      "startMonth": 4,
      "startDay": 5,
      "endMonth": 4,
      "endDay": 20,
      "adviceContent": "保花保果，注意防治花蕾蛆。避免使用高毒农药。",
      "diseaseRelated": [],
      "sortOrder": 2
    }
  ],
  "timestamp": 1716268800000
}
```

---

### 9.2 创建日历

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/admin/calendar |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| phenologyName | string | 是 | 物候期名称 |
| solarTerm | string | 否 | 对应节气 |
| startMonth | int | 是 | 开始月份（1-12） |
| startDay | int | 是 | 开始日期 |
| endMonth | int | 是 | 结束月份（1-12） |
| endDay | int | 是 | 结束日期 |
| adviceContent | string | 是 | 农事建议内容 |
| diseaseRelated | array | 否 | 相关病害编码数组 |

**响应示例**

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 13
  },
  "timestamp": 1716268800000
}
```

---

### 9.3 更新日历

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | /api/admin/calendar/{id} |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 日历ID |
| phenologyName | string | 否 | Body | 物候期名称 |
| solarTerm | string | 否 | Body | 对应节气 |
| startMonth | int | 否 | Body | 开始月份 |
| startDay | int | 否 | Body | 开始日期 |
| endMonth | int | 否 | Body | 结束月份 |
| endDay | int | 否 | Body | 结束日期 |
| adviceContent | string | 否 | Body | 农事建议内容 |
| diseaseRelated | array | 否 | Body | 相关病害编码数组 |

**响应示例**

```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 9.4 删除日历

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | DELETE |
| 请求路径 | /api/admin/calendar/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 日历ID |

**响应示例**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1716268800000
}
```

---

### 9.5 当前提醒

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/miniapp/calendar/current |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "current": {
      "id": 6,
      "phenologyName": "果实膨大期",
      "solarTerm": "大暑",
      "period": "7月23日 - 8月7日",
      "adviceContent": "加强水肥管理，注意抗旱。重点防治锈壁虱、潜叶蛾。",
      "diseaseRelated": [
        {
          "diseaseCode": "S10",
          "diseaseName": "锈壁虱"
        }
      ]
    },
    "upcoming": {
      "id": 7,
      "phenologyName": "果实着色期",
      "solarTerm": "处暑",
      "period": "8月23日 - 9月7日",
      "daysLeft": 47
    }
  },
  "timestamp": 1716268800000
}
```

---

## M10 用户反馈模块

### 10.1 提交反馈

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/miniapp/feedback |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| orchardId | long | 是 | 果园ID |
| imageUrls | array | 是 | 上传图片URL数组 |
| description | string | 否 | 问题描述，最长500字 |
| feedbackType | string | 是 | 反馈类型：manual_diagnosis=人工诊断, issue_report=问题上报 |

**响应示例**

```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "feedbackId": 11001,
    "status": "pending_quiz"
  },
  "timestamp": 1716268800000
}
```

---

### 10.2 获取感官问卷

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/miniapp/feedback/{id}/quiz |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 反馈ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "feedbackId": 11001,
    "questions": [
      {"id": 1, "question": "叶子正面是否有黄褐色的小斑点？", "type": "confirm"},
      {"id": 2, "question": "斑点摸起来是不是粗糙凸起的？", "type": "confirm"},
      {"id": 3, "question": "斑点周围是不是有一圈黄色的晕？", "type": "confirm"},
      {"id": 4, "question": "最近半个月是不是下过大雨或台风？", "type": "confirm"}
    ]
  },
  "timestamp": 1716268800000
}
```

---

### 10.3 提交问卷答案

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/miniapp/feedback/{id}/quiz |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 反馈ID |
| answers | array | 是 | Body | 答案数组 |

**请求示例**

```json
{
  "answers": [
    {"questionId": 1, "answer": "yes"},
    {"questionId": 2, "answer": "yes"},
    {"questionId": 3, "answer": "no"},
    {"questionId": 4, "answer": "yes"}
  ]
}
```

matchScore = 3/4 = 0.75

**响应示例**

```json
{
  "code": 200,
  "message": "提交成功",
  "data": {
    "feedbackId": 11001,
    "status": "pending_review"
  },
  "timestamp": 1716268800000
}
```

---

### 10.4 我的反馈列表

📱 小程序（需登录）

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/miniapp/feedbacks |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 11001,
        "orchardName": "张家果园",
        "imageUrls": ["https://storage.example.com/feedback/xxx_thumb.jpg"],
        "feedbackType": "manual_diagnosis",
        "description": "果实上出现褐色斑点",
        "reviewStatus": 1,
        "aiResult": {
          "diseaseName": "溃疡病",
          "probability": 0.85
        },
        "createdAt": "2026-06-05 14:30:00"
      }
    ],
    "total": 20,
    "page": 1,
    "size": 20,
    "pages": 1
  },
  "timestamp": 1716268800000
}
```

---

### 10.5 待审核列表

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/feedbacks |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| reviewStatus | string | 否 | 审核状态：pending_quiz/pending_review/accepted/revised/rejected |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 11001,
        "userId": 10001,
        "userNickname": "果农张三",
        "orchardId": 2001,
        "orchardName": "张家果园",
        "imageUrls": ["https://storage.example.com/feedback/xxx.jpg"],
        "feedbackType": "manual_diagnosis",
        "description": "果实上出现褐色斑点",
        "reviewStatus": 0,
        "aiResult": {
          "diseaseName": "溃疡病",
          "probability": 0.85,
          "allProbabilities": {
            "S14": 0.85,
            "S12": 0.10,
            "S1": 0.05
          }
        },
        "userAnswers": [
          { "question": "病斑主要出现在哪个部位？", "answer": "果实" },
          { "question": "病斑的颜色特征？", "answer": "褐色、黑色" }
        ],
        "createdAt": "2026-06-05 14:30:00"
      }
    ],
    "total": 30,
    "page": 1,
    "size": 20,
    "pages": 2
  },
  "timestamp": 1716268800000
}
```

---

### 10.6 反馈详情

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/feedbacks/{id} |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 反馈ID |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 11001,
    "userId": 10001,
    "userNickname": "果农张三",
    "userPhone": "138****8000",
    "orchardId": 2001,
    "orchardName": "张家果园",
    "imageUrls": ["https://storage.example.com/feedback/xxx.jpg"],
    "feedbackType": "manual_diagnosis",
    "description": "果实上出现褐色斑点",
    "reviewStatus": 0,
    "aiResult": {
      "diseaseName": "溃疡病",
      "probability": 0.85,
      "allProbabilities": {
        "S14": 0.85,
        "S12": 0.10,
        "S1": 0.05
      }
    },
    "userAnswers": [
      { "question": "病斑主要出现在哪个部位？", "answer": "果实" },
      { "question": "病斑的颜色特征？", "answer": "褐色、黑色" },
      { "question": "病斑的形状特征？", "answer": "不规则形" },
      { "question": "是否有明显的病征？", "answer": "没有" },
      { "question": "发病的严重程度？", "answer": "中等" }
    ],
    "reviewHistory": [],
    "createdAt": "2026-06-05 14:30:00",
    "updatedAt": "2026-06-05 14:30:00"
  },
  "timestamp": 1716268800000
}
```

---

### 10.7 审核反馈

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | /api/admin/feedbacks/{id}/review |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 反馈ID |
| action | string | 是 | Body | 审核操作：accept=采纳, revise=修正, reject=拒绝 |
| comment | string | 否 | Body | 审核意见 |
| correctedDisease | string | 否 | Body | 修正后的病害名称（action=revise时必填） |

**请求示例**

```json
{
  "action": "revise",
  "comment": "根据病斑特征，判断为疮痂病而非溃疡病",
  "correctedDisease": "疮痂病"
}
```

**响应示例**

```json
{
  "code": 200,
  "message": "审核完成",
  "data": {
    "feedbackId": 11001,
    "reviewStatus": 2,
    "datasetIncluded": true
  },
  "timestamp": 1716268800000
}
```

---

### 10.8 导出训练集

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | /api/admin/feedbacks/export-dataset |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| format | string | 否 | 导出格式：csv/json，默认csv |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |
| diseaseCode | string | 否 | 病害编码筛选 |

**响应**

返回文件下载，Content-Type 为 `application/octet-stream`。

响应头：

```
Content-Disposition: attachment; filename="dataset_20260607.csv"
```

CSV 文件格式：

```csv
id,image_url,disease_code,disease_name,probability,feedback_type,quiz_answers,expert_disease,expert_comment,created_at
11001,https://storage.example.com/feedback/xxx.jpg,canker,溃疡病,0.85,disease,"{""部位"":""果实"",""颜色"":""褐色""}",疮痂病,修正为疮痂病,2026-06-05 14:30:00
```

---

## 五、附录

### 5.1 病害编码对照表

| 编码 | 病害名称 | 英文名 |
| --- | --- | --- |
| S1 | 健康果实 | Healthy fruit |
| S2 | 健康叶片 | Healthy leaf |
| S3 | 斑驳花纹（黄龙病早期） | Blotchy mottling |
| S4 | 红鼻果（黄龙病特征） | Red nose fruit |
| S5 | 缺锌 | Zinc deficiency |
| S6 | 叶脉黄化 | Vein yellowing |
| S7 | 均匀黄化 | Uniform yellowing |
| S8 | 缺镁 | Magnesium deficiency |
| S9 | 缺硼 | Boron deficiency |
| S10 | 炭疽病 | Anthracnose |
| S11 | 脂点黄斑病 | Citrus greasy spot |
| S12 | 藻斑与苔藓 | Algal spot and moss |
| S13 | 煤污病 | Sooty mould |
| S14 | 溃疡病 | Citrus canker |

### 5.2 设备类型对照表

| 编码 | 类型名称 | 说明 |
| --- | --- | --- |
| camera | 摄像头 | 用于图像采集和病害识别 |
| sensor | 传感器 | 用于环境数据采集（温湿度、土壤等） |
| weather_station | 气象站 | 用于气象数据采集 |

### 5.3 消息级别说明

| 级别 | 说明 | 推送方式 |
| --- | --- | --- |
| L1 (1) | 低风险-关注 | 仅应用内，每日汇总 |
| L2 (2) | 中风险-预警 | 应用内，实时推送 |
| L3 (3) | 高风险-紧急 | 应用内 + 短信，实时推送 |

### 5.4 分页参数说明

所有分页接口均支持以下通用参数：

| 参数名 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| page | int | 1 | 页码，从1开始 |
| size | int | 20 | 每页数量，最大100 |

### 5.5 通用枚举值

**用户状态 `status`**

| 值 | 说明 |
| --- | --- |
| 0 | 禁用 |
| 1 | 启用 |

**设备状态 `status`**

| 值 | 说明 |
| --- | --- |
| 0 | 离线 |
| 1 | 在线 |
| 2 | 故障 |

**设备类型 `type`**

| 值 | 说明 |
| --- | --- |
| camera | 摄像头 |
| sensor | 传感器 |

**预警等级 `level`**

| 值 | 说明 |
| --- | --- |
| 1 | L1 低风险-关注 |
| 2 | L2 中风险-预警 |
| 3 | L3 高风险-紧急 |

**消息类型 `messageType`**

| 值 | 说明 |
| --- | --- |
| alert | 预警消息 |
| calendar | 农事提醒 |
| system | 系统通知 |

**推送状态 `pushStatus`**

| 值 | 说明 |
| --- | --- |
| 0 | 待推送 |
| 1 | 已推送 |
| 2 | 推送失败 |

**已读状态 `readStatus`**

| 值 | 说明 |
| --- | --- |
| 0 | 未读 |
| 1 | 已读 |

**反馈类型 `feedbackType`**

| 值 | 说明 |
| --- | --- |
| manual_diagnosis | 拍照诊断 |
| issue_report | 问题反馈 |

**审核状态 `reviewStatus`**

| 值 | 说明 |
| --- | --- |
| 0 | 待审核 |
| 1 | 已采纳 |
| 2 | 已修正 |
| 3 | 已驳回 |

**果园成员角色 `roleType`**

| 值 | 说明 |
| --- | --- |
| owner | 园主 |
| manager | 管理员 |
| viewer | 只读 |

**系统角色 `roles`**

| 值 | 说明 |
| --- | --- |

ROLE_FARMER | 农户 |

| ROLE_ADMIN | 管理员 |
