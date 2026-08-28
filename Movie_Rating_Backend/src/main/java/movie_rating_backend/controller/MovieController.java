package movie_rating_backend.controller;

import movie_rating_backend.annotation.AuthRequired;
import movie_rating_backend.entity.Actor;
import movie_rating_backend.entity.Director;
import movie_rating_backend.entity.Movie;
import movie_rating_backend.mapper.MovieMapper;
import movie_rating_backend.service.MovieService;
import movie_rating_backend.utils.Result;
import movie_rating_backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
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

    // 1. 获取列表（简单列表，用于某些场景）
    @GetMapping("/list")
    public Result<?> List() {
        return Result.success(movieService.list());
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
            }
        }

        IPage<Movie> result = movieMapper.selectPage(pageObj, queryWrapper);
        return Result.success(result);
    }

    // 3. 详情接口
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = null;
        String token = request.getHeader("Authorization");
        if (token != null) {
            try {
                userId = JwtUtil.getUserId(token);
            } catch (Exception e) {
                // token 无效或过期，userId 为 null
            }
        }
        return Result.success(movieService.selectOneById(id, userId));
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
    @AuthRequired
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(@RequestBody Movie movie) {
        // 请求体只有一个，actors/directors 数组嵌在 movie JSON 中
        List<Actor> filteredActors = movie.getActors() != null ? movie.getActors().stream()
                .filter(a -> a.getActorId() != null)
                .collect(java.util.stream.Collectors.toList()) : List.of();

        List<Director> filteredDirectors = movie.getDirectors() != null ? movie.getDirectors().stream()
                .filter(d -> d.getDirectorId() != null)
                .collect(java.util.stream.Collectors.toList()) : List.of();

        movieService.createMovieWithRelations(movie, filteredActors, filteredDirectors);
        return Result.success("添加成功");
    }

    // 6. 更新电影（使用批量插入 + 事务）
    @PutMapping("/{id}")
    @AuthRequired
    @Transactional(rollbackFor = Exception.class)
    public Result<String> update(@PathVariable Integer id,
                                @RequestBody Movie movie) {
        // 请求体只有一个，actors/directors 数组嵌在 movie JSON 中
        List<Actor> filteredActors = movie.getActors() != null ? movie.getActors().stream()
                .filter(a -> a.getActorId() != null)
                .collect(java.util.stream.Collectors.toList()) : List.of();

        List<Director> filteredDirectors = movie.getDirectors() != null ? movie.getDirectors().stream()
                .filter(d -> d.getDirectorId() != null)
                .collect(java.util.stream.Collectors.toList()) : List.of();

        movieService.updateMovieWithRelations(id, movie, filteredActors, filteredDirectors);
        return Result.success("更新成功");
    }

    // 7. 删除电影（使用事务）
    @DeleteMapping("/{id}")
    @AuthRequired
    @Transactional(rollbackFor = Exception.class)
    public Result<String> delete(@PathVariable Integer id) {
        movieService.deleteMovieWithRelations(id);
        return Result.success("删除成功");
    }
}
