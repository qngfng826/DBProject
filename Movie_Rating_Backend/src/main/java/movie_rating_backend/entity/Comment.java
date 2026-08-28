package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment1707")
public class Comment {
    // 主键为 int AUTO_INCREMENT，必须用数据库自增，避免雪花 ID 溢出 int
    @TableId(value = "CommentID", type = IdType.AUTO)
    @JsonProperty("CommentID")
    private Integer commentId;

    @TableField("UserID")
    @JsonProperty("UserID")
    private Integer userId;

    @TableField("MovieID")
    @JsonProperty("MovieID")
    @JsonAlias({"movieId", "MovieID"})
    private Integer movieId;

    @TableField("Content")
    @JsonProperty("Content")
    @JsonAlias({"content"})
    private String content;

    @TableField("CommentTime")
    @JsonProperty("CommentTime")
    private LocalDateTime commentTime;

    @TableField(exist = false)
    @JsonProperty("Username")
    private String username;
}