package movie_rating_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import movie_rating_backend.entity.Actor;
import movie_rating_backend.entity.Director;
import movie_rating_backend.entity.Movie;
import java.util.List;
import java.util.Map;

public interface MovieService extends IService<Movie> {
    Movie selectOneById(Integer id);

    /**
     * 根据电影ID获取电影详情（支持用户评分过滤）
     * @param id 电影ID
     * @param userId 用户ID（用于过滤用户自己的评分）
     * @return 电影详情
     */
    Movie selectOneById(Integer id, Integer userId);

    /**
     * 创建电影及其关联关系（演员和导演）
     * 使用事务确保数据一致性
     */
    void createMovieWithRelations(Movie movie, List<Actor> actors, List<Director> directors);

    /**
     * 更新电影及其关联关系
     * 使用事务确保数据一致性
     */
    void updateMovieWithRelations(Integer movieId, Movie movie, List<Actor> actors, List<Director> directors);

    /**
     * 删除电影及其关联关系
     * 使用事务确保数据一致性
     */
    void deleteMovieWithRelations(Integer movieId);

    /**
     * 搜索电影（支持关键字、类型、年份、排序）
     */
    Movie searchMovies(int page, int size, String keyword, String genre, Integer year, String sort);

    /**
     * 获取热门电影列表
     * 按平均评分和综合评分排序
     */
    List<Map<String, Object>> getHotMovies();
}
