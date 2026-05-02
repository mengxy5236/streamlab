# OSS, FFmpeg, and HLS Guide

This document explains the media pipeline used by StreamLab.

## 1. Upload Flow

1. The client creates a draft video through `/api/videos`.
2. The client uploads the original source file through `/api/upload/{videoId}`.
3. The backend uploads the source file to OSS and creates an `upload_tasks` record.
4. The backend sends a RabbitMQ message for asynchronous transcoding.
5. `TranscodeConsumer` receives the task and starts HLS conversion.
6. After conversion, HLS artifacts are uploaded to OSS and the `videos` record is updated.

## 2. Why OSS

OSS is used to store:

- original uploaded video files
- generated HLS playlists and segment files
- cover images

This keeps large files outside the application server and makes them easier to serve through public URLs or a CDN.

## 3. Why FFmpeg

FFmpeg is responsible for:

- probing media metadata
- converting source video into HLS format
- generating `.m3u8` playlists and `.ts` segments

The project also uses `ffprobe` to extract duration and related media metadata.

## 4. Why HLS

HLS is used because:

- it is broadly supported by modern video players
- segmented media is easier to deliver over HTTP
- it works well with OSS/CDN based delivery

Relevant video fields include:

- `videoUrl`: original uploaded source file
- `hlsUrl`: generated HLS playlist URL
- `hlsReady`: whether HLS output is ready
- `duration`, `resolution`, `bitrate`: metadata from the transcode result

## 5. Current Implementation Notes

- RabbitMQ is now used only for video transcoding.
- Likes, comment likes, and view counters no longer use a message queue.
- Playback counters are buffered in Redis and synced to MySQL on a schedule.
- The transcode task supports retries through `TranscodeMessage`.

## 6. Key Classes

- `upload/UploadService`
- `upload/TranscodeProducer`
- `upload/TranscodeConsumer`
- `upload/HlsService`
- `upload/UploadTask`
- `videos/Video`

## 7. Future Improvements

- multi-bitrate HLS output
- better transcode progress reporting
- OSS/CDN access policy tuning
- stronger operational monitoring for long-running transcode jobs
