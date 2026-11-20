package movie_rating_backend.utils.cache;

import movie_rating_backend.DTO.CachedPage;
import movie_rating_backend.config.CacheConfig;
import movie_rating_backend.entity.Actor;
import movie_rating_backend.entity.Director;
import movie_rating_backend.entity.Movie;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用生产环境同一份 ObjectMapper（CacheConfig.redisObjectMapper）验证缓存载体的序列化往返，
 * 提前暴露 Jackson 默认类型信息在 Movie / List / Map / CachedPage / LocalDate 上的问题。
 */
class CacheSerializationTest {

    private final GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(CacheConfig.redisObjectMapper());

    @Test
    void movieRoundTrips() {
        Movie movie = sampleMovie();

        Object restored = serializer.deserialize(serializer.serialize(movie));

        assertTrue(restored instanceof Movie);
        Movie cached = (Movie) restored;
        assertEquals(movie.getMovieId(), cached.getMovieId());
        assertEquals(movie.getTitle(), cached.getTitle());
        assertEquals(0, movie.getRating().compareTo(cached.getRating()));
    }

    @Test
    void movieListRoundTrips() {
        List<Movie> movies = new ArrayList<>();
        movies.add(sampleMovie());

        Object restored = serializer.deserialize(serializer.serialize(movies));

        assertTrue(restored instanceof List);
        List<?> list = (List<?>) restored;
        assertEquals(1, list.size());
        assertTrue(list.get(0) instanceof Movie);
    }

    @Test
    void hotListRoundTrips() {
        List<Map<String, Object>> hot = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("movieId", 13);
        row.put("title", "Test Movie");
        row.put("rating", new BigDecimal("9.10"));
        row.put("CommentCount", 7L);
        hot.add(row);

        Object restored = serializer.deserialize(serializer.serialize(hot));

        assertTrue(restored instanceof List);
        Object first = ((List<?>) restored).get(0);
        assertTrue(first instanceof Map);
        Map<?, ?> cached = (Map<?, ?>) first;
        assertEquals(13, ((Number) cached.get("movieId")).intValue());
        assertNotNull(cached.get("rating"));
        assertEquals(0, new BigDecimal("9.10").compareTo(new BigDecimal(cached.get("rating").toString())));
        assertEquals(7, ((Number) cached.get("CommentCount")).intValue());
    }

    @Test
    void reportMapRoundTrips() {
        Map<String, Object> report = new LinkedHashMap<>();
        Map<String, Object> global = new LinkedHashMap<>();
        global.put("MaxRating", 10);
        global.put("MinRating", 1);
        global.put("TotalComments", 12L);
        report.put("globalStats", global);

        List<Map<String, Object>> summary = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Genre", "剧情");
        row.put("Count", 3L);
        summary.add(row);
        report.put("summary", summary);

        Object restored = serializer.deserialize(serializer.serialize(report));

        assertTrue(restored instanceof Map);
        Map<?, ?> cached = (Map<?, ?>) restored;
        assertTrue(cached.get("globalStats") instanceof Map);
        assertTrue(cached.get("summary") instanceof List);
    }

    @Test
    void movieWithDateFieldsRoundTrips() {
        // 详情接口的缓存值带 Director/Actor 集合，birthDate 是 java.time.LocalDate（回归点）
        Movie movie = sampleMovie();

        Object restored = serializer.deserialize(serializer.serialize(movie));

        assertTrue(restored instanceof Movie);
        Movie cached = (Movie) restored;
        assertEquals(1, cached.getDirectors().size());
        assertEquals(LocalDate.of(1970, 1, 1), cached.getDirectors().get(0).getBirthDate());
        assertEquals(1, cached.getActors().size());
        assertEquals(LocalDate.of(1980, 2, 2), cached.getActors().get(0).getBirthDate());
        assertEquals("男主角", cached.getActors().get(0).getRoleName());
    }

    @Test
    void cachedPageRoundTrips() {
        List<Movie> records = new ArrayList<>();
        records.add(sampleMovie());
        CachedPage page = new CachedPage(records, 1L, 1L, 10L);

        Object restored = serializer.deserialize(serializer.serialize(page));

        assertTrue(restored instanceof CachedPage);
        CachedPage cached = (CachedPage) restored;
        assertEquals(1L, cached.getTotal());
        assertEquals(10L, cached.getSize());
        assertEquals(1, cached.getRecords().size());
        assertTrue(cached.getRecords().get(0) instanceof Movie);
    }

    private Movie sampleMovie() {
        Movie movie = new Movie();
        movie.setMovieId(13);
        movie.setTitle("Test Movie");
        movie.setReleaseYear(2024);
        movie.setDuration(120);
        movie.setGenre("剧情");
        movie.setRating(new BigDecimal("8.75"));

        Director director = new Director();
        director.setDirectorId(1);
        director.setName("导演甲");
        director.setGender("男");
        director.setBirthDate(LocalDate.of(1970, 1, 1));
        director.setNationality("中国");
        movie.setDirectors(new ArrayList<>(Collections.singletonList(director)));

        Actor actor = new Actor();
        actor.setActorId(2);
        actor.setName("演员乙");
        actor.setGender("女");
        actor.setBirthDate(LocalDate.of(1980, 2, 2));
        actor.setNationality("中国");
        actor.setRoleName("男主角");
        movie.setActors(new ArrayList<>(Collections.singletonList(actor)));
        return movie;
    }
}