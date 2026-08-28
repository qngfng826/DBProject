-- 测试报告接口返回的数据
SELECT Genre, COUNT(DISTINCT MovieID) as Count, AVG(r.Score) as AvgRating
FROM movie1707 m
LEFT JOIN rating1707 r ON m.MovieID = r.MovieID
LEFT JOIN comment1707 c ON m.MovieID = c.MovieID
WHERE m.Genre IS NOT NULL
AND m.Genre IN ('剧情', '喜剧', '动作', '爱情', '科幻', '动画', '悬疑', '犯罪', '奇幻')
AND r.Score IS NOT NULL
GROUP BY m.Genre
ORDER BY m.Genre;

-- 查看所有不同Genre的值
SELECT DISTINCT Genre FROM movie1707 WHERE Genre IS NOT NULL ORDER BY Genre;

-- 查看明细信息
SELECT Title, Genre, ReleaseYear FROM movie1707
WHERE Genre IS NOT NULL
AND Genre IN ('剧情', '喜剧', '动作', '爱情', '科幻', '动画', '悬疑', '犯罪', '奇幻')
ORDER BY Genre, ReleaseYear DESC
LIMIT 20;
