import { useEffect, useRef } from "react";
import { CommentList } from "../components/CommentList";
import { VideoCard } from "../components/VideoCard";
import { formatDuration, formatRelativeDate } from "../lib/format";
import type { Comment, Video } from "../types/api";

interface VideoPageProps {
  video: Video | null;
  comments: Comment[];
  relatedVideos: Video[];
  loading: boolean;
  commentsLoading: boolean;
  error: string | null;
  resumeSeconds: number;
  onBack: () => void;
  onOpenVideo: (id: number) => void;
  onRecordProgress: (progress: number, duration: number) => void;
}

export function VideoPage({
  video,
  comments,
  relatedVideos,
  loading,
  commentsLoading,
  error,
  resumeSeconds,
  onBack,
  onOpenVideo,
  onRecordProgress
}: VideoPageProps) {
  const videoRef = useRef<HTMLVideoElement | null>(null);

  useEffect(() => {
    if (!videoRef.current || !resumeSeconds) {
      return;
    }
    const player = videoRef.current;
    const handleLoaded = () => {
      if (resumeSeconds > 0 && player.currentTime < 1) {
        player.currentTime = resumeSeconds;
      }
    };
    player.addEventListener("loadedmetadata", handleLoaded);
    return () => player.removeEventListener("loadedmetadata", handleLoaded);
  }, [resumeSeconds, video?.id]);

  if (loading) {
    return <div className="detail-skeleton card-surface" />;
  }

  if (!video || error) {
    return (
      <div className="empty-state card-surface">
        <h3>Video unavailable</h3>
        <p>{error || "The selected video could not be loaded."}</p>
        <button className="primary-button" onClick={onBack}>
          Back home
        </button>
      </div>
    );
  }

  const playbackUrl = video.hlsUrl || video.videoUrl || "";

  return (
    <div className="page-stack">
      <button className="ghost-button back-button" onClick={onBack}>
        Back
      </button>

      <section className="detail-layout">
        <div className="player-card card-surface">
          <div className="player-stage">
            {playbackUrl ? (
              <video
                ref={videoRef}
                controls
                playsInline
                preload="metadata"
                src={playbackUrl}
                poster={video.coverUrl || undefined}
                onPause={(event) =>
                  onRecordProgress(
                    Math.floor(event.currentTarget.currentTime || 0),
                    Math.floor(event.currentTarget.duration || 0)
                  )
                }
                onEnded={(event) =>
                  onRecordProgress(
                    Math.floor(event.currentTarget.duration || 0),
                    Math.floor(event.currentTarget.duration || 0)
                  )
                }
              />
            ) : (
              <div className="player-placeholder">
                <span>Playback source not ready yet</span>
              </div>
            )}
          </div>

          <div className="player-meta">
            <div className="eyebrow">Video story</div>
            <h1>{video.title}</h1>
            <p>{video.description || "No description provided for this video yet."}</p>
            <div className="player-stat-row">
              <span>{video.hlsReady ? "Streaming ready" : "Processing or source-only"}</span>
              <span>{formatDuration(video.duration)}</span>
              <span>{formatRelativeDate(video.publishedAt ?? video.updatedAt)}</span>
              {resumeSeconds > 0 ? <span>Resume at {formatDuration(resumeSeconds)}</span> : null}
            </div>
          </div>
        </div>

        <aside className="detail-sidebar">
          <div className="info-panel card-surface">
            <div className="eyebrow">Technical finish</div>
            <ul>
              <li>Resolution: {video.resolution || "Unknown"}</li>
              <li>Bitrate: {video.bitrate ? `${video.bitrate} kbps` : "Unknown"}</li>
              <li>Pipeline: {video.hlsReady ? "HLS transcoded" : "Draft or source file"}</li>
            </ul>
          </div>

          <div className="info-panel card-surface accent-panel">
            <div className="eyebrow">Why it feels better</div>
            <p>
              The page is built to feel more like your own private watchroom than a loud public feed:
              large player, quiet spacing, and just enough red to keep it alive.
            </p>
          </div>
        </aside>
      </section>

      <section className="section-header">
        <div>
          <div className="eyebrow">Discussion</div>
          <h2>Comments around the video</h2>
        </div>
      </section>

      {commentsLoading ? (
        <div className="comment-list">
          {Array.from({ length: 3 }).map((_, index) => (
            <div className="comment-skeleton card-surface" key={index} />
          ))}
        </div>
      ) : (
        <CommentList comments={comments} />
      )}

      <section className="section-header">
        <div>
          <div className="eyebrow">More to open</div>
          <h2>Related surfaces from your current library</h2>
        </div>
      </section>

      <div className="video-grid compact-grid">
        {relatedVideos.map((item) => (
          <VideoCard key={item.id} video={item} onOpen={onOpenVideo} />
        ))}
      </div>
    </div>
  );
}
