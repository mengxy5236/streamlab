import type { Comment } from "../types/api";
import { formatRelativeDate } from "../lib/format";

interface CommentListProps {
  comments: Comment[];
}

export function CommentList({ comments }: CommentListProps) {
  if (comments.length === 0) {
    return (
      <div className="empty-state card-surface">
        <h3>No comments yet</h3>
        <p>The detail page is wired. Once your backend has data, comments will appear here.</p>
      </div>
    );
  }

  return (
    <div className="comment-list">
      {comments.map((comment) => (
        <article className="comment-card card-surface" key={comment.id}>
          <div className="comment-avatar">
            {(comment.username || "U").charAt(0).toUpperCase()}
          </div>
          <div className="comment-body">
            <div className="comment-head">
              <strong>{comment.username || `User #${comment.userId}`}</strong>
              <span>{formatRelativeDate(comment.createdAt)}</span>
            </div>
            <p>{comment.content}</p>
            <div className="comment-meta">
              <span>{comment.likesCount ?? 0} likes</span>
              <span>{comment.replyCount ?? 0} replies</span>
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}
