package movie_rating_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import movie_rating_backend.entity.Actor;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import movie_rating_backend.entity.Movie;

public interface ActorMapper extends BaseMapper<Actor> {
    List<Movie> searchMovies(@Param("name") String name);
}

