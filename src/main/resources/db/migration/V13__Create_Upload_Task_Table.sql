CREATE TABLE upload_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NULL,
    user_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NULL,
    file_size BIGINT NULL,
    status ENUM('PENDING', 'UPLOADING', 'TRANSCODING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    progress INT NOT NULL DEFAULT 0 COMMENT '处理进度百分比',
    error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_task_video FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

