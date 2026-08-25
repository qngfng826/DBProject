package movie_rating_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import movie_rating_backend.entity.Comment;
import movie_rating_backend.entity.MovieComment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    List<Comment> getCommentsByMovieId(Integer movieId);

    List<MovieComment> getUserComments(Integer userId);
}

