package movie_rating_backend.utils.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.SerializationException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 缓存客户端单测：全部用 Mockito 模拟 RedisTemplate，不依赖真实 Redis，CI 可直接运行。
 */
@ExtendWith(MockitoExtension.class)
class RedisCacheClientTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisCacheClient client;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 重试 2 次、间隔 1ms，测试锁等待时不必真等 500ms
        client = new RedisCacheClient(redisTemplate, Duration.ofSeconds(60), 10, 30, 2, 1L);
    }

    @Test
    void returnsCachedValueWithoutLoading() {
        when(valueOperations.get("k")).thenReturn("cached");

        AtomicBoolean loaded = new AtomicBoolean(false);
        String result = client.getOrLoad("k", Duration.ofSeconds(30), () -> {
            loaded.set(true);
            return "db";
        });

        assertEquals("cached", result);
        assertFalse(loaded.get());
    }

    @Test
    void loadsAndStoresOnMiss() {
        when(valueOperations.get("k")).thenReturn(null);
        when(valueOperations.setIfAbsent(eq("mr:lock:k"), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        String result = client.getOrLoad("k", Duration.ofSeconds(30), () -> "db");

        assertEquals("db", result);
        verify(valueOperations).set(eq("k"), eq("db"), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void cachesNullResultWithShortTtl() {
        when(valueOperations.get("k")).thenReturn(null);
        when(valueOperations.setIfAbsent(eq("mr:lock:k"), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        AtomicInteger loads = new AtomicInteger();
        assertNull(client.getOrLoad("k", Duration.ofSeconds(30), () -> {
            loads.incrementAndGet();
            return null;
        }));

        verify(valueOperations).set(eq("k"), eq(RedisCacheClient.NULL_VALUE), eq(60L), eq(TimeUnit.SECONDS));
        assertEquals(1, loads.get());

        // 空值哨兵命中后不再回源
        when(valueOperations.get("k")).thenReturn(RedisCacheClient.NULL_VALUE);
        assertNull(client.getOrLoad("k", Duration.ofSeconds(30), () -> {
            loads.incrementAndGet();
            return null;
        }));
        assertEquals(1, loads.get());
    }

    @Test
    void opensCircuitAndFallsBackWhenRedisFails() {
        when(valueOperations.get("k")).thenThrow(new IllegalStateException("redis down"));

        assertEquals("db-1", client.getOrLoad("k", Duration.ofSeconds(30), () -> "db-1"));
        assertEquals("db-2", client.getOrLoad("k", Duration.ofSeconds(30), () -> "db-2"));

        // 第二次请求在熔断窗口内，不再访问 Redis
        verify(valueOperations, times(1)).get("k");
    }

    @Test
    void waitsForConcurrentLoaderAndReturnsCachedValue() {
        when(valueOperations.get("k")).thenReturn(null).thenReturn("cached");
        when(valueOperations.setIfAbsent(eq("mr:lock:k"), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        AtomicBoolean loaded = new AtomicBoolean(false);
        String result = client.getOrLoad("k", Duration.ofSeconds(30), () -> {
            loaded.set(true);
            return "db";
        });

        assertEquals("cached", result);
        assertFalse(loaded.get());
    }

    @Test
    void propagatesLoaderExceptionWithoutOpeningCircuit() {
        when(valueOperations.get("k")).thenReturn(null);
        when(valueOperations.setIfAbsent(eq("mr:lock:k"), anyString(), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> client.getOrLoad("k", Duration.ofSeconds(30), () -> {
                    throw new IllegalStateException("db down");
                }));

        // loader 异常不应触发 Redis 熔断：下一次仍会访问 Redis 并命中
        when(valueOperations.get("k")).thenReturn("cached");
        assertEquals("cached", client.getOrLoad("k", Duration.ofSeconds(30), () -> "db"));
    }

    @Test
    void evictsCorruptedEntryAndFallsBackWithoutOpeningCircuit() {
        when(valueOperations.get("k")).thenThrow(new SerializationException("bad json"));

        assertEquals("db", client.getOrLoad("k", Duration.ofSeconds(30), () -> "db"));
        verify(redisTemplate).delete("k");

        // 反序列化失败只删坏 key，不应熔断；下一次仍会访问 Redis
        // 注意：上一次打桩是 thenThrow，重新打桩必须用 doReturn，避免 when(...) 触发异常
        doReturn("cached").when(valueOperations).get("k");
        assertEquals("cached", client.getOrLoad("k", Duration.ofSeconds(30), () -> "db"));
    }

    @Test
    void createsVersionAtomicallyWhenMissing() {
        when(valueOperations.get("mr:movie:ver")).thenReturn(null).thenReturn("v1");
        when(valueOperations.setIfAbsent(eq("mr:movie:ver"), anyString(), eq(86400L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        String version = client.getOrCreateVersion("mr:movie:ver", Duration.ofSeconds(86400));

        assertEquals("v1", version);
    }
}