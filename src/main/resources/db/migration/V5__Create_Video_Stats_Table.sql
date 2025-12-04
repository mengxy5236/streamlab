CREATE TABLE video_stats (
    video_id BIGINT PRIMARY KEY,
    views_count BIGINT NOT NULL DEFAULT 0 COMMENT '播放量',
    likes_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    coins_count INT NOT NULL DEFAULT 0 COMMENT '投币数',
    favorites_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    comments_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    shares_count INT NOT NULL DEFAULT 0 COMMENT '分享数',
    danmaku_count INT NOT NULL DEFAULT 0 COMMENT '弹幕数',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_stats_video FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

