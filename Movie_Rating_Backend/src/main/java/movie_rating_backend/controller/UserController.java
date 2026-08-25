package movie_rating_backend.controller;
import movie_rating_backend.entity.User;
import movie_rating_backend.service.UserService;
import movie_rating_backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<?> list() { return Result.success(userService.list()); }

    @PutMapping
    public Result<?> update(@RequestBody User user) { userService.updateById(user); return Result.success(); }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) { userService.removeById(id); return Result.success(); }
}

