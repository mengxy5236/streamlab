export function formatDuration(seconds: number | null | undefined) {
  if (!seconds || seconds <= 0) {
    return "00:00";
  }

  const total = Math.floor(seconds);
  const hours = Math.floor(total / 3600);
  const minutes = Math.floor((total % 3600) / 60);
  const remaining = total % 60;

  if (hours > 0) {
    return [hours, minutes, remaining].map((value) => String(value).padStart(2, "0")).join(":");
  }

  return [minutes, remaining].map((value) => String(value).padStart(2, "0")).join(":");
}

export function formatRelativeDate(input: string | null) {
  if (!input) {
    return "Recently";
  }

  const date = new Date(input);
  if (Number.isNaN(date.getTime())) {
    return "Recently";
  }

  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    year: "numeric"
  }).format(date);
}
