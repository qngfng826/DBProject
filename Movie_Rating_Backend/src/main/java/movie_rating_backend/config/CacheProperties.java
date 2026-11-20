package movie_rating_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 热点缓存配置。所有 TTL 单位为秒，可通过 application.yml 的 app.cache.* 覆盖。
 * 缓存总开关关闭时，容器中只存在 NoOpCacheClient，不会建立任何 Redis 连接。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private boolean enabled = false;

    private long hotTtlSeconds = 300;
    private long listTtlSeconds = 300;
    private long detailTtlSeconds = 300;
    private long searchTtlSeconds = 180;
    private long reportTtlSeconds = 600;
    private long nullTtlSeconds = 60;
    private long versionTtlSeconds = 86400;
    private long lockTtlSeconds = 10;
    private long circuitOpenSeconds = 30;

    public Duration hotTtl() {
        return Duration.ofSeconds(hotTtlSeconds);
    }

    public Duration listTtl() {
        return Duration.ofSeconds(listTtlSeconds);
    }

    public Duration detailTtl() {
        return Duration.ofSeconds(detailTtlSeconds);
    }

    public Duration searchTtl() {
        return Duration.ofSeconds(searchTtlSeconds);
    }

    public Duration reportTtl() {
        return Duration.ofSeconds(reportTtlSeconds);
    }

    public Duration nullTtl() {
        return Duration.ofSeconds(nullTtlSeconds);
    }

    public Duration versionTtl() {
        return Duration.ofSeconds(versionTtlSeconds);
    }
}