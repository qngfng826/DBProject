package movie_rating_backend.controller;

import movie_rating_backend.entity.Movie;
import movie_rating_backend.entity.Actor;
import movie_rating_backend.entity.Director;
import movie_rating_backend.mapper.MovieMapper;
import movie_rating_backend.utils.Result;
import movie_rating_backend.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/movie")
public class MovieController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private MovieMapper movieMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

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
                case "rating_desc": queryWrapper.orderByDesc("rating"); break;
                case "rating_asc": queryWrapper.orderByAsc("rating"); break;
                case "year_desc": queryWrapper.orderByDesc("releaseYear"); break;
                case "year_asc": queryWrapper.orderByAsc("releaseYear"); break;
            }
        }

        IPage<Movie> result = movieMapper.selectPage(pageObj, queryWrapper);
        return Result.success(result);
    }

    // 3. 详情接口
    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Integer id) {
        return Result.success(movieService.selectOneById(id));
    }

    // 4. 热门电影接口
    @GetMapping("/hot")
    public Result<List<Map<String, Object>>> getHotMovies() {
        String sql = "SELECT * FROM v_hot_movies";
        return Result.success(jdbcTemplate.queryForList(sql));
    }

    // === 新增以下接口：解决添加、编辑、删除功能失效的问题 ===

    // 5. 新增电影
    @PostMapping
    public Result<String> add(@RequestBody Movie movie) {
        boolean success = movieService.save(movie);
        if (!success) return Result.error(500, "添加失败");
        saveRelations(movie.getMovieId(), movie.getActors(), movie.getDirectors());
        return Result.success("添加成功");
    }

    // 6. 更新电影
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Movie movie) {
        movie.setMovieId(id);
        boolean success = movieService.updateById(movie);
        if (!success) return Result.error(500, "更新失败");
        jdbcTemplate.update("DELETE FROM movie_actor1707 WHERE movieId = ?", id);
        jdbcTemplate.update("DELETE FROM movie_director1707 WHERE movieId = ?", id);
        saveRelations(id, movie.getActors(), movie.getDirectors());
        return Result.success("更新成功");
    }

    // 7. 删除电影
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = movieService.removeById(id);
        return success ? Result.success("删除成功") : Result.error(500, "删除失败");
    }

    // 提取出的私有方法：批量插入关联表
    private void saveRelations(Integer movieId, List<Actor> actors, List<Director> directors) {
        if (directors != null) {
            for (Director d : directors) {
                if (d.getDirectorId() != null) {
                    jdbcTemplate.update("INSERT INTO movie_director1707 (MovieID, DirectorID) VALUES (?, ?)", movieId, d.getDirectorId());
                }
            }
        }
        if (actors != null) {
            for (Actor a : actors) {
                if (a.getActorId() != null) {
                    jdbcTemplate.update("INSERT INTO movie_actor1707 (MovieID, ActorID, RoleName) VALUES (?, ?, ?)",
                            movieId, a.getActorId(), a.getRoleName());
                }
            }
        }
    }
}
