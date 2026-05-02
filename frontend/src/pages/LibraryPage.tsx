import { HistoryRail } from "../components/HistoryRail";
import { VideoCard } from "../components/VideoCard";
import type { User, Video, WatchHistoryItem } from "../types/api";

interface LibraryPageProps {
  user: User | null;
  history: WatchHistoryItem[];
  videos: Video[];
  loading: boolean;
  onOpenLogin: () => void;
  onOpenStudio: () => void;
  onOpenVideo: (id: number) => void;
}

export function LibraryPage({
  user,
  history,
  videos,
  loading,
  onOpenLogin,
  onOpenStudio,
  onOpenVideo
}: LibraryPageProps) {
  if (!user) {
    return (
      <div className="empty-state card-surface">
        <h3>Library stays personal</h3>
        <p>Sign in to unlock continue watching, your own uploads, and a cleaner daily flow.</p>
        <div className="inline-actions">
          <button className="primary-button" onClick={onOpenLogin}>
            Sign in
          </button>
          <button className="ghost-button" onClick={onOpenStudio}>
            Open Studio first
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <section className="section-header">
        <div>
          <div className="eyebrow">Continue watching</div>
          <h2>Your recent flow, ready to resume</h2>
        </div>
      </section>

      <HistoryRail items={history} onOpenVideo={onOpenVideo} />

      <section className="section-header">
        <div>
          <div className="eyebrow">Your uploads</div>
          <h2>Everything currently tied to your account</h2>
        </div>
      </section>

      {loading ? (
        <div className="video-grid">
          {Array.from({ length: 3 }).map((_, index) => (
            <div className="video-skeleton card-surface" key={index} />
          ))}
        </div>
      ) : videos.length === 0 ? (
        <div className="empty-state card-surface">
          <h3>No uploads yet</h3>
          <p>Create your first draft in Studio and this space will start feeling like your own app.</p>
        </div>
      ) : (
        <div className="video-grid">
          {videos.map((video) => (
            <VideoCard key={video.id} video={video} onOpen={onOpenVideo} />
          ))}
        </div>
      )}
    </div>
  );
}
