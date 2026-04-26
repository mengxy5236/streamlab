# API Request Examples

Base URL:

```text
http://localhost:8080
```

Authentication:

```text
Authorization: Bearer <access_token>
```

## 1. Auth

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Refresh access token:

```http
POST /api/auth/refresh
Cookie: refreshToken=<refresh_token>
```

Get current user:

```http
GET /api/auth/me
Authorization: Bearer <access_token>
```

## 2. Users

Register:

```http
POST /api/users
Content-Type: application/json

{
  "phone": "13800138000",
  "email": "user@example.com",
  "password": "password123"
}
```

Get user profile:

```http
GET /api/users/1
```

Change password:

```http
POST /api/users/1/change-password
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "oldPassword": "password123",
  "newPassword": "newPassword456"
}
```

## 3. Videos

Create draft video:

```http
POST /api/videos
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "title": "My first video",
  "description": "A short introduction",
  "coverUrl": "https://example.com/cover.jpg"
}
```

Get public video detail:

```http
GET /api/videos/1
```

List public videos:

```http
GET /api/videos/list?page=0&size=10
```

Increase view count:

```http
POST /api/videos/1/view
```

Publish video:

```http
POST /api/videos/1/publish
Authorization: Bearer <access_token>
```

## 4. Upload

Upload source video:

```http
POST /api/upload/1
Authorization: Bearer <access_token>
Content-Type: multipart/form-data

file=<video_file>
```

Upload cover:

```http
POST /api/upload/1/cover
Authorization: Bearer <access_token>
Content-Type: multipart/form-data

file=<image_file>
```

Query upload/transcode task:

```http
GET /api/upload/tasks/1
Authorization: Bearer <access_token>
```

## 5. Comments

Create comment:

```http
POST /api/comments
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "videoId": 1,
  "content": "Nice video"
}
```

List comments:

```http
GET /api/comments/video/1?page=0&size=10
```

Like comment:

```http
POST /api/comments/1/like
Authorization: Bearer <access_token>
```

## 6. Likes and History

Like video:

```http
POST /api/likes/video/1
Authorization: Bearer <access_token>
```

Get like status:

```http
GET /api/likes/video/1/status
Authorization: Bearer <access_token>
```

Record watch progress:

```http
POST /api/history/video/1
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "progress": 120,
  "duration": 600
}
```

Get my history:

```http
GET /api/history?page=0&size=10
Authorization: Bearer <access_token>
```
