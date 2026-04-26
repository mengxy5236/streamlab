interface HeroProps {
  videoCount: number;
}

export function Hero({ videoCount }: HeroProps) {
  return (
    <section className="hero-grid">
      <div className="hero-copy card-surface hero-card">
        <div className="eyebrow">Creator-ready video backend</div>
        <h1>Minimal luxury on the surface. Quiet engineering underneath.</h1>
        <p>
          A calm, cinema-inspired interface for exploring your Spring Boot video platform.
          Soft glass, restrained red accents, and buttery transitions keep the focus on content.
        </p>
        <div className="hero-stats">
          <div>
            <strong>{videoCount}</strong>
            <span>Published videos</span>
          </div>
          <div>
            <strong>HLS</strong>
            <span>Streaming-ready pipeline</span>
          </div>
          <div>
            <strong>JWT</strong>
            <span>Typed auth flow</span>
          </div>
        </div>
      </div>

      <div className="hero-panel card-surface">
        <div className="hero-preview">
          <div className="preview-bezel">
            <div className="preview-screen">
              <div className="preview-badge">Featured Interface</div>
              <div className="preview-line preview-line-large" />
              <div className="preview-line" />
              <div className="preview-line preview-line-short" />
              <div className="preview-thumbs">
                <div />
                <div />
                <div />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
