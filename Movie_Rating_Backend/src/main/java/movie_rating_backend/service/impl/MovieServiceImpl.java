package movie_rating_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import movie_rating_backend.entity.Actor;
import movie_rating_backend.entity.Director;
import movie_rating_backend.entity.Movie;
import movie_rating_backend.entity.MovieActor;
import movie_rating_backend.entity.MovieDirector;
import movie_rating_backend.mapper.MovieMapper;
import movie_rating_backend.mapper.MovieActorMapper;
import movie_rating_backend.mapper.MovieDirectorMapper;
import movie_rating_backend.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements MovieService {

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private MovieActorMapper movieActorMapper;

    @Autowired
    private MovieDirectorMapper movieDirectorMapper;

    @Override
    public Movie selectOneById(Integer id) {
        return movieMapper.selectOneById(id, null);
    }

    @Override
    public Movie selectOneById(Integer id, Integer userId) {
        return movieMapper.selectOneById(id, userId);
    }

    /**
     * 创建电影及其关联关系（演员和导演）
     * 使用事务确保数据一致性
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createMovieWithRelations(Movie movie, List<Actor> actors, List<Director> directors) {
        // 1. 保存电影主表
        movieMapper.insert(movie);

        // 2. 批量插入导演关联
        if (directors != null && !directors.isEmpty()) {
            List<MovieDirector> directorRelations = directors.stream()
                    .filter(d -> d.getDirectorId() != null)
                    .map(d -> new MovieDirector(movie.getMovieId(), d.getDirectorId()))
                    .collect(Collectors.toList());
            if (!directorRelations.isEmpty()) {
                directorRelations.forEach(movieDirectorMapper::insert);
            }
        }

        // 3. 批量插入演员关联
        if (actors != null && !actors.isEmpty()) {
            List<MovieActor> actorRelations = actors.stream()
                    .filter(a -> a.getActorId() != null)
                    .map(a -> new MovieActor(movie.getMovieId(), a.getActorId(), a.getRoleName()))
                    .collect(Collectors.toList());
            if (!actorRelations.isEmpty()) {
                actorRelations.forEach(movieActorMapper::insert);
            }
        }
    }

    /**
     * 更新电影及其关联关系
     * 使用事务确保数据一致性
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMovieWithRelations(Integer movieId, Movie movie, List<Actor> actors, List<Director> directors) {
        // 1. 更新电影主表
        movie.setMovieId(movieId);
        movieMapper.updateById(movie);

        // 2. 删除旧的演员关联
        movieActorMapper.delete(new LambdaQueryWrapper<MovieActor>()
                .eq(MovieActor::getMovieId, movieId));

        // 3. 批量插入新的演员关联
        if (actors != null && !actors.isEmpty()) {
            List<MovieActor> actorRelations = actors.stream()
                    .filter(a -> a.getActorId() != null)
                    .map(a -> new MovieActor(movieId, a.getActorId(), a.getRoleName()))
                    .collect(Collectors.toList());
            if (!actorRelations.isEmpty()) {
                actorRelations.forEach(movieActorMapper::insert);
            }
        }

        // 4. 删除旧的导演关联
        movieDirectorMapper.delete(new LambdaQueryWrapper<MovieDirector>()
                .eq(MovieDirector::getMovieId, movieId));

        // 5. 批量插入新的导演关联
        if (directors != null && !directors.isEmpty()) {
            List<MovieDirector> directorRelations = directors.stream()
                    .filter(d -> d.getDirectorId() != null)
                    .map(d -> new MovieDirector(movieId, d.getDirectorId()))
                    .collect(Collectors.toList());
            if (!directorRelations.isEmpty()) {
                directorRelations.forEach(movieDirectorMapper::insert);
            }
        }
    }

    /**
     * 删除电影及其关联关系
     * 使用事务确保数据一致性
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMovieWithRelations(Integer movieId) {
        // 1. 删除演员关联
        movieActorMapper.delete(new LambdaQueryWrapper<MovieActor>()
                .eq(MovieActor::getMovieId, movieId));

        // 2. 删除导演关联
        movieDirectorMapper.delete(new LambdaQueryWrapper<MovieDirector>()
                .eq(MovieDirector::getMovieId, movieId));

        // 3. 删除电影
        movieMapper.deleteById(movieId);
    }

    /**
     * 搜索电影（支持关键字、类型、年份、排序）
     */
    @Override
    public Movie searchMovies(int page, int size, String keyword, String genre, Integer year, String sort) {
        LambdaQueryWrapper<Movie> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Movie::getTitle, keyword);
        }
        if (StringUtils.hasText(genre)) {
            queryWrapper.like(Movie::getGenre, genre);
        }
        if (year != null) {
            queryWrapper.eq(Movie::getReleaseYear, year);
        }
        if (StringUtils.hasText(sort)) {
            switch (sort) {
                case "rating_desc":
                    queryWrapper.orderByDesc(Movie::getRating);
                    break;
                case "rating_asc":
                    queryWrapper.orderByAsc(Movie::getRating);
                    break;
                case "year_desc":
                    queryWrapper.orderByDesc(Movie::getReleaseYear);
                    break;
                case "year_asc":
                    queryWrapper.orderByAsc(Movie::getReleaseYear);
                    break;
            }
        }

        return movieMapper.selectOne(queryWrapper);
    }

    /**
     * 获取热门电影列表
     * 按平均评分和综合评分排序
     */
    @Override
    public List<Map<String, Object>> getHotMovies() {
        return movieMapper.getHotMovies();
    }
}
