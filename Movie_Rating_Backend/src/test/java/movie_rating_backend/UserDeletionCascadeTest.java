package movie_rating_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 回归测试（待办⑥）：删除用户时外键级联清理评论/评分，并补偿重算电影均分。
 */
class UserDeletionCascadeTest extends AbstractIntegrationTest {

    private int adminId;
    private int userA;
    private int userB;
    private int movieId;

    @BeforeEach
    void seedData() {
        adminId = insertUser("admin", "admin123456");
        userA = insertUser("cascadeA", "pass123456");
        userB = insertUser("cascadeB", "pass123456");
        movieId = insertMovie("Cascade Test Movie");
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
    @DisplayName("删除用户级联清理评论/评分并重算受影响电影均分")
    void deletingUserCascadesChildrenAndRecalculatesRating() throws Exception {
        rate(userA, "cascadeA", 2);
        rate(userB, "cascadeB", 8);

        mockMvc.perform(post("/api/comment")
                        .header("Authorization", bearer(token(userA, "cascadeA", "user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":" + movieId + ",\"content\":\"will be cascaded\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals(5.0, movieRating(movieId), 0.001);

        mockMvc.perform(delete("/api/user/" + userA)
                        .header("Authorization", bearer(token(adminId, "admin", "admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals(0, countRows("rating1707", "UserID = ?", userA));
        assertEquals(0, countRows("comment1707", "UserID = ?", userA));
        assertEquals(0, countRows("user1707", "UserID = ?", userA));
        // 另一用户的评分与均分缓存不受影响
        assertEquals(1, countRows("rating1707", "UserID = ?", userB));
        assertEquals(8.0, movieRating(movieId), 0.001);
    }
}