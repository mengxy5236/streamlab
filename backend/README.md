# StreamLab Backend

StreamLab backend is a Spring Boot prototype for a video platform inspired by Bilibili. It covers authentication, video metadata management, upload and transcoding, comments, likes, follows, watch history, danmaku, Redis counters, and scheduled data synchronization.

## Highlights

- Spring Boot 3 + Java 21
- JWT based authentication and authorization
- MySQL persistence with Flyway migrations
- Redis for counters, cache, progress, and lightweight coordination
- OSS based media storage
- FFmpeg based HLS transcoding
- Kafka used only for asynchronous video transcoding

## Current Scope

Implemented modules:

- user registration, login, refresh token, and profile management
- video draft creation, update, publish, and listing
- source video upload and cover upload
- Kafka-driven transcode task handling
- HLS metadata persistence
- video likes and comment likes
- follow system
- watch history and playback progress
- danmaku persistence and websocket support

## Project Structure

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/franklintju/streamlab/
│   │   │   ├── auth/
│   │   │   ├── comment/
│   │   │   ├── common/
│   │   │   ├── config/
│   │   │   ├── danmaku/
│   │   │   ├── follow/
│   │   │   ├── history/
│   │   │   ├── interaction/
│   │   │   ├── oss/
│   │   │   ├── upload/
│   │   │   ├── users/
│   │   │   └── videos/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/migration/
│   │       └── mapper/
│   └── test/
├── docs/
│   ├── ARCHITECTURE.md
│   ├── API_REQUEST_EXAMPLES.md
│   ├── DATABASE_DESIGN.md
│   └── OSS_FFmpeg_HLS_Guide.md
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## Quick Start

### 1. Environment

Prepare these environment variables before startup:

```text
DB_USERNAME
DB_PASSWORD
JWT_SECRET
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
OSS_ACCESS_KEY_ID
OSS_ACCESS_KEY_SECRET
KAFKA_BOOTSTRAP_SERVERS
```

### 2. Build

```bash
./mvnw clean package
```

Windows:

```powershell
.\mvnw.cmd clean package
```

### 3. Run

```bash
./mvnw spring-boot:run
```

## Runtime Notes

- Redis counters for views, likes, and danmaku are synced back into MySQL by scheduled jobs.
- Upload task lookup is authenticated and owner-checked.
- Password change is restricted to the current user.
- Likes, comment likes, and view count updates no longer depend on Kafka.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [API Examples](docs/API_REQUEST_EXAMPLES.md)
- [Database Design](docs/DATABASE_DESIGN.md)
- [OSS / FFmpeg / HLS Guide](docs/OSS_FFmpeg_HLS_Guide.md)
