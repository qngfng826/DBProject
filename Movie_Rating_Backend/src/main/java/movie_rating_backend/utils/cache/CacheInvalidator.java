package movie_rating_backend.utils.cache;

import movie_rating_backend.config.CacheProperties;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 写路径统一失效入口：
 * - 评分变化：详情（均分/userRating）、热度榜、报表，并更换列表版本号（列表含 Rating 列）
 * - 评论变化：详情（评论数）、热度榜（CommentCount）、报表（TotalComments）
 * - 电影管理端变化：同评分变化
 * - 用户删除：级联评分/评论，按受影响的电影逐个失效
 */
@Component
public class CacheInvalidator {

    private final CacheClient cacheClient;
    private final CacheProperties properties;

    public CacheInvalidator(CacheClient cacheClient, CacheProperties properties) {
        this.cacheClient = cacheClient;
        this.properties = properties;
    }

    public void onRatingWrite(Integer movieId) {
        evictMovieAndGlobals(movieId, true);
    }

    public void onCommentWrite(Integer movieId) {
        evictMovieAndGlobals(movieId, false);
    }

    public void onMovieWrite(Integer movieId) {
        evictMovieAndGlobals(movieId, true);
    }

    public void onUserDelete(Collection<Integer> movieIds) {
        CacheEvictionSupport.afterCommit(() -> {
            if (movieIds != null) {
                for (Integer movieId : movieIds) {
                    if (movieId != null) {
                        cacheClient.evict(CacheKeys.movieDetail(movieId));
                    }
                }
            }
            cacheClient.evict(CacheKeys.MOVIE_HOT, CacheKeys.REPORT_GENRE);
            cacheClient.bumpVersion(CacheKeys.MOVIE_VERSION, properties.versionTtl());
        });
    }

    private void evictMovieAndGlobals(Integer movieId, boolean bumpListVersion) {
        CacheEvictionSupport.afterCommit(() -> {
            if (movieId != null) {
                cacheClient.evict(CacheKeys.movieDetail(movieId));
            }
            cacheClient.evict(CacheKeys.MOVIE_HOT, CacheKeys.REPORT_GENRE);
            if (bumpListVersion) {
                cacheClient.bumpVersion(CacheKeys.MOVIE_VERSION, properties.versionTtl());
            }
        });
    }
}