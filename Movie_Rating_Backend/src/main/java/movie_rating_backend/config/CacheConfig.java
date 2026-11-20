package movie_rating_backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import movie_rating_backend.utils.cache.CacheClient;
import movie_rating_backend.utils.cache.NoOpCacheClient;
import movie_rating_backend.utils.cache.RedisCacheClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 缓存装配：app.cache.enabled=true 时使用 Redis（Valkey 兼容），
 * 否则注册 NoOp 实现，测试 / CI / 未配置 Redis 的部署完全不触发连接。
 */
@Configuration
public class CacheConfig {

    /**
     * 缓存值专用 ObjectMapper：默认类型信息（@class）+ JSR310 时间支持。
     * 实体里 Director/Actor 的 birthDate 是 java.time.LocalDate，
     * GenericJackson2JsonRedisSerializer 自带的裸 ObjectMapper 不带 JSR310 模块，写入会直接抛异常。
     */
    public static ObjectMapper redisObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(om, null);
        return om;
    }

    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
    public CacheClient redisCacheClient(RedisTemplate<String, Object> redisTemplate,
                                        CacheProperties properties) {
        return new RedisCacheClient(redisTemplate, properties.nullTtl(),
                properties.getLockTtlSeconds(), properties.getCircuitOpenSeconds());
    }

    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "false", matchIfMissing = true)
    public CacheClient noOpCacheClient() {
        return new NoOpCacheClient();
    }
}