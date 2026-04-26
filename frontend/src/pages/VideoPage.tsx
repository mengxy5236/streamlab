import type { Comment, Video } from "../types/api";
import { formatDuration, formatRelativeDate } from "../lib/format";
import { CommentList } from "../components/CommentList";

interface VideoPageProps {
  video: Video | null;
  comments: Comment[];
  loading: boolean;
  commentsLoading: boolean;
  error: string | null;
  onBack: () => void;
}

export function VideoPage({
  video,
  comments,
  loading,
  commentsLoading,
  error,
  onBack
}: VideoPageProps) {
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
              video.hlsUrl ? (
                <video controls playsInline preload="metadata" src={playbackUrl} />
              ) : (
                <video controls playsInline preload="metadata" src={playbackUrl} />
              )
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
            <div className="eyebrow">Tone</div>
            <p>
              The interface keeps the iOS calmness in spacing and motion, while the restrained red
              signal gives it a creator-platform pulse.
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
    </div>
  );
}
