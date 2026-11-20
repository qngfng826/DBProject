package movie_rating_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 读接口在缓存关闭（NoOp）下的端到端回归：
 * list / hot / search / detail 的用户评分覆盖与不存在电影行为。
 */
class MovieReadEndpointsTest extends AbstractIntegrationTest {

    private int userId;
    private int movieId;

    @BeforeEach
    void seedData() {
        userId = insertUser("reader1", "pass123456");
        movieId = insertMovie("Hot Movie");
        jdbcTemplate.update("INSERT INTO rating1707 (UserID, MovieID, Score) VALUES (?, ?, ?)",
                userId, movieId, 9);
        jdbcTemplate.update("INSERT INTO comment1707 (UserID, MovieID, Content) VALUES (?, ?, ?)",
                userId, movieId, "好看");
    }

    @Test
    @DisplayName("列表 / 热门榜 / 无关键词搜索均返回种子电影")
    void listHotAndSearchReturnSeededMovie() throws Exception {
        mockMvc.perform(get("/api/movie/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Hot Movie"));

        mockMvc.perform(get("/api/movie/hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("Hot Movie"));

        mockMvc.perform(get("/api/movie/search").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].title").value("Hot Movie"));
    }

    @Test
    @DisplayName("详情对登录用户返回自己的评分，匿名请求 userRating 为 null")
    void detailOverlaysUserRatingOnlyWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/movie/" + movieId)
                        .header("Authorization", bearer(token(userId, "reader1", "user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.movieId").value(movieId))
                .andExpect(jsonPath("$.data.userRating").value(9));

        mockMvc.perform(get("/api/movie/" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userRating").value(nullValue()));
    }

    @Test
    @DisplayName("不存在的电影返回 data=null")
    void unknownMovieReturnsNullData() throws Exception {
        mockMvc.perform(get("/api/movie/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}