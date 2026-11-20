package movie_rating_backend.utils.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.SerializationException;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 热点缓存实现：
 * - 空值缓存（防穿透）
 * - SETNX 互斥锁 + 双重检查（防击穿）
 * - TTL 随机抖动（防雪崩）
 * - Redis 异常熔断 30 秒，期间直接回源，避免每个请求都吃超时
 */
@Slf4j
public class RedisCacheClient implements CacheClient {

    static final String NULL_VALUE = "@NULL@";
    private static final String LOCK_PREFIX = "mr:lock:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration nullTtl;
    private final long lockTtlSeconds;
    private final long circuitOpenMillis;
    private final int retryTimes;
    private final long retrySleepMillis;

    private volatile long redisUnavailableUntil = 0L;

    public RedisCacheClient(RedisTemplate<String, Object> redisTemplate,
                            Duration nullTtl,
                            long lockTtlSeconds,
                            long circuitOpenSeconds) {
        this(redisTemplate, nullTtl, lockTtlSeconds, circuitOpenSeconds, 10, 50L);
    }

    RedisCacheClient(RedisTemplate<String, Object> redisTemplate,
                     Duration nullTtl,
                     long lockTtlSeconds,
                     long circuitOpenSeconds,
                     int retryTimes,
                     long retrySleepMillis) {
        this.redisTemplate = redisTemplate;
        this.nullTtl = nullTtl;
        this.lockTtlSeconds = lockTtlSeconds;
        this.circuitOpenMillis = TimeUnit.SECONDS.toMillis(circuitOpenSeconds);
        this.retryTimes = retryTimes;
        this.retrySleepMillis = retrySleepMillis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader) {
        if (!isRedisUsable()) {
            return loader.get();
        }

        ValueOperations<String, Object> ops;
        Object cached;
        try {
            ops = redisTemplate.opsForValue();
            cached = ops.get(key);
        } catch (Exception e) {
            // 旧版本 DTO 等反序列化失败：只清掉坏 key，不触发全局熔断
            if (e instanceof SerializationException) {
                safeDelete(key);
                return loader.get();
            }
            // 只有 Redis 访问异常才熔断；loader 的异常不能在缓存层被吞掉
            markRedisUnavailable(e);
            return loader.get();
        }
        if (isNullMarker(cached)) {
            return null;
        }
        if (cached != null) {
            return (T) cached;
        }

        String lockKey = LOCK_PREFIX + key;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked;
        try {
            locked = ops.setIfAbsent(lockKey, lockValue, lockTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            markRedisUnavailable(e);
            return loader.get();
        }

        if (Boolean.TRUE.equals(locked)) {
            try {
                Object doubleChecked;
                try {
                    // 双重检查：拿到锁期间可能已有其他请求回填
                    doubleChecked = ops.get(key);
                } catch (Exception e) {
                    if (e instanceof SerializationException) {
                        safeDelete(key);
                        return loader.get();
                    }
                    markRedisUnavailable(e);
                    return loader.get();
                }
                if (isNullMarker(doubleChecked)) {
                    return null;
                }
                if (doubleChecked != null) {
                    return (T) doubleChecked;
                }

                T value = loader.get();
                try {
                    setWithTtl(key, value, ttl);
                } catch (Exception e) {
                    // 序列化失败等写入问题：本次仅跳过缓存返回 DB 结果，不误触发熔断
                    if (e instanceof SerializationException) {
                        log.warn("缓存写入失败（跳过本次缓存）: key={}, {}", key, e.getMessage());
                    } else {
                        markRedisUnavailable(e);
                    }
                }
                return value;
            } finally {
                releaseLock(lockKey, lockValue);
            }
        }

        // 未拿到锁：短暂等待其他请求回填，超时后降级直查数据库
        for (int i = 0; i < retryTimes; i++) {
            sleep(retrySleepMillis);
            try {
                Object retried = ops.get(key);
                if (isNullMarker(retried)) {
                    return null;
                }
                if (retried != null) {
                    return (T) retried;
                }
            } catch (Exception e) {
                if (e instanceof SerializationException) {
                    safeDelete(key);
                    return loader.get();
                }
                markRedisUnavailable(e);
                return loader.get();
            }
        }
        return loader.get();
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        if (!isRedisUsable()) {
            return;
        }
        try {
            setWithTtl(key, value, ttl);
        } catch (Exception e) {
            if (!(e instanceof SerializationException)) {
                markRedisUnavailable(e);
            }
        }
    }

    @Override
    public void evict(String... keys) {
        if (keys == null || keys.length == 0 || !isRedisUsable()) {
            return;
        }
        try {
            for (String key : keys) {
                if (key != null && !key.isEmpty()) {
                    redisTemplate.delete(key);
                }
            }
        } catch (Exception e) {
            markRedisUnavailable(e);
        }
    }

    @Override
    public String getOrCreateVersion(String key, Duration ttl) {
        if (!isRedisUsable()) {
            return "0";
        }
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            Object cached = ops.get(key);
            if (cached != null) {
                return String.valueOf(cached);
            }
            String version = newVersion();
            ops.setIfAbsent(key, version, ttl.getSeconds(), TimeUnit.SECONDS);
            Object actual = ops.get(key);
            return actual != null ? String.valueOf(actual) : version;
        } catch (Exception e) {
            markRedisUnavailable(e);
            return "0";
        }
    }

    @Override
    public void bumpVersion(String key, Duration ttl) {
        if (!isRedisUsable()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, newVersion(), ttl.getSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            markRedisUnavailable(e);
        }
    }

    private void setWithTtl(String key, Object value, Duration ttl) {
        Duration effectiveTtl = value == null ? nullTtl : jitter(ttl);
        Object payload = value == null ? NULL_VALUE : value;
        redisTemplate.opsForValue().set(key, payload, effectiveTtl.getSeconds(), TimeUnit.SECONDS);
    }

    private boolean isNullMarker(Object cached) {
        return NULL_VALUE.equals(cached);
    }

    private boolean isRedisUsable() {
        return System.currentTimeMillis() >= redisUnavailableUntil;
    }

    private void markRedisUnavailable(Exception e) {
        long now = System.currentTimeMillis();
        if (now >= redisUnavailableUntil) {
            log.warn("Redis 不可用，热点缓存降级直查数据库（{} 秒内不再尝试）: {}",
                    TimeUnit.MILLISECONDS.toSeconds(circuitOpenMillis), e.getMessage());
        }
        redisUnavailableUntil = now + circuitOpenMillis;
    }

    private void safeDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {
            // 删除失败不影响回源
        }
    }

    private void releaseLock(String lockKey, String lockValue) {
        try {
            Object current = redisTemplate.opsForValue().get(lockKey);
            if (lockValue.equals(current)) {
                redisTemplate.delete(lockKey);
            }
        } catch (Exception ignored) {
            // 锁会按 TTL 自动释放
        }
    }

    private Duration jitter(Duration ttl) {
        long baseSeconds = ttl.getSeconds();
        if (baseSeconds <= 1) {
            return ttl;
        }
        long jitterSeconds = Math.max(1L, (long) (baseSeconds * 0.1));
        long delta = ThreadLocalRandom.current().nextLong(-jitterSeconds, jitterSeconds + 1);
        return Duration.ofSeconds(Math.max(1L, baseSeconds + delta));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String newVersion() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}