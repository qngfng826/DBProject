package movie_rating_backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    // 密钥支持通过环境变量 JWT_SECRET 注入；默认值仅供本地开发，部署环境务必配置并轮换
    private static final String SECRET = System.getenv("JWT_SECRET") != null
            ? System.getenv("JWT_SECRET") : "movie_rating_secret_key_2024";
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24; // 24小时

    public static String createToken(Integer userId, String username, String role) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        return JWT.create()
                .withHeader(header)
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static Integer getUserId(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token不能为空");
        }
        // 去除可能存在的 "Bearer " 前缀和首尾空白字符
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getClaim("userId")
                .asInt();
    }

    public static String getRole(String token) {
        if (token == null || token.isEmpty()) {
            return "user";
        }
        // 去除可能存在的 "Bearer " 前缀和首尾空白字符
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getClaim("role")
                .asString();
    }

    public static String getUsername(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 去除可能存在的 "Bearer " 前缀和首尾空白字符
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token)
                .getClaim("username")
                .asString();
    }
    public static boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}

