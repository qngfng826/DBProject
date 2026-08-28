package movie_rating_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 电影-演员关联实体类
 */
@Data
@TableName("movie_actor1707")
public class MovieActor {
    @TableId(type = IdType.AUTO)
    private Integer movieActorId;

    private Integer movieId;

    private Integer actorId;

    private String roleName;

    public MovieActor() {
    }

    public MovieActor(Integer movieId, Integer actorId, String roleName) {
        this.movieId = movieId;
        this.actorId = actorId;
        this.roleName = roleName;
    }
}
