package movie_rating_backend.utils.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoOpCacheClientTest {

    @Test
    void alwaysLoadsFromSource() {
        NoOpCacheClient client = new NoOpCacheClient();
        AtomicInteger loads = new AtomicInteger();

        String first = client.getOrLoad("k", Duration.ofSeconds(30), () -> {
            loads.incrementAndGet();
            return "db";
        });
        String second = client.getOrLoad("k", Duration.ofSeconds(30), () -> {
            loads.incrementAndGet();
            return "db";
        });

        assertEquals("db", first);
        assertEquals("db", second);
        assertEquals(2, loads.get());
        assertEquals("0", client.getOrCreateVersion("v", Duration.ofSeconds(1)));

        // 这些调用都应为空操作，不抛异常
        client.set("k", "v", Duration.ofSeconds(1));
        client.evict("k");
        client.bumpVersion("v", Duration.ofSeconds(1));
    }
}