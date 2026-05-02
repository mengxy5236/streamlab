import { apiFetch } from "./client";
import type { CoverUploadResult, UploadTask, UploadVideoResult } from "../types/api";

export function uploadVideoFile(videoId: number, file: File) {
  const formData = new FormData();
  formData.append("file", file);
  return apiFetch<UploadVideoResult>(`/upload/${videoId}`, {
    method: "POST",
    body: formData
  }, true);
}

export function uploadCoverFile(videoId: number, file: File) {
  const formData = new FormData();
  formData.append("file", file);
  return apiFetch<CoverUploadResult>(`/upload/${videoId}/cover`, {
    method: "POST",
    body: formData
  }, true);
}

export function fetchUploadTask(taskId: number) {
  return apiFetch<UploadTask>(`/upload/tasks/${taskId}`, {}, true);
}
