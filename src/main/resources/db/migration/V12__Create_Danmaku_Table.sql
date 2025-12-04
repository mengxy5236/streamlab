CREATE TABLE danmaku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(100) NOT NULL COMMENT '弹幕内容',
    send_time DECIMAL(10,3) NOT NULL COMMENT '视频时间点(秒)',
    mode TINYINT NOT NULL DEFAULT 1 COMMENT '1滚动 4底部 5顶部',
    font_size INT NOT NULL DEFAULT 25,
    color INT NOT NULL DEFAULT 16777215 COMMENT 'RGB颜色值',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_danmaku_video FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
    CONSTRAINT fk_danmaku_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_video_time (video_id, send_time),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

