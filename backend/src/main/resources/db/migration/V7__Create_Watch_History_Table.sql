CREATE TABLE watch_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    video_id BIGINT NOT NULL,
    progress INT NOT NULL DEFAULT 0 COMMENT '播放进度(秒)',
    duration INT NOT NULL DEFAULT 0 COMMENT '本次观看时长(秒)',
    watched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_video FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
    UNIQUE INDEX uk_user_video (user_id, video_id),
    INDEX idx_video_id (video_id),
    INDEX idx_watched_at (watched_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

