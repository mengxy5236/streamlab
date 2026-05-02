# Database Design

StreamLab uses MySQL with Flyway migrations under `src/main/resources/db/migration`.

## Core Tables

### Users

- `users`: account identity, password hash, role, status
- `profiles`: public profile, avatar, bio, follower/following counts
- `user_follows`: follow relationship between users

### Videos

- `videos`: title, description, cover, source video URL, HLS metadata, status, counters
- `upload_tasks`: upload and transcode task state
- `categories`: category hierarchy
- `video_categories`: many-to-many mapping between videos and categories

### Interactions

- `video_likes`: who liked which video
- `comments`: tree-structured comments with `parent_id` and `root_id`
- `comment_likes`: who liked which comment
- `watch_histories`: last watch progress per user and video

## Notes

- Counters such as views and likes are buffered in Redis and synced back to MySQL by scheduled jobs.
- Follow counts are denormalized on `profiles` for faster reads.
- Comment threads use `root_id` for paginated reply loading.
- HLS-related fields live on `videos`, including `hls_url`, `hls_ready`, `duration`, `resolution`, and `bitrate`.

## Suggested Reading

- Flyway migrations: `src/main/resources/db/migration`
- Redis counter sync: `src/main/java/com/franklintju/streamlab/common/StatsSyncScheduler.java`
- Upload and transcode flow: `docs/OSS_FFmpeg_HLS_Guide.md`
