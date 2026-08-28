package movie_rating_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import movie_rating_backend.entity.MovieRating;
import movie_rating_backend.entity.Rating;
import movie_rating_backend.mapper.RatingMapper;
import movie_rating_backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;



@Service
@Transactional
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating> implements RatingService {

    @Autowired
    private RatingMapper ratingMapper;

    @Override
    public void rateMovie(Rating rating) {
        // 调用 RatingMapper.xml 中定义的 insertOrUpdate 方法
        ratingMapper.insertOrUpdate(rating);
    }
    @Override
    public void updateRating(Rating rating) {
        ratingMapper.updateRating(rating);
    }

    @Override
    public Rating getUserMovieRating(Integer userId, Integer movieId) {
        return ratingMapper.getUserMovieRating(userId, movieId);
    }
    @Override
    public List<MovieRating> getUserRatings(Integer userId) {
        return ratingMapper.getUserRatings(userId);
    }
    @Override
    public Double getAverageRating(Integer movieId) {
        return ratingMapper.getAverageRating(movieId);
    }

    @Override
    public void removeRating(Integer ratingId) {
        this.removeById(ratingId);
    }
    @Override
    public void removeByUserIdAndMovieId(Integer userId, Integer movieId) {
        this.remove(Wrappers.<Rating>lambdaQuery()
                .eq(Rating::getUserId, userId)
                .eq(Rating::getMovieId, movieId));
    }
}
