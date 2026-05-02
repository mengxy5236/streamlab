import { apiFetch } from "./client";
import type { PageResponse, VideoProgress, WatchHistoryItem } from "../types/api";

export function fetchHistory(page = 0, size = 8) {
  return apiFetch<PageResponse<WatchHistoryItem>>(`/history?page=${page}&size=${size}`, {}, true);
}

export function fetchVideoProgress(videoId: number) {
  return apiFetch<VideoProgress>(`/history/video/${videoId}/progress`, {}, true);
}

export function recordVideoProgress(videoId: number, progress: number, duration: number) {
  return apiFetch<void>(`/history/video/${videoId}`, {
    method: "POST",
    body: JSON.stringify({ progress, duration })
  }, true);
}
