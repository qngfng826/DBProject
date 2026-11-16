package movie_rating_backend.controller;

import movie_rating_backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 与前端电影列表保持一致的电影类型列表（List.of 是 Java 9+ API，改用 Java 8 写法）
    private static final List<String> MOVIE_GENRES = Collections.unmodifiableList(Arrays.asList(
            "剧情", "喜剧", "动作", "爱情", "科幻", "动画", "悬疑", "犯罪", "奇幻"
    ));

    // 9 个类型的派生表（UNION ALL），供汇总 SQL 复用。
    // 显式 COLLATE utf8mb4_general_ci：TiDB 会把派生表字面量视为隐式排序规则，
    // 与 movie1707.Genre 列（utf8mb4_general_ci）做 LIKE 时会报 1267 Illegal mix of collations；
    // MySQL 8 本地虽能自动强制转换，显式声明可保证两端行为一致。
    private static final String GENRE_VALUES = MOVIE_GENRES.stream()
            .map(genre -> "SELECT '" + genre + "' COLLATE utf8mb4_general_ci AS Genre")
            .collect(Collectors.joining(" UNION ALL "));

    @GetMapping("/genre-summary")
    public Result<Map<String, Object>> getGenreSummary() {
        Map<String, Object> result = new HashMap<>();

        // 1. 全局统计（1 条 SQL）：全部评分的最高/最低分 + 评论总数
        Map<String, Object> globalStats = jdbcTemplate.queryForMap(
                "SELECT MAX(r.Score) AS MaxRating, MIN(r.Score) AS MinRating, " +
                        "(SELECT COUNT(*) FROM comment1707) AS TotalComments " +
                        "FROM rating1707 r");
        result.put("globalStats", globalStats);

        // 2. 类型汇总（合并为 1 条 SQL，替代原先 9 个类型 × 2 条的循环查询）：
        //    派生表给出 9 个类型；评分与评论分别由两个分组子查询聚合——
        //    两者分开 JOIN，避免 rating × comment 笛卡尔积导致 AVG 失真；
        //    复合类型（如"剧情/犯罪"）仍按 LIKE 子串语义计入其包含的每个类型。
        String summarySql =
                "SELECT g.Genre AS Genre, s.`Count` AS `Count`, s.AvgRating AS AvgRating, " +
                        "s.MaxRating AS MaxRating, s.MinRating AS MinRating, " +
                        "COALESCE(c.TotalComments, 0) AS TotalComments " +
                        "FROM (" + GENRE_VALUES + ") g " +
                        "LEFT JOIN (" +
                        "  SELECT g2.Genre AS Genre, COUNT(DISTINCT m.MovieID) AS `Count`, " +
                        "         AVG(r.Score) AS AvgRating, MAX(r.Score) AS MaxRating, MIN(r.Score) AS MinRating " +
                        "  FROM (" + GENRE_VALUES + ") g2 " +
                        "  LEFT JOIN movie1707 m ON m.Genre LIKE CONCAT('%', g2.Genre, '%') " +
                        "  LEFT JOIN rating1707 r ON r.MovieID = m.MovieID " +
                        "  GROUP BY g2.Genre" +
                        ") s ON s.Genre = g.Genre " +
                        "LEFT JOIN (" +
                        "  SELECT g3.Genre AS Genre, COUNT(c.CommentID) AS TotalComments " +
                        "  FROM (" + GENRE_VALUES + ") g3 " +
                        "  JOIN movie1707 m2 ON m2.Genre LIKE CONCAT('%', g3.Genre, '%') " +
                        "  JOIN comment1707 c ON c.MovieID = m2.MovieID " +
                        "  GROUP BY g3.Genre" +
                        ") c ON c.Genre = g.Genre";

        List<Map<String, Object>> summary = new ArrayList<>(jdbcTemplate.queryForList(summarySql));

        // 按固定类型顺序输出（GROUP BY 不保证顺序），保证前端图表的分类顺序稳定
        summary.sort(Comparator.comparingInt(row -> MOVIE_GENRES.indexOf(String.valueOf(row.get("Genre")))));
        result.put("summary", summary);

        return Result.success(result);
    }

}