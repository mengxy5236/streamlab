import { apiFetch } from "./client";
import type { Comment, PageResponse, Video } from "../types/api";

export function fetchVideos(page = 0, size = 8) {
  return apiFetch<PageResponse<Video>>(`/videos/list?page=${page}&size=${size}`);
}

export function fetchVideo(videoId: number) {
  return apiFetch<Video>(`/videos/${videoId}`);
}

export function fetchComments(videoId: number, page = 0, size = 12) {
  return apiFetch<PageResponse<Comment>>(
    `/comments/video/${videoId}?page=${page}&size=${size}`
  );
}

export function incrementView(videoId: number) {
  return apiFetch<void>(`/videos/${videoId}/view`, { method: "POST" });
}
