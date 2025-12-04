CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL COMMENT '父评论ID',
    root_id BIGINT NULL COMMENT '根评论ID',
    content TEXT NOT NULL,
    likes_count INT NOT NULL DEFAULT 0,
    reply_count INT NOT NULL DEFAULT 0,
    status ENUM('VISIBLE', 'HIDDEN', 'DELETED') NOT NULL DEFAULT 'VISIBLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_video FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_root FOREIGN KEY (root_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_video_id (video_id),
    INDEX idx_user_id (user_id),
    INDEX idx_root_id (root_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

