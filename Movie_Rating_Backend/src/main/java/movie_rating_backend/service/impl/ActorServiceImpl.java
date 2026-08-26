package movie_rating_backend.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import movie_rating_backend.entity.Actor;
import movie_rating_backend.entity.Movie;
import movie_rating_backend.mapper.ActorMapper;
import movie_rating_backend.service.ActorService;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class ActorServiceImpl extends ServiceImpl<ActorMapper, Actor> implements ActorService {
    @Override
    public List<Movie> searchMovies(String name) {
        return baseMapper.searchMovies(name);
    }
}

