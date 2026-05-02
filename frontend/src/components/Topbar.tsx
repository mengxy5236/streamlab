import type { User } from "../types/api";

type NavKey = "home" | "library" | "studio";

interface TopbarProps {
  user: User | null;
  activeNav: NavKey;
  onOpenLogin: () => void;
  onGoHome: () => void;
  onGoLibrary: () => void;
  onGoStudio: () => void;
  onLogout: () => void;
}

export function Topbar({
  user,
  activeNav,
  onOpenLogin,
  onGoHome,
  onGoLibrary,
  onGoStudio,
  onLogout
}: TopbarProps) {
  return (
    <div className="topbar-inner card-surface">
      <button className="brand-mark" onClick={onGoHome}>
        <span className="brand-orb" />
        <div>
          <strong>StreamLab</strong>
          <span>Your own calm creator cinema</span>
        </div>
      </button>

      <nav className="topbar-nav">
        <button className={activeNav === "home" ? "nav-chip is-active" : "nav-chip"} onClick={onGoHome}>
          Home
        </button>
        <button
          className={activeNav === "library" ? "nav-chip is-active" : "nav-chip"}
          onClick={onGoLibrary}
        >
          Library
        </button>
        <button
          className={activeNav === "studio" ? "nav-chip is-active" : "nav-chip"}
          onClick={onGoStudio}
        >
          Studio
        </button>
      </nav>

      <div className="topbar-actions">
        <div className="pill">iOS calm, YouTube pulse</div>
        {user ? (
          <div className="user-chip">
            <span className="user-avatar">{(user.email ?? "U").charAt(0).toUpperCase()}</span>
            <div className="user-copy">
              <strong>{user.email?.split("@")[0] ?? "Signed in"}</strong>
              <span>{user.email ?? "Creator mode enabled"}</span>
            </div>
            <button className="ghost-button compact-button" onClick={onLogout}>
              Sign out
            </button>
          </div>
        ) : (
          <button className="primary-button" onClick={onOpenLogin}>
            Sign in
          </button>
        )}
      </div>
    </div>
  );
}
