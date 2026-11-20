package movie_rating_backend.utils.cache;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 热点缓存抽象。Redis 不可用 / 缓存开关关闭时必须退化为直接查库，不能影响接口可用性。
 */
public interface CacheClient {

    /**
     * 读缓存；未命中时加互斥锁回源，加载成功后回填。loader 返回 null 时缓存空值哨兵。
     */
    <T> T getOrLoad(String key, Duration ttl, Supplier<T> loader);

    void set(String key, Object value, Duration ttl);

    void evict(String... keys);

    /**
     * 读取列表 / 搜索共用的版本号；不存在时原子创建。
     */
    String getOrCreateVersion(String key, Duration ttl);

    /**
     * 写操作后更换版本号，使旧版本的所有列表 / 搜索 key 自然过期，避免 SCAN/KEYS。
     */
    void bumpVersion(String key, Duration ttl);
}