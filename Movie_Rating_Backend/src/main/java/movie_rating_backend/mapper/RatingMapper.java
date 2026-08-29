package movie_rating_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import movie_rating_backend.entity.MovieRating;
import movie_rating_backend.entity.Rating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;


@Mapper
public interface RatingMapper extends BaseMapper<Rating> {
    // 必须添加这个方法签名，对应 RatingMapper.xml 中的 insertOrUpdate
    void insertOrUpdate(@Param("rating") Rating rating);

    Rating getUserMovieRating(Integer userId, Integer movieId);

    List<MovieRating> getUserRatings(Integer userId);

    Double getAverageRating(Integer movieId);

    void updateRating(@Param("rating") Rating rating);

    // 重算电影平均分写回 movie1707.Rating（原数据库触发器的职责，TiDB 不支持触发器，改由服务层调用）
    void updateMovieRatingFromAvg(@Param("movieId") Integer movieId);

//    void deleteRating(Integer userId, Integer movieId);
//    delete操作使用MyBatis的delete方法
}

