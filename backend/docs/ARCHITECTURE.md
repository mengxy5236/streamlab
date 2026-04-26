# Architecture Overview

StreamLab is currently a Spring Boot monolith focused on backend practice rather than service splitting.

## Main Stack

- Spring Boot 3
- Java 21
- MySQL + Flyway
- Spring Security + JWT
- JPA + MyBatis
- Redis
- Kafka for video transcoding only
- Aliyun OSS
- FFmpeg / ffprobe

## Functional Modules

- `auth`: login, refresh token, JWT parsing
- `users`: account, profile, password change
- `videos`: video metadata, publishing, playback access
- `upload`: upload task lifecycle and HLS transcoding
- `interaction`: video likes
- `comment`: comments and comment likes
- `follow`: follower/following graph
- `history`: watch progress and history
- `danmaku`: danmaku persistence and websocket delivery
- `common`: exception handling, locks, rate limiting, scheduled sync

## Runtime Flow

- Request/response business logic is mostly synchronous.
- Video transcoding is the only Kafka-backed asynchronous pipeline.
- Redis is used for counters, caching, progress state, and lightweight distributed coordination.
- Scheduled jobs sync buffered counters from Redis back into MySQL.

## Repository Layout

- `src/main/java`: application code
- `src/main/resources`: configuration, mappers, migrations
- `src/test/java`: tests
- `docs`: project documentation
