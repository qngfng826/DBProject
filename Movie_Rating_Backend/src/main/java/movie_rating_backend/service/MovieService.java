package movie_rating_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import movie_rating_backend.entity.Movie;

public interface MovieService extends IService<Movie> {
    Movie selectOneById(Integer id);
}

