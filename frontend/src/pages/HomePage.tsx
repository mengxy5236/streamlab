import type { Video } from "../types/api";
import { Hero } from "../components/Hero";
import { VideoCard } from "../components/VideoCard";

interface HomePageProps {
  videos: Video[];
  loading: boolean;
  error: string | null;
  onOpenVideo: (id: number) => void;
}

export function HomePage({ videos, loading, error, onOpenVideo }: HomePageProps) {
  return (
    <div className="page-stack">
      <Hero videoCount={videos.length} />

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
    </div>
  );
}
