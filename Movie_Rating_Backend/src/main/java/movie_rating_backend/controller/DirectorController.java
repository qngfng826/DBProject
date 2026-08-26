package movie_rating_backend.controller;

import movie_rating_backend.entity.Director;
import movie_rating_backend.service.DirectorService;
import movie_rating_backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/director")
public class DirectorController {

    @Autowired
    private DirectorService directorService;

    @GetMapping("/{name}/movies")
    public Result<?> searchMovies(@PathVariable String name) {
        return Result.success(directorService.searchMovies(name));
    }


    // 1. 搜索接口（适配前端 director.js 的 getDirectorList -> /director/search）
    @GetMapping("/search")
    public Result<IPage<Director>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Director> pageObj = new Page<>(page, size);
        QueryWrapper<Director> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("name", keyword);
        }

        IPage<Director> result = directorService.page(pageObj, queryWrapper);
        return Result.success(result);
    }

    // 2. 简单列表接口
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(directorService.list());
    }

    // 3. 详情接口
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        return Result.success(directorService.getById(id));
    }

    // === 新增接口：修复增删改功能 ===

    // 4. 新增导演
    @PostMapping
    public Result<String> add(@RequestBody Director director) {
        boolean success = directorService.save(director);
        return success ? Result.success("添加成功") : Result.error(500, "添加失败");
    }

    // 5. 更新导演
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Director director) {
        director.setDirectorId(id);
        boolean success = directorService.updateById(director);
        return success ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    // 6. 删除导演
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = directorService.removeById(id);
        return success ? Result.success("删除成功") : Result.error(500, "删除失败");
    }
}
