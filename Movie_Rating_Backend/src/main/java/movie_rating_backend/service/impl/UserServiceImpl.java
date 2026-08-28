package movie_rating_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 登录校验逻辑
     */
    public Result login(LoginDTO loginDTO) {
        // 1. 根据前端传来的用户名去数据库查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(wrapper);

        // 2. 判断用户是否存在
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        // 3. 校验密码（使用 BCrypt 验证密码）
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误"); // 密码不匹配，返回错误
        }

        // 4. 密码正确，生成 JWT Token
        String token = JwtUtil.createToken(user.getUserId(), user.getUsername());

        // 5. 将 token 返回给前端
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("user", user);

        return Result.success(data);
    }
}


