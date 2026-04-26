# StreamLab Workspace

This repository is now organized as a small full-stack workspace.

## Layout

```text
streamlab/
├── backend/   # Spring Boot backend
├── frontend/  # frontend app placeholder
├── .git/
└── README.md
```

## Why `.git` stays at the root

Keeping `.git` at the workspace root is the right choice if you want:

- one repository for both frontend and backend
- shared commit history
- a single issue / branch / PR flow
- easier full-stack collaboration

`backend/` should contain Spring Boot specific files such as `pom.xml`, `.mvn`, `src`, `docs`, and `.env`.

## Projects

- Backend: [backend/README.md](backend/README.md)
- Frontend: `frontend/` is currently empty and ready for scaffolding

## Suggested next step

Create the frontend as a separate app inside `frontend/`, for example with:

```text
React + Vite + TypeScript
```
