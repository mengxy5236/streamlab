import type { ApiResponse } from "../types/api";

const API_PREFIX = "/api";

export class HttpError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

export function getAccessToken() {
  return window.localStorage.getItem("streamlab.accessToken");
}

export function setAccessToken(token: string | null) {
  if (token) {
    window.localStorage.setItem("streamlab.accessToken", token);
  } else {
    window.localStorage.removeItem("streamlab.accessToken");
  }
}

export async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
  authenticated = false
): Promise<T> {
  const headers = new Headers(options.headers ?? {});

  if (!headers.has("Content-Type") && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (authenticated) {
    const token = getAccessToken();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  const response = await fetch(`${API_PREFIX}${path}`, {
    ...options,
    headers,
    credentials: "include"
  });

  const payload = (await response.json().catch(() => null)) as ApiResponse<T> | null;

  if (!response.ok || !payload || payload.code >= 400) {
    throw new HttpError(
      response.status || payload?.code || 500,
      payload?.message || "Request failed"
    );
  }

  return payload.data;
}
