package movie_rating_backend.DTO;

import lombok.Data;
import movie_rating_backend.entity.Movie;

import java.util.List;

/**
 * 分页搜索的缓存载体。只保留重建 MyBatis-Plus Page 所需的字段，
 * 避免直接序列化 IPage 实现类带来的兼容问题。
 */
@Data
public class CachedPage {

    private List<Movie> records;
    private long total;
    private long current;
    private long size;

    public CachedPage() {
    }

    public CachedPage(List<Movie> records, long total, long current, long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
    }
}