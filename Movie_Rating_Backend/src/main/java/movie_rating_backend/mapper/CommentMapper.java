package movie_rating_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    List<Comment> getMovieCommentsWithUsername(@Param("movieId") Integer movieId);

    // 后台评论管理：分页搜索，关键字匹配评论内容/用户名/电影标题
    IPage<Comment> searchCommentsWithUsername(Page<Comment> page, @Param("keyword") String keyword);
}

