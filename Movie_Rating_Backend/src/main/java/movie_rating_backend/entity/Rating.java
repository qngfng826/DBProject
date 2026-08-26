package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rating1707")
public class Rating {
    @TableId(value = "RatingID")
    private Integer ratingId;
    private Integer userId;
    private Integer movieId;
    private Integer score;
    private LocalDateTime ratingTime;
}
