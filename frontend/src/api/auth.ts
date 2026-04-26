import { apiFetch, setAccessToken } from "./client";
import type { AuthTokenResponse, LoginPayload, User } from "../types/api";

export async function login(payload: LoginPayload) {
  const token = await apiFetch<AuthTokenResponse>(
    "/auth/login",
    {
      method: "POST",
      body: JSON.stringify(payload)
    },
    false
  );
  setAccessToken(token.token);
  return token;
}

export function fetchCurrentUser() {
  return apiFetch<User>("/auth/me", {}, true);
}

export async function logout() {
  await apiFetch<void>("/auth/logout", { method: "POST" }, true);
  setAccessToken(null);
}
