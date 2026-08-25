package movie_rating_backend.controller;

import movie_rating_backend.entity.Actor;
import movie_rating_backend.service.ActorService;
import movie_rating_backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/actor")
public class ActorController {

    @Autowired
    private ActorService actorService;

    @GetMapping("/{name}/movies")
    public Result<?> searchMovies(@PathVariable String name) {
        return Result.success(actorService.searchMovies(name));
    }

    // 1. 搜索接口（适配前端 actor.js 的 getActorList -> /actor/search）
    @GetMapping("/search")
    public Result<IPage<Actor>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Actor> pageObj = new Page<>(page, size);
        QueryWrapper<Actor> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("name", keyword);
        }

        IPage<Actor> result = actorService.page(pageObj, queryWrapper);
        return Result.success(result);
    }

    // 2. 简单列表接口
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(actorService.list());
    }

    // 3. 详情接口
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Integer id) {
        return Result.success(actorService.getById(id));
    }

    // === 新增接口：修复增删改功能 ===

    // 4. 新增演员
    @PostMapping
    public Result<String> add(@RequestBody Actor actor) {
        boolean success = actorService.save(actor);
        return success ? Result.success("添加成功") : Result.error(500, "添加失败");
    }

    // 5. 更新演员
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Integer id, @RequestBody Actor actor) {
        actor.setActorId(id);
        boolean success = actorService.updateById(actor);
        return success ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    // 6. 删除演员
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = actorService.removeById(id);
        return success ? Result.success("删除成功") : Result.error(500, "删除失败");
    }
}
