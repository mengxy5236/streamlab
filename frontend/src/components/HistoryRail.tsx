import { formatDuration, formatRelativeDate } from "../lib/format";
import type { WatchHistoryItem } from "../types/api";

interface HistoryRailProps {
  items: WatchHistoryItem[];
  onOpenVideo: (id: number) => void;
}

export function HistoryRail({ items, onOpenVideo }: HistoryRailProps) {
  if (items.length === 0) {
    return (
      <div className="empty-state card-surface">
        <h3>No watch history yet</h3>
        <p>Once you watch signed-in, your continue-watching rail will live here.</p>
      </div>
    );
  }

  return (
    <div className="history-rail">
      {items.map((item) => {
        const progress = item.progress ?? 0;
        const duration = item.duration ?? 0;
        const ratio = duration > 0 ? Math.min(progress / duration, 1) : 0;

        return (
          <button
            className="history-card card-surface"
            key={item.id}
            onClick={() => onOpenVideo(item.videoId)}
          >
            <img
              className="history-cover"
              src={item.videoCoverUrl || "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80"}
              alt={item.videoTitle}
            />
            <div className="history-copy">
              <strong>{item.videoTitle}</strong>
              <span>{formatRelativeDate(item.watchedAt)}</span>
              <div className="history-progress">
                <div className="history-progress-fill" style={{ width: `${ratio * 100}%` }} />
              </div>
              <small>
                {formatDuration(progress)} / {formatDuration(duration)}
              </small>
            </div>
          </button>
        );
      })}
    </div>
  );
}
