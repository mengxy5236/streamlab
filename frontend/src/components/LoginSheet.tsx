import { useEffect, useState } from "react";

interface LoginSheetProps {
  open: boolean;
  loading: boolean;
  error: string | null;
  onClose: () => void;
  onSubmit: (email: string, password: string) => Promise<void>;
}

export function LoginSheet({
  open,
  loading,
  error,
  onClose,
  onSubmit
}: LoginSheetProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  useEffect(() => {
    if (!open) {
      setPassword("");
    }
  }, [open]);

  if (!open) {
    return null;
  }

  return (
    <div className="sheet-backdrop" onClick={onClose}>
      <div className="sheet card-surface" onClick={(event) => event.stopPropagation()}>
        <div className="sheet-header">
          <div>
            <div className="eyebrow">Account access</div>
            <h2>Sign in to unlock studio actions</h2>
          </div>
          <button className="ghost-button compact-button" onClick={onClose}>
            Close
          </button>
        </div>

        <form
          className="sheet-form"
          onSubmit={async (event) => {
            event.preventDefault();
            await onSubmit(email, password);
          }}
        >
          <label>
            <span>Email</span>
            <input
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>

          <label>
            <span>Password</span>
            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>

          {error ? <div className="error-banner">{error}</div> : null}

          <button className="primary-button wide-button" disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </div>
    </div>
  );
}
