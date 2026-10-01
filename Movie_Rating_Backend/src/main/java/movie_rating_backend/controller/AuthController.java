package movie_rating_backend.controller;

import lombok.extern.slf4j.Slf4j;
import movie_rating_backend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import movie_rating_backend.service.UserService;
import movie_rating_backend.utils.Result;
import movie_rating_backend.DTO.LoginDTO;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin // 虽然全局配置了跨域，Controller 加一层保险
public class AuthController {

    @Autowired
    private UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO loginDTO) {
        log.info("=== AuthController.login 被调用 ===");
        log.info("请求用户名: {}", loginDTO.getUsername());

        // 直接调用 UserServiceImpl 中的 login 方法
        return userService.login(loginDTO);
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        log.info("=== AuthController.register 被调用 ===");
        log.info("注册用户名: {}", user.getUsername());
        log.info("用户邮箱: {}", user.getEmail());

        // 基本校验：密码为空时 BCrypt 会直接 NPE；admin 是系统保留的管理员用户名
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("用户名和密码不能为空");
        }
        if ("admin".equalsIgnoreCase(user.getUsername().trim())) {
            return Result.error("该用户名为系统保留，请更换");
        }

        try {
            // 加密密码
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            log.info("密码已加密，长度: {}", encodedPassword.length());

            user.setPassword(encodedPassword);
            boolean saved = userService.save(user);

            log.info("用户注册{}: {}", saved ? "成功" : "失败", user.getUsername());
            return Result.success("注册成功");
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("注册失败(可能用户名已存在): " + e.getMessage());
        }
    }
}
