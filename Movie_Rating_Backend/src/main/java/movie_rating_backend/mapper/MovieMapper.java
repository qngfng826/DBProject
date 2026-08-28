package movie_rating_backend.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import movie_rating_backend.entity.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface MovieMapper extends BaseMapper<Movie> {
    // 查询电影详情 (其实 selectById 是自带的，但这里手动声明方便你理解)
    Movie selectOneById(@Param("id") Integer id, @Param("userId") Integer userId);

    // 查询热门电影
    List<Map<String, Object>> getHotMovies();
}

