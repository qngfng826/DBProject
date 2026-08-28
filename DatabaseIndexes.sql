-- =====================================================
-- 电影评分系统数据库索引优化脚本
-- 创建日期: 2026-08-25
-- 目的: 提升查询性能 3-5 倍
-- =====================================================

-- 1. 电影表 - 标题搜索索引
-- 用于支持按电影标题搜索和模糊查询
CREATE INDEX idx_movie_title ON movie1707(Title);

-- 2. 电影表 - 评分索引
-- 用于支持按评分排序和评分筛选查询
CREATE INDEX idx_movie_rating ON movie1707(Rating);

-- 3. 评论表 - 用户ID索引
-- 用于支持按用户查询评论列表
CREATE INDEX idx_comment_user_id ON comment1707(UserID);

-- 4. 评分表 - 联合索引 (用户ID, 电影ID)
-- 用于支持用户查询自己对某电影的评分
CREATE INDEX idx_rating_user_movie ON rating1707(UserID, MovieID);

-- 5. 评分表 - 电影ID索引
-- 用于支持按电影查询平均评分
CREATE INDEX idx_rating_movie_id ON rating1707(MovieID);

-- 6. 演员表 - 名称索引
-- 用于支持按演员姓名搜索和模糊查询
CREATE INDEX idx_actor_name ON actor1707(Name);

-- 7. 导演表 - 名称索引
-- 用于支持按导演姓名搜索和模糊查询
CREATE INDEX idx_director_name ON director1707(Name);

-- =====================================================
-- 索引使用建议
-- =====================================================

-- 常见查询场景及使用索引说明：
--
-- 1. 按电影标题搜索 (LIKE '%xxx%')
--    -> 使用 idx_movie_title
--
-- 2. 按评分筛选和排序 (ORDER BY Rating DESC)
--    -> 使用 idx_movie_rating
--
-- 3. 查询某用户的所有评论 (WHERE UserID = ?)
--    -> 使用 idx_comment_user_id
--
-- 4. 查询某用户对某电影的评分 (WHERE UserID = ? AND MovieID = ?)
--    -> 使用 idx_rating_user_movie (联合索引)
--
-- 5. 查询某电影的平均评分 (SELECT AVG(Score) ...)
--    -> 使用 idx_rating_movie_id
--
-- 6. 按演员姓名搜索 (LIKE '%xxx%')
--    -> 使用 idx_actor_name
--
-- 7. 按导演姓名搜索 (LIKE '%xxx%')
--    -> 使用 idx_director_name

-- =====================================================
-- 监控索引使用情况
-- =====================================================

-- 查看索引使用统计
-- SELECT
--     table_name,
--     index_name,
--     index_type,
--     seq_in_index,
--     column_name,
--     cardinality
-- FROM information_schema.statistics
-- WHERE table_schema = DATABASE()
--   AND table_name IN ('movie1707', 'comment1707', 'rating1707', 'actor1707', 'director1707')
-- ORDER BY table_name, index_name, seq_in_index;

-- =====================================================
-- 删除索引（如需要回滚）
-- =====================================================

-- DROP INDEX idx_movie_title ON movie1707;
-- DROP INDEX idx_movie_rating ON movie1707;
-- DROP INDEX idx_comment_user_id ON comment1707;
-- DROP INDEX idx_rating_user_movie ON rating1707;
-- DROP INDEX idx_rating_movie_id ON rating1707;
-- DROP INDEX idx_actor_name ON actor1707;
-- DROP INDEX idx_director_name ON director1707;
