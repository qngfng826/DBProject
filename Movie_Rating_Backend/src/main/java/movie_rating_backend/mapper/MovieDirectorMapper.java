package movie_rating_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import movie_rating_backend.entity.MovieDirector;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieDirectorMapper extends BaseMapper<MovieDirector> {
}
