interface HeroProps {
  videoCount: number;
  personalVideoCount: number;
  historyCount: number;
  onGoStudio: () => void;
}

export function Hero({ videoCount, personalVideoCount, historyCount, onGoStudio }: HeroProps) {
  return (
    <section className="hero-grid">
      <div className="hero-copy card-surface hero-card">
        <div className="eyebrow">Personal video space</div>
        <h1>Quiet on the eyes. Ready for your own uploads.</h1>
        <p>
          StreamLab now leans into a daily-use shape: browse what is public, jump back into your
          watch history, and push new drafts through a polished creator flow.
        </p>
        <div className="hero-actions">
          <button className="primary-button" onClick={onGoStudio}>
            Open Studio
          </button>
          <button className="ghost-button">Keep the mood minimal</button>
        </div>
        <div className="hero-stats">
          <div>
            <strong>{videoCount}</strong>
            <span>Public videos</span>
          </div>
          <div>
            <strong>{personalVideoCount}</strong>
            <span>Your uploads</span>
          </div>
          <div>
            <strong>{historyCount}</strong>
            <span>Resume-ready items</span>
          </div>
        </div>
      </div>

      <div className="hero-panel card-surface">
        <div className="hero-preview">
          <div className="preview-bezel">
            <div className="preview-screen">
              <div className="preview-badge">Studio Snapshot</div>
              <div className="preview-pane">
                <div className="preview-column">
                  <div className="preview-line preview-line-large" />
                  <div className="preview-line" />
                  <div className="preview-line preview-line-short" />
                </div>
                <div className="preview-card-grid">
                  <div />
                  <div />
                  <div />
                  <div />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
