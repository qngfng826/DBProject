package movie_rating_backend.controller;
import movie_rating_backend.entity.User;
import movie_rating_backend.service.UserService;
import movie_rating_backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<?> list() { return Result.success(userService.list()); }

    // 分页搜索接口（供后台用户管理使用，关键字匹配用户名或邮箱）
    @GetMapping("/search")
    public Result<IPage<User>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<User> pageObj = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("username", keyword).or().like("email", keyword);
        }
        queryWrapper.orderByAsc("UserId");

        IPage<User> result = userService.page(pageObj, queryWrapper);
        return Result.success(result);
    }

    @PutMapping
    public Result<?> update(@RequestBody User user) { userService.updateById(user); return Result.success(); }

    // 按 id 更新用户（供后台用户管理使用）
    @PutMapping("/{id}")
    public Result<String> updateById(@PathVariable Integer id, @RequestBody User user) {
        user.setUserId(id);
        // 前端传了新密码才重置密码；数据库存的是 BCrypt 哈希，明文需先加密。
        // 留空时置 null，避免 updateById 把空字符串当有效值清空密码
        if (StringUtils.hasText(user.getPassword()) && !user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        boolean success = userService.updateById(user);
        return success ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    // 按 id 删除用户（供后台用户管理使用）
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        boolean success = userService.removeById(id);
        return success ? Result.success("删除成功") : Result.error(500, "删除失败");
    }
}
