import { startTransition, useEffect, useMemo, useState } from "react";
import { fetchCurrentUser, login, logout } from "./api/auth";
import { HttpError, getAccessToken } from "./api/client";
import { fetchHistory, fetchVideoProgress, recordVideoProgress } from "./api/history";
import { fetchUploadTask, uploadCoverFile, uploadVideoFile } from "./api/upload";
import {
  createVideo,
  fetchComments,
  fetchUserVideos,
  fetchVideo,
  fetchVideos,
  incrementView,
  publishVideo
} from "./api/videos";
import { LoginSheet } from "./components/LoginSheet";
import { Shell } from "./components/Shell";
import { Topbar } from "./components/Topbar";
import { HomePage } from "./pages/HomePage";
import { LibraryPage } from "./pages/LibraryPage";
import { StudioPage } from "./pages/StudioPage";
import { VideoPage } from "./pages/VideoPage";
import type { Comment, UploadTask, User, Video, WatchHistoryItem } from "./types/api";

type RouteState =
  | { kind: "home" }
  | { kind: "library" }
  | { kind: "studio" }
  | { kind: "video"; id: number };

function deriveRoute(): RouteState {
  const path = window.location.pathname.replace(/\/+$/, "") || "/";
  const videoMatch = path.match(/^\/video\/(\d+)$/);
  if (videoMatch) {
    return { kind: "video", id: Number(videoMatch[1]) };
  }
  if (path === "/library") {
    return { kind: "library" };
  }
  if (path === "/studio") {
    return { kind: "studio" };
  }
  return { kind: "home" };
}

export default function App() {
  const [route, setRoute] = useState<RouteState>(() => deriveRoute());

  const [videos, setVideos] = useState<Video[]>([]);
  const [videosLoading, setVideosLoading] = useState(true);
  const [videosError, setVideosError] = useState<string | null>(null);

  const [selectedVideo, setSelectedVideo] = useState<Video | null>(null);
  const [relatedVideos, setRelatedVideos] = useState<Video[]>([]);
  const [videoLoading, setVideoLoading] = useState(false);
  const [videoError, setVideoError] = useState<string | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [resumeSeconds, setResumeSeconds] = useState(0);

  const [user, setUser] = useState<User | null>(null);
  const [userVideos, setUserVideos] = useState<Video[]>([]);
  const [userDataLoading, setUserDataLoading] = useState(false);
  const [history, setHistory] = useState<WatchHistoryItem[]>([]);

  const [loginOpen, setLoginOpen] = useState(false);
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

  const [task, setTask] = useState<UploadTask | null>(null);
  const [taskPollingId, setTaskPollingId] = useState<number | null>(null);
  const [taskBusy, setTaskBusy] = useState(false);
  const [draftBusy, setDraftBusy] = useState(false);
  const [publishBusyId, setPublishBusyId] = useState<number | null>(null);

  useEffect(() => {
    const syncRoute = () => setRoute(deriveRoute());
    window.addEventListener("popstate", syncRoute);
    return () => window.removeEventListener("popstate", syncRoute);
  }, []);

  useEffect(() => {
    let active = true;

    async function loadVideos() {
      try {
        setVideosLoading(true);
        const page = await fetchVideos();
        if (!active) {
          return;
        }
        setVideos(page.content);
        setVideosError(null);
      } catch (error) {
        if (!active) {
          return;
        }
        setVideosError(error instanceof Error ? error.message : "Failed to load videos");
      } finally {
        if (active) {
          setVideosLoading(false);
        }
      }
    }

    loadVideos();
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!getAccessToken()) {
      return;
    }

    fetchCurrentUser()
      .then(setUser)
      .catch(() => setUser(null));
  }, []);

  useEffect(() => {
    if (!user) {
      setUserVideos([]);
      setHistory([]);
      return;
    }

    let active = true;

    async function loadPersonalData() {
      const currentUser = user;
      if (!currentUser) {
        return;
      }
      try {
        setUserDataLoading(true);
        const [videosResult, historyPage] = await Promise.all([
          fetchUserVideos(currentUser.id),
          fetchHistory()
        ]);
        if (!active) {
          return;
        }
        setUserVideos(videosResult);
        setHistory(historyPage.content);
      } finally {
        if (active) {
          setUserDataLoading(false);
        }
      }
    }

    loadPersonalData();
    return () => {
      active = false;
    };
  }, [user]);

  useEffect(() => {
    if (taskPollingId == null) {
      return;
    }

    setTaskBusy(true);
    const timer = window.setInterval(async () => {
      try {
        const nextTask = await fetchUploadTask(taskPollingId);
        setTask(nextTask);
        if (nextTask.status === "SUCCESS" || nextTask.status === "FAILED") {
          window.clearInterval(timer);
          setTaskPollingId(null);
          setTaskBusy(false);
          const [videoPage, personalVideos] = await Promise.all([
            fetchVideos(),
            user ? fetchUserVideos(user.id) : Promise.resolve([])
          ]);
          setVideos(videoPage.content);
          setUserVideos(personalVideos);
        }
      } catch {
        window.clearInterval(timer);
        setTaskPollingId(null);
        setTaskBusy(false);
      }
    }, 2200);

    return () => window.clearInterval(timer);
  }, [taskPollingId, user]);

  useEffect(() => {
    if (route.kind !== "video") {
      setSelectedVideo(null);
      setComments([]);
      setResumeSeconds(0);
      setVideoError(null);
      return;
    }

    const videoId = route.id;
    let active = true;

    async function loadDetail() {
      try {
        setVideoLoading(true);
        setCommentsLoading(true);

        const [video, page, progress] = await Promise.all([
          fetchVideo(videoId),
          fetchComments(videoId),
          user ? fetchVideoProgress(videoId).catch(() => null) : Promise.resolve(null)
        ]);

        if (!active) {
          return;
        }

        setSelectedVideo(video);
        setComments(page.content);
        setResumeSeconds(progress?.progress ?? 0);
        setRelatedVideos(videos.filter((item) => item.id !== videoId).slice(0, 3));
        setVideoError(null);
        incrementView(videoId).catch(() => undefined);
      } catch (error) {
        if (!active) {
          return;
        }
        setVideoError(error instanceof Error ? error.message : "Failed to load video");
      } finally {
        if (active) {
          setVideoLoading(false);
          setCommentsLoading(false);
        }
      }
    }

    loadDetail();
    return () => {
      active = false;
    };
  }, [route, user, videos]);

  const activeNav = route.kind === "video" ? "home" : route.kind;

  const topbar = useMemo(
    () => (
      <Topbar
        user={user}
        activeNav={activeNav}
        onOpenLogin={() => setLoginOpen(true)}
        onGoHome={() => navigate("/", { kind: "home" })}
        onGoLibrary={() => navigate("/library", { kind: "library" })}
        onGoStudio={() => navigate("/studio", { kind: "studio" })}
        onLogout={async () => {
          await logout().catch(() => undefined);
          setUser(null);
          navigate("/", { kind: "home" });
        }}
      />
    ),
    [activeNav, user]
  );

  function navigate(path: string, nextRoute: RouteState) {
    window.history.pushState({}, "", path);
    startTransition(() => {
      setRoute(nextRoute);
    });
  }

  async function refreshPersonalData(userId: number) {
    const [videosResult, historyPage] = await Promise.all([
      fetchUserVideos(userId),
      fetchHistory()
    ]);
    setUserVideos(videosResult);
    setHistory(historyPage.content);
  }

  async function handleLogin(email: string, password: string) {
    try {
      setLoginLoading(true);
      setLoginError(null);
      await login({ email, password });
      const currentUser = await fetchCurrentUser();
      setUser(currentUser);
      await refreshPersonalData(currentUser.id);
      setLoginOpen(false);
    } catch (error) {
      const message =
        error instanceof HttpError
          ? error.message
          : "Unable to sign in with the provided credentials.";
      setLoginError(message);
    } finally {
      setLoginLoading(false);
    }
  }

  async function handleCreateDraft(title: string, description: string) {
    setDraftBusy(true);
    try {
      const created = await createVideo({ title, description });
      if (user) {
        const nextVideos = [created, ...userVideos];
        setUserVideos(nextVideos);
      }
      navigate("/studio", { kind: "studio" });
    } finally {
      setDraftBusy(false);
    }
  }

  async function handleUploadVideo(videoId: number, file: File) {
    const result = await uploadVideoFile(videoId, file);
    setTaskPollingId(result.taskId);
    setTask({
      id: result.taskId,
      videoId: result.videoId,
      userId: user?.id ?? 0,
      filePath: result.videoUrl,
      fileSize: file.size,
      status: result.status,
      progress: 0,
      errorMessage: null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      completedAt: null
    });
  }

  async function handleUploadCover(videoId: number, file: File) {
    await uploadCoverFile(videoId, file);
    if (user) {
      const refreshed = await fetchUserVideos(user.id);
      setUserVideos(refreshed);
    }
    const page = await fetchVideos();
    setVideos(page.content);
  }

  async function handlePublishVideo(videoId: number) {
    setPublishBusyId(videoId);
    try {
      await publishVideo(videoId);
      if (user) {
        const refreshed = await fetchUserVideos(user.id);
        setUserVideos(refreshed);
      }
      const page = await fetchVideos();
      setVideos(page.content);
    } finally {
      setPublishBusyId(null);
    }
  }

  function openVideo(id: number) {
    navigate(`/video/${id}`, { kind: "video", id });
  }

  async function handleRecordProgress(progress: number, duration: number) {
    if (!user || !selectedVideo) {
      return;
    }
    await recordVideoProgress(selectedVideo.id, progress, duration).catch(() => undefined);
  }

  return (
    <Shell topbar={topbar}>
      {route.kind === "home" ? (
        <HomePage
          user={user}
          videos={videos}
          myVideos={userVideos}
          history={history}
          loading={videosLoading}
          error={videosError}
          onOpenVideo={openVideo}
          onGoStudio={() => navigate("/studio", { kind: "studio" })}
        />
      ) : null}

      {route.kind === "library" ? (
        <LibraryPage
          user={user}
          history={history}
          videos={userVideos}
          loading={userDataLoading}
          onOpenLogin={() => setLoginOpen(true)}
          onOpenStudio={() => navigate("/studio", { kind: "studio" })}
          onOpenVideo={openVideo}
        />
      ) : null}

      {route.kind === "studio" ? (
        <StudioPage
          user={user}
          videos={userVideos}
          loading={userDataLoading}
          task={task}
          taskBusy={taskBusy}
          draftBusy={draftBusy}
          publishBusyId={publishBusyId}
          onOpenLogin={() => setLoginOpen(true)}
          onOpenVideo={openVideo}
          onCreateDraft={handleCreateDraft}
          onUploadVideo={handleUploadVideo}
          onUploadCover={handleUploadCover}
          onPublishVideo={handlePublishVideo}
        />
      ) : null}

      {route.kind === "video" ? (
        <VideoPage
          video={selectedVideo}
          comments={comments}
          relatedVideos={relatedVideos}
          loading={videoLoading}
          commentsLoading={commentsLoading}
          error={videoError}
          resumeSeconds={resumeSeconds}
          onBack={() => navigate("/", { kind: "home" })}
          onOpenVideo={openVideo}
          onRecordProgress={handleRecordProgress}
        />
      ) : null}

      <LoginSheet
        open={loginOpen}
        loading={loginLoading}
        error={loginError}
        onClose={() => setLoginOpen(false)}
        onSubmit={handleLogin}
      />
    </Shell>
  );
}
