package movie_rating_backend;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import movie_rating_backend.utils.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 纯单元测试（不启动 Spring 容器）。
 */
class JwtUtilTest {

    private static final String SECRET = "unit-test-secret-0123456789abcdef0123456789abcdef";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET);

    @Test
    @DisplayName("签发后可解析出 userId/username/role")
    void createAndParseToken() {
        String token = jwtUtil.createToken(7, "alice", "user");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals(Integer.valueOf(7), jwtUtil.getUserId(token));
        assertEquals("alice", jwtUtil.getUsername(token));
        assertEquals("user", jwtUtil.getRole(token));
    }

    @Test
    @DisplayName("解析时允许携带 Bearer 前缀")
    void acceptsBearerPrefix() {
        String token = jwtUtil.createToken(7, "alice", "user");
        assertEquals(Integer.valueOf(7), jwtUtil.getUserId("Bearer " + token));
    }

    @Test
    @DisplayName("不同密钥签发的 token 校验失败")
    void rejectsTokenSignedWithDifferentSecret() {
        JwtUtil other = new JwtUtil("another-secret-0123456789abcdef0123456789abcdef");
        String token = other.createToken(7, "alice", "user");

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("过期 token 校验失败")
    void rejectsExpiredToken() {
        String expired = JWT.create()
                .withClaim("userId", 7)
                .withClaim("username", "alice")
                .withClaim("role", "user")
                .withExpiresAt(new Date(System.currentTimeMillis() - 60_000))
                .sign(Algorithm.HMAC256(SECRET));

        assertFalse(jwtUtil.validateToken(expired));
    }

    @Test
    @DisplayName("旧版 token 无 role claim 时 getRole 返回 null（兼容语义）")
    void legacyTokenWithoutRoleHasNullRole() {
        String legacy = JWT.create()
                .withClaim("userId", 7)
                .withClaim("username", "admin")
                .withExpiresAt(new Date(System.currentTimeMillis() + 60_000))
                .sign(Algorithm.HMAC256(SECRET));

        assertTrue(jwtUtil.validateToken(legacy));
        assertNull(jwtUtil.getRole(legacy));
    }
}