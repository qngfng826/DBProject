package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment1707")
public class Comment {
    @TableId(value = "CommentID")
    private Integer commentId;
    private Integer userId;
    private Integer movieId;
    private String content;
    private LocalDateTime commentTime;
}