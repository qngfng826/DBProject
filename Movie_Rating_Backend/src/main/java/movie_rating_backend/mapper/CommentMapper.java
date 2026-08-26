package movie_rating_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import movie_rating_backend.entity.Comment;
import movie_rating_backend.entity.MovieComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    List<Map<String, Object>> findCommentsByMovieTitle(@Param("title") String title);
    List<MovieComment> getUserComments(@Param("userId") Integer userId);
}

