ALTER TABLE videos ADD COLUMN danmaku_count INT NOT NULL DEFAULT 0 COMMENT '弹幕数' AFTER comments_count;
