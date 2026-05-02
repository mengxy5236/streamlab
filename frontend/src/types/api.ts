export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface Video {
  id: number;
  title: string;
  description: string | null;
  coverUrl: string | null;
  videoUrl: string | null;
  hlsUrl: string | null;
  hlsReady: boolean;
  resolution: string | null;
  bitrate: number | null;
  duration: number | null;
  updatedAt: string | null;
  publishedAt: string | null;
}

export interface Comment {
  id: number;
  videoId: number;
  userId: number;
  username: string | null;
  avatarUrl: string | null;
  content: string;
  parentId: number | null;
  rootId: number | null;
  likesCount: number | null;
  replyCount: number | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface AuthTokenResponse {
  token: string;
}

export interface User {
  id: number;
  phone: string | null;
  email: string | null;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface CreateVideoPayload {
  title: string;
  description: string;
  coverUrl?: string | null;
}

export interface UploadTask {
  id: number;
  videoId: number;
  userId: number;
  filePath: string | null;
  fileSize: number | null;
  status: string;
  progress: number | null;
  errorMessage: string | null;
  createdAt: string | null;
  updatedAt: string | null;
  completedAt: string | null;
}

export interface UploadVideoResult {
  taskId: number;
  videoId: number;
  status: string;
  videoUrl: string;
  mode: string;
}

export interface CoverUploadResult {
  coverUrl: string;
}

export interface WatchHistoryItem {
  id: number;
  videoId: number;
  videoTitle: string;
  videoCoverUrl: string | null;
  progress: number | null;
  duration: number | null;
  watchedAt: string | null;
}

export interface VideoProgress {
  userId: number;
  videoId: number;
  progress: number | null;
  duration: number | null;
  updatedAt: string | null;
}
