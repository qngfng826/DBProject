package movie_rating_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import movie_rating_backend.entity.MovieRating;
import movie_rating_backend.entity.Rating;
import java.util.List;


public interface RatingService extends IService<Rating> {
    // 自定义方法：处理评分逻辑（调用 Mapper 里的 insertOrUpdate）
    void rateMovie(Rating rating);

    Rating getUserMovieRating(Integer userId, Integer movieId);

    List<MovieRating> getUserRatings(Integer userId);

    Double getAverageRating(Integer movieId);

    void updateRating(Rating rating);

    void removeRating(Integer ratingId);

    void removeByUserIdAndMovieId(Integer userId, Integer movieId);
}

