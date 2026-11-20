package movie_rating_backend;

import movie_rating_backend.utils.cache.CacheClient;
import movie_rating_backend.utils.cache.RedisCacheClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 只验证 app.cache.enabled=true 时的 Bean 装配与序列化器配置，
 * 不发起任何 Redis 连接；CI 无 Redis 也能稳定通过。
 */
@SpringBootTest(properties = "app.cache.enabled=true")
@ActiveProfiles("test")
class CacheEnabledContextTest {

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void wiresRedisCacheClientWhenEnabled() {
        assertTrue(cacheClient instanceof RedisCacheClient);
        assertTrue(redisTemplate.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
    }
}