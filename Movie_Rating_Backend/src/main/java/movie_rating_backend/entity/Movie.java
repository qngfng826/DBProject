package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import com.baomidou.mybatisplus.annotation.TableField;

@Data
@TableName("movie1707")
public class Movie {
    @TableId(type = IdType.AUTO)
    private Integer movieId;
    private String title;
//    @TableField(value = "releaseYear")
    private Integer releaseYear;
    private Integer duration;
    private String genre;
    private String language;
    private String country;
    private String synopsis;
    private BigDecimal rating;
    private String posterUrl;
    private String jumpUrl;

    @TableField(exist = false)
    private List<Director> directors;

    @TableField(exist = false)
    private List<Actor> actors;

    @TableField(exist = false)
    private BigDecimal userRating;
}
