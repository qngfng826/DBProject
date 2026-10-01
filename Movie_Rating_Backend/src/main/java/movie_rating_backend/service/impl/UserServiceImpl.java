package movie_rating_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import movie_rating_backend.DTO.LoginDTO;
import movie_rating_backend.entity.User;
import movie_rating_backend.mapper.UserMapper;
import movie_rating_backend.service.UserService;
import movie_rating_backend.utils.JwtUtil;
import movie_rating_backend.utils.Result;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 登录校验逻辑
     */
    public Result login(LoginDTO loginDTO) {
        log.info("=== UserServiceImpl.login 开始 ===");
        log.info("尝试登录用户: {}", loginDTO.getUsername());

        // 1. 根据前端传来的用户名去数据库查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            log.warn("用户不存在: {}", loginDTO.getUsername());
            return Result.error("用户名或密码错误");
        }

        log.info("找到用户: {}", user.getUsername());
        log.info("用户ID: {}", user.getUserId());

        // 2. 校验密码（使用 BCrypt 验证密码）
        boolean matches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        if (!matches) {
            log.warn("密码不匹配，用户: {}", loginDTO.getUsername());
            return Result.error("用户名或密码错误"); // 密码不匹配，返回错误
        }

        log.info("密码验证通过");

        // 3. 密码正确，生成 JWT Token
        // 判断用户是否为管理员
        String role = "user";
        if ("admin".equals(user.getUsername())) {
            role = "admin";
        }

        log.info("用户角色: {}", role);

        String token = JwtUtil.createToken(user.getUserId(), user.getUsername(), role);
        log.info("JWT token 生成成功");

        // 4. 将 token 返回给前端
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("user", user);

        log.info("=== 登录成功，返回 token ===");
        return Result.success(data);
    }
}


