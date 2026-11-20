package movie_rating_backend.utils.cache;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 缓存关闭时的空实现：每次都直接回源，不产生任何 Redis 连接或网络开销。
 */
public class NoOpCacheClient implements CacheClient {

    @Override
    public <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader) {
        return loader.get();
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        // no-op
    }

    @Override
    public void evict(String... keys) {
        // no-op
    }

    @Override
    public String getOrCreateVersion(String key, Duration ttl) {
        return "0";
    }

    @Override
    public void bumpVersion(String key, Duration ttl) {
        // no-op
    }
}