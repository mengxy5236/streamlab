import type { User } from "../types/api";

interface TopbarProps {
  user: User | null;
  onOpenLogin: () => void;
  onGoHome: () => void;
}

export function Topbar({ user, onOpenLogin, onGoHome }: TopbarProps) {
  return (
    <div className="topbar-inner card-surface">
      <button className="brand-mark" onClick={onGoHome}>
        <span className="brand-orb" />
        <div>
          <strong>StreamLab</strong>
          <span>Motion-first creator cinema</span>
        </div>
      </button>

      <div className="topbar-actions">
        <div className="pill">iOS calm • YouTube pulse</div>
        {user ? (
          <div className="user-chip">
            <span className="user-avatar">{(user.email ?? "U").charAt(0).toUpperCase()}</span>
            <span>{user.email ?? "Signed in"}</span>
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
