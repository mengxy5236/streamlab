CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT NULL COMMENT '父分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(255) NULL COMMENT '图标URL',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE video_categories (
    video_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (video_id, category_id),
    CONSTRAINT fk_vc_video FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
    CONSTRAINT fk_vc_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

