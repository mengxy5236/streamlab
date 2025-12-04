# StreamLab 数据库设计文档

## 概述

本文档描述 StreamLab 项目的数据库表结构设计，基于 MySQL 8.0，使用 Flyway 进行版本管理。

---

## ER 关系图

```
users 1──1 profiles
  │
  ├──< user_follows (自关联多对多)
  │
  ├──< videos 1──1 video_stats
  │     │
  │     ├──< video_categories >──1 categories (多对多)
  │     │
  │     ├──< video_likes
  │     ├──< video_coins
  │     ├──< video_favorites
  │     ├──< watch_histories
  │     ├──< comments
  │     └──< danmaku
  │
  └──< upload_tasks
```

---

## 表结构

### 1. 用户模块

#### users - 用户账号表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| phone | VARCHAR(11) | UNIQUE, NULL | 手机号 |
| email | VARCHAR(100) | UNIQUE, NULL | 邮箱 |
| password | VARCHAR(255) | NOT NULL | BCrypt加密密码 |
| status | ENUM | NOT NULL, DEFAULT 'ACTIVE' | ACTIVE/BANNED/DELETED |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

约束：`CHECK (phone IS NOT NULL OR email IS NOT NULL)`

#### profiles - 用户档案表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 档案ID |
| user_id | BIGINT | FK, UNIQUE | 关联用户 |
| username | VARCHAR(50) | UNIQUE | 昵称 |
| avatar_url | VARCHAR(500) | NULL | 头像URL |
| bio | TEXT | NULL | 个人简介 |
| gender | ENUM | NULL | MALE/FEMALE/OTHER |
| birthday | DATE | NULL | 生日 |
| level | INT | DEFAULT 1 | 用户等级 |
| coins | INT | DEFAULT 0 | 硬币余额 |
| followers_count | INT | DEFAULT 0 | 粉丝数 |
| following_count | INT | DEFAULT 0 | 关注数 |
| created_at | TIMESTAMP | | 创建时间 |
| updated_at | TIMESTAMP | | 更新时间 |

#### user_follows - 关注关系表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| follower_id | BIGINT | PK, FK | 关注者ID |
| following_id | BIGINT | PK, FK | 被关注者ID |
| created_at | TIMESTAMP | | 关注时间 |

索引：`idx_following_id`, `idx_created_at`

---

### 2. 视频模块

#### videos - 视频基础信息表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 视频ID |
| user_id | BIGINT | FK | 作者ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| description | TEXT | NULL | 简介 |
| cover_url | VARCHAR(500) | NULL | 封面URL |
| video_url | VARCHAR(500) | NULL | 视频URL |
| duration | INT | NULL | 时长(秒) |
| file_size | BIGINT | NULL | 文件大小(字节) |
| status | ENUM | NOT NULL | UPLOADING/TRANSCODING/PUBLISHED/DELETED |
| updated_at | TIMESTAMP | | 更新时间 |
| published_at | TIMESTAMP | NULL | 发布时间 |

索引：`idx_user_id`, `idx_status`, `idx_published_at`

#### video_stats - 视频统计表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| video_id | BIGINT | PK, FK | 视频ID |
| views_count | BIGINT | DEFAULT 0 | 播放量 |
| likes_count | INT | DEFAULT 0 | 点赞数 |
| coins_count | INT | DEFAULT 0 | 投币数 |
| favorites_count | INT | DEFAULT 0 | 收藏数 |
| comments_count | INT | DEFAULT 0 | 评论数 |
| shares_count | INT | DEFAULT 0 | 分享数 |
| danmaku_count | INT | DEFAULT 0 | 弹幕数 |
| updated_at | TIMESTAMP | | 更新时间 |

设计说明：从 videos 表拆分，减少热点行锁冲突，提高并发性能。

---

### 3. 分类模块

#### categories - 分类表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 分类ID |
| parent_id | BIGINT | FK, NULL | 父分类ID |
| name | VARCHAR(50) | NOT NULL | 分类名称 |
| icon | VARCHAR(255) | NULL | 图标URL |
| sort_order | INT | DEFAULT 0 | 排序权重 |
| created_at | TIMESTAMP | | 创建时间 |

索引：`idx_parent_id`, `idx_sort_order`

支持多级分类结构（如：动画 > 番剧 > 连载动画）

#### video_categories - 视频分类关联表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| video_id | BIGINT | PK, FK | 视频ID |
| category_id | BIGINT | PK, FK | 分类ID |

索引：`idx_category_id`

---

### 4. 互动行为模块

#### video_likes - 点赞记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| user_id | BIGINT | PK, FK | 用户ID |
| video_id | BIGINT | PK, FK | 视频ID |
| created_at | TIMESTAMP | | 点赞时间 |

索引：`idx_video_id`, `idx_created_at`

#### video_coins - 投币记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 记录ID |
| user_id | BIGINT | FK | 用户ID |
| video_id | BIGINT | FK | 视频ID |
| amount | INT | DEFAULT 1 | 投币数量 |
| created_at | TIMESTAMP | | 投币时间 |

索引：`idx_user_video`, `idx_video_id`, `idx_created_at`

说明：支持单次投多币，同一用户可对同一视频多次投币。

#### video_favorites - 收藏记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| user_id | BIGINT | PK, FK | 用户ID |
| video_id | BIGINT | PK, FK | 视频ID |
| created_at | TIMESTAMP | | 收藏时间 |

索引：`idx_video_id`, `idx_created_at`

---

### 5. 观看历史模块

#### watch_histories - 观看历史表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 记录ID |
| user_id | BIGINT | FK | 用户ID |
| video_id | BIGINT | FK | 视频ID |
| progress | INT | DEFAULT 0 | 播放进度(秒) |
| duration | INT | DEFAULT 0 | 本次观看时长(秒) |
| watched_at | TIMESTAMP | | 最后观看时间 |

索引：`uk_user_video` (UNIQUE), `idx_video_id`, `idx_watched_at`

说明：同一用户对同一视频只保留一条记录，支持断点续播。

---

### 6. 评论模块

#### comments - 评论表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 评论ID |
| video_id | BIGINT | FK | 视频ID |
| user_id | BIGINT | FK | 用户ID |
| parent_id | BIGINT | FK, NULL | 父评论ID |
| root_id | BIGINT | FK, NULL | 根评论ID |
| content | TEXT | NOT NULL | 评论内容 |
| likes_count | INT | DEFAULT 0 | 点赞数 |
| reply_count | INT | DEFAULT 0 | 回复数 |
| status | ENUM | DEFAULT 'VISIBLE' | VISIBLE/HIDDEN/DELETED |
| created_at | TIMESTAMP | | 创建时间 |
| updated_at | TIMESTAMP | | 更新时间 |

索引：`idx_video_id`, `idx_user_id`, `idx_root_id`, `idx_created_at`

说明：
- `root_id` 指向一级评论，用于楼中楼分页查询
- `parent_id` 指向直接父评论，用于 @回复

---

### 7. 弹幕模块

#### danmaku - 弹幕表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 弹幕ID |
| video_id | BIGINT | FK | 视频ID |
| user_id | BIGINT | FK | 用户ID |
| content | VARCHAR(100) | NOT NULL | 弹幕内容 |
| send_time | DECIMAL(10,3) | NOT NULL | 视频时间点(秒) |
| mode | TINYINT | DEFAULT 1 | 1滚动/4底部/5顶部 |
| font_size | INT | DEFAULT 25 | 字体大小 |
| color | INT | DEFAULT 16777215 | RGB颜色值 |
| created_at | TIMESTAMP | | 发送时间 |

索引：`idx_video_time` (video_id, send_time), `idx_user_id`, `idx_created_at`

---

### 8. 上传任务模块

#### upload_tasks - 上传任务表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | 任务ID |
| video_id | BIGINT | FK, NULL | 关联视频ID |
| user_id | BIGINT | FK | 用户ID |
| original_filename | VARCHAR(255) | NOT NULL | 原始文件名 |
| file_path | VARCHAR(500) | NULL | 存储路径 |
| file_size | BIGINT | NULL | 文件大小 |
| status | ENUM | NOT NULL | PENDING/UPLOADING/TRANSCODING/COMPLETED/FAILED |
| progress | INT | DEFAULT 0 | 处理进度(%) |
| error_message | TEXT | NULL | 错误信息 |
| created_at | TIMESTAMP | | 创建时间 |
| updated_at | TIMESTAMP | | 更新时间 |
| completed_at | TIMESTAMP | NULL | 完成时间 |

索引：`idx_user_id`, `idx_status`, `idx_created_at`

---

## Flyway 迁移文件

| 版本 | 文件名 | 说明 |
|------|--------|------|
| V1 | Create_User_Tables.sql | 创建 users, profiles 表 |
| V2 | Create_Videos_Table.sql | 创建 videos 表 |
| V3 | Create_User_Follow_Table.sql | 创建 user_follows 表 |
| V4 | Create_Category_Tables.sql | 创建 categories, video_categories 表 |
| V5 | Create_Video_Stats_Table.sql | 创建 video_stats 表 |
| V6 | Alter_Videos_Remove_Stats.sql | 从 videos 移除统计字段 |
| V7 | Create_Like_Table.sql | 创建 video_likes 表 |
| V8 | Create_Coin_Table.sql | 创建 video_coins 表 |
| V9 | Create_Favorite_Table.sql | 创建 video_favorites 表 |
| V10 | Create_Watch_History_Table.sql | 创建 watch_histories 表 |
| V11 | Create_Comment_Table.sql | 创建 comments 表 |
| V12 | Create_Danmaku_Table.sql | 创建 danmaku 表 |
| V13 | Create_Upload_Task_Table.sql | 创建 upload_tasks 表 |

---

## 业务包结构

```
com.franklintju.streamlab
├── users/              # 用户账号
├── profiles/           # 用户档案
├── videos/             # 视频基础
├── follow/             # 关注关系
│   ├── UserFollow.java
│   ├── UserFollowId.java
│   └── UserFollowRepository.java
├── category/           # 分类
│   ├── Category.java
│   ├── VideoCategory.java
│   ├── VideoCategoryId.java
│   ├── CategoryRepository.java
│   └── VideoCategoryRepository.java
├── stats/              # 视频统计
│   ├── VideoStats.java
│   └── VideoStatsRepository.java
├── interaction/        # 互动行为
│   ├── VideoLike.java
│   ├── VideoLikeId.java
│   ├── VideoLikeRepository.java
│   ├── VideoCoin.java
│   ├── VideoCoinRepository.java
│   ├── VideoFavorite.java
│   ├── VideoFavoriteId.java
│   └── VideoFavoriteRepository.java
├── history/            # 观看历史
│   ├── WatchHistory.java
│   └── WatchHistoryRepository.java
├── comment/            # 评论
│   ├── Comment.java
│   └── CommentRepository.java
├── danmaku/            # 弹幕
│   ├── Danmaku.java
│   └── DanmakuRepository.java
└── upload/             # 上传任务
    ├── UploadTask.java
    └── UploadTaskRepository.java
```

---

## 索引设计说明

### 查询优化

| 表 | 索引 | 用途 |
|---|------|------|
| user_follows | idx_following_id | 查询某用户的粉丝列表 |
| video_categories | idx_category_id | 按分类筛选视频 |
| video_likes | idx_video_id | 统计视频点赞数 |
| watch_histories | uk_user_video | 更新观看进度时快速定位 |
| comments | idx_root_id | 分页加载楼中楼回复 |
| danmaku | idx_video_time | 按时间段加载弹幕 |
| upload_tasks | idx_status | 查询待处理任务队列 |

### 写入优化

- video_stats 独立存储计数，避免 videos 表行锁冲突
- 行为表使用联合主键，插入时自动去重
- 弹幕表使用 (video_id, send_time) 复合索引支持范围查询

---

## 数据完整性

所有外键均设置 `ON DELETE CASCADE`，删除用户或视频时自动级联删除关联数据。

categories 的 parent_id 外键设置 `ON DELETE SET NULL`，删除父分类时子分类变为顶级分类。

