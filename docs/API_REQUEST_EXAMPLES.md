# StreamLab API 请求示例

> Base URL: `http://localhost:8080`
>
> 认证：登录后获取 `accessToken`，在 Header 中添加：
> ```
> Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
> ```

---

## 1. 认证模块 `/api/auth`

### 1.1 用户登录 🔓 公开
```json
POST /api/auth/login
Content-Type: application/json

{
    "email": "user@example.com",
    "password": "password123"
}
```

**响应**：
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### 1.2 刷新 Token 🔓 公开
```json
POST /api/auth/refresh
Cookie: refreshToken=eyJhbGciOiJIUzI1NiJ9...
```

---

### 1.3 获取当前用户 🔐 需认证
```json
GET /api/auth/me
Authorization: Bearer <token>
```

---

### 1.4 退出登录 🔐 需认证
```json
POST /api/auth/logout
Authorization: Bearer <token>
```

---

## 2. 用户模块 `/api/users`

### 2.1 注册用户 🔓 公开
```json
POST /api/users
Content-Type: application/json

{
    "phone": "13800138000",
    "email": "user@example.com",
    "password": "password123"
}
```

---

### 2.2 获取用户信息 🔓 公开
```json
GET /api/users/{id}
```

**示例**：GET `/api/users/1`

---

### 2.3 获取所有用户 🔓 公开
```json
GET /api/users?sort=createdAt
```

---

### 2.4 删除用户 🔐 需认证
```json
DELETE /api/users/{id}
Authorization: Bearer <token>
```

---

### 2.5 修改密码 🔐 需认证
```json
POST /api/users/{id}/change-password
Content-Type: application/json
Authorization: Bearer <token>

{
    "oldPassword": "oldpassword123",
    "newPassword": "newpassword456"
}
```

---

### 2.6 获取用户视频列表 🔓 公开
```json
GET /api/users/{id}/videos
```

**示例**：GET `/api/users/1/videos`

---

## 3. 用户资料模块 `/api/profiles`

### 3.1 获取用户资料 🔓 公开
```json
GET /api/profiles/{id}
```

**示例**：GET `/api/profiles/1`

---

### 3.2 更新用户资料 🔐 需认证
```json
POST /api/profiles/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
    "username": "新昵称",
    "avatarUrl": "https://example.com/avatar.jpg",
    "bio": "这是我的个人简介",
    "gender": "MALE",
    "birthday": "2000-01-01"
}
```

**gender 可选值**：`MALE`, `FEMALE`, `OTHER`

---

## 4. 关注模块 `/api/users`

### 4.1 关注用户 🔐 需认证
```json
POST /api/users/follow
Content-Type: application/json
Authorization: Bearer <token>

{
    "followerId": 1,
    "followingId": 2
}
```

**说明**：`followerId` 当前登录用户，`followingId` 要关注的用户

---

### 4.2 取消关注 🔐 需认证
```json
POST /api/users/unfollow
Content-Type: application/json
Authorization: Bearer <token>

{
    "followerId": 1,
    "followingId": 2
}
```

---

### 4.3 获取关注列表 🔓 公开
```json
GET /api/users/{id}/following
```

**示例**：GET `/api/users/1/following`

---

### 4.4 获取粉丝列表 🔓 公开
```json
GET /api/users/{id}/follower
```

**示例**：GET `/api/users/1/follower`

---

## 5. 视频模块 `/api/videos`

### 5.1 创建视频 🔐 需认证
```json
POST /api/videos
Content-Type: application/json
Authorization: Bearer <token>

{
    "title": "我的第一个视频",
    "description": "这是视频描述内容",
    "coverUrl": "https://example.com/cover.jpg"
}
```

**响应**：
```json
{
    "id": 1,
    "title": "我的第一个视频",
    "description": "这是视频描述内容",
    "coverUrl": "https://example.com/cover.jpg",
    "status": "DRAFT",
    "viewsCount": 0,
    "likesCount": 0,
    ...
}
```

---

### 5.2 更新视频 🔐 需认证
```json
PUT /api/videos/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
    "title": "更新后的标题",
    "description": "更新后的描述",
    "coverUrl": "https://example.com/new-cover.jpg",
    "videoUrl": "https://example.com/video.mp4"
}
```

---

### 5.3 删除视频 🔐 需认证
```json
DELETE /api/videos/{id}
Authorization: Bearer <token>
```

---

### 5.4 获取视频详情 🔓 公开
```json
GET /api/videos/{id}
```

**示例**：GET `/api/videos/1`

---

### 5.5 获取用户视频列表 🔓 公开
```json
GET /api/videos?userId={userId}
```

**示例**：GET `/api/videos?userId=1`

---

### 5.6 获取视频列表（分页）🔓 公开
```json
GET /api/videos/list?page=0&size=10
```

**示例**：GET `/api/videos/list?page=0&size=20`

---

### 5.7 增加播放量 🔓 公开
```json
POST /api/videos/{id}/view
```

**示例**：POST `/api/videos/1/view`

---

## 6. 上传模块 `/api/upload`

### 6.1 上传视频文件 🔐 需认证
```bash
POST /api/upload/{videoId}
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: [选择视频文件]
```

**说明**：`videoId` 是创建视频后返回的 ID

**示例**：
```
POST /api/upload/1
Parameter name: file
File: myvideo.mp4
```

---

### 6.2 获取上传任务状态 🔐 需认证
```json
GET /api/upload/tasks/{taskId}
```

**示例**：GET `/api/upload/tasks/1

---

## 完整测试流程

```
1. 注册用户
   POST /api/users
   → 获取用户ID

2. 登录
   POST /api/auth/login
   → 获取 accessToken

3. 创建视频（Draft状态）
   POST /api/videos
   Header: Authorization: Bearer <token>
   → 获取 videoId

4. 上传视频文件
   POST /api/upload/{videoId}
   Header: Authorization: Bearer <token>
   File: [视频文件]

5. 获取视频详情
   GET /api/videos/{videoId}

6. 播放视频
   POST /api/videos/{videoId}/view
```

---

## 错误响应示例

```json
// 401 Unauthorized
{
    "error": "Full authentication is required to access this resource"
}

// 400 Bad Request
{
    "phone": ["手机号不能为空"],
    "email": ["邮箱格式不正确"]
}

// 404 Not Found
{
    "error": "Resource not found"
}
```

---

*文档版本: 1.0*
*最后更新: 2026-02-05*
