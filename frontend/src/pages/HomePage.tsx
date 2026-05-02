import { Hero } from "../components/Hero";
import { HistoryRail } from "../components/HistoryRail";
import { VideoCard } from "../components/VideoCard";
import type { User, Video, WatchHistoryItem } from "../types/api";

interface HomePageProps {
  user: User | null;
  videos: Video[];
  myVideos: Video[];
  history: WatchHistoryItem[];
  loading: boolean;
  error: string | null;
  onOpenVideo: (id: number) => void;
  onGoStudio: () => void;
}

export function HomePage({
  user,
  videos,
  myVideos,
  history,
  loading,
  error,
  onOpenVideo,
  onGoStudio
}: HomePageProps) {
  const personalHighlights = myVideos.slice(0, 3);

  return (
    <div className="page-stack">
      <Hero
        videoCount={videos.length}
        personalVideoCount={myVideos.length}
        historyCount={history.length}
        onGoStudio={onGoStudio}
      />

      {user ? (
        <>
          <section className="section-header">
            <div>
              <div className="eyebrow">Continue watching</div>
              <h2>Jump back into what you touched recently</h2>
            </div>
          </section>
          <HistoryRail items={history.slice(0, 4)} onOpenVideo={onOpenVideo} />
        </>
      ) : null}

      <section className="section-header">
        <div>
          <div className="eyebrow">Public videos</div>
          <h2>Recent uploads in a calm, cinematic grid</h2>
        </div>
      </section>

      {loading ? (
        <div className="video-grid">
          {Array.from({ length: 6 }).map((_, index) => (
            <div className="video-skeleton card-surface" key={index} />
          ))}
        </div>
      ) : error ? (
        <div className="empty-state card-surface">
          <h3>Unable to load videos</h3>
          <p>{error}</p>
        </div>
      ) : (
        <div className="video-grid">
          {videos.map((video) => (
            <VideoCard key={video.id} video={video} onOpen={onOpenVideo} />
          ))}
        </div>
      )}

      {user ? (
        <>
          <section className="section-header">
            <div>
              <div className="eyebrow">Your recent uploads</div>
              <h2>Fast access to what you are actively shaping</h2>
            </div>
          </section>
          {personalHighlights.length === 0 ? (
            <div className="empty-state card-surface">
              <h3>Your library is still quiet</h3>
              <p>Create a draft in Studio and your personal row will start filling up here.</p>
            </div>
          ) : (
            <div className="video-grid compact-grid">
              {personalHighlights.map((video) => (
                <VideoCard key={video.id} video={video} onOpen={onOpenVideo} />
              ))}
            </div>
          )}
        </>
      ) : null}
    </div>
  );
}
