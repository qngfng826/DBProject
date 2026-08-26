package movie_rating_backend.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import movie_rating_backend.entity.Director;
import movie_rating_backend.mapper.DirectorMapper;
import movie_rating_backend.service.DirectorService;
import org.springframework.stereotype.Service;
import java.util.List;
import movie_rating_backend.entity.Movie;

@Service
public class DirectorServiceImpl extends ServiceImpl<DirectorMapper, Director> implements DirectorService {
    @Override
    public List<Movie> searchMovies(String name) {
        return baseMapper.searchMovies(name);
    }
}

