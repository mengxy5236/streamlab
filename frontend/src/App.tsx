import { useEffect, useMemo, useState } from "react";
import { fetchCurrentUser, login } from "./api/auth";
import { HttpError, getAccessToken } from "./api/client";
import { fetchComments, fetchVideo, fetchVideos, incrementView } from "./api/videos";
import { Shell } from "./components/Shell";
import { Topbar } from "./components/Topbar";
import { LoginSheet } from "./components/LoginSheet";
import { HomePage } from "./pages/HomePage";
import { VideoPage } from "./pages/VideoPage";
import type { Comment, User, Video } from "./types/api";

type RouteState =
  | { kind: "home" }
  | { kind: "video"; id: number };

function deriveRoute(): RouteState {
  const path = window.location.pathname.replace(/\/+$/, "") || "/";
  const match = path.match(/^\/video\/(\d+)$/);
  if (match) {
    return { kind: "video", id: Number(match[1]) };
  }
  return { kind: "home" };
}

export default function App() {
  const [route, setRoute] = useState<RouteState>(() => deriveRoute());
  const [videos, setVideos] = useState<Video[]>([]);
  const [videosLoading, setVideosLoading] = useState(true);
  const [videosError, setVideosError] = useState<string | null>(null);

  const [selectedVideo, setSelectedVideo] = useState<Video | null>(null);
  const [videoLoading, setVideoLoading] = useState(false);
  const [videoError, setVideoError] = useState<string | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(false);

  const [loginOpen, setLoginOpen] = useState(false);
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

  const [user, setUser] = useState<User | null>(null);

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
    if (route.kind !== "video") {
      setSelectedVideo(null);
      setComments([]);
      setVideoError(null);
      return;
    }

    const videoId = route.id;
    let active = true;

    async function loadDetail() {
      try {
        setVideoLoading(true);
        setCommentsLoading(true);

        const [video, page] = await Promise.all([
          fetchVideo(videoId),
          fetchComments(videoId)
        ]);

        if (!active) {
          return;
        }

        setSelectedVideo(video);
        setComments(page.content);
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
  }, [route]);

  const topbar = useMemo(
    () => (
      <Topbar
        user={user}
        onOpenLogin={() => setLoginOpen(true)}
        onGoHome={() => {
          window.history.pushState({}, "", "/");
          setRoute({ kind: "home" });
        }}
      />
    ),
    [user]
  );

  async function handleLogin(email: string, password: string) {
    try {
      setLoginLoading(true);
      setLoginError(null);
      await login({ email, password });
      const currentUser = await fetchCurrentUser();
      setUser(currentUser);
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

  function openVideo(id: number) {
    window.history.pushState({}, "", `/video/${id}`);
    setRoute({ kind: "video", id });
  }

  return (
    <Shell topbar={topbar}>
      {route.kind === "home" ? (
        <HomePage
          videos={videos}
          loading={videosLoading}
          error={videosError}
          onOpenVideo={openVideo}
        />
      ) : (
        <VideoPage
          video={selectedVideo}
          comments={comments}
          loading={videoLoading}
          commentsLoading={commentsLoading}
          error={videoError}
          onBack={() => {
            window.history.pushState({}, "", "/");
            setRoute({ kind: "home" });
          }}
        />
      )}

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
