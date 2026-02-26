# StreamLab 视频平台后端

## 1. 项目概述

**StreamLab** - 仿 B 站视频平台后端，Spring Boot 3 + Java 21

### 技术栈
- Spring Boot 3.5.7 / Java 21 / Maven
- MySQL 8.0 + Flyway
- JPA + MyBatis
- JWT + Spring Security 6
- Lombok + MapStruct

---

## 2. 项目结构

```
streamlab/
├── src/main/java/com/franklintju/streamlab/
│   ├── auth/          # 认证：JWT、双令牌、BCrypt
│   ├── users/         # 用户：注册、登录、资料
│   ├── videos/        # 视频：CRUD、分页、统计
│   ├── danmaku/       # 弹幕：实体、Repository
│   ├── comment/       # 评论：树形结构
│   ├── follow/        # 关注：粉丝/关注列表
│   ├── category/      # 分类：多级分类
│   ├── history/       # 历史：观看记录
│   ├── interaction/   # 点赞/投币/收藏
│   ├── upload/        # 上传：任务管理、转码
│   ├── config/        # 安全、JPA配置
│   └── common/        # 异常处理、日志
│
├── src/main/resources/
│   ├── application.properties
│   ├── db/migration/  # Flyway迁移文件
│   └── mapper/        # MyBatis映射
└── docs/
    ├── DATABASE_DESIGN.md  # 数据库设计
    └── TODO.md             # 待实现功能
```

---

## 3. API 接口

### 认证 `/api/auth`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 登录 | 公开 |
| POST | /api/auth/refresh | 刷新Token | 公开 |
| POST | /api/auth/logout | 退出 | 需认证 |
| GET | /api/auth/me | 当前用户 | 需认证 |

### 用户 `/api/users`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/users | 注册 | 公开 |
| GET | /api/users/{id} | 用户信息 | 公开 |
| GET | /api/users/{id}/videos | 用户视频 | 公开 |
| POST | /api/users/{id}/change-password | 修改密码 | 需认证 |

### 视频 `/api/videos`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/videos | 创建视频 | 需认证 |
| GET | /api/videos/{id} | 视频详情 | 公开 |
| GET | /api/videos/list | 视频列表 | 公开 |
| POST | /api/videos/{id}/view | 增加播放量 | 公开 |
| POST | /api/videos/{id}/like | 点赞 | 需认证 |

### 弹幕 `/api/danmaku` & WebSocket
| 类型 | 目的地 | 说明 |
|------|--------|------|
| WebSocket | /topic/danmaku/{videoId} | 订阅弹幕 |
| STOMP | /app/danmaku | 发送弹幕 |

---

## 4. 数据库表

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| users | 用户账号 | phone, email, password |
| profiles | 用户资料 | username, avatar, bio |
| videos | 视频 | title, url, status, views |
| danmaku | 弹幕 | video_id, content, send_time |
| comments | 评论 | video_id, parent_id, root_id |
| user_follows | 关注 | follower_id, following_id |
| video_likes | 点赞 | user_id, video_id |
| watch_histories | 观看历史 | user_id, video_id, progress |

---

## 5. 配置

### application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/streamlabDB
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true

spring.jwt.secret=${JWT_SECRET}
spring.jwt.access-token-expiration=900
spring.jwt.refresh-token-expiration=604800

file.upload-dir=uploads/
```

### 环境变量
| 变量 | 说明 | 必须 |
|------|------|------|
| JWT_SECRET | JWT签名密钥 | 是 |
| DB_PASSWORD | 数据库密码 | 是 |

---

## 6. 启动

```bash
# 构建
./mvnw clean package

# 运行
./mvnw spring-boot:run

# 测试
./mvnw test
```

---

## 7. AIOps 预留

### 日志格式
```java
log.info("ACTION=PUBLISH_VIDEO USER_ID={} VIDEO_ID={}", userId, videoId);
```

### 压力检测
```java
public boolean isSystemHighPressure() {
    double cpu = getJVMCpuUsage();
    return cpu > 0.8;
}
```

---

文档版本: 1.0
最后更新: 2026-02-05
