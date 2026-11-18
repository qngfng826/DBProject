package movie_rating_backend;

import movie_rating_backend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 集成测试基类：H2 内存库（MySQL 兼容模式）+ MockMvc。
 * 每个用例前清空全部业务表保证独立性；提供种子数据与 JWT 构造辅助方法。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected JwtUtil jwtUtil;

    @BeforeEach
    void cleanDatabase() {
        // 先清子表再清父表，避免外键约束阻塞
        jdbcTemplate.update("DELETE FROM movie_actor1707");
        jdbcTemplate.update("DELETE FROM movie_director1707");
        jdbcTemplate.update("DELETE FROM comment1707");
        jdbcTemplate.update("DELETE FROM rating1707");
        jdbcTemplate.update("DELETE FROM movie1707");
        jdbcTemplate.update("DELETE FROM actor1707");
        jdbcTemplate.update("DELETE FROM director1707");
        jdbcTemplate.update("DELETE FROM user1707");
    }

    protected int insertUser(String username, String rawPassword) {
        jdbcTemplate.update("INSERT INTO user1707 (Username, Password) VALUES (?, ?)",
                username, PASSWORD_ENCODER.encode(rawPassword));
        Integer id = jdbcTemplate.queryForObject(
                "SELECT UserID FROM user1707 WHERE Username = ?", Integer.class, username);
        return id;
    }

    protected int insertMovie(String title) {
        jdbcTemplate.update("INSERT INTO movie1707 (Title, Rating) VALUES (?, 0.0)", title);
        Integer id = jdbcTemplate.queryForObject(
                "SELECT MovieID FROM movie1707 WHERE Title = ?", Integer.class, title);
        return id;
    }

    protected String token(int userId, String username, String role) {
        return jwtUtil.createToken(userId, username, role);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected Double movieRating(int movieId) {
        return jdbcTemplate.queryForObject(
                "SELECT Rating FROM movie1707 WHERE MovieID = ?", Double.class, movieId);
    }

    protected int countRows(String table, String where, Object... args) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + where, Integer.class, args);
    }
}