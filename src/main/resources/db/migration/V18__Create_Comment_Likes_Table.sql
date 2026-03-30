CREATE TABLE IF NOT EXISTS comment_likes (
    user_id     BIGINT NOT NULL,
    comment_id  BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, comment_id),
    UNIQUE KEY uk_comment_like (user_id, comment_id),
    KEY idx_comment_id (comment_id),
    CONSTRAINT fk_comment_like_user  FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
