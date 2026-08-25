package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("movie1707")
public class MovieComment extends Movie{
    private Integer commentId;
    private Integer userId;
    private String content;
    private LocalDateTime commentTime;
}
