# StreamLab

StreamLab is a Spring Boot based video platform prototype inspired by Bilibili.

## Features

- User authentication and profile management
- Video metadata CRUD
- Video upload with OSS storage
- HLS transcoding with FFmpeg
- Asynchronous transcoding with Kafka
- Comments, likes, follows, danmaku, and watch history
- Redis based hot data and scheduled stats sync

## Architecture

- Spring Boot monolith
- MySQL + Flyway
- Redis cache and counters
- Kafka used only for video transcoding
- OSS and FFmpeg integration for media processing
