package movie_rating_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import movie_rating_backend.entity.MovieActor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieActorMapper extends BaseMapper<MovieActor> {
}
