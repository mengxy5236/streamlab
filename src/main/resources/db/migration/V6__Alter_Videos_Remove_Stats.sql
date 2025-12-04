ALTER TABLE videos
    DROP INDEX idx_views_count,
    DROP COLUMN views_count,
    DROP COLUMN likes_count,
    DROP COLUMN coins_count,
    DROP COLUMN favorites_count,
    DROP COLUMN comments_count,
    DROP COLUMN shares_count,
    DROP COLUMN category;

