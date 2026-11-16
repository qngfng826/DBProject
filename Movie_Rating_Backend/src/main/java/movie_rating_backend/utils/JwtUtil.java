package movie_rating_backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具（Spring Bean）。
 * 签名密钥仅从配置读取：环境变量 JWT_SECRET 优先，本地开发使用 application-local.yml 的 jwt.secret；
 * 源码中不包含任何默认密钥，密钥缺失时应用启动即失败（fail-fast）。
 */
@Component
public class JwtUtil {

    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24; // 24小时

    private final Algorithm algorithm;

    public JwtUtil(@Value("${jwt.secret:}") String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                    "JWT 密钥未配置：请设置环境变量 JWT_SECRET，或在本地 application-local.yml 中配置 jwt.secret（模板见 application-local.yml.example）");
        }
        this.algorithm = Algorithm.HMAC256(secret.trim());
    }

    public String createToken(Integer userId, String username, String role) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        return JWT.create()
                .withHeader(header)
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(algorithm);
    }

    public Integer getUserId(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        // 去除可能存在的 "Bearer " 前缀和首尾空白字符
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getClaim("userId")
                .asInt();
    }

    public String getRole(String token) {
        if (token == null || token.isEmpty()) {
            return "user";
        }
        // 去除可能存在的 "Bearer " 前缀和首尾空白字符
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getClaim("role")
                .asString();
    }

    public String getUsername(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 去除可能存在的 "Bearer " 前缀和首尾空白字符
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getClaim("username")
                .asString();
    }

    public boolean validateToken(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}