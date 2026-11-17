package movie_rating_backend.DTO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 「我的评分」查询投影 DTO：承接 RatingMapper.getUserRatings 的
 * rating1707 JOIN movie1707 联表查询结果（显式列投影）。
 * 非表实体，禁止作为 MyBatis-Plus 实体使用或对其做写操作。
 */
@Data
public class UserRatingDTO {
    private Integer ratingId;
    private Integer userId;
    private Integer movieId;
    private Integer score;
    private LocalDateTime ratingTime;
    // movie1707 投影列
    private String title;
    private Integer releaseYear;
    private Integer duration;
    private String genre;
    private String posterUrl;
}
