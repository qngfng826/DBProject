package movie_rating_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import movie_rating_backend.entity.Comment;
import movie_rating_backend.mapper.CommentMapper;
import movie_rating_backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import movie_rating_backend.entity.MovieComment;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public List<Comment> getCommentsByMovieId(Integer movieId) {
        return list(new QueryWrapper<Comment>().eq("MovieID", movieId));
    }

    @Override
    public List<MovieComment> getUserComments(Integer userId) {
        return commentMapper.getUserComments(userId);
    }
}

