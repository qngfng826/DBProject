package movie_rating_backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 评论核心 CRUD：发表、公开查询、属主校验（改/删）、管理员删除。
 */
class CommentCrudTest extends AbstractIntegrationTest {

    private int authorId;
    private int otherId;
    private int adminId;
    private int movieId;

    @BeforeEach
    void seedData() {
        authorId = insertUser("author1", "pass123456");
        otherId = insertUser("other1", "pass123456");
        adminId = insertUser("admin", "admin123456");
        movieId = insertMovie("Comment Test Movie");
    }

    private int createComment(int uid, String username, String content) throws Exception {
        mockMvc.perform(post("/api/comment")
                        .header("Authorization", bearer(token(uid, username, "user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"movieId\":" + movieId + ",\"content\":\"" + content + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        return jdbcTemplate.queryForObject(
                "SELECT CommentID FROM comment1707 WHERE UserID = ? AND MovieID = ?", Integer.class, uid, movieId);
    }

    @Test
    @DisplayName("发表评论后可在电影评论区公开看到")
    void createdCommentVisibleInMovieComments() throws Exception {
        createComment(authorId, "author1", "nice movie");

        mockMvc.perform(get("/api/comment/movie/" + movieId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].Content").value("nice movie"))
                .andExpect(jsonPath("$.data[0].UserID").value(authorId));
    }

    @Test
    @DisplayName("非属主不能修改/删除他人评论（403）")
    void nonOwnerCannotUpdateOrDelete() throws Exception {
        int commentId = createComment(authorId, "author1", "original");

        mockMvc.perform(put("/api/comment/" + commentId)
                        .header("Authorization", bearer(token(otherId, "other1", "user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hacked\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        mockMvc.perform(delete("/api/comment/" + commentId)
                        .header("Authorization", bearer(token(otherId, "other1", "user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));

        assertEquals("original", jdbcTemplate.queryForObject(
                "SELECT Content FROM comment1707 WHERE CommentID = ?", String.class, commentId));
    }

    @Test
    @DisplayName("属主可以修改并删除自己的评论")
    void ownerCanUpdateAndDelete() throws Exception {
        int commentId = createComment(authorId, "author1", "original");

        mockMvc.perform(put("/api/comment/" + commentId)
                        .header("Authorization", bearer(token(authorId, "author1", "user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals("updated", jdbcTemplate.queryForObject(
                "SELECT Content FROM comment1707 WHERE CommentID = ?", String.class, commentId));

        mockMvc.perform(delete("/api/comment/" + commentId)
                        .header("Authorization", bearer(token(authorId, "author1", "user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals(0, countRows("comment1707", "CommentID = ?", commentId));
    }

    @Test
    @DisplayName("管理员可以删除任意用户评论")
    void adminCanDeleteAnyComment() throws Exception {
        int commentId = createComment(authorId, "author1", "to be removed");

        mockMvc.perform(delete("/api/comment/admin/" + commentId)
                        .header("Authorization", bearer(token(adminId, "admin", "admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        assertEquals(0, countRows("comment1707", "CommentID = ?", commentId));
    }
}