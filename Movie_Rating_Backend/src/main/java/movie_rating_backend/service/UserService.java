package movie_rating_backend.service;

import movie_rating_backend.DTO.LoginDTO;
import movie_rating_backend.utils.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import movie_rating_backend.entity.User;

public interface UserService extends IService<User> {
    // 声明登录方法
    Result<?> login(LoginDTO loginDTO);
}

