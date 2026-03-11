-- V12__Seed_Categories.sql
-- 初始分类数据
INSERT INTO categories (name, sort_order, created_at) VALUES
  ('番剧', 10, NOW()),
  ('电影', 20, NOW()),
  ('电视剧', 30, NOW()),
  ('纪录片', 40, NOW()),
  ('综艺', 50, NOW()),
  ('音乐', 60, NOW()),
  ('知识', 70, NOW());
