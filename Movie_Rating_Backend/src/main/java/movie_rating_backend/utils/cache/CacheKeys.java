package movie_rating_backend.utils.cache;

/**
 * 缓存 key 统一定义。列表 / 搜索带版本号，写操作更换版本即可整体失效，避免 SCAN/KEYS。
 */
public final class CacheKeys {

    public static final String MOVIE_HOT = "mr:movie:hot";
    public static final String MOVIE_VERSION = "mr:movie:ver";
    public static final String REPORT_GENRE = "mr:report:genre";

    private static final String MOVIE_DETAIL_PREFIX = "mr:movie:detail:";
    private static final String MOVIE_LIST_PREFIX = "mr:movie:list:v";
    private static final String MOVIE_SEARCH_PREFIX = "mr:movie:search:v";

    private CacheKeys() {
    }

    public static String movieDetail(Integer movieId) {
        return MOVIE_DETAIL_PREFIX + movieId;
    }

    public static String movieList(String version) {
        return MOVIE_LIST_PREFIX + version;
    }

    public static String movieSearch(String version, int page, int size,
                                     String genre, Integer year, String sort) {
        return MOVIE_SEARCH_PREFIX + version
                + ":p" + page
                + ":s" + size
                + ":g" + safe(genre)
                + ":y" + (year == null ? "all" : year)
                + ":o" + safe(sort);
    }

    private static String safe(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "all";
        }
        return value.trim().replace(':', '_');
    }
}