package movie_rating_backend;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 鉴权链集成测试：Security 过滤链 401 + @AuthRequired 拦截器 403 + 旧 token 兼容。
 */
class AuthChainTest extends AbstractIntegrationTest {

    /** 与 application-test.yml 中的 jwt.secret 保持一致，用于手工构造特殊 token */
    private static final String TEST_SECRET = "test-only-jwt-secret-0123456789abcdef0123456789abcdef";

    private int adminId;
    private int userId;

    @BeforeEach
    void seedUsers() {
        adminId = insertUser("admin", "admin123456");
        userId = insertUser("testuser", "user123456");
    }

    @Test
    @DisplayName("公开浏览接口无需 token 即可访问")
    void publicEndpointAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/api/movie/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("管理员接口无 token → 401")
    void adminEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("管理员接口普通用户 token → 403")
    void adminEndpointWithUserTokenReturns403() throws Exception {
        mockMvc.perform(get("/api/user/list")
                        .header("Authorization", bearer(token(userId, "testuser", "user"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    @DisplayName("管理员接口管理员 token → 200")
    void adminEndpointWithAdminTokenReturns200() throws Exception {
        mockMvc.perform(get("/api/user/list")
                        .header("Authorization", bearer(token(adminId, "admin", "admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("permitAll 路径上的 @AuthRequired 方法无 token → 401")
    void authRequiredPermitAllPathWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/rating/movie/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("permitAll 路径上的 @AuthRequired 方法非法 token → 401（而非 500）")
    void authRequiredPermitAllPathWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/rating/movie/1")
                        .header("Authorization", "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("permitAll 路径上的 @AuthRequired 方法过期 token → 401")
    void authRequiredPermitAllPathWithExpiredTokenReturns401() throws Exception {
        String expired = JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", "testuser")
                .withClaim("role", "user")
                .withExpiresAt(new Date(System.currentTimeMillis() - 60_000))
                .sign(Algorithm.HMAC256(TEST_SECRET));

        mockMvc.perform(get("/api/rating/movie/1")
                        .header("Authorization", bearer(expired)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("无 role claim 的旧版 admin token 仍可通过管理员校验")
    void legacyAdminTokenStillWorks() throws Exception {
        String legacy = JWT.create()
                .withClaim("userId", adminId)
                .withClaim("username", "admin")
                .withExpiresAt(new Date(System.currentTimeMillis() + 3_600_000))
                .sign(Algorithm.HMAC256(TEST_SECRET));

        mockMvc.perform(get("/api/user/list")
                        .header("Authorization", bearer(legacy)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("登录成功返回 token")
    void loginReturnsToken() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }

    @Test
    @DisplayName("密码错误登录失败")
    void loginWithWrongPasswordFails() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }
}