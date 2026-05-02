import { useEffect, useState } from "react";
import { formatRelativeDate } from "../lib/format";
import type { UploadTask, User, Video } from "../types/api";

interface StudioPageProps {
  user: User | null;
  videos: Video[];
  loading: boolean;
  task: UploadTask | null;
  taskBusy: boolean;
  draftBusy: boolean;
  publishBusyId: number | null;
  onOpenLogin: () => void;
  onOpenVideo: (id: number) => void;
  onCreateDraft: (title: string, description: string) => Promise<void>;
  onUploadVideo: (videoId: number, file: File) => Promise<void>;
  onUploadCover: (videoId: number, file: File) => Promise<void>;
  onPublishVideo: (videoId: number) => Promise<void>;
}

export function StudioPage({
  user,
  videos,
  loading,
  task,
  taskBusy,
  draftBusy,
  publishBusyId,
  onOpenLogin,
  onOpenVideo,
  onCreateDraft,
  onUploadVideo,
  onUploadCover,
  onPublishVideo
}: StudioPageProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!draftBusy) {
      return;
    }
    setError(null);
  }, [draftBusy]);

  if (!user) {
    return (
      <div className="empty-state card-surface">
        <h3>Studio needs your account</h3>
        <p>Sign in first and you can create drafts, upload covers, push videos, and monitor tasks.</p>
        <button className="primary-button" onClick={onOpenLogin}>
          Sign in
        </button>
      </div>
    );
  }

  return (
    <div className="page-stack">
      <section className="studio-layout">
        <div className="studio-panel card-surface">
          <div className="section-header section-header-tight">
            <div>
              <div className="eyebrow">New draft</div>
              <h2>Start something simple</h2>
            </div>
          </div>

          <form
            className="studio-form"
            onSubmit={async (event) => {
              event.preventDefault();
              try {
                await onCreateDraft(title, description);
                setTitle("");
                setDescription("");
              } catch (submissionError) {
                setError(submissionError instanceof Error ? submissionError.message : "Failed to create draft");
              }
            }}
          >
            <label>
              <span>Title</span>
              <input
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                placeholder="My late-night city walkthrough"
                required
              />
            </label>

            <label>
              <span>Description</span>
              <textarea
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                placeholder="A short note about what this upload is for."
                rows={5}
              />
            </label>

            {error ? <div className="error-banner">{error}</div> : null}

            <button className="primary-button" disabled={draftBusy}>
              {draftBusy ? "Creating..." : "Create draft"}
            </button>
          </form>
        </div>

        <div className="studio-panel card-surface task-panel">
          <div className="section-header section-header-tight">
            <div>
              <div className="eyebrow">Current task</div>
              <h2>Upload and transcode status</h2>
            </div>
          </div>

          {task ? (
            <div className="task-card">
              <strong>Video #{task.videoId}</strong>
              <span>{task.status}</span>
              <div className="task-progress">
                <div
                  className="task-progress-fill"
                  style={{ width: `${Math.max(6, task.progress ?? 0)}%` }}
                />
              </div>
              <small>{task.progress ?? 0}% complete</small>
              {task.errorMessage ? <p>{task.errorMessage}</p> : null}
            </div>
          ) : (
            <div className="empty-task">
              <p>Your next upload task will appear here with live progress.</p>
            </div>
          )}

          <div className="status-note">
            <span className={taskBusy ? "status-dot is-live" : "status-dot"} />
            <span>{taskBusy ? "Polling active task" : "No active upload polling right now"}</span>
          </div>
        </div>
      </section>

      <section className="section-header">
        <div>
          <div className="eyebrow">Studio videos</div>
          <h2>Drafts, source uploads, and publish actions</h2>
        </div>
      </section>

      {loading ? (
        <div className="studio-list">
          {Array.from({ length: 3 }).map((_, index) => (
            <div className="video-skeleton card-surface" key={index} />
          ))}
        </div>
      ) : videos.length === 0 ? (
        <div className="empty-state card-surface">
          <h3>No drafts yet</h3>
          <p>Create one above and this studio will immediately become useful.</p>
        </div>
      ) : (
        <div className="studio-list">
          {videos.map((video) => (
            <article className="studio-video card-surface" key={video.id}>
              <div className="studio-video-copy">
                <div className="video-badges">
                  <span>{video.publishedAt ? "Published" : "Draft"}</span>
                  <span>{video.hlsReady ? "Streaming ready" : "Awaiting source or transcode"}</span>
                </div>
                <h3>{video.title}</h3>
                <p>{video.description || "No description yet. Keep it minimal or refine it later."}</p>
                <small>{formatRelativeDate(video.updatedAt)}</small>
              </div>

              <div className="studio-actions">
                <label className="upload-pill">
                  <input
                    type="file"
                    accept="image/png,image/jpeg,image/webp,image/gif"
                    onChange={async (event) => {
                      const file = event.target.files?.[0];
                      if (file) {
                        await onUploadCover(video.id, file);
                      }
                      event.currentTarget.value = "";
                    }}
                  />
                  Upload cover
                </label>

                <label className="upload-pill">
                  <input
                    type="file"
                    accept="video/mp4,video/avi,video/quicktime,video/x-matroska,video/webm"
                    onChange={async (event) => {
                      const file = event.target.files?.[0];
                      if (file) {
                        await onUploadVideo(video.id, file);
                      }
                      event.currentTarget.value = "";
                    }}
                  />
                  Upload video
                </label>

                <button
                  className="ghost-button"
                  disabled={publishBusyId === video.id}
                  onClick={() => onPublishVideo(video.id)}
                >
                  {publishBusyId === video.id ? "Publishing..." : "Publish"}
                </button>

                <button className="primary-button" onClick={() => onOpenVideo(video.id)}>
                  Open video
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
