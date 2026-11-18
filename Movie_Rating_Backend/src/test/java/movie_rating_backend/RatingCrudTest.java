package movie_rating_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 评分核心 CRUD：唯一约束（同一人同一电影一条）、均分重算、属主删除校验。
 */
class RatingCrudTest extends AbstractIntegrationTest {

    private int userId;
    private int otherUserId;
    private int movieId;

    @BeforeEach
    void seedData() {
        userId = insertUser("rater1", "pass123456");
        otherUserId = insertUser("rater2", "pass123456");
        movieId = insertMovie("Rating Test Movie");
    }

    private void rate(int uid, String username, int score) throws Exception {
        mockMvc.perform(post("/api/rating")
                        .header("Authorization", bearer(token(uid, username, "user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":" + movieId + ",\"score\":" + score + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("同一用户重复评分只保留一条记录且均分重算")
    void repeatRatingUpsertsSingleRowAndRecalculatesAverage() throws Exception {
        rate(userId, "rater1", 8);
        rate(userId, "rater1", 6);

        assertEquals(1, countRows("rating1707", "UserID = ? AND MovieID = ?", userId, movieId));
        Integer score = jdbcTemplate.queryForObject(
                "SELECT Score FROM rating1707 WHERE UserID = ? AND MovieID = ?", Integer.class, userId, movieId);
        assertEquals(Integer.valueOf(6), score);
        assertEquals(6.0, movieRating(movieId), 0.001);
    }

    @Test
    @DisplayName("我的评分接口返回 UserRatingDTO 投影字段")
    void userRatingsProjectionReturnsMovieInfo() throws Exception {
        rate(userId, "rater1", 8);

        mockMvc.perform(get("/api/rating/user")
                        .header("Authorization", bearer(token(userId, "rater1", "user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].ratingId").isNumber())
                .andExpect(jsonPath("$.data[0].score").value(8))
                .andExpect(jsonPath("$.data[0].movieId").value(movieId))
                .andExpect(jsonPath("$.data[0].title").value("Rating Test Movie"));
    }

    @Test
    @DisplayName("他人不能删除我的评分，本人删除后均分置空")
    void onlyOwnerCanDeleteRating() throws Exception {
        rate(userId, "rater1", 8);
        Integer ratingId = jdbcTemplate.queryForObject(
                "SELECT RatingID FROM rating1707 WHERE UserID = ? AND MovieID = ?", Integer.class, userId, movieId);

        // 他人删除：控制器返回业务码 403（HTTP 状态始终 200）
        mockMvc.perform(delete("/api/rating/" + ratingId)
                        .header("Authorization", bearer(token(otherUserId, "rater2", "user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        // 本人删除：成功且均分缓存置 NULL
        mockMvc.perform(delete("/api/rating/" + ratingId)
                        .header("Authorization", bearer(token(userId, "rater1", "user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals(0, countRows("rating1707", "UserID = ?", userId));
        assertNull(movieRating(movieId));
    }
}