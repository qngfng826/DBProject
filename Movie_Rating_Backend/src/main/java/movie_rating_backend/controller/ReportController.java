package movie_rating_backend.controller;

import movie_rating_backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/genre-summary")
    public Result<Map<String, Object>> getGenreSummary() {
        Map<String, Object> result = new HashMap<>();

        // 1. 全局统计信息 (保留)
        String globalStatsSql = "SELECT " +
                "MAX(Rating) as MaxRating, " +
                "MIN(Rating) as MinRating, " +
                "(SELECT COUNT(*) FROM comment1707) as TotalComments " +
                "FROM movie1707";
        Map<String, Object> globalStats = jdbcTemplate.queryForMap(globalStatsSql);
        result.put("globalStats", globalStats);

        // 2. 【修改】汇总信息：增加最高/最低/评论统计
        // 注意：这里必须使用 LEFT JOIN，并用 COUNT(DISTINCT m.MovieID) 统计电影数量，否则会算成评论数量
        String summarySql = "SELECT " +
                "m.Genre, " +
                "COUNT(DISTINCT m.MovieID) as Count, " +
                "AVG(m.Rating) as AvgRating, " +
                "MAX(m.Rating) as MaxRating, " +     // 新增
                "MIN(m.Rating) as MinRating, " +     // 新增
                "COUNT(c.CommentID) as TotalComments " + // 新增：统计该类型的评论总数
                "FROM movie1707 m " +
                "LEFT JOIN comment1707 c ON m.MovieID = c.MovieID " +
                "WHERE m.Genre IS NOT NULL " +
                "GROUP BY m.Genre";

        List<Map<String, Object>> summary = jdbcTemplate.queryForList(summarySql);
        result.put("summary", summary);

        // 3. 明细信息 (保留)
        String detailSql = "SELECT Title, Genre, ReleaseYear, Rating FROM movie1707 ORDER BY Genre, Rating DESC";
        List<Map<String, Object>> detail = jdbcTemplate.queryForList(detailSql);
        result.put("detail", detail);

        return Result.success(result);
    }

}

