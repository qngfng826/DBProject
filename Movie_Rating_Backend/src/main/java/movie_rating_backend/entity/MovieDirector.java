package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 电影-导演关联实体类
 */
@Data
@TableName("movie_director1707")
public class MovieDirector {
    @TableId(type = IdType.AUTO)
    private Integer movieDirectorId;
    private Integer movieId;
    private Integer directorId;

    public MovieDirector() {
    }

    public MovieDirector(Integer movieId, Integer directorId) {
        this.movieId = movieId;
        this.directorId = directorId;
    }
}
