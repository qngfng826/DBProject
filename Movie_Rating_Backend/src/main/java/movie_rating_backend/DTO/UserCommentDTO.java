package movie_rating_backend.DTO;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 「我的评论」查询投影 DTO：承接 CommentMapper.getUserComments 的
 * comment1707 JOIN movie1707 联表查询结果（显式列投影）。
 * 非表实体，禁止作为 MyBatis-Plus 实体使用或对其做写操作。
 */
@Data
public class UserCommentDTO {
    // movie1707 投影列
    private Integer movieId;
    private String title;
    private Integer releaseYear;
    private Integer duration;
    private String genre;
    private String language;
    private String country;
    private String synopsis;
    private BigDecimal rating;
    private String posterUrl;
    private String jumpUrl;
    // comment1707 投影列
    private Integer commentId;
    private Integer userId;
    private String content;
    private LocalDateTime commentTime;
}
