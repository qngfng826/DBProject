package movie_rating_backend.controller;

import movie_rating_backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import movie_rating_backend.service.UserService;
import movie_rating_backend.utils.Result;
import movie_rating_backend.DTO.LoginDTO;

@RestController
@RequestMapping("/api")
@CrossOrigin // 虽然全局配置了跨域，Controller 加一层保险
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO) {
        // 直接调用 UserServiceImpl 中的 login 方法
        return userService.login(loginDTO);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.save(user);
            result.put("code", 200);
            result.put("msg", "注册成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "注册失败(可能用户名已存在)");
        }
        return result;
    }
}

