package movie_rating_backend.controller;

import movie_rating_backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 与前端电影列表保持一致的电影类型列表（List.of 是 Java 9+ API，改用 Java 8 写法）
    private static final List<String> MOVIE_GENRES = Collections.unmodifiableList(Arrays.asList(
            "剧情", "喜剧", "动作", "爱情", "科幻", "动画", "悬疑", "犯罪", "奇幻"
    ));

    @GetMapping("/genre-summary")
    public Result<Map<String, Object>> getGenreSummary() {
        Map<String, Object> result = new HashMap<>();

        // 1. 全局统计信息 - 使用rating1707.Score（用户评分）
        String globalStatsSql = "SELECT " +
                "MAX(r.Score) as MaxRating, " +
                "MIN(r.Score) as MinRating, " +
                "(SELECT COUNT(*) FROM comment1707) as TotalComments " +
                "FROM rating1707 r";
        Map<String, Object> globalStats = jdbcTemplate.queryForMap(globalStatsSql);
        result.put("globalStats", globalStats);

        // 2. 汇总信息 - 按单一类型统计：复合类型电影（如"剧情/犯罪"）计入其包含的每个类型
        // 逐类型聚合，避免 GROUP BY 原始 Genre 产生"剧情/犯罪"这类复合分组
        List<Map<String, Object>> summary = new java.util.ArrayList<>();
        for (String genre : MOVIE_GENRES) {
            String genreLike = "%" + genre + "%";
            // 评分只 JOIN rating 表，避免 rating × comment 笛卡尔积导致统计膨胀
            Map<String, Object> stats = jdbcTemplate.queryForMap(
                    "SELECT COUNT(DISTINCT m.MovieID) as Count, " +
                            "AVG(r.Score) as AvgRating, MAX(r.Score) as MaxRating, MIN(r.Score) as MinRating " +
                            "FROM movie1707 m LEFT JOIN rating1707 r ON m.MovieID = r.MovieID " +
                            "WHERE m.Genre LIKE ?",
                    genreLike);
            // 评论数单独统计，同样避免与评分 JOIN 产生重复计数
            Integer totalComments = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM comment1707 c JOIN movie1707 m ON c.MovieID = m.MovieID " +
                            "WHERE m.Genre LIKE ?",
                    Integer.class, genreLike);

            Map<String, Object> row = new HashMap<>();
            row.put("Genre", genre);
            row.put("Count", stats.get("Count"));
            row.put("AvgRating", stats.get("AvgRating"));
            row.put("MaxRating", stats.get("MaxRating"));
            row.put("MinRating", stats.get("MinRating"));
            row.put("TotalComments", totalComments);
            summary.add(row);
        }
        result.put("summary", summary);

        // 3. 明细信息 - 返回包含任一标准类型的电影（保留复合类型原文展示）
        String likeConditionsNoAlias = MOVIE_GENRES.stream()
                .map(genre -> "Genre LIKE '%" + genre + "%'")
                .collect(java.util.stream.Collectors.joining(" OR "));

        String detailSql = "SELECT Title, Genre, ReleaseYear FROM movie1707 " +
                "WHERE Genre IS NOT NULL " +
                "AND (" + likeConditionsNoAlias + ")" +
                "ORDER BY Genre, ReleaseYear DESC";
        List<Map<String, Object>> detail = jdbcTemplate.queryForList(detailSql);
        result.put("detail", detail);

        return Result.success(result);
    }

}

