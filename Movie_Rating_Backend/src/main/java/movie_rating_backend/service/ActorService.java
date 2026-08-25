package movie_rating_backend.service;
import com.baomidou.mybatisplus.extension.service.IService;
import movie_rating_backend.entity.Actor;
import movie_rating_backend.entity.Movie;
import java.util.List;


public interface ActorService extends IService<Actor> {
    public List<Movie> searchMovies(String name);
}

