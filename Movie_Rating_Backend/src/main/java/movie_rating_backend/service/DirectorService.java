package movie_rating_backend.service;
import com.baomidou.mybatisplus.extension.service.IService;
import movie_rating_backend.entity.Director;
import movie_rating_backend.entity.Movie;
import java.util.List;

public interface DirectorService extends IService<Director> {
    public List<Movie> searchMovies(String name);
}

