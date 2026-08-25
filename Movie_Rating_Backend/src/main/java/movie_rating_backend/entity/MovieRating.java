package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("movie1707")
public class MovieRating extends Movie{
    private Integer ratingId;
    private Integer userId;
    private Integer score;
    private LocalDateTime ratingTime;
}
