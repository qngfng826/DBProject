package movie_rating_backend.controller;

import movie_rating_backend.annotation.AuthRequired;
import movie_rating_backend.DTO.CachedPage;
import movie_rating_backend.config.CacheProperties;
import movie_rating_backend.entity.Actor;
import movie_rating_backend.entity.Director;
import movie_rating_backend.entity.Movie;
import movie_rating_backend.entity.Rating;
import movie_rating_backend.mapper.MovieMapper;
import movie_rating_backend.service.MovieService;
import movie_rating_backend.service.RatingService;
import movie_rating_backend.utils.Result;
import movie_rating_backend.utils.JwtUtil;
import movie_rating_backend.utils.cache.CacheClient;
import movie_rating_backend.utils.cache.CacheKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


@RestController
@RequestMapping("/api/movie")
public class MovieController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private CacheClient cacheClient;

    @Autowired
    private CacheProperties cacheProperties;

    // 1. 获取列表（简单列表，用于某些场景）
    @GetMapping("/list")
    public Result<?> List() {
        String version = cacheClient.getOrCreateVersion(CacheKeys.MOVIE_VERSION, cacheProperties.versionTtl());
        List<Movie> movies = cacheClient.getOrLoad(CacheKeys.movieList(version),
                cacheProperties.listTtl(), movieService::list);
        return Result.success(movies);
    }

    // 2. 搜索接口（配合前端 MovieManage.vue 的 getMovieList -> /movie/search）
    @GetMapping("/search")
    public Result<IPage<Movie>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String sort) {

        // keyword 搜索的 key 空间无界，不缓存；只缓存无关键词的常规分页（限制页大小/页码，防 key 膨胀）
        boolean cacheable = !StringUtils.hasText(keyword)
                && page != null && page >= 1 && page <= 20
                && size != null && size >= 1 && size <= 50;
        if (!cacheable) {
            return Result.success(executeSearch(page, size, keyword, genre, year, sort));
        }

        String version = cacheClient.getOrCreateVersion(CacheKeys.MOVIE_VERSION, cacheProperties.versionTtl());
        String cacheKey = CacheKeys.movieSearch(version, page, size, genre, year, sort);
        CachedPage cached = cacheClient.getOrLoad(cacheKey, cacheProperties.searchTtl(),
                () -> toCachedPage(executeSearch(page, size, keyword, genre, year, sort)));
        if (cached == null) {
            return Result.success(executeSearch(page, size, keyword, genre, year, sort));
        }

        Page<Movie> result = new Page<>(cached.getCurrent(), cached.getSize());
        result.setRecords(cached.getRecords());
        result.setTotal(cached.getTotal());
        return Result.success(result);
    }

    private IPage<Movie> executeSearch(Integer page, Integer size, String keyword,
                                       String genre, Integer year, String sort) {
        Page<Movie> pageObj = new Page<>(page, size);
        QueryWrapper<Movie> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("title", keyword);
        }
        if (StringUtils.hasText(genre)) {
            queryWrapper.like("genre", genre);
        }
        if (year != null) {
            queryWrapper.eq("releaseYear", year);
        }
        if (StringUtils.hasText(sort)) {
            switch (sort) {
                case "rating_desc":
                    queryWrapper.orderByDesc("rating");
                    break;
                case "rating_asc":
                    queryWrapper.orderByAsc("rating");
                    break;
                case "year_desc":
                    queryWrapper.orderByDesc("releaseYear");
                    break;
                case "year_asc":
                    queryWrapper.orderByAsc("releaseYear");
                    break;
                default:
                    break;
            }
        }

        return movieMapper.selectPage(pageObj, queryWrapper);
    }

    private CachedPage toCachedPage(IPage<Movie> page) {
        return new CachedPage(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    // 3. 详情接口
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = null;
        String token = request.getHeader("Authorization");
        if (token != null) {
            try {
                userId = jwtUtil.getUserId(token);
            } catch (Exception e) {
                // token 无效或过期，userId 为 null
            }
        }

        // 缓存共享部分（不含 userRating，避免不同用户串号）；用户评分命中后按索引单行查询补上
        Movie movie = cacheClient.getOrLoad(CacheKeys.movieDetail(id), cacheProperties.detailTtl(),
                () -> movieService.selectOneById(id, null));
        if (movie != null && userId != null) {
            Rating rating = ratingService.getUserMovieRating(userId, id);
            movie.setUserRating(rating == null || rating.getScore() == null
                    ? null
                    : BigDecimal.valueOf(rating.getScore()));
        }
        return Result.success(movie);
    }

    // 4. 热门电影接口（使用优化后的查询）
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotMovies() {
        List<Map<String, Object>> hotMovies = movieService.getHotMovies();
        return Result.success(hotMovies);
    }

    // === 新增以下接口：解决添加、编辑、删除功能失效的问题 ===

    // 5. 新增电影（使用批量插入 + 事务）
    @PostMapping
    @AuthRequired(admin = true)
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(@RequestBody Movie movie) {
        // 请求体只有一个，actors/directors 数组嵌在 movie JSON 中
        List<Actor> filteredActors = movie.getActors() != null ? movie.getActors().stream()
                .filter(a -> a.getActorId() != null)
                .collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList();

        List<Director> filteredDirectors = movie.getDirectors() != null ? movie.getDirectors().stream()
                .filter(d -> d.getDirectorId() != null)
                .collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList();

        movieService.createMovieWithRelations(movie, filteredActors, filteredDirectors);
        return Result.success("添加成功");
    }

    // 6. 更新电影（使用批量插入 + 事务）
    @PutMapping("/{id}")
    @AuthRequired(admin = true)
    @Transactional(rollbackFor = Exception.class)
    public Result<String> update(@PathVariable Integer id,
                                @RequestBody Movie movie) {
        // 请求体只有一个，actors/directors 数组嵌在 movie JSON 中
        List<Actor> filteredActors = movie.getActors() != null ? movie.getActors().stream()
                .filter(a -> a.getActorId() != null)
                .collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList();

        List<Director> filteredDirectors = movie.getDirectors() != null ? movie.getDirectors().stream()
                .filter(d -> d.getDirectorId() != null)
                .collect(java.util.stream.Collectors.toList()) : java.util.Collections.emptyList();

        movieService.updateMovieWithRelations(id, movie, filteredActors, filteredDirectors);
        return Result.success("更新成功");
    }

    // 7. 删除电影（使用事务）
    @DeleteMapping("/{id}")
    @AuthRequired(admin = true)
    @Transactional(rollbackFor = Exception.class)
    public Result<String> delete(@PathVariable Integer id) {
        movieService.deleteMovieWithRelations(id);
        return Result.success("删除成功");
    }
}
