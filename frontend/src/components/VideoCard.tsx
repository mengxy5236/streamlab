import type { Video } from "../types/api";
import { formatDuration, formatRelativeDate } from "../lib/format";

interface VideoCardProps {
  video: Video;
  onOpen: (id: number) => void;
}

const fallbackCover =
  "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80";

export function VideoCard({ video, onOpen }: VideoCardProps) {
  return (
    <button className="video-card" onClick={() => onOpen(video.id)}>
      <div className="video-cover-wrap">
        <img
          className="video-cover"
          src={video.coverUrl || fallbackCover}
          alt={video.title}
          loading="lazy"
        />
        <div className="video-duration">{formatDuration(video.duration)}</div>
        <div className="video-gloss" />
      </div>

      <div className="video-meta">
        <div className="video-badges">
          <span>{video.hlsReady ? "HLS Ready" : "Source Only"}</span>
          {video.resolution ? <span>{video.resolution}</span> : null}
        </div>
        <h3>{video.title}</h3>
        <p>{video.description || "A focused creator-facing upload flowing through the StreamLab pipeline."}</p>
        <div className="video-footer">
          <span>{formatRelativeDate(video.publishedAt ?? video.updatedAt)}</span>
          <span>Open story</span>
        </div>
      </div>
    </button>
  );
}
