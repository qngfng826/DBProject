package movie_rating_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import movie_rating_backend.entity.Director;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import movie_rating_backend.entity.Movie;

public interface DirectorMapper extends BaseMapper<Director> {
    List<Map<String, Object>> findMoviesByDirectorId(@Param("directorId") Integer directorId);
    List<Movie> searchMovies(@Param("name") String name);
}
