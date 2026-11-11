# 表情检测系统 — Java后端 API接口文档

> **文档版本**：v1.9
>
> **更新日期**：2026-08-01
>
> **Base URL**：`http://localhost:8080`
>
> **认证方式**：JWT Bearer Token（Header携带）
>
> **统一响应格式**：
> ```json
> {
>   "code": 200,
>   "message": "success",
>   "data": {},
>   "timestamp": 1753478400000
> }
> ```

---

## 一、基础信息

### 1.1 API Host

| 环境 | Host |
| --- | --- |
| 开发环境 | `http://localhost:8080` |

### 1.2 认证方式

需要登录的接口，请求头中携带 Access Token：

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 1.3 通用返回状态码

| 状态码 | 含义 |
| --- | --- |
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限（如非管理员访问管理接口） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 1.4 统一响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1753478400000
}
```

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 业务状态码，200=成功 |
| message | string | 提示信息 |
| data | object | 业务数据（可选，部分接口无data） |
| timestamp | long | 响应时间戳（毫秒） |

---

## 二、认证模块 `/api/auth`

### 2.1 用户注册

**接口说明**

用户注册新账号，注册成功后可直接登录。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/auth/register` |
| Content-Type | `application/json` |
| 需要登录 | 否 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| username | string | 是 | 登录账号（手机号） |
| email | string | 是 | 邮箱地址（用于登录和找回密码） |
| password | string | 是 | 密码（6-20位，需含字母和数字） |
| nickname | string | 否 | 用户昵称（默认：用户_xxxx） |

**请求示例**

```json
POST http://localhost:8080/api/auth/register

{
    "username": "13800138000",
    "email": "zhangsan@example.com",
    "password": "Abc123456",
    "nickname": "张三"
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "注册成功",
    "data": {
        "userId": 1001,
        "username": "13800138000",
        "email": "zhangsan@example.com",
        "nickname": "张三"
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 200=注册成功，400=参数错误（账号或邮箱已存在） |
| data.userId | long | 新用户ID |
| data.username | string | 注册手机号 |
| data.email | string | 注册邮箱 |
| data.nickname | string | 用户昵称 |

---

### 2.2 用户登录

**接口说明**

用户登录获取JWT令牌。支持**手机号**或**邮箱**两种方式登录，连续登录失败5次将锁定账号30分钟。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/auth/login` |
| Content-Type | `application/json` |
| 需要登录 | 否 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| account | string | 是 | 登录账号（手机号或邮箱） |
| password | string | 是 | 密码 |

**请求示例**

```json
POST http://localhost:8080/api/auth/login

{
    "account": "13800138000",
    "password": "Abc123456"
}
```

```json
POST http://localhost:8080/api/auth/login

{
    "account": "zhangsan@example.com",
    "password": "Abc123456"
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "a1b2c3d4e5f6g7h8...",
        "expiresIn": 7200,
        "user": {
            "userId": 1001,
            "username": "13800138000",
            "email": "zhangsan@example.com",
            "nickname": "张三",
            "avatarUrl": null,
            "role": 1
        }
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 200=成功，400=账号或密码错误，403=账号已锁定 |
| data.accessToken | string | JWT短期令牌（2小时有效） |
| data.refreshToken | string | 长期刷新令牌（20天有效） |
| data.expiresIn | int | Access Token过期时间（秒） |
| data.user.userId | long | 用户ID |
| data.user.username | string | 手机号 |
| data.user.email | string | 邮箱 |
| data.user.nickname | string | 用户昵称 |
| data.user.avatarUrl | string | 头像URL（可能为null） |
| data.user.role | int | 角色：1=普通用户 2=管理员 |

---

### 2.3 刷新Token

**接口说明**

Access Token过期后，使用Refresh Token获取新的Access Token。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/auth/refresh` |
| Content-Type | `application/json` |
| 需要登录 | 否 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| refreshToken | string | 是 | 登录时返回的Refresh Token |

**请求示例**

```json
POST http://localhost:8080/api/auth/refresh

{
    "refreshToken": "a1b2c3d4e5f6g7h8..."
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "刷新成功",
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiJ9...(新Token)",
        "expiresIn": 7200
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 200=成功，401=Refresh Token无效或已过期 |
| data.accessToken | string | 新的JWT短期令牌 |
| data.expiresIn | int | 过期时间（秒） |

---

### 2.4 退出登录

**接口说明**

退出登录，同时删除Redis和数据库中的Token。需要在请求头中携带Access Token。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/auth/logout` |
| 需要登录 | 是 |

**请求头示例**

```
POST /api/auth/logout HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "退出成功"
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 200=退出成功 |

---

## 三、用户模块 `/api/user`

> 以下接口均需登录，请求头携带 `Authorization: Bearer {accessToken}`

### 3.1 用户列表

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/admin/users` |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认10 |
| keyword | string | 否 | 搜索关键词（模糊匹配账号/昵称） |

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 100,
        "page": 1,
        "size": 10,
        "pages": 10,
        "records": [
            {
                "userId": 1001,
                "username": "13800138000",
                "email": "zhangsan@example.com",
                "nickname": "张三",
                "avatarUrl": "https://oss.xxx.com/avatar/1001.jpg",
                "role": 1,
                "status": 1,
                "createdAt": "2026-07-20 10:00:00",
                "lastLoginAt": "2026-07-25 14:30:00",
                "lastLoginIp": "192.168.1.100"
            }
        ]
    },
    "timestamp": 1753478400000
}
```

---

### 3.2 用户详情

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/admin/users/{id}` |
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
        "userId": 1001,
        "username": "13800138000",
        "email": "zhangsan@example.com",
        "nickname": "张三",
        "avatarUrl": "https://oss.xxx.com/avatar/1001.jpg",
        "role": 1,
        "status": 1,
        "createdAt": "2026-07-20 10:00:00",
        "updatedAt": "2026-07-25 14:30:00",
        "lastLoginAt": "2026-07-25 14:30:00",
        "lastLoginIp": "192.168.1.100"
    },
    "timestamp": 1753478400000
}
```

---

### 3.3 更新用户状态

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | `/api/admin/users/{id}/status` |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 用户ID |
| status | int | 是 | Body | 状态：0=禁用, 1=启用 |

**请求示例**

```json
{
    "status": 0
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "操作成功",
    "data": null,
    "timestamp": 1753478400000
}
```

---

### 3.4 操作日志

👑 管理员

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/admin/logs` |
| 请求头 | Authorization: Bearer {accessToken} |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| operationType | string | 否 | 操作类型：login/register/detect |
| operationModule | string | 否 | 操作模块：user/detection/system |
| startDate | string | 否 | 开始日期，格式：yyyy-MM-dd |
| endDate | string | 否 | 结束日期，格式：yyyy-MM-dd |

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 500,
        "page": 1,
        "size": 20,
        "pages": 25,
        "records": [
            {
                "id": 10001,
                "userId": 1001,
                "username": "13800138000",
                "operationType": "login",
                "operationModule": "user",
                "description": "密码登录",
                "operationTime": "2026-07-25 14:30:00",
                "clientIp": "192.168.1.100",
                "userAgent": "Mozilla/5.0...",
                "status": 1,
                "detail": {"loginType": "password", "isNewDevice": false}
            }
        ]
    },
    "timestamp": 1753478400000
}
```

---

### 3.5 查看个人信息

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/user/profile` |
| 请求头 | Authorization: Bearer {accessToken} |

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": 1001,
        "username": "138****8000",
        "email": "zhangsan@example.com",
        "nickname": "张三",
        "avatarUrl": "https://oss.xxx.com/avatar/1001.jpg",
        "role": 1,
        "createdAt": "2026-07-25 10:00:00",
        "lastLoginAt": "2026-07-25 14:30:00"
    },
    "timestamp": 1753478400000
}
```

---

### 3.6 修改密码

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | `/api/user/password` |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| oldPassword | string | 是 | 原密码 |
| newPassword | string | 是 | 新密码，6-20位，需包含字母和数字 |

**请求示例**

```json
{
    "oldPassword": "Abc123456",
    "newPassword": "Def789012"
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "密码修改成功",
    "data": null,
    "timestamp": 1753478400000
}
```

**说明**

- 新密码不能与原密码相同
- 密码修改成功后，当前accessToken立即失效，需重新登录
- 若忘记原密码，请使用"忘记密码"功能通过短信验证码重置

---

### 3.7 通知设置

🔒 需登录

**接口说明**

已实现。用户可配置是否接收预警消息（pushEnabled）与是否开启邮箱推送（emailEnabled）。部分更新，未传字段不修改。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | `/api/user/notification-settings` |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | application/json |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| emailEnabled | boolean | 否 | 是否开启邮箱推送（默认0=关闭，1=开启；开启后每日22:00对未处理预警发邮件提醒） |
| pushEnabled | boolean | 否 | 是否接收预警消息（默认1=接收，0=不接收；关闭后情绪预警定时任务对该账号停用，天气预警按地区不受影响） |

**请求示例**

```json
{
    "emailEnabled": true,
    "pushEnabled": false
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "设置成功",
    "data": {
        "userId": 1001,
        "username": "13800138000",
        "email": "zhangsan@example.com",
        "nickname": "张三",
        "pushEnabled": 0,
        "emailEnabled": 1
    },
    "timestamp": 1753478400000
}
```

**说明**

- 返回值 `data` 为更新后的完整用户信息（UserDTO）
- `GET /api/user/profile` 同样会返回 `pushEnabled` / `emailEnabled`，用于个人中心展示当前设置

---

### 3.8 上传头像

🔒 需登录

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/user/avatar` |
| 请求头 | Authorization: Bearer {accessToken} |
| Content-Type | multipart/form-data |

**请求参数（FormData）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | 头像图片文件，支持jpg/jpeg/png格式，最大2MB |

**响应示例**

```json
{
    "code": 200,
    "message": "上传成功",
    "data": "https://oss.xxx.com/avatars/uuid-1234.jpg",
    "timestamp": 1753478400000
}
```

**说明**

- 上传成功后会自动更新用户头像URL
- 如果用户已有旧头像，会自动删除旧头像文件
- 头像存储在云存储的 `avatars/` 目录下
- 文件名由系统自动生成（UUID），避免冲突

---

## 四、检测模块 `/api/detection`

> 以下接口均需登录

### 4.1 表情检测（核心接口）

**接口说明**

上传图片进行表情检测。后端将图片上传到云存储，调用Python推理服务进行检测，将结果和建议保存到数据库。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/detection/detect` |
| Content-Type | `multipart/form-data` |
| 需要登录 | 是 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | 图片文件（jpg/png，≤10MB） |

**请求示例**

```
POST http://localhost:8080/api/detection/detect
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: multipart/form-data

file: (图片二进制数据)
```

**响应示例**

```json
{
    "code": 200,
    "message": "检测成功",
    "data": {
        "recordId": 5001,
        "imageUrl": "https://oss.xxx.com/detect/2026/07/25/uuid.jpg",
        "emotionLabel": "sad",
        "emotionCn": "悲伤",
        "confidence": 0.9234,
        "faceCount": 1,
        "validFaceCount": 1,
        "detectDetail": {
            "faces": [
                {
                    "bbox": [120, 80, 350, 400],
                    "faceConfidence": 0.9873,
                    "rejectReason": null,
                    "allEmotions": [
                        {"label": "angry", "confidence": 0.0105},
                        {"label": "disgust", "confidence": 0.0},
                        {"label": "fear", "confidence": 0.1362},
                        {"label": "happy", "confidence": 0.0},
                        {"label": "sad", "confidence": 0.5401},
                        {"label": "surprise", "confidence": 0.0},
                        {"label": "neutral", "confidence": 0.3001},
                        {"label": "unknown", "confidence": 0.0131}
                    ]
                }
            ],
            "pipelineVersion": "v1.0",
            "modelVersion": "yolov8n-cls-8class"
        },
        "adviceText": "每个人都会有情绪低落的时刻。请记住，你并不孤单。如果愿意，可以试着和信任的朋友聊聊天，或者做一些让自己放松的事情。你的感受是重要的。",
        "adviceSource": "System Default",
        "adviceSourceType": "dict",
        "processingTimeMs": 156
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 200=检测成功，400=图片格式错误或无人脸 |
| data.recordId | long | 检测记录ID（用于反馈） |
| data.imageUrl | string | 云存储带签名的图片URL |
| data.emotionLabel | string | 表情英文标签：angry/disgust/fear/happy/neutral/sad/surprise/unknown |
| data.emotionCn | string | 表情中文标签 |
| data.confidence | double | 模型置信度（0~1） |
| data.faceCount | int | 检测到的总人脸数 |
| data.validFaceCount | int | 通过质量检查的有效人脸数 |
| data.detectDetail | object | 详细检测结果（含bbox、8类表情概率分布） |
| data.adviceText | string | 心理健康建议内容 |
| data.adviceSource | string | 建议来源（文档名 / System Default） |
| data.adviceSourceType | string | llm=RAG生成 / dict=兜底字典 / none=无建议 |
| data.processingTimeMs | int | 推理耗时（毫秒） |

---

### 4.2 查询检测历史

**接口说明**

分页查询当前用户的检测历史记录，支持按表情类型和日期范围筛选。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/detection/history` |
| 需要登录 | 是 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码（默认1） |
| size | int | 否 | 每页条数（默认10，最大50） |
| emotionLabel | string | 否 | 按表情筛选（如：sad/angry/happy） |
| startDate | string | 否 | 开始日期（如：2026-07-01） |
| endDate | string | 否 | 结束日期（如：2026-07-25） |

**请求示例**

```
GET http://localhost:8080/api/detection/history?page=1&size=10&emotionLabel=sad
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 35,
        "page": 1,
        "size": 10,
        "records": [
            {
                "recordId": 5001,
                "imageUrl": "https://oss.xxx.com/detect/2026/07/25/uuid.jpg",
                "emotionLabel": "sad",
                "emotionCn": "悲伤",
                "confidence": 0.9234,
                "adviceSourceType": "dict",
                "createdAt": "2026-07-25 14:30:00"
            }
        ]
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.total | long | 总记录数 |
| data.page | int | 当前页码 |
| data.size | int | 每页条数 |
| data.records | array | 检测记录列表 |
| data.records[].recordId | long | 检测记录ID |
| data.records[].imageUrl | string | 图片URL |
| data.records[].emotionLabel | string | 表情英文标签 |
| data.records[].emotionCn | string | 表情中文标签 |
| data.records[].confidence | double | 置信度 |
| data.records[].adviceSourceType | string | 建议来源类型 |
| data.records[].createdAt | string | 检测时间 |

---

### 4.3 获取检测详情

**接口说明**

根据记录ID获取单条检测记录的完整详情，包括检测结果、建议、反馈和环境数据。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/detection/{id}` |
| 需要登录 | 是 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 路径参数，检测记录ID |

**请求示例**

```
GET http://localhost:8080/api/detection/5001
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "recordId": 5001,
        "imageUrl": "https://oss.xxx.com/detect/2026/07/25/uuid.jpg",
        "objectKey": "2026/07/25/uuid.jpg",
        "emotionLabel": "sad",
        "emotionCn": "悲伤",
        "confidence": 0.9234,
        "faceCount": 1,
        "validFaceCount": 1,
        "detectDetail": {
            "faces": [
                {
                    "bbox": [120, 80, 350, 400],
                    "faceConfidence": 0.9873,
                    "rejectReason": null,
                    "allEmotions": [
                        {"label": "angry", "confidence": 0.0105},
                        {"label": "sad", "confidence": 0.5401},
                        {"label": "neutral", "confidence": 0.3001}
                    ]
                }
            ],
            "pipelineVersion": "v1.0",
            "modelVersion": "yolov8n-cls-8class"
        },
        "adviceText": "每个人都会有情绪低落的时刻。请记住，你并不孤单。",
        "adviceSource": "System Default",
        "adviceSourceType": "dict",
        "processingTimeMs": 156,
        "createdAt": "2026-07-25 14:30:00",
        "feedback": {
            "isHelpful": null,
            "feedbackText": null
        },
        "environment": {
            "weatherMain": "多云",
            "temperature": 23.0,
            "humidity": 55,
            "windDir": "东风",
            "windScale": "3-4",
            "locationName": "广州市",
            "indoorOutDoor": 2
        }
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.recordId | long | 检测记录ID |
| data.imageUrl | string | 图片URL |
| data.objectKey | string | 云存储原始路径 |
| data.emotionLabel | string | 表情英文标签 |
| data.emotionCn | string | 表情中文标签 |
| data.confidence | double | 置信度 |
| data.faceCount | int | 总人脸数 |
| data.validFaceCount | int | 有效人脸数 |
| data.detectDetail | object | 详细检测结果JSON |
| data.adviceText | string | 建议内容 |
| data.adviceSource | string | 建议来源 |
| data.adviceSourceType | string | llm/dict/none |
| data.processingTimeMs | int | 推理耗时（ms） |
| data.createdAt | string | 检测时间 |
| data.feedback | object | 反馈数据（未反馈则为null） |
| data.feedback.isHelpful | boolean | 是否有用（1=有用 0=没用） |
| data.feedback.feedbackText | string | 补充意见 |
| data.environment | object | 环境数据（未采集则为null） |
| data.environment.weatherMain | string | 天气状况（晴/多云/阴/小雨等） |
| data.environment.temperature | double | 温度（℃） |
| data.environment.humidity | int | 湿度（%） |
| data.environment.windDir | string | 风向 |
| data.environment.windScale | string | 风力等级 |
| data.environment.locationName | string | 位置描述 |
| data.environment.indoorOutDoor | int | 0=未知 1=室内 2=室外 |

---

### 4.4 提交纠正反馈

**接口说明**

用户对检测结果中的表情标签进行纠正反馈，用于收集训练数据改善模型准确率。图片复用检测时已上传的图片，用户只需选择正确的表情标签。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/detection/{recordId}/feedback` |
| Content-Type | `application/json` |
| 需要登录 | 是 |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| recordId | long | 是 | Path | 检测记录ID |
| correctLabel | string | 是 | Body | 正确的表情标签：angry/disgust/fear/happy/neutral/sad/surprise/unknown |
| description | string | 否 | Body | 补充描述（≤500字） |

**请求示例**

```json
POST http://localhost:8080/api/detection/5001/feedback
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

{
    "correctLabel": "sad",
    "description": "实际是悲伤的表情，系统误识别为happy"
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "反馈提交成功"
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| code | int | 200=提交成功，400=该检测记录已提交过反馈 |

---

### 4.5 查看反馈状态

**接口说明**

用户查看自己对某条检测记录的反馈审核状态。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/detection/{recordId}/feedback/status` |
| 需要登录 | 是 |

**请求示例**

```
GET http://localhost:8080/api/detection/5001/feedback/status
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例（已提交反馈）**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "feedbackId": 1001,
        "systemLabel": "happy",
        "correctLabel": "sad",
        "status": 1,
        "statusText": "已采用",
        "adminNote": "确认标注正确",
        "createdAt": "2026-07-26 14:30:00",
        "reviewedAt": "2026-07-26 15:00:00"
    }
}
```

**响应示例（未提交反馈）**

```json
{
    "code": 200,
    "message": "success",
    "data": null
}
```

---

## 五、天气模块 `/api/weather`

> 需要登录

### 5.1 获取当前天气

**接口说明**

根据区域编码（adcode）获取当前实时天气数据，数据来源于高德地图天气API。天气数据同时会自动写入 `environment_record` 表。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/weather/current` |
| 需要登录 | 是 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| location | string | 是 | 区域编码（如：440100=广州，330100=杭州） |

**请求示例**

```
GET http://localhost:8080/api/weather/current?location=440100
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "weatherMain": "阴",
        "temperature": 23.0,
        "humidity": 55,
        "windDir": "东风",
        "windScale": "3-4",
        "obsTime": "2026-07-25 10:08:00"
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.weatherMain | string | 天气状况文字描述（晴/多云/阴/小雨等） |
| data.temperature | double | 温度（℃） |
| data.humidity | int | 相对湿度（%） |
| data.windDir | string | 风向（东风/南风/西北风等） |
| data.windScale | string | 风力等级（如：3-4） |
| data.obsTime | string | 数据发布时间（格式：yyyy-MM-dd HH:mm:ss） |

---

### 5.2 获取4天预报

**接口说明**

获取指定城市未来4天（含当天）的每日天气预报数据。每次调用会同步将预报结果（含白天/夜间温度、湿度）写入 `weather_record` 表（record_type=2），供天气预警定时任务使用。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/weather/forecast` |
| 需要登录 | 是 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| location | string | 是 | 区域编码（adcode） |

**请求示例**

```
GET http://localhost:8080/api/weather/forecast?location=440100
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "daily": [
            {
                "fxDate": "2026-07-25",
                "tempMax": 31,
                "tempMin": 19,
                "textDay": "晴",
                "textNight": "多云"
            },
            {
                "fxDate": "2026-07-26",
                "tempMax": 32,
                "tempMin": 20,
                "textDay": "雷阵雨",
                "textNight": "雷阵雨"
            }
        ]
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.daily | array | 每日预报列表 |
| data.daily[].fxDate | string | 预报日期 |
| data.daily[].tempMax | int | 最高温度（℃） |
| data.daily[].tempMin | int | 最低温度（℃） |
| data.daily[].textDay | string | 白天天气描述 |
| data.daily[].textNight | string | 夜间天气描述 |

---

## 六、预警消息模块 `/api/alert`

> 需要登录。普通用户仅能查看自己**user_id 的情绪预警**及自己所在地区（`user.location_id`）的天气预警；管理员（`role = 2`）可查看/操作全部。
>
> **预警产生机制（定时任务）：**
> 1. **天气预警**：每 4 天 0:00 拉取高德预报 → 0:05 遍历 `alert_rule` 中含天气条件的规则，4 天预报全部满足即写入（按地区，无 user_id）
> 2. **情绪预警**：每天 0:06 遍历 `alert_rule` 中 `emotion_labels` 非空的规则，按 `time_window_hours` 时间范围统计各用户 `detection_record` 命中记录数，≥ `min_occurrences` 即触发（`NIGHT_NEGATIVE` 规则仅统计 22:00~06:00 的记录）；仅对 `push_enabled=1` 的用户写入；同时写入 `emotion_count`/`emotion_label`/`last_detect_time` 结构化数据
> 3. **邮件提醒**：每天 22:00 对 `active=1 且 status=0 且 email_sent=0` 的情绪预警，向开启了 `push_enabled=1 且 email_enabled=1` 的用户发送固定静态模板邮件（占位符填充结构化数据，💡建议按 `emotion_label` 查 `emotion_dict.defaultAdvice`），发送成功即置 `status=1`（已处理）与 `email_sent=1`（只发一次）

### 6.1 分页查询预警消息

**接口说明**

分页查询天气预警消息，按触发时间倒序排列。普通用户强制按自己地区（location）过滤，管理员返回全部。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/alert/messages` |
| 需要登录 | 是 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码（默认1） |
| size | int | 否 | 每页条数（默认10） |

**请求示例**

```
GET http://localhost:8080/api/alert/messages?page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 2,
        "page": 1,
        "size": 10,
        "pages": 1,
        "records": [
            {
                "id": 3,
                "ruleId": 2,
                "ruleName": "高温预警",
                "location": "440100",
                "cityName": "广州",
                "fxDateStart": "2026-07-29",
                "fxDateEnd": "2026-08-01",
                "alertContent": "未来4天（2026-07-29至2026-08-01）持续高温，最高35℃~38℃，建议减少户外活动，注意防暑降温。",
                "active": 1,
                "status": 0,
                "createdAt": "2026-07-29 00:05:00"
            }
        ]
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.total | long | 总记录数 |
| data.page | int | 当前页码 |
| data.size | int | 每页条数 |
| data.pages | int | 总页数 |
| data.records | array | 预警消息列表 |
| data.records[].id | long | 消息ID |
| data.records[].ruleId | int | 触发规则ID |
| data.records[].ruleName | string | 规则名称 |
| data.records[].location | string | 区域编码（adcode）（情绪预警为NULL） |
| data.records[].cityName | string | 城市名称 |
| data.records[].userId | long | 情绪预警关联用户ID（天气预警为NULL） |
| data.records[].fxDateStart | string | 预警区间开始日期（yyyy-MM-dd） |
| data.records[].fxDateEnd | string | 预警区间结束日期（yyyy-MM-dd） |
| data.records[].alertContent | string | 预警内容 |
| data.records[].active | int | 是否生效：1=生效，0=已解除 |
| data.records[].status | int | 阅读状态：0=未读，1=已读 |
| data.records[].emailSent | int | 邮件发送标记：0=未发，1=已发（只发一次） |
| data.records[].emotionCount | int | 情绪预警触发次数（天气预警为NULL） |
| data.records[].emotionLabel | string | 触发情绪标签（多个用/分隔，天气预警为NULL） |
| data.records[].lastDetectTime | string | 最近一次命中检测时间（天气预警为NULL） |
| data.records[].createdAt | string | 触发时间（yyyy-MM-dd HH:mm:ss） |

---

### 6.2 预警摘要

**接口说明**

获取当前用户可见的预警消息总数及最新3条，用于首页预警卡片回显。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/alert/messages/summary` |
| 需要登录 | 是 |

**请求示例**

```
GET http://localhost:8080/api/alert/messages/summary
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 2,
        "recent": [
            {
                "id": 3,
                "ruleId": 2,
                "ruleName": "高温预警",
                "location": "440100",
                "cityName": "广州",
                "fxDateStart": "2026-07-29",
                "fxDateEnd": "2026-08-01",
                "alertContent": "未来4天（2026-07-29至2026-08-01）持续高温，最高35℃~38℃，建议减少户外活动，注意防暑降温。",
                "active": 1,
                "status": 0,
                "createdAt": "2026-07-29 00:05:00"
            }
        ]
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.total | long | 可见预警总数 |
| data.recent | array | 最新预警消息（最多3条），结构同 `data.records[]` |

---

### 6.3 标记预警消息已读

**接口说明**

将指定预警消息标记为已读。非管理员只能操作自己地区的预警，否则返回 403。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | `/api/alert/messages/{id}/read` |
| 需要登录 | 是 |

**请求示例**

```
PUT http://localhost:8080/api/alert/messages/3/read
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "已标记为已读",
    "data": null
}
```

---

## 七、管理模块 `/api/admin`

> 👑 以下接口需要管理员权限（`role = 2`），所有接口均需登录

### 7.1 查询预警规则列表

👑 管理员

**接口说明**

获取所有预警规则配置。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/admin/alert-rules` |
| 请求头 | `Authorization: Bearer {accessToken}` |

**请求示例**

```
GET http://localhost:8080/api/admin/alert-rules
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "ruleName": "连续悲伤预警",
            "ruleCode": "SAD_CONTINUOUS",
            "enabled": true,
            "priority": 10,
            "emotionLabels": "sad",
            "minConfidence": 0.70,
            "weatherConditions": null,
            "minTemperature": null,
            "maxTemperature": null,
            "minHumidity": null,
            "maxHumidity": null,
            "timeWindowHours": 24,
            "minOccurrences": 3,
            "actionType": "push",
            "cooldownMinutes": 120
        }
    ],
    "timestamp": 1753478400000
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data[].id | int | 规则ID |
| data[].ruleName | string | 规则名称 |
| data[].ruleCode | string | 规则编码 |
| data[].enabled | boolean | 是否启用 |
| data[].priority | int | 优先级 |
| data[].emotionLabels | string | 匹配表情（逗号分隔） |
| data[].minConfidence | double | 置信度下限 |
| data[].weatherConditions | string | 匹配天气（逗号分隔中文） |
| data[].minTemperature | double | 温度下限（含边界） |
| data[].maxTemperature | double | 温度上限（含边界） |
| data[].minHumidity | integer | 湿度下限（%，含边界） |
| data[].maxHumidity | integer | 湿度上限（%，含边界） |
| data[].timeWindowHours | int | 统计时间窗口（小时） |
| data[].minOccurrences | int | 最少出现次数 |
| data[].actionType | string | 动作类型：push=推送 |
| data[].cooldownMinutes | int | 冷却时间（分钟） |

---

### 7.2 修改预警规则

👑 管理员

**接口说明**

修改指定预警规则的配置。支持部分更新，仅传入需要修改的字段即可。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | `/api/admin/alert-rules/{id}` |
| 请求头 | `Authorization: Bearer {accessToken}` |
| Content-Type | `application/json` |

**请求参数（Path + Body）**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | int | 是 | Path | 规则ID |
| enabled | boolean | 否 | Body | 是否启用 |
| ruleName | string | 否 | Body | 规则名称 |
| emotionLabels | string | 否 | Body | 匹配表情（逗号分隔英文标签，如：sad,angry,fear） |
| weatherConditions | string | 否 | Body | 匹配天气（逗号分隔中文，如：小雨,大雨,雪） |
| minTemperature | number | 否 | Body | 温度下限（℃） |
| maxTemperature | number | 否 | Body | 温度上限（℃） |
| minHumidity | int | 否 | Body | 湿度下限（%，0-100） |
| maxHumidity | int | 否 | Body | 湿度上限（%，0-100） |
| minOccurrences | int | 否 | Body | 最少出现次数（≥1） |
| timeWindowHours | int | 否 | Body | 统计时间窗口（小时，≥1） |
| cooldownMinutes | int | 否 | Body | 冷却时间（分钟，≥1） |

**注意：** 天气/温度/湿度条件传 null 表示不限制（NULL=不限制），规则评估时未配置的条件跳过比对。

**请求示例**

```json
PUT http://localhost:8080/api/admin/alert-rules/1
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "enabled": false,
    "ruleName": "连续悲伤预警（修改后）",
    "emotionLabels": "sad,angry",
    "weatherConditions": "小雨,大雨",
    "minTemperature": 0,
    "maxTemperature": 15,
    "minHumidity": 60,
    "maxHumidity": null,
    "minOccurrences": 5,
    "timeWindowHours": 48,
    "cooldownMinutes": 180
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "修改成功",
    "data": null,
    "timestamp": 1753478400000
}
```

---

### 7.3 查询表情字典列表

👑 管理员

**接口说明**

获取所有表情字典配置（8种表情的兜底建议）。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/admin/emotion-dict` |
| 请求头 | `Authorization: Bearer {accessToken}` |

**请求示例**

```
GET http://localhost:8080/api/admin/emotion-dict
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "emotionLabel": "happy",
            "emotionCn": "开心",
            "emojiIcon": "😊",
            "defaultAdvice": "看到你心情不错，真为你感到高兴！",
            "adviceSource": "System Default",
            "sortOrder": 1,
            "isActive": true
        }
    ],
    "timestamp": 1753478400000
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data[].id | int | 字典ID |
| data[].emotionLabel | string | 表情英文标签 |
| data[].emotionCn | string | 表情中文名称 |
| data[].emojiIcon | string | Emoji图标 |
| data[].defaultAdvice | string | 兜底建议内容 |
| data[].adviceSource | string | 建议来源 |
| data[].sortOrder | int | 排序顺序 |
| data[].isActive | boolean | 是否启用 |

---

### 7.4 修改表情字典

👑 管理员

**接口说明**

修改指定表情的兜底建议内容。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | `/api/admin/emotion-dict/{id}` |
| 请求头 | `Authorization: Bearer {accessToken}` |
| Content-Type | `application/json` |

**请求参数（Path + Body）**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | int | 是 | Path | 字典ID |
| defaultAdvice | string | 否 | Body | 新的兜底建议内容 |
| isActive | boolean | 否 | Body | 是否启用 |

**请求示例**

```json
PUT http://localhost:8080/api/admin/emotion-dict/3
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "defaultAdvice": "每个人都会有情绪低落的时刻。请记住，你并不孤单。新的建议内容...",
    "isActive": true
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "修改成功",
    "data": null,
    "timestamp": 1753478400000
}
```

---

### 7.5 反馈管理列表

👑 管理员

**接口说明**

分页查询表情纠正反馈列表，支持按审核状态和表情标签筛选。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/admin/feedbacks` |
| 请求头 | `Authorization: Bearer {accessToken}` |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页数量，默认20 |
| status | int | 否 | 审核状态：0=待审核 1=已采用 2=已驳回 |
| correctLabel | string | 否 | 按正确标签筛选 |

**请求示例**

```
GET http://localhost:8080/api/admin/feedbacks?status=0&page=1&size=20
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 35,
        "page": 1,
        "size": 20,
        "pages": 2,
        "records": [
            {
                "feedbackId": 1001,
                "userId": 1001,
                "username": "13800138000",
                "imageUrl": "http://tirphv4db.hn-bkt.clouddn.com/detect/xxx.jpg",
                "systemLabel": "happy",
                "correctLabel": "sad",
                "description": "实际是悲伤的表情",
                "status": 0,
                "createdAt": "2026-07-26 14:30:00"
            }
        ]
    }
}
```

---

### 7.6 审核反馈

👑 管理员

**接口说明**

审核表情纠正反馈：采用（用于训练数据导出）或驳回。管理员可修正 correctLabel。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | PUT |
| 请求路径 | `/api/admin/feedbacks/{id}/review` |
| 请求头 | `Authorization: Bearer {accessToken}` |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 位置 | 说明 |
| --- | --- | --- | --- | --- |
| id | long | 是 | Path | 反馈ID |
| status | int | 是 | Body | 1=采用 2=驳回 |
| correctLabel | string | 否 | Body | 修正后的正确标签（与用户标注不一致时使用） |
| adminNote | string | 否 | Body | 管理员备注 |

**请求示例**

```json
PUT http://localhost:8080/api/admin/feedbacks/1001/review
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

{
    "status": 1,
    "correctLabel": "sad",
    "adminNote": "确认标注正确，图片已用于训练"
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "审核成功",
    "data": null
}
```

---

### 7.7 导出训练数据

👑 管理员

**接口说明**

将所有已采用（status=1）的反馈按 correct_label 分类，下载图片并打包为 zip 文件，可直接用于 YOLO 重训练。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/admin/feedbacks/export` |
| 请求头 | `Authorization: Bearer {accessToken}` |

**响应**

返回 `application/zip` 文件流，浏览器直接下载。

**zip 文件结构**

```
emotion_feedback_export_20260726.zip
├── angry/
│   ├── fb_001.jpg
│   └── fb_002.jpg
├── happy/
│   └── fb_003.jpg
├── sad/
│   └── fb_004.jpg
└── ...（8个类别文件夹）
```

下载后直接放入 YOLO 训练目录即可重训练模型。

---

### 7.5 发送测试预警邮件

**接口说明**

管理员测试 SMTP 配置与邮件样式，向指定邮箱发送一封固定版式预警邮件。仅管理员（`role=2`）可调用。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/admin/alert/email/test` |
| 请求头 | `Authorization: Bearer {accessToken}`（管理员），`Content-Type: application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| to | string | 是 | 收件人邮箱 |
| subject | string | 否 | 邮件主题（不填默认"表情检测预警提醒"，建议填如 `【情绪预警】连续悲伤预警`） |
| title | string | 否 | 标题（首行加粗，如规则名称） |
| userName | string | 否 | 用户称呼（昵称/用户名） |
| emotion | string | 否 | 触发情绪标签（如 `悲伤/焦虑`） |
| count | int | 否 | 触发次数 |
| advice | string | 否 | 建议内容（💡建议卡片文本） |
| lastTime | string | 否 | 最近一次检测时间（yyyy-MM-dd HH:mm:ss） |

**请求示例**

```json
{
    "to": "15179015408@163.com",
    "subject": "【情绪预警】连续悲伤预警",
    "title": "连续悲伤预警",
    "userName": "张三",
    "emotion": "悲伤",
    "count": 3,
    "advice": "建议多晒太阳、与朋友交流、参与感兴趣的活动；若持续低落请及时寻求专业帮助。",
    "lastTime": "2026-07-25 15:34:00"
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "测试邮件已发送",
    "data": null,
    "timestamp": 1785830400000
}
```

**失败响应**

```json
{
    "code": 500,
    "message": "邮件发送失败，请检查SMTP配置与日志",
    "data": null,
    "timestamp": 1785830400000
}
```

**邮件样式说明**

固定静态模板（`alert_email.html`）渲染，情绪关怀卡片版式：暖色渐变背景（顶部 #FFF5F0 奶白 → 底部 #F0E6FF 淡薰衣草紫）+ 半透明白毛玻璃卡片（`rgba(255,255,255,0.75)` + `blur(20px)` + 24px 圆角）+ 水印"✦ 心语守护" + 问候"{{userName}}，注意到你最近的情绪变化" + 💡建议卡片（珊瑚粉强调色）+ 时间行 + 淡灰虚线分隔线 + 热线（"如需专业帮助，可随时拨打 400-161-9995"）+ 右对齐页脚"内容仅供陪伴参考 ✦"。全局字体色 #3D2C2C，无红色元素。发送时仅将结构化数据填入占位符，不解析文案文本。

---

## 八、系统模块 `/api/system`

### 8.1 健康检查

**接口说明**

检查系统各组件运行状态，包括数据库、Redis、Python推理服务。无需登录。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/system/health` |
| 需要登录 | 否 |

**请求示例**

```
GET http://localhost:8080/api/system/health
```

**响应示例**

```json
{
    "code": 200,
    "data": {
        "status": "UP",
        "services": {
            "database": "UP",
            "redis": "UP",
            "pythonService": "UP"
        },
        "modelInfo": {
            "modelLoaded": true,
            "className": "yolov8n-cls-8class",
            "numClasses": 8,
            "device": "cuda:0"
        }
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.status | string | 整体状态：UP=正常 DOWN=异常 |
| data.services.database | string | MySQL数据库状态 |
| data.services.redis | string | Redis缓存状态 |
| data.services.pythonService | string | Python推理服务状态 |
| data.modelInfo.modelLoaded | boolean | 模型是否已加载 |
| data.modelInfo.className | string | 模型类别名 |
| data.modelInfo.numClasses | int | 表情类别数 |
| data.modelInfo.device | string | 运行设备（cuda:0/cpu） |

---

## 九、评估模块 `/api/assessment`

> 🔒 以下接口均需登录，请求头携带 `Authorization: Bearer {accessToken}`

### 9.1 获取问卷列表

**接口说明**

获取当前可用的评估问卷列表（仅返回已启用的问卷）。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/assessment/list` |
| 需要登录 | 是 |

**请求示例**

```
GET http://localhost:8080/api/assessment/list
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "questionnaires": [
            {
                "id": 1,
                "name": "PHQ-9抑郁筛查量表",
                "code": "PHQ9",
                "description": "过去两周内，你有多常被以下问题所困扰？",
                "totalQuestions": 9
            },
            {
                "id": 2,
                "name": "GAD-7焦虑筛查量表",
                "code": "GAD7",
                "description": "过去两周内，你有多常被以下问题所困扰？",
                "totalQuestions": 7
            }
        ]
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.questionnaires | array | 已启用的问卷列表 |
| data.questionnaires[].id | long | 问卷ID |
| data.questionnaires[].name | string | 问卷名称 |
| data.questionnaires[].code | string | 问卷编码（PHQ9/GAD7等） |
| data.questionnaires[].description | string | 问卷描述 |
| data.questionnaires[].totalQuestions | int | 题目总数 |

---

### 9.2 开始评估

**接口说明**

用户选择问卷后开始一次新的评估，返回第一题信息和评估记录ID。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/assessment/start` |
| Content-Type | `application/json` |
| 需要登录 | 是 |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| questionnaireId | long | 是 | 问卷ID（从问卷列表接口获取） |

**请求示例**

```json
POST http://localhost:8080/api/assessment/start
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "questionnaireId": 1
}
```

**响应示例**

```json
{
    "code": 200,
    "message": "评估已开始",
    "data": {
        "recordId": 5001,
        "questionnaireId": 1,
        "questionnaireName": "PHQ-9抑郁筛查量表",
        "totalQuestions": 9,
        "currentQuestion": {
            "questionId": 101,
            "questionIndex": 1,
            "questionText": "做事时提不起劲或没有兴趣",
            "options": [
                {"label": "A", "text": "完全不会", "value": 0},
                {"label": "B", "text": "好几天", "value": 1},
                {"label": "C", "text": "一半以上", "value": 2},
                {"label": "D", "text": "几乎每天", "value": 3}
            ]
        }
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.recordId | long | 评估记录ID（后续答题需使用） |
| data.questionnaireId | long | 问卷ID |
| data.questionnaireName | string | 问卷名称 |
| data.totalQuestions | int | 总题数 |
| data.currentQuestion.questionId | long | 当前题目ID |
| data.currentQuestion.questionIndex | int | 当前题目序号（从1开始） |
| data.currentQuestion.questionText | string | 题目内容 |
| data.currentQuestion.options | array | 四个选项列表 |
| data.currentQuestion.options[].label | string | 选项标签：A/B/C/D |
| data.currentQuestion.options[].text | string | 选项文字描述 |
| data.currentQuestion.options[].value | int | 选项分值 |

**错误码说明**

| code | message | 说明 |
| --- | --- | --- |
| 400 | 问卷不存在或未启用 | questionnaireId无效 |
| 400 | 该问卷暂无题目 | 问卷未配置题目 |
| 5004 | 评估已超时 | 上一轮评估超时未完成 |

---

### 9.3 提交答案

**接口说明**

提交当前题目答案，返回下一题信息或评估完成结果。每提交一题，后端会累加分数到Redis缓存，最后一题提交后自动匹配评分规则得出等级和建议。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/assessment/answer` |
| Content-Type | `application/json` |
| 需要登录 | 是 |

**请求参数（Body）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| recordId | long | 是 | 评估记录ID（从start接口获取） |
| questionId | long | 是 | 当前题目ID |
| selectedOption | string | 是 | 用户选择的选项：A / B / C / D |

**请求示例**

```json
POST http://localhost:8080/api/assessment/answer
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
    "recordId": 5001,
    "questionId": 101,
    "selectedOption": "B"
}
```

**响应示例（未完成，返回下一题）**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "finished": false,
        "currentQuestion": {
            "questionId": 102,
            "questionIndex": 2,
            "questionText": "感到心情低落、沮丧或绝望",
            "options": [
                {"label": "A", "text": "完全不会", "value": 0},
                {"label": "B", "text": "好几天", "value": 1},
                {"label": "C", "text": "一半以上", "value": 2},
                {"label": "D", "text": "几乎每天", "value": 3}
            ]
        },
        "progress": {
            "current": 2,
            "total": 9
        }
    }
}
```

**响应示例（已完成）**

```json
{
    "code": 200,
    "message": "评估已完成",
    "data": {
        "finished": true,
        "result": {
            "recordId": 5001,
            "totalScore": 7,
            "levelCode": "mild",
            "levelName": "轻度",
            "advice": "建议关注自身情绪变化，保持规律作息，适当运动。如症状持续两周以上未改善，建议寻求专业心理咨询。",
            "completedAt": "2026-07-30 15:30:00"
        }
    }
}
```

**响应字段说明（未完成）**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.finished | boolean | false=评估未完成，需继续答题 |
| data.currentQuestion | object | 下一题信息（结构同9.2） |
| data.progress.current | int | 当前进度（下一题的序号） |
| data.progress.total | int | 总题数 |

**响应字段说明（已完成）**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.finished | boolean | true=评估已完成 |
| data.result.recordId | long | 评估记录ID |
| data.result.totalScore | int | 总分 |
| data.result.levelCode | string | 等级编码：normal/mild/moderate/severe/extremely_severe |
| data.result.levelName | string | 等级中文名称：正常/轻度/中度/中重度/重度 |
| data.result.advice | string | 综合建议内容（从 grade_rule 表匹配） |
| data.result.completedAt | string | 完成时间（yyyy-MM-dd HH:mm:ss） |

**错误码说明**

| code | message | 说明 |
| --- | --- | --- |
| 400 | 评估记录不匹配 | recordId与当前用户不匹配 |
| 400 | 题目不存在或已禁用 | questionId无效 |
| 5004 | 评估已超时 | 超过30分钟未操作，需重新开始 |

---

### 9.4 评估历史

**接口说明**

分页查询当前用户的历史评估记录。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/assessment/history` |
| 需要登录 | 是 |

**请求参数（Query）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| page | int | 否 | 页码（默认1） |
| size | int | 否 | 每页条数（默认10，最大50） |

**请求示例**

```
GET http://localhost:8080/api/assessment/history?page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 5,
        "records": [
            {
                "recordId": 5001,
                "questionnaireId": 1,
                "questionnaireName": "PHQ-9抑郁筛查量表",
                "questionnaireCode": "PHQ9",
                "totalScore": 7,
                "levelCode": "mild",
                "levelName": "轻度",
                "completedAt": "2026-07-30 15:30:00"
            }
        ]
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.total | long | 总记录数 |
| data.records | array | 历史记录列表 |
| data.records[].recordId | long | 评估记录ID |
| data.records[].questionnaireId | long | 问卷ID |
| data.records[].questionnaireName | string | 问卷名称 |
| data.records[].questionnaireCode | string | 问卷编码 |
| data.records[].totalScore | int | 总分 |
| data.records[].levelCode | string | 等级编码 |
| data.records[].levelName | string | 等级中文名称 |
| data.records[].completedAt | string | 完成时间 |

---

### 9.5 评估详情

**接口说明**

获取某次评估的完整结果详情。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | GET |
| 请求路径 | `/api/assessment/detail/{id}` |
| 需要登录 | 是 |

**请求参数（Path）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | long | 是 | 评估记录ID |

**请求示例**

```
GET http://localhost:8080/api/assessment/detail/5001
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**响应示例**

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "recordId": 5001,
        "questionnaireId": 1,
        "questionnaireName": "PHQ-9抑郁筛查量表",
        "questionnaireCode": "PHQ9",
        "totalScore": 7,
        "levelCode": "mild",
        "levelName": "轻度",
        "advice": "建议关注自身情绪变化，保持规律作息，适当运动。",
        "startedAt": "2026-07-30 15:20:00",
        "completedAt": "2026-07-30 15:30:00"
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.recordId | long | 评估记录ID |
| data.questionnaireId | long | 问卷ID |
| data.questionnaireName | string | 问卷名称 |
| data.questionnaireCode | string | 问卷编码 |
| data.totalScore | int | 总分 |
| data.levelCode | string | 等级编码 |
| data.levelName | string | 等级中文名称 |
| data.advice | string | 建议内容 |
| data.startedAt | string | 开始时间 |
| data.completedAt | string | 完成时间 |

---

### 9.6 上传文档 + AI解析

**接口说明**

管理员上传 DOCX 格式的评估问卷文档，系统自动提取文字并调用智谱 AI 解析为结构化数据（题目+评分规则），返回给前端预览确认。

**请求**

| 项目 | 说明 |
| --- | --- |
| 请求方法 | POST |
| 请求路径 | `/api/assessment/document/parse` |
| Content-Type | `multipart/form-data` |
| 需要登录 | 是 |

**请求参数（FormData）**

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | DOCX 格式文档（仅支持 .docx） |

**请求示例**

```
POST http://localhost:8080/api/assessment/document/parse
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: multipart/form-data

file: (PHQ-9量表.docx 二进制数据)
```

**响应示例**

```json
{
    "code": 200,
    "message": "文档解析成功，请预览后确认",
    "data": {
        "name": "PHQ-9抑郁筛查量表",
        "code": "PHQ9",
        "description": "过去两周内，你有多常被以下问题所困扰？",
        "questions": [
            {
                "questionIndex": 1,
                "questionText": "做事时提不起劲或没有兴趣",
                "optionA": "完全不会",
                "optionB": "好几天",
                "optionC": "一半以上",
                "optionD": "几乎每天",
                "scoreA": 0,
                "scoreB": 1,
                "scoreC": 2,
                "scoreD": 3
            }
        ],
        "gradeRules": [
            {
                "minScore": 0,
                "maxScore": 4,
                "levelCode": "normal",
                "levelName": "正常",
                "adviceText": "保持良好的生活习惯，继续保持。"
            },
            {
                "minScore": 5,
                "maxScore": 9,
                "levelCode": "mild",
                "levelName": "轻度",
                "adviceText": "建议关注自身情绪变化，适当运动和社交。"
            }
        ]
    }
}
```

**响应字段说明**

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| data.name | string | AI提取的问卷名称 |
| data.code | string | AI提取的问卷编码 |
| data.description | string | AI提取的问卷描述 |
| data.questions | array | AI解析的题目列表 |
| data.questions[].questionIndex | int | 题目序号 |
| data.questions[].questionText | string | 题目内容 |
| data.questions[].optionA~D | string | 四个选项文字 |
| data.questions[].scoreA~D | int | 四个选项的分值 |
| data.gradeRules | array | AI解析的评分规则列表 |
| data.gradeRules[].minScore | int | 最低分（包含） |
| data.gradeRules[].maxScore | int | 最高分（包含） |
| data.gradeRules[].levelCode | string | 等级编码 |
| data.gradeRules[].levelName | string | 等级名称 |
| data.gradeRules[].adviceText | string | 建议内容 |

**注意：** 返回的解析结果需由管理员在预览页面确认后，再调用管理端入库接口完成数据持久化。当前接口仅做解析预览，不自动写入数据库。

---

## 附录A：区域编码对照表（常用）

| 城市 | 区域编码（adcode） |
| --- | --- |
| 北京 | 110000 |
| 上海 | 310000 |
| 广州 | 440100 |
| 深圳 | 440300 |
| 杭州 | 330100 |
| 成都 | 510100 |
| 武汉 | 420100 |

## 附录B：高德地图API配置

| 配置项 | 值 |
| --- | --- |
| API Host | `https://restapi.amap.com` |
| API Key | 通过配置文件 `map.amap.api-key` 配置 |
| 认证方式 | 请求参数 `key={key}` |

## 附录C：Python服务调用配置

| 配置项 | 值 |
| --- | --- |
| Python服务地址 | `http://localhost:8001` |
| 检测接口 | `POST /api/inference/image` |
| 健康检查 | `GET /api/inference/health` |

---

**文档版本：** v1.9
**最后更新：** 2026-08-01
**适用范围：** Java SpringBoot后端接口
**关联文档：** 数据库设计文档.md / 项目功能文档.md / 天气API测试文档.md / AI聊天与心理健康评估功能文档.md