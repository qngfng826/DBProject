package movie_rating_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import movie_rating_backend.entity.Movie;
import movie_rating_backend.mapper.MovieMapper;
import movie_rating_backend.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovieServiceImpl extends ServiceImpl<MovieMapper, Movie> implements MovieService {

    @Autowired
    private MovieMapper movieMapper;

    @Override
    public Movie selectOneById(Integer id) {
        return movieMapper.selectOneById(id);
    }
}

