package movie_rating_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import movie_rating_backend.DTO.LoginDTO;
import movie_rating_backend.entity.Rating;
import movie_rating_backend.entity.User;
import movie_rating_backend.mapper.RatingMapper;
import movie_rating_backend.mapper.UserMapper;
import movie_rating_backend.service.UserService;
import movie_rating_backend.utils.JwtUtil;
import movie_rating_backend.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RatingMapper ratingMapper;

    /**
     * 登录校验逻辑
     */
    public Result login(LoginDTO loginDTO) {
        // 1. 根据前端传来的用户名去数据库查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            log.warn("用户不存在: {}", loginDTO.getUsername());
            return Result.error("用户名或密码错误");
        }

        // 2. 校验密码（使用 BCrypt 验证密码）
        boolean matches = passwordEncoder.matches(loginDTO.getPassword(), user.getPassword());
        if (!matches) {
            log.warn("密码不匹配，用户: {}", loginDTO.getUsername());
            return Result.error("用户名或密码错误"); // 密码不匹配，返回错误
        }

        // 3. 密码正确，生成 JWT Token
        // 判断用户是否为管理员
        String role = "user";
        if ("admin".equals(user.getUsername())) {
            role = "admin";
        }

        String token = jwtUtil.createToken(user.getUserId(), user.getUsername(), role);

        // 4. 将 token 返回给前端
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getUserId());
        data.put("user", user);

        return Result.success(data);
    }

    /**
     * 删除用户。comment1707/rating1707 的外键为 ON DELETE CASCADE，删用户即级联清其评论与评分；
     * 但数据库级联不触发均分重算（原触发器职责已迁至服务层），故删前记下所评电影、删后逐个重算 movie1707.Rating。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteUserWithCleanup(Integer userId) {
        Set<Integer> ratedMovieIds = ratingMapper.selectList(
                        Wrappers.<Rating>lambdaQuery().eq(Rating::getUserId, userId))
                .stream()
                .map(Rating::getMovieId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        boolean success = this.removeById(userId);
        if (success) {
            ratedMovieIds.forEach(ratingMapper::updateMovieRatingFromAvg);
        }
        return success;
    }
}


