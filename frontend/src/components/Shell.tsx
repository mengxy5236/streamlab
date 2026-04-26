import type { PropsWithChildren, ReactNode } from "react";

interface ShellProps extends PropsWithChildren {
  topbar?: ReactNode;
}

export function Shell({ topbar, children }: ShellProps) {
  return (
    <div className="app-shell">
      <div className="ambient ambient-left" />
      <div className="ambient ambient-right" />
      <header className="topbar">{topbar}</header>
      <main className="main-content">{children}</main>
    </div>
  );
}
