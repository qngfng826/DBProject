package movie_rating_backend.service;

import movie_rating_backend.DTO.LoginDTO;
import movie_rating_backend.utils.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import movie_rating_backend.entity.User;

public interface UserService extends IService<User> {
    // 声明登录方法
    Result<?> login(LoginDTO loginDTO);

    // 删除用户：外键级联清其评论/评分后，补偿重算受影响电影的均分缓存
    boolean deleteUserWithCleanup(Integer userId);
}

